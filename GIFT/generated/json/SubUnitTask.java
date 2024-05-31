
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "unitDesignation",
    "unitTask"
})
public class SubUnitTask {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    private String unitDesignation;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitTask")
    private String unitTask;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    public String getUnitDesignation() {
        return unitDesignation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitDesignation")
    public void setUnitDesignation(String unitDesignation) {
        this.unitDesignation = unitDesignation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitTask")
    public String getUnitTask() {
        return unitTask;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("unitTask")
    public void setUnitTask(String unitTask) {
        this.unitTask = unitTask;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SubUnitTask.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("unitDesignation");
        sb.append('=');
        sb.append(((this.unitDesignation == null)?"<null>":this.unitDesignation));
        sb.append(',');
        sb.append("unitTask");
        sb.append('=');
        sb.append(((this.unitTask == null)?"<null>":this.unitTask));
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
        result = ((result* 31)+((this.unitDesignation == null)? 0 :this.unitDesignation.hashCode()));
        result = ((result* 31)+((this.unitTask == null)? 0 :this.unitTask.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubUnitTask) == false) {
            return false;
        }
        SubUnitTask rhs = ((SubUnitTask) other);
        return (((this.unitDesignation == rhs.unitDesignation)||((this.unitDesignation!= null)&&this.unitDesignation.equals(rhs.unitDesignation)))&&((this.unitTask == rhs.unitTask)||((this.unitTask!= null)&&this.unitTask.equals(rhs.unitTask))));
    }

}
