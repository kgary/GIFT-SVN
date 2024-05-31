package mil.arl.gift.lms.impl.lrs.xapi;

import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.ContextActivities;

/**
 * This class provides utility methods for working with contextActivities
 * 
 * @author Yet Analytics
 */

public class ContextActivitiesHelper {
    
    /**
     * Parse existing ContextActivities or creates new, empty ContextActivities
     * 
     * @param ctx - Context to look in
     * 
     * @return existing or empty but non null ContextActivities
     */
    public static ContextActivities getContextActivitiesColl(Context ctx) {
        if(ctx == null) {
            return new ContextActivities();
        } else {
            return ctx.getContextActivities() != null ? ctx.getContextActivities() : new ContextActivities();
        }   
    }
    
    /**
     * Parse Parent ContextActivities from Context
     *  
     * @param ctx - Context to look in
     * 
     * @return existing Parent ContextActivities or empty ArrayList<Activity>
     */
    public static List<Activity> getParentActivities(Context ctx) {
        return getParentActivities(getContextActivitiesColl(ctx));
    }
    
    /**
     * Parse Parent from ContextActivities or return empty ArrayList<Activity>
     * 
     * @param ctxActivities - ContextActivities to look in
     * 
     * @return possibly empty, non null ArrayList<Activity>
     */
    public static List<Activity> getParentActivities(ContextActivities ctxActivities){
        return ctxActivities.getParent() != null ? ctxActivities.getParent() : new ArrayList<Activity>();
    }
    
    /**
     * Adds activity to Context as a Parent ContextActivity
     * 
     * @param activity - Activity to add as parent
     * @param ctx - Context to update
     */
    public static void addParentActivity(Activity activity, Context ctx) {
        ctx = ctx != null ? ctx : new Context();
        ContextActivities ctxActivities = getContextActivitiesColl(ctx);
        List<Activity> parent = getParentActivities(ctxActivities);
        parent.add(activity);
        ctxActivities.setParent(parent);
        ctx.setContextActivities(ctxActivities);
    }
    
    /**
     * Parse Grouping ContextActivities from Context
     * 
     * @param ctx - Context to look in
     * 
     * @return existing Grouping ContextActivities or empty ArrayList<Activity>
     */
    public static List<Activity> getGroupingActivities(Context ctx) {
        return getGroupingActivities(getContextActivitiesColl(ctx));
    }
    
    /**
     * Parse Grouping from ContextActivities or return empty ArrayList<Activity>
     * 
     * @param ctxActivities - ContextActivities to look in
     * 
     * @return possibly empty, non null ArrayList<Activity>
     */
    public static List<Activity> getGroupingActivities(ContextActivities ctxActivities) {
        return ctxActivities.getGrouping() != null ? ctxActivities.getGrouping() : new ArrayList<Activity>();
    }
    
    /**
     * Adds the activity to Context as a Grouping ContextActivity
     * 
     * @param activity - Activity to add as grouping
     * @param ctx - Context to update
     */
    public static void addGroupingActivity(Activity activity, Context ctx) {
        ctx = ctx != null ? ctx : new Context();
        ContextActivities ctxActivities = getContextActivitiesColl(ctx);
        List<Activity> grouping = getGroupingActivities(ctxActivities);
        grouping.add(activity);
        ctxActivities.setGrouping(grouping);
        ctx.setContextActivities(ctxActivities);
    }
    
    /**
     * Parse Category from ContextActivities within Context
     * 
     * @param ctx - Context to look in
     * 
     * @return existing Category ContextActivities or empty ArrayList<Activity>
     */
    public static List<Activity> getCategoryActivities(Context ctx) {
        return getCategoryActivities(getContextActivitiesColl(ctx));
    }
    
    /**
     * Parse Category from ContextActivities or return empty ArrayList<Activity>
     * 
     * @param ctxActivities - ContextActivities to look in
     * 
     * @return possibly empty, non null ArrayList<Activity>
     */
    public static List<Activity> getCategoryActivities(ContextActivities ctxActivities) {
        return ctxActivities.getCategory() != null ? ctxActivities.getCategory() : new ArrayList<Activity>();
    }
    
    /**
     * Adds the activity to Context as a Category ContextActivity
     * 
     * @param activity - Activity to add as category
     * @param ctx - Context to update
     */
    public static void addCategoryActivity(Activity activity, Context ctx) {
        ctx = ctx != null ? ctx : new Context();
        ContextActivities ctxActivities = getContextActivitiesColl(ctx);
        List<Activity> category = getCategoryActivities(ctxActivities);
        category.add(activity);
        ctxActivities.setCategory(category);
        ctx.setContextActivities(ctxActivities);
    }
    
    /**
     * Parse Other from ContextActivities within Context
     * 
     * @param ctx - Context to look in
     * 
     * @return existing Other ContextActivities or empty ArrayList<Activity>
     */
    public static List<Activity> getOtherActivities(Context ctx) {
        return getOtherActivities(getContextActivitiesColl(ctx));
    }
    
    /**
     * Parse Other from ContextActivities or return empty ArrayList<Activity>
     * 
     * @param ctxActivities - ContextActivities to look in
     * 
     * @return possibly empty, non null ArrayList<Activity>
     */
    public static List<Activity> getOtherActivities(ContextActivities ctxActivities) {
        return ctxActivities.getOther() != null ? ctxActivities.getOther() : new ArrayList<Activity>();
    }
    
    /**
     * Adds the activity to Context as an Other ContextActivity
     * 
     * @param activity - Activity to add
     * @param ctx - Context to update
     */
    public static void addOtherActivity(Activity activity, Context ctx) {
        ctx = ctx != null ? ctx : new Context();
        ContextActivities ctxActivities = getContextActivitiesColl(ctx);
        List<Activity> other = getOtherActivities(ctxActivities);
        other.add(activity);
        ctxActivities.setOther(other);
        ctx.setContextActivities(ctxActivities);
    }

}
