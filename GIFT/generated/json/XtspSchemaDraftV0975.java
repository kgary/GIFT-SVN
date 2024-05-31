
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Experiential Training Support Package (XTSP)
 * <p>
 * A research model for developing a sharable experiential learning session (experience) and specific actor, activity, condition learning events for use with the US Army Synthetic Training Environment training management tools (TMT).  Includes specifications for measures, criteria, and data required to validate competence in a task.  Originally conceived in research conducted for US Navy (2009-2017), then based on US Army TRADOC Pamplet 350-70-1 (TSP reference), and parts of SISO-STD-007-2008 Standard Military Scenario Definition Language (MSDL). 9.7.4: Changed all map coordinates to comply with GeoJSON format.  Made 'anchor' term a point that will serve as an evaluation overlay pivot point.  9.7.4.1: Make measure data sources a pointer to a central array of common data resources (resolution, recording speed, etc...).  Check for compliance with Army public releasable publications.  Still pending final SME review and evaluation.  9.7.4.2: Add various semantic changes as well as format changes to elements like actor, role, equipmentItem, dataSource, taskMeasure, trigger, criteria.  9.7.5: Made changes to general information
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Identification",
    "Missions",
    "Environment",
    "Equipment",
    "Actors",
    "Roles",
    "Teams",
    "Units",
    "ForceSides",
    "Overlays",
    "Levels",
    "DataSources",
    "LearningResources",
    "Tasks",
    "TeamSkills",
    "Affects",
    "TransitionStates",
    "Triggers",
    "Strategies",
    "Activities",
    "Functions",
    "XEvents",
    "References"
})
public class XtspSchemaDraftV0975 {

    /**
     * this is the information that a search engine will look at during exercise query and display.  It also provides administrative information regarding the exercise maintenance and access
     * (Required)
     * 
     */
    @JsonProperty("Identification")
    @JsonPropertyDescription("this is the information that a search engine will look at during exercise query and display.  It also provides administrative information regarding the exercise maintenance and access")
    private GenInfo identification;
    /**
     * This is a list of assigned orders to be used during experience to support different xEvents
     * (Required)
     * 
     */
    @JsonProperty("Missions")
    @JsonPropertyDescription("This is a list of assigned orders to be used during experience to support different xEvents")
    private List<Mission> missions = new ArrayList<Mission>();
    /**
     * This is the overall playable map or real space that an exercise will occur in.  Will consist of three layers consisting of a real-world map, an exericse-based map, and an exercise-based parameters
     * (Required)
     * 
     */
    @JsonProperty("Environment")
    @JsonPropertyDescription("This is the overall playable map or real space that an exercise will occur in.  Will consist of three layers consisting of a real-world map, an exericse-based map, and an exercise-based parameters")
    private Environment environment;
    @JsonProperty("Equipment")
    private List<EquipmentItem> equipment = new ArrayList<EquipmentItem>();
    /**
     * These are pre-set AI actors or pre-schedule real actors that are assigned to specific roles in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Actors")
    @JsonPropertyDescription("These are pre-set AI actors or pre-schedule real actors that are assigned to specific roles in the Force Structure.")
    private List<Actor> actors = new ArrayList<Actor>();
    /**
     * These are roles that actors are assigned to support teams or units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Roles")
    @JsonPropertyDescription("These are roles that actors are assigned to support teams or units in the Force Structure.")
    private List<Role> roles = new ArrayList<Role>();
    /**
     * These are teams assigned to units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Teams")
    @JsonPropertyDescription("These are teams assigned to units in the Force Structure.")
    private List<Team> teams = new ArrayList<Team>();
    /**
     * These are units assigned to a force side organization in the Force Structure.
     * 
     */
    @JsonProperty("Units")
    @JsonPropertyDescription("These are units assigned to a force side organization in the Force Structure.")
    private List<Unit> units = new ArrayList<Unit>();
    /**
     * The defined players and AI sides of any given exercise that defines the target teams (BLUFOR), opposing teams (OPFOR), neutral teams (NEUFOR), civilian populace, and animal population
     * (Required)
     * 
     */
    @JsonProperty("ForceSides")
    @JsonPropertyDescription("The defined players and AI sides of any given exercise that defines the target teams (BLUFOR), opposing teams (OPFOR), neutral teams (NEUFOR), civilian populace, and animal population")
    private List<Side> forceSides = new ArrayList<Side>();
    /**
     * These are visual indicators used for describing to XDT designer and player mission elements, entity parameters, trigger points and criteria elements.
     * 
     */
    @JsonProperty("Overlays")
    @JsonPropertyDescription("These are visual indicators used for describing to XDT designer and player mission elements, entity parameters, trigger points and criteria elements.")
    private List<Overlay> overlays = new ArrayList<Overlay>();
    /**
     * These are the levels to be used with this xTSP for assigning performance merit.
     * (Required)
     * 
     */
    @JsonProperty("Levels")
    @JsonPropertyDescription("These are the levels to be used with this xTSP for assigning performance merit.")
    private List<Level> levels = new ArrayList<Level>();
    /**
     * These are the  sources data to measure performance evidence from.
     * 
     */
    @JsonProperty("DataSources")
    @JsonPropertyDescription("These are the  sources data to measure performance evidence from.")
    private List<DataSource> dataSources = new ArrayList<DataSource>();
    /**
     * A listing of all learning resources to support each task - crawl and walk phase only
     * 
     */
    @JsonProperty("LearningResources")
    @JsonPropertyDescription("A listing of all learning resources to support each task - crawl and walk phase only")
    private List<LrngRsrc> learningResources = new ArrayList<LrngRsrc>();
    /**
     * These are the tasks that will be formatively or summatively evaluated using various task measures.
     * 
     */
    @JsonProperty("Tasks")
    @JsonPropertyDescription("These are the tasks that will be formatively or summatively evaluated using various task measures.")
    private List<Task> tasks = new ArrayList<Task>();
    /**
     * These are the team dimensions that will be formatively or summatively evaluated using various measures.
     * 
     */
    @JsonProperty("TeamSkills")
    @JsonPropertyDescription("These are the team dimensions that will be formatively or summatively evaluated using various measures.")
    private List<TeamSkill> teamSkills = new ArrayList<TeamSkill>();
    /**
     * These are the affects that will be formatively and summatively evaluated using various measures.
     * 
     */
    @JsonProperty("Affects")
    @JsonPropertyDescription("These are the affects that will be formatively and summatively evaluated using various measures.")
    private List<Affect> affects = new ArrayList<Affect>();
    /**
     * These are the expected transistions that will be used as trigger strategies and activities
     * 
     */
    @JsonProperty("TransitionStates")
    @JsonPropertyDescription("These are the expected transistions that will be used as trigger strategies and activities")
    private List<Transition> transitionStates = new ArrayList<Transition>();
    /**
     * A listing of all triggers/types that will be used in the exercise for various functions
     * 
     */
    @JsonProperty("Triggers")
    @JsonPropertyDescription("A listing of all triggers/types that will be used in the exercise for various functions")
    private List<Trigger> triggers = new ArrayList<Trigger>();
    /**
     * A listing of all pre-defined strategies that need to be read-in and used in the exercise
     * 
     */
    @JsonProperty("Strategies")
    @JsonPropertyDescription("A listing of all pre-defined strategies that need to be read-in and used in the exercise")
    private List<Strategy> strategies = new ArrayList<Strategy>();
    /**
     * A listing of all pre-defined activities that need to be used for triggers
     * 
     */
    @JsonProperty("Activities")
    @JsonPropertyDescription("A listing of all pre-defined activities that need to be used for triggers")
    private List<Activity__1> activities = new ArrayList<Activity__1>();
    /**
     * A listing of all training enviornment defined data functions that are to be activated globally or locally as part of an xevent
     * 
     */
    @JsonProperty("Functions")
    @JsonPropertyDescription("A listing of all training enviornment defined data functions that are to be activated globally or locally as part of an xevent")
    private List<Function> functions = new ArrayList<Function>();
    /**
     * A listing of all experience events that will be used in the exercise to prompt and measure specific tasks for specific teams / actors
     * 
     */
    @JsonProperty("XEvents")
    @JsonPropertyDescription("A listing of all experience events that will be used in the exercise to prompt and measure specific tasks for specific teams / actors")
    private List<XEvent> xEvents = new ArrayList<XEvent>();
    /**
     * A listing of all references supporting this exercise
     * (Required)
     * 
     */
    @JsonProperty("References")
    @JsonPropertyDescription("A listing of all references supporting this exercise")
    private List<Reference> references = new ArrayList<Reference>();

    /**
     * this is the information that a search engine will look at during exercise query and display.  It also provides administrative information regarding the exercise maintenance and access
     * (Required)
     * 
     */
    @JsonProperty("Identification")
    public GenInfo getIdentification() {
        return identification;
    }

    /**
     * this is the information that a search engine will look at during exercise query and display.  It also provides administrative information regarding the exercise maintenance and access
     * (Required)
     * 
     */
    @JsonProperty("Identification")
    public void setIdentification(GenInfo identification) {
        this.identification = identification;
    }

    /**
     * This is a list of assigned orders to be used during experience to support different xEvents
     * (Required)
     * 
     */
    @JsonProperty("Missions")
    public List<Mission> getMissions() {
        return missions;
    }

    /**
     * This is a list of assigned orders to be used during experience to support different xEvents
     * (Required)
     * 
     */
    @JsonProperty("Missions")
    public void setMissions(List<Mission> missions) {
        this.missions = missions;
    }

    /**
     * This is the overall playable map or real space that an exercise will occur in.  Will consist of three layers consisting of a real-world map, an exericse-based map, and an exercise-based parameters
     * (Required)
     * 
     */
    @JsonProperty("Environment")
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * This is the overall playable map or real space that an exercise will occur in.  Will consist of three layers consisting of a real-world map, an exericse-based map, and an exercise-based parameters
     * (Required)
     * 
     */
    @JsonProperty("Environment")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @JsonProperty("Equipment")
    public List<EquipmentItem> getEquipment() {
        return equipment;
    }

    @JsonProperty("Equipment")
    public void setEquipment(List<EquipmentItem> equipment) {
        this.equipment = equipment;
    }

    /**
     * These are pre-set AI actors or pre-schedule real actors that are assigned to specific roles in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Actors")
    public List<Actor> getActors() {
        return actors;
    }

    /**
     * These are pre-set AI actors or pre-schedule real actors that are assigned to specific roles in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Actors")
    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    /**
     * These are roles that actors are assigned to support teams or units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Roles")
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * These are roles that actors are assigned to support teams or units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Roles")
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * These are teams assigned to units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Teams")
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * These are teams assigned to units in the Force Structure.
     * (Required)
     * 
     */
    @JsonProperty("Teams")
    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    /**
     * These are units assigned to a force side organization in the Force Structure.
     * 
     */
    @JsonProperty("Units")
    public List<Unit> getUnits() {
        return units;
    }

    /**
     * These are units assigned to a force side organization in the Force Structure.
     * 
     */
    @JsonProperty("Units")
    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    /**
     * The defined players and AI sides of any given exercise that defines the target teams (BLUFOR), opposing teams (OPFOR), neutral teams (NEUFOR), civilian populace, and animal population
     * (Required)
     * 
     */
    @JsonProperty("ForceSides")
    public List<Side> getForceSides() {
        return forceSides;
    }

    /**
     * The defined players and AI sides of any given exercise that defines the target teams (BLUFOR), opposing teams (OPFOR), neutral teams (NEUFOR), civilian populace, and animal population
     * (Required)
     * 
     */
    @JsonProperty("ForceSides")
    public void setForceSides(List<Side> forceSides) {
        this.forceSides = forceSides;
    }

    /**
     * These are visual indicators used for describing to XDT designer and player mission elements, entity parameters, trigger points and criteria elements.
     * 
     */
    @JsonProperty("Overlays")
    public List<Overlay> getOverlays() {
        return overlays;
    }

    /**
     * These are visual indicators used for describing to XDT designer and player mission elements, entity parameters, trigger points and criteria elements.
     * 
     */
    @JsonProperty("Overlays")
    public void setOverlays(List<Overlay> overlays) {
        this.overlays = overlays;
    }

    /**
     * These are the levels to be used with this xTSP for assigning performance merit.
     * (Required)
     * 
     */
    @JsonProperty("Levels")
    public List<Level> getLevels() {
        return levels;
    }

    /**
     * These are the levels to be used with this xTSP for assigning performance merit.
     * (Required)
     * 
     */
    @JsonProperty("Levels")
    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    /**
     * These are the  sources data to measure performance evidence from.
     * 
     */
    @JsonProperty("DataSources")
    public List<DataSource> getDataSources() {
        return dataSources;
    }

    /**
     * These are the  sources data to measure performance evidence from.
     * 
     */
    @JsonProperty("DataSources")
    public void setDataSources(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * A listing of all learning resources to support each task - crawl and walk phase only
     * 
     */
    @JsonProperty("LearningResources")
    public List<LrngRsrc> getLearningResources() {
        return learningResources;
    }

    /**
     * A listing of all learning resources to support each task - crawl and walk phase only
     * 
     */
    @JsonProperty("LearningResources")
    public void setLearningResources(List<LrngRsrc> learningResources) {
        this.learningResources = learningResources;
    }

    /**
     * These are the tasks that will be formatively or summatively evaluated using various task measures.
     * 
     */
    @JsonProperty("Tasks")
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * These are the tasks that will be formatively or summatively evaluated using various task measures.
     * 
     */
    @JsonProperty("Tasks")
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * These are the team dimensions that will be formatively or summatively evaluated using various measures.
     * 
     */
    @JsonProperty("TeamSkills")
    public List<TeamSkill> getTeamSkills() {
        return teamSkills;
    }

    /**
     * These are the team dimensions that will be formatively or summatively evaluated using various measures.
     * 
     */
    @JsonProperty("TeamSkills")
    public void setTeamSkills(List<TeamSkill> teamSkills) {
        this.teamSkills = teamSkills;
    }

    /**
     * These are the affects that will be formatively and summatively evaluated using various measures.
     * 
     */
    @JsonProperty("Affects")
    public List<Affect> getAffects() {
        return affects;
    }

    /**
     * These are the affects that will be formatively and summatively evaluated using various measures.
     * 
     */
    @JsonProperty("Affects")
    public void setAffects(List<Affect> affects) {
        this.affects = affects;
    }

    /**
     * These are the expected transistions that will be used as trigger strategies and activities
     * 
     */
    @JsonProperty("TransitionStates")
    public List<Transition> getTransitionStates() {
        return transitionStates;
    }

    /**
     * These are the expected transistions that will be used as trigger strategies and activities
     * 
     */
    @JsonProperty("TransitionStates")
    public void setTransitionStates(List<Transition> transitionStates) {
        this.transitionStates = transitionStates;
    }

    /**
     * A listing of all triggers/types that will be used in the exercise for various functions
     * 
     */
    @JsonProperty("Triggers")
    public List<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * A listing of all triggers/types that will be used in the exercise for various functions
     * 
     */
    @JsonProperty("Triggers")
    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * A listing of all pre-defined strategies that need to be read-in and used in the exercise
     * 
     */
    @JsonProperty("Strategies")
    public List<Strategy> getStrategies() {
        return strategies;
    }

    /**
     * A listing of all pre-defined strategies that need to be read-in and used in the exercise
     * 
     */
    @JsonProperty("Strategies")
    public void setStrategies(List<Strategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * A listing of all pre-defined activities that need to be used for triggers
     * 
     */
    @JsonProperty("Activities")
    public List<Activity__1> getActivities() {
        return activities;
    }

    /**
     * A listing of all pre-defined activities that need to be used for triggers
     * 
     */
    @JsonProperty("Activities")
    public void setActivities(List<Activity__1> activities) {
        this.activities = activities;
    }

    /**
     * A listing of all training enviornment defined data functions that are to be activated globally or locally as part of an xevent
     * 
     */
    @JsonProperty("Functions")
    public List<Function> getFunctions() {
        return functions;
    }

    /**
     * A listing of all training enviornment defined data functions that are to be activated globally or locally as part of an xevent
     * 
     */
    @JsonProperty("Functions")
    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    /**
     * A listing of all experience events that will be used in the exercise to prompt and measure specific tasks for specific teams / actors
     * 
     */
    @JsonProperty("XEvents")
    public List<XEvent> getXEvents() {
        return xEvents;
    }

    /**
     * A listing of all experience events that will be used in the exercise to prompt and measure specific tasks for specific teams / actors
     * 
     */
    @JsonProperty("XEvents")
    public void setXEvents(List<XEvent> xEvents) {
        this.xEvents = xEvents;
    }

    /**
     * A listing of all references supporting this exercise
     * (Required)
     * 
     */
    @JsonProperty("References")
    public List<Reference> getReferences() {
        return references;
    }

    /**
     * A listing of all references supporting this exercise
     * (Required)
     * 
     */
    @JsonProperty("References")
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(XtspSchemaDraftV0975 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("identification");
        sb.append('=');
        sb.append(((this.identification == null)?"<null>":this.identification));
        sb.append(',');
        sb.append("missions");
        sb.append('=');
        sb.append(((this.missions == null)?"<null>":this.missions));
        sb.append(',');
        sb.append("environment");
        sb.append('=');
        sb.append(((this.environment == null)?"<null>":this.environment));
        sb.append(',');
        sb.append("equipment");
        sb.append('=');
        sb.append(((this.equipment == null)?"<null>":this.equipment));
        sb.append(',');
        sb.append("actors");
        sb.append('=');
        sb.append(((this.actors == null)?"<null>":this.actors));
        sb.append(',');
        sb.append("roles");
        sb.append('=');
        sb.append(((this.roles == null)?"<null>":this.roles));
        sb.append(',');
        sb.append("teams");
        sb.append('=');
        sb.append(((this.teams == null)?"<null>":this.teams));
        sb.append(',');
        sb.append("units");
        sb.append('=');
        sb.append(((this.units == null)?"<null>":this.units));
        sb.append(',');
        sb.append("forceSides");
        sb.append('=');
        sb.append(((this.forceSides == null)?"<null>":this.forceSides));
        sb.append(',');
        sb.append("overlays");
        sb.append('=');
        sb.append(((this.overlays == null)?"<null>":this.overlays));
        sb.append(',');
        sb.append("levels");
        sb.append('=');
        sb.append(((this.levels == null)?"<null>":this.levels));
        sb.append(',');
        sb.append("dataSources");
        sb.append('=');
        sb.append(((this.dataSources == null)?"<null>":this.dataSources));
        sb.append(',');
        sb.append("learningResources");
        sb.append('=');
        sb.append(((this.learningResources == null)?"<null>":this.learningResources));
        sb.append(',');
        sb.append("tasks");
        sb.append('=');
        sb.append(((this.tasks == null)?"<null>":this.tasks));
        sb.append(',');
        sb.append("teamSkills");
        sb.append('=');
        sb.append(((this.teamSkills == null)?"<null>":this.teamSkills));
        sb.append(',');
        sb.append("affects");
        sb.append('=');
        sb.append(((this.affects == null)?"<null>":this.affects));
        sb.append(',');
        sb.append("transitionStates");
        sb.append('=');
        sb.append(((this.transitionStates == null)?"<null>":this.transitionStates));
        sb.append(',');
        sb.append("triggers");
        sb.append('=');
        sb.append(((this.triggers == null)?"<null>":this.triggers));
        sb.append(',');
        sb.append("strategies");
        sb.append('=');
        sb.append(((this.strategies == null)?"<null>":this.strategies));
        sb.append(',');
        sb.append("activities");
        sb.append('=');
        sb.append(((this.activities == null)?"<null>":this.activities));
        sb.append(',');
        sb.append("functions");
        sb.append('=');
        sb.append(((this.functions == null)?"<null>":this.functions));
        sb.append(',');
        sb.append("xEvents");
        sb.append('=');
        sb.append(((this.xEvents == null)?"<null>":this.xEvents));
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
        result = ((result* 31)+((this.teams == null)? 0 :this.teams.hashCode()));
        result = ((result* 31)+((this.functions == null)? 0 :this.functions.hashCode()));
        result = ((result* 31)+((this.references == null)? 0 :this.references.hashCode()));
        result = ((result* 31)+((this.strategies == null)? 0 :this.strategies.hashCode()));
        result = ((result* 31)+((this.roles == null)? 0 :this.roles.hashCode()));
        result = ((result* 31)+((this.teamSkills == null)? 0 :this.teamSkills.hashCode()));
        result = ((result* 31)+((this.equipment == null)? 0 :this.equipment.hashCode()));
        result = ((result* 31)+((this.affects == null)? 0 :this.affects.hashCode()));
        result = ((result* 31)+((this.xEvents == null)? 0 :this.xEvents.hashCode()));
        result = ((result* 31)+((this.units == null)? 0 :this.units.hashCode()));
        result = ((result* 31)+((this.forceSides == null)? 0 :this.forceSides.hashCode()));
        result = ((result* 31)+((this.triggers == null)? 0 :this.triggers.hashCode()));
        result = ((result* 31)+((this.transitionStates == null)? 0 :this.transitionStates.hashCode()));
        result = ((result* 31)+((this.learningResources == null)? 0 :this.learningResources.hashCode()));
        result = ((result* 31)+((this.actors == null)? 0 :this.actors.hashCode()));
        result = ((result* 31)+((this.environment == null)? 0 :this.environment.hashCode()));
        result = ((result* 31)+((this.identification == null)? 0 :this.identification.hashCode()));
        result = ((result* 31)+((this.missions == null)? 0 :this.missions.hashCode()));
        result = ((result* 31)+((this.activities == null)? 0 :this.activities.hashCode()));
        result = ((result* 31)+((this.overlays == null)? 0 :this.overlays.hashCode()));
        result = ((result* 31)+((this.dataSources == null)? 0 :this.dataSources.hashCode()));
        result = ((result* 31)+((this.levels == null)? 0 :this.levels.hashCode()));
        result = ((result* 31)+((this.tasks == null)? 0 :this.tasks.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof XtspSchemaDraftV0975) == false) {
            return false;
        }
        XtspSchemaDraftV0975 rhs = ((XtspSchemaDraftV0975) other);
        return ((((((((((((((((((((((((this.teams == rhs.teams)||((this.teams!= null)&&this.teams.equals(rhs.teams)))&&((this.functions == rhs.functions)||((this.functions!= null)&&this.functions.equals(rhs.functions))))&&((this.references == rhs.references)||((this.references!= null)&&this.references.equals(rhs.references))))&&((this.strategies == rhs.strategies)||((this.strategies!= null)&&this.strategies.equals(rhs.strategies))))&&((this.roles == rhs.roles)||((this.roles!= null)&&this.roles.equals(rhs.roles))))&&((this.teamSkills == rhs.teamSkills)||((this.teamSkills!= null)&&this.teamSkills.equals(rhs.teamSkills))))&&((this.equipment == rhs.equipment)||((this.equipment!= null)&&this.equipment.equals(rhs.equipment))))&&((this.affects == rhs.affects)||((this.affects!= null)&&this.affects.equals(rhs.affects))))&&((this.xEvents == rhs.xEvents)||((this.xEvents!= null)&&this.xEvents.equals(rhs.xEvents))))&&((this.units == rhs.units)||((this.units!= null)&&this.units.equals(rhs.units))))&&((this.forceSides == rhs.forceSides)||((this.forceSides!= null)&&this.forceSides.equals(rhs.forceSides))))&&((this.triggers == rhs.triggers)||((this.triggers!= null)&&this.triggers.equals(rhs.triggers))))&&((this.transitionStates == rhs.transitionStates)||((this.transitionStates!= null)&&this.transitionStates.equals(rhs.transitionStates))))&&((this.learningResources == rhs.learningResources)||((this.learningResources!= null)&&this.learningResources.equals(rhs.learningResources))))&&((this.actors == rhs.actors)||((this.actors!= null)&&this.actors.equals(rhs.actors))))&&((this.environment == rhs.environment)||((this.environment!= null)&&this.environment.equals(rhs.environment))))&&((this.identification == rhs.identification)||((this.identification!= null)&&this.identification.equals(rhs.identification))))&&((this.missions == rhs.missions)||((this.missions!= null)&&this.missions.equals(rhs.missions))))&&((this.activities == rhs.activities)||((this.activities!= null)&&this.activities.equals(rhs.activities))))&&((this.overlays == rhs.overlays)||((this.overlays!= null)&&this.overlays.equals(rhs.overlays))))&&((this.dataSources == rhs.dataSources)||((this.dataSources!= null)&&this.dataSources.equals(rhs.dataSources))))&&((this.levels == rhs.levels)||((this.levels!= null)&&this.levels.equals(rhs.levels))))&&((this.tasks == rhs.tasks)||((this.tasks!= null)&&this.tasks.equals(rhs.tasks))));
    }

}
