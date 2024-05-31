/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;


import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class GatViewScaffold.
 */
public class GatViewScaffold extends Composite {

	//rootLogger is used in the scrolling log view (do not change this)
	/** The root logger. */
	@SuppressWarnings("unused")
    private static Logger rootLogger = Logger.getLogger("");
	
	/**
	 * The Interface GatViewScaffoldUiBinder.
	 */
	interface GatViewScaffoldUiBinder extends UiBinder<Widget, GatViewScaffold> {
	}
	
	/** The ui binder. */
	private static GatViewScaffoldUiBinder uiBinder = GWT
			.create(GatViewScaffoldUiBinder.class);
	
	/** The application panel. */
	protected @UiField SimpleLayoutPanel applicationPanel;

	/**
	 * Instantiates a new gat view scaffold.
	 */
	public GatViewScaffold() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		// Removed the logging footer here since gwt now has ability to
		// log to console/external windows.  
		// TODO look at cleaning up the footer logic entirely if it is no longer going to be used.
	}
	
	/**
	 * Gets the app panel.
	 *
	 * @return the app panel
	 */
	public AcceptsOneWidget getAppPanel() {
		return applicationPanel;
	}
	
}
