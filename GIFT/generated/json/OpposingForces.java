
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Required: describes enemy type, size, disposition
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "country",
    "command",
    "unitType",
    "size",
    "descriptiveFeatures",
    "background",
    "mission",
    "equipment",
    "vehicles",
    "fireSupport",
    "sensorSupport",
    "competenceMorale",
    "location",
    "weaknessesVulnerables",
    "coursesOfAction",
    "highInterestObject"
})
public class OpposingForces {

    @JsonProperty("country")
    private String country;
    @JsonProperty("command")
    private String command;
    @JsonProperty("unitType")
    private String unitType;
    @JsonProperty("size")
    private String size;
    @JsonProperty("descriptiveFeatures")
    private String descriptiveFeatures;
    @JsonProperty("background")
    private String background;
    @JsonProperty("mission")
    private String mission;
    @JsonProperty("equipment")
    private String equipment;
    @JsonProperty("vehicles")
    private String vehicles;
    @JsonProperty("fireSupport")
    private String fireSupport;
    @JsonProperty("sensorSupport")
    private String sensorSupport;
    @JsonProperty("competenceMorale")
    private String competenceMorale;
    @JsonProperty("location")
    private String location;
    @JsonProperty("weaknessesVulnerables")
    private String weaknessesVulnerables;
    @JsonProperty("coursesOfAction")
    private List<String> coursesOfAction = new ArrayList<String>();
    /**
     * an OPFOR person, object or place to make contact with and related to mission
     * 
     */
    @JsonProperty("highInterestObject")
    @JsonPropertyDescription("an OPFOR person, object or place to make contact with and related to mission")
    private String highInterestObject;

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("command")
    public String getCommand() {
        return command;
    }

    @JsonProperty("command")
    public void setCommand(String command) {
        this.command = command;
    }

    @JsonProperty("unitType")
    public String getUnitType() {
        return unitType;
    }

    @JsonProperty("unitType")
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    @JsonProperty("size")
    public String getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(String size) {
        this.size = size;
    }

    @JsonProperty("descriptiveFeatures")
    public String getDescriptiveFeatures() {
        return descriptiveFeatures;
    }

    @JsonProperty("descriptiveFeatures")
    public void setDescriptiveFeatures(String descriptiveFeatures) {
        this.descriptiveFeatures = descriptiveFeatures;
    }

    @JsonProperty("background")
    public String getBackground() {
        return background;
    }

    @JsonProperty("background")
    public void setBackground(String background) {
        this.background = background;
    }

    @JsonProperty("mission")
    public String getMission() {
        return mission;
    }

    @JsonProperty("mission")
    public void setMission(String mission) {
        this.mission = mission;
    }

    @JsonProperty("equipment")
    public String getEquipment() {
        return equipment;
    }

    @JsonProperty("equipment")
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    @JsonProperty("vehicles")
    public String getVehicles() {
        return vehicles;
    }

    @JsonProperty("vehicles")
    public void setVehicles(String vehicles) {
        this.vehicles = vehicles;
    }

    @JsonProperty("fireSupport")
    public String getFireSupport() {
        return fireSupport;
    }

    @JsonProperty("fireSupport")
    public void setFireSupport(String fireSupport) {
        this.fireSupport = fireSupport;
    }

    @JsonProperty("sensorSupport")
    public String getSensorSupport() {
        return sensorSupport;
    }

    @JsonProperty("sensorSupport")
    public void setSensorSupport(String sensorSupport) {
        this.sensorSupport = sensorSupport;
    }

    @JsonProperty("competenceMorale")
    public String getCompetenceMorale() {
        return competenceMorale;
    }

    @JsonProperty("competenceMorale")
    public void setCompetenceMorale(String competenceMorale) {
        this.competenceMorale = competenceMorale;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("weaknessesVulnerables")
    public String getWeaknessesVulnerables() {
        return weaknessesVulnerables;
    }

    @JsonProperty("weaknessesVulnerables")
    public void setWeaknessesVulnerables(String weaknessesVulnerables) {
        this.weaknessesVulnerables = weaknessesVulnerables;
    }

    @JsonProperty("coursesOfAction")
    public List<String> getCoursesOfAction() {
        return coursesOfAction;
    }

    @JsonProperty("coursesOfAction")
    public void setCoursesOfAction(List<String> coursesOfAction) {
        this.coursesOfAction = coursesOfAction;
    }

    /**
     * an OPFOR person, object or place to make contact with and related to mission
     * 
     */
    @JsonProperty("highInterestObject")
    public String getHighInterestObject() {
        return highInterestObject;
    }

    /**
     * an OPFOR person, object or place to make contact with and related to mission
     * 
     */
    @JsonProperty("highInterestObject")
    public void setHighInterestObject(String highInterestObject) {
        this.highInterestObject = highInterestObject;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(OpposingForces.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("country");
        sb.append('=');
        sb.append(((this.country == null)?"<null>":this.country));
        sb.append(',');
        sb.append("command");
        sb.append('=');
        sb.append(((this.command == null)?"<null>":this.command));
        sb.append(',');
        sb.append("unitType");
        sb.append('=');
        sb.append(((this.unitType == null)?"<null>":this.unitType));
        sb.append(',');
        sb.append("size");
        sb.append('=');
        sb.append(((this.size == null)?"<null>":this.size));
        sb.append(',');
        sb.append("descriptiveFeatures");
        sb.append('=');
        sb.append(((this.descriptiveFeatures == null)?"<null>":this.descriptiveFeatures));
        sb.append(',');
        sb.append("background");
        sb.append('=');
        sb.append(((this.background == null)?"<null>":this.background));
        sb.append(',');
        sb.append("mission");
        sb.append('=');
        sb.append(((this.mission == null)?"<null>":this.mission));
        sb.append(',');
        sb.append("equipment");
        sb.append('=');
        sb.append(((this.equipment == null)?"<null>":this.equipment));
        sb.append(',');
        sb.append("vehicles");
        sb.append('=');
        sb.append(((this.vehicles == null)?"<null>":this.vehicles));
        sb.append(',');
        sb.append("fireSupport");
        sb.append('=');
        sb.append(((this.fireSupport == null)?"<null>":this.fireSupport));
        sb.append(',');
        sb.append("sensorSupport");
        sb.append('=');
        sb.append(((this.sensorSupport == null)?"<null>":this.sensorSupport));
        sb.append(',');
        sb.append("competenceMorale");
        sb.append('=');
        sb.append(((this.competenceMorale == null)?"<null>":this.competenceMorale));
        sb.append(',');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("weaknessesVulnerables");
        sb.append('=');
        sb.append(((this.weaknessesVulnerables == null)?"<null>":this.weaknessesVulnerables));
        sb.append(',');
        sb.append("coursesOfAction");
        sb.append('=');
        sb.append(((this.coursesOfAction == null)?"<null>":this.coursesOfAction));
        sb.append(',');
        sb.append("highInterestObject");
        sb.append('=');
        sb.append(((this.highInterestObject == null)?"<null>":this.highInterestObject));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.fireSupport == null)? 0 :this.fireSupport.hashCode()));
        result = ((result* 31)+((this.country == null)? 0 :this.country.hashCode()));
        result = ((result* 31)+((this.sensorSupport == null)? 0 :this.sensorSupport.hashCode()));
        result = ((result* 31)+((this.coursesOfAction == null)? 0 :this.coursesOfAction.hashCode()));
        result = ((result* 31)+((this.descriptiveFeatures == null)? 0 :this.descriptiveFeatures.hashCode()));
        result = ((result* 31)+((this.equipment == null)? 0 :this.equipment.hashCode()));
        result = ((result* 31)+((this.vehicles == null)? 0 :this.vehicles.hashCode()));
        result = ((result* 31)+((this.command == null)? 0 :this.command.hashCode()));
        result = ((result* 31)+((this.unitType == null)? 0 :this.unitType.hashCode()));
        result = ((result* 31)+((this.highInterestObject == null)? 0 :this.highInterestObject.hashCode()));
        result = ((result* 31)+((this.weaknessesVulnerables == null)? 0 :this.weaknessesVulnerables.hashCode()));
        result = ((result* 31)+((this.mission == null)? 0 :this.mission.hashCode()));
        result = ((result* 31)+((this.size == null)? 0 :this.size.hashCode()));
        result = ((result* 31)+((this.competenceMorale == null)? 0 :this.competenceMorale.hashCode()));
        result = ((result* 31)+((this.background == null)? 0 :this.background.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OpposingForces) == false) {
            return false;
        }
        OpposingForces rhs = ((OpposingForces) other);
        return (((((((((((((((((this.fireSupport == rhs.fireSupport)||((this.fireSupport!= null)&&this.fireSupport.equals(rhs.fireSupport)))&&((this.country == rhs.country)||((this.country!= null)&&this.country.equals(rhs.country))))&&((this.sensorSupport == rhs.sensorSupport)||((this.sensorSupport!= null)&&this.sensorSupport.equals(rhs.sensorSupport))))&&((this.coursesOfAction == rhs.coursesOfAction)||((this.coursesOfAction!= null)&&this.coursesOfAction.equals(rhs.coursesOfAction))))&&((this.descriptiveFeatures == rhs.descriptiveFeatures)||((this.descriptiveFeatures!= null)&&this.descriptiveFeatures.equals(rhs.descriptiveFeatures))))&&((this.equipment == rhs.equipment)||((this.equipment!= null)&&this.equipment.equals(rhs.equipment))))&&((this.vehicles == rhs.vehicles)||((this.vehicles!= null)&&this.vehicles.equals(rhs.vehicles))))&&((this.command == rhs.command)||((this.command!= null)&&this.command.equals(rhs.command))))&&((this.unitType == rhs.unitType)||((this.unitType!= null)&&this.unitType.equals(rhs.unitType))))&&((this.highInterestObject == rhs.highInterestObject)||((this.highInterestObject!= null)&&this.highInterestObject.equals(rhs.highInterestObject))))&&((this.weaknessesVulnerables == rhs.weaknessesVulnerables)||((this.weaknessesVulnerables!= null)&&this.weaknessesVulnerables.equals(rhs.weaknessesVulnerables))))&&((this.mission == rhs.mission)||((this.mission!= null)&&this.mission.equals(rhs.mission))))&&((this.size == rhs.size)||((this.size!= null)&&this.size.equals(rhs.size))))&&((this.competenceMorale == rhs.competenceMorale)||((this.competenceMorale!= null)&&this.competenceMorale.equals(rhs.competenceMorale))))&&((this.background == rhs.background)||((this.background!= null)&&this.background.equals(rhs.background))))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))));
    }

}
