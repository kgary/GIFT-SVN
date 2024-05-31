/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The AbstractCourseLaunchParameters is a gwt client/server compatible class that is used
 * to allow for future extension of passing parameters when launching a course.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractCourseLaunchParameters implements IsSerializable  {

    /**
     * Constructor - needed for Gwt serialization.
     */
    public AbstractCourseLaunchParameters() {
        
    }

}
