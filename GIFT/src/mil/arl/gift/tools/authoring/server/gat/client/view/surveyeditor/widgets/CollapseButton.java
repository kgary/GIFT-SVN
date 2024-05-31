/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.widgets;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;

/**
 * The CollapseButton class encapsulates a button that can be used to show a collapsed/uncollapsed state.
 * This common button class can be used in the survey editor to allow common button functionality across different widgets.
 * 
 * @author nblomberg
 *
 */
public class CollapseButton extends Button  {

    private static Logger logger = Logger.getLogger(CollapseButton.class.getName());

	/** Boolean to indicate if the button is collapsed or expanded.  */
    private boolean collapsed = false;

	/**
	 * Constructor (default)
	 */
	public CollapseButton() {
	    
	    logger.info("constructor()");
	    
	    setCollapsed(collapsed);
	    
	    addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {

                logger.info("CollapseButton clickHandler()");
                toggleCollapsed();
            }
            
        });
	    
	}
	
	/**
     * Set the button to be collapsed (or expanded). 
     * 
     * @param collapse - true to collapse the button, false to expand the button.
     */
    public void setCollapsed(boolean collapse) {
        collapsed = collapse;

        updateCollapseIcon();
        
    }
    
    /**
     * Get the state of the button (collapsed or expanded).
     * 
     * @return - True if the button is collapsed, false otherwise.
     */
    public boolean isCollapsed() {
        return collapsed;
    }
    
    /**
     * Update the icon to show if the page is expanded or collapsed.
     */
    private void updateCollapseIcon() {
        
        if (collapsed) {
            setIcon(IconType.CARET_RIGHT);
        } else {
            setIcon(IconType.CARET_DOWN);
        }
        
        
    }
    
    /**
     * Toggle the button collapse state.
     * 
     */
    public void toggleCollapsed() {
        setCollapsed(!collapsed);
    }


}
