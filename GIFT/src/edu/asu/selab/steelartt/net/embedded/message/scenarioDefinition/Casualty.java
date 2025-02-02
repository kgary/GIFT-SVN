package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public static class Casualty extends Entity {
        public Casualty(String id, String description, List<Integer> location, List<AreaOfInterest> areasOfInterest) {
            super(id, description, location, areasOfInterest);
        }
    }