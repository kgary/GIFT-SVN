package mil.arl.gift.net.embedded.message.codec.json.competency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.competency.layers.TraineeLayer;
import mil.arl.gift.net.embedded.message.competency.components.Trainee;

import java.util.ArrayList;
import java.util.List;

public class TraineeLayerJSON {

    private static final String TRAINEE = "Trainee";

    public TraineeLayer parse(JSONObject jsonObj) throws MessageDecodeException {
        try {
            JSONArray traineesJsonArray = (JSONArray) jsonObj.get(TRAINEE);
            if (traineesJsonArray == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Trainee field is missing.");
            }

            List<Trainee> trainees = new ArrayList<>();
            for (Object traineeObj : traineesJsonArray) {
                JSONObject traineeJson = (JSONObject) traineeObj;

                Trainee trainee = new Trainee(
                    (String) traineeJson.get("id"),
                    (String) traineeJson.get("role"),
                    (String) traineeJson.get("location"),
                    (String) traineeJson.get("heading"),
                    (String) traineeJson.get("timestamp")
                );

                trainees.add(trainee);
            }

            return new TraineeLayer(trainees);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing TraineeLayer.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, TraineeLayer traineeLayer) {
        JSONArray traineesJsonArray = new JSONArray();

        for (Trainee trainee : traineeLayer.getTrainee()) {
            JSONObject traineeJson = new JSONObject();
            traineeJson.put("id", trainee.getId());
            traineeJson.put("role", trainee.getRole());
            traineeJson.put("location", trainee.getLocation());
            traineeJson.put("heading", trainee.getHeading());
            traineeJson.put("timestamp", trainee.getTimestamp());
            traineesJsonArray.add(traineeJson);
        }

        jsonObj.put(TRAINEE, traineesJsonArray);
    }
}
