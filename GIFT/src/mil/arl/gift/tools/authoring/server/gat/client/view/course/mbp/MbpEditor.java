/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import generated.course.Course;
import generated.course.MerrillsBranchPoint;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.MbpPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link MerrillsBranchPoint} course objects.
 * 
 * @author nroberts
 */
public class MbpEditor extends AbstractCourseObjectEditor<MerrillsBranchPoint>{
	
	/** The course that is currently loaded */
	private Course loadedCourse = null;

	/**
	 * The view being modified by this editor's presenter
	 */
	private MbpViewImpl viewImpl;
	
	/**
	 * Creates a new editor
	 * 
	 * @param course contains the currently authored contents of the course that contains
	 * the adaptive courseflow course object
	 */
	public MbpEditor(Course course){
		
		loadedCourse = course;
		
		viewImpl = new MbpViewImpl();		
		presenter = new MbpPresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(MerrillsBranchPoint courseObject) {		
		((MbpPresenter) presenter).edit(courseObject, loadedCourse);
	}
}
