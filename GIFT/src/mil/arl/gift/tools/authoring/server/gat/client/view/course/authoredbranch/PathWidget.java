/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.AuthoredBranch.Paths.Path.Courseobjects;
import generated.course.AuthoredBranch.Paths.Path.Courseobjects.End;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.authoredbranch.BranchSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * A widget used in conjunction with {@link AuthoredBranchEditor} to allow course authors to view and modify an 
 * {@link generated.course.AuthoredBranch AuthoredBranch's} constituent {@link generated.course.AuthoredBranch.Paths.Path Paths}.
 * 
 * @author nroberts
 */
public class PathWidget extends Composite {

	private static PathWidgetUiBinder uiBinder = GWT.create(PathWidgetUiBinder.class);

	interface PathWidgetUiBinder extends UiBinder<Widget, PathWidget> {
	}
	
	@UiField
	protected TextBox nameLabel;
	
	@UiField
	protected FlowPanel percentageLabel;
	
	@UiField
	protected HTMLPanel hr1, hr2;

    @UiField(provided = true)
    protected NumberSpinner customPercentage = new NumberSpinner(100, 0, 100, 1);
	
	@UiField
	protected Button removeButton;
	
	@UiField
	protected Button stopButton;
	
	@UiField
	protected Icon defaultPathIcon;
	
	
	/** The path represented by this widget */
	private Path path;
	
	/** The branch editor this widget is interacting with */
	private AuthoredBranchEditor parentEditor;
	
	/**
	 * Creates a new widget interacting with the given branch editor to display and modify the given path.
	 * 
	 * @param path the path this widget should display and modify
	 * @param parentEditor the branch editor this widget should interact with
	 */
	public PathWidget(final Path path, final AuthoredBranchEditor parentEditor) {
		
		this.path = path;
		this.parentEditor = parentEditor;
		
		initWidget(uiBinder.createAndBindUi(this));
		
		customPercentage.addValueChangeHandler(new ValueChangeHandler<Integer>() {

			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				if (path != null) { 
                    if (path.getCondition() == null) {
                        path.setCondition(new AuthoredBranch.Paths.Path.Condition());
                    }
				    BigDecimal value = new BigDecimal(event.getValue());
					path.getCondition().setCustomPercentOrLearnerCentric(value);
				}
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				parentEditor.onRemovePath(PathWidget.this);
			}
		});
		
		nameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				String name = event.getValue();
				
				if(name != null){
					name = name.trim();
				}
				
				if(parentEditor.isPathNameValid(name, path)){		
					
					path.setName(name);
					
					SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(parentEditor.getCourseObject()));
					
				} else {
					
					WarningDialog.error("Invalid value", "Path names cannot be empty and must be unique.");
					
					nameLabel.setValue(path.getName());
				}
			}
		});
		
		stopButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				event.stopPropagation();
				
				if(path != null){
					
					if(path.getCourseobjects() == null){
						path.setCourseobjects(new Courseobjects());
					}
					
					boolean foundEnd = false;
					
					Iterator<Serializable> itr = path.getCourseobjects().getAAROrAuthoredBranchOrEnd().iterator();
					
					while(itr.hasNext()){
						
						Serializable courseObject = itr.next();
						
						if(courseObject instanceof End){
							
							//an End object is present inside the path, so remove it
							itr.remove();
							
							foundEnd = true;
						}
					}
					
					if(!foundEnd){
						path.getCourseobjects().getAAROrAuthoredBranchOrEnd().add(new End());
					}
				}	

				stopButton.toggle();
				
				SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(parentEditor.getCourseObject()));
			}
		});

        setCustomDistVisible(false);
		
		updateDisplay();
	}
	
	/**
	 * Sets the default path
	 * 
	 * @param defaultPath the default path
	 */
	void setDefaultPath(boolean defaultPath) {
        // #4255 - hiding default path until non-SimpleDistribution (e.g. balanced) can be authored
        //         for adaptive courseflow.
	    //defaultPathIcon.setVisible(defaultPath);
	}
	
	/**
	 * Updates this widget's UI elements to match the state of the path this widget represents and the branch it is a part of.
	 */
	void updateDisplay() {
		
		if(path != null){
			
			//update the label used for the path name
			nameLabel.setValue(path.getName());
			
			if(parentEditor != null 
					&& parentEditor.getCourseObject() != null){ 
				
				//update the icon used to indicate if this path is the default
				if(parentEditor.getCourseObject().getDefaultPathId() != null
						&& parentEditor.getCourseObject().getDefaultPathId().equals(path.getPathId())){
				
				    // #4255 - hiding default path until non-SimpleDistribution (e.g. balanced) can be authored
				    //         for adaptive courseflow.
					// defaultPathIcon.setVisible(true);
					
				} else {
					defaultPathIcon.setVisible(false);
				}
				
				if(parentEditor.getCourseObject().getPaths() != null 
						&& parentEditor.getCourseObject().getPaths().getPath().size() <= 1){
					
					removeButton.setVisible(false);
					
				} else {
					removeButton.setVisible(true);
				}
				
			} else {
				defaultPathIcon.setVisible(false);
			}
			
			if(path.getCourseobjects() == null){
				path.setCourseobjects(new Courseobjects());
			}
			
			if (path.getCondition() != null) {
			    // TODO: handle LearnerCentric
			    if (path.getCondition().getCustomPercentOrLearnerCentric() instanceof BigDecimal) {
			        customPercentage.setValue(((BigDecimal)path.getCondition().getCustomPercentOrLearnerCentric()).intValue());
			    }
			}
			
			boolean foundEnd = false;
			
			for(Serializable courseObject : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()){
				
				if(courseObject instanceof End){
					foundEnd = true;
					break;
				}
			}
			
			if(foundEnd){
				stopButton.setActive(true);
				
			} else {
				stopButton.setActive(false);
			}
		}
		
		boolean readOnly = GatClientUtility.isReadOnly();
        nameLabel.setEnabled(!readOnly);
        stopButton.setEnabled(!readOnly);
        removeButton.setEnabled(!readOnly);
        customPercentage.setEnabled(!readOnly);
	}

	/**
	 * Gets the path being represented by this widget
	 * 
	 * @return the path represented by this widget
	 */
	public Path getPath(){
		return path;
	}

	/**
	 * Sets the visibility of the custom distribution controls on the path widget
	 * 
	 * @param visible whether the custom distribution controls on the path widget are visible
	 */
    public void setCustomDistVisible(boolean visible) {
        percentageLabel.setVisible(visible);
        customPercentage.setVisible(visible);
        hr1.setVisible(visible);
        hr2.setVisible(visible);
    }

}
