package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;

/**
 * Generator for voiding xAPI Statements that target Predicted Affective Learner State Attribute xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class VoidPredictedAffectiveLsaGenerator extends VoidPredictedLearnerStateAttributeGenerator {
    /**
     * Configure Generator for to be voided xAPI Statement
     * 
     * @param knowledgeSession - Knowledge Session associated with to be voided xAPI Statement
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * @param actorSlug - Name for the user associated with to be voided xAPI Statement
     * @param attribute - Learner State Attribute used within to be voided xAPI Statement generator
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param domainSessionId - Identifier for Domain Session associated with to be voided xAPI Statement
     * @param domainId - Identifier for Domain associated with to be voided xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public VoidPredictedAffectiveLsaGenerator(AbstractKnowledgeSession knowledgeSession, String voiderSlug, String actorSlug, LearnerStateAttribute attribute,
            List<AbstractPerformanceState> history, Integer domainSessionId, String domainId)
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(new PredictedAffectiveLsaGenerator(knowledgeSession, actorSlug, attribute, history, domainSessionId, domainId), domainSessionId, voiderSlug);
    }
}
