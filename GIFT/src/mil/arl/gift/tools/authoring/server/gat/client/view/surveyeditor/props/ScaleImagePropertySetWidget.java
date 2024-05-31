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
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.gwt.client.widgets.file.BsFileSelectionModal;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyImageInterface;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyImageUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * The ScaleImagePropertySetWidget is responsible for displaying the controls that
 * allow for a custom image to be displayed with the rating scale answer.
 * 
 * @author nblomberg
 *
 */
public class ScaleImagePropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(ScaleImagePropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, ScaleImagePropertySetWidget> {
	}
	
	@UiField
	protected CheckBox displayImageBox;
	
	@UiField
	protected Collapse displayImageCollapse;
	
	@UiField
	protected ListBox locationBox;
	
	@UiField
	protected TextBox widthBox;
	
	@UiField
    protected Button uploadButton;
	
	@UiField(provided = true)
    BsFileSelectionModal fileUploadModal;
	
	@UiHandler("uploadButton")
	void onUploadButtonClicked(ClickEvent event) {
	    fileUploadModal.show();
	}
	
	/** The last width value that the was set for the image. */
	private Integer lastWidthValue = QuestionImagePropertySet.DEFAULT_WIDTH;
	
	/** The imagesList contains the list of images from the server. It must be kept in sync (including order) with the listBox contents. */
    private ArrayList<String> imagesList = new ArrayList<String>();
    
    /**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
	
	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The property set for the widget.
	 * @param listener - The listener that will handle changes to the properties.
	 */
    public ScaleImagePropertySetWidget(ScaleImagePropertySet propertySet, PropertySetListener listener, SurveyImageInterface imageInterface) {
	    super(propertySet, listener);

        if(logger.isLoggable(Level.INFO)){
            logger.info("constructor()");
        }
	    
	    // This must be done before initWidget() is called.
        fileUploadModal = new BsFileSelectionModal(GwtSurveySystemProperties.SURVEY_IMAGE_UPLOAD_URL, DefaultMessageDisplay.includeAllMessages);
        
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    displayImageBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				ScaleImagePropertySet props = (ScaleImagePropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE, event.getValue());
				
				if(event.getValue()){
					displayImageCollapse.show();
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY, locationBox.getSelectedValue());
					
				} else {
					displayImageCollapse.hide();
					props.getProperties().removeProperty(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
				}
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    locationBox.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("scale image changed: " + locationBox.getSelectedValue());
                }
                
                ScaleImagePropertySet props = (ScaleImagePropertySet) propSet;
                props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY, locationBox.getSelectedValue());
                
                propListener.onPropertySetChange(propSet);
            }
        });
	    
	    widthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				ScaleImagePropertySet props = (ScaleImagePropertySet) propSet;
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					
					try{
						
						Integer width = Integer.valueOf(event.getValue());
					
						props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY, width);
						lastWidthValue = width;
						
					} catch(@SuppressWarnings("unused") Exception e){
						
						WarningDialog.error("Invalid value", "Please enter a number for the scale image width or leave the field blank.");
						
						widthBox.setValue(lastWidthValue != null ? lastWidthValue.toString() : QuestionImagePropertySet.DEFAULT_WIDTH.toString()); //reset the width box
						
						return;
					}
					
				} else {
					
					props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY, QuestionImagePropertySet.DEFAULT_WIDTH);
					lastWidthValue = QuestionImagePropertySet.DEFAULT_WIDTH;
				}			
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    

	    if(propertySet != null){
	    	
	    	Serializable displayImage = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE);
	    	
	    	if(displayImage != null && displayImage instanceof Boolean){
	    		
	    		displayImageBox.setValue((Boolean) displayImage);
	    		
	    		if((Boolean) displayImage){
					displayImageCollapse.show();
					
				} else {
					displayImageCollapse.hide();
				}
	    		
	    	} else {
	    		displayImageBox.setValue(null);
	    		displayImageCollapse.hide();
	    	}
	    	
	    	String location = (String)propertySet.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
            if(logger.isLoggable(Level.INFO)){
                logger.info("Populating drop down scale image list with selection: " + location);
            }
            populateSurveyImagesList(imageInterface.getSurveyImageList(), location);
	    	
	    	
	    	Serializable width = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY);
	    	
	    	if(width != null && width instanceof Integer){
	    		widthBox.setValue(((Integer) width).toString());
	    		
	    	} else {
	    		widthBox.setValue(null);
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
                            logger.severe("Failure to retrieve the latest list of scale images from the server.");
                        }

                        @Override
                        public void onSuccess(List<String> result) {
                            if (result != null) {
                                populateSurveyImagesList(result, browsedImage);
                            } else {
                                logger.severe("Server returned a null result when fetching the lastest list of scale images from the server.");
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
        ScaleImagePropertySet props = (ScaleImagePropertySet) propSet;
        if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE)){
        	props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY, locationBox.getSelectedValue());
        }
        propListener.onPropertySetChange(propSet); 
    }

}
