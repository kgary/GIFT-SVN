package mil.arl.gift.common.ta.state.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class Casualty extends Entity {
        public Casualty(String id, String description, List<Double> location, List<AreaOfInterest> areasOfInterest) {
            super(id, description, location, areasOfInterest);
        }
    }