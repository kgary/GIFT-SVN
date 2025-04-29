package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class NPC extends Entity {
     private String role;

    public NPC(String id, String description, String role, List<Double> location, List<AreaOfInterest> areasOfInterest) {
        super(id, description, location, areasOfInterest);
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}