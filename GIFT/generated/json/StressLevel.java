
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "stressorId",
    "stressType",
    "difficultyPoints",
    "stressPoints"
})
public class StressLevel {

    @JsonProperty("stressorId")
    private Object stressorId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressType")
    private StressLevel.StressType stressType;
    @JsonProperty("difficultyPoints")
    private Double difficultyPoints = 0.0D;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressPoints")
    private Double stressPoints = 0.0D;

    @JsonProperty("stressorId")
    public Object getStressorId() {
        return stressorId;
    }

    @JsonProperty("stressorId")
    public void setStressorId(Object stressorId) {
        this.stressorId = stressorId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressType")
    public StressLevel.StressType getStressType() {
        return stressType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressType")
    public void setStressType(StressLevel.StressType stressType) {
        this.stressType = stressType;
    }

    @JsonProperty("difficultyPoints")
    public Double getDifficultyPoints() {
        return difficultyPoints;
    }

    @JsonProperty("difficultyPoints")
    public void setDifficultyPoints(Double difficultyPoints) {
        this.difficultyPoints = difficultyPoints;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressPoints")
    public Double getStressPoints() {
        return stressPoints;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stressPoints")
    public void setStressPoints(Double stressPoints) {
        this.stressPoints = stressPoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StressLevel.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("stressorId");
        sb.append('=');
        sb.append(((this.stressorId == null)?"<null>":this.stressorId));
        sb.append(',');
        sb.append("stressType");
        sb.append('=');
        sb.append(((this.stressType == null)?"<null>":this.stressType));
        sb.append(',');
        sb.append("difficultyPoints");
        sb.append('=');
        sb.append(((this.difficultyPoints == null)?"<null>":this.difficultyPoints));
        sb.append(',');
        sb.append("stressPoints");
        sb.append('=');
        sb.append(((this.stressPoints == null)?"<null>":this.stressPoints));
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
        result = ((result* 31)+((this.stressPoints == null)? 0 :this.stressPoints.hashCode()));
        result = ((result* 31)+((this.stressorId == null)? 0 :this.stressorId.hashCode()));
        result = ((result* 31)+((this.stressType == null)? 0 :this.stressType.hashCode()));
        result = ((result* 31)+((this.difficultyPoints == null)? 0 :this.difficultyPoints.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StressLevel) == false) {
            return false;
        }
        StressLevel rhs = ((StressLevel) other);
        return (((((this.stressPoints == rhs.stressPoints)||((this.stressPoints!= null)&&this.stressPoints.equals(rhs.stressPoints)))&&((this.stressorId == rhs.stressorId)||((this.stressorId!= null)&&this.stressorId.equals(rhs.stressorId))))&&((this.stressType == rhs.stressType)||((this.stressType!= null)&&this.stressType.equals(rhs.stressType))))&&((this.difficultyPoints == rhs.difficultyPoints)||((this.difficultyPoints!= null)&&this.difficultyPoints.equals(rhs.difficultyPoints))));
    }

    public enum StressType {

        ENVIRONMENTAL("Environmental"),
        COGNITIVE("Cognitive"),
        PHYSICAL("Physical");
        private final String value;
        private final static Map<String, StressLevel.StressType> CONSTANTS = new HashMap<String, StressLevel.StressType>();

        static {
            for (StressLevel.StressType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        StressType(String value) {
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
        public static StressLevel.StressType fromValue(String value) {
            StressLevel.StressType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
