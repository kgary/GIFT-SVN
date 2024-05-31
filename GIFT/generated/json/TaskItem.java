
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "taskId",
    "tgtPerformers"
})
public class TaskItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Object taskId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    private List<Performer> tgtPerformers = new ArrayList<Performer>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public Object getTaskId() {
        return taskId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Object taskId) {
        this.taskId = taskId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    public List<Performer> getTgtPerformers() {
        return tgtPerformers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tgtPerformers")
    public void setTgtPerformers(List<Performer> tgtPerformers) {
        this.tgtPerformers = tgtPerformers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TaskItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("taskId");
        sb.append('=');
        sb.append(((this.taskId == null)?"<null>":this.taskId));
        sb.append(',');
        sb.append("tgtPerformers");
        sb.append('=');
        sb.append(((this.tgtPerformers == null)?"<null>":this.tgtPerformers));
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
        result = ((result* 31)+((this.taskId == null)? 0 :this.taskId.hashCode()));
        result = ((result* 31)+((this.tgtPerformers == null)? 0 :this.tgtPerformers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskItem) == false) {
            return false;
        }
        TaskItem rhs = ((TaskItem) other);
        return (((this.taskId == rhs.taskId)||((this.taskId!= null)&&this.taskId.equals(rhs.taskId)))&&((this.tgtPerformers == rhs.tgtPerformers)||((this.tgtPerformers!= null)&&this.tgtPerformers.equals(rhs.tgtPerformers))));
    }

}
