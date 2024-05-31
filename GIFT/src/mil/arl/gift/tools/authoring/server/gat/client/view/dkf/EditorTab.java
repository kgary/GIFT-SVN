/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Scenario;
import generated.dkf.Scenario.EndTriggers;
import generated.dkf.Strategy;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.gwt.client.validation.ValidationStatusChangedCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A widget used to provide tabs that authors can click on to switch between active scenario object editors
 * 
 * @author nroberts
 */
public class EditorTab extends Composite implements HasText {

    /** The logger for the class */
	private static Logger logger = Logger.getLogger(EditorTab.class.getName());
	
    /** Interface for handling events. */
    interface WidgetEventBinder extends EventBinder<EditorTab> {
    }
    
    /** Create the instance of the event binder (binds the widget for events. */
    private static final WidgetEventBinder eventBinder = GWT
            .create(WidgetEventBinder.class);    

    private static EditorTabUiBinder uiBinder = GWT.create(EditorTabUiBinder.class);

    interface EditorTabUiBinder extends UiBinder<Widget, EditorTab> {
    }

	private static final SafeHtml UNPIN_TOOLTIP = SafeHtmlUtils.fromTrustedString(""
			+ "Unpin from editor<br/>(removes tab from editor when other tabs are opened)"
	);
	
	private static final SafeHtml PIN_TOOLTIP = SafeHtmlUtils.fromTrustedString(""
			+ "Pin to editor<br/>(prevents tab from closing)"
	);
	
	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
		String inActive();
	}

    /**
     * Constructor
     */
    public EditorTab() {
        initWidget(uiBinder.createAndBindUi(this));

        eventBinder.bindEventHandlers(EditorTab.this, SharedResources.getInstance().getEventBus());
    }

	/** The maximum number of characters that be assigned for a scenario object's name*/
	private static final int MAX_NAME_LENGTH = 128;
	
	/**
	 * The number of tabs collapsed into the tab menu when the editor is too small to display all pinned and open tabs
	 */
	private static int TabsCollapsed = 0;
	
    @UiField
	protected Style style;
    
    @UiField
    protected HTMLPanel editorTab;
    
    @UiField
    protected EditableInlineLabel nameLabel;
    
	@UiField
    Icon objectIcon;

    @UiField
    protected Tooltip pinTooltip;
    
    @UiField
    protected Button pinButton;
    
    @UiField
    protected Button removeButton;    

    /**
     * True if the tab is pinned to the editor panel
     */
    private boolean isPinned = false;

    /**
     * True if the tab is collapsed within the tabMenu
     */
    private boolean isCollapsed = false;
    
    /**
     * True if the tab is marked as read-only. Will disable the editable tab name.
     */
    private boolean isHeaderReadOnly = false;

    /**
     * The currently selected tab that is opened for editing
     */
    private static EditorTab activeTab;
    
    /**
     * The {@link Serializable} scenario object this tab is representing
     */
    private Serializable scenarioObject;

    /**
     * The {@link AbstractScenarioObjectEditor} for the scenario object
     */
    private AbstractScenarioObjectEditor<?> editor;
    
    /**
     * The menu item in the tab menu representing this tab when collapsed
     */
    private MenuItem menuItem;
    
    /**
     * Constructor
     * 
     * @param scenarioObject - the {@link Serializable} scenario object the tab represents
     * @param scenario - the overall scenario that is being modified
     * @param editorPanel - the {@link ScenarioObjectEditorPanel} containing this tab
     * @param fireEvents - whether events should be fired after creating the tab (specifically the JumpToEvent)
     */
    public EditorTab(final Serializable scenarioObject, final Scenario scenario, final ScenarioObjectEditorPanel editorPanel, boolean fireEvents) {
        if (scenarioObject == null) {
            throw new IllegalArgumentException("scenarioObject cannot be null.");
        }

        if (editorPanel == null) {
            throw new IllegalArgumentException("editorPanel cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        eventBinder.bindEventHandlers(EditorTab.this, SharedResources.getInstance().getEventBus());
        
        this.scenarioObject = scenarioObject;
        
        boolean disableHeader = scenarioObject instanceof Condition
                || scenarioObject instanceof Scenario
                || scenarioObject instanceof PlacesOfInterest
                || scenarioObject instanceof EndTriggers
                || scenarioObject instanceof AvailableLearnerActions;
        setTabHeaderReadonly(disableHeader || ScenarioClientUtility.isReadOnly());
        
        setActive(true);
        try {
        	setText(ScenarioElementUtil.getObjectName(scenarioObject));
        } catch (IllegalArgumentException e) {
        	logger.log(Level.SEVERE, "There was a problem initializing the editor tab for the scenario object.", e);
        }

        objectIcon.setSize(IconSize.LARGE);

        /* Updates the icon for the tree item. An additional feature of this
         * method is that if the validation cache is dirty for this object,
         * validation will be performed and updated in the cache. */
        updateIcon(ScenarioClientUtility.getValidationCache().isValid(scenarioObject));

		nameLabel.setMaxLength(MAX_NAME_LENGTH);
		
		nameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				final String newName = event.getValue().trim();
				
				if(newName != null && newName.equals(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()))) {
					return;
				}
				
				if((scenarioObject instanceof generated.dkf.Task || scenarioObject instanceof generated.dkf.Concept)
						&&CourseConceptUtility.getConceptWithName(ScenarioClientUtility.getScenarioObjectName(getScenarioObject())) != null){
				
				     OkayCancelDialog.show("Warning", "This task or concept will no longer be associated with its course concept if it is renamed. Are you sure you want to continue?", "Rename", new OkayCancelCallback() {

					        @Override
					        public void okay() {
						    setNameIfAllowed(newName);
				         
					      }

					        @Override
					        public void cancel() {
					        nameLabel.setValue(ScenarioClientUtility.getScenarioObjectName(getScenarioObject()));
						
					     }
					  
				     });
				  } 
				
				else {
				    setNameIfAllowed(newName);
				}
			}
		});
		
		nameLabel.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		            nameLabel.setFocus(false);
		        }
			}
		});
		
		nameLabel.addFocusHandler(new FocusHandler() {
			
			@Override
			public void onFocus(FocusEvent event) {
				nameLabel.getTextEditor().selectAll();
			}
		});

		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    setPinned(false);
				
				if(editorPanel != null){
				    // edit the next object if this tab is the active tab
				    boolean editNextObject = getActiveTab() != null && getActiveTab().getScenarioObject().equals(scenarioObject);
                    editorPanel.stopEditing(scenarioObject, editNextObject, false);
				}
			}
		});

        pinTooltip.setHtml(PIN_TOOLTIP);
        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (scenarioObject != null) {
                    setPinned(!isPinned());
                }
            }
        });
		
		editorTab.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (getActiveTab() == null || !getActiveTab().getScenarioObject().equals(scenarioObject)) {
                    ScenarioEventUtility.fireJumpToEvent(scenarioObject);
                }
            }
        }, ClickEvent.getType());
		
		try { 
		    menuItem = new MenuItem(generateScenarioObjectTabHtml(scenarioObject, ScenarioElementUtil.getTypeIcon(scenarioObject), "black"),
		            new Scheduler.ScheduledCommand() {
	                    
	                    @Override
	                    public void execute() {
	                        ScenarioEventUtility.fireJumpToEvent(scenarioObject);
	                        
	                        editorPanel.getTabMenu().hide();
	                    }
	                });
		} catch (IllegalArgumentException e) {
	    	logger.log(Level.SEVERE, "There was a a problem creating a menu item for scenario object.", e);
	    }
    }

    /**
     * Focuses on the tab's name label.
     */
    public void focusOnTabName() {
        nameLabel.setFocus(true);
    }

    @Override
    public String getText() {
        return nameLabel.getValue();
    }

    @Override
    public void setText(String text) {
        nameLabel.setValue(text);
        nameLabel.setTooltipIsHtml(true);
        nameLabel.setTooltipText("Click to edit<br/>" + nameLabel.getValue());
        nameLabel.setTooltipPlacement(Placement.BOTTOM);
    }
    
    /**
     * Handles when the name of the object has been changed. Changes the name
     * label for this tab item to use the new name.
     * 
     * @param event The {@link RenameScenarioObjectEvent} containing the details
     *        of the rename including what the new name is.
     */
    public void handleRename(RenameScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleRename(" + event + ")");
        }

        String newName = event.getNewName();
        String oldName = getText();

        if (!StringUtils.equals(newName, oldName) && event.getScenarioObject().equals(getScenarioObject())) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Updating the name on the editor from '" + oldName + "' to '" + newName + "'");
            }

            setText(newName);
        }
    }

	/**
	 * Get the number of tabs currently collapsed. This is number of pinned tabs beyond what can be displayed
	 * within the editor's width.
	 * 
	 * @return number of tabs collapsed
	 */
	public static int getTabsCollapsed() {
        return TabsCollapsed;
    }

	/**
	 * Set the number of tabs currently collapsed in the tab menu
	 * 
	 * @param tabsCollapsed number of tabs collapsed
	 */
    public static void setTabsCollapsed(int tabsCollapsed) {
        TabsCollapsed = tabsCollapsed;
    }


    /**
     * Gets whether this tab is pinned to the editor
     * 
     * @return isPinned
     */
	public boolean isPinned() {
        return isPinned;
    }

	/**
	 * Sets whether this tab is pinned to the editor. If true, this tab will remain open
	 * when another tab is selected, or collapsed in the tab menu if there is no room to
	 * display it. If false, the tab will be closed when another scenario object is selected
	 * for editing.
	 * 
	 * @param isPinned true if the tab is pinned
	 */
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
        pinTooltip.setHtml(isPinned ? UNPIN_TOOLTIP : PIN_TOOLTIP);
        pinButton.setActive(isPinned);
    }

    /**
     * Gets whether this tab is currently collapsed in the tab menu
     * 
     * @return true if the tab is collapsed
     */
    public boolean isCollapsed() {
        return isCollapsed;
    }

    /**
     * Sets if this tab is currently collapsed in the tab menu
     * 
     * @param isCollapsed true if the tab is collapsed to the tab menu
     */
    public void setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
    }

    /**
     * Gets the currently selected tab opened for editing. Returns null if no tabs are open.
     * 
     * @return the activeTab
     */
    public static EditorTab getActiveTab() {
        return activeTab;
    }

    /**
     * Gets the {@link Serializable} scenarioObject
     * 
     * @return the scenarioObject
     */
    public Serializable getScenarioObject() {
        return scenarioObject;
    }

    /**
     * Gets the {@link AbstractScenarioObjectEditor} used to edit the scenario object when this tab is selected
     * 
     * @return the editor
     */
    public AbstractScenarioObjectEditor<?> getEditor() {
        return editor;
    }

    /**
     * Sets the {@link AbstractScenarioObjectEditor} that will be displayed when this tab is
     * selected for editing
     * 
     * @param editor the editor to set
     * @throws UnsupportedOperationException if the editor has already been set
     */
    public void setEditor(AbstractScenarioObjectEditor<?> editor) {
        if (this.editor != null) {
            throw new UnsupportedOperationException(
                    "Cannot override a tab's editor. This tab already contains editor '" + this.editor + "'");
        }
        this.editor = editor;
        editor.addValidationStatusChangedCallback(new ValidationStatusChangedCallback() {
            @Override
            public void changedValidity(boolean isValid, boolean fireEvents) {
                /* Update the tab icon to represent the validity */
                updateIcon(isValid);
            }
        });
    }

    /**
     * Gets the {@link MenuItem} used to represent this tab in a {@link ScenarioObjectEditorPanel}'s tab menu once it has been collapsed.
     * 
     * @return the menuItem
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
	 * Generates the HTML to use for a scenario object's accompanying tab item in the tab selection menu
	 * 
	 * @param scenarioObject the scenario object for whom HTML is being generated
	 * @param iconType the icon to use.
	 * @param color the color to set the icon.
	 * @return the HTML for the given scenario's objects tab item
	 */
	private SafeHtml generateScenarioObjectTabHtml(Serializable scenarioObject, IconType iconType, String color) {
	    Icon icon = new Icon(iconType);
	    icon.setColor(color);
	    icon.setSize(IconSize.LARGE);
	    icon.setMarginRight(5);

	    String name = ScenarioElementUtil.getObjectName(scenarioObject);
	    
        /* The 'appendEscaped' method below throws a NPE if null is provided so pass in an empty
         * string instead. The DKF schema has objects like state transition names as optional,
         * therefore null is schema valid. */
        return new SafeHtmlBuilder()
                .appendHtmlConstant("<table><tr><td style=\"text-align='center'; vertical-align='middle'\">")
                .appendHtmlConstant(icon.toString()).appendHtmlConstant("</td><td>")
                .appendEscaped(StringUtils.isBlank(name) ? Constants.EMPTY : name)
                .appendHtmlConstant("</td></tr></table>").toSafeHtml();
    }

	/**
	 * Sets whether a tab is the active tab being focused on, changing it's css values and updating the currently active tab
	 * 
	 * @param active - true if the tab is the active tab being edited
	 */
    public void setActive(boolean active) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setActive(" + active + ")");
        }

	    if (active) {
            // Deactivate the last active tab if it exists
	        if (!this.equals(getActiveTab()) && getActiveTab() != null) {
                getActiveTab().setActive(false);
	        }

            // Make this tab the new active tab
	        activeTab = this;

	        editorTab.removeStyleName(style.inActive());
	        
	        nameLabel.setEditingEnabled(!isHeaderReadOnly);
	        nameLabel.setTooltipIsHtml(true);
	        nameLabel.setTooltipText("Click to edit<br/>" + nameLabel.getValue());
	        nameLabel.setTooltipPlacement(Placement.BOTTOM);	        
            removeButton.removeStyleName(style.inActive());
	    } else {
	        if (this.equals(getActiveTab())) {
	            activeTab = null;
	        }
	        editorTab.addStyleName(style.inActive());
	        nameLabel.setEditingEnabled(false);
	        removeButton.addStyleName(style.inActive());
	    }
	}
    
    /**
     * Updates the icon for the tree item using the provided validity flag.
     * 
     * @param valid true to mark the tab as valid; false to mark it as invalid.
     */
    private void updateIcon(boolean valid) {
        if (valid) {
            setIconType(ScenarioElementUtil.getTypeIcon(scenarioObject), null);
        } else {
            setIconType(IconType.EXCLAMATION_TRIANGLE, "red");
        }
    }

    /**
     * Sets the icon type for the tree item.
     * 
     * @param iconType the {@link IconType}
     * @param color the color to set the icon. Defaults to black.
     */
    public void setIconType(IconType iconType, String color) {
        objectIcon.setType(iconType);
        objectIcon.setColor(StringUtils.isNotBlank(color) ? color : "black");

        if (getMenuItem() != null) {
            getMenuItem().setHTML(generateScenarioObjectTabHtml(getScenarioObject(), iconType, color));
        }
    }
	
    /**
     * Mark the tab header for read-only or not read-only.
     * 
     * @param isReadOnly true to be read-only; will disable the editable name label.
     */
    public void setTabHeaderReadonly(boolean isReadOnly) {
        this.isHeaderReadOnly = isReadOnly;
    }
    
    /**
     * Checks if the scenario object being edited can be changed to the given name, and if it can,
     * updates the scenario object to use the new name. If changing the name would have potentially
     * unwanted side effects, the user may be prompted to confirm or cancel the name change. If the
     * name change is prevented, then the UI elements used to change the name will be reverted to
     * show the existing name and an informative message may be shown to the user.
     *
     * @param newName the new name to give the scenario object being edited. If null, the name
     * change will be cancelled and a message will be displayed to the user.
     */
    public void setNameIfAllowed(String newName) {
    	
    	 if (logger.isLoggable(Level.FINE)) {
             logger.fine("onValueChangeHandler");
		    }
			
			try {
			
			    String name = ScenarioClientUtility.getScenarioObjectName(scenarioObject);
			    
             if (StringUtils.equals(name, newName)) {

                 // No change
                 return;
             } else if(StringUtils.isBlank(newName)) {
                 
                 Notify.notify("The name must contain at least one visible character.", NotifyType.DANGER);
                 setText(name);
                 
                 return;
             }
             
             if (!ScenarioClientUtility.isScenarioObjectNameValid(scenarioObject, newName)) {
                 String warning = "";
                 if(scenarioObject instanceof Task || scenarioObject instanceof Concept){
                     warning = "Another Task or Concept is already named '" + newName +"'.";
                 } else if(scenarioObject instanceof StateTransition){
                     warning = "Another State Transition is already named '" + newName +"'.";
                 } else if(scenarioObject instanceof Strategy){
                     warning = "Another Strategy is already named '" + newName +"'.";
                 }
                 Notify.notify(warning, NotifyType.DANGER);
                 setText(name);

                 return;
             }
             
             ScenarioClientUtility.setScenarioObjectName(scenarioObject, newName);

             setText(newName);

             SharedResources.getInstance().getEventBus()
                     .fireEvent(new RenameScenarioObjectEvent(scenarioObject, name, newName));
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "There was a a problem setting the name for the course object.", e);
			}
    }
}
