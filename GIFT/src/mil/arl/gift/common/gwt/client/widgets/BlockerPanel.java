/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * An extension of {@link SimplePanel} that can be added to any container widget in order to intercept user interactions before they
 * can reach the widget's other children. Note that interactions will only be intercepted for children that are added before this widget
 * or placed at a lower z-index than this widget.
 * 
 * @author nroberts
 */
public class BlockerPanel extends SimplePanel {

    /**
     * Constructor
     */
	public BlockerPanel(){
		super();
		
		setVisible(false);
		
		getElement().getStyle().setPosition(Position.ABSOLUTE);
		getElement().getStyle().setTop(0, Unit.PX);
		getElement().getStyle().setBottom(0, Unit.PX);
		getElement().getStyle().setLeft(0, Unit.PX);
		getElement().getStyle().setRight(0, Unit.PX);
		getElement().getStyle().setZIndex(Integer.MAX_VALUE);
	}
	
	/**
	 * Prevents widgets underneath this widget from handling user interaction
	 */
	public void block(){
		setVisible(true);
	}
	
	/**
	 * Allows widgets underneath this widget to handle user interaction
	 */
	public void unblock(){
		setVisible(false);
	}

    /**
     * Checks if this widget is current blocking
     * 
     * @return true if it is currently blocking; false otherwise.
     */
    public boolean isBlocking() {
        return isVisible();
    }

	/**
	 * Sets whether or not blocked widgets should appear disabled
	 * 
	 * @param show  whether or not blocked widgets should appear disabled
	 */
	public void setShowDisabled(boolean show){
		
		if(show){			
			getElement().getStyle().setBackgroundColor("rgba(0,0,0,0.15)");
			getElement().getStyle().setProperty("cursor", "not-allowed");
			
		} else {
			getElement().getStyle().clearBackgroundColor();
			getElement().getStyle().clearProperty("cursor");
		}
	}
}
