package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.scenarioDefinition.AreaOfInterest;

public class AreaOfInterestJSON {

    public AreaOfInterest decode(JSONObject jsonObj) {
        return new AreaOfInterest(
            (String) jsonObj.get("label"),
            (String) jsonObj.get("areaType"),
            (String) jsonObj.get("interestType"),
            (String) jsonObj.get("description")
        );
    }

    public void encode(JSONObject jsonObj, AreaOfInterest areaOfInterest) {
        jsonObj.put("label", areaOfInterest.getLabel());
        jsonObj.put("areaType", areaOfInterest.getAreaType());
        jsonObj.put("interestType", areaOfInterest.getInterestType());
        jsonObj.put("description", areaOfInterest.getDescription());
    }
}
