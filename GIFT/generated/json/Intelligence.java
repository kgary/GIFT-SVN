
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "weather",
    "opposingForces",
    "civilConsiderations"
})
public class Intelligence {

    /**
     * Option: describes aspects of weather that could impact operations
     * (Required)
     * 
     */
    @JsonProperty("weather")
    @JsonPropertyDescription("Option: describes aspects of weather that could impact operations")
    private List<WeatherObject> weather = new ArrayList<WeatherObject>();
    /**
     * Required: describes enemy type, size, disposition
     * (Required)
     * 
     */
    @JsonProperty("opposingForces")
    @JsonPropertyDescription("Required: describes enemy type, size, disposition")
    private OpposingForces opposingForces;
    /**
     * option: describes civil factors - if any/known
     * (Required)
     * 
     */
    @JsonProperty("civilConsiderations")
    @JsonPropertyDescription("option: describes civil factors - if any/known")
    private CivilConsiderations civilConsiderations;

    /**
     * Option: describes aspects of weather that could impact operations
     * (Required)
     * 
     */
    @JsonProperty("weather")
    public List<WeatherObject> getWeather() {
        return weather;
    }

    /**
     * Option: describes aspects of weather that could impact operations
     * (Required)
     * 
     */
    @JsonProperty("weather")
    public void setWeather(List<WeatherObject> weather) {
        this.weather = weather;
    }

    /**
     * Required: describes enemy type, size, disposition
     * (Required)
     * 
     */
    @JsonProperty("opposingForces")
    public OpposingForces getOpposingForces() {
        return opposingForces;
    }

    /**
     * Required: describes enemy type, size, disposition
     * (Required)
     * 
     */
    @JsonProperty("opposingForces")
    public void setOpposingForces(OpposingForces opposingForces) {
        this.opposingForces = opposingForces;
    }

    /**
     * option: describes civil factors - if any/known
     * (Required)
     * 
     */
    @JsonProperty("civilConsiderations")
    public CivilConsiderations getCivilConsiderations() {
        return civilConsiderations;
    }

    /**
     * option: describes civil factors - if any/known
     * (Required)
     * 
     */
    @JsonProperty("civilConsiderations")
    public void setCivilConsiderations(CivilConsiderations civilConsiderations) {
        this.civilConsiderations = civilConsiderations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Intelligence.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("weather");
        sb.append('=');
        sb.append(((this.weather == null)?"<null>":this.weather));
        sb.append(',');
        sb.append("opposingForces");
        sb.append('=');
        sb.append(((this.opposingForces == null)?"<null>":this.opposingForces));
        sb.append(',');
        sb.append("civilConsiderations");
        sb.append('=');
        sb.append(((this.civilConsiderations == null)?"<null>":this.civilConsiderations));
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
        result = ((result* 31)+((this.weather == null)? 0 :this.weather.hashCode()));
        result = ((result* 31)+((this.opposingForces == null)? 0 :this.opposingForces.hashCode()));
        result = ((result* 31)+((this.civilConsiderations == null)? 0 :this.civilConsiderations.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Intelligence) == false) {
            return false;
        }
        Intelligence rhs = ((Intelligence) other);
        return ((((this.weather == rhs.weather)||((this.weather!= null)&&this.weather.equals(rhs.weather)))&&((this.opposingForces == rhs.opposingForces)||((this.opposingForces!= null)&&this.opposingForces.equals(rhs.opposingForces))))&&((this.civilConsiderations == rhs.civilConsiderations)||((this.civilConsiderations!= null)&&this.civilConsiderations.equals(rhs.civilConsiderations))));
    }

}
