
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * a wound will be randomly determined based on the weapon, the body area it collides with.  This will produce different blood loss rates, performance limitations, and require different first-aid tasks to perform before blood loss and shock occurs.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "woundType",
    "location",
    "severity",
    "damageValue"
})
public class Wound {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("woundType")
    private String woundType;
    @JsonProperty("location")
    private Wound.Location location;
    @JsonProperty("severity")
    private Wound.Severity severity;
    @JsonProperty("damageValue")
    private Double damageValue;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("woundType")
    public String getWoundType() {
        return woundType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("woundType")
    public void setWoundType(String woundType) {
        this.woundType = woundType;
    }

    @JsonProperty("location")
    public Wound.Location getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Wound.Location location) {
        this.location = location;
    }

    @JsonProperty("severity")
    public Wound.Severity getSeverity() {
        return severity;
    }

    @JsonProperty("severity")
    public void setSeverity(Wound.Severity severity) {
        this.severity = severity;
    }

    @JsonProperty("damageValue")
    public Double getDamageValue() {
        return damageValue;
    }

    @JsonProperty("damageValue")
    public void setDamageValue(Double damageValue) {
        this.damageValue = damageValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Wound.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("woundType");
        sb.append('=');
        sb.append(((this.woundType == null)?"<null>":this.woundType));
        sb.append(',');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("severity");
        sb.append('=');
        sb.append(((this.severity == null)?"<null>":this.severity));
        sb.append(',');
        sb.append("damageValue");
        sb.append('=');
        sb.append(((this.damageValue == null)?"<null>":this.damageValue));
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
        result = ((result* 31)+((this.severity == null)? 0 :this.severity.hashCode()));
        result = ((result* 31)+((this.damageValue == null)? 0 :this.damageValue.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.woundType == null)? 0 :this.woundType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Wound) == false) {
            return false;
        }
        Wound rhs = ((Wound) other);
        return (((((this.severity == rhs.severity)||((this.severity!= null)&&this.severity.equals(rhs.severity)))&&((this.damageValue == rhs.damageValue)||((this.damageValue!= null)&&this.damageValue.equals(rhs.damageValue))))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.woundType == rhs.woundType)||((this.woundType!= null)&&this.woundType.equals(rhs.woundType))));
    }

    public enum Location {

        _1_HEAD("1-head"),
        _2_NECK("2-neck"),
        _3_TORSO("3-torso"),
        _4_LEFT_ARM("4-left-arm"),
        _5_RIGHT_ARM("5-right-arm"),
        _6_LEFT_LEG("6-left-leg"),
        _7_RIGHT_LEG("7-right-leg"),
        _8_LEFT_HAND("8-left-hand"),
        _9_RIGHT_HAND("9-right-hand"),
        _10_LEFT_FOOT("10-left-foot"),
        _11_RIGHT_FOOT("11-right-foot");
        private final String value;
        private final static Map<String, Wound.Location> CONSTANTS = new HashMap<String, Wound.Location>();

        static {
            for (Wound.Location c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Location(String value) {
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
        public static Wound.Location fromValue(String value) {
            Wound.Location constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum Severity {

        _1_PUNCTURED_ARTERY("1-punctured artery"),
        _2_PUNCTURED_NON_ARTERY("2-punctured non-artery"),
        _3_SUCKING_CHEST("3-sucking chest"),
        _4_CONCUSSION("4-concussion"),
        _5_CLEAN_BRAKE("5-clean-brake"),
        _6_COMPOUND_BRAKE("6-compound-brake"),
        _7_SEVERED("7-severed");
        private final String value;
        private final static Map<String, Wound.Severity> CONSTANTS = new HashMap<String, Wound.Severity>();

        static {
            for (Wound.Severity c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Severity(String value) {
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
        public static Wound.Severity fromValue(String value) {
            Wound.Severity constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
