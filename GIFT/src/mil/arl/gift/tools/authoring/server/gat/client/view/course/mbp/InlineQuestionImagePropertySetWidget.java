/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.LoadedMetadataEvent;
import com.google.gwt.event.dom.client.LoadedMetadataHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The QuestionImagePropertySetWidget is responsible for displaying the controls that
 * allow the author to add a custom image to the question text.
 * 
 * @author nblomberg
 *
 */
public class InlineQuestionImagePropertySetWidget extends Composite {

    private static Logger logger = Logger.getLogger(InlineQuestionImagePropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
	
	private static int instanceNumber = 0;

	interface WidgetUiBinder extends
			UiBinder<Widget, InlineQuestionImagePropertySetWidget> {
	}
	
	@UiField
	protected FocusPanel mainFocus;
	
	@UiField
	protected DeckPanel displayImageDeck;
	
	@UiField
	protected Widget placeholderContainer;
	
	@UiField
	protected DeckPanel placeholderPanel;
	
	@UiField
	protected Widget placeholder;
	
	@UiField
	protected SimplePanel mediaPanel;
	
	@UiField
	protected Widget propertiesPanel;
	
	@UiField
	protected Label mediaFileLabel;
	
	@UiField(provided=true)
	protected Radio aboveQuestionRadio;
	
	@UiField(provided=true)
	protected Radio belowQuestionRadio;
	
	@UiField
	protected FlowPanel sizePanel;
	
	@UiField(provided=true)
	protected NumberSpinner widthBox = new NumberSpinner(0, 0, 100);
	
	@UiField
	protected Button newMediaButton;
	
	@UiField
	protected Button closeButton;
	
    /** The dialog used to select media files to place into the course folder associated with the survey */
    private DefaultGatFileSelectionDialog mediaFileDialog = new DefaultGatFileSelectionDialog();
	
	@UiHandler("newMediaButton")
    void onUploadButtonClicked(ClickEvent event) {
	    mediaFileDialog.center();
    }
	
	private boolean isMouseOver = false;	
	
	/** The property set for the widget. */
	protected QuestionImagePropertySet propSet = null;
	
	/** The listener that should be called to notify of property changes. */
	protected PropertySetListener propListener = null;
	/**
	 * Constructor (default)
	 * @param propertySet - The property set for the widget.
	 * @param listener - The listener that can be used to handle changes in the properties.
	 */
    public InlineQuestionImagePropertySetWidget(PropertySetListener listener) {
        if(logger.isLoggable(Level.INFO)){
            logger.info("constructor()");
        }
	    
	    this.propListener = listener;
	    
	    // This must be done before initWidget() is called.
        mediaFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        mediaFileDialog.setText("Select media to add");
        
        aboveQuestionRadio  = new Radio("InlineQuestionRadio - " + instanceNumber);
        belowQuestionRadio  = new Radio("InlineQuestionRadio - " + instanceNumber);
        
        instanceNumber++;
        
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    mainFocus.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent arg0) {
				isMouseOver = true;
			}
		});
	    
	    mainFocus.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {				
                isMouseOver = false;
                mainFocus.setFocus(true);
			}
		});
	    
	    mainFocus.addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent arg0) {
				
				if(!isMouseOver){
				    logger.info("mainFocus-onBlur: showing placeholder container");
					displayImageDeck.showWidget(displayImageDeck.getWidgetIndex(placeholderContainer));
				}
			}
		});
	    
	    logger.info("cstr - Showing placeholder container");
	    displayImageDeck.showWidget(displayImageDeck.getWidgetIndex(placeholderContainer));
	    
	    placeholderPanel.showWidget(placeholderPanel.getWidgetIndex(placeholder));
	    
	    placeholderPanel.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				
				if(displayImageDeck.getVisibleWidget() == displayImageDeck.getWidgetIndex(placeholderContainer)){	
				    logger.info("Changing display to show properties panel of the media");
					displayImageDeck.showWidget(displayImageDeck.getWidgetIndex(propertiesPanel));
				}
			}
		}, MouseDownEvent.getType());
	    
	    closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
			    logger.info("CloseButton-onClick: Showing placeholder container");
				displayImageDeck.showWidget(displayImageDeck.getWidgetIndex(placeholderContainer));
			}
		});
	    
	    belowQuestionRadio.setValue(true);
	    
	    belowQuestionRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
				
					propSet.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, 0);
					
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    aboveQuestionRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){					
				
					propSet.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, 1);
					
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    widthBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				
				if(event.getValue() != null){
										
					Integer width = event.getValue();
				
					propSet.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, width);
					
				} else {
					
				    propSet.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, QuestionImagePropertySet.DEFAULT_WIDTH);
				}			
				
				propListener.onPropertySetChange(propSet);
			}
		});

	    mediaFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if (event.getValue() != null) {
                    String fileName = event.getValue();
                    
                    // whether media was selected
                    propSet.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, true);
                    
                    // the reference to the media
                    propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, fileName);
                    
                    // indicates this is not a legacy image reference but a newer media reference
                    propSet.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY, true);
                    
                    populateMediaPanel();

                    mediaFileLabel.setText(fileName);
                    
                    placeholderPanel.showWidget(placeholderPanel.getWidgetIndex(mediaPanel));

                }
            }
            
        });
	    
	}
    
    /**
     * Used to re-show this widget in a state that is reflective on the current properties (which should
     * be the default property values because the previous widget state should be not shown / not used).
     * This method will set the appropriate property (SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) to true
     * to indicate the question media is desired but still needs additional properties to be fully defined.
     */
    public void onDisplayWidget(){
        
        if(propSet == null){
            return;
        }
        
        //change the survey question property value for question media 
        propSet.getProperties().setBooleanPropertyValue(
                SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, 
                true
        );
        
        // populate the widget based on the lastest property values
        edit(propSet);
    }
    
    /**
     * Resets the property values that can be set by this widget.  This is useful when trying to remove
     * the inline question media from a question.  
     * Note: property values are changed to their defaults because SurveyItemProperties.copyInto is used which
     * doesn't consider removal of properties only the current properties.
     */
    public void clearWidgetProperties(){
        
        if(propSet == null){
            logger.info("Unable to find property object");
            return;
        }
                
        // turn off media 
        propSet.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, false);
        
        // remove the reference to the media
        propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, null);
        
        // indicates this is not a legacy image reference but a newer media reference
        propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY, null);
        
        propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, null);
        
        propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, null);
        
        notifyPropertySetChanged();
        
        logger.info("Finished removing question media properties");
    }
    
    /**
     * Populate the appropriate widget based on the media type found and add it to the appropriate
     * media panel for display.
     */
    private void populateMediaPanel(){
        
        logger.info("populateMediaPanel - start");
        
        Boolean displayMedia = null;
        
        if(propSet.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) != null){
            displayMedia = propSet.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
        }
        
        final String mediaLocation;
        
        if(propSet.getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) && propSet.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) instanceof String){
            mediaLocation = (String) propSet.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
            
        } else {
            mediaLocation = null;
        }
        
        Integer mediaPosition = null;
        
        if(propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY) != null){
            mediaPosition = propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
        }
        
        final Integer mediaWidth;
        
        if(propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY) != null){
            mediaWidth = propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY);
        } else {
            mediaWidth = 0;
        }
        
        boolean isLegacyImage = !propSet.getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Generating question media panel");
        }
        
        if(displayMedia == null || !displayMedia || mediaLocation == null || mediaLocation.isEmpty()) {            
            /* No media to show */
            return;
        }
        
        final Widget media;
        
        if(Constants.isVideoFile(mediaLocation)) {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question video - "+mediaLocation);
            }
            
            /* Display a video */
            final Video video = Video.createIfSupported();
            video.setPreload(MediaElement.PRELOAD_AUTO);
            video.setControls(true);
            
            media = video;

            video.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth);
                }
            });
            
            video.setSrc(GatClientUtility.getBaseCourseFolderUrl() + "/" + mediaLocation);
            
            logger.info("finished generating question video widget - "+video.getSrc());
            
        } else if(Constants.isAudioFile(mediaLocation)) {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question audio - "+mediaLocation);
            }
            
            /* Display an audio file */
            final Audio audio = Audio.createIfSupported();
            audio.setPreload(MediaElement.PRELOAD_AUTO);
            audio.setControls(true);
            
            media = audio;
            
            audio.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth);
                }
            });
            
            audio.setSrc(GatClientUtility.getBaseCourseFolderUrl() + "/" + mediaLocation);
            
            logger.info("finished generating question audio widget - "+audio.getSrc());
            
        } else if(isLegacyImage || Constants.isImageFile(mediaLocation)){
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question image - "+mediaLocation);
            }
            
            String prefix;
            if(isLegacyImage) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Legacy question image detected. Modifying media URL appropriately");
                }
                
                /* Legacy image reference. Assume URL is relative to data folder. */
                prefix = "";
                
            } else {
                prefix = GatClientUtility.getBaseCourseFolderUrl() + "/";
            }
            
            /* Display either a legacy image or an image file */
            final Image image = new Image(prefix + mediaLocation);
            
            media = image;

            image.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    resizeMedia(mediaWidth);
                }
            });
            
            logger.info("finished building question image widget - "+image.getUrl());
            
        } else {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question unknown media");
            }
            
            /* Display a button that can be used to view a media file with an unknown type */
            final Button button = new Button("Click to View");
            button.setType(ButtonType.PRIMARY);
            button.setIcon(IconType.FILE);
            button.setIconSize(IconSize.TIMES2);
            button.getElement().getStyle().setProperty("padding", "10px 80px");
            button.getElement().getStyle().setProperty("display", "flex");
            button.getElement().getStyle().setProperty("flexDirection", "column");
            button.getElement().getStyle().setProperty("alignItems", "center");
            button.getElement().getStyle().setProperty("fontSize", "20px");
            
            final Tooltip tooltip = new Tooltip("Click to show this media in a new window");
            tooltip.setWidget(button);
            
            media = button;
            
            button.addMouseDownHandler(new MouseDownHandler() {
                
                @Override
                public void onMouseDown(MouseDownEvent event) {
                    event.stopPropagation();
                }
            });
            
            button.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                    tooltip.hide();
                    String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                    Window.open(
                            GatClientUtility.getBaseCourseFolderUrl() + "/" + mediaLocation, 
                            "_blank", 
                            options);
                }
            });
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Question media generated.");
        }
        
        media.getElement().getStyle().setProperty("margin", "10px auto");
        media.getElement().getStyle().setProperty("display", "flex");
        media.getElement().getStyle().setProperty("maxWidth", "100%");

        mediaPanel.setWidget(media);
        
        mediaPanel.getElement().getStyle().setOverflowX(Overflow.AUTO);
        mediaPanel.getElement().getStyle().setOverflowY(Overflow.VISIBLE);
    
        if(mediaPosition == null){
            mediaPosition = 0;
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Finished generating media panel");
        }
    }
    
    /**
     * Resizes the media associated with this question to reflect the provided size
     * 
     * @param size the size that the rendered media should be shown with (as a percentage
     * of its original size)
     */
    private void resizeMedia(Integer size) {
        
        Widget media = mediaPanel.getWidget();
        if(media == null) {
            return;
        }
        
        float widthPercentage = (size / 100f);

        if (widthPercentage == 0) {

            widthPercentage = 1f;
        }
        
        int width;
        
        /* Attempt to get the natural width of the media being displayed. The property
         * that needs to be looked at to get this will vary slightly depending on
         * what type of HTML element is used */
        String widthProp = media instanceof Video ? "videoWidth" : "naturalWidth";
        String widthAttr = media.getElement().getPropertyString(widthProp);
        try {
            width = Integer.valueOf(widthAttr);
            
        } catch(@SuppressWarnings("unused") NumberFormatException e) {
            
            /* The element is likely still loading, so the natural width is not yet known.
             * If this happens, fall back on the offset width (i.e. the rendered width) */
            width = media.getOffsetWidth();
        }
        
        media.getElement().setAttribute("width", width * widthPercentage + "px");
        media.getElement().setAttribute("height", (widthPercentage * 100) + "%");
    }
    
    /**
     * Populate the widget with the question properties values.
     * @param propertySet the properties to use to populate the widget.  If null this method does nothing.
     */
    public void edit(QuestionImagePropertySet propertySet){
    	
    	this.propSet = propertySet;
    	
    	if(propertySet != null){
    	    
            logger.info("loading question properties");            
            
            populateMediaPanel();
	    	
	    	String location = (String)propertySet.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);

	    	mediaFileLabel.setText(location);
	    	
	    	if(location != null){
	            logger.info("Media location provided, showing media panel");
	    		placeholderPanel.showWidget(placeholderPanel.getWidgetIndex(mediaPanel));
	    		
	    	} else {
	    	    logger.info("Media location not provided, showing placeholder panel");
	    		placeholderPanel.showWidget(placeholderPanel.getWidgetIndex(placeholder));
	    		
	    		// make sure the placeholder container is shown and not the media panel.  The media panel
	    		// could show the media properties panel if that was the last panel set in the displayImageDeck
	    		displayImageDeck.showWidget(displayImageDeck.getWidgetIndex(placeholderContainer)); 
	    	}
	    	
	    	Serializable position = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
	    	
	    	if(position != null && position instanceof Integer){
	    		
	    		if((Integer)position == 0){
	    			belowQuestionRadio.setValue(true);
	    			
	    		} else {
	    			aboveQuestionRadio.setValue(true);
	    		}
	    		
	    	} else {
	    		belowQuestionRadio.setValue(true);
	    	}
	    	
	    	Serializable width = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY);
	    	
	    	if(width != null && width instanceof Integer){
	    		widthBox.setValue((Integer) width);
	    		
	    	} else {
	    		widthBox.setValue(QuestionImagePropertySet.DEFAULT_WIDTH);
	    	}
	    	
	    	/* Don't let the author modify the size of audio or unknown media files, since the size isn't really
             * able to be controlled */
            sizePanel.setVisible(Constants.isVideoFile(location) || Constants.isImageFile(location));
	    	
	    	propListener.onPropertySetChange(propSet);
	    	
	    	logger.info("finished loading question properties");
	    }
    }
    
    /**
     * Notifies the property set listener that the property set has been changed.
     */
    public void notifyPropertySetChanged() {
        propListener.onPropertySetChange(propSet);
    } 
    
    /**
     * The properties for this question image widget
     * @return the property set for this widget
     */
    public AbstractPropertySet getObjectBeingEdited(){
    	return propSet;
    }

}
