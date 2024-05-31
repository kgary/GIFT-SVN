/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.rest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class provides logic to perform REST client requests to a REST server.
 *
 * @author mhoffman
 *
 */
public class RESTClient {

    /** timeouts for all interactions */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT       = 5000;

    private static final String ACCEPT_PROPERTY         = "Accept";
    private static final String ACCEPT_PROPERTY_VALUE   = "*/*";                    //other value(s): application/json+nxentity
    private static final String CONTENT_TYPE_PROPERTY   = "Content-Type";
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_BINARY = "application/octet-stream";
    private static final String CHAR_SET_PROPERTY = "charset";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final String LINE_FEED = "\r\n";
    private static final String MULTIPART_BOUNDARY = "===GIFT===";
    private static final String MULTIPART_BOUNDARY_DASHES = "--";
    private static final String UTF_8 = "UTF-8";
    private static final char AND = '&';
    private static final char EQUALS = '=';

    private static final String HTTPS = "https";
    private static final String SECURE_PROTOCOL = "TLSv1.2";

    /**
     * A trust all manager
     */
    private static final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers(){
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType){
        }

        @Override
        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType){
        }
    } };

    //HTTP Methods
    private static final String GET     = "GET";
    private static final String POST    = "POST";
    private static final String PUT     = "PUT";
    private static final String DELETE    = "DELETE";

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    /**
     * the content type request property to set on the connection
     * (e.g. "application/json,text/plain")
     */
    private String contentType = "";

    /**
     * Add a token to the content type request property for this connection.
     *
     * @param contentType can't be null.
     */
    public void addContentTypeProperty(String contentType){

        if(contentType == null){
            throw new IllegalArgumentException("The content type can't be null.");
        }

        contentType += contentType + ",";
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource
     * referenced by this URLConnection. If the timeout expires before the connection can be established, a java.net.SocketTimeoutException
     * is raised. A timeout of zero is interpreted as an infinite timeout. Some non-standard implementation of this method
     * may ignore the specified timeout. To see the connect timeout set, please call getConnectTimeout().
     *
     * @param timeoutMs a non-negative value
     */
    public void setConnectionTimeout(int timeoutMs){

        if(timeoutMs < 0){
            throw new IllegalArgumentException("The connection timeout can't be negative");
        }

        this.connectionTimeout = timeoutMs;
    }

    public int getConnectionTimeout(){
        return this.connectionTimeout;
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout
     * when reading from Input stream when a connection is established to a resource. If the timeout expires
     * before there is data available for read, a java.net.SocketTimeoutException is raised. A timeout of zero
     * is interpreted as an infinite timeout. Some non-standard implementation of this method ignores the
     * specified timeout. To see the read timeout set, please call getReadTimeout().
     *
     * @param timeoutMs a non-negative value
     */
    public void setReadTimeout(int timeoutMs){

        if(timeoutMs < 0){
            throw new IllegalArgumentException("The read timeout can't be negative");
        }

        this.readTimeout = timeoutMs;
    }

    public int getReadTimeout(){
        return this.readTimeout;
    }

    /**
     * Create a connection to the specified URL
     *
     * @param url URL to create connection
     * @param requestMethod request method (GET/POST/PUT)
     * @return the connection instance
     * @throws IOException if there was a problem creating the connection instance
     */
    private HttpURLConnection getConnection(URL url, String requestMethod) throws IOException{

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, contentType);
        connection.setRequestProperty(ACCEPT_PROPERTY, ACCEPT_PROPERTY_VALUE);
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod(requestMethod);

        return connection;
    }

    /**
     * Connect to the URL and then read the response from the server.
     *
     * @param connection to use for the rest call
     * @return the response from the server (if any).  Can be empty but not null.
     * @throws IOException if there was a problem with the connection or reading the response.
     */
    private byte[] callRequest(HttpURLConnection connection) throws IOException{

        byte[] returnValue = null;
        try{

          try(InputStream in = new BufferedInputStream(connection.getInputStream())){

              ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
              byte[] inBuf = new byte[1024];
              int nRead;
              while ((nRead = in.read(inBuf)) > 0) {
                  outBuf.write(inBuf, 0, nRead);
              }
              outBuf.flush();

              returnValue = outBuf.toByteArray();
          }

        }finally{

            if(connection != null){
                connection.disconnect();
            }

        }

        return returnValue;
    }

    /**
     * Calls the HTTP POST method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @return the response to the POST request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] post(URL url) throws IOException {
        HttpURLConnection connection = getConnection(url, POST);
        return callRequest(connection);
    }



    /**
     * Calls the HTTP PUT method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @return the response to the POST request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] put(URL url) throws IOException {
        HttpURLConnection connection = getConnection(url, PUT);
        return callRequest(connection);
    }

    /**
     * Calls the HTTP PUT method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @param params, map of  parameters to send in the post request (if not sending parameters pass null)
     * key: name of the parameter/variable
     * value: value of the parameter
     * @return the response to the POST request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] put(URL url, Map<String, String> params) throws IOException {
        HttpURLConnection connection = getConnection(url, PUT);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty( CONTENT_TYPE_PROPERTY, "application/x-www-form-urlencoded");
        connection.setRequestProperty( CHAR_SET_PROPERTY, UTF_8);

        if(HTTPS.equals(url.getProtocol())){
            try{
                setHostnameVerifier(connection);
            }catch(Exception e){
                throw new IOException("Failed to set the hostname verifier for the HTTPS connection to "+url, e);
            }
        }

        OutputStream outputStream = connection.getOutputStream();
        addParametersToPostRequest(outputStream, params);

        return callRequest(connection);
    }


    /**
     * Calls the HTTP DELETE method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @return the response to the POST request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] delete(URL url) throws IOException {
        HttpURLConnection connection = getConnection(url, DELETE);
        return callRequest(connection);
    }

    /**
     * Calls the HTTP PUT method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @param params, map of  parameters to send in the post request (if not sending parameters pass null)
     * key: name of the parameter/variable
     * value: value of the parameter
     * @return the response to the POST request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] delete(URL url, Map<String, String> params) throws IOException {
        HttpURLConnection connection = getConnection(url, DELETE);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty( CONTENT_TYPE_PROPERTY, "application/x-www-form-urlencoded");
        connection.setRequestProperty( CHAR_SET_PROPERTY, UTF_8);

        if(HTTPS.equals(url.getProtocol())){
            try{
                setHostnameVerifier(connection);
            }catch(Exception e){
                throw new IOException("Failed to set the hostname verifier for the HTTPS connection to "+url, e);
            }
        }

        OutputStream outputStream = connection.getOutputStream();
        addParametersToPostRequest(outputStream, params);

        return callRequest(connection);
    }


    /**
     * Calls the HTTP POST method on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @param params map of parameters to send in the post request (if not
     *        sending parameters pass null) key: name of the parameter/variable
     *        value: value of the parameter
     * @return the response to the POST request. Will be null if the response
     *         was empty.
     * @throws IOException if there was a problem connected to the URL or
     *         retrieving the response (if any).
     */
    public byte[] post(URL url, Map<String, String> params) throws IOException {
        HttpURLConnection connection = getConnection(url, POST);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, CONTENT_TYPE_PROPERTY_VALUE);

        if(HTTPS.equals(url.getProtocol())){
            try{
                setHostnameVerifier(connection);
            }catch(Exception e){
                throw new IOException("Failed to set the hostname verifier for the HTTPS connection to "+url, e);
            }
        }

        OutputStream outputStream = connection.getOutputStream();
        addParametersToPostRequest(outputStream, params);

        return callRequest(connection);
    }

    /**
     * Sends a POST request to a provided {@link URL} which uses a provided byte
     * array as the body of the request.
     *
     * @param url The {@link URL} to which to send the request. Can't be null.
     * @param body The binary data to send via the body.
     * @return The response to the request as a binary array.
     * @throws IOException if there was a problem sending the data.
     */
    public byte[] post(URL url, byte[] body) throws IOException {
        HttpURLConnection connection = getConnection(url, POST);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, CONTENT_TYPE_BINARY);

        if (HTTPS.equals(url.getProtocol())) {
            try {
                setHostnameVerifier(connection);
            } catch (Exception e) {
                throw new IOException("Failed to set the hostname verifier for the HTTPS connection to " + url, e);
            }
        }

        connection.getOutputStream().write(body);

        return callRequest(connection);
    }

    /**
     * Set the host name verifier to a custom verifier that ignores certificate validation.
     *
     * @param connection the URL connection to set the custom host name verifier on, will only
     * be applied to HttpsURLConnection instances.
     * @throws Exception if there was a problem setting the host name verifier
     */
    private void setHostnameVerifier(HttpURLConnection connection) throws Exception{

        // override the default SSL factory just for this HTTPS connection.
        if (connection instanceof HttpsURLConnection){

            HttpsURLConnection conHttps = (HttpsURLConnection) connection;

            // Get a new SSL context
            SSLContext sc = SSLContext.getInstance(SECURE_PROTOCOL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            // Set our connection to use this SSL context, with the "Trust all" manager in place.
            conHttps.setSSLSocketFactory(sc.getSocketFactory());
            // Also force it to trust all hosts
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // and set the hostname verifier.
            conHttps.setHostnameVerifier(allHostsValid);
        }

    }

    /**
     * Calls the HTTP POST method with attached files on the URL specified.
     *
     * @param url the request URL to perform a HTTP POST request on
     * @param files map of files to post key: value used in the name attribute
     *        of the HTTP 'content-disposition' header field value: the file to
     *        read into the stream
     * @param params map of parameters to send in the post request (if not
     *        sending parameters pass null) key: name of the parameter/variable
     *        value: value of the parameter
     * @return the response to the POST request. Will be null if the response
     *         was empty.
     * @throws IOException if there was a problem connected to the URL or
     *         retrieving the response (if any).
     */
    public byte[] post(URL url, Map<String, File> files, Map<String, String> params) throws IOException {
        HttpURLConnection connection = getConnection(url, POST);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, "multipart/form-data; boundary=" + MULTIPART_BOUNDARY );

        if(HTTPS.equals(url.getProtocol())){
            try{
                setHostnameVerifier(connection);
            }catch(Exception e){
                throw new IOException("Failed to set the hostname verifier for the HTTPS connection to "+url, e);
            }
        }

        OutputStream outputStream = connection.getOutputStream();
        addFilesToPostRequest(outputStream, files, params);
        return callRequest(connection);
    }

    /**
     * Add files to post request
     *
     * @param outputStream output stream to write file contents to
     * @param files map of files to to add to post request
     * key: value used in the name attribute of the HTTP 'content-disposition' header field
     * value: the file to read into the stream
     * @param params, map of  parameters to send in the post request (if not sending parameters pass null)
     * key: name of the parameter/variable
     * value: value of the parameter
     * @throws IOException if there was a problem writing to the output stream.
     */
    private void addFilesToPostRequest(OutputStream outputStream, Map<String, File> files, Map<String, String> params) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, UTF_8), true);

        // Add part for each file
        for (Map.Entry<String, File> entry : files.entrySet()) {
            String fieldName = entry.getKey();
            File file = entry.getValue();
            writer.append(MULTIPART_BOUNDARY_DASHES).append(MULTIPART_BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(file.getName()).append("\"").append(LINE_FEED);
            writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
            writer.append(LINE_FEED);
            writer.flush();
        }

        if(params != null){
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String fieldName = entry.getKey();
                String value = entry.getValue();
                writer.append(MULTIPART_BOUNDARY_DASHES).append(MULTIPART_BOUNDARY).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.append(value);
                writer.flush();
            }
        }

        writer.append(LINE_FEED).flush();
        writer.append(MULTIPART_BOUNDARY_DASHES).append(MULTIPART_BOUNDARY).append(MULTIPART_BOUNDARY_DASHES).append(LINE_FEED);
        writer.flush();
        writer.close();
    }
    /**
     * Add files to post request
     *
     * @param outputStream output stream to write file contents to
     * @param params map of parameters and their values.  (null if not sending parameters)
     * key: name of the parameter being sent
     * value: the value of the parameter
     * @throws IOException if there was a problem writing to the output stream.
     */
    private void addParametersToPostRequest(OutputStream outputStream, Map<String, String> params) throws IOException {

        if(params == null){
            return;
        }

        // Add part for each file
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,String> param : params.entrySet()) {

            if (postData.length() != 0){
                postData.append(AND);
            }

            postData.append(URLEncoder.encode(param.getKey(), UTF_8));
            postData.append(EQUALS);
            postData.append(URLEncoder.encode(param.getValue(), UTF_8));
        }

        byte[] postDataBytes = postData.toString().getBytes(UTF_8);
        outputStream.write(postDataBytes);
    }

    /**
     * Calls the HTTP GET method on the URL specified.
     *
     * @param url the request URL to perform a HTTP GET request on
     * @return the response to the GET request.  Will be null if the response was empty.
     * @throws IOException if there was a problem connected to the URL or retrieving the response (if any).
     */
    public byte[] get(URL url) throws IOException{
        HttpURLConnection connection = getConnection(url, GET);
        return callRequest(connection);
    }
}
