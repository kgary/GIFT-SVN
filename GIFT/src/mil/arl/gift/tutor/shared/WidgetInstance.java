/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * Represents a single instance of a widget.
 *
 * @author jleonard
 */
public class WidgetInstance implements IsSerializable {

    private String widgetId;
    private WidgetTypeEnum widgetType;
    private WidgetProperties widgetProperties;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public WidgetInstance() {
    }

    /**
     * Constructor, constructs a widget instance
     *
     * @param widgetType The type of widget
     */
    public WidgetInstance(WidgetTypeEnum widgetType) {
        this.widgetId = null;
        this.widgetType = widgetType;
        this.widgetProperties = new WidgetProperties();
    }

    /**
     * Constructor, constructs a widget instance
     *
     * @param widgetType The type of widget
     * @param widgetProperties The properties of the widget
     */
    public WidgetInstance(WidgetTypeEnum widgetType, WidgetProperties widgetProperties) {
        this.widgetId = null;
        this.widgetType = widgetType;
        this.widgetProperties = widgetProperties;
    }

    /**
     * Constructor, constructs a widget instance
     *
     * @param widgetId The widget ID
     * @param widgetType The type of widget
     */
    public WidgetInstance(String widgetId, WidgetTypeEnum widgetType) {
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.widgetProperties = new WidgetProperties();
    }

    /**
     * Constructor, constructs a widget instance
     *
     * @param widgetId The widget ID
     * @param widgetType The type of widget
     * @param widgetProperties The properties of the widget
     */
    public WidgetInstance(String widgetId, WidgetTypeEnum widgetType, WidgetProperties widgetProperties) {
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.widgetProperties = widgetProperties;
    }

    /**
     * Gets the widget instance ID
     *
     * @return String The widget instance ID
     */
    public String getWidgetId() {
        return widgetId;
    }

    /**
     * Gets the widget type
     *
     * @return WidgetTypeEnum The widget type
     */
    public WidgetTypeEnum getWidgetType() {
        return widgetType;
    }

    /**
     * Gets the widget properties
     *
     * @return WidgetProperties The widget properties
     */
    public WidgetProperties getWidgetProperties() {
        return widgetProperties;
    }

    /**
     * Applies an action to the widget
     *
     * @param action The action to apply to the widget
     */
    public void applyAction(AbstractWidgetAction action) {
        if (action.getActionType() == ActionTypeEnum.SUBMIT) {
            SubmitAction submit = (SubmitAction) action;
            widgetProperties.putAll(submit.getProperties());
        }
    }

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[WidgetInstance: ID = ").append(getWidgetId());
        sb.append(", Type = ").append(getWidgetType());
        sb.append(", WidgetProperties = ").append(getWidgetProperties());
        sb.append("]");
        return sb.toString();
    }
}
