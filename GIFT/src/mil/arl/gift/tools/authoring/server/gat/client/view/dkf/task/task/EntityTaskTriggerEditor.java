/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task;

import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.Point;
import generated.dkf.PointRef;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor.RibbonPanelChangeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The Class EntityTaskTriggerEditor.
 */
public class EntityTaskTriggerEditor extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EntityTaskTriggerEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static EntityTaskTriggerEditorUiBinder uiBinder = GWT.create(EntityTaskTriggerEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface EntityTaskTriggerEditorUiBinder extends UiBinder<Widget, EntityTaskTriggerEditor> {
    }

    /** The entity ID editor */
    @UiField
    protected EntityIdEditor entityIdEditor;

    /** The trigger coordinate */
    @UiField
    protected ScenarioCoordinateEditor triggerCoordinateEditor;
    
    /** ribbon with the choices of location type for this trigger (inline coordinate or place of interest) */
    @UiField
    protected Ribbon locationTypeRibbon;
    
    /** contains the widgets used to author the inline coordinate location for this trigger */
    @UiField
    protected FlowPanel inlineCoordinatePanel;
    
    /** contains the widgets used to author the place of interest for this trigger */
    @UiField
    protected FlowPanel placeOfInterestPanel;
    
    /** the radius to use with the point selected */
    @UiField
    protected DecimalNumberSpinner minDistance;
    
    /** used to select the point for this trigger */
    @UiField(provided = true)
    protected PlaceOfInterestPicker placeOfInterestPicker = new PlaceOfInterestPicker(Point.class);
    
    /** contains the ribbon for selecting the location type of inline coordinate or place of interest */
    @UiField
    protected DeckPanel locationTypeDeck;

    /**
     * The container for showing validation messages for the entity start location being invalid.
     */
    private final WidgetValidationStatus entityStartValidation;

    /**
     * The container for showing validation messages for the trigger coordinate type being invalid because a type was not selected (e.g. GDC).
     */
    private final WidgetValidationStatus triggerValidation;
        
    /**
     * the container for showing validation messages for the trigger location choice being invalid.
     */
    private final WidgetValidationStatus locationChoiceValidation;

    /** Instantiates a new entity based task trigger editor. */
    public EntityTaskTriggerEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        entityStartValidation = new WidgetValidationStatus(entityIdEditor,
                "The entity starting coordinate must be populated. Please select a coordinate type.");
        triggerValidation = new WidgetValidationStatus(triggerCoordinateEditor.getEditorRibbon(),
                "The trigger coordinate must be populated. Please select a coordinate type.");
        locationChoiceValidation = new WidgetValidationStatus(locationTypeRibbon, "The location type must be selected.");
        
        initRibbon();
        
        if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
            // for VBS, AGL is only allowed for point authoring
            triggerCoordinateEditor.setDisallowedTypes(CoordinateType.AGL);
            placeOfInterestPicker.setDisallowedTypes(CoordinateType.AGL);
        }
       
        minDistance.setMinValue(BigDecimal.ZERO);

        triggerCoordinateEditor.addRibbonPanelChangeCallback(new RibbonPanelChangeCallback() {
            @Override
            public void ribbonVisible(boolean visible) {
                /* we only care about re-validating if the author selected a coordinate type (ribbon
                 * was hidden) for the first time */
                if (!visible && !triggerValidation.isValid()) {
                    requestValidation(triggerValidation);
                }                
            }
        });
    }
    
    /**
     * Initializes the {@link #locationTypeRibbon}.
     */
    private void initRibbon() {
        
        locationTypeRibbon.setTileHeight(105);
        
        if (ScenarioClientUtility.getScenario().getResources().getSourcePath() == null) {
			locationTypeRibbon.addRibbonItem(IconType.GLOBE, "Coordinate", "Provide a coordinate", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					showInlineCoordinateEditor();
				}
			});
        }
        
        locationTypeRibbon
            .addRibbonItem(IconType.MAP_MARKER, "Place of Interest", "Select a place of interest", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showPlaceOfInterestEditor();
                }
            });
        
        resetLocationType(false);
    }
    
    /**
     * Show the location type ribbon that allows the author to choose inline coordinate or place of interest.
     * @param validate whether to call validation check on the location type choice.  False is used when validation
     * can't be called yet due to the widget still being loaded.
     */
    public void resetLocationType(boolean validate) {
        locationTypeDeck.showWidget(locationTypeDeck.getWidgetIndex(locationTypeRibbon));
        
        // to disable validation because the picker is not visible
        placeOfInterestPicker.setActive(false);
        
        if(validate) {
            requestValidation(locationChoiceValidation);
        }
    }
    
    /**
     * Show the inline coordinate editor which allows authoring coordinates
     */
    public void showInlineCoordinateEditor() {
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showInlineCoordinateEditor()");
        }
        
        if(triggerCoordinateEditor.getCoordinateCopy().getType() == null) {
            triggerCoordinateEditor.setCoordinate(new Coordinate());
        }
        
        // to disable validation because the picker is not visible
        placeOfInterestPicker.setActive(false);

        locationTypeDeck.showWidget(locationTypeDeck.getWidgetIndex(inlineCoordinatePanel));
        requestValidation(locationChoiceValidation);
    }
    
    /**
     * Show the place of interest picker panel which allows selecting a place of interest (point) and defining
     * a radius.
     */
    public void showPlaceOfInterestEditor() {
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showPlaceOfInterestEditor()");
        }
        
        // to enable validation and then validate
        placeOfInterestPicker.setActive(true);
        placeOfInterestPicker.validateAll();

        locationTypeDeck.showWidget(locationTypeDeck.getWidgetIndex(placeOfInterestPanel));
        requestValidation(locationChoiceValidation);
    }
    
    /**
     * Return the coordinate from the inline coordinate editor
     * @return will be null if the inline coordinate location type was not selected.
     */
    public Coordinate getCoordinate() {
        
        if(locationTypeDeck.getVisibleWidget() == locationTypeDeck.getWidgetIndex(inlineCoordinatePanel)) {
            return triggerCoordinateEditor.getCoordinateCopy();
        }else {
            return null;
        }        
    }
    
    /**
     * Return the place of interest point reference that contains the details of the place of interest
     * picker and the distance widget.
     * @return a new PointRef with the widget values, will be null if the place of interest location type was not selected.
     */
    public PointRef getPlaceOfInterest() {
        
        if(locationTypeDeck.getVisibleWidget() == locationTypeDeck.getWidgetIndex(placeOfInterestPanel)) {
            String poiName = placeOfInterestPicker.getValue();
            BigDecimal radius = minDistance.getValue();
            PointRef ptRef = new PointRef();
            ptRef.setValue(poiName);
            ptRef.setDistance(radius);
            return ptRef;
        }        
        
        return null;
    }

    /**
     * Gets the entity ID editor.
     *
     * @return the entity ID editor
     */
    public EntityIdEditor getEntityIdEditor() {
        return entityIdEditor;
    }

    /**
     * Gets the trigger coordinate editor.
     *
     * @return the trigger coordinate editor instance used to populate inline coordinates as the location value
     */
    public ScenarioCoordinateEditor getTriggerCoordinateEditor() {
        return triggerCoordinateEditor;
    }
    
    /**
     * Gets the place of interest picker
     * @return the picker used to select a place of interest point as the location value
     */
    public PlaceOfInterestPicker getPlaceOfInterestPicker() {
        return placeOfInterestPicker;
    }
    
    /**
     * Gets the widget used to input the place of interest point radius
     * @return the widget used to provide a radius
     */
    public DecimalNumberSpinner getMinDistanceSpinner() {
        return minDistance;
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        entityIdEditor.setReadOnly(isReadonly);
        triggerCoordinateEditor.setReadOnly(isReadonly);
        placeOfInterestPicker.setReadonly(isReadonly);
        locationTypeRibbon.setReadonly(isReadonly);
        minDistance.setEnabled(!isReadonly);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(entityStartValidation);
        validationStatuses.add(triggerValidation);
        validationStatuses.add(locationChoiceValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (triggerValidation.equals(validationStatus)) {
            // valid if 
            // 1. the inline coordinate type is shown and the trigger coordinate editor type is selected
            // 2. the inline coordinate type is not shown
            boolean coordinateTypeShown = locationTypeDeck.getVisibleWidget() == locationTypeDeck.getWidgetIndex(inlineCoordinatePanel);
            boolean coordinateTypeSelected = triggerCoordinateEditor.isTypeSelected();
            validationStatus.setValidity((coordinateTypeShown && coordinateTypeSelected) || !coordinateTypeShown);
            
        }else if(locationChoiceValidation.equals(validationStatus)) {
            // invalid if the location type ribbon is shown
            validationStatus.setValidity(locationTypeDeck.getVisibleWidget() != locationTypeDeck.getWidgetIndex(locationTypeRibbon));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(entityIdEditor);
        childValidationComposites.add(placeOfInterestPicker);
    }
}
