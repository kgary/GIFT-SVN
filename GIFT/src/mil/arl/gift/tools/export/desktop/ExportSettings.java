/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

/**
 * Unique setting names used by the wizard
 * 
 * @author cdettmering
 */
public class ExportSettings {
	
	private static final String OUTPUT_FILE = "OUTPUT_FILE";
	private static final String DOMAIN_CONTENT = "DOMAIN_CONTENT";
	private static final String EXPORT_DOMAIN_CONTENT_ONLY = "EXPORT_DOMAIN_CONTENT_ONLY";
	private static final String EXPORT_USER_DATA = "EXPORT_USER_DATA";
	private static final String EXTERNAL_DOMAIN_RESOURCES = "EXTERNAL_DOMAIN_RESOURCES";
	
	/**
	 * Gets the string used to map the output directory under the settings.
	 * 
	 * @return String output directory key
	 */
	public static String getOutputFile() {
		return OUTPUT_FILE;
	}
	
	/**
	 * Gets the string used to map the domain content under the settings.
	 * 
	 * @return String domain content key
	 */
	public static String getDomainContent() {
		return DOMAIN_CONTENT;
	}
	
	/**
     * Gets the string used to map the export domain content only under the settings.
     * 
     * @return String export domain content only key
     */
    public static String getExportDomainContentOnly() {
        return EXPORT_DOMAIN_CONTENT_ONLY;
    }
    
    /**
     * Gets the string used to map the export User data under the settings.
     * 
     * @return String export user data key
     */
    public static String getExportUserData() {
    	return EXPORT_USER_DATA;
    }
    
    /**
     * Gets the string used to map the external domain resource files under the settings
     * 
     * @return String external domain resources key
     */
    public static String getExternalDomainResources() {
    	return EXTERNAL_DOMAIN_RESOURCES;
    }
}
