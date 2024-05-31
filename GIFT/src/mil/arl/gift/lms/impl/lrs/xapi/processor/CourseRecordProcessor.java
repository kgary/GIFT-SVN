package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Statement;
import generated.course.ConceptNode;
import generated.course.Concepts;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.ScoreNodeTypeEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.CourseRecordHelper;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.SkillAssessmentGenerator;

/**
 * Processes an LMS Course Record to create Assessed Assessment xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class CourseRecordProcessor extends AbstractProcessor {
    /** course record passed to LRSs' insertCourseRecord method */
    protected LMSCourseRecord courseRecord;
    /** Hierarchy of Concepts from DKF passed to LRSs' insertCourseRecord method */
    protected Concepts.Hierarchy dkfConceptHierarchy;
    /** Order list of Graded Score Node parent(s) populated within processCourseRecord method defined in this class */
    protected List<GradedScoreNode> history;
    /** Knowledge Session corresponding to the LMSCourseRecord */
    protected AbstractKnowledgeSession knowledgeSession;
    /** Information used to create Chain Of Custody extension */
    protected AssessmentChainOfCustody chainOfCustody;
    /**
     * Sets common state used within processing and xAPI statement generation
     * 
     * @param lmsCourseRecord - course record passed to LRSs' insertCourseRecord method
     * @param conceptHierarchy - Hierarchy of Concepts from DKF passed to LRSs' insertCourseRecord method
     * @param domainId - Identifier for the Course corresponding to lmsCourseRecord
     * @param domainSessionId - Domain Session identifier passed to LRSs' insertCourseRecord method
     * @param session - Knowledge Session corresponding to the LMSCourseRecord
     */
    public CourseRecordProcessor(LMSCourseRecord lmsCourseRecord, Concepts.Hierarchy conceptHierarchy,
            String domainId, Integer domainSessionId, AbstractKnowledgeSession session) {
        super(null, domainId, domainSessionId);
        if(lmsCourseRecord == null) {
            throw new IllegalArgumentException("lmsCourseRecord can not be null!");
        }
        this.courseRecord = lmsCourseRecord;
        this.dkfConceptHierarchy = conceptHierarchy;
        this.history = new ArrayList<GradedScoreNode>();
        this.knowledgeSession = session;
    }
    /**
     * Sets common state used within processing and xAPI statement generation
     * 
     * @param lmsCourseRecord - course record passed to LRSs' insertCourseRecord method
     * @param conceptHierarchy - Hierarchy of Concepts from DKF passed to LRSs' insertCourseRecord method
     * @param domainId - Identifier for the Course corresponding to lmsCourseRecord
     * @param chain - Chain of custody information
     * @param session - Knowledge Session corresponding to the LMSCourseRecord
     */
    public CourseRecordProcessor(LMSCourseRecord lmsCourseRecord, Concepts.Hierarchy conceptHierarchy,
            String domainId, AssessmentChainOfCustody chain, AbstractKnowledgeSession session) {
        super(null, domainId, chain.getDomainsessionid());
        if(lmsCourseRecord == null) {
            throw new IllegalArgumentException("lmsCourseRecord can not be null!");
        }
        this.courseRecord = lmsCourseRecord;
        this.dkfConceptHierarchy = conceptHierarchy;
        this.history = new ArrayList<GradedScoreNode>();
        this.knowledgeSession = session;
        this.chainOfCustody = chain;
    }
    /**
     * Compare AbstractScoreNode to all nodes found within DKF ConceptNode set by constructor
     * 
     * @param node - Abstract Score Node from LMSCourseRecord
     * 
     * @return true if found within dkfConceptHierarchy, false otherwise
     */
    public boolean isCourseConcept(AbstractScoreNode node) {
        return dkfConceptHierarchy != null && CourseRecordHelper.isCourseConcept(dkfConceptHierarchy.getConceptNode(), node);
    }
    /**
     * Compare AbstractScoreNode to Concept Node(s) found within dkfConceptHierarchy
     * and return matching Concept Node if found
     * 
     * @param node - Abstract Score Node used in comparison
     * 
     * @return corresponding Concept Node if found, null otherwise
     */
    public ConceptNode getCourseConcept(AbstractScoreNode node) {
        if(node == null || node.getName() == null) {
            return null;
        }
        if(dkfConceptHierarchy != null) {
            ConceptNode cnode = dkfConceptHierarchy.getConceptNode();
            if(cnode == null) {
                return null;
            }
            return CourseRecordHelper.getCourseConcept(cnode, node);
        } else {
            return null;
        }
    }
    /**
     * Recursively processes all Graded Score Node(s); building up history of parent GradedScoreNode(s) until a course concept GradedScoreNode
     * with child RawScoreNode is found. Once found, xAPI Statement generated and added to xAPI Statement accumulator.
     * 
     * @param node - GradedScoreNode to process, if nested, all children are also processed
     * @param history - Ordered list of parent GradedScoreNodes, updated per tree of GradedScoreNode children and reset after xAPI Statement generated
     * @param statements - xAPI Statement accumulator to add xAPI Statement(s) to
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    protected void processCourseRecord(GradedScoreNode node, List<GradedScoreNode> history, List<Statement> statements) throws LmsXapiProcessorException {
        for(AbstractScoreNode child : node.getChildren()) {
            if(child.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                List<GradedScoreNode> ancestory = new ArrayList<GradedScoreNode>(history);
                ancestory.add(node);
                processCourseRecord((GradedScoreNode) child, ancestory, statements);
            } else if(child.getType() == ScoreNodeTypeEnum.RAW_SCORE_NODE) {
                // check if GradedScoreNode is a course concept
                ConceptNode cnode = getCourseConcept(node);
                if(cnode != null) {
                    DateTime timestamp = new DateTime(courseRecord.getDate().getTime());
                    AbstractStatementGenerator gen;
                    if(chainOfCustody == null) {
                        try {
                            gen = new SkillAssessmentGenerator(knowledgeSession, timestamp, node, (RawScoreNode) child,
                                    history, cnode, domainId, domainSessionId, courseRecord.getCourseRecordRef());
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException | LmsXapiAgentException e) {
                            throw new LmsXapiProcessorException("Unable to initialize Assessed Assessment generator!", e);
                        }
                    } else {
                        try {
                            gen = new SkillAssessmentGenerator(knowledgeSession, timestamp, node, (RawScoreNode) child,
                                    history, cnode, domainId, chainOfCustody, courseRecord.getCourseRecordRef());
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException | LmsXapiAgentException e) {
                            throw new LmsXapiProcessorException("Unable to initialize Assessed Assessment generator!", e);
                        }
                    }
                    try {
                        gen.generateAndAdd(statements);
                    } catch (LmsXapiGeneratorException e) {
                        throw new LmsXapiProcessorException("Unable to generate Assessed Assessment statement!", e);
                    }
                }
            }
        }
    }
    @Override
    public void process(List<Statement> statements) throws LmsXapiProcessorException {
        if(statements == null) {
            throw new IllegalArgumentException("statements can not be null!");
        }
        processCourseRecord(courseRecord.getRoot(), history, statements);
    }
}
