
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
 * a real or AI person in an exercise that is represented by an entity or a live person (in live events).  Each actor will be registered to a specific device(s) and/or feduciary marker to track their performance.  Each actor will be assigned to one or more team-roles on a team and side
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "actorId",
    "actorUuid",
    "actorName",
    "realPlayer",
    "offMapPlayer",
    "ghostPlayer",
    "model",
    "location",
    "bodyDirection",
    "headDirection",
    "eyesDirection",
    "rank",
    "sex",
    "race",
    "age",
    "bodyType",
    "bodyHeight",
    "fitness",
    "bodyPose",
    "weaponPose",
    "actions",
    "gestures",
    "training",
    "experience",
    "leadership",
    "teamwork",
    "resilience",
    "awareness",
    "bloodLevel",
    "health",
    "wounds",
    "mounted",
    "carriedEquipment",
    "carriedWeight"
})
public class Actor {

    /**
     * required: exercise unique id - should also be used as the entity id
     * (Required)
     * 
     */
    @JsonProperty("actorId")
    @JsonPropertyDescription("required: exercise unique id - should also be used as the entity id")
    private Object actorId;
    /**
     * required: the real person's id if a real player else blank
     * 
     */
    @JsonProperty("actorUuid")
    @JsonPropertyDescription("required: the real person's id if a real player else blank")
    private String actorUuid;
    /**
     * required: default will be a 'dummy name' (e.g., 'Actor1'). when assigned a real player will use real trainee's name
     * (Required)
     * 
     */
    @JsonProperty("actorName")
    @JsonPropertyDescription("required: default will be a 'dummy name' (e.g., 'Actor1'). when assigned a real player will use real trainee's name")
    private String actorName = "";
    /**
     * required: defines if an actor is a real player or AI
     * 
     */
    @JsonProperty("realPlayer")
    @JsonPropertyDescription("required: defines if an actor is a real player or AI")
    private Boolean realPlayer = true;
    /**
     * required: defines if an actor will be on map or played in background only using text or voice (e.g., command, fires, aircraft)
     * 
     */
    @JsonProperty("offMapPlayer")
    @JsonPropertyDescription("required: defines if an actor will be on map or played in background only using text or voice (e.g., command, fires, aircraft)")
    private Boolean offMapPlayer = false;
    /**
     * required: defines actor is 'alive' in-exercise or else can 'roam' map freely as a 'ghost' player
     * 
     */
    @JsonProperty("ghostPlayer")
    @JsonPropertyDescription("required: defines actor is 'alive' in-exercise or else can 'roam' map freely as a 'ghost' player")
    private Boolean ghostPlayer = true;
    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    @JsonPropertyDescription("describes the on-map 3D model associated with an actor, vehicle, equipment or map object")
    private _3dModel model;
    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("location")
    @JsonPropertyDescription("Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format")
    private List<Object> location = new ArrayList<Object>();
    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("bodyDirection")
    @JsonPropertyDescription("Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)")
    private List<Double> bodyDirection = new ArrayList<Double>();
    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("headDirection")
    @JsonPropertyDescription("Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)")
    private List<Double> headDirection = new ArrayList<Double>();
    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("eyesDirection")
    @JsonPropertyDescription("Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)")
    private List<Double> eyesDirection = new ArrayList<Double>();
    /**
     * required: defaults to that associated with assigned role - overwritten by actual rank of the real player
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("required: defaults to that associated with assigned role - overwritten by actual rank of the real player")
    private String rank;
    /**
     * required: default auto - will become the sex and characteristics of the real player
     * 
     */
    @JsonProperty("sex")
    @JsonPropertyDescription("required: default auto - will become the sex and characteristics of the real player")
    private Actor.Sex sex;
    /**
     * required: default auto. Will become the real race of the real player
     * 
     */
    @JsonProperty("race")
    @JsonPropertyDescription("required: default auto. Will become the real race of the real player")
    private Actor.Race race;
    /**
     * required: default auto. Will become the real age-range of real player
     * 
     */
    @JsonProperty("age")
    @JsonPropertyDescription("required: default auto. Will become the real age-range of real player")
    private Actor.Age age;
    /**
     * required: default auto - will become the real build of the real player
     * 
     */
    @JsonProperty("bodyType")
    @JsonPropertyDescription("required: default auto - will become the real build of the real player")
    private Actor.BodyType bodyType;
    /**
     * required: default 69 inches - will become the real height of the real player
     * 
     */
    @JsonProperty("bodyHeight")
    @JsonPropertyDescription("required: default 69 inches - will become the real height of the real player")
    private Double bodyHeight = 69.0D;
    /**
     * required: default 7 - will become the real fitness level of the real player.  Will reduce fatigue rate with complex environments
     * 
     */
    @JsonProperty("fitness")
    @JsonPropertyDescription("required: default 7 - will become the real fitness level of the real player.  Will reduce fatigue rate with complex environments")
    private Double fitness = 7.0D;
    /**
     * required: default auto - will change with player or script init.  Some of these will need to be custom
     * 
     */
    @JsonProperty("bodyPose")
    @JsonPropertyDescription("required: default auto - will change with player or script init.  Some of these will need to be custom")
    private Actor.BodyPose bodyPose;
    /**
     * required: default primary is low-ready, secondary is slung - will change with player or script init
     * 
     */
    @JsonProperty("weaponPose")
    @JsonPropertyDescription("required: default primary is low-ready, secondary is slung - will change with player or script init")
    private Actor.WeaponPose weaponPose;
    @JsonProperty("actions")
    private List<Double> actions = new ArrayList<Double>();
    @JsonProperty("gestures")
    private List<Double> gestures = new ArrayList<Double>();
    /**
     * required: default 5 - will increase speed, shot accuracy, reduce fatigue rate, increase fatique threshold, determine navigation route used to approach enemy 
     * 
     */
    @JsonProperty("training")
    @JsonPropertyDescription("required: default 5 - will increase speed, shot accuracy, reduce fatigue rate, increase fatique threshold, determine navigation route used to approach enemy ")
    private Double training = 5.0D;
    /**
     * required: default 5 - will decrease morale drop rate, decrease suppression range, and decrease response to being shot at
     * 
     */
    @JsonProperty("experience")
    @JsonPropertyDescription("required: default 5 - will decrease morale drop rate, decrease suppression range, and decrease response to being shot at")
    private Double experience = 5.0D;
    /**
     * required: default 5 - will increase accuracy and decrease delay that AI subordinate entities will follow leader movements and verbal directions
     * 
     */
    @JsonProperty("leadership")
    @JsonPropertyDescription("required: default 5 - will increase accuracy and decrease delay that AI subordinate entities will follow leader movements and verbal directions")
    private Double leadership = 5.0D;
    /**
     * optional: how well the team supports each other, communicates, etc.  Not implemented now until more defined measures are conceived 
     * 
     */
    @JsonProperty("teamwork")
    @JsonPropertyDescription("optional: how well the team supports each other, communicates, etc.  Not implemented now until more defined measures are conceived ")
    private Object teamwork;
    /**
     * required: default 6 - threshold to be suppressed or to break.
     * 
     */
    @JsonProperty("resilience")
    @JsonPropertyDescription("required: default 6 - threshold to be suppressed or to break.")
    private Double resilience = 6.0D;
    /**
     * required: cooper's code state with overload - white - not aware, yellow - observant, orange - active searching for threat, red - observing/engaging threat, black - cognitive overload
     * 
     */
    @JsonProperty("awareness")
    @JsonPropertyDescription("required: cooper's code state with overload - white - not aware, yellow - observant, orange - active searching for threat, red - observing/engaging threat, black - cognitive overload")
    private Actor.Awareness awareness = Actor.Awareness.fromValue("yellow");
    /**
     * required: will default 100 - lost at rate based on health and wound types
     * 
     */
    @JsonProperty("bloodLevel")
    @JsonPropertyDescription("required: will default 100 - lost at rate based on health and wound types")
    private Double bloodLevel = 100.0D;
    /**
     * required: will default 0 - no injury. 1=heat-stroke,2=heat-exhaustion,3=wounded,4=shock,5=dead
     * 
     */
    @JsonProperty("health")
    @JsonPropertyDescription("required: will default 0 - no injury. 1=heat-stroke,2=heat-exhaustion,3=wounded,4=shock,5=dead")
    private Double health = 100.0D;
    /**
     * required: array of severity and type of wounds
     * 
     */
    @JsonProperty("wounds")
    @JsonPropertyDescription("required: array of severity and type of wounds")
    private List<Wound> wounds = new ArrayList<Wound>();
    /**
     * required: is actor being carried by a vessel, vehicle, aircraft, animal or human
     * 
     */
    @JsonProperty("mounted")
    @JsonPropertyDescription("required: is actor being carried by a vessel, vehicle, aircraft, animal or human")
    private Boolean mounted = false;
    /**
     * required: list of equipment being carried.  Will multiply fatique rate, reduce speed, and decrease response time
     * 
     */
    @JsonProperty("carriedEquipment")
    @JsonPropertyDescription("required: list of equipment being carried.  Will multiply fatique rate, reduce speed, and decrease response time")
    private List<Object> carriedEquipment = new ArrayList<Object>();
    /**
     * required: will be automatically calculated based on carried equipment, and/or carried personnel
     * 
     */
    @JsonProperty("carriedWeight")
    @JsonPropertyDescription("required: will be automatically calculated based on carried equipment, and/or carried personnel")
    private Double carriedWeight;

    /**
     * required: exercise unique id - should also be used as the entity id
     * (Required)
     * 
     */
    @JsonProperty("actorId")
    public Object getActorId() {
        return actorId;
    }

    /**
     * required: exercise unique id - should also be used as the entity id
     * (Required)
     * 
     */
    @JsonProperty("actorId")
    public void setActorId(Object actorId) {
        this.actorId = actorId;
    }

    /**
     * required: the real person's id if a real player else blank
     * 
     */
    @JsonProperty("actorUuid")
    public String getActorUuid() {
        return actorUuid;
    }

    /**
     * required: the real person's id if a real player else blank
     * 
     */
    @JsonProperty("actorUuid")
    public void setActorUuid(String actorUuid) {
        this.actorUuid = actorUuid;
    }

    /**
     * required: default will be a 'dummy name' (e.g., 'Actor1'). when assigned a real player will use real trainee's name
     * (Required)
     * 
     */
    @JsonProperty("actorName")
    public String getActorName() {
        return actorName;
    }

    /**
     * required: default will be a 'dummy name' (e.g., 'Actor1'). when assigned a real player will use real trainee's name
     * (Required)
     * 
     */
    @JsonProperty("actorName")
    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    /**
     * required: defines if an actor is a real player or AI
     * 
     */
    @JsonProperty("realPlayer")
    public Boolean getRealPlayer() {
        return realPlayer;
    }

    /**
     * required: defines if an actor is a real player or AI
     * 
     */
    @JsonProperty("realPlayer")
    public void setRealPlayer(Boolean realPlayer) {
        this.realPlayer = realPlayer;
    }

    /**
     * required: defines if an actor will be on map or played in background only using text or voice (e.g., command, fires, aircraft)
     * 
     */
    @JsonProperty("offMapPlayer")
    public Boolean getOffMapPlayer() {
        return offMapPlayer;
    }

    /**
     * required: defines if an actor will be on map or played in background only using text or voice (e.g., command, fires, aircraft)
     * 
     */
    @JsonProperty("offMapPlayer")
    public void setOffMapPlayer(Boolean offMapPlayer) {
        this.offMapPlayer = offMapPlayer;
    }

    /**
     * required: defines actor is 'alive' in-exercise or else can 'roam' map freely as a 'ghost' player
     * 
     */
    @JsonProperty("ghostPlayer")
    public Boolean getGhostPlayer() {
        return ghostPlayer;
    }

    /**
     * required: defines actor is 'alive' in-exercise or else can 'roam' map freely as a 'ghost' player
     * 
     */
    @JsonProperty("ghostPlayer")
    public void setGhostPlayer(Boolean ghostPlayer) {
        this.ghostPlayer = ghostPlayer;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    public _3dModel getModel() {
        return model;
    }

    /**
     * describes the on-map 3D model associated with an actor, vehicle, equipment or map object
     * 
     */
    @JsonProperty("model")
    public void setModel(_3dModel model) {
        this.model = model;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("location")
    public List<Object> getLocation() {
        return location;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * (Required)
     * 
     */
    @JsonProperty("location")
    public void setLocation(List<Object> location) {
        this.location = location;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("bodyDirection")
    public List<Double> getBodyDirection() {
        return bodyDirection;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("bodyDirection")
    public void setBodyDirection(List<Double> bodyDirection) {
        this.bodyDirection = bodyDirection;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("headDirection")
    public List<Double> getHeadDirection() {
        return headDirection;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("headDirection")
    public void setHeadDirection(List<Double> headDirection) {
        this.headDirection = headDirection;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("eyesDirection")
    public List<Double> getEyesDirection() {
        return eyesDirection;
    }

    /**
     * Vector coordinate used to support the direction of items from a fixed geographic axis coordinate or gcc.  Used for eyes, head, body parts, weapons, sensors, vehicles, aircraft or vessels.  Uses azimuth (direction), pitch(tilt), roll(tilt), yaw (twist)
     * 
     */
    @JsonProperty("eyesDirection")
    public void setEyesDirection(List<Double> eyesDirection) {
        this.eyesDirection = eyesDirection;
    }

    /**
     * required: defaults to that associated with assigned role - overwritten by actual rank of the real player
     * 
     */
    @JsonProperty("rank")
    public String getRank() {
        return rank;
    }

    /**
     * required: defaults to that associated with assigned role - overwritten by actual rank of the real player
     * 
     */
    @JsonProperty("rank")
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * required: default auto - will become the sex and characteristics of the real player
     * 
     */
    @JsonProperty("sex")
    public Actor.Sex getSex() {
        return sex;
    }

    /**
     * required: default auto - will become the sex and characteristics of the real player
     * 
     */
    @JsonProperty("sex")
    public void setSex(Actor.Sex sex) {
        this.sex = sex;
    }

    /**
     * required: default auto. Will become the real race of the real player
     * 
     */
    @JsonProperty("race")
    public Actor.Race getRace() {
        return race;
    }

    /**
     * required: default auto. Will become the real race of the real player
     * 
     */
    @JsonProperty("race")
    public void setRace(Actor.Race race) {
        this.race = race;
    }

    /**
     * required: default auto. Will become the real age-range of real player
     * 
     */
    @JsonProperty("age")
    public Actor.Age getAge() {
        return age;
    }

    /**
     * required: default auto. Will become the real age-range of real player
     * 
     */
    @JsonProperty("age")
    public void setAge(Actor.Age age) {
        this.age = age;
    }

    /**
     * required: default auto - will become the real build of the real player
     * 
     */
    @JsonProperty("bodyType")
    public Actor.BodyType getBodyType() {
        return bodyType;
    }

    /**
     * required: default auto - will become the real build of the real player
     * 
     */
    @JsonProperty("bodyType")
    public void setBodyType(Actor.BodyType bodyType) {
        this.bodyType = bodyType;
    }

    /**
     * required: default 69 inches - will become the real height of the real player
     * 
     */
    @JsonProperty("bodyHeight")
    public Double getBodyHeight() {
        return bodyHeight;
    }

    /**
     * required: default 69 inches - will become the real height of the real player
     * 
     */
    @JsonProperty("bodyHeight")
    public void setBodyHeight(Double bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    /**
     * required: default 7 - will become the real fitness level of the real player.  Will reduce fatigue rate with complex environments
     * 
     */
    @JsonProperty("fitness")
    public Double getFitness() {
        return fitness;
    }

    /**
     * required: default 7 - will become the real fitness level of the real player.  Will reduce fatigue rate with complex environments
     * 
     */
    @JsonProperty("fitness")
    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    /**
     * required: default auto - will change with player or script init.  Some of these will need to be custom
     * 
     */
    @JsonProperty("bodyPose")
    public Actor.BodyPose getBodyPose() {
        return bodyPose;
    }

    /**
     * required: default auto - will change with player or script init.  Some of these will need to be custom
     * 
     */
    @JsonProperty("bodyPose")
    public void setBodyPose(Actor.BodyPose bodyPose) {
        this.bodyPose = bodyPose;
    }

    /**
     * required: default primary is low-ready, secondary is slung - will change with player or script init
     * 
     */
    @JsonProperty("weaponPose")
    public Actor.WeaponPose getWeaponPose() {
        return weaponPose;
    }

    /**
     * required: default primary is low-ready, secondary is slung - will change with player or script init
     * 
     */
    @JsonProperty("weaponPose")
    public void setWeaponPose(Actor.WeaponPose weaponPose) {
        this.weaponPose = weaponPose;
    }

    @JsonProperty("actions")
    public List<Double> getActions() {
        return actions;
    }

    @JsonProperty("actions")
    public void setActions(List<Double> actions) {
        this.actions = actions;
    }

    @JsonProperty("gestures")
    public List<Double> getGestures() {
        return gestures;
    }

    @JsonProperty("gestures")
    public void setGestures(List<Double> gestures) {
        this.gestures = gestures;
    }

    /**
     * required: default 5 - will increase speed, shot accuracy, reduce fatigue rate, increase fatique threshold, determine navigation route used to approach enemy 
     * 
     */
    @JsonProperty("training")
    public Double getTraining() {
        return training;
    }

    /**
     * required: default 5 - will increase speed, shot accuracy, reduce fatigue rate, increase fatique threshold, determine navigation route used to approach enemy 
     * 
     */
    @JsonProperty("training")
    public void setTraining(Double training) {
        this.training = training;
    }

    /**
     * required: default 5 - will decrease morale drop rate, decrease suppression range, and decrease response to being shot at
     * 
     */
    @JsonProperty("experience")
    public Double getExperience() {
        return experience;
    }

    /**
     * required: default 5 - will decrease morale drop rate, decrease suppression range, and decrease response to being shot at
     * 
     */
    @JsonProperty("experience")
    public void setExperience(Double experience) {
        this.experience = experience;
    }

    /**
     * required: default 5 - will increase accuracy and decrease delay that AI subordinate entities will follow leader movements and verbal directions
     * 
     */
    @JsonProperty("leadership")
    public Double getLeadership() {
        return leadership;
    }

    /**
     * required: default 5 - will increase accuracy and decrease delay that AI subordinate entities will follow leader movements and verbal directions
     * 
     */
    @JsonProperty("leadership")
    public void setLeadership(Double leadership) {
        this.leadership = leadership;
    }

    /**
     * optional: how well the team supports each other, communicates, etc.  Not implemented now until more defined measures are conceived 
     * 
     */
    @JsonProperty("teamwork")
    public Object getTeamwork() {
        return teamwork;
    }

    /**
     * optional: how well the team supports each other, communicates, etc.  Not implemented now until more defined measures are conceived 
     * 
     */
    @JsonProperty("teamwork")
    public void setTeamwork(Object teamwork) {
        this.teamwork = teamwork;
    }

    /**
     * required: default 6 - threshold to be suppressed or to break.
     * 
     */
    @JsonProperty("resilience")
    public Double getResilience() {
        return resilience;
    }

    /**
     * required: default 6 - threshold to be suppressed or to break.
     * 
     */
    @JsonProperty("resilience")
    public void setResilience(Double resilience) {
        this.resilience = resilience;
    }

    /**
     * required: cooper's code state with overload - white - not aware, yellow - observant, orange - active searching for threat, red - observing/engaging threat, black - cognitive overload
     * 
     */
    @JsonProperty("awareness")
    public Actor.Awareness getAwareness() {
        return awareness;
    }

    /**
     * required: cooper's code state with overload - white - not aware, yellow - observant, orange - active searching for threat, red - observing/engaging threat, black - cognitive overload
     * 
     */
    @JsonProperty("awareness")
    public void setAwareness(Actor.Awareness awareness) {
        this.awareness = awareness;
    }

    /**
     * required: will default 100 - lost at rate based on health and wound types
     * 
     */
    @JsonProperty("bloodLevel")
    public Double getBloodLevel() {
        return bloodLevel;
    }

    /**
     * required: will default 100 - lost at rate based on health and wound types
     * 
     */
    @JsonProperty("bloodLevel")
    public void setBloodLevel(Double bloodLevel) {
        this.bloodLevel = bloodLevel;
    }

    /**
     * required: will default 0 - no injury. 1=heat-stroke,2=heat-exhaustion,3=wounded,4=shock,5=dead
     * 
     */
    @JsonProperty("health")
    public Double getHealth() {
        return health;
    }

    /**
     * required: will default 0 - no injury. 1=heat-stroke,2=heat-exhaustion,3=wounded,4=shock,5=dead
     * 
     */
    @JsonProperty("health")
    public void setHealth(Double health) {
        this.health = health;
    }

    /**
     * required: array of severity and type of wounds
     * 
     */
    @JsonProperty("wounds")
    public List<Wound> getWounds() {
        return wounds;
    }

    /**
     * required: array of severity and type of wounds
     * 
     */
    @JsonProperty("wounds")
    public void setWounds(List<Wound> wounds) {
        this.wounds = wounds;
    }

    /**
     * required: is actor being carried by a vessel, vehicle, aircraft, animal or human
     * 
     */
    @JsonProperty("mounted")
    public Boolean getMounted() {
        return mounted;
    }

    /**
     * required: is actor being carried by a vessel, vehicle, aircraft, animal or human
     * 
     */
    @JsonProperty("mounted")
    public void setMounted(Boolean mounted) {
        this.mounted = mounted;
    }

    /**
     * required: list of equipment being carried.  Will multiply fatique rate, reduce speed, and decrease response time
     * 
     */
    @JsonProperty("carriedEquipment")
    public List<Object> getCarriedEquipment() {
        return carriedEquipment;
    }

    /**
     * required: list of equipment being carried.  Will multiply fatique rate, reduce speed, and decrease response time
     * 
     */
    @JsonProperty("carriedEquipment")
    public void setCarriedEquipment(List<Object> carriedEquipment) {
        this.carriedEquipment = carriedEquipment;
    }

    /**
     * required: will be automatically calculated based on carried equipment, and/or carried personnel
     * 
     */
    @JsonProperty("carriedWeight")
    public Double getCarriedWeight() {
        return carriedWeight;
    }

    /**
     * required: will be automatically calculated based on carried equipment, and/or carried personnel
     * 
     */
    @JsonProperty("carriedWeight")
    public void setCarriedWeight(Double carriedWeight) {
        this.carriedWeight = carriedWeight;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Actor.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("actorId");
        sb.append('=');
        sb.append(((this.actorId == null)?"<null>":this.actorId));
        sb.append(',');
        sb.append("actorUuid");
        sb.append('=');
        sb.append(((this.actorUuid == null)?"<null>":this.actorUuid));
        sb.append(',');
        sb.append("actorName");
        sb.append('=');
        sb.append(((this.actorName == null)?"<null>":this.actorName));
        sb.append(',');
        sb.append("realPlayer");
        sb.append('=');
        sb.append(((this.realPlayer == null)?"<null>":this.realPlayer));
        sb.append(',');
        sb.append("offMapPlayer");
        sb.append('=');
        sb.append(((this.offMapPlayer == null)?"<null>":this.offMapPlayer));
        sb.append(',');
        sb.append("ghostPlayer");
        sb.append('=');
        sb.append(((this.ghostPlayer == null)?"<null>":this.ghostPlayer));
        sb.append(',');
        sb.append("model");
        sb.append('=');
        sb.append(((this.model == null)?"<null>":this.model));
        sb.append(',');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("bodyDirection");
        sb.append('=');
        sb.append(((this.bodyDirection == null)?"<null>":this.bodyDirection));
        sb.append(',');
        sb.append("headDirection");
        sb.append('=');
        sb.append(((this.headDirection == null)?"<null>":this.headDirection));
        sb.append(',');
        sb.append("eyesDirection");
        sb.append('=');
        sb.append(((this.eyesDirection == null)?"<null>":this.eyesDirection));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null)?"<null>":this.rank));
        sb.append(',');
        sb.append("sex");
        sb.append('=');
        sb.append(((this.sex == null)?"<null>":this.sex));
        sb.append(',');
        sb.append("race");
        sb.append('=');
        sb.append(((this.race == null)?"<null>":this.race));
        sb.append(',');
        sb.append("age");
        sb.append('=');
        sb.append(((this.age == null)?"<null>":this.age));
        sb.append(',');
        sb.append("bodyType");
        sb.append('=');
        sb.append(((this.bodyType == null)?"<null>":this.bodyType));
        sb.append(',');
        sb.append("bodyHeight");
        sb.append('=');
        sb.append(((this.bodyHeight == null)?"<null>":this.bodyHeight));
        sb.append(',');
        sb.append("fitness");
        sb.append('=');
        sb.append(((this.fitness == null)?"<null>":this.fitness));
        sb.append(',');
        sb.append("bodyPose");
        sb.append('=');
        sb.append(((this.bodyPose == null)?"<null>":this.bodyPose));
        sb.append(',');
        sb.append("weaponPose");
        sb.append('=');
        sb.append(((this.weaponPose == null)?"<null>":this.weaponPose));
        sb.append(',');
        sb.append("actions");
        sb.append('=');
        sb.append(((this.actions == null)?"<null>":this.actions));
        sb.append(',');
        sb.append("gestures");
        sb.append('=');
        sb.append(((this.gestures == null)?"<null>":this.gestures));
        sb.append(',');
        sb.append("training");
        sb.append('=');
        sb.append(((this.training == null)?"<null>":this.training));
        sb.append(',');
        sb.append("experience");
        sb.append('=');
        sb.append(((this.experience == null)?"<null>":this.experience));
        sb.append(',');
        sb.append("leadership");
        sb.append('=');
        sb.append(((this.leadership == null)?"<null>":this.leadership));
        sb.append(',');
        sb.append("teamwork");
        sb.append('=');
        sb.append(((this.teamwork == null)?"<null>":this.teamwork));
        sb.append(',');
        sb.append("resilience");
        sb.append('=');
        sb.append(((this.resilience == null)?"<null>":this.resilience));
        sb.append(',');
        sb.append("awareness");
        sb.append('=');
        sb.append(((this.awareness == null)?"<null>":this.awareness));
        sb.append(',');
        sb.append("bloodLevel");
        sb.append('=');
        sb.append(((this.bloodLevel == null)?"<null>":this.bloodLevel));
        sb.append(',');
        sb.append("health");
        sb.append('=');
        sb.append(((this.health == null)?"<null>":this.health));
        sb.append(',');
        sb.append("wounds");
        sb.append('=');
        sb.append(((this.wounds == null)?"<null>":this.wounds));
        sb.append(',');
        sb.append("mounted");
        sb.append('=');
        sb.append(((this.mounted == null)?"<null>":this.mounted));
        sb.append(',');
        sb.append("carriedEquipment");
        sb.append('=');
        sb.append(((this.carriedEquipment == null)?"<null>":this.carriedEquipment));
        sb.append(',');
        sb.append("carriedWeight");
        sb.append('=');
        sb.append(((this.carriedWeight == null)?"<null>":this.carriedWeight));
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
        result = ((result* 31)+((this.bodyType == null)? 0 :this.bodyType.hashCode()));
        result = ((result* 31)+((this.weaponPose == null)? 0 :this.weaponPose.hashCode()));
        result = ((result* 31)+((this.offMapPlayer == null)? 0 :this.offMapPlayer.hashCode()));
        result = ((result* 31)+((this.headDirection == null)? 0 :this.headDirection.hashCode()));
        result = ((result* 31)+((this.bodyHeight == null)? 0 :this.bodyHeight.hashCode()));
        result = ((result* 31)+((this.actorName == null)? 0 :this.actorName.hashCode()));
        result = ((result* 31)+((this.training == null)? 0 :this.training.hashCode()));
        result = ((result* 31)+((this.experience == null)? 0 :this.experience.hashCode()));
        result = ((result* 31)+((this.eyesDirection == null)? 0 :this.eyesDirection.hashCode()));
        result = ((result* 31)+((this.carriedEquipment == null)? 0 :this.carriedEquipment.hashCode()));
        result = ((result* 31)+((this.gestures == null)? 0 :this.gestures.hashCode()));
        result = ((result* 31)+((this.awareness == null)? 0 :this.awareness.hashCode()));
        result = ((result* 31)+((this.realPlayer == null)? 0 :this.realPlayer.hashCode()));
        result = ((result* 31)+((this.fitness == null)? 0 :this.fitness.hashCode()));
        result = ((result* 31)+((this.carriedWeight == null)? 0 :this.carriedWeight.hashCode()));
        result = ((result* 31)+((this.leadership == null)? 0 :this.leadership.hashCode()));
        result = ((result* 31)+((this.rank == null)? 0 :this.rank.hashCode()));
        result = ((result* 31)+((this.model == null)? 0 :this.model.hashCode()));
        result = ((result* 31)+((this.resilience == null)? 0 :this.resilience.hashCode()));
        result = ((result* 31)+((this.bloodLevel == null)? 0 :this.bloodLevel.hashCode()));
        result = ((result* 31)+((this.wounds == null)? 0 :this.wounds.hashCode()));
        result = ((result* 31)+((this.race == null)? 0 :this.race.hashCode()));
        result = ((result* 31)+((this.sex == null)? 0 :this.sex.hashCode()));
        result = ((result* 31)+((this.bodyPose == null)? 0 :this.bodyPose.hashCode()));
        result = ((result* 31)+((this.health == null)? 0 :this.health.hashCode()));
        result = ((result* 31)+((this.mounted == null)? 0 :this.mounted.hashCode()));
        result = ((result* 31)+((this.bodyDirection == null)? 0 :this.bodyDirection.hashCode()));
        result = ((result* 31)+((this.actorId == null)? 0 :this.actorId.hashCode()));
        result = ((result* 31)+((this.ghostPlayer == null)? 0 :this.ghostPlayer.hashCode()));
        result = ((result* 31)+((this.teamwork == null)? 0 :this.teamwork.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.actions == null)? 0 :this.actions.hashCode()));
        result = ((result* 31)+((this.actorUuid == null)? 0 :this.actorUuid.hashCode()));
        result = ((result* 31)+((this.age == null)? 0 :this.age.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Actor) == false) {
            return false;
        }
        Actor rhs = ((Actor) other);
        return (((((((((((((((((((((((((((((((((((this.bodyType == rhs.bodyType)||((this.bodyType!= null)&&this.bodyType.equals(rhs.bodyType)))&&((this.weaponPose == rhs.weaponPose)||((this.weaponPose!= null)&&this.weaponPose.equals(rhs.weaponPose))))&&((this.offMapPlayer == rhs.offMapPlayer)||((this.offMapPlayer!= null)&&this.offMapPlayer.equals(rhs.offMapPlayer))))&&((this.headDirection == rhs.headDirection)||((this.headDirection!= null)&&this.headDirection.equals(rhs.headDirection))))&&((this.bodyHeight == rhs.bodyHeight)||((this.bodyHeight!= null)&&this.bodyHeight.equals(rhs.bodyHeight))))&&((this.actorName == rhs.actorName)||((this.actorName!= null)&&this.actorName.equals(rhs.actorName))))&&((this.training == rhs.training)||((this.training!= null)&&this.training.equals(rhs.training))))&&((this.experience == rhs.experience)||((this.experience!= null)&&this.experience.equals(rhs.experience))))&&((this.eyesDirection == rhs.eyesDirection)||((this.eyesDirection!= null)&&this.eyesDirection.equals(rhs.eyesDirection))))&&((this.carriedEquipment == rhs.carriedEquipment)||((this.carriedEquipment!= null)&&this.carriedEquipment.equals(rhs.carriedEquipment))))&&((this.gestures == rhs.gestures)||((this.gestures!= null)&&this.gestures.equals(rhs.gestures))))&&((this.awareness == rhs.awareness)||((this.awareness!= null)&&this.awareness.equals(rhs.awareness))))&&((this.realPlayer == rhs.realPlayer)||((this.realPlayer!= null)&&this.realPlayer.equals(rhs.realPlayer))))&&((this.fitness == rhs.fitness)||((this.fitness!= null)&&this.fitness.equals(rhs.fitness))))&&((this.carriedWeight == rhs.carriedWeight)||((this.carriedWeight!= null)&&this.carriedWeight.equals(rhs.carriedWeight))))&&((this.leadership == rhs.leadership)||((this.leadership!= null)&&this.leadership.equals(rhs.leadership))))&&((this.rank == rhs.rank)||((this.rank!= null)&&this.rank.equals(rhs.rank))))&&((this.model == rhs.model)||((this.model!= null)&&this.model.equals(rhs.model))))&&((this.resilience == rhs.resilience)||((this.resilience!= null)&&this.resilience.equals(rhs.resilience))))&&((this.bloodLevel == rhs.bloodLevel)||((this.bloodLevel!= null)&&this.bloodLevel.equals(rhs.bloodLevel))))&&((this.wounds == rhs.wounds)||((this.wounds!= null)&&this.wounds.equals(rhs.wounds))))&&((this.race == rhs.race)||((this.race!= null)&&this.race.equals(rhs.race))))&&((this.sex == rhs.sex)||((this.sex!= null)&&this.sex.equals(rhs.sex))))&&((this.bodyPose == rhs.bodyPose)||((this.bodyPose!= null)&&this.bodyPose.equals(rhs.bodyPose))))&&((this.health == rhs.health)||((this.health!= null)&&this.health.equals(rhs.health))))&&((this.mounted == rhs.mounted)||((this.mounted!= null)&&this.mounted.equals(rhs.mounted))))&&((this.bodyDirection == rhs.bodyDirection)||((this.bodyDirection!= null)&&this.bodyDirection.equals(rhs.bodyDirection))))&&((this.actorId == rhs.actorId)||((this.actorId!= null)&&this.actorId.equals(rhs.actorId))))&&((this.ghostPlayer == rhs.ghostPlayer)||((this.ghostPlayer!= null)&&this.ghostPlayer.equals(rhs.ghostPlayer))))&&((this.teamwork == rhs.teamwork)||((this.teamwork!= null)&&this.teamwork.equals(rhs.teamwork))))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.actions == rhs.actions)||((this.actions!= null)&&this.actions.equals(rhs.actions))))&&((this.actorUuid == rhs.actorUuid)||((this.actorUuid!= null)&&this.actorUuid.equals(rhs.actorUuid))))&&((this.age == rhs.age)||((this.age!= null)&&this.age.equals(rhs.age))));
    }


    /**
     * required: default auto. Will become the real age-range of real player
     * 
     */
    public enum Age {

        AUTO("auto"),
        INFANT_0_1("infant (0-1)"),
        TODDLER_1_3("toddler (1-3)"),
        CHILD_3_10("child (3-10)"),
        ADOLESCENT_10_12("adolescent (10-12)"),
        TEENAGER_13_16("teenager (13-16)"),
        YOUNG_ADULT_17_25("youngAdult (17-25)"),
        ADULT_25_55("adult (25-55)"),
        SENIOR_ADAULT_55_70("seniorAdault (55-70)"),
        ELDERLY_71("elderly (71+)");
        private final String value;
        private final static Map<String, Actor.Age> CONSTANTS = new HashMap<String, Actor.Age>();

        static {
            for (Actor.Age c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Age(String value) {
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
        public static Actor.Age fromValue(String value) {
            Actor.Age constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: cooper's code state with overload - white - not aware, yellow - observant, orange - active searching for threat, red - observing/engaging threat, black - cognitive overload
     * 
     */
    public enum Awareness {

        WHITE("white"),
        YELLOW("yellow"),
        ORANGE("orange"),
        RED("red"),
        BLACK("black");
        private final String value;
        private final static Map<String, Actor.Awareness> CONSTANTS = new HashMap<String, Actor.Awareness>();

        static {
            for (Actor.Awareness c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Awareness(String value) {
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
        public static Actor.Awareness fromValue(String value) {
            Actor.Awareness constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: default auto - will change with player or script init.  Some of these will need to be custom
     * 
     */
    public enum BodyPose {

        STAND("stand"),
        KNEEL("kneel"),
        PRONE("prone"),
        FLOAT("float"),
        PARACHUTING("parachuting"),
        SIT("sit"),
        SQUAT("squat"),
        CROUCH("crouch"),
        LYING("lying"),
        SLUMP("slump");
        private final String value;
        private final static Map<String, Actor.BodyPose> CONSTANTS = new HashMap<String, Actor.BodyPose>();

        static {
            for (Actor.BodyPose c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BodyPose(String value) {
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
        public static Actor.BodyPose fromValue(String value) {
            Actor.BodyPose constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: default auto - will become the real build of the real player
     * 
     */
    public enum BodyType {

        AUTO("auto"),
        HEAVY("heavy"),
        MEDIUM("medium"),
        SLIM("slim");
        private final String value;
        private final static Map<String, Actor.BodyType> CONSTANTS = new HashMap<String, Actor.BodyType>();

        static {
            for (Actor.BodyType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BodyType(String value) {
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
        public static Actor.BodyType fromValue(String value) {
            Actor.BodyType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: default auto. Will become the real race of the real player
     * 
     */
    public enum Race {

        AUTO("auto"),
        ASIAN("asian"),
        PACIFIC_ISLANDER("pacific islander"),
        BLACK("black"),
        SOUTHEAST_ASIAN("southeast asian"),
        SOUTHWEST_ASIAN("southwest asian"),
        HISPANIC("hispanic"),
        ARAB("arab"),
        CAUCASIAN("caucasian");
        private final String value;
        private final static Map<String, Actor.Race> CONSTANTS = new HashMap<String, Actor.Race>();

        static {
            for (Actor.Race c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Race(String value) {
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
        public static Actor.Race fromValue(String value) {
            Actor.Race constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: default auto - will become the sex and characteristics of the real player
     * 
     */
    public enum Sex {

        AUTO("auto"),
        MALE("male"),
        FEMALE("female");
        private final String value;
        private final static Map<String, Actor.Sex> CONSTANTS = new HashMap<String, Actor.Sex>();

        static {
            for (Actor.Sex c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Sex(String value) {
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
        public static Actor.Sex fromValue(String value) {
            Actor.Sex constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: default primary is low-ready, secondary is slung - will change with player or script init
     * 
     */
    public enum WeaponPose {

        AIMING("aiming"),
        HIGH_READY("high-ready"),
        LOW_READY("low-ready"),
        PATROL("patrol"),
        SHOW_OF_FORCE("show-of-force"),
        OFFSET_ABOVE("offset-above"),
        OFFSET_LEFT("offset-left"),
        OFFSET_RIGHT("offset-right"),
        SLUNG("slung");
        private final String value;
        private final static Map<String, Actor.WeaponPose> CONSTANTS = new HashMap<String, Actor.WeaponPose>();

        static {
            for (Actor.WeaponPose c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        WeaponPose(String value) {
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
        public static Actor.WeaponPose fromValue(String value) {
            Actor.WeaponPose constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
