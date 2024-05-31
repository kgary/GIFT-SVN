/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ImageProperties;
import generated.dkf.Media;
import generated.dkf.PDFProperties;
import generated.dkf.Size;
import generated.dkf.SlideShowProperties;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import generated.dkf.VideoProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A panel used to add and edit lesson material media. This panel is meant to act as the DKF editor
 * equivalent of the Course editor's
 * {@link mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.AddMediaDialog
 * AddMediaDialog}.
 * 
 * @author nroberts
 */
public class AddMediaPanel extends ItemEditor<Media> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static AddMediaPanelUiBinder uiBinder = GWT.create(AddMediaPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AddMediaPanelUiBinder extends UiBinder<Widget, AddMediaPanel> {
    }

    /** The deck to toggle between the panels */
    @UiField
    protected DeckPanel mainDeck;

    /** Ribbon for selecting the media type */
    @UiField
    protected Ribbon choiceRibbon;

    /** The media panel that contains the different media content */
    @UiField
    protected MediaPanel mediaPanel;

    /** Allows the user to change the type of media */
    @UiField
    protected Button changeTypeButton;

    /**
     * A dummy media item being edited. This should NOT be a loaded schema object but should instead
     * be a deep copy.
     */
    private Media editMedia = null;

    /**
     * Creates a new dialog for adding and editing media
     */
    public AddMediaPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        choiceRibbon.setTileHeight(105);

        // Slide Show
        Image slideShowImage = new Image("images/slideshow_icon.png");
        slideShowImage.setSize("24px", "24px");
        choiceRibbon.addRibbonItem(slideShowImage, "Slide Show", "Select this content type to display a slide show.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new SlideShowProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editMedia(media);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                        mediaPanel.validateAll();
                    }
                });

        // PDF
        choiceRibbon.addRibbonItem(IconType.FILE_PDF_O, "PDF", "Select this content type to display a PDF file.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new PDFProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editMedia(media);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                        mediaPanel.validateAll();
                    }
                });

        // Local webpage
        choiceRibbon.addRibbonItem(IconType.FILE, "Local Webpage",
                "Select this content type to display a local web page.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new WebpageProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editLocalWebpage(editMedia);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                    }
                });

        // Video
        choiceRibbon.addRibbonItem(IconType.FILE_VIDEO_O, "Local Video", 
                "Select this content type to display a local video file.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new VideoProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editMedia(media);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                    }
                });
        
        // Image
        Icon imageIcon = new Icon(IconType.IMAGE);
        imageIcon.setColor("rgb(100,100,100)");
        choiceRibbon.addRibbonItem(imageIcon, "Local Image", "Select this content type to display a local image file.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new ImageProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editMedia(media);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                    }
                });

        // Website
        Icon websiteIcon = new Icon(IconType.GLOBE);
        websiteIcon.setColor("darkblue");
        choiceRibbon.addRibbonItem(websiteIcon, "Web Address",
                "Select this content type to display a resource using a web address.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new WebpageProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editWebAddress(editMedia);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                    }
                });

        // Youtube
        Icon youtubeIcon = new Icon(IconType.YOUTUBE_PLAY);
        youtubeIcon.setColor("red");
        choiceRibbon.addRibbonItem(youtubeIcon, "YouTube Video", "Select this content type to display a YouTube video.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {

                        Media media = new Media();
                        media.setMediaTypeProperties(new YoutubeVideoProperties());

                        if (editMedia != null) {
                            media.setName(editMedia.getName());
                        }

                        editMedia = media;

                        mediaPanel.editMedia(media);
                        showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
                    }
                });

        changeTypeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {

                Media media = new Media();

                if (editMedia != null) {
                    media.setName(editMedia.getName());
                }

                editMedia = media;
                showDeckWidget(mainDeck.getWidgetIndex(choiceRibbon));
            }
        });

        changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
    }

    /**
     * Shows the provided deck widget and shows/hides the save button accordingly.
     * 
     * @param deckWidget the deck widget index.
     */
    private void showDeckWidget(int deckWidget) {
        mainDeck.showWidget(deckWidget);

        boolean isChoicePanelVisible = deckWidget == mainDeck.getWidgetIndex(choiceRibbon);
        setSaveButtonVisible(!isChoicePanelVisible);
        changeTypeButton.setVisible(!GatClientUtility.isReadOnly() && !isChoicePanelVisible);
    }

    @Override
    protected void populateEditor(Media existingMedia) {

        editMedia = new Media();

        // Checks if editing existing media
        if (existingMedia != null) {

            // create a copy of the given media item so that changes can be cancelled
            editMedia.setName(existingMedia.getName());
            editMedia.setMessage(existingMedia.getMessage());
            editMedia.setUri(existingMedia.getUri());
            editMedia.setDisplaySessionProperties(existingMedia.getDisplaySessionProperties());

            if (existingMedia.getMediaTypeProperties() != null) {

                if (existingMedia.getMediaTypeProperties() instanceof WebpageProperties) {
                    editMedia.setMediaTypeProperties(new WebpageProperties());

                } else if (existingMedia.getMediaTypeProperties() instanceof ImageProperties) {
                    editMedia.setMediaTypeProperties(new ImageProperties());
                    
                } else if (existingMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) existingMedia.getMediaTypeProperties();
                    VideoProperties copyProperties = new VideoProperties();
                    
                    copyProperties.setAllowAutoPlay(properties.getAllowAutoPlay());
                    copyProperties.setAllowFullScreen(properties.getAllowFullScreen());
                    
                    if (properties.getSize() != null) {
                        Size copySize = new Size();
                        
                        copySize.setHeight(properties.getSize().getHeight());
                        copySize.setWidth(properties.getSize().getWidth());
                        
                        copySize.setHeightUnits(properties.getSize().getHeightUnits());
                        copySize.setWidthUnits(properties.getSize().getWidthUnits());
                        
                        copySize.setConstrainToScreen(properties.getSize().getConstrainToScreen());
                        
                        copyProperties.setSize(copySize);
                    }
                    
                    editMedia.setMediaTypeProperties(copyProperties);

                } else if (existingMedia.getMediaTypeProperties() instanceof PDFProperties) {
                    editMedia.setMediaTypeProperties(new PDFProperties());

                } else if (existingMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) existingMedia.getMediaTypeProperties();
                    YoutubeVideoProperties copyProperties = new YoutubeVideoProperties();

                    copyProperties.setAllowAutoPlay(properties.getAllowAutoPlay());
                    copyProperties.setAllowFullScreen(properties.getAllowFullScreen());

                    if (properties.getSize() != null) {
                        Size copySize = new Size();

                        copySize.setHeight(properties.getSize().getHeight());
                        copySize.setWidth(properties.getSize().getWidth());

                        copySize.setHeightUnits(properties.getSize().getHeightUnits());
                        copySize.setWidthUnits(properties.getSize().getWidthUnits());

                        copySize.setConstrainToScreen(properties.getSize().getConstrainToScreen());

                        copyProperties.setSize(copySize);
                    }

                    editMedia.setMediaTypeProperties(copyProperties);

                } else if (existingMedia.getMediaTypeProperties() instanceof SlideShowProperties) {
                    SlideShowProperties properties = (SlideShowProperties) existingMedia.getMediaTypeProperties();
                    SlideShowProperties copyProperties = new SlideShowProperties();

                    copyProperties.setDisplayPreviousSlideButton(properties.getDisplayPreviousSlideButton());

                    copyProperties.setKeepContinueButton(properties.getKeepContinueButton());

                    copyProperties.getSlideRelativePath().addAll(properties.getSlideRelativePath());

                    editMedia.setMediaTypeProperties(copyProperties);
                }
            }
        }

        if (editMedia.getMediaTypeProperties() instanceof WebpageProperties) {
            if (CourseElementUtil.isWebAddress(editMedia)) {
                mediaPanel.editWebAddress(editMedia);
            } else {
                mediaPanel.editLocalWebpage(editMedia);
            }
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));

        } else if (editMedia.getMediaTypeProperties() instanceof ImageProperties) {
            mediaPanel.editMedia(editMedia);
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));
            
        } else if (editMedia.getMediaTypeProperties() instanceof VideoProperties) {
            mediaPanel.editMedia(editMedia);
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));

        } else if (editMedia.getMediaTypeProperties() instanceof PDFProperties) {
            mediaPanel.editMedia(editMedia);
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));

        } else if (editMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
            mediaPanel.editMedia(editMedia);
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));

        } else if (editMedia.getMediaTypeProperties() instanceof SlideShowProperties) {
            mediaPanel.editMedia(editMedia);
            showDeckWidget(mainDeck.getWidgetIndex(mediaPanel));

        } else {
            showDeckWidget(mainDeck.getWidgetIndex(choiceRibbon));
        }

        // if the media panel is visible, perform validation on its contents.
        if (mainDeck.getVisibleWidget() != mainDeck.getWidgetIndex(choiceRibbon)) {
            mediaPanel.validateAll();
        }
    }

    @Override
    protected void applyEdits(Media obj) {
        if (editMedia == null) {
            return;
        }

        obj.setMediaTypeProperties(editMedia.getMediaTypeProperties());
        obj.setMessage(editMedia.getMessage());
        obj.setName(editMedia.getName());
        obj.setUri(editMedia.getUri());
        obj.setDisplaySessionProperties(editMedia.getDisplaySessionProperties());
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // No validations. It's all handled by MediaPanel
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // Nothing to validate. It's all handled by MediaPanel
    }

    @Override
    protected boolean validate(Media media) {
        String errorMsg = ScenarioValidatorUtility.validateMedia(media);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(mediaPanel);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        choiceRibbon.setReadonly(isReadonly);
        changeTypeButton.setEnabled(!isReadonly);
    }
}
