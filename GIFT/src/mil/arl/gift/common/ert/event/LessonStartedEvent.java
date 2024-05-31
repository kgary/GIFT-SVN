/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Custom parsing logic for the lesson started event
 * @author mhoffman
 *
 */
public class LessonStartedEvent extends DomainSessionEvent {

    /**
     * Class constructor - set attributes
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     */
    public LessonStartedEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry) {
        super(MessageTypeEnum.LESSON_STARTED.getDisplayName(), time, domainSessionMessageEntry, null);
        
    }
}
