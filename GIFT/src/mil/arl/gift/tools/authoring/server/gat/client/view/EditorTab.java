/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import generated.course.Course;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseObjectEditorPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;

/**
 * Class for editor tabs in the gat
 * 
 * @author cpadilla
 *
 */
public class EditorTab extends Composite implements HasText {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EditorTab.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static EditorTabUiBinder uiBinder = GWT.create(EditorTabUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface EditorTabUiBinder extends UiBinder<Widget, EditorTab> {
    }

    /** Tooltip for the unpin button */
	private static final SafeHtml UNPIN_TOOLTIP = SafeHtmlUtils.fromTrustedString(""
			+ "Unpin from editor<br/>(removes object from editor when other objects are opened)"
	);
	
	/** Tooltip for the pin button */
	private static final SafeHtml PIN_TOOLTIP = SafeHtmlUtils.fromTrustedString(""
			+ "Pin to editor<br/>(moves object to new tab when other objects are opened)"
	);
	
	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
        /**
         * inactive style
         * 
         * @return the style
         */
        String inActive();

        /**
         * disabled name label style
         * 
         * @return the style
         */
        String nameLabelDisabled();
	}

    /**
     * Constructor
     */
    public EditorTab() {
        initWidget(uiBinder.createAndBindUi(this));
    }

	/** The maximum number of characters that be assigned for a course object's name*/
	private static final int MAX_NAME_LENGTH = 128;
	
    /**
     * The number of tabs collapsed into the tab menu when the editor is too small to display all
     * pinned and open tabs
     */
    private static int TabsCollapsed = 0;

    /** The style */
    @UiField
    protected Style style;

    /** The panel containing the tab */
    @UiField
    HTMLPanel editorTab;

    /** The name label */
    @UiField
    TextBox nameLabel;

    /** The tooltip for the name label */
    @UiField
    Tooltip nameTooltip;

    /** The icon to be displayed in the tab */
    @UiField
    Image objectIcon;

    /** The tooltip for the pin/unpin button */
    @UiField
    Tooltip pinTooltip;

    /** The pin/unpin button */
    @UiField
    Button pinButton;

    /** The remove tab button */
    @UiField
    Button removeButton;

    /**
     * True if the tab is pinned to the editor panel
     */
    private boolean isPinned = false;

    /**
     * True if the tab is collapsed within the tabMenu
     */
    private boolean isCollapsed = false;

    /**
     * The currently selected tab that is opened for editing
     */
    private static EditorTab activeTab;
    
    /**
     * The {@link Course} the course objects belong to 
     */
    private static Course loadedCourse;
    
    /**
     * The {@link Serializable} course object this tab is representing
     */
    private Serializable courseObject;

    /**
     * The {@link AbstractCourseObjectEditor} for the course object
     */
    private AbstractCourseObjectEditor<?> editor;
    
    /**
     * The menu item in the tab menu representing this tab when collapsed
     */
    private MenuItem menuItem;
    
    /**
     * Constructor
     * 
     * @param courseObject - the {@link Serializable} course object the tab represents
     * @param editorPanel - the {@link CourseObjectEditorPanel} containing this tab
     */
    public EditorTab(final Serializable courseObject, final CourseObjectEditorPanel editorPanel) {
        if (courseObject == null) {
            throw new IllegalArgumentException("courseObject cannot be null.");
        }

        if (editorPanel == null) {
            throw new IllegalArgumentException("editorPanel cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        this.courseObject = courseObject;

        setActive(true);
        try {
        	setText(CourseElementUtil.getTransitionName(courseObject));
        } catch (IllegalArgumentException e) {
        	logger.log(Level.SEVERE, "There was a problem initializing the editor tab for the course object.", e);
        }

        objectIcon.setUrl(CourseElementUtil.getTypeIcon(courseObject));

		nameLabel.setMaxLength(MAX_NAME_LENGTH);
		
		nameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				try {
				
	                if(event.getValue() == null || event.getValue().isEmpty()) {
	                    
	                    Notify.notify("The " +CourseElementUtil.getTypeDisplayName(courseObject)+" name cannot be empty.", NotifyType.DANGER);
	                    nameLabel.setValue(CourseElementUtil.getTransitionName(courseObject));
	                    
	                    return;
	                    
	                }else if(event.getValue().trim().isEmpty()){
	                    
	                    Notify.notify("The " +CourseElementUtil.getTypeDisplayName(courseObject)+" name must contain characters other than spaces.", NotifyType.DANGER);
	                    nameLabel.setValue(CourseElementUtil.getTransitionName(courseObject));
	                    
	                    return;
	                }
	                                    
	                String name = event.getValue().trim();
	                
	                if(getCourse() != null && getCourse().getTransitions() != null){
	                    
	                    if(!GatClientUtility.isCourseObjectNameValid(name, getCourse(), courseObject)){
                            Notify.notify("Another course object is already named '"+ name +"'.", NotifyType.DANGER);
                            nameLabel.setValue(CourseElementUtil.getTransitionName(courseObject));
                            return;
                        }

	                }

	                String prevName = CourseElementUtil.getTransitionName(courseObject);
	                
	                CourseElementUtil.setTransitionName(courseObject, name);
	                
	                SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectRenamedEvent(prevName, courseObject));
	                
	                nameLabel.setValue(name);
	                
				} catch (IllegalArgumentException e) {
					logger.log(Level.SEVERE, "There was a a problem setting the name for the course object.", e);
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
				nameLabel.selectAll();
			}
		});

		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    isPinned = false;
				
				if(editorPanel != null){
				    // edit the next object if this tab is the active tab
				    boolean editNextObject = EditorTab.activeTab != null && EditorTab.activeTab.courseObject.equals(courseObject);
					editorPanel.stopEditing(courseObject, editNextObject);
				}
			}
		});

        pinTooltip.setHtml(PIN_TOOLTIP);
		pinButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(courseObject != null){
					
					if(!isPinned){

					    isPinned = true;
						pinTooltip.setHtml(UNPIN_TOOLTIP);
						
					} else {
					    isPinned = false;
						pinTooltip.setHtml(PIN_TOOLTIP);

					}
				}
			}
		});
		
		editorTab.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                editorPanel.startEditing(courseObject);
            }
        }, ClickEvent.getType());
		
		try { 
		    menuItem = new MenuItem(generateCourseObjectTabHtml(courseObject),
		            new Scheduler.ScheduledCommand() {
	                    
	                    @Override
	                    public void execute() {
	                        
	                        editorPanel.startEditing(courseObject);
	                        
	                        editorPanel.getTabMenu().hide();
	                    }
	                });
		} catch (IllegalArgumentException e) {
	    	logger.log(Level.SEVERE, "There was a a problem creating a menu item for course object.", e);
	    }
		
    }

    @Override
    public String getText() {
        return nameLabel.getText();
    }

    @Override
    public void setText(String text) {
        nameLabel.setText(text);
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
	 * display it. If false, the tab will be closed when another course object is selected
	 * for editing.
	 * 
	 * @param isPinned true if the tab is pinned
	 */
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
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
     * Gets the currently loaded {@link Course}
     * 
     * @return the loadedCourse
     */
    public static Course getCourse() {
        return loadedCourse;
    }

    /**
     * Sets the currently loaded {@link Course}
     * 
     * @param course the loaded course
     */
    public static void setCourse(Course course) {
        EditorTab.loadedCourse = course;
    }

    /**
     * Gets the {@link Serializable} courseObject
     * 
     * @return the courseObject
     */
    public Serializable getCourseObject() {
        return courseObject;
    }
    
    /**
     * Get the index of the course object being edited in this editor.
     * 
     * @return the index in the course tree of the GAT for the currently selected course object.
     * If the index can't be determined, -1 will be returned.
     */
    public int getCourseObjectIndex(){
        
        try{
            return loadedCourse.getTransitions().getTransitionType().indexOf(courseObject);
        }catch(@SuppressWarnings("unused") Exception e){
            //failed to get index
        }
        
        return -1;
    }

    /**
     * Gets the {@link AbstractCourseObjectEditor} used to edit the course object when this tab is selected
     * 
     * @return the editor
     */
    public AbstractCourseObjectEditor<?> getEditor() {
        return editor;
    }

    /**
     * Sets the {@link AbstractCourseObjectEditor} that will be displayed when this tab is selected for editing
     * 
     * @param editor the editor to set
     */
    public void setEditor(AbstractCourseObjectEditor<?> editor) {
        this.editor = editor;
    }

    /**
     * Gets the {@link MenuItem} used to represent this tab in a {@link CourseObjectEditorPanel}'s tab menu once it has been collapsed.
     * 
     * @return the menuItem
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
	 * Generates the HTML to use for a course object's accompanying tab item in the tab selection menu
	 * 
	 * @param courseObject the course object for whom HTML is being generated
	 * @return the HTML for the given course's objects tab item
	 */
	private SafeHtml generateCourseObjectTabHtml(Serializable courseObject) {
		return new SafeHtmlBuilder()
				.appendHtmlConstant("<table><tr><td><img src='"
						+ CourseElementUtil.getTypeIcon(courseObject)
						+ "' style='width: 24px; height: 24px; margin-right: 5px;'/></td><td>"
				)
				.appendEscaped(CourseElementUtil.getTransitionName(courseObject))
				.appendHtmlConstant("</td></tr></table>").toSafeHtml();
	}

	/**
	 * Sets whether a tab is the active tab being focused on, changing it's css values and updating the currently active tab
	 * 
	 * @param active - true if the tab is the active tab being edited
	 */
	public void setActive(boolean active) {

        nameLabel.setEnabled(active);
	    
	    if (active) {
	        if (!this.equals(activeTab) && activeTab != null) {
	            activeTab.setActive(false);
	        }
	        activeTab = this;
	        editorTab.removeStyleName(style.inActive());
	        nameLabel.removeStyleName(style.nameLabelDisabled());
	        nameTooltip.setTitle("Click to edit");
	        removeButton.removeStyleName(style.inActive());
	    } else {
	        if (this.equals(activeTab)) {
	            activeTab = null;
	        }
	        editorTab.addStyleName(style.inActive());
	        nameLabel.addStyleName(style.nameLabelDisabled());
	        nameTooltip.setTitle("");
	        removeButton.addStyleName(style.inActive());
	    }
	}
	
	/**
	 * Redraws the course object data that is shown by this widget so that it matches the state of the underlying
	 * course object. This includes the course object's name and its type icon.
	 */
	public void redraw() {
	    
	    try {
            setText(CourseElementUtil.getTransitionName(courseObject));
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "There was a problem redrawing the name for a course object.", e);
        }

        objectIcon.setUrl(CourseElementUtil.getTypeIcon(courseObject));
	}
	
}
