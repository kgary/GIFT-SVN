/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.course.Concepts;
import generated.course.Course;
import mil.arl.gift.common.course.CourseFileAccessDetails.CourseFileUserPermissionsDetails;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.HeaderView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseTree;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;

/**
 * The Interface CourseView.
 */
public interface CourseView extends IsWidget, Serializable {			
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter {

		//used by Activity
		/**
		 * Start.
		 *
		 * @param containerWidget the container widget
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 */
		void start(AcceptsOneWidget containerWidget, HashMap<String, String> startParams);
		
		/**
		 * Confirm stop.
		 *
		 * @return the string
		 */
		String confirmStop();

		/**
		 * Stop.
		 */
		void stop();
	}    

	/**
	 * Initialize view.
	 */
	void initializeView();	

	/**
	 * Gets the course header editor view.
	 * 
	 * @return the course header editor view
	 */
	HeaderView getHeaderView();

	/**
	 * Sets the file save command.
	 *
	 * @param command the new file save command
	 */
	void setFileSaveCommand(ScheduledCommand command);

	/**
	 * Sets the file discard changes command.
	 *
	 * @param command the new file discard changes command
	 */
	void setFileDiscardChangesCommand(ScheduledCommand command);

	/**
	 * Sets the save and validate file menu item command.
	 *
	 * @param command the new save and validate file menu item command
	 */
	void setFileSaveAndValidateCommand(ScheduledCommand command);
	
	/**
	 * Display the course description editor.
	 */
	void displayDescriptionEditor();
	
	/** 
	 * Sets whether or not this editor should be Read-Only
	 * 
	 * @param readOnly whether or not this editor should be Read-Only
	 */
	void setReadOnly(boolean readOnly);

	//void clearView();
	
	/**
	 * Creates a new course tree using the given course
	 * 
	 * @param course the course to create the tree with
	 */
	void createCourseTree(Course course);

	/**
	 * Hides the current course object modal.
	 */
	void hideCourseObjectModal();
	

	Widget getAddGuidanceButton();

	Widget getAddTAButton();

	Widget getAddAARButton();

	Widget getAddMBPButton();
	
	Widget getAddLTIButton();

	Widget getAddSurveyButton();
	
	Widget getAddAutoTutorButton();

	Widget getAddConversationTreeButton();

	Widget getAddQuestionBankButton();

	Widget getAddLessonMaterialButton();
	
	Widget getAddUnityButton();
	
	Widget getAddMobileAppButton();
	
	CourseTree getCourseTree();
	
	HTML getDiskSpaceLabel();
	
	HTML getHelpHTML();
	
	void setSaveDescriptionCommand(ScheduledCommand command);

	String getEditorDescription();

	/**
	 * Gets the course name button in the course toolbar
	 * 
	 * @return The course name button
	 */
	HasValue<String> getCourseNameButton();

	/**
	 * Shows or hides the read only label in the course toolbar
	 * 
	 * @param show Whether or not to display the read only label
	 */
	void showReadOnlyLabel(boolean show);

	/**
	 * Gets the course path label in the advanced properties panel
	 * 
	 * @return The course path label
	 */
	Label getCoursePathLabel();

	/**
	 * Gets the dialog used to display GIFT Wrap
	 * 
	 * @return the dialog
	 */
	CourseObjectModal getGiftWrapDialog();

	/** 
	 * Refreshes the media file list
	 */
	void refreshMediaList();

	/**
	 * Gets the panel used to hold editors for course objects
	 * 
	 * @return the editor panel
	 */
	CourseObjectEditorPanel getEditorPanel();

	Widget getAddVbsButton();

	Widget getAddTc3Button();

	Widget getAddTestbedButton();

	Widget getAddAresButton();

	Widget getAddExampleAppButton();

	Widget getAddSlideShowButton();

	Widget getAddAuthoredBranchButton();

	TreeManager getTreeManager();
    
    Widget getAddImageButton();
    
    Widget getAddVideoButton();

    Widget getAddPDFButton();

    Widget getAddWebAddressButton();

    Widget getAddYoutubeVideoButton();

    Widget getAddLocalWebpageButton();
    
    HasClickHandlers getShowUserActionListButton();

    /**
     * Show or hide the lock label.
     * 
     * @param showLock True to show the lock label, false otherwise.
     */
    void showLockLabel(boolean showLock);

    /**
     * Set the command that will be executed when the unlock course button is pressed.
     * 
     * @param command The command to be executed when the unlock course button is pressed.
     */
    void setUnlockCourseCommand(final ScheduledCommand command);
    
    void setUserAccessList(List<CourseFileUserPermissionsDetails> userAccessList);
    
    void showUserAccessList();

    /**
     * Gets the button used to create VR-Engage course objects
     * 
     * @return the VR-Engage button
     */
    Widget getAddVREngageButton();
    
    /**
     * Gets the button used to create standalone Unity course objects
     * 
     * @return the Unity button
     */
    Widget getAddUnityStandaloneButton();
    
    /**
     * Gets the button used to create HAVEN course objects
     * 
     * @return the HAVEN button
     */
    Widget getAddHAVENButton();
    
    /**
     * Gets the button used to create RIDE course objects
     * 
     * @return the RIDE button
     */
    Widget getAddRIDEButton();

    /**
     * Load the given concepts into the concept editor and display it
     * 
     * @param concepts the concepts to load and display. Cannot be null.
     */
    void showConceptsEditor(Concepts concepts);

    /**
     * Sets the command that should be invoked when the course properties dialog is closed (via save or cancel)
     * 
     * @param command the command to invoke. Can be null, if no special behavior should be invoked when the dialog is closed.
     */
    void setPropertiesDialogClosedCommand(Command command);

    /**
     * Opens the given transition while treating it as an scenario from an external application
     * 
     * @param transition the transition to treat as a scenario. Cannot be null.
     */
    void editExternalScenarioDkf(Serializable transition);
}
