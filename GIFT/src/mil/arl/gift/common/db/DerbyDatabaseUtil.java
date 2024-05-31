/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.derby.tools.ij;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.ZipUtils;

/**
 * This class provides helpful methods for dealing with derby database backups and restores.
 * 
 * @author mhoffman
 *
 */
public class DerbyDatabaseUtil {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(DerbyDatabaseUtil.class);
    
    private static final String TABLE_BACKUP_FILE_EXT = ".derby.export";
    
    /**
     * Note: based on GIFT db entries on 8/7/2013 unable to use: ; ,  
     */
    private static final String COLUMN_DELIM = "|";
    
    private static final String CHARACTER_DELIM = null;
    
    /**
     * Run the statements found in the file.
     * 
     * @param connection the connection to the Derby database used to execute commands.
     * @param statementsFile contains database statements to execute (e.g. create table...)
     * @throws Exception if there was a problem running the commands
     */
    public static void runStatements(Connection connection, File statementsFile) throws Exception{
              
        try{
            connection.setAutoCommit(false);
            
            byte[] encoded = Files.readAllBytes(Paths.get(statementsFile.getAbsolutePath()));
            String fileContents = new String(encoded);
            
            connection.createStatement().executeUpdate(fileContents);
            connection.commit();
            
            //return back to original value
            connection.setAutoCommit(true);
            
            logger.info("Applied statements from '"+statementsFile+"'.");
            
        }catch(Exception exception){
            
            try{
                logger.error("There was a problem executing the statements.  Attempting to roll back the database.", exception);
                connection.rollback();
                logger.info("Rollback of the database completed.");
            }catch(SQLException sqlException){
                logger.error("There was a problem rolling back after failing to complete the statements.", sqlException);
            }
            
            throw new Exception("Failed to run the statements from '"+statementsFile+"'.");
        }
    }
    
    
    /**
     * Import one or more table backup files located in the specified directory.
     * 
     * @param connection the connection to the Derby database used to execute commands.
     * @param derbyDatabaseBackupDir where table backup files are stored and need to be imported from
     * @param constraintOrderedTableNames an ordering of table names based on key constraints in those tables.
     *                  The ordering should be based on SQL INSERT operations where, for example, a foreign key
     *                  must exist in a table before creating an entry that references that foreign key in a different table. 
     * @param constraintOrderedTablesToClear an ordering of table names based on key constraints in those tables to remove
     *                  all rows from.  This operation supports the idea of restoring a database by making the tables match
     *                  what is in the backup.  Can be null or empty.
     * @param permissionsTableNames a list of all visible and editable to usernames tables.
     * @throws Exception if there was a severe problem importing the table(s)
     */
    public static void importTables(Connection connection, File derbyDatabaseBackupDir, List<String> constraintOrderedTableNames, List<String> constraintOrderedTablesToClear, List<String> permissionsTableNames) throws Exception{
        
        List<FileProxy> fileProxies = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(new DesktopFolderProxy(derbyDatabaseBackupDir), fileProxies, TABLE_BACKUP_FILE_EXT);
        
        //convert to File objects
        List<File> files = new ArrayList<>(fileProxies.size());
        for(FileProxy proxy : fileProxies){
            files.add(new File(proxy.getFileId()));
        }        
        
        logger.info("Attempting to import "+files.size()+" derby tables from backups located in "+derbyDatabaseBackupDir+".");
        
        if(constraintOrderedTableNames != null && !constraintOrderedTableNames.isEmpty()){
            
            //
            // Re-order based on dependencies
            //
            List<File> reorderedFiles = new ArrayList<>(files.size());
            for(int i = constraintOrderedTableNames.size()-1; i >= 0; i--){
                
                String constraintTableName = constraintOrderedTableNames.get(i);
                
                for(File file : files){
                    
                    String tableName = getTableNameFromFile(file);
                    if(tableName.equals(constraintTableName)){
                        //found match, add to beginning of list
                        reorderedFiles.add(0, file);
                        break;
                    }
                }
            }
            
            //now add all other tables that don't have constraints
            for(File file : files){
             
                if(!reorderedFiles.contains(file)){
                    reorderedFiles.add(file);
                }
            }
            
            files = reorderedFiles;       
        }
        
        try{
            connection.setAutoCommit(false);
            
            // used to execute a derby system call to import the table
            // Reference: http://db.apache.org/derby/docs/10.0/manuals/reference/sqlj120.html#HDRIMPORTPROC
            CallableStatement importTableStatement = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)"); 
            
            //
            // Clear table contents before importing new data
            //
            Statement deleteFromTableStatement = connection.createStatement();
            
            //clear permissions tables first to helps prevent foreign key constraint exceptions
            for(String tableName : permissionsTableNames){
                deleteFromTableStatement.addBatch("DELETE FROM "+tableName);
                
            }
            
            //clear tables specified before remaining tables to helps prevent foreign key constraint exceptions
            //Note: the file order is based on INSERT key constraints, DELETE constraints are the opposite
            if(constraintOrderedTablesToClear != null){
                for(int i = constraintOrderedTablesToClear.size()-1; i >=0 ; i--){                
                    deleteFromTableStatement.addBatch("DELETE FROM "+constraintOrderedTablesToClear.get(i));            
                }
            }
            
            //Note: the file order is based on INSERT key constraints, DELETE constraints are the opposite
            for(int i = files.size()-1; i >=0 ; i--){
                
                String tableName = getTableNameFromFile(files.get(i));
                deleteFromTableStatement.addBatch("DELETE FROM "+tableName);            
            }
            
            for(int i = 0; i < files.size(); i++){
                importTable(importTableStatement, deleteFromTableStatement, files.get(i));
            }
                       
            //
            // Development Note: during testing it was discovered that foreign key constraints would cause this type of error -
            // Caused by: org.apache.derby.client.am.SqlException: Error for batch element #0: Import error on line 695 of file 
            // C:\Users\test\AppData\Local\Temp\GIFT_DerbyDb_903025650841517\derbyDb\GiftUms\questioncategory.derby.export: INSERT on table 'QUESTIONCATEGORY' 
            // caused a violation of foreign key constraint 'FK1695B2E4DA5488B7' for key (1).  The statement has been rolled back. 
            // To figure out the correct table ordering I would open the derby db in the third party application Squirrel SQL 3.5.0 and look at the 
            // foreign key references associated with this table.  Depending on the situation it could be that the table reported is trying to reference a foreign key that doesn't exist OR
            // that the operation is removing a value in this table that is a foreign key in another table.
            // Also, the same ordered list of tables is used during database backups, therefore even if you change the ordering for importing
            // the export may also have to be recreated with the new ordering.
            //
            deleteFromTableStatement.executeBatch();
            importTableStatement.executeBatch();  
            
            importTableStatement.close();
            deleteFromTableStatement.close();
            connection.commit();
            
            //return back to original value
            connection.setAutoCommit(true);
            
        }catch(BatchUpdateException exception){
            
            try{
                logger.error("There was a problem executing the batch update.  Attempting to roll back the database.", exception);
                connection.rollback();
                logger.info("Rollback of the database completed.");
            }catch(SQLException sqlException){
                logger.error("There was a problem rolling back after failing to complete the batch update.", sqlException);
            }
            
            throw new Exception("Failed to import the tables.");
        }
        
        logger.info("Import of "+files.size()+" tables completed.");
    }
    
    /**
     * Return the table name based on the table backup file exported using this class.
     * 
     * @param tableBackupFile the backup file created by this class when it backed up (i.e. exported) a table in a derby database
     * @return String the table name associated with the table backup file
     */
    private static String getTableNameFromFile(File tableBackupFile){
        
        if(tableBackupFile == null || !tableBackupFile.exists()){
            throw new IllegalArgumentException("The table backup file of "+tableBackupFile+" doesn't exist.");
        } 
        
        return tableBackupFile.getName().substring(0, tableBackupFile.getName().indexOf(TABLE_BACKUP_FILE_EXT));
    }
    
    /**
     * Import the specified derby database table export file in the currently connected database.
     * 
     * @param connection the connection to the Derby database used to execute commands.
     * @param tableBackupFile an export of a derby database table to import
     * @throws Exception if there was a severe problem importing the table(s)
     */
    private static void importTable(CallableStatement importTableStatement, Statement deleteFromTableStatement, File tableBackupFile) throws Exception{
        
        if(tableBackupFile == null || !tableBackupFile.exists()){
            throw new IllegalArgumentException("The table backup file of "+tableBackupFile+" doesn't exist.");
        } 
        
        String tableName = getTableNameFromFile(tableBackupFile);

        if(tableBackupFile.length() == 0){
            //file is empty
            //Note: if derby tries to import this file it will throw a null pointer exception
            
            logger.info("Ignoring import file for table named "+tableName+" because it is empty.");

        }else{
            
            //SCHEMANAME - use default
            importTableStatement.setString(1, null);
            
            //TABLENAME
            importTableStatement.setString(2, tableName.toUpperCase());
            
            //FILENAME
            importTableStatement.setString(3, tableBackupFile.getAbsolutePath());
            
            //COLUMNDELIMITER
            importTableStatement.setString(4, COLUMN_DELIM);
            
            //CHARACTERDELIMITER
            importTableStatement.setString(5, CHARACTER_DELIM);
            
            //CODESET - use default
            importTableStatement.setString(6, null);
            
            //REPLACE - use replace mode
            importTableStatement.setShort(7, (short)1);
            
            importTableStatement.addBatch();
            
            logger.info("Added import derby table of "+tableName+" from "+tableBackupFile+" to batch.");

        }
        
    }

    /**
     * Retrieve the location where the specified zip file containing an extracted derby database was extracted to, 
     * including the path to the derby database specified by the suffix portion of the database URL.
     * 
     * @param databaseUrl a connection URL (usually from a hibernate config xml file) that specifies the location of the derby database
     *                  being restored (e.g. "GiftUms", "GiftLms").  
     *                  The URL must in the form of:
     *                      - jdbc:derby://localhost:1527/pathFromGIFT/to/derbyDB/GiftUmsORGiftLms
     *                      OR
     *                      - jdbc:derby:pathFromGIFT/to/derbyDB/GiftUmsORGiftLms
     *                  (e.g. "jdbc:derby://localhost:1527/data/derbyDB/GiftUms" or "jdbc:derby:data/derbyDB/GiftUms")
     *                  
     * @param derbyDatabaseZip a zip file containing an exported GIFT derby database.  
     *                The ".zip" file structure must match that of the GIFT derbyDb:
     *                 - derbyDb
     *                     - GiftLms  
     *                                 (choose at least one of these sub-folders to include)
     *                     - GiftUms 
     * @return File the directory of the derby database to use to restore from.
     *          Note: this file should 
     * @throws Exception if there was a problem with the inputs to this method or the temporary unzip location couldn't be created.
     */
    public static File getRestoreDatabaseLocation(String databaseUrl, File derbyDatabaseZip) throws Exception{      
        
        File location = null;
        
        //unzip to temp directory
        String tempDir = FileUtils.getTempDirectoryPath();
        File tempFolderName = new File(tempDir + File.separator + "GIFT_DerbyDb_"+ Long.toString(System.nanoTime()) + File.separator);
        tempFolderName.mkdir();
        ZipUtils.unzipArchive(derbyDatabaseZip, tempFolderName, null);
        
        //check that the restore directory ends with the same directory name of the connection url ("jdbc:derby://localhost:1527/data/derbyDB/GiftUms" or "jdbc:derby:data/derbyDB/GiftUms")
        String[] urlTokens = databaseUrl.split(":");
        if(urlTokens.length == 3 || urlTokens.length == 4){
            //syntax matches the required format
            
            String urlPath = null;
            if(urlTokens.length == 3){
                urlPath = urlTokens[2];
            }else{
                //remove port
                String urlPathWithPort = urlTokens[3];
                urlPath = urlPathWithPort.substring(urlPathWithPort.indexOf("/")+1);
            }
            
            logger.info("Using url path of "+urlPath);
            
            String derbyDbUrlPath = urlPath.substring(urlPath.indexOf("derbyDb"));
            String derbyDbTempPath = tempFolderName.getAbsolutePath() + File.separator + derbyDbUrlPath;
            File derbyDbTemp = new File(derbyDbTempPath);
            if(derbyDbTemp.exists()){
                location = derbyDbTemp;
            }else{
                //ERROR
                throw new IllegalArgumentException("The temporary derby database directory of "+derbyDbTemp+" doesn't exist.  " +
                		"It was created using the database URL suffix of '"+derbyDbUrlPath+"' and the temporary unzip location of "+tempFolderName+".");
            }
            
        }else{
            //ERROR
            throw new IllegalArgumentException("The database URL value of "+databaseUrl+" doesn't follow the syntax of 'jdbc:derby:path/to/derbyDB/GiftUmsORGiftLms");
        }

        return location;
    }
    
    /**
     * Return whether the specified directory contains derby database table backup files.  
     *   
     * @param derbyDatabaseBackupDir the directory to search for database backup files.
     * @return boolean true iff the directory has derby table backup files.
     */
    public static boolean isTableBackup(File derbyDatabaseBackupDir){
        
        File[] files = derbyDatabaseBackupDir.listFiles();
        for(File file : files){
            
            if(file.getName().endsWith(TABLE_BACKUP_FILE_EXT)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Backup the entire derby database referenced by the parameter provided to the zip file which needs
     * to be created.  The backup will be temporarily saved to a temp folder on disk before being zipped.
     * 
     * @param connection the connection to the Derby database
     * @param backupZipFile where to zip the backup files too
     * @throws Exception if there was a problem backing up the derby database
     */
    public static void backupEntireDatabase(Connection connection, File backupZipFile) throws Exception{        
        
        String tempDir = FileUtils.getTempDirectoryPath();
        File tempFolderName = new File(tempDir + File.separator + "GIFT_DerbyDbBackup_"+ Long.toString(System.nanoTime()) + File.separator + "derbyDb" + File.separator);
        tempFolderName.mkdirs();
            
        logger.info("Attempting to backup the derby database temporarily to "+tempFolderName+".");

        // used to execute a derby system call to backup the entire database
        // Reference: http://db.apache.org/derby/docs/10.0/manuals/reference/sqlj117.html#HDRBACKUPDBPROC
        CallableStatement cs = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)"); 
        cs.setString(1, tempFolderName.getCanonicalPath());
        cs.execute(); 
        cs.close();            
        
        File baseFolderName = tempFolderName.getParentFile();
        logger.info("Backup completed.  Now zipping backup folder of "+baseFolderName+" to "+backupZipFile+".");
        
        ZipUtils.zipFolder(baseFolderName, backupZipFile);
        
        logger.info("Zip finished.  Deleting temporary directory of "+tempFolderName+".");
        
        //delete temp folder
        baseFolderName.delete();

    }
    
    /**
     * Backup the specified list of tables to the zip file.
     * 
     * @param connection the connection to the Derby database used to execute commands.
     * @param backupZipFile where to zip the backup files too
     * @param tables list of table names in the database to backup
     * @param databaseName the name of the database where the tables reside (e.g. GiftUms)
     * @throws Exception if there was a problem backing up the derby database table(s)
     */
    public static void backupTables(Connection connection, File backupZipFile, List<String> tables, String databaseName) throws Exception{
        
        String tempDir = FileUtils.getTempDirectoryPath();
        File tempFolderName = new File(tempDir + File.separator + "GIFT_DerbyDbBackup_"+ Long.toString(System.nanoTime()) + File.separator + "derbyDb" + File.separator + databaseName + File.separator);
        tempFolderName.mkdirs();
        
        for(String table : tables){
            backupTable(connection, table, tempFolderName);
        }
        
        File baseFolderName = tempFolderName.getParentFile().getParentFile();
        logger.info("Backup completed.  Now zipping backup folder of "+baseFolderName+" to "+backupZipFile+".");
        
        ZipUtils.zipFolder(baseFolderName, backupZipFile);
        
        logger.info("Zip finished.  Deleting temporary directory of "+tempFolderName+".");
        
        //delete temp folder
        baseFolderName.delete();
    }
    
    /**
     * Backup the specified table to the backup file.  
     * 
     * @param connection the connection to the Derby database
     * @param tableName the name of the existing table to backup to the file
     * @param backupLocation where to create the backup file
     * @throws Exception if there was a problem backing up the derby table
     */
    public static void backupTable(Connection connection, String tableName, File backupLocation) throws Exception{
        
        if(backupLocation == null || backupLocation.isFile()){
            throw new IllegalArgumentException("The backup location of "+backupLocation+" must be a directory.");
        }
        
        String filename = backupLocation.getAbsolutePath() + File.separator + tableName + TABLE_BACKUP_FILE_EXT;
        
        // used to execute a derby system call to backup the table
        // Reference: http://db.apache.org/derby/docs/10.0/manuals/reference/sqlj118.html#HDREXPORTPROC
        CallableStatement cs = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE(?,?,?,?,?,?)"); 
        
        //SCHEMANAME - use default
        cs.setString(1, "APP");
        
        //TABLENAME
        cs.setString(2, tableName.toUpperCase());
        
        //FILENAME
        cs.setString(3, filename);
        
        //COLUMNDELIMITER - use default 
        cs.setString(4, COLUMN_DELIM);
        
        //CHARACTERDELIMITER - use default
        cs.setString(5, CHARACTER_DELIM);
        
        //CODESET - use default
        cs.setString(6, null);
        
        logger.info("Attempting to backup the derby table of "+tableName+" to "+filename+".");
        
        cs.execute(); 
        cs.close(); 
        
        logger.info("Backup of "+tableName+" completed.");
    }
    
    
    /**
     * Applies a *.sql file to the database using the derby ij utility tool.  The *.sql file can
     * contain multiple statements (unlike ddl files), which can be useful to perform multiple
     * related sql commands in a common file.  Note that derby does not support all sql statements such
     * as 'IF NOT EXISTS' syntax on table creation.  See derby documentation and ij documentation for more details.
     * 
     * This method will apply the sql file and expect a 'success' (or result of 0 from ij). If a non zero result
     * is returned, an exception is thrown to indicate that the sql statements could not be applied.
     * 
     * @param connection the connection to the Derby database used to execute commands.
     * @param sqlFile the file path to the *.sql file containing the sql commands to be executed.
     * @throws SQLException if an error or non successful result occurs during the execution of the sql commands, an exception is thrown and must be handled by the caller.
     */
    public static void applySqlFile(Connection connection, File sqlFile) throws SQLException {
        
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(sqlFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            
            logger.info("Attempting to apply sql file to the database: " + sqlFile.getName());
            int result = ij.runScript(connection, inStream, "UTF-8", printStream, "UTF-8");
            logger.debug("Raw sql logs: " + outputStream.toString());
            
            
            if (result != 0) {
                logger.info("Applying sql file: " + sqlFile.getName() + " failed. ");
                throw new Exception("Failed to apply sql file to the database. A successful result was not returned.  The full details are: " + outputStream.toString());
            } else {
                
                logger.info("Applying sql file: " + sqlFile.getName() + " returned success.");
            }
        } catch (Exception e) {
            throw new SQLException(e);
            
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            }
        }

    }
    
}
