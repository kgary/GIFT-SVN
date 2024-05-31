
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
 * the automated observation point to measure performance data or evidence from
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "srcId",
    "srcUuid",
    "srcName",
    "dataType",
    "location",
    "focalPoint",
    "focusTeam",
    "focusRole",
    "focusActor",
    "sampleRate",
    "mountType"
})
public class DataSource {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcId")
    private Object srcId;
    @JsonProperty("srcUuid")
    private String srcUuid;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcName")
    private String srcName;
    /**
     * the type of data an algorithm or trainer uses to compare values to level criterion
     * (Required)
     * 
     */
    @JsonProperty("dataType")
    @JsonPropertyDescription("the type of data an algorithm or trainer uses to compare values to level criterion")
    private DataSource.DataType dataType;
    /**
     * one or more coordinate points using a single string or an array of GDC, MGRS or GCC point formats
     * 
     */
    @JsonProperty("location")
    @JsonPropertyDescription("one or more coordinate points using a single string or an array of GDC, MGRS or GCC point formats")
    private Object location;
    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("focalPoint")
    @JsonPropertyDescription("Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)")
    private List<Double> focalPoint = new ArrayList<Double>();
    /**
     * optional: which team, is the data source focus binded to
     * 
     */
    @JsonProperty("focusTeam")
    @JsonPropertyDescription("optional: which team, is the data source focus binded to")
    private Integer focusTeam;
    /**
     * optional: which role is the data source focus binded to
     * 
     */
    @JsonProperty("focusRole")
    @JsonPropertyDescription("optional: which role is the data source focus binded to")
    private Integer focusRole;
    /**
     * optional: which actor is the data source focus binded to
     * 
     */
    @JsonProperty("focusActor")
    @JsonPropertyDescription("optional: which actor is the data source focus binded to")
    private Integer focusActor;
    /**
     * optional: what is the sample rate of the source - e.g., eye tracker
     * 
     */
    @JsonProperty("sampleRate")
    @JsonPropertyDescription("optional: what is the sample rate of the source - e.g., eye tracker")
    private Double sampleRate;
    /**
     * optional: what or who is the data sensor mounted on
     * (Required)
     * 
     */
    @JsonProperty("mountType")
    @JsonPropertyDescription("optional: what or who is the data sensor mounted on")
    private DataSource.MountType mountType;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcId")
    public Object getSrcId() {
        return srcId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcId")
    public void setSrcId(Object srcId) {
        this.srcId = srcId;
    }

    @JsonProperty("srcUuid")
    public String getSrcUuid() {
        return srcUuid;
    }

    @JsonProperty("srcUuid")
    public void setSrcUuid(String srcUuid) {
        this.srcUuid = srcUuid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcName")
    public String getSrcName() {
        return srcName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("srcName")
    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    /**
     * the type of data an algorithm or trainer uses to compare values to level criterion
     * (Required)
     * 
     */
    @JsonProperty("dataType")
    public DataSource.DataType getDataType() {
        return dataType;
    }

    /**
     * the type of data an algorithm or trainer uses to compare values to level criterion
     * (Required)
     * 
     */
    @JsonProperty("dataType")
    public void setDataType(DataSource.DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * one or more coordinate points using a single string or an array of GDC, MGRS or GCC point formats
     * 
     */
    @JsonProperty("location")
    public Object getLocation() {
        return location;
    }

    /**
     * one or more coordinate points using a single string or an array of GDC, MGRS or GCC point formats
     * 
     */
    @JsonProperty("location")
    public void setLocation(Object location) {
        this.location = location;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("focalPoint")
    public List<Double> getFocalPoint() {
        return focalPoint;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("focalPoint")
    public void setFocalPoint(List<Double> focalPoint) {
        this.focalPoint = focalPoint;
    }

    /**
     * optional: which team, is the data source focus binded to
     * 
     */
    @JsonProperty("focusTeam")
    public Integer getFocusTeam() {
        return focusTeam;
    }

    /**
     * optional: which team, is the data source focus binded to
     * 
     */
    @JsonProperty("focusTeam")
    public void setFocusTeam(Integer focusTeam) {
        this.focusTeam = focusTeam;
    }

    /**
     * optional: which role is the data source focus binded to
     * 
     */
    @JsonProperty("focusRole")
    public Integer getFocusRole() {
        return focusRole;
    }

    /**
     * optional: which role is the data source focus binded to
     * 
     */
    @JsonProperty("focusRole")
    public void setFocusRole(Integer focusRole) {
        this.focusRole = focusRole;
    }

    /**
     * optional: which actor is the data source focus binded to
     * 
     */
    @JsonProperty("focusActor")
    public Integer getFocusActor() {
        return focusActor;
    }

    /**
     * optional: which actor is the data source focus binded to
     * 
     */
    @JsonProperty("focusActor")
    public void setFocusActor(Integer focusActor) {
        this.focusActor = focusActor;
    }

    /**
     * optional: what is the sample rate of the source - e.g., eye tracker
     * 
     */
    @JsonProperty("sampleRate")
    public Double getSampleRate() {
        return sampleRate;
    }

    /**
     * optional: what is the sample rate of the source - e.g., eye tracker
     * 
     */
    @JsonProperty("sampleRate")
    public void setSampleRate(Double sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * optional: what or who is the data sensor mounted on
     * (Required)
     * 
     */
    @JsonProperty("mountType")
    public DataSource.MountType getMountType() {
        return mountType;
    }

    /**
     * optional: what or who is the data sensor mounted on
     * (Required)
     * 
     */
    @JsonProperty("mountType")
    public void setMountType(DataSource.MountType mountType) {
        this.mountType = mountType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DataSource.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("srcId");
        sb.append('=');
        sb.append(((this.srcId == null)?"<null>":this.srcId));
        sb.append(',');
        sb.append("srcUuid");
        sb.append('=');
        sb.append(((this.srcUuid == null)?"<null>":this.srcUuid));
        sb.append(',');
        sb.append("srcName");
        sb.append('=');
        sb.append(((this.srcName == null)?"<null>":this.srcName));
        sb.append(',');
        sb.append("dataType");
        sb.append('=');
        sb.append(((this.dataType == null)?"<null>":this.dataType));
        sb.append(',');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("focalPoint");
        sb.append('=');
        sb.append(((this.focalPoint == null)?"<null>":this.focalPoint));
        sb.append(',');
        sb.append("focusTeam");
        sb.append('=');
        sb.append(((this.focusTeam == null)?"<null>":this.focusTeam));
        sb.append(',');
        sb.append("focusRole");
        sb.append('=');
        sb.append(((this.focusRole == null)?"<null>":this.focusRole));
        sb.append(',');
        sb.append("focusActor");
        sb.append('=');
        sb.append(((this.focusActor == null)?"<null>":this.focusActor));
        sb.append(',');
        sb.append("sampleRate");
        sb.append('=');
        sb.append(((this.sampleRate == null)?"<null>":this.sampleRate));
        sb.append(',');
        sb.append("mountType");
        sb.append('=');
        sb.append(((this.mountType == null)?"<null>":this.mountType));
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
        result = ((result* 31)+((this.focusActor == null)? 0 :this.focusActor.hashCode()));
        result = ((result* 31)+((this.srcName == null)? 0 :this.srcName.hashCode()));
        result = ((result* 31)+((this.srcId == null)? 0 :this.srcId.hashCode()));
        result = ((result* 31)+((this.dataType == null)? 0 :this.dataType.hashCode()));
        result = ((result* 31)+((this.focusTeam == null)? 0 :this.focusTeam.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.focusRole == null)? 0 :this.focusRole.hashCode()));
        result = ((result* 31)+((this.focalPoint == null)? 0 :this.focalPoint.hashCode()));
        result = ((result* 31)+((this.sampleRate == null)? 0 :this.sampleRate.hashCode()));
        result = ((result* 31)+((this.mountType == null)? 0 :this.mountType.hashCode()));
        result = ((result* 31)+((this.srcUuid == null)? 0 :this.srcUuid.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataSource) == false) {
            return false;
        }
        DataSource rhs = ((DataSource) other);
        return ((((((((((((this.focusActor == rhs.focusActor)||((this.focusActor!= null)&&this.focusActor.equals(rhs.focusActor)))&&((this.srcName == rhs.srcName)||((this.srcName!= null)&&this.srcName.equals(rhs.srcName))))&&((this.srcId == rhs.srcId)||((this.srcId!= null)&&this.srcId.equals(rhs.srcId))))&&((this.dataType == rhs.dataType)||((this.dataType!= null)&&this.dataType.equals(rhs.dataType))))&&((this.focusTeam == rhs.focusTeam)||((this.focusTeam!= null)&&this.focusTeam.equals(rhs.focusTeam))))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.focusRole == rhs.focusRole)||((this.focusRole!= null)&&this.focusRole.equals(rhs.focusRole))))&&((this.focalPoint == rhs.focalPoint)||((this.focalPoint!= null)&&this.focalPoint.equals(rhs.focalPoint))))&&((this.sampleRate == rhs.sampleRate)||((this.sampleRate!= null)&&this.sampleRate.equals(rhs.sampleRate))))&&((this.mountType == rhs.mountType)||((this.mountType!= null)&&this.mountType.equals(rhs.mountType))))&&((this.srcUuid == rhs.srcUuid)||((this.srcUuid!= null)&&this.srcUuid.equals(rhs.srcUuid))));
    }


    /**
     * the type of data an algorithm or trainer uses to compare values to level criterion
     * 
     */
    public enum DataType {

        SCREEN_POV("screen-pov"),
        SCREEN_VIDEO("screen-video"),
        COMP_AUDIO("comp-audio"),
        VIDEO("video"),
        AUDIO("audio"),
        BIOMETRIC("biometric"),
        MOTION("motion"),
        COGNITIVE("cognitive"),
        MESSAGE("message"),
        DATA_FUNCTION("data-function");
        private final String value;
        private final static Map<String, DataSource.DataType> CONSTANTS = new HashMap<String, DataSource.DataType>();

        static {
            for (DataSource.DataType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DataType(String value) {
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
        public static DataSource.DataType fromValue(String value) {
            DataSource.DataType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: what or who is the data sensor mounted on
     * 
     */
    public enum MountType {

        FIXED("fixed"),
        ROBOT("robot"),
        ACTOR("actor"),
        GAME("game");
        private final String value;
        private final static Map<String, DataSource.MountType> CONSTANTS = new HashMap<String, DataSource.MountType>();

        static {
            for (DataSource.MountType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MountType(String value) {
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
        public static DataSource.MountType fromValue(String value) {
            DataSource.MountType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
