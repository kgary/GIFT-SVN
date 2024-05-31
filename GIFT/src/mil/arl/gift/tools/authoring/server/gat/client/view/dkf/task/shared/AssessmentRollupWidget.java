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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.PerformanceMetricArguments;
import generated.dkf.Task;
import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor used to modify the rules that determine how a performance node's metrics are calculated
 * with respect to its children
 * 
 * @author nroberts
 */
public class AssessmentRollupWidget extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(AssessmentRollupWidget.class.getName());

    private static AssessmentRollupWidgetUiBinder uiBinder = GWT.create(AssessmentRollupWidgetUiBinder.class);
    
    /** Interface for handling events. */
    interface WidgetEventBinder extends EventBinder<AssessmentRollupWidget> {
    }
    
    /** Create the instance of the event binder (binds the widget for events. */
    private final WidgetEventBinder eventBinder = GWT.create(WidgetEventBinder.class);

    interface AssessmentRollupWidgetUiBinder extends UiBinder<Widget, AssessmentRollupWidget> {
    }
    
    /** The collapse that contains the body of the widget */
    @UiField
    protected Collapse collapse;
    
    /** the header for this widget */
    @UiField
    protected PanelHeader panelHeader;
    
    /** A list displaying the rules that determine how each child should be evaluated */
    @UiField(provided = true)
    protected ItemListEditor<MetricArgumentsWrapper<?>> childRules = new ItemListEditor<>();
    
    /** The validation for the total weights needing to equal 100% (1.0) */
    private final WidgetValidationStatus childRulesTotalValidation = new WidgetValidationStatus(childRules, "The total weight must equal 100%");
    
    /** The performance node being edited */
    private Serializable scenarioObj;
    
    /**
     * Handles revalidating the assessment rollup widget
     */
    private ValueChangeHandler<BigDecimal> weightValueChangeHandler = new ValueChangeHandler<BigDecimal>(){

        @Override
        public void onValueChange(ValueChangeEvent<BigDecimal> arg0) {
            requestValidationAndFireDirtyEvent(scenarioObj, childRulesTotalValidation);
        }
        
    };

    /**
     * Creates a new editor that can modify how a performance node's metrics are calculated
     * with respect to its children
     */
    public AssessmentRollupWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(AssessmentRollupWidget.this, SharedResources.getInstance().getEventBus());
        
        panelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (collapse.isShown()) {
                    collapse.hide();
                } else {
                    
                    //if the author tries to change the rollup rules, ensure the children all have non-null weights
                    if(provideMissingWeights()) {
                        refresh();
                    }
                    
                    collapse.show();
                }
            }
        }, ClickEvent.getType());
        
        childRules.setFields(buildItemFields());
        childRules.setActions(null, false); //hide the delete button, since deleting doesn't make sense here
    }
    
    /**
     * Automatically adjusts the performance metric rules for the given parent object's children so that any
     * metric rules that do not yet have a weight associated with them are provided with one based on 
     * how much remaining weight is unused (out of 100%)
     * 
     * @param parent the parent object whose children should be adjusted. Only Tasks and Concepts will
     * be affected by this method. For any other argument, this will function as a no-op.
     */
    public static void adjustChildRollupRules(Serializable parent) {
        provideMissingWeights(generateRuleWrappers(parent));
    }
    
    /**
     * Automatically populates default weights for any children under the object being edited that do
     * not yet have an assigned weight. The default weight is evenly distributed based on how much
     * remaining weight (out of 100%) is not used.
     * 
     * @return whether any weights were actually changed. If false, then all children already have
     * weights.
     */
    private boolean provideMissingWeights() {
        return provideMissingWeights(childRules.getItems());
    }
        
    /**
     * Automatically populates default weights for any children in the given list that do
     * not yet have an assigned weight. The default weight is evenly distributed based on how much
     * remaining weight (out of 100%) is not used.
     * 
     * @return whether any weights were actually changed. If false, then all children already have
     * weights.
     */
    private static boolean provideMissingWeights(List<MetricArgumentsWrapper<?>> children) {
        
        double usedWeight = 0;
        double numChildrenWithNoWeights = 0;
        for(MetricArgumentsWrapper<?> child : children) {
            
            if(child.getArguments() != null) {
                usedWeight += child.getArguments().getWeight();
            } else {
                numChildrenWithNoWeights++;
            }
        }
        
        if(numChildrenWithNoWeights <= 0) {
            return true;
        }
        
        //divide the remaining weight evenly between all children that do not have weights
        double splitUnusedWeight = Math.max(0, 1 - usedWeight)/numChildrenWithNoWeights;
        if(splitUnusedWeight < 0.01) {
            splitUnusedWeight = 0; //round to 0 if sufficiently low to account for precision errors
        }
        
        for(MetricArgumentsWrapper<?> child : children) {
            if(child.getArguments() == null) {
                child.setArguments(new PerformanceMetricArguments());
                child.getArguments().setWeight(splitUnusedWeight);
            }
        }
        
        return true;
    }

    /**
     * Loads the given performance node and begins modifying the rules used to evaulate its children
     * 
     * @param obj the performance node to edit. Can be null.
     */
    public void edit(Serializable obj) {
        updateReadOnly();
        
        scenarioObj = obj;
        refresh();
    }
    
    /**
     * Build the summary widgets for the name and summary columns of the item list editor that shows
     * the child concepts or conditions and their weights.
     * @return the collection of ItemFields that define how to summarize each column in a row.
     */
    private Iterable<? extends ItemField<MetricArgumentsWrapper<?>>> buildItemFields() {
        
        ItemField<MetricArgumentsWrapper<?>> nameField = new ItemField<MetricArgumentsWrapper<?>>(null, null) {
            @Override
            public Widget getViewWidget(MetricArgumentsWrapper<?> wrapper) {

                final Label label = new Label("Loading...");
                label.getElement().getStyle().setProperty("minWidth", "200px");
                
                wrapper.getName(new AsyncCallback<String>() {
                    
                    @Override
                    public void onSuccess(String result) {
                        label.setText(result);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        label.setText("Unknown");
                    }
                });
                
                return label;
            }
        };
        
        ItemField<MetricArgumentsWrapper<?>> summaryField = new ItemField<MetricArgumentsWrapper<?>>(null, "100%") {
            @Override
            public Widget getViewWidget(MetricArgumentsWrapper<?> wrapper) {
                
                MetricArgumentsPanel editor = new MetricArgumentsPanel();
                editor.edit(wrapper);
                editor.setWeightChangedListener(weightValueChangeHandler);

                return editor;
            }
        };

        return Arrays.asList(nameField, summaryField);
    }
    
    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(childRulesTotalValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (childRulesTotalValidation.equals(validationStatus)) {
            // determine if the total weights equal 1.0 (close enough w/in precision)
            double total = 0.0;
            boolean wasDefined = false;
            for(MetricArgumentsWrapper<?> wrapper : childRules.getItems()){
                generated.dkf.PerformanceMetricArguments args = wrapper.getArguments();
                if(args != null){
                    wasDefined = true;
                    total += args.getWeight();
                }
            }
            logger.info("total = "+total+", wasDefined = "+wasDefined+", "+ScenarioClientUtility.getScenarioObjectName(scenarioObj));
            validationStatus.setValidity(!wasDefined || (total > 0.999 && total < 1.001));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(childRules);
    }
    
    /**
     * Reload the widget using the latest from the data model.  Then requests validation.
     */
    private void refresh() {
        
        List<MetricArgumentsWrapper<?>> wrappers = generateRuleWrappers(scenarioObj);
        
        childRules.setItems(wrappers);
        
        requestValidation(childRulesTotalValidation);
    }
    
    private static List<MetricArgumentsWrapper<?>> generateRuleWrappers(Serializable scenarioObj) {
        
        List<MetricArgumentsWrapper<?>> wrappers = new ArrayList<>();
        
        if(scenarioObj instanceof Task) {
            
            Task task = (Task) scenarioObj;
            wrap(task.getConcepts(), wrappers);
            
        } else if(scenarioObj instanceof Concept) {
            
            Concept concept = (Concept) scenarioObj;
            
            if(concept.getConditionsOrConcepts() instanceof Concepts) {
                wrap((Concepts) concept.getConditionsOrConcepts(), wrappers);
                
            } else if(concept.getConditionsOrConcepts() instanceof Conditions) {
                for(Condition child : ((Conditions) concept.getConditionsOrConcepts()).getCondition()) {
                    wrappers.add(new ConditionMetricArguments(child));
                }
            }
        }
        
        return wrappers;
    }
    
    /**
     * Place the concepts provided into new wrapper objects and add to the provided list.
     * @param concepts the concepts to place in a new wrapper object.  Can't be null.
     * @param wrappers where to add the new wrapper objects too. Can't be null.
     */
    private static void wrap(Concepts concepts, List<MetricArgumentsWrapper<?>> wrappers) {
        for(Concept child : concepts.getConcept()) {
            wrappers.add(new ConceptMetricArguments(child));
        }
    }
    
    /**
     * Handles when a scenario object is created
     * 
     * @param event the creation event. Cannot be null.
     */
    @EventHandler
    protected void onScenarioObjectCreated(CreateScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof Concept || event.getScenarioObject() instanceof Condition) {
            refresh();
        }
    }
    
    /**
     * Handles when a scenario object is deleted
     * 
     * @param event the deletion event. Cannot be null.
     */
    @EventHandler
    protected void onScenarioObjectDeleted(DeleteScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof Concept || event.getScenarioObject() instanceof Condition) {
            refresh();
        }
    }
    
    /**
     * Handles when a scenario object is renamed
     * 
     * @param event the rename event. Cannot be null.
     */
    @EventHandler
    protected void onRenameScenarioObjectEvent(RenameScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof Concept || event.getScenarioObject() instanceof Condition) {
            refresh();
        }
    }
    
    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        boolean isReadOnly = ScenarioClientUtility.isReadOnly();
        childRules.setReadonly(isReadOnly);
    }

    /**
     * A wrapper around a scenario object with associated arguments used to calculate
     * its parent's metrics 
     * 
     * @author nroberts
     *
     * @param <T> the type of scenario object this wrapper wraps
     */
    public static abstract class MetricArgumentsWrapper<T extends Serializable>{
        
        /** The object that is wrapped by this wrapper */
        public T wrappedObj;
        
        /**
         * Creates a new wrapper that wraps the given object
         * 
         * @param object the object to wrap. Cannot be null.
         */
        public MetricArgumentsWrapper(T object) {
            
            if(object == null) {
                throw new IllegalArgumentException("The object to obtain metric arguments from cannot be null");
            }
            
            wrappedObj = object;
        }
        
        /**
         * Gets the scenario object wrapped by this wrapper
         * 
         * @return the scenario object. Will not be null.
         */
        public T getWrappedObject() {
            return wrappedObj;
        }
        
        /**
         * Gets the metric arguments associated with the scenario object that this
         * object wraps
         * 
         * @return the arguments associated with the scenario object. Can be null.
         */
        public abstract PerformanceMetricArguments getArguments();
        
        /**
         * Sets the metric arguments associated with the scenario object that this
         * object wraps
         * 
         * @param args the arguments associated with the scenario object. Can be null.
         */
        public abstract void setArguments(PerformanceMetricArguments args);
        
        /**
         * Gets the name to display for the the scenario object that this
         * object wraps
         * 
         * @return the name to display for the scenario object. Can be null.
         */
        public abstract void getName(AsyncCallback<String> callback);
    }
    
    /**
     * A wrapper around a concept with arguments used to determine its parent's metrics
     * 
     * @author nroberts
     */
    private static class ConceptMetricArguments extends MetricArgumentsWrapper<Concept>{

        /**
         * Creates a new wrapper around the given concept's metric arguments
         * 
         * @param object the concept to wrap. Cannot be null.
         */
        public ConceptMetricArguments(Concept object) {
            super(object);
        }

        @Override
        public PerformanceMetricArguments getArguments() {
            return getWrappedObject().getPerformanceMetricArguments();
        }

        @Override
        public void setArguments(PerformanceMetricArguments args) {
            getWrappedObject().setPerformanceMetricArguments(args);
        }

        @Override
        public void getName(AsyncCallback<String> callback) {
            callback.onSuccess(getWrappedObject().getName());
        }
    }
    
    /**
     * A wrapper around a condition with arguments used to determine its parent's metrics
     * 
     * @author nroberts
     */
    private static class ConditionMetricArguments extends MetricArgumentsWrapper<Condition>{

        /**
         * Creates a new wrapper around the given condition's metric arguments
         * 
         * @param object the condition to wrap. Cannot be null.
         */
        public ConditionMetricArguments(Condition object) {
            super(object);
        }

        @Override
        public PerformanceMetricArguments getArguments() {
            return getWrappedObject().getPerformanceMetricArguments();
        }
        
        @Override
        public void setArguments(PerformanceMetricArguments args) {
            getWrappedObject().setPerformanceMetricArguments(args);
        }

        @Override
        public void getName(final AsyncCallback<String> callback) {
            
            final Condition condition = getWrappedObject();
            
            if (condition.getConditionImpl() != null) {

                if (ScenarioClientUtility.isConditionExcluded(condition) != null) {
                    /* If the condition was excluded, try to find some useful name
                     * to display other than 'Unknown'. I've chosen to set the class
                     * name. */
                    String[] split = condition.getConditionImpl().split("\\.");
                    callback.onSuccess(split[split.length - 1]);
                } else {
                    ScenarioClientUtility.getConditionInfoForConditionImpl(condition.getConditionImpl(),
                            new AsyncCallback<InteropsInfo.ConditionInfo>() {

                        @Override
                        public void onSuccess(ConditionInfo conditionInfo) {

                            if (conditionInfo != null && conditionInfo.getDisplayName() != null
                                    && !conditionInfo.getDisplayName().isEmpty()) {
                               callback.onSuccess(conditionInfo.getDisplayName());
                            } else {
                                logger.warning("The server could not find the condition display name for '"
                                        + condition.getConditionImpl() + "'.");
                                callback.onFailure(null);
                            }
                        }

                        @Override
                        public void onFailure(Throwable thrown) {
                            logger.log(Level.SEVERE,
                                    "The server had an problem retrieving the condition information for '"
                                            + condition.getConditionImpl() + "'.",
                                    thrown);
                            callback.onFailure(null);
                        }
                    });
                }

            }else{
                callback.onFailure(null);
            }
        }
    }
}
