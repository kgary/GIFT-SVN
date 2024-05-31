/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A widget that is attached to a gwt tree item used for teams.  This widget displays the name of the tree item
 * and the type (team or user). 
 * 
 * @author nblomberg
 *
 */
public class TeamTreeWidgetItem extends Composite  {

    /** ui binder interface */
    interface TeamTreeWidgetItemUiBinder extends UiBinder<Widget, TeamTreeWidgetItem> {
    }
        
    /** instance of the ui binder. */
    private static TeamTreeWidgetItemUiBinder uiBinder = GWT.create(TeamTreeWidgetItemUiBinder.class);
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(TeamTreeWidgetItem.class.getName());
        
    /** Style for a team item.  This is used to override and not show hover styles for team objects. */
    private static final String TEAM_STYLE = "teamSessionTreeTeamItem";
    /** Style for user items.  This is used to apply hover styles for user items. */
    private static final String USER_STYLE = "teamSessionTreeUserItem";
    
    /** The icon indicating the this tree item's type (i.e. team or team member) */
    @UiField
    Icon treeItemType;
    
    /** A label indicating this tree item's name */
    @UiField
    Text treeItemName;
    
    /** The element containing the icon and name label */
    @UiField
    protected Widget container;
    
    /** A tooltip attached to the container widget */
    @UiField
    protected Tooltip tooltip;
    
    /** A label used to display the name of the user that this role is assigned to, if applicable */
    @UiField
    protected Label userNameLabel;
    
    /** Whether the mouse is currently inside this item's boundaries */
    private boolean hasMouseEntered = false;
    
    /** Whether this item's styling currently indicates that it represents a selected team member role */
    private boolean selected = false;
    
    /** The user that this role is currently assigned to, if applicable */
    private String assignedUser = null;
    
    /**
     * Constructor
     * 
     * @param type The icon that will be displayed for the item.
     * @param name The name that will be displayed next to the icon.
     */
    public TeamTreeWidgetItem(IconType type, String name) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("TeamTreeWidgetItem()");
        }
        initWidget(uiBinder.createAndBindUi(this));
        
        treeItemType.setType(type);
        treeItemName.setText(name);
        
        if (type != IconType.USER && 
                type != IconType.USERS) {
            logger.severe("Unexpected icon type being used for TeamTreeWidgetItem: " + type);
        }
        
        if (isTeamMember()) {
            
            addStyleName(USER_STYLE);
            container.addStyleName("btn");
            container.addStyleName("btn-primary");
            tooltip.setTitle("Click to choose this role");
            
        } else {
            addStyleName(TEAM_STYLE);
            tooltip.setTitle("Click to show / hide this team");
        } 
        
        //trigger the container widget's tooltip whenever the mouse overs over any part of this widget, rather than just the container
        addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                if(!hasMouseEntered) {
                    tooltip.show();
                    hasMouseEntered = true;
                }
                
                if(!selected 
                        && StringUtils.isNotBlank(assignedUser) 
                        && StringUtils.isBlank(getElement().getStyle().getCursor())) {
                    
                    getElement().getStyle().setProperty("cursor", "not-allowed");
                }
            }
        }, MouseOverEvent.getType());
        
        addDomHandler(new MouseOutHandler() {
            
            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                if(hasMouseEntered) {
                    tooltip.hide();
                    hasMouseEntered = false;
                }
                
                getElement().getStyle().clearCursor();
            }
        }, MouseOutEvent.getType());
    }
    
    /**
     * Indicates if the item is a user item (instead of a team item)
     * 
     * @return True if the item represents a user (not a team)
     */
    public boolean isTeamMember() {
        return (treeItemType.getType() == IconType.USER);
    }
    
    /**
     * Gets the name of the item
     * 
     * @return Name of the item
     */
    public String getItemName() {
        return treeItemName.getText();
    }
    
    /**
     * Updates this item's styling to match the given selected state
     * 
     * @param selected whether this item should appear visually selected
     */
    public void onSelectedStateChanged(boolean selected) {
        
        boolean needsVisualUpdate = this.selected != selected;
        this.selected = selected;
        
        if(needsVisualUpdate) {
            updateVisualState();
        }
    }
    
    /**
     * Updates this item's assigned user name
     * 
     * @param assignedUser the new assigned user name
     */
    public void onAssignedUserChanged(String assignedUser) {
        
        boolean needsVisualUpdate = this.assignedUser != assignedUser;
        this.assignedUser = assignedUser;
        
        if(needsVisualUpdate) {
            updateVisualState();
        }
    }
    
    /**
     * Updates this item's current styling to match its internal visual state. If the item represents a team member that is selected,
     * the item will look like a green button. If the item represents a team member that is not selected but is available to be chosen, 
     * it will look like a blue button. If the item represents a team member that is not selected AND not available, it will look like a
     * disabled white button. If the item represents a team rather than a team member, no special styling will be applied.
     */
    private void updateVisualState() {
           
        if(isTeamMember()) {
            
            if(selected) {
                
                container.addStyleName("btn-success");
                container.removeStyleName("btn-primary");
                container.removeStyleName("btn-default");
                container.getElement().removeAttribute("style");
                tooltip.setTitle("You have chosen this rule");
                
            } else {
                
                if(assignedUser == null) {
                    
                    container.addStyleName("btn-primary");
                    container.removeStyleName("btn-success");
                    container.removeStyleName("btn-default");
                    container.getElement().removeAttribute("style");
                    tooltip.setTitle("Click to choose this role");
                    
                } else {
                    
                    container.addStyleName("btn-default");
                    container.removeStyleName("btn-success");
                    container.removeStyleName("btn-primary");
                    container.getElement().setAttribute("style", "opacity: 0.6; pointer-events: none;");
                    tooltip.setTitle("Another user has chosen this role");
                }
            }
            
            userNameLabel.setText(assignedUser);
        }
        
        //reposition the tooltip to account for any visual size changes
        if(hasMouseEntered) {
            tooltip.show();
            
        } else {
            tooltip.hide();
        }
    }
    
    /**
     * Whether or not the team member that this tree item represents is currently available for the author to pick
     * 
     * @return whether this tree item's team member is available
     */
    public boolean isAvailable() {
        return assignedUser == null;
    }
}
