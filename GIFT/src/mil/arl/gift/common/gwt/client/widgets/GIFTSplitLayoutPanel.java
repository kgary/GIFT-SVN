/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Extends {@link SplitLayoutPanel} to add helpful methods.
 * 
 * @author sharrison
 */
public class GIFTSplitLayoutPanel extends TouchSplitLayoutPanel {

    /**
     * Construct a new {@link SplitLayoutPanel} with the default splitter size.
     */
    @UiConstructor
    public GIFTSplitLayoutPanel() {
        super();
    }

    /**
     * Construct a new {@link SplitLayoutPanel} with the specified splitter size
     * in pixels.
     *
     * @param splitterSize the size of the splitter in pixels
     */
    public GIFTSplitLayoutPanel(int splitterSize) {
        super(splitterSize);
    }

    /**
     * Checks if the widget at the given index is hidden.
     * 
     * @param i the widget index
     * @return true if it is hidden; false otherwise.
     */
    public boolean isHidden(int i) {
        return ((DockLayoutPanel.LayoutData) getWidget(i).getLayoutData()).hidden;
    }

    /**
     * Checks if the widget is hidden.
     * 
     * @param w the widget
     * @return true if it is hidden; false otherwise.
     */
    public boolean isHidden(Widget w) {
        return ((DockLayoutPanel.LayoutData) w.getLayoutData()).hidden;
    }
}
