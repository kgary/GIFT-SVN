/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.model.dkf;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * The Class PerfNodeIdNamePair.
 */
public class PerfNodeIdNamePair implements Serializable {

	/** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
	private String name;
	
	/** The id. */
	private BigInteger id;
		
	/**
	 * No-Arg constructor.
	 */
	public PerfNodeIdNamePair() {
		super();
	}	

	/**
	 * Instantiates a new perf node id name pair.
	 *
	 * @param name the name
	 * @param id the id
	 */
	public PerfNodeIdNamePair(String name, BigInteger id) {
		super();
		this.name = name;
		this.id = id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public BigInteger getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 */
	public void setId(BigInteger id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
}
