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
 * Submits the properties of the widget
 *
 * @author jleonard
 */
public class SubmitAction extends AbstractWidgetAction implements IsSerializable {

    private WidgetProperties submitProperties;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public SubmitAction() {
        super(ActionTypeEnum.SUBMIT, null);
        submitProperties = new WidgetProperties();
    }

    /**
     * Constructor
     * 
     * @param widgetId The widget ID associated with the properties
     * @param submitProperties The properties of the widget to submit
     */
    public SubmitAction(String widgetId, WidgetProperties submitProperties) {
        super(ActionTypeEnum.SUBMIT, widgetId);
        this.submitProperties = submitProperties;
    }

    /**
     * Gets the submitted widget properties
     * 
     * @return WidgetProperties The submitted widget properties
     */
    public WidgetProperties getProperties() {
        return submitProperties;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[SubmitAction: ");
        sb.append(super.toString());
        sb.append(", properties = ").append(getProperties());
        return sb.toString();
    }
}
