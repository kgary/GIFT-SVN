/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;

/**
 * A panel capable of displaying {@link ConceptPerformanceState a ROOT concept's performance state data} and updating it visually in real-time
 * as new data is provided. In this case, a root concept is any concept that is the immediate child of a task. If the concept is an intermediate
 * concept, then all the leaf concepts inside of it will be shown.
 *
 * @author nroberts
 */
public class RootConceptDataPanel extends Composite implements PerformanceNodeDataDisplay {

    private static RootConceptDataPanelUiBinder uiBinder = GWT.create(RootConceptDataPanelUiBinder.class);

    interface RootConceptDataPanelUiBinder extends UiBinder<Widget, RootConceptDataPanel> {
    }
    
    /** The panel used to display the root concept's lead concepts */
    @UiField
    protected FlowPanel leafConceptPanel;
    
    /** the data panels used to display leaf concept data for this root concept */
    private List<ConceptBarPanel> conceptPanels = new ArrayList<>();
    
    /** The current knowledge session */
    private AbstractKnowledgeSession knowledgeSession;
    
    /** The concept state data currently being represented by this panel */
    private ConceptPerformanceState currentState;

    /**
     * Creates a new panel displaying the given state data for a root concept and any child concepts it may or
     * may not have
     *
     * @param state the state data for the concept to represent
     * @param knowledgeSession the current knowledge session. Can't be null.
     */
    public RootConceptDataPanel(ConceptPerformanceState state, AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }
        
        this.knowledgeSession = knowledgeSession;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        updateState(state);
    }
    
    /**
     * Gets the task state data that this panel is currently displaying
     *
     * @return the current task state
     */
    public ConceptPerformanceState getCurrentState() {
        return currentState;
    }
    
    /**
     * Updates this panel's labels to reflect the attribute data provided by the given concept state. If
     * this root concept has any subconcepts, then those will also be updated.
     *
     * @param state the concept state from which to derive attribute data
     * @return an enumerated type of sound that needs to be played for this concept and any descendant
     * concepts where {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     * return null;
     */
    public AssessmentSoundType updateState(ConceptPerformanceState state) {
        final ConceptPerformanceState oldState = getCurrentState();
        boolean firstState = oldState == null;
        boolean newState = oldState == null || oldState.getState().getShortTermTimestamp() != state.getState().getShortTermTimestamp();
        this.currentState = state;

        /* Update the assessment source if a new evaluator is provided or if the
         * short term assessment has changed */
        final boolean assessmentChanged = oldState == null || oldState.getState() == null
                || oldState.getState().getShortTerm() == null
                || !oldState.getState().getShortTerm().equals(state.getState().getShortTerm());

        refreshTimerWidgets();

     // allow alerts such as poor performance sounds to be played
        //... unless this is the first time the task state was provided or the same task state that has already been provided
        //    basically don't want to keep replaying alerts when the user is just opening the assessment panel where this task is drawn
        final boolean updateState = !firstState && newState;
        final boolean allowAlertSound = updateState && assessmentChanged;
        return redraw(updateState, allowAlertSound);
    }
    
    /**
     * Redraw this panel using the last state received.
     * 
     * @param updateStateOnRedraw flag determining whether to update the concept
     *        states with the current state.
     * @param allowAlertSound flag determining whether the alert sounds are
     *        allowed. Even if this is true, other criteria might prevent the
     *        sound from being played.
     * @return an enumerated type of sound that needs to be played for this root concept
     *         and any descendant concepts where
     *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     *         return null;
     */
    public AssessmentSoundType redraw(final boolean updateStateOnRedraw, boolean allowAlertSound){
        
        if(currentState == null){
            return null;
        }
        
        AssessmentSoundType requestedSoundType = null;
        
        //begin updating this root concept's leaf concept panels to match the current state
        List<ConceptBarPanel> existingConceptPanels = new ArrayList<>(conceptPanels);
        conceptPanels.clear();
        
        AssessmentSoundType conceptRequestedSoundType = null;
        //get the panels corresponding to the leaf concepts found in this state
        for(ConceptPerformanceState concept : getLeafConcepts(currentState)) {
            
            Integer conceptId = concept.getState().getNodeId();
            
            if(concept.getState().isScenarioSupportNode() 
                    && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
                
                //scenario support is hidden, so skip this concept
                continue;
            }
            
            // find the previous version of the panel for this concept (if existed previously)
            Iterator<ConceptBarPanel> existingConceptPanelItr = existingConceptPanels.iterator();
            ConceptBarPanel conceptPanel = null;
            while(existingConceptPanelItr.hasNext()){
                ConceptBarPanel existingConceptPanel = existingConceptPanelItr.next();
                if(existingConceptPanel.getCurrentState().getState().getNodeId() == conceptId){
                    conceptPanel = existingConceptPanel;
                    existingConceptPanelItr.remove();
                    break;
                }
            }
            
            boolean isPoorPerforming = concept.getState().getShortTerm().isPoorPerforming();
            boolean isIntermediateConcept = concept instanceof IntermediateConceptPerformanceState;
            boolean hasPreviousState = conceptPanel != null;
            if(Dashboard.getInstance().getSettings().isHideGoodAutoAssessments() &&
                    !hasPreviousState &&
                    !concept.isContainsObservedAssessmentCondition() && 
                    !isPoorPerforming &&
                    !isIntermediateConcept){
                // good automated assessments should be hidden unless:
                // 1. the concept was shown before with a poor performance, i.e. if before below expectation was shown and then At expectation is skipped the concept will show Below forever
                // 2. the concept is an intermediate concept and has children that may need to be drawn because they are poor performing (but the roll up results in good performing - maybe possible)
                //    The logic below will remove this intermediate concept if no descendants are drawn.
                continue;
            }

            if(conceptPanel == null) {

                //a panel does not yet exist for this concept, so create one
                conceptPanel = new ConceptBarPanel(concept, knowledgeSession);

            } else {
                boolean stateChanged = false;
                if (conceptPanel.getCurrentState() != null) {
                    stateChanged = PerformanceStateAttributeDiff
                            .performDiff(conceptPanel.getCurrentState().getState(), concept.getState(), false);
                }

                if(updateStateOnRedraw || stateChanged){
                    // update the existing concept panel using the new state
                    conceptRequestedSoundType = conceptPanel.update(concept, updateStateOnRedraw, allowAlertSound); 
                }else{
                    // just redraw using the existing state
                    conceptRequestedSoundType = conceptPanel.redraw(false, false);
                }
                    
                if(AssessmentSoundType.isHigherPriority(conceptRequestedSoundType, requestedSoundType)){
                    // the concept requested sound type is higher priority for this redraw than the current one set for this redraw 
                    requestedSoundType = conceptRequestedSoundType;
                }
            }

            conceptPanels.add(conceptPanel);
        }
        
        //remove the panels for any concepts that are not found in the new state
        for(ConceptBarPanel panel : existingConceptPanels) {
            leafConceptPanel.remove(panel);
        }
        
        //insert concepts' panels into the main panel
        List<ConceptBarPanel> sortedPanels = new ArrayList<>(conceptPanels);
        Dashboard.getInstance().getSettings().sortByPriority(sortedPanels);

        int size = sortedPanels.size();
        for(int i = 0; i < size; i++) {

            ConceptBarPanel panel = sortedPanels.get(i);
                
            Widget itemAtIndex = i < leafConceptPanel.getWidgetCount() ? leafConceptPanel.getWidget(i) : null;
            if(itemAtIndex == null) {
                leafConceptPanel.add(panel); //only add new data panels when the main panel does not yet contain them
                
            } else if(!itemAtIndex.equals(panel)) {
                leafConceptPanel.insert(panel, i); //sort the existing data panel to its intended position
            }
        }
        
        return requestedSoundType;
    }
    
    /**
     * Gets all of the leaf concept performance states within the given root concept performance state. This
     * effectively retrieves all of the subconcepts within the given concept that do not have any children. 
     * Note that the result can contain the given concept itself, if it has no such subconcepts.
     * 
     * @param state the performance state of the concept whose leaf concepts are being retrieved. Cannot be null.
     * @return the lead concepts within the given concept. Will not be null. Can contain the provided concept 
     * if it has no other leaf concepts.
     */
    private List<ConceptPerformanceState> getLeafConcepts(ConceptPerformanceState state) {
        
        List<ConceptPerformanceState> leafConcepts = new ArrayList<ConceptPerformanceState>();
        
        if(state instanceof IntermediateConceptPerformanceState) {
            
            for(ConceptPerformanceState subConcept : ((IntermediateConceptPerformanceState) state).getConcepts()){
                leafConcepts.addAll(getLeafConcepts(subConcept));
            }
            
        } else {
            leafConcepts.add(state);
        }
        
        return leafConcepts;
    }

    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        for(ConceptBarPanel conceptPanel : conceptPanels) {
            conceptPanel.refreshTimerWidgets();
        }
    }

    @Override
    public boolean applyConceptFilter() {
        for (ConceptBarPanel conceptPanel : conceptPanels) {
            conceptPanel.applyConceptFilter();
        }

        final boolean showConcept = !Dashboard.getInstance().getSettings().isShowOcOnly()
                || requiresObservedAssessment();
        setVisible(showConcept);
        return showConcept;
    }
    
    @Override
    public boolean requiresObservedAssessment() {
        
        boolean requiresAssessment = getCurrentState() != null 
                && getCurrentState().isContainsObservedAssessmentCondition();
        
        if(requiresAssessment) {
            return true;
        }
        
        for(ConceptBarPanel concept : conceptPanels) {
            if(concept.requiresObservedAssessment()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the leaf concepts that this panel is displaying. This can be used to display said leaf
     * concepts in other ares while still allowing this panel to push data to them.
     * 
     * @return the leaf panels. Will not be null.
     */
    public List<ConceptBarPanel> getLeafPanels(){
        return conceptPanels;
        
    }

    /**
     * Attempts to find a widget representing the given performance node within this widget and,
     * if such a widget is found, scrolls that widget into view so that the performance node's data
     * is visible
     * 
     * @param performanceNodePath the path to the performance node to scroll into view. Cannot be null.
     */
    public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
        
        if(performanceNodePath == null) {
            throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
        }
        
        for(ConceptBarPanel conceptPanel : conceptPanels) {
            
            if(conceptPanel.getCurrentState() != null 
                    && conceptPanel.getCurrentState().getState() != null
                    && conceptPanel.getCurrentState().getState().getNodeId() == performanceNodePath.getNodeId()) {
                
                conceptPanel.getElement().scrollIntoView();
                return;
            }
        }
    }
}
