package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Casualty;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Entity;

import java.util.List;

public class CasualtyJSON extends EntityJSON {

    @Override
    public Casualty decode(JSONObject jsonObj) {
        Entity entity = super.decode(jsonObj);
        return new Casualty(entity.getId(), entity.getDescription(), entity.getLocation(), entity.getAreasOfInterest());
    }

    @Override
    public void encode(JSONObject jsonObj, Entity entity) {
        super.encode(jsonObj, entity);
    }
}
