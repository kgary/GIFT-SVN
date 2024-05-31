/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.MemoryFileServletRequest;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MemoryFileServlet class allows for serving a file from memory to the web client.
 * The servlet allows for streaming a file from Nuxeo backend into memory and then streaming the file to the webclient.
 * 
 * 
 * It is adapted from the FileServlet class from BalusC
 * {@link "http://balusc.blogspot.com/2007/07/fileservlet.html"} 
 * 
 * @author nblomberg
 *
 */
public class MemoryFileServlet extends HttpServlet {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MemoryFileServlet.class);
    
    private static final int MAX_UPLOAD_BYTES = 1000000000; // 1 GB

    // Constants ----------------------------------------------------------------------------------
    
    /* Setting the content disposition to "attachment" allows the client to download 
     * files that would normally open in the browser, such as .txt and .log files, 
     * but prevents the Media Semantic Avatar from being displayed inline. Therefore
     * the default value is "inline" */
    private static final String CONTENT_DISPOSITION_PROPERTY = "Content_Disposition";
    
    private static final String FILE_PATH_PROPERTY = "File_Path";
    
    private static final String ENABLE_FILE_UPLOAD = "Enable_File_Upload";
    
    
    private static final String ENABLE_NUXEO_CONNECTION = "Enable_Nuxeo_Connection";
    
    private static final long serialVersionUID = 1L;

    // Properties ---------------------------------------------------------------------------------
    
    private String filePath;
    
    private String contentDisposition = "inline";
    
    private boolean canUploadFile = false;
        
    private boolean enableNuxeoConnection = false;
    
    // Actions ------------------------------------------------------------------------------------
    @Override
    public void init() throws ServletException {
    	
    	CommonProperties.getInstance();
    	
    	ServicesManager.getInstance();
        
        String nuxeoConnection = getServletConfig().getInitParameter(ENABLE_NUXEO_CONNECTION);
        if (nuxeoConnection != null) {
            enableNuxeoConnection = Boolean.parseBoolean(nuxeoConnection);

        }
        
        if (!enableNuxeoConnection) {
            throw new ServletException("The " + ENABLE_NUXEO_CONNECTION + " property must be set to true for this servlet to work properly.");
        }
        logger.info("Servlet initialized with enableNuxeoConnection = "  + this.enableNuxeoConnection);
        
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
    
    @SuppressWarnings("unchecked")
    @Override
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
            
            // process only multipart requests - all uploads are multipart requests
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
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        
        try {
        	
        	MemoryFileServletRequest servletRequest = MemoryFileServletRequest.decode(request.getPathInfo());
        	
        	// Get requested file by path info.
            String requestedPath = servletRequest.getResourcePath();
            String userName = servletRequest.getUsername();
            
            if(logger.isInfoEnabled()) {
                logger.info("doGet() called with path: " + requestedPath + ", userName: " + userName);
            }
            
            if (userName == null || userName.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        	
            FileProxy fileProxy = fileServices.getFile(requestedPath, userName);
            
            // Prepare streams.
            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            
            InputStream inputStream = fileProxy.getInputStream();
            
            int size = inputStream.available();
            
            // Get content type by filename.
            String fileName = fileProxy.getName();
            String contentType = getServletContext().getMimeType(fileName);

            // If content type is unknown, then set the default value.
            // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
            // To add new content types, add new mime-mapping entry in web.xml.
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Init servlet response.
            response.reset();
            response.setBufferSize(size);
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes"); //needed to allow setCurrentTime to work for audio files in Chrome
            response.setHeader("Content-Length", String.valueOf(size));
           
            response.setHeader("Content-Disposition", this.contentDisposition + "; filename=\"" + fileName + "\"");
            try {
                // Open streams.
                input = new BufferedInputStream(fileProxy.getInputStream(), size);
                output = new BufferedOutputStream(response.getOutputStream(), size);

                // Write file contents to response.
                byte[] buffer = new byte[size];
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
            
            if(logger.isInfoEnabled()) {
                logger.info("File Proxy = " + fileProxy.getFileId());
            }
            return;
            
        } catch (Exception e) {
            logger.error("Unhandled exception caught: ", e);
            
            // Send a not found error back to the client.
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
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
