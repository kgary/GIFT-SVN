
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * option: describes civil factors - if any/known
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "rulesInteraction",
    "epwProcedures",
    "civilContacts",
    "civilNorms",
    "civilAffiliations",
    "civilResources",
    "civilThreatRisks"
})
public class CivilConsiderations {

    @JsonProperty("rulesInteraction")
    private String rulesInteraction;
    @JsonProperty("epwProcedures")
    private String epwProcedures;
    @JsonProperty("civilContacts")
    private String civilContacts;
    @JsonProperty("civilNorms")
    private String civilNorms;
    @JsonProperty("civilAffiliations")
    private String civilAffiliations;
    @JsonProperty("civilResources")
    private String civilResources;
    @JsonProperty("civilThreatRisks")
    private String civilThreatRisks;

    @JsonProperty("rulesInteraction")
    public String getRulesInteraction() {
        return rulesInteraction;
    }

    @JsonProperty("rulesInteraction")
    public void setRulesInteraction(String rulesInteraction) {
        this.rulesInteraction = rulesInteraction;
    }

    @JsonProperty("epwProcedures")
    public String getEpwProcedures() {
        return epwProcedures;
    }

    @JsonProperty("epwProcedures")
    public void setEpwProcedures(String epwProcedures) {
        this.epwProcedures = epwProcedures;
    }

    @JsonProperty("civilContacts")
    public String getCivilContacts() {
        return civilContacts;
    }

    @JsonProperty("civilContacts")
    public void setCivilContacts(String civilContacts) {
        this.civilContacts = civilContacts;
    }

    @JsonProperty("civilNorms")
    public String getCivilNorms() {
        return civilNorms;
    }

    @JsonProperty("civilNorms")
    public void setCivilNorms(String civilNorms) {
        this.civilNorms = civilNorms;
    }

    @JsonProperty("civilAffiliations")
    public String getCivilAffiliations() {
        return civilAffiliations;
    }

    @JsonProperty("civilAffiliations")
    public void setCivilAffiliations(String civilAffiliations) {
        this.civilAffiliations = civilAffiliations;
    }

    @JsonProperty("civilResources")
    public String getCivilResources() {
        return civilResources;
    }

    @JsonProperty("civilResources")
    public void setCivilResources(String civilResources) {
        this.civilResources = civilResources;
    }

    @JsonProperty("civilThreatRisks")
    public String getCivilThreatRisks() {
        return civilThreatRisks;
    }

    @JsonProperty("civilThreatRisks")
    public void setCivilThreatRisks(String civilThreatRisks) {
        this.civilThreatRisks = civilThreatRisks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CivilConsiderations.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("rulesInteraction");
        sb.append('=');
        sb.append(((this.rulesInteraction == null)?"<null>":this.rulesInteraction));
        sb.append(',');
        sb.append("epwProcedures");
        sb.append('=');
        sb.append(((this.epwProcedures == null)?"<null>":this.epwProcedures));
        sb.append(',');
        sb.append("civilContacts");
        sb.append('=');
        sb.append(((this.civilContacts == null)?"<null>":this.civilContacts));
        sb.append(',');
        sb.append("civilNorms");
        sb.append('=');
        sb.append(((this.civilNorms == null)?"<null>":this.civilNorms));
        sb.append(',');
        sb.append("civilAffiliations");
        sb.append('=');
        sb.append(((this.civilAffiliations == null)?"<null>":this.civilAffiliations));
        sb.append(',');
        sb.append("civilResources");
        sb.append('=');
        sb.append(((this.civilResources == null)?"<null>":this.civilResources));
        sb.append(',');
        sb.append("civilThreatRisks");
        sb.append('=');
        sb.append(((this.civilThreatRisks == null)?"<null>":this.civilThreatRisks));
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
        result = ((result* 31)+((this.civilThreatRisks == null)? 0 :this.civilThreatRisks.hashCode()));
        result = ((result* 31)+((this.civilAffiliations == null)? 0 :this.civilAffiliations.hashCode()));
        result = ((result* 31)+((this.rulesInteraction == null)? 0 :this.rulesInteraction.hashCode()));
        result = ((result* 31)+((this.epwProcedures == null)? 0 :this.epwProcedures.hashCode()));
        result = ((result* 31)+((this.civilContacts == null)? 0 :this.civilContacts.hashCode()));
        result = ((result* 31)+((this.civilResources == null)? 0 :this.civilResources.hashCode()));
        result = ((result* 31)+((this.civilNorms == null)? 0 :this.civilNorms.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CivilConsiderations) == false) {
            return false;
        }
        CivilConsiderations rhs = ((CivilConsiderations) other);
        return ((((((((this.civilThreatRisks == rhs.civilThreatRisks)||((this.civilThreatRisks!= null)&&this.civilThreatRisks.equals(rhs.civilThreatRisks)))&&((this.civilAffiliations == rhs.civilAffiliations)||((this.civilAffiliations!= null)&&this.civilAffiliations.equals(rhs.civilAffiliations))))&&((this.rulesInteraction == rhs.rulesInteraction)||((this.rulesInteraction!= null)&&this.rulesInteraction.equals(rhs.rulesInteraction))))&&((this.epwProcedures == rhs.epwProcedures)||((this.epwProcedures!= null)&&this.epwProcedures.equals(rhs.epwProcedures))))&&((this.civilContacts == rhs.civilContacts)||((this.civilContacts!= null)&&this.civilContacts.equals(rhs.civilContacts))))&&((this.civilResources == rhs.civilResources)||((this.civilResources!= null)&&this.civilResources.equals(rhs.civilResources))))&&((this.civilNorms == rhs.civilNorms)||((this.civilNorms!= null)&&this.civilNorms.equals(rhs.civilNorms))));
    }

}
