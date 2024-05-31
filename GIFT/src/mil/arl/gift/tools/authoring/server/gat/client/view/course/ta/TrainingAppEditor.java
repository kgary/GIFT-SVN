/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import com.google.gwt.user.client.Command;

import generated.course.TrainingApplication;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.TrainingAppPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link TrainingApplication} course objects.
 * 
 * @author nroberts
 */
public class TrainingAppEditor extends AbstractCourseObjectEditor<TrainingApplication>{

	/**
	 * The view being modified by this editor's presenter
	 */
	private TrainingAppViewImpl viewImpl;
	
	/**
	 * Creates a new editor
	 */
	public TrainingAppEditor(){
		
		viewImpl = new TrainingAppViewImpl();		
		presenter = new TrainingAppPresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(TrainingApplication courseObject) {		
		((TrainingAppPresenter) presenter).edit(courseObject);
	}
	
	/**
	 * Assigns a listener that will be notified when the user selects a different type of application
	 * 
	 * @param command the listener command. Can be null.
	 */
	public void setChoiceSelectionListener(Command command) {
		((TrainingAppPresenter) presenter).setChoiceSelectionListener(command);
	}
}
