/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.AuthoringSupportElements;
import generated.course.TrainingApplication;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModalCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.shared.wrap.TrainingApplicationObject;

/**
 * The typical landing page for GIFT Wrap. End users can use this page to view the list of available
 * training application objects, create new objects, and delete existing objects.
 *
 * @author nroberts
 */
public class GIFTWrapHome extends AbstractBsWidget {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GIFTWrapHome.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static GIFTWrapHomeUiBinder uiBinder = GWT.create(GIFTWrapHomeUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface GIFTWrapHomeUiBinder extends UiBinder<Widget, GIFTWrapHome> {
    }

    /** The placeholder string for the {@link #itemListEditor} */
    private final static String DEFAULT_TABLE_PLACEHOLDER = "No real time assessments exist.";

    /**
     * The panel to show within the {@link #itemListEditor} when the objects are being retrieved and
     * loaded
     */
    private final FlowPanel loadingPanel = new FlowPanel();
    {
        Icon loadingIcon = new Icon(IconType.REFRESH);
        loadingIcon.setSpin(true);
        loadingIcon.setSize(IconSize.TIMES3);

        Label emptyLabel = new Label("Loading objects...");
        loadingPanel.add(loadingIcon);
        loadingPanel.add(emptyLabel);
    }

    /** The name shown to the user for the course object if it is unknown */
    private static final String UNKNOWN_NAME = "UNKNOWN";

    /** Creates a remote service proxy to talk to the server-side RPC service. */
    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

    /** The label that is displayed above the {@link #itemListEditor} */
    @UiField
    protected Label tableLabel;

    /** The {@link ItemEditor} used for the {@link #itemListEditor} */
    private final CourseObjectItemEditor itemEditor = new CourseObjectItemEditor() {
        @Override
        protected void showDkfEditorDialog(TrainingApplicationObject courseObjectItem) {
            try {
                GIFTWrapHome.this.showDkfEditorDialog(courseObjectItem);
            } catch (Exception e) {
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "A problem occurred when trying to open the real-time assessment.", e.getMessage(),
                        DetailedException.getFullStackTrace(e));
                dialog.center();
            }
        }
    };

    /** The table that contains the existing course objects */
    @UiField(provided = true)
    protected ItemListEditor<CourseObjectWrapper> itemListEditor = new ItemListEditor<CourseObjectWrapper>(itemEditor);

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(itemListEditor);

    /** The container to hold the DKF editor dialog */
    @UiField
    protected SimplePanel dialogContainer;

    /**
     * Whether or not this client has requested the list of training application objects from the
     * server at least once
     */
    private boolean hasInitializedTrainingAppObjects = false;

    /** The add button for the {@link #itemListEditor} */
    protected final Widget addButton;

    /**
     * Creates the GIFT Wrap home page
     */
    public GIFTWrapHome() {
        this(null);
    }

    /**
     * Creates the GIFT Wrap home page
     *
     * @param trainingApplicationType the training application type to be viewed and selected in the
     *        {@link #itemListEditor}. If null, all types will be viewable.
     */
    protected GIFTWrapHome(final TrainingApplicationEnum trainingApplicationType) {
        initWidget(uiBinder.createAndBindUi(this));

        itemListEditor.setFields(buildEditorItemFields());
        itemListEditor.setSaveButtonText("Save");
        addButton = itemListEditor.addCreateListAction("Click here to create a new real time assessment",
                new CreateListAction<CourseObjectWrapper>() {
                    @Override
                    public CourseObjectWrapper createDefaultItem() {
                        return new CourseObjectWrapper();
                    }
                });
        itemListEditor.setRemoveItemStringifier(new Stringifier<CourseObjectWrapper>() {
            @Override
            public String stringify(CourseObjectWrapper courseObjectItem) {
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

                return "this real time assessment";
            }
        });
        itemListEditor.addListChangedCallback(new ListChangedCallback<CourseObjectWrapper>() {
            @Override
            public void listChanged(ListChangedEvent<CourseObjectWrapper> event) {
                List<CourseObjectWrapper> affectedItems = event.getAffectedItems();
                if (event.getActionPerformed() == ListAction.REMOVE) {
                    for (CourseObjectWrapper wrapper : affectedItems) {
                        itemEditor.deleteTrainingApplicationFiles(wrapper.getCourseObject());
                    }
                }
            }
        });

        if (trainingApplicationType != null) {
            itemEditor.setFilterByType(trainingApplicationType);
        }

        updateTrainingAppObjects(trainingApplicationType);

        // needs to be called last
        itemListEditor.initValidationComposite(validations);
    }

    /**
     * Builds the item fields for the course object table.
     *
     * @return the {@link ItemField item fields} for the course object table columns.
     */
    protected List<ItemField<CourseObjectWrapper>> buildEditorItemFields() {
        ItemField<CourseObjectWrapper> applicationTypeField = new ItemField<CourseObjectWrapper>(null, "1%") {
            @Override
            public Widget getViewWidget(CourseObjectWrapper courseObjectItem) {
                SimplePanel viewPanel = new SimplePanel();
                if (courseObjectItem != null && courseObjectItem.getCourseObject() != null
                        && courseObjectItem.getCourseObject().getTrainingApplication() != null) {

                    TrainingAppCourseObjectWrapper trainingapp = courseObjectItem.getCourseObject()
                            .getTrainingApplication();

                    TrainingApplicationEnum type = TrainingAppUtil
                            .getTrainingAppType(trainingapp.getTrainingApplicationObj());
                    if (TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(type)) {
                        Image mobileImage = CourseObjectItemEditor.getTrainingApplicationImage(type);
                        viewPanel.setWidget(new Tooltip(mobileImage, "Android Event Application"));
                    } else if (TrainingApplicationEnum.UNITY_EMBEDDED.equals(type)) {
                        Image unityImage = CourseObjectItemEditor.getTrainingApplicationImage(type);
                        viewPanel.setWidget(new Tooltip(unityImage, "Unity Application"));
                    } else {
                        Image customIcon = new Image("images/transitions/ta.png");
                        customIcon.setSize("24px", "24px");
                        viewPanel.setWidget(new Tooltip(customIcon, "Custom Application"));
                    }
                }

                return viewPanel;
            }
        };

        ItemField<CourseObjectWrapper> objectNameField = new ItemField<CourseObjectWrapper>(null, "75%") {
            @Override
            public Widget getViewWidget(CourseObjectWrapper courseObjectItem) {
                String objectName = CourseObjectItemEditor.getObjectName(courseObjectItem);
                return new HTML(bold(objectName != null ? objectName : UNKNOWN_NAME));
            }
        };

        ItemField<CourseObjectWrapper> objectWorkspaceField = new ItemField<CourseObjectWrapper>(null, "100%") {
            @Override
            public Widget getViewWidget(CourseObjectWrapper courseObjectItem) {
                String parentFolder;
                if (courseObjectItem.getCourseObject() != null
                        && courseObjectItem.getCourseObject().getLibraryPath() != null) {
                    parentFolder = courseObjectItem.getCourseObject().getLibraryPath().getParentTreeModel()
                            .getFileOrDirectoryName();
                } else {
                    parentFolder = UNKNOWN_NAME;
                }

                return new HTML(parentFolder);
            }
        };

        return Arrays.asList(applicationTypeField, objectNameField, objectWorkspaceField);
    }

    /**
     * Sets the placeholder text for the {@link #itemListEditor}.
     *
     * @param isLoading true to set the placeholder to a "loading" screen within the table; false to
     *        set the placeholder to the default "no items" text.
     */
    private void setListEditorPlaceholder(boolean isLoading) {
        itemListEditor.setAddButtonEnabled(addButton, !isLoading);

        if (isLoading) {
            itemListEditor.setPlaceholder(SafeHtmlUtils.fromTrustedString(loadingPanel.toString()));
        } else {
            itemListEditor.setPlaceholder(DEFAULT_TABLE_PLACEHOLDER);
        }
    }

    /**
     * Shows the dkf editor dialog.
     *
     * @param courseObjectItem the course object to open in the DKF editor.
     * @throws Exception if no training application type could be found AND the training application
     *         object is not using custom interop inputs
     */
    private void showDkfEditorDialog(TrainingApplicationObject courseObjectItem) throws Exception {
        if (courseObjectItem == null || courseObjectItem.getTrainingApplication() == null
                || courseObjectItem.getTrainingApplication().getTrainingApplicationObj() == null) {
            logger.warning("Not showing the DKF editor because the application is null");
            itemListEditor.cancelEditing();
            return;
        }
        BsLoadingDialogBox.display("Loading Real-Time Assessment Editor",
                "Please wait while the editor is being loaded.");

        TrainingAppCourseObjectWrapper trainingAppCourseObjectWrapper = courseObjectItem.getTrainingApplication();
        if (trainingAppCourseObjectWrapper.getValidationException() != null) {
            logger.warning("Not showing the DKF editor because there is a validation exception: "
                    + trainingAppCourseObjectWrapper.getValidationException());
            itemListEditor.cancelEditing();
            return;
        }

        // should be the subfolder of workspace (e.g. Public/TrainingAppsLib/...)
        String courseFolderPath = courseObjectItem.getLibraryPath().getRelativePathFromRoot(true);
        exposeCoursePathToChildFrames(courseFolderPath);

        /* Expose the global survey's composer's JavaScript functions to any sub-editor iframe
         * windows so that all of the GAT's editors use the same survey composer instance. */
        SurveyEditorModal.exposeNativeFunctions();

        GatClientUtility.setReadOnly(false);

        final TrainingApplication trainingApplicationObj = trainingAppCourseObjectWrapper.getTrainingApplicationObj();
        String dkfFileName = trainingApplicationObj.getDkfRef().getFile();

        TrainingApplicationEnum type = TrainingAppUtil.getTrainingAppType(trainingApplicationObj);

        // Pass the course survey context id into the url via a parameter.
        HashMap<String, String> paramMap = new HashMap<String, String>();
        // just need a non-zero value here for survey context id.
        paramMap.put(DkfPlace.PARAM_SURVEYCONTEXTID, BigInteger.ONE.toString());
        paramMap.put(DkfPlace.PARAM_IMPORTEDDKF, Boolean.FALSE.toString());
        paramMap.put(DkfPlace.PARAM_READONLY, Boolean.FALSE.toString());
        paramMap.put(DkfPlace.PARAM_TRAINING_APP, type.toString());
        paramMap.put(DkfPlace.PARAM_GIFTWRAP, Boolean.TRUE.toString());
        setAuthoringSupportElements(trainingApplicationObj.getAuthoringSupportElements());

        final CourseObjectModal dkfEditorDialog = new CourseObjectModal();
        dialogContainer.setWidget(dkfEditorDialog);
        dkfEditorDialog.getElement().getStyle().setZIndex(1070);

        dkfEditorDialog.setSaveButtonHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GatClientUtility.saveEmbeddedCourseObject();
                dkfEditorDialog.stopEditor();
                CourseObjectModal.resetEmbeddedSaveObject();
                setGIFTWrapDialogCancelButtonVisibility(true);
                
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this gift wrap object being edited
                }
            }
        });

        dkfEditorDialog.setCancelCallback(new CourseObjectModalCancelCallback() {
            @Override
            public void onCancelModal(boolean removeSelection) {
                setGIFTWrapDialogCancelButtonVisibility(true);
            }
        });

        setGIFTWrapDialogCancelButtonVisibility(false);
        String url = GatClientUtility.getModalDialogUrlWithParams(courseFolderPath, dkfFileName, paramMap);
        dkfEditorDialog.setCourseObjectUrl(
                CourseElementUtil.getCourseObjectTypeImgTag(TrainingApplicationEnum.getTrainingAppTypeIcon(type)) + " "
                        + CourseObjectName.DKF.getDisplayName(),
                url);

        BsLoadingDialogBox.remove();

        dkfEditorDialog.show();
    }

    /**
     * Saves the supplied {@link AuthoringSupportElements} to the
     * $wnd.authoringSupportElements variable. This allows the DKF editor to
     * access the elemnets via JSNI.
     *
     * @param authoringSupportElements The {@link AuthoringSupportElements} that
     *        the DKF editor should use.
     */
    private static native void setAuthoringSupportElements(AuthoringSupportElements authoringSupportElements) /*-{
		$wnd.authoringSupportElements = authoringSupportElements;
    }-*/;

    /**
     * Sets the visibility of the cancel button on the GIFT Wrap dialog modal
     *
     * @param visible true to show the cancel button; false to hide it
     */
    protected void setGIFTWrapDialogCancelButtonVisibility(boolean visible) {
        // do nothing, there is no parent dialog to modify
    }

    /**
     * Updates the {@link #itemListEditor} with the latest course object data.
     *
     * @param trainingApplicationType the training application type to be viewed and selected in the
     *        {@link #itemListEditor}. If null, all types will be viewable.
     */
    private void updateTrainingAppObjects(TrainingApplicationEnum trainingApplicationType) {
        // disable add, show loading label, and set table items to empty
        setListEditorPlaceholder(true);
        itemListEditor.setItems(new ArrayList<CourseObjectWrapper>());

        rpcService.getTrainingApplicationObjects(trainingApplicationType, GatClientUtility.getUserName(), null,
                new AsyncCallback<GenericRpcResponse<List<TrainingApplicationObject>>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<List<TrainingApplicationObject>> result) {

                        if (result != null && result.getWasSuccessful()) {
                            if (!hasInitializedTrainingAppObjects) {
                                itemListEditor.setItems(new ArrayList<CourseObjectWrapper>());
                                hasInitializedTrainingAppObjects = true;
                            }

                            if (result.getContent() != null) {
                                Collections.sort(result.getContent(), new Comparator<TrainingApplicationObject>() {
                                    @Override
                                    public int compare(TrainingApplicationObject o1, TrainingApplicationObject o2) {

                                        String name1 = null;
                                        if (o1 != null && o1.getTrainingApplication() != null) {
                                            TrainingAppCourseObjectWrapper trainingAppObjectWrapper = o1
                                                    .getTrainingApplication();
                                            if (trainingAppObjectWrapper.getValidationException() != null) {
                                                name1 = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                                            } else if (trainingAppObjectWrapper.getTrainingApplicationObj()
                                                    .getTransitionName() != null) {
                                                name1 = trainingAppObjectWrapper.getTrainingApplicationObj()
                                                        .getTransitionName();
                                            }
                                        }

                                        String name2 = null;
                                        if (o2 != null && o2.getTrainingApplication() != null) {
                                            TrainingAppCourseObjectWrapper trainingAppObjectWrapper = o2
                                                    .getTrainingApplication();
                                            if (trainingAppObjectWrapper.getValidationException() != null) {
                                                name2 = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                                            } else if (trainingAppObjectWrapper.getTrainingApplicationObj()
                                                    .getTransitionName() != null) {
                                                name2 = trainingAppObjectWrapper.getTrainingApplicationObj()
                                                        .getTransitionName();
                                            }
                                        }

                                        if (name1 != null && name2 != null) {
                                            return name1.compareTo(name2);
                                        } else if (name1 != null && name2 == null) {
                                            return -1;
                                        } else if (name1 == null && name2 != null) {
                                            return 1;
                                        }

                                        return 0;
                                    }
                                });

                                List<CourseObjectWrapper> wrappers = new ArrayList<CourseObjectWrapper>();
                                for (TrainingApplicationObject appObject : result.getContent()) {
                                    if (appObject.getTrainingApplication() != null
                                            && appObject.getTrainingApplication().getTrainingApplicationObj() != null) {
                                        TrainingApplication trainingApp = appObject.getTrainingApplication()
                                                .getTrainingApplicationObj();
                                        if (CourseElementUtil.isGIFTWrapSupported(trainingApp)) {
                                            wrappers.add(new CourseObjectWrapper(appObject));
                                        }
                                    }
                                }

                                itemListEditor.setItems(wrappers);
                            }

                        } else if (result != null && result.getException() != null) {
                            DetailedExceptionSerializedWrapper e = result.getException();
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                    e.getErrorStackTrace());
                            dialog.center();
                        }

                        setListEditorPlaceholder(false);
                    }

                    @Override
                    public void onFailure(Throwable caught) {

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

                        setListEditorPlaceholder(false);
                    }
                });
    }

    /**
     * Exposes the given course path to any editor frames opened within this editor. This is used to
     * allow sub-editors opened in dialogs to locate the course folder where course object files
     * should be saved to.
     *
     * @param path the path to expose
     */
    private native void exposeCoursePathToChildFrames(String path)/*-{
		$wnd.editorBaseCoursePath = path;
    }-*/;
}
