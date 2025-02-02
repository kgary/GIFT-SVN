package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.EmbeddedEventStatus;
import mil.arl.gift.net.embedded.message.eventStatus.Event;
import mil.arl.gift.net.embedded.message.codec.json.eventStatus.EventJSON;
import java.util.List;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.api.message.MessageDecodeException;

public class EmbeddedEventStatusJSON implements JSONCodec{

    private final EventJSON eventJSON = new EventJSON();

    public EmbeddedEventStatus decode(JSONObject jsonObj) throws MessageDecodeException{
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

    public void encode(JSONObject jsonObj, Object payload) {
        
        EmbeddedEventStatus embeddedEventStatus = (EmbeddedEventStatus) payload;
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
