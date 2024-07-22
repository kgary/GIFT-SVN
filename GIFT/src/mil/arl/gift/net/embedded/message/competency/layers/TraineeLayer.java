package mil.arl.gift.net.embedded.message.competency.layers;
import mil.arl.gift.net.embedded.message.competency.components.Trainee;
import java.util.*;

public class TraineeLayer {
    private List<Trainee> Trainee;

    public List<Trainee> getTrainee() {
        return Trainee;
    }

    public void setTrainee(List<Trainee> Trainee) {
        this.Trainee = Trainee;
    }

    public TraineeLayer(List<Trainee> Trainee){
        this.Trainee = Trainee;
    }
}
