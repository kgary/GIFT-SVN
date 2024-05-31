/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.lti;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import org.imsglobal.pox.IMSPOXRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.lti.LtiRuntimeParameters;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.domain.AbstractProgressReporter;
import mil.arl.gift.domain.DomainModuleProperties;
import oauth.signpost.exception.OAuthException;


/**
 * The LtiProgressReporter class is used to report the progress of the learner (as a score value) back to
 * the LTI Tool Consumer.  The score is represented as a float value from 0.0 to 1.0 where 1.0 means '100% completion' of the course.
 * Note that the score reporter will overwrite any previous value for the learner in the tool consumer.  So if a leaner completes a course
 * and has 1.0 score, but restarts the course, the score will get set back to 0.0.  This currently is as intended per the LTI specification.
 * 
 * @author nblomberg
 *
 */
public class LtiProgressReporter extends AbstractProgressReporter {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LtiProgressReporter.class);
    
    /** The lti runtime parameters containin the information to send the score value back to the tool consumer. */
    LtiRuntimeParameters runtimeParams = null;
    /** A mapping of valid tool consumer information that is supported by GIFT> */
    private HashMap<String, TrustedLtiConsumer> consumerMap = DomainModuleProperties.getInstance().getTrustedLtiConsumers();
    
    /** 
     * Constructor (default)
     */
    public LtiProgressReporter() {
        
    }
    
    
    /** 
     * Sets the lti runtime parameters that will be used by the score reporter.
     * 
     * @param runtimeParams The runtime parameters that will be used by the score reporter.  Cannot be null.
     */
    public void setLtiRuntimeParameters(LtiRuntimeParameters runtimeParams) {
        
        if (runtimeParams == null) {
            throw new IllegalArgumentException("The runtime parameters cannot be null.");
        }
        
        this.runtimeParams = runtimeParams;
    }

    
    
    @Override
    public void reportProgress(int currentProgress, int maxProgress, boolean isFinalProgressReport) {

        try {
            if (runtimeParams != null) {
                LtiRuntimeParameters ltiParams = runtimeParams;
                
                // Calculate the "score" which is a float value between 0.0 and 1.0, where 1.0 represents 100% completion
                // through the course.
                Float progress = 0.0f;
                // Avoid divide by 0.
                if (maxProgress != 0) {
                    /* The current progress assumes the page is complete as soon
                     * as you get to it, so revert by 1. This means that the
                     * page will not be complete until they hit 'next'. */
                    if (!isFinalProgressReport) {
                        currentProgress = currentProgress - 1 < 0 ? 0 : currentProgress - 1;
                    }
                    progress = (float) currentProgress / (float) maxProgress;
                } else {
                    logger.error("Max potential progress is 0, which is not correct.  Progress will not be reported.");
                    return;
                }
                
                String sourceUrl = ltiParams.getOutcomeServiceUrl();
                String lisSourcedid = ltiParams.getLisSourcedid();
                String consumerKey = ltiParams.getConsumerKey();
                
               
                if (consumerMap != null && !consumerMap.isEmpty()) {
                    // Make sure the consumer key that was used is valid according to GIFT (also get the tool consumer secret value).
                    TrustedLtiConsumer consumer = consumerMap.get(consumerKey);
                    if (consumer != null) {
                        try {
                            // Send the score result to the tool consumer.
                            IMSPOXRequest.sendReplaceResult(sourceUrl, consumerKey, consumer.getConsumerSharedSecret(), lisSourcedid, 
                                    progress.toString());
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("reportProgress(): sent score of: " + progress + ", with lti params: " + ltiParams);
                            }
                        } catch (IOException e) {
                            logger.error("IOException caught sending the score to the Tool Consumer: ", e);
                        } catch (OAuthException e) {
                            logger.error("OAuthException caught sending the score to the Tool Consumer: ", e);
                        } catch (GeneralSecurityException e) {
                            logger.error("GeneralSecurityException caught sending the score to the Tool Consumer: ", e);
                        } catch (Exception e) {
                            logger.error("Exception caught sending the score to the Tool Consumer: ", e);
                        }
                    } else {
                        logger.error("Error sending the score to the lti consumer.  The consumer key could not be found: " + consumerKey);
                    }
                    
                } else {
                    logger.error("The consumer map was not properly configured to send a score to the tool consumer.");
                   
                }
                
            } else {
                // The runtime parameters may not be used for scoring, which is okay.
            }
        } catch (Throwable t) {
            logger.error("Caught throwable error while reporting the score: ", t);
        }
        
    }
}
