/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 * This class is responsible for generating a dynamic .zip file that is unique
 * to a specific domain session that is requesting the file using a unique token
 * passed in from Domain. The .zip file will be generated in
 * bin\war\remote\generated folder and will have a name that looks like:
 * 
 * loadGatewayDependenciesGenerated.zip
 * 
 * The template .zip file is created once a build is triggered and is placed
 * inside bin\war\remote\generated. Once created, the class adds in the respective
 * properties containing the Domain information to the .zip then returns that
 * .zip to the learner.
 * 
 * @author cpolynice
 *
 */
public class RemoteZipGenerator {
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(RemoteZipGenerator.class);

    /** String paths that point to zip generation paths */
    private static final String REMOTE_PATH = "remote";
    private static final String WAR_PATH = "bin" + File.separator + "war";
    private static final String REMOTE_DEPENDENCIES_PATH = "config" + File.separator + "gateway";
    private static final String GATEWAY_PROPERTIES_FILE = "gateway.remote.properties";
    private static final String ZIP_EXT = ".zip";
    private static final String REMOTE_GENERATED_PATH = WAR_PATH + File.separator + REMOTE_PATH + File.separator
            + "generated";
    private static final String GIFT_GATEWAY_FILE_NAME = "loadGatewayDependencies";
    private static final String GIFT_GATEWAY_ZIP = GIFT_GATEWAY_FILE_NAME + ZIP_EXT;

    /** instance of the domain.properties file to grab relevant properties */
    private static DomainModuleProperties domainProperties = DomainModuleProperties.getInstance();

    /** url for connecting Gateway module to activemq */ 
    private static String ACTIVEMQ_URL = domainProperties.getBrokerURL();

    /** property keys the remote Gateway zip will use to connect to Domain module */
    private static String ACTIVEMQ_KEY = "ActiveMQURL";
    private static String CLIENT_ID_KEY = "ClientId";

    /** The destination of the generated zip file. Note that this location is used once
     * the root zip is copied from bin\war\remote\generated and the gateway.remote.properties
     * file's properties are modified to include Domain connection information. */
    private File zipDest;

    /** The unique token that will store the Domain session's client ID */
    private String uniqueToken = "";

    /** Default constructor - created in case zip has already been generated and user
     *  only needs location */
    public RemoteZipGenerator() {

    }

    /**
     * Creates a new instance of the zip generator class with the token initialized
     * from Domain Module to deliver to client.
     * 
     * @param token the client ID from Domain that will be used to connect Gateway to
     *        Domain module on zip launch.
     */
    public RemoteZipGenerator(String token) {
        uniqueToken = token;
    }

    /**
     * Generate the .zip that will be used to launch Gateway remotely. The
     * generated .zip will be stored in GIFT/bin/war/jws/generated folder and
     * will have a name that looks like:
     * 
     * 
     * loadGatewayDependenciesGenerated.
     * 
     * The template zip file exists in
     * GIFT/bin/war/remote/loadGatewayDependencies.zip and this function will
     * extract contents of base zip, read in existing properties file, update
     * information, and add newly configured .properties file to zip.
     * 
     * @return boolean - true if the file was generated successfully. False if
     *         the file could not be generated properly.
     */
    public boolean generateZip() {

        boolean success = false;
        
        /* Generate the path to the source .zip file. */
        String srcZipName = REMOTE_GENERATED_PATH + File.separator + GIFT_GATEWAY_ZIP;

        /* Generate the path to the destination generated .zip file. */
        String destZipName = REMOTE_GENERATED_PATH + File.separator + GIFT_GATEWAY_FILE_NAME + uniqueToken + ZIP_EXT;

        zipDest = new File(destZipName);

        try {
            /* Get the path to the source config .properties and the generated
             * .properties file. */
            File srcPropFile = new File(REMOTE_DEPENDENCIES_PATH + File.separator + GATEWAY_PROPERTIES_FILE);
            FileOutputStream propFileStream = new FileOutputStream(
                    REMOTE_GENERATED_PATH + File.separator + GATEWAY_PROPERTIES_FILE);

            Properties remoteProperties = new Properties();

            /* Load in the gateway.remote.properties file and update ClientId
             * and ActiveMQ. */
            remoteProperties.load(new FileInputStream(srcPropFile));
            remoteProperties.setProperty(CLIENT_ID_KEY, uniqueToken);
            remoteProperties.setProperty(ACTIVEMQ_KEY, ACTIVEMQ_URL);

            /* Save updated properties to the generated
             * gateway.remote.properties file. */
            remoteProperties.store(propFileStream, null);
            propFileStream.close();

            /* Retrieve the generated gateway.remote.properties file. */
            File propFile = new File(REMOTE_GENERATED_PATH + File.separator + GATEWAY_PROPERTIES_FILE);

            /* Create the directory where the gateway.remote.properties file
             * will be copied to the .zip. */
            File destPropFile = new File(REMOTE_GENERATED_PATH + File.separator + "config");

            /* Copy the contents of the source .zip and add the folder
             * containing the gateway.remote.properties file to the .zip. */
            ZipUtils.copyZip(srcZipName, destZipName);
            FileUtils.copyFileToDirectory(propFile, destPropFile);
            ZipUtils.addFolder(destZipName, destPropFile);

            /* Clean-up operations. */
            propFile.delete();
            FileUtils.deleteDirectory(destPropFile);

            /* Operation finished successfully. */
            success = true;
        } catch (IOException e) {
            logger.error("IOException thrown when generating the remote .zip file: " + e);
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException thrown when generating the .zip file: " + e);
        } catch (Exception e) {
            logger.error("Unhandled Exception thrown when generating the .zip file: " + e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Zip file generation completed with success: " + success);
        }
       
        return success;
    }

    /**
     * Get the url of the file that was generated.
     * 
     * @return String - The url of the generated jnlp file.
     */
    public String getGeneratedFileUrl() {
        String destFileName = GIFT_GATEWAY_FILE_NAME + uniqueToken + ZIP_EXT;
        String generatedFileUrl = "remote" + File.separator + "generated" + File.separator + destFileName;
        return generatedFileUrl;
    }

    /**
     * Cleanup this class. This will delete the generated file if it exists.
     */
    public void cleanup() {

        if (zipDest != null) {
            zipDest.delete();
        }
    }

    /**
     * Main entry point. This is only used to test the .zip file generator
     * class.
     * 
     * @param args not used
     */
    public static void main(String[] args) {

        System.out.println("Starting .zip file generation.");

        String token = UUID.randomUUID().toString();
        RemoteZipGenerator zipGenerator = new RemoteZipGenerator(token);
        boolean success = zipGenerator.generateZip();

        System.out.println("Finished generating .zip file with token: " + token + ". Result was: " + success);
    }

}
