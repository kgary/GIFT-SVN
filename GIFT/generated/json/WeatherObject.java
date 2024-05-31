
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

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "reportDate",
    "sunriseTime",
    "sunsetTime",
    "cloudCover",
    "visualRange",
    "nightVisionRange",
    "infraredRange",
    "laserRange",
    "wind",
    "precipitation",
    "precipType",
    "surfDustConcentration",
    "highTemp",
    "lowTemp",
    "humidity"
})
public class WeatherObject {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportDate")
    private String reportDate;
    /**
     * begin morning nautical time (BMNT)
     * (Required)
     * 
     */
    @JsonProperty("sunriseTime")
    @JsonPropertyDescription("begin morning nautical time (BMNT)")
    private String sunriseTime;
    /**
     * evening nautical time
     * (Required)
     * 
     */
    @JsonProperty("sunsetTime")
    @JsonPropertyDescription("evening nautical time")
    private String sunsetTime;
    @JsonProperty("cloudCover")
    private WeatherObject.CloudCover cloudCover;
    /**
     * 'Runway Visual Range' measured in meters (m)
     * (Required)
     * 
     */
    @JsonProperty("visualRange")
    @JsonPropertyDescription("'Runway Visual Range' measured in meters (m)")
    private Double visualRange;
    /**
     * NVG range measured in meters (m)
     * 
     */
    @JsonProperty("nightVisionRange")
    @JsonPropertyDescription("NVG range measured in meters (m)")
    private Double nightVisionRange;
    /**
     * FLIR range measured in meters (m)
     * 
     */
    @JsonProperty("infraredRange")
    @JsonPropertyDescription("FLIR range measured in meters (m)")
    private Double infraredRange;
    /**
     * measured in meters (m)
     * 
     */
    @JsonProperty("laserRange")
    @JsonPropertyDescription("measured in meters (m)")
    private Double laserRange;
    /**
     * measured in true azimuth and knots kts
     * (Required)
     * 
     */
    @JsonProperty("wind")
    @JsonPropertyDescription("measured in true azimuth and knots kts")
    private String wind;
    /**
     * modeled probability measured in percentage (%)
     * (Required)
     * 
     */
    @JsonProperty("precipitation")
    @JsonPropertyDescription("modeled probability measured in percentage (%)")
    private Integer precipitation;
    @JsonProperty("precipType")
    private WeatherObject.PrecipType precipType;
    /**
     * measured in milligrams per cubic meter (Mg/M3)
     * 
     */
    @JsonProperty("surfDustConcentration")
    @JsonPropertyDescription("measured in milligrams per cubic meter (Mg/M3)")
    private Double surfDustConcentration;
    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("highTemp")
    @JsonPropertyDescription("measured in celcius (c)")
    private HighTemp highTemp;
    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("lowTemp")
    @JsonPropertyDescription("measured in celcius (c)")
    private LowTemp lowTemp;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    private Integer humidity;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportDate")
    public String getReportDate() {
        return reportDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportDate")
    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * begin morning nautical time (BMNT)
     * (Required)
     * 
     */
    @JsonProperty("sunriseTime")
    public String getSunriseTime() {
        return sunriseTime;
    }

    /**
     * begin morning nautical time (BMNT)
     * (Required)
     * 
     */
    @JsonProperty("sunriseTime")
    public void setSunriseTime(String sunriseTime) {
        this.sunriseTime = sunriseTime;
    }

    /**
     * evening nautical time
     * (Required)
     * 
     */
    @JsonProperty("sunsetTime")
    public String getSunsetTime() {
        return sunsetTime;
    }

    /**
     * evening nautical time
     * (Required)
     * 
     */
    @JsonProperty("sunsetTime")
    public void setSunsetTime(String sunsetTime) {
        this.sunsetTime = sunsetTime;
    }

    @JsonProperty("cloudCover")
    public WeatherObject.CloudCover getCloudCover() {
        return cloudCover;
    }

    @JsonProperty("cloudCover")
    public void setCloudCover(WeatherObject.CloudCover cloudCover) {
        this.cloudCover = cloudCover;
    }

    /**
     * 'Runway Visual Range' measured in meters (m)
     * (Required)
     * 
     */
    @JsonProperty("visualRange")
    public Double getVisualRange() {
        return visualRange;
    }

    /**
     * 'Runway Visual Range' measured in meters (m)
     * (Required)
     * 
     */
    @JsonProperty("visualRange")
    public void setVisualRange(Double visualRange) {
        this.visualRange = visualRange;
    }

    /**
     * NVG range measured in meters (m)
     * 
     */
    @JsonProperty("nightVisionRange")
    public Double getNightVisionRange() {
        return nightVisionRange;
    }

    /**
     * NVG range measured in meters (m)
     * 
     */
    @JsonProperty("nightVisionRange")
    public void setNightVisionRange(Double nightVisionRange) {
        this.nightVisionRange = nightVisionRange;
    }

    /**
     * FLIR range measured in meters (m)
     * 
     */
    @JsonProperty("infraredRange")
    public Double getInfraredRange() {
        return infraredRange;
    }

    /**
     * FLIR range measured in meters (m)
     * 
     */
    @JsonProperty("infraredRange")
    public void setInfraredRange(Double infraredRange) {
        this.infraredRange = infraredRange;
    }

    /**
     * measured in meters (m)
     * 
     */
    @JsonProperty("laserRange")
    public Double getLaserRange() {
        return laserRange;
    }

    /**
     * measured in meters (m)
     * 
     */
    @JsonProperty("laserRange")
    public void setLaserRange(Double laserRange) {
        this.laserRange = laserRange;
    }

    /**
     * measured in true azimuth and knots kts
     * (Required)
     * 
     */
    @JsonProperty("wind")
    public String getWind() {
        return wind;
    }

    /**
     * measured in true azimuth and knots kts
     * (Required)
     * 
     */
    @JsonProperty("wind")
    public void setWind(String wind) {
        this.wind = wind;
    }

    /**
     * modeled probability measured in percentage (%)
     * (Required)
     * 
     */
    @JsonProperty("precipitation")
    public Integer getPrecipitation() {
        return precipitation;
    }

    /**
     * modeled probability measured in percentage (%)
     * (Required)
     * 
     */
    @JsonProperty("precipitation")
    public void setPrecipitation(Integer precipitation) {
        this.precipitation = precipitation;
    }

    @JsonProperty("precipType")
    public WeatherObject.PrecipType getPrecipType() {
        return precipType;
    }

    @JsonProperty("precipType")
    public void setPrecipType(WeatherObject.PrecipType precipType) {
        this.precipType = precipType;
    }

    /**
     * measured in milligrams per cubic meter (Mg/M3)
     * 
     */
    @JsonProperty("surfDustConcentration")
    public Double getSurfDustConcentration() {
        return surfDustConcentration;
    }

    /**
     * measured in milligrams per cubic meter (Mg/M3)
     * 
     */
    @JsonProperty("surfDustConcentration")
    public void setSurfDustConcentration(Double surfDustConcentration) {
        this.surfDustConcentration = surfDustConcentration;
    }

    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("highTemp")
    public HighTemp getHighTemp() {
        return highTemp;
    }

    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("highTemp")
    public void setHighTemp(HighTemp highTemp) {
        this.highTemp = highTemp;
    }

    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("lowTemp")
    public LowTemp getLowTemp() {
        return lowTemp;
    }

    /**
     * measured in celcius (c)
     * (Required)
     * 
     */
    @JsonProperty("lowTemp")
    public void setLowTemp(LowTemp lowTemp) {
        this.lowTemp = lowTemp;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(WeatherObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("reportDate");
        sb.append('=');
        sb.append(((this.reportDate == null)?"<null>":this.reportDate));
        sb.append(',');
        sb.append("sunriseTime");
        sb.append('=');
        sb.append(((this.sunriseTime == null)?"<null>":this.sunriseTime));
        sb.append(',');
        sb.append("sunsetTime");
        sb.append('=');
        sb.append(((this.sunsetTime == null)?"<null>":this.sunsetTime));
        sb.append(',');
        sb.append("cloudCover");
        sb.append('=');
        sb.append(((this.cloudCover == null)?"<null>":this.cloudCover));
        sb.append(',');
        sb.append("visualRange");
        sb.append('=');
        sb.append(((this.visualRange == null)?"<null>":this.visualRange));
        sb.append(',');
        sb.append("nightVisionRange");
        sb.append('=');
        sb.append(((this.nightVisionRange == null)?"<null>":this.nightVisionRange));
        sb.append(',');
        sb.append("infraredRange");
        sb.append('=');
        sb.append(((this.infraredRange == null)?"<null>":this.infraredRange));
        sb.append(',');
        sb.append("laserRange");
        sb.append('=');
        sb.append(((this.laserRange == null)?"<null>":this.laserRange));
        sb.append(',');
        sb.append("wind");
        sb.append('=');
        sb.append(((this.wind == null)?"<null>":this.wind));
        sb.append(',');
        sb.append("precipitation");
        sb.append('=');
        sb.append(((this.precipitation == null)?"<null>":this.precipitation));
        sb.append(',');
        sb.append("precipType");
        sb.append('=');
        sb.append(((this.precipType == null)?"<null>":this.precipType));
        sb.append(',');
        sb.append("surfDustConcentration");
        sb.append('=');
        sb.append(((this.surfDustConcentration == null)?"<null>":this.surfDustConcentration));
        sb.append(',');
        sb.append("highTemp");
        sb.append('=');
        sb.append(((this.highTemp == null)?"<null>":this.highTemp));
        sb.append(',');
        sb.append("lowTemp");
        sb.append('=');
        sb.append(((this.lowTemp == null)?"<null>":this.lowTemp));
        sb.append(',');
        sb.append("humidity");
        sb.append('=');
        sb.append(((this.humidity == null)?"<null>":this.humidity));
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
        result = ((result* 31)+((this.cloudCover == null)? 0 :this.cloudCover.hashCode()));
        result = ((result* 31)+((this.surfDustConcentration == null)? 0 :this.surfDustConcentration.hashCode()));
        result = ((result* 31)+((this.precipitation == null)? 0 :this.precipitation.hashCode()));
        result = ((result* 31)+((this.laserRange == null)? 0 :this.laserRange.hashCode()));
        result = ((result* 31)+((this.reportDate == null)? 0 :this.reportDate.hashCode()));
        result = ((result* 31)+((this.sunsetTime == null)? 0 :this.sunsetTime.hashCode()));
        result = ((result* 31)+((this.nightVisionRange == null)? 0 :this.nightVisionRange.hashCode()));
        result = ((result* 31)+((this.visualRange == null)? 0 :this.visualRange.hashCode()));
        result = ((result* 31)+((this.lowTemp == null)? 0 :this.lowTemp.hashCode()));
        result = ((result* 31)+((this.precipType == null)? 0 :this.precipType.hashCode()));
        result = ((result* 31)+((this.infraredRange == null)? 0 :this.infraredRange.hashCode()));
        result = ((result* 31)+((this.humidity == null)? 0 :this.humidity.hashCode()));
        result = ((result* 31)+((this.highTemp == null)? 0 :this.highTemp.hashCode()));
        result = ((result* 31)+((this.sunriseTime == null)? 0 :this.sunriseTime.hashCode()));
        result = ((result* 31)+((this.wind == null)? 0 :this.wind.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WeatherObject) == false) {
            return false;
        }
        WeatherObject rhs = ((WeatherObject) other);
        return ((((((((((((((((this.cloudCover == rhs.cloudCover)||((this.cloudCover!= null)&&this.cloudCover.equals(rhs.cloudCover)))&&((this.surfDustConcentration == rhs.surfDustConcentration)||((this.surfDustConcentration!= null)&&this.surfDustConcentration.equals(rhs.surfDustConcentration))))&&((this.precipitation == rhs.precipitation)||((this.precipitation!= null)&&this.precipitation.equals(rhs.precipitation))))&&((this.laserRange == rhs.laserRange)||((this.laserRange!= null)&&this.laserRange.equals(rhs.laserRange))))&&((this.reportDate == rhs.reportDate)||((this.reportDate!= null)&&this.reportDate.equals(rhs.reportDate))))&&((this.sunsetTime == rhs.sunsetTime)||((this.sunsetTime!= null)&&this.sunsetTime.equals(rhs.sunsetTime))))&&((this.nightVisionRange == rhs.nightVisionRange)||((this.nightVisionRange!= null)&&this.nightVisionRange.equals(rhs.nightVisionRange))))&&((this.visualRange == rhs.visualRange)||((this.visualRange!= null)&&this.visualRange.equals(rhs.visualRange))))&&((this.lowTemp == rhs.lowTemp)||((this.lowTemp!= null)&&this.lowTemp.equals(rhs.lowTemp))))&&((this.precipType == rhs.precipType)||((this.precipType!= null)&&this.precipType.equals(rhs.precipType))))&&((this.infraredRange == rhs.infraredRange)||((this.infraredRange!= null)&&this.infraredRange.equals(rhs.infraredRange))))&&((this.humidity == rhs.humidity)||((this.humidity!= null)&&this.humidity.equals(rhs.humidity))))&&((this.highTemp == rhs.highTemp)||((this.highTemp!= null)&&this.highTemp.equals(rhs.highTemp))))&&((this.sunriseTime == rhs.sunriseTime)||((this.sunriseTime!= null)&&this.sunriseTime.equals(rhs.sunriseTime))))&&((this.wind == rhs.wind)||((this.wind!= null)&&this.wind.equals(rhs.wind))));
    }

    public enum CloudCover {

        NONE("None"),
        PARTLY("Partly"),
        FULL("Full");
        private final String value;
        private final static Map<String, WeatherObject.CloudCover> CONSTANTS = new HashMap<String, WeatherObject.CloudCover>();

        static {
            for (WeatherObject.CloudCover c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CloudCover(String value) {
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
        public static WeatherObject.CloudCover fromValue(String value) {
            WeatherObject.CloudCover constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum PrecipType {

        NONE("None"),
        FOG("Fog"),
        DRIZZLE("Drizzle"),
        RAIN("Rain"),
        SLEET("Sleet"),
        FREEZING_RAIN("Freezing Rain"),
        SNOW("Snow");
        private final String value;
        private final static Map<String, WeatherObject.PrecipType> CONSTANTS = new HashMap<String, WeatherObject.PrecipType>();

        static {
            for (WeatherObject.PrecipType c: values()) {
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
        public static WeatherObject.PrecipType fromValue(String value) {
            WeatherObject.PrecipType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
