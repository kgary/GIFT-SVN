package mil.arl.gift.net.embedded.message;

import java.util.List;
import java.util.Map;
import mil.arl.gift.net.embedded.message.competency.layers.*;

public class EmbeddedCompetencyMessage {
    private CasualtyLayer CasualtyLayer;
    private VisualActivityLayer VisualActivityLayer;
    private TraineeLayer TraineeLayer;
    private EnvironmentLayer EnvironmentLayer;
    private CommunicationLayer CommunicationLayer;

    public CasualtyLayer getCasualtyLayer() {
        return CasualtyLayer;
    }

    public void setCasualtyLayer(CasualtyLayer CasualtyLayer) {
        this.CasualtyLayer = CasualtyLayer;
    }

    public VisualActivityLayer getVisualActivityLayer() {
        return VisualActivityLayer;
    }

    public void setVisualActivityLayer(VisualActivityLayer VisualActivityLayer) {
        this.VisualActivityLayer = VisualActivityLayer;
    }

    public TraineeLayer getTraineeLayer() {
        return TraineeLayer;
    }

    public void setTraineeLayer(TraineeLayer TraineeLayer) {
        this.TraineeLayer = TraineeLayer;
    }

    public EnvironmentLayer getEnvironmentLayer() {
        return EnvironmentLayer;
    }

    public void setEnvironmentLayer(EnvironmentLayer EnvironmentLayer) {
        this.EnvironmentLayer = EnvironmentLayer;
    }

    public CommunicationLayer getCommunicationLayer() {
        return CommunicationLayer;
    }

    public void setCommunicationLayer(CommunicationLayer CommunicationLayer) {
        this.CommunicationLayer = CommunicationLayer;
    }

    public static void main(String[] args) {
        EmbeddedCompetencyMessage message = new EmbeddedCompetencyMessage();
    }

}
