/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A FocusPanel that can be used to show a Tooltip whenever the mouse hovers over its contents
 * 
 * @author nroberts
 *
 */
public class TooltipPanel extends FocusPanel{
	
	/** The popup showing the tooltip */
	private PopupPanel tooltipPopup = new PopupPanel();
	
	/** The tooltip's HTML */
	private HTML tooltipHTML = new HTML();

	/**
	 * Creates a new TooltipPanel
	 */
	public TooltipPanel(){
		
		tooltipPopup.setWidget(tooltipHTML);
		
		tooltipPopup.setWidth("400px");
		
		tooltipPopup.getElement().getStyle().setProperty("color", "white");
		tooltipPopup.getElement().getStyle().setProperty("backgroundColor", "black");
		tooltipPopup.getElement().getStyle().setProperty("borderStyle", "none");
		tooltipPopup.getElement().getStyle().setProperty("borderRadius", "5px");
		tooltipPopup.getElement().getStyle().setProperty("padding", "5px");
		tooltipPopup.getElement().getStyle().setProperty("textShadow", "2px 2px rgba(0,0,0,0.5)");
		tooltipPopup.getElement().getStyle().setProperty("boxShadow", "3px 3px 5px rgba(0,0,0,0.5)");
		
		this.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				if(tooltipHTML.getHTML() != null && !tooltipHTML.getHTML().isEmpty()){
					tooltipPopup.showRelativeTo(TooltipPanel.this);
				}
			}
		});
		
		this.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				tooltipPopup.hide();
			}
		});
	}

	/**
	 * Sets the HTML for the tooltip
	 * 
	 * @param html the HTML for the tooltip
	 */
	public void setTooltip(String html) {
		tooltipHTML.setHTML(html);
	}
}

