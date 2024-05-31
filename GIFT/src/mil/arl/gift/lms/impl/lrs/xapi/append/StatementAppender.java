package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Interface implemented by Statement Appenders. The appendToStatement method allows for
 * Appenders to update and return an xAPI Statement via Class specific logic.
 * 
 * @author Yet Analytics
 *
 */
public interface StatementAppender {

    /**
     * Perform mutation of passed in xAPI Statement.
     * 
     * @param statement - xAPI Statement to mutate
     * 
     * @return updated xAPI Statement
     * 
     * @throws LmsXapiAppenderException when error occurs during mutation
     */
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException;
}
