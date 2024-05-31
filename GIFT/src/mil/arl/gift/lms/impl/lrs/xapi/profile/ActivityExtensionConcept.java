package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.Extensions;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;
import com.rusticisoftware.tincan.Activity;

/**
 * Representation of an Activity Extension Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ActivityExtensionConcept extends AbstractExtensionConcept {

    /**
     * An array of activity type URIs that this extension is recommended for use with (extending to narrower of the same).
     */
    private List<URI> recommendedActivityTypes;
    
    /**
     * Set Activity Extension Concept fields from SPARQL query result
     * 
     * @param id - identifier for the Activity Extension Concept
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ActivityExtensionConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        for(String recType : src.getRecommendedActivityTypes()) {
            addRecommendedActivityType(toURI(recType));
        }
    }
    
    /**
     * Create SPARQL query string for activity extension with id, execute query and parse result
     * 
     * @param id - activity extension concept identifier
     * 
     * @return result of the SPARQL query
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult activityExtensionSparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createActivityExtensionQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute activity extension SPARQL query!", e);
            }
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchConcepts(id);
        } else {
            throw new LmsXapiProfileException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Create SPARQL query string for activity extension with id, execute query and parse result
     * 
     * @param id - activity extension concept identifier
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return result of the SPARQL query
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult activityExtensionSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchConcepts(id);
        } else {
            return activityExtensionSparqlQuery(id);
        }
    }

    /**
     * @return existing recommendedActivityTypes or empty ArrayList<URI>
     */
    private List<URI> existingRecommendedActivityTypes(){
        return recommendedActivityTypes != null ? recommendedActivityTypes : new ArrayList<URI>();
    }

    /**
     * @return recommended Activity Types, can be null
     */
    public List<URI> getRecommendedActivityTypes(){
        return recommendedActivityTypes;
    }

    /**
     * Setter for 'recommendedActivityTypes'
     * 
     * @param recommendedActivityTypes - collection of Activity Type URIs
     */
    private void setRecommendedActivityTypes(List<URI> recommendedActivityTypes) {
        if(recommendedActivityTypes == null) {
            throw new IllegalArgumentException("recommnededActivityTypes can not be null!");
        }
        this.recommendedActivityTypes = recommendedActivityTypes;
    }

    /**
     * Add activityType to recommendedActivityTypes
     * 
     * @param activityType - Activity Type to add
     */
    public void addRecommendedActivityType(URI activityType) {
        if(activityType == null) {
            throw new IllegalArgumentException("activityType can not be null!");
        }
        List<URI> coll = existingRecommendedActivityTypes();
        coll.add(activityType);
        setRecommendedActivityTypes(coll);
    }

    /**
     * Parses ActivityType out of activityDefinition and search for within recommendedActivityTypes.
     * 
     * @param activityDefinition - ActivityDefinition that needs to have its type checked
     * 
     * @return activityDefinition has recommendedActivityType?
     */
    public boolean isRecommendedActivityType(ActivityDefinition activityDefinition) {
        if(activityDefinition == null) {
            return false;
        }
        URI activityType = activityDefinition.getType();
        if(activityType == null) {
            return false;
        }
        return isRecommended(activityType, getRecommendedActivityTypes());
    }

    /**
     * Parses ActivityType out of activity and search for within recommendedActivityTypes.
     * 
     * @param activity - Activity that contains activity type
     * 
     * @return true if activity contain a recommended activity type, false otherwise
     */
    public boolean isRecommendedActivityType(Activity activity) {
        if(activity == null) {
            return false;
        }
        ActivityDefinition aDef = activity.getDefinition();
        if(aDef == null) {
            return false;
        }
        return isRecommendedActivityType(aDef);
    }

    /**
     * Adds activityExtensions to activityDefinition when the activityDefinitions type is 
     * contained within this ActivityExtensionConcept's recommendedActivityTypes
     * 
     * @param activityDefinition - ActivityDefinition to update
     * @param activityExtensions - Extensions to possibly add to activityDefinition 
     */
    private void addToActivityDefinition(ActivityDefinition activityDefinition, Extensions activityExtensions) {
        if(activityExtensions != null) {
            // no-op otherwise
            if(isRecommendedActivityType(activityDefinition)) {
                // no-op otherwise
                activityDefinition.setExtensions(activityExtensions);
            }   
        }
    }

    /**
     * Adds Extension to activityDefinition when the activityDefinitions type is 
     * contained within this ActivityExtensionConcept's recommendedActivityTypes
     * 
     * If added, the extension key is the id of this ActivityExtensionConcept
     * and the value is the passed in 'value'. attempts to update instead of overwrite on conflict
     * 
     * @param activityDefinition - ActivityDefinition to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ActivityExtensionConcept
     */
    public void addToActivityDefinition(ActivityDefinition activityDefinition, Object value) {
        addToActivityDefinition(activityDefinition, value, false);
    }

    /**
     * Adds Extension to activityDefinition when the activityDefinition's type is
     * contained within this ActivityExtensionConcept's recommendedActivityTypes
     * 
     * If added, the extension key is the id of this ActivityExtensionConcept
     * and the value is the passed in 'value'. on conflict behavior mediated by 'forceOverwrite'
     * 
     * @param activityDefinition - ActivityDefinition to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ActivityExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ActivityExtensionConcept with 'value'
     */
    public void addToActivityDefinition(ActivityDefinition activityDefinition, Object value, Boolean forceOverwrite) {
        if(activityDefinition == null) {
            throw new IllegalArgumentException("activityDefinition can not be null!");
        }
        Extensions existingExt = activityDefinition.getExtensions();
        Extensions updatedExt;
        if(existingExt != null) {
            updatedExt = asExtension(value, existingExt, forceOverwrite);
        } else {
            updatedExt = asExtension(value);
        }
        addToActivityDefinition(activityDefinition, updatedExt);
    }

    /**
     * Adds this ActivityExtensionConcept as extension within activity, value is set to the passed in 'value'
     * 
     * Merge attempted on conflict
     * 
     * @param activity - Activity to update with extension when of a recommended type
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ActivityExtensionConcept
     */
    public void addToActivity(Activity activity, Object value) {
        addToActivity(activity, value, false);
    }

    /**
     * Adds this activityExtensions to the Activity when its of a recommended type
     * 
     * on conflict behavior mediated by 'forceOverwrite'
     * 
     * @param activity - Activity to update with extension when of a recommended type
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ActivityExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ActivityExtensionConcept with 'value'
     */
    public void addToActivity(Activity activity, Object value, Boolean forceOverwrite) {
        if(activity == null) {
            throw new IllegalArgumentException("activity can not be null!");
        }
        ActivityDefinition activityDefinition = activity.getDefinition();
        addToActivityDefinition(activityDefinition, value, forceOverwrite);
        activity.setDefinition(activityDefinition);
    }
}
