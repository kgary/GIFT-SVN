/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TreeItem;

import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Task;
import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A tree item representing a layer of drawn drawn shapes that can be shown and hidden by the author
 * 
 * @author nroberts
 */
public class LayerTreeItem extends TreeItem {
    
    /** The logger for the class */
    static final Logger logger = Logger.getLogger(LayerTreeItem.class.getName());
    
    /** The performance node represented by this layer*/
    private Serializable performanceNode = null;
    
    /** Checkbox used to determine if the author has selected this layer for viewing*/
    private CheckBox checkBox = new CheckBox();

    /**
     * Creates a layer tree item representing the given performance node
     * 
     * @param performanceNode the Task, Concept, or Condition this layer should represent
     * @param onStateChange a command to execute whenever this layers state changes (i.e. from visible to invisible)
     * @param children an optional list of layer tree items to assign as this item's children. This is basically
     * a shorthand method of calling {@link #addItem(TreeItem)} for multiple items.
     */
    public LayerTreeItem(Serializable performanceNode, final Command onStateChange, LayerTreeItem... children) {
        super();
        
        this.performanceNode = performanceNode;
        
        getElement().getStyle().setPaddingTop(0, Unit.PX);
        getElement().getStyle().setPaddingBottom(0, Unit.PX);
        
        //create a check box to allow the author to decide whether or not the layer represented by this item should be shown  
        checkBox.setValue(true);
        
        //set the rendered HTML for the check box's label (i.e. the icon and name)
        final SafeHtmlBuilder html = new SafeHtmlBuilder()
                .appendHtmlConstant("<i class='fa ");
        
        if(performanceNode != null) {
            html.appendHtmlConstant(ScenarioElementUtil.getTypeIcon(performanceNode).getCssName());
        }
        
        html.appendHtmlConstant("'></i>");
        
        if(performanceNode instanceof Task) {
            checkBox.setHTML(html.appendEscaped(((Task) performanceNode).getName()).toSafeHtml());
            
        } else if(performanceNode instanceof Concept) {
            checkBox.setHTML(html.appendEscaped(((Concept) performanceNode).getName()).toSafeHtml());
            
        } else if(performanceNode instanceof Condition) {
            
            final Condition condition = (Condition) performanceNode;
            checkBox.setText("Loading...");
            
            ScenarioClientUtility.getConditionInfoForConditionImpl(condition.getConditionImpl(), new AsyncCallback<InteropsInfo.ConditionInfo>() {
                
                @Override
                public void onSuccess(ConditionInfo conditionInfo) {
                    
                    if(conditionInfo != null && conditionInfo.getDisplayName() != null && !conditionInfo.getDisplayName().isEmpty()){
                        checkBox.setHTML(html.appendEscaped(conditionInfo.getDisplayName()).toSafeHtml());
                    }else{
                        logger.warning("The server could not find the condition display name for '"+condition.getConditionImpl()+"'.");
                        checkBox.setHTML(html.appendEscaped("Unknown").toSafeHtml());
                    }
                }
                
                @Override
                public void onFailure(Throwable thrown) {
                    logger.log(Level.SEVERE, "The server had an problem retrieving the condition information for '"+condition.getConditionImpl()+"'.", thrown);
                    checkBox.setHTML(html.appendEscaped("Unknown").toSafeHtml());
                }
            });
            
        } else {
            
            //this layer does not represent a place of interest reference, so it must represent unused places of interest
            checkBox.setHTML("<i>UNUSED</i>");
        }
        
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(event.getValue()) {
                    
                    //if the author checks this item, need to make sure its parents are checked too
                    LayerTreeItem currentParent = (LayerTreeItem) LayerTreeItem.this.getParentItem();
                    
                    while(currentParent != null) {
                        
                        if(!currentParent.isChecked()) {
                            currentParent.checkBox.setValue(true);
                        }
                        
                        currentParent = (LayerTreeItem) currentParent.getParentItem();
                    }
                }
                
                setChildrenChecked(event.getValue());
                
                //let this item's listener know when this item's state changes
                Scheduler.get().scheduleDeferred(onStateChange);
            }
        });
        
        //allow author to change the check box value by clicking anywhere in its row, rather than just on the check box
        D3.select(getElement()).on("click", new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                D3.event().stopPropagation();
                
                checkBox.setValue(!checkBox.getValue(), true);
                
                return null;
            }
        });
        
        setWidget(checkBox);
        
        //if child items are defined, add them
        if(children != null) {
            for(int i = 0; i < children.length; i++) {
                addItem(children[i]);                
            }
        }
        
        //make this layer visible by default
        setState(true);
    }

    /**
     * Recursively navigates through this tree item's children and updates their check boxes to match the given state
     * 
     * @param checked the state that this tree item's children's check boxes should be given
     */
    protected void setChildrenChecked(Boolean checked) {
        
        int childCount = getChildCount();
        
        if(childCount > 0) {
            for(int i = 0; i < childCount; i++) {
                
                LayerTreeItem item = (LayerTreeItem) getChild(i);
                item.checkBox.setValue(checked);
                item.setChildrenChecked(checked);
            }
        }
    }

    /**
     * Gets the performance node this layer represents
     * 
     * @return the performance node
     */
    public Serializable getPerformanceNode() {
        return performanceNode;
    }
    
    /**
     * Gets all of the conditions that are marked as visible within this item and its children
     * 
     * @return the conditions that are visible
     */
    public Set<Condition> getVisibleConditions(){
        
        Set<Condition> visibleConditions = new HashSet<>();
        
        getVisibleConditions(visibleConditions);
        
        return visibleConditions;
    }

    /**
     * Gets all of the conditions that are marked as visible within this item and its children and adds them to the given set
     * 
     * @param the set to add visible conditions to
     */
    private void getVisibleConditions(Set<Condition> visibleConditions) {
        
        
        if(isChecked()) {
            
            if(performanceNode != null && performanceNode instanceof Condition) {
                visibleConditions.add((Condition) performanceNode);
            }
            
            if(getChildCount() > 0) {
                for(int i = 0; i < getChildCount(); i++) {
                    ((LayerTreeItem) getChild(i)).getVisibleConditions(visibleConditions);
                }   
            }
        }
    }
    
    /**
     * Gets whether or not this layer's check box is checked (i.e. whether or not the layer should be visible)
     * 
     * @return whether the check box is checked.
     */
    public boolean isChecked() {
        return checkBox.getValue();
    }
}
