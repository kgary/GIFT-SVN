/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem.PickMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamTreeItem;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * The widget that is responsible for showing the {@link Team} structure.
 * 
 * @author sharrison
 */
public class TeamTreeWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TeamTreeWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static TeamTreeWidgetUiBinder uiBinder = GWT.create(TeamTreeWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TeamTreeWidgetUiBinder extends UiBinder<Widget, TeamTreeWidget> {
    }

    /**
     * The color state for a tree item.
     * 
     * @author sharrison
     */
    private enum ColorState {
        /* ORDER MATTERS! This is ordered by priority (highest to lowest). This
         * is important when comparing enums. */

        /** Red */
        RED("red"),
        /** Green */
        GREEN("green"),
        /** Black */
        BLACK("black"),
        /** Unknown */
        UNKNOWN;

        /** The value of the enum */
        String value;

        /** Default constructor. */
        private ColorState() {
        }

        /**
         * Constructor.
         * 
         * @param value the value of the enum.
         */
        private ColorState(String value) {
            this.value = value;
        }

        /**
         * Gets the color state using the provided value.
         * 
         * @param colorString the color string to parse to find the
         *        {@link ColorState}.
         * @return the {@link ColorState} with the value of 'colorString'.
         *         {@link ColorState#UNKNOWN} if it cannot be found.
         */
        public static ColorState getColorStateByValue(String colorString) {
            for (ColorState colorState : ColorState.values()) {
                if (StringUtils.equalsIgnoreCase(colorState.value, colorString)) {
                    return colorState;
                }
            }

            return UNKNOWN;
        }
    }

    /** The {@link Team} tree */
    @UiField(provided = true)
    protected Tree teamTree = new Tree() {
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            /* prevent keyboard navigation so that arrow keys and special
             * characters do not change selection */
            return false;
        }
    };

    /** The domain session id for this team */
    private final int domainSessionId;

    /** The callback to execute when the picked tree state is changed */
    private final ChangeCallback<Map<String, String>> onPickStateChange;

    /** The collection of previously picked items */
    private Map<String, String> previouslyPicked = new HashMap<>();
    
    /** The root of the tree */
    private final TeamTreeItem rootItem;
    
    /**
     * A mapping from each role name to the item used to represent it in the team tree. Used to
     * quickly lookup roles without potentially traversing the entire tree.
     */
    private Map<String, TeamObjectTreeItem<?>> roleNameToTeamItem = new HashMap<>();
    
    /**
     * Instantiates a panel to display the {@link Team} structure as a tree.
     * 
     * @param team the team to use to populate the widget. Can't be null.
     * @param domainSessionId the domain session id for this team.
     * @param onPickStateChange the callback to execute when the picked tree
     *        state is changed. Can't be null.
     */
    public TeamTreeWidget(Team team, int domainSessionId, final ChangeCallback<Map<String, String>> onPickStateChange) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        if (team == null) {
            throw new IllegalArgumentException("The parameter 'team' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.domainSessionId = domainSessionId;
        this.onPickStateChange = onPickStateChange;

        rootItem = new TeamTreeItem(team);
        rootItem.setPickMode(PickMode.MULTIPLE);
        rootItem.setTeamPickEnabled(true);
        collapseAll(rootItem);
        teamTree.addItem(rootItem);

        rootItem.setOnPickStateChangeCommand(new Command() {
            @Override
            public void execute() {
                fireChangedState();
            }
        });

        rootItem.setState(true);
        exposeAllTeamsOnly(rootItem);
    }
    
    /**
     * Recursively expand the team tree until all teams are exposed.  If a team
     * contains only team members than that team node will not be expanded.
     * 
     * @param parent the team node to check if it should be expanded
     */
    private void exposeAllTeamsOnly(TeamTreeItem parent){
        
        for(int index = 0; index < parent.getChildCount(); index++){
            
            final TeamObjectTreeItem<?> child = (TeamObjectTreeItem<?>)parent.getChild(index);
            if(child instanceof TeamTreeItem){
                
                parent.setState(true);
                
                TeamTreeItem teamTreeItem = (TeamTreeItem)child;
                exposeAllTeamsOnly(teamTreeItem);
                
            } else {
                roleNameToTeamItem.put(child.getName(), child);
            }
        }
        
        roleNameToTeamItem.put(parent.getName(), parent);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        setTreeItemEventListener(rootItem);
    }

    /**
     * Applies the click handler to the provided item and all descendants if any
     * of them is a team member.
     * 
     * @param item the root item to search for team members.
     */
    private void setTreeItemEventListener(final TeamObjectTreeItem<?> item) {
        /* Add mouse down listener for tree item selection */
        Event.setEventListener(item.getElement(), new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (StringUtils.equalsIgnoreCase(event.getType(), BrowserEvents.MOUSEDOWN)) {
                    event.stopPropagation();

                    item.togglePicked();

                    if (item.getTeamObject() instanceof Team) {
                        item.setState(Boolean.TRUE.equals(item.isPicked()));
                    }
                }
            }
        });
        Event.sinkEvents(item.getElement(), Event.ONMOUSEDOWN);

        /* Add mouse down listener on the expand/collapse image for teams */
        if (item.getTeamObject() instanceof Team) {
            Node imgNode = JsniUtility.querySelector(item.getElement(), "img");
            final Element imgElement = Element.as(imgNode);
            Event.setEventListener(imgElement, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (StringUtils.equalsIgnoreCase(event.getType(), BrowserEvents.MOUSEDOWN)) {
                        event.stopPropagation();
                        item.setState(!item.getState());
                    }
                }
            });
            Event.sinkEvents(imgElement, Event.ONMOUSEDOWN);
        }

        /* Recurse through children */
        for (int i = 0; i < item.getChildCount(); i++) {
            final TeamObjectTreeItem<?> child = (TeamObjectTreeItem<?>) item.getChild(i);
            setTreeItemEventListener(child);
        }
    }

    /**
     * Execute the handler for the picked team members state change.
     */
    private void fireChangedState() {
        Map<String, String> pickedNames = getPickedTeamMemberRoles();
        onPickStateChange.onChange(Collections.unmodifiableMap(pickedNames),
                Collections.unmodifiableMap(previouslyPicked));

        previouslyPicked.clear();
        previouslyPicked.putAll(pickedNames);
    }

    /**
     * Gets the names of all the team members whether they are picked or not.
     * 
     * @return the names of the team members.
     */
    public List<String> getAllTeamMemberNames() {
        return rootItem.getAllTeamMemberNames();
    }

    /**
     * Checks if anything in the tree is picked.
     * 
     * @return true if anything in the tree; false otherwise.
     */
    public boolean containsPicked() {
        return rootItem.containsPicked();
    }

    /**
     * Sets the names of the top-most teams and team members that should be
     * checked. If one of the provided names belongs to a parent team with
     * children, all of its children will be selected automatically.
     * 
     * @param teamNames the names of the teams and team members that should be
     *        checked
     */
    public void setPickedTeamObjectNames(Collection<String> teamNames) {
        boolean equals = CollectionUtils.equalsIgnoreOrder(teamNames, previouslyPicked.keySet());
        if (equals) {
            return;
        }

        rootItem.setPickedTeamObjectNames(teamNames);
        fireChangedState();
    }

    /**
     * Collapses the given tree item and its children
     * 
     * @param item the tree item to collapse
     */
    private void collapseAll(TreeItem item) {
        item.setState(false);

        for (int i = 0; i < item.getChildCount(); i++) {
            collapseAll(item.getChild(i));
        }
    }

    /**
     * Update the provided team role with the assessment status. This will
     * result in a color change for the team tree item label.
     * 
     * @param assessmentStatus the assessment status with which to update the
     *        team role. Can't be null.
     * @param teamRole the team role to update.
     */
    public void setStatus(Status assessmentStatus, String teamRole) {
        if (assessmentStatus == null) {
            throw new IllegalArgumentException("The parameter 'assessmentStatus' cannot be null.");
        }

        ColorState color;
        switch (assessmentStatus) {
        case PRESENT:
        case PRESENT_FULL_TO_CAPACITY:
        case PRESENT_FULLY_CAPABLE:
            color = ColorState.GREEN;
            break;
        case PRESENT_DAMAGED:
        case PRESENT_DESTROYED:
            color = ColorState.RED;
            break;
        case ANTICIPATED:
        default:
            color = ColorState.BLACK;
        }

        setTeamRoleColor(rootItem, color, teamRole);
    }

    /**
     * Sets the color of the tree item label with the provided team role. If the
     * team role is found, all parents of the team role will be reevaluated to
     * determine if their color should change as well.
     * 
     * @param item the tree item to search for the team role. Can't be null.
     * @param color the color to set. Can't be null.
     * @param teamRole the team role to find and update with the provided color.
     * @return true if the team role was found within the provided item; false
     *         otherwise.
     */
    private boolean setTeamRoleColor(TeamObjectTreeItem<?> item, ColorState color, String teamRole) {
        /* Process team member */
        if (item.getTeamObject() instanceof TeamMember) {
            TeamMember member = (TeamMember) item.getTeamObject();

            /* Check if the team member matches the role */
            if (StringUtils.equalsIgnoreCase(teamRole, member.getName())) {
                item.getElement().getStyle().setColor(color.value);
                return true;
            }
            return false;
        }

        /* Process team */
        if (item.getTeamObject() instanceof Team) {
            ColorState highestPriorityChildColor = ColorState.UNKNOWN;
            boolean foundTeamRole = false;

            for (int i = 0; i < item.getChildCount(); i++) {
                final TeamObjectTreeItem<?> child = (TeamObjectTreeItem<?>) item.getChild(i);
                if (!foundTeamRole) {
                    foundTeamRole = setTeamRoleColor(child, color, teamRole);
                }

                /* Keep track of the child colors in case we find the team role
                 * as a descendant of this team. This team item's color will be
                 * the highest priority color of all its children. */

                String elemChildColorStr = child.getElement().getStyle().getColor();
                ColorState childColor = ColorState.getColorStateByValue(elemChildColorStr);

                /* Check if the child color is a higher priority */
                if (childColor.compareTo(highestPriorityChildColor) < 0) {
                    highestPriorityChildColor = childColor;
                }
            }

            /* The team role we are searching for was part of this team; update
             * the color with the highest priority color of its children. */
            if (foundTeamRole) {
                item.getElement().getStyle().setColor(highestPriorityChildColor.value);
                return true;
            }
        }

        /* Color didn't change */
        return false;
    }
    
    /**
     * Updates the item in the team tree corresponding to the given role name so that it displays the given
     * military symbol next to it. If the item has any parent roles that do not have military symbols next to them,
     * then symbols will be fetched and displayed for them as well.
     * 
     * @param teamRole the role that a military symbol needs to be displayed for
     * @param symbol the symbol to display next to the role
     * @param sidc the SIDC of the military symbol to display
     * @return whether the role's symbol was successfully changed
     */
    public boolean setTeamRoleSymbol(String teamRole, final MilitarySymbol symbol, final SIDC sidc) {
        
        TeamObjectTreeItem<?> item = roleNameToTeamItem.get(teamRole);
        if(item == null) {
            return false;
        }
        
        //update the image displayed next to the target role
        item.setImageUrl(SessionsMapPanel.getMilitarySymbolGenerator().getSymbolUrl(symbol, sidc));
        
        //determine if the target role has any parents that also need symbols next to them
        for(TeamObjectTreeItem<?> currentParent = item.getParentItem(); currentParent != null; currentParent = currentParent.getParentItem()) {
            
            if(StringUtils.isNotBlank(currentParent.getImageUrl())) {
                continue; //this parent role already has a symbol, so skip it
            }
            
            //if a parent role does not have a symbol, request the role's appropriate symbol SIDC from the server
            final TeamObjectTreeItem<?> finalParent = currentParent;
            UiManager.getInstance().getDashboardService().getSidcForRole(
                    BrowserSession.getInstance().getBrowserSessionKey(), 
                    domainSessionId, 
                    finalParent.getName(), 
                    new AsyncCallback<GenericRpcResponse<SIDC>>() {
                        
                        @Override
                        public void onSuccess(GenericRpcResponse<SIDC> result) {
                            
                            if(!result.getWasSuccessful()) {
                                logger.severe("Failed to get SIDC for " + finalParent.getName() + ": " + result.getException().toString());
                                return;
                            }
                            
                            if(result.getContent() != null) {
                                
                                //update the parent role's image to use the returned SIDC corresponding to it
                                finalParent.setImageUrl(SessionsMapPanel.getMilitarySymbolGenerator().getSymbolUrl(
                                        new MilitarySymbol(symbol.getAffiliation(), MilitarySymbol.Status.PRESENT).setSize(30), 
                                        result.getContent()));
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.severe("Caught exception while getting SIDC for " + finalParent.getName() + ": " + caught.toString());
                        }
                    });
        }
        
        return true;
    }
    
    /**
     * Gets the names of all the team members that have been picked and returns a mapping that
     * can be used to quickly reference their nodes in the tree.
     * 
     * @return a mapping from the names of the team members that have been picked to their nodes
     * in the tree. Will not be null.
     */
    public Map<String, String> getPickedTeamMemberRoles() {
        return rootItem.getMembersToNearestPickedRoles();
    }

    /**
     * Returns the current state of the tree.
     * 
     * @return A map containing every team and team member name and whether or
     *         not that entity is selected.
     */
    public Map<String, Boolean> getCurrentStateOfTree() {
        return getCurrentStateOfTree(rootItem);
    }

    /**
     * Returns the current state of the tree.
     * 
     * @param treeItem the tree item to search.
     * @return A map containing every team and team member name and whether or
     *         not that entity is selected.
     */
    private Map<String, Boolean> getCurrentStateOfTree(TeamObjectTreeItem<?> treeItem) {
        Map<String, Boolean> treeState = new HashMap<>();
        treeState.put(treeItem.getName(), Boolean.TRUE.equals(treeItem.isPicked()));

        for (int i = 0; i < treeItem.getChildCount(); i++) {
            final TeamObjectTreeItem<?> child = (TeamObjectTreeItem<?>) treeItem.getChild(i);
            treeState.putAll(getCurrentStateOfTree(child));
        }

        return treeState;
    }

    /**
     * Gets the names of all the team members whether they have been picked on not and returns a mapping that
     * can be used to quickly reference their nodes in the tree.
     * 
     * @return a mapping from the names of the team members to their nodes
     * in the tree. Will not be null.
     */
    public Map<String, String> getTeamMemberRoles() {
        
        List<String> memberRoles = rootItem.getAllTeamMemberNames();
        Map<String, String> memberToNearestSelectedRole = new HashMap<String, String>();
        
        for(String memberRole : memberRoles) {
            memberToNearestSelectedRole.put(memberRole, memberRole);
        }
        
        return memberToNearestSelectedRole;
    }

    /**
     * Return the domain session id for this widget.
     * 
     * @return the domain session id.
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }
}
