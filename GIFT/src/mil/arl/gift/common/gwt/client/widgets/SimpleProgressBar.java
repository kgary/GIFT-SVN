/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import mil.arl.gift.common.io.ProgressIndicator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple progress bar with a status message
 * 
 * @author nroberts
 */
public class SimpleProgressBar extends Composite {

	private static ProgressBarListEntryUiBinder uiBinder = GWT
			.create(ProgressBarListEntryUiBinder.class);

	interface ProgressBarListEntryUiBinder extends
			UiBinder<Widget, SimpleProgressBar> {
	}
	
	@UiField
	protected Label taskDescriptionLabel;
	
	@UiField
	protected Widget progressBarInner;

	/**
	 * Creates a new simple progress bar
	 */
	public SimpleProgressBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	/**
	 * Updates the progress information displayed by this widget
	 * 
	 * @param progress the progress information to display
	 */
	public void updateProgress(ProgressIndicator progress){
		
		int percentProgress = Math.min(progress.getPercentComplete(), 100);
		
		progressBarInner.setWidth(progress.getPercentComplete() + "%");
		taskDescriptionLabel.setText(progress.getTaskDescription() + " " + percentProgress + "%");
	}

}
