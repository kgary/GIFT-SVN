/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.aar;

import generated.course.AAR;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.AarPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link AAR} course objects.
 * 
 * @author nroberts
 */
public class AarEditor extends AbstractCourseObjectEditor<AAR> {

	/**
	 * The view being modified by this editor's presenter
	 */
	private AarViewImpl viewImpl;
	
	/**
	 * Creates a new editor
	 */
	public AarEditor(){
		
		viewImpl = new AarViewImpl();		
		presenter = new AarPresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(AAR courseObject) {		
		((AarPresenter) presenter).edit(courseObject);
	}
}
