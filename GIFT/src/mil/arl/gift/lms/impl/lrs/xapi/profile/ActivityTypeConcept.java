package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.SubStatement;
import com.rusticisoftware.tincan.internal.StatementBase;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of an Activity Type Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ActivityTypeConcept extends AbstractConceptRelation {
    
    /**
     * Set Activity Type Concept fields from SPARQL query result
     * 
     * @param id - identifier for the Activity Type Concept
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ActivityTypeConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }

    /**
     * Convert from ActivityType Concept to Activity Type
     * 
     * @return the URI for the Activity Type
     */
    public URI asActivityType() {
        return getId();
    }

    /**
     * Sets type within activityDefinition to be the id of this Activity Type Concept
     * 
     * @param activityDefinition - the activityDefinition to update
     */
    public void addToActivityDefinition(ActivityDefinition activityDefinition) {
        if(activityDefinition == null) {
            throw new IllegalArgumentException("activityDefinition can not be null!");
        }
        activityDefinition.setType(asActivityType());
    }

    /**
     * Sets type within activityDefinition to be the id of this Activity Type Concept
     * 
     * @param activityDefinition - the activityDefinition to update
     * 
     * @return - updated ActivityDefinition
     */
    public ActivityDefinition addToActivityDefinitionAndReturn(ActivityDefinition activityDefinition) {
        addToActivityDefinition(activityDefinition);
        return activityDefinition;
    }

    /**
     * Sets type within activity's ActivityDefinition to be the id of this Activity Type Concept
     * 
     * @param activity - the activity to update
     */
    public void addToActivity(Activity activity) {
        if(activity == null) {
            throw new IllegalArgumentException("activity can not be null!");
        }
        ActivityDefinition activityDefinition = activity.getDefinition() != null ? activity.getDefinition() : new ActivityDefinition();
        addToActivityDefinition(activityDefinition);
        activity.setDefinition(activityDefinition);
    }

    /**
     * Sets type within activity's ActivityDefinition to be the id of this Activity Type Concept
     * 
     * @param activity - the activity to update
     * 
     * @return - updated Activity
     */
    public AbstractGiftActivity addToActivityAndReturn(Activity activity) {
        addToActivity(activity);
        return (AbstractGiftActivity) activity;
    }

    /**
     * Does the Activity contain this Activity Type Concept
     * 
     * @param activity - Activity to check
     * 
     * @return true if found, false otherwise
     */
    public boolean withinActivity(Activity activity) {
        if(activity == null || activity.getDefinition() == null || activity.getDefinition().getType() == null) {
            return false;
        }
        return activity.getDefinition().getType().toString().equals(asActivityType().toString());
    }
    
    /**
     * Search through collection for Activities with type equal to this activity type concept.
     * 
     * matches are added to accumulator
     * 
     * @param collection - Collection of Activity to search through
     * @param accum - Collection of Activity to add matches to
     */
    public void findInstancesInCollection(List<Activity> collection, List<Activity> accum) {
        if(collection == null || accum == null) {
            throw new IllegalArgumentException("Collection and accum can not be null!");
        }
        for(Activity a : collection) {
            if(withinActivity(a)) {
                accum.add(a);
            }
        }
    }
    
    /**
     * Search through collection of for Activities with type equal to this activity type concept.
     * 
     * matches are added to the returned collection of Activities.
     * 
     * @param collection - Collection of Activity to search through
     * 
     * @return collection of matches
     */
    public List<Activity> findInstancesInCollection(List<Activity> collection) {
        List<Activity> matches = new ArrayList<Activity>();
        findInstancesInCollection(collection, matches);
        return matches;
    }
    
    /**
     * Search through statement and return all Activities with type equal to this activity type concept.
     * 
     * @param statement - Statement to search through
     * 
     * @return Collection of matches
     * 
     * @throws LmsInvalidStatementException when statement object is not set
     */
    public List<Activity> findInstancesInStatement(StatementBase statement) throws LmsInvalidStatementException {
        if(statement == null) {
            throw new IllegalArgumentException("Statement can not be null!");
        }
        List<Activity> coll = new ArrayList<Activity>();
        if(statement.getObject() == null) {
            throw new LmsInvalidStatementException("statement didn't have an object!");
        }
        // statement.object
        StatementTarget target = statement.getObject();
        if(target instanceof Activity) {
            Activity stmtObj = (Activity) target;
            if(withinActivity(stmtObj)) {
                coll.add(stmtObj);
            }
        } else if(target instanceof SubStatement) {
            coll.addAll(findInstancesInStatement((SubStatement) target));
        }
        if(statement.getContext() != null) {
            Context stmtContext = statement.getContext();
            // statement.context.contextActivities.parent
            findInstancesInCollection(ContextActivitiesHelper.getParentActivities(stmtContext), coll);
            // statement.context.contextActivities.grouping
            findInstancesInCollection(ContextActivitiesHelper.getGroupingActivities(stmtContext), coll);
            // statement.context.contextActivities.category
            findInstancesInCollection(ContextActivitiesHelper.getCategoryActivities(stmtContext), coll);
            // statement.context.contextActivities.other
            findInstancesInCollection(ContextActivitiesHelper.getOtherActivities(stmtContext), coll);
        }
        return coll;
    }
}
