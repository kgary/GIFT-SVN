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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
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
public class TaskDataTreeView extends Composite implements TaskDataView {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TaskDataTreeView.class.getName());
    
    /** The style name that should be applied to concepts that require the user to provide an observed assessment */
    private static final String OBSERVED_ASSESSMENT_STYLE_NAME = "barObservedAssessment";
    
    /** The style name that should be applied to nodes that are below expectation */
    private static final String BELOW_EXPECTATION_STYLE_NAME = "barBelowExpectation";

    private static TaskDataTreeViewUiBinder uiBinder = GWT.create(TaskDataTreeViewUiBinder.class);

    interface TaskDataTreeViewUiBinder extends UiBinder<Widget, TaskDataTreeView> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {

        String barData();
        
        String itemContainer();
    }
    
    /** An accessor for this widget's CSS styling rules */
    @UiField
    protected Style style;
    
    /** The tree used to display the hierarchy of concepts within the task */
    @UiField
    protected Tree tree;
    
    /** A button used to collapse the tree and switch to a list view */
    @UiField
    protected Button collapseButton;
    
    /** The root of the concept hierarchy, which represents the task itself */
    private TaskPerformanceItem taskItem;
    
    /** The provider of task assessment information used to populate this widget */
    private TaskDataProvider dataProvider;

    /**
     * Creates a new tree view that retrieves data about the current task state from the given provider
     * 
     * @param dataProvider a provider capable of retrieving task state data for this widget. Cannot be null.
     */
    public TaskDataTreeView(TaskDataProvider dataProvider) {
        
        if(dataProvider == null) {
            throw new IllegalArgumentException("The data provider cannot be null");
        }
        
        this.dataProvider = dataProvider;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        taskItem = new TaskPerformanceItem();
        tree.addItem(taskItem);
        
        //allow the user to collapse the tree by clicking on a button
        collapseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                TaskDataTreeView.this.dataProvider.setViewMode(ViewMode.LIST);
            }
        });
        
        redraw(false, false, true);
    }

    @Override
    public AssessmentSoundType redraw(boolean updateStateOnRedraw, boolean allowAlertSound, boolean assessmentChanged) {
        return taskItem.redraw(updateStateOnRedraw, allowAlertSound, assessmentChanged);
    }

    @Override
    public void refreshTimerWidgets() {
        taskItem.refreshTimerWidgets();
    }

    @Override
    public void updateStatusControlIconVisibility(boolean visible) {
        taskItem.getDataPanel().updateStatusControlIconVisibility(visible);
    }

    @Override
    public boolean applyConceptFilter() {
        return taskItem.applyConceptFilter();
    }
    
    /**
     * A tree item used to represent a task and display descriptive information about it
     * 
     * @author nroberts
     */
    private class TaskPerformanceItem extends TreeItem {
        
        /** The panel used to display descriptive information about the task */
        private TaskDescriptionPanel dataPanel;
        
        /** A widget used to display the task's assessment level in a bar to the side*/
        private AssessmentLevelBar barData;
        
        /** the data panels used to display concepts data for this task */
        private List<ConceptPerformanceItem> conceptPanels = new ArrayList<>();
        
        private FlowPanel container = new FlowPanel();
        
        /** The last displayed subconcept widget that needs an observer controller assessment */
        private PerformanceNodeDataDisplay lastOCNode;
        
        /**
         * Creates a new tree item to represent this widget's task
         */
        public TaskPerformanceItem() {
            
            if(dataProvider == null) {
                throw new IllegalArgumentException("The provider of data to the root task item cannot be null");
            }
            
            container.addStyleName(style.itemContainer());
            container.addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    dataPanel.setMetricsPanelVisible(true);
                }
            }, ClickEvent.getType());
            
            FlowPanel dataContainer = new FlowPanel();
            
            dataPanel = new TaskDescriptionPanel(dataProvider){
                @Override
                void setMetricsPanelVisible(boolean visible) {
                    super.setMetricsPanelVisible(visible);
                    
                    if(visible && !getState()) {
                        setState(true);
                    }
                }
            };
            dataPanel.setMinimized(true);
            dataContainer.add(dataPanel);
            
            dataContainer.getElement().getStyle().setProperty("flex", "1");
            container.add(dataContainer);
            
            barData = new AssessmentLevelBar(dataProvider);
            barData.addStyleName(style.barData());
            container.add(barData);
            
            setWidget(container);
            
            redraw(false, false, true);
            
            setState(true);
        }
        
        /**
         * Redraw this panel using the last state received.
         * 
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @param allowAlertSound flag determining whether the alert sounds are
         *        allowed. Even if this is true, other criteria might prevent the
         *        sound from being played.
         * @param assessmentChanged whether the assessment state has just changed
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public AssessmentSoundType redraw(boolean updateStateOnRedraw, boolean allowAlertSound, boolean assessmentChanged) {
            
            if(dataProvider.getCurrentState() == null){
                return null;
            }
            
            AssessmentSoundType requestedSoundType = null;
            
            final PerformanceStateAttribute stateAttr = dataProvider.getCurrentState().getState();
            
            dataPanel.redraw(assessmentChanged);
            barData.redraw();
            
            if (dataProvider.getCurrentState().isContainsObservedAssessmentCondition()
                    && AssessmentLevelEnum.UNKNOWN.equals(dataProvider.getCurrentState().getState().getShortTerm())) {
                /* If this concept requires an observed assessment and its current
                 * assessment level is unknown, change its styling so the user's
                 * attention is drawn toward it. */
                
                if(Dashboard.getInstance().getSettings().isHideOCAssessmentVisual()){
                    container.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
                }else{
                    container.addStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
                }

            } else {
                /* Otherwise, use the default styling for concepts */
                container.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }
            
            /* If the concept hasn't been activated yet, ignore it's current
             * state value */
            final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED.equals(stateAttr.getNodeStateEnum());
            if(!ignoreState && stateAttr.getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){
                // if the concept is below expectation change its styling so the user's attention is drawn toward it
                // Note: this takes higher precedence than the OBSERVED_ASSESSMENT_STYLE_NAME applied above.
                
                if(Dashboard.getInstance().getSettings().isHidePoorAssessmentVisual()){
                    container.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
                }else{
                    container.addStyleName(BELOW_EXPECTATION_STYLE_NAME);
                }
                
            } else {
                // use the default styling for the concepts
                container.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }
            
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
            List<ConceptPerformanceItem> existingConceptPanels = new ArrayList<>(conceptPanels);
            conceptPanels.clear();
            
            setDeepestLastOCNodeStyling(false); //remove the line after the current last node that needs an OC assessment
            
            AssessmentSoundType conceptRequestedSoundType = null;
            //get the panels corresponding to the concepts found in this state
            for(ConceptPerformanceState concept : dataProvider.getCurrentState().getConcepts()) {

                Integer conceptId = concept.getState().getNodeId();
                
                if(concept.getState().isScenarioSupportNode() 
                        && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
                    
                    //scenario support is hidden, so skip this concept
                    continue;
                }
                
                // find the previous version of the panel for this concept (if existed previously)
                Iterator<ConceptPerformanceItem> existingConceptPanelItr = existingConceptPanels.iterator();
                ConceptPerformanceItem conceptPanel = null;
                while(existingConceptPanelItr.hasNext()){
                    ConceptPerformanceItem existingConceptPanel = existingConceptPanelItr.next();
                    if(existingConceptPanel.getDataPanel().getCurrentState().getState().getNodeId() == conceptId){
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
                    conceptPanel = new ConceptPerformanceItem(concept);

                } else {
                    boolean stateChanged = false;
                    if (conceptPanel.getDataPanel().getCurrentState() != null) {
                        stateChanged = PerformanceStateAttributeDiff
                                .performDiff(conceptPanel.getDataPanel().getCurrentState().getState(), concept.getState(), false);
                    }

                    //need to always update intermediate concepts in case their childen have changed
                    if(concept instanceof IntermediateConceptPerformanceState || updateStateOnRedraw || stateChanged){
                        // update the existing concept panel using the new state
                        conceptRequestedSoundType = conceptPanel.update(concept, updateStateOnRedraw, assessmentChanged); 
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
            for(ConceptPerformanceItem panel : existingConceptPanels) {
                panel.remove();
            }

            //insert concepts' panels into the main panel
            List<ConceptPerformanceItem> sortedPanels = new ArrayList<>(conceptPanels);
            
            lastOCNode = Dashboard.getInstance().getSettings().sortByPriority(sortedPanels);
            
            setDeepestLastOCNodeStyling(true); //add a line after the new last node that needs an OC assessment

            int size = sortedPanels.size();
            for(int i = 0; i < size; i++) {

                ConceptPerformanceItem panel = sortedPanels.get(i);
                    
                TreeItem itemAtIndex = getChild(i);
                if(itemAtIndex == null) {
                    addItem(panel); //only add new data panels when the main panel does not yet contain them
                    
                } else if(!itemAtIndex.equals(panel)) {
                    insertItem(i, panel); //sort the existing data panel to its intended position
                }
            }
            
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Finished updating state for task: " + stateAttr.getName());
            }
            
            return requestedSoundType;
        }

        /**
         * Gets the panel used to display the task's descriptive information within this item
         * 
         * @return the data panel. Will not be null.
         */
        public TaskDescriptionPanel getDataPanel() {
            return dataPanel;
        }
        
        /**
         * Gets the tree items representing the concepts underneath the task
         * 
         * @return the concept tree items. Will not be null.
         */
        public List<ConceptPerformanceItem> getConcepts(){
            
            List<ConceptPerformanceItem> concepts = new ArrayList<ConceptPerformanceItem>();
            
            for(int i = 0; i < getChildCount(); i++) {
                concepts.add((ConceptPerformanceItem) getChild(i));
            }
            
            return concepts;
        }
        
        /**
         * Refreshes any widget that has a timestamp or something else that needs frequent updating.
         */
        public void refreshTimerWidgets() {
            
            dataPanel.refreshTimerWidgets();
            
            for(ConceptPerformanceItem child : getConcepts()) {
                child.refreshTimerWidgets();
            }
        }
        
        /**
         * Updates the visual state of the concept data shown by this item to match the provided filter
         * 
         * @return whether all of the task's concepts have been filtered out.
         */
        public boolean applyConceptFilter() {
            
            boolean childConceptShown = false;
            
            for (ConceptPerformanceItem concept : getConcepts()) {
                boolean showConcept = concept.applyConceptFilter();
                
                /* If the concept passes the filters, show it; otherwise hide it */
                if(showConcept) {
                    concept.getElement().getStyle().clearDisplay();
                    
                } else {
                    concept.getElement().getStyle().setDisplay(Display.NONE);
                }
                childConceptShown |= showConcept;
            }
            
            return childConceptShown;
        }
        
        @Override
        public void setState(boolean open, boolean fireEvents){
            super.setState(open, fireEvents);
            
            // when collapsing a node, collapse all descendants so that the user doesn't
            // have to collapse each descendant manually.  The goal is to have GIFT show the direct
            // descendants of a selected node automatically (done elsewhere) but not the rest
            // of the descendants if the selected node was previously manually collapsed. 
            if(!open){
                for(int index = 0; index < getChildCount(); index ++){
                    TreeItem child = getChild(index);
                    child.setState(open, fireEvents);
                }
            }
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
            ConceptPerformanceItem currOCNode = (ConceptPerformanceItem) lastOCNode;
            while(currOCNode.getLastOCNode() != null) {
                currOCNode = (ConceptPerformanceItem) currOCNode.getLastOCNode();
            }
            
            //add or remove the styling as specified
            if(mark) {
                currOCNode.getElement().getStyle().setProperty("borderBottom", "1px solid white");
            } else {
                currOCNode.getElement().getStyle().setProperty("borderBottom", "none");
            }
        }
        
        /**
         * Attempts to find a tree item representing the given performance node within this tree item and,
         * if such a tree item is found, scrolls that tree item into view so that the performance node's data
         * is visible
         * 
         * @param performanceNodePath the path to the performance node to scroll into view. Cannot be null.
         */
        public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
            
            if(performanceNodePath == null) {
                throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
            }
            
            for(ConceptPerformanceItem conceptPanel : getConcepts()) {
                
                if(conceptPanel.getCurrentState() != null 
                        && conceptPanel.getCurrentState().getState() != null
                        && conceptPanel.getCurrentState().getState().getNodeId() == performanceNodePath.getNodeId()) {
                    
                    if(performanceNodePath.getChild() == null) {
                        conceptPanel.getElement().scrollIntoView();
                        
                    } else {
                        conceptPanel.scrollNodeIntoView(performanceNodePath.getChild());
                    }
                    
                    return;
                }
            }
        }
    }

    /**
     * A tree item used to represent a task and display descriptive information about it
     * 
     * @author nroberts
     */
    private class ConceptPerformanceItem extends TreeItem implements PerformanceNodeDataProvider<ConceptPerformanceState>, 
            PerformanceNodeDataDisplay{
        
        /** The panel used to display descriptive information about the task */
        private LeafConceptDataPanel dataPanel;
        
        /** A widget used to display the task's assessment level in a bar to the side*/
        private AssessmentLevelBar barData;
        
        /** the data panels used to display concepts data for this task */
        private List<ConceptPerformanceItem> conceptPanels = new ArrayList<>();
        
        private FlowPanel container = new FlowPanel();
        
        /** The last displayed subconcept widget that needs an observer controller assessment */
        private PerformanceNodeDataDisplay lastOCNode;
        
        /**
         * Creates a new tree item to represent the concept that the given
         * performance state applies to
         * 
         * @param the concept performance state that this item should represent
         */
        public ConceptPerformanceItem(ConceptPerformanceState state) {
            
            if(state == null) {
                throw new IllegalArgumentException("The concept to create a performance tree item for cannot be null");
            }
            
            container.addStyleName(style.itemContainer());
            container.addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    dataPanel.setMetricsPanelVisible(true);
                }
            }, ClickEvent.getType());
            
            FlowPanel dataContainer = new FlowPanel();
            
            dataPanel = new LeafConceptDataPanel(state, dataProvider.getKnowledgeSession()) {
                @Override
                void setMetricsPanelVisible(boolean visible) {
                    super.setMetricsPanelVisible(visible);
                    
                    if(visible && !getState()) {
                        setState(true);
                    }
                }
            };
            dataContainer.add(dataPanel);
            
            dataContainer.getElement().getStyle().setProperty("flex", "1");
            container.add(dataContainer);
            
            barData = new AssessmentLevelBar(this);
            barData.addStyleName(style.barData());
            container.add(barData);
            
            setWidget(container);
            
            update(state, false, false);
            
            setState(true);
        }
        
        /**
         * Gets the tree items representing the concepts underneath this concept
         * 
         * @return the concept tree items. Will not be null.
         */
        public List<ConceptPerformanceItem> getConcepts(){
            
            List<ConceptPerformanceItem> concepts = new ArrayList<ConceptPerformanceItem>();
            
            for(int i = 0; i < getChildCount(); i++) {
                concepts.add((ConceptPerformanceItem) getChild(i));
            }
            
            return concepts;
        }
        
        /**
         * Gets the panel used to display the concept's descriptive information within this item
         * 
         * @return the data panel. Will not be null.
         */
        public LeafConceptDataPanel getDataPanel() {
            return dataPanel;
        }
        
        /**
         * Refreshes any widget that has a timestamp or something else that needs frequent updating.
         */
        public void refreshTimerWidgets() {
            
            dataPanel.refreshTimerWidgets();
            
            for(ConceptPerformanceItem child : getConcepts()) {
                child.refreshTimerWidgets();
            }
        }
        
        /**
         * Updates this panel's labels to reflect the attribute data provided by the given concept state
         * 
         * @param state the concept state from which to derive attribute data. Cannot be null.
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @param allowAlertSound flag determining whether the alert sounds are
         *        allowed. Even if this is true, other criteria might prevent the
         *        sound from being played.
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public AssessmentSoundType update(ConceptPerformanceState state, boolean updateStateOnRedraw, boolean allowAlertSound) {
            
            dataPanel.updateState(state);
            barData.redraw();
            
            return redrawSubConcepts(updateStateOnRedraw, allowAlertSound);
        }
        
        /**
         * Redraw this panel using the last state received.
         * 
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @param allowAlertSound flag determining whether the alert sounds are
         *        allowed. Even if this is true, other criteria might prevent the
         *        sound from being played.
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public AssessmentSoundType redraw(boolean updateStateOnRedraw, boolean allowAlertSound) {
            
            dataPanel.redraw(allowAlertSound);
            barData.redraw();
            
            return redrawSubConcepts(updateStateOnRedraw, allowAlertSound);
        }
        
        private AssessmentSoundType redrawSubConcepts(boolean updateStateOnRedraw, boolean allowAlertSound) {
            
            AssessmentSoundType requestedSoundType = null;
            
            refreshTimerWidgets();
            
            if (dataPanel.getCurrentState().isContainsObservedAssessmentCondition()
                    && AssessmentLevelEnum.UNKNOWN.equals(dataPanel.getCurrentState().getState().getShortTerm())) {
                /* If this concept requires an observed assessment and its current
                 * assessment level is unknown, change its styling so the user's
                 * attention is drawn toward it. */
                
                if(Dashboard.getInstance().getSettings().isHideOCAssessmentVisual()){
                    container.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
                }else{
                    container.addStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
                }

            } else {
                /* Otherwise, use the default styling for concepts */
                container.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }
            
            if(getCurrentState().getState().getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){
                // if the concept is below expectation change its styling so the user's attention is drawn toward it
                // Note: this takes higher precedence than the OBSERVED_ASSESSMENT_STYLE_NAME applied above.
                
                if(Dashboard.getInstance().getSettings().isHidePoorAssessmentVisual()){
                    container.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
                }else{
                    container.addStyleName(BELOW_EXPECTATION_STYLE_NAME);
                }
                
            } else {
                // use the default styling for the concepts
                container.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }

            //begin updating this task's concept panels to match the current state
            List<ConceptPerformanceItem> existingConceptPanels = new ArrayList<>(conceptPanels);
            conceptPanels.clear();
            
            AssessmentSoundType conceptRequestedSoundType = null;
            
            if(getCurrentState() instanceof IntermediateConceptPerformanceState) {
                
                //get the panels corresponding to the concepts found in this state
                for(ConceptPerformanceState concept : ((IntermediateConceptPerformanceState) getCurrentState()).getConcepts()) {
    
                    Integer conceptId = concept.getState().getNodeId();
                    
                    if(concept.getState().isScenarioSupportNode() 
                            && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
                        
                        // scenario support is hidden, so skip this concept
                        continue;
                    }
                    
                    // find the previous version of the panel for this concept (if existed previously)
                    Iterator<ConceptPerformanceItem> existingConceptPanelItr = existingConceptPanels.iterator();
                    ConceptPerformanceItem conceptPanel = null;
                    while(existingConceptPanelItr.hasNext()){
                        ConceptPerformanceItem existingConceptPanel = existingConceptPanelItr.next();
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
                        conceptPanel = new ConceptPerformanceItem(concept);
    
                    } else {
                        boolean stateChanged = false;
                        if (conceptPanel.getCurrentState() != null) {
                            stateChanged = PerformanceStateAttributeDiff
                                    .performDiff(conceptPanel.getDataPanel().getCurrentState().getState(), concept.getState(), false);
                        }
    
                        //need to always update intermediate concepts in case their children have changed
                        if(concept instanceof IntermediateConceptPerformanceState || updateStateOnRedraw || stateChanged){
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
            }
            
            //remove the panels for any concepts that are not found in the new state
            for(ConceptPerformanceItem panel : existingConceptPanels) {
                panel.remove();
            }

            //insert concepts' panels into the main panel
            List<ConceptPerformanceItem> sortedPanels = new ArrayList<>(conceptPanels);
            lastOCNode = Dashboard.getInstance().getSettings().sortByPriority(sortedPanels);
            
            int size = sortedPanels.size();
            for(int i = 0; i < size; i++) {

                ConceptPerformanceItem panel = sortedPanels.get(i);
                    
                TreeItem itemAtIndex = getChild(i);
                if(itemAtIndex == null) {
                    addItem(panel); //only add new data panels when the main panel does not yet contain them
                    
                } else if(!itemAtIndex.equals(panel)) {
                    insertItem(i, panel); //sort the existing data panel to its intended position
                }
            }
            
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Finished updating state for concept: " + getCurrentState().getState().getName());
            }
            
            return requestedSoundType;
        }
        
        @Override
        public boolean applyConceptFilter() {
            
            boolean childConceptShown = false;
            
            List<ConceptPerformanceItem> concepts = getConcepts();
            if(concepts.isEmpty()) {
                
                boolean showConcept = !Dashboard.getInstance().getSettings().isShowOcOnly()
                        || requiresObservedAssessment();
                
                /* If the concept passes the filters, show it; otherwise hide */
                if(showConcept) {
                    getElement().getStyle().clearDisplay();
                    
                } else {
                    getElement().getStyle().setDisplay(Display.NONE);
                }
                childConceptShown |= showConcept;
                
            } else {
                
                //this concept has subconcepts, so apply the filter to them
                for (ConceptPerformanceItem concept : concepts) {
                    boolean showConcept = concept.applyConceptFilter();
                    
                    /* If the concept passes the filters, show it; otherwise hide */
                    if(showConcept) {
                        concept.getElement().getStyle().clearDisplay();
                        
                    } else {
                        concept.getElement().getStyle().setDisplay(Display.NONE);
                    }
                    childConceptShown |= showConcept;
                }
            }
            
            return childConceptShown;
        }

        @Override
        public AbstractKnowledgeSession getKnowledgeSession() {
            return dataProvider.getKnowledgeSession();
        }

        @Override
        public ConceptPerformanceState getCurrentState() {
            return dataPanel.getCurrentState();
        }
        
        @Override
        public boolean requiresObservedAssessment() {
            
            boolean requiresAssessment = getCurrentState() != null 
                    && getCurrentState().isContainsObservedAssessmentCondition();
            
            if(requiresAssessment) {
                return true;
            }
            
            for(ConceptPerformanceItem concept : conceptPanels) {
                if(concept.requiresObservedAssessment()) {
                    return true;
                }
            }
            
            return false;
        }
        
        @Override
        public void setState(boolean open, boolean fireEvents){
            super.setState(open, fireEvents);
            
            // when collapsing a node, collapse all descendants so that the user doesn't
            // have to collapse each descendant manually.  The goal is to have GIFT show the direct
            // descendants of a selected node automatically (done elsewhere) but not the rest
            // of the descendants if the selected node was previously manually collapsed. 
            if(!open){
                for(int index = 0; index < getChildCount(); index ++){
                    TreeItem child = getChild(index);
                    child.setState(open, fireEvents);
                }
            }
        }

        /**
         * Gets the last displayed subconcept widget that needs an observer controller assessment.
         * 
         * @return the last widget that needs an observer controller assessment. Can be null.
         */
        public PerformanceNodeDataDisplay getLastOCNode() {
            return lastOCNode;
        }
        
        /**
         * Attempts to find a tree item representing the given performance node within this tree item and,
         * if such a tree item is found, scrolls that tree item into view so that the performance node's data
         * is visible
         * 
         * @param performanceNodePath the path to the performance node to scroll into view. Cannot be null.
         */
        public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
            
            if(performanceNodePath == null) {
                throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
            }
            
            for(ConceptPerformanceItem conceptPanel : getConcepts()) {
                
                if(conceptPanel.getCurrentState() != null 
                        && conceptPanel.getCurrentState().getState() != null
                        && conceptPanel.getCurrentState().getState().getNodeId() == performanceNodePath.getNodeId()) {
                    
                    if(performanceNodePath.getChild() == null) {
                        conceptPanel.getElement().scrollIntoView();
                        
                    } else {
                        conceptPanel.scrollNodeIntoView(performanceNodePath.getChild());
                    }
                    
                    return;
                }
            }
        }
    }

    @Override
    public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
        
        if(performanceNodePath == null) {
            throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
        }
        
        taskItem.scrollNodeIntoView(performanceNodePath);
    }
}
