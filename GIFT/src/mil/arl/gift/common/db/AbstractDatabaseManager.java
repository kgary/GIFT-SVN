/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.util.List;

/**
 * This class is the base class for database manager implementations.  
 * It contains methods to select, insert, update and delete rows in the database tables.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractDatabaseManager {    
	/**
	 * Insert the provided data as a row in a table in the db schema
	 * 
	 * @param data - row of data to insert into a table
	 * @return boolean - true iff the insert completed without error
	 */
    public abstract boolean insertRow(Object data);

	
	/**
	 * Delete the provided data from a row in a table in the db schema
	 * 
	 * @param data - the data to delete in the db
	 * @return boolean - true iff the delete completed without error
	 */
    public abstract boolean deleteRow(Object data);
    
	/**
	 * Update the row in the table where the object resides
	 * 
	 * @param data - the data to update in the db
	 * @return boolean - true iff the update completed without error
	 */
    public abstract boolean updateRow(Object data);
    
    /**
     * Insert the provided data as a row in a table in the database if it
     * doesn't exist, otherwise update the existing object
     *
     * @param data The object to save or update
     * @return boolean True if the update completed without error
     */
    public abstract boolean saveOrUpdateRow(Object data);
    
	/**
	 * Return the row which has entries that match the example object.  
	 * Using an Example allows you to construct a query criterion from a given instance.
	 * 
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.
	 * 
	 * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @return E - the row found to have the primary key value
	 */
	public abstract <E> E selectRowByExample(Object example, Class<E> rowClass);
		
	/**
	 * Return the rows which has entries that match the example object.  
	 * Using an Example allows you to construct a query criterion from a given instance.
	 * 
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.
	 * 
	 * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @return List<E> - the row found to have the primary key value
	 */
	public abstract <E> List<E> selectRowsByExample(Object example, Class<E> rowClass);
	
	/**
	 * Return the row which has the primary key value specified.
	 * 
	 * @param pkValue - the primary key value of the row to return.
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @return E - the row found to have the primary key value
	 */
	public abstract <E> E selectRowById(Object pkValue, Class<E> rowClass);
	
}
