package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedPositionalMessage;
import mil.arl.gift.net.embedded.message.EmbeddedPositionalMessageBatch;
import mil.arl.gift.net.json.JSONCodec;

import mil.arl.gift.net.embedded.message.Position;
import mil.arl.gift.net.embedded.message.Rotation;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedPositionalMessageBatchJSON implements JSONCodec {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedPositionalMessageBatchJSON.class);

    private static final String TIMESTAMP = "Timestamp";
    private static final String DATA_SIZE = "DataSize";
    private static final String MESSAGES = "Messages";
    private static final String POSITION = "position";
    private static final String ROTATION = "rotation";
    private static final String NAME = "name";
    private static final String PARENT_INDEX = "parentIndex";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try {
            logger.info("Received JSONObject: ");
            logger.info(jsonObj.toJSONString());

            String timestamp = (String) jsonObj.get(TIMESTAMP);
            if (timestamp == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Timestamp field is missing.");
            }

            Long dataSizeLong = (Long) jsonObj.get(DATA_SIZE);
            if (dataSizeLong == null) {
                throw new MessageDecodeException(this.getClass().getName(), "DataSize field is missing.");
            }
            int dataSize = dataSizeLong.intValue();

            JSONArray messagesJsonArray = (JSONArray) jsonObj.get(MESSAGES);
            if (messagesJsonArray == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Messages field is missing.");
            }

            List<EmbeddedPositionalMessage> messages = new ArrayList<>();
            JSONParser parser = new JSONParser();  // JSON parser to parse the strings into JSONObjects

            for (Object messageObj : messagesJsonArray) {

                // Parse the string back into a JSONObject
                JSONObject messageJson;
                try {
                    messageJson = (JSONObject) parser.parse((String) messageObj);
                } catch (ParseException e) {
                    throw new MessageDecodeException(this.getClass().getName(), "Error parsing message JSON string.", e);
                }                
                

                JSONObject positionJson = (JSONObject) messageJson.get(POSITION);
                if (positionJson == null) {
                    throw new MessageDecodeException(this.getClass().getName(), "Position field is missing in a message.");
                }
                Position position = new Position(
                    (double) positionJson.get("x"),
                    (double) positionJson.get("y"),
                    (double) positionJson.get("z")
                );

                JSONObject rotationJson = (JSONObject) messageJson.get(ROTATION);
                if (rotationJson == null) {
                    throw new MessageDecodeException(this.getClass().getName(), "Rotation field is missing in a message.");
                }
                Rotation rotation = new Rotation(
                    (double) rotationJson.get("x"),
                    (double) rotationJson.get("y"),
                    (double) rotationJson.get("z"),
                    (double) rotationJson.get("w")
                );

                String name = (String) messageJson.get(NAME);
                if (name == null) {
                    throw new MessageDecodeException(this.getClass().getName(), "Name field is missing in a message.");
                }

                Long parentIndexLong = (Long) messageJson.get(PARENT_INDEX);
                if (parentIndexLong == null) {
                    throw new MessageDecodeException(this.getClass().getName(), "ParentIndex field is missing in a message.");
                }
                int parentIndex = parentIndexLong.intValue();

                EmbeddedPositionalMessage message = new EmbeddedPositionalMessage(position, rotation, name, parentIndex);
                messages.add(message);
            }

            return new EmbeddedPositionalMessageBatch(timestamp, dataSize, messages);

        } catch (Exception e) {
            logger.error("Caught exception while creating " + this.getClass().getName() + " from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    // idealy shouldn't require encode method for the below data as it is a data message to be sent from unity and received by gift.
    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedPositionalMessageBatch batch = (EmbeddedPositionalMessageBatch) payload;

        jsonObj.put(TIMESTAMP, batch.getTimestamp());
        jsonObj.put(DATA_SIZE, batch.getDataSize());

        JSONArray messagesJsonArray = new JSONArray();
        for (EmbeddedPositionalMessage message : batch.getMessages()) {
            JSONObject messageJson = new JSONObject();

            JSONObject positionJson = new JSONObject();
            Position position = message.getPosition();
            positionJson.put("x", position.getX());
            positionJson.put("y", position.getY());
            positionJson.put("z", position.getZ());

            JSONObject rotationJson = new JSONObject();
            Rotation rotation = message.getRotation();
            rotationJson.put("x", rotation.getX());
            rotationJson.put("y", rotation.getY());
            rotationJson.put("z", rotation.getZ());
            rotationJson.put("w", rotation.getW());

            messageJson.put(POSITION, positionJson);
            messageJson.put(ROTATION, rotationJson);
            messageJson.put(NAME, message.getName());
            messageJson.put(PARENT_INDEX, message.getParentIndex());

            messagesJsonArray.add(messageJson);
        }

        jsonObj.put(MESSAGES, messagesJsonArray);
    }
}
