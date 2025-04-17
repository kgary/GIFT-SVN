package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedTimer;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.embedded.message.codec.json.timer.CasualtyLayerJSON;
import mil.arl.gift.net.embedded.message.codec.json.timer.TraineeLayerJSON;
import mil.arl.gift.net.embedded.message.timer.CasualtyLayer;
import mil.arl.gift.net.embedded.message.timer.TraineeLayer;

public class EmbeddedTimerJSON implements JSONCodec {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedTimerJSON.class);

    private static final String SESSION_ID      = "sessionID";
    private static final String SCENARIO_EVENT  = "scenarioEvent";
    private static final String TIMESTAMP       = "timestamp";

    private final CasualtyLayerJSON casualtyLayerJSON = new CasualtyLayerJSON();
    private final TraineeLayerJSON  traineeLayerJSON  = new TraineeLayerJSON();

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {
        try {
            logger.info("Received JSONObject: {}", jsonObj.toJSONString());

            String sessionID     = (String) jsonObj.get(SESSION_ID);
            String scenarioEvent = (String) jsonObj.get(SCENARIO_EVENT);
            String timestamp     = (String) jsonObj.get(TIMESTAMP);

            // these parse() methods should look for "casualties" and "trainees" arrays inside jsonObj
            CasualtyLayer casualtyLayer = casualtyLayerJSON.parse(jsonObj);
            TraineeLayer  traineeLayer  = traineeLayerJSON.parse(jsonObj);

            return new EmbeddedTimer(sessionID, scenarioEvent, timestamp, casualtyLayer, traineeLayer);

        } catch (Exception e) {
            logger.error("Error decoding EmbeddedTimerJSON from {}: ", jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {
        EmbeddedTimer message = (EmbeddedTimer) payload;

        jsonObj.put(SESSION_ID,     message.getSessionID());
        jsonObj.put(SCENARIO_EVENT, message.getScenarioEvent());
        jsonObj.put(TIMESTAMP,      message.getTimestamp());

        // these encode() methods should inject the "casualties" and "trainees" arrays into jsonObj
        casualtyLayerJSON.encode(jsonObj, message.getCasualtyLayer());
        traineeLayerJSON.encode(jsonObj,  message.getTraineeLayer());
    }
}
