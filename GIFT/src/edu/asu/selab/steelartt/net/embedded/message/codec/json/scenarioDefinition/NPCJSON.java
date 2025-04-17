package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Entity;
import mil.arl.gift.net.embedded.message.scenarioDefinition.NPC;

public class NPCJSON extends EntityJSON {
    @Override
    public NPC decode(JSONObject jsonObj) {
        Entity entity = super.decode(jsonObj);
        String role = (String) jsonObj.get("role");
        return new NPC(entity.getId(), entity.getDescription(), role, entity.getLocation(), entity.getAreasOfInterest());
    }

    @Override
    public void encode(JSONObject jsonObj, Entity entity) {
        super.encode(jsonObj, entity);
        jsonObj.put("role", ((NPC) entity).getRole());
    }
}
