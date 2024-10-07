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

/**
 * The interop plugin that allows for communication with a training application
 * that was built using the Unity game engine and the GIFT Unity SDK.
 *
 * @author tflowers
 *
 */
public class SteelArttKafka extends AbstractInteropInterface {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelArttKafka.class);
    private final Properties consumerProps;
    private final String topic;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser jsonParser = new JSONParser();

    public SteelArttKafka(String displayName,Properties consumerProps, String topic) {
        super(displayName,false);
    }
    
    /**
     * The singleton collection of the message types which are supported.
     */
    private static final List<MessageTypeEnum> supportedMsgs = Arrays.asList(
            MessageTypeEnum.SIMAN,
            MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST
    );

    /**
     * The singleton collection of the message types which this interop plugin
     * can produce.
     */
    private static final List<MessageTypeEnum> producedMsgs = Arrays.asList(
            MessageTypeEnum.SIMPLE_EXAMPLE_STATE,
            MessageTypeEnum.STOP_FREEZE
    );

    /**
     * The collection of training applications that must be configured for this
     * interop plugin to run.
     */
    private static final List<TrainingApplicationEnum> REQUIRED_TRAINING_APPLICATIONS = Arrays.asList(
            TrainingApplicationEnum.UNITY_DESKTOP
    );

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgs;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes() {
        return producedMsgs;
    }
    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations() {
        return REQUIRED_TRAINING_APPLICATIONS;
    }

    @Override
    public Serializable getScenarios() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() throws DetailedException {
        return null;
    }

    @Override
    public void selectObject(Serializable objectIdentifier) throws DetailedException {

    }

    @Override
    public void loadScenario(String scenarioIdentifier) throws DetailedException {

    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }


    // goes inside common abstract class as a template, this will create the kafka consumer as well as the control channel socket
    @Override
    public boolean configure(Serializable config) throws ConfigurationException {

        if (config instanceof generated.gateway.Unity) {

            // Testing Kafka
            createKafkaDataConsumer();

            if (logger.isInfoEnabled()) {
                logger.info("Plugin has been configured");
            }

            return false;
        } else {
            throw new ConfigurationException(
                    "Unity Plugin interface can't configure.",
                    "The Unity Plugin interface only uses the interop config type of "
                            + generated.gateway.Unity.class
                            + " and doesn't support using the interop config instance of " + config,
                    null);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException {

        JSONObject jsonMsg;
        try {
            if (message.getMessageType().equals(MessageTypeEnum.SIMAN)) {
                Siman siman = (Siman) message.getPayload();

                JSONObject jsonLoadArgs = null;
                if (SimanTypeEnum.LOAD.equals(siman.getSimanTypeEnum())) {
                    jsonLoadArgs = new JSONObject();
                    final InteropInputs interopInputs = getLoadArgsByInteropImpl(getClass().getName(),
                            siman.getLoadArgs());

                    UnityInteropInputs unityInteropInputs = (UnityInteropInputs) interopInputs.getInteropInput();
                    for (Nvpair nv : unityInteropInputs.getLoadArgs().getNvpair()) {
                        jsonLoadArgs.put(nv.getName(), nv.getValue());
                    }

                }

                jsonMsg = EmbeddedAppMessageEncoder.encodeSimanForEmbeddedApplication(siman, jsonLoadArgs);
            } else {
                jsonMsg = EmbeddedAppMessageEncoder.encodeForEmbeddedApplication(message);
            }
        } catch (Exception e) {
            errorMsg.append("There was a problem encoding the following message for the Unity application: ")
                    .append(message).append('\n').append(e);
            return false;
        }

        final String jsonString = jsonMsg.toJSONString();
        try {
            controlSocketHandler.sendMessage(jsonString);
        } catch (IOException e) {
            logger.error("Caught exception when trying to send message to GIFT Unity SDK:\n"+jsonString, e);
            errorMsg.append("There was a problem sending the following message to the Unity application: ")
                    .append(jsonString).append('\n').append(e);
        }

        return false;
    }
    // goes inside common abstract class

    /**
     * The method that is invoked when a data message is received from the
     * Unity application. This method then should ideally send an ACK.
     *
     * @param line The text that was received from the Unity application.
     */

    // This method receives the ACKs for the control messages(sent by gift to unity)  sent by the unity app. 
    private void handleControlMessage(String line) {
        logger.info("handleControlMessage()");
        if (logger.isTraceEnabled()) {
            logger.trace("handleControlMessage('" + line + "')");
        }

        // Handle control message ACK here

        // Log the control message
        logger.info("Control message ACK received: {}", line);
    }

  

    @Override
    public void setEnabled(boolean value) throws ConfigurationException {
        super.setEnabled(value);

        if (value) {
            start();
        } else {
            stop();
        }
        
    }

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }
        stop();
    }

   private void createKafkaDataConsumer() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace IP with your Kafka broker address
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start reading from the end of the topic
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Disable auto-commit of offsets
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, "gift-unity-consumer"); // Set a unique client ID

            String topic = "scenario-topic"; // Change topic name here if required

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


    private void handleRawUnityMessage(String message) {
        try {
            final Object decodedMessage = EmbeddedAppMessageEncoder.decodeForGift(message);
            MessageTypeEnum msgType = EmbeddedAppMessageEncoder.getDecodedMessageType(decodedMessage);

            if (decodedMessage instanceof TrainingAppState) {
                GatewayModule.getInstance().sendMessageToGIFT((TrainingAppState) decodedMessage, msgType, this);
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
