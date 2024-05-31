/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.coordinate;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import generated.dkf.AGL;
import generated.dkf.GCC;
import generated.dkf.GDC;

/** The different types of Coordinates */
public enum CoordinateType {
    /** Geocentric coordinate - 2 decimals for centimeter precision */
    GCC(IconType.LINE_CHART, "Geocentric coordinate:", NumberFormat.getFormat(".##")),
    /** above ground coordinate - 2 decimals for centimeter precision */
    AGL(IconType.COMPASS, "Above Ground Location:", NumberFormat.getFormat(".##")),
    /** Geodetic coordinate - 8 decimals for millimeter precision */
    GDC(IconType.GLOBE, "Geodetic coordinate:", NumberFormat.getFormat(".########"));

    /** The icon type used to indicate the coordinate type */
    private IconType iconType;

    /** The label for the coordinate type */
    private SafeHtml label;
    
    /** the format to use to display the coordinate numbers */
    private NumberFormat displayFormat;

    /**
     * Constructor.
     * 
     * @param iconType the icon type used to indicate the coordinate type, can't be null.
     * @param label the label of the coordinate to be display to the user, can't be null or empty.
     * @param displayFormat the format to use to display the coordinate numbers.
     */
    private CoordinateType(IconType iconType, String label, NumberFormat displayFormat) {
        
        if(iconType == null){
            throw new IllegalArgumentException("The icon type is null");
        }else if(label == null || label.isEmpty()){
            throw new IllegalArgumentException("The label can't be null or empty");
        }else if(displayFormat == null){
            throw new IllegalArgumentException("The display format is null");
        }
        
        this.iconType = iconType;
        this.label = SafeHtmlUtils.fromTrustedString(label);
        this.displayFormat = displayFormat;
    }

    /**
     * The icon type used to indicate the coordinate type
     * 
     * @return the {@link IconType}
     */
    public IconType getIconType() {
        return iconType;
    }

    /**
     * The label of the coordinate.
     * 
     * @return the label of the coordinate to be display to the user
     */
    public SafeHtml getLabel() {
        return label;
    }

    /**
     * Finds the {@link CoordinateType type enum} using the provided coordinate type.
     * 
     * @param coordinateType the coordinate type to use to identify the enum.
     * @return the {@link CoordinateType}
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public static CoordinateType getCoordinateTypeFromCoordinate(Serializable coordinateType) {
        if (coordinateType == null) {
            throw new IllegalArgumentException("The parameter 'coordinateType' cannot be null.");
        }

        if (coordinateType instanceof GCC) {
            return GCC;
        } else if (coordinateType instanceof AGL) {
            return AGL;
        } else if (coordinateType instanceof GDC) {
            return GDC;
        } else {
            throw new UnsupportedOperationException(
                    "The type '" + coordinateType.getClass().getName() + "' was unexpected");
        }
    }

    /**
     * Creates a new instance of the associated coordinate type
     * 
     * @return the coordinate type instance
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public Serializable createNewCoordindateType() {
        switch (this) {
        case GCC:
            return new GCC();
        case AGL:
            return new AGL();
        case GDC:
            return new GDC();
        default:
            throw new UnsupportedOperationException("The type '" + this + "' was unexpected");
        }
    }

    /**
     * Builds the label for the X coordinate. Can be called by a different name (e.g. longitude).
     * 
     * @param value the text coordinate value.
     * @return the {@link SafeHtml} label for the coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public SafeHtml buildXLabel(Float value) {
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();

        String label;
        switch (this) {
        case GCC: // intentional dropthrough
        case AGL:
            label = "X";
            break;
        case GDC:
            label = "Longitude";
            break;
        default:
            throw new UnsupportedOperationException("The CoordinateType '" + this + "' was not recognized");
        }

        htmlBuilder.append(bold(label)).appendHtmlConstant(": ").appendEscaped(displayFormat.format(value));
        return htmlBuilder.toSafeHtml();
    }

    /**
     * Builds the label for the Y coordinate. Can be called by a different name (e.g. latitude).
     * 
     * @param value the text coordinate value.
     * @return the {@link SafeHtml} label for the coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public SafeHtml buildYLabel(Float value) {
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();

        String label;
        switch (this) {
        case GCC: // intentional dropthrough
        case AGL:
            label = "Y";
            break;
        case GDC:
            label = "Latitude";
            break;
        default:
            throw new UnsupportedOperationException("The CoordinateType '" + this + "' was not recognized");
        }

        htmlBuilder.append(bold(label)).appendHtmlConstant(": ").appendEscaped(displayFormat.format(value));
        return htmlBuilder.toSafeHtml();
    }

    /**
     * Builds the label for the Z coordinate. Can be called by a different name (e.g. elevation).
     * 
     * @param value the text coordinate value.
     * @return the {@link SafeHtml} label for the coordinate value.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    public SafeHtml buildZLabel(Float value) {
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();

        String label;
        switch (this) {
        case GCC:
            label = "Z";
            break;
        case AGL: // intentional dropthrough
        case GDC:
            label = "Elevation";
            break;
        default:
            throw new UnsupportedOperationException("The CoordinateType '" + this + "' was not recognized");
        }

        htmlBuilder.append(bold(label)).appendHtmlConstant(": ").appendEscaped(displayFormat.format(value));
        return htmlBuilder.toSafeHtml();
    }
}
