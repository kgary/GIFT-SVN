/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.lm;

import com.google.gwt.user.client.ui.RequiresResize;

import generated.course.Course;
import generated.course.LessonMaterial;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.LessonMaterialPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link LessonMaterial} course objects.
 * 
 * @author nroberts
 */
public class LessonMaterialEditor extends AbstractCourseObjectEditor<LessonMaterial> implements RequiresResize{

    /** The course that is currently loaded */
    private Course loadedCourse = null;
    
	/**
	 * The view being modified by this editor's presenter
	 */
	private LessonMaterialViewImpl viewImpl;
	
    /**
     * Creates a new editor
     * 
     * @param course contains the currently authored contents of the course that contains the LTI
     *            course object.
     */
	public LessonMaterialEditor(Course course){
		
	    loadedCourse = course;
	    
		viewImpl = new LessonMaterialViewImpl();		
		presenter = new LessonMaterialPresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(LessonMaterial courseObject) {		
		((LessonMaterialPresenter) presenter).edit(courseObject, loadedCourse);
	}

	@Override
	public void onResize() {
		viewImpl.redraw();
	}
	
}
