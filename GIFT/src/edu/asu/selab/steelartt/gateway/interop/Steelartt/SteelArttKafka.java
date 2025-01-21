package mil.arl.gift.gateway.interop.Steelartt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.errors.WakeupException;
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



public class SteelArttKafka extends SteelArttInteropTemplate {

    /** The __logger for the class */
    private static final Logger __logger = LoggerFactory.getLogger(SteelArttKafka.class);
    private final Properties __consumerProps;
    private final String __topic;
    private final AtomicBoolean __running = new AtomicBoolean(false);
    private ExecutorService __executorService;
    private KafkaConsumer<String, String> __consumer;

    private Unity __unityConfig;

    protected Unity _getUnityConfig(){
        return this.__unityConfig;
    }

    protected void _setUnityConfig(Unity config){
        this.__unityConfig = config;
    }

    /**
     * The socket handler that sends control mesgs to the unity app over socket and receives ACKs for the same.
     */
    private AsyncSocketHandler __controlSocketHandler;

    public SteelArttKafka(String displayName) {
        super(displayName);
        this.__consumerProps = __getDefaultProperties();
        this.__topic = __getDefaultTopic();
        this.__consumer = new KafkaConsumer<>(this.__consumerProps);
    }

    private String __getDefaultTopic(){
        return "scenario-topic";
    }

    private Properties __getDefaultProperties(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace localhost with the machine's IP running the Kafka server (if running on a different machine)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start reading from the end of the _topic
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Disable auto-commit of offsets
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "gift-unity-consumer"); // Set a unique client ID

        return props;
    }

    protected void _disconnectSocketHandlerOrKafka(){
        _stopKafka();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        _stopKafka(); //stop kafka connection
    }

    @Override
    protected void _establishConnection() throws IOException{
        super._establishConnection();
        __logger.info("about to start Kafka");
        _startKafka();
    }

    protected void _startKafka() {
        __logger.info("starting Kafka");
        if (__running.getAndSet(true)) {
            __logger.warn("Kafka consumer is already running.");
            return;
        }

        try {
            if (__consumer == null) {
                __logger.error("Kafka consumer is null. Cannot start.");
                return;
            }

            if (__topic == null || __topic.isEmpty()) {
                __logger.error("Topic is null or empty. Cannot start Kafka consumer.");
                return;
            }

            __consumer = new KafkaConsumer<>(__consumerProps);
            TopicPartition partition = new TopicPartition(__topic, 0);
            __consumer.assign(Collections.singletonList(partition));
            
            __consumer.seekToEnd(Collections.singletonList(partition));
            long position = __consumer.position(partition);
            __consumer.seek(partition, position);

            __logger.debug("Kafka consumer assigned to topic: {} at position: {}", __topic, position);

            __executorService = Executors.newSingleThreadExecutor();
            __executorService.submit(this::_consumeMessages);
            __logger.info("Kafka consumer started successfully without group management.");
        } catch (Exception e) {
            __logger.error("Failed to start Kafka consumer", e);
            __running.set(false);
            if(__consumer != null){
                __consumer.close();
            }
        }
    }

    protected void _consumeMessages() {
        __logger.debug("Entering consumerMessages method.");
        try {
            while (__running.get()) {
                try{
                    ConsumerRecords<String, String> records = __consumer.poll(Duration.ofMillis(3000));
                    __logger.debug("Polled {} records", records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        __logger.debug("Received message: topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
                        _handleRawUnityMessage(record.value());
                    }
                } catch (WakeupException e) {
                    if (__running.get()){
                        throw e;
                    }
                }
                
            } 
        } catch (Exception e) {
            if (__running.get()) {
                __logger.error("Error in Kafka consumer", e);
            }
        } finally {
            __consumer.close();
            __logger.info("Exiting consumerMessages method.");
        }
    }

    protected void _stopKafka() {
        __logger.info("Stopping Kafka Consumer.");
        __running.set(false);
        if (__consumer != null) {
            __consumer.wakeup();
        }
        if (__executorService != null) {
            __executorService.shutdown();
            try {
                if (!__executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    __executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                __executorService.shutdownNow();
            }
        }
        __logger.info("Kafka consumer stopped.");
    }

}
