package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
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
 * Step through original and updated Learner State, comparing corresponding items from both at each step.
 * 
 * Generation of xAPI Statements is based on the result of the comparison.
 * 
 * - Item found within original Learner State but not updated Learner State results in voiding xAPI statement(s)
 *   which target the xAPI Statement(s) generated from the item
 *   
 * - Item found within both Learner States but its properties differ across Learner States results
 *   in 1) voiding xAPI Statement(s) which target the xAPI Statement(s) generated from the original properties
 *   and 2) replacement xAPI Statement(s) which reference the original xAPI Statement(s) and represent the updated properties
 *   
 * - Item found within updated Learner State but not the original Learner State results in generation of novel
 *   xAPI Statement(s) based on the properties of the item.
 *   
 * When an Item is found within both Learner States and its properties do not differ across Learner States results
 * in a no-op
 * 
 * @author Yet Analytics
 *
 */
public class LearnerStateInvalidationProcessor extends LearnerStateProcessor {
    
    private static Logger logger = LoggerFactory.getLogger(LearnerStateInvalidationProcessor.class);
    /** Updated Learner State */
    protected LearnerState updatedLearnerState;
    /** Affective Learner State Attribute Collections from updated Learner State */
    protected Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> updatedAffectiveAttributeColl;
    /** Cognitive Learner State Attribute Collections from updated Learner State */
    protected Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> updatedCognitiveAttributeColl;
    /** do the edits apply to an Active Knowledge Session */
    protected boolean isActivePastKnowledgeSession;
    /**
     * Configures LearnerStateProcessor and Learner State Helper
     * 
     * @param updatedState - Updated Learner State to compare to original
     * @param originalState - Original and now out-dated Learner State
     * @param octSlug - Name of user making edits to the Learner State
     * @param domainId - Identifier for course associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param session - Knowledge Session associated with Learner State
     */
    public LearnerStateInvalidationProcessor(LearnerState updatedState, LearnerState originalState, 
            String octSlug, String domainId, Integer domainSessionId, AbstractKnowledgeSession session) {
        super(octSlug, originalState, domainId, domainSessionId, session);
        if(updatedState == null) {
            throw new IllegalArgumentException("Updated Learner State can not be null!");
        }
        this.updatedLearnerState = updatedState;
        this.updatedAffectiveAttributeColl = LearnerStateHelper.onlyLearnerStateAttributeCollections(updatedLearnerState.getAffective());
        this.updatedCognitiveAttributeColl = LearnerStateHelper.onlyLearnerStateAttributeCollections(updatedLearnerState.getCognitive());
        boolean isActiveSession;
        SessionType st = session.getSessionType();
        if(st == SessionType.ACTIVE) {
            isActiveSession = true;
        } else {
            isActiveSession = false;
        }
        this.isActivePastKnowledgeSession = isActiveSession && knowledgeSession.inPastSessionMode();
    }
    @Override
    public void process(List<Statement> statements) throws LmsXapiProcessorException {
        handleLearnerStateUpdate(statements);
    }
    /**
     * Interface which contains callback method attached to comparison inner classes
     * 
     * @author Yet Analytics
     *
     */
    public interface Reacts {
        /**
         * Common method implemented by classes which correspond to comparison conditions. Each class defines the implementation of this method
         * 
         * @param statements - Collection of xAPI Statements to update
         * @param original - KV pair or ConceptPerformanceState found within Original Learner State, can be null
         * @param updated - KV pair or ConceptPerformanceState found within Updated Learner State, can be null
         * 
         * @throws LmsXapiProcessorException allows for callback classes' implementation to throw
         */
        public void react(List<Statement> statements, Object original, Object updated) throws LmsXapiProcessorException;
    }
    /**
     * Performs comparison between original and updated and calls react method of callback
     * 
     * @param statements - Statement Accumulator
     * @param original - Map or List from Original Learner State
     * @param updated - Map or List from Updated Learner State
     * @param callback - Inner class that implements Reacts
     * 
     * @throws LmsXapiProcessorException when call to react results in an error
     */
    public static void compareAndReact(List<Statement> statements, Object original, Object updated, Reacts callback) throws LmsXapiProcessorException {
        if(statements == null) {
            throw new IllegalArgumentException("statements can not be null!");
        }
        if(original instanceof Map && updated instanceof Map) {
            Map<?, ?> om = (Map<?, ?>) original;
            Map<?, ?> um = (Map<?, ?>) updated;
            for(Entry<?, ?> oKV : om.entrySet()) {
                Object oKey = oKV.getKey();
                Object oVal = oKV.getValue();
                Object uVal = um.get(oKey);
                if(uVal == null) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("The following was not found within the updated state: ");
                        logger.debug(oKV.toString());
                        logger.debug("");
                    }
                    callback.react(statements, oKV, null);
                } else {
                    Entry<?, ?> uKV = new AbstractMap.SimpleEntry<>(oKey, uVal);
                    if(!oVal.equals(uVal)) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("The following Changed: ");
                            logger.debug("Original State: "+oKV);
                            logger.debug("Updated State: "+uKV);
                            logger.debug("");
                        }
                        callback.react(statements, oKV, uKV);
                    }
                }
            }
            for(Entry<?, ?> uKV : um.entrySet()) {
                if(om.get(uKV.getKey()) == null) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("The following was not found within the original state: ");
                        logger.debug(uKV.toString());
                        logger.debug("");
                    }
                    callback.react(statements, null, uKV);
                }
            }
        } else if(original instanceof List && updated instanceof List) {
            List<?> ol = (List<?>) original;
            List<?> ul = (List<?>) updated;
            for(Object oItem : ol) {
                if(oItem instanceof ConceptPerformanceState) {
                    ConceptPerformanceState oCPS = (ConceptPerformanceState) oItem;
                    ConceptPerformanceState uCPS = null;
                    for(Object uItem : ul) {
                        if(uItem instanceof ConceptPerformanceState &&
                                ((ConceptPerformanceState) uItem).getState().getName().equals(oCPS.getState().getName())) {
                            uCPS = (ConceptPerformanceState) uItem;
                            break;
                        }
                    }
                    if(uCPS == null) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("concept only found within oldState!");
                            logger.debug(oCPS.toString());
                            logger.debug("");
                        }
                        callback.react(statements, oCPS, null);
                    } else if(!oCPS.equals(uCPS)) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("concept differs across states!");
                            logger.debug("Original Concept: "+oCPS);
                            logger.debug("Updated Concept: "+uCPS);
                            logger.debug("");
                        }
                        callback.react(statements, oCPS, uCPS);
                    }
                }
            }
            for(Object uItem : ul) {
                if(uItem instanceof ConceptPerformanceState) {
                    ConceptPerformanceState uCPS = (ConceptPerformanceState) uItem;
                    ConceptPerformanceState oCPS = null;
                    for(Object oItem : ol) {
                        if(oItem instanceof ConceptPerformanceState 
                                && ((ConceptPerformanceState) oItem).getState().getName().equals(uCPS.getState().getName())) {
                            oCPS = (ConceptPerformanceState) oItem;
                            break;
                        }
                    }
                    if(oCPS == null) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("concept only found within updated state!");
                            logger.debug(uCPS.toString());
                            logger.debug("");
                        }
                        callback.react(statements, null, uCPS);
                    }
                }
            }
        }
    }
    /**
     * Step through Original and Updated Learner States, comparing along the way to determine if
     * 
     * - any novel xAPI Statements need to be generated based on novel data found within Updated Learner State
     * - any out-dated xAPI Statements need to be invalidated (without replacement) when data found in Original but not Updated Learner State
     * - replace and invalidate any out-dated xAPI Statements when data found in both Original and Updated Learner States but a difference exists
     * 
     * @param statements - xAPI Statement accumulator
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    private void handleLearnerStateUpdate(List<Statement> statements) throws LmsXapiProcessorException {
        if(statements == null) {
            throw new IllegalArgumentException("statement accumulator can not be null!");
        }
        // Bookmark statements
        PerformanceState performance = updatedLearnerState.getPerformance();
        try {
            LearnerStateHelper.generateBookmarkStatement(statements, performance, knowledgeSession, domainSessionId);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to generate bookmark statement!", e);
        }
        // Learner State Attributes Statements only generated from Active Knowledge Sessions
        if(isActivePastKnowledgeSession) {
            // -> Affective Learner State Attributes
            compareAndReact(statements,
                    learnerState.getAffective().getAttributes(),
                    updatedLearnerState.getAffective().getAttributes(),
                    new AffectiveLsaCallback());
            // -> Cognitive Learner State Attributes
            compareAndReact(statements,
                    learnerState.getCognitive().getAttributes(),
                    updatedLearnerState.getCognitive().getAttributes(),
                    new CognitiveLsaCallback());
        }
        // Compare Performance State
        Map<Integer, TaskPerformanceState> newTasks = updatedLearnerState.getPerformance().getTasks();
        Map<Integer, TaskPerformanceState> oldTasks = learnerState.getPerformance().getTasks();
        compareAndReact(statements, oldTasks, newTasks, new TaskStateCallback());
    }
    /**
     * Void Affective Learner State Attribute Collection with associated Performance State xAPI Statement(s)
     * 
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or create xAPI statement
     */
    protected void voidAllPredictedAffectiveLsaForState(List<Statement> statements, List<AbstractPerformanceState> history) 
            throws LmsXapiProcessorException {
        if(isActivePastKnowledgeSession) {
            // Learner State Attributes Statements only generated from Active Knowledge Sessions
            for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : affectiveAttributeColl.entrySet()) {
                LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history);
                if(lsa != null) {
                    try {
                        LearnerStateHelper.voidAffectiveLsaStatement(statements, lsa, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Affective Lsa Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Performs comparison of Affective Learner State Attribute Collections which reference Performance State Attribute.
     * 
     * Generates corresponding xAPI Statements when there are relevant LSA(s) for the PSA (last entry of both lists).
     *  
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State of original Learner State
     * @param updatedHistory - Navigation into Performance State of updated Learner State
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or create xAPI Statement
     */
    protected void handlePredictedAffectiveLsaForStateDelta(List<Statement> statements, 
            List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory)
                    throws LmsXapiProcessorException {
        // Map of relevant LearnerStateAttribute collections based on last entry of history
        Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> stateAttributeCollections = 
                new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection>();
        // Map of relevant LearnerStateAttribute collections based on last entry of updatedHistory
        Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> updatedStateAttributeCollections = 
                new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection>();
        // Populate maps
        for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : affectiveAttributeColl.entrySet()) {
            if(LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history) != null) {
                stateAttributeCollections.put(kv.getKey(), kv.getValue());
            }
        }
        for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : updatedAffectiveAttributeColl.entrySet()) {
            if(LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), updatedHistory) != null) {
                updatedStateAttributeCollections.put(kv.getKey(), kv.getValue());
            }
        }
        // perform comparison check on relevant map entries
        compareAndReact(statements,
                stateAttributeCollections, 
                updatedStateAttributeCollections,
                new AffectiveLsaCallback(history, updatedHistory));
    }
    /**
     * Void Cognitive Learner State Attribute Collection with associated Performance State xAPI Statement(s)
     * 
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or create xAPI statement
     */
    protected void voidAllPredictedCognitiveLsaForState(List<Statement> statements, List<AbstractPerformanceState> history) 
            throws LmsXapiProcessorException {
        if(isActivePastKnowledgeSession) {
            // Learner State Attributes Statements only generated from Active Knowledge Sessions
            for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : cognitiveAttributeColl.entrySet()) {
                LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history);
                if(lsa != null) {
                    try {
                        LearnerStateHelper.voidCognitiveLsaStatement(statements, lsa, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Cognitive Lsa Statement!", e);
                    }
                }
            }
        }   
    }
    /**
     * Performs comparison of Cognitive Learner State Attribute Collections which reference Performance State Attribute.
     * 
     * Generates corresponding xAPI Statements when there are relevant LSA(s) for the PSA (last entry of both lists)
     * 
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State of original Learner State
     * @param updatedHistory - Navigation into Performance State of updated Learner State
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or create xAPI Statement
     */
    protected void handlePredictedCognitiveLsaForStateDelta(List<Statement> statements, 
            List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory)
                    throws LmsXapiProcessorException {
        // Map of relevant LearnerStateAttribute collections based on last entry of history
        Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> stateAttributeCollections = 
                new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection>();
        // Map of relevant LearnerStateAttribute collections based on last entry of updatedHistory
        Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> updatedStateAttributeCollections = 
                new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection>();
        // Populate maps
        for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : cognitiveAttributeColl.entrySet()) {
            if(LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), history) != null) {
                stateAttributeCollections.put(kv.getKey(), kv.getValue());
            }
        }
        for(Entry<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> kv : updatedCognitiveAttributeColl.entrySet()) {
            if(LearnerStateHelper.searchInLearnerStateAttributeColl(kv.getValue(), updatedHistory) != null) {
                updatedStateAttributeCollections.put(kv.getKey(), kv.getValue());
            }
        }
        // perform comparison check on relevant map entries
        compareAndReact(statements,
                stateAttributeCollections, 
                updatedStateAttributeCollections,
                new CognitiveLsaCallback(history, updatedHistory));
    }
    /**
     * Invalidation of a child Concept Performance State and possibly its children
     * 
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State
     * @param child - Child to create voiding xAPI statement for
     * 
     * @throws LmsXapiProcessorException when unable to create voiding xAPI Statement
     */
    protected void invalidateChildConcepts(List<Statement> statements, List<AbstractPerformanceState> history, ConceptPerformanceState child) throws LmsXapiProcessorException {
        if(child == null) {
            throw new IllegalArgumentException("child ConceptPerformanceState can not be null!");
        }
        if(history == null) {
            throw new IllegalArgumentException("history can not be null!");
        }
        List<AbstractPerformanceState> childPath = new ArrayList<AbstractPerformanceState>(history);
        childPath.add(child);
        if(isActivePastKnowledgeSession) {
            // Learner State Attributes Statements only generated from Active Knowledge Sessions
            voidAllPredictedAffectiveLsaForState(statements, childPath);
            voidAllPredictedCognitiveLsaForState(statements, childPath);
        }
        // void statement for child
        try {
            LearnerStateHelper.voidFormativeAssessmentStatement(statements, childPath, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to generate Void Concept Formative Assessment Statement!", e);
        }
        // void child's children
        if(child instanceof IntermediateConceptPerformanceState) {
            // void statements associated with children of ic
            for(ConceptPerformanceState concept : ((IntermediateConceptPerformanceState) child).getConcepts()) {
                invalidateChildConcepts(statements, childPath, concept);
            }
        }
    }
    /**
     * Generation of new statement from child using LearnerStateProcessor methods
     * 
     * @param statements - Statement accumulator
     * @param history - Navigation into Performance State
     * @param child - Child to create xAPI Statement from
     * 
     * @throws LmsXapiProcessorException when unable to generate xAPI Statement
     */
    protected void generateNovel(List<Statement> statements, List<AbstractPerformanceState> history, ConceptPerformanceState child) 
            throws LmsXapiProcessorException {
        if(child == null) {
            throw new IllegalArgumentException("child ConceptPerformanceState can not be null!");
        }
        if(history == null) {
            throw new IllegalArgumentException("history can not be null!");
        }
        List<AbstractPerformanceState> childrenPath = new ArrayList<AbstractPerformanceState>(history);
        childrenPath.add(child);
        processLearnerState(statements, childrenPath, updatedAffectiveAttributeColl, updatedCognitiveAttributeColl);
    }    
    /**
     * Handle children deltas
     * 
     * @param statements Statement accumulator
     * @param history - Navigation into original Performance State
     * @param updatedHistory - Navigation into updated Performance State
     * @param oldChild - Concept Performance State from original Performance State
     * @param newChild - Concept Performance State from updated Performance State
     * 
     * @throws LmsXapiProcessorException when unable to create xAPI Statement
     */
    protected void handleChildrenDelta(List<Statement> statements, List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory, 
            ConceptPerformanceState oldChild, ConceptPerformanceState newChild) throws LmsXapiProcessorException {
        if(oldChild == null || newChild == null) {
            throw new IllegalArgumentException("neither child ConceptPerformanceState can be null!");
        }
        if(history == null || updatedHistory == null) {
            throw new IllegalArgumentException("neither history can not be null!");
        }
        // Update paths
        history.add(oldChild);
        updatedHistory.add(newChild);
        // Learner State Attributes Statements only generated from Active Knowledge Sessions
        if(isActivePastKnowledgeSession) {
            handlePredictedAffectiveLsaForStateDelta(statements, history, updatedHistory);
            handlePredictedCognitiveLsaForStateDelta(statements, history, updatedHistory);
        }
        // Branch based on type of children
        if(oldChild instanceof IntermediateConceptPerformanceState && newChild instanceof IntermediateConceptPerformanceState) {
            // Different - both have children
            IntermediateConceptPerformanceState oldIc = (IntermediateConceptPerformanceState) oldChild;
            IntermediateConceptPerformanceState newIc = (IntermediateConceptPerformanceState) newChild;
            // -> Invalidate and replace when difference exists at top level
            if(!LearnerStateHelper.samePerformanceStateShallow(oldChild, newChild)) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Intermediate Concept Performance States contain difference at top lvel!");
                    logger.debug("old Intermediate Concept Performance State: "+oldChild.getState());
                    logger.debug("new Intermediate Concept Performance State: "+newChild.getState());
                    logger.debug("");
                }
                UUID voidedDemonstratedIcId;
                try {
                    voidedDemonstratedIcId = LearnerStateHelper.voidFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
                } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to generate Void Intermediate Concept Formative Assessment Statement!", e);
                }
                try {
                    LearnerStateHelper.generateFormativeAssessmentStatement(statements, updatedHistory, voidedDemonstratedIcId, knowledgeSession, domainSessionId, domainId, actorSlug);
                } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to generate Replacement Intermediate Concept Formative Assessment Statement!", e);
                }
            }
            // Handle children
            List<ConceptPerformanceState> oldChildren = oldIc.getConcepts();
            List<ConceptPerformanceState> newChildren = newIc.getConcepts();
            compareAndReact(statements, oldChildren, newChildren, new ChildStateCallback(history, updatedHistory));
        } else if(oldChild instanceof IntermediateConceptPerformanceState && !(newChild instanceof IntermediateConceptPerformanceState)) {
            // Different - old has children, new doesn't
            IntermediateConceptPerformanceState oldIc = (IntermediateConceptPerformanceState) oldChild;
            // -> Invalidate and replace
            UUID voidedDemonstratedIcId;
            try {
                voidedDemonstratedIcId = LearnerStateHelper.voidFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate Void Intermediate Concept Formative Assessment Statement!", e);
            }
            try {
                LearnerStateHelper.generateFormativeAssessmentStatement(statements, updatedHistory, voidedDemonstratedIcId, knowledgeSession, domainSessionId, domainId, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate replacement Intermediate Concept Formative Assessment Statement!", e);
            }
            // -> Void all children statements
            for(ConceptPerformanceState concept : oldIc.getConcepts()) {
                invalidateChildConcepts(statements, history, concept);
            }
        } else if(!(oldChild instanceof IntermediateConceptPerformanceState) && newChild instanceof IntermediateConceptPerformanceState) {
            // Different - new has children, old doesn't
            IntermediateConceptPerformanceState newIc = (IntermediateConceptPerformanceState) newChild;
            // -> Invalidate and replace
            UUID voidedDemonstratedConceptId;
            try {
                voidedDemonstratedConceptId = LearnerStateHelper.voidFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate Void Concept Formative Assessment Statement!", e);
            }
            try {
                LearnerStateHelper.generateFormativeAssessmentStatement(statements, updatedHistory, voidedDemonstratedConceptId, knowledgeSession, domainSessionId, domainId, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate Replacement Concept Formative Assessment Statement!", e);
            }
            // -> Create statements from new children
            for(ConceptPerformanceState concept : newIc.getConcepts()) {
                generateNovel(statements, updatedHistory, concept);
            }
        } else {
            // invalidate and replace concept children
            UUID voidedDemonstratedConceptId;
            try {
                voidedDemonstratedConceptId = LearnerStateHelper.voidFormativeAssessmentStatement(statements, history, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate Void Concept Formative Assessment Statement!", e);
            }
            try {
                LearnerStateHelper.generateFormativeAssessmentStatement(statements, updatedHistory, voidedDemonstratedConceptId, knowledgeSession, domainSessionId, domainId, actorSlug);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to generate replacement Concept Formative Assessment Statement!", e);
            }
        }
    }
    /**
     * Implementation of react specific to TaskPerformanceStates
     * 
     * @author Yet Analytics
     *
     */
    public class TaskStateCallback implements Reacts {
        @Override
        public void react(List<Statement> statements, Object original, Object updated) throws LmsXapiProcessorException {
            List<AbstractPerformanceState> oHistory = new ArrayList<AbstractPerformanceState>();
            List<AbstractPerformanceState> uHistory = new ArrayList<AbstractPerformanceState>();
            if(original != null && updated == null) {
                // only in old
                TaskPerformanceState task = (TaskPerformanceState) ((Entry<?, ?>) original).getValue();
                oHistory.add(task);
                // Void invalid Demonstrated Task Performance State xAPI Statement
                try {
                    LearnerStateHelper.voidFormativeAssessmentStatement(statements, oHistory, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
                } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to generate Void Task Formative Assessment Statement!", e);
                }
                // Void invalid Predicted LSA associated with task
                // Learner State Attributes Statements only generated from Active Knowledge Sessions
                if(isActivePastKnowledgeSession) {
                    // Invalidate 0 or more statement(s) for Cognitive / Affective Learner State Attribute Collections related to current task
                    voidAllPredictedAffectiveLsaForState(statements, oHistory);
                    voidAllPredictedCognitiveLsaForState(statements, oHistory);
                }
                // void all statements derived from now invalidated TaskState children
                for(ConceptPerformanceState concept : task.getConcepts()) {
                    invalidateChildConcepts(statements, oHistory, concept);
                }
            } else if(original == null && updated != null) {
                // only in new
                TaskPerformanceState task = (TaskPerformanceState) ((Entry<?, ?>) updated).getValue();
                uHistory.add(task);
                // Create xAPI Statement from task performance state
                try {
                    LearnerStateHelper.generateFormativeAssessmentStatement(statements, uHistory, knowledgeSession, domainSessionId, domainId, actorSlug);
                } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to generate Novel Demonstrated Task Generator!", e);
                }
                // Learner State Attributes Statements only generated from Active Knowledge Sessions
                if(isActivePastKnowledgeSession) {
                    // Predicted Learner State Attribute with associated Performance State xAPI Statements
                    processAffectiveLsaAssociatedPerformanceState(updatedAffectiveAttributeColl, uHistory, statements);
                    processCognitiveLsaAssociatedPerformanceState(updatedCognitiveAttributeColl, uHistory, statements);
                }
                // Novel xAPI Statements from Children
                for(ConceptPerformanceState concept : task.getConcepts()) {
                    generateNovel(statements, uHistory, concept);
                }
            } else if(original != null && updated != null) {
                // in both but delta
                TaskPerformanceState oTask = (TaskPerformanceState) ((Entry<?, ?>) original).getValue();
                oHistory.add(oTask);
                TaskPerformanceState uTask = (TaskPerformanceState) ((Entry<?, ?>) updated).getValue();
                uHistory.add(uTask);
                // Learner State Attributes Statements only generated from Active Knowledge Sessions
                if(isActivePastKnowledgeSession) {
                    // Void and replace 0 or more statement(s) for Cognitive / Affective Learner State Attribute Collections related to old task
                    handlePredictedAffectiveLsaForStateDelta(statements, oHistory, uHistory);
                    handlePredictedCognitiveLsaForStateDelta(statements, oHistory, uHistory);
                }
                // invalidate and replace when difference exists at the top level
                if(!LearnerStateHelper.compareTaskPerformanceStates(oTask, uTask)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Tasks contain difference at top lvel!");
                        logger.debug("original Task State: "+oTask.getState());
                        logger.debug("updated Task State: "+uTask.getState());
                        logger.debug("");
                    }
                    UUID voidedDemonstartedTaskId;
                    try {
                        voidedDemonstartedTaskId = LearnerStateHelper.voidFormativeAssessmentStatement(statements, oHistory, knowledgeSession, domainSessionId, domainId, actorSlug, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Task Formative Assessment Statement!", e);
                    }
                    try {
                        LearnerStateHelper.generateFormativeAssessmentStatement(statements, uHistory, voidedDemonstartedTaskId, knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Replacement Task Formative Assessment Statement!", e);
                    }
                }
                // Handle children comparison
                compareAndReact(statements, oTask.getConcepts(), uTask.getConcepts(), new ChildStateCallback(oHistory, uHistory));
            }
        }
    }
    /**
     * Implementation of react that handles IntermediateConceptPerformanceState and ConceptPerformanceState
     * 
     * @author Yet Analytics
     *
     */
    public class ChildStateCallback implements Reacts {
        private List<AbstractPerformanceState> history;
        private List<AbstractPerformanceState> updatedHistory;
        public ChildStateCallback(List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory) {
            this.history = history;
            this.updatedHistory = updatedHistory;
        }
        @Override
        public void react(List<Statement> statements, Object original, Object updated) throws LmsXapiProcessorException {
            if(original != null && updated == null) {
                invalidateChildConcepts(statements, history, (ConceptPerformanceState) original);
            } else if(original == null && updated != null) {
                generateNovel(statements, updatedHistory, (ConceptPerformanceState) updated);
            } else if(original != null && updated != null) {
                handleChildrenDelta(statements, history, updatedHistory, (ConceptPerformanceState) original, (ConceptPerformanceState) updated);
            }
        }
    }
    /**
     * Implementation of react that handles Affective LearnerStateAttribute and LearnerStateAttributeCollection
     * 
     * @author Yet Analytics
     *
     */
    public class AffectiveLsaCallback implements Reacts {
        private List<AbstractPerformanceState> history;
        private List<AbstractPerformanceState> updatedHistory;
        public AffectiveLsaCallback() {}
        public AffectiveLsaCallback(List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory) {
            this.history = history;
            this.updatedHistory = updatedHistory;
        }
        @Override
        public void react(List<Statement> statements, Object original, Object updated) throws LmsXapiProcessorException {
            if(original != null && updated == null) {
                Entry<?, ?> oKV = (Entry <?, ?>) original;
                if(history != null && oKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) oKV.getValue(), history);
                    if(lsa != null) {
                        try {
                            LearnerStateHelper.voidAffectiveLsaStatement(statements, lsa, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Void Predicted Affective Lsa Statement!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    try {
                        LearnerStateHelper.voidAffectiveLsaStatement(statements, (LearnerStateAttribute) oKV.getValue(), 
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Affective Lsa Statement!", e);
                    }
                }
            } else if(original == null && updated != null) {
                Entry<?, ?> uKV = (Entry<?, ?>) updated;
                if(updatedHistory != null && uKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) uKV.getValue(), updatedHistory);
                    if(lsa != null) {
                        try {
                            LearnerStateHelper.generateAffectiveLsaStatement(statements, lsa, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to derive Novel Predicted Affective Lsa associated with performance state generator!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    try {
                        LearnerStateHelper.generateAffectiveLsaStatement(statements, (LearnerStateAttribute) uKV.getValue(), 
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Novel Predicted Affective Lsa statement!", e);
                    }
                }
            } else if(original != null && updated != null) {
                Entry<?, ?> oKV = (Entry <?, ?>) original;
                Entry<?, ?> uKV = (Entry<?, ?>) updated;
                if(history != null && oKV.getValue() instanceof LearnerStateAttributeCollection &&
                        updatedHistory != null && uKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute oldLsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) oKV.getValue(), history);
                    LearnerStateAttribute updatedLsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) uKV.getValue(), updatedHistory);
                    UUID invalidatedStmtId = null;
                    if(oldLsa != null) {
                        try {
                            invalidatedStmtId = LearnerStateHelper.voidAffectiveLsaStatement(statements, oldLsa, history,
                                    knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Void Predicted Affective Lsa Statement!", e);
                        }
                    }
                    if(invalidatedStmtId != null && updatedLsa != null) {
                        try {
                            LearnerStateHelper.generateAffectiveLsaStatement(statements, updatedLsa, updatedHistory, invalidatedStmtId,
                                    knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate replacement Predicted Affective Lsa Statement!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    // Void the old Learner State Attribute
                    UUID voidedStmtId;
                    try {
                        voidedStmtId = LearnerStateHelper.voidAffectiveLsaStatement(statements, (LearnerStateAttribute) oKV.getValue(),
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Affective Lsa Statement!", e);
                    }
                    // Generate replacement from new
                    try {
                        LearnerStateHelper.generateAffectiveLsaStatement(statements, (LearnerStateAttribute) uKV.getValue(), voidedStmtId, 
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Replacement Predicted Affective Lsa Statement!", e);
                    }
                }
            }
        }
    }
    /**
     * Implementation of react that handles Cognitive LearnerStateAttribute and LearnerStateAttributeCollection
     * 
     * @author Yet Analytics
     *
     */
    public class CognitiveLsaCallback implements Reacts {
        private List<AbstractPerformanceState> history;
        private List<AbstractPerformanceState> updatedHistory;
        public CognitiveLsaCallback() {}
        public CognitiveLsaCallback(List<AbstractPerformanceState> history, List<AbstractPerformanceState> updatedHistory) {
            this.history = history;
            this.updatedHistory = updatedHistory;
        }
        @Override
        public void react(List<Statement> statements, Object original, Object updated) throws LmsXapiProcessorException {
            if(original != null && updated == null) {
                Entry<?, ?> oKV = (Entry <?, ?>) original;
                if(history != null && oKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) oKV.getValue(), history);
                    if(lsa != null) {
                        try {
                            LearnerStateHelper.voidCognitiveLsaStatement(statements, lsa, history, knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Void Predicted Cognitive Lsa Statement!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    try {
                        LearnerStateHelper.voidCognitiveLsaStatement(statements, (LearnerStateAttribute) oKV.getValue(),
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Cognitive Lsa Statement!", e);
                    }
                }
            } else if(original == null && updated != null) {
                Entry<?, ?> uKV = (Entry<?, ?>) updated;
                if(updatedHistory != null && uKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute lsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) uKV.getValue(), updatedHistory);
                    if(lsa != null) {
                        try {
                            LearnerStateHelper.generateCognitiveLsaStatement(statements, lsa, updatedHistory, knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Novel Predicted Cognitive Lsa assocaited with performacne state statement!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    try {
                        LearnerStateHelper.generateCognitiveLsaStatement(statements, (LearnerStateAttribute) uKV.getValue(),
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Novel Predicted Cognitive Lsa Statement!", e);
                    }
                }
                
            } else if(original != null && updated != null) {
                Entry<?, ?> oKV = (Entry <?, ?>) original;
                Entry<?, ?> uKV = (Entry<?, ?>) updated;
                if(history != null && oKV.getValue() instanceof LearnerStateAttributeCollection &&
                        updatedHistory != null && uKV.getValue() instanceof LearnerStateAttributeCollection) {
                    // Learner State Attribute Collection
                    LearnerStateAttribute oldLsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) oKV.getValue(), history);
                    LearnerStateAttribute updatedLsa = LearnerStateHelper.searchInLearnerStateAttributeColl((LearnerStateAttributeCollection) uKV.getValue(), updatedHistory);
                    UUID invalidatedStmtId = null;
                    if(oldLsa != null) {
                        try {
                            invalidatedStmtId = LearnerStateHelper.voidCognitiveLsaStatement(statements, oldLsa, history,
                                    knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Void Predicted Cognitive Lsa Statement!", e);
                        }
                    }
                    if(invalidatedStmtId != null && updatedLsa != null) {
                        try {
                            LearnerStateHelper.generateCognitiveLsaStatement(statements, updatedLsa, updatedHistory, invalidatedStmtId,
                                    knowledgeSession, domainSessionId, domainId, actorSlug);
                        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                            throw new LmsXapiProcessorException("Unable to generate Replacement Predicted Cognitive Lsa Statement!", e);
                        }
                    }
                } else {
                    // Learner State Attribute
                    // Void the old Learner State Attribute
                    UUID voidedStmtId;
                    try {
                        voidedStmtId = LearnerStateHelper.voidCognitiveLsaStatement(statements, (LearnerStateAttribute) oKV.getValue(),
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Void Predicted Cognitive Lsa Statement!", e);
                    }
                    // Generate replacement from new
                    try {
                        LearnerStateHelper.generateCognitiveLsaStatement(statements, (LearnerStateAttribute) uKV.getValue(), voidedStmtId,
                                knowledgeSession, domainSessionId, domainId, actorSlug);
                    } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                        throw new LmsXapiProcessorException("Unable to generate Replacement Predicted Cognitive Lsa Statement!", e);
                    }
                }
            }
        }
    }
}
