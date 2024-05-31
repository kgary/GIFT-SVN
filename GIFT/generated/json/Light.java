
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
 * degree of light in environment that impacts visibility
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lightType",
    "moonCategory",
    "sunrise",
    "sunset",
    "moonrise",
    "moonset"
})
public class Light {

    /**
     * optional: height layer of clouds
     * (Required)
     * 
     */
    @JsonProperty("lightType")
    @JsonPropertyDescription("optional: height layer of clouds")
    private Light.LightType lightType;
    /**
     * optional: moon shape
     * 
     */
    @JsonProperty("moonCategory")
    @JsonPropertyDescription("optional: moon shape")
    private Light.MoonCategory moonCategory;
    /**
     * optional: map location sunrise time
     * 
     */
    @JsonProperty("sunrise")
    @JsonPropertyDescription("optional: map location sunrise time")
    private String sunrise;
    /**
     * optional: map location sunset time
     * 
     */
    @JsonProperty("sunset")
    @JsonPropertyDescription("optional: map location sunset time")
    private String sunset;
    /**
     * optional: map location moonrise time
     * 
     */
    @JsonProperty("moonrise")
    @JsonPropertyDescription("optional: map location moonrise time")
    private String moonrise;
    /**
     * optional: map location moonset time
     * 
     */
    @JsonProperty("moonset")
    @JsonPropertyDescription("optional: map location moonset time")
    private String moonset;

    /**
     * optional: height layer of clouds
     * (Required)
     * 
     */
    @JsonProperty("lightType")
    public Light.LightType getLightType() {
        return lightType;
    }

    /**
     * optional: height layer of clouds
     * (Required)
     * 
     */
    @JsonProperty("lightType")
    public void setLightType(Light.LightType lightType) {
        this.lightType = lightType;
    }

    /**
     * optional: moon shape
     * 
     */
    @JsonProperty("moonCategory")
    public Light.MoonCategory getMoonCategory() {
        return moonCategory;
    }

    /**
     * optional: moon shape
     * 
     */
    @JsonProperty("moonCategory")
    public void setMoonCategory(Light.MoonCategory moonCategory) {
        this.moonCategory = moonCategory;
    }

    /**
     * optional: map location sunrise time
     * 
     */
    @JsonProperty("sunrise")
    public String getSunrise() {
        return sunrise;
    }

    /**
     * optional: map location sunrise time
     * 
     */
    @JsonProperty("sunrise")
    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * optional: map location sunset time
     * 
     */
    @JsonProperty("sunset")
    public String getSunset() {
        return sunset;
    }

    /**
     * optional: map location sunset time
     * 
     */
    @JsonProperty("sunset")
    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    /**
     * optional: map location moonrise time
     * 
     */
    @JsonProperty("moonrise")
    public String getMoonrise() {
        return moonrise;
    }

    /**
     * optional: map location moonrise time
     * 
     */
    @JsonProperty("moonrise")
    public void setMoonrise(String moonrise) {
        this.moonrise = moonrise;
    }

    /**
     * optional: map location moonset time
     * 
     */
    @JsonProperty("moonset")
    public String getMoonset() {
        return moonset;
    }

    /**
     * optional: map location moonset time
     * 
     */
    @JsonProperty("moonset")
    public void setMoonset(String moonset) {
        this.moonset = moonset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Light.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lightType");
        sb.append('=');
        sb.append(((this.lightType == null)?"<null>":this.lightType));
        sb.append(',');
        sb.append("moonCategory");
        sb.append('=');
        sb.append(((this.moonCategory == null)?"<null>":this.moonCategory));
        sb.append(',');
        sb.append("sunrise");
        sb.append('=');
        sb.append(((this.sunrise == null)?"<null>":this.sunrise));
        sb.append(',');
        sb.append("sunset");
        sb.append('=');
        sb.append(((this.sunset == null)?"<null>":this.sunset));
        sb.append(',');
        sb.append("moonrise");
        sb.append('=');
        sb.append(((this.moonrise == null)?"<null>":this.moonrise));
        sb.append(',');
        sb.append("moonset");
        sb.append('=');
        sb.append(((this.moonset == null)?"<null>":this.moonset));
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
        result = ((result* 31)+((this.moonset == null)? 0 :this.moonset.hashCode()));
        result = ((result* 31)+((this.sunrise == null)? 0 :this.sunrise.hashCode()));
        result = ((result* 31)+((this.moonCategory == null)? 0 :this.moonCategory.hashCode()));
        result = ((result* 31)+((this.sunset == null)? 0 :this.sunset.hashCode()));
        result = ((result* 31)+((this.moonrise == null)? 0 :this.moonrise.hashCode()));
        result = ((result* 31)+((this.lightType == null)? 0 :this.lightType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Light) == false) {
            return false;
        }
        Light rhs = ((Light) other);
        return (((((((this.moonset == rhs.moonset)||((this.moonset!= null)&&this.moonset.equals(rhs.moonset)))&&((this.sunrise == rhs.sunrise)||((this.sunrise!= null)&&this.sunrise.equals(rhs.sunrise))))&&((this.moonCategory == rhs.moonCategory)||((this.moonCategory!= null)&&this.moonCategory.equals(rhs.moonCategory))))&&((this.sunset == rhs.sunset)||((this.sunset!= null)&&this.sunset.equals(rhs.sunset))))&&((this.moonrise == rhs.moonrise)||((this.moonrise!= null)&&this.moonrise.equals(rhs.moonrise))))&&((this.lightType == rhs.lightType)||((this.lightType!= null)&&this.lightType.equals(rhs.lightType))));
    }


    /**
     * optional: height layer of clouds
     * 
     */
    public enum LightType {

        SUN("sun"),
        MOON("moon"),
        URBAN("urban");
        private final String value;
        private final static Map<String, Light.LightType> CONSTANTS = new HashMap<String, Light.LightType>();

        static {
            for (Light.LightType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        LightType(String value) {
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
        public static Light.LightType fromValue(String value) {
            Light.LightType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: moon shape
     * 
     */
    public enum MoonCategory {

        FULL("full"),
        NEW("new"),
        HALF("half"),
        QUARTER("quarter");
        private final String value;
        private final static Map<String, Light.MoonCategory> CONSTANTS = new HashMap<String, Light.MoonCategory>();

        static {
            for (Light.MoonCategory c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MoonCategory(String value) {
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
        public static Light.MoonCategory fromValue(String value) {
            Light.MoonCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
