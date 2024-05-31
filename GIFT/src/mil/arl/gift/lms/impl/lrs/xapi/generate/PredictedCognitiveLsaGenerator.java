package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;
import java.util.UUID;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.append.CognitiveAppender;

/**
 * Generator for Predicted Cognitive Learner State Attribute xAPI Statements. 
 * 
 * @author Yet Analytics
 *
 */
public class PredictedCognitiveLsaGenerator extends PredictedLearnerStateAttributeGenerator {
    /**
     * Generator for Cognitive Learner State Attribute xAPI Statement
     * 
     * @param session - Knowledge Session associated with the learner state
     * @param actorSlug - User name associated with the learner state
     * @param attribute - Learner State Attribute from Cognitive State to generate statement about
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for the Domain associated with the learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public PredictedCognitiveLsaGenerator(AbstractKnowledgeSession session, String actorSlug, LearnerStateAttribute attribute,
            List<AbstractPerformanceState> history, Integer domainSessionId, String domainId) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(session, history, actorSlug, attribute, domainSessionId, domainId);
        addStatementAppender(new CognitiveAppender());
    }
    /**
     * Generator for replacement Cognitive Learner State Attribute xAPI Statement
     * 
     * @param session - Knowledge Session associated with the learner state
     * @param actorSlug - User name associated with the learner state
     * @param attribute - Learner State Attribute from Cognitive State to generate statement about
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param invalidStmtId - Identifier of out-dated xAPI Statement being replaced
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for the Domain associated with the learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public PredictedCognitiveLsaGenerator(AbstractKnowledgeSession session, String actorSlug, LearnerStateAttribute attribute, 
            List<AbstractPerformanceState> history, UUID invalidStmtId, Integer domainSessionId, String domainId) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(session, history, actorSlug, attribute, invalidStmtId, domainSessionId, domainId);
        addStatementAppender(new CognitiveAppender());
    }
}
