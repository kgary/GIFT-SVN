
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "decisionPoint",
    "task",
    "risk",
    "shapingOp",
    "multiplier"
})
public class CourseOfAction {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("decisionPoint")
    private String decisionPoint;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("task")
    private String task;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("risk")
    private String risk;
    @JsonProperty("shapingOp")
    private String shapingOp;
    @JsonProperty("multiplier")
    private String multiplier;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("decisionPoint")
    public String getDecisionPoint() {
        return decisionPoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("decisionPoint")
    public void setDecisionPoint(String decisionPoint) {
        this.decisionPoint = decisionPoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("task")
    public String getTask() {
        return task;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("task")
    public void setTask(String task) {
        this.task = task;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("risk")
    public String getRisk() {
        return risk;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("risk")
    public void setRisk(String risk) {
        this.risk = risk;
    }

    @JsonProperty("shapingOp")
    public String getShapingOp() {
        return shapingOp;
    }

    @JsonProperty("shapingOp")
    public void setShapingOp(String shapingOp) {
        this.shapingOp = shapingOp;
    }

    @JsonProperty("multiplier")
    public String getMultiplier() {
        return multiplier;
    }

    @JsonProperty("multiplier")
    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CourseOfAction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("decisionPoint");
        sb.append('=');
        sb.append(((this.decisionPoint == null)?"<null>":this.decisionPoint));
        sb.append(',');
        sb.append("task");
        sb.append('=');
        sb.append(((this.task == null)?"<null>":this.task));
        sb.append(',');
        sb.append("risk");
        sb.append('=');
        sb.append(((this.risk == null)?"<null>":this.risk));
        sb.append(',');
        sb.append("shapingOp");
        sb.append('=');
        sb.append(((this.shapingOp == null)?"<null>":this.shapingOp));
        sb.append(',');
        sb.append("multiplier");
        sb.append('=');
        sb.append(((this.multiplier == null)?"<null>":this.multiplier));
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
        result = ((result* 31)+((this.shapingOp == null)? 0 :this.shapingOp.hashCode()));
        result = ((result* 31)+((this.risk == null)? 0 :this.risk.hashCode()));
        result = ((result* 31)+((this.task == null)? 0 :this.task.hashCode()));
        result = ((result* 31)+((this.decisionPoint == null)? 0 :this.decisionPoint.hashCode()));
        result = ((result* 31)+((this.multiplier == null)? 0 :this.multiplier.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CourseOfAction) == false) {
            return false;
        }
        CourseOfAction rhs = ((CourseOfAction) other);
        return ((((((this.shapingOp == rhs.shapingOp)||((this.shapingOp!= null)&&this.shapingOp.equals(rhs.shapingOp)))&&((this.risk == rhs.risk)||((this.risk!= null)&&this.risk.equals(rhs.risk))))&&((this.task == rhs.task)||((this.task!= null)&&this.task.equals(rhs.task))))&&((this.decisionPoint == rhs.decisionPoint)||((this.decisionPoint!= null)&&this.decisionPoint.equals(rhs.decisionPoint))))&&((this.multiplier == rhs.multiplier)||((this.multiplier!= null)&&this.multiplier.equals(rhs.multiplier))));
    }

}
