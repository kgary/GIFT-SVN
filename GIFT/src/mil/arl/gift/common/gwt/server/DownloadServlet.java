/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.safehtml.shared.UriUtils;

import mil.arl.gift.common.io.Constants;


/**
 * 
 * Servlet for serving up files in a webpage to force the browser to download
 * 
 * @author cpadilla
 *
 */
public class DownloadServlet extends HttpServlet {
            
    private static final Logger logger = LoggerFactory.getLogger(DownloadServlet.class);
    
    private static final long serialVersionUID = -2518144378629776469L;
    
    private static final int BUFFER = 1024 * 100;

    private static final int BYTE_SIZE = 4096;

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String fileAddress = req.getParameter("fileInfo1");
            String fileName = req.getParameter("fileName");
            
            ServletOutputStream out = resp.getOutputStream();
            String tempDirName = System.getProperty("java.io.tmpdir");
            File tempDir = new File(tempDirName);            
            File file = new File(tempDirName + Constants.FORWARD_SLASH + fileName);
            if(!file.getCanonicalPath().startsWith(tempDir.getCanonicalPath())){
                // protected against
                // CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                throw new Exception("The servlet file request attempted to access a file outside of the temp directory - "+fileName);
            }
            
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition:", "attachment;filename=" + "\"" + fileName);
            resp.setBufferSize(BUFFER);
            
            //encode characters in the file address that either aren't allowed in URLs (e.g. spaces) or would unintentionally affect the path (e.g. &)
            String urlEncoded = UriUtils.encodeAllowEscapes(fileAddress);
            
            //need to handle # character as a special case, since it is normally used as an identifier in URLs, which isn't what we want here
            urlEncoded = urlEncoded.replace("#", "%23");
            
            URL url = new URL(urlEncoded);
            FileUtils.copyURLToFile(url, file);
            
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[BYTE_SIZE];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.flush();
        
        } catch (Exception e) {
            logger.error("Exception caught in DownloadServlet for request\n"+req,e);
        }
    }

}
