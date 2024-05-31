
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * atmospheric moisture in percent of atmospheric capacity
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "temperature",
    "humidity",
    "baroPressure",
    "inversionLayer",
    "cbrn"
})
public class Atmosphere {

    /**
     * required: celsius
     * (Required)
     * 
     */
    @JsonProperty("temperature")
    @JsonPropertyDescription("required: celsius")
    private Double temperature;
    /**
     * optional: ratio
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    @JsonPropertyDescription("optional: ratio")
    private Integer humidity;
    /**
     * optional: millibars
     * (Required)
     * 
     */
    @JsonProperty("baroPressure")
    @JsonPropertyDescription("optional: millibars")
    private Integer baroPressure;
    /**
     * optional: height in meters
     * 
     */
    @JsonProperty("inversionLayer")
    @JsonPropertyDescription("optional: height in meters")
    private Integer inversionLayer;
    /**
     * required: if chemical, biologic, radiation, nuclear contamination is in the atmospher
     * 
     */
    @JsonProperty("cbrn")
    @JsonPropertyDescription("required: if chemical, biologic, radiation, nuclear contamination is in the atmospher")
    private Boolean cbrn = false;

    /**
     * required: celsius
     * (Required)
     * 
     */
    @JsonProperty("temperature")
    public Double getTemperature() {
        return temperature;
    }

    /**
     * required: celsius
     * (Required)
     * 
     */
    @JsonProperty("temperature")
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * optional: ratio
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * optional: ratio
     * (Required)
     * 
     */
    @JsonProperty("humidity")
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * optional: millibars
     * (Required)
     * 
     */
    @JsonProperty("baroPressure")
    public Integer getBaroPressure() {
        return baroPressure;
    }

    /**
     * optional: millibars
     * (Required)
     * 
     */
    @JsonProperty("baroPressure")
    public void setBaroPressure(Integer baroPressure) {
        this.baroPressure = baroPressure;
    }

    /**
     * optional: height in meters
     * 
     */
    @JsonProperty("inversionLayer")
    public Integer getInversionLayer() {
        return inversionLayer;
    }

    /**
     * optional: height in meters
     * 
     */
    @JsonProperty("inversionLayer")
    public void setInversionLayer(Integer inversionLayer) {
        this.inversionLayer = inversionLayer;
    }

    /**
     * required: if chemical, biologic, radiation, nuclear contamination is in the atmospher
     * 
     */
    @JsonProperty("cbrn")
    public Boolean getCbrn() {
        return cbrn;
    }

    /**
     * required: if chemical, biologic, radiation, nuclear contamination is in the atmospher
     * 
     */
    @JsonProperty("cbrn")
    public void setCbrn(Boolean cbrn) {
        this.cbrn = cbrn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Atmosphere.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("temperature");
        sb.append('=');
        sb.append(((this.temperature == null)?"<null>":this.temperature));
        sb.append(',');
        sb.append("humidity");
        sb.append('=');
        sb.append(((this.humidity == null)?"<null>":this.humidity));
        sb.append(',');
        sb.append("baroPressure");
        sb.append('=');
        sb.append(((this.baroPressure == null)?"<null>":this.baroPressure));
        sb.append(',');
        sb.append("inversionLayer");
        sb.append('=');
        sb.append(((this.inversionLayer == null)?"<null>":this.inversionLayer));
        sb.append(',');
        sb.append("cbrn");
        sb.append('=');
        sb.append(((this.cbrn == null)?"<null>":this.cbrn));
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
        result = ((result* 31)+((this.temperature == null)? 0 :this.temperature.hashCode()));
        result = ((result* 31)+((this.humidity == null)? 0 :this.humidity.hashCode()));
        result = ((result* 31)+((this.inversionLayer == null)? 0 :this.inversionLayer.hashCode()));
        result = ((result* 31)+((this.baroPressure == null)? 0 :this.baroPressure.hashCode()));
        result = ((result* 31)+((this.cbrn == null)? 0 :this.cbrn.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Atmosphere) == false) {
            return false;
        }
        Atmosphere rhs = ((Atmosphere) other);
        return ((((((this.temperature == rhs.temperature)||((this.temperature!= null)&&this.temperature.equals(rhs.temperature)))&&((this.humidity == rhs.humidity)||((this.humidity!= null)&&this.humidity.equals(rhs.humidity))))&&((this.inversionLayer == rhs.inversionLayer)||((this.inversionLayer!= null)&&this.inversionLayer.equals(rhs.inversionLayer))))&&((this.baroPressure == rhs.baroPressure)||((this.baroPressure!= null)&&this.baroPressure.equals(rhs.baroPressure))))&&((this.cbrn == rhs.cbrn)||((this.cbrn!= null)&&this.cbrn.equals(rhs.cbrn))));
    }

}
