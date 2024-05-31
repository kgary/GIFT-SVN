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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import generated.dkf.Actions.StateTransitions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a state transition in the scenario outline and allows the author to rename it, remove it, and edit it
 * 
 * @author nroberts
 */
public class StateTransitionTreeItem extends ScenarioObjectTreeItem<StateTransition>{
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(StateTransitionTreeItem.class.getName());
    
    /**
     * Creates a new tree item that represents and modifies the given state transition and validates it against the given scenario
     * 
     * @param transition the state transition to represent and modify
     */
    public StateTransitionTreeItem(final StateTransition transition) {
        super(transition, true);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Constructing state transition tree item for state transition " + transition.toString());
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        // button to delete this state transition
        addButton(IconType.TRASH, "Delete this state transition", new ClickHandler() {
 
            @Override
            public void onClick(ClickEvent event) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting state transition " + transition.toString());
                }

                String displayName = ScenarioClientUtility.getScenarioObjectName(getScenarioObject());
                
                OkayCancelDialog.show(
                        "Delete State Transition?", 
                        "Are you sure you want to delete " +
                        (StringUtils.isNotBlank(displayName) ? 
                               ("<b>" + displayName + "</b>") :
                               ("this state transition"))+
                        " from this assessment?", 
                        "Delete State Transition", 
                        new OkayCancelCallback() {
                            
                            @Override
                            public void okay() {
                                
                                if(logger.isLoggable(Level.FINE)){
                                    logger.fine("User confirmed delete");
                                }
                                
                                /* Alert all interested parts of the Scenario editor that the state
                                 * transition has been deleted through an event */
                                ScenarioEventUtility.fireDeleteScenarioObjectEvent(transition, null);

                                if(logger.isLoggable(Level.FINE)){
                                    logger.fine("Finished deleting state transition");
                                }
                            }
                            
                            @Override
                            public void cancel() {
                                //Nothing to do
                            }
                        }
                ); 
            }
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing state transition tree item");
        }
    }
    
    @Override
    public void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {
        
        if(dragged != null 
                && dragged.getScenarioObject() instanceof StateTransition 
                && !dragged.getScenarioObject().equals(getScenarioObject())) {
            
            StateTransition dragTransition = (StateTransition) dragged.getScenarioObject();
            
            //if a strategy is dragged on top of this strategy, place the dragged strategy after this strategy
            StateTransitions transitions = ScenarioClientUtility.getStateTransitions();
            
            if(transitions != null) {
                
                transitions.getStateTransition().remove(dragTransition);
                
                int dropIndex = transitions.getStateTransition().indexOf(getScenarioObject());
                
                transitions.getStateTransition().add(dropIndex, dragTransition);
                
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