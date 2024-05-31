/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.domain.AbstractProgressReporter;
import mil.arl.gift.domain.BaseDomainSession;
import mil.arl.gift.domain.DomainCourseFileHandler;

/**
 * Used to send course object progress reports to the UMS module.
 * 
 * @author mhoffman
 *
 */
public class LMSProgressReporter extends AbstractProgressReporter {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(LMSProgressReporter.class);
    
    /** used to send the data to the domain module */
    private BaseDomainSession baseDomainSessionInstance;
    
    private static final String PERCENT = "percent";
    
    /** who's progress is being reported to the LMS */
    private Set<String> usernames = new HashSet<>();
    
    /**
     * set attribute
     * 
     * @param baseDomainSessionInstance used to send the data to the domain module.  Can't be null.
     */
    public LMSProgressReporter(BaseDomainSession baseDomainSessionInstance){
        
        if(baseDomainSessionInstance == null){
            throw new IllegalArgumentException("The base domain session instance can't be null");
        }
        this.baseDomainSessionInstance = baseDomainSessionInstance;
        
        usernames.add(baseDomainSessionInstance.getDomainSessionInfo().getUsername());
    }

    @Override
    public void reportProgress(int currentProgress, int maxProgress, boolean isFinalProgressReport) {
        
        if(isFinalProgressReport){
            //only send the last progress report, which could be at the completion of a course, a GIFT determined premature
            //end to a course, the user gracefully ended a course using the stop button or the user closed the webpage and then
            //started another course.
        
            // Calculate the "score" which is a float value between 0.0 and 1.0, where 1.0 represents 100% completion
            // through the course.
            Float progress = 0.0f;
            // Avoid divide by 0.
            if (maxProgress != 0) {
                progress = (float) currentProgress / (float) maxProgress;
            } else {
                logger.error("Max potential progress is 0, which is not correct.  Progress will not be reported.");
                return;
            }
            
            AssessmentLevelEnum assessment = AssessmentLevelEnum.UNKNOWN;
            if(progress >= 1.0){
                progress = 100.0f;  //cap value, just in case
                assessment = AssessmentLevelEnum.AT_EXPECTATION;
            }else{
                progress = progress * 100.0f;  //convert to percent
            }

            GradedScoreNode progressRootNode = new GradedScoreNode(baseDomainSessionInstance.getCourseManager().getCourseName(), assessment);
            
            String percentStr = String.format("%.2f", progress);
            DefaultRawScore defaultRawScore = new DefaultRawScore(percentStr, PERCENT);
            RawScoreNode rawScoreNode = new RawScoreNode(DomainCourseFileHandler.COMPLETE_NODE_NAME, defaultRawScore, assessment, usernames);
            progressRootNode.addChild(rawScoreNode);
            
            //send to LMS
            baseDomainSessionInstance.sendPublishScore(new LMSCourseRecord(baseDomainSessionInstance.getDomainSessionInfo().getDomainSourceId(), progressRootNode, new Date()), null);
        }
    }

}
