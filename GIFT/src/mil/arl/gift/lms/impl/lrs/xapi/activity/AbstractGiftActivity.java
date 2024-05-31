package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import java.util.List;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.LanguageMap;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.SubStatement;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.LanguageTagEnum;
import mil.arl.gift.lms.impl.lrs.xapi.IdHelper;

/**
 * Abstract class that wraps the tincan Activity class and provides additional utilities. 
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractGiftActivity extends Activity implements XapiActivity {
    
    /**
     * Creates a basic xAPI <code>Activity</code> with the given ID and populates the
     * <code>ActivityDefinition</code> with the given name and description
     * 
     * @param id - The URI of the activity
     * @param name - The name of the activity
     * @param description - A description of the activity
     * 
     * @return Activity with Id, Name and Description
     * 
     * @throws LmsXapiActivityException when unable to create xAPI Activity
     */
    public AbstractGiftActivity(String id, String name, String description) throws LmsXapiActivityException {
        this(id, name);
        if(description == null || StringUtils.isBlank(description)) {
            throw new LmsXapiActivityException("The Activity Description can not be null or empty when creating an xAPI Activity.");
        }
        ActivityDefinition d = getDefinition();
        LanguageMap lmap = new LanguageMap();
        lmap.put(LanguageTagEnum.EN_US.getValue(), description);
        d.setDescription(lmap);
        setDefinition(d);
    }

    /**
     * Creates a basic xAPI <code>Activity</code> with the given ID and populates the
     * <code>ActivityDefinition</code> with the given name
     * 
     * @param id - The URI of the activity
     * @param name - The name of the activity
     * 
     * @return Activity with Id and Name
     * 
     * @throws LmsXapiActivityException when unable to create xAPI Activity
     */
    public AbstractGiftActivity(String id, String name) throws LmsXapiActivityException {
        this(id);
        if(name == null || StringUtils.isBlank(name)) {
            throw new LmsXapiActivityException("The Activity Name can not be null or empty when creating an xAPI Activity.");
        }
        ActivityDefinition d = new ActivityDefinition();
        LanguageMap lmap = new LanguageMap();
        lmap.put(LanguageTagEnum.EN_US.getValue(), name);
        d.setName(lmap);
        setDefinition(d);
    }

    /**
     * Creates a basic xAPI <code>Activity</code> with the given ID
     * 
     * @param id - The URI of the activity
     * 
     * @return Activity with Id
     * 
     * @throws LmsXapiActivityException when unable to set the xAPI Activity Id
     */
    public AbstractGiftActivity(String id) throws LmsXapiActivityException {
        super();
        if(id == null || StringUtils.isBlank(id)) {
            throw new LmsXapiActivityException("The Activity Id can not be null or empty when creating an xAPI Activity.");
        }
        try {
            setId(id);
        } catch (URISyntaxException e) {
            throw new LmsXapiActivityException("The provided id could not be set as Acitivyt Id!", e);
        }
    }
    
    /**
     * Adds utility methods defined in this class to existing Activity
     * 
     * @param a - Activity to wrap
     * 
     * @throws URISyntaxException when activity id is not a valid URI
     */
    public AbstractGiftActivity(Activity a) throws URISyntaxException {
        super(a.toJSONNode());
    }

    /**
     * Creates the activity id string from name
     * 
     * @param name - string to encode and set as distinct part of activity id
     * 
     * @return the activity id string
     * 
     * @throws LmsXapiActivityException when unable to create the activity id
     */
    protected static String createId(String name) throws LmsXapiActivityException {
        return IdHelper.createActivityId(name);
    }

    /**
     * Creates the activity id from encoding all paths and joining with separator '/'
     * 
     * @param paths - n ordered strings to encode
     * 
     * @return - the activity id string
     * 
     * @throws LmsXapiActivityException when unable to encode one of the paths
     */
    protected static String createId(String... paths) throws LmsXapiActivityException {
        return IdHelper.createActivityId(paths);
    }

    /**
     * Parse name from activity
     * 
     * @param a - Activity to parse
     * @param tag - Language tag of name
     * 
     * @return - name at language tag within activity or null if not found
     */
    public static String parseActivityName(Activity a, LanguageTagEnum tag) {
        if(a.getDefinition() == null) {
            return null;
        }
        if(a.getDefinition().getName() == null) {
            return null;
        }
        return a.getDefinition().getName().get(tag.getValue());
    }
    
    /**
     * Parses value mapped to tag within Activity name
     * 
     * @param tag - Language Tag to return value for
     * 
     * @return value at tag or null
     */
    public String parseActivityName(LanguageTagEnum tag) {
        return parseActivityName(this, tag);
    }
    
    /**
     * Parses value mapped to 'en-US' within Activity name
     * 
     * @param a - Activity to parse name from
     * 
     * @return name if found at 'en-US', null otherwise
     */
    public static String parseActivityName(Activity a) {
        return parseActivityName(a, LanguageTagEnum.EN_US);
    }

    /**
     * Parses United States English Activity name value
     * 
     * @return string at 'en-US' or null
     */
    public String parseActivityName() {
        return parseActivityName(LanguageTagEnum.EN_US);
    }

    /**
     * Parses value mapped to tag within Activity description
     * 
     * @param a - Activity to parse
     * @param tag - Language Tag to return value for
     * 
     * @return value at tag or null
     */
    public static String parseActivityDescription(Activity a, LanguageTagEnum tag) {
        if(a.getDefinition() == null) {
            return null;
        }
        if(a.getDefinition().getDescription() == null) {
            return null;
        }
        return a.getDefinition().getDescription().get(tag.getValue());
    }
    
    /**
     * Parses value mapped to tag within Activity description
     * 
     * @param tag - Language Tag to return value for
     * 
     * @return value at tag or null
     */
    public String parseActivityDescription(LanguageTagEnum tag) {
        return parseActivityDescription(this, tag);
    }
    
    /**
     * Parses United States English Activity description value
     * 
     * @param a - Activity to parse
     * 
     * @return - description or null
     */
    public static String parseActivityDescription(Activity a) {
        return parseActivityDescription(a, LanguageTagEnum.EN_US);
    }

    /**
     * Parses United States English Activity description value
     * 
     * @return description or null
     */
    public String parseActivityDescription() {
        return parseActivityDescription(LanguageTagEnum.EN_US);
    }
    
    /**
     * Compares two Activity identifiers. The default comparison between activities can return false negatives 
     * based on activity definition language maps.
     * 
     * @param a - Activity to compare
     * @param b - Activity to compare
     * 
     * @return do the activities have the same identifier
     */
    public static boolean isSameActivityId(Activity a, Activity b) {
        if(a == null || b == null) {
            return false;
        }
        if(a.getId() == null) {
            return false;
        }
        if(b.getId() == null) {
            return false;
        }
        return a.getId().equals(b.getId());
    }
    
    /**
     * Compares an Activity to this. The default comparison between activities can return false negatives
     * based on activity definition language maps.
     * 
     * @param a - Activity to compare to this
     * 
     * @return does the activity have the same identifier as this
     */
    public boolean isSameActivityId(Activity a) {
        return isSameActivityId(this, a);
    }
    
    /**
     * Determines if a collection of Activities contains target Activity based on identifier. The collection of Activities is not altered.
     * 
     * @param coll - collection of activities to search
     * @param target - activity to search for
     * 
     * @return true if target activity found within collection, false otherwise
     */
    public static boolean containsActivity(List<Activity> coll, Activity target) {
        for(Activity a : coll) {
            if(isSameActivityId(a, target)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if this is found within a collection of Activities; the collection is not altered.
     * 
     * @param coll - collection of activities to search
     * 
     * @return true if this is found within the collection, false otherwise
     */
    public boolean containsActivity(List<Activity> coll) {
        return containsActivity(coll, this);
    }
    
    /**
     * Look for Activity with same id
     * 
     * @param coll - collection of Activity to search
     * 
     * @return Activity with matching id, null otherwise.
     */
    protected Activity parseFromCollection(List<Activity> coll) {
        for(Activity a : coll) {
            if(isSameActivityId(a)) {
                return a;
            }
        }
        return null;
    }
    
    /**
     * Compare target to Activity by id.
     * 
     * @param target - Statement object
     *  
     * @return Activity if found to be (Sub)Statement object, null otherwise.
     */
    protected Activity parseFromStatementTarget(StatementTarget target) {
        if(target == null) {
            return null;
        }
        if(target instanceof Activity && isSameActivityId((Activity) target)) {
            return (Activity) target;
        }
        if(target instanceof SubStatement) {
            return parseFromStatementTarget(((SubStatement) target).getObject());
        }
        return null;
    }
}
