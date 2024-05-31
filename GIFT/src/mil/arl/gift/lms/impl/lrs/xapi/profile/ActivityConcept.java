package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.InteractionType;
import com.rusticisoftware.tincan.LanguageMap;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.InteractionComponent;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of a static / canonical xAPI Activity defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ActivityConcept extends AbstractProfileComponent {

    /**
     * default value for Activity '@context'
     */
    private static final String defaultContext = "https://w3id.org/xapi/profiles/activity-context";

    /**
     * Activity Definition that describes the id
     */
    private ActivityDefinition activityDefinition;

    /**
     * Private variable for '@context' which is singular. null when 'manyAtContext' non-null
     */
    private URI atContext;

    /**
     * Private variable for '@context' which contains 'defaultContext' plus user supplied URIs. null when ''atContext' non-null
     */
    private List<URI> manyAtContext;
    
    /**
     * Set Activity Concept fields from SPARQL query result
     * 
     * @param id - identifier for the Activity Concept
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ActivityConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        // TODO: handle @context
        setAtContext(defaultContextAsURI());
        ActivityDefinition aDef = new ActivityDefinition();
        if(src.getActivityType() != null) {
            try {
                aDef.setType(src.getActivityType());
            } catch (URISyntaxException e) {
                throw new LmsXapiProfileException("Unable to set activity type from SPARQL result!", e);
            }
        }
        if(src.getActivityName() != null && src.getActivityNameLang() != null) {
            LanguageMap lmap = new LanguageMap();
            lmap.put(src.getActivityNameLang(), src.getActivityName());
            aDef.setName(lmap);
        }
        if(src.getActivityDescription() != null && src.getActivityDescriptionLang() != null) {
            LanguageMap lmap = new LanguageMap();
            lmap.put(src.getActivityDescriptionLang(), src.getActivityDescription());
            aDef.setDescription(lmap);
        }
        if(src.getActivityMoreInfo() != null) {
            try {
                aDef.setMoreInfo(src.getActivityMoreInfo());
            } catch (URISyntaxException e) {
                throw new LmsXapiProfileException("Unable to set activity moreInfo from SPARQL result!", e);
            }
        }
        if(src.getActivityInteractionType() != null) {
            aDef.setInteractionType(InteractionType.valueOf(src.getActivityInteractionType()));
        }
        if(src.getActivityExtensions() != null && 
                CollectionUtils.isNotEmpty(src.getActivityExtensions())) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode asJson = mapper.valueToTree(src.getActivityExtensions());
            try {
                aDef.setExtensions(new Extensions(asJson));
            } catch (URISyntaxException e) {
                throw new LmsXapiProfileException("Unable to set activity extension from SPARQL result!", e);
            }
        }
        if(src.getActivityCorrectResponsePattern() != null && 
                CollectionUtils.isNotEmpty(src.getActivityCorrectResponsePattern())) {
            aDef.setCorrectResponsesPattern(new ArrayList<String>(src.getActivityCorrectResponsePattern()));
        }
        if(src.getActivityChoices() != null && 
                CollectionUtils.isNotEmpty(src.getActivityChoices())) {
            aDef.setChoices(new ArrayList<InteractionComponent>(src.getActivityChoices()));
        }
        if(src.getActivityScale() != null && 
                CollectionUtils.isNotEmpty(src.getActivityScale())) {
            aDef.setScale(new ArrayList<InteractionComponent>(src.getActivityScale()));
        }
        if(src.getActivitySource() != null && 
                CollectionUtils.isNotEmpty(src.getActivitySource())) {
            aDef.setSource(new ArrayList<InteractionComponent>(src.getActivitySource()));
        }
        if(src.getActivityTarget() != null && 
                CollectionUtils.isNotEmpty(src.getActivityTarget())) {
            aDef.setTarget(new ArrayList<InteractionComponent>(src.getActivityTarget()));
        }
        if(src.getActivitySteps() != null && 
                CollectionUtils.isNotEmpty(src.getActivitySteps())) {
            aDef.setSteps(new ArrayList<InteractionComponent>(src.getActivitySteps()));
        }
        setActivityDefinition(aDef);
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result
     * 
     * @param id - Activity Concept identifier
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult canonicalActivitySparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createActivityConceptQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute canoncial activity SPARQL query!", e);
            }
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchConcepts(id);
        } else {
            throw new LmsXapiProfileException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, all concepts are searched.
     * 
     * @param id - Activity Concept identifier
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult canonicalActivitySparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchConcepts(id);
        } else {
            return canonicalActivitySparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, all concepts within GIFT xAPI Profile are searched.
     * 
     * @param id - Activity Concept identifier
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult giftCanonicalActivitySparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchGiftConcepts(id);
        } else {
            return canonicalActivitySparqlQuery(id);
        }
    }
    
    /**
     * Manages conversion of defaultContext String to URI
     * 
     * @return defaultContext as URI
     * 
     * @throws LmsXapiProfileException when unable to create URI from defaultContext
     */
    private static URI defaultContextAsURI() throws LmsXapiProfileException {
        try {
            return new URI(defaultContext);
        } catch (URISyntaxException e) {
            throw new LmsXapiProfileException("Unable to convert defaultContext to URI!", e);
        }
    }

    /**
     * Compares passed in URI atContext to defaultContext
     * 
     * @param atContext - URI to compare to default context URI
     * 
     * @return true if match, false otherwise
     * 
     * @throws LmsXapiProfileException when unable to create URI from defaultContext
     */
    private boolean isDefaultContext(URI atContext) throws LmsXapiProfileException {
        URI context = defaultContextAsURI();
        if(context != null && atContext != null && atContext.toString().equals(context.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true when atContext contains the defaultContext
     * 
     * @param atContext - ArrayList of URIs for the ActivityConcept
     * 
     * @return true if atContext contains defaultContext, false otherwise
     * 
     * @throws LmsXapiProfileException when unable to create URI from defaultContext
     */
    private boolean contextArrayContainsDefault(List<URI> atContext) throws LmsXapiProfileException {
        if(atContext == null) {
            throw new IllegalArgumentException("atContext collection can not be null!");
        }
        for(URI context : atContext) {
            if(isDefaultContext(context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for activityDefinition
     * 
     * @return the ActivityDefinition associated with this Activity Concept
     */
    public ActivityDefinition getActivityDefinition() {
        return activityDefinition;
    }

    /**
     * Setter for activityDefinition
     * 
     * @param activityDefinition
     */
    private void setActivityDefinition(ActivityDefinition activityDefinition) {
        if(activityDefinition == null) {
            throw new IllegalArgumentException("activityDefinition can not be null!");
        }
        this.activityDefinition = activityDefinition;
    }

    /**
     * Getter for singular atContext, null when manyAtContext non-null
     * 
     * @return Activity Concept default '@context' or null
     */
    public URI getAtContext() {
        return atContext;
    }

    /**
     * Getter for collection of atContext, null when atContext non-null
     * 
     * @return Activity Concept collection of '@context' URIs, one of them being the default value
     */
    public List<URI> getAtContextCollection() {
        return manyAtContext;
    }

    /**
     * Setter for singular atContext, handles 'atContext' vs 'manyAtContext' based on comparison between 'atContext' and 'defaultContext'
     * 
     * @param atContext - URI value to set as '@context' for this Activity Concept
     * 
     * @throws LmsXapiProfileException when unable to create URI from defaultContext
     */
    private void setAtContext(URI atContext) throws LmsXapiProfileException {
        if(atContext == null) {
            throw new IllegalArgumentException("atContext collection can not be null!");
        }
        if(isDefaultContext(atContext)) {
            this.atContext = atContext;
            this.manyAtContext = null;
        } else {
            this.atContext = null;
            List<URI> allContext = new ArrayList<URI>();
            allContext.add(atContext); 
            allContext.add(defaultContextAsURI());
            this.manyAtContext = allContext;
        }
    }

    /**
     * Setter for collection manyAtContext, adds 'defaultContext' to the array if not present
     * 
     * @param atContext - Array of URIs to set as '@context' for this Activity Concept
     * 
     * @throws LmsXapiProfileException when unable to create URI from defaultContext
     */
    // TODO: handle @context
    @SuppressWarnings("unused")
    private void setAtContext(List<URI> atContext) throws LmsXapiProfileException {
        this.atContext = null;
        if(atContext == null) {
            throw new IllegalArgumentException("atContext collection can not be null!");
        }
        if(contextArrayContainsDefault(atContext)) {
            this.manyAtContext = atContext;
        } else {
            atContext.add(defaultContextAsURI());
            this.manyAtContext = atContext;
        }
    }

    /**
     * Convert from Activity Concept to Activity
     * 
     * @return Activity representation of xAPI Profile Activity Concept
     */
    public Activity asActivity() throws LmsXapiActivityException {
        Activity a = new Activity(getId());
        a.setDefinition(getActivityDefinition());
        return a;
    }
}

