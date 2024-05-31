/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.metadata;

import generated.course.Guidance;
import generated.course.InteropInputs;
import generated.course.PowerPointInteropInputs;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppInteropEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * An editor used to author training application references
 * 
 * @author nroberts
 */
public class TrainingAppRefEditor extends Composite {
    
    private static Logger logger = Logger.getLogger(TrainingAppRefEditor.class.getName());

	private static TrainingAppRefEditorUiBinder uiBinder = GWT
			.create(TrainingAppRefEditorUiBinder.class);

	interface TrainingAppRefEditorUiBinder extends
			UiBinder<Widget, TrainingAppRefEditor> {
	}
	
	@UiField
    protected OptionalGuidanceCreator applicationGuidanceCreator;
	
	@UiField
	protected TrainingAppInteropEditor taInteropEditor;
	
	/**
	 * The current training application wrapper
	 */
	private TrainingApplicationWrapper taWrapper = null;
	
	/** An optional command to execute once the course object has finished loading */
	private Command finishedLoadingCommand = null;

	public TrainingAppRefEditor() {
		
		initWidget(uiBinder.createAndBindUi(this));
       
		applicationGuidanceCreator.setTrainingAppEmbedded(true);
		applicationGuidanceCreator.hideMessageEditor(true);
		applicationGuidanceCreator.addValueChangeHandler(new ValueChangeHandler<Guidance>() {
		
			@Override
			public void onValueChange(ValueChangeEvent<Guidance> event) {
				
				if(taWrapper != null 
						&& taWrapper.getTrainingApplication() != null){
					
					if(event.getValue() != null){
						generateGuidanceName(event.getValue());
					}
					
					taWrapper.getTrainingApplication().setGuidance(event.getValue());
				}
			}
		});
		
		taInteropEditor.setChoiceSelectionListener(new Command() {
			
			@Override
			public void execute() {
			    
			    logger.info("running choice selection listener");

				boolean hasTrainingApp = taWrapper != null && taWrapper.getTrainingApplication() != null;
				
				if(hasTrainingApp 
						&& (taWrapper.getTrainingApplication().getInterops() != null || taWrapper.getTrainingApplication().getEmbeddedApps() != null)){
					
					if(taWrapper.getTrainingApplication().getEmbeddedApps() != null){
						
						//hide loading messages for embedded apps, since they don't make sense
						applicationGuidanceCreator.setVisible(false);
						
					} else {
						applicationGuidanceCreator.setVisible(true);
					}
				
				} else {
					
					if(hasTrainingApp){
						
						//reset the training app guidance
						taWrapper.getTrainingApplication().setGuidance(null);
						applicationGuidanceCreator.setValue(null);
					}
					
					applicationGuidanceCreator.setVisible(false);
				}
				
				logger.info("FINISHED running choice selection listener");
			}
		});
	}
	
	/**
	 * Sets a generic name for the guidance transition if there is no transition name specified.
	 */
	private void generateGuidanceName(Guidance guidance) {
		if(guidance == null) {
			return;
		}
		
		if(guidance.getTransitionName() == null || guidance.getTransitionName().trim().isEmpty()) {
			guidance.setTransitionName("Training App Loading Guidance");
		}
	}
	
	/**
	 * Sets the training application wrapper to edit and modifies the UI to match the state of its current values
	 * 
	 * @param wrapper the training application wrapper to edit
	 */
	public void setTrainingApplicationWrapper(TrainingApplicationWrapper wrapper){
		
		taWrapper = wrapper;		
		
		if(taWrapper != null){
			
			if(taWrapper.getTrainingApplication() != null){
				
				//Edit guidance button and dialog
				if(taWrapper.getTrainingApplication().getGuidance() != null){	
					
					generateGuidanceName(taWrapper.getTrainingApplication().getGuidance());
					
					applicationGuidanceCreator.setValue(taWrapper.getTrainingApplication().getGuidance());		
					
				} else {					
					applicationGuidanceCreator.setValue(null);
				}
				
				taInteropEditor.setTrainingApplication(taWrapper.getTrainingApplication());
			}
		}
		
		onFinishedLoading();
	}
								
	/**
	 * Gets the training application wrapper currently being edited
	 * 
	 * @return the training application wrapper currently being edited
	 */
	public TrainingApplicationWrapper getTrainingApplicationWrapper(){
		return taWrapper;
	}	

	/**
	 * @return TrainingApplicationWrapper
	 */
	public TrainingApplicationWrapper generateTrainingApplicationWrapper() {
		
		TrainingApplicationWrapper taWrapper = new TrainingApplicationWrapper();
		
		PowerPointInteropInputs.LoadArgs loadArgs = new PowerPointInteropInputs.LoadArgs();
		loadArgs.setShowFile(null);
		
		PowerPointInteropInputs inputs = new PowerPointInteropInputs();
		inputs.setLoadArgs(loadArgs);
		
		InteropInputs interopInputs = new InteropInputs();
		interopInputs.setInteropInput(inputs);
		
		TrainingApplication trainingApp = new TrainingApplication();	
		
		taWrapper.setTrainingApplication(trainingApp);			    			
		
		return taWrapper;
	}
	
	/**
     * Loads a training application wrapper if a valid path is supplied, if null is
     * supplied then it creates a new training application wrapper that only exists on
     * the client side (if the user saves it then it'll be stored on the
     * server)
     * @param filePath The file path of the metadata.
     * @param relativePath Path to the training application wrapper  you want to
     * load, NULL if you want to create a new training application wrapper .
     */
    public void loadTrainingApplicationReference(final String filePath, final String relativePath) {
		
    	if(relativePath == null){
    		
    		taWrapper = generateTrainingApplicationWrapper();
    		setTrainingApplicationWrapper(taWrapper);
    		
    	} else {
    		AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){
    			@Override
    			public void onFailure(Throwable t) {
    				
    				WarningDialog.error("Load failed", "An error occurred while trying to load a training application file: " + t.getMessage());
    			
    				onFinishedLoading();
    			}

    			@Override
    			public void onSuccess(FetchJAXBObjectResult result) {
    				
    				if(result.isSuccess()){
	    				taWrapper = (TrainingApplicationWrapper) result.getJAXBObject();
	    				setTrainingApplicationWrapper(taWrapper);
	    				
    				} else {
    					
    					ErrorDetailsDialog error = new ErrorDetailsDialog(result.getErrorMsg(), 
    							result.getErrorDetails(), result.getErrorStackTrace());
    					error.setText("Failed to Load Training Application");
    					error.center();
    					error.addCloseHandler(new CloseHandler<PopupPanel>() {

							@Override
							public void onClose(CloseEvent<PopupPanel> arg0) {
								closeFile(filePath);
							}
    						
    					});
    					
    					onFinishedLoading();
    				}
    			}		
    		};
    		
    		FetchJAXBObject action = new FetchJAXBObject();
    		action.setRelativePath(GatClientUtility.getBaseCourseFolderPath() + "/" + relativePath);
    		action.setUserName(GatClientUtility.getUserName());
    		
    		taInteropEditor.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
    		taInteropEditor.setCourseSurveyContextId(GatClientUtility.getBaseCourseSurveyContextId());
    		
    		SharedResources.getInstance().getDispatchService().execute(action, callback);
    	}
	}
    
    void stopGuidancePresenter(){
    	
    }
    
    public TrainingAppInteropEditor getInteropEditor(){
    	return taInteropEditor;
    }
    
    private native void closeFile(String path)/*-{
	
		if($wnd.parent != null){
			$wnd.parent.closeFile(path);
			
		}
		
	}-*/;
    
    /**
	 * Sets a command to execute once this editor finishes loading a course object. If no command is specified, then only the editor's
	 * internal logic will be invoked when the course object finishes loading
	 * 
	 * @param onFinishedLoading the command to execute when the course object finishes loading.
	 */
    public void setFinishedLoadingCommand(Command onFinishedLoading){
		this.finishedLoadingCommand = onFinishedLoading;
	}
    
    /**
	 * Executes whatever command was specified by {@link #setFinishedLoadingCommand(Command)}, if one was provided. If no command
	 * was provided, this method will do nothing.
	 */
    protected void onFinishedLoading(){
		
		if(finishedLoadingCommand != null){
			finishedLoadingCommand.execute();
		}
	}
}
