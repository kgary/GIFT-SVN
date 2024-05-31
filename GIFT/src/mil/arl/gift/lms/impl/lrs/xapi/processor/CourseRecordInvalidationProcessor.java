package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.CourseRecordHelper;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractVoidStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.SkillAssessmentGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.VoidSkillAssessmentGenerator;

public class CourseRecordInvalidationProcessor extends CourseRecordProcessor {
    /** LMS Course Record that represents an updated version of a previous LMS Course Record */
    private LMSCourseRecord updatedCourseRecord;
    /** Name of user responsible for the update */
    private String voider;
    /** Ordered list of Graded Score Node parent(s) for the updatedCourseRecord*/
    protected List<GradedScoreNode> newHistory;
    /**
     * Sets state used within generation of novel, replacement and voiding xAPI Statements
     * 
     * @param originalRecord - out-dated course record passed to LRSs' pastSessionCourseRecordUpdated method
     * @param updatedRecord - modified course record passed to LRSs' pastSessionCourseRecordUpdated method
     * @param conceptHierarchy - Hierarchy of Concepts from DKF passed to LRSs' pastSessionCourseRecordUpdated method
     * @param domainId - Identifier for the Course corresponding to lmsCourseRecord
     * @param domainSessionId - Domain Session identifier passed to LRSs' pastSessionCourseRecordUpdated method
     * @param session - Knowledge Session corresponding to the original LMSCourseRecord
     */
    public CourseRecordInvalidationProcessor(LMSCourseRecord originalRecord, LMSCourseRecord updatedRecord, Concepts.Hierarchy conceptHierarchy,
            String domainId, AssessmentChainOfCustody chain, AbstractKnowledgeSession session) {
        super(originalRecord, conceptHierarchy, domainId, chain, session);
        if(updatedRecord == null || session == null) {
            throw new IllegalArgumentException("updated Course Record and session can not be null!");
        }
        this.updatedCourseRecord = updatedRecord;
        this.voider = session.getHostSessionMember().getUserSession().getUsername();
        this.newHistory = new ArrayList<GradedScoreNode>();
    }
    /**
     * Generate voiding xAPI Statement, add to statement accumulator.
     * 
     * @param statements - xAPI Statement accumulator
     * @param gsn - GradedScoreNode from original Course Record
     * @param rsn - RawScoreNode for GradedScoreNode
     * @param dkf - DKF concept corresponding to GradedScoreNode
     * @param ancestors - Ordered collection of parents for GradedScoreNode
     * @param timestamp - Event time for the now out-dated assessment
     * 
     * @return Id of the statement being voided
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or generate statement
     */
    private UUID voidStatement(List<Statement> statements, GradedScoreNode gsn, RawScoreNode rsn,
            ConceptNode dkf, List<GradedScoreNode> ancestors, DateTime timestamp) throws LmsXapiProcessorException {
        AbstractVoidStatementGenerator gen;
        try {
            gen = new VoidSkillAssessmentGenerator(voider, knowledgeSession, timestamp, gsn, rsn,
                    ancestors, dkf, domainId, chainOfCustody, courseRecord.getCourseRecordRef());
        } catch (LmsXapiGeneratorException | LmsXapiProfileException | LmsXapiAgentException e) {
            throw new LmsXapiProcessorException("Unable to configure void assessed assessment generator!", e);
        }
        UUID targetStmtId;
        try {
            targetStmtId = gen.generateAndReturnTargetId(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("Unable to generate target assessed assessment statement!", e);
        }
        return targetStmtId;
    }
    /**
     * Generate replacement assessed assessment xAPI Statement and add to accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param replacementStmtId - Id of the xAPI Statement being replaced
     * @param gsn - GradedScoreNode from updated Course Record
     * @param rsn - RawScoreNode for GradedScoreNode
     * @param dkf - DKF concept corresponding to GradedScoreNode
     * @param ancestors - Ordered collection of parents for GradedScoreNode
     * @param timestamp - Event time for the updated assessment
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or generate statement
     */
    private void replacementStatement(List<Statement> statements, UUID replacementStmtId, GradedScoreNode gsn,
            RawScoreNode rsn, ConceptNode dkf, List<GradedScoreNode> ancestors, DateTime timestamp) throws LmsXapiProcessorException {
        AbstractStatementGenerator gen;
        try {
            gen = new SkillAssessmentGenerator(knowledgeSession, timestamp, gsn, rsn, ancestors, dkf,
                    domainId, chainOfCustody, updatedCourseRecord.getCourseRecordRef(), replacementStmtId, voider);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException | LmsXapiAgentException e) {
            throw new LmsXapiProcessorException("Unable to configure replacement assessed assessment generator!", e);
        }
        try {
            gen.generateAndAdd(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("Unable to generate replacement assessed assessment statement!", e);
        }
    }
    /**
     * Generate novel assessed assessment xAPI Statement and add to accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param gsn - GradedScoreNode from updated Course Record
     * @param rsn - RawScoreNode for GradedScoreNode
     * @param dkf - DKF concept corresponding to GradedScoreNode
     * @param ancestors - Ordered collection of parents for GradedScoreNode
     * @param timestamp - Event time for the novel assessment
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or generate statement
     */
    private void novelStatement(List<Statement> statements, GradedScoreNode gsn, RawScoreNode rsn,
            ConceptNode dkf, List<GradedScoreNode> ancestors, DateTime timestamp) throws LmsXapiProcessorException {
        AbstractStatementGenerator gen;
        try {
            gen = new SkillAssessmentGenerator(knowledgeSession, timestamp, gsn, rsn, ancestors, dkf,
                    domainId, chainOfCustody, updatedCourseRecord.getCourseRecordRef(), voider);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException | LmsXapiAgentException e) {
            throw new LmsXapiProcessorException("Unable to configure novel assessed assessment generator!", e);
        }
        try {
            gen.generateAndAdd(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("Unable to generate novel assessed assessment statement!", e);
        }
    }
    /**
     * Compare oldNode to newNode and generate all novel, invalidation and/or replacement statements
     * 
     * @param statements - statements accumulator
     * @param oldNode - GradedScoreNode from old LMS Course Record
     * @param newNode - GradedScoreNode from updated LMS Course Record
     * @param oldParents - GradedScoreNode parents for oldNode
     * @param newParents - GradedScoreNode parents for newNode
     * 
     * @throws LmsXapiProcessorException when unable to perform comparison or generate statement
     */
    private void processCourseRecordUpdate(List<Statement> statements, GradedScoreNode oldNode, GradedScoreNode newNode,
            List<GradedScoreNode> oldParents, List<GradedScoreNode> newParents) throws LmsXapiProcessorException {
        List<AbstractScoreNode> oldChildren, newChildren;
        oldChildren = oldNode != null ? oldNode.getChildren() : new ArrayList<AbstractScoreNode>(0);
        newChildren = newNode != null ? newNode.getChildren() : new ArrayList<AbstractScoreNode>(0);
        // old vs new
        for(AbstractScoreNode oldChild : oldChildren) {
            // proceed based on type of oldChild
            if(oldChild.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                // Find corresponding AbstractScoreNode
                AbstractScoreNode newChild = CourseRecordHelper.findNodeInCollShallow(oldChild, newChildren);
                // determine if difference exists within structure
                boolean isNotSameChild;
                if(oldChild instanceof TaskScoreNode) {
                    isNotSameChild = !CourseRecordHelper.isSameNode((TaskScoreNode) oldChild, (TaskScoreNode) newChild);
                } else {
                    isNotSameChild = !CourseRecordHelper.isSameNode((GradedScoreNode) oldChild, (GradedScoreNode) newChild);
                }
                List<GradedScoreNode> ancestoryOld = new ArrayList<GradedScoreNode>(oldParents);
                ancestoryOld.add(oldNode); 
                if(isNotSameChild) {
                    // difference exists within corresponding trees
                    List<GradedScoreNode> ancestoryNew = new ArrayList<GradedScoreNode>(newParents);
                    // NOTE: will contain null(s) once newChild is null BUT this tracks divergence between the two trees
                    //       and will always result in newChild being null within each successive iteration of this method.
                    //
                    //       The end result in that case is invalidation of GradeScoreNode tree based xAPI Statement(s)
                    //       opposed to invalidation of old AND replacement with new.
                    ancestoryNew.add(newNode);
                    processCourseRecordUpdate(statements, (GradedScoreNode) oldChild, (GradedScoreNode) newChild,
                            ancestoryOld, ancestoryNew);
                }
            } else if(oldChild.getType() == ScoreNodeTypeEnum.RAW_SCORE_NODE) {
                ConceptNode dkfNode = getCourseConcept(oldNode);
                // Only process RawScoreNode when parent is a Course Concept
                if(dkfNode != null) {
                    // Strict lookup of corresponding child
                    RawScoreNode newChild = CourseRecordHelper.findNodeInCollStrict((RawScoreNode) oldChild, newChildren);
                    // Determination if TaskScoreNodes difference exists at TaskScoreNode level
                    boolean sameGradedScoreNode = CourseRecordHelper.isSameNode(oldNode, newNode);
                    boolean notSameTaskScoreNode = oldNode instanceof TaskScoreNode && 
                            !CourseRecordHelper.isSameNode((TaskScoreNode) oldNode, (TaskScoreNode) newNode);
                    // Difference in TaskScoreNode unique properties not RawScoreNode
                    if(newChild != null && sameGradedScoreNode && notSameTaskScoreNode) {
                        // Invalidate xAPI Statement about oldNode + oldChild
                        UUID invalidatedStmtId = voidStatement(statements, oldNode, (RawScoreNode) oldChild,
                                dkfNode, oldParents, new DateTime(courseRecord.getDate().getTime()));
                        // create replacement using newNode + newChild w/ reference to original statement
                        replacementStatement(statements, invalidatedStmtId, newNode, newChild,
                                dkfNode, newParents, new DateTime(courseRecord.getDate().getTime()));
                    } else if(!sameGradedScoreNode && newChild == null) {
                        // Difference exists within GradedScoreNode, unable to find exact match to current child
                        // search for corresponding AbstractScoreNode with matching user names and RawScore units
                        newChild = CourseRecordHelper.findCorrespondingNodeInColl((RawScoreNode) oldChild, newChildren);
                        if(newChild != null) {
                            // Corresponding RawScoreNode found but has changed
                            // Invalidate xAPI Statement about oldNode + oldChild
                            UUID invalidatedStmtId = voidStatement(statements, oldNode, (RawScoreNode) oldChild,
                                    dkfNode, oldParents, new DateTime(courseRecord.getDate().getTime()));
                            // create replacement using newNode + newChild w/ reference to original statement
                            replacementStatement(statements, invalidatedStmtId, newNode, newChild,
                                    dkfNode, newParents, new DateTime(courseRecord.getDate().getTime()));
                        } else {
                            // invalidate xAPI Statement about oldNode + oldChild
                            voidStatement(statements, oldNode, (RawScoreNode) oldChild,
                                    dkfNode, oldParents, new DateTime(courseRecord.getDate().getTime()));
                        }
                    }
                }
            }
        }
        // new vs old
        for(AbstractScoreNode newChild : newChildren) {
            // NOTE: when there is an oldChild found, the handling already covered within oldChildren loop
            if(newChild.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                AbstractScoreNode oldChild = CourseRecordHelper.findNodeInCollShallow(newChild, oldChildren);
                List<GradedScoreNode> ancestoryNew = new ArrayList<GradedScoreNode>(newParents);
                ancestoryNew.add(newNode);
                if(oldChild == null) {
                    // NOTE: continue with newChild and null oldNode which prevents unnecessary execution
                    //       of the old vs new loop and results in generation of novel xAPI Statement(s).
                    processCourseRecordUpdate(statements, (GradedScoreNode) oldChild, (GradedScoreNode) newChild,
                            oldParents, ancestoryNew);
                }
            } else if(newChild.getType() == ScoreNodeTypeEnum.RAW_SCORE_NODE) {
                ConceptNode dkfNode = getCourseConcept(newNode);
                if(dkfNode != null) {
                    // Check at user names + units level, if nothing found then newChild is novel
                    RawScoreNode oldChild = CourseRecordHelper.findCorrespondingNodeInColl((RawScoreNode) newChild, oldChildren);
                    if(oldChild == null) {
                        novelStatement(statements, newNode, (RawScoreNode) newChild, dkfNode,
                                newParents, new DateTime(updatedCourseRecord.getDate().getTime()));
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
        // NOTE: this assumes that the root GradedScoreNode is the same for both records
        //       if this assumption is incorrect, will need to invalidate all of oldRecord
        //       and generate novel for newRecord
        processCourseRecordUpdate(statements, courseRecord.getRoot(), updatedCourseRecord.getRoot(), history, newHistory);
    }
}
