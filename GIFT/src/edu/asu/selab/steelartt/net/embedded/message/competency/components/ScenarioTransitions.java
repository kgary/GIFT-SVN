package mil.arl.gift.net.embedded.message.competency.components;
import java.util.*;

public class ScenarioTransitions {
    private List<String> values;
    private List<String> timestamps;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<String> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(List<String> timestamps) {
        this.timestamps = timestamps;
    }

    public ScenarioTransitions(List<String> values, List<String> timestamps){
        this.values=values;
        this.timestamps=timestamps;
    }

}
