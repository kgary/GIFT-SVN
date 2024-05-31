package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;

/**
 * xAPI Statement Helper methods for xAPI Statement Templates.
 * 
 * @author Yet Analytics
 *
 */
public interface StatementTemplateXapi {

    public Boolean statementMatchDeterminingProperties(Statement statement) throws LmsInvalidStatementException;
    public void matchingStatement(Statement statement) throws LmsInvalidStatementException;
}
