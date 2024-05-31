/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.google;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.maps.client.controls.MapTypeControlOptions;
import com.google.gwt.maps.client.drawinglib.DrawingManager;
import com.google.gwt.maps.client.drawinglib.DrawingManagerOptions;
import com.google.gwt.maps.client.drawinglib.OverlayType;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapEvent;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapHandler;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.marker.MarkerCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.marker.MarkerCompleteMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.polyline.PolylineCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.polyline.PolylineCompleteMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.PolygonOptions;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.Command;

import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.client.BoundsChangedCallback.Bounds;
import mil.arl.gift.tools.map.client.MapControlModeEnum;
import mil.arl.gift.tools.map.client.draw.AbstractMapShape;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolygonShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;
import mil.arl.gift.tools.map.shared.GDC;

/**
 * A panel that renders a map using the Google Maps platform. Note that a Google Maps API key must be
 * provided in order to actually use Google's services and display the map.
 *
 * @author nroberts
 */
public class GoogleMapPanel extends AbstractMapPanel{

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GoogleMapPanel.class.getName());

    /**
     * An enumeration describing the states that the Google Maps JavaScript API can be in
     * 
     * @author nroberts
     */
    private static enum GoogleMapsApiStatus{
        NOT_LOADED,
        LOADING,
        LOADED
    }
    
    /** The status of the Google Maps JavaScript API associated with the surrounding window */
    private static GoogleMapsApiStatus apiStatus = GoogleMapsApiStatus.NOT_LOADED;
    
    /** The instances of this class that are currently waiting for the Google Maps JavaScript API to load */
    private static List<GoogleMapPanel> instancesWaitingForAPILoad = new ArrayList<>();
    
    /** A widget containing the map rendered by Google Maps */
    private MapWidget map;

    /** The drawing controls used to allow users to draw on the map */
    private DrawingManager drawingManager;

    /** A set of properties that this map uses to determine its behavior, including its loading parameters */
    private Properties mapProperties = null;

    /**
     * Creates a new panel containing a map rendered by Google Maps and initializes it. Note that a Google Maps API key must be provided
     * by the given properties in order to allow Google Maps' features to be used.
     * <br/><br/>
     * Bear in mind that it is important to keep the same map attached to the page as long as possible, since every
     * load of the map counts as a single use for Google Maps' pricing policy. This constructor should be called sparingly, and
     * instances of this widget should be reused whenever possible. Panning and zooming the map once it is loaded does
     * not count toward usage as far as pricing is concerned.
     *
     * @param mapProperties the properties defining the behavior of the map, including its loading parameters. Cannot be null.
     */
    public GoogleMapPanel(Properties mapProperties) {
        super(mapProperties != null ? mapProperties.getOnMapReadyCommand() : null);
        
        if(mapProperties == null) {
            throw new IllegalArgumentException("The properties defining a Google map's behavior cannot be null.");
    }
        
        this.mapProperties = mapProperties;

        if(apiStatus == GoogleMapsApiStatus.LOADED) {
            
            // The Google Maps JavaScript API has already been loaded by another
            // instance of this widget, so continue using
            // the API that was loaded previously to avoid potential errors from
            // loading the API multiple times
            initializeMap();
            
        } else if(apiStatus == GoogleMapsApiStatus.LOADING){
            
            // Another instance of this widget has kicked off a call to load the
            // JavaScript API, so wait for that load to finish
            instancesWaitingForAPILoad.add(this);
            
        } else {
            
            // The Google Maps JavaScript API has not been loaded to this window
            // yet, so we need to load it
            apiStatus = GoogleMapsApiStatus.LOADING;
            
        initNativeFunctions();
            
            if (isBlank(mapProperties.getApiKey())) {
                
            StringBuilder warningMessage = new StringBuilder()
                    .append("A Google Maps API key has not been provided for use with this instance of GIFT. ")
                    .append("As a result, certain features of this interface that utilize Google Maps may not work properly.<br/><br/>");
                
                if(mapProperties.isServerMode()) {
                warningMessage.append("Please try again to see if the problem persists.")
                            .append("<br/><br/>If it happens again there are a few things you can check")
                            .append("to help determine the cause:<br/>")
                            .append("<ol><li>Check the browser developer console for more error details</li>")
                            .append("<li>If you have access to the GIFT configuration files,")
                            .append("make sure your TutorURL property is set correctly in GIFT/config/common.properties</li>")
                            .append("<li>If you do not have access to the GIFT configuration files,")
                            .append("contact your server administrator and provide them with the contents of this message");
                    
            } else {
                warningMessage.append("Make sure your GoogleMapsApiKey property is set to a valid ")
                            .append("Google Maps API key in GIFT/config/common.properties");
            }
                
                if(mapProperties.getAuthHandler() != null) {
                    mapProperties.getAuthHandler().onAuthenticationFailure("Google Maps API key not found",
                            warningMessage.toString());
            }
        }
            
            // define all of the libraries in the Google Maps JavaScript API
            // that need to be loaded
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<>();
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);
        loadLibraries.add(LoadLibrary.VISUALIZATION);
            
            instancesWaitingForAPILoad.add(this);
            
            //begin loading the Google Maps JavaScript API
        LoadApi.go(new Runnable() {
            @Override
            public void run() {
                    initializeAllWaitingMaps();
            }
            }, loadLibraries, false, "key=" + mapProperties.getApiKey());
    }
    }

    /**
     * Initializes all instances of this widget that are currently waiting for
     * the Google Maps JavaScript API to be loaded
     */
    private static void initializeAllWaitingMaps() {
        
        apiStatus = GoogleMapsApiStatus.LOADED;
        
        Iterator<GoogleMapPanel> itr = instancesWaitingForAPILoad.iterator();
        while(itr.hasNext()) {
            
            itr.next().initializeMap();
            itr.remove();
        }
    }
    
    /**
     * Initializes the map widget with its size, controls, and event handlers.
     * The Google Maps API MUST be loaded before calling this method, or else an
     * error will occur.
     */
    protected void initializeMap() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initializeMap()");
        }

        map = createMapWidget();
        setWidget(map);

        drawingManager = createDrawingManager();

        notifyMapReady();
    }

    /**
     * Creates the {@link MapWidget} during initialization of the
     * {@link GoogleMapPanel}.
     *
     * @return An initialized {@link MapWidget}. Can't be null.
     */
    protected MapWidget createMapWidget() {
        final MapOptions opts = createMapOptions();
        map = new MapWidget(opts);
        map.setSize("100%", "100%");
        return map;
    }

    /**
     * Creates the {@link MapOptions} during initialization of the
     * {@link GoogleMapPanel}.
     *
     * @return An initialized {@link MapOptions}. Can't be null.
     */
    protected MapOptions createMapOptions() {

        MapOptions opts = MapOptions.newInstance();
        opts.setZoom(5);
        opts.setMapTypeId(MapTypeId.HYBRID);
        
        if(mapProperties.isMapTypeControllable()) {
        MapTypeControlOptions mapTypeControlOptions = MapTypeControlOptions.newInstance();
            MapTypeId[] mapTypeIds = new MapTypeId[]{MapTypeId.HYBRID, MapTypeId.ROADMAP};
        mapTypeControlOptions.setMapTypeIds(mapTypeIds);
        mapTypeControlOptions.setPosition(ControlPosition.BOTTOM_CENTER);
            
        opts.setMapTypeControlOptions(mapTypeControlOptions);
        opts.setMapTypeControl(true);
        } else {
            //prevent the user from changing the map type
            opts.setMapTypeControl(false);
        }
        
        //hide the default UI controls, since this widget will provide all the controls that are needed
        opts.setStreetViewControl(false);
        opts.setZoomControl(false);
        opts.setDisableDefaultUi(true);
        
        //determine if the user should be able to control the map bounds
        opts.setDraggable(mapProperties.areBoundsControllable());
        opts.setDisableDoubleClickZoom(!mapProperties.areBoundsControllable());
        opts.setScrollWheel(mapProperties.areBoundsControllable());
        
        // show the United states by default
        LatLng unitedStatesApproxCenter = LatLng.newInstance(38.0355994, -95.7809798);
        opts.setCenter(unitedStatesApproxCenter);

        return opts;
    }

    /**
     * Creates the {@link DrawingManager} during initialization of the
     * {@link GoogleMapPanel}.
     *
     * @return An initialized {@link DrawingManager}. Can't be null.
     */
    protected DrawingManager createDrawingManager() {
        DrawingManagerOptions drawOptions = DrawingManagerOptions.newInstance();
        drawOptions.setDrawingControl(false); //hide the default drawing controls

        MarkerOptions markerOptions = MarkerOptions.newInstance();
        setDefaultMarkerIcon(markerOptions, AbstractMapShape.DEFAULT_SHAPE_COLOR);
        drawOptions.setMarkerOptions(markerOptions);

        PolylineOptions polylineOptions = PolylineOptions.newInstance();
        polylineOptions.setStrokeWeight(4);
        polylineOptions.setStrokeColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        drawOptions.setPolylineOptions(polylineOptions);

        PolygonOptions polygonOptions = PolygonOptions.newInstance();
        polygonOptions.setStrokeWeight(4);
        polygonOptions.setStrokeColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        polygonOptions.setFillColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        drawOptions.setPolygonOptions(polygonOptions);

        drawingManager = DrawingManager.newInstance(drawOptions);

        // handle when a marker (i.e. a point) is drawn
        drawingManager.addMarkerCompleteHandler(new MarkerCompleteMapHandler() {

            @Override
            public void onEvent(MarkerCompleteMapEvent event) {

                GoogleMapsPointShape shape = new GoogleMapsPointShape(GoogleMapPanel.this, event.getMarker());

                onManualDrawingComplete(shape);
            }
        });

        // handle when a polyline is drawn
        drawingManager.addPolylineCompleteHandler(new PolylineCompleteMapHandler() {

            @Override
            public void onEvent(PolylineCompleteMapEvent event) {

                GoogleMapsPolylineShape shape = new GoogleMapsPolylineShape(GoogleMapPanel.this, event.getPolyline());

                onManualDrawingComplete(shape);
            }
        });

        // handle when a polygon is drawn
        drawingManager.addPolygonCompleteHandler(new PolygonCompleteMapHandler() {

            @Override
            public void onEvent(PolygonCompleteMapEvent event) {

                GoogleMapsPolygonShape shape = new GoogleMapsPolygonShape(GoogleMapPanel.this, event.getPolygon());

                onManualDrawingComplete(shape);
            }
        });

        //handle when the map's bounds change
        map.addBoundsChangeHandler(new BoundsChangeMapHandler() {
            
            @Override
            public void onEvent(BoundsChangeMapEvent event) {
                
                LatLngBounds bounds = map.getBounds();
                
                onBoundsChanged(new Bounds(
                        new GDC(map.getCenter().getLatitude(), map.getCenter().getLongitude(), 0),
                        new GDC(bounds.getNorthEast().getLatitude(), bounds.getSouthWest().getLongitude(), 0),
                        new GDC(bounds.getNorthEast().getLatitude(), bounds.getNorthEast().getLongitude(), 0),
                        new GDC(bounds.getSouthWest().getLatitude(), bounds.getNorthEast().getLongitude(), 0),
                        new GDC(bounds.getSouthWest().getLatitude(), bounds.getSouthWest().getLongitude(), 0),
                        map.getZoom()
                ));
            }
        });
        
        //handle when the map is clicked
        map.addClickHandler(new ClickMapHandler() {
            
            @Override
            public void onEvent(ClickMapEvent event) {
                
                if(event.getMouseEvent() != null) {
                    onMapClicked(latLngToCoordinate(event.getMouseEvent().getLatLng()));
                }
            }
        });
        
        notifyMapReady();
        return drawingManager;
    }

    /**
     * Assigns the given marker options with the default marker icon
     *
     * @param markerOptions the options to give an icon
     * @param color the color to give the icon
     */
    protected native void setDefaultMarkerIcon(MarkerOptions markerOptions, String color)/*-{

		markerOptions.icon = {
			path : 'M 0,0 L -6.928,-12 A 8 8 1 1 1 6.928,-12 L 0,0 z',
			fillColor : "black",
			fillOpacity : 1,
			strokeColor : color,
			strokeWeight : 1,
			scale : 1
		}
    }-*/;


    @Override
    public boolean zoomIn() {

        if(map.getZoom() < 20) {
            map.setZoom(map.getZoom() + 1);
        }

        return map.getZoom() < 20;
    }

    @Override
    public boolean zoomOut() {

        if(map.getZoom() > 0) {
            map.setZoom(map.getZoom() - 1);
        }

        return map.getZoom() > 0;
    }

    /**
     * Gets the map rendered by Google Maps
     *
     * @return the map
     */
    MapWidget getMap() {
        return map;
    }

    /**
     * Converts the given abstract map coordinate into a Google Maps LatLng coordinate
     *
     * @param coordinate the abstract map coordinate to convert
     * @return the converted LatLng coordinate
     */
    static LatLng coordinateToLatLng(AbstractMapCoordinate coordinate) {
        if(coordinate instanceof GDC) {

            GDC gdc = (GDC) coordinate;
            return LatLng.newInstance(gdc.getLatitude(), gdc.getLongitude());

        } else {
            //TODO: Figure out if we can show GCC points
            return LatLng.newInstance(0, 0);
        }
    }

    /**
     * Converts the given Google Maps LatLng coordinate into an abstract map coordinate
     *
     * @param latLng the LatLng coordinate to convert
     * @return the converted abstract map coordinate
     */
    static AbstractMapCoordinate latLngToCoordinate(LatLng latLng) {
        return new GDC(latLng.getLatitude(), latLng.getLongitude(), 0);
    }

    @Override
    public PointShape<? extends GoogleMapPanel> createPoint(AbstractMapCoordinate coordinate) {
        return new GoogleMapsPointShape(GoogleMapPanel.this, coordinate);
    }

    @Override
    public PolylineShape<? extends GoogleMapPanel> createPolyline(List<AbstractMapCoordinate> vertices) {
        return new GoogleMapsPolylineShape(GoogleMapPanel.this, vertices);
    }

    @Override
    public PolygonShape<? extends GoogleMapPanel> createPolygon(List<AbstractMapCoordinate> vertices) {
        return new GoogleMapsPolygonShape(GoogleMapPanel.this, vertices);
    }

    @Override
    public void setControlMode(MapControlModeEnum mode) {

        if(mode.equals(MapControlModeEnum.PAN)){
            drawingManager.setMap(null);

        } else if(mode.equals(MapControlModeEnum.DRAW_POINT)) {
            drawingManager.setMap(map);
            drawingManager.setDrawingMode(OverlayType.MARKER);

        } else if(mode.equals(MapControlModeEnum.DRAW_POLYLINE)) {
            drawingManager.setMap(map);
            drawingManager.setDrawingMode(OverlayType.POLYLINE);

        } else if(mode.equals(MapControlModeEnum.DRAW_POLYGON)) {
            drawingManager.setMap(map);
            drawingManager.setDrawingMode(OverlayType.POLYGON);
        }
    }

    /**
     * Converts the given array of LatLng objects to a list of coordinates
     *
     * @param latLngs the array of LatLng objects to convert
     * @return the equivalent list of coordinates
     */
    static List<AbstractMapCoordinate> toCoordinateList(MVCArray<LatLng> latLngs) {

        List<AbstractMapCoordinate> coordinates = new ArrayList<>();

        for(int i = 0; i < latLngs.getLength(); i++) {
            coordinates.add(GoogleMapPanel.latLngToCoordinate(latLngs.get(i)));
        }

        return coordinates;
    }

    /**
     * Converts the given list of coordinates to an array of LatLng objects
     *
     * @param coordinates the list of coordinates to convert
     * @return the equivalent array of LatLng objects
     */
    static MVCArray<LatLng> toLatLngArray(List<? extends AbstractMapCoordinate> coordinates) {

        MVCArray<LatLng> latLngs = MVCArray.newInstance();

        for (AbstractMapCoordinate coordinate : coordinates) {
            LatLng latLng = GoogleMapPanel.coordinateToLatLng(coordinate);
            latLngs.push(latLng);
        }

        return latLngs;
    }

    @Override
    public void centerView(boolean zoomToFit, AbstractMapShape<?>... shapes) {

        if(shapes == null || shapes.length < 1) {
            throw new IllegalArgumentException("There must be at least one shape to center the view around");
        }

        LatLngBounds bounds = LatLngBounds.newInstance(null, null);

        for(AbstractMapShape<?> shape : shapes) {

            if(shape instanceof PointShape) {

                PointShape<?> pointShape = (PointShape<?>) shape;

                bounds.extend(convertCoordinateToLatLng(pointShape.getLocation()));

            } else if(shape instanceof PolylineShape) {

                PolylineShape<?> polylineShape = (PolylineShape<?>) shape;

                for(AbstractMapCoordinate coordinate : polylineShape.getVertices()) {
                    bounds.extend(convertCoordinateToLatLng(coordinate));
                }

            } else if(shape instanceof PolygonShape) {

                PolygonShape<?> polygonShape = (PolygonShape<?>) shape;

                for(AbstractMapCoordinate coordinate : polygonShape.getVertices()) {
                    bounds.extend(convertCoordinateToLatLng(coordinate));
                }
            }
        }

        if(zoomToFit) {

            //fit the map to the given bounds to both center it and zoom as close as possible
            map.fitBounds(bounds);

        } else {

            //pan the map to the center of the bounds
            map.panTo(bounds.getCenter());
        }
    }

    /**
     * Converts an {@link AbstractMapCoordinate} to a {@link LatLng} object.
     *
     * @param coordinate The {@link AbstractMapCoordinate} to convert.
     * @return The {@link LatLng} object that is created by the conversion.
     */
    protected LatLng convertCoordinateToLatLng(AbstractMapCoordinate coordinate) {
        return coordinateToLatLng(coordinate);
    }

    /**
     * Handles when Google Maps's authentication service determines that the provided API key is invalid by displaying a warning
     * message to the user indicating the problem and how to fix it.
     */
    private void onAuthenticationFailed() {

        StringBuilder warningMessage = new StringBuilder()
                .append("The Google Maps API key that was provided for this instance of GIFT is not valid. ")
                .append("As a result, certain features of this interface that utilize Google Maps may not work properly.<br/><br/>");

        if(mapProperties.isServerMode()) {
            warningMessage.append("Please try again to see if the problem persists.")
                .append("<br/><br/>If it happens again there are a few things you can check")
                .append("to help determine the cause:<br/>")
                .append("<ol><li>Check the browser developer console for more error details. The failure status code ")
                .append("that was returned from Google's authentication service will likely be reported there.")
                .append("<li>If you have access to the GIFT configuration files,")
                .append("make sure your TutorURL property is set correctly in GIFT/config/common.properties</li>")
                .append("<li>If you do not have access to the GIFT configuration files,")
                .append("contact your server administrator and provide them with the contents of this message");

        } else {
            warningMessage.append("Make sure your GoogleMapsApiKey property is set to a valid ")
                .append("Google Maps API key in GIFT/config/common.properties");
        }

        if(mapProperties.getAuthHandler() != null) {
            mapProperties.getAuthHandler().onAuthenticationFailure("Invalid Google Maps API key", warningMessage.toString());
        }
    }

    /**
     * Initializes native functions that are needed by these widget. This method is primarily used to define global
     * functions on the browser window that are needed to handle some of Google Maps' behavior.
     */
    private native void initNativeFunctions()/*-{

		var that = this;

		//detect when Google Maps fails to authenticate the API key
		$wnd.gm_authFailure = $entry(function() {
			that.@mil.arl.gift.tools.map.client.google.GoogleMapPanel::onAuthenticationFailed()()
		});
    }-*/;

    /**
     * A handler that can execute logic depending on the result of authenticating an instance of this widget
     * with Google Maps' services
     *
     * @author nroberts
     */
    public static interface AuthenticationHandler{

        /**
         * Handles when a {@link GoogleMapPanel} fails to authenticate with Google Maps' services
         *
         * @param errorTitle the title of the failure error message
         * @param errorBody the body of the failure error message
         */
        public void onAuthenticationFailure(String errorTitle, String errorBody);
    }

    /**
     * A set of properties defining a map's behavior, including its loading parameters
     * 
     * @author nroberts
     */
    public static class Properties {
        
        /** 
         * The API key that will be used to load the Google Maps JavaScript API. If invalid or
         * not provided, some features of Google Maps may not function properly.
         */
        private String apiKey;
        
        /** Whether this client should be treated as if it is in server mode */
        private boolean isServerMode = true;

        /** A handler that can be used to detect when the map fails to authenticate its API key */
        private AuthenticationHandler authHandler;
        
        /** A command to be invoked when this panel's map is ready to handle operations */
        private Command onMapReadyCommand;
        
        /** Whether the user should be able to change the type of imagery shown by the map */
        private boolean isMapTypeControllable = true;
        
        /** Whether the user should be able to change the map's bounds (i.e. pan, zoom, etc.) */
        private boolean areBoundsControllable = true;
        
        /**
         * Creates a new set of map properties with the given API key and server mode status.
         * <br/><br/>
         * Note that a valid Google Maps API key must be provided in order to allow all of Google Maps' features to be used.
         * 
         * @param apiKey the API key needed to use features that rely on Google's hosted map services
         * @param isServerMode whether this client should be treated as if it is in server mode. Affects the wording of
         * authentication errors that are reported API key is missing or fails authentication.
         */
        public Properties(String apiKey, boolean isServerMode) {
            this(apiKey, isServerMode, null);
}
        
        /**
         * Creates a new set of map properties with the given API key, server mode status, and authentication handler.
         * <br/><br/>
         * Note that a valid Google Maps API key must be provided in order to allow all of Google Maps' features to be used.
         * 
         * @param apiKey the API key needed to use features that rely on Google's hosted map services
         * @param isServerMode whether this client should be treated as if it is in server mode. Affects the wording of
         * authentication errors that are reported API key is missing or fails authentication.
         * @param authHandler a optional handler that can be used to handle when the map fails to authenticate the
         * provided API key. Can be null.
         */
        public Properties(String apiKey, boolean isServerMode, AuthenticationHandler authHandler) {
            this(apiKey, isServerMode, null, authHandler);
        }
        
        /**
         * Creates a new set of map properties with the given API key, server mode status, ready command, and 
         * authentication handler.
         * <br/><br/>
         * Note that a valid Google Maps API key must be provided in order to allow all of Google Maps' features to be used.
         * 
         * @param apiKey the API key needed to use features that rely on Google's hosted map services
         * @param isServerMode whether this client should be treated as if it is in server mode. Affects the wording of
         * authentication errors that are reported API key is missing or fails authentication.
         * @param onMapReady a command to be invoked when this panel's map is ready to handle operations. Can be null.
         * @param authHandler a optional handler that can be used to handle when the map fails to authenticate the
         * provided API key. Can be null.
         */
        public Properties(String apiKey, boolean isServerMode, Command onMapReady, AuthenticationHandler authHandler) {
            setApiKey(apiKey);
            setServerMode(isServerMode);
            setOnMapReadyCommand(onMapReady);
            setAuthHandler(authHandler);
        }

        /**
         * Gets the API key that will be used to load the Google Maps JavaScript API. If invalid or
         * not provided, some features of Google Maps may not function properly.
         * 
         * @return the API key. Can be null.
         */
        public String getApiKey() {
            return apiKey;
        }

        /**
         * Sets the API key that will be used to load the Google Maps JavaScript API. If invalid or
         * not provided, some features of Google Maps may not function properly.
         * 
         * @param apiKey the API key. Can be null.
         */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Get whether this client should be treated as if it is in server mode. Affects the wording of
         * authentication errors that are reported API key is missing or fails authentication.
         * 
         * @return whether this client is in server mode
         */
        public boolean isServerMode() {
            return isServerMode;
        }

        /**
         * Sets whether this client should be treated as if it is in server mode. Affects the wording of
         * authentication errors that are reported API key is missing or fails authentication.
         * 
         * @param isServerMode whether this client is in server mode. True by default.
         */
        public void setServerMode(boolean isServerMode) {
            this.isServerMode = isServerMode;
        }

        /**
         * Gets the handler used to detect when the map fails to authenticate its API key
         * 
         * @return the authentication handler. Can be null.
         */
        public AuthenticationHandler getAuthHandler() {
            return authHandler;
        }

        /**
         * Sets the handler used to detect when the map fails to authenticate its API key
         * 
         * @param authHandler the authentication handler. Can be null.
         */
        public void setAuthHandler(AuthenticationHandler authHandler) {
            this.authHandler = authHandler;
        }

        /**
         * Gets the command to invoke when this panel's map is ready to handle operations
         * 
         * @return the command to invoke. Can be null.
         */
        public Command getOnMapReadyCommand() {
            return onMapReadyCommand;
        }

        /**
         * Sets the command to invoke when this panel's map is ready to handle operations
         * 
         * @param onMapReadyCommand the command to invoke. Can be null.
         */
        public void setOnMapReadyCommand(Command onMapReadyCommand) {
            this.onMapReadyCommand = onMapReadyCommand;
        }

        /**
         * Gets whether the user should be able to change the map's bounds (i.e. pan, zoom, etc.)
         * 
         * @return whether the user can change the map's bounds
         */
        public boolean areBoundsControllable() {
            return areBoundsControllable;
        }

        /**
         * Sets whether the user should be able to change the map's bounds (i.e. pan, zoom, etc.)
         * 
         * @param areBoundsControllable whether the user can change the map's bounds. True by default.
         */
        public void setBoundsControllable(boolean areBoundsControllable) {
            this.areBoundsControllable = areBoundsControllable;
        }

        /**
         * Gets whether the user should be able to change the type of imagery shown by the map
         * 
         * @return whether the user can change the map type
         */
        public boolean isMapTypeControllable() {
            return isMapTypeControllable;
        }

        /**
         * Sets whether the user should be able to change the type of imagery shown by the map
         * 
         * @param isMapTypeControllable whether the user can change the map type. True by default.
         */
        public void setMapTypeControllable(boolean isMapTypeControllable) {
            this.isMapTypeControllable = isMapTypeControllable;
        }
    }

    @Override
    public void panTo(AbstractMapCoordinate center, Integer zoomLevel) {
        
        if(center == null) {
            return;
        }
        
        //pan the map to the given center point
        map.panTo(coordinateToLatLng(center));
        
        if(zoomLevel != null) {
            
            //zoom the map to the appropriate level
            map.setZoom(zoomLevel);
        }
    }

    @Override
    public AbstractMapCoordinate getCentroid(AbstractMapCoordinate... locations) {
        
        if(locations == null || locations.length < 1) {
            throw new IllegalArgumentException("There must be at least one location to determine a centroid for");
        }
         
        LatLngBounds bounds = LatLngBounds.newInstance(null, null);
        
        for(AbstractMapCoordinate location : locations) {
            bounds.extend(coordinateToLatLng(location));
        }
        
        return latLngToCoordinate(bounds.getCenter());
    }

}
