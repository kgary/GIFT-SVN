/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AnimateWrapper;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AbstractMapDataPoint.MappableData;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityFilterProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityFilterProvider.EntitySelectionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider.EntityStatusChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.GeolocationProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.GeolocationProvider.GeolocationUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider.RegisteredSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.shared.messages.DetonationUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.RemoveEntityMessage;
import mil.arl.gift.tools.dashboard.shared.messages.SessionEntityIdentifier;
import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.client.BoundsChangedCallback;
import mil.arl.gift.tools.map.client.MapClickedCallback;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Affiliation;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;
import mil.arl.gift.tools.map.client.draw.MilitarySymbolUrlGenerator;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.client.google.GoogleMapPanel;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * A panel used to track real-world domain sessions on an interactive map
 *
 * @author nroberts
 */
public class SessionsMapPanel extends Composite implements GeolocationUpdateHandler,
        EntitySelectionChangeHandler, SessionStateUpdateHandler, EntityStatusChangeHandler, RegisteredSessionChangeHandler {

    /** The CSS class name that provides the styling used to hide the entity settings panel */
    private static final String HIDE_ENTITY_SETTINGS_STYLE = "entitySettingsHidden";

    /** The amount of time (in milliseconds) that detonations should remain on the map when they are drawn*/
    private static final int DETONATION_DURATION_MILLIS = 3000;
    
    /** 
     * The amount of time (in milliseconds) that detonations should wait between checking the ellapsed time to
     * see if they should fade. This is used to let detonations persist on the map as long as a past session is
     * currently paused.
     */
    private static final int DETONATION_FADE_CHECK_INTERVAL = 500;

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionsMapPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionsMapPanelUiBinder uiBinder = GWT.create(SessionsMapPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionsMapPanelUiBinder extends UiBinder<Widget, SessionsMapPanel> {
    }

    /** The active session provider */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /**
     * The generator used to obtain the URLs needed to render military symbols
     * to the map
     */
    private static MilitarySymbolUrlGenerator milSymGenerator = new MilitarySymbolUrlGenerator(
            Dashboard.getInstance().getServerProperties().getMilitarySymbolServiceURL());
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The CSS applied to the map controls panel.
         *
         * @return the CSS style name
         */
        String hideZoomPanel();
    }
    
    /** The style from the ui.xml */
    @UiField
    protected Style style;
    
    @UiField
    protected FlowPanel mapControlsPanel;

    /** The panel containing the map */
    @UiField
    protected SimplePanel mapContainer;

    /** An icon button used to toggle full screen mode on an off */
    @UiField
    protected Icon fullscreenToggle;

    /**
     * An icon button used to reset the map's zoom level and position to show
     * all rendered shapes
     */
    @UiField
    protected Icon resetZoomButton;

    /** An icon button used to zoom in on the map */
    @UiField
    protected Icon zoomInButton;

    /** An icon button used to zoom out from the map */
    @UiField
    protected Icon zoomOutButton;

    /** The main panel container */
    @UiField
    protected FlowPanel mainPanel;

    /** The panel containing the minimap */
    @UiField
    protected SimplePanel minimapContainer;

    /**
     * A button that indicates how many entities are currently below expectation
     */
    @UiField
    protected Button alertButton;
       
    /** contains a google maps authentication error title to show to the user */
    private String googleMapAuthenticationErrorTitle = null;
    
    /** contains a google maps authentication error body to show to the user */
    private String googleMapAuthenticationErrorBody = null;

    /** The panel used to modify the settings for the currently selected entity, if applicable */
    @UiField
    protected EntitySettingsPanel entitySettingsPanel;

    /** The map currently being shown */
    private AbstractMapPanel currentMap = new GoogleMapPanel(new GoogleMapPanel.Properties(
            UiManager.getInstance().getGoogleMapsAPIKey(),
                    UiManager.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER,
                    new GoogleMapPanel.AuthenticationHandler() {

                        @Override
                        public void onAuthenticationFailure(String errorTitle, String errorBody) {
                            
                            // save for showing later
                            googleMapAuthenticationErrorTitle = errorTitle;
                            googleMapAuthenticationErrorBody = errorBody;
                        }
            }
    ));

    /**
     * A mapping from the host domain session ID of each knowledge session to the layer used to render that session's
     * entities on the map. Used to quickly look up which entities have been rendered for a particular session.
     */
    private final HashMap<Integer, SessionMapLayer> sessionIdToLayer = new HashMap<>();

    /** The entity that the user has currently selected */
    private AbstractMapDataPoint selectedMapData = null;

    /**
     * Whether the currently selected entity should be deselected the next time the map fires a "click" event.
     * This is used to determine if the user has clicked an empty area on the map without dragging the map to
     * move it, in which case, the currently selected shape should be deselected and its session data should
     * be hidden.
     */
    private boolean shouldDeselectEntityOnMapClick = false;

    /** The element to make full screen when the map's full screen button is clicked */
    private Element fullScreenTarget = null;

    /** A manager used to create and the minimap and keep it in sync with the main map */
    private MinimapManager minimapManager = new MinimapManager();

    /** The number of entities that are below expectation, which will be indicated by the alert button*/
    private int numAlerts = 0;

    /**
     * Whether the main map's bounds and zoom level should be automatically adjusted to fit new entities as they are
     * added to the map. This flag is used to allow the map to begin auto-fitting new entities once the first entity is
     * added to it and to stop auto-fitting once the user manually modifies the zoom level.
     */
    private boolean shouldAutoFitEntities;

    /** Whether the next bounds changed event that will be fired on the main map was triggered by auto-fitting entities */
    private boolean isAutoFittingEntities;

    /**
     * Creates a new panel capable of displaying the locations of entities and
     * learners in domain knowledge sessions on a map for the Game Master user
     * to observe and interact with
     *
     * @param parentGameMasterPanel the parent game master panel that contains
     *        this session map panel.
     */
    public SessionsMapPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
        
        logger.info("Using military symbol service at "+ milSymGenerator.getMilitarySymbolServiceURL());

        initWidget(uiBinder.createAndBindUi(this));

        /* Display a map that domain session data can be overlaid on top of */
        currentMap.setSize("100%", "100%");
        mapContainer.add(currentMap);
        minimapContainer.add(minimapManager.getMinimap());

        fullscreenToggle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (JsniUtility.getFullscreenElement() == null) {

                    if (fullScreenTarget != null) {

                        // this widget is not being shown in full screen mode, so
                        // show it in full screen mode
                        JsniUtility.requestFullscreen(fullScreenTarget);

                        fullscreenToggle.setColor("lightgray");
                        fullscreenToggle.getElement().getStyle()
                                .setBackgroundImage("radial-gradient(transparent, rgba(0, 0, 0, 0.25))");
                    }

                } else {

                    // this widget is currently in full screen mode, so exit
                    // full screen mode
                    JsniUtility.exitFullscreen();

                    fullscreenToggle.setColor("white");
                    fullscreenToggle.getElement().getStyle().clearBackgroundImage();
                }
            }
        });

        resetZoomButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                resetZoom();
            }
        });

        zoomInButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentMap != null) {
                    currentMap.zoomIn();
                }
            }
        });

        zoomOutButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentMap != null) {
                    currentMap.zoomOut();
                }
            }
        });

        currentMap.addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {

                //prepare to deselect the selected entity once we determine if the user has clicked an empty area
                shouldDeselectEntityOnMapClick = true;
            }
        }, MouseDownEvent.getType());

        currentMap.addDomHandler(new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {

                //cancel deselecting the selected entity if the mouse has moved, since panning the map can trigger this
                shouldDeselectEntityOnMapClick = false;
            }
        }, MouseMoveEvent.getType());

        currentMap.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (shouldDeselectEntityOnMapClick) {

                    // if the user clicks on the map while the session data panel is open, has not clicked a shape
                    // on the map, and has not panned the map, then deselect the currently selected shape and
                    // close the session data panel
                    setSelectedMapData(null);
                }

                shouldDeselectEntityOnMapClick = false;
            }
        }, ClickEvent.getType());

        entitySettingsPanel.setSettingChangedCommand(new Command() {

            @Override
            public void execute() {

                //redraw the currently selected entity whenever its settings are changed
                if (selectedMapData != null) {
                    selectedMapData.draw();
                }
            }
        });

        /* Subscribe to the data providers */
        subscribe();
    }
    
    /**
     * Show or hide the mini map.  Will also move the zoom controls.
     * @param show true to show the mini map, false to hide.
     */
    public void showMiniMap(boolean show) {
    	
    	if(show) {
    		mapControlsPanel.removeStyleName(style.hideZoomPanel());
    	}else{
    		mapControlsPanel.addStyleName(style.hideZoomPanel());
    	}
    }
    
    /**
     * Notification that the map panel is about to be shown.
     */
    public void notifyShowingMapPanel(){
        
        if(googleMapAuthenticationErrorBody != null){
            // there is a google maps authentication error that hasn't been show to the user yet
            
            // display a warning dialog upon an authentication error
            UiManager.getInstance().displayErrorDialog(googleMapAuthenticationErrorTitle, googleMapAuthenticationErrorBody, null);
            
            googleMapAuthenticationErrorBody = null;
            googleMapAuthenticationErrorTitle = null;
        }
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to any geolocation updates */
        GeolocationProvider.getInstance().addManagedHandler(this);

        /* Subscribe to the filter for entity events */
        EntityFilterProvider.getInstance().addManagedHandler(this);

        /* Subscribe to the knowledge session state updates */
        SessionStateProvider.getInstance().addManagedHandler(this);

        /* Subscribe to the entity status updates */
        EntityStatusProvider.getInstance().addManagedHandler(this);

        /* Subscribe to registered session changes */
        RegisteredSessionProvider.getInstance().addManagedHandler(this);
    }

    /**
     * For a single domain session, erase all of the domain knowledge session
     * information that has been drawn to the map, including the shapes used to
     * represent entities and learners.
     *
     * @param domainSessionId the domain session id
     */
    private void removeShapesForDomainSession(final int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeShapesForDomainSession(" + domainSessionId + ")");
        }

        /* Remove the layer used to render entities for the domain session with
         * the given ID */
        SessionMapLayer sessionLayer = sessionIdToLayer.remove(domainSessionId);
        if (sessionLayer != null) {
            sessionLayer.clear();
        }
    }

    /**
     * Updates the state of an entity of the map. If the entity does not have
     * a shape on the map that is associated with it yet, then one will be
     * created at the appropriate location. If the entity DOES have an
     * associated shape, then it will be updated to appear at the proper
     * location.
     *
     * @param update an update indicating the state of an entity in a
     *        domain knowledge session. Cannot be null.
     */
    private void updateEntityState(final EntityStateUpdate update) {

        /* Determine if a shape representing this learner already exists */
        final SessionEntityIdentifier sessionEntityId = update.getSessionEntityId();
        final int hostDomainSessionId = sessionEntityId.getHostDomainSessionId();

        AbstractKnowledgeSession session = activeSessionProvider
                .getActiveSessionFromDomainSessionId(hostDomainSessionId);
        if (session == null || session.getTeamStructure() == null) {
            /* This session can't be found or doesn't have a team */
            return;
        }

        SessionMapLayer sessionLayer = sessionIdToLayer.get(hostDomainSessionId);
        if (sessionLayer == null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Creating SessionMapLayer for domain session " + hostDomainSessionId);
            }

            /* create a layer to handle rendering this session's entities */
            sessionLayer = new SessionMapLayer(this, hostDomainSessionId);
            sessionIdToLayer.put(hostDomainSessionId, sessionLayer);
        }

        EntityDataPoint shape = sessionLayer.getShape(sessionEntityId);

        /* If a shape has not existed previously and the active flag is false,
         * return early since no shape should be drawn anyway. */
        if (shape == null && !update.isActive()) {
            return;
        }

        if (shape == null) {

            /* Create a new shape for this entity and save a mapping for it so
             * it can be updated */
            shape = new EntityDataPoint(update, sessionLayer);

            if (isMapEmpty()) {

                /* If the map is empty, begin auto-fitting the map's bounds to
                 * it as new entities are added */
                setAutoFitEntities(true);
            }

            if (shouldAutoFitEntities) {

                /* Ensure that the following zoom operation is not treated as if
                 * the user manually changed the map's bounds, since that
                 * cancels auto-fitting */
                isAutoFittingEntities = true;

                /* Reset the map's zoom level to fit all of the entity that is
                 * being added */
                resetZoom();
            }

            /* Add this entity's shape to the global mapping and its session
             * layer */
            sessionLayer.addShapeToLayer(shape);

        } else {
            /* Update the state of the entity's existing shape */
            shape.setState(update);
        }

        /* check if the entity should be removed based on being deactivated */
        if (!update.isActive()) {
            logger.info("Removing deactivated entity " + update.getEntityMarking());
            sessionLayer.removeEntity(sessionEntityId);
        } else {

            /* Render the learner's shape based on its current data */
            shape.draw();
        }
    }

    /**
     * Selects the given map data, changing the appearance of the shape used to render it and centering the
     * map's view around it. If the given map data point is null or a different data point then the previously
     * selected data point, then the previously selected data point will be deselected.  If the data point is
     * the same as the currently selected and the data point selection can be toggled, then the data point will
     * be deselected.  If the data point can't be toggled than it will remain selected.  
     *
     * @param mapData the map data point to select. Can be null. 
     */
    public void setSelectedMapData(AbstractMapDataPoint mapData) {
        
        boolean isCurrentlySelectedObject = Objects.equals(mapData, selectedMapData);
        boolean currentlySelectedCanToggle = mapData != null && mapData.isToggleSelected();
        if(isCurrentlySelectedObject && !currentlySelectedCanToggle){
            // the currently selected object is the object being selected and the object being selected
            // can't be toggled by selecting it, so nothing to do.
            return;
        }

        //
        // change the state of the currently selected entity shape
        //
        
        if (selectedMapData != null && !isCurrentlySelectedObject) {
            // deselect the previously selected entity shape which is not the shape being selected now
            selectedMapData.setSelected(false);
            selectedMapData.draw();
            selectedMapData = null;
        }

        if (mapData != null) {
            // select the given entity shape
            
            selectedMapData = mapData;
            selectedMapData.setSelected(true);
            selectedMapData.draw();

            // center the map on
            PointShape<?> centerPoint = selectedMapData.getMapPoint();

            if (centerPoint != null) {
                currentMap.centerView(false, centerPoint);
            }

            // because the selected value can be toggled now, need to check it after calling setSelected above
            if(selectedMapData.isSelected()){
                // show the settings for the selected entity
                entitySettingsPanel.removeStyleName(HIDE_ENTITY_SETTINGS_STYLE);
            }else{
                // hide the settings for the selected entity
                entitySettingsPanel.addStyleName(HIDE_ENTITY_SETTINGS_STYLE);
            }

        } else {

            // hide the settings for the selected entity
            entitySettingsPanel.addStyleName(HIDE_ENTITY_SETTINGS_STYLE);
        }

    }

    /**
     * Get the selected map data point
     *
     * @return the selected map data point
     */
    public AbstractMapDataPoint getSelectedMapData() {
        return selectedMapData;
    }

    /**
     * Resets the zoom level and position of the map's view to show all of the
     * domain knowledge session entities that are rendered to the map
     */
    public void resetZoom() {

        final int mapCount = getMapEntityCount();
        if (mapCount != 0) {

            // determine the map shapes that were drawn so the map can be
            // centered around them
            PointShape<?>[] mapShapes = new PointShape<?>[mapCount];
            int i = 0;

            for (SessionMapLayer layer : sessionIdToLayer.values()) {
                for (PointShape<?> mapPoint : layer.getMapPoints()) {
                    mapShapes[i++] = mapPoint;
                }
            }

            // center the map around the shapes of the places of interest that
            // were drawn
            if (mapShapes.length > 0) {
                currentMap.centerView(true, mapShapes);
            }
        }
    }

    /**
     * Checks if the map is currently empty.
     *
     * @return true if the map is empty; false otherwise.
     */
    public boolean isMapEmpty() {
        for (SessionMapLayer layer : sessionIdToLayer.values()) {
            if (layer.getEntitySize() != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Counts the number of entities on the map.
     *
     * @return the number of entities on the map.
     */
    public int getMapEntityCount() {
        int count = 0;

        for (SessionMapLayer layer : sessionIdToLayer.values()) {
            count += layer.getEntitySize();
        }

        return count;
    }
    
    /**
     * Gets the map layer that is being used to display map data for the domain session with the given ID
     * 
     * @param domainSessionId the ID of the domain session whose map layer is needed
     * @return the map layer corresponding to the given domain session ID
     */
    public SessionMapLayer getSessionMapLayer(int domainSessionId) {
        return sessionIdToLayer.get(domainSessionId);
    }

    /**
     * Deselect any selected entities on the map.
     */
    public void deselectEntities() {
        /* Deselect the currently selected shape */
        setSelectedMapData(null);
    }

    /**
     * Add a click handler to the alert button.
     *
     * @param handler the click handler to add.
     * @return the handler registration.
     */
    public HandlerRegistration addAlertButtonClickHandler(ClickHandler handler) {
        return alertButton.addClickHandler(handler);
    }

    /**
     * Adds the given number to the alert button used to indicate how many
     * entities currently need attention (i.e. are below expectation). If the
     * total number of entities in need of attention is greater than 0, the
     * alert button will be shown; otherwise, said button will be hidden.
     *
     * @param alertCount the number of entities in need of attention to add to
     *        the alert count
     */
    public void addAlerts(int alertCount) {

        numAlerts += alertCount;

        if (numAlerts > 0) {

            /* Show number of entity alerts */
            alertButton.setText(Integer.toString(numAlerts));
            alertButton.getElement().getStyle().setProperty("transform", "none");

        } else {

            /* Hide number of entity alerts */
            alertButton.setText(null);
            alertButton.getElement().getStyle().clearProperty("transform");
        }
    }

    /**
     * Returns the entity roles for each domain session that are part of the
     * alerts.
     *
     * @return the entity roles for each domain session that are part of the
     *         alerts. Will never be null.
     */
    public Map<Integer, Set<String>> getAlertEntityRoles() {
        Map<Integer, Set<String>> alertEntities = new HashMap<>();

        for (Entry<Integer, SessionMapLayer> entry : sessionIdToLayer.entrySet()) {
            SessionMapLayer mapLayer = entry.getValue();
            final Set<String> alertRoles = mapLayer.getAlertRoles();
            if (!alertRoles.isEmpty()) {
                alertEntities.put(entry.getKey(), alertRoles);
            }
        }

        return alertEntities;
    }

    /**
     * Gets the generator that should be used to generate the URLs needed to
     * render military symbols to the map
     *
     * @return the military symbol URL generator
     */
    public static MilitarySymbolUrlGenerator getMilitarySymbolGenerator() {
        return milSymGenerator;
    }

    @Override
    public void registeredSessionChanged(AbstractKnowledgeSession newSession, AbstractKnowledgeSession oldSession) {
        if (oldSession != null) {
            removeShapesForDomainSession(oldSession.getHostSessionMember().getDomainSessionId());
        }
    }

    @Override
    public void logPatchFileChanged(String logPatchFileName) {
        /* Nothing to do */
    }

    @Override
    public void entityLocationUpdate(EntityStateUpdate update) {
        updateEntityState(update);
    }

    @Override
    public void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId) {
        SessionMapLayer sessionLayer = sessionIdToLayer.get(domainSessionId);
        if (sessionLayer != null) {
            /* Null assessment means that entity received a "visual only" state.
             * Do not process further. This was implemented as a quick hack for
             * ARES visualization. */
            if (state != null && state.getLearnerState() != null) {
                PerformanceState performance = state.getLearnerState().getPerformance();
                for (TaskPerformanceState perfState : performance.getTasks().values()) {
                    for (ConceptPerformanceState c : perfState.getConcepts()) {
                        if (c.getState().getAssessedTeamOrgEntities().containsValue(null)) {
                            return;
                        }
                    }
                }
            }

            sessionLayer.updateTeamRoleAssessments(state);
        }
    }

    /**
     * Sets the element that should be made fullscreen when the map's full screen button is clicked
     *
     * @param element the element to make full screen. If null, clicking the full screen button will do nothing.
     */
    public void setFullScreenTarget(Element element) {
        fullScreenTarget = element;
    }

    /**
     * Sets whether the map should automatically adjust its bounds to fit any
     * new entities that are added to it. If the user manually alters the map's
     * bounds, auto-fitting will automatically be disabled to avoid interfering
     * with map controls.
     *
     * @param autoFit whether to the map should automatically fit new entities.
     */
    public void setAutoFitEntities(boolean autoFit) {
        this.shouldAutoFitEntities = autoFit;
    }

    /**
     * Gets the settings for entities that are selected on the map
     *
     * @return the entity settings. Will not be null.
     */
    public EntitySettingsPanel getEntitySettings() {
        return entitySettingsPanel;
    }

    /**
     * Creates a shape on the map to render the data from the given data point
     *
     * @param mapData the map data point to create a shape for. Cannot be null.
     * @return the shape created for the given data. Will not be null.
     */
    public MapShape createMapShape(AbstractMapDataPoint mapData) {
        return new MapShape(mapData.getMappableData(), currentMap, minimapManager.getMinimap());
    }

    /**
     * Notifies the map that the shape used to render the given map data in the given layer has been clicked on
     *
     * @param layer the layer that the map data is a part of. If null, this method will do nothing.
     * @param dataSource the map data point whose shape was clicked on. If null, this method will do nothing.
     */
    public void onDataPointClicked(SessionMapLayer layer, AbstractMapDataPoint dataSource) {

        if (layer == null || dataSource == null) {
            return;
        }

        /* Cancel deselecting the selected entity when the mouse is
         * clicked, since we want to select another entity */
        shouldDeselectEntityOnMapClick = false;

        /* Select this shape when the user clicks on it */
        setSelectedMapData(dataSource);
    }

    /**
     * An object used to create and manage the minimap so that it works in tandem with the main map and synchronizes
     * its behavior accordingly
     *
     * @author nroberts
     */
    private class MinimapManager {

        /** The zoom level of the minimap relative to the main map's zoom level */
        private static final int MINIMAP_ZOOM_DELTA = -4; //zoom minimap out 4 levels

        /**
         * Whether to ignore the next bounds changed event fired from the map or minimap. Used to avoid
         * endlessly looping when synchronizing the map's and minimap's bounds, since changing either
         * map's bounds will also automatically change the other's.
         */
        private boolean ignoreNextBoundsChangedEvent = false;

        /** The minimap accompanying the main map */
        private AbstractMapPanel minimap;

        /**
         * Creates a new manager and initializes the minimap
         */
        private MinimapManager() {

            //create a minimap to show an overview of all data rendered to the main map
            GoogleMapPanel.Properties minimapProperties = new GoogleMapPanel.Properties(
                    UiManager.getInstance().getGoogleMapsAPIKey(),
                    UiManager.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER
            );

            minimapProperties.setMapTypeControllable(false);

            minimap = new GoogleMapPanel(minimapProperties);
            minimap.setSize("100%", "100%");

            //listen for when the main map's bounds are changed (via resizing, panning, or zooming)
            currentMap.setBoundsChangedHandler(new BoundsChangedCallback() {

                @Override
                public void onBoundsChanged(Bounds bounds) {

                    if (isAutoFittingEntities) {

                        //reset the auto-fitting status so we can detect when the user manually changes the map bounds
                        isAutoFittingEntities = false;

                    } else if (shouldAutoFitEntities) {

                        /*
                         * once the user manually modifies the map bounds, stop auto-fitting new entities so
                         * that the user can move the map freely without interruption
                         */
                        setAutoFitEntities(false);
                    }

                    if (bounds != null) {

                        //adjust the bounds of the minimap so they sync up with the main map's new bounds
                        synchronizeMinimapBoundsToMap(bounds.getCenter(), bounds.getZoomLevel());
                    }
                }
            });

            //listen for when the minimap's bounds are changed (via resizing, panning, or zooming)
            minimap.setBoundsChangedHandler(new BoundsChangedCallback() {

                @Override
                public void onBoundsChanged(Bounds bounds) {

                    if (bounds != null) {

                        //adjust the bounds of the main map so they sync up with the minimap's new bounds
                        synchronizeMapBoundsToMinimap(bounds.getCenter(), bounds.getZoomLevel());
                    }
                }
            });

            // listen for when the minimap is clicked
            minimap.setMapClickedHandler(new MapClickedCallback() {

                @Override
                public void onClick(AbstractMapCoordinate position) {

                    if (position != null) {
                        minimap.panTo(position, null); //pan the minimap to the location that was clicked without zooming
                    }
                }
            });
        }

        /**
         * Pans the minimap and adjusts its zoom level so that it synchronizes
         * with the provided center point and zoom level for the main map
         *
         * @param center the center point of the main map. Cannot be null.
         * @param zoomLevel the zoom level of the main map
         */
        private void synchronizeMinimapBoundsToMap(AbstractMapCoordinate center, int zoomLevel) {

            if (!ignoreNextBoundsChangedEvent) {

                minimap.panTo(center, zoomLevel + MINIMAP_ZOOM_DELTA);
                ignoreNextBoundsChangedEvent = true;

            } else {
                ignoreNextBoundsChangedEvent = false;
            }
        }

        /**
         * Pans the main map and adjusts its zoom level so that it synchronizes
         * with the provided center point and zoom level for the minimap
         *
         * @param center the center point of the minimap. Cannot be null.
         * @param zoomLevel the zoom level of the minimap
         */
        private void synchronizeMapBoundsToMinimap(AbstractMapCoordinate center, int zoomLevel) {

            if (!ignoreNextBoundsChangedEvent) {

                currentMap.panTo(center, zoomLevel - MINIMAP_ZOOM_DELTA);
                ignoreNextBoundsChangedEvent = true;

            } else {
                ignoreNextBoundsChangedEvent = false;
            }
        }

        /**
         * Gets the minimap that this manager created and is managing
         *
         * @return the minimap. Will not be null.
         */
        public AbstractMapPanel getMinimap() {
            return minimap;
        }
    }

    @Override
    public void entitySelected(int domainSessionId, Set<String> entityRole) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("entitySelected(");
            List<Object> params = Arrays.<Object>asList(domainSessionId, entityRole);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        /* Zoom the map to the selected entity */
        SessionMapLayer sessionLayer = sessionIdToLayer.get(domainSessionId);
        if (sessionLayer == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("No session layer for domain session id " + domainSessionId);
            }

            return;
        }

        final List<EntityDataPoint> points = new ArrayList<>();
        final List<PointShape<?>> shapes = new ArrayList<>();
        for (String role : entityRole) {
            EntityDataPoint dataPoint = sessionLayer.getShape(role);
            if (dataPoint != null) {
                points.add(dataPoint);
                shapes.add(dataPoint.getMapPoint());
            }
        }

        if (shapes.isEmpty()) {
            return;
        }

        currentMap.centerView(shapes.size() > 1, shapes.toArray(new PointShape[shapes.size()]));
        for (EntityDataPoint point : points) {
            point.playPulseAnimation();
        }
    }

    @Override
    public void entityStatusChange(int domainSessionId, String teamRole, Status newStatus, Status oldStatus) {
        SessionMapLayer mapLayer = sessionIdToLayer.get(domainSessionId);
        if (mapLayer == null) {
            return;
        }

        EntityDataPoint shape = mapLayer.getShape(teamRole);
        if (shape == null) {
            return;
        }

        if (Status.PRESENT_DESTROYED.equals(oldStatus)) {
            mapLayer.removeAlertEntity(teamRole);
        } else if (Status.PRESENT_DESTROYED.equals(newStatus)) {
            mapLayer.addAlertEntity(teamRole);
        }
    }

    /**
     * Gets the location of the given locations' centroid (i.e. the center of mass) on the map
     *
     * @param locations the locations to get the centroid for. Cannot be null or empty.
     * @return the calculated centroid location. Will not be null.
     */
    public AbstractMapCoordinate getCentroid(AbstractMapCoordinate... locations) {
        return currentMap.getCentroid(locations);
    }

    @Override
    public void removeEntityRequest(RemoveEntityMessage remove) {

        final SessionEntityIdentifier sessionEntityId = remove.getSessionEntityId();
        final int hostDomainSessionId = sessionEntityId.getHostDomainSessionId();

        SessionMapLayer sessionLayer = sessionIdToLayer.get(hostDomainSessionId);
        if (sessionLayer != null) {
            sessionLayer.removeEntity(sessionEntityId);
        }

    }

    /**
     * Redraw the entity and team map icons by redrawing each known session map layer.
     */
    public void redrawDataPoints() {

        Iterator<SessionMapLayer> sessionMapLayerItr = sessionIdToLayer.values().iterator();
        while (sessionMapLayerItr.hasNext()) {
            sessionMapLayerItr.next().redrawDataPoints();
        }
    }

    @Override
    public void detonationLocationUpdate(final DetonationUpdate update) {

        //render a yellow circle military symbol to represent the detonation
        SIDC detonationSIDC = new SIDC("SFSP-----------");
        
        MilitarySymbol detonationSymbol = new MilitarySymbol(Affiliation.FRIEND, Status.PRESENT);
        
        if(update.getDetonationType() != null && update.getDetonationType().equals(DetonationResultEnum.ENTITY_IMPACT)){
            // yellow detonation circle
            detonationSymbol.setFillColor("0xffff00");
            detonationSymbol.setLineColor("0xffff00");
        }else{
            // gray detonation circle
            detonationSymbol.setFillColor("0x999999");
            detonationSymbol.setLineColor("0x999999");
        }
        
        //create and draw a map shape to display the detonation on the map
        MappableData drawData = new MappableData(update.getLocation(), null,
                milSymGenerator.getSymbolUrl(detonationSymbol, detonationSIDC),
                AbstractMapDataPoint.DETONATION_Z_INDEX, null, false);

        final PolylineShape<?> fireLine;
        
        final MapShape shape = new MapShape(drawData, currentMap, minimapManager.getMinimap());
        shape.draw(drawData);
        
        if(update.getFiringEntityLocation() != null) {
        
            Affiliation affiliation = Affiliation.getAffiliationFromForceId(update.getForceId());
            
            List<AbstractMapCoordinate> vertices = new ArrayList<>();
            vertices.add(update.getLocation());
            vertices.add(update.getFiringEntityLocation());
            
            fireLine = currentMap.createPolyline(vertices);
            String color;
            switch(affiliation) {
                case UNKNOWN:
                case PENDING:
                case NONE_SPECIFIED:
                    color = "yellow";
                    break;
                case ASSUMED_FRIEND:
                case FRIEND:
                    color = "blue";
                    break;
                case NEUTRAL:
                    color = "lightgreen";
                    break;
                case SUSPECT: 
                case HOSTILE:
                    color = "red";
                    break;   
                default:
                    color = "yellow";
            }
            
            fireLine.setColor(color);
            fireLine.draw();
            
        } else {
            
            /* Do not draw a line if there is no firing entity location */
            fireLine = null;
        }

        new Timer() {

            @Override
            public void run() {

                final Element mapElement = shape.getMapElement(currentMap.getElement());
                final Element minimapElement = shape.getMapElement(minimapManager.getMinimap().getElement());

                if(mapElement != null && minimapElement != null) {
                    
                    mapElement.addClassName(MapShape.ENTITY_STYLE);

                    //animate the detonation's appearance
                    Animate.animate(new AnimateWrapper(mapElement), Animation.BOUNCE_IN, 1, DETONATION_DURATION_MILLIS/2);
                    Animate.animate(new AnimateWrapper(minimapElement), Animation.BOUNCE_IN, 1, DETONATION_DURATION_MILLIS/2);
                }
            }

        }.schedule(200); //map doesn't expose exactly when DOM element is added, so use a slight delay
        

        new Timer() {
            
            private Long pauseTime = null;
            
            /** The amount of unpaused time that has passed while this timer is active */
            private long sessionTimePassed = 0;
            
            boolean fadeStarted = false;

            @Override
            public void run() {
                
                final Element mapElement = shape.getMapElement(currentMap.getElement());
                final Element minimapElement = shape.getMapElement(minimapManager.getMinimap().getElement());
                    
                if(TimelineProvider.getInstance().isPaused()
					&& ActiveSessionProvider.RunState.RUNNING.equals(activeSessionProvider.getRunState(update.getHostDomainSessionId()))) {
                    
                    if(pauseTime == null) {
                        pauseTime = TimelineProvider.getInstance().getPlaybackTime();
                        
                        if(fadeStarted && mapElement != null && minimapElement != null) {
                            
                            /* Pause the fading animation */
                            mapElement.getStyle().setProperty("animationPlayState", "paused");
                            minimapElement.getStyle().setProperty("animationPlayState", "paused");
                        }
                    }
                    
                } else {
                    pauseTime = null;  
                    
                    if(mapElement != null && minimapElement != null) {
                    
                        /* Stop pausing the fading animation */
                        mapElement.getStyle().clearProperty("animationPlayState");
                        minimapElement.getStyle().clearProperty("animationPlayState");
                    }
                }
                
                if(pauseTime != null) {
                    
                    /* Check if there is a difference between the current playback time and the time that
                     * was paused, and if so, add that to the ellapsed time. This is used to account for
                     * when the playhead is moved while the session is paused. */
                    sessionTimePassed += TimelineProvider.getInstance().getPlaybackTime() - pauseTime;
                    
                } else {
                    
                    /* Since the session was not paused, just add the timer interval to the ellapsed time */
                    sessionTimePassed += DETONATION_FADE_CHECK_INTERVAL;
                }
                    
                if(DETONATION_DURATION_MILLIS/2 - Math.abs(sessionTimePassed) >= 0) {
                    
                    /* Not enough session time has ellapsed to begin the fading animation. Note the Math.abs
                     * is used above to account for when the playhead is moved backwards while paused. */
                    return;
                    
                } else if(!fadeStarted && mapElement != null && minimapElement != null) {

                    //animate the detonation's disappearance
                    Animate.animate(new AnimateWrapper(mapElement), Animation.FADE_OUT, 1, DETONATION_DURATION_MILLIS/2);
                    Animate.animate(new AnimateWrapper(minimapElement), Animation.FADE_OUT, 1, DETONATION_DURATION_MILLIS/2);
                    
                    fadeStarted = true;
                }
                
                if(DETONATION_DURATION_MILLIS - Math.abs(sessionTimePassed) >= 0) {
                    
                    /* Not enough session time has ellapsed to finally erase the detonation. Note the Math.abs
                     * is used above to account for when the playhead is moved backwards while paused. */
                    return;
                }
                
                shape.erase(); //erase the detonation from the map after a certain amount of time
                
                if(fireLine != null) {
                    fireLine.erase();
                }
                
                cancel();
            }

        }.scheduleRepeating(DETONATION_FADE_CHECK_INTERVAL); 

        
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        /* Nothing to do */
    }
}
