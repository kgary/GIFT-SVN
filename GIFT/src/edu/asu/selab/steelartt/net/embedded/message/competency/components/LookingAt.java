package mil.arl.gift.net.embedded.message.competency.components;
import java.util.*;

public class LookingAt {

    public static class InnerLookingAt {
        private List<String> objects;
        private String timestamp;

        public List<String> getObjects() {
            return objects;
        }

        public void setObjects(List<String> objects) {
            this.objects = objects;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public InnerLookingAt(List<String> objects, String timestamp){
            this.objects=objects;
            this.timestamp=timestamp;
        }
    }
    
    private String id;
    private InnerLookingAt innerLookingAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InnerLookingAt getInnerLookingAt() {
        return innerLookingAt;
    }

    public void setInnerLookingAt(InnerLookingAt innerLookingAt) {
        this.innerLookingAt = innerLookingAt;
    }

    public LookingAt(String id, InnerLookingAt innerLookingAt){
        this.id=id;
        this.innerLookingAt=innerLookingAt;
    }
}
