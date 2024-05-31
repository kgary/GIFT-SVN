/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateMetadataAttribute;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import generated.metadata.Metadata;

/**
 * A dialog used to let users select remediation content files to use in a Merrill's Branch Point
 * 
 * @author nroberts
 */
public class AddRemediationDialog extends Composite {

	private static final Binder binder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, AddRemediationDialog> {
	}

	private static final Logger logger = Logger.getLogger(AddRemediationDialog.class.getName());
	
	@UiField
	protected Modal addContentModal;
	
	private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();
	
	@UiField(provided=true)
	protected CellTable<CandidateConcept> conceptsTable = new CellTable<CandidateConcept>();
	
	@UiField(provided=true)
	protected CellTable<CandidateMetadataAttribute> attributesTable = new CellTable<CandidateMetadataAttribute>();
	
	
	//'Concepts:' table columns ----------------------------------------------------------------------------------------------------------
	
	/** The checkbox column for the 'Concepts:' table */
    private Column<CandidateConcept, Boolean> conceptSelectionColumn = new Column<CandidateConcept, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateConcept candidate) {
			
			return candidate.isChosen();
		}
    	
    };
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateConcept, String> conceptNameColumn = new Column<CandidateConcept, String>(new TextCell()){

		@Override
		public String getValue(CandidateConcept candidate) {

			if(candidate.getConceptName() != null ){
				return candidate.getConceptName();
			}
			
			return null;
		}
    	
    };
    
    //'Attributes:' table columns --------------------------------------------------------------------------------------------------------
	
    /** The checkbox column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, Boolean> attributeSelectionColumn = new Column<CandidateMetadataAttribute, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateMetadataAttribute candidate) {
			
			return candidate.isChosen();
		}
    	
    };
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, String> attributeNameColumn = new Column<CandidateMetadataAttribute, String>(new TextCell()){

		@Override
		public String getValue(CandidateMetadataAttribute candidate) {

			if(candidate.getAttribute() != null ){
				return candidate.getAttribute().getDisplayName();
			}
			
			return null;
		}
    	
    };
    
    @UiField
    protected DockPanel conceptsDock;
    
    @UiField
    protected FlowPanel attributesPanel;
    
    @UiField
    protected HTML validationErrorText;
    
	@UiField
	protected Button uploadButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected InlineHTML title;
	
	@UiField
	protected ContentReferenceEditor referenceEditor;
	
	@UiField
	protected Button changeTypeButton;
	
	/** List of file extensions that are not allowed to be selected from the add content dialog */
	private static String[] FILTERED_EXTS = new String[]{AbstractSchemaHandler.DKF_FILE_EXTENSION, AbstractSchemaHandler.COURSE_FILE_EXTENSION, 
            AbstractSchemaHandler.INTEROP_FILE_EXTENSION, AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION, 
            AbstractSchemaHandler.LEARNER_ACTIONS_FILE_EXTENSION, AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION,
            AbstractSchemaHandler.METADATA_FILE_EXTENSION, AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION, 
            AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION};

	/**
	 * Creates a new 'Add Content' dialog
	 */
	public AddRemediationDialog() {
		
		initWidget(binder.createAndBindUi(this));
		
		setText("Add Remediation Content");
		
		referenceEditor.setRemediationEnabled(true);
        
        // initialize 'Concepts:' table
        
        conceptsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        conceptsTable.addColumn(conceptSelectionColumn);
        conceptsTable.addColumn(conceptNameColumn);
        conceptsTable.setColumnWidth(conceptSelectionColumn, "50px");
        
        VerticalPanel emptyConceptsWidget = new VerticalPanel();
        emptyConceptsWidget.setSize("100%", "100%");
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        Label emptyConceptsLabel = new Label("No course concepts were found. Please make sure you have added a list or hierarchy of concepts to the course summary.");
        emptyConceptsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyConceptsWidget.add(emptyConceptsLabel);
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        conceptsTable.setEmptyTableWidget(emptyConceptsWidget);
        
        // initialize 'Attributes:' table
        
        attributesTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        attributesTable.addColumn(attributeSelectionColumn);
        attributesTable.addColumn(attributeNameColumn);
        attributesTable.setColumnWidth(attributeSelectionColumn, "50px");
        
        VerticalPanel emptyAttributesWidget = new VerticalPanel();
        emptyAttributesWidget.setSize("100%", "100%");
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        Label emptyAttributesLabel = new Label("Please select a checked concept to view the attributes assigned to it.");
        emptyAttributesLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyAttributesWidget.add(emptyAttributesLabel);
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        attributesTable.setEmptyTableWidget(emptyAttributesWidget);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addContentModal.hide();
				deleteSlideShowFolder();
			}
		});
		
		// Setup the disallowed file types in the dialog.
		List<String> filteredList = Arrays.asList(FILTERED_EXTS);
		fileSelectionDialog.getFileSelector().filterOutExtensions(filteredList);
		
		StringBuilder reason = new StringBuilder();
		reason.append("<div style='margin-bottom: 5px;'>Please select a content file with one of the following extensions: <br/><b>");
		
		for(String type : Constants.ppt_show_supported_types){
			reason.append(type);
			reason.append(", ");
		}
		
		for(String type : Constants.html_supported_types){
			reason.append(type);
			reason.append(", ");
		}
		
		reason.append(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
		
		reason.append("</b><br/></div>");

		fileSelectionDialog.setIntroMessageHTML(reason.toString());
		
		List<String> allowedFiles = new ArrayList<String>();
		Collections.addAll(allowedFiles, Constants.ppt_show_supported_types);
		Collections.addAll(allowedFiles, Constants.html_supported_types);
		Collections.addAll(allowedFiles, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
		
		fileSelectionDialog.setAllowedFileExtensions(allowedFiles.toArray(new String[]{}));
		
		fileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
		
		changeTypeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				if(referenceEditor.getCourseObject() != null){
					
					Metadata metadata = referenceEditor.getCourseObject();
					metadata.setContent(null);
					
					referenceEditor.edit(metadata);
					deleteSlideShowFolder();
				}
			}
		});
		
		referenceEditor.setTypeChangedCommand(new Command() {
				
			@Override
			public void execute() {
				
                changeTypeButton.setVisible(referenceEditor.hasSelectedType());
                
                if(referenceEditor.getQuestionExport() != null 
                        || referenceEditor.getTrainingApp() != null
                        || referenceEditor.getConversationTree() != null){
                    conceptsDock.remove(attributesPanel);
                    
                } else {
                    conceptsDock.add(attributesPanel, DockPanel.EAST);
                    conceptsDock.setCellWidth(attributesPanel, "50%");
                }
			}
		});
	}
	
	/**
	 * Checks the reference editor for a Slide Show folder created when the user uploads a PowerPoint show. If it exists, the folder is deleted.
	 */
	private void deleteSlideShowFolder() {
		final String slideShowFolder = referenceEditor.getCreatedSlideShowFolder();
		if(slideShowFolder != null) {
            DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(GatClientUtility.getUserName(), GatClientUtility.getBrowserSessionKey(), Arrays.asList(slideShowFolder), true);
            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                @Override
                public void onFailure(Throwable caught) {
                	logger.log(Level.WARNING, "An error occurred while trying to delete the folder: " + slideShowFolder, caught);
                }

                @Override
                public void onSuccess(GatServiceResult result) {
                    if(!result.isSuccess()){
                        logger.warning("An error occurred while trying to delete the folder: " + slideShowFolder + "\nError Message: " + result.getErrorDetails());
                    }
                }
                
            });
		}
	}
	
	public HasData<CandidateConcept> getConceptsTable(){
		return conceptsTable;
	}
	
	public void setConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	conceptSelectionColumn.setFieldUpdater(updater);
    }
	
	public HasData<CandidateMetadataAttribute> getAttributesTable(){
		return attributesTable;
	}
	
	public void setAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater){
    	attributeSelectionColumn.setFieldUpdater(updater);
    }
	
	public void redrawConceptsTable(){
		conceptsTable.redraw();
	}
	
	/**
	 * Shows the dialog
	 */
	public void center(){
		
		addContentModal.show();
		
		conceptsTable.redraw();
		attributesTable.redraw();
	}

	/**
	 * Hides the dialog
	 */
	public void hide(){
		addContentModal.hide();
	}
	
	/**
	 * Sets the dialog title
	 * 
	 * @param text The title of the dialog
	 */
	public void setText(String text){
		title.setText(text);
	}
	
	public HasValue<String> getFileSelectionDialog(){
		return fileSelectionDialog;
	}
	
	public HasClickHandlers getAddButton(){
		return uploadButton;
	}
	
	public HasEnabled getAddButtonEnabled(){
		return uploadButton;
	}
	
	public HasHTML getValidationErrorText(){
		return validationErrorText;
	}
	
	public ContentReferenceEditor getReferenceEditor(){
		return referenceEditor;
	}
	
	/**
     * Update the remediation content being authored based on the given concept's selection state, if necessary
     * 
     * @param concept the concept whose selection state was changed
     * @param selected the concept's selection state
     */
	public void conceptSelected(String concept, boolean selected) {
	    referenceEditor.conceptSelected(concept, selected);
	}
}
