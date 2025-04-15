package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.ta.state.timer.CasualtyLayer;
import mil.arl.gift.common.ta.state.timer.TraineeLayer;

public class Timer implements TrainingAppState{

    private CasualtyLayer casualtyLayer;
    private TraineeLayer traineeLayer;

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
