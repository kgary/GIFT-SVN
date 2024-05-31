/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jdis.PDUReader;
import org.jdis.pdu.EntityStatePDU;
import org.jdis.pdu.PDU;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import mil.arl.gift.common.Siman;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.common.ta.state.SimpleExampleState;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.dis.DISToGIFTConverter;
import mil.arl.gift.net.embedded.message.EmbeddedBinaryData;
import mil.arl.gift.net.embedded.message.EmbeddedBinaryData.BinaryDataType;
import mil.arl.gift.net.embedded.message.EmbeddedGenericJSONState;
import mil.arl.gift.net.embedded.message.EmbeddedGeolocation;
import mil.arl.gift.net.embedded.message.EmbeddedSiman;
import mil.arl.gift.net.embedded.message.EmbeddedSimpleExampleState;
import mil.arl.gift.net.embedded.message.EmbeddedStopFreeze;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedBinaryDataJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedGenericJSONStateJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedGeolocationJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedSimanJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedSimpleExampleStateJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedStopFreezeJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedStringPayloadJSON;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedVibrateDeviceJSON;
import mil.arl.gift.net.json.JSONCodec;

/**
 * A utility class used to encode objects to JSON to use in embedded applications and decode JSON to objects to use in GIFT.
 * <br/><br/>
 * Note that this class is restricted to GIFT's side of the communication protocol and does not control how embedded applications
 * themselves encode and decode messages. Because of this, changing how a particular message type is encoded or decoded in this
 * class will not carry over to embedded applications written using the previous encoding, meaning that those applications will
 * need to be updated to match the encodings used by this class.
 *
 *
 * @author nroberts
 */
public class EmbeddedAppMessageEncoder {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedAppMessageEncoder.class);

    /** The key corresponding to an embedded app message's type */
	private static String TYPE_ATTR = "type";

	/** The key corresponding to an embedded app message's payload */
	private static String PAYLOAD_ATTR = "payload";

    /** The codec used for {@link EmbeddedSiman} messages. */
	private static EmbeddedSimanJSON SIMAN_JSON_CODEC = new EmbeddedSimanJSON();

    /** The codec used for {@link String} messages. */
	private static EmbeddedStringPayloadJSON STRING_JSON_CODEC = new EmbeddedStringPayloadJSON();

    /** The codec used for vibrate messages. */
	private static EmbeddedVibrateDeviceJSON VIBRATE_JSON_CODEC = new EmbeddedVibrateDeviceJSON();

    /** The codec used for {@link StopFreeze} messages. */
	private static EmbeddedStopFreezeJSON STOP_FREEZE_JSON_CODEC = new EmbeddedStopFreezeJSON();

    /** The codec used for {@link SimpleExampleState} messages. */
	private static EmbeddedSimpleExampleStateJSON SIMPLE_EXAMPLE_STATE_JSON_CODEC = new EmbeddedSimpleExampleStateJSON();

    /** The codec used for {@link Geolocation} messages. */
	private static EmbeddedGeolocationJSON GEOLOCATION_JSON_CODEC = new EmbeddedGeolocationJSON();

    /** The codec used for {@link GenericJSONState} messages. */
	private static EmbeddedGenericJSONStateJSON GENERIC_JSON_STATE_JSON_CODEC = new EmbeddedGenericJSONStateJSON();

    /** The codec used for {@link BinaryData} messages. */
	private static EmbeddedBinaryDataJSON BINARY_DATA_JSON_CODEC = new EmbeddedBinaryDataJSON();

	/**
	 * An enumeration of object types that are supported for encoding/decoding. The name of an object's type will be added to its
	 * JSON encoding upon invoking {@link EmbeddedAppMessageEncoder#encodeForEmbeddedApplication(Object)} so that embedded applications
	 * can tell what type of object is being passed. Note that the decoding logic in
	 * {@link EmbeddedAppMessageEncoder#decodeForGift(String)} expects a similar encoding scheme to be used by embedded applications,
	 * so embedded applications will have to add these types to their messages as well in order for GIFT to decode them properly.
	 *
	 * @author nroberts
	 */
	private enum EncodedMessageType{
        /** Simulation management messages. */
        Siman,
        /** Messages for delivering feedback to the learner. */
        Feedback,
        /** A message used to stop the scenario. */
        StopFreeze,
        /** A simple string message. */
        SimpleExampleState,
        /** A non-specific JSON message. */
        GenericJSONState,
        /** An acknowledgement of a SIMAN message. */
        SimanResponse,
        /** A message used to vibrate the user's device. */
        Vibrate,
        /** A message containing the location of a learner. */
        Geolocation,
        /** A message containing raw binary data. */
        BinaryData
	}

	/**
	 * A map from each know GIFT object type to the associated JSON codec used to encode and decode it. GIFT objects with message
	 * types not in this map or with no message type will be encoded/decoded by GSON using reflection as a fallback.
	 */
	private static Map<EncodedMessageType, JSONCodec> messageTypeToCodec = new HashMap<>();

	static{

		//populate the map of codecs
		messageTypeToCodec.put(EncodedMessageType.Siman, SIMAN_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.Feedback, STRING_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.Vibrate, VIBRATE_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.StopFreeze, STOP_FREEZE_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.SimpleExampleState, SIMPLE_EXAMPLE_STATE_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.Geolocation, GEOLOCATION_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.GenericJSONState, GENERIC_JSON_STATE_JSON_CODEC);
		messageTypeToCodec.put(EncodedMessageType.BinaryData, BINARY_DATA_JSON_CODEC );
	}

	/**
	 * A mapping used to determine what {@link MessageTypeEnum} an object decoded from an embedded application corresponds to.
	 * This is used to push an appropriate {@link Message} to listening modules once GIFT receives it.
	 */
	private static Map<Class<?>, MessageTypeEnum> decodedPayloadClassToMessageType = new HashMap<>();

	static {
        decodedPayloadClassToMessageType.put(ACK.class, MessageTypeEnum.PROCESSED_ACK);
        decodedPayloadClassToMessageType.put(SimpleExampleState.class, MessageTypeEnum.SIMPLE_EXAMPLE_STATE);
        decodedPayloadClassToMessageType.put(StopFreeze.class, MessageTypeEnum.STOP_FREEZE);
        decodedPayloadClassToMessageType.put(Geolocation.class, MessageTypeEnum.GEOLOCATION);
        decodedPayloadClassToMessageType.put(EntityState.class, MessageTypeEnum.ENTITY_STATE);
	}

    /**
     * The JDIS PDU reader class (used for processing any incoming binary DIS
     * data
     */
    private static PDUReader pduReader;
    static {
        try {
            /* The JDIS library PDU Reader class has some coupling between the
             * network/socket logic and the PDU decoding logic. For our message
             * handler, we really just want the PDU decoding logic so we create
             * a default reader here, but do not intend to use the
             * socket/network logic in the class. */
            pduReader = new PDUReader();
        } catch (SocketException socketEx) {
            logger.error("Exception caught while initializing the PDUReader: " + socketEx);
        }
    }

	/**
     * Encodes the given object as a JSON string to pass from GIFT into an
     * embedded application. If a known {@link JSONCodec} exists for the
     * object's type, then that codec will be used to encode the object. If no
     * such codec exists, the object will be converted to JSON via reflection
     * and wrapped in a {@link GenericJSONState} object.
     *
     * @param object the object to encode
     * @return The {@link JSONObject} that is produced as a result of the
     *         encoding.
     * @throws ParseException if a problem occurs while converting an
     *         unsupported object to JSON via reflection
     */
	@SuppressWarnings("unchecked")
    public static JSONObject encodeForEmbeddedApplication(Object object) throws ParseException {

        /* The JSON object to populate */
		JSONObject jsonObject = new JSONObject();

		EncodedMessageType type = null;
		JSONObject payload = new JSONObject();

        if (object instanceof Siman) {
            type = EncodedMessageType.Siman;
        } else if (object instanceof Message) {

            /* Set the type based on the type of the message type */
            Message message = (Message) object;
            if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_EMBEDDED_REQUEST) {
                type = EncodedMessageType.Feedback;
                object = message.getPayload();
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST) {
                type = EncodedMessageType.Feedback;
                object = message.getPayload();
            } else if (message.getMessageType() == MessageTypeEnum.VIBRATE_DEVICE_REQUEST) {
                type = EncodedMessageType.Vibrate;
                object = message.getPayload();
            } else if (message.getMessageType() == MessageTypeEnum.GEOLOCATION) {
                type = EncodedMessageType.Geolocation;
                object = message.getPayload();
            } else {
                throw new IllegalArgumentException("The paramater object was a message of unsupported message type '" + message.getMessageType().getName() + "'");
            }

        } else if (object instanceof StopFreeze) {
            type = EncodedMessageType.GenericJSONState;
        } else if (object instanceof SimpleExampleState) {
            type = EncodedMessageType.SimpleExampleState;
        } else if (object instanceof GenericJSONState) {
            type = EncodedMessageType.GenericJSONState;
        } else {
            throw new IllegalArgumentException("The class of the parameter object was an unsupported type '" + object.getClass().getName() + "'");
        }


        /* Encode the payload if a codec was found, otherwise treat it as a
         * GenericJSONState */
        JSONCodec codec = messageTypeToCodec.get(type);
		if(codec != null){
            Object embeddedObject = giftPayloadToEmbeddedPayload(object);
            codec.encode(payload, embeddedObject);
		} else {

			//no codec mapping exists for the given type so we need to convert it to JSON and wrap it in a generic JSON state
			type = EncodedMessageType.GenericJSONState;

			//use GSON to convert the object to a JSON object via reflection
			Gson gson = new Gson();
			String jsonStr = gson.toJson(object);

			JSONParser parser = new JSONParser();

			JSONObject reflectedJsonObj = (JSONObject) parser.parse(jsonStr);

			//copy that JSON object's attributes into the generic JSON state object
            GenericJSONState state = new GenericJSONState();

			state.getJSONObject().putAll(reflectedJsonObj);

			GENERIC_JSON_STATE_JSON_CODEC.encode(payload, state);
		}

		jsonObject.put(TYPE_ATTR, type.name());
		jsonObject.put(PAYLOAD_ATTR, payload.toJSONString());

        return jsonObject;
	}

    @SuppressWarnings("unchecked")
    public static JSONObject encodeSimanForEmbeddedApplication(Siman siman, JSONObject simanLoadArgs) {
        JSONObject jsonObject = new JSONObject();
        JSONObject payload = new JSONObject();

        SIMAN_JSON_CODEC.encode(payload, giftPayloadToEmbeddedPayload(siman));

        if (SimanTypeEnum.LOAD.equals(siman.getSimanTypeEnum()) && simanLoadArgs != null) {
            payload.put(EmbeddedSimanJSON.LOAD_ARGS, simanLoadArgs);
        }

        jsonObject.put(TYPE_ATTR, EncodedMessageType.Siman.name());
        jsonObject.put(PAYLOAD_ATTR, payload.toJSONString());
        return jsonObject;
	}

	/**
	 * Decodes the given JSON string as an object to pass into GIFT from an embedded application. If a known {@link JSONCodec}
	 * exists for the JSON string's type, then that codec will be used to decode the string. If no such codec exists, the string
	 * will be decoded as a {@link GenericJSONState} object.
	 *
	 * @param jsonObjString the JSON string to decode
	 * @return the decoded object
	 * @throws ParseException if the JSON string could not be parsed
	 */
    public static Object decodeForGift(String jsonObjString) throws ParseException {

		JSONParser parser = new JSONParser();

		JSONObject jsonObject = (JSONObject) parser.parse(jsonObjString);

		String typeName = (String) jsonObject.get(TYPE_ATTR);

		//Checks if the typeName is a response
		if(EncodedMessageType.SimanResponse.name().equals(typeName)) {
            return new ACK();
		}

		EncodedMessageType type = EncodedMessageType.valueOf(typeName);

		String payloadString = (String) jsonObject.get(PAYLOAD_ATTR);
		JSONObject payload = (JSONObject) parser.parse(payloadString);

		JSONCodec codec = messageTypeToCodec.get(type);

		if(codec != null){
            Object embeddedPayload = codec.decode(payload);
            return embeddedPayloadToGiftPayload(embeddedPayload);
		} else {

			//default to a generic JSON message
			return GENERIC_JSON_STATE_JSON_CODEC.decode(payload);
		}
	}

	/**
     * Gets what {@link MessageTypeEnum} the given object decoded from an
     * embedded application corresponds to. This can be used to push an
     * appropriate {@link Message} to listening modules once GIFT receives it.
     *
     * @param giftPayload the payload object that was decoded from an embedded
     *        application
     * @return the message type corresponding to the object. Can't be null.
     * @throws Exception if a corresponding message type could not be found for
     *         the given object
     */
    public static MessageTypeEnum getDecodedMessageType(Object giftPayload) throws Exception {

        if (giftPayload == null) {
            throw new IllegalArgumentException("The payload must not be null");
        }

		Class<?> clazz = giftPayload.getClass();

		if(decodedPayloadClassToMessageType.containsKey(clazz)) {
			return decodedPayloadClassToMessageType.get(clazz);
		} else {
			throw new Exception("No corresponding message type could be found for the payload object with the type of '" + clazz.toString());
		}
	}

    /**
     * Converts a payload of a GIFT message to a payload that an embedded
     * application can understand.
     *
     * @param giftPayload The GIFT payload to convert. Can't be null.
     * @return The payload that can be understood by an embedded application.
     */
    @SuppressWarnings("unchecked")
    public static Object giftPayloadToEmbeddedPayload(Object giftPayload) {
        if (giftPayload == null) {
            throw new IllegalArgumentException("The parameter 'giftPayload' cannot be null.");
        }

        if (giftPayload instanceof Siman) {
            Siman giftSiman = (Siman) giftPayload;
            SimanTypeEnum simanTypeEnum = giftSiman.getSimanTypeEnum();
            EmbeddedSiman embeddedSiman;
            if (simanTypeEnum == SimanTypeEnum.LOAD) {
                embeddedSiman = EmbeddedSiman.CreateLoad(giftSiman.getLoadArgs());
                }else {
                embeddedSiman = EmbeddedSiman.Create(simanTypeEnum);
            }
            embeddedSiman.setFileSize(giftSiman.getFileSize());
            embeddedSiman.setRouteType(giftSiman.getRouteType());
            embeddedSiman.setRuntimeCourseFolderPath(giftSiman.getRuntimeCourseFolderPath());
            return embeddedSiman;
        } else if (giftPayload instanceof StopFreeze) {
            StopFreeze giftStopFreeze = (StopFreeze) giftPayload;
            long realWorldTime = giftStopFreeze.getRealWorldTime();
            Integer reason = giftStopFreeze.getReason();
            Integer frozenBehavior = giftStopFreeze.getFrozenBehavior();
            long requestID = giftStopFreeze.getRequestID();
            return new EmbeddedStopFreeze(realWorldTime, reason, frozenBehavior, requestID);
        } else if (giftPayload instanceof SimpleExampleState) {
            SimpleExampleState giftSimpleExampleState = (SimpleExampleState) giftPayload;
            return new EmbeddedSimpleExampleState(giftSimpleExampleState.getVar());
        } else if (giftPayload instanceof GenericJSONState) {
            GenericJSONState giftGenericJsonState = (GenericJSONState) giftPayload;
            EmbeddedGenericJSONState embeddedGenericJsonState = new EmbeddedGenericJSONState();
            embeddedGenericJsonState.getJSONObject().putAll(giftGenericJsonState.getJSONObject());
            return embeddedGenericJsonState;
        } else if (giftPayload instanceof Geolocation) {
            Geolocation giftGeolocation = (Geolocation) giftPayload;
            GDC coordinates = giftGeolocation.getCoordinates();
            Double accuracy = giftGeolocation.getAccuracy();
            Double altitudeAccuracy = giftGeolocation.getAltitudeAccuracy();
            Double heading = giftGeolocation.getHeading();
            Double speed = giftGeolocation.getSpeed();
            return new EmbeddedGeolocation(coordinates, accuracy, altitudeAccuracy, heading, speed);
        }
        else if (giftPayload instanceof String || giftPayload instanceof ArrayList<?>) {
            return giftPayload;
        } else {
            throw new IllegalArgumentException("The class of the GIFT payload was an unsupported type '" + giftPayload.getClass().getName() + "'");
        }
    }

    /**
     * Converts a payload generated by an embedded application to a GIFT message
     * payload.
     *
     * @param embeddedPayload The payload from the embedded application to
     *        convert. Can't be null.
     * @return The payload that can be placed into a GIFT message.
     */
    @SuppressWarnings("unchecked")
    private static Object embeddedPayloadToGiftPayload(Object embeddedPayload) {
        if (embeddedPayload instanceof EmbeddedSiman) {
            EmbeddedSiman embeddedSiman = (EmbeddedSiman) embeddedPayload;
            SimanTypeEnum simanTypeEnum = embeddedSiman.getSimanTypeEnum();
            Siman giftSiman;
            if (simanTypeEnum == SimanTypeEnum.LOAD) {
                giftSiman = Siman.CreateLoad(embeddedSiman.getLoadArgs());
            } else {
                giftSiman = Siman.Create(simanTypeEnum);
            }
            giftSiman.setFileSize(embeddedSiman.getFileSize());
            giftSiman.setRouteType(embeddedSiman.getRouteType());
            giftSiman.setRuntimeCourseFolderPath(embeddedSiman.getRuntimeCourseFolderPath());
            return giftSiman;
        } else if (embeddedPayload instanceof EmbeddedStopFreeze) {
            EmbeddedStopFreeze embeddedStopFreeze = (EmbeddedStopFreeze) embeddedPayload;
            long realWorldTime = embeddedStopFreeze.getRealWorldTime();
            Integer reason = embeddedStopFreeze.getReason();
            Integer frozenBehavior = embeddedStopFreeze.getFrozenBehavior();
            long requestID = embeddedStopFreeze.getRequestID();
            return new StopFreeze(realWorldTime, reason, frozenBehavior, requestID);
        } else if (embeddedPayload instanceof EmbeddedSimpleExampleState) {
            EmbeddedSimpleExampleState embeddedSimpleExampleState = (EmbeddedSimpleExampleState) embeddedPayload;
            return new SimpleExampleState(embeddedSimpleExampleState.getVar());
        } else if (embeddedPayload instanceof EmbeddedGenericJSONState) {
            EmbeddedGenericJSONState embeddedGenericJsonState = (EmbeddedGenericJSONState) embeddedPayload;
            GenericJSONState giftGenericJsonState = new GenericJSONState();
            giftGenericJsonState.getJSONObject().putAll(embeddedGenericJsonState.getJSONObject());
            return giftGenericJsonState;
        } else if (embeddedPayload instanceof EmbeddedBinaryData) {
            EmbeddedBinaryData embeddedBinaryData = (EmbeddedBinaryData) embeddedPayload;
            Object convertedBinaryData = convertBinaryDataToGiftPayload(embeddedBinaryData);
            if (convertedBinaryData == null) {
                throw new UnsupportedOperationException("Unable to convert EmbeddedBinaryData: " + embeddedBinaryData);
            }
            return convertedBinaryData;
        } else if (embeddedPayload instanceof EmbeddedGeolocation) {
            EmbeddedGeolocation embeddedGeolocation = (EmbeddedGeolocation) embeddedPayload;
            GDC coordinates = embeddedGeolocation.getCoordinates();
            Double accuracy = embeddedGeolocation.getAccuracy();
            Double altitudeAccuracy = embeddedGeolocation.getAltitudeAccuracy();
            Double heading = embeddedGeolocation.getHeading();
            Double speed = embeddedGeolocation.getSpeed();
            return new Geolocation(coordinates, accuracy, altitudeAccuracy, heading, speed);
        } else {
            throw new IllegalArgumentException("The class of the GIFT payload was an unsupported type '" + embeddedPayload.getClass().getName() + "'");
        }
    }

    /**
     * Converts an {@link EmbeddedBinaryData} payload to an appropriate GIFT
     * message payload.
     *
     * @param binaryData The {@link EmbeddedBinaryData} payload to convert.
     *        Can't be null.
     * @return The payload that can be placed into a GIFT message. Can be null
     *         if the conversion fails.
     */
    private static Object convertBinaryDataToGiftPayload(EmbeddedBinaryData binaryData) {
        if (binaryData == null) {
            throw new IllegalArgumentException("The parameter 'binaryData' cannot be null.");
        }

        try {
            if (binaryData.getDataType() == BinaryDataType.DisPdu) {

                byte[] data = binaryData.getData();

                PDU pdu = pduReader.createPDU(data);
                if (pdu instanceof EntityStatePDU) {

                    /* Instead of sending the DIS binary data into ActiveMQ, we
                     * are converting to an internal GIFT data type
                     * (EntityState). */
                    EntityStatePDU entityStatePDU = (EntityStatePDU) pdu;
                    EntityState entityState = DISToGIFTConverter.createEntityState(entityStatePDU);
                    logger.debug("Received entity state data: " + entityState);

                    return entityState;
                } else {
                    logger.error("Received unexpected DIS message from the embedded application: "
                            + pdu.getPDUType() + ".  The message will not be handled by GIFT.");
                }
            } else {
                logger.error("Received unexpected binary data message of type: "
                        + binaryData.getDataType() + ".  The message will not be handled by GIFT.");
            }
        } catch (Exception e) {
            logger.error("Exception caught converting data to PDU.", e);
        }

        return null;
    }
}