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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.tagsinput.client.event.ItemRemovedEvent;
import org.gwtbootstrap3.extras.tagsinput.client.event.ItemRemovedHandler;
import org.gwtbootstrap3.extras.tagsinput.client.ui.TagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.StringDataset;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.CorridorPostureCondition;
import generated.dkf.Path;
import generated.dkf.PathRef;
import generated.dkf.Segment;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.DeletePredicate;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;

/**
 * The condition impl for CorridorPosture.
 */
public class CorridorPostureConditionEditorImpl extends ConditionInputPanel<CorridorPostureCondition> {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(CorridorPostureConditionEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static CorridorPostureConditionEditorUiBinder uiBinder = GWT
            .create(CorridorPostureConditionEditorUiBinder.class);

    interface CorridorPostureConditionEditorImplEventBinder extends EventBinder<CorridorPostureConditionEditorImpl> {}

    /**
     * An event binder used to allow this widget to receive events from the shared event bus
     */
    private static final CorridorPostureConditionEditorImplEventBinder eventBinder = 
            GWT.create(CorridorPostureConditionEditorImplEventBinder.class);
    
    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface CorridorPostureConditionEditorUiBinder extends UiBinder<Widget, CorridorPostureConditionEditorImpl> {
    }

    /**
     * The panel surrounding the multiple select component {@link #postureMultipleSelect}. This is
     * required for validation because the multiple select's getElement() is returning the actual
     * select component which is hidden by bootstrap. The validation's addStyle() gets automatically
     * applied to the surrounding div, but removeStyle() does not propogate up. Therefore, we are
     * just using this panel to show the validation styling for this multiple select component.
     */
    @UiField
    protected SimplePanel postureSelectPanel;
    
    /** The multiple select dropdown for postures */
    @UiField
    protected MultipleSelect postureMultipleSelect;

    /** The panel that contains the posture tags */
    @UiField
    protected FlowPanel postureTagPanel;

    /** The posture tag input. Displays the collection of the selected postures */
    @UiField
    protected TagsInput postureTagInput;

    /** Place of interest picker for the path to follow */
    @UiField(provided = true)
    protected PlaceOfInterestPicker pathPicker = new PlaceOfInterestPicker(Path.class);
    
    /** An editor used to modify this condition's list of segments */
    @UiField(provided = true)
    protected ItemListEditor<Segment> segmentListEditor = new ItemListEditor<Segment>(new SegmentItemEditor(this));
    
    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());
    
    /** used to block the removal of postures from tags input panel when read only */
    @UiField
    protected BlockerPanel postureTagPanelBlocker;

    /** The container for showing validation messages for having no selected postures. */
    private final WidgetValidationStatus postureValidationStatus;

    /**
     * Constructor
     */
    public CorridorPostureConditionEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CorridorPostureConditionEditorImpl()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        postureValidationStatus = new WidgetValidationStatus(postureSelectPanel,
                "The are no postures to maintain. Add a posture that the learner should maintain.");

        initPostures();

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
     * Initializes the posture widgets
     */
    private void initPostures() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initPostures()");
        }

        List<PostureEnum> postures = new ArrayList<PostureEnum>(PostureEnum.VALUES());
        Collections.sort(postures, new Comparator<PostureEnum>() {
            @Override
            public int compare(PostureEnum o1, PostureEnum o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        ArrayList<String> postureNames = new ArrayList<String>();
        for (PostureEnum postureEnum : postures) {
            postureNames.add(postureEnum.getDisplayName());
            Option option = new Option();
            option.setText(postureEnum.getDisplayName());
            option.setValue(postureEnum.getName());
            postureMultipleSelect.add(option);
        }

        StringDataset dataset = new StringDataset(postureNames);
        postureTagInput.setDatasets(dataset);
        postureTagInput.reconfigure();

        postureTagPanel.setVisible(false);

        postureMultipleSelect.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("postureMultipleSelect.onValueChange(" + event.getValue() + ")");
                }

                postureTagInput.removeAll();
                postureTagInput.add(event.getValue());
                postureTagInput.refresh();

                postureTagPanel.setVisible(!postureTagInput.getItems().isEmpty());

                /* Update the data model with the new value */
                getInput().getPostures().getPosture().clear();
                getInput().getPostures().getPosture().addAll(event.getValue());
                
                requestValidationAndFireDirtyEvent(getCondition(), postureValidationStatus);
            }
        });

        postureTagInput.addItemRemovedHandler(new ItemRemovedHandler<String>() {
            @Override
            public void onItemRemoved(ItemRemovedEvent<String> event) {
                List<String> selectedItems = new ArrayList<String>();
                for (Option option : postureMultipleSelect.getSelectedItems()) {
                    // get all selected except the one that was removed
                    if (!StringUtils.equals(option.getText(), event.getItem())) {
                        selectedItems.add(option.getValue());
                    }
                }

                // update multiple select with the updated selected items list
                postureMultipleSelect.setValue(selectedItems, true);
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
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        if (getInput().getPathRef() == null) {
            getInput().setPathRef(new PathRef());
        }

        postureMultipleSelect.deselectAll();

        HashSet<String> posturesInCondition = new HashSet<String>(getInput().getPostures().getPosture());
        for (String selected : posturesInCondition) {
            for (Option option : postureMultipleSelect.getItems()) {
                if (StringUtils.equals(selected, option.getText())) {
                    option.setSelected(true);
                    break;
                }
            }
        }

        postureTagInput.removeAll();
        postureTagInput.add(postureMultipleSelect.getValue());
        postureTagInput.refresh();

        postureTagPanel.setVisible(!postureTagInput.getItems().isEmpty());

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
        validationStatuses.add(postureValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if (postureValidationStatus.equals(validationStatus)) {
            postureValidationStatus.setValidity(!postureMultipleSelect.getValue().isEmpty());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(pathPicker);
        childValidationComposites.add(segmentListEditor);
        childValidationComposites.add(teamPicker);
    }
    
    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }
        
        postureMultipleSelect.setEnabled(!isReadonly);
        postureMultipleSelect.setVisible(!isReadonly);
        postureSelectPanel.setVisible(!isReadonly);
        if(isReadonly){
            postureTagPanelBlocker.block();  // block click events to prevent removing tags
        }else{
            postureTagPanelBlocker.unblock();
        }
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