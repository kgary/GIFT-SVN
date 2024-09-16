package mil.arl.gift.net.embedded.message.competency.components;
import java.util.*;

public class DirectedCommunication {
    private List<Integer> values;
    private List<String> timestamps;

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public List<String> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(List<String> timestamps) {
        this.timestamps = timestamps;
    }
    
    public DirectedCommunication(List<Integer> values, List<String> timestamps){
        this.values=values;
        this.timestamps=timestamps;
    }
}

