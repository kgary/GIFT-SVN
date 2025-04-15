package mil.arl.gift.common.ta.state;

import java.util.Map;
import mil.arl.gift.common.ta.state.triage.*;
public class Triage implements TrainingAppState{


    private String sessionID;
    private String scenarioEvent;
    private String timestamp;
    private String traineeId;
    private String casualtyId;
    private String subtypeId;
    private ActionsPerformed actionsPerformed;

    public Triage(String sessionID, String scenarioEvent, String timestamp, String traineeId,
                        String casualtyId, String subtypeId, ActionsPerformed actionsPerformed) {
        this.sessionID = sessionID;
        this.scenarioEvent = scenarioEvent;
        this.timestamp = timestamp;
        this.traineeId = traineeId;
        this.casualtyId = casualtyId;
        this.subtypeId = subtypeId;
        this.actionsPerformed = actionsPerformed;
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

    public String getTraineeId() {
        return traineeId;
    }

    public String getCasualtyId() {
        return casualtyId;
    }

    public String getSubtypeId() {
        return subtypeId;
    }

    public ActionsPerformed getActionsPerformed() {
        return actionsPerformed;
    }
}
