package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Assessment Activity from Task Performance State and adds the Activity
 * to xAPI Statement as Parent Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class ParentTaskAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Task Parent Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Assessment xAPI Activity from Task Performance State and attaches to xAPI Statement as Parent Context Activity";
    /** Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.AssessmentNode.Task taskATC;
    /** Task Performance State to convert */
    private TaskPerformanceState task;
    /**
     * Parses Activity Type from xAPI Profile
     * 
     * @param state - Task Performance State to convert
     * 
     * @throws LmsXapiProfileException when unable to parse Activity Type from xAPI Profile
     */
    public ParentTaskAppender(TaskPerformanceState state) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(state == null) {
            throw new IllegalArgumentException("Task Performance State can not be null!");
        }
        this.task = state;
        this.taskATC = ItsActivityTypeConcepts.AssessmentNode.Task.getInstance();
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addParentActivity(taskATC.asActivity(task));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append task parent activity!", e);
        }
        return statement;
    }
}
