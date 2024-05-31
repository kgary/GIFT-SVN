/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays a message in the TUI 
 * 
 * @author bzahid
 */
public class CourseMessageDialog extends Composite {
	
	interface CourseMessageDialogUiBinder extends UiBinder<Widget, CourseMessageDialog> {
	}
	
	private static CourseMessageDialogUiBinder uiBinder = GWT.create(CourseMessageDialogUiBinder.class);
	
	@UiField
	protected Modal modal;
	
	@UiField
	protected HTML modalMessage;
	
	public CourseMessageDialog() {
		initWidget(uiBinder.createAndBindUi(this));
		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override
		            public void execute() {
						CourseMessageDialog.this.onResize();
		            }
				});
			}
		});
	}
	
	/**
	 * Sets the message content and displays the modal dialog
	 * 
	 * @param message The html message to set
	 */
	public void showMessage(String message) {
		modalMessage.setHTML(message);
		modal.show();
		onResize();
	}
	
	/**
	 * Hides the modal dialog
	 */
	public void hide() {
		modal.hide();
	}
	
	/**
	 * Adds a handler to execute when the modal is hidden
	 * 
	 * @param handler The handler to execute when the modal is hidden
	 */
	public void addCloseHandler(ModalHideHandler handler) {
		modal.addHideHandler(handler);
	}
	
	@Override
	public void onAttach() {
		super.onAttach();
		onResize();
	}
	
	private void onResize() {
		int pad = 98;
		int height = modal.getElement().getFirstChildElement().getOffsetHeight();
		modalMessage.getElement().getStyle().setProperty("maxHeight", (height - pad) + "px");
	}
}
