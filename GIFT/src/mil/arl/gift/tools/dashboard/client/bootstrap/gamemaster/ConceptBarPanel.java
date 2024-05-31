/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;

/**
 * A panel used to display a concept's performance information in a bar-graph-like format
 * 
 * @author nroberts
 */
public class ConceptBarPanel extends Composite implements PerformanceNodeDataProvider<ConceptPerformanceState>, PerformanceNodeDataDisplay {

    private static ConceptBarPanelUiBinder uiBinder = GWT.create(ConceptBarPanelUiBinder.class);
    
    /** The style name that should be applied to concepts that require the user to provide an observed assessment */
    private static final String OBSERVED_ASSESSMENT_STYLE_NAME = "barObservedAssessment";
    
    /** The style name that should be applied to nodes that are below expectation */
    private static final String BELOW_EXPECTATION_STYLE_NAME = "barBelowExpectation";
    
    /** The label used to display the concept's name*/
    @UiField
    protected Label label = new Label();
    
    /** A widget used to display the concept's assessment level in a bar */
    @UiField(provided=true)
    protected AssessmentLevelBar assessmentBar;
    
    /** The panel used to display descriptive information about the task */
    private LeafConceptDataPanel dataPanel;

    interface ConceptBarPanelUiBinder extends UiBinder<Widget, ConceptBarPanel> {
    }
    
    /** A knowledge session that this bar's concept is a part of */
    private AbstractKnowledgeSession knowledgeSession;

    /**
     * Creates a new concept bar with the given initial state and the given task data
     * 
     * @param state the initial state of the concept. Cannot be null.
     * @param dataProvider ad ata provider used to fetch information about the task 
     * that this bar's concept is a part of. Cannot be null.
     */
    public ConceptBarPanel(ConceptPerformanceState state, AbstractKnowledgeSession knowledgeSession) {
        
        if(state == null) {
            throw new IllegalArgumentException("The concept to create a bar panel for cannot be null");
        }
        
        if(knowledgeSession == null) {
            throw new IllegalArgumentException("The data provider for a bar panel cannot be null");
        }
        
        this.knowledgeSession = knowledgeSession;
        
        dataPanel = new LeafConceptDataPanel(state, knowledgeSession) {
            
            @Override
            public void setMetricsPanelVisible(boolean visible) {
                
                //keep the data panel expanded and show/hide the dialog containing it instead
                if(visible){
                    super.setMetricsPanelVisible(visible);
                    
                } else {
                    DataDialog.hideInstance();
                }
            }
        };
        dataPanel.setMetricsPanelVisible(true);
        
        assessmentBar = new AssessmentLevelBar(this);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        label.setText(state.getState().getName());
        
        addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                DataDialog.showAtCurrentMousePosition(ConceptBarPanel.this, event);
            }
        }, ClickEvent.getType());
        
        update(state, false, false);
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
        
        AssessmentSoundType toReturn = dataPanel.updateState(state);
        
        redraw(updateStateOnRedraw, allowAlertSound);
        
        return toReturn;
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
        
        AssessmentSoundType toReturn = dataPanel.redraw(allowAlertSound);
        assessmentBar.redraw();
        
        if (dataPanel.getCurrentState().isContainsObservedAssessmentCondition()
                && AssessmentLevelEnum.UNKNOWN.equals(dataPanel.getCurrentState().getState().getShortTerm())) {
            /* If this concept requires an observed assessment and its current
             * assessment level is unknown, change its styling so the user's
             * attention is drawn toward it. */
            
            if(Dashboard.getInstance().getSettings().isHideOCAssessmentVisual()){
                removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }else{
                addStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }

        } else {
            /* Otherwise, use the default styling for concepts */
            removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
        }
        
        /* If the concept hasn't been activated yet, ignore it's current state
         * value */
        final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED
                .equals(dataPanel.getCurrentState().getState().getNodeStateEnum());
        if(!ignoreState && dataPanel.getCurrentState().getState().getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){
            // if the concept is below expectation change its styling so the user's attention is drawn toward it
            // Note: this takes higher precedence than the OBSERVED_ASSESSMENT_STYLE_NAME applied above.
            
            if(Dashboard.getInstance().getSettings().isHidePoorAssessmentVisual()){
                removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }else{
                addStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }
            
        } else {
            // use the default styling for the concepts
            removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
        }
        
        return toReturn;
    }

    @Override
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    @Override
    public ConceptPerformanceState getCurrentState() {
        return dataPanel.getCurrentState();
    }
    
    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        dataPanel.refreshTimerWidgets();
    }

    @Override
    public boolean applyConceptFilter() {
        final boolean showConcept = !Dashboard.getInstance().getSettings().isShowOcOnly()
                || requiresObservedAssessment();

        setVisible(showConcept);
        return showConcept;
    }

    @Override
    public boolean requiresObservedAssessment() {
        return getCurrentState() != null 
                && getCurrentState().isContainsObservedAssessmentCondition();
    }
    
    /**
     * A dialog used to display concept bars' associated metrics in a floating panel
     * 
     * @author nroberts
     */
    public static class DataDialog extends PopupPanel{
        
        /** The singleton instance of this dialog */
        private static DataDialog instance;
        
        /**
         * Creates a new dialog for display concept bar data
         */
        private DataDialog() {
            setAutoHideEnabled(true);
            
            getElement().getStyle().setProperty("border", "none");
            getElement().getStyle().setProperty("padding", "0px");
            getElement().getStyle().setProperty("borderRadius", "5px");
            getElement().getStyle().setProperty("boxShadow", "3px 3px 5px rgba(0,0,0,0.3)");
        }
        
        @Override
        public void show(){ 
            
            super.show();
            
            // we need to try to keep the dialog from going off the page
            int top = getPopupTop();
            int left = getPopupLeft();
            
            int maxHeight = Window.getClientHeight() - top - 1;
            int maxWidth = Window.getClientWidth() - left;      
            
            if(getOffsetHeight() > maxHeight){
                
                int displacement = maxHeight - getOffsetHeight();
                setPopupPosition(left, top + displacement);
            }
            
            if(getOffsetWidth() > maxWidth){
                
                int displacement = maxWidth - getOffsetWidth();
                setPopupPosition(left + displacement, top);
            }
        }
        
        /**
         * Shows the given concept bar's metrics data next to the mouse's current location
         * 
         * @param bar the bar whose metrics data is being displayed. Cannot be null.
         * @param event the mouse event (i.e. click, enter, exit) from which to get the current mouse position
         */
        public static void showAtCurrentMousePosition(ConceptBarPanel bar, MouseEvent<?> event){
            
            if(bar == null) {
                throw new IllegalArgumentException("The concept bar whose data is being displayed cannot be null");
            }
            
            bar.dataPanel.setMetricsPanelVisible(true, true);
            
            if(instance == null) {
                instance = new DataDialog();
            }
            
            instance.setWidget(bar.dataPanel);
            
            instance.setPopupPosition(event.getClientX(), event.getClientY());
            instance.show();
        }
        
        /**
         * Hides the singleton instance of this dialog
         */
        public static void hideInstance() {
            instance.hide();
        }
    }
}
