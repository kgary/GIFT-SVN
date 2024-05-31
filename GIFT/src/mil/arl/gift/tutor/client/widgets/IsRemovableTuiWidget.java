/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An interface that indicates that a widget needs to perform specific logic when the TUI stops displaying it and removes it
 * from the page. 
 * <br/><br/>
 * At surface level, this interface works similarly to an AttachHandler, but unlike an AttachHandler, this interface's
 * {@link #onRemoval()} method should only be invoked when a widget is EXPLICITLY removed <i>by the TUI</i>. Actions that remove
 * a widget from the page without going through the TUI, such as reloading the page, should NOT invoke the 
 * {@link #onRemoval()} method for widgets implementing this interface.
 * 
 * @author nroberts
 */
public interface IsRemovableTuiWidget extends IsWidget{

    /**
     * Invokes the necessary logic when the TUI stops displaying this widget (i.e. removes it from the page).
     * 
     * @param isGiftInvoked whether the GIFT system directed the TUI to remove this widget. If false, the widget was
     * removed due to an action explicitly performed by the user, such as by clicking their browser's back button.
     */
    public void onRemoval(boolean isGiftInvoked);
}
