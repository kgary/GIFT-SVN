/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Area;
import generated.dkf.AreaRef;
import generated.dkf.Point;
import generated.dkf.PointRef;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An {@link ItemEditor} responsible for editing a {@link AvoidPlaceRefWrapper} within an
 * {@link ItemListEditor}.
 *
 * @author tflowers
 *
 */
public class AvoidPlaceRefItemEditor extends ItemEditor<AvoidPlaceRefWrapper> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AvoidPlaceRefItemEditor.class.getName());

    /** The binder that combines the ui.xml with this java class */
    private static AvoidPlaceRefItemEditorUiBinder uiBinder = GWT.create(AvoidPlaceRefItemEditorUiBinder.class);

    /** The binder that combines the ui.xml with the java class */
    interface AvoidPlaceRefItemEditorUiBinder extends UiBinder<Widget, AvoidPlaceRefItemEditor> {
    }

    /** Place of interest picker for the place to avoid */
    @UiField(provided = true)
    protected PlaceOfInterestPicker avoidPlacePicker = new PlaceOfInterestPicker(Point.class, Area.class);

    /** The minimum expected distance input field */
    @UiField(provided = true)
    protected DecimalNumberSpinner minDistance = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    @UiField
    protected Widget distancePanel;

    /** Verifies the place to avoid has not been already used in the ItemListEditor. */
    private final WidgetValidationStatus placeStatus;

    /**
     * The current {@link AvoidPlaceRefWrapper} being edited.
     * <b style="color: red;">IMPORTANT:</b> Do not edit the object through this
     * reference.
     */
    private AvoidPlaceRefWrapper editingPlaceRef;

    /**
     * Constructs a new {@link AvoidPlaceRefItemEditor}
     * 
     * @param inputPanel the {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to.
     *        Can't be null.
     */
    public AvoidPlaceRefItemEditor(final ConditionInputPanel<?> inputPanel) {
        
        if (inputPanel == null) {
            throw new IllegalArgumentException("Input panel argument cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        /* Initialize validation statuses */
        placeStatus = new WidgetValidationStatus(avoidPlacePicker.getTextBoxRef(), "You've already authored a rule for this place");        
        
        CoordinateType[] disallowedTypes = ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.AvoidLocationCondition.class);
        if(disallowedTypes != null){
            avoidPlacePicker.setDisallowedTypes(disallowedTypes);
        }
        avoidPlacePicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                inputPanel.setDirty();

                Serializable selectedLocation = ScenarioClientUtility.getPlaceOfInterestWithName(event.getValue());
                
                //hide the minimum distance field if an Area is selected
                distancePanel.setVisible(!(selectedLocation instanceof Area));
                
                requestValidation(placeStatus);
            }
        });

        minDistance.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> arg0) {
                inputPanel.setDirty();
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(placeStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (validationStatus.equals(placeStatus)) {
            for (AvoidPlaceRefWrapper ref : getParentItemListEditor().getItems()) {
                /* Ignore the place currently being edited */
                if (ref == editingPlaceRef) {
                    continue;
                }

                /* If the selected place already has a rule defined, fail
                 * validation */
                String referencedPlace = null;
                
                if(ref.getPlaceRef() instanceof PointRef) {
                    referencedPlace = ((PointRef) ref.getPlaceRef()).getValue();
                    
                } else if(ref.getPlaceRef() instanceof AreaRef) {
                    referencedPlace = ((AreaRef) ref.getPlaceRef()).getValue();
                }
                
                if (StringUtils.equals(referencedPlace, avoidPlacePicker.getValue())) {
                    validationStatus.setInvalid();
                    return;
                }
            }

            validationStatus.setValid();
        }
    }

    @Override
    protected boolean validate(AvoidPlaceRefWrapper avoidPlaceRefWrapper) {
        Serializable placeRef = avoidPlaceRefWrapper.getPlaceRef();

        String thisReferencedPlace, errorMsg;
        if (placeRef instanceof PointRef) {
            PointRef pointRef = (PointRef) placeRef;
            thisReferencedPlace = pointRef.getValue();
            errorMsg = ScenarioValidatorUtility.validatePointRef(pointRef);
        } else if (placeRef instanceof AreaRef) {
            AreaRef areaRef = (AreaRef) placeRef;
            thisReferencedPlace = areaRef.getValue();
            errorMsg = ScenarioValidatorUtility.validateAreaRef(areaRef);
        } else {
            /* Null is invalid */
            return false;
        }

        if (StringUtils.isNotBlank(errorMsg)) {
            return false;
        }

        for (AvoidPlaceRefWrapper ref : getParentItemListEditor().getItems()) {
            /* Skip the specified wrapper */
            if (ref == avoidPlaceRefWrapper) {
                continue;
            }

            /* If the specified place already has a rule defined, fail
             * validation */
            String referencedPlace = null;

            if (ref.getPlaceRef() instanceof PointRef) {
                referencedPlace = ((PointRef) ref.getPlaceRef()).getValue();
            } else if (ref.getPlaceRef() instanceof AreaRef) {
                referencedPlace = ((AreaRef) ref.getPlaceRef()).getValue();
            }

            if (StringUtils.equals(thisReferencedPlace, referencedPlace)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    protected void populateEditor(AvoidPlaceRefWrapper ref) {        
        editingPlaceRef = ref;
        
        Serializable place = ref.getPlaceRef();
        
        if(place == null) {
            
            PointRef newPoint = new PointRef();
            newPoint.setDistance(BigDecimal.ZERO);
            
            place = newPoint;
        }
        
        if(place instanceof PointRef) {
            
            PointRef pointRef = (PointRef) place;
            
            // Set the distance
            BigDecimal distance = pointRef.getDistance() == null ? BigDecimal.ZERO : pointRef.getDistance();
            minDistance.setValue(distance);
            distancePanel.setVisible(true);

            // Set the place to avoid
            avoidPlacePicker.setValue(pointRef.getValue());
            
        } else if(place instanceof AreaRef) {
            
            AreaRef areaRef = (AreaRef) place;
            
            distancePanel.setVisible(false);

            // Set the place to avoid
            avoidPlacePicker.setValue(areaRef.getValue());
        }  
    }

    @Override
    protected void applyEdits(AvoidPlaceRefWrapper ref) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + ref + ")");
        }

        if(distancePanel.isVisible()) {
            
            PointRef pointRef = new PointRef();
            pointRef.setDistance(minDistance.getValue());
            pointRef.setValue(avoidPlacePicker.getValue());
            
            ref.setPlaceRef(pointRef);
            
        } else {
            
            AreaRef areaRef = new AreaRef();
            areaRef.setValue(avoidPlacePicker.getValue());
            
            ref.setPlaceRef(areaRef);
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        avoidPlacePicker.setReadonly(isReadonly);
        minDistance.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(avoidPlacePicker);
    }
}