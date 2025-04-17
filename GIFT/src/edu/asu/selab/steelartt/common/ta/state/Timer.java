package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.ta.state.timer.CasualtyLayer;
import mil.arl.gift.common.ta.state.timer.TraineeLayer;

public class Timer implements TrainingAppState{

    private String sessionID;
    private String scenarioEvent; //Timer
    private String timestamp;
    private CasualtyLayer casualtyLayer;
    private TraineeLayer traineeLayer;

    public Timer(String sessionID, String scenarioEvent, String timestamp, CasualtyLayer casualtyLayer, TraineeLayer traineeLayer){
        this.sessionID = sessionID;
        this.scenarioEvent = scenarioEvent;
        this.timestamp = timestamp;
        this.casualtyLayer = casualtyLayer;
        this.traineeLayer = traineeLayer;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID){
        this.sessionID = sessionID;
    }

    public String getScenarioEvent() {
        return scenarioEvent;
    }

    public void setScenarioEvent(String scenarioEvent){
        this.scenarioEvent = scenarioEvent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public CasualtyLayer getCasualtyLayer() {
        return casualtyLayer;
    }

    public void setCasualtyLayer(CasualtyLayer casualtyLayer) {
        this.casualtyLayer = casualtyLayer;
    }

    public TraineeLayer getTraineeLayer() {
        return traineeLayer;
    }

    public void setTraineeLayer(TraineeLayer traineeLayer) {
        this.traineeLayer = traineeLayer;
    }

}
