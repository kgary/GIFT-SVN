/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for performing operations on a MySQL database
 *
 * @author jleonard
 */
public class MysqlDatabaseUtil {

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(MysqlDatabaseUtil.class);

//    /**
//     * Backups a MySQL database with to a MySQL dump file
//     *
//     * @param mysqlBinDirectory The directory the MySQL binaries are in, if null
//     * the binaries on the classpath will be used
//     * @param url The URL of the MySQL server, if null the server is assumed to
//     * be on the local machine
//     * @param port The port of the MySQL server, if 0 or negative is it assumed
//     * to be the default port of 3306
//     * @param username The username of a user on the MySQL server, if null no
//     * username is used
//     * @param password The password of the user, if null no password is used
//     * @param databaseName The name of the database(s) to backup, if null it
//     * will backup all databases
//     * @param outputFile The file to put the MySQL dump into
//     */
//    public static synchronized void backupDatabase(String mysqlBinDirectory, String url, int port, String username, String password, String databaseName, String outputFile) {
//        StringBuilder backupCommand = new StringBuilder();
//        backupCommand.append("\"");
//        if (mysqlBinDirectory != null) {
//            backupCommand.append(mysqlBinDirectory).append("/");
//        }
//        backupCommand.append("mysqldump\" --add-drop-database ");
//        if (url != null) {
//            backupCommand.append("-h \"").append(url).append("\" ");
//        }
//        if (port > 0) {
//            backupCommand.append("-P ").append(port).append(" ");
//        }
//        if (username != null) {
//            backupCommand.append("-u \"").append(username).append("\" ");
//        }
//        if (password != null) {
//            backupCommand.append("--password=\"").append(password).append("\" ");
//        }
//        if (databaseName != null) {
//                backupCommand.append("--databases ").append(databaseName).append(" ");
//        } else {
//            backupCommand.append("--all-databases ");
//        }
//        try {
//            File databaseBackupFile = new File(outputFile);
//            OutputStream backupOutputStream = new FileOutputStream(databaseBackupFile);
//            logger.info("Dumping MySQL database with command: '" + backupCommand.toString() + "'");
//            Process process = Runtime.getRuntime().exec(backupCommand.toString());
//            InputStream mysqlInputStream = process.getInputStream();
//
//            IOUtils.copy(mysqlInputStream, backupOutputStream);
//
//            backupOutputStream.close();
//            mysqlInputStream.close();
//        } catch (IOException e) {
//            logger.warn("Caught an IOException while backing up a database", e);
//        }
//    }

//    /**
//     * Backups a MySQL database with to a MySQL dump file (Schema Only)
//     *
//     * @param mysqlBinDirectory The directory the MySQL binaries are in, if null
//     * the binaries on the classpath will be used
//     * @param url The URL of the MySQL server, if null the server is assumed to
//     * be on the local machine
//     * @param port The port of the MySQL server, if 0 or negative is it assumed
//     * to be the default port of 3306
//     * @param username The username of a user on the MySQL server, if null no
//     * username is used
//     * @param password The password of the user, if null no password is used
//     * @param databaseName The name of the database(s) to backup, if null it
//     * will backup all databases
//     * @param outputFile The file to put the MySQL dump into
//     */
//    public static synchronized void backupDatabaseSchema(String mysqlBinDirectory, String url, int port, String username, String password, String databaseName, String outputFile) {
//        StringBuilder backupCommand = new StringBuilder();
//        backupCommand.append("\"");
//        if (mysqlBinDirectory != null) {
//            backupCommand.append(mysqlBinDirectory).append("/");
//        }
//        backupCommand.append("mysqldump\" --add-drop-database --no-data ");
//        if (url != null) {
//            backupCommand.append("-h \"").append(url).append("\" ");
//        }
//        if (port > 0) {
//            backupCommand.append("-P ").append(port).append(" ");
//        }
//        if (username != null) {
//            backupCommand.append("-u \"").append(username).append("\" ");
//        }
//        if (password != null) {
//            backupCommand.append("--password=\"").append(password).append("\" ");
//        }
//        if (databaseName != null) {
//            backupCommand.append("--databases ").append(databaseName).append(" ");
//        } else {
//            backupCommand.append("--all-databases ");
//        }
//        try {
//            File databaseBackupFile = new File(outputFile);
//            OutputStream backupOutputStream = new FileOutputStream(databaseBackupFile);
//            logger.info("Dumping MySQL database with command: '" + backupCommand.toString() + "'");
//            Process process = Runtime.getRuntime().exec(backupCommand.toString());
//            InputStream mysqlInputStream = process.getInputStream();
//
//            IOUtils.copy(mysqlInputStream, backupOutputStream);
//
//            backupOutputStream.close();
//            mysqlInputStream.close();
//        } catch (IOException e) {
//            logger.warn("Caught an IOException while backing up a database", e);
//        }
//    }

    /**
     * Backups a MySQL database with to a MySQL dump file (Data only)
     *
     * @param mysqlBinDirectory The directory the MySQL binaries are in, if null
     * the binaries on the classpath will be used
     * @param url The URL of the MySQL server, if null the server is assumed to
     * be on the local machine
     * @param port The port of the MySQL server, if 0 or negative is it assumed
     * to be the default port of 3306
     * @param username The username of a user on the MySQL server, if null no
     * username is used
     * @param password The password of the user, if null no password is used
     * @param databaseName The name of the database(s) to backup, if null it
     * will backup all databases
     * @param tableNames The name of the tables to backup the data of
     * @param outputFile The file to put the MySQL dump into
     */
    public static synchronized void backupDatabaseData(String mysqlBinDirectory, String url, int port, String username, String password, String databaseName, List<String> tableNames, String outputFile) {
        
        StringBuilder backupCommand = new StringBuilder();
        backupCommand.append("\"");
        
        if (mysqlBinDirectory != null) {
            backupCommand.append(mysqlBinDirectory).append("/");
        }
        
        //no-create-db      This option suppresses the CREATE DATABASE statements
        //complete-insert   Use complete INSERT statements that include column names
        //insert-ignore     Write INSERT IGNORE statements rather than INSERT statements
        //                  An error will occur when inserting a new record in MySQL if the primary key specified in the insert query already exists. 
        //                  Using the "IGNORE" keyword prevents errors from occurring and other queries are still able to be run.
        //no-create-info    Do not write CREATE TABLE statements that re-create each dumped table
        backupCommand.append("mysqldump\" --no-create-db --complete-insert --insert-ignore --no-create-info ");
        if (url != null) {
            backupCommand.append("-h \"").append(url).append("\" ");
        }
        if (port > 0) {
            backupCommand.append("-P ").append(port).append(" ");
        }
        if (username != null) {
            backupCommand.append("-u \"").append(username).append("\" ");
        }
        if (password != null) {
            backupCommand.append("--password=\"").append(password).append("\" ");
        }
        
        if (databaseName != null) {
            
            if (tableNames != null) {
                
                backupCommand.append(databaseName).append(" ");

                for (String tableName : tableNames) {

                    backupCommand.append(tableName).append(" ");
                }
            } else {

                backupCommand.append("--databases ").append(databaseName).append(" ");
            }
        } else {
            
            backupCommand.append("--all-databases ");
        }
        
        try {
            File databaseBackupFile = new File(outputFile);
            OutputStream backupOutputStream = new FileOutputStream(databaseBackupFile);
            logger.info("Dumping MySQL database with command: '" + backupCommand.toString() + "'");
            Process process = Runtime.getRuntime().exec(backupCommand.toString());
            InputStream mysqlInputStream = process.getInputStream();

            IOUtils.copy(mysqlInputStream, backupOutputStream);

            backupOutputStream.close();
            mysqlInputStream.close();
        } catch (IOException e) {
            logger.warn("Caught an IOException while backing up a database", e);
        }
    }

    /**
     * Restores a MySQL database with a MySQL dump file
     *
     * @param mysqlBinDirectory The directory the MySQL binaries are in, if null
     * the binaries on the classpath will be used
     * @param url The URL of the MySQL server, if null the server is assumed to
     * be on the local machine
     * @param port The port of the MySQL server, if 0 or negative is it assumed
     * to be the default port of 3306
     * @param username The username of a user on the MySQL server, if null no
     * username is used
     * @param password The password of the user, if null no password is used
     * @param databaseName The name of the database to restore with the dump
     * file, if null it is assumed that the input file specifies the database(s)
     * @param inputFile The MySQL dump file
     */
    public static synchronized void restoreDatabase(String mysqlBinDirectory, String url, int port, String username, String password, String databaseName, String inputFile) {
        StringBuilder backupCommand = new StringBuilder();
        backupCommand.append("\"");
        if (mysqlBinDirectory != null) {
            backupCommand.append(mysqlBinDirectory).append("/");
        }
        backupCommand.append("mysql\" ");
        if (url != null) {
            backupCommand.append("-h \"").append(url).append("\" ");
        }
        if (port > 0) {
            backupCommand.append("-P ").append(port).append(" ");
        }
        if (username != null) {
            backupCommand.append("-u \"").append(username).append("\" ");
        }
        if (password != null) {
            backupCommand.append("--password=\"").append(password).append("\" ");
        }
        if (databaseName != null) {
            backupCommand.append(databaseName).append(" ");
        }
        try {
            File databaseBackupFile = new File(inputFile);
            InputStream backupInputStream = new FileInputStream(databaseBackupFile);
            logger.info("Restoring MySQL database with command: '" + backupCommand.toString() + "'");
            Process process = Runtime.getRuntime().exec(backupCommand.toString());
            OutputStream mysqlOuputStream = process.getOutputStream();

            IOUtils.copy(backupInputStream, mysqlOuputStream);

            mysqlOuputStream.close();
            backupInputStream.close();
        } catch (IOException e) {
            logger.warn("Caught an IOException while restoring the database with a backup", e);
        }
    }

    private MysqlDatabaseUtil() {
    }
}
