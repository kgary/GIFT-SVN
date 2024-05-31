
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
 * a position on a team that has duties or responsibilities to make specific decisions, perform specific tasks and/or use specific equipment
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "roleId",
    "roleUuid",
    "roleName",
    "roleAbbrev",
    "roleModel",
    "roleRank",
    "leader",
    "roleSymbol",
    "roleActor",
    "roleEquipment",
    "roleLocation"
})
public class Role {

    /**
     * required: in-exercise unique team-role id - same role can be assigned to different teams giving it a new unique id
     * (Required)
     * 
     */
    @JsonProperty("roleId")
    @JsonPropertyDescription("required: in-exercise unique team-role id - same role can be assigned to different teams giving it a new unique id")
    private Object roleId;
    /**
     * required: the common army role UUIDj - aka positions
     * 
     */
    @JsonProperty("roleUuid")
    @JsonPropertyDescription("required: the common army role UUIDj - aka positions")
    private String roleUuid;
    /**
     * required: the name is based on the role/position defined by the Army or other organization
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    @JsonPropertyDescription("required: the name is based on the role/position defined by the Army or other organization")
    private String roleName = "";
    /**
     * required: abbreviation used on the glyph symbol
     * (Required)
     * 
     */
    @JsonProperty("roleAbbrev")
    @JsonPropertyDescription("required: abbreviation used on the glyph symbol")
    private String roleAbbrev = "";
    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("roleModel")
    @JsonPropertyDescription("describes the on-map 3D model associated with an actor, vehicle, equipment or map object")
    private _3dModel roleModel;
    /**
     * required: rank typically assigned to role - used as default
     * 
     */
    @JsonProperty("roleRank")
    @JsonPropertyDescription("required: rank typically assigned to role - used as default")
    private Role.RoleRank roleRank;
    /**
     * required: defines if role is a leader position in an assigned team
     * (Required)
     * 
     */
    @JsonProperty("leader")
    @JsonPropertyDescription("required: defines if role is a leader position in an assigned team")
    private Boolean leader = false;
    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("roleSymbol")
    @JsonPropertyDescription("optional: a glyph-symbol image sused to represent a team, role or object on map")
    private Glyph roleSymbol;
    /**
     * required: the AI or real actors that will play this role
     * 
     */
    @JsonProperty("roleActor")
    @JsonPropertyDescription("required: the AI or real actors that will play this role")
    private Object roleActor;
    /**
     * required: a list of equipment that this role requires
     * 
     */
    @JsonProperty("roleEquipment")
    @JsonPropertyDescription("required: a list of equipment that this role requires")
    private List<Object> roleEquipment = new ArrayList<Object>();
    /**
     * required: unique position associated with this team-role to identify if an actor is playing the role
     * 
     */
    @JsonProperty("roleLocation")
    @JsonPropertyDescription("required: unique position associated with this team-role to identify if an actor is playing the role")
    private Object roleLocation;

    /**
     * required: in-exercise unique team-role id - same role can be assigned to different teams giving it a new unique id
     * (Required)
     * 
     */
    @JsonProperty("roleId")
    public Object getRoleId() {
        return roleId;
    }

    /**
     * required: in-exercise unique team-role id - same role can be assigned to different teams giving it a new unique id
     * (Required)
     * 
     */
    @JsonProperty("roleId")
    public void setRoleId(Object roleId) {
        this.roleId = roleId;
    }

    /**
     * required: the common army role UUIDj - aka positions
     * 
     */
    @JsonProperty("roleUuid")
    public String getRoleUuid() {
        return roleUuid;
    }

    /**
     * required: the common army role UUIDj - aka positions
     * 
     */
    @JsonProperty("roleUuid")
    public void setRoleUuid(String roleUuid) {
        this.roleUuid = roleUuid;
    }

    /**
     * required: the name is based on the role/position defined by the Army or other organization
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }

    /**
     * required: the name is based on the role/position defined by the Army or other organization
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * required: abbreviation used on the glyph symbol
     * (Required)
     * 
     */
    @JsonProperty("roleAbbrev")
    public String getRoleAbbrev() {
        return roleAbbrev;
    }

    /**
     * required: abbreviation used on the glyph symbol
     * (Required)
     * 
     */
    @JsonProperty("roleAbbrev")
    public void setRoleAbbrev(String roleAbbrev) {
        this.roleAbbrev = roleAbbrev;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("roleModel")
    public _3dModel getRoleModel() {
        return roleModel;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("roleModel")
    public void setRoleModel(_3dModel roleModel) {
        this.roleModel = roleModel;
    }

    /**
     * required: rank typically assigned to role - used as default
     * 
     */
    @JsonProperty("roleRank")
    public Role.RoleRank getRoleRank() {
        return roleRank;
    }

    /**
     * required: rank typically assigned to role - used as default
     * 
     */
    @JsonProperty("roleRank")
    public void setRoleRank(Role.RoleRank roleRank) {
        this.roleRank = roleRank;
    }

    /**
     * required: defines if role is a leader position in an assigned team
     * (Required)
     * 
     */
    @JsonProperty("leader")
    public Boolean getLeader() {
        return leader;
    }

    /**
     * required: defines if role is a leader position in an assigned team
     * (Required)
     * 
     */
    @JsonProperty("leader")
    public void setLeader(Boolean leader) {
        this.leader = leader;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("roleSymbol")
    public Glyph getRoleSymbol() {
        return roleSymbol;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("roleSymbol")
    public void setRoleSymbol(Glyph roleSymbol) {
        this.roleSymbol = roleSymbol;
    }

    /**
     * required: the AI or real actors that will play this role
     * 
     */
    @JsonProperty("roleActor")
    public Object getRoleActor() {
        return roleActor;
    }

    /**
     * required: the AI or real actors that will play this role
     * 
     */
    @JsonProperty("roleActor")
    public void setRoleActor(Object roleActor) {
        this.roleActor = roleActor;
    }

    /**
     * required: a list of equipment that this role requires
     * 
     */
    @JsonProperty("roleEquipment")
    public List<Object> getRoleEquipment() {
        return roleEquipment;
    }

    /**
     * required: a list of equipment that this role requires
     * 
     */
    @JsonProperty("roleEquipment")
    public void setRoleEquipment(List<Object> roleEquipment) {
        this.roleEquipment = roleEquipment;
    }

    /**
     * required: unique position associated with this team-role to identify if an actor is playing the role
     * 
     */
    @JsonProperty("roleLocation")
    public Object getRoleLocation() {
        return roleLocation;
    }

    /**
     * required: unique position associated with this team-role to identify if an actor is playing the role
     * 
     */
    @JsonProperty("roleLocation")
    public void setRoleLocation(Object roleLocation) {
        this.roleLocation = roleLocation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Role.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("roleId");
        sb.append('=');
        sb.append(((this.roleId == null)?"<null>":this.roleId));
        sb.append(',');
        sb.append("roleUuid");
        sb.append('=');
        sb.append(((this.roleUuid == null)?"<null>":this.roleUuid));
        sb.append(',');
        sb.append("roleName");
        sb.append('=');
        sb.append(((this.roleName == null)?"<null>":this.roleName));
        sb.append(',');
        sb.append("roleAbbrev");
        sb.append('=');
        sb.append(((this.roleAbbrev == null)?"<null>":this.roleAbbrev));
        sb.append(',');
        sb.append("roleModel");
        sb.append('=');
        sb.append(((this.roleModel == null)?"<null>":this.roleModel));
        sb.append(',');
        sb.append("roleRank");
        sb.append('=');
        sb.append(((this.roleRank == null)?"<null>":this.roleRank));
        sb.append(',');
        sb.append("leader");
        sb.append('=');
        sb.append(((this.leader == null)?"<null>":this.leader));
        sb.append(',');
        sb.append("roleSymbol");
        sb.append('=');
        sb.append(((this.roleSymbol == null)?"<null>":this.roleSymbol));
        sb.append(',');
        sb.append("roleActor");
        sb.append('=');
        sb.append(((this.roleActor == null)?"<null>":this.roleActor));
        sb.append(',');
        sb.append("roleEquipment");
        sb.append('=');
        sb.append(((this.roleEquipment == null)?"<null>":this.roleEquipment));
        sb.append(',');
        sb.append("roleLocation");
        sb.append('=');
        sb.append(((this.roleLocation == null)?"<null>":this.roleLocation));
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
        result = ((result* 31)+((this.leader == null)? 0 :this.leader.hashCode()));
        result = ((result* 31)+((this.roleLocation == null)? 0 :this.roleLocation.hashCode()));
        result = ((result* 31)+((this.roleModel == null)? 0 :this.roleModel.hashCode()));
        result = ((result* 31)+((this.roleId == null)? 0 :this.roleId.hashCode()));
        result = ((result* 31)+((this.roleActor == null)? 0 :this.roleActor.hashCode()));
        result = ((result* 31)+((this.roleEquipment == null)? 0 :this.roleEquipment.hashCode()));
        result = ((result* 31)+((this.roleName == null)? 0 :this.roleName.hashCode()));
        result = ((result* 31)+((this.roleRank == null)? 0 :this.roleRank.hashCode()));
        result = ((result* 31)+((this.roleAbbrev == null)? 0 :this.roleAbbrev.hashCode()));
        result = ((result* 31)+((this.roleUuid == null)? 0 :this.roleUuid.hashCode()));
        result = ((result* 31)+((this.roleSymbol == null)? 0 :this.roleSymbol.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Role) == false) {
            return false;
        }
        Role rhs = ((Role) other);
        return ((((((((((((this.leader == rhs.leader)||((this.leader!= null)&&this.leader.equals(rhs.leader)))&&((this.roleLocation == rhs.roleLocation)||((this.roleLocation!= null)&&this.roleLocation.equals(rhs.roleLocation))))&&((this.roleModel == rhs.roleModel)||((this.roleModel!= null)&&this.roleModel.equals(rhs.roleModel))))&&((this.roleId == rhs.roleId)||((this.roleId!= null)&&this.roleId.equals(rhs.roleId))))&&((this.roleActor == rhs.roleActor)||((this.roleActor!= null)&&this.roleActor.equals(rhs.roleActor))))&&((this.roleEquipment == rhs.roleEquipment)||((this.roleEquipment!= null)&&this.roleEquipment.equals(rhs.roleEquipment))))&&((this.roleName == rhs.roleName)||((this.roleName!= null)&&this.roleName.equals(rhs.roleName))))&&((this.roleRank == rhs.roleRank)||((this.roleRank!= null)&&this.roleRank.equals(rhs.roleRank))))&&((this.roleAbbrev == rhs.roleAbbrev)||((this.roleAbbrev!= null)&&this.roleAbbrev.equals(rhs.roleAbbrev))))&&((this.roleUuid == rhs.roleUuid)||((this.roleUuid!= null)&&this.roleUuid.equals(rhs.roleUuid))))&&((this.roleSymbol == rhs.roleSymbol)||((this.roleSymbol!= null)&&this.roleSymbol.equals(rhs.roleSymbol))));
    }


    /**
     * required: rank typically assigned to role - used as default
     * 
     */
    public enum RoleRank {

        PVT("PVT"),
        CPL("CPL"),
        SPC("SPC"),
        SGT("SGT"),
        SSG("SSG"),
        SFC("SFC"),
        _1_LT("1LT"),
        CPT("CPT"),
        CIV("CIV"),
        CTR("CTR");
        private final String value;
        private final static Map<String, Role.RoleRank> CONSTANTS = new HashMap<String, Role.RoleRank>();

        static {
            for (Role.RoleRank c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        RoleRank(String value) {
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
        public static Role.RoleRank fromValue(String value) {
            Role.RoleRank constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
