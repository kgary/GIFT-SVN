/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event fired when the course folder is changed on the server due to a client
 * side request.  For example when using the GAT to rename the course than saving the course.
 * 
 * @author mhoffman
 *
 */
public class CourseFolderChangedEvent extends GenericEvent {

    public CourseFolderChangedEvent(){ }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseFolderChangedEvent: ]");
        return builder.toString();
    }
}
