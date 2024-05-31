/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;

import com.google.gwt.user.client.Command;

import generated.course.Guidance;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.GuidancePresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;

/**
 * An editor that modifies {@link Guidance} course objects.
 * 
 * @author nroberts
 */
public class GuidanceEditor extends AbstractCourseObjectEditor<Guidance>{

	/**
	 * The view being modified by this editor's presenter
	 */
	private GuidanceViewImpl viewImpl;
	
	/**
	 * Creates a new editor
	 */
	public GuidanceEditor(){
		
		viewImpl = new GuidanceViewImpl();		
		presenter = new GuidancePresenter(viewImpl);
		
		setWidget(viewImpl);
	}

	@Override
	protected void editObject(Guidance courseObject) {		
		((GuidancePresenter) presenter).edit(courseObject);
	}
	
	/**
	 * Assigns a listener that will be notified when the user selects a different type of guidance
	 * 
	 * @param command the listener command
	 */
	public void setChoiceSelectionListener(Command command){
		((GuidancePresenter) presenter).setChoiceSelectionListener(command);
	}
	
	/**
	 * Show or hide the informative message editor.
	 * 
	 * @param hide True to hide the informative message editor.
	 */
	public void hideInfoMessage(boolean hide) {
		((GuidancePresenter) presenter).hideInfoMessage(hide);
	}
	
	/**
     * Show or hide the disabled option in the option panel.
     * 
     * @param hide True to hide the disabled option.
     */
	public void hideDisabledOption(boolean hide) {
	    viewImpl.hideDisabledOption(hide);
    }

	/**
	 * Sets whether or not this editor is embedded within a training application editor and adjusts its UI components appropriately
	 * 
	 * @param isTrainingAppEmbedded whether this editor is embedded within a training application editor
	 */
    public void setTrainingAppEmbedded(boolean isTrainingAppEmbedded) {
        viewImpl.setTrainingAppEmbedded(isTrainingAppEmbedded);
    }
}
