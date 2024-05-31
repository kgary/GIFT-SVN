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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.util.StringUtils;

/**
 * This servlet will be responsible for retrieving the requested scenario map image.
 *
 * @author sharrison
 */
public class GiftScenarioMapImageServlet extends HttpServlet {

    /** default serial version */
    private static final long serialVersionUID = 1L;

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GiftScenarioMapImageServlet.class);

    /** Default buffer size */
    private static final int RESPONSE_BUFFER_SIZE = 1024 * 100;

    /** Default byte size */
    private static final int FILE_BUFFER_SIZE = 4096;

    @Override
    public void init() throws ServletException {
        if (logger.isTraceEnabled()) {
            logger.trace("GiftScenarioMapImageServlet initialized.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("GiftScenarioMapImageServlet received request: " + request.toString());
        }

        final String imagePath = request.getPathInfo();

        /* Check if the image file is actually supplied to the request URI */
        if (StringUtils.isBlank(imagePath)) {
            logger.error("The request URI did not contain an image file.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File imageFile = new File(CommonProperties.getInstance().getTrainingAppsDirectory() + File.separator
                + PackageUtil.getWrapResourcesDir() + File.separator
                + PackageUtil.getTrainingAppsMaps() + File.separator + imagePath);

        try (ServletOutputStream out = response.getOutputStream();
                FileInputStream in = new FileInputStream(imageFile);) {

            /* Check for supported image extensions */
            if (imagePath.endsWith(".png")) {
                response.setContentType("image/png");
            } else if (imagePath.endsWith(".jpg") || imagePath.endsWith(".jpeg")) {
                response.setContentType("image/jpeg");
            } else {
                logger.error("Trying to retrieve an image with an unsupported file extension. Image path: '" + imagePath
                        + "'");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setBufferSize(RESPONSE_BUFFER_SIZE);

            byte[] buffer = new byte[FILE_BUFFER_SIZE];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.flush();

        } catch (Exception e) {
            logger.error("Exception caught in GiftScenarioMapImageServlet: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
