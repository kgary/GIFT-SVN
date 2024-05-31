/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.AuthoredBranch.Paths.Path;
import generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse.Answer.Selection;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.domain.AsyncActionCallback;
import mil.arl.gift.domain.DomainModule;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;

/**
 * Used to evaluate the conditions for the various paths of an authored branch course object.
 * 
 * @author mhoffman
 *
 */
public class PathConditionEvaluator {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(PathConditionEvaluator.class);

    /**
     * Get's the next path of the authored branch to use
     * 
     * @param authoredBranch the authored branch
     * @param domainSessionInfo the domain session
     * @param lmsUsername the lms username
     * @param surveyResponses the list of survey responses
     * @param publishedScoreToEventIdMap the map of published scores to dates
     * @param callback the callback for when the branch evaluation has been completed
     * @return the next path the authored branch should take
     */
    public static generated.course.AuthoredBranch.Paths.Path getNextPath(final generated.course.AuthoredBranch authoredBranch, 
            final DomainSession domainSessionInfo, String lmsUsername, final List<SurveyResponse> surveyResponses, 
            Map<PublishLessonScoreResponse, Date> publishedScoreToEventIdMap, final AsyncActionCallback callback){
        
        List<generated.course.AuthoredBranch.Paths.Path> paths = authoredBranch.getPaths().getPath();
        
        //build map of paths
        Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById = new HashMap<>();
        for(generated.course.AuthoredBranch.Paths.Path path : paths){
            pathMapById.put(path.getPathId().intValue(), path);
        }
        
        final generated.course.AuthoredBranch.SimpleDistribution simpleDistr = authoredBranch.getSimpleDistribution();
        if(simpleDistr != null){
            //random or balanced distribution
            
            Serializable choice = simpleDistr.getRandomOrBalancedOrCustom();
            if(choice instanceof generated.course.AuthoredBranch.SimpleDistribution.Random){
                //randomly choose a path, random number doesn't need to depend on previous random numbers
                
                int pathIndex = new Random().nextInt(paths.size());
                
                generated.course.AuthoredBranch.Paths.Path selectedPath = paths.get(pathIndex);
                
                logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                        " (id "+authoredBranch.getBranchId()+") based on a random distribution.");
                
                return selectedPath;
                
            }else{
                //determine which path should be chosen based on balanced OR custom distribution
                
                //get all path db data to make decision
                List<BranchPathHistory> branchHistoryPaths = new ArrayList<>(paths.size());
                for(generated.course.AuthoredBranch.Paths.Path path : paths){
                    
                    BranchPathHistory branchHistoryPath = new BranchPathHistory(domainSessionInfo.getDomainSourceId(), domainSessionInfo.getExperimentId(),
                            authoredBranch.getBranchId().intValue(), path.getPathId().intValue(), path.getName(), false);
                    
                    branchHistoryPaths.add(branchHistoryPath);
                }
                
                return handleBranchHistoryEvaluation(authoredBranch, pathMapById, choice, branchHistoryPaths, domainSessionInfo, callback);
                
            }
        }else{
            
            //learner centric
            // - learner state -> goto ped? (TBD)
            // - knowledge assessment -> goto ped? (TBD)
            // - survey response -> look at courseEvents (DONE)
            // - dkf scoring -> look at publishedScoreToEventIdMap
            //
            // Note: if multiple path conditions are satisfied, choose the one with more conditions first than if still a tie choose default            
            
            return handleLearnerCentricEvaluation(authoredBranch, domainSessionInfo, lmsUsername, pathMapById, 
                    publishedScoreToEventIdMap, paths, surveyResponses, callback);
        }
        
    }
    
    /**
     * Selects a path from an authored branched based on learner centric evaluation
     * 
     * @param authoredBranch the authored branch to evaulate
     * @param domainSessionInfo the domain session
     * @param lmsUsername the lms username
     * @param pathMapById the map of the path id's to the path
     * @param publishedScoreToEventIdMap the map of published scores to dates
     * @param paths the list of paths
     * @param surveyResponses the list of survey responses
     * @param callback the callback for when the branch evaluation has been completed
     * @return the next path the authored branch should take
     */
    private static Path handleLearnerCentricEvaluation(final generated.course.AuthoredBranch authoredBranch, 
            final DomainSession domainSessionInfo,
            String lmsUsername,
            Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById, 
            Map<PublishLessonScoreResponse, Date> publishedScoreToEventIdMap,
            List<generated.course.AuthoredBranch.Paths.Path> paths,
            final List<SurveyResponse> surveyResponses,
            AsyncActionCallback callback){
        
        int defaultPathId = authoredBranch.getDefaultPathId().intValue();
        generated.course.AuthoredBranch.Paths.Path defaultPath = pathMapById.get(defaultPathId);
        
        // contains paths which have the most condition(s) satisfied.  This collection will contain
        // more than one entry when more than one path is satisfied and the number of conditions satisfied
        // is equal to the other conditions satisfied for the path(s) already in the collection.
        List<generated.course.AuthoredBranch.Paths.Path> equallySatisfiedPaths = new ArrayList<>();
        int highestNumOfCond = 0;
        
        LMSCourseRecordsWrapper lmsCourseRecordsWrapper = new LMSCourseRecordsWrapper();
        for(generated.course.AuthoredBranch.Paths.Path path : paths){
            
            Serializable condition = path.getCondition().getCustomPercentOrLearnerCentric();
            if(condition instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric){
                
                generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric learnerCondition = (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric) condition;
                
                boolean satisfiedAll = true;
                List<Serializable> condTypes = learnerCondition.getLearnerCondTypes();
                for(Serializable condType : condTypes){
                    
                    if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse){
                        //check responses to a survey 
                        
                        generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse surveyResponseCond =
                                (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse)condType;

                        if(!checkSurveyResponsePathCondition(surveyResponseCond, surveyResponses)){
                            satisfiedAll = false;
                            break;
                        }
                        
                    }else if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring){
                        //check DKF scoring results
                        
                        generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring dkfScoringCond =
                                (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring)condType;
                        
                        if(lmsCourseRecordsWrapper.lmsCourseRecords == null){
                            //retrieve them from the LMS
                            Object waitObj = new Object();
                            
                            sendLMSDataRequest(publishedScoreToEventIdMap.keySet(), domainSessionInfo, lmsUsername, new MessageCollectionCallback() {
                                
                                @Override
                                public void success() {

                                    synchronized(waitObj){
                                        waitObj.notifyAll();
                                    }
                                }
                                
                                @Override
                                public void received(Message msg) {
                                    lmsCourseRecordsWrapper.lmsCourseRecords = (LMSCourseRecords) msg.getPayload();
                                }
                                
                                @Override
                                public void failure(String why) {
                                    callback.onFailure(new DetailedException("Failed to select the appropriate path for the authored branch named '"+authoredBranch.getTransitionName()+"'.",
                                            "There was a problem retrieving the LMS records which are needed to compare the real-time assessment (DKF) scoring results.  The reason reads\n"+why, null));
                                    
                                    synchronized(waitObj){
                                        waitObj.notifyAll();
                                    }
                                }
                                
                                @Override
                                public void failure(Message msg) {
                                    callback.onFailure(new DetailedException("Failed to select the appropriate path for the authored branch named '"+authoredBranch.getTransitionName()+"'.",
                                            "There was a problem retrieving the LMS records which are needed to compare the real-time assessment (DKF) scoring results.  The reason reads\n"+msg, null));
                                    
                                    synchronized(waitObj){
                                        waitObj.notifyAll();
                                    }
                                }
                            });
                            
                            synchronized(waitObj){
                                try {
                                    waitObj.wait();
                                } catch (InterruptedException e) {
                                    callback.onFailure(new DetailedException("Failed to select the appropriate path for the authored branch named '"+authoredBranch.getTransitionName()+"'.",
                                            "There was a problem while waiting for the LMS records request which are needed to compare the real-time assessment (DKF) scoring results", e));
                                }
                            }
                        }
                        
                        if(!checkDkfScoringPathCondition(dkfScoringCond, publishedScoreToEventIdMap, lmsCourseRecordsWrapper.lmsCourseRecords)){
                            satisfiedAll = false;
                            break;
                        }
                        
                    }else if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.LearnerState){
                        
                        /*
                        generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.LearnerState learnerStateCond = 
                                (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.LearnerState)condType;
                        
                        String attributeName = learnerStateCond.getAttribute();
                        String attributeValue = learnerStateCond.getValue();
                        LearnerStateAttributeNameEnum stateAttr = LearnerStateAttributeNameEnum.valueOf(attributeName);
                        AbstractEnum stateAttrValue = stateAttr.getAttributeValue(attributeValue);
                        */
                        
                        throw new RuntimeException("Learner state path condition logic hasn't been implemented yet.  Therefore no path will be chosen from the authored branch named '"+authoredBranch.getTransitionName()+"'.");

                    }else if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.KnowledgeAssessment){
                        
                        /*
                        generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.KnowledgeAssessment knowledgeAssessmentCond = 
                                (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.KnowledgeAssessment)condType;
                        
                        String conceptName = knowledgeAssessmentCond.getConcept();
                        generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.KnowledgeAssessment.Expertise expertise = 
                                knowledgeAssessmentCond.getExpertise();
                        */
                        
                        throw new RuntimeException("Knowledge assessment path condition logic hasn't been implemented yet.  Therefore no path will be chosen from the authored branch named '"+authoredBranch.getTransitionName()+"'.");

                    }else{
                        
                        logger.error("Found unhandled path condition type of '"+condition+"' in authored branch course object named '"+authoredBranch.getTransitionName()+"'.");
                        
                        satisfiedAll = false;
                        break;
                    }
                    
                }//end for on conditions for this path
                
                if(satisfiedAll){
                    //this path's conditions where satisfied
                    
                    if(condTypes.size() > highestNumOfCond){
                        //new highest
                        
                        highestNumOfCond = condTypes.size();
                        equallySatisfiedPaths.clear();
                        equallySatisfiedPaths.add(path);
                        
                    }else if(condTypes.size() == highestNumOfCond){
                        //another path with the same number of conditions satisfied
                        equallySatisfiedPaths.add(path);
                    }else{
                        //although satisfied, the number of conditions is less than the current
                        //highest number of conditions satisfied
                        logger.info("Not considering the path named '"+path.getName()+"' because it has "+condTypes.size()+" conditions satisfied which is less than the current best of "+highestNumOfCond+" conditions satisfied.");
                    }
                }
                
            }else{
                //ERROR - only other element at this level is custom percent and 
                //        that should be handled way above in this method
                callback.onFailure(
                        new DetailedException("Unable to select the appropriate path for the learner to take in the Authored branch named '"+authoredBranch.getTransitionName()+"'.", 
                                "Found an unexpected path condition type of '"+condition+"'.", null));
            }
        }//end for on paths
        
        generated.course.AuthoredBranch.Paths.Path selectedPath = null;
        if(equallySatisfiedPaths.isEmpty()){
            //select default path
            selectedPath = defaultPath;
            
        }else if(equallySatisfiedPaths.size() == 1){
            selectedPath = equallySatisfiedPaths.get(0);
            
        }else{
            //there is more than 1 satisfied path, all with the same number of satisfied conditions:               
            // - choose default path                
            selectedPath = defaultPath;
        }
        
        return selectedPath;
    }
    
    /**
     * Selects a path from an authored branched based on branch history evaluation
     * 
     * @param authoredBranch the authored branch
     * @param pathMapById the map of the path id's to the path
     * @param choice the choice of distribution
     * @param branchHistoryPaths the list of branch history paths
     * @param domainSessionInfo the domain session 
     * @param callback the callback for when the branch evaluation has been completed
     * @return the next path the authored branch should take
     */
    private static Path handleBranchHistoryEvaluation(final generated.course.AuthoredBranch authoredBranch, 
            Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById, 
            Serializable choice, List<BranchPathHistory> branchHistoryPaths, 
            final DomainSession domainSessionInfo, AsyncActionCallback callback){
        
        final AsyncReturnBlocker<generated.course.AuthoredBranch.Paths.Path> returnBlocker = new AsyncReturnBlocker<>();
        
        DomainModule.getInstance().sendBranchPathHistoryInfoRequest(domainSessionInfo.getDomainSessionId(), 
                branchHistoryPaths, new MessageCollectionCallback() {
                    
                    //branch history for this course from the UMS db
                    List<BranchPathHistory> dbBranchPathsHistory;
            
                    @Override
                    public void success() {
                        
                        if(choice instanceof generated.course.AuthoredBranch.SimpleDistribution.Balanced){
                            //balanced - choose path with the lowest count, otherwise choose first
                            
                            BranchPathHistory lowestBranchPathHistory = null;
                            for(BranchPathHistory branchPathHistory : dbBranchPathsHistory){
                                
                                if(lowestBranchPathHistory == null || branchPathHistory.getCnt() < lowestBranchPathHistory.getCnt()){
                                    lowestBranchPathHistory = branchPathHistory;
                                }
                            }                                
                            
                            if(lowestBranchPathHistory == null){
                                //ERROR
                                
                                callback.onFailure(
                                        new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.", 
                                                "After searching "+dbBranchPathsHistory.size()+" paths of the current authored branch course object, no path was choosen.", null));
                                return;
                            }
                            
                            //find the path by id
                            generated.course.AuthoredBranch.Paths.Path selectedPath = pathMapById.get(lowestBranchPathHistory.getPathId());
                            returnBlocker.setReturnValue(selectedPath);
                            logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                                    " (id "+authoredBranch.getBranchId()+") based on a balanced distribution.  That branch has had "+lowestBranchPathHistory.getCnt()+" learners.");

                        }else{
                            //custom - choose path based on % for each path
                            
                            generated.course.AuthoredBranch.Paths.Path selectedPath = deteremineCustomPercentPath(dbBranchPathsHistory, pathMapById);
                            returnBlocker.setReturnValue(selectedPath);
                            logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                                    " (id "+authoredBranch.getBranchId()+") based on a custom distribution.");
                            
                        }//end custom

                    }
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void received(Message msg) {
                        
                        dbBranchPathsHistory = (List<BranchPathHistory>) msg.getPayload();
                    }
                    
                    @Override
                    public void failure(String why) {

                        returnBlocker.setReturnValue(null);
                        callback.onFailure(new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.", 
                                "There was a problem requesting the lasted branch history information from the UMS database.  The reason reads:\n"+why, null));
                    }
                    
                    @Override
                    public void failure(Message msg) {

                        returnBlocker.setReturnValue(null);
                        callback.onFailure(new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.", 
                                "There was a problem requesting the lasted branch history information from the UMS database.  The response message was:\n"+msg, null));
                    }
                });
        
        return returnBlocker.getReturnValue();
    }
    
    /**
     * Requests the LMS for course records of completed lessons
     * 
     * @param publishedScores collection of identifiable information for published score located in the LMS
     * @param callback The callback for the message's response.
     */
    private static void sendLMSDataRequest(final Collection<PublishLessonScoreResponse> publishedScores, 
            final DomainSession domainSessionInfo, String lmsUsername, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendLmsDataRequest(
                domainSessionInfo.getDomainSessionId(), 
                lmsUsername, 
                publishedScores, 
                callback);
    }
    
    private static boolean checkDkfScoringPathCondition(
            generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring dkfScoringCond,
            Map<PublishLessonScoreResponse, Date> publishedScoreToEventIdMap,
            LMSCourseRecords lmsCourseRecords){
        
//      throw new RuntimeException("DKF Scoring path condition logic hasn't been implemented yet.");
              
        String nodeName = dkfScoringCond.getTaskconcept();
        
        for(LMSCourseRecord record : lmsCourseRecords.getRecords()) {
            
            CourseRecordRef recordRef = record.getCourseRecordRef();
            LMSConnectionInfo lmsInfo = record.getLMSConnectionInfo();
            
            for(PublishLessonScoreResponse publishedScore : publishedScoreToEventIdMap.keySet()){
                                
                CourseRecordRef ref = publishedScore.getPublishedRecordsByLMS().get(lmsInfo);
                if(recordRef.equals(ref)){
                    //found matching LMS and the matching record Id in this publish score instance
                    
                    GradedScoreNode gradedScoreNode = record.getRoot();
                    
                    AssessmentLevelEnum assessment = gradedScoreNode.getGradeByName(nodeName);
                    if(assessment != null){
                        
                        return assessment.getName().equals(dkfScoringCond.getAssessment());
                    }
                }
                
            }
        }
      
        return false;      
    }
    
    /**
     * Select the appropriate path based on custom percentage values defined for the current authored branch
     * course object.
     * 
     * @param dbBranchPathsHistory information about the paths learners have taken in the past.  This is needed
     * to determine with path this learner should take.
     * @param pathMapById paths defined in the current authored branch course object
     * @return the path the learner should take next.  Will be null if there was a problem.
     */
    private static generated.course.AuthoredBranch.Paths.Path deteremineCustomPercentPath(List<BranchPathHistory> dbBranchPathsHistory, 
            Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById){
        
        //get total participants from all paths of this branch defined in the course
        int totalParticipants = 0;
        int[] cnts = new int[dbBranchPathsHistory.size()];
        double[] distributions = new double[dbBranchPathsHistory.size()];
        for(int index = 0; index < dbBranchPathsHistory.size(); index++){
            
            BranchPathHistory branchPathHistory = dbBranchPathsHistory.get(index);
            totalParticipants += branchPathHistory.getCnt();
            
            cnts[index] = branchPathHistory.getCnt();
            
            //find the path distribution
            generated.course.AuthoredBranch.Paths.Path path = pathMapById.get(branchPathHistory.getPathId());
            distributions[index] = ((BigDecimal)path.getCondition().getCustomPercentOrLearnerCentric()).doubleValue() / 100.0;                                        
        } 
        
        //determine which path should be chosen based on...
        // brute force (for now), i.e. trying to add one to each path count to find 
        // the closest outcome to the desired percentages
        
        int bestIndex = 0;
        double leastTotalOff = Double.MAX_VALUE, totalOff;
        for(int i = 0; i < cnts.length; i++){
            
            totalOff = 0;
            
            for(int j = 0; j < cnts.length; j++){
                
                if(i == j){
                    //adding to this cnt
                    
                    totalOff += Math.abs(distributions[j] - ((cnts[j] + 1.0) / (totalParticipants + 1.0)) );
                    
                }else{
                    
                    totalOff += Math.abs(distributions[j] - ((cnts[j]) / (totalParticipants + 1.0)) );
                }
            }
            
            if(totalOff < leastTotalOff){
                // better selection found
                
                bestIndex = i;
                leastTotalOff = totalOff;
            }
        }
        
        return pathMapById.get(dbBranchPathsHistory.get(bestIndex).getPathId());
    }
    
    /**
     * Check whether the path's condition provided from the current authored branch course object is satisfied based on the 
     * current history of survey responses made by this learner in this course execution.
     * 
     * @param surveyResponseCond the authored survey response condition to check for an authored branch course object's path.  Essentially
     * does the learner's answer to the survey question match the survey response the author is looking for in this path's condition. 
     * @return true iff the condition is satisfied, meaning this path should be considered as the next path for the learner in the course.
     */
    private static boolean checkSurveyResponsePathCondition(
            generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse surveyResponseCond,
            final List<SurveyResponse> surveyResponses){
        
        int surveyId = surveyResponseCond.getSurvey().intValue();
        boolean foundSurvey = false;
        
        //start from the latest survey response as it is possible the same survey could be
        //given multiple times in a course and the latest response should be applied to the path condition
        ListIterator<SurveyResponse> itr = surveyResponses.listIterator(surveyResponses.size());
        while(itr.hasPrevious()){

            SurveyResponse surveyResponse = itr.previous();
            
            if(surveyId == surveyResponse.getSurveyId()){
                //found the survey, now find the question
                
                foundSurvey = true;
                int questionId = surveyResponseCond.getSurveyQuestion().intValue();
                
                boolean foundQuestion = false;
                for(SurveyPageResponse surveyPageResponse : surveyResponse.getSurveyPageResponses()){
                    
                    for(AbstractQuestionResponse questionResponse : surveyPageResponse.getQuestionResponses()){
                        
                        if(questionId == questionResponse.getSurveyQuestion().getId()){
                            //found the question, now check the response
                            
                            foundQuestion = true;
                            
                            Serializable answerType = surveyResponseCond.getAnswer().getAnswerType();
                            
                            if(answerType instanceof Selection){
                                //check all the path condition's answers against the learner's answers
                                
                                Selection selection = (Selection)answerType;
                                
                                //the path's condition number of answers must match the learner's number of answers
                                if(selection.getChoice().size() != questionResponse.getResponses().size()){
                                    break;
                                }
                                
                                boolean match = false;
                                for(BigInteger choiceId : selection.getChoice()){
                                    
                                    match = false;
                                    
                                    for(QuestionResponseElement qResponse :  questionResponse.getResponses()){
                                        
                                        for(ListOption listOption : qResponse.getChoices().getListOptions()){
                                            
                                            if(qResponse.getText() != null && qResponse.getText().equals(listOption.getText())){
                                                //found matching choice text
                                                
                                                if(listOption.getId() == choiceId.intValue()){
                                                    //match
                                                    match = true;
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        if(match){
                                            break;
                                        }
                                    }
                                    
                                    if(!match){
                                        //learner didn't select this choice as an answer, therefore condition failed
                                        break;
                                    }
                                }
                                
                                //if loop ended with match = true than all choices were matched
                                if(match){
                                    return true;
                                }
                                
                            }else{
                                //a simple value
                                
                                //only allowed 1 simple string value to compare in this condition so
                                //the question answer can only have 1 value as well
                                if(questionResponse.getResponses().size() != 1){
                                    break;
                                }
                                
                                for(QuestionResponseElement qResponse : questionResponse.getResponses()){
                                    
                                    if(answerType.equals(qResponse.getText())){
                                        //satisfied condition
                                        return true;
                                    }
                                }                                                  
                                 
                            }
                            
                            break;
                        }
                    }
                    
                    if(foundQuestion){
                        break;
                    }
                }
                
                break;
            }
            
            if(foundSurvey){
                break;
            }
        }//end while
        
        return false;
    }
    
    private static class LMSCourseRecordsWrapper{
        
        public LMSCourseRecords lmsCourseRecords;
    }
}
