/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * The Class MenuItemWrapper.
 */
public class MenuItemWrapper implements MvpMenuItem {

	/** The menu item. */
	private MenuItem menuItem;
	
	/**
	 * Instantiates a new menu item wrapper.
	 *
	 * @param menuItem the menu item
	 */
	public MenuItemWrapper(MenuItem menuItem) {
		this.menuItem = menuItem;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.util.HasScheduledCommand#setScheduledCommand(com.google.gwt.core.client.Scheduler.ScheduledCommand)
	 */
	@Override
	public void setScheduledCommand(ScheduledCommand cmd) {
		menuItem.setScheduledCommand(cmd);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#getText()
	 */
	@Override
	public String getText() {
		return menuItem.getText();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		menuItem.setText(text);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasEnabled#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return menuItem.isEnabled();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasEnabled#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		menuItem.setEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.util.MvpMenuItem#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		menuItem.setVisible(visible);
	}
}
