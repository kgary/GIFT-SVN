package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Entity;
import mil.arl.gift.net.embedded.message.scenarioDefinition.ObjectOfInterest;

public class ObjectOfInterestJSON extends EntityJSON {
    @Override
    public ObjectOfInterest decode(JSONObject jsonObj) {
        Entity entity = super.decode(jsonObj);
        String type = (String) jsonObj.get("type");
        return new ObjectOfInterest(entity.getId(), entity.getDescription(), type, entity.getLocation(), entity.getAreasOfInterest());
    }

    @Override
    public void encode(JSONObject jsonObj, Entity entity) {
        super.encode(jsonObj, entity);
        jsonObj.put("type", ((ObjectOfInterest) entity).getType());
    }
}