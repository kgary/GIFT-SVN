
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
 * this is the information that a search engine will look at during exercise query and display.  It also provides administrative information regarding the exercise maintenance and access
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "experienceId",
    "experienceUuid",
    "experienceTitle",
    "experienceType",
    "experienceVersion",
    "schemaVersion",
    "publishState",
    "releaseDate",
    "securityClass",
    "distributionRestriction",
    "distributionNotice",
    "foreignRestriction",
    "description",
    "occupation",
    "occupationDomain",
    "occupationSpecialty",
    "organizationType",
    "organizationName",
    "organizationEchelon",
    "platformType",
    "weapons",
    "systems",
    "echelonTeams",
    "echelonRoles",
    "competencyFramework",
    "competencies",
    "competenceRequired",
    "trainingPhase",
    "trainingEnvironment",
    "difficultyLevel",
    "useNotes",
    "measurementUnits",
    "useHistory",
    "keywords",
    "symbol",
    "authors",
    "changeHistory"
})
public class GenInfo {

    /**
     * required: experience unique id (number and/or string)
     * (Required)
     * 
     */
    @JsonProperty("experienceId")
    @JsonPropertyDescription("required: experience unique id (number and/or string)")
    private Object experienceId;
    /**
     * required: has to be a unique URI based id
     * 
     */
    @JsonProperty("experienceUuid")
    @JsonPropertyDescription("required: has to be a unique URI based id")
    private String experienceUuid;
    /**
     * required: experience name - doesn't have to be unique if other data is different
     * (Required)
     * 
     */
    @JsonProperty("experienceTitle")
    @JsonPropertyDescription("required: experience name - doesn't have to be unique if other data is different")
    private String experienceTitle;
    /**
     * required: type of experience xtsp is designed to support
     * (Required)
     * 
     */
    @JsonProperty("experienceType")
    @JsonPropertyDescription("required: type of experience xtsp is designed to support")
    private GenInfo.ExperienceType experienceType;
    /**
     * required: version of xtsp - note re-use with any key info changes should re-start version to 0
     * (Required)
     * 
     */
    @JsonProperty("experienceVersion")
    @JsonPropertyDescription("required: version of xtsp - note re-use with any key info changes should re-start version to 0")
    private Double experienceVersion;
    /**
     * option: XTSP Schema version being used
     * (Required)
     * 
     */
    @JsonProperty("schemaVersion")
    @JsonPropertyDescription("option: XTSP Schema version being used")
    private String schemaVersion;
    /**
     * required: date xtsp was published
     * (Required)
     * 
     */
    @JsonProperty("publishState")
    @JsonPropertyDescription("required: date xtsp was published")
    private GenInfo.PublishState publishState;
    /**
     * option: JSON standard being used
     * (Required)
     * 
     */
    @JsonProperty("releaseDate")
    @JsonPropertyDescription("option: JSON standard being used")
    private String releaseDate;
    /**
     * a derived classification and declassify date
     * (Required)
     * 
     */
    @JsonProperty("securityClass")
    @JsonPropertyDescription("a derived classification and declassify date")
    private SecurityClass securityClass;
    /**
     * option: if UNCLASS, who can have access to this experience
     * (Required)
     * 
     */
    @JsonProperty("distributionRestriction")
    @JsonPropertyDescription("option: if UNCLASS, who can have access to this experience")
    private GenInfo.DistributionRestriction distributionRestriction = GenInfo.DistributionRestriction.fromValue("Distribution A");
    /**
     * option: verbiage that is provided with distribution restriction
     * 
     */
    @JsonProperty("distributionNotice")
    @JsonPropertyDescription("option: verbiage that is provided with distribution restriction")
    private String distributionNotice;
    /**
     * option: what export control or foreign restriction applies
     * (Required)
     * 
     */
    @JsonProperty("foreignRestriction")
    @JsonPropertyDescription("option: what export control or foreign restriction applies")
    private String foreignRestriction;
    /**
     * option: summary description of the experience
     * (Required)
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("option: summary description of the experience")
    private String description;
    /**
     * required: o*net occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupation")
    @JsonPropertyDescription("required: o*net occupation this experience applies to")
    private GenInfo.Occupation occupation;
    /**
     * required: domain of the occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupationDomain")
    @JsonPropertyDescription("required: domain of the occupation this experience applies to")
    private GenInfo.OccupationDomain occupationDomain;
    /**
     * required: occupation specialty experience applies to (proponent, MOS, NEC, OSC...)
     * (Required)
     * 
     */
    @JsonProperty("occupationSpecialty")
    @JsonPropertyDescription("required: occupation specialty experience applies to (proponent, MOS, NEC, OSC...)")
    private GenInfo.OccupationSpecialty occupationSpecialty;
    /**
     * required: type of occupation domain organization does this experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationType")
    @JsonPropertyDescription("required: type of occupation domain organization does this experience apply to")
    private GenInfo.OrganizationType organizationType;
    /**
     * optional: specific organization experience apply to (if only one)
     * 
     */
    @JsonProperty("organizationName")
    @JsonPropertyDescription("optional: specific organization experience apply to (if only one)")
    private String organizationName;
    /**
     * required: organization echelon the experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationEchelon")
    @JsonPropertyDescription("required: organization echelon the experience apply to")
    private GenInfo.OrganizationEchelon organizationEchelon;
    /**
     * optional: platform this xtsp is focused on
     * 
     */
    @JsonProperty("platformType")
    @JsonPropertyDescription("optional: platform this xtsp is focused on")
    private List<PlatformType> platformType = new ArrayList<PlatformType>();
    /**
     * optional: weapon system an XTSP is focused on
     * 
     */
    @JsonProperty("weapons")
    @JsonPropertyDescription("optional: weapon system an XTSP is focused on")
    private List<Weapon> weapons = new ArrayList<Weapon>();
    /**
     * optional: combat or other system this xtsp is focused on
     * 
     */
    @JsonProperty("systems")
    @JsonPropertyDescription("optional: combat or other system this xtsp is focused on")
    private List<String> systems = new ArrayList<String>();
    /**
     * optional: echelon teams the experience applies to
     * 
     */
    @JsonProperty("echelonTeams")
    @JsonPropertyDescription("optional: echelon teams the experience applies to")
    private List<String> echelonTeams = new ArrayList<String>();
    /**
     * optional: echelon roles the experience applies to
     * 
     */
    @JsonProperty("echelonRoles")
    @JsonPropertyDescription("optional: echelon roles the experience applies to")
    private List<String> echelonRoles = new ArrayList<String>();
    /**
     * optional: competence framework or METL the experience supports
     * 
     */
    @JsonProperty("competencyFramework")
    @JsonPropertyDescription("optional: competence framework or METL the experience supports")
    private List<String> competencyFramework = new ArrayList<String>();
    /**
     * optional: competencies or METs the experience supports
     * 
     */
    @JsonProperty("competencies")
    @JsonPropertyDescription("optional: competencies or METs the experience supports")
    private List<String> competencies = new ArrayList<String>();
    /**
     * option: minimum competence level required to complete experience
     * 
     */
    @JsonProperty("competenceRequired")
    @JsonPropertyDescription("option: minimum competence level required to complete experience")
    private GenInfo.CompetenceRequired competenceRequired;
    /**
     * required: training phase xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingPhase")
    @JsonPropertyDescription("required: training phase xtsp applies to")
    private GenInfo.TrainingPhase trainingPhase;
    /**
     * required: training environment xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingEnvironment")
    @JsonPropertyDescription("required: training environment xtsp applies to")
    private GenInfo.TrainingEnvironment trainingEnvironment;
    /**
     * required: a summary level based on contained x-event terrains, conditions, tasks and activities
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    @JsonPropertyDescription("required: a summary level based on contained x-event terrains, conditions, tasks and activities")
    private GenInfo.DifficultyLevel difficultyLevel;
    /**
     * optional: any notes the designer feels need to be passed on to other users
     * 
     */
    @JsonProperty("useNotes")
    @JsonPropertyDescription("optional: any notes the designer feels need to be passed on to other users")
    private Object useNotes;
    /**
     * required: global indicator of measurement unit class
     * (Required)
     * 
     */
    @JsonProperty("measurementUnits")
    @JsonPropertyDescription("required: global indicator of measurement unit class")
    private GenInfo.MeasurementUnits measurementUnits = GenInfo.MeasurementUnits.fromValue("metric");
    /**
     * required: automated list of training events xtsp has been used in
     * (Required)
     * 
     */
    @JsonProperty("useHistory")
    @JsonPropertyDescription("required: automated list of training events xtsp has been used in")
    private Object useHistory;
    /**
     * optional: keywords that can be used to search for
     * 
     */
    @JsonProperty("keywords")
    @JsonPropertyDescription("optional: keywords that can be used to search for")
    private Object keywords;
    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("symbol")
    @JsonPropertyDescription("optional: a glyph-symbol image sused to represent a team, role or object on map")
    private Glyph symbol;
    /**
     * required: point of contact who 'owns' the xtsp
     * (Required)
     * 
     */
    @JsonProperty("authors")
    @JsonPropertyDescription("required: point of contact who 'owns' the xtsp")
    private List<Author> authors = new ArrayList<Author>();
    /**
     * required: list of changes made - automatically done in XDT
     * (Required)
     * 
     */
    @JsonProperty("changeHistory")
    @JsonPropertyDescription("required: list of changes made - automatically done in XDT")
    private Object changeHistory;

    /**
     * required: experience unique id (number and/or string)
     * (Required)
     * 
     */
    @JsonProperty("experienceId")
    public Object getExperienceId() {
        return experienceId;
    }

    /**
     * required: experience unique id (number and/or string)
     * (Required)
     * 
     */
    @JsonProperty("experienceId")
    public void setExperienceId(Object experienceId) {
        this.experienceId = experienceId;
    }

    /**
     * required: has to be a unique URI based id
     * 
     */
    @JsonProperty("experienceUuid")
    public String getExperienceUuid() {
        return experienceUuid;
    }

    /**
     * required: has to be a unique URI based id
     * 
     */
    @JsonProperty("experienceUuid")
    public void setExperienceUuid(String experienceUuid) {
        this.experienceUuid = experienceUuid;
    }

    /**
     * required: experience name - doesn't have to be unique if other data is different
     * (Required)
     * 
     */
    @JsonProperty("experienceTitle")
    public String getExperienceTitle() {
        return experienceTitle;
    }

    /**
     * required: experience name - doesn't have to be unique if other data is different
     * (Required)
     * 
     */
    @JsonProperty("experienceTitle")
    public void setExperienceTitle(String experienceTitle) {
        this.experienceTitle = experienceTitle;
    }

    /**
     * required: type of experience xtsp is designed to support
     * (Required)
     * 
     */
    @JsonProperty("experienceType")
    public GenInfo.ExperienceType getExperienceType() {
        return experienceType;
    }

    /**
     * required: type of experience xtsp is designed to support
     * (Required)
     * 
     */
    @JsonProperty("experienceType")
    public void setExperienceType(GenInfo.ExperienceType experienceType) {
        this.experienceType = experienceType;
    }

    /**
     * required: version of xtsp - note re-use with any key info changes should re-start version to 0
     * (Required)
     * 
     */
    @JsonProperty("experienceVersion")
    public Double getExperienceVersion() {
        return experienceVersion;
    }

    /**
     * required: version of xtsp - note re-use with any key info changes should re-start version to 0
     * (Required)
     * 
     */
    @JsonProperty("experienceVersion")
    public void setExperienceVersion(Double experienceVersion) {
        this.experienceVersion = experienceVersion;
    }

    /**
     * option: XTSP Schema version being used
     * (Required)
     * 
     */
    @JsonProperty("schemaVersion")
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * option: XTSP Schema version being used
     * (Required)
     * 
     */
    @JsonProperty("schemaVersion")
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * required: date xtsp was published
     * (Required)
     * 
     */
    @JsonProperty("publishState")
    public GenInfo.PublishState getPublishState() {
        return publishState;
    }

    /**
     * required: date xtsp was published
     * (Required)
     * 
     */
    @JsonProperty("publishState")
    public void setPublishState(GenInfo.PublishState publishState) {
        this.publishState = publishState;
    }

    /**
     * option: JSON standard being used
     * (Required)
     * 
     */
    @JsonProperty("releaseDate")
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * option: JSON standard being used
     * (Required)
     * 
     */
    @JsonProperty("releaseDate")
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * a derived classification and declassify date
     * (Required)
     * 
     */
    @JsonProperty("securityClass")
    public SecurityClass getSecurityClass() {
        return securityClass;
    }

    /**
     * a derived classification and declassify date
     * (Required)
     * 
     */
    @JsonProperty("securityClass")
    public void setSecurityClass(SecurityClass securityClass) {
        this.securityClass = securityClass;
    }

    /**
     * option: if UNCLASS, who can have access to this experience
     * (Required)
     * 
     */
    @JsonProperty("distributionRestriction")
    public GenInfo.DistributionRestriction getDistributionRestriction() {
        return distributionRestriction;
    }

    /**
     * option: if UNCLASS, who can have access to this experience
     * (Required)
     * 
     */
    @JsonProperty("distributionRestriction")
    public void setDistributionRestriction(GenInfo.DistributionRestriction distributionRestriction) {
        this.distributionRestriction = distributionRestriction;
    }

    /**
     * option: verbiage that is provided with distribution restriction
     * 
     */
    @JsonProperty("distributionNotice")
    public String getDistributionNotice() {
        return distributionNotice;
    }

    /**
     * option: verbiage that is provided with distribution restriction
     * 
     */
    @JsonProperty("distributionNotice")
    public void setDistributionNotice(String distributionNotice) {
        this.distributionNotice = distributionNotice;
    }

    /**
     * option: what export control or foreign restriction applies
     * (Required)
     * 
     */
    @JsonProperty("foreignRestriction")
    public String getForeignRestriction() {
        return foreignRestriction;
    }

    /**
     * option: what export control or foreign restriction applies
     * (Required)
     * 
     */
    @JsonProperty("foreignRestriction")
    public void setForeignRestriction(String foreignRestriction) {
        this.foreignRestriction = foreignRestriction;
    }

    /**
     * option: summary description of the experience
     * (Required)
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * option: summary description of the experience
     * (Required)
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * required: o*net occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupation")
    public GenInfo.Occupation getOccupation() {
        return occupation;
    }

    /**
     * required: o*net occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupation")
    public void setOccupation(GenInfo.Occupation occupation) {
        this.occupation = occupation;
    }

    /**
     * required: domain of the occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupationDomain")
    public GenInfo.OccupationDomain getOccupationDomain() {
        return occupationDomain;
    }

    /**
     * required: domain of the occupation this experience applies to
     * (Required)
     * 
     */
    @JsonProperty("occupationDomain")
    public void setOccupationDomain(GenInfo.OccupationDomain occupationDomain) {
        this.occupationDomain = occupationDomain;
    }

    /**
     * required: occupation specialty experience applies to (proponent, MOS, NEC, OSC...)
     * (Required)
     * 
     */
    @JsonProperty("occupationSpecialty")
    public GenInfo.OccupationSpecialty getOccupationSpecialty() {
        return occupationSpecialty;
    }

    /**
     * required: occupation specialty experience applies to (proponent, MOS, NEC, OSC...)
     * (Required)
     * 
     */
    @JsonProperty("occupationSpecialty")
    public void setOccupationSpecialty(GenInfo.OccupationSpecialty occupationSpecialty) {
        this.occupationSpecialty = occupationSpecialty;
    }

    /**
     * required: type of occupation domain organization does this experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationType")
    public GenInfo.OrganizationType getOrganizationType() {
        return organizationType;
    }

    /**
     * required: type of occupation domain organization does this experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationType")
    public void setOrganizationType(GenInfo.OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    /**
     * optional: specific organization experience apply to (if only one)
     * 
     */
    @JsonProperty("organizationName")
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * optional: specific organization experience apply to (if only one)
     * 
     */
    @JsonProperty("organizationName")
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * required: organization echelon the experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationEchelon")
    public GenInfo.OrganizationEchelon getOrganizationEchelon() {
        return organizationEchelon;
    }

    /**
     * required: organization echelon the experience apply to
     * (Required)
     * 
     */
    @JsonProperty("organizationEchelon")
    public void setOrganizationEchelon(GenInfo.OrganizationEchelon organizationEchelon) {
        this.organizationEchelon = organizationEchelon;
    }

    /**
     * optional: platform this xtsp is focused on
     * 
     */
    @JsonProperty("platformType")
    public List<PlatformType> getPlatformType() {
        return platformType;
    }

    /**
     * optional: platform this xtsp is focused on
     * 
     */
    @JsonProperty("platformType")
    public void setPlatformType(List<PlatformType> platformType) {
        this.platformType = platformType;
    }

    /**
     * optional: weapon system an XTSP is focused on
     * 
     */
    @JsonProperty("weapons")
    public List<Weapon> getWeapons() {
        return weapons;
    }

    /**
     * optional: weapon system an XTSP is focused on
     * 
     */
    @JsonProperty("weapons")
    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    /**
     * optional: combat or other system this xtsp is focused on
     * 
     */
    @JsonProperty("systems")
    public List<String> getSystems() {
        return systems;
    }

    /**
     * optional: combat or other system this xtsp is focused on
     * 
     */
    @JsonProperty("systems")
    public void setSystems(List<String> systems) {
        this.systems = systems;
    }

    /**
     * optional: echelon teams the experience applies to
     * 
     */
    @JsonProperty("echelonTeams")
    public List<String> getEchelonTeams() {
        return echelonTeams;
    }

    /**
     * optional: echelon teams the experience applies to
     * 
     */
    @JsonProperty("echelonTeams")
    public void setEchelonTeams(List<String> echelonTeams) {
        this.echelonTeams = echelonTeams;
    }

    /**
     * optional: echelon roles the experience applies to
     * 
     */
    @JsonProperty("echelonRoles")
    public List<String> getEchelonRoles() {
        return echelonRoles;
    }

    /**
     * optional: echelon roles the experience applies to
     * 
     */
    @JsonProperty("echelonRoles")
    public void setEchelonRoles(List<String> echelonRoles) {
        this.echelonRoles = echelonRoles;
    }

    /**
     * optional: competence framework or METL the experience supports
     * 
     */
    @JsonProperty("competencyFramework")
    public List<String> getCompetencyFramework() {
        return competencyFramework;
    }

    /**
     * optional: competence framework or METL the experience supports
     * 
     */
    @JsonProperty("competencyFramework")
    public void setCompetencyFramework(List<String> competencyFramework) {
        this.competencyFramework = competencyFramework;
    }

    /**
     * optional: competencies or METs the experience supports
     * 
     */
    @JsonProperty("competencies")
    public List<String> getCompetencies() {
        return competencies;
    }

    /**
     * optional: competencies or METs the experience supports
     * 
     */
    @JsonProperty("competencies")
    public void setCompetencies(List<String> competencies) {
        this.competencies = competencies;
    }

    /**
     * option: minimum competence level required to complete experience
     * 
     */
    @JsonProperty("competenceRequired")
    public GenInfo.CompetenceRequired getCompetenceRequired() {
        return competenceRequired;
    }

    /**
     * option: minimum competence level required to complete experience
     * 
     */
    @JsonProperty("competenceRequired")
    public void setCompetenceRequired(GenInfo.CompetenceRequired competenceRequired) {
        this.competenceRequired = competenceRequired;
    }

    /**
     * required: training phase xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingPhase")
    public GenInfo.TrainingPhase getTrainingPhase() {
        return trainingPhase;
    }

    /**
     * required: training phase xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingPhase")
    public void setTrainingPhase(GenInfo.TrainingPhase trainingPhase) {
        this.trainingPhase = trainingPhase;
    }

    /**
     * required: training environment xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingEnvironment")
    public GenInfo.TrainingEnvironment getTrainingEnvironment() {
        return trainingEnvironment;
    }

    /**
     * required: training environment xtsp applies to
     * (Required)
     * 
     */
    @JsonProperty("trainingEnvironment")
    public void setTrainingEnvironment(GenInfo.TrainingEnvironment trainingEnvironment) {
        this.trainingEnvironment = trainingEnvironment;
    }

    /**
     * required: a summary level based on contained x-event terrains, conditions, tasks and activities
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    public GenInfo.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * required: a summary level based on contained x-event terrains, conditions, tasks and activities
     * (Required)
     * 
     */
    @JsonProperty("difficultyLevel")
    public void setDifficultyLevel(GenInfo.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    /**
     * optional: any notes the designer feels need to be passed on to other users
     * 
     */
    @JsonProperty("useNotes")
    public Object getUseNotes() {
        return useNotes;
    }

    /**
     * optional: any notes the designer feels need to be passed on to other users
     * 
     */
    @JsonProperty("useNotes")
    public void setUseNotes(Object useNotes) {
        this.useNotes = useNotes;
    }

    /**
     * required: global indicator of measurement unit class
     * (Required)
     * 
     */
    @JsonProperty("measurementUnits")
    public GenInfo.MeasurementUnits getMeasurementUnits() {
        return measurementUnits;
    }

    /**
     * required: global indicator of measurement unit class
     * (Required)
     * 
     */
    @JsonProperty("measurementUnits")
    public void setMeasurementUnits(GenInfo.MeasurementUnits measurementUnits) {
        this.measurementUnits = measurementUnits;
    }

    /**
     * required: automated list of training events xtsp has been used in
     * (Required)
     * 
     */
    @JsonProperty("useHistory")
    public Object getUseHistory() {
        return useHistory;
    }

    /**
     * required: automated list of training events xtsp has been used in
     * (Required)
     * 
     */
    @JsonProperty("useHistory")
    public void setUseHistory(Object useHistory) {
        this.useHistory = useHistory;
    }

    /**
     * optional: keywords that can be used to search for
     * 
     */
    @JsonProperty("keywords")
    public Object getKeywords() {
        return keywords;
    }

    /**
     * optional: keywords that can be used to search for
     * 
     */
    @JsonProperty("keywords")
    public void setKeywords(Object keywords) {
        this.keywords = keywords;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("symbol")
    public Glyph getSymbol() {
        return symbol;
    }

    /**
     * optional: a glyph-symbol image sused to represent a team, role or object on map
     * 
     */
    @JsonProperty("symbol")
    public void setSymbol(Glyph symbol) {
        this.symbol = symbol;
    }

    /**
     * required: point of contact who 'owns' the xtsp
     * (Required)
     * 
     */
    @JsonProperty("authors")
    public List<Author> getAuthors() {
        return authors;
    }

    /**
     * required: point of contact who 'owns' the xtsp
     * (Required)
     * 
     */
    @JsonProperty("authors")
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    /**
     * required: list of changes made - automatically done in XDT
     * (Required)
     * 
     */
    @JsonProperty("changeHistory")
    public Object getChangeHistory() {
        return changeHistory;
    }

    /**
     * required: list of changes made - automatically done in XDT
     * (Required)
     * 
     */
    @JsonProperty("changeHistory")
    public void setChangeHistory(Object changeHistory) {
        this.changeHistory = changeHistory;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GenInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("experienceId");
        sb.append('=');
        sb.append(((this.experienceId == null)?"<null>":this.experienceId));
        sb.append(',');
        sb.append("experienceUuid");
        sb.append('=');
        sb.append(((this.experienceUuid == null)?"<null>":this.experienceUuid));
        sb.append(',');
        sb.append("experienceTitle");
        sb.append('=');
        sb.append(((this.experienceTitle == null)?"<null>":this.experienceTitle));
        sb.append(',');
        sb.append("experienceType");
        sb.append('=');
        sb.append(((this.experienceType == null)?"<null>":this.experienceType));
        sb.append(',');
        sb.append("experienceVersion");
        sb.append('=');
        sb.append(((this.experienceVersion == null)?"<null>":this.experienceVersion));
        sb.append(',');
        sb.append("schemaVersion");
        sb.append('=');
        sb.append(((this.schemaVersion == null)?"<null>":this.schemaVersion));
        sb.append(',');
        sb.append("publishState");
        sb.append('=');
        sb.append(((this.publishState == null)?"<null>":this.publishState));
        sb.append(',');
        sb.append("releaseDate");
        sb.append('=');
        sb.append(((this.releaseDate == null)?"<null>":this.releaseDate));
        sb.append(',');
        sb.append("securityClass");
        sb.append('=');
        sb.append(((this.securityClass == null)?"<null>":this.securityClass));
        sb.append(',');
        sb.append("distributionRestriction");
        sb.append('=');
        sb.append(((this.distributionRestriction == null)?"<null>":this.distributionRestriction));
        sb.append(',');
        sb.append("distributionNotice");
        sb.append('=');
        sb.append(((this.distributionNotice == null)?"<null>":this.distributionNotice));
        sb.append(',');
        sb.append("foreignRestriction");
        sb.append('=');
        sb.append(((this.foreignRestriction == null)?"<null>":this.foreignRestriction));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("occupation");
        sb.append('=');
        sb.append(((this.occupation == null)?"<null>":this.occupation));
        sb.append(',');
        sb.append("occupationDomain");
        sb.append('=');
        sb.append(((this.occupationDomain == null)?"<null>":this.occupationDomain));
        sb.append(',');
        sb.append("occupationSpecialty");
        sb.append('=');
        sb.append(((this.occupationSpecialty == null)?"<null>":this.occupationSpecialty));
        sb.append(',');
        sb.append("organizationType");
        sb.append('=');
        sb.append(((this.organizationType == null)?"<null>":this.organizationType));
        sb.append(',');
        sb.append("organizationName");
        sb.append('=');
        sb.append(((this.organizationName == null)?"<null>":this.organizationName));
        sb.append(',');
        sb.append("organizationEchelon");
        sb.append('=');
        sb.append(((this.organizationEchelon == null)?"<null>":this.organizationEchelon));
        sb.append(',');
        sb.append("platformType");
        sb.append('=');
        sb.append(((this.platformType == null)?"<null>":this.platformType));
        sb.append(',');
        sb.append("weapons");
        sb.append('=');
        sb.append(((this.weapons == null)?"<null>":this.weapons));
        sb.append(',');
        sb.append("systems");
        sb.append('=');
        sb.append(((this.systems == null)?"<null>":this.systems));
        sb.append(',');
        sb.append("echelonTeams");
        sb.append('=');
        sb.append(((this.echelonTeams == null)?"<null>":this.echelonTeams));
        sb.append(',');
        sb.append("echelonRoles");
        sb.append('=');
        sb.append(((this.echelonRoles == null)?"<null>":this.echelonRoles));
        sb.append(',');
        sb.append("competencyFramework");
        sb.append('=');
        sb.append(((this.competencyFramework == null)?"<null>":this.competencyFramework));
        sb.append(',');
        sb.append("competencies");
        sb.append('=');
        sb.append(((this.competencies == null)?"<null>":this.competencies));
        sb.append(',');
        sb.append("competenceRequired");
        sb.append('=');
        sb.append(((this.competenceRequired == null)?"<null>":this.competenceRequired));
        sb.append(',');
        sb.append("trainingPhase");
        sb.append('=');
        sb.append(((this.trainingPhase == null)?"<null>":this.trainingPhase));
        sb.append(',');
        sb.append("trainingEnvironment");
        sb.append('=');
        sb.append(((this.trainingEnvironment == null)?"<null>":this.trainingEnvironment));
        sb.append(',');
        sb.append("difficultyLevel");
        sb.append('=');
        sb.append(((this.difficultyLevel == null)?"<null>":this.difficultyLevel));
        sb.append(',');
        sb.append("useNotes");
        sb.append('=');
        sb.append(((this.useNotes == null)?"<null>":this.useNotes));
        sb.append(',');
        sb.append("measurementUnits");
        sb.append('=');
        sb.append(((this.measurementUnits == null)?"<null>":this.measurementUnits));
        sb.append(',');
        sb.append("useHistory");
        sb.append('=');
        sb.append(((this.useHistory == null)?"<null>":this.useHistory));
        sb.append(',');
        sb.append("keywords");
        sb.append('=');
        sb.append(((this.keywords == null)?"<null>":this.keywords));
        sb.append(',');
        sb.append("symbol");
        sb.append('=');
        sb.append(((this.symbol == null)?"<null>":this.symbol));
        sb.append(',');
        sb.append("authors");
        sb.append('=');
        sb.append(((this.authors == null)?"<null>":this.authors));
        sb.append(',');
        sb.append("changeHistory");
        sb.append('=');
        sb.append(((this.changeHistory == null)?"<null>":this.changeHistory));
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
        result = ((result* 31)+((this.publishState == null)? 0 :this.publishState.hashCode()));
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.occupation == null)? 0 :this.occupation.hashCode()));
        result = ((result* 31)+((this.distributionNotice == null)? 0 :this.distributionNotice.hashCode()));
        result = ((result* 31)+((this.keywords == null)? 0 :this.keywords.hashCode()));
        result = ((result* 31)+((this.difficultyLevel == null)? 0 :this.difficultyLevel.hashCode()));
        result = ((result* 31)+((this.platformType == null)? 0 :this.platformType.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.experienceUuid == null)? 0 :this.experienceUuid.hashCode()));
        result = ((result* 31)+((this.experienceType == null)? 0 :this.experienceType.hashCode()));
        result = ((result* 31)+((this.trainingPhase == null)? 0 :this.trainingPhase.hashCode()));
        result = ((result* 31)+((this.occupationDomain == null)? 0 :this.occupationDomain.hashCode()));
        result = ((result* 31)+((this.experienceId == null)? 0 :this.experienceId.hashCode()));
        result = ((result* 31)+((this.useHistory == null)? 0 :this.useHistory.hashCode()));
        result = ((result* 31)+((this.echelonRoles == null)? 0 :this.echelonRoles.hashCode()));
        result = ((result* 31)+((this.experienceTitle == null)? 0 :this.experienceTitle.hashCode()));
        result = ((result* 31)+((this.experienceVersion == null)? 0 :this.experienceVersion.hashCode()));
        result = ((result* 31)+((this.organizationEchelon == null)? 0 :this.organizationEchelon.hashCode()));
        result = ((result* 31)+((this.useNotes == null)? 0 :this.useNotes.hashCode()));
        result = ((result* 31)+((this.systems == null)? 0 :this.systems.hashCode()));
        result = ((result* 31)+((this.measurementUnits == null)? 0 :this.measurementUnits.hashCode()));
        result = ((result* 31)+((this.securityClass == null)? 0 :this.securityClass.hashCode()));
        result = ((result* 31)+((this.changeHistory == null)? 0 :this.changeHistory.hashCode()));
        result = ((result* 31)+((this.schemaVersion == null)? 0 :this.schemaVersion.hashCode()));
        result = ((result* 31)+((this.organizationName == null)? 0 :this.organizationName.hashCode()));
        result = ((result* 31)+((this.releaseDate == null)? 0 :this.releaseDate.hashCode()));
        result = ((result* 31)+((this.competencyFramework == null)? 0 :this.competencyFramework.hashCode()));
        result = ((result* 31)+((this.competencies == null)? 0 :this.competencies.hashCode()));
        result = ((result* 31)+((this.organizationType == null)? 0 :this.organizationType.hashCode()));
        result = ((result* 31)+((this.competenceRequired == null)? 0 :this.competenceRequired.hashCode()));
        result = ((result* 31)+((this.echelonTeams == null)? 0 :this.echelonTeams.hashCode()));
        result = ((result* 31)+((this.trainingEnvironment == null)? 0 :this.trainingEnvironment.hashCode()));
        result = ((result* 31)+((this.foreignRestriction == null)? 0 :this.foreignRestriction.hashCode()));
        result = ((result* 31)+((this.distributionRestriction == null)? 0 :this.distributionRestriction.hashCode()));
        result = ((result* 31)+((this.occupationSpecialty == null)? 0 :this.occupationSpecialty.hashCode()));
        result = ((result* 31)+((this.weapons == null)? 0 :this.weapons.hashCode()));
        result = ((result* 31)+((this.authors == null)? 0 :this.authors.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GenInfo) == false) {
            return false;
        }
        GenInfo rhs = ((GenInfo) other);
        return ((((((((((((((((((((((((((((((((((((((this.publishState == rhs.publishState)||((this.publishState!= null)&&this.publishState.equals(rhs.publishState)))&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.occupation == rhs.occupation)||((this.occupation!= null)&&this.occupation.equals(rhs.occupation))))&&((this.distributionNotice == rhs.distributionNotice)||((this.distributionNotice!= null)&&this.distributionNotice.equals(rhs.distributionNotice))))&&((this.keywords == rhs.keywords)||((this.keywords!= null)&&this.keywords.equals(rhs.keywords))))&&((this.difficultyLevel == rhs.difficultyLevel)||((this.difficultyLevel!= null)&&this.difficultyLevel.equals(rhs.difficultyLevel))))&&((this.platformType == rhs.platformType)||((this.platformType!= null)&&this.platformType.equals(rhs.platformType))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.experienceUuid == rhs.experienceUuid)||((this.experienceUuid!= null)&&this.experienceUuid.equals(rhs.experienceUuid))))&&((this.experienceType == rhs.experienceType)||((this.experienceType!= null)&&this.experienceType.equals(rhs.experienceType))))&&((this.trainingPhase == rhs.trainingPhase)||((this.trainingPhase!= null)&&this.trainingPhase.equals(rhs.trainingPhase))))&&((this.occupationDomain == rhs.occupationDomain)||((this.occupationDomain!= null)&&this.occupationDomain.equals(rhs.occupationDomain))))&&((this.experienceId == rhs.experienceId)||((this.experienceId!= null)&&this.experienceId.equals(rhs.experienceId))))&&((this.useHistory == rhs.useHistory)||((this.useHistory!= null)&&this.useHistory.equals(rhs.useHistory))))&&((this.echelonRoles == rhs.echelonRoles)||((this.echelonRoles!= null)&&this.echelonRoles.equals(rhs.echelonRoles))))&&((this.experienceTitle == rhs.experienceTitle)||((this.experienceTitle!= null)&&this.experienceTitle.equals(rhs.experienceTitle))))&&((this.experienceVersion == rhs.experienceVersion)||((this.experienceVersion!= null)&&this.experienceVersion.equals(rhs.experienceVersion))))&&((this.organizationEchelon == rhs.organizationEchelon)||((this.organizationEchelon!= null)&&this.organizationEchelon.equals(rhs.organizationEchelon))))&&((this.useNotes == rhs.useNotes)||((this.useNotes!= null)&&this.useNotes.equals(rhs.useNotes))))&&((this.systems == rhs.systems)||((this.systems!= null)&&this.systems.equals(rhs.systems))))&&((this.measurementUnits == rhs.measurementUnits)||((this.measurementUnits!= null)&&this.measurementUnits.equals(rhs.measurementUnits))))&&((this.securityClass == rhs.securityClass)||((this.securityClass!= null)&&this.securityClass.equals(rhs.securityClass))))&&((this.changeHistory == rhs.changeHistory)||((this.changeHistory!= null)&&this.changeHistory.equals(rhs.changeHistory))))&&((this.schemaVersion == rhs.schemaVersion)||((this.schemaVersion!= null)&&this.schemaVersion.equals(rhs.schemaVersion))))&&((this.organizationName == rhs.organizationName)||((this.organizationName!= null)&&this.organizationName.equals(rhs.organizationName))))&&((this.releaseDate == rhs.releaseDate)||((this.releaseDate!= null)&&this.releaseDate.equals(rhs.releaseDate))))&&((this.competencyFramework == rhs.competencyFramework)||((this.competencyFramework!= null)&&this.competencyFramework.equals(rhs.competencyFramework))))&&((this.competencies == rhs.competencies)||((this.competencies!= null)&&this.competencies.equals(rhs.competencies))))&&((this.organizationType == rhs.organizationType)||((this.organizationType!= null)&&this.organizationType.equals(rhs.organizationType))))&&((this.competenceRequired == rhs.competenceRequired)||((this.competenceRequired!= null)&&this.competenceRequired.equals(rhs.competenceRequired))))&&((this.echelonTeams == rhs.echelonTeams)||((this.echelonTeams!= null)&&this.echelonTeams.equals(rhs.echelonTeams))))&&((this.trainingEnvironment == rhs.trainingEnvironment)||((this.trainingEnvironment!= null)&&this.trainingEnvironment.equals(rhs.trainingEnvironment))))&&((this.foreignRestriction == rhs.foreignRestriction)||((this.foreignRestriction!= null)&&this.foreignRestriction.equals(rhs.foreignRestriction))))&&((this.distributionRestriction == rhs.distributionRestriction)||((this.distributionRestriction!= null)&&this.distributionRestriction.equals(rhs.distributionRestriction))))&&((this.occupationSpecialty == rhs.occupationSpecialty)||((this.occupationSpecialty!= null)&&this.occupationSpecialty.equals(rhs.occupationSpecialty))))&&((this.weapons == rhs.weapons)||((this.weapons!= null)&&this.weapons.equals(rhs.weapons))))&&((this.authors == rhs.authors)||((this.authors!= null)&&this.authors.equals(rhs.authors))));
    }


    /**
     * option: minimum competence level required to complete experience
     * 
     */
    public enum CompetenceRequired {

        NOVICE("novice"),
        PRACTICED("practiced"),
        PROFICIENT("proficient"),
        EXPERT("expert");
        private final String value;
        private final static Map<String, GenInfo.CompetenceRequired> CONSTANTS = new HashMap<String, GenInfo.CompetenceRequired>();

        static {
            for (GenInfo.CompetenceRequired c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CompetenceRequired(String value) {
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
        public static GenInfo.CompetenceRequired fromValue(String value) {
            GenInfo.CompetenceRequired constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: a summary level based on contained x-event terrains, conditions, tasks and activities
     * 
     */
    public enum DifficultyLevel {

        EASY("Easy"),
        MODERATE("Moderate"),
        HARD("Hard");
        private final String value;
        private final static Map<String, GenInfo.DifficultyLevel> CONSTANTS = new HashMap<String, GenInfo.DifficultyLevel>();

        static {
            for (GenInfo.DifficultyLevel c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DifficultyLevel(String value) {
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
        public static GenInfo.DifficultyLevel fromValue(String value) {
            GenInfo.DifficultyLevel constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * option: if UNCLASS, who can have access to this experience
     * 
     */
    public enum DistributionRestriction {

        DISTRIBUTION_A("Distribution A"),
        DISTRIBUTION_B("Distribution B"),
        DISTRIBUTION_C("Distribution C"),
        DISTRIBUTION_D("Distribution D"),
        DISTRIBUTION_E("Distribution E"),
        DISTRIBUTION_F("Distribution F");
        private final String value;
        private final static Map<String, GenInfo.DistributionRestriction> CONSTANTS = new HashMap<String, GenInfo.DistributionRestriction>();

        static {
            for (GenInfo.DistributionRestriction c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DistributionRestriction(String value) {
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
        public static GenInfo.DistributionRestriction fromValue(String value) {
            GenInfo.DistributionRestriction constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: type of experience xtsp is designed to support
     * 
     */
    public enum ExperienceType {

        TACTICAL_EX_WITHOUT_TROOPS_TEWT("Tactical Ex Without Troops (TEWT)"),
        STAFF_EXERCISE_STAFFEX("Staff Exercise (STAFFEX)"),
        COMMAND_POST_EXERCISE_CPX("Command Post Exercise (CPX)"),
        FIELD_TRAINING_EXERCISE_FTX("Field Training Exercise (FTX)"),
        FIRE_COORDINATION_EXERCISE_FCX("Fire Coordination Exercise (FCX)"),
        COMBINED_ARMS_LIVE_FIRE_EXERCISE_CALFEX("Combined Arms Live Fire Exercise (CALFEX)"),
        VIRTUAL_SITUATIONAL_TRAINING_EXERCISE_STX_V("Virtual Situational Training Exercise (STX-V)"),
        LIVE_SITUATIONAL_TRAINING_EXERCISE_STX("Live Situational Training Exercise (STX)"),
        SELF_INSTRUCTION("Self-Instruction"),
        KNOWLEDGE_DRILL("Knowledge Drill"),
        PRE_LIVE_FIRE_SIMULATION_PLFS("Pre-Live Fire Simulation (PLFS)"),
        GUNNERY_SKILLS_TEST_GST("Gunnery Skills Test (GST)"),
        BATTLE_TASK_BATTLE_DRILLS("Battle Task / Battle Drills"),
        LIVE_FIRE_PRACTICE_LFX("Live-Fire Practice (LFX)"),
        SEMI_LIVE_FIRE_EXERCISE_PRACTICE_SLFX("Semi-Live Fire Exercise (Practice) (SLFX)"),
        BASIC_SKILLS_PRACTICE_BSP("Basic Skills Practice (BSP)"),
        DEMONSTRATION("Demonstration"),
        VICARIOUS_CASE_STUDY("Vicarious Case-Study"),
        DIRECT_SKILL_PRACTICE("Direct Skill-Practice"),
        MAP_EXERCISE("Map Exercise"),
        MISSION_READINESS_EXERCISE("Mission readiness Exercise");
        private final String value;
        private final static Map<String, GenInfo.ExperienceType> CONSTANTS = new HashMap<String, GenInfo.ExperienceType>();

        static {
            for (GenInfo.ExperienceType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ExperienceType(String value) {
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
        public static GenInfo.ExperienceType fromValue(String value) {
            GenInfo.ExperienceType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: global indicator of measurement unit class
     * 
     */
    public enum MeasurementUnits {

        METRIC("metric"),
        ENGLISH("english");
        private final String value;
        private final static Map<String, GenInfo.MeasurementUnits> CONSTANTS = new HashMap<String, GenInfo.MeasurementUnits>();

        static {
            for (GenInfo.MeasurementUnits c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MeasurementUnits(String value) {
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
        public static GenInfo.MeasurementUnits fromValue(String value) {
            GenInfo.MeasurementUnits constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: o*net occupation this experience applies to
     * 
     */
    public enum Occupation {

        US_ARMY("US Army"),
        US_NAVY("US Navy"),
        US_AIRFORCE("US Airforce"),
        US_MARINES("US Marines"),
        US_COAST_GUARD("US Coast-Guard");
        private final String value;
        private final static Map<String, GenInfo.Occupation> CONSTANTS = new HashMap<String, GenInfo.Occupation>();

        static {
            for (GenInfo.Occupation c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Occupation(String value) {
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
        public static GenInfo.Occupation fromValue(String value) {
            GenInfo.Occupation constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: domain of the occupation this experience applies to
     * 
     */
    public enum OccupationDomain {

        GROUND_FORCES("Ground Forces"),
        SCIENCE_MEDICAL("Science-Medical"),
        AVIATION_AIR_DEFENSE("Aviation-Air Defense"),
        CYBER_AND_SIGNAL_ANALYSIS("Cyber and Signal Analysis");
        private final String value;
        private final static Map<String, GenInfo.OccupationDomain> CONSTANTS = new HashMap<String, GenInfo.OccupationDomain>();

        static {
            for (GenInfo.OccupationDomain c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OccupationDomain(String value) {
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
        public static GenInfo.OccupationDomain fromValue(String value) {
            GenInfo.OccupationDomain constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: occupation specialty experience applies to (proponent, MOS, NEC, OSC...)
     * 
     */
    public enum OccupationSpecialty {

        INFANTRY("Infantry"),
        ARMOR("Armor"),
        AVIATION("Aviation"),
        INTELLIGENCE("Intelligence"),
        ENGINEERING("Engineering"),
        MILITARY_POLICE("Military police"),
        QUARTERMASTER("Quartermaster"),
        TRANSPORTATION("Transportation"),
        FIELD_ARTILLERY("Field-artillery"),
        SIGNALS("Signals"),
        AIR_DEFENSE("Air-defense"),
        ORDNANCE("Ordnance"),
        CYBER_SECURITY("Cyber-security"),
        FINANCIAL("Financial"),
        LEGAL("Legal"),
        CBRN("CBRN");
        private final String value;
        private final static Map<String, GenInfo.OccupationSpecialty> CONSTANTS = new HashMap<String, GenInfo.OccupationSpecialty>();

        static {
            for (GenInfo.OccupationSpecialty c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OccupationSpecialty(String value) {
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
        public static GenInfo.OccupationSpecialty fromValue(String value) {
            GenInfo.OccupationSpecialty constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: organization echelon the experience apply to
     * 
     */
    public enum OrganizationEchelon {

        BATTALION_REGIMENT("battalion/regiment"),
        COMPANY("company"),
        PLATOON("platoon"),
        SECTION("section"),
        SQUAD("squad"),
        TEAM("team"),
        CREW("crew"),
        ROLE("role");
        private final String value;
        private final static Map<String, GenInfo.OrganizationEchelon> CONSTANTS = new HashMap<String, GenInfo.OrganizationEchelon>();

        static {
            for (GenInfo.OrganizationEchelon c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OrganizationEchelon(String value) {
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
        public static GenInfo.OrganizationEchelon fromValue(String value) {
            GenInfo.OrganizationEchelon constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: type of occupation domain organization does this experience apply to
     * 
     */
    public enum OrganizationType {

        RIFLE_INFANTRY("rifle-infantry"),
        MOBILIZED_INFANTRY("mobilized-infantry"),
        RANGER_INFANTRY("ranger-infantry"),
        SCOUT_RECONNAISANCE("scout & reconnaisance");
        private final String value;
        private final static Map<String, GenInfo.OrganizationType> CONSTANTS = new HashMap<String, GenInfo.OrganizationType>();

        static {
            for (GenInfo.OrganizationType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OrganizationType(String value) {
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
        public static GenInfo.OrganizationType fromValue(String value) {
            GenInfo.OrganizationType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: date xtsp was published
     * 
     */
    public enum PublishState {

        DRAFT("Draft"),
        APPROVED("Approved"),
        UPDATE_IN_PROGRESS("UpdateInProgress"),
        DEPRECATED("Deprecated");
        private final String value;
        private final static Map<String, GenInfo.PublishState> CONSTANTS = new HashMap<String, GenInfo.PublishState>();

        static {
            for (GenInfo.PublishState c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        PublishState(String value) {
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
        public static GenInfo.PublishState fromValue(String value) {
            GenInfo.PublishState constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: training environment xtsp applies to
     * 
     */
    public enum TrainingEnvironment {

        VBS_FULL_SYNTHETIC("VBS (full synthetic)"),
        STEELR_RIDE_FULL_SYNTHETIC("STEELR-RIDE (full synthetic)"),
        RVCT_S_FULL_SYNTHETIC("RVCT-S (full synthetic)"),
        RVCT_G_FULL_SYNTHETIC("RVCT-G (full synthetic)"),
        RVCT_A_FULL_SYNTHETIC("RVCT-A (full synthetic)"),
        EST_SEMI_SYNTHETIC("EST (semi-synthetic)"),
        SVT_SEMI_SYNTHETIC("SVT (semi-synthetic)"),
        SI_VT_SEMI_LIVE("SiVT (semi-live)"),
        COLLECTIVE_LVCIA("Collective (LVCIA)"),
        LIVE_ILTE("Live (ILTE)");
        private final String value;
        private final static Map<String, GenInfo.TrainingEnvironment> CONSTANTS = new HashMap<String, GenInfo.TrainingEnvironment>();

        static {
            for (GenInfo.TrainingEnvironment c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TrainingEnvironment(String value) {
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
        public static GenInfo.TrainingEnvironment fromValue(String value) {
            GenInfo.TrainingEnvironment constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * required: training phase xtsp applies to
     * 
     */
    public enum TrainingPhase {

        CRAWL("crawl"),
        WALK("walk"),
        RUN("run");
        private final String value;
        private final static Map<String, GenInfo.TrainingPhase> CONSTANTS = new HashMap<String, GenInfo.TrainingPhase>();

        static {
            for (GenInfo.TrainingPhase c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TrainingPhase(String value) {
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
        public static GenInfo.TrainingPhase fromValue(String value) {
            GenInfo.TrainingPhase constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
