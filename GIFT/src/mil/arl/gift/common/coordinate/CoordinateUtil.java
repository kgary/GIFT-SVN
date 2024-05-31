/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.coordinate;

import java.math.BigDecimal;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import edu.nps.moves.disutil.CoordinateConversions;
import edu.nps.moves.disutil.EulerConversions;

/**
 * Utility Singleton class to assist with coordinate conversions and common methods
 * such as distance checks between different coordinate types.
 *
 * This is a singleton so that the Gdc_To_Gcc_Converter class can be initialized by
 * the singleton object.
 *
 * @author nblomberg
 *
 */
public class CoordinateUtil {

    /** center of the earth */
    private static final Point3d CENTER_POINT = new Point3d(0.0, 0.0, 0.0);

    /** The minimum distance from the center of the earth. */
    public static final double MIN_DISTANCE_FROM_CENTER_OF_EARTH = 500.0;

    /** The squared minimum distance from the center of the earth. */
    public static final double MIN_DISTANCE_FROM_CENTER_OF_EARTH_2 = 250000.0;

    /** The greatest magnitude possible for a latitude value. */
    private static final double LATITUDE_RANGE = 90.0;

    /** The greatest longitude possible for a longitude value. */
    private static final double LONGITUDE_RANGE = 180.0;

    /** Singleton instance. */
    private static CoordinateUtil instance;    

    /**
     * Constructor - private
     */
    private CoordinateUtil() {

    }

    /**
     * Gets the instance of the singleton
     *
     * @return Instance of the singleton
     */
    public static CoordinateUtil getInstance() {
        if (instance == null) {
            instance = new CoordinateUtil();
        }

        return instance;
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create two new Point3d objects.
     *
     * @param agl1 - the first point to use
     * @param agl2 - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(AGL agl1, AGL agl2){
        Point3d agl1Point = convertToPoint(agl1);
        Point3d agl2Point = convertToPoint(agl2);
        return agl1Point.distance(agl2Point);
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create two new Point3d objects.
     *
     * @param gcc1 - the first point to use
     * @param gcc2 - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(GCC gcc1, GCC gcc2){
        Point3d point1 = convertToPoint(gcc1);
        return point1.distance(convertToPoint(gcc2));
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create two new Point3d objects.
     * 
     * <em>NOTE:</em> both GDC points are first converted to GCC
     *
     * @param gdc1 - the first point to use
     * @param gdc2 - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(GDC gdc1, GDC gdc2){
        Point3d point1 = convertToPoint(gdc1);
        return point1.distance(convertToPoint(gdc2));
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create a new Point3d object.
     *
     * @param pt - the first point to use
     * @param gcc - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(Point3d pt, GCC gcc){
        return pt.distance(convertToPoint(gcc));
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create a new Point3d object.
     * 
     * <em>NOTE:</em> the GDC point is first converted to GCC
     *
     * @param pt - the first point to use
     * @param gdc - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(Point3d pt, GDC gdc){
        return pt.distance(convertToPoint(gdc));
    }

    /**
     * Return the distance between the two points. Note that this 
     * will internally create a new Point3d object.
     *
     * @param pt - the first point to use
     * @param agl - the second point to use
     * @return double - the distance between the two points
     */
    public double distance(Point3d pt, AGL agl){
        Point3d aglPoint = convertToPoint(agl);
        return pt.distance(aglPoint);
    }

    /**
     * Returns the squared distance between the two {@link GDC} coordinates.
     * Note that this will internally create two new Point3d objects.
     *
     * <em>NOTE:</em> Use this instead of {@link #distance(GDC, GDC)} when
     * possible for better performance.
     *
     * @param gdc1 The first {@link GDC} coordinate. Can't be null.
     * @param gdc2 The second {@link GDC} coordinate. Can't be null.
     * @return The <em>squared</em> distance between the two points.
     */
    public double distanceSquared(GDC gdc1, GDC gdc2) {
        Point3d p1 = convertToPoint(gdc1);
        return p1.distanceSquared(convertToPoint(gdc2));
    }

    /**
     * Converts an abstract coordinate into a point.  Note this will
     * create and return new Point3d object.  If speed/memory is
     * of concern, use the convertIntoPoint method which does not
     * create a new point object.
     *
     * @param coord The abstract coordinate to convert
     * @return The converted point
     */
    public Point3d convertToPoint(AbstractCoordinate coord) {
        Point3d point = null;
        if (coord instanceof GCC) {
            GCC gcc = (GCC)coord;
            point = new Point3d(gcc.getX(), gcc.getY(), gcc.getZ());
        } else if (coord instanceof GDC) {
            GDC gdc = (GDC)coord;
            GCC gcc = convertToGCC(gdc);
            point =  convertToPoint(gcc);
        } else if (coord instanceof AGL) {
            AGL agl = (AGL)coord;
            point = new Point3d(agl.getX(), agl.getY(), agl.getZ());
        } else {
            throw new UnsupportedOperationException("Unsupported conversion of coordinate to point.");
        }

        return point;
    }

    /**
     * Converts an abstract coordinate into an existing point.  This method
     * takes an existing coordinate and sets the values of the passed in
     * point to the converted values.  This is more optimized and can be used
     * where speed/memory is a concern.
     *
     * @param coord The abstract coordinate to convert
     * @param outPoint The existing point object that will contain the converted values
     */
    public void convertIntoPoint(AbstractCoordinate coord, Point3d outPoint) {

        if (coord instanceof GCC) {
            GCC gcc = (GCC)coord;
            outPoint.set(gcc.getX(), gcc.getY(), gcc.getZ());
        } else if (coord instanceof GDC) {
            GDC gdc = (GDC)coord;
            GCC gcc = convertToGCC(gdc);
            convertIntoPoint(gcc, outPoint);
        } else if (coord instanceof AGL) {
            AGL agl = (AGL)coord;
            outPoint.set(agl.getX(), agl.getY(), agl.getZ());
        } else {
            throw new UnsupportedOperationException("Unsupported conversion of coordinate into existing point.");
        }
    }

    /**
     * Converts a GCC coordinate encoded as a {@link Point3d} into a {@link GDC}
     * coordinate.
     *
     * @param gcc The {@link Point3d} to convert. Can't be null.
     * @return The resulting {@link GDC}. Can't be null.
     */
    public GDC convertFromGCCToGDC(Point3d gcc) {
        final GDC gdc = new GDC();
        convertFromGCCToGDC(gcc, gdc);
        return gdc;
    }

    /**
     * Converts a GCC coordinate encoded as a {@link Point3d} into a {@link GDC}
     * coordinate.
     *
     * @param gcc The {@link Point3d} to convert. Can't be null.
     * @param gdc The {@link GDC} coordinate to populate with the result of the
     *        conversion. Can't be null.
     */
    public void convertFromGCCToGDC(Point3d gcc, GDC gdc) {
        final double[] latLonElv = CoordinateConversions.xyzToLatLonDegrees(new double[] { gcc.x, gcc.y, gcc.z });
        gdc.set(latLonElv[0], latLonElv[1], latLonElv[2]);
    }

    /**
     * Creates a {@link GCC} object from a {@link Point3d} whose components are
     * already expressed in GCC coordinates.
     *
     * @param gccPoint The {@link Point3d} to convert from. Can't be null.
     * @return The resulting {@link GCC}. Can't be null.
     */
    public GCC convertToGCC(Point3d gccPoint) {
        return new GCC(gccPoint.x, gccPoint.y, gccPoint.z);
    }

    /**
     * Transform an abstract coordinate to GCC.
     *
     * @param coord The {@link AbstractCoordinate} to convert to
     * @return The resulting {@link GCC} coordinate. Can't be null.
     * @throws UnsupportedOperationException if the coordinate type of the
     *         parameter is not convertible to GCC.
     */
    public GCC convertToGCC(AbstractCoordinate coord) {

        if (coord instanceof GCC) {
            return (GCC)coord;
        } else if (coord instanceof GDC) {
            return convertToGCC( (GDC)coord);
        } else if (coord instanceof AGL) {
            throw new UnsupportedOperationException("Unable to convert from AGL to GDC");
        } else {
            throw new UnsupportedOperationException("Unsupported conversion of coordinate to GCC.");
        }
    }

    /**
     * Transform a GDC coordinate to GCC.
     *
     * @param gdc The {@link GDC} client to convert. Can't be null.
     * @return The resulting {@link GCC} coordinate.
     */
    public GCC convertToGCC(GDC gdc) {

        double[] xyz = CoordinateConversions.getXYZfromLatLonDegrees(
                gdc.getLatitude(),
                gdc.getLongitude(),
                gdc.getElevation()
        );

        return new GCC(xyz[0], xyz[1], xyz[2]);
    }
    
    /**
     * Transform a generate.dkf.GDC coordinate to generate.dkf.GCC.
     *
     * @param gdc The generated.dkf.GDC coordinate to convert. Can't be null.
     * @return The resulting generated.dkf.GCC coordinate.
     * @throws IllegalArgumentException in the case that the 'gdc' parameter is null.
     */
    public generated.dkf.GCC convertToDkfGCC(generated.dkf.GDC gdc) {
        
        if (gdc == null) {
            throw new IllegalArgumentException("The parameter 'gdc' cannot be blank.");
        }

        double[] xyz = CoordinateConversions.getXYZfromLatLonDegrees(
                gdc.getLatitude().doubleValue(),
                gdc.getLongitude().doubleValue(),
                gdc.getElevation().doubleValue()
        );

        generated.dkf.GCC gccOutput = new generated.dkf.GCC();
        
        gccOutput.setX(new BigDecimal(xyz[0]));
        gccOutput.setY(new BigDecimal(xyz[1]));
        gccOutput.setZ(new BigDecimal(xyz[2]));
        
        return gccOutput;
    }
    
    public generated.dkf.GDC convertToDkfGDC(generated.dkf.GCC gcc) {
    	
    	if (gcc == null) {
            throw new IllegalArgumentException("The parameter 'gcc' cannot be blank.");
        }
    	
        double[] latLonElv = CoordinateConversions.xyzToLatLonDegrees(new double[] {gcc.getX().doubleValue(), gcc.getY().doubleValue(), gcc.getZ().doubleValue()});
        
        generated.dkf.GDC gdcResult = new generated.dkf.GDC();
        
        gdcResult.setLatitude(new BigDecimal(latLonElv[0]));
        gdcResult.setLongitude(new BigDecimal(latLonElv[1]));
        gdcResult.setElevation(new BigDecimal(latLonElv[2]));
        
        return gdcResult;
    }

    /**
     * Transform this coordinate to GDC.
     *
     * @param gcc The {@link GCC} coordinate to convert. Can't be null.
     * @return The resulting {@link GDC} coordinate. Can't be null.
     */
    public GDC convertToGDC(GCC gcc) {

        double[] latLonElv = CoordinateConversions.xyzToLatLonDegrees(new double[] {gcc.getX(), gcc.getY(), gcc.getZ()});

        return new GDC(latLonElv[0], latLonElv[1], latLonElv[2]);
    }

    /**
     * Determines the heading of an entity based on the entity's orientation and
     * location.
     *
     * @param eulerOrientation The orientation of the entity as an euler angle.
     * @param location The {@link GDC} location of the entity.
     * @return The heading of the entity as degrees from north. A negative value
     *         represents a counter-clockwise rotation from north while a
     *         positive value represents a positive rotation from north (e.g.
     *         -90 is west and 90 is east).
     */
    public double getHeading(Vector3d eulerOrientation, GDC location) {
        final double latitude = Math.toRadians(location.getLatitude());
        final double longitude = Math.toRadians(location.getLongitude());
        return EulerConversions.getOrientationFromEuler(latitude, longitude,
                eulerOrientation.getX(),
                eulerOrientation.getY());
    }

    /**
     * Is the GCC coordinate valid
     *
     * @param gcc The {@link GCC} coordinate to test. Can't be null.
     * @return True if the coordinate is valid, false otherwise.
     */
    public boolean isValid(GCC gcc) {

        Point3d point = convertToPoint(gcc);
        double dist = point.distanceSquared(CENTER_POINT);

        return dist > MIN_DISTANCE_FROM_CENTER_OF_EARTH_2;
    }

    /**
     * Is the GDC coordinate valid
     *
     * @param gdc The {@link GDC} coordinate to test. Can't be null.
     * @return True if the coordinate is valid, false otherwise.
     */
    public boolean isValid(GDC gdc) {

        double latitude = gdc.getLatitude();
        double longitude = gdc.getLongitude();
        double elevation = gdc.getElevation();

        if (Double.isNaN(latitude)) {

            return false;
        }

        if (Double.isNaN(longitude)) {

            return false;
        }

        if (latitude < -LATITUDE_RANGE ||
            latitude > LATITUDE_RANGE) {

            return false;
        }

        if (longitude < -LONGITUDE_RANGE ||
            longitude > LONGITUDE_RANGE) {

            return false;
        }

        if (Double.isNaN(elevation)) {

            return false;
        }

        return true;
    }

}
