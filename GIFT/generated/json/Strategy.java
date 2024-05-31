
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
 * an set of activities (training app scripts) that provide either an experience alteration or actor intervention
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "strategyId",
    "strategyUuid",
    "strategyName",
    "strategySource",
    "difficultyLevel",
    "stressLevel",
    "activities"
})
public class Strategy {

    /**
     * required: a unique local learning strategy identifier
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    @JsonPropertyDescription("required: a unique local learning strategy identifier")
    private Object strategyId;
    @JsonProperty("strategyUuid")
    private String strategyUuid;
    @JsonProperty("strategyName")
    private String strategyName;
    /**
     * required: what/who is responsible for activating strategy.
     * (Required)
     * 
     */
    @JsonProperty("strategySource")
    @JsonPropertyDescription("required: what/who is responsible for activating strategy.")
    private Strategy.StrategySource strategySource = Strategy.StrategySource.fromValue("training app");
    /**
     * required: an integer indicating the inherit degree of difficulty the sub-activities add up to.
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    @JsonPropertyDescription("required: an integer indicating the inherit degree of difficulty the sub-activities add up to.")
    private Integer difficultyLevel;
    /**
     * optional: when tracking difficulty (complexity) and stress seperately, this is a number indicating the degree of positive or negative stress 'points' the sub-activities are expected to produce
     * 
     */
    @JsonProperty("stressLevel")
    @JsonPropertyDescription("optional: when tracking difficulty (complexity) and stress seperately, this is a number indicating the degree of positive or negative stress 'points' the sub-activities are expected to produce")
    private Double stressLevel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activities")
    private List<Activity> activities = new ArrayList<Activity>();

    /**
     * required: a unique local learning strategy identifier
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    public Object getStrategyId() {
        return strategyId;
    }

    /**
     * required: a unique local learning strategy identifier
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    public void setStrategyId(Object strategyId) {
        this.strategyId = strategyId;
    }

    @JsonProperty("strategyUuid")
    public String getStrategyUuid() {
        return strategyUuid;
    }

    @JsonProperty("strategyUuid")
    public void setStrategyUuid(String strategyUuid) {
        this.strategyUuid = strategyUuid;
    }

    @JsonProperty("strategyName")
    public String getStrategyName() {
        return strategyName;
    }

    @JsonProperty("strategyName")
    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    /**
     * required: what/who is responsible for activating strategy.
     * (Required)
     * 
     */
    @JsonProperty("strategySource")
    public Strategy.StrategySource getStrategySource() {
        return strategySource;
    }

    /**
     * required: what/who is responsible for activating strategy.
     * (Required)
     * 
     */
    @JsonProperty("strategySource")
    public void setStrategySource(Strategy.StrategySource strategySource) {
        this.strategySource = strategySource;
    }

    /**
     * required: an integer indicating the inherit degree of difficulty the sub-activities add up to.
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * required: an integer indicating the inherit degree of difficulty the sub-activities add up to.
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    /**
     * optional: when tracking difficulty (complexity) and stress seperately, this is a number indicating the degree of positive or negative stress 'points' the sub-activities are expected to produce
     * 
     */
    @JsonProperty("stressLevel")
    public Double getStressLevel() {
        return stressLevel;
    }

    /**
     * optional: when tracking difficulty (complexity) and stress seperately, this is a number indicating the degree of positive or negative stress 'points' the sub-activities are expected to produce
     * 
     */
    @JsonProperty("stressLevel")
    public void setStressLevel(Double stressLevel) {
        this.stressLevel = stressLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activities")
    public List<Activity> getActivities() {
        return activities;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activities")
    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Strategy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("strategyId");
        sb.append('=');
        sb.append(((this.strategyId == null)?"<null>":this.strategyId));
        sb.append(',');
        sb.append("strategyUuid");
        sb.append('=');
        sb.append(((this.strategyUuid == null)?"<null>":this.strategyUuid));
        sb.append(',');
        sb.append("strategyName");
        sb.append('=');
        sb.append(((this.strategyName == null)?"<null>":this.strategyName));
        sb.append(',');
        sb.append("strategySource");
        sb.append('=');
        sb.append(((this.strategySource == null)?"<null>":this.strategySource));
        sb.append(',');
        sb.append("difficultyLevel");
        sb.append('=');
        sb.append(((this.difficultyLevel == null)?"<null>":this.difficultyLevel));
        sb.append(',');
        sb.append("stressLevel");
        sb.append('=');
        sb.append(((this.stressLevel == null)?"<null>":this.stressLevel));
        sb.append(',');
        sb.append("activities");
        sb.append('=');
        sb.append(((this.activities == null)?"<null>":this.activities));
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
        result = ((result* 31)+((this.strategyName == null)? 0 :this.strategyName.hashCode()));
        result = ((result* 31)+((this.strategySource == null)? 0 :this.strategySource.hashCode()));
        result = ((result* 31)+((this.difficultyLevel == null)? 0 :this.difficultyLevel.hashCode()));
        result = ((result* 31)+((this.activities == null)? 0 :this.activities.hashCode()));
        result = ((result* 31)+((this.strategyId == null)? 0 :this.strategyId.hashCode()));
        result = ((result* 31)+((this.stressLevel == null)? 0 :this.stressLevel.hashCode()));
        result = ((result* 31)+((this.strategyUuid == null)? 0 :this.strategyUuid.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Strategy) == false) {
            return false;
        }
        Strategy rhs = ((Strategy) other);
        return ((((((((this.strategyName == rhs.strategyName)||((this.strategyName!= null)&&this.strategyName.equals(rhs.strategyName)))&&((this.strategySource == rhs.strategySource)||((this.strategySource!= null)&&this.strategySource.equals(rhs.strategySource))))&&((this.difficultyLevel == rhs.difficultyLevel)||((this.difficultyLevel!= null)&&this.difficultyLevel.equals(rhs.difficultyLevel))))&&((this.activities == rhs.activities)||((this.activities!= null)&&this.activities.equals(rhs.activities))))&&((this.strategyId == rhs.strategyId)||((this.strategyId!= null)&&this.strategyId.equals(rhs.strategyId))))&&((this.stressLevel == rhs.stressLevel)||((this.stressLevel!= null)&&this.stressLevel.equals(rhs.stressLevel))))&&((this.strategyUuid == rhs.strategyUuid)||((this.strategyUuid!= null)&&this.strategyUuid.equals(rhs.strategyUuid))));
    }


    /**
     * required: what/who is responsible for activating strategy.
     * 
     */
    public enum StrategySource {

        TRAINER("trainer"),
        TRAINING_APP("training app"),
        ADAPTION_ENGINE("adaption engine"),
        ITS("its");
        private final String value;
        private final static Map<String, Strategy.StrategySource> CONSTANTS = new HashMap<String, Strategy.StrategySource>();

        static {
            for (Strategy.StrategySource c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        StrategySource(String value) {
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
        public static Strategy.StrategySource fromValue(String value) {
            Strategy.StrategySource constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
