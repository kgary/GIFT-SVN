package mil.arl.gift.net.embedded.message;

import mil.arl.gift.net.embedded.message.competency.layers.CasualtyLayer;
import mil.arl.gift.net.embedded.message.competency.layers.TraineeLayer;

public class EmbeddedCompetencyMessage {

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
        EmbeddedCompetencyMessage message = new EmbeddedCompetencyMessage();
    }
}
