package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.triage.ActionsPerformed;
import mil.arl.gift.net.embedded.message.codec.json.triage.ActionsPerformedJSON;
import mil.arl.gift.net.embedded.message.EmbeddedTriage;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.api.message.MessageDecodeException;

public class EmbeddedTriageJSON implements JSONCodec{

    private final ActionsPerformedJSON actionsPerformedJSON = new ActionsPerformedJSON();

    @Override
    public EmbeddedTriage decode(JSONObject jsonObj) throws MessageDecodeException{
        ActionsPerformed actionsPerformed = actionsPerformedJSON.decode((JSONObject) jsonObj.get("actionsPerformed"));
        
        return new EmbeddedTriage(
            (String) jsonObj.get("sessionID"),
            (String) jsonObj.get("scenarioEvent"),
            (String) jsonObj.get("timestamp"),
            (String) jsonObj.get("trainee_id"),
            (String) jsonObj.get("casualty_id"),
            (String) jsonObj.get("subtype_id"),
            actionsPerformed
        );
    }

    @Override
    public void encode(JSONObject jsonObj, Object payload) {
        
        EmbeddedTriage embeddedTriage = (EmbeddedTriage) payload;

        jsonObj.put("sessionID", embeddedTriage.getSessionID());
        jsonObj.put("scenarioEvent", embeddedTriage.getScenarioEvent());
        jsonObj.put("timestamp", embeddedTriage.getTimestamp());
        jsonObj.put("traineeId", embeddedTriage.getTraineeId());
        jsonObj.put("casualtyId", embeddedTriage.getCasualtyId());
        jsonObj.put("subtypeId", embeddedTriage.getSubtypeId());
        
        JSONObject actionsPerformedJson = new JSONObject();
        actionsPerformedJSON.encode(actionsPerformedJson, embeddedTriage.getActionsPerformed());
        jsonObj.put("actionsPerformed", actionsPerformedJson);
    }
}
