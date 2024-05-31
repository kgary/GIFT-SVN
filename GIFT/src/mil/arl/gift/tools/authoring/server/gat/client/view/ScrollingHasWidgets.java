/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ScrollingHasWidgets implements HasWidgets{
	private LinkedList<Widget> linkedList = new LinkedList<Widget>();
	
	private VerticalPanel verticalPanel;
	private ScrollPanel logScroller;
	
	//TODO: make this configurable?
	private final int MAX_CAPACITY = 100;
	
	public ScrollingHasWidgets(VerticalPanel verticalPanel, ScrollPanel logScroller ) {
		this.verticalPanel = verticalPanel;
		this.logScroller = logScroller;
	}
	
	@Override
	public void add(Widget w) {				
		linkedList.add(w);				
		if(linkedList.size() > MAX_CAPACITY) {
			Widget staleWidget = linkedList.removeFirst();
			verticalPanel.remove(staleWidget);
		}				
		verticalPanel.add(w);
		logScroller.scrollToBottom();
	}

	@Override
	public void clear() {
		verticalPanel.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return verticalPanel.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return verticalPanel.remove(w);
	}
}
