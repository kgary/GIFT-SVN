/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Checkpoint;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.CheckpointEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The Class CheckpointProgressConditionEditor.
 */
public class CheckpointProgressConditionEditorImpl extends ConditionInputPanel<CheckpointProgressCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CheckpointProgressConditionEditorImpl.class.getName());

    /** The ui binder. */
    private static CheckpointProgressConditionEditorUiBinder uiBinder = GWT
            .create(CheckpointProgressConditionEditorUiBinder.class);

    /**
     * The Interface CheckpointProgressConditionEditorUiBinder.
     */
    interface CheckpointProgressConditionEditorUiBinder
            extends UiBinder<Widget, CheckpointProgressConditionEditorImpl> {
    }

    /** The data grid. */
    @UiField(provided = true)
    protected ItemListEditor<Checkpoint> checkpointsEditor = new ItemListEditor<>(new CheckpointEditor(this));

    /** Team member picker used to determine the learner role that this condition should assess */
    @UiField(provided = true)
    protected TeamMemberPicker teamMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** Validation container for when no checkpoint is created */
    private final WidgetValidationStatus checkpointValidation;

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public CheckpointProgressConditionEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CheckpointProgressConditionEditorImpl()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        Widget createButton = checkpointsEditor.addCreateListAction("Checkpoint", new CreateListAction<Checkpoint>() {
            @Override
            public Checkpoint createDefaultItem() {
                
                setDirty();
                
                List<Serializable> placesOfInterest = ScenarioClientUtility.getUnmodifiablePlacesOfInterestList();

                Checkpoint checkpoint = new Checkpoint();
                checkpoint.setAtTime("00:00:00");
                checkpoint.setWindowOfTime(BigDecimal.valueOf(0.0));
                if (!placesOfInterest.isEmpty()) {
                    
                    for(Serializable placeOfInterest : placesOfInterest){
                        
                        if(placeOfInterest instanceof Point){
                            
                            Point pointStart = (Point) placeOfInterest;
                            checkpoint.setPoint(pointStart.getName());
                            
                            break;
                        }
                    }
                }

                return checkpoint;
        }
        });

        checkpointsEditor.setFields(Arrays.asList(
        
        new ItemField<Checkpoint>() {
            
            @Override
            public Widget getViewWidget(Checkpoint item) {
                
                Label label = new Label(item.getPoint());
                label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                
                return label;
            }
            
        }, new ItemField<Checkpoint>() {
            
            @Override
            public Widget getViewWidget(Checkpoint item) {
                
                int atTime = FormattedTimeBox.getTimeFromString(item.getAtTime());
                BubbleLabel label = new BubbleLabel(
                        "<b>Arrival Time:</b> " + FormattedTimeBox.getDisplayText(atTime));
                label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                
                return label;
            }
            
        }, new ItemField<Checkpoint>(null, "100%") {
            
            @Override
            public Widget getViewWidget(Checkpoint item) {
                
                HTML html = new HTML();
                html.setWidth("100%");
                
                // window of time is optional, don't show if zero
                if (item.getWindowOfTime().intValue() != 0) {
                    
                    BubbleLabel label = new BubbleLabel("<b>Time Window:</b> "
                            + FormattedTimeBox.getDisplayText(item.getWindowOfTime().intValue()));
                    label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                    
                    html.setHTML(label.toString());
                    
                } else {
                    
                    //add padding, otherwise the html will have no height and will be unclickable
                    html.getElement().getStyle().setPadding(10, Unit.PX);
                }
        
                return html;
            }
            
        }));

        checkpointsEditor.addListChangedCallback(new ListChangedCallback<Checkpoint>() {
            @Override
            public void listChanged(ListChangedEvent<Checkpoint> event) {
                
                setClean();
                
                ScenarioClientUtility.gatherPlacesOfInterestReferences();
                
                requestValidationAndFireDirtyEvent(getCondition(), checkpointValidation);
            }
        });
        
        checkpointsEditor.addEditCancelledCallback(new EditCancelledCallback() {
            
            @Override
            public void editCancelled() {
                setClean();
            }
        });
        
        checkpointsEditor.setRemoveItemDialogTitle("Remove Checkpoint");
        checkpointsEditor.setRemoveItemStringifier(new Stringifier<Checkpoint>() {
            
            @Override
            public String stringify(Checkpoint obj) {

                String placeName = obj.getPoint();
                
                if(StringUtils.isNotBlank(placeName)){
                    return "the checkpoint associated with the point "+bold(placeName).asString()+" (does not delete the underlying place of interest)";
                }else{
                    return "this checkpoint (does not delete the underlying place of interest)";
                }
            }
        });

        checkpointValidation = new WidgetValidationStatus(createButton,
                "You must have at least 1 checkpoint to measure progress. Please add a new checkpoint.");
        
        teamMemberPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                setDirty();
                
                String selectedRole = event.getValue();
                
                //update the backing data model with the referenced team member name
                if(StringUtils.isNotBlank(selectedRole)) {
                    getInput().setTeamMemberRef(selectedRole);
                    
                } else {
                    getInput().setTeamMemberRef(null);
                }
                
                //this condition's team references may have changed, so update the global reference map
                ScenarioClientUtility.gatherTeamReferences();

                /* Validate picker after it changed */
                teamMemberPicker.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }

    @Override
    protected void onEdit() {
        checkpointsEditor.setItems(getInput().getCheckpoint());
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            String memberRef = getInput().getTeamMemberRef();
            
            //the current training app requires a team member learner ID, so load any that are found
            teamMemberPicker.setValue(memberRef);
            
            //update the backing value in case the picker automatically provides a new value
            getInput().setTeamMemberRef(teamMemberPicker.getValue());
            
        } else {
            
            if(getInput().getTeamMemberRef() != null) {
                getInput().setTeamMemberRef(null);
            }
            
            teamMemberPicker.setValue(null);
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(checkpointValidation);
            }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int checkpointSize;
        if (checkpointValidation.equals(validationStatus)) {
            checkpointSize = checkpointsEditor.size();
            checkpointValidation.setValidity(checkpointSize > 0);
    }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(checkpointsEditor);
        childValidationComposites.add(teamMemberPicker);
    }
    
    @Override
    protected void setReadonly(boolean isReadonly) {
        checkpointsEditor.setReadonly(isReadonly);
        teamMemberPicker.setReadonly(isReadonly);
    }
}