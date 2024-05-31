
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * option: describes force command, adjacent units
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "highHQDesignation",
    "highHQMission",
    "highHQIntent",
    "hqDesignation",
    "hqMission",
    "hqIntent",
    "adjacentUnitsMissions"
})
public class FriendlyForces {

    /**
     * this is HQ two echelons up
     * 
     */
    @JsonProperty("highHQDesignation")
    @JsonPropertyDescription("this is HQ two echelons up")
    private String highHQDesignation;
    @JsonProperty("highHQMission")
    private String highHQMission;
    @JsonProperty("highHQIntent")
    private String highHQIntent;
    /**
     * this is HQ one echelon up
     * 
     */
    @JsonProperty("hqDesignation")
    @JsonPropertyDescription("this is HQ one echelon up")
    private String hqDesignation;
    @JsonProperty("hqMission")
    private String hqMission;
    @JsonProperty("hqIntent")
    private String hqIntent;
    @JsonProperty("adjacentUnitsMissions")
    private List<Object> adjacentUnitsMissions = new ArrayList<Object>();

    /**
     * this is HQ two echelons up
     * 
     */
    @JsonProperty("highHQDesignation")
    public String getHighHQDesignation() {
        return highHQDesignation;
    }

    /**
     * this is HQ two echelons up
     * 
     */
    @JsonProperty("highHQDesignation")
    public void setHighHQDesignation(String highHQDesignation) {
        this.highHQDesignation = highHQDesignation;
    }

    @JsonProperty("highHQMission")
    public String getHighHQMission() {
        return highHQMission;
    }

    @JsonProperty("highHQMission")
    public void setHighHQMission(String highHQMission) {
        this.highHQMission = highHQMission;
    }

    @JsonProperty("highHQIntent")
    public String getHighHQIntent() {
        return highHQIntent;
    }

    @JsonProperty("highHQIntent")
    public void setHighHQIntent(String highHQIntent) {
        this.highHQIntent = highHQIntent;
    }

    /**
     * this is HQ one echelon up
     * 
     */
    @JsonProperty("hqDesignation")
    public String getHqDesignation() {
        return hqDesignation;
    }

    /**
     * this is HQ one echelon up
     * 
     */
    @JsonProperty("hqDesignation")
    public void setHqDesignation(String hqDesignation) {
        this.hqDesignation = hqDesignation;
    }

    @JsonProperty("hqMission")
    public String getHqMission() {
        return hqMission;
    }

    @JsonProperty("hqMission")
    public void setHqMission(String hqMission) {
        this.hqMission = hqMission;
    }

    @JsonProperty("hqIntent")
    public String getHqIntent() {
        return hqIntent;
    }

    @JsonProperty("hqIntent")
    public void setHqIntent(String hqIntent) {
        this.hqIntent = hqIntent;
    }

    @JsonProperty("adjacentUnitsMissions")
    public List<Object> getAdjacentUnitsMissions() {
        return adjacentUnitsMissions;
    }

    @JsonProperty("adjacentUnitsMissions")
    public void setAdjacentUnitsMissions(List<Object> adjacentUnitsMissions) {
        this.adjacentUnitsMissions = adjacentUnitsMissions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(FriendlyForces.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("highHQDesignation");
        sb.append('=');
        sb.append(((this.highHQDesignation == null)?"<null>":this.highHQDesignation));
        sb.append(',');
        sb.append("highHQMission");
        sb.append('=');
        sb.append(((this.highHQMission == null)?"<null>":this.highHQMission));
        sb.append(',');
        sb.append("highHQIntent");
        sb.append('=');
        sb.append(((this.highHQIntent == null)?"<null>":this.highHQIntent));
        sb.append(',');
        sb.append("hqDesignation");
        sb.append('=');
        sb.append(((this.hqDesignation == null)?"<null>":this.hqDesignation));
        sb.append(',');
        sb.append("hqMission");
        sb.append('=');
        sb.append(((this.hqMission == null)?"<null>":this.hqMission));
        sb.append(',');
        sb.append("hqIntent");
        sb.append('=');
        sb.append(((this.hqIntent == null)?"<null>":this.hqIntent));
        sb.append(',');
        sb.append("adjacentUnitsMissions");
        sb.append('=');
        sb.append(((this.adjacentUnitsMissions == null)?"<null>":this.adjacentUnitsMissions));
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
        result = ((result* 31)+((this.highHQDesignation == null)? 0 :this.highHQDesignation.hashCode()));
        result = ((result* 31)+((this.highHQIntent == null)? 0 :this.highHQIntent.hashCode()));
        result = ((result* 31)+((this.hqDesignation == null)? 0 :this.hqDesignation.hashCode()));
        result = ((result* 31)+((this.hqIntent == null)? 0 :this.hqIntent.hashCode()));
        result = ((result* 31)+((this.highHQMission == null)? 0 :this.highHQMission.hashCode()));
        result = ((result* 31)+((this.hqMission == null)? 0 :this.hqMission.hashCode()));
        result = ((result* 31)+((this.adjacentUnitsMissions == null)? 0 :this.adjacentUnitsMissions.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FriendlyForces) == false) {
            return false;
        }
        FriendlyForces rhs = ((FriendlyForces) other);
        return ((((((((this.highHQDesignation == rhs.highHQDesignation)||((this.highHQDesignation!= null)&&this.highHQDesignation.equals(rhs.highHQDesignation)))&&((this.highHQIntent == rhs.highHQIntent)||((this.highHQIntent!= null)&&this.highHQIntent.equals(rhs.highHQIntent))))&&((this.hqDesignation == rhs.hqDesignation)||((this.hqDesignation!= null)&&this.hqDesignation.equals(rhs.hqDesignation))))&&((this.hqIntent == rhs.hqIntent)||((this.hqIntent!= null)&&this.hqIntent.equals(rhs.hqIntent))))&&((this.highHQMission == rhs.highHQMission)||((this.highHQMission!= null)&&this.highHQMission.equals(rhs.highHQMission))))&&((this.hqMission == rhs.hqMission)||((this.hqMission!= null)&&this.hqMission.equals(rhs.hqMission))))&&((this.adjacentUnitsMissions == rhs.adjacentUnitsMissions)||((this.adjacentUnitsMissions!= null)&&this.adjacentUnitsMissions.equals(rhs.adjacentUnitsMissions))));
    }

}
