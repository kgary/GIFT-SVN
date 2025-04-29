package mil.arl.gift.net.embedded.message.timer;

import java.util.ArrayList;
import java.util.List;

public class CasualtyLayer {

    public static class Casualty {
        private String id;
        private String status;

        public Casualty(String id, String status) {
            this.id = id;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }
    }

    private List<Casualty> casualties = new ArrayList<>();

    public void addCasualty(String id, String status) {
        casualties.add(new Casualty(id, status));
    }

    public List<Casualty> getCasualties() {
        return casualties;
    }
}
