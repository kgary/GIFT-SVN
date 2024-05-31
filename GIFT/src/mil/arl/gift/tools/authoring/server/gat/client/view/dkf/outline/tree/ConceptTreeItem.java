/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.TreeItem;

import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Task;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility.ConceptNodeRef;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog.ImportHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a concept in the scenario outline and allows the author to rename it, remove it, edit it,
 * and add concepts or conditions to it.
 * 
 * @author nroberts
 */
public class ConceptTreeItem extends ScenarioObjectTreeItem<Concept>{
    
    /** A context menu that lets the author pick whether they want to add a concept or a condition*/
    private ContextMenu addObjectMenu = new ContextMenu();
    
    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(ConceptTreeItem.class.getName());
    
    /** The button for adding a sub-concept or condition */
    private final Icon addButton;
    
    /** The tool tip for a concept with no children */
    private final String ADD_CONCEPT_AND_CONDITION_TOOLTIP = "Add a sub-concept or a condition that defines an activity the learner should be assessed on";

    /** The tool tip for a concept with sub concepts */
    private final String ADD_CONCEPT_TOOLTIP = "Add a sub-concept";

    /** The tool tip for a concept with conditions for children */
    private final String ADD_CONDITION_TOOLTIP = "Add a condition that defines an activity the learner should be assessed on";

    /**
     * Creates a new tree item that represents and modifies the given concept and validates it
     * against the given scenario
     * 
     * @param concept the concept to represent and modify
     */
    public ConceptTreeItem(final Concept concept) {
        super(concept, true);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Constructing concept tree item for concept " + concept.toString());
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button context menu");
        }
        
        //italicizes the name label in the tree if it is associated with a course concept
        if(CourseConceptUtility.getConceptWithName(ScenarioClientUtility.getScenarioObjectName(getScenarioObject())) != null) {
        	nameLabel.addStyleName("associatedWithConcept");
        }
        
        final ScheduledCommand addConceptCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Adding sub-concept to concept" + concept.toString());
                }
                
                addObjectMenu.hide();

                Concept childConcept = ScenarioClientUtility.generateNewConcept();

                ScenarioEventUtility.fireCreateScenarioObjectEvent(childConcept, concept);

                updateButtonTooltip(addButton, ADD_CONCEPT_TOOLTIP);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding sub concept");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-lightbulb-o scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add sub-concept"
            +   "</span>", true, addConceptCommand);
        
        final ScheduledCommand addConditionCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Adding condition to concept" + concept.toString());
                }

                addObjectMenu.hide();

                Condition condition = new Condition();
                condition.setConditionImpl(null);

                ScenarioEventUtility.fireCreateScenarioObjectEvent(condition, concept);
               
                updateButtonTooltip(addButton, ADD_CONDITION_TOOLTIP);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding condition");
                }
            }
        };
        
        final MenuItem addConditionItem = addObjectMenu.getMenu().addItem(
                "<i class='fa fa-cogs scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add condition"
            +   "</span>", true, addConditionCommand);
        
        final ScheduledCommand importCourseConceptCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Importing course concept to concept " + concept.toString());
                }
                
                addObjectMenu.hide();

                ImportCourseConceptDialog.display(new ImportHandler() {
                    
                    @Override
                    public void onImport(ConceptNodeRef nodeToImport) {
                        
                        Concept childConcept = ScenarioClientUtility.importConceptFromCourse(nodeToImport);

                        ScenarioEventUtility.fireCreateScenarioObjectEvent(childConcept, concept);
                    }
                });

                updateButtonTooltip(addButton, ADD_CONCEPT_TOOLTIP);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished importing course concept");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-external-link-square scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Import course concept"
            +   "</span>", true, importCourseConceptCommand);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button logic");
        }
        
        // Update the add button tooltip text
        String addButtonTooltip = ADD_CONCEPT_AND_CONDITION_TOOLTIP;
        if(concept.getConditionsOrConcepts() instanceof Concepts) {
            addButtonTooltip = ADD_CONCEPT_TOOLTIP;
        } else if(concept.getConditionsOrConcepts() instanceof Conditions) {
            addButtonTooltip = ADD_CONDITION_TOOLTIP;
        }

        // button to add concepts or conditions
        addButton = addButton(IconType.PLUS_CIRCLE, addButtonTooltip, new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                if(concept.getConditionsOrConcepts() == null) {
                    addConditionItem.setVisible(true);
                    addObjectMenu.showAtCurrentMousePosition(event);
                    
                } else if(concept.getConditionsOrConcepts() instanceof Concepts) {
                    addConditionItem.setVisible(false);
                    addObjectMenu.showAtCurrentMousePosition(event);
                
                } else if(concept.getConditionsOrConcepts() instanceof Conditions) {
                    addConditionCommand.execute();
                    
                } else {
                    
                    logger.warning("Invalid child type detected for concept. Prompting user for new type.");
                    addObjectMenu.showAtCurrentMousePosition(event);
                }
            }
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        // button to delete this concept
        addButton(IconType.TRASH, "Delete this concept", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting concept " + concept.toString());
                }
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                final TreeItem parentItem = getParentItem();
                
                if(parentItem != null) {
                    
                    Concepts tempConcepts = null;
                    final Serializable parentNode;
                    
                    if(parentItem instanceof TaskTreeItem) {
                        
                        Task task = ((TaskTreeItem) parentItem).getScenarioObject();
                        parentNode = task;
                        
                        if(task.getConcepts() == null) {
                            task.setConcepts(new Concepts());
                        }
                        
                        tempConcepts = task.getConcepts();
                        
                    } else if(parentItem instanceof ConceptTreeItem) {
                        
                        Concept parentConcept = ((ConceptTreeItem) parentItem).getScenarioObject();
                        parentNode = parentConcept;
                        
                        if(parentConcept.getConditionsOrConcepts() == null
                                || !(parentConcept.getConditionsOrConcepts() instanceof Concepts)) {
                            
                            parentConcept.setConditionsOrConcepts(new Concepts());
                        }
                        
                        tempConcepts = (Concepts) parentConcept.getConditionsOrConcepts();
                    } else{
                        return;
                    }
                    
                    final Concepts concepts = tempConcepts;
                    
                    if(concepts != null){
                            
                        OkayCancelDialog.show(
                            "Delete Concept?", 
                            "Are you sure you want to delete <b>" + ScenarioClientUtility.getScenarioObjectName(getScenarioObject()) + "</b> from its parent?", 
                            "Delete Concept", 
                            new OkayCancelCallback() {
                                
                                @Override
                                public void okay() {
                                    
                                    if(logger.isLoggable(Level.FINE)){
                                        logger.fine("User confirmed delete");
                                    }
                                    
                                    ScenarioEventUtility.fireDeleteScenarioObjectEvent(concept, parentNode);
                                    
                                    //allow the author to add conditions again after all the concepts are removed
                                    if(concepts.getConcept().isEmpty()) {
                                        
                                        if(parentItem instanceof ConceptTreeItem) {
                                            
                                            Concept parentConcept = ((ConceptTreeItem) parentItem).getScenarioObject();
                                            parentConcept.setConditionsOrConcepts(null);

                                            ((ConceptTreeItem) parentItem).resetAddButtonTooltip();
                                            
                                        }
                                    }
                                    
                                    if(logger.isLoggable(Level.FINE)){
                                        logger.fine("Finished deleting concept");
                                    }
                                }
                                
                                @Override
                                public void cancel() {
                                    
                                    if(logger.isLoggable(Level.FINE)){
                                        logger.fine("User cancelled delete");
                                    }
                                }
                            }
                        ); 
                    }
                }
            }
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Populating child elements");
        }
        
        if(concept.getConditionsOrConcepts() instanceof Concepts) {
            
            for(Concept childConcept : ((Concepts) concept.getConditionsOrConcepts()).getConcept()) {
                
                TreeItem childItem = new ConceptTreeItem(childConcept);
                
                addItem(childItem);
            }
            
        } else if(concept.getConditionsOrConcepts() instanceof Conditions) {
            
            for(Condition condition : ((Conditions) concept.getConditionsOrConcepts()).getCondition()) {
                
                TreeItem childItem = new ConditionTreeItem(condition);
                
                addItem(childItem);
            }
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing concept tree item");
        }
    }
    
    /**
     * Resets the add button tooltip to the default tooltip which indicates
     * you can add a sub-concept or a condition.
     */
    public void resetAddButtonTooltip() {
        updateButtonTooltip(addButton, ADD_CONCEPT_AND_CONDITION_TOOLTIP);
    }
    
    @Override
    public void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {
        
        if(dragged != null) {
            
            if(dragged.getScenarioObject() instanceof Condition
                    && !(getScenarioObject().getConditionsOrConcepts() instanceof Concepts)) {
                
                Condition dragCondition = (Condition) dragged.getScenarioObject();
                
                //if a condition is dragged on top of this condition, place the dragged condition after this condition
                Conditions fromConditions = null;
                
                //get the list of conditions that the dragged item should be removed from
                Serializable fromParent = ((ScenarioObjectTreeItem<?>) dragged.getParentItem()).getScenarioObject();
                
                if(fromParent instanceof Concept) {
                    
                    Concept parentConcept = (Concept) fromParent;
                    
                    if(parentConcept.getConditionsOrConcepts() instanceof Conditions) {
                        fromConditions = (Conditions) parentConcept.getConditionsOrConcepts();
                    }
                }
                
                if(fromConditions != null) {
                    
                    //remove the dragged item from its original list of conditions
                    fromConditions.getCondition().remove(dragCondition);
                    
                    if(fromConditions.getCondition().isEmpty()) {
                        
                        //if the list of conditions is empty, remove it
                        if(fromParent instanceof Concept) {
                            ((Concept) fromParent).setConditionsOrConcepts(null);
                        }
                    }
                }
                
                //add the dragged condition to this concept's list of conditions
                if(getScenarioObject().getConditionsOrConcepts() == null) {
                    getScenarioObject().setConditionsOrConcepts(new Conditions());
                }
                
                ((Conditions) getScenarioObject().getConditionsOrConcepts()).getCondition().add(0, dragCondition);
                
                //remove the condition's tree item from its parent and add it as a child of this concept
                dragged.remove();
                insertItem(0, dragged);
                
                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));
                
                if(!getScenarioObject().equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(getScenarioObject()));
                }
            
            } else if(dragged.getScenarioObject() instanceof Concept 
                    && !dragged.getScenarioObject().equals(getScenarioObject())) {
            
                Concept dragConcept = (Concept) dragged.getScenarioObject();
                
                //if a concept is dragged on top of this concept, place the dragged concept after this concept
                Concepts fromConcepts = null;
                Concepts toConcepts = null;
                
                Serializable fromParent = ((ScenarioObjectTreeItem<?>) dragged.getParentItem()).getScenarioObject();
                
                if(fromParent instanceof Task) {
                    fromConcepts = ((Task) fromParent).getConcepts();
                    
                } else if(fromParent instanceof Concept) {
                    
                    Concept parentConcept = (Concept) fromParent;
                    
                    if(parentConcept.getConditionsOrConcepts() instanceof Concepts) {
                        fromConcepts = (Concepts) parentConcept.getConditionsOrConcepts();
                    }
                }
                
                Serializable toParent = ((ScenarioObjectTreeItem<?>) getParentItem()).getScenarioObject();
                
                if(toParent instanceof Task) {
                    toConcepts = ((Task) toParent).getConcepts();
                    
                } else if(toParent instanceof Concept) {
                    
                    Concept parentConcept = (Concept) toParent;
                    
                    if(parentConcept.getConditionsOrConcepts() instanceof Concepts) {
                        toConcepts = (Concepts) parentConcept.getConditionsOrConcepts();
                    }
                }
                
                if(fromConcepts != null && toConcepts != null) {
                    
                    fromConcepts.getConcept().remove(dragConcept);
                    
                    if(fromConcepts.getConcept().isEmpty()) {
                        
                        //if the list of conditions is empty, remove it
                        if(fromParent instanceof Concept) {
                            ((Concept) fromParent).setConditionsOrConcepts(null);
                        }
                    }
                    
                    int dropIndex = toConcepts.getConcept().indexOf(getScenarioObject());
                        
                    toConcepts.getConcept().add(dropIndex, dragConcept);
                    
                    boolean wasDragItemSelected = dragged.isSelected();
                    
                    dragged.remove();
                    
                    //move the tree items accordingly
                    for(int index = 0; index < getParentItem().getChildCount(); index++) {
                        
                        if(getParentItem().getChild(index).equals(this)) {
                            
                            getParentItem().insertItem(index, dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                            
                            break;
                        }  
                    }
                }
                
                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));
                
                if(!toParent.equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(toParent));
                }
            }
        }
    }
    
    @Override
    public boolean allowDrop(ScenarioObjectTreeItem<?> otherItem) {
        
        if(otherItem == null) {
            return false;
        }
        
        Serializable object = otherItem.getScenarioObject();
        
        //allow other concepts and conditions to be dropped onto this concept
        return (object instanceof Concept && !otherItem.getScenarioObject().equals(getScenarioObject()))
                || (object instanceof Condition && !(getScenarioObject().getConditionsOrConcepts() instanceof Concepts));
    }
    
    @Override
    public boolean shouldDropBelow(ScenarioObjectTreeItem<?> otherItem) {
        
        if(otherItem == null) {
            return false;
        }
        
        Serializable object = otherItem.getScenarioObject();
        
        //allow other concepts and conditions to be dropped onto this concept
        return object instanceof Condition;
    }
    
	@Override
	public void setNameIfAllowed(final String newName) {
		
		if(newName != null && newName.equals(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()))) {
			return;
		}

		if(CourseConceptUtility.getConceptWithName(ScenarioClientUtility.getScenarioObjectName(getScenarioObject())) != null) {
			OkayCancelDialog.show("Warning", "This task or concept will no longer be associated with its course concept if it is renamed. Are you sure you want to continue?", "Rename", new OkayCancelCallback() {
                 
				@Override
				public void okay() {
					ConceptTreeItem.super.setNameIfAllowed(newName);
					
				}

				@Override
				public void cancel() {
					getNameLabel().setValue(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()));
				}
				
			 });
			
		  } else {
		      ConceptTreeItem.super.setNameIfAllowed(newName);
		  }
	 }	
}