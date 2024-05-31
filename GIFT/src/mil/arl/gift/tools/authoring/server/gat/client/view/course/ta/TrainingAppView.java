/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.math.BigInteger;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

import generated.course.TrainingApplication;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;

/**
 * The Interface AarView.
 */
public interface TrainingAppView extends IsWidget, IsSerializable{
	
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
	 * Gets the full screen input.
	 *
	 * @return the full screen input
	 */
	HasValue<Boolean> getFullScreenInput();
	
	/**
     * Gets the full screen has enabled interface
     *  
     * @return the full screen has enabled interface
     */
	HasEnabled getFullScreenHasEnabled();
	
    /**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasValue<Boolean> getDisabledInput();

    /**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasEnabled getDisabledInputHasEnabled();
	
	/**
	 * Gets the disable tutoring input.
	 *
	 * @return the disable tutoring input
	 */
	HasValue<Boolean> getDisableInstrInput();
	
	/**
	 * Gets the disable tutoring has enabled interface
	 *  
	 * @return the disable tutoring has enabled interface
	 */
	HasEnabled getDisableTutoringHasEnabled();

	/**
	 * Gets the show avatar input.
	 *
	 * @return the show avatar input
	 */
	CheckBox getShowAvatarInput();
	
	/**
     * Gets the show avatar has enabled interface
     *  
     * @return the show avatar has enabled interface
     */
	HasEnabled getShowAvatarHasEnabled();

	/**
	 * Gets the choose avatar panel has visibility interface.
	 *
	 * @return the choose avatar panel has visibility interface
	 */
	HasVisibility getChooseAvatarButtonInputHasVisibility();
	
	/**
     * Gets the avatar selected panel has visibility interface.
     *
     * @return the avatar selected panel has visibility interface
     */
	HasVisibility getAvatarSelectedHasVisibility();

	/**
	 * Gets the choose avatar button input.
	 *
	 * @return the choose avatar button input
	 */
	HasClickHandlers getChooseAvatarButtonInput();
	
	/**
     * Gets the remove avatar button input.
     *
     * @return the remove avatar button input
     */
	HasClickHandlers getRemoveAvatarButtonInput();
	
	/**
     * Gets the remove avatar has enabled interface
     *  
     * @return the remove avatar has enabled interface
     */
	HasEnabled getRemoveAvatarHasEnabled();

	/**
	 * Show confirm create guidance dialog.
	 *
	 * @param callback the callback
	 */
	void showConfirmCreateGuidanceDialog(OkayCancelCallback callback);

	/**
	 * Gets the label used to show the avatar file name
	 * 
	 * @return the label used to show the avatar file name
	 */
	HasText getAvatarFileLabel();
	
	/**
     * Gets the label used to indicate to pick an avatar file
     * 
     * @return the label used to indicate to pick an avatar file
     */
	HasText getSelectAvatarFileLabel();

	/**
	 * Gets the training application that the interop editor should modify
	 * 
	 * @param app the training application that the interop editor should modify
	 */
	void setInteropEditorTrainingApplication(TrainingApplication app);

	/**
	 * Sets the path to the current course file.
	 * 
	 * @param coursePath The course path.
	 */
	void setCourseFolderPath(String coursePath);

	/**
	 * Sets the survey context id that the course is using. 
	 * 
	 * @param courseSurveyContextId - The survy context id that the course is using.
	 */
    void setCourseSurveyContextId(BigInteger courseSurveyContextId);

	/**
	 * Gets the guidance creator
	 * 
	 * @return the guidance creator
	 */
	OptionalGuidanceCreator getGuidanceCreator();

	/**
	 * Sets the command to execute when the training application type changes
	 * 
	 * @param command the command to execute
	 */
	void setChoiceSelectionListener(Command command);

	/**
	 * Sets whether or not the input element used to set the avatar to full screen should be visible
	 * 
	 * @param visible whether or not the element should be visible
	 */
	void setAvatarFullScreenInputVisible(boolean visible);
	
	/**
     * Gets the button used to change the training application type
     * 
     * @return the button used to change types. Will not be null.
     */
    public Button getChangeApplicationButton();

	/**
	 * Sets the remediation to load into the remediation panel. Also refreshes the list of remediation
	 * files to match the given list of course concepts.
	 * 
	 * @param remediation the remediation to load. Can be null if no remediation data should be shown.
	 * @param courseConcepts the list of concepts that this remediation should be associated with. Can be null.
	 * @param showNoConceptsError whether to show error messages when the provided course concepts are empty.
	 * Can be helpful when resetting concepts.
	 */
    void setRemediation(generated.course.TrainingApplication.Options.Remediation remediation, 
            List<String> courseConcepts, boolean showNoConceptsError);

    /**
     * Gets the button that the author must click to add remediation
     * 
     * @return the add remediation button
     */
    HasClickHandlers getAddRemediationButton();

    /**
     * Gets the button that the author must click to remove remediation
     * 
     * @return the remove remediation button
     */
    HasClickHandlers getDeleteRemediationButton();

    /**
     * Sets whether the author should be able to edit remediation
     * 
     * @param enabled whether editing remediation should be enabled
     */
    void setRemediationEditingEnabled(boolean enabled);

    /**
     * Sets the command to invoke whenever the training app's referenced DKF file is changed.
     * 
     * @param command the command to invoke. Can be null, if no command should be invoked.
     */
    void setDkfChangedCommand(Command command);
	
}
