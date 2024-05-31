/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.gallery.client.ui.Gallery;
import org.gwtbootstrap3.extras.gallery.client.ui.GalleryImage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.AuthoringSupportElements;
import generated.course.DkfRef;
import generated.course.EmbeddedApp;
import generated.course.EmbeddedApps;
import generated.course.MobileApp;
import generated.course.TrainingApplication;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.FileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;
import mil.arl.gift.tools.authoring.server.gat.shared.wrap.TrainingApplicationObject;

/**
 * An editor that edits a single course object. This editor allows the author change the name of the
 * course object and to edit the course object through the DKF editor.
 * 
 * @author sharrison
 */
public abstract class CourseObjectItemEditor extends ItemEditor<CourseObjectWrapper> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CourseObjectItemEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static CourseObjectItemEditorUiBinder uiBinder = GWT.create(CourseObjectItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface CourseObjectItemEditorUiBinder extends UiBinder<Widget, CourseObjectItemEditor> {
    }

    /** The label for the android event option */
    private static final String ANDROID_EVENT_LABEL = "Android Events";

    /** The tooltip for the android event option */
    private static final String ANDROID_EVENT_TOOLTIP = "Click here to create an Android Event real time assessment.";

    /** The label for the Unity event option */
    private static final String UNITY_EVENT_LABEL = "Unity";

    /** The tooltip for the Unity event option */
    private static final String UNITY_EVENT_TOOLTIP = "Click here to create a Unity real time assessment.";

    /** The container holding the different panel options to show to the user */
    @UiField
    protected DeckPanel deckPanel;

    /** The ribbon that allows the user to choose which type of course object to create */
    @UiField
    protected Ribbon courseObjectTypeRibbon;

    /** The panel containing the components to edit an existing course object */
    @UiField
    protected HTMLPanel editPanel;

    /** The panel containing the training application icon */
    @UiField
    protected SimplePanel trainingAppIconPanel;

    /** The label to show what workspace the training application is in */
    @UiField
    protected InlineHTML workspaceLabel;

    /** The name text box */
    @UiField
    protected TextBox nameTextBox;

    /** The panel containing the unity scenario choices and their respective details */
    @UiField
    protected HTMLPanel unityScenarioPanel;

    /** Container panel to show/hide the {@link #unityScenarios} list */
    @UiField
    protected SimplePanel unityScenariosContainer;

    /** The list of unity scenarios */
    @UiField
    protected LinkedGroup unityScenarios;

    /** The details of the selected unity scenario */
    @UiField
    protected HTML scenarioDetails;

    /**
     * The error label to be displayed if the author tries to access the DKF editor before setting a
     * valid name
     */
    @UiField
    protected HTML noNameErrorMsg;

    /** The error label to be displayed when the author is not allowed to access the DKF editor */
    @UiField
    protected HTML dkfEditorErrorMsg;

    /** The button to allow the user to edit a DKF file */
    @UiField
    protected Button dkfEditorButton;

    /** contains map images, dynamically added */
    @UiField
    protected Gallery mapGallery;

    /** Creates a remote service proxy to talk to the server-side RPC service. */
    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

    /** The wrapper being edited */
    private CourseObjectWrapper editedItem;

    /** The container for showing validation messages for the course object name. */
    private final WidgetValidationStatus nameTextBoxValidation;

    /** The container for showing validation messages for the real time assessment. */
    private final WidgetValidationStatus realTimeAssessmentValidation;

    /** The default validation message for the name text box */
    private static final String DEFAULT_NAME_VALIDATION_MSG = "The real time assessment must have a name.";

    /** The name shown to the user for the course object if it is unknown */
    private static final String UNKNOWN_NAME = "UNKNOWN";

    /**
     * The flag to indicate if we are creating a new training application or editing an existing one
     */
    private boolean creatingNew;

    /** The type of training application the user chose to create */
    private TrainingApplicationEnum ribbonChoice;

    /**
     * The last saved name of the training application. This is used to determine if the name has
     * changed.
     */
    private String savedTrainingAppName;

    /**
     * The training application type allowed to be selected. If null, allow any of the types to be
     * selected.
     */
    private TrainingApplicationEnum filterByType;

    /** The workspace\Public\TrainingAppsLib user folder for the user in this editor */
    private FileTreeModel userFolder;

    /** The properties for the GIFT scenarios */
    private List<GiftScenarioProperties> scenarioPropertyFiles;

    /** Maps the UI group item to its respective property */
    private Map<LinkedGroupItem, GiftScenarioProperties> groupItemToPropertyMap = new HashMap<>();

    /**
     * If editing an existing training application, this path will be to the TrainingAppsLib
     * subfolder; otherwise this will point to the newly created temp folder
     */
    private FileTreeModel editedObjectParentFolder;

    /**
     * The callback for when the template DKF is copied.
     */
    private final AsyncCallback<Boolean> copyCallback = new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
            String errorMsg = "Caught exception while copying dkf template file. Reason: '" + caught.toString() + "'";
            logger.severe(errorMsg);
            WarningDialog.error("Failed to create DKF file", errorMsg);
            getParentItemListEditor().cancelEditing();
        }

        @Override
        public void onSuccess(Boolean result) {
            if (result) {
                setSelectedScenarioIntoTrainingApplication();
                showDkfEditorDialog(editedItem.getCourseObject());
            } else {
                String errorMsg = "Caught exception while copying dkf template file.";
                logger.severe(errorMsg);
                WarningDialog.error("Failed to create DKF file", errorMsg);
                getParentItemListEditor().cancelEditing();
            }
        }
    };

    /**
     * Constructor.
     */
    public CourseObjectItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        nameTextBoxValidation = new WidgetValidationStatus(nameTextBox, DEFAULT_NAME_VALIDATION_MSG);
        realTimeAssessmentValidation = new WidgetValidationStatus(dkfEditorButton,
                "Please create a new real time assessment.");

        // rpc calls
        executeRpcMethods();

        // create ribbon
        initRibbon();

        nameTextBox.addDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent event) {
                noNameErrorMsg.setVisible(false);
                requestValidation(nameTextBoxValidation);
            }
        }, InputEvent.getType());

        dkfEditorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // edit
                if (editedItem.getCourseObject() != null) {
                    showDkfEditorDialog(editedItem.getCourseObject());
                } else {
                    final String nameValue = nameTextBox.getValue();
                    if (dkfEditorErrorMsg.isVisible() || noNameErrorMsg.isVisible()) {
                        return;
                    } else if (StringUtils.isBlank(nameValue) || isNameDuplicate(nameValue)) {
                        noNameErrorMsg.setVisible(true);
                        return;
                    }

                    BsLoadingDialogBox.display("Checking Name",
                            "Checking if the name is conflicting with another real time assessment, please wait...");
                    performRemotePreSaveValidation(new ValidationCallback() {
                        @Override
                        public void validationPassed() {
                            BsLoadingDialogBox.remove();

                            // ok to create new folder
                            createTrainingAppsLibFolder(nameValue);
                        }

                        @Override
                        public void validationFailed() {
                            BsLoadingDialogBox.remove();
                            noNameErrorMsg.setVisible(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * Call the RPC service to initialize some class members.
     */
    private void executeRpcMethods() {
        final String username = GatClientUtility.getUserName();
        rpcService.getTrainingAppsLibUserFolder(username, new AsyncCallback<GenericRpcResponse<FileTreeModel>>() {
            @Override
            public void onSuccess(GenericRpcResponse<FileTreeModel> response) {
                if (response.getWasSuccessful()) {
                    userFolder = response.getContent();
                } else {
                    throw new DetailedException("Unable to retrieve the TrainingAppsLib user folder.",
                            "Unable to retrieve the TrainingAppsLib user folder for user '" + username + "'.", null);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                throw new DetailedException("Unable to retrieve the TrainingAppsLib user folder.",
                        "Unable to retrieve the TrainingAppsLib user folder for user '" + username + "'.", t);
            }
        });

        rpcService.getTrainingApplicationScenarioProperties(username,
                new AsyncCallback<GenericRpcResponse<List<GiftScenarioProperties>>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<List<GiftScenarioProperties>> response) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("getTrainingApplicationScenarioPropertyFiles.onSuccess("
                                    + response.getWasSuccessful() + ")");
                        }

                        if (response.getWasSuccessful()) {
                            scenarioPropertyFiles = response.getContent();
                            refreshFilteredScenarioProperties();
                        } else {
                            throw new DetailedException(
                                    "Unable to retrieve the training application scenario property files.",
                                    "Unble to retrieve the training application scenario property files for user '"
                                            + username + "'.",
                                    null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("getTrainingApplicationScenarioPropertyFiles.onFailure()");
                        }

                        throw new DetailedException(
                                "Unable to retrieve the training application scenario property files.",
                                "Unable to retrieve the training application scenario property files for user '"
                                        + username + "'.",
                                t);
                    }
                });
    }

    /**
     * Update the visibility and text of the scenario property label based on a number of factors.
     */
    private void checkDkfEditorErrorMsg() {
        /* scenario properties do not need to be checked when editing */
        if (!creatingNew) {
            dkfEditorErrorMsg.setVisible(false);
            return;
        }

        /* currently only Unity requires the author to select a property */
        if (TrainingApplicationEnum.UNITY_EMBEDDED.equals(ribbonChoice)) {
            if (unityScenarios.getWidgetCount() == 0) {
                dkfEditorErrorMsg.setHTML(
                        "There are no Unity scenarios to select. This means that a Unity Application cannot be created at this time.");
                dkfEditorErrorMsg.setVisible(true);
                /* DKF error msg takes precedence over the no name error msg */
                noNameErrorMsg.setVisible(false);
                return;
            } else {
                /* Find the active item and see if the entry point was found */
                for (int i = 0; i < unityScenarios.getWidgetCount(); i++) {
                    Widget w = unityScenarios.getWidget(i);

                    if (w instanceof LinkedGroupItem) {
                        LinkedGroupItem listItem = (LinkedGroupItem) w;
                        if (!listItem.isActive()) {
                            continue;
                        }

                        final GiftScenarioProperties giftScenarioProperties = groupItemToPropertyMap.get(listItem);

                        /* entry point found is null or false */
                        if (!Boolean.TRUE.equals(giftScenarioProperties.isEntryPointFound())) {
                            dkfEditorErrorMsg.setHTML("The Unity scenario referenced by '"
                                    + giftScenarioProperties.getName()
                                    + "' cannot be found.<br/>This may be resolved by running the GIFT installer, which has an option in the 'Training Applications' section to download a Unity scenario.");
                            dkfEditorErrorMsg.setVisible(true);
                            /* DKF error msg takes precedence over the no name error msg */
                            noNameErrorMsg.setVisible(false);
                            return;
                        }
                        break;
                    }
                }
            }
        }

        /* Property is not required, hide label */
        dkfEditorErrorMsg.setVisible(false);
    }

    /**
     * Refreshes the filtered list of scenario properties.
     */
    private void refreshFilteredScenarioProperties() {

        unityScenarios.clear();
        groupItemToPropertyMap.clear();
        if (scenarioPropertyFiles == null) {
            checkDkfEditorErrorMsg();
            return;
        }

        List<GiftScenarioProperties> filteredScenarioPropertyFiles = new ArrayList<>();

        /* No filter */
        if (filterByType == null) {
            filteredScenarioPropertyFiles.addAll(scenarioPropertyFiles);
        } else {
            /* add the scenario properties that match the filtered type */
            for (GiftScenarioProperties propertyFile : scenarioPropertyFiles) {
                if (filterByType.equals(propertyFile.getTrainingApplicationType())) {
                    filteredScenarioPropertyFiles.add(propertyFile);
                }
            }
        }

        // Sort the conditions by display name
        Collections.sort(filteredScenarioPropertyFiles, new Comparator<GiftScenarioProperties>() {
            @Override
            public int compare(GiftScenarioProperties o1, GiftScenarioProperties o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        LinkedGroupItem firstItem = null;
        for (final GiftScenarioProperties property : filteredScenarioPropertyFiles) {
            final LinkedGroupItem groupItem = new LinkedGroupItem();
            groupItem.setText(property.getName());
            groupItem.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectUnityScenario(groupItem);
                    checkDkfEditorErrorMsg();
                }
            }, ClickEvent.getType());

            /* first widget added gets automatically selected */
            if (unityScenarios.getWidgetCount() == 0) {
                firstItem = groupItem;
            }

            /* add item to group list */
            unityScenarios.add(groupItem);
            groupItemToPropertyMap.put(groupItem, property);
        }

        /* if an item was added than make sure it is selected by default */
        if (firstItem != null) {
            firstItem.setActive(true);
            selectUnityScenario(firstItem);
        }

        checkDkfEditorErrorMsg();
    }

    /**
     * Selects the provided scenario group item in {@link #unityScenarios} and rebuilds the
     * {@link #scenarioDetails}.
     * 
     * @param selectGroupItem the scenario group item to select.
     */
    private void selectUnityScenario(LinkedGroupItem selectGroupItem) {
        /* unselect all items */
        for (int i = 0; i < unityScenarios.getWidgetCount(); i++) {
            Widget w = unityScenarios.getWidget(i);
            if (w instanceof LinkedGroupItem) {
                LinkedGroupItem listItem = (LinkedGroupItem) w;
                listItem.setActive(false);

                /* find first item in list to be selected if one wasn't provided */
                if (selectGroupItem == null) {
                    selectGroupItem = listItem;
                }
            }
        }

        
        /** No unity scenarios available */
        if (selectGroupItem == null) {
            return;
        }

        /* select the item that was clicked */
        selectGroupItem.setActive(true);

        final GiftScenarioProperties giftScenarioProperties = groupItemToPropertyMap.get(selectGroupItem);

        /* rebuild details HTML - with a placeholder message for retrieving the map thumbnail(s) */
        scenarioDetails.setHTML(buildScenarioPropertyDetails(giftScenarioProperties, null));

        /** retireve map thumbnail(s) */
        rpcService.getScenarioMapTileProperties(giftScenarioProperties.getPropertyParentPath(),
                GatClientUtility.getUserName(), new AsyncCallback<GenericRpcResponse<List<MapTileProperties>>>() {

                    @Override
                    public void onSuccess(GenericRpcResponse<List<MapTileProperties>> result) {
                        if (!result.getWasSuccessful()) {
                            DetailedExceptionSerializedWrapper exception = result.getException();
                            StringBuilder reasonBuilder = new StringBuilder();

                            /* Create the explanation for what went wrong */
                            reasonBuilder.append("message: ").append(exception.getMessage()).append('\n');
                            reasonBuilder.append("reason: ").append(exception.getReason()).append('\n');
                            reasonBuilder.append("details: ").append(exception.getDetails());
                            reasonBuilder.append("stack trace: ").append('\n');
                            for (String stackLine : exception.getErrorStackTrace()) {
                                reasonBuilder.append('\t').append(stackLine).append('\n');
                            }
                            logger.warning(
                                    "Failed to retrieve the map thumbnails because:\n" + reasonBuilder.toString());
                            scenarioDetails
                                    .setHTML(buildScenarioPropertyDetailsFailedThumbnails(giftScenarioProperties));
                            return;
                        }

                        if (result.getContent().isEmpty()) {
                            logger.warning(
                                    "Failed to retrieve the map thumbnails because the server returned no map tiles");
                            scenarioDetails
                                    .setHTML(buildScenarioPropertyDetailsFailedThumbnails(giftScenarioProperties));
                            return;
                        }

                        String zoomedOutImageUrl = null;
                        int smallestZoomLevel = Integer.MAX_VALUE;
                        for (MapTileProperties mapTileProp : result.getContent()) {
                           final int zoomLevel = mapTileProp.getZoomLevel();
                           if (zoomLevel < smallestZoomLevel){
                              zoomedOutImageUrl = ScenarioClientUtility.buildMapImageUrl(mapTileProp, giftScenarioProperties.getPropertyParentPath());
                              smallestZoomLevel = zoomLevel;
                              if(zoomLevel == 0) {
                                 break;
                                 
                              }
                           }
                        }

                        scenarioDetails.setHTML(buildScenarioPropertyDetails(giftScenarioProperties, zoomedOutImageUrl));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        scenarioDetails.setHTML(buildScenarioPropertyDetailsFailedThumbnails(giftScenarioProperties));
                    }
                });
    }

    /**
     * Selects the scenario group item in {@link #unityScenarios} that has the provided path and
     * rebuilds the {@link #scenarioDetails}.
     * 
     * @param scenarioMapPath the path to the scenario map directory that contains the scenario to
     *        be loaded.
     */
    private void selectUnityScenarioByParentPath(String scenarioMapPath) {
        if (StringUtils.isBlank(scenarioMapPath)) {
            selectUnityScenario(null);
            return;
        }

        for (Entry<LinkedGroupItem, GiftScenarioProperties> entrySet : groupItemToPropertyMap.entrySet()) {
            if (StringUtils.equalsIgnoreCase(scenarioMapPath, entrySet.getValue().getPropertyParentPath())) {
                selectUnityScenario(entrySet.getKey());
                return;
            }
        }
    }

    /**
     * Builds details for the provided property. Also adds images to the gallery if image URLs are
     * provided.
     * 
     * @param property the property to use for the details. Can't be null.
     * @param imageUrl the map/scenario image with the highest zoom level to be shown. If null
     *        or empty a placeholder message will be shown with the intent that this method will be
     *        called again soon with the image to display.
     * @return the {@link SafeHtml} string of the property details.
     */
    private SafeHtml buildScenarioPropertyDetails(GiftScenarioProperties property, String imageUrl) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(bold("Name: ")).appendEscapedLines(property.getName());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Author: ")).appendEscapedLines(property.getAuthor());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Type: ")).appendEscapedLines(property.getTrainingApplicationType().getDisplayName());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Description: ")).appendEscapedLines(property.getDescription());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("userStartLocation: ")).appendEscapedLines(property.getUserStartLocation().toString());
        builder.appendHtmlConstant("<br/>");
        
        mapGallery.clear();
        if (StringUtils.isNotBlank(imageUrl)) {
                GalleryImage gImage = new GalleryImage(imageUrl);
                mapGallery.add(gImage);
        } else {
            // show a placeholder (working...) message that won't be included the next
            // time this method is called (with image urls)
            builder.appendEscapedLines("Retrieving map thumbnails...");
        }

        return builder.toSafeHtml();
    }

    /**
     * Builds details for the provided property with a fixed message mentioning that the
     * map/scenario images could not be retrieved from the server.
     * 
     * @param property the property to use for the details. Can't be null.
     * @return the {@link SafeHtml} string of the property details.
     */
    private SafeHtml buildScenarioPropertyDetailsFailedThumbnails(GiftScenarioProperties property) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(bold("Name: ")).appendEscapedLines(property.getName());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Author: ")).appendEscapedLines(property.getAuthor());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Type: ")).appendEscapedLines(property.getTrainingApplicationType().getDisplayName());
        builder.appendHtmlConstant("<br/>");
        builder.append(bold("Description: ")).appendEscapedLines(property.getDescription());
        builder.appendHtmlConstant("<br/>");
        builder.appendHtmlConstant("<i>Failed to retrieve map thumbnails</i>");

        return builder.toSafeHtml();
    }

    /**
     * Creates a new folder within the training apps lib user directory (e.g.
     * workspace/Public/TrainingAppsLib/user/[folderName])
     * 
     * @param folderName the name of the folder to create within the training apps lib directory
     */
    private void createTrainingAppsLibFolder(String folderName) {
        BsLoadingDialogBox.display("Creating Directory", "Creating the real time assessment directory, please wait...");

        rpcService.createTrainingAppsLibFolder(folderName, GatClientUtility.getUserName(),
                new AsyncCallback<GenericRpcResponse<FileTreeModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("createTrainingAppsLibFolder().onFailure()");
                        }
                        BsLoadingDialogBox.remove();

                        DetailedException e = null;
                        if (caught instanceof DetailedException) {
                            e = (DetailedException) caught;
                        } else {
                            e = new DetailedException("An unexpected error ocurred on the server", caught.getMessage(),
                                    caught);
                        }

                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                e.getErrorStackTrace());
                        dialog.center();
                    }

                    @Override
                    public void onSuccess(GenericRpcResponse<FileTreeModel> result) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("createTrainingAppsLibFolder().onSuccess()");
                        }
                        BsLoadingDialogBox.remove();

                        // copy the DKF template xml
                        copyDKFTemplate(createTrainingApplication(), result.getContent(), copyCallback);
                    }
                });
    }

    @Override
    protected void onCancel() {
        if (creatingNew) {
            deleteTrainingApplicationFiles(editedItem.getCourseObject());
        }
    }

    /**
     * Initializes the ribbon used to select the type of course object.
     */
    private void initRibbon() {
        courseObjectTypeRibbon.setTileHeight(100);
        courseObjectTypeRibbon.addRibbonItem(getTrainingApplicationImage(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS),
                ANDROID_EVENT_LABEL, ANDROID_EVENT_TOOLTIP, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        ribbonChoice = TrainingApplicationEnum.MOBILE_DEVICE_EVENTS;
                        trainingAppIconPanel.setWidget(getTrainingApplicationImage(ribbonChoice));
                        unityScenarioPanel.setVisible(false);
                        checkDkfEditorErrorMsg();
                        showDeckPanelWidget(editPanel);
                        validateAll();
                    }
                });

        courseObjectTypeRibbon.addRibbonItem(getTrainingApplicationImage(TrainingApplicationEnum.UNITY_EMBEDDED),
                UNITY_EVENT_LABEL, UNITY_EVENT_TOOLTIP, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        ribbonChoice = TrainingApplicationEnum.UNITY_EMBEDDED;
                        trainingAppIconPanel.setWidget(getTrainingApplicationImage(ribbonChoice));
                        unityScenarioPanel.setVisible(unityScenarios.getWidgetCount() != 0);
                        checkDkfEditorErrorMsg();
                        showDeckPanelWidget(editPanel);
                        validateAll();
                    }
                });
    }

    /**
     * Creates a new training application based on the user choice.
     * 
     * @return the newly created training application
     */
    private TrainingApplication createTrainingApplication() {
        if (ribbonChoice == null) {
            return null;
        }

        TrainingApplication trainingApp = new TrainingApplication();
        EmbeddedApps embeddedApps = new EmbeddedApps();
        if (ribbonChoice.equals(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS)) {
            EmbeddedApp embeddedApp = new EmbeddedApp();
            embeddedApp.setEmbeddedAppImpl(new MobileApp());

            embeddedApps.setEmbeddedApp(embeddedApp);
        } else if (ribbonChoice.equals(TrainingApplicationEnum.UNITY_EMBEDDED)) {
            // nothing to set into embedded apps for Unity
        } else {
            throw new UnsupportedOperationException(
                    "The ribbon choice is not supported in 'createTrainingApplication()'.");
        }

        trainingApp.setEmbeddedApps(embeddedApps);
        trainingApp.setFinishedWhen(TrainingApplicationStateEnum.STOPPED.getName());
        return trainingApp;
    }

    /**
     * Deletes the given training application object and its associated folder
     * 
     * @param object the training application object to delete
     */
    protected void deleteTrainingApplicationFiles(TrainingApplicationObject object) {
        // if null, nothing to delete
        if (object == null || object.getLibraryPath() == null) {
            return;
        }

        final FileOperationProgressModal fileProgressModal = new FileOperationProgressModal(ProgressType.DELETE);
        fileProgressModal.startPollForProgress();

        rpcService.deleteTrainingApplicationObject(object, GatClientUtility.getUserName(),
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("deleteTrainingApplicationObject().onFailure()");
                        }

                        fileProgressModal.stopPollForProgress(true);

                        DetailedException e = null;
                        if (caught instanceof DetailedException) {
                            e = (DetailedException) caught;
                        } else {
                            e = new DetailedException("An unexpected error ocurred on the server", caught.getMessage(),
                                    caught);
                        }

                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                e.getErrorStackTrace());
                        dialog.center();
                    }

                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("deleteTrainingApplicationObject().onSuccess()");
                        }

                        fileProgressModal.stopPollForProgress(true);

                        if (!result.getWasSuccessful()) {
                            DetailedExceptionSerializedWrapper e = result.getException();
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                    e.getErrorStackTrace());
                            dialog.center();
                        }
                    }
                });
    }

    /**
     * Updates the DKF Editor button with the correct icon and label.
     * 
     * @param isCreate true to set the button to "create" mode; false to set the button to "edit"
     *        mode.
     */
    private void updateDKFEditorButton(boolean isCreate) {
        /* Can only author the scenario if the DKF has not been created yet */
        unityScenariosContainer.setVisible(isCreate);

        dkfEditorButton.setIcon(isCreate ? IconType.PLUS : IconType.PENCIL);
        dkfEditorButton.setText((isCreate ? "Create" : "Edit") + " Real Time Assessment");
        requestValidation(realTimeAssessmentValidation);
    }

    /**
     * Copy the 'simplest' DKF file to use as a default
     * 
     * @param trainingApp the training application to set the DKF
     * @param courseFolderModel the course folder path to put the copied DKF template
     * @param fileCopiedCallback callback that gets triggered when the file completes or fails to
     *        copy
     */
    private void copyDKFTemplate(final TrainingApplication trainingApp, final FileTreeModel courseFolderModel,
            final AsyncCallback<Boolean> fileCopiedCallback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("copyDKFTemplate(" + courseFolderModel.getRelativePathFromRoot() + ")");
        }

        if (fileCopiedCallback == null) {
            throw new IllegalArgumentException("The parameter 'fileCopiedCallback' cannot be null.");
        }

        BsLoadingDialogBox.display("Creating External Application",
                "Creating external application resouces, please wait...");

        final String newName = courseFolderModel.getFileOrDirectoryName();
        CopyTemplateFile action = new CopyTemplateFile(GatClientUtility.getUserName(),
                courseFolderModel.getRelativePathFromRoot(true), newName, AbstractSchemaHandler.DKF_FILE_EXTENSION);
        action.setAppendUUIDToFilename(false);

        SharedResources.getInstance().getDispatchService().execute(action,
                new AsyncCallback<CopyWorkspaceFilesResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.warning("Caught exception while copying dkf template file: '" + caught.toString()
                                    + "' Application will not have simplest DKF file initialized.");
                        }

                        BsLoadingDialogBox.remove();
                        fileCopiedCallback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(CopyWorkspaceFilesResult result) {

                        BsLoadingDialogBox.remove();
                        if (result.isSuccess()) {
                            String dkfFile = result.getCopiedFilename();

                            if (StringUtils.isBlank(dkfFile)) {
                                if (logger.isLoggable(Level.WARNING)) {
                                    logger.warning(
                                            "Failed to copy the dkf template for the external application transition.");
                                }
                                fileCopiedCallback.onSuccess(false);
                                return;
                            } else if (logger.isLoggable(Level.INFO)) {
                                logger.info(
                                        "The 'simplest' DKF file was copied successfully to the training application.");
                            }

                            DkfRef dkfRef = new DkfRef();
                            dkfRef.setFile(dkfFile);

                            trainingApp.setTransitionName(newName);
                            trainingApp.setDkfRef(dkfRef);

                            // create new
                            TrainingAppCourseObjectWrapper newTrainingAppCourseObjWrapper = new TrainingAppCourseObjectWrapper(
                                    trainingApp, null);
                            TrainingApplicationObject newTrainingAppObject = new TrainingApplicationObject(
                                    courseFolderModel, newTrainingAppCourseObjWrapper);
                            editedItem.setCourseObject(newTrainingAppObject);

                            // create the training application xml
                            saveTACourseObject(newTrainingAppObject, null, null);
                            updateDKFEditorButton(false);
                            fileCopiedCallback.onSuccess(true);
                        } else {
                            if (logger.isLoggable(Level.WARNING)) {
                                logger.warning(
                                        "Failed to copy the dkf template for the external application transition.");
                            }
                            fileCopiedCallback.onSuccess(false);
                        }
                    }
                });
    }

    /**
     * Displays the provided widget from the {@link #deckPanel}. Hides the other widgets in the
     * {@link #deckPanel}.
     * 
     * @param widgetToShow the widget to display.
     */
    private void showDeckPanelWidget(Widget widgetToShow) {
        setSaveButtonVisible(widgetToShow == editPanel);
        deckPanel.showWidget(deckPanel.getWidgetIndex(widgetToShow));
    }

    @Override
    protected void populateEditor(CourseObjectWrapper obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor()");
        }

        /* set the item being edited */
        editedItem = obj;

        /* creating new or editing */
        creatingNew = obj.getCourseObject() == null || obj.getCourseObject().getTrainingApplication() == null
                || obj.getCourseObject().getTrainingApplication().getTrainingApplicationObj() == null;

        /* reset panel to correct state */
        savedTrainingAppName = getObjectName(obj);
        updateDKFEditorButton(creatingNew);
        noNameErrorMsg.setVisible(false);
        dkfEditorErrorMsg.setVisible(false);
        editedObjectParentFolder = creatingNew ? null : obj.getCourseObject().getLibraryPath();
        workspaceLabel.setHTML(creatingNew ? Constants.EMPTY
                : "Workspace: " + bold(editedObjectParentFolder.getRelativePathFromRoot()));

        nameTextBox.setValue(savedTrainingAppName);
        ValueChangeEvent.fire(nameTextBox, savedTrainingAppName);

        if (creatingNew && filterByType == null) {
            /* Go to ribbon */

            ribbonChoice = null;
            unityScenarioPanel.setVisible(false);
            selectUnityScenario(null);
            showDeckPanelWidget(courseObjectTypeRibbon);
        } else {
            /* Go to edit panel */

            String unityScenarioMapPath = null;
            if (creatingNew) {
                /* filterByType guaranteed to be non-null at this point */
                ribbonChoice = filterByType;
            } else {
                /* training application guaranteed to be non-null at this point */
                TrainingApplication ta = obj.getCourseObject().getTrainingApplication().getTrainingApplicationObj();
                ribbonChoice = filterByType != null ? filterByType : TrainingAppUtil.getTrainingAppType(ta);

                AuthoringSupportElements supportElements = ta.getAuthoringSupportElements();
                if (supportElements != null
                        && StringUtils.isNotBlank(supportElements.getTrainingAppsScenarioMapPath())) {
                    unityScenarioMapPath = supportElements.getTrainingAppsScenarioMapPath();
                }
            }

            /* Update unity scenario widgets */
            final boolean isUnity = TrainingApplicationEnum.UNITY_EMBEDDED.equals(ribbonChoice);
            unityScenarioPanel.setVisible(isUnity && unityScenarios.getWidgetCount() != 0);
            selectUnityScenarioByParentPath(isUnity ? unityScenarioMapPath : null);
            checkDkfEditorErrorMsg();

            /* Set icon */
            Widget icon = getTrainingApplicationImage(ribbonChoice);
            trainingAppIconPanel.setWidget(icon);

            showDeckPanelWidget(editPanel);
        }
    }

    @Override
    protected void applyEdits(final CourseObjectWrapper obj) {
        // handled by applyEdits(obj, command);
    }

    @Override
    protected void applyEdits(CourseObjectWrapper obj, final Command command) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits()");
        }

        // should never get nulls here; if we do something strange happened.
        final TrainingApplicationObject courseObject = obj.getCourseObject();
        if (courseObject == null || courseObject.getLibraryPath() == null) {
            throw new DetailedException("The wrapped course object doesn't exist.",
                    "Somehow the GIFT Wrap course does not exist. This should never happen and needs to be investigated.",
                    null);
        }

        final TrainingApplication trainingApplicationObj = courseObject.getTrainingApplication()
                .getTrainingApplicationObj();

        // create copy of path
        final FileTreeModel oldPath = FileTreeModel
                .createFromRawPath(courseObject.getLibraryPath().getRelativePathFromRoot());

        final String newName = nameTextBox.getValue();
        final String oldName = oldPath.getFileOrDirectoryName();
        if (!StringUtils.equalsIgnoreCase(oldName, newName)) {
            // update parent folder name reference
            courseObject.getLibraryPath().setFileOrDirectoryName(newName);

            // update training app transition name
            trainingApplicationObj.setTransitionName(newName);

            // update training app dkf file reference
            final DkfRef dkfRef = trainingApplicationObj.getDkfRef();
            if (StringUtils.equals(dkfRef.getFile(), oldName + AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
                dkfRef.setFile(dkfRef.getFile().replaceFirst(oldName, newName));
            }
        }

        // save with new name
        saveTACourseObject(courseObject, oldPath, command);
    }

    /**
     * Sets the selected scenario into the training application for the item being edited.
     */
    private void setSelectedScenarioIntoTrainingApplication() {
        /* Only set the selected scenario if creating a new Unity application */
        if (!creatingNew || !TrainingApplicationEnum.UNITY_EMBEDDED.equals(ribbonChoice)) {
            return;
        }

        /* Verify training application exists */
        if (editedItem == null || editedItem.getCourseObject() == null
                || editedItem.getCourseObject().getTrainingApplication() == null
                || editedItem.getCourseObject().getTrainingApplication().getTrainingApplicationObj() == null) {
            return;
        }

        TrainingApplication ta = editedItem.getCourseObject().getTrainingApplication().getTrainingApplicationObj();
        for (Entry<LinkedGroupItem, GiftScenarioProperties> entrySet : groupItemToPropertyMap.entrySet()) {
            // check if this group item is the selected item
            if (!entrySet.getKey().isActive()) {
                continue;
            }

            // create authoring support elements object if it doesn't exist
            if (ta.getAuthoringSupportElements() == null) {
                ta.setAuthoringSupportElements(new AuthoringSupportElements());
            }

            // set scenario map path
            ta.getAuthoringSupportElements()
                    .setTrainingAppsScenarioMapPath(entrySet.getValue().getPropertyParentPath());

            // exit loop
            break;
        }
    }

    /**
     * Save the training application
     * 
     * @param courseObject the updated training application to save
     * @param oldPath the parent path of the training application that was changed. The new object
     *        data contains the new path so it cannot be trusted to find the location of the old
     *        data. The root of the path must be a workspace sub-folder (e.g. Public,
     *        &lt;username&gt;).
     * @param command the command to execute once the saving is complete.
     */
    private void saveTACourseObject(final TrainingApplicationObject courseObject, FileTreeModel oldPath,
            final Command command) {
        BsLoadingDialogBox.display("Saving Real Time Assessment",
                "Please wait while the real time assessment is being saved.");

        // perform course folder and training app rename
        rpcService.saveTACourseObject(courseObject, oldPath, GatClientUtility.getUserName(),
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("saveTACourseObject().onSuccess()");
                        }
                        BsLoadingDialogBox.remove();

                        // update the saved training app name
                        savedTrainingAppName = courseObject.getLibraryPath().getFileOrDirectoryName();
                        if (command != null) {
                            command.execute();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("saveTACourseObject().onFailure()");
                        }
                        BsLoadingDialogBox.remove();

                        DetailedException e = null;
                        if (caught instanceof DetailedException) {
                            e = (DetailedException) caught;
                        } else {
                            e = new DetailedException("An unexpected error ocurred on the server", caught.getMessage(),
                                    caught);
                        }

                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                e.getErrorStackTrace());
                        dialog.center();

                        if (command != null) {
                            command.execute();
                        }
                    }
                });
    }

    /**
     * Shows the DKF editor dialog with the provided training application object.
     * 
     * @param courseObjectItem the training application object to populate the DKF editor.
     */
    protected abstract void showDkfEditorDialog(TrainingApplicationObject courseObjectItem);

    /**
     * Checks if the provided name is a duplicate of another item in the list.
     * 
     * @param newName the name to check
     * @return true if the name is valid; false otherwise.
     */
    private boolean isNameDuplicate(String newName) {
        if (StringUtils.isBlank(newName)) {
            return false;
        }

        // make sure we aren't creating a duplicate
        for (CourseObjectWrapper courseObjectItem : getParentItemListEditor().getItems()) {
            if (courseObjectItem == editedItem) {
                continue;
            }

            // name is in conflict, need to check path
            if (StringUtils.equalsIgnoreCase(newName, getObjectName(courseObjectItem))) {
                final FileTreeModel objPath = courseObjectItem.getCourseObject().getLibraryPath();

                // check the edited item path
                FileTreeModel newPath;
                if (editedItem != null && editedItem.getCourseObject() != null
                        && editedItem.getCourseObject().getLibraryPath() != null) {
                    FileTreeModel currentPath = editedItem.getCourseObject().getLibraryPath();
                    newPath = currentPath.getParentTreeModel().getModelFromRelativePath(newName);
                } else if (userFolder != null) {
                    // item hasn't been created yet. Check user folder path.
                    newPath = userFolder.getModelFromRelativePath(newName);
                } else {
                    /* since we do not have a library path or user folder yet, assume the paths are
                     * in conflict */
                    return true;
                }

                // compare new path with object path, if the same then we have a duplicate
                if (StringUtils.equalsIgnoreCase(objPath.getRelativePathFromRoot(),
                        newPath.getRelativePathFromRoot())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * The training application type allowed to be selected. If null, allow any of the types to be
     * selected.
     * 
     * @param wrapType the type of the training application that is allowed to be selected.
     */
    public void setFilterByType(TrainingApplicationEnum wrapType) {
        this.filterByType = wrapType;
        refreshFilteredScenarioProperties();
    }

    /**
     * Gets the name of the provided course object.
     * 
     * @param courseObjectItem the course object to parse.
     * @return the name of the course object.
     */
    public static String getObjectName(CourseObjectWrapper courseObjectItem) {
        if (courseObjectItem != null && courseObjectItem.getCourseObject() != null
                && courseObjectItem.getCourseObject().getTrainingApplication() != null) {
            TrainingAppCourseObjectWrapper trainingAppObjectWrapper = courseObjectItem.getCourseObject()
                    .getTrainingApplication();

            if (trainingAppObjectWrapper.getValidationException() != null) {
                return trainingAppObjectWrapper.getInvalidObjectIdentifier();
            } else if (trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                return trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
            }
        }

        return null;
    }

    /**
     * Gets the image for the provided training application type.
     * 
     * @param trainingApplicationType the training application type to indicate which image to use.
     *        Can't be null.
     * @return the image for the training application.
     */
    public static Image getTrainingApplicationImage(TrainingApplicationEnum trainingApplicationType) {
        if (trainingApplicationType == null) {
            throw new IllegalArgumentException("The parameter 'trainingApplicationType' cannot be null.");
        }

        Image image;
        if (trainingApplicationType.equals(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS)) {
            image = new Image("images/mobile-app.png");
        } else if (trainingApplicationType.equals(TrainingApplicationEnum.UNITY_EMBEDDED)) {
            image = new Image("images/Unity.png");
        } else {
            throw new UnsupportedOperationException(
                    "The training application type is not supported in 'getTrainingApplicationIcon()'.");
        }

        image.setSize("24px", "24px");
        return image;
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        courseObjectTypeRibbon.setReadonly(isReadonly);
        nameTextBox.setEnabled(!isReadonly);
        dkfEditorButton.setEnabled(!isReadonly);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameTextBoxValidation);
        validationStatuses.add(realTimeAssessmentValidation);
    }

    @Override
    protected void performRemotePreSaveValidation(final ValidationCallback validationCallback) {
        final String newName = nameTextBox.getValue();

        // name isn't set or name didn't change, skip the check
        if (StringUtils.isBlank(newName) || StringUtils.equalsIgnoreCase(savedTrainingAppName, newName)) {
            validationCallback.validationPassed();
            return;
        }

        // check the edited item path
        FileTreeModel newPath;
        if (editedItem != null && editedItem.getCourseObject() != null
                && editedItem.getCourseObject().getLibraryPath() != null) {
            FileTreeModel currentPath = editedItem.getCourseObject().getLibraryPath();
            newPath = currentPath.getParentTreeModel().getModelFromRelativePath(newName);
        } else if (userFolder != null) {
            // item hasn't been created yet. Check user folder path.
            newPath = userFolder.getModelFromRelativePath(newName);
        } else {
            throw new DetailedException(
                    "Unable to check if the folder name is a duplicate or not because the path cannot be found.",
                    "Unable to check if the folder name is a duplicate or not because the path cannot be found.", null);
        }

        rpcService.checkTrainingApplicationPath(newPath, GatClientUtility.getUserName(),
                new AsyncCallback<GenericRpcResponse<TrainingApplicationObject>>() {
                    final String errMsg = "The name '" + newName + "' is a duplicate of another real time assessment.";

                    @Override
                    public void onSuccess(GenericRpcResponse<TrainingApplicationObject> response) {
                        if (response.getWasSuccessful()) {
                            TrainingApplicationObject conflictObj = response.getContent();
                            if (conflictObj == null) {
                                validationCallback.validationPassed();
                            } else {
                                nameTextBoxValidation.setErrorMessage(errMsg);
                                nameTextBoxValidation.setInvalid();
                                // manually update the validation status
                                updateValidationStatus(nameTextBoxValidation);
                                validationCallback.validationFailed();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        nameTextBoxValidation.setErrorMessage(errMsg);
                        nameTextBoxValidation.setInvalid();
                        // manually update the validation status
                        updateValidationStatus(nameTextBoxValidation);
                        validationCallback.validationFailed();
                    }
                });
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameTextBoxValidation.equals(validationStatus)) {
            // only validate if the edit panel is visible
            if (deckPanel.getVisibleWidget() != deckPanel.getWidgetIndex(editPanel)) {
                nameTextBoxValidation.setValid();
                return;
            }

            String newName = nameTextBox.getText();
            if (StringUtils.isBlank(newName)) {
                nameTextBoxValidation.setErrorMessage(DEFAULT_NAME_VALIDATION_MSG);
                nameTextBoxValidation.setInvalid();
                return;
            } else if (StringUtils.equalsIgnoreCase(newName, UNKNOWN_NAME)) {
                nameTextBoxValidation.setErrorMessage("Can't use the name '" + UNKNOWN_NAME + "'.");
                nameTextBoxValidation.setInvalid();
                return;
            } else if (isNameDuplicate(newName)) {
                nameTextBoxValidation
                        .setErrorMessage("The name '" + newName + "' is a duplicate of another real time assessment.");
                nameTextBoxValidation.setInvalid();
                return;
            }

            // check if the name is valid
            String fileNameErrorMsg = DocumentUtil.validateFileName(newName);
            if (fileNameErrorMsg != null) {
                nameTextBoxValidation.setErrorMessage(
                        "The name '" + newName + "' is invalid because " + fileNameErrorMsg.toLowerCase());
                nameTextBoxValidation.setInvalid();
                return;
            }

            // no validation errors
            nameTextBoxValidation.setValid();
        } else if (realTimeAssessmentValidation.equals(validationStatus)) {
            if (deckPanel.getVisibleWidget() != deckPanel.getWidgetIndex(editPanel)) {
                realTimeAssessmentValidation.setValid();
                return;
            }

            realTimeAssessmentValidation.setValidity(editedItem.getCourseObject() != null);
        }
    }

    @Override
    protected boolean validate(CourseObjectWrapper obj) {
        TrainingApplicationObject trainingAppObj = obj.getCourseObject();
        if (trainingAppObj == null || trainingAppObj.getLibraryPath() == null
                || trainingAppObj.getTrainingApplication() == null
                || trainingAppObj.getTrainingApplication().getTrainingApplicationObj() == null) {
            return false;
        }

        TrainingApplication trainingApp = trainingAppObj.getTrainingApplication().getTrainingApplicationObj();
        if (StringUtils.isBlank(trainingApp.getTransitionName())) {
            return false;
        } else if (trainingApp.getDkfRef() == null || StringUtils.isBlank(trainingApp.getDkfRef().getFile())) {
            return false;
        }

        return true;
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }
}
