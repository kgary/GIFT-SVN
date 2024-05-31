
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "maintenance",
    "transport",
    "medical",
    "ammunition",
    "epwProcessing",
    "mealsWater",
    "personnel"
})
public class Logistics {

    @JsonProperty("maintenance")
    private String maintenance;
    @JsonProperty("transport")
    private String transport;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("medical")
    private String medical;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ammunition")
    private String ammunition;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("epwProcessing")
    private String epwProcessing;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mealsWater")
    private String mealsWater;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personnel")
    private String personnel;

    @JsonProperty("maintenance")
    public String getMaintenance() {
        return maintenance;
    }

    @JsonProperty("maintenance")
    public void setMaintenance(String maintenance) {
        this.maintenance = maintenance;
    }

    @JsonProperty("transport")
    public String getTransport() {
        return transport;
    }

    @JsonProperty("transport")
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("medical")
    public String getMedical() {
        return medical;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("medical")
    public void setMedical(String medical) {
        this.medical = medical;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ammunition")
    public String getAmmunition() {
        return ammunition;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ammunition")
    public void setAmmunition(String ammunition) {
        this.ammunition = ammunition;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("epwProcessing")
    public String getEpwProcessing() {
        return epwProcessing;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("epwProcessing")
    public void setEpwProcessing(String epwProcessing) {
        this.epwProcessing = epwProcessing;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mealsWater")
    public String getMealsWater() {
        return mealsWater;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("mealsWater")
    public void setMealsWater(String mealsWater) {
        this.mealsWater = mealsWater;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personnel")
    public String getPersonnel() {
        return personnel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personnel")
    public void setPersonnel(String personnel) {
        this.personnel = personnel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Logistics.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("maintenance");
        sb.append('=');
        sb.append(((this.maintenance == null)?"<null>":this.maintenance));
        sb.append(',');
        sb.append("transport");
        sb.append('=');
        sb.append(((this.transport == null)?"<null>":this.transport));
        sb.append(',');
        sb.append("medical");
        sb.append('=');
        sb.append(((this.medical == null)?"<null>":this.medical));
        sb.append(',');
        sb.append("ammunition");
        sb.append('=');
        sb.append(((this.ammunition == null)?"<null>":this.ammunition));
        sb.append(',');
        sb.append("epwProcessing");
        sb.append('=');
        sb.append(((this.epwProcessing == null)?"<null>":this.epwProcessing));
        sb.append(',');
        sb.append("mealsWater");
        sb.append('=');
        sb.append(((this.mealsWater == null)?"<null>":this.mealsWater));
        sb.append(',');
        sb.append("personnel");
        sb.append('=');
        sb.append(((this.personnel == null)?"<null>":this.personnel));
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
        result = ((result* 31)+((this.ammunition == null)? 0 :this.ammunition.hashCode()));
        result = ((result* 31)+((this.medical == null)? 0 :this.medical.hashCode()));
        result = ((result* 31)+((this.epwProcessing == null)? 0 :this.epwProcessing.hashCode()));
        result = ((result* 31)+((this.mealsWater == null)? 0 :this.mealsWater.hashCode()));
        result = ((result* 31)+((this.personnel == null)? 0 :this.personnel.hashCode()));
        result = ((result* 31)+((this.transport == null)? 0 :this.transport.hashCode()));
        result = ((result* 31)+((this.maintenance == null)? 0 :this.maintenance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Logistics) == false) {
            return false;
        }
        Logistics rhs = ((Logistics) other);
        return ((((((((this.ammunition == rhs.ammunition)||((this.ammunition!= null)&&this.ammunition.equals(rhs.ammunition)))&&((this.medical == rhs.medical)||((this.medical!= null)&&this.medical.equals(rhs.medical))))&&((this.epwProcessing == rhs.epwProcessing)||((this.epwProcessing!= null)&&this.epwProcessing.equals(rhs.epwProcessing))))&&((this.mealsWater == rhs.mealsWater)||((this.mealsWater!= null)&&this.mealsWater.equals(rhs.mealsWater))))&&((this.personnel == rhs.personnel)||((this.personnel!= null)&&this.personnel.equals(rhs.personnel))))&&((this.transport == rhs.transport)||((this.transport!= null)&&this.transport.equals(rhs.transport))))&&((this.maintenance == rhs.maintenance)||((this.maintenance!= null)&&this.maintenance.equals(rhs.maintenance))));
    }

}
