/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.provider.certpath.SunCertPathBuilderException;
import sun.security.validator.ValidatorException;

/**
 * Utility for validating URI addresses that are used in code.
 * 
 * @author iapostolos
 */
public class UriUtil {

	private static final Logger logger = LoggerFactory.getLogger(UriUtil.class);
    
	/** The header key for X-Frame-Options. */
    private static final String HEADER_KEY_XOP = "X-Frame-Options";
    
    /** The deny header field for X-Frame-Options key. */
    private static final String HEADER_FIELD_DENY = "DENY";
    
    /** The same origin header field for X-Frame-Options key. */
    private static final String HEADER_FIELD_SAME_ORIGIN = "SAMEORIGIN";
    
    /** types of request methods to test for URL validation */
    private static final String[] methods = {"GET", "POST", "HEAD", "OPTIONS", "PUT"};
    
    /** used when an http Uri is being validated/checked in an https server instance */
    private static final String GOOGLE_SSL_PROXY_STR = "https://www.google.com/search?q=%http://yourhttpsite.com&btnI=Im+Feeling+Lucky";
    private static final String GOOGLE_SSL_PROXY_REPLACE_STR = "yourhttpsite.com";
    
    /** Connection Timeout */
    private static final int CONNECT_TIMEOUT = 5000;
    
    /**
     * Different types of Internet connection status
     * 
     * @author mhoffman
     *
     */
    public enum InternetConnectionStatusEnum {
        UNKNOWN, CONNECTED, NOT_CONNECTED
    }
	
    /**
     * Checks the media URI to see if the content will be blocked (if the page is served over HTTPS but the 
     * content requests HTTP) or if it violates the same origin policy by retrieving the header data.
     *
     * @param mediaUri The uri of the media to check.  This value can be modified by this method if, during 
     * validation/checking, there is a work around for an exception or SOP violation. The modified Uri should 
     * be used at that point when rendering to the user.
     * @return boolean Returns true if the uri will result in a mixed content error or if it violates the 
     * same origin policy.
     */
    public static boolean validateUriForSOPViolationOrBlockedContent(StringBuilder mediaUri){
        
        boolean urlViolatesSOP = false;
        boolean hasBlockedContent = false;
        String headerField;
        
        try {
            
            URL url = new URL(mediaUri.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);

            if (connection.getResponseCode() == 200) {
                             
                headerField = connection.getHeaderField(HEADER_KEY_XOP);    
                urlViolatesSOP = isBlockingOption(headerField);
                
                logger.info("The media uri " + mediaUri + " has a urlViolatesSOP value of : " + urlViolatesSOP);
                // Got a response back from the server, it is online
                //reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
                
                hasBlockedContent = CommonProperties.getInstance().shouldUseHttps() && !mediaUri.toString().startsWith(Constants.HTTPS);
                
            } else if(connection.getResponseCode() == 301 || connection.getResponseCode() == 302) {
            	// If the url is redirected, validate the redirect url to determine if it should open in a new window

            	mediaUri.setLength(0);
            	mediaUri.append(connection.getHeaderField("Location"));
            	return validateUriForSOPViolationOrBlockedContent(mediaUri);

            } else {
            	throw new Exception("Received a non-success (200) response of " + connection.getResponseCode() + " from the address of "+ mediaUri);
            }
            
        } catch (MalformedURLException me){
            logger.error("Caught Malformed URL Exception while validating the mediaItem URL of "+ mediaUri +".  "
                    + "The most common fix is to add a URL scheme (e.g. http://) to the beginning of the address.", me);
        } catch (SSLHandshakeException e){
            //can be thrown by getResponseCode 
            // example: url = "www.cnn.com" on https server
            
            if(e.getCause() != null && e.getCause() instanceof CertificateException){
                //the error seen in #2123
                //use google ssl proxy to see if the URL can be routed differently and pass the check
                logger.error("Caught exception while trying to check the mediaItem URL of '"+ mediaUri.toString() +"'.  Attempting the Google SSL proxy solution." , e);
                
                StringBuilder googleSSLMediaUri = new StringBuilder(mediaUri);
                setSSLProxyUrl(googleSSLMediaUri);
                
                urlViolatesSOP = validateUriForSOPViolationOrBlockedContent(googleSSLMediaUri);
                if(!urlViolatesSOP){
                    //that worked, update the provided Uri
                    mediaUri.setLength(0);
                    mediaUri.append(googleSSLMediaUri.toString());
                }else{
                    logger.error("The Google SSL proxy solution didn't work on the original media Uri of '"+ mediaUri +"'.  ("+googleSSLMediaUri+")");
                }
            }else{
                logger.error("Caught exception while trying to check the mediaItem URL of '"+ mediaUri +"'.  However this was not a Certificate Exception therefore NOT attempting the Google SSL proxy solution." , e);
            }
            
        } catch (Exception ex) {
            logger.error("Caught exception while trying to check the mediaItem URL of "+ mediaUri , ex);
        }
        
        return urlViolatesSOP || hasBlockedContent;
    }
        
    /**
     * Replaces the url with a google ssl proxy.
     * 
     * @param mediaUri The string builder containing the url to replace.
     */
    private static void setSSLProxyUrl(StringBuilder mediaUri) {
    	String  url = mediaUri.toString();
    	url = StringUtils.replace(url, Constants.HTTP, Constants.EMPTY);
    	mediaUri.setLength(0);
    	mediaUri.append(StringUtils.replace(GOOGLE_SSL_PROXY_STR, GOOGLE_SSL_PROXY_REPLACE_STR, url));
    }
    
    private static boolean isBlockingOption(String headerField) {
        
        if (headerField != null && (headerField.equalsIgnoreCase(HEADER_FIELD_DENY) 
                || headerField.equalsIgnoreCase(HEADER_FIELD_SAME_ORIGIN))) {
            return true;
        } else {
        	logger.info("The header field " + headerField + " does not exist in the http response.");
        }
        
        return false;
    }
    
    /**
     * Validates a URI address to verify that the resource it references can be accessed without
     * actually contacting the URI client. This validation logic does NOT guarantee that the client
     * can be reached. Use this validation to only validation the text of a URI and not the
     * connectivity. In order to test for connectivity, use
     * {@link #validateUri(String, AbstractFolderProxy, InternetConnectionStatusEnum)}.
     * 
     * @param uri The URI address to be validated. Can't be null.
     * @param courseDirectory the course directory to use for course content references (e.g. DKF
     *            path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @return String If the URI address was corrected during validation, the corrected URI address
     *         that should replace the original. Otherwise, null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL
     *             conventions. If the URI is null.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by
     *             the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address
     *             yields an invalid response code.
     * @throws Exception If the URI address does not reference a valid local file or web resource.
     */
    public static String validateUriNoPing(String uri, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) 
            throws MalformedURLException, IOException, ConnectException, Exception{
        return validateUri(uri, courseDirectory, connectionStatus, false);
    }
    
    /**
     * Validates a URI address to verify that the resource it references can be accessed. This tests
     * the text of the URI as well as the connectivity (the client will receive a 'ping' to ensure
     * it can be reached).
     * 
     * @param uri The URI address to be validated. Can't be null.
     * @param courseDirectory the course directory to use for course content references (e.g. DKF
     *            path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @return String If the URI address was corrected during validation, the corrected URI address
     *         that should replace the original. Otherwise, null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL
     *             conventions. If the URI is null.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by
     *             the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address
     *             yields an invalid response code.
     * @throws Exception If the URI address does not reference a valid local file or web resource.
     */
    public static String validateUri(String uri, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) 
            throws MalformedURLException, IOException, ConnectException, Exception{
        return validateUri(uri, courseDirectory, connectionStatus, true);
    }
    
    /**
     * Validates a URI address to verify that the resource it references can be accessed.
     * 
     * @param uri The URI address to be validated. Can't be null.
     * @param courseDirectory the course directory to use for course content references (e.g. DKF
     *            path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @param allowPing true to 'ping' the URI client to test connectivity and ensure it can be
     *            reached; false to skip over connectivity validation and only check the URI text.
     * @return String If the URI address was corrected during validation, the corrected URI address
     *         that should replace the original. Otherwise, null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL
     *             conventions. If the URI is null.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by
     *             the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address
     *             yields an invalid response code.
     * @throws Exception If the URI address does not reference a valid local file or web resource.
     */
    private static String validateUri(String uri, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus, boolean allowPing) 
            throws MalformedURLException, IOException, ConnectException, Exception{
        
        if(courseDirectory == null){
            throw new IllegalArgumentException("The domain directory can't be null.");
        }else if(uri == null){
            throw new MalformedURLException("A null URI is not valid.");
        }

        if(logger.isInfoEnabled()){
            logger.info("Validating URI of "+uri+".  The course folder to use for file validation is "+courseDirectory+".");
        }
            
        try{
            if(courseDirectory.fileExists(uri)){
                // if the URI associated with this media addresses an existing local file, the URI is valid
                return null; 
            }
            
        }catch(@SuppressWarnings("unused") IOException e){
           //not a file, continue analysis
           // logger.trace("Caught exception while trying to check if the uri "+uri+" is a file.", e);
        }
        
        //
        // otherwise, validate the URI associated with this media as a web address URL
        //              
        
        // remove leading or trailing whitespace from the URL in case a URL scheme needs to be added
        uri = uri.trim();
        
        // determine whether the URL does not begin with a valid URL scheme            
        boolean hasValidUrlScheme = hasValidURLScheme(uri);
        
        if(!hasValidUrlScheme){
            
            // if the URL needs a valid URL scheme added to it, try to validate it using each of the known schemes
            String alternativeURL = null;
            for(String scheme : Constants.VALID_URL_SCHEMES){
            
                try{
                   alternativeURL = validateUri(scheme + uri, courseDirectory, connectionStatus, allowPing);
                   if(alternativeURL != null){
                       // if a scheme was found that made the URL valid, return the corrected URL
                       return alternativeURL;
                   } else {
					   // if a scheme was found that made the URL valid, return the corrected URL
                	   return scheme + uri;
                   }
                } catch (@SuppressWarnings("unused") Exception e){                      
                    // otherwise, move on to the next scheme
                }
            }
        }
        
        if (!allowPing) {
            // URI has valid scheme and we don't want to test the connection, so we have gone as far
            // as we can; return the URI.
            return uri;
        } else {
            return isURLReachable(uri, connectionStatus);          
        }
    }
    
    /**
     * Validates a URL address to verify that the resource it references can be accessed.
     * 
     * @param url The URL address to be validated. Can't be null.
     * @param connectionStatus used to indicate whether this computer has an Internet connection.  
     * Use null or InternetConnectionStatusEnum.UNKNOWN as the default.
     * @return String If the URL address was corrected during validation, the corrected URL address
     *         that should replace the original. Otherwise, null.
     * @throws ConnectException If a connection to a web resource specified by the URL address
     *             yields an invalid response code.
     * @throws Exception If the URL address does not reference a valid local file or web resource.
     */
    public static String isURLReachable(String url, InternetConnectionStatusEnum connectionStatus) throws java.net.ConnectException, Exception{
        
        // Determine if Internet connection is active
        if (connectionStatus == InternetConnectionStatusEnum.NOT_CONNECTED
                || (connectionStatus == InternetConnectionStatusEnum.UNKNOWN && !UriUtil.isInternetReachable())) {
            connectionStatus = InternetConnectionStatusEnum.NOT_CONNECTED;
            throw new java.net.ConnectException(
                    "It appears you don't have an active internet connection, therefore " + url + " can't be validated.");
        }

        // try each of the request methods to see if any are valid for the specific URI
        int responseCode;
        StringBuffer errorBuffer = new StringBuffer();
        boolean isHTTPSURL = url.startsWith(Constants.HTTPS);
        URL alternativeURLValidated = null;
        for (int index = 0; index < methods.length; index++) {

            // reset
            responseCode = -1;

            // At this point the URI is formatted for a connection test,
            // try connecting to the web resource that the URL references
            URL alternativeURL = null;
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) (new URL(url)).openConnection();
            } catch (MalformedURLException malformedURLException) {
                // handles the case when the URI is suppose to be an existing file in the course
                // folder and should never reach this part of this method but rather 'return
                // null' earlier in this method.
                throw new Exception(
                        "The URL of '" + url
                                + "' is not a valid URL.  This is not the validation logic to call if this URL references a file in the GIFT course folder.",
                        malformedURLException);
            }

            // The user agent property is needed to prevent some websites from returning a
            // response of 403 instead of 200
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod(methods[index]);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            try {
                connection.connect();
                responseCode = connection.getResponseCode(); // this can throw an exception

            } catch (Exception e) {

                errorBuffer.append("\n{").append(url).append(":'").append(methods[index]).append("' caused '").append(e.getMessage()).append("'}");

                connection.disconnect();

                try {

                    if (isHTTPSURL) {
                        
                        if(e instanceof ValidatorException || e.getCause() instanceof ValidatorException ||
                                e instanceof SunCertPathBuilderException || e.getCause() instanceof SunCertPathBuilderException){
                            // ignoring security certificate exceptions so GIFT doesn't have to worry about adding certificates to the java key store
                            if(logger.isDebugEnabled()){
                                logger.debug("Ignoring security certificate exception when pinging URL of "+url);
                            }
                            return null;
                        }else{

                            // if the URL has an "https://" scheme and the connection fails, try an
                            // "http://" scheme
                            alternativeURL = new URL(url.replaceFirst(Constants.HTTPS, Constants.HTTP));
                            connection = (HttpURLConnection) (alternativeURL).openConnection();
                            connection.setRequestMethod(methods[index]);
                            connection.connect();
                        }

                    } else if (url.startsWith(Constants.HTTP)) {

                        // if the URL has an "http://" scheme and the connection fails, try an
                        // "https://" scheme
                        alternativeURL = new URL(url.replaceFirst(Constants.HTTP, Constants.HTTPS));
                        connection = (HttpURLConnection) (alternativeURL).openConnection();
                        connection.setRequestMethod(methods[index]);
                        connection.connect();
                    }

                    responseCode = connection.getResponseCode(); // this can throw an exception

                } catch (Exception exception) {
                    // it was worth a try but since changing the prefix didn't work, continue
                    // on...
                    errorBuffer.append("\n{").append(alternativeURL).append(":'").append(methods[index]).append("' caused '")
                            .append(exception.getMessage()).append("'}");
                }
            }finally{
                connection.disconnect();
            }

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                
                if(alternativeURL != null){
                    
                    if(alternativeURLValidated == null){
                        //an alternative URL passed the check and an previous alternative URL has not been set,
                        //store the alternative URL in case other checks on the provided URL to this method also fail
                        if(logger.isDebugEnabled()){
                            logger.debug("The URL provided '"+url+"' didn't pass the ping test for '"+methods[index]+"' but the alternative URL of '"+
                                    alternativeURL.toString()+"' did. Saving this alternative URL as a possible replacement.  Ping fail reason:\n"+errorBuffer.toString());
                        }
                        alternativeURLValidated = alternativeURL;
                        continue;
                    }else{
                        //a previous alternative URL has passed validation, no need to save another one
                        continue;
                    }
                }else{
                    //the provided URL worked for the current method type (ignore any previous alternative URLs)
                    if(logger.isDebugEnabled()){
                        logger.debug("URL '"+url+"' passed PING test using "+methods[index]);
                    }
                    return null;
                }
                
//                if(alternativeURL != null && logger.isDebugEnabled()){
//                    //this is mainly for when deployed to servers with HTTPS and its hard to debug the reasons why a URL might be changed
//                    logger.debug("Changing the URL from '"+url+"' to '"+alternativeURL.toString()+"' because\n"+errorBuffer.toString());
//                }
//
//                // if the web resource responded with a valid HTTP status code, the URL is valid
//                // reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
//                return alternativeURL == null ? null : alternativeURL.toString();
            }else{

                errorBuffer.append("\n{").append(url).append(":'").append(methods[index]).append("' bad response code of '").append(responseCode)
                        .append("'}");
            }

        } // end for
        
        if(alternativeURLValidated != null){
            if(logger.isDebugEnabled()){
                logger.debug("Using the alternative URL of '"+alternativeURLValidated+"' for '"+url+"'.  Reason:\n"+errorBuffer.toString());
            }
            return alternativeURLValidated.toString();
        }
        
        // otherwise, throw an exception indicating that an invalid response code was received
        throw new java.net.ConnectException("Received a non-success (" + HttpURLConnection.HTTP_OK + ") and non-redirection ("
                + HttpURLConnection.HTTP_MOVED_PERM + ", " + HttpURLConnection.HTTP_MOVED_TEMP + ", " + HttpURLConnection.HTTP_SEE_OTHER
                + ") response " + "from URL of " + url + ".  The error buffer reads: " + errorBuffer.toString() + ".");
    }
    
    /**
     * Returns a new URI from the string provided.  The string will be encoded to be
     * URI compliant.
     * 
     * @param rawStr the string to encode and create a URI from
     * @return a new URI from the string
     * @throws URISyntaxException if there was a problem creating the URI
     */
    public static URI fromRawString(String rawStr) throws URISyntaxException{        
        return new URI(makeURICompliant(rawStr));
    }
    
    /**
     * Returns a URI compliant string from the provided string by encoding the following
     * characters: <br>
     * '\' -> / <br> 
     * ' ' -> %20 <br> 
     * '+' -> %2B <br>
     * '&' -> %26 <br>
     * '[' -> %5B <br>
     * ']' -> %5D <br>
     * '`' -> %60 <br>
     * '^' -> %5E <br>
     * '{' -> %7B <br>
     * '}' -> %7D
     * 
     * @param rawStr the string to encode
     * @return the encoded string
     */
    public static String makeURICompliant(String rawStr){        
        return rawStr.trim().replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH)
                .replace(Constants.SPACE, Constants.ENCODED_SPACE)
                .replace(Constants.PLUS, Constants.ENCODED_PLUS)
                .replace(Constants.AND, Constants.ENCODED_AND)
                .replace(Constants.OPEN_SQUARE_BRACKET, Constants.ENCODED_OPEN_SQUARE_BRACKET)
                .replace(Constants.CLOSE_SQUARE_BRACKET, Constants.ENCODED_CLOSE_SQUARE_BRACKET)
                .replace(Constants.BACK_QUOTE, Constants.ENCODED_BACK_QUOTE)
                .replace(Constants.CARET, Constants.ENCODED_CARET)
                .replace(Constants.OPEN_CURLY_BRACKET, Constants.ENCODED_OPEN_CURLY_BRACKET)
                .replace(Constants.CLOSE_CURLY_BRACKET, Constants.ENCODED_CLOSE_CURLY_BRACKET);
    }
    
    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     * 
     * @param html the html string to escape
     * @return the escaped string
     */
    public static String escapeHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll(Constants.AND, Constants.HTML_ENCODED_AND).replaceAll(Constants.LESS_THAN, Constants.HTML_ENCODED_LESS_THAN)
                .replaceAll(Constants.GREATER_THAN, Constants.HTML_ENCODED_GREATER_THAN);
    }
    
    /**
     * Return whether the URI starts with the appropriate prefix.
     * 
     * currently supported prefixes are:
     * http://, https://
     * 
     * @param uri the URI to check
     * @return true iff the URI is valid
     */
    public static boolean hasValidURLScheme(String uri){
        
        boolean hasValidUrlScheme = false;
        
        // determine whether the URL does not begin with a valid URL scheme
        for(int i=0; i < Constants.VALID_URL_SCHEMES.length; i++){
            
            if(uri.startsWith(Constants.VALID_URL_SCHEMES[i])){
                hasValidUrlScheme = true;
                break;
            }
        }
        
        if(!hasValidUrlScheme){
            logger.info("The URI of "+uri+" is NOT a valid URL when checked against these: "+Constants.VALID_URL_SCHEMES+".");
        }
        
        return hasValidUrlScheme;
    }
    
    /**
     * Checks whether the given media object is a web address.
     * From mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil.java.
     * 
     * @param url the URL of a website to check
     * @return true if the URL contains the appropriate characters for GIFT to identify the string as a website
     * versus a local webpage.
     */
    public static boolean isWebAddress(String url){
        return (url != null && (url.contains("://") || url.contains("www.")));
    }
    
    /**
     * Return the enumerated status of whether the caller can reach the Internet.
     * 
     * @return InternetConnectionStatusEnum
     */
    public static InternetConnectionStatusEnum getInternetStatus(){        
        return isInternetReachable() ? InternetConnectionStatusEnum.CONNECTED : InternetConnectionStatusEnum.NOT_CONNECTED;
    }
    
    /**
     * Returns whether or not the website www.google.com is reachable via an HTTP
     * connection. This is a good indicator of whether the computer running this JVM has
     * an Internet connection that Java can access.
     *  
     * @return boolean whether or not the Internet is reachable
     */
    public static boolean isInternetReachable() {
        
        try {
            //make a URL to a known source
            URL url = new URL("http://www.google.com");

            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            urlConnect.getContent();

        } catch (@SuppressWarnings("unused") Throwable t) {              
            return false;
        }

        return true;
    }
    
    /**
     * Replaces backslashes in a URL with forward slashes.
     * 
     * @param url the URL with backslashes to replace
     * @return the new url
     */
    public String replaceURLBackslashes(String url) {
    	return url.replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);
    }
    
    public static void main(String[] args){
        
        StringBuilder url = new StringBuilder("https://www.cnn.com");
        UriUtil.validateUriForSOPViolationOrBlockedContent(url);
        
        System.out.println("Done");
    }
}
