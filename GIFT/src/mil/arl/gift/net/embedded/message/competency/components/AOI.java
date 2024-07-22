package mil.arl.gift.net.embedded.message.competency.components;
import mil.arl.gift.net.embedded.message.competency.components.Gaze;

public class AOI {
    private String id;
    private Gaze gaze;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Gaze getGaze() {
        return gaze;
    }

    public void setGaze(Gaze gaze) {
        this.gaze = gaze;
    }
    
    public AOI(String id, Gaze gaze){
        this.id=id;
        this.gaze=gaze;
    }
}
