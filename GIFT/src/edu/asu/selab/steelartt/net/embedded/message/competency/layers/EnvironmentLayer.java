package mil.arl.gift.net.embedded.message.competency.layers;
import mil.arl.gift.net.embedded.message.competency.components.ScenarioTransitions;
import mil.arl.gift.net.embedded.message.competency.components.Perturbation;

public class EnvironmentLayer {
    private ScenarioTransitions ScenarioTransitions;
    private Perturbation Perturbation;

    public ScenarioTransitions getScenarioTransitions() {
        return ScenarioTransitions;
    }

    public void setScenarioTransitions(ScenarioTransitions ScenarioTransitions) {
        this.ScenarioTransitions = ScenarioTransitions;
    }

    public Perturbation getPerturbation() {
        return Perturbation;
    }

    public void setPerturbation(Perturbation Perturbation) {
        this.Perturbation = Perturbation;
    }

    public EnvironmentLayer(ScenarioTransitions ScenarioTransitions,Perturbation Perturbation){
        this.ScenarioTransitions = ScenarioTransitions;
        this.Perturbation = Perturbation;
    }
}
