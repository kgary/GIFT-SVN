/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

/**
 * A class holding GAT system properties shared across the client, server, and shared packages
 * 
 * @author nroberts
 */
public class SharedGatSystemProperties {

	/** The URL of the servlet used to handle uploading course content files */
	public static final String COURSE_RESOURCE_UPLOAD_URL = "courseResources/"; 
	
	/** URL of the servlet handling files uploaded for importing. This is configurable in src/mil/arl/gift/tools/gat/war/WEB-INF/web.xml */
    public static final String IMPORT_SERVLET_URL = "import/";
}
