/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyImageInterface;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyImageUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The QuestionImagePropertySetWidget is responsible for displaying the controls that
 * allow the author to add a custom image to the question text.
 * 
 * @author nblomberg
 *
 */
public class QuestionImagePropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(QuestionImagePropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, QuestionImagePropertySetWidget> {
	}
	
	/** The button used to add media */
    @UiField
    protected Button addMediaButton;
    
    /** The button used to add media */
    @UiField
    protected Button noMediaButton;
    
    /** The panel containing the button used to add media */
    @UiField
    protected FlowPanel addMediaPanel;
    
    /** The panel containing UI controls to edit the current media */
    @UiField
    protected FlowPanel mediaEditorPanel;
    
    /** The deck used to switch between the UIs for when media is/isn't authored */
    @UiField
    protected DeckPanel displayImageCollapse;
	
	
	@UiField
	protected ListBox locationBox;

	@UiField
	protected ListBox positionBox;
	
	@UiField
	protected TextBox widthBox;
	
	/** The panel containing the editor components for legacy images */
	@UiField
	protected Widget legacyImgPanel;
	
	/** The panel containing the components used to modify the media */
    @UiField
    protected Widget mediaPanel;
	
	/** The last width value that the was set for the image. */
	private Integer lastWidthValue = QuestionImagePropertySet.DEFAULT_WIDTH;
	
	/** The imagesList contains the list of images from the server. It must be kept in sync (including order) with the listBox contents. */
	private ArrayList<String> imagesList = new ArrayList<String>();
	
	/** file selection dialog to upload a new image for the current question */
    FileSelectionDialog fileUploadModal;
	
	/** The dialog used to select media files to place into the course folder associated with the survey */
	private DefaultGatFileSelectionDialog mediaFileDialog = new DefaultGatFileSelectionDialog();

	/** The label showing that a media file has been selected */
    @UiField
    protected Label mediaFileLabel;
    
    @UiField
    protected Widget sizePanel;
	
	/**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);

	/**
	 * Constructor (default)
	 * @param propertySet - The property set for the widget.
	 * @param listener - The listener that can be used to handle changes in the properties.
	 */
    public QuestionImagePropertySetWidget(QuestionImagePropertySet propertySet, PropertySetListener listener, SurveyImageInterface imageInterface) {
	    super(propertySet, listener);
        if(logger.isLoggable(Level.INFO)){
            logger.info("constructor()");
        }
	    
	    // This must be done before initWidget() is called.
        fileUploadModal = new FileSelectionDialog(GwtSurveySystemProperties.SURVEY_IMAGE_UPLOAD_URL, DefaultMessageDisplay.includeAllMessages);
        
        fileUploadModal.setAnimationEnabled(true);
        fileUploadModal.setAllowedFileExtensions(new String[]{".gif", ".jpg", ".jpeg", ".png", ".GIF", ".JPG", ".JPEG", ".PNG"});
        fileUploadModal.setText("Select an Image");
        
	    initWidget(uiBinder.createAndBindUi(this));

	    locationBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				
                if(logger.isLoggable(Level.INFO)){
                    logger.info("image changed: " + locationBox.getSelectedValue());
                }
				QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
				if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE)){
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, locationBox.getSelectedValue());
				}
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    positionBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				
				QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
				props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, positionBox.getSelectedIndex());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    widthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					
					try{
						
						Integer width = Integer.valueOf(event.getValue());
					
						props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, width);
						lastWidthValue = width;
						
					} catch(@SuppressWarnings("unused") Exception e){
						
						WarningDialog.error("Invalid value", "Please enter a number for the image width or leave the field blank.");
						
						widthBox.setValue(lastWidthValue != null ? lastWidthValue.toString() : QuestionImagePropertySet.DEFAULT_WIDTH.toString()); //reset the width box
						
						return;
					}
					
				} else {
					
					props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, QuestionImagePropertySet.DEFAULT_WIDTH);
					lastWidthValue = QuestionImagePropertySet.DEFAULT_WIDTH;
				}			
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    initMediaFileHandlers();

	    if(propertySet != null){
	    	
	    	Serializable displayImage = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
	    	
	    	boolean isLegacyImage = false;
	    	
	    	if(displayImage != null && displayImage instanceof Boolean){
	    		
	    		if((Boolean) displayImage){
	    		    
	    		    Serializable mediaType = propertySet.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
	    		    
	    		    if(mediaType == null) {
	    		        isLegacyImage = true;
	    		    }
				}
	    	}
	    	
	    	String location = (String)propertySet.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
	    	
	    	if(isLegacyImage) {
	    	    
	    	    legacyImgPanel.setVisible(true);
	    	    mediaPanel.setVisible(false);
    	    		    	
                if(logger.isLoggable(Level.INFO)){
                    logger.info("Populating drop down image list with selection: " + location);
                }
                
    	    	populateSurveyImagesList(imageInterface.getSurveyImageList(), location);
    	    	
    	    	displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(mediaEditorPanel));
    	    	
	    	} else {
	    	    
	    	    legacyImgPanel.setVisible(false);
	    	    mediaPanel.setVisible(true);
	    	    
	    	    if(location != null){
                    mediaFileLabel.setText(location);                   
                    displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(mediaEditorPanel));
                    
                } else {
                    mediaFileLabel.setText("Select a file");
                    displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(addMediaPanel));
                }
	    	    
	    	    /* Don't let the author modify the size of audio or unknown media files, since the size isn't really
                 * able to be controlled */
	    	    sizePanel.setVisible(Constants.isVideoFile(location) || Constants.isImageFile(location));
	    	}
	    	
	    	Serializable position = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
	    	
	    	if(position != null && position instanceof Integer){
	    		positionBox.setSelectedIndex((Integer) position);
	    		
	    	} else {
	    		positionBox.setSelectedIndex(0);
	    	}
	    	
	    	Serializable width = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY);
	    	
	    	if(width != null && width instanceof Integer){
	    		widthBox.setValue(((Integer) width).toString());
	    		
	    	} else {
	    		widthBox.setValue(QuestionImagePropertySet.DEFAULT_WIDTH.toString());
	    	}
	    	
	    	propListener.onPropertySetChange(propSet);
	    }

	    fileUploadModal.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (event.getValue() != null) {
                    final String browsedImage = GwtSurveySystemProperties.SURVEY_IMAGE_UPLOAD_URL + event.getValue();
                    rpcService.getSurveyImages(new AsyncCallback<List<String>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.severe("Failure to retrieve the latest list of images from the server.");
                        }

                        @Override
                        public void onSuccess(List<String> result) {
                            if (result != null) {
                                populateSurveyImagesList(result, browsedImage);
                            } else {
                                logger.severe("Server returned a null result when fetching the lastest list of images from the server.");
                            }
                        }
                    });
                }
            }
            
        });
	}
    
    /**
     * Populates the list box with the list of images and sets the value to the selectedImage.
     * 
     * @param imageList - The list of images to populate the list box with.
     * @param selectedImage - The value that should be selected in the list box.  If the value cannot be found in the list, then it is
     *                        added to the list.
     */
    private void populateSurveyImagesList(List<String> imageList, String selectedImage) {
        if(logger.isLoggable(Level.INFO)){
            logger.info("populateSurveyImagesList() called: " + selectedImage);
        }

        SurveyImageUtil.populateSurveyImagesList(imageList,  imagesList, locationBox, selectedImage);

        // Update the property for the image key once the location box is rebuilt.
        QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
        if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE)){
        	props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, locationBox.getSelectedValue());
        }
        propListener.onPropertySetChange(propSet); 
    }
    
    /**
     * Initializes the UI handlers needed to allow the user to select and interact with media files from
     * the course folder associated with the survey
     */
    private void initMediaFileHandlers() {
        
        mediaFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        
        addMediaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) { 
                mediaFileDialog.center();
            }
        });
        
        noMediaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                /* Create a callback to handle the removal once the user has been prompted (if needed) */
                DeleteRemoveCancelCallback removeCallback = new DeleteRemoveCancelCallback() {
                    
                    @Override
                    public void cancel() {
                        
                    }

                    @Override
                    public void delete() {
                        
                      String username = GatClientUtility.getUserName();
                      String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                      List<String> filesToDelete = new ArrayList<String>();
                      String currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
                      final String filePath = currentCoursePath + "/" + mediaFileLabel.getText();
                      filesToDelete.add(filePath);
                      
                      DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                      SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                          @Override
                          public void onFailure(Throwable error) {
                              ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                      "Failed to delete the file.", 
                                      error.getMessage(), 
                                      DetailedException.getFullStackTrace(error));
                              dialog.setDialogTitle("Deletion Failed");
                              dialog.center();
                          }

                          @Override
                          public void onSuccess(GatServiceResult result) {
                              
                              if(result.isSuccess()){
                                  logger.warning("Successfully deleted the file '"+filePath+"'.");
                              } else{
                                  logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                              }
                              
                              resetUI();
                          }
                          
                      });
                    }

                    @Override
                    public void remove() {
                        resetUI();
                    }
                    
                    private void resetUI(){  
                        
                        QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
                        props.getProperties().removeProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
                        props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, false);
                        
                        mediaFileLabel.setText("Select Media");
                        
                        displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(addMediaPanel));
                        
                        propListener.onPropertySetChange(propSet);
                    }

                };
                
                /* Check if a legacy image is being used */
                QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
                
                Serializable displayImage = props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
                
                if(displayImage != null && displayImage instanceof Boolean){
                    
                    if((Boolean) displayImage){
                        
                        if(props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY) == null) {
                            
                            /* If a legacy image is being used, we don't need to ask the user if they want to delete it */
                            removeCallback.remove();
                            return;
                        }
                    }
                }

                /* Non-legacy media is being used, so prompt the user asking if they want to delete the media file */
                DeleteRemoveCancelDialog.show("Delete Content", 
                        "Do you wish to <b>permanently delete</b> '"+mediaFileLabel.getText()+
                        "' from the course or simply remove the reference to that content in this survey?<br><br>"+
                                "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                        removeCallback, 
                        "Delete Content");
            }
        });

        
        mediaFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                String fileName = event.getValue();
                
                QuestionImagePropertySet props = (QuestionImagePropertySet) propSet;
                
                boolean showMedia = fileName != null;
                props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, showMedia);
                
                props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, fileName);
                    
                if(showMedia){
                    mediaFileLabel.setText(event.getValue());
                    
                    props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY, true);
                    
                    sizePanel.setVisible(Constants.isVideoFile(fileName) || Constants.isImageFile(fileName));
                    
                    displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(mediaEditorPanel));
                    
                } else {
                    
                    props.getProperties().removeProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
                    props.getProperties().removeProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
                    
                    mediaFileLabel.setText("Select a file");
                    
                    displayImageCollapse.showWidget(displayImageCollapse.getWidgetIndex(addMediaButton));
                }
                
                propListener.onPropertySetChange(propSet);
            }
        });
    }
}
