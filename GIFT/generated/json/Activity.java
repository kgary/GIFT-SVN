
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "activityId",
    "activityName"
})
public class Activity {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    private Integer activityId;
    @JsonProperty("activityName")
    private String activityName;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public Integer getActivityId() {
        return activityId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activityId")
    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    @JsonProperty("activityName")
    public String getActivityName() {
        return activityName;
    }

    @JsonProperty("activityName")
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Activity.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("activityId");
        sb.append('=');
        sb.append(((this.activityId == null)?"<null>":this.activityId));
        sb.append(',');
        sb.append("activityName");
        sb.append('=');
        sb.append(((this.activityName == null)?"<null>":this.activityName));
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
        result = ((result* 31)+((this.activityName == null)? 0 :this.activityName.hashCode()));
        result = ((result* 31)+((this.activityId == null)? 0 :this.activityId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Activity) == false) {
            return false;
        }
        Activity rhs = ((Activity) other);
        return (((this.activityName == rhs.activityName)||((this.activityName!= null)&&this.activityName.equals(rhs.activityName)))&&((this.activityId == rhs.activityId)||((this.activityId!= null)&&this.activityId.equals(rhs.activityId))));
    }

}
