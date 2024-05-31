/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;





/**
 * The PropertySetListener interface is used to send notifications that properties have been
 * changed and that the changes can be handled appropriately.
 * 
 * @author nblomberg
 *
 */
public interface PropertySetListener  {

    /**
     * Used to notify the listener when a property set value has changed.
     * 
     * @param propSet - The property set that was changed.  
     */
    void onPropertySetChange(AbstractPropertySet propSet);

}
