package mil.arl.gift.net.embedded.message.timer;

import org.json.simple.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class TraineeLayer {

    public static class Trainee {
        private String id;
        private boolean communication;
        private JSONArray ooiWatched;
        private JSONArray roiWatched;
        private JSONArray headRotation;
        private JSONArray head;
        private JSONArray leftHand;
        private JSONArray rightHand;
        private JSONArray leftFoot;
        private JSONArray rightFoot;

        public Trainee(String id, boolean communication, JSONArray ooiWatched, JSONArray roiWatched, JSONArray headRotation, JSONArray head, JSONArray leftHand, JSONArray rightHand, JSONArray leftFoot, JSONArray rightFoot) {
            this.id = id;
            this.communication = communication;
            this.ooiWatched = ooiWatched;
            this.roiWatched = roiWatched;
            this.headRotation = headRotation;
            this.head = head;
            this.leftHand = leftHand;
            this.rightHand = rightHand;
            this.leftFoot = leftFoot;
            this.rightFoot = rightFoot;
        }

        public String getId() {
            return id;
        }

        public boolean isCommunication() {
            return communication;
        }

        public JSONArray getOOIWatched() {
            return ooiWatched;
        }

        public JSONArray getROIWatched() {
            return roiWatched;
        }

        public JSONArray getHeadRotation() {
            return headRotation;
        }

        public JSONArray getHead() {
            return head;
        }

        public JSONArray getLeftHand() {
            return leftHand;
        }

        public JSONArray getRightHand() {
            return rightHand;
        }

        public JSONArray getLeftFoot() {
            return leftFoot;
        }

        public JSONArray getRightFoot() {
            return rightFoot;
        }
    }

    private List<Trainee> trainees = new ArrayList<>();

    public void addTrainee(String id, boolean communication, JSONArray ooiWatched, JSONArray roiWatched, JSONArray headRotation, JSONArray head, JSONArray leftHand, JSONArray rightHand, JSONArray leftFoot, JSONArray rightFoot) {
        trainees.add(new Trainee(id, communication, ooiWatched, roiWatched, headRotation, head, leftHand, rightHand, leftFoot, rightFoot));
    }

    public List<Trainee> getTrainees() {
        return trainees;
    }
}
