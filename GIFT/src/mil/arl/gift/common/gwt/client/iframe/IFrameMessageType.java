/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe;


/**
 * Enum mapping of all messages that can be sent using cross-domain communication within GIFT.
 * New message types should be added here along with appropritate encoding/decoding logic in the
 * IFrameMessageParser class.
 * 
 * @author nblomberg
 *
 */
public enum IFrameMessageType {

    TUI_READY,          // Signals that the tui is ready (called during onModuleLoad() after the iframe handlers are setup in the tui).
    /** The END_COURSE message is a one-way message sent from the TUI to the dashboard.  This message occurs once the domain session has ended. (eg. if the course completed successfully) */
    END_COURSE,
    COURSE_STARTING,    // Signals the tui startCourse rpc has finished.
    COURSE_READY,       // Signals that the course is ready (domain session is started).
    DISPLAY_DIALOG,
    DISPLAY_NOTIFICATION,
    /** The stop course message is a one-way message sent from the Dashboard to the TUI in the cases where the user manually stops the course or signs out, 
     * however the dashboard will wait until the TUI sends back the END_COURSE message which means the TUI has shutdown the domain session and ended the course.
     */
    STOP_COURSE,
    INVALID,
    GAT_LOADED,
    CLOSE_GAT_FILES,
    GAT_FILES_CLOSED,
    GAT_FILES_OPEN,
    GAT_SET_COOKIE,
    GO_TO_DASHBOARD,
    WRAP_OPEN,
    WRAP_CLOSED,
    
    /** 
     * The control application message is a one-way message used to control a training application wrapped around GIFT's
     * client end, such as the GIFT mobile app, by passing messages between iframes
     */
    CONTROL_APPLICATION,
    
    /** 
     * The application event message is a one-way message used to notify GIFT whenever certain events happen in a training 
     * application wrapped around GIFT's client end, such as the GIFT mobile app, by passing messages between iframes
     */
    APPLICATION_EVENT,
    
    /**
     * Signals to the tui to add a new history item. This can be used to resume previous sessions if the domain session id is saved.
     */
    ADD_HISTORY_ITEM;
}
