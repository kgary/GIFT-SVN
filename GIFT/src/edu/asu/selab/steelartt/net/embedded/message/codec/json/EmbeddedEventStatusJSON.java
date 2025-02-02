package mil.arl.gift.net.embedded.message.codec.json.event;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.EmbeddedEventStatus;
import mil.arl.gift.net.embedded.message.eventStatus.Event;
import java.util.List;

public class EmbeddedEventStatusJSON {

    private final EventJSON eventJSON = new EventJSON();

    public EmbeddedEventStatus decode(JSONObject jsonObj) {
        EmbeddedEventStatus embeddedEventStatus = new EmbeddedEventStatus();
        JSONArray eventsArray = (JSONArray) jsonObj.get("events");
        for (Object obj : eventsArray) {
            JSONObject eventObj = (JSONObject) obj;
            Event event = eventJSON.decode(eventObj);
            embeddedEventStatus.addEvent(
                event.getSessionID(),
                event.getScenarioEvent(),
                event.getTimestamp(),
                event.getEvent(),
                event.getSubtype(),
                event.getSubtypeId()
            );
        }
        return embeddedEventStatus;
    }

    public void encode(JSONObject jsonObj, EmbeddedEventStatus embeddedEventStatus) {
        JSONArray eventsArray = new JSONArray();
        List<Event> events = embeddedEventStatus.getEvents();
        for (Event event : events) {
            JSONObject eventObj = new JSONObject();
            eventJSON.encode(eventObj, event);
            eventsArray.add(eventObj);
        }
        jsonObj.put("events", eventsArray);
    }
}
