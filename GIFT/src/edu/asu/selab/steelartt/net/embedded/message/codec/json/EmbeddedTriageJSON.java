package mil.arl.gift.net.embedded.message.codec.json.triage;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.triage.ActionsPerformed;
import mil.arl.gift.net.embedded.message.codec.json.triage.ActionsPerformedJSON;
import mil.arl.gift.net.embedded.message.EmbeddedTriage;

public class EmbeddedTriageJSON {

    private final ActionsPerformedJSON actionsPerformedJSON = new ActionsPerformedJSON();

    public EmbeddedTriage parse(JSONObject jsonObj) {
        ActionsPerformed actionsPerformed = actionsPerformedJSON.parse((JSONObject) jsonObj.get("actionsPerformed"));
        
        return new EmbeddedTriage(
            (String) jsonObj.get("sessionID"),
            (String) jsonObj.get("scenarioEvent"),
            (String) jsonObj.get("timestamp"),
            (String) jsonObj.get("traineeId"),
            (String) jsonObj.get("casualtyId"),
            (String) jsonObj.get("subtypeId"),
            actionsPerformed
        );
    }

    public void encode(JSONObject jsonObj, EmbeddedTriage embeddedTriage) {
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
