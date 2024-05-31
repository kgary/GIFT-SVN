/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class GatFooter.
 */
public class GatFooter extends Composite {
	
	/**
	 * The Interface GatFooterUiBinder.
	 */
	interface GatFooterUiBinder extends UiBinder<Widget, GatFooter> {
	}
	
	/** The ui binder. */
	private static GatFooterUiBinder uiBinder = GWT
			.create(GatFooterUiBinder.class);
	
	//TODO: Consider removing these from the ui.xml file
	//instead inject the via the program (or not) based on a build configuration file.
	//this may help: http://webcentersuite.blogspot.com/2011/08/using-gwt-configuration-properties.html
	/** The log scroller. */
	protected @UiField ScrollPanel scrollPanel;
	
	/** The log display. */
	protected @UiField VerticalPanel logDisplay;
	
	protected ScrollingHasWidgets scrollingHasWidgets = null;
	/**
	 * Instantiates a new gat footer.
	 */
	public GatFooter() {
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public ScrollingHasWidgets getScrollingHasWidgets() {
		if(scrollingHasWidgets == null) {
			scrollingHasWidgets = new ScrollingHasWidgets(logDisplay, scrollPanel);
		}
		return scrollingHasWidgets;
	}
	
}
