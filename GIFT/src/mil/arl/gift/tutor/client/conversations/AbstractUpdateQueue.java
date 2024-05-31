/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.conversations;

import com.google.gwt.user.client.ui.Composite;

/**
 * This class contains the common implementation for widgets that need to queue updates.
 * 
 * @author bzahid
 */
public abstract class AbstractUpdateQueue extends Composite {

	/** Whether or not the widget is active. */
	protected boolean isActive = false;
	
	/** A widget that displays the number of queued updates. */
	protected UpdateCounterWidget counterWidget;
	
	/** Whether or not an update was dequeued. */
	protected boolean dequeuing = false;
	
	/**
	 * Class constructor.
	 */
	public AbstractUpdateQueue() {
		
	}
	
	/**
	 * Class constructor.
	 * 
	 * @param isActive Whether or not this widget is active.
	 */
	public AbstractUpdateQueue(boolean isActive) {
		setIsActive(isActive);
	}
	
	/**
	 * Sets the update counter widget
	 * 
	 * @param counterWidget A widget that displays the number of queued updates.
	 */
	public void setUpdateCounter(UpdateCounterWidget counterWidget) {
		this.counterWidget = counterWidget;
		counterWidget.setVisible(!isActive);
	}
	
	/**
	 * Increments the update counter widget.
	 */
	public void incrementCounter() {
		if(this.counterWidget != null) {
			counterWidget.incrementCount();
			counterWidget.setVisible(!isActive);
		}
	}
	
	/**
	 * Decrements the update counter widget.
	 */
	public void decrementCounter() {
		if(this.counterWidget != null) {
			counterWidget.decrementCount();
			counterWidget.setVisible(!isActive && (counterWidget.getCount() > 0));
		}
	}
		
	/**
	 * Gets the number of available updates for this widget.
	 * 
	 * @return the number of available updates.
	 */
	public int getUpdateCount() {
		return (counterWidget == null) ? 0 : counterWidget.getCount();
	}
	
	/**
	 * Gets whether or not the widget is currently active.
	 * 
	 * @return True if the widget is active, false otherwise.
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * Sets whether or not the widget is currently active.
	 * 
	 * @param isActive True if the widget is active, false otherwise.
	 */
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
		if(counterWidget != null){
			counterWidget.setVisible(!isActive && (getUpdateCount() > 0));
		}
		
		updateServerStatus();
	}
	
	/**
	 * Allow concrete classes to notify the server of any change in state/status.  For example
	 * this can be used to let the server know if the widget is active/inactive.
	 */
	public abstract void updateServerStatus();
	
	/**
	 * Allow concrete classes to notify the server that the widget is ready to receive the next
	 * available message.  For example this can be used to let the server know to send the next
	 * conversation message to the widget showing the conversation to the learner.
	 */
	public abstract void dequeueUpdates();
	
}
