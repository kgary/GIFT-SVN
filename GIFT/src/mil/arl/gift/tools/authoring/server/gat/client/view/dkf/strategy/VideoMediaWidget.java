package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;

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
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.Media;
import generated.dkf.Size;
import generated.dkf.VideoProperties;
import mil.arl.gift.common.enums.VideoCssUnitsEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * Allows creating or editing a Video media item.
 * 
 * @author bdonovan
 */

public class VideoMediaWidget extends ScenarioValidationComposite {
	
    /** The logger for the class */
	private static final Logger logger = Logger.getLogger(VideoMediaWidget.class.getName());
	
	/** The UiBinder that combines the ui.xml with this java class */
    private static VideoMediaWidgetUiBinder uiBinder = GWT.create(VideoMediaWidgetUiBinder.class);
    
    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface VideoMediaWidgetUiBinder extends UiBinder<Widget, VideoMediaWidget> {
    }
	
    /** Label to be displayed when there is no file selected */
    private static final String NO_FILE_LABEL = "No File Selected";
	
    /** The media title textbox */
	@UiField
    protected TextBox mediaTitleTextbox;
	
	/** The deck panel that allows for toggling other panels */
	@UiField
	protected DeckPanel deckPanel;
	
	/** The panel to be displayed when no video is selected */
	@UiField
	protected FocusPanel selectLocalVideoPanel;
	
	/** The panel to be displayed when a video is selected */
	@UiField
	protected Widget localVideoSelectedPanel;
	
	/** The button to remove the selected video */
	@UiField
	protected Button removeLocalVideoButton;
	
	/** The file label to display when a video is selected */
	@UiField
	protected Label localVideoFileLabel;
	
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
	
    /** A dialog used to select video files */
    private DefaultGatFileSelectionDialog videoFileDialog = new DefaultGatFileSelectionDialog();
    
    /** The media that is currently being edited */
    private Media currentMedia;
    
    /** The path to the course folder */
    private final String courseFolderPath;
    
    /** The read only flag */
    private boolean readOnly = false;
    
    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle selecting a video */
    private WidgetValidationStatus videoValidation;
    
    /** Validation used to handle entering the width of a video */
    private final WidgetValidationStatus videoWidthValidation;

    /** Validation used to handle entering the height of a video */
    private final WidgetValidationStatus videoHeightValidation;
    
    /**
     * Creates a new editor for modifying feedback items
     */
	public VideoMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");
        
        videoValidation = new WidgetValidationStatus(selectLocalVideoPanel,
                "No video file has been selected. Select the video file that should be presented to the learner.");
        
        videoWidthValidation = new WidgetValidationStatus(videoWidthBox,
                "The width of the video must be greater than 0. Specify the width that this video should be presented with.");

        videoHeightValidation = new WidgetValidationStatus(videoHeightBox,
                "The height of the video must be greater than 0. Specify the height that this video should be presented with.");

        mediaTitleTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                currentMedia.setName(newName == null ? null : newName.trim());
                requestValidation(nameValidation);
            }
        });
        
        String currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
        String courseName = GatClientUtility.getCourseFolderName(currentCoursePath);
        courseFolderPath = currentCoursePath.substring(0, currentCoursePath.indexOf(courseName) + courseName.length());       
		
        initVideo();
	}
	
	/** Initializes the panel */
	private void initVideo() {
		videoFileDialog.setAllowedFileExtensions(Constants.VIDEO);
		videoFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
		videoFileDialog.setIntroMessageHTML(" Select and image file.<br> Supported extensions are :<b>" + Constants.VIDEO + "</b>");
		videoUnitWidth.clear();
		videoUnitHeight.clear();
		for (VideoCssUnitsEnum unitEnum : VideoCssUnitsEnum.VALUES()) {
            videoUnitWidth.addItem(unitEnum.getDisplayName(), unitEnum.getName());
            videoUnitHeight.addItem(unitEnum.getDisplayName(), unitEnum.getName());
        }
		
		selectLocalVideoPanel.addClickHandler(new ClickHandler() {
		    @Override
            public void onClick(ClickEvent event) {
                videoFileDialog.center();
            }
		});
		
		if (readOnly) {
		    removeLocalVideoButton.setVisible(false);
		}
		removeLocalVideoButton.addClickHandler(new ClickHandler () {
		    @Override
		    public void onClick(ClickEvent event) {
		        if (readOnly) {
		            return;
		        }
		        
		        DeleteRemoveCancelDialog.show("Delete Content",  "Do you wish to <b>permanently delete</b> '"
		                + localVideoFileLabel.getText()
		                + "' from the course or simply remove the reference to that content in this metadata object? <br><br>"
		                + "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.", 
		                new DeleteRemoveCancelCallback() {
		                    
        		            @Override
                            public void cancel() {
        
                            }
        		            
        		            @Override
        		            public void delete() {
        		                
        		                String username = GatClientUtility.getUserName();
        		                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
        		                List<String> filesToDelete = new ArrayList<String>();
        		                final String filePath = courseFolderPath + "/" + localVideoFileLabel.getText();
        		                filesToDelete.add(filePath);
        		                
        		                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
        		                SharedResources.getInstance().getDispatchService().execute(action, 
        		                        new AsyncCallback<GatServiceResult>() {
        		                    
        		                            @Override
        		                            public void onFailure(Throwable error) {
        		                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                        "Failed to delete the file.", error.getMessage(),
                                                        DetailedException.getFullStackTrace(error));
                                                dialog.setDialogTitle("Deletion Failed");
                                                dialog.center();
        		                            }
        		                            
        		                            @Override
        		                            public void onSuccess(GatServiceResult result) {
        		                                
        		                                if (result.isSuccess()) {
                                                    logger.warning("Successfully deleted the file '" + filePath + "'.");
                                                } else {
                                                    logger.warning("Was unable to delete the file: " + filePath
                                                            + "\nError Message: " + result.getErrorMsg());
                                                }
        		                                
        		                                resetUI();
        		                            }
        		                    
        		                        });
        		            }
        		            
        		            @Override
        		            public void remove() {
        		                resetUI();
        		            }
        		            
        		            private void resetUI() {
        		                if (currentMedia != null) {
                                    currentMedia.setUri(null);
                                    requestValidation(videoValidation);

                                    localVideoFileLabel.setText("Select Image");
                                    deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalVideoPanel));
                                }
        		            }
		            
		                }, "Delete Content");
		    }
		});
		
		videoFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
		    
		    @Override
		    public void onValueChange(ValueChangeEvent<String> event) {
		        
		        if (currentMedia != null) {
		            currentMedia.setUri(event.getValue());
		            deckPanel.showWidget(deckPanel.getWidgetIndex(localVideoSelectedPanel));
		            requestValidation(videoValidation);
		            
		            if (event.getValue() != null) {
                        localVideoFileLabel.setText(event.getValue());
                        deckPanel.showWidget(deckPanel.getWidgetIndex(localVideoSelectedPanel));

                        final String filename = event.getValue();

                        if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                            mediaTitleTextbox.setValue(filename, true);
                        }

                    } else {
                        localVideoFileLabel.setText("Select Image");
                        deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalVideoPanel));
                    }
		        }
		    }
		});
		
		videoUnitWidth.addChangeHandler(new ChangeHandler() {
		    @Override
		    public void onChange(ChangeEvent arg0) {
		        if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
		            VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
		            if (properties != null && properties.getSize() != null) {
		                String units = videoUnitWidth.getSelectedValue();
                        properties.getSize().setWidthUnits(units);
                        requestValidation(videoWidthValidation);
		            }
		        }
		    }
		});
		
		videoUnitHeight.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent arg0) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties != null && properties.getSize() != null) {
                        String units = videoUnitHeight.getSelectedValue();
                        properties.getSize().setHeightUnits(units);
                        requestValidation(videoHeightValidation);
                    }
                }
            }
        });
		
		videoSizeCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    if (event.getValue()) {
                        setDefaultVideoPropertiesSize(properties);
                    } else {
                        properties.setSize(null);
                    }
                    videoSizePanel.setVisible(event.getValue());
                }
            }
        });
		
		videoWidthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() == null) {
                        setDefaultVideoPropertiesSize(properties);
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
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() == null) {
                        setDefaultVideoPropertiesSize(properties);
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
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    properties.setAllowFullScreen(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });
		
		videoAutoPlayCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    properties.setAllowAutoPlay(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });
		
		constrainToScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    if (properties.getSize() != null) {
                        properties.getSize()
                                .setConstrainToScreen(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                    }
                }
            }
        });
	}
	
	public void edit(Media media) {
	    if (media == null) {
	        throw new IllegalArgumentException("The parameter 'media' cannot be null.");
	    } else if (!(media.getMediaTypeProperties() instanceof VideoProperties)) {
	        throw new IllegalArgumentException("The parameter 'media.getMediaTypeProperties()' must be of type 'VideoProperties'.");
	    }
	    
	    currentMedia = media;
	    
	    VideoProperties properties = (VideoProperties) media.getMediaTypeProperties();
	    resetPanel(properties);
	    
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
	    
	    if (StringUtils.isNotBlank(media.getUri())) {
	        localVideoFileLabel.setText(media.getUri());
	        deckPanel.showWidget(deckPanel.getWidgetIndex(localVideoSelectedPanel));
	    }
	    
	    validateAll();
	}
	
	private void setInitialUnitsListBoxSelection(ListBox unitListBox, String unitName) {
	    int selectedIndex = 0;
	    for (VideoCssUnitsEnum unitsEnum : VideoCssUnitsEnum.VALUES()) {
	        if (unitName.compareTo(unitsEnum.getName()) == 0) {
	            selectedIndex = unitsEnum.getValue();
	        }
	    }
	    unitListBox.setSelectedIndex(selectedIndex);
	}
	
	private void setDefaultVideoPropertiesSize(VideoProperties props) {
	    logger.info("setDefaultVideoPropertiesSize()");
	    if (props != null) {
	        Size newSize = new Size();
	        newSize.setHeightUnits(VideoCssUnitsEnum.PIXELS.getName());
	        newSize.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
	        newSize.setConstrainToScreen(BooleanEnum.FALSE);
	        props.setSize(newSize);
	    }
	}
	
	public void resetPanel(VideoProperties properties) {
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
        
        localVideoFileLabel.setText(NO_FILE_LABEL);
        deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalVideoPanel));
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
	    validationStatuses.add(videoValidation);
	    validationStatuses.add(videoWidthValidation);
	    validationStatuses.add(videoHeightValidation);
	}
	
	@Override
	public void validate(ValidationStatus validationStatus) {
	    if (nameValidation.equals(validationStatus)) {
	        nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));
	        
	    } else if (videoValidation.equals(validationStatus)) {
	        boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof VideoProperties;
	        videoValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));

	    } else if (videoWidthValidation.equals(validationStatus)) {
	        boolean valid = true;
	        if (currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
	            VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
	            
	            if (properties.getSize() != null) {
	                valid = properties.getSize().getWidth() != null
	                        && properties.getSize().getWidth().compareTo(BigDecimal.ZERO) > 0;
	            }
	        }
	        videoWidthValidation.setValidity(valid);
	    
	    } else if (videoHeightValidation.equals(validationStatus)) {
	        boolean valid = true;
	        if (currentMedia.getMediaTypeProperties() instanceof VideoProperties) {
	            VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
	            
	            if (properties.getSize() != null) {
	                valid = properties.getSize().getHeight() != null
	                        && properties.getSize().getHeight().compareTo(BigDecimal.ZERO) > 0;
	            }
	        }
	        videoHeightValidation.setValidity(valid);
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
	    this.readOnly = isReadonly;
	    
	    mediaTitleTextbox.setEnabled(!isReadonly);
        removeLocalVideoButton.setVisible(!isReadonly);
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
