
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * a measured outcome credential assigned automatically or manually, based on associated criteria within a measure.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "levelId",
    "levelUuid",
    "levelTitle",
    "levelDescription"
})
public class Level {

    /**
     * required: unique local competency level identifier
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    @JsonPropertyDescription("required: unique local competency level identifier")
    private Object levelId;
    @JsonProperty("levelUuid")
    private String levelUuid;
    @JsonProperty("levelTitle")
    private String levelTitle;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("levelDescription")
    private String levelDescription;

    /**
     * required: unique local competency level identifier
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    public Object getLevelId() {
        return levelId;
    }

    /**
     * required: unique local competency level identifier
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    public void setLevelId(Object levelId) {
        this.levelId = levelId;
    }

    @JsonProperty("levelUuid")
    public String getLevelUuid() {
        return levelUuid;
    }

    @JsonProperty("levelUuid")
    public void setLevelUuid(String levelUuid) {
        this.levelUuid = levelUuid;
    }

    @JsonProperty("levelTitle")
    public String getLevelTitle() {
        return levelTitle;
    }

    @JsonProperty("levelTitle")
    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("levelDescription")
    public String getLevelDescription() {
        return levelDescription;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("levelDescription")
    public void setLevelDescription(String levelDescription) {
        this.levelDescription = levelDescription;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Level.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("levelId");
        sb.append('=');
        sb.append(((this.levelId == null)?"<null>":this.levelId));
        sb.append(',');
        sb.append("levelUuid");
        sb.append('=');
        sb.append(((this.levelUuid == null)?"<null>":this.levelUuid));
        sb.append(',');
        sb.append("levelTitle");
        sb.append('=');
        sb.append(((this.levelTitle == null)?"<null>":this.levelTitle));
        sb.append(',');
        sb.append("levelDescription");
        sb.append('=');
        sb.append(((this.levelDescription == null)?"<null>":this.levelDescription));
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
        result = ((result* 31)+((this.levelTitle == null)? 0 :this.levelTitle.hashCode()));
        result = ((result* 31)+((this.levelId == null)? 0 :this.levelId.hashCode()));
        result = ((result* 31)+((this.levelUuid == null)? 0 :this.levelUuid.hashCode()));
        result = ((result* 31)+((this.levelDescription == null)? 0 :this.levelDescription.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Level) == false) {
            return false;
        }
        Level rhs = ((Level) other);
        return (((((this.levelTitle == rhs.levelTitle)||((this.levelTitle!= null)&&this.levelTitle.equals(rhs.levelTitle)))&&((this.levelId == rhs.levelId)||((this.levelId!= null)&&this.levelId.equals(rhs.levelId))))&&((this.levelUuid == rhs.levelUuid)||((this.levelUuid!= null)&&this.levelUuid.equals(rhs.levelUuid))))&&((this.levelDescription == rhs.levelDescription)||((this.levelDescription!= null)&&this.levelDescription.equals(rhs.levelDescription))));
    }

}
