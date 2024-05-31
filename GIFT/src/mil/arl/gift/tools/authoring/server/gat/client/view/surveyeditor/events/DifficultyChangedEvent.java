/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyWidgetId;

/**
 * Event sent when the Difficulty value of a survey widget changes
 * 
 * @author cpadilla
 *
 */
public class DifficultyChangedEvent extends GenericEvent {

	/* The widget ID that the difficulty changed for */
	private SurveyWidgetId widgetId;

	/* The new value of the difficulty the widget was changed to */
	private QuestionDifficultyEnum difficulty;
	
	/**
	 * Constructor
	 * 
	 * @param widgetId the widget ID that the difficulty changed for
	 * @param difficulty the new difficutly value of the widget
	 */
	public DifficultyChangedEvent(SurveyWidgetId widgetId, QuestionDifficultyEnum difficulty) {
		
        if(widgetId == null) {
            throw new IllegalArgumentException("The value of widgetId can't be null");
        }
        
        if(difficulty == null) {
            throw new IllegalArgumentException("The value of difficulty can't be null");
        }
        
        this.widgetId = widgetId;
        this.difficulty = difficulty;
	}
	
	/**
	 * Gets the widget ID
	 * 
	 * @return the widget ID
	 */
	public SurveyWidgetId getWidgetId() {
		return widgetId;
	}
	
	/**
	 * Gets the difficulty of the changed widget
	 * 
	 * @return the new difficulty
	 */
	public QuestionDifficultyEnum getDifficulty() {
		return difficulty;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("[DifficultyChangedEvent: widgetId = ")
				.append(widgetId)
				.append(", difficulty = ")
				.append(difficulty)
				.append("]")
				.toString();
	}
}
