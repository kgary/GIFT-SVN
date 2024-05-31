/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.lti;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.imsglobal.pox.IMSPOXRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LtiProvider;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.DomainModule;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 * The LTI Tool Consumer Tutor Servlet is a servlet that implements the LTI Tool Consumer
 * specification for LTI 1.1.1. This servlet will be responsible for handling the incoming LTI
 * provider score responses.
 * 
 * @author sharrison
 *
 */
public class LtiToolConsumerTutorServlet extends HttpServlet {

    /** Default serial version number */
    private static final long serialVersionUID = 1L;

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(LtiToolConsumerTutorServlet.class);

    /** Lookup key for the LTI result score */
    private static final String SCORE_KEY = "/resultRecord/result/resultScore/textString";

    /** Lookup key for the LTI result source id */
    private static final String SOURCE_ID_KEY = "/resultRecord/sourcedGUID/sourcedId";

    /** The delimiter used to separate the components in the LTI sourcedid */
    private static final String SOURCE_DELIM = ":";

    @Override
    public void init() throws ServletException {

        if (logger.isInfoEnabled()) {
            logger.info("Consumer Servlet initialized.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (logger.isInfoEnabled()) {
            logger.info("Consumer Servlet received request: " + request.toString());
        }

        // Debug option to print out the contents of each incoming post request.
        if (logger.isDebugEnabled()) {

            // Print out the post contents.
            StringBuilder postMsg = new StringBuilder("Incoming POST request..  Method type: ").append(request.getMethod())
                    .append("  Authorization type: ").append(request.getAuthType()).append("  Request URL: ");
            if (request.getRequestURL() == null) {
                postMsg.append("null");
            } else {
                postMsg.append(request.getRequestURL().toString());
            }

            logger.debug(postMsg.toString());
        }

        IMSPOXRequest pox = new IMSPOXRequest(request);
        StringBuilder errorMsg = new StringBuilder();
        // first validation check, is request valid (skipping authorization check for now)
        if (pox.valid) {
            if (pox.getBodyMap() == null) {
                errorMsg.append("The LTI Tool Consumer score request does not contain a body map.");
            } else if (pox.getBodyMap().get(SCORE_KEY) == null) {
                errorMsg.append("The LTI Tool Consumer score request does not contain the required parameter: Score.");
            } else if (pox.getBodyMap().get(SOURCE_ID_KEY) == null) {
                errorMsg.append("The LTI Tool Consumer score request does not contain the required parameter: Source.");
            } else {
                String scoreStr = pox.getBodyMap().get(SCORE_KEY);
                String source = pox.getBodyMap().get(SOURCE_ID_KEY);

                String[] splitSource = source.split("(?<!\\\\):");
                /* Length should be at least 3. If there are more than 3 pieces,
                 * then the concept name was appended and the returned score
                 * should only be applied to that specific concept. If there are
                 * exactly 3 pieces, the score should be applied to all selected
                 * concepts in the LTI course object */
                /* NOTE (STEVEN): Concept specific logic has not yet been merged */
                if (splitSource.length >= 3) {
                    int i = 0;
                    @SuppressWarnings("unused")
                    String courseId = parseEscapedLtiSourceComponent(splitSource[i++], SOURCE_DELIM); // course id
                    String domainSessionIdStr = parseEscapedLtiSourceComponent(splitSource[i++], SOURCE_DELIM); // domain session id
                    @SuppressWarnings("unused")
                    String userId = parseEscapedLtiSourceComponent(splitSource[i++], SOURCE_DELIM); // user id

                    Integer domainSessionId;
                    try {
                        domainSessionId = Integer.valueOf(domainSessionIdStr);
                    } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
                        // just set domain session id to null. Will log error and show error page later.
                        domainSessionId = null;
                    }

                    if (domainSessionId == null) {
                        String msg = "The LTI Tool Consumer score request contains an invalid source. There is no (or an invalid) domain session ID.";
                        logger.error(msg);
                        sendResponse(response, pox.getResponseFailure(msg, null));
                        return;
                    } else if (!DomainModule.getInstance().domainSessionExists(domainSessionId)) {
                        String msg = "The LTI Tool Consumer score request contains a valid domain session id, but there is no existing domain session that maps to the ID.";
                        logger.error(msg);
                        sendResponse(response, pox.getResponseFailure(msg, null));
                        return;
                    }

                    // now that we have the domain session id, check signature
                    LtiProvider provider = DomainModule.getInstance().lookupCurrentLtiProvider(domainSessionId);
                    if (provider != null) {
                        pox.validateRequest(provider.getKey(), provider.getSharedSecret(), request, DomainModuleProperties.getInstance().getLtiConsumerServletUrl());
                        if (!pox.valid) {
                            String msg = "The LTI Tool Consumer score request failed validation.";
                            sendResponse(response, pox.getResponseFailure(msg, null));
                            DomainModule.getInstance().closeDomainSessionWithError(domainSessionId, msg, pox.errorMessage);
                            return;
                        }

                        Double score;
                        try {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Consumer Servlet request contains score of: " + scoreStr);
                            }
                            score = Double.valueOf(scoreStr);
                        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
                            // just set score to null. Will log error and show error page later.
                            score = null;
                        }

                        if (score == null || score < 0 || score > 1) {
                            sendResponse(response, pox.getResponseFailure(
                                    "Score " + scoreStr + " is not a valid response. Score must be between 0-1 inclusively.", null));
                            DomainModule.getInstance().closeDomainSessionWithError(domainSessionId,
                                    "The LTI Tool Consumer score request did not contain a valid score.",
                                    "The LTI Tool Consumer score request contained a score of '" + scoreStr
                                            + "'. Score must be between 0-1 inclusively.");
                            return;
                        }

                        try {
                            DomainModule.getInstance().processLtiConsumerScoreResponse(score, domainSessionId);
                        } catch (DetailedException de) {
                            String msg = "Exception occurred when trying to process the LTI score response: " + de.getDetails();
                            sendResponse(response, pox.getResponseFailure(msg, null));
                            DomainModule.getInstance().closeDomainSessionWithError(domainSessionId,
                                    "Exception occurred when trying to process the LTI score response.", de.getDetails());
                            return;

                        } catch (Exception e) {
                            String msg = "Exception occurred when trying to process the LTI score response: " + e.getMessage();
                            sendResponse(response, pox.getResponseFailure(msg, null));
                            DomainModule.getInstance().closeDomainSessionWithError(domainSessionId,
                                    "Exception occurred when trying to process the LTI score response.", e.getMessage());
                            return;
                        }
                    } else {
                        // this can occur if the learner chooses to proceed to the next course object before completing the LTI provider.
                        errorMsg.append("The LTI provider could not be located because either the domain session could not be found or the domain session didn't have a reference to the Lesson Material that contains the LTI provider.");
                    }
                } else {
                    errorMsg.append("The LTI Tool Consumer score request contains an invalid source and could not be parsed correctly.");
                }
            }
        } else {
            errorMsg.append("The LTI Tool Consumer score request was not in a valid format and was unable to be read.");
            if (pox.errorMessage != null && !pox.errorMessage.isEmpty()) {
                errorMsg.append(" Error message: ").append(pox.errorMessage);
            }
        }

        if (!errorMsg.toString().isEmpty()) {
            logger.error(errorMsg.toString());
            sendResponse(response, pox.getResponseFailure(errorMsg.toString(), null));
            return;
        }

        sendResponse(response, pox.getResponseSuccess("Successfully processed score.", null));
    }

    /**
     * Parses the escaped sourcedid component value.
     * 
     * @param value the escaped string.
     * @param delimiter the delimiter was escaped if it existed within the
     *        value.
     * @return the parsed string.
     */
    private String parseEscapedLtiSourceComponent(String value, String delimiter) {
        // regex is used which is why there are so many backslashes
        return value.replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\" + delimiter, delimiter);
    }

    /**
     * Sends the response back to the provider.
     * 
     * @param response the HTTP servlet response
     * @param message the message to send
     * @throws IOException might occur when writing the message to the response
     */
    private void sendResponse(HttpServletResponse response, String message) {
        try {
            response.setContentType("application/xml");
            response.getOutputStream().write(message.getBytes());
        } catch (Exception e) {
            logger.error("Failed to send a response to the LTI provider for the score message.", e);
        }
    }
}
