/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ATRemoteSKO;
import generated.dkf.ATRemoteSKO.URL;
import generated.dkf.AutoTutorSKO;
import generated.dkf.ConversationTreeFile;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.LocalSKO;
import generated.dkf.TutorMeParams;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayLaterDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

/**
 * An inline editor used to edit a {@link LearnerAction} within an
 * {@link ItemListEditor}
 *
 * @author tflowers
 *
 */
public class LearnerActionEditor extends ItemEditor<LearnerAction> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionEditor.class.getName());

    /** Combines this java class with the ui.xml */
    private static final LearnerActionEditorUiBinder uiBinder = GWT.create(LearnerActionEditorUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface LearnerActionEditorUiBinder extends UiBinder<Widget, LearnerActionEditor> {
    }

    /**
     * An enum that references all types of Learner Actions that the user can
     * author. Serves a UI friendly facade over the
     * {@link LearnerActionEnumType} enum. Provide display strings for items
     * within the {@link #actionTypeRibbon}
     */
    public enum LearnerActionType {
        /**
         * Represents a
         * {@link LearnerActionEnumType#EXPLOSIVE_HAZARD_SPOT_REPORT}.
         */
        EXPLOSIVE_HAZARD_SPOT_REPORT(
                "Explosive Hazard Spot Report",
                LearnerActionEnumType.EXPLOSIVE_HAZARD_SPOT_REPORT),
        /**
         * Represents a {@link LearnerActionEnumType#NINE_LINE_REPORT}.
         */
        NINE_LINE_REPORT(
                "Nine Line Report",
                LearnerActionEnumType.NINE_LINE_REPORT),
        /**
         * Represents a {@link LearnerActionEnumType#SPOT_REPORT}.
         */
        SPOT_REPORT(
                "Spot Report",
                LearnerActionEnumType.SPOT_REPORT),
        /**
         * Represents a {@link LearnerActionEnumType#RADIO}.
         */
        USE_RADIO(
                "Radio",
                LearnerActionEnumType.RADIO),

        /**
         * Represents a {@link LearnerActionEnumType#START_PACE_COUNT}.
         */
        START_PACE_COUNT(
                "Start Pace Count",
                LearnerActionEnumType.START_PACE_COUNT),

        /**
         * Represents a {@link LearnerActionEnumType#END_PACE_COUNT}.
         */
        END_PACE_COUNT(
                "End Pace Count",
                LearnerActionEnumType.END_PACE_COUNT),
        /**
         * Represents a {@link LearnerActionEnumType#TUTOR_ME} with an
         * {@link AutoTutorSKO} for the
         * {@link LearnerAction#getLearnerActionParams()}.
         */
        AUTO_TUTOR_CONVERSATION(
                "AutoTutor Conversation",
                LearnerActionEnumType.TUTOR_ME),
        /**
         * Represents a {@link LearnerActionEnumType#TUTOR_ME} with an
         * {@link ConversationTreeFile} for the
         * {@link LearnerAction#getLearnerActionParams()}.
         */
        CONVERSATION_TREE(
                "Conversation Tree",
                LearnerActionEnumType.TUTOR_ME),
        
        /**
         * Represents a {@link LearnerActionEnumType#ASSESS_MY_LOCATION}.
         */
        ASSESS_MY_LOCATION(
                "Assess my location",
                LearnerActionEnumType.ASSESS_MY_LOCATION),
        
        /**
         * Represents a {@link LearnerActionEnumType#APPLY_STRATEGY}.
         */
        APPLY_STRATEGY(
                "Apply Strategy",
                LearnerActionEnumType.APPLY_STRATEGY);

        /** The string used as the display name for this learner action type. */
        private final String displayName;

        /**
         * The {@link LearnerActionEnumType} this {@link LearnerActionType} is
         * reresenting.
         */
        private final LearnerActionEnumType actionType;

        /**
         * Constructs an enum with the given {@link #displayName} and
         * {@link #actionType}.
         *
         * @param displayName The {@link String} used when referring to this
         *        type within the UI.
         * @param actionType The {@link LearnerActionEnumType} that this
         *        {@link LearnerActionType} represents.
         */
        private LearnerActionType(String displayName, LearnerActionEnumType actionType) {

            if (displayName == null) {
                throw new IllegalArgumentException("The parameter 'displayName' cannot be null.");
            }

            if (actionType == null) {
                throw new IllegalArgumentException("The parameter 'actionType' cannot be null.");
            }

            this.displayName = displayName;
            this.actionType = actionType;
        }

        /**
         * Determines the {@link LearnerActionType} of a given
         * {@link LearnerAction}.
         *
         * @param action The {@link LearnerAction} for which to determine the
         *        {@link LearnerActionType}. Can't be null
         * @return The {@link LearnerActionType} of the supplied
         *         {@link LearnerAction}. Can be null.
         */
        public static LearnerActionType getTypeFromAction(LearnerAction action) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getTypeFromAction(" + action + ")");
            }

            if (action == null) {
                throw new IllegalArgumentException("The parameter 'action' cannot be null.");
            }

            if (action.getType() == null) {
                return null;
            }

            switch (action.getType()) {
            case EXPLOSIVE_HAZARD_SPOT_REPORT:
                return LearnerActionType.EXPLOSIVE_HAZARD_SPOT_REPORT;
            case NINE_LINE_REPORT:
                return LearnerActionType.NINE_LINE_REPORT;
            case RADIO:
                return LearnerActionType.USE_RADIO;
            case START_PACE_COUNT:
                return LearnerActionType.START_PACE_COUNT;
            case END_PACE_COUNT:
                return LearnerActionType.END_PACE_COUNT;
            case SPOT_REPORT:
                return LearnerActionType.SPOT_REPORT;
            case APPLY_STRATEGY:
                return LearnerActionType.APPLY_STRATEGY;
            case TUTOR_ME:
                Serializable actionParams = action.getLearnerActionParams();
                TutorMeParams learnerActionParams = null;
                if(actionParams instanceof generated.dkf.TutorMeParams){
                    learnerActionParams = (generated.dkf.TutorMeParams)actionParams;                    
                }
                
                if (learnerActionParams == null) {
                    learnerActionParams = new TutorMeParams();
                    action.setLearnerActionParams(learnerActionParams);
                }
    
                Serializable configuration = learnerActionParams.getConfiguration();
                if (configuration == null) {
                    configuration = new ConversationTreeFile();
                    learnerActionParams.setConfiguration(configuration);
                }

                if (configuration instanceof ConversationTreeFile) {
                    return LearnerActionType.CONVERSATION_TREE;
                } else {
                    return LearnerActionType.AUTO_TUTOR_CONVERSATION;
                }

            case ASSESS_MY_LOCATION:
                return LearnerActionType.ASSESS_MY_LOCATION;
            }

            String msg = "The LearnerActionEnumType '" + action + "' does not have an associated icon";
            throw new IllegalArgumentException(msg);
        }

        /**
         * Getter for the {@link #displayName}.
         *
         * @return The value of {@link #displayName}. Can't be null.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Getter for the {@link #actionType}.
         *
         * @return The value of {@link #actionType}. Can't be null.
         */
        public LearnerActionEnumType getActionType() {
            return actionType;
        }
    }

    /** The root panel that toggles between the ribbon and editor */
    @UiField
    protected DeckPanel editorDeckPanel;

    /** The control used to select the type of {@link LearnerAction} */
    @UiField
    protected Ribbon actionTypeRibbon;

    /** The panel that contains the editor shown once a type is selected */
    @UiField
    protected FlowPanel actionEditorPanel;

    /** The text box that is used to change the display name */
    @UiField
    protected TextBox displayNameBox;

    /** The icon that represents the type of {@link LearnerAction} */
    @UiField
    protected SimplePanel typeIcon;

    /** The text that displays the type of {@link LearnerAction} */
    @UiField
    protected HTML typeName;

    /** The editor used to edit the description of the {@link LearnerAction} */
    @UiField
    protected Summernote descriptionBox;

    /** Control used to select a conversation tree */
    @UiField
    protected RealTimeAssessmentPanel conversationTreeSelectPanel;
    
    /** contains the widgets to select an instructional strategy for the apply strategy learner action */
    @UiField
    protected FlowPanel strategyRefSelectPanel;
    
    /** The ValueListBox containing the names of available instructional strategies */
    @UiField
    protected Select strategyNameBox;

    /** The panel that contains the url label and text box */
    @UiField
    protected FlowPanel autoTutorUrlPanel;

    /** The text box that contains the AutoTutor URL */
    @UiField
    protected TextBox autoTutorUrlBox;

    /** The button that takes the user to the AutoTutor authoring page */
    @UiField
    protected Button autoTutorLinkButton;

    /** The button that changes the type of {@link LearnerAction} */
    @UiField
    protected Button changeContentTypeButton;

    /** The dialog that displays the conversation tree editor */
    @UiField
    protected CourseObjectModal editorDialog;
    
    /**
     * the Select widget option for the placeholder strategy reference choice
     */
    private static final String STRATEGY_REF_PLACEHOLDER = "Select a strategy";

    /** The action that is currently being edited */
    private LearnerAction learnerActionToEdit = null;

    /**
     * The {@link LearnerActionEnumType} of the {@link LearnerAction} being
     * edited.
     */
    private LearnerActionType learnerActionType = null;

    /** Used to revert changes in dialog if user chooses to cancel */
    private TutorMeParams tempParams;

    /** The current SKO reference being edited */
    private AutoTutorSKO currentSko;

    /** The current remote SKO reference being edited */
    private ATRemoteSKO remoteSko;
    
    /** The current instructional strategy reference for the apply strategy learner action */
    private generated.dkf.LearnerAction.StrategyReference strategyReference;

    /** The current conversation file that is being edited */
    private ConversationTreeFile currentConversationTreeFile;

    /** The dialog used to select an existing conversation tree file */
    private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();

    /** The container for showing validation messages for the learner action not having a display name. */
    private final WidgetValidationStatus displayNameValidation;

    /** The container for showing validation messages for the learner action not having an AutoTutorURL */
    private final WidgetValidationStatus autoTutorUrlValidation;

    /** The container for showing validation messages for the learner action not having a conversation tree file. */
    private final WidgetValidationStatus conversationValidation;
    
    /** The container for showing validation messages for the learner action not having an instructional strategy selected */
    private final WidgetValidationStatus applyStrategyValidation;

    /**
     * Builds a new {@link LearnerActionEditor} that is aware of the list of
     * learner actions that it is editing each {@link LearnerAction}
     */
    public LearnerActionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LearnerActionEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        displayNameValidation = new WidgetValidationStatus(displayNameBox, "The learner action requires a unqiue display name. Please enter a unique display name.");
        displayNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("displayNameBox.onValueChange(" + event.getValue() + ")");
                }

                requestValidation(displayNameValidation);
            }
        });

        autoTutorUrlValidation = new WidgetValidationStatus(autoTutorUrlBox, "An AutoTutor URL must be provided");
        autoTutorUrlBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("autoTutorUrlBox.onValueChange(" + event.getValue() + ")");
                }

                URL url = new URL();
                url.setAddress(event.getValue());
                remoteSko.setURL(url);

                requestValidation(autoTutorUrlValidation);
            }
        });

        autoTutorLinkButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                GatClientUtility.openASATWindow();
            }
        });

        Toolbar defaultToolbar = new Toolbar()
                .addGroup(ToolbarButton.STYLE)
                .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC)
                .addGroup(ToolbarButton.FONT_NAME)
                .addGroup(ToolbarButton.FONT_SIZE, ToolbarButton.COLOR)
                .addGroup(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
                .addGroup(ToolbarButton.TABLE)
                .addGroup(ToolbarButton.UNDO, ToolbarButton.REDO)
                .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);

        descriptionBox.setToolbar(defaultToolbar);

        /* Populates the ribbon with each type of LearnerActionType */
        for (final LearnerActionType type : LearnerActionType.values()) {
            
            switch(type){
            
            case AUTO_TUTOR_CONVERSATION:
            case CONVERSATION_TREE:
                if(!ScenarioClientUtility.canTrainingAppUseMidLessonConversation()){
                    // training application doesn't support pausing so currently it doesn't make sense to have
                    // a conversation in the TUI
                    break;
                }
                //$FALL-THROUGH$
            default:
                actionTypeRibbon.addRibbonItem(
                        getIconForActionType(type),
                        type.getDisplayName(),
                        "Select this to add a " + type.getDisplayName(),
                        new ClickHandler() {

                            @Override
                            public void onClick(ClickEvent event) {
                                setActionType(type);
                                updateStrategyList();
                                showActionEditor();
                            }
                        });
            }

        }

        changeContentTypeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                showRibbon();
            }
        });
        
        applyStrategyValidation = new WidgetValidationStatus(strategyNameBox, "A strategy must be specified for the learner action.");
        strategyNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null) {

                    String oldRef = strategyReference.getName();
                    strategyReference.setName(event.getValue());
                    
                    ScenarioEventUtility.fireReferencesChangedEvent(learnerActionToEdit, oldRef, event.getValue());  
                    
                    requestValidation(applyStrategyValidation);
                }
            }
        });

        conversationValidation = new WidgetValidationStatus(conversationTreeSelectPanel.getAddAssessmentButton(),
                "A conversation tree file must be specified for the learner action.");
        fileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("fileSelectionDialog.onValueChange(" + event.getValue() + ")");
                }

                if (tempParams == null) {
                    tempParams = new TutorMeParams();
                }

                currentConversationTreeFile.setName(event.getValue());
                tempParams.setConfiguration(currentConversationTreeFile);

                /* Update the data model with the new conversation tree file */
                String name = currentConversationTreeFile.getName();
                boolean notBlank = StringUtils.isNotBlank(name);
                if (notBlank) {
                    conversationTreeSelectPanel.setAssessment(name);
                } else {
                    conversationTreeSelectPanel.removeAssessment();
                }

                requestValidation(conversationValidation);
            }
        });

        conversationTreeSelectPanel.getAddAssessmentButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "File selection is disabled in Read-Only mode.");
                    return;
                }

                if (learnerActionType == LearnerActionType.CONVERSATION_TREE) {
                    NewOrExistingFileDialog.showCreateOrSelect("Real-Time Assessment", new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent createEvent) {
                            final String courseFolder = GatClientUtility.getBaseCourseFolderPath();

                            final String url = GatClientUtility.createModalDialogUrl(courseFolder, "ConversationTree_",
                                    AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);

                            showConversationTreeModalEditor(courseFolder, url);
                        }

                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent selectEvent) {
                            fileSelectionDialog.getFileSelector().setAllowedFileExtensions(
                                    new String[] { AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION });
                            fileSelectionDialog.setIntroMessageHTML(DefaultGatFileSelectionDialog.CHOOSE_CONVERSATION_TREE_FILE_OBJECT);
                            setFileSelectionDialogVisible(true);
                        }
                    });
                } else if (learnerActionType == LearnerActionType.AUTO_TUTOR_CONVERSATION) {
                    NewOrExistingFileDialog.showCreateOrSelect("Real-Time Assessment", new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent createEvent) {

                            String title = "Opening the AutoTutor Conversation Authoring Tool";
                            String msgHtml = "The AutoTutor Conversation Authoring Tool will be opened if you click Okay.<br><br>Return to GIFT after completing your conversation.";
                            OkayCancelDialog.show(title, msgHtml, "Okay", new OkayCancelCallback() {

                                @Override
                                public void okay() {
                                    GatClientUtility.openASATWindow();

                                    /* A new dialog explaining the steps to
                                     * create the conversation in another
                                     * window, save it, then add it to GIFT via
                                     * file selection button or later
                                     * manually */
                                    OkayLaterDialog dialog = new OkayLaterDialog(
                                            "How to use your AutoTutor Conversation with GIFT",
                                            "<html>Follow these steps to use the AutoTutor Conversation you created with GIFT:"
                                                    + " <ul><li>Download and save the newly created conversation locally to your computer</li>"
                                                    + "<li>Add the conversation to your course using the 'Select Conversation' button below, or add it manually later</li></ul>",
                                            null, "Select Conversation", "Later", new OkayCancelCallback() {

                                                @Override
                                                public void okay() {
                                                    fileSelectionDialog.getFileSelector()
                                                            .setAllowedFileExtensions(new String[] {
                                                                    AbstractSchemaHandler.AUTOTUTOR_SKO_EXTENSION });
                                                    fileSelectionDialog.setIntroMessageHTML(DefaultGatFileSelectionDialog.CHOOSE_SKO_FILE);
                                                    setFileSelectionDialogVisible(true);

                                                }

                                                @Override
                                                public void cancel() {
                                                    /* Do Nothing */
                                                }
                                            });

                                    dialog.center();
                                }

                                @Override
                                public void cancel() {
                                    /* Do nothing */
                                }
                            });
                        }
                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent selectEvent) {
                            fileSelectionDialog.getFileSelector().setAllowedFileExtensions(
                                    new String[] { AbstractSchemaHandler.AUTOTUTOR_SKO_EXTENSION });
                            fileSelectionDialog.setIntroMessageHTML(DefaultGatFileSelectionDialog.CHOOSE_SKO_FILE);
                            setFileSelectionDialogVisible(true);
                        }
                    });

                }
            }
        });

        conversationTreeSelectPanel.getDeleteButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (GatClientUtility.isReadOnly()) {
                    return;
                }
                DeleteRemoveCancelDialog.show("Delete Conversation Tree",
                        "Do you wish to <b>permanently delete</b> this conversation tree or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
                                + "Other course objects will be unable to use this conversation tree if it is deleted, which may cause validation issues if this conversation tree is being referenced in other parts of the course.",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void delete() {
                                /* Gets the user name */
                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();

                                /* Gets the name of the file to delete */
                                List<String> filesToDelete = new ArrayList<>();
                                String conversationName = currentConversationTreeFile.getName();
                                final String filename = GatClientUtility.getBaseCourseFolderPath() +
                                        "/" + conversationName;
                                filesToDelete.add(filename);

                                /* Performs the delete */
                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
                                SharedResources.getInstance().getDispatchService().execute(action,
                                        new AsyncCallback<GatServiceResult>() {

                                            @Override
                                            public void onFailure(Throwable t) {
                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                        "Failed to delete the file.",
                                                        t.getMessage(),
                                                        DetailedException.getFullStackTrace(t));
                                                dialog.setDialogTitle("Deletion Failed");
                                                dialog.center();
                                            }

                                            @Override
                                            public void onSuccess(GatServiceResult result) {
                                                if (result.isSuccess()) {
                                                    remove();
                                                    tempParams.setConfiguration(null);
                                                } else {
                                                    logger.warning("Was unable to delete the file: " + filename
                                                            + "\nError Message: " + result.getErrorMsg());
                                                }
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                clearTemporaryLearnerActionObjects();
                                tempParams.setConfiguration(currentConversationTreeFile);
                                conversationTreeSelectPanel.removeAssessment();
                                fileSelectionDialog.setValue(null, true);
                                requestValidation(conversationValidation);
                            }

                            @Override
                            public void cancel() {

                            }

                        });
            }

        });

        conversationTreeSelectPanel.getEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> paramMap = new HashMap<>();
                paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));

                String name = ((ConversationTreeFile) tempParams.getConfiguration()).getName();
                final String courseFolder = GatClientUtility.getBaseCourseFolderPath();
                final String url = GatClientUtility.getModalDialogUrlWithParams(
                        courseFolder, name, paramMap);

                showConversationTreeModalEditor(courseFolder, url);
            }

        });

        clearTemporaryLearnerActionObjects();
    }

    @Override
    protected void populateEditor(final LearnerAction action) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + action + ")");
        }

        if (action == null) {
            throw new IllegalArgumentException("The parameter 'action' cannot be null.");
        }

        /* Hold a reference to the LearnerAction for validation later. */
        learnerActionToEdit = action;

        /* Resets each of the controls to an uninitialized state */
        autoTutorUrlBox.setValue(null);
        conversationTreeSelectPanel.removeAssessment();
        descriptionBox.setCode(null);
        displayNameBox.setValue(null);

        /* Populate display name */
        displayNameBox.setValue(action.getDisplayName());

        /* Populate the description */
        descriptionBox.setCode(action.getDescription());

        /* Populate sub-editor/sub-panel */
        if (action.getType() == LearnerActionEnumType.TUTOR_ME) {
            Serializable actionParams = action.getLearnerActionParams();
            if(actionParams instanceof generated.dkf.TutorMeParams){
                tempParams = (generated.dkf.TutorMeParams)actionParams;

                if (tempParams.getConfiguration() instanceof ConversationTreeFile) {
                    currentConversationTreeFile = (ConversationTreeFile) tempParams.getConfiguration();
                    if (currentConversationTreeFile.getName() != null) {
                        conversationTreeSelectPanel.setAssessment(currentConversationTreeFile.getName());
                        fileSelectionDialog.setValue(currentConversationTreeFile.getName());
                    }
                } else if (tempParams.getConfiguration() instanceof AutoTutorSKO) {
                    currentSko = (AutoTutorSKO) tempParams.getConfiguration();
                    if (currentSko.getScript() instanceof LocalSKO) {
                        /* Local SKOs are no longer supported so ignore the authored
                         * values and insert a ATRemoteSKO instead */
                        currentSko.setScript(remoteSko);
                    } else if (currentSko.getScript() instanceof ATRemoteSKO) {
                        remoteSko = (ATRemoteSKO) currentSko.getScript();
                        if (remoteSko.getURL() != null && remoteSko.getURL().getAddress() != null) {
                            autoTutorUrlBox.setValue(remoteSko.getURL().getAddress());
                        } else {
                            remoteSko.setURL(new URL());
                        }
                    }
                }
            }
        }else if(action.getType() == LearnerActionEnumType.APPLY_STRATEGY){
            Serializable actionParams = action.getLearnerActionParams();
            if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                strategyReference = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                String strategyName = strategyReference.getName();                
                
                // make sure the list has the latest strategies, returns with the placeholder being selected
                updateStrategyList();

                //update the box used to select the strategy name
                strategyNameBox.setValue(strategyName); 

                strategyNameBox.render();
                strategyNameBox.refresh();
                
            }
        }

        /* Show the appropriate UI based on the action type */
        setActionType(LearnerActionType.getTypeFromAction(action));
        if (learnerActionType == null) {
            // learner action type will be null when authoring a new type, not when switching types
            clearTemporaryLearnerActionObjects();  // clear out previous data model so previous choices don't populated new UX
            showRibbon();
        } else {
            showActionEditor();
        }
    }

    @Override
    protected void applyEdits(LearnerAction obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + obj + ")");
        }

        final String oldName = obj.getDisplayName();
        String newName = displayNameBox.getValue();
        obj.setType(learnerActionType.getActionType());

        /* Don't allow an empty string in the description since the
         * validateAvailableLearnerActions in ScenarioValidatorUtility, requires
         * that a learner action description may not be blank. In the case where
         * there is no text for the description, it should be made null. */
        obj.setDescription(StringUtils.isBlank(descriptionBox.getCode()) ? null : descriptionBox.getCode());
        obj.setDisplayName(newName);
        if (learnerActionType == LearnerActionType.AUTO_TUTOR_CONVERSATION
                || learnerActionType == LearnerActionType.CONVERSATION_TREE) {
            obj.setLearnerActionParams(tempParams);
        }else if(learnerActionType == LearnerActionType.APPLY_STRATEGY){
            obj.setLearnerActionParams(strategyReference);
        }

        if (StringUtils.isNotBlank(oldName) && !StringUtils.equals(oldName, newName)) {
            ScenarioEventUtility.fireRenameEvent(obj, oldName, newName);
        }
    }

    /**
     * Displays the ribbon used to selection the type of {@link LearnerAction}.
     */
    private void showRibbon() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showRibbon()");
        }

        setActionType(null);

        editorDeckPanel.showWidget(editorDeckPanel.getWidgetIndex(actionTypeRibbon));
        setSaveButtonVisible(false);
        clearValidations();
    }

    /**
     * Displays the editor that is used to edit the action once the type of the
     * action has been selected.
     */
    private void showActionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showActionEditor()");
        }

        setSaveButtonVisible(true);

        /* Show ribbon or editor */
        editorDeckPanel.showWidget(editorDeckPanel.getWidgetIndex(actionEditorPanel));

        /* Show or hide additional controls */
        if (learnerActionType == LearnerActionType.CONVERSATION_TREE) {
            tempParams.setConfiguration(currentConversationTreeFile);
            showConversationTreeSelector();
        } else if (learnerActionType == LearnerActionType.AUTO_TUTOR_CONVERSATION) {
            currentSko.setScript(remoteSko);
            tempParams.setConfiguration(currentSko);
            showAutoTutorConversationUrl();
        } else if(learnerActionType == LearnerActionType.APPLY_STRATEGY){
            strategyReference.setName(strategyNameBox.getValue());
            showStrategySelector();
        } else {
            autoTutorUrlPanel.setVisible(false);
            conversationTreeSelectPanel.setVisible(false);
            strategyRefSelectPanel.setVisible(false);
        }

        validateAll();
    }
    
    /**
     * Updates the available strategy names that the author can select from
     */
    public void updateStrategyList() {
        updateStrategyList(null, null);
    }    

    /**
     * Updates the available strategy names that the author can select from and, if necessary, replaces the old
     * name of a renamed strategy.
     *
     * @param oldName the old name to update.  Can be null if the list just needs to be updated.
     * @param newName the new value to update the name with. Can be null if the list just needs to be updated
     */
    public void updateStrategyList(String oldName, String newName) {

        String selectedName = strategyNameBox.getValue();

        List<String> strategyNames = ScenarioClientUtility.getAvailableStrategyNames();        

        //
        // update the UI with the most current list of strategy names
        //
        strategyNameBox.clear();
        Option placeholderOption = new Option();
        placeholderOption.setText(STRATEGY_REF_PLACEHOLDER);
        placeholderOption.setValue(STRATEGY_REF_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        strategyNameBox.add(placeholderOption);
        
        for (String strategyName : strategyNames) {
            Option option = new Option();
            option.setText(strategyName);
            option.setValue(strategyName);
            strategyNameBox.add(option);
        }

        if(!strategyNames.isEmpty()) {         
            
            if (StringUtils.isNotBlank(oldName)) {
                
                if(StringUtils.equals(oldName, selectedName)) {
                    // the current selected name is being changed to a new name
                    selectedName = newName;
                }
                
                //if a rename is occurring, make sure to replace the old name with the new one in the list
                // (for each occurrence of the old name)
                ListIterator<String> itr = strategyNames.listIterator();
                while (itr.hasNext()) {
                    String strategyName = itr.next();
                    if (StringUtils.equals(oldName, strategyName)) {
                        itr.set(newName);
                    }
                }
            }

            if(selectedName != null && strategyNames.contains(selectedName)) {
                //if a name was already selected and still exists in the updated list, reselect it
                strategyNameBox.setValue(selectedName);
                strategyReference.setName(selectedName);
            }else{
                strategyNameBox.setValue(STRATEGY_REF_PLACEHOLDER);
            }
        }
                
        strategyNameBox.render();
        strategyNameBox.refresh();

        requestValidation(applyStrategyValidation);
    }
    
    /**
     * Show the selector for instructional strategies.
     */
    private void showStrategySelector(){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showStrategySelector()");
        }
        
        autoTutorUrlPanel.setVisible(false);
        conversationTreeSelectPanel.setVisible(false);
        strategyRefSelectPanel.setVisible(true);
    }

    /**
     * Show the selector for a conversation tree file.
     */
    private void showConversationTreeSelector() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showConversationTreeSelector()");
        }

        autoTutorUrlPanel.setVisible(false);
        conversationTreeSelectPanel.setVisible(true);
        strategyRefSelectPanel.setVisible(false);
    }

    /**
     * Show the text box used to enter a url for an AutoTutor Conversation
     */
    private void showAutoTutorConversationUrl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showAutoTutorConversationUrl()");
        }

        autoTutorUrlPanel.setVisible(true);
        conversationTreeSelectPanel.setVisible(false);
        strategyRefSelectPanel.setVisible(false);
    }

    /**
     * Sets {@link #learnerActionType} and updates all controls that are
     * dependent on the value of {@link #learnerActionType}.
     *
     * @param type The {@link LearnerActionType} that should be used as the new
     *        value.
     */
    private void setActionType(LearnerActionType type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setActionType(" + type + ")");
        }

        learnerActionType = type;
        if (learnerActionType != null) {
            typeIcon.setWidget(getIconForActionType(learnerActionType));
            typeName.setHTML(learnerActionType.getDisplayName());
        }
    }

    /**
     * Gets the icon that is associated with a provided
     * {@link LearnerActionEnumType}.
     *
     * @param learnerActionType The {@link LearnerActionEnumType} for which to
     *        fetch an {@link IconType}. Can't be null.
     * @return The {@link IconType} that is associated with the provided
     *         {@link LearnerActionEnumType}. Can't be null.
     */
    private Widget getIconForActionType(LearnerActionType learnerActionType) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getIconForActionType(" + learnerActionType + ")");
        }

        if (learnerActionType == null) {
            throw new IllegalArgumentException("The parameter 'learnerActionType' cannot be null.");
        }

        switch (learnerActionType) {
        case EXPLOSIVE_HAZARD_SPOT_REPORT:
            Icon explosiveIcon = new Icon(IconType.TASKS);
            explosiveIcon.setSize(IconSize.TIMES2);
            return explosiveIcon;
        case NINE_LINE_REPORT:
            Icon nineLineIcon = new Icon(IconType.TASKS);
            nineLineIcon.setSize(IconSize.TIMES2);
            return nineLineIcon;
        case USE_RADIO:
            Icon useRadioIcon = new Icon(IconType.PHONE);
            useRadioIcon.setSize(IconSize.TIMES2);
            return useRadioIcon;
        case START_PACE_COUNT:
            Image startImage = new Image("images/pace_start.png");
            startImage.setWidth("28px");
            return startImage;
        case END_PACE_COUNT:
            Image endImage = new Image("images/pace_end.png");
            endImage.setWidth("28px");
            return endImage;
        case SPOT_REPORT:
            Icon spotReportIcon = new Icon(IconType.TASKS);
            spotReportIcon.setSize(IconSize.TIMES2);
            return spotReportIcon;
        case AUTO_TUTOR_CONVERSATION:
            return new Image(GatClientBundle.INSTANCE.survey_icon());
        case CONVERSATION_TREE:
            return new Image(GatClientBundle.INSTANCE.survey_icon());
        case ASSESS_MY_LOCATION:
            Image mapImage = new Image("images/map.png");
            mapImage.setWidth("28px");
            return mapImage;
        case APPLY_STRATEGY:
            Image strategyImg = new Image("images/strategy.png");
            strategyImg.setWidth("28px");
            return strategyImg;
        }

        String msg = "The LearnerActionEnumType '" + learnerActionType + "' does not have an associated icon";
        throw new IllegalArgumentException(msg);
    }


    /**
     * Shows or hides the {@link #fileSelectionDialog}.
     *
     * @param visible True if the {@link #fileSelectionDialog} should be shown.
     *        False if it should be hidden.
     */
    private void setFileSelectionDialogVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setFileSelectionDialogVisible(" + visible + ")");
        }

        if (visible) {
            fileSelectionDialog.center();
        } else {
            fileSelectionDialog.hide();
        }
    }

    /**
     * Initializes and clears the temporary objects for some learner action types.
     */
    private void clearTemporaryLearnerActionObjects() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("clearTemporaryLearnerActionObjects()");
        }

        /* clear temporary values. These will be re-initialized when the panel
         * is opened again. */
        tempParams = new TutorMeParams();
        currentConversationTreeFile = new ConversationTreeFile();
        currentSko = new AutoTutorSKO();
        remoteSko = new ATRemoteSKO();
        currentSko.setScript(remoteSko);
        strategyReference = new generated.dkf.LearnerAction.StrategyReference();
        
        strategyNameBox.setValue(null);
    }

    /**
     * Launches the modal for editing or creating a conversation tree.
     *
     * @param coursePath The path to the course
     * @param url The url containing the data that is used to instantiate the
     *        modal.
     */
    private void showConversationTreeModalEditor(final String coursePath, final String url) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(coursePath, url);
            logger.fine("showConversationTreeModalEditor(" + StringUtils.join(", ", params) + ")");
        }

        editorDialog.setCourseObjectUrl(CourseObjectName.CONVERSATION_TREE.getDisplayName()
                + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
        editorDialog.setSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("editorDialog.saveButton.onClick(" + event.toDebugString() + ")");
                }

                GatClientUtility.saveEmbeddedCourseObject();
                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, url,
                        AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                fileSelectionDialog.setValue(filename, true);
                editorDialog.stopEditor();
                CourseObjectModal.resetEmbeddedSaveObject();
                
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this conversation being edited
                }
            }
        });

        editorDialog.show();
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        boolean enabled = !isReadonly;

        actionTypeRibbon.setReadonly(isReadonly);
        descriptionBox.setEnabled(enabled);
        autoTutorUrlBox.setEnabled(enabled);
        autoTutorLinkButton.setEnabled(enabled);
        changeContentTypeButton.setEnabled(enabled);
        conversationTreeSelectPanel.setReadOnlyMode(isReadonly);
        displayNameBox.setEnabled(enabled);
        strategyNameBox.setEnabled(enabled);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(displayNameValidation);
        validationStatuses.add(autoTutorUrlValidation);
        validationStatuses.add(conversationValidation);
        validationStatuses.add(applyStrategyValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (displayNameValidation.equals(validationStatus)) {

            /* if ribbon is visible, cannot have invalid name */
            if (editorDeckPanel.getVisibleWidget() == editorDeckPanel.getWidgetIndex(actionTypeRibbon)) {
                displayNameValidation.setValid();
                return;
            }

            /* Determine if a name was provided. */
            String newName = displayNameBox.getValue();
            boolean isBlank = StringUtils.isBlank(newName);
            if (isBlank) {
                displayNameValidation.setErrorMessage("Please enter a unique display name");
                displayNameValidation.setInvalid();
                return;
            }

            /* Determine if the name that was provided is a duplicate of an
             * existing name. */
            for (LearnerAction action : getParentItemListEditor().getItems()) {
                String actionName = action.getDisplayName();
                if (action != learnerActionToEdit && StringUtils.equals(actionName, newName)) {
                    String errorMsg = new StringBuilder()
                            .append("There is already a learner action named '")
                            .append(newName)
                            .append("'. Please enter a unique display name.")
                            .toString();
                    displayNameValidation.setErrorMessage(errorMsg);
                    displayNameValidation.setInvalid();
                    return;
                }
            }

            displayNameValidation.setValid();
        } else if (autoTutorUrlValidation.equals(validationStatus)) {
            boolean notBlank = StringUtils.isNotBlank(autoTutorUrlBox.getValue());
            boolean isAutoTutor = learnerActionType == LearnerActionType.AUTO_TUTOR_CONVERSATION;
            autoTutorUrlValidation.setValidity(learnerActionType == null || !isAutoTutor || notBlank);
        } else if (conversationValidation.equals(validationStatus)) {
            boolean notBlank = StringUtils.isNotBlank(fileSelectionDialog.getValue());
            boolean isConversationTree = learnerActionType == LearnerActionType.CONVERSATION_TREE;
            conversationValidation.setValidity(learnerActionType == null || !isConversationTree || notBlank);
        } else if(applyStrategyValidation.equals(validationStatus)){
            
            if(learnerActionType == LearnerActionType.APPLY_STRATEGY){
                boolean hasStrategyName = StringUtils.isNotBlank(strategyNameBox.getValue());
                boolean isExistingStrategy = false;
                if(hasStrategyName){
                    for(generated.dkf.Strategy strategy : ScenarioClientUtility.getStrategies().getStrategy()){
                        if(StringUtils.equals(strategyNameBox.getValue(), strategy.getName())){
                            isExistingStrategy = true;
                            break;
                        }
                    }
                }
                applyStrategyValidation.setValidity(hasStrategyName && isExistingStrategy);
            }else{
                applyStrategyValidation.setValid();
            }
        }
    }

    @Override
    protected boolean validate(LearnerAction learnerAction) {
        String errorMsg = ScenarioValidatorUtility.validateLearnerAction(learnerAction);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }
}