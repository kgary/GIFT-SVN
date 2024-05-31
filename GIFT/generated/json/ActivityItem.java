
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "activityId",
    "activityInputs"
})
public class ActivityItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    private Object activityId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityInputs")
    private String activityInputs;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public Object getActivityId() {
        return activityId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public void setActivityId(Object activityId) {
        this.activityId = activityId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityInputs")
    public String getActivityInputs() {
        return activityInputs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityInputs")
    public void setActivityInputs(String activityInputs) {
        this.activityInputs = activityInputs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ActivityItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("activityId");
        sb.append('=');
        sb.append(((this.activityId == null)?"<null>":this.activityId));
        sb.append(',');
        sb.append("activityInputs");
        sb.append('=');
        sb.append(((this.activityInputs == null)?"<null>":this.activityInputs));
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
        result = ((result* 31)+((this.activityId == null)? 0 :this.activityId.hashCode()));
        result = ((result* 31)+((this.activityInputs == null)? 0 :this.activityInputs.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActivityItem) == false) {
            return false;
        }
        ActivityItem rhs = ((ActivityItem) other);
        return (((this.activityId == rhs.activityId)||((this.activityId!= null)&&this.activityId.equals(rhs.activityId)))&&((this.activityInputs == rhs.activityInputs)||((this.activityInputs!= null)&&this.activityInputs.equals(rhs.activityInputs))));
    }

}
