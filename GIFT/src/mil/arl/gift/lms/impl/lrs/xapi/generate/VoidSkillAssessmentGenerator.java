package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;
import org.joda.time.DateTime;
import generated.course.ConceptNode;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.SkillAssessmentTemplate;

/**
 * Generator for voiding xAPI Statements that target (Team | Individual) Experienced Assessment xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */

public class VoidSkillAssessmentGenerator extends AbstractVoidStatementGenerator {
    /**
     * Sets xAPI Statement template for voiding statement
     * 
     * @param targetGen - Generator for to be voided xAPI Statement
     * @param chain - Chain of custody info
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    private VoidSkillAssessmentGenerator(AbstractStatementGenerator targetGen, AssessmentChainOfCustody chain, String voiderSlug) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(SkillAssessmentTemplate.VoidingTemplate.getInstance(), chain, targetGen, voiderSlug);
    }
    /**
     * Configures Experienced Assessment Generator for the to be voided xAPI Statement
     * 
     * @param voiderSlug - Name for user responsible for the creation of the void xAPI Statement
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param chain - Chain of custody info
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public VoidSkillAssessmentGenerator(String voiderSlug, AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, 
            ConceptNode conceptDkf, String domainId, AssessmentChainOfCustody chain, CourseRecordRef courseRecordRef) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(new SkillAssessmentGenerator(knowledgeSession, timestamp, conceptGsn, conceptRsn, history, conceptDkf, domainId, chain, courseRecordRef), 
                chain, voiderSlug);
    }
}