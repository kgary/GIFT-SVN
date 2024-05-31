
package generated.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * state are learning, cognitive, physiological or affective conditions that can trigger specific strategies
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "affectId",
    "affectUuid",
    "competencyUuid",
    "affectName",
    "affectType",
    "affectTgtState",
    "affectMeasures"
})
public class Affect {

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("affectId")
    @JsonPropertyDescription("required: a unique local state identifier")
    private Object affectId;
    /**
     * optional: the universal affect identifier
     * 
     */
    @JsonProperty("affectUuid")
    @JsonPropertyDescription("optional: the universal affect identifier")
    private String affectUuid;
    /**
     * optional: the universal competency identifier that this provides evidence for
     * 
     */
    @JsonProperty("competencyUuid")
    @JsonPropertyDescription("optional: the universal competency identifier that this provides evidence for")
    private String competencyUuid;
    /**
     * required: name of affect
     * (Required)
     * 
     */
    @JsonProperty("affectName")
    @JsonPropertyDescription("required: name of affect")
    private String affectName;
    /**
     * required: type of affect
     * (Required)
     * 
     */
    @JsonProperty("affectType")
    @JsonPropertyDescription("required: type of affect")
    private Affect.AffectType affectType;
    @JsonProperty("affectTgtState")
    private String affectTgtState;
    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("affectMeasures")
    @JsonPropertyDescription("required: measures of this team-skill")
    private List<Measure> affectMeasures = new ArrayList<Measure>();

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("affectId")
    public Object getAffectId() {
        return affectId;
    }

    /**
     * required: a unique local state identifier
     * (Required)
     * 
     */
    @JsonProperty("affectId")
    public void setAffectId(Object affectId) {
        this.affectId = affectId;
    }

    /**
     * optional: the universal affect identifier
     * 
     */
    @JsonProperty("affectUuid")
    public String getAffectUuid() {
        return affectUuid;
    }

    /**
     * optional: the universal affect identifier
     * 
     */
    @JsonProperty("affectUuid")
    public void setAffectUuid(String affectUuid) {
        this.affectUuid = affectUuid;
    }

    /**
     * optional: the universal competency identifier that this provides evidence for
     * 
     */
    @JsonProperty("competencyUuid")
    public String getCompetencyUuid() {
        return competencyUuid;
    }

    /**
     * optional: the universal competency identifier that this provides evidence for
     * 
     */
    @JsonProperty("competencyUuid")
    public void setCompetencyUuid(String competencyUuid) {
        this.competencyUuid = competencyUuid;
    }

    /**
     * required: name of affect
     * (Required)
     * 
     */
    @JsonProperty("affectName")
    public String getAffectName() {
        return affectName;
    }

    /**
     * required: name of affect
     * (Required)
     * 
     */
    @JsonProperty("affectName")
    public void setAffectName(String affectName) {
        this.affectName = affectName;
    }

    /**
     * required: type of affect
     * (Required)
     * 
     */
    @JsonProperty("affectType")
    public Affect.AffectType getAffectType() {
        return affectType;
    }

    /**
     * required: type of affect
     * (Required)
     * 
     */
    @JsonProperty("affectType")
    public void setAffectType(Affect.AffectType affectType) {
        this.affectType = affectType;
    }

    @JsonProperty("affectTgtState")
    public String getAffectTgtState() {
        return affectTgtState;
    }

    @JsonProperty("affectTgtState")
    public void setAffectTgtState(String affectTgtState) {
        this.affectTgtState = affectTgtState;
    }

    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("affectMeasures")
    public List<Measure> getAffectMeasures() {
        return affectMeasures;
    }

    /**
     * required: measures of this team-skill
     * (Required)
     * 
     */
    @JsonProperty("affectMeasures")
    public void setAffectMeasures(List<Measure> affectMeasures) {
        this.affectMeasures = affectMeasures;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Affect.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("affectId");
        sb.append('=');
        sb.append(((this.affectId == null)?"<null>":this.affectId));
        sb.append(',');
        sb.append("affectUuid");
        sb.append('=');
        sb.append(((this.affectUuid == null)?"<null>":this.affectUuid));
        sb.append(',');
        sb.append("competencyUuid");
        sb.append('=');
        sb.append(((this.competencyUuid == null)?"<null>":this.competencyUuid));
        sb.append(',');
        sb.append("affectName");
        sb.append('=');
        sb.append(((this.affectName == null)?"<null>":this.affectName));
        sb.append(',');
        sb.append("affectType");
        sb.append('=');
        sb.append(((this.affectType == null)?"<null>":this.affectType));
        sb.append(',');
        sb.append("affectTgtState");
        sb.append('=');
        sb.append(((this.affectTgtState == null)?"<null>":this.affectTgtState));
        sb.append(',');
        sb.append("affectMeasures");
        sb.append('=');
        sb.append(((this.affectMeasures == null)?"<null>":this.affectMeasures));
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
        result = ((result* 31)+((this.affectUuid == null)? 0 :this.affectUuid.hashCode()));
        result = ((result* 31)+((this.affectType == null)? 0 :this.affectType.hashCode()));
        result = ((result* 31)+((this.affectMeasures == null)? 0 :this.affectMeasures.hashCode()));
        result = ((result* 31)+((this.affectTgtState == null)? 0 :this.affectTgtState.hashCode()));
        result = ((result* 31)+((this.affectId == null)? 0 :this.affectId.hashCode()));
        result = ((result* 31)+((this.affectName == null)? 0 :this.affectName.hashCode()));
        result = ((result* 31)+((this.competencyUuid == null)? 0 :this.competencyUuid.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Affect) == false) {
            return false;
        }
        Affect rhs = ((Affect) other);
        return ((((((((this.affectUuid == rhs.affectUuid)||((this.affectUuid!= null)&&this.affectUuid.equals(rhs.affectUuid)))&&((this.affectType == rhs.affectType)||((this.affectType!= null)&&this.affectType.equals(rhs.affectType))))&&((this.affectMeasures == rhs.affectMeasures)||((this.affectMeasures!= null)&&this.affectMeasures.equals(rhs.affectMeasures))))&&((this.affectTgtState == rhs.affectTgtState)||((this.affectTgtState!= null)&&this.affectTgtState.equals(rhs.affectTgtState))))&&((this.affectId == rhs.affectId)||((this.affectId!= null)&&this.affectId.equals(rhs.affectId))))&&((this.affectName == rhs.affectName)||((this.affectName!= null)&&this.affectName.equals(rhs.affectName))))&&((this.competencyUuid == rhs.competencyUuid)||((this.competencyUuid!= null)&&this.competencyUuid.equals(rhs.competencyUuid))));
    }


    /**
     * required: type of affect
     * 
     */
    public enum AffectType {

        ATTITUDE("attitude"),
        VALUE("value"),
        PSYCHOLOGIC("psychologic"),
        PHYSIOLOGIC("physiologic");
        private final String value;
        private final static Map<String, Affect.AffectType> CONSTANTS = new HashMap<String, Affect.AffectType>();

        static {
            for (Affect.AffectType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        AffectType(String value) {
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
        public static Affect.AffectType fromValue(String value) {
            Affect.AffectType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
