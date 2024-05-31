/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;

/**
 * The Interface MvpMenuItem.
 */
public interface MvpMenuItem extends HasText, HasEnabled, HasScheduledCommand {

	public void setVisible(boolean visible);

}
