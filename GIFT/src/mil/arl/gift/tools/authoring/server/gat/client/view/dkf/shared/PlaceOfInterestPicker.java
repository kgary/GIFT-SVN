/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A widget used to pick a place of interest from the scenario's list of places of interests. This widget can also be used to create new
 * places, edit existing places, or remove existing places.
 *
 * @author sharrison
 */
public class PlaceOfInterestPicker extends ScenarioValidationComposite implements HasValue<String> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PlaceOfInterestPicker.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static PlaceOfInterestPickerUiBinder uiBinder = GWT.create(PlaceOfInterestPickerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface PlaceOfInterestPickerUiBinder extends UiBinder<Widget, PlaceOfInterestPicker> {
    }

    /** An event binder used to allow this widget to receive global events */
    interface PlaceOfInterestPickerEventBinder extends EventBinder<PlaceOfInterestPicker> {
    }

    /** The event binder that handles global events for this widget */
    private static final PlaceOfInterestPickerEventBinder eventBinder = GWT.create(PlaceOfInterestPickerEventBinder.class);

    /** A text box used to search for places of interest to select */
    @UiField
    protected TextBox placeSelect;

    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;
    
    /** disallowed types to be hidden in the coordinate editor.  Can be null or empty. */
    private CoordinateType[] disallowedCoordinateTypes = null;

    /** The button used to jump to the selected task/concept page */
    @UiField(provided = true)
    protected EnforcedButton nodeJumpButton = new EnforcedButton(IconType.EXTERNAL_LINK, "", "Navigates to the places of interest page",
            new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    if(!nodeJumpButton.isEnabled()) {
                        return;
                    }

                    ScenarioEventUtility.fireJumpToPlacesOfInterest();

                    //hide the place of interest selector so that it doesn't block the editor that the author jumps to
                    PlaceOfInterestSelectionPanel.hideSelector();
                }
            });
    
    @UiField(provided = true)
    protected WrapButton wrapButton = new WrapButton() {
        
        @Override
        public Serializable getOriginalPlaceToWrap() {
            return ScenarioClientUtility.getPlaceOfInterestWithName(getValue());
        }
    };

    /** The collapse containing a panel for selecting a {@link Point} */
    @UiField
    protected Collapse selectorPanel;

    /** The label that is used to annotate the picker field */
    @UiField
    protected HTML label;

    /** Validation container for when the {@link PlaceOfInterestPicker} has no place of interest selected. */
    private final WidgetValidationStatus placeSelectedValidation;
    
    /** Validation container for when the {@link PlaceOfInterestPicker} has a place selected that uses a disallowed coordinate type */
    private final WidgetValidationStatus placeAllowedCoordinatesValidation;

    /** The last value that was entered. Used to revert changes when the author enters an invalid place of interest reference. */
    private String lastValue = null;

    /** The event binder registration */
    private com.google.web.bindery.event.shared.HandlerRegistration eventBinderRegistration;

    /** The types of places that this picker allows the author to pick from. If empty, all types are allowed */
    private Class<?>[] acceptedPlaceTypes = null;

    /** Whether or not this picker should allow the author to edit places of interest within the drop down selector */
    private boolean poiEditingEnabled = true;

    /**
     * Creates a new place of interest picker and initializes its event handling logic
     */
    public PlaceOfInterestPicker(){

        //GWT's compiler doesn't seem to pick up on the fact that the vararg constructor doesn't need an argument, so we need to pass one
        this(new Class<?>[0]);
    }

    /**
     * Creates a new place of interest picker and initializes its event handling logic
     *
     * @param acceptedPlaceTypes the types of places of interest that the author can select. Cannot be null. If unspecified,
     * ALL types of places of interest will be available to select. E.g. generated.dkf.Point, generated.dkf.Path, generated.dkf.Area
     */
    public PlaceOfInterestPicker(Class<?>... acceptedPlaceTypes) {

        if(acceptedPlaceTypes == null) {
            throw new IllegalArgumentException("The accepted place of interest types cannot be null.");
        }

        this.acceptedPlaceTypes = acceptedPlaceTypes;

        initWidget(uiBinder.createAndBindUi(this));

        eventBinderRegistration = eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                final boolean attached = event.isAttached();
                final boolean hasHandler = eventBinderRegistration != null;

                /* add the event binder to this picker if it is attached and does not have the event
                 * binder already */
                if (attached && !hasHandler) {
                    eventBinderRegistration = eventBinder.bindEventHandlers(PlaceOfInterestPicker.this,
                            SharedResources.getInstance().getEventBus());
                } else if (!attached && hasHandler) {
                    /* remove the event binder from this picker if it is detached */
                    eventBinderRegistration.removeHandler();
                }
            }
        });

        placeSelectedValidation = new WidgetValidationStatus(placeSelect,
                "You must have a place of interest selected. Please select a place of interest. If none exist, you need to create one.");
        
        placeAllowedCoordinatesValidation = new WidgetValidationStatus(placeSelect,
                "The place of interest selected uses a coordinate type that is not allowed here.");

        placeSelect.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                //show the place of interest selection dropdown whenever the search box is selected
                PlaceOfInterestSelectionPanel.showSelector(PlaceOfInterestPicker.this);
            }
        });
        placeSelect.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {

                if(scheduledFilter == null) {

                    // Schedule a filter operation for the list. We don't want to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {

                        @Override
                        public void execute() {

                            //update the filter for the selection dropdown
                            PlaceOfInterestSelectionPanel.loadAndFilterPlacesOfInterest();

                            //allow the filter operation to be applied again, since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        placeSelect.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                //select all of the search box's text when it gains focus so that it's easier for the author to clear out
                placeSelect.selectAll();
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
        disallowedCoordinateTypes = disallowedTypes;
        PlaceOfInterestSelectionPanel.setDisallowedTypes(disallowedTypes);
    }
    
    public CoordinateType[] getDisallowedTypes() {
        return disallowedCoordinateTypes;
    }

    /**
     * Handles the selection changes for the places of interest.
     *
     * @param event the value change event containing the new selection
     */
    @UiHandler("placeSelect")
    protected void onPlaceSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onPlaceSelectChanged.onValueChange(" + event.getValue() + ")");
        }

        Serializable referencedPlaceOfInterest = ScenarioClientUtility.getPlaceOfInterestWithName(event.getValue());

        if(referencedPlaceOfInterest != null) {

            //the author has chosen a valid place of interest, so update this widget's value
            setValue(event.getValue(), true);

        } else {
            //the author has not chosen a valid place of interest, so revert to the last entered value
            placeSelect.setValue(lastValue);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Will return an empty string when the placeholder value is shown visually
     */
    @Override
    public String getValue() {
        return placeSelect.getValue();
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {

        lastValue = value;

        placeSelect.setValue(StringUtils.isBlank(value) ? null : value);
        requestValidation(placeSelectedValidation, placeAllowedCoordinatesValidation);

        if (fireEvents) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(placeSelectedValidation);
        validationStatuses.add(placeAllowedCoordinatesValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (placeSelectedValidation.equals(validationStatus)) {
            placeSelectedValidation.setValidity(StringUtils.isNotBlank(getValue()));
            final String selectedValue = getValue();
            if (StringUtils.isBlank(selectedValue)) {
                placeSelectedValidation.setInvalid();
                return;
            }

            // must reference a place of interest
            boolean found = ScenarioClientUtility.getPlaceOfInterestWithName(selectedValue) != null;
            placeSelectedValidation.setValidity(found);
        }else if(placeAllowedCoordinatesValidation.equals(validationStatus)){
            
            final String selectedValue = getValue();
            if (StringUtils.isBlank(selectedValue)) {
                // place hasn't been selected yet so can't violate disallowed coordinate type
                placeAllowedCoordinatesValidation.setValid();
                return;
            }
         
            Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(selectedValue);            
            placeAllowedCoordinatesValidation.setValidity(
                    !ScenarioValidatorUtility.containsDisallowedCoordinate(poi, disallowedCoordinateTypes));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(PlaceOfInterestSelectionPanel.getListValidation());
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        placeSelect.setEnabled(!isReadonly);
    }

    /**
     * Gets the text box used to enter search text
     *
     * @return the search text box
     */
    ValueBoxBase<String> getTextBox() {
        return placeSelect;
    }

    /**
     * Gets a {@link Widget} reference to the {@link #placeSelect} textbox. This
     * method is used to expose the {@link Widget} for purpose of validation
     * highlighting.
     *
     * @return The {@link Widget} reference to {@link #placeSelect}. Can't be
     *         null.
     */
    public Widget getTextBoxRef() {
        return placeSelect;
    }

    /**
     * Updates the place of interest picker when the place of interest it is referencing has been deleted
     *
     * @param event an event indicating that a place of interest is deleted
     */
    @EventHandler
    protected void onPlaceOfInterestRenamedEvent(PlaceOfInterestRenamedEvent event) {
        /* if the old place of interest name is the same as this place of interest picker value, then update it to the
         * new place of interest name */
        if (StringUtils.isNotBlank(event.getOldName()) && StringUtils.equals(event.getOldName(), getValue())) {
            setValue(event.getNewName());
            requestValidation(placeSelectedValidation, placeAllowedCoordinatesValidation);
        }
    }
    
    @EventHandler
    protected void onPlaceOfInterestEditedEvent(PlaceOfInterestEditedEvent event){
        // if the edited place is the same as this place of interest picker value, check to make sure the place
        // coordinate type is still of the allowed types
        Serializable editedPOI = event.getPlace();
        String editedPOIName = ScenarioClientUtility.getPlaceOfInterestName(editedPOI);
        if(editedPOIName != null && editedPOIName.equals(getValue())){
            requestValidation(placeAllowedCoordinatesValidation);
        }
    }

    /**
     * Gets the collapseable panel that the selector should be placed in when it is shown
     *
     * @return the panel where the selector should be placed
     */
    Collapse getSelectorPanel() {
        return selectorPanel;
    }

    /**
     * Sets the label text for this place of interest picker
     *
     * @param text the plain text to use as the label
     */
    public void setLabel(String text) {
        label.setText(text);
    }

    /**
     * Sets the label text for this place of interest picker and renders it as HTML
     *
     * @param html the HTML to use as the label
     */
    public void setLabel(SafeHtml html) {
        label.setHTML(html, Direction.LTR);
    }

    /**
     * Gets the types of places of interest that this picker allows the author to select. If empty, the author can select
     * any type of place of interest.
     *
     * @return the types of places of interest that this picker allows the author to select. Cannot be null. If empty, all types of
     * places of interest are available to select.
     */
    public Class<?>[] getAcceptedPlaceTypes(){
        return acceptedPlaceTypes;
    }

    /**
     * Sets whether the author can jump to the GIFT wrap overlay or to the global places of interest panel
     * 
     * @param enabled whether the author can jump to other locations from this picker
     */
    public void setJumpEnabled(boolean enabled) {
        wrapButton.setVisible(enabled);
        wrapButton.setEnabled(enabled);
        nodeJumpButton.setVisible(enabled);
        nodeJumpButton.setEnabled(enabled);
    }
    
    /**
     * Sets whether or not this picker should allow the author to edit places of interest within the drop down selector
     * 
     * @param enabled whether the should be allowed to author to edit places of interest within the drop down selector
     */
    public void setPoiEditingEnabled(boolean enabled) {
        poiEditingEnabled = enabled;
    }
    
    /**
     * Gets whether or not this picker should allow the author to edit places of interest within the drop down selector
     * 
     * @return whether the should be allowed to author to edit places of interest within the drop down selector
     */
    public boolean isPoiEditingEnabled() {
        return poiEditingEnabled;
    }
}
