
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Required: describes how a unit will maneuver to accomplish its mission and meets the commander's intent. In offensive operations, it specifies the unit's formation, movement technique, routes or avenues of approach, and plans for direct fire and overwatch. In defensive operations, it specifies the unit's engagement plan, battle positions, orientation of weapons, and the plan for movement between positions. It should also include combat identification measures taken by friendly forces. NOTE: Include force protection steps under scheme of maneuver. In peacetime training, a sixth paragraph outlines overall mission safety measures
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "unitDesignation",
    "assemblyArea",
    "departureLine",
    "objRallyPoints",
    "probLineDeployment",
    "limitOfAdvance",
    "movementOverlay",
    "movementInst",
    "obscuration",
    "safetyPrecautions"
})
public class ManeuverObject {

    @JsonProperty("unitDesignation")
    private String unitDesignation;
    /**
     * required: overlay that describes the assembly area
     * 
     */
    @JsonProperty("assemblyArea")
    @JsonPropertyDescription("required: overlay that describes the assembly area")
    private Integer assemblyArea;
    /**
     * required: overlay that describes the movement of units
     * 
     */
    @JsonProperty("departureLine")
    @JsonPropertyDescription("required: overlay that describes the movement of units")
    private Integer departureLine;
    /**
     * option: overlay areas that describes a point on MGRS map where to stage and reorganize units to and from the objective area
     * 
     */
    @JsonProperty("objRallyPoints")
    @JsonPropertyDescription("option: overlay areas that describes a point on MGRS map where to stage and reorganize units to and from the objective area")
    private List<Integer> objRallyPoints = new ArrayList<Integer>();
    /**
     * option: overlays that describes a phase line designating location where to deploy unit into assault formation
     * 
     */
    @JsonProperty("probLineDeployment")
    @JsonPropertyDescription("option: overlays that describes a phase line designating location where to deploy unit into assault formation")
    private List<Integer> probLineDeployment = new ArrayList<Integer>();
    /**
     * option: during assault, this overlay describes the max limit of advancement
     * 
     */
    @JsonProperty("limitOfAdvance")
    @JsonPropertyDescription("option: during assault, this overlay describes the max limit of advancement")
    private Integer limitOfAdvance;
    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * 
     */
    @JsonProperty("movementOverlay")
    @JsonPropertyDescription("Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units")
    private List<Integer> movementOverlay = new ArrayList<Integer>();
    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * (Required)
     * 
     */
    @JsonProperty("movementInst")
    @JsonPropertyDescription("Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units")
    private List<String> movementInst = new ArrayList<String>();
    /**
     * Option: description of the means to obscure force movement and maneuver
     * 
     */
    @JsonProperty("obscuration")
    @JsonPropertyDescription("Option: description of the means to obscure force movement and maneuver")
    private String obscuration;
    /**
     * Option: description of precautions to ensure safety of units during movement and maneuver
     * 
     */
    @JsonProperty("safetyPrecautions")
    @JsonPropertyDescription("Option: description of precautions to ensure safety of units during movement and maneuver")
    private String safetyPrecautions;

    @JsonProperty("unitDesignation")
    public String getUnitDesignation() {
        return unitDesignation;
    }

    @JsonProperty("unitDesignation")
    public void setUnitDesignation(String unitDesignation) {
        this.unitDesignation = unitDesignation;
    }

    /**
     * required: overlay that describes the assembly area
     * 
     */
    @JsonProperty("assemblyArea")
    public Integer getAssemblyArea() {
        return assemblyArea;
    }

    /**
     * required: overlay that describes the assembly area
     * 
     */
    @JsonProperty("assemblyArea")
    public void setAssemblyArea(Integer assemblyArea) {
        this.assemblyArea = assemblyArea;
    }

    /**
     * required: overlay that describes the movement of units
     * 
     */
    @JsonProperty("departureLine")
    public Integer getDepartureLine() {
        return departureLine;
    }

    /**
     * required: overlay that describes the movement of units
     * 
     */
    @JsonProperty("departureLine")
    public void setDepartureLine(Integer departureLine) {
        this.departureLine = departureLine;
    }

    /**
     * option: overlay areas that describes a point on MGRS map where to stage and reorganize units to and from the objective area
     * 
     */
    @JsonProperty("objRallyPoints")
    public List<Integer> getObjRallyPoints() {
        return objRallyPoints;
    }

    /**
     * option: overlay areas that describes a point on MGRS map where to stage and reorganize units to and from the objective area
     * 
     */
    @JsonProperty("objRallyPoints")
    public void setObjRallyPoints(List<Integer> objRallyPoints) {
        this.objRallyPoints = objRallyPoints;
    }

    /**
     * option: overlays that describes a phase line designating location where to deploy unit into assault formation
     * 
     */
    @JsonProperty("probLineDeployment")
    public List<Integer> getProbLineDeployment() {
        return probLineDeployment;
    }

    /**
     * option: overlays that describes a phase line designating location where to deploy unit into assault formation
     * 
     */
    @JsonProperty("probLineDeployment")
    public void setProbLineDeployment(List<Integer> probLineDeployment) {
        this.probLineDeployment = probLineDeployment;
    }

    /**
     * option: during assault, this overlay describes the max limit of advancement
     * 
     */
    @JsonProperty("limitOfAdvance")
    public Integer getLimitOfAdvance() {
        return limitOfAdvance;
    }

    /**
     * option: during assault, this overlay describes the max limit of advancement
     * 
     */
    @JsonProperty("limitOfAdvance")
    public void setLimitOfAdvance(Integer limitOfAdvance) {
        this.limitOfAdvance = limitOfAdvance;
    }

    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * 
     */
    @JsonProperty("movementOverlay")
    public List<Integer> getMovementOverlay() {
        return movementOverlay;
    }

    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * 
     */
    @JsonProperty("movementOverlay")
    public void setMovementOverlay(List<Integer> movementOverlay) {
        this.movementOverlay = movementOverlay;
    }

    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * (Required)
     * 
     */
    @JsonProperty("movementInst")
    public List<String> getMovementInst() {
        return movementInst;
    }

    /**
     * Required: overlays that describes the main movement path as well as passage points, contact points, checkpoints and CCP of units
     * (Required)
     * 
     */
    @JsonProperty("movementInst")
    public void setMovementInst(List<String> movementInst) {
        this.movementInst = movementInst;
    }

    /**
     * Option: description of the means to obscure force movement and maneuver
     * 
     */
    @JsonProperty("obscuration")
    public String getObscuration() {
        return obscuration;
    }

    /**
     * Option: description of the means to obscure force movement and maneuver
     * 
     */
    @JsonProperty("obscuration")
    public void setObscuration(String obscuration) {
        this.obscuration = obscuration;
    }

    /**
     * Option: description of precautions to ensure safety of units during movement and maneuver
     * 
     */
    @JsonProperty("safetyPrecautions")
    public String getSafetyPrecautions() {
        return safetyPrecautions;
    }

    /**
     * Option: description of precautions to ensure safety of units during movement and maneuver
     * 
     */
    @JsonProperty("safetyPrecautions")
    public void setSafetyPrecautions(String safetyPrecautions) {
        this.safetyPrecautions = safetyPrecautions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ManeuverObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("unitDesignation");
        sb.append('=');
        sb.append(((this.unitDesignation == null)?"<null>":this.unitDesignation));
        sb.append(',');
        sb.append("assemblyArea");
        sb.append('=');
        sb.append(((this.assemblyArea == null)?"<null>":this.assemblyArea));
        sb.append(',');
        sb.append("departureLine");
        sb.append('=');
        sb.append(((this.departureLine == null)?"<null>":this.departureLine));
        sb.append(',');
        sb.append("objRallyPoints");
        sb.append('=');
        sb.append(((this.objRallyPoints == null)?"<null>":this.objRallyPoints));
        sb.append(',');
        sb.append("probLineDeployment");
        sb.append('=');
        sb.append(((this.probLineDeployment == null)?"<null>":this.probLineDeployment));
        sb.append(',');
        sb.append("limitOfAdvance");
        sb.append('=');
        sb.append(((this.limitOfAdvance == null)?"<null>":this.limitOfAdvance));
        sb.append(',');
        sb.append("movementOverlay");
        sb.append('=');
        sb.append(((this.movementOverlay == null)?"<null>":this.movementOverlay));
        sb.append(',');
        sb.append("movementInst");
        sb.append('=');
        sb.append(((this.movementInst == null)?"<null>":this.movementInst));
        sb.append(',');
        sb.append("obscuration");
        sb.append('=');
        sb.append(((this.obscuration == null)?"<null>":this.obscuration));
        sb.append(',');
        sb.append("safetyPrecautions");
        sb.append('=');
        sb.append(((this.safetyPrecautions == null)?"<null>":this.safetyPrecautions));
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
        result = ((result* 31)+((this.objRallyPoints == null)? 0 :this.objRallyPoints.hashCode()));
        result = ((result* 31)+((this.obscuration == null)? 0 :this.obscuration.hashCode()));
        result = ((result* 31)+((this.assemblyArea == null)? 0 :this.assemblyArea.hashCode()));
        result = ((result* 31)+((this.limitOfAdvance == null)? 0 :this.limitOfAdvance.hashCode()));
        result = ((result* 31)+((this.movementInst == null)? 0 :this.movementInst.hashCode()));
        result = ((result* 31)+((this.unitDesignation == null)? 0 :this.unitDesignation.hashCode()));
        result = ((result* 31)+((this.departureLine == null)? 0 :this.departureLine.hashCode()));
        result = ((result* 31)+((this.safetyPrecautions == null)? 0 :this.safetyPrecautions.hashCode()));
        result = ((result* 31)+((this.probLineDeployment == null)? 0 :this.probLineDeployment.hashCode()));
        result = ((result* 31)+((this.movementOverlay == null)? 0 :this.movementOverlay.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ManeuverObject) == false) {
            return false;
        }
        ManeuverObject rhs = ((ManeuverObject) other);
        return (((((((((((this.objRallyPoints == rhs.objRallyPoints)||((this.objRallyPoints!= null)&&this.objRallyPoints.equals(rhs.objRallyPoints)))&&((this.obscuration == rhs.obscuration)||((this.obscuration!= null)&&this.obscuration.equals(rhs.obscuration))))&&((this.assemblyArea == rhs.assemblyArea)||((this.assemblyArea!= null)&&this.assemblyArea.equals(rhs.assemblyArea))))&&((this.limitOfAdvance == rhs.limitOfAdvance)||((this.limitOfAdvance!= null)&&this.limitOfAdvance.equals(rhs.limitOfAdvance))))&&((this.movementInst == rhs.movementInst)||((this.movementInst!= null)&&this.movementInst.equals(rhs.movementInst))))&&((this.unitDesignation == rhs.unitDesignation)||((this.unitDesignation!= null)&&this.unitDesignation.equals(rhs.unitDesignation))))&&((this.departureLine == rhs.departureLine)||((this.departureLine!= null)&&this.departureLine.equals(rhs.departureLine))))&&((this.safetyPrecautions == rhs.safetyPrecautions)||((this.safetyPrecautions!= null)&&this.safetyPrecautions.equals(rhs.safetyPrecautions))))&&((this.probLineDeployment == rhs.probLineDeployment)||((this.probLineDeployment!= null)&&this.probLineDeployment.equals(rhs.probLineDeployment))))&&((this.movementOverlay == rhs.movementOverlay)||((this.movementOverlay!= null)&&this.movementOverlay.equals(rhs.movementOverlay))));
    }

}
