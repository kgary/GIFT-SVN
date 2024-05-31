
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "designation",
    "purpose",
    "system",
    "channel",
    "frequency",
    "endpoint",
    "port"
})
public class CommNet {

    /**
     * network designation
     * (Required)
     * 
     */
    @JsonProperty("designation")
    @JsonPropertyDescription("network designation")
    private String designation;
    /**
     * function of comm network
     * 
     */
    @JsonProperty("purpose")
    @JsonPropertyDescription("function of comm network")
    private String purpose;
    /**
     * system network is using or part of
     * 
     */
    @JsonProperty("system")
    @JsonPropertyDescription("system network is using or part of")
    private String system;
    /**
     * channel number being used (if any)
     * 
     */
    @JsonProperty("channel")
    @JsonPropertyDescription("channel number being used (if any)")
    private String channel;
    /**
     * frequency of channel or system being used
     * 
     */
    @JsonProperty("frequency")
    @JsonPropertyDescription("frequency of channel or system being used")
    private String frequency;
    /**
     * endpoint of network being connected to (ip or domain)
     * 
     */
    @JsonProperty("endpoint")
    @JsonPropertyDescription("endpoint of network being connected to (ip or domain)")
    private String endpoint;
    /**
     * port in endpoint being connected to - if any
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("port in endpoint being connected to - if any")
    private String port;

    /**
     * network designation
     * (Required)
     * 
     */
    @JsonProperty("designation")
    public String getDesignation() {
        return designation;
    }

    /**
     * network designation
     * (Required)
     * 
     */
    @JsonProperty("designation")
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    /**
     * function of comm network
     * 
     */
    @JsonProperty("purpose")
    public String getPurpose() {
        return purpose;
    }

    /**
     * function of comm network
     * 
     */
    @JsonProperty("purpose")
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * system network is using or part of
     * 
     */
    @JsonProperty("system")
    public String getSystem() {
        return system;
    }

    /**
     * system network is using or part of
     * 
     */
    @JsonProperty("system")
    public void setSystem(String system) {
        this.system = system;
    }

    /**
     * channel number being used (if any)
     * 
     */
    @JsonProperty("channel")
    public String getChannel() {
        return channel;
    }

    /**
     * channel number being used (if any)
     * 
     */
    @JsonProperty("channel")
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * frequency of channel or system being used
     * 
     */
    @JsonProperty("frequency")
    public String getFrequency() {
        return frequency;
    }

    /**
     * frequency of channel or system being used
     * 
     */
    @JsonProperty("frequency")
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /**
     * endpoint of network being connected to (ip or domain)
     * 
     */
    @JsonProperty("endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * endpoint of network being connected to (ip or domain)
     * 
     */
    @JsonProperty("endpoint")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * port in endpoint being connected to - if any
     * 
     */
    @JsonProperty("port")
    public String getPort() {
        return port;
    }

    /**
     * port in endpoint being connected to - if any
     * 
     */
    @JsonProperty("port")
    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CommNet.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("designation");
        sb.append('=');
        sb.append(((this.designation == null)?"<null>":this.designation));
        sb.append(',');
        sb.append("purpose");
        sb.append('=');
        sb.append(((this.purpose == null)?"<null>":this.purpose));
        sb.append(',');
        sb.append("system");
        sb.append('=');
        sb.append(((this.system == null)?"<null>":this.system));
        sb.append(',');
        sb.append("channel");
        sb.append('=');
        sb.append(((this.channel == null)?"<null>":this.channel));
        sb.append(',');
        sb.append("frequency");
        sb.append('=');
        sb.append(((this.frequency == null)?"<null>":this.frequency));
        sb.append(',');
        sb.append("endpoint");
        sb.append('=');
        sb.append(((this.endpoint == null)?"<null>":this.endpoint));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null)?"<null>":this.port));
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
        result = ((result* 31)+((this.endpoint == null)? 0 :this.endpoint.hashCode()));
        result = ((result* 31)+((this.system == null)? 0 :this.system.hashCode()));
        result = ((result* 31)+((this.purpose == null)? 0 :this.purpose.hashCode()));
        result = ((result* 31)+((this.port == null)? 0 :this.port.hashCode()));
        result = ((result* 31)+((this.channel == null)? 0 :this.channel.hashCode()));
        result = ((result* 31)+((this.designation == null)? 0 :this.designation.hashCode()));
        result = ((result* 31)+((this.frequency == null)? 0 :this.frequency.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CommNet) == false) {
            return false;
        }
        CommNet rhs = ((CommNet) other);
        return ((((((((this.endpoint == rhs.endpoint)||((this.endpoint!= null)&&this.endpoint.equals(rhs.endpoint)))&&((this.system == rhs.system)||((this.system!= null)&&this.system.equals(rhs.system))))&&((this.purpose == rhs.purpose)||((this.purpose!= null)&&this.purpose.equals(rhs.purpose))))&&((this.port == rhs.port)||((this.port!= null)&&this.port.equals(rhs.port))))&&((this.channel == rhs.channel)||((this.channel!= null)&&this.channel.equals(rhs.channel))))&&((this.designation == rhs.designation)||((this.designation!= null)&&this.designation.equals(rhs.designation))))&&((this.frequency == rhs.frequency)||((this.frequency!= null)&&this.frequency.equals(rhs.frequency))));
    }

}
