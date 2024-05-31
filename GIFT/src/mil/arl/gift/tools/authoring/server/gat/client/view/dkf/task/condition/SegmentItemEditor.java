/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.Segment;
import generated.dkf.Segment.End;
import generated.dkf.Segment.Start;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A widget used to add and edit segments.
 * 
 * @author sharrison
 */
public class SegmentItemEditor extends ItemEditor<Segment> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static SegmentItemEditorUiBinder uiBinder = GWT.create(SegmentItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SegmentItemEditorUiBinder extends UiBinder<Widget, SegmentItemEditor> {
    }

    /** TextBox component for setting the segment name */
    @UiField
    protected TextBox segmentName;

    /** The segment width input field */
    @UiField(provided = true)
    protected DecimalNumberSpinner segmentWidth = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);

    /** The buffer width input field */
    @UiField(provided = true)
    protected DecimalNumberSpinner bufferWidth = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    /** An editor used to modify the start coordinate of the segment being edited */
    @UiField
    protected ScenarioCoordinateEditor startCoordinate;
    
    /** An editor used to modify the end coordinate of the segment being edited */
    @UiField
    protected ScenarioCoordinateEditor endCoordinate;

    /** The segment currently being edited */
    private Segment selectedSegment;
    
    /** Error message for when the segment name is blank */
    private final static String EMPTY_NAME_ERR_MSG = "The segment name cannot be empty.";
    
    /** The container for showing validation messages for not having a segment name set. */
    private WidgetValidationStatus segmentNameValidationStatus;

    /** The container for showing validation messages for not having a segment width set. */
    private WidgetValidationStatus segmentWidthValidationStatus;

    /** The container for showing validation messages for not having a buffer width set. */
    private WidgetValidationStatus bufferWidthValidationStatus;
    
    /** The {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to. */
    private final ConditionInputPanel<?> inputPanel;

    /**
     * Constructor.
     * 
     * @param inputPanel the {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to
     *        Can't be null.
     */
    public SegmentItemEditor(final ConditionInputPanel<?> inputPanel) {
        
        if (inputPanel == null) {
            throw new IllegalArgumentException("Input panel argument cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        this.inputPanel = inputPanel;

        segmentNameValidationStatus = new WidgetValidationStatus(segmentName, EMPTY_NAME_ERR_MSG);
        segmentWidthValidationStatus = new WidgetValidationStatus(segmentWidth,
                "The segment width must be at least 0.01 meters.");
        bufferWidthValidationStatus = new WidgetValidationStatus(bufferWidth, "The buffer width cannot be empty.");

        segmentName.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                inputPanel.setDirty();
                requestValidation(segmentNameValidationStatus);
            }
        });

        segmentWidth.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                inputPanel.setDirty();
                requestValidation(segmentWidthValidationStatus);
            }
        });

        bufferWidth.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                inputPanel.setDirty();
                requestValidation(bufferWidthValidationStatus);
            }
        });
        
        startCoordinate.setReadOnly(true);
        endCoordinate.setReadOnly(true);
    }

    @Override
    protected void populateEditor(Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("The parameter 'segment' cannot be null.");
        }
        
        selectedSegment = segment;
        
        segmentName.setValue(segment.getName());
        segmentWidth.setValue(segment.getWidth() == null ? BigDecimal.ZERO : segment.getWidth());
        bufferWidth.setValue(segment.getBufferWidthPercent() == null ? BigDecimal.ZERO : segment.getBufferWidthPercent());

        if (segment.getStart() == null) {
            segment.setStart(new Start());
        }
        
        if(segment.getStart().getCoordinate() == null) {
            segment.getStart().setCoordinate(new Coordinate());
        }
        
        startCoordinate.setCoordinate(segment.getStart().getCoordinate());

        if (segment.getEnd() == null) {
            segment.setEnd(new End());
        }
        
        if(segment.getEnd().getCoordinate() == null) {
            segment.getEnd().setCoordinate(new Coordinate());
        }
        
        endCoordinate.setCoordinate(segment.getEnd().getCoordinate());
    }

    @Override
    protected void applyEdits(Segment obj) {
        inputPanel.setClean();
        
        obj.setName(segmentName.getValue());

        obj.setWidth(segmentWidth.getValue() == null ? BigDecimal.ZERO : segmentWidth.getValue());

        obj.setBufferWidthPercent(bufferWidth.getValue() == null ? BigDecimal.ZERO : bufferWidth.getValue());
        
        //a waypoint reference may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }
    
    @Override
    protected void onCancel() {
        inputPanel.setClean();
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //no children to validate
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(segmentNameValidationStatus);
        validationStatuses.add(segmentWidthValidationStatus);
        validationStatuses.add(bufferWidthValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (segmentNameValidationStatus.equals(validationStatus)) {
            if (StringUtils.isBlank(segmentName.getValue())) {
                segmentNameValidationStatus.setErrorMessage(EMPTY_NAME_ERR_MSG);
                segmentNameValidationStatus.setInvalid();
            } else {
                boolean foundDuplicate = false;
                for (Segment segment : getParentItemListEditor().getItems()) {
                    // skip current segment
                    if (segment.equals(selectedSegment)) {
                        continue;
                    }

                    if (StringUtils.equalsIgnoreCase(segment.getName(), segmentName.getValue())) {
                        foundDuplicate = true;
                        segmentNameValidationStatus
                                .setErrorMessage("The segment name '" + segmentName.getValue() + "' is a duplicate.");
                        break;
                    }
                }
                segmentNameValidationStatus.setValidity(!foundDuplicate);
            }
        } else if (segmentWidthValidationStatus.equals(validationStatus)) {
            segmentWidthValidationStatus.setValidity(
                    segmentWidth.getValue() != null && segmentWidth.getValue().compareTo(BigDecimal.valueOf(0.01)) >= 0);
        } else if (bufferWidthValidationStatus.equals(validationStatus)) {
            bufferWidthValidationStatus.setValidity(
                    bufferWidth.getValue() != null && bufferWidth.getValue().compareTo(BigDecimal.ZERO) >= 0);  
        }
    }

    @Override
    protected boolean validate(Segment segment) {
        String errorMsg = ScenarioValidatorUtility.validateSegment(segment);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        segmentName.setEnabled(!isReadonly);
        segmentWidth.setEnabled(!isReadonly);
        bufferWidth.setEnabled(!isReadonly);
    }
}
