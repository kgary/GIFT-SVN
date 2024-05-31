/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

/**
 * This interface is used by any survey scorer classes.
 * 
 * Note:
 *    i. make sure any implementation is handled in the ERT as well
 *       specifically mil.arl.gift.tools.ert.server.event.SurveyResultEvent:parseEvent method
 *       in the section that deals with survey scoring.
 *   ii. make sure any implementation is handled in the Tutor as well
 *       specifically mil.arl.gift.tutor.web.server.DomainSessionWebSession:displayAfterActionReview method
 *       in the section that deals with AfterActionReviewSurveyEvent events.
 * 
 * @author mhoffman
 *
 */
public interface ScoreInterface {

}
