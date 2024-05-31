/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.model.wrapper;

import generated.dkf.AGL;

import java.math.BigDecimal;

/**
 * The Class AglTypeWrapper.
 */
public class AglTypeWrapper extends CoordinateTypeWrapper<AGL> {
	
	/**
	 * Instantiates a new vbs agl type wrapper.
	 */
	public AglTypeWrapper() {
		this(new AGL());
	}	
	
	/**
	 * Instantiates a new agl type wrapper.
	 *
	 * @param agl the agl
	 */
	public AglTypeWrapper(AGL agl) {
		super(agl);
		if(agl.getX() == null || agl.getY() == null || agl.getElevation() == null) {
			set(0,0,0);
		}
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#setX(double)
	 */
	@Override
	public void setX(double x) {
		type.setX(BigDecimal.valueOf(x));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getX()
	 */
	@Override
	public double getX() {
		return type.getX().doubleValue();
	}	
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#setY(double)
	 */
	@Override
	public void setY(double y) {
		type.setY(BigDecimal.valueOf(y));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getY()
	 */
	@Override
	public double getY() {
		return type.getY().doubleValue();
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
		return "AGL";
	}
}
