/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.SpacingCondition;
import generated.dkf.SpacingCondition.SpacingPair;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for Spacing.
 */
public class SpacingConditionInputEditor extends ConditionInputPanel<SpacingCondition> {

    /** The ui binder. */
    private static SpacingConditionEditorUiBinder uiBinder = GWT
            .create(SpacingConditionEditorUiBinder.class);

    /** The editor that edits the list of {@link PointRef} */
    @UiField(provided = true)
    protected ItemListEditor<SpacingPair> spacingPairEditor = new ItemListEditor<>(new SpacingPairItemEditor());
    
    /** The box used to enter how long the speed must be exceeded to trigger an assessment */
    @UiField
    protected FormattedTimeBox durationBox;

    /** Validation that displays an error if there are no pairs that define spacing between entities */
    private final WidgetValidationStatus noPairsValidation;

    /**
     * The Interface SpacingConditionEditorUiBinder.
     */
    interface SpacingConditionEditorUiBinder extends UiBinder<Widget, SpacingConditionInputEditor> {
    }

    /**
     * Default Constructor
     *
     * Required to be public for GWT UIBinder compatibility.
     */
    public SpacingConditionInputEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        spacingPairEditor.setFields(buildItemFields());
        Widget addPlaceButton = spacingPairEditor.addCreateListAction("Click to add a pair of entities that should maintain spacing", 
            new CreateListAction<SpacingPair>() {
                @Override
                public SpacingPair createDefaultItem() {
                    return new SpacingPair();
                }
        });

        spacingPairEditor.addListChangedCallback(new ListChangedCallback<SpacingPair>() {

            @Override
            public void listChanged(ListChangedEvent<SpacingPair> event) {

                getInput().getSpacingPair().clear();
                getInput().getSpacingPair().addAll(spacingPairEditor.getItems());

                requestValidationAndFireDirtyEvent(getCondition(), noPairsValidation);
                
                //a team reference may have been changed, so update the global list of references
                ScenarioClientUtility.gatherTeamReferences();
            }
        });
        
        spacingPairEditor.setRemoveItemDialogTitle("Remove Spacing Rule");
        spacingPairEditor.setRemoveItemStringifier(new Stringifier<SpacingPair>() {
            
            @Override
            public String stringify(SpacingPair obj) {
                
                String firstObjName = "UNKNOWN";
                String secondObjName = "UNKNOWN";
                
                if(obj.getFirstObject() != null && obj.getFirstObject().getTeamMemberRef() != null) {
                    firstObjName = obj.getFirstObject().getTeamMemberRef();
                }
                
                if(obj.getSecondObject() != null && obj.getSecondObject().getTeamMemberRef() != null) {
                    secondObjName = obj.getSecondObject().getTeamMemberRef();
                }
                
                return "this spacing rule for " + firstObjName + " and " + secondObjName;
            }
        });
        
        durationBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                
                getInput().setMinDurationBeforeViolation(
                        event.getValue() != null 
                            ? BigInteger.valueOf(event.getValue()) 
                            : null);
            }
        });

        noPairsValidation = new WidgetValidationStatus(addPlaceButton, 
                "At least one pair of entities that should maintain spacing must be defined.");
    }

    @Override
    protected void onEdit() {

        List<SpacingPair> pairs = new ArrayList<>();
        pairs.addAll(getInput().getSpacingPair());
        
        spacingPairEditor.setItems(pairs);
        
        if(getInput().getMinDurationBeforeViolation() == null) {
            getInput().setMinDurationBeforeViolation(BigInteger.valueOf(10)); //default duration of 5 seconds
        }
        
        durationBox.setValue(getInput().getMinDurationBeforeViolation().intValue());
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(noPairsValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int spacingPairSize;
        if (noPairsValidation.equals(validationStatus)) {
            spacingPairSize = spacingPairEditor.size();
            validationStatus.setValidity(spacingPairSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(spacingPairEditor);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        spacingPairEditor.setReadonly(isReadonly);
        durationBox.setReadOnly(isReadonly);
    }

    /**
     * Creates the fields for the {@link #spacingPairEditor}.
     *
     * @return The {@link Iterable} containing each {@link ItemField}.
     */
    private Iterable<? extends ItemField<SpacingPair>> buildItemFields() {
        ItemField<SpacingPair> summaryField = new ItemField<SpacingPair>(null, "100%") {
            @Override
            public Widget getViewWidget(SpacingPair pair) {
                
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                
                String firstObjName = "UNKNOWN";
                String secondObjName = "UNKNOWN";
                String idealMin = "UNKNOWN";
                String idealMax = "UNKNOWN";
                
                if(pair.getFirstObject() != null && pair.getFirstObject().getTeamMemberRef() != null) {
                    firstObjName = pair.getFirstObject().getTeamMemberRef();
                }
                
                if(pair.getSecondObject() != null && pair.getSecondObject().getTeamMemberRef() != null) {
                    secondObjName = pair.getSecondObject().getTeamMemberRef();
                }
                
                if(pair.getIdeal() != null && pair.getIdeal().getIdealMinSpacing() != null) {
                    idealMin = pair.getIdeal().getIdealMinSpacing().toString();
                }
                
                if(pair.getIdeal() != null && pair.getIdeal().getIdealMaxSpacing() != null) {
                    idealMax = pair.getIdeal().getIdealMaxSpacing().toString();
                }
                
                builder.appendHtmlConstant("<b>")
                .appendEscaped(firstObjName)
                .appendHtmlConstant("</b> and <b>")
                .appendEscaped(secondObjName)
                .appendHtmlConstant("</b> should remain between <b>")
                .appendEscaped(idealMin)
                .appendHtmlConstant("</b> and <b>")
                .appendEscaped(idealMax)
                .appendHtmlConstant("</b> meters from each other");

                return new HTML(builder.toSafeHtml());
            }
        };

        return Arrays.asList(summaryField);
    }
}