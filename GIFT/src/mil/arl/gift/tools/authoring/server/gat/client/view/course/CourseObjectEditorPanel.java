/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.AAR;
import generated.course.AuthoredBranch;
import generated.course.Course;
import generated.course.Guidance;
import generated.course.LessonMaterial;
import generated.course.MerrillsBranchPoint;
import generated.course.PresentSurvey;
import generated.course.TrainingApplication;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLoadedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDoneEditingEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectOpenedForEditingEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRedrawEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.EditorTab;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.aar.AarEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.AuthoredBranchEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.GuidanceEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.LessonMaterialEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;

/**
 * A widget used to create, manage, and display editors for course objects.
 * 
 * @author nroberts
 */
public class CourseObjectEditorPanel extends SimpleLayoutPanel implements ProvidesResize, RequiresResize{
	
	private static final Logger logger = Logger.getLogger(CourseObjectEditorPanel.class.getName());
	
	private static CourseObjectEditorPanelUiBinder uiBinder = GWT.create(CourseObjectEditorPanelUiBinder.class);

	interface CourseObjectEditorPanelUiBinder extends UiBinder<Widget, CourseObjectEditorPanel> {
	}
	
	/**
	 * An event binder used to allow this class to handle events on the shared message bus
	 * 
	 * @author nroberts
	 */
	interface MyEventBinder extends EventBinder<CourseObjectEditorPanel>{
	}
	
	/** The event binder associated with this instance */
	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	/**
	 * The different orientations that can be used by this panel
	 * 
	 * @author nroberts
	 */
	public enum Orientation{
		RIGHT,
		BOTTOM,
		FULLSCREEN
	}
	
	@UiField
	protected Button layoutButton;
	
	@UiField
	protected Button fullScreenButton;
	
	@UiField
	protected Button tabButton;
	
	@UiField
	protected DeckPanel editorDeck;
	
	@UiField
	protected HeaderPanel mainPanel;
	
	@UiField
	protected HorizontalPanel tabsPanel;
	
	@UiField
	protected Widget loadingIndicator;

	@UiField
	protected HTML loadingText;
	
	@UiField
	protected BsLoadingIcon loadingIcon;

	/** A mapping from each opened course object to the editor tab object */
	private Map<Serializable, EditorTab> objectToTabItem = new HashMap<Serializable, EditorTab>(); 
	
	/** The course object currently being shown for editing */
	private Serializable objectBeingEdited;
	
	/** The orientation this widget is currently using */
	private Orientation currentOrientation = Orientation.RIGHT;
	
	/** The last orientation this widget was using */
	private Orientation lastOrientation = null;
	
	/** The context menu used to allow the user to select a course object to display from the list of currently opened course objects */
	private ContextMenu tabMenu = new ContextMenu();
	
	/**
	 * Gets the tabMenu that is shown only when there are more pinned tabs than can be displayed within the tab panel's width 
	 */
	public ContextMenu getTabMenu() {
        return tabMenu;
    }

    /** This widget's display handler, if one has been assigned */
	private DisplayHandler displayHandler = null;
	
	/** The currently loaded course */
	private Course course = null;
	
	/** 
	 * Creates a new panel with no editors
	 */
	public CourseObjectEditorPanel() {
		setWidget(uiBinder.createAndBindUi(this));	
		
		exposeNativeFunctions();
		
		eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
		
		layoutButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				loadingText.setText("Changing layout...");
				loadingIndicator.setVisible(true);

				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override

					public void execute() {

						if(Orientation.RIGHT.equals(currentOrientation)){
							setOrientation(Orientation.BOTTOM);

						} else {
							setOrientation(Orientation.RIGHT);
						}

						loadingIndicator.setVisible(false);
					}

				});
			}
		});
		
		fullScreenButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				loadingText.setText("Changing layout...");
				loadingIndicator.setVisible(true);

				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						
						if(Orientation.FULLSCREEN.equals(currentOrientation)){
							
							//return to the previous orientation
							if(lastOrientation != null){
								setOrientation(lastOrientation);

							} else {
								setOrientation(Orientation.RIGHT);
							}

						} else {

							//switch to full screen orientation
							setOrientation(Orientation.FULLSCREEN);
						}

						loadingIndicator.setVisible(false);
					}
				});	
			}
		});
		
		tabButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				tabMenu.showAtCurrentMousePosition(event);
			}
		});
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent arg0) {
				CourseObjectEditorPanel.this.onResize();
			}
		});
	}
	
	/**
	 * Creates a new panel with no editors whose display properties can be modified by the given {@link DisplayHandler}.
	 * Using a display handler allows this widget's creator to respond to user input with this widget's layout controls, 
	 * which can, in turn, allows this widget to modify its orientation and visibility based on users' actions.
	 * 
	 * @param displayHandler
	 */
	public CourseObjectEditorPanel(DisplayHandler displayHandler){
		this();
		
		this.displayHandler = displayHandler;
	}
	
	/**
	 * Displays an editor modifying the given course object. If the given course object has not been opened for editing before, then
	 * this method will also create an editor modifying the course object. Otherwise, this method simply displays the editor that 
	 * was already created for this course object the first time it was shown.
	 * 
	 * @param courseObject the course object to edit
	 */
	public void startEditing(final Serializable courseObject){
		
		logger.info("Loading course object for editing.");
		
		if(courseObject != null){
			
			EditorTab initialTab = objectToTabItem.get(courseObject);
			
		    if (initialTab == null) {
		    	initialTab = new EditorTab(courseObject, this);
		    }
		    
		    final EditorTab tab = initialTab;
		    
		    tab.setActive(true);

		    if (tab.isCollapsed()) {
		    	
		    	//if this tab was collapsed, expand it
		        removeFromTabMenu(tab);
		        updateTabCount();
		    }
		    
		    if(tabsPanel.getWidgetIndex(tab) == -1){
		    	
		    	//add this object's tab to the editor panel if it hasn't been added already
		    	tabsPanel.add(tab);
		    }
		    
            objectToTabItem.put(courseObject, tab);			
			
			if(tab.getEditor() == null){
				
				// get a list of tabs to unpin first to prevent concurrent modification
	            ArrayList<EditorTab> tabsToUnpin = new ArrayList<EditorTab>();
	            for(EditorTab unpinnedTab : objectToTabItem.values()){
	                if (!unpinnedTab.equals(EditorTab.getActiveTab()) && !unpinnedTab.isPinned()) {
	                    tabsToUnpin.add(unpinnedTab);
	                }
	            }
				
	            // stop editing any unpinned course objects
	            for(EditorTab unpinnedTab : tabsToUnpin){
	                if (!unpinnedTab.equals(EditorTab.getActiveTab())) {
	                    stopEditing(unpinnedTab.getCourseObject(), false);
	                }
	            }
			}
			
			//set this course object as the one actively being edited
			objectBeingEdited = courseObject;	
			
			//need to set the orientation first to make the editor panel visible immediately
			setOrientation(currentOrientation);		
			
			//notify the course presenter when a course object is being edited so it can select it in the course tree
			SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectOpenedForEditingEvent(courseObject));
			
			//allow the editor panel to be attached to the page before continuing
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
					
					updateTabCount();
					
					//allow the loading indicator to be displayed before continuing
					loadingText.setText("Loading...");
					loadingIndicator.setVisible(true);
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							
							if(tab.getEditor() == null){
				                
								//this course object hasn't been opened for editing yet, so we need to initialize its editor.
								if(courseObject instanceof AAR){
									
									AarEditor myEditor = new AarEditor();					
									myEditor.edit((AAR) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for AAR object");
									
								} else if(courseObject instanceof LessonMaterial){
									
									logger.info("Creating a new editor for LessonMaterial object");
									
									LessonMaterialEditor myEditor = new LessonMaterialEditor(course);					
									myEditor.edit((LessonMaterial) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for LessonMaterial object");
									
								} else if(courseObject instanceof PresentSurvey){
									
									logger.info("Creating a new editor for PresentSurvey object");
									
									PresentSurveyEditor myEditor = new PresentSurveyEditor(course);					
									myEditor.edit((PresentSurvey) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for PresentSurvey object");
									
								} else if(courseObject instanceof Guidance){
									
									logger.info("Creating a new editor for Guidance object");
									
									GuidanceEditor myEditor = new GuidanceEditor();					
									myEditor.edit((Guidance) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for Guidance object");
								
								} else if(courseObject instanceof MerrillsBranchPoint){
									
									logger.info("Creating a new editor for MerrillsBranchPoint object");
									
									MbpEditor myEditor = new MbpEditor(course);					
									myEditor.edit((MerrillsBranchPoint) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for MerrillsBranchPoint object");
									
								} else if(courseObject instanceof TrainingApplication){
									
									logger.info("Creating a new editor for TrainingApplication object");
									
									TrainingAppEditor myEditor = new TrainingAppEditor();					
									myEditor.edit((TrainingApplication) courseObject);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
				                    logger.info("Finished creating editor for TrainingApplication object");
									
								} else if(courseObject instanceof AuthoredBranch){
									
								    AuthoredBranch authoredBranch = (AuthoredBranch) courseObject;
									logger.info("Creating a new editor for AuthoredBranch object named "+authoredBranch.getTransitionName());
									
									AuthoredBranchEditor myEditor = new AuthoredBranchEditor();					
									myEditor.edit(authoredBranch);					
									myEditor.setSize("100%", "100%");
									
				                    tab.setEditor(myEditor);
									
									logger.info("Finished creating editor for AuthoredBranch object named "+authoredBranch.getTransitionName());
								}
								
				                if(tab.getEditor() != null){
									
									//add the editor to this widget
				                    editorDeck.add(tab.getEditor());											
									
								} else {
									
									logger.severe("Failed to load a course object for editing. The class '"
											+ courseObject.getClass().getName() + "' does not have an accompanying editor.");
									
									return;
								}
							}   
							
				            // add the editor to the editor deck if it isn't there already
				            if (editorDeck.getWidgetIndex(tab.getEditor()) == -1) { 
				                editorDeck.add(tab.getEditor());
				            }

							//show the editor for this course object
				            editorDeck.showWidget(editorDeck.getWidgetIndex(tab.getEditor()));
							
				            if(tab.getEditor() instanceof RequiresResize){
								
								//resize the editor once it is shown, if necessary
				                ((RequiresResize) tab.getEditor()).onResize();
				            }
							
				            if(objectToTabItem.size() == 1){
								
								//if this is the only object opened for editing, show this widget so the objec's editor is visible
								setOrientation(currentOrientation);
							}
							
							loadingIndicator.setVisible(false);
							
							//notify the course presenter when a course object is being edited so it can select it in the course tree
							SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectOpenedForEditingEvent(courseObject));
						}
					});
				}
			});
			
		} else {
			logger.severe("Failed to load a course object for editing. The course object to edit is null.");
		}
	}
	
	/**
	 * Get the index of the current course object being edited.
	 * 
	 * @return the index in the course tree of the GAT for the currently selected course object.
	 * If there is no currently selected course object -1 will be returned.
	 */
	public int getEditingCourseObjectIndex(){
	    
	    if(objectBeingEdited != null){
	        
	        EditorTab editorTab = objectToTabItem.get(objectBeingEdited);
	        if(editorTab != null){
	            
	            return editorTab.getCourseObjectIndex();
	        }
	    }
	    
	    return -1;
	}

	/**
	 * Removes the editor modifying the given course object. If other course objects have been opened for editing, then this 
	 * method will switch to the editor for the next available course object. Otherwise, this editor will remove itself from its 
	 * parent once no more course objects are opened for editing.
	 * 
	 * @param courseObject the course object whose editor should be removed
	 */
	public void stopEditing(Serializable courseObject){
	    stopEditing(courseObject, true);
	}
	
	/**
     * Removes the editor modifying the given course object, and optionally starts editing the next available course object if one exists.
     * If no other course objects are available or if editing the next course object is disabled, this editor will simply remove itself 
     * from its parent.
     * 
     * @param courseObject the course object whose editor should be removed
     * @param shouldEditNext whether or not the next available course object should be opened
     */
    public void stopEditing(Serializable courseObject, boolean shouldEditNext) {
        stopEditing(courseObject, shouldEditNext, false);
    }

	/**
	 * Removes the editor modifying the given course object, and optionally starts editing the next available course object if one exists.
	 * If no other course objects are available or if editing the next course object is disabled, this editor will simply remove itself 
	 * from its parent.
	 * 
	 * @param courseObject the course object whose editor should be removed
	 * @param shouldEditNext whether or not the next available course object should be opened
	 * @param forceIfPinned whether or not editing should be stopped on the course object even if it is pinned
	 * to the editor. This should generally be avoided to let the user control when pinned course objects are 
	 * removed from the editor panel, but there are some edge cases where forcing closure is acceptable, such as
	 * when a course object is deleted.
	 */
	public void stopEditing(Serializable courseObject, boolean shouldEditNext, boolean forceIfPinned) {

		EditorTab tab = objectToTabItem.get(courseObject);

		if (tab != null) {
		    
		    if(tab.isPinned() && !forceIfPinned) {
	            
	            /* Avoid removing pinned course objects from the editor panel unless an operation EXPLICITLY 
	             * requires that editing should be stopped */
	            return;
	        }

			tab.setActive(false);

			AbstractCourseObjectEditor<?> editor = tab.getEditor();

			if (editor != null) {

				// stop any presenter this course object's editor may be using
			    logger.info("Stop editing, called stopEditing() on current editor");
				editor.stopEditing();

				if (editor instanceof TrainingAppEditor) {
					((TrainingAppEditor) editor).setChoiceSelectionListener(null);
				}

				if (editorDeck.getWidgetCount() > 1 && shouldEditNext) {

					// if there are more course objects open for editing, find
					// the next available one
					int index = editorDeck.getWidgetIndex(editor);

					Serializable nextObject = null;

					if (index < editorDeck.getWidgetCount() - 1) {

						Widget widget = editorDeck.getWidget(index + 1);

						if (widget instanceof AbstractCourseObjectEditor<?>) {
							nextObject = ((AbstractCourseObjectEditor<?>) widget).getCourseObject();
						}

					} else {

						Widget widget = editorDeck.getWidget(index - 1);

						if (widget instanceof AbstractCourseObjectEditor<?>) {
							nextObject = ((AbstractCourseObjectEditor<?>) widget).getCourseObject();
						}
					}

					// start editing the next available course object
					startEditing(nextObject);

					// remove the editor for the course object being removed
					editorDeck.remove(editor);

				} else {

					// remove the editor for the course object being removed
					editorDeck.remove(editor);
				}
			}

			// if the tab is not pinned, remove it from the editor
			objectToTabItem.remove(courseObject);
			tabsPanel.remove(tab);
			removeFromTabMenu(tab);
		}

		updateTabCount();

		// if there aren't any more course objects open for editing, remove this
		// widget from its parent so it isn't shown
		if (objectToTabItem.size() < 1) {
			updateVisibility(false);

			// notify the course presenter when a course object is done being
			// edited so it can deselect it in the course tree
			SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDoneEditingEvent());
		}
	}

	@Override
	public void onResize() {
		super.onResize();
		
		updateTabCount();
	}
	
	/**
	 * Changes this widget's orientation with respect to its parent. This can be used to change this widget from a horizontal orientation
	 * to a horizontal one and vice-versa.
	 * 
	 * @param orientation
	 */
	private void setOrientation(Orientation orientation){
			
		if(orientation.equals(Orientation.BOTTOM)){				
			
			layoutButton.setEnabled(true);
			layoutButton.setIcon(IconType.TOGGLE_RIGHT);
			
			fullScreenButton.setIcon(IconType.ARROWS_ALT);
			
		} else if(orientation.equals(Orientation.FULLSCREEN)){
			
			layoutButton.setEnabled(false);
			
			fullScreenButton.setIcon(IconType.COMPRESS);
					
		} else {
			
			layoutButton.setEnabled(true);
			layoutButton.setIcon(IconType.TOGGLE_DOWN);
			
			fullScreenButton.setIcon(IconType.ARROWS_ALT);
		} 
		
		if(this.currentOrientation != null && !this.currentOrientation.equals(orientation)){
			this.lastOrientation = currentOrientation;
		}
		
		this.currentOrientation = orientation;
		
		updateOrientation(orientation);		
		
	}
	
	/**
	 * Removes a tab from the editor panel and collapses it to the tab menu
	 * 
	 * @param tab - the tab to be collapsed
	 * @throws IllegalArgumentException - if the tab is null
	 */
	private void addToTabMenu(EditorTab tab) {
		
	    if (tab == null) {
	        throw new IllegalArgumentException("tab cannot be null");
	    }
	   
	    tab.setActive(false);
	    tab.setVisible(false);
	    tab.setCollapsed(true);
	    
	    if (tab.getMenuItem() != null ) {
	    	
	    	if(tabMenu.getMenu().getItemIndex(tab.getMenuItem()) == -1){
	    	
	    		//add this tab if the tab menu does not contain it
	            tabMenu.getMenu().addItem(tab.getMenuItem());
	            EditorTab.setTabsCollapsed(EditorTab.getTabsCollapsed() + 1);
	    	}
	    	
	    } else {
	        logger.warning("tab menuItem was null");
	    }
	}
	
	/**
	 * Removes a tab from the tab menu and displays it on the editor panel
	 * 
	 * @param tab - tab to be removed from the tab menu
	 * @throws IllegalArgumentException - if the tab is null
	 */
	private void removeFromTabMenu(EditorTab tab) {
		
	    if (tab == null) {
	        throw new IllegalArgumentException("tab cannot be null");
	    }
	   
	    tab.setVisible(true);
	    tab.setCollapsed(false);
	    
	    if (tab.getMenuItem() != null) {
	    	
	    	if(tabMenu.getMenu().getItemIndex(tab.getMenuItem()) != -1){
	    		
	    		//remove this tab if the tab menu contains it
	            tabMenu.getMenu().removeItem(tab.getMenuItem());
	            EditorTab.setTabsCollapsed(EditorTab.getTabsCollapsed() - 1);
	    	}
	    	
	    } else {
	        logger.warning("tab menuItem was null");
	    }
	}
	
	/**
	 * Updates the counter for the button used to open the tab menu and displays tabs based on space available
	 */
	private void updateTabCount(){
	    int tabCount = objectToTabItem.size();
        int tabsInMenu = EditorTab.getTabsCollapsed();
		
	    // Calculate tabRoom based on space available
	    int offsetWidth = editorDeck.getOffsetWidth() - 6; // 3px padding on each side
	    int tabWidth = (EditorTab.getActiveTab() != null ? EditorTab.getActiveTab().getOffsetWidth() : 244); // assume 244 tab width
	    int tabButtonWidth = tabButton.getOffsetWidth();
	    int tabRoom = offsetWidth / tabWidth;
	    if (tabCount > tabRoom && tabCount > 1) {
	        tabRoom = (offsetWidth - tabButtonWidth) / tabWidth;
	    }
	    
	    int displayedTabs = tabCount - tabsInMenu;

	    // if we need to add a tab...
	    if (displayedTabs < tabRoom && displayedTabs < tabCount) {
            
	        // display the active tab if it is not already, or the next available tab
            EditorTab tabToDisplay = null;
            
            if (EditorTab.getActiveTab().isCollapsed()) {
                tabToDisplay = EditorTab.getActiveTab();
                
            } else {
                for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
                    EditorTab tab = (EditorTab) tabsPanel.getWidget(i);
                    if (!tab.equals(EditorTab.getActiveTab()) && tab.isCollapsed()) {
                        tabToDisplay = tab;
                        break;
                    }
                }
            }

            if (tabToDisplay != null) {
            	
                removeFromTabMenu(tabToDisplay);
                updateTabCount();
            }
            
	    } else if (displayedTabs > tabRoom && displayedTabs > 1) {
	    	
	        // if we need to remove a tab
	        EditorTab tabToHide = null;
	        
	        for (int i = tabsPanel.getWidgetCount() - 1; i >= 0; i--) {
	            EditorTab tab = (EditorTab) tabsPanel.getWidget(i);
	            if (!tab.equals(EditorTab.getActiveTab()) && !tab.isCollapsed()) {
	                tabToHide = tab;
	                break;
	            }
	        }
	        
	        if (tabToHide != null) {
	        	
                addToTabMenu(tabToHide);
                updateTabCount();
	        }
	    }

		if(tabCount <= tabRoom || EditorTab.getTabsCollapsed() == 0){
			tabButton.setVisible(false);
			
		} else {
			tabButton.setVisible(true);
		}
		
		tabButton.setText(Integer.toString(EditorTab.getTabsCollapsed()));
	}
	
	/**
	 * Notifies this widget's assigned display handle to update its size and positioning based on the given orientation
	 * 
	 * @param orientation the orientation this widget should use
	 */
	public void updateOrientation(Orientation orientation){
		
		if(displayHandler != null){
			displayHandler.updateOrientation(orientation);
		}
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                updateTabCount();
	}
	
		});
	}
	
	/**
	 * Notifies this widget's assigned display handle to update its visibility
	 * 
	 * @param visible whether or not this widget should be visible
	 */
	private void updateVisibility(boolean visible){
		
		if(displayHandler != null){
			displayHandler.updateVisibility(visible);
		}
	}
	
	/**
	 * Stops editing all the course object that have been opened for editing by this widget and returns this widget
	 * to its initial state. Only display information, such as the current orientation, will be retained after
	 * invoking this method.
	 */
	public void stopAllEditing(){
		
		Set<EditorTab> toClose = new HashSet<EditorTab>(objectToTabItem.values());
		
		for(EditorTab tab : toClose){
			stopEditing(tab.getCourseObject(), false, true);
		}
				
		objectBeingEdited = null;
		
	}
	
	@EventHandler
	protected void onCourseLoaded(CourseLoadedEvent event){
		
		if(event.getCourse() != null){
			this.course = event.getCourse();
			EditorTab.setCourse(course);
		}
	}
	
	@EventHandler
    protected void onCourseObjectRedraw(CourseObjectRedrawEvent event){
	    
	    //find the tab corresponding to the course object that needs to be redrawn and redraw it
	    EditorTab tabToUpdate = objectToTabItem.get(event.getCourseObject());
	    if(tabToUpdate != null) {
	        tabToUpdate.redraw();
	    }
	}
	
	/**
	 * An object that manages the display properties of a {@link CourseObjectEditorPanel}.
	 * 
	 * @author nroberts
	 */
	public interface DisplayHandler{		
		
		/**
		 * Updates the size and positioning of a {@link CourseObjectEditorPanel} based on the given orientation.
		 * 
		 * @param orientation the orientation to use
		 */
		public void updateOrientation(Orientation orientation);
		
		/**
		 * Updates the visibility of a {@link CourseObjectEditorPanel}.
		 * 
		 * @param visible whether or not the {@link CourseObjectEditorPanel} should be visible
		 */
		public void updateVisibility(boolean visible);
	}

	/**
	 * Reloads the specified training application if it has been opened for editing. This is useful for working with GIFT Wrap, 
	 * since it allows new training application data to be reloaded from another source.
	 * 
	 * @param ta the training application to reload
	 */
	public void reloadTrainingApplication(TrainingApplication ta) {
	    EditorTab tab = objectToTabItem.get(ta);
		
	    if (tab != null) {
            AbstractCourseObjectEditor<?> editor = tab.getEditor();
		
    		if(editor != null && editor instanceof TrainingAppEditor){
    			
    			((TrainingAppEditor) editor).edit(ta);
    		}
    	}
    }
	
	/**
	 * Gets the name of the course object currently being edited (i.e. the tab that currently has focus)
	 * 
	 * @return the name of the course object being edited
	 */
	public String getCurrentObjectName() {
	    
	    if(objectBeingEdited != null) {
	        return CourseElementUtil.getTransitionName(objectBeingEdited);
	        
	    } else {
	        return null;
	    }
	}
	
	 /**
     * Defines JavaScript methods used to invoke logic on this this widget from native function calls
     */
    private native void exposeNativeFunctions()/*-{
    
        var that = this;
    
        $wnd.getCurrentCourseObjectName = $entry(function(){
            return that.@mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseObjectEditorPanel::getCurrentObjectName()();
        });
        
    }-*/;
}
