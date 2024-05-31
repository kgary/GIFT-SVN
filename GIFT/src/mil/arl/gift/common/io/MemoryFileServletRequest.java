/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.HashMap;
import java.util.Map;

/**
 * A request to be sent to a {@link mil.arl.gift.tools.dashboard.server.MemoryFileServlet MemoryFileServlet}. This object contains
 * the parameters needed to request resources from GIFT's content management system and is used to encode these parameters within
 * a URL pointing to a MemoryFileServlet so that the parameters can be correctly passed and decoded on the servlet.
 * <br/><br/>
 * Note that this request passes paramaters within the URL's path info directly instead of as query arguments so that they are
 * propagated within iframes as well. This means that accessing a web page through an iframe pointing to a MemoryFileServlet
 * using this request will pass all of the parameters used to get the web page to any other relative URLs used by that web page. 
 * Since most resources require permissions to be accessed within the CMS, this helps prevent 404 errors when accessing URLs
 * within a CMS-hosted web page, since any credentials used to access the web page will be passed to the URLs within it.
 * 
 * @author nroberts
 */
public class MemoryFileServletRequest{
	
	/** The key used to encode and decode the username parameter*/
	private static final String USERNAME = "username";
	
	/** The character used to start encoding parameters */
	private static final String ENCODE_START = "[";
	
	/** The character used to assign parameter key-value pairs */
	private static final String ASSIGN = "=";
	
	/** The character used to delimit parameters */
	private static final String DELIM = ",";
	
	/** The character used to finish encoding parameters */
	private static final String ENCODE_END = "]";
	
	/** The path to the CMS file resource being requested */
	private String resourcePath;
	
	/** The parameters to pass along to the servlet as part of the request */
	private Map<String, String> servletParams = new HashMap<String, String>();
	
	/**
	 * Creates a new request requesting the CMS file resource at the given path
	 * 
	 * @param resourcePath the path to the CMS file resource
	 */
	private MemoryFileServletRequest(String resourcePath){
		
		if(resourcePath == null || resourcePath.isEmpty()){
			throw new IllegalArgumentException("The resource path cannot be null or empty.");
		}
		
		this.resourcePath = resourcePath;
		
		if(this.resourcePath.startsWith(Constants.FORWARD_SLASH)){
			
			//remove any forward slashes preceding the resource path, since the encode logic already adds one
			this.resourcePath = this.resourcePath.replaceFirst(Constants.FORWARD_SLASH, "");
		}
	}
	
	/**
	 * Creates a new request requesting the CMS file resource at the given path for the user with the given username
	 * 
	 * @param resourcePath the path to the CMS file resource
	 * @param username the username of the user attempting to request the resource
	 */
	public MemoryFileServletRequest(String resourcePath, String username){
		this(resourcePath);
		this.servletParams.put(USERNAME, username);
	}
	
	/**
	 * Creates a new request requesting the CMS file resource at the given path with the given parameters to pass to the servlet
	 * 
	 * @param resourcePath the path to the CMS file resource
	 * @param servletParams the parameters to pass to the servlet
	 */
	public MemoryFileServletRequest(String resourcePath, Map<String, String> servletParams){
		this(resourcePath);
		
		if(servletParams != null){
			this.servletParams.putAll(servletParams);
		}
	}
	
	/**
	 * Gets the path to the CMS file resource being requested
	 * 
	 * @return the path to the resource
	 */
	public String getResourcePath(){
		return resourcePath;
	}
	
	/**
	 * Gets the username of the user making this request
	 * 
	 * @return the username
	 */
	public String getUsername(){
		return servletParams.get(USERNAME);
	}
	
	/**
	 * Gets the parameters this request is passing to the servlet
	 * 
	 * @return the servlet parameters
	 */
	public Map<String, String> getServletParams(){
		return servletParams;
	}
	
	/**
	 * Encodes the given request so that it can be passed to the servlet as part of the path info in the URL used to send the request
	 * 
	 * @param request the request to encode
	 * @return the encoded request
	 */
	public static String encode(MemoryFileServletRequest request){
		
		StringBuilder sb = new StringBuilder();
		
		if(request.getServletParams() != null && !request.getServletParams().isEmpty()){
			
			sb.append(Constants.FORWARD_SLASH)
			  .append(ENCODE_START);
			
			boolean isFirst = true;
			
			for(String param : request.getServletParams().keySet()){
				
				if(!isFirst){
					sb.append(DELIM);
					
				} else {
					isFirst = !isFirst;
				}
				
				sb.append(param)
				  .append(ASSIGN)
				  .append(request.getServletParams().get(param));
			}
			
			sb.append(ENCODE_END);
		}
		
		sb.append(Constants.FORWARD_SLASH)
		  .append(request.getResourcePath());
		
		return sb.toString();
	}
	
	/**
	 * Decodes the given URL path info string into a request to be processed by the servlet
	 * 
	 * @param encodedString a URL path info string
	 * @return the decoded request
	 */
	public static MemoryFileServletRequest decode(String encodedString) {
		
		String encodePrefix = Constants.FORWARD_SLASH + ENCODE_START;
		
		int paramStartIndex = encodedString.indexOf(encodePrefix);
		int paramEndIndex = encodedString.substring(paramStartIndex).indexOf(ENCODE_END);

		String paramsString = encodedString.substring(paramStartIndex + encodePrefix.length(), paramEndIndex);
		String resourcePath = encodedString.substring(0, paramStartIndex) + encodedString.substring(paramEndIndex + ENCODE_END.length());

		String[] params = paramsString.split(DELIM);
		Map<String, String> servletParams = new HashMap<String, String>();

		for (String param : params) {
			
			String[] args = param.split(ASSIGN);

			if (args.length == 2) {
				
				//each parameter should only have 1 value
				servletParams.put(args[0], args[1]);
			}
		}

		return new MemoryFileServletRequest(resourcePath, servletParams);
	}
}