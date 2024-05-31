/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.Constants;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The File servlet for serving from a relative path.
 *
 * @author BalusC
 * {@link "http://balusc.blogspot.com/2007/07/fileservlet.html"} 
 */
public class FileServlet extends HttpServlet {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FileServlet.class);

    // Constants ----------------------------------------------------------------------------------
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    private static final int MAX_UPLOAD_BYTES = 1000000000; // 1 GB
    
    private static final String FILE_PATH_PROPERTY = "File_Path";
    
    /* Setting the content disposition to "attachment" allows the client to download 
     * files that would normally open in the browser, such as .txt and .log files, 
     * but prevents the Media Semantic Avatar from being displayed inline. Therefore
     * the default value is "inline" */
    private static final String CONTENT_DISPOSITION_PROPERTY = "Content_Disposition";
    
    private static final String ENABLE_FILE_UPLOAD = "Enable_File_Upload";
    
    private static final long serialVersionUID = 1L;

    // Properties ---------------------------------------------------------------------------------
    private String filePath;
    
    private String contentDisposition = "inline";
    
    private boolean canUploadFile = false;

    // Actions ------------------------------------------------------------------------------------
    @Override
    public void init() throws ServletException {
    	
    	CommonProperties.getInstance();

        // Get the file path to serve files from an initialization paramter of 
        // the servlet
        String filePath = getServletConfig().getInitParameter(FILE_PATH_PROPERTY);
        
        if (filePath != null) {
            this.filePath = filePath;
            File file = new File(filePath);
            logger.info("Initializing file server in " + file.getAbsolutePath());
        } else {
            throw new ServletException("The '" + FILE_PATH_PROPERTY + "' was not defined as an init-property");
        }
        
        String canUploadFile = getServletConfig().getInitParameter(ENABLE_FILE_UPLOAD);
        if (canUploadFile != null) {
            this.canUploadFile = Boolean.parseBoolean(canUploadFile);
            if(this.canUploadFile) {
                logger.info("Can now upload files using this servlet.");
            }
        }
        
        String contentDisposition = getServletConfig().getInitParameter(CONTENT_DISPOSITION_PROPERTY);
        if (contentDisposition != null) {        	
        	this.contentDisposition = contentDisposition;
        }        
        
        logger.info("Initializing content disposition to " + this.contentDisposition);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get requested file by path info.
        String requestedFile = request.getPathInfo();

        // Check if file is actually supplied to the request URI.
        if (requestedFile == null) {
            // Do your thing if the file is not supplied to the request URI.
            // Throw an exception, or send 404, or show default/warning page, or just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }

        // Decode the file name (might contain spaces and on) and prepare file object.
        File file = new File(filePath, URLDecoder.decode(requestedFile, "UTF-8"));
        logger.info("Accessing file at " + file.getAbsolutePath());

        // Check if file actually exists in filesystem.
        if (!file.exists()) {
            // Do your thing if the file appears to be non-existing.
            // Throw an exception, or send 404, or show default/warning page, or just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }

        // Get content type by filename.
        String contentType = getServletContext().getMimeType(file.getName());

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Init servlet response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setContentType(contentType);
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Access-Control-Allow-Origin", "*");
       
        response.setHeader("Content-Disposition", this.contentDisposition + "; filename=\"" + file.getName() + "\"");

        // Prepare streams.
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        
        try {
            // Open streams.
            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length = input.read(buffer);
            while (length > 0) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }
        } finally {
            // Gently close streams.
            close(output);
            close(input);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        if (canUploadFile) {
        	
        	String domainDirectory = CommonProperties.getInstance().getDomainDirectory();
        	
        	String domainRelativeServletPath = null;
        	
        	if(domainDirectory != null){
        		domainRelativeServletPath = FileFinderUtil.getRelativePath(new File(domainDirectory), new File(filePath));
        	}
            
            JSONObject jsonResponse = new JSONObject();
            
            //Note: using text/json to help with filenames with ampersand & in them causes
            //      the browser (tested with IE11) to present a download file confirmation dialog which
            //      is bad because GIFT is trying to upload a file here, not download.
            //Note: text/plain, text/html, text/xml cause ampersand to be encoded as &amp;
            resp.setContentType("text/plain");

            // process only multipart requests
            if (ServletFileUpload.isMultipartContent(req)) {
        	   
            	if(DeploymentModeEnum.SERVER == CommonProperties.getInstance().getDeploymentMode()){
            		
	        	    if(req.getContentLength() > MAX_UPLOAD_BYTES) {
	        		    // Limit the file size to 100 MB
	        		   
	        		    int maxSize = MAX_UPLOAD_BYTES / (1000 * 1000 * 1000);
	        		    logger.warn("Could not upload file because it exceeds the file size limit.");
	        		    throw new IOException("This file is too large to be uploaded. Please choose a smaller file (less than " + maxSize + " GB).");
	                }
            	}
        	   
                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                try {
                    List<FileItem> items = upload.parseRequest(req);
                    for (FileItem item : items) {
                        // process only file upload - discard other form item types
                        if (item.isFormField()) {
                            continue;
                        }
                        
                        String fileName = item.getName();
                        
                        if (fileName != null) {
                            fileName = FilenameUtils.getName(fileName);
                            
                            // create a unique path for this file to prevent errors caused by duplicate file names
                            String uuid = UUID.randomUUID().toString();
                            int attempts = 0;
                            
                            // try to generate a new uuid up to five times if it is not unique
                            while(new File(uuid).exists() && attempts < 5) {
                            	uuid = UUID.randomUUID().toString();
                            	attempts++;
                            }
                            
                            if(attempts == 5) {
                            	logger.warn("Made 5 attempts at generating a new UUID for file : " + fileName);
                            }
                             
                            if(new File(filePath + Constants.FORWARD_SLASH + uuid).mkdirs()){
                            	fileName = uuid + Constants.FORWARD_SLASH + fileName;
                            } else {
                            	logger.warn("Unable to create unique directory for " + fileName);
                            }
                        }
                        
                        File uploadedFile = new File(filePath, fileName);
                        if (uploadedFile.createNewFile()) {
                            item.write(uploadedFile);
                            resp.setStatus(HttpServletResponse.SC_CREATED);
                            this.getServletContext().getContextPath();

                            jsonResponse.put(Constants.FILE_SERVLET_RESULT_KEY, Constants.FILE_SERVLET_RESULT_SUCCESS);
                            jsonResponse.put(Constants.FILE_SERVLET_FILE_KEY, fileName);
                            
                            if(domainRelativeServletPath != null){
                            	jsonResponse.put(Constants.FILE_SERVLET_SERVLET_PATH, domainRelativeServletPath);
                            }                            

                            logger.info("Successfully uploaded file '" + uploadedFile.getAbsolutePath() + "'.\nPost Response is: "+jsonResponse.toJSONString());

                            resp.getWriter().print(jsonResponse.toJSONString());
                            resp.flushBuffer();
                            
                        } else {
                            logger.warn("Could not upload file, the file already exists in the directory.");
                            throw new IOException("The file already exists on the server.");
                        }
                    }
                } catch (Exception e) {

                    logger.warn("Could not upload file, an error occurred while creating the file : " + e.getMessage());

                    jsonResponse.put(Constants.FILE_SERVLET_RESULT_KEY, Constants.FILE_SERVLET_RESULT_FAILURE);
                    jsonResponse.put(Constants.FILE_SERVLET_ERROR, Constants.FILE_SERVLET_FILE_ALREADY_EXISTS);
                    jsonResponse.put(Constants.FILE_SERVLET_MESSAGE, "An error occurred while creating the file : " + e.getMessage());

                    resp.getWriter().print(jsonResponse.toJSONString());
                    resp.flushBuffer();
                }

            } else {

                logger.warn("Could not upload file, request contents type is not supported by the servlet.");

                jsonResponse.put(Constants.FILE_SERVLET_RESULT_KEY, Constants.FILE_SERVLET_RESULT_FAILURE);
                jsonResponse.put(Constants.FILE_SERVLET_ERROR, Constants.FILE_SERVLET_FILE_UPLOAD_FAILURE);
                jsonResponse.put(Constants.FILE_SERVLET_MESSAGE, "Request contents type is not supported by the servlet.");

                resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        jsonResponse.toJSONString());
            }
            
        } else {
            super.doPost(req, resp);
        }
    }

    // Helpers (can be refactored to public utility class) ----------------------------------------
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (@SuppressWarnings("unused") IOException e) {
            }
        }
    }
}
