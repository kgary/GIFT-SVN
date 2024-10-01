package mil.arl.gift.gateway.interop.Steelartt;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;

public class KafkaDataConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaDataConsumer.class);

    private final Properties consumerProps;
    private final String topic;
    private final SteelarttInterface steelarttInterface;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser jsonParser = new JSONParser();

    public KafkaDataConsumer(Properties consumerProps, String topic, SteelarttInterface steelarttInterface) {
        this.consumerProps = consumerProps;
        this.topic = topic;
        this.steelarttInterface = steelarttInterface;
        this.consumer = new KafkaConsumer<>(consumerProps);
        logger.debug("KafkaDataConsumer initialized with topic: {}", topic);

    }

    public void start() {
        if (running.getAndSet(true)) {
            logger.warn("Kafka consumer is already running.");
            return;
        }

        try {
            if (consumer == null) {
                logger.error("Kafka consumer is null. Cannot start.");
                return;
            }

            if (topic == null || topic.isEmpty()) {
                logger.error("Topic is null or empty. Cannot start Kafka consumer.");
                return;
            }

            consumer = new KafkaConsumer<>(consumerProps);
            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.assign(Collections.singletonList(partition));
            
            consumer.seekToEnd(Collections.singletonList(partition));
            long position = consumer.position(partition);
            consumer.seek(partition, position);

            logger.debug("Kafka consumer assigned to topic: {} at position: {}", topic, position);

            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::consumeMessages);
            logger.info("Kafka consumer started successfully without group management.");
        } catch (Exception e) {
            logger.error("Failed to start Kafka consumer", e);
            running.set(false);
            if(consumer != null){
                consumer.close();
            }
        }
    }

    private void consumeMessages() {
        logger.debug("Entering consumerMessages method.");
        try {
            while (running.get()) {
                try{
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
                    logger.debug("Polled {} records", records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        logger.debug("Received message: topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
                        handleMessage(record.value());
                    }
                } catch (WakeupException e) {
                    if (running.get()){
                        throw e;
                    }
                }
                
            } 
        } catch (Exception e) {
            if (running.get()) {
                logger.error("Error in Kafka consumer", e);
            }
        } finally {
            consumer.close();
            logger.debug("Entering consumerMessages method.");
        }
    }

    private void handleMessage(String message) {
        try {
            final Object decodedMessage = EmbeddedAppMessageEncoder.decodeForGift(message);
            MessageTypeEnum msgType = EmbeddedAppMessageEncoder.getDecodedMessageType(decodedMessage);

            if (decodedMessage instanceof TrainingAppState) {
                GatewayModule.getInstance().sendMessageToGIFT((TrainingAppState) decodedMessage, msgType, steelarttInterface);
            } else {
                final String typeName = decodedMessage != null ? decodedMessage.getClass().getName() : "null";
                logger.warn("A message of type '{}' was received from Kafka. "
                        + "It could not be sent to the DomainModule because it is not of type TrainingAppState", typeName);
            }
        } catch (ParseException e) {
            logger.error("Error parsing JSON from Kafka message: {}", message, e);
        } catch (Exception e) {
            logger.error("Error handling Kafka message: {}", message, e);
        }
    }

    public void stop() {
        logger.debug("Stopping Kafka Consumer.");
        running.set(false);
        if (consumer != null) {
            consumer.wakeup();
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        logger.info("Kafka consumer stopped.");
    }

}