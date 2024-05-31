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

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility.ConceptNodeRef;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog.ImportHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * A tree item that represents a task in the scenario outline and allows the author to rename it, remove it, edit it,
 * and add concepts to it.
 * 
 * @author nroberts
 */
public class TaskTreeItem extends ScenarioObjectTreeItem<Task>{
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(TaskTreeItem.class.getName());
    
    /** A context menu that lets the author pick whether they want to add or import a concept */
    private ContextMenu addObjectMenu = new ContextMenu();
    
    /**
     * Creates a new tree item that represents and modifies the given task and validates it against the given scenario
     * 
     * @param task the task to represent and modify
     */
    public TaskTreeItem(final Task task) {
        super(task, true);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Constructing task tree item for task " + task.toString());
        }
        
        if (task.getConcepts() == null) {
            task.setConcepts(new Concepts());
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button logic");
        }
        
        //italicizes the name label in the tree if it is associated with a course concept
        if(CourseConceptUtility.getConceptWithName(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()))!= null) {
        	nameLabel.addStyleName("associatedWithConcept");
        }
        
        final ScheduledCommand addConceptCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Adding concept to task" + task.toString());
                }
                
                addObjectMenu.hide();

                Concept concept = ScenarioClientUtility.generateNewConcept();

                ScenarioEventUtility.fireCreateScenarioObjectEvent(concept, task);

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding concept");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-lightbulb-o scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add concept"
            +   "</span>", true, addConceptCommand);
        
        final ScheduledCommand importCourseConceptCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Importing course concept to task " + task.toString());
                }
                
                addObjectMenu.hide();
                
                ImportCourseConceptDialog.display(new ImportHandler() {
                    
                    @Override
                    public void onImport(ConceptNodeRef nodeToImport) {
                        
                        Concept childConcept = ScenarioClientUtility.importConceptFromCourse(nodeToImport);

                        ScenarioEventUtility.fireCreateScenarioObjectEvent(childConcept, task);
                    }
                });
                
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
        
        // button to add a concept
        addButton(IconType.PLUS_CIRCLE, "Add a concept that this task should cover", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                addObjectMenu.showAtCurrentMousePosition(event);
            }
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        // button to delete this task
        addButton(IconType.TRASH, "Delete this task", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting task " + task.toString());
                }
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                if(ScenarioClientUtility.getUnmodifiableTaskList().size() > 1) {
                    
                    OkayCancelDialog.show(
                            "Delete Task?",
                            "Are you sure you want to delete <b>" + ScenarioClientUtility.getScenarioObjectName(getScenarioObject()) + "</b> and all of its concepts and conditions from this assessment?",
                            "Delete Task",
                            new OkayCancelCallback() {

                                @Override
                                public void okay() {

                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("User confirmed delete");
                                    }

                                    /* Alert all interested parts of the Scenario editor that the
                                     * task has been deleted through an event */
                                    ScenarioEventUtility.fireDeleteScenarioObjectEvent(task, null);

                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("Finished deleting task");
                                    }
                                }

                                @Override
                                public void cancel() {

                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("User cancelled delete");
                                    }
                                }
                            });
                } else {
                    WarningDialog.error("Failed To Delete Task", "A real time assessment must have one or more tasks. Please create another task before trying to delete this task.");
                }
            }
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Populating child elements");
        }
        
        for (Concept concept : task.getConcepts().getConcept()) {
            addItem(new ConceptTreeItem(concept));
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing task tree item for task");
        }
    }
    
    @Override
    public void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {
        
        if(dragged != null) {
            
            if(dragged.getScenarioObject() instanceof Concept) {
                
                Concept dragConcept = (Concept) dragged.getScenarioObject();
                
                //if a concept is dragged on top of this concept, place the dragged concept after this concept
                Concepts fromConcepts = null;
                
                //get the task/concept that the dragged item should be removed from
                Serializable fromParent = ((ScenarioObjectTreeItem<?>) dragged.getParentItem()).getScenarioObject();
                
                if(fromParent instanceof Concept) {
                    
                    Concept parentConcept = (Concept) fromParent;
                    
                    if(parentConcept.getConditionsOrConcepts() instanceof Concepts) {
                        fromConcepts = (Concepts) parentConcept.getConditionsOrConcepts();
                    }
                    
                } else if(fromParent instanceof Task) {
                    
                    Task parentConcept = (Task) fromParent;
                    
                    fromConcepts = parentConcept.getConcepts();
                }
                
                if(fromConcepts != null) {
                    
                    //remove the dragged item from its original list of concepts
                    fromConcepts.getConcept().remove(dragConcept);
                    
                    if(fromConcepts.getConcept().isEmpty()) {
                        
                        //if the list of concepts is empty, remove it
                        if(fromParent instanceof Concept) {
                            ((Concept) fromParent).setConditionsOrConcepts(null);  
                        }
                    }
                }
                
                //add the dragged concept to this concept's list of concepts
                if(getScenarioObject().getConcepts() == null) {
                    getScenarioObject().setConcepts(new Concepts());
                }
                
                getScenarioObject().getConcepts().getConcept().add(0, dragConcept);
                
                //remove the concept's tree item from its parent and add it as a child of this concept
                dragged.remove();
                insertItem(0, dragged);
                
                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));
                
                if(!getScenarioObject().equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(getScenarioObject()));
                }
            
            } else if(dragged.getScenarioObject() instanceof Task 
                    && !dragged.getScenarioObject().equals(getScenarioObject())) {
                
                Task dragTask = (Task) dragged.getScenarioObject();
                
                //if a task is dragged on top of this task, place the dragged task after this task
                Tasks tasks = ScenarioClientUtility.getTasks();
                
                if(tasks != null) {
                    
                    tasks.getTask().remove(dragTask);
                    
                    int dropIndex = tasks.getTask().indexOf(getScenarioObject());
                        
                    tasks.getTask().add(dropIndex, dragTask);
                    
                    boolean wasDragItemSelected = dragged.isSelected();
                    
                    dragged.remove();
                    
                    //move the tree items accordingly
                    for(int index = 0; index < getTree().getItemCount(); index++) {
                        
                        if(getTree().getItem(index).equals(this)) {
                            
                            getTree().insertItem(index, dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                            
                            break;
                        }  
                    }
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
        
        //allow other tasks and concepts to be dropped onto this task
        return (object instanceof Task && !otherItem.getScenarioObject().equals(getScenarioObject()))
                || object instanceof Concept;
    }
    
    @Override
    public boolean shouldDropBelow(ScenarioObjectTreeItem<?> otherItem) {
        
        if(otherItem == null) {
            return false;
        }
        
        Serializable object = otherItem.getScenarioObject();
        
        //allow other concepts and conditions to be dropped onto this concept
        return object instanceof Concept;
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
						TaskTreeItem.super.setNameIfAllowed(newName);
						
					}

					@Override
					public void cancel() {
						getNameLabel().setValue(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()));
					}
    				
    			});
    			
    		} else {
    		    TaskTreeItem.super.setNameIfAllowed(newName);
    		}
        }		
    }