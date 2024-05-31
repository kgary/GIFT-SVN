/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import generated.course.LtiProvider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.consumer.LtiConsumerPropertiesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.provider.LtiProviderPropertiesPanel;

/**
 * The Interface Course2HeaderView.
 *
 * @author nroberts
 */
public interface HeaderView {

	/**
	 * Gets the name input.
	 *
	 * @return the name input
	 */
	public HasValue<String> getNameInput();

	/**
	 * Gets the description input.
	 *
	 * @return the description input
	 */
	public HasValue<String> getDescriptionInput();
	
	/**
	 * Sets the sort handler for the concept list.
	 * @param sortHandler Sort handler for the concept list.
	 */
	public void setSortHandlerForConceptList(ListHandler<Concepts.List.Concept> sortHandler);
	
	/**
     * Gets the LTI providers list.
     * @return the LTI providers list.
     */
    public HasData<LtiProvider> getLtiProviderList();
	
	/**
     * Gets the button that signals the user wants to add an LTI provider to the list
     * @return Button that adds an LTI provider to the list
     */
    public HasClickHandlers getAddLtiProviderListButton();

	/**
	 * Sets the character limit of the name textbox for this transition's header.
	 * 
	 * @param limit maximum number of characters to allow.
	 */
	void setNameLimit(int limit);

	/**
	 * Gets the sensor file selection dialog
	 * 
	 * @return the sensor file selection dialog
	 */
	HasValue<String> getSensorFileDialog();

	/**
	 * Gets the learner file selection dialog
	 * 
	 * @return the learner file selection dialog
	 */
	HasValue<String> getLearnerFileDialog();

	/**
	 * Gets the pedagogical file selection dialog
	 * 
	 * @return the pedagogical file selection dialog
	 */
	HasValue<String> getPedagogicalFileDialog();

	/**
	 * Gets the pedagogical file selection dialog
	 * 
	 * @return the pedagogical file selection dialog
	 */
	HasValue<String> getCourseImageFileDialog();

	/**
	 * Gets the course image name label
	 * 
	 * @return the course image name label
	 */
	HasText getCourseImageNameLabel();
	
	/**
     * Gets the date of the last successful course validation
     * 
     * @return the date of the last successful course validation
     */
    HasText getLastSuccessfulValidationLabel();
    
    /**
     * Gets the date of the last course folder modification
     * 
     * @return the date of the last course folder modification
     */
    HasText getLastModifiedLabel();
    
    /**
     * Gets the date of the last modification to the survey context of a course
     * 
     * @return the date of the last modification to the survey context of a course
     */
    HasText getSurveyContextLastModifiedLabel();

	/**
	 * Gets the checkbox used to specify whether or not to hide the course
	 * 
	 * @return the checkbox
	 */
	HasValue<Boolean> getExcludeCheckBox();
	/**
     * Gets the column used to determine if we should protect the client data or not
     * 
     * @return the column used to determine if we should protect the client data or not
     */
	Column<LtiProvider, Boolean> getLtiProviderListProtectClientDataColumn();
	
	/**
     * Gets the column used to edit LTI providers
     * 
     * @return the column used to edit LTI providers
     */
    Column<LtiProvider, String> getLtiProviderListEditColumn();
    
    /**
     * Gets the column used to remove LTI providers
     * 
     * @return the column used to remove LTI providers
     */
    Column<LtiProvider, String> getLtiProviderListRemoveColumn();

	/**
	 * Sets whether to show the panel for an empty concept hierarchy or the concept hierarchy tree itself
	 * 
	 * @param show whether to show the panel for an empty concept hierarchy or the concept hierarchy tree itself
	 */
	void showConceptHierarchyEmptyPanel(boolean show);
	
	/**
     * Sets whether to show the panel for an empty LTI provider list or the LTI provider list itself
     * 
     * @param show whether to show the panel for an empty LTI provider list or the LTI provider list itself
     */
    void showLtiProvidersListEmptyPanel(boolean show);

	/**
     * Adds a handler to handle when an empty LTI provider list panel is clicked
     * 
     * @param handler the handler
     * @return the handler's registration
     */
    HandlerRegistration addEmptyLtiProvidersListPanelClickHandler(ClickHandler handler);

	/**
	 * returns the clear image button on the header view
	 * 
	 * @return Button returned 
	 */
	Image getClearImageButton();

	/**
	 * Sets clear image button to visible or not
	 * 
	 * @param visible
	 */
	void setClearImageButtonVisible(boolean visible);

	/**
	 * Returns the preview Image icon
	 * 
	 * @return preview icon
	 */
	Icon getPreviewTileIcon();

	/**
	 * Sets the preview image icon visible or not
	 * 
	 * @param visible
	 */
	void setPreviewTileIconVisible(boolean visible);

	/**
	 * Displays the pedagogical configuration editor
	 * 
	 * @param coursePath The path to the current course
	 * @param url The url of the pedagogical file. If null, a new file will be created.
	 */
	void showPedConfigEditor(String coursePath, String url);

	/**
	 * Displays the sensor configuration editor
	 * 
	 * @param coursePath The path to the current course
	 * @param url The url of the pedagogical file. If null, a new file will be created.
	 */
	void showSensorConfigEditor(String coursePath, String url);

	/**
	 * Displays the learner configuration editor
	 * 
	 * @param coursePath The path to the current course
	 * @param url The url of the pedagogical file. If null, a new file will be created.
	 */
	void showLearnerConfigEditor(String coursePath, String url);

	/**
	 * Presents a dialog to the user with the options to create a new learner configuration,
	 * select or edit an existing one, or use the default configuration.
	 */
	void showLearnerSelectDialog();

	/**
	 * Presents a dialog to the user with the options to create a new pedagogical configuration,
	 * select or edit an existing one, or use the default configuration.
	 */
	void showPedSelectDialog();

	/**
	 * Presents a dialog to the user with the options to create a new sensor configuration,
	 * select or edit an existing one, or use the default configuration.
	 */
	void showSensorSelectDialog();

	/**
	 * Gets the panel containing editable configurations
	 * 
	 * @return a panel containing editable configurations
	 */
	Widget getConfigurationsWidget();
	
	/**
	 * Gets the panel containing editable course properties
	 * 
	 * @return a panel containing editable course properties
	 */
	Widget getCoursePropertiesWidget();
    
    /**
     * Gets the panel containing the LTI consumer properties
     * 
     * @return a panel containing the LTI consumer properties
     */
	LtiConsumerPropertiesPanel getLTIConsumerPropertiesPanel();

	/**
     * Gets the panel containing the LTI provider properties
     * 
     * @return a panel containing the LTI provider properties
     */
    LtiProviderPropertiesPanel getLTIProviderPropertiesPanel();
	
	/**
	 * Adds a click handler to the description label in the properties panel
	 * 
	 * @param addHandler The click handler to execute when the description label is clicked.
	 */
	void setAddDescriptionHandler(ClickHandler addHandler);
	
	/**
	 * Gets the properties concept panel
	 * 
	 * @return The concept properties panel
	 */
	ConceptHierarchyPanel getConceptsPanel();
	
	/**
     * Gets the properties LTI providers panel
     * 
     * @return The LTI providers panel
     */
    Widget getLtiProvidersPanel();
	
	/**
	 * Gets the concept list from the properties concept panel
	 * 
	 * @return The concept list.
	 */
	HasData<Concept> getSimpleConceptList();
	
	/**
	 * Adds a click handler to the concept panel in the properties panel
	 * 
	 * @param clickHandler The click handler to execute when the properties panel is clicked.
	 */
	void setPropertiesConceptsClickHandler(ClickHandler clickHandler);
    
	/**
     * Adds a click handler to the LTI consumer panel in the properties panel
     * 
     * @param clickHandler The click handler to execute when the edit button for LTI Consumer properties panel is clicked.
     */
    void setLtiConsumerClickHandler(ClickHandler clickHandler);

	/**
	 * Gets the course image displayed in the properties panel
	 * 
	 * @return The course image
	 */
	Image getCourseTileImage();

	/**
	 * Shows the course image file selection dialog
	 */
	void showCourseImageFileDialog();

	/**
	 * Gets the data provider for the concept hierarchy displayed in the properties panel
	 * 
	 * @return The concept hierarchy data provider
	 */
	HashMap<ConceptNode, ListDataProvider<ConceptNode>> getPropertiesHierarchyDataProviders();

	/**
	 * Gets the root node of the concept hierarchy displayed in the properties panel.
	 *  
	 * @return The root concept node
	 */
	TreeNode getSimpleRootConceptTreeNode();

	
	/** 
     * Sets whether or not this editor should be Read-Only
     * 
     * @param readOnly whether or not this editor should be Read-Only
     */
    void setReadOnly(boolean readOnly);
    
    
    /** 
     * Sets whether or not the tooltip should be visible or not.
     * 
     * @param visible True to make the tooltip visible, false otherwise.
     */
    void setPedTooltipVisibility(boolean visible);
    
    /** 
     * Sets whether or not the tooltip should be visible or not.
     * 
     * @param visible True to make the tooltip visible, false otherwise.
     */
    void setCourseImageTooltipVisibility(boolean visible);
    
    /** 
     * Sets whether or not the tooltip should be visible or not.
     * 
     * @param visible True to make the tooltip visible, false otherwise.
     */
    void setLearnerTooltipVisibility(boolean visible);

	HasClickHandlers getCourseTileImageButton();
	
    /**
     * Gets the button that signals the user wants to client to request updated
     * course history information.
     * @return Button that causes the client to query the server for course history information
     */
	HasClickHandlers getRefreshCourseHistoryButton();

	Button getLearnerFileButton();

	Button getPedFileButton();
	
	Button getPedEditButton();
	
	Button getLearnerEditButton();

	/**
	 * Loads the concepts from the given list into the concept list editor
	 * 
	 * @param list the loaded concepts
	 */
    void setConceptsList(List<Concepts.List.Concept> list);

    /**
     * Populates the hierarchy previous in the properties panel with the given concept information
     * 
     * @param root the root node of the loaded concept hierarchy. Cannot be null.
     */
    void populatePropertiesHierarchy(ConceptNode root);
}
