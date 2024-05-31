/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Point;
import generated.dkf.PointRef;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A widget used to add and edit start locations.
 * 
 * @author sharrison
 */
public class WaypointRefItemEditor extends ItemEditor<PointRef> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static WaypointRefItemEditorUiBinder uiBinder = GWT.create(WaypointRefItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface WaypointRefItemEditorUiBinder extends UiBinder<Widget, WaypointRefItemEditor> {
    }
    
    /** An event binder used to allow this widget to receive global events */
    interface WaypointRefItemEditorEventBinder extends EventBinder<WaypointRefItemEditor> {
    }

    /** The event binder that handles global events for this widget */
    private static final WaypointRefItemEditorEventBinder eventBinder = GWT.create(WaypointRefItemEditorEventBinder.class);
    
    /** The event binder registration */
    private HandlerRegistration eventBinderRegistration;

    /** The waypoint picker */
    @UiField(provided = true)
    protected PlaceOfInterestPicker waypointPicker = new PlaceOfInterestPicker(Point.class);

    /**
     * Constructor.
     * 
     * @param inputPanel the {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to.
     *        Can't be null.
     */
    public WaypointRefItemEditor(final ConditionInputPanel<?> inputPanel) {
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        
        if (inputPanel == null) {
            throw new IllegalArgumentException("Input panel argument cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        waypointPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                inputPanel.setDirty();
            }
        });
        
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                final boolean attached = event.isAttached();
                final boolean hasHandler = eventBinderRegistration != null;

                /* add the event binder to this widget if it is attached and does not have the event
                 * binder already */
                if (attached && !hasHandler) {
                    eventBinderRegistration = eventBinder.bindEventHandlers(WaypointRefItemEditor.this,
                            SharedResources.getInstance().getEventBus());
                } else if (!attached && hasHandler) {
                    /* remove the event binder from this widget if it is detached */
                    eventBinderRegistration.removeHandler();
                }
            }
        });
    }
    
    /**
     * Sets the disallowed types to be hidden in the coordinate editor.
     * 
     * @param disallowedTypes the {@link CoordinateType types} to be hidden.  Can be empty/null
     * to hide nothing.
     */
    public void setDisallowedTypes(CoordinateType... disallowedTypes) {
        waypointPicker.setDisallowedTypes(disallowedTypes);
    }

    @Override
    protected void populateEditor(PointRef obj) {
        waypointPicker.setValue(obj.getValue());
    }

    @Override
    protected void applyEdits(PointRef obj) {
        obj.setValue(waypointPicker.getValue());
        
        //a waypoint reference may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(waypointPicker);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    protected boolean validate(PointRef pointRef) {
        String errorMsg = ScenarioValidatorUtility.validatePointRef(pointRef, waypointPicker.getDisallowedTypes());
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        waypointPicker.setReadonly(isReadonly);
    }
    
    @EventHandler
    protected void onPlaceOfInterestEditedEvent(PlaceOfInterestEditedEvent event){
        
        if(getParentItemListEditor() == null) {
            return;
        }
        
        //redraw the editor once it finishes editing in case places of interest have changed
        getParentItemListEditor().redrawListEditor(false);
        
        //if a point ref is currently being edited, revalidate it and the rest of the list
        if(getParentItemListEditor().isEditing()) {
            getParentItemListEditor().validateAll();   
        }
    }
}
