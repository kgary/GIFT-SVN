package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.EmbeddedScenarioDefinition;
import mil.arl.gift.net.embedded.message.scenarioDefinition.Scenario;
import mil.arl.gift.net.embedded.message.codec.json.scenarioDefinition.ScenarioJSON;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.api.message.MessageDecodeException;

public class EmbeddedScenarioDefinitionJSON implements JSONCodec{
    private final ScenarioJSON scenarioJSON = new ScenarioJSON();

    @Override
    public EmbeddedScenarioDefinition decode(JSONObject jsonObj) throws MessageDecodeException {
        String scenarioEvent = (String) jsonObj.get("scenarioEvent");
        Scenario scenario = scenarioJSON.decode((JSONObject) jsonObj.get("Scenario"));
        return new EmbeddedScenarioDefinition(scenarioEvent, scenario);
    }

    @Override
    public void encode(JSONObject jsonObj, Object payload) throws MessageDecodeException {
        EmbeddedScenarioDefinition embeddedScenarioDefinition = (EmbeddedScenarioDefinition) payload;
        jsonObj.put("scenarioEvent", embeddedScenarioDefinition.getScenarioEvent());
        
        JSONObject scenarioObj = new JSONObject();
        scenarioJSON.encode(scenarioObj, embeddedScenarioDefinition.getScenario());
        jsonObj.put("Scenario", scenarioObj);
    }
}