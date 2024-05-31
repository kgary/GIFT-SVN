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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.EnterAreaCondition;
import generated.dkf.Entrance;
import generated.dkf.Inside;
import generated.dkf.Outside;
import generated.dkf.Point;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for Enter Area.
 */
public class EnterAreaConditionEditorImpl extends ConditionInputPanel<EnterAreaCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EnterAreaConditionEditorImpl.class.getName());

    /** The ui binder. */
    private static EnterAreaConditionEditorUiBinder uiBinder = GWT.create(EnterAreaConditionEditorUiBinder.class);

    /** The item list editor table for the entrances */
    @UiField(provided = true)
    protected ItemListEditor<Entrance> entranceListEditor = new ItemListEditor<Entrance>(new EntranceItemEditor(this));

    /** Team member picker used to determine the learner role that this condition should assess */
    @UiField(provided = true)
    protected TeamMemberPicker teamMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The container for showing validation messages for having no selected entrances. */
    private final WidgetValidationStatus entranceValidationStatus;

    /**
     * The Interface EnterAreaConditionEditorUiBinder.
     */
    interface EnterAreaConditionEditorUiBinder extends UiBinder<Widget, EnterAreaConditionEditorImpl> {
    }

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public EnterAreaConditionEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("EnterAreaConditionEditorImpl()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        entranceListEditor.setTableLabel("Entrances that the learner should enter:");
        entranceListEditor.setPlaceholder("No entrances have been created.");
        entranceListEditor.setFields(buildEntranceItemFields());
        entranceListEditor.setDraggable(true);
        entranceListEditor.setRemoveItemDialogTitle("Remove Entrance");
        entranceListEditor.setRemoveItemStringifier(new Stringifier<Entrance>() {
            @Override
            public String stringify(Entrance obj) {
                return "the entrance "+bold(obj.getName()).asString()+" that starts at "+
                        bold(obj.getOutside().getPoint()).asString()+" and ends at "+bold(obj.getInside().getPoint()).asString();
            }
        });

        entranceListEditor.addListChangedCallback(new ListChangedCallback<Entrance>() {
            @Override
            public void listChanged(ListChangedEvent<Entrance> event) {
                setClean();
                requestValidationAndFireDirtyEvent(getCondition(), entranceValidationStatus);
            }
        });
        
        entranceListEditor.addEditCancelledCallback(new EditCancelledCallback() {
            
            @Override
            public void editCancelled() {
                setClean();
            }
        });

        Widget entranceAddAction = entranceListEditor.addCreateListAction("Click here to add a new entrance location",
                buildEntranceCreateAction());

        entranceValidationStatus = new WidgetValidationStatus(entranceAddAction,
                "There are no entrances. Add an entrance location.");
        
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

    /**
     * Builds the item fields for the entrance table.
     * 
     * @return the {@link ItemField item field columns} for the entrance table.
     */
    private List<ItemField<Entrance>> buildEntranceItemFields() {

        ItemField<Entrance> entranceNameField = new ItemField<Entrance>("Name", "25%") {
            @Override
            public Widget getViewWidget(Entrance item) {
                return new HTML(item.getName());
            }
        };

        ItemField<Entrance> startWaypointField = new ItemField<Entrance>("Start", "25%") {
            @Override
            public Widget getViewWidget(Entrance item) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold("Start:")).appendHtmlConstant(" ");
                if (item.getOutside().getPoint() != null) {
                    sb.appendEscaped(item.getOutside().getPoint());
                } else {
                    sb.appendEscaped("None");
                }

                return new BubbleLabel(sb.toSafeHtml());
            }
        };

        ItemField<Entrance> endWaypointField = new ItemField<Entrance>("End", "25%") {
            @Override
            public Widget getViewWidget(Entrance item) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold("End:")).appendHtmlConstant(" ");
                if (item.getInside().getPoint() != null) {
                    sb.appendEscaped(item.getInside().getPoint());
                } else {
                    sb.appendEscaped("None");
                }

                return new BubbleLabel(sb.toSafeHtml());
            }
        };

        ItemField<Entrance> assessmentField = new ItemField<Entrance>("Assessment", "25%") {
            @Override
            public Widget getViewWidget(Entrance item) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold("End:")).appendHtmlConstant(" ").appendEscaped(item.getAssessment());
                return new BubbleLabel(sb.toSafeHtml());
            }
        };

        return Arrays.asList(entranceNameField, startWaypointField, endWaypointField, assessmentField);
    }

    /**
     * Builds the create action for the entrance table.
     * 
     * @return the {@link CreateListAction} for the entrance table.
     */
    private CreateListAction<Entrance> buildEntranceCreateAction() {
        return new CreateListAction<Entrance>() {

            private static final String entranceNameTemplate = "New Entrance ";

            @Override
            public Entrance createDefaultItem() {
                setDirty();
                
                List<Serializable> placesOfInterest = ScenarioClientUtility.getUnmodifiablePlacesOfInterestList();

                Inside inside = new Inside();
                inside.setProximity(BigDecimal.ZERO);

                Outside outside = new Outside();
                outside.setProximity(BigDecimal.ZERO);
                
                if (!placesOfInterest.isEmpty()) {
                    
                    for(Serializable placeOfInterest : placesOfInterest){
                        
                        if(placeOfInterest instanceof Point){
                            
                            if(inside.getPoint() == null){
                                Point pointStart = (Point) placeOfInterest;
                                inside.setPoint(pointStart.getName());
                                
                                //just in case there is no second point type object in the list
                                outside.setPoint(pointStart.getName());
                            }else{
                                Point pointEnd = (Point) placeOfInterest;
                                outside.setPoint(pointEnd.getName());
                                break;
                            }
                            
                        }
                    }

                }

                int i = 1;
                boolean reLoop;
                String entranceName;
                do {
                    reLoop = false;
                    entranceName = entranceNameTemplate + i++;
                    for (Entrance existingEntrance : getInput().getEntrance()) {
                        if (StringUtils.equals(entranceName, existingEntrance.getName())) {
                            reLoop = true;
                            break;
                        }
                    }
                } while (reLoop);

                Entrance entrance = new Entrance();
                entrance.setAssessment(AssessmentLevelEnum.AT_EXPECTATION.getName());
                entrance.setInside(inside);
                entrance.setName(entranceName);
                entrance.setOutside(outside);

                return entrance;
            }
        };
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        // set list into the entrance editor
        entranceListEditor.setItems(getInput().getEntrance());
        
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
        validationStatuses.add(entranceValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int listSize;
        if (entranceValidationStatus.equals(validationStatus)) {
            listSize = entranceListEditor.size();
            entranceValidationStatus.setValidity(listSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(entranceListEditor);
        childValidationComposites.add(teamMemberPicker);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        entranceListEditor.setReadonly(isReadonly);
        teamMemberPicker.setReadonly(isReadonly);
    }
}