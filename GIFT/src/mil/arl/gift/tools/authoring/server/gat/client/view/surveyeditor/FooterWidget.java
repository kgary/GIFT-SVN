/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddPageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyImportEvent;

/**
 * The footer widget for the survey editor panel always displays as the last
 * element on the workspace. It allows for the author to insert new questions,
 * pages, or copy existing survey items into the survey.
 * 
 * @author nblomberg
 *
 */
public class FooterWidget extends Composite {

    private static Logger logger = Logger.getLogger(FooterWidget.class.getName());

    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, FooterWidget> {
    }

    /** Flag to control if the footer widget is in readonly mode. */
    private boolean isReadOnly = false;

    /** Type of survey the footer widget belongs to. */
    private SurveyDialogOption surveyDialogOptionType;

    /** The mode that the survey is in. */
    private SurveyEditMode mode = SurveyEditMode.WritingMode;

    @UiField
    Button addPageButton;

    @UiField
    Button addSurveyItemButton;

    @UiField
    protected Button importButton;

    @UiHandler("importButton")
    void onClickImport(ClickEvent event) {
        if (!isReadOnly) {
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyImportEvent());
        }
    }

    /**
     * Constructor (default)
     */
    public FooterWidget() {

        logger.info("constructor()");
        initWidget(uiBinder.createAndBindUi(this));

        addPageButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {

                logger.info("firing SurveyAddPageEvent");
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyAddPageEvent());
            }
        });

        addSurveyItemButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {

                logger.info("firing SurveyAddSurveyItemEvent");
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyAddSurveyItemEvent());
            }
        });
    }

    /**
     * Initializes the widget based on the type of survey being edited.
     * 
     * @param type - The type of survey being edited.
     * @param isReadOnly - Whether the widget is allowed to be edited.
     */
    public void initializeWidget(SurveyDialogOption type, Boolean isReadOnly) {

        setReadOnlyMode(isReadOnly);
        surveyDialogOptionType = type;

        switch (type) {
        case ASSESSLEARNER_QUESTIONBANK:
            addPageButton.setVisible(false);
            break;
        case ASSESSLEARNER_STATIC:
            // intentional pass through
        case COLLECTINFO_NOTSCORED:
            // intentional pass through
        case COLLECTINFO_SCORED:
            // Set the visibility. Usually defaults to true, however we still
            // set
            // it explicitly in case it was false before
            addPageButton.setVisible(mode == SurveyEditMode.WritingMode && !isReadOnly);
            break;
        default:
            logger.severe("SurveyEditorPanel received unsupported mode.  Panel could not be initialized with mode: "
                    + surveyDialogOptionType);
            break;
        }
    }

    /**
     * Sets the visibility of footer buttons based on readOnly mode
     * 
     * @param readOnly true if the buttons should be hidden
     */
    public void setReadOnlyMode(boolean readOnly) {

        isReadOnly = readOnly;

        addPageButton.setVisible(surveyDialogOptionType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK ? false
                : !readOnly && mode == SurveyEditMode.WritingMode);
        addSurveyItemButton.setVisible(!readOnly);
        importButton.setVisible(!readOnly);
    }

    /**
     * Sets the editing mode for the footer widget.
     * 
     * @param mode the editing mode
     */
    public void setMode(SurveyEditMode mode) {

        boolean isWritingMode = (mode == SurveyEditMode.WritingMode);
        this.mode = mode;

        addPageButton.setVisible(
                surveyDialogOptionType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK ? false : isWritingMode);
        addSurveyItemButton.setVisible(isWritingMode);
        importButton.setVisible(isWritingMode);
    }

}
