/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TaskDataPanel.ViewMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;

/**
 * A {@link TaskDataView} that displays leaf concept data from a task's current state as a flat list. This effectively
 * hides intermediate concepts from the user, which can help simplify the state data observed by the user.
 * 
 * @author nroberts
 */
public class TaskDataListView extends Composite implements TaskDataView {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TaskDataListView.class.getName());

    private static TaskDataListViewUiBinder uiBinder = GWT.create(TaskDataListViewUiBinder.class);

    interface TaskDataListViewUiBinder extends UiBinder<Widget, TaskDataListView> {
    }
    
    /** the data panels used to display concepts data for this task */
    private List<RootConceptDataPanel> conceptPanels = new ArrayList<>();

    /** The base panel of this widget that contains its content */
    @UiField
    protected FlowPanel basePanel;

    /** The main panel that child concept data panels should be added to */
    @UiField
    protected FlowPanel mainPanel;

    /** The panel containing the description of the task's current state */
    @UiField(provided=true)
    protected TaskDescriptionPanel taskDescriptionPanel;
    
    /** The button used to expand the view displaying intermediate concepts */
    @UiField
    protected Button intermediateConceptsButton;

    /** The provider of task assessment information used to populate this widget */
    private TaskDataProvider dataProvider;

    /** The last displayed subconcept widget that needs an observer controller assessment */
    private PerformanceNodeDataDisplay lastOCNode;

    /**
     * Creates a new list view that retrieves data about the current task state from the given provider
     * 
     * @param dataProvider a provider capable of retrieving task state data for this widget. Cannot be null.
     */
    public TaskDataListView(final TaskDataProvider dataProvider) {
        
        if(dataProvider == null) {
            throw new IllegalArgumentException("The data provider cannot be null");
        }
        
        this.dataProvider = dataProvider;
        
        taskDescriptionPanel = new TaskDescriptionPanel(dataProvider);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        //switch to a tree view if the user decides to expand this concept to view its intermediate concepts
        intermediateConceptsButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                dataProvider.setViewMode(ViewMode.TREE);
            }
        });
        
        redraw(false, false, true);
    }
    
    @Override
    public AssessmentSoundType redraw(final boolean updateStateOnRedraw, boolean allowAlertSound, boolean assessmentChanged){
        
        if(dataProvider.getCurrentState() == null){
            return null;
        }
        
        AssessmentSoundType requestedSoundType = null;
        
        final PerformanceStateAttribute stateAttr = dataProvider.getCurrentState().getState();
        
        taskDescriptionPanel.redraw(assessmentChanged);
        
        refreshTimerWidgets();

        /*- Determine if an alert sound should be played by meeting ALL these criteria:
         * 1. Alerts are allowed for this redraw method call
         * 2. The task is active 
         * 3. The assessment didn't come from this client's user during ACTIVE session
         *    (i.e. don't play sound right after user provides an assessment)
         * 4. It is not a support node or support nodes are visible
         */
        if (allowAlertSound) {
            final boolean isThisUsersEvaluation = StringUtils.equalsIgnoreCase(stateAttr.getEvaluator(),
                    UiManager.getInstance().getUserName());
            final boolean otherClientOrPlaybackMode = !isThisUsersEvaluation || PermissionsProvider.getInstance()
                    .getCurrentMode() == PermissionsProvider.Mode.PAST_SESSION_PLAYBACK;
            final boolean isTaskActive = stateAttr.getNodeStateEnum().equals(PerformanceNodeStateEnum.ACTIVE);
            final boolean passesSupportNodeFilter = !stateAttr.isScenarioSupportNode()
                    || Dashboard.getInstance().getSettings().isShowScenarioSupport();

            allowAlertSound &= isTaskActive && otherClientOrPlaybackMode && passesSupportNodeFilter;
        }

        if(stateAttr.getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){            
            
            /* For below expectation, verify the poor assessment sound is not
             * muted */ 
            if (allowAlertSound && !Dashboard.VolumeSettings.POOR_ASSESSMENT_SOUND.getSetting().isMuted()) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("requesting play beep for " + stateAttr.getName() + " task poor perf");
                }

                requestedSoundType = AssessmentSoundType.POOR_ASSESSMENT;
            }
        } else {

            /* Only play the good assessment sound if At or Above expectation
             * and verify the good assessment sound is not muted */
            if (allowAlertSound && !Dashboard.VolumeSettings.GOOD_ASSESSMENT_SOUND.getSetting().isMuted()
                    && (stateAttr.getShortTerm().equals(AssessmentLevelEnum.AT_EXPECTATION)
                            || stateAttr.getShortTerm().equals(AssessmentLevelEnum.ABOVE_EXPECTATION))) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("requesting play beep for " + stateAttr.getName() + " task good perf");
                }

                requestedSoundType = AssessmentSoundType.GOOD_ASSESSMENT;
            }
        }

        //begin updating this task's concept panels to match the current state
        List<RootConceptDataPanel> existingConceptPanels = new ArrayList<>(conceptPanels);
        conceptPanels.clear();
        
        setDeepestLastOCNodeStyling(false); //remove the line after the current last node that needs an OC assessment
        
        AssessmentSoundType conceptRequestedSoundType = null;
        boolean hasIntermedConcept = false;
        //get the panels corresponding to the concepts found in this state
        for(ConceptPerformanceState concept : dataProvider.getCurrentState().getConcepts()) {
            
            if(!hasIntermedConcept && concept instanceof IntermediateConceptPerformanceState) {
                hasIntermedConcept = true;
            }

            Integer conceptId = concept.getState().getNodeId();
                        
            if(concept.getState().isScenarioSupportNode() 
                    && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
                
                //scenario support is hidden, so skip this concept
                continue;
            }
            
            // find the previous version of the panel for this concept (if existed previously)
            Iterator<RootConceptDataPanel> existingConceptPanelItr = existingConceptPanels.iterator();
            RootConceptDataPanel conceptPanel = null;
            while(existingConceptPanelItr.hasNext()){
                RootConceptDataPanel existingConceptPanel = existingConceptPanelItr.next();
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
                conceptPanel = new RootConceptDataPanel(concept, dataProvider.getKnowledgeSession());

            } else {                
                boolean stateChanged = false;
                if (conceptPanel.getCurrentState() != null) {
                    stateChanged = PerformanceStateAttributeDiff
                            .performDiff(conceptPanel.getCurrentState().getState(), concept.getState(), false);
                }

                if(concept instanceof IntermediateConceptPerformanceState || updateStateOnRedraw || stateChanged){
                    // update the existing concept panel using the new state
                    conceptRequestedSoundType = conceptPanel.updateState(concept); 
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
        for(RootConceptDataPanel panel : existingConceptPanels) {
            
            mainPanel.remove(panel); //remove root panel used when not sorting by OC assessments
            
            for(ConceptBarPanel subPanel: panel.getLeafPanels()) {
                mainPanel.remove(subPanel); //remove leaf panels used when not sorting by OC assessments
            }
        }

        //insert concepts' panels into the main panel
        List<PerformanceNodeDataDisplay> sortedPanels = new ArrayList<>();
        
        boolean prioritizeOCAssessment = Dashboard.getInstance().getSettings().isPrioritizeOCAssessment();
        for(RootConceptDataPanel rootPanel : conceptPanels) {
            
            if(!prioritizeOCAssessment) {
                
                //if concepts needing OC assessments should not appear first, group the leaf concepts by their root concept
                sortedPanels.add(rootPanel);
                
            } else {
            
                //if concepts needingOC assessments should appear first, just dusplay the leaf concepts
                for(ConceptBarPanel leafPanel : rootPanel.getLeafPanels()) {
                    sortedPanels.add(leafPanel);
                }
            }
        }
        
        lastOCNode = Dashboard.getInstance().getSettings().sortByPriority(sortedPanels);
        
        setDeepestLastOCNodeStyling(true); //add a line after the new last node that needs an OC assessment

        int size = sortedPanels.size();
        for(int i = 0; i < size; i++) {

            Widget panel = (Widget) sortedPanels.get(i);
                
            Widget itemAtIndex = i < mainPanel.getWidgetCount() ? mainPanel.getWidget(i) : null;
            if(itemAtIndex == null) {
                mainPanel.add(panel); //only add new data panels when the main panel does not yet contain them
                
            } else if(!itemAtIndex.equals(panel)) {
                mainPanel.insert(panel, i); //sort the existing data panel to its intended position
            }
        }
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Finished updating state for task: " + stateAttr.getName());
        }
        
        return requestedSoundType;
    }

    @Override
    public void refreshTimerWidgets() {
        taskDescriptionPanel.refreshTimerWidgets();
    }

    @Override
    public void updateStatusControlIconVisibility(boolean visible) {
        taskDescriptionPanel.updateStatusControlIconVisibility(visible);
    }
    
    /**
     * Return the task's concept panels.
     * 
     * @return the unmodifiable collection of concept data panels.
     */
    public List<RootConceptDataPanel> getConceptDataPanels() {
        return Collections.unmodifiableList(conceptPanels);
    }

    @Override
    public boolean applyConceptFilter() {
        
        boolean childConceptShown = false;

        for (RootConceptDataPanel conceptDataPanel : getConceptDataPanels()) {
            /* If the concept passes the filters, show it; otherwise hide it */
            boolean showConcept = conceptDataPanel.applyConceptFilter();
            childConceptShown |= showConcept;
        }
        
        return childConceptShown;
    }
    
    /**
     * Sets whether or not special styling should be applied to the last visible
     * widget that represents a concept node that requires an OC assessment
     * 
     * @param mark whether the special styling should be added or removed.
     */
    private void setDeepestLastOCNodeStyling(boolean mark) {
        
        if(lastOCNode == null) {
            return;
        }
        
        /* look inside the last OC node to see if it contains any deeper OC nodes,
         * then select the last node among them */
        Widget currOCNode = (Widget) lastOCNode;
        
        //add or remove the styling as specified
        if(mark) {
            currOCNode.getElement().getStyle().setProperty("borderRight", "1px solid white");
        } else {
            currOCNode.getElement().getStyle().setProperty("borderRight", "none");
        }
    }

    @Override
    public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
        
        if(performanceNodePath == null) {
            throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
        }
        
        for(RootConceptDataPanel conceptPanel : conceptPanels) {
            
            if(conceptPanel.getCurrentState() != null 
                    && conceptPanel.getCurrentState().getState() != null
                    && conceptPanel.getCurrentState().getState().getNodeId() == performanceNodePath.getNodeId()) {
                
                conceptPanel.scrollNodeIntoView(performanceNodePath.getPathEnd());
                return;
            }
        }
    }
}
