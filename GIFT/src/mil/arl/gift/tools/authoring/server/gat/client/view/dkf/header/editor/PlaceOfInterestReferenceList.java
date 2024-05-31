/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.util.logging.Level;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * A widget used to display a list of conditions referencing a place of interest
 * 
 * @author nroberts
 */
public class PlaceOfInterestReferenceList extends Composite{
    
    /** The list of condition references*/
    private GenericListEditor<PlaceOfInterestReference> referenceList;
    
    /** A command to execute whenever the author decides to jump to one of the condition references*/
    private Command onJumpCommand;
    
    /**
     * Creates a new widget that displays a list of conditions referencing a place of interest
     */
    public PlaceOfInterestReferenceList() {
        
        referenceList = new GenericListEditor<>(new Stringifier<PlaceOfInterestReference>() {
            @Override
            public String stringify(final PlaceOfInterestReference ref) {
                
                final StringBuilder displayString = new StringBuilder();
                
                if(ref instanceof PlaceOfInterestConditionReference){
                    final PlaceOfInterestConditionReference condRef = (PlaceOfInterestConditionReference)ref;
                    
                    //get the display name for the condition, potentially invoking a server call
                    ScenarioClientUtility.getConditionInfoForConditionImpl(condRef.getCondition().getConditionImpl(), new AsyncCallback<InteropsInfo.ConditionInfo>() {
                        
                        /**
                         * Renders the given condition display name as HTML, including its parent concept and number of references
                         * 
                         * @param string the condition display name to render
                         */
                        private void renderDisplayName(String string) {
                            
                            if(displayString.toString().equals("Loading...")) {
                                
                                //a server call was made to get the condition name, so redraw this condition in the list
                                referenceList.redrawItem(ref);
                                
                            } else {
                                
                                //no server call was made, so just update the display string before it returns
                                displayString.append("<div style='margin-right: 10px; display: inline-block;'>");
                                displayString.append(string);
                                displayString.append("</div>");
                                
                                displayString.append("<div style='display: inline-block; border: 1px solid black; padding: 2px 8px; border-radius: 8px;'>");
                                displayString.append("<b style='margin-right: 5px;'>Concept:</b>");
                                displayString.append(condRef.getParent().getName());
                                displayString.append("</div>");
                                
                                displayString.append("<div style='display: inline-block; float: right;'>");
                                displayString.append(ref.getReferenceCount());
                                displayString.append("</div>");
                            }
                        }
                        
                        @Override
                        public void onSuccess(ConditionInfo conditionInfo) {
                            
                            String text = "UNKNOWN";
                            
                            if(conditionInfo != null && conditionInfo.getDisplayName() != null && !conditionInfo.getDisplayName().isEmpty()){
                                text = conditionInfo.getDisplayName();
                            }else{
                                PlacesOfInterestPanel.logger.warning("The server could not find the condition display name for '"+condRef.getCondition().getConditionImpl()+"'.");
                            }
                            
                            renderDisplayName(text);
                        }
                        
                        @Override
                        public void onFailure(Throwable thrown) {
                            PlacesOfInterestPanel.logger.log(Level.SEVERE, "The server had an problem retrieving the condition information for '"+condRef.getCondition().getConditionImpl()+"'.", thrown);
                            
                            renderDisplayName("UNKNOWN");
                        }
                    });
                }else if(ref instanceof PlaceOfInterestStrategyReference){
                    PlaceOfInterestStrategyReference strategyRef = (PlaceOfInterestStrategyReference)ref;
                    
                    displayString.append("<div style='margin-right: 10px; display: inline-block;'>");
                    displayString.append(strategyRef.getStrategy().getName());
                    displayString.append("</div>");
                    
                    displayString.append("<div style='display: inline-block; float: right;'>");
                    displayString.append(ref.getReferenceCount());
                    displayString.append("</div>");
                }
                
                if(displayString.toString().isEmpty()) {
                    displayString.append("Loading...");
                }
                
                return displayString.toString();
            }
        });
        
        ItemAction<PlaceOfInterestReference> jumpToAction = new ItemAction<PlaceOfInterestReference>() {
            
            @Override
            public boolean isEnabled(PlaceOfInterestReference item) {
                return true;
            }
            
            @Override
            public String getTooltip(PlaceOfInterestReference item) {
                return "Click to navigate to this reference";
            }
            
            @Override
            public IconType getIconType(PlaceOfInterestReference item) {
                return IconType.EXTERNAL_LINK;
            }
            
            @Override
            public void execute(PlaceOfInterestReference item) {
                if (PlacesOfInterestPanel.logger.isLoggable(Level.FINE)) {
                    PlacesOfInterestPanel.logger.fine("jumpToAction.execute(" + item + ")");
                }
                
                if(item instanceof PlaceOfInterestConditionReference){                
                    ScenarioEventUtility.fireJumpToEvent(((PlaceOfInterestConditionReference)item).getCondition());
                }else if(item instanceof PlaceOfInterestStrategyReference){
                    ScenarioEventUtility.fireJumpToEvent(((PlaceOfInterestStrategyReference)item).getStrategy());
                }
                
                //hide the wrap panel if it is showing so that the author can see the object that was jumped to
                WrapPanel.hide();
                
                onJump();
            }
        };
        
        referenceList.setRowAction(jumpToAction);
        referenceList.setPlaceholder("No conditions are referencing this place of interest");
        referenceList.getElement().getStyle().setProperty("maxHeight", "200px");
        referenceList.getElement().getStyle().setProperty("border", "1px solid rgb(200,200,200)");
        referenceList.getElement().getStyle().setProperty("borderRadius", "3px");
        referenceList.getElement().getStyle().setOverflow(Overflow.AUTO);
        
        initWidget(referenceList);
    }
    
    /**
     * Gets the list used to display the conditions referencing a place of interest
     * 
     * @return the condition reference list
     */
    public GenericListEditor<PlaceOfInterestReference> getListEditor(){
        return referenceList;
    }
    
    /**
     * Sets a command to be invoked when the author decides to jump to a condition reference
     * 
     * @param command the command to invoke
     */
    public void setOnJumpCommand(Command command) {
        this.onJumpCommand = command;
    }
    
    /**
     * Notifies listeners that the author has decided to jump to a condition reference
     */
    private void onJump() {
        
        if(onJumpCommand != null) {
            onJumpCommand.execute();
        }
    }
}