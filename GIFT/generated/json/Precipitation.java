
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
 * optional: type of precipitation
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "precipType",
    "precipRate"
})
public class Precipitation {

    /**
     * required: type of precipitation
     * (Required)
     * 
     */
    @JsonProperty("precipType")
    @JsonPropertyDescription("required: type of precipitation")
    private Precipitation.PrecipType precipType;
    /**
     * required: rate of precipitation in millimeters/hour
     * 
     */
    @JsonProperty("precipRate")
    @JsonPropertyDescription("required: rate of precipitation in millimeters/hour")
    private Integer precipRate;

    /**
     * required: type of precipitation
     * (Required)
     * 
     */
    @JsonProperty("precipType")
    public Precipitation.PrecipType getPrecipType() {
        return precipType;
    }

    /**
     * required: type of precipitation
     * (Required)
     * 
     */
    @JsonProperty("precipType")
    public void setPrecipType(Precipitation.PrecipType precipType) {
        this.precipType = precipType;
    }

    /**
     * required: rate of precipitation in millimeters/hour
     * 
     */
    @JsonProperty("precipRate")
    public Integer getPrecipRate() {
        return precipRate;
    }

    /**
     * required: rate of precipitation in millimeters/hour
     * 
     */
    @JsonProperty("precipRate")
    public void setPrecipRate(Integer precipRate) {
        this.precipRate = precipRate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Precipitation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("precipType");
        sb.append('=');
        sb.append(((this.precipType == null)?"<null>":this.precipType));
        sb.append(',');
        sb.append("precipRate");
        sb.append('=');
        sb.append(((this.precipRate == null)?"<null>":this.precipRate));
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
        result = ((result* 31)+((this.precipType == null)? 0 :this.precipType.hashCode()));
        result = ((result* 31)+((this.precipRate == null)? 0 :this.precipRate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Precipitation) == false) {
            return false;
        }
        Precipitation rhs = ((Precipitation) other);
        return (((this.precipType == rhs.precipType)||((this.precipType!= null)&&this.precipType.equals(rhs.precipType)))&&((this.precipRate == rhs.precipRate)||((this.precipRate!= null)&&this.precipRate.equals(rhs.precipRate))));
    }


    /**
     * required: type of precipitation
     * 
     */
    public enum PrecipType {

        NONE("none"),
        RAIN("rain"),
        DRIZLE("drizle"),
        FREEZE_RAIN("freeze rain"),
        HAIL("hail"),
        SLEET("sleet"),
        SNOW("snow");
        private final String value;
        private final static Map<String, Precipitation.PrecipType> CONSTANTS = new HashMap<String, Precipitation.PrecipType>();

        static {
            for (Precipitation.PrecipType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        PrecipType(String value) {
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
        public static Precipitation.PrecipType fromValue(String value) {
            Precipitation.PrecipType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
