/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.EditorTab;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamReferencesUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The widget that is responsible for editing a {@link TeamOrganization} object
 * 
 * @author nroberts
 *
 */
public class TeamOrganizationPanel extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(TeamOrganizationPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static TeamOrganizationPanelUiBinder uiBinder = GWT.create(TeamOrganizationPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TeamOrganizationPanelUiBinder extends UiBinder<Widget, TeamOrganizationPanel> {
    }
    
    /**
     * The Interface TeamOrganizationPanelEventBinder.
     */
    interface TeamOrganizationPanelEventBinder extends EventBinder<TeamOrganizationPanel> {
    }

    /** The Constant eventBinder. */
    private static final TeamOrganizationPanelEventBinder eventBinder = GWT.create(TeamOrganizationPanelEventBinder.class);
    
	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
		String teamTreeDisabled();
	}
    
	/** Style for the Team Organization Panel */
    @UiField
	protected Style style;
    
    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);
    
    /** The tree of {@link Team teams} */
    @UiField(provided=true)
    protected Tree teamTree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };
    
    /** Button used to collapse all the teams in the tree */
    @UiField
    protected Button collapseAllButton;
    
    /** Button used to expand all the teams in the tree */
    @UiField
    protected Button expandAllButton;
    
    /** A text box used to search for team and team member names */
    @UiField
    protected TextBox searchBox;
    
    /** Validation that warns the author when there are no team members in the entire team organization outline */
    private WidgetValidationStatus emptyTreeValidation;

    /** Validation that warns the author when there is an error with one of the team organization's children */
    private WidgetValidationStatus treeValidation;
    
    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;

    /**
     * Instantiates a panel to modify the scenario's global {@link TeamOrganization}
     */
    public TeamOrganizationPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        
        if (GatClientUtility.isReadOnly()) {
            setReadonly(true);
        }
        
        emptyTreeValidation = new WidgetValidationStatus(teamTree, 
                "No learner roles are available. Please add at least one learner role "
                + "corresponding to a learner in the training application.");
        
        treeValidation = new WidgetValidationStatus(teamTree, "An item in the tree is invalid.");
        
        collapseAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < teamTree.getItemCount(); i++) {
                    collapseAll(teamTree.getItem(i));
                }
            }

            /**
             * Collapses the given tree item and its children
             * 
             * @param item the tree item to collapse
             */
            private void collapseAll(TreeItem item) {
                item.setState(false);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    collapseAll(item.getChild(i));
                }
            }
        });
        
        expandAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < teamTree.getItemCount(); i++) {
                    expandAll(teamTree.getItem(i));
                }
            }

            /**
             * Expands the given tree item and its children
             * 
             * @param item the tree item to expand
             */
            private void expandAll(TreeItem item) {
                item.setState(true);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    expandAll(item.getChild(i));
                }
            }
        });
        
        searchBox.setPlaceholder("Search teams");
        searchBox.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {
                
                if(scheduledFilter == null) {
                    
                    // Schedule a filter operation for the list. We don't want to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            // update the filter for the list
                            edit(ScenarioClientUtility.getTeamOrganization());
                            
                            //allow the filter operation to be applied again, since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        searchBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                /* select all of the search box's text when it gains focus so that it's easier for
                 * the author to clear out */
                searchBox.selectAll();
            }
        });

        // needs to be called last
        initValidationComposite(validations);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(emptyTreeValidation);
        validationStatuses.add(treeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if(emptyTreeValidation.equals(validationStatus)) {
            emptyTreeValidation.setValidity(ScenarioClientUtility.getAnyTeamMemberName() != null);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //no children to validate
    }

    /**
     * Populates the panel using the data within the given {@link TeamOrganization}.
     * 
     * @param courseObject the data object that will be used to populate the panel.
     */
    public void edit(TeamOrganization courseObject) {
        
        teamTree.clear();
        
        EditableTeamTreeItem rootItem = new EditableTeamTreeItem(courseObject.getTeam());
        /* Manually update the tree validation with the root tree item's
         * validity */
        treeValidation.setValidity(rootItem.isValid());
        teamTree.addItem(rootItem);
        
        if(StringUtils.isNotBlank(searchBox.getText())) {
            
            //sort the team tree's items by the given search text, if any search text has been entered
            rootItem.sortByText(searchBox.getText().toLowerCase());
        }
        
        validateAll();
    }
    
    /**
     * Find the TreeItem that corresponds to the Serializable item it represents
     * 
     * @param item The Serializable item to look for. Cannot be null.
     * @return the TeamObjectTreeItem that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private TeamObjectTreeItem<?> findTreeItem(Serializable item) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("findTreeItem(" + item + ")");
        }

        if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        TeamObjectTreeItem<?> node = null;

        for (int i = 0; i < teamTree.getItemCount(); i++) {
            node = findTreeItem((TeamObjectTreeItem<?>) teamTree.getItem(i), item);
            if (node != null) {
                return node;
            }
        }

        return node;
    }
    
    /**
     * Recursive method to search through the tree for the Serializable item.
     * 
     * @param node the root node containing the TeamObjectTreeItems we want to search through.
     *        Cannot be null.
     * @param item the Serializable item we want to find. Cannot be null.
     * @return the TeamObjectTreeItem that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private TeamObjectTreeItem<?> findTreeItem(TeamObjectTreeItem<?> node, Serializable item) {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'node' cannot be null.");
        } else if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        if (node.getTeamObject() == item) {
            return node;
        }

        TeamObjectTreeItem<?> currNode = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i) instanceof TeamObjectTreeItem) {
                currNode = findTreeItem((TeamObjectTreeItem<?>) node.getChild(i), item);
                if (currNode != null) {
                    return currNode;
                }
            }
        }

        return currNode;
    }
    
    /**
     * Walks the tree (from the bottom up) and performs validation for each item found.
     * 
     * @param treeItem the bottom node of the tree from which to start walking. If null, no validation occurs.
     */
    private void walkTreeAndValidate(TeamObjectTreeItem<?> treeItem) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("walkTreeAndValidate(" + treeItem + ")");
        }

        if (treeItem == null) {
            return;
        }

        boolean isTreeValid = true;
        while (treeItem != null) {

            ScenarioClientUtility.getValidationCache().getStatus(treeItem.getTeamObject(), true);
            
            // update icon to reflect validity of the object
            treeItem.updateIcon();
            isTreeValid &= treeItem.isValid();

            // move up the tree
            treeItem = treeItem.getParentItem();

            if (treeItem == null) {
                // update validation message for tree
            }
        }

        treeValidation.setValidity(isTreeValid);
    }
    
    /**
     * Handles when a dirty event has been fired. Revalidates any necessary tree items.
     * 
     * @param event The dirty event containing details about the source scenario object.
     */
    public void handleDirtyEvent(ScenarioEditorDirtyEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleDirtyEvent(" + event.getSourceScenarioObject() + ")");
        }

        Serializable scenarioObject = event.getSourceScenarioObject();
        if (scenarioObject == null 
                || (!(scenarioObject instanceof Team) && !(scenarioObject instanceof TeamMember))) {
            return;
        }

        // walk tree
        walkTreeAndValidate(findTreeItem(scenarioObject));
        
        validateAll();
    }

    /**
     * Handles when a team or team member is renamed and updates the tree if necessary
     * 
     * @param event the rename event
     */
    @EventHandler
    public void onTeamRenamed(TeamRenamedEvent event) {
        
        if(EditorTab.getActiveTab() != null 
                && EditorTab.getActiveTab().getScenarioObject() != null
                && !EditorTab.getActiveTab().getScenarioObject().equals(ScenarioClientUtility.getTeamOrganization())) {
            
            //update the team organization tree if a team or team member's name is changed outside this editor
            edit(ScenarioClientUtility.getTeamOrganization());
        }
    }

    /**
     * Handles when team and team member references are updated and updates the tree if necessary
     * 
     * @param event the update event
     */
    @EventHandler
    public void onTeamReferencesUpdated(TeamReferencesUpdatedEvent event) {
        
        if(EditorTab.getActiveTab() != null 
                && EditorTab.getActiveTab().getScenarioObject() != null
                && !EditorTab.getActiveTab().getScenarioObject().equals(ScenarioClientUtility.getTeamOrganization())) {
            
            //update the team organization tree if a team or team member's reference is changed outside this editor
            edit(ScenarioClientUtility.getTeamOrganization());
        }
    }
    
    /**
     * Updates the style of the team organization panel to read only mode
     * 
     * @param isReadonly whether to set the panel as read only
     */
    private void setReadonly(boolean isReadonly) {
        if (isReadonly) {
            teamTree.addStyleName(style.teamTreeDisabled());
        } else {
            teamTree.removeStyleName(style.teamTreeDisabled());
        }
    }

}
