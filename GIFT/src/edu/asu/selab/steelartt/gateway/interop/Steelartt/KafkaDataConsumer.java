package mil.arl.gift.gateway.interop.Steelartt;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final SteelarttInterface steelarttInterface;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser jsonParser = new JSONParser();

    public KafkaDataConsumer(String bootstrapServers, String topic, String groupId, SteelarttInterface steelarttInterface) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.steelarttInterface = steelarttInterface;
    }

    public void start() {
        if (running.get()) {
            logger.warn("Kafka consumer is already running.");
            return;
        }

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        running.set(true);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::consumeMessages);
    }

    private void consumeMessages() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    handleMessage(record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }

    private void handleMessage(String message) {
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
            String jsonString = jsonObject.toJSONString();
            
            final Object decodedMessage = EmbeddedAppMessageEncoder.decodeForGift(jsonString);
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
        if (!running.getAndSet(false)) {
            logger.warn("Kafka consumer is not running.");
            return;
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }

        if (consumer != null) {
            consumer.wakeup();
        }
    }
}