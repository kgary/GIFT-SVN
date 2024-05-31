
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * optional: a pre defined xAPI format to be used with variables
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "actor",
    "verb",
    "object",
    "result",
    "context",
    "timestamp",
    "stored",
    "authority",
    "version"
})
public class MsrReport {

    @JsonProperty("id")
    private String id;
    @JsonProperty("actor")
    private String actor;
    @JsonProperty("verb")
    private String verb;
    @JsonProperty("object")
    private String object;
    @JsonProperty("result")
    private String result;
    @JsonProperty("context")
    private String context;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("stored")
    private String stored;
    @JsonProperty("authority")
    private String authority;
    @JsonProperty("version")
    private Double version;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("actor")
    public String getActor() {
        return actor;
    }

    @JsonProperty("actor")
    public void setActor(String actor) {
        this.actor = actor;
    }

    @JsonProperty("verb")
    public String getVerb() {
        return verb;
    }

    @JsonProperty("verb")
    public void setVerb(String verb) {
        this.verb = verb;
    }

    @JsonProperty("object")
    public String getObject() {
        return object;
    }

    @JsonProperty("object")
    public void setObject(String object) {
        this.object = object;
    }

    @JsonProperty("result")
    public String getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(String result) {
        this.result = result;
    }

    @JsonProperty("context")
    public String getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(String context) {
        this.context = context;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("stored")
    public String getStored() {
        return stored;
    }

    @JsonProperty("stored")
    public void setStored(String stored) {
        this.stored = stored;
    }

    @JsonProperty("authority")
    public String getAuthority() {
        return authority;
    }

    @JsonProperty("authority")
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @JsonProperty("version")
    public Double getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Double version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MsrReport.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("actor");
        sb.append('=');
        sb.append(((this.actor == null)?"<null>":this.actor));
        sb.append(',');
        sb.append("verb");
        sb.append('=');
        sb.append(((this.verb == null)?"<null>":this.verb));
        sb.append(',');
        sb.append("object");
        sb.append('=');
        sb.append(((this.object == null)?"<null>":this.object));
        sb.append(',');
        sb.append("result");
        sb.append('=');
        sb.append(((this.result == null)?"<null>":this.result));
        sb.append(',');
        sb.append("context");
        sb.append('=');
        sb.append(((this.context == null)?"<null>":this.context));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("stored");
        sb.append('=');
        sb.append(((this.stored == null)?"<null>":this.stored));
        sb.append(',');
        sb.append("authority");
        sb.append('=');
        sb.append(((this.authority == null)?"<null>":this.authority));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
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
        result = ((result* 31)+((this.actor == null)? 0 :this.actor.hashCode()));
        result = ((result* 31)+((this.result == null)? 0 :this.result.hashCode()));
        result = ((result* 31)+((this.stored == null)? 0 :this.stored.hashCode()));
        result = ((result* 31)+((this.authority == null)? 0 :this.authority.hashCode()));
        result = ((result* 31)+((this.verb == null)? 0 :this.verb.hashCode()));
        result = ((result* 31)+((this.context == null)? 0 :this.context.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.object == null)? 0 :this.object.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MsrReport) == false) {
            return false;
        }
        MsrReport rhs = ((MsrReport) other);
        return (((((((((((this.actor == rhs.actor)||((this.actor!= null)&&this.actor.equals(rhs.actor)))&&((this.result == rhs.result)||((this.result!= null)&&this.result.equals(rhs.result))))&&((this.stored == rhs.stored)||((this.stored!= null)&&this.stored.equals(rhs.stored))))&&((this.authority == rhs.authority)||((this.authority!= null)&&this.authority.equals(rhs.authority))))&&((this.verb == rhs.verb)||((this.verb!= null)&&this.verb.equals(rhs.verb))))&&((this.context == rhs.context)||((this.context!= null)&&this.context.equals(rhs.context))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.object == rhs.object)||((this.object!= null)&&this.object.equals(rhs.object))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
