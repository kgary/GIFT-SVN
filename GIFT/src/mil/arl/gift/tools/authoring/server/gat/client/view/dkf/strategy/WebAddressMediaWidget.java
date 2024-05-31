/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Media.DisplaySessionProperties;
import generated.dkf.BooleanEnum;
import generated.dkf.Media;
import generated.dkf.WebpageProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * Allows creating or editing a Webpage media item.
 * 
 * @author sharrison
 */
public class WebAddressMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(WebAddressMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static WebAddressMediaWidgetUiBinder uiBinder = GWT.create(WebAddressMediaWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface WebAddressMediaWidgetUiBinder extends UiBinder<Widget, WebAddressMediaWidget> {
    }

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The webpage url text box */
    @UiField
    protected TextBox urlTextBox;

    /** The button to preview the webpage url */
    @UiField
    protected Button urlPreviewButton;
    
    @UiField
    protected MediaDisplaySessionPropertiesWrapper requestSessionState;

    /** The media that is currently being edited */
    private Media currentMedia;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** The error message for validation if the media does not have a title */
    private static final String MEDIA_TITLE_ERR_MSG = "No title has been given to this media. Enter a title to be shown alongside this media.";

    /** Validation used to handle entering a web address */
    private WidgetValidationStatus webAddressValidation;
    
    /** The error message to display if there is no web address */
    private static final String NO_WEB_ADDRESS_MSG = "No address URL has been entered. Enter the URL addres of the content that should be presented to the learner.";

    /** The error message to display if the web address is too short */
    private static final String WEB_ADDRESS_LENGTH_MSG = "The address URL must be at least 3 characters.";

    /** The error message to display if the web address isn't in a valid format */
    private static final String INVALID_WEB_ADDRESS_MSG = "The entered address URL is in an invalid format.";

    /**
     * Creates a new editor for modifying feedback items
     */
    public WebAddressMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox, MEDIA_TITLE_ERR_MSG);
        webAddressValidation = new WidgetValidationStatus(urlTextBox, NO_WEB_ADDRESS_MSG);

        mediaTitleTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                currentMedia.setName(newName == null ? null : newName.trim());
                requestValidation(nameValidation);
            }
        });

        initWebpage();
    }

    /** Initializes the panel */
    private void initWebpage() {

        urlTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (currentMedia != null) {
                    currentMedia.setUri(event.getValue());
                }

                // auto fill the title textbox to help the author, they can always change it
                if (StringUtils.isNotBlank(event.getValue()) && StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                    mediaTitleTextbox.setValue(event.getValue(), true);
                }
                
                requestValidation(webAddressValidation);
            }
        });

        urlPreviewButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                String url = urlTextBox.getValue();
                if (StringUtils.isBlank(url)) {
                    WarningDialog.error("URL Error", "Please provide a URL to preview.");
                } else {
                    if (!url.startsWith("http")) {
                        url = "http://".concat(url);
                    }
                    Window.open(url, "_blank", "");
                }
            }
        });
        
        requestSessionState.addValueChangeHandler(new ValueChangeHandler<DisplaySessionProperties>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<DisplaySessionProperties> event) {
                
                if(currentMedia != null) {
                    currentMedia.setDisplaySessionProperties(event.getValue());
                    
                    refreshDisplayProperties(event.getValue());
                }
            }
        });
    }

    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link WebpageProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof WebpageProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'WebpageProperties'.");
        }

        currentMedia = media;

        WebpageProperties properties = (WebpageProperties) media.getMediaTypeProperties();
        resetPanel(properties);

        String uri = currentMedia.getUri();
        if (StringUtils.isNotBlank(uri)) {
            urlTextBox.setValue(uri);
        }
        
        requestSessionState.setValue(media.getDisplaySessionProperties());
        refreshDisplayProperties(media.getDisplaySessionProperties());

        validateAll();
    }

    /**
     * Resets the panel with the provided properties
     * 
     * @param properties the {@link WebpageProperties}. Can't be null.
     */
    public void resetPanel(WebpageProperties properties) {
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

        urlTextBox.setValue(null);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(webAddressValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (webAddressValidation.equals(validationStatus)) {
            if (!(currentMedia.getMediaTypeProperties() instanceof WebpageProperties)) {
                webAddressValidation.setValid();
                return;
            }

            if (StringUtils.isBlank(currentMedia.getUri())) {
                webAddressValidation.setErrorMessage(NO_WEB_ADDRESS_MSG);
                webAddressValidation.setInvalid();
            } else if (currentMedia.getUri().length() < 3) {
                webAddressValidation.setErrorMessage(WEB_ADDRESS_LENGTH_MSG);
                webAddressValidation.setInvalid();
            } else if (!CourseElementUtil.isWebAddress(currentMedia)) {
                webAddressValidation.setErrorMessage(INVALID_WEB_ADDRESS_MSG);
                webAddressValidation.setInvalid();
            } else {
                webAddressValidation.setValid();
            }
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
        urlTextBox.setEnabled(!isReadonly);
    }

    /**
     * Refreshes the UI to match the state of the current display properties
     * 
     * @param displaySessionProperties the display properties. Can be null;
     */
    private void refreshDisplayProperties(DisplaySessionProperties displaySessionProperties) {
        
        if(displaySessionProperties != null && BooleanEnum.TRUE.equals(displaySessionProperties.getRequestUsingSessionState())){
            
            /* Disable editing the URL since the strategy provider should provide it instead */
            urlTextBox.setEnabled(false);
            urlTextBox.setTitle("The external strategy provider will provide this URL");
            urlPreviewButton.setEnabled(false);
            
            String providerUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.EXTERNAL_STRATEGY_PROVIDER_URL);
            if(StringUtils.isNotBlank(providerUrl)) {
                urlTextBox.setValue(providerUrl, true);
            }
            
        } else {
            
            /* Enable editing the URL when the strategy provider is not being used */
            urlTextBox.setEnabled(true);
            urlTextBox.setTitle(null);
            urlPreviewButton.setEnabled(true);
        }
    }
}