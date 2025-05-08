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

    public void setSessionID(String sessionID){
        this.sessionID =  sessionID;
    }

    public String getScenarioEvent() {
        return scenarioEvent;
    }

    public void setScenarioEvent(String scenarioEvent){
        this.scenarioEvent =  scenarioEvent;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp){
        this.timestamp =  timestamp;
    }

    public String getTraineeId() {
        return traineeId;
    }
    
    public void setTraineeId(String traineeId) {
        this.traineeId = traineeId;
    }

    public String getCasualtyId() {
        return casualtyId;
    }

    public void setCasualtyId(String casualtyId) {
        this.casualtyId = casualtyId;
    }

    public String getSubtypeId() {
        return subtypeId;
    }

    public void setSubtypeId(String subtypeId) {
        this.subtypeId = subtypeId;
    }

    public ActionsPerformed getActionsPerformed() {
        return actionsPerformed;
    }

    public void setActionsPerformed(ActionsPerformed actionsPerformed) {
        this.actionsPerformed = actionsPerformed;
    }
}
