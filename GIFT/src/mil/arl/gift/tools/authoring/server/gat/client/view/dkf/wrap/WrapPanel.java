/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AvoidLocationCondition;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.Condition;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.EnterAreaCondition;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.EditorTab;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.ConditionEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.UnityMapPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.client.google.GoogleMapPanel;

/**
 * A panel that wraps controls around a map representing a training application environment so that an author can
 * directly define scenario features on the map using the GAT
 *
 * @author nroberts
 */
public class WrapPanel extends Composite {

    private static WrapPanelUiBinder uiBinder = GWT.create(WrapPanelUiBinder.class);

    private static WrapPanel instance;

    /** The editor tab that is being temporarily pinned and should be unpinned when the wrap panel is hidden */
    private static EditorTab tabToUnpinOnHide;

    interface WrapPanelUiBinder extends UiBinder<Widget, WrapPanel> {
    }

    /**
     * The types of maps that can be shown by the overlay
     *
     * @author nroberts
     */
    public enum MapTypeEnum{

        /** A map rendered by the Google Maps API */
        GOOGLE,

        /** A map of a Unity scenario */
        UNITY
    }

    /** Splitter separating the overlay panel from the environment map */
    @UiField
    protected SplitLayoutPanel splitter;

    @UiField
    protected SimplePanel overlayPanel;

    /** Button used to show and hide the overlay panel*/
    @UiField
    protected Icon overlayToggle;

    @UiField
    protected Icon fullscreenToggle;

    @UiField
    protected Icon layersToggle;

    @UiField
    protected DeckPanel mapDeck;

    @UiField
    protected Icon resetZoomButton;

    @UiField
    protected Icon zoomInButton;

    @UiField
    protected Icon zoomOutButton;

    /** Whether or not the overlay panel is visible*/
    boolean overlayVisible = true;

    /** The map currently being shown to the author */
    private AbstractMapPanel currentMap = null;

    /** Overlay used to interact with places of interest */
    private AbstractPlacesOfInterestOverlay overlay;

    /**
     * The panel used to show and interact with Google maps. Bear in mind that it is important to keep the same
     * map attached to the page as long as possible, since every load of the map counts as a single use for
     * Google Maps' pricing policy
     */
    private GoogleMapPanel googleMapPanel;

    /** Whether to clean up the default values for a condition when returning from the Wrap panel */
    private static boolean cleanupDefault;
    
    public static Serializable firstPlaceSelectedOrCreated;
    private UnityMapPanel unityMapPanel;

    /**
     * Creates a new panel wrapping an environment map for a training application and initializes the handlers for its controls
     */
    private WrapPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        overlayToggle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                overlayVisible = !overlayVisible;
                splitter.setWidgetHidden(overlayPanel, !overlayVisible);
                overlayToggle.setType(overlayVisible ? IconType.CHEVRON_CIRCLE_LEFT : IconType.CHEVRON_CIRCLE_RIGHT);
            }
        });

        fullscreenToggle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(JsniUtility.getFullscreenElement() == null) {

                    //this widget is not being shown in full screen mode, so show it in full screen mode
                    JsniUtility.requestFullscreen(WrapPanel.this.getElement());

                    fullscreenToggle.setColor("lightgray");
                    fullscreenToggle.getElement().getStyle().setBackgroundImage("radial-gradient(transparent, rgba(0, 0, 0, 0.25))");

                } else {

                    //this widget is currently in full screen mode, so exit full screen mode
                    JsniUtility.exitFullscreen();

                    fullscreenToggle.setColor("white");
                    fullscreenToggle.getElement().getStyle().clearBackgroundImage();
                }
            }
        });

        layersToggle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                //toggle whether or not the layers panel should be visible
                if(overlay != null) {
                    overlay.setLayersPanelVisible(!overlay.isLayersPanelVisible());
                }
            }
        });

        resetZoomButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(overlay != null && currentMap != null && currentMap.isReady()) {
                    overlay.resetZoomToFitPlaces();
                }
            }
        });

        zoomInButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(currentMap != null) {
                    currentMap.zoomIn();
                }
            }
        });

        zoomOutButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(currentMap != null) {
                    currentMap.zoomOut();
                }
            }
        });

        if (ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.UNITY_EMBEDDED) {
            showMap(MapTypeEnum.UNITY);
        } else {
            showMap(MapTypeEnum.GOOGLE);
        }
    }

    /**
     * Gets the singleton instance of this class
     *
     * @return the singleton instance
     */
    public static WrapPanel getInstance() {

        if(instance == null) {
            instance = new WrapPanel();
        }

        return instance;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                if(isAttached()) {

                    //animate sliding in the wrap panel whenever it is attached and loaded
                    getInstance().removeStyleName("translateBelow");
                }
            }
        });
    }

    /**
     * Shows the wrap panel on top of the DKF editor
     */
    public static void show() {
        editOnMap(null, null, true);
    }

    /**
     * Shows the wrap panel and immediately opens the given place of interest for editing once the map is ready. If the place
     * of interest is currently being edited, the edited version can also be passed in so that the editor in the wrap panel
     * can be updated to match it.
     *
     * @param originalPlace the original place of interest to open for editing on the map. Can be null, if the author is editing
     * a new place of interest.
     * @param editedPlace an optional place of interest that represents any modifications the author has made to the original
     * place of interest. If the original place of interest is null, the author will start editing a new place of interest using
     * the edited place's data. If the original place of interest is not null, the editor for that place of interest will be updated
     * to match the edited place. Can be null, if the author has made no changes to the original place that need to be loaded.
     * @param automaticallyAddPoints whether or not to automatically add points added to the map to the condition the map is editing
     * for
     */
    public static void editOnMap(Serializable originalPlace, Serializable editedPlace, boolean cleanupDefault) {

        firstPlaceSelectedOrCreated = null;
        WrapPanel.cleanupDefault = cleanupDefault;

        EditorTab currentTab = EditorTab.getActiveTab();

        if(!currentTab.isPinned()) {
            tabToUnpinOnHide = currentTab; //temporarily pin this tab in case the author jumps to another object
        }

        currentTab.setPinned(true);

        Serializable activeObject = currentTab.getScenarioObject();

        if(getInstance().overlay != null) {

            //if another overlay was showing before, clean up its changes to the map first
            getInstance().overlay.cleanUpMap();
        }

        //load the appropriate overlay
        getInstance().overlay = getOverlay(activeObject);
        
        getInstance().overlayPanel.setWidget(getInstance().overlay);

        getInstance().overlay.setReadOnly(ScenarioClientUtility.isReadOnly());
        getInstance().overlay.prepareForEditing(originalPlace, editedPlace);

        //hide save and cancel buttons while the wrap panel is showing so the author doesn't click them accidentally
        GatClientUtility.setModalButtonsVisible(false, false);

        if(!getInstance().isAttached()) {

            //attach the wrap panel
            RootPanel.get().add(getInstance());
        }

        if(getInstance().getCurrentMap().isReady()) {
            getInstance().initOverlay();
        }
    }

    /**
     * Hides the wrap panel so that the DKF editor is visible
     */
    public static void hide() {

        //places of interest may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();

        Serializable activeObject = EditorTab.getActiveTab().getScenarioObject();

        if(activeObject instanceof Condition && EditorTab.getActiveTab().getEditor() instanceof ConditionEditor) {

            //if a condition was being edited, reload it in case changes were made to it from the overlay
            ConditionEditor editor = (ConditionEditor) EditorTab.getActiveTab().getEditor();

            editor.edit((Condition) activeObject);
            
        }

        if(tabToUnpinOnHide != null
                && activeObject != null
                && activeObject.equals(tabToUnpinOnHide.getScenarioObject())) {

            //if we temporarily pinned a tab while the wrap panel was showing and returned to the same tab, unpin that tab
            tabToUnpinOnHide.setPinned(false);
        }

        tabToUnpinOnHide = null;

        if(getInstance().overlay != null && getInstance().overlay.getCondition() != null) {

            //if a condition was being edited in the overlay, revalidate it when the overlay is hidden
            ScenarioEventUtility.fireDirtyEditorEvent(getInstance().overlay.getCondition());
            
            if (getInstance().overlay instanceof AvoidLocationConditionOverlay) {

                ScenarioEventUtility.firePlaceOfInterestResumeEditEvent(firstPlaceSelectedOrCreated, getInstance().overlay.getCondition(), cleanupDefault);

        }
        }

        //make the save and cancel buttons visible again when the wrap panel is hidden
        GatClientUtility.setModalButtonsVisible(!GatClientUtility.isReadOnly(),true);

        if(getInstance().isAttached()) {

            //slide the wrap panel out
            getInstance().addStyleName("translateBelow");

            new Timer() {

                @Override
                public void run() {

                    //remove the wrap panel once the sliding animation is done
                    RootPanel.get().remove(getInstance());
                }
            }.schedule(500);
        }
    }

    /**
     * Gets the appropriate map widget for the given map type
     *
     * @param type the type of map that is needed
     * @return the appropriate widget needed to render the map of the given type
     */
    public AbstractMapPanel getMap(MapTypeEnum type) {

        final ServerProperties serverProperties = GatClientUtility.getServerProperties();
        final String googleMapsApiKey = serverProperties.getPropertyValue(ServerProperties.GOOGLE_MAPS_API_KEY);
        final boolean isServerMode = serverProperties.isServerMode();

        GoogleMapPanel.Properties mapProperties = new GoogleMapPanel.Properties(googleMapsApiKey, isServerMode,
                        new Command() {

                    @Override
                    public void execute() {
                        onMapReady();
                    }
                }, new GoogleMapPanel.AuthenticationHandler() {

                    @Override
                    public void onAuthenticationFailure(String errorTitle, String errorBody) {

                        // display a warning dialog upon an authentication error
                        WarningDialog.alert(errorTitle, errorBody);
                    }
                });

        if(MapTypeEnum.GOOGLE == type) {

            if(googleMapPanel == null) {

                //if the google map panel hasn't been created yet, create it
                googleMapPanel = new GoogleMapPanel(mapProperties);
            }

            return googleMapPanel;

        } else if(MapTypeEnum.UNITY == type) {
            if (unityMapPanel == null) {
                unityMapPanel = new UnityMapPanel(mapProperties);
            }

            return unityMapPanel;
        } else {
            return null;
        }
    }

    /**
     * Shows the map widget corresponding to the given map type
     *
     * @param type the type of map to be shown
     */
    private void showMap(MapTypeEnum type) {

        /* Hide the fullscreen and zoom buttons if the map type is unity since
         * the unity map doesn't support this functionality */
        boolean isNotUnity = type != MapTypeEnum.UNITY;
        fullscreenToggle.setVisible(isNotUnity);
        zoomInButton.setVisible(isNotUnity);
        zoomOutButton.setVisible(isNotUnity);

        AbstractMapPanel mapPanel = getMap(type);

        if(mapPanel != null) {

            if(mapDeck.getWidgetIndex(mapPanel) == -1){
                mapDeck.add(mapPanel);
            }

            currentMap = mapPanel;
            mapDeck.showWidget(mapDeck.getWidgetIndex(mapPanel));

        }
    }

    /**
     * Gets the map currently being shown by the wrap panel
     *
     * @return the current map
     */
    public AbstractMapPanel getCurrentMap() {
        return currentMap;
    }

    /**
     * Re-initializes the proper components once the current map is ready to be interacted with
     */
    public void onMapReady() {
        initOverlay();
    }

    /**
     * Initializes the map's overlay by refreshing its data
     */
    private void initOverlay() {

        if(overlay != null) {
            overlay.refreshPlacesOfInterest();
    }
    }

    /**
     * Gets whether or not the wrap overlay supports authoring places of interest for the given training application
     *
     * @return whether the current training application is supported
     */
    public static boolean isCurrentTrainingAppSupported() {

        TrainingApplicationEnum type = ScenarioClientUtility.getTrainingAppType();
        if(type == null){
            return false;
        }

        if (TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(type)) {
            return true;
        } else if (TrainingApplicationEnum.UNITY_EMBEDDED.equals(type)) {

            //must also check that scenario resources have been set to know which Unity imagery to
            //serve up to the gift wrap overlay.
            //NOTE: not currently checking the validity of the values in the support elements as that will
            //      be the responsibility of the overlay UI on demand as the underlying files can be manipulated
            //      post this check.
            generated.course.AuthoringSupportElements supportElements = ScenarioClientUtility.getAuthoringSupportElements();
            return supportElements != null;
        }

        return false;
    }

    /**
     * Gets the appropriate overlay that should be used to interact with the given scenario object. If no scenario object is
     * provided or no specific overlay exists for that object, the default overlay will be used.
     *
     * @param scenarioObject the scenario object that an overlay should be shown for. Can be null.
     * @return the appropriate editor for interacting with the given scenario object, or the default overlay, if no specific
     * overlay exists for that object
     */
    private static AbstractPlacesOfInterestOverlay getOverlay(Serializable scenarioObject) {

        if(scenarioObject instanceof Condition) {

            Condition condition = (Condition) scenarioObject;
            if(condition.getInput() != null) {

                if(condition.getInput().getType() instanceof AvoidLocationCondition) {
                    return new AvoidLocationConditionOverlay(condition);

                } else if(condition.getInput().getType() instanceof CorridorBoundaryCondition) {
                    return new CorridorBoundaryConditionOverlay(condition);

                } else if(condition.getInput().getType() instanceof EnterAreaCondition) {
                    return new EnterAreaConditionOverlay(condition);

                } else if(condition.getInput().getType() instanceof CheckpointPaceCondition) {
                    return new CheckpointPaceConditionOverlay(condition);

                } else if(condition.getInput().getType() instanceof CheckpointProgressCondition) {
                    return new CheckpointProgressConditionOverlay(condition);
                }
            }
        }

        return new PlacesOfInterestOverlay();
    }
}
