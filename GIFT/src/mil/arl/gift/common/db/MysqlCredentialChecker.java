/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks credentials for a mysql database.
 * @author cdettmering
 *
 */
public class MysqlCredentialChecker implements DatabaseCredentialChecker {
	
	private static Logger logger = LoggerFactory.getLogger(MysqlCredentialChecker.class);

	@Override
	public boolean checkCredentials(String url, String user, String password, String driverClass) {
		boolean success = false;
		
		try {
            Class<?> driverClazz = Class.forName(driverClass);
            if(Driver.class.isAssignableFrom(driverClazz)){
    			Driver driver = (Driver) driverClazz.getDeclaredConstructor().newInstance();
    			DriverManager.registerDriver(driver);
    			Properties connProps = new Properties();
    			connProps.put("user", user);
    			connProps.put("password", password);
    			DriverManager.getConnection(url, connProps);
            }else{
                throw new IllegalArgumentException("The driver class named "+driverClass+" is not a "+Driver.class.getCanonicalName()+" class implementation.");
            }
            
            success = true;
			
		} catch (SQLException ex) {
			logger.error("Could not connect to database with given credentials.", ex);
		} catch (ClassNotFoundException cnf) {
            logger.error("Could not connect to database with given credentials because the driver class of "+driverClass+" could not be found.", cnf);
        } catch (InstantiationException instantiationException) {
            logger.error("Could not connect to database with given credentials because the driver class of "+driverClass+" could not instatiated.", instantiationException);
        } catch (IllegalAccessException illegalAccess) {
            logger.error("Could not connect to database with given credentials because the driver class of "+driverClass+" could not be accessed.", illegalAccess);
        } catch (ReflectiveOperationException refOp) {
            logger.error("Could not connect to database with given credentials because the driver class of "+driverClass+"'s constructor could not be accessed.", refOp);
        }
		
		return success;
	}
}
