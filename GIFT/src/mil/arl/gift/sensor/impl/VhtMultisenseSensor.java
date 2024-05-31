/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.JMSException;
import javax.vecmath.Point3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.enums.vht.BehaviorTypeEnum;
import mil.arl.gift.common.enums.vht.GazeCategoryDirectionEnum;
import mil.arl.gift.common.enums.vht.GazeCategoryHorizontalEnum;
import mil.arl.gift.common.enums.vht.GazeCategoryVerticalEnum;
import mil.arl.gift.common.enums.vht.PoseTypeEnum;
import mil.arl.gift.common.enums.vht.PostureTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.net.api.MessageClient;
import mil.arl.gift.net.api.TopicMessageClient;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.sensor.SensorData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A sensor implementation for generating sensor data coming from Multisense
 * through the Virtual Human Toolkit
 *
 * @author jleonard
 */
public class VhtMultisenseSensor extends AbstractSensor {

    private static Logger logger = LoggerFactory.getLogger(MouseTempHumiditySensor.class);
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.POSTURE_TYPE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ACTIVITY_TYPE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT_TYPE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT_VALUE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HORIZONTAL_GAZE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.VERTICAL_GAZE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GAZE_DIRECTION, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_HAND_POSE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_HAND_POSE_ROT, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_HAND_POSE_TYPE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_HAND_POSE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_HAND_POSE_ROT, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_HAND_POSE_TYPE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ATTENTION_VALUE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HEAD_POSE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HEAD_POSE_ROT, Tuple3dValue.class));
    }

    private long lastSendTime = 0;

    private static final int ZERO = 0;

    /**
     * XML tag element labels
     */
    private static final String BODY = "body";
    private static final String LAYER_ONE = "layer1";
    private static final String LAYER_TWO = "layer2";
    private static final String ACTIVITY = "activity";
    private static final String ATTENTION = "attention";
    private static final String ENGAGEMENT = "engagement";
    private static final String BEHAVIOR_TYPE = "behaviorType";
    private static final String BEHAVIOR_VALUE = "behaviorValue";
    private final static String POSTURE = "posture";
    private final static String POSTURE_TYPE = "postureType";
    private final static String HEAD_POSE = "headPose";
    private final static String POSITION = "position";
    private final static String ROTATION = "rotation";
    private final static String HAND_POSE_LEFT = "handPoseLeft";
    private final static String POSE_TYPE = "poseType";
    private final static String HAND_POSE_RIGHT = "handPoseRight";
    private final static String FACE_GAZE = "faceGaze";
    private final static String GAZE_CATEGORY_HORIZONTAL = "gazeCategoryHorizontal";
    private final static String GAZE_CATEGORY_VERTICAL = "gazeCategoryVertical";
    private final static String GAZE_CATEGORY_DIRECTION = "gazeCategoryDirection";
    private static final String VR_PERCEPTION = "vrPerception";
    private static final String UTF_8_ENCODING = "utf-8";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String ROT_X = "rotX";
    private static final String ROT_Y = "rotY";
    private static final String ROT_Z = "rotZ";

    /**
     * An ActiveMQ message handler for dealing with messages coming from the VHT
     * topic
     */
    private RawMessageHandler messageHandler = new RawMessageHandler() {
        @Override
        public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

            long timeNow = System.currentTimeMillis();

            // Check to see if it is time to process another Multisense message
            if (VhtMultisenseSensor.this.getSensorInterval() == null || (timeNow - lastSendTime) > (getSensorInterval() * 1000)) {

                // Multisense messages will always start with "vrPerception"
                if (msg.startsWith(VR_PERCEPTION)) {

                    try {

                        // Everything after vrPerception is Percent-encoded
                        String perceptionMsgEncoded = msg.substring(VR_PERCEPTION.length() + 1);
                        String perceptionMsg = URLDecoder.decode(perceptionMsgEncoded, UTF_8_ENCODING);

                        // There's some nonsense between vrPerception and the start of the XML, culling that out
                        String perceptionXml = perceptionMsg.substring(perceptionMsg.indexOf('<'));

                        SensorData data = processVhtMessage(perceptionXml);

                        if (data != null) {

                            if (datalessWarningTimer != null) {

                                datalessWarningTimer.cancel();

                                datalessWarningTimer = null;
                            }

                            sendDataEvent(data);
                        }

                        lastSendTime = timeNow;

                    } catch (UnsupportedEncodingException ex) {

                        logger.error("Could not decode VHT message.", ex);
                    }
                }
            }

            return true;
        }
    };

    private Timer datalessWarningTimer = new Timer("VhtMultisenseWarningTimer");

    /**
     * Task for sending a message that Multisense has not sent any data yet
     */
    private TimerTask datalessWarningTask = new TimerTask() {
        @Override
        public void run() {

            if (sensorState == SensorStateEnum.RUNNING) {

                createSensorStatus("The Multisense sensor has not received any data yet.");
            }
        }
    };

    /**
     * The URL of the ActiveMQ VHT is running on
     */
    private String vhtActiveMqUrl = "tcp://localhost:61616";

    /**
     * The Topic in ActiveMQ that VHT is running on
     */
    private String vhtActiveMqTopicName = "DEFAULT_SCOPE";

    /**
     * How long to wait after the start of the sensor to warn that no Multisense
     * data has been received yet
     */
    private long datalessWarningDelay = 0;

    private MessageClient vhtMessageClient = null;

    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public VhtMultisenseSensor(String sensorName) {
        super(sensorName, SensorTypeEnum.VHT_MULTISENSE);
        setEventProducerInformation(eventProducerInformation);
    }

    /**
     * Class constructor - configure using the sensor configuration input for
     * this sensor
     *
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public VhtMultisenseSensor(String sensorName, generated.sensor.VhtMultisenseSensor configuration) {
        this(sensorName);

        if (configuration.getVhtActiveMqUrl() != null) {

            vhtActiveMqUrl = configuration.getVhtActiveMqUrl();
        }

        if (configuration.getVhtActiveMqTopic() != null) {

            vhtActiveMqTopicName = configuration.getVhtActiveMqTopic();
        }

        if (configuration.getDatalessWarningDelay() != null) {

            datalessWarningDelay = configuration.getDatalessWarningDelay() * 1000;
        }
    }

    @Override
    public boolean test() {

        final AtomicBoolean gotValue = new AtomicBoolean(false);

        MessageClient testClient = new TopicMessageClient(vhtActiveMqUrl, vhtActiveMqTopicName);

        testClient.setMessageHandler(new RawMessageHandler() {
            @Override
            public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                // Multisense messages will always start with "vrPerception"
                if (msg.startsWith(VR_PERCEPTION)) {

                    try {

                        // Everything after vrPerception is Percent-encoded
                        String perceptionMsgEncoded = msg.substring(VR_PERCEPTION.length() + 1);
                        String perceptionMsg = URLDecoder.decode(perceptionMsgEncoded, UTF_8_ENCODING);

                        // There's some nonsense between vrPerception and the start of the XML, culling that out
                        String perceptionXml = perceptionMsg.substring(perceptionMsg.indexOf('<'));

                        SensorData data = processVhtMessage(perceptionXml);

                        if (data != null) {

                            gotValue.set(true);

                            synchronized (gotValue) {

                                gotValue.notifyAll();
                            }
                        }

                    } catch (UnsupportedEncodingException ex) {

                        logger.error("Could not decode VHT message.", ex);
                    }
                }

                return true;
            }
        });

        try {

            testClient.connect();

            synchronized (gotValue) {

                try {
                    gotValue.wait(10000);
                } catch (@SuppressWarnings("unused") InterruptedException ex) {
                }
            }
            
            testClient.disconnect(false);

            return gotValue.get();

        } catch (JMSException e) {

            logger.error("Unable to connect to VHT: Exception caught when connecting: " + e, e);
        }

        return false;
    }

    @Override
    public void start(long domainSessionStartTime) throws Exception {

        if (vhtMessageClient == null || sensorState == SensorStateEnum.STOPPED) {

            super.start(domainSessionStartTime);

            if (vhtActiveMqUrl != null) {

                if (vhtActiveMqTopicName != null) {

                    vhtMessageClient = new TopicMessageClient(vhtActiveMqUrl, vhtActiveMqTopicName);

                    vhtMessageClient.setMessageHandler(messageHandler);

                    try {

                        vhtMessageClient.connect();

                        sensorState = SensorStateEnum.RUNNING;

                        if (datalessWarningDelay > 0) {

                            datalessWarningTimer.schedule(datalessWarningTask, datalessWarningDelay);
                        }

                        logger.info("Sensor Thread started for " + this);

                    } catch (JMSException e) {
                        createSensorError("Unable to connect to VHT: Exception caught when connecting: " + e);
                        logger.error("Unable to connect to VHT: Exception caught when connecting: " + e, e);
                    }
                } else {
                    createSensorError("Unable to connect to VHT: No ActiveMQ Topic specified");
                    logger.error("Unable to connect to VHT: No ActiveMQ Topic specified");
                }
            } else {
                createSensorError("Unable to connect to VHT: No ActiveMQ URL specified");
                logger.error("Unable to connect to VHT: No ActiveMQ URL specified");
            }
        }
    }

    @Override
    public void stop() {

        if (vhtMessageClient != null) {
            super.stop();
            sensorState = SensorStateEnum.STOPPED;

            vhtMessageClient.disconnect(false);
            logger.info("Sensor Thread stopped for " + this);
        }
    }

    /**
     * Processes a VHT message in to a Sensor Data event if it is from
     * Multisense
     *
     * Multisense XML data follows the Perception Markup Language schema, which
     * is defined in the paper "Perception Markup Language: Towards a
     * Standardized Representation of Perceived Nonverbal Behavior" by Stefan
     * Scherer, Stacy Marsella, Giota Stratou, Yuyu Xu, Fabrizio Morbini, Alesia
     * Egan, Albert (Skip) Rizzo and Louis-Philippe Morency
     *
     * @param message The XML formatted Multisense message
     */
    private SensorData processVhtMessage(String message) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(message));
            Document document = builder.parse(is);

            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();

            Element pmlElement = document.getDocumentElement();

            Element bodyElement = (Element) pmlElement.getElementsByTagName(BODY).item(ZERO);

            parseBodyElement(bodyElement, sensorAttributeToValue);

            long elapsedTime = System.currentTimeMillis() - getDomainSessionStartTime();

            return new SensorData(sensorAttributeToValue, elapsedTime);

        } catch (SAXException | ParserConfigurationException | IOException | NumberFormatException ex) {

            logger.error("Caught an exception while parsing Multisense data", ex);

            createSensorError("Could not parse Multisense data: " + ex);
        }

        return null;
    }

    /**
     * Parses the Body element of the PML schema
     *
     * @param bodyElement
     * @param sensorAttributeToValue
     */
    private static void parseBodyElement(Element bodyElement, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue) {

        Element layerOneElement = (Element) bodyElement.getElementsByTagName(LAYER_ONE).item(ZERO);

        parseLayerOneElement(layerOneElement, sensorAttributeToValue);

        Element layerTwoElement = (Element) bodyElement.getElementsByTagName(LAYER_TWO).item(ZERO);

        parseLayerTwoElement(layerTwoElement, sensorAttributeToValue);
    }

    /**
     * Parses the Layer One element of the PML schema
     *
     * @param layerOneElement The layer one element
     * @param sensorAttributeToValue
     */
    private static void parseLayerOneElement(Element layerOneElement, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue) {

        Element activityElement = (Element) layerOneElement.getElementsByTagName(ACTIVITY).item(ZERO);

        // Parse the activity element

        if (activityElement != null) {

            Element activityBehaviorTypeElement = (Element) activityElement.getElementsByTagName(BEHAVIOR_TYPE).item(ZERO);

            BehaviorTypeEnum activityBehaviorType = BehaviorTypeEnum.valueOf(activityBehaviorTypeElement.getTextContent());

            if (activityBehaviorType != null) {

                sensorAttributeToValue.put(SensorAttributeNameEnum.ACTIVITY_TYPE, new DoubleValue(SensorAttributeNameEnum.ACTIVITY_TYPE, activityBehaviorType.getValue()));
            }
        }

        // Parse the attention element

        Element attentionElement = (Element) layerOneElement.getElementsByTagName(ATTENTION).item(ZERO);

        if (attentionElement != null) {

            Element attentionBehaviorTypeElement = (Element) attentionElement.getElementsByTagName(BEHAVIOR_TYPE).item(ZERO);

            String attentionBehaviorTypeString = attentionBehaviorTypeElement.getTextContent();

            if (attentionBehaviorTypeString != null) {

                BehaviorTypeEnum attentionBehaviorType = BehaviorTypeEnum.valueOf(attentionBehaviorTypeElement.getTextContent());

                if (attentionBehaviorType != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.ATTENTION_TYPE, new DoubleValue(SensorAttributeNameEnum.ATTENTION_TYPE, attentionBehaviorType.getValue()));
                }
            }

            Element attentionBehaviorValueElement = (Element) attentionElement.getElementsByTagName(BEHAVIOR_VALUE).item(ZERO);

            String attentionBehaviorValueString = attentionBehaviorValueElement.getTextContent();

            if (attentionBehaviorValueString != null) {

                sensorAttributeToValue.put(SensorAttributeNameEnum.ATTENTION_VALUE, new DoubleValue(SensorAttributeNameEnum.ATTENTION_VALUE, Double.parseDouble(attentionBehaviorValueString)));
            }
        }

        // Parse the engagement element

        Element engagementElement = (Element) layerOneElement.getElementsByTagName(ENGAGEMENT).item(ZERO);

        if (engagementElement != null) {

            Element engagementBehaviorTypeElement = (Element) engagementElement.getElementsByTagName(BEHAVIOR_TYPE).item(ZERO);

            String engagementBehaviorTypeString = engagementBehaviorTypeElement.getTextContent();

            if (engagementBehaviorTypeString != null) {

                BehaviorTypeEnum engagementBehaviorType = BehaviorTypeEnum.valueOf(engagementBehaviorTypeElement.getTextContent());

                if (engagementBehaviorType != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.ENGAGEMENT_TYPE, new DoubleValue(SensorAttributeNameEnum.ENGAGEMENT_TYPE, engagementBehaviorType.getValue()));
                }
            }

            Element engagementBehaviorValueElement = (Element) engagementElement.getElementsByTagName(BEHAVIOR_VALUE).item(ZERO);

            String engagementBehaviorValueString = engagementBehaviorValueElement.getTextContent();

            if (engagementBehaviorValueString != null) {

                sensorAttributeToValue.put(SensorAttributeNameEnum.ENGAGEMENT_VALUE, new DoubleValue(SensorAttributeNameEnum.ENGAGEMENT_VALUE, Double.parseDouble(engagementBehaviorValueString)));
            }
        }
    }

    /**
     * Parses the Layer Two element of the PML schema
     *
     * @param layerTwoElement The layer two element
     * @param sensorAttributeToValue
     */
    private static void parseLayerTwoElement(Element layerTwoElement, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue) {

        if (layerTwoElement != null) {

            // Parse the posture element

            Element postureElement = (Element) layerTwoElement.getElementsByTagName(POSTURE).item(ZERO);

            if (postureElement != null) {

                Element postureTypeElement = (Element) postureElement.getElementsByTagName(POSTURE_TYPE).item(ZERO);

                String postureTypeString = postureTypeElement.getTextContent();

                if (postureTypeString != null) {

                    PostureTypeEnum postureType = PostureTypeEnum.valueOf(postureTypeString);

                    if (postureType != null) {

                        sensorAttributeToValue.put(SensorAttributeNameEnum.POSTURE_TYPE, new DoubleValue(SensorAttributeNameEnum.POSTURE_TYPE, postureType.getValue()));
                    }
                }
            }

            // Parse the head pose element

            Element headPoseElement = (Element) layerTwoElement.getElementsByTagName(HEAD_POSE).item(ZERO);

            Element positionElement = (Element) headPoseElement.getElementsByTagName(POSITION).item(ZERO);

            sensorAttributeToValue.put(SensorAttributeNameEnum.HEAD_POSE, new Tuple3dValue(SensorAttributeNameEnum.HEAD_POSE, parsePosition(positionElement)));

            Element rotationElement = (Element) headPoseElement.getElementsByTagName(ROTATION).item(ZERO);

            sensorAttributeToValue.put(SensorAttributeNameEnum.HEAD_POSE_ROT, new Tuple3dValue(SensorAttributeNameEnum.HEAD_POSE_ROT, parseRotation(rotationElement)));

            // Parse the left hand element

            Element handPoseLeftElement = (Element) layerTwoElement.getElementsByTagName(HAND_POSE_LEFT).item(ZERO);

            if (handPoseLeftElement != null) {

                Element handPoseLeftPositionElement = (Element) handPoseLeftElement.getElementsByTagName(POSITION).item(ZERO);

                sensorAttributeToValue.put(SensorAttributeNameEnum.LEFT_HAND_POSE, new Tuple3dValue(SensorAttributeNameEnum.LEFT_HAND_POSE, parsePosition(handPoseLeftPositionElement)));

                Element handPoseLeftTypeElement = (Element) handPoseLeftElement.getElementsByTagName(POSE_TYPE).item(ZERO);

                String handPoseLeftTypeString = handPoseLeftTypeElement.getTextContent();

                PoseTypeEnum handPoseLeftType = PoseTypeEnum.valueOf(handPoseLeftTypeString);

                if (handPoseLeftType != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.LEFT_HAND_POSE_TYPE, new DoubleValue(SensorAttributeNameEnum.LEFT_HAND_POSE_TYPE, handPoseLeftType.getValue()));
                }

                Element handPoseLeftRotationElement = (Element) handPoseLeftElement.getElementsByTagName(ROTATION).item(ZERO);

                sensorAttributeToValue.put(SensorAttributeNameEnum.LEFT_HAND_POSE_ROT, new Tuple3dValue(SensorAttributeNameEnum.LEFT_HAND_POSE_ROT, parseRotation(handPoseLeftRotationElement)));
            }

            // Parse the right hand element

            Element handPoseRightElement = (Element) layerTwoElement.getElementsByTagName(HAND_POSE_RIGHT).item(ZERO);

            if (handPoseRightElement != null) {

                Element handPoseRightPositionElement = (Element) handPoseRightElement.getElementsByTagName(POSITION).item(ZERO);

                sensorAttributeToValue.put(SensorAttributeNameEnum.RIGHT_HAND_POSE, new Tuple3dValue(SensorAttributeNameEnum.RIGHT_HAND_POSE, parsePosition(handPoseRightPositionElement)));

                Element handPoseRightTypeElement = (Element) handPoseRightElement.getElementsByTagName(POSE_TYPE).item(ZERO);

                String handPoseRightTypeString = handPoseRightTypeElement.getTextContent();

                PoseTypeEnum handPoseRightType = PoseTypeEnum.valueOf(handPoseRightTypeString);

                if (handPoseRightType != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.RIGHT_HAND_POSE_TYPE, new DoubleValue(SensorAttributeNameEnum.RIGHT_HAND_POSE_TYPE, handPoseRightType.getValue()));
                }

                Element handPoseRightRotationElement = (Element) handPoseRightElement.getElementsByTagName(ROTATION).item(ZERO);

                sensorAttributeToValue.put(SensorAttributeNameEnum.RIGHT_HAND_POSE_ROT, new Tuple3dValue(SensorAttributeNameEnum.RIGHT_HAND_POSE_ROT, parseRotation(handPoseRightRotationElement)));
            }

            // Parse the face gaze element

            Element faceGazeElement = (Element) layerTwoElement.getElementsByTagName(FACE_GAZE).item(ZERO);

            Element gazeCategoryHorizontalElement = (Element) layerTwoElement.getElementsByTagName(GAZE_CATEGORY_HORIZONTAL).item(ZERO);

            if (gazeCategoryHorizontalElement != null) {

                String gazeCategoryHorizontalString = gazeCategoryHorizontalElement.getTextContent();
                GazeCategoryHorizontalEnum gazeCategoryHorizontal = GazeCategoryHorizontalEnum.valueOf(gazeCategoryHorizontalString);

                if (gazeCategoryHorizontal != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.HORIZONTAL_GAZE, new DoubleValue(SensorAttributeNameEnum.HORIZONTAL_GAZE, gazeCategoryHorizontal.getValue()));
                }
            }

            Element gazeCategoryVerticalElement = (Element) faceGazeElement.getElementsByTagName(GAZE_CATEGORY_VERTICAL).item(ZERO);

            if (gazeCategoryVerticalElement != null) {

                String gazeCategoryVerticalString = gazeCategoryVerticalElement.getTextContent();
                GazeCategoryVerticalEnum gazeCategoryVertical = GazeCategoryVerticalEnum.valueOf(gazeCategoryVerticalString);

                if (gazeCategoryVertical != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.VERTICAL_GAZE, new DoubleValue(SensorAttributeNameEnum.VERTICAL_GAZE, gazeCategoryVertical.getValue()));
                }
            }

            Element gazeCategoryDirectionElement = (Element) faceGazeElement.getElementsByTagName(GAZE_CATEGORY_DIRECTION).item(ZERO);

            if (gazeCategoryDirectionElement != null) {

                String gazeCategoryDirectionString = gazeCategoryDirectionElement.getTextContent();
                GazeCategoryDirectionEnum gazeCategoryDirection = GazeCategoryDirectionEnum.valueOf(gazeCategoryDirectionString);

                if (gazeCategoryDirection != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.GAZE_DIRECTION, new DoubleValue(SensorAttributeNameEnum.GAZE_DIRECTION, gazeCategoryDirection.getValue()));
                }
            }
        }
    }

    /**
     * Parses a position in to an object
     *
     * @param positionElement The element with position elements
     * @return The position object
     */
    private static Point3d parsePosition(Element positionElement) {

        String posX = positionElement.getAttribute(X);
        String posY = positionElement.getAttribute(Y);
        String posZ = positionElement.getAttribute(Z);

        double x = Double.parseDouble(posX);

        double y = Double.parseDouble(posY);

        double z = Double.parseDouble(posZ);

        return new Point3d(x, y, z);
    }

    /**
     * Parses a rotation in to an object
     *
     * @param rotationElement The elsment with rotation elements
     * @return Point3d The rotation object
     */
    private static Point3d parseRotation(Element rotationElement) {

        String rotXString = rotationElement.getAttribute(ROT_X);
        String rotYString = rotationElement.getAttribute(ROT_Y);
        String rotZString = rotationElement.getAttribute(ROT_Z);

        double rotX = Double.parseDouble(rotXString);

        double rotY = Double.parseDouble(rotYString);

        double rotZ = Double.parseDouble(rotZString);

        return new Point3d(rotX, rotY, rotZ);

    }
}
