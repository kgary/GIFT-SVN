/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.model.wrapper;

import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.AGL;

import java.io.Serializable;

/**
 * The Class CoordinateTypeWrapper.
 *
 * @param <T> the generic type
 */
public abstract class CoordinateTypeWrapper<T extends Serializable> {
	
	/**
	 * Wrap.
	 *
	 * @param object the object
	 * @return the coordinate type wrapper
	 */
	public static CoordinateTypeWrapper<?> wrap(Serializable object) {
		
		CoordinateTypeWrapper<?> wrapper = null;
		
		if(object == null) {
			wrapper = new GccTypeWrapper();
		}
		if(object instanceof GCC) {
			wrapper = new GccTypeWrapper((GCC)object);
			
		} else if (object instanceof GDC) {
			wrapper = new GdcTypeWrapper((GDC)object);
			
		} else if (object instanceof AGL) {
			wrapper = new AglTypeWrapper((AGL)object);	
		}
		
		return wrapper;
	}
	
	/**
	 * Wrap copy.
	 *
	 * @param object the object
	 * @return the coordinate type wrapper
	 */
	public static CoordinateTypeWrapper<?> wrapCopy(Serializable object) {
		
		CoordinateTypeWrapper<?> wrapper = null;
		CoordinateTypeWrapper<?> copy = null;

		if(object instanceof GCC) {
			wrapper = new GccTypeWrapper((GCC)object);
			copy = new GccTypeWrapper();

		} else if (object instanceof GDC) {
			wrapper = new GdcTypeWrapper((GDC)object);
			copy = new GdcTypeWrapper();

		} else if (object instanceof AGL) {
			wrapper = new AglTypeWrapper((AGL)object);
			copy = new AglTypeWrapper();

		} else {
			//Wrapping not technically necessary, but done to keep code consistent
			wrapper = new GccTypeWrapper(new GCC());
			copy = new GccTypeWrapper();
		}

		copy.copy(wrapper);		
		
		return copy;
	}
	
	/** The type. */
	protected T type;
	
	/**
	 * Instantiates a new coordinate type wrapper.
	 *
	 * @param type the type
	 */
	protected CoordinateTypeWrapper(T type) {
		this.type = type;
	}
	
	/**
	 * Sets the.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	protected void set(double x, double y, double z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	
	/**
	 * Sets the x.
	 *
	 * @param x the new x
	 */
	public abstract void setX(double x);
	
	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public abstract double getX();
	
	/**
	 * Sets the y.
	 *
	 * @param y the new y
	 */
	public abstract void setY(double y);
	
	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public abstract double getY();
	
	/**
	 * Sets the z.
	 *
	 * @param z the new z
	 */
	public abstract void setZ(double z);
	
	/**
	 * Gets the z.
	 *
	 * @return the z
	 */
	public abstract double getZ();
	
	/**
	 * Copy.
	 *
	 * @param other the other
	 */
	public void copy(CoordinateTypeWrapper<?> other) {
		
		this.setX(other.getX());
		this.setY(other.getY());
		this.setZ(other.getZ());		
	}	
	
	/**
	 * Gets the type string.
	 *
	 * @return the type string
	 */
	public abstract String getTypeString();
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public T getValue() { 
		return type;
	}
}
