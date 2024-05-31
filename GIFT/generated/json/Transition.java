
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
 * state are learning, cognitive, physiological or affective conditions that can trigger specific strategies
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transitionId",
    "transitionName",
    "transitionType",
    "initialState",
    "targetStates"
})
public class Transition {

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("transitionId")
    @JsonPropertyDescription("required: a unique local state identifier")
    private Object transitionId;
    /**
     * optional: the name of a transition
     * 
     */
    @JsonProperty("transitionName")
    @JsonPropertyDescription("optional: the name of a transition")
    private String transitionName;
    /**
     * required: the type of state
     * (Required)
     * 
     */
    @JsonProperty("transitionType")
    @JsonPropertyDescription("required: the type of state")
    private Transition.TransitionType transitionType;
    /**
     * optional: the initial state
     * 
     */
    @JsonProperty("initialState")
    @JsonPropertyDescription("optional: the initial state")
    private Object initialState;
    /**
     * required: the target state
     * (Required)
     * 
     */
    @JsonProperty("targetStates")
    @JsonPropertyDescription("required: the target state")
    private List<Object> targetStates = new ArrayList<Object>();

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("transitionId")
    public Object getTransitionId() {
        return transitionId;
    }

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("transitionId")
    public void setTransitionId(Object transitionId) {
        this.transitionId = transitionId;
    }

    /**
     * optional: the name of a transition
     * 
     */
    @JsonProperty("transitionName")
    public String getTransitionName() {
        return transitionName;
    }

    /**
     * optional: the name of a transition
     * 
     */
    @JsonProperty("transitionName")
    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    /**
     * required: the type of state
     * (Required)
     * 
     */
    @JsonProperty("transitionType")
    public Transition.TransitionType getTransitionType() {
        return transitionType;
    }

    /**
     * required: the type of state
     * (Required)
     * 
     */
    @JsonProperty("transitionType")
    public void setTransitionType(Transition.TransitionType transitionType) {
        this.transitionType = transitionType;
    }

    /**
     * optional: the initial state
     * 
     */
    @JsonProperty("initialState")
    public Object getInitialState() {
        return initialState;
    }

    /**
     * optional: the initial state
     * 
     */
    @JsonProperty("initialState")
    public void setInitialState(Object initialState) {
        this.initialState = initialState;
    }

    /**
     * required: the target state
     * (Required)
     * 
     */
    @JsonProperty("targetStates")
    public List<Object> getTargetStates() {
        return targetStates;
    }

    /**
     * required: the target state
     * (Required)
     * 
     */
    @JsonProperty("targetStates")
    public void setTargetStates(List<Object> targetStates) {
        this.targetStates = targetStates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Transition.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("transitionId");
        sb.append('=');
        sb.append(((this.transitionId == null)?"<null>":this.transitionId));
        sb.append(',');
        sb.append("transitionName");
        sb.append('=');
        sb.append(((this.transitionName == null)?"<null>":this.transitionName));
        sb.append(',');
        sb.append("transitionType");
        sb.append('=');
        sb.append(((this.transitionType == null)?"<null>":this.transitionType));
        sb.append(',');
        sb.append("initialState");
        sb.append('=');
        sb.append(((this.initialState == null)?"<null>":this.initialState));
        sb.append(',');
        sb.append("targetStates");
        sb.append('=');
        sb.append(((this.targetStates == null)?"<null>":this.targetStates));
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
        result = ((result* 31)+((this.initialState == null)? 0 :this.initialState.hashCode()));
        result = ((result* 31)+((this.targetStates == null)? 0 :this.targetStates.hashCode()));
        result = ((result* 31)+((this.transitionId == null)? 0 :this.transitionId.hashCode()));
        result = ((result* 31)+((this.transitionType == null)? 0 :this.transitionType.hashCode()));
        result = ((result* 31)+((this.transitionName == null)? 0 :this.transitionName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Transition) == false) {
            return false;
        }
        Transition rhs = ((Transition) other);
        return ((((((this.initialState == rhs.initialState)||((this.initialState!= null)&&this.initialState.equals(rhs.initialState)))&&((this.targetStates == rhs.targetStates)||((this.targetStates!= null)&&this.targetStates.equals(rhs.targetStates))))&&((this.transitionId == rhs.transitionId)||((this.transitionId!= null)&&this.transitionId.equals(rhs.transitionId))))&&((this.transitionType == rhs.transitionType)||((this.transitionType!= null)&&this.transitionType.equals(rhs.transitionType))))&&((this.transitionName == rhs.transitionName)||((this.transitionName!= null)&&this.transitionName.equals(rhs.transitionName))));
    }


    /**
     * required: the type of state
     * 
     */
    public enum TransitionType {

        TASK("task"),
        TEAM_SKILL("team-skill"),
        AFFECT("affect"),
        MEASURE("measure"),
        CRITERION("criterion");
        private final String value;
        private final static Map<String, Transition.TransitionType> CONSTANTS = new HashMap<String, Transition.TransitionType>();

        static {
            for (Transition.TransitionType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TransitionType(String value) {
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
        public static Transition.TransitionType fromValue(String value) {
            Transition.TransitionType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
