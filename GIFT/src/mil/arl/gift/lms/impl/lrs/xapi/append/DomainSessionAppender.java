package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiUUIDException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Domain Session Activity from domain session id and adds to xAPI Statement
 * as Grouping Context Activity. Also uses domain session id to create UUID and set as Registration.
 * 
 * @author Yet Analytics
 *
 */
public class DomainSessionAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Domain Session Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Domain Session xAPI Activity to xAPI Statement as Grouping Context Activity"
            + " and sets Revision to UUID generated from Domain Session identifier";
    /** Domain Session Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.DomainSession domainSessionATC;
    /** Domain Session identifier */
    private Integer domainSessionId;
    /**
     * Parses Domain Session Activity Type from xAPI Profile and sets Domain Session identifier
     * 
     * @param domainSessionId - Domain Session identifier
     * 
     * @throws LmsXapiProfileException when unable to parse Domain Session Activity Type from xAPI Profile
     */
    public DomainSessionAppender(Integer domainSessionId) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(domainSessionId == null) {
            throw new IllegalArgumentException("Domain Session identifier can not be null!");
        }
        this.domainSessionId = domainSessionId;
        this.domainSessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addGroupingActivity(domainSessionATC.asActivity(domainSessionId));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append domain session activity!", e);
        }
        try {
            statement.addRegistration(domainSessionId);
        } catch (LmsXapiUUIDException e) {
            throw new LmsXapiAppenderException("Unable to append registration!", e);
        }
        return statement;
    }
}
