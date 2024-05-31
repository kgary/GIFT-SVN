
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * optional: a glyph-symbol image sused to represent a team, role or object on map
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "standard",
    "symbolCode",
    "additionalInfo",
    "symbolModifier2",
    "functionId"
})
public class Glyph {

    @JsonProperty("standard")
    private String standard = "MIL-STD-2525C";
    @JsonProperty("symbolCode")
    private String symbolCode;
    @JsonProperty("additionalInfo")
    private String additionalInfo;
    @JsonProperty("symbolModifier2")
    private String symbolModifier2;
    @JsonProperty("functionId")
    private String functionId;

    @JsonProperty("standard")
    public String getStandard() {
        return standard;
    }

    @JsonProperty("standard")
    public void setStandard(String standard) {
        this.standard = standard;
    }

    @JsonProperty("symbolCode")
    public String getSymbolCode() {
        return symbolCode;
    }

    @JsonProperty("symbolCode")
    public void setSymbolCode(String symbolCode) {
        this.symbolCode = symbolCode;
    }

    @JsonProperty("additionalInfo")
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @JsonProperty("symbolModifier2")
    public String getSymbolModifier2() {
        return symbolModifier2;
    }

    @JsonProperty("symbolModifier2")
    public void setSymbolModifier2(String symbolModifier2) {
        this.symbolModifier2 = symbolModifier2;
    }

    @JsonProperty("functionId")
    public String getFunctionId() {
        return functionId;
    }

    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Glyph.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("standard");
        sb.append('=');
        sb.append(((this.standard == null)?"<null>":this.standard));
        sb.append(',');
        sb.append("symbolCode");
        sb.append('=');
        sb.append(((this.symbolCode == null)?"<null>":this.symbolCode));
        sb.append(',');
        sb.append("additionalInfo");
        sb.append('=');
        sb.append(((this.additionalInfo == null)?"<null>":this.additionalInfo));
        sb.append(',');
        sb.append("symbolModifier2");
        sb.append('=');
        sb.append(((this.symbolModifier2 == null)?"<null>":this.symbolModifier2));
        sb.append(',');
        sb.append("functionId");
        sb.append('=');
        sb.append(((this.functionId == null)?"<null>":this.functionId));
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
        result = ((result* 31)+((this.standard == null)? 0 :this.standard.hashCode()));
        result = ((result* 31)+((this.symbolCode == null)? 0 :this.symbolCode.hashCode()));
        result = ((result* 31)+((this.additionalInfo == null)? 0 :this.additionalInfo.hashCode()));
        result = ((result* 31)+((this.functionId == null)? 0 :this.functionId.hashCode()));
        result = ((result* 31)+((this.symbolModifier2 == null)? 0 :this.symbolModifier2 .hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Glyph) == false) {
            return false;
        }
        Glyph rhs = ((Glyph) other);
        return ((((((this.standard == rhs.standard)||((this.standard!= null)&&this.standard.equals(rhs.standard)))&&((this.symbolCode == rhs.symbolCode)||((this.symbolCode!= null)&&this.symbolCode.equals(rhs.symbolCode))))&&((this.additionalInfo == rhs.additionalInfo)||((this.additionalInfo!= null)&&this.additionalInfo.equals(rhs.additionalInfo))))&&((this.functionId == rhs.functionId)||((this.functionId!= null)&&this.functionId.equals(rhs.functionId))))&&((this.symbolModifier2 == rhs.symbolModifier2)||((this.symbolModifier2 != null)&&this.symbolModifier2 .equals(rhs.symbolModifier2))));
    }

}
