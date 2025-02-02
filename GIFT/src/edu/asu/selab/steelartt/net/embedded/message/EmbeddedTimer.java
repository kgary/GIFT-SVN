package mil.arl.gift.net.embedded.message;

import mil.arl.gift.net.embedded.message.timer.CasualtyLayer;
import mil.arl.gift.net.embedded.message.timer.TraineeLayer;

public class EmbeddedTimer {

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

    public static void main(String[] args) {
        EmbeddedTimer message = new EmbeddedTimer();
    }
}
