package mil.arl.gift.gateway.interop.Steelartt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import generated.course.InteropInputs;
import generated.course.Nvpair;
import generated.course.UnityInteropInputs;
import generated.gateway.Unity;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.net.socket.AsyncSocketHandler;
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
import org.json.simple.parser.JSONParser;


public class SteelArttKafka extends SteelArttInteropTemplate {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelArttKafka.class);
    private final Properties consumerProps;
    private final String topic;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser jsonParser = new JSONParser();

    private Unity unityConfig;

    protected Unity getUnityConfig(){
        return this.unityConfig;
    }

    protected void setUnityConfig(Unity config){
        this.unityConfig = config;
    }

    /**
     * The socket handler that sends control mesgs to the unity app over socket and receives ACKs for the same.
     */
    private AsyncSocketHandler controlSocketHandler;

    public SteelArttKafka(String displayName,Properties consumerProps, String topic) {
        super(displayName);
        this.consumerProps = consumerProps;
        this.topic = topic;
    }

    protected void disconnectSocketHandlerOrKafka(){
        stopKafka();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        stopKafka(); //stop kafka connection
    }

    @Override
    protected void establishConnection() throws IOException{
        super.establishConnection();
        startKafka();
        createKafkaDataConsumer();
    }

    protected void createKafkaDataConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace localhost with the machine's IP running the Kafka server (if running on a different machine)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start reading from the end of the topic
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Disable auto-commit of offsets
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "gift-unity-consumer"); // Set a unique client ID

        String topic = "scenario-topic"; // Change topic name here if required

    }

    protected void startKafka() {
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

    protected void consumeMessages() {
        logger.debug("Entering consumerMessages method.");
        try {
            while (running.get()) {
                try{
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
                    logger.debug("Polled {} records", records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        logger.debug("Received message: topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
                        handleRawUnityMessage(record.value());
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

    protected void stopKafka() {
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
