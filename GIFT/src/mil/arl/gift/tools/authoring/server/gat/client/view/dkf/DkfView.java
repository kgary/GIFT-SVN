/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.ScenarioOutlineEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;


/**
 * The Interface DkfView.
 */
public interface DkfView extends IsWidget, Serializable {
    
	//used by Activity
    /**
	 * The Interface Presenter.
	 */
	public interface Presenter {
    	
		/**
		 * Start.
		 *
		 * @param containerWidget the container widget
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 */
		void start(AcceptsOneWidget containerWidget, HashMap<String, String> startParams);		
		
		/**
		 * Confirm stop.
		 *
		 * @return the string
		 */
		String confirmStop();
		
		/**
		 * Stop.
		 */
		void stop();

		/** 
		 * Sets whether or not this editor should be Read-Only
		 * 
		 * @param readOnly whether or not this editor should be Read-Only
		 */
		void setReadOnly(boolean readOnly);
    }
    
	/**
	 * Show confirm dialog.
	 *
	 * @param msgHtml the msg html
	 * @param confirmMsg the confirm button text 
	 * @param callback the callback
	 */
	void showConfirmDialog(String msgHtml, String confirmMsg, OkayCancelCallback callback);

	/** 
	 * Sets whether or not this editor should be Read-Only
	 * 
	 * @param readOnly whether or not this editor should be Read-Only
	 */
	void setReadOnly(boolean readOnly);

	/**
	 * Hides the dkf object dialog
	 */
	void hideDkfObjectModal();

	/**
	 * Gets the editor used to provide an interactive outline of the scenario
	 * 
	 * @return the outline editor
	 */
    ScenarioOutlineEditor getScenarioOutline();
    
    /**
     * The panel used to load editors for various scenario objects
     * 
     * @return the scenario object editor panel
     */
    ScenarioObjectEditorPanel getObjectEditorPanel();
}
