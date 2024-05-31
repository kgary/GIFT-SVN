
package generated.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * optional: this is a force side in a scenario.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sideId",
    "sideName",
    "affiliation",
    "organizations",
    "units",
    "teams"
})
public class Side {

    /**
     * required: exercise unique side id
     * (Required)
     * 
     */
    @JsonProperty("sideId")
    @JsonPropertyDescription("required: exercise unique side id")
    private Object sideId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sideName")
    @JsonPropertyDescription("")
    private String sideName;
    /**
     * required: exercise affiliation relative to trainee (BLUFOR) side
     * 
     */
    @JsonProperty("affiliation")
    @JsonPropertyDescription("required: exercise affiliation relative to trainee (BLUFOR) side")
    private Side.Affiliation affiliation;
    /**
     * required: organizations that is affiliated in the side
     * 
     */
    @JsonProperty("organizations")
    @JsonPropertyDescription("required: organizations that is affiliated in the side")
    private List<Organization> organizations = new ArrayList<Organization>();
    /**
     * required: units that are assigned to this side
     * 
     */
    @JsonProperty("units")
    @JsonPropertyDescription("required: units that are assigned to this side")
    private List<Object> units = new ArrayList<Object>();
    /**
     * optional: team(s) assigned to this side
     * 
     */
    @JsonProperty("teams")
    @JsonPropertyDescription("optional: team(s) assigned to this side")
    private List<Object> teams = new ArrayList<Object>();

    /**
     * required: exercise unique side id
     * (Required)
     * 
     */
    @JsonProperty("sideId")
    public Object getSideId() {
        return sideId;
    }

    /**
     * required: exercise unique side id
     * (Required)
     * 
     */
    @JsonProperty("sideId")
    public void setSideId(Object sideId) {
        this.sideId = sideId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sideName")
    public String getSideName() {
        return sideName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sideName")
    public void setSideName(String sideName) {
        this.sideName = sideName;
    }

    /**
     * required: exercise affiliation relative to trainee (BLUFOR) side
     * 
     */
    @JsonProperty("affiliation")
    public Side.Affiliation getAffiliation() {
        return affiliation;
    }

    /**
     * required: exercise affiliation relative to trainee (BLUFOR) side
     * 
     */
    @JsonProperty("affiliation")
    public void setAffiliation(Side.Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * required: organizations that is affiliated in the side
     * 
     */
    @JsonProperty("organizations")
    public List<Organization> getOrganizations() {
        return organizations;
    }

    /**
     * required: organizations that is affiliated in the side
     * 
     */
    @JsonProperty("organizations")
    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    /**
     * required: units that are assigned to this side
     * 
     */
    @JsonProperty("units")
    public List<Object> getUnits() {
        return units;
    }

    /**
     * required: units that are assigned to this side
     * 
     */
    @JsonProperty("units")
    public void setUnits(List<Object> units) {
        this.units = units;
    }

    /**
     * optional: team(s) assigned to this side
     * 
     */
    @JsonProperty("teams")
    public List<Object> getTeams() {
        return teams;
    }

    /**
     * optional: team(s) assigned to this side
     * 
     */
    @JsonProperty("teams")
    public void setTeams(List<Object> teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Side.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sideId");
        sb.append('=');
        sb.append(((this.sideId == null)?"<null>":this.sideId));
        sb.append(',');
        sb.append("sideName");
        sb.append('=');
        sb.append(((this.sideName == null)?"<null>":this.sideName));
        sb.append(',');
        sb.append("affiliation");
        sb.append('=');
        sb.append(((this.affiliation == null)?"<null>":this.affiliation));
        sb.append(',');
        sb.append("organizations");
        sb.append('=');
        sb.append(((this.organizations == null)?"<null>":this.organizations));
        sb.append(',');
        sb.append("units");
        sb.append('=');
        sb.append(((this.units == null)?"<null>":this.units));
        sb.append(',');
        sb.append("teams");
        sb.append('=');
        sb.append(((this.teams == null)?"<null>":this.teams));
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
        result = ((result* 31)+((this.sideId == null)? 0 :this.sideId.hashCode()));
        result = ((result* 31)+((this.teams == null)? 0 :this.teams.hashCode()));
        result = ((result* 31)+((this.affiliation == null)? 0 :this.affiliation.hashCode()));
        result = ((result* 31)+((this.organizations == null)? 0 :this.organizations.hashCode()));
        result = ((result* 31)+((this.units == null)? 0 :this.units.hashCode()));
        result = ((result* 31)+((this.sideName == null)? 0 :this.sideName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Side) == false) {
            return false;
        }
        Side rhs = ((Side) other);
        return (((((((this.sideId == rhs.sideId)||((this.sideId!= null)&&this.sideId.equals(rhs.sideId)))&&((this.teams == rhs.teams)||((this.teams!= null)&&this.teams.equals(rhs.teams))))&&((this.affiliation == rhs.affiliation)||((this.affiliation!= null)&&this.affiliation.equals(rhs.affiliation))))&&((this.organizations == rhs.organizations)||((this.organizations!= null)&&this.organizations.equals(rhs.organizations))))&&((this.units == rhs.units)||((this.units!= null)&&this.units.equals(rhs.units))))&&((this.sideName == rhs.sideName)||((this.sideName!= null)&&this.sideName.equals(rhs.sideName))));
    }


    /**
     * required: exercise affiliation relative to trainee (BLUFOR) side
     * 
     */
    public enum Affiliation {

        BLUFOR("BLUFOR"),
        OPFOR("OPFOR"),
        NEUFOR("NEUFOR"),
        INDHOSTILE("INDHOSTILE"),
        CIVILIAN("CIVILIAN"),
        ADMIN("ADMIN");
        private final String value;
        private final static Map<String, Side.Affiliation> CONSTANTS = new HashMap<String, Side.Affiliation>();

        static {
            for (Side.Affiliation c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Affiliation(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Side.Affiliation fromValue(String value) {
            Side.Affiliation constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
