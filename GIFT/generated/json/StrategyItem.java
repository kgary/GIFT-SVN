
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "strategyId",
    "strategyInputs"
})
public class StrategyItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    private Object strategyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyInputs")
    private String strategyInputs;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    public Object getStrategyId() {
        return strategyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyId")
    public void setStrategyId(Object strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyInputs")
    public String getStrategyInputs() {
        return strategyInputs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("strategyInputs")
    public void setStrategyInputs(String strategyInputs) {
        this.strategyInputs = strategyInputs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StrategyItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("strategyId");
        sb.append('=');
        sb.append(((this.strategyId == null)?"<null>":this.strategyId));
        sb.append(',');
        sb.append("strategyInputs");
        sb.append('=');
        sb.append(((this.strategyInputs == null)?"<null>":this.strategyInputs));
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
        result = ((result* 31)+((this.strategyId == null)? 0 :this.strategyId.hashCode()));
        result = ((result* 31)+((this.strategyInputs == null)? 0 :this.strategyInputs.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StrategyItem) == false) {
            return false;
        }
        StrategyItem rhs = ((StrategyItem) other);
        return (((this.strategyId == rhs.strategyId)||((this.strategyId!= null)&&this.strategyId.equals(rhs.strategyId)))&&((this.strategyInputs == rhs.strategyInputs)||((this.strategyInputs!= null)&&this.strategyInputs.equals(rhs.strategyInputs))));
    }

}
