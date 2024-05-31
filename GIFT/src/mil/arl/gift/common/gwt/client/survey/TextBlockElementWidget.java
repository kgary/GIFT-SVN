/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.LoadedMetadataEvent;
import com.google.gwt.event.dom.client.LoadedMetadataHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.TextSurveyElement;

/**
 * A widget for rendering a text survey element
 *
 * @author jleonard
 */
public class TextBlockElementWidget extends AbstractSurveyElementWidget<TextSurveyElement> {
    
    /**
     * Constructor
     * 
     * @param surveyProperties The properties of the survey the element is in
     * @param surveyElement The text survey element to make a widget for
     * @param isPreview If the survey element is being previewed
     */
    public TextBlockElementWidget(SurveyProperties surveyProperties, TextSurveyElement surveyElement, boolean isPreview) {
        super(surveyProperties, surveyElement, isPreview);
        
        FlowPanel containerPanel = new FlowPanel();
        
        containerPanel.setStyleName(SurveyCssStyles.SURVEY_TEXT_BLOCK);

        String displayedText = surveyElement.getText();

        String imagePath = (String) surveyElement.getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
        final Widget image = imagePath != null ? generateMedia() : null;
        final int imagePosition = surveyElement.getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, 1);
        
        //set image above text
        if(image != null && imagePosition == 1) {
            containerPanel.add(image);
        }
        
        if (displayedText != null) {

            //format the string for GWT HTML
            String displayStr = DocumentUtil.unescapeHTML(displayedText);
            HTML displayedHtml = new HTML(displayStr);            
            containerPanel.add(displayedHtml); 
        }
        
        //set image below text
        if(imagePath != null && imagePosition == 0) {
            containerPanel.add(image);
        }
        
        addWidget(containerPanel);   
    }
    
    /**
     * Generates the media for this survey element
     *
     * @return Widget the generated media. Will not be null. 
     */
    private Widget generateMedia() {
        
        final Integer mediaWidth = getSurveyElement().getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, 100);
        final String mediaLocation = (String) getSurveyElement().getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
        boolean isLegacyImage = !getSurveyElement().getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
        
        final Widget media;
        
        if(Constants.isVideoFile(mediaLocation)) {
            
            /* Display a video */
            final Video video = Video.createIfSupported();
            video.setPreload(MediaElement.PRELOAD_AUTO);
            video.setControls(true);
            
            media = video;

            video.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
            video.setSrc(mediaLocation);
            
        } else if(Constants.isAudioFile(mediaLocation)) {
            
            /* Display an audio file */
            final Audio audio = Audio.createIfSupported();
            audio.setPreload(MediaElement.PRELOAD_AUTO);
            audio.setControls(true);
            
            media = audio;
            
            audio.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
            audio.setSrc(mediaLocation);
            
        } else if(isLegacyImage || Constants.isImageFile(mediaLocation)){
            
            /* Display either a legacy image or an image file */
            final Image image = new Image(mediaLocation);
            
            media = image;

            image.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
        } else {
                
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
                    tooltip.hide();
                    String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                    Window.open(
                            mediaLocation, 
                            "_blank", 
                            options);
                }
            });
        }
        
        media.getElement().getStyle().setProperty("margin", "10px auto");
        media.getElement().getStyle().setProperty("display", "flex");
        media.getElement().getStyle().setProperty("maxWidth", "100%");

        FlowPanel mediaContainer = new FlowPanel();
        mediaContainer.add(media);
        
        mediaContainer.getElement().getStyle().setOverflowX(Overflow.AUTO);
        mediaContainer.getElement().getStyle().setOverflowY(Overflow.VISIBLE);

        return mediaContainer;
    }
    
    /**
     * Resizes the media associated with this question to reflect the provided size
     * 
     * @param size the size that the rendered media should be shown with (as a percentage
     * of its original size)
     * @param media the media to resize. If null, no resizing will be performed.
     */
    private void resizeMedia(Integer size, Widget media) {
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
}
