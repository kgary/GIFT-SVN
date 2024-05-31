
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "intent",
    "conceptOfOperations"
})
public class Execution {

    /**
     * option: commander's input
     * (Required)
     * 
     */
    @JsonProperty("intent")
    @JsonPropertyDescription("option: commander's input")
    private Intent intent;
    /**
     * Required: a statement that directs the method in which subordinate units cooperate and the sequence of actions to achieve the end state.  Usually with a map, overlays or sand table.
     * (Required)
     * 
     */
    @JsonProperty("conceptOfOperations")
    @JsonPropertyDescription("Required: a statement that directs the method in which subordinate units cooperate and the sequence of actions to achieve the end state.  Usually with a map, overlays or sand table.")
    private ConceptOfOperations conceptOfOperations;

    /**
     * option: commander's input
     * (Required)
     * 
     */
    @JsonProperty("intent")
    public Intent getIntent() {
        return intent;
    }

    /**
     * option: commander's input
     * (Required)
     * 
     */
    @JsonProperty("intent")
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    /**
     * Required: a statement that directs the method in which subordinate units cooperate and the sequence of actions to achieve the end state.  Usually with a map, overlays or sand table.
     * (Required)
     * 
     */
    @JsonProperty("conceptOfOperations")
    public ConceptOfOperations getConceptOfOperations() {
        return conceptOfOperations;
    }

    /**
     * Required: a statement that directs the method in which subordinate units cooperate and the sequence of actions to achieve the end state.  Usually with a map, overlays or sand table.
     * (Required)
     * 
     */
    @JsonProperty("conceptOfOperations")
    public void setConceptOfOperations(ConceptOfOperations conceptOfOperations) {
        this.conceptOfOperations = conceptOfOperations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Execution.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("intent");
        sb.append('=');
        sb.append(((this.intent == null)?"<null>":this.intent));
        sb.append(',');
        sb.append("conceptOfOperations");
        sb.append('=');
        sb.append(((this.conceptOfOperations == null)?"<null>":this.conceptOfOperations));
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
        result = ((result* 31)+((this.intent == null)? 0 :this.intent.hashCode()));
        result = ((result* 31)+((this.conceptOfOperations == null)? 0 :this.conceptOfOperations.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Execution) == false) {
            return false;
        }
        Execution rhs = ((Execution) other);
        return (((this.intent == rhs.intent)||((this.intent!= null)&&this.intent.equals(rhs.intent)))&&((this.conceptOfOperations == rhs.conceptOfOperations)||((this.conceptOfOperations!= null)&&this.conceptOfOperations.equals(rhs.conceptOfOperations))));
    }

}
