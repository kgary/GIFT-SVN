
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * required: the status of this xEvent to aid in query or selection.  Should be automated using external review tools
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "state",
    "stateDate"
})
public class Status {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private Status.State state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stateDate")
    private String stateDate;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public Status.State getState() {
        return state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(Status.State state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stateDate")
    public String getStateDate() {
        return stateDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stateDate")
    public void setStateDate(String stateDate) {
        this.stateDate = stateDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Status.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("stateDate");
        sb.append('=');
        sb.append(((this.stateDate == null)?"<null>":this.stateDate));
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
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.stateDate == null)? 0 :this.stateDate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Status) == false) {
            return false;
        }
        Status rhs = ((Status) other);
        return (((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state)))&&((this.stateDate == rhs.stateDate)||((this.stateDate!= null)&&this.stateDate.equals(rhs.stateDate))));
    }

    public enum State {

        DRAFT("Draft"),
        APPROVED("Approved"),
        UPDATE_IN_PROGRESS("UpdateInProgress"),
        DEPRECATED("Deprecated");
        private final String value;
        private final static Map<String, Status.State> CONSTANTS = new HashMap<String, Status.State>();

        static {
            for (Status.State c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        State(String value) {
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
        public static Status.State fromValue(String value) {
            Status.State constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
