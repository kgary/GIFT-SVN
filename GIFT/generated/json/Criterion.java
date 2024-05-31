
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * level criterion are unique event inputs used by automatic or manual assessment
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "criterionId",
    "levelId",
    "criterion",
    "criterionType",
    "position",
    "operator",
    "value",
    "metric",
    "weight",
    "logic",
    "criterionNotes"
})
public class Criterion {

    /**
     * required: unique for every event, task/measure/criteria in an exercise
     * (Required)
     * 
     */
    @JsonProperty("criterionId")
    @JsonPropertyDescription("required: unique for every event, task/measure/criteria in an exercise")
    private Object criterionId;
    /**
     * required: the level the criterion is for
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    @JsonPropertyDescription("required: the level the criterion is for")
    private Integer levelId;
    @JsonProperty("criterion")
    private String criterion;
    /**
     * required: the type of criterion
     * (Required)
     * 
     */
    @JsonProperty("criterionType")
    @JsonPropertyDescription("required: the type of criterion")
    private Criterion.CriterionType criterionType;
    /**
     * required: the order in which the criterion is evaluated
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("required: the order in which the criterion is evaluated")
    private Integer position;
    /**
     * required: the evaluation operators used to compare data with value
     * (Required)
     * 
     */
    @JsonProperty("operator")
    @JsonPropertyDescription("required: the evaluation operators used to compare data with value")
    private Criterion.Operator operator;
    /**
     * required: what is expected for the level this criterion is assigned to.  Can be a decimal number, integer, string or boolean value
     * (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("required: what is expected for the level this criterion is assigned to.  Can be a decimal number, integer, string or boolean value")
    private Object value;
    /**
     * optional: the unit of measure used
     * 
     */
    @JsonProperty("metric")
    @JsonPropertyDescription("optional: the unit of measure used")
    private Criterion.Metric metric;
    /**
     * optional: the weight of this criterion compared to other criterion
     * 
     */
    @JsonProperty("weight")
    @JsonPropertyDescription("optional: the weight of this criterion compared to other criterion")
    private Double weight;
    /**
     * optional: indicates if this criterion is to be measured with previous criterion in position order
     * 
     */
    @JsonProperty("logic")
    @JsonPropertyDescription("optional: indicates if this criterion is to be measured with previous criterion in position order")
    private Criterion.Logic logic;
    /**
     * optional: give additional criterion instruction to evaluator
     * 
     */
    @JsonProperty("criterionNotes")
    @JsonPropertyDescription("optional: give additional criterion instruction to evaluator")
    private String criterionNotes;

    /**
     * required: unique for every event, task/measure/criteria in an exercise
     * (Required)
     * 
     */
    @JsonProperty("criterionId")
    public Object getCriterionId() {
        return criterionId;
    }

    /**
     * required: unique for every event, task/measure/criteria in an exercise
     * (Required)
     * 
     */
    @JsonProperty("criterionId")
    public void setCriterionId(Object criterionId) {
        this.criterionId = criterionId;
    }

    /**
     * required: the level the criterion is for
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    public Integer getLevelId() {
        return levelId;
    }

    /**
     * required: the level the criterion is for
     * (Required)
     * 
     */
    @JsonProperty("levelId")
    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    @JsonProperty("criterion")
    public String getCriterion() {
        return criterion;
    }

    @JsonProperty("criterion")
    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    /**
     * required: the type of criterion
     * (Required)
     * 
     */
    @JsonProperty("criterionType")
    public Criterion.CriterionType getCriterionType() {
        return criterionType;
    }

    /**
     * required: the type of criterion
     * (Required)
     * 
     */
    @JsonProperty("criterionType")
    public void setCriterionType(Criterion.CriterionType criterionType) {
        this.criterionType = criterionType;
    }

    /**
     * required: the order in which the criterion is evaluated
     * 
     */
    @JsonProperty("position")
    public Integer getPosition() {
        return position;
    }

    /**
     * required: the order in which the criterion is evaluated
     * 
     */
    @JsonProperty("position")
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * required: the evaluation operators used to compare data with value
     * (Required)
     * 
     */
    @JsonProperty("operator")
    public Criterion.Operator getOperator() {
        return operator;
    }

    /**
     * required: the evaluation operators used to compare data with value
     * (Required)
     * 
     */
    @JsonProperty("operator")
    public void setOperator(Criterion.Operator operator) {
        this.operator = operator;
    }

    /**
     * required: what is expected for the level this criterion is assigned to.  Can be a decimal number, integer, string or boolean value
     * (Required)
     * 
     */
    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    /**
     * required: what is expected for the level this criterion is assigned to.  Can be a decimal number, integer, string or boolean value
     * (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * optional: the unit of measure used
     * 
     */
    @JsonProperty("metric")
    public Criterion.Metric getMetric() {
        return metric;
    }

    /**
     * optional: the unit of measure used
     * 
     */
    @JsonProperty("metric")
    public void setMetric(Criterion.Metric metric) {
        this.metric = metric;
    }

    /**
     * optional: the weight of this criterion compared to other criterion
     * 
     */
    @JsonProperty("weight")
    public Double getWeight() {
        return weight;
    }

    /**
     * optional: the weight of this criterion compared to other criterion
     * 
     */
    @JsonProperty("weight")
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * optional: indicates if this criterion is to be measured with previous criterion in position order
     * 
     */
    @JsonProperty("logic")
    public Criterion.Logic getLogic() {
        return logic;
    }

    /**
     * optional: indicates if this criterion is to be measured with previous criterion in position order
     * 
     */
    @JsonProperty("logic")
    public void setLogic(Criterion.Logic logic) {
        this.logic = logic;
    }

    /**
     * optional: give additional criterion instruction to evaluator
     * 
     */
    @JsonProperty("criterionNotes")
    public String getCriterionNotes() {
        return criterionNotes;
    }

    /**
     * optional: give additional criterion instruction to evaluator
     * 
     */
    @JsonProperty("criterionNotes")
    public void setCriterionNotes(String criterionNotes) {
        this.criterionNotes = criterionNotes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Criterion.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("criterionId");
        sb.append('=');
        sb.append(((this.criterionId == null)?"<null>":this.criterionId));
        sb.append(',');
        sb.append("levelId");
        sb.append('=');
        sb.append(((this.levelId == null)?"<null>":this.levelId));
        sb.append(',');
        sb.append("criterion");
        sb.append('=');
        sb.append(((this.criterion == null)?"<null>":this.criterion));
        sb.append(',');
        sb.append("criterionType");
        sb.append('=');
        sb.append(((this.criterionType == null)?"<null>":this.criterionType));
        sb.append(',');
        sb.append("position");
        sb.append('=');
        sb.append(((this.position == null)?"<null>":this.position));
        sb.append(',');
        sb.append("operator");
        sb.append('=');
        sb.append(((this.operator == null)?"<null>":this.operator));
        sb.append(',');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null)?"<null>":this.value));
        sb.append(',');
        sb.append("metric");
        sb.append('=');
        sb.append(((this.metric == null)?"<null>":this.metric));
        sb.append(',');
        sb.append("weight");
        sb.append('=');
        sb.append(((this.weight == null)?"<null>":this.weight));
        sb.append(',');
        sb.append("logic");
        sb.append('=');
        sb.append(((this.logic == null)?"<null>":this.logic));
        sb.append(',');
        sb.append("criterionNotes");
        sb.append('=');
        sb.append(((this.criterionNotes == null)?"<null>":this.criterionNotes));
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
        result = ((result* 31)+((this.criterionNotes == null)? 0 :this.criterionNotes.hashCode()));
        result = ((result* 31)+((this.criterion == null)? 0 :this.criterion.hashCode()));
        result = ((result* 31)+((this.criterionId == null)? 0 :this.criterionId.hashCode()));
        result = ((result* 31)+((this.criterionType == null)? 0 :this.criterionType.hashCode()));
        result = ((result* 31)+((this.metric == null)? 0 :this.metric.hashCode()));
        result = ((result* 31)+((this.levelId == null)? 0 :this.levelId.hashCode()));
        result = ((result* 31)+((this.weight == null)? 0 :this.weight.hashCode()));
        result = ((result* 31)+((this.position == null)? 0 :this.position.hashCode()));
        result = ((result* 31)+((this.logic == null)? 0 :this.logic.hashCode()));
        result = ((result* 31)+((this.value == null)? 0 :this.value.hashCode()));
        result = ((result* 31)+((this.operator == null)? 0 :this.operator.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Criterion) == false) {
            return false;
        }
        Criterion rhs = ((Criterion) other);
        return ((((((((((((this.criterionNotes == rhs.criterionNotes)||((this.criterionNotes!= null)&&this.criterionNotes.equals(rhs.criterionNotes)))&&((this.criterion == rhs.criterion)||((this.criterion!= null)&&this.criterion.equals(rhs.criterion))))&&((this.criterionId == rhs.criterionId)||((this.criterionId!= null)&&this.criterionId.equals(rhs.criterionId))))&&((this.criterionType == rhs.criterionType)||((this.criterionType!= null)&&this.criterionType.equals(rhs.criterionType))))&&((this.metric == rhs.metric)||((this.metric!= null)&&this.metric.equals(rhs.metric))))&&((this.levelId == rhs.levelId)||((this.levelId!= null)&&this.levelId.equals(rhs.levelId))))&&((this.weight == rhs.weight)||((this.weight!= null)&&this.weight.equals(rhs.weight))))&&((this.position == rhs.position)||((this.position!= null)&&this.position.equals(rhs.position))))&&((this.logic == rhs.logic)||((this.logic!= null)&&this.logic.equals(rhs.logic))))&&((this.value == rhs.value)||((this.value!= null)&&this.value.equals(rhs.value))))&&((this.operator == rhs.operator)||((this.operator!= null)&&this.operator.equals(rhs.operator))));
    }


    /**
     * required: the type of criterion
     * 
     */
    public enum CriterionType {

        CATEGORY("category"),
        DISTANCE("distance"),
        TIME("time"),
        COUNT("count"),
        MATCH("match"),
        CORRECT("correct"),
        RATIO("ratio");
        private final String value;
        private final static Map<String, Criterion.CriterionType> CONSTANTS = new HashMap<String, Criterion.CriterionType>();

        static {
            for (Criterion.CriterionType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CriterionType(String value) {
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
        public static Criterion.CriterionType fromValue(String value) {
            Criterion.CriterionType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: indicates if this criterion is to be measured with previous criterion in position order
     * 
     */
    public enum Logic {

        AND("AND"),
        OR("OR"),
        EXOR("EXOR"),
        NOT("NOT");
        private final String value;
        private final static Map<String, Criterion.Logic> CONSTANTS = new HashMap<String, Criterion.Logic>();

        static {
            for (Criterion.Logic c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Logic(String value) {
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
        public static Criterion.Logic fromValue(String value) {
            Criterion.Logic constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * optional: the unit of measure used
     * 
     */
    public enum Metric {

        METERS("meters"),
        CENTIMETERS("centimeters"),
        MILLIMETERS("millimeters"),
        MINUTES("minutes"),
        SECONDS("seconds"),
        MILLISECONDS("milliseconds"),
        PERCENT("percent"),
        PROBABILITY("probability"),
        ACTIVATED("activated"),
        DEACTIVATED("deactivated"),
        COMPLETED("completed"),
        DECIBELS("decibels"),
        LEVEL("level"),
        KM_HR("km/hr"),
        M_S("m/s"),
        SHOTS_SEC("shots/sec"),
        WORDS("words"),
        SHOTS("shots"),
        DEAD("dead"),
        WOUNDED("wounded"),
        MAX_FIRE("max-fire"),
        DEGREES("degrees");
        private final String value;
        private final static Map<String, Criterion.Metric> CONSTANTS = new HashMap<String, Criterion.Metric>();

        static {
            for (Criterion.Metric c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Metric(String value) {
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
        public static Criterion.Metric fromValue(String value) {
            Criterion.Metric constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: the evaluation operators used to compare data with value
     * 
     */
    public enum Operator {

        __EMPTY__("="),
        __EMPTY___(">"),
        __EMPTY____("<"),
        __EMPTY_____(">="),
        __EMPTY______("<="),
        __EMPTY_______("!=");
        private final String value;
        private final static Map<String, Criterion.Operator> CONSTANTS = new HashMap<String, Criterion.Operator>();

        static {
            for (Criterion.Operator c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Operator(String value) {
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
        public static Criterion.Operator fromValue(String value) {
            Criterion.Operator constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
