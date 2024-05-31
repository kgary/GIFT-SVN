package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementRef;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.VoidedStatement;

/**
 * Generator for voiding xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractVoidStatementGenerator extends AbstractStatementGenerator {
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractVoidStatementGenerator.class);
    /** name of the user who invalidated a previously generated xAPI Statement */
    protected String voiderSlug;
    /** Generator that creates target xAPI Statement */
    protected AbstractStatementGenerator targetGen;
    /**
     * Sets common xAPI Statement Generator properties and sets xAPI Statement Generator for the target xAPI Statement
     * 
     * @param template - xAPI Statement Template describing the voiding xAPI Statement
     * @param dsId - Domain Session identifier associated with the target xAPI Statement
     * @param targetGen - Generator for the target xAPI Statement
     * @param voiderSlug - name of the individual responsible for the invalidation when known.
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    protected AbstractVoidStatementGenerator(StatementTemplate template, Integer dsId, AbstractStatementGenerator targetGen, String voiderSlug) 
            throws LmsXapiGeneratorException {
        super(template, dsId);
        this.voiderSlug = voiderSlug;
        if(targetGen == null) {
            throw new IllegalArgumentException("Generator for target xAPI Statement can not be null!");
        }
        this.targetGen = targetGen;
    }
    /**
     * Sets common xAPI Statement Generator properties and sets xAPI Statement Generator for the target xAPI Statement
     * 
     * @param template - xAPI Statement Template describing the voiding xAPI Statement
     * @param chain - Chain of custody info
     * @param targetGen - Generator for the target xAPI Statement
     * @param voiderSlug - name of the individual responsible for the invalidation when known.
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    protected AbstractVoidStatementGenerator(StatementTemplate template, AssessmentChainOfCustody chain, AbstractStatementGenerator targetGen, String voiderSlug)
            throws LmsXapiGeneratorException {
        super(template, chain);
        this.voiderSlug = voiderSlug;
        if(targetGen == null) {
            throw new IllegalArgumentException("Generator for target xAPI Statement can not be null!");
        }
        this.targetGen = targetGen;
    }
    
    @Override
    AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        if(StringUtils.isBlank(voiderSlug)) {
            try {
                actor = PersonaHelper.createGiftAgent();
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiGeneratorException("Unable to set GIFT agent as voiding statement actor!", e);
            }
        } else {
            try {
                actor = PersonaHelper.createMboxAgent(voiderSlug);
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiGeneratorException("Unable to set voider agent as statement actor!", e);
            }
        }
        // Add logging here
        Statement ogstmt = targetGen.generateStatement();
        if(logger.isDebugEnabled()) {
            logger.debug("Statement being voided: "+ogstmt.toJSON(true));
        }
        UUID targetId = ogstmt.getId();
        // Statement
        VoidedStatement stmt;
        try {
            stmt = new VoidedStatement(actor, targetId);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to create voiding statement!", e);
        }
        return generateStatement(stmt, deriveId);
    }
    
    public UUID generateAndReturnTargetId(List<Statement> statements) throws LmsXapiGeneratorException {
        if(statements == null) {
            throw new IllegalArgumentException("statements can not be null!");
        }
        VoidedStatement stmt = (VoidedStatement) generateStatement();
        statements.add(stmt);
        return ((StatementRef) stmt.getObject()).getId();
    }
}