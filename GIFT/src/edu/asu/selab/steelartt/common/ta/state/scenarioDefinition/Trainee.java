package mil.arl.gift.common.ta.state.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;
public class Trainee extends Entity {
        private String role;

        public Trainee(String id, String description, String role, List<Integer> location, List<AreaOfInterest> areasOfInterest) {
            super(id, description, location, areasOfInterest);
            this.role = role;
        }

        public String getRole() {
            return role;
        }
}