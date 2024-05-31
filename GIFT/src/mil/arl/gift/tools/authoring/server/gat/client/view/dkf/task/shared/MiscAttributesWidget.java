/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Concept;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The widget that displays the optional misc attributes for tasks and concepts.
 * @author mhoffman
 *
 */
public class MiscAttributesWidget extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(MiscAttributesWidget.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static MiscAttributesWidgetUiBinder uiBinder = GWT.create(MiscAttributesWidgetUiBinder.class);
    
    /** used to indicate if a task/concept is meant for scenario support */
    @UiField
    protected CheckBoxButton scenarioSupportButton;
    
    /** The collapse that contains the body of the widget */
    @UiField
    protected Collapse collapse;
    
    /** the header for this widget */
    @UiField
    protected PanelHeader panelHeader;
    
    /** the panel that contains the task difficulty widgets, used to easy hide the widgets when editing a concept */
    @UiField
    FlowPanel difficultyPanel;
    
    /** the panel that contains the task stress widgets, used to easy hide the widgets when editing a concept */
    @UiField
    FlowPanel stressPanel;
    
    /** The slider used to author stress value */
    @UiField
    Slider stressSlider;
    
    /**
     * A button used to set the task difficulty to easy
     */
    @UiField
    Button easyDifficultyButton;

    /**
     * A button used to set the task difficulty to medium
     */
    @UiField
    Button mediumDifficultyButton;

    /**
     * A button used to set the task difficulty to hard
     */
    @UiField
    Button hardDifficultyButton;
    
    /** the current task/concept data model being changed on this widget */
    private Serializable currentTaskOrConcept;
    
    /**
     * Default constructor
     */
    public MiscAttributesWidget(){
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        panelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (collapse.isShown()) {
                    collapse.hide();
                } else {
                    collapse.show();
                }
            }
        }, ClickEvent.getType());
        
        scenarioSupportButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentTaskOrConcept == null){
                    return;
                }
                
                boolean enabled = event.getValue();
                scenarioSupportButton.setActive(enabled);
                
                if(currentTaskOrConcept instanceof Concept){
                    Concept concept = (Concept)currentTaskOrConcept;
                    concept.setScenarioSupport(enabled);
                }else{
                    Task task = (Task)currentTaskOrConcept;
                    task.setScenarioSupport(enabled);
                }
                
            }
        });
        
        stressSlider.addSlideStopHandler(new SlideStopHandler<Double>() {
            
            @Override
            public void onSlideStop(SlideStopEvent<Double> event) {

                if(currentTaskOrConcept != null && currentTaskOrConcept instanceof generated.dkf.Task) {
                    generated.dkf.Task task = (generated.dkf.Task)currentTaskOrConcept;
                    generated.dkf.Task.StressMetric stressMetric = task.getStressMetric();
                    if(stressMetric == null) {
                        stressMetric = new generated.dkf.Task.StressMetric();
                        task.setStressMetric(stressMetric);
                    }
                    
                    stressMetric.setValue(BigDecimal.valueOf(event.getValue()));
                }
            }
        });
    }
    
    /**
     * Sets the currently authored task difficulty to easy
     *
     * @param event The event containing information about the click
     */
    @UiHandler("easyDifficultyButton")
    protected void onEasyDifficultyToggled(ClickEvent event) {
        
        logger.fine("Selected Easy Difficulty");
        easyDifficultyButton.setActive(true);
        mediumDifficultyButton.setActive(false);
        hardDifficultyButton.setActive(false);
        
        if(event != null && currentTaskOrConcept != null && currentTaskOrConcept instanceof generated.dkf.Task) {
            generated.dkf.Task task = (generated.dkf.Task)currentTaskOrConcept;
            generated.dkf.Task.DifficultyMetric difficultyMetric = task.getDifficultyMetric();
            if(difficultyMetric == null) {
                difficultyMetric = new generated.dkf.Task.DifficultyMetric();
                task.setDifficultyMetric(difficultyMetric);
            }
            
            difficultyMetric.setValue(BigDecimal.valueOf(TaskPerformanceState.EASY_DIFFICULTY));
        }
    }

    /**
     * Sets the currently authored task difficulty to medium
     *
     * @param event The event containing information about the click
     */
    @UiHandler("mediumDifficultyButton")
    protected void onMediumDifficultyToggled(ClickEvent event) {
        
        logger.fine("Selected Medium Difficulty");
        
        easyDifficultyButton.setActive(false);
        mediumDifficultyButton.setActive(true);
        hardDifficultyButton.setActive(false);
        
        if(currentTaskOrConcept != null && currentTaskOrConcept instanceof generated.dkf.Task) {
            generated.dkf.Task task = (generated.dkf.Task)currentTaskOrConcept;
            generated.dkf.Task.DifficultyMetric difficultyMetric = task.getDifficultyMetric();
            if(difficultyMetric == null) {
                difficultyMetric = new generated.dkf.Task.DifficultyMetric();
                task.setDifficultyMetric(difficultyMetric);
            }
            
            difficultyMetric.setValue(BigDecimal.valueOf(TaskPerformanceState.MED_DIFFICULTY));
        }
    }

    /**
     * Sets the currently authored task difficulty to hard
     *
     * @param event The event containing information about the click
     */
    @UiHandler("hardDifficultyButton")
    protected void onHardDifficultyToggled(ClickEvent event) {
        
        logger.fine("Selected Hard Difficulty");
        
        easyDifficultyButton.setActive(false);
        mediumDifficultyButton.setActive(false);
        hardDifficultyButton.setActive(true);
        
        if(currentTaskOrConcept != null && currentTaskOrConcept instanceof generated.dkf.Task) {
            generated.dkf.Task task = (generated.dkf.Task)currentTaskOrConcept;
            generated.dkf.Task.DifficultyMetric difficultyMetric = task.getDifficultyMetric();
            if(difficultyMetric == null) {
                difficultyMetric = new generated.dkf.Task.DifficultyMetric();
                task.setDifficultyMetric(difficultyMetric);
            }
            
            difficultyMetric.setValue(BigDecimal.valueOf(TaskPerformanceState.HARD_DIFFICULTY));
        }

    }
    
    /**
     * Edit a concept's data model using this widget.
     * 
     * @param concept the concept being edited
     */
    public void edit(Concept concept){
        updateReadOnly();
        
        currentTaskOrConcept = concept;
        
        if(concept == null){
            return;
        }
        
        // only supported for Task
        difficultyPanel.setVisible(false);
        stressPanel.setVisible(false);
        
        scenarioSupportButton.setActive(concept.isScenarioSupport());
        
        if(concept.isScenarioSupport()){
            collapse.show(); // auto expand the panel when there is authored content to show
        }
    }
    
    /**
     * Edit a task's data model using this widget.
     * 
     * @param task the task being edited
     */
    public void edit(Task task){        
        updateReadOnly();
        
        currentTaskOrConcept = task;
        
        if(task == null){
            return;
        }
        
        // because the previous instance could have been a Concept which hides these panels
        difficultyPanel.setVisible(true);
        stressPanel.setVisible(true);
        
        // whether to auto expand the collapse for this widget because something is authored in it
        boolean autoShow = false;
        
        scenarioSupportButton.setActive(task.isScenarioSupport());
        autoShow |= task.isScenarioSupport();
        
        if(task.getStressMetric() != null) {
            Double stressValue = task.getStressMetric().getValue() != null ? task.getStressMetric().getValue().doubleValue() : 0.0;
            stressSlider.setValue(stressValue != null ? stressValue : 0.0);
            autoShow |= stressValue != 0.0;
        }
        
        if(task.getDifficultyMetric() != null) {
            generated.dkf.Task.DifficultyMetric difficultyMetric = task.getDifficultyMetric();
            BigDecimal bgDiffValue = difficultyMetric.getValue();
            if(bgDiffValue == null) {
                // default to easy
                onEasyDifficultyToggled(null);
            }else if(bgDiffValue.doubleValue() >= TaskPerformanceState.HARD_DIFFICULTY) {
                autoShow |= true;
                onHardDifficultyToggled(null);
            }else if(bgDiffValue.doubleValue() >= TaskPerformanceState.MED_DIFFICULTY) {
                autoShow |= true;
                onMediumDifficultyToggled(null);
            }else{
                autoShow |= true;
                onEasyDifficultyToggled(null);
            }
        }

        if(autoShow){
            collapse.show(); // auto expand the panel when there is authored content to show
        }
    }

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface MiscAttributesWidgetUiBinder extends UiBinder<Widget, MiscAttributesWidget> {
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
    }
    
    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        boolean isReadOnly = ScenarioClientUtility.isReadOnly();
        scenarioSupportButton.setEnabled(!isReadOnly);
        
        stressSlider.setEnabled(!isReadOnly);
        easyDifficultyButton.setEnabled(!isReadOnly);
        mediumDifficultyButton.setEnabled(!isReadOnly);
        hardDifficultyButton.setEnabled(!isReadOnly);
    }

}
