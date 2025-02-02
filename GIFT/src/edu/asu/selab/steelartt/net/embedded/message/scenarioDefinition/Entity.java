package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public static class Entity {
        private String id;
        private String description;
        private List<Integer> location;
        private List<AreaOfInterest> areasOfInterest;

        public Entity(String id, String description, List<Integer> location, List<AreaOfInterest> areasOfInterest) {
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

        public List<Integer> getLocation() {
            return location;
        }

        public List<AreaOfInterest> getAreasOfInterest() {
            return areasOfInterest;
        }
    }