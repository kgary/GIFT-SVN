
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
    "functionId",
    "functionInputs"
})
public class FunctionItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    private Object functionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionInputs")
    private List<String> functionInputs = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    public Object getFunctionId() {
        return functionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    public void setFunctionId(Object functionId) {
        this.functionId = functionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionInputs")
    public List<String> getFunctionInputs() {
        return functionInputs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionInputs")
    public void setFunctionInputs(List<String> functionInputs) {
        this.functionInputs = functionInputs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(FunctionItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("functionId");
        sb.append('=');
        sb.append(((this.functionId == null)?"<null>":this.functionId));
        sb.append(',');
        sb.append("functionInputs");
        sb.append('=');
        sb.append(((this.functionInputs == null)?"<null>":this.functionInputs));
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
        result = ((result* 31)+((this.functionId == null)? 0 :this.functionId.hashCode()));
        result = ((result* 31)+((this.functionInputs == null)? 0 :this.functionInputs.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionItem) == false) {
            return false;
        }
        FunctionItem rhs = ((FunctionItem) other);
        return (((this.functionId == rhs.functionId)||((this.functionId!= null)&&this.functionId.equals(rhs.functionId)))&&((this.functionInputs == rhs.functionInputs)||((this.functionInputs!= null)&&this.functionInputs.equals(rhs.functionInputs))));
    }

}
