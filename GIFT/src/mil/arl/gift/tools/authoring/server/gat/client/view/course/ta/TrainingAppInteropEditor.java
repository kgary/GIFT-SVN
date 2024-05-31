/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import generated.course.AuthoringSupportElements;
import generated.course.BooleanEnum;
import generated.course.ConceptNode;
import generated.course.CustomInteropInputs;
import generated.course.CustomInteropInputs.LoadArgs;
import generated.course.DETestbedInteropInputs;
import generated.course.DISInteropInputs;
import generated.course.DkfRef;
import generated.course.EmbeddedApp;
import generated.course.EmbeddedAppInputs;
import generated.course.EmbeddedApps;
import generated.course.GenericLoadInteropInputs;
import generated.course.HAVENInteropInputs;
import generated.course.Interop;
import generated.course.InteropInputs;
import generated.course.Interops;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LogFile;
import generated.course.LogFile.DomainSessionLog;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.Media;
import generated.course.MobileApp;
import generated.course.Nvpair;
import generated.course.PowerPointInteropInputs;
import generated.course.RIDEInteropInputs;
import generated.course.SimpleExampleTAInteropInputs;
import generated.course.TC3InteropInputs;
import generated.course.TrainingApplication;
import generated.course.UnityInteropInputs;
import generated.course.VBSInteropInputs;
import generated.course.VREngageInteropInputs;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.GenericDataProvider;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.LogSpan;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DetailsDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.select.TrainingAppTypeSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.MediaPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.LoadedFileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModalCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.GiftWrapModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.NameValuePairEditor;
import mil.arl.gift.tools.authoring.server.gat.shared.XTSPImporterResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CourseFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CourseFilesExistResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchInteropImplementations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchInteropImplementationsResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ParsePlaybackLog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ParsePlaybackLogResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateUnityWebGLApplication;

/**
 * An editor used to populate a training application's interops and set its DKF file
 *
 * @author nroberts
 */
public class TrainingAppInteropEditor extends Composite implements CourseObjectModalCancelCallback, CourseReadOnlyHandler {

    private static Logger logger = Logger.getLogger(TrainingAppInteropEditor.class.getName());

    private static TrainingAppInteropEditorUiBinder uiBinder = GWT
            .create(TrainingAppInteropEditorUiBinder.class);

    interface TrainingAppInteropEditorUiBinder extends
            UiBinder<Widget, TrainingAppInteropEditor> {
    }

    /** label to show if no file was provided for a course object (when a file is needed) */
    private static final String NO_FILE_SELECTED = "No file selected";

    /** text to show if a unity application was provided for a unity course object */
    private static final String UNITY_FILE_SET = "Unity application";

    /** The file extension for archive (ZIP) files */
    private static final String ZIP = ".zip";

    /** The instructions to display in the file selection dialog used for Unity WebGL applications */
    private static final String LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS =
            " Select a web page file containing a Unity WebGL application.<br> Supported extensions are :<b>"
            + Constants.html_supported_types +".</b><br/><br/>"
            + "You can also select a <b>.zip</b> file containing a web page file and its resources (e.g. style sheets, scripts, "
            + "images, etc.) in order to load them simultaneously. This can be helpful when loading Unity applications "
            + "with many dependencies.";

    /** Tooltip when the captured audio file is not selected */
    private static final String CAPTURED_AUDIO_EMPTY = "Click to select an audio file";

    /** Tooltip when the captured audio file is selected */
    private static final String CAPTURED_AUDIO_SELECTED = "Click to remove the selected audio file";

    /** Default text for the captured audio button */
    private static final String CAPTURED_AUDIO_BTN_DEFAULT = "No captured audio";

    /** The file extensions that should be allowed by the file selection dialog used for Unity WebGL applications */
    private static final String[] UNITY_APP_ALLOWED_FILE_EXTENSIONS;
        
    /** The training application imported from Gift Wrap */
    private TrainingApplication importedTrainingApp = null;

    static{

        List<String> allowedExtensions = new ArrayList<>();
        allowedExtensions.addAll(Arrays.asList(Constants.html_supported_types));
        allowedExtensions.add(ZIP);

        String[] extensionsArray = new String[allowedExtensions.size()];

        UNITY_APP_ALLOWED_FILE_EXTENSIONS = allowedExtensions.toArray(extensionsArray);
    }

    @UiField
    protected InlineHTML titleLabel;

    @UiField
    protected TextBox simpleTAScenarioNameBox;

    @UiField
    protected TextBox testbedScenarioNameBox;

    @UiField
    protected TextBox vbsScenarioNameBox;

    @UiField
    protected TextBox tc3ScenarioNameBox;

    @UiField
    protected TextBox sudokuScenarioNameBox;

    @UiField
    protected TextBox aresIdTextbox;

    @UiField
    protected TextBox genericContentTextbox;

    @UiField
    protected Label selectFileLabel;

    @UiField
    protected Label pptFileLabel;

    @UiField
    protected DeckPanel applicationArgsDeckPanel;

    @UiField
    protected Widget noTaPanel;

    @UiField
    protected Widget simpleTaPanel;

    @UiField
    protected Widget vbsPanel;

    @UiField
    protected Widget tc3Panel;

    @UiField
    protected Widget aresPanel;
    
    /** The panel used to edit load arguments for HAVEN */
    @UiField
    protected Widget havenPanel;
    
    /** The panel used to edit load arguments for RIDE */
    @UiField
    protected Widget ridePanel;
    
    /** The panel used to edit load arguments for VR-Engage */
    @UiField
    protected Widget vrEngagePanel;

    /** The panel used to edit load arguments for desktop Unity */
    @UiField
    protected Widget unityDesktopPanel;

    /** The table used to edit load arguments for desktop Unity */
    @UiField
    protected NameValuePairEditor unityDesktopArgsTable;

    @UiField
    protected Widget unityPanel;

    @UiField
    protected MediaPanel mediaPanel;

    @UiField
    protected Widget mobileAppPanel;

    @UiField
    protected Widget genericInputPanel;

    @UiField
    protected Widget powerPointPanel;

    @UiField
    protected FocusPanel selectPPTFilePanel;

    @UiField
    protected Widget pptSelectedPanel;

    @UiField
    protected Button removePptButton;

    @UiField
    protected Widget sudokuPanel;

    @UiField
    protected Widget testbedPanel;

    @UiField
    protected Widget customPanel;

    @UiField
    protected RadioButton aresIdButton;

    @UiField
    protected RadioButton aresFileButton;

    @UiField
    protected Button selectAresFileButton;

    @UiField
    protected Label aresFileLabel;

    @UiField
    protected CellTable<Nvpair> argumentTable;

    @UiField
    protected CourseObjectModal dkfEditorDialog;

    @UiField
    protected GiftWrapModal giftWrapDialog;

    @UiField
    protected RealTimeAssessmentPanel dkfSelectPanel;

    @UiField
    protected DeckPanel aresSelectFilePanel;

    @UiField
    protected DeckPanel aresDeckPanel;

    @UiField
    protected Image removeAresFileButton;

    /**
     * contains the vbsChoicePanel and the vbsChoiceEditorPanel to either choose the vbs
     * input type or edit a selected input types inputs (respectively)
     */
    @UiField
    protected DeckPanel vbsChoiceDeck;

    /** shows a ribbon with the choice of either VBS scenario name or domain session log */
    @UiField
    protected Widget vbsChoicePanel;
    
    /**
     * contains the havenChoicePanel and the havenChoiceEditorPanel to either choose the HAVEN
     * input type or edit a selected input types inputs (respectively)
     */
    @UiField
    protected DeckPanel havenChoiceDeck;
    
    /** shows a ribbon with the choice of domain session log when in Log Playback mode */
    @UiField
    protected Widget havenChoicePanel;
    
    /**
     * contains the rideChoicePanel and the rideChoiceEditorPanel to either choose the RIDE
     * input type or edit a selected input types inputs (respectively)
     */
    @UiField
    protected DeckPanel rideChoiceDeck;
    
    /** shows a ribbon with the choice of domain session log when in Log Playback mode */
    @UiField
    protected Widget rideChoicePanel;
    
    /** a panel containing the ribbon choice for Unity WebGL */
    @UiField
    protected Widget unityChoicePanel;
    
    /** a panel containing the ribbon choice for Mobile Events */
    @UiField
    protected Widget mobileAppChoicePanel;
    
    /** a panel containing the ribbon choice for Demo Application */
    @UiField
    protected Widget demoAppChoicePanel;
    
    /** a panel containing the ribbon choice for LTI Provider */
    @UiField
    protected Widget ltiChoicePanel;
    
    /** button to show the UI that allows changing the vbs input type (VBS scenario or domain session log) */
    @UiField
    protected Button changeVBSChoiceTypeButton;

    /** contains the components to author the vbs inputs types */
    @UiField
    protected Widget vbsChoiceEditorPanel;

    /** contains the vbsScenarioNamePanel and the vbsDomainSessionLogPanel to either author
     *  the vbs scenario name or browse for domain session log */
    @UiField
    protected DeckPanel vbsChoiceEditorDeckPanel;

    /** button shown on the vbsChoicePanel that when selected will allow authoring a vbs scenario name */
    @UiField
    protected Button vbsScenarioButton;
    
    /** button shown on the vbsChoicePanel that when selected will allow authoring of not managing a VBS scenario */
    @UiField
    protected Button vbsNotSpecifiedButton;

    /** The tooltip for the {@link #domainSessionLogFileLabel} */
    @UiField
    protected ManagedTooltip domainSessionLogFileLabelTooltip;
    
    /** shows the name of the domain session message log file used for vbs playback */
    @UiField
    protected Label domainSessionLogFileLabel;

    /** the panel that allows seeing the domain session message log file name and removing it
     * for a vbs course object */
    @UiField
    protected Widget domainSessionLogSelectedPanel;

    /** button used to remove a domain session message log file on the domainSessionLogSelectedPanel */
    @UiField
    protected Button removeDomainSessionLogButton;

    /**
     * The deck panel to show the select log assessment button and the selected
     * log assessment
     */
    @UiField
    protected DeckPanel logAssessmentDeckPanel;

    /** Panel containing the button to select a log assessment */
    @UiField
    protected FocusPanel chooseAssessmentPanel;

    /** The panel to show when a log assessment has been selected */
    @UiField
    protected Widget selectedAssessmentPanel;

    /** The tooltip for the {@link #selectedAssessmentLabel} */
    @UiField
    protected ManagedTooltip selectedAssessmentLabelTooltip;

    /** The label displaying the name of the selected assessment */
    @UiField
    protected Label selectedAssessmentLabel;

    /** The panel containing the scenario selection dropdown */
    @UiField
    protected Button changeSelectedAssessmentBtn;

    /** button shown on the vbsChoicePanel that when selected will allow selecting a domain session log */
    @UiField
    protected Button vbsLogFileButton;

    /** the label that says "Scenario Name"*/
    @UiField
    protected Widget vbsScenarioNamePanel;

    /** the panel that contains the vbsDomainSessionLogdeckPanel for specifying a GIFT domain session log file */
    @UiField
    protected Widget vbsDomainSessionLogPanel;
    
    /** the panel that is used to specify GIFT won't be managing a VBS scenario or log file playback */
    @UiField
    protected Widget vbsNotSpecifiedPanel;

    /** the panel for selecting a domain session log file */
    @UiField
    protected FocusPanel selectDomainSessionLogPanel;
    
    /** a deck containing the panels used to either select a captured audio file or edit an existing one */
    @UiField
    protected DeckPanel capturedAudioDeckPanel;

    /** The tooltip for {@link #capturedAudioFileLabel} */
    @UiField
    protected ManagedTooltip capturedAudioFileLabelTooltip;
    
    /** the label that displays the name of the selected capture audio file when one is selected */
    @UiField
    protected Label capturedAudioFileLabel;

    /** The panel shown when a captured audio file is selected */
    @UiField
    protected Widget capturedAudioSelectedPanel;

    /** The button used to remove the selected captured audio file */
    @UiField
    protected Button removeCapturedAudioButton;
    
    /** The panel containing all of the captured audio authoring elements */
    @UiField
    protected Widget capturedAudioPanel;

    /** the panel for selecting a domain session log file */
    @UiField
    protected Button selectCapturedAudioButton;

    /**
     * contains the selectDomainSessionLogPanel and the domainSessionLogSelectedPanel to either choose the
     * domain session log file or remove it to choose another
     */
    @UiField
    protected DeckPanel vbsDomainSessionLogdeckPanel;
    
    /** button shown on the havenChoicePanel that when selected will allow authoring a HAVEN scenario name */
    @UiField
    protected Button havenNotSpecifiedButton;
    
    /** button shown on the havenChoicePanel that when selected will allow selecting a domain session log */
    @UiField
    protected Button havenLogFileButton;
        
    /** contains the components to author the HAVEN inputs types */
    @UiField
    protected FlowPanel havenChoiceEditorPanel;
    
    /** contains the havenDomainSessionLogPanel to
     *  browse for domain session log */
    @UiField
    protected DeckPanel havenChoiceEditorDeckPanel;
    
    /** the panel that contains the havenDomainSessionLogdeckPanel for specifying a GIFT domain session log file */
    @UiField
    protected FlowPanel havenDomainSessionLogPanel;
    
    /** the panel that is used to specify GIFT won't be managing a HAVEN scenario or log file playback */
    @UiField
    protected Widget havenNotSpecifiedPanel;
    
    /**
     * contains the havenSelectDomainSessionLogPanel and the havenDomainSessionLogSelectedPanel to either choose the
     * domain session log file or remove it to choose another
     */
    @UiField
    protected DeckPanel havenDomainSessionLogdeckPanel;
    
    /** the panel for selecting a domain session log file in HAVEN playback mode */
    @UiField
    protected FocusPanel havenSelectDomainSessionLogPanel;
        
    /** the panel that allows seeing the domain session message log file name and removing it
     * for a HAVEN course object */
    @UiField
    protected FlowPanel havenDomainSessionLogSelectedPanel;
    
    /** The tooltip for the {@link #havenDomainSessionLogFileLabel} */
    @UiField
    protected ManagedTooltip havenDomainSessionLogFileLabelTooltip;
    
    /** shows the name of the domain session message log file used for HAVEN playback */
    @UiField
    protected Label havenDomainSessionLogFileLabel;
    
    /** button used to remove a domain session message log file on the havenDomainSessionLogSelectedPanel */
    @UiField
    protected Button havenRemoveDomainSessionLogButton;
    
    /**
     * The deck panel to show the select log assessment button and the selected
     * log assessment for HAVEN playback
     */
    @UiField
    protected DeckPanel havenLogAssessmentDeckPanel;
    
    /** Panel containing the button to select a log assessment for HAVEN playback */
    @UiField
    protected FocusPanel havenChooseAssessmentPanel;
    
    /** The panel to show when a log assessment has been selected for HAVEN playback */
    @UiField
    protected FlowPanel havenSelectedAssessmentPanel;
    
    /** The tooltip for the {@link #havenSelectedAssessmentLabel} */
    @UiField
    protected ManagedTooltip havenSelectedAssessmentLabelTooltip;
    
    /** The label displaying the name of the selected assessment for HAVEN playback */
    @UiField
    protected Label havenSelectedAssessmentLabel;
    
    /** The panel containing the scenario selection dropdown for HAVEN playback */
    @UiField
    protected Button havenChangeSelectedAssessmentBtn;
    
    /** The panel containing all of the captured audio authoring elements for HAVEN playback */
    @UiField
    protected FlowPanel havenCapturedAudioPanel;
    
    /** a deck containing the panels used to either select a captured audio file or edit an existing one for HAVEN playback */
    @UiField
    protected DeckPanel havenCapturedAudioDeckPanel;
    
    /** the panel for selecting a domain session log file for HAVEN playback */
    @UiField
    protected Button havenSelectCapturedAudioButton;
    
    /** The panel shown when a captured audio file is selected for HAVEN playback */
    @UiField
    protected FlowPanel havenCapturedAudioSelectedPanel;
    
    /** The tooltip for {@link #havenCapturedAudioFileLabel} */
    @UiField
    protected ManagedTooltip havenCapturedAudioFileLabelTooltip;
    
    /** the label that displays the name of the selected capture audio file when one is selected for HAVEN playback */
    @UiField
    protected Label havenCapturedAudioFileLabel;
    
    /** The button used to remove the selected captured audio file for HAVEN playback */
    @UiField
    protected Button havenRemoveCapturedAudioButton;
        
    /** button to show the UI that allows changing the HAVEN input type (domain session log or not specified) */
    @UiField
    protected Button changeHAVENChoiceTypeButton;
    
    /** button shown on the rideChoicePanel that when selected will allow authoring a RIDE scenario name */
    @UiField
    protected Button rideNotSpecifiedButton;
    
    /** button shown on the rideChoicePanel that when selected will allow selecting a domain session log */
    @UiField
    protected Button rideLogFileButton;
        
    /** contains the components to author the RIDE inputs types */
    @UiField
    protected FlowPanel rideChoiceEditorPanel;
    
    /** contains the rideDomainSessionLogPanel to
     *  browse for domain session log */
    @UiField
    protected DeckPanel rideChoiceEditorDeckPanel;
    
    /** the panel that contains the rideDomainSessionLogdeckPanel for specifying a RIDE domain session log file */
    @UiField
    protected FlowPanel rideDomainSessionLogPanel;
    
    /** the panel that is used to specify GIFT won't be managing a RIDE scenario or log file playback */
    @UiField
    protected Widget rideNotSpecifiedPanel;
    
    /**
     * contains the rideSelectDomainSessionLogPanel and the rideDomainSessionLogSelectedPanel to either choose the
     * domain session log file or remove it to choose another
     */
    @UiField
    protected DeckPanel rideDomainSessionLogdeckPanel;
    
    /** the panel for selecting a domain session log file in RIDE playback mode */
    @UiField
    protected FocusPanel rideSelectDomainSessionLogPanel;
        
    /** the panel that allows seeing the domain session message log file name and removing it
     * for a RIDE course object */
    @UiField
    protected FlowPanel rideDomainSessionLogSelectedPanel;
    
    /** The tooltip for the {@link #rideDomainSessionLogFileLabel} */
    @UiField
    protected ManagedTooltip rideDomainSessionLogFileLabelTooltip;
    
    /** shows the name of the domain session message log file used for RIDE playback */
    @UiField
    protected Label rideDomainSessionLogFileLabel;
    
    /** button used to remove a domain session message log file on the rideDomainSessionLogSelectedPanel */
    @UiField
    protected Button rideRemoveDomainSessionLogButton;
    
    /**
     * The deck panel to show the select log assessment button and the selected
     * log assessment for RIDE playback
     */
    @UiField
    protected DeckPanel rideLogAssessmentDeckPanel;
    
    /** Panel containing the button to select a log assessment for RIDE playback */
    @UiField
    protected FocusPanel rideChooseAssessmentPanel;
    
    /** The panel to show when a log assessment has been selected for RIDE playback */
    @UiField
    protected FlowPanel rideSelectedAssessmentPanel;
    
    /** The tooltip for the {@link #rideSelectedAssessmentLabel} */
    @UiField
    protected ManagedTooltip rideSelectedAssessmentLabelTooltip;
    
    /** The label displaying the name of the selected assessment for RIDE playback */
    @UiField
    protected Label rideSelectedAssessmentLabel;
    
    /** The panel containing the scenario selection dropdown for RIDE playback */
    @UiField
    protected Button rideChangeSelectedAssessmentBtn;
    
    /** The panel containing all of the captured audio authoring elements for RIDE playback */
    @UiField
    protected FlowPanel rideCapturedAudioPanel;
    
    /** a deck containing the panels used to either select a captured audio file or edit an existing one for RIDE playback */
    @UiField
    protected DeckPanel rideCapturedAudioDeckPanel;
    
    /** the panel for selecting a domain session log file for RIDE playback */
    @UiField
    protected Button rideSelectCapturedAudioButton;
    
    /** The panel shown when a captured audio file is selected for RIDE playback */
    @UiField
    protected FlowPanel rideCapturedAudioSelectedPanel;
    
    /** The tooltip for {@link #rideCapturedAudioFileLabel} */
    @UiField
    protected ManagedTooltip rideCapturedAudioFileLabelTooltip;
    
    /** the label that displays the name of the selected capture audio file when one is selected for RIDE playback */
    @UiField
    protected Label rideCapturedAudioFileLabel;
    
    /** The button used to remove the selected captured audio file for RIDE playback */
    @UiField
    protected Button rideRemoveCapturedAudioButton;
        
    /** button to show the UI that allows changing the RIDE input type (domain session log or not specified) */
    @UiField
    protected Button changeRIDEChoiceTypeButton;

    @UiField
    protected DeckPanel choiceDeck;

    /** The panel containing the thumbnails that allow the author to pick which training application type to author */
    @UiField
    protected FlowPanel choicePanel;

    @UiField
    protected Widget editorPanel;

    @UiField
    protected Widget realTimeAssessmentPanel;

    @UiField
    protected Button powerPointButton;

    @UiField
    protected Button vbsButton;

    @UiField
    protected Button tc3Button;

    @UiField
    protected Button testbedButton;

    @UiField
    protected Button aresButton;
    
    /** The button used to select VR-Engage as the training application type */
    @UiField
    protected Button vrEngageButton;
    
    /** The button used to select HAVEN as the training application type */
    @UiField
    protected Button havenButton;
    
    /** The button used to select RIDE as the training application type */
    @UiField
    protected Button rideButton;

    /**
     * The button used to select desktop Unity as the training application type
     */
    @UiField
    protected Button unityDesktopButton;

    @UiField
    protected Button unityButton;

    @UiField
    protected Button mobileAppButton;

    @UiField
    protected Button exampleAppButton;

    @UiField
    protected Button ltiProviderButton;

    @UiField
    protected Widget titlePanel;

    @UiField
    protected TextBox titleTextBox;

    @UiField
    protected Label realTimeAssessmentLabel;
    
    private String courseFolderPath;

    /** A dialog used to select domain session message log files */
    private DefaultGatFileSelectionDialog domainSessionLogFileDialog = new DefaultGatFileSelectionDialog();

    /** Boolean to indicate if the user has just selected "Select Existing" dkf from the selection dialog. */
    private boolean isExistingDkfSelected = false;

    private Column<Nvpair, String> argumentNameColumn = new Column<Nvpair, String>(new TextCell()){

        @Override
        public String getValue(Nvpair record){

            if(record == null){
                return "";
            }

            return record.getName();
        }
    };

    private Column<Nvpair, String> argumentValueColumn = new Column<Nvpair, String>(new TextCell()){

        @Override
        public String getValue(Nvpair record){

            if(record == null){
                return "";
            }

            return record.getValue();
        }
    };

    private Column<Nvpair, String> editColumn = new Column<Nvpair, String>(new ButtonCell(){

            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    String value, SafeHtmlBuilder sb) {

                Image image = new Image(value);
                image.setTitle("Edit this argument");

                SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(html);
            }
        }){

        @Override
        public String getValue(Nvpair record) {

            return GatClientBundle.INSTANCE.edit_image().getSafeUri().asString();
        }
    };

    private Column<Nvpair, String> removeColumn = new Column<Nvpair, String>(new ButtonCell(){

            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    String value, SafeHtmlBuilder sb) {

                Image image = new Image(value);
                image.setTitle("Remove this argument");

                SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(html);
            }
        }){

        @Override
        public String getValue(Nvpair record) {

            return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
        }
    };

    /** The add button. */
    @UiField(provided=true)
    protected Image addArgumentButton = new Image(GatClientBundle.INSTANCE.add_image());


    @UiField
    protected Label unityAppLabel;

    @UiField
    protected FocusPanel selectUnityPanel;

    @UiField
    protected Widget unitySelectedPanel;

    @UiField
    protected Button removeUnityButton;

    @UiField(provided=true)
    protected Image addEmbeddedArgumentButton = new Image(GatClientBundle.INSTANCE.add_image());

    @UiField
    protected CellTable<Nvpair> embeddedArgumentTable;

    private Column<Nvpair, String> embeddedArgumentNameColumn = new Column<Nvpair, String>(new TextCell()){

        @Override
        public String getValue(Nvpair record){

            if(record == null){
                return "";
            }

            return record.getName();
        }
    };

    private Column<Nvpair, String> embeddedArgumentValueColumn = new Column<Nvpair, String>(new TextCell()){

        @Override
        public String getValue(Nvpair record){

            if(record == null){
                return "";
            }

            return record.getValue();
        }
    };

    private Column<Nvpair, String> embeddedEditColumn = new Column<Nvpair, String>(new ButtonCell(){

            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    String value, SafeHtmlBuilder sb) {

                Image image = new Image(value);
                image.setTitle("Edit this argument");

                SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(html);
            }
        }){

        @Override
        public String getValue(Nvpair record) {

            return GatClientBundle.INSTANCE.edit_image().getSafeUri().asString();
        }
    };

    private Column<Nvpair, String> embeddedRemoveColumn = new Column<Nvpair, String>(new ButtonCell(){

            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    String value, SafeHtmlBuilder sb) {
            	
            	if(GatClientUtility.isReadOnly()) {
            		return;
            	}
            	
                Image image = new Image(value);
                image.setTitle("Remove this argument");

                SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(html);
            }
        }){

        @Override
        public String getValue(Nvpair record) {

            return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
        }
    };

    private ListDataProvider<Nvpair> embeddedArgumentProvider = new ListDataProvider<>();

    private AddArgumentDialog addEmbeddedArgumentDialog = new AddArgumentDialog();

    private Nvpair embeddedArgumentBeingEdited = null;


    private DefaultGatFileSelectionDialog pptFileSelectionDialog = new DefaultGatFileSelectionDialog();

    private DefaultGatFileSelectionDialog aresFileSelectionDialog = new DefaultGatFileSelectionDialog();

    private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();

    /** The dialog used to select web pages containing Unity WebGL applications */
    private DefaultGatFileSelectionDialog unityFileSelectionDialog = new DefaultGatFileSelectionDialog();
    
    /** The dialog for selecting MP3 audio files */
    private DefaultGatFileSelectionDialog mp3FileDialog = new DefaultGatFileSelectionDialog();
    
    /** The dialog for selecting xTSP files for populating Real-Time Assessments */
    private DefaultGatFileSelectionDialog xTSPFileSelectionDialog = new DefaultGatFileSelectionDialog();

    /** The current training app. */
    private TrainingApplication currentTrainingApp;

    private TrainingApplicationEnum currentTrainingAppType;

    /** The current lesson material. */
    private LessonMaterial currentLessonMaterial;

    /** The locations of the interop classes specified above on the server. This list is populated by a call to the server. */
    private List<String> interopClassLocations = new ArrayList<>();

    private ListDataProvider<Nvpair> argumentProvider = new ListDataProvider<>();

    private AddArgumentDialog addArgumentDialog = new AddArgumentDialog();

    private Nvpair argumentBeingEdited = null;

    private PowerPointFileChangedCallback pptChangedCallback = null;

    private InteropsEditedCallback editedCallback = null;

    /** whether this editor is for practice metadata as opposed to external application course objects */
    private boolean metadataModeEnabled = false;

    /** whether or not the author should be able to view and edit the titles of training applications loaded into this editor*/
    private boolean titleAuthorable = false;

    /** The survey context for the course that the training editor belongs to. */
    private BigInteger courseSurveyContextId = BigInteger.ZERO;

    /** A command to be executed when the type of guidance is changed */
    private Command choiceSelectedCommand;

    /** A modal used to track the progress of unzipping operations */
    private LoadedFileOperationProgressModal<UnzipFileResult> unzipProgressModal = new LoadedFileOperationProgressModal<>(ProgressType.UNZIP);

    /**
     * The dialog for selecting an assessment from a specific domain session log
     */
    private final LogAssessmentPickerDialog assessmentPickerDialog = new LogAssessmentPickerDialog();

    /** An optional command to invoke whenever the training app's referenced DKF file is changed */
    protected Command dkfChangedCommand;

    /** An optional command to invoke whenever the type of training app being authored is changed */
    private Command typeSelectedCommand;

    /** Whether or not the dkf being edited is for remediation activity (e.g. remediation for a course object) or not (e.g. the course object dkf) */
    private boolean isRemediationMode = false;

    /**
     * Creates a new editor instance
     */
    public TrainingAppInteropEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // If the lesson level is set to RTA, prevent selecting training apps that rely on the TUI
        if (GatClientUtility.isRtaLessonLevel()) {
            unityChoicePanel.setVisible(false);
            mobileAppChoicePanel.setVisible(false);
            demoAppChoicePanel.setVisible(false);
            ltiChoicePanel.setVisible(false);
        }

        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
        
        fileSelectionDialog.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.DKF_FILE_EXTENSION});
        
        dkfSelectPanel.getAddAssessmentButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "File selection is disabled in Read-Only mode");
                    return;
                }

                isExistingDkfSelected = false;
                if(!metadataModeEnabled){
                    //don't allow new real time assessments if editing metadata ???
                    NewOrExistingFileDialog.showCreateOrImportFromXTSP(
                            CourseElementUtil.getCourseObjectTypeImgTag(
                                    TrainingApplicationEnum.getTrainingAppTypeIcon(currentTrainingAppType))+" Real-Time Assessment",
                            new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent createEvent) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("New DKF file selected.");
                            }

                            if (currentTrainingApp == null) {
                                currentTrainingApp = new TrainingApplication();
                            }

                            copyDKFTemplate(currentTrainingApp, new AsyncCallback<CopyWorkspaceFilesResult>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    String errorMsg = "Caught exception while copying dkf template file. Reason: '"
                                            + caught.toString() + "'";
                                    logger.severe(errorMsg);
                                    WarningDialog.error("Failed to create DKF file", errorMsg);
                                }

                                @Override
                                public void onSuccess(CopyWorkspaceFilesResult result) {
                                    if (result.isSuccess()) {
                                        showDkfEditorDialog(Boolean.FALSE);
                                    } else {
                                        String errorMsg = "Caught exception while copying dkf template file. Reason: '"
                                                + result.getErrorDetails() + "'";
                                        logger.severe(errorMsg);
                                        WarningDialog.error("Failed to create DKF file", errorMsg);
                                    }
                                }
                            });
                        }

                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent selectEvent) {
                            logger.info("Existing dkf file selected.");

                            isExistingDkfSelected = true;
                            fileSelectionDialog.center();
                        }

                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent selectEvent) {
                            logger.info("New dkf file selected from xTSP file import.");

                            /* We are creating a new DKF constructed from xTSP file. */
                            xTSPFileSelectionDialog.center();

                            //paramMap.put(DkfPlace.PARAM_IMPORTEDDKF, Boolean.TRUE.toString());
                        }

                    });
                }
            }
        });

        selectPPTFilePanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(!metadataModeEnabled && !GatClientUtility.isReadOnly()){
                    pptFileSelectionDialog.center();
                }
            }
        });

        //don't move the file if already in the course folder
        pptFileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);

        if(GatClientUtility.isReadOnly()) {

            //in case no file is selected update that UI
            selectFileLabel.setText("No file selected");
            selectFileLabel.getElement().getStyle().setProperty("cursor", "not-allowed");

            simpleTAScenarioNameBox.setEnabled(false);
            testbedScenarioNameBox.setEnabled(false);
            vbsScenarioNameBox.setEnabled(false);
            vbsLogFileButton.setEnabled(false);
            changeVBSChoiceTypeButton.setVisible(false);
            vbsScenarioButton.setEnabled(false);
            vbsNotSpecifiedButton.setEnabled(false);
            havenLogFileButton.setEnabled(false);
            changeHAVENChoiceTypeButton.setVisible(false);
            havenNotSpecifiedButton.setEnabled(false);
            rideLogFileButton.setEnabled(false);
            changeRIDEChoiceTypeButton.setVisible(false);
            rideNotSpecifiedButton.setEnabled(false);
            changeSelectedAssessmentBtn.setVisible(false);
            removeDomainSessionLogButton.setVisible(false);
            removeCapturedAudioButton.setVisible(false);
            tc3ScenarioNameBox.setEnabled(false);
            sudokuScenarioNameBox.setEnabled(false);
            aresIdTextbox.setEnabled(false);
            aresIdButton.setEnabled(false);
            aresFileButton.setEnabled(false);
            genericContentTextbox.setEnabled(false);

            //in case a file is selected disable the remove component
            removePptButton.setEnabled(false);
            removePptButton.addStyleName("buttonDisabled");
            removeUnityButton.setVisible(false);
            addEmbeddedArgumentButton.setVisible(false);

        }else{

            pptFileSelectionDialog.getFileSelector().setAllowedFileExtensions(Constants.ppt_show_supported_types);
            pptFileSelectionDialog.setText("Select PowerPoint File");
            pptFileSelectionDialog.setIntroMessageHTML("Choose a PowerPoint show file to present to the learner ("+Constants.ppt_show_supported_types+").");
        }

        //don't move the file if already in the course folder for DKF and xTSP editors
        fileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        xTSPFileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);

        xTSPFileSelectionDialog.setIntroMessageHTML("Select a xTSP JSON file.<br>This JSON file will be used to populate a Real-Time Assessment.<br>");

        argumentTable.setEmptyTableWidget(new HTML("<span style='font-size: 12pt; padding: 10px;'>No arguments have been added; therefore, "
                + "no arguments will be loaded when this training application starts.</span>"));

        argumentTable.addColumn(argumentNameColumn, "Key Name");
        argumentTable.setColumnWidth(argumentNameColumn, "50%");
        argumentTable.addColumn(argumentValueColumn, "Value");
        argumentTable.setColumnWidth(argumentValueColumn, "50%");
        argumentTable.addColumn(editColumn);
        argumentTable.addColumn(removeColumn);

        loadInteropImplementationsList();

        fileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {


                if(currentTrainingApp != null){

                    if(currentTrainingApp.getDkfRef() == null){
                        currentTrainingApp.setDkfRef(new DkfRef());
                    }

                    if(event.getValue() != null && !event.getValue().isEmpty()){

                        //don't show this label because a label will be shown in place of the dkf filename on the UI
                        realTimeAssessmentLabel.setVisible(false);

                        currentTrainingApp.getDkfRef().setFile(event.getValue());
                        dkfSelectPanel.setAssessment(event.getValue(), getSetAssessmentCallback());

                        logger.info("Is Existing File Selected = " + isExistingDkfSelected);

                        if (isExistingDkfSelected) {
                            showDkfEditorDialog(Boolean.TRUE);
                        }

                    } else {
                        dkfSelectPanel.removeAssessment();
                        currentTrainingApp.getDkfRef().setFile(null);

                        //show this label because the button label alone doesn't give enough info
                        realTimeAssessmentLabel.setVisible(true);
                    }

                    if(editedCallback != null){
                        editedCallback.onEdit();
                    }
                    
                    onDkfChanged();

                    SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
                }

                isExistingDkfSelected = false;
            }
        });
        
        /* Logic below handles copying the xTSP file to the course folder along with renames. */
        xTSPFileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {


                if(currentTrainingApp != null){

                    if(currentTrainingApp.getDkfRef() == null){
                        currentTrainingApp.setDkfRef(new DkfRef());
                    }

                    if(event.getValue() != null && !event.getValue().isEmpty()){

                        //don't show this label because a label will be shown in place of the dkf filename on the UI
                        realTimeAssessmentLabel.setVisible(false);

                        final String xtspFilePath = event.getValue();
                        
                        logger.info("Is Existing File Selected = " + isExistingDkfSelected);

                        /* We are creating a new DKF constructed from xTSP file. */
                        copyDKFTemplate(currentTrainingApp, new AsyncCallback<CopyWorkspaceFilesResult>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                String errorMsg = "Caught exception while copying dkf template file. Reason: '"
                                        + caught.toString() + "'";
                                logger.severe(errorMsg);
                                WarningDialog.error("Failed to create DKF file", errorMsg);
                            }

                            @Override
                            public void onSuccess(CopyWorkspaceFilesResult result) {
                                if (result.isSuccess()) {
                                    SharedResources.getInstance().getRpcService().importXtspIntoScenarioFile(
                                            GatClientUtility.getUserName(), 
                                            GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + currentTrainingApp.getDkfRef().getFile(), 
                                            GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + xtspFilePath,
                                            currentTrainingAppType,
                                            new AsyncCallback<GenericRpcResponse<XTSPImporterResult>>() {
                                            	
                                            	@Override
												public void onSuccess(GenericRpcResponse<XTSPImporterResult> result) {
													/*
                                                     * An "onSuccess" response can still be received if an
                                                     * exception occurred but was caught. So getWasSuccessful() is used
                                                     * to check whether or not the XTSP import process was completed properly.
                                                     */
                                                    if (result.getWasSuccessful()) {
                                                        showDkfEditorDialog(Boolean.FALSE);
                                                        
                                                        ConceptNode rootConcept = CourseConceptUtility.getRootConcept();
                                                        
                                                        rootConcept.getConceptNode().clear();
                                                        
                                                        for (String conceptToAdd : result.getContent().getCourseConceptNameList()) {
                                                            ConceptNode nodeToAdd = new ConceptNode();
                                                            nodeToAdd.setName(conceptToAdd);
                                                            
                                                            rootConcept.getConceptNode().add(nodeToAdd);
                                                        }
                                                        
                                                        CourseConceptUtility.gatherConceptReferences();
                                                        
                                                        if(!result.getContent().getErrorMessagesList().isEmpty()) {
                                                        	
                                                        	List<DetailedException> errorMessages = result.getContent().getErrorMessagesList();
                                                        	
                                                        	CourseValidationResults validationResults = new CourseValidationResults("Course");
                                                        	
                                                        	validationResults.addWarningIssues(errorMessages);
                                                        	
                                                        	String helpMessage = "Below is a list of issues that were found while translating XTSP content."
                                                        	        + "<br/><br>Warnings shown below do not nercessarily require your action to fix, but they may "
                                                        	        + "have caused some scenario content to be modified. For details, please click on the "
                                                        	        + "warnings";
                                                        	
                                                        	DetailsDialogBox dialog = new ErrorDetailsDialog("Issues Found", validationResults, false, helpMessage);
                                                        	
                                                            dialog.setTitle("xTSP Import Issues");
                                                            dialog.setText("xTSP Import Issues");
                                                            dialog.center();                                                                                                             	
                                                        }
                                                        
                                                    } else {
                                                        String errorReason = result.getException().getReason();
                                                        
                                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                                errorReason, 
                                                                result.getException().getDetails(), 
                                                                result.getException().getErrorStackTrace()
                                                        );
                                                        dialog.setTitle("Error parsing XTSP file and modifying DKF.");
                                                        dialog.setText("Error parsing XTSP file and modifying DKF.");
                                                        dialog.center();
                                                        
                                                        /* 
                                                         * If an error occurs while parsing the XTSP file, remove the DKF
                                                         * in case it has been partially changed, or corrupted somehow
                                                         */
                                                        dkfSelectPanel.removeAssessment();
                                                        currentTrainingApp.getDkfRef().setFile(null);

                                                        //show this label because the button label alone doesn't give enough info
                                                        realTimeAssessmentLabel.setVisible(true);
                                                        
                                                        logger.severe(errorReason);

                                                    }
													
												}
                                                
                                                @Override
                                                public void onFailure(Throwable thrown) {
                                                    String errorMsg = "Caught exception while parsing XTSP file and modifying DKF. Reason: '"
                                                            + thrown.getMessage() + "'";
                                                                                                        
                                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                            "Caught exception while parsing XTSP file and modifying DKF.", 
                                                            thrown.getMessage(), 
                                                            DetailedException.getFullStackTrace(thrown));
                                                    
                                                    dialog.setText("Error parsing XTSP file and modifying DKF.");
                                                    dialog.center();
                                                    
                                                    /* 
                                                     * If an error occurs while parsing the XTSP file, remove the DKF
                                                     * in case it has been partially changed, or corrupted somehow
                                                     */
                                                    dkfSelectPanel.removeAssessment();
                                                    currentTrainingApp.getDkfRef().setFile(null);

                                                    //show this label because the button label alone doesn't give enough info
                                                    realTimeAssessmentLabel.setVisible(true);
                                                    
                                                    logger.severe(errorMsg);                                                    
                                                }
                                            });
                                } else {
                                    String errorMsg = "Caught exception while copying dkf template file. Reason: '"
                                            + result.getErrorDetails() + "'";
                                    logger.severe(errorMsg);
                                    WarningDialog.error("Failed to create DKF file", errorMsg);
                                }
                            }
                        });

                    } else {
                        dkfSelectPanel.removeAssessment();
                        currentTrainingApp.getDkfRef().setFile(null);

                        //show this label because the button label alone doesn't give enough info
                        realTimeAssessmentLabel.setVisible(true);
                    }

                    if(editedCallback != null){
                        editedCallback.onEdit();
                    }
                    
                    onDkfChanged();

                    SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
                }

                isExistingDkfSelected = false;
            }
        });

        //Interop Editor

        simpleTAScenarioNameBox.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                boolean wasFound = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == currentTrainingApp.getInterops()){

                        currentTrainingApp.setInterops(new Interops());

                        selectTrainingApp(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA);

                        simpleTAScenarioNameBox.setValue(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() != null
                                && interop.getInteropInputs().getInteropInput() instanceof SimpleExampleTAInteropInputs){

                            SimpleExampleTAInteropInputs inputs = (SimpleExampleTAInteropInputs) interop.getInteropInputs().getInteropInput();

                            SimpleExampleTAInteropInputs.LoadArgs args = new SimpleExampleTAInteropInputs.LoadArgs();
                            args.setScenarioName(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!wasFound){
                    WarningDialog.error("Failed to set name", "An error occurred while trying to update the scenario name.");
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }

        });

        testbedScenarioNameBox.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                boolean wasFound = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){

                        currentTrainingApp.setInterops(interops = new Interops());

                        selectTrainingApp(TrainingApplicationEnum.DE_TESTBED);

                        testbedScenarioNameBox.setValue(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() != null
                                && interop.getInteropInputs().getInteropInput() instanceof DETestbedInteropInputs){

                            DETestbedInteropInputs inputs = (DETestbedInteropInputs) interop.getInteropInputs().getInteropInput();

                            DETestbedInteropInputs.LoadArgs args = new DETestbedInteropInputs.LoadArgs();
                            args.setScenarioName(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!wasFound){
                    WarningDialog.error("Failed to set name", "An error occurred while trying to update the Testbed scenario name.");
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }

        });

        vbsScenarioNameBox.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                boolean wasFound = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){

                        currentTrainingApp.setInterops(interops = new Interops());

                        selectTrainingApp(TrainingApplicationEnum.VBS);

                        vbsScenarioNameBox.setValue(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() != null
                                && interop.getInteropInputs().getInteropInput() instanceof VBSInteropInputs){

                            VBSInteropInputs inputs = (VBSInteropInputs) interop.getInteropInputs().getInteropInput();

                            VBSInteropInputs.LoadArgs args = new VBSInteropInputs.LoadArgs();
                            args.setScenarioName(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!wasFound){
                    WarningDialog.error("Failed to set name", "An error occurred while trying to update the VBS scenario name.");
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }

        });

        tc3ScenarioNameBox.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                boolean wasFound = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){

                        currentTrainingApp.setInterops(interops = new Interops());

                        selectTrainingApp(TrainingApplicationEnum.TC3);

                        tc3ScenarioNameBox.setValue(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() != null
                                && interop.getInteropInputs().getInteropInput() instanceof TC3InteropInputs){

                            TC3InteropInputs inputs = (TC3InteropInputs) interop.getInteropInputs().getInteropInput();

                            TC3InteropInputs.LoadArgs args = new TC3InteropInputs.LoadArgs();
                            args.setScenarioName(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!wasFound){
                    WarningDialog.error("Failed to set name", "An error occurred while trying to update the TC3 scenario name.");
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }

        });

        sudokuScenarioNameBox.addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                boolean wasFound = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){

                        currentTrainingApp.setInterops(interops = new Interops());

                        selectTrainingApp(TrainingApplicationEnum.SUDOKU);

                         sudokuScenarioNameBox.setValue(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() != null
                                && interop.getInteropInputs().getInteropInput() instanceof SimpleExampleTAInteropInputs){

                            SimpleExampleTAInteropInputs inputs = (SimpleExampleTAInteropInputs) interop.getInteropInputs().getInteropInput();

                            SimpleExampleTAInteropInputs.LoadArgs args = new SimpleExampleTAInteropInputs.LoadArgs();
                            args.setScenarioName(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!wasFound){
                    WarningDialog.error("Failed to set name", "An error occurred while trying to update the Sudoku scenario name.");
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }

        });

        //
        // PowerPoint file selection dialog
        //

        pptFileSelectionDialog.setMessageDisplay(DefaultMessageDisplay.ignoreInfoMessage);

       pptFileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {

                boolean hasPowerPointInterops = false;

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){

                        currentTrainingApp.setInterops(interops = new Interops());

                        selectTrainingApp(TrainingApplicationEnum.POWERPOINT);

                        pptFileLabel.setText(event.getValue());
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof PowerPointInteropInputs){

                            PowerPointInteropInputs inputs = (PowerPointInteropInputs) interop.getInteropInputs().getInteropInput();

                            PowerPointInteropInputs.LoadArgs args = new PowerPointInteropInputs.LoadArgs();
                            args.setShowFile(event.getValue());

                            inputs.setLoadArgs(args);

                            if(!titleAuthorable){

                                if(currentTrainingApp.getTransitionName() == null){
                                    currentTrainingApp.setTransitionName(event.getValue());
                                }
                            }

                            hasPowerPointInterops = true;

                            pptFileLabel.setText(event.getValue());

                            if(pptChangedCallback != null){
                                pptChangedCallback.onFileChanged(event.getValue());
                            }

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }

                            break;
                        }
                    }
                }

                if(!hasPowerPointInterops){
                    WarningDialog.error("Failed to set file", "An error occurred while assigning the PowerPoint show file to the "
                            + "current training application.");
                }else{

                    Notify.notify("", "Successfully set '"+ event.getValue() +"' as the PowerPoint show.", IconType.INFO, NotifyUtil.generateDefaultSettings());

                    setTrainingApplication(currentTrainingApp);
                }

                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }
        });

        titleTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                //update this training application's title whenever the user changes its value
                if(currentTrainingApp != null){
                    currentTrainingApp.setTransitionName(event.getValue());
                }

                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        argumentProvider.addDataDisplay(argumentTable);
        argumentProvider.getList().clear();
        argumentProvider.refresh();

        addArgumentButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                argumentBeingEdited = null;
                addArgumentDialog.setValue(null);
                addArgumentDialog.center();
            }
        });

        editColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {

            @Override
            public void update(int column, Nvpair row, String value) {

                argumentBeingEdited = row;
                addArgumentDialog.setValue(row);
                addArgumentDialog.center();
            }
        });

        removeColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {

            @Override
            public void update(int column, Nvpair row, String value) {

                final Nvpair toRemove = row;

                if(toRemove != null){

                    StringBuilder sb = new StringBuilder();
                    sb.append("Are you sure you want to remove <b>");
                    sb.append(row.getName());
                    sb.append("</b> and it's value, <b>");
                    sb.append(row.getValue());
                    sb.append("</b>?");

                    OkayCancelDialog.show(
                            "Remove Argument?",
                            sb.toString(),
                            "Yes, remove this argument",
                            new OkayCancelCallback() {

                                @Override
                                public void okay() {

                                    boolean wasFound = false;

                                    if(currentTrainingApp != null){

                                        Interops interops = currentTrainingApp.getInterops();

                                        if(interops == null){
                                            return;
                                        }

                                        for(Interop interop : interops.getInterop()){

                                            if(interop.getInteropInputs() != null
                                                    && interop.getInteropInputs().getInteropInput() != null
                                                    && interop.getInteropInputs().getInteropInput() instanceof CustomInteropInputs){

                                                CustomInteropInputs inputs = (CustomInteropInputs) interop.getInteropInputs().getInteropInput();

                                                if(inputs.getLoadArgs() == null){
                                                    return;
                                                }

                                                if(inputs.getLoadArgs().getNvpair().size() <= 1){

                                                    WarningDialog.error("Failed to delete", "This argument cannot be deleted. This list must have at least "
                                                            + "one argument to be valid.");
                                                    return;
                                                }

                                                inputs.getLoadArgs().getNvpair().remove(toRemove);

                                                argumentProvider.getList().remove(toRemove);
                                                argumentProvider.refresh();

                                                wasFound = true;

                                                if(editedCallback != null){
                                                    editedCallback.onEdit();
                                                }

                                                break;
                                            }
                                        }
                                    }

                                    if(!wasFound){
                                        WarningDialog.error("Failed to delete", "An error occurred while trying to delete an argument.");
                                    }
                                }

                                @Override
                                public void cancel() {
                                    //Nothing to do
                                }
                            });
                }
            }
        });

        addArgumentDialog.addValueChangeHandler(new ValueChangeHandler<Nvpair>() {

            @Override
            public void onValueChange(ValueChangeEvent<Nvpair> event) {

                if(argumentBeingEdited == null){

                    Nvpair toAdd = event.getValue();

                    boolean wasFound = false;

                    if(currentTrainingApp != null){

                        Interops interops = currentTrainingApp.getInterops();

                        for(Interop interop : interops.getInterop()){

                            if(interop.getInteropInputs() != null
                                    && interop.getInteropInputs().getInteropInput() != null
                                    && interop.getInteropInputs().getInteropInput() instanceof CustomInteropInputs){

                                CustomInteropInputs inputs = (CustomInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() == null){

                                    inputs.setLoadArgs(new LoadArgs());

                                    argumentProvider.getList().clear();
                                }

                                for(Nvpair pair : inputs.getLoadArgs().getNvpair()){

                                    if(pair != null
                                            && pair.getName() != null
                                            && pair.getName().equals(toAdd.getName())){

                                        WarningDialog.error("Duplicate key", "An argument using the key name <b>" + pair.getName() + "</b> has already "
                                                + "been assigned to this training application.<br/><br/>"
                                                + "Please enter a different key name for this argument.");
                                        return;
                                    }
                                }

                                inputs.getLoadArgs().getNvpair().add(toAdd);

                                argumentProvider.getList().add(toAdd);
                                argumentProvider.refresh();

                                wasFound = true;

                                if(editedCallback != null){
                                    editedCallback.onEdit();
                                }

                                break;
                            }
                        }
                    }

                    if(!wasFound){
                        WarningDialog.error("Failed to add argument", "An error occurred while trying to add an argument.");
                    }

                } else {

                    Nvpair toSave = event.getValue();

                    boolean wasFound = false;

                    if(currentTrainingApp != null){

                        Interops interops = currentTrainingApp.getInterops();

                        for(Interop interop : interops.getInterop()){

                            if(interop.getInteropInputs() != null
                                    && interop.getInteropInputs().getInteropInput() != null
                                    && interop.getInteropInputs().getInteropInput() instanceof CustomInteropInputs){

                                CustomInteropInputs inputs = (CustomInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() == null){

                                    inputs.setLoadArgs(new LoadArgs());

                                    argumentProvider.getList().clear();
                                }

                                for(Nvpair pair : inputs.getLoadArgs().getNvpair()){

                                    if(pair != null
                                            && !pair.equals(argumentBeingEdited)
                                            && pair.getName() != null
                                            && pair.getName().equals(toSave.getName())){

                                        WarningDialog.error("Duplicate key", "An argument using the key name <b>" + pair.getName() + "</b> has already "
                                                + "been assigned to this training application.<br/><br/>"
                                                + "Please enter a different key name for this argument.");
                                        return;
                                    }
                                }

                                int index = inputs.getLoadArgs().getNvpair().indexOf(argumentBeingEdited);

                                if(index >= 0){
                                    inputs.getLoadArgs().getNvpair().set(index, toSave);
                                } else {
                                    inputs.getLoadArgs().getNvpair().add(toSave);
                                }

                                int viewIndex = argumentProvider.getList().indexOf(argumentBeingEdited);

                                if(viewIndex >= 0){
                                    argumentProvider.getList().set(viewIndex, toSave);
                                } else {
                                    argumentProvider.getList().add(toSave);
                                }

                                argumentProvider.refresh();

                                wasFound = true;

                                if(editedCallback != null){
                                    editedCallback.onEdit();
                                }

                                break;
                            }
                        }
                    }

                    if(!wasFound){
                        WarningDialog.error("Failed to add argument", "An error occurred while trying to add an argument.");
                    }
                }

                addArgumentDialog.hide();
            }
        });

        dkfSelectPanel.getEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                isExistingDkfSelected = false;
                showDkfEditorDialog(Boolean.FALSE);
            }

        });

       dkfSelectPanel.getDeleteButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!GatClientUtility.isReadOnly()){
                    DeleteRemoveCancelDialog.show("Delete Real-Time Assessment",
                            "Do you wish to <b>permanently delete</b> this real-time assessment or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
                                    + "Other course objects will be unable to use this real-time assessment if it is deleted, which may cause validation issues if this real-time assessment is being referenced in other parts of the course.",
                                          new DeleteRemoveCancelCallback() {

                        @Override
                        public void delete() {
                            List<String> filesToDelete = new ArrayList<>();

                            final String filename = GatClientUtility.getBaseCourseFolderPath() + "/" + fileSelectionDialog.getValue();
                            filesToDelete.add(filename);

                            String username = GatClientUtility.getUserName();
                            String browserSessionKey = GatClientUtility.getBrowserSessionKey();

                            DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);

                            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                @Override
                                public void onFailure(Throwable arg0) {
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                            "Failed to delete the file.",
                                            arg0.getMessage(),
                                            DetailedException.getFullStackTrace(arg0));
                                    dialog.setDialogTitle("Deletion Failed");
                                    dialog.center();
                                }

                                @Override
                                public void onSuccess(GatServiceResult arg0) {
                                    if(arg0.isSuccess()) {
                                        fileSelectionDialog.setValue("", true);
                                        xTSPFileSelectionDialog.setValue("", true);
                                        saveCourse();
                                        dkfSelectPanel.removeAssessment();

                                        //show this label because the button label alone doesn't give enough info
                                        realTimeAssessmentLabel.setVisible(true);
                                    }

                                    else {
                                        logger.warning("Was unable to delete the file: " + filename + "\nError Message: " + arg0.getErrorMsg());
                                    }

                                }

                            });
                        }

                        @Override
                        public void remove() {
                            fileSelectionDialog.setValue("", true);
                            xTSPFileSelectionDialog.setValue("", true);
                            dkfSelectPanel.removeAssessment();

                            //show this label because the button label alone doesn't give enough info
                            realTimeAssessmentLabel.setVisible(true);
                        }

                        @Override
                        public void cancel() {

                        }

                    });

                }
            }

        });

        aresIdButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                aresDeckPanel.showWidget(aresDeckPanel.getWidgetIndex(aresIdTextbox));

                // set the load args to the value in the textbox
                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){
                        currentTrainingApp.setInterops(interops = new Interops());
                        selectTrainingApp(TrainingApplicationEnum.ARES);
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                            GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                            GenericLoadInteropInputs.LoadArgs args = new GenericLoadInteropInputs.LoadArgs();

                            args.setContentRef(aresIdTextbox.getText());
                            inputs.setLoadArgs(args);

                            break;
                        }
                    }
                }
            }

        });

        aresFileButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                aresDeckPanel.showWidget(aresDeckPanel.getWidgetIndex(aresSelectFilePanel));

                // set the load args to the file label value

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){
                        currentTrainingApp.setInterops(interops = new Interops());
                        selectTrainingApp(TrainingApplicationEnum.ARES);
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                            GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                            GenericLoadInteropInputs.LoadArgs args = new GenericLoadInteropInputs.LoadArgs();

                            args.setContentRef(aresFileLabel.getText());
                            inputs.setLoadArgs(args);

                            break;
                        }
                    }
                }
            }

        });

        aresIdTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){
                        currentTrainingApp.setInterops(interops = new Interops());
                        selectTrainingApp(TrainingApplicationEnum.ARES);
                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                            GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                            GenericLoadInteropInputs.LoadArgs args = new GenericLoadInteropInputs.LoadArgs();

                            args.setContentRef(event.getValue());
                            inputs.setLoadArgs(args);

                            break;
                        }
                    }
                }

            }
        });

        aresFileSelectionDialog.setAllowedFileExtensions(new String[]{".zip"});

        aresFileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if(event.getValue() != null && !event.getValue().isEmpty()) {
                    // show the file name and the remove file label

                    aresSelectFilePanel.showWidget(1);
                    removeAresFileButton.setVisible(true);
                    aresFileLabel.setText(event.getValue());

                } else {
                    // show the browse button
                    aresSelectFilePanel.showWidget(0);
                }


                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops == null){
                        currentTrainingApp.setInterops(interops = new Interops());
                        selectTrainingApp(TrainingApplicationEnum.ARES);

                    }

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                            GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                            GenericLoadInteropInputs.LoadArgs args = new GenericLoadInteropInputs.LoadArgs();

                            args.setContentRef(event.getValue());
                            inputs.setLoadArgs(args);

                            break;
                        }
                    }
                }
            }

        });

        selectAresFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                aresFileSelectionDialog.center();
            }
        });

        aresDeckPanel.showWidget(0);
        aresSelectFilePanel.showWidget(0);
        aresIdButton.setValue(true, true);
        removeAresFileButton.setResource(GatClientBundle.INSTANCE.cancel_image());
        removeAresFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                aresFileSelectionDialog.setValue("", true);
            }

        });

        //handle when the user chooses to add a unity application
        selectUnityPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "File selection is disabled in Read-Only mode");
                    return;
                }

                unityFileSelectionDialog.center();
            }
        });

        //handle when the user chooses to remove a unity application
        removeUnityButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(GatClientUtility.isReadOnly() || metadataModeEnabled) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                // find the unity build output folder in order to delete that (if the user selects to do so)
                // by using the label tooltip value (a hack for sure but it works for now).  The value
                // will not have the course folder in it so there is less risk of deleting the course folder accidently.
                // If Unity changes the build output folder structure with another nested level of folders to
                // get to the index.html file, this logic will need to be updated.
                String unityIndexHtmlFile = unityAppLabel.getTitle();
                final String unityProjectFolder;
                if(StringUtils.isNotBlank(unityIndexHtmlFile)){
                    //get unity project folder
                    int parentFolderIndex = unityIndexHtmlFile.lastIndexOf(Constants.FORWARD_SLASH);
                    if(parentFolderIndex != -1){
                        unityProjectFolder = unityIndexHtmlFile.substring(0, parentFolderIndex);
                    }else{
                        //unable to find the parent folder for some reason
                        unityProjectFolder = unityIndexHtmlFile;
                    }
                }else{
                    //not sure if this is possible, but being safe here
                    unityProjectFolder = unityIndexHtmlFile;
                }

                /* find if the unity index html file is located within the course. If it is, allow
                 * the user to delete content; otherwise only allow the user to remove the
                 * reference. */
                Set<String> filesToCheck = new HashSet<>(Arrays.asList(unityProjectFolder));
                CourseFilesExist fileExistsAction = new CourseFilesExist(courseFolderPath, filesToCheck,
                        GatClientUtility.getUserName());
                SharedResources.getInstance().getDispatchService().execute(fileExistsAction,
                        new AsyncCallback<CourseFilesExistResult>() {
                            @Override
                            public void onFailure(Throwable t) {
                                /* default to not allowing a delete option */
                                showUnityRemoveCancelDialog(unityProjectFolder);
                            }

                            @Override
                            public void onSuccess(CourseFilesExistResult result) {
                                if (result.isSuccess() && result.getFilesExistMap().get(unityProjectFolder)) {
                                    showUnityDeleteRemoveCancelDialog(unityProjectFolder);
                                } else {
                                    /* don't allow a delete option */
                                    showUnityRemoveCancelDialog(unityProjectFolder);
                                }
                            }
                        });
            }
        });

        //restrict untiy file selection dialog to only allow valid Unity WebGL files (i.e. web pages)
        unityFileSelectionDialog.setAllowedFileExtensions(UNITY_APP_ALLOWED_FILE_EXTENSIONS);

        //set up logic to allow the author to upload either plain HTML files or ZIP archives containing them
        final CanHandleUploadedFile originalUploadHandler = unityFileSelectionDialog.getUploadHandler();

        unityFileSelectionDialog.setUploadHandler(new CanHandleUploadedFile() {

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
                    unityFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {

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
        final CopyFileRequest originalCopyRequest = unityFileSelectionDialog.getCopyFileRequest();

        unityFileSelectionDialog.setCopyFileRequest(new CopyFileRequest() {

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
                    unityFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {

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
        unityFileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

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
                        selectUnityAppFile(event.getValue());
                    }

                } else {
                    selectUnityAppFile(event.getValue());
                }
            }
        });

        embeddedArgumentTable.setEmptyTableWidget(new HTML("<span style='font-size: 12pt; padding: 10px;'>"
                + "No arguments will be loaded when this application starts.</span>"));

        embeddedArgumentTable.addColumn(embeddedArgumentNameColumn, "Key Name");
        embeddedArgumentTable.setColumnWidth(embeddedArgumentNameColumn, "50%");
        embeddedArgumentTable.addColumn(embeddedArgumentValueColumn, "Value");
        embeddedArgumentTable.setColumnWidth(embeddedArgumentValueColumn, "50%");
        embeddedArgumentTable.addColumn(embeddedEditColumn);
        embeddedArgumentTable.addColumn(embeddedRemoveColumn);

        embeddedArgumentProvider.addDataDisplay(embeddedArgumentTable);
        embeddedArgumentProvider.getList().clear();
        embeddedArgumentProvider.refresh();

        addEmbeddedArgumentButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(GatClientUtility.isReadOnly()) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                embeddedArgumentBeingEdited = null;
                addEmbeddedArgumentDialog.setValue(null);
                addEmbeddedArgumentDialog.setReadOnly(GatClientUtility.isReadOnly());
                addEmbeddedArgumentDialog.center();
            }
        });

        embeddedEditColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {

            @Override
            public void update(int column, Nvpair row, String value) {

                embeddedArgumentBeingEdited = row;
                addEmbeddedArgumentDialog.setValue(row);
                addEmbeddedArgumentDialog.setReadOnly(GatClientUtility.isReadOnly());
                addEmbeddedArgumentDialog.center();
            }
        });

        embeddedRemoveColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {

            @Override
            public void update(int column, Nvpair row, String value) {

                if(GatClientUtility.isReadOnly()) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                final Nvpair toRemove = row;

                if(toRemove != null){

                    StringBuilder sb = new StringBuilder();
                    sb.append("Are you sure you want to remove <b>");
                    sb.append(row.getName());
                    sb.append("</b> and it's value, <b>");
                    sb.append(row.getValue());
                    sb.append("</b>?");

                    OkayCancelDialog.show(
                            "Remove Argument?",
                            sb.toString(),
                            "Yes, remove this argument",
                            new OkayCancelCallback() {

                                @Override
                                public void okay() {

                                    boolean wasFound = false;

                                    if(currentTrainingApp != null
                                            && currentTrainingApp.getEmbeddedApps() != null
                                            && currentTrainingApp.getEmbeddedApps().getEmbeddedApp() != null){

                                        if(currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs() == null){
                                            return;
                                        }

                                        EmbeddedAppInputs inputs = currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs();

                                        if(inputs.getNvpair().size() <= 1){

                                            //clear the embdedded app inputs if there are no arguments
                                            currentTrainingApp.getEmbeddedApps().getEmbeddedApp().setEmbeddedAppInputs(null);

                                        } else {
                                            inputs.getNvpair().remove(toRemove);
                                        }

                                        embeddedArgumentProvider.getList().remove(toRemove);
                                        embeddedArgumentProvider.refresh();

                                        wasFound = true;

                                        if(editedCallback != null){
                                            editedCallback.onEdit();
                                        }
                                    }

                                    if(!wasFound){
                                        WarningDialog.error("Failed to delete", "An error occurred while trying to delete an argument.");
                                    }
                                }

                                @Override
                                public void cancel() {
                                    //Nothing to do
                                }
                            });
                }
            }
        });

        addEmbeddedArgumentDialog.addValueChangeHandler(new ValueChangeHandler<Nvpair>() {

            @Override
            public void onValueChange(ValueChangeEvent<Nvpair> event) {

                 if(GatClientUtility.isReadOnly()) {
                     // Make sure logic doesn't execute if a user modifies the DOM
                     return;
                 }

                if(embeddedArgumentBeingEdited == null){

                    Nvpair toAdd = event.getValue();

                    boolean wasFound = false;

                    if(currentTrainingApp != null
                            && currentTrainingApp.getEmbeddedApps() != null
                            && currentTrainingApp.getEmbeddedApps().getEmbeddedApp() != null){

                        if(currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs() == null){

                            currentTrainingApp.getEmbeddedApps().getEmbeddedApp().setEmbeddedAppInputs(new EmbeddedAppInputs());

                            embeddedArgumentProvider.getList().clear();
                        }

                        EmbeddedAppInputs inputs = currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs();

                        for(Nvpair pair : inputs.getNvpair()){

                            if(pair != null
                                    && pair.getName() != null
                                    && pair.getName().equals(toAdd.getName())){

                                WarningDialog.error("Duplicate key", "An argument using the key name <b>" + pair.getName() + "</b> has already "
                                        + "been assigned to this training application.<br/><br/>"
                                        + "Please enter a different key name for this argument.");
                                return;
                            }
                        }

                        inputs.getNvpair().add(toAdd);

                        embeddedArgumentProvider.getList().add(toAdd);
                        embeddedArgumentProvider.refresh();

                        wasFound = true;

                        if(editedCallback != null){
                            editedCallback.onEdit();
                        }
                    }

                    if(!wasFound){
                        WarningDialog.error("Failed to add argument", "An error occurred while trying to add an argument.");
                    }

                } else {

                    Nvpair toSave = event.getValue();

                    boolean wasFound = false;

                    if(currentTrainingApp != null){

                        if(currentTrainingApp != null
                                && currentTrainingApp.getEmbeddedApps() != null
                                && currentTrainingApp.getEmbeddedApps().getEmbeddedApp() != null){

                            if(currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs() == null){

                                currentTrainingApp.getEmbeddedApps().getEmbeddedApp().setEmbeddedAppInputs(new EmbeddedAppInputs());

                                embeddedArgumentProvider.getList().clear();
                            }

                            EmbeddedAppInputs inputs = currentTrainingApp.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppInputs();

                            for(Nvpair pair : inputs.getNvpair()){

                                if(pair != null
                                        && !pair.equals(embeddedArgumentBeingEdited)
                                        && pair.getName() != null
                                        && pair.getName().equals(toSave.getName())){

                                    WarningDialog.error("Duplicate key", "An embeddedArgument using the key name <b>" + pair.getName() + "</b> has already "
                                            + "been assigned to this training application.<br/><br/>"
                                            + "Please enter a different key name for this embeddedArgument.");
                                    return;
                                }
                            }

                            int index = inputs.getNvpair().indexOf(embeddedArgumentBeingEdited);

                            if(index >= 0){
                                inputs.getNvpair().set(index, toSave);
                            } else {
                                inputs.getNvpair().add(toSave);
                            }

                            int viewIndex = embeddedArgumentProvider.getList().indexOf(embeddedArgumentBeingEdited);

                            if(viewIndex >= 0){
                                embeddedArgumentProvider.getList().set(viewIndex, toSave);
                            } else {
                                embeddedArgumentProvider.getList().add(toSave);
                            }

                            embeddedArgumentProvider.refresh();

                            wasFound = true;

                            if(editedCallback != null){
                                editedCallback.onEdit();
                            }
                        }
                    }

                    if(!wasFound){
                        WarningDialog.error("Failed to add argument", "An error occurred while trying to add an argument.");
                    }
                }

                addEmbeddedArgumentDialog.hide();
            }
        });

        //handler for removing a guidance file
        removePptButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    if(GatClientUtility.isReadOnly()) {
                        // Make sure logic doesn't execute if a user modifies the DOM
                        return;
                    }

                     DeleteRemoveCancelDialog.show("Delete Content",
                            "Do you wish to <b>permanently delete</b> '"+pptFileLabel.getText()+
                            "' from the course or simply remove the reference to that content in this course object?<br><br>"+
                                    "Other course objects will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                            new DeleteRemoveCancelCallback() {

                                @Override
                                public void cancel() {

                                }

                                @Override
                                public void delete() {

                                  String username = GatClientUtility.getUserName();
                                  String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                  List<String> filesToDelete = new ArrayList<>();
                                  final String filePath = courseFolderPath + "/" + pptFileLabel.getText();
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

                                          try{
                                              Interops interops = currentTrainingApp.getInterops();

                                              PowerPointInteropInputs input = (PowerPointInteropInputs) interops.getInterop().get(0).getInteropInputs().getInteropInput();
                                              input.getLoadArgs().setShowFile(null);

                                          }catch(Exception e){
                                              ErrorDetailsDialog errorDialog = new ErrorDetailsDialog("Failed to remove the PowerPoint show reference of '"+pptFileLabel.getText()+"'.",
                                                      "An exception was thrown that reads:\n"+e.getMessage(),
                                                      DetailedException.getFullStackTrace(e));
                                              errorDialog.setText("Failed to remove reference");
                                              errorDialog.center();
                                          }

                                          if(result.isSuccess()){
                                              logger.warning("Successfully deleted the file '"+filePath+"'.");
                                          } else{
                                              logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                          }

                                          //save the course since the content file was deleted and
                                          //a course undo operation would result in the reference being
                                          //brought back but the file would still not exist
                                          GatClientUtility.saveCourseAndNotify();

                                          setTrainingApplication(currentTrainingApp);
                                      }

                                  });
                                }

                                @Override
                                public void remove() {

                                    Interops interops = currentTrainingApp.getInterops();

                                    try{
                                        PowerPointInteropInputs input = (PowerPointInteropInputs) interops.getInterop().get(0).getInteropInputs().getInteropInput();
                                        input.getLoadArgs().setShowFile(null);

                                    }catch(Exception e){
                                        ErrorDetailsDialog errorDialog = new ErrorDetailsDialog("Failed to remove the PowerPoint show reference of '"+pptFileLabel.getText()+"'.",
                                                "An exception was thrown that reads:\n"+e.getMessage(),
                                                DetailedException.getFullStackTrace(e));
                                        errorDialog.setText("Failed to remove reference");
                                        errorDialog.center();
                                    }

                                    setTrainingApplication(currentTrainingApp);
                                }

                            });
                }

            });

        genericContentTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(currentTrainingApp != null){

                    Interops interops = currentTrainingApp.getInterops();

                    for(Interop interop : interops.getInterop()){

                        if(interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                            GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                            GenericLoadInteropInputs.LoadArgs args = new GenericLoadInteropInputs.LoadArgs();

                            args.setContentRef(event.getValue());
                            inputs.setLoadArgs(args);
                            break;
                        }
                    }
                }

            }
        });

        changeVBSChoiceTypeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                // show the widget that allows choosing the VBS input type (scenario name or log file)
                vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoicePanel));
            }
        });
        
        vbsNotSpecifiedButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for the vbs choice which contains a deck panel for vbs scenario
                // name OR log file OR not specified
                vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));
                
                // show the panel for not specified which contains nothing
                vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsNotSpecifiedPanel));
                
                if(currentTrainingApp != null){
                    // determine if there is a log file value already in the data model and clear
                    // out the value if specified since the user choose to provide no runtime option for GIFT to manage
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs){
                            generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs)interop.getInteropInputs().getInteropInput();
                            disInteropInputs.setLogFile(null);
                        } else if(interop.getInteropInputs().getInteropInput() instanceof generated.course.VBSInteropInputs){
                            // clear out the scenario name element if present
                            generated.course.VBSInteropInputs vbsInteropInputs = (generated.course.VBSInteropInputs)interop.getInteropInputs().getInteropInput();
                            if(vbsInteropInputs.getLoadArgs() != null){
                                vbsInteropInputs.getLoadArgs().setScenarioName(null);
                            }
                        }
                    }

                }

                // show the panel for not specified
                vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsNotSpecifiedPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        vbsScenarioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the vbs choice which contains a deck panel for vbs scenario
                // name OR log file OR not specified
                vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));

                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model and clear
                    // out the value if specified since the user choose to provide a vbs scenario name
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    boolean hasVBSInterops = false;
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs){
                            generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs)interop.getInteropInputs().getInteropInput();
                            disInteropInputs.setLogFile(null);
                        } else if(interop.getInteropInputs().getInteropInput() instanceof generated.course.VBSInteropInputs){
                            hasVBSInterops = true;
                            
                            generated.course.VBSInteropInputs vbsInteropInputs = (generated.course.VBSInteropInputs)interop.getInteropInputs().getInteropInput();
                            if(vbsInteropInputs.getLoadArgs() == null){
                                vbsInteropInputs.setLoadArgs(new VBSInteropInputs.LoadArgs());
                            }
                            
                            if(vbsInteropInputs.getLoadArgs().getScenarioName() == null){
                                vbsInteropInputs.getLoadArgs().setScenarioName(""); // provide a not-null value to differentiate this scenario name 
                                                                                    // option from the not managed option when ScenarioName element is null
                            }
                        }
                    }

                    if (!hasVBSInterops) {
                        Interop interop = new Interop();
                        interop.setInteropImpl(TrainingAppUtil.VBS_PLUGIN_INTERFACE);

                        VBSInteropInputs inputs = new VBSInteropInputs();
                        inputs.setLoadArgs(new VBSInteropInputs.LoadArgs());
                        inputs.getLoadArgs().setScenarioName(""); // provide a not-null value to differentiate this scenario name 
                                                                  // option from the not managed option when ScenarioName element is null

                        InteropInputs interopInputs = new InteropInputs();
                        interopInputs.setInteropInput(inputs);

                        interop.setInteropInputs(interopInputs);
                        interops.getInterop().add(interop);
                    }
                }

                // show the panel that contains the scenario name
                vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsScenarioNamePanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        removeDomainSessionLogButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the domain session message log is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + domainSessionLogFileLabel.getText()
                        + "' log file from the course or simply remove the reference to that log file in this VBS course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + domainSessionLogFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetDomainSessionLogUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetDomainSessionLogUI();
                            }
                        }, "Delete Content");
            }
        });

        vbsLogFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the vbs choice which contains a deck panel for vbs scenario
                // name OR log file
                vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));

                // show the panel for the log file which contains a deck panel for selecting the log file
                // OR removing the log file
                vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsDomainSessionLogPanel));

                boolean hasLogFile = false;
                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model and clear
                    // out the vbs scenario name value (Vbs interop) if specified
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs){
                            generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs) interop
                                    .getInteropInputs().getInteropInput();
                            final LogFile logFile = disInteropInputs.getLogFile();
                            if (logFile == null) {
                                hasLogFile = false;
                                disInteropInputs.setLogFile(new LogFile());
                                disInteropInputs.getLogFile().setDomainSessionLog(new DomainSessionLog());
                            } else if (logFile.getDomainSessionLog() == null) {
                                hasLogFile = false;
                                logFile.setDomainSessionLog(new DomainSessionLog());
                            } else {
                                hasLogFile = StringUtils.isNotBlank(logFile.getDomainSessionLog().getValue());
                            }

                        } else if(interop.getInteropInputs().getInteropInput() instanceof generated.course.VBSInteropInputs){
                            // remove the vbs interop since the user choose to use log file which doesn't need VBS connection
                            interopItr.remove();
                        }
                    }
                }

                /* Show panel for the selected log or to select one */
                vbsDomainSessionLogdeckPanel.showWidget(vbsDomainSessionLogdeckPanel
                        .getWidgetIndex(hasLogFile ? domainSessionLogSelectedPanel : selectDomainSessionLogPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });
        
        changeHAVENChoiceTypeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                // show the widget that allows choosing the HAVEN input type (log file or not specified)
                havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoicePanel));
            }
        });
        
        havenNotSpecifiedButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the HAVEN choice which contains a deck panel for HAVEN log file OR not specified
                havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));
                
                // show the panel for not specified
                havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenNotSpecifiedPanel));

                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model and clear
                    // out the value if specified since the user choose to use the HAVEN not specified option
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs){
                            generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs)interop.getInteropInputs().getInteropInput();
                            havenInteropInputs.setLogFile(null);
                        }
                    }
                }
                
                // show the panel for not specified
                havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenNotSpecifiedPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        havenRemoveDomainSessionLogButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the domain session message log is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + havenDomainSessionLogFileLabel.getText()
                        + "' log file from the course or simply remove the reference to that log file in this SE Sandbox course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + havenDomainSessionLogFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetDomainSessionLogUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetDomainSessionLogUI();
                            }
                        }, "Delete Content");
            }
        });

        havenLogFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the HAVEN choice which contains a deck panel for HAVEN not specified
                // OR log file
                havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));

                // show the panel for the log file which contains a deck panel for selecting the log file
                // OR removing the log file
                havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenDomainSessionLogPanel));

                boolean hasLogFile = false;
                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs){
                            generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs) interop
                                    .getInteropInputs().getInteropInput();
                            final LogFile logFile = havenInteropInputs.getLogFile();
                            if (logFile == null) {
                                hasLogFile = false;
                                havenInteropInputs.setLogFile(new LogFile());
                                havenInteropInputs.getLogFile().setDomainSessionLog(new DomainSessionLog());
                            } else if (logFile.getDomainSessionLog() == null) {
                                hasLogFile = false;
                                logFile.setDomainSessionLog(new DomainSessionLog());
                            } else {
                                hasLogFile = StringUtils.isNotBlank(logFile.getDomainSessionLog().getValue());
                            }
                        }
                    }
                }

                /* Show panel for the selected log or to select one */
                havenDomainSessionLogdeckPanel.showWidget(havenDomainSessionLogdeckPanel
                        .getWidgetIndex(hasLogFile ? havenDomainSessionLogSelectedPanel : havenSelectDomainSessionLogPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });
        
        changeRIDEChoiceTypeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                // show the widget that allows choosing the RIDE input type (log file or not specified)
                rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoicePanel));
            }
        });
        
        rideNotSpecifiedButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the RIDE choice which contains a deck panel for RIDE log file OR not specified
                rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));
                
                // show the panel for not specified
                rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideNotSpecifiedPanel));

                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model and clear
                    // out the value if specified since the user choose to use the RIDE not specified option
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs){
                            generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs)interop.getInteropInputs().getInteropInput();
                            rideInteropInputs.setLogFile(null);
                        }
                    }
                }
                
                // show the panel for not specified
                rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideNotSpecifiedPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        rideRemoveDomainSessionLogButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the domain session message log is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + rideDomainSessionLogFileLabel.getText()
                        + "' log file from the course or simply remove the reference to that log file in this RIDE course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + rideDomainSessionLogFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetDomainSessionLogUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetDomainSessionLogUI();
                            }
                        }, "Delete Content");
            }
        });

        rideLogFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                // show the panel for editing the RIDE choice which contains a deck panel for RIDE not specified
                // OR log file
                rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));

                // show the panel for the log file which contains a deck panel for selecting the log file
                // OR removing the log file
                rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideDomainSessionLogPanel));

                boolean hasLogFile = false;
                if(currentTrainingApp != null){

                    // determine if there is a log file value already in the data model
                    Interops interops = currentTrainingApp.getInterops();
                    Iterator<Interop> interopItr = interops.getInterop().iterator();
                    while(interopItr.hasNext()){

                        Interop interop = interopItr.next();

                        if(interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs){
                            generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs) interop
                                    .getInteropInputs().getInteropInput();
                            final LogFile logFile = rideInteropInputs.getLogFile();
                            if (logFile == null) {
                                hasLogFile = false;
                                rideInteropInputs.setLogFile(new LogFile());
                                rideInteropInputs.getLogFile().setDomainSessionLog(new DomainSessionLog());
                            } else if (logFile.getDomainSessionLog() == null) {
                                hasLogFile = false;
                                logFile.setDomainSessionLog(new DomainSessionLog());
                            } else {
                                hasLogFile = StringUtils.isNotBlank(logFile.getDomainSessionLog().getValue());
                            }
                        }
                    }
                }

                /* Show panel for the selected log or to select one */
                rideDomainSessionLogdeckPanel.showWidget(rideDomainSessionLogdeckPanel
                        .getWidgetIndex(hasLogFile ? rideDomainSessionLogSelectedPanel : rideSelectDomainSessionLogPanel));
                
                if(editedCallback != null){
                    editedCallback.onEdit();
                }
            }
        });

        domainSessionLogFileDialog.setAllowedFileExtensions(new String[]{".log", ".bin"});  // note: using '.protobuf.bin' just changes to '.bin' in the browser's file browser dialog
        domainSessionLogFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        domainSessionLogFileDialog.setIntroMessageHTML(
                " Select a GIFT domain session message log file.<br>GIFT logs can be found in GIFT/output/domainSessions/.<br>");

        domainSessionLogFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                final String filename = event.getValue();
                if (currentTrainingApp == null || StringUtils.isBlank(filename)) {
                    return;
                }

                final LogFile logFile = new LogFile();
                if (logFile.getDomainSessionLog() == null) {
                    logFile.setDomainSessionLog(new DomainSessionLog());
                }

                /* set the data model */
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {

                    if (currentTrainingAppType == TrainingApplicationEnum.VBS) {
                        
                        if (interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs) {
                            DISInteropInputs disInteropInputs = (DISInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            disInteropInputs.setLogFile(logFile);
                        }
                        
                        /* Set the label */
                        domainSessionLogFileLabelTooltip.setTitle(filename);
                        domainSessionLogFileLabel.setText(filename);

                        /* show the domain session log choice panel */
                        vbsChoiceEditorDeckPanel
                                .showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsDomainSessionLogPanel));

                        /* show the edit panel for the domain session message
                         * log file */
                        vbsDomainSessionLogdeckPanel
                                .showWidget(vbsDomainSessionLogdeckPanel.getWidgetIndex(domainSessionLogSelectedPanel));

                        logAssessmentDeckPanel.showWidget(logAssessmentDeckPanel.getWidgetIndex(chooseAssessmentPanel));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.HAVEN) {
                        
                        if (interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs) {
                            HAVENInteropInputs havenInteropInputs = (HAVENInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            havenInteropInputs.setLogFile(logFile);
                        }
                        
                        /* Set the label */
                        havenDomainSessionLogFileLabelTooltip.setTitle(filename);
                        havenDomainSessionLogFileLabel.setText(filename);

                        /* show the domain session log choice panel */
                        havenChoiceEditorDeckPanel
                                .showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenDomainSessionLogPanel));

                        /* show the edit panel for the domain session message
                         * log file */
                        havenDomainSessionLogdeckPanel
                                .showWidget(havenDomainSessionLogdeckPanel.getWidgetIndex(havenDomainSessionLogSelectedPanel));

                        havenLogAssessmentDeckPanel.showWidget(havenLogAssessmentDeckPanel.getWidgetIndex(havenChooseAssessmentPanel));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.RIDE) {
                        
                        if (interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs) {
                            RIDEInteropInputs rideInteropInputs = (RIDEInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            rideInteropInputs.setLogFile(logFile);
                        }
                        
                        /* Set the label */
                        rideDomainSessionLogFileLabelTooltip.setTitle(filename);
                        rideDomainSessionLogFileLabel.setText(filename);

                        /* show the domain session log choice panel */
                        rideChoiceEditorDeckPanel
                                .showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideDomainSessionLogPanel));

                        /* show the edit panel for the domain session message
                         * log file */
                        rideDomainSessionLogdeckPanel
                                .showWidget(rideDomainSessionLogdeckPanel.getWidgetIndex(rideDomainSessionLogSelectedPanel));

                        rideLogAssessmentDeckPanel.showWidget(rideLogAssessmentDeckPanel.getWidgetIndex(rideChooseAssessmentPanel));
                    }

                    parseDomainSessionLog(filename, new AsyncCallback<List<LogMetadata>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            /* Nothing additional to do */
                        }

                        @Override
                        public void onSuccess(List<LogMetadata> result) {
                            if (CollectionUtils.isNotEmpty(result)) {
                                logFile.getDomainSessionLog().setValue(filename);
                                assessmentPickerDialog.center();
                            }
                        }
                    });

                    break;
                }
            }
        });

        selectDomainSessionLogPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                domainSessionLogFileDialog.center();
            }
        });
        
        havenSelectDomainSessionLogPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                domainSessionLogFileDialog.center();
            }
        });
        
        rideSelectDomainSessionLogPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                domainSessionLogFileDialog.center();
            }
        });

        logAssessmentDeckPanel.showWidget(logAssessmentDeckPanel.getWidgetIndex(chooseAssessmentPanel));
        havenLogAssessmentDeckPanel.showWidget(havenLogAssessmentDeckPanel.getWidgetIndex(havenChooseAssessmentPanel));
        rideLogAssessmentDeckPanel.showWidget(rideLogAssessmentDeckPanel.getWidgetIndex(rideChooseAssessmentPanel));
        chooseAssessmentPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the assessment is disabled in Read-Only mode");
                    return;
                }

                /* Show picker dialog (and parse the domain session log if
                 * necessary */
                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.center();
                } else {
                    Interops interops = currentTrainingApp.getInterops();
                    for (Interop interop : interops.getInterop()) {

                        if (interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs) {
                            DISInteropInputs disInteropInputs = (DISInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            final LogFile logFile = disInteropInputs.getLogFile();
                            if (logFile != null && logFile.getDomainSessionLog() != null) {
                                final DomainSessionLog domainSessionLog = logFile.getDomainSessionLog();
                                final String logFilename = domainSessionLog.getValue();
                                if (StringUtils.isBlank(logFilename)) {
                                    break;
                                }

                                parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        /* Nothing additional to do */
                                    }

                                    @Override
                                    public void onSuccess(List<LogMetadata> result) {
                                        if (CollectionUtils.isNotEmpty(result)) {
                                            assessmentPickerDialog.center();
                                        }
                                    }
                                });
                            }

                            break;
                        }
                    }
                }
            }
        });
        
        havenChooseAssessmentPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the assessment is disabled in Read-Only mode");
                    return;
                }

                /* Show picker dialog (and parse the domain session log if
                 * necessary */
                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.center();
                } else {
                    Interops interops = currentTrainingApp.getInterops();
                    for (Interop interop : interops.getInterop()) {

                        if (interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs) {
                            HAVENInteropInputs havenInteropInputs = (HAVENInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            final LogFile logFile = havenInteropInputs.getLogFile();
                            if (logFile != null && logFile.getDomainSessionLog() != null) {
                                final DomainSessionLog domainSessionLog = logFile.getDomainSessionLog();
                                final String logFilename = domainSessionLog.getValue();
                                if (StringUtils.isBlank(logFilename)) {
                                    break;
                                }

                                parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        /* Nothing additional to do */
                                    }

                                    @Override
                                    public void onSuccess(List<LogMetadata> result) {
                                        if (CollectionUtils.isNotEmpty(result)) {
                                            assessmentPickerDialog.center();
                                        }
                                    }
                                });
                            }

                            break;
                        }
                    }
                }
            }
        });
        
        rideChooseAssessmentPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the assessment is disabled in Read-Only mode");
                    return;
                }

                /* Show picker dialog (and parse the domain session log if
                 * necessary */
                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.center();
                } else {
                    Interops interops = currentTrainingApp.getInterops();
                    for (Interop interop : interops.getInterop()) {

                        if (interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs) {
                            RIDEInteropInputs rideInteropInputs = (RIDEInteropInputs) interop.getInteropInputs()
                                    .getInteropInput();
                            final LogFile logFile = rideInteropInputs.getLogFile();
                            if (logFile != null && logFile.getDomainSessionLog() != null) {
                                final DomainSessionLog domainSessionLog = logFile.getDomainSessionLog();
                                final String logFilename = domainSessionLog.getValue();
                                if (StringUtils.isBlank(logFilename)) {
                                    break;
                                }

                                parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        /* Nothing additional to do */
                                    }

                                    @Override
                                    public void onSuccess(List<LogMetadata> result) {
                                        if (CollectionUtils.isNotEmpty(result)) {
                                            assessmentPickerDialog.center();
                                        }
                                    }
                                });
                            }

                            break;
                        }
                    }
                }
            }
        });

        mp3FileDialog.setIntroMessageHTML("Please select an MP3 file to play back alongside this log.");
        mp3FileDialog.setAllowedFileExtensions(new String[]{".mp3"});

        capturedAudioDeckPanel.showWidget(capturedAudioDeckPanel.getWidgetIndex(selectCapturedAudioButton));
        havenCapturedAudioDeckPanel.showWidget(havenCapturedAudioDeckPanel.getWidgetIndex(havenSelectCapturedAudioButton));
        rideCapturedAudioDeckPanel.showWidget(rideCapturedAudioDeckPanel.getWidgetIndex(rideSelectCapturedAudioButton));
        selectCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the captured audio is disabled in Read-Only mode");
                    return;
                }

                /* Prompt the user to select an MP3 file containing the captured
                 * audio */
                mp3FileDialog.center();
            }
        });
        
        havenSelectCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the captured audio is disabled in Read-Only mode");
                    return;
                }

                /* Prompt the user to select an MP3 file containing the captured
                 * audio */
                mp3FileDialog.center();
            }
        });
        
        rideSelectCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Changing the captured audio is disabled in Read-Only mode");
                    return;
                }

                /* Prompt the user to select an MP3 file containing the captured
                 * audio */
                mp3FileDialog.center();
            }
        });

        mp3FileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                String audioFile = event.getValue();

                if (audioFile != null) {
                    if (currentTrainingAppType == TrainingApplicationEnum.VBS) { 
                        capturedAudioFileLabelTooltip.setTitle(audioFile);
                        capturedAudioFileLabel.setText(audioFile);
                        capturedAudioDeckPanel
                                .showWidget(capturedAudioDeckPanel.getWidgetIndex(capturedAudioSelectedPanel));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.HAVEN) {
                        havenCapturedAudioFileLabelTooltip.setTitle(audioFile);
                        havenCapturedAudioFileLabel.setText(audioFile);
                        havenCapturedAudioDeckPanel
                                .showWidget(havenCapturedAudioDeckPanel.getWidgetIndex(havenCapturedAudioSelectedPanel));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.RIDE) {
                        rideCapturedAudioFileLabelTooltip.setTitle(audioFile);
                        rideCapturedAudioFileLabel.setText(audioFile);
                        rideCapturedAudioDeckPanel
                                .showWidget(rideCapturedAudioDeckPanel.getWidgetIndex(rideCapturedAudioSelectedPanel));
                    }
                } else {
                    if (currentTrainingAppType == TrainingApplicationEnum.VBS) { 
                        capturedAudioDeckPanel.showWidget(capturedAudioDeckPanel.getWidgetIndex(selectCapturedAudioButton));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.HAVEN) {
                        havenCapturedAudioDeckPanel.showWidget(havenCapturedAudioDeckPanel.getWidgetIndex(havenSelectCapturedAudioButton));
                    } else if (currentTrainingAppType == TrainingApplicationEnum.RIDE) {
                        rideCapturedAudioDeckPanel.showWidget(rideCapturedAudioDeckPanel.getWidgetIndex(rideSelectCapturedAudioButton));
                    }
                }

                //update the underlying interops to save a reference to the audio file that was selected
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {

                    if (interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs) {
                        generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs) interop
                                .getInteropInputs().getInteropInput();

                        if(disInteropInputs.getLogFile() != null) {
                            disInteropInputs.getLogFile().setCapturedAudioFile(audioFile);
                            break;
                        }
                    } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs) {
                        generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs) interop
                                .getInteropInputs().getInteropInput();
                        
                        if(havenInteropInputs.getLogFile() != null) {
                            havenInteropInputs.getLogFile().setCapturedAudioFile(audioFile);
                            break;
                        }
                    } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs) {
                        generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs) interop
                                .getInteropInputs().getInteropInput();
                        
                        if(rideInteropInputs.getLogFile() != null) {
                            rideInteropInputs.getLogFile().setCapturedAudioFile(audioFile);
                            break;
                        }
                    }
                }
            }
        });
        
        removeCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the captured audio is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + capturedAudioFileLabel.getText()
                        + "' file from the course or simply remove the reference to that file in this VBS course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + capturedAudioFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetCapturedAudioUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetCapturedAudioUI();
                            }
                        }, "Delete Content");
            }
        });
        
        havenRemoveCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the captured audio is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + havenCapturedAudioFileLabel.getText()
                        + "' file from the course or simply remove the reference to that file in this HAVEN course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + havenCapturedAudioFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetCapturedAudioUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetCapturedAudioUI();
                            }
                        }, "Delete Content");
            }
        });
        
        rideRemoveCapturedAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "Removing the captured audio is disabled in Read-Only mode");
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + rideCapturedAudioFileLabel.getText()
                        + "' file from the course or simply remove the reference to that file in this RIDE course object?",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {
                                //nothing to do
                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<>();
                                final String filePath = courseFolderPath + "/" + rideCapturedAudioFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
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

                                                resetCapturedAudioUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetCapturedAudioUI();
                            }
                        }, "Delete Content");
            }
        });

        powerPointButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.POWERPOINT);
            }
        });

        vbsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.VBS);
            }
        });

        tc3Button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.TC3);
            }
        });

        testbedButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.DE_TESTBED);
            }
        });

        aresButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.ARES);
            }
        });
        
        vrEngageButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.VR_ENGAGE);
            }
        });
        
        havenButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.HAVEN);
            }
        });
        
        rideButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.RIDE);
            }
        });

        unityDesktopButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
				onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.UNITY_DESKTOP);
            }
        });

        //handle when the user selects Unity as their training application
        unityButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                final String icon = TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.UNITY_EMBEDDED);
                NewOrExistingFileDialog.showCreateOrImportFromGIFTWrap(CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Add Unity assessment", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent createEvent) {
                        
                        onTypeSeleted();
                        
                        if (currentTrainingApp == null) {
                            currentTrainingApp = new TrainingApplication();
                        }

                        if (currentTrainingApp.getEmbeddedApps() == null || currentTrainingApp.getInterops() != null) {
                            currentTrainingApp.setInterops(null);
                            currentTrainingApp.setEmbeddedApps(new EmbeddedApps());
                        }

                        //copy a dkf to use by default for this new practice assessment
                        copyDKFTemplate(currentTrainingApp, null);
                    }
                }, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent selectEvent) {
                        
                        onTypeSeleted();
                        showGIFTWrap(TrainingApplicationEnum.UNITY_EMBEDDED);
                    }
                });
            }
        });

        //handle when the user selects GIFT Mobile App as their training application
        mobileAppButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                final String icon = TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS);
                NewOrExistingFileDialog.showCreateOrImportFromGIFTWrap(CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Add Android Events assessment", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent createEvent) {
                        
                        onTypeSeleted();
                        
                        if (currentTrainingApp == null) {
                            currentTrainingApp = new TrainingApplication();
                        }

                        if (currentTrainingApp.getEmbeddedApps() == null || currentTrainingApp.getInterops() != null) {
                            currentTrainingApp.setInterops(null);

                            EmbeddedApps apps = new EmbeddedApps();
                            EmbeddedApp app = new EmbeddedApp();
                            app.setEmbeddedAppImpl(new MobileApp());
                            apps.setEmbeddedApp(app);

                            currentTrainingApp.setEmbeddedApps(apps);
                        }

                        //copy a dkf to use by default for this new practice assessment
                        copyDKFTemplate(currentTrainingApp, null);
                    }
                }, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent selectEvent) {
                        
                        onTypeSeleted();
                        showGIFTWrap(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS);
                    }
                });
            }
        });

        exampleAppButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                onTypeSeleted();
                setupApplicationInterops(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA);
            }
        });

        // handle when the user selects LTI provider as their training application
        ltiProviderButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                
                onTypeSeleted();
                
                if (currentLessonMaterial == null) {
                    currentLessonMaterial = new LessonMaterial();
                    currentLessonMaterial.setLessonMaterialList(new LessonMaterialList());

                    Media media = new Media();
                    media.setMediaTypeProperties(new LtiProperties());

                    currentLessonMaterial.getLessonMaterialList().getMedia().add(media);
                    currentLessonMaterial.getLessonMaterialList().setIsCollection(BooleanEnum.FALSE);
                }

                GenericDataProvider<LtiProvider> contentLtiProvidersDataProvider = new GenericDataProvider<>();
                contentLtiProvidersDataProvider.createChild(mediaPanel.getCourseLtiProviderList());

                // populate the LTI provider list
                List<LtiProvider> ltiProviders = GatClientUtility.getCourseLtiProviders();
                if (ltiProviders != null) {
                    contentLtiProvidersDataProvider.getList().addAll(ltiProviders);
                    contentLtiProvidersDataProvider.refresh();
                }

                setLessonMaterial(currentLessonMaterial);
            }
        });

        dkfEditorDialog.setCancelCallback(this);

        assessmentPickerDialog.addValueChangeHandler(new ValueChangeHandler<LogMetadata>() {
            @Override
            public void onValueChange(ValueChangeEvent<LogMetadata> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("assessmentPickerDialog.onValueChange(" + event.getValue() + ")");
                }

                final LogMetadata logMetadata = event.getValue();
                final LogSpan logSpan = logMetadata.getLogSpan();

                /* Set the data model */
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {
                    if (interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs) {
                        generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs) interop
                                .getInteropInputs().getInteropInput();

                        LogFile logFile = disInteropInputs.getLogFile();
                        logFile.getDomainSessionLog().setStart(BigInteger.valueOf(logSpan.getStart()));
                        logFile.getDomainSessionLog().setEnd(BigInteger.valueOf(logSpan.getEnd()));
                        logFile.getDomainSessionLog().setAssessmentName(logMetadata.getSession().getNameOfSession());

                        break;
                    } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs) {
                        generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs) interop
                                .getInteropInputs().getInteropInput();

                        LogFile logFile = havenInteropInputs.getLogFile();
                        logFile.getDomainSessionLog().setStart(BigInteger.valueOf(logSpan.getStart()));
                        logFile.getDomainSessionLog().setEnd(BigInteger.valueOf(logSpan.getEnd()));
                        logFile.getDomainSessionLog().setAssessmentName(logMetadata.getSession().getNameOfSession());

                        break;
                    } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs) {
                        generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs) interop
                                .getInteropInputs().getInteropInput();

                        LogFile logFile = rideInteropInputs.getLogFile();
                        logFile.getDomainSessionLog().setStart(BigInteger.valueOf(logSpan.getStart()));
                        logFile.getDomainSessionLog().setEnd(BigInteger.valueOf(logSpan.getEnd()));
                        logFile.getDomainSessionLog().setAssessmentName(logMetadata.getSession().getNameOfSession());

                        break;
                    }
                }

                if (currentTrainingAppType == TrainingApplicationEnum.VBS) {
                    selectedAssessmentLabelTooltip.setTitle(logMetadata.getSession().getNameOfSession());
                    selectedAssessmentLabel.setText(logMetadata.getSession().getNameOfSession());
                    
                    logAssessmentDeckPanel.showWidget(logAssessmentDeckPanel.getWidgetIndex(selectedAssessmentPanel));
                } else if (currentTrainingAppType == TrainingApplicationEnum.HAVEN) {
                    havenSelectedAssessmentLabelTooltip.setTitle(logMetadata.getSession().getNameOfSession());
                    havenSelectedAssessmentLabel.setText(logMetadata.getSession().getNameOfSession());
                    
                    havenLogAssessmentDeckPanel.showWidget(havenLogAssessmentDeckPanel.getWidgetIndex(havenSelectedAssessmentPanel));
                } else if (currentTrainingAppType == TrainingApplicationEnum.RIDE) {
                    rideSelectedAssessmentLabelTooltip.setTitle(logMetadata.getSession().getNameOfSession());
                    rideSelectedAssessmentLabel.setText(logMetadata.getSession().getNameOfSession());
                    
                    rideLogAssessmentDeckPanel.showWidget(rideLogAssessmentDeckPanel.getWidgetIndex(rideSelectedAssessmentPanel));
                }
            }
        });

        changeSelectedAssessmentBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    /* Make sure logic doesn't execute if a user modifies the
                     * DOM */
                    return;
                }

                DomainSessionLog domainSessionLog = null;
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {
                    if (!(interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs)) {
                        continue;
                    }

                    DISInteropInputs disInteropInputs = (DISInteropInputs) interop.getInteropInputs()
                            .getInteropInput();
                    final LogFile logFile = disInteropInputs.getLogFile();
                    if (logFile != null && logFile.getDomainSessionLog() != null) {
                        domainSessionLog = logFile.getDomainSessionLog();
                        
                    }
                }

                final String logFilename = domainSessionLog != null ? domainSessionLog.getValue() : null;
                final String assessmentName = domainSessionLog != null ? domainSessionLog.getAssessmentName() : null;

                if (StringUtils.isBlank(logFilename)) {
                    changeSelectedAssessmentBtn.setVisible(false);
                    return;
                }

                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.setValue(assessmentName);
                    assessmentPickerDialog.center();
                } else {
                    parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            /* Nothing additional to do because parseDomainSessionLog showed a warning dialog  */
                        }

                        @Override
                        public void onSuccess(List<LogMetadata> result) {
                            if (CollectionUtils.isNotEmpty(result)) {
                                if (result.size() == 1) {
                                    changeSelectedAssessmentBtn.setVisible(false);
                                    WarningDialog.info("1 Scenario Found", "Since there is only 1 scenario found in the log file, the change button will be hidden.");
                                }

                                assessmentPickerDialog.setValue(assessmentName);
                                assessmentPickerDialog.center();
                            }
                        }
                    });
                }
            }
        });
        
        havenChangeSelectedAssessmentBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    /* Make sure logic doesn't execute if a user modifies the
                     * DOM */
                    return;
                }

                DomainSessionLog domainSessionLog = null;
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {
                    if (!(interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs)) {
                        continue;
                    }

                    HAVENInteropInputs havenInteropInputs = (HAVENInteropInputs) interop.getInteropInputs()
                            .getInteropInput();
                    final LogFile logFile = havenInteropInputs.getLogFile();
                    if (logFile != null && logFile.getDomainSessionLog() != null) {
                        domainSessionLog = logFile.getDomainSessionLog();
                        
                    }
                }

                final String logFilename = domainSessionLog != null ? domainSessionLog.getValue() : null;
                final String assessmentName = domainSessionLog != null ? domainSessionLog.getAssessmentName() : null;

                if (StringUtils.isBlank(logFilename)) {
                    havenChangeSelectedAssessmentBtn.setVisible(false);
                    return;
                }

                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.setValue(assessmentName);
                    assessmentPickerDialog.center();
                } else {
                    parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            /* Nothing additional to do because parseDomainSessionLog showed a warning dialog  */
                        }

                        @Override
                        public void onSuccess(List<LogMetadata> result) {
                            if (CollectionUtils.isNotEmpty(result)) {
                                if (result.size() == 1) {
                                    havenChangeSelectedAssessmentBtn.setVisible(false);
                                    WarningDialog.info("1 Scenario Found", "Since there is only 1 scenario found in the log file, the change button will be hidden.");
                                }

                                assessmentPickerDialog.setValue(assessmentName);
                                assessmentPickerDialog.center();
                            }
                        }
                    });
                }
            }
        });
        
        rideChangeSelectedAssessmentBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    /* Make sure logic doesn't execute if a user modifies the
                     * DOM */
                    return;
                }

                DomainSessionLog domainSessionLog = null;
                Interops interops = currentTrainingApp.getInterops();
                for (Interop interop : interops.getInterop()) {
                    if (!(interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs)) {
                        continue;
                    }

                    RIDEInteropInputs rideInteropInputs = (RIDEInteropInputs) interop.getInteropInputs()
                            .getInteropInput();
                    final LogFile logFile = rideInteropInputs.getLogFile();
                    if (logFile != null && logFile.getDomainSessionLog() != null) {
                        domainSessionLog = logFile.getDomainSessionLog();
                        
                    }
                }

                final String logFilename = domainSessionLog != null ? domainSessionLog.getValue() : null;
                final String assessmentName = domainSessionLog != null ? domainSessionLog.getAssessmentName() : null;

                if (StringUtils.isBlank(logFilename)) {
                    rideChangeSelectedAssessmentBtn.setVisible(false);
                    return;
                }

                if (assessmentPickerDialog.isPopulated()) {
                    assessmentPickerDialog.setValue(assessmentName);
                    assessmentPickerDialog.center();
                } else {
                    parseDomainSessionLog(logFilename, new AsyncCallback<List<LogMetadata>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            /* Nothing additional to do because parseDomainSessionLog showed a warning dialog  */
                        }

                        @Override
                        public void onSuccess(List<LogMetadata> result) {
                            if (CollectionUtils.isNotEmpty(result)) {
                                if (result.size() == 1) {
                                    rideChangeSelectedAssessmentBtn.setVisible(false);
                                    WarningDialog.info("1 Scenario Found", "Since there is only 1 scenario found in the log file, the change button will be hidden.");
                                }

                                assessmentPickerDialog.setValue(assessmentName);
                                assessmentPickerDialog.center();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Reset the UI for the domain session log picker button back to a 'none
     * selected' state.
     */
    private void resetDomainSessionLogUI() {
        if (currentTrainingApp != null) {

            // clear data model
            Interops interops = currentTrainingApp.getInterops();
            for (Interop interop : interops.getInterop()) {

                if (interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs) {
                    generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    disInteropInputs.setLogFile(null);
                    break;
                } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs) {
                    generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    havenInteropInputs.setLogFile(null);
                    break;
                } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs) {
                    generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    rideInteropInputs.setLogFile(null);
                    break;
                }
            }
            
            TrainingApplicationEnum appType = TrainingApplicationEnum.valueOf(currentTrainingApp.getTrainingAppTypeEnum());

            if (appType == TrainingApplicationEnum.VBS) {
                // clear labels
                domainSessionLogFileLabelTooltip.setTitle(Constants.EMPTY);
                domainSessionLogFileLabel.setText(Constants.EMPTY);
                capturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                capturedAudioFileLabel.setText(Constants.EMPTY);
    
                // show domain session message log file selection components again
                vbsDomainSessionLogdeckPanel
                        .showWidget(vbsDomainSessionLogdeckPanel.getWidgetIndex(selectDomainSessionLogPanel));
    
                // hide any captured audio associated with the log file selection
                changeSelectedAssessmentBtn.setVisible(false);
    
                // show captured audio file selection components again
                capturedAudioPanel.setVisible(false);      
            } else if (appType == TrainingApplicationEnum.HAVEN) {
                // clear labels
                havenDomainSessionLogFileLabelTooltip.setTitle(Constants.EMPTY);
                havenDomainSessionLogFileLabel.setText(Constants.EMPTY);
                havenCapturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                havenCapturedAudioFileLabel.setText(Constants.EMPTY);
    
                // show domain session message log file selection components again
                havenDomainSessionLogdeckPanel
                        .showWidget(havenDomainSessionLogdeckPanel.getWidgetIndex(havenSelectDomainSessionLogPanel));
    
                // hide any captured audio associated with the log file selection
                havenChangeSelectedAssessmentBtn.setVisible(false);
    
                // show captured audio file selection components again
                havenCapturedAudioPanel.setVisible(false);      
            } else if (appType == TrainingApplicationEnum.RIDE) {
                // clear labels
                rideDomainSessionLogFileLabelTooltip.setTitle(Constants.EMPTY);
                rideDomainSessionLogFileLabel.setText(Constants.EMPTY);
                rideCapturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                rideCapturedAudioFileLabel.setText(Constants.EMPTY);
    
                // show domain session message log file selection components again
                rideDomainSessionLogdeckPanel
                        .showWidget(rideDomainSessionLogdeckPanel.getWidgetIndex(rideSelectDomainSessionLogPanel));
    
                // hide any captured audio associated with the log file selection
                rideChangeSelectedAssessmentBtn.setVisible(false);
    
                // show captured audio file selection components again
                rideCapturedAudioPanel.setVisible(false);      
            }
            
            resetCapturedAudioUI();
        }
    }
    
    /**
     * Reset the UI for the captured audio button back to a 'none selected' state.
     */
    private void resetCapturedAudioUI() {
        if (currentTrainingApp != null) {

            // clear data model
            Interops interops = currentTrainingApp.getInterops();
            for (Interop interop : interops.getInterop()) {

                if (interop.getInteropInputs().getInteropInput() instanceof generated.course.DISInteropInputs) {
                    generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    if(disInteropInputs.getLogFile() != null) {
                        disInteropInputs.getLogFile().setCapturedAudioFile(null);
                    }
                    break;
                } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs) {
                    generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    if(havenInteropInputs.getLogFile() != null) {
                        havenInteropInputs.getLogFile().setCapturedAudioFile(null);
                    }
                    break;
                } else if (interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs) {
                    generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs) interop
                            .getInteropInputs().getInteropInput();
                    if(rideInteropInputs.getLogFile() != null) {
                        rideInteropInputs.getLogFile().setCapturedAudioFile(null);
                    }
                    break;
                }
            }
            TrainingApplicationEnum appType = TrainingApplicationEnum.valueOf(currentTrainingApp.getTrainingAppTypeEnum());
            if (appType == TrainingApplicationEnum.VBS) { 
                // clear label
                capturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                capturedAudioFileLabel.setText(Constants.EMPTY);
                
                // show captured audio file selection components again
                capturedAudioDeckPanel.showWidget(capturedAudioDeckPanel.getWidgetIndex(selectCapturedAudioButton));
            } else if (appType == TrainingApplicationEnum.HAVEN) {
                // clear label
                havenCapturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                havenCapturedAudioFileLabel.setText(Constants.EMPTY);
                
                // show captured audio file selection components again
                havenCapturedAudioDeckPanel.showWidget(havenCapturedAudioDeckPanel.getWidgetIndex(havenSelectCapturedAudioButton));
            } else if (appType == TrainingApplicationEnum.RIDE) {
                // clear label
                rideCapturedAudioFileLabelTooltip.setTitle(Constants.EMPTY);
                rideCapturedAudioFileLabel.setText(Constants.EMPTY);
                
                // show captured audio file selection components again
                rideCapturedAudioDeckPanel.showWidget(rideCapturedAudioDeckPanel.getWidgetIndex(rideSelectCapturedAudioButton));
            }
        }
    }

    /**
     * Provide the user the option to either remove the Unity reference from the course object or cancel without action.
     *
     * @param unityProjectFolder the folder path of the referenced unity project
     */
    private void showUnityRemoveCancelDialog(final String unityProjectFolder) {
        OkayCancelDialog.show("Remove Reference", "Do you wish to remove the reference to the content located at '" + unityProjectFolder
                + "' from this course object?<br/><br/>If other parts of the course are also using this reference, they will continue to behave normally.",
                "Remove Reference", new OkayCancelCallback() {
                    @Override
                    public void okay() {
                        selectUnityAppFile(null);
                        if (currentTrainingApp != null) {
                            currentTrainingApp.setAuthoringSupportElements(null);
                        }
                    }

                    @Override
                    public void cancel() {
                        // nothing to do
                    }
                });
    }

    /**
     * Provide the user the option to either delete the Unity reference from the system, remove the
     * Unity reference from the course object, or cancel without action.
     *
     * @param unityProjectFolder the folder path of the referenced unity project
     */
    private void showUnityDeleteRemoveCancelDialog(final String unityProjectFolder) {
        DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                + unityProjectFolder
                + "' from the course or simply remove the reference to that content in this course object?<br><br>"
                + "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                new DeleteRemoveCancelCallback() {

                    @Override
                    public void cancel() {
                        // nothing to do
                    }

                    @Override
                    public void delete() {

                        String username = GatClientUtility.getUserName();
                        String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                        List<String> filesToDelete = new ArrayList<>();
                        final String filePath = courseFolderPath + "/" + unityProjectFolder;
                        filesToDelete.add(filePath);

                        DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                filesToDelete, true);
                        SharedResources.getInstance().getDispatchService().execute(action,
                                new AsyncCallback<GatServiceResult>() {

                                    @Override
                                    public void onFailure(Throwable error) {
                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to delete the file.",
                                                error.getMessage(), DetailedException.getFullStackTrace(error));
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
                        selectUnityAppFile(null);
                        if (currentTrainingApp != null) {
                            currentTrainingApp.setAuthoringSupportElements(null);
                        }
                    }

                }, "Delete Content");
    }

    /**
     * Show the GIFT Wrap landing page dialog.
     *
     * @param applicationType the training application type with which to filter the viewable list on the landing page.
     */
    private void showGIFTWrap(TrainingApplicationEnum applicationType) {
        importedTrainingApp = null;

        giftWrapDialog.addHideHandler(new ModalHideHandler() {
            @Override
            public void onHide(ModalHideEvent evt) {
                if (importedTrainingApp != null) {
                    currentTrainingApp = importedTrainingApp;
                    setTrainingApplication(currentTrainingApp);
                }
            }

        });

        giftWrapDialog.setCourseObjectUrl("GIFT Wrap", GatClientUtility.createGIFTWrapModalUrl(applicationType));
        giftWrapDialog.show();
    }

    /**
     * Handles when a training application is imported from GIFT Wrap.
     *
     * @param trainingApplication the training application that was imported.
     */
    public void handleTrainingApplicationImport(TrainingApplication trainingApplication) {
        // this modal isn't visible so this isn't the target of the import
        if (!giftWrapDialog.isShowing()) {
            return;
        }

        importedTrainingApp = trainingApplication;
        giftWrapDialog.hide();
    }

    /**
     * Shows the dkf editor dialog.
     *
     * @param isImportedDkf - True if the user has just selected an existing dkf file to import into the course.  False if the user is editing
     *                        an already existing dkf file or a new dkf file.
     */
    public void showDkfEditorDialog(Boolean isImportedDkf) {
        String filename = currentTrainingApp.getDkfRef().getFile();
        // Pass the course survey context id into the url via a parameter.
        HashMap<String, String> paramMap = new HashMap<>();
        
        BigInteger surveyContextId = BigInteger.ZERO;
        
        if(courseSurveyContextId != null && !BigInteger.ZERO.equals(courseSurveyContextId)) {
            surveyContextId = courseSurveyContextId;
            
        } else {
            
            //use the base survey context ID from the course editor if no other ID is provided
            BigInteger baseContextId = GatClientUtility.getBaseCourseSurveyContextId();
            
            if(baseContextId != null) {
                surveyContextId = baseContextId;
            }
        }
        
        paramMap.put(DkfPlace.PARAM_SURVEYCONTEXTID, surveyContextId.toString());
        paramMap.put(DkfPlace.PARAM_IMPORTEDDKF, isImportedDkf.toString());
        paramMap.put(DkfPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
        paramMap.put(DkfPlace.PARAM_TRAINING_APP, currentTrainingAppType.toString());
        paramMap.put(DkfPlace.PARAM_REMEDIATION, Boolean.toString(isRemediationMode));

        /* Determine if there is a playback log file */
        boolean isPlayback = false;
        if (currentTrainingApp.getInterops() != null
                && CollectionUtils.isNotEmpty(currentTrainingApp.getInterops().getInterop())) {
            for (Interop interop : currentTrainingApp.getInterops().getInterop()) {
                if (interop.getInteropInputs() != null
                        && interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs
                        && ((DISInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    isPlayback = true;
                    break;
                } else if (interop.getInteropInputs() != null
                        && interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs
                        && ((HAVENInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    isPlayback = true;
                    break;
                } else if (interop.getInteropInputs() != null
                        && interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs
                        && ((RIDEInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    isPlayback = true;
                    break;
                }
            }
        }
        paramMap.put(DkfPlace.PARAM_PLAYBACK, Boolean.toString(isPlayback));

        final String url = GatClientUtility.getModalDialogUrlWithParams(courseFolderPath, filename, paramMap);
        dkfEditorDialog.setCourseObjectUrl(
                CourseElementUtil.getCourseObjectTypeImgTag(
                        TrainingApplicationEnum.getTrainingAppTypeIcon(currentTrainingAppType)) +" "+CourseObjectName.DKF.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
        dkfEditorDialog.show();
        setAuthoringSupportElements(currentTrainingApp.getAuthoringSupportElements());
        dkfEditorDialog.setSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                GatClientUtility.saveEmbeddedCourseObject();
                dkfEditorDialog.stopEditor();
                CourseObjectModal.resetEmbeddedSaveObject();
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this dkf being edited
                }
            }

        });
    }

    /**
     * Saves the supplied {@link AuthoringSupportElements} to the
     * $wnd.authoringSupportElements variable. This allows the DKF editor to
     * access the elemnets via JSNI.
     *
     * @param authoringSupportElements The {@link AuthoringSupportElements} that
     *        the DKF editor should use.
     */
    private native void setAuthoringSupportElements(AuthoringSupportElements authoringSupportElements) /*-{
		$wnd.authoringSupportElements = authoringSupportElements;
    }-*/;

    /**
     * Shows the sub-editor corresponding to the given training application
     *
     * @param mode the application whose editor should be shown
     */
    public void setTrainingAppEditingMode(TrainingApplicationEnum mode){

        currentTrainingAppType = mode;

        if(mode == TrainingApplicationEnum.SIMPLE_EXAMPLE_TA){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(simpleTaPanel));

        } else if(mode == TrainingApplicationEnum.DE_TESTBED){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(testbedPanel));

        } else if(mode == TrainingApplicationEnum.VBS){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(vbsPanel));

        } else if(mode == TrainingApplicationEnum.TC3){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(tc3Panel));

        }  else if(mode == TrainingApplicationEnum.SUDOKU){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(sudokuPanel));

        }  else if(mode == TrainingApplicationEnum.POWERPOINT){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(powerPointPanel));

        } else if(mode == TrainingApplicationEnum.ARES){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(aresPanel));
            
        } else if(mode == TrainingApplicationEnum.VR_ENGAGE){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(vrEngagePanel));

        } else if(mode == TrainingApplicationEnum.UNITY_DESKTOP){
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(unityDesktopPanel));

        } else if(mode == TrainingApplicationEnum.HAVEN) {
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(havenPanel));
          
        } else if(mode == TrainingApplicationEnum.RIDE) {
            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(ridePanel));
                
        } else {

            Interops interops = currentTrainingApp != null ? currentTrainingApp.getInterops() : null;

            if(interops != null) {
                // If the generic interop input is being used, show the generic interop input ui

                for(Interop interop : interops.getInterop()){

                    if(interop.getInteropInputs() != null
                            && interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs){

                        applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(genericInputPanel));
                        return;
                    }
                }
            }

            applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(customPanel));
        }

        choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));

        //notify UI of event in order to enable/disable the 'edit with gift wrap' button on the external application editor
        SharedResources.getInstance().getEventBus().fireEvent(new TrainingAppTypeSelectedEvent(mode));
    }

    /**
     * Selects the specified training application by populating its interops
     *
     * @param value the training application to select
     */
    protected void selectTrainingApp(TrainingApplicationEnum value) {

        if(TrainingAppUtil.trainingAppToInteropClassNames.get(value) != null){

            List<String> interopImpls = TrainingAppUtil.trainingAppToInteropClassNames.get(value);

            Interops interops = currentTrainingApp != null ? currentTrainingApp.getInterops() : null;

            if(interops == null){
                currentTrainingApp.setInterops(interops = new Interops());
            }

            interops.getInterop().clear();

            for(String implClassName : interopImpls){

                Interop interop = new Interop();

                String implClassLocation = getInteropClassLocation(implClassName);

                if(implClassLocation != null){

                    interop.setInteropImpl(implClassLocation);

                    Class<?> interopImplInputClass = TrainingAppUtil.interopClassNameToInputClass.get(implClassName);

                    boolean foundInterop = true;

                    if(interopImplInputClass != null){

                        if(interopImplInputClass.equals(SimpleExampleTAInteropInputs.class)){

                            SimpleExampleTAInteropInputs.LoadArgs loadArgs = new SimpleExampleTAInteropInputs.LoadArgs();

                            if(TrainingApplicationEnum.SUDOKU.equals(value)){
                                loadArgs.setScenarioName(sudokuScenarioNameBox.getValue());

                            } else {
                                loadArgs.setScenarioName(simpleTAScenarioNameBox.getValue());
                            }

                            SimpleExampleTAInteropInputs inputs = new SimpleExampleTAInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(DETestbedInteropInputs.class)){

                            DETestbedInteropInputs.LoadArgs loadArgs = new DETestbedInteropInputs.LoadArgs();
                            loadArgs.setScenarioName(testbedScenarioNameBox.getValue());

                            DETestbedInteropInputs inputs = new DETestbedInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(VBSInteropInputs.class)){

                            VBSInteropInputs.LoadArgs loadArgs = new VBSInteropInputs.LoadArgs();
                            loadArgs.setScenarioName(vbsScenarioNameBox.getValue());

                            VBSInteropInputs inputs = new VBSInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(TC3InteropInputs.class)){

                            TC3InteropInputs.LoadArgs loadArgs = new TC3InteropInputs.LoadArgs();
                            loadArgs.setScenarioName(tc3ScenarioNameBox.getValue());

                            TC3InteropInputs inputs = new TC3InteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(PowerPointInteropInputs.class)){

                            PowerPointInteropInputs.LoadArgs loadArgs = new PowerPointInteropInputs.LoadArgs();

                            if(!NO_FILE_SELECTED.equals(pptFileLabel.getText())){
                                loadArgs.setShowFile(pptFileLabel.getText());
                            }

                            PowerPointInteropInputs inputs = new PowerPointInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(DISInteropInputs.class)){

                            DISInteropInputs.LoadArgs loadArgs = new DISInteropInputs.LoadArgs();

                            DISInteropInputs inputs = new DISInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);
                            
                        } else if (interopImplInputClass.equals(UnityInteropInputs.class)) {
                            UnityInteropInputs.LoadArgs loadArgs = new UnityInteropInputs.LoadArgs();

                            UnityInteropInputs inputs = new UnityInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if (interopImplInputClass.equals(VREngageInteropInputs.class)) {

                            VREngageInteropInputs inputs = new VREngageInteropInputs();
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);
                        } else if (interopImplInputClass.equals(HAVENInteropInputs.class)) {
                            
                            HAVENInteropInputs inputs = new HAVENInteropInputs();
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        } else if (interopImplInputClass.equals(RIDEInteropInputs.class)) {
                            
                            RIDEInteropInputs inputs = new RIDEInteropInputs();
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        } else if(interopImplInputClass.equals(GenericLoadInteropInputs.class)){

                            GenericLoadInteropInputs.LoadArgs loadArgs = new GenericLoadInteropInputs.LoadArgs();

                            GenericLoadInteropInputs inputs = new GenericLoadInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else {
                            foundInterop = true;
                        }

                    } else {
                        foundInterop = false;
                    }

                    if(!foundInterop){

                        CustomInteropInputs.LoadArgs loadArgs = new CustomInteropInputs.LoadArgs();

                        if(argumentProvider.getList().isEmpty()){

                            //if the current arg list is empty, populate it with something
                            Nvpair examplePair = new Nvpair();
                            examplePair.setName("Example key");
                            examplePair.setValue("Example value");

                            loadArgs.getNvpair().add(examplePair);

                            argumentProvider.getList().add(examplePair);

                        } else {

                            //otherwise, reassign the old args
                            for(Nvpair pair : argumentProvider.getList()){
                                loadArgs.getNvpair().add(pair);
                            }
                        }

                        argumentProvider.refresh();

                        CustomInteropInputs inputs = new CustomInteropInputs();
                        inputs.setLoadArgs(loadArgs);

                        InteropInputs interopInputs = new InteropInputs();
                        interopInputs.setInteropInput(inputs);

                        interop.setInteropInputs(interopInputs);
                    }

                } else {
                    WarningDialog.error("Missing interop plugin class", "The interop implementation class \"" + implClassName + "\" was not found on the server.<br/><br/>"
                            + "This class is needed in order to correctly author the selected training application type.");
                    return;
                }

                interops.getInterop().add(interop);
            }

        } else {
            WarningDialog.error("Missing interop plugin mapping", "No implementation mapping was found for '" + value.getDisplayName() + "'. It is "
                    + "possible that this training application type is new to GIFT and does not yet have an associated "
                    + "interface with which to author it.");
            return;
        }
    }

    /**
     * Sets the given practice application to current and all other current values to null.
     *
     * @param practiceApp the application to set.
     */
    private void setCurrentPracticeType(Serializable practiceApp) {

        currentTrainingApp = practiceApp instanceof TrainingApplication ? (TrainingApplication) practiceApp : null;
        currentLessonMaterial = practiceApp instanceof LessonMaterial ? (LessonMaterial) practiceApp : null;
    }

    /**
     * Restores the 'Click to Add' button on the real time assessment panel to allow the author
     * to create a new dkf.
     */
    public void removeAssessment(){
        dkfSelectPanel.removeAssessment();
    }

    /**
     * Sets the lesson material to edit
     *
     * @param lessonMaterial the lesson material to edit
     */
    public void setLessonMaterial(LessonMaterial lessonMaterial) {
        setCurrentPracticeType(lessonMaterial);

        // populate the editor
        if (currentLessonMaterial != null && currentLessonMaterial.getLessonMaterialList() != null
                && currentLessonMaterial.getLessonMaterialList().getMedia() != null) {

            for (Media media : currentLessonMaterial.getLessonMaterialList().getMedia()) {

                if (media.getMediaTypeProperties() instanceof LtiProperties) {
                    mediaPanel.editMetadataLtiForMbpPractice(media, null, true);
                    realTimeAssessmentPanel.setVisible(false);
                }

                applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(mediaPanel));

                choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));
            }
        }

        // let any listeners know that a valid training application type has been loaded
        if (choiceSelectedCommand != null) {
            choiceSelectedCommand.execute();
        }
    }

    /**
     * Sets the training application to edit
     *
     * @param trainingApp the training application to edit
     */
    public void setTrainingApplication(TrainingApplication trainingApp){

        //Checks to make sure that the TrainingApplication being passed in is
        //contains interops
        setCurrentPracticeType(trainingApp);

        final String icon = CourseElementUtil.getTypeIcon(trainingApp);
        titleLabel.setHTML(CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Application Title:");

        //clear load argument fields
        simpleTAScenarioNameBox.setValue(null);
        sudokuScenarioNameBox.setValue(null);
        testbedScenarioNameBox.setValue(null);
        vbsScenarioNameBox.setValue(null);
        tc3ScenarioNameBox.setValue(null);
        aresIdTextbox.setValue(null);
        aresDeckPanel.showWidget(0);
        aresSelectFilePanel.showWidget(0);
        aresIdButton.setValue(true, false);
        aresFileSelectionDialog.setValue(null);
        argumentProvider.getList().clear();
        embeddedArgumentProvider.getList().clear();
        argumentProvider.refresh();
        embeddedArgumentProvider.refresh();

        titleTextBox.setValue(null);

        fileSelectionDialog.setValue(null);
        
        xTSPFileSelectionDialog.setValue(null);

        realTimeAssessmentPanel.setVisible(true);

        // populate the editor
        if (currentTrainingApp != null){

            if(currentTrainingApp.getDkfRef() != null && currentTrainingApp.getDkfRef().getFile() != null){
                dkfSelectPanel.setAssessment(currentTrainingApp.getDkfRef().getFile());
                fileSelectionDialog.setValue(currentTrainingApp.getDkfRef().getFile());
                xTSPFileSelectionDialog.setValue(currentTrainingApp.getDkfRef().getFile());

                //don't show this label because a label will be shown in place of the dkf filename on the UI
                realTimeAssessmentLabel.setVisible(false);

            } else {
                dkfSelectPanel.removeAssessment();

                //show this label because the button label alone doesn't give enough info
                realTimeAssessmentLabel.setVisible(true);
            }

            boolean noInterops = true;

            TrainingApplicationEnum appType = null;
            if(currentTrainingApp.getInterops() != null){

                if(currentTrainingApp.getTrainingAppTypeEnum() != null){
                    try{
                        appType = TrainingApplicationEnum.valueOf(currentTrainingApp.getTrainingAppTypeEnum());
                        logger.info("Found training application type enum of "+appType);
                        setTrainingAppEditingMode(appType);
                    }catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
                        // value wasn't specified correctly in the training app course object,
                        // check the old way by looking at the interops
                        logger.warning("Found unhandled training application type of "+currentTrainingApp.getTrainingAppTypeEnum());
                    }
                }

                Interops interops = currentTrainingApp.getInterops();

                if(interops != null){

                    List<String> interopClassesNotHandled = new ArrayList<>();
                    
                    //Application list box and arguments
                    for(Interop interop : interops.getInterop()){

                        String implClass = interop.getInteropImpl();

                        if(implClass != null){

                            boolean foundImplClass = false;

                            for(TrainingApplicationEnum ta : TrainingAppUtil.trainingAppToInteropClassNames.keySet()){

                                List<String> classNames = TrainingAppUtil.trainingAppToInteropClassNames.get(ta);

                                if(classNames != null && classNames.contains(implClass)){
                                    foundImplClass = true;
                                    break;
                                }
                            }

                            if(!foundImplClass){
                                interopClassesNotHandled.add(implClass);
                            }
                        }

                        if(interop.getInteropInputs() != null && interop.getInteropInputs().getInteropInput() != null){

                            if(interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs){

                                if(appType != null){
                                    // the training application type was set explicitly, having just the DIS interop
                                    // will work (e.g. DIS interop w/ playback and no VBS plugin is valid)
                                    noInterops = false;

                                    DISInteropInputs disInteropInputs = (DISInteropInputs) interop.getInteropInputs().getInteropInput();
                                    if (appType == TrainingApplicationEnum.VBS) {
                                        // VBS training app enum configurations are:
                                        // 1. DIS + VBS - VBS scenario loaded and managed by gift
                                        // 2. DIS w/ log file - no VBS scenario, GIFT DS log played back
                                        // 3. DIS - no VBS scenario, no GIFT DS log, waiting for DIS traffic to assess
                                        
                                        if(disInteropInputs.getLogFile() != null){
                                            // log file was selected
                                            final DomainSessionLog domainSessionLog = disInteropInputs.getLogFile().getDomainSessionLog();
                                            if (logger.isLoggable(Level.INFO)) {
                                                logger.info(
                                                        "setTrainingApplication() : Setting domain session log file label to "
                                                                + domainSessionLog);
                                            }
                                        
                                            /* Set the selected log label */
                                            final String logFilename = domainSessionLog != null
                                                    ? domainSessionLog.getValue()
                                                    : null;
                                            domainSessionLogFileLabelTooltip.setTitle(logFilename);
                                            domainSessionLogFileLabel.setText(logFilename);
    
                                            /* Set the selected assessment label */
                                            final String assessmentName = domainSessionLog != null
                                                    ? domainSessionLog.getAssessmentName()
                                                    : null;
                                            if (StringUtils.isBlank(assessmentName)) {
                                                logAssessmentDeckPanel.showWidget(
                                                        logAssessmentDeckPanel.getWidgetIndex(chooseAssessmentPanel));
                                            } else {
                                                selectedAssessmentLabelTooltip.setTitle(assessmentName);
                                                selectedAssessmentLabel.setText(assessmentName);
                                                changeSelectedAssessmentBtn.setVisible(true);
                                                logAssessmentDeckPanel.showWidget(
                                                        logAssessmentDeckPanel.getWidgetIndex(selectedAssessmentPanel));
                                            }
    
                                            // load the captured audio file name if there is one
                                            String capturedAudio = disInteropInputs.getLogFile().getCapturedAudioFile();
                                            if (StringUtils.isNotBlank(capturedAudio)) {
                                                capturedAudioFileLabelTooltip.setTitle(capturedAudio);
                                                capturedAudioFileLabel.setText(capturedAudio);
                                                capturedAudioDeckPanel.showWidget(capturedAudioDeckPanel
                                                        .getWidgetIndex(capturedAudioSelectedPanel));
                                            } else {
                                                capturedAudioDeckPanel.showWidget(capturedAudioDeckPanel
                                                        .getWidgetIndex(selectCapturedAudioButton));
                                            }
    
                                            // show the panel for editing the vbs choice which contains a deck panel for vbs scenario
                                            // name OR log file or not specified
                                            vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));
    
                                            // show the panel for the log file which contains a deck panel for selecting the log file
                                            // OR removing the log file
                                            vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsDomainSessionLogPanel));
    
                                            // show the panel for editing the domain session log choice
                                            if (StringUtils.isBlank(logFilename)) {
                                                vbsDomainSessionLogdeckPanel.showWidget(vbsDomainSessionLogdeckPanel.getWidgetIndex(selectDomainSessionLogPanel));
                                            } else {
                                                vbsDomainSessionLogdeckPanel.showWidget(vbsDomainSessionLogdeckPanel.getWidgetIndex(domainSessionLogSelectedPanel));
                                            }
                                        }else{
                                            // show the panel for editing the vbs choice which contains a deck panel for vbs scenario
                                            // name OR log file
                                            vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));
    
                                            // show the panel for the not specified option which contains nothing
                                            vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsNotSpecifiedPanel));
                                        }
                                    } 
                                }

                            } else if(interop.getInteropInputs().getInteropInput() instanceof SimpleExampleTAInteropInputs){

                                boolean isSudoku = interop.getInteropImpl() != null && interop.getInteropImpl().equals(TrainingAppUtil.SUDOKU_TA_PLUGIN_INTERFACE);

                                if(isSudoku){

                                    //Sudoku
                                    setTrainingAppEditingMode(TrainingApplicationEnum.SUDOKU);

                                } else {

                                    //Simple Example TA
                                    setTrainingAppEditingMode(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA);

                                    // correct the interop impl class if it doesn't match its inputs
                                    interop.setInteropImpl(TrainingAppUtil.SIMPLE_EXAMPLE_TA_PLUGIN_INTERFACE);
                                }

                                SimpleExampleTAInteropInputs inputs = (SimpleExampleTAInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null){

                                    if(isSudoku){
                                        sudokuScenarioNameBox.setValue(inputs.getLoadArgs().getScenarioName());

                                    } else {
                                        simpleTAScenarioNameBox.setValue(inputs.getLoadArgs().getScenarioName());
                                    }
                                }

                                noInterops = false;

                                break;

                            } else if(interop.getInteropInputs().getInteropInput() instanceof DETestbedInteropInputs){

                                //Testbed
                                setTrainingAppEditingMode(TrainingApplicationEnum.DE_TESTBED);

                                DETestbedInteropInputs inputs = (DETestbedInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null && inputs.getLoadArgs().getScenarioName() != null){
                                    testbedScenarioNameBox.setValue(inputs.getLoadArgs().getScenarioName());
                                }

                                // correct the interop impl class if it doesn't match its inputs
                                interop.setInteropImpl(TrainingAppUtil.DE_TESTBED_INTERFACE_PLUGIN_XML_RPC_INTERFACE);

                                noInterops = false;

                                break;

                            } else if(interop.getInteropInputs().getInteropInput() instanceof VBSInteropInputs){

                                //VBS
                                setTrainingAppEditingMode(TrainingApplicationEnum.VBS);

                                // show the panel for editing the vbs choice which contains a deck panel for vbs scenario
                                // name OR log file
                                vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoiceEditorPanel));

                                VBSInteropInputs inputs = (VBSInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null){
                                    
                                    if(inputs.getLoadArgs().getScenarioName() != null){
                                        // a non-null, even empty scenario name means that the author, at some point, picked
                                        // scenario as the VBS course object vbsChoiceEditorDeckPanel item.
                                        
                                        // show the panel that contains the scenario name
                                        vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsScenarioNamePanel));
                                        
                                        vbsScenarioNameBox.setValue(inputs.getLoadArgs().getScenarioName());
                                    }else{
                                        vbsChoiceEditorDeckPanel.showWidget(vbsChoiceEditorDeckPanel.getWidgetIndex(vbsNotSpecifiedPanel));
                                    }
                                }else{
                                    // user needs to make a choice on the type of VBS scenario management
                                    
                                    // show the widget that allows choosing the VBS input type (scenario name or log file)
                                    vbsChoiceDeck.showWidget(vbsChoiceDeck.getWidgetIndex(vbsChoicePanel));
                                }

                                // correct the interop impl class if it doesn't match its inputs
                                interop.setInteropImpl(TrainingAppUtil.VBS_PLUGIN_INTERFACE);

                                noInterops = false;

                                break;

                            } else if(interop.getInteropInputs().getInteropInput() instanceof TC3InteropInputs){

                                //TC3
                                setTrainingAppEditingMode(TrainingApplicationEnum.TC3);

                                TC3InteropInputs inputs = (TC3InteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null){
                                    tc3ScenarioNameBox.setValue(inputs.getLoadArgs().getScenarioName());
                                }

                                // correct the interop impl class if it doesn't match its inputs
                                interop.setInteropImpl(TrainingAppUtil.TC3_PLUGIN_INTERFACE);

                                noInterops = false;

                                break;

                            } else if(interop.getInteropInputs().getInteropInput() instanceof PowerPointInteropInputs){

                                //PowerPoint
                                setTrainingAppEditingMode(TrainingApplicationEnum.POWERPOINT);

                                PowerPointInteropInputs inputs = (PowerPointInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null){

                                    if(inputs.getLoadArgs().getShowFile() != null){

                                        pptSelectedPanel.setVisible(true);
                                        selectPPTFilePanel.setVisible(false);
                                        pptFileLabel.setText(inputs.getLoadArgs().getShowFile());

                                    } else {

                                        pptSelectedPanel.setVisible(false);
                                        selectPPTFilePanel.setVisible(true);
                                        pptFileLabel.setText(NO_FILE_SELECTED);
                                    }
                                }

                                // correct the interop impl class if it doesn't match its inputs
                                interop.setInteropImpl(TrainingAppUtil.PPT_INTERFACE);

                                noInterops = false;

                                break;
                                
                            } else if(interop.getInteropInputs().getInteropInput() instanceof UnityInteropInputs){
                                // standalone Unity
                                setTrainingAppEditingMode(TrainingApplicationEnum.UNITY_DESKTOP);

                                UnityInteropInputs inputs = (UnityInteropInputs) interop.getInteropInputs()
                                        .getInteropInput();

                                if (inputs.getLoadArgs() == null) {
                                    inputs.setLoadArgs(new UnityInteropInputs.LoadArgs());
                                }

                                unityDesktopArgsTable.setNameValueList(inputs.getLoadArgs().getNvpair());

                                // correct the interop impl class if it doesn't
                                // match its inputs
                                interop.setInteropImpl(TrainingAppUtil.UNITY_PLUGIN_INTERFACE);

                                noInterops = false;

                                break;

                            } else if (interop.getInteropInputs().getInteropInput() instanceof VREngageInteropInputs) {

                                // VR-Engage
                                setTrainingAppEditingMode(TrainingApplicationEnum.VR_ENGAGE);

                                // correct the interop impl class if it doesn't
                                // match its inputs
                                interop.setInteropImpl(TrainingAppUtil.VR_ENGAGE_PLUGIN_INTERFACE);

                                noInterops = false;

                                break;

                            } else if (interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs) {
                                // HAVEN
                                setTrainingAppEditingMode(TrainingApplicationEnum.HAVEN);
                                
                                HAVENInteropInputs inputs = (HAVENInteropInputs) interop.getInteropInputs().getInteropInput();
                                
                                if (inputs != null) {
                                    if (inputs.getLogFile() != null) {
                                        // Log file has been selected
                                        final DomainSessionLog domainSessionLog = inputs.getLogFile().getDomainSessionLog();
                                        if (logger.isLoggable(Level.INFO)) {
                                            logger.info(
                                                    "setTrainingApplication() : Setting domain session log file label to "
                                                            + domainSessionLog);
                                        }
                                        
                                        /* Set the selected log label */
                                        final String logFilename = domainSessionLog != null
                                                ? domainSessionLog.getValue()
                                                : null;
                                        havenDomainSessionLogFileLabelTooltip.setTitle(logFilename);
                                        havenDomainSessionLogFileLabel.setText(logFilename);
                                        
                                        /* Set the selected assessment label */
                                        final String assessmentName = domainSessionLog != null
                                                ? domainSessionLog.getAssessmentName()
                                                : null;
                                        if (StringUtils.isBlank(assessmentName)) {
                                            havenLogAssessmentDeckPanel.showWidget(
                                                    havenLogAssessmentDeckPanel.getWidgetIndex(havenChooseAssessmentPanel));
                                        } else {
                                            havenSelectedAssessmentLabelTooltip.setTitle(assessmentName);
                                            havenSelectedAssessmentLabel.setText(assessmentName);
                                            havenChangeSelectedAssessmentBtn.setVisible(true);
                                            havenLogAssessmentDeckPanel.showWidget(
                                                    havenLogAssessmentDeckPanel.getWidgetIndex(havenSelectedAssessmentPanel));
                                        }
                                        
                                        // load the captured audio file name if there is one
                                        String capturedAudio = inputs.getLogFile().getCapturedAudioFile();
                                        if (StringUtils.isNotBlank(capturedAudio)) {
                                            havenCapturedAudioFileLabelTooltip.setTitle(capturedAudio);
                                            havenCapturedAudioFileLabel.setText(capturedAudio);
                                            havenCapturedAudioDeckPanel.showWidget(havenCapturedAudioDeckPanel
                                                    .getWidgetIndex(havenCapturedAudioSelectedPanel));
                                        } else {
                                            havenCapturedAudioDeckPanel.showWidget(havenCapturedAudioDeckPanel
                                                    .getWidgetIndex(havenSelectCapturedAudioButton));
                                        }

                                        // show the panel for editing the HAVEN choice which contains a deck panel for
                                        // log file or not specified
                                        havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));
                                        
                                        // show the panel for the log file which contains a deck panel for selecting the log file
                                        // OR removing the log file
                                        havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenDomainSessionLogPanel));

                                        // show the panel for editing the domain session log choice
                                        if (StringUtils.isBlank(logFilename)) {
                                            havenDomainSessionLogdeckPanel.showWidget(havenDomainSessionLogdeckPanel.getWidgetIndex(havenSelectDomainSessionLogPanel));
                                        } else {
                                            havenDomainSessionLogdeckPanel.showWidget(havenDomainSessionLogdeckPanel.getWidgetIndex(havenDomainSessionLogSelectedPanel));
                                        }
                                    } else {
                                        // Log file is null
                                        
                                        // show the panel for the not specified option
                                        havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenNotSpecifiedPanel));
                                        
                                        // show the panel for editing the HAVEN choice which contains a deck panel for
                                        // log file OR not specified
                                        havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));    
                                    }
                                } else {
                                    // Interop inputs are null
                                    
                                    // show the panel for the not specified option
                                    havenChoiceEditorDeckPanel.showWidget(havenChoiceEditorDeckPanel.getWidgetIndex(havenNotSpecifiedPanel));
                                    
                                    // show the panel for editing the HAVEN choice which contains a deck panel for
                                    // log file OR not specified
                                    havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));    
                                }
                                
                                // show the panel for editing the HAVEN choice which contains a deck panel for HAVEN not specified
                                // OR log file
                                havenChoiceDeck.showWidget(havenChoiceDeck.getWidgetIndex(havenChoiceEditorPanel));
                                                                
                                // correct the interop impl class if it doesn't
                                // match its inputs
                                interop.setInteropImpl(TrainingAppUtil.HAVEN_PLUGIN_INTERFACE);
                                                                
                                noInterops = false;
                                                                
                                break;
                                
                            } else if (interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs) {
                                // RIDE
                                setTrainingAppEditingMode(TrainingApplicationEnum.RIDE);
                                
                                RIDEInteropInputs inputs = (RIDEInteropInputs) interop.getInteropInputs().getInteropInput();
                                
                                if (inputs != null) {
                                    if (inputs.getLogFile() != null) {
                                        // Log file has been selected
                                        final DomainSessionLog domainSessionLog = inputs.getLogFile().getDomainSessionLog();
                                        if (logger.isLoggable(Level.INFO)) {
                                            logger.info(
                                                    "setTrainingApplication() : Setting domain session log file label to "
                                                            + domainSessionLog);
                                        }
                                        
                                        /* Set the selected log label */
                                        final String logFilename = domainSessionLog != null
                                                ? domainSessionLog.getValue()
                                                : null;
                                        rideDomainSessionLogFileLabelTooltip.setTitle(logFilename);
                                        rideDomainSessionLogFileLabel.setText(logFilename);
                                        
                                        /* Set the selected assessment label */
                                        final String assessmentName = domainSessionLog != null
                                                ? domainSessionLog.getAssessmentName()
                                                : null;
                                        if (StringUtils.isBlank(assessmentName)) {
                                            rideLogAssessmentDeckPanel.showWidget(
                                                    rideLogAssessmentDeckPanel.getWidgetIndex(rideChooseAssessmentPanel));
                                        } else {
                                            rideSelectedAssessmentLabelTooltip.setTitle(assessmentName);
                                            rideSelectedAssessmentLabel.setText(assessmentName);
                                            rideChangeSelectedAssessmentBtn.setVisible(true);
                                            rideLogAssessmentDeckPanel.showWidget(
                                                    rideLogAssessmentDeckPanel.getWidgetIndex(rideSelectedAssessmentPanel));
                                        }
                                        
                                        // load the captured audio file name if there is one
                                        String capturedAudio = inputs.getLogFile().getCapturedAudioFile();
                                        if (StringUtils.isNotBlank(capturedAudio)) {
                                            rideCapturedAudioFileLabelTooltip.setTitle(capturedAudio);
                                            rideCapturedAudioFileLabel.setText(capturedAudio);
                                            rideCapturedAudioDeckPanel.showWidget(rideCapturedAudioDeckPanel
                                                    .getWidgetIndex(rideCapturedAudioSelectedPanel));
                                        } else {
                                            rideCapturedAudioDeckPanel.showWidget(rideCapturedAudioDeckPanel
                                                    .getWidgetIndex(rideSelectCapturedAudioButton));
                                        }

                                        // show the panel for editing the RIDE choice which contains a deck panel for
                                        // log file or not specified
                                        rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));
                                        
                                        // show the panel for the log file which contains a deck panel for selecting the log file
                                        // OR removing the log file
                                        rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideDomainSessionLogPanel));

                                        // show the panel for editing the domain session log choice
                                        if (StringUtils.isBlank(logFilename)) {
                                            rideDomainSessionLogdeckPanel.showWidget(rideDomainSessionLogdeckPanel.getWidgetIndex(rideSelectDomainSessionLogPanel));
                                        } else {
                                            rideDomainSessionLogdeckPanel.showWidget(rideDomainSessionLogdeckPanel.getWidgetIndex(rideDomainSessionLogSelectedPanel));
                                        }
                                    } else {
                                        // Log file is null
                                        
                                        // show the panel for the not specified option
                                        rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideNotSpecifiedPanel));
                                        
                                        // show the panel for editing the RIDE choice which contains a deck panel for
                                        // log file OR not specified
                                        rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));    
                                    }
                                } else {
                                    // Interop inputs are null
                                    
                                    // show the panel for the not specified option
                                    rideChoiceEditorDeckPanel.showWidget(rideChoiceEditorDeckPanel.getWidgetIndex(rideNotSpecifiedPanel));
                                    
                                    // show the panel for editing the RIDE choice which contains a deck panel for
                                    // log file OR not specified
                                    rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));    
                                }
                                
                                // show the panel for editing the RIDE choice which contains a deck panel for RIDE not specified
                                // OR log file
                                rideChoiceDeck.showWidget(rideChoiceDeck.getWidgetIndex(rideChoiceEditorPanel));
                                                                
                                // correct the interop impl class if it doesn't
                                // match its inputs
                                interop.setInteropImpl(TrainingAppUtil.RIDE_PLUGIN_INTERFACE);
                                                                
                                noInterops = false;
                                                                
                                break;
                                
                            } else if(interop.getInteropInputs().getInteropInput() instanceof CustomInteropInputs){

                                TrainingApplicationEnum match = null;

                                if(interop.getInteropImpl() != null){

                                    for(TrainingApplicationEnum app : TrainingAppUtil.trainingAppToInteropClassNames.keySet()){

                                        List<String> interopClassNames = TrainingAppUtil.trainingAppToInteropClassNames.get(app);

                                        if(interopClassNames != null){

                                            for(String className : interopClassNames){

                                                if(className != null && interop.getInteropImpl().equals(className)){
                                                    match = app;
                                                    break;
                                                }
                                            }

                                            if(match != null){
                                                break;
                                            }
                                        }
                                    }
                                }

                                if(match != null){
                                    setTrainingAppEditingMode(match);

                                } else {

                                    //this should never really happen unless someone loads a course using an interop impl that doesn't exist
                                    setTrainingAppEditingMode(null);
                                }

                                CustomInteropInputs inputs = (CustomInteropInputs) interop.getInteropInputs().getInteropInput();


                                if(inputs.getLoadArgs() != null){

                                    for(Nvpair pair : inputs.getLoadArgs().getNvpair()){
                                        argumentProvider.getList().add(pair);
                                    }
                                }

                                argumentProvider.refresh();

                                noInterops = false;

                                break;

                            } else if (interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs
                                    && interop.getInteropImpl().equals(TrainingAppUtil.ARES_TA_PLUGIN_INTERFACE)) {

                                interop.setInteropImpl(TrainingAppUtil.ARES_TA_PLUGIN_INTERFACE);
                                setTrainingAppEditingMode(TrainingApplicationEnum.ARES);
                                noInterops = false;

                                GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();

                                if(inputs.getLoadArgs() != null){

                                    String contentRef = inputs.getLoadArgs().getContentRef();

                                    if(contentRef != null && contentRef.endsWith(".zip")) {
                                        aresFileSelectionDialog.setValue(contentRef, true);
                                        aresFileButton.setValue(true, true);

                                    } else if(contentRef != null) {
                                        aresIdTextbox.setValue(contentRef);
                                        aresIdButton.setValue(true, true);
                                    }

                                }

                                break;

                            } else if (interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs) {

                                TrainingApplicationEnum match = null;

                                if(interop.getInteropImpl() != null){
                                    // Find the TrainingApplicationEnum that maps to the interop impl value

                                    for(TrainingApplicationEnum app : TrainingAppUtil.trainingAppToInteropClassNames.keySet()){

                                        List<String> interopClassNames = TrainingAppUtil.trainingAppToInteropClassNames.get(app);

                                        if(interopClassNames != null){

                                            for(String className : interopClassNames){
                                                if(className != null && interop.getInteropImpl().equals(className)){
                                                    match = app;
                                                    break;
                                                }
                                            }

                                            if(match != null){
                                                break;
                                            }
                                        }
                                    }
                                }
                                if(match != null){

                                    setTrainingAppEditingMode(match);

                                    GenericLoadInteropInputs inputs = (GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput();
                                    if(inputs.getLoadArgs() != null){
                                        genericContentTextbox.setValue(inputs.getLoadArgs().getContentRef(), false);
                                    }

                                }

                                noInterops = false;

                                break;
                            }
                        }
                    }

                    if(!interopClassesNotHandled.isEmpty()){

                        StringBuilder sb = new StringBuilder();
                        sb.append("This training application uses one or more interop implementation classes that are not supported for ");
                        sb.append("authoring by this version of GIFT:<ul>");

                        for(String unhandledClass : interopClassesNotHandled){
                            sb.append("<li><b>");
                            sb.append(unhandledClass);
                            sb.append("</b></li>");
                        }

                        sb.append("</ul>You may experience issues authoring this training application's arguments or running this ");
                        sb.append("course until these interop implementation classes are supported by GIFT.<br/><br/>");

                        sb.append("If you wish to avoid these problems and use a supported interop implementation class, simply ");
                        sb.append("select a different application type to author.");

                        WarningDialog.error("Unsupported interops", sb.toString());
                    }
                }else{
                    logger.info("Found no interop object defined in the current training app course object");
                }

            } else if(currentTrainingApp.getEmbeddedApps() != null){

                EmbeddedApps apps = currentTrainingApp.getEmbeddedApps();

                if(apps.getEmbeddedApp() != null) {

                    EmbeddedApp app = apps.getEmbeddedApp();

                    if(app.getEmbeddedAppImpl() instanceof MobileApp) {

                        setTrainingAppEditingMode(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS);

                        //show the mobile app panel
                        applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(mobileAppPanel));

                        choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));

                        noInterops = false;

                    } else {

                        setTrainingAppEditingMode(TrainingApplicationEnum.UNITY_EMBEDDED);

                        //populate unity panel
                        if(app.getEmbeddedAppImpl() != null) {

                            selectUnityPanel.setVisible(false);
                            unitySelectedPanel.setVisible(true);

                            unityAppLabel.setTitle((String) app.getEmbeddedAppImpl());
                            unityAppLabel.setText(UNITY_FILE_SET);

                        } else {

                            //populate unity panel by default
                            selectUnityPanel.setVisible(true);
                            unitySelectedPanel.setVisible(false);

                            unityAppLabel.setTitle(Constants.EMPTY);
                            unityAppLabel.setText(NO_FILE_SELECTED);
                        }

                        if(app.getEmbeddedAppInputs() != null){
                            embeddedArgumentProvider.getList().addAll(app.getEmbeddedAppInputs().getNvpair());

                        } else {
                            embeddedArgumentProvider.getList().clear();
                        }

                        embeddedArgumentProvider.refresh();

                        //show the unity panel
                        applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(unityPanel));

                        choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));

                        noInterops = false;
                    }

                } else {

                    setTrainingAppEditingMode(TrainingApplicationEnum.UNITY_EMBEDDED);

                    //populate unity panel by default
                    selectUnityPanel.setVisible(true);
                    unitySelectedPanel.setVisible(false);

                    unityAppLabel.setTitle(Constants.EMPTY);
                    unityAppLabel.setText(NO_FILE_SELECTED);

                    embeddedArgumentProvider.getList().clear();
                    embeddedArgumentProvider.refresh();

                    //show the unity panel
                    applicationArgsDeckPanel.showWidget(applicationArgsDeckPanel.getWidgetIndex(unityPanel));

                    choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));

                    noInterops = false;
                }
            }

            if(noInterops){

                logger.info("There are no interops defined for training app type "+appType+", showing choice panel instead of specific training app authoring panel." );

                //give the user the option of picking what interops they want
                choiceDeck.showWidget(choiceDeck.getWidgetIndex(choicePanel));

                if(titleAuthorable){

                    titlePanel.setVisible(false);
                    titleTextBox.setValue(null);
                }

            } else {

                if(titleAuthorable){
                    titlePanel.setVisible(true);
                }
            }

            if(titleAuthorable){
                titleTextBox.setValue(currentTrainingApp.getTransitionName());
            }
        }

        //let any listeners know that a valid training application type has been loaded
        if(choiceSelectedCommand != null){
            choiceSelectedCommand.execute();
        }
        
        /* If an external scenario is being loaded, automatically show the DKF editor */
        if(GatClientUtility.getExternalScenarioId() != null) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    showDkfEditorDialog(true);
                }
            });
        }
    }

    /**
     * Parse a domain session log and extract the assessment scenarios.
     * 
     * @param filename the file for the domain session log.
     * @param callback the callback to execute on success or failure of the
     *        parse.
     */
    private void parseDomainSessionLog(final String filename, final AsyncCallback<List<LogMetadata>> callback) {
        final AsyncCallback<ParsePlaybackLogResult> parseLogCallback = new AsyncCallback<ParsePlaybackLogResult>() {
            @Override
            public void onFailure(Throwable caught) {
                BsLoadingDialogBox.remove();
                resetDomainSessionLogUI();
                WarningDialog.warning("Invalid Playback Log", "The selected playback log file '" + filename
                        + "' was not able to be parsed because '" + caught.getMessage() + "'.");
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ParsePlaybackLogResult result) {
                BsLoadingDialogBox.remove();
                List<LogMetadata> metadatas = result.getParsedMetadatas();
                if (result.isSuccess() && CollectionUtils.isNotEmpty(metadatas)) {
                    if (currentTrainingAppType == TrainingApplicationEnum.VBS) {
                        assessmentPickerDialog.populateOptions(metadatas);
    
                        /* Allow to change assessment selection if options exist */
                        changeSelectedAssessmentBtn.setVisible(metadatas.size() > 1);
    
                        /* Set the label */
                        domainSessionLogFileLabelTooltip.setTitle(filename);
                        domainSessionLogFileLabel.setText(filename);
                    } else if (currentTrainingAppType == TrainingApplicationEnum.HAVEN) {
                        assessmentPickerDialog.populateOptions(metadatas);
                        
                        /* Allow to change assessment selection if options exist */
                        havenChangeSelectedAssessmentBtn.setVisible(metadatas.size() > 1);
    
                        /* Set the label */
                        havenDomainSessionLogFileLabelTooltip.setTitle(filename);
                        havenDomainSessionLogFileLabel.setText(filename);
                    } else if (currentTrainingAppType == TrainingApplicationEnum.RIDE) {
                        assessmentPickerDialog.populateOptions(metadatas);
                        
                        /* Allow to change assessment selection if options exist */
                        rideChangeSelectedAssessmentBtn.setVisible(metadatas.size() > 1);
    
                        /* Set the label */
                        rideDomainSessionLogFileLabelTooltip.setTitle(filename);
                        rideDomainSessionLogFileLabel.setText(filename);
                    }
                } else {
                    resetDomainSessionLogUI();
                    WarningDialog.warning("Invalid Playback Log",
                            "The selected playback log file '" + filename + "' does not contain a complete scenario.");
                }
                callback.onSuccess(metadatas);
            }
        };

        try{
            final String relativeFilename = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + filename;
            ParsePlaybackLog action = new ParsePlaybackLog(GatClientUtility.getUserName(),
                    GatClientUtility.getBrowserSessionKey(), relativeFilename);
    
            WarningDialog.hideAll();
            BsLoadingDialogBox.display("Checking Log", "Validating the scenario inside the log file.");
    
            SharedResources.getInstance().getDispatchService().execute(action, parseLogCallback);
        }catch(Exception e){
            parseLogCallback.onFailure(e);
        }
    }

    /**
     * Gets the callback for when the modal dialog is closed
     *
     * @return the AsyncCallback object
     */
    private AsyncCallback<GatServiceResult> getSetAssessmentCallback() {
        return new AsyncCallback<GatServiceResult>(){

            @Override
            public void onFailure(Throwable thrown) {
                dkfSelectPanel.removeAssessment();
                realTimeAssessmentLabel.setVisible(true);
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
                    //don't show this label because a label will be shown in place of the dkf filename on the UUI
                    realTimeAssessmentLabel.setVisible(false);
                } else {
                    WarningDialog.error("Real-Time Assessment Missing" ,"The real-time assessment could not be found. The reference will be removed but will not be commited until the next time the course is saved.<br /><br />" +
                                        "<b>Error message: </b>" + result.getErrorMsg(),
                                        new ModalDialogCallback() {

                                            @Override
                                            public void onClose() {
                                                dkfSelectPanel.removeAssessment();
                                                realTimeAssessmentLabel.setVisible(true);
                                                currentTrainingApp.getDkfRef().setFile(null);
                                            }
                                        });
                }

            }

        };
    }

    /**
     * Gets the server-side package location of the interop class with the given name. If no such server-side location exists, then
     * null will be returned instead. This is mainly useful for verifying that interop impl classes actually exist on the server.
     *
     * @param interopClassName the name of the interop class to find
     * @return the location of the interop class
     */
    private String getInteropClassLocation(String interopClassName){

        for(String classLocation : interopClassLocations){

            if(classLocation.equals(interopClassName)){
                return classLocation;
            }
        }

        return null;
    }

    /**
     * Gets the list of interop implementation classes from the server
     */
    private void loadInteropImplementationsList(){

        AsyncCallback<FetchInteropImplementationsResult> callback = new AsyncCallback<FetchInteropImplementationsResult>(){

            @Override
            public void onFailure(Throwable thrown) {

                ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
                        "An problem occurred while trying to fetch the interop implementations needed to author a "
                                + "training application from the server.",
                        thrown.toString(), DetailedException.getFullStackTrace(thrown));

                errorDialog.setText("Failed to get Gateway Interop Plugins");
                errorDialog.center();
            }

            @Override
            public void onSuccess(FetchInteropImplementationsResult result) {

                if(result.isSuccess() && result.getInteropImplementations() != null){

                    interopClassLocations.addAll(result.getInteropImplementations());

                    Interops interops = currentTrainingApp != null
                            ? currentTrainingApp.getInterops()
                            : null;

                    //if a training app is currently being edited, set its appropriate interop impls based on its interop inputs
                    if(interops != null){

                        for(Interop interop : interops.getInterop()){

                            if(interop.getInteropInputs() != null
                                    && interop.getInteropInputs().getInteropInput() != null){

                                for(String interopImplClassName : TrainingAppUtil.interopClassNameToInputClass.keySet()){

                                    Class<?> inputClass = TrainingAppUtil.interopClassNameToInputClass.get(interopImplClassName);

                                    if(inputClass.equals(interop.getInteropInputs().getInteropInput())){

                                        interop.setInteropImpl(getInteropClassLocation(interopImplClassName));

                                        break;
                                    }
                                }
                            }
                        }
                    }

                } else {

                    ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
                            "An error occurred while initializing the interop implementations needed to "
                                    + "author a training application : "
                            + result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());

                    errorDialog.setText("Failed to get Gateway Interop Plugins");
                    errorDialog.center();

                }
            }

        };

        FetchInteropImplementations action = new FetchInteropImplementations();
        SharedResources.getInstance().getDispatchService().execute(action, callback);
    }

    /**
     * Gets a string describing the validation problems with the current training application. If no problems are found, then an
     * empty string will be returned.
     *
     * @return the validation error message
     */
    public String getValidationErrors(){
        
        StringBuilder errorMsg = new StringBuilder();

        if(currentTrainingApp != null){

            if(titleAuthorable){

                if(currentTrainingApp.getTransitionName() == null || currentTrainingApp.getTransitionName().isEmpty()){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this application.")
                            .append("</li>");
                }
            }
            
            if(currentTrainingApp.getInterops() == null && currentTrainingApp.getEmbeddedApps() == null){

                errorMsg.append("")
                        .append("<li>")
                        .append("No application type has been specified.")
                        .append("</li>");

            } else {
                
                if(currentTrainingApp.getInterops() != null){

                    Interops interops = currentTrainingApp.getInterops();

                    if(interops.getInterop() != null){

                        for(Serializable interop : interops.getInterop()){
                            
                            if(interop != null
                                    && interop instanceof Interop
                                    && ((Interop) interop).getInteropInputs().getInteropInput() != null){

                                Serializable interopInput = ((Interop) interop).getInteropInputs().getInteropInput();

                                if(interopInput instanceof SimpleExampleTAInteropInputs){

                                    if(((SimpleExampleTAInteropInputs) interopInput).getLoadArgs() == null
                                            || ((SimpleExampleTAInteropInputs) interopInput).getLoadArgs().getScenarioName() == null){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No scenario name has been given to use with this practice application.")
                                                .append("</li>");
                                    }

                                } else if(interopInput instanceof DETestbedInteropInputs){

                                    if(((DETestbedInteropInputs) interopInput).getLoadArgs() == null
                                            || ((DETestbedInteropInputs) interopInput).getLoadArgs().getScenarioName() == null){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No scenario name has been given to use with this practice application.")
                                                .append("</li>");
                                    }

                                } else if(interopInput instanceof VBSInteropInputs){

                                    if(((VBSInteropInputs) interopInput).getLoadArgs() == null){
                                        // load args need to be specified
                                        
                                        errorMsg.append("")
                                            .append("<li>")
                                            .append("Need to choose whether to provide a VBS scenario name or not manage the scenario.")
                                            .append("</li>");
                                        
                                    }else if( ((VBSInteropInputs) interopInput).getLoadArgs().getScenarioName() != null &&
                                            StringUtils.isBlank(((VBSInteropInputs) interopInput).getLoadArgs().getScenarioName())){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No scenario name has been given to use with the VBS.")
                                                .append("</li>");
                                    }

                                } else if(interopInput instanceof TC3InteropInputs){

                                    if(((TC3InteropInputs) interopInput).getLoadArgs() == null
                                            || ((TC3InteropInputs) interopInput).getLoadArgs().getScenarioName() == null){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No scenario name has been given to use with this practice application.")
                                                .append("</li>");
                                    }

                                } else if(interopInput instanceof PowerPointInteropInputs){

                                    if(((PowerPointInteropInputs) interopInput).getLoadArgs() == null
                                            || ((PowerPointInteropInputs) interopInput).getLoadArgs().getShowFile() == null){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No PowerPoint show file has been selected to use with this practice application.")
                                                .append("</li>");
                                    }

                                } else if(interopInput instanceof CustomInteropInputs){

                                    if(((CustomInteropInputs) interopInput).getLoadArgs() == null
                                            || ((CustomInteropInputs) interopInput).getLoadArgs().getNvpair() == null
                                            || ((CustomInteropInputs) interopInput).getLoadArgs().getNvpair().isEmpty()){

                                        errorMsg.append("")
                                                .append("<li>")
                                                .append("No load arguments have been specified to use with this practice application.")
                                                .append("</li>");
                                    }
                                }
                            }
                        }

                    } else {

                        errorMsg.append("")
                                .append("<li>")
                                .append("No application type has been specified.")
                                .append("</li>");
                    }

                } else if(currentTrainingApp.getEmbeddedApps() != null){

                    EmbeddedApps apps = currentTrainingApp.getEmbeddedApps();

                    if(apps.getEmbeddedApp() != null){

                        EmbeddedApp app = apps.getEmbeddedApp();

                        if(app.getEmbeddedAppImpl() != null) {

                            if(app.getEmbeddedAppImpl() instanceof String) {

                                String url = (String) app.getEmbeddedAppImpl();

                                if(url == null || url.isEmpty()){
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("No Unity application file has been selected to use with this application.")
                                    .append("</li>");
                        }

                            } else if (!(app.getEmbeddedAppImpl() instanceof MobileApp)){
                                errorMsg.append("")
                                .append("<li>")
                                .append("An invalid embedded application implementation has been selected to use with this application.")
                                .append("</li>");
                            }
                        }

                    } else {
                        errorMsg.append("")
                                .append("<li>")
                                .append("No Unity application file has been selected to use with this application.")
                                .append("</li>");
                    }

                } else {

                    errorMsg.append("")
                            .append("<li>")
                            .append("An invalid application type has been specified. Please select another application.")
                            .append("</li>");
                }
            }
            
            if(currentTrainingApp.getDkfRef() == null
                    || currentTrainingApp.getDkfRef().getFile() == null){

                errorMsg.append("")
                        .append("<li>")
                        .append("No real-time assessment has been set for this practice content.")
                        .append("</li>");
            }
        } else if (currentLessonMaterial != null && currentLessonMaterial.getLessonMaterialList() != null
                && currentLessonMaterial.getLessonMaterialList().getMedia() != null) {
            for (Media media : currentLessonMaterial.getLessonMaterialList().getMedia()) {
                // validate media name
                if (media.getName() == null || media.getName().isEmpty()) {
                    errorMsg.append("<li>").append("Please specify the name to give this LTI provider.").append("</li>");
                }

                // validate media url
                if (media.getUri() == null) {
                    errorMsg.append("<li>").append("Please specify the URL address for the content to be used.").append("</li>");
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
                        errorMsg.append("<li>").append("The URL must start with one of the following prefixes: ")
                                .append(Constants.VALID_URL_SCHEMES).append(".</li>");
                    }
                }

                if (media.getMediaTypeProperties() instanceof LtiProperties) {
                    validateLtiProperties((LtiProperties)media.getMediaTypeProperties(), errorMsg);
                }
            }
        }

        return errorMsg.toString();
    }

    /**
     * Builds a string describing the validation problems with the given LTI properties if any exist.
     *
     * @param properties the {@link LtiProperties}
     * @param errorMsg the validation error message
     */
    private void validateLtiProperties(LtiProperties properties, StringBuilder errorMsg) {
        // check lti property specific fields

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
                    errorMsg.append("<li>").append("You have an incomplete custom parameter. Please complete the missing field or remove the entry.")
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

    /**
     * Sets a callback to be invoked any time a new PowerPoint file is selected
     *
     * @param callback the new value of the pptChangedCallback member
     */
    public void setPowerPointFileChangeCallback(PowerPointFileChangedCallback callback){
        this.pptChangedCallback = callback;
    }

    /**
     * Sets a callback to be invoked any time a field is edited
     *
     * @param callback the new value of the editedCallback member
     */
    public void setInteropsEditedCallback(InteropsEditedCallback callback){
        this.editedCallback = callback;

        // set the media panel's on change method to match the callback command
        mediaPanel.setOnChangeCommand(new Command() {

            @Override
            public void execute() {
                if (editedCallback != null) {
                    editedCallback.onEdit();
                }
            }
        });
    }

    /**
     * If true, disables or hides certain fields that shouldn't be modifiable in the Metadata editor. Otherwise, all fields will be
     * presented normally.
     *
     * Note: this is called by GWT logic due to the reference in TrainingAppRefEditor.ui.xml which calls with the value of true.
     *
     * @param metadataModeEnabled whether or not to enabled metadata mode
     */
    public void setMetadataModeEnabled(boolean metadataModeEnabled){

        this.metadataModeEnabled = metadataModeEnabled;

        if(metadataModeEnabled){

            removePptButton.setVisible(false);
            dkfSelectPanel.showAddButton(true);
            selectUnityPanel.setVisible(false);
            unitySelectedPanel.setVisible(true);

        } else {

            removePptButton.setVisible(true);
            dkfSelectPanel.showAddButton(false);
            selectUnityPanel.setVisible(true);
            unitySelectedPanel.setVisible(false);
        }
    }

    /**
     * Sets whether or not this editor should present its title field so authors can modify the titles of
     * loaded training applications
     *
     * @param authorable whether or not the title field should be authorable
     */
    public void setTitleAuthorable(boolean authorable){

        this.titleAuthorable = authorable;
    }

    /**
     * Sets the path to the course folder
     *
     * @param courseFolderPath The path to the course folder.
     */
    public void setCourseFolderPath(String courseFolderPath) {
        logger.info("setCourseFolderPath = '"+courseFolderPath+"'.");
        this.courseFolderPath = courseFolderPath;
    }

    public void setCourseSurveyContextId(BigInteger surveyContextId) {
        logger.info("setCourseSurveyContextId = " + surveyContextId);
        this.courseSurveyContextId = surveyContextId;

}


    @Override
    public void onCancelModal(boolean removeSelection) {
        logger.info("onCancelModal() called - removeSelection = " + removeSelection);

        if (removeSelection) {
            fileSelectionDialog.setValue("", true);
            xTSPFileSelectionDialog.setValue("", true);
        }

    }

    /**
     * Generates interops corresponding to the given type of training application and assigns them to the training application
     * currently being edited.
     *
     * @param type the type of training application to generate
     */
    private void setupApplicationInterops(TrainingApplicationEnum type){

        if(currentTrainingApp == null) {
            currentTrainingApp = new TrainingApplication();
        }

        final TrainingApplication trainingApp = currentTrainingApp;

        Interops interops = trainingApp.getInterops() != null
                ? (Interops) trainingApp.getInterops()
                : null;

        if(TrainingAppUtil.trainingAppToInteropClassNames.get(type) != null){

            List<String> interopImpls = TrainingAppUtil.trainingAppToInteropClassNames.get(type);

            if(interops == null){
                trainingApp.setInterops(interops = new Interops());
            }

            interops.getInterop().clear();

            for(String implClassName : interopImpls){

                Interop interop = new Interop();

                if(implClassName != null){

                    interop.setInteropImpl(implClassName);

                    Class<?> interopImplInputClass = TrainingAppUtil.interopClassNameToInputClass.get(implClassName);

                    boolean foundInterop = true;

                    if(interopImplInputClass != null){

                        if(interopImplInputClass.equals(SimpleExampleTAInteropInputs.class)){

                            SimpleExampleTAInteropInputs.LoadArgs loadArgs = new SimpleExampleTAInteropInputs.LoadArgs();

                            SimpleExampleTAInteropInputs inputs = new SimpleExampleTAInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(DETestbedInteropInputs.class)){

                            DETestbedInteropInputs.LoadArgs loadArgs = new DETestbedInteropInputs.LoadArgs();

                            DETestbedInteropInputs inputs = new DETestbedInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(VBSInteropInputs.class)){

                            VBSInteropInputs inputs = new VBSInteropInputs();
                            
                            VBSInteropInputs.LoadArgs loadArgs = new VBSInteropInputs.LoadArgs();
                            if (!GatClientUtility.isRtaLessonLevel()) {
                                // default to showing scenario name field for new VBS interops 
                                loadArgs.setScenarioName("");
                            }
                            
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(TC3InteropInputs.class)){

                            TC3InteropInputs.LoadArgs loadArgs = new TC3InteropInputs.LoadArgs();

                            TC3InteropInputs inputs = new TC3InteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(PowerPointInteropInputs.class)){

                            PowerPointInteropInputs.LoadArgs loadArgs = new PowerPointInteropInputs.LoadArgs();

                            PowerPointInteropInputs inputs = new PowerPointInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(DISInteropInputs.class)){

                            DISInteropInputs.LoadArgs loadArgs = new DISInteropInputs.LoadArgs();

                            DISInteropInputs inputs = new DISInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);
                            
                        } else if (interopImplInputClass.equals(UnityInteropInputs.class)) {
                            UnityInteropInputs.LoadArgs loadArgs = new UnityInteropInputs.LoadArgs();

                            UnityInteropInputs inputs = new UnityInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if (interopImplInputClass.equals(VREngageInteropInputs.class)) {
                            VREngageInteropInputs inputs = new VREngageInteropInputs();

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        }else if(interopImplInputClass.equals(HAVENInteropInputs.class)){
                            
                            HAVENInteropInputs.LoadArgs loadArgs = new HAVENInteropInputs.LoadArgs();
                            
                            HAVENInteropInputs inputs = new HAVENInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(RIDEInteropInputs.class)){
                            
                            RIDEInteropInputs.LoadArgs loadArgs = new RIDEInteropInputs.LoadArgs();
                            
                            RIDEInteropInputs inputs = new RIDEInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else if(interopImplInputClass.equals(GenericLoadInteropInputs.class)){

                            GenericLoadInteropInputs.LoadArgs loadArgs = new GenericLoadInteropInputs.LoadArgs();

                            GenericLoadInteropInputs inputs = new GenericLoadInteropInputs();
                            inputs.setLoadArgs(loadArgs);

                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);

                            interop.setInteropInputs(interopInputs);

                        } else {
                            foundInterop = true;
                        }

                    } else {
                        foundInterop = false;
                    }

                    if(!foundInterop){

                        CustomInteropInputs.LoadArgs loadArgs = new CustomInteropInputs.LoadArgs();

                        CustomInteropInputs inputs = new CustomInteropInputs();
                        inputs.setLoadArgs(loadArgs);

                        InteropInputs interopInputs = new InteropInputs();
                        interopInputs.setInteropInput(inputs);

                        interop.setInteropInputs(interopInputs);
                    }

                } else {

                    WarningDialog.error("Failed to get interop", "The interop implementation class \"" + implClassName + "\" was not found on the server.<br/><br/>"
                            + "This class is needed in order to correctly author the selected training application type.");
                }

                interops.getInterop().add(interop);
            }

        } else {
            WarningDialog.error("Missing interop mapping", "No implementation mapping was found for '" + type.getDisplayName() + "'. It is "
                    + "possible that this training application type is new to GIFT and does not yet have an associated "
                    + "interface with which to author it.");
        }

        // if there is no DKF file, attempt to default it to the simplest.dkf.xml
        if (trainingApp.getDkfRef() == null) {
            copyDKFTemplate(trainingApp, null);
        } else {
            setTrainingApplication(trainingApp);
        }
    }

    /**
     * Copy the 'simplest' DKF file to use as a default
     *
     * @param trainingApp the training application to set the DKF
     * @param fileCopiedCallback callback that gets triggered when the file completes or fails to copy
     */
    private void copyDKFTemplate(final TrainingApplication trainingApp, final AsyncCallback<CopyWorkspaceFilesResult> fileCopiedCallback ) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("copyDKFTemplate()... Attempting to copy the 'simplest' DKF file to use as a default.");
        }

        CopyTemplateFile action = new CopyTemplateFile(GatClientUtility.getUserName(), GatClientUtility.getBaseCourseFolderPath(),
                "TrainingApp", AbstractSchemaHandler.DKF_FILE_EXTENSION);

        BsLoadingDialogBox.display("Creating External Application", "Creating external application resouces, please wait...");
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CopyWorkspaceFilesResult>() {

            @Override
            public void onFailure(Throwable caught) {

                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Caught exception while copying dkf template file: '" + caught.toString()
                            + "' Application will not have simplest DKF file initialized.");
                }

                BsLoadingDialogBox.remove();
                setTrainingApplication(trainingApp);
                if (fileCopiedCallback != null) {
                    fileCopiedCallback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(CopyWorkspaceFilesResult result) {

                BsLoadingDialogBox.remove();
                if (result.isSuccess()) {
                    String dkfFile = result.getCopiedFilename();

                    if (dkfFile != null) {
                        DkfRef dkfRef = new DkfRef();
                        dkfRef.setFile(dkfFile);
                        trainingApp.setDkfRef(dkfRef);

                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("The 'simplest' DKF file was copied successfully to the training application.");
                        }

                        setTrainingApplication(trainingApp);
                    } else {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.warning(
                                    "Failed to copy the dkf template for the external application transition because the dkf file is null:\n"
                                            + result.getErrorMsg() + "\n" + result.getErrorDetails());
                        }

                        setTrainingApplication(trainingApp);
                    }
                } else {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning("Failed to copy the dkf template for the external application transition:\n" + result.getErrorMsg() + "\n"
                                + result.getErrorDetails());
                    }

                    setTrainingApplication(trainingApp);
                }

                if (fileCopiedCallback != null) {
                    fileCopiedCallback.onSuccess(result);
                }
            }
        });
    }

    /**
     * Performs a request to the server to extract the contents of a .zip file. If successful, the path to the base web page will be copied
     * to the Unity WebGL application course object. If more than one web page is extracted from the .zip, the author will be
     * prompted to select which one should be used.
     *
     * @param action The action to perform
     * @param copyCallback The callback to execute if an existing .zip is being copied or null if this is an upload operation.
     * @param uploadCallback The callback to execute if a new slide .zip is being uploaded or null if this is a copy operation.
     */
    private void copyOrUploadZip(final UnzipFile action, final CopyFileCallback copyCallback, final HandleUploadedFileCallback uploadCallback) {

        unityFileSelectionDialog.hide();

        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

            @Override
            public void onFailure(Throwable caught) {

                 logger.severe("onFailure() occurred when extracting the archive: " + caught.getMessage());

                 WarningDialog.error(
                         "Failed to extract files from archive",
                         "An unexpected error occurred while extracting files from the archive: " + caught.getMessage()
                );
            }

            @Override
            public void onSuccess(final GatServiceResult result) {

                unzipProgressModal.startPollForProgress(new AsyncCallback<LoadedProgressIndicator<UnzipFileResult>>() {

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

                                    selectUnityAppFile(parentPath + "/" + firstHtmlFile);

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

                                            selectUnityAppFile(parentPath + "/" + selectedHtmlFile);
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

                });
            }

        });
    }

    /**
     * Selects the file with the given file name as the web page containing the target WebGL application. This will
     * update the underlying JAXB course objects to save the selection and will validate the chosen file to ensure it is
     * a valid, GIFT-compatible Unity application.
     *
     * @param fileName the name of the web page file containing the Unity application to select
     */
    private void selectUnityAppFile(String fileName){

        if(currentTrainingApp != null){

            if(currentTrainingApp.getEmbeddedApps() == null || currentTrainingApp.getInterops() != null){

                //remove any interops, since we don't need them
                currentTrainingApp.setInterops(null);
                currentTrainingApp.setEmbeddedApps(new EmbeddedApps());
            }

            //create the appropriate schema objects for the Unity application
            EmbeddedApps apps = currentTrainingApp.getEmbeddedApps();

            EmbeddedApp unityApp = new EmbeddedApp();
            unityApp.setEmbeddedAppImpl(fileName); //the URL will tell GIFT where to host Unity from at runtime

            apps.setEmbeddedApp(unityApp);

            if(fileName != null){

                //update the Unity editor to reflect the updated schema objects
                selectUnityPanel.setVisible(false);
                unitySelectedPanel.setVisible(true);

                unityAppLabel.setTitle(fileName);
                unityAppLabel.setText(UNITY_FILE_SET);

                if(!fileName.isEmpty()){
                    validateUnityWebGLApplication(fileName);
                }

            } else {

                //reset the Unity editor to its default state
                selectUnityPanel.setVisible(true);
                unitySelectedPanel.setVisible(false);

                unityAppLabel.setTitle(Constants.EMPTY);
                unityAppLabel.setText(NO_FILE_SELECTED);
            }

            if(editedCallback != null){
                editedCallback.onEdit();
            }
        }
    }

    /**
     * Assigns a listener that will be notified when the user selects a different type of application
     *
     * @param command the listener command
     */
    public void setChoiceSelectionListener(Command command) {
        this.choiceSelectedCommand = command;
    }

    /**
     * Saves the course in the background so it no longer references
     * a deleted object if the user cancels the editor instead of saving
     */
    private native static void saveCourse()/*-{

		if ($wnd.saveCourse != null) {
			$wnd.saveCourse();
		}

    }-*/;

    /**
     * Validates the Unity WebGL application at the given file location to ensure that it is compatible with GIFT. If the
     * file is not compatible, the validation logic will attempt to add the appropriate logic to make it compatible.
     *
     * @param filePath
     */
    private void validateUnityWebGLApplication(final String filePath) {

        ValidateUnityWebGLApplication action = new ValidateUnityWebGLApplication(
                GatClientUtility.getUserName(),
                DefaultGatFileSelectionDialog.courseFolderPath + "/" + filePath);

        BsLoadingDialogBox.display("Validating Application File", "Validating, please wait...");
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

            @Override
            public void onFailure(Throwable thrown) {
                BsLoadingDialogBox.remove();
                WarningDialog.error("Failed to validate", "Failed to validate application file because a server error occurred: " + thrown.toString());
            }

            @Override
            public void onSuccess(GatServiceResult result) {
                BsLoadingDialogBox.remove();

                if(!result.isSuccess()) {
                    ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
                            result.getErrorMsg(),
                            result.getErrorDetails(),
                            result.getErrorStackTrace());
                    errorDialog.setText("An Error Occurred");
                    errorDialog.center();
                }
            }

        });
    }

    /**
     * Retrieves the current training application. Can be null.
     *
     * @return the {@link TrainingApplication}
     */
    public TrainingApplication getCurrentTrainingApp() {
        return currentTrainingApp;
    }

    /**
     * Retrieves the current lesson material. Can be null.
     *
     * @return the {@link LessonMaterial}
     */
    public LessonMaterial getCurrentLessonMaterial() {
        return currentLessonMaterial;
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		simpleTAScenarioNameBox.setEnabled(!isReadOnly);
		testbedScenarioNameBox.setEnabled(!isReadOnly);
		vbsScenarioNameBox.setEnabled(!isReadOnly);
		tc3ScenarioNameBox.setEnabled(!isReadOnly);
		sudokuScenarioNameBox.setEnabled(!isReadOnly);
		aresIdTextbox.setEnabled(!isReadOnly);
		genericContentTextbox.setEnabled(!isReadOnly);
		removePptButton.setEnabled(!isReadOnly);
		aresIdButton.setEnabled(!isReadOnly);
		aresFileButton.setEnabled(!isReadOnly);
		selectAresFileButton.setEnabled(!isReadOnly);
		powerPointButton.setEnabled(!isReadOnly);
		vbsButton.setEnabled(!isReadOnly);
		tc3Button.setEnabled(!isReadOnly);
		testbedButton.setEnabled(!isReadOnly);
		aresButton.setEnabled(!isReadOnly);
		unityButton.setEnabled(!isReadOnly);
		mobileAppButton.setEnabled(!isReadOnly);
		exampleAppButton.setEnabled(!isReadOnly);
		ltiProviderButton.setEnabled(!isReadOnly);
		titleTextBox.setEnabled(!isReadOnly);
        removePptButton.setEnabled(!isReadOnly);
        removePptButton.addStyleName("buttonEnabled");
		removeUnityButton.setVisible(!isReadOnly);
		addEmbeddedArgumentButton.setVisible(!isReadOnly);
		embeddedArgumentTable.redraw();
	}
	
	/**
	 * Sets the command to invoke whenever the training app's referenced DKF file is changed.
	 * 
	 * @param command the command to invoke. Can be null, if no command should be invoked.
	 */
	public void setDkfChangedCommand(Command command) {
	    this.dkfChangedCommand = command;
	}
	
	/**
	 * Notifies any listeners when the DKF file referenced by the training application has changed
	 */
	private void onDkfChanged() {
	    
	    if(dkfChangedCommand != null) {
	        dkfChangedCommand.execute();
	    }
	}
	
	/**
     * Sets the command to invoke whenever the author selects the type of training application to author
     * 
     * @param command the command to invoke. Can be null, if no command should be invoked.
     */
	public void setTypeSelectedCommand(Command command) {
        this.typeSelectedCommand = command;
    }
	
	/**
     * Notifies any listeners when the author selects the type of training application to author
     */
	private void onTypeSeleted() {
        
        if(typeSelectedCommand != null) {
            typeSelectedCommand.execute();
        }
    }
	
	/**
	 * Gets the choice widgets that this editor uses to allow the author to pick the type of training application to author.
	 * This can be useful for showing said choices in a different editor's ribbon.
	 * 
	 * @return the choices that allow the author to pick their training application type.
	 */
	public List<Widget> getTypeChoices(){
	    List<Widget> choices = new ArrayList<>();
	    for(int i = 0; i < choicePanel.getWidgetCount(); i++) {
	        choices.add(choicePanel.getWidget(i));
	    }
	    return choices;
	}
	
	/**
     * Whether or not the dkf being edited is for remediation activity (e.g. remediation for a course object)
     * or not (e.g. the course object dkf) 
     * 
     * @param enabled flag used to customize the dkf editing experience
     */
    public void setRemediationEnabled(boolean enabled){
        isRemediationMode  = enabled;
    }
}
