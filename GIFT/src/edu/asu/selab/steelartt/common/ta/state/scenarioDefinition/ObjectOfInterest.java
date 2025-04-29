package mil.arl.gift.common.ta.state.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class ObjectOfInterest extends Entity {
    private String type;

    public ObjectOfInterest(String id, String description, String type, List<Double> location, List<AreaOfInterest> areasOfInterest) {
        super(id, description, location, areasOfInterest);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}