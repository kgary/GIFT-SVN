package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.net.URISyntaxException;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.activity.KnowledgeSessionTypeActivity;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Determines Knowledge Session Type Static / Canonical xAPI Activity from Knowledge Session Type
 * and adds to xAPI Statement as Category Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class KnowledgeSessionTypeAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Knowledge Session Type Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Knowledge Session Type static / canoncial xAPI Activity corresponding to the SessionType as Category Context Activity";
    /** Static / canonical xAPI Activity from xAPI Profile corresponding to SessionType */
    protected KnowledgeSessionTypeActivity activity;
    /**
     * Parses static / canonical xAPI Activity from xAPI Profile based on sessionType
     * 
     * @param sessionType - SessionType enum from Knowledge Session
     * 
     * @throws LmsXapiActivityException when unable to create Activity from Activity Concept parsed from xAPI Profile
     * @throws LmsXapiProfileException when unable to parse Activity Concept from xAPI Profile
     */
    public KnowledgeSessionTypeAppender(SessionType sessionType) throws LmsXapiActivityException, LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(sessionType == null) {
            throw new IllegalArgumentException("sessionType can not be null!");
        }
        switch(sessionType) {
            case ACTIVE: {
                try {
                    activity = KnowledgeSessionTypeActivity.Active.getInstance();
                } catch (URISyntaxException e) {
                    throw new LmsXapiActivityException("Unable to form Active Knowledge Session activity!", e);
                }
                break;
            }
            case ACTIVE_PLAYBACK: {
                try {
                    activity = KnowledgeSessionTypeActivity.ActivePlayback.getInstance();
                } catch (URISyntaxException e) {
                    throw new LmsXapiActivityException("Unable to form Active Playback Knowledge Session activity!", e);
                }
                break;
            }
        }
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        statement.addCategoryActivity(activity);
        return statement;
    }
}
