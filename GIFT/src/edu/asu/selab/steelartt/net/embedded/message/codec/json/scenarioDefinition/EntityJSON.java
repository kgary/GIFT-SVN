package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.AreaOfInterest;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Entity;
import java.util.ArrayList;
import java.util.List;

public class EntityJSON {
    private final AreaOfInterestJSON areaOfInterestJSON = new AreaOfInterestJSON();

    public Entity decode(JSONObject jsonObj) {
        String id = (String) jsonObj.get("id");
        String description = (String) jsonObj.get("description");

        JSONArray locationArray = (JSONArray) jsonObj.get("location");
        List<Double> location = new ArrayList<>();
        for (Object obj : locationArray) {
            location.add(((Double) obj).doubleValue());
        }

        JSONArray areasArray = (JSONArray) jsonObj.get("areasOfInterest");
        List<AreaOfInterest> areasOfInterest = new ArrayList<>();
        if (areasArray != null) {
            for (Object obj : areasArray) {
                areasOfInterest.add(areaOfInterestJSON.decode((JSONObject) obj));
            }
        }

        return new Entity(id, description, location, areasOfInterest);
    }

    public void encode(JSONObject jsonObj, Entity entity) {
        jsonObj.put("id", entity.getId());
        jsonObj.put("description", entity.getDescription());

        JSONArray locationArray = new JSONArray();
        for (Double loc : entity.getLocation()) {
            locationArray.add(loc);
        }
        jsonObj.put("location", locationArray);

        JSONArray areasArray = new JSONArray();
        for (AreaOfInterest area : entity.getAreasOfInterest()) {
            JSONObject areaObj = new JSONObject();
            areaOfInterestJSON.encode(areaObj, area);
            areasArray.add(areaObj);
        }
        jsonObj.put("areasOfInterest", areasArray);
    }
}
