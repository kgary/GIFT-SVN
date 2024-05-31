/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.FileUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.jdbc.Work;
import org.hibernate.jdbc.util.FormatStyle;
import org.hibernate.jdbc.util.Formatter;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for hibernate util classes and contains the common logic
 * to configure a hibernate connection to a database.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractHibernateUtil {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractHibernateUtil.class);
    
    /** string that identifies hibernate as using MySQL */
    public static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";
    
    /** string that identifies hibernate as using Derby */
    public static final String DERBY_DIALECT = "org.hibernate.dialect.DerbyDialect";
    
    /** hibernate config property keys */
    public static final String DRIVER_CLASS     = "connection.driver_class";
    public static final String CONNECTION_URL   = "connection.url";
    public static final String DIALECT          = "dialect";
    public static final String USERNAME         = "connection.username";
    public static final String PASSWORD         = "connection.password";
    public static final String SCHEMA           = "hibernate.default_schema";
    
    private static final String TYPE_NAME   = "TYPE_NAME";
	
	/** represents an entire set of mappings of an applications Java types to an SQL db */
	protected Configuration config;

	/** hibernate's session factory - used to create session instances */
	private SessionFactory sessionFactory;
	
	/** the hibernate configuration file to use */
	protected String configFileName;
	
	/**
	 * Build the specific hibernate configuration
	 * 
	 * @return Configuration - the configuration created
	 */
	protected abstract Configuration buildConfiguration();
	
	/**
	 * Class constructor - provided the hibernate configuration file name
	 * 
     * @param configFileName - the hibernate configuration file
     * @param createSchema Specifies if the schema should be rebuilt
	 * @throws ConfigurationException if there was a problem initializing the hibernate connection
	 */
	public AbstractHibernateUtil(String configFileName, boolean createSchema) throws ConfigurationException{
		
	    if(configFileName == null){
	        throw new IllegalArgumentException("The configuration file name can't be null.");
	    }
	    
		this.configFileName = configFileName;
		
		try{
		    initialize(null, createSchema, null, null, null);
		}catch(Exception e){
		    throw new ConfigurationException("Failed to initialize hibernate connection.",
		            e.getMessage(),
		            e);
		}
    }

    /**
     * Class constructor - provided the hibernate configuration file name
     *
     * @param configFileName - the hibernate configuration file
     * @throws ConfigurationException if there was a problem initializing the hibernate connection
     */
    public AbstractHibernateUtil(String configFileName) throws ConfigurationException {
        this(configFileName, false);
    }
    
    /**
     * Initialize the hibernate connection and session.
     * 
     * @param restoreFile  Restore the database from a file containing the necessary information about the database data.  Can be null.
     *                      In MySQL this is a ".sql" script file with INSERT statements.
     *                      In Derby, for GIFT, this is a ".zip" file of a Derby database (UMS or LMS).
     * @param createSchema Specifies if the schema should be rebuilt
     * @param constraintOrderedTableNames an ordering of table names based on key constraints in those tables.
     *                  The ordering should be based on SQL INSERT operations where, for example, a foreign key
     *                  must exist in a table before creating an entry that references that foreign key in a different table. 
     * @param constraintOrderedTablesToClear an ordering of table names based on key constraints in those tables to remove
     *                  all rows from.  This operation supports the idea of restoring a database by making the tables match
     *                  what is in the backup.  Can be null or empty.
     * @param permissionsTableNames a list of all visible and editable to usernames tables.
     * @throws Exception if there was a problem initializing the hibernate connection
     * @throws ConfigurationException if there was a problem connecting to the database
     */
    private void initialize(File restoreFile, boolean createSchema, List<String> constraintOrderedTableNames, List<String> constraintOrderedTablesToClear, List<String> permissionsTableNames) 
    		throws Exception, ConfigurationException{
           
        if(logger.isInfoEnabled()){
            logger.info("Initializing hibernate configuration");
        }
        
        Configuration config = buildConfiguration();
        configure(config, restoreFile, constraintOrderedTableNames, constraintOrderedTablesToClear, permissionsTableNames);
        
        //set configuration to configuration that succeeded without issue
        this.config = config;        
        
        sessionFactory = buildSessionFactory(createSchema);        
    }
    
    /**
     * Get the list of database backups available in the specified directory for the current configuration dialect.
     * 
     * Note: MySQL dialect uses '.sql' files, where Derby dialect uses '.zip' files
     * 
     * @param databaseBackupsDirectoryName the directory where database backups can be found
     * @return List<File> collection of database backup files found for the current configuration dialect.
     */
    public List<File> getDatabaseBackupFileNames(String databaseBackupsDirectoryName){
        
        if (databaseBackupsDirectoryName != null) {

            File databaseBackupsDirectory = new File(databaseBackupsDirectoryName);

            if (databaseBackupsDirectory.isDirectory()) {

                final String dialect = config.getProperty(DIALECT);
                File[] files = databaseBackupsDirectory.listFiles(new FilenameFilter() {
                    
                    @Override
                    public boolean accept(File dir, String name) {
                        
                        if(dialect.equals(MYSQL_DIALECT) && name.endsWith(".sql") ){
                            return true;
                        }else if(dialect.equals(DERBY_DIALECT) && name.endsWith(".zip")){
                            return true;
                        }
                        
                        return false;
                    }
                });
                
                List<File> databaseBackups = new ArrayList<>();

                for (File file : files) {

                    if (file.isFile()) {
                        databaseBackups.add(file);
                    }
                }

                return databaseBackups;

            } else {

                logger.error("Could not get the list of database backups, '" + databaseBackupsDirectory + "' is not a directory");
            }

        } else {

            logger.error("Could not get list of database backups, the database backups directory property is undefined.");
        }
        
        return null;
    }
    
    /**
     * Restore the database from a file containing the necessary information about the database data.
     * In MySQL this is a ".sql" script file with INSERT statements.
     * In Derby, for GIFT, this is a ".zip" file of a Derby database (UMS or LMS).
     * 
     * @param restoreFile the file containing information for a GIFT database.  
     *          For Derby, the ".zip" file structure must match that of the GIFT derbyDb:
     *                 - derbyDb
     *                     - GiftLms  
     *                                 (choose at least one of these sub-folders to include)
     *                     - GiftUms 
     * @param constraintOrderedTableNames an ordering of table names based on key constraints in those tables.
     *                  The ordering should be based on SQL INSERT operations where, for example, a foreign key
     *                  must exist in a table before creating an entry that references that foreign key in a different table. 
     * @param constraintOrderedTablesToClear an ordering of table names based on key constraints in those tables to remove
     *                  all rows from.  This operation supports the idea of restoring a database by making the tables match
     *                  what is in the backup.  Can be null or empty.
     * @param permissionsTableNames a list of all visible and editable to usernames tables.
     * @throws Exception if there was a problem restoring the database from the file provided.
     * @throws ConfigurationException if there was a problem connecting to the database
     */
    public void restoreFrom(File restoreFile, List<String> constraintOrderedTableNames, List<String> constraintOrderedTablesToClear, List<String> permissionsTableNames) 
    		throws Exception, ConfigurationException{
        
        if(restoreFile == null || !restoreFile.exists()){
            throw new IllegalArgumentException("The restore file of "+restoreFile+" doesn't exist.");
        }
        
        initialize(restoreFile, false, constraintOrderedTableNames, constraintOrderedTablesToClear, permissionsTableNames);
    }
    
    /**
     * Shutdown any hibernate session as well as any lingering database items that 
     * need to be cleaned up to allow for a multi-user system (e.g. starting the database
     * from different windows user accounts sequentially)
     * 
     * @throws Exception if there was a server problem shutting down
     */
    public void shutdown() throws Exception{        
        
        // shutdown the derby db (not the process) gracefully to remove the lock files
        try{
            final String connectionUrl = config.getProperty(CONNECTION_URL);
            DriverManager.getConnection(connectionUrl + ";shutdown=true");
        }catch(@SuppressWarnings("unused") SQLNonTransientConnectionException exception){
            // expected
        }
        
        if(getSessionFactory() != null){
            getSessionFactory().close();
        }

    }
    
    /**
     * Backup the database currently connected too. 
     * 
     * @param backupFileNameNoExtension the full file name of where to backup the database too.  
     *              Note: A file extension will be added based on the type of backup performed.
     * @param tableClasses optional list of table classes to backup.  If this is null the entire database will be backed up.
     * @param permissionsTableNames a list of visible and editable usernames tables to backup.
     * @throws Exception if there was a severe problem backing up the database 
     */
    public void backupTo(String backupFileNameNoExtension, List<Class<? extends Object>> tableClasses, List<String> permissionsTableNames) throws Exception{
        
        if(tableClasses != null && !tableClasses.isEmpty()){
            backupTables(backupFileNameNoExtension, tableClasses, permissionsTableNames);
        }else{
            backupEntireDatabase(backupFileNameNoExtension);
        }        

    }
    
    /**
     * Used for development purposes to output the results of the initial hibernate configuration.
     * This can output table schema information such as create/update statements.
     * 
     * @param out the stream to send the output to
     * @param generateCreateQueries whether to include table create statements in the output
     * @param generateDropQueries whether to include table drop statements in the output
     */
    public void export(OutputStream out, boolean generateCreateQueries, boolean generateDropQueries) {

        Dialect hibDialect = Dialect.getDialect(config.getProperties());
        try (PrintWriter writer = new PrintWriter(out)) {

            if (generateCreateQueries) {
                String[] createSQL = config.generateSchemaCreationScript(hibDialect);
                write(writer, createSQL, FormatStyle.DDL.getFormatter());
            }
            if (generateDropQueries) {
                String[] dropSQL = config.generateDropSchemaScript(hibDialect);
                write(writer, dropSQL, FormatStyle.DDL.getFormatter());
            }
        }
    }
    
    private void write(PrintWriter writer, String[] lines, Formatter formatter) {

        for (String string : lines){
            writer.println(formatter.format(string) + ";");
        }
    }
    
    /**
     * Backup the specific list of tables.
     * 
     * @param backupFileNameNoExtension the full file name of where to backup the database too.  
     *              Note: A file extension will be added based on the type of backup performed.
     * @param tableClasses optional list of table classes to backup.  Cannot be null or empty.
     * @param permissionsTableNames a list of visible and editable usernames tables to backup.
     * @throws Exception if there was a severe problem backing up the database tables listed
     */
    private void backupTables(final String backupFileNameNoExtension, final List<Class<? extends Object>> tableClasses, final List<String> permissionsTableNames) throws Exception{
        
        if(tableClasses == null || tableClasses.isEmpty()){
            throw new IllegalArgumentException("The list of table classes to backup must not be empty.");
        }
        
        String dialect = config.getProperty(DIALECT);
        final String connectionUrl = config.getProperty(CONNECTION_URL);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{
                        String databaseName = "databaseNameHere";
                        if(connectionUrl.contains("/")){
                            databaseName = connectionUrl.substring(connectionUrl.lastIndexOf("/"));
                        }else{
                            logger.error("Unable to retrieve database name from connection URL of "+connectionUrl+", therefore the backup will not succeed.");
                        }
                        
                        List<String> tableNames = AbstractHibernateDatabaseManager.getTableNames(tableClasses);
                        tableNames.addAll(permissionsTableNames);
                        
                        DerbyDatabaseUtil.backupTables(connection, new File(backupFileNameNoExtension + ".zip"), tableNames, databaseName);
                    }catch(Exception e){
                        logger.error("Caught exception while trying to backup the derby database table(s) from list.", e);
                        throw new SQLException(e);
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Finished derby database backup table(s) command.");
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            
            if(logger.isInfoEnabled()){
                logger.info("Finished backing up derby database table(s) from list.");
            }
            
        } else{
            throw new Exception("The database backup will not be performed because unable to handle dialect of "+dialect+".");
        }
    }
    
    /**
     * Create the database table (if it doesn't already exist) using the commands found in the file.
     * 
     * @param tableName the name of the table (from the create table statement in the file).  This is used
     * to determine if the table already exist.
     * @param statementFileList contains the list of ddl files that can be run.  The statements will be run IN ORDER
     * of the list, where typically the first statement should be the create table statement.  Secondary statements
     * can be used for other things such as adding indexes to the table.  The files must exist.
     * @throws Exception if there was a problem executing the statements in the file.
     */
    protected void runCreateTableIfNotExist(String tableName, ArrayList<File> statementFileList) throws Exception{
        
        if (statementFileList == null || statementFileList.isEmpty()) {
            throw new IllegalArgumentException("statementFileList cannot be empty.");
        }
        
        
        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{   
                        
                        if (!doesTableExistInDb(tableName)) {
                            for (File statementsFile : statementFileList) {
                                
                                if(!statementsFile.exists()){
                                    throw new FileNotFoundException("Can't find the file '"+statementsFile+"'.");
                                }
                                
                                if(logger.isInfoEnabled()){
                                    logger.info("Running statement file: " + statementsFile.getPath());
                                }
                                DerbyDatabaseUtil.runStatements(connection, statementsFile);
                            }
                        } else {
                            // Table exists - nothing to do
                            return;
                        }
                         
                    }catch(Exception e){
                        logger.error("Caught exception while trying to run SQL commands for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Finished applying SQL commands to derby database for table '"+tableName+"'.");
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            
            if(logger.isInfoEnabled()){
                logger.info("Finished running commands for table '"+ tableName +"'.");
            }
            
        } else{
            throw new Exception("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
    }
    
    /**
     * Backup the currently connected database to the specified file name.  The file name should lack a file extension
     * in order to allow the particular connection dialect to determine the appropriately supported file type.
     * 
     * @param backupFileNameNoExtension the full file name of where to backup the database too.  
     *              Note: A file extension will be added based on the type of backup performed.
     * @throws Exception if there was a problem backing up the database
     */
    private void backupEntireDatabase(final String backupFileNameNoExtension) throws Exception{
        
        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{
                        DerbyDatabaseUtil.backupEntireDatabase(connection, new File(backupFileNameNoExtension + ".zip"));
                        
                    }catch(Exception e){
                        logger.error("Caught exception while trying to backup the derby database.", e);
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Finished derby database backup command.");
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            
            if(logger.isInfoEnabled()){
                logger.info("Finished backing up derby database.");
            }
            
        }else{
            throw new Exception("The database backup will not be performed because unable to handle dialect of "+dialect+".");
        }
    }
	
	/**
	 * Configure the Configuration using the restore directory or the configuration file.
	 * 
	 * @param config - the configuration build initialized
	 * @param restoreFile - a database directory to restore from.  Can be null.  If null, use the configuration file.
     * @param constraintOrderedTableNames an ordering of table names based on key constraints in those tables.
     *                  The ordering should be based on SQL INSERT operations where, for example, a foreign key
     *                  must exist in a table before creating an entry that references that foreign key in a different table. 
     * @param constraintOrderedTablesToClear an ordering of table names based on key constraints in those tables to remove
     *                  all rows from.  This operation supports the idea of restoring a database by making the tables match
     *                  what is in the backup.  Can be null or empty.
	 * @param permissionsTableNames a list of all visible and editable to usernames tables.
	 * @throws Exception if there was a problem configuring the hibernate connection
	 */
	private void configure(Configuration config, File restoreFile, List<String> constraintOrderedTableNames, List<String> constraintOrderedTablesToClear, List<String> permissionsTableNames) throws Exception{
	    
        //reads configuration file
        // -tells hibernate how to access the underlying db
        //default: hibernate.cfg.xml
        //optional: specify file name
        String dir = System.getProperty("user.dir");
        config.configure(new File(dir + File.separator + PackageUtil.getConfiguration() + File.separator +configFileName));
	    
	    if(restoreFile != null){
	        
            final String connectionUrl = config.getProperty(CONNECTION_URL);
            String dialect = config.getProperty(DIALECT);
            
            if(dialect.equals(DERBY_DIALECT)) {
                
                final File restoreDirectory = DerbyDatabaseUtil.getRestoreDatabaseLocation(connectionUrl, restoreFile);
                
                if(DerbyDatabaseUtil.isTableBackup(restoreDirectory)){
                    
                    DerbyDatabaseUtil.importTables(DriverManager.getConnection(connectionUrl), restoreDirectory, constraintOrderedTableNames, constraintOrderedTablesToClear, permissionsTableNames);
                    
                }else{
                
                    if(logger.isInfoEnabled()){
                        logger.info("Using restory directory of "+restoreDirectory+" for derby restore.");
                    }
                    
                    try {
                      DriverManager.getConnection("jdbc:derby://localhost:1527/derbyDb/GiftUms;shutdown=true");
                    }catch (@SuppressWarnings("unused") SQLException ex) {
                        //expect exception as per apache derby documentation, therefore ignore it
                    }
                    
                    try {
                        Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/derbyDb/GiftUms;restoreFrom="+restoreDirectory.getAbsolutePath(), new Properties());
                        conn.commit();
                      }catch (SQLException ex) {
                          logger.warn("Caught exception while restoring", ex);
                      } 
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Configured derby database with connection.url of "+config.getProperty("connection.url")+".");
                    }
                }
                
                //make sure to delete the temp directory (which is the grandparent of the derby database directory)
                if(restoreDirectory != null){
                    if(logger.isInfoEnabled()){
                        logger.info("Deleting temp directory of "+restoreDirectory.getParentFile().getParentFile()+".");
                    }
                    FileUtil.delete(restoreDirectory.getParentFile().getParentFile());  
                }

                
            } else{
                logger.error("Unable to handle dialect of "+dialect+", therefore the hibernate configuration has failed.");
            }
	    }

	}

	/**
	 * Create the session factory
	 * 
	 * @return SessionFactory - the created session factory,
	 * null if the session factory could not be created.
	 * @throws ConfigurationException if there was a problem connecting to the database
	 */
    protected SessionFactory buildSessionFactory(boolean createSchema) throws ConfigurationException {
    	
    	String url = config.getProperty(CONNECTION_URL);
    	String user = config.getProperty(USERNAME);
    	String password = config.getProperty(PASSWORD);
    	String dialect = config.getProperty(DIALECT);
    	String driver = config.getProperty(DRIVER_CLASS);
    	
        if(logger.isInfoEnabled()){
            logger.info("Attempting to connect to " + url);
        }
        
        DatabaseCredentialChecker checker = null;
        if(dialect.equals(MYSQL_DIALECT)) {
        	checker = new MysqlCredentialChecker();
        } else if(dialect.equals(DERBY_DIALECT)) {
        	checker = new DerbyCredentialChecker();
        } else{
            throw new ConfigurationException("Failed to connect to the database.",
                    "Unrecognized dialect " + dialect +
                    ", manual inspection of hibernate configuration is required.",
                    null);
        }
        
        boolean success = checker.checkCredentials(url, user, password, driver);
        
        if(!success) {
            logger.error("Connection to " + url + " failed.");
        	throw new ConfigurationException("Failed to connect to the database",
        	        "Could not connect to database with given credentials" +
        	        ", manual inspection of hibernate configuration is required.",
        	        null);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Connection succeeded!");
        }
        
        try {
            
            if(createSchema) {
                
                createSchema();
            }
            
            // Create the SessionFactory from hibernate.cfg.xml
        	return config.buildSessionFactory();
        	
        } catch (Throwable ex) {

            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed.", ex);

            throw new ExceptionInInitializerError(ex);
        }

    }

    /**
     * Return the session factory
     * 
     * @return SessionFactory - the session factory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    /**
     * Return the hibernate configuration
     * 
     * @return Configuration - the configuration
     */
    public Configuration getConfig(){
    	return config;
    }
    
    /**
     * Return the schema value from the hibernate configuration file.
     * This is useful when needed to reference the db tables using SQL (not HQL) statements.
     * For example:  "Select * from App.SurveyContext" is an SQL statement where "App" is the schema.
     * 
     * @return the schema property value
     */
    public String getSchemaFromConfig(){
        return config.getProperty(SCHEMA);        
    }
	
	/**
	 * Create the schema
	 * NOTE: 
	 * 1) this method will drop the tables and their data
	 * 2) if you just want to update an existing table, use the hibernate.cfg.xml solution commented out in ums.hibernate.cfg.xml
	 */
	public void createSchema(){
		
		//only run this once in project - it creates the tables.
		// after tables are created, dont run it again
		// because it drops the tables and you loose existing data
		//script - print hibernate SQL statements to log file
		//export - print hibernate SQL statements to the db
		new SchemaExport(config).create(true, true);

		//have to rebuild the session factory after creating schema
		//buildSessionFactory();
	}	

	/**
	 * Applies a *.sql file to the database. 
	 * 
	 * @param tableName The name of the table to check the existence for.
	 * @param sqlFile The name of the *.sql file to apply to the database if the table doesn't exist.
	 * @param ifTableDoesntExist true if the sql file should only be applied if the table mentioned doesn't exist yet.
	 * @throws Exception if the arguments are invalid, connection the database couldn't be established or checking
	 * the database for the table caused an error.
	 */
    protected void applySqlFile(String tableName, File sqlFile, boolean ifTableDoesntExist) throws Exception{
        
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be empty.");
        }
        
        if (sqlFile == null || !sqlFile.exists()) {
            throw new IllegalArgumentException("sqlFile paraemter must be valid and exist: " + sqlFile);
        }        
        
        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    try{   

                        if (!ifTableDoesntExist || !doesTableExistInDb(tableName)) {
                            // this will succeed or throw an exception.
                            DerbyDatabaseUtil.applySqlFile(connection, sqlFile);
                        }
                       
                    } catch(Exception e){
                        logger.error("Caught exception applying sql file '" + sqlFile.getPath() + "' for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            session.close();
            
            
        } else{
            throw new Exception("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
    }
    
    /**
     * Applies a *.sql file to the database if the column doesn't exist in the table. 
     * 
     * @param tableName The name of the table to check the existence of the column in.
     * @param columnName the name of the column to check for in the table
     * @param sqlFile The name of the *.sql file to apply to the database if the table doesn't exist.
     * @throws Exception if the arguments are invalid, connection the database couldn't be established or checking
     * the database for the column in the table caused an error.
     */
    protected void applySqlFileIfColumnNotExist(String tableName, String columnName, File sqlFile) throws Exception{
        
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be empty.");
        }
        
        if (sqlFile == null || !sqlFile.exists()) {
            throw new IllegalArgumentException("sqlFile paraemter must be valid and exist: " + sqlFile);
        }        
        
        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    try{   

                        if (!doesColumnExistInTable(tableName, columnName)) {
                            // this will succeed or throw an exception.
                            DerbyDatabaseUtil.applySqlFile(connection, sqlFile);
                        }
                       
                    } catch(Exception e){
                        logger.error("Caught exception applying sql file '" + sqlFile.getPath() + "' for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            session.close();
            
            
        } else{
            throw new Exception("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
    
    }
    
    /**
     * Applies a *.sql file to the database if the column doesn't exist or is not the type specified in the table. 
     * 
     * @param tableName The name of the table to check the existence of the column in.  Can't be null or empty.
     * @param columnName the name of the column to check for in the table.  Can't be null or empty.
     * @param columnType the SQL column type name value to check for (e.g. VARCHAR).  Can't be null or empty.
     * @param sqlFile The name of the *.sql file to apply to the database if the table doesn't exist.
     * @throws Exception if the arguments are invalid, connection the database couldn't be established or checking
     * the database for the column in the table caused an error.
     */
    protected void applySqlFileIfColumnNotOfType(String tableName, String columnName, String columnType, File sqlFile) throws Exception{
        
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be empty.");
        }
        
        if (sqlFile == null || !sqlFile.exists()) {
            throw new IllegalArgumentException("sqlFile paraemter must be valid and exist: " + sqlFile);
        }        
        
        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            
            Work work = new Work() {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    try{   

                        if (!doesColumnExistAsTypeInTable(tableName, columnName, columnType)) {
                            // this will succeed or throw an exception.
                            DerbyDatabaseUtil.applySqlFile(connection, sqlFile);
                        }
                       
                    } catch(Exception e){
                        logger.error("Caught exception applying sql file '" + sqlFile.getPath() + "' for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            session.close();
            
            
        } else{
            throw new Exception("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
    
    }
    
    /**
     * Checks if a column with the given name and type exists in the table specified.
     * 
     * @param tableName The name of the table to check the existence of the column in.  Can't be null or empty.
     * @param columnName the name of the column to check for in the table.  Can't be null or empty.
     * @param columnType the SQL column type name value to check for (e.g. VARCHAR).  Can't be null or empty.
     * @return True if the table contains the column, false otherwise.
     * @throws IllegalArgumentException if the table or column name is null or empty
     * @throws SQLException if the database is of a different dialect other than Derby
     */
    private boolean doesColumnExistAsTypeInTable(String tableName, String columnName, String columnType) throws IllegalArgumentException, SQLException{
        
        boolean columnExists = false;
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be null or empty.");
        }else if(columnName == null || columnName.isEmpty()){
            throw new IllegalArgumentException("columnName parameter cannot be null or empty.");
        }else if(columnType == null || columnType.isEmpty()){
            throw new IllegalArgumentException("columnType parameter cannot be null or empty.");
        }

        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();            

            WorkResult<Boolean> work = new WorkResult<Boolean>(false) {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{   
                        DatabaseMetaData dbm = connection.getMetaData();
                        
                         // check if column is there
                         ResultSet columns = dbm.getColumns(null, null, tableName, columnName);
                         if (columns.next()) {
                             
                             String typeName = columns.getString(TYPE_NAME);
                             if(typeName != null && typeName.equalsIgnoreCase(columnType)){
                                 // column exists and is matching type - nothing to do
                                 setResult(true);
                             }
                         }
                         
                         if(logger.isInfoEnabled()){
                             logger.info("doesColumnExistAsTypeInTable() for table: " + tableName + " and column: " + columnName + " and columnType: " + columnType + "  returned " + getResult());
                         }

                    }catch(Exception e){
                        logger.error("Caught exception while trying to run SQL commands for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            columnExists = work.getResult();
            session.close();

            
        } else{
            throw new SQLException("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
       
        return columnExists;
    }
    
    /**
     * Checks if a column with the given name exists in the table specified.
     * 
     * @param tableName The name of the table to check the existence of the column in.
     * @param columnName the name of the column to check for in the table
     * @return True if the table contains the column, false otherwise.
     * @throws IllegalArgumentException if the table or column name is null or empty
     * @throws SQLException if the database is of a different dialect other than Derby
     */
    private boolean doesColumnExistInTable(String tableName, String columnName) throws IllegalArgumentException, SQLException{
        
        boolean columnExists = false;
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be null or empty.");
        }else if(columnName == null || columnName.isEmpty()){
            throw new IllegalArgumentException("columnName parameter cannot be null or empty.");
        }

        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();            

            WorkResult<Boolean> work = new WorkResult<Boolean>(false) {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{   
                        DatabaseMetaData dbm = connection.getMetaData();
                        
                         // check if column is there
                         ResultSet columns = dbm.getColumns(null, null, tableName, columnName);
                         if (columns.next()) {
                             // column exists - nothing to do
                             setResult(true);
                         }
                         
                         if(logger.isInfoEnabled()){
                             logger.info("doesColumnExistInTable() for table: " + tableName + " and column: " + columnName + " returned " + getResult());
                         }

                    }catch(Exception e){
                        logger.error("Caught exception while trying to run SQL commands for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            columnExists = work.getResult();
            session.close();

            
        } else{
            throw new SQLException("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
       
        return columnExists;
    }
	
    /**
     * Checks if a table exists in the database.
     * 
     * @param tableName the name of the table to check for.
     * @return True if the table exists in the database, false otherwise.
     * @throws IllegalArgumentException if the table name is null or empty
     * @throws SQLException if the database is of a different dialect other than Derby
     */
	private boolean doesTableExistInDb(String tableName) throws IllegalArgumentException, SQLException {

	    boolean tableExists = false;
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName parameter cannot be empty.");
        }

        String dialect = config.getProperty(DIALECT);
        
        if(dialect.equals(DERBY_DIALECT)) {
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            

            WorkResult<Boolean> work = new WorkResult<Boolean>(false) {
                
                @Override
                public void execute(Connection connection) throws SQLException {
                    
                    try{   
                        DatabaseMetaData dbm = connection.getMetaData();

                        
                         // check if table is there
                         ResultSet tables = dbm.getTables(null, null, tableName, new String[] {"TABLE"});
                         if (tables.next()) {
                             // Table exists - nothing to do
                             setResult(true);
                         }
                         
                         if(logger.isInfoEnabled()){
                             logger.info("doesTableExistInDb() for table: " + tableName + " returned " + getResult());
                         }

                    }catch(Exception e){
                        logger.error("Caught exception while trying to run SQL commands for table '"+tableName+"'.", e);
                        throw new SQLException(e);
                    }
                }
            };
            
            session.doWork(work);
            session.getTransaction().commit();
            tableExists = work.getResult();
            session.close();

            
        } else{
            throw new SQLException("Unable to run SQL commands for table '"+tableName+"' because unable to handle dialect of "+dialect+".");
        }
       
        return tableExists;
    }

	/**
	 * Generic class to allow a result object of type T to be passed back from a Hibernate Work object.
	 * The caller can pass in a default result object and then update the result in the execute() method.
	 * 
	 * @author nblomberg
	 *
	 * @param <T> Type of result object to return.
	 */
	private abstract class WorkResult<T> implements Work {
	    private T result;
	    
	    public WorkResult(T t) {
	        // Initialize the result;
	        setResult(t);
	    }
	    
        @Override
        abstract public void execute(Connection arg0) throws SQLException;

        /**
         * @return the resultType
         */
        public T getResult() {
            return result;
        }
        
        public void setResult(T result) {
            this.result = result;
        }
	    
	}
}
