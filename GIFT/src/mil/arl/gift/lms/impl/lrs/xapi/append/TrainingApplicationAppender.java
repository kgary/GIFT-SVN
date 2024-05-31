package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Training Application Activity from Training Application Enum
 * and adds the Activity to xAPI Statement as Category Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class TrainingApplicationAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Weather Environment Adaptation Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Extension JSON from Environment Adaptation and adds to xAPI Statement as Context Extension";
    /** Training Application Enumeration used to create xAPI Activity */
    private TrainingApplicationEnum trainingApp;
    /** Training Application Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.TrainingApplication activityType;
    /**
     * Parses Activity Type from xAPI Profile and sets Training Application
     * 
     * @param trainingApplication - Training Application Enumeration to create xAPI Activity from
     * 
     * @throws LmsXapiProfileException when unable to parse Activity Type from xAPI Profile
     */
    public TrainingApplicationAppender(TrainingApplicationEnum trainingApplication) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.activityType = ItsActivityTypeConcepts.TrainingApplication.getInstance();
        if(trainingApplication == null) {
            throw new IllegalArgumentException("trainingApplication can not be null!");
        }
        this.trainingApp = trainingApplication;
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addCategoryActivity(activityType.asActivity(trainingApp));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append Training Application Activity!", e);
        }
        return statement;
    }
}
