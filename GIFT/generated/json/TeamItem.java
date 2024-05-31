
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "teamSkillId",
    "tgtPerformers"
})
public class TeamItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    private Object teamSkillId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    private List<Performer> tgtPerformers = new ArrayList<Performer>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    public Object getTeamSkillId() {
        return teamSkillId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("teamSkillId")
    public void setTeamSkillId(Object teamSkillId) {
        this.teamSkillId = teamSkillId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    public List<Performer> getTgtPerformers() {
        return tgtPerformers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    public void setTgtPerformers(List<Performer> tgtPerformers) {
        this.tgtPerformers = tgtPerformers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TeamItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("teamSkillId");
        sb.append('=');
        sb.append(((this.teamSkillId == null)?"<null>":this.teamSkillId));
        sb.append(',');
        sb.append("tgtPerformers");
        sb.append('=');
        sb.append(((this.tgtPerformers == null)?"<null>":this.tgtPerformers));
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
        result = ((result* 31)+((this.tgtPerformers == null)? 0 :this.tgtPerformers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TeamItem) == false) {
            return false;
        }
        TeamItem rhs = ((TeamItem) other);
        return (((this.teamSkillId == rhs.teamSkillId)||((this.teamSkillId!= null)&&this.teamSkillId.equals(rhs.teamSkillId)))&&((this.tgtPerformers == rhs.tgtPerformers)||((this.tgtPerformers!= null)&&this.tgtPerformers.equals(rhs.tgtPerformers))));
    }

}
