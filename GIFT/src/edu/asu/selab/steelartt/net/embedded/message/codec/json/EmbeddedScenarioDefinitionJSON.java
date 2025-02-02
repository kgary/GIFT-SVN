package mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.EmbeddedScenarioDefinition;

public class EmbeddedScenarioDefinitionJSON {
    private final ScenarioJSON scenarioJSON = new ScenarioJSON();

    public EmbeddedScenarioDefinition decode(JSONObject jsonObj) {
        String scenarioEvent = (String) jsonObj.get("scenarioEvent");
        Scenario scenario = scenarioJSON.decode((JSONObject) jsonObj.get("scenario"));
        return new EmbeddedScenarioDefinition(scenarioEvent, scenario);
    }

    public void encode(JSONObject jsonObj, EmbeddedScenarioDefinition embeddedScenarioDefinition) {
        jsonObj.put("scenarioEvent", embeddedScenarioDefinition.getScenarioEvent());
        
        JSONObject scenarioObj = new JSONObject();
        scenarioJSON.encode(scenarioObj, embeddedScenarioDefinition.getScenario());
        jsonObj.put("scenario", scenarioObj);
    }
}