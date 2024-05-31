/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import mil.arl.gift.tutor.shared.WidgetTypeEnum;

/**
 * Properties for the AvatarContainer widget
 *
 * @author bzahid
 */
public class AvatarContainerWidgetProperties {
    
    /* Property names */
    private final static String WIDGET_TYPE = "WIDGET_TYPE";
    
    /**
     * Gets the widget type that should be displayed in the avatar container widget.
     * 
     * @param properties The widget properties
     * @return WidgetTypeEnum The widget type.
     */
    public static WidgetTypeEnum getWidgetType(WidgetProperties properties) {
        
        return properties.getPropertyValue(WIDGET_TYPE) == null ? 
        		WidgetTypeEnum.AVATAR_CONTAINER_WIDGET : (WidgetTypeEnum) properties.getPropertyValue(WIDGET_TYPE);
    }

    /**
     * Sets the widget type that should be displayed in the avatar container widget.
     * 
     * @param properties The widget properties
     * @param widgetType The type of widget to display
     */
    public static void setWidgetType(WidgetProperties properties, WidgetTypeEnum widgetType) {
        
        properties.setPropertyValue(WIDGET_TYPE, widgetType);
    }
    
}
