
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
 * xEvent is a realistic experience used to reliably/consistently prompt, apply and measure taskwork and/or teamwork in different contexts and in different levels of difficulty (stress levels).  Prompts can be synthetic or live/human scripted adaptations or interventions activated through exercise triggers that are 'activated' by targeted actors using various methods
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "xEventId",
    "xEventUuid",
    "xEventName",
    "xEventType",
    "startTriggers",
    "endTriggers",
    "taskwork",
    "teamwork",
    "affects",
    "strategies",
    "activities",
    "dataSources",
    "dataFunctions",
    "initDifficulty",
    "stressLevel",
    "authors",
    "status",
    "changes",
    "references"
})
public class XEvent {

    /**
     * required: a unique local xEvent identifier
     * (Required)
     * 
     */
    @JsonProperty("xEventId")
    @JsonPropertyDescription("required: a unique local xEvent identifier")
    private Object xEventId;
    /**
     * required: the unique global xEvent identifier
     * 
     */
    @JsonProperty("xEventUuid")
    @JsonPropertyDescription("required: the unique global xEvent identifier")
    private String xEventUuid;
    /**
     * required: the name of this xevent - should be unique in its description
     * (Required)
     * 
     */
    @JsonProperty("xEventName")
    @JsonPropertyDescription("required: the name of this xevent - should be unique in its description")
    private String xEventName;
    /**
     * required: fixed or dynamic - how will this xEvent be injected into exercise
     * 
     */
    @JsonProperty("xEventType")
    @JsonPropertyDescription("required: fixed or dynamic - how will this xEvent be injected into exercise")
    private XEvent.XEventType xEventType;
    /**
     * required: a list of triggers that can activate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("startTriggers")
    @JsonPropertyDescription("required: a list of triggers that can activate this xEvent")
    private List<Object> startTriggers = new ArrayList<Object>();
    /**
     * required: a list of triggers that can deactivate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("endTriggers")
    @JsonPropertyDescription("required: a list of triggers that can deactivate this xEvent")
    private List<Object> endTriggers = new ArrayList<Object>();
    /**
     * optional: the performers and taskwork to perform
     * 
     */
    @JsonProperty("taskwork")
    @JsonPropertyDescription("optional: the performers and taskwork to perform")
    private List<TaskItem> taskwork = new ArrayList<TaskItem>();
    /**
     * optional: the team tasks and target teams to perform them
     * 
     */
    @JsonProperty("teamwork")
    @JsonPropertyDescription("optional: the team tasks and target teams to perform them")
    private List<TeamItem> teamwork = new ArrayList<TeamItem>();
    /**
     * optional: the affective states to demonstrate and incorporate into taskwork and teamwork
     * 
     */
    @JsonProperty("affects")
    @JsonPropertyDescription("optional: the affective states to demonstrate and incorporate into taskwork and teamwork")
    private List<AffectItem> affects = new ArrayList<AffectItem>();
    /**
     * optional: pointers to strategies to activate multiple activities and/or functions.
     * 
     */
    @JsonProperty("strategies")
    @JsonPropertyDescription("optional: pointers to strategies to activate multiple activities and/or functions.")
    private List<StrategyItem> strategies = new ArrayList<StrategyItem>();
    /**
     * optional: pointers to script(s) to activate to activate prompts, behaviors, adaptations, interventions
     * 
     */
    @JsonProperty("activities")
    @JsonPropertyDescription("optional: pointers to script(s) to activate to activate prompts, behaviors, adaptations, interventions")
    private List<ActivityItem> activities = new ArrayList<ActivityItem>();
    /**
     * optional: data evaluation sources to help measure xevent performance
     * 
     */
    @JsonProperty("dataSources")
    @JsonPropertyDescription("optional: data evaluation sources to help measure xevent performance")
    private Object dataSources;
    /**
     * optional: TSS function(s) to activate to automate data collection
     * 
     */
    @JsonProperty("dataFunctions")
    @JsonPropertyDescription("optional: TSS function(s) to activate to automate data collection")
    private List<FunctionItem> dataFunctions = new ArrayList<FunctionItem>();
    /**
     * optional: an baseline difficulty level (complexity) of the xEvent initially before or without any additional difficuty/complexity or stress items (activities) being enabled
     * 
     */
    @JsonProperty("initDifficulty")
    @JsonPropertyDescription("optional: an baseline difficulty level (complexity) of the xEvent initially before or without any additional difficuty/complexity or stress items (activities) being enabled")
    private Object initDifficulty;
    /**
     * optional: the stress level of the xEvent is a roll-up of the included activities that are triggered at start - does not include additional stress items/activities inserted by OCT - but those are accounted for in xAPI report.
     * 
     */
    @JsonProperty("stressLevel")
    @JsonPropertyDescription("optional: the stress level of the xEvent is a roll-up of the included activities that are triggered at start - does not include additional stress items/activities inserted by OCT - but those are accounted for in xAPI report.")
    private Object stressLevel;
    /**
     * required: the author(s) of this xEvent - including changes - to aid in translation or further description matters.  Should be automated by EDT
     * 
     */
    @JsonProperty("authors")
    @JsonPropertyDescription("required: the author(s) of this xEvent - including changes - to aid in translation or further description matters.  Should be automated by EDT")
    private List<Author> authors = new ArrayList<Author>();
    /**
     * required: the status of this xEvent to aid in query or selection.  Should be automated using external review tools
     * (Required)
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("required: the status of this xEvent to aid in query or selection.  Should be automated using external review tools")
    private Status status;
    /**
     * optional: the list of any changes made to master xEvent - should be automated using EDT
     * 
     */
    @JsonProperty("changes")
    @JsonPropertyDescription("optional: the list of any changes made to master xEvent - should be automated using EDT")
    private List<Object> changes = new ArrayList<Object>();
    /**
     * required: the tactical doctrine and/or battle drill this xEvent is intended to exercise and help measure
     * 
     */
    @JsonProperty("references")
    @JsonPropertyDescription("required: the tactical doctrine and/or battle drill this xEvent is intended to exercise and help measure")
    private List<Object> references = new ArrayList<Object>();

    /**
     * required: a unique local xEvent identifier
     * (Required)
     * 
     */
    @JsonProperty("xEventId")
    public Object getxEventId() {
        return xEventId;
    }

    /**
     * required: a unique local xEvent identifier
     * (Required)
     * 
     */
    @JsonProperty("xEventId")
    public void setxEventId(Object xEventId) {
        this.xEventId = xEventId;
    }

    /**
     * required: the unique global xEvent identifier
     * 
     */
    @JsonProperty("xEventUuid")
    public String getxEventUuid() {
        return xEventUuid;
    }

    /**
     * required: the unique global xEvent identifier
     * 
     */
    @JsonProperty("xEventUuid")
    public void setxEventUuid(String xEventUuid) {
        this.xEventUuid = xEventUuid;
    }

    /**
     * required: the name of this xevent - should be unique in its description
     * (Required)
     * 
     */
    @JsonProperty("xEventName")
    public String getxEventName() {
        return xEventName;
    }

    /**
     * required: the name of this xevent - should be unique in its description
     * (Required)
     * 
     */
    @JsonProperty("xEventName")
    public void setxEventName(String xEventName) {
        this.xEventName = xEventName;
    }

    /**
     * required: fixed or dynamic - how will this xEvent be injected into exercise
     * 
     */
    @JsonProperty("xEventType")
    public XEvent.XEventType getxEventType() {
        return xEventType;
    }

    /**
     * required: fixed or dynamic - how will this xEvent be injected into exercise
     * 
     */
    @JsonProperty("xEventType")
    public void setxEventType(XEvent.XEventType xEventType) {
        this.xEventType = xEventType;
    }

    /**
     * required: a list of triggers that can activate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("startTriggers")
    public List<Object> getStartTriggers() {
        return startTriggers;
    }

    /**
     * required: a list of triggers that can activate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("startTriggers")
    public void setStartTriggers(List<Object> startTriggers) {
        this.startTriggers = startTriggers;
    }

    /**
     * required: a list of triggers that can deactivate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("endTriggers")
    public List<Object> getEndTriggers() {
        return endTriggers;
    }

    /**
     * required: a list of triggers that can deactivate this xEvent
     * (Required)
     * 
     */
    @JsonProperty("endTriggers")
    public void setEndTriggers(List<Object> endTriggers) {
        this.endTriggers = endTriggers;
    }

    /**
     * optional: the performers and taskwork to perform
     * 
     */
    @JsonProperty("taskwork")
    public List<TaskItem> getTaskwork() {
        return taskwork;
    }

    /**
     * optional: the performers and taskwork to perform
     * 
     */
    @JsonProperty("taskwork")
    public void setTaskwork(List<TaskItem> taskwork) {
        this.taskwork = taskwork;
    }

    /**
     * optional: the team tasks and target teams to perform them
     * 
     */
    @JsonProperty("teamwork")
    public List<TeamItem> getTeamwork() {
        return teamwork;
    }

    /**
     * optional: the team tasks and target teams to perform them
     * 
     */
    @JsonProperty("teamwork")
    public void setTeamwork(List<TeamItem> teamwork) {
        this.teamwork = teamwork;
    }

    /**
     * optional: the affective states to demonstrate and incorporate into taskwork and teamwork
     * 
     */
    @JsonProperty("affects")
    public List<AffectItem> getAffects() {
        return affects;
    }

    /**
     * optional: the affective states to demonstrate and incorporate into taskwork and teamwork
     * 
     */
    @JsonProperty("affects")
    public void setAffects(List<AffectItem> affects) {
        this.affects = affects;
    }

    /**
     * optional: pointers to strategies to activate multiple activities and/or functions.
     * 
     */
    @JsonProperty("strategies")
    public List<StrategyItem> getStrategies() {
        return strategies;
    }

    /**
     * optional: pointers to strategies to activate multiple activities and/or functions.
     * 
     */
    @JsonProperty("strategies")
    public void setStrategies(List<StrategyItem> strategies) {
        this.strategies = strategies;
    }

    /**
     * optional: pointers to script(s) to activate to activate prompts, behaviors, adaptations, interventions
     * 
     */
    @JsonProperty("activities")
    public List<ActivityItem> getActivities() {
        return activities;
    }

    /**
     * optional: pointers to script(s) to activate to activate prompts, behaviors, adaptations, interventions
     * 
     */
    @JsonProperty("activities")
    public void setActivities(List<ActivityItem> activities) {
        this.activities = activities;
    }

    /**
     * optional: data evaluation sources to help measure xevent performance
     * 
     */
    @JsonProperty("dataSources")
    public Object getDataSources() {
        return dataSources;
    }

    /**
     * optional: data evaluation sources to help measure xevent performance
     * 
     */
    @JsonProperty("dataSources")
    public void setDataSources(Object dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * optional: TSS function(s) to activate to automate data collection
     * 
     */
    @JsonProperty("dataFunctions")
    public List<FunctionItem> getDataFunctions() {
        return dataFunctions;
    }

    /**
     * optional: TSS function(s) to activate to automate data collection
     * 
     */
    @JsonProperty("dataFunctions")
    public void setDataFunctions(List<FunctionItem> dataFunctions) {
        this.dataFunctions = dataFunctions;
    }

    /**
     * optional: an baseline difficulty level (complexity) of the xEvent initially before or without any additional difficuty/complexity or stress items (activities) being enabled
     * 
     */
    @JsonProperty("initDifficulty")
    public Object getInitDifficulty() {
        return initDifficulty;
    }

    /**
     * optional: an baseline difficulty level (complexity) of the xEvent initially before or without any additional difficuty/complexity or stress items (activities) being enabled
     * 
     */
    @JsonProperty("initDifficulty")
    public void setInitDifficulty(Object initDifficulty) {
        this.initDifficulty = initDifficulty;
    }

    /**
     * optional: the stress level of the xEvent is a roll-up of the included activities that are triggered at start - does not include additional stress items/activities inserted by OCT - but those are accounted for in xAPI report.
     * 
     */
    @JsonProperty("stressLevel")
    public Object getStressLevel() {
        return stressLevel;
    }

    /**
     * optional: the stress level of the xEvent is a roll-up of the included activities that are triggered at start - does not include additional stress items/activities inserted by OCT - but those are accounted for in xAPI report.
     * 
     */
    @JsonProperty("stressLevel")
    public void setStressLevel(Object stressLevel) {
        this.stressLevel = stressLevel;
    }

    /**
     * required: the author(s) of this xEvent - including changes - to aid in translation or further description matters.  Should be automated by EDT
     * 
     */
    @JsonProperty("authors")
    public List<Author> getAuthors() {
        return authors;
    }

    /**
     * required: the author(s) of this xEvent - including changes - to aid in translation or further description matters.  Should be automated by EDT
     * 
     */
    @JsonProperty("authors")
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    /**
     * required: the status of this xEvent to aid in query or selection.  Should be automated using external review tools
     * (Required)
     * 
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * required: the status of this xEvent to aid in query or selection.  Should be automated using external review tools
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * optional: the list of any changes made to master xEvent - should be automated using EDT
     * 
     */
    @JsonProperty("changes")
    public List<Object> getChanges() {
        return changes;
    }

    /**
     * optional: the list of any changes made to master xEvent - should be automated using EDT
     * 
     */
    @JsonProperty("changes")
    public void setChanges(List<Object> changes) {
        this.changes = changes;
    }

    /**
     * required: the tactical doctrine and/or battle drill this xEvent is intended to exercise and help measure
     * 
     */
    @JsonProperty("references")
    public List<Object> getReferences() {
        return references;
    }

    /**
     * required: the tactical doctrine and/or battle drill this xEvent is intended to exercise and help measure
     * 
     */
    @JsonProperty("references")
    public void setReferences(List<Object> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(XEvent.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("xEventId");
        sb.append('=');
        sb.append(((this.xEventId == null)?"<null>":this.xEventId));
        sb.append(',');
        sb.append("xEventUuid");
        sb.append('=');
        sb.append(((this.xEventUuid == null)?"<null>":this.xEventUuid));
        sb.append(',');
        sb.append("xEventName");
        sb.append('=');
        sb.append(((this.xEventName == null)?"<null>":this.xEventName));
        sb.append(',');
        sb.append("xEventType");
        sb.append('=');
        sb.append(((this.xEventType == null)?"<null>":this.xEventType));
        sb.append(',');
        sb.append("startTriggers");
        sb.append('=');
        sb.append(((this.startTriggers == null)?"<null>":this.startTriggers));
        sb.append(',');
        sb.append("endTriggers");
        sb.append('=');
        sb.append(((this.endTriggers == null)?"<null>":this.endTriggers));
        sb.append(',');
        sb.append("taskwork");
        sb.append('=');
        sb.append(((this.taskwork == null)?"<null>":this.taskwork));
        sb.append(',');
        sb.append("teamwork");
        sb.append('=');
        sb.append(((this.teamwork == null)?"<null>":this.teamwork));
        sb.append(',');
        sb.append("affects");
        sb.append('=');
        sb.append(((this.affects == null)?"<null>":this.affects));
        sb.append(',');
        sb.append("strategies");
        sb.append('=');
        sb.append(((this.strategies == null)?"<null>":this.strategies));
        sb.append(',');
        sb.append("activities");
        sb.append('=');
        sb.append(((this.activities == null)?"<null>":this.activities));
        sb.append(',');
        sb.append("dataSources");
        sb.append('=');
        sb.append(((this.dataSources == null)?"<null>":this.dataSources));
        sb.append(',');
        sb.append("dataFunctions");
        sb.append('=');
        sb.append(((this.dataFunctions == null)?"<null>":this.dataFunctions));
        sb.append(',');
        sb.append("initDifficulty");
        sb.append('=');
        sb.append(((this.initDifficulty == null)?"<null>":this.initDifficulty));
        sb.append(',');
        sb.append("stressLevel");
        sb.append('=');
        sb.append(((this.stressLevel == null)?"<null>":this.stressLevel));
        sb.append(',');
        sb.append("authors");
        sb.append('=');
        sb.append(((this.authors == null)?"<null>":this.authors));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("changes");
        sb.append('=');
        sb.append(((this.changes == null)?"<null>":this.changes));
        sb.append(',');
        sb.append("references");
        sb.append('=');
        sb.append(((this.references == null)?"<null>":this.references));
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
        result = ((result* 31)+((this.endTriggers == null)? 0 :this.endTriggers.hashCode()));
        result = ((result* 31)+((this.xEventUuid == null)? 0 :this.xEventUuid.hashCode()));
        result = ((result* 31)+((this.xEventType == null)? 0 :this.xEventType.hashCode()));
        result = ((result* 31)+((this.xEventId == null)? 0 :this.xEventId.hashCode()));
        result = ((result* 31)+((this.references == null)? 0 :this.references.hashCode()));
        result = ((result* 31)+((this.strategies == null)? 0 :this.strategies.hashCode()));
        result = ((result* 31)+((this.changes == null)? 0 :this.changes.hashCode()));
        result = ((result* 31)+((this.affects == null)? 0 :this.affects.hashCode()));
        result = ((result* 31)+((this.taskwork == null)? 0 :this.taskwork.hashCode()));
        result = ((result* 31)+((this.initDifficulty == null)? 0 :this.initDifficulty.hashCode()));
        result = ((result* 31)+((this.stressLevel == null)? 0 :this.stressLevel.hashCode()));
        result = ((result* 31)+((this.teamwork == null)? 0 :this.teamwork.hashCode()));
        result = ((result* 31)+((this.dataFunctions == null)? 0 :this.dataFunctions.hashCode()));
        result = ((result* 31)+((this.xEventName == null)? 0 :this.xEventName.hashCode()));
        result = ((result* 31)+((this.startTriggers == null)? 0 :this.startTriggers.hashCode()));
        result = ((result* 31)+((this.activities == null)? 0 :this.activities.hashCode()));
        result = ((result* 31)+((this.dataSources == null)? 0 :this.dataSources.hashCode()));
        result = ((result* 31)+((this.authors == null)? 0 :this.authors.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof XEvent) == false) {
            return false;
        }
        XEvent rhs = ((XEvent) other);
        return ((((((((((((((((((((this.endTriggers == rhs.endTriggers)||((this.endTriggers!= null)&&this.endTriggers.equals(rhs.endTriggers)))&&((this.xEventUuid == rhs.xEventUuid)||((this.xEventUuid!= null)&&this.xEventUuid.equals(rhs.xEventUuid))))&&((this.xEventType == rhs.xEventType)||((this.xEventType!= null)&&this.xEventType.equals(rhs.xEventType))))&&((this.xEventId == rhs.xEventId)||((this.xEventId!= null)&&this.xEventId.equals(rhs.xEventId))))&&((this.references == rhs.references)||((this.references!= null)&&this.references.equals(rhs.references))))&&((this.strategies == rhs.strategies)||((this.strategies!= null)&&this.strategies.equals(rhs.strategies))))&&((this.changes == rhs.changes)||((this.changes!= null)&&this.changes.equals(rhs.changes))))&&((this.affects == rhs.affects)||((this.affects!= null)&&this.affects.equals(rhs.affects))))&&((this.taskwork == rhs.taskwork)||((this.taskwork!= null)&&this.taskwork.equals(rhs.taskwork))))&&((this.initDifficulty == rhs.initDifficulty)||((this.initDifficulty!= null)&&this.initDifficulty.equals(rhs.initDifficulty))))&&((this.stressLevel == rhs.stressLevel)||((this.stressLevel!= null)&&this.stressLevel.equals(rhs.stressLevel))))&&((this.teamwork == rhs.teamwork)||((this.teamwork!= null)&&this.teamwork.equals(rhs.teamwork))))&&((this.dataFunctions == rhs.dataFunctions)||((this.dataFunctions!= null)&&this.dataFunctions.equals(rhs.dataFunctions))))&&((this.xEventName == rhs.xEventName)||((this.xEventName!= null)&&this.xEventName.equals(rhs.xEventName))))&&((this.startTriggers == rhs.startTriggers)||((this.startTriggers!= null)&&this.startTriggers.equals(rhs.startTriggers))))&&((this.activities == rhs.activities)||((this.activities!= null)&&this.activities.equals(rhs.activities))))&&((this.dataSources == rhs.dataSources)||((this.dataSources!= null)&&this.dataSources.equals(rhs.dataSources))))&&((this.authors == rhs.authors)||((this.authors!= null)&&this.authors.equals(rhs.authors))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }


    /**
     * required: fixed or dynamic - how will this xEvent be injected into exercise
     * 
     */
    public enum XEventType {

        FIXED("fixed"),
        DYNAMIC("dynamic");
        private final String value;
        private final static Map<String, XEvent.XEventType> CONSTANTS = new HashMap<String, XEvent.XEventType>();

        static {
            for (XEvent.XEventType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        XEventType(String value) {
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
        public static XEvent.XEventType fromValue(String value) {
            XEvent.XEventType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
