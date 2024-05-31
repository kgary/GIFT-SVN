/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map;

import static mil.arl.gift.common.util.StringUtils.isBlank;
import static mil.arl.gift.common.util.StringUtils.join;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility.buildMapImageUrl;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility.getAuthoringSupportElements;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToAGL;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToLatLng;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.normalizeTileCoordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.base.Size;
import com.google.gwt.maps.client.controls.MapTypeControlOptions;
import com.google.gwt.maps.client.drawinglib.DrawingManager;
import com.google.gwt.maps.client.drawinglib.DrawingManagerOptions;
import com.google.gwt.maps.client.events.overlaycomplete.marker.MarkerCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.marker.MarkerCompleteMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.polyline.PolylineCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.polyline.PolylineCompleteMapHandler;
import com.google.gwt.maps.client.maptypes.ImageMapType;
import com.google.gwt.maps.client.maptypes.ImageMapTypeOptions;
import com.google.gwt.maps.client.maptypes.TileUrlCallBack;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.Polygon;
import com.google.gwt.maps.client.overlays.PolygonOptions;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;

import generated.course.AuthoringSupportElements;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.io.MapTileProperties.MapTileCoordinate;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityMapPointShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityMapPolygonShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityMapPolylineShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.map.client.draw.AbstractMapShape;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolygonShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.client.google.GoogleMapPanel;
import mil.arl.gift.tools.map.shared.AGL;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A panel that renders a map of a Unity scenario
 *
 * @author nroberts
 */
public class UnityMapPanel extends GoogleMapPanel {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(UnityMapPanel.class.getName());

    /** The RPC service used to fetch information about the map data */
    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

    /** The minimum zoom that the map can be set to */
    public static final int MIN_ZOOM = 20;

    /** The coordinate location of the top left-most pixel */
    private AGL topLeft;

    /** The coordinate location of the bottom right-most pixel */
    private AGL bottomRight;

    /** A mapping of zoom levels to the tiles for that zoom level */
    private Map<Integer, List<MapTileProperties>> zoomToTiles = new HashMap<>();

    /**
     * Creates a new panel containing a rendered map of a Unity scenario
     *
     * @param mapProperties the properties defining the behavior of the map,
     *        including its loading parameters. Cannot be null.
     */
    public UnityMapPanel(Properties mapProperties) {
        super(mapProperties);
    }

    @Override
    protected void initializeMap() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initializeMap()");
        }

        BsLoadingDialogBox.display("Loading Map", "The map data is currently being retrieved");

        AuthoringSupportElements authoringSupport = getAuthoringSupportElements();
        if (authoringSupport == null) {
            onFailedToLoadMap(
                    "No AuthoringSupportElements are available for this scenario so no map data could be loaded.");
            return;
        }

        final String propertyPath = authoringSupport.getTrainingAppsScenarioMapPath();
        if (isBlank(propertyPath)) {
            onFailedToLoadMap(
                    "No TrainingAppsScenarioMapPath was provided for this scenario so no map data could be loaded.");
            return;
        }

        final String userName = GatClientUtility.getUserName();
        rpcService.getScenarioMapTileProperties(propertyPath, userName,
                new AsyncCallback<GenericRpcResponse<List<MapTileProperties>>>() {

                    @Override
                    public void onSuccess(GenericRpcResponse<List<MapTileProperties>> result) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("rpcService.getScenarioMapTileProperties.onSuccess(" + result + ")");
                        }

                        if (!result.getWasSuccessful()) {
                            DetailedExceptionSerializedWrapper exception = result.getException();
                            StringBuilder reasonBuilder = new StringBuilder();

                            /* Create the explanation for what went wrong */
                            reasonBuilder.append("message: ").append(exception.getMessage()).append('\n');
                            reasonBuilder.append("reason: ").append(exception.getReason()).append('\n');
                            reasonBuilder.append("details: ").append(exception.getDetails());
                            reasonBuilder.append("stack trace: ").append('\n');
                            for (String stackLine : exception.getErrorStackTrace()) {
                                reasonBuilder.append('\t').append(stackLine).append('\n');
                            }

                            onFailedToLoadMap(reasonBuilder.toString());
                            return;
                        }

                        if (result.getContent().isEmpty()) {
                            onFailedToLoadMap(
                                    "No image metadata was returned by the server. Make sure that *.maptile.properties files exist in the directory "
                                            + propertyPath);
                            return;
                        }

                        /* Index all the MapTileProperties to facilitate the
                         * tile lookups */
                        for (MapTileProperties props : result.getContent()) {
                            final int zoomLevel = props.getZoomLevel();
                            if (!zoomToTiles.containsKey(zoomLevel)) {
                                zoomToTiles.put(zoomLevel, new ArrayList<MapTileProperties>());
                            }

                            zoomToTiles.get(zoomLevel).add(props);
                        }

                        if (!zoomToTiles.containsKey(0)) {
                            onFailedToLoadMap("There was no tile metadata specified for zoom level 0");
                        }

                        MapTileProperties tileProperties = zoomToTiles.get(0).get(0);

                        /* Save the values of the map corners' coordinates */
                        MapTileCoordinate ll = tileProperties.getLowerLeftAGL();
                        MapTileCoordinate ur = tileProperties.getUpperRightAGL();
                        setTopLeft(new AGL(ll.getX().doubleValue(), ur.getY().doubleValue(), 0));
                        setBottomRight(new AGL(ur.getX().doubleValue(), ll.getY().doubleValue(), 0));

                        UnityMapPanel.super.initializeMap();
                        BsLoadingDialogBox.remove();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        onFailedToLoadMap("There was an issue fetching the list of tile properties: " + caught);
                    }
                });
    }

    @Override
    protected DrawingManager createDrawingManager() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createDrawingManager()");
        }

        final UnityMapPanel map = this;
        final DrawingManagerOptions options = DrawingManagerOptions.newInstance();
        options.setDrawingControl(false);

        MarkerOptions markerOptions = MarkerOptions.newInstance();
        setDefaultMarkerIcon(markerOptions, AbstractMapShape.DEFAULT_SHAPE_COLOR);
        options.setMarkerOptions(markerOptions);

        PolylineOptions polylineOptions = PolylineOptions.newInstance();
        polylineOptions.setStrokeWeight(4);
        polylineOptions.setStrokeColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        options.setPolylineOptions(polylineOptions);

        PolygonOptions polygonOptions = PolygonOptions.newInstance();
        polygonOptions.setStrokeWeight(4);
        polygonOptions.setStrokeColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        polygonOptions.setFillColor(AbstractMapShape.DEFAULT_SHAPE_COLOR);
        options.setPolygonOptions(polygonOptions);

        DrawingManager manager = DrawingManager.newInstance(options);
        manager.addMarkerCompleteHandler(new MarkerCompleteMapHandler() {

            @Override
            public void onEvent(MarkerCompleteMapEvent event) {
                Marker marker = event.getMarker();
                UnityMapPointShape point = new UnityMapPointShape(map, marker);
                onManualDrawingComplete(point);
            }
        });

        manager.addPolylineCompleteHandler(new PolylineCompleteMapHandler() {

            @Override
            public void onEvent(PolylineCompleteMapEvent event) {
                Polyline polyline = event.getPolyline();
                UnityMapPolylineShape line = new UnityMapPolylineShape(map, polyline);
                onManualDrawingComplete(line);
            }
        });

        manager.addPolygonCompleteHandler(new PolygonCompleteMapHandler() {

            @Override
            public void onEvent(PolygonCompleteMapEvent event) {
                Polygon polygon = event.getPolygon();
                UnityMapPolygonShape area = new UnityMapPolygonShape(map, polygon);
                onManualDrawingComplete(area);
            }
        });

        return manager;
    }

    /**
     * Performs all the configuration that is necessary for creating the
     * {@link MapWidget} to display to the user.
     *
     * @return The constructed {@link MapWidget}. Can't be null.
     */
    @Override
    protected MapWidget createMapWidget() {
        /* Assert that a 0 zoom image exists */
        if (!zoomToTiles.containsKey(0) || zoomToTiles.get(0).isEmpty()) {
            throw new IllegalStateException("A map tile with a zoom level of 0 does not exist");
        }

        /* Create the options for building the map */
        ImageMapTypeOptions imageMapOptions = createImageMapOptions();

        final String UNITY_MAP_TYPE_ID = "unity";

        /* Create the map widget */
        MapWidget mapWidget = super.createMapWidget();
        ImageMapType imageMapType = ImageMapType.newInstance(imageMapOptions);
        mapWidget.getMapTypeRegistry().set(UNITY_MAP_TYPE_ID, imageMapType);
        mapWidget.setMapTypeId(UNITY_MAP_TYPE_ID);
        mapWidget.setCenter(LatLng.newInstance(0, 0));

        return mapWidget;
    }

    /**
     * Creates the {@link ImageMapTypeOptions} for the map generated by the
     * {@link #createMapWidget()} method.
     *
     * @return The {@link ImageMapTypeOptions} for the {@link MapWidget}. Can't
     *         be null.
     */
    private ImageMapTypeOptions createImageMapOptions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createImageMapOptions()");
        }

        ImageMapTypeOptions imageMapOptions = ImageMapTypeOptions.newInstance();
        imageMapOptions.setMinZoom(MIN_ZOOM + 1);
        imageMapOptions.setMaxZoom(MIN_ZOOM + zoomToTiles.keySet().size() - 1);
        imageMapOptions.setTileSize(Size.newInstance(256, 256));
        imageMapOptions.setTileUrl(new TileUrlCallBack() {

            @Override
            public String getTileUrl(Point point, int zoomLevel) {
                final int normalizedZoom = zoomLevel - MIN_ZOOM;
                if (normalizedZoom == 0) {
                    return null;
                }

                final Point normalizedPoint = normalizeTileCoordinates(point, zoomLevel);
                if (normalizedPoint == null) {
                    return null;
                }

                int x = (int) normalizedPoint.getX();
                int y = (int) normalizedPoint.getY();

                /* Get the collection of tiles corresponding to the most
                 * applicable zoom level. */
                Collection<MapTileProperties> mapTiles;
                if (zoomToTiles.containsKey(normalizedZoom)) {
                    mapTiles = zoomToTiles.get(normalizedZoom);
                } else {
                    return null;
                }

                /* Determine which map tile in the collection of applicable map
                 * tiles, contains the coordinate */
                MapTileProperties mapTile = null;
                for (MapTileProperties tile : mapTiles) {

                    /* There is only one tile at the highest zoom level so
                     * return immediately */
                    if (normalizedZoom == 0) {
                        mapTile = tile;
                        break;
                    }

                    MapTileCoordinate tileCoordinate = tile.getTileCoordinate();
                    boolean xMatches = x == tileCoordinate.getX().doubleValue() - 1;
                    boolean yMatches = y == tileCoordinate.getY().doubleValue() - 1;
                    if (xMatches && yMatches) {
                        mapTile = tile;
                        break;
                    }
                }

                /* If no tile was found, return no URL */
                if (mapTile == null) {
                    return null;
                }

                /* Calculates the map URL with the appropriate map tile, */
                String scenarioMapPath = getAuthoringSupportElements().getTrainingAppsScenarioMapPath();

                return buildMapImageUrl(mapTile, scenarioMapPath);
            }
        });

        return imageMapOptions;
    }

    /**
     * Creates the {@link MapOptions} for the map generated by the
     * {@link #createMapWidget()} method.
     *
     * @return The {@link MapOptions} for the {@link MapWidget}. Can't be null.
     */
    @Override
    protected MapOptions createMapOptions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createMapOptions()");
        }
        
        MapTypeControlOptions mapTypeControlOptions = MapTypeControlOptions.newInstance();
        mapTypeControlOptions.setMapTypeIds(new String[] { "unity" });

        MapOptions mapOptions = MapOptions.newInstance();
        mapOptions.setMapTypeControlOptions(mapTypeControlOptions);
        mapOptions.setMapTypeControl(false);

        /* Set the initial viewport settings for the map */
        mapOptions.setZoom(MIN_ZOOM);

        /* Disable the undesired controls */
        mapOptions.setStreetViewControl(false);
        mapOptions.setZoomControl(false);
        mapOptions.setDisableDefaultUi(true);

        return mapOptions;
    }

    /**
     * Handles the case in which a map image could not be loaded. Logs the
     * reason for the failure and displays the 'image not found' map image.
     *
     * @param reason The reason that the image failed to load. Can't be null.
     */
    private void onFailedToLoadMap(String reason) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onFailedToLoadMap(" + reason + ")");
        }

        if (reason == null) {
            throw new IllegalArgumentException("The parameter 'reason' cannot be null.");
        }

        /* Since the load has ended in failure, no need to show the loading
         * dialog box */
        BsLoadingDialogBox.remove();

        WarningDialog.error("Map Load Error", reason);
    }

    /**
     * Getter for the top-left-most coordinate of this {@link UnityMapPanel}.
     *
     * @return The value of {@link #topLeft}.
     */
    public AGL getTopLeft() {
        return topLeft;
    }

    /**
     * Setter for the top-left-most coordinate of this {@link UnityMapPanel}.
     *
     * @param topLeft The new value of {@link #topLeft}.
     */
    private void setTopLeft(AGL topLeft) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(topLeft);
            logger.fine("setTopLeft(" + join(", ", params) + ")");
        }

        this.topLeft = topLeft;
    }

    /**
     * Getter for the bottom-right-most coordinate of this
     * {@link UnityMapPanel}.
     *
     * @return The value of {@link #bottomRight}.
     */
    public AGL getBottomRight() {
        return bottomRight;
    }

    /**
     * Setter for the bottom-right-most coordinate of this
     * {@link UnityMapPanel}.
     *
     * @param bottomRight The new value of {@link #bottomRight}.
     */
    private void setBottomRight(AGL bottomRight) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(bottomRight);
            logger.fine("setBottomRight(" + join(", ", params) + ")");
        }

        this.bottomRight = bottomRight;
    }

    @Override
    protected LatLng convertCoordinateToLatLng(AbstractMapCoordinate coordinate) {
        return convertFromMapCoordinateToLatLng(this, coordinate);
    }

    @Override
    public PointShape<? extends GoogleMapPanel> createPoint(AbstractMapCoordinate coordinate) {
        AGL agl = convertFromMapCoordinateToAGL(this, coordinate);
        return new UnityMapPointShape(this, agl);
    }

    @Override
    public PolylineShape<? extends GoogleMapPanel> createPolyline(List<AbstractMapCoordinate> vertices) {
        List<AGL> agls = convertFromMapCoordinateToAGL(this, vertices);
        List<AbstractMapCoordinate> aglsAsAbstract = new ArrayList<>();
        aglsAsAbstract.addAll(agls);

        return new UnityMapPolylineShape(this, aglsAsAbstract);
    }

    @Override
    public PolygonShape<? extends GoogleMapPanel> createPolygon(List<AbstractMapCoordinate> vertices) {
        List<AGL> agls = convertFromMapCoordinateToAGL(this, vertices);
        List<AbstractMapCoordinate> aglsAsAbstract = new ArrayList<>();
        aglsAsAbstract.addAll(agls);

        return new UnityMapPolygonShape(this, aglsAsAbstract);
    }

    @Override
    public String toString() {
        return new StringBuilder("[UnityMapPanel: ")
                .append("bottomRight = ").append(bottomRight)
                .append(", topLeft = ").append(topLeft)
                .append(']').toString();
    }

    @Override
    public AbstractMapCoordinate getCentroid(AbstractMapCoordinate... locations) {
        
        if(locations == null || locations.length < 1) {
            throw new IllegalArgumentException("There must be at least one location to determine a centroid for");
        }
        
        return locations[0];
    }
}