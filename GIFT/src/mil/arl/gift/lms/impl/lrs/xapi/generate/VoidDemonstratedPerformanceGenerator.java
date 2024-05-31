package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.DemonstratedPerformanceTemplate;

/**
 * Generator for voiding xAPI Statements that target Demonstrated Performance State Attribute xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class VoidDemonstratedPerformanceGenerator extends AbstractVoidStatementGenerator {
    /**
     * Sets xAPI Statement template for voiding statement
     * 
     * @param targetGen - Generator for to be voided xAPI Statement
     * @param domainSessionId - Identifier for Domain Session associated with to be voided xAPI Statement
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected VoidDemonstratedPerformanceGenerator(AbstractStatementGenerator targetGen, Integer domainSessionId, String voiderSlug) throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(DemonstratedPerformanceTemplate.VoidingTemplate.getInstance(), domainSessionId, targetGen, voiderSlug);
    }
    /**
     * Configures Demonstrated Performance Generator for the to be voided xAPI Statement
     * 
     * @param history - Ordered collection of performance states
     * @param knowledgeSession - Knowledge Session associated with to be voided xAPI Statement
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * @param domainSessionId - Identifier for Domain Session associated with to be voided xAPI Statement
     * @param domainId - Identifier for Domain associated with to be voided xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public VoidDemonstratedPerformanceGenerator(List<AbstractPerformanceState> history, AbstractKnowledgeSession knowledgeSession,
            String voiderSlug, Integer domainSessionId, String domainId, String actorSlug) throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(new DemonstratedPerformanceGenerator(history, knowledgeSession, domainSessionId, domainId, actorSlug), domainSessionId, voiderSlug);
    }
}
