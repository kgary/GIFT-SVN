/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.model.wrapper;

import generated.dkf.GCC;

import java.math.BigDecimal;

/**
 * The Class GccTypeWrapper.
 */
public class GccTypeWrapper extends CoordinateTypeWrapper<GCC> {
	
	/**
	 * Instantiates a new gcc type wrapper.
	 */
	public GccTypeWrapper() {
		this(new GCC());
	}	
	
	/**
	 * Instantiates a new gcc type wrapper.
	 *
	 * @param gcc the gcc
	 */
	public GccTypeWrapper(GCC gcc) {
		super(gcc);
		if(gcc.getX() == null || gcc.getY() == null || gcc.getZ() == null) {
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
		type.setZ(BigDecimal.valueOf(z));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getZ()
	 */
	@Override
	public double getZ() {
		return type.getZ().doubleValue();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.model.CoordinateTypeWrapper#getTypeString()
	 */
	@Override
	public String getTypeString() {
		return "GCC";
	}
}
