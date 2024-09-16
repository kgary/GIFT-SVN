package mil.arl.gift.net.embedded.message.competency.components;
import java.util.*;

public class Gaze {
    private List<String> trainees;
    private String timestamp;

    public List<String> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<String> trainees) {
        this.trainees = trainees;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Gaze(List<String> trainees,String timestamp){
        this.trainees=trainees;
        this.timestamp=timestamp;
    }
}
