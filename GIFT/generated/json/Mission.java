
package generated.json;

import java.util.ArrayList;
import java.util.Date;
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
 * These can be Operational, Fragmentary or Warning types.  They drive and provide purpose to an experience.  Format is per FM 6-99
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "missionId",
    "missionUuid",
    "orderType",
    "dateTime",
    "sendingUnit",
    "orderNumber",
    "timeZone",
    "reference",
    "situation",
    "msnStatement",
    "execution",
    "sustainment",
    "commandSignal"
})
public class Mission {

    /**
     * required: unique mission id - locally assigned
     * 
     */
    @JsonProperty("missionId")
    @JsonPropertyDescription("required: unique mission id - locally assigned")
    private Object missionId;
    @JsonProperty("missionUuid")
    private String missionUuid;
    /**
     * Required: for query to search for specific order type
     * (Required)
     * 
     */
    @JsonProperty("orderType")
    @JsonPropertyDescription("Required: for query to search for specific order type")
    private Mission.OrderType orderType;
    /**
     * Required: DTG order is effective - use DDHHMMSSTZMONYR format
     * (Required)
     * 
     */
    @JsonProperty("dateTime")
    @JsonPropertyDescription("Required: DTG order is effective - use DDHHMMSSTZMONYR format")
    private Date dateTime;
    /**
     * Option: unit making order
     * (Required)
     * 
     */
    @JsonProperty("sendingUnit")
    @JsonPropertyDescription("Option: unit making order")
    private String sendingUnit;
    /**
     * Required: sequence of orders especially warno and frago
     * 
     */
    @JsonProperty("orderNumber")
    @JsonPropertyDescription("Required: sequence of orders especially warno and frago")
    private Integer orderNumber;
    /**
     * Option: time zone used for all times - same as DTG
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("Option: time zone used for all times - same as DTG")
    private String timeZone;
    /**
     * Option: only if change: directive or order this order references
     * 
     */
    @JsonProperty("reference")
    @JsonPropertyDescription("Option: only if change: directive or order this order references")
    private String reference;
    /**
     * Required: Provides the current situation that supports the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("situation")
    @JsonPropertyDescription("Required: Provides the current situation that supports the 'why' of an assignment")
    private Situation situation;
    /**
     * required: describes the mission elements of assignment
     * (Required)
     * 
     */
    @JsonProperty("msnStatement")
    @JsonPropertyDescription("required: describes the mission elements of assignment")
    private List<String> msnStatement = new ArrayList<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("execution")
    private Execution execution;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sustainment")
    private Sustainment sustainment;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commandSignal")
    private CommandSignal commandSignal;

    /**
     * required: unique mission id - locally assigned
     * 
     */
    @JsonProperty("missionId")
    public Object getMissionId() {
        return missionId;
    }

    /**
     * required: unique mission id - locally assigned
     * 
     */
    @JsonProperty("missionId")
    public void setMissionId(Object missionId) {
        this.missionId = missionId;
    }

    @JsonProperty("missionUuid")
    public String getMissionUuid() {
        return missionUuid;
    }

    @JsonProperty("missionUuid")
    public void setMissionUuid(String missionUuid) {
        this.missionUuid = missionUuid;
    }

    /**
     * Required: for query to search for specific order type
     * (Required)
     * 
     */
    @JsonProperty("orderType")
    public Mission.OrderType getOrderType() {
        return orderType;
    }

    /**
     * Required: for query to search for specific order type
     * (Required)
     * 
     */
    @JsonProperty("orderType")
    public void setOrderType(Mission.OrderType orderType) {
        this.orderType = orderType;
    }

    /**
     * Required: DTG order is effective - use DDHHMMSSTZMONYR format
     * (Required)
     * 
     */
    @JsonProperty("dateTime")
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * Required: DTG order is effective - use DDHHMMSSTZMONYR format
     * (Required)
     * 
     */
    @JsonProperty("dateTime")
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Option: unit making order
     * (Required)
     * 
     */
    @JsonProperty("sendingUnit")
    public String getSendingUnit() {
        return sendingUnit;
    }

    /**
     * Option: unit making order
     * (Required)
     * 
     */
    @JsonProperty("sendingUnit")
    public void setSendingUnit(String sendingUnit) {
        this.sendingUnit = sendingUnit;
    }

    /**
     * Required: sequence of orders especially warno and frago
     * 
     */
    @JsonProperty("orderNumber")
    public Integer getOrderNumber() {
        return orderNumber;
    }

    /**
     * Required: sequence of orders especially warno and frago
     * 
     */
    @JsonProperty("orderNumber")
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Option: time zone used for all times - same as DTG
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Option: time zone used for all times - same as DTG
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Option: only if change: directive or order this order references
     * 
     */
    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    /**
     * Option: only if change: directive or order this order references
     * 
     */
    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Required: Provides the current situation that supports the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("situation")
    public Situation getSituation() {
        return situation;
    }

    /**
     * Required: Provides the current situation that supports the 'why' of an assignment
     * (Required)
     * 
     */
    @JsonProperty("situation")
    public void setSituation(Situation situation) {
        this.situation = situation;
    }

    /**
     * required: describes the mission elements of assignment
     * (Required)
     * 
     */
    @JsonProperty("msnStatement")
    public List<String> getMsnStatement() {
        return msnStatement;
    }

    /**
     * required: describes the mission elements of assignment
     * (Required)
     * 
     */
    @JsonProperty("msnStatement")
    public void setMsnStatement(List<String> msnStatement) {
        this.msnStatement = msnStatement;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("execution")
    public Execution getExecution() {
        return execution;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("execution")
    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sustainment")
    public Sustainment getSustainment() {
        return sustainment;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sustainment")
    public void setSustainment(Sustainment sustainment) {
        this.sustainment = sustainment;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commandSignal")
    public CommandSignal getCommandSignal() {
        return commandSignal;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commandSignal")
    public void setCommandSignal(CommandSignal commandSignal) {
        this.commandSignal = commandSignal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Mission.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("missionId");
        sb.append('=');
        sb.append(((this.missionId == null)?"<null>":this.missionId));
        sb.append(',');
        sb.append("missionUuid");
        sb.append('=');
        sb.append(((this.missionUuid == null)?"<null>":this.missionUuid));
        sb.append(',');
        sb.append("orderType");
        sb.append('=');
        sb.append(((this.orderType == null)?"<null>":this.orderType));
        sb.append(',');
        sb.append("dateTime");
        sb.append('=');
        sb.append(((this.dateTime == null)?"<null>":this.dateTime));
        sb.append(',');
        sb.append("sendingUnit");
        sb.append('=');
        sb.append(((this.sendingUnit == null)?"<null>":this.sendingUnit));
        sb.append(',');
        sb.append("orderNumber");
        sb.append('=');
        sb.append(((this.orderNumber == null)?"<null>":this.orderNumber));
        sb.append(',');
        sb.append("timeZone");
        sb.append('=');
        sb.append(((this.timeZone == null)?"<null>":this.timeZone));
        sb.append(',');
        sb.append("reference");
        sb.append('=');
        sb.append(((this.reference == null)?"<null>":this.reference));
        sb.append(',');
        sb.append("situation");
        sb.append('=');
        sb.append(((this.situation == null)?"<null>":this.situation));
        sb.append(',');
        sb.append("msnStatement");
        sb.append('=');
        sb.append(((this.msnStatement == null)?"<null>":this.msnStatement));
        sb.append(',');
        sb.append("execution");
        sb.append('=');
        sb.append(((this.execution == null)?"<null>":this.execution));
        sb.append(',');
        sb.append("sustainment");
        sb.append('=');
        sb.append(((this.sustainment == null)?"<null>":this.sustainment));
        sb.append(',');
        sb.append("commandSignal");
        sb.append('=');
        sb.append(((this.commandSignal == null)?"<null>":this.commandSignal));
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
        result = ((result* 31)+((this.dateTime == null)? 0 :this.dateTime.hashCode()));
        result = ((result* 31)+((this.orderType == null)? 0 :this.orderType.hashCode()));
        result = ((result* 31)+((this.execution == null)? 0 :this.execution.hashCode()));
        result = ((result* 31)+((this.missionId == null)? 0 :this.missionId.hashCode()));
        result = ((result* 31)+((this.orderNumber == null)? 0 :this.orderNumber.hashCode()));
        result = ((result* 31)+((this.commandSignal == null)? 0 :this.commandSignal.hashCode()));
        result = ((result* 31)+((this.timeZone == null)? 0 :this.timeZone.hashCode()));
        result = ((result* 31)+((this.reference == null)? 0 :this.reference.hashCode()));
        result = ((result* 31)+((this.msnStatement == null)? 0 :this.msnStatement.hashCode()));
        result = ((result* 31)+((this.missionUuid == null)? 0 :this.missionUuid.hashCode()));
        result = ((result* 31)+((this.sendingUnit == null)? 0 :this.sendingUnit.hashCode()));
        result = ((result* 31)+((this.sustainment == null)? 0 :this.sustainment.hashCode()));
        result = ((result* 31)+((this.situation == null)? 0 :this.situation.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Mission) == false) {
            return false;
        }
        Mission rhs = ((Mission) other);
        return ((((((((((((((this.dateTime == rhs.dateTime)||((this.dateTime!= null)&&this.dateTime.equals(rhs.dateTime)))&&((this.orderType == rhs.orderType)||((this.orderType!= null)&&this.orderType.equals(rhs.orderType))))&&((this.execution == rhs.execution)||((this.execution!= null)&&this.execution.equals(rhs.execution))))&&((this.missionId == rhs.missionId)||((this.missionId!= null)&&this.missionId.equals(rhs.missionId))))&&((this.orderNumber == rhs.orderNumber)||((this.orderNumber!= null)&&this.orderNumber.equals(rhs.orderNumber))))&&((this.commandSignal == rhs.commandSignal)||((this.commandSignal!= null)&&this.commandSignal.equals(rhs.commandSignal))))&&((this.timeZone == rhs.timeZone)||((this.timeZone!= null)&&this.timeZone.equals(rhs.timeZone))))&&((this.reference == rhs.reference)||((this.reference!= null)&&this.reference.equals(rhs.reference))))&&((this.msnStatement == rhs.msnStatement)||((this.msnStatement!= null)&&this.msnStatement.equals(rhs.msnStatement))))&&((this.missionUuid == rhs.missionUuid)||((this.missionUuid!= null)&&this.missionUuid.equals(rhs.missionUuid))))&&((this.sendingUnit == rhs.sendingUnit)||((this.sendingUnit!= null)&&this.sendingUnit.equals(rhs.sendingUnit))))&&((this.sustainment == rhs.sustainment)||((this.sustainment!= null)&&this.sustainment.equals(rhs.sustainment))))&&((this.situation == rhs.situation)||((this.situation!= null)&&this.situation.equals(rhs.situation))));
    }


    /**
     * Required: for query to search for specific order type
     * 
     */
    public enum OrderType {

        OPERATION("Operation"),
        FRAGMENTARY("Fragmentary"),
        WARNING("Warning");
        private final String value;
        private final static Map<String, Mission.OrderType> CONSTANTS = new HashMap<String, Mission.OrderType>();

        static {
            for (Mission.OrderType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OrderType(String value) {
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
        public static Mission.OrderType fromValue(String value) {
            Mission.OrderType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
