package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class RegionOfInterest extends Entity {
    private String type;

    public RegionOfInterest(String id, String description, String type, List<Double> location, List<AreaOfInterest> areasOfInterest) {
        super(id, description, location, areasOfInterest);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}