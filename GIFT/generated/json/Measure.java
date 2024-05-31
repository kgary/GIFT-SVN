
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
 * a measurable team outcome or individual (synthetic/live) action/gesture that is required for a task to be measured formatively in a given condition.  Measures are done using criteria, which that have sources of data, rubrics and levels (criterion) to automatically/manually evaluate
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "msrId",
    "msrUuid",
    "msrTitle",
    "msrClass",
    "msrType",
    "position",
    "weight",
    "msrConditions",
    "evalMethod",
    "evalClass",
    "dataSources",
    "methodInputs",
    "defaultLevel",
    "formativeCriteria",
    "summativeCriteria",
    "subMeasures",
    "msrReport",
    "measureNotes"
})
public class Measure {

    /**
     * required: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * (Required)
     * 
     */
    @JsonProperty("msrId")
    @JsonPropertyDescription("required: an exercise unique task measure identifier associated with a tiven task in a given xEvent")
    private Object msrId;
    /**
     * optional: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * 
     */
    @JsonProperty("msrUuid")
    @JsonPropertyDescription("optional: an exercise unique task measure identifier associated with a tiven task in a given xEvent")
    private String msrUuid;
    /**
     * required: The behavior, decision, identification, physiological state expected to happen
     * (Required)
     * 
     */
    @JsonProperty("msrTitle")
    @JsonPropertyDescription("required: The behavior, decision, identification, physiological state expected to happen")
    private String msrTitle;
    /**
     * required: how is measure measured.  Summative means to evaluate the sum of all measured instances.  formative means to evaluate each instance.  Roll-up means to combine this measure with sub-measures.
     * 
     */
    @JsonProperty("msrClass")
    @JsonPropertyDescription("required: how is measure measured.  Summative means to evaluate the sum of all measured instances.  formative means to evaluate each instance.  Roll-up means to combine this measure with sub-measures.")
    private Measure.MsrClass msrClass;
    /**
     * optional: what is actually being measured
     * (Required)
     * 
     */
    @JsonProperty("msrType")
    @JsonPropertyDescription("optional: what is actually being measured")
    private Measure.MsrType msrType;
    /**
     * optional: the sequence the measure should occur - for procedural steps
     * (Required)
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("optional: the sequence the measure should occur - for procedural steps")
    private Integer position;
    @JsonProperty("weight")
    private Double weight;
    /**
     * option: required to enable measure
     * (Required)
     * 
     */
    @JsonProperty("msrConditions")
    @JsonPropertyDescription("option: required to enable measure")
    private List<String> msrConditions = new ArrayList<String>();
    @JsonProperty("evalMethod")
    private Measure.EvalMethod evalMethod;
    @JsonProperty("evalClass")
    private List<String> evalClass = new ArrayList<String>();
    /**
     * optional: evaluation points to observe xevent
     * 
     */
    @JsonProperty("dataSources")
    @JsonPropertyDescription("optional: evaluation points to observe xevent")
    private Object dataSources;
    /**
     * option: use to define parameter inputs to evaluator
     * 
     */
    @JsonProperty("methodInputs")
    @JsonPropertyDescription("option: use to define parameter inputs to evaluator")
    private List<String> methodInputs = new ArrayList<String>();
    /**
     * option: what performance level should be assumed
     * 
     */
    @JsonProperty("defaultLevel")
    @JsonPropertyDescription("option: what performance level should be assumed")
    private Object defaultLevel;
    /**
     * option: use to measure in real-time
     * 
     */
    @JsonProperty("formativeCriteria")
    @JsonPropertyDescription("option: use to measure in real-time")
    private List<Criterion> formativeCriteria = new ArrayList<Criterion>();
    /**
     * option: use to measure at end of an xevent
     * 
     */
    @JsonProperty("summativeCriteria")
    @JsonPropertyDescription("option: use to measure at end of an xevent")
    private List<Criterion> summativeCriteria = new ArrayList<Criterion>();
    /**
     * optional: a sub-measure that with its own criteria that must be met first to roll-up into this measure
     * 
     */
    @JsonProperty("subMeasures")
    @JsonPropertyDescription("optional: a sub-measure that with its own criteria that must be met first to roll-up into this measure")
    private List<Measure> subMeasures = new ArrayList<Measure>();
    /**
     * optional: a pre defined xAPI format to be used with variables
     * 
     */
    @JsonProperty("msrReport")
    @JsonPropertyDescription("optional: a pre defined xAPI format to be used with variables")
    private MsrReport msrReport;
    @JsonProperty("measureNotes")
    private String measureNotes;

    /**
     * required: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * (Required)
     * 
     */
    @JsonProperty("msrId")
    public Object getMsrId() {
        return msrId;
    }

    /**
     * required: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * (Required)
     * 
     */
    @JsonProperty("msrId")
    public void setMsrId(Object msrId) {
        this.msrId = msrId;
    }

    /**
     * optional: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * 
     */
    @JsonProperty("msrUuid")
    public String getMsrUuid() {
        return msrUuid;
    }

    /**
     * optional: an exercise unique task measure identifier associated with a tiven task in a given xEvent
     * 
     */
    @JsonProperty("msrUuid")
    public void setMsrUuid(String msrUuid) {
        this.msrUuid = msrUuid;
    }

    /**
     * required: The behavior, decision, identification, physiological state expected to happen
     * (Required)
     * 
     */
    @JsonProperty("msrTitle")
    public String getMsrTitle() {
        return msrTitle;
    }

    /**
     * required: The behavior, decision, identification, physiological state expected to happen
     * (Required)
     * 
     */
    @JsonProperty("msrTitle")
    public void setMsrTitle(String msrTitle) {
        this.msrTitle = msrTitle;
    }

    /**
     * required: how is measure measured.  Summative means to evaluate the sum of all measured instances.  formative means to evaluate each instance.  Roll-up means to combine this measure with sub-measures.
     * 
     */
    @JsonProperty("msrClass")
    public Measure.MsrClass getMsrClass() {
        return msrClass;
    }

    /**
     * required: how is measure measured.  Summative means to evaluate the sum of all measured instances.  formative means to evaluate each instance.  Roll-up means to combine this measure with sub-measures.
     * 
     */
    @JsonProperty("msrClass")
    public void setMsrClass(Measure.MsrClass msrClass) {
        this.msrClass = msrClass;
    }

    /**
     * optional: what is actually being measured
     * (Required)
     * 
     */
    @JsonProperty("msrType")
    public Measure.MsrType getMsrType() {
        return msrType;
    }

    /**
     * optional: what is actually being measured
     * (Required)
     * 
     */
    @JsonProperty("msrType")
    public void setMsrType(Measure.MsrType msrType) {
        this.msrType = msrType;
    }

    /**
     * optional: the sequence the measure should occur - for procedural steps
     * (Required)
     * 
     */
    @JsonProperty("position")
    public Integer getPosition() {
        return position;
    }

    /**
     * optional: the sequence the measure should occur - for procedural steps
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(Integer position) {
        this.position = position;
    }

    @JsonProperty("weight")
    public Double getWeight() {
        return weight;
    }

    @JsonProperty("weight")
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * option: required to enable measure
     * (Required)
     * 
     */
    @JsonProperty("msrConditions")
    public List<String> getMsrConditions() {
        return msrConditions;
    }

    /**
     * option: required to enable measure
     * (Required)
     * 
     */
    @JsonProperty("msrConditions")
    public void setMsrConditions(List<String> msrConditions) {
        this.msrConditions = msrConditions;
    }

    @JsonProperty("evalMethod")
    public Measure.EvalMethod getEvalMethod() {
        return evalMethod;
    }

    @JsonProperty("evalMethod")
    public void setEvalMethod(Measure.EvalMethod evalMethod) {
        this.evalMethod = evalMethod;
    }

    @JsonProperty("evalClass")
    public List<String> getEvalClass() {
        return evalClass;
    }

    @JsonProperty("evalClass")
    public void setEvalClass(List<String> evalClass) {
        this.evalClass = evalClass;
    }

    /**
     * optional: evaluation points to observe xevent
     * 
     */
    @JsonProperty("dataSources")
    public Object getDataSources() {
        return dataSources;
    }

    /**
     * optional: evaluation points to observe xevent
     * 
     */
    @JsonProperty("dataSources")
    public void setDataSources(Object dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * option: use to define parameter inputs to evaluator
     * 
     */
    @JsonProperty("methodInputs")
    public List<String> getMethodInputs() {
        return methodInputs;
    }

    /**
     * option: use to define parameter inputs to evaluator
     * 
     */
    @JsonProperty("methodInputs")
    public void setMethodInputs(List<String> methodInputs) {
        this.methodInputs = methodInputs;
    }

    /**
     * option: what performance level should be assumed
     * 
     */
    @JsonProperty("defaultLevel")
    public Object getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * option: what performance level should be assumed
     * 
     */
    @JsonProperty("defaultLevel")
    public void setDefaultLevel(Object defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    /**
     * option: use to measure in real-time
     * 
     */
    @JsonProperty("formativeCriteria")
    public List<Criterion> getFormativeCriteria() {
        return formativeCriteria;
    }

    /**
     * option: use to measure in real-time
     * 
     */
    @JsonProperty("formativeCriteria")
    public void setFormativeCriteria(List<Criterion> formativeCriteria) {
        this.formativeCriteria = formativeCriteria;
    }

    /**
     * option: use to measure at end of an xevent
     * 
     */
    @JsonProperty("summativeCriteria")
    public List<Criterion> getSummativeCriteria() {
        return summativeCriteria;
    }

    /**
     * option: use to measure at end of an xevent
     * 
     */
    @JsonProperty("summativeCriteria")
    public void setSummativeCriteria(List<Criterion> summativeCriteria) {
        this.summativeCriteria = summativeCriteria;
    }

    /**
     * optional: a sub-measure that with its own criteria that must be met first to roll-up into this measure
     * 
     */
    @JsonProperty("subMeasures")
    public List<Measure> getSubMeasures() {
        return subMeasures;
    }

    /**
     * optional: a sub-measure that with its own criteria that must be met first to roll-up into this measure
     * 
     */
    @JsonProperty("subMeasures")
    public void setSubMeasures(List<Measure> subMeasures) {
        this.subMeasures = subMeasures;
    }

    /**
     * optional: a pre defined xAPI format to be used with variables
     * 
     */
    @JsonProperty("msrReport")
    public MsrReport getMsrReport() {
        return msrReport;
    }

    /**
     * optional: a pre defined xAPI format to be used with variables
     * 
     */
    @JsonProperty("msrReport")
    public void setMsrReport(MsrReport msrReport) {
        this.msrReport = msrReport;
    }

    @JsonProperty("measureNotes")
    public String getMeasureNotes() {
        return measureNotes;
    }

    @JsonProperty("measureNotes")
    public void setMeasureNotes(String measureNotes) {
        this.measureNotes = measureNotes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Measure.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("msrId");
        sb.append('=');
        sb.append(((this.msrId == null)?"<null>":this.msrId));
        sb.append(',');
        sb.append("msrUuid");
        sb.append('=');
        sb.append(((this.msrUuid == null)?"<null>":this.msrUuid));
        sb.append(',');
        sb.append("msrTitle");
        sb.append('=');
        sb.append(((this.msrTitle == null)?"<null>":this.msrTitle));
        sb.append(',');
        sb.append("msrClass");
        sb.append('=');
        sb.append(((this.msrClass == null)?"<null>":this.msrClass));
        sb.append(',');
        sb.append("msrType");
        sb.append('=');
        sb.append(((this.msrType == null)?"<null>":this.msrType));
        sb.append(',');
        sb.append("position");
        sb.append('=');
        sb.append(((this.position == null)?"<null>":this.position));
        sb.append(',');
        sb.append("weight");
        sb.append('=');
        sb.append(((this.weight == null)?"<null>":this.weight));
        sb.append(',');
        sb.append("msrConditions");
        sb.append('=');
        sb.append(((this.msrConditions == null)?"<null>":this.msrConditions));
        sb.append(',');
        sb.append("evalMethod");
        sb.append('=');
        sb.append(((this.evalMethod == null)?"<null>":this.evalMethod));
        sb.append(',');
        sb.append("evalClass");
        sb.append('=');
        sb.append(((this.evalClass == null)?"<null>":this.evalClass));
        sb.append(',');
        sb.append("dataSources");
        sb.append('=');
        sb.append(((this.dataSources == null)?"<null>":this.dataSources));
        sb.append(',');
        sb.append("methodInputs");
        sb.append('=');
        sb.append(((this.methodInputs == null)?"<null>":this.methodInputs));
        sb.append(',');
        sb.append("defaultLevel");
        sb.append('=');
        sb.append(((this.defaultLevel == null)?"<null>":this.defaultLevel));
        sb.append(',');
        sb.append("formativeCriteria");
        sb.append('=');
        sb.append(((this.formativeCriteria == null)?"<null>":this.formativeCriteria));
        sb.append(',');
        sb.append("summativeCriteria");
        sb.append('=');
        sb.append(((this.summativeCriteria == null)?"<null>":this.summativeCriteria));
        sb.append(',');
        sb.append("subMeasures");
        sb.append('=');
        sb.append(((this.subMeasures == null)?"<null>":this.subMeasures));
        sb.append(',');
        sb.append("msrReport");
        sb.append('=');
        sb.append(((this.msrReport == null)?"<null>":this.msrReport));
        sb.append(',');
        sb.append("measureNotes");
        sb.append('=');
        sb.append(((this.measureNotes == null)?"<null>":this.measureNotes));
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
        result = ((result* 31)+((this.msrConditions == null)? 0 :this.msrConditions.hashCode()));
        result = ((result* 31)+((this.msrReport == null)? 0 :this.msrReport.hashCode()));
        result = ((result* 31)+((this.defaultLevel == null)? 0 :this.defaultLevel.hashCode()));
        result = ((result* 31)+((this.weight == null)? 0 :this.weight.hashCode()));
        result = ((result* 31)+((this.evalClass == null)? 0 :this.evalClass.hashCode()));
        result = ((result* 31)+((this.msrTitle == null)? 0 :this.msrTitle.hashCode()));
        result = ((result* 31)+((this.formativeCriteria == null)? 0 :this.formativeCriteria.hashCode()));
        result = ((result* 31)+((this.msrId == null)? 0 :this.msrId.hashCode()));
        result = ((result* 31)+((this.methodInputs == null)? 0 :this.methodInputs.hashCode()));
        result = ((result* 31)+((this.msrUuid == null)? 0 :this.msrUuid.hashCode()));
        result = ((result* 31)+((this.msrType == null)? 0 :this.msrType.hashCode()));
        result = ((result* 31)+((this.subMeasures == null)? 0 :this.subMeasures.hashCode()));
        result = ((result* 31)+((this.summativeCriteria == null)? 0 :this.summativeCriteria.hashCode()));
        result = ((result* 31)+((this.msrClass == null)? 0 :this.msrClass.hashCode()));
        result = ((result* 31)+((this.position == null)? 0 :this.position.hashCode()));
        result = ((result* 31)+((this.measureNotes == null)? 0 :this.measureNotes.hashCode()));
        result = ((result* 31)+((this.dataSources == null)? 0 :this.dataSources.hashCode()));
        result = ((result* 31)+((this.evalMethod == null)? 0 :this.evalMethod.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Measure) == false) {
            return false;
        }
        Measure rhs = ((Measure) other);
        return (((((((((((((((((((this.msrConditions == rhs.msrConditions)||((this.msrConditions!= null)&&this.msrConditions.equals(rhs.msrConditions)))&&((this.msrReport == rhs.msrReport)||((this.msrReport!= null)&&this.msrReport.equals(rhs.msrReport))))&&((this.defaultLevel == rhs.defaultLevel)||((this.defaultLevel!= null)&&this.defaultLevel.equals(rhs.defaultLevel))))&&((this.weight == rhs.weight)||((this.weight!= null)&&this.weight.equals(rhs.weight))))&&((this.evalClass == rhs.evalClass)||((this.evalClass!= null)&&this.evalClass.equals(rhs.evalClass))))&&((this.msrTitle == rhs.msrTitle)||((this.msrTitle!= null)&&this.msrTitle.equals(rhs.msrTitle))))&&((this.formativeCriteria == rhs.formativeCriteria)||((this.formativeCriteria!= null)&&this.formativeCriteria.equals(rhs.formativeCriteria))))&&((this.msrId == rhs.msrId)||((this.msrId!= null)&&this.msrId.equals(rhs.msrId))))&&((this.methodInputs == rhs.methodInputs)||((this.methodInputs!= null)&&this.methodInputs.equals(rhs.methodInputs))))&&((this.msrUuid == rhs.msrUuid)||((this.msrUuid!= null)&&this.msrUuid.equals(rhs.msrUuid))))&&((this.msrType == rhs.msrType)||((this.msrType!= null)&&this.msrType.equals(rhs.msrType))))&&((this.subMeasures == rhs.subMeasures)||((this.subMeasures!= null)&&this.subMeasures.equals(rhs.subMeasures))))&&((this.summativeCriteria == rhs.summativeCriteria)||((this.summativeCriteria!= null)&&this.summativeCriteria.equals(rhs.summativeCriteria))))&&((this.msrClass == rhs.msrClass)||((this.msrClass!= null)&&this.msrClass.equals(rhs.msrClass))))&&((this.position == rhs.position)||((this.position!= null)&&this.position.equals(rhs.position))))&&((this.measureNotes == rhs.measureNotes)||((this.measureNotes!= null)&&this.measureNotes.equals(rhs.measureNotes))))&&((this.dataSources == rhs.dataSources)||((this.dataSources!= null)&&this.dataSources.equals(rhs.dataSources))))&&((this.evalMethod == rhs.evalMethod)||((this.evalMethod!= null)&&this.evalMethod.equals(rhs.evalMethod))));
    }

    public enum EvalMethod {

        AUTOMATED("automated"),
        MANUAL("manual"),
        ROLL_UP("roll-up");
        private final String value;
        private final static Map<String, Measure.EvalMethod> CONSTANTS = new HashMap<String, Measure.EvalMethod>();

        static {
            for (Measure.EvalMethod c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        EvalMethod(String value) {
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
        public static Measure.EvalMethod fromValue(String value) {
            Measure.EvalMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: how is measure measured.  Summative means to evaluate the sum of all measured instances.  formative means to evaluate each instance.  Roll-up means to combine this measure with sub-measures.
     * 
     */
    public enum MsrClass {

        INDIVIDUAL("individual"),
        TEAM("team");
        private final String value;
        private final static Map<String, Measure.MsrClass> CONSTANTS = new HashMap<String, Measure.MsrClass>();

        static {
            for (Measure.MsrClass c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MsrClass(String value) {
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
        public static Measure.MsrClass fromValue(String value) {
            Measure.MsrClass constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: what is actually being measured
     * 
     */
    public enum MsrType {

        ASSIGNED_TASK_COMPLETION("assigned-task-completion"),
        LEADERSHIP_SKILL("leadership-skill"),
        MOTOR_SKILL("motor-skill"),
        COGNITIVE_SKILL("cognitive-skill"),
        TEAMWORK_SKILL("teamwork-skill"),
        ATTITUDE_STATE("attitude-state"),
        AFFECTIVE_STATE("affective-state"),
        KNOWLEDGE_STATE("knowledge-state");
        private final String value;
        private final static Map<String, Measure.MsrType> CONSTANTS = new HashMap<String, Measure.MsrType>();

        static {
            for (Measure.MsrType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MsrType(String value) {
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
        public static Measure.MsrType fromValue(String value) {
            Measure.MsrType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
