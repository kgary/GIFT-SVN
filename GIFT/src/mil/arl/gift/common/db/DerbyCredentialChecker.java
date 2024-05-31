/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.net.ConnectException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.Properties;

import org.apache.derby.client.am.DisconnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the credentials for a Derby embedded database.
 * @author cdettmering
 *
 */
public class DerbyCredentialChecker implements DatabaseCredentialChecker{
	
	private static Logger logger = LoggerFactory.getLogger(DerbyCredentialChecker.class);
	
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
         
		} catch (SQLNonTransientConnectionException connectionException) {
		    
		    //custom handling of an exception that is thrown when the derby server is not running
		    if(connectionException.getCause() instanceof Exception && ((Exception)connectionException.getCause()) instanceof DisconnectException &&
		            ((Exception)connectionException.getCause().getCause()) instanceof ConnectException){
		        logger.error("Could not connect to database with given credentials.  Is the derby server at "+url+" running? \n" +
	                    "Try opening the Windows command prompt and running the command of 'netstat -aon | find \"1527\" '.\n" +
	                    "If nothing is returned then derby server is not running and will need to be started manually by running GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat.\n" +
	                    "Take a look at the resulting window for errors.  In most cases the UMS, for example, will not start if the previous instance of the UMS was not closed gracefully by Window's 'User A' causing the file db.lck and folder tmp\n" +
	                    "to be created which prevents another Window's user, 'User B', from being able to start the derby server (you need to delete that file and folder!).  Restart GIFT once those errors are resolved as GIFT will execute that same start script.", connectionException);
		    }else{
		        logger.error("Could not connect to database with given credentials.", connectionException);
		    }
		    
        } catch (SQLException ex) {
            logger.error("Could not connect to database with given credentials.  Is the derby server at "+url+" running? \n" +
                    "Try opening the Windows command prompt and running the command of 'netstat -aon | find \"1527\" '.\n" +
                    "If nothing is returned then derby server is not running and will need to be started manually by running GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat.\n" +
                    "Take a look at the resulting window for errors.  In most cases the UMS, for example, will not start if the previous instance of the UMS was not closed gracefully by Window's 'User A' causing the file db.lck and folder tmp\n" +
                    "to be created which prevents another Window's user, 'User B', from being able to start the derby server (you need to delete that file and folder!).  Restart GIFT once those errors are resolved as GIFT will execute that same start script.", ex);
            
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
