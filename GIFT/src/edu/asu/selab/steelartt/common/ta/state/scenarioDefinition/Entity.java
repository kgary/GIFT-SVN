package mil.arl.gift.common.ta.state.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class Entity {
        private String id;
        private String description;
        private List<Double> location;
        private List<AreaOfInterest> areasOfInterest;

        public Entity(String id, String description, List<Double> location, List<AreaOfInterest> areasOfInterest) {
            this.id = id;
            this.description = description;
            this.location = location;
            this.areasOfInterest = areasOfInterest;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public List<Double> getLocation() {
            return location;
        }

        public List<AreaOfInterest> getAreasOfInterest() {
            return areasOfInterest;
        }
    }