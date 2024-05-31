
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "melId",
    "melCategory",
    "visualModelName",
    "epmModelName",
    "secoreModelName",
    "vbsModelName"
})
public class _3dModel {

    /**
     * required: mel ID given to the entity 3D model
     * (Required)
     * 
     */
    @JsonProperty("melId")
    @JsonPropertyDescription("required: mel ID given to the entity 3D model")
    private String melId;
    @JsonProperty("melCategory")
    private String melCategory;
    /**
     * required: visual model name given to the entity 3D model
     * 
     */
    @JsonProperty("visualModelName")
    @JsonPropertyDescription("required: visual model name given to the entity 3D model")
    private String visualModelName;
    /**
     * optional: gen 1 epm name given to the entity 3D model
     * 
     */
    @JsonProperty("epmModelName")
    @JsonPropertyDescription("optional: gen 1 epm name given to the entity 3D model")
    private String epmModelName;
    /**
     * required: gen 2 SECcore name given to the entity 3D model
     * 
     */
    @JsonProperty("secoreModelName")
    @JsonPropertyDescription("required: gen 2 SECcore name given to the entity 3D model")
    private String secoreModelName;
    /**
     * optional: Bohemia name given to the entity 3D model
     * 
     */
    @JsonProperty("vbsModelName")
    @JsonPropertyDescription("optional: Bohemia name given to the entity 3D model")
    private String vbsModelName;

    /**
     * required: mel ID given to the entity 3D model
     * (Required)
     * 
     */
    @JsonProperty("melId")
    public String getMelId() {
        return melId;
    }

    /**
     * required: mel ID given to the entity 3D model
     * (Required)
     * 
     */
    @JsonProperty("melId")
    public void setMelId(String melId) {
        this.melId = melId;
    }

    @JsonProperty("melCategory")
    public String getMelCategory() {
        return melCategory;
    }

    @JsonProperty("melCategory")
    public void setMelCategory(String melCategory) {
        this.melCategory = melCategory;
    }

    /**
     * required: visual model name given to the entity 3D model
     * 
     */
    @JsonProperty("visualModelName")
    public String getVisualModelName() {
        return visualModelName;
    }

    /**
     * required: visual model name given to the entity 3D model
     * 
     */
    @JsonProperty("visualModelName")
    public void setVisualModelName(String visualModelName) {
        this.visualModelName = visualModelName;
    }

    /**
     * optional: gen 1 epm name given to the entity 3D model
     * 
     */
    @JsonProperty("epmModelName")
    public String getEpmModelName() {
        return epmModelName;
    }

    /**
     * optional: gen 1 epm name given to the entity 3D model
     * 
     */
    @JsonProperty("epmModelName")
    public void setEpmModelName(String epmModelName) {
        this.epmModelName = epmModelName;
    }

    /**
     * required: gen 2 SECcore name given to the entity 3D model
     * 
     */
    @JsonProperty("secoreModelName")
    public String getSecoreModelName() {
        return secoreModelName;
    }

    /**
     * required: gen 2 SECcore name given to the entity 3D model
     * 
     */
    @JsonProperty("secoreModelName")
    public void setSecoreModelName(String secoreModelName) {
        this.secoreModelName = secoreModelName;
    }

    /**
     * optional: Bohemia name given to the entity 3D model
     * 
     */
    @JsonProperty("vbsModelName")
    public String getVbsModelName() {
        return vbsModelName;
    }

    /**
     * optional: Bohemia name given to the entity 3D model
     * 
     */
    @JsonProperty("vbsModelName")
    public void setVbsModelName(String vbsModelName) {
        this.vbsModelName = vbsModelName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_3dModel.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("melId");
        sb.append('=');
        sb.append(((this.melId == null)?"<null>":this.melId));
        sb.append(',');
        sb.append("melCategory");
        sb.append('=');
        sb.append(((this.melCategory == null)?"<null>":this.melCategory));
        sb.append(',');
        sb.append("visualModelName");
        sb.append('=');
        sb.append(((this.visualModelName == null)?"<null>":this.visualModelName));
        sb.append(',');
        sb.append("epmModelName");
        sb.append('=');
        sb.append(((this.epmModelName == null)?"<null>":this.epmModelName));
        sb.append(',');
        sb.append("secoreModelName");
        sb.append('=');
        sb.append(((this.secoreModelName == null)?"<null>":this.secoreModelName));
        sb.append(',');
        sb.append("vbsModelName");
        sb.append('=');
        sb.append(((this.vbsModelName == null)?"<null>":this.vbsModelName));
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
        result = ((result* 31)+((this.melId == null)? 0 :this.melId.hashCode()));
        result = ((result* 31)+((this.epmModelName == null)? 0 :this.epmModelName.hashCode()));
        result = ((result* 31)+((this.visualModelName == null)? 0 :this.visualModelName.hashCode()));
        result = ((result* 31)+((this.melCategory == null)? 0 :this.melCategory.hashCode()));
        result = ((result* 31)+((this.vbsModelName == null)? 0 :this.vbsModelName.hashCode()));
        result = ((result* 31)+((this.secoreModelName == null)? 0 :this.secoreModelName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof _3dModel) == false) {
            return false;
        }
        _3dModel rhs = ((_3dModel) other);
        return (((((((this.melId == rhs.melId)||((this.melId!= null)&&this.melId.equals(rhs.melId)))&&((this.epmModelName == rhs.epmModelName)||((this.epmModelName!= null)&&this.epmModelName.equals(rhs.epmModelName))))&&((this.visualModelName == rhs.visualModelName)||((this.visualModelName!= null)&&this.visualModelName.equals(rhs.visualModelName))))&&((this.melCategory == rhs.melCategory)||((this.melCategory!= null)&&this.melCategory.equals(rhs.melCategory))))&&((this.vbsModelName == rhs.vbsModelName)||((this.vbsModelName!= null)&&this.vbsModelName.equals(rhs.vbsModelName))))&&((this.secoreModelName == rhs.secoreModelName)||((this.secoreModelName!= null)&&this.secoreModelName.equals(rhs.secoreModelName))));
    }

}
