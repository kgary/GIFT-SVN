/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.course;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.BooleanEnum;
import generated.course.Guidance;
import generated.course.ShowAvatarInitially;
import generated.course.ShowAvatarInitially.MediaSemantics;
import generated.course.TrainingApplication;
import generated.course.TrainingApplication.Options;
import generated.course.TrainingApplication.Options.Remediation;
import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseFolderChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRedrawEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.ScenarioSavedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppView;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModelResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemantics;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemanticsResult;

/**
 * The Class TrainingAppPresenter.
 */
public class TrainingAppPresenter extends AbstractGatPresenter implements TrainingAppView.Presenter{
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(TrainingAppPresenter.class.getName());
	
	/** The Constant MEDIA_SEMANTICS_AVATAR_FILE_EXT (file extension for Media Semantic Avatar file) */
	private static final String MEDIA_SEMANTICS_AVATAR_FILE_EXT = ".html";
	
	private static final String NO_FILE_SELECTED = "No file selected";
	
	/**
     * Interface for the event binder for this class.  
     * @author cragusa
     */
    interface MyEventBinder extends EventBinder<TrainingAppPresenter> {
    }   
    
    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT
            .create(MyEventBinder.class);
    
    /** The file browser presenter. */
    private DefaultGatFileSelectionDialog fileDialog = null;
        
    /**
     * The view used to handle user input and display data back to the user
     */
    private TrainingAppView view;
    
    /**
     * The {@link TrainingApplication} currently being edited
     */
    private TrainingApplication currentTrainingApp;
    
    /**
     * Creates a new presenter managing the given view
     * 
     * @param view the view to be managed
     */
    public TrainingAppPresenter(TrainingAppView view) {
    	
        super();
        
        this.view = view;
        
        start();
        
        init();
    }    
    
    /**
     * Loads the given {@link TrainingApplication} into the view for editing
     * 
     * @param trainingApp the training application to edit
     */
    public void edit(TrainingApplication trainingApp){
    	
    	this.currentTrainingApp = trainingApp;

    	view.setInteropEditorTrainingApplication(currentTrainingApp);
		view.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
		view.setCourseSurveyContextId(GatClientUtility.getBaseCourseSurveyContextId());
		
    	populateView();
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#start()
     */
    @Override
    public void start(){
    	super.start();
    	eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#stop()
     */
    @Override
    public void stop(){
    	super.stop();
    }
    
    private void init() {    
    	
    	//Header Editor
        
        if(GatClientUtility.isReadOnly()){
            
            view.getFullScreenHasEnabled().setEnabled(false);
            view.getDisableTutoringHasEnabled().setEnabled(false);
            view.getShowAvatarHasEnabled().setEnabled(false);
            view.getGuidanceCreator().setEnabled(false);
            view.getRemoveAvatarHasEnabled().setEnabled(false);
            view.getDisabledInputHasEnabled().setEnabled(false);
            view.setRemediationEditingEnabled(false);            
        }else{
    	
        	handlerRegistrations.add(view.getFullScreenInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){
    
    			@Override
    			public void onValueChange(ValueChangeEvent<Boolean> event) {
    
    				if(currentTrainingApp != null){					
    
    					if(currentTrainingApp.getOptions() == null){
    						currentTrainingApp.setOptions(new Options());
    					}
    					
    					currentTrainingApp.getOptions().setFullScreen(event.getValue() != null && event.getValue()
    							? BooleanEnum.TRUE
    							: BooleanEnum.FALSE
    					);
    					
    					eventBus.fireEvent(new EditorDirtyEvent());
    				}
    			}
        		
        	}));
        	
        	setChoiceSelectionListener(new Command() {
                
                @Override
                public void execute() {
                    
                    logger.info("running choice selection listener");
                    
                    //hide the change application button when a type has been selected
                    view.getChangeApplicationButton().setVisible(currentTrainingApp.getInterops() != null && GatClientUtility.isRtaLessonLevel());
                
                    //if a different type of application is selected, let any listeners know that this training app needs to be redrawn
                    SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectRedrawEvent(currentTrainingApp));
                    
                    logger.info("FINISHED running choice selection listener");
                }
            });
        	
        	handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    
                    if(currentTrainingApp != null){        
                        
                        if(currentTrainingApp.getOptions() == null){
                            currentTrainingApp.setOptions(new Options());
                        }
                        
                        currentTrainingApp.getOptions().setDisabled(event.getValue() != null && event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                        );
                                            
                        eventBus.fireEvent(new EditorDirtyEvent());
                                            
                        SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentTrainingApp));
                    }
                }
                
            }));
    	
        	handlerRegistrations.add(view.getDisableInstrInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){
    
    			@Override
    			public void onValueChange(ValueChangeEvent<Boolean> event) {
    
    				if(currentTrainingApp != null){					
    
    					if(currentTrainingApp.getOptions() == null){
    						currentTrainingApp.setOptions(new Options());
    					}
    					
    					currentTrainingApp.getOptions().setDisableInstInterImpl(event.getValue() != null && event.getValue()
    							? BooleanEnum.TRUE
    							: BooleanEnum.FALSE
    					);
    					
    					eventBus.fireEvent(new EditorDirtyEvent());
    				}
    			}
        		
        	}));
    	
        	handlerRegistrations.add(view.getShowAvatarInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {

                    if(currentTrainingApp != null){                 

                        if(currentTrainingApp.getOptions() == null){
                            currentTrainingApp.setOptions(new Options());
                        }
                        
                        if(event.getValue() != null && event.getValue()){
                            //checkbox is checked
                            
                            ShowAvatarInitially showAvatarSettings = new ShowAvatarInitially();                     
                            
                            currentTrainingApp.getOptions().setShowAvatarInitially(showAvatarSettings);
                            
                            view.getChooseAvatarButtonInputHasVisibility().setVisible(true);
                            view.getAvatarSelectedHasVisibility().setVisible(false);
                        
                        } else {
                            //checkbox is not checked
                            
                            currentTrainingApp.getOptions().setShowAvatarInitially(null);
                            
                            view.getChooseAvatarButtonInputHasVisibility().setVisible(false);
                            view.getAvatarSelectedHasVisibility().setVisible(false);
                        }           
                        
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }
                    
                }
                
            }));    
            
            fileDialog = new DefaultGatFileSelectionDialog(new CanGetRootDirectory() {

                @Override
                public void getRootDirectory(final GetRootDirectoryCallback callback) {

                    AsyncCallback<FetchRootDirectoryModelResult> asyncCallback = new AsyncCallback<FetchRootDirectoryModelResult>(){

                        @Override
                        public void onFailure(Throwable thrown) {
                            callback.onFailure(thrown);
                        }

                        @Override
                        public void onSuccess(FetchRootDirectoryModelResult result) {

                                if(result.isSuccess()){
                                    callback.onSuccess(result.getDomainDirectoryModel().getModelFromRelativePath(
                                            DefaultGatFileSelectionDialog.courseFolderPath));

                                } else {

                                    if(result.getErrorMsg() != null){
                                        callback.onFailure(result.getErrorMsg());

                                    } else {
                                        callback.onFailure("An error occurred while getting the root directory.");
                                    }
                                }
                            }

                        };

                        String userName = GatClientUtility.getUserName();

                        FetchRootDirectoryModel action = new FetchRootDirectoryModel();
                        action.setUserName(userName);       

                        SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
                    }
                });
            
            fileDialog.setUploadEnabledIfPossible(false, 
                    "An agent consists of multiple files.  GIFT currently doesn't have logic to copy only the necessary files for the agent and not other unrelated files.");
            fileDialog.setIntroMessageHTML(" Select the agent file in this course folder.<br>"+
                    " Supported file extensions are : <b>"+MEDIA_SEMANTICS_AVATAR_FILE_EXT+
                    "</b><br> In the future GIFT will have logic to allow uploading a character project's output as a zip.");
            
    	handlerRegistrations.add(view.getChooseAvatarButtonInput().addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				fileDialog.center();
			}
    		
    	}));
    	
            handlerRegistrations.add(view.getRemoveAvatarButtonInput().addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent arg0) {

                    if(currentTrainingApp != null 
                            && currentTrainingApp.getOptions() != null
                            && currentTrainingApp.getOptions().getShowAvatarInitially() != null
                            && currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice() != null){
                        
                        currentTrainingApp.getOptions().getShowAvatarInitially().setAvatarChoice(null); 
                        
                        view.getAvatarFileLabel().setText("Select Agent");
                        view.getAvatarSelectedHasVisibility().setVisible(false);
                        view.getChooseAvatarButtonInputHasVisibility().setVisible(true);
                        
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }
                    
                }
            }));
            
            handlerRegistrations.add(fileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    
                    if(currentTrainingApp != null 
                            && currentTrainingApp.getOptions() != null
                            && currentTrainingApp.getOptions().getShowAvatarInitially() != null){
                    	
                    	if(currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice() == null){
                    		currentTrainingApp.getOptions().getShowAvatarInitially().setAvatarChoice(new MediaSemantics());
                    	}
                        
                        currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice().setAvatar(event.getValue()); 
                        
                        view.getAvatarFileLabel().setText(event.getValue());
                        view.getAvatarSelectedHasVisibility().setVisible(true);
                        view.getChooseAvatarButtonInputHasVisibility().setVisible(false);
                           
                        validateMediaSemantics(event.getValue(), false);                    
                        
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }
                }
            }));
            
        //add remediation and display the UI to edit it when the author clicks the button to add remediation
        handlerRegistrations.add(view.getAddRemediationButton().addClickHandler(new ClickHandler() {
                
            @Override
            public void onClick(ClickEvent event) {
                
                if(currentTrainingApp.getOptions() == null) {
                    currentTrainingApp.setOptions(new Options());
                }
                
                if(currentTrainingApp.getOptions().getRemediation() == null) {
                    currentTrainingApp.getOptions().setRemediation(new Remediation());
                }
                
                refreshRemediation();
            }
        }));
        
        //remove remediation and hide the UI to edit it when the author clicks the button to remove remediation
        handlerRegistrations.add(view.getDeleteRemediationButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(currentTrainingApp.getOptions() != null) {
                    currentTrainingApp.getOptions().setRemediation(null);
                }
                
                refreshRemediation();
            }
        }));
            
        view.getGuidanceCreator().setTrainingAppEmbedded(true);
    	view.getGuidanceCreator().hideMessageEditor(true);
            handlerRegistrations.add(view.getGuidanceCreator().addValueChangeHandler(new ValueChangeHandler<Guidance>() {
                
                @Override
                public void onValueChange(ValueChangeEvent<Guidance> event) {
                    
                    if(currentTrainingApp != null){
                    
                        Guidance guidance = event.getValue();
                    
                        if(guidance != null){
                            guidance.setTransitionName(currentTrainingApp.getTransitionName() + " - Guidance");     
                        }
                        
                        currentTrainingApp.setGuidance(guidance);                   
                        
                    eventBus.fireEvent(new EditorDirtyEvent());
                }
                }
            }));
            
            // Set the dialog to only allow *.html files. 
            fileDialog.getFileSelector().setAllowedFileExtensions(new String[]{MEDIA_SEMANTICS_AVATAR_FILE_EXT});
        }
        
        view.setDkfChangedCommand(new Command() {
            
            @Override
            public void execute() {
                refreshRemediation();
            }
        });
        
        view.getChangeApplicationButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                final TrainingApplication trainingApp = currentTrainingApp;
                if(currentTrainingApp == null) {
                    return;
                }
                
                if(trainingApp.getDkfRef() != null && StringUtils.isNotBlank(trainingApp.getDkfRef().getFile())){
                    // show are you sure you want to change training applications and either delete
                    // the dkf or just remove the reference to the dkf 
                    
                    // get the training application type for display purposes
                    String tAppStr = "current";
                    TrainingApplicationEnum tappEnum = TrainingAppUtil.getTrainingAppType(trainingApp);
                    if(tappEnum != null){
                        tAppStr = tappEnum.getDisplayName();
                    }
                    
                    DeleteRemoveCancelDialog.show("Change Training Application?", "The real time assessment created for this "+tAppStr+" application can not be applied"
                            + " to another training application.<br/><br/>"
                            + "If you proceed with ONLY changing the training application, the real time assessment file will be kept and can be selected from your workspace later.<br/></br>"
                            + "(<b>file name:</b> "+trainingApp.getDkfRef().getFile()+")", new DeleteRemoveCancelCallback() {
                                
                                @Override
                                public void remove() {
                                    
                                    logger.info("Removing reference to real time assessment file: "+trainingApp.getDkfRef().getFile());
                                    clearTrainingAppType();
                                }
                                
                                @Override
                                public void delete() {
                                    
                                    logger.info("Removing real time assessment file from server: "+trainingApp.getDkfRef().getFile());
                                    final String filename = GatClientUtility.getBaseCourseFolderPath()
                                                        + "/"
                                                        + trainingApp.getDkfRef().getFile();
                                    
                                    String username = GatClientUtility.getUserName();
                                    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                    List<String> filesToDelete = new ArrayList<String>(1);
                                    filesToDelete.add(filename);
                                    
                                    //Performs the deletion on the server
                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                        @Override
                                        public void onFailure(Throwable throwable) {
                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                    "Failed to delete the real time assessment file.", 
                                                    throwable.getMessage(), 
                                                    DetailedException.getFullStackTrace(throwable));
                                            dialog.setDialogTitle("Deletion Failed");
                                            dialog.center();
                                        }

                                        @Override
                                        public void onSuccess(GatServiceResult result) {
                                            if(result.isSuccess()) {
                                                remove();
                                            }else if(result.getErrorMsg() != null) {
                                                logger.warning("Unable to delete the file: " + filename + "\nError Message: " + result.getErrorMsg());
                                            }
                                        }
                                        
                                    });
                                    
                                    clearTrainingAppType();
                                }
                                
                                @Override
                                public void cancel() {
                                    //nothing to do                                    
                                }
                            }, "Delete Real Time assessment and change application", "Change Application");
                }else{    
                    clearTrainingAppType();
                }
            }
        });
        
    	exposeNativeFunctions();
    }
    
    /**
     * Clears the training application being edited of any data identifying it as a particular type and reloads it.
     * This will remove the training application enum and all interops from the application so that effectively has no type.
     */
    private void clearTrainingAppType() {
        currentTrainingApp.setTrainingAppTypeEnum(null);
        currentTrainingApp.setInterops(null);                   
        edit(currentTrainingApp);
        
        SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectRedrawEvent(currentTrainingApp));
        }
        
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
	 */
	@Override
	protected Logger getLogger() {
		return logger;
	}	
	
	/**
	 * Clears the view, setting all fields to their initial state.
	 */
	private void clearView(){
		
		view.getDisableInstrInput().setValue(null);
		
		view.getFullScreenInput().setValue(null);
		
		view.getShowAvatarInput().setValue(null);
		
		view.getDisabledInput().setValue(null);
	}
	
	/**
	 * Populates the view based on the current training app.
	 */
	private void populateView(){

		clearView();
		
		if(currentTrainingApp != null){
			
		    view.getChangeApplicationButton().setVisible(currentTrainingApp.getInterops() != null && GatClientUtility.isRtaLessonLevel());
		    
			//populate header editor
			
			view.getFullScreenInput().setValue(currentTrainingApp.getOptions() != null && currentTrainingApp.getOptions().getFullScreen() != null
					? currentTrainingApp.getOptions().getFullScreen().equals(BooleanEnum.TRUE) 
					: null
			);
			
            view.getDisabledInput().setValue(currentTrainingApp.getOptions() != null && currentTrainingApp.getOptions().getDisabled() != null
                    ? currentTrainingApp.getOptions().getDisabled().equals(BooleanEnum.TRUE)
                    : false // not checked by default
                    , true);
			
			view.getDisableInstrInput().setValue(currentTrainingApp.getOptions() != null && currentTrainingApp.getOptions().getDisableInstInterImpl() != null
					? currentTrainingApp.getOptions().getDisableInstInterImpl().equals(BooleanEnum.TRUE) 
					: null
			);
			
			refreshRemediation();
			
			if(currentTrainingApp.getOptions() != null
                    && currentTrainingApp.getOptions().getShowAvatarInitially() != null){
			    
			    view.getShowAvatarInput().setValue(true);
			    
			    if(currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice() != null &&
			            currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice().getAvatar() != null){
			        view.getAvatarFileLabel().setText(currentTrainingApp.getOptions().getShowAvatarInitially().getAvatarChoice().getAvatar());
			        view.getChooseAvatarButtonInputHasVisibility().setVisible(false);
			        view.getAvatarSelectedHasVisibility().setVisible(true);
			    }else{
			        //no avatar value provided
                    view.getChooseAvatarButtonInputHasVisibility().setVisible(true);
                    view.getAvatarSelectedHasVisibility().setVisible(false);
                    
                    if(GatClientUtility.isReadOnly()){
                        view.getSelectAvatarFileLabel().setText(NO_FILE_SELECTED);
                    }
			    }
			}else{
			    //no avatar value provided
			    
			    view.getShowAvatarInput().setValue(null);
                view.getChooseAvatarButtonInputHasVisibility().setVisible(false);
                view.getAvatarSelectedHasVisibility().setVisible(false);
			}

			
			if(currentTrainingApp.getFinishedWhen() == null){
				currentTrainingApp.setFinishedWhen(TrainingApplicationStateEnum.STOPPED.getName());
			}

	    	
	    	view.getGuidanceCreator().reset();
	    	view.getGuidanceCreator().setValue(currentTrainingApp.getGuidance());
		}
	}
	
	/**
	 * Refreshes the appearance of the remediation panel to match the underlying schema objects. If a DKF file has
	 * been chosen, this will also refresh the lists of remediation files based on the concepts implemented by that
	 * DKF's scenario.
	 */
	private void refreshRemediation() {
	    
	    List<String> courseConcepts = GatClientUtility.getBaseCourseConcepts();
	    
	    if(courseConcepts == null || courseConcepts.isEmpty() || currentTrainingApp.getDkfRef() == null) {
	        
	        //if no concepts have been defined or no DKF has been chosen, don't bother attempting to read the DKF's scenario for concepts
	        view.setRemediation(
                    currentTrainingApp.getOptions() != null ? currentTrainingApp.getOptions().getRemediation() : null, 
                    null,
                    true);
	        return;
	    }
        
	    //reset the appearance of the remediation panel until the remediation concepts are refreshed
	    view.setRemediation(currentTrainingApp.getOptions() != null ? currentTrainingApp.getOptions().getRemediation() : null, null, false);
            
        //read the DKF scenario to see if it implements any of the course's concepts
        SharedResources.getInstance().getRpcService().getScenarioConcepts(
            GatClientUtility.getUserName(), 
            GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + currentTrainingApp.getDkfRef().getFile(), 
            new HashSet<>(courseConcepts), 
            new AsyncCallback<GenericRpcResponse<Set<String>>>() {
                
                @Override
                public void onSuccess(GenericRpcResponse<Set<String>> result) {
                    
                    ArrayList<String> concepts = null;
                    
                    if(result.getWasSuccessful()) {
                        concepts = result.getContent() != null ? new ArrayList<>(result.getContent()) : null;
                    }
                    
                    view.setRemediation(
                            currentTrainingApp.getOptions() != null ? currentTrainingApp.getOptions().getRemediation() : null, 
                            concepts,
                            true);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    view.setRemediation(
                            currentTrainingApp.getOptions() != null ? currentTrainingApp.getOptions().getRemediation() : null, 
                            null,
                            true);
                }
            });
    }

    /**
	 * Validates an Avatar.html file for compatibility with GIFT.
	 * 
	 * @param filePath The path to the Avatar.html file
	 * @param updateInvalidFiles True if any invalid files should be updated.
	 */
	private void validateMediaSemantics(final String filePath, final boolean updateInvalidFiles) {

		ValidateMediaSemantics action = new ValidateMediaSemantics(
				GatClientUtility.getUserName(), 
				DefaultGatFileSelectionDialog.courseFolderPath + "/" + filePath, 
				updateInvalidFiles);

		BsLoadingDialogBox.display("Validating File", "Validating, please wait...");
		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<ValidateMediaSemanticsResult>() {

			@Override
			public void onFailure(Throwable thrown) {
			    BsLoadingDialogBox.remove();
				WarningDialog.error("Failed to validate", "The server threw an exception while validating the media semantics file.\n" + thrown.toString());
			}

			@Override
			public void onSuccess(ValidateMediaSemanticsResult result) {
			    BsLoadingDialogBox.remove();

				if(!result.isSuccess()) {
					ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
							result.getErrorMsg(), 
							result.getErrorDetails(), 
							result.getErrorStackTrace());
					errorDialog.setText("An Error Occurred");
					errorDialog.center();
				} else {

					if(!updateInvalidFiles && !result.isValidFile()) {
						OkayCancelDialog.show("Incompatible File", "The file you selected is not compatible with GIFT. As a result, "
								+ "this agent may not work correctly during the course.<br/>Would you like to update the file to be "
								+ "compatible with GIFT?", "Update File", new OkayCancelCallback() {

							@Override
							public void okay() {
								validateMediaSemantics(filePath, true);
							}

							@Override
							public void cancel() {
								//Nothing to do
							}
						});
					} else if(updateInvalidFiles){
						Notify.notify("The Media Semantics character file was successfully converted to a GIFT compatible version", NotifyType.SUCCESS);
					}
				}
			}

		});
	}
	
	public TrainingApplicationWrapper getCurrentTrainingAppStr() {
		TrainingApplicationWrapper taWrapper = new TrainingApplicationWrapper();
		taWrapper.setTrainingApplication(currentTrainingApp);		
		return taWrapper;
	}
	
	private native void exposeNativeFunctions() /*-{
		var that = this;
		
		$wnd.getCurrentTrainingAppStr = $entry(function(){
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.TrainingAppPresenter::getCurrentTrainingAppStr()();
		});
	}-*/;

	/**
	 * Assigns a listener that will be notified when the user selects a different type of application
	 * 
	 * @param command the listener command. can be null.
	 */
    public void setChoiceSelectionListener(final Command command) {
    	
		view.setChoiceSelectionListener(new Command() {
			
			@Override
			public void execute() {
			    
			    logger.info("running choice selection listener");
							    
				if(currentTrainingApp.getEmbeddedApps() != null || GatClientUtility.isRtaLessonLevel()){
					
					view.getGuidanceCreator().setVisible(false);
					view.setAvatarFullScreenInputVisible(false);
					
				} else {
					
					view.getGuidanceCreator().setVisible(true);
					view.setAvatarFullScreenInputVisible(true);
				}
				
				if(command != null){
				    command.execute();	
				}
			}
		});
	}
    
    @EventHandler
    protected void onCourseFolderChanged(CourseFolderChangedEvent event){       
        logger.info("Notified that the course folder path changed");
        view.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
    }
    
    @EventHandler
    protected void onCourseConceptsChanged(CourseConceptsChangedEvent event){   
        
        if(currentTrainingApp != null) {
            refreshRemediation();
        }
    }
    
    /**
     * Refreshes this editor's displayed data whenever a scenario is saved
     * 
     * @param event an event indicating that a scenario was saved. Cannot be null.
     */
    @EventHandler
    protected void onScenarioSaved(ScenarioSavedEvent event){       
        
        if(currentTrainingApp != null) {
            refreshRemediation();
        }
    }
}
