package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;
public class Trainee extends Entity {
        private String role;

        public Trainee(String id, String description, String role, List<Double> location, List<AreaOfInterest> areasOfInterest) {
            super(id, description, location, areasOfInterest);
            this.role = role;
        }

        public String getRole() {
            return role;
        }
}