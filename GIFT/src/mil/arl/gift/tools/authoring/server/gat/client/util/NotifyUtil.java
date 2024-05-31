/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

/**
 * A utility class for dealing with Bootstrap's Notify API. This class is mainly used to keep notifications looking and behaving
 * consistently throughout the GAT, since it provides a shared set of notification settings via its {@link #generateDefaultSettings()}
 * method that can be reused throughout the GAT.
 * 
 * @author nroberts
 */
public class NotifyUtil {
	
	/** The Z-index to assign to all notifications */
	private final static int Z_INDEX = 999999;

	/**
	 * Generates a set of settings to be used when showing a notification. This method should usually be used to retrieve the
	 * settings used for notification messages so that they behave consistently.
	 * 
	 * @return
	 */
	public static NotifySettings generateDefaultSettings(){
		
		NotifySettings settings = NotifySettings.newSettings();
		settings.setPauseOnMouseOver(true);
		settings.setZIndex(Z_INDEX);
		
		return settings;
	}
}
