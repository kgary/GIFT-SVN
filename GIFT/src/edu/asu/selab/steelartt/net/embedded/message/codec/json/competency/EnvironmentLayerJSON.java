package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.competency.layers.EnvironmentLayer;
import mil.arl.gift.net.embedded.message.competency.components.Perturbation;
import mil.arl.gift.net.embedded.message.competency.components.ScenarioTransitions;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentLayerJSON {

    private static final String SCENARIO_TRANSITIONS = "ScenarioTransitions";
    private static final String PERTURBATION = "Perturbation";

    public EnvironmentLayer parse(JSONObject jsonObj) throws MessageDecodeException {
        try {
            JSONObject scenarioTransitionsJson = (JSONObject) jsonObj.get(SCENARIO_TRANSITIONS);
            if (scenarioTransitionsJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "ScenarioTransitions field is missing.");
            }

            ScenarioTransitions scenarioTransitions = parseScenarioTransitions(scenarioTransitionsJson);

            JSONObject perturbationJson = (JSONObject) jsonObj.get(PERTURBATION);
            if (perturbationJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Perturbation field is missing.");
            }

            Perturbation perturbation = parsePerturbation(perturbationJson);

            return new EnvironmentLayer(scenarioTransitions, perturbation);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing EnvironmentLayer.", e);
        }
    }

    private ScenarioTransitions parseScenarioTransitions(JSONObject jsonObj) throws MessageDecodeException {
        try {
            List<String> values = (List<String>) jsonObj.get("values");
            List<String> timestamps = (List<String>) jsonObj.get("timestamps");

            return new ScenarioTransitions(values, timestamps);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing ScenarioTransitions.", e);
        }
    }

    private Perturbation parsePerturbation(JSONObject jsonObj) throws MessageDecodeException {
        try {
            List<String> values = (List<String>) jsonObj.get("values");
            List<String> timestamps = (List<String>) jsonObj.get("timestamps");

            return new Perturbation(values, timestamps);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing Perturbation.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, EnvironmentLayer environmentLayer) {
        JSONObject scenarioTransitionsJson = new JSONObject();
        ScenarioTransitions scenarioTransitions = environmentLayer.getScenarioTransitions();

        scenarioTransitionsJson.put("values", scenarioTransitions.getValues());
        scenarioTransitionsJson.put("timestamps", scenarioTransitions.getTimestamps());

        jsonObj.put(SCENARIO_TRANSITIONS, scenarioTransitionsJson);

        JSONObject perturbationJson = new JSONObject();
        Perturbation perturbation = environmentLayer.getPerturbation();

        perturbationJson.put("values", perturbation.getValues());
        perturbationJson.put("timestamps", perturbation.getTimestamps());

        jsonObj.put(PERTURBATION, perturbationJson);
    }
}
