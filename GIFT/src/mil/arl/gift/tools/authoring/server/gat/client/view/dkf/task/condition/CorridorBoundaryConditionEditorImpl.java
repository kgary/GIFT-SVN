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
import java.util.ArrayList;
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
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.Path;
import generated.dkf.PathRef;
import generated.dkf.Segment;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.DeletePredicate;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for CorridorBoundary.
 */
public class CorridorBoundaryConditionEditorImpl extends ConditionInputPanel<CorridorBoundaryCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CorridorPostureConditionEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static CorridorBoundaryConditionEditorUiBinder uiBinder = GWT
            .create(CorridorBoundaryConditionEditorUiBinder.class);
    
    interface CorridorBoundaryConditionEditorImplEventBinder extends EventBinder<CorridorBoundaryConditionEditorImpl> {}

    /**
     * An event binder used to allow this widget to receive events from the shared event bus
     */
    private static final CorridorBoundaryConditionEditorImplEventBinder eventBinder = 
            GWT.create(CorridorBoundaryConditionEditorImplEventBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface CorridorBoundaryConditionEditorUiBinder extends UiBinder<Widget, CorridorBoundaryConditionEditorImpl> {
    }

    /**
     * The input field corresponding to the first coordinate value. This is currently hidden in the
     * editor since we do not use the value at run-time.
     */
    @UiField(provided = true)
    protected DecimalNumberSpinner bufferWidth = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.valueOf(Float.MAX_VALUE));

    /** Place of interest picker for the path to follow */
    @UiField(provided = true)
    protected PlaceOfInterestPicker pathPicker = new PlaceOfInterestPicker(Path.class);

    /** An editor used to modify this condition's list of segments */
	@UiField(provided = true)
    protected ItemListEditor<Segment> segmentListEditor = new ItemListEditor<Segment>(new SegmentItemEditor(this));

	/** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /**
     * Constructor
     */
    public CorridorBoundaryConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        pathPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                setClean();
                
                if(getInput().getPathRef() == null) {
                    getInput().setPathRef(new PathRef());
                }
                
                getInput().getPathRef().setValue(event.getValue());
                
                //the reference was changed, so update the reference map
                ScenarioClientUtility.gatherPlacesOfInterestReferences();
                
                Serializable path = ScenarioClientUtility.getPlaceOfInterestWithName(getInput().getPathRef().getValue());
                
                if(path instanceof Path) {
                    segmentListEditor.setItems(((Path) path).getSegment());
                    segmentListEditor.setVisible(true);
                    
                } else {
                    segmentListEditor.setVisible(false);
                }
                
                ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
            }
        });

        segmentListEditor.setFields(buildSegmentItemFields());
        segmentListEditor.setDeletePredicate(new DeletePredicate<Segment>() {
            
            @Override
            public boolean canDelete(Segment item) {
                return false;
            }
        });
        
        segmentListEditor.addListChangedCallback(new ListChangedCallback<Segment>() {
            
            @Override
            public void listChanged(ListChangedEvent<Segment> event) {
                setClean();
            }
        });
        
        segmentListEditor.addEditCancelledCallback(new EditCancelledCallback() {
            
            @Override
            public void editCancelled() {
                setClean();
            }
        });
        
        bufferWidth.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                setDirty();
                getInput().setBufferWidthPercent(event.getValue());
            }
        });
        
        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                setDirty();
                
                List<String> selectedRoles = event.getValue();
                
                if(selectedRoles != null && !selectedRoles.isEmpty()) {
                    
                    if(getInput().getTeamMemberRefs() == null) {
                        getInput().setTeamMemberRefs(new TeamMemberRefs());
                    }
                    
                    //update the backing data model with the list of referenced team members
                    getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(selectedRoles);
                    
                }else{
                    getInput().setTeamMemberRefs(null);
                }
                
                //this condition's team references may have changed, so update the global reference map
                ScenarioClientUtility.gatherTeamReferences();

                /* Validate picker after it changed */
                teamPicker.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }
    
    /**
     * Builds the item fields for the segment table.
     * 
     * @return the {@link ItemField item field columns} for the segment table.
     */
    private List<ItemField<Segment>> buildSegmentItemFields() {

        ItemField<Segment> segmentNameField = new ItemField<Segment>(null, "33%") {
            @Override
            public Widget getViewWidget(Segment item) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("segmentNameField.getViewWidget(" + item + ")");
                }

                return new HTML(item.getName());
            }
        };

        ItemField<Segment> startWaypointField = new ItemField<Segment>(null, "33%") {
            @Override
            public Widget getViewWidget(Segment item) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("startWaypointField.getViewWidget(" + item + ")");
                }

                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold("Start:")).appendHtmlConstant(" ");
                if (item.getStart() != null && item.getStart().getCoordinate() != null) {
                    sb.appendEscaped(ScenarioClientUtility.prettyPrintCoordinate(item.getStart().getCoordinate()));
                } else {
                    sb.appendEscaped("None");
                }

                return new BubbleLabel(sb.toSafeHtml());
            }
        };

        ItemField<Segment> endWaypointField = new ItemField<Segment>(null, "33%") {
            @Override
            public Widget getViewWidget(Segment item) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("endWaypointField.getViewWidget(" + item + ")");
                }

                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold("End:")).appendHtmlConstant(" ");
                if (item.getEnd() != null && item.getEnd().getCoordinate() != null) {
                    sb.appendEscaped(ScenarioClientUtility.prettyPrintCoordinate(item.getEnd().getCoordinate()));
                } else {
                    sb.appendEscaped("None");
                }

                return new BubbleLabel(sb.toSafeHtml());
            }
        };

        return Arrays.asList(segmentNameField, startWaypointField, endWaypointField);
    }

    @Override
    protected void onEdit() {
        
        if (getInput().getPathRef() == null) {
            getInput().setPathRef(new PathRef());
        }

        bufferWidth.setValue(
                getInput().getBufferWidthPercent() == null ? BigDecimal.ZERO : getInput().getBufferWidthPercent());

        // set list into the segment editor
        pathPicker.setValue(getInput().getPathRef().getValue());
    
        Serializable path = ScenarioClientUtility.getPlaceOfInterestWithName(getInput().getPathRef().getValue());
        
        if(path instanceof Path) {
            segmentListEditor.setItems(((Path) path).getSegment());
            segmentListEditor.setVisible(true);
            
        } else {
            segmentListEditor.setVisible(false);
        }
        

        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            //the current training app requires team member learner IDs, so load any that are found
            if(getInput().getTeamMemberRefs() == null) {
                getInput().setTeamMemberRefs(new TeamMemberRefs());
            }
            
            List<String> targetTeamMembers = new ArrayList<>(getInput().getTeamMemberRefs().getTeamMemberRef());
            
            teamPicker.setValue(targetTeamMembers);
            
            if(!targetTeamMembers.equals(teamPicker.getValue())){
                
                //the team picker removed some invalid team member names, so update the backing data model to match
                getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                
                if(teamPicker.getValue() != null) {
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(teamPicker.getValue());
                }
            }
            
        } else {
            
            if(getInput().getTeamMemberRefs() != null) {
                getInput().setTeamMemberRefs(null);
            }
            
            teamPicker.setValue(null);
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //no validation statuses to get
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(pathPicker);
        childValidationComposites.add(segmentListEditor);
        childValidationComposites.add(teamPicker);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        bufferWidth.setEnabled(!isReadonly);
        pathPicker.setReadonly(isReadonly);
        segmentListEditor.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
    }
    
    /**
     * Handles when the author has finished editing a place of interest
     * 
     * @param event the place of interest that was edited
     */
    @EventHandler
    protected void onPlaceOfInterestEdited(PlaceOfInterestEditedEvent event) {
        
        if(event.getPlace() != null) {
            
            Serializable path = event.getPlace();
            
            if(pathPicker.getValue() != null 
                    && pathPicker.getValue().equals(ScenarioClientUtility.getPlaceOfInterestName(event.getPlace()))) {
            
                //if the selected path has been changed, update the segment list accordingly
                if(path instanceof Path) {
                    segmentListEditor.setItems(((Path) path).getSegment());
                    segmentListEditor.setVisible(true);
                    
                } else {
                    segmentListEditor.setVisible(false);
                }
            }
        }
    }
}