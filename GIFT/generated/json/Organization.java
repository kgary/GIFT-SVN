
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
    "orgId",
    "orgName",
    "countryCode",
    "orgType",
    "orgSymbol",
    "units",
    "teams"
})
public class Organization {

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("orgId")
    @JsonPropertyDescription("required: unique ID of a force side in an exercise")
    private Object orgId;
    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("orgName")
    @JsonPropertyDescription("required: the name of the organization")
    private String orgName;
    /**
     * optional: the country the organization represents
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    @JsonPropertyDescription("optional: the country the organization represents")
    private Organization.CountryCode countryCode;
    /**
     * required: the type of organization
     * (Required)
     * 
     */
    @JsonProperty("orgType")
    @JsonPropertyDescription("required: the type of organization")
    private Organization.OrgType orgType;
    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("orgSymbol")
    @JsonPropertyDescription("optional: a glyph-symbol image sused to represent a team, role or object on map")
    private Glyph orgSymbol;
    /**
     * required: units that are part of organization
     * 
     */
    @JsonProperty("units")
    @JsonPropertyDescription("required: units that are part of organization")
    private List<Object> units = new ArrayList<Object>();
    /**
     * optional: team(s) assigned to this side
     * 
     */
    @JsonProperty("teams")
    @JsonPropertyDescription("optional: team(s) assigned to this side")
    private List<Object> teams = new ArrayList<Object>();

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("orgId")
    public Object getOrgId() {
        return orgId;
    }

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("orgId")
    public void setOrgId(Object orgId) {
        this.orgId = orgId;
    }

    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("orgName")
    public String getOrgName() {
        return orgName;
    }

    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("orgName")
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * optional: the country the organization represents
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    public Organization.CountryCode getCountryCode() {
        return countryCode;
    }

    /**
     * optional: the country the organization represents
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    public void setCountryCode(Organization.CountryCode countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * required: the type of organization
     * (Required)
     * 
     */
    @JsonProperty("orgType")
    public Organization.OrgType getOrgType() {
        return orgType;
    }

    /**
     * required: the type of organization
     * (Required)
     * 
     */
    @JsonProperty("orgType")
    public void setOrgType(Organization.OrgType orgType) {
        this.orgType = orgType;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("orgSymbol")
    public Glyph getOrgSymbol() {
        return orgSymbol;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("orgSymbol")
    public void setOrgSymbol(Glyph orgSymbol) {
        this.orgSymbol = orgSymbol;
    }

    /**
     * required: units that are part of organization
     * 
     */
    @JsonProperty("units")
    public List<Object> getUnits() {
        return units;
    }

    /**
     * required: units that are part of organization
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
        sb.append(Organization.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("orgId");
        sb.append('=');
        sb.append(((this.orgId == null)?"<null>":this.orgId));
        sb.append(',');
        sb.append("orgName");
        sb.append('=');
        sb.append(((this.orgName == null)?"<null>":this.orgName));
        sb.append(',');
        sb.append("countryCode");
        sb.append('=');
        sb.append(((this.countryCode == null)?"<null>":this.countryCode));
        sb.append(',');
        sb.append("orgType");
        sb.append('=');
        sb.append(((this.orgType == null)?"<null>":this.orgType));
        sb.append(',');
        sb.append("orgSymbol");
        sb.append('=');
        sb.append(((this.orgSymbol == null)?"<null>":this.orgSymbol));
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
        result = ((result* 31)+((this.orgType == null)? 0 :this.orgType.hashCode()));
        result = ((result* 31)+((this.orgName == null)? 0 :this.orgName.hashCode()));
        result = ((result* 31)+((this.teams == null)? 0 :this.teams.hashCode()));
        result = ((result* 31)+((this.countryCode == null)? 0 :this.countryCode.hashCode()));
        result = ((result* 31)+((this.units == null)? 0 :this.units.hashCode()));
        result = ((result* 31)+((this.orgId == null)? 0 :this.orgId.hashCode()));
        result = ((result* 31)+((this.orgSymbol == null)? 0 :this.orgSymbol.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Organization) == false) {
            return false;
        }
        Organization rhs = ((Organization) other);
        return ((((((((this.orgType == rhs.orgType)||((this.orgType!= null)&&this.orgType.equals(rhs.orgType)))&&((this.orgName == rhs.orgName)||((this.orgName!= null)&&this.orgName.equals(rhs.orgName))))&&((this.teams == rhs.teams)||((this.teams!= null)&&this.teams.equals(rhs.teams))))&&((this.countryCode == rhs.countryCode)||((this.countryCode!= null)&&this.countryCode.equals(rhs.countryCode))))&&((this.units == rhs.units)||((this.units!= null)&&this.units.equals(rhs.units))))&&((this.orgId == rhs.orgId)||((this.orgId!= null)&&this.orgId.equals(rhs.orgId))))&&((this.orgSymbol == rhs.orgSymbol)||((this.orgSymbol!= null)&&this.orgSymbol.equals(rhs.orgSymbol))));
    }


    /**
     * optional: the country the organization represents
     * 
     */
    public enum CountryCode {

        US("US"),
        DA("DA"),
        AT("AT");
        private final String value;
        private final static Map<String, Organization.CountryCode> CONSTANTS = new HashMap<String, Organization.CountryCode>();

        static {
            for (Organization.CountryCode c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CountryCode(String value) {
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
        public static Organization.CountryCode fromValue(String value) {
            Organization.CountryCode constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: the type of organization
     * 
     */
    public enum OrgType {

        AIRFRC("AIRFRC"),
        ARMY("ARMY"),
        GUERLL("GUERLL"),
        NAVY("NAVY"),
        MARINE("MARINE"),
        SPFRC("SPFRC"),
        TERFRC("TERFRC");
        private final String value;
        private final static Map<String, Organization.OrgType> CONSTANTS = new HashMap<String, Organization.OrgType>();

        static {
            for (Organization.OrgType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OrgType(String value) {
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
        public static Organization.OrgType fromValue(String value) {
            Organization.OrgType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
