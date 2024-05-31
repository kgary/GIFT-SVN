package mil.arl.gift.lms.impl.lrs.xapi.append;

import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates xAPI Agent from the evaluators user name and adds the Agent to xAPI Statement
 * as Instructor.
 * 
 * @author Yet Analytics
 *
 */
public class EvaluatorAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Evaluator Appender";
    /** appender description */
    private static final String appenderInfo = "Creates xAPI Agent from evaluator username and sets as Instructor";
    /** xAPI Agent created from evaluator name */
    private Agent evaluator;
    /**
     * Creates xAPI Agent from evaluator name
     * 
     * @param evaluatorName - name of the evaluator
     * 
     * @throws LmsXapiAgentException when unable to create xAPI Agent from name
     */
    public EvaluatorAppender(String evaluatorName) throws LmsXapiAgentException {
        super(appenderName, appenderInfo);
        if(StringUtils.isBlank(evaluatorName)) {
            throw new IllegalArgumentException("Evaluator username can not be null or blank!");
        }
        this.evaluator = PersonaHelper.createMboxAgent(evaluatorName);
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context ctx = statement.getContext();
        ctx.setInstructor(evaluator);
        statement.setContext(ctx);
        return statement;
    }
}
