package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Course Record Activity from Course Record Reference and adds the
 * Activity to xAPI Statement as Grouping Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class CourseRecordAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Course Record Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Course Record Ref xAPI Activity to xAPI Statement as Grouping Context Activity";
    /** xAPI Activity Type for Course Record from xAPI Profile */
    protected ItsActivityTypeConcepts.CourseRecord courseRecordATC;
    /** Course Record Ref to convert to xAPI Activity */
    protected CourseRecordRef courseRecordRef;
    /**
     * Parses Course Record Activity Type from xAPI Profile
     * 
     * @param courseRecordRef - Course Record Ref to convert to xAPI Activity
     * 
     * @throws LmsXapiProfileException when unable to parse Activity Type from xAPI Profile
     */
    public CourseRecordAppender(CourseRecordRef courseRecordRef) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(courseRecordRef == null) {
            throw new IllegalArgumentException("Course Record Ref can not be null!");
        }
        this.courseRecordRef = courseRecordRef;
        this.courseRecordATC = ItsActivityTypeConcepts.CourseRecord.getInstance();
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        if(courseRecordRef.getRef() == null || courseRecordRef.getRef() instanceof UUIDCourseRecordRefIds){
            return statement;
        }
        // Support for legacy courseRecordRef
        try {
            statement.addGroupingActivity(courseRecordATC.asActivity(courseRecordRef));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append course record activity!", e);
        }
        return statement;
    }
}
