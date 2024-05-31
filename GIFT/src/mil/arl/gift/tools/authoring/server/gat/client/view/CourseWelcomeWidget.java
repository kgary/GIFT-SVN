/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that shows a welcome message and a loading icon to be displayed as the course editor loads.
 * 
 * @author bzahid
 */
public class CourseWelcomeWidget extends Composite {
	
	private static CourseWelcomeWidgetUiBinder uiBinder = GWT.create(CourseWelcomeWidgetUiBinder.class);
	
	interface CourseWelcomeWidgetUiBinder extends UiBinder<Widget, CourseWelcomeWidget> {
		
	}
	
	@UiField
	protected BsLoadingIcon welcomeLoadIcon;
	
	/**
	 * Creates a new course welcome widget
	 */
	public CourseWelcomeWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		welcomeLoadIcon.startLoading();
	}
	
}
