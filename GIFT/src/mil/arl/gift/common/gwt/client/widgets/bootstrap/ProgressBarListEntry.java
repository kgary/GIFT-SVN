/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import mil.arl.gift.common.io.ProgressIndicator;

import org.gwtbootstrap3.client.ui.ProgressBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to display progress in a list of progress bars
 * 
 * @author nroberts
 */
public class ProgressBarListEntry extends Composite {

	private static ProgressBarListEntryUiBinder uiBinder = GWT
			.create(ProgressBarListEntryUiBinder.class);

	interface ProgressBarListEntryUiBinder extends
			UiBinder<Widget, ProgressBarListEntry> {
	}

	@UiField
	protected ProgressBar progressBar;
	
	@UiField
	protected Label taskDescriptionLabel;

	/**
	 * Creates a new progress bar list entry
	 */
	public ProgressBarListEntry() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	/**
	 * Updates the progress information displayed by this widget
	 * 
	 * @param progress the progress information to display
	 */
	public void updateProgress(ProgressIndicator progress){
		
		int percentProgress = Math.min(progress.getPercentComplete(), 100);
		
		updateProgress(progress.getTaskDescription(), percentProgress);
	}
	
	/**
	 * Updates the progress information display by this widget
	 * 
	 * @param taskDescription a useful label about the task being performed and will
	 * be displayed near to the progress bar.
	 * @param percentProgress how much to shade in the progress bar as an indication
	 * of progress being made on the task.
	 */
	public void updateProgress(String taskDescription, int percentProgress){
	    
        progressBar.setPercent(percentProgress);
        taskDescriptionLabel.setText(taskDescription + " " + percentProgress + "%");
	}
	
	/**
	 * Clears the widgets components so that if the widget is used again
	 * it will won't start with the previous values.
	 */
	public void clear(){
	    progressBar.setPercent(0);
	    taskDescriptionLabel.setText("");
	}
	
	/**
	 * Set the color of the description label.
	 * @param color an css color value
	 */
	public void setDescriptionLabelColor(String color){
	    taskDescriptionLabel.getElement().getStyle().setColor(color);
	}

}
