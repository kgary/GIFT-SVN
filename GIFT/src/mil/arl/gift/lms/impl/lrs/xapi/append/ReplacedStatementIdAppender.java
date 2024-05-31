package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.util.UUID;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Takes a xAPI Statement id, converts to string and adds to xAPI Statement as revision.
 * 
 * @author Yet Analytics
 *
 */
public class ReplacedStatementIdAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Replacement Statement Id Appender";
    /** appender description */
    private static final String appenderInfo = "Uses passed in UUID to set Revision within replacement xAPI Statement";
    /** replacement xAPI Statement identifier */
    protected UUID replacedStatementId;
    /**
     * Sets xAPI Statement identifier
     * 
     * @param replacedStatementId - xAPI Statement identifier of an xAPI Statement being voided and replaced
     */
    public ReplacedStatementIdAppender(UUID replacedStatementId) {
        super(appenderName, appenderInfo);
        if(replacedStatementId == null) {
            throw new IllegalArgumentException("replacedStatementId can not be null!");
        }
        this.replacedStatementId = replacedStatementId;
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context ctx = statement.getContext();
        ctx.setRevision(replacedStatementId.toString());
        statement.setContext(ctx);
        return statement;
    }
}
