
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
 * this is any static object that is non terrain and is added to the battlespace map as part of the environment like a trench, wire, fortification, wall, wrecks, trashpile, animals, etc...
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mapObjId",
    "mapObjName",
    "mapObjType",
    "mapObjLocation",
    "mapObjModel",
    "scale",
    "azimuthDirection",
    "altitudeOffset",
    "visualState",
    "smoke",
    "smokeDegree",
    "fire",
    "fireType"
})
public class MapObject {

    /**
     * required: exercise unique map object id
     * (Required)
     * 
     */
    @JsonProperty("mapObjId")
    @JsonPropertyDescription("required: exercise unique map object id")
    private Object mapObjId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjName")
    private String mapObjName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjType")
    private MapObject.MapObjType mapObjType;
    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("mapObjLocation")
    @JsonPropertyDescription("Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format")
    private List<Object> mapObjLocation = new ArrayList<Object>();
    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * (Required)
     * 
     */
    @JsonProperty("mapObjModel")
    @JsonPropertyDescription("describes the on-map 3D model associated with an actor, vehicle, equipment or map object")
    private _3dModel mapObjModel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scale")
    private Double scale;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("azimuthDirection")
    private Double azimuthDirection;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("altitudeOffset")
    private Double altitudeOffset;
    @JsonProperty("visualState")
    private String visualState;
    @JsonProperty("smoke")
    private Boolean smoke = false;
    @JsonProperty("smokeDegree")
    private Double smokeDegree;
    @JsonProperty("fire")
    private Boolean fire = false;
    @JsonProperty("fireType")
    private MapObject.FireType fireType;

    /**
     * required: exercise unique map object id
     * (Required)
     * 
     */
    @JsonProperty("mapObjId")
    public Object getMapObjId() {
        return mapObjId;
    }

    /**
     * required: exercise unique map object id
     * (Required)
     * 
     */
    @JsonProperty("mapObjId")
    public void setMapObjId(Object mapObjId) {
        this.mapObjId = mapObjId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjName")
    public String getMapObjName() {
        return mapObjName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjName")
    public void setMapObjName(String mapObjName) {
        this.mapObjName = mapObjName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjType")
    public MapObject.MapObjType getMapObjType() {
        return mapObjType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mapObjType")
    public void setMapObjType(MapObject.MapObjType mapObjType) {
        this.mapObjType = mapObjType;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("mapObjLocation")
    public List<Object> getMapObjLocation() {
        return mapObjLocation;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("mapObjLocation")
    public void setMapObjLocation(List<Object> mapObjLocation) {
        this.mapObjLocation = mapObjLocation;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * (Required)
     * 
     */
    @JsonProperty("mapObjModel")
    public _3dModel getMapObjModel() {
        return mapObjModel;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * (Required)
     * 
     */
    @JsonProperty("mapObjModel")
    public void setMapObjModel(_3dModel mapObjModel) {
        this.mapObjModel = mapObjModel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scale")
    public Double getScale() {
        return scale;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scale")
    public void setScale(Double scale) {
        this.scale = scale;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("azimuthDirection")
    public Double getAzimuthDirection() {
        return azimuthDirection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("azimuthDirection")
    public void setAzimuthDirection(Double azimuthDirection) {
        this.azimuthDirection = azimuthDirection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("altitudeOffset")
    public Double getAltitudeOffset() {
        return altitudeOffset;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("altitudeOffset")
    public void setAltitudeOffset(Double altitudeOffset) {
        this.altitudeOffset = altitudeOffset;
    }

    @JsonProperty("visualState")
    public String getVisualState() {
        return visualState;
    }

    @JsonProperty("visualState")
    public void setVisualState(String visualState) {
        this.visualState = visualState;
    }

    @JsonProperty("smoke")
    public Boolean getSmoke() {
        return smoke;
    }

    @JsonProperty("smoke")
    public void setSmoke(Boolean smoke) {
        this.smoke = smoke;
    }

    @JsonProperty("smokeDegree")
    public Double getSmokeDegree() {
        return smokeDegree;
    }

    @JsonProperty("smokeDegree")
    public void setSmokeDegree(Double smokeDegree) {
        this.smokeDegree = smokeDegree;
    }

    @JsonProperty("fire")
    public Boolean getFire() {
        return fire;
    }

    @JsonProperty("fire")
    public void setFire(Boolean fire) {
        this.fire = fire;
    }

    @JsonProperty("fireType")
    public MapObject.FireType getFireType() {
        return fireType;
    }

    @JsonProperty("fireType")
    public void setFireType(MapObject.FireType fireType) {
        this.fireType = fireType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MapObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("mapObjId");
        sb.append('=');
        sb.append(((this.mapObjId == null)?"<null>":this.mapObjId));
        sb.append(',');
        sb.append("mapObjName");
        sb.append('=');
        sb.append(((this.mapObjName == null)?"<null>":this.mapObjName));
        sb.append(',');
        sb.append("mapObjType");
        sb.append('=');
        sb.append(((this.mapObjType == null)?"<null>":this.mapObjType));
        sb.append(',');
        sb.append("mapObjLocation");
        sb.append('=');
        sb.append(((this.mapObjLocation == null)?"<null>":this.mapObjLocation));
        sb.append(',');
        sb.append("mapObjModel");
        sb.append('=');
        sb.append(((this.mapObjModel == null)?"<null>":this.mapObjModel));
        sb.append(',');
        sb.append("scale");
        sb.append('=');
        sb.append(((this.scale == null)?"<null>":this.scale));
        sb.append(',');
        sb.append("azimuthDirection");
        sb.append('=');
        sb.append(((this.azimuthDirection == null)?"<null>":this.azimuthDirection));
        sb.append(',');
        sb.append("altitudeOffset");
        sb.append('=');
        sb.append(((this.altitudeOffset == null)?"<null>":this.altitudeOffset));
        sb.append(',');
        sb.append("visualState");
        sb.append('=');
        sb.append(((this.visualState == null)?"<null>":this.visualState));
        sb.append(',');
        sb.append("smoke");
        sb.append('=');
        sb.append(((this.smoke == null)?"<null>":this.smoke));
        sb.append(',');
        sb.append("smokeDegree");
        sb.append('=');
        sb.append(((this.smokeDegree == null)?"<null>":this.smokeDegree));
        sb.append(',');
        sb.append("fire");
        sb.append('=');
        sb.append(((this.fire == null)?"<null>":this.fire));
        sb.append(',');
        sb.append("fireType");
        sb.append('=');
        sb.append(((this.fireType == null)?"<null>":this.fireType));
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
        result = ((result* 31)+((this.azimuthDirection == null)? 0 :this.azimuthDirection.hashCode()));
        result = ((result* 31)+((this.altitudeOffset == null)? 0 :this.altitudeOffset.hashCode()));
        result = ((result* 31)+((this.smoke == null)? 0 :this.smoke.hashCode()));
        result = ((result* 31)+((this.scale == null)? 0 :this.scale.hashCode()));
        result = ((result* 31)+((this.mapObjLocation == null)? 0 :this.mapObjLocation.hashCode()));
        result = ((result* 31)+((this.mapObjType == null)? 0 :this.mapObjType.hashCode()));
        result = ((result* 31)+((this.visualState == null)? 0 :this.visualState.hashCode()));
        result = ((result* 31)+((this.smokeDegree == null)? 0 :this.smokeDegree.hashCode()));
        result = ((result* 31)+((this.mapObjId == null)? 0 :this.mapObjId.hashCode()));
        result = ((result* 31)+((this.fireType == null)? 0 :this.fireType.hashCode()));
        result = ((result* 31)+((this.mapObjModel == null)? 0 :this.mapObjModel.hashCode()));
        result = ((result* 31)+((this.fire == null)? 0 :this.fire.hashCode()));
        result = ((result* 31)+((this.mapObjName == null)? 0 :this.mapObjName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MapObject) == false) {
            return false;
        }
        MapObject rhs = ((MapObject) other);
        return ((((((((((((((this.azimuthDirection == rhs.azimuthDirection)||((this.azimuthDirection!= null)&&this.azimuthDirection.equals(rhs.azimuthDirection)))&&((this.altitudeOffset == rhs.altitudeOffset)||((this.altitudeOffset!= null)&&this.altitudeOffset.equals(rhs.altitudeOffset))))&&((this.smoke == rhs.smoke)||((this.smoke!= null)&&this.smoke.equals(rhs.smoke))))&&((this.scale == rhs.scale)||((this.scale!= null)&&this.scale.equals(rhs.scale))))&&((this.mapObjLocation == rhs.mapObjLocation)||((this.mapObjLocation!= null)&&this.mapObjLocation.equals(rhs.mapObjLocation))))&&((this.mapObjType == rhs.mapObjType)||((this.mapObjType!= null)&&this.mapObjType.equals(rhs.mapObjType))))&&((this.visualState == rhs.visualState)||((this.visualState!= null)&&this.visualState.equals(rhs.visualState))))&&((this.smokeDegree == rhs.smokeDegree)||((this.smokeDegree!= null)&&this.smokeDegree.equals(rhs.smokeDegree))))&&((this.mapObjId == rhs.mapObjId)||((this.mapObjId!= null)&&this.mapObjId.equals(rhs.mapObjId))))&&((this.fireType == rhs.fireType)||((this.fireType!= null)&&this.fireType.equals(rhs.fireType))))&&((this.mapObjModel == rhs.mapObjModel)||((this.mapObjModel!= null)&&this.mapObjModel.equals(rhs.mapObjModel))))&&((this.fire == rhs.fire)||((this.fire!= null)&&this.fire.equals(rhs.fire))))&&((this.mapObjName == rhs.mapObjName)||((this.mapObjName!= null)&&this.mapObjName.equals(rhs.mapObjName))));
    }

    public enum FireType {

        ALPHA("alpha"),
        BRAVO("bravo"),
        CHARLIE("charlie"),
        DELTA("delta");
        private final String value;
        private final static Map<String, MapObject.FireType> CONSTANTS = new HashMap<String, MapObject.FireType>();

        static {
            for (MapObject.FireType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FireType(String value) {
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
        public static MapObject.FireType fromValue(String value) {
            MapObject.FireType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum MapObjType {

        BUILDING("building"),
        BARRIER("barrier"),
        WRECK("wreck"),
        FORTIFICATION("fortification"),
        FENCING("fencing"),
        SIGNS("signs"),
        EXPLOSIVE("explosive"),
        ANIMAL("animal");
        private final String value;
        private final static Map<String, MapObject.MapObjType> CONSTANTS = new HashMap<String, MapObject.MapObjType>();

        static {
            for (MapObject.MapObjType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MapObjType(String value) {
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
        public static MapObject.MapObjType fromValue(String value) {
            MapObject.MapObjType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
