/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog.NewVideoCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.DisplayPicker.DisplayOptions;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager.VideoState;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider.RegisteredSessionChangeHandler;

/**
 * A picker tool to list the different display options that the user can show on
 * their Game Master dashboard when a session is selected.
 *
 * @author sharrison
 */
public class DisplayPicker extends Composite implements HasValue<DisplayOptions>, RegisteredSessionChangeHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(DisplayPicker.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static DisplayPickerUiBinder uiBinder = GWT.create(DisplayPickerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface DisplayPickerUiBinder extends UiBinder<Widget, DisplayPicker> {
    }

    /**
     * A unique static identifier so that we can create unique "groups" for the
     * radio buttons
     */
    private static int uniqueInstanceId = 0;
    
    /** default left px padding on radio label icon */
    private static int RADIO_LABEL_DEFAULT_PADDING_LEFT = 0;
    
    /** default right px padding on left label icon */
    private static int RADIO_LABEL_DEFAULT_PADDING_RIGHT = 8;

    /** The prefix to the group name */
    private final String radioGroupName = "displayPickerGroup-" + uniqueInstanceId;

    /** The header button that displays what is currently selected */
    @UiField
    protected Button headerButton;

    /** The menu containing the display options */
    @UiField
    protected DropDownMenu selectMenu;

    /** The radio button for {@link DisplayOptions#NONE} */
    @UiField(provided = true)
    protected Radio noneRadio = new Radio(radioGroupName, "None");

    /** The radio button for {@link DisplayOptions#MAP} */
    @UiField(provided = true)
    protected Radio mapRadio = new Radio(radioGroupName, buildRadioLabel(DisplayOptions.MAP, RADIO_LABEL_DEFAULT_PADDING_LEFT, 11));

    /** The radio button for {@link DisplayOptions#ASSESSMENTS} */
    @UiField(provided = true)
    protected Radio assessmentsRadio = new Radio(radioGroupName, buildRadioLabel(DisplayOptions.ASSESSMENTS, RADIO_LABEL_DEFAULT_PADDING_LEFT, RADIO_LABEL_DEFAULT_PADDING_RIGHT));
    
    /** The radio button for {@link DisplayOptions#STRATEGIES} */
    @UiField(provided = true)
    protected Radio strategiesRadio = new Radio(radioGroupName, buildRadioLabel(DisplayOptions.STRATEGIES, 5, 12));
    
    /** The radio button for {@link DisplayOptions#BOOKMARK} */
    @UiField(provided = true)
    protected Radio bookmarkRadio = new Radio(radioGroupName, buildRadioLabel(DisplayOptions.BOOKMARK, 5, 12));

    /** The menu containing all the videos */
    @UiField
    protected DropDownMenu videoMenu;

    /** The panel containing the {@link #videoRadio} */
    @UiField
    protected FlowPanel videoRadioContainer;

    /** The radio button for {@link DisplayOptions#VIDEO} */
    @UiField(provided = true)
    protected Radio videoRadio = new Radio(radioGroupName, buildRadioLabel(DisplayOptions.VIDEO, RADIO_LABEL_DEFAULT_PADDING_LEFT, RADIO_LABEL_DEFAULT_PADDING_RIGHT));

    /** The tooltip shown next to this picker's main button */
    @UiField
    protected ManagedTooltip tooltip;
    
    /** The selected option for this picker */
    private DisplayOptions selectedOption;

    /** The list of disabled options */
    private final Set<DisplayOptions> disabledOptions = new HashSet<>();

    /**
     * The videos that could be played during the playback session. Using a
     * linked hashmap to maintain insert order
     */
    private final Map<VideoManager, CheckBox> videoManagerOptions = new LinkedHashMap<>();

    /** The videos that are selected */
    private final Set<VideoManager> selectedVideos = new HashSet<>();

    /** The option to add a new video */
    private final HTML addNewVideo = new HTML("Add Video...");

    /** The callback for when the video selections change */
    private final ChangeCallback<Set<VideoManager>> videoSelectionCallback;

    /** The callback to invoke when the add video upload finishes or fails */
    private final NewVideoCallback newVideoCallback;

    /** The modal dialog for adding a new video to the playback */
    private AddVideoModalDialog addVideoDialog;

    /**
     * The different display options for the game master panel.
     * 
     * @author sharrison
     */
    public enum DisplayOptions {
        /** Show nothing */
        NONE("Nothing Selected", null),
        /** Show the map panel */
        MAP("Map", IconType.GLOBE),
        /** Show the session assessment details */
        ASSESSMENTS("Assessments", IconType.GAVEL),
        /** Show the video panel */
        VIDEO("Video", IconType.VIDEO_CAMERA),
        /** Show the strategies panel */
        STRATEGIES("Scenario Injects", IconType.BOLT),
        /** Show the bookmark panel */
        BOOKMARK("Notes", IconType.BOOKMARK);

        /** The name to show to the user in the menu */
        private final String displayName;

        /** The associated icon for the display type */
        private final IconType icon;

        /**
         * Constructor.
         * 
         * @param displayName the name to show to the user in the menu
         * @param icon the associated icon for the display type
         */
        private DisplayOptions(String displayName, IconType icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
    }

    /**
     * Constructor.
     * 
     * @param videoSelectionCallback the callback for when the video selections
     *        change. If null, the {@link DisplayOptions#VIDEO} option will be
     *        removed.
     * @param newVideoCallback the callback to invoke when the add video upload
     *        finishes or fails. If null, the add video option will be removed.
     */
    public DisplayPicker(ChangeCallback<Set<VideoManager>> videoSelectionCallback, NewVideoCallback newVideoCallback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        this.videoSelectionCallback = videoSelectionCallback;
        this.newVideoCallback = newVideoCallback;

        uniqueInstanceId++;
        initWidget(uiBinder.createAndBindUi(this));

        if (videoSelectionCallback == null) {
            removeDisplayOption(DisplayOptions.VIDEO);
        }

        /* Stop propagation on the menu so clicking on a radio button doesn't
         * close the menu */
        selectMenu.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        addNewVideo.getElement().getStyle().setPaddingLeft(4, Unit.PX);
        addNewVideo.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (addVideoDialog != null) {
                    addVideoDialog.show();
                }
            }
        });

        /* Add handlers for all the options */
        addRadioHandler(noneRadio, DisplayOptions.NONE);
        addRadioHandler(mapRadio, DisplayOptions.MAP);
        addRadioHandler(assessmentsRadio, DisplayOptions.ASSESSMENTS);
        addRadioHandler(videoRadio, DisplayOptions.VIDEO);
        addRadioHandler(strategiesRadio, DisplayOptions.STRATEGIES);
        addRadioHandler(bookmarkRadio, DisplayOptions.BOOKMARK);

        /* Attempt to default value to None first */
        setValue(DisplayOptions.NONE);

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to registered session changes */
        RegisteredSessionProvider.getInstance().addManagedHandler(this);
    }

    /**
     * Builds the radio label using the icon css name and display name of the
     * provided option.
     * 
     * @param option the display option to use to build the label. Can't be
     *        null.
     * @param paddingLeftPx non-negative padding (px) to apply to left of radio label icon. Can be 0.
     * @param paddingRightPx non-negative padding (px) to apply to right of radio lable icon.  Can be 0.
     * @return the {@link SafeHtml} of the label.
     */
    private static SafeHtml buildRadioLabel(DisplayOptions option, int paddingLeftPx, int paddingRightPx) {
        return SafeHtmlUtils.fromTrustedString("<i class='fa " + option.icon.getCssName()
                + "' style='font-size:large; padding-left:"+paddingLeftPx+"px; padding-right:"+paddingRightPx+"px;'></i><span class='text'>" + option.displayName
                + "</span>");
    }

    /**
     * Adds the handler for the provided radio.
     * 
     * @param radio the radio to add the handler too. Can't be null.
     * @param displayOption the {@link DisplayOptions} associated with the
     *        radio. Can't be null.
     */
    private void addRadioHandler(final Radio radio, final DisplayOptions displayOption) {
        if (displayOption == null) {
            throw new IllegalArgumentException("The parameter 'displayOption' cannot be null.");
        }
        
        /* used to determine if selecting an already selected choice in order to unselect this radio
         * and choose the default if possible.
         * default =  (map for left panel, none for right panel) */
        radio.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if (selectedOption == null || !selectedOption.equals(displayOption)){
                    return;
                }
                    
                if(noneRadio.isAttached()){
                    // none is a choice - this is right panel picker
                    
                    if(!DisplayOptions.NONE.equals(displayOption)){
                        /*  the option being interacted with is NOT none, 
                         * deselect this choice and choose None */
                        setValue(DisplayOptions.NONE, true);
                        radio.setValue(false, false);
                    }
                }else{
                    // none is NOT a choice - this is left panel picker
                    
                    if(mapRadio.isEnabled() && !DisplayOptions.MAP.equals(displayOption)){
                        /* the option being interacted with is NOT Map, 
                         * deselect this choice and choose Map
                         * Note: Map can be disabled if Map is selected on right panel */
                        setValue(DisplayOptions.MAP, true);
                        radio.setValue(false, false);
                    }
                }
            }
        });
        
        radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (Boolean.TRUE.equals(event.getValue())) {
                    setValue(displayOption, true);
                }
            }
        });
    }

    /** Updates the radio buttons' state */
    private void updateDisplay() {
        for (DisplayOptions option : DisplayOptions.values()) {
            final boolean isSelected = option == selectedOption;
            final boolean isEnabled = !disabledOptions.contains(option);

            Radio radio;
            switch (option) {
            case NONE:
                radio = noneRadio;
                break;
            case MAP:
                radio = mapRadio;
                break;
            case ASSESSMENTS:
                radio = assessmentsRadio;
                break;
            case STRATEGIES:
                radio = strategiesRadio;
                break;
            case BOOKMARK:
                radio = bookmarkRadio;
                break;
            case VIDEO:
                radio = videoRadio;
                for (CheckBox chkbox : videoManagerOptions.values()) {
                    chkbox.setEnabled(isEnabled);
                }
                break;
            default:
                continue;
            }

            radio.setValue(isSelected);
            radio.setEnabled(isEnabled);
        }
    }

    /**
     * Populate this picker with the possible videos.
     * 
     * @param videos the videos that exist in this session.
     */
    public void populateVideos(List<VideoManager> videos) {
        videoManagerOptions.clear();
        selectedVideos.clear();

        if (videos != null) {
            for (VideoManager vid : videos) {
                addNewVideo(vid);
            }
        }

        rebuildVideoMenu();
    }

    /**
     * Add a new video to the list of video selections. It will be selected by
     * default.
     * 
     * @param videoManager the manager of the video to add. If null, nothing
     *        will be added.
     */
    public void addNewVideo(final VideoManager videoManager) {
        if (videoManager == null) {
            return;
        }

        CheckBox chkbox = new CheckBox(videoManager.getChannelLabel());
        chkbox.setValue(true);
        chkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                final Set<VideoManager> oldValues = new HashSet<>(selectedVideos);

                /* If it is deselected, disable the video; and vice-versa */
                if (Boolean.FALSE.equals(event.getValue())) {
                    videoManager.updateState(VideoState.DISABLED);
                    selectedVideos.remove(videoManager);
                } else {
                    videoManager.removeState(VideoState.DISABLED);
                    selectedVideos.add(videoManager);
                }

                /* If a video is de/selected, automatically switch to the Video
                 * display option */
                setValue(DisplayOptions.VIDEO, true);
                if (videoSelectionCallback != null) {
                    videoSelectionCallback.onChange(selectedVideos, oldValues);
                }
            }
        });

        videoManagerOptions.put(videoManager, chkbox);
        selectedVideos.add(videoManager);

        rebuildVideoMenu();
    }
    
    /**
     * Update the video properties in this widget.
     * @param videoManager contains video properties that alter the picker details, e.g. title
     */
    public void updateVideo(final VideoManager videoManager){
        if (videoManager == null) {
            return;
        }
        
        logger.info("Updating video - "+videoManager.getVideoMetadata().getTitle());
        
        CheckBox chkbox = videoManagerOptions.get(videoManager);
        if(chkbox == null){
            logger.warning("Failed to find the video to update in the display picker");
            return;
        }
        chkbox.setText(videoManager.getChannelLabel());
        
        rebuildVideoMenu();
    }
    
    /**
     * Removes a video from this picker instance.
     * @param videoManager the video manager that contains the video being removed.  If null
     * this method does nothing.
     */
    public void removeVideo(final VideoManager videoManager){
        if (videoManager == null) {
            return;
        }
        
        logger.info("Removing video - "+videoManager.getVideoMetadata().getTitle());
        
        CheckBox vidChkBox = videoManagerOptions.remove(videoManager);
        boolean removed = selectedVideos.remove(videoManager);
        
        logger.info("Remove video status-> Removed checkbox = "+vidChkBox != null ? "true": "false" +", data set ="+removed);

        rebuildVideoMenu();
    }

    /** Rebuild the video menu with the existing video checkboxes */
    private void rebuildVideoMenu() {
        videoMenu.clear();
        for (CheckBox vidChkBox : videoManagerOptions.values()) {
            videoMenu.add(vidChkBox);
        }

        if (newVideoCallback != null) {
            if (videoMenu.getWidgetCount() != 0) {
                videoMenu.add(new Divider());
            }

            videoMenu.add(addNewVideo);
        }
    }

    /**
     * Updates the picker UI with the provided videos.
     * 
     * @param selectedVideos the list of selected videos.
     */
    public void updateSelectedVideos(Set<VideoManager> selectedVideos) {
        this.selectedVideos.clear();
        for (Entry<VideoManager, CheckBox> entry : videoManagerOptions.entrySet()) {
            final VideoManager vid = entry.getKey();
            if (selectedVideos != null && selectedVideos.contains(vid)) {
                entry.getValue().setValue(true);
                this.selectedVideos.add(vid);
            } else {
                entry.getValue().setValue(false);
            }
        }
    }

    /**
     * Set the {@link DisplayOptions} that should become disabled.
     * 
     * @param values the {@link DisplayOptions} to disable. Can't disable
     *        {@link DisplayOptions#NONE} or the currently selected option.
     */
    public void setDisabledOptions(DisplayOptions... values) {
        disabledOptions.clear();

        if (values != null) {
            for (DisplayOptions toDisable : values) {
                if (toDisable == selectedOption || toDisable == DisplayOptions.NONE) {
                    continue;
                }
                disabledOptions.add(toDisable);
            }
        }

        updateDisplay();
    }

    /**
     * Removes the display option from the picker. This is irreversible.
     * 
     * @param toRemove the {@link DisplayOptions} to remove.
     */
    public void removeDisplayOption(DisplayOptions toRemove) {
        switch (toRemove) {
        case NONE:
            noneRadio.removeFromParent();
            break;
        case MAP:
            mapRadio.removeFromParent();
            break;
        case ASSESSMENTS:
            assessmentsRadio.removeFromParent();
            break;
        case STRATEGIES:
            strategiesRadio.removeFromParent();
            break;
        case BOOKMARK:
            bookmarkRadio.removeFromParent();
            break;
        case VIDEO:
            videoRadioContainer.removeFromParent();
            break;
        }
    }
    
    /**
     * Checks if this picker allows the given display option to be selected.
     * 
     * @param option the option to check.
     * @return true if the option can be selected. False otherwise.
     */
    public boolean hasDisplayOption(DisplayOptions option) {
        switch (option) {
            case NONE:
                return noneRadio.getParent() != null;
            case MAP:
                return mapRadio.getParent() != null;
            case ASSESSMENTS:
                return assessmentsRadio.getParent() != null;
            case STRATEGIES:
                return strategiesRadio.getParent() != null;
            case BOOKMARK:
                return bookmarkRadio.getParent() != null;
            case VIDEO:
                return videoRadioContainer.getParent() != null;
        }
        
        return false;
    }

    /**
     * Shows/Hides the display option from the list of selectable options.
     * 
     * @param option the {@link DisplayOptions} to show or hide. Can't be null.
     * @param show true to show the display option (default); false to hide it.
     */
    private void showDisplayOption(DisplayOptions option, boolean show) {
        if (option == null) {
            throw new IllegalArgumentException("The parameter 'option' cannot be null.");
        }

        switch (option) {
        case NONE:
            noneRadio.setVisible(show);
            break;
        case MAP:
            mapRadio.setVisible(show);
            break;
        case ASSESSMENTS:
            assessmentsRadio.setVisible(show);
            break;
        case STRATEGIES:
            strategiesRadio.setVisible(show);
            break;
        case BOOKMARK:
            bookmarkRadio.setVisible(show);
            break;
        case VIDEO:
            videoRadioContainer.setVisible(show);
            break;
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisplayOptions> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public DisplayOptions getValue() {
        return selectedOption;
    }

    @Override
    public void setValue(DisplayOptions value) {
        setValue(value, false);
    }

    @Override
    public void setValue(DisplayOptions value, boolean fireEvents) {
        if (value == null) {
            value = DisplayOptions.NONE;
        }

        if (value == selectedOption || disabledOptions.contains(value)) {
            return;
        }

        final DisplayOptions before = this.selectedOption;
        this.selectedOption = value;
        headerButton.setText(value.displayName);
        headerButton.setIcon(value.icon);

        updateDisplay();

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    @Override
    public void registeredSessionChanged(AbstractKnowledgeSession newSession, AbstractKnowledgeSession oldSession) {
        if (newSession == null || !newSession.inPastSessionMode()) {
            showDisplayOption(DisplayOptions.VIDEO, false);
            addVideoDialog = null;
        } else {
            showDisplayOption(DisplayOptions.VIDEO, true);
            if (newVideoCallback != null) {
                addVideoDialog = new AddVideoModalDialog(new NewVideoCallback() {
                    @Override
                    public void onUploaded(VideoMetadata videoMetadata) {
                        newVideoCallback.onUploaded(videoMetadata);
                    }

                    @Override
                    public void onUploadFailed(String message) {
                        newVideoCallback.onUploadFailed(message);
                    }
                });
            }
        }
    }

    @Override
    public void logPatchFileChanged(String logPatchFileName) {
        /* Nothing to do */
    }
    
    /**
     * Sets the text that should be displayed in this picker's tooltip
     * 
     * @param text the tooltip text. Can be null.
     */
    public void setTooltipText(String text) {
        tooltip.setTitle(text);
    }
}