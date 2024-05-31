
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
 * this is a unit type of a specific echelon
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "unitId",
    "unitDomain",
    "unitType",
    "unitDesignation",
    "unitEchelon",
    "unitSymbol",
    "unitStatus",
    "unitComms",
    "unitOnMap",
    "unitLocation",
    "unitEquipment",
    "unitTeams",
    "unitRoles",
    "subUnits"
})
public class Unit {

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("unitId")
    @JsonPropertyDescription("required: unique ID of a force side in an exercise")
    private Object unitId;
    /**
     * optional: the proponent type
     * 
     */
    @JsonProperty("unitDomain")
    @JsonPropertyDescription("optional: the proponent type")
    private Unit.UnitDomain unitDomain;
    /**
     * optional: the force unit's capabilities and equipment - intel dependent
     * (Required)
     * 
     */
    @JsonProperty("unitType")
    @JsonPropertyDescription("optional: the force unit's capabilities and equipment - intel dependent")
    private Unit.UnitType unitType;
    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    @JsonPropertyDescription("required: the name of the organization")
    private String unitDesignation;
    /**
     * required: the echelon of the unit
     * (Required)
     * 
     */
    @JsonProperty("unitEchelon")
    @JsonPropertyDescription("required: the echelon of the unit")
    private Unit.UnitEchelon unitEchelon;
    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("unitSymbol")
    @JsonPropertyDescription("optional: a glyph-symbol image sused to represent a team, role or object on map")
    private Glyph unitSymbol;
    /**
     * required: status of the team
     * 
     */
    @JsonProperty("unitStatus")
    @JsonPropertyDescription("required: status of the team")
    private Object unitStatus;
    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("unitComms")
    @JsonPropertyDescription("optional: what communication does unit have or can be on")
    private List<CommNet> unitComms = new ArrayList<CommNet>();
    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("unitOnMap")
    @JsonPropertyDescription("required:is team on map or played in background - e.g., a training or command team")
    private Boolean unitOnMap = true;
    /**
     * required: where team leader begins exercise-scenario at start time
     * 
     */
    @JsonProperty("unitLocation")
    @JsonPropertyDescription("required: where team leader begins exercise-scenario at start time")
    private Object unitLocation;
    /**
     * optional: what equipment does the unit have
     * 
     */
    @JsonProperty("unitEquipment")
    @JsonPropertyDescription("optional: what equipment does the unit have")
    private List<Object> unitEquipment = new ArrayList<Object>();
    /**
     * optional: team(s) assigned to this team
     * 
     */
    @JsonProperty("unitTeams")
    @JsonPropertyDescription("optional: team(s) assigned to this team")
    private List<Object> unitTeams = new ArrayList<Object>();
    /**
     * required: role(s) associated with this team
     * 
     */
    @JsonProperty("unitRoles")
    @JsonPropertyDescription("required: role(s) associated with this team")
    private List<Object> unitRoles = new ArrayList<Object>();
    /**
     * optional: what units are assigned to this unit
     * 
     */
    @JsonProperty("subUnits")
    @JsonPropertyDescription("optional: what units are assigned to this unit")
    private List<Object> subUnits = new ArrayList<Object>();

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("unitId")
    public Object getUnitId() {
        return unitId;
    }

    /**
     * required: unique ID of a force side in an exercise
     * (Required)
     * 
     */
    @JsonProperty("unitId")
    public void setUnitId(Object unitId) {
        this.unitId = unitId;
    }

    /**
     * optional: the proponent type
     * 
     */
    @JsonProperty("unitDomain")
    public Unit.UnitDomain getUnitDomain() {
        return unitDomain;
    }

    /**
     * optional: the proponent type
     * 
     */
    @JsonProperty("unitDomain")
    public void setUnitDomain(Unit.UnitDomain unitDomain) {
        this.unitDomain = unitDomain;
    }

    /**
     * optional: the force unit's capabilities and equipment - intel dependent
     * (Required)
     * 
     */
    @JsonProperty("unitType")
    public Unit.UnitType getUnitType() {
        return unitType;
    }

    /**
     * optional: the force unit's capabilities and equipment - intel dependent
     * (Required)
     * 
     */
    @JsonProperty("unitType")
    public void setUnitType(Unit.UnitType unitType) {
        this.unitType = unitType;
    }

    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    public String getUnitDesignation() {
        return unitDesignation;
    }

    /**
     * required: the name of the organization
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    public void setUnitDesignation(String unitDesignation) {
        this.unitDesignation = unitDesignation;
    }

    /**
     * required: the echelon of the unit
     * (Required)
     * 
     */
    @JsonProperty("unitEchelon")
    public Unit.UnitEchelon getUnitEchelon() {
        return unitEchelon;
    }

    /**
     * required: the echelon of the unit
     * (Required)
     * 
     */
    @JsonProperty("unitEchelon")
    public void setUnitEchelon(Unit.UnitEchelon unitEchelon) {
        this.unitEchelon = unitEchelon;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("unitSymbol")
    public Glyph getUnitSymbol() {
        return unitSymbol;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("unitSymbol")
    public void setUnitSymbol(Glyph unitSymbol) {
        this.unitSymbol = unitSymbol;
    }

    /**
     * required: status of the team
     * 
     */
    @JsonProperty("unitStatus")
    public Object getUnitStatus() {
        return unitStatus;
    }

    /**
     * required: status of the team
     * 
     */
    @JsonProperty("unitStatus")
    public void setUnitStatus(Object unitStatus) {
        this.unitStatus = unitStatus;
    }

    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("unitComms")
    public List<CommNet> getUnitComms() {
        return unitComms;
    }

    /**
     * optional: what communication does unit have or can be on
     * 
     */
    @JsonProperty("unitComms")
    public void setUnitComms(List<CommNet> unitComms) {
        this.unitComms = unitComms;
    }

    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("unitOnMap")
    public Boolean getUnitOnMap() {
        return unitOnMap;
    }

    /**
     * required:is team on map or played in background - e.g., a training or command team
     * 
     */
    @JsonProperty("unitOnMap")
    public void setUnitOnMap(Boolean unitOnMap) {
        this.unitOnMap = unitOnMap;
    }

    /**
     * required: where team leader begins exercise-scenario at start time
     * 
     */
    @JsonProperty("unitLocation")
    public Object getUnitLocation() {
        return unitLocation;
    }

    /**
     * required: where team leader begins exercise-scenario at start time
     * 
     */
    @JsonProperty("unitLocation")
    public void setUnitLocation(Object unitLocation) {
        this.unitLocation = unitLocation;
    }

    /**
     * optional: what equipment does the unit have
     * 
     */
    @JsonProperty("unitEquipment")
    public List<Object> getUnitEquipment() {
        return unitEquipment;
    }

    /**
     * optional: what equipment does the unit have
     * 
     */
    @JsonProperty("unitEquipment")
    public void setUnitEquipment(List<Object> unitEquipment) {
        this.unitEquipment = unitEquipment;
    }

    /**
     * optional: team(s) assigned to this team
     * 
     */
    @JsonProperty("unitTeams")
    public List<Object> getUnitTeams() {
        return unitTeams;
    }

    /**
     * optional: team(s) assigned to this team
     * 
     */
    @JsonProperty("unitTeams")
    public void setUnitTeams(List<Object> unitTeams) {
        this.unitTeams = unitTeams;
    }

    /**
     * required: role(s) associated with this team
     * 
     */
    @JsonProperty("unitRoles")
    public List<Object> getUnitRoles() {
        return unitRoles;
    }

    /**
     * required: role(s) associated with this team
     * 
     */
    @JsonProperty("unitRoles")
    public void setUnitRoles(List<Object> unitRoles) {
        this.unitRoles = unitRoles;
    }

    /**
     * optional: what units are assigned to this unit
     * 
     */
    @JsonProperty("subUnits")
    public List<Object> getSubUnits() {
        return subUnits;
    }

    /**
     * optional: what units are assigned to this unit
     * 
     */
    @JsonProperty("subUnits")
    public void setSubUnits(List<Object> subUnits) {
        this.subUnits = subUnits;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Unit.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("unitId");
        sb.append('=');
        sb.append(((this.unitId == null)?"<null>":this.unitId));
        sb.append(',');
        sb.append("unitDomain");
        sb.append('=');
        sb.append(((this.unitDomain == null)?"<null>":this.unitDomain));
        sb.append(',');
        sb.append("unitType");
        sb.append('=');
        sb.append(((this.unitType == null)?"<null>":this.unitType));
        sb.append(',');
        sb.append("unitDesignation");
        sb.append('=');
        sb.append(((this.unitDesignation == null)?"<null>":this.unitDesignation));
        sb.append(',');
        sb.append("unitEchelon");
        sb.append('=');
        sb.append(((this.unitEchelon == null)?"<null>":this.unitEchelon));
        sb.append(',');
        sb.append("unitSymbol");
        sb.append('=');
        sb.append(((this.unitSymbol == null)?"<null>":this.unitSymbol));
        sb.append(',');
        sb.append("unitStatus");
        sb.append('=');
        sb.append(((this.unitStatus == null)?"<null>":this.unitStatus));
        sb.append(',');
        sb.append("unitComms");
        sb.append('=');
        sb.append(((this.unitComms == null)?"<null>":this.unitComms));
        sb.append(',');
        sb.append("unitOnMap");
        sb.append('=');
        sb.append(((this.unitOnMap == null)?"<null>":this.unitOnMap));
        sb.append(',');
        sb.append("unitLocation");
        sb.append('=');
        sb.append(((this.unitLocation == null)?"<null>":this.unitLocation));
        sb.append(',');
        sb.append("unitEquipment");
        sb.append('=');
        sb.append(((this.unitEquipment == null)?"<null>":this.unitEquipment));
        sb.append(',');
        sb.append("unitTeams");
        sb.append('=');
        sb.append(((this.unitTeams == null)?"<null>":this.unitTeams));
        sb.append(',');
        sb.append("unitRoles");
        sb.append('=');
        sb.append(((this.unitRoles == null)?"<null>":this.unitRoles));
        sb.append(',');
        sb.append("subUnits");
        sb.append('=');
        sb.append(((this.subUnits == null)?"<null>":this.subUnits));
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
        result = ((result* 31)+((this.unitTeams == null)? 0 :this.unitTeams.hashCode()));
        result = ((result* 31)+((this.unitOnMap == null)? 0 :this.unitOnMap.hashCode()));
        result = ((result* 31)+((this.unitStatus == null)? 0 :this.unitStatus.hashCode()));
        result = ((result* 31)+((this.unitComms == null)? 0 :this.unitComms.hashCode()));
        result = ((result* 31)+((this.unitEquipment == null)? 0 :this.unitEquipment.hashCode()));
        result = ((result* 31)+((this.unitType == null)? 0 :this.unitType.hashCode()));
        result = ((result* 31)+((this.subUnits == null)? 0 :this.subUnits.hashCode()));
        result = ((result* 31)+((this.unitDesignation == null)? 0 :this.unitDesignation.hashCode()));
        result = ((result* 31)+((this.unitDomain == null)? 0 :this.unitDomain.hashCode()));
        result = ((result* 31)+((this.unitLocation == null)? 0 :this.unitLocation.hashCode()));
        result = ((result* 31)+((this.unitId == null)? 0 :this.unitId.hashCode()));
        result = ((result* 31)+((this.unitEchelon == null)? 0 :this.unitEchelon.hashCode()));
        result = ((result* 31)+((this.unitSymbol == null)? 0 :this.unitSymbol.hashCode()));
        result = ((result* 31)+((this.unitRoles == null)? 0 :this.unitRoles.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Unit) == false) {
            return false;
        }
        Unit rhs = ((Unit) other);
        return (((((((((((((((this.unitTeams == rhs.unitTeams)||((this.unitTeams!= null)&&this.unitTeams.equals(rhs.unitTeams)))&&((this.unitOnMap == rhs.unitOnMap)||((this.unitOnMap!= null)&&this.unitOnMap.equals(rhs.unitOnMap))))&&((this.unitStatus == rhs.unitStatus)||((this.unitStatus!= null)&&this.unitStatus.equals(rhs.unitStatus))))&&((this.unitComms == rhs.unitComms)||((this.unitComms!= null)&&this.unitComms.equals(rhs.unitComms))))&&((this.unitEquipment == rhs.unitEquipment)||((this.unitEquipment!= null)&&this.unitEquipment.equals(rhs.unitEquipment))))&&((this.unitType == rhs.unitType)||((this.unitType!= null)&&this.unitType.equals(rhs.unitType))))&&((this.subUnits == rhs.subUnits)||((this.subUnits!= null)&&this.subUnits.equals(rhs.subUnits))))&&((this.unitDesignation == rhs.unitDesignation)||((this.unitDesignation!= null)&&this.unitDesignation.equals(rhs.unitDesignation))))&&((this.unitDomain == rhs.unitDomain)||((this.unitDomain!= null)&&this.unitDomain.equals(rhs.unitDomain))))&&((this.unitLocation == rhs.unitLocation)||((this.unitLocation!= null)&&this.unitLocation.equals(rhs.unitLocation))))&&((this.unitId == rhs.unitId)||((this.unitId!= null)&&this.unitId.equals(rhs.unitId))))&&((this.unitEchelon == rhs.unitEchelon)||((this.unitEchelon!= null)&&this.unitEchelon.equals(rhs.unitEchelon))))&&((this.unitSymbol == rhs.unitSymbol)||((this.unitSymbol!= null)&&this.unitSymbol.equals(rhs.unitSymbol))))&&((this.unitRoles == rhs.unitRoles)||((this.unitRoles!= null)&&this.unitRoles.equals(rhs.unitRoles))));
    }


    /**
     * optional: the proponent type
     * 
     */
    public enum UnitDomain {

        INFANTRY("Infantry"),
        ARMOR("Armor"),
        AVIATION("Aviation"),
        ENGINEERS("Engineers"),
        FIELD_ARTILLERY("Field Artillery"),
        AIR_DEFENSE("Air Defense"),
        CBRN("CBRN"),
        MEDICAL("Medical"),
        MILITARY_INTELLIGENCE("Military Intelligence"),
        MILITARY_POLICE("Military Police"),
        MISSION_COMMAND("Mission Command"),
        PSYCHOLOGICAL_OPERATIONS("Psychological Operations"),
        CIVIL_AFFAIRS("Civil Affairs"),
        ORDANCE("Ordance"),
        MAINTENANCE("Maintenance"),
        QUARTERMASTER("Quartermaster"),
        SUPPLY("Supply"),
        SPECIAL_OPERATIONS("Special Operations"),
        TRANSPORTATION("Transportation");
        private final String value;
        private final static Map<String, Unit.UnitDomain> CONSTANTS = new HashMap<String, Unit.UnitDomain>();

        static {
            for (Unit.UnitDomain c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        UnitDomain(String value) {
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
        public static Unit.UnitDomain fromValue(String value) {
            Unit.UnitDomain constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: the echelon of the unit
     * 
     */
    public enum UnitEchelon {

        SQUAD("Squad"),
        PLATOON("Platoon"),
        COMPANY("Company"),
        TROOP("Troop"),
        BATTERY("Battery"),
        BATTALION("Battalion"),
        SQUADRON("Squadron");
        private final String value;
        private final static Map<String, Unit.UnitEchelon> CONSTANTS = new HashMap<String, Unit.UnitEchelon>();

        static {
            for (Unit.UnitEchelon c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        UnitEchelon(String value) {
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
        public static Unit.UnitEchelon fromValue(String value) {
            Unit.UnitEchelon constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: the force unit's capabilities and equipment - intel dependent
     * 
     */
    public enum UnitType {

        RIFLE("Rifle"),
        ANTI_ARMOR("Anti-Armor"),
        RANGER("Ranger"),
        AIRBORNE("Airborne"),
        DISMOUNTED_CAVALRY("Dismounted Cavalry"),
        MOTORIZED("Motorized"),
        RECONNAISSANCE("Reconnaissance"),
        SCOUTING("Scouting"),
        SECURITY("Security");
        private final String value;
        private final static Map<String, Unit.UnitType> CONSTANTS = new HashMap<String, Unit.UnitType>();

        static {
            for (Unit.UnitType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        UnitType(String value) {
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
        public static Unit.UnitType fromValue(String value) {
            Unit.UnitType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
