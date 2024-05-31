
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
 * Activities are scripts used initiate, modify exercise or provide communication to actors
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "activityId",
    "activityUuid",
    "activityName",
    "difficultyLevel",
    "stressLevel",
    "activityType",
    "scriptHandler",
    "scriptParameters",
    "scriptCommand"
})
public class Activity__1 {

    /**
     * required: a unique local activity identifier
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    @JsonPropertyDescription("required: a unique local activity identifier")
    private Object activityId;
    /**
     * optional: unique master activity id for reuse
     * 
     */
    @JsonProperty("activityUuid")
    @JsonPropertyDescription("optional: unique master activity id for reuse")
    private String activityUuid;
    /**
     * optional: the name of a API function that runs a activity
     * (Required)
     * 
     */
    @JsonProperty("activityName")
    @JsonPropertyDescription("optional: the name of a API function that runs a activity")
    private String activityName;
    /**
     * optional: a float number indicating the inherit degree of difficulty / complexity the activity will present to the target actor.  Used to set a baseline difficulty rating in an given xEvent.  Not using this difficulty level corresponds to lowest difficulty rating.
     * 
     */
    @JsonProperty("difficultyLevel")
    @JsonPropertyDescription("optional: a float number indicating the inherit degree of difficulty / complexity the activity will present to the target actor.  Used to set a baseline difficulty rating in an given xEvent.  Not using this difficulty level corresponds to lowest difficulty rating.")
    private Double difficultyLevel;
    /**
     * optional: assigns number of stress points to players and type.  Positive number if negative stress.  Negative number if positive stress.  type is either environmental, cognitive or physical.  These points are summed up at the strategy and/or xEvent level
     * 
     */
    @JsonProperty("stressLevel")
    @JsonPropertyDescription("optional: assigns number of stress points to players and type.  Positive number if negative stress.  Negative number if positive stress.  type is either environmental, cognitive or physical.  These points are summed up at the strategy and/or xEvent level")
    private List<StressLevel> stressLevel = new ArrayList<StressLevel>();
    /**
     * required: helps filter GUI list
     * (Required)
     * 
     */
    @JsonProperty("activityType")
    @JsonPropertyDescription("required: helps filter GUI list")
    private Activity__1 .ActivityType activityType;
    @JsonProperty("scriptHandler")
    private String scriptHandler;
    @JsonProperty("scriptParameters")
    private String scriptParameters;
    /**
     * required: the script command that executes an activity
     * 
     */
    @JsonProperty("scriptCommand")
    @JsonPropertyDescription("required: the script command that executes an activity")
    private String scriptCommand;

    /**
     * required: a unique local activity identifier
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public Object getActivityId() {
        return activityId;
    }

    /**
     * required: a unique local activity identifier
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public void setActivityId(Object activityId) {
        this.activityId = activityId;
    }

    /**
     * optional: unique master activity id for reuse
     * 
     */
    @JsonProperty("activityUuid")
    public String getActivityUuid() {
        return activityUuid;
    }

    /**
     * optional: unique master activity id for reuse
     * 
     */
    @JsonProperty("activityUuid")
    public void setActivityUuid(String activityUuid) {
        this.activityUuid = activityUuid;
    }

    /**
     * optional: the name of a API function that runs a activity
     * (Required)
     * 
     */
    @JsonProperty("activityName")
    public String getActivityName() {
        return activityName;
    }

    /**
     * optional: the name of a API function that runs a activity
     * (Required)
     * 
     */
    @JsonProperty("activityName")
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * optional: a float number indicating the inherit degree of difficulty / complexity the activity will present to the target actor.  Used to set a baseline difficulty rating in an given xEvent.  Not using this difficulty level corresponds to lowest difficulty rating.
     * 
     */
    @JsonProperty("difficultyLevel")
    public Double getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * optional: a float number indicating the inherit degree of difficulty / complexity the activity will present to the target actor.  Used to set a baseline difficulty rating in an given xEvent.  Not using this difficulty level corresponds to lowest difficulty rating.
     * 
     */
    @JsonProperty("difficultyLevel")
    public void setDifficultyLevel(Double difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    /**
     * optional: assigns number of stress points to players and type.  Positive number if negative stress.  Negative number if positive stress.  type is either environmental, cognitive or physical.  These points are summed up at the strategy and/or xEvent level
     * 
     */
    @JsonProperty("stressLevel")
    public List<StressLevel> getStressLevel() {
        return stressLevel;
    }

    /**
     * optional: assigns number of stress points to players and type.  Positive number if negative stress.  Negative number if positive stress.  type is either environmental, cognitive or physical.  These points are summed up at the strategy and/or xEvent level
     * 
     */
    @JsonProperty("stressLevel")
    public void setStressLevel(List<StressLevel> stressLevel) {
        this.stressLevel = stressLevel;
    }

    /**
     * required: helps filter GUI list
     * (Required)
     * 
     */
    @JsonProperty("activityType")
    public Activity__1 .ActivityType getActivityType() {
        return activityType;
    }

    /**
     * required: helps filter GUI list
     * (Required)
     * 
     */
    @JsonProperty("activityType")
    public void setActivityType(Activity__1 .ActivityType activityType) {
        this.activityType = activityType;
    }

    @JsonProperty("scriptHandler")
    public String getScriptHandler() {
        return scriptHandler;
    }

    @JsonProperty("scriptHandler")
    public void setScriptHandler(String scriptHandler) {
        this.scriptHandler = scriptHandler;
    }

    @JsonProperty("scriptParameters")
    public String getScriptParameters() {
        return scriptParameters;
    }

    @JsonProperty("scriptParameters")
    public void setScriptParameters(String scriptParameters) {
        this.scriptParameters = scriptParameters;
    }

    /**
     * required: the script command that executes an activity
     * 
     */
    @JsonProperty("scriptCommand")
    public String getScriptCommand() {
        return scriptCommand;
    }

    /**
     * required: the script command that executes an activity
     * 
     */
    @JsonProperty("scriptCommand")
    public void setScriptCommand(String scriptCommand) {
        this.scriptCommand = scriptCommand;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Activity__1 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("activityId");
        sb.append('=');
        sb.append(((this.activityId == null)?"<null>":this.activityId));
        sb.append(',');
        sb.append("activityUuid");
        sb.append('=');
        sb.append(((this.activityUuid == null)?"<null>":this.activityUuid));
        sb.append(',');
        sb.append("activityName");
        sb.append('=');
        sb.append(((this.activityName == null)?"<null>":this.activityName));
        sb.append(',');
        sb.append("difficultyLevel");
        sb.append('=');
        sb.append(((this.difficultyLevel == null)?"<null>":this.difficultyLevel));
        sb.append(',');
        sb.append("stressLevel");
        sb.append('=');
        sb.append(((this.stressLevel == null)?"<null>":this.stressLevel));
        sb.append(',');
        sb.append("activityType");
        sb.append('=');
        sb.append(((this.activityType == null)?"<null>":this.activityType));
        sb.append(',');
        sb.append("scriptHandler");
        sb.append('=');
        sb.append(((this.scriptHandler == null)?"<null>":this.scriptHandler));
        sb.append(',');
        sb.append("scriptParameters");
        sb.append('=');
        sb.append(((this.scriptParameters == null)?"<null>":this.scriptParameters));
        sb.append(',');
        sb.append("scriptCommand");
        sb.append('=');
        sb.append(((this.scriptCommand == null)?"<null>":this.scriptCommand));
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
        result = ((result* 31)+((this.scriptParameters == null)? 0 :this.scriptParameters.hashCode()));
        result = ((result* 31)+((this.activityId == null)? 0 :this.activityId.hashCode()));
        result = ((result* 31)+((this.difficultyLevel == null)? 0 :this.difficultyLevel.hashCode()));
        result = ((result* 31)+((this.activityName == null)? 0 :this.activityName.hashCode()));
        result = ((result* 31)+((this.activityUuid == null)? 0 :this.activityUuid.hashCode()));
        result = ((result* 31)+((this.stressLevel == null)? 0 :this.stressLevel.hashCode()));
        result = ((result* 31)+((this.activityType == null)? 0 :this.activityType.hashCode()));
        result = ((result* 31)+((this.scriptHandler == null)? 0 :this.scriptHandler.hashCode()));
        result = ((result* 31)+((this.scriptCommand == null)? 0 :this.scriptCommand.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Activity__1) == false) {
            return false;
        }
        Activity__1 rhs = ((Activity__1) other);
        return ((((((((((this.scriptParameters == rhs.scriptParameters)||((this.scriptParameters!= null)&&this.scriptParameters.equals(rhs.scriptParameters)))&&((this.activityId == rhs.activityId)||((this.activityId!= null)&&this.activityId.equals(rhs.activityId))))&&((this.difficultyLevel == rhs.difficultyLevel)||((this.difficultyLevel!= null)&&this.difficultyLevel.equals(rhs.difficultyLevel))))&&((this.activityName == rhs.activityName)||((this.activityName!= null)&&this.activityName.equals(rhs.activityName))))&&((this.activityUuid == rhs.activityUuid)||((this.activityUuid!= null)&&this.activityUuid.equals(rhs.activityUuid))))&&((this.stressLevel == rhs.stressLevel)||((this.stressLevel!= null)&&this.stressLevel.equals(rhs.stressLevel))))&&((this.activityType == rhs.activityType)||((this.activityType!= null)&&this.activityType.equals(rhs.activityType))))&&((this.scriptHandler == rhs.scriptHandler)||((this.scriptHandler!= null)&&this.scriptHandler.equals(rhs.scriptHandler))))&&((this.scriptCommand == rhs.scriptCommand)||((this.scriptCommand!= null)&&this.scriptCommand.equals(rhs.scriptCommand))));
    }


    /**
     * required: helps filter GUI list
     * 
     */
    public enum ActivityType {

        SCENARIO_ADAPTATION("Scenario adaptation"),
        ACTOR_INTERVENTION("Actor intervention"),
        EXERCISE_CONTROL("Exercise control"),
        CUSTOM_SCRIPT("Custom script");
        private final String value;
        private final static Map<String, Activity__1 .ActivityType> CONSTANTS = new HashMap<String, Activity__1 .ActivityType>();

        static {
            for (Activity__1 .ActivityType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ActivityType(String value) {
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
        public static Activity__1 .ActivityType fromValue(String value) {
            Activity__1 .ActivityType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
