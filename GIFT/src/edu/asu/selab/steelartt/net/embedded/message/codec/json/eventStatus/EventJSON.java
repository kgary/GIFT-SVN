package mil.arl.gift.net.embedded.message.codec.json.eventStatus;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.eventStatus.Event;

public class EventJSON {

    public Event decode(JSONObject jsonObj) {
        return new Event(
            (String) jsonObj.get("sessionID"),
            (String) jsonObj.get("scenarioEvent"),
            (String) jsonObj.get("timestamp"),
            (String) jsonObj.get("event"),
            (String) jsonObj.get("subtype"),
            (String) jsonObj.get("subtypeId")
        );
    }

    public void encode(JSONObject jsonObj, Event event) {
        jsonObj.put("sessionID", event.getSessionID());
        jsonObj.put("scenarioEvent", event.getScenarioEvent());
        jsonObj.put("timestamp", event.getTimestamp());
        jsonObj.put("event", event.getEvent());
        jsonObj.put("subtype", event.getSubtype());
        jsonObj.put("subtypeId", event.getSubtypeId());
    }
}
