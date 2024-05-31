
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
    "objectType",
    "objectId",
    "objectName"
})
public class TriggerObject {

    /**
     * the objects type that trips trigger.  can be more than one - e.g., measure and level
     * 
     */
    @JsonProperty("objectType")
    @JsonPropertyDescription("the objects type that trips trigger.  can be more than one - e.g., measure and level")
    private TriggerObject.ObjectType objectType;
    /**
     * required: a unique local trigger identifier
     * 
     */
    @JsonProperty("objectId")
    @JsonPropertyDescription("required: a unique local trigger identifier")
    private Object objectId;
    /**
     * an object name that trips trigger
     * 
     */
    @JsonProperty("objectName")
    @JsonPropertyDescription("an object name that trips trigger")
    private String objectName;

    /**
     * the objects type that trips trigger.  can be more than one - e.g., measure and level
     * 
     */
    @JsonProperty("objectType")
    public TriggerObject.ObjectType getObjectType() {
        return objectType;
    }

    /**
     * the objects type that trips trigger.  can be more than one - e.g., measure and level
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(TriggerObject.ObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * required: a unique local trigger identifier
     * 
     */
    @JsonProperty("objectId")
    public Object getObjectId() {
        return objectId;
    }

    /**
     * required: a unique local trigger identifier
     * 
     */
    @JsonProperty("objectId")
    public void setObjectId(Object objectId) {
        this.objectId = objectId;
    }

    /**
     * an object name that trips trigger
     * 
     */
    @JsonProperty("objectName")
    public String getObjectName() {
        return objectName;
    }

    /**
     * an object name that trips trigger
     * 
     */
    @JsonProperty("objectName")
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TriggerObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("objectType");
        sb.append('=');
        sb.append(((this.objectType == null)?"<null>":this.objectType));
        sb.append(',');
        sb.append("objectId");
        sb.append('=');
        sb.append(((this.objectId == null)?"<null>":this.objectId));
        sb.append(',');
        sb.append("objectName");
        sb.append('=');
        sb.append(((this.objectName == null)?"<null>":this.objectName));
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
        result = ((result* 31)+((this.objectName == null)? 0 :this.objectName.hashCode()));
        result = ((result* 31)+((this.objectId == null)? 0 :this.objectId.hashCode()));
        result = ((result* 31)+((this.objectType == null)? 0 :this.objectType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TriggerObject) == false) {
            return false;
        }
        TriggerObject rhs = ((TriggerObject) other);
        return ((((this.objectName == rhs.objectName)||((this.objectName!= null)&&this.objectName.equals(rhs.objectName)))&&((this.objectId == rhs.objectId)||((this.objectId!= null)&&this.objectId.equals(rhs.objectId))))&&((this.objectType == rhs.objectType)||((this.objectType!= null)&&this.objectType.equals(rhs.objectType))));
    }


    /**
     * the objects type that trips trigger.  can be more than one - e.g., measure and level
     * 
     */
    public enum ObjectType {

        XEVENT("xevent"),
        TASK("task"),
        MEASURE("measure"),
        CRITERIA("criteria"),
        ACTOR("actor"),
        ROLE("role"),
        TEAM("team"),
        SIDE("side"),
        STATE("state"),
        LEVEL("level"),
        OVERLAY("overlay"),
        FUNCTION("function"),
        DEVICE("device"),
        OBJECT("object"),
        ENTITY("entity"),
        EQUIPMENT("equipment");
        private final String value;
        private final static Map<String, TriggerObject.ObjectType> CONSTANTS = new HashMap<String, TriggerObject.ObjectType>();

        static {
            for (TriggerObject.ObjectType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ObjectType(String value) {
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
        public static TriggerObject.ObjectType fromValue(String value) {
            TriggerObject.ObjectType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
