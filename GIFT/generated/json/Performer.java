
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "teamId",
    "roleId"
})
public class Performer {

    @JsonProperty("teamId")
    private Object teamId;
    @JsonProperty("roleId")
    private Object roleId;

    @JsonProperty("teamId")
    public Object getTeamId() {
        return teamId;
    }

    @JsonProperty("teamId")
    public void setTeamId(Object teamId) {
        this.teamId = teamId;
    }

    @JsonProperty("roleId")
    public Object getRoleId() {
        return roleId;
    }

    @JsonProperty("roleId")
    public void setRoleId(Object roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Performer.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("teamId");
        sb.append('=');
        sb.append(((this.teamId == null)?"<null>":this.teamId));
        sb.append(',');
        sb.append("roleId");
        sb.append('=');
        sb.append(((this.roleId == null)?"<null>":this.roleId));
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
        result = ((result* 31)+((this.roleId == null)? 0 :this.roleId.hashCode()));
        result = ((result* 31)+((this.teamId == null)? 0 :this.teamId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Performer) == false) {
            return false;
        }
        Performer rhs = ((Performer) other);
        return (((this.roleId == rhs.roleId)||((this.roleId!= null)&&this.roleId.equals(rhs.roleId)))&&((this.teamId == rhs.teamId)||((this.teamId!= null)&&this.teamId.equals(rhs.teamId))));
    }

}
