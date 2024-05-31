
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
 * functions are TSS routines that stream data in support of a specific task measure.  They are intended to support either live, semi-live sensors or full synthetic rendering engines that need to emulate a specific live sensor so measuresments across each training environment is the same.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "functionId",
    "functionUuid",
    "functionName",
    "functionType",
    "functionClass",
    "functionArguments"
})
public class Function {

    /**
     * required: a unique local function identifier
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    @JsonPropertyDescription("required: a unique local function identifier")
    private Object functionId;
    /**
     * optional: unique master script id for reuse
     * 
     */
    @JsonProperty("functionUuid")
    @JsonPropertyDescription("optional: unique master script id for reuse")
    private String functionUuid;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionName")
    private String functionName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionType")
    private Function.FunctionType functionType = Function.FunctionType.fromValue("DataSource");
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionClass")
    private Function.FunctionClass functionClass;
    /**
     * optional: list of parameters required by the script
     * (Required)
     * 
     */
    @JsonProperty("functionArguments")
    @JsonPropertyDescription("optional: list of parameters required by the script")
    private List<Object> functionArguments = new ArrayList<Object>();

    /**
     * required: a unique local function identifier
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    public Object getFunctionId() {
        return functionId;
    }

    /**
     * required: a unique local function identifier
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    public void setFunctionId(Object functionId) {
        this.functionId = functionId;
    }

    /**
     * optional: unique master script id for reuse
     * 
     */
    @JsonProperty("functionUuid")
    public String getFunctionUuid() {
        return functionUuid;
    }

    /**
     * optional: unique master script id for reuse
     * 
     */
    @JsonProperty("functionUuid")
    public void setFunctionUuid(String functionUuid) {
        this.functionUuid = functionUuid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionName")
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionName")
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionType")
    public Function.FunctionType getFunctionType() {
        return functionType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionType")
    public void setFunctionType(Function.FunctionType functionType) {
        this.functionType = functionType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionClass")
    public Function.FunctionClass getFunctionClass() {
        return functionClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionClass")
    public void setFunctionClass(Function.FunctionClass functionClass) {
        this.functionClass = functionClass;
    }

    /**
     * optional: list of parameters required by the script
     * (Required)
     * 
     */
    @JsonProperty("functionArguments")
    public List<Object> getFunctionArguments() {
        return functionArguments;
    }

    /**
     * optional: list of parameters required by the script
     * (Required)
     * 
     */
    @JsonProperty("functionArguments")
    public void setFunctionArguments(List<Object> functionArguments) {
        this.functionArguments = functionArguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Function.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("functionId");
        sb.append('=');
        sb.append(((this.functionId == null)?"<null>":this.functionId));
        sb.append(',');
        sb.append("functionUuid");
        sb.append('=');
        sb.append(((this.functionUuid == null)?"<null>":this.functionUuid));
        sb.append(',');
        sb.append("functionName");
        sb.append('=');
        sb.append(((this.functionName == null)?"<null>":this.functionName));
        sb.append(',');
        sb.append("functionType");
        sb.append('=');
        sb.append(((this.functionType == null)?"<null>":this.functionType));
        sb.append(',');
        sb.append("functionClass");
        sb.append('=');
        sb.append(((this.functionClass == null)?"<null>":this.functionClass));
        sb.append(',');
        sb.append("functionArguments");
        sb.append('=');
        sb.append(((this.functionArguments == null)?"<null>":this.functionArguments));
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
        result = ((result* 31)+((this.functionId == null)? 0 :this.functionId.hashCode()));
        result = ((result* 31)+((this.functionName == null)? 0 :this.functionName.hashCode()));
        result = ((result* 31)+((this.functionUuid == null)? 0 :this.functionUuid.hashCode()));
        result = ((result* 31)+((this.functionClass == null)? 0 :this.functionClass.hashCode()));
        result = ((result* 31)+((this.functionArguments == null)? 0 :this.functionArguments.hashCode()));
        result = ((result* 31)+((this.functionType == null)? 0 :this.functionType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Function) == false) {
            return false;
        }
        Function rhs = ((Function) other);
        return (((((((this.functionId == rhs.functionId)||((this.functionId!= null)&&this.functionId.equals(rhs.functionId)))&&((this.functionName == rhs.functionName)||((this.functionName!= null)&&this.functionName.equals(rhs.functionName))))&&((this.functionUuid == rhs.functionUuid)||((this.functionUuid!= null)&&this.functionUuid.equals(rhs.functionUuid))))&&((this.functionClass == rhs.functionClass)||((this.functionClass!= null)&&this.functionClass.equals(rhs.functionClass))))&&((this.functionArguments == rhs.functionArguments)||((this.functionArguments!= null)&&this.functionArguments.equals(rhs.functionArguments))))&&((this.functionType == rhs.functionType)||((this.functionType!= null)&&this.functionType.equals(rhs.functionType))));
    }

    public enum FunctionClass {

        GLOBAL("global"),
        LOCAL("local");
        private final String value;
        private final static Map<String, Function.FunctionClass> CONSTANTS = new HashMap<String, Function.FunctionClass>();

        static {
            for (Function.FunctionClass c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FunctionClass(String value) {
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
        public static Function.FunctionClass fromValue(String value) {
            Function.FunctionClass constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum FunctionType {

        BEHAVIOR("Behavior"),
        DATA_SOURCE("DataSource");
        private final String value;
        private final static Map<String, Function.FunctionType> CONSTANTS = new HashMap<String, Function.FunctionType>();

        static {
            for (Function.FunctionType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FunctionType(String value) {
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
        public static Function.FunctionType fromValue(String value) {
            Function.FunctionType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
