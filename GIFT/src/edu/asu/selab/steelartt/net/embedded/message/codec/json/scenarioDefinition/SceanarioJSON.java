package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.*;
import java.util.ArrayList;
import java.util.List;

public class ScenarioJSON {
    private final CasualtyJSON casualtyJSON = new CasualtyJSON();
    private final TraineeJSON traineeJSON = new TraineeJSON();
    private final ObjectOfInterestJSON objectOfInterestJSON = new ObjectOfInterestJSON();
    private final RegionOfInterestJSON regionOfInterestJSON = new RegionOfInterestJSON();

    public Scenario decode(JSONObject jsonObj) {
        String id = (String) jsonObj.get("id");
        String title = (String) jsonObj.get("title");
        String description = (String) jsonObj.get("description");
        String timestamp = (String) jsonObj.get("timestamp");

        List<Casualty> casualties = new ArrayList<>();
        for (Object obj : (JSONArray) jsonObj.get("casualties")) {
            casualties.add(casualtyJSON.decode((JSONObject) obj));
        }

        List<Trainee> trainees = new ArrayList<>();
        for (Object obj : (JSONArray) jsonObj.get("trainees")) {
            trainees.add(traineeJSON.decode((JSONObject) obj));
        }

        List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();
        for (Object obj : (JSONArray) jsonObj.get("objectsOfInterest")) {
            objectsOfInterest.add(objectOfInterestJSON.decode((JSONObject) obj));
        }

        List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
        for (Object obj : (JSONArray) jsonObj.get("regionsOfInterest")) {
            regionsOfInterest.add(regionOfInterestJSON.decode((JSONObject) obj));
        }

        return new Scenario(id, title, description, timestamp, casualties, trainees, objectsOfInterest, regionsOfInterest);
    }

    public void encode(JSONObject jsonObj, Scenario scenario) {
        jsonObj.put("id", scenario.getId());
        jsonObj.put("title", scenario.getTitle());
        jsonObj.put("description", scenario.getDescription());
        jsonObj.put("timestamp", scenario.getTimestamp());

        JSONArray casualtiesArray = new JSONArray();
        for (Casualty casualty : scenario.getCasualties()) {
            JSONObject obj = new JSONObject();
            casualtyJSON.encode(obj, casualty);
            casualtiesArray.add(obj);
        }
        jsonObj.put("casualties", casualtiesArray);

        JSONArray traineesArray = new JSONArray();
        for (Trainee trainee : scenario.getTrainees()) {
            JSONObject obj = new JSONObject();
            traineeJSON.encode(obj, trainee);
            traineesArray.add(obj);
        }
        jsonObj.put("trainees", traineesArray);

        JSONArray objectsArray = new JSONArray();
        for (ObjectOfInterest object : scenario.getObjectsOfInterest()) {
            JSONObject obj = new JSONObject();
            objectOfInterestJSON.encode(obj, object);
            objectsArray.add(obj);
        }
        jsonObj.put("objectsOfInterest", objectsArray);

        JSONArray regionsArray = new JSONArray();
        for (RegionOfInterest region : scenario.getRegionsOfInterest()) {
            JSONObject obj = new JSONObject();
            regionOfInterestJSON.encode(obj, region);
            regionsArray.add(obj);
        }
        jsonObj.put("regionsOfInterest", regionsArray);
    }
}
