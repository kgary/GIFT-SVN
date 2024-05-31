
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * a skill that is necessary for a person to produce teamwork
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "teamSkillId",
    "teamSkillUuid",
    "teamSkillTitle",
    "compFramework",
    "competency",
    "teamSkillMeasures"
})
public class TeamSkill {

    /**
     * required: a unique local team-skill identifier
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    @JsonPropertyDescription("required: a unique local team-skill identifier")
    private Object teamSkillId;
    /**
     * required: a unique global common team-skill
     * 
     */
    @JsonProperty("teamSkillUuid")
    @JsonPropertyDescription("required: a unique global common team-skill")
    private String teamSkillUuid;
    /**
     * required: the given name of the team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillTitle")
    @JsonPropertyDescription("required: the given name of the team-skill")
    private String teamSkillTitle;
    /**
     * optional: the universal competency framework identifier that this team-skill supports
     * 
     */
    @JsonProperty("compFramework")
    @JsonPropertyDescription("optional: the universal competency framework identifier that this team-skill supports")
    private String compFramework;
    /**
     * optional: the universal competency identifier that provides evidence for this team-skill results in
     * 
     */
    @JsonProperty("competency")
    @JsonPropertyDescription("optional: the universal competency identifier that provides evidence for this team-skill results in")
    private String competency;
    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillMeasures")
    @JsonPropertyDescription("required: measures of this team-skill")
    private List<Measure> teamSkillMeasures = new ArrayList<Measure>();

    /**
     * required: a unique local team-skill identifier
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    public Object getTeamSkillId() {
        return teamSkillId;
    }

    /**
     * required: a unique local team-skill identifier
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    public void setTeamSkillId(Object teamSkillId) {
        this.teamSkillId = teamSkillId;
    }

    /**
     * required: a unique global common team-skill
     * 
     */
    @JsonProperty("teamSkillUuid")
    public String getTeamSkillUuid() {
        return teamSkillUuid;
    }

    /**
     * required: a unique global common team-skill
     * 
     */
    @JsonProperty("teamSkillUuid")
    public void setTeamSkillUuid(String teamSkillUuid) {
        this.teamSkillUuid = teamSkillUuid;
    }

    /**
     * required: the given name of the team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillTitle")
    public String getTeamSkillTitle() {
        return teamSkillTitle;
    }

    /**
     * required: the given name of the team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillTitle")
    public void setTeamSkillTitle(String teamSkillTitle) {
        this.teamSkillTitle = teamSkillTitle;
    }

    /**
     * optional: the universal competency framework identifier that this team-skill supports
     * 
     */
    @JsonProperty("compFramework")
    public String getCompFramework() {
        return compFramework;
    }

    /**
     * optional: the universal competency framework identifier that this team-skill supports
     * 
     */
    @JsonProperty("compFramework")
    public void setCompFramework(String compFramework) {
        this.compFramework = compFramework;
    }

    /**
     * optional: the universal competency identifier that provides evidence for this team-skill results in
     * 
     */
    @JsonProperty("competency")
    public String getCompetency() {
        return competency;
    }

    /**
     * optional: the universal competency identifier that provides evidence for this team-skill results in
     * 
     */
    @JsonProperty("competency")
    public void setCompetency(String competency) {
        this.competency = competency;
    }

    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillMeasures")
    public List<Measure> getTeamSkillMeasures() {
        return teamSkillMeasures;
    }

    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("teamSkillMeasures")
    public void setTeamSkillMeasures(List<Measure> teamSkillMeasures) {
        this.teamSkillMeasures = teamSkillMeasures;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TeamSkill.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("teamSkillId");
        sb.append('=');
        sb.append(((this.teamSkillId == null)?"<null>":this.teamSkillId));
        sb.append(',');
        sb.append("teamSkillUuid");
        sb.append('=');
        sb.append(((this.teamSkillUuid == null)?"<null>":this.teamSkillUuid));
        sb.append(',');
        sb.append("teamSkillTitle");
        sb.append('=');
        sb.append(((this.teamSkillTitle == null)?"<null>":this.teamSkillTitle));
        sb.append(',');
        sb.append("compFramework");
        sb.append('=');
        sb.append(((this.compFramework == null)?"<null>":this.compFramework));
        sb.append(',');
        sb.append("competency");
        sb.append('=');
        sb.append(((this.competency == null)?"<null>":this.competency));
        sb.append(',');
        sb.append("teamSkillMeasures");
        sb.append('=');
        sb.append(((this.teamSkillMeasures == null)?"<null>":this.teamSkillMeasures));
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
        result = ((result* 31)+((this.teamSkillId == null)? 0 :this.teamSkillId.hashCode()));
        result = ((result* 31)+((this.teamSkillMeasures == null)? 0 :this.teamSkillMeasures.hashCode()));
        result = ((result* 31)+((this.competency == null)? 0 :this.competency.hashCode()));
        result = ((result* 31)+((this.teamSkillTitle == null)? 0 :this.teamSkillTitle.hashCode()));
        result = ((result* 31)+((this.teamSkillUuid == null)? 0 :this.teamSkillUuid.hashCode()));
        result = ((result* 31)+((this.compFramework == null)? 0 :this.compFramework.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TeamSkill) == false) {
            return false;
        }
        TeamSkill rhs = ((TeamSkill) other);
        return (((((((this.teamSkillId == rhs.teamSkillId)||((this.teamSkillId!= null)&&this.teamSkillId.equals(rhs.teamSkillId)))&&((this.teamSkillMeasures == rhs.teamSkillMeasures)||((this.teamSkillMeasures!= null)&&this.teamSkillMeasures.equals(rhs.teamSkillMeasures))))&&((this.competency == rhs.competency)||((this.competency!= null)&&this.competency.equals(rhs.competency))))&&((this.teamSkillTitle == rhs.teamSkillTitle)||((this.teamSkillTitle!= null)&&this.teamSkillTitle.equals(rhs.teamSkillTitle))))&&((this.teamSkillUuid == rhs.teamSkillUuid)||((this.teamSkillUuid!= null)&&this.teamSkillUuid.equals(rhs.teamSkillUuid))))&&((this.compFramework == rhs.compFramework)||((this.compFramework!= null)&&this.compFramework.equals(rhs.compFramework))));
    }

}
