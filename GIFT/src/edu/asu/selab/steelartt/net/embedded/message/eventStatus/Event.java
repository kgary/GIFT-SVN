package mil.arl.gift.net.embedded.message.eventStatus;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private String sessionID;
    private String scenarioEvent; // Application or Domain
    private String timestamp;
    private String event; // start or stop
    private String subtype; // scene or triage or perturbation
    private String subtypeId;

    public Event(String sessionID, String scenarioEvent, String timestamp, String event, String subtype, String subtypeId) {
        this.sessionID = sessionID;
        this.scenarioEvent = scenarioEvent;
        this.timestamp = timestamp;
        this.event = event;
        this.subtype = subtype;
        this.subtypeId = subtypeId;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getScenarioEvent() {
        return scenarioEvent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getEvent() {
        return event;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getSubtypeId() {
        return subtypeId;
    }
}
