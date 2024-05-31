/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An action to display a widget
 *
 * @author jleonard
 */
public class DisplayWidgetAction extends AbstractAction implements IsSerializable {

    private WidgetInstance instance;

    private WidgetLocationEnum displayLocation;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public DisplayWidgetAction() {
        super(ActionTypeEnum.DISPLAY_WIDGET);
    }

    /**
     * Constructor
     *
     * @param instance The widget instance to display
     * @param displayLocation Where to display the widget at
     */
    public DisplayWidgetAction(WidgetInstance instance, WidgetLocationEnum displayLocation) {
        super(ActionTypeEnum.DISPLAY_WIDGET);
        this.instance = instance;
        this.displayLocation = displayLocation;
    }

    /**
     * Gets the instance of the widget to display
     *
     * @return WidgetInstance The widget instance to display
     */
    public WidgetInstance getWidgetInstance() {
        return instance;
    }

    /**
     * Gets the location where the widget should be displayed
     *
     * @return WidgetLocationEnum Where the widget should be displayed
     */
    public WidgetLocationEnum getDisplayLocation() {
        return displayLocation;
    }

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayWidgetAction: ");
        sb.append(super.toString());
        sb.append(", ").append(getWidgetInstance());
        sb.append(", ").append(getDisplayLocation());
        sb.append("]");
        return sb.toString();
    }
}
