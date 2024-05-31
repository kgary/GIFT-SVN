
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "logistics"
})
public class Sustainment {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logistics")
    private Logistics logistics;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logistics")
    public Logistics getLogistics() {
        return logistics;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logistics")
    public void setLogistics(Logistics logistics) {
        this.logistics = logistics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Sustainment.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("logistics");
        sb.append('=');
        sb.append(((this.logistics == null)?"<null>":this.logistics));
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
        result = ((result* 31)+((this.logistics == null)? 0 :this.logistics.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Sustainment) == false) {
            return false;
        }
        Sustainment rhs = ((Sustainment) other);
        return ((this.logistics == rhs.logistics)||((this.logistics!= null)&&this.logistics.equals(rhs.logistics)));
    }

}
