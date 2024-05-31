package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Domain Activity from domain id and adds to xAPI Statement
 * as Parent Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class DomainAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Domain Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Domain xAPI Activity to xAPI Statement as Parent Context Activity";
    /** Domain identifier used to create Domain xAPI Activity */
    private String domainId;
    /** Domain Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.Domain domainATC;
    /**
     * Parses Domain Activity Type from xAPI Profile and sets Domain identifier
     * 
     * @param domainId - Domain identifier used to create Domain xAPI Activity
     * 
     * @throws LmsXapiProfileException when unable to parse Domain Activity Type from xAPI Profile
     */
    public DomainAppender(String domainId) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(domainId == null) {
            throw new IllegalArgumentException("Domain identifier can not be null!");
        }
        this.domainId = domainId;
        this.domainATC = ItsActivityTypeConcepts.Domain.getInstance();
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addParentActivity(domainATC.asActivity(domainId));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append domain activity!", e);
        }
        return statement;
    }
}
