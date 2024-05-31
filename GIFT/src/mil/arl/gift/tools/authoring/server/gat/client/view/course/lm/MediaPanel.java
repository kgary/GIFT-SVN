/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.lm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.extras.gallery.client.ui.Gallery;
import org.gwtbootstrap3.extras.gallery.client.ui.GalleryImage;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import generated.course.BooleanEnum;
import generated.course.CustomParameters;
import generated.course.DisplayModeEnum;
import generated.course.ImageProperties;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LessonMaterialList.Assessment;
import generated.course.LessonMaterialList.Assessment.UnderDwell;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.Media;
import generated.course.Nvpair;
import generated.course.OverDwell;
import generated.course.OverDwell.Duration.DurationPercent;
import generated.course.PDFProperties;
import generated.course.Size;
import generated.course.SlideShowProperties;
import generated.course.WebpageProperties;
import generated.course.VideoProperties;
import generated.course.YoutubeVideoProperties;
import generated.metadata.Metadata;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.VideoCssUnitsEnum;
import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableHTML;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MetadataFileReferenceListDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.FileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.LoadedFileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SetNameDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.NameValuePairEditor;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShowResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMetadataFilesForMerrillQuadrant;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.StringListResult;

/**
 * A deck panel containing editors for each media type 
 * 
 * @author bzahid
 */
public class MediaPanel extends Composite implements CourseReadOnlyHandler {

    private static MediaPanelUiBinder uiBinder = GWT.create(MediaPanelUiBinder.class);
    
    interface MediaPanelUiBinder extends UiBinder<Widget, MediaPanel>{
    }
    
    private static Logger logger = Logger.getLogger(MediaPanel.class.getName());
    
    private static final String NO_FILE_LABEL = "No File Selected";
    
    /** the name of the folder under the course folder where slide show folders will be created */
    private static final String SLIDE_SHOW_FOLDER_NAME = "Slide Shows"; 
    
    /** The file extension for archive (ZIP) files */
    private static final String ZIP = ".zip";
    
    /** An ID identifying the last instance of this class that was created. This is used to ensure radia button groups are unique. */
    private static int lastInstanceId = 0;
    
    /** The instructions to display in the file selection dialog used for Local Webpage objects */
    private static final String LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS = 
            " Select a web page file.<br> Supported extensions are :<b>" 
            + Constants.html_supported_types +".</b><br/><br/>"
            + "You can also select a <b>.zip</b> file containing a web page file and its resources (e.g. style sheets, scripts, "
            + "images, etc.) in order to load them simultaneously. This can be helpful when loading web pages with many dependencies.";
    
    /** The file extensions that should be allowed by the file selection dialog used for Local Webpage objects */
    private static final String[] LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS;
    
    /** The prefix url for the youtube video thumbnail */
    private static final String YOUTUBE_THUMBNAIL_URL_PREFIX = "https://img.youtube.com/vi/";
    
    /** The siffx url for the youtube video thumbnail */
    private static final String YOUTUBE_THUMBNAIL_URL_SUFFIX = "/0.jpg";
    
    /** The percentage of a YouTube video's duration to use by default. */
    private static final int DEFAULT_VIDEO_DURATION_PERCENT = 150;

    /** The base URL to build the full preview URL from */
    private static final String BASE_PREVIEW_URL = "LessonMaterialPreview.html";

    /** The full preview URL including the GAT host from the server */
    private static String previewUrl = BASE_PREVIEW_URL;
    
    static{
        
        List<String> allowedExtensions = new ArrayList<String>();
        allowedExtensions.addAll(Arrays.asList(Constants.html_supported_types));
        allowedExtensions.add(ZIP);
        
        String[] extensionsArray = new String[allowedExtensions.size()];
        
        LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS = allowedExtensions.toArray(extensionsArray);
    }
    
    private Media currentMedia = null;
    
    /** 
     * The lesson material course object that will be used to obtain the name of the course object, if one is available. This
     * is mainly used with PowerPoint and Slide Show objects to name their files.
     */
    private LessonMaterial currentLessonMaterial = null;
    
    /** 
     * The lesson material list containing this media's assessments. This is used to handle overdwell and underdwell assessments 
     * for YouTube videos 
     */
    private LessonMaterialList currentAssessmentList = null;
    
    private Metadata currentMetadata = null;
    
    private final String courseFolderPath;
    
    private boolean lockMetadata = false;
    
    private DefaultGatFileSelectionDialog pptFileSelectionDialog = new DefaultGatFileSelectionDialog();   
    
    /** A dialog used to select PDF files */
    private DefaultGatFileSelectionDialog pdfFileDialog = new DefaultGatFileSelectionDialog();
    
    /** A dialog used to select PowerPoint files */
    private DefaultGatFileSelectionDialog webpageFileDialog = new DefaultGatFileSelectionDialog();
    
    /** A dialog used to select image files */
    private DefaultGatFileSelectionDialog imageFileDialog = new DefaultGatFileSelectionDialog();
    
    /** A dialog used to select video files */
    private DefaultGatFileSelectionDialog videoFileDialog = new DefaultGatFileSelectionDialog();
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        String groupLabel();
    }
    
    @UiField
    protected Style style;
    
    @UiField
    protected DeckPanel mainDeck;
    
    @UiField
    protected DeckPanel mediaDeckPanel;
    
    @UiField
    protected FlowPanel mediaWrapper;
    
    @UiField
    protected FlowPanel iconPanel;
    
    @UiField
    protected FlowPanel titlePanel;
    
    @UiField
    protected FlowPanel borderPanel;
    
    @UiField
    protected Icon mediaIcon;
    
    @UiField 
    protected InlineHTML mediaTypeHtml;
    
    @UiField
    protected TextBox linkBox;
    
    ////////////////////////
    // Slide Show
    ////////////////////////

    @UiField
    protected DeckPanel slideshowDeck;
    
    @UiField
    protected FlowPanel slideShowPanel;

    @UiField
    protected FocusPanel selectPptPanel;
    
    @UiField
    protected FlowPanel pptSelectedPanel;
    
    @UiField
    protected Button replaceSlideShowButton;
    
    @UiField
    protected Button removePptButton;
    
    @UiField
    protected CheckBox previousCheckbox;
    
    @UiField
    protected CheckBox continueCheckbox;
    
    @UiField
    protected Label slideNumberLabel;
    
    @UiField
    protected Label selectPptLabel;
    
    @UiField
    protected HTMLPanel slideShowWarning;
    
    private boolean replaceSlideShow = false;
    
    /** A modal used to track the progress of creating slide shows */
    private FileOperationProgressModal progressModal = new FileOperationProgressModal(ProgressType.SLIDE_SHOW);
    
    /** A modal used to track the progress of unzipping operations */
    private LoadedFileOperationProgressModal<UnzipFileResult> unzipProgressModal = new LoadedFileOperationProgressModal<UnzipFileResult>(ProgressType.UNZIP);

    ////////////////////////
    // PDF
    ////////////////////////

    @UiField
    protected Widget pdfPanel;

    @UiField
    protected Label pdfFileLabel;

    @UiField
    protected FocusPanel selectPDFFilePanel;

    @UiField
    protected Widget pdfSelectedPanel;

    @UiField
    protected Button removePDFButton;

    ////////////////////////
    // Local webpage
    ////////////////////////

    @UiField
    protected Widget localWebpagePanel;

    @UiField
    protected Label localWebpageFileLabel;

    @UiField
    protected FocusPanel selectLocalWebpagePanel;
 
    @UiField
    protected Widget localWebpageSelectedPanel;

    @UiField
    protected Button removeLocalWebpageButton;
    
    ////////////////////////
    // Local video
    ////////////////////////
    
    @UiField
    protected Widget localVideoPanel;
    
    @UiField
    protected Label localVideoFileLabel;
    
    @UiField
    protected FocusPanel selectLocalVideoPanel;
    
    @UiField
    protected Widget localVideoSelectedPanel;
    
    @UiField
    protected Button removeLocalVideoButton;
    
    @UiField
    protected ListBox localVideoUnitHeight; 
    
    @UiField
    protected ListBox localVideoUnitWidth;

    @UiField
    protected CheckBox localVideoSizeCheck;

    @UiField
    protected Widget localVideoSizePanel;

    @UiField
    protected TextBox localVideoWidthBox;

    @UiField
    protected TextBox localVideoHeightBox;

    @UiField
    protected CheckBox localVideoFullScreenCheck;

    @UiField
    protected CheckBox localVideoAutoPlayCheck;
    
    @UiField
    protected CheckBox localConstrainToScreenCheck;
    
    ////////////////////////
    // Image
    ////////////////////////  

    @UiField
    protected Widget localImagePanel;

    @UiField
    protected Label localImageFileLabel;

    @UiField
    protected FocusPanel selectLocalImagePanel;

    @UiField
    protected Widget localImageSelectedPanel;
    
    /** The image gallery for showing the image thumbnail */
    @UiField
    protected Gallery localImageGallery;

    @UiField
    protected Button removeLocalImageButton;

    ////////////////////////
    // Website
    ////////////////////////    

    @UiField
    protected Widget webAddressPanel;

    @UiField
    protected TextBox urlTextBox;

    @UiField
    protected Button urlPreviewButton;

    @UiField
    protected Widget youTubePanel;
    
    /** The image gallery for showing the youtube thumbnail */
    @UiField
    protected Gallery youtubeGallery;
    
    @UiField
    protected Button previewYoutubeButton;

    @UiField
    protected TextBox videoTextBox;
    
    @UiField
    protected ListBox videoUnitHeight;
    
    @UiField
    protected ListBox videoUnitWidth; 

    @UiField
    protected CheckBox videoSizeCheck;

    @UiField
    protected Widget videoSizePanel;

    @UiField
    protected TextBox videoWidthBox; 

    @UiField
    protected TextBox videoHeightBox;

    @UiField
    protected CheckBox videoFullScreenCheck;

    @UiField
    protected CheckBox videoAutoPlayCheck; 
    
    @UiField
    protected CheckBox constrainToScreenCheck;
    
    ////////////////////////
    // LTI
    ////////////////////////
    
    @UiField
    protected Widget ltiPanel;
    
    @UiField
    protected Widget scoringPropertiesPanel;
    
    @UiField
    protected Widget allowScorePanel;
    
    @UiField
    protected Widget ltiLearnerStateAttributePanel;
    
    @UiField
    protected Widget ltiConceptPanel;
    
     /** LTI Identifier. This id links to the client key and client secret. **/
    @UiField
    protected MultipleSelect ltiIdentifierDropdown;

    @UiField
    protected TextBox ltiURL; 
    
    @UiField(provided=true)
    protected NameValuePairEditor ltiCustomParametersTable = new NameValuePairEditor("Key", "Value");

    /** The allow score checkbox **/
    @UiField
    protected CheckBox allowScore;

    /** The score range slider **/
    @UiField
    protected RangeSlider scoreSlider;

    /** The novice text. Gets updated by the slider. **/
    @UiField
    protected DescriptionData noviceText;

    /** The journeyman text. Gets updated by the slider. **/
    @UiField
    protected DescriptionData journeymanText;

    /** The expert text. Gets updated by the slider. **/
    @UiField
    protected DescriptionData expertText;
       
    /** GIFT concept multi select checkbox list table. **/
    @UiField
    protected CellTable<CandidateConcept> conceptCellTable = new CellTable<CandidateConcept>();

    /** Knowledge attribute button option */
    @UiField
    protected org.gwtbootstrap3.client.ui.RadioButton knowledgeTypeButton;
    
    /** Skill attribute button option */
    @UiField
    protected org.gwtbootstrap3.client.ui.RadioButton skillTypeButton;
    
    /** Display mode select select dropdown picker **/
    @UiField
    protected ValueListBox<String> displayModeDropdown;
    
    /**
     * An html string representation of a checked input box.
     */
    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");

    /**
     * An html string representation of an unchecked input box.
     */
    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

    /** The checkbox column for the 'Concepts:' table */
    private Column<CandidateConcept, Boolean> conceptSelectionColumn = new Column<CandidateConcept, Boolean>(new CheckboxCell()) {

        @Override
        public Boolean getValue(CandidateConcept candidate) {
            if(candidate != null) {
                return candidate.isChosen();
            } else {
                return false;
            }
        }

        @Override
        public void render(Context context, CandidateConcept candidate, SafeHtmlBuilder sb) {
            if (readOnly) {

                boolean checked = getValue(candidate);

                if (checked) {
                    sb.append(INPUT_CHECKED_DISABLED);
                } else if (!checked) {
                    sb.append(INPUT_UNCHECKED_DISABLED);
                }
            } else {
                super.render(context, candidate, sb);
            }
        }

    };

    /** The name column for the 'Concepts:' table */
    private Column<CandidateConcept, String> conceptNameColumn = new Column<CandidateConcept, String>(new TextCell()) {

        @Override
        public String getValue(CandidateConcept candidate) {

            if (candidate.getConceptName() != null) {
                return candidate.getConceptName();
            }

            return null;
        }

    };
    
    /** The list of course LTI providers used to populate the identifier dropdown */
    private List<LtiProvider> courseLtiProviderIdList = new ArrayList<LtiProvider>();
    
    /** String value shown to the user when the LTI data needs to be hidden */
    private final static String PROTECTED_LTI_DATA = "**protected**";
    
    ////////////////////////
    // Message
    ////////////////////////

    @UiField
    protected FlowPanel editorPanel;
    
    @UiField
    protected FlowPanel messagePanel;
    
    @UiField
    protected Icon messageIcon;
    
    @UiField
    protected FocusPanel messageButton;
    
    @UiField(provided=true)
    protected Summernote richTextEditor = new Summernote(){
        
        @Override
        protected void onLoad() {
            super.onLoad();
            
            //need to reconfigure the editor or else the blur event doesn't fire properly
            richTextEditor.reconfigure();
            
            //need to reassign the message HTML to the editor since it gets lost when the editor is detached.
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    if(currentMedia != null && currentMedia.getMessage() != null) {
                        richTextEditor.setCode(currentMedia.getMessage());
                    }
                }
            });
        }
    };
    
    @UiField
    protected Widget videoDurationLabel;
    
    @UiField
    protected FormattedTimeBox videoDurationBox;
    
    @UiField
    protected Button videoDurationButton;
    
    @UiField
    protected CheckBox videoUnderDwellCheck;
    
    @UiField
    protected Collapse videoUnderDwellCollapse;
    
    @UiField
    protected EditableHTML videoUnderDwellFeedback;
    
    @UiField
    protected CheckBox videoOverDwellCheck;
    
    @UiField
    protected Collapse videoOverDwellCollapse;
    
    @UiField
    protected FormattedTimeBox videoMinimumTimeBox;
    
    @UiField(provided=true)
    protected RadioButton videoOverDwellPercentButton;
    
    @UiField(provided=true)
    protected RadioButton videoOverDwellFixedButton;
    
    @UiField
    protected DeckPanel videoOverDwellDeck;
    
    @UiField
    protected Widget videoOverDwellPercentPanel;
    
    @UiField
    protected Widget videoOverDwellFixedPanel;
    
    @UiField(provided=true)
    protected NumberSpinner videoPercentDurationBox = new NumberSpinner(0, 0, Integer.MAX_VALUE);
    
    @UiField
    protected FormattedTimeBox videoMaximumTimeBox;
    
    @UiField
    protected EditableHTML videoOverDwellFeedback;
    
    @UiField
    protected FormattedTimeBox videoCalculatedPercentBox;
    
    @UiField
    protected Collapse videoAssessmentCollapse;
    
    /** An optional command that will be executed whenever the underlying metadata is changed*/
    private Command onChangeCommand = null;
    
    private boolean readOnly = false;
    
    /** Label for the extraneous dropdown group of LTI provider ids. */
    private static final String EXTRANEOUS_LTI_PROVIDER_IDS_GROUP_LABEL = "Extraneous Course LTI Providers";
    
    /** Label for the regular dropdown group of LTI provider ids. Should only be displayed if the Extraneous group isn't empty. */
    private static final String COURSE_LTI_PROVIDER_IDS_GROUP_LABEL = "Course LTI Providers";
    
    /** Text color for the items in the extraneous LTI provider group. */
    private static final String EXTRANEOUS_LTI_PROVIDER_IDS_GROUP_TEXT_COLOR = "red";
    
    public MediaPanel() {
        
        int instanceId = lastInstanceId++;
        
        videoOverDwellPercentButton = new RadioButton("videoOverDwellButton - " + instanceId);
        videoOverDwellFixedButton = new RadioButton("videoOverDwellButton - " + instanceId);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
        
        readOnly = GatClientUtility.isReadOnly();
        String currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
        String courseName = GatClientUtility.getCourseFolderName(currentCoursePath);
        courseFolderPath = currentCoursePath.substring(0, currentCoursePath.indexOf(courseName) + courseName.length());        
        mainDeck.showWidget(mainDeck.getWidgetIndex(mediaWrapper));
        
        initPDF();
        initYoutube();
        initLocalWebpage();
        initWebAddress();
        initImage();
        initVideo();
        initSlideShow();
        initLTI();
        
        Toolbar defaultToolbar = new Toolbar()
        .addGroup(ToolbarButton.STYLE)
        .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC, ToolbarButton.FONT_SIZE)
        .addGroup(ToolbarButton.LINK, ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
        .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);

        richTextEditor.setToolbar(defaultToolbar);
        richTextEditor.reconfigure();
        richTextEditor.addSummernoteBlurHandler(new SummernoteBlurHandler() {

            @Override
            public void onSummernoteBlur(SummernoteBlurEvent event) {
                if(!readOnly && currentMedia != null) {
                    currentMedia.setMessage(richTextEditor.getCode());
                }
            }
        });

        messageButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                editorPanel.setVisible(!editorPanel.isVisible());
                if(editorPanel.isVisible()) {
                    messageIcon.setType(IconType.MINUS_SQUARE);
                } else {
                    messageIcon.setType(IconType.PLUS_SQUARE);
                }
            }

        });
        
        linkBox.addValueChangeHandler(new ValueChangeHandler<String>() {
                
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    
                    if(!readOnly && event.getValue() != null){
                        
                        if(currentMedia != null){
                            currentMedia.setName(event.getValue());
                        }
                        
                        if(currentMetadata != null) {
                            currentMetadata.setDisplayName(event.getValue());
                        }
                    
                        onChange();
                    }
                }
            });
        
        /** Previews the selected media object. */
        previewYoutubeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                String url = videoTextBox.getText();

                if(StringUtils.isNotBlank(url)) {

                    if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties) {

                        // open the url in an iframe with the specified width
                        Size s = ((YoutubeVideoProperties) currentMedia.getMediaTypeProperties()).getSize();
                        String width = (s == null) ? "81%" : s.getWidth().toString() + "px";
                        String height = (s == null) ? "95%" : s.getWidth().toString() + "px";

                        url = GatClientUtility.getEmbeddedYouTubeUrl(url);

                        Window.open(previewUrl + "?url=" + URL.encodeQueryString(url) + "&height=" + height + "&width=" + width, "_blank", "");
                    }

                } else {
                    WarningDialog.warning("URL Error", "Please provide a URL to preview.");
                }

            }
        });
        
        if(readOnly) {
            linkBox.setEnabled(false);
            richTextEditor.setEnabled(false);
        }
            
    }
    
    public void editMedia(LessonMaterial lessonMaterial, Media media) {
        currentLessonMaterial = lessonMaterial;
        editMetadataMedia(media, null, false);
    }

    public void editMetadataSlideShow(LessonMaterial lessonMaterial, Media media,  Metadata metadata, boolean lockMetadata) {
        currentLessonMaterial = lessonMaterial;
        editMetadataMedia(media, metadata, lockMetadata);
    }
    
    /**
     * Edits a YouTube video media item and modifies the assessment handled contained by the given lesson material list
     * 
     * @param lessonMaterialList the lesson material list contining the assessment to modify
     * @param media the YouTube video media item
     * @param metadata the metadata associated with this item, if any
     * @param lockMetadata whether or not the metadata file being edited (if any) should be locked.
     */
    public void editMetadataYouTubeVideo(LessonMaterialList lessonMaterialList, Media media,  Metadata metadata, boolean lockMetadata) {
        currentAssessmentList = lessonMaterialList;
        editMetadataMedia(media, metadata, lockMetadata);
    }
    
    /**
     * Load the media panel for an LTI type for MBP Rule or Example. This hides the scoring properties and
     * forces the allow score option to false.
     * 
     * @param media the LTI media item
     * @param metadata the metadata associated with this item, if any
     * @param lockMetadata whether or not the metadata file being edited (if any) should be locked.
     */
    public void editMetadataLtiForMbpRuleOrExample(Media media, Metadata metadata, boolean lockMetadata) {
        editMetadataMedia(media, metadata, lockMetadata);
        
        // set values for hidden objects
        setLtiAttributeActive(true);
        allowScore.setValue(false);
        
        // force properties to update
        ValueChangeEvent.fire(knowledgeTypeButton, true);
        ValueChangeEvent.fire(allowScore, allowScore.getValue());
        
        // hide objects we don't want the author to be able to change for MBP Rules
        scoringPropertiesPanel.setVisible(false);
    }
    
    /**
     * Load the media panel for an LTI type for MBP Remediation. This hides the scoring properties and
     * forces the allow score option to false.
     * 
     * @param media the LTI media item
     * @param metadata the metadata associated with this item, if any
     * @param lockMetadata whether or not the metadata file being edited (if any) should be locked.
     */
    public void editMetadataLtiForMbpRemediation(Media media, Metadata metadata, boolean lockMetadata) {
        // same logic for Remediation as Rule
        editMetadataLtiForMbpRuleOrExample(media, metadata, lockMetadata);
    }
    
    /**
     * Load the media panel for an LTI type for MBP Practice. This hides the scoring properties and
     * forces the scoring attribute to SKILL.
     * 
     * @param media the LTI media item
     * @param metadata the metadata associated with this item, if any
     * @param lockMetadata whether or not the metadata file being edited (if any) should be locked.
     */
    public void editMetadataLtiForMbpPractice(Media media, Metadata metadata, boolean lockMetadata) {
        editMetadataMedia(media, metadata, lockMetadata);
     
        // set values for hidden objects
        setLtiAttributeActive(false);
        
        // force properties to update
        ValueChangeEvent.fire(skillTypeButton, true);
        
        // hide objects we don't want the author to be able to change for MBP Practice
        ltiLearnerStateAttributePanel.setVisible(false);
        ltiConceptPanel.setVisible(false);
    }
    
    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel to show.
     * if null nothing happens.
     * @param metadata optional metadata for the media type. Can be null.
     * @param lockMetadata whether or not the metadata file being edited (if any) should be locked.
     */
    public void editMetadataMedia(final Media media, Metadata metadata, boolean lockMetadata) {
        setMetadata(metadata);
        this.lockMetadata = lockMetadata;
        
        if(media != null && media.getMediaTypeProperties() != null) {
            
            currentMedia = media;

            resetPanel(media.getMediaTypeProperties(), lockMetadata);
            messagePanel.setVisible(true);
            
            if(media.getMessage() != null){
                
                richTextEditor.setCode(media.getMessage());
                editorPanel.setVisible(true);
                
            } else {
                
                richTextEditor.clear();
                editorPanel.setVisible(false);
            }
            
            if(editorPanel.isVisible()) {
                messageIcon.setType(IconType.MINUS_SQUARE);
            } else {
                messageIcon.setType(IconType.PLUS_SQUARE);
            }

            if(metadata != null && metadata.getDisplayName() != null){
                linkBox.setText(metadata.getDisplayName());
            }else if(media.getName() != null) {
                linkBox.setText(media.getName());
            }
            
            if(media.getMediaTypeProperties() instanceof PDFProperties) {
                // Show the PDF panel
                
                if(media.getUri() != null && !media.getUri().isEmpty()) {
                    pdfFileLabel.setText(media.getUri());
                    selectPDFFilePanel.setVisible(false);
                    pdfSelectedPanel.setVisible(true);
                }
                
                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(pdfPanel));
                
            } else if (media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                // Show the YouTube video panel 
                
                showYoutubeThumbnail(media.getUri());
                
                videoDurationBox.setValue(0);
                
                YoutubeVideoProperties properties = (YoutubeVideoProperties) media.getMediaTypeProperties();        
                
                videoAutoPlayCheck.setValue(properties.getAllowAutoPlay().equals(BooleanEnum.TRUE));
                videoFullScreenCheck.setValue(properties.getAllowFullScreen().equals(BooleanEnum.TRUE));
                
                videoSizeCheck.setValue(properties.getSize() != null);
                videoSizePanel.setVisible(properties.getSize() != null);
                
                
                Size propsSize = properties.getSize();

                if (propsSize != null) {
                    if(propsSize.getHeight() != null) {
                        videoHeightBox.setValue(propsSize.getHeight().toString());
                    }
                        
                    // Default height units to pixel units
                    if (propsSize.getHeightUnits() == null) {
                        propsSize.setHeightUnits(VideoCssUnitsEnum.PIXELS.getName());
                    } else if (propsSize.getHeightUnits() != null) {
                        setInitialUnitsListBoxSelection(videoUnitHeight, propsSize.getHeightUnits());
                    }
            
                    if(propsSize.getWidth() != null) {
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
                } else if (propsSize != null){
                    logger.info("Setting initial constrain to screen value.");
                    propsSize.setConstrainToScreen(BooleanEnum.FALSE);
                    constrainToScreenCheck.setValue(false);
                    
                }

                if(currentAssessmentList != null && currentAssessmentList.getAssessment() != null){
                    
                    videoDurationLabel.setVisible(true);
                    
                    //populate the fields for the underswell assessment logic
                    if(currentAssessmentList.getAssessment().getUnderDwell() != null){
                        
                        if(currentAssessmentList.getAssessment().getUnderDwell().getDuration() != null){
                            videoMinimumTimeBox.setValue(currentAssessmentList.getAssessment().getUnderDwell().getDuration().intValue());
                            
                        } else {
                            videoMinimumTimeBox.setValue(null);
                        }
                        
                        videoUnderDwellFeedback.setValue(currentAssessmentList.getAssessment().getUnderDwell().getFeedback());
                        
                        videoUnderDwellCheck.setValue(true);
                        videoUnderDwellCollapse.show();
                        
                    } else {
                        videoUnderDwellCheck.setValue(false);
                        videoUnderDwellCollapse.hide();
                    }
                    
                    //populate the fields for the overdwell assessment logic
                    if(currentAssessmentList.getAssessment().getOverDwell() != null){
                        
                        if(currentAssessmentList.getAssessment().getOverDwell().getDuration() != null){
                            
                            if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() == null){
                                currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(new DurationPercent());
                            }   
                            
                            if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent){
                                
                                DurationPercent duration = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                                
                                if(duration.getPercent() != null){
                                    videoPercentDurationBox.setValue(duration.getPercent().intValue());
                                    
                                } else {
                                    videoPercentDurationBox.setValue(DEFAULT_VIDEO_DURATION_PERCENT);
                                }
                                
                                videoOverDwellPercentButton.setValue(true);
                                videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellPercentPanel));
                                
                            } else if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof BigInteger){
                                
                                BigInteger duration = (BigInteger) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                                
                                videoMaximumTimeBox.setValue(duration.intValue());
                                
                                videoOverDwellFixedButton.setValue(true);
                                videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellFixedPanel));
                            }
                            
                        } else {
                            
                            videoMaximumTimeBox.setValue(null);
                            videoOverDwellFixedButton.setValue(true);
                            videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellFixedPanel));
                        }
                        
                        videoOverDwellFeedback.setValue(currentAssessmentList.getAssessment().getOverDwell().getFeedback());
                        
                        videoOverDwellCheck.setValue(true);
                        videoOverDwellCollapse.show();
                        
                    } else {
                        videoOverDwellCheck.setValue(false);
                        videoOverDwellCollapse.hide();
                    }
                    
                } else {
                    videoDurationLabel.setVisible(false);
                } 

                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(youTubePanel));
                                
            } else if (media.getMediaTypeProperties() instanceof WebpageProperties) {
                // Show the correct webpage panel
                
                if(CourseElementUtil.isWebAddress(media)) {
                    editWebAddress(media, lockMetadata);
                } else {
                    editLocalWebpage(media, lockMetadata);
                }               
                
            } else if (media.getMediaTypeProperties() instanceof ImageProperties) {
                // Show the image panel
                
                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(localImagePanel));
                
                showImageThumbnail(media.getUri());
                
            } else if (media.getMediaTypeProperties() instanceof VideoProperties) {
                // Show the image panel
                
                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(localVideoPanel));
                
                VideoProperties properties = (VideoProperties) media.getMediaTypeProperties();        
                
                localVideoAutoPlayCheck.setValue(properties.getAllowAutoPlay().equals(BooleanEnum.TRUE));
                localVideoFullScreenCheck.setValue(properties.getAllowFullScreen().equals(BooleanEnum.TRUE));
                
                localVideoSizeCheck.setValue(properties.getSize() != null);
                localVideoSizePanel.setVisible(properties.getSize() != null);
                
                
                Size propsSize = properties.getSize();

                if (propsSize != null) {
                    if(propsSize.getHeight() != null) {
                        localVideoHeightBox.setValue(propsSize.getHeight().toString());
                    }
                        
                    // Default height units to pixel units
                    if (propsSize.getHeightUnits() == null) {
                        propsSize.setHeightUnits(VideoCssUnitsEnum.PIXELS.getName());
                    } else if (propsSize.getHeightUnits() != null) {
                        setInitialUnitsListBoxSelection(localVideoUnitHeight, propsSize.getHeightUnits());
                    }
            
                    if(propsSize.getWidth() != null) {
                        localVideoWidthBox.setValue(propsSize.getWidth().toString());
                    }
                        
                    // Default width units to pixel units.
                    if (propsSize.getWidthUnits() == null) {
                        propsSize.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
                    } else if (propsSize.getWidthUnits() != null) {
                        setInitialUnitsListBoxSelection(localVideoUnitWidth, propsSize.getWidthUnits());
                    }
                } else {
                    // Default to pixel units.
                    setInitialUnitsListBoxSelection(localVideoUnitHeight, VideoCssUnitsEnum.PIXELS.getName());
                    setInitialUnitsListBoxSelection(localVideoUnitWidth, VideoCssUnitsEnum.PIXELS.getName());
                }
                
                if (propsSize != null && propsSize.getConstrainToScreen() != null) {
                    localConstrainToScreenCheck.setValue(propsSize.getConstrainToScreen().equals(BooleanEnum.TRUE));
                } else if (propsSize != null){
                    logger.info("Setting initial constrain to screen value.");
                    propsSize.setConstrainToScreen(BooleanEnum.FALSE);
                    localConstrainToScreenCheck.setValue(false);
                    
                }
                
                showVideoThumbnail(media.getUri());
                
                
            } else if (media.getMediaTypeProperties() instanceof SlideShowProperties) {
                // Show the slide show panel
                
                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(slideShowPanel));
                
            } else if (media.getMediaTypeProperties() instanceof LtiProperties) {
                // Show the LTI panel 
                LtiProperties properties = (LtiProperties) media.getMediaTypeProperties();
                
                // LTI identifier
                LtiProvider selectedProvider = populateLtiProviderIdSelectList(getCourseLtiProviderList(), properties.getLtiIdentifier());
                if (properties.getLtiIdentifier() == null) {
                    ltiIdentifierDropdown.setValue(new ArrayList<String>());
                } else {
                    ltiIdentifierDropdown.setValue(Arrays.asList(properties.getLtiIdentifier()), true);
                }
                
                // LTI url : hide if readonly and selected provider wants to be protected.
                if (GatClientUtility.isReadOnly() && selectedProvider != null && BooleanEnum.TRUE.equals(selectedProvider.getProtectClientData())) {
                    ltiURL.setValue(PROTECTED_LTI_DATA);
                } else {
                    ltiURL.setValue(media.getUri());
                }
                
                // LTI Custom Parameters
                if (properties.getCustomParameters() == null) {
                    properties.setCustomParameters(new CustomParameters());
                }
                
                ltiCustomParametersTable.setNameValueList(properties.getCustomParameters().getNvpair());
                
                // allow score
                boolean allow;
                if (properties.getAllowScore() == null) {
                    allow = true;
                    // force properties to update to true
                    ValueChangeEvent.fire(allowScore, true);
                } else {
                    allow = properties.getAllowScore().equals(BooleanEnum.TRUE);
                }
                allowScore.setValue(allow, true);
                
                allowScorePanel.setVisible(allow);
                
                // score slider
                Range scoreRange;
                if (properties.getSliderMinValue() != null && properties.getSliderMaxValue() != null) {
                    scoreRange = new Range(properties.getSliderMinValue().doubleValue(), properties.getSliderMaxValue().doubleValue());
                } else {
                    // default values
                    scoreRange = new Range(25.0, 75.0);
                }
                 
                scoreSlider.setValue(scoreRange, true);
                SlideStopEvent.fire(scoreSlider, scoreRange);
                updateScoreSliderLabels(scoreRange, scoreSlider.getMin(), scoreSlider.getMax());
                
                // attribute toggle
                boolean isKnowledge = !BooleanEnum.FALSE.equals(properties.getIsKnowledge());
                setLtiAttributeActive(isKnowledge);
                if(isKnowledge) {
                    // force properties to update
                    ValueChangeEvent.fire(knowledgeTypeButton, true);
                } else {
                    // force properties to update
                    ValueChangeEvent.fire(skillTypeButton, true);
                }
                

                
                // display mode
                List<String> displayModeAcceptableValues = new ArrayList<String>();
                displayModeAcceptableValues.add(DisplayModeEnum.INLINE.value());
                displayModeAcceptableValues.add(DisplayModeEnum.MODAL.value());
                displayModeAcceptableValues.add(DisplayModeEnum.NEW_WINDOW.value());
                
                // need to set a default value first or "null" will be an accepted value
                DisplayModeEnum attribute = properties.getDisplayMode();
                if (attribute == null) {
                    attribute = DisplayModeEnum.INLINE;
                    // force properties to update to INLINE
                    ValueChangeEvent.fire(displayModeDropdown, attribute.value());
                }
                displayModeDropdown.setValue(attribute.value(), true);
                displayModeDropdown.setAcceptableValues(displayModeAcceptableValues);
                
                mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(ltiPanel));
            }
        }
    }

    /**
     * Shows an thumbnail of an image file
     * @param url the url of the image. No-op if null
     */
    private void showImageThumbnail(final String url) {
        if(!StringUtils.isBlank(url)) {
            // Populate the url text
            localImageFileLabel.setText(url);
            localImageFileLabel.setVisible(false);
            localImageGallery.clear();

            if(url != null && !url.isEmpty()) {
                //Checks to make sure the url doesn't use any parent directory references
                if(url.equals("..") ||
                        url.endsWith("/..") ||
                        url.startsWith("../") ||
                        url.contains("/../")) {
                    WarningDialog.error("Invalid path", "The URI must reference a course within the course folder.\nThe url cannot contain any use of the '..' operator.");
                }
                
                // fetch the image thumbnail
                else {
                    String userName = GatClientUtility.getUserName();
                    final FetchContentAddress action = new FetchContentAddress(courseFolderPath, url, userName);
                    
                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<FetchContentAddressResult>() {

                        @Override
                        public void onFailure(Throwable cause) {
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                    "There was a problem retrieving the image thumbnail.", 
                                    cause.getMessage(), 
                                    DetailedException.getFullStackTrace(cause));
                                    dialog.setDialogTitle("Failed to Retrieve Image Thumbnail");
                                    dialog.center();
                        }

                        @Override
                        public void onSuccess(FetchContentAddressResult result) {
                            
                            if(result.isSuccess()) {
                                GalleryImage image = new GalleryImage(result.getContentURL());
                                Tooltip tooltip = new Tooltip();
                                tooltip.setTitle(url);
                                tooltip.add(image);
                                tooltip.setPlacement(Placement.BOTTOM);
                                localImageGallery.add(tooltip);
                            } else {
                                WarningDialog.error("Preview Failed", result.getErrorMsg());
                            }
                        }
                        
                    });
                }
            }
            selectLocalImagePanel.setVisible(false);
            localImageSelectedPanel.setVisible(true);
        } else {
            localImageFileLabel.setText("Select Image");
            localImageFileLabel.setVisible(true);
            localImageSelectedPanel.setVisible(false);
            selectLocalImagePanel.setVisible(true);
        }
    }
    
    private void showVideoThumbnail(final String url) {
        if(!StringUtils.isBlank(url)) {
            localVideoFileLabel.setText(url);
            
            selectLocalVideoPanel.setVisible(false);
            localVideoSelectedPanel.setVisible(true);
        } else {
            localVideoFileLabel.setText("Select Video");
            localVideoFileLabel.setVisible(true);
            localVideoSelectedPanel.setVisible(false);
            selectLocalVideoPanel.setVisible(true);
        }
    }

    /**
     * Shows an image thumbnail of a youtube url
     * @param url the url of the youtube video. No-op if null
     */
    private void showYoutubeThumbnail(final String url) {
        youtubeGallery.clear();

        if(!StringUtils.isBlank(url)) {
            previewYoutubeButton.setEnabled(true);
            // Populate the url text
            videoTextBox.setValue(url);

            if(!StringUtils.isBlank(url)) {
                //Checks to make sure the url doesn't use any parent directory references
                if(url.equals("..") ||
                        url.endsWith("/..") ||
                        url.startsWith("../") ||
                        url.contains("/../")
                        ) {
                    WarningDialog.error("Invalid path", "The URI must reference a course within the course folder.\nThe url cannot contain any use of the '..' operator.");
                } else {
                    // search the url for the video id parameter
                    String argSubstring = url.substring(url.indexOf("?") + 1);
                    String[] args = argSubstring.split("\\&");
                    String vid = null;
                    for (String arg : args) {
                        if (arg.startsWith("v=")) {
                            vid = arg.split("=")[1];
                            break;
                        }
                    }
                    if (vid != null) {
                        GalleryImage image = new GalleryImage(YOUTUBE_THUMBNAIL_URL_PREFIX + URL.encode(vid) + YOUTUBE_THUMBNAIL_URL_SUFFIX);
                        Tooltip tooltip = new Tooltip();
                        tooltip.setTitle(url);
                        tooltip.add(image);
                        tooltip.setPlacement(Placement.BOTTOM);
                        youtubeGallery.add(tooltip);
                    }
                }
            }
            selectLocalImagePanel.setVisible(false);
            localImageSelectedPanel.setVisible(true);
        } else {
            localImageFileLabel.setText("Select Image");
            localImageFileLabel.setVisible(true);
            localImageSelectedPanel.setVisible(false);
            selectLocalImagePanel.setVisible(true);
            previewYoutubeButton.setEnabled(false);
        }
    }

    /**
     * Persists the concept selection choice to the media properties. Not all
     * properties will care about this change.
     * 
     * @param concept the concept being updated
     * @param selected true if the concept has been selected; false if it has
     *        been deselected.
     */
    public void conceptSelected(String concept, boolean selected) {
        if (currentMedia == null) {
            return;
        }

        Serializable mediaProperties = currentMedia.getMediaTypeProperties();
        if (mediaProperties instanceof LtiProperties) {
            LtiProperties ltiProperties = (LtiProperties) mediaProperties;
            LtiConcepts ltiConcepts = ltiProperties.getLtiConcepts();
            if (selected) {
                if (ltiConcepts == null) {
                    ltiConcepts = new LtiConcepts();
                    ltiProperties.setLtiConcepts(ltiConcepts);
                }
                if (!ltiConcepts.getConcepts().contains(concept)) {
                    ltiConcepts.getConcepts().add(concept);
                }
            } else {
                if (ltiConcepts != null) {
                    ltiConcepts.getConcepts().remove(concept);
                }
            }
        }
    }
    
    /**
     * Sets the initial unit list box selected value based on the unit name.
     * 
     * @param unitListBox The list box object to set the selected value for.
     * @param widthUnits The name of the units to select.
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
     * Sets the metadata for the media being modified. This method will also show and hide metadata-specific fields based on
     * the metadata passed in.
     * 
     * @param metadata the metadata to use
     */
    public void setMetadata(Metadata metadata) {
        currentMetadata = metadata;
        
        if(metadata != null && metadata.getPresentAt() != null 
                && (MerrillQuadrantEnum.RULE.getName().equals(metadata.getPresentAt().getMerrillQuadrant())
                        || MerrillQuadrantEnum.EXAMPLE.getName().equals(metadata.getPresentAt().getMerrillQuadrant()))){
                
            //allow authors to create YouTube video assessments for Rule and Example media
            videoAssessmentCollapse.setVisible(true);
            
        } else {
            
            //prevent authors from creating YouTube video assessments for everything else
            videoAssessmentCollapse.setVisible(false);
            videoDurationLabel.setVisible(false);
    }
    }
    
    /**
     * Assigns the command to be executed whenever the underlying metadata is changed
     * 
     * @param command the command to be executed
     */
    public void setOnChangeCommand(Command command){
        this.onChangeCommand = command;
    }

    public void editWebAddress(Serializable courseObject, boolean lockMetadata) {
        
        if(courseObject instanceof Metadata) {
            currentMedia = null;
            setMetadata((Metadata) courseObject);
            resetPanel(new WebpageProperties(), lockMetadata);
            
            String value = null;
            if(currentMetadata.getContent() != null && currentMetadata.getContent() instanceof generated.metadata.Metadata.URL){
                value = ((generated.metadata.Metadata.URL)currentMetadata.getContent()).getValue();
            }   
            urlTextBox.setValue(value);
            messagePanel.setVisible(false);
            
        } else if (courseObject instanceof Media) {
            
            currentMedia = (Media) courseObject;
            resetPanel(new WebpageProperties(), lockMetadata);
            urlTextBox.setValue(currentMedia.getUri(), true);
            
            if(currentMedia.getMessage() != null){
                
                richTextEditor.setCode(currentMedia.getMessage());
                editorPanel.setVisible(true);
                
            } else {
                
                richTextEditor.clear();
                editorPanel.setVisible(false);
            }
            
            if(editorPanel.isVisible()) {
                messageIcon.setType(IconType.MINUS_SQUARE);
            } else {
                messageIcon.setType(IconType.PLUS_SQUARE);
            }
            
        } else {
            logger.severe("Cannot edit web address. Unsupported course object in Media Panel");
        }
        
        mediaIcon.setType(IconType.GLOBE);
        mediaTypeHtml.setHTML("<b>Web Address</b>");
        mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(webAddressPanel));
    }
    
    public void editLocalWebpage(Serializable courseObject, boolean lockMetadata) {
        
        String uri = null;
        
        if(courseObject instanceof Metadata) {
            currentMedia = null;
            setMetadata((Metadata) courseObject);
            resetPanel(new WebpageProperties(), lockMetadata);
            if(currentMetadata.getContent() != null && currentMetadata.getContent() instanceof generated.metadata.Metadata.Simple){
                uri = ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).getValue();
            }
            messagePanel.setVisible(false);
            
        } else if (courseObject instanceof Media) {
            
            currentMedia = (Media) courseObject;
            resetPanel(new WebpageProperties(), lockMetadata);
            uri = currentMedia.getUri();
            
            if(currentMedia != null){
                linkBox.setText(currentMedia.getName());
            }
            
            if(currentMedia.getMessage() != null){
                
                richTextEditor.setCode(currentMedia.getMessage());
                editorPanel.setVisible(true);
                
            } else {
                
                richTextEditor.clear();
                editorPanel.setVisible(false);
            }
            
            if(editorPanel.isVisible()) {
                messageIcon.setType(IconType.MINUS_SQUARE);
            } else {
                messageIcon.setType(IconType.PLUS_SQUARE);
            }
            
        } else {
            logger.severe("Cannot edit local webpage. Unsupported course object in Media Panel");
        }
        
        if(uri != null && !uri.isEmpty()) {
            localWebpageFileLabel.setText(uri);
            selectLocalWebpagePanel.setVisible(false);
            localWebpageSelectedPanel.setVisible(true);
        }
        
        mediaIcon.setType(IconType.FILE);
        mediaTypeHtml.setHTML("<b>Local Webpage</b>");
        mediaDeckPanel.showWidget(mediaDeckPanel.getWidgetIndex(localWebpagePanel));
    }
    
    /**
     * Set the visibility of the title panel and link textbox.
     * 
     * @param value true if the media title UI components should be visible
     */
    private void showTitlePanel(boolean value){ 
        linkBox.setVisible(value);
        titlePanel.setVisible(value);
    }
    
    private void resetPanel(Serializable properties, boolean lockMetadata) {
        
        if(currentMetadata == null) {
            borderPanel.removeStyleName("contentBorder");
            mediaWrapper.removeStyleName("contentBorder");
            
        } else {
            borderPanel.addStyleName("contentBorder");
            mediaWrapper.addStyleName("contentBorder");
        }
        
        iconPanel.setVisible(currentMetadata != null);
        showTitlePanel(currentMetadata != null);
        
        //make sure the link text box has a useful value with priority of display name first than media given name
        if(currentMetadata != null && currentMetadata.getDisplayName() != null){
            linkBox.setText(currentMetadata.getDisplayName());
        }else if(currentMedia != null && currentMedia.getName() != null){
            linkBox.setText(currentMedia.getName());
        }else{
            linkBox.setText("");   //otherwise the previous value will be shown which is not what we want when adding a new media
        }
        
        if(properties instanceof PDFProperties) {
            // Reset the PDF panel
            
            mediaIcon.setType(IconType.FILE_PDF_O);
            mediaTypeHtml.setHTML("<b>PDF</b>");
            pdfFileLabel.setText("Select PDF");
            
            showTitlePanel(true);
            
            selectPDFFilePanel.setVisible(true);
            pdfSelectedPanel.setVisible(false);
            removePDFButton.setVisible(!lockMetadata && !GatClientUtility.isReadOnly());
            
        } else if(properties instanceof YoutubeVideoProperties) {
            // Reset the youtube panel
                        
            mediaIcon.setType(IconType.YOUTUBE_PLAY);
            mediaTypeHtml.setHTML("<b>Youtube Video</b>");
            
            showTitlePanel(true);
            
            videoTextBox.setValue(null);
            videoSizeCheck.setValue(false);
            videoSizePanel.setVisible(false);
            videoWidthBox.setValue(null);
            videoHeightBox.setValue(null);
            videoFullScreenCheck.setValue(false);
            videoAutoPlayCheck.setValue(false);
            constrainToScreenCheck.setValue(false);
            videoOverDwellCheck.setValue(false);
            videoMaximumTimeBox.setValue(null);
            videoOverDwellCollapse.hide();
            videoUnderDwellCheck.setValue(false);
            videoMinimumTimeBox.setValue(null);
            videoUnderDwellCollapse.hide();
            videoDurationBox.setValue(0);
            videoOverDwellFixedButton.setValue(false);
            videoOverDwellPercentButton.setValue(true);
            videoUnderDwellFeedback.setValue(null);
            videoOverDwellFeedback.setValue(null);
            videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellPercentPanel));
            
            
        } else if (properties instanceof ImageProperties) {
            // Reset the image panel
            
            localImageFileLabel.setVisible(true);
            localImageFileLabel.setText(NO_FILE_LABEL);
            mediaIcon.setType(IconType.IMAGE);
            mediaTypeHtml.setHTML("<b>Local Image</b>");
            
            showTitlePanel(true);
            
            selectLocalImagePanel.setVisible(true);
            localImageSelectedPanel.setVisible(false);
            removeLocalImageButton.setVisible(!lockMetadata && !GatClientUtility.isReadOnly());
            
        } else if (properties instanceof WebpageProperties) {
            //  Clear Web Address and Local Webpage panels
            
            localWebpageFileLabel.setText(NO_FILE_LABEL);
            
            showTitlePanel(true);
            
            urlTextBox.setValue(null);
            selectLocalWebpagePanel.setVisible(true);
            localWebpageSelectedPanel.setVisible(false);
            urlTextBox.setEnabled(!lockMetadata && !GatClientUtility.isReadOnly());
            removeLocalWebpageButton.setVisible(!lockMetadata && !GatClientUtility.isReadOnly());
            
        } else if (properties instanceof VideoProperties) {
            //  Reset the video panel
            
            localVideoFileLabel.setVisible(true);
            localVideoFileLabel.setText(NO_FILE_LABEL);
            mediaIcon.setType(IconType.FILE_VIDEO_O);
            mediaTypeHtml.setHTML("<b>Local Video</b>");
            
            showTitlePanel(true);
            
            localVideoSizeCheck.setValue(false);
            localVideoSizePanel.setVisible(false);
            localVideoWidthBox.setValue(null);
            localVideoHeightBox.setValue(null);
            localVideoFullScreenCheck.setValue(false);
            localVideoAutoPlayCheck.setValue(false);
            localConstrainToScreenCheck.setValue(false);
            selectLocalVideoPanel.setVisible(true);
            localVideoSelectedPanel.setVisible(false);
            removeLocalVideoButton.setVisible(!lockMetadata && !GatClientUtility.isReadOnly());
            
        } else if (properties instanceof SlideShowProperties) {
            // Reset the SlideShow panel
            
            showTitlePanel(true);

            iconPanel.setVisible(false);
            borderPanel.removeStyleName("contentBorder");
            SlideShowProperties props = (SlideShowProperties) properties;
            
            if(props.getSlideRelativePath() != null && !props.getSlideRelativePath().isEmpty()) {
                slideNumberLabel.setText(props.getSlideRelativePath().size() + " Slides");
                slideshowDeck.showWidget(slideshowDeck.getWidgetIndex(pptSelectedPanel));
                slideShowWarning.getElement().getStyle().setProperty("color", "rgb(68, 68, 68)");

                boolean showContinue = (props.getKeepContinueButton() == null || props.getKeepContinueButton() == BooleanEnum.TRUE);
                boolean showPrevious = (props.getDisplayPreviousSlideButton() == null || props.getDisplayPreviousSlideButton() == BooleanEnum.TRUE);
                
                continueCheckbox.setValue(showContinue, true);
                previousCheckbox.setValue(showPrevious, true);
                
            } else {
                slideshowDeck.showWidget(slideshowDeck.getWidgetIndex(selectPptPanel));
                slideShowWarning.getElement().getStyle().setProperty("color", "#da0000");
            }
            
        } 
        else if (properties instanceof LtiProperties) {
            // Clear LTI Properties panels

            mediaIcon.setType(IconType.PLUG);
            mediaTypeHtml.setHTML("<b>LTI Provider</b>");

            showTitlePanel(true);

            ltiURL.setValue(null);
            
            ltiCustomParametersTable.setNameValueList(null);
            scoringPropertiesPanel.setVisible(true);
            ltiLearnerStateAttributePanel.setVisible(true);
            ltiConceptPanel.setVisible(true);
            allowScore.setValue(true);
            Range range = new Range(25.0, 75.0);
            scoreSlider.setMin(0.0);
            scoreSlider.setMax(100.0);
            scoreSlider.setValue(range);
            updateScoreSliderLabels(range, scoreSlider.getMin(), scoreSlider.getMax());
            setLtiAttributeActive(true);
            displayModeDropdown.setValue(DisplayModeEnum.INLINE.value());
            
            while (ltiIdentifierDropdown.getWidgetCount() != 0) {
                ltiIdentifierDropdown.remove(0);
            }
        } 
        else {
            logger.warning("Failed to reset Media Panel: Unsupported course object in Media Panel");
        }
        
    }
    
    /**
     * Executes whatever command is waiting for a change event, assuming such a command has been assigned
     */
    private void onChange(){
        
        if(onChangeCommand != null && !readOnly){
            onChangeCommand.execute();
        }
    }
    
    /**
     * Initialize the website UI components and handling.
     * 
     * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
     * true indicates editing metadata and false indicates creating metadata
     */
    private void initWebAddress(){
        
        urlTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null){
                    currentMedia.setUri(event.getValue());
                }
                
                if(currentMetadata != null) {
                    if(currentMedia == null){
                        generated.metadata.Metadata.URL url = new generated.metadata.Metadata.URL();
                        url.setValue(event.getValue());
                        currentMetadata.setContent(url);
                    }

                    currentMedia.setName(event.getValue());
                }
                
                if(event.getValue() != null){
                    
                    final String filename = event.getValue();
                    
                    //auto fill the title textbox to help the author, they can always change it
                    if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                        linkBox.setValue(filename, true);
                    }
                }

                onChange();
            }
        });
        
        urlPreviewButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                String url = urlTextBox.getValue();
                
                if(url == null || url.isEmpty()) {
                    WarningDialog.error("URL Error", "Please provide a URL to preview.");
                    
                } else {
                    
                    if(!url.startsWith("http")){
                        url = "http://".concat(url);
                    }
                    
                    Window.open(url, "_blank", "");
                }
                
                onChange();
            }
        });
        
        if(readOnly) {
            urlTextBox.setEnabled(false);
        }
    }
    
    /**
     * Initialize the local webpage UI components and handling.
     * 
     * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
     * true indicates editing metadata and false indicates creating metadata
     */
    private void initLocalWebpage(){
        
        //allow authors to upload HTML files or ZIP archives containing them
        webpageFileDialog.setAllowedFileExtensions(LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS);
        webpageFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        webpageFileDialog.setIntroMessageHTML(LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS);
        
        //add a handler to allow the webpage file selection dialog to be shown by clicking a button
        selectLocalWebpagePanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                webpageFileDialog.center();
            }
        });
        
        if(readOnly) {
            removeLocalWebpageButton.setVisible(false);
        }
        
        //add a handler to allow authors to delete local webpages by clicking a button
        removeLocalWebpageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }
                
                 DeleteRemoveCancelDialog.show("Delete Content", 
                        "Do you wish to <b>permanently delete</b> '"+localWebpageFileLabel.getText()+
                        "' from the course or simply remove the reference to that content in this metadata object?<br><br>"+
                                "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                        new DeleteRemoveCancelCallback() {
                    
                            @Override
                            public void cancel() {
                                
                            }

                            @Override
                            public void delete() {
                                
                              String username = GatClientUtility.getUserName();
                              String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                              List<String> filesToDelete = new ArrayList<String>();
                              final String filePath = courseFolderPath + "/" + localWebpageFileLabel.getText();
                              filesToDelete.add(filePath);
                              
                              DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                              SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                  @Override
                                  public void onFailure(Throwable error) {
                                      ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                              "Failed to delete the file.", 
                                              error.getMessage(), 
                                              DetailedException.getFullStackTrace(error));
                                      dialog.setDialogTitle("Deletion Failed");
                                      dialog.center();
                                  }

                                  @Override
                                  public void onSuccess(GatServiceResult result) {
                                      
                                      if(result.isSuccess()){
                                          logger.warning("Successfully deleted the file '"+filePath+"'.");
                                      } else{
                                          logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                      }                                           
                                       
                                      resetUI();
                                  }
                                  
                              });
                            }

                            @Override
                            public void remove() {
                                resetUI();
                            }
                            
                            private void resetUI(){ 
                                
                                if(currentMedia != null) {
                                    currentMedia.setUri(null);
                                }

                                if(currentMetadata != null) {
                                    currentMetadata.setContent(null); 
                                    currentMetadata.setDisplayName(null);             
                                }
                                
                                localWebpageFileLabel.setText("Select Webpage");
                                localWebpageSelectedPanel.setVisible(false);
                                selectLocalWebpagePanel.setVisible(true);
                                
                                onChange();

                            }

                 }, "Delete Content");
            }
        });
        
        //set up logic to allow the author to upload either plain HTML files or ZIP archives containing them
        final CanHandleUploadedFile originalUploadHandler = webpageFileDialog.getUploadHandler();
        
        webpageFileDialog.setUploadHandler(new CanHandleUploadedFile() {

            @Override
            public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {
                
                if(uploadFilePath.endsWith(ZIP)){
                    
                    //ZIP files containing web pages should have their contents extracted to the course folder
                    UnzipFile action = new UnzipFile(
                            GatClientUtility.getUserName(), 
                            GatClientUtility.getBaseCourseFolderPath(),
                            uploadFilePath
                    );
                    
                    final String filename = uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                    
                    //display successful upload message in Notify UI not a dialog
                    webpageFileDialog.setMessageDisplay(new DisplaysMessage() {
                        
                        @Override
                        public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                            WarningDialog.warning(title, text, callback);
                        }
                        
                        @Override
                        public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                            Notify.notify("", "The contents of '"+ filename +"' have been extracted into your course.", IconType.INFO, NotifyUtil.generateDefaultSettings());
                        }
                        
                        @Override
                        public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
                        
                        @Override
                        public void showDetailedErrorMessage(String text, String details, List<String> stackTrace, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
                    });
                    
                    copyOrUploadZip(action, null, callback);
                    
                } else {
                    
                    //upload HTML files normally
                    originalUploadHandler.handleUploadedFile(uploadFilePath, fileName, callback);
                }
            }

        });
        
        //set up logic to allow the author to copy either plain HTML files or ZIP archives containing them
        final CopyFileRequest originalCopyRequest = webpageFileDialog.getCopyFileRequest();
        
        webpageFileDialog.setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(final FileTreeModel source, final CopyFileCallback callback) {
                
                final String filename = source.getFileOrDirectoryName();
                
                if(filename.endsWith(ZIP)){
                
                    //ZIP files containing web pages should have their contents extracted to the course folder
                    UnzipFile action = new UnzipFile(
                            GatClientUtility.getUserName(), 
                            GatClientUtility.getBaseCourseFolderPath(),
                            source.getRelativePathFromRoot()
                    );
                    
                    //display successful upload message in Notify UI not a dialog
                    webpageFileDialog.setMessageDisplay(new DisplaysMessage() {
                        
                        @Override
                        public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                            WarningDialog.warning(title, text, callback);
                        }
                        
                        @Override
                        public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                            Notify.notify("", "The contents of '"+ filename +"' have been extracted into your course.", IconType.INFO, NotifyUtil.generateDefaultSettings());
                        }
                        
                        @Override
                        public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
    
                        @Override
                        public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
                    });
                    
                    copyOrUploadZip(action, callback, null);
                
                } else {
                    originalCopyRequest.asyncCopy(source, callback);
                }
            }
            
        });
        
        //set up logic to handle when the user has selected a non-ZIP file from the file selection dialog (ZIP logic is separate)
        webpageFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() { 
            
            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {
                
                if(event.getValue() != null){
                    
                    boolean isHtmlFile = false;
                    
                    for(String extension : Constants.html_supported_types){
                        
                        if(event.getValue().endsWith(extension)){
                            isHtmlFile = true;
                            break;
                        }
                    }
                                   
                    if(isHtmlFile){
                        
                        //HTML files should be loaded normally when the user has selected them in the file selection dialog
                        selectLocalWebpage(event.getValue());
                    }
                
                } else {
                    selectLocalWebpage(event.getValue());
                }
            }
        });
        
    }
    
    /**
     * Initialize the LTI UI components and handling.
     */
    private void initLTI(){
                
        ltiURL.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if (currentMedia != null) {
                    currentMedia.setUri(event.getValue() == null ? null : event.getValue().trim());
                    onChange();
                }
            }
        });
        
        ltiCustomParametersTable.setOnChangeCommand(new Command() {

            @Override
            public void execute() {
                onChange();
            }
        });

        allowScore.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    boolean allow = event.getValue() == null ? true : event.getValue();
                    properties.setAllowScore(allow ? BooleanEnum.TRUE : BooleanEnum.FALSE);

                    allowScorePanel.setVisible(allow);

                    onChange();
                }
            }
        });

        scoreSlider.addSlideStopHandler(new SlideStopHandler<Range>() {

            @Override
            public void onSlideStop(SlideStopEvent<Range> event) {

                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    properties.setSliderMinValue(new BigDecimal(event.getValue().getMinValue()).toBigInteger());
                    properties.setSliderMaxValue(new BigDecimal(event.getValue().getMaxValue()).toBigInteger());
                    updateScoreSliderLabels(event.getValue(), scoreSlider.getMin(), scoreSlider.getMax());

                    onChange();
                }
            }
        });
        
        displayModeDropdown.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    properties.setDisplayMode(event.getValue() == null ? DisplayModeEnum.INLINE : DisplayModeEnum.fromValue(event.getValue()));

                    onChange();
                }
            }
        });
        
        initConceptTable();
        
        conceptSelectionColumn.setFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {

            @Override
            public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {

                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    candidate.setChosen(hasBeenSelected);

                    if (properties.getLtiConcepts() == null) {
                        properties.setLtiConcepts(new LtiConcepts());
                    }
                    
                    if (hasBeenSelected) {
                        properties.getLtiConcepts().getConcepts().add(candidate.getConceptName());
                    } else {
                        properties.getLtiConcepts().getConcepts().remove(candidate.getConceptName());
                    }

                    onChange();
                }
            }
        });
        
        knowledgeTypeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    // only set knowledge to false if the value is explicitly false since this also the default.
                    BooleanEnum isKnowledge = Boolean.FALSE.equals(event.getValue()) ? BooleanEnum.FALSE : BooleanEnum.TRUE;
                    properties.setIsKnowledge(isKnowledge);
                    
                    onChange();
                }
            }
        });
        
        skillTypeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

                    // only set skill if value is explicitly true. Otherwise it goes to default value.
                    BooleanEnum isKnowledge = Boolean.TRUE.equals(event.getValue()) ? BooleanEnum.FALSE : BooleanEnum.TRUE;
                    properties.setIsKnowledge(isKnowledge);
                    
                    onChange();
                }
            }
        });

        if (readOnly) {
            ltiIdentifierDropdown.setEnabled(false);
            ltiURL.setEnabled(false);
            ltiCustomParametersTable.setEnabled(false);
            allowScore.setEnabled(false);
            scoreSlider.setEnabled(false);
            knowledgeTypeButton.setEnabled(false);
            skillTypeButton.setEnabled(false);
            displayModeDropdown.setEnabled(false);
        }
    }
    
    /**
     * Toggles the active learner state attribute option for LTI.
     * 
     * @param isKnowledge true if the knowledge option is being selected.
     */
    private void setLtiAttributeActive(boolean isKnowledge) {
        knowledgeTypeButton.setActive(isKnowledge);
        skillTypeButton.setActive(!isKnowledge);
    }

    /**
     * Initializes the concept table.
     */
    private void initConceptTable() {
        conceptCellTable.setPageSize(Integer.MAX_VALUE);
        
        // attach the 'Concepts:' cell table to its associated columns 
        
        conceptCellTable.addColumn(conceptSelectionColumn);
        conceptCellTable.setColumnWidth(conceptSelectionColumn, "0px");
        
        conceptCellTable.addColumn(conceptNameColumn);
        
        FlowPanel emptyConceptsWidget = new FlowPanel();
        emptyConceptsWidget.setSize("100%", "100%");
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        Label emptyConceptsLabel = new Label("No course concepts were found. Please make sure you have added a list or hierarchy of concepts to the course summary.");
        emptyConceptsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyConceptsWidget.add(emptyConceptsLabel);
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        conceptCellTable.setEmptyTableWidget(emptyConceptsWidget);
    }
    
    @UiHandler("ltiIdentifierDropdown")
    void onValueChangeLtiProvider(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onValueChangeLtiProvider triggered: " + event);
        }

        if (currentMedia != null && currentMedia.getMediaTypeProperties() instanceof LtiProperties) {
            LtiProperties properties = (LtiProperties) currentMedia.getMediaTypeProperties();

            // max options == 1, so just pull first event value
            String selectedItem = event.getValue() != null && !event.getValue().isEmpty() ? event.getValue().get(0) : null;
            properties.setLtiIdentifier(selectedItem);

            ltiIdentifierDropdown.refresh();
            
            onChange();
        }
    }
    
    /**
     * Populates the select dropdown with the LTI providers that have been configured.
     * 
     * @param ltiProviders the list of LTI providers
     * @param propertyLtiProviderId the selected LTI provider id from the properties. Can be null.
     * @return LtiProvider returns the selected LTI provider if it exists. Can be null.
     */
    private LtiProvider populateLtiProviderIdSelectList(List<LtiProvider> ltiProviders, String propertyLtiProviderId) {

        LtiProvider selectedProvider = null;

        ltiIdentifierDropdown.clear();

        if (ltiProviders != null) {

            List<String> ltiProviderIds = new ArrayList<String>();
            for (LtiProvider provider : ltiProviders) {
                ltiProviderIds.add(provider.getIdentifier());

                if (selectedProvider == null) {
                    if (propertyLtiProviderId != null && !propertyLtiProviderId.isEmpty()
                            && provider.getIdentifier().equalsIgnoreCase(propertyLtiProviderId)) {
                        selectedProvider = provider;
                    }
                }
            }

            // make sure the list is sorted
            Collections.sort(ltiProviderIds);

            OptGroup courseOptionGroup = new OptGroup();
            for (String providerId : ltiProviderIds) {
                Option option = new Option();
                option.setText(providerId);
                option.setValue(providerId);
                courseOptionGroup.add(option);
            }

            ltiIdentifierDropdown.add(courseOptionGroup);

            if (selectedProvider == null && propertyLtiProviderId != null && !propertyLtiProviderId.isEmpty()) {
                OptGroup extraneousOptionGroup = new OptGroup();
                extraneousOptionGroup.setStyleName(style.groupLabel());
                extraneousOptionGroup.setLabel(EXTRANEOUS_LTI_PROVIDER_IDS_GROUP_LABEL);

                Option opt = new Option();
                opt.setText(propertyLtiProviderId);
                opt.setValue(propertyLtiProviderId);
                opt.setColor(EXTRANEOUS_LTI_PROVIDER_IDS_GROUP_TEXT_COLOR);

                extraneousOptionGroup.add(opt);
                ltiIdentifierDropdown.add(extraneousOptionGroup);

                // only add this label to the regular option group because there are extraneous
                // concepts for this survey item which will have a label for that section of
                // listed concepts
                courseOptionGroup.setStyleName(style.groupLabel());
                courseOptionGroup.setLabel(COURSE_LTI_PROVIDER_IDS_GROUP_LABEL);
            }
        }

        ltiIdentifierDropdown.refresh();
        return selectedProvider;
    }
    
    /**
     * Updates the slider label descriptions for novice, journeyman, and expert.
     * 
     * @param properties the LTI properties.
     * @param slider the slider used to determine the score.
     */
    public void updateScoreSliderLabels(Range scoreRange, double minPossible, double maxPossible) {

        StringBuilder noviceStr = new StringBuilder();
        StringBuilder journeymanStr = new StringBuilder();
        StringBuilder expertStr = new StringBuilder();

        if (scoreRange != null) {
            double minScore = scoreRange.getMinValue();
            double maxScore = scoreRange.getMaxValue();
            
            // novice is unused if minimum is 0
            if (minScore == minPossible) {
                noviceStr.append("unused");
            } else {
                noviceStr.append(minPossible);
                if (minScore > 1) {
                    noviceStr.append("-").append(Double.toString(minScore - 1));
                }
                noviceStr.append("%");
            }

            // journeyman is unused if the min and max are equal
            if (minScore == maxScore) {
                journeymanStr.append("unused");
            } else {
                journeymanStr.append(minScore);
                if (maxScore - minScore > 1) {
                    journeymanStr.append("-").append(Double.toString(maxScore - 1));
                }
                journeymanStr.append("%");
            }

            // expert is never unused
            if (maxScore == maxPossible) {
                expertStr.append(maxScore);
            } else {
                expertStr.append(maxScore).append("-").append(maxPossible);
            }
            expertStr.append("%");
        }

        noviceText.setText(noviceStr.toString());
        journeymanText.setText(journeymanStr.toString());
        expertText.setText(expertStr.toString());
    }
    
    /**
     * Selects the web page file with the given file name, updating both the UI and the backing course objects as necessary to
     * reference the web page
     * 
     * @param fileName the path the file from this course's folder
     */
    private void selectLocalWebpage(final String fileName) {
        
        //update the backing media object
        if(currentMedia != null) {
            currentMedia.setUri(fileName);
        }
        
        //update the backing metadata object, if necessary
        if(currentMetadata != null){
            if(currentMedia == null) {
                generated.metadata.Metadata.Simple simple = new generated.metadata.Metadata.Simple();
                simple.setValue(fileName);
                currentMetadata.setContent(simple);
            }

            if(currentMedia.getName() == null){
                currentMedia.setName(fileName);
            }
        }
        
        //update the UI showing the file name
        localWebpageFileLabel.setText(fileName != null ? fileName : NO_FILE_LABEL);
                    
        if(fileName != null){
            
            final String simpleFileName = fileName.substring(
                    fileName.lastIndexOf("/") + 1,
                    fileName.lastIndexOf(".")
            );
            
            //auto fill the title textbox to help the author, they can always change it
            if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                linkBox.setValue(simpleFileName, true);
            }
            
            localWebpageSelectedPanel.setVisible(true);
            selectLocalWebpagePanel.setVisible(false);
            
            if(currentMetadata != null && currentMetadata.getPresentAt() != null &&
                    currentMetadata.getPresentAt().getMerrillQuadrant() != null){
            
                //check to see if other metadata files reference this web page, since they may need to be update
                GetMetadataFilesForMerrillQuadrant action = new GetMetadataFilesForMerrillQuadrant(
                        GatClientUtility.getUserName(), 
                        courseFolderPath + "/" + fileName, 
                        currentMetadata.getPresentAt().getMerrillQuadrant()
                );
                
                BsLoadingDialogBox.display("Please Wait", "Checking for metadata references.");
                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<StringListResult>(){

                    @Override
                    public void onFailure(Throwable thrown) {
                        
                        BsLoadingDialogBox.remove();
                        
                        new ErrorDetailsDialog( 
                                "An error occurred while checking for metadata references. You can continue adding content to "
                                + "this Merrill's branch point, but if you want to edit an existing metadata file for this "
                                + "content, you will have to find it manually in the file navigator to edit it.", 
                                thrown.toString(), 
                                null).center();
                    }

                    @Override
                    public void onSuccess(StringListResult result) {
                        
                        BsLoadingDialogBox.remove();
                        
                        if(result.isSuccess() && result.getStrings() != null){
                            
                            if(!result.getStrings().isEmpty()){
                            
                                new MetadataFileReferenceListDialog(fileName, result.getStrings()).center();
                            }
                            
                        } else {
                            
                            if(result.getErrorMsg() != null){
                                
                                if(result.getErrorDetails() != null){
                                    
                                    new ErrorDetailsDialog(
                                            result.getErrorMsg(), 
                                            result.getErrorDetails(), 
                                            null).center();
                                } else {
                                    
                                    new ErrorDetailsDialog(
                                            "An error occurred while checking for metadata references. You can continue adding content to "
                                            + "this Merrill's branch point, but if you want to edit an existing metadata file for this "
                                            + "content, you will have to find it manually in the file navigator to edit it.", 
                                            result.getErrorMsg(), 
                                            null).center();
                                }
                            
                            
                            } else {
                                
                                if(result.getErrorDetails() != null){
                                    
                                    new ErrorDetailsDialog(
                                        "An error occurred while checking for metadata references. You can continue adding content to "
                                        + "this Merrill's branch point, but if you want to edit an existing metadata file for this "
                                        + "content, you will have to find it manually in the file navigator to edit it.", 
                                        result.getErrorDetails(), 
                                        null).center();
                
                                } else {
                                
                                    new ErrorDetailsDialog(
                                            "An error occurred while checking for metadata references. You can continue adding content to "
                                            + "this Merrill's branch point, but if you want to edit an existing metadata file for this "
                                        + "content, you will have to find it manually in the file navigator to edit it.", 
                                        "No details available.", 
                                        null).center();
                                }
                            }
                        }
                    }
                });
               
            }
        }
        
        onChange();
    }

    /**
     * Initialize the Image UI components and handling.
     * 
     * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
     * true indicates editing metadata and false indicates creating metadata
     */
    private void initImage(){
        
        imageFileDialog.setAllowedFileExtensions(Constants.image_supported_types);
        imageFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        imageFileDialog.setIntroMessageHTML(" Select an image file.<br> Supported extensions are :<b>"+Constants.image_supported_types+"</b>");
     
        selectLocalImagePanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                imageFileDialog.center();
            }
        });
        
        if(readOnly) {
            removeLocalImageButton.setVisible(false);
        }
        removeLocalImageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }
                
                 DeleteRemoveCancelDialog.show("Delete Content", 
                        "Do you wish to <b>permanently delete</b> '"+localImageFileLabel.getText()+
                        "' from the course or simply remove the reference to that content in this metadata object?<br><br>"+
                                "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                        new DeleteRemoveCancelCallback() {
                    
                            @Override
                            public void cancel() {
                                
                            }

                            @Override
                            public void delete() {
                                
                              String username = GatClientUtility.getUserName();
                              String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                              List<String> filesToDelete = new ArrayList<String>();
                              final String filePath = courseFolderPath + "/" + localImageFileLabel.getText();
                              filesToDelete.add(filePath);
                              
                              DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                              SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                  @Override
                                  public void onFailure(Throwable error) {
                                      ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                              "Failed to delete the file.", 
                                              error.getMessage(), 
                                              DetailedException.getFullStackTrace(error));
                                      dialog.setDialogTitle("Deletion Failed");
                                      dialog.center();
                                  }

                                  @Override
                                  public void onSuccess(GatServiceResult result) {
                                      
                                      if(result.isSuccess()){
                                          logger.warning("Successfully deleted the file '"+filePath+"'.");
                                      } else{
                                          logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                      }                                           
                                       
                                      resetUI();
                                  }
                                  
                              });
                            }

                            @Override
                            public void remove() {
                                resetUI();
                            }
                            
                            private void resetUI(){                                     
                                if(currentMedia != null){
                                    currentMedia.setUri(null); 
                                    
                                    if(currentMetadata != null) {
                                        currentMetadata.setDisplayName(null);
                                    }
                                    
                                    localImageFileLabel.setText("Select Image");
                                    localImageFileLabel.setVisible(true);
                                    localImageSelectedPanel.setVisible(false);
                                    selectLocalImagePanel.setVisible(true);
                                    
                                    onChange();
                                }
                            }

                 }, "Delete Content");
            }
        });
        
        imageFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null){
                    currentMedia.setUri(event.getValue());
                    showImageThumbnail(event.getValue());

                    if (event.getValue() == null || linkBox.getText().isEmpty()) {
                        final String filename = event.getValue();

                        //auto fill the title textbox to help the author, they can always change it
                        if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                            linkBox.setValue(filename, true);
                        }
                    }
                }
                
                onChange();
            }
        });
    
    }
    
    private void initVideo() {
        videoFileDialog.setAllowedFileExtensions(Constants.VIDEO);
        videoFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        videoFileDialog.setIntroMessageHTML(" Select a video file.<br> Supported extensions are :<b>"+Constants.VIDEO+"</b>");
        localVideoUnitWidth.clear();
        localVideoUnitHeight.clear();
        for (VideoCssUnitsEnum unitEnum : VideoCssUnitsEnum.VALUES()) {
            localVideoUnitWidth.addItem(unitEnum.getDisplayName(), unitEnum.getName());
            localVideoUnitHeight.addItem(unitEnum.getDisplayName(), unitEnum.getName());
        }
        
        
        selectLocalVideoPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                videoFileDialog.center();
            }
        });
        
        if(readOnly) {
            removeLocalVideoButton.setVisible(false);
        }
        removeLocalVideoButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }
                
                 DeleteRemoveCancelDialog.show("Delete Content", 
                        "Do you wish to <b>permanently delete</b> '"+localVideoFileLabel.getText()+
                        "' from the course or simply remove the reference to that content in this metadata object?<br><br>"+
                                "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
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
                              SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                  @Override
                                  public void onFailure(Throwable error) {
                                      ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                              "Failed to delete the file.", 
                                              error.getMessage(), 
                                              DetailedException.getFullStackTrace(error));
                                      dialog.setDialogTitle("Deletion Failed");
                                      dialog.center();
                                  }

                                  @Override
                                  public void onSuccess(GatServiceResult result) {
                                      
                                      if(result.isSuccess()){
                                          logger.warning("Successfully deleted the file '"+filePath+"'.");
                                      } else{
                                          logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                      }                                           
                                       
                                      resetUI();
                                  }
                                  
                              });
                            }

                            @Override
                            public void remove() {
                                resetUI();
                            }
                            
                            private void resetUI(){                                     
                                if(currentMedia != null){
                                    currentMedia.setUri(null); 
                                    
                                    if(currentMetadata != null) {
                                        currentMetadata.setDisplayName(null);
                                    }
                                    
                                    localVideoFileLabel.setText("Select Video");
                                    localVideoFileLabel.setVisible(true);
                                    localVideoSelectedPanel.setVisible(false);
                                    selectLocalVideoPanel.setVisible(true);
                                    
                                    onChange();
                                }
                            }

                 }, "Delete Content");
            }
        });
        
        videoFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null){
                    currentMedia.setUri(event.getValue());
                    showVideoThumbnail(event.getValue());

                    if (event.getValue() == null || linkBox.getText().isEmpty()) {
                        final String filename = event.getValue();

                        //auto fill the title textbox to help the author, they can always change it
                        if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                            linkBox.setValue(filename, true);
                        }
                    }
                }
                
                onChange();
            }
        });
        
        localVideoUnitWidth.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent arg0) {
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties != null && properties.getSize() != null) {
                        String units = localVideoUnitWidth.getSelectedValue();
                        
                        properties.getSize().setWidthUnits(units);
                    }
                }
                
                
            }
            
        });
        
        
        localVideoUnitHeight.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent arg0) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties != null && properties.getSize() != null) {
                        String units = localVideoUnitHeight.getSelectedValue();
                        
                        properties.getSize().setHeightUnits(units);
                    }
                }
                
            }
            
        });
        
        localVideoSizeCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if(event.getValue()){
                        setDefaultVideoPropertiesSize(properties);
                        
                    } else {
                        properties.setSize(null);
                    }
                
                    localVideoSizePanel.setVisible(event.getValue());
                }
                
                onChange();
            }
        });
        
        localVideoWidthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                 if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                     
                    if(properties.getSize() == null){
                        setDefaultVideoPropertiesSize(properties);
                    }
                    
                    try{
                        
                        properties.getSize().setWidth(
                                event.getValue() != null 
                                    ? new BigDecimal(event.getValue())
                                    : null
                        );
                        
                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                        
                        localVideoWidthBox.setValue(null);
                        
                        WarningDialog.error("Width Error", "Please enter a numeric decimal or integer value.");
                    }
                }
                
                onChange();
            }
        });
        
        localVideoHeightBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                    
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                     
                    if(properties.getSize() == null){
                        setDefaultVideoPropertiesSize(properties);
                    }
                    
                    try{
                        
                        properties.getSize().setHeight(
                                event.getValue() != null 
                                    ? new BigDecimal(event.getValue())
                                    : null
                        );
                        
                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                        
                        localVideoHeightBox.setValue(null);
                        
                        WarningDialog.error("Height Error", "Please enter a numeric decimal or integer value.");
                    }
                }
                
                onChange();
            }
        });
        
        localVideoFullScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                    
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                     
                    properties.setAllowFullScreen(
                            event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                    );
                }
                
                onChange();
            }
        });
        
        localVideoAutoPlayCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){

                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();

                    properties.setAllowAutoPlay(
                            event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                            );
                }

                onChange();
            }
        }); 
        
        localConstrainToScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof VideoProperties){
                    VideoProperties properties = (VideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties.getSize() != null) {
                        properties.getSize().setConstrainToScreen(
                            event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                            );
                    }
                }
                
                onChange();
            } 
        });
        
        if(readOnly) {
            localVideoUnitWidth.setEnabled(false);
            localVideoUnitHeight.setEnabled(false);
            localVideoSizeCheck.setEnabled(false);
            localVideoWidthBox.setEnabled(false);
            localVideoHeightBox.setEnabled(false);
            localVideoFullScreenCheck.setEnabled(false);
            localVideoAutoPlayCheck.setEnabled(false);
            localConstrainToScreenCheck.setEnabled(false);
        }
    }
    
    /**
     * Sets the default local video properties size values.
     * 
     * @param props The properties object to set the size for.
     */
    private void setDefaultVideoPropertiesSize(VideoProperties props) {
        logger.info("setDefaultVideoPropertiesSize()");
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
     * Initialize the Youtube UI components and handling.
     * 
     * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
     * true indicates editing metadata and false indicates creating metadata
     */
    private void initYoutube(){

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
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties != null && properties.getSize() != null) {
                        String units = videoUnitWidth.getSelectedValue();
                        
                        properties.getSize().setWidthUnits(units);
                    }
                }
                
                
            }
            
        });
        
        
        videoUnitHeight.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent arg0) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties != null && properties.getSize() != null) {
                        String units = videoUnitHeight.getSelectedValue();
                        
                        properties.getSize().setHeightUnits(units);
                    }
                }
                
            }
            
        });
        
        videoSizeCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if(event.getValue()){
                        setDefaultYoutubeVideoPropertiesSize(properties);
                        
                    } else {
                        properties.setSize(null);
                    }
                
                    videoSizePanel.setVisible(event.getValue());
                }
                
                onChange();
            }
        });
        
        videoTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null) {
                    
                    String value = (event.getValue() != null && !event.getValue().trim().isEmpty()) ? event.getValue() : null;
                    
                    currentMedia.setUri(value);
                    showYoutubeThumbnail(value);
                }
                
                onChange();
            }
        });
        
        videoDurationButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(currentMetadata != null){
                 
                    updateYouTubeVideoDuration(videoTextBox.getValue());
                }
            }
        });
        
        videoWidthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                 if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                     
                    if(properties.getSize() == null){
                        setDefaultYoutubeVideoPropertiesSize(properties);
                    }
                    
                    try{
                        
                        properties.getSize().setWidth(
                                event.getValue() != null 
                                    ? new BigDecimal(event.getValue())
                                    : null
                        );
                        
                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                        
                        videoWidthBox.setValue(null);
                        
                        WarningDialog.error("Width Error", "Please enter a numeric decimal or integer value.");
                    }
                }
                
                onChange();
            }
        });
        
        videoHeightBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                    
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                     
                    if(properties.getSize() == null){
                        setDefaultYoutubeVideoPropertiesSize(properties);
                    }
                    
                    try{
                        
                        properties.getSize().setHeight(
                                event.getValue() != null 
                                    ? new BigDecimal(event.getValue())
                                    : null
                        );
                        
                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                        
                        videoHeightBox.setValue(null);
                        
                        WarningDialog.error("Height Error", "Please enter a numeric decimal or integer value.");
                    }
                }
                
                onChange();
            }
        });
        
        videoFullScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                    
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                     
                    properties.setAllowFullScreen(
                            event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                    );
                }
                
                onChange();
            }
        });
        
        videoAutoPlayCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){

                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();

                    properties.setAllowAutoPlay(
                            event.getValue()
                            ? BooleanEnum.TRUE
                                    : BooleanEnum.FALSE
                            );
                }

                onChange();
            }
        }); 
        
        constrainToScreenCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof YoutubeVideoProperties){
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) currentMedia.getMediaTypeProperties();
                    
                    if (properties.getSize() != null) {
                        properties.getSize().setConstrainToScreen(
                            event.getValue()
                            ? BooleanEnum.TRUE
                                    : BooleanEnum.FALSE
                            );
                    }
                }
                
                onChange();
            }
                
        });

        
        videoUnderDwellCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentAssessmentList != null){
                    
                    if(event.getValue()){
                        
                        if(currentAssessmentList.getAssessment() == null){
                            currentAssessmentList.setAssessment(new Assessment());
                        }
                        
                        if(currentAssessmentList.getAssessment().getUnderDwell() == null){
                            currentAssessmentList.getAssessment().setUnderDwell(new UnderDwell());
                        }
                        
                        if(currentAssessmentList.getAssessment().getUnderDwell().getDuration() != null){
                            videoMinimumTimeBox.setValue(currentAssessmentList.getAssessment().getUnderDwell().getDuration().intValue());
                            
                        } else if(videoMinimumTimeBox.getValue() != null && videoMinimumTimeBox.getValue() != 0){
                            ValueChangeEvent.fire(videoMinimumTimeBox, videoMinimumTimeBox.getValue());
                            
                        } else if(videoDurationBox.getValue() != null && videoDurationBox.getValue() != 0){
                            videoMinimumTimeBox.setValue(videoDurationBox.getValue(), true);
                                
                        } else {
                            videoMinimumTimeBox.setValue(null);
                        }
                        
                        if(currentAssessmentList.getAssessment().getUnderDwell().getFeedback() == null 
                                && videoUnderDwellFeedback.getValue() != null){
                            
                            //if the user entered a feedback message earlier, try to reload it
                            ValueChangeEvent.fire(videoUnderDwellFeedback, videoUnderDwellFeedback.getValue());
                        }
                        
                        videoUnderDwellFeedback.setValue(currentAssessmentList.getAssessment().getUnderDwell().getFeedback());
                        
                        videoUnderDwellCollapse.show();
                        
                    } else {
                        
                        if(currentAssessmentList.getAssessment() != null){
                            
                            currentAssessmentList.getAssessment().setUnderDwell(null);
                            
                            if(currentAssessmentList.getAssessment().getOverDwell() == null){
                                currentAssessmentList.setAssessment(null);
                            }
                        }
                        
                        videoUnderDwellCollapse.hide();
                    }
                    
                    videoDurationLabel.setVisible(currentAssessmentList.getAssessment() != null);
                    
                    onChange();
                    
                }
            }
        });
        
        videoMinimumTimeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                
                if(currentAssessmentList.getAssessment() == null){
                    currentAssessmentList.setAssessment(new Assessment());
                }
                
                if(currentAssessmentList.getAssessment().getUnderDwell() == null){
                    currentAssessmentList.getAssessment().setUnderDwell(new UnderDwell());
                }
                
                if(event.getValue() != null){
                    currentAssessmentList.getAssessment().getUnderDwell().setDuration(BigInteger.valueOf(event.getValue()));
                
                } else {
                    currentAssessmentList.getAssessment().getUnderDwell().setDuration(null);
                }
                
                onChange();
            }
        });
        
        videoUnderDwellFeedback.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentAssessmentList.getAssessment() == null){
                    currentAssessmentList.setAssessment(new Assessment());
                }
                
                if(currentAssessmentList.getAssessment().getUnderDwell() == null){
                    currentAssessmentList.getAssessment().setUnderDwell(new UnderDwell());
                }
                
                if(!videoUnderDwellFeedback.getHtmlEditor().isEmpty()){
                    currentAssessmentList.getAssessment().getUnderDwell().setFeedback(event.getValue());
                    
                } else {
                    currentAssessmentList.getAssessment().getUnderDwell().setFeedback(null);
                }
                
                onChange();
            }
        });
        
        videoOverDwellCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentAssessmentList != null){
                    
                    if(event.getValue()){
                        
                        if(currentAssessmentList.getAssessment() == null){
                            currentAssessmentList.setAssessment(new Assessment());
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell() == null){
                            currentAssessmentList.getAssessment().setOverDwell(new OverDwell());
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell().getDuration() == null){
                            currentAssessmentList.getAssessment().getOverDwell().setDuration(new OverDwell.Duration());
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() == null){
                            
                            if(videoOverDwellPercentButton.getValue() 
                                    && videoPercentDurationBox.getValue() != null
                                    && videoPercentDurationBox.getValue() != 0){
                                
                                //if the user entered a percent earlier, try to reload it
                                ValueChangeEvent.fire(videoPercentDurationBox, videoPercentDurationBox.getValue());
                                ValueChangeEvent.fire(videoOverDwellPercentButton, videoOverDwellPercentButton.getValue());
                                
                            } else if(videoOverDwellFixedButton.getValue() 
                                    && videoMaximumTimeBox.getValue() != null
                                    && videoMaximumTimeBox.getValue() != 0){
                            
                                //if the user entered a fixed duration earlier, try to reload it
                                ValueChangeEvent.fire(videoMaximumTimeBox, videoMaximumTimeBox.getValue());
                                ValueChangeEvent.fire(videoOverDwellFixedButton, videoOverDwellFixedButton.getValue());
                                
                            } else {
                                
                                //otherwise, just create a new assessment duration
                                currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(new DurationPercent());
                            }
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell().getFeedback() == null 
                                && videoOverDwellFeedback.getValue() != null){
                            
                            //if the user entered a feedback message earlier, try to reload it
                            ValueChangeEvent.fire(videoOverDwellFeedback, videoOverDwellFeedback.getValue());
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent){
                            
                            DurationPercent percent = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                            
                            if(percent.getPercent() == null){
                                
                                //default to 150% duration
                                percent.setPercent(BigInteger.valueOf(DEFAULT_VIDEO_DURATION_PERCENT));
                            }
                            
                            videoPercentDurationBox.setValue(percent.getPercent().intValue());
                            
                            if(videoDurationBox.getValue() != null){
                                
                                percent.setTime(BigInteger.valueOf(videoDurationBox.getValue()));
                                videoCalculatedPercentBox.setValue(videoDurationBox.getValue() * percent.getPercent().intValue()/100);
                                
                            } else {
                                
                                percent.setTime(null);
                                videoCalculatedPercentBox.setValue(0);
                            }
                            
                            videoOverDwellPercentButton.setValue(true);
                            videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellPercentPanel));
                            
                        } else if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof BigInteger){
                            
                            BigInteger duration = (BigInteger) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                            
                            videoMaximumTimeBox.setValue(duration.intValue());
                            
                            videoOverDwellFixedButton.setValue(true);
                            videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellFixedPanel));
                            
                        }
                        
                        videoOverDwellFeedback.setValue(currentAssessmentList.getAssessment().getOverDwell().getFeedback());
                        
                        videoOverDwellCollapse.show();
                        
                    } else {
                        
                        if(currentAssessmentList.getAssessment() != null){
                            
                            currentAssessmentList.getAssessment().setOverDwell(null);
                            
                            if(currentAssessmentList.getAssessment().getUnderDwell() == null){
                                currentAssessmentList.setAssessment(null);
                            }
                        }
                        
                        videoOverDwellCollapse.hide();
                    }
                    
                    //show the video duration box if an assessment is available
                    videoDurationLabel.setVisible(currentAssessmentList.getAssessment() != null);
                    
                    onChange();
                    
                }
            }
        });
        
        videoOverDwellPercentButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() == null
                    || !(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent)){
                    
                    if(videoPercentDurationBox.getValue() != null
                            && videoPercentDurationBox.getValue() != 0){
                        
                        //if the user entered a percent earlier, try to reload it
                        ValueChangeEvent.fire(videoPercentDurationBox, videoPercentDurationBox.getValue());
                        
                    } else {
                        currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(new DurationPercent());
                    }
                }
                
                DurationPercent percent = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                
                if(percent.getPercent() == null){
                    
                    //default to 150% duration
                    percent.setPercent(BigInteger.valueOf(DEFAULT_VIDEO_DURATION_PERCENT));
                }
                
                videoPercentDurationBox.setValue(percent.getPercent().intValue());
                
                if(videoDurationBox.getValue() != null){
                    percent.setTime(BigInteger.valueOf(videoDurationBox.getValue()));
                    videoCalculatedPercentBox.setValue(videoDurationBox.getValue() * percent.getPercent().intValue()/100);
                    
                } else {
                    percent.setTime(null);
                    videoCalculatedPercentBox.setValue(0);
                }
                
                videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellPercentPanel));
                
                onChange();
            }
        });
        
        videoOverDwellFixedButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() == null
                    || !(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof BigInteger)){
                    
                    if(videoMaximumTimeBox.getValue() != null
                            && videoMaximumTimeBox.getValue() != 0){
                    
                        //if the user entered a fixed duration earlier, try to reload it
                        ValueChangeEvent.fire(videoMaximumTimeBox, videoMaximumTimeBox.getValue());
                        
                    } else if(videoDurationBox.getValue() != null){
                            currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(BigInteger.valueOf((int) (videoDurationBox.getValue() * 1.5)));
                            
                    } else {
                        currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(BigInteger.valueOf(0));
                    }
                }
                
                BigInteger duration = (BigInteger) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                
                videoMaximumTimeBox.setValue(duration.intValue());
                
                videoOverDwellDeck.showWidget(videoOverDwellDeck.getWidgetIndex(videoOverDwellFixedPanel));
                
                onChange();
            }
        });
        
        videoPercentDurationBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                
                if(currentAssessmentList.getAssessment() == null){
                    currentAssessmentList.setAssessment(new Assessment());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell() == null){
                    currentAssessmentList.getAssessment().setOverDwell(new OverDwell());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell().getDuration() == null){
                    currentAssessmentList.getAssessment().getOverDwell().setDuration(new OverDwell.Duration());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() == null
                        || !(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent)){
                    
                    currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(new DurationPercent());
                }
                
                DurationPercent duration = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                
                if(event.getValue() != null){
                    duration.setPercent(BigInteger.valueOf(event.getValue()));
                    
                    if(duration.getTime() != null){
                        videoCalculatedPercentBox.setValue(duration.getTime().intValue() * event.getValue()/100);
                        
                    } else {
                        videoCalculatedPercentBox.setValue(0);
                    }
                
                } else {
                    duration.setPercent(null);
                    videoCalculatedPercentBox.setValue(0);
                }
                
                onChange();
            }
        });
        
        videoMaximumTimeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                
                if(currentAssessmentList.getAssessment() == null){
                    currentAssessmentList.setAssessment(new Assessment());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell() == null){
                    currentAssessmentList.getAssessment().setOverDwell(new OverDwell());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell().getDuration() == null){
                    currentAssessmentList.getAssessment().getOverDwell().setDuration(new OverDwell.Duration());
                }
                
                if(event.getValue() != null){
                    currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(BigInteger.valueOf(event.getValue()));
                
                } else {
                    currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(null);
                }
                
                onChange();
            }
        });
        
        videoOverDwellFeedback.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentAssessmentList.getAssessment() == null){
                    currentAssessmentList.setAssessment(new Assessment());
                }
                
                if(currentAssessmentList.getAssessment().getOverDwell() == null){
                    currentAssessmentList.getAssessment().setOverDwell(new OverDwell());
                }
                
                if(!videoOverDwellFeedback.getHtmlEditor().isEmpty()){
                    currentAssessmentList.getAssessment().getOverDwell().setFeedback(event.getValue());
                    
                } else {
                    currentAssessmentList.getAssessment().getOverDwell().setFeedback(null);
                }
                
                onChange();
            }
        });
        
        if(readOnly) {
            videoSizeCheck.setEnabled(false);
            videoTextBox.setEnabled(false);
            videoWidthBox.setEnabled(false);
            videoUnitWidth.setEnabled(false);
            videoHeightBox.setEnabled(false);
            videoUnitHeight.setEnabled(false);
            videoFullScreenCheck.setEnabled(false);
            videoAutoPlayCheck.setEnabled(false);
            constrainToScreenCheck.setEnabled(false);
            videoOverDwellCheck.setEnabled(false);
            videoUnderDwellCheck.setEnabled(false);
            videoMinimumTimeBox.setEnabled(false);
            videoUnderDwellFeedback.setEditable(false);
            videoMaximumTimeBox.setEnabled(false);
            videoPercentDurationBox.setEnabled(false);
            videoOverDwellFeedback.setEditable(false);
        }
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
     * Updates the currently stored video duration with the length of the YouTube video at the given URL. This duration value will be used
     * to populate some of the video underdwell and overdwell fields and is also used to supply the video length when a percent-based
     * overflow duration is being used
     * 
     * @param videoUrl the URL of the YouTube video whose length should be used
     */
    private void updateYouTubeVideoDuration(String videoUrl) {
        
        final Integer oldDuration = videoDurationBox.getValue();
        
        videoDurationBox.setValue(0);
        
        if(currentAssessmentList != null 
                && currentAssessmentList.getAssessment() != null 
                && currentAssessmentList.getAssessment().getOverDwell() != null 
                && currentAssessmentList.getAssessment().getOverDwell().getDuration() != null 
                && currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() != null
                && currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent){
            
            DurationPercent duration = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
            
            if(duration.getTime() == null){
                
                duration.setTime(BigInteger.valueOf(0));
                videoCalculatedPercentBox.setValue(0);
            }
        }
         
        if(videoUrl != null && !videoUrl.isEmpty()){
            
            GatClientUtility.getYouTubeVideoLength(videoUrl, new Callback<Integer, String>() {
                
                @Override
                public void onSuccess(Integer duration) {
                    
                    videoDurationBox.setValue(duration);
                    
                    if(currentAssessmentList != null 
                            && currentAssessmentList.getAssessment() != null){
                        
                        if(currentAssessmentList.getAssessment().getUnderDwell() != null){
                            
                            if(currentAssessmentList.getAssessment().getUnderDwell().getDuration() != null){

                                BigInteger minDuration = currentAssessmentList.getAssessment().getUnderDwell().getDuration();
                                
                                if(oldDuration == minDuration.intValue()){
                                    
                                    currentAssessmentList.getAssessment().getUnderDwell().setDuration(BigInteger.valueOf(duration));
                                    
                                    videoMinimumTimeBox.setValue(duration);
                                }
                                
                            } else {
                                currentAssessmentList.getAssessment().getUnderDwell().setDuration(BigInteger.valueOf(duration));
                                
                                videoMinimumTimeBox.setValue(duration);
                            }
                        }
                        
                        if(currentAssessmentList.getAssessment().getOverDwell() != null 
                                && currentAssessmentList.getAssessment().getOverDwell().getDuration() != null 
                                && currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() != null){
                        
                            if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof DurationPercent){
                            
                                DurationPercent durationPercent = (DurationPercent) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                                    
                                durationPercent.setTime(BigInteger.valueOf(duration));
                                
                                if(durationPercent.getPercent() != null){
                                    videoCalculatedPercentBox.setValue(duration * durationPercent.getPercent().intValue()/100);
                                    
                                } else {
                                    videoCalculatedPercentBox.setValue(0);
                                }
                                
                            } else if(currentAssessmentList.getAssessment().getOverDwell().getDuration().getType() instanceof BigInteger){
                            
                                BigInteger fixedDuration = (BigInteger) currentAssessmentList.getAssessment().getOverDwell().getDuration().getType();
                                
                                if(oldDuration == fixedDuration.intValue()){
                                    
                                    currentAssessmentList.getAssessment().getOverDwell().getDuration().setType(BigInteger.valueOf(duration));
                                    
                                    videoMaximumTimeBox.setValue(duration);
                                }
                            }
                        }
                        
                        onChange();
                    }
                }
                
                @Override
                public void onFailure(String errorMsg) {
                    
                    logger.info("Unable to get YouTube video properties: " + errorMsg);
                    
                    onChange();
                }
            });
        }
    }

    private void initPDF() {
         
       pdfFileDialog.setAllowedFileExtensions(new String[]{".pdf"});
       pdfFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
              
       selectPDFFilePanel.addClickHandler(new ClickHandler() {
           
           @Override
           public void onClick(ClickEvent event) {
               pdfFileDialog.center();
           }
       });
       
       if(readOnly) {
           removePDFButton.setVisible(false);
       }
       
       removePDFButton.addClickHandler(new ClickHandler() {
           
           @Override
           public void onClick(ClickEvent event) {
               if(readOnly) {
                   // Make sure logic doesn't execute if a user modifies the DOM
                   return;
               }

                DeleteRemoveCancelDialog.show("Delete Content", 
                       "Do you wish to <b>permanently delete</b> '"+pdfFileLabel.getText()+
                       "' from the course or simply remove the reference to that content in this metadata object?<br><br>"+
                               "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                       new DeleteRemoveCancelCallback() {
                   
                           @Override
                           public void cancel() {
                               
                           }

                           @Override
                           public void delete() {
                               
                             String username = GatClientUtility.getUserName();
                             String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                             List<String> filesToDelete = new ArrayList<String>();
                             final String filePath = courseFolderPath + "/" + pdfFileLabel.getText();
                             filesToDelete.add(filePath);
                             
                             DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                             SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                 @Override
                                 public void onFailure(Throwable error) {
                                     ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                             "Failed to delete the file.", 
                                             error.getMessage(), 
                                             DetailedException.getFullStackTrace(error));
                                     dialog.setDialogTitle("Deletion Failed");
                                     dialog.center();
                                 }

                                 @Override
                                 public void onSuccess(GatServiceResult result) {
                                     
                                     if(result.isSuccess()){
                                         logger.warning("Successfully deleted the file '"+filePath+"'.");
                                     } else{
                                         logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                     }                                           
                                      
                                     resetUI();
                                 }
                                 
                             });
                           }

                           @Override
                           public void remove() {
                               resetUI();
                           }
                           
                           private void resetUI(){                                     
                               if(currentMedia != null){
                                   
                                   currentMedia.setUri(null); 
                                   if(currentMetadata != null) {
                                       currentMetadata.setDisplayName(null);              
                                   }
                                   
                                   pdfFileLabel.setText("Select PDF");
                                   pdfSelectedPanel.setVisible(false);
                                   selectPDFFilePanel.setVisible(true);
                                   
                                   onChange();
                               }
                           }

                }, "Delete Content");
           }
       });

       
       pdfFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
           
           @Override
           public void onValueChange(ValueChangeEvent<String> event) {
               
               if(currentMedia != null){
                   
                   currentMedia.setUri(event.getValue());
                   
                   if(event.getValue() != null){
                       pdfFileLabel.setText(event.getValue());
                       
                       final String filename = event.getValue();
                       
                       //auto fill the title textbox to help the author, they can always change it
                       if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                           linkBox.setValue(filename, true);
                       }
                       
                       pdfSelectedPanel.setVisible(true);
                       selectPDFFilePanel.setVisible(false);
                       
                   } else {
                       pdfFileLabel.setText("Select PDF");
                       pdfSelectedPanel.setVisible(false);
                       selectPDFFilePanel.setVisible(true);
                   }
               }
               
               onChange();
           }
       });
    
    }
    
    private void initSlideShow() {
        
        continueCheckbox.setValue(true);
        previousCheckbox.setValue(true);
        
        if(GatClientUtility.isReadOnly()) {
            selectPptLabel.setText("No slide show selected");
            selectPptLabel.setTitle("Slide shows cannot be uploaded since the course is in read-only mode.");
            selectPptPanel.getElement().getStyle().setProperty("cursor", "not-allowed");
            continueCheckbox.setEnabled(false);
            previousCheckbox.setEnabled(false);
            replaceSlideShowButton.setEnabled(false);
            removePptButton.setVisible(false);
            replaceSlideShowButton.addStyleName("buttonDisabled");
            
        } else {
                        
            String[] supportedPpt = {Constants.ppt_show_supported_types[0], Constants.ppt_show_supported_types[1], Constants.ppt_show_supported_types[3]};
            pptFileSelectionDialog.setIntroMessageHTML("<span style=\"font-size: 15px; margin-left: 10px; margin-top: 4px;\">"
                    + "Choose a PowerPoint show file to convert to a GIFT slideshow <b>(" + supportedPpt[0] + ")</b>");
            pptFileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
            pptFileSelectionDialog.getFileSelector().setAllowedFileExtensions(supportedPpt);
            pptFileSelectionDialog.setAdditionalFileExtensionInfo("<br/>You may need to do the following: <br/>"
                    + "1. Edit the presentation in PowerPoint<br/>"
                    + "2. Click 'Save As'<br/>"
                    + "3. Select <b>.pps</b> under 'Save as type'");
                        
        }

        replaceSlideShowButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                pptFileSelectionDialog.center();
                replaceSlideShow = true;

            }

        });
                
        pptFileSelectionDialog.setUploadHandler(new CanHandleUploadedFile() {

            @Override
            public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {

                CreateSlideShow action = new CreateSlideShow();
                action.setPptFilePath(uploadFilePath);
                action.setUsername(GatClientUtility.getUserName());
                action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
                action.setReplaceExisting(replaceSlideShow);
                
                if(currentLessonMaterial != null){
                    action.setCourseObjectName(currentLessonMaterial.getTransitionName());
                    
                } else if(currentMedia != null){
                    
                    if(currentMedia.getUri() != null
                            && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties){
                        
                        //the existing media object is already a slide show, so the transition name must be the slide folder name
                        FileTreeModel firstSlideModel = FileTreeModel.createFromRawPath(currentMedia.getUri());
                        
                        if(firstSlideModel.getParentTreeModel() != null){
                            action.setCourseObjectName(firstSlideModel.getParentTreeModel().getFileOrDirectoryName());
                        }
                    }
                }
                
                if(action.getCourseObjectName() == null){
                    
                    //if we couldn't find the existing transition name for the slide show, generate a new name for the slides folder
                    Date date = new Date();
                    DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                    action.setCourseObjectName("LessonMaterialContent_" + format.format(date));
                }
                
                final String filename = uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                
                //auto fill the title textbox to help the author, they can always change it
                if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                    linkBox.setValue(filename, true);
                }
                
                //display successful upload message in Notify UI not a dialog
                pptFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {
                    
                    @Override
                    public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                        WarningDialog.warning(title, text, callback);
                    }
                    
                    @Override
                    public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                        Notify.notify("", "'"+ filename +"' was converted to a GIFT slide show.", IconType.INFO, NotifyUtil.generateDefaultSettings());
                    }
                    
                    @Override
                    public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                        // Let the onFailure method display ErrorDetailsDialog
                    }
                    
                    @Override
                    public void showDetailedErrorMessage(String text, String details, List<String> stackTrace, ModalDialogCallback callback) {
                        // Let the onFailure method display ErrorDetailsDialog
                    }
                });
                
                copyOrUploadSlideShow(action, null, callback);
            }

        });
        
        pptFileSelectionDialog.setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(final FileTreeModel source, final CopyFileCallback callback) {
                
                CreateSlideShow action = new CreateSlideShow();
                action.setUsername(GatClientUtility.getUserName());
                action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
                action.setReplaceExisting(replaceSlideShow);
                action.setPptFilePath(source.getRelativePathFromRoot());
                
                if(currentLessonMaterial != null){
                    action.setCourseObjectName(currentLessonMaterial.getTransitionName());
                    
                } else if(currentMedia != null){
                    
                    if(currentMedia.getUri() != null
                            && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties){
                        
                        //the existing media object is already a slide show, so the transition name must be the slide folder name
                        FileTreeModel firstSlideModel = FileTreeModel.createFromRawPath(currentMedia.getUri());
                        
                        if(firstSlideModel.getParentTreeModel() != null){
                            action.setCourseObjectName(firstSlideModel.getParentTreeModel().getFileOrDirectoryName());
                        }
                    }
                }
                
                if(action.getCourseObjectName() == null){
                    
                    //if we couldn't find the existing transition name for the slide show, generate a new name for the slides folder
                    Date date = new Date();
                    DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                    action.setCourseObjectName("LessonMaterialContent_" + format.format(date));
                }
                
               final String filename = source.getFileOrDirectoryName();
                               
               //auto fill the title textbox to help the author, they can always change it
               if(linkBox.getText() == null || linkBox.getText().isEmpty()){
                   linkBox.setValue(filename, true);
               }
                
                //display successful upload message in Notify UI not a dialog
                pptFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {
                    
                    @Override
                    public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                        WarningDialog.warning(title, text, callback);
                    }
                    
                    @Override
                    public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                        Notify.notify("", "'"+ filename +"' was converted to a GIFT slide show.", IconType.INFO, NotifyUtil.generateDefaultSettings());
                    }
                    
                    @Override
                    public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                        // Let the onFailure method display ErrorDetailsDialog
                    }

                    @Override
                    public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, ModalDialogCallback callback) {
                        // Let the onFailure method display ErrorDetailsDialog
                    }
                });
                
                copyOrUploadSlideShow(action, callback, null);
            }
            
        });
        
        selectPptPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if(!GatClientUtility.isReadOnly()) {
                    
                    pptFileSelectionDialog.center();
                }
            }
            
        });
        
        removePptButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                OkayCancelDialog.show("Delete Slide Show Images", "Do you wish to <b>permanently delete</b> the slide show images?", "Delete",
                        new OkayCancelCallback() {
                    
                            @Override
                            public void okay() {
                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<String>();

                                //can't use media name to find path to specific course object slideshow folder, need to dissect image path name.
                                if(currentMedia.getMediaTypeProperties() instanceof SlideShowProperties){
                                    
                                    SlideShowProperties props = (SlideShowProperties)currentMedia.getMediaTypeProperties();
                                    List<String> images = props.getSlideRelativePath();
                                    if(images == null || images.isEmpty()){
                                        //nothing to delete because there are no images in the generated object
                                        return;
                                    }
                                    
                                    String imageOne = images.get(0);
                                    int startIndex = imageOne.indexOf(SLIDE_SHOW_FOLDER_NAME);
                                    if(startIndex == -1){
                                        //ERROR
                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                "Failed to delete slide show images.", 
                                                "The file name of the first of "+images.size()+" slide show images doesn't contain the default slide show folder name of "+SLIDE_SHOW_FOLDER_NAME+
                                                ".  This key is needed in order to determine the name of the folder to delete.\n\n1st image file name = "+imageOne, 
                                                null);
                                        dialog.setDialogTitle("Deletion Failed");
                                        dialog.center();
                                    }
                                    
                                    int nameIndex = startIndex + SLIDE_SHOW_FOLDER_NAME.length() + 1;
                                    int endNameIndex = imageOne.indexOf(Constants.FORWARD_SLASH, nameIndex);
                                    String slideshowFolderName = imageOne.substring(nameIndex, endNameIndex);
                                    
                                    final String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + SLIDE_SHOW_FOLDER_NAME + Constants.FORWARD_SLASH + slideshowFolderName;
                                    filesToDelete.add(filePath);
                                    
                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                        @Override
                                        public void onFailure(Throwable error) {
                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                    "Failed to delete the file.", 
                                                    error.getMessage(), 
                                                    DetailedException.getFullStackTrace(error));
                                            dialog.setDialogTitle("Deletion Failed");
                                            dialog.center();
                                        }

                                        @Override
                                        public void onSuccess(GatServiceResult result) {
                                            
                                            if (result.isSuccess()) {   
                                                SlideShowProperties properties = new SlideShowProperties();
                                                currentMedia.setMediaTypeProperties(properties);
                                                resetPanel(properties, lockMetadata);
                                                onChange();
                                                
                                            } else {
                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to delete the file: " + filePath,
                                                        result.getErrorMsg(),
                                                        result.getErrorStackTrace());
                                                dialog.setDialogTitle("Deletion Failed");
                                                dialog.center();
                                            }
                                        }
                                        
                                    });
                                    
                                    SlideShowProperties properties = new SlideShowProperties();
                                    currentMedia.setMediaTypeProperties(properties);
                                    resetPanel(properties, lockMetadata);
                                }else{
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to delete slide show images.", 
                                            "There appears to be a logic error in the course creator.  The current media object properties needs to be of type "+SlideShowProperties.class+
                                            " but are instead of type "+currentMedia.getMediaTypeProperties()+".  Therefore the path to the slide show folder to delete can't be deteremined.",
                                            null);
                                    dialog.setDialogTitle("Deletion Failed");
                                    dialog.center();
                                }

                            }

                            @Override
                            public void cancel() {
                                
                            }
                        });
            }
            
        });
        
        previousCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue() != null && currentMedia != null && currentMedia.getMediaTypeProperties()  instanceof SlideShowProperties) {
                    ((SlideShowProperties) currentMedia.getMediaTypeProperties()).setDisplayPreviousSlideButton(
                            (event.getValue()) ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });
        
        continueCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(event.getValue() != null && currentMedia != null && currentMedia.getMediaTypeProperties()  instanceof SlideShowProperties) {
                    ((SlideShowProperties) currentMedia.getMediaTypeProperties()).setKeepContinueButton(
                            (event.getValue()) ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });
    }
    
    /**
     * Gets a string describing the validation problems with the current training application. If no problems are found, then an 
     * empty string will be returned.
     * 
     * @return the validation error message
     */
    public String getValidationErrors(){
        
        StringBuilder errorMsg = new StringBuilder();
        
        if(currentMetadata != null && currentMedia != null){
            
            Media media = currentMedia;
            Serializable genericProperties = media.getMediaTypeProperties();
            
            if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(slideShowPanel)){
                
                if(genericProperties instanceof SlideShowProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to give this Slide Show.")
                                .append("</li>");
                    }
                    
                    SlideShowProperties properties = (SlideShowProperties) genericProperties;
                    
                    if(properties.getSlideRelativePath().isEmpty()){
                        
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify a PowerPoint file from which to generate a slideshow.")
                                .append("</li>");
                    }
                    
                } 
                
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(pdfPanel)){
                
                if(genericProperties instanceof PDFProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to give this PDF.")
                                .append("</li>");
                    }
                    
                    if(media.getUri() == null){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the PDF file to be used.")
                                .append("</li>");
                    }
                }
                
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(localImagePanel)){
                
                if(genericProperties instanceof ImageProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to give this image.")
                                .append("</li>");
                    }
                    
                    if(media.getUri() == null){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the image file to be used.")
                                .append("</li>");
                    }
                }
                
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(localVideoPanel)){
                
                if(genericProperties instanceof VideoProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to give this video.")
                                .append("</li>");
                    }
                    
                    if(media.getUri() == null){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the video file to be used.")
                                .append("</li>");
                    }
                }
            
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(youTubePanel)){
                
                if(genericProperties instanceof YoutubeVideoProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to be give this Youtube video.")
                                .append("</li>");
                    }
                    
                    if(media.getUri() == null){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the URL where this YouTube video is located.")
                                .append("</li>");
                    }
                    
                    YoutubeVideoProperties properties = (YoutubeVideoProperties) genericProperties;
                    
                    if(properties.getSize() != null){
                        
                        if(properties.getSize().getWidth() == null){
                            
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("Please specify a width for this YouTube video.")
                                    .append("</li>");
                        }
                        
                        if(properties.getSize().getHeight() == null){
                            
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("Please specify a height for this YouTube video.")
                                    .append("</li>");
                        }
                    }
                    
                    if(currentMetadata != null && currentMetadata.getPresentAt() != null 
                            && (MerrillQuadrantEnum.RULE.getName().equals(currentMetadata.getPresentAt().getMerrillQuadrant())
                                    || MerrillQuadrantEnum.EXAMPLE.getName().equals(currentMetadata.getPresentAt().getMerrillQuadrant()))){
                    
                        //if this YouTube video is used in a Rule or Example quadrant, check its assessments as well
                    if(currentAssessmentList != null && currentAssessmentList.getAssessment() != null){
                        
                        UnderDwell underdwell = currentAssessmentList.getAssessment().getUnderDwell();
                        
                        if(underdwell != null){
                            
                            if(underdwell.getDuration() == null){
                                
                                errorMsg.append("")
                                        .append("<li>")
                                        .append("Please specify a minimum viewing time for this video.")
                                        .append("</li>");
                            
                            } else if(BigInteger.ZERO.compareTo(underdwell.getDuration()) == 0){
                                    
                                errorMsg.append("")
                                        .append("<li>")
                                        .append("Please specify a non-zero minimum viewing time for this video.")
                                        .append("</li>");
                                }
                                
                            if(underdwell.getFeedback() == null || underdwell.getFeedback().isEmpty()){
                                
                                errorMsg.append("")
                                        .append("<li>")
                                        .append("Please specify the feedback message to display when a learner ")
                                        .append("doesn't spend enough time on this video.")
                                        .append("</li>");
                            }
                        }
                        
                        OverDwell overdwell = currentAssessmentList.getAssessment().getOverDwell();
                        
                        if(overdwell != null){
                            
                            if(overdwell.getDuration() != null 
                                    && overdwell.getDuration().getType() != null){
                                
                                if(overdwell.getDuration().getType() instanceof BigInteger){
                                    
                                    if(BigInteger.ZERO.compareTo((BigInteger) overdwell.getDuration().getType()) == 0){
                                        
                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("Please specify a non-zero maximum viewing time for this video.")
                                                .append("</li>");
                                    }
                                    
                                } else if(overdwell.getDuration().getType() instanceof DurationPercent){
                                    
                                    DurationPercent duration = (DurationPercent) overdwell.getDuration().getType();
                                    
                                    if(duration.getPercent() == null){
                                        
                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("Please specify a maxiumum viewing percentage for this video.")
                                                .append("</li>");
                                    
                                    } else if(BigInteger.ZERO.compareTo(duration.getPercent()) == 0){
                                            
                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("Please specify a non-zero maxiumum viewing percentage for this video.")
                                                .append("</li>");
                                    }
                                    
                                    if(duration.getTime() == null || BigInteger.ZERO.compareTo(duration.getTime()) == 0){
                                            
                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("The duration of the YouTube video at the specified video URL could not be calculated. ")
                                                .append("Please ensure that the URL points to a valid YouTube video.")
                                                .append( "</li>");
                                    }
                                    
                                } else {
                                    
                                    errorMsg.append("")
                                            .append("<li>")
                                            .append("Please specify a maximum viewing time for this video.")
                                            .append("</li>");
                                }
                                
                            
                            } else {
                                
                                errorMsg.append("")
                                        .append("<li>")
                                        .append("Please specify what type of timing metric should be used to detect ")
                                        .append("when a learner spends too much time watching this video.")
                                        .append("</li>");
                            }
                            
                            if(overdwell.getFeedback() == null || overdwell.getFeedback().isEmpty()){
                                
                                errorMsg.append("")
                                        .append("<li>")
                                        .append("Please specify the feedback message to display when a learner spends too much time on this video.")
                                        .append("</li>");
                            }
                        }
                    }
                }
                }
                
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(localVideoPanel)){
                
                if(genericProperties instanceof VideoProperties){
                    
                    if(media.getName() == null || media.getName().isEmpty()){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the name to give this video.")
                                .append("</li>");
                    }
                    
                    if(media.getUri() == null){
                        errorMsg.append("")
                                .append("<li>")
                                .append("Please specify the video file to be used.")
                                .append("</li>");
                    }
                }
                
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(localWebpagePanel)){
                
                if(media.getName() == null || media.getName().isEmpty()){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this webpage.")
                            .append("</li>");
                }
                
                if(media.getUri() == null) {
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append("Please specify the webpage file to be used.")
                            .append("</li>");
                }
            
            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(webAddressPanel)){
                
                if(media.getName() == null || media.getName().isEmpty()){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this website.")
                            .append("</li>");
                }
                
                if(media.getUri() == null){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append("Please specify the web address for the content to be used.")
                            .append("</li>");
                    
                }else{
                    
                    boolean passedPrefixCheck = false;
                    String lowercaseURL = media.getUri().toLowerCase();
                    for(String prefix : Constants.VALID_URL_SCHEMES){
                        
                        //case insensitive search
                        String lowercasePrefix = prefix.toLowerCase();
                        if(lowercaseURL.startsWith(lowercasePrefix)){
                            passedPrefixCheck = true;
                            break;
                        }
                    }
                    
                    if(!passedPrefixCheck){
                        errorMsg.append("")
                                .append("<li>")
                                .append("The URL must start with one of the following prefixes: ").append(Constants.VALID_URL_SCHEMES)
                                .append(".</li>");
                    }
                }
            } else if (mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(ltiPanel)) {
                // validate media name
                if (media.getName() == null || media.getName().isEmpty()) {
                    errorMsg.append("<li>").append("Please specify the name to give this LTI provider.").append("</li>");
                }

                // validate media url
                if (media.getUri() == null) {
                    errorMsg.append("<li>").append("Please specify the LTI provider URL address for the content to be used.").append("</li>");
                } else {
                    boolean passedPrefixCheck = false;
                    String lowercaseURL = media.getUri().toLowerCase();
                    for (String prefix : Constants.VALID_URL_SCHEMES) {

                        // case insensitive search
                        String lowercasePrefix = prefix.toLowerCase();
                        if (lowercaseURL.startsWith(lowercasePrefix)) {
                            passedPrefixCheck = true;
                            break;
                        }
                    }

                    if (!passedPrefixCheck) {
                        errorMsg.append("<li>").append("The LTI provider URL must start with one of the following prefixes: ")
                                .append(Constants.VALID_URL_SCHEMES).append(".</li>");
                    }
                }
                
                // check lti property specific fields
                if (genericProperties instanceof LtiProperties) {
                    LtiProperties properties = (LtiProperties) genericProperties;

                    // validate LTI provider id
                    if (properties.getLtiIdentifier() == null || properties.getLtiIdentifier().isEmpty()) {
                        errorMsg.append("<li>").append("You must select an LTI identifier.").append("</li>");
                    }

                    // validate custom parameters
                    if (properties.getCustomParameters() != null && !properties.getCustomParameters().getNvpair().isEmpty()) {
                        for (Nvpair customParam : properties.getCustomParameters().getNvpair()) {
                            boolean blankName = customParam.getName() == null || customParam.getName().trim().isEmpty();
                            boolean blankValue = customParam.getValue() == null || customParam.getValue().trim().isEmpty();
                            if (blankName && blankValue) {
                                // gets removed automatically
                                continue;
                            } else if (blankName || blankValue) {
                                // one of these is blank while the other is not.
                                errorMsg.append("<li>")
                                        .append("You have an incomplete custom parameter. Please complete the missing field or remove the entry.")
                                        .append("</li>");
                                
                                // Only need one of these error messages so exit for loop
                                break;
                            }
                        }
                    }

                    // validate display mode
                    if (properties.getDisplayMode() == null) {
                        errorMsg.append("<li>").append("You must select a display mode.").append("</li>");
                    }
                }
            }
        } else if (currentMetadata != null && currentMedia == null) {
                
            if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(localWebpagePanel)){
                
                if(currentMetadata.getDisplayName() == null){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this website.")
                            .append("</li>");
                }

                if(currentMetadata.getContent() == null || !(currentMetadata.getContent() instanceof generated.metadata.Metadata.Simple) ||
                        ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).getValue() == null) {

                    errorMsg.append("")
                            .append("<li>")
                            .append("Please specify the webpage file to be used.")
                            .append("</li>");
                }

            } else if(mediaDeckPanel.getVisibleWidget() == mediaDeckPanel.getWidgetIndex(webAddressPanel)){
                
                if(currentMetadata.getDisplayName() == null){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this website.")
                            .append("</li>");
                }

                if(currentMetadata.getContent() == null || !(currentMetadata.getContent() instanceof generated.metadata.Metadata.URL) ||
                        ((generated.metadata.Metadata.URL)currentMetadata.getContent()).getValue() == null) {

                    errorMsg.append("")
                            .append("<li>")
                            .append("Please specify the web address for the content to be used.")
                            .append("</li>");

                } else {

                    boolean passedPrefixCheck = false;
                    String lowercaseURL = ((generated.metadata.Metadata.URL)currentMetadata.getContent()).getValue().toLowerCase();
                    for(String prefix : Constants.VALID_URL_SCHEMES){

                        //case insensitive search
                        String lowercasePrefix = prefix.toLowerCase();
                        if(lowercaseURL.startsWith(lowercasePrefix)){
                            passedPrefixCheck = true;
                            break;
                        }
                    }

                    if(!passedPrefixCheck){
                        errorMsg.append("")
                                .append("<li>")
                                .append("The URL must start with one of the following prefixes: ").append(Constants.VALID_URL_SCHEMES)
                                .append(".</li>");
                    }
                }

            }
            
        } else {
            
            errorMsg.append("")
                    .append("<li>")
                    .append("No content type has been selected.")
                    .append("</li>");
        }
        
        return errorMsg.toString();
    }
    
    /**
     * Gets the Slide Show folder created when the user uploads a PowerPoint show or null if no folder was created.
     * 
     * @return The path to the slide show folder (e.g. Course/Slide Shows/myPpt). Can be null.
     */
    public String getCreatedSlideShowFolder() {
        if(currentMedia != null && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {
            if(!((SlideShowProperties) currentMedia.getMediaTypeProperties()).getSlideRelativePath().isEmpty()) {
                return GatClientUtility.getBaseCourseFolderPath() + "/Slide Shows/" + currentMedia.getName();
            }
        }

        return null;
    }
    
    public HasClickHandlers getReplaceSlideShowInput(){
        return replaceSlideShowButton;
    }

    public DefaultGatFileSelectionDialog getAddSlideShowInput() {
        return pptFileSelectionDialog;
    }
    

    /**
     * Performs a request to the server to create the slide show file. If successful, the slide paths are copied to the Slide Show course object properties.
     * 
     * @param action The action to perform
     * @param copyCallback The callback to execute if an existing slide show is being copied or null if this is an upload operation.
     * @param uploadCallback The callback to execute if a new slide show is being uploaded or null if this is a copy operation.
     */
    private void copyOrUploadSlideShow(final CreateSlideShow action, final CopyFileCallback copyCallback, final HandleUploadedFileCallback uploadCallback) {
        
        progressModal.startPollForProgress();
        pptFileSelectionDialog.hide();
        
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CreateSlideShowResult>() {

            @Override
            public void onFailure(Throwable caught) {
                
                progressModal.stopPollForProgress(true);
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "A server error occured while creating the slide show.", 
                        "The action failed on the server: " + caught.getMessage(), null);
                dialog.setText("Error");
                dialog.center();
                
                if(copyCallback != null) {
                    copyCallback.onFailure(caught);
                    
                } else {
                    uploadCallback.onFailure(caught);
                }
                
            }

            @Override
            public void onSuccess(final CreateSlideShowResult result) {

                progressModal.stopPollForProgress(!result.isSuccess());
                
                if(result.isSuccess()) {
                    
                    SlideShowProperties properties = new SlideShowProperties();
                    Serializable props = currentMedia.getMediaTypeProperties();
                    if(props != null && props instanceof SlideShowProperties) {
                        // Use the existing properties to retain continue & previous checkbox values
                        
                        properties = (SlideShowProperties) props;                       
                          
                        //clear the previous in memory list of slides in the slides folder
                        //in order to re-populate the list with the latest from the server
                        properties.getSlideRelativePath().clear();
                        
                        if(properties.getDisplayPreviousSlideButton() == null) {
                            // Set the values if they haven't been initialized already 
                            
                            properties.setDisplayPreviousSlideButton(BooleanEnum.TRUE);
                            properties.setKeepContinueButton(BooleanEnum.TRUE);
                        }
                    }
                    
                    for(String path : result.getRelativeSlidePaths()) {
                        properties.getSlideRelativePath().add(path);
                    }

                    if(currentMedia.getName() == null){
                        currentMedia.setName(currentLessonMaterial.getTransitionName());
                    }
                    
                    currentMedia.setMediaTypeProperties(properties);
                    currentMedia.setUri(result.getRelativeSlidePaths().get(0));
                    resetPanel(properties, lockMetadata);
                    
                    GatClientUtility.saveCourseAndNotify();
                    
                    if(copyCallback != null) {
                        copyCallback.onSuccess(result.getSlidesFolderModel());
                        
                    } else {
                        uploadCallback.onSuccess(result.getSlidesFolderModel());
                    }
                    
                    onChange();
                                                
                } else if (result.getHasNameConflict()) {
                    
                    final SetNameDialog renameDialog = new SetNameDialog(
                            "The Slide Show Already Exists", 
                            "A Slide Show with the name <b>" + result.getNameConflict() + "</b> already exists. Please enter a new name for the Slide Show.", 
                            "Rename Slide Show");
                    
                    renameDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            if(event.getValue() != null && !event.getValue().isEmpty() && !event.getValue().equals(result.getNameConflict())) {
                                action.setCourseObjectName(event.getValue());
                                copyOrUploadSlideShow(action, copyCallback, uploadCallback);
                                renameDialog.hide();
                            }
                        }
                        
                    });
                    
                    renameDialog.center();
                    
                } else {                
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), 
                            result.getErrorDetails(), result.getErrorStackTrace());
                    dialog.setText("Error");
                    dialog.center();
                    
                    if(copyCallback != null) {
                        copyCallback.onFailure(result.getErrorMsg());
                    } else {
                        uploadCallback.onFailure(result.getErrorMsg());
                    }
                }
            }

        });
    }
    
    /**
     * Performs a request to the server to extract the contents of a .zip file. If successful, the path to the base web page will be copied 
     * to the Local Webpage course objects. If more than one web page is extracted from the .zip, the author will be prompted to select 
     * which one should be used.
     * 
     * @param action The action to perform
     * @param copyCallback The callback to execute if an existing .zip is being copied or null if this is an upload operation.
     * @param uploadCallback The callback to execute if a new slide .zip is being uploaded or null if this is a copy operation.
     */
    private void copyOrUploadZip(final UnzipFile action, final CopyFileCallback copyCallback, final HandleUploadedFileCallback uploadCallback) {
        
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

            @Override
            public void onFailure(Throwable caught) {
                
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "A server error occured while unzipping the file.", 
                        "The action failed on the server: " + caught.getMessage(), DetailedException.getFullStackTrace(caught));
                dialog.setText("Error");
                dialog.center();
                
                if(copyCallback != null) {
                    copyCallback.onFailure(caught);
                    
                } else {
                    uploadCallback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(final GatServiceResult result) {
                
                webpageFileDialog.hide();
                
                unzipProgressModal.startPollForProgress(new AsyncCallback<LoadedProgressIndicator<UnzipFileResult>>() {
                    
                    @Override
                    public void onSuccess(final LoadedProgressIndicator<UnzipFileResult> response) {
                        
                        if(response.isComplete() && response.getPayload() != null){
                            
                            final UnzipFileResult result = response.getPayload();
                            
                            if(result.isSuccess()) {
                                
                                //invoke the callback of whatever operation initially called this method (copy or upload)
                                if(copyCallback != null) {
                                    copyCallback.onSuccess(result.getUnzippedFolderModel());
                                    
                                } else {
                                    uploadCallback.onSuccess(result.getUnzippedFolderModel());
                                }
                                
                                int numHtmlFiles = 0;
                                String firstHtmlFile = null;
                                
                                //interate through the list of files that were extracted and identify the number of HTML files
                                for(String file : result.getUnzippedFolderModel().getFileNamesUnderModel()){
                                    
                                    for(String extension : Constants.html_supported_types){
                                        
                                        if(file.endsWith(extension)){
                                            
                                            numHtmlFiles++;
                                            
                                            if(firstHtmlFile == null){
                                                firstHtmlFile = file;
                                            }
                                            
                                            break;
                                        }
                                    }
                                }
                                
                                if(numHtmlFiles == 0){
                                    
                                    //report a warning if no HTML files were found
                                    WarningDialog.warning(
                                            "No Web Pages Found", 
                                            "No web page files (e.g. .htm, .html) were extracted from the ZIP archive you selected. <br/><br/>"
                                            + "The files that were extracted will remain in your course folder, but they will not be used by "
                                            + "this Local Webpage object. If you wish to discard these files, you can use the "
                                            + "Media panel to delete them."
                                    );
                                    
                                } else if(numHtmlFiles == 1){
                                    
                                    //if only one HTML file was extracted, select it
                                    String parentPath = result.getUnzippedFolderModel().getFileOrDirectoryName();
                                    
                                    selectLocalWebpage(parentPath + "/" + firstHtmlFile);
                                    
                                    onChange();
                                
                                } else {
                                    
                                    //if more than one HTML file was extracted, prompt the author to select which one they want to use
                                    final FileSelectionDialog selectTargetDialog = new FileSelectionDialog(new CanGetRootDirectory() {
                                        
                                        @Override
                                        public void getRootDirectory(GetRootDirectoryCallback callback) {
                                            callback.onSuccess(result.getUnzippedFolderModel());
                                        }
                                        
                                    }, DefaultMessageDisplay.includeAllMessages);
                                    
                                    selectTargetDialog.setAllowedFileExtensions(Constants.html_supported_types);
                                    selectTargetDialog.setIntroMessageHTML("The contents of the ZIP archive you selected have been successfully "
                                            + "extracted to this course's folder. <br/><br/>"
                                            + "Multiple web pages were found in the extracted ZIP archive. Please select the web page "
                                            + "file you wish to display.");
                                    
                                    selectTargetDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
                                        
                                        @Override
                                        public void onValueChange(ValueChangeEvent<String> event) {
                                            
                                            selectTargetDialog.hide();
                                            
                                            //author has selected the HTML file they want to use, so use it
                                            String selectedHtmlFile = event.getValue();                             
                                            String parentPath = result.getUnzippedFolderModel().getFileOrDirectoryName();
                                            
                                            selectLocalWebpage(parentPath + "/" + selectedHtmlFile);
                                            
                                            onChange();
                                        }
                                    });
                                    
                                    selectTargetDialog.center();
                                }
                                                            
                            } else {                
                                
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an error occurred on the server: " + result.getErrorMsg(), 
                                        result.getErrorDetails(), 
                                        result.getErrorStackTrace()
                                
                                        );
                                dialog.setText("Error");
                                dialog.center();
                                
                                if(copyCallback != null) {
                                    copyCallback.onFailure(result.getErrorMsg());
                                } else {
                                    uploadCallback.onFailure(result.getErrorMsg());
                                }
                            }
                            
                        } else {
                            
                            if(response.getException() != null){
                                
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an error occurred on the server: "
                                                + response.getException().getReason(), 
                                        response.getException().getReason(), 
                                        response.getException().getErrorStackTrace()
                                );
                                dialog.setText("Error");
                                dialog.center();
                                
                                if(copyCallback != null) {
                                    copyCallback.onFailure(response.getException().getReason());
                                } else {
                                    uploadCallback.onFailure(response.getException().getReason());
                                }
                                
                            } else {
                                
                                //this shouldn't be possible to hit, but handle it just in case
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an unknown error occurred on the server.",
                                        "Failed to extract the archive because an unknown error occurred on the server.",
                                        null);
                                dialog.setText("Error");
                                dialog.center();
                                
                                if(copyCallback != null) {
                                    copyCallback.onFailure(response.getException().getReason());
                                } else {
                                    uploadCallback.onFailure(response.getException().getReason());
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                "A server error occured while unzipping the file.", 
                                "The action failed on the server: " + caught.getMessage(), DetailedException.getFullStackTrace(caught));
                        dialog.setText("Error");
                        dialog.center();
                        
                        if(copyCallback != null) {
                            copyCallback.onFailure(caught);
                            
                        } else {
                            uploadCallback.onFailure(caught);
                        }
                    }
                });
            }

        });
    }
    
    /**
     * Gets the list of course LTI providers. If null, will return an empty list.
     * 
     * @return the list of course LTI providers
     */
    public List<LtiProvider> getCourseLtiProviderList() {
        if (courseLtiProviderIdList == null) {
            courseLtiProviderIdList = new ArrayList<LtiProvider>();
        }
        return courseLtiProviderIdList;
    }
    
    /**
     * Gets the table containing the selected and unselected concepts.
     * 
     * @return the concept table.
     */
    public HasData<CandidateConcept> getConceptsTable(){
        return conceptCellTable;
    }    

    @Override
    public void onReadOnlyChange(boolean isReadOnly) {
        linkBox.setEnabled(!isReadOnly);
        replaceSlideShowButton.setEnabled(!isReadOnly);
        removePptButton.setEnabled(!isReadOnly);
        previousCheckbox.setEnabled(!isReadOnly);
        continueCheckbox.setEnabled(!isReadOnly);
        removePDFButton.setEnabled(!isReadOnly);
        removeLocalWebpageButton.setEnabled(!isReadOnly);
        removeLocalImageButton.setEnabled(!isReadOnly);
        removeLocalVideoButton.setEnabled(!isReadOnly);
        urlTextBox.setEnabled(!isReadOnly);
        urlPreviewButton.setEnabled(!isReadOnly);
        localVideoUnitWidth.setEnabled(!isReadOnly);
        localVideoUnitHeight.setEnabled(!isReadOnly);
        localVideoSizeCheck.setEnabled(!isReadOnly);
        localVideoWidthBox.setEnabled(!isReadOnly);
        localVideoHeightBox.setEnabled(!isReadOnly);
        localVideoFullScreenCheck.setEnabled(!isReadOnly);
        localVideoAutoPlayCheck.setEnabled(!isReadOnly);
        localConstrainToScreenCheck.setEnabled(!isReadOnly);
        videoTextBox.setEnabled(!isReadOnly);
        videoUnitHeight.setEnabled(!isReadOnly);
        videoUnitWidth.setEnabled(!isReadOnly);
        videoSizeCheck.setEnabled(!isReadOnly);
        videoWidthBox.setEnabled(!isReadOnly);
        videoHeightBox.setEnabled(!isReadOnly);
        videoFullScreenCheck.setEnabled(!isReadOnly);
        videoAutoPlayCheck.setEnabled(!isReadOnly);
        constrainToScreenCheck.setEnabled(!isReadOnly);
        ltiURL.setEnabled(!isReadOnly);
        ltiIdentifierDropdown.setEnabled(!isReadOnly);
        ltiCustomParametersTable.setEnabled(!isReadOnly);
        knowledgeTypeButton.setEnabled(!isReadOnly);
        skillTypeButton.setEnabled(!isReadOnly);
        scoreSlider.setEnabled(!isReadOnly);
        displayModeDropdown.setEnabled(!isReadOnly);
        allowScore.setEnabled(!isReadOnly);
        ltiCustomParametersTable.setEnabled(!isReadOnly);
    }
}