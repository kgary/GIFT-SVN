package mil.arl.gift.common.ta.state;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.ta.state.scenarioDefinition.Scenario;

public class ScenarioDefinition implements TrainingAppState{

    private String scenarioEvent;
    private Scenario scenario;

    public ScenarioDefinition(String scenarioEvent, Scenario scenario) {
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
