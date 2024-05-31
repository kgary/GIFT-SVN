
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Required: coordination required as part of CONOPS
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timeEffective",
    "PriIntelRequirements",
    "essentialFriendlyInformation",
    "fireSupportCoordination",
    "airspaceCoordination",
    "rulesOfEngagement",
    "riskReductionControl",
    "persRecoveryCoordination",
    "environmentalConsiderations",
    "themesMessages",
    "otherCoordinationInsts"
})
public class CoordinatingInsts {

    /**
     * Required: time to execute
     * 
     */
    @JsonProperty("timeEffective")
    @JsonPropertyDescription("Required: time to execute")
    private String timeEffective;
    /**
     * option: list of what priority intelligence is required
     * 
     */
    @JsonProperty("PriIntelRequirements")
    @JsonPropertyDescription("option: list of what priority intelligence is required")
    private List<String> priIntelRequirements = new ArrayList<String>();
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("essentialFriendlyInformation")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String essentialFriendlyInformation;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("fireSupportCoordination")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String fireSupportCoordination;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("airspaceCoordination")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String airspaceCoordination;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("rulesOfEngagement")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String rulesOfEngagement;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("riskReductionControl")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String riskReductionControl;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("persRecoveryCoordination")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String persRecoveryCoordination;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("environmentalConsiderations")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String environmentalConsiderations;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("themesMessages")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String themesMessages;
    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("otherCoordinationInsts")
    @JsonPropertyDescription("Required: Describes the 'who', 'when' and 'where' of operation")
    private String otherCoordinationInsts;

    /**
     * Required: time to execute
     * 
     */
    @JsonProperty("timeEffective")
    public String getTimeEffective() {
        return timeEffective;
    }

    /**
     * Required: time to execute
     * 
     */
    @JsonProperty("timeEffective")
    public void setTimeEffective(String timeEffective) {
        this.timeEffective = timeEffective;
    }

    /**
     * option: list of what priority intelligence is required
     * 
     */
    @JsonProperty("PriIntelRequirements")
    public List<String> getPriIntelRequirements() {
        return priIntelRequirements;
    }

    /**
     * option: list of what priority intelligence is required
     * 
     */
    @JsonProperty("PriIntelRequirements")
    public void setPriIntelRequirements(List<String> priIntelRequirements) {
        this.priIntelRequirements = priIntelRequirements;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("essentialFriendlyInformation")
    public String getEssentialFriendlyInformation() {
        return essentialFriendlyInformation;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("essentialFriendlyInformation")
    public void setEssentialFriendlyInformation(String essentialFriendlyInformation) {
        this.essentialFriendlyInformation = essentialFriendlyInformation;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("fireSupportCoordination")
    public String getFireSupportCoordination() {
        return fireSupportCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("fireSupportCoordination")
    public void setFireSupportCoordination(String fireSupportCoordination) {
        this.fireSupportCoordination = fireSupportCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("airspaceCoordination")
    public String getAirspaceCoordination() {
        return airspaceCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("airspaceCoordination")
    public void setAirspaceCoordination(String airspaceCoordination) {
        this.airspaceCoordination = airspaceCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("rulesOfEngagement")
    public String getRulesOfEngagement() {
        return rulesOfEngagement;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("rulesOfEngagement")
    public void setRulesOfEngagement(String rulesOfEngagement) {
        this.rulesOfEngagement = rulesOfEngagement;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("riskReductionControl")
    public String getRiskReductionControl() {
        return riskReductionControl;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("riskReductionControl")
    public void setRiskReductionControl(String riskReductionControl) {
        this.riskReductionControl = riskReductionControl;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("persRecoveryCoordination")
    public String getPersRecoveryCoordination() {
        return persRecoveryCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("persRecoveryCoordination")
    public void setPersRecoveryCoordination(String persRecoveryCoordination) {
        this.persRecoveryCoordination = persRecoveryCoordination;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("environmentalConsiderations")
    public String getEnvironmentalConsiderations() {
        return environmentalConsiderations;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("environmentalConsiderations")
    public void setEnvironmentalConsiderations(String environmentalConsiderations) {
        this.environmentalConsiderations = environmentalConsiderations;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("themesMessages")
    public String getThemesMessages() {
        return themesMessages;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("themesMessages")
    public void setThemesMessages(String themesMessages) {
        this.themesMessages = themesMessages;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("otherCoordinationInsts")
    public String getOtherCoordinationInsts() {
        return otherCoordinationInsts;
    }

    /**
     * Required: Describes the 'who', 'when' and 'where' of operation
     * 
     */
    @JsonProperty("otherCoordinationInsts")
    public void setOtherCoordinationInsts(String otherCoordinationInsts) {
        this.otherCoordinationInsts = otherCoordinationInsts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CoordinatingInsts.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("timeEffective");
        sb.append('=');
        sb.append(((this.timeEffective == null)?"<null>":this.timeEffective));
        sb.append(',');
        sb.append("priIntelRequirements");
        sb.append('=');
        sb.append(((this.priIntelRequirements == null)?"<null>":this.priIntelRequirements));
        sb.append(',');
        sb.append("essentialFriendlyInformation");
        sb.append('=');
        sb.append(((this.essentialFriendlyInformation == null)?"<null>":this.essentialFriendlyInformation));
        sb.append(',');
        sb.append("fireSupportCoordination");
        sb.append('=');
        sb.append(((this.fireSupportCoordination == null)?"<null>":this.fireSupportCoordination));
        sb.append(',');
        sb.append("airspaceCoordination");
        sb.append('=');
        sb.append(((this.airspaceCoordination == null)?"<null>":this.airspaceCoordination));
        sb.append(',');
        sb.append("rulesOfEngagement");
        sb.append('=');
        sb.append(((this.rulesOfEngagement == null)?"<null>":this.rulesOfEngagement));
        sb.append(',');
        sb.append("riskReductionControl");
        sb.append('=');
        sb.append(((this.riskReductionControl == null)?"<null>":this.riskReductionControl));
        sb.append(',');
        sb.append("persRecoveryCoordination");
        sb.append('=');
        sb.append(((this.persRecoveryCoordination == null)?"<null>":this.persRecoveryCoordination));
        sb.append(',');
        sb.append("environmentalConsiderations");
        sb.append('=');
        sb.append(((this.environmentalConsiderations == null)?"<null>":this.environmentalConsiderations));
        sb.append(',');
        sb.append("themesMessages");
        sb.append('=');
        sb.append(((this.themesMessages == null)?"<null>":this.themesMessages));
        sb.append(',');
        sb.append("otherCoordinationInsts");
        sb.append('=');
        sb.append(((this.otherCoordinationInsts == null)?"<null>":this.otherCoordinationInsts));
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
        result = ((result* 31)+((this.fireSupportCoordination == null)? 0 :this.fireSupportCoordination.hashCode()));
        result = ((result* 31)+((this.rulesOfEngagement == null)? 0 :this.rulesOfEngagement.hashCode()));
        result = ((result* 31)+((this.themesMessages == null)? 0 :this.themesMessages.hashCode()));
        result = ((result* 31)+((this.riskReductionControl == null)? 0 :this.riskReductionControl.hashCode()));
        result = ((result* 31)+((this.airspaceCoordination == null)? 0 :this.airspaceCoordination.hashCode()));
        result = ((result* 31)+((this.persRecoveryCoordination == null)? 0 :this.persRecoveryCoordination.hashCode()));
        result = ((result* 31)+((this.otherCoordinationInsts == null)? 0 :this.otherCoordinationInsts.hashCode()));
        result = ((result* 31)+((this.timeEffective == null)? 0 :this.timeEffective.hashCode()));
        result = ((result* 31)+((this.environmentalConsiderations == null)? 0 :this.environmentalConsiderations.hashCode()));
        result = ((result* 31)+((this.essentialFriendlyInformation == null)? 0 :this.essentialFriendlyInformation.hashCode()));
        result = ((result* 31)+((this.priIntelRequirements == null)? 0 :this.priIntelRequirements.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CoordinatingInsts) == false) {
            return false;
        }
        CoordinatingInsts rhs = ((CoordinatingInsts) other);
        return ((((((((((((this.fireSupportCoordination == rhs.fireSupportCoordination)||((this.fireSupportCoordination!= null)&&this.fireSupportCoordination.equals(rhs.fireSupportCoordination)))&&((this.rulesOfEngagement == rhs.rulesOfEngagement)||((this.rulesOfEngagement!= null)&&this.rulesOfEngagement.equals(rhs.rulesOfEngagement))))&&((this.themesMessages == rhs.themesMessages)||((this.themesMessages!= null)&&this.themesMessages.equals(rhs.themesMessages))))&&((this.riskReductionControl == rhs.riskReductionControl)||((this.riskReductionControl!= null)&&this.riskReductionControl.equals(rhs.riskReductionControl))))&&((this.airspaceCoordination == rhs.airspaceCoordination)||((this.airspaceCoordination!= null)&&this.airspaceCoordination.equals(rhs.airspaceCoordination))))&&((this.persRecoveryCoordination == rhs.persRecoveryCoordination)||((this.persRecoveryCoordination!= null)&&this.persRecoveryCoordination.equals(rhs.persRecoveryCoordination))))&&((this.otherCoordinationInsts == rhs.otherCoordinationInsts)||((this.otherCoordinationInsts!= null)&&this.otherCoordinationInsts.equals(rhs.otherCoordinationInsts))))&&((this.timeEffective == rhs.timeEffective)||((this.timeEffective!= null)&&this.timeEffective.equals(rhs.timeEffective))))&&((this.environmentalConsiderations == rhs.environmentalConsiderations)||((this.environmentalConsiderations!= null)&&this.environmentalConsiderations.equals(rhs.environmentalConsiderations))))&&((this.essentialFriendlyInformation == rhs.essentialFriendlyInformation)||((this.essentialFriendlyInformation!= null)&&this.essentialFriendlyInformation.equals(rhs.essentialFriendlyInformation))))&&((this.priIntelRequirements == rhs.priIntelRequirements)||((this.priIntelRequirements!= null)&&this.priIntelRequirements.equals(rhs.priIntelRequirements))));
    }

}
