/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.FreeResponseQuestionResponse;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestionResponse;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestionResponse;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.RatingScaleQuestionResponse;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SliderQuestionResponse;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.util.StringUtils;

/**
 * The score manager helps score survey responses.
 *
 * @author mhoffman
 *
 */
public class SurveyScorerManager {

    /**
     * instance of the logger
     */
    private static Logger logger = LoggerFactory.getLogger(SurveyScorerManager.class);

    /**
     * Return scores for the survey response based on the authored survey's
     * scoring choices.
     *
     * @param surveyResponse The survey response to get the scores from
     * @return List<ScoreInterface> The collection of scores for the survey response
     */
    public static List<ScoreInterface> getScores(SurveyResponse surveyResponse) {

        List<ScoreInterface> scores = new ArrayList<>();

        //get survey scorer types (list) to use appropriate "wrap-up" algorithm for each
        SurveyScorer scorerModel = surveyResponse.getSurveyScorerModel();

        if (scorerModel != null) {

            TotalScorer totalScorer = scorerModel.getTotalScorer();

            if (totalScorer != null) {

                SurveyAnswerScore sas = calculateSurveyAnswerScore(surveyResponse);

                if (sas == null) {
                    //ERROR
                    logger.error("Unable to get an answer score object for survey response of type TOTAL");

                } else if (!sas.getQuestionScores().isEmpty()) {
                    //no scoring information provided

                    logger.info("Calculated survey score of " + sas);
                    scores.add(sas);
                }

                if (sas != null) {

                    double totalPoints = sas.getTotalEarnedPoints();

                    for (AttributeScorerProperties attributeModel : totalScorer.getAttributeScorers()) {

                        AbstractEnum returnValue = attributeModel.getReturnValue(totalPoints);

                        SurveyScaleScore sss = new SurveyScaleScore();

                        SurveyScale surveyScale = new SurveyScale(attributeModel.getAttributeType(), returnValue, totalPoints);
                        if(surveyResponse.getSurveyEndTime() != null) {
                            surveyScale.setTimeStamp(surveyResponse.getSurveyEndTime());
                        }
                        
                        sss.getScales().add(surveyScale);
                        scores.add(sss);
                    }
                }
            }

            for (AttributeScorerProperties attributeScorerModel : scorerModel.getAttributeScorers()) {

                SurveyScaleScore sss = calculateSurveyScaleScore(attributeScorerModel, surveyResponse);

                if (sss == null) {
                    //ERROR
                    logger.error("Unable to get an scale score object for survey response with scorer type of SCALE for type " + attributeScorerModel.getAttributeType());
                    continue;

                } else if (!sss.getQuestionScores().isEmpty()) {
                    //no scoring information provided

                    logger.info("Calculated survey score of " + sss);
                    scores.add(sss);
                }
            }
        }
        
        //
        // check concept score(s) (always do this by default) - this will analyze the survey responses and return a
        //                                                      per concept score if any of the questions are associated 
        //                                                      with concepts.
        //
        SurveyConceptAssessmentScore surveyConceptAssessmentScore = calculateSurveyConceptAssessmentScore(surveyResponse);
        if(surveyConceptAssessmentScore != null){
            scores.add(surveyConceptAssessmentScore);
        }
        
        // If there are no explicit survey scorers than check if there is feedback for the
        // the survey responses provided.  If there is feedback, there needs to be a scorer to indicate that
        // the survey responses should be shown in a structured review
        if(scores.isEmpty()){
           
            if(surveyResponse.hasFeedbackForResponses()){
                //create scorer for feedback, otherwise the feedback for the survey response won't be shown
                
                scores.add(new SurveyFeedbackScorer());
            }
        }

        return scores;
    }
    
    /**
     * Analyze the survey responses for scores that can be associated with concepts.  This is done by
     * gathering all the concepts associated with the survey questions and calculating a score based on the 
     * number of correct/incorrect responses to those questions.
     * 
     * @param surveyResponse contains the responses to a survey
     * @return SurveyConceptAssessmentScore scoring information on a per concept basis.  Can be null if there are
     *         no survey questions with associated concepts.
     */
    private static SurveyConceptAssessmentScore calculateSurveyConceptAssessmentScore(SurveyResponse surveyResponse){
        
        @SuppressWarnings("unchecked")
		Map<String, ConceptOverallDetails> conceptToDetails = new CaseInsensitiveMap();
        
        //find the question responses in the survey response 
        for (SurveyPageResponse pageResponse : surveyResponse.getSurveyPageResponses()) {

            for (AbstractQuestionResponse questionResponse : pageResponse.getQuestionResponses()) {
         
                // get the concepts associated with the question
                AbstractSurveyQuestion<? extends AbstractQuestion> sQuestion = questionResponse.getSurveyQuestion();
                SurveyItemProperties properties = ((AbstractQuestion)sQuestion.getQuestion()).getProperties();
                // the set of one or more concepts set on this question by the author
                String associatedConcepts = (String) properties.getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS);
                // the concept this question was selected for
                String selectedConcept = (String) sQuestion.getPropertyValue(SurveyPropertyKeyEnum.SELECTED_CONCEPT);
                List<String> associatedConceptList;
                if(StringUtils.isBlank(selectedConcept)){
                    // use the associated concepts as a backup, this can present an issue in a multi tagged concept question (#4985)
                    associatedConceptList = SurveyItemProperties.decodeListString(associatedConcepts);
                }else{
                    associatedConceptList = new ArrayList<>();
                    associatedConceptList.add(selectedConcept);
                }
                
                if(!associatedConceptList.isEmpty()){
                    //there is at least one concept associated with this survey question
                
                    //
                    // Determine whether the question is right or wrong 
                    //
                    boolean correct = false;
                    double highestPossiblePoints = 0, points = 0;
                    
                    if (questionResponse instanceof MultipleChoiceQuestionResponse) {
    
                        MultipleChoiceQuestionResponse multipleChoiceResponse = (MultipleChoiceQuestionResponse) questionResponse;

                        highestPossiblePoints = multipleChoiceResponse.getSurveyQuestion().getHighestPossibleScore();
                        points = multipleChoiceResponse.getPoints();                       
    
                    } else if (questionResponse instanceof RatingScaleQuestionResponse) {
    
                        RatingScaleQuestionResponse ratingScaleResponse = (RatingScaleQuestionResponse) questionResponse;
    
                        highestPossiblePoints = ratingScaleResponse.getSurveyQuestion().getHighestPossibleScore();
                        points = ratingScaleResponse.getPoints();
    
                    } else if (questionResponse instanceof MatrixOfChoicesQuestionResponse) {
    
                        MatrixOfChoicesQuestionResponse matrixOfChoicesResponse = (MatrixOfChoicesQuestionResponse) questionResponse;
    
                        points = matrixOfChoicesResponse.getPoints();
                        highestPossiblePoints = matrixOfChoicesResponse.getSurveyQuestion().getHighestPossibleScore();
    
                    } else if (questionResponse instanceof SliderQuestionResponse) {
    
                        SliderQuestionResponse sliderResponse = (SliderQuestionResponse) questionResponse;
    
                        highestPossiblePoints = sliderResponse.getSurveyQuestion().getHighestPossibleScore();
                        points = sliderResponse.getPoints();    
                    } else if (questionResponse instanceof FreeResponseQuestionResponse) {
    
                        FreeResponseQuestionResponse freeResponse = (FreeResponseQuestionResponse) questionResponse;
    
                        highestPossiblePoints = freeResponse.getSurveyQuestion().getHighestPossibleScore();
                        points = freeResponse.getEarnedPoints();    
                    } 
                    
                    //the question is right if the highest possible points were earned
                    if(highestPossiblePoints > 0){
                        correct = highestPossiblePoints == points;
                    }
                    
                    // Update the scoring information for the concept(s) associated with the question
                    for(String concept : associatedConceptList){
                        
                        ConceptOverallDetails details = conceptToDetails.get(concept);
                        if(details == null){
                            details = new ConceptOverallDetails();
                            conceptToDetails.put(concept, details);
                        }
                        
                        if(correct){
                            details.addCorrectQuestion(((AbstractQuestion)sQuestion.getQuestion()).getQuestionId());
                        }else{
                            details.addIncorrectQuestion(((AbstractQuestion)sQuestion.getQuestion()).getQuestionId());
                        }
                    }
                
                }
                
            }
            
        }
        
        if(conceptToDetails.isEmpty()){
            return null;
        }else{
            return new SurveyConceptAssessmentScore(conceptToDetails);
        }
    }

    /**
     * Build the survey answer score (similar to what a scantron does) based on
     * the question scores.
     *
     * @param surveyResponse - contains the survey with responses
     * @return the scores for each question in the survey
     */
    private static SurveyAnswerScore calculateSurveyAnswerScore(SurveyResponse surveyResponse) {

        SurveyAnswerScore sas = new SurveyAnswerScore();

        //look at each question's responses and gather score attributes
        for (SurveyPageResponse pageResponse : surveyResponse.getSurveyPageResponses()) {

            for (AbstractQuestionResponse questionResponse : pageResponse.getQuestionResponses()) {

                QuestionScorer scorerModel = questionResponse.getSurveyQuestion().getScorerModel();

                if (scorerModel != null && scorerModel.getTotalQuestion()) {
                    //make sure the question has the appropriate scorer type 

                    QuestionAnswerScore qas = calculateQuestionAnswerScore(questionResponse);

                    if (qas == null) {
                        //ERROR
                        logger.error("Unable to get an answer score object for question response of " + questionResponse);
                        continue;
                    }

                    sas.getQuestionScores().add(qas);
                }

            }//end for


        }//end for

        if (sas.getQuestionScores().isEmpty()) {
            logger.info("There are no score results from the survey response");

        } else {

            //gather scoring information at each level of the survey hierarchy
            sas.collate();
        }

        return sas;
    }

    /**
     * Return the highest possible score from the weights provided and the
     * maximum number of selections allowed from those weights.
     *
     * @param weights - represents the value for each individual response.
     * @param maxSelections - the maximum number of selections (i.e. responses)
     * that can be selected (for a survey question).
     * @return double - the sum of the N highest positive weights, where N =
     * maxSelections.
     */
    public static double getHighestScore(List<Double> weights, int maxSelections) {

        double highestPossiblePoints = 0;

        if (maxSelections > 0 && weights != null) {

            if (maxSelections > weights.size()) {
                throw new IllegalArgumentException("The number of maximum selections (" + maxSelections + ") can't be greater than the number of answers available (" + weights.size() + ")");
            }

            Collections.sort(weights);
            for (int index = weights.size() - 1; index >= (weights.size() - maxSelections); index--) {

                Double weight = weights.get(index);
                if (weight > 0) {
                    highestPossiblePoints += weights.get(index);
                }
            }
        }

        return highestPossiblePoints;
    }

    /**
     * Build the question answer score (similar to what a scantron does) based
     * on the reply scores.
     *
     * @param questionResponse - contains the question with responses
     * @return the scores for each reply in the question
     */
    private static QuestionAnswerScore calculateQuestionAnswerScore(AbstractQuestionResponse questionResponse) {

        QuestionAnswerScore qas = new QuestionAnswerScore();

        if (questionResponse instanceof MultipleChoiceQuestionResponse) {

            MultipleChoiceQuestionResponse multipleChoiceResponse = (MultipleChoiceQuestionResponse) questionResponse;

            //get highest possible score
            double highestPossiblePoints = multipleChoiceResponse.getSurveyQuestion().getHighestPossibleScore();

            if (questionResponse.getResponses().isEmpty()) {
                //if there are no answers selected, we still want to know about this question for highest possible point calculation

                ReplyAnswerScore ras = new ReplyAnswerScore(0, highestPossiblePoints);
                qas.getReplyScores().add(ras);

            } else {

                double points = multipleChoiceResponse.getPoints();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !multipleChoiceResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+multipleChoiceResponse);
                    }
                    points = 0;
                }

                ReplyAnswerScore ras = new ReplyAnswerScore(points, highestPossiblePoints);
                qas.getReplyScores().add(ras);
            }

        } else if (questionResponse instanceof RatingScaleQuestionResponse) {

            RatingScaleQuestionResponse ratingScaleResponse = (RatingScaleQuestionResponse) questionResponse;

            //get highest possible score
            double highestPossiblePoints = ratingScaleResponse.getSurveyQuestion().getHighestPossibleScore();

            if (questionResponse.getResponses().isEmpty()) {
                //if there are no answers selected, we still want to know about this question for highest possible point calculation

                ReplyAnswerScore ras = new ReplyAnswerScore(0, highestPossiblePoints);
                qas.getReplyScores().add(ras);

            } else {

                double points = ratingScaleResponse.getPoints();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !ratingScaleResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+ratingScaleResponse);
                    }
                    points = 0;
                }

                ReplyAnswerScore ras = new ReplyAnswerScore(points, highestPossiblePoints);
                qas.getReplyScores().add(ras);
            }

        } else if (questionResponse instanceof MatrixOfChoicesQuestionResponse) {

            MatrixOfChoicesQuestionResponse matrixOfChoicesResponse = (MatrixOfChoicesQuestionResponse) questionResponse;

            //get highest possible score
            double highestPossiblePoints = matrixOfChoicesResponse.getSurveyQuestion().getHighestPossibleScore();

            if (questionResponse.getResponses().isEmpty()) {
                //if there are no answers selected, we still want to know about this question for highest possible point calculation

                ReplyAnswerScore ras = new ReplyAnswerScore(0, highestPossiblePoints);
                qas.getReplyScores().add(ras);

            } else {

                double points = matrixOfChoicesResponse.getPoints();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !matrixOfChoicesResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+matrixOfChoicesResponse);
                    }
                    points = 0;
                }

                ReplyAnswerScore ras = new ReplyAnswerScore(points, highestPossiblePoints);
                qas.getReplyScores().add(ras);
            }

        } else if (questionResponse instanceof FreeResponseQuestionResponse) {

            FreeResponseQuestionResponse freeResponse = (FreeResponseQuestionResponse) questionResponse;

            // if there are no answers selected, earned points will be 0
            double earnedPts = questionResponse.getResponses().isEmpty() ? 0 : freeResponse.getEarnedPoints();

            // get highest possible score
            double highestPossiblePoints = freeResponse.getSurveyQuestion().getHighestPossibleScore();
            
            // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
            if(earnedPts < highestPossiblePoints && !freeResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Setting earned points to 0 for "+freeResponse);
                }
                earnedPts = 0;
            }

            qas.getReplyScores().add(new ReplyAnswerScore(earnedPts, highestPossiblePoints));

        } else if (questionResponse instanceof SliderQuestionResponse) {

            SliderQuestionResponse sliderResponse = (SliderQuestionResponse) questionResponse;

            //get highest possible score
            double highestPossiblePoints = sliderResponse.getSurveyQuestion().getHighestPossibleScore();

            if (questionResponse.getResponses().isEmpty()) {
                //if there are no answers selected, we still want to know about this question for highest possible point calculation

                ReplyAnswerScore ras = new ReplyAnswerScore(0, highestPossiblePoints);
                qas.getReplyScores().add(ras);

            } else {

                double points = sliderResponse.getPoints();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !sliderResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+sliderResponse);
                    }
                    points = 0;
                }

                ReplyAnswerScore ras = new ReplyAnswerScore(points, highestPossiblePoints);
                qas.getReplyScores().add(ras);
            }

        } else {

            throw new IllegalArgumentException("There is no support for survey question type of " + questionResponse.getSurveyQuestion());
        }

        return qas;
    }

    /**
     * Build the survey scale score based on the question scales.
     *
     * @param surveyResponse - contains the survey with responses
     * @return the 'scale' scores for each question in the survey
     */
    private static SurveyScaleScore calculateSurveyScaleScore(AttributeScorerProperties attributeModel, SurveyResponse surveyResponse) {

        SurveyScaleScore sss = new SurveyScaleScore();
        List<QuestionScaleScore> questionScores = sss.getQuestionScores();

        //look at each question's responses and gather score attributes

        double totalPoints = 0.0;

        for (SurveyPageResponse pageResponse : surveyResponse.getSurveyPageResponses()) {

            for (AbstractQuestionResponse questionResponse : pageResponse.getQuestionResponses()) {

                QuestionScorer scorerModel = questionResponse.getSurveyQuestion().getScorerModel();

                if (scorerModel != null) {
                    AttributeScorerProperties attributeScorer = scorerModel.getAttributeScorer(attributeModel.getAttributeType());

                    if (attributeScorer != null) {

                        //make sure the question has the appropriate scorer type 

                        QuestionScaleScore qss = calculateQuestionScaleScore(questionResponse, attributeScorer);

                        if (qss == null) {
                            //ERROR
                            logger.error("Unable to get an scale score object for question response of " + questionResponse);
                        } else {

                            for (ReplyScaleScore rss : qss.getReplyScores()) {

                                for (AbstractScale as : rss.getScales()) {

                                    totalPoints += as.getRawValue();
                                }
                            }
                        }

                        questionScores.add(qss);
                    }
                } else {
                    
                    // If it's fill in the blank (free response or essay) then it is not scored, so ignore it.
                    // Otherwise log an error message.
                    if (!(questionResponse.getSurveyQuestion() instanceof FillInTheBlankSurveyQuestion)) {
                        logger.error("Unable to get a scorer model for question: " + questionResponse.getSurveyQuestion().getQuestion().getText());
                    } 
                    
                }
                
            }
        }

        AbstractEnum returnValue = attributeModel.getReturnValue(totalPoints);

        sss.getScales().add(new SurveyScale(attributeModel.getAttributeType(), returnValue, totalPoints));

        return sss;
    }

    //TODO: this is not completed because there needs to be a re-factor of question response objects throughout GIFT.  The current
    //logic makes it hard or cumbersome to store and retrieve scale:value pairs for slightly complex questions such as matrix of choices.
    //The matrix of choices question is used in common surveys such as the API, PANASX and ITC SOPI which need scales associated with responses. 
    /**
     *
     * @param questionResponse - contains the question with responses
     * @return the 'scale' score for each response in the question
     */
    private static QuestionScaleScore calculateQuestionScaleScore(AbstractQuestionResponse questionResponse, AttributeScorerProperties attributeModel) {

        QuestionScaleScore qss = new QuestionScaleScore();
        
        Date recentAnswer = questionResponse.getMostRecentAnswerTime();

        if (questionResponse.getSurveyQuestion() instanceof MultipleChoiceSurveyQuestion) {

            MultipleChoiceQuestionResponse multipleChoiceResponse = (MultipleChoiceQuestionResponse) questionResponse;

            if (questionResponse.getResponses().isEmpty()) {

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), 0);                
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);

            } else {

                double points = multipleChoiceResponse.getPoints();
                
                //get highest possible score
                double highestPossiblePoints = multipleChoiceResponse.getSurveyQuestion().getHighestPossibleScore();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !multipleChoiceResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+multipleChoiceResponse);
                    }
                    points = 0;
                }

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), points);
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);
            }

        } else if (questionResponse.getSurveyQuestion() instanceof RatingScaleSurveyQuestion) {

            RatingScaleQuestionResponse ratingScaleResponse = (RatingScaleQuestionResponse) questionResponse;

            if (questionResponse.getResponses().isEmpty()) {

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), 0);
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);

            } else {

                double points = ratingScaleResponse.getPoints();
                
                //get highest possible score
                double highestPossiblePoints = ratingScaleResponse.getSurveyQuestion().getHighestPossibleScore();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !ratingScaleResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+ratingScaleResponse);
                    }
                    points = 0;
                }

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), points);
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);
            }

        } else if (questionResponse.getSurveyQuestion() instanceof MatrixOfChoicesSurveyQuestion) {

            MatrixOfChoicesQuestionResponse matrixOfChoicesResponse = (MatrixOfChoicesQuestionResponse) questionResponse;

            if (questionResponse.getResponses().isEmpty()) {

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), 0);
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);

            } else {

                double points = matrixOfChoicesResponse.getPoints();
                
                //get highest possible score
                double highestPossiblePoints = matrixOfChoicesResponse.getSurveyQuestion().getHighestPossibleScore();
                
                // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
                if(points < highestPossiblePoints && !matrixOfChoicesResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Setting earned points to 0 for "+matrixOfChoicesResponse);
                    }
                    points = 0;
                }

                ReplyScaleScore rss = new ReplyScaleScore();

                QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), points);
                if(recentAnswer != null){
                    scale.setTimeStamp(recentAnswer);
                }
                
                rss.getScales().add(scale);

                qss.getReplyScores().add(rss);
            }
            
        } else if (questionResponse.getSurveyQuestion() instanceof FillInTheBlankSurveyQuestion) {

            FreeResponseQuestionResponse freeResponse = (FreeResponseQuestionResponse) questionResponse;

            // if there are no answers selected, earned points will be 0
            double points = questionResponse.getResponses().isEmpty() ? 0 : freeResponse.getEarnedPoints();

            // get highest possible score
            double highestPossiblePoints = freeResponse.getSurveyQuestion().getHighestPossibleScore();
            
            // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
            if(points < highestPossiblePoints && !freeResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Setting earned points to 0 for "+freeResponse);
                }
                points = 0;
            }

            ReplyScaleScore rss = new ReplyScaleScore();

            QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), points);
            if(recentAnswer != null){
                scale.setTimeStamp(recentAnswer);
            }
            
            rss.getScales().add(scale);

            qss.getReplyScores().add(rss);

        } else if (questionResponse.getSurveyQuestion() instanceof SliderSurveyQuestion) {

            SliderQuestionResponse sliderResponse = (SliderQuestionResponse) questionResponse;

            double points = sliderResponse.getPoints();
            
            //get highest possible score
            double highestPossiblePoints = sliderResponse.getSurveyQuestion().getHighestPossibleScore();
            
            // if the highest points where not earned and the question doesn't allow partial credit, set the earned points to zero
            if(points < highestPossiblePoints && !sliderResponse.getSurveyQuestion().getAllowsPartialCredit()) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Setting earned points to 0 for "+sliderResponse);
                }
                points = 0;
            }

            ReplyScaleScore rss = new ReplyScaleScore();

            QuestionScale scale = new QuestionScale(attributeModel.getAttributeType(), points);
            if(recentAnswer != null){
                scale.setTimeStamp(recentAnswer);
            }
            
            rss.getScales().add(scale);

            qss.getReplyScores().add(rss);

        } else {

            throw new IllegalArgumentException("There is no support for survey question type of " + questionResponse.getSurveyQuestion());
        }

        return qss;
    }
}
