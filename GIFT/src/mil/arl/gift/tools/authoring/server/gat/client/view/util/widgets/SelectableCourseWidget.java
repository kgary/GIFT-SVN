/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets;

import java.util.logging.Logger;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to represent a course that can be selected for an export
 * 
 * @author bzahid
 */
public class SelectableCourseWidget extends Composite {
	
	private static Logger logger = Logger.getLogger(SelectableCourseWidget.class.getName());
	
	private static SelectableCourseWidgetUiBinder uiBinder = GWT.create(SelectableCourseWidgetUiBinder.class);
	
	interface SelectableCourseWidgetUiBinder extends UiBinder<Widget, SelectableCourseWidget> {
	}

	@UiField
	protected FocusPanel container;
	
	@UiField
	protected CheckBox checkbox;
	
	@UiField 
	protected Image invalidImage;
	
	@UiField
	protected FlowPanel validatingPanel;
	
	@UiField 
	protected BsLoadingIcon validatingIcon;
		
	private AsyncCallback<ValidateFileResult> validationCallback = null;
	
	private DomainOption course;
	
	private boolean isValid = false;
	
	/**
	 * Creates a new widget to represent the DomainOption provided.
	 * 
	 * @param course The domain option containing information about the course. 
	 */
	public SelectableCourseWidget(DomainOption course) {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		this.course = course;
		
		checkbox.setText(course.getDomainName());
		container.setTitle(course.getDomainId());
		
		container.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				if(checkbox.isEnabled()) {
					checkbox.setValue(checkbox.getValue(), true);
				}
			}
		});
		
		checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean> () {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				checkbox.setValue(checkbox.getValue());
				
				if(checkbox.getValue() && !isValid) {
					validateCourse();
				}
			}
		});
	}
	
	/**
	 * Gets whether or not the course is valid.
	 * 
	 * @return true if the course is valid, false otherwise.
	 */
	public boolean courseIsValid() {
		return isValid;
	}
	
	/**
	 * Adds a value change handler to the course selection checkbox.
	 * 
	 * @param selectionHandler The handler to execute when the checkbox is clicked.
	 */
	public void setCourseSelectionHandler(ValueChangeHandler<Boolean> selectionHandler) {
		checkbox.addValueChangeHandler(selectionHandler);
	}
	
	/**
	 * Adds a callback to be executed when the validation result returns from the server.
	 * 
	 * @param validationCallback The callback to execute.
	 */
	public void setValidationCallback(AsyncCallback<ValidateFileResult> validationCallback) {
		this.validationCallback = validationCallback;
	}
	
	/**
	 * Validates the course. A loading busy indicator will be 
	 * displayed while the course is validating.
	 */
	public void validateCourse() {
		
		validatingPanel.setVisible(true);
		checkbox.setEnabled(false);
		
		ValidateFile action = new ValidateFile();
		action.setRelativePath(course.getDomainId());
		action.setUserName(GatClientUtility.getUserName());
		validatingIcon.startLoading();
		
		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<ValidateFileResult>() {

			@Override
			public void onFailure(Throwable t) {
				logger.warning("Caught exception while validating course \"" + course.getDomainName() + "\": " + t);
				disableCourse("An error occurred while validating the course.");
				validationCallback.onFailure(t);
			}

			@Override
			public void onSuccess(ValidateFileResult result) {
				if(result.isSuccess()) {
					checkbox.setEnabled(true);
					validatingPanel.setVisible(false);
					validatingIcon.stopLoading();
					isValid = true;
					
				} else {
					disableCourse("This course failed to validate successfully and cannot be exported.");
				}
				
				validationCallback.onSuccess(result);
			}
			
		});
	}
	
	/**
	 * Disables the selection checkbox and sets a tooltip with the reason provided.
	 * 
	 * @param reason The reason the course is disabled.
	 */
	private void disableCourse(String reason) {
		
		checkbox.setValue(false);
		checkbox.setEnabled(false);
		validatingIcon.stopLoading();
		checkbox.getElement().setTitle(reason);
		validatingPanel.setVisible(false);
		invalidImage.setVisible(true);
	}
	
}
