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

import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Strategy;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a strategy in the scenario outline and allows the author to rename it, remove it, and edit it
 * 
 * @author nroberts
 */
public class StrategyTreeItem extends ScenarioObjectTreeItem<Strategy>{

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyTreeItem.class.getName());
    
    /**
     * Creates a new tree item that represents and modifies the given strategy and validates it against the given scenario
     * 
     * @param strategy the strategy to represent and modify
     */
    public StrategyTreeItem(final Strategy strategy) {
        super(strategy, true);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Constructing strategy tree item for strategy " + strategy.toString());
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        // button to delete this strategy
        addButton(IconType.TRASH, "Delete this strategy", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting strategy " + strategy.toString());
                }

                OkayCancelDialog.show(
                        "Delete Strategy?", 
                        "Are you sure you want to delete <b>" + ScenarioClientUtility.getScenarioObjectName(getScenarioObject()) + "</b> from this assessment?", 
                        "Delete Strategy", 
                        new OkayCancelCallback() {
                            
                            @Override
                            public void okay() {
                                
                                if(logger.isLoggable(Level.FINE)){
                                    logger.fine("User confirmed delete");
                                }
                                
                                /* Alert all interested parts of the Scenario editor that the
                                 * strategy has been deleted through an event */
                                ScenarioEventUtility.fireDeleteScenarioObjectEvent(strategy, null);
                                
                                if(logger.isLoggable(Level.FINE)){
                                    logger.fine("Finished deleting strategy");
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
        }, false);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing strategy tree item");
        }
    } 
    
    @Override
    public void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {
        
        if(dragged != null 
                && dragged.getScenarioObject() instanceof Strategy 
                && !dragged.getScenarioObject().equals(getScenarioObject())) {
            
            Strategy dragStrategy = (Strategy) dragged.getScenarioObject();
            
            //if a strategy is dragged on top of this strategy, place the dragged strategy after this strategy
            InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();
            
            if(strategies != null) {
                
                strategies.getStrategy().remove(dragStrategy);
                
                int dropIndex = strategies.getStrategy().indexOf(getScenarioObject());
                    
                strategies.getStrategy().add(dropIndex, dragStrategy);
                
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