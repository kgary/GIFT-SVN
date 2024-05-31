
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * option: commander's input
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "purpose",
    "endState"
})
public class Intent {

    /**
     * required: describes the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("purpose")
    @JsonPropertyDescription("required: describes the 'why' of an assignment")
    private String purpose;
    /**
     * required: describes the commander's envisioned outcome of mission.  This should be identicle to the overall task outcome criteria
     * (Required)
     * 
     */
    @JsonProperty("endState")
    @JsonPropertyDescription("required: describes the commander's envisioned outcome of mission.  This should be identicle to the overall task outcome criteria")
    private String endState;

    /**
     * required: describes the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("purpose")
    public String getPurpose() {
        return purpose;
    }

    /**
     * required: describes the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("purpose")
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * required: describes the commander's envisioned outcome of mission.  This should be identicle to the overall task outcome criteria
     * (Required)
     * 
     */
    @JsonProperty("endState")
    public String getEndState() {
        return endState;
    }

    /**
     * required: describes the commander's envisioned outcome of mission.  This should be identicle to the overall task outcome criteria
     * (Required)
     * 
     */
    @JsonProperty("endState")
    public void setEndState(String endState) {
        this.endState = endState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Intent.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("purpose");
        sb.append('=');
        sb.append(((this.purpose == null)?"<null>":this.purpose));
        sb.append(',');
        sb.append("endState");
        sb.append('=');
        sb.append(((this.endState == null)?"<null>":this.endState));
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
        result = ((result* 31)+((this.purpose == null)? 0 :this.purpose.hashCode()));
        result = ((result* 31)+((this.endState == null)? 0 :this.endState.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Intent) == false) {
            return false;
        }
        Intent rhs = ((Intent) other);
        return (((this.purpose == rhs.purpose)||((this.purpose!= null)&&this.purpose.equals(rhs.purpose)))&&((this.endState == rhs.endState)||((this.endState!= null)&&this.endState.equals(rhs.endState))));
    }

}
