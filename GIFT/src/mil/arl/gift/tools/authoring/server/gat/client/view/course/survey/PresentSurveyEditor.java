/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import generated.course.Course;
import generated.course.PresentSurvey;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.PresentSurveyPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link PresentSurvey} course objects.
 * 
 * @author nroberts
 */
public class PresentSurveyEditor extends AbstractCourseObjectEditor<PresentSurvey>{
	
	/** The course that is currently loaded */
	private Course loadedCourse = null;

	/**
	 * The view being modified by this editor's presenter
	 */
	private PresentSurveyViewImpl viewImpl;
	
	/**
	 * Creates a new editor
	 */
	public PresentSurveyEditor(Course course){
		
		loadedCourse = course;
		
		viewImpl = new PresentSurveyViewImpl();		
		presenter = new PresentSurveyPresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(PresentSurvey courseObject) {		
		((PresentSurveyPresenter) presenter).edit(courseObject, loadedCourse);
	}
}
