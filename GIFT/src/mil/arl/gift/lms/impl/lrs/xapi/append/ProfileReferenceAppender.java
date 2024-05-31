package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.lrs.xapi.activity.ProfileReferenceActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates xAPI Profile reference Activity from xAPI Profile associated with the
 * xAPI Statement Template and adds the Activity to xAPI Statement as Category Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class ProfileReferenceAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Profile Reference Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches xAPI Profile reference activity to xAPI Statement";
    /** xAPI Activity created from xAPI Statement Templates' inScheme */
    private ProfileReferenceActivity activity;
    /**
     * Creates xAPI Activity from Statement Template inScheme.
     * 
     * @param template - xAPI Statement Template for the to be mutated xAPI Statement
     * 
     * @throws LmsXapiActivityException when unable to create xAPI Profile reference activity
     */
    public ProfileReferenceAppender(StatementTemplate template) throws LmsXapiActivityException {
        super(appenderName, appenderInfo);
        if(template == null) {
            throw new IllegalArgumentException("xAPI Statement Template can not be null!");
        }
        this.activity = new ProfileReferenceActivity(template.getInScheme());
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        statement.addCategoryActivity(activity);
        return statement;
    }    
}
