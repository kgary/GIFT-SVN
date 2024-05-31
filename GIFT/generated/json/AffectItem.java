
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
    "affectId",
    "tgtPerformers"
})
public class AffectItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("affectId")
    private Object affectId;
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
    @JsonProperty("affectId")
    public Object getAffectId() {
        return affectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("affectId")
    public void setAffectId(Object affectId) {
        this.affectId = affectId;
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
        sb.append(AffectItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("affectId");
        sb.append('=');
        sb.append(((this.affectId == null)?"<null>":this.affectId));
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
        result = ((result* 31)+((this.affectId == null)? 0 :this.affectId.hashCode()));
        result = ((result* 31)+((this.tgtPerformers == null)? 0 :this.tgtPerformers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AffectItem) == false) {
            return false;
        }
        AffectItem rhs = ((AffectItem) other);
        return (((this.affectId == rhs.affectId)||((this.affectId!= null)&&this.affectId.equals(rhs.affectId)))&&((this.tgtPerformers == rhs.tgtPerformers)||((this.tgtPerformers!= null)&&this.tgtPerformers.equals(rhs.tgtPerformers))));
    }

}
