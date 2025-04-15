package mil.arl.gift.common.ta.state;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.ta.state.eventStatus.Event;
public class EventStatus implements TrainingAppState{

    private List<Event> events = new ArrayList<>();

    public void addEvent(String sessionID, String scenarioEvent, String timestamp, String event, String subtype, String subtypeId) {
        events.add(new Event(sessionID, scenarioEvent, timestamp, event, subtype, subtypeId));
    }

    public List<Event> getEvents() {
        return events;
    }
}
