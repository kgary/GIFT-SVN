/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.LtiProvider;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import generated.metadata.Concept;
import generated.metadata.Metadata;
import generated.metadata.Metadata.Concepts;
import generated.metadata.Metadata.Simple;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.GenericDataProvider;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.LearnerConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.ContentReferenceEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.InteropsEditedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateQuestionExportReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.SaveMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.SaveTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

public class MetadataEditor extends Composite {
	
    /** The logger. */
    private static Logger logger = Logger.getLogger(MetadataEditor.class.getName());
    
	/** The ui binder. */
    interface MetadataEditorUiBinder extends UiBinder<Widget, MetadataEditor> {} 
	private static MetadataEditorUiBinder uiBinder = GWT.create(MetadataEditorUiBinder.class);
	
	private static String CONTENT_REF_CHOICE = "Content Resource";
	
	private static String TRAINING_APPLICATION_REF_CHOICE = "Training Application";
	
	private static String POTENTIAL_UNSAVED_CHANGES_MESSAGE = "This tool doesn't keep track of whether or not you've made changes since your last save operation. Leaving this tool, as you've requested, will result in any unsaved changes being discarded. Would you like to proceed with your request to leave this tool?";
	
	/**
	 * Used to jump back to the dashboard at the user's request.
	 */
	@Inject
	private PlaceController placeController;
	
	/**
	 * Used to communicate with the server.
	 */
	@Inject
	protected DispatchAsync dispatchAsync;
	
	@UiField
	protected DeckPanel dataTypeEditorDeckPanel;
	
	@UiField(provided=true)
	protected ContentReferenceEditor contentRefEditor = new ContentReferenceEditor(true);
	
	@UiField
	protected TrainingAppRefEditor trainingAppRefEditor;
	
	@UiField
	protected ConceptsEditor conceptsEditor;
	
	@UiField
	protected HTML validationErrorText;
	
	@UiField
	protected Widget merrillQuadrantPanel;
	
	@UiField
	protected RadioButton rulePhaseRadio;
	
	@UiField
	protected RadioButton examplePhaseRadio;
	
	/** Bug text box. */
    @UiField
    protected TextBox bugTextBox;
	
	/**
	 * File New dialog.
	 */
	private DefaultGatFileSaveAsDialog fileNewDialog = null;
	
	/**
	 * File Save As dialog.
	 */
	private DefaultGatFileSaveAsDialog fileSaveAsDialog = null;
	
	/**
	 * The metadata path.
	 */
	private String filePath;
	
	/**
	 * Whether or not this editor is Read-Only
	 */
	boolean readOnly;
	
	/** 
     * special condition if a file is locked and another user tries to open the file
     * should currently only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
	
	/**
	 * The Metadata object we're editing
	 */
	private Metadata metadata;
	
	/**
	 * The Interface MyEventBinder.
	 */
	interface MyEventBinder extends EventBinder<MetadataEditor> {
	}
	
	/** The event registration. */
	protected HandlerRegistration eventRegistration;
	
	/** The Constant eventBinder. */
	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	/**
	 * Heartbeat timer so the server doesn't unlock the file we're editing.
	 */
	private Timer lockTimer = new Timer() {
		@Override
		public void run() {
			if(filePath != null) {
				lockMetadata();
			}
		}
	};
	
	boolean hasTrainingAppRefFileToLock = false;
    
    public MetadataEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        eventRegistration = eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        
        // populate the concepts
        List<String> conceptNames = GatClientUtility.getBaseCourseConcepts();
        ListDataProvider<CandidateConcept> conceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
        conceptsTableDataProvider.addDataDisplay(contentRefEditor.getMediaPanel().getConceptsTable());
        if (conceptNames != null) {
            for (String cName : conceptNames) {
                conceptsTableDataProvider.getList().add(new CandidateConcept(cName, false));
            }
        }
        conceptsTableDataProvider.refresh();

        contentRefEditor.setOnChangeCommand(new Command() {

            @Override
            public void execute() {
                showValidationState();
            }
        });
        
        //Data type deck panel
        dataTypeEditorDeckPanel.showWidget(dataTypeEditorDeckPanel.getWidgetIndex(contentRefEditor));
        
        ArrayList<String> values = new ArrayList<String>();
        
        values.add(CONTENT_REF_CHOICE);
        values.add(TRAINING_APPLICATION_REF_CHOICE); 
        
        //Merrill quadrant list box      
        
        rulePhaseRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(metadata != null && event.getValue()){
					
                    generated.metadata.PresentAt presentAt = metadata.getPresentAt();
                    if(presentAt == null){
                        presentAt = new generated.metadata.PresentAt();
                        metadata.setPresentAt(presentAt);
                    }
					
                    presentAt.setMerrillQuadrant(MerrillQuadrantEnum.RULE.getName());
					
				} else {
					WarningDialog.error("Selection failed", "An error occurred while selecting the Merrill quadrant.");
				}
				
				showValidationState();
			}
		});
        
        examplePhaseRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(metadata != null && event.getValue()){
					
                    generated.metadata.PresentAt presentAt = metadata.getPresentAt();
                    if(presentAt == null){
                        presentAt = new generated.metadata.PresentAt();
                        metadata.setPresentAt(presentAt);
                    }
					
                    presentAt.setMerrillQuadrant(MerrillQuadrantEnum.EXAMPLE.getName());
					
				} else {
					WarningDialog.error("Selection failed", "An error occurred while selecting the Merrill quadrant.");
				}
				
				showValidationState();
			}
		});
        
		//Every minute remind the server that the Metadata needs to 
		//be locked because we're still working on it.
		lockTimer.scheduleRepeating(60000);
		
        /*
         * There is an odd bug that is a result of some combination of text 
         * boxes, iFrames, and Internet Explorer. The text boxes would somehow
         * get into a state where clicking on it would be ignored thus
         * preventing the user from typing in it. I've found that putting a
         * text box in the highest GUI element, scheduling this delayed
         * command, and then making the text box invisible prevents the bug
         * from happening. I don't know why this works.
         */
        ScheduledCommand command = new ScheduledCommand() {
			@Override
			public void execute() {
				bugTextBox.setFocus(true);
				bugTextBox.setVisible(false);
			}
		};
        Scheduler.get().scheduleDeferred(command);
        
        trainingAppRefEditor.getInteropEditor().setInteropsEditedCallback(new InteropsEditedCallback() {
			
			@Override
			public void onEdit() {
				showValidationState();
			}
		});
        
        contentRefEditor.setTypeChangedCommand(new Command() {
			
			@Override
			public void execute() {
			    logger.info("contentrefeditor type changed");
				conceptsEditor.updateConceptsForType(metadata, contentRefEditor);
    }
		});
    }
    
    /**
     * Selects a data type corresponding to the value given
     * 
	 * @param value a value representing the choice of data type
	 */
	private void selectDataType(String value) {
		
		if(metadata != null){
			
			if(value.equals(TRAINING_APPLICATION_REF_CHOICE)){
				
				dataTypeEditorDeckPanel.showWidget(dataTypeEditorDeckPanel.getWidgetIndex(trainingAppRefEditor));
				
			} else {
				
				dataTypeEditorDeckPanel.showWidget(dataTypeEditorDeckPanel.getWidgetIndex(contentRefEditor));
			}
			
		} else {
			WarningDialog.error("Selection failed", "An error occurred while setting the data type.");
		}
	}

	/**
     * Stops the lock renewing timer and unlocks the file.
     */
    public void stop() {
    	lockTimer.cancel();
		unlockMetadata();
    }
    
    private void createNewFile(String path) {
    	metadata = createDefaultMetadata();
		saveMetadata(path, false);
		displayMetadata();
    }
    
    private String getFileNewPath() {
    	String path = fileNewDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.METADATA_FILE_EXTENSION);
		}
		return path;
    }
    
    private String getFileSaveAsPath() {
    	String path = fileSaveAsDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.METADATA_FILE_EXTENSION);
		}
		return path;
    }
    
    /**
     * Updates this editor's UI to display data from the currently loaded metadata object
     */
    private void displayMetadata(){
    	displayMetadata(null);
    }
    
    /**
     * Updates this editor's UI to display data from the currently loaded metadata object and executes the given command
     * once the UI has finished updating. 
     * 
     * @param finishedLoadingCommand the command to execute once the UI has been updated. Can be null.
     */
    private void displayMetadata(final Command finishedLoadingCommand) {
    	    	
    	MerrillQuadrantEnum initQuadrant = null;
    	
    	try{
            if(metadata.getPresentAt() != null){
                initQuadrant = MerrillQuadrantEnum.valueOf(metadata.getPresentAt().getMerrillQuadrant());
            }    		
    	} catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
    		//Nothing to do
    	}
    	
    	final MerrillQuadrantEnum quadrant = initQuadrant;
    	    	
    	if(MerrillQuadrantEnum.RULE.equals(quadrant)){
    		
    		merrillQuadrantPanel.setVisible(true);
    		rulePhaseRadio.setValue(true);
    		
    	} else if(MerrillQuadrantEnum.EXAMPLE.equals(quadrant)){
    		
    		merrillQuadrantPanel.setVisible(true);
    		examplePhaseRadio.setValue(true);
    		
    	} else {
    		merrillQuadrantPanel.setVisible(false);
    	}
    	
        Serializable reference = metadata.getContent();
        if(reference instanceof generated.metadata.Metadata.Simple 
                || reference instanceof generated.metadata.Metadata.URL 
                || reference instanceof generated.metadata.Metadata.LessonMaterial ){
			            
			selectDataType(CONTENT_REF_CHOICE);
			
			hasTrainingAppRefFileToLock = false;
			
            // populate the LTI providers before we launch the metadata editor
            GenericDataProvider<LtiProvider> contentLtiProvidersDataProvider = new GenericDataProvider<LtiProvider>();
            contentLtiProvidersDataProvider.createChild(contentRefEditor.getMediaPanel().getCourseLtiProviderList());
            contentLtiProvidersDataProvider.getList().clear();

            List<LtiProvider> providers = GatClientUtility.getCourseLtiProviders();
            if (providers != null) {
                contentLtiProvidersDataProvider.getList().addAll(providers);
            }
            contentLtiProvidersDataProvider.refresh();
            
            contentRefEditor.setFinishedLoadingCommand(finishedLoadingCommand);
	        
			contentRefEditor.edit(metadata);
    	
        } else if(reference instanceof generated.metadata.Metadata.TrainingApp){
    		
    		selectDataType(TRAINING_APPLICATION_REF_CHOICE);
            
            //if a training app reference file exists, it should be loaded and locked.
            hasTrainingAppRefFileToLock = true;   
    	
            trainingAppRefEditor.setFinishedLoadingCommand(finishedLoadingCommand);
            
            boolean isRemediationOnly = metadata.getPresentAt() != null &&
                    generated.metadata.BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly());
            logger.info("Setting remediation enabled to "+isRemediationOnly);
            trainingAppRefEditor.getInteropEditor().setRemediationEnabled(isRemediationOnly);
            
            trainingAppRefEditor.loadTrainingApplicationReference(getMetadataFolderPath(), ((generated.metadata.Metadata.TrainingApp)reference).getValue());
            
            conceptsEditor.handleConcepts(metadata);
            
    	} else {
        	
            contentRefEditor.setFinishedLoadingCommand(finishedLoadingCommand);
        	
            hasTrainingAppRefFileToLock = false;
    	
    	}
    	
    	showValidationState();
    }

	/**
	 * Returns the workspace relative path to the metadata folder.
	 * 
	 * @return String - workspace relative path to the metadata folder.  An empty string is returned if the folder cannot be found.
	 */
	private String getMetadataFolderPath() {
	    String metadataFolderPath = "";
	    if (filePath != null && !filePath.isEmpty()) {
	    	metadataFolderPath = filePath.substring(0,  filePath.lastIndexOf("/"));
	    }
	    
	    return metadataFolderPath;
	}
    
	static private Metadata createDefaultMetadata() {
		Concept concept = new Concept();
		concept.setName("Default Concept");
		
		Concepts concepts = new Concepts();
		concepts.getConcept().add(concept);
		
    	Metadata metadata = new Metadata();
    	metadata.setConcepts(concepts);
        
        //MH: not sure why we are setting URL value here, I would prefer null
        generated.metadata.Metadata.URL url = new generated.metadata.Metadata.URL();
        url.setValue("");
        metadata.setContent(url);
        
        //MH: not sure why we are setting quadrant value here, I would prefer null
        generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
        presentAt.setMerrillQuadrant(MerrillQuadrantEnum.RULE.getName());
        metadata.setPresentAt(presentAt);
        
    	return metadata;
    }
	
	/**
	 * Used by MetadataActivity to set the file locked value
	 * @param locked the value to set the fileAlreadyLocked as
	 */
	public void setFileAlreadyLocked(boolean locked){
		fileAlreadyLocked = locked;
	}
    
    /**
     * Loads a metadata if a valid path is supplied, if null is
     * supplied then it creates a new metadata that only exists on
     * the client side (if the user saves it then it'll be stored on the
     * server)
     * @param path Path to the metadata you want to
     * load, NULL if you want to create a new metadata.
     */
    public void loadMetadata(final String path) {
    	if(path == null) {
    		//Hide the file loading dialog 
    		BsLoadingDialogBox.remove();
    		
    		//I don't know why showFileNewDialog can't be called directly but
    		//I think there is an issue with the file browser classes.
    		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					showFileNewDialog();
				}
			});
    		return;
    	}
		
    	if(logger.isLoggable(Level.INFO)){
    	    logger.info("Loading metadata from "+path);
    	}
		
    	AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){
			@Override
			public void onFailure(Throwable t) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();
				
				final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"An error occurred while trying to load the metadata file", t.getMessage(), DetailedException.getFullStackTrace(t));				
				dialog.setText("Failed to Load File");
				
				dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
					
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						GatClientUtility.cancelModal();
					}
				});
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						dialog.center();
					}
				});
			}

			@Override
			public void onSuccess(final FetchJAXBObjectResult result) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();				

				if(logger.isLoggable(Level.INFO)){
				    logger.info("Result of retrieving metadata from '"+path+"' on server: "+result);
				}
				
				if(result.isSuccess()) {

					metadata = (Metadata)result.getJAXBObject();
					if(!fileAlreadyLocked){
						setReadOnly(!result.getModifiable());
					}
					setFilePath(path);
    	    		
    	    		if(result.wasConversionAttempted() && result.getModifiable()) {

    	    			//need to actually load data into the sub-editors before saving the converted file, otherwise we 
    	    			//may incorrectly overwrite training app references and lesson material references
    	    			displayMetadata(new Command() {
							
							@Override
							public void execute() {
								
								WarningDialog.warning("File updated", "The file you've loaded was created with an old version of GIFT, "
		    	    					+ "but we were able to update it for you. This file has already been saved and "
		    	    					+ "is ready for modification. A backup of the original file was created: " 
		    	    					+ result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
		    	    			
		    	    			//if the file was converted, we need to save it after the conversion
		    	    			saveMetadata(path, false);
							}
						});  
    	    			
					} else {
						displayMetadata();   
					}
    	    		
    			} else {
    				
    				final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
    						result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());				
					dialog.setText("Failed to Load File");
					
					dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							GatClientUtility.cancelModal();
						}
					});
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							dialog.center();
						}
					});
				}
			}		
		};
		
		String userName = GatClientUtility.getUserName();
		
		FetchJAXBObject action = new FetchJAXBObject();
		action.setRelativePath(path);
		action.setUserName(userName);
		
		dispatchAsync.execute(action, callback);
		
		GatClientUtility.defineSaveEmbeddedCourseObject(createSaveObject(path));
		exposeNativeFunctions();
		
		logger.info("finished");
    }
    
    /**
     * Saves the Metadata to the server. 
	 *
	 * @param path the path where the metadata should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
	 */
	private void saveMetadata(final String path, final boolean showConfirmationDialog) {
		saveMetadata(path, showConfirmationDialog, false);
	}
    
	private native JavaScriptObject createSaveObject(String path)/*-{
	
		var that = this;
	   	var saveObj = function() {
	        that.@mil.arl.gift.tools.authoring.server.gat.client.view.metadata.MetadataEditor::saveMetadata(Ljava/lang/String;Z)(path, false);
	    };
		
		return saveObj;
	}-*/;	
    
    /**
     * Saves the Metadata to the server. It turns out there is
     * actually a bit of complexity/bookkeeping associated with this:
     * 
     * 1.) If the target path is different from this file's previous path (i.e.
     * Save-As is being performed with a file that already exists on the
     * server) then this method takes care of the bookkeeping to release the
     * lock on the original location and apply it to the new location.
     * 2.) The server supplies an incremented version number every time a file
     * is saved as this method takes care of saving that attribute to the
     * Metadata.
     * 3.) Whenever you save a file you have to tell the server to either
     * acquire (save new file or overwrite a different file) or renew (re-save
     * the file) the lock, this method figures out which case is appropriate.
	 *
	 * @param path the path where the metadata should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
	 * @param validateFile whether or not to validate the file after saving
	 */
	private void saveMetadata(final String path, final boolean showConfirmationDialog, final boolean validateFile) {
		
		if(readOnly && path != null && filePath != null && path.equals(filePath)){
			WarningDialog.warning("Read only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
					+ "<br/>"
					+ "If you wish to save this metadata to a different file, please specify another file name.");
			return;
		}
		
        if(logger.isLoggable(Level.INFO)){
            logger.info("Saving metadata to "+path);
        }
        
		String trainingAppPath = null;
		
		//attach changes made in subeditors to the current metadata		
		Widget visibleEditor = dataTypeEditorDeckPanel.getWidget(dataTypeEditorDeckPanel.getVisibleWidget());
		
		if(visibleEditor!= null){
			
			if(visibleEditor.equals(trainingAppRefEditor)){
				
				trainingAppPath = path.replace(AbstractSchemaHandler.METADATA_FILE_EXTENSION, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
				
            } else if (metadata.getContent() != null && metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial){
                
                generated.metadata.Metadata.LessonMaterial lessonMaterial = (generated.metadata.Metadata.LessonMaterial)metadata.getContent();
                if(lessonMaterial.getValue() != null && !lessonMaterial.getValue().isEmpty()) {
				// Save the lesson material file 

				GenerateLessonMaterialReferenceFile action = new GenerateLessonMaterialReferenceFile();
				action.setLessonMaterialList(contentRefEditor.getLessonMaterial());
                    action.setTargetFilename(GatClientUtility.getBaseCourseFolderPath() + "/" + lessonMaterial.getValue());
				action.setUserName(GatClientUtility.getUserName());
				
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("saving lesson material file : "+action);
                    }
                    
				dispatchAsync.execute(action, new AsyncCallback<GenerateLessonMaterialReferenceFileResult>() {
					
					@Override
					public void onFailure(Throwable e) {
						WarningDialog.error("Failed to save metadata", "A server error occured. Changes to the content could not be saved.");
					}
		
					@Override
					public void onSuccess(GenerateLessonMaterialReferenceFileResult result) {
						if(!result.isSuccess()){
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to save the metadata content because "+result.getErrorMsg()+".", 
                                        result.getErrorDetails(), result.getErrorStackTrace());
                                dialog.setText("Failed to save metadata");
							dialog.center();
						}
					}
				});
                }
				
            } else if (metadata.getContent() != null && metadata.getContent() instanceof Simple){
                
            	Simple lessonMaterial = (Simple)metadata.getContent();
			    if(lessonMaterial.getValue() != null && lessonMaterial.getValue().endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)) {
    				// Save the lesson material file 
    
    				GenerateQuestionExportReferenceFile action = new GenerateQuestionExportReferenceFile();
    				action.setQuestion(contentRefEditor.getQuestionExport());
    				action.setTargetFilename(GatClientUtility.getBaseCourseFolderPath() + "/" + lessonMaterial.getValue());
    				action.setUserName(GatClientUtility.getUserName());
    				
    				dispatchAsync.execute(action, new AsyncCallback<GenericGatServiceResult<Void>>() {
    					
    					@Override
    					public void onFailure(Throwable e) {
    						WarningDialog.error("Failed to save", "A server error occured. Changes to the content could not be saved.");
			}
    		
    					@Override
    					public void onSuccess(GenericGatServiceResult<Void> result) {
    						
    						GenericRpcResponse<Void> response = result.getResponse();
    						
    						if(!response.getWasSuccessful()){
    							ErrorDetailsDialog dialog = new ErrorDetailsDialog(
    									"An error occured while saving the metadata content. " + response.getException().getReason(), 
    									response.getException().getDetails(), 
    									response.getException().getErrorStackTrace()
    							);
    							dialog.setText("Error");
    							dialog.center();
		}
    					}
    				});
			    }       
            }
        }
		
		//This flag will be false if we're saving to the same file we just
		//opened. It'll be true if we're saving to a brand new file or if we're
		//overwriting an existing file on disk (Save-As).
		boolean acquireInsteadOfRenew = false;
		if(filePath == null ||
				!filePath.equals(path)) {
			acquireInsteadOfRenew = true;
		}
		
		String userName = GatClientUtility.getUserName();
		
		//if the metadata uses a training application reference, that needs to be saved too
        if(metadata.getContent() instanceof Metadata.TrainingApp &&
                ((Metadata.TrainingApp)metadata.getContent()).getValue() != null && trainingAppRefEditor.getTrainingApplicationWrapper() != null){
			
			TrainingApplicationWrapper taWrapper = trainingAppRefEditor.getTrainingApplicationWrapper();
			
			//ensure the metadata display name and training application name match
			if(taWrapper.getTrainingApplication() != null 
					&& taWrapper.getTrainingApplication().getTransitionName() != null){
				
				metadata.setDisplayName(taWrapper.getTrainingApplication().getTransitionName());
				
			} else if(metadata.getDisplayName() != null){
				
				if(taWrapper.getTrainingApplication() == null){
					taWrapper.setTrainingApplication(new TrainingApplication());
				}
				
				taWrapper.getTrainingApplication().setTransitionName(metadata.getDisplayName());
			}
			
			AsyncCallback<SaveJaxbObjectResult> saveTrainingApplicationReferenceCallback = new AsyncCallback<SaveJaxbObjectResult>() {

				@Override
				public void onFailure(Throwable t) {
					
					ErrorDetailsDialog error = new ErrorDetailsDialog(
							"An error ocurred while saving a training application reference for the current metadata : " + t.getMessage(),
							t.toString(),
							DetailedException.getFullStackTrace(t));
					
					error.center();
				}

				@Override
				public void onSuccess(SaveJaxbObjectResult result) {                     
					if(result.isSuccess()) {                                    
						
						//When we ask the server to save a file it generates
						//a new version for that file before saving. The newly
						//generated version is based on the previous version.
						//So if we don't save the newly generated version in the
						//TrainingApplicationReference object then the server will generate 
						//the same version every time we save.
						String newVersion = result.getNewVersion();
						trainingAppRefEditor.getTrainingApplicationWrapper().setVersion(newVersion);
					
					} else {
						ErrorDetailsDialog error = new ErrorDetailsDialog(
								"An error ocurred while saving a training application reference for the current metadata : " + result.getErrorMsg(),
								result.getErrorDetails(),
								result.getErrorStackTrace());
						
						error.center();
					}
				}
			};

			SaveTrainingApplicationReference action2 = new SaveTrainingApplicationReference();
			action2.setTrainingApplicationReference(taWrapper);
			action2.setRelativePath(trainingAppPath);
			action2.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);
			action2.setUserName(userName);
			dispatchAsync.execute(action2, saveTrainingApplicationReferenceCallback);
		}
		
		AsyncCallback<SaveJaxbObjectResult> saveMetadataCallback = new AsyncCallback<SaveJaxbObjectResult>() {

			@Override
			public void onFailure(Throwable t) {
				WarningDialog.error("Failed to save metadata", "An error ocurred while saving the current metadata : " + t.toString());
			}

			@Override
			public void onSuccess(SaveJaxbObjectResult result) {                     
				if(result.isSuccess()) {
					//Let the user know the save result.
					if(showConfirmationDialog) {
						String message = "File saved successfully!";
						if(!result.isSchemaValid()) {
							message += "\n\nThe authored content is not valid against the schema. Therefore, this file should not be used with GIFT until the contents are correct. Using this incomplete file can result in GIFT not executing correctly.";
						}
						WarningDialog.info("Save successful", message);
					}
					
					//This condition is true when we load a file from the
					//server and then use the Save-As functionality to save as
					//a different file. In this case it is our responsibility
					//to release the lock on the original file since we're no
					//longer using it.
					if(filePath != null &&
							!filePath.equals(path)) {
						
						if(readOnly){
							setReadOnly(false);
						}
						
						unlockMetadata();
					}
					
					//When we ask the server to save a file it generates
					//a new version for that file before saving. The newly
					//generated version is based on the previous version.
					//So if we don't save the newly generated version in the
					//Metadata object then the server will generate 
					//the same version every time we save.
					String newVersion = result.getNewVersion();
					metadata.setVersion(newVersion);
					
					setFilePath(path);
				
					if(validateFile) {
						validateFile(filePath, GatClientUtility.getUserName());
					}
					
				} else {
					ErrorDetailsDialog dialog = new ErrorDetailsDialog("An error ocurred while saving the current metadata : " + result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
					dialog.setText("Error");
					dialog.center();
				}
			}
		};

		SaveMetadata action = new SaveMetadata();
		action.setUserName(userName);
		action.setRelativePath(path);
		action.setMetadata(metadata);
		action.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);  
		dispatchAsync.execute(action, saveMetadataCallback);
	}
	
	private void showFileNewDialog() {
		fileNewDialog.clearFileName();
		fileNewDialog.center();
	}
	
	private void showSaveAsDialog() {
		fileSaveAsDialog.clearFileName();
		fileSaveAsDialog.center();
        
        WarningDialog.warning("Save warning", "GIFT files contain relative paths that reference other GIFT files as well as associated content. Saving a file that already exists to a new location has the potential to invalidate these paths. In the future the GIFT team will develop a feature to automatically handle these issues for you but until then use the Save-As feature at your own risk.");
	}
	
	/**
	 * Unlocks the metadata file and sets 
	 */
	private void unlockMetadata() {
		
		if(!readOnly){
			
			//This callback does absolutely nothing. In the case of releasing the
			//lock that is the appropriate course of action. It seems the only way
			//the lock wouldn't be released is if communication to the server
			//fails. The probability of that is very low but if we don't handle
			//it then that file will be locked forever.
			//
			//TODO Handle the failure case.
			AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
		        @Override
				public void onFailure(Throwable t) {
		        }
		        @Override
				public void onSuccess(GatServiceResult result) {
		        }
		    };
		    
		    String userName = GatClientUtility.getUserName();
		    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
		    
		    //Try to lock the metadata before we open it.
		    UnlockMetadata action = new UnlockMetadata();
		    action.setRelativePath(filePath);
		    action.setUserName(userName);
		    action.setBrowserSessionKey(browserSessionKey);
		    
		    dispatchAsync.execute(action, callback);
		    
		    //if the metadata file has an associated training application reference file, we need to unlock that too
            if(metadata != null && metadata.getContent() != null &&
                    metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){
                unlockTrainingApplicationReference(((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue());        
		    }
		    
		    contentRefEditor.unlockLessonMaterialReference();
		}
	}
	

	/**
	 * Tells the server to unlock the training application reference file at the given path.
	 * @param path Path of the training application reference to unlock.
	 */
	private void unlockTrainingApplicationReference(String path) {
		
		AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
	        @Override
			public void onFailure(Throwable t) {
	        }
	        @Override
			public void onSuccess(GatServiceResult result) {
	        }
	    };
	    
	    String userName = GatClientUtility.getUserName();
	    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
	    
	    //Try to lock the training application reference before we open it.
	    UnlockTrainingApplicationReference action = new UnlockTrainingApplicationReference();
	    action.setRelativePath(path);
	    action.setUserName(userName);
	    action.setBrowserSessionKey(browserSessionKey);
	    dispatchAsync.execute(action, callback);
	}
	
	/**
	 * Tells the server to lock the Metdata.
	 */
	private void lockMetadata() {
		
        //This callback does absolutely nothing. In the case of acquiring the
        //lock that is the appropriate course of action. However, there seem to
        //be two ways to fail to acquire the lock:
        //
        //  1.) After saving a brand new file OR executing a Save-As, another
        //  user sees that Metadata in the dashboard and acquires 
        //  the lock before you.
        //  2.) The communication to the server simply failed.
        //
        //For the first case we'd probably want to tell the user what happened
        //and jump back to the dashboard. The second case is less clear,
        //perhaps we'd want to do the same.
        //
        //TODO Handle the failure case.
        AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {
                // nothing to do
            }
            @Override
            public void onSuccess(LockFileResult result) {
                // nothing to do
            }
        };
        
        String userName = GatClientUtility.getUserName();
        
        //Try to lock the file.
        LockMetadata action = new LockMetadata();
        action.setRelativePath(filePath);
        action.setUserName(userName);
        action.setBrowserSessionKey(GatClientUtility.getUserName());
        dispatchAsync.execute(action, callback);
        
        //if the training application reference file has an associated training application reference file, we need to lock that too
        if(metadata != null && metadata.getContent() != null &&
                metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){
            lockTrainingApplicationReference(((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue());        
        }
        
        contentRefEditor.lockLessonMaterialReference();
	}
	
	/**
	 * Tells the server to lock the Training Application Reference at the given path.
	 * @param path Path of the TrainingApplicationReference file to lock.
	 */
	private void lockTrainingApplicationReference(String path) {
		
		if(!readOnly){
		
			AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
		        @Override
				public void onFailure(Throwable t) {
		        }
		        @Override
				public void onSuccess(LockFileResult result) {
		        }
		    };
		    
		    String userName = GatClientUtility.getUserName();
		    
		    //Try to lock the file.
            if(metadata.getContent() != null && metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){
		    LockTrainingApplicationReference action = new LockTrainingApplicationReference();
            action.setRelativePath(((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue());
		    action.setUserName(userName);
		    action.setBrowserSessionKey(GatClientUtility.getUserName());
		    dispatchAsync.execute(action, callback);	    
		}
	}
    }
		
	/**
	 * Sets the file path and updates the GUI to reflect the
	 * new path.
	 * @param path
	 */
	private void setFilePath(String path) {
		filePath = path;
		
		//Let the file selection dialog know which folder files should be
		//uploaded/copied to.
		String courseFolderPath = filePath.substring(0,  filePath.lastIndexOf("/"));
		DefaultGatFileSelectionDialog.courseFolderPath = courseFolderPath;
	}
	
	public String mayStop() {
		//If the filePath is null it means nothing was ever loaded and
		//therefore there is certainly no reason to prompt the user about
		//losing unsaved changes.
//		if(filePath == null) {
//			return null;
//		} else {
//			return POTENTIAL_UNSAVED_CHANGES_MESSAGE;
//		}
		//Nick: Set to null for now to prevent the "Are you sure" dialog from showing
		return null;
	}
	
	/** 
	 * Sets whether or not this editor should be Read-Only
	 * 
	 * @param readOnly whether or not this editor should be Read-Only
	 */
	public void setReadOnly(boolean readOnly){
		this.readOnly = readOnly;
		
		DefaultGatFileSelectionDialog.setReadOnly(readOnly);
		
		contentRefEditor.setReadOnly(readOnly);
	}
	
	/**
     * Initializes the file new dialog based on the deployment mode. 
     * In this case, if the dkf editor is launched in server mode, then
     * the dialog should not allow the user to save outside of the username folder.
     * 
     * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
     */
	private void initFileNewDialog(DeploymentModeEnum mode) {
        
        if (mode == DeploymentModeEnum.SERVER) {
            fileNewDialog = new DefaultGatFileSaveAsDialog(true);
        } else {
            fileNewDialog = new DefaultGatFileSaveAsDialog(false);
        }
        
        fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.METADATA_FILE_EXTENSION});
        
        fileNewDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> arg0) {
                final String path = getFileNewPath();
                if(path == null) {
                    //If the user hit cancel on a File->New that originated in
                    //the dashboard then jump back to the dashboard.
                    if(filePath == null) {
                    	GatClientUtility.cancelModal();
                    }
                    
                    //If the user hit cancel on a File->New that originated in
                    //the tool then we don't have to do anything.
                    return;
                }
                
                //If the path is within the course folder then the first
                //element will be the user name, the second will be the course
                //folder, and the last element will be the file name.
                int length = path.split("/").length;
                if(length < 3) {
                    WarningDialog.warning("Invalid path", "All new files must be created inside a course folder.");
                    if(filePath == null) {
                        GatClientUtility.cancelModal();
                    }
                    return;
                }
                
                int index = path.lastIndexOf("/") + 1;
                String fileName = path.substring(index);
                boolean exists = fileNewDialog.isFileOrFolderInCurrentDirectory(fileName);
                if(!exists || path.equals(filePath)) {
                    createNewFile(path);
                } else {
                    OkayCancelDialog.show("Confirm Save As", "Would you like to overwrite the existing file?", "Save Changes", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            createNewFile(path);
                        }
                        
                        @Override
                        public void cancel() {
                            if(filePath == null) {
                            	GatClientUtility.cancelModal();
                            }
                        }
                    });
                }
            }
        });
        
    }
	
	/**
     * Initializes the file save as dialog based on the deployment mode. 
     * In this case, if the dkf editor is launched in server mode, then
     * the dialog should not allow the user to save outside of the username folder.
     * 
     * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
     */
    private void initFileSaveAsDialog(DeploymentModeEnum mode) {
        
        if (mode == DeploymentModeEnum.SERVER) {
            fileSaveAsDialog = new DefaultGatFileSaveAsDialog(true);
        } else {
            fileSaveAsDialog = new DefaultGatFileSaveAsDialog(false);
        }
        
        fileSaveAsDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.METADATA_FILE_EXTENSION});
        
        fileSaveAsDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> arg0) {
                final String path = getFileSaveAsPath();
                if(path == null) {
                    return;
                }
                
                //If the path is within the course folder then the first
                //element will be the user name, the second will be the course
                //folder, and the last element will be the file name.
                int length = path.split("/").length;
                if(length < 3) {
                    WarningDialog.warning("Invalid path", "All new files must be created inside a course folder.");
                    return;
                }
                
                int index = path.lastIndexOf("/") + 1;
                String fileName = path.substring(index);
                boolean exists = fileSaveAsDialog.isFileOrFolderInCurrentDirectory(fileName);
                if(!exists || path.equals(filePath)) {
                    saveMetadata(path, true);
                } else {
                    OkayCancelDialog.show("Confirm Save As", "Would you like to overwrite the existing file?", "Save Changes", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            saveMetadata(path, true);
                        }
                        
                        @Override
                        public void cancel() {
                        }
                    });
                }
            }
        });
    }
	
	/**
	 * Initialized the metadata edtior prior to the widget being started.
	 * 
	 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
	public void initMetaDataEditor(HashMap<String, String> startParams) {
	    logger.info("initMetaDataEditor() called with params: " + startParams);
        
        // Setup any start parameters that have been passed in via the url.
        DeploymentModeEnum mode = DeploymentModeEnum.DESKTOP;
        String modeParam = startParams.get(LearnerConfigurationPlace.PARAM_DEPLOYMODE);

        if (modeParam != null) {
            mode = DeploymentModeEnum.valueOf(modeParam);
        } else {
            logger.severe("Start mode could not be determined.  Defaulting to DESKTOP.");
        }
        
        // Initialize the dialogs based on what mode the presenter is deployed in.
        initFileNewDialog(mode);
        initFileSaveAsDialog(mode);
	}
		
	/**
	 * Validates the specified file and displays a dialog containing validation 
	 * errors to the client.
	 * 
	 * @param filePath The path of the file to validate.
	 * @param userName The user validating the file.
	 */
	private void validateFile(String filePath, final String userName) {
		
		ValidateFile action = new ValidateFile();
		
		action.setRelativePath(filePath);
		action.setUserName(GatClientUtility.getUserName());
		dispatchAsync.execute(action, new AsyncCallback<ValidateFileResult>() {

			@Override
			public void onFailure(Throwable cause) {

				ArrayList<String> stackTrace = new ArrayList<String>();
				
				if(cause.getStackTrace() != null) {
					stackTrace.add(cause.toString());
					
					for(StackTraceElement e : cause.getStackTrace()) {
						stackTrace.add("at " + e.toString());
					}
				}
				
				ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"Failed to validate the file.", 
						cause.toString(), stackTrace);
				dialog.setDialogTitle("Validation Failed");
				dialog.center();
			}

			@Override
			public void onSuccess(ValidateFileResult result) {
				if(result.isSuccess()) {
					ModalDialogBox dialog = new ModalDialogBox();
					dialog.setText("Validation Complete");
					dialog.setWidget(new Label("No errors found."));
					dialog.setCloseable(true);
					dialog.setGlassEnabled(true);
					dialog.center();
				} else {
					FileValidationDialog dialog = new FileValidationDialog(result.getFileName(),
							result.getReason(), result.getDetails(), result.getStackTrace());
					dialog.setGlassEnabled(true);
					dialog.setText("Validation Errors");
					dialog.center();
				}				
			}			
		});
	}
	
	/**
	 * Validates the current training application reference and indicates to the user what fields still need to be filled
	 */
	private void showValidationState(){
		
		StringBuilder errorMsg = new StringBuilder();
		
		if(dataTypeEditorDeckPanel.getVisibleWidget() == dataTypeEditorDeckPanel.getWidgetIndex(contentRefEditor)){

			String contentValidationErrors = contentRefEditor.getValidationErrors();
			
			if(contentValidationErrors != null){
				errorMsg.append(contentValidationErrors);
			}		
		}
		
		if(dataTypeEditorDeckPanel.getVisibleWidget() == dataTypeEditorDeckPanel.getWidgetIndex(trainingAppRefEditor)){
			
			String taValidationErrors = trainingAppRefEditor.getInteropEditor().getValidationErrors();
			
			if(taValidationErrors != null){
				errorMsg.append(taValidationErrors);
			}
		}
			
		if(metadata.getConcepts() == null
				|| metadata.getConcepts().getConcept() == null
				|| metadata.getConcepts().getConcept().isEmpty()){
			
			errorMsg.append("")
					.append("<li>")
					.append("At least one metadata concept must be chosen.")
					.append("</li>");
			
		} else {
			
			for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){
				
                if(concept.getActivityType() != null && concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
					
                    generated.metadata.ActivityType.Passive passive = (generated.metadata.ActivityType.Passive)concept.getActivityType().getType();
                    generated.metadata.Attributes attributes = passive.getAttributes();
                    if(attributes == null 
                            || attributes.getAttribute() == null
                            || attributes.getAttribute().isEmpty()){
                        
                        errorMsg.append("")
                        .append("<li>")
                        .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                        .append("</li>");
                    }
                }else if(metadata.getPresentAt() != null && metadata.getPresentAt().getMerrillQuadrant() != null){
                    //which phase to present this content has been set but the specific concept activity has not.
                    //currently the rule/example/practice phases are passive only and passive requires metadata attributes.  The remediation
                    //phase doesn't use the presentAt value but remediation only attribute instead.

                    errorMsg.append("")
                        .append("<li>")
                        .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                        .append("</li>");
                }
            }
			
		}
			
		if(!errorMsg.toString().isEmpty()){
			
			validationErrorText.setHTML(""
					+ "<div style='width: 100%; color: red; font-weight: bold;'> "
					+ 		"The following problem(s) have been detected in this practice application:"
					+ 		"<ul>"
					+ 			errorMsg.toString() 
					+		"</ul>"
					+ 		"You must correct these problems before you can add your practice application."
					+ "</div>"
					+ "<hr style='border-top-style: solid; border-top-width: 1px; border-top-color: #AAAAAA;'/>"
			);
			
			//disable saving, since the metadata is invalid
			GatClientUtility.setModalSaveEnabled(false);
		
		} else {
			
			validationErrorText.setHTML("");
			
			//enable saving, since the metadata is valid
			GatClientUtility.setModalSaveEnabled(true);
		}
	}
	
	/**
	 * On editor dirty event.
	 *
	 * @param event the event
	 */
	@EventHandler
	protected void onEditorDirtyEvent(EditorDirtyEvent event) {
		showValidationState();
	}
	
	/**
     * Save the Metadata. Called by native functions (see {@link #exposeNativeFunctions()}.
     */
	private void saveMetadata(){
	    if(!readOnly && filePath != null){
	        saveMetadata(filePath, false);
	    }
	}
	
	public native void exposeNativeFunctions()/*-{
    
		var that = this;
		
		$wnd.stop = $entry(function(){
			that.@mil.arl.gift.tools.authoring.server.gat.client.view.metadata.MetadataEditor::stop()();
		});
		
		$wnd.saveCourseAndNotify = $entry(function(){
            that.@mil.arl.gift.tools.authoring.server.gat.client.view.metadata.MetadataEditor::saveMetadata()();
            $wnd.parent.saveCourseAndNotify();
        });
	
	}-*/;
}
