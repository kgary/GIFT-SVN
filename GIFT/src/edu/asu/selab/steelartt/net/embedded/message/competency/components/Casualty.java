package mil.arl.gift.net.embedded.message.competency.components;

public class Casualty {
        private String timestamp;
        private String id;
        private String triageStatus;
        private String stable;
        private String gettingTransported;
        private String gettingTreated;

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTriageStatus() {
            return triageStatus;
        }

        public void setTriageStatus(String triageStatus) {
            this.triageStatus = triageStatus;
        }

        public String getStable() {
            return stable;
        }

        public void setStable(String stable) {
            this.stable = stable;
        }

        public String getGettingTransported() {
            return gettingTransported;
        }

        public void setGettingTransported(String gettingTransported) {
            this.gettingTransported = gettingTransported;
        }

        public String getGettingTreated() {
            return gettingTreated;
        }

        public void setGettingTreated(String gettingTreated) {
            this.gettingTreated = gettingTreated;
        }

        public Casualty(String timestamp,String id,String triageStatus,String stable, String gettingTransported, String gettingTreated){
            this.timestamp=timestamp;
            this.id=id;
            this.triageStatus=triageStatus;
            this.stable=stable;
            this.gettingTransported=gettingTransported;
            this.gettingTreated=gettingTreated;
        }
    }
