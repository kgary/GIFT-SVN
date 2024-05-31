
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * a derived classification and declassify date
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "derivedBy",
    "declassifyDate"
})
public class SecurityClass {

    /**
     * level of classification - UNCLASS = no harm to national security, CUI = possible disclosure to persons without need to know, CONFIDENTIAL = Some harm to national security, SECRET = exceptional harm to national security
     * (Required)
     * 
     */
    @JsonProperty("label")
    @JsonPropertyDescription("level of classification - UNCLASS = no harm to national security, CUI = possible disclosure to persons without need to know, CONFIDENTIAL = Some harm to national security, SECRET = exceptional harm to national security")
    private SecurityClass.Label label = SecurityClass.Label.fromValue("UNCLASSIFIED");
    /**
     * what source is the classification from
     * 
     */
    @JsonProperty("derivedBy")
    @JsonPropertyDescription("what source is the classification from")
    private String derivedBy = "Multiple Sources";
    /**
     * when does classification expire - normally 25 years from date
     * 
     */
    @JsonProperty("declassifyDate")
    @JsonPropertyDescription("when does classification expire - normally 25 years from date")
    private String declassifyDate;

    /**
     * level of classification - UNCLASS = no harm to national security, CUI = possible disclosure to persons without need to know, CONFIDENTIAL = Some harm to national security, SECRET = exceptional harm to national security
     * (Required)
     * 
     */
    @JsonProperty("label")
    public SecurityClass.Label getLabel() {
        return label;
    }

    /**
     * level of classification - UNCLASS = no harm to national security, CUI = possible disclosure to persons without need to know, CONFIDENTIAL = Some harm to national security, SECRET = exceptional harm to national security
     * (Required)
     * 
     */
    @JsonProperty("label")
    public void setLabel(SecurityClass.Label label) {
        this.label = label;
    }

    /**
     * what source is the classification from
     * 
     */
    @JsonProperty("derivedBy")
    public String getDerivedBy() {
        return derivedBy;
    }

    /**
     * what source is the classification from
     * 
     */
    @JsonProperty("derivedBy")
    public void setDerivedBy(String derivedBy) {
        this.derivedBy = derivedBy;
    }

    /**
     * when does classification expire - normally 25 years from date
     * 
     */
    @JsonProperty("declassifyDate")
    public String getDeclassifyDate() {
        return declassifyDate;
    }

    /**
     * when does classification expire - normally 25 years from date
     * 
     */
    @JsonProperty("declassifyDate")
    public void setDeclassifyDate(String declassifyDate) {
        this.declassifyDate = declassifyDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SecurityClass.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("label");
        sb.append('=');
        sb.append(((this.label == null)?"<null>":this.label));
        sb.append(',');
        sb.append("derivedBy");
        sb.append('=');
        sb.append(((this.derivedBy == null)?"<null>":this.derivedBy));
        sb.append(',');
        sb.append("declassifyDate");
        sb.append('=');
        sb.append(((this.declassifyDate == null)?"<null>":this.declassifyDate));
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
        result = ((result* 31)+((this.declassifyDate == null)? 0 :this.declassifyDate.hashCode()));
        result = ((result* 31)+((this.label == null)? 0 :this.label.hashCode()));
        result = ((result* 31)+((this.derivedBy == null)? 0 :this.derivedBy.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityClass) == false) {
            return false;
        }
        SecurityClass rhs = ((SecurityClass) other);
        return ((((this.declassifyDate == rhs.declassifyDate)||((this.declassifyDate!= null)&&this.declassifyDate.equals(rhs.declassifyDate)))&&((this.label == rhs.label)||((this.label!= null)&&this.label.equals(rhs.label))))&&((this.derivedBy == rhs.derivedBy)||((this.derivedBy!= null)&&this.derivedBy.equals(rhs.derivedBy))));
    }


    /**
     * level of classification - UNCLASS = no harm to national security, CUI = possible disclosure to persons without need to know, CONFIDENTIAL = Some harm to national security, SECRET = exceptional harm to national security
     * 
     */
    public enum Label {

        UNCLASSIFIED("UNCLASSIFIED"),
        CONTROLLED_UNCLASSIFIED("CONTROLLED UNCLASSIFIED"),
        CONFIDENTIAL("CONFIDENTIAL"),
        SECRET("SECRET");
        private final String value;
        private final static Map<String, SecurityClass.Label> CONSTANTS = new HashMap<String, SecurityClass.Label>();

        static {
            for (SecurityClass.Label c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Label(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static SecurityClass.Label fromValue(String value) {
            SecurityClass.Label constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
