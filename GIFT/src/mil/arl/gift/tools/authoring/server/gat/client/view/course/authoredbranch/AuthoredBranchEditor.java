/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.BooleanEnum;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.authoredbranch.BranchSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SetNameDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * An editor that modifies {@link AuthoredBranch} course objects.
 * 
 * @author nroberts
 */
public class AuthoredBranchEditor extends AbstractCourseObjectEditor<AuthoredBranch> implements CourseReadOnlyHandler {

	private static AuthoredBranchEditorUiBinder uiBinder = GWT.create(AuthoredBranchEditorUiBinder.class);
	
	/** The event bus. */
	protected EventBus eventBus = SharedResources.getInstance().getEventBus();	

	/** The event registration. */
	protected HandlerRegistration eventRegistration;
    
    /**
     * The Interface MyEventBinder.
     */
    interface MyEventBinder extends EventBinder<AuthoredBranchEditor> {
    }
    
    /** The Constant eventBinder. */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private static int radioButtonNameIncrementor = 0;

	interface AuthoredBranchEditorUiBinder extends UiBinder<Widget, AuthoredBranchEditor> {
	}
	
	@UiField
	protected DynamicHeaderScrollPanel splitter;
	
	@UiField(provided=true)
	protected RadioButton balancedDistRadio;
	
	@UiField(provided=true)
	protected RadioButton randomDistRadio;
	
	@UiField(provided=true)
	protected RadioButton customDistRadio;
	
	// Chris: disabled for now until implemented in the future
	//	@UiField(provided=true)
	protected RadioButton ruleDistRadio;
	
    /** The disable course object checkbox. */
    @UiField
    protected CheckBox disabled;
	
	@UiField(provided=true)
	protected ValueListBox<Path> defaultBranchListBox = new ValueListBox<Path>(new Renderer<Path>() {

		@Override
		public String render(Path value) {
			
			if(value != null && value.getName() != null){
				return value.getName();
			}
			
			return null;
		}

		@Override
		public void render(Path value, Appendable appendable) throws IOException {
			
			appendable.append(render(value));
		}
	});
	
	@UiField
	protected FlowPanel pathContainer;
	
	@UiField
	protected Button addPathButton;
	

	/** The branch currently being edited*/
	private AuthoredBranch currentBranch;
	
	
	/**
	 * Creates a new editor for editing {@link AuthoredBranch} course objects.
	 */
	public AuthoredBranchEditor() {
		
		String distributionRadioName = generateDistribitionRadioButtonName();
		
		balancedDistRadio = new RadioButton(distributionRadioName, "Balanced");
		randomDistRadio = new RadioButton(distributionRadioName, "Random");
		customDistRadio = new RadioButton(distributionRadioName, "Custom Percent");
		ruleDistRadio = new RadioButton(distributionRadioName, "Rule-Based");
		
		setWidget(uiBinder.createAndBindUi(this));
        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);

		init();
	}
		
	/**
	 * Sets up handlers for this widget's UI elements and performs any additional work needed to get each element ready
	 * for user interaction
	 */
	private void init(){

        eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
		
		addPathButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				final SetNameDialog enterNameDialog = new SetNameDialog(
						"New Path Name", 
						"Please enter a unique name to describe this path.", 
						"Create"
				);
				
				enterNameDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
					
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						
						String name = event.getValue();
						
						if(name != null){
							name = name.trim();
						}
						
						if(isPathNameValid(name, null)){
							
							Path path = addNewPath();
							path.setName(name);
							
							final PathWidget pathWidget = new PathWidget(path, AuthoredBranchEditor.this);
							
							pathContainer.add(pathWidget);
							
							enterNameDialog.hide();
							
							refreshPathDisplay();
							
							updateDisplay();
							
							SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(currentBranch));
							
						} else {
							WarningDialog.error("Invalid value", "Path names cannot be empty and must be unique.");
						}

					}
				});
				
				enterNameDialog.center();
			}
		});
		
		final FlowPanel finalPathContainer = pathContainer;

        balancedDistRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue().booleanValue()) {

                    AuthoredBranch.SimpleDistribution balancedDistribution = new AuthoredBranch.SimpleDistribution();
                    balancedDistribution.setRandomOrBalancedOrCustom(new AuthoredBranch.SimpleDistribution.Balanced());
                    currentBranch.setSimpleDistribution(balancedDistribution);

                    for(int i=0; i<finalPathContainer.getWidgetCount(); i++){
                        ((PathWidget)finalPathContainer.getWidget(i)).setCustomDistVisible(!event.getValue().booleanValue());
                    }
                
                    refreshPathDisplay();
                }
            }
            
        });

        randomDistRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue().booleanValue()) {
                    AuthoredBranch.SimpleDistribution randomDistribution = new AuthoredBranch.SimpleDistribution();
                    randomDistribution.setRandomOrBalancedOrCustom(new AuthoredBranch.SimpleDistribution.Random());
                    currentBranch.setSimpleDistribution(randomDistribution);
                
                    for(int i=0; i<finalPathContainer.getWidgetCount(); i++){
                        ((PathWidget)finalPathContainer.getWidget(i)).setCustomDistVisible(!event.getValue().booleanValue());
                    }
                
                    refreshPathDisplay();
                }
            }
            
        });

        customDistRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if (event.getValue().booleanValue()) {
                    AuthoredBranch.SimpleDistribution customDistribution = new AuthoredBranch.SimpleDistribution();
                    customDistribution.setRandomOrBalancedOrCustom(new AuthoredBranch.SimpleDistribution.Custom());
                    currentBranch.setSimpleDistribution(customDistribution);
                }
                
                for(int i=0; i<finalPathContainer.getWidgetCount(); i++){
                    ((PathWidget)finalPathContainer.getWidget(i)).setCustomDistVisible(event.getValue().booleanValue());
                }
                
                refreshPathDisplay();
            }
            
        });
        
        defaultBranchListBox.addValueChangeHandler(new ValueChangeHandler<Path>() {

            @Override
            public void onValueChange(ValueChangeEvent<Path> event) {
                
                currentBranch.setDefaultPathId(event.getValue().getPathId());
                
                for(Widget widget : pathContainer){
                    if(widget instanceof PathWidget){
                        PathWidget pathWidget = (PathWidget) widget;
                        if (pathWidget.getPath().equals(event.getValue())) {
                            pathWidget.setDefaultPath(true);
                        } else {
                            pathWidget.setDefaultPath(false);
                        }
                    }
                }
            }
            
        });
        
        disabled.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentBranch != null){        
                    
                    currentBranch.setDisabled(event.getValue() != null && event.getValue()
                            ? BooleanEnum.TRUE
                            : BooleanEnum.FALSE
                    );
                                        
                    eventBus.fireEvent(new EditorDirtyEvent());
                                        
                    SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentBranch));
                }
            }
        });
	}

	/**
	 * Adds a new path to the branch currently being edited. Note that this only affects the editor's underlying data and doesn't update
	 * the UI in any way.
	 */
	protected Path addNewPath() {
		
		Path path = new Path();
		path.setPathId(getNextPathId());
		
		currentBranch.getPaths().getPath().add(path);
		
		return path;
	}

	@Override
	protected void editObject(AuthoredBranch courseObject) {
		
		this.currentBranch = courseObject;
		
		updateDisplay();
	}
	
	/**
	 * Updates this widget's UI elements to match the current state of the object being edited.
	 */
	private void updateDisplay(){
		
		if(currentBranch != null){
			
			if(currentBranch.getPaths() == null){
				currentBranch.setPaths(new Paths());
			}
			
			pathContainer.clear();
			
			if(currentBranch.getPaths().getPath().isEmpty()){
				
				//make sure there is at least one path available at all times
				Path path = addNewPath();				
				path.setName("Initial Path");
			}		
			
			for(Path path : currentBranch.getPaths().getPath()){
				
				if(path.getPathId() == null){
					
					//if this path somehow doesn't have an ID associated with it yet, generate one for it
					path.setPathId(getNextPathId());
				}
				
				final PathWidget pathWidget = new PathWidget(path, AuthoredBranchEditor.this);
				
				pathContainer.add(pathWidget);
			}
			
			Path defaultPath = null;
			if(currentBranch.getDefaultPathId() == null){
				
				//if no default path is set, find the first path available and use it as the default
				defaultPath = currentBranch.getPaths().getPath().get(0);
				
				currentBranch.setDefaultPathId(defaultPath.getPathId());				
			}

			if (defaultPath == null) {
			    for (Path path : currentBranch.getPaths().getPath()) {
			        if (path.getPathId().equals(currentBranch.getDefaultPathId())) {
			            defaultPath = path;
			            break;
			        }
			    }
			}
			
			// Update the UI to show the default path
			defaultBranchListBox.setValue(defaultPath);
			defaultBranchListBox.setAcceptableValues(currentBranch.getPaths().getPath());
			
			if (currentBranch.getSimpleDistribution() == null) {
			    AuthoredBranch.SimpleDistribution defaultDistribution = new AuthoredBranch.SimpleDistribution();
			    defaultDistribution.setRandomOrBalancedOrCustom(new AuthoredBranch.SimpleDistribution.Balanced());
			    currentBranch.setSimpleDistribution(defaultDistribution);
			}

			// Update the UI to show the distribution method
			Serializable distribution = currentBranch.getSimpleDistribution().getRandomOrBalancedOrCustom();
			if (distribution instanceof AuthoredBranch.SimpleDistribution.Random) {
			    randomDistRadio.setValue(true, true);
			} else if (distribution instanceof AuthoredBranch.SimpleDistribution.Balanced) {
			    balancedDistRadio.setValue(true, true);
			} else if (distribution instanceof AuthoredBranch.SimpleDistribution.Custom) {
			    customDistRadio.setValue(true, true);

                for(int i=0; i<pathContainer.getWidgetCount(); i++){
                    ((PathWidget)pathContainer.getWidget(i)).setCustomDistVisible(true);
                }
			}
			
			disabled.setValue(currentBranch.getDisabled() == BooleanEnum.TRUE);
			
            boolean readOnly = GatClientUtility.isReadOnly();
            randomDistRadio.setEnabled(!readOnly);
            balancedDistRadio.setEnabled(!readOnly);
            customDistRadio.setEnabled(!readOnly);
			defaultBranchListBox.setEnabled(!readOnly);
			addPathButton.setVisible(!readOnly);
			disabled.setEnabled(!readOnly);
			
			refreshPathDisplay();
		}
		
	}

	private String generateDistribitionRadioButtonName(){
		
		if(radioButtonNameIncrementor < Integer.MAX_VALUE){
			radioButtonNameIncrementor++;
			
		} else {
			radioButtonNameIncrementor = 0;
		}
		
		return "AuthoredBrachEditor-Distribitution-" + radioButtonNameIncrementor;
	}

	/**
	 * Removes the path corresponding to the given path widget from the branch currently being edited. If the path being removed 
	 * is the default branch, then this method will also assign another branch as the default in its place.
	 * 
	 * @param pathWidget the path widget triggering the removal
	 */
	void onRemovePath(PathWidget pathWidget) {
		
		if(pathWidget != null 
				&& currentBranch != null
				&& currentBranch.getPaths().getPath().size() > 1){
			
			Path path = pathWidget.getPath();
			
			int pathIndex = currentBranch.getPaths().getPath().indexOf(path);
			
			//remove the path corresponding to the given widget
			currentBranch.getPaths().getPath().remove(path);
			pathContainer.remove(pathWidget);
			
			if(currentBranch.getDefaultPathId() != null 
					&& currentBranch.getDefaultPathId().equals(path.getPathId())){
				
				//the default path was deleted, so we need to assign a new default path
				if(pathIndex != -1){
					
					if(pathIndex < currentBranch.getPaths().getPath().size()){
						
						//if there was a path after the one that was removed, use it as the default
						currentBranch.setDefaultPathId(currentBranch.getPaths().getPath().get(pathIndex).getPathId());
						
					} else {
						
						//otherwise, use the path before it as the default 
						currentBranch.setDefaultPathId(currentBranch.getPaths().getPath().get(pathIndex - 1).getPathId());
					}
					
				} else {
					
					//if the default path somehow wasn't in the list of paths, just default to the first path available
					currentBranch.setDefaultPathId(currentBranch.getPaths().getPath().get(0).getPathId());
				}
			}
			
			refreshPathDisplay();
			
			//update the tree for this branch
			SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(currentBranch));
		}
	}
	
    @EventHandler
    protected void onBranchSelected(BranchSelectedEvent event) {
        updateDisplay();
    }

	/**
	 * Checks whether or not the given path name is valid by ensuring that it has at least one character and doesn't match 
	 * any existing path names.
	 * 
	 * @param name the path name to check
	 * @param pathToRename an optional parameter indicating the path being renamed, assuming there is one
	 * @return whether or not the given path name is valid
	 */
	boolean isPathNameValid(String name, Path pathToRename){
		
		if(StringUtils.isBlank(name)){
			return false;
		}
		
		if(currentBranch != null){
			
			for(Path path : currentBranch.getPaths().getPath()){
				
				if((pathToRename == null || !pathToRename.equals(path)) && name.equals(path.getName())){
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Updates the display state of all the paths that have been added to match the current state of their associated authored branch
	 */
	void refreshPathDisplay(){
		
		for(Widget widget : pathContainer){
			
			if(widget instanceof PathWidget){
				
				((PathWidget) widget).updateDisplay();				
			}
		}
	}
	
	/**
	 * Iterates through the currently loaded branch's existing paths and get's the path ID that should be assigned to the next
	 * new path that gets added.
	 * 
	 * @return the ID of the next path to be added
	 */
	private BigInteger getNextPathId(){
		
		BigInteger currentMaxId = BigInteger.ZERO;
		
		if(currentBranch != null){
			
			//default the maximum ID to the number of paths currently in this branch
			currentMaxId = BigInteger.valueOf(currentBranch.getPaths().getPath().size());
			
			//check to see if there's a branch with a higher ID, and if so, use it as the maximum
			for(Path path : currentBranch.getPaths().getPath()){
				
				if(path.getPathId() != null && path.getPathId().compareTo(currentMaxId) > 0){
					currentMaxId = path.getPathId();
				}
			}
		}
		
		//increment the maximum ID to get the next ID
		return currentMaxId.add(BigInteger.ONE);
	}
	
	/**
	 * Get's the selected distribution method from the editor
	 * 
	 * @return the {@link AuthoredBranch.SimpleDistribution} method
	 */
	public Serializable getDistributionMethod() {
	    if (currentBranch != null && currentBranch.getSimpleDistribution() != null) {
            return currentBranch.getSimpleDistribution().getRandomOrBalancedOrCustom();
	    } else {
	        return new AuthoredBranch.SimpleDistribution.Balanced();
	    }
	}

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		balancedDistRadio.setEnabled(!isReadOnly);
		randomDistRadio.setEnabled(!isReadOnly);
		customDistRadio.setEnabled(!isReadOnly);
		disabled.setEnabled(!isReadOnly);
		addPathButton.setVisible(!isReadOnly);
		addPathButton.setEnabled(!isReadOnly);
	}
}
