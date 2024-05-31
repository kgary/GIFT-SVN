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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.video.LOMType;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.aar.VideoMetadata.VideoMetadataField;
import mil.arl.gift.common.aar.VideoMetadataFileHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * A servlet used to process raw recording data and save it to GIFT's file system as a file on disk.
 * 
 * @author nroberts
 */
public class RecorderServlet extends HttpServlet {

    /** The file extension to give uploaded audio recording data */
    private static final String AUDIO_RECORDING_EXTENSION = ".wav";

    /** The path to the folder where recording files should be written */
    private static final String RECORDING_OUTPUT_PATH = "output/recording";
    
    /** The MIME type corresponding to plain text content using UTF-8 encoding */
    private static final String UTF_8_TEXT_TYPE = "text/plain;charset=UTF-8";

    /** An Access-Control-Allow-Origin HTTP header value indicating that any origin may receive an HTTP response */
    private static final String ANY_ORIGIN = "*";

    /** The HTTP response header denoting which origins an HTTP response can be shared with */
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    /** The HTTP response header denoting the character length of an HTTP response */
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    /**
     * The HTTP response header denoting the character length of an HTTP
     * response
     */
    private static final String CONTENT_RANGE_HEADER = "Content-Range";

    /**
     * The HTTP response header for informing the client that it supports range
     * requests
     */
    private static final String ACCEPT_RANGES_HEADER = "Accept-Ranges";

    /** The HTTP request header for a specific range */
    private static final String RANGE_HEADER = "Range";

    /** 
     * The default MIME type that file data should be encoded as if the server cannot 
     * determine which content type to use
     */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(RecorderServlet.class);
    
    /** The default number of bytes to allocate when writing recording data to a GET response */
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    /** The version number used by the java serialization logic */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        
        CommonProperties.getInstance();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        /*
         * Note: The below code is copied almost verbatim from FileServlet. If this code
         * needs to be changed later in a way that also makes sense for FileServlet, we may
         * want to consider merging this servlet's behavior with FileServlet or even replacing
         * uses of this servlet with FileServlet instead.
         */
        
        // Get requested file by path info.
        String requestedFile = request.getPathInfo();

        // Check if file is actually supplied to the request URI.
        if (requestedFile == null) {
            // Do your thing if the file is not supplied to the request URI.
            // Throw an exception, or send 404, or show default/warning page, or just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }
        
        // Remove leading slashes in the path, if needed
        if(requestedFile.startsWith(Constants.FORWARD_SLASH)) {
            requestedFile = requestedFile.substring(1);
        }

        // Decode the file name (might contain spaces and on) and prepare file object.
        File file = new File(URLDecoder.decode(requestedFile, "UTF-8"));
        
        if(logger.isInfoEnabled()) {
            logger.info("Accessing file at " + file.getAbsolutePath());
        }

        // Check if file actually exists in filesystem.
        if (!file.exists()) {
            // Do your thing if the file appears to be non-existing.
            // Throw an exception, or send 404, or show default/warning page, or just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }

        // Get content type by filename.
        String contentType = getServletContext().getMimeType(file.getName());

        /* Get content type by path */
        if (contentType == null) {
            contentType = Files.probeContentType(file.toPath());
        }

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }

        // Init servlet response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setContentType(contentType);
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ANY_ORIGIN);
        response.setHeader(ACCEPT_RANGES_HEADER, "bytes");
        if (contentType.startsWith("video")) {
            /* 206 partial content status */
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }

        /* Represents the full file */
        final long fileLength = file.length();
        Range full = new Range(0, fileLength - 1, fileLength);
        List<Range> ranges = new ArrayList<Range>();

        /* Process the range header */
        String range = request.getHeader(RANGE_HEADER);
        if (StringUtils.isNotBlank(range)) {
            /* Substring past the 'range=' part and split on comma in case there
             * are multiple ranges */
            for (String part : range.substring(RANGE_HEADER.length() + 1).split(",")) {
                /*-
                 * Assuming a file with length of 100, the following examples
                 * returns bytes at: 
                 * 50-80 (50 to 80)
                 * 40- (40 to length=100)
                 * -20 (length-20=80 to length=100) */
                long start = sublong(part, 0, part.indexOf("-"));
                long end = sublong(part, part.indexOf("-") + 1, part.length());

                if (start == -1) {
                    start = fileLength - end;
                    end = fileLength - 1;
                } else if (end == -1 || end > fileLength - 1) {
                    end = fileLength - 1;
                }

                /* Check if Range is syntactically valid. If not, then return
                 * 416 */
                if (start > end) {
                    /* Required in 416 */
                    response.setHeader(CONTENT_RANGE_HEADER, "bytes */" + fileLength);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }

                /* Add range */
                ranges.add(new Range(start, end, fileLength));

                /* We currently only support a single range */
                break;
            }
        }

        /* Prepare streams */
        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        try {
            /* Open streams */
            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

            Range r;
            if (ranges.isEmpty() || ranges.get(0) == full) {
                r = full;
            } else {
                /* We currently only support a single range */
                r = ranges.get(0);
                response.setHeader(CONTENT_RANGE_HEADER, "bytes " + r.start + "-" + r.end + "/" + r.total);
            }

            response.setHeader(CONTENT_LENGTH_HEADER, String.valueOf(r.length));
            copy(input, output, r.start, r.length, fileLength);
        } catch (Throwable t) {
            logger.warn("Caught exception reading file.", t);
        } finally {
            // Gently close streams.
            close(output);
            close(input);
        }
    }

    /**
     * Copy the given byte range of the given input to the given output.
     * 
     * @param input The input to copy the given range to the given output for.
     *        Can't be null.
     * @param output The output to copy the given range from the given input
     *        for. Can't be null.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @param fileLength the total length of the file.
     * @throws IOException If something fails at I/O level.
     */
    private static void copy(BufferedInputStream input, BufferedOutputStream output, long start, long length,
            long fileLength) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;

        if (fileLength == length) {
            /* Write full range */
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } else {
            /* Write partial range */
            input.skip(start);
            long toRead = length;

            while ((read = input.read(buffer)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    /* Reached the end of the range */
                    break;
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if(logger.isInfoEnabled()) {
            logger.info("Received post request");
        }
        
        //ensure the response sends back text, since we need to return the path to the uploaded file
        response.reset();
        response.setContentType(UTF_8_TEXT_TYPE);
        
        /* 
         * Look at the parameters passed in to determine if this recording is associated with a domain session.
         * If it is, then it needs to be saved to that domain session's output folder.
         */
        final String query = URLDecoder.decode(request.getQueryString(), Constants.UTF8);
        RecorderParams params = RecorderParams.decodeFromQuery(query);
        
        if(logger.isDebugEnabled()){
            logger.debug("Recorder params are ="+params);
        }

        if (params != null && params.getVideoMetadata() != null && params.isPerformDelete()){
            handleDelete(params, request, response);
        }else{
            handleUpload(params, request, response);
        }
    }
    
    /**
     * Handle the Post request to delete a recording.
     * @param params the client parameters specific to the request, e.g. metadata reference 
     * @param request the Http request details from the client
     * @param response where to place the server response to this request
     * @throws IOException if there was a problem populating the response
     */
    private void handleDelete(RecorderParams params, HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        try {
                    
            VideoMetadata videoMetadata = params.getVideoMetadata();
            FileTreeModel locationPath = FileTreeModel
                    .createFromRawPath(PackageUtil.getDomainSessions() + File.separator + videoMetadata.getLocation());
            File outputFolder = new File(locationPath.getParentTreeModel().getRelativePathFromRoot());
            
            // Decode the file name (might contain spaces and on) and prepare file object.
            File file = new File(outputFolder, videoMetadata.getFileName());
            
            if(logger.isInfoEnabled()) {
                logger.info("Deleting video file at " + file.getAbsolutePath());
            }

            // Check if file actually exists in file system.
            if (file.exists()) {
                file.delete();
            }                    
            
            if(videoMetadata.getMetadataFile() != null){
                file = new File(outputFolder, videoMetadata.getMetadataFileName());
                
                if(logger.isInfoEnabled()) {
                    logger.info("Deleting metadata file at " + file.getAbsolutePath());
                }
                
                // Check if file actually exists in file system.
                if (file.exists()) {
                    file.delete();
                } 
            }
            
            if(videoMetadata.getSpaceMetadataFile() != null){
                file = new File(outputFolder, videoMetadata.getSpaceMetadataFileName());
                
                if(logger.isInfoEnabled()) {
                    logger.info("Deleting space metadata file at " + file.getAbsolutePath());
                }
                
                // Check if file actually exists in file system.
                if (file.exists()) {
                    file.delete();
                } 
                
            }
            
            if(logger.isInfoEnabled()){
                logger.info("Finished deleting");
            }
            
            response.getOutputStream().print("");

        } catch (Exception e) {

            logger.error("Failed to delete recorded data.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Cound not delete recorded data because " + "an error occurred while processing said data: " + e);
        }
    }
    
    /**
     * Handle the Post request to upload a new recording.
     * @param params the client parameters specific to the request, e.g. metadata reference and recorded blob
     * @param request the Http request details from the client
     * @param response where to place the server response to this request
     * @throws IOException if there was a problem populating the response
     */
    private void handleUpload(RecorderParams params, HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        try {
            File file;
            if (params != null && params.getVideoMetadata() != null) {
                file = handleVideoUpload(params.getVideoMetadata(), request, response);
            } else {
                file = handleAudioRecording(params, request, response);
            }
            
            /* Get the path to this servlet, minus any leading slashes */
            String servlet = request.getServletPath();
            if (servlet.startsWith(Constants.FORWARD_SLASH)) {
                servlet = servlet.substring(1);
            }

            /* Send the client a response containing the path needed to get the
             * uploaded file from this servlet */
            response.getOutputStream().print(servlet + Constants.FORWARD_SLASH + file.getPath());

            if (logger.isInfoEnabled()) {
                logger.info("Finished uploading a file "+file);
            }
        } catch (Exception e) {

            logger.error("Failed to upload recorded data.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Cound not upload recorded data because " + "an error occurred while processing said data: " + e);
        }
    }
    
    private File handleAudioRecording(RecorderParams params, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        DomainSession targetSession = null;
        if(params != null && params.getDomainSessionId() != null && params.getUserId() != null) {
            targetSession = new DomainSession(params.getDomainSessionId(), params.getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);

            if (params.getExperimentId() != null) {
                targetSession.setExperimentId(params.getExperimentId());
            }
        }

        /* Create the folder where recording output should go, if it does not yet exist */
        String targetFolder = targetSession != null 
                ? PackageUtil.getDomainSessions() + File.separator + targetSession.buildLogFileName()
                : RECORDING_OUTPUT_PATH;
                
        File recordingOutputFolder = new File(targetFolder);
        if(!recordingOutputFolder.exists() || !recordingOutputFolder.isDirectory()) {
            recordingOutputFolder.mkdir();
        }
        
        /* Create a unique name for the file that the uploaded data will be written to */
        String recordingPath = UUID.randomUUID().toString() + AUDIO_RECORDING_EXTENSION;
        
        /* Create the file */
        File recordingFile = new File(recordingOutputFolder, recordingPath);
        recordingFile.createNewFile();
        
        writeFile(recordingFile, request);
        
        return recordingFile;
    }

    /**
     * Create the video and metadata file on the server.
     * @param videoMetadata contains metadata about the video
     * @param request the request to upload the video
     * @param response the response to upload the video. Not used yet.
     * @return the metadata file that was created.  This has a reference to the video file.
     * @throws Exception if there was a problem creating the video or metadata file on the server
     */
    private File handleVideoUpload(VideoMetadata videoMetadata, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (videoMetadata == null) {
            throw new IllegalArgumentException("The parameter 'videoMetadata' cannot be null.");
        }

        FileTreeModel locationPath = FileTreeModel
                .createFromRawPath(PackageUtil.getDomainSessions() + File.separator + videoMetadata.getLocation());
        File recordingOutputFolder = new File(locationPath.getParentTreeModel().getRelativePathFromRoot());
        if (!recordingOutputFolder.exists() || !recordingOutputFolder.isDirectory()) {
            throw new IllegalArgumentException(
                    "Unable to upload the video. The folder '" + recordingOutputFolder.getPath() + "' does not exist.");
        }
        
        final String videoName = locationPath.getFileOrDirectoryName();
        
        /* Create the metadata file */
        final String metadataFilename = videoName.substring(0, videoName.lastIndexOf('.'));
        File videoMetadataFile = new File(recordingOutputFolder,
                metadataFilename + AbstractSchemaHandler.VIDEO_FILE_EXTENSION);

        /* Create the video file */
        File videoFile = new File(recordingOutputFolder, videoName);
        if(videoFile.exists()){
            // this is a request to update the metadata
            
        }else{
            // this is a request to create a new video and metadata
            videoFile.createNewFile();

            writeFile(videoFile, request);
            
            /* Ensure unique name */
            int index = 1;
            while (videoMetadataFile.exists()) {
                videoMetadataFile = new File(recordingOutputFolder,
                        metadataFilename + " (" + index++ + ")" + AbstractSchemaHandler.VIDEO_FILE_EXTENSION);
            }
        }

        try (OutputStream output = new FileOutputStream(videoMetadataFile)) {
            /* Since we are putting the video metadata file in the same location
             * as the video file, create a new video metadata with location as
             * just the video filename since it's a relative path */
            final Map<String, String> metaProps = new HashMap<String, String>();
            videoMetadata.getProperties(metaProps);
            metaProps.put(VideoMetadataField.LOCATION.getTag(), videoName);

            final VideoMetadata lomMeta = VideoMetadata.generateMetadataFromProperties(metaProps);
            videoMetadata.setMetadataFile(videoMetadataFile.getName());
            final JAXBElement<LOMType> lomObj = VideoMetadataFileHandler.buildLOM(lomMeta);

            AbstractSchemaHandler.writeToFile(lomObj, output, FileType.VIDEO_METADATA, true);
        }

        return videoMetadataFile;
    }

    private void writeFile(File file, HttpServletRequest request) throws FileNotFoundException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("The parameter 'file' cannot be null.");
        }

        /* Read the uploaded data into a buffer and write it to the created
         * file */
        try (OutputStream output = new FileOutputStream(file)) {
            InputStream input = request.getInputStream();
            byte[] buffer = new byte[16 * 1024];

            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
    
    // Helpers (can be refactored to public utility class) ----------------------------------------
    /**
     * Closes the resource.
     * 
     * @param resource the resource to close.
     */
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (@SuppressWarnings("unused") IOException e) {
            }
        }
    }

    /**
     * Returns a substring of the given string value from the given begin index
     * to the given end index as a long. If the substring is empty, then -1 will
     * be returned
     * 
     * @param value The string value to return a substring as long for. Can't be
     *        null.
     * @param beginIndex The begin index of the substring to be returned as
     *        long.
     * @param endIndex The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or -1 if substring
     *         is empty.
     */
    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    /**
     * This class represents a byte range.
     * 
     * @author sharrison
     */
    private class Range {
        /** The start of the range */
        private final long start;

        /** The end of the range */
        private final long end;

        /** The length of the range */
        private final long length;

        /** The total length of the byte source */
        private final long total;

        /**
         * Construct a byte range.
         * 
         * @param start Start of the byte range.
         * @param end End of the byte range.
         * @param total Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }
    }
}
