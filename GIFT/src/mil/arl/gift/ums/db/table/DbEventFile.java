/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="eventfile")
public class DbEventFile extends DbDataFile {

	/**
	 * Default constructor - required by Hibernate
	 */
	public DbEventFile(){
		super();
	}
	
	/**
	 * Class constructor 
	 * 
	 * @param filename - the event file name
	 */
	public DbEventFile(String filename){
		super(filename);
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[EventFile:");
	    sb.append(super.toString());
	    sb.append("]");
		
		return sb.toString();
	}
}
