package mil.arl.gift.lms.impl.lrs.xapi.generate;

import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.PredictedLearnerStateAttributeTemplate;

/**
 * Generator for voiding xAPI Statements that target Predicted (Affective | Cognitive) Learner State Attribute xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class VoidPredictedLearnerStateAttributeGenerator extends AbstractVoidStatementGenerator {
    /**
     * Configure Voiding Statement Generator for (Affective | Cognitive) Learner State Attribute xAPI Statements.
     * 
     * @param targetGen - Generator for to be voided xAPI Statement
     * @param domainSessionId - Identifier for Domain Session associated with to be voided xAPI Statement
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected VoidPredictedLearnerStateAttributeGenerator(AbstractStatementGenerator targetGen, Integer domainSessionId, String voiderSlug) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(PredictedLearnerStateAttributeTemplate.VoidingTemplate.getInstance(), domainSessionId, targetGen, voiderSlug);
    }
}
