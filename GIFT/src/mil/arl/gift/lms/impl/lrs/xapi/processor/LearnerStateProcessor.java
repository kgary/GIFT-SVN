package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
//import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.LearnerStateHelper;

/**
 * Process Learner State in xAPI Statements.
 * 
 *  - Affective and Cognitive State are used to generate Predicted Learner State Attribute xAPI Statements which
 *    contain a representation of the Learner State Attribute. xAPI Statements generated for Learner State Attribute Collections
 *    contain a reference to the associated Performance State Attribute.
 *  
 *  - Performance State is used to generate Demonstrated Performance State xAPI Statements (formative assessments) and Bookmarked xAPI Statement.
 *    A Bookmark xAPI Statement is generated when the Performance State contains a global bookmark. Formative assessment xAPI Statements are
 *    generated per AbstractPerformanceState (Tasks, Intermediate Concepts and Concepts) found within the Performance State.
 * 
 * @author Yet Analytics
 *
 */
public class LearnerStateProcessor extends AbstractProcessor {
    /** Learner State to process into xAPI Statement(s) */
    protected LearnerState learnerState;
    /** Affective Learner State Attribute Collections */
    protected Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> affectiveAttributeColl;
    /** Cognitive Learner State Attribute Collections */
    protected Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> cognitiveAttributeColl;
    /** Knowledge Session associated with Learner State */
    protected AbstractKnowledgeSession knowledgeSession;
    /** is the knowledge session active? */
    protected boolean isActiveKnowledgeSession;
    
    /**
     * Sets common state used within processing of Learner State into xAPI Statement(s)
     * 
     * @param actorSlug - User name associated with Learner State
     * @param learnerState - Learner State to process into xAPI Statement(s)
     * @param domainId - Identifier for the course associated with the Learner State
     * @param domainSessionId - Identifier for the Domain Session associated with actorSlug
     * @param session - Knowledge Session associated with the Learner State
     */
    public LearnerStateProcessor(String actorSlug, LearnerState learnerState, 
            String domainId, Integer domainSessionId, AbstractKnowledgeSession session) {
        super(actorSlug, domainId, domainSessionId);
        if(learnerState == null) {
            throw new IllegalArgumentException("Learner State can not be null!");
        }
        this.learnerState = learnerState;
        this.knowledgeSession = session;
//        if(session != null) {
//            this.isActiveKnowledgeSession = true;
//            SessionType st = session.getSessionType();
//            if(st == SessionType.ACTIVE) {
//                this.isActiveKnowledgeSession = true;
//            } else {
//                this.isActiveKnowledgeSession = false;
//            }
//        }
        this.isActiveKnowledgeSession = true;
        this.affectiveAttributeColl = LearnerStateHelper.onlyLearnerStateAttributeCollections(learnerState.getAffective());
        this.cognitiveAttributeColl = LearnerStateHelper.onlyLearnerStateAttributeCollections(learnerState.getCognitive());
    }
    @Override
    public void process(List<Statement> statements) throws LmsXapiProcessorException {
        processLearnerState(statements);
    }
    /**
     * Handle Predicted Affective Learner State Attribute associated with Performance State Attribute
     * 
     * @param collectionAttributes - Collection of LearnerStateAttributeCollection(s) searched based on navigation into Performance State
     * @param history - Tracks navigation into Performance State
     * @param statements - Collection of xAPI Statement(s) to potentially update
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    protected void processAffectiveLsaAssociatedPerformanceState(Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> collectionAttributes,
            List<AbstractPerformanceState> history, List<Statement> statements) throws LmsXapiProcessorException {
        if(isActiveKnowledgeSession) {
            if(collectionAttributes == null) {
                throw new IllegalArgumentException("collection Attributes can not be null!"); 
            }
            for(Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : collectionAttributes.entrySet()) {
                LearnerStateAttribute targetAttr = LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history);
                if(targetAttr != null) {
                    try {
                        LearnerStateHelper.generateAffectiveLsaStatement(statements, targetAttr, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Affective State with associated Performance State Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Handle Predicted Cognitive Learner State Attribute with associated Performance State Attribute
     * 
     * @param collectionAttributes - Collection of LearnerStateAttributeCollection(s) searched based on navigation into Performance State
     * @param history - Tracks navigation into Performance State
     * @param statements - Collection of xAPI Statement(s) to potentially update
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    protected void processCognitiveLsaAssociatedPerformanceState(Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> collectionAttributes,
            List<AbstractPerformanceState> history, List<Statement> statements) throws LmsXapiProcessorException {
        if(isActiveKnowledgeSession) {
            if(collectionAttributes == null) {
                throw new IllegalArgumentException("collection Attributes can not be null!"); 
            }
            for(Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : collectionAttributes.entrySet()) {
                LearnerStateAttribute targetAttr = LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history);
                if(targetAttr != null) {
                    try {
                        LearnerStateHelper.generateCognitiveLsaStatement(statements, targetAttr, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Cognitive Learner State with associated Performance State Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Handles top level Affective Learner State Attributes that are NOT Learner State Attribute Collections
     * 
     * @param statements - Collection of xAPI Statement(s) to potentially update
     * @param state - Affective State from Learner State
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement(s)
     */
    private void processAffectiveState(List<Statement> statements) throws LmsXapiProcessorException {
        if(isActiveKnowledgeSession) {
            for(Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> kv : learnerState.getAffective().getAttributes().entrySet()) {
                if(!(kv.getValue() instanceof LearnerStateAttributeCollection)) {
                    try {
                        LearnerStateHelper.generateAffectiveLsaStatement(statements, kv.getValue(), knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Affective State Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Handles top level Cognitive Learner State Attributes that are NOT Learner State Attribute Collections
     * 
     * @param statements - Collection of xAPI Statement(s) to potentially update
     * @param state - Cognitive State from Learner State
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement(s)
     */
    private void processCognitiveState(List<Statement> statements) throws LmsXapiProcessorException {
        if(isActiveKnowledgeSession) {
            for(Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> kv : learnerState.getCognitive().getAttributes().entrySet()) {
                if(!(kv.getValue() instanceof LearnerStateAttributeCollection)) {
                    try {
                        LearnerStateHelper.generateCognitiveLsaStatement(statements, kv.getValue(), knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Cognitive Learner State Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Process Learner State into xAPI Statements
     * 
     * @param statements - xAPI Statement accumulator
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement(s)
     */
    private void processLearnerState(List<Statement> statements) throws LmsXapiProcessorException {
        if(statements == null) {
            throw new IllegalArgumentException("Statements accumulator can not be null!");
        }
        PerformanceState performance = learnerState.getPerformance();
        // Bookmark statement
        try {
            LearnerStateHelper.generateBookmarkStatement(statements, performance, knowledgeSession, domainSessionId);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to generate bookmark statement!", e);
        }
        // Learner State Attributes are only processed when knowledge session is active
        // -> Affective Learner State Attribute processing
        processAffectiveState(statements);
        // -> Cognitive Learner State Attribute processing
        processCognitiveState(statements);
        // walk tasks
        for(Map.Entry<Integer, TaskPerformanceState> kv : performance.getTasks().entrySet()) {
            TaskPerformanceState taskState = kv.getValue();
            List<AbstractPerformanceState> history = new ArrayList<AbstractPerformanceState>();
            history.add(taskState);
            // Statement for current Task Performance State
            try {
                LearnerStateHelper.generateFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate Demonstrated Task Performance State Statement!", e);
            }
            // 0 or more statement(s) for Cognitive / Affective Learner State Attribute Collections related to current task
            // -> Affective Learner State Attribute Collections
            processAffectiveLsaAssociatedPerformanceState(affectiveAttributeColl, history, statements);
            // -> Cognitive Learner State Attribute Collections
            processCognitiveLsaAssociatedPerformanceState(cognitiveAttributeColl, history, statements);
            // statement(s) for children
            for(ConceptPerformanceState concept : taskState.getConcepts()) {
                List<AbstractPerformanceState> childrenPath = new ArrayList<AbstractPerformanceState>(history);
                childrenPath.add(concept);
                processLearnerState(statements, childrenPath, affectiveAttributeColl, cognitiveAttributeColl);
            }
        }
    }
    /**
     * Generate xAPI Statement for child and possibly recursively generate xAPI Statements for the child's children.
     * 
     * @param statements - xAPI Statement accumulator
     * @param child - child to generate xAPI Statement for
     * @param history - Navigation into Performance State
     * @param affectiveCollectionAttributes - Collection of LearnerStateAttributeCollection(s) searched based on navigation into Performance State
     * @param cognitiveCollectionAttributes - Collection of LearnerStateAttributeCollection(s) searched based on navigation into Performance State
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    protected void processLearnerState(List<Statement> statements, List<AbstractPerformanceState> history,
            Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> affectiveCollectionAttributes,
            Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> cognitiveCollectionAttributes) 
                    throws LmsXapiProcessorException {
        // create statement
        try {
            LearnerStateHelper.generateFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to generate Demonstrated Concept Performance State with parent Task Statement!", e);
        }
        // 0 or more statement(s) for Cognitive / Affective Learner State Attribute Collections related to child
        // -> Affective Learner State Attribute Collections
        processAffectiveLsaAssociatedPerformanceState(affectiveCollectionAttributes, history, statements);
        // -> Cognitive Learner State Attribute Collections
        processCognitiveLsaAssociatedPerformanceState(cognitiveCollectionAttributes, history, statements);
        // handle possible branch
        AbstractPerformanceState child = LearnerStateHelper.getLast(history);
        if(child instanceof IntermediateConceptPerformanceState) {
            for(ConceptPerformanceState concept : ((IntermediateConceptPerformanceState) child).getConcepts()) {
                List<AbstractPerformanceState> childrenPath = new ArrayList<AbstractPerformanceState>(history);
                childrenPath.add(concept);
                processLearnerState(statements, childrenPath, affectiveCollectionAttributes, cognitiveCollectionAttributes);
            }
        }
    }
}
