/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.Media;
import generated.dkf.Size;
import generated.dkf.YoutubeVideoProperties;
import mil.arl.gift.common.enums.VideoCssUnitsEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * Allows creating or editing a YouTube media item.
 * 
 * @author sharrison
 */
public class YouTubeMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(YouTubeMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static YouTubeMediaWidgetWidgetUiBinder uiBinder = GWT.create(YouTubeMediaWidgetWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface YouTubeMediaWidgetWidgetUiBinder extends UiBinder<Widget, YouTubeMediaWidget> {
    }

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The video URL text box */
    @UiField
    protected TextBox videoTextBox;

    /** Indicates if the author wants to check the video size */
    @UiField
    protected CheckBox videoSizeCheck;

    /** The panel containing the video size components */
    @UiField
    protected Widget videoSizePanel;

    /** The video width text box */
    @UiField
    protected TextBox videoWidthBox;

    /** The list of units for the video width */
    @UiField
    protected ListBox videoUnitWidth;

    /** The video height text box */
    @UiField
    protected TextBox videoHeightBox;

    /** The list of units for the video height */
    @UiField
    protected ListBox videoUnitHeight;

    /** Indicates if the author wants to constrain the video size to the screen */
    @UiField
    protected CheckBox constrainToScreenCheck;

    /** Indicates if the author wants to enable the full screen button */
    @UiField
    protected CheckBox videoFullScreenCheck;

    /** Indicates if the author wants the video to start playing automatically */
    @UiField
    protected CheckBox videoAutoPlayCheck;

    /** The media that is currently being edited */
    private Media currentMedia;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle entering a YouTube URL */
    private final WidgetValidationStatus youtubeUrlValidation;

    /** Validation used to handle entering the width of a video */
    private final WidgetValidationStatus youtubeWidthValidation;

    /** Validation used to handle entering the height of a video */
    private final WidgetValidationStatus youtubeHeightValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public YouTubeMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");

        youtubeUrlValidation = new WidgetValidationStatus(videoTextBox,
                "No video URL has been entered. Enter the URL of the YouTube video that should be presented to the learner.");

        youtubeWidthValidation = new WidgetValidationStatus(videoWidthBox,
                "The width of a YouTube video must be greater than 0. Specify the width that this video should be presented with.");

        youtubeHeightValidation = new WidgetValidationStatus(videoHeightBox,
                "The height of a YouTube video must be greater than 0. Specify the height that this video should be presented with.");

        mediaTitleTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                currentMedia.setName(newName == null ? null : newName.trim());
                requestValidation(nameValidation);
            }
        });

        initYouTube();
    }

    /** Initializes the panel */
    private void initYouTube() {

        // Populate the drop down boxes to select the custom size units for height & width.
        videoUnitWidth.clear();
        videoUnitHeight.clear();
        for (VideoCssUnitsEnum unitEnum : VideoCssUnitsEnum.VALUES()) {
            videoUnitWidth.addItem(unitEnum.getDisplayName(), unitEnum.getName());
            videoUnitHeight.addItem(unitEnum.getDisplayName(), unitEnum.getName());
        }

        videoUnitWidth.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent arg0) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties != null && properties.getSize() != null) {
                        String units = videoUnitWidth.getSelectedValue();
                        properties.getSize().setWidthUnits(units);
                        requestValidation(youtubeWidthValidation);
                    }
                }
            }
        });

        videoUnitHeight.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent arg0) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties != null && properties.getSize() != null) {
                        String units = videoUnitHeight.getSelectedValue();
                        properties.getSize().setHeightUnits(units);
                        requestValidation(youtubeHeightValidation);
                    }
                }
            }
        });

        videoSizeCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (event.getValue()) {
                        setDefaultYoutubeVideoPropertiesSize(properties);
                    } else {
                        properties.setSize(null);
                    }
                    videoSizePanel.setVisible(event.getValue());
                }
            }
        });

        videoTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (currentMedia != null) {
                    String value = StringUtils.isBlank(event.getValue()) ? null : event.getValue().trim();
                    currentMedia.setUri(value);
                    requestValidation(youtubeUrlValidation);
                }
            }
        });

        videoWidthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() == null) {
                        setDefaultYoutubeVideoPropertiesSize(properties);
                    }

                    try {
                        properties.getSize().setWidth(
                                StringUtils.isBlank(event.getValue()) ? null : new BigDecimal(event.getValue()));
                    } catch (@SuppressWarnings("unused") NumberFormatException e) {
                        videoWidthBox.setValue(null);
                        WarningDialog.error("Width Error", "Please enter a numeric decimal or integer value.");
                    }
                }
            }
        });

        videoHeightBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() == null) {
                        setDefaultYoutubeVideoPropertiesSize(properties);
                    }

                    try {
                        properties.getSize().setHeight(
                                StringUtils.isBlank(event.getValue()) ? null : new BigDecimal(event.getValue()));
                    } catch (@SuppressWarnings("unused") NumberFormatException e) {
                        videoHeightBox.setValue(null);
                        WarningDialog.error("Height Error", "Please enter a numeric decimal or integer value.");
                    }
                }
            }
        });

        videoFullScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    properties.setAllowFullScreen(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });

        videoAutoPlayCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    properties.setAllowAutoPlay(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });

        constrainToScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() != null) {
                        properties.getSize()
                                .setConstrainToScreen(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                    }
                }
            }
        });
    }

    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link YoutubeVideoProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof YoutubeVideoProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'YoutubeVideoProperties'.");
        }

        currentMedia = media;

        YoutubeVideoProperties properties = (YoutubeVideoProperties) media.getMediaTypeProperties();
        resetPanel(properties);

        videoTextBox.setValue(media.getUri());

        videoAutoPlayCheck.setValue(properties.getAllowAutoPlay().equals(BooleanEnum.TRUE));
        videoFullScreenCheck.setValue(properties.getAllowFullScreen().equals(BooleanEnum.TRUE));

        videoSizeCheck.setValue(properties.getSize() != null);
        videoSizePanel.setVisible(properties.getSize() != null);

        Size propsSize = properties.getSize();

        if (propsSize != null) {
            if (propsSize.getHeight() != null) {
                videoHeightBox.setValue(propsSize.getHeight().toString());
            }

            // Default height units to pixel units
            if (propsSize.getHeightUnits() == null) {
                propsSize.setHeightUnits(VideoCssUnitsEnum.PIXELS.getName());
            } else if (propsSize.getHeightUnits() != null) {
                setInitialUnitsListBoxSelection(videoUnitHeight, propsSize.getHeightUnits());
            }

            if (propsSize.getWidth() != null) {
                videoWidthBox.setValue(propsSize.getWidth().toString());
            }

            // Default width units to pixel units.
            if (propsSize.getWidthUnits() == null) {
                propsSize.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
            } else if (propsSize.getWidthUnits() != null) {
                setInitialUnitsListBoxSelection(videoUnitWidth, propsSize.getWidthUnits());
            }
        } else {
            // Default to pixel units.
            setInitialUnitsListBoxSelection(videoUnitHeight, VideoCssUnitsEnum.PIXELS.getName());
            setInitialUnitsListBoxSelection(videoUnitWidth, VideoCssUnitsEnum.PIXELS.getName());
        }

        if (propsSize != null && propsSize.getConstrainToScreen() != null) {
            constrainToScreenCheck.setValue(propsSize.getConstrainToScreen().equals(BooleanEnum.TRUE));
        } else if (propsSize != null) {
            logger.info("Setting initial constrain to screen value.");
            propsSize.setConstrainToScreen(BooleanEnum.FALSE);
            constrainToScreenCheck.setValue(false);

        }

        validateAll();
    }

    /**
     * Sets the initial unit list box selected value based on the unit name.
     * 
     * @param unitListBox The list box object to set the selected value for.
     * @param unitName The name of the units to select.
     */
    private void setInitialUnitsListBoxSelection(ListBox unitListBox, String unitName) {
        int selectedIndex = 0;
        for (VideoCssUnitsEnum unitsEnum : VideoCssUnitsEnum.VALUES()) {
            if (unitName.compareTo(unitsEnum.getName()) == 0) {
                selectedIndex = unitsEnum.getValue();
            }

        }

        unitListBox.setSelectedIndex(selectedIndex);
    }

    /**
     * Sets the default youtube video properties size values.
     * 
     * @param props The properties object to set the size for.
     */
    private void setDefaultYoutubeVideoPropertiesSize(YoutubeVideoProperties props) {
        logger.info("setDefaultYoutubeVideoPropertiesSize()");
        if (props != null) {
            // Default to pixels.
            Size newSize = new Size();
            newSize.setHeightUnits(VideoCssUnitsEnum.PIXELS.getName());
            newSize.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
            newSize.setConstrainToScreen(BooleanEnum.FALSE);
            props.setSize(newSize);
        }
    }

    /**
     * Resets the panel with the provided properties
     * 
     * @param properties the {@link YoutubeVideoProperties}. Can't be null.
     */
    public void resetPanel(YoutubeVideoProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("The parameter 'properties' cannot be null.");
        }

        currentMedia.setMediaTypeProperties(properties);

        /* make sure the link text box has a useful value with the given media name */
        if (currentMedia != null && StringUtils.isNotBlank(currentMedia.getName())) {
            mediaTitleTextbox.setValue(currentMedia.getName());
        } else {
            /* otherwise the previous value will be shown which is not what we want when adding a
             * new media */
            mediaTitleTextbox.setValue(null);
        }

        videoTextBox.setValue(null);
        videoSizeCheck.setValue(false);
        videoSizePanel.setVisible(false);
        videoWidthBox.setValue(null);
        videoHeightBox.setValue(null);
        videoFullScreenCheck.setValue(false);
        videoAutoPlayCheck.setValue(false);
        constrainToScreenCheck.setValue(false);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(youtubeUrlValidation);
        validationStatuses.add(youtubeWidthValidation);
        validationStatuses.add(youtubeHeightValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (youtubeUrlValidation.equals(validationStatus)) {
            boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties;
            youtubeUrlValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));

        } else if (youtubeWidthValidation.equals(validationStatus)) {
            boolean valid = true;
            if (currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();

                if (properties.getSize() != null) {
                    valid = properties.getSize().getWidth() != null
                            && properties.getSize().getWidth().compareTo(BigDecimal.ZERO) > 0;
                }
            }

            youtubeWidthValidation.setValidity(valid);

        } else if (youtubeHeightValidation.equals(validationStatus)) {
            boolean valid = true;
            if (currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();

                if (properties.getSize() != null) {
                    valid = properties.getSize().getHeight() != null
                            && properties.getSize().getHeight().compareTo(BigDecimal.ZERO) > 0;
                }
            }
            youtubeHeightValidation.setValidity(valid);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        mediaTitleTextbox.setEnabled(!isReadonly);
        videoTextBox.setEnabled(!isReadonly);
        videoSizeCheck.setEnabled(!isReadonly);
        videoWidthBox.setEnabled(!isReadonly);
        videoUnitWidth.setEnabled(!isReadonly);
        videoHeightBox.setEnabled(!isReadonly);
        videoUnitHeight.setEnabled(!isReadonly);
        constrainToScreenCheck.setEnabled(!isReadonly);
        videoFullScreenCheck.setEnabled(!isReadonly);
        videoAutoPlayCheck.setEnabled(!isReadonly);
    }
}