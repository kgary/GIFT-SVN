/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * The panel that acts as the dropdown for {@link TeamPicker team pickers} and provides the tree of 
 * teams that's used to allow authors to select, add, edit, and remove team members.
 * <br/><br/>
 * This panel is absolutely positioned relative to its associated team picker so that it moves relative to the picker's
 * rendered position, even when the author scrolls the page. At a basic level, the behavior of this panel mimics that of
 * GWT's PopupPanel, only in this case, the panel is attached to the picker itself rather than to the RootPanel.
 * 
 * @author nroberts
 *
 */
public class TeamSelectionPanel extends Composite {

    /** The ui binder. */
    private static TeamSelectionPanelUiBinder uiBinder = GWT.create(TeamSelectionPanelUiBinder.class);

    /** The Interface TeamSelectionPanelUiBinder */
    interface TeamSelectionPanelUiBinder extends UiBinder<Widget, TeamSelectionPanel> {
    }

    /** The singleton instance of this class */
    private static TeamSelectionPanel instance;
    
    /** The team picker that this panel is currently being shown for */
    private static TeamPicker currentPicker;
    
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
    
    /**
     * Creates a new team selection panel, initializes its tree of teams, and sets up its event handlers
     */
    private TeamSelectionPanel() {
        
        initWidget(uiBinder.createAndBindUi(this));
        
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
        
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                // Nick: This logic imitates the auto-hiding logic in
                // PopupPanel.previewNativeEvent()
                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());
                boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent) || eventTargetsPartner(nativeEvent);
                if (eventTargetsPopupOrPartner) {
                    event.consume();
                }

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    if (!eventTargetsPopupOrPartner) {
                        hideSelector();
                        return;
                    }
                    break;
                }
            }
        });
    }
    
    /**
     * Shows the team selector for the given team picker and applies that picker's search text as a filter
     * for the selector's tree of teams
     * 
     * @param picker the place of interest picker that the selector is being shown for
     */
    public static void showSelector(TeamPicker picker) {
        
        if(currentPicker == null || !currentPicker.equals(picker)) {
            
            //if a different picker is attempting to show the selector, remove the selector from the old picker
            getInstance().removeFromParent();
        }
        
        currentPicker = picker;
        
        //apply the picker's search text as a filter and update the tree of teams
        loadAndFilterTeams(picker);
        
        if(currentPicker != null) {
            
            //show the selector below the picker
            currentPicker.getSelectorPanel().add(getInstance());
            currentPicker.getSelectorPanel().show();
        }
    }
    
    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance
     */
    private static TeamSelectionPanel getInstance() {
        
        if(instance == null) {
            instance = new TeamSelectionPanel();
        }
        
        return instance;
    }
    
    /**
     * Hides the place of interest selector
     */
    public static void hideSelector() {
        
        if(currentPicker != null) {
            currentPicker.getSelectorPanel().hide();
            currentPicker.getSearchBox().clear(); //clear search text when dropdown is hidden
        }
    }
    
    /**
     * Does the event target this popup?
     *
     * @param event the native event
     * @return true if the event targets the popup
     */
    private boolean eventTargetsPopup(NativeEvent event) {
      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          return getElement().isOrHasChild(Element.as(target));
      }
      return false;
    }
    
    /**
     * Does the event target one of the partner elements?
     *
     * @param event the native event
     * @return true if the event targets a partner
     */
    private boolean eventTargetsPartner(NativeEvent event) {
        
      if (currentPicker == null) {
          return false;
      }

      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          if (currentPicker.getTextBoxRef().getElement().isOrHasChild(Element.as(target))) {
              return true;
          }
      }
      return false;
    }
    
    /**
     * Loads the global tree of teams and filters it based on the search text entered into the
     * current team picker (if applicable). If no search text has been entered, then all of the teams
     * will be shown.
     * 
     * @param teamPicker the team picker that is using this selection panel
     */
    public static void loadAndFilterTeams(final TeamPicker teamPicker) {
        
        TeamObjectTreeItem.cancelDragging(); //need to cancel dragging if tree is reloaded
        
        for(int i = 0; i < getInstance().teamTree.getItemCount(); i++) {
            
            TreeItem item = getInstance().teamTree.getItem(i);
            
            if(item instanceof TeamObjectTreeItem<?>) {
                ((TeamObjectTreeItem<?>) item).cleanUpTooltips();
            }
        }
        
        final TeamTreeItem rootItem = teamPicker.createTeamTreeItem();
        
        if(currentPicker != null) {
            rootItem.setPickMode(currentPicker.getPickMode());
            rootItem.setPickedTeamObjectNames(currentPicker.getDisplayedValue());
        }
        
        rootItem.setOnPickStateChangeCommand(new Command() {
            
            @Override
            public void execute() {
                
                if(currentPicker != null) {
                    currentPicker.setValue(rootItem.getPickedTeamObjectNames(), true);
                }
            }
        });
        
        if(currentPicker != null && StringUtils.isNotBlank(currentPicker.getSearchBox().getText())) {
            
            //sort the team tree's items by the given search text, if any search text has been entered
            rootItem.sortByText(currentPicker.getSearchBox().getText().toLowerCase());
        }
        
        getInstance().teamTree.clear();
        getInstance().teamTree.addItem(rootItem);
    }
}
