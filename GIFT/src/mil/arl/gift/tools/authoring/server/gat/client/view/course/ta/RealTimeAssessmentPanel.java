/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.util.HashMap;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFileExists;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;

/**
 * A widget for displaying the Real-Time Assessment label
 * 
 * @author bzahid
 */
public class RealTimeAssessmentPanel extends Composite implements CourseReadOnlyHandler {
	
	private static RealTimeAssessmentPanelUiBinder uiBinder = GWT.create(RealTimeAssessmentPanelUiBinder.class);
	
	interface RealTimeAssessmentPanelUiBinder extends UiBinder<Widget, RealTimeAssessmentPanel> {
		
	}
	
	private static final String READ_ONLY_MSG = "Unavailable in Read-Only mode";
	
	/** default text to use as label when a custom name is not provided */
    private static final String DEFAULT_LABEL = "Real-Time Assessment";
	
	@UiField
	protected Button addButton;
	
	@UiField
	protected Button editButton;
	
	@UiField
	protected Button copyButton;
	
	@UiField
	protected Button deleteButton;
	
	@UiField
	protected FlowPanel addPanel;
	
	@UiField
	protected FlowPanel buttonPanel;
	
	@UiField
	protected DeckPanel assessmentPanel;
	
	@UiField
	protected Tooltip addTooltip;
	
	@UiField
	protected Tooltip copyTooltip;
	
	@UiField
	protected Tooltip deleteTooltip;
	
	/** the label shown when a real time assessment has been set, right under the buttons such as Edit */
	@UiField
	protected Label label;
	
	/** optional custom label used to over ride the default label */
	private String customNameOfAssessedItem = DEFAULT_LABEL;
		
	/** path to the assessed object (e.g. dkf.xml, conversationTree.xml), null if not set yet */
	private String assessmentPath = null;
	
	public RealTimeAssessmentPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
		CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
		
		assessmentPanel.showWidget(0);
				
		setReadOnlyMode(GatClientUtility.isReadOnly());
		
		copyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(assessmentPath != null) {
					BsLoadingDialogBox.display("Copying "+customNameOfAssessedItem+"", "Please wait...");
					HashMap<String, String> sourceToTargetMap = new HashMap<String, String>();					
					sourceToTargetMap.put(assessmentPath, assessmentPath);
					
					CopyWorkspaceFiles action = new CopyWorkspaceFiles(GatClientUtility.getUserName(), sourceToTargetMap, true);
					action.setAppendTimestamp(true);
					SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CopyWorkspaceFilesResult>() {

						@Override
						public void onFailure(Throwable caught) {
							BsLoadingDialogBox.remove();
							ErrorDetailsDialog dialog = new ErrorDetailsDialog("A server error occurred while copying the file.", caught.toString(), null);
							dialog.setText("Copy Failed");
							dialog.center();
						}

						@Override
						public void onSuccess(CopyWorkspaceFilesResult result) {
							BsLoadingDialogBox.remove();
							
							if(result.isSuccess()) {
								WarningDialog.info("File Copied", "Created <b>" + result.getCopiedFilename() + "</b> in your course folder.");
								
							} else {
								ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
								dialog.setText("Copy Failed");
								dialog.center();
							}
						}
						
					});
					
				} else {
					WarningDialog.error("Copy Failed", "There is no "+customNameOfAssessedItem+" available to copy.");
				}
				
				
			}
		});
	}
		
	/**
	 * Disables the interface 
	 */
	public void setReadOnlyMode(boolean readOnly) {
		addButton.setText("No "+customNameOfAssessedItem+" Selected");
		
		addButton.setEnabled(!readOnly);
		copyButton.setEnabled(!readOnly);
		deleteButton.setEnabled(!readOnly);
		
		addTooltip.setTitle(readOnly ? READ_ONLY_MSG : null);
		copyButton.setTitle(readOnly ? READ_ONLY_MSG : null);
		deleteButton.setTitle(readOnly ? READ_ONLY_MSG : null);
	}
	
	/**
	 * Set the display name for the object being assessed (e.g. conversation tree).  
	 *  
	 * @param label can be null or empty to set the label to the default value of {@link #DEFAULT_LABEL}.
	 */
	public void setNameOfAssessedItem(String name){
	    
	    if(StringUtils.isBlank(name)){
	        customNameOfAssessedItem = DEFAULT_LABEL;
	    }else{
	        customNameOfAssessedItem = name;
	    }
	    
	    label.setText(customNameOfAssessedItem);
	    addButton.setText("No "+customNameOfAssessedItem+" Selected");
	}
	
	/**
	 * Sets the realtime-assessment object. If the assessment is valid, hides the "Click to Add" button
	 * and replaces it with Edit, Copy and Remove buttons. Else, an error dialog is displayed and the
	 * "Click to Add" button is displayed.<br/>
     * Note: will reset the name of the assessed item to the default of {@value #DEFAULT_LABEL}.
	 * 
	 * @param path The path to the real-time assessment file.  Can't be null or empty.
	 * @throws IllegalArgumentException Throws if the path argument is null or empty
	 */
	public void setAssessment(String path) {
	    setAssessment(path, null);
	}

	/**
	 * Sets the real-time assessment object. If the assessment is valid, hides the "Click to Add" button
	 * and replaces it with Edit, Copy and Remove buttons. Else, an error dialog is displayed and the
	 * "Click to Add" button is displayed.<br/>
	 * Note: will reset the name of the assessed item to the default of {@value #DEFAULT_LABEL}.
	 * 
	 * @param path The path to the real-time assessment file.  Can't be null or empty.
	 * @param currentTrainingApp The application the real time assessment is being used for
	 * @throws IllegalArgumentException Throws if the path argument is null or empty
	 */
	public void setAssessment(String path, AsyncCallback<GatServiceResult> callback) {
	    
	    if (path == null || path.isEmpty()) {
	        throw new IllegalArgumentException("The path to the "+customNameOfAssessedItem+" file cannot be null or empty.");
	    }
	    
	    setNameOfAssessedItem(null);
	    
	    WorkspaceFileExists action = new WorkspaceFileExists(GatClientUtility.getUserName(),
	            GatClientUtility.getBaseCourseFolderPath() +
	            "/" + path);
	    
	    if (callback == null) {
	        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                @Override
                public void onFailure(Throwable thrown) {
                    removeAssessment();
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            thrown.getMessage(),
                            "The server was unable to complete the check for the existence of the user defined uri",
                            DetailedException.getFullStackTrace(thrown)
                    );
                    dialog.center();
                }

                @Override
                public void onSuccess(GatServiceResult result) {
                    if (!result.isSuccess()) {
                        WarningDialog.error(customNameOfAssessedItem+" Missing", "The "+customNameOfAssessedItem+" could not be found. The reference will be removed but will not be commited until the next time the course is saved.<br /><br />" +
                                            "<b>Error message: </b>" + result.getErrorMsg(), new ModalDialogCallback() {
                                                @Override
                                                public void onClose() {
                                                    removeAssessment();
                                                } });
                    }
                }
	            
	        });
	    } else {
	        SharedResources.getInstance().getDispatchService().execute(action, callback);
	    }
	    
		assessmentPath = GatClientUtility.getBaseCourseFolderPath() + "/" + path;
		assessmentPanel.showWidget(assessmentPanel.getWidgetIndex(buttonPanel));
	}
	
	/**
	 * Restores the 'Click to Add' button on the real time assessment panel to allow the author
	 * to create a new dkf.
	 */
	public void removeAssessment() {
		assessmentPanel.showWidget(assessmentPanel.getWidgetIndex(addPanel));
	}
	
	/**
	 * Hides the edit button.
	 */
	public void hideEditButton() {
		editButton.setVisible(false);
	}
	
	/**
	 * Hides or shows the 'Click to Add' button
	 * 
	 * @param show True to show the button, false to hide it.
	 */
	public void showAddButton(boolean show) {
		addButton.setVisible(show);
	}
	
	/**
	 * Gets the edit button for adding click handlers.
	 * 
	 * @return The edit button
	 */
	public HasClickHandlers getEditButton() {
		return editButton;
	}
	
	/**
	 * Gets the delete button for adding click handlers.
	 * 
	 * @return The delete button
	 */
	public HasClickHandlers getDeleteButton() {
		return deleteButton;
	}
	
	/**
	 * Gets the copy button for adding click handlers.
	 * 
	 * @return The copy button
	 */
	public HasClickHandlers getCopyButton() {
		return copyButton;
	}
	
	/**
	 * Gets the 'Click to Add' button for adding click handlers.
	 * 
	 * @return The 'Click to Add' button
	 */
	public Button getAddAssessmentButton() {
		return addButton;
	}

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		addButton.setEnabled(!isReadOnly);
		editButton.setEnabled(!isReadOnly);
		copyButton.setEnabled(!isReadOnly);
		deleteButton.setEnabled(!isReadOnly);
	}
}
