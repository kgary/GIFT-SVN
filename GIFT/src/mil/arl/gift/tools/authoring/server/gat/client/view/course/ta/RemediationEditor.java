/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.ValueListBox;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import generated.course.BooleanEnum;
import generated.course.ImageProperties;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.PDFProperties;
import generated.course.SlideShowProperties;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import generated.course.YoutubeVideoProperties;
import generated.course.TrainingApplication.Options.Remediation;
import generated.metadata.ActivityType;
import generated.metadata.Attribute;
import generated.metadata.Attributes;
import generated.metadata.Concept;
import generated.metadata.Metadata;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.GenericDataProvider;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.AddRemediationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.ContentFileWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateMetadataAttribute;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateQuestionExportReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFilesResult;

/**
 * An editor used to present a UI so authors can modify remediation for a training application
 * 
 * @author nroberts
 */
public class RemediationEditor extends AbstractCourseObjectEditor<Remediation>{

    private static RemediationEditorUiBinder uiBinder = GWT.create(RemediationEditorUiBinder.class);

    interface RemediationEditorUiBinder extends UiBinder<Widget, RemediationEditor> {
    }
    
    private static final Logger logger = Logger.getLogger(RemediationEditor.class.getName());
    
    private static final String NO_REMEDIATION_CONTENT_MESSAGE = "No Remediation phase content was found for the selected course concept(s).";
    
    private static final String DO_NOT_REPEAT = "Do not repeat";
    private static final String REPEAT_INDEFINITELY = "Repeat indefinitely";
    private static final String REPEAT_UP_TO = "Repeat up to...";
    
    /** A label shown then there are no course concepts */
    @UiField
    protected Label noCourseConceptsLabel;
    
    /** The checkbox that allows the author to decide whether rule/example content should be used by the remediation */
    @UiField
    protected CheckBox excludeRuleExampleContentCheckBox;
    
    /** The button used to refresh the list of remediation files*/
    @UiField
    protected org.gwtbootstrap3.client.ui.Button remediationRefreshButton;
    
    /** The button used to display the dialog used to add remediation content */
    @UiField(provided=true)
    protected Image addRemediationContentButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    /** The loading indicator shown while loading the remediation file list */
    @UiField
    protected BsLoadingIcon remediationFilesLoadingIcon;
    
    /** The panel where the list of remediation files is displayed */
    @UiField
    protected FlowPanel remediationFilesList;
    
    /** The panel containing the loading indicator for the remediation file list*/
    @UiField
    protected Widget remediationFilesLoadingPanel;

    /** The selection box used to select how remediation content should be repeated */
    @UiField(provided = true)
    protected ValueListBox<String> repeatRuleBox = new ValueListBox<String>(new Renderer<String>() {

        @Override
        public String render(String object) {
            return object == null ? "" : object.toString();
        }

        @Override
        public void render(String object, Appendable appendable) throws IOException {
            appendable.append(object);
        }
    });
    
    /** The panel containing the number spinner that specifies the number of repeat attempts learners can make */
    @UiField
    protected Widget allowedAttemptsPanel;
    
    /** The number spinner that specifies the number of repeat attempts learners can make */
    @UiField(provided = true)
    protected NumberSpinner repeatAllowedAttempts = new NumberSpinner(1, 1, Integer.MAX_VALUE, 1);
    
    /** A dialog used to modify existing metadata files*/
    @UiField
    protected CourseObjectModal metadataObjectDialog;
    
    /** The dialog used to build the metadata for new remediation content */
    protected AddRemediationDialog addRemediationDialog = new AddRemediationDialog();
    
    /** Data provider for the 'Concepts:' table in the dialog used to add remediation content */
    private ListDataProvider<CandidateConcept> contentConceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** Data provider for the 'Attributes:' table in the dialog used to add remediation content */
    private ListDataProvider<CandidateMetadataAttribute> contentAttributesTableDataProvider = new ListDataProvider<CandidateMetadataAttribute>();
    
    /** Selection model for the 'Concepts:' table in the dialog used to add remediation content */
    private SingleSelectionModel<CandidateConcept> contentConceptsTableSelectionModel = new SingleSelectionModel<CandidateConcept>();
    
    /** Data provider for the concepts list used by the LTI remediation editor */
    private ListDataProvider<CandidateConcept> conceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** The list of LTI Providers used by the LTI remediation editor */
    private GenericDataProvider<LtiProvider> contentLtiProvidersDataProvider = new GenericDataProvider<LtiProvider>(); 
    
    /** The remediation currently being edited by this editor. Can */
    private Remediation remediation = null;

    /** The metadata currently being edited. Used when the author clicks the button to add remediation content. */
    protected Metadata currentMetadata;

    /** The list of concepts that the remediation is associated with */
    private List<String> availableConcepts;

    /** Whether to show an error if the remediation has no associated concepts */
    private boolean showNoConceptsError;

    /**
     * Creates a new remedition editor and initializes the interaction behavior of its UI components
     */
    public RemediationEditor() {
        setWidget(uiBinder.createAndBindUi(this));
        
        // If LessonLevel is set to RTA, then the widgets should be hidden.
        if(GatClientUtility.isRtaLessonLevel()){
            addRemediationContentButton.setVisible(false);
        }
        
        //register UI elements in the LTI remediation editor to receive the list of concepts and LTI providers
        conceptsTableDataProvider.addDataDisplay(addRemediationDialog.getReferenceEditor().getMediaPanel().getConceptsTable());
        contentLtiProvidersDataProvider.createChild(addRemediationDialog.getReferenceEditor().getMediaPanel().getCourseLtiProviderList());

        excludeRuleExampleContentCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                
                remediation.setExcludeRuleExampleContent(newValue.getValue() ? generated.course.BooleanEnum.TRUE : generated.course.BooleanEnum.FALSE);
                
                // refresh remediation phase table if shown
                logger.info("calling refresh content files for excluding rule-example remediation checkbox");
                refreshContentFiles();
            }
        });
        
        remediationRefreshButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                refreshContentFiles();
            }
        });
        
        addRemediationContentButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(GatClientUtility.isReadOnly()){
                    
                    WarningDialog.error("Read only", "Remediation content cannot be added in Read-Only mode.");
                    
                    return;
                }            
                
                currentMetadata = new Metadata();
                generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
                presentAt.setRemediationOnly(generated.metadata.BooleanEnum.TRUE);
                currentMetadata.setPresentAt(presentAt);
                
                validateCurrentRemediationContent();
                
                addRemediationDialog.getReferenceEditor().edit(currentMetadata);
                addRemediationDialog.center();
                
                populateAddFileDialogConcepts();
            }
        });
        
        addRemediationDialog.getAddButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                boolean errorOccurred = false;
                boolean enabled = addRemediationDialog.getAddButtonEnabled().isEnabled();
                
                String courseFolderPath = GatClientUtility.getBaseCourseFolderPath();
                
                if(currentMetadata != null && enabled){
                    
                    Serializable content = currentMetadata.getContent();
                    if(content instanceof generated.metadata.Metadata.Simple &&
                            ((generated.metadata.Metadata.Simple)content).getValue() != null && 
                            !((generated.metadata.Metadata.Simple)content).getValue().isEmpty()){
                        
                        String currentContentFileName = ((generated.metadata.Metadata.Simple)content).getValue();
                        
                        //using the file name (including file extension) when naming the metadata XML file in order
                        //to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String metadataFileName = courseFolderPath + "/" + contentFileName + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        Notify.notify("<html>Content for <b>'" + currentContentFileName + "'</b> has been successfully uploaded to the server.");
                        
                        generateMetadataFile(metadataFileName, currentContentFileName);
                        
                    } else if(content instanceof generated.metadata.Metadata.URL &&
                            ((generated.metadata.Metadata.URL)content).getValue() != null && 
                            !((generated.metadata.Metadata.URL)content).getValue().isEmpty()){
                        
                        String currentUrl = ((generated.metadata.Metadata.URL)content).getValue();
                        
                        //escape some characters to make a valid Windows file name
                        //Notes:
                        // 1) remove slashes to make a valid Nuxeo file name 
                        //    (otherwise Nuxeo returns 400 response code for getDocumentEntityByName)
                        // 2) remove equals because gat.client.util.PlaceParamParser uses equals sign as delimeter and that logic provides
                        //    a null metadata file relative path value which makes its way through the 
                        //    MetadataPlace->MetadataActivity->LockMetadat->FileTreeModel logic resulting in an illegal arg exception.
                        // 3) only use the leading 30 characters of the URL to prevent long filenames in the file system
                        //    
                        String escapedUrl = currentUrl;

                        escapedUrl = escapedUrl.replace("/", "");
                        escapedUrl = escapedUrl.replace("\\", "");
                        escapedUrl = escapedUrl.replace("\"", "");
                        escapedUrl = escapedUrl.replace(":", ""); //%3A
                        escapedUrl = escapedUrl.replace("*", ""); //%2A
                        escapedUrl = escapedUrl.replace("<", ""); //%3C
                        escapedUrl = escapedUrl.replace(">", ""); //%3E
                        escapedUrl = escapedUrl.replace("|", ""); //%7C
                        escapedUrl = escapedUrl.replace("?", ""); //%3F
                        escapedUrl = escapedUrl.replace("=", "");
                        
                        if(escapedUrl.length() > 30){
                            escapedUrl = escapedUrl.substring(0, 29) + "...";
                        }
                        
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");                       
                        String metadataFileName = courseFolderPath + "/" + escapedUrl + "_"+format.format(date) + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        Notify.notify("<html>Web address content for <b>'" + currentUrl + "'</b> has been "
                                + "successfully uploaded to the server.");
                        
                        generateMetadataFile(metadataFileName, currentUrl);
                        
                    } else if(addRemediationDialog.getReferenceEditor().getLessonMaterial() != null){
                        
                        //define a unique name for the lesson material file being generated
                        //(Nick: This is based off the same naming convention used in
                        //GatClientUtility.createModalDialogUrlWithParams(String, String, String, HashMap<String, String>)).
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "MediaContent_" + format.format(date);
                        
                        //using the file name (including file extension) when naming the metadata XML file in order
                        //to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = courseFolderPath + "/" + contentFileName;
                        
                        String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        if(content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)){
                            content = new generated.metadata.Metadata.LessonMaterial();
                            currentMetadata.setContent(content);
                        }
                        
                        ((generated.metadata.Metadata.LessonMaterial)currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);
                        
                        generateLessonMaterialReferenceFile(lessonMaterialFilePath, addRemediationDialog.getReferenceEditor().getLessonMaterial());
                        generateMetadataFile(metadataFilePath, currentContentFileName);  
                        
                    } else if(addRemediationDialog.getReferenceEditor().getConversationTree() != null){
                        
                        // define a unique name for the metadata file being generated
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "ConversationTree_" + format.format(date);
                        
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = courseFolderPath + "/" + contentFileName;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        if(content == null || !(content instanceof generated.metadata.Metadata.Simple)){
                            content = new generated.metadata.Metadata.Simple();
                            currentMetadata.setContent(content);
                        }
                        
                        ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).setValue(addRemediationDialog.getReferenceEditor().getConversationTree().getName());
                        
                        generateMetadataFile(metadataFilePath, addRemediationDialog.getReferenceEditor().getConversationTree().getName());
                        
                    } else if(addRemediationDialog.getReferenceEditor().getQuestionExport() != null){
                        
                        //define a unique name for the lesson material file being generated
                        //(Nick: This is based off the same naming convention used in
                        //GatClientUtility.createModalDialogUrlWithParams(String, String, String, HashMap<String, String>)).
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "QuestionContent_" + format.format(date);
                        
                        //using the file name (including file extension) when naming the metadata XML file in order
                        //to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = courseFolderPath + "/" + contentFileName;
                        
                        String exportFilePath = base + FileUtil.QUESTION_EXPORT_SUFFIX;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        if(content == null || !(content instanceof generated.metadata.Metadata.Simple)){
                            content = new generated.metadata.Metadata.Simple();
                            currentMetadata.setContent(content);
                        }
                        
                        ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).setValue(contentFileName + FileUtil.QUESTION_EXPORT_SUFFIX);
                        
                        generateQuestionExportReferenceFile(exportFilePath, addRemediationDialog.getReferenceEditor().getQuestionExport());
                        generateMetadataFile(metadataFilePath, currentContentFileName);
                        
                    } else if (addRemediationDialog.getReferenceEditor().getTrainingApp() != null) {
                        
                        //save the authored interactive content to a metadata file and save its training application
                        //to a training application reference file
                        TrainingApplication trainingApp = addRemediationDialog.getReferenceEditor().getTrainingApp();
                        
                        String dkfFilePath = trainingApp.getDkfRef().getFile();
                        int beginIndex = dkfFilePath.lastIndexOf("/") + 1;
                        int lastIndex = dkfFilePath.lastIndexOf(AbstractSchemaHandler.DKF_FILE_EXTENSION);
                        String dkfFileName = dkfFilePath.substring(beginIndex, lastIndex);

                        String base = courseFolderPath + "/" + dkfFileName;
                        String trainingAppFilePath = base + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                        if (trainingApp.getTransitionName() == null
                                || trainingApp.getTransitionName().isEmpty()) {
                            trainingApp.setTransitionName(base);
                        }

                        if (content == null || !(content instanceof generated.metadata.Metadata.TrainingApp)) {
                            content = new generated.metadata.Metadata.TrainingApp();
                            currentMetadata.setContent(content);
                        }

                        ((generated.metadata.Metadata.TrainingApp) currentMetadata.getContent()).setValue(dkfFileName + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);

                        if (currentMetadata.getDisplayName() == null) {
                            currentMetadata.setDisplayName(trainingApp.getTransitionName());
                        }

                        generateTrainingAppReferenceFile(trainingAppFilePath, trainingApp);
                        generateMetadataFile(metadataFilePath, dkfFilePath); 
                        
                    } else if (addRemediationDialog.getReferenceEditor().getInteractiveLessonMaterial() != null) {
                        // adding a new interactive remediation lesson material. This is entered when the remediation editor is loading a interactive lesson material content.

                        String name = null;

                        LessonMaterial lm = addRemediationDialog.getReferenceEditor().getInteractiveLessonMaterial();
                        if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {
                            for (generated.course.Media media : lm.getLessonMaterialList().getMedia()) {
                                
                                if (media.getMediaTypeProperties() instanceof LtiProperties) {
                                    LtiProperties properties = (LtiProperties) media.getMediaTypeProperties();
                                    name = media.getName();
                                    if (properties.getLtiConcepts() == null) {
                                        properties.setLtiConcepts(new LtiConcepts());
                                    }

                                    properties.getLtiConcepts().getConcepts().clear();
                                    if (currentMetadata != null && currentMetadata.getConcepts() != null) {
                                        for (generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()) {
                                            properties.getLtiConcepts().getConcepts().add(concept.getName());
                                        }
                                    }
                                    
                                    // there should only be 1 LTI properties media
                                    break;
                                }
                            }
                        }

                        if (currentMetadata.getDisplayName() == null) {
                            currentMetadata.setDisplayName(name);
                        }

                        // define a unique name for the lesson material file being generated (Nick: This
                        // is based off the same naming convention used in
                        // GatClientUtility.createModalDialogUrlWithParams(String, String, String,
                        // HashMap<String, String>)).
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "MediaContent_" + format.format(date);

                        // using the file name (including file extension) when naming the metadata XML
                        // file in order to support uploading two content files named the same but with
                        // different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = courseFolderPath + "/" + contentFileName;

                        String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                        if (content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)) {
                            content = new generated.metadata.Metadata.LessonMaterial();
                            currentMetadata.setContent(content);
                        }

                        ((generated.metadata.Metadata.LessonMaterial) currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);

                        generateLessonMaterialReferenceFile(lessonMaterialFilePath, lm.getLessonMaterialList());
                        generateMetadataFile(metadataFilePath, currentContentFileName);
                        
                    } else {
                        errorOccurred = true;
                    }
                    
                } else {
                    errorOccurred = true;
                }
                
                if(errorOccurred && enabled){
                    WarningDialog.warning("Failed to add content", "Could not generate metadata file. An error occurred "
                            + "while getting the file name needed to generate the files.");
                }
            }
        });
        
        repeatRuleBox.setValue(DO_NOT_REPEAT);
        repeatRuleBox.setAcceptableValues(Arrays.asList(
                DO_NOT_REPEAT, 
                REPEAT_INDEFINITELY, 
                REPEAT_UP_TO
        ));
        
        repeatRuleBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(remediation != null) {
                    switch(event.getValue()) {
                    
                        case REPEAT_INDEFINITELY:
                            remediation.setLoopUntilPassed(BooleanEnum.TRUE);
                            remediation.setAllowedAttempts(null);
                            allowedAttemptsPanel.setVisible(false);
                            break;
                            
                        case REPEAT_UP_TO:
                            remediation.setLoopUntilPassed(BooleanEnum.TRUE);
                            if(repeatAllowedAttempts.getValue() != null) {
                                remediation.setAllowedAttempts(BigInteger.valueOf(repeatAllowedAttempts.getValue()));
                            } else {
                                remediation.setAllowedAttempts(BigInteger.valueOf(1));
                                repeatAllowedAttempts.setValue(1);
                            }
                            allowedAttemptsPanel.setVisible(true);
                            break;
                            
                        default:
                            remediation.setLoopUntilPassed(null);
                            remediation.setAllowedAttempts(null);
                            allowedAttemptsPanel.setVisible(false);
                    }
                }
            }
        });
        
        repeatAllowedAttempts.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                    
                if(BooleanEnum.TRUE.equals(remediation.getLoopUntilPassed())){
                    remediation.setAllowedAttempts(BigInteger.valueOf(event.getValue()));
                    
                } else {
                    remediation.setAllowedAttempts(null);
                }
            }
        });
        
        addRemediationDialog.getConceptsTable().setSelectionModel(contentConceptsTableSelectionModel);
        contentConceptsTableDataProvider.addDataDisplay(addRemediationDialog.getConceptsTable());
        
        contentConceptsTableSelectionModel.addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                
                logger.info("contentConceptsTableSelectionModel.onSelectionChange");
                refreshContentAttributesTable();
            }
        });
        
        addRemediationDialog.setConceptSelectionColumnFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {

            @Override
            public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {

                if(currentMetadata != null){
                    
                    if(currentMetadata.getConcepts() == null){
                        currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
                    }
                    
                    generated.metadata.Concept concept = null;
                    
                    for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
                        
                        if(existingConcept.getName() != null && existingConcept.getName().equals(candidate.getConceptName())){
                            
                            concept = existingConcept;
                        }
                    }
                    
                    if(hasBeenSelected){
                        
                        if(concept == null){
                            
                            concept = new generated.metadata.Concept();
                            
                            updateActivityType(concept);
                            
                            concept.setName(candidate.getConceptName());
                            
                            logger.info("setRemediationConceptSelectionColumnFieldUpdater: Adding concept "+concept.getName()+" to currentMetadata");
                            candidate.setChosen(currentMetadata.getConcepts().getConcept().add(concept));
                            
                        } else {
                            candidate.setChosen(true);
                        }
                        
                    } else {
                        
                        if(concept != null){
                            logger.info("setRemediationConceptSelectionColumnFieldUpdater: Removing concept "+concept.getName()+" from currentMetadata");
                            candidate.setChosen(!currentMetadata.getConcepts().getConcept().remove(concept));
                            
                        } else {
                            candidate.setChosen(false);
                        }
                    }
                    
                    populateAddFileDialogConcepts();
                    
                    if(hasBeenSelected && concept != null){                 
                        selectContentConceptName(concept.getName());
                    }
                    
                    addRemediationDialog.conceptSelected(candidate.getConceptName(), candidate.isChosen());
                    
                    // now that the currentMetadata has possibly been changed, 
                    // recheck if the attributes table choices should be shown
                    refreshContentAttributesTable();
                    
                    validateCurrentRemediationContent();
                }
            }
        });  
        
        addRemediationDialog.getReferenceEditor().setOnChangeCommand(new Command() {
            
            @Override
            public void execute() {
                
                if(currentMetadata != null && currentMetadata.getConcepts() != null){
                    
                    //if concepts have been authored, we need to reset their activity data in case the user switched
                    //to a content type that uses a different activity type
                    
                    for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
                        updateActivityType(concept);
                    }
                }
                
                populateAddFileDialogConcepts();
                
                validateCurrentRemediationContent();
            }
        });
        
        contentAttributesTableDataProvider.addDataDisplay(addRemediationDialog.getAttributesTable());
        
        addRemediationDialog.setAttributeSelectionColumnFieldUpdater(new FieldUpdater<CandidateMetadataAttribute, Boolean>() {

            @Override
            public void update(int index, CandidateMetadataAttribute candidate, Boolean hasBeenSelected) {
                
                if(candidate.getParentConcept().getActivityType() == null){
                    candidate.getParentConcept().setActivityType(new ActivityType());
                }
                
                Serializable parentConceptActivity = candidate.getParentConcept().getActivityType().getType();
                generated.metadata.Attributes attributes = null;
                if(parentConceptActivity instanceof generated.metadata.ActivityType.Passive){                   
                    
                    if(((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes() == null){
                        attributes = new generated.metadata.Attributes(); 
                        ((generated.metadata.ActivityType.Passive)parentConceptActivity).setAttributes(attributes);
                    }else{
                        attributes = ((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes();
                    }
                }else{
                    //either null, constructive or active - set to passive
                    candidate.getParentConcept().setActivityType(new generated.metadata.ActivityType());
                    generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
                    candidate.getParentConcept().getActivityType().setType(passive);
                    attributes = new generated.metadata.Attributes();
                    passive.setAttributes(attributes);
                }
                
                Attribute attribute = null;
                
                for(Attribute existingAttribute : attributes.getAttribute()){
                    
                    if(existingAttribute.getValue() != null && candidate.getAttribute().getName().equals(existingAttribute.getValue())){
                        attribute = existingAttribute;
                    }
                }               
                
                if(hasBeenSelected){
                    
                    if(attribute == null){
                        
                        attribute = new Attribute();
                        attribute.setValue(candidate.getAttribute().getName());
                        
                        candidate.setChosen(attributes.getAttribute().add(attribute));
                        
                    } else {
                        candidate.setChosen(true);
                    }
                    
                } else {
                    
                    if(attribute != null){                      
                        candidate.setChosen(!attributes.getAttribute().remove(attribute));
                    
                    } else {
                        candidate.setChosen(false);
                    }
                }
                
                validateCurrentRemediationContent();
            }
        });
        
        setReadOnly(GatClientUtility.isReadOnly());
    }
    
    /**
     * Sets the visual loading state of the remediation file list
     * 
     * @param loadingContentList whether remediation content files are being loaded
     */
    private void setRemediationFilePanelLoading(boolean loadingContentList){
        
        if(loadingContentList){
            remediationFilesList.clear();
            remediationFilesLoadingIcon.startLoading();
        }else{
            remediationFilesLoadingIcon.stopLoading();
        }
           
        remediationFilesLoadingPanel.setVisible(loadingContentList);
    }
    
    /**
     * Sets whether this editor's UI components should be set to read-only
     * 
     * @param isReadOnly whether editable UI components should allow editing
     */
    public void setReadOnly(boolean isReadOnly) {
        excludeRuleExampleContentCheckBox.setEnabled(!isReadOnly);
        addRemediationContentButton.setVisible(!isReadOnly);
        repeatAllowedAttempts.setEnabled(!isReadOnly);
        repeatRuleBox.setEnabled(!isReadOnly);
    }
    
    /**
     * Updates the displayed remediation content files using the provided metadata
     * 
     * @param metadataFiles the metadata pointing to the remediation content files to display. Can be null,
     * if no remediation content is available.
     */
    private void setRemediationFiles(QuadrantResultSet metadataFiles){
        
        remediationFilesList.clear();
        
        if(metadataFiles != null){
        
            if(metadataFiles.getMetadataRefs() != null && !metadataFiles.getMetadataRefs().isEmpty()){
                
                for(final MetadataWrapper metadataWrapper : metadataFiles.getMetadataRefs().values()){
                    
                    if(metadataWrapper.hasExtraneousConcept()){
                        continue;
                    }
                    
                    Command deleteCommand = null;
                    if(!metadataWrapper.hasExtraneousConcept() && metadataWrapper.isRemediationOnly()){
                        deleteCommand = new Command() {
                            
                            @Override
                            public void execute() {
                                
                                if (!GatClientUtility.isReadOnly()) {
                                    deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName());
                                }
                                
                            }
                        };
                    }
                    
                    ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
                    
                    remediationFilesList.add(contentWidget);
                }
            } else {
                remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
            }
            
        } else {
            remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
        }

    }

    /**
     * Refreshes the list of remediation content files to match the current state within the course folder
     */
    private void refreshContentFiles() {
        
        //get set of course concepts not selected for the Adaptive courseflow course object
        List<String> courseConcepts = availableConcepts;
        
        //when the course object has no course concepts selected, don't query the server for metadata cause it will
        //return an empty list
        if(courseConcepts == null || courseConcepts.isEmpty()){
            setRemediationFilePanelLoading(false);
            setRemediationFiles(null);
            return;
        }
        
        logger.info("Building getmerrillquadrantfiles action.");
        
        GetMerrillQuadrantFiles action = 
                new GetMerrillQuadrantFiles(GatClientUtility.getUserName(), GatClientUtility.getBaseCourseFolderPath());
              
        setRemediationFilePanelLoading(true);
        
        QuadrantRequest remediationRequest = 
                new QuadrantRequest(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL, true, courseConcepts, new ArrayList<String>());

        if(remediation != null){
            remediationRequest.setExcludeRuleExampleContent(remediation.getExcludeRuleExampleContent() == generated.course.BooleanEnum.TRUE);
        }
        
        action.addRequest(remediationRequest);

        if(!action.getRequests().isEmpty()){
            logger.info("Requesting getmerrillquadrantfiles action.\n"+action);
            
            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetMerrillQuadrantFilesResult>() {
    
                @Override
                public void onFailure(Throwable thrown) {
                    
                    logger.severe("Received failure when trying to retrieve the metadata files : "+thrown.getMessage());
                    
                    List<String> stackTrace = new ArrayList<String>();
                    
                    if(thrown.getStackTrace() != null){
                        for(StackTraceElement e : thrown.getStackTrace()){
                            stackTrace.add(e.toString());
                        }
                    }
                    
                    setRemediationFilePanelLoading(false);
                    
                    ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
                            "An error occurred while getting the list of Rule content files.",
                            thrown.toString(), stackTrace);
                    
                    detailsDialog.setDialogTitle("Failed to Get Rule Content Files");
                    detailsDialog.center();
                }
    
                @Override
                public void onSuccess(GetMerrillQuadrantFilesResult result) {
                    
                    logger.info("Received response from server with merrill quadrant metadata search results.");
                    
                    setRemediationFilePanelLoading(false);
                    
                    if(result.isSuccess()){ 
                        setRemediationFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL));   
                    
                    } else {
                        
                        if(result.getErrorDetails() != null){
                            
                            ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
                                    result.getErrorMsg(),
                                    result.getErrorDetails(), 
                                    result.getErrorStackTrace());
                            
                            detailsDialog.setDialogTitle("Failed to Get Remediation Content Files");
                            detailsDialog.center();
                            
                        } else {
                            
                            WarningDialog.alert("Failed to get Remediation files", result.getErrorMsg());
                        }
                    }
                }
            });
        }
        
    }
    
    /**
     * Deletes the metadata file with the given path and its associated content file
     * 
     * @param metadataFilePath the path to the metadata file to delete. Cannot be null.
     * @param contentFilePath the path to the associated content file to delete. Can be null.
     */
    private void deleteMetadata(final String metadataFilePath, String contentFilePath){
        
        String objectName = "UNKNOWN";
        
        if(contentFilePath != null){
            objectName = contentFilePath;
        }
        
        OkayCancelDialog.show(
                "Delete Metadata?", 
                "Are you sure you want to delete the metadata for " + (
                        objectName != null 
                                ? "<b>" + objectName + "</b>" 
                                : "this content file"
                ) + "?", 
                "Yes, delete this metadata", 
                new OkayCancelCallback() {
                    
                    @Override
                    public void okay() {
                        
                        DeleteMetadata action = new DeleteMetadata(
                                GatClientUtility.getUserName(), 
                                GatClientUtility.getBrowserSessionKey(),
                                GatClientUtility.getBaseCourseFolderPath(), 
                                metadataFilePath, 
                                true
                        );
                        
                        BsLoadingDialogBox.display("Deleting Metadata", "Please wait while GIFT deletes this metadata.");
                        
                        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GenericGatServiceResult<Void>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                
                                BsLoadingDialogBox.remove();
                                
                                DetailedException exception = new DetailedException(
                                        "GIFT was unable to delete this metadata. An unexpected error occurred "
                                        + "during the deletion.", 
                                        caught.toString(), 
                                        caught
                                );
                                
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        exception.getReason(), 
                                        exception.getDetails(), 
                                        exception.getErrorStackTrace()
                                );
                                
                                dialog.center();
                            }

                            @Override
                            public void onSuccess(GenericGatServiceResult<Void> result) {
                                
                                BsLoadingDialogBox.remove();
                                
                                if(result.getResponse().getWasSuccessful()){
                                    
                                    //instead of making a call to the server to get the list again,
                                    //just remove it from the UI's list
                                    
                                    boolean removed = false;
                                        
                                    removed = removeContentWidget(remediationFilesList, metadataFilePath);
                                    
                                    if(remediationFilesList.getWidgetCount() == 0){
                                        remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
                                    }
                                    
                                    //fail safe just in case... update all content lists
                                    if(!removed){
                                        refreshContentFiles();
                                    }
                                    
                                } else {
                                    
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                            result.getResponse().getException().getReason(), 
                                            result.getResponse().getException().getDetails(), 
                                            result.getResponse().getException().getErrorStackTrace()
                                    );
                                    
                                    dialog.center();
                                }
                            }
                        });
                                                        
                    }
                    
                    @Override
                    public void cancel() {
                        //Nothing to do
                    }
        });
    }
    
    /**
     * Removes the given metadata file's displayed representation in the remediation content file list
     * 
     * @param contentListPanel the panel to remove the entry that is mapped to the provided metadata file path
     * @param metadataFilePath the course folder path to a metadata file to remove
     * @return true if an entry was removed, false otherwise.
     */
    private boolean removeContentWidget(FlowPanel contentListPanel, String metadataFilePath){
        
        if(contentListPanel == null){
            return false;
        }
        
        boolean removed = false;
        int removeWidgetIndex = -1;
        for(int index = 0; index < contentListPanel.getWidgetCount(); index++){
            
            Widget widget = contentListPanel.getWidget(index);
            if(widget instanceof ContentFileWidget){
                
                ContentFileWidget contentFileWidget = (ContentFileWidget)widget;
                if(contentFileWidget.getMetadataFilePath().equals(metadataFilePath)){
                    removeWidgetIndex = index;
                    break;
                }
            }
        }
        
        if(removeWidgetIndex != -1){
            contentListPanel.remove(removeWidgetIndex);
            removed = true;
        }
        
        return removed;
    }

    @Override
    protected void editObject(Remediation courseObject) {
        
        //reset this widget's internal data objects
        this.remediation = courseObject;
        
        //reset the editable UI components
        excludeRuleExampleContentCheckBox.setValue(false);
        repeatRuleBox.setValue(DO_NOT_REPEAT);
        repeatAllowedAttempts.setValue(0);
        allowedAttemptsPanel.setVisible(false);
        
        // populate the concepts for the LTI remediation editor
        conceptsTableDataProvider.getList().clear();   
        List<String> courseConcepts = availableConcepts;
        if(courseConcepts != null) {
            
            noCourseConceptsLabel.setVisible(courseConcepts.isEmpty() && showNoConceptsError);
            
            for(String concept : courseConcepts) {
                conceptsTableDataProvider.getList().add(new CandidateConcept(concept, true));
            }
            
        } else {
            noCourseConceptsLabel.setVisible(showNoConceptsError);
        }
        conceptsTableDataProvider.refresh();
        
        // populate the LTI providers' list
        contentLtiProvidersDataProvider.getList().clear();
        List<LtiProvider> ltiProviderIds = GatClientUtility.getCourseLtiProviders();
        if (ltiProviderIds != null) {
            contentLtiProvidersDataProvider.getList().addAll(ltiProviderIds);
        }
        contentLtiProvidersDataProvider.refresh();
        
        //load the remediation data
        excludeRuleExampleContentCheckBox.setValue(remediation.getExcludeRuleExampleContent() == generated.course.BooleanEnum.TRUE);
        if(BooleanEnum.TRUE.equals(remediation.getLoopUntilPassed())){
            
            if(remediation.getAllowedAttempts() != null) {
                repeatRuleBox.setValue(REPEAT_UP_TO);
                allowedAttemptsPanel.setVisible(true);
                repeatAllowedAttempts.setValue(remediation.getAllowedAttempts().intValue());
                
            } else {
                repeatRuleBox.setValue(REPEAT_INDEFINITELY);
            }
        }
        
        refreshContentFiles();
    }
    
    /**
     * Validates the current remediation content and indicates to the user what fields still need to be filled
     */
    private void validateCurrentRemediationContent(){
    
        StringBuilder errorMsg = new StringBuilder();
        
        String contentValidationErrors = addRemediationDialog.getReferenceEditor().getValidationErrors();
        
        if(contentValidationErrors != null){
            errorMsg.append(contentValidationErrors);
        }
        
        if(currentMetadata != null){
            
            if(currentMetadata.getConcepts() == null
                    || currentMetadata.getConcepts().getConcept() == null
                    || currentMetadata.getConcepts().getConcept().isEmpty()){
                
                errorMsg.append("")
                        .append("<li>")
                        .append("At least one metadata concept must be chosen.")
                        .append("</li>");
                
            } else if(addRemediationDialog.getReferenceEditor().getQuestionExport() == null &&
                    addRemediationDialog.getReferenceEditor().getConversationTree() == null){
                
                //check the attributes for any content type other than
                // - Highlight or Summarize Passage
                // - Conversation tree
                
                for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
                    
                    if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                        
                        generated.metadata.Attributes attributes = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes();
                        if(attributes == null 
                            || attributes.getAttribute() == null
                            || attributes.getAttribute().isEmpty()){
                        
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                                    .append("</li>");
                        }
                        
                    }else if(currentMetadata.getPresentAt() != null && currentMetadata.getPresentAt().getMerrillQuadrant() != null){
                        //which phase to present this content has been set but the specific concept activity has not.
                        //currently the rule/example/practice phases are passive only and passive requires metadata attributes.  The remediation
                        //phase doesn't use the presentAt value but remediation only attribute instead.

                        errorMsg.append("")
                            .append("<li>")
                            .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                            .append("</li>");
                    }
                }
                
            }
        }
            
        if(!errorMsg.toString().isEmpty()){
            
            addRemediationDialog.getValidationErrorText().setHTML(""
                    + "<div style='width: 100%; color: red; font-weight: bold;'> "
                    +       "The following problem(s) have been detected in this content:"
                    +       "<ul>"
                    +           errorMsg.toString() 
                    +       "</ul>"
                    +       "You must correct these problems before you can add your content."
                    + "</div>"
                    + "<hr style='border-top-style: solid; border-top-width: 1px; border-top-color: #AAAAAA;'/>"
            );
            
            addRemediationDialog.getAddButtonEnabled().setEnabled(false);
        
        } else {
            
            addRemediationDialog.getValidationErrorText().setHTML("");
            
            addRemediationDialog.getAddButtonEnabled().setEnabled(true);
        }
    }
    
    /**
     * Populates the concepts in the dialog used to add remediation content
     */
    private void populateAddFileDialogConcepts(){
        
        logger.info("Populating Add metadata concept dialog with course concepts.");
        
        // clear the 'Concepts:' table in the 'Add Content' dialog
        contentConceptsTableDataProvider.getList().clear();
        contentConceptsTableDataProvider.refresh();

        //populate the 'Concepts:' table
        List<String> conceptNames = availableConcepts;
        
        if(conceptNames != null){
                
            List<String> existingConceptNames = new ArrayList<String>();
            
            if(currentMetadata.getConcepts() == null){
                currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
            }
            
            for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
                
                if(concept.getName() != null){
                    existingConceptNames.add(concept.getName());
                }
            }
            
            for(String conceptName : conceptNames){
                
                if(existingConceptNames.contains(conceptName)){
                    contentConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, true));
                    
                } else {                
                    contentConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, false));
                }
            }
            
            contentConceptsTableDataProvider.refresh();
        }
        
        contentAttributesTableDataProvider.getList().clear();
        contentAttributesTableDataProvider.refresh();
    }
    
    /**
     * Generates a metadata file for the given file name
     * 
     * @param filename the name of the file to generate a metadata file for. Cannot be null.
     * @param contentFileName the name of the content file being added to the metadata (e.g. *.ppsm file). Cannot be null.
     */
    private void generateMetadataFile(final String filename, final String contentFilename) {
        
        if(currentMetadata != null){

            AsyncCallback<GenerateMetadataFileResult> callback = new AsyncCallback<GenerateMetadataFileResult>(){
    
                @Override
                public void onFailure(Throwable e) {
                    BsLoadingDialogBox.remove();
                    WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a metadata file for " + filename);
                }
    
                @Override
                public void onSuccess(GenerateMetadataFileResult result) {
                    BsLoadingDialogBox.remove();
                    if(result.isSuccess()){
                        
                        Notify.notify("<html>A metadata file has been created for you based on the attributes you selected.</html>");
                        
                        addRemediationDialog.hide();
                        
                        //get the phase where the content was added too (not necessarily this course object depending on the concepts selected)
                        MerrillQuadrantEnum phase = MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL;
                                                        
                        MetadataWrapper metadataWrapper = result.getMetadataWrapper();
                        if(metadataWrapper != null && phase != null && phase != MerrillQuadrantEnum.PRACTICE){
                            
                            logger.info("Added content for the "+phase+" phase on the server.  Updating content table on the client next.");
                            
                            //check if this course object's content list should be updated                                
                            if(currentMetadata.getConcepts() != null && !currentMetadata.getConcepts().getConcept().isEmpty()){
                                //created metadata has concepts, need to compare against this course objects selected concepts
                                
                                boolean foundConcept = false;
                                for(generated.metadata.Concept metadataConcept : currentMetadata.getConcepts().getConcept()){
                                    
                                    foundConcept = false;
                                    
                                    //the selected concepts for this course object
                                    for(String courseObjectConcept : availableConcepts){
                                        
                                        if(metadataConcept.getName() != null && metadataConcept.getName().equalsIgnoreCase(courseObjectConcept)){
                                            //found concept match
                                            foundConcept = true;
                                            break;
                                        }
                                    }
                                    
                                    if(!foundConcept){
                                        break;
                                    }
                                }
                                
                                //only update the client side list if the metadata that was just created is for the concepts 
                                //taught by this course object
                                if(foundConcept){
                                    addContentFile(metadataWrapper);
                                }
                            }

                            
                        }else if(phase != null){
                            //for practice and as a fail safe... update the specific phase's content list with a server call
                            logger.info("calling refresh content files for generated metadata file");
                            refreshContentFiles();
                        }
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                        dialog.setText("Error");
                        dialog.center();
                    }
                }
                
            };
            
            BsLoadingDialogBox.display("Generating Metadata", "Writing metadata file to the course.");
            
            GenerateMetadataFile action = new GenerateMetadataFile(currentMetadata, filename);
            action.setUserName(GatClientUtility.getUserName());
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        
        } else {
            WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying metadata file for the specified file.");
        }
    }
    
    /**
     * Adds a UI element to the display list of remediation content files to represent the given metadata
     * 
     * @param metadataWrapper
     */
    private void addContentFile(final MetadataWrapper metadataWrapper){
        
        if(metadataWrapper == null){
            return;
        }else if(metadataWrapper.hasExtraneousConcept()){
            return;
        }
        
        logger.info("Adding content to the Remediation list panel.");
        
        Command deleteCommand = new Command() {
                
            @Override
            public void execute() {
                
                if (!GatClientUtility.isReadOnly()) {
                    deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName());
                }
                
            }
        };
        
        ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
           
        //handle removing the 'no content' value in the list
        if(remediationFilesList.getWidgetCount() == 1 && remediationFilesList.getWidget(0) instanceof HTML){
            remediationFilesList.remove(0);
        }
        
        remediationFilesList.insert(contentWidget, 0);
    }
    
    /**
     * Generates a lesson material reference file for the given file name
     * 
     * @param filename the name of the file to generate a lesson material reference file for. Cannot be null.
     * @param the list of lesson material to be written to the file. Cannot be null.
     */
    private void generateLessonMaterialReferenceFile(final String filename, final LessonMaterialList lessonMaterial) {
        
        if(currentMetadata != null){

            AsyncCallback<GenerateLessonMaterialReferenceFileResult> callback = new AsyncCallback<GenerateLessonMaterialReferenceFileResult>(){
    
                @Override
                public void onFailure(Throwable e) {
                    
                    WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a lesson material reference file for " + filename);
                }
    
                @Override
                public void onSuccess(GenerateLessonMaterialReferenceFileResult result) {
                    
                    if(result.isSuccess()){
                        
                        //figure out what type of lesson material was generated
                        String type = null;
                        
                        if(lessonMaterial != null
                                && !lessonMaterial.getMedia().isEmpty()){
                            
                            Serializable properties = lessonMaterial.getMedia().get(0);
                            
                            if(properties != null){
                                
                                if(properties instanceof SlideShowProperties){
                                    type = "slide show";
                                    
                                } else if(properties instanceof PDFProperties){
                                    type = "PDF";
                                
                                } else if(properties instanceof ImageProperties){
                                    type = "image";
                                
                                } else if(properties instanceof YoutubeVideoProperties){
                                    type = "YouTube video";
                                    
                                } else if (properties instanceof LtiProperties) {
                                    type = "LTI provider";
                                }
                            }
                        }
                        
                        if(type != null){
                            
                            Notify.notify("<html>Your " + type + " content has been successfully uploaded to the server.</html>");
                            
                        } else {
                            
                            Notify.notify("<html>Your content has been successfully uploaded to the server.</html>");
                        }
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                        dialog.setText("Error");
                        dialog.center();
                    }
                }
                
            };
            
            String userName = GatClientUtility.getUserName();
            
            GenerateLessonMaterialReferenceFile action = new GenerateLessonMaterialReferenceFile();
            action.setLessonMaterialList(lessonMaterial);
            action.setTargetFilename(filename);
            action.setUserName(userName);
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        
        } else {
            WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying training app reference file for the specified file.");
        }
    }
    
    /**
     * Generates a training application reference file for the given file name
     * 
     * @param filename the name of the file to generate a training application reference file for. If null, this method will do nothing.
     * @param app the training application to save to the file. If null, this method will do nothing.
     */
    private void generateTrainingAppReferenceFile(final String filename, TrainingApplication app) {
        
        if(filename == null || app == null) {
            return;
        }
        
        if(currentMetadata != null){

            AsyncCallback<GenerateTrainingAppReferenceFileResult> callback = new AsyncCallback<GenerateTrainingAppReferenceFileResult>(){
    
                @Override
                public void onFailure(Throwable e) {
                    
                    WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a training app reference file for " + filename);
                }
    
                @Override
                public void onSuccess(GenerateTrainingAppReferenceFileResult result) {
                    
                    if(result.isSuccess()){
                        
                        Notify.notify("<html>A reference to your training application has been successfully uploaded to the server.");
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                        dialog.setText("Error");
                        dialog.center();
                    }
                }
                
            };
            
            String userName = GatClientUtility.getUserName();
            
            GenerateTrainingAppReferenceFile action = new GenerateTrainingAppReferenceFile();
            TrainingApplicationWrapper currentTAWrapper = new TrainingApplicationWrapper();
            currentTAWrapper.setTrainingApplication(app);
            action.setTrainingAppWrapper(currentTAWrapper);
            action.setTargetFilename(filename);
            action.setUserName(userName);
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        
        } else {
            WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying training app reference file for the specified file.");
        }
    }
    
    /**
     * Generates a question export reference file for the given file name
     * 
     * @param filename the name of the file to generate a question export reference file for. Cannot be null.
     * @param the question to be written to the question export file. Cannot be null.
     */
    private void generateQuestionExportReferenceFile(final String filename, final AbstractQuestion question) {
        
        if(currentMetadata != null){

            AsyncCallback<GenericGatServiceResult<Void>> callback = new AsyncCallback<GenericGatServiceResult<Void>>(){
    
                @Override
                public void onFailure(Throwable e) {
                    
                    WarningDialog.error("Faile to create file", "An error occurred while communicating with the server to generate a question export reference file for " + filename);
                }
    
                @Override
                public void onSuccess(GenericGatServiceResult<Void> result) {
                    
                    GenericRpcResponse<Void> response = result.getResponse();
                    
                    if(response.getWasSuccessful()){
                        
                        //figure out what type of lesson material was generated
                        String type = null;
                        
                        if(question != null && question.getProperties() != null){
                            
                            if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                                type = "summarize text";
                                
                            } else {
                                type = "highlight text";
                            }
                        }
                        
                        if(type != null){
                            
                            Notify.notify("<html>Your " + type + " content has been successfully uploaded to the server.</html>");
                            
                        } else {
                            
                            Notify.notify("<html>Your content has been successfully uploaded to the server.</html>");
                        }
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                response.getException().getReason(), 
                                response.getException().getDetails(), 
                                response.getException().getErrorStackTrace()
                        );
                        dialog.setText("Error");
                        dialog.center();
                    }
                }
                
            };
            
            String userName = GatClientUtility.getUserName();
            
            GenerateQuestionExportReferenceFile action = new GenerateQuestionExportReferenceFile();
            action.setQuestion(question);
            action.setTargetFilename(filename);
            action.setUserName(userName);
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        
        } else {
            WarningDialog.error("Failed to create file", "An error occurred while generating the specified question export file.");
        }
    }
    
    /**
     * Decides what to show on the content attributes table depending on whether the concept
     * is selected and whether the data model already has attributes selected for that concept.
     */
    private void refreshContentAttributesTable(){
        
        contentAttributesTableDataProvider.getList().clear();
        
        if(contentConceptsTableSelectionModel.getSelectedObject() != null){
        
            String conceptName = contentConceptsTableSelectionModel.getSelectedObject().getConceptName();
            logger.info("Selected concept name is "+conceptName+", current metadata concepts: "+currentMetadata.getConcepts().getConcept());
            generated.metadata.Concept concept = null;
            
            for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
                
                if(existingConcept.getName() != null && existingConcept.getName().equals(conceptName)){                     
                    concept = existingConcept;
                    break;
                }
            }
            
            // when the concept is null it means that the metadata begin built (currentMetadata) does
            // not have the concept as a selected concept
            if(concept != null){
                
                logger.info("Selected concept "+concept.getName());
                
                updateActivityType(concept);
                
                if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive &&
                        ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes() == null){
                    ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).setAttributes(new Attributes());                     
                }
                
                List<MetadataAttributeEnum> existingAttributes = new ArrayList<MetadataAttributeEnum>();
                
                if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    for(Attribute attribute: ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes().getAttribute()){
                    
                        try{
                            
                            if(attribute.getValue() != null){
                                existingAttributes.add(MetadataAttributeEnum.valueOf(attribute.getValue()));
                            }
                            
                        }catch(EnumerationNotFoundException e){
                            logger.log(Level.SEVERE, "Caught exception while getting metadata attributes for a concept.", e);
                        }
                    }
                }
                                        
                //populate collection of possible metadata attributes
                for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
                    contentAttributesTableDataProvider.getList().add(new CandidateMetadataAttribute(concept, attribute, existingAttributes.contains(attribute)));
                }
            
            }
            
        }
        
        contentAttributesTableDataProvider.refresh();
    }
    
    /**
     * Updates the activity type of the given concept based on the current visual state of the editor. 
     * If constructive, active, or interactive content is being authored, then this method will update 
     * the concept to use the Constructive, Active, or Interactive activity type, respectively.
     * 
     * @param concept the concept whose activity type should be updated. If null, this method will do nothing.
     */
    private void updateActivityType(Concept concept) {
        
        if(concept.getActivityType() == null){
            concept.setActivityType(new generated.metadata.ActivityType());
        }
            
        Serializable type = null;
        
        if(currentMetadata.getPresentAt() != null                                   
                && generated.metadata.BooleanEnum.TRUE.equals(currentMetadata.getPresentAt().getRemediationOnly())){
            
            if(addRemediationDialog.getReferenceEditor().getQuestionExport() != null) {
            
                AbstractQuestion question = addRemediationDialog.getReferenceEditor().getQuestionExport();
                
                if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                        && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                    
                    //summarize content should use the Constructive activity type
                    type = new generated.metadata.ActivityType.Constructive();
                    
                } else {
                    
                    //highlight content should use the Active activity type
                    type = new generated.metadata.ActivityType.Active();
                }  
                
            } else if(addRemediationDialog.getReferenceEditor().getConversationTree() != null){
                
                // conversation tree should use the Active activity type
                type = new generated.metadata.ActivityType.Active();
                
            } else if(addRemediationDialog.getReferenceEditor().getTrainingApp() != null){
                
                //training app content should use the Active activity type
                type = new generated.metadata.ActivityType.Interactive();
                
            } else if(addRemediationDialog.getReferenceEditor().getInteractiveLessonMaterial() != null){
                
                //interactive lesson material content should use the Active activity type
                type = new generated.metadata.ActivityType.Interactive();
            }
        }
        
        if(type != null){
            concept.getActivityType().setType(type);
        
        } else {
            concept.getActivityType().setType(new generated.metadata.ActivityType.Passive());
        }
    }

    /**
     * Selects a concept name in the dialog used to add remediation content files
     * 
     * @param name the name of the concept to select. Cannot be null.
     */
    private void selectContentConceptName(String name){
        
        if(name == null){
            throw new IllegalArgumentException("The name of the concept to select cannot be null.");
        }
        
        for(CandidateConcept concept : contentConceptsTableDataProvider.getList()){
            
            if(concept.getConceptName().equals(name)){
                
                contentConceptsTableSelectionModel.setSelected(concept, true);
                break;
            }
        }
    }
    
    /**
     * Sets list of concepts that the remediation is associated with
     * 
     * @param concepts the concepts to associate with the remediation. Can be null.
     * @param showNoConceptsError whether to show an error if the provided concepts are empty
     */
    public void setAvailableConcepts(List<String> concepts, boolean showNoConceptsError) {
        this.availableConcepts = concepts;
        this.showNoConceptsError = showNoConceptsError;
    }
}
