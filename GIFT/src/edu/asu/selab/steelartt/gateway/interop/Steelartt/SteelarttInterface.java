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
import mil.arl.gift.gateway.interop.unity.UnityInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.net.socket.AsyncSocketHandler;

/**
 * The interop plugin that allows for communication with a training application
 * that was built using the Unity game engine and the GIFT Unity SDK.
 *
 * @author tflowers
 *
 */
public class SteelarttInterface extends UnityInterface {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelarttInterface.class);


    /**
     * The socket handler that recieves positional/speech data from the unity app over socket
     */
    // private AsyncSocketHandler dataSocketHandler;

    // Testing Kafka
    private KafkaDataConsumer kafkaDataConsumer;

    public SteelarttInterface(String displayName) {
        super(displayName);
    }
    

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        super.configure(config);

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
            kafkaDataConsumer.start();
        } else {
            kafkaDataConsumer.stop();
        }
        
    }

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }
        super.cleanup();
        if (kafkaDataConsumer != null) {
            kafkaDataConsumer.stop();
            kafkaDataConsumer = null;
        }
    }

   private void createKafkaDataConsumer() {
        if (kafkaDataConsumer == null) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace IP with your Kafka broker address
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start reading from the end of the topic
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Disable auto-commit of offsets
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, "gift-unity-consumer"); // Set a unique client ID

            String topic = "scenario-topic"; // Change topic name here if required

            kafkaDataConsumer = new KafkaDataConsumer(props, topic, this);
        }
    }
}
