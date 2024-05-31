/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that shows an image and text to inform the user the preview is unavailable
 */
public class PreviewUnavailableWidget extends Composite {
	
	interface PreviewUnavailableWidgetUiBinder extends UiBinder<Widget, PreviewUnavailableWidget>{	
	}
	
	private static PreviewUnavailableWidgetUiBinder uiBinder = GWT.create(PreviewUnavailableWidgetUiBinder.class);
	
	private static final String MESSAGE = "Previewing this course object is not supported at this time";
	
	@UiField
	protected HTML description;
	
	@UiField
	protected Image image;
	
	@UiField
	protected Label title;
	
	/**
	 * Constructor
	 */
	public PreviewUnavailableWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		description.setHTML(MESSAGE);
	}
	
	/**
	 * Constructor. Using this constructor changes the color theme to red.
	 * 
	 * @param message A user-friendly message about why the preview is unavailable.
	 */
	public PreviewUnavailableWidget(String message) {
		initWidget(uiBinder.createAndBindUi(this));
		description.setHTML(message);
		
		description.getElement().getStyle().setProperty("color", "#403a3a");
		description.getElement().getStyle().setProperty("fontSize", "1.6em");
		title.getElement().getStyle().setProperty("color", "rgb(107, 58, 56)");
		image.getElement().getStyle().setProperty("border-color", "rgba(152, 128, 122, 0.75)");
		image.setUrl("images/preview_error.png");
	}
}
