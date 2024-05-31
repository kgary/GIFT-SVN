/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.conversations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that displays the number of queued updates.
 * 
 * @author bzahid
 */
public class UpdateCounterWidget extends Composite {

	interface UpdateCounterWidgetUiBinder extends UiBinder<Widget, UpdateCounterWidget> {
	}
	
	private static UpdateCounterWidgetUiBinder uiBinder = GWT.create(UpdateCounterWidgetUiBinder.class);
	
	@UiField
	protected Label counter;
	
	/**
	 * Creates a new update counter widget.
	 */
	public UpdateCounterWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		setCount(0);
	}
	
	/**
	 * Updates the counter label
	 * 
	 * @param count The number of queued updates.
	 */
	public void setCount(int count) {
		counter.setText(Integer.toString(count));
	}
	
	/**
	 * Increments the counter label
	 */
	public void incrementCount() {
		counter.setText(Integer.toString(getCount() + 1));
	}
	
	/**
	 * Decrements the counter label
	 */
	public void decrementCount() {
		if(getCount() > 0) {  //don't decrement into negative numbers
			counter.setText(Integer.toString(getCount() - 1));
		}
	}
	
	/**
	 * Gets the number of queued updates.
	 * 
	 * @return The number of queued updates.
	 */
	public int getCount() {
		return Integer.parseInt(counter.getText());
	}
}
