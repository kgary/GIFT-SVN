package mil.arl.gift.net.embedded.message.scenarioDefinition;

import java.util.ArrayList;
import java.util.List;

public class AreaOfInterest {
        private String label;
        private String areaType;
        private String interestType;
        private String description;

        public AreaOfInterest(String label, String areaType, String interestType, String description) {
            this.label = label;
            this.areaType = areaType;
            this.interestType = interestType;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getAreaType() {
            return areaType;
        }

        public String getInterestType() {
            return interestType;
        }

        public String getDescription() {
            return description;
        }
    }
