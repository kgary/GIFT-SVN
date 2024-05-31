/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.model.wrapper;

import generated.dkf.GDC;

import java.math.BigDecimal;

/**
 * The Class GdcTypeWrapper.
 */
public class GdcTypeWrapper extends CoordinateTypeWrapper<GDC> {
	
	/**
	 * Instantiates a new gdc type wrapper.
	 */
	public GdcTypeWrapper() {
		this(new GDC());
	}
	
	/**
	 * Instantiates a new gdc type wrapper.
	 *
	 * @param gdc the gdc
	 */
	public GdcTypeWrapper(GDC gdc) {
		super(gdc);
		if(gdc.getLatitude() == null || gdc.getLongitude() == null || gdc.getElevation() == null) {
			set(0,0,0);
		}
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#setX(double)
	 */
	@Override
	public void setX(double x) {
		type.setLatitude(BigDecimal.valueOf(x));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getX()
	 */
	@Override
	public double getX() {
		return type.getLatitude().doubleValue();
	}	
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#setY(double)
	 */
	@Override
	public void setY(double y) {
		type.setLongitude(BigDecimal.valueOf(y));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getY()
	 */
	@Override
	public double getY() {
		return type.getLongitude().doubleValue();
	}	

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#setZ(double)
	 */
	@Override
	public void setZ(double z) {
		type.setElevation(BigDecimal.valueOf(z));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getZ()
	 */
	@Override
	public double getZ() {
		return type.getElevation().doubleValue();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getTypeString()
	 */
	@Override
	public String getTypeString() {
		return "GDC";
	}
}