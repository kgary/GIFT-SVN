/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget representing a context menu from which users can select actions to execute
 * 
 * @author nroberts
 */
public class ContextMenu extends PopupPanel {

	private static ContextMenuUiBinder uiBinder = GWT
			.create(ContextMenuUiBinder.class);

	interface ContextMenuUiBinder extends UiBinder<Widget, ContextMenu> {
	}
	
	@UiField
	protected MenuBar menu;

	/**
	 * Creates a new, empty context menu
	 */
	public ContextMenu() {
		setWidget(uiBinder.createAndBindUi(this));
		
		addStyleName("contextMenu");
		
		setAutoHideEnabled(true);
	}
	
	/**
	 * Gets the menu bar this context menu uses to present its options
	 * 
	 * @return the menu bar
	 */
	public MenuBar getMenu(){
		return menu;
	}

	@Override
	public void show(){	
		
		super.show();
		
		// We need to try to keep the context menu from going off the page; otherwise, scrollbars can appear next to the map.
		// We don't want this to happen because it interfere's with the map's zooming controls.
		int top = getPopupTop();
		int left = getPopupLeft();
		
		int maxHeight = Window.getClientHeight() - top - 1;
		int maxWidth = Window.getClientWidth() - left;		
		
		if(getOffsetHeight() > maxHeight){
			
			int displacement = maxHeight - getOffsetHeight();
			setPopupPosition(left, top + displacement);
		}
		
		if(getOffsetWidth() > maxWidth){
			
			int displacement = maxWidth - getOffsetWidth();
			setPopupPosition(left + displacement, top);
		}
	}
	
	/**
	 * Shows this context menu next to the mouse's current location
	 * 
	 * @param event the mouse event (i.e. click, enter, exit) from which to get the current mouse position
	 */
	public void showAtCurrentMousePosition(MouseEvent<?> event){
		setPopupPosition(event.getClientX(), event.getClientY());
		show();
	}
}
