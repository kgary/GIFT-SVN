
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
 * triggers activate xEvents and/or strategies or activities.  Can be a system action, actor-behavior, entity-behavior, entity-state transition, or a actor-state transition.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "triggerId",
    "triggerUuid",
    "triggerName",
    "targets",
    "triggerAction",
    "triggerObjects",
    "triggerDelayTime",
    "triggerEnableTime",
    "triggerActivities",
    "triggerActive",
    "repeat"
})
public class Trigger {

    /**
     * required: a unique local trigger identifier
     * (Required)
     * 
     */
    @JsonProperty("triggerId")
    @JsonPropertyDescription("required: a unique local trigger identifier")
    private Object triggerId;
    /**
     * optional: unique master trigger id for reuse
     * 
     */
    @JsonProperty("triggerUuid")
    @JsonPropertyDescription("optional: unique master trigger id for reuse")
    private URI triggerUuid = URI.create("https://www.army.mil/ste-tmt/triggers/#");
    /**
     * required: Defines the name of the trigger - referenced in menu.
     * (Required)
     * 
     */
    @JsonProperty("triggerName")
    @JsonPropertyDescription("required: Defines the name of the trigger - referenced in menu.")
    private String triggerName;
    /**
     * required: Defines the list of 'who/what' (actors/entities/activities) can trip this trigger - defined by their type and id
     * 
     */
    @JsonProperty("targets")
    @JsonPropertyDescription("required: Defines the list of 'who/what' (actors/entities/activities) can trip this trigger - defined by their type and id")
    private List<Target> targets = new ArrayList<Target>();
    /**
     * required: Defines what action target must do to the target object to trip trigger.
     * (Required)
     * 
     */
    @JsonProperty("triggerAction")
    @JsonPropertyDescription("required: Defines what action target must do to the target object to trip trigger.")
    private Trigger.TriggerAction triggerAction;
    /**
     * optional: Defines object/location/state that contains the trigger
     * 
     */
    @JsonProperty("triggerObjects")
    @JsonPropertyDescription("optional: Defines object/location/state that contains the trigger")
    private List<TriggerObject> triggerObjects = new ArrayList<TriggerObject>();
    /**
     * option: scenario time when the trigger actually activates after being tripped
     * 
     */
    @JsonProperty("triggerDelayTime")
    @JsonPropertyDescription("option: scenario time when the trigger actually activates after being tripped")
    private Object triggerDelayTime;
    /**
     * option: available only when trigger is not tripped.  window of scenario time when the trigger is enabled to be tripped
     * 
     */
    @JsonProperty("triggerEnableTime")
    @JsonPropertyDescription("option: available only when trigger is not tripped.  window of scenario time when the trigger is enabled to be tripped")
    private Object triggerEnableTime;
    /**
     * optional: activities that start when trigger activated
     * 
     */
    @JsonProperty("triggerActivities")
    @JsonPropertyDescription("optional: activities that start when trigger activated")
    private List<ActivityItem> triggerActivities = new ArrayList<ActivityItem>();
    /**
     * optional: controls trigger active - used to control xevent used
     * 
     */
    @JsonProperty("triggerActive")
    @JsonPropertyDescription("optional: controls trigger active - used to control xevent used")
    private Boolean triggerActive = true;
    /**
     * optional: allows trigger to be tripped again
     * 
     */
    @JsonProperty("repeat")
    @JsonPropertyDescription("optional: allows trigger to be tripped again")
    private Boolean repeat = false;

    /**
     * required: a unique local trigger identifier
     * (Required)
     * 
     */
    @JsonProperty("triggerId")
    public Object getTriggerId() {
        return triggerId;
    }

    /**
     * required: a unique local trigger identifier
     * (Required)
     * 
     */
    @JsonProperty("triggerId")
    public void setTriggerId(Object triggerId) {
        this.triggerId = triggerId;
    }

    /**
     * optional: unique master trigger id for reuse
     * 
     */
    @JsonProperty("triggerUuid")
    public URI getTriggerUuid() {
        return triggerUuid;
    }

    /**
     * optional: unique master trigger id for reuse
     * 
     */
    @JsonProperty("triggerUuid")
    public void setTriggerUuid(URI triggerUuid) {
        this.triggerUuid = triggerUuid;
    }

    /**
     * required: Defines the name of the trigger - referenced in menu.
     * (Required)
     * 
     */
    @JsonProperty("triggerName")
    public String getTriggerName() {
        return triggerName;
    }

    /**
     * required: Defines the name of the trigger - referenced in menu.
     * (Required)
     * 
     */
    @JsonProperty("triggerName")
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    /**
     * required: Defines the list of 'who/what' (actors/entities/activities) can trip this trigger - defined by their type and id
     * 
     */
    @JsonProperty("targets")
    public List<Target> getTargets() {
        return targets;
    }

    /**
     * required: Defines the list of 'who/what' (actors/entities/activities) can trip this trigger - defined by their type and id
     * 
     */
    @JsonProperty("targets")
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    /**
     * required: Defines what action target must do to the target object to trip trigger.
     * (Required)
     * 
     */
    @JsonProperty("triggerAction")
    public Trigger.TriggerAction getTriggerAction() {
        return triggerAction;
    }

    /**
     * required: Defines what action target must do to the target object to trip trigger.
     * (Required)
     * 
     */
    @JsonProperty("triggerAction")
    public void setTriggerAction(Trigger.TriggerAction triggerAction) {
        this.triggerAction = triggerAction;
    }

    /**
     * optional: Defines object/location/state that contains the trigger
     * 
     */
    @JsonProperty("triggerObjects")
    public List<TriggerObject> getTriggerObjects() {
        return triggerObjects;
    }

    /**
     * optional: Defines object/location/state that contains the trigger
     * 
     */
    @JsonProperty("triggerObjects")
    public void setTriggerObjects(List<TriggerObject> triggerObjects) {
        this.triggerObjects = triggerObjects;
    }

    /**
     * option: scenario time when the trigger actually activates after being tripped
     * 
     */
    @JsonProperty("triggerDelayTime")
    public Object getTriggerDelayTime() {
        return triggerDelayTime;
    }

    /**
     * option: scenario time when the trigger actually activates after being tripped
     * 
     */
    @JsonProperty("triggerDelayTime")
    public void setTriggerDelayTime(Object triggerDelayTime) {
        this.triggerDelayTime = triggerDelayTime;
    }

    /**
     * option: available only when trigger is not tripped.  window of scenario time when the trigger is enabled to be tripped
     * 
     */
    @JsonProperty("triggerEnableTime")
    public Object getTriggerEnableTime() {
        return triggerEnableTime;
    }

    /**
     * option: available only when trigger is not tripped.  window of scenario time when the trigger is enabled to be tripped
     * 
     */
    @JsonProperty("triggerEnableTime")
    public void setTriggerEnableTime(Object triggerEnableTime) {
        this.triggerEnableTime = triggerEnableTime;
    }

    /**
     * optional: activities that start when trigger activated
     * 
     */
    @JsonProperty("triggerActivities")
    public List<ActivityItem> getTriggerActivities() {
        return triggerActivities;
    }

    /**
     * optional: activities that start when trigger activated
     * 
     */
    @JsonProperty("triggerActivities")
    public void setTriggerActivities(List<ActivityItem> triggerActivities) {
        this.triggerActivities = triggerActivities;
    }

    /**
     * optional: controls trigger active - used to control xevent used
     * 
     */
    @JsonProperty("triggerActive")
    public Boolean getTriggerActive() {
        return triggerActive;
    }

    /**
     * optional: controls trigger active - used to control xevent used
     * 
     */
    @JsonProperty("triggerActive")
    public void setTriggerActive(Boolean triggerActive) {
        this.triggerActive = triggerActive;
    }

    /**
     * optional: allows trigger to be tripped again
     * 
     */
    @JsonProperty("repeat")
    public Boolean getRepeat() {
        return repeat;
    }

    /**
     * optional: allows trigger to be tripped again
     * 
     */
    @JsonProperty("repeat")
    public void setRepeat(Boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Trigger.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("triggerId");
        sb.append('=');
        sb.append(((this.triggerId == null)?"<null>":this.triggerId));
        sb.append(',');
        sb.append("triggerUuid");
        sb.append('=');
        sb.append(((this.triggerUuid == null)?"<null>":this.triggerUuid));
        sb.append(',');
        sb.append("triggerName");
        sb.append('=');
        sb.append(((this.triggerName == null)?"<null>":this.triggerName));
        sb.append(',');
        sb.append("targets");
        sb.append('=');
        sb.append(((this.targets == null)?"<null>":this.targets));
        sb.append(',');
        sb.append("triggerAction");
        sb.append('=');
        sb.append(((this.triggerAction == null)?"<null>":this.triggerAction));
        sb.append(',');
        sb.append("triggerObjects");
        sb.append('=');
        sb.append(((this.triggerObjects == null)?"<null>":this.triggerObjects));
        sb.append(',');
        sb.append("triggerDelayTime");
        sb.append('=');
        sb.append(((this.triggerDelayTime == null)?"<null>":this.triggerDelayTime));
        sb.append(',');
        sb.append("triggerEnableTime");
        sb.append('=');
        sb.append(((this.triggerEnableTime == null)?"<null>":this.triggerEnableTime));
        sb.append(',');
        sb.append("triggerActivities");
        sb.append('=');
        sb.append(((this.triggerActivities == null)?"<null>":this.triggerActivities));
        sb.append(',');
        sb.append("triggerActive");
        sb.append('=');
        sb.append(((this.triggerActive == null)?"<null>":this.triggerActive));
        sb.append(',');
        sb.append("repeat");
        sb.append('=');
        sb.append(((this.repeat == null)?"<null>":this.repeat));
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
        result = ((result* 31)+((this.triggerAction == null)? 0 :this.triggerAction.hashCode()));
        result = ((result* 31)+((this.triggerActive == null)? 0 :this.triggerActive.hashCode()));
        result = ((result* 31)+((this.triggerName == null)? 0 :this.triggerName.hashCode()));
        result = ((result* 31)+((this.triggerId == null)? 0 :this.triggerId.hashCode()));
        result = ((result* 31)+((this.repeat == null)? 0 :this.repeat.hashCode()));
        result = ((result* 31)+((this.triggerUuid == null)? 0 :this.triggerUuid.hashCode()));
        result = ((result* 31)+((this.triggerDelayTime == null)? 0 :this.triggerDelayTime.hashCode()));
        result = ((result* 31)+((this.triggerObjects == null)? 0 :this.triggerObjects.hashCode()));
        result = ((result* 31)+((this.targets == null)? 0 :this.targets.hashCode()));
        result = ((result* 31)+((this.triggerEnableTime == null)? 0 :this.triggerEnableTime.hashCode()));
        result = ((result* 31)+((this.triggerActivities == null)? 0 :this.triggerActivities.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Trigger) == false) {
            return false;
        }
        Trigger rhs = ((Trigger) other);
        return ((((((((((((this.triggerAction == rhs.triggerAction)||((this.triggerAction!= null)&&this.triggerAction.equals(rhs.triggerAction)))&&((this.triggerActive == rhs.triggerActive)||((this.triggerActive!= null)&&this.triggerActive.equals(rhs.triggerActive))))&&((this.triggerName == rhs.triggerName)||((this.triggerName!= null)&&this.triggerName.equals(rhs.triggerName))))&&((this.triggerId == rhs.triggerId)||((this.triggerId!= null)&&this.triggerId.equals(rhs.triggerId))))&&((this.repeat == rhs.repeat)||((this.repeat!= null)&&this.repeat.equals(rhs.repeat))))&&((this.triggerUuid == rhs.triggerUuid)||((this.triggerUuid!= null)&&this.triggerUuid.equals(rhs.triggerUuid))))&&((this.triggerDelayTime == rhs.triggerDelayTime)||((this.triggerDelayTime!= null)&&this.triggerDelayTime.equals(rhs.triggerDelayTime))))&&((this.triggerObjects == rhs.triggerObjects)||((this.triggerObjects!= null)&&this.triggerObjects.equals(rhs.triggerObjects))))&&((this.targets == rhs.targets)||((this.targets!= null)&&this.targets.equals(rhs.targets))))&&((this.triggerEnableTime == rhs.triggerEnableTime)||((this.triggerEnableTime!= null)&&this.triggerEnableTime.equals(rhs.triggerEnableTime))))&&((this.triggerActivities == rhs.triggerActivities)||((this.triggerActivities!= null)&&this.triggerActivities.equals(rhs.triggerActivities))));
    }


    /**
     * required: Defines what action target must do to the target object to trip trigger.
     * 
     */
    public enum TriggerAction {

        START("start"),
        STOP("stop"),
        ASSESS("assess"),
        CROSS("cross"),
        CHANGE_TO("change to"),
        SELECT("select"),
        COMPLETE("complete"),
        MOVE("move"),
        MOVE_TO("move to"),
        ENTER("enter"),
        EXIT("exit"),
        FIRE("fire"),
        TRIP("trip"),
        PERFORM("perform"),
        DETECT("detect"),
        SPEAK("speak"),
        SHUT_DOWN("shut down"),
        ACTIVATE("activate");
        private final String value;
        private final static Map<String, Trigger.TriggerAction> CONSTANTS = new HashMap<String, Trigger.TriggerAction>();

        static {
            for (Trigger.TriggerAction c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TriggerAction(String value) {
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
        public static Trigger.TriggerAction fromValue(String value) {
            Trigger.TriggerAction constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
