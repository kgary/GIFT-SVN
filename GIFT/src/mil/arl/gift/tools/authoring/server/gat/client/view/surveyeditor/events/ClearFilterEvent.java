/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Event thrown by the Question Filter Widget to notify the Survey Editor Panel that the filter has been cleared.
 * This is used to reload the questions displayed on the survey page.
 * 
 * @author tflowers
 *
 */
public class ClearFilterEvent extends GenericEvent {

}
