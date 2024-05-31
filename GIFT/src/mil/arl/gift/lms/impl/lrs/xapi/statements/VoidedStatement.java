package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.UUID;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.StatementRef;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.adl.AdlVerbConcepts;

/**
 * xAPI Statement corresponding to invalidation of existing xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class VoidedStatement extends AbstractGiftStatement {

    protected VoidedStatement(Agent actor, Verb verb, StatementTarget object, DateTime timestamp) {
        super(actor, verb, object, timestamp);
    }
    
    public VoidedStatement(Agent actor, UUID targetStatementId, DateTime timestamp) throws LmsXapiProfileException {
        this(actor, AdlVerbConcepts.Voided.getInstance().asVerb(), new StatementRef(targetStatementId), timestamp);
    }
    
    public VoidedStatement(Agent actor, UUID targetStatementId) throws LmsXapiProfileException {
        this(actor, targetStatementId, DateTime.now());
    }
    
    public VoidedStatement(UUID targetStatementId, DateTime timestamp) throws LmsXapiProfileException, LmsXapiAgentException {
        this(PersonaHelper.createGiftAgent(), targetStatementId, timestamp);
    }
    
    public VoidedStatement(UUID targetStatementId) throws LmsXapiProfileException, LmsXapiAgentException {
        this(targetStatementId, DateTime.now());
    }

    @Override
    UUID deriveStatementId() throws LmsException {
        return UUID.randomUUID();
    }
}
