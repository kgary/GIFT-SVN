
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "targetId",
    "targetName",
    "targetType",
    "targetDefaultState"
})
public class Target {

    /**
     * required: points to a subject element
     * (Required)
     * 
     */
    @JsonProperty("targetId")
    @JsonPropertyDescription("required: points to a subject element")
    private Integer targetId;
    /**
     * optional: supplements subject element id
     * 
     */
    @JsonProperty("targetName")
    @JsonPropertyDescription("optional: supplements subject element id")
    private String targetName;
    /**
     * required: classifies subjects for filtering
     * (Required)
     * 
     */
    @JsonProperty("targetType")
    @JsonPropertyDescription("required: classifies subjects for filtering")
    private Target.TargetType targetType;
    /**
     * optional: a default state (level, articulation, bio, etc...) an target needs to be in to trip trigger
     * 
     */
    @JsonProperty("targetDefaultState")
    @JsonPropertyDescription("optional: a default state (level, articulation, bio, etc...) an target needs to be in to trip trigger")
    private Object targetDefaultState;

    /**
     * required: points to a subject element
     * (Required)
     * 
     */
    @JsonProperty("targetId")
    public Integer getTargetId() {
        return targetId;
    }

    /**
     * required: points to a subject element
     * (Required)
     * 
     */
    @JsonProperty("targetId")
    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    /**
     * optional: supplements subject element id
     * 
     */
    @JsonProperty("targetName")
    public String getTargetName() {
        return targetName;
    }

    /**
     * optional: supplements subject element id
     * 
     */
    @JsonProperty("targetName")
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * required: classifies subjects for filtering
     * (Required)
     * 
     */
    @JsonProperty("targetType")
    public Target.TargetType getTargetType() {
        return targetType;
    }

    /**
     * required: classifies subjects for filtering
     * (Required)
     * 
     */
    @JsonProperty("targetType")
    public void setTargetType(Target.TargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * optional: a default state (level, articulation, bio, etc...) an target needs to be in to trip trigger
     * 
     */
    @JsonProperty("targetDefaultState")
    public Object getTargetDefaultState() {
        return targetDefaultState;
    }

    /**
     * optional: a default state (level, articulation, bio, etc...) an target needs to be in to trip trigger
     * 
     */
    @JsonProperty("targetDefaultState")
    public void setTargetDefaultState(Object targetDefaultState) {
        this.targetDefaultState = targetDefaultState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Target.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("targetId");
        sb.append('=');
        sb.append(((this.targetId == null)?"<null>":this.targetId));
        sb.append(',');
        sb.append("targetName");
        sb.append('=');
        sb.append(((this.targetName == null)?"<null>":this.targetName));
        sb.append(',');
        sb.append("targetType");
        sb.append('=');
        sb.append(((this.targetType == null)?"<null>":this.targetType));
        sb.append(',');
        sb.append("targetDefaultState");
        sb.append('=');
        sb.append(((this.targetDefaultState == null)?"<null>":this.targetDefaultState));
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
        result = ((result* 31)+((this.targetName == null)? 0 :this.targetName.hashCode()));
        result = ((result* 31)+((this.targetType == null)? 0 :this.targetType.hashCode()));
        result = ((result* 31)+((this.targetDefaultState == null)? 0 :this.targetDefaultState.hashCode()));
        result = ((result* 31)+((this.targetId == null)? 0 :this.targetId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Target) == false) {
            return false;
        }
        Target rhs = ((Target) other);
        return (((((this.targetName == rhs.targetName)||((this.targetName!= null)&&this.targetName.equals(rhs.targetName)))&&((this.targetType == rhs.targetType)||((this.targetType!= null)&&this.targetType.equals(rhs.targetType))))&&((this.targetDefaultState == rhs.targetDefaultState)||((this.targetDefaultState!= null)&&this.targetDefaultState.equals(rhs.targetDefaultState))))&&((this.targetId == rhs.targetId)||((this.targetId!= null)&&this.targetId.equals(rhs.targetId))));
    }


    /**
     * required: classifies subjects for filtering
     * 
     */
    public enum TargetType {

        STRATEGY("strategy"),
        EXERCISE("exercise"),
        SIDE("side"),
        TEAM("team"),
        ROLE("role"),
        ACTOR("actor"),
        TRAINER("trainer"),
        PLATFORM("platform"),
        SYSTEM("system"),
        TRIGGER("trigger"),
        TRANSITION("transition");
        private final String value;
        private final static Map<String, Target.TargetType> CONSTANTS = new HashMap<String, Target.TargetType>();

        static {
            for (Target.TargetType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TargetType(String value) {
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
        public static Target.TargetType fromValue(String value) {
            Target.TargetType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
