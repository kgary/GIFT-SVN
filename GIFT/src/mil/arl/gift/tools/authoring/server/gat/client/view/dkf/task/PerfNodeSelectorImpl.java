/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Concept;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A widget that allows authors to select a performance node ID for a task or concept and an assessment value. Tasks and concepts
 * can be searched by entering text into the appropriate search field
 * 
 * @author nroberts
 */
public class PerfNodeSelectorImpl extends ScenarioValidationComposite implements HasValue<BigInteger> {

    /** The ui binder. */
	private static PerfNodeSelectorUiBinder uiBinder = GWT
			.create(PerfNodeSelectorUiBinder.class);

	/**
	 * The Interface DkfPerfNodeSelectorUiBinder.
	 */
	interface PerfNodeSelectorUiBinder extends
			UiBinder<Widget, PerfNodeSelectorImpl> {
	}
	
	/** The list box title. */
	@UiField 
	protected Label listBoxTitle;
	
	/** The text box used to search for performance nodes */
	@UiField
	protected TextBox nodeSearchBox;
	
	/** The button used to jump to a particular performance node for editing */
	@UiField(provided = true)
	protected EnforcedButton jumpToButton = new EnforcedButton(IconType.EXTERNAL_LINK, "", "Navigate to the selected task or concept",
            new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            
            Serializable node = getNodeFromName(nodeSearchBox.getValue());

            if(node != null) {
                ScenarioEventUtility.fireJumpToEvent(node, null, true);
            }
            
            //hide the waypoint selector so that it doesn't block the editor that the author jumps to
            PerfNodeSelectorPanel.hideSelector();
        }
    });
	
	/** The result title. */
	@UiField 
	protected Label resultTitle;
	
	/** The result box. */
	@UiField (provided = true) 
	protected ValueListBox<String> resultBox = new ValueListBox<String>(new Renderer<String>() {

        @Override
        public String render(String object) {
            
            try{
                return AssessmentLevelEnum.valueOf(object).getDisplayName();
                
            } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                return object;
            }

        }

        @Override
        public void render(String object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    });
	
	/** The panel where the performance node selector will appear*/
	@UiField
    protected Collapse selectorPanel;
	
	/** The last valid node name entered into the search box*/
	private String lastSearchValue = null;
	
	/** Whether or not to allow the author to search for tasks instead of concepts*/
	private boolean selectsTasks = false;
	
	/** the task name to filter out  in this selector widget.  Can be null */
	private String taskNameToIgnore = null;
	
    /** The error message to show when no {@link Task} performance node id is selected */
    private final static String TASK_ERR_MSG = "A Task needs to be selected. Please select a Task.";

    /** The error message to show when no {@link Concept} performance node id is selected */
    private final static String CONCEPT_ERR_MSG = "A Concept needs to be selected. Please select a Concept.";

    /**
     * The container for showing validation messages for not having a performance node selected.
     */
    private final WidgetValidationStatus perfNodeValidation;

	/**
	 * Instantiates a new dkf perf node selector impl.
	 */
	public PerfNodeSelectorImpl() {
	    
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    perfNodeValidation = new WidgetValidationStatus(nodeSearchBox, CONCEPT_ERR_MSG);

	    nodeSearchBox.setPlaceholder("Search concepts");
        nodeSearchBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //show the waypoint selection dropdown whenever the search box is selected
                PerfNodeSelectorPanel.showSelector(PerfNodeSelectorImpl.this);
            }
        });
        nodeSearchBox.addDomHandler(new InputHandler() {
            
            @Override
            public void onInput(InputEvent event) {
                
                //update the filter for the selection dropdown
                PerfNodeSelectorPanel.loadAndFilterPerfNodes(taskNameToIgnore);
            }
            
        }, InputEvent.getType());
        nodeSearchBox.addFocusHandler(new FocusHandler() {
            
            @Override
            public void onFocus(FocusEvent event) {
                
                //select all of the search box's text when it gains focus so that it's easier for the author to clear out
                nodeSearchBox.selectAll();
            }
        });

		nodeSearchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                selectNode(event.getValue());
            }
        });
		
		ArrayList<String> results = new ArrayList<String>();
        for (AssessmentLevelEnum result : AssessmentLevelEnum.VALUES()) {
            results.add(result.getName());
        }
		
        resultBox.setValue(results.get(0));
        resultBox.setAcceptableValues(results);
	}
	
	/**
	 * Selects the first available task or concept node found in the suggestion list
	 */
	private void selectFirstNode() {
	        
        Tasks tasks = ScenarioClientUtility.getTasks();
        
        if(tasks != null) {
            
            for(Task task : tasks.getTask()) {
                
                if(selectsTasks) {
                    
                    // check if this task should be ignored
                    if(StringUtils.equalsIgnoreCase(taskNameToIgnore, task.getName())){
                        continue;
                    }
                    
                    if(task.getNodeId() != null) {
                        setValue(task.getNodeId());
                        return;
                    }
                    
                } else if(task.getConcepts() != null){
                    
                    for(Concept concept : task.getConcepts().getConcept()) {
                            
						if (concept.getNodeId() != null
								&& (ScenarioClientUtility.getScenario().getResources().getSourcePath() == null
										|| concept.getExternalSourceId() != null)) {

							setValue(concept.getNodeId());
							return;
						}
						
						List<Concept> subConcepts = ScenarioClientUtility.getSubConcepts(concept);
						
						for(Concept subConcept : subConcepts) {
							if(concept.getNodeId() != null
								&& (ScenarioClientUtility.getScenario().getResources().getSourcePath() == null
										|| subConcept.getExternalSourceId() != null)) {
								setValue(subConcept.getNodeId());
								return;
							}
						}
						
						
                        }
                    }
                }
            }
        }


    /**
	 * Selects the task or concept with the given name in the search box
	 * 
	 * @param value the name of the task or concept to select
	 */
	private void selectNode(String value) {
	    
	    Serializable chosenNode = getNodeFromName(value);
        
        if(StringUtils.isNotBlank(value) 
                && chosenNode != null) {
            
            if(chosenNode instanceof Task) {
                ValueChangeEvent.fire(PerfNodeSelectorImpl.this, ((Task) chosenNode).getNodeId());
                
            } else if(chosenNode instanceof Concept) {
                ValueChangeEvent.fire(PerfNodeSelectorImpl.this, ((Concept) chosenNode).getNodeId());
            }
            
            jumpToButton.setVisible(true);
            
        } else {
            
            //revert to the last text entry entered
            nodeSearchBox.setValue(lastSearchValue);
            
            jumpToButton.setVisible(StringUtils.isNotBlank(lastSearchValue));
        }
    }

    /**
     * Gets the list box title.
     *
     * @return the list box title
     */
	public HasText getListBoxTitle() {
		return listBoxTitle;
	}
	
	/**
     * Gets the result choices.
     * 
     * @return the result choices
     */
	public HasConstrainedValue<String> getResultChoices() {
		return resultBox;
	}
	
	/**
     * Gets the result title.
     *
     * @return the result title
     */
	public HasText getResultTitle() {
		return resultTitle;
	}

	/**
     * Sets the visibility of the results widgets.
     * 
     * @param visible True if visible, false otherwise
     */
	public void setResultVisibility(boolean visible) {
		resultTitle.setVisible(visible);
		resultBox.setVisible(visible);
	}
	
    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        nodeSearchBox.setEnabled(!isReadonly);
        resultBox.setEnabled(!isReadonly);
    }

    @Override
    public BigInteger getValue() {
        
        Serializable node = getNodeFromName(nodeSearchBox.getValue());
        
        if(node instanceof Task) {
            return ((Task) node).getNodeId();
            
        } else if(node instanceof Concept) {
            return ((Concept) node).getNodeId();
        }
        
        return null;
    }

    /**
     * Finds the performance node (i.e. a task or a concept) with the given name.
     * 
     * @param value the name to search for
     * @return the performance node with the given name, or null, if no performance node has that
     *         name
     */
    private Serializable getNodeFromName(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (selectsTasks) {
            for (Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {
                if (StringUtils.equals(value, task.getName())) {
                    return task;
                }
            }
        } else {
            for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
                if (StringUtils.equals(value, concept.getName())) {
                    return concept;
                }
            }
        }

        return null;
    }

    @Override
    public void setValue(BigInteger value) {
        setValue(value, false);
    }

    @Override
    public void setValue(BigInteger value, boolean fireEvents) {
        
        if(value == null) {
            selectFirstNode();
            return;
        }
        
        String nodeName = null;
        
        if(selectsTasks) {
        
            Task task = ScenarioClientUtility.getTaskWithId(value);
            
            if(task != null) {
                nodeName = task.getName();
            }
            
        } else {
            
            Concept concept = ScenarioClientUtility.getConceptWithId(value);

            if(concept != null) {
                nodeName = concept.getName();
            }
        }
        
        nodeSearchBox.setValue(nodeName, fireEvents);
        
        lastSearchValue = nodeSearchBox.getValue();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<BigInteger> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
    /**
     * Sets whether or not this selector should select tasks instead of concepts
     * 
     * @param selectsTasks whether or not tasks should be selected
     */
    public void setSelectsTasks(boolean selectsTasks) {
        
        boolean needsRefresh = this.selectsTasks != selectsTasks;
        this.selectsTasks = selectsTasks;
        
        nodeSearchBox.setPlaceholder(selectsTasks ? "Search tasks" : "Search concepts");
        perfNodeValidation.setErrorMessage(selectsTasks ? TASK_ERR_MSG : CONCEPT_ERR_MSG);
        
        if(needsRefresh) {
            refresh();
        }
    }
    
    /**
     * Gets whether or not this selector selects tasks instead of concepts
     * 
     * @return whether or not this selector selects tasks instead of concepts
     */
    public boolean getSelectsTasks() {
        return selectsTasks;
    }
    
    /**
     * Refresh the node selector widget by clearing out the search value and re-populate
     * the values (tasks or concepts) in the selector
     */
    private void refresh(){
        nodeSearchBox.setValue(null);
        PerfNodeSelectorPanel.loadAndFilterPerfNodes(taskNameToIgnore);
    }
    
    /**
     * Set the task name to filter out in this selector widget.  If the value is a change than
     * refresh the selector widget.
     * 
     * @param taskNameToIgnore the name of a task that should be excluded from the selector widget.
     * Can be null to remove previous filtering.
     */
    public void setTaskNameToIgnore(String taskNameToIgnore){
        
        boolean needsRefresh = !StringUtils.equalsIgnoreCase(this.taskNameToIgnore, taskNameToIgnore);
        this.taskNameToIgnore = taskNameToIgnore;
        
        if(needsRefresh) {
            refresh();
        }
    }
    
    /**
     * Return the task name that is filtered out in this selector widget.
     * @return can be null.
     */
    public String getTaskNameToIgnore(){
        return taskNameToIgnore;
    }
    
    /**
     * Gets the text box used to enter search text
     * 
     * @return the search text box
     */
    ValueBoxBase<String> getSearchBox() {
        return nodeSearchBox;
    }
    
    /**
     * Gets the collapseable panel that the selector should be placed in when it is shown
     * 
     * @return the panel where the selector should be placed
     */
    Collapse getSelectorPanel() {
        return selectorPanel;
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(perfNodeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (perfNodeValidation.equals(validationStatus)) {
            perfNodeValidation.setValidity(getNodeFromName(nodeSearchBox.getValue()) != null);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }
}
