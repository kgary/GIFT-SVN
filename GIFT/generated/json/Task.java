
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
 * an approved unit of performance required by a team or individual team role or job in routine, abnormal or emergent conditions
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "taskId",
    "taskUuid",
    "compFramework",
    "competency",
    "taskTitle",
    "taskType",
    "cues",
    "safetyRisk",
    "taskMeasures",
    "supportTasks",
    "subTasks",
    "teamSkills",
    "supportAffects",
    "lrngResources"
})
public class Task {

    /**
     * required: a unique local-exercise-xevent task identifier
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    @JsonPropertyDescription("required: a unique local-exercise-xevent task identifier")
    private Object taskId;
    /**
     * optional: a unique global task identifier in semantic network
     * 
     */
    @JsonProperty("taskUuid")
    @JsonPropertyDescription("optional: a unique global task identifier in semantic network")
    private String taskUuid;
    /**
     * optional: the universal competency framework identifier that this task supports
     * 
     */
    @JsonProperty("compFramework")
    @JsonPropertyDescription("optional: the universal competency framework identifier that this task supports")
    private String compFramework;
    /**
     * optional: the universal competency identifier that provides evidence for this task result
     * 
     */
    @JsonProperty("competency")
    @JsonPropertyDescription("optional: the universal competency identifier that provides evidence for this task result")
    private String competency;
    /**
     * required: the given task title by the task proponent
     * (Required)
     * 
     */
    @JsonProperty("taskTitle")
    @JsonPropertyDescription("required: the given task title by the task proponent")
    private String taskTitle;
    /**
     * required: is task for team outcomes or individual role outcomes
     * (Required)
     * 
     */
    @JsonProperty("taskType")
    @JsonPropertyDescription("required: is task for team outcomes or individual role outcomes")
    private Task.TaskType taskType;
    /**
     * optional: the one or various triggers that together should activate a task
     * 
     */
    @JsonProperty("cues")
    @JsonPropertyDescription("optional: the one or various triggers that together should activate a task")
    private List<Object> cues = new ArrayList<Object>();
    /**
     * required: indicates the potential safety risk performing the task - tells OCT what to watch
     * (Required)
     * 
     */
    @JsonProperty("safetyRisk")
    @JsonPropertyDescription("required: indicates the potential safety risk performing the task - tells OCT what to watch")
    private Task.SafetyRisk safetyRisk;
    /**
     * optional: indicates the methods to measure the task
     * (Required)
     * 
     */
    @JsonProperty("taskMeasures")
    @JsonPropertyDescription("optional: indicates the methods to measure the task")
    private List<Measure> taskMeasures = new ArrayList<Measure>();
    /**
     * optional: tasks required to be completed by other roles to complete the task
     * 
     */
    @JsonProperty("supportTasks")
    @JsonPropertyDescription("optional: tasks required to be completed by other roles to complete the task")
    private List<Object> supportTasks = new ArrayList<Object>();
    /**
     * optional: sub-tasks required to complete the task
     * 
     */
    @JsonProperty("subTasks")
    @JsonPropertyDescription("optional: sub-tasks required to complete the task")
    private List<Object> subTasks = new ArrayList<Object>();
    /**
     * optional: if team task type then list each required teamskill
     * 
     */
    @JsonProperty("teamSkills")
    @JsonPropertyDescription("optional: if team task type then list each required teamskill")
    private List<Object> teamSkills = new ArrayList<Object>();
    /**
     * optional: affects needed to support task
     * 
     */
    @JsonProperty("supportAffects")
    @JsonPropertyDescription("optional: affects needed to support task")
    private List<Object> supportAffects = new ArrayList<Object>();
    /**
     * optional: learning resources that can be provided on demand to actors during crawl or training walk phases
     * 
     */
    @JsonProperty("lrngResources")
    @JsonPropertyDescription("optional: learning resources that can be provided on demand to actors during crawl or training walk phases")
    private List<Object> lrngResources = new ArrayList<Object>();

    /**
     * required: a unique local-exercise-xevent task identifier
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public Object getTaskId() {
        return taskId;
    }

    /**
     * required: a unique local-exercise-xevent task identifier
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Object taskId) {
        this.taskId = taskId;
    }

    /**
     * optional: a unique global task identifier in semantic network
     * 
     */
    @JsonProperty("taskUuid")
    public String getTaskUuid() {
        return taskUuid;
    }

    /**
     * optional: a unique global task identifier in semantic network
     * 
     */
    @JsonProperty("taskUuid")
    public void setTaskUuid(String taskUuid) {
        this.taskUuid = taskUuid;
    }

    /**
     * optional: the universal competency framework identifier that this task supports
     * 
     */
    @JsonProperty("compFramework")
    public String getCompFramework() {
        return compFramework;
    }

    /**
     * optional: the universal competency framework identifier that this task supports
     * 
     */
    @JsonProperty("compFramework")
    public void setCompFramework(String compFramework) {
        this.compFramework = compFramework;
    }

    /**
     * optional: the universal competency identifier that provides evidence for this task result
     * 
     */
    @JsonProperty("competency")
    public String getCompetency() {
        return competency;
    }

    /**
     * optional: the universal competency identifier that provides evidence for this task result
     * 
     */
    @JsonProperty("competency")
    public void setCompetency(String competency) {
        this.competency = competency;
    }

    /**
     * required: the given task title by the task proponent
     * (Required)
     * 
     */
    @JsonProperty("taskTitle")
    public String getTaskTitle() {
        return taskTitle;
    }

    /**
     * required: the given task title by the task proponent
     * (Required)
     * 
     */
    @JsonProperty("taskTitle")
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    /**
     * required: is task for team outcomes or individual role outcomes
     * (Required)
     * 
     */
    @JsonProperty("taskType")
    public Task.TaskType getTaskType() {
        return taskType;
    }

    /**
     * required: is task for team outcomes or individual role outcomes
     * (Required)
     * 
     */
    @JsonProperty("taskType")
    public void setTaskType(Task.TaskType taskType) {
        this.taskType = taskType;
    }

    /**
     * optional: the one or various triggers that together should activate a task
     * 
     */
    @JsonProperty("cues")
    public List<Object> getCues() {
        return cues;
    }

    /**
     * optional: the one or various triggers that together should activate a task
     * 
     */
    @JsonProperty("cues")
    public void setCues(List<Object> cues) {
        this.cues = cues;
    }

    /**
     * required: indicates the potential safety risk performing the task - tells OCT what to watch
     * (Required)
     * 
     */
    @JsonProperty("safetyRisk")
    public Task.SafetyRisk getSafetyRisk() {
        return safetyRisk;
    }

    /**
     * required: indicates the potential safety risk performing the task - tells OCT what to watch
     * (Required)
     * 
     */
    @JsonProperty("safetyRisk")
    public void setSafetyRisk(Task.SafetyRisk safetyRisk) {
        this.safetyRisk = safetyRisk;
    }

    /**
     * optional: indicates the methods to measure the task
     * (Required)
     * 
     */
    @JsonProperty("taskMeasures")
    public List<Measure> getTaskMeasures() {
        return taskMeasures;
    }

    /**
     * optional: indicates the methods to measure the task
     * (Required)
     * 
     */
    @JsonProperty("taskMeasures")
    public void setTaskMeasures(List<Measure> taskMeasures) {
        this.taskMeasures = taskMeasures;
    }

    /**
     * optional: tasks required to be completed by other roles to complete the task
     * 
     */
    @JsonProperty("supportTasks")
    public List<Object> getSupportTasks() {
        return supportTasks;
    }

    /**
     * optional: tasks required to be completed by other roles to complete the task
     * 
     */
    @JsonProperty("supportTasks")
    public void setSupportTasks(List<Object> supportTasks) {
        this.supportTasks = supportTasks;
    }

    /**
     * optional: sub-tasks required to complete the task
     * 
     */
    @JsonProperty("subTasks")
    public List<Object> getSubTasks() {
        return subTasks;
    }

    /**
     * optional: sub-tasks required to complete the task
     * 
     */
    @JsonProperty("subTasks")
    public void setSubTasks(List<Object> subTasks) {
        this.subTasks = subTasks;
    }

    /**
     * optional: if team task type then list each required teamskill
     * 
     */
    @JsonProperty("teamSkills")
    public List<Object> getTeamSkills() {
        return teamSkills;
    }

    /**
     * optional: if team task type then list each required teamskill
     * 
     */
    @JsonProperty("teamSkills")
    public void setTeamSkills(List<Object> teamSkills) {
        this.teamSkills = teamSkills;
    }

    /**
     * optional: affects needed to support task
     * 
     */
    @JsonProperty("supportAffects")
    public List<Object> getSupportAffects() {
        return supportAffects;
    }

    /**
     * optional: affects needed to support task
     * 
     */
    @JsonProperty("supportAffects")
    public void setSupportAffects(List<Object> supportAffects) {
        this.supportAffects = supportAffects;
    }

    /**
     * optional: learning resources that can be provided on demand to actors during crawl or training walk phases
     * 
     */
    @JsonProperty("lrngResources")
    public List<Object> getLrngResources() {
        return lrngResources;
    }

    /**
     * optional: learning resources that can be provided on demand to actors during crawl or training walk phases
     * 
     */
    @JsonProperty("lrngResources")
    public void setLrngResources(List<Object> lrngResources) {
        this.lrngResources = lrngResources;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Task.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("taskId");
        sb.append('=');
        sb.append(((this.taskId == null)?"<null>":this.taskId));
        sb.append(',');
        sb.append("taskUuid");
        sb.append('=');
        sb.append(((this.taskUuid == null)?"<null>":this.taskUuid));
        sb.append(',');
        sb.append("compFramework");
        sb.append('=');
        sb.append(((this.compFramework == null)?"<null>":this.compFramework));
        sb.append(',');
        sb.append("competency");
        sb.append('=');
        sb.append(((this.competency == null)?"<null>":this.competency));
        sb.append(',');
        sb.append("taskTitle");
        sb.append('=');
        sb.append(((this.taskTitle == null)?"<null>":this.taskTitle));
        sb.append(',');
        sb.append("taskType");
        sb.append('=');
        sb.append(((this.taskType == null)?"<null>":this.taskType));
        sb.append(',');
        sb.append("cues");
        sb.append('=');
        sb.append(((this.cues == null)?"<null>":this.cues));
        sb.append(',');
        sb.append("safetyRisk");
        sb.append('=');
        sb.append(((this.safetyRisk == null)?"<null>":this.safetyRisk));
        sb.append(',');
        sb.append("taskMeasures");
        sb.append('=');
        sb.append(((this.taskMeasures == null)?"<null>":this.taskMeasures));
        sb.append(',');
        sb.append("supportTasks");
        sb.append('=');
        sb.append(((this.supportTasks == null)?"<null>":this.supportTasks));
        sb.append(',');
        sb.append("subTasks");
        sb.append('=');
        sb.append(((this.subTasks == null)?"<null>":this.subTasks));
        sb.append(',');
        sb.append("teamSkills");
        sb.append('=');
        sb.append(((this.teamSkills == null)?"<null>":this.teamSkills));
        sb.append(',');
        sb.append("supportAffects");
        sb.append('=');
        sb.append(((this.supportAffects == null)?"<null>":this.supportAffects));
        sb.append(',');
        sb.append("lrngResources");
        sb.append('=');
        sb.append(((this.lrngResources == null)?"<null>":this.lrngResources));
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
        result = ((result* 31)+((this.competency == null)? 0 :this.competency.hashCode()));
        result = ((result* 31)+((this.subTasks == null)? 0 :this.subTasks.hashCode()));
        result = ((result* 31)+((this.teamSkills == null)? 0 :this.teamSkills.hashCode()));
        result = ((result* 31)+((this.taskUuid == null)? 0 :this.taskUuid.hashCode()));
        result = ((result* 31)+((this.cues == null)? 0 :this.cues.hashCode()));
        result = ((result* 31)+((this.taskType == null)? 0 :this.taskType.hashCode()));
        result = ((result* 31)+((this.supportTasks == null)? 0 :this.supportTasks.hashCode()));
        result = ((result* 31)+((this.lrngResources == null)? 0 :this.lrngResources.hashCode()));
        result = ((result* 31)+((this.safetyRisk == null)? 0 :this.safetyRisk.hashCode()));
        result = ((result* 31)+((this.taskMeasures == null)? 0 :this.taskMeasures.hashCode()));
        result = ((result* 31)+((this.taskTitle == null)? 0 :this.taskTitle.hashCode()));
        result = ((result* 31)+((this.supportAffects == null)? 0 :this.supportAffects.hashCode()));
        result = ((result* 31)+((this.compFramework == null)? 0 :this.compFramework.hashCode()));
        result = ((result* 31)+((this.taskId == null)? 0 :this.taskId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Task) == false) {
            return false;
        }
        Task rhs = ((Task) other);
        return (((((((((((((((this.competency == rhs.competency)||((this.competency!= null)&&this.competency.equals(rhs.competency)))&&((this.subTasks == rhs.subTasks)||((this.subTasks!= null)&&this.subTasks.equals(rhs.subTasks))))&&((this.teamSkills == rhs.teamSkills)||((this.teamSkills!= null)&&this.teamSkills.equals(rhs.teamSkills))))&&((this.taskUuid == rhs.taskUuid)||((this.taskUuid!= null)&&this.taskUuid.equals(rhs.taskUuid))))&&((this.cues == rhs.cues)||((this.cues!= null)&&this.cues.equals(rhs.cues))))&&((this.taskType == rhs.taskType)||((this.taskType!= null)&&this.taskType.equals(rhs.taskType))))&&((this.supportTasks == rhs.supportTasks)||((this.supportTasks!= null)&&this.supportTasks.equals(rhs.supportTasks))))&&((this.lrngResources == rhs.lrngResources)||((this.lrngResources!= null)&&this.lrngResources.equals(rhs.lrngResources))))&&((this.safetyRisk == rhs.safetyRisk)||((this.safetyRisk!= null)&&this.safetyRisk.equals(rhs.safetyRisk))))&&((this.taskMeasures == rhs.taskMeasures)||((this.taskMeasures!= null)&&this.taskMeasures.equals(rhs.taskMeasures))))&&((this.taskTitle == rhs.taskTitle)||((this.taskTitle!= null)&&this.taskTitle.equals(rhs.taskTitle))))&&((this.supportAffects == rhs.supportAffects)||((this.supportAffects!= null)&&this.supportAffects.equals(rhs.supportAffects))))&&((this.compFramework == rhs.compFramework)||((this.compFramework!= null)&&this.compFramework.equals(rhs.compFramework))))&&((this.taskId == rhs.taskId)||((this.taskId!= null)&&this.taskId.equals(rhs.taskId))));
    }


    /**
     * required: indicates the potential safety risk performing the task - tells OCT what to watch
     * 
     */
    public enum SafetyRisk {

        LOW("low"),
        MEDIUM("medium"),
        HIGH("high");
        private final String value;
        private final static Map<String, Task.SafetyRisk> CONSTANTS = new HashMap<String, Task.SafetyRisk>();

        static {
            for (Task.SafetyRisk c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SafetyRisk(String value) {
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
        public static Task.SafetyRisk fromValue(String value) {
            Task.SafetyRisk constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: is task for team outcomes or individual role outcomes
     * 
     */
    public enum TaskType {

        TEAM("team"),
        INDIVIDUAL("individual");
        private final String value;
        private final static Map<String, Task.TaskType> CONSTANTS = new HashMap<String, Task.TaskType>();

        static {
            for (Task.TaskType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TaskType(String value) {
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
        public static Task.TaskType fromValue(String value) {
            Task.TaskType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
