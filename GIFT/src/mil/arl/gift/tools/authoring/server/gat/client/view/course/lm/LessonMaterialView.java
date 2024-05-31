/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.lm;

import java.util.List;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

import generated.course.LessonMaterial;
import generated.course.LtiProvider;
import generated.course.Media;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;

/**
 * The Interface LessonMaterialView.
 */
public interface LessonMaterialView extends IsWidget, IsSerializable{
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter{

		/**
		 * Start.
		 */
		void start();
		
		/**
		 * Stop.
		 */
		void stop();
	}
	
	/**
	 * Gets the media list display.
	 *
	 * @return the media list display
	 */
	HasData<Media> getMediaListDisplay();
	
	/**
     * Gets the Concepts table in the media
     * 
     * @return the Concepts table
     */
    public HasData<CandidateConcept> getMediaConceptsTable();
    
    /**
     * Gets the list of LTI providers
     * 
     * @return the list of LTI providers
     */
    public List<LtiProvider> getLtiProviderIdList();

	/**
	 * Gets the adds the media input.
	 *
	 * @return the adds the media input
	 */
	HasClickHandlers getAddMediaInput();
	
	/**
	 * Gets the button to retrigger validation
	 * @return the revalidate button
	 */
	HasClickHandlers getValidateMediaElementsButton();

	/**
	 * Redraws the view.
	 */
	void redraw();
	
	Column<Media, String> getEditColumn();

	Column<Media, String> getPreviewColumn();

	Column<Media, String> getRemoveColumn();

    public void showMediaCollection();
	
	/**
	 * Displays a single media type editor 
	 * 
	 * @param lessonMaterial The lesson material
	 */
	void showMediaType(LessonMaterial lessonMaterial);
	
	HasClickHandlers getReplaceSlideShowInput();

	DefaultGatFileSelectionDialog getAddSlideShowInput();

    /**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasValue<Boolean> getDisabledInput();
    
    /**
     * Sets whether or not the lesson material options panel should be visible
     * 
     * @param visible whether or not the lesson material options panel should be visible
     */
    void setLmOptionsVisible(boolean visible);
}
