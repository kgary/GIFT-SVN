
package generated.json;

import java.net.URI;
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
 * an equipment item that has a unique instance in an exercise.  Can be dropped, manned, carried, or used by actors
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "equipId",
    "equipUuid",
    "wpUrl",
    "equipName",
    "equipType",
    "model",
    "location",
    "direction",
    "disposable",
    "weight",
    "equipMode",
    "equipState",
    "ammoType",
    "ammoQty",
    "assignedActor",
    "inUse"
})
public class EquipmentItem {

    /**
     * required: the unique synthetic environment core (SECORE) master equipment list identifier
     * (Required)
     * 
     */
    @JsonProperty("equipId")
    @JsonPropertyDescription("required: the unique synthetic environment core (SECORE) master equipment list identifier")
    private Object equipId;
    /**
     * required: unique master equipment model designation or model number
     * (Required)
     * 
     */
    @JsonProperty("equipUuid")
    @JsonPropertyDescription("required: unique master equipment model designation or model number")
    private String equipUuid;
    /**
     * option: unique wikipedia reference to support equipment data
     * 
     */
    @JsonProperty("wpUrl")
    @JsonPropertyDescription("option: unique wikipedia reference to support equipment data")
    private URI wpUrl;
    /**
     * required: fas ref designated nomenclature given to equipment
     * (Required)
     * 
     */
    @JsonProperty("equipName")
    @JsonPropertyDescription("required: fas ref designated nomenclature given to equipment")
    private String equipName;
    /**
     * required: fas designated type of equipment that defines its use
     * (Required)
     * 
     */
    @JsonProperty("equipType")
    @JsonPropertyDescription("required: fas designated type of equipment that defines its use")
    private EquipmentItem.EquipType equipType;
    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    @JsonPropertyDescription("describes the on-map 3D model associated with an actor, vehicle, equipment or map object")
    private _3dModel model;
    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("location")
    @JsonPropertyDescription("Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format")
    private List<Object> location = new ArrayList<Object>();
    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("direction")
    @JsonPropertyDescription("Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)")
    private List<Double> direction = new ArrayList<Double>();
    /**
     * required: if equipment cannot be reused - launchers, grenades, etc...
     * 
     */
    @JsonProperty("disposable")
    @JsonPropertyDescription("required: if equipment cannot be reused - launchers, grenades, etc...")
    private Boolean disposable;
    /**
     * required: the weight of the equipment (in lbs) when carried
     * (Required)
     * 
     */
    @JsonProperty("weight")
    @JsonPropertyDescription("required: the weight of the equipment (in lbs) when carried")
    private Double weight;
    /**
     * required: which state or mode - if equipment item has modes or states of readiness prior to use.  e.g., safe, auto, semi, etc...
     * 
     */
    @JsonProperty("equipMode")
    @JsonPropertyDescription("required: which state or mode - if equipment item has modes or states of readiness prior to use.  e.g., safe, auto, semi, etc...")
    private String equipMode;
    /**
     * required: if equipment is good, damaged or malfunctioned
     * 
     */
    @JsonProperty("equipState")
    @JsonPropertyDescription("required: if equipment is good, damaged or malfunctioned")
    private EquipmentItem.EquipState equipState;
    /**
     * option: only if equipment is a weapon type - can be tracer, ball, AP, HE, smoke, etc...
     * 
     */
    @JsonProperty("ammoType")
    @JsonPropertyDescription("option: only if equipment is a weapon type - can be tracer, ball, AP, HE, smoke, etc...")
    private String ammoType;
    /**
     * option: only if equipment is a weapon type
     * 
     */
    @JsonProperty("ammoQty")
    @JsonPropertyDescription("option: only if equipment is a weapon type")
    private Integer ammoQty;
    /**
     * required: what actor is assigned to carry equipment - 0 means dropped
     * (Required)
     * 
     */
    @JsonProperty("assignedActor")
    @JsonPropertyDescription("required: what actor is assigned to carry equipment - 0 means dropped")
    private Integer assignedActor;
    /**
     * option: flag if this equipment is being used
     * 
     */
    @JsonProperty("inUse")
    @JsonPropertyDescription("option: flag if this equipment is being used")
    private Boolean inUse;

    /**
     * required: the unique synthetic environment core (SECORE) master equipment list identifier
     * (Required)
     * 
     */
    @JsonProperty("equipId")
    public Object getEquipId() {
        return equipId;
    }

    /**
     * required: the unique synthetic environment core (SECORE) master equipment list identifier
     * (Required)
     * 
     */
    @JsonProperty("equipId")
    public void setEquipId(Object equipId) {
        this.equipId = equipId;
    }

    /**
     * required: unique master equipment model designation or model number
     * (Required)
     * 
     */
    @JsonProperty("equipUuid")
    public String getEquipUuid() {
        return equipUuid;
    }

    /**
     * required: unique master equipment model designation or model number
     * (Required)
     * 
     */
    @JsonProperty("equipUuid")
    public void setEquipUuid(String equipUuid) {
        this.equipUuid = equipUuid;
    }

    /**
     * option: unique wikipedia reference to support equipment data
     * 
     */
    @JsonProperty("wpUrl")
    public URI getWpUrl() {
        return wpUrl;
    }

    /**
     * option: unique wikipedia reference to support equipment data
     * 
     */
    @JsonProperty("wpUrl")
    public void setWpUrl(URI wpUrl) {
        this.wpUrl = wpUrl;
    }

    /**
     * required: fas ref designated nomenclature given to equipment
     * (Required)
     * 
     */
    @JsonProperty("equipName")
    public String getEquipName() {
        return equipName;
    }

    /**
     * required: fas ref designated nomenclature given to equipment
     * (Required)
     * 
     */
    @JsonProperty("equipName")
    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    /**
     * required: fas designated type of equipment that defines its use
     * (Required)
     * 
     */
    @JsonProperty("equipType")
    public EquipmentItem.EquipType getEquipType() {
        return equipType;
    }

    /**
     * required: fas designated type of equipment that defines its use
     * (Required)
     * 
     */
    @JsonProperty("equipType")
    public void setEquipType(EquipmentItem.EquipType equipType) {
        this.equipType = equipType;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    public _3dModel getModel() {
        return model;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    public void setModel(_3dModel model) {
        this.model = model;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("location")
    public List<Object> getLocation() {
        return location;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("location")
    public void setLocation(List<Object> location) {
        this.location = location;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("direction")
    public List<Double> getDirection() {
        return direction;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("direction")
    public void setDirection(List<Double> direction) {
        this.direction = direction;
    }

    /**
     * required: if equipment cannot be reused - launchers, grenades, etc...
     * 
     */
    @JsonProperty("disposable")
    public Boolean getDisposable() {
        return disposable;
    }

    /**
     * required: if equipment cannot be reused - launchers, grenades, etc...
     * 
     */
    @JsonProperty("disposable")
    public void setDisposable(Boolean disposable) {
        this.disposable = disposable;
    }

    /**
     * required: the weight of the equipment (in lbs) when carried
     * (Required)
     * 
     */
    @JsonProperty("weight")
    public Double getWeight() {
        return weight;
    }

    /**
     * required: the weight of the equipment (in lbs) when carried
     * (Required)
     * 
     */
    @JsonProperty("weight")
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * required: which state or mode - if equipment item has modes or states of readiness prior to use.  e.g., safe, auto, semi, etc...
     * 
     */
    @JsonProperty("equipMode")
    public String getEquipMode() {
        return equipMode;
    }

    /**
     * required: which state or mode - if equipment item has modes or states of readiness prior to use.  e.g., safe, auto, semi, etc...
     * 
     */
    @JsonProperty("equipMode")
    public void setEquipMode(String equipMode) {
        this.equipMode = equipMode;
    }

    /**
     * required: if equipment is good, damaged or malfunctioned
     * 
     */
    @JsonProperty("equipState")
    public EquipmentItem.EquipState getEquipState() {
        return equipState;
    }

    /**
     * required: if equipment is good, damaged or malfunctioned
     * 
     */
    @JsonProperty("equipState")
    public void setEquipState(EquipmentItem.EquipState equipState) {
        this.equipState = equipState;
    }

    /**
     * option: only if equipment is a weapon type - can be tracer, ball, AP, HE, smoke, etc...
     * 
     */
    @JsonProperty("ammoType")
    public String getAmmoType() {
        return ammoType;
    }

    /**
     * option: only if equipment is a weapon type - can be tracer, ball, AP, HE, smoke, etc...
     * 
     */
    @JsonProperty("ammoType")
    public void setAmmoType(String ammoType) {
        this.ammoType = ammoType;
    }

    /**
     * option: only if equipment is a weapon type
     * 
     */
    @JsonProperty("ammoQty")
    public Integer getAmmoQty() {
        return ammoQty;
    }

    /**
     * option: only if equipment is a weapon type
     * 
     */
    @JsonProperty("ammoQty")
    public void setAmmoQty(Integer ammoQty) {
        this.ammoQty = ammoQty;
    }

    /**
     * required: what actor is assigned to carry equipment - 0 means dropped
     * (Required)
     * 
     */
    @JsonProperty("assignedActor")
    public Integer getAssignedActor() {
        return assignedActor;
    }

    /**
     * required: what actor is assigned to carry equipment - 0 means dropped
     * (Required)
     * 
     */
    @JsonProperty("assignedActor")
    public void setAssignedActor(Integer assignedActor) {
        this.assignedActor = assignedActor;
    }

    /**
     * option: flag if this equipment is being used
     * 
     */
    @JsonProperty("inUse")
    public Boolean getInUse() {
        return inUse;
    }

    /**
     * option: flag if this equipment is being used
     * 
     */
    @JsonProperty("inUse")
    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(EquipmentItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("equipId");
        sb.append('=');
        sb.append(((this.equipId == null)?"<null>":this.equipId));
        sb.append(',');
        sb.append("equipUuid");
        sb.append('=');
        sb.append(((this.equipUuid == null)?"<null>":this.equipUuid));
        sb.append(',');
        sb.append("wpUrl");
        sb.append('=');
        sb.append(((this.wpUrl == null)?"<null>":this.wpUrl));
        sb.append(',');
        sb.append("equipName");
        sb.append('=');
        sb.append(((this.equipName == null)?"<null>":this.equipName));
        sb.append(',');
        sb.append("equipType");
        sb.append('=');
        sb.append(((this.equipType == null)?"<null>":this.equipType));
        sb.append(',');
        sb.append("model");
        sb.append('=');
        sb.append(((this.model == null)?"<null>":this.model));
        sb.append(',');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("direction");
        sb.append('=');
        sb.append(((this.direction == null)?"<null>":this.direction));
        sb.append(',');
        sb.append("disposable");
        sb.append('=');
        sb.append(((this.disposable == null)?"<null>":this.disposable));
        sb.append(',');
        sb.append("weight");
        sb.append('=');
        sb.append(((this.weight == null)?"<null>":this.weight));
        sb.append(',');
        sb.append("equipMode");
        sb.append('=');
        sb.append(((this.equipMode == null)?"<null>":this.equipMode));
        sb.append(',');
        sb.append("equipState");
        sb.append('=');
        sb.append(((this.equipState == null)?"<null>":this.equipState));
        sb.append(',');
        sb.append("ammoType");
        sb.append('=');
        sb.append(((this.ammoType == null)?"<null>":this.ammoType));
        sb.append(',');
        sb.append("ammoQty");
        sb.append('=');
        sb.append(((this.ammoQty == null)?"<null>":this.ammoQty));
        sb.append(',');
        sb.append("assignedActor");
        sb.append('=');
        sb.append(((this.assignedActor == null)?"<null>":this.assignedActor));
        sb.append(',');
        sb.append("inUse");
        sb.append('=');
        sb.append(((this.inUse == null)?"<null>":this.inUse));
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
        result = ((result* 31)+((this.equipId == null)? 0 :this.equipId.hashCode()));
        result = ((result* 31)+((this.equipName == null)? 0 :this.equipName.hashCode()));
        result = ((result* 31)+((this.weight == null)? 0 :this.weight.hashCode()));
        result = ((result* 31)+((this.equipMode == null)? 0 :this.equipMode.hashCode()));
        result = ((result* 31)+((this.equipState == null)? 0 :this.equipState.hashCode()));
        result = ((result* 31)+((this.disposable == null)? 0 :this.disposable.hashCode()));
        result = ((result* 31)+((this.assignedActor == null)? 0 :this.assignedActor.hashCode()));
        result = ((result* 31)+((this.ammoType == null)? 0 :this.ammoType.hashCode()));
        result = ((result* 31)+((this.equipUuid == null)? 0 :this.equipUuid.hashCode()));
        result = ((result* 31)+((this.equipType == null)? 0 :this.equipType.hashCode()));
        result = ((result* 31)+((this.wpUrl == null)? 0 :this.wpUrl.hashCode()));
        result = ((result* 31)+((this.ammoQty == null)? 0 :this.ammoQty.hashCode()));
        result = ((result* 31)+((this.inUse == null)? 0 :this.inUse.hashCode()));
        result = ((result* 31)+((this.model == null)? 0 :this.model.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.direction == null)? 0 :this.direction.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EquipmentItem) == false) {
            return false;
        }
        EquipmentItem rhs = ((EquipmentItem) other);
        return (((((((((((((((((this.equipId == rhs.equipId)||((this.equipId!= null)&&this.equipId.equals(rhs.equipId)))&&((this.equipName == rhs.equipName)||((this.equipName!= null)&&this.equipName.equals(rhs.equipName))))&&((this.weight == rhs.weight)||((this.weight!= null)&&this.weight.equals(rhs.weight))))&&((this.equipMode == rhs.equipMode)||((this.equipMode!= null)&&this.equipMode.equals(rhs.equipMode))))&&((this.equipState == rhs.equipState)||((this.equipState!= null)&&this.equipState.equals(rhs.equipState))))&&((this.disposable == rhs.disposable)||((this.disposable!= null)&&this.disposable.equals(rhs.disposable))))&&((this.assignedActor == rhs.assignedActor)||((this.assignedActor!= null)&&this.assignedActor.equals(rhs.assignedActor))))&&((this.ammoType == rhs.ammoType)||((this.ammoType!= null)&&this.ammoType.equals(rhs.ammoType))))&&((this.equipUuid == rhs.equipUuid)||((this.equipUuid!= null)&&this.equipUuid.equals(rhs.equipUuid))))&&((this.equipType == rhs.equipType)||((this.equipType!= null)&&this.equipType.equals(rhs.equipType))))&&((this.wpUrl == rhs.wpUrl)||((this.wpUrl!= null)&&this.wpUrl.equals(rhs.wpUrl))))&&((this.ammoQty == rhs.ammoQty)||((this.ammoQty!= null)&&this.ammoQty.equals(rhs.ammoQty))))&&((this.inUse == rhs.inUse)||((this.inUse!= null)&&this.inUse.equals(rhs.inUse))))&&((this.model == rhs.model)||((this.model!= null)&&this.model.equals(rhs.model))))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.direction == rhs.direction)||((this.direction!= null)&&this.direction.equals(rhs.direction))));
    }


    /**
     * required: if equipment is good, damaged or malfunctioned
     * 
     */
    public enum EquipState {

        GOOD("good"),
        DAMAGED("damaged"),
        MALFUNCTIONED("malfunctioned");
        private final String value;
        private final static Map<String, EquipmentItem.EquipState> CONSTANTS = new HashMap<String, EquipmentItem.EquipState>();

        static {
            for (EquipmentItem.EquipState c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        EquipState(String value) {
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
        public static EquipmentItem.EquipState fromValue(String value) {
            EquipmentItem.EquipState constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: fas designated type of equipment that defines its use
     * 
     */
    public enum EquipType {

        WEAPON("weapon"),
        SUPPORT("support"),
        PROTECTION("protection"),
        FIXED("fixed"),
        SENSOR("sensor"),
        ADMIN("admin");
        private final String value;
        private final static Map<String, EquipmentItem.EquipType> CONSTANTS = new HashMap<String, EquipmentItem.EquipType>();

        static {
            for (EquipmentItem.EquipType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        EquipType(String value) {
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
        public static EquipmentItem.EquipType fromValue(String value) {
            EquipmentItem.EquipType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
