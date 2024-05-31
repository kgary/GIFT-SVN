/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Area;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.Path;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReferenceList;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemFormatter;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The panel that acts as the dropdown for {@link PlaceOfInterestPicker place of interest pickers} and provides the list of 
 * places of interest that's used to allow authors to select, add, edit, and remove places of interest.
 * <br/><br/>
 * This panel is absolutely positioned relative to its associated place of interest picker so that it moves relative to the picker's
 * rendered position, even when the author scrolls the page. At a basic level, the behavior of this panel mimics that of
 * GWT's PopupPanel, only in this case, the panel is attached to the picker itself rather than to the RootPanel.
 * 
 * @author nroberts
 *
 */
public class PlaceOfInterestSelectionPanel extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PlaceOfInterestSelectionPanel.class.getName());
    
    /** The ui binder. */
    private static PlaceOfInterestSelectionPanelUiBinder uiBinder = GWT.create(PlaceOfInterestSelectionPanelUiBinder.class);

    /** The Interface PlaceofInterestPanelUiBinder */
    interface PlaceOfInterestSelectionPanelUiBinder extends UiBinder<Widget, PlaceOfInterestSelectionPanel> {
    }

    /** The singleton instance of this class */
    private static PlaceOfInterestSelectionPanel instance;
    
    /** The place of interest picker that this panel is currently being shown for */
    private static PlaceOfInterestPicker currentPicker;
    
    /** A list of places of interest that contain one or more search items*/
    private static ArrayList<Serializable> placesOfInterestMatchingTerms = new ArrayList<Serializable>();
    
    /** A panel with buttons used to filter the types of places of interest shown */
    @UiField
    protected PlaceOfInterestFilterPanel filterPanel;

    /** The editor used to edit items in the list of places of interest*/
    private PlaceOfInterestItemEditor itemEditor = new PlaceOfInterestItemEditor();
    
    /** disallowed types to be hidden in the coordinate editor.  Can be null or empty. */
    private static CoordinateType[] disallowedCoordinateTypes = null;
    
    /** The list of places of interest that authors can pick from. */
    @UiField(provided = true)
    protected ItemListEditor<PlaceOfInterestWrapper> placeOfInterestList = new ItemListEditor<PlaceOfInterestWrapper>(itemEditor) {
        
        @Override
        public void remove(final PlaceOfInterestWrapper wrapper) {
            
            final Serializable element = wrapper.getPlaceOfInterest();
            
            final List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(element);
            
            if(references != null && !references.isEmpty()) {
                
                final PlaceOfInterestReferenceList referenceList = new PlaceOfInterestReferenceList();
                referenceList.getListEditor().replaceItems(references);
                referenceList.setOnJumpCommand(new Command() {
                    
                    @Override
                    public void execute() {
                        
                        //close the current prompt if the author jumps to a condition
                        OkayCancelDialog.cancel();
                    }
                });
                
                //defer the next prompt so that the previous one can close and so the reference list can be populated
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        int refCount = 0;
                        
                        for(PlaceOfInterestReference reference : references) {
                            refCount += reference.getReferenceCount();
                        }
                        
                        //this place of interest has references, so we need to ask the author if they want to remove these references first
                        OkayCancelDialog.show(
                                "Remove references?", 
                                refCount +" condition(s) are referencing this place of interest. These references must be removed in order to "
                                + "delete this place of interest.<br/><br/>Do you want to remove these references and continue with the delete? "
                                + "This will likely make the affected conditions invalid until they are assigned with different "
                                + "places of interest.<br/><br/>Conditions that reference this place of interest:", 
                                referenceList,
                                "Remove References and Continue Delete", 
                                new OkayCancelCallback() {
                                    
                                    @Override
                                    public void okay() {
                                        
                                        //this place of interest's references have been removed, so remove the place of interest itself now
                                        delete(wrapper);
                                        
                                        String name = ScenarioClientUtility.getPlaceOfInterestName(element);
                                        ScenarioClientUtility.updatePlaceOfInterestReferences(name, null);
                                    }
                                    
                                    @Override
                                    public void cancel() {
                                        
                                        if(logger.isLoggable(Level.FINE)){
                                            logger.fine("User cancelled removing references");
                                        }
                                    }
                                }
                        ); 
                    }
                });
                
            } else {
                
                //this place of interest has no references, so just remove it outright
                delete(wrapper);
            }
        }
        
        /**
         * Deletes the given place of interest regardless of any references it may have
         * 
         * @param element the place of interest to delete
         */
        private void delete(PlaceOfInterestWrapper wrapper) {
            
            Serializable element = wrapper.getPlaceOfInterest();
            
            super.remove(wrapper);
            
            // notify listeners that the place of interest was removed
            ScenarioEventUtility.fireDeleteScenarioObjectEvent(element, null);
        }
    };

    /**
     * Creates a new place of interest selection panel, initializes its list of places of interest, and sets up its event handlers
     */
    private PlaceOfInterestSelectionPanel() {
        
        initWidget(uiBinder.createAndBindUi(this));

        addStyleName("contextMenu");
        
        //initialize the filter panel
        resetTypeFilter();
        
        //initialize the place of interest list
        placeOfInterestList.setPlaceholder("No places of interest were found.");
        placeOfInterestList.setRemoveItemDialogTitle("Delete place of interest?");
        placeOfInterestList.setRemoveItemStringifier(new Stringifier<PlaceOfInterestWrapper>() {
            
            @Override
            public String stringify(PlaceOfInterestWrapper wrapper) {
                
                Serializable obj = wrapper.getPlaceOfInterest();
                
                String name = ScenarioClientUtility.getPlaceOfInterestName(obj);
                
                StringBuilder builder = new StringBuilder()
                        .append("<b>")
                        .append((name != null) ? name : "this place of interest")
                        .append("</b>");
                
                return builder.toString();
            }
        });
        
        placeOfInterestList.setFields(buildListFields());
        
        placeOfInterestList.addCreateListAction("Click here to add a new place of interest", new CreateListAction<PlaceOfInterestWrapper>() {
            @Override
            public PlaceOfInterestWrapper createDefaultItem() {
                
                PlaceOfInterestWrapper newWrapper = new PlaceOfInterestWrapper();
                
                if(currentPicker != null 
                        && currentPicker.getAcceptedPlaceTypes().length == 1) {
                    
                    //if the picker limits the user to certain types of places, construct a place of the appropriate type by default
                    Class<?> acceptedType = currentPicker.getAcceptedPlaceTypes()[0];
                    
                    if(acceptedType.equals(Point.class)) {
                        newWrapper.setPlaceOfInterest(new Point());
                        
                    } else if(acceptedType.equals(Path.class)) {
                        newWrapper.setPlaceOfInterest(new Path());
                        
                    } else if(acceptedType.equals(Area.class)) {
                        newWrapper.setPlaceOfInterest(new Area());   
                    }
                }
                
                return newWrapper;
            }
        });
        
        placeOfInterestList.addListChangedCallback(new ListChangedCallback<PlaceOfInterestWrapper>() {
            
            @Override
            public void listChanged(ListChangedEvent<PlaceOfInterestWrapper> event) {
                
                if(ListAction.ADD.equals(event.getActionPerformed())) {

                    Serializable placeOfInterest = event.getAffectedItems().get(0).getPlaceOfInterest();
                    ScenarioEventUtility.fireCreateScenarioObjectEvent(placeOfInterest);

                    String name = ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest);
                    currentPicker.setValue(name, true);

                    loadAndFilterPlacesOfInterest();
                    
                } else if(ListAction.EDIT.equals(event.getActionPerformed())) {
                    
                    for(PlaceOfInterestWrapper wrapper : event.getAffectedItems()) {
                        SharedResources.getInstance().getEventBus().fireEvent(new PlaceOfInterestEditedEvent(wrapper.getPlaceOfInterest()));
                        ScenarioClientUtility.updatePlaceOfInterestReferences(
                                ScenarioClientUtility.getPlaceOfInterestName(wrapper.getPlaceOfInterest()));
                    }
                }
            }
        });
        
        placeOfInterestList.setItemFormatter(new ItemFormatter<PlaceOfInterestWrapper>() {
            
            @Override
            public void format(PlaceOfInterestWrapper item, TableRowElement rowElement) {
                
                if(currentPicker != null && !currentPicker.isPoiEditingEnabled()) {
                    
                    //if the current picker doesn't allow places of interest to be edited, make sure they can't be opened for editing
                    rowElement.addClassName("poiSelectorEditingDisabled");
                    
                } else {
                    
                    //otherwise, allow places of interest to be edited normally
                    rowElement.removeClassName("poiSelectorEditingDisabled");
                }
            }
        });
        
        filterPanel.addValueChangeHandler(new ValueChangeHandler<List<Class<?>>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<Class<?>>> event) {
                
                if(filterPanel.isVisible()) {
                    
                    itemEditor.setAuthorablePlaceTypes(event.getValue());
                    
                    //if the type filter is visible, update the list whenever its values are changed
                    loadAndFilterPlacesOfInterest();
                }
            }
        });
        
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                // Nick: This logic imitates the auto-hiding logic in
                // PopupPanel.previewNativeEvent()
                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());
                boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent) || eventTargetsPartner(nativeEvent);
                if (eventTargetsPopupOrPartner) {
                    event.consume();
                }

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    if (!eventTargetsPopupOrPartner) {
                        hideSelector();
                        return;
                    }
                    break;
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
    public static void setDisallowedTypes(CoordinateType... disallowedTypes) {
        disallowedCoordinateTypes = disallowedTypes;
        getInstance().itemEditor.setDisallowedTypes(disallowedTypes);
    }
    
    /**
     * Builds the fields for the list of places of interest
     * 
     * @return the list of fields
     */
    private List<ItemField<PlaceOfInterestWrapper>> buildListFields() {
        
        List<ItemField<PlaceOfInterestWrapper>> fields = new ArrayList<>();
        
        fields.add(new ItemField<PlaceOfInterestWrapper>() {
            
            @Override
            public Widget getViewWidget(final PlaceOfInterestWrapper wrapper) {
                
                //allow the author to select this place of interest in the accompanying place of interest picker via a button
                Button selectButton = new Button();
                selectButton.setText("Select");
                selectButton.getElement().setAttribute("style", 
                        "padding: 3px 10px; border-radius: 20px; margin-right: -15px; pointer-events: all;");
                selectButton.addMouseDownHandler(new MouseDownHandler() {
                    
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        event.stopPropagation();
                        
                        if(currentPicker != null) {
                            
                            Serializable item = wrapper.getPlaceOfInterest();
                            
                            //update the picker with the selected place of interest
                            String name = ScenarioClientUtility.getPlaceOfInterestName(item);
                            currentPicker.setValue(name, true);
                            hideSelector();
                        }
                    }
                });
                
                selectButton.setType(ButtonType.PRIMARY);
                
                return selectButton;
            }
        });
        
        fields.add(new ItemField<PlaceOfInterestWrapper>() {
            @Override
            public Widget getViewWidget(PlaceOfInterestWrapper wrapper) {
                
                FlowPanel panel = new FlowPanel();
                panel.getElement().getStyle().setMarginLeft(-15, Unit.PX);
                
                Serializable item = wrapper.getPlaceOfInterest();
                
                Widget iconWidget = PlacesOfInterestPanel.getPlaceIcon(item.getClass());
                
                if(iconWidget != null) {
                    panel.add(iconWidget);
                }
                
                String name = ScenarioClientUtility.getPlaceOfInterestName(item);
                InlineLabel label = new InlineLabel(name);
                label.setWidth("150px");
                label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
                label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                label.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                label.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
                label.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
                
                if (!placesOfInterestMatchingTerms.isEmpty() && placesOfInterestMatchingTerms.contains(item)) {
                    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    label.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
                }
                
                StringBuilder tooltipTitle = new StringBuilder()
                .append(name);
                
                if(item instanceof Point){
                    //show the coordinate of the point in a tooltip
                    
                    Point point = (Point)item;
                    if(point.getCoordinate() != null && point.getCoordinate().getType() != null) {
                        
                        Serializable type = point.getCoordinate().getType();
                        
                        tooltipTitle.append(", ")
                        .append(type.getClass().getSimpleName());
                        
                        if(type instanceof GCC) {
                            
                            GCC coordinate = (GCC) type;
                            
                            tooltipTitle.append(" (X: ")
                            .append(coordinate.getX());
                            
                            tooltipTitle.append(", Y: ")
                            .append(coordinate.getX());
                            
                            tooltipTitle.append(", Z: ")
                            .append(coordinate.getX())
                            .append(")");
                            
                        } else if (type instanceof GDC) {
                            
                            GDC coordinate = (GDC) type;
                            
                            tooltipTitle.append(" (Lat: ")
                            .append(coordinate.getLatitude());
                            
                            tooltipTitle.append(", Lon: ")
                            .append(coordinate.getLongitude());
                            
                            tooltipTitle.append(", Elev: ")
                            .append(coordinate.getElevation())
                            .append(")");
                            
                        } else if(type instanceof AGL) {
                            
                            AGL coordinate = (AGL) type;
                            
                            tooltipTitle.append(" (X: ")
                            .append(coordinate.getX());
                            
                            tooltipTitle.append(", Y: ")
                            .append(coordinate.getY());
                            
                            tooltipTitle.append(", Elevation: ")
                            .append(coordinate.getElevation())
                            .append(")");
                        }
                    }
                }else if(item instanceof Path){
                    //show the number of segments in the path as a tooltip
                    
                    Path path = (Path)item;
                    tooltipTitle.append(", ").append(path.getSegment().size()).append(" segments");
                    
                }else if(item instanceof Area){
                    //show the number of vertices in the area as a tooltip
                    
                    Area area = (Area)item;
                    tooltipTitle.append(", ").append(area.getCoordinate().size()).append(" vertices");
                }
                
                label.setTitle(tooltipTitle.toString());
                
                panel.add(label);
                
                return panel;
            }
        });

        return fields;
    }
    
    /**
     * Shows the place of interest selector for the given place of interest picker and applies that picker's search text as a filter
     * for the selector's list of places of interest
     * 
     * @param picker the place of interest picker that the selector is being shown for
     */
    public static void showSelector(PlaceOfInterestPicker picker) {
        
        if(currentPicker == null || !currentPicker.equals(picker)) {
            
            //if a different place of interest picker is attempting to show the selector, remove the selector from the old picker
            getInstance().removeFromParent();
        }
        
        currentPicker = picker;
        
        //if the picker restricts the user to certain place types, apply those restrictions to the filter as well
        getInstance().filterPanel.setAcceptedTypes(currentPicker.getAcceptedPlaceTypes());
        getInstance().itemEditor.setAuthorablePlaceTypes(getInstance().filterPanel.getValue());
        
        //apply the picker's search text as a filter and update the list of places of interest
        loadAndFilterPlacesOfInterest();
        
        if(currentPicker != null) {
            
            //show the selector below the picker
            currentPicker.getSelectorPanel().add(getInstance());
            currentPicker.getSelectorPanel().show();
        }
    }
    
    /**
     * Loads the list of places of interest from the underlying schema objects and filters it based on the search text entered into the
     * current place of interest picker (if applicable). If no search text has been entered, then all of the places of interest 
     * will be shown.
     */
    public static void loadAndFilterPlacesOfInterest() {
        
        //cancel any editing, otherwise buttons in the list will be stuck in a disabled state
        getInstance().placeOfInterestList.cancelEditing();
        
        String filterText = null;
        
        if(currentPicker != null && StringUtils.isNotBlank(currentPicker.getTextBox().getText())) {
            filterText = currentPicker.getTextBox().getText().toLowerCase();
        }
        
        List<Serializable> filteredPoiList = new ArrayList<>();
        
        //determine search terms in the filter based on whitespace
        String[] searchTerms = null;
        
        if(filterText != null) {
            searchTerms = filterText.split("\\s+");
        }
        
        placesOfInterestMatchingTerms.clear();
        
        //determine which types of places of interest should be shown
        List<Class<?>> filteredTypes = new ArrayList<Class<?>>(getInstance().filterPanel.getValue());
        
        if(currentPicker != null 
                && currentPicker.getAcceptedPlaceTypes().length > 0) {
            
            //if the current picker limits the types that can be selected, remove types that aren't allowed by the picker
            filteredTypes.retainAll(Arrays.asList(currentPicker.getAcceptedPlaceTypes()));
        }
        
        List<Serializable> poisMissingTerms = new ArrayList<>();
        
        for(Serializable poi : ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea()) {
            
            if(filteredTypes.contains(poi.getClass())) {
                // of an allowed Point/Path/Area type
                
                if(disallowedCoordinateTypes != null && ScenarioValidatorUtility.containsDisallowedCoordinate(poi, disallowedCoordinateTypes)){
                    // of a disallowed coordinate type
                    continue;
                }

                //this place of interest has an accepted type, so we need to see if it contains any of the search terms
                if (searchTerms != null) {
                    
                    boolean missingTerm = false;
                    
                    String placeName = ScenarioClientUtility.getPlaceOfInterestName(poi);
                    
                    //check if each place of interest contains the necessary search terms
                    for(int i = 0; i < searchTerms.length; i++) {
                        
                        if(placeName == null || !placeName.toLowerCase().contains(searchTerms[i])) {
                            missingTerm = true;
                            break;
                        }
                    }
                    
                    if(!missingTerm) {
                        placesOfInterestMatchingTerms.add(poi);
                        
                    } else {
                        poisMissingTerms.add(poi);
                    }
                    
                } else {
                    poisMissingTerms.add(poi);
                }
            }
        }
        
        //populate the list of places of interest, with places that match search terms listed first
        filteredPoiList.addAll(placesOfInterestMatchingTerms);
        filteredPoiList.addAll(poisMissingTerms);
        
        List<PlaceOfInterestWrapper> wrappers = new ArrayList<>();
        
        for(Serializable placeOfInterest : filteredPoiList) {
            wrappers.add(new PlaceOfInterestWrapper(placeOfInterest));
        }
        
        getInstance().placeOfInterestList.setItems(wrappers);
    }
    
    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance
     */
    private static PlaceOfInterestSelectionPanel getInstance() {
        
        if(instance == null) {
            instance = new PlaceOfInterestSelectionPanel();
        }
        
        return instance;
    }
    
    /**
     * Hides the place of interest selector
     */
    public static void hideSelector() {
        
        if(currentPicker != null) {
            currentPicker.getSelectorPanel().hide();
            getInstance().placeOfInterestList.cancelEditing();
        }
        
        //reset the type filter for the places of interest list
        getInstance().resetTypeFilter();
    }
    
    /**
     * Gets the validation logic for the list inside the selector
     * 
     * @return the selector's place of interest list validation
     */
    public static ScenarioValidationComposite getListValidation() {
        return getInstance().placeOfInterestList;
    }
    
    /**
     * Does the event target this popup?
     *
     * @param event the native event
     * @return true if the event targets the popup
     */
    private boolean eventTargetsPopup(NativeEvent event) {
      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          return getElement().isOrHasChild(Element.as(target));
      }
      return false;
    }
    
    /**
     * Does the event target one of the partner elements?
     *
     * @param event the native event
     * @return true if the event targets a partner
     */
    private boolean eventTargetsPartner(NativeEvent event) {
        
      if (currentPicker == null) {
          return false;
      }

      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          if (currentPicker.getTextBox().getElement().isOrHasChild(Element.as(target))) {
              return true;
          }
      }
      return false;
    }
    
    /**
     * Resets the filter panel so that all types of places of interest are included, by default
     */
    private void resetTypeFilter() {
        filterPanel.setValue(Arrays.asList(new Class<?>[] {Point.class, Path.class, Area.class}));
        itemEditor.setAuthorablePlaceTypes(filterPanel.getValue());
    }
}
