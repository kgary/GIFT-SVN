/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

/**
 * Interface that enables different types of database connectors to check 
 * their credentials in their own way.
 * 
 * @author cdettmering
 */
public interface DatabaseCredentialChecker {

	/**
	 * Checks the credentials user and password for the database at url.
	 * @param url Database url to check credentials.
	 * @param user Username to check
	 * @param password Password for user
	 * @param driverClass the driver class name used for the database connection (e.g. com.mysql.jdbc.Driver, org.apache.derby.jdbc.ClientDriver)
	 * @return True if the credentials check out, false otherwise.
	 */
	public boolean checkCredentials(String url, String user, String password, String driverClass);
}
