/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ImageProperties;
import generated.dkf.Media;
import generated.dkf.PDFProperties;
import generated.dkf.SlideShowProperties;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import generated.dkf.VideoProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An alternate version of the course editor's
 * {@link mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.MediaPanel MediaPanel}
 * designed to be used with the equivalent {@link generated.dkf.Media} objects from the DKF schema.
 *
 * @author nroberts
 */
public class MediaPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(MediaPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static MediaPanelUiBinder uiBinder = GWT.create(MediaPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface MediaPanelUiBinder extends UiBinder<Widget, MediaPanel> {
    }

    /** Deck panel containing the different media widgets */
    @UiField
    protected DeckPanel mediaDeckPanel;

    /** Slide show widget */
    @UiField
    protected SlideShowMediaWidget slideShowWidget;

    /** PDF widget */
    @UiField
    protected PdfMediaWidget pdfWidget;

    /** Local Webpage widget */
    @UiField
    protected LocalWebpageMediaWidget localWebpageWidget;

    /** Image widget */
    @UiField
    protected ImageMediaWidget imageWidget;
    
    /** Video widget */
    @UiField
    protected VideoMediaWidget videoWidget;

    /** Web Address widget */
    @UiField
    protected WebAddressMediaWidget webAddressWidget;

    /** YouTube widget */
    @UiField
    protected YouTubeMediaWidget youTubeWidget;

    ////////////////////////
    // Message
    ////////////////////////

    /** Panel containing the message components */
    @UiField
    protected FlowPanel messagePanel;

    /** Panel that collapses or expands the message panel */
    @UiField
    protected FocusPanel messageButton;

    /** The icon showing if the panel is collapsed or expanded */
    @UiField
    protected Icon messageIcon;

    /** Panel containing the actual text editor */
    @UiField
    protected FlowPanel editorPanel;

    /** The text editor */
    @UiField(provided = true)
    protected Summernote richTextEditor = new Summernote() {
        @Override
        protected void onLoad() {
            super.onLoad();

            // need to reconfigure the editor or else the blur event doesn't fire properly
            richTextEditor.reconfigure();

            // need to reassign the message HTML to the editor since it gets lost when the editor is
            // detached.
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    if (currentMedia != null && currentMedia.getMessage() != null) {
                        richTextEditor.setCode(currentMedia.getMessage());
                    }
                }
            });
        }
    };

    /** The media currently being edited */
    private Media currentMedia = null;

    /** Read only flag */
    private boolean readOnly = false;

    /** Constructor */
    public MediaPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        // set all media widgets to inactive until one is chosen
        slideShowWidget.setActive(false);
        pdfWidget.setActive(false);
        localWebpageWidget.setActive(false);
        imageWidget.setActive(false);
        videoWidget.setActive(false);
        webAddressWidget.setActive(false);
        youTubeWidget.setActive(false);

        Toolbar defaultToolbar = new Toolbar().addGroup(ToolbarButton.STYLE)
                .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC, ToolbarButton.FONT_SIZE)
                .addGroup(ToolbarButton.LINK, ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
                .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);

        richTextEditor.setToolbar(defaultToolbar);
        richTextEditor.reconfigure();
        richTextEditor.addSummernoteBlurHandler(new SummernoteBlurHandler() {
            @Override
            public void onSummernoteBlur(SummernoteBlurEvent event) {
                if (!readOnly && currentMedia != null) {
                    currentMedia.setMessage(richTextEditor.getCode());
                }
            }
        });

        messageButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editorPanel.setVisible(!editorPanel.isVisible());
                if (editorPanel.isVisible()) {
                    messageIcon.setType(IconType.MINUS_SQUARE);
                } else {
                    messageIcon.setType(IconType.PLUS_SQUARE);
                }
            }
        });

        setReadonly(GatClientUtility.isReadOnly());
    }

    /**
     * Populate the panel with the provided {@link Media}.
     *
     * @param media the media to populate the panel. Can't be null. Can't have a null media type
     *        properties.
     */
    public void editLocalWebpage(Media media) {
        editMedia(media);

        // Show the Local Webpage panel
        localWebpageWidget.edit(media);
        showEditor(localWebpageWidget);
    }
    
    /**
     * Populate the panel with the provided {@link Media}.
     *
     * @param media the media to populate the panel. Can't be null. Can't have a null media type
     *        properties.
     */
    public void editWebAddress(Media media) {
        editMedia(media);

        // Show the Web Address panel
        webAddressWidget.edit(media);
        showEditor(webAddressWidget);
    }

    /**
     * Populate the panel with the provided {@link Media}.
     *
     * @param media the media to populate the panel. Can't be null. Can't have a null media type
     *        properties.
     */
    public void editMedia(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (media.getMediaTypeProperties() == null) {
            throw new IllegalArgumentException("The parameter 'media.getMediaTypeProperties()' cannot be null.");
        }

        currentMedia = media;

        messagePanel.setVisible(true);

        if (media.getMessage() != null) {
            richTextEditor.setCode(media.getMessage());
            editorPanel.setVisible(true);

        } else {
            richTextEditor.clear();
            editorPanel.setVisible(false);
        }

        if (editorPanel.isVisible()) {
            messageIcon.setType(IconType.MINUS_SQUARE);
        } else {
            messageIcon.setType(IconType.PLUS_SQUARE);
        }

        final Serializable properties = media.getMediaTypeProperties();
        if (properties instanceof PDFProperties) {
            // Show the PDF panel
            pdfWidget.edit(media);
            showEditor(pdfWidget);

        } else if (properties instanceof YoutubeVideoProperties) {
            // Show the YouTube video panel
            youTubeWidget.edit(media);
            showEditor(youTubeWidget);

        } else if (properties instanceof WebpageProperties) {
            // not handled here, handled by their individual methods.

        } else if (properties instanceof ImageProperties) {
            // Show the Image panel
            imageWidget.edit(media);
            showEditor(imageWidget);
            
        } else if (properties instanceof VideoProperties) {
            // Show the Image panel
            videoWidget.edit(media);
            showEditor(videoWidget);

        } else if (properties instanceof SlideShowProperties) {
            // Show the Slide Show panel
            slideShowWidget.edit(media);
            showEditor(slideShowWidget);
        }
    }

    /**
     * Shows the provided editor to the user. Hides all other editors.
     *
     * @param editorToShow the editor to display.
     */
    private void showEditor(Widget editorToShow) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showEditor(" + editorToShow + ")");
        }

        int visibleWidgetIndex = mediaDeckPanel.getVisibleWidget();
        if (visibleWidgetIndex != -1) {
            Widget visibleWidget = mediaDeckPanel.getWidget(visibleWidgetIndex);

            // trying to show an editor that is already visible
            if (visibleWidget == editorToShow) {
                return;
            }

            if (visibleWidget instanceof ScenarioValidationComposite) {
                ScenarioValidationComposite editor = (ScenarioValidationComposite) visibleWidget;
                editor.clearValidations();
                editor.setActive(false);
            }
        }

        int widgetIndex = mediaDeckPanel.getWidgetIndex(editorToShow);
        if (widgetIndex != -1) {
            mediaDeckPanel.showWidget(widgetIndex);
            if (editorToShow instanceof ScenarioValidationComposite) {
                ScenarioValidationComposite editor = (ScenarioValidationComposite) editorToShow;
                editor.setActive(true);
                editor.validateAll();
            }
        } else {
            logger.severe("Could not show editor '" + editorToShow + "' because it could not be found.");
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(slideShowWidget);
        childValidationComposites.add(pdfWidget);
        childValidationComposites.add(youTubeWidget);
        childValidationComposites.add(localWebpageWidget);
        childValidationComposites.add(webAddressWidget);
        childValidationComposites.add(imageWidget);
        childValidationComposites.add(videoWidget);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation composites
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // all validation is handled within the individual widgets
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        this.readOnly = isReadonly;

        slideShowWidget.setReadonly(isReadonly);
        pdfWidget.setReadonly(isReadonly);
        youTubeWidget.setReadonly(isReadonly);
        localWebpageWidget.setReadonly(isReadonly);
        webAddressWidget.setReadonly(isReadonly);
        imageWidget.setReadonly(isReadonly);
        videoWidget.setReadonly(isReadonly);
        richTextEditor.setEnabled(!isReadonly);
    }
}
