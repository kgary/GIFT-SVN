/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

/**
 * A callback to be invoked whenever part of the interops editor has successfully been edited
 * 
 * @author nroberts
 */
public interface InteropsEditedCallback {
	
	/**
	 * Performs logic whenever part of the interops editor has successfully been edited
	 */
	public void onEdit();
}
