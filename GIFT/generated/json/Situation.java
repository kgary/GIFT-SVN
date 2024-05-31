
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Required: Provides the current situation that supports the 'why' of an assignment
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areaOfInterest",
    "intelligence",
    "friendlyForces",
    "nonDODForcesTasks",
    "attachedExternalUnits",
    "detachedUnits",
    "assumptions"
})
public class Situation {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    private Object areaOfInterest;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("intelligence")
    private Intelligence intelligence;
    /**
     * option: describes force command, adjacent units
     * (Required)
     * 
     */
    @JsonProperty("friendlyForces")
    @JsonPropertyDescription("option: describes force command, adjacent units")
    private FriendlyForces friendlyForces;
    /**
     * option: describes non DOD controlled forces/units in area and tasking or mission
     * 
     */
    @JsonProperty("nonDODForcesTasks")
    @JsonPropertyDescription("option: describes non DOD controlled forces/units in area and tasking or mission")
    private List<Object> nonDODForcesTasks = new ArrayList<Object>();
    /**
     * option: describes available or attached external units
     * 
     */
    @JsonProperty("attachedExternalUnits")
    @JsonPropertyDescription("option: describes available or attached external units")
    private List<String> attachedExternalUnits = new ArrayList<String>();
    /**
     * option: describes units that have been detached from your unit
     * 
     */
    @JsonProperty("detachedUnits")
    @JsonPropertyDescription("option: describes units that have been detached from your unit")
    private List<String> detachedUnits = new ArrayList<String>();
    /**
     * option: describes assumptions not covered
     * 
     */
    @JsonProperty("assumptions")
    @JsonPropertyDescription("option: describes assumptions not covered")
    private String assumptions;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    public Object getAreaOfInterest() {
        return areaOfInterest;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    public void setAreaOfInterest(Object areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("intelligence")
    public Intelligence getIntelligence() {
        return intelligence;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("intelligence")
    public void setIntelligence(Intelligence intelligence) {
        this.intelligence = intelligence;
    }

    /**
     * option: describes force command, adjacent units
     * (Required)
     * 
     */
    @JsonProperty("friendlyForces")
    public FriendlyForces getFriendlyForces() {
        return friendlyForces;
    }

    /**
     * option: describes force command, adjacent units
     * (Required)
     * 
     */
    @JsonProperty("friendlyForces")
    public void setFriendlyForces(FriendlyForces friendlyForces) {
        this.friendlyForces = friendlyForces;
    }

    /**
     * option: describes non DOD controlled forces/units in area and tasking or mission
     * 
     */
    @JsonProperty("nonDODForcesTasks")
    public List<Object> getNonDODForcesTasks() {
        return nonDODForcesTasks;
    }

    /**
     * option: describes non DOD controlled forces/units in area and tasking or mission
     * 
     */
    @JsonProperty("nonDODForcesTasks")
    public void setNonDODForcesTasks(List<Object> nonDODForcesTasks) {
        this.nonDODForcesTasks = nonDODForcesTasks;
    }

    /**
     * option: describes available or attached external units
     * 
     */
    @JsonProperty("attachedExternalUnits")
    public List<String> getAttachedExternalUnits() {
        return attachedExternalUnits;
    }

    /**
     * option: describes available or attached external units
     * 
     */
    @JsonProperty("attachedExternalUnits")
    public void setAttachedExternalUnits(List<String> attachedExternalUnits) {
        this.attachedExternalUnits = attachedExternalUnits;
    }

    /**
     * option: describes units that have been detached from your unit
     * 
     */
    @JsonProperty("detachedUnits")
    public List<String> getDetachedUnits() {
        return detachedUnits;
    }

    /**
     * option: describes units that have been detached from your unit
     * 
     */
    @JsonProperty("detachedUnits")
    public void setDetachedUnits(List<String> detachedUnits) {
        this.detachedUnits = detachedUnits;
    }

    /**
     * option: describes assumptions not covered
     * 
     */
    @JsonProperty("assumptions")
    public String getAssumptions() {
        return assumptions;
    }

    /**
     * option: describes assumptions not covered
     * 
     */
    @JsonProperty("assumptions")
    public void setAssumptions(String assumptions) {
        this.assumptions = assumptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Situation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("areaOfInterest");
        sb.append('=');
        sb.append(((this.areaOfInterest == null)?"<null>":this.areaOfInterest));
        sb.append(',');
        sb.append("intelligence");
        sb.append('=');
        sb.append(((this.intelligence == null)?"<null>":this.intelligence));
        sb.append(',');
        sb.append("friendlyForces");
        sb.append('=');
        sb.append(((this.friendlyForces == null)?"<null>":this.friendlyForces));
        sb.append(',');
        sb.append("nonDODForcesTasks");
        sb.append('=');
        sb.append(((this.nonDODForcesTasks == null)?"<null>":this.nonDODForcesTasks));
        sb.append(',');
        sb.append("attachedExternalUnits");
        sb.append('=');
        sb.append(((this.attachedExternalUnits == null)?"<null>":this.attachedExternalUnits));
        sb.append(',');
        sb.append("detachedUnits");
        sb.append('=');
        sb.append(((this.detachedUnits == null)?"<null>":this.detachedUnits));
        sb.append(',');
        sb.append("assumptions");
        sb.append('=');
        sb.append(((this.assumptions == null)?"<null>":this.assumptions));
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
        result = ((result* 31)+((this.attachedExternalUnits == null)? 0 :this.attachedExternalUnits.hashCode()));
        result = ((result* 31)+((this.nonDODForcesTasks == null)? 0 :this.nonDODForcesTasks.hashCode()));
        result = ((result* 31)+((this.areaOfInterest == null)? 0 :this.areaOfInterest.hashCode()));
        result = ((result* 31)+((this.detachedUnits == null)? 0 :this.detachedUnits.hashCode()));
        result = ((result* 31)+((this.friendlyForces == null)? 0 :this.friendlyForces.hashCode()));
        result = ((result* 31)+((this.assumptions == null)? 0 :this.assumptions.hashCode()));
        result = ((result* 31)+((this.intelligence == null)? 0 :this.intelligence.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Situation) == false) {
            return false;
        }
        Situation rhs = ((Situation) other);
        return ((((((((this.attachedExternalUnits == rhs.attachedExternalUnits)||((this.attachedExternalUnits!= null)&&this.attachedExternalUnits.equals(rhs.attachedExternalUnits)))&&((this.nonDODForcesTasks == rhs.nonDODForcesTasks)||((this.nonDODForcesTasks!= null)&&this.nonDODForcesTasks.equals(rhs.nonDODForcesTasks))))&&((this.areaOfInterest == rhs.areaOfInterest)||((this.areaOfInterest!= null)&&this.areaOfInterest.equals(rhs.areaOfInterest))))&&((this.detachedUnits == rhs.detachedUnits)||((this.detachedUnits!= null)&&this.detachedUnits.equals(rhs.detachedUnits))))&&((this.friendlyForces == rhs.friendlyForces)||((this.friendlyForces!= null)&&this.friendlyForces.equals(rhs.friendlyForces))))&&((this.assumptions == rhs.assumptions)||((this.assumptions!= null)&&this.assumptions.equals(rhs.assumptions))))&&((this.intelligence == rhs.intelligence)||((this.intelligence!= null)&&this.intelligence.equals(rhs.intelligence))));
    }

}
