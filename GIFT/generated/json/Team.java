
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
 * a tactical small unit that is associated with a side.  A team can contain roles and/or sub-teams.  Each team has assigned actors in assigned role(s) at beginning of an exercise
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "teamId",
    "teamUuid",
    "teamDesignation",
    "teamType",
    "teamEchelon",
    "teamStatus",
    "onMap",
    "teamComms",
    "teamModel",
    "teamSymbol",
    "teamLocation",
    "teamEquipment",
    "teamRoles",
    "subTeams"
})
public class Team {

    /**
     * in exercise team identifier
     * (Required)
     * 
     */
    @JsonProperty("teamId")
    @JsonPropertyDescription("in exercise team identifier")
    private Object teamId;
    /**
     * global team identifier
     * 
     */
    @JsonProperty("teamUuid")
    @JsonPropertyDescription("global team identifier")
    private String teamUuid;
    /**
     * required: default name that can be overwritten with actual team name playing this exercise team
     * (Required)
     * 
     */
    @JsonProperty("teamDesignation")
    @JsonPropertyDescription("required: default name that can be overwritten with actual team name playing this exercise team")
    private String teamDesignation;
    /**
     * required: what type of team is required for this team - e.g., rifle-infantry, motorized-infantry, etc...
     * 
     */
    @JsonProperty("teamType")
    @JsonPropertyDescription("required: what type of team is required for this team - e.g., rifle-infantry, motorized-infantry, etc...")
    private Team.TeamType teamType;
    /**
     * required: what echelon is required for this team
     * (Required)
     * 
     */
    @JsonProperty("teamEchelon")
    @JsonPropertyDescription("required: what echelon is required for this team")
    private Team.TeamEchelon teamEchelon;
    /**
     * required: status of the team
     * 
     */
    @JsonProperty("teamStatus")
    @JsonPropertyDescription("required: status of the team")
    private Object teamStatus;
    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("onMap")
    @JsonPropertyDescription("required:is team on map or played in background - e.g., a training or command team")
    private Boolean onMap = true;
    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("teamComms")
    @JsonPropertyDescription("optional: what communication does unit have or can be on")
    private List<CommNet> teamComms = new ArrayList<CommNet>();
    /**
     * optional: what 3D model does the unit use
     * 
     */
    @JsonProperty("teamModel")
    @JsonPropertyDescription("optional: what 3D model does the unit use")
    private String teamModel;
    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("teamSymbol")
    @JsonPropertyDescription("optional: a glyph-symbol image sused to represent a team, role or object on map")
    private Glyph teamSymbol;
    /**
     * required: where team begins exercise-scenario at start time
     * 
     */
    @JsonProperty("teamLocation")
    @JsonPropertyDescription("required: where team begins exercise-scenario at start time")
    private Object teamLocation;
    /**
     * optional: what equipment does the team have in it
     * 
     */
    @JsonProperty("teamEquipment")
    @JsonPropertyDescription("optional: what equipment does the team have in it")
    private List<Object> teamEquipment = new ArrayList<Object>();
    /**
     * required: role(s) assigned to this team
     * 
     */
    @JsonProperty("teamRoles")
    @JsonPropertyDescription("required: role(s) assigned to this team")
    private List<Object> teamRoles = new ArrayList<Object>();
    /**
     * optional: sub team(s) assigned to this team
     * 
     */
    @JsonProperty("subTeams")
    @JsonPropertyDescription("optional: sub team(s) assigned to this team")
    private List<Object> subTeams = new ArrayList<Object>();

    /**
     * in exercise team identifier
     * (Required)
     * 
     */
    @JsonProperty("teamId")
    public Object getTeamId() {
        return teamId;
    }

    /**
     * in exercise team identifier
     * (Required)
     * 
     */
    @JsonProperty("teamId")
    public void setTeamId(Object teamId) {
        this.teamId = teamId;
    }

    /**
     * global team identifier
     * 
     */
    @JsonProperty("teamUuid")
    public String getTeamUuid() {
        return teamUuid;
    }

    /**
     * global team identifier
     * 
     */
    @JsonProperty("teamUuid")
    public void setTeamUuid(String teamUuid) {
        this.teamUuid = teamUuid;
    }

    /**
     * required: default name that can be overwritten with actual team name playing this exercise team
     * (Required)
     * 
     */
    @JsonProperty("teamDesignation")
    public String getTeamDesignation() {
        return teamDesignation;
    }

    /**
     * required: default name that can be overwritten with actual team name playing this exercise team
     * (Required)
     * 
     */
    @JsonProperty("teamDesignation")
    public void setTeamDesignation(String teamDesignation) {
        this.teamDesignation = teamDesignation;
    }

    /**
     * required: what type of team is required for this team - e.g., rifle-infantry, motorized-infantry, etc...
     * 
     */
    @JsonProperty("teamType")
    public Team.TeamType getTeamType() {
        return teamType;
    }

    /**
     * required: what type of team is required for this team - e.g., rifle-infantry, motorized-infantry, etc...
     * 
     */
    @JsonProperty("teamType")
    public void setTeamType(Team.TeamType teamType) {
        this.teamType = teamType;
    }

    /**
     * required: what echelon is required for this team
     * (Required)
     * 
     */
    @JsonProperty("teamEchelon")
    public Team.TeamEchelon getTeamEchelon() {
        return teamEchelon;
    }

    /**
     * required: what echelon is required for this team
     * (Required)
     * 
     */
    @JsonProperty("teamEchelon")
    public void setTeamEchelon(Team.TeamEchelon teamEchelon) {
        this.teamEchelon = teamEchelon;
    }

    /**
     * required: status of the team
     * 
     */
    @JsonProperty("teamStatus")
    public Object getTeamStatus() {
        return teamStatus;
    }

    /**
     * required: status of the team
     * 
     */
    @JsonProperty("teamStatus")
    public void setTeamStatus(Object teamStatus) {
        this.teamStatus = teamStatus;
    }

    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("onMap")
    public Boolean getOnMap() {
        return onMap;
    }

    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("onMap")
    public void setOnMap(Boolean onMap) {
        this.onMap = onMap;
    }

    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("teamComms")
    public List<CommNet> getTeamComms() {
        return teamComms;
    }

    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("teamComms")
    public void setTeamComms(List<CommNet> teamComms) {
        this.teamComms = teamComms;
    }

    /**
     * optional: what 3D model does the unit use
     * 
     */
    @JsonProperty("teamModel")
    public String getTeamModel() {
        return teamModel;
    }

    /**
     * optional: what 3D model does the unit use
     * 
     */
    @JsonProperty("teamModel")
    public void setTeamModel(String teamModel) {
        this.teamModel = teamModel;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("teamSymbol")
    public Glyph getTeamSymbol() {
        return teamSymbol;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("teamSymbol")
    public void setTeamSymbol(Glyph teamSymbol) {
        this.teamSymbol = teamSymbol;
    }

    /**
     * required: where team begins exercise-scenario at start time
     * 
     */
    @JsonProperty("teamLocation")
    public Object getTeamLocation() {
        return teamLocation;
    }

    /**
     * required: where team begins exercise-scenario at start time
     * 
     */
    @JsonProperty("teamLocation")
    public void setTeamLocation(Object teamLocation) {
        this.teamLocation = teamLocation;
    }

    /**
     * optional: what equipment does the team have in it
     * 
     */
    @JsonProperty("teamEquipment")
    public List<Object> getTeamEquipment() {
        return teamEquipment;
    }

    /**
     * optional: what equipment does the team have in it
     * 
     */
    @JsonProperty("teamEquipment")
    public void setTeamEquipment(List<Object> teamEquipment) {
        this.teamEquipment = teamEquipment;
    }

    /**
     * required: role(s) assigned to this team
     * 
     */
    @JsonProperty("teamRoles")
    public List<Object> getTeamRoles() {
        return teamRoles;
    }

    /**
     * required: role(s) assigned to this team
     * 
     */
    @JsonProperty("teamRoles")
    public void setTeamRoles(List<Object> teamRoles) {
        this.teamRoles = teamRoles;
    }

    /**
     * optional: sub team(s) assigned to this team
     * 
     */
    @JsonProperty("subTeams")
    public List<Object> getSubTeams() {
        return subTeams;
    }

    /**
     * optional: sub team(s) assigned to this team
     * 
     */
    @JsonProperty("subTeams")
    public void setSubTeams(List<Object> subTeams) {
        this.subTeams = subTeams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Team.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("teamId");
        sb.append('=');
        sb.append(((this.teamId == null)?"<null>":this.teamId));
        sb.append(',');
        sb.append("teamUuid");
        sb.append('=');
        sb.append(((this.teamUuid == null)?"<null>":this.teamUuid));
        sb.append(',');
        sb.append("teamDesignation");
        sb.append('=');
        sb.append(((this.teamDesignation == null)?"<null>":this.teamDesignation));
        sb.append(',');
        sb.append("teamType");
        sb.append('=');
        sb.append(((this.teamType == null)?"<null>":this.teamType));
        sb.append(',');
        sb.append("teamEchelon");
        sb.append('=');
        sb.append(((this.teamEchelon == null)?"<null>":this.teamEchelon));
        sb.append(',');
        sb.append("teamStatus");
        sb.append('=');
        sb.append(((this.teamStatus == null)?"<null>":this.teamStatus));
        sb.append(',');
        sb.append("onMap");
        sb.append('=');
        sb.append(((this.onMap == null)?"<null>":this.onMap));
        sb.append(',');
        sb.append("teamComms");
        sb.append('=');
        sb.append(((this.teamComms == null)?"<null>":this.teamComms));
        sb.append(',');
        sb.append("teamModel");
        sb.append('=');
        sb.append(((this.teamModel == null)?"<null>":this.teamModel));
        sb.append(',');
        sb.append("teamSymbol");
        sb.append('=');
        sb.append(((this.teamSymbol == null)?"<null>":this.teamSymbol));
        sb.append(',');
        sb.append("teamLocation");
        sb.append('=');
        sb.append(((this.teamLocation == null)?"<null>":this.teamLocation));
        sb.append(',');
        sb.append("teamEquipment");
        sb.append('=');
        sb.append(((this.teamEquipment == null)?"<null>":this.teamEquipment));
        sb.append(',');
        sb.append("teamRoles");
        sb.append('=');
        sb.append(((this.teamRoles == null)?"<null>":this.teamRoles));
        sb.append(',');
        sb.append("subTeams");
        sb.append('=');
        sb.append(((this.subTeams == null)?"<null>":this.subTeams));
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
        result = ((result* 31)+((this.teamLocation == null)? 0 :this.teamLocation.hashCode()));
        result = ((result* 31)+((this.teamModel == null)? 0 :this.teamModel.hashCode()));
        result = ((result* 31)+((this.teamSymbol == null)? 0 :this.teamSymbol.hashCode()));
        result = ((result* 31)+((this.teamEquipment == null)? 0 :this.teamEquipment.hashCode()));
        result = ((result* 31)+((this.teamRoles == null)? 0 :this.teamRoles.hashCode()));
        result = ((result* 31)+((this.onMap == null)? 0 :this.onMap.hashCode()));
        result = ((result* 31)+((this.teamComms == null)? 0 :this.teamComms.hashCode()));
        result = ((result* 31)+((this.teamUuid == null)? 0 :this.teamUuid.hashCode()));
        result = ((result* 31)+((this.teamDesignation == null)? 0 :this.teamDesignation.hashCode()));
        result = ((result* 31)+((this.teamId == null)? 0 :this.teamId.hashCode()));
        result = ((result* 31)+((this.teamType == null)? 0 :this.teamType.hashCode()));
        result = ((result* 31)+((this.teamEchelon == null)? 0 :this.teamEchelon.hashCode()));
        result = ((result* 31)+((this.teamStatus == null)? 0 :this.teamStatus.hashCode()));
        result = ((result* 31)+((this.subTeams == null)? 0 :this.subTeams.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Team) == false) {
            return false;
        }
        Team rhs = ((Team) other);
        return (((((((((((((((this.teamLocation == rhs.teamLocation)||((this.teamLocation!= null)&&this.teamLocation.equals(rhs.teamLocation)))&&((this.teamModel == rhs.teamModel)||((this.teamModel!= null)&&this.teamModel.equals(rhs.teamModel))))&&((this.teamSymbol == rhs.teamSymbol)||((this.teamSymbol!= null)&&this.teamSymbol.equals(rhs.teamSymbol))))&&((this.teamEquipment == rhs.teamEquipment)||((this.teamEquipment!= null)&&this.teamEquipment.equals(rhs.teamEquipment))))&&((this.teamRoles == rhs.teamRoles)||((this.teamRoles!= null)&&this.teamRoles.equals(rhs.teamRoles))))&&((this.onMap == rhs.onMap)||((this.onMap!= null)&&this.onMap.equals(rhs.onMap))))&&((this.teamComms == rhs.teamComms)||((this.teamComms!= null)&&this.teamComms.equals(rhs.teamComms))))&&((this.teamUuid == rhs.teamUuid)||((this.teamUuid!= null)&&this.teamUuid.equals(rhs.teamUuid))))&&((this.teamDesignation == rhs.teamDesignation)||((this.teamDesignation!= null)&&this.teamDesignation.equals(rhs.teamDesignation))))&&((this.teamId == rhs.teamId)||((this.teamId!= null)&&this.teamId.equals(rhs.teamId))))&&((this.teamType == rhs.teamType)||((this.teamType!= null)&&this.teamType.equals(rhs.teamType))))&&((this.teamEchelon == rhs.teamEchelon)||((this.teamEchelon!= null)&&this.teamEchelon.equals(rhs.teamEchelon))))&&((this.teamStatus == rhs.teamStatus)||((this.teamStatus!= null)&&this.teamStatus.equals(rhs.teamStatus))))&&((this.subTeams == rhs.subTeams)||((this.subTeams!= null)&&this.subTeams.equals(rhs.subTeams))));
    }


    /**
     * required: what echelon is required for this team
     * 
     */
    public enum TeamEchelon {

        BRIGADE("Brigade"),
        BATTALION("Battalion"),
        SQUADRON("Squadron"),
        COMPANY("Company"),
        TROOP("Troop"),
        BATTERY("Battery"),
        PLATOON("Platoon"),
        SECTION("Section"),
        SQUAD("Squad"),
        TEAM("Team"),
        CREW("Crew"),
        EVAL("Eval"),
        OTHER("Other");
        private final String value;
        private final static Map<String, Team.TeamEchelon> CONSTANTS = new HashMap<String, Team.TeamEchelon>();

        static {
            for (Team.TeamEchelon c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TeamEchelon(String value) {
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
        public static Team.TeamEchelon fromValue(String value) {
            Team.TeamEchelon constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: what type of team is required for this team - e.g., rifle-infantry, motorized-infantry, etc...
     * 
     */
    public enum TeamType {

        RIFLE_INFANTRY("Rifle-Infantry"),
        MOTORIZED_INFANTRY("Motorized-Infantry"),
        ADMIN("Admin");
        private final String value;
        private final static Map<String, Team.TeamType> CONSTANTS = new HashMap<String, Team.TeamType>();

        static {
            for (Team.TeamType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TeamType(String value) {
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
        public static Team.TeamType fromValue(String value) {
            Team.TeamType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
