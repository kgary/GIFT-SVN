/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

/**
 * An action on a widget instance
 *
 * @author jleonard
 */
public class AbstractWidgetAction extends AbstractAction {

    private String widgetId = null;

    /**
     * Constructor
     *
     * @param widgetId The widget ID of the widget to apply the action to
     * @param actionType The type of action to take
     */
    public AbstractWidgetAction(ActionTypeEnum actionType, String widgetId) {
        super(actionType);
        this.widgetId = widgetId;
    }

    /**
     * Gets the widget ID to apply an action to
     *
     * @return String The widget ID to apply an action to
     */
    public String getWidgetId() {
        return widgetId;
    }

    @Override
    public String toString() {
        return "id = " + widgetId + ", " + super.toString() + "]";
    }
}
