/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * Edits a {@link Coordinate} object.
 * 
 * @author sharrison
 */
public class ScenarioCoordinateEditor extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioCoordinateEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static WaypointEditorUiBinder uiBinder = GWT.create(WaypointEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface WaypointEditorUiBinder extends UiBinder<Widget, ScenarioCoordinateEditor> {
    }
    
    /** Hyphen string */
    private static final String HYPHEN = "-";

    /** Decimal string */
    private static final String DECIMAL = ".";
    
    /** the coordinate type ribbon tile width, increasing the width to account for 'AGL' label length */
    private static final double COORDINATE_RIBBON_WIDTH = 140;

    /**
     * Callback to indicate that the {@link ScenarioCoordinateEditor} panel changed.
     * 
     * @author sharrison
     */
    public interface RibbonPanelChangeCallback {
        /**
         * Callback used when the choice ribbon panel is shown or hidden.
         * 
         * @param visible true if the ribbon is visible.
         */
        void ribbonVisible(boolean visible);
    }
    
    /** The panel containing the possible panels to be displayed */
    @UiField
    protected DeckPanel deckPanel;
    
    @UiField
    protected Widget editTypeLabelPanel;

    /** The widget containing the different coordiante type choices */
    @UiField
    protected Ribbon choiceRibbon;

    /** The panel containing the components necessary to edit a coordinate */
    @UiField
    protected FlowPanel editCoordinatePanel;

    /** The icon that indicates the selected coordinate type when editing */
    @UiField
    protected Icon editIcon;

    /** The label to display for the selected coordinate type when editing */
    @UiField
    protected InlineHTML editIconLabel;

    /** The label corresponding to the first coordinate value */
    @UiField
    protected InlineHTML firstEditLabel;

    /** The label corresponding to the second coordinate value */
    @UiField
    protected InlineHTML secondEditLabel;

    /** The label corresponding to the third coordinate value */
    @UiField
    protected InlineHTML thirdEditLabel;

    /** The input field corresponding to the first coordinate value */
    @UiField
    protected TextBox firstEditField;

    /** The input field corresponding to the second coordinate value */
    @UiField
    protected TextBox secondEditField;

    /** The input field corresponding to the third coordinate value */
    @UiField
    protected TextBox thirdEditField;

    /** The button used to change from one coordinate type to another */
    @UiField
    protected Button changeTypeButton;

    /** The panel containing the save and cancel buttons */
    @UiField
    protected FlowPanel buttonPanel;

    /**
     * The save button to push the coordinate changes to the data model and display the view mode
     */
    @UiField
    protected Button saveButton;

    /** Reverts any coordinate value changes and displays the view mode */
    @UiField
    protected Button cancelButton;

    /** The panel surrounding the components for the view mode */
    @UiField
    protected FlowPanel viewModePanel;

    /** The label to display for the selected coordinate type when in view mode */
    @UiField
    protected Icon viewModeIcon;

    /** Label to specify which coordinate type was selected */
    @UiField
    protected InlineHTML viewCoordinateTypeLabel;

    /** The first coordinate value label to be displayed when in view mode */
    @UiField
    protected BubbleLabel firstViewLabel;

    /** The second coordinate value label to be displayed when in view mode */
    @UiField
    protected BubbleLabel secondViewLabel;

    /** The third coordinate value label to be displayed when in view mode */
    @UiField
    protected BubbleLabel thirdViewLabel;
    
    /** The {@link Coordinate} that is being edited */
    private Coordinate coordinate = null;

    /** The selected coordinate type. */
    private Serializable selectedCoordinateType;
    
    /** The ribbon item for the {@link GCC} coordinate 
     *  Can be null if the training application doesn't support this type.
     */
    private final Widget gccRibbonButton;
    
    /** The ribbon item for the {@link GDC} coordinate 
     *  Can be null if the training application doesn't support this type.
     */
    private final Widget gdcRibbonButton;
    
    /** The ribbon item for the {@link AGL} coordinate 
     *  Can be null if the training application doesn't support this type.
     */
    private final Widget aglRibbonButton;
    
    /** read only flag */
    private boolean isReadOnly = false;

    /**
     * Standalone mode prevents live-updating of the data model and instead displays a 'set/cancel'
     * button that when clicked will push the changes to the data model and then display a 'view
     * mode' of the data. When standalone is disabled, the coordinate fields will update the
     * data-model in real-time.
     */
    private boolean isStandaloneMode = false;
    
    /**
     * List of callback listeners to be executed when the ribbon panel changes.
     */
    private List<RibbonPanelChangeCallback> ribbonPanelChangeCallbacks = new ArrayList<RibbonPanelChangeCallback>();

    /**
     * Constructor
     */
    public ScenarioCoordinateEditor() {
        this(false);
    }

    /**
     * Constructor
     * 
     * @param enableStandaloneMode true to enable standalone mode; false to disable it. Standalone
     *        mode prevents live-updating of the data model and instead displays a 'set/cancel'
     *        button that when clicked will push the changes to the data model and then display a
     *        'view mode' of the data. When standalone is disabled, the coordinate fields will
     *        update the data-model in real-time.
     */
    public ScenarioCoordinateEditor(boolean enableStandaloneMode) {

        initWidget(uiBinder.createAndBindUi(this));
        
        isStandaloneMode = enableStandaloneMode;
        
        choiceRibbon.setTileWidth(COORDINATE_RIBBON_WIDTH);
        
        // determine which coordinate types are allowed by training application type
        boolean createGDC = false, createGCC = false, createAGL = false;
        
        TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
        Set<String> coordinateTypesToCreate = TrainingApplicationEnum.getValidCoordinateTypes(taType);
        
        if (coordinateTypesToCreate.contains(TrainingApplicationEnum.GDC_COORDINATE_NAME)) {
            createGDC = true;
        }
        if (coordinateTypesToCreate.contains(TrainingApplicationEnum.GCC_COORDINATE_NAME)) {
            createGCC = true;
        }
        if (coordinateTypesToCreate.contains(TrainingApplicationEnum.AGL_COORDINATE_NAME)) {
            createAGL = true;
        }

        if(createGCC){
            gccRibbonButton = choiceRibbon.addRibbonItem(CoordinateType.GCC.getIconType(), "Geocentric Coordinates (GCC)",
                    "Select this to add a geocentric coordinate.", new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            Serializable type = (selectedCoordinateType instanceof GCC) ? selectedCoordinateType
                                    : CoordinateType.GCC.createNewCoordindateType();
                            populateEditor(type);
                            showPanel(editCoordinatePanel);
                        }
                    });
        }else{
            gccRibbonButton = null;
        }

        if(createGDC){
            gdcRibbonButton = choiceRibbon.addRibbonItem(CoordinateType.GDC.getIconType(), "Geodetic Coordinates (GDC)",
                    "Select this to add a geodetic coordinate.", new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            Serializable type = (selectedCoordinateType instanceof GDC) ? selectedCoordinateType
                                    : CoordinateType.GDC.createNewCoordindateType();
                            populateEditor(type);
                            showPanel(editCoordinatePanel);
                        }
                    });
        }else{
            gdcRibbonButton = null;
        }

        if(createAGL){
            aglRibbonButton = choiceRibbon.addRibbonItem(CoordinateType.AGL.getIconType(),
                    "Above Ground Location (AGL)", "Select this to add an above ground location.",
                    new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            
                            // in case the user edited the dom to unhide the AGL button on the ribbon when
                            // the ribbon is shown because there are 2 coordinate types to choose from.  Only
                            // VBS has 2 coordinate types to choose from, so other training apps that use AGL will
                            // auto select that coordinate type and therefore they click event never happens on the ribbon
                            // because the ribbon is never shown for that training app type
                            if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
                                Serializable type = (selectedCoordinateType instanceof AGL) ? selectedCoordinateType
                                        : CoordinateType.AGL.createNewCoordindateType();
                                populateEditor(type);
                                showPanel(editCoordinatePanel);
                            }
                        }
                    });
        }else{
            aglRibbonButton = null;
        }
        
        // no coordinate type tiles will exists, show a label instead
        if(!createAGL && !createGCC && !createGDC){
            choiceRibbon.setNoRibbonItemMessage("Unable to author locations for "+taType.getDisplayName() + ".");
        }
        
        // make sure buttons in the ribbon are visible and the change type button is shown appropriately
        refreshRibbon(null);
        
        KeyPressHandler keyPressHandler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent pressEvent) {
                char keyPressed = pressEvent.getCharCode();
                // Prevent any non-numerical values from being entered
                if (!(Character.isDigit(keyPressed) || keyPressed == '-' || keyPressed == '.')) {
                    pressEvent.preventDefault();
                }
            }
        };

        firstEditField.addKeyPressHandler(keyPressHandler);
        secondEditField.addKeyPressHandler(keyPressHandler);
        thirdEditField.addKeyPressHandler(keyPressHandler);

        firstEditField.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent arg0) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        formatNumber(firstEditField);
                    }
                });
            }
        });

        secondEditField.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent arg0) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        formatNumber(secondEditField);
                    }
                });
            }
        });

        thirdEditField.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent arg0) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        formatNumber(thirdEditField);
                    }
                });
            }
        });

        firstEditField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                try {
                    final String newValue = event.getValue();
                    if (StringUtils.equals(newValue, HYPHEN) || StringUtils.equals(newValue, DECIMAL)
                            || newValue.trim().length() == 0) {
                        firstEditField.setValue(getX().toString());
                    } else {
                        setX(new BigDecimal(newValue));
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    firstEditField.setValue(getX().toString());
                }
            }
        });

        secondEditField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                try {
                    final String newValue = event.getValue();
                    if (StringUtils.equals(newValue, HYPHEN) || StringUtils.equals(newValue, DECIMAL)
                            || newValue.trim().length() == 0) {
                        secondEditField.setValue(getY().toString());
                    } else {
                        setY(new BigDecimal(newValue));
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    secondEditField.setValue(getY().toString());
                }
            }
        });

        thirdEditField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                try {
                    final String newValue = event.getValue();
                    if (StringUtils.equals(newValue, HYPHEN) || StringUtils.equals(newValue, DECIMAL)
                            || newValue.trim().length() == 0) {
                        thirdEditField.setValue(getZ().toString());
                    } else {
                        setZ(new BigDecimal(newValue));
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    thirdEditField.setValue(getZ().toString());
                }
            }
        });

        changeTypeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showPanel(choiceRibbon);
            }
        });

        addSaveHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateCoordinate();
                populateViewModeLabels();
                showPanel(viewModePanel);
            }
        });

        addCancelHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (coordinate.getType() == null) {
                    showPanel(choiceRibbon);
                } else {
                    populateEditor(coordinate.getType());
                    showPanel(viewModePanel);
                }
            }
        });

        viewModePanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!isReadOnly) {
                    showPanel(editCoordinatePanel);
                }
            }
        }, ClickEvent.getType());

        buttonPanel.setVisible(isStandaloneMode);
    }
    
    /**
     * Checks if the inputBox contains a hyphen and formats it as a negative number, checks that the
     * value is within the min/max range and handles empty string values as the default value
     * @param coordinateTextBox the text box to format
     */
    private void formatNumber(TextBox coordinateTextBox) {
        String text = coordinateTextBox.getText();
        String originialText = text;

        if (text.contains(HYPHEN)) {

            // if it starts with '-', remove all '-' and add one to the start
            // to remove any duplicates
            if (text.startsWith(HYPHEN)) {
                text = text.replaceAll(HYPHEN, "");
                text = HYPHEN.concat(text);
            } else { // otherwise, remove any '-' since it would be an invalid format
                text = text.replaceAll(HYPHEN, "");
            }

        }

        // if it contains a '.', remove any subsequent '.'
        if (text.contains(DECIMAL)) {
            if (text.indexOf(DECIMAL) != text.lastIndexOf(DECIMAL)) {
                int firstIndex = text.indexOf(DECIMAL);
                String textIncludingFirstDecimal = text.substring(0, firstIndex + 1);
                String textAfterFirstDecimal = text.substring(firstIndex + 1).replaceAll(RegExp.quote(DECIMAL), "");
                text = textIncludingFirstDecimal.concat(textAfterFirstDecimal);
            }
        }

        try {

            BigDecimal numValue = new BigDecimal(text);
            if (!text.endsWith(DECIMAL) && !StringUtils.equals(originialText, numValue.toString())) {
                coordinateTextBox.setValue(numValue.toString(), true);
            }

        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            if (StringUtils.equals(text, HYPHEN) || text.trim().length() == 0) {
                // do nothing
            } else {
                // reset the input box to this widget's last valid value
                coordinateTextBox.setValue(coordinateTextBox.getValue());
            }
        }
    }

    /**
     * Builds the ribbon with the provided options.  Also sets whether the change type button
     * is shown (shown if there is more than one coordinate type button in the ribbon).
     * 
     * @param hideCoordinateTypes the list of coordinate items to hide in the ribbon.  If null or empty,
     * than all coordinate type buttons that were added in the constructor based on the training application type
     * will be visible.
     */
    private void refreshRibbon(List<CoordinateType> hideCoordinateTypes) {
        // if null we want to hide everything so use empty list
        if (hideCoordinateTypes == null) {
            hideCoordinateTypes = new ArrayList<>();
        }

        if(gccRibbonButton != null){
            gccRibbonButton.getElement().getStyle()
                    .setDisplay(hideCoordinateTypes.contains(CoordinateType.GCC) ? Display.NONE : Display.INLINE_BLOCK);
        }

        if(gdcRibbonButton != null){
            gdcRibbonButton.getElement().getStyle()
                    .setDisplay(hideCoordinateTypes.contains(CoordinateType.GDC) ? Display.NONE : Display.INLINE_BLOCK);
        }

        if(aglRibbonButton != null){
            aglRibbonButton.getElement().getStyle()
                    .setDisplay(hideCoordinateTypes.contains(CoordinateType.AGL) ? Display.NONE : Display.INLINE_BLOCK);
        }
        
        changeTypeButton.setVisible(choiceRibbon.containsOneChoice() == null);
        logger.info("Change type button visible = "+changeTypeButton.isVisible());
    }

    /**
     * Returns a copy of the {@link Coordinate} that is currently being edited.
     *
     * @return A copy of the {@link Coordinate}. Can't be null.
     */
    public Coordinate getCoordinateCopy() {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList();
            logger.fine("getCoordinateCopy(" + StringUtils.join(", ", params) + ")");
        }

        Coordinate toRet = new Coordinate();
        
        if(isTypeSelected()) {
            toRet.setType(createCoordinateType());
        }
        
        return toRet;
    }

    /**
     * Populates the editors backing coordinate. Will update the panel using the data within the
     * given {@link Coordinate}.
     * 
     * @param coordinate the data object that will be used to populate the panel.
     */
    public void setCoordinate(Coordinate coordinate) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setCoordinate(" + coordinate + ")");
        }

        if (coordinate == null) {
            throw new IllegalArgumentException("The parameter 'coordinate' cannot be null.");
        }

        this.coordinate = coordinate;

        // reset field labels
        firstEditField.setValue("0");
        secondEditField.setValue("0");
        thirdEditField.setValue("0");
        
        if (coordinate.getType() == null) {
            Widget oneChoiceWidget = choiceRibbon.containsOneChoice();
            if (oneChoiceWidget != null) {
                //there is only one choice in the ribbon, so choose it for the user automatically
                logger.info("Only 1 choice for coordinate");
                if(oneChoiceWidget == gccRibbonButton){
                    Serializable gcc = CoordinateType.GCC.createNewCoordindateType();
                    coordinate.setType(gcc);
                    populateEditor(gcc);
                }else if(oneChoiceWidget == gdcRibbonButton){
                    Serializable gdc = CoordinateType.GDC.createNewCoordindateType();
                    coordinate.setType(gdc);
                    populateEditor(gdc);
                }else if(oneChoiceWidget == aglRibbonButton){
                    Serializable agl = CoordinateType.AGL.createNewCoordindateType();
                    coordinate.setType(agl);
                    populateEditor(agl);
                }else{
                    //default to choice ribbon
                    selectedCoordinateType = null;
                    showPanel(choiceRibbon);
                    return;
                }                

                if(isStandaloneMode) {
                    showPanel(viewModePanel);
                } else {
                    showPanel(editCoordinatePanel);
                }
            } else {
                logger.info("There is more than 1 choice");
                selectedCoordinateType = null;
                showPanel(choiceRibbon);
            }
        } else {
            populateEditor(coordinate.getType());

            if (isStandaloneMode) {
                showPanel(viewModePanel);
            } else {
                showPanel(editCoordinatePanel);
            }
        }
    }
    
    /**
     * Populates the editors backing coordinate. Will update the panel using the data within the
     * given {@link Coordinate} if the editor is not currently editing.
     * 
     * @param coordinate the data object that will be used to populate the panel.
     */
    public void silentlyUpdateCoordinate(Coordinate coordinate) {
        /* if editing, silently update the data model. This change will be seen if the user cancels
         * their edit. */
        if (isEditing()) {
            this.coordinate = coordinate;
            return;
        }

        /* if it is not currently editing, update data model and repopulate panel */
        setCoordinate(coordinate);
    }

    /**
     * Sets the X coordinate value. Can be called by a different name (e.g. longitude).
     * 
     * @param value the value to set.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public void setX(BigDecimal value) {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            gcc.setX(value);
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            gdc.setLongitude(value);
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            agl.setX(value);
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Retrieves the X coordinate value. Can be called by a different name (e.g. longitude).
     * 
     * @return the X coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public BigDecimal getX() {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            return gcc.getX();
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            return gdc.getLongitude();
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            return agl.getX();
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Sets the Y coordinate value. Can be called by a different name (e.g. latitude).
     * 
     * @param value the value to set.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public void setY(BigDecimal value) {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            gcc.setY(value);
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            gdc.setLatitude(value);
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            agl.setY(value);
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Retrieves the Y coordinate value. Can be called by a different name (e.g. latitude).
     * 
     * @return the Y coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public BigDecimal getY() {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            return gcc.getY();
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            return gdc.getLatitude();
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            return agl.getY();
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Sets the Z coordinate value. Can be called by a different name (e.g. elevation).
     * 
     * @param value the value to set.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public void setZ(BigDecimal value) {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            gcc.setZ(value);
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            gdc.setElevation(value);
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            agl.setElevation(value);
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }
    
    /**
     * Retrieves the Z coordinate value. Can be called by a different name (e.g. elevation).
     * 
     * @return the Z coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public BigDecimal getZ() {
        if (selectedCoordinateType instanceof GCC) {
            GCC gcc = (GCC) selectedCoordinateType;
            return gcc.getZ();
        } else if (selectedCoordinateType instanceof GDC) {
            GDC gdc = (GDC) selectedCoordinateType;
            return gdc.getElevation();
        } else if (selectedCoordinateType instanceof AGL) {
            AGL agl = (AGL) selectedCoordinateType;
            return agl.getElevation();
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Displays the provided panel; hides the other panels.
     * 
     * @param panel the panel to show
     */
    private void showPanel(Widget panel) {
        deckPanel.showWidget(deckPanel.getWidgetIndex(panel));
        for (RibbonPanelChangeCallback callback : ribbonPanelChangeCallbacks) {
            callback.ribbonVisible(panel == choiceRibbon);
        }
    }
    
    /**
     * Checks if the editor is currently editing.
     * 
     * @return true if the editor is currently editing; false otherwise
     */
    private boolean isEditing() {
        return editCoordinatePanel.isVisible();
    }
    
    /**
     * Checks if the author has selected a type.
     * 
     * @return true if the author has selected a type, false if a type was never selected (can only
     *         happen if the editor is new and the author never selected an option from the ribbon).
     */
    public boolean isTypeSelected() {
        return selectedCoordinateType != null;
    }
    
    /**
     * Adds a callback to be executed whenever the ribbon panel is shown or hidden in the
     * {@link ScenarioCoordinateEditor}.
     * 
     * @param callback the callback to execute.
     */
    public void addRibbonPanelChangeCallback(RibbonPanelChangeCallback callback) {
        ribbonPanelChangeCallbacks.add(callback);
    }

    /**
     * Updates the backing data model {@link Coordinate} with the selected coordinate type.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public void updateCoordinate() {
        if (coordinate == null) {
            throw new UnsupportedOperationException("Cannot update a null coordinate.");
        } else if (selectedCoordinateType == null) {
            throw new UnsupportedOperationException("No coordinate type was selected. Cannot update the coordinate.");
        }
        
        Serializable coordinateType = createCoordinateType();
        coordinate.setType(coordinateType);
    }

    /**
     * Creates a {@link GCC}, {@link GDC}, or {@link VBSAGL} based on the
     * current widget state.
     *
     * @return The {@link Coordinate} type that can be used to populate
     *         {@link Coordinate#getType()}. Can't be null.
     */
    private Serializable createCoordinateType() {
        if (selectedCoordinateType instanceof GCC) {
            GCC selectedGcc = (GCC) selectedCoordinateType;
            GCC newGcc = new GCC();
            newGcc.setX(selectedGcc.getX());
            newGcc.setY(selectedGcc.getY());
            newGcc.setZ(selectedGcc.getZ());
            return newGcc;
        } else if (selectedCoordinateType instanceof GDC) {
            GDC selectedGdc = (GDC) selectedCoordinateType;
            GDC newGdc = new GDC();
            newGdc.setLongitude(selectedGdc.getLongitude());
            newGdc.setLatitude(selectedGdc.getLatitude());
            newGdc.setElevation(selectedGdc.getElevation());
            coordinate.setType(newGdc);
            return newGdc;
        } else if (selectedCoordinateType instanceof AGL) {
            AGL selectedAgl = (AGL) selectedCoordinateType;
            AGL newAgl = new AGL();
            newAgl.setX(selectedAgl.getX());
            newAgl.setY(selectedAgl.getY());
            newAgl.setElevation(selectedAgl.getElevation());
            coordinate.setType(newAgl);
            return newAgl;
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + selectedCoordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Populates the editor with the provided {@link Coordinate} type.
     * 
     * @param coordinateType the coordinate type used to populate the editor.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public void populateEditor(Serializable coordinateType) {
        if (coordinateType == null) {
            throw new IllegalArgumentException("The parameter 'coordinateType' cannot be null.");
        }

        CoordinateType coordinateTypeEnum;
        if (coordinateType instanceof GCC) {
            coordinateTypeEnum = CoordinateType.GCC;
            GCC gcc = (GCC) coordinateType;
            firstEditField.setValue(gcc.getX() == null ? "0" : gcc.getX().toString());
            secondEditField.setValue(gcc.getY() == null ? "0" : gcc.getY().toString());
            thirdEditField.setValue(gcc.getZ() == null ? "0" : gcc.getZ().toString());
        } else if (coordinateType instanceof GDC) {
            coordinateTypeEnum = CoordinateType.GDC;
            GDC gdc = (GDC) coordinateType;
            firstEditField.setValue(gdc.getLongitude() == null ? "0" : gdc.getLongitude().toString());
            secondEditField.setValue(gdc.getLatitude() == null ? "0" : gdc.getLatitude().toString());
            thirdEditField.setValue(gdc.getElevation() == null ? "0" : gdc.getElevation().toString());
        } else if (coordinateType instanceof AGL) {
            coordinateTypeEnum = CoordinateType.AGL;
            AGL agl = (AGL) coordinateType;
            firstEditField.setValue(agl.getX() == null ? "0" : agl.getX().toString());
            secondEditField.setValue(agl.getY() == null ? "0" : agl.getY().toString());
            thirdEditField.setValue(agl.getElevation() == null ? "0" : agl.getElevation().toString());
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + coordinateType.getClass().getName() + "' was unexpected");
        }

        editIcon.setType(coordinateTypeEnum.getIconType());
        editIconLabel.setHTML(coordinateTypeEnum.getLabel());
        viewModeIcon.setType(coordinateTypeEnum.getIconType());

        populateEditFieldLabels(coordinateTypeEnum);

        // reset the selected coorindate type
        selectedCoordinateType = coordinateTypeEnum.createNewCoordindateType();

        // populate the selected coordiante type with the newly populated edit fields
        setX(new BigDecimal(firstEditField.getValue()));
        setY(new BigDecimal(secondEditField.getValue()));
        setZ(new BigDecimal(thirdEditField.getValue()));

        if (isStandaloneMode) {
            populateViewModeLabels();
        }
    }

    /**
     * Populates the edit field labels associated with the provided type.
     * 
     * @param type the {@link CoordinateType} used to determine which labels to use.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    private void populateEditFieldLabels(CoordinateType type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateFieldLabels(" + type + ")");
        }

        switch (type) {
        case GCC:
            firstEditLabel.setText("X:");
            secondEditLabel.setText("Y:");
            thirdEditLabel.setText("Z:");
            break;
        case AGL:
            firstEditLabel.setText("X:");
            secondEditLabel.setText("Y:");
            thirdEditLabel.setText("Elevation:");
            break;
        case GDC:
            firstEditLabel.setText("Longitude:");
            secondEditLabel.setText("Latitude:");
            thirdEditLabel.setText("Elevation:");
            break;
        default:
            throw new UnsupportedOperationException(
                    "The CoordinateType '" + type + "' was not handled by populateFieldLabels");
        }
    }

    /**
     * Populates the view mode labels associated with the provided type.
     */
    private void populateViewModeLabels() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateFieldLabels()");
        }

        CoordinateType type = CoordinateType.getCoordinateTypeFromCoordinate(selectedCoordinateType);
        viewCoordinateTypeLabel.setText(type.name());

        firstViewLabel.setHTML(type.buildXLabel(Float.valueOf(firstEditField.getValue())));
        secondViewLabel.setHTML(type.buildYLabel(Float.valueOf(secondEditField.getValue())));
        thirdViewLabel.setHTML(type.buildZLabel(Float.valueOf(thirdEditField.getValue())));
    }
    
    /**
     * Sets the disallowed types to be hidden in the editor ribbon.
     * 
     * @param disallowedTypes the {@link CoordinateType types} to be hidden.  Can be empty/null
     * to hide nothing.
     */
    public void setDisallowedTypes(CoordinateType... disallowedTypes) {
        
        if(disallowedTypes == null || disallowedTypes.length == 0){
            return;
        }
        
        logger.info("Hiding the following coordinate types: "+disallowedTypes);
        
        List<CoordinateType> disallowedTypeChoices = new ArrayList<CoordinateType>();
        disallowedTypeChoices.addAll(Arrays.asList(disallowedTypes));

        refreshRibbon(disallowedTypeChoices);
    }
    
    /**
     * Adds a click handler to the save button.
     * 
     * @param clickHandler the click handler that will be executed on save.
     */
    public void addSaveHandler(ClickHandler clickHandler) {
        saveButton.addClickHandler(clickHandler);
    }

    /**
     * Adds a click handler to the cancel button.
     * 
     * @param clickHandler the click handler that will be executed on cancel.
     */
    public void addCancelHandler(ClickHandler clickHandler) {
        cancelButton.addClickHandler(clickHandler);
    }
    
    /**
     * Retrieves the ribbon used by this editor.
     * 
     * @return the {@link Ribbon}
     */
    public Ribbon getEditorRibbon() {
        return choiceRibbon;
    }

    /**
     * Updates the components in the editor based on the provided read-only flag.
     * 
     * @param readOnly true to set the components as read-only.
     */
    public void setReadOnly(boolean readOnly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadOnly(" + readOnly + ")");
        }
        this.isReadOnly = readOnly;
        choiceRibbon.setReadonly(readOnly);
        firstEditField.setEnabled(!readOnly);
        secondEditField.setEnabled(!readOnly);
        thirdEditField.setEnabled(!readOnly);
        changeTypeButton.setEnabled(!readOnly);
        saveButton.setEnabled(!readOnly);
    }
    
    /**
     * Sets whether or not the label indicating the current type should be visible
     * 
     * @param visible whether to show the type label
     */
    public void setTypeLabelVisible(boolean visible) {
        editTypeLabelPanel.setVisible(visible);
    }
    
    /**
     * Sets whether or not the button that lets authors change the coordinate type should be visible
     * 
     * @param visible whether to show the change type button
     */
    public void setChangeTypeButtonVisible(boolean visible) {
        changeTypeButton.setVisible(false);
    }
}
