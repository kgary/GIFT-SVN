package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Adds Cognitive State static / canonical xAPI Activity to xAPI Statement as Category Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class CognitiveAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Cognitive LSA static / canonical xAPI Activity Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Cognitive LSA static / canonical xAPI Activity to xAPI Statement as Category Context Activity";
    /** Cognitive LSA static / canonical xAPI Activity defined in xAPI Profile */
    protected ItsActivityConcepts.CognitiveStateActivity categoryActivity;
    /**
     * Parses Cognitive LSA static / canonical xAPI Activity from xAPI Profile.
     * 
     * @throws LmsXapiProfileException when unable to parse from xAPI Profile
     */
    public CognitiveAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.categoryActivity = ItsActivityConcepts.CognitiveStateActivity.getInstance();
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addCategoryActivity(categoryActivity.asActivity());
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append cognitive activity!", e);
        }
        return statement;
    }
}
