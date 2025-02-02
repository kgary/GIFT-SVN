package mil.arl.gift.net.embedded.message;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.net.embedded.message.eventStatus.Event;
public class EmbeddedEventStatus {

    private List<Event> events = new ArrayList<>();

    public void addEvent(String sessionID, String scenarioEvent, String timestamp, String event, String subtype, String subtypeId) {
        events.add(new Event(sessionID, scenarioEvent, timestamp, event, subtype, subtypeId));
    }

    public List<Event> getEvents() {
        return events;
    }
}
