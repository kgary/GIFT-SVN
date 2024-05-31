/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.FreeResponseScoringWidget;

/**
 * Event to signal when the FreeResponseScoringWidget has changed it's weighted scores or scoring
 * conditions.
 * 
 * @author sharrison
 *
 */
public class FreeResponseScoringWidgetChangedEvent extends GenericEvent {

    /** the widget that was changed */
    private FreeResponseScoringWidget widget;

    /**
     * Constructor.
     * 
     * @param scoringWidget the widget that was changed. Can't be null.
     * @throws IllegalArgumentException if the scoringWidget is null.
     */
    public FreeResponseScoringWidgetChangedEvent(FreeResponseScoringWidget scoringWidget) {
        if (scoringWidget == null) {
            throw new IllegalArgumentException("Cannot pass a null FreeResponseScoringWidget into the change event.");
        }

        this.widget = scoringWidget;
    }

    /**
     * The widget that was changed and therefore triggered this event. Will never be null.
     * 
     * @return {@link FreeResponseScoringWidget}
     */
    public FreeResponseScoringWidget getWidget() {
        return widget;
    }
}
