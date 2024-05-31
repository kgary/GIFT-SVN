package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.List;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Attachment;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation of an xAPI Profile Statement Template.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractStatementTemplate extends AbstractProfileComponentDescribed {
    
    /** Verb used within matching xAPI Statement */
    protected VerbConcept verb;
    /** Activity Type of object within matching xAPI Statement */
    protected ActivityTypeConcept objectActivityType;
    /** Activity Type(s) of Activities found within Grouping Context Activities within matching xAPI Statement */
    protected List<ActivityTypeConcept> contextGroupingActivityType;
    /** Activity Type(s) of Activities found within Parent Context Activities within matching xAPI Statement */
    protected List<ActivityTypeConcept> contextParentActivityType;
    /** Activity Type(s) of Activities found within Other Context Activities within matching xAPI Statement */
    protected List<ActivityTypeConcept> contextOtherActivityType;
    /** Activity Type(s) of Activities found within Category Context Activities within matching xAPI Statement */
    protected List<ActivityTypeConcept> contextCategoryActivityType;
    /** Usage Type(s) of attachments within matching xAPI Statement */
    protected List<AttachmentUsageTypeConcept> attachmentUsageType;
    /** Statement Template that matches SubStatement object within matching xAPI Statement */
    protected List<AbstractStatementTemplate> objectStatementRefTemplate;
    /** Statement Template that matches a referenced statement within matching xAPI Statement */
    protected List<AbstractStatementTemplate> contextStatementRefTemplate;
    /** Collection of Statement Template rules */
    protected List<StatementTemplateRule> rules;
    
    /**
     * Set Statement Template fields from SPARQL query result
     * 
     * @param id - identifier for the xAPI Statement Template
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set id or derive profile component type
     */
    public AbstractStatementTemplate(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        if(src.getVerb() != null) {
            SparqlResult verb = conceptRelationSparqlQuery(src.getVerb(), true);
            setVerbConcept(new VerbConcept(src.getVerb(), verb));
        }
        if(src.getObjectActivityType() != null) {
            SparqlResult objectActivityType = conceptRelationSparqlQuery(src.getObjectActivityType(), true);
            setObjectActivityType(new ActivityTypeConcept(src.getObjectActivityType(), objectActivityType));
        }
        for(String groupingAT : src.getContextGroupingActivityType()) {
            SparqlResult groupingActivityType = conceptRelationSparqlQuery(groupingAT, true);
            addGroupingActivityType(new ActivityTypeConcept(groupingAT, groupingActivityType));
        }
        for(String parentAT : src.getContextParentActivityType()) {
            SparqlResult parentActivityType = conceptRelationSparqlQuery(parentAT, true);
            addParentActivityType(new ActivityTypeConcept(parentAT, parentActivityType));
        }
        for(String otherAT : src.getContextOtherActivityType()) {
            SparqlResult otherActivityType = conceptRelationSparqlQuery(otherAT, true);
            addOtherActivityType(new ActivityTypeConcept(otherAT, otherActivityType));
        }
        for(String categoryAT : src.getContextCategoryActivityType()) {
            SparqlResult categoryActivityType = conceptRelationSparqlQuery(categoryAT, true);
            addCategoryActivityType(new ActivityTypeConcept(categoryAT, categoryActivityType));
        }
        for(String attachmentUT : src.getAttachmentUsageType()) {
            SparqlResult attachmentUsageType = conceptRelationSparqlQuery(attachmentUT, true);
            addCategoryActivityType(new ActivityTypeConcept(attachmentUT, attachmentUsageType));
        }
        // TODO: handle statement template rules
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result
     *  
     * @param id - identifier for xAPI Profile component
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult statementTemplateSparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createStatementTemplateQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute Statement Template SPARQL query!", e);
            }
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchTemplates(id);
        } else {
            throw new LmsXapiProfileException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, all statement templates are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult statementTemplateSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchTemplates(id);
        } else {
            return statementTemplateSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, statement templates within GIFT xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult giftStatementTemplateSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchGiftTemplates(id);
        } else {
            return statementTemplateSparqlQuery(id);
        }
    }
    
    /**
     * @return VerbConcept for the Statement Template, can be null
     */
    public VerbConcept getVerbConcept() {
        return verb;
    }
    
    /**
     * @return Verb for the Statement Template, can be null
     */
    public Verb getVerb() {
        return getVerbConcept() != null ? getVerbConcept().asVerb() : null;
    }
    
    /**
     * @return Id of the Statement Template Verb if set, null otherwise
     */
    public URI getVerbId() {
        return getVerb() != null ? getVerb().getId() : null;
    }
    
    /**
     * Setter for verb
     * 
     * @param verbConcept - Verb Concept to set as Verb
     */
    private void setVerbConcept(VerbConcept verbConcept) {
        if(verbConcept == null) {
            throw new IllegalArgumentException("verbConcept can not be null!");
        }
        this.verb = verbConcept;
    }
    
    /**
     * @return Object Activity Type for the Statement Template, can be null
     */
    public ActivityTypeConcept getObjectActivityType() {
        return objectActivityType;
    }
    
    /**
     * Setter for Object Activity Type
     * 
     * @param activityTypeConcept - Activity Type Concept to set as Object Activity Type
     */
    private void setObjectActivityType(ActivityTypeConcept activityTypeConcept) {
        if(activityTypeConcept == null) {
            throw new IllegalArgumentException("activityTypeConcept can not be null!");
        }
        this.objectActivityType = activityTypeConcept;
    }
    
    /**
     * @return Collection of Activity Type Concepts, can be null
     */
    public List<ActivityTypeConcept> getGroupingActivityType(){
        return contextGroupingActivityType;
    }
    
    /**
     * Setter for Grouping Activity Type
     * 
     * @param groupingActivityTypeConcepts - Collection of Activity Type Concepts
     */
    private void setGroupingActivityType(List<ActivityTypeConcept> groupingActivityTypeConcepts) {
        if(groupingActivityTypeConcepts == null) {
            throw new IllegalArgumentException("groupingActivityTypeConcepts can not be null!");
        }
        this.contextGroupingActivityType = groupingActivityTypeConcepts;
    }
    
    /**
     * Add ActivityTypeConcept to collection of Grouping Activity Type Concepts
     * 
     * @param groupingActivityTypeConcept - ActivityTypeConcept to add
     */
    public void addGroupingActivityType(ActivityTypeConcept groupingActivityTypeConcept) {
        List<ActivityTypeConcept> coll = existingOrNovel(contextGroupingActivityType);
        coll.add(groupingActivityTypeConcept);
        setGroupingActivityType(coll);
    }
    
    /**
     * @return Collection of Activity Type Concepts, can be null
     */
    public List<ActivityTypeConcept> getParentActivityType(){
        return contextParentActivityType;
    }
    
    /**
     * Setter for Parent Activity Type
     * 
     * @param parentActivityTypeConcepts - Collection of Activity Type Concepts
     */
    private void setParentActivityType(List<ActivityTypeConcept> parentActivityTypeConcepts) {
        if(parentActivityTypeConcepts == null) {
            throw new IllegalArgumentException("parentActivityTypeConcepts can not be null!");
        }
        this.contextParentActivityType = parentActivityTypeConcepts;
    }
    
    /**
     * Add ActivityTypeConcept to collection of Parent Activity Type Concepts
     * 
     * @param parentActivityTypeConcept - ActivityTypeConcept to add
     */
    public void addParentActivityType(ActivityTypeConcept parentActivityTypeConcept) {
        List<ActivityTypeConcept> coll = existingOrNovel(contextParentActivityType);
        coll.add(parentActivityTypeConcept);
        setParentActivityType(coll);
    }
    
    /**
     * @return Collection of Activity Type Concepts, can be null
     */
    public List<ActivityTypeConcept> getOtherActivityType(){
        return contextOtherActivityType;
    }
    
    /**
     * Setter for Other Activity Type
     * 
     * @param otherActivityTypeConcepts - Collection of Activity Type Concepts
     */
    private void setOtherActivityType(List<ActivityTypeConcept> otherActivityTypeConcepts) {
        if(otherActivityTypeConcepts == null) {
            throw new IllegalArgumentException("otherActivityTypeConcepts can not be null!");
        }
        this.contextOtherActivityType = otherActivityTypeConcepts;
    }
    
    /**
     * Add ActivityTypeConcept to collection of Other Activity Type Concepts
     * 
     * @param otherActivityTypeConcept - ActivityTypeConcept to add
     */
    public void addOtherActivityType(ActivityTypeConcept otherActivityTypeConcept) {
        List<ActivityTypeConcept> coll = existingOrNovel(contextOtherActivityType);
        coll.add(otherActivityTypeConcept);
        setOtherActivityType(coll);
    }
    
    /**
     * @return Collection of Activity Type Concepts, can be null
     */
    public List<ActivityTypeConcept> getCategoryActivityType() {
        return contextCategoryActivityType;
    }
    
    /**
     * Setter for Category Activity Type
     * 
     * @param categoryActivityTypeConcepts - Collection of Activity Type Concepts
     */
    private void setCategoryActivityType(List<ActivityTypeConcept> categoryActivityTypeConcepts) {
        if(categoryActivityTypeConcepts == null) {
            throw new IllegalArgumentException("categoryActivityTypeConcepts can not be null!");
        }
        this.contextCategoryActivityType = categoryActivityTypeConcepts;
    }
    
    /**
     * Add ActivityTypeConcept to collection of Category Activity Type Concepts
     * 
     * @param categoryActivityTypeConcept - ActivityTypeConcept to add
     */
    public void addCategoryActivityType(ActivityTypeConcept categoryActivityTypeConcept) {
        List<ActivityTypeConcept> coll = existingOrNovel(contextCategoryActivityType);
        coll.add(categoryActivityTypeConcept);
        setCategoryActivityType(coll);
    }
    
    /**
     * @return Attachment Usage Type Concept collection, can be null
     */
    public List<AttachmentUsageTypeConcept> getAttachmentUsageType(){
        return attachmentUsageType;
    }
    
    /**
     * Setter for Attachment Usage Type
     * 
     * @param attachmentUsageTypes - Collection of Attachment Usage Types
     */
    private void setAttachmentUsageType(List<AttachmentUsageTypeConcept> attachmentUsageTypes) {
        if(attachmentUsageTypes == null) {
            throw new IllegalArgumentException("attachmentUsageTypes can not be null!");
        }
        this.attachmentUsageType = attachmentUsageTypes;
    }
    
    /**
     * Add Attachment Usage Type Concept
     * 
     * @param attachmentUsageT - Attachment Usage Type Concept to add
     */
    public void addAttachmentUsageType(AttachmentUsageTypeConcept attachmentUsageT) {
        List<AttachmentUsageTypeConcept> coll = existingOrNovel(attachmentUsageType);
        coll.add(attachmentUsageT);
        setAttachmentUsageType(coll);
    }
    
    /**
     * @return Object Statement Template, can be null
     */
    public List<AbstractStatementTemplate> getObjectStatementRefTemplate() {
        return objectStatementRefTemplate;
    }
    
    /**
     * Setter for Object Statement Template
     * 
     * @param statementTemplates - Collection of Object Statement Templates
     */
    private void setObjectStatementRefTemplate(List<AbstractStatementTemplate> statementTemplates) {
        if(statementTemplates == null) {
            throw new IllegalArgumentException("statementTemplates can not be null!");
        }
        this.objectStatementRefTemplate = statementTemplates;
    }
    
    /**
     * Add Object Statement Template
     * 
     * @param statementTempalte - Statement Template to add
     */
    public void addObjectStatementRefTemplate(AbstractStatementTemplate statementTempalte) {
        List<AbstractStatementTemplate> coll = existingOrNovel(objectStatementRefTemplate);
        coll.add(statementTempalte);
        setObjectStatementRefTemplate(coll);
    }
    
    /**
     * @return Context Statement Template, can be null
     */
    public List<AbstractStatementTemplate> getContextStatementRefTemplate() {
        return contextStatementRefTemplate;
    }
    
    /**
     * Setter for Context Statement Template
     * 
     * @param statementTemplates - Collection of Statement Templates
     */
    private void setContextStatementRefTemplate(List<AbstractStatementTemplate> statementTemplates) {
        if(statementTemplates == null) {
            throw new IllegalArgumentException("statementTemplates can not be null!");
        }
        this.contextStatementRefTemplate = statementTemplates;
    }
    
    /**
     * Add Context Statement Template
     *  
     * @param statementTemplate - Statement Template to add
     */
    public void addContextStatementRefTemplate(AbstractStatementTemplate statementTemplate) {
        List<AbstractStatementTemplate> coll = existingOrNovel(contextStatementRefTemplate);
        coll.add(statementTemplate);
        setContextStatementRefTemplate(coll);
    }
    
    /**
     * @return Collection of Statement Template Rules
     */
    public List<StatementTemplateRule> getRules() {
        return rules;
    }
    
    /**
     * Setter for Statement Template Rules
     * 
     * @param rules - Collection of Statement Template Rules
     */
    private void setRules(List<StatementTemplateRule> rules) {
        if(rules == null) {
            throw new IllegalArgumentException("rules can not be null!");
        }
        this.rules = rules;
    }
    
    /**
     * Add rule
     * 
     * @param r - Statement Template Rule to add
     */
    public void addRule(StatementTemplateRule r) {
        List<StatementTemplateRule> coll = existingOrNovel(rules);
        coll.add(r);
        setRules(coll);
    }
    
    /**
     * Determine if all Activity Types are found within collection of Activities
     * 
     * @param concepts - Activity Types to check against
     * @param activities - Collection of Activities to check
     * 
     * @return true when all Activity Type Concepts are found within collection of Activities
     */
    private boolean matchContextActivities(List<ActivityTypeConcept> concepts, List<Activity> activities) {
        for(ActivityTypeConcept c : concepts) {
            boolean match = false;
            for(Activity a : activities) {
                if(c.withinActivity(a)) {
                    match = true;
                    break;
                }
            }
            if(!match) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare Statement Template determining properties to xAPI Statement to determine if
     * xAPI Statement follows Statement Template.
     * 
     * @param statement - xAPI Statement to compare to Statement Template
     * 
     * @return true if statement conforms to specified determining properties, false otherwise
     * 
     * @throws LmsInvalidStatementException when passed in xAPI statement is invalid
     */
    public boolean statementMatchDeterminingProperties(Statement statement) throws LmsInvalidStatementException {
        // Check Against Determing Properties
        // -> Verb (optional)
        if(getVerbId() != null) {
            // statement validation check
            if(statement.getVerb() == null || statement.getVerb().getId() == null) {
                throw new LmsInvalidStatementException("Invalid Statement - null Verb or Verb Id!");
            }
            // comparison
            if(!getVerbId().toString().equals(statement.getVerb().getId().toString())) {
                return false;
            }
        }
        // -> object Activity Type (optional)
        if(getObjectActivityType() != null) {
            // statement validation check
            if(statement.getObject() == null) {
                throw new LmsInvalidStatementException("Invalid Statement - null Object!");
            }
            // comparison
            if(!(statement.getObject() instanceof Activity)) {
                return false;
            }
            if(!getObjectActivityType().withinActivity((Activity) statement.getObject())) {
                return false;
            }
        }
        // -> attachment usage types (optional)
        if(getAttachmentUsageType() != null && CollectionUtils.isNotEmpty(getAttachmentUsageType())) {
            List<Attachment> stmtAttachments = statement.getAttachments();
            // mismatch on determining property but not invalid statement
            if(stmtAttachments == null || CollectionUtils.isEmpty(stmtAttachments)) {
                return false;
            } 
            // all usageTypes that must be found across statement attachments
            for(AttachmentUsageTypeConcept c : getAttachmentUsageType()) {
                boolean match = false;
                for(Attachment a : stmtAttachments) {
                    if(c.asAttachmentUsageType().toString().equals(a.getUsageType().toString())) {
                        match = true;
                        break;
                    }
                }
                if(!match) {
                    return false;
                }
            }
        }
        // -> context activity types (optional)
        // --> parent
        if(getParentActivityType() != null && CollectionUtils.isNotEmpty(getParentActivityType())) {
            if(statement.getContext() == null) {
                return false;
            }
            if(!matchContextActivities(getParentActivityType(), ContextActivitiesHelper.getParentActivities(statement.getContext()))) {
                return false;
            }
        }
        // --> grouping
        if(getGroupingActivityType() != null && CollectionUtils.isNotEmpty(getGroupingActivityType())) {
            if(statement.getContext() == null) {
                return false;
            }
            if(!matchContextActivities(getGroupingActivityType(), ContextActivitiesHelper.getGroupingActivities(statement.getContext()))) {
                return false;
            }
        }
        // --> category
        if(getCategoryActivityType() != null && CollectionUtils.isNotEmpty(getCategoryActivityType())) {
            if(statement.getContext() == null) {
                return false;
            }
            if(!matchContextActivities(getCategoryActivityType(), ContextActivitiesHelper.getCategoryActivities(statement.getContext()))) {
                return false;
            }
        }
        // --> other
        if(getOtherActivityType() != null && CollectionUtils.isNotEmpty(getOtherActivityType())) {
            if(statement.getContext() == null) {
                return false;
            }
            if(!matchContextActivities(getOtherActivityType(), ContextActivitiesHelper.getOtherActivities(statement.getContext()))) {
                return false;
            }
        }
        // no checks failed, return true
        return true;
    }
    
    /**
     * Compare xAPI Statement to Statement Template.
     * 
     * @param statement - xAPI Statement to compare to Statement Template
     *  
     * @throws LmsInvalidStatementException when xAPI Statement is invalid or does not conform to Statement Template
     */
    public void matchingStatement(Statement statement) throws LmsInvalidStatementException {
        if(!statementMatchDeterminingProperties(statement)) {
            throw new LmsInvalidStatementException("unable to create Statement that conforms to this Statement Template's Determining Properties!");
        }
    }
}
