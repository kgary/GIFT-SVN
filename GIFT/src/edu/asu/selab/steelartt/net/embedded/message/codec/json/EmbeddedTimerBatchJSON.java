package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedTimer;
import mil.arl.gift.net.embedded.message.EmbeddedTimerBatch;
import mil.arl.gift.net.embedded.message.codec.json.EmbeddedTimerJSON;
import mil.arl.gift.net.json.JSONCodec;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedTimerBatchJSON implements JSONCodec {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedTimerBatchJSON.class);

    private static final String TIMESTAMP = "Timestamp";
    private static final String DATA_SIZE = "DataSize";
    private static final String MESSAGES = "Messages";

    private final EmbeddedTimerJSON messageCodec = new EmbeddedTimerJSON();
    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {
            logger.info("Received JSONObject: ");
            logger.info(jsonObj.toJSONString());
        try {
            

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

            List<EmbeddedTimer> messages = new ArrayList<>();
            JSONParser parser = new JSONParser();  // JSON parser to parse the strings into JSONObjects

            for (Object messageObj : messagesJsonArray) {

                // Parse the string back into a JSONObject
                JSONObject messageJson;
                if(messageObj instanceof JSONObject){
                    messageJson = (JSONObject) messageObj;
                }else{
                    try {
                        messageJson = (JSONObject) parser.parse((String) messageObj);
                    } catch (ParseException e) {
                        throw new MessageDecodeException(this.getClass().getName(), "Error parsing message JSON string.", e);
                    }                
                }
                EmbeddedTimer message = (EmbeddedTimer)messageCodec.decode(messageJson);
                messages.add(message);
            }

            return new EmbeddedTimerBatch(timestamp, dataSize, messages);

        } catch (Exception e) {
            logger.error("Caught exception while creating " + this.getClass().getName() + " from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    // Ideally shouldn't require encode method for the below data as it is a data message to be sent from unity and received by gift.
    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedTimerBatch batch = (EmbeddedTimerBatch) payload;

        jsonObj.put(TIMESTAMP, batch.getTimestamp());
        jsonObj.put(DATA_SIZE, batch.getDataSize());

        JSONArray messagesJsonArray = new JSONArray();
        for (EmbeddedTimer message : batch.getMessages()) {
            JSONObject messageJson = new JSONObject();
            messageCodec.encode(messageJson,message);
            messagesJsonArray.add(messageJson);
        }

        jsonObj.put(MESSAGES, messagesJsonArray);
    }
}