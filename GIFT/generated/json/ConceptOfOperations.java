
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Required: a statement that directs the method in which subordinate units cooperate and the sequence of actions to achieve the end state.  Usually with a map, overlays or sand table.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "statement",
    "coursesOfAction",
    "schemeOfManeuver",
    "schemeOfIntel",
    "schemeOfFires",
    "schemeOfSecurity",
    "stabilityTasks",
    "assessment",
    "subUnitTasks",
    "coordinatingInsts"
})
public class ConceptOfOperations {

    /**
     * option: single statement describing the CONOPS and how it fits into higher echelon CONOPS
     * 
     */
    @JsonProperty("statement")
    @JsonPropertyDescription("option: single statement describing the CONOPS and how it fits into higher echelon CONOPS")
    private String statement;
    /**
     * Required: Describes the possible courses of action and their decisive point (trigger)
     * (Required)
     * 
     */
    @JsonProperty("coursesOfAction")
    @JsonPropertyDescription("Required: Describes the possible courses of action and their decisive point (trigger)")
    private List<CourseOfAction> coursesOfAction = new ArrayList<CourseOfAction>();
    /**
     * required: describes how each unit will maneuver to accomplish its mission and meets the commander's intent. 
     * (Required)
     * 
     */
    @JsonProperty("schemeOfManeuver")
    @JsonPropertyDescription("required: describes how each unit will maneuver to accomplish its mission and meets the commander's intent. ")
    private List<ManeuverObject> schemeOfManeuver = new ArrayList<ManeuverObject>();
    /**
     * Option: Describes how intelligence will be collected, provided and updated
     * 
     */
    @JsonProperty("schemeOfIntel")
    @JsonPropertyDescription("Option: Describes how intelligence will be collected, provided and updated")
    private String schemeOfIntel;
    /**
     * Required: Describes how fires will be provided including sources, round types, times, prioritized units
     * (Required)
     * 
     */
    @JsonProperty("schemeOfFires")
    @JsonPropertyDescription("Required: Describes how fires will be provided including sources, round types, times, prioritized units")
    private String schemeOfFires;
    /**
     * Option: Describes how security will be set, detainee teams, crowd control, traffic control, female search teams, surrveilance assets
     * 
     */
    @JsonProperty("schemeOfSecurity")
    @JsonPropertyDescription("Option: Describes how security will be set, detainee teams, crowd control, traffic control, female search teams, surrveilance assets")
    private String schemeOfSecurity;
    /**
     * Option: Describes tasks to establish conditions of normality and cooperation in a civil area
     * 
     */
    @JsonProperty("stabilityTasks")
    @JsonPropertyDescription("Option: Describes tasks to establish conditions of normality and cooperation in a civil area")
    private String stabilityTasks;
    /**
     * Option: Describes how to assess progress of operation
     * 
     */
    @JsonProperty("assessment")
    @JsonPropertyDescription("Option: Describes how to assess progress of operation")
    private String assessment;
    /**
     * Required: These should be identicle to the team task(s) being measured
     * (Required)
     * 
     */
    @JsonProperty("subUnitTasks")
    @JsonPropertyDescription("Required: These should be identicle to the team task(s) being measured")
    private List<SubUnitTask> subUnitTasks = new ArrayList<SubUnitTask>();
    /**
     * Required: coordination required as part of CONOPS
     * 
     */
    @JsonProperty("coordinatingInsts")
    @JsonPropertyDescription("Required: coordination required as part of CONOPS")
    private CoordinatingInsts coordinatingInsts;

    /**
     * option: single statement describing the CONOPS and how it fits into higher echelon CONOPS
     * 
     */
    @JsonProperty("statement")
    public String getStatement() {
        return statement;
    }

    /**
     * option: single statement describing the CONOPS and how it fits into higher echelon CONOPS
     * 
     */
    @JsonProperty("statement")
    public void setStatement(String statement) {
        this.statement = statement;
    }

    /**
     * Required: Describes the possible courses of action and their decisive point (trigger)
     * (Required)
     * 
     */
    @JsonProperty("coursesOfAction")
    public List<CourseOfAction> getCoursesOfAction() {
        return coursesOfAction;
    }

    /**
     * Required: Describes the possible courses of action and their decisive point (trigger)
     * (Required)
     * 
     */
    @JsonProperty("coursesOfAction")
    public void setCoursesOfAction(List<CourseOfAction> coursesOfAction) {
        this.coursesOfAction = coursesOfAction;
    }

    /**
     * required: describes how each unit will maneuver to accomplish its mission and meets the commander's intent. 
     * (Required)
     * 
     */
    @JsonProperty("schemeOfManeuver")
    public List<ManeuverObject> getSchemeOfManeuver() {
        return schemeOfManeuver;
    }

    /**
     * required: describes how each unit will maneuver to accomplish its mission and meets the commander's intent. 
     * (Required)
     * 
     */
    @JsonProperty("schemeOfManeuver")
    public void setSchemeOfManeuver(List<ManeuverObject> schemeOfManeuver) {
        this.schemeOfManeuver = schemeOfManeuver;
    }

    /**
     * Option: Describes how intelligence will be collected, provided and updated
     * 
     */
    @JsonProperty("schemeOfIntel")
    public String getSchemeOfIntel() {
        return schemeOfIntel;
    }

    /**
     * Option: Describes how intelligence will be collected, provided and updated
     * 
     */
    @JsonProperty("schemeOfIntel")
    public void setSchemeOfIntel(String schemeOfIntel) {
        this.schemeOfIntel = schemeOfIntel;
    }

    /**
     * Required: Describes how fires will be provided including sources, round types, times, prioritized units
     * (Required)
     * 
     */
    @JsonProperty("schemeOfFires")
    public String getSchemeOfFires() {
        return schemeOfFires;
    }

    /**
     * Required: Describes how fires will be provided including sources, round types, times, prioritized units
     * (Required)
     * 
     */
    @JsonProperty("schemeOfFires")
    public void setSchemeOfFires(String schemeOfFires) {
        this.schemeOfFires = schemeOfFires;
    }

    /**
     * Option: Describes how security will be set, detainee teams, crowd control, traffic control, female search teams, surrveilance assets
     * 
     */
    @JsonProperty("schemeOfSecurity")
    public String getSchemeOfSecurity() {
        return schemeOfSecurity;
    }

    /**
     * Option: Describes how security will be set, detainee teams, crowd control, traffic control, female search teams, surrveilance assets
     * 
     */
    @JsonProperty("schemeOfSecurity")
    public void setSchemeOfSecurity(String schemeOfSecurity) {
        this.schemeOfSecurity = schemeOfSecurity;
    }

    /**
     * Option: Describes tasks to establish conditions of normality and cooperation in a civil area
     * 
     */
    @JsonProperty("stabilityTasks")
    public String getStabilityTasks() {
        return stabilityTasks;
    }

    /**
     * Option: Describes tasks to establish conditions of normality and cooperation in a civil area
     * 
     */
    @JsonProperty("stabilityTasks")
    public void setStabilityTasks(String stabilityTasks) {
        this.stabilityTasks = stabilityTasks;
    }

    /**
     * Option: Describes how to assess progress of operation
     * 
     */
    @JsonProperty("assessment")
    public String getAssessment() {
        return assessment;
    }

    /**
     * Option: Describes how to assess progress of operation
     * 
     */
    @JsonProperty("assessment")
    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    /**
     * Required: These should be identicle to the team task(s) being measured
     * (Required)
     * 
     */
    @JsonProperty("subUnitTasks")
    public List<SubUnitTask> getSubUnitTasks() {
        return subUnitTasks;
    }

    /**
     * Required: These should be identicle to the team task(s) being measured
     * (Required)
     * 
     */
    @JsonProperty("subUnitTasks")
    public void setSubUnitTasks(List<SubUnitTask> subUnitTasks) {
        this.subUnitTasks = subUnitTasks;
    }

    /**
     * Required: coordination required as part of CONOPS
     * 
     */
    @JsonProperty("coordinatingInsts")
    public CoordinatingInsts getCoordinatingInsts() {
        return coordinatingInsts;
    }

    /**
     * Required: coordination required as part of CONOPS
     * 
     */
    @JsonProperty("coordinatingInsts")
    public void setCoordinatingInsts(CoordinatingInsts coordinatingInsts) {
        this.coordinatingInsts = coordinatingInsts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ConceptOfOperations.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("statement");
        sb.append('=');
        sb.append(((this.statement == null)?"<null>":this.statement));
        sb.append(',');
        sb.append("coursesOfAction");
        sb.append('=');
        sb.append(((this.coursesOfAction == null)?"<null>":this.coursesOfAction));
        sb.append(',');
        sb.append("schemeOfManeuver");
        sb.append('=');
        sb.append(((this.schemeOfManeuver == null)?"<null>":this.schemeOfManeuver));
        sb.append(',');
        sb.append("schemeOfIntel");
        sb.append('=');
        sb.append(((this.schemeOfIntel == null)?"<null>":this.schemeOfIntel));
        sb.append(',');
        sb.append("schemeOfFires");
        sb.append('=');
        sb.append(((this.schemeOfFires == null)?"<null>":this.schemeOfFires));
        sb.append(',');
        sb.append("schemeOfSecurity");
        sb.append('=');
        sb.append(((this.schemeOfSecurity == null)?"<null>":this.schemeOfSecurity));
        sb.append(',');
        sb.append("stabilityTasks");
        sb.append('=');
        sb.append(((this.stabilityTasks == null)?"<null>":this.stabilityTasks));
        sb.append(',');
        sb.append("assessment");
        sb.append('=');
        sb.append(((this.assessment == null)?"<null>":this.assessment));
        sb.append(',');
        sb.append("subUnitTasks");
        sb.append('=');
        sb.append(((this.subUnitTasks == null)?"<null>":this.subUnitTasks));
        sb.append(',');
        sb.append("coordinatingInsts");
        sb.append('=');
        sb.append(((this.coordinatingInsts == null)?"<null>":this.coordinatingInsts));
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
        result = ((result* 31)+((this.schemeOfFires == null)? 0 :this.schemeOfFires.hashCode()));
        result = ((result* 31)+((this.assessment == null)? 0 :this.assessment.hashCode()));
        result = ((result* 31)+((this.subUnitTasks == null)? 0 :this.subUnitTasks.hashCode()));
        result = ((result* 31)+((this.schemeOfIntel == null)? 0 :this.schemeOfIntel.hashCode()));
        result = ((result* 31)+((this.stabilityTasks == null)? 0 :this.stabilityTasks.hashCode()));
        result = ((result* 31)+((this.coursesOfAction == null)? 0 :this.coursesOfAction.hashCode()));
        result = ((result* 31)+((this.statement == null)? 0 :this.statement.hashCode()));
        result = ((result* 31)+((this.schemeOfSecurity == null)? 0 :this.schemeOfSecurity.hashCode()));
        result = ((result* 31)+((this.coordinatingInsts == null)? 0 :this.coordinatingInsts.hashCode()));
        result = ((result* 31)+((this.schemeOfManeuver == null)? 0 :this.schemeOfManeuver.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConceptOfOperations) == false) {
            return false;
        }
        ConceptOfOperations rhs = ((ConceptOfOperations) other);
        return (((((((((((this.schemeOfFires == rhs.schemeOfFires)||((this.schemeOfFires!= null)&&this.schemeOfFires.equals(rhs.schemeOfFires)))&&((this.assessment == rhs.assessment)||((this.assessment!= null)&&this.assessment.equals(rhs.assessment))))&&((this.subUnitTasks == rhs.subUnitTasks)||((this.subUnitTasks!= null)&&this.subUnitTasks.equals(rhs.subUnitTasks))))&&((this.schemeOfIntel == rhs.schemeOfIntel)||((this.schemeOfIntel!= null)&&this.schemeOfIntel.equals(rhs.schemeOfIntel))))&&((this.stabilityTasks == rhs.stabilityTasks)||((this.stabilityTasks!= null)&&this.stabilityTasks.equals(rhs.stabilityTasks))))&&((this.coursesOfAction == rhs.coursesOfAction)||((this.coursesOfAction!= null)&&this.coursesOfAction.equals(rhs.coursesOfAction))))&&((this.statement == rhs.statement)||((this.statement!= null)&&this.statement.equals(rhs.statement))))&&((this.schemeOfSecurity == rhs.schemeOfSecurity)||((this.schemeOfSecurity!= null)&&this.schemeOfSecurity.equals(rhs.schemeOfSecurity))))&&((this.coordinatingInsts == rhs.coordinatingInsts)||((this.coordinatingInsts!= null)&&this.coordinatingInsts.equals(rhs.coordinatingInsts))))&&((this.schemeOfManeuver == rhs.schemeOfManeuver)||((this.schemeOfManeuver!= null)&&this.schemeOfManeuver.equals(rhs.schemeOfManeuver))));
    }

}
