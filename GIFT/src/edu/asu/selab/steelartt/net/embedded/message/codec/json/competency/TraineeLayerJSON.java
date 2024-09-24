package mil.arl.gift.net.embedded.message.codec.json.competency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.competency.layers.TraineeLayer;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TraineeLayerJSON {

    public TraineeLayer parse(JSONObject jsonObj) throws ParseException {
        TraineeLayer traineeLayer = new TraineeLayer();
        JSONArray traineesArray = (JSONArray) jsonObj.get("trainees");

        for (Object obj : traineesArray) {
            JSONObject traineeObj = (JSONObject) obj;

            String id = (String) traineeObj.get("id");
            boolean communication = (Boolean) traineeObj.get("Communication");

            JSONObject visualActivityObj = (JSONObject) traineeObj.get("VisualActivity");
            JSONArray ooiWatched = (JSONArray) visualActivityObj.get("OOI_Watched");
            JSONArray roiWatched = (JSONArray) visualActivityObj.get("ROI_Watched");

            JSONObject movementObj = (JSONObject) traineeObj.get("Movement");
            JSONArray headRotation = (JSONArray) movementObj.get("headRotation");
            JSONArray head = (JSONArray) movementObj.get("head");
            JSONArray leftHand = (JSONArray) movementObj.get("leftHand");
            JSONArray rightHand = (JSONArray) movementObj.get("rightHand");
            JSONArray leftFoot = (JSONArray) movementObj.get("leftFoot");
            JSONArray rightFoot = (JSONArray) movementObj.get("rightFoot");

            traineeLayer.addTrainee(id, communication, ooiWatched, roiWatched, headRotation, head, leftHand, rightHand, leftFoot, rightFoot);
        }

        return traineeLayer;
    }

    public void encode(JSONObject jsonObj, TraineeLayer traineeLayer) {
        JSONArray traineesArray = new JSONArray();

        for (TraineeLayer.Trainee trainee : traineeLayer.getTrainees()) {
            JSONObject traineeObj = new JSONObject();
            traineeObj.put("id", trainee.getId());
            traineeObj.put("Communication", trainee.isCommunication());

            JSONObject visualActivityObj = new JSONObject();
            visualActivityObj.put("OOI_Watched", trainee.getOOIWatched());
            visualActivityObj.put("ROI_Watched", trainee.getROIWatched());
            traineeObj.put("VisualActivity", visualActivityObj);

            JSONObject movementObj = new JSONObject();
            movementObj.put("headRotation", trainee.getHeadRotation());
            movementObj.put("head", trainee.getHead());
            movementObj.put("leftHand", trainee.getLeftHand());
            movementObj.put("rightHand", trainee.getRightHand());
            movementObj.put("leftFoot", trainee.getLeftFoot());
            movementObj.put("rightFoot", trainee.getRightFoot());
            traineeObj.put("Movement", movementObj);

            traineesArray.add(traineeObj);
        }

        jsonObj.put("trainees", traineesArray);
    }
}
