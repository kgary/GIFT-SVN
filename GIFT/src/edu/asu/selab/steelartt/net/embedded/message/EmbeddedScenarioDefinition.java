package mil.arl.gift.net.embedded.message;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.net.embedded.message.scenarioDefinition.Scenario;

public class EmbeddedScenarioDefinition {

    private String scenarioEvent;
    private Scenario scenario;

    public EmbeddedScenarioDefinition(String scenarioEvent, Scenario scenario) {
        this.scenarioEvent = scenarioEvent;
        this.scenario = scenario;
    }

    public String getScenarioEvent() {
        return scenarioEvent;
    }

    public Scenario getScenario() {
        return scenario;
    }
}
