package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Entity;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Trainee;

public class TraineeJSON extends EntityJSON {
    @Override
    public Trainee decode(JSONObject jsonObj) {
        Entity entity = super.decode(jsonObj);
        String role = (String) jsonObj.get("role");
        return new Trainee(entity.getId(), entity.getDescription(), role, entity.getLocation(), entity.getAreasOfInterest());
    }

    @Override
    public void encode(JSONObject jsonObj, Entity entity) {
        super.encode(jsonObj, entity);
        jsonObj.put("role", ((Trainee) entity).getRole());
    }
}
