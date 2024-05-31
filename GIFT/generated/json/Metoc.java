
package generated.json;

import java.util.Date;
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
 * the real or synthetic environmental parameters for the exercise map, based on the real geographic location, altitude, season and time of day.  Ideally will draw from historical data and randomized air-pressure systems.  These variables can be set dynamically by rendering engine during design but must be based on modeled data from other settings like location, season, sun or moon data, etc.  Can include other non-natural elements such as smoke and CBRN levels.  Can get much of this data from sites like http://www.worldweatheronline.com
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dataTime",
    "magneticDeclination",
    "atmosphere",
    "cloudCover",
    "icing",
    "light",
    "precipitation",
    "visibilityRange",
    "windDirection",
    "windSpeed"
})
public class Metoc {

    /**
     * required: date (season) at exercise start
     * (Required)
     * 
     */
    @JsonProperty("dataTime")
    @JsonPropertyDescription("required: date (season) at exercise start")
    private Date dataTime;
    /**
     * optional: magnetic offset of magnetic true north
     * 
     */
    @JsonProperty("magneticDeclination")
    @JsonPropertyDescription("optional: magnetic offset of magnetic true north")
    private Double magneticDeclination;
    /**
     * atmospheric moisture in percent of atmospheric capacity
     * (Required)
     * 
     */
    @JsonProperty("atmosphere")
    @JsonPropertyDescription("atmospheric moisture in percent of atmospheric capacity")
    private Atmosphere atmosphere;
    /**
     * density of clouds
     * 
     */
    @JsonProperty("cloudCover")
    @JsonPropertyDescription("density of clouds")
    private Object cloudCover;
    @JsonProperty("icing")
    private Metoc.Icing icing;
    /**
     * degree of light in environment that impacts visibility
     * (Required)
     * 
     */
    @JsonProperty("light")
    @JsonPropertyDescription("degree of light in environment that impacts visibility")
    private Light light;
    /**
     * optional: type of precipitation
     * 
     */
    @JsonProperty("precipitation")
    @JsonPropertyDescription("optional: type of precipitation")
    private Precipitation precipitation;
    /**
     * range / ability to identify an object measured in meters
     * (Required)
     * 
     */
    @JsonProperty("visibilityRange")
    @JsonPropertyDescription("range / ability to identify an object measured in meters")
    private Integer visibilityRange;
    /**
     * required: direction wind is blowing on average - measured in degrees
     * (Required)
     * 
     */
    @JsonProperty("windDirection")
    @JsonPropertyDescription("required: direction wind is blowing on average - measured in degrees")
    private Integer windDirection;
    /**
     * required: average speed of wind - measured in meters/sec
     * (Required)
     * 
     */
    @JsonProperty("windSpeed")
    @JsonPropertyDescription("required: average speed of wind - measured in meters/sec")
    private Integer windSpeed;

    /**
     * required: date (season) at exercise start
     * (Required)
     * 
     */
    @JsonProperty("dataTime")
    public Date getDataTime() {
        return dataTime;
    }

    /**
     * required: date (season) at exercise start
     * (Required)
     * 
     */
    @JsonProperty("dataTime")
    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    /**
     * optional: magnetic offset of magnetic true north
     * 
     */
    @JsonProperty("magneticDeclination")
    public Double getMagneticDeclination() {
        return magneticDeclination;
    }

    /**
     * optional: magnetic offset of magnetic true north
     * 
     */
    @JsonProperty("magneticDeclination")
    public void setMagneticDeclination(Double magneticDeclination) {
        this.magneticDeclination = magneticDeclination;
    }

    /**
     * atmospheric moisture in percent of atmospheric capacity
     * (Required)
     * 
     */
    @JsonProperty("atmosphere")
    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    /**
     * atmospheric moisture in percent of atmospheric capacity
     * (Required)
     * 
     */
    @JsonProperty("atmosphere")
    public void setAtmosphere(Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    /**
     * density of clouds
     * 
     */
    @JsonProperty("cloudCover")
    public Object getCloudCover() {
        return cloudCover;
    }

    /**
     * density of clouds
     * 
     */
    @JsonProperty("cloudCover")
    public void setCloudCover(Object cloudCover) {
        this.cloudCover = cloudCover;
    }

    @JsonProperty("icing")
    public Metoc.Icing getIcing() {
        return icing;
    }

    @JsonProperty("icing")
    public void setIcing(Metoc.Icing icing) {
        this.icing = icing;
    }

    /**
     * degree of light in environment that impacts visibility
     * (Required)
     * 
     */
    @JsonProperty("light")
    public Light getLight() {
        return light;
    }

    /**
     * degree of light in environment that impacts visibility
     * (Required)
     * 
     */
    @JsonProperty("light")
    public void setLight(Light light) {
        this.light = light;
    }

    /**
     * optional: type of precipitation
     * 
     */
    @JsonProperty("precipitation")
    public Precipitation getPrecipitation() {
        return precipitation;
    }

    /**
     * optional: type of precipitation
     * 
     */
    @JsonProperty("precipitation")
    public void setPrecipitation(Precipitation precipitation) {
        this.precipitation = precipitation;
    }

    /**
     * range / ability to identify an object measured in meters
     * (Required)
     * 
     */
    @JsonProperty("visibilityRange")
    public Integer getVisibilityRange() {
        return visibilityRange;
    }

    /**
     * range / ability to identify an object measured in meters
     * (Required)
     * 
     */
    @JsonProperty("visibilityRange")
    public void setVisibilityRange(Integer visibilityRange) {
        this.visibilityRange = visibilityRange;
    }

    /**
     * required: direction wind is blowing on average - measured in degrees
     * (Required)
     * 
     */
    @JsonProperty("windDirection")
    public Integer getWindDirection() {
        return windDirection;
    }

    /**
     * required: direction wind is blowing on average - measured in degrees
     * (Required)
     * 
     */
    @JsonProperty("windDirection")
    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    /**
     * required: average speed of wind - measured in meters/sec
     * (Required)
     * 
     */
    @JsonProperty("windSpeed")
    public Integer getWindSpeed() {
        return windSpeed;
    }

    /**
     * required: average speed of wind - measured in meters/sec
     * (Required)
     * 
     */
    @JsonProperty("windSpeed")
    public void setWindSpeed(Integer windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Metoc.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dataTime");
        sb.append('=');
        sb.append(((this.dataTime == null)?"<null>":this.dataTime));
        sb.append(',');
        sb.append("magneticDeclination");
        sb.append('=');
        sb.append(((this.magneticDeclination == null)?"<null>":this.magneticDeclination));
        sb.append(',');
        sb.append("atmosphere");
        sb.append('=');
        sb.append(((this.atmosphere == null)?"<null>":this.atmosphere));
        sb.append(',');
        sb.append("cloudCover");
        sb.append('=');
        sb.append(((this.cloudCover == null)?"<null>":this.cloudCover));
        sb.append(',');
        sb.append("icing");
        sb.append('=');
        sb.append(((this.icing == null)?"<null>":this.icing));
        sb.append(',');
        sb.append("light");
        sb.append('=');
        sb.append(((this.light == null)?"<null>":this.light));
        sb.append(',');
        sb.append("precipitation");
        sb.append('=');
        sb.append(((this.precipitation == null)?"<null>":this.precipitation));
        sb.append(',');
        sb.append("visibilityRange");
        sb.append('=');
        sb.append(((this.visibilityRange == null)?"<null>":this.visibilityRange));
        sb.append(',');
        sb.append("windDirection");
        sb.append('=');
        sb.append(((this.windDirection == null)?"<null>":this.windDirection));
        sb.append(',');
        sb.append("windSpeed");
        sb.append('=');
        sb.append(((this.windSpeed == null)?"<null>":this.windSpeed));
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
        result = ((result* 31)+((this.atmosphere == null)? 0 :this.atmosphere.hashCode()));
        result = ((result* 31)+((this.precipitation == null)? 0 :this.precipitation.hashCode()));
        result = ((result* 31)+((this.visibilityRange == null)? 0 :this.visibilityRange.hashCode()));
        result = ((result* 31)+((this.dataTime == null)? 0 :this.dataTime.hashCode()));
        result = ((result* 31)+((this.light == null)? 0 :this.light.hashCode()));
        result = ((result* 31)+((this.cloudCover == null)? 0 :this.cloudCover.hashCode()));
        result = ((result* 31)+((this.magneticDeclination == null)? 0 :this.magneticDeclination.hashCode()));
        result = ((result* 31)+((this.windDirection == null)? 0 :this.windDirection.hashCode()));
        result = ((result* 31)+((this.windSpeed == null)? 0 :this.windSpeed.hashCode()));
        result = ((result* 31)+((this.icing == null)? 0 :this.icing.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Metoc) == false) {
            return false;
        }
        Metoc rhs = ((Metoc) other);
        return (((((((((((this.atmosphere == rhs.atmosphere)||((this.atmosphere!= null)&&this.atmosphere.equals(rhs.atmosphere)))&&((this.precipitation == rhs.precipitation)||((this.precipitation!= null)&&this.precipitation.equals(rhs.precipitation))))&&((this.visibilityRange == rhs.visibilityRange)||((this.visibilityRange!= null)&&this.visibilityRange.equals(rhs.visibilityRange))))&&((this.dataTime == rhs.dataTime)||((this.dataTime!= null)&&this.dataTime.equals(rhs.dataTime))))&&((this.light == rhs.light)||((this.light!= null)&&this.light.equals(rhs.light))))&&((this.cloudCover == rhs.cloudCover)||((this.cloudCover!= null)&&this.cloudCover.equals(rhs.cloudCover))))&&((this.magneticDeclination == rhs.magneticDeclination)||((this.magneticDeclination!= null)&&this.magneticDeclination.equals(rhs.magneticDeclination))))&&((this.windDirection == rhs.windDirection)||((this.windDirection!= null)&&this.windDirection.equals(rhs.windDirection))))&&((this.windSpeed == rhs.windSpeed)||((this.windSpeed!= null)&&this.windSpeed.equals(rhs.windSpeed))))&&((this.icing == rhs.icing)||((this.icing!= null)&&this.icing.equals(rhs.icing))));
    }

    public enum Icing {

        NONE("none"),
        LIGHT("light"),
        MODERATE("moderate"),
        SEVERE("severe");
        private final String value;
        private final static Map<String, Metoc.Icing> CONSTANTS = new HashMap<String, Metoc.Icing>();

        static {
            for (Metoc.Icing c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Icing(String value) {
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
        public static Metoc.Icing fromValue(String value) {
            Metoc.Icing constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
