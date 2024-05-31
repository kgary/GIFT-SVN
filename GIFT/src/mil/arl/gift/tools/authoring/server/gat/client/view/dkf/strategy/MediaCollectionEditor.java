/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.ImageProperties;
import generated.dkf.LessonMaterialList;
import generated.dkf.Media;
import generated.dkf.MidLessonMedia;
import generated.dkf.PDFProperties;
import generated.dkf.SlideShowProperties;
import generated.dkf.Strategy;
import generated.dkf.StrategyHandler;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import generated.dkf.VideoProperties;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFileExists;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.WorkspaceFilesExistResult;

/**
 * An sub-editor that modifies media collections (i.e. {@link LessonMaterialList LessonMaterialLists}) for the DKF editor
 *
 * @author nroberts
 */
public class MediaCollectionEditor extends ScenarioValidationComposite {

    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(MediaCollectionEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static MediaCollectionEditorUiBinder uiBinder = GWT.create(MediaCollectionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface MediaCollectionEditorUiBinder extends UiBinder<Widget, MediaCollectionEditor> {
    }

    /** The base URL to build the full preview URL from */
    private static final String BASE_PREVIEW_URL = "LessonMaterialPreview.html";

    /** Button to allow the author to validate the media in {@link #mediaList} */
    @UiField
    protected Button validateMediaButton;

    /** The list of authored media */
    @UiField(provided = true)
    protected ItemListEditor<Media> mediaList = new ItemListEditor<>(new AddMediaPanel());

    /** A dialog that asks the user how to preview the media uri. */
    private ModalDialogBox previewPrompt = new ModalDialogBox();

    /** The previewPrompt text. */
    private HTML promptText = new HTML();

    /** The url to the course folder. */
    private String courseFolderUrl;

    /** The help drop down panel for the preview dialog. */
    private final DisclosurePanel error1Panel = new DisclosurePanel(" HTTP ERROR: 404?");

    /** The help drop down panel for the preview dialog. */
    private final DisclosurePanel error2Panel = new DisclosurePanel(" Neither options work?");

    /** The media object to use in the preview dialog when it is shown*/
    private Media mediaToPreview = null;

    /** The path to the course folder. */
    private String courseFolderPath = GatClientUtility.getBaseCourseFolderPath();

    /** The full preview URL including the GAT host from the server */
    private static String previewUrl = BASE_PREVIEW_URL;

    /**
     * Maps the URI (from {@link FilePath#getFilePath()}) of the media object to
     * the {@link GatServiceResult} that was received in response to a
     * {@link WorkspaceFilesExist} request for the URI. The value of
     * {@link GatServiceResult#isSuccess()} determines whether or not the file
     * exists.
     */
    public HashMap<String, GatServiceResult> mediaListValidMap = new HashMap<>();

    /** The strategy that is being editing */
    @SuppressWarnings("unused")
    private Strategy selectedStrategy;

    /** The container for showing validation messages for not having created a media item. */
    private final WidgetValidationStatus mediaValidationStatus;

    /**
     * Creates a new media collection data with no data populated
     */
    public MediaCollectionEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        //define how media items should be added
        Widget createRow = mediaList.addCreateListAction("Click here to add media", new CreateListAction<Media>() {

            @Override
            public Media createDefaultItem() {
                return new Media();
            }
        });

        mediaValidationStatus = new WidgetValidationStatus(createRow, "There must be at least one media item. Please create a media item.");

        /* Initialize the mediaList */
        mediaList.setFields(buildFields());
        mediaList.setActions(buildActions());
        mediaList.addListChangedCallback(new ListChangedCallback<Media>() {
            @Override
            public void listChanged(ListChangedEvent<Media> event) {
                requestValidation(mediaValidationStatus);
            }
        });

        /* build the preview URL */
        previewUrl = GWT.getHostPageBaseURL() + BASE_PREVIEW_URL;

        setupPreviewInterface();
    }

    /**
     * The handler that is invoked when the {@link #validateMediaButton} is
     * clicked.
     *
     * @param event The {@link ClickEvent} containing details about the click on
     *        the {@link #validateMediaButton}. Can't be null.
     */
    @UiHandler("validateMediaButton")
    protected void onValidateMediaButtonClick(ClickEvent event) {
        validateMediaElements(true);
    }

    /**
     * Constructs each {@link ItemField} for the {@link #mediaList}.
     *
     * @return The {@link Iterable} that contains each {@link ItemField} for the
     *         {@link #mediaList}. Can't be null.
     */
    private Iterable<ItemField<Media>> buildFields() {
        List<ItemField<Media>> fields = new ArrayList<>();

        /* display an icon for each media item type */
        fields.add(new ItemField<Media>() {

            @Override
            public Widget getViewWidget(Media item) {
                return getMediaIcon(item);
            }
        });

        /* display the name of each media item */
        fields.add(new ItemField<Media>() {

            @Override
            public Widget getViewWidget(Media item) {

                String displayName = StringUtils.isNotBlank(item.getName()) ? item.getName() : "UNKNOWN";

                Label label = new Label(displayName);

                String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + item.getUri();
                GatServiceResult validation = mediaListValidMap.get(filePath);

                if (validation != null && !validation.isSuccess()) {
                    label.getElement().getStyle().setColor("red");
                    label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
                }

                return label;
            }
        });

        /* display icons for any additional configuration details for each media
         * item */
        fields.add(new ItemField<Media>() {

            @Override
            public Widget getViewWidget(Media item) {
                return getMediaConfigDisplay(item);
            }
        });

        /* display the path to each media item */
        fields.add(new ItemField<Media>(null, "100%") {

            @Override
            public Widget getViewWidget(Media item) {

                String url = StringUtils.isNotBlank(item.getUri()) ? item.getUri() : "UNKNOWN";

                Label label = new Label(url);
                label.addStyleName("ellipsisAtFullWidth");

                String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + item.getUri();
                GatServiceResult validation = mediaListValidMap.get(filePath);

                if (validation != null && !validation.isSuccess()) {
                    label.getElement().getStyle().setColor("red");
                    label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
                }

                return label;
            }
        });

        return fields;
    }

    /**
     * Constructs each {@link ItemAction} for the {@link #mediaList}.
     *
     * @return The {@link Iterable} that contains each {@link ItemAction} for
     *         the {@link #mediaList}. Can't be null.
     */
    private Iterable<ItemAction<Media>> buildActions() {
        List<ItemAction<Media>> actions = new ArrayList<>();

        /* allow authors to preview media items */
        actions.add(new ItemAction<Media>() {

            @Override
            public boolean isEnabled(Media item) {

                String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + item.getUri();
                GatServiceResult validation = mediaListValidMap.get(filePath);

                if (validation != null && !validation.isSuccess()) {
                    return false;
                }

                return true;
            }

            @Override
            public String getTooltip(Media item) {
                return "Preview";
            }

            @Override
            public IconType getIconType(Media item) {
                return IconType.EYE;
            }

            @Override
            public void execute(Media item) {
                previewMediaObject(item);
            }
        });

        return actions;
    }

    /**
     * Populates the editor with the contents of the given media collection and begins editing it
     *
     * @param collection the collection to edit
     */
    public void editMediaCollection(LessonMaterialList collection) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editMediaCollection(" + collection + ")");
        }

        if (collection != null) {
            List<Media> collectionCopy = new ArrayList<>(collection.getMedia());
            mediaList.setItems(collectionCopy);
        }
    }

    /**
     * Sets up the user interface used to preview media items
     */
    private void setupPreviewInterface() {

        // Style the preview prompt

        Button fileButton = new Button("In Course Folder");
        Button webButton = new Button("Not in Course Folder");
        Button cancelButton = new Button("Cancel");
        Button helpButton = new Button("Having trouble?");
        VerticalPanel wrapper = new VerticalPanel();

        HTML error1Html = new HTML();
        HTML error2Html = new HTML();

        wrapper.add(promptText);
        wrapper.add(helpButton);
        wrapper.add(error1Panel);
        wrapper.add(error2Panel);

        previewPrompt.setWidget(wrapper);
        previewPrompt.setFooterWidget(cancelButton);
        previewPrompt.setFooterWidget(webButton);
        previewPrompt.setFooterWidget(fileButton);
        previewPrompt.setText("Select a Preview Option");
        previewPrompt.setCloseable(false);
        previewPrompt.setGlassEnabled(true);

        wrapper.setWidth("650px");
        cancelButton.setWidth("160px");
        webButton.setWidth("160px");
        fileButton.setWidth("160px");
        promptText.addStyleName("dialogText");
        error1Html.addStyleName("dialogText");
        error2Html.addStyleName("dialogText");
        error1Panel.getElement().getStyle().setProperty("marginBottom", "10px");
        helpButton.getElement().getStyle().setProperty("padding", "0px 0px 5px 0px");
        helpButton.getElement().getStyle().setProperty("fontFamily", "Arial");

        error1Panel.setContent(error1Html);
        error2Panel.setContent(error2Html);
        error1Panel.setVisible(false);
        error2Panel.setVisible(false);

        error1Html.setHTML("If your preview resulted in an 'HTTP ERROR: 404', try the other button below.");

        error2Html.setHTML("If you are trying to preview an external website, verify that:"
                + "<ul><li>The \"Media URI\" you entered is correct.</li></ul>"
                + "If you are trying to preview a file in your workspace, verify that:"
                + "<ul><li>The file is located in this course folder</li>"
                + "<li>The file name is spelled correctly.</li></ul>");

        helpButton.setType(ButtonType.LINK);
        webButton.setType(ButtonType.PRIMARY);
        fileButton.setType(ButtonType.SUCCESS);
        cancelButton.setType(ButtonType.DANGER);

        // Attach click handlers

        helpButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                error1Panel.setVisible(!error1Panel.isVisible());
                error2Panel.setVisible(!error2Panel.isVisible());
            }

        });

        fileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER) {

                    WarningDialog.info("Coming Soon!", "Previewing files in your course folder is currently not supported. "
                            + "However, you can preview resources that are hosted on other servers (e.g. youtube videos)."
                            + "<br/><br/>Note: This type of preview is supported in the downloadable version of GIFT "
                            + "running in Desktop mode.");

                } else if(mediaToPreview != null){

                    final Media media = mediaToPreview;
                    final String uri = media.getUri();

                    //Checks to make sure the uri doesn't use any parent directory references
                    if(uri.equals("..") ||
                            uri.endsWith("/..") ||
                            uri.startsWith("../") ||
                            uri.contains("/../")) {
                        WarningDialog.error("Invalid path", "The URI must reference a course within the course folder.\nThe uri cannot contain any use of the '..' operator.");
                    }

                    //Checks to make sure the specified file exists
                    else {
                        WorkspaceFileExists action = new WorkspaceFileExists(GatClientUtility.getUserName(),
                                GatClientUtility.getBaseCourseFolderPath() +
                                Constants.FORWARD_SLASH + uri);

                        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                            @Override
                            public void onFailure(Throwable thrown) {
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        thrown.getMessage(),
                                        "The server was unable to complete the check for the existence of the user defined uri",
                                        DetailedException.getFullStackTrace(thrown)
                                );

                                dialog.center();
                            }

                            @Override
                            public void onSuccess(GatServiceResult result) {
                                if(result.isSuccess()) {
                                    if(media.getMediaTypeProperties() instanceof ImageProperties) {

                                        Window.open(previewUrl + "?courseFolder=" + courseFolderUrl + "&url=" + URL.encode(media.getUri()) + "&image=true", "_blank", "");

                                    } else {
                                        Window.open(previewUrl + "?courseFolder=" + courseFolderUrl + "&url=" + URL.encode(media.getUri()), "_blank", "");
                                    }
                                } else {
                                    WarningDialog.error("Preview Failed", result.getErrorMsg());
                                }
                            }

                        });
                    }
                } else {
                    WarningDialog.error("Nothing to preview", "There is no lesson material selected to preview.");
                }
                previewPrompt.hide();
            }

        });

        webButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(mediaToPreview != null){

                    Media media = mediaToPreview;
                    String url = URL.encode(media.getUri());

                    if(!url.startsWith("http")) {
                        url = "http://" + url;
                    }

                    if(media.getMediaTypeProperties() instanceof ImageProperties) {
                        Window.open(previewUrl + "?url=" + url + "&image=true", "_blank", "");
                    } else {
                        Window.open(previewUrl + "?url=" + url, "_blank", "");
                    }

                    previewPrompt.hide();
                }
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                previewPrompt.hide();
            }
        });

        SharedResources.getInstance().getDispatchService().execute(new FetchDomainContentServerAddress(),
                new AsyncCallback<FetchDomainContentServerAddressResult>() {

                    @Override
                    public void onFailure(Throwable t) {
                         logger.warning("FetchDomainContentServerAddressResult returned throwable error: " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(FetchDomainContentServerAddressResult result) {
                        String domainUrl = result.getDomainContentServerAddress();
                        courseFolderUrl = domainUrl + "/workspace/" + courseFolderPath;
                    }

        });
    }

    /**
     * Previews the selected media object.
     *
     * @param media the media to preview
     */
    private void previewMediaObject(Media media) {

        if (media == null) {
            return;
        }

        mediaToPreview = media;

        String url = media.getUri();

        if(StringUtils.isNotBlank(url)) {

            if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {

                // open the url in an iframe with the specified width
                generated.dkf.Size s = ((YoutubeVideoProperties) media.getMediaTypeProperties()).getSize();
                String width = (s == null) ? "81%" : s.getWidth().toString() + "px";
                String height = (s == null) ? "95%" : s.getWidth().toString() + "px";

                url = GatClientUtility.getEmbeddedYouTubeUrl(url);

                Window.open(previewUrl + "?url=" + URL.encodeQueryString(url) + "&height=" + height + "&width=" + width, "_blank", "");

            } else {

                promptText.setHTML(" Is <i>" + url + "</i>  a file in your course folder? Please "
                        + "indicate where this lesson material content is located so that GIFT can correctly "
                        + "render it.<br/><br/>Please note that during course execution, GIFT will "
                        + "automatically select the best way to render lesson material content.<br/><br/>");

                promptText.getElement().getStyle().setProperty("maxWidth", "655px");
                promptText.getElement().getStyle().setProperty("wordWrap", "break-word");

                error1Panel.setOpen(false);
                error2Panel.setOpen(false);
                previewPrompt.center();
            }

        } else {
            WarningDialog.warning("URL Error", "Please provide a URL to preview.");
        }
    }

    /**
     * Makes a call to the server to validate that each of the URIs specified by the users
     * reference valid resources. After the server call the table formatting is reevaluated to
     * delineate between valid and invalid resources
     * @param notifyUser specifies whether or not to notify the user about the actions taking place
     */
    private void validateMediaElements(final boolean notifyUser) {
        if (mediaList.size() == 0) {
            return;
        }

        final Notify notification = notifyUser ?
                Notify.notify("Validating media...") :
                null;
        WorkspaceFilesExist action = new WorkspaceFilesExist(
                GatClientUtility.getUserName(),
                GatClientUtility.getBaseCourseFolderPath(),
                mediaList.getItems());

        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<WorkspaceFilesExistResult>() {

            @Override
            public void onFailure(Throwable t) {
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "There was a problem validating the MediaCollection",
                        t.getMessage(),
                        DetailedException.getFullStackTrace(t));
                dialog.center();
            }

            @Override
            public void onSuccess(WorkspaceFilesExistResult result) {

                for (Map.Entry<FilePath, GatServiceResult> entry : result.getFilesExistResults().entrySet()) {
                    mediaListValidMap.put(entry.getKey().getFilePath(), entry.getValue());
                }

                rerenderValidationState();

                if(notification != null && notifyUser) {
                    notification.hide();
                }

                Notify.notify("Validation for media completed");
            }

        });
    }

    /**
     * Re-renders all of the media items currently being displayed by this widget in order to
     * reflect their validation state
     */
    private void rerenderValidationState() {
        /* Force rebuild the editor if the user requested to validate the media items */
        mediaList.redrawListEditor(true);
    }

    /**
     * Sets the {@link Strategy} being edited.
     *
     * @param strategy the {@link Strategy} being edited. Can't be null.
     */
    public void setStrategyBeingEdited(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setStrategyBeingEdited(" + strategy + ")");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        this.selectedStrategy = strategy;
    }

    /**
     * Resets the editor to be the state before the user interacted with it.
     */
    public void resetEditor() {
        mediaList.setItems(new ArrayList<Media>());
    }

    /**
     * Populates the UI with data from the current {@link MidLessonMedia} type being edited
     *
     * @param midLessonMedia the type being edited
     */
    public void populateMidLessonMedia(MidLessonMedia midLessonMedia) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateMidLessonMedia(" + midLessonMedia + ")");
        }

        if (midLessonMedia.getLessonMaterialList() == null) {
            midLessonMedia.setLessonMaterialList(new LessonMaterialList());
        }

        editMediaCollection(midLessonMedia.getLessonMaterialList());

        if (midLessonMedia.getStrategyHandler() == null) {
            midLessonMedia.setStrategyHandler(new StrategyHandler());
        }
    }

    /**
     * Populates a provided {@link MidLessonMedia} with the values currently
     * within the editor.
     *
     * @param midLessonMedia the media to apply the edits to. Can't be null.
     */
    public void applyEdits(MidLessonMedia midLessonMedia) {
        if (midLessonMedia == null) {
            throw new IllegalArgumentException("The parameter 'midLessonMedia' cannot be null.");
        }

        /* Ensures the provided MidLessonMedia has a LessonMaterialList */
        LessonMaterialList lessonMaterialList = midLessonMedia.getLessonMaterialList();
        if (lessonMaterialList == null) {
            lessonMaterialList = new LessonMaterialList();
            midLessonMedia.setLessonMaterialList(lessonMaterialList);
        }

        /* Clears and populates the media list */
        lessonMaterialList.getMedia().clear();
        for (Media media : mediaList.getItems()) {
            lessonMaterialList.getMedia().add(media);
        }
    }

    /**
     * Gets a widget displaying the media's type
     *
     * @param media the media to display the type for. Can't be null.
     * @return a widget displaying the media's type
     */
    public Widget getMediaIcon(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        }

        FlowPanel panel = new FlowPanel();
        panel.getElement().getStyle().setTextAlign(TextAlign.CENTER);

        if(media.getMediaTypeProperties() instanceof SlideShowProperties) {

            Image image = new Image("images/slideshow_icon.png");
            image.setSize("24px", "24px");

            panel.add(image);

        } else if(media.getMediaTypeProperties() instanceof PDFProperties) {

            Icon icon = new Icon(IconType.FILE_PDF_O);
            icon.getElement().getStyle().setFontSize(20, Unit.PX);

            panel.add(icon);

        } else if(media.getMediaTypeProperties() instanceof WebpageProperties) {

            if(CourseElementUtil.isWebAddress(media)) {

                Icon icon = new Icon(IconType.GLOBE);
                icon.setColor("darkblue");
                icon.getElement().getStyle().setFontSize(20, Unit.PX);

                panel.add(icon);

            } else {

                Icon icon = new Icon(IconType.FILE);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);

                panel.add(icon);
            }

        } else if(media.getMediaTypeProperties() instanceof ImageProperties) {

            Icon icon = new Icon(IconType.IMAGE);
            icon.setColor("rgb(100, 100, 100)");
            icon.getElement().getStyle().setFontSize(20, Unit.PX);

            panel.add(icon);
            
        } else if(media.getMediaTypeProperties() instanceof VideoProperties) {

            Icon icon = new Icon(IconType.FILE_VIDEO_O);
            icon.getElement().getStyle().setFontSize(20, Unit.PX);

            panel.add(icon);

        } else if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {

            Icon icon = new Icon(IconType.YOUTUBE_PLAY);
            icon.setColor("red");
            icon.getElement().getStyle().setFontSize(20, Unit.PX);

            panel.add(icon);

        } else {
            panel.add(new Icon(IconType.QUESTION_CIRCLE));
        }

        return panel;
    }

    /**
     * Gets a widget displaying details about the given media's internal configuration (i.e. optional message, fullscreen, etc.)
     *
     * @param media the media to display configuration details for. Can't be null.
     * @return a widget displaying the media's configurtion details
     */
    public Widget getMediaConfigDisplay(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        }

        HorizontalPanel panel = new HorizontalPanel();

        if(media.getMessage() != null) {

            Icon icon = new Icon(IconType.COMMENT_O);
            icon.getElement().getStyle().setFontSize(20, Unit.PX);
            icon.getElement().getStyle().setMarginRight(5, Unit.PX);

            Tooltip tooltip = new Tooltip(icon);
            tooltip.setTitle("Display an informative message when this media is shown");

            panel.add(icon);
        }

        if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {

            YoutubeVideoProperties properties = (YoutubeVideoProperties) media.getMediaTypeProperties();

            if(properties.getSize() != null) {

                Icon icon = new Icon(IconType.OBJECT_GROUP);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Display this video with a custom size ");

                panel.add(icon);
            }

            if(BooleanEnum.TRUE.equals(properties.getAllowFullScreen())) {

                Icon icon = new Icon(IconType.ARROWS_ALT);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Allow the learner to display this video in fullscreen");

                panel.add(icon);
            }

            if(BooleanEnum.TRUE.equals(properties.getAllowAutoPlay())) {

                Icon icon = new Icon(IconType.PLAY_CIRCLE_O);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Play this video automatically when it is shown");

                panel.add(icon);
            }
            
        } else if(media.getMediaTypeProperties() instanceof VideoProperties) {

            VideoProperties properties = (VideoProperties) media.getMediaTypeProperties();

            if(properties.getSize() != null) {

                Icon icon = new Icon(IconType.OBJECT_GROUP);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Display this video with a custom size ");

                panel.add(icon);
            }

            if(BooleanEnum.TRUE.equals(properties.getAllowFullScreen())) {

                Icon icon = new Icon(IconType.ARROWS_ALT);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Allow the learner to display this video in fullscreen");

                panel.add(icon);
            }

            if(BooleanEnum.TRUE.equals(properties.getAllowAutoPlay())) {

                Icon icon = new Icon(IconType.PLAY_CIRCLE_O);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                icon.getElement().getStyle().setMarginRight(5, Unit.PX);

                Tooltip tooltip = new Tooltip(icon);
                tooltip.setTitle("Play this video automatically when it is shown");

                panel.add(icon);
            }
        }

        return panel;
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(mediaList);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(mediaValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (mediaValidationStatus.equals(validationStatus)) {
            mediaValidationStatus.setValidity(!mediaList.getItems().isEmpty());
        }
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {

        //Note: We shouldn't disable the validateMediaButton in read-only mode, since it doesn't edit anything and should always be enabled
        mediaList.setReadonly(isReadonly);
    }
}
