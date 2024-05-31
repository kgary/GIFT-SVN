package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;

import mil.arl.gift.common.course.CourseRecordRef.IntCourseRecordRefId;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of a Course Record
 * 
 * @author Yet Analytics
 *
 */
public class CourseRecordActivity extends AbstractGiftActivity {
    
    private static final String slug = "course.record";
    
    /**
     * Creation of Activity Identifier
     * 
     * @param recordId - string which makes up the trailing part of identifier
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier
     */
    protected CourseRecordActivity(String recordId) throws LmsXapiActivityException {
        super(createId(slug, recordId), recordId);
    }
    
    /**
     * Creation of Course Record Activity
     * 
     * @param recordId - used to create identifier and display name
     * @param recordUUIDs - comma separated string of relevant course record UUIDs
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier
     */
    public CourseRecordActivity(String recordId, String recordUUIDs) throws LmsXapiActivityException {
        super(createId(slug, recordId), recordId, recordUUIDs);
    }
    
    /**
     * Creation of Course Record Activity
     * 
     * @param recordRef - Course Record with integer identifier
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier
     */
    public CourseRecordActivity(IntCourseRecordRefId recordRef) throws LmsXapiActivityException {
        this(Integer.valueOf(recordRef.getRecordId()).toString());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Course Record Activity expected to be found within Grouping Context Activities
        return parseFromCollection(ContextActivitiesHelper.getGroupingActivities(statement.getContext()));
    }
}
