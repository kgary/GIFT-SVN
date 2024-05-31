/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import generated.course.AuthoringSupportElements;
import generated.dkf.Area;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Coordinate;
import generated.dkf.Path;
import generated.dkf.Point;
import generated.dkf.Segment;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestConditionReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReferenceList;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestStrategyReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestFilterPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.AbstractPlaceOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.AreaOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.OverlayPoiItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.PathOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.PointOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.map.client.MapControlModeEnum;
import mil.arl.gift.tools.map.client.draw.AbstractMapShape;
import mil.arl.gift.tools.map.client.draw.ManualDrawingCompleteCallback;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolygonShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AGL;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;
import mil.arl.gift.tools.map.shared.GDC;

/**
 * A panel that is overlaid on top of a training application environment map and used to view and edit the list of
 * places of interest
 *
 * @author nroberts
 */
public class PlacesOfInterestOverlay extends AbstractPlacesOfInterestOverlay {

    /** The logger for the class */
    static final Logger logger = Logger.getLogger(PlacesOfInterestOverlay.class.getName());

    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

    protected static final String DEFAULT_DRAW_POINT_INSTRUCTIONS = "Click the location where you want to create this point.";

    protected static final String DEFAULT_DRAW_PATH_INSTRUCTIONS = "Click at least 2 locations that this path should follow."
            + "<br/><br/>To complete the path, double-click its last location.";

    protected static final String DEFAULT_DRAW_AREA_INSTRUCTIONS = "Click at least 3 locations that should define this area's boundaries. "
            + "<br/><br/>To complete the area, re-click the first location to connect it to the last.";

    /**
     * The default comparator used to sort the list of places of interest. By default, the list is unsorted, so this comparator
     * merely arranges items in the order they were added.
     */
    private static final Comparator<Serializable> DEFAULT_LIST_COMPARATOR = new Comparator<Serializable>() {

            @Override
            public int compare(Serializable o1, Serializable o2) {

                //by default, the list of places of interest should not be sorted
                return 1;
            }
        };

    private static PlacesOfInterestOverlayUiBinder uiBinder = GWT.create(PlacesOfInterestOverlayUiBinder.class);

    interface PlacesOfInterestOverlayUiBinder extends UiBinder<Widget, PlacesOfInterestOverlay> {
    }

    @UiField
    protected HTML mainHeader;

    @UiField
    protected SplitLayoutPanel splitter;

    @UiField
    protected Button returnButton;

    @UiField
    protected RadioButton panButton;

    @UiField
    protected RadioButton pointButton;

    @UiField
    protected RadioButton pathButton;

    @UiField
    protected RadioButton areaButton;

    /** A panel with buttons used to filter the types of places of interest shown */
    @UiField
    protected PlaceOfInterestFilterPanel filterPanel;

    @UiField
    protected TextBox searchBox;

    /** The item editor used to edit places of interest in the list */
    private OverlayPoiItemEditor placeOfInterestItemEditor = new OverlayPoiItemEditor() {

        @Override
        protected void populateEditor(final AbstractPlaceOfInterestShape<?, ?> wrapper) {
            super.populateEditor(wrapper);

            if(placesOfInterestList.isEditing()) {

                poiBeingEdited = wrapper;

                final AbstractMapShape<?> shape = wrapper.getMapShape();

                if(!isReadOnly) {

                    //make the shape of the place of interest being edited editable so the author can adjust its coordinates on the map
                    shape.enableEditing(new Command() {

                        @Override
                        public void execute() {

                            if(shape instanceof PointShape) {
                                populateEditor(new PointOfInterestShape((PointShape<?>) shape), false);

                            } else if(shape instanceof PolylineShape) {
                                populateEditor(new PathOfInterestShape((PolylineShape<?>) shape), false);

                            } else if(shape instanceof PolygonShape) {
                                populateEditor(new AreaOfInterestShape((PolygonShape<?>) shape), false);
                            }
                        }
                    });
                }

                //center the map on the shape being edited
                WrapPanel.getInstance().getCurrentMap().centerView(false, shape);

                onEditingStateChanged(true);
            }
        }

        @Override
        protected void applyEdits(AbstractPlaceOfInterestShape<?, ?> wrapper) {
            applyEdits(wrapper, true);
        }

        private void applyEdits(AbstractPlaceOfInterestShape<?, ?> wrapper, boolean isFinishedEditing) {

            if(isFinishedEditing) {

                //now that we've stopped editing this place of interest, make its shape uneditable
                wrapper.getMapShape().disableEditing();

                if(poiBeingEdited.equals(temporaryAuthoredPoi)) {

                    //the author has saved the temporary place of interest that they have drawn, so it is no longer temporary
                    temporaryAuthoredPoi = null;
                }

                poiBeingEdited = null;

                onEditingStateChanged(false);
            }

            super.applyEdits(wrapper);
        }

        @Override
        protected void onCancel() {

            if(poiBeingEdited != null) {

                //now that we've stopped editing this place of interest, make its shape uneditable
                poiBeingEdited.getMapShape().disableEditing();

                if(poiBeingEdited.equals(temporaryAuthoredPoi)) {

                    //if the edited place of interest is a temporary one created by the author using the manual
                    //drawing controls, remove it and its shape since its creation was cancelled
                    poiBeingEdited.getMapShape().erase();
                    poiToShape.remove(temporaryAuthoredPoi.getPlaceOfInterest());
                    temporaryAuthoredPoi = null;

                } else {

                    poiBeingEdited.updateMapShape(); //revert the shape to its original appearance
                    poiBeingEdited.getMapShape().draw();
                }
            }

            poiBeingEdited = null;

            onEditingStateChanged(false);

            super.onCancel();
        }
    };

    /** The places of interest data grid. */
    @UiField(provided=true)
    protected ItemListEditor<AbstractPlaceOfInterestShape<?, ?>> placesOfInterestList = new ItemListEditor<AbstractPlaceOfInterestShape<?, ?>>(placeOfInterestItemEditor) {

        @Override
        public void remove(final AbstractPlaceOfInterestShape<?, ?> wrapper) {

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

                        //close the wrap panel so the author can see the condition
                        WrapPanel.hide();
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
        private void delete(AbstractPlaceOfInterestShape<?, ?> wrapper) {

            Serializable element = wrapper.getPlaceOfInterest();

            // Notify listeners that the place of interest was removed.
            // Need to do this before updating the list so the data model is updated
            ScenarioEventUtility.fireDeleteScenarioObjectEvent(element, null);

            super.remove(wrapper);
        }
    };

    @UiField
    protected Widget layersPanel;

    @UiField
    protected Tree layersTree;

    @UiField
    protected Widget placesOfInterestPanel;

    @UiField
    protected Widget drawInstructionsPanel;

    @UiField
    protected HTML drawInstructions;

    @UiField
    protected Tooltip pointTooltip;

    @UiField
    protected Tooltip pathTooltip;

    @UiField
    protected Tooltip areaTooltip;

    @UiField
    protected SimplePanel subEditorPanel;

    /** A list of places of interest that contain one or more search items*/
    protected ArrayList<Serializable> placesOfInterestMatchingTerms = new ArrayList<>();

    /** The list-filtering command that is scheduled to be executed after keystrokes in the search box*/
    private ScheduledCommand scheduledFilter = null;

    /** A mapping from each generated object (e.g. condition, task trigger, strategy) in the scenario to the places of interest it references */
    private Map<Serializable, Set<Serializable>> conditionToPois = new HashMap<>();

    /** A mapping from each place of interest to the shape used to represent it on the map */
    protected Map<Serializable, AbstractPlaceOfInterestShape<?, ?>> poiToShape = new HashMap<>();

    /** The place of interest that is currently being edited by the author */
    protected AbstractPlaceOfInterestShape<?, ?> poiBeingEdited;

    /**
     * A temporary place of interest that is created when the author finishes manually drawing a shape. If the author cancels
     * creating this place of interest, it and its shape will be discarded.
     */
    private AbstractPlaceOfInterestShape<?, ?> temporaryAuthoredPoi;

    /** The layer used to represent places of interest that are not referenced by any conditions*/
    private LayerTreeItem unusedPoiLayer;

    /** The instructions telling the author how to draw an area */
    private String areaInstructions;

    /** The instructions telling the author how to draw a path */
    private String pathInstructions;

    /** The instructions telling the author how to draw a point */
    private String pointInstructions;

    /** Whether or not the author should be allowed to draw on the map */
    protected boolean drawingEnabled = true;

    /** Whether or not this widget is read-only */
    protected boolean isReadOnly;

    /**
     * Creates a new overlay panel and initializes the handlers for its controls
     */
    public PlacesOfInterestOverlay() {
        initWidget(uiBinder.createAndBindUi(this));

        returnButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                WrapPanel.hide();
            }
        });

        setDrawPointInstructions(DEFAULT_DRAW_POINT_INSTRUCTIONS);
        setDrawPathInstructions(DEFAULT_DRAW_PATH_INSTRUCTIONS);
        setDrawAreaInstructions(DEFAULT_DRAW_AREA_INSTRUCTIONS);

        Icon panIcon = new Icon(IconType.HAND_PAPER_O);
        panIcon.setSize(IconSize.LARGE);

        Icon pointIcon = new Icon(IconType.MAP_MARKER);
        pointIcon.setSize(IconSize.LARGE);

        Image pathImage = new Image("images/timeline.png");
        Image areaImage = new Image("images/area.png");

        panButton.setHTML(panIcon.getElement().getString());
        pointButton.setHTML(pointIcon.getElement().getString());
        pathButton.setHTML(pathImage.getElement().getString());
        areaButton.setHTML(areaImage.getElement().getString());

        panButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if(event.getValue()) {
                    drawInstructionsPanel.setVisible(false);
                    WrapPanel.getInstance().getCurrentMap().setControlMode(MapControlModeEnum.PAN);
                }

                panButton.setActive(event.getValue());
            }
        });

        pointButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if(event.getValue()) {

                    placesOfInterestList.cancelEditing(); //cancel editing to avoid interfering with drawing controls

                    drawInstructions.setHTML(pointInstructions);
                    drawInstructionsPanel.setVisible(true);

                    WrapPanel.getInstance().getCurrentMap().setControlMode(MapControlModeEnum.DRAW_POINT);
                }

                pointButton.setActive(event.getValue());
            }
        });
        pointButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(!pointButton.isEnabled()) {

                    //for some baffling reason, you can still click on disabled radio buttons, so prevent that
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });

        pathButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if(event.getValue()) {

                    placesOfInterestList.cancelEditing(); //cancel editing to avoid interfering with drawing controls

                    drawInstructions.setHTML(pathInstructions);
                    drawInstructionsPanel.setVisible(true);

                    WrapPanel.getInstance().getCurrentMap().setControlMode(MapControlModeEnum.DRAW_POLYLINE);
                }

                pathButton.setActive(event.getValue());
            }
        });
        pathButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(!pathButton.isEnabled()) {

                    //for some baffling reason, you can still click on disabled radio buttons, so prevent that
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });

        areaButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if(event.getValue()) {

                    placesOfInterestList.cancelEditing(); //cancel editing to avoid interfering with drawing controls

                    drawInstructions.setHTML(areaInstructions);
                    drawInstructionsPanel.setVisible(true);

                    WrapPanel.getInstance().getCurrentMap().setControlMode(MapControlModeEnum.DRAW_POLYGON);
                }

                areaButton.setActive(event.getValue());
            }
        });
        areaButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(!areaButton.isEnabled()) {

                    //for some baffling reason, you can still click on disabled radio buttons, so prevent that
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });

        RootPanel.get().addHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {

                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    Window.alert("ESC key detected");
                    panButton.setValue(true, true);
                }
            }

        }, KeyPressEvent.getType());

        filterPanel.addValueChangeHandler(new ValueChangeHandler<List<Class<?>>>() {

            @Override
            public void onValueChange(ValueChangeEvent<List<Class<?>>> event) {

                if(filterPanel.isVisible()) {

                    //if the type filter is visible, update the list whenever its values are changed
                    loadAndFilterPlacesOfInterest();

                    if(isLayersPanelVisible()) {
                        refreshLayers();
                    }
                }
            }
        });

        searchBox.setPlaceholder("Search places of interest");
        searchBox.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {

                if(scheduledFilter == null) {

                    // Schedule a filter operation for the list. We don't want to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {

                        @Override
                        public void execute() {

                            // update the filter for the selection dropdown
                            loadAndFilterPlacesOfInterest();

                            //allow the filter operation to be applied again, since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        searchBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                /* select all of the search box's text when it gains focus so that it's easier for
                 * the author to clear out */
                searchBox.selectAll();
            }
        });

        placesOfInterestList.setPlaceholder("No places of interest were found.");
        placesOfInterestList.setRemoveItemDialogTitle("Delete Place of Interest?");
        placesOfInterestList.setRemoveItemStringifier(new Stringifier<AbstractPlaceOfInterestShape<?, ?>>() {

            @Override
            public String stringify(AbstractPlaceOfInterestShape<?, ?> wrapper) {

                Serializable obj = wrapper.getPlaceOfInterest();

                String name = ScenarioClientUtility.getPlaceOfInterestName(obj);

                StringBuilder builder = new StringBuilder().append("<b>")
                        .append(name != null ? name : "this place of interest")
                        .append("</b>");

                return builder.toString();
            }
        });

        placesOfInterestList.setFields(buildListFields());

        placesOfInterestList.addListChangedCallback(new ListChangedCallback<AbstractPlaceOfInterestShape<?, ?>>() {

            @Override
            public void listChanged(ListChangedEvent<AbstractPlaceOfInterestShape<?, ?>> event) {

                if (ListAction.ADD.equals(event.getActionPerformed())) {

                    List<Serializable> placesOfInterest = ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea();
                    AbstractPlaceOfInterestShape<?, ?> edited = event.getAffectedItems().get(0);

                    if (!placesOfInterest.contains(edited.getPlaceOfInterest())) {

                        /* if a new place of interest item is added, we need to update the schema objects and
                         * the place of interest picker accordingly */
                        placesOfInterest.add(edited.getPlaceOfInterest());

                        onPlaceOfInterestAdded(edited.getPlaceOfInterest());
                    }

                } else if(ListAction.EDIT.equals(event.getActionPerformed())) {

                    for(AbstractPlaceOfInterestShape<?, ?> editted : event.getAffectedItems()) {
                        SharedResources.getInstance().getEventBus().fireEvent(new PlaceOfInterestEditedEvent(editted.getPlaceOfInterest()));
                    }
                }

                loadAndFilterPlacesOfInterest();

                if(isLayersPanelVisible()) {
                    refreshLayers();
                }
            }
        });

        layersTree.setAnimationEnabled(true);

        setLayersPanelVisible(false);

        //need to allow the list of places to validate, even though we don't show validation errors here
        placesOfInterestList.initValidationComposite(new ValidationWidget(placesOfInterestList));
    }

    /**
     * Sets the instructions that will be shown when the author is drawing an area. Subclasses can use this method
     * define special instructions for a specific case, such as drawing a relevant object for a condition.
     *
     * @param instructions the instructions to use
     */
    protected void setDrawAreaInstructions(String instructions) {
        this.areaInstructions = instructions;
    }

    /**
     * Sets the instructions that will be shown when the author is drawing a path. Subclasses can use this method
     * define special instructions for a specific case, such as drawing a relevant object for a condition.
     *
     * @param instructions the instructions to use
     */
    protected void setDrawPathInstructions(String instructions) {
        this.pathInstructions = instructions;
    }

    /**
     * Sets the instructions that will be shown when the author is drawing a point. Subclasses can use this method
     * define special instructions for a specific case, such as drawing a relevant object for a condition.
     *
     * @param instructions the instructions to use
     */
    protected void setDrawPointInstructions(String instructions) {
        this.pointInstructions = instructions;
    }

    /**
     * Builds the fields for the list of places of interest
     *
     * @return the list of fields
     */
    protected List<ItemField<AbstractPlaceOfInterestShape<?, ?>>> buildListFields() {

        List<ItemField<AbstractPlaceOfInterestShape<?, ?>>> fields = new ArrayList<>();

        //render the name of each place of interest
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>(null, "100%") {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {

                FlowPanel panel = new FlowPanel();

                Serializable item = wrapper.getPlaceOfInterest();

                Widget iconWidget = PlacesOfInterestPanel.getPlaceIcon(item.getClass());

                if(iconWidget != null) {
                    panel.add(iconWidget);
                }

                String name = ScenarioClientUtility.getPlaceOfInterestName(item);

                InlineLabel label = new InlineLabel(name);
                label.setTitle(name);
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

                panel.add(label);
                panel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);

                return panel;
            }
        });

        //render the color of each place of interest
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>() {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {

                final Serializable item = wrapper.getPlaceOfInterest();

                String color = ScenarioClientUtility.getPlaceOfInterestColor(item);
                if(color == null) {
                    color = "#000000";
                }

                ColorBox colorPicker = new ColorBox();
                colorPicker.addStyleName("colorDisplay");
                colorPicker.setValue(color);

                if(isReadOnly) {
                    colorPicker.getElement().getStyle().setProperty("pointer-events", "none");
                }

                final Tooltip tooltip = new Tooltip(colorPicker, "Change color");
                tooltip.setContainer("body");

                colorPicker.addValueChangeHandler(new ValueChangeHandler<String>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {

                        //update the item's underlying color when the author selects a new one.
                        String color = event.getValue();

                        AbstractPlaceOfInterestShape<?, ?> poiShape = poiToShape.get(item);
                        if(poiShape != null) {
                            poiShape.getMapShape().setColor(color);
                            poiShape.getMapShape().draw();
                            poiShape.updatePlaceOfInterest();
                        }
                    }
                });

                colorPicker.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation(); //prevent list element from handling the click inadvertently
                    }
                });

                return colorPicker;
            }
        });

        return fields;
    }

    /**
     * Loads the list of places of interest from the underlying schema objects and filters it based on the search text entered into the
     * current place of interest picker (if applicable). If no search text has been entered, then all of the places of interest will be shown.
     */
    public void loadAndFilterPlacesOfInterest() {

        //cancel any editing, otherwise buttons in the list will be stuck in a disabled state
        placesOfInterestList.cancelEditing();

        final String username = GatClientUtility.getUserName();


        final AuthoringSupportElements authoringSupportElements = ScenarioClientUtility.getAuthoringSupportElements();
        final String propertyPath;
        if (authoringSupportElements != null) {
            propertyPath = authoringSupportElements.getTrainingAppsScenarioMapPath();
        } else {
            propertyPath = "";
        }

        if (authoringSupportElements != null && !propertyPath.isEmpty()) {

            rpcService.getTrainingApplicationScenarioProperty(propertyPath, username,
        			new AsyncCallback<GenericRpcResponse<GiftScenarioProperties>>() {

        		@Override
        		public void onSuccess(GenericRpcResponse<GiftScenarioProperties> result) {

        			Point pt = new Point();
        			generated.dkf.AGL agl = new generated.dkf.AGL();
        			Coordinate cord = new Coordinate();

        			agl.setX(result.getContent().getUserStartLocation().getX());
        			agl.setY(result.getContent().getUserStartLocation().getY());
        			agl.setElevation(result.getContent().getUserStartLocation().getZ());
        			cord.setType(agl);
        			pt.setCoordinate(cord);

        			AbstractMapCoordinate startLocation = PlacesOfInterestOverlay.toMapCoordinate(cord);
        			PointShape<?> userStartShape = WrapPanel.getInstance().getCurrentMap().createPoint(startLocation);

        			userStartShape.setClickCommand(new Command() {
        				@Override
        				public void execute() {

        				}
        			});
        			userStartShape.setName("Learner start location");
        			userStartShape.setColor("#FFC0CB");
        			userStartShape.draw();
        		}

        		@Override
        		public void onFailure(Throwable caught) {
        			logger.warning("PlacesOfInterestOverlay.loadAndFilterPlacesOfInterest.getTrainingApplicationScenarioProperty: onFailure: " + caught);
        		}
        	});
        }

        String filterText = null;

        if(StringUtils.isNotBlank(searchBox.getText())) {
            filterText = searchBox.getText().toLowerCase();
        }

        List<Serializable> filteredPoiList = new ArrayList<>();

        // determine search terms in the filter based on whitespace
        String[] searchTerms = null;

        if (filterText != null) {
            searchTerms = filterText.split("\\s+");
        }

        placesOfInterestMatchingTerms.clear();

        //keep track of which places of interest get filtered out so we can erase their shapes
        Map<Serializable, AbstractPlaceOfInterestShape<?, ?>> poiToShapeToErase = new HashMap<>();
        poiToShapeToErase.putAll(poiToShape);
        poiToShape.clear();

        //determine which types of places of interest should be shown
        List<Class<?>> filteredTypes = new ArrayList<>(filterPanel.getValue());

        enableDrawButtonsToMatchFilter();

        TreeSet<Serializable> placesOfInterestMissingTerms = new TreeSet<>(getListSortComparator());

        for (Serializable placeOfInterest : ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea()) {

            String placeName = ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest);

            if(filteredTypes.contains(placeOfInterest.getClass())) {

                if (searchTerms != null) {

                    boolean missingTerm = false;

                    //check if each place of interest contains the necessary search terms
                    for(int i = 0; i < searchTerms.length; i++) {

                        if(placeName == null || !placeName.toLowerCase().contains(searchTerms[i])) {
                            missingTerm = true;
                            break;
                        }
                    }

                    if(!missingTerm) {
                        placesOfInterestMatchingTerms.add(placeOfInterest);

                    } else {
                        placesOfInterestMissingTerms.add(placeOfInterest);
                    }

                } else {
                    placesOfInterestMissingTerms.add(placeOfInterest);
                }

                //determine whether there is an existing shape for this POI that we can reuse
                AbstractPlaceOfInterestShape<?, ?> shape = poiToShapeToErase.get(placeOfInterest);

                try {
                    if(placeOfInterest instanceof Point) {

                        if(shape instanceof PointOfInterestShape) {
                            shape.updateMapShape(); //update the existing shape

                        } else {
                            shape = new PointOfInterestShape((Point) placeOfInterest); //create a new shape
                        }

                    } else if(placeOfInterest instanceof Path){

                        if(shape instanceof PathOfInterestShape) {
                            shape.updateMapShape(); //update the existing shape

                        } else {
                            shape = new PathOfInterestShape((Path) placeOfInterest); //create a new shape
                        }

                    } else if(placeOfInterest instanceof Area) {

                        if(shape instanceof AreaOfInterestShape) {
                            shape.updateMapShape(); //update the existing shape

                        } else {
                            shape = new AreaOfInterestShape((Area) placeOfInterest); //create a new shape
                        }

                    } else {
                        logger.severe("Failed to create shape for invalid place of interest: " + placeOfInterest);
                    }

                } catch(IllegalArgumentException e) {
                    logger.warning("An exception occurred while rendering a shape for " + placeOfInterest + ": " + e);
                }

                if(shape != null) {

                    final AbstractPlaceOfInterestShape<?, ?> finalShape = shape;
                    finalShape.getMapShape().setClickCommand(new Command() {

                        @Override
                        public void execute() {
                            onPlaceShapeClicked(finalShape);
                        }
                    });

                    shape.getMapShape().draw(); //update the drawn shape

                    poiToShape.put(placeOfInterest, shape);
            }
        }
        }

        //erase the shapes that were rendered for places of interest that are no longer in the list
        for(Serializable placeOfInterest : poiToShapeToErase.keySet()) {

            AbstractPlaceOfInterestShape<?, ?> toErase = poiToShapeToErase.get(placeOfInterest);

            if(toErase != null && !toErase.equals(poiToShape.get(placeOfInterest))) {
                toErase.getMapShape().erase();
            }
        }

        //populate the list of places of interest, with places of interest that match search terms listed first
        filteredPoiList.addAll(placesOfInterestMatchingTerms);
        filteredPoiList.addAll(placesOfInterestMissingTerms);

        List<AbstractPlaceOfInterestShape<?, ?>> wrappers = new ArrayList<>();

        for(Serializable placeOfInterest : filteredPoiList) {
            wrappers.add(poiToShape.get(placeOfInterest));
        }

        placesOfInterestList.setItems(wrappers);
    }

    @Override
    public void refreshPlacesOfInterest() {

        filterPanel.setValue(getPreferredTypes());

        loadAndFilterPlacesOfInterest();

        resetZoomToFitPlaces();

        if(isLayersPanelVisible()) {
            refreshLayers();
        }

        resetToolButtonsState();

        WrapPanel.getInstance().getCurrentMap().setManualDrawingCompleteHandler(new ManualDrawingCompleteCallback() {

            @Override
            public void onDrawingComplete(AbstractMapShape<?> shape) {

                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("Manual drawing complete for " + shape);
                }

                resetToolButtonsState();

                if(placesOfInterestList.isEditing()) {
                    placesOfInterestList.cancelEditing();
                }

                if(shape instanceof PointShape<?>) {

                    PointOfInterestShape pointShape = new PointOfInterestShape((PointShape<?>) shape);
                    poiToShape.put(pointShape.getPlaceOfInterest(), pointShape); //add a temporary mapping from this point to its shape

                    temporaryAuthoredPoi = pointShape;

                    placesOfInterestList.editNewElement(pointShape);

                } else if(shape instanceof PolylineShape<?>) {

                    PathOfInterestShape pathShape = new PathOfInterestShape((PolylineShape<?>) shape);
                    poiToShape.put(pathShape.getPlaceOfInterest(), pathShape); //add a temporary mapping from this point to its shape

                    temporaryAuthoredPoi = pathShape;

                    placesOfInterestList.editNewElement(pathShape);

                } else if(shape instanceof PolygonShape<?>) {

                    AreaOfInterestShape areaShape = new AreaOfInterestShape((PolygonShape<?>) shape);
                    poiToShape.put(areaShape.getPlaceOfInterest(), areaShape); //add a temporary mapping from this point to its shape

                    temporaryAuthoredPoi = areaShape;

                    placesOfInterestList.editNewElement(areaShape);
                }
            }
        });

        openPreppedPlaceForEditing();
    }

    @Override
    public void resetZoomToFitPlaces() {

        //determine the map shapes that were drawn so the map can be centered around them
        AbstractMapShape<?>[] mapShapes = new AbstractMapShape<?>[poiToShape.size()];
        int i = 0;

        for(Serializable placeOfInterest : poiToShape.keySet()) {
            mapShapes[i] = poiToShape.get(placeOfInterest).getMapShape();
            i++;
        }

        //center the map around the shapes of the places of interest that were drawn
        if(mapShapes.length > 0) {
            WrapPanel.getInstance().getCurrentMap().centerView(true, mapShapes);
        }
    }

    /**
     * Gets the types of places of interest that should be included in the list when this overlay is shown. By default, all types
     * of interest will be shown. Subclasses can extend this method to limit the author to relevant types initially.
     *
     * @return the list of preferred types
     */
    protected List<Class<?>> getPreferredTypes() {
        return Arrays.asList(new Class<?>[] {Point.class, Path.class, Area.class});
    }

    /**
     * Begins editing the place of interest prepared for editing by {@link #prepareForEditing(Serializable, Serializable)}
     */
    private void openPreppedPlaceForEditing() {

        //determine whether or not the original place to edit is an existing place of interest
        AbstractPlaceOfInterestShape<?, ?> existingShape = null;

        if(placeToEditWhenReady != null) {

            for(AbstractPlaceOfInterestShape<?, ?> placeShape : placesOfInterestList.getItems()) {
                if(placeShape.getPlaceOfInterest().equals(placeToEditWhenReady)) {

                    //if the original place to edit is an existing place, open its editor so we can modify the existing object
                    existingShape = placeShape;
                    placesOfInterestList.editExisting(existingShape);

                    break;
                }
            }
        }

        if(editedPlaceToEditWhenReady != null) {

            Serializable place = editedPlaceToEditWhenReady;

            if(existingShape != null) {

                //update the editor for the existing place of interest with the changes from the edited copy
                if(existingShape instanceof PointOfInterestShape && place instanceof Point) {

                    PointOfInterestShape placeShape = new PointOfInterestShape(((PointOfInterestShape) existingShape).getMapShape());
                    placeShape.setPlaceOfInterest((Point) place);
                    placeShape.updateMapShape();
                    placeShape.getMapShape().draw();

                    placeOfInterestItemEditor.populateEditor(placeShape, false);

                } else if(existingShape instanceof PathOfInterestShape && place instanceof Path) {

                    PathOfInterestShape placeShape = new PathOfInterestShape(((PathOfInterestShape) existingShape).getMapShape());
                    placeShape.setPlaceOfInterest((Path) place);
                    placeShape.updateMapShape();
                    placeShape.getMapShape().draw();

                    placeOfInterestItemEditor.populateEditor(placeShape, false);

                } else if(existingShape instanceof AreaOfInterestShape && place instanceof Area) {

                    AreaOfInterestShape placeShape = new AreaOfInterestShape(((AreaOfInterestShape) existingShape).getMapShape());
                    placeShape.setPlaceOfInterest((Area) place);
                    placeShape.updateMapShape();
                    placeShape.getMapShape().draw();

                    placeOfInterestItemEditor.populateEditor(placeShape, false);
                }

            } else {

                //create a new place of interest with the changes from the edited place of interest
                if(place instanceof Point) {

                    Point point = (Point) place;

                    boolean needsToBeDrawn = point.getCoordinate() == null || point.getCoordinate().getType() == null;

                    if(needsToBeDrawn) {

                        //prompt author to draw a shape
                        panButton.setValue(false, true);
                        pointButton.setValue(true, true);
                        pathButton.setValue(false, true);
                        areaButton.setValue(false, true);

                    } else {

                        //start editing the shape as if the author just drew it
                        placesOfInterestList.editNewElement(new PointOfInterestShape(point));
                    }

                } else if(place instanceof Path) {

                    Path path = (Path) place;

                    boolean needsToBeDrawn = true;

                    if(!path.getSegment().isEmpty()) {

                        needsToBeDrawn = false;

                        for(Segment segment : path.getSegment()) {

                            if(segment.getStart() == null
                                    || segment.getStart().getCoordinate() == null
                                    || segment.getStart().getCoordinate().getType() == null
                                    || segment.getEnd() == null
                                    || segment.getEnd().getCoordinate() == null
                                    || segment.getEnd().getCoordinate().getType() == null) {

                                needsToBeDrawn = true;
                                break;
                            }
                        }
                    }

                    if(needsToBeDrawn) {

                        //prompt author to draw a shape
                        panButton.setValue(false, true);
                        pointButton.setValue(false, true);
                        pathButton.setValue(true, true);
                        areaButton.setValue(false, true);

                    } else {

                        //start editing the shape as if the author just drew it
                        placesOfInterestList.editNewElement(new PathOfInterestShape(path));
                    }

                } else if(place instanceof Area) {

                    Area area = (Area) place;

                    boolean needsToBeDrawn = true;

                    if(!area.getCoordinate().isEmpty() && area.getCoordinate().size() > 2) {

                        needsToBeDrawn = false;

                        for(Coordinate coordinate : area.getCoordinate()) {

                            if(coordinate.getType() == null) {
                                needsToBeDrawn = true;
                                break;
                            }
                        }
                    }

                    if(needsToBeDrawn) {

                        //prompt author to draw a shape
                        panButton.setValue(false, true);
                        pointButton.setValue(false, true);
                        pathButton.setValue(false, true);
                        areaButton.setValue(true, true);

                    } else {

                        //start editing the shape as if the author just drew it
                        placesOfInterestList.editNewElement(new AreaOfInterestShape(area));
                    }
                }
            }
        }

        placeToEditWhenReady = null;
        editedPlaceToEditWhenReady = null;
    }

    /**
     * Resets the tool buttons so that the author can drag the map rather than draw shapes
     */
    private void resetToolButtonsState() {

        panButton.setValue(true, true);
        panButton.setActive(true);

        pointButton.setValue(false, true);
        pointButton.setActive(false);

        pathButton.setValue(false, true);
        pathButton.setActive(false);

        areaButton.setValue(false, true);
        areaButton.setActive(false);
    }

    /**
     * Updates the rendered tree of performance node layers to match the underlying data. These layers will be used
     * to allow the author to show/hide places of interest referenced by certain performance nodes.
     */
    void refreshLayers() {

        layersTree.clear();
        conditionToPois.clear();

        //figure out which conditions reference which places of interest
        for(AbstractPlaceOfInterestShape<?, ?> wrapper : placesOfInterestList.getItems()) {

            List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(wrapper.getPlaceOfInterest());

            if(references != null) {
                for(PlaceOfInterestReference reference : references) {

                    Serializable refObject = null;
                    if(reference instanceof PlaceOfInterestConditionReference){
                        refObject = ((PlaceOfInterestConditionReference)reference).getCondition();
                    }else if(reference instanceof PlaceOfInterestStrategyReference){
                        refObject = ((PlaceOfInterestStrategyReference)reference).getStrategy();
                    }
                    
                    if(refObject != null){
                        Set<Serializable> pois = conditionToPois.get(refObject);
                        if(pois == null) {
                            pois = new HashSet<>();
                            conditionToPois.put(refObject, pois);
                        }
    
                       pois.add(wrapper.getPlaceOfInterest());
                    }
                }
            }
        }

        unusedPoiLayer = new LayerTreeItem(null, new Command() {

            @Override
            public void execute() {
                updateRenderedLayers();
            }
        });
        layersTree.addItem(unusedPoiLayer);

        Tasks tasks = ScenarioClientUtility.getTasks();

        //create a tree of layers corresponding to the scenarios performance nodes
        if(tasks != null) {
            for(Task task : tasks.getTask()) {

                List<TreeItem> children = new ArrayList<>();

                for(Concept childConcept : task.getConcepts().getConcept()) {

                    TreeItem childItem = createLayer(childConcept);

                    if(childItem != null) {

                        //this concept contained a condition that references a place of interest, so add it
                        children.add(childItem);
                    }
                }

                if(!children.isEmpty()) {

                    //create a tree item for this task, since one of its children has one of the conditions we're looking for
                    layersTree.addItem(new LayerTreeItem(task, new Command() {

                        @Override
                        public void execute() {
                            updateRenderedLayers();
                        }

                    }, children.toArray(new LayerTreeItem[children.size()])));
                }
            }
        }
    }

    /**
     * Creates a performance node layer for the given concept and its children. This layers will be used
     * to allow the author to show/hide places of interest referenced by this concept's underlying conditions.
     *
     * @param concept the concept to create a layer for
     * @return the layer tree item corresponding to the given concept
     */
    private LayerTreeItem createLayer(Concept concept) {

        if(concept == null) {
            throw new IllegalArgumentException("The concept to create a layer for cannot be null");
        }

        if(conditionToPois == null) {
            throw new IllegalArgumentException("The list of conditions to search for cannot be null");
        }

        List<TreeItem> children = new ArrayList<>();

        //create tree items for all child concepts and conditions that reference a place of interest
        if(concept.getConditionsOrConcepts() instanceof Conditions) {

            for(final Condition condition : ((Conditions) concept.getConditionsOrConcepts()).getCondition()) {
                if(conditionToPois.containsKey(condition) || condition.equals(getCondition())) {

                    //add a tree item for this condition, since it references a place of interest
                    children.add(new LayerTreeItem(condition, new Command() {

                        @Override
                        public void execute() {
                            updateRenderedLayers();
                        }
                    }));
                }
            }

        } else if(concept.getConditionsOrConcepts() instanceof Concepts) {

            for(Concept childConcept : ((Concepts) concept.getConditionsOrConcepts()).getConcept()) {

                TreeItem childItem = createLayer(childConcept);

                if(childItem != null) {

                    //this concept contained a condition that references a place of interest, so add it
                    children.add(childItem);
                }
            }
        }

        if(!children.isEmpty()) {

            //create a tree item for this concept, since one of its children has one of the conditions we're looking for
            return new LayerTreeItem(concept, new Command() {

                @Override
                public void execute() {
                    updateRenderedLayers();
                }
            }, children.toArray(new LayerTreeItem[children.size()]));
        }

        return null;
    }

    @Override
    public void setLayersPanelVisible(boolean visible) {

        if(visible) {

            //load the content of the layers panel
            refreshLayers();

            //show the layer panel
            splitter.setWidgetHidden(layersPanel, false);
            splitter.setWidgetSize(layersPanel, 0);
            placesOfInterestPanel.getElement().getStyle().setProperty("margin-bottom", "-8px");
            splitter.forceLayout();

            //growing animation
            splitter.setWidgetSize(layersPanel, getOffsetHeight()/3);
            splitter.animate(500);

        } else {

            //shrinking animation
            splitter.setWidgetSize(layersPanel, 0);
            splitter.animate(500);

            new Timer() {

                @Override
                public void run() {

                    //hide and clear the layer panel
                    layersTree.clear();
                    splitter.setWidgetHidden(layersPanel, true);
                    placesOfInterestPanel.getElement().getStyle().clearProperty("margin-bottom");
                }

            }.schedule(500);
        }
    }

    /**
     * Updates the map to reflect which layers the author has selected to be visible. If all of the layers representing the
     * conditions that reference a particular place of interest are unselected, then the shape corresponding to that place of
     * interest will be removed from the map.
     */
    protected void updateRenderedLayers() {

        Set<Condition> visibleConditions = new HashSet<>();
        boolean unusedPoisVisible = false;

        for(int i = 0; i < layersTree.getItemCount(); i ++) {

            LayerTreeItem item = (LayerTreeItem) layersTree.getItem(i);

            if(item.equals(unusedPoiLayer)) {
                unusedPoisVisible = item.isChecked();

            } else {
                visibleConditions.addAll(item.getVisibleConditions());
            }
        }

        Set<Serializable> visiblePois = new HashSet<>();

        for(Condition condition : visibleConditions) {

            Set<Serializable> pois = conditionToPois.get(condition);

            if(pois != null) {
                visiblePois.addAll(pois);
            }
        }

        for(Serializable poi : poiToShape.keySet()) {

            AbstractMapShape<?> shape = poiToShape.get(poi).getMapShape();

            if(visiblePois.contains(poi) || ScenarioClientUtility.getReferencesTo(poi).isEmpty() && unusedPoisVisible) {

                //draw places of interest that either don't have any references or have at least one reference that is selected
                shape.draw();

            } else {

                //hide places of interest that have no selected references
                shape.erase();
            }
        }
    }

    @Override
    public boolean isLayersPanelVisible() {
        return splitter.getWidgetContainerElement(layersPanel).getStyle().getDisplay() != "none";
    }

    /**
     * Converts the given schema coordinate to an equivalent map coordinate
     *
     * @param schemaCoordinate the schema coordinate
     * @return the equivalent map coordinate
     * @throws IllegalArgumentException if the schema coordinate is null or cannot be converted to a map coordinate
     */
    public static AbstractMapCoordinate toMapCoordinate(Coordinate schemaCoordinate) {

        if(schemaCoordinate == null) {
            throw new IllegalArgumentException("The schema coordinate to convert cannot be null.");
        }

        Serializable type = schemaCoordinate.getType();

        if(type instanceof generated.dkf.GDC) {

            generated.dkf.GDC gdc = (generated.dkf.GDC) type;

            return new GDC(
                    gdc.getLatitude().doubleValue(),
                    gdc.getLongitude().doubleValue(),
                    gdc.getElevation().doubleValue()
            );

        } else if(type instanceof generated.dkf.AGL) {
            generated.dkf.AGL agl = (generated.dkf.AGL) type;

            return new AGL(
                    agl.getX().doubleValue(),
                    agl.getY().doubleValue(),
                    agl.getElevation().doubleValue()
            );
        } else {
            throw new IllegalArgumentException("Unknown schema coordinate type:" + type);
        }
    }

    /**
     * Converts the given map coordinate to an equivalent schema coordinate
     *
     * @param mapCoordinate the map coordinate
     * @return the equivalent schema coordinate
     * @throws IllegalArgumentException if the map coordinate is null or cannot be converted to a schema coordinate
     */
    public static Coordinate toSchemaCoordinate(AbstractMapCoordinate mapCoordinate) {

        if(mapCoordinate == null) {
            throw new IllegalArgumentException("The map coordinate to convert cannot be null.");
        }

        if(mapCoordinate instanceof GDC) {

            GDC gdc = (GDC) mapCoordinate;

            generated.dkf.GDC schemaGdc = new generated.dkf.GDC();
            schemaGdc.setLatitude(BigDecimal.valueOf(gdc.getLatitude()));
            schemaGdc.setLongitude(BigDecimal.valueOf(gdc.getLongitude()));
            schemaGdc.setElevation(BigDecimal.valueOf(gdc.getAltitude()));

            Coordinate schemaCoordinate = new Coordinate();
            schemaCoordinate.setType(schemaGdc);

            return schemaCoordinate;

        } else if (mapCoordinate instanceof AGL) {
            AGL agl = (AGL) mapCoordinate;

            generated.dkf.AGL schemaAgl = new generated.dkf.AGL();
            schemaAgl.setX(BigDecimal.valueOf(agl.getX()));
            schemaAgl.setY(BigDecimal.valueOf(agl.getY()));
            schemaAgl.setElevation(BigDecimal.valueOf(agl.getElevation()));

            Coordinate schemaCoordinate = new Coordinate();
            schemaCoordinate.setType(schemaAgl);

            return schemaCoordinate;
        } else {
            throw new IllegalArgumentException("Unknown map coordinate type:" + mapCoordinate);
        }
    }

    @Override
    public void cleanUpMap() {

        //erase all shapes that were drawn on the map
        for(AbstractPlaceOfInterestShape<?, ?> placeShape : poiToShape.values()) {
            placeShape.getMapShape().erase();
        }
    }

    /**
     * Enables and disables the buttons used to draw places of interest so that the author can only draw types that
     * are allowed by the current filter
     */
    private void enableDrawButtonsToMatchFilter() {

        if(drawingEnabled) {

            if(filterPanel.getValue().contains(Point.class)) {
                pointButton.setEnabled(true);
                pointTooltip.setTitle("Draw a point");

            } else {
                pointButton.setEnabled(false);
                pointTooltip.setTitle("The current filter does not allow points to be drawn. "
                        + "To change the filter, click the <i class='fa fa-database'/> button");
            }

            if(filterPanel.getValue().contains(Path.class)) {
                pathButton.setEnabled(true);
                pathTooltip.setTitle("Draw a path");

            } else {
                pathButton.setEnabled(false);
                pathTooltip.setTitle("The current filter does not allow paths to be drawn. "
                        + "To change the filter, click the <i class='fa fa-database'/> button");
            }

            if(filterPanel.getValue().contains(Area.class)) {
                areaButton.setEnabled(true);
                areaTooltip.setTitle("Draw an area");

            } else {
                areaButton.setEnabled(false);
                areaTooltip.setTitle("The current filter does not allow areas to be drawn. "
                        + "To change the filter, click the <i class='fa fa-database'/> button");
            }

        } else {

            pointButton.setEnabled(false);
            pointTooltip.setTitle("Drawing points has been temporarily disabeld");

            pathButton.setEnabled(false);
            pathTooltip.setTitle("Drawing paths has been temporarily disabeld");

            areaButton.setEnabled(false);
            areaTooltip.setTitle("Drawing areas has been temporarily disabeld");
        }
    }

    /**
     * Performs logic whenever a place of interest is added. By default, this method will do nothing, but
     * subclasses can extend this method to perform additional logic upon adding an item.
     *
     * @param placeOfInterest the place of interest that was added
     */
    protected void onPlaceOfInterestAdded(Serializable placeOfInterest) {
        //by default, don't do any extra work when a place of interest is added
    }

    /**
     * Executes logic when the author starts or stops editing a place of interest. By default, this method will temporarily
     * disable drawing when the author starts editing a place of interest and will re-enable drawing according to the current
     * filter when the author is finished editing. Subclasses can override this method to extend this behavior.
     *
     * @param isEditing whether the author is starting to edit a place of interest
     */
    protected void onEditingStateChanged(boolean isEditing) {

        if(isEditing) {

           //disable the drawing controls while editing
            pointButton.setEnabled(false);
            pointTooltip.setTitle("New points cannot be drawn while editing");
            pathButton.setEnabled(false);
            pathTooltip.setTitle("New paths cannot be drawn while editing");
            areaButton.setEnabled(false);
            areaTooltip.setTitle("New areas cannot be drawn while editing");

        } else {

            //re-enable the drawing controls once editing is complete
            enableDrawButtonsToMatchFilter();
        }
    }

    /**
     * Gets a comparator that will be used to sort this overlay's list of places of interest
     *
     * @return the sorting comparator
     */
    protected Comparator<Serializable> getListSortComparator() {
        return DEFAULT_LIST_COMPARATOR;
    }

    /**
     * Sets the sub-editor widget that will appear above the places of interest list
     *
     * @param widget the widget to use as a sub-editor
     */
    protected void setSubEditor(Widget widget) {
        subEditorPanel.setWidget(widget);
    }

    /**
     * Executes logic when a place of interest's shape is clicked on. By default, this method will begin editing the shape's
     * corresponding place of interest if the author is not already editing one. Subclasses can override this method
     * to extend this behavior.
     *
     * @param placeShape the place of interest shape whose map shape was clicked on.
     */
    protected void onPlaceShapeClicked(AbstractPlaceOfInterestShape<?, ?> placeShape) {

        if(!placeShape.equals(poiBeingEdited)) {

            if(placesOfInterestList.isEditing()) {
                placesOfInterestList.cancelEditing(); //cancel editing so we can start editing the given shape
            }

            placesOfInterestList.editExisting(placeShape);
        }
    }

    /**
     * Sets whether or not the author should be allowed to draw places of interest
     *
     * @param enabled whether the learner should be able to draw
     */
    public void setDrawingEnabled(boolean enabled) {
        this.drawingEnabled = enabled;

        enableDrawButtonsToMatchFilter();
    }

    @Override
    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;

        placesOfInterestList.setReadonly(isReadOnly);
        setDrawingEnabled(!isReadOnly);
    }
}
