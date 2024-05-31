/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.survey;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters.QuestionTypeParameter;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckRequest.Question;
import mil.arl.gift.common.SurveyCheckRequest.Reply;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyCheckResponse.FailureResponse;
import mil.arl.gift.common.SurveyCheckResponse.ResponseInterface;
import mil.arl.gift.common.SurveyCheckResponse.SuccessResponse;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.util.CaseInsensitiveList;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.ums.db.HibernateObjectConverter;
import mil.arl.gift.ums.db.HibernateObjectReverter;
import mil.arl.gift.ums.db.SurveyContextUtil;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbFolder;
import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbPropertyKey;
import mil.arl.gift.ums.db.table.DbPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionCategory;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionResponse;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyElementProperty;
import mil.arl.gift.ums.db.table.DbSurveyElementType;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyPageProperty;
import mil.arl.gift.ums.db.table.DbSurveyProperty;
import mil.arl.gift.ums.db.table.DbSurveyResponse;

/**
 * Class used to help interact with survey tables.
 * 
 * @author cragusa
 *
 */
public class Surveys {
    
    private static Logger logger = LoggerFactory.getLogger(Surveys.class);
    
    /** table property names */
    public static final String QUESTION_ID_PROPERTY                = "question.questionId";
    public static final String SURVEY_ELEMENT_ID_PROPERTY          = "surveyElement.surveyElementId";
    public static final String SURVEY_ELEMENT_QUESTION_ID          = "questionId";
    public static final String QUESTION_RESPONSE_QUESTION_ID       = "surveyQuestion.questionId";
    public static final String SURVEY_PAGE_ID_PROPERTY             = "surveyPage.surveyPageId";
    public static final String SURVEY_ID_PROPERTY                  = "survey.surveyId";
    public static final String PROPERTY_KEY_ID_PROPERTY            = "propertyKey.id";    
    public static final String LIST_OPTION_OPTION_LIST_ID_PROPERTY = "optionList.optionListId";    
    public static final String QUESTION_CATEGORY_QUESTION_ID_PROPERTY              = "question.questionId";    
    public static final String SURVEY_CONTEXT_SURVEY_SURVEY_CONTEXT_ID_PROPERTY    = "surveyContext.surveyContextId";
    public static final String SURVEY_CONTEXT_SURVEY_GIFT_KEY_PROPERTY = "giftKey";
    
    private static final File DATA_FOLDER = new File("data");
    
    /** used for hibernate queries to ignore start index and max results parameters */
    public static final int UNUSED_INDEX = -1;
    
    /** instance of the UMS database manager used to perform database operations */
    private static UMSDatabaseManager dbMgr = null;
    
    /** used to convert hibernate table classes to their common class representation */
    private static HibernateObjectReverter hibernateToGift = null;
    
    /** used to convert common classes to hibernate table classes */
    private static HibernateObjectConverter giftToHibernate = null;
    static{
        
        try{
            dbMgr = UMSDatabaseManager.getInstance();
            hibernateToGift = new HibernateObjectReverter(UMSDatabaseManager.getInstance());
            giftToHibernate = new HibernateObjectConverter(UMSDatabaseManager.getInstance());
        }catch(ConfigurationException e){
            logger.error("There was a problem getting the UMS database manager instance", e);
        }
    }
        
    /** the extension for exported surveys */
    public static final String SURVEY_EXPORT_EXTENSION = ".survey";
    
    /** protected constructor - all methods should be static */
    protected Surveys(){}
    
    /**
     * Creates a new hibernate session that can be used to group sql commands into 
     * a single atomic transaction.  Make sure to close the session when finished.
     * 
     * @return the new hibernate session.  the session will already be opened for you.
     */
    public static Session createSession(){
        return UMSDatabaseManager.getInstance().createNewSession(); 
    }
    
    /**
     * Check the survey references provided against the Survey database entries.
     * 
     * @param checkRequest contains a collection of survey references that need to be verified against the survey database.
     * @return the result of the check.  Can include failure information on a per survey check request.  Will not be null.
     * @throws IllegalArgumentException if there was a problem with a provided parameter
     * @throws SurveyValidationException if there was a critical problem with one of the survey check requests.  A critical problem is rare and doesn't
     * include logic like the survey element specified doesn't exist, not enough questions for a knowledge assessment survey)
     */
    public static SurveyCheckResponse checkSurveyReferences(SurveyListCheckRequest checkRequest) throws IllegalArgumentException, SurveyValidationException{
        
        if(checkRequest == null){
            throw new IllegalArgumentException("The check request can't be null.");
        }
        
        Map<String, List<ResponseInterface>> responsesMap = new HashMap<>();
        
        // The kaSurveyMap keeps a list of knowledge assessment surveys that have been retrieved from the database for this request.  
        // This is to prevent multiple fetches of the same knowledge assessment survey from the database within the context of a single surveycheck request.
        HashMap<Integer, Survey> kaSurveyMap = new HashMap<Integer, Survey>();
        for(String courseId : checkRequest.getRequests().keySet()){                
            
            //the requests associated with a course id
            List<SurveyCheckRequest> requests = checkRequest.getRequests().get(courseId);
            
            //where to put the responses to the requests
            List<ResponseInterface> responsesList = new ArrayList<>(requests.size());

            // Maps processed requests to their responses
            Map<SurveyCheckRequest, ResponseInterface> requestResponse = new HashMap<>();
            
            for(SurveyCheckRequest request : requests){
                boolean requestHandled = false;
                
                if(requestResponse.containsKey(request)) {
                    responsesList.add(requestResponse.get(request));
                    continue;
                }
                
                if(request.getGiftKey() == null){
                    //this is a check if the survey context exists 
                    
                    ResponseInterface response;
                    
                    if(!Surveys.doesSurveyContextExist(request.getSurveyContextId())){
                        
                        response = new FailureResponse("Unable to find the course survey context with id "+request.getSurveyContextId()
                                + ". The course survey context needs to be authored in the Survey Authoring System.", request.getCourseObjectIndex());
                        ((FailureResponse)response).setCourseObjectIndex(request.getCourseObjectIndex());
                        responsesList.add(response);
                    }else{
                        response = new SuccessResponse();
                        responsesList.add(response);
                    }                   
                    
                    //this check is done...
                    requestResponse.put(request, response);
                    continue;
                }
                
                
                
                if(request.getGiftKey().equals(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY)){
                    //this could be a Branch Point course element's Recall Survey based on the gift key OR someone used that special gift key
                    
                    if(request.getKnowledgeAssessmentSurveyRequest() != null){
                        //since the request contains a knowledge assessment survey request we now know this is a generate survey request,
                        //try to build the survey based on the request's information.
                        
                        GetKnowledgeAssessmentSurveyRequest knowledgeAssessmentRequest = request.getKnowledgeAssessmentSurveyRequest();
                        try{

                            // Optimization to only fetch the knowledge assessment survey once from the db.
                            Integer surveyContextId = request.getSurveyContextId();

                            if (kaSurveyMap.get(surveyContextId) == null) {
                                Survey newConceptSurvey = Surveys.getSurveyContextSurvey(surveyContextId, mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
                                kaSurveyMap.put(surveyContextId,  newConceptSurvey);
                            }
                            
                            
                            // Get the concept survey (which validates to make sure there are enough questions based on the concepts in the request).  
                            Survey conceptSurvey = kaSurveyMap.get(surveyContextId);
                            Surveys.validateConceptsSurvey(conceptSurvey, request.getSurveyContextId(), knowledgeAssessmentRequest.getConcepts());
                            
                        }catch(DetailedException e){
                            //the survey was unable to be built based on the request's parameters
                            FailureResponse failure = new FailureResponse(e.getReason() + "\n\n" + e.getDetails(), request.getCourseObjectIndex());
                            responsesList.add(failure);
                            
                            requestResponse.put(request, failure);
                            continue;
                        }                        
                        
                        SuccessResponse success = new SuccessResponse();
                        responsesList.add(success);
                        
                        //this check is done...
                        requestResponse.put(request, success);
                        continue;
                    }

                }
                
                mil.arl.gift.common.survey.Survey survey = Surveys.getSurveyContextSurvey(request.getSurveyContextId(), request.getGiftKey());
                
                if(survey == null){
                    //failure: survey was not found
                    FailureResponse failure = new FailureResponse("Unable to find the survey with the gift key of '" + request.getGiftKey() + "' in the course survey context with id " + request.getSurveyContextId() +
                          ". The course survey context needs to contain a survey with that key in order to know which survey to present.", request.getCourseObjectIndex());
                    failure.setCourseObjectIndex(request.getCourseObjectIndex());
                    responsesList.add(failure);
                    
                    requestResponse.put(request, failure);
                    continue;
                }
                
                boolean questionFound;
                for(Question question : request.getQuestions()){
                    questionFound = false;
                    
                    for(SurveyPage page : survey.getPages()){
                        
                        for(AbstractSurveyElement element : page.getElements()){
                            
                            if(element instanceof AbstractSurveyQuestion){
                                
                                int elementQuestionId;
                                if(element instanceof MultipleChoiceSurveyQuestion){
                                    //only support checking multiple choice question types right now...
                                    
                                    MultipleChoiceSurveyQuestion multipleChoiceSurveyQuestion = (MultipleChoiceSurveyQuestion)element;
                                    MultipleChoiceQuestion multipleChoiceQuestion = multipleChoiceSurveyQuestion.getQuestion();
                                    elementQuestionId = multipleChoiceSurveyQuestion.getId();
                                    
                                    //check question id
                                    if(question.getQuestionId() == elementQuestionId){
                                        //found match
                                        questionFound = true;
                                        
                                        //now check for replies
                                        for(Reply reply : question.getReplies()){
                                            boolean replyFound = false;
                                            
                                            OptionList optionlist = multipleChoiceQuestion.getReplyOptionSet();
                                            for(ListOption option : optionlist.getListOptions()){
                                                
                                                if(reply.getReplyId() == option.getId()){
                                                    //found match
                                                    replyFound = true;
                                                    break;
                                                }
                                            }
                                            
                                            if(!replyFound){   
                                                FailureResponse failure = new FailureResponse("Unable to find a reply w/ id of "+reply.getReplyId()
                                                        +" in survey question w/ id of "+question.getQuestionId()+" in survey with survey context of "
                                                        +request.getSurveyContextId()+" and GIFT key of "+request.getGiftKey()+".", request.getCourseObjectIndex());
                                                failure.setCourseObjectIndex(request.getCourseObjectIndex());
                                                responsesList.add(failure);
                                                requestHandled = true;
                                                
                                                //no need to keep trying to match replies, there was a failure.
                                                requestResponse.put(request, failure);
                                                break;
                                            }
                                            
                                        }//end for reply
                                        
                                        
                                        if(!requestHandled){
                                            //success! - the question was found and the replies were found.  Handle the success response later...
                                            break;
                                        }
                                            
                                    }
                                    
                                }                                   
                                
                            }//end if
                            
                            if(questionFound){
                                //no need to keep searching for the survey question, it was found and checked
                                break;
                            }
                            
                        }//end for AbstractSurveyElement
                        
                        if(!requestHandled && questionFound){
                            //success! - this means the question was found and the replies were found
                            SuccessResponse success = new SuccessResponse();
                            responsesList.add(success);
                            requestHandled = true;
                            
                            //no need to keep searching the surveypages for the question if the request was handled
                            requestResponse.put(request, success);
                            break;
                            
                        }else if(requestHandled){
                            
                            //no need to keep searching the surveypages for the question if the request was handled
                            break;
                        }
                        
                    }//end for surveypage
                    
                    if(!questionFound){
                        //question was not found
                        FailureResponse failure = new FailureResponse("Unable to find the survey question w/ id of "+question.getQuestionId()
                                +" in survey with course survey context of "+request.getSurveyContextId()+" and GIFT key of "+request.getGiftKey()+".", 
                                request.getCourseObjectIndex());
                        failure.setCourseObjectIndex(request.getCourseObjectIndex());
                        responsesList.add(failure);
                        requestHandled = true;
                        
                        //don't keep checking questions because they probably won't be found either
                        requestResponse.put(request, failure);
                        break;
                        
                    }else if(requestHandled){
                        
                        //no need to keep searching the surveypages for the question if the request was handled
                        break;
                    }
                    
                }//end for Question
                
                if(!requestHandled){
                    //success! - this means the request had no questions but the survey was found
                    SuccessResponse success = new SuccessResponse();
                    responsesList.add(success);
                    requestHandled = true;
                    requestResponse.put(request, success);
                }
                
            }//end for requests
            
            if(requests.size() != responsesList.size()){
                throw new SurveyValidationException("Collected "+responsesList.size()+" responses to "+requests.size()+" requests for "+requests+" with key of "+courseId+".  The responses must equal the number of requests.  Something went wrong.");
            }
            
            responsesMap.put(courseId, responsesList);
            
        }//end for checkRequests
        
        SurveyCheckResponse response = new SurveyCheckResponse(responsesMap);
        return response;
    }
    
    /**
     * Get the db question with the unique id
     * 
     * @param questionId - the key to finding the question in the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return DbQuestion - the question with the given id, null if not found
     */
    protected static DbQuestion getDbQuestion(int questionId, Session session){
        
        if (session == null) {
            return dbMgr.selectRowById(questionId, DbQuestion.class);
        } else {
            return dbMgr.selectRowById(questionId, DbQuestion.class, session);
        }
    }
    
    /**
     * Get the db survey with the unique id
     * 
     * @param surveyId the key to finding the survey in the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return the db survey object with the given id, null if not found
     */
    protected static DbSurvey getDbSurvey(int surveyId, Session session){
        
        if (session == null) {
            return dbMgr.selectRowById(surveyId, DbSurvey.class);
        } else {
            return dbMgr.selectRowById(surveyId, DbSurvey.class, session);
        }
    }
    
    /**
     * Get the db survey page with the unique id
     * 
     * @param surveyPageId the key to finding the survey page in the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return the db survey object with the given id, null if not found
     */
    protected static DbSurveyPage getDbSurveyPage(int surveyPageId, Session session){
        
        if (session == null) {
            return dbMgr.selectRowById(surveyPageId, DbSurveyPage.class);
        } else {
            return dbMgr.selectRowById(surveyPageId, DbSurveyPage.class, session);
        }
    }
    
    /**
     * Get the common question object populated with the db question with the unique id
     * 
     * @param questionId the question to find in the database
     * @return the common question object.  Will be null if the question wasn't found.
     */
    public static AbstractQuestion getQuestion(int questionId){        
        return hibernateToGift.convertQuestion(getDbQuestion(questionId, null));
    }
    
    /**
     * Get the db survey question (a question in a survey, not a direct reference to a survey) with the unique id
     * 
     * @param surveyQuestionId - the key to finding the survey question in the database
     * @return DbSurveyElement - the survey question with the given id, null if not found
     */
    protected static DbSurveyElement getDbSurveyQuestion(int surveyQuestionId, Session session){
        
        if (session == null) {
            return dbMgr.selectRowById(surveyQuestionId, DbSurveyElement.class);
        } else {
            return dbMgr.selectRowById(surveyQuestionId, DbSurveyElement.class, session);
        }
    }
    
    /**
     * Get the common survey question populated with the db survey question with the unique id.
     * 
     * @param surveyQuestionId a survey question id to find in the database.  Note this is not
     * a question id but a survey question id.
     * @return the survey question object.  Will be null if the survey question could not be found
     * or the survey element with the given id is not a survey question but another type of survey element.
     */
    public static AbstractSurveyQuestion<?> getSurveyQuestion(int surveyQuestionId){
        
        DbSurveyElement dbSurveyQuestion = getDbSurveyQuestion(surveyQuestionId, null);
        if(dbSurveyQuestion != null){
            AbstractSurveyElement surveyElement = hibernateToGift.convertSurveyElement(dbSurveyQuestion);
            if(surveyElement instanceof AbstractSurveyQuestion){
                return (AbstractSurveyQuestion<?>)surveyElement;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieve the list of survey context survey entries in the UMS database as common class representations.
     * Note: this will remove knowledge assessment surveys that were generated for this survey context (if any)
     * from the return collection.
     * 
     * @param surveyContextId the key to finding the survey context survey entries 
     * @return List<SurveyContextSurvey> - the survey context survey common classes representing the rows in tables in the UMS database
     */
    public static List<SurveyContextSurvey> getSurveyContextSurveys(int surveyContextId){
        
        Set<DbSurveyContextSurvey> dbSurveyContextSurveys = getDbSurveyContextSurveys(surveyContextId, null);
        
        List<SurveyContextSurvey> surveyContextSurveys = new ArrayList<>(dbSurveyContextSurveys.size());
        for(DbSurveyContextSurvey dbSurveyContextSurvey : dbSurveyContextSurveys){

            //remove knowledge assessment surveys from the survey context surveys being returned for this survey context
            if(dbSurveyContextSurvey.getGiftKey().matches(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX)
                    || dbSurveyContextSurvey.getGiftKey().equals(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY)) {
                continue;
            }

            surveyContextSurveys.add(hibernateToGift.convertSurveyContextSurvey(dbSurveyContextSurvey));
        }
        
        return surveyContextSurveys;
    }
    
    /**
     * Retrieve the list of survey context entries in the UMS database as common class representations that
     * the user has visible access to.</br>
     * Notes:</br>
     * 1. this will remove knowledge assessment surveys that were generated for this survey context (if any)
     * from the return collection.</br>
     * 2. this can be an expensive call so be cautious when calling this method.
     * 
     * @param username the name to use to filter the survey context based on what is visible to this user.
     * If null, all survey context will be returned (Note this can be an even more expensive call and should
     * not be used frequently).
     * @return List<SurveyContext> - the survey context common classes representing the rows in tables in the UMS database
     */
    public static ArrayList<SurveyContext> getSurveyContexts(String username){
        // Open up a transaction here since the contexts are lazy fetched.
        Session session = dbMgr.getCurrentSession();
        try{
            session.beginTransaction();
            List<DbSurveyContext> dbSurveyContexts = getDbSurveyContexts(username, session);
    
            // get context ids from each dbSurveyContext, this will be used to retrieve the converted
            // survey contexts outside of this transaction.
            List<Integer> contextIds = new ArrayList<Integer>();
            for (DbSurveyContext dbSurveyContext : dbSurveyContexts) {
                contextIds.add(dbSurveyContext.getSurveyContextId());
            }
    
            if (session.isOpen()) {
                session.close();
            }
    
            // Gets the converted survey context for each survey context id
            ArrayList<SurveyContext> surveyContexts = new ArrayList<SurveyContext>(dbSurveyContexts.size());
            for (Integer surveyContextId : contextIds) {
                DbSurveyContext dbContext = SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr);
                surveyContexts.add(hibernateToGift.convertSurveyContext(dbContext, true));
            }
    
            return surveyContexts;
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Return the survey contexts from the database that the user has visibility on.
     * 
     * @param userName the user name to use to check for visibility on survey contexts in the database.<br/>  
     * If null, all survey context will be returned<br/> 
     * Note: this can be an expensive call if you try to load all survey contexts in the db
     * 
     * @return the collection of survey contexts this user can see
     * @throws DetailedException if there was a problem with the query
     */
    private static List<DbSurveyContext> getDbSurveyContexts(String userName, Session session) throws DetailedException{
        
        if(userName == null || userName.isEmpty()){          
          List<DbSurveyContext> dbSurveyContexts = dbMgr.selectAllRows(DbSurveyContext.class, session);
          return dbSurveyContexts;
        }
        
        try {
            
            final String queryString = "from DbSurveyContext as SurveyContext where '*' in elements(SurveyContext.visibleToUserNames) or '" + 
                            userName + "' in elements(SurveyContext.visibleToUserNames)";
            
            return dbMgr.selectRowsByQuery(DbSurveyContext.class, queryString, -1, -1, session);


        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the survey contexts.", 
                    "There was a problem while retrieving the survey contexts for username '"+userName+"'.  The exception reads:\nd"+e.getMessage(), e);
        }
    }
    
    /**
     * Return the DbSurveyContextSurvey instances for the survey context id provided.<br/>
     * This will filter out the generated surveys for question banks but will include the question bank survey itself if present.
     * 
     * @param surveyContextId - the key to finding the collection of SurveyContextSurvey instances
     * @param session - the session to perform the work in.  If null a new session transaction will be created and managed and the session closed in this method.
     * @return Set<SurveyContextSurvey> collection of DbSurveyContextSurveys found using the given survey context id.
     * Includes the question bank survey but not any generated surveys for question banks.  Can return null if the survey context
     * wasn't found in the db. 
     */
    private static Set<DbSurveyContextSurvey> getDbSurveyContextSurveys(int surveyContextId, Session session){
        
        DbSurveyContext sContext;
        if(session == null){
            sContext = SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr);
        }else{
            sContext = SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr, session);
        }

        if (sContext == null) {
            logger.error("Unable to find a survey context entry for survey context id = " + surveyContextId);
            return null;
        }else{
            return sContext.getSurveyContextSurveys();
        }        
    }
    
    /**
     * Creates a new folder in the database
     *
     * @param folder The folder to create in the database
     * @throws Exception if there was a problem inserting the folder
     */
    public static void insertFolder(Folder folder) throws Exception {

        DbFolder dbFolder = new DbFolder(folder.getName());
        DbFolder existingFolder = dbMgr.selectRowByExample(dbFolder, DbFolder.class);

        if(existingFolder != null) {

            StringBuilder reason = new StringBuilder();
            String details = "Folders with duplicate names are currently not allowed in the database.";

            reason.append("A folder with the name '").append(folder.getName()).append("' already exists");

            if(existingFolder.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD) 
            || existingFolder.getVisibleToUserNames().containsAll(folder.getVisibleToUserNames())) {

                reason.append(". ");

            } else {
                reason.append(", however you do not have permission to view this folder. ");
            }

            reason.append("Please enter a different name and try again.");

            throw new DetailedException(reason.toString(), details, null);              
        }

        dbFolder.setEditableToUserNames(folder.getEditableToUserNames());
        dbFolder.setName(folder.getName());
        dbFolder.setVisibleToUserNames(folder.getVisibleToUserNames());

        dbMgr.insertRow(dbFolder);
    }

    /**
     * Inserts a category into the database
     *
     * @param category The category to insert into the database
     * @throws Exception if there was a problem inserting the row
     */
    public static void insertCategory(Category category) throws Exception {

        DbCategory dbCategory = new DbCategory(category.getName());
        DbCategory existingCategory = dbMgr.selectRowByExample(dbCategory, DbCategory.class);

        if(existingCategory != null) {

            StringBuilder reason = new StringBuilder();
            String details = "Duplicate category names are not currently allowed in the database.";

            reason.append("A category with the name '").append(category.getName()).append("' already exists");

            if(existingCategory.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD) 
            || existingCategory.getVisibleToUserNames().containsAll(category.getVisibleToUserNames())) {
                
                reason.append(". ");
            } else {
                reason.append(", however you do not have permission to view this category. ");
            }

            reason.append("Please enter a different name and try again.");

            throw new DetailedException(reason.toString(), details, null); 
        }

        dbCategory.setCategoryId(category.getId());
        dbCategory.setEditableToUserNames(category.getEditableToUserNames());
        dbCategory.setName(category.getName());
        dbCategory.setVisibleToUserNames(category.getVisibleToUserNames());

        dbMgr.insertRow(dbCategory);

    }
    
    /**
     * Copy an existing survey context in the db.
     * 
     * @param surveyContextId the unique id of the survey context to copy
     * @param username the user to give read and write access on the new survey context
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param session the transaction to perform the inserts in
     * @return the new survey context id
     * @throws Exception if there was a problem copying the survey context and its elements
     */
    public static int copySurveyContext(int surveyContextId, String username, ExternalSurveyMapper mapper, Session session) throws Exception{
        
        DbSurveyContext existingDbSurveyContext = SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr, session);
        if(existingDbSurveyContext == null){
            //ERROR
            throw new Exception("Unable to copy the survey context with id "+surveyContextId+" because it doesn't exist in the database.");
        }

        //create new survey context
        DbSurveyContext newDbSurveyContext = new DbSurveyContext(existingDbSurveyContext.getName());
        newDbSurveyContext.setSurveyContextSurveys(new HashSet<DbSurveyContextSurvey>());
        newDbSurveyContext.setSurveyResponses(new HashSet<DbSurveyResponse>());
        newDbSurveyContext.setVisibleToUserNames(new HashSet<String>(Arrays.asList(username)));
        newDbSurveyContext.setEditableToUserNames(new HashSet<String>(Arrays.asList(username)));
        
        if(!insertOrUpdateRow(newDbSurveyContext, session)){
            //ERROR
            throw new Exception("Unable to insert a copy of the survey context with id "+surveyContextId+" because the insert database operation failed.");
        }
        
        //add change in survey context id to mapper
        if(mapper != null){
            mapper.addSurveyContext(surveyContextId, newDbSurveyContext.getSurveyContextId());
        }
        
        ArrayList<String> surveyContextKeys = new ArrayList<String>();
        
        //now insert the surveys
        for(DbSurveyContextSurvey scs : existingDbSurveyContext.getSurveyContextSurveys()){
            
            if (mapper != null && !mapper.surveyExists(scs.getSurvey().getSurveyId())) {
                
                DbSurvey scs_survey = scs.getSurvey();
                
                DbSurvey newDbSurvey;
                try{
                    int newSurveyId = copySurvey(scs_survey.getSurveyId(), username, mapper, session);
                    newDbSurvey = dbMgr.selectRowById(newSurveyId, DbSurvey.class);
                }catch(Exception e){
                    throw new Exception("Unable to insert external survey with id "+scs_survey.getSurveyId()+" in external survey context with id "+existingDbSurveyContext.getSurveyContextId()+".", e);
                }
                
                //now that the survey exists, create new survey context survey
                //Note: this will link the survey context, survey and survey context survey references in all 3 tables at once
                DbSurveyContextSurvey surveyContextSurvey = new DbSurveyContextSurvey(newDbSurveyContext, newDbSurvey, scs.getGiftKey());
                if(!insertOrUpdateRow(surveyContextSurvey, session)){
                    throw new Exception("Unable to create a new survey context survey with id "+existingDbSurveyContext.getSurveyContextId()+".");
                }
               
                mapper.addSurvey(scs.getSurvey().getSurveyId(), newDbSurvey.getSurveyId());
                
                /* keep track of inserted surveys and their survey context keys, since the user can use
                 * unique survey context keys to reference the same survey. */
                mapper.addSurveyContextSurvey(scs.getSurvey().getSurveyId(), newDbSurvey);
                surveyContextKeys.add(scs.getGiftKey());
                
            } else if (mapper != null && !surveyContextKeys.contains(scs.getGiftKey())) {
                // An existing survey is being referenced by a new survey context key
                
                DbSurveyContextSurvey surveyContextSurvey = new DbSurveyContextSurvey(
                        newDbSurveyContext, 
                        mapper.surveyContextSurveyMap.get(scs.getSurvey().getSurveyId()), 
                        scs.getGiftKey());
                
                if(!insertOrUpdateRow(surveyContextSurvey, session)){
                    throw new Exception("Unable to update external survey context with id "+existingDbSurveyContext.getSurveyContextId()+".");
                }
                
            }
        }
        
        return newDbSurveyContext.getSurveyContextId();
        
    }
    
    /**
     * Insert the survey context represented by the common class representation in the survey
     * database. This method will treat the survey context and all primary key ids as new entries in
     * the database, i.e. as an external survey being inserted into the database. Therefore new ids
     * will be generated upon inserting in the database. The mapper will contain the relationship
     * between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param sourceSurveyContextSurvey contains the survey context information to populate database
     *        tables with
     * @param mapper where to maintain information about the ids that have changed during the insert
     *        process
     * @param userName used for write permissions checks. Can't be blank. 
     * @param sharedSurveyGiftKeys a unique set of Survey Context GIFT keys found in the course. The
     *        set should only contain GIFT keys for surveys referenced in survey course objects that
     *        are marked as shared (i.e. not authored in the course but selected from pre-existing
     *        surveys and the original survey is used rather than making a copy for this
     *        course).<br/>
     *        Any surveys found to be shared will not be copied for the new survey context. Instead
     *        a reference will be made to the existing survey.<br/>
     *        A null or empty list can be provided if this functionality is not used.
     * @param session a database session with a transaction (that has already began) to do all the
     *        insert operations in. This is useful for doing rollback operations if any one of the
     *        inserts fails. Can be null if the caller doesn't care about partial fails and wants
     *        this class to manage the session.
     * @return SurveyContext a new survey context containing the new ids created by the database
     * @throws Exception if there was a problem inserting
     */
    public static DbSurveyContextSurvey insertExternalSurveyContextSurvey(SurveyContextSurvey sourceSurveyContextSurvey,
            ExternalSurveyMapper mapper, String userName, Session session) throws Exception {

        if (sourceSurveyContextSurvey == null) {
            throw new IllegalArgumentException("The survey context survey can't be null.");
        } else if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("The parameter 'userName' cannot be blank.");
        }

        int surveyContextId = sourceSurveyContextSurvey.getSurveyContextId();
        if (!isSurveyContextEditable(surveyContextId, userName)) {
            throw new Exception("Failed to insert the survey context survey into the survey context because '"
                    + userName + "' doesn't have write access to the survey context (id : " + surveyContextId + ").");
        }

        DbSurvey dbSurvey = insertExternalSurvey(sourceSurveyContextSurvey.getSurvey(), mapper, session);
        // update survey context survey id with the newly inserted survey id
        sourceSurveyContextSurvey.getSurvey().setId(dbSurvey.getSurveyId());

        /* Note: this will link the survey context, survey and survey context survey references in
         * all 3 tables at once */
        DbSurveyContextSurvey dbSurveyContext = giftToHibernate.convertSurveyContextSurvey(sourceSurveyContextSurvey, session);
        if (!insertOrUpdateRow(dbSurveyContext, session)) {
            // ERROR
            logger.error("Failed to add the survey context survey " + dbSurveyContext.getGiftKey() + ".");
            return null;
        }

        return dbSurveyContext;
    }

    /**
     * Insert the survey context represented by the common class representation in the survey database.  This method
     * will treat the survey context and all primary key ids as new entries in the database, i.e. as an external survey
     * being inserted into the database.  Therefore new ids will be generated upon inserting in the database.  The mapper
     * will contain the relationship between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param sourceSurveyContext contains the survey context information to populate database tables with
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param sharedSurveyGiftKeys a unique set of Survey Context GIFT keys found in the course.  
     * The set should only contain GIFT keys for surveys referenced in survey course objects that are marked as 
     * shared (i.e. not authored in the course but selected from pre-existing surveys and the original survey is 
     * used rather than making a copy for this course).<br/>
     * Any surveys found to be shared will not be copied for the new survey context.  Instead a reference will be made to the existing survey.<br/>
     * A null or empty list can be provided if this functionality is not used.
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return SurveyContext a new survey context containing the new ids created by the database
     * @throws Exception if there was a problem inserting
     */
    public static SurveyContext insertExternalSurveyContext(SurveyContext sourceSurveyContext, ExternalSurveyMapper mapper, 
            Set<String> sharedSurveyGiftKeys, Session session) throws Exception{
        
        if(sourceSurveyContext == null){
            throw new IllegalArgumentException("The survey context can't be null.");
        }
        
        //create new survey context
        DbSurveyContext newSurveyContext = new DbSurveyContext(sourceSurveyContext.getName());
        newSurveyContext.setSurveyContextSurveys(new HashSet<DbSurveyContextSurvey>());
        newSurveyContext.setSurveyResponses(new HashSet<DbSurveyResponse>());
        newSurveyContext.setVisibleToUserNames(sourceSurveyContext.getVisibleToUserNames());
        newSurveyContext.setEditableToUserNames(sourceSurveyContext.getEditableToUserNames());
        
        if(!insertOrUpdateRow(newSurveyContext, session)){
            //ERROR
            logger.error("Unable to insert external survey context with id "+sourceSurveyContext.getId()+".");
            return null;
        }
        
        //add change in survey context id to mapper
        if(mapper != null){
            mapper.addSurveyContext(sourceSurveyContext.getId(), newSurveyContext.getSurveyContextId());
        }
        
        ArrayList<String> surveyContextKeys = new ArrayList<String>();
        
        //now insert the surveys
        for(SurveyContextSurvey scs : sourceSurveyContext.getContextSurveys()){
            
            if(sharedSurveyGiftKeys != null && sharedSurveyGiftKeys.contains(scs.getKey())){                
                //just need to update the new survey context survey to point to the existing/shared survey
                
                DbSurvey existingSurvey = getSurvey(sourceSurveyContext.getId(), scs.getKey());
                if(existingSurvey == null){
                    throw new Exception("Unable to find the shared survey from the source survey context "+sourceSurveyContext.getId()+", mapped to GIFT key "+scs.getKey());
                }
                
                //Note: this will link the survey context, survey and survey context survey references in all 3 tables at once
                DbSurveyContextSurvey surveyContextSurvey = new DbSurveyContextSurvey(newSurveyContext, existingSurvey, scs.getKey());
                if(!insertOrUpdateRow(surveyContextSurvey, session)){
                    //ERROR
                    logger.error("Failed to add the survey "+existingSurvey.getSurveyId() + " to the new survey context being created from the source survey context of "+sourceSurveyContext.getId()+".");
                    return null;
                }
                
                continue;
            }
            
            if (mapper != null && !mapper.surveyExists(scs.getSurvey().getId())) {
                
                Survey scs_survey = scs.getSurvey();
                
                scs_survey.getEditableToUserNames().clear();
                scs_survey.getVisibleToUserNames().clear();
                
                scs_survey.getEditableToUserNames().addAll(sourceSurveyContext.getEditableToUserNames());
                scs_survey.getVisibleToUserNames().addAll(sourceSurveyContext.getVisibleToUserNames());
                
                DbSurvey newSurvey;
                try{
                    newSurvey = insertExternalSurvey(scs_survey, mapper, session);
                }catch(Exception e){
                    throw new Exception("Unable to insert external survey with id "+scs_survey.getId()+" in external survey context with id "+sourceSurveyContext.getId()+".", e);
                }
                
                //now that the survey exists, create new survey context survey
                //Note: this will link the survey context, survey and survey context survey references in all 3 tables at once
                DbSurveyContextSurvey surveyContextSurvey = new DbSurveyContextSurvey(newSurveyContext, newSurvey, scs.getKey());
                if(!insertOrUpdateRow(surveyContextSurvey, session)){
                    //ERROR
                    logger.error("Unable to update external survey context with id "+sourceSurveyContext.getId()+".");
                    return null;
                }
               
                mapper.addSurvey(scs.getSurvey().getId(), newSurvey.getSurveyId());
                
                /* keep track of inserted surveys and their survey context keys, since the user can use
                 * unique survey context keys to reference the same survey. */
                mapper.addSurveyContextSurvey(scs.getSurvey().getId(), newSurvey);
                surveyContextKeys.add(scs.getKey());
                
            } else if (mapper != null && !surveyContextKeys.contains(scs.getKey())) {
                // An existing survey is being referenced by a new survey context key
                
                DbSurveyContextSurvey surveyContextSurvey = new DbSurveyContextSurvey(
                        newSurveyContext, 
                        mapper.surveyContextSurveyMap.get(scs.getSurvey().getId()), 
                        scs.getKey());
                
                if(!insertOrUpdateRow(surveyContextSurvey, session)){
                    //ERROR
                    logger.error("Unable to update external survey context with id "+sourceSurveyContext.getId()+".");
                    return null;
                }
                
            }
        }
        
        newSurveyContext = SurveyContextUtil.getSurveyContextEager(newSurveyContext.getSurveyContextId(), dbMgr, session);
        
        return hibernateToGift.convertSurveyContext(newSurveyContext, false);
    }

    /**
     * Inserts the provided {@link SurveyContextSurvey survey context surveys} into the survey
     * context associated with the given id.
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     * 
     * @param surveyContextSurveys the survey context surveys to insert into the survey context.
     *        Can't be null.
     * @param surveyContextId the ID that maps to a survey context.
     * @param userName used for write permissions checks. Can't be blank.
     * @param session a database session with a transaction (that has already began) to do all the
     *        insert operations in. This is useful for doing rollback operations if any one of the
     *        inserts fails. Can be null if the caller doesn't care about partial fails and wants
     *        this class to manage the session.
     * @throws Exception if there was a problem inserting the survey context surveys
     */
    public static void insertSurveyContextSurveysIntoSurveyContext(List<SurveyContextSurvey> surveyContextSurveys,
            int surveyContextId, String userName, Session session) throws Exception {
        if (surveyContextSurveys == null) {
            throw new IllegalArgumentException("The parameter 'surveyContextSurveys' cannot be null.");
        } else if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("The parameter 'userName' cannot be blank.");
        }

        // whether or not to commit the transaction in this method
        boolean commit = false;

        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            if (!isSurveyContextEditable(surveyContextId, userName)) {
                throw new Exception(
                        "Failed to insert the survey context surveys into the survey context because '" + userName
                                + "' doesn't have write access to the survey context (id : " + surveyContextId + ").");
            }

            // insert survey context surveys
            for (SurveyContextSurvey scs : surveyContextSurveys) {
                scs.setSurveyContextId(surveyContextId);

                if (insertExternalSurveyContextSurvey(scs, null, userName, session) == null) {
                    throw new DetailedException(
                            "The survey context survey '" + scs.getKey() + "' was not inserted into the database.",
                            "The survey context survey '" + scs.toString()
                                    + "' was not inserted into the database for an unknown reason.",
                            null);
                }
            }

            if (commit && session.isOpen()) {
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            if (commit && session.isOpen()) {
                session.getTransaction().rollback();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Inserts a survey context into the database
     *
     * @param context The survey context to insert into the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     * @return SurveyContext The new survey context iff the insert was successful
     * @throws Exception if there was a problem inserting the survey context
     */
    public static SurveyContext insertSurveyContext(SurveyContext context, Session session) throws Exception {

        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            DbSurveyContext dbSurveyContext = giftToHibernate.convertSurveyContext(context, session);

            dbMgr.insertRow(dbSurveyContext, session);

            if(commit){
                session.getTransaction().commit();
                session.close();
            }

            return hibernateToGift.convertSurveyContext(dbSurveyContext, false);

        } catch (Exception e) {

            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e;
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Inserts a survey context into the database
     *
     * @param context The survey context to insert into the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return DbSurveyContext The new database survey context iff the insert was successful, null otherwise
     * @throws Exception if there was a problem inserting the survey context
     */
    @SuppressWarnings("unused")
    private static DbSurveyContext insertDbSurveyContext(DbSurveyContext context, Session session) throws Exception{
        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            session.saveOrUpdate(context);
            dbMgr.insertRow(context, session);

            if(commit){
                session.getTransaction().commit();
                session.close();
            }

            return context;

        } catch (Exception e) {

            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e;
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Return whether the survey has any survey responses.  This will look at all survey context responses.
     * 
     * @param surveyId the survey to check
     * @return true if there is one or more survey responses in the db
     */
    public static boolean hasSurveyResponses(int surveyId){

        long count = dbMgr.getRowCountByQuery("select count(*) from DbSurveyResponse as sr where sr.survey.surveyId = " + surveyId, null);       
        return count > 0;
    }
    
    /**
     * Return whether the survey has any survey responses in this specific survey context (i.e. has a learner answered
     * this survey in a specific course).
     * 
     * @param surveyContextId a course survey context to look for the responses for
     * @param surveyId the survey in a survey context to look for the response for
     * @return true if there is one or more survey response in the db for the survey in the survey context.
     */
    public static boolean hasSurveyResponses(int surveyContextId, int surveyId){
     
        long count = dbMgr.getRowCountByQuery("select count(*) from DbSurveyResponse as sr where sr.surveyContext.surveyContextId = "+surveyContextId+" AND sr.survey.surveyId = " + surveyId, null);       
        return count > 0;
    }
    
    /**
     * Return whether the survey is in any other survey contexts.
     * 
     * @param surveyContextId the survey context to ignore
     * @param surveyId the survey to find
     * @return true if there is one or more survey contexts that reference the survey
     */
    public static boolean isSurveyInAnotherSurveyContext(int surveyContextId, int surveyId){
               
        long count = dbMgr.getRowCountByQuery("select count(*) from DbSurveyContextSurvey as scs where scs.survey.surveyId = " + surveyId + 
                " and scs.surveyContext.surveyContextId != " + surveyContextId, null);   
        return count > 0;
    }
    
    /**
     * Returns the collection of question ids for questions that are using the option list specified.
     * @param optionListId an option list id to find all questions that use it
     * @return the list of unique question ids for questions that referenced the provided option list.
     * Will be empty if no results are found.
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> isOptionListInAnotherQuestion(int optionListId){
        
        List<Object> rawQuestionIds = dbMgr.executeSelectSQLQuery(
                "select QProp.questionid_fk from "+ dbMgr.getSchemaFromConfig()+".QuestionProperty as QProp where QProp.questionpropertyvalueid_fk IN (select QPropVal.questionpropertyvalueId_pk from "+ dbMgr.getSchemaFromConfig()+".QuestionPropertyValue as QPropVal where QPropVal.optionlistid_fk = "+optionListId+")");
        
        return (List<Integer>)(List<?>)rawQuestionIds;
    }
    
    /**
     * Return the survey for the survey components identified.
     * 
     * @param surveyContextId - unique id of a survey context in the db
     * @param giftKey - label for the particular survey to retrieve
     * @return DbSurvey the survey associated with the survey context and the gift key.  Null if not found.
     */
    private static DbSurvey getSurvey(int surveyContextId, String giftKey){
        DbSurvey surveyTableEntry = null;

        DbSurveyContextSurvey dbSurveyContextSurvey = 
                dbMgr.selectRowByTwoTupleCompositeId(SURVEY_CONTEXT_SURVEY_SURVEY_CONTEXT_ID_PROPERTY, surveyContextId, 
                        SURVEY_CONTEXT_SURVEY_GIFT_KEY_PROPERTY, giftKey, DbSurveyContextSurvey.class);

        if(dbSurveyContextSurvey != null){
            surveyTableEntry = dbSurveyContextSurvey.getSurvey();
        }

        return surveyTableEntry;
    }

    /**
     * Return the Survey common class representation for the survey identified.
     * 
     * @param surveyContextId - unique id of a survey context in the db
     * @param giftKey - label for the particular survey to retrieve
     * @return the common class representation of the Survey identified by the survey context and gift key.
     * Can be null if the survey was not found.
     */
    public static mil.arl.gift.common.survey.Survey getSurveyContextSurvey(int surveyContextId, String giftKey) {

        mil.arl.gift.common.survey.Survey survey = null;

        DbSurvey surveyTableEntry = getSurvey(surveyContextId, giftKey);
        if (surveyTableEntry != null) {
            survey = hibernateToGift.convertSurvey(surveyTableEntry);
        } 

        return survey;
    }
    
    /**
     * Copy the existing survey in the database.
     * 
     * @param surveyId the id of the survey to copy
     * @param username the user to give read and write access to on the new survey
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param session the transaction to perform the inserts in
     * @return the new survey id
     * @throws Exception if there was a problem copying the survey and its elements
     */
    private static int copySurvey(int surveyId, String username, ExternalSurveyMapper mapper, Session session) throws Exception{
        
        DbSurvey existingDbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class);
        if(existingDbSurvey == null){
            throw new IllegalArgumentException("Unable to copy the survey with id "+surveyId+" because it doesn't exist in the database.");
        }
        
        //check for existence of folder (which has a unique name)
        //TODO: currently ignoring folders since latest GAT survey composer doesn't expose folders        
        
        //Create new Survey object to insert into db
        DbSurvey newDbSurvey = new DbSurvey();
        newDbSurvey.setName(existingDbSurvey.getName());
        newDbSurvey.getVisibleToUserNames().add(username);
        newDbSurvey.getEditableToUserNames().add(username);
        
        if(!insertOrUpdateRow(newDbSurvey, session)){
            throw new Exception("Unable to insert new survey from copying survey with id "+existingDbSurvey.getSurveyId()+" because the insert operation failed.");
        }
        
        //create placeholder (to represent an empty set and to prevent null pointers)
        newDbSurvey.setProperties(new HashSet<DbSurveyProperty>());
        
        //now that the survey has been created with the new id and is in the db, create the survey pages
        int pageIndex = 0;
        Iterator<DbSurveyPage> pageItr = existingDbSurvey.getSurveyPages().iterator();
        while(pageItr.hasNext()){

            DbSurveyPage existingDbSurveyPage = pageItr.next();
        
            //create new survey page to insert into db
            DbSurveyPage newDbSurveyPage = new DbSurveyPage();
            newDbSurveyPage.setName(existingDbSurveyPage.getName());
            newDbSurveyPage.setPageNumber(pageIndex+1);  //1-based index
            newDbSurveyPage.setSurvey(newDbSurvey);
            if(!insertOrUpdateRow(newDbSurveyPage, session)){
                //ERROR
                throw new Exception("Unable to insert new survey page from copying survey page named "+existingDbSurveyPage.getName()+" in survey with id "+existingDbSurvey.getSurveyId()+" because the insert operation failed.");
            }
            
            //create placeholder (to represent an empty set and to prevent null pointers)
            newDbSurveyPage.setProperties(new HashSet<DbSurveyPageProperty>());
            
            //now the page has the new id and is in the db, create the pages elements
            int elementIndex = 0;
            Iterator<DbSurveyElement> elementItr = existingDbSurveyPage.getSurveyElements().iterator();
            while(elementItr.hasNext()){
                
                DbSurveyElement existingDbSurveyElement = elementItr.next();
                  
                if (mapper == null || (mapper != null && !mapper.surveyQuestionExists(existingDbSurveyElement.getSurveyElementId()))) {

                    DbSurveyElement newDbSurveyElement = new DbSurveyElement();
                    newDbSurveyElement.setElementNumber(elementIndex+1);  //1-based index
                    newDbSurveyElement.setSurveyPage(newDbSurveyPage);
                    
                    //create placeholder (to represent an empty set and to prevent null pointers)
                    newDbSurveyElement.setProperties(new HashSet<DbSurveyElementProperty>());
                    
                    //check for existence of survey element type
                    DbSurveyElementType elementType = existingDbSurveyElement.getSurveyElementType();
                    DbSurveyElementType dbSurveyElementTypeExample = new DbSurveyElementType(elementType.getKey(), elementType.getName());
                    DbSurveyElementType dbSurveyElementType = dbMgr.selectRowByExample(dbSurveyElementTypeExample, DbSurveyElementType.class, session);
                    if(dbSurveyElementType == null){
                        //create new survey element type
                        
                        if(!insertOrUpdateRow(dbSurveyElementTypeExample, session)){
                            throw new Exception("Unable to insert copy of survey element with id "+existingDbSurveyElement.getSurveyElementId()+" from survey page named "+ existingDbSurveyPage.getName()+" from survey with id "+existingDbSurvey.getSurveyId()+" because the insert operation failed.");
                        }
                        
                        dbSurveyElementType = dbSurveyElementTypeExample;
                    }
                    newDbSurveyElement.setSurveyElementType(dbSurveyElementType);
                    
                    if(existingDbSurveyElement.getQuestionId() != 0) {
                        //handle survey questions different than other elements...
                        
                        int newDbQuestionId = copyQuestion(existingDbSurveyElement.getQuestionId(), username, session);
                        newDbSurveyElement.setQuestionId(newDbQuestionId);                  
                        
                    }else{
                        
                        // Set all ids to 0 so the system treats these as new survey elements
                        existingDbSurveyElement.setSurveyElementId(0);
                    }
                    
    
                    if(!insertOrUpdateRow(newDbSurveyElement, session)){
                        //ERROR
                        throw new Exception("Unable to insert new survey element with id "+newDbSurveyElement.getSurveyElementId()+" in external survey with id "+newDbSurvey.getSurveyId()+".");
                    }
                    
                    //add survey question element id change to mapper
                    if(mapper != null && existingDbSurveyElement.getQuestionId() != 0){
                        mapper.addSurveyQuestion(existingDbSurveyElement.getSurveyElementId(), newDbSurveyElement.getSurveyElementId());
                    }
                    
                    //now the survey element has been created, create the survey element properties
                    if(existingDbSurveyElement.getProperties() != null){                
                        copySurveyElementProperties(newDbSurveyElement, existingDbSurveyElement.getProperties(), session);
                    }
                }             
                
                elementIndex++;
            } //end while loop

            //now the survey page has been created, create the survey page properties
            if(existingDbSurveyPage.getProperties() != null){                
                copySurveyPageProperties(newDbSurveyPage, existingDbSurveyPage.getProperties(), session);
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Finished converting page "+(pageIndex+1)+" of "+existingDbSurvey.getSurveyPages().size()+" from survey named "+existingDbSurvey.getName()+".\nConverted:\n"+existingDbSurveyPage+"\nTo:\n"+newDbSurveyPage);
            }

            pageIndex++;
        }//end while loop        
        
        //now the survey pages have been created, create the survey properties
        if(existingDbSurvey.getProperties() != null){
            copySurveyProperties(newDbSurvey, existingDbSurvey.getProperties(), session);
        }
        
        return newDbSurvey.getSurveyId();
    }
    
    /**
     * Converts survey properties into new survey properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param newDbSurveyPage the object to populate attributes for
     * @param propertiesToCopy The properties to copy
     * @param session The database session to work in
     */
    private static void copySurveyProperties(DbSurvey newDbSurvey, Set<DbSurveyProperty> propertiesToCopy, Session session) {

        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyProperty> toRemoveDbProperties = new HashSet<>(newDbSurvey.getProperties());

        for (DbSurveyProperty existingDbSurveyProperty : propertiesToCopy) {

            DbPropertyKey dbPropertyKey = existingDbSurveyProperty.getPropertyKey();

            boolean found = false;

            for (DbSurveyProperty dbProperty : newDbSurvey.getProperties()) {

                if (dbProperty.getPropertyKey() == dbPropertyKey) {

                    if (!dbProperty.getPropertyValue().getValue().equals(existingDbSurveyProperty.getPropertyValue().getValue())) {

                        DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyProperty.getPropertyValue().getValue());

                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating survey property "+dbProperty+" to "+propertyValue+" because the survey property changed.");
                        }
                        session.saveOrUpdate(propertyValue);

                        toAddMap.put(dbPropertyKey, propertyValue);

                    } else {

                        // The property exists and has not changed,
                        // no need to update
                        toRemoveDbProperties.remove(dbProperty);
                    }

                    found = true;
                }
            } //end for

            if (!found) {

                DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyProperty.getPropertyValue().getValue());

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding survey property of "+ propertyValue + " for key: " + dbPropertyKey + " because the survey property didn't exist.");
                }
                session.saveOrUpdate(propertyValue);

                //TODO: A query for an existing property value row with the same value could decrease the table size
                newDbSurvey.getProperties().add(new DbSurveyProperty(newDbSurvey, dbPropertyKey, propertyValue));
            }
        } //end for

        for (DbSurveyProperty toRemoveDbProperty : toRemoveDbProperties) {

            newDbSurvey.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

        session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            newDbSurvey.getProperties().add(new DbSurveyProperty(newDbSurvey, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }
    
    /**
     * Converts survey page properties into new survey page properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param newDbSurveyPage the object to populate attributes for
     * @param propertiesToCopy The properties to copy
     * @param session The database session to work in
     */
    private static void copySurveyPageProperties(DbSurveyPage newDbSurveyPage, Set<DbSurveyPageProperty> propertiesToCopy, Session session){

        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyPageProperty> toRemoveDbProperties = new HashSet<>(newDbSurveyPage.getProperties());

        for (DbSurveyPageProperty existingDbSurveyPageProperty : propertiesToCopy) {

            DbPropertyKey dbPropertyKey = existingDbSurveyPageProperty.getPropertyKey();

            boolean found = false;

            for (DbSurveyPageProperty dbProperty : newDbSurveyPage.getProperties()) {

                if (dbProperty.getPropertyKey() == dbPropertyKey) {

                    if (!dbProperty.getPropertyValue().getValue().equals(existingDbSurveyPageProperty.getPropertyValue().getValue())) {

                        DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyPageProperty.getPropertyValue().getValue());

                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating survey page property "+dbProperty+" to "+propertyValue+" because the survey property changed.");
                        }
                        session.saveOrUpdate(propertyValue);

                        toAddMap.put(dbPropertyKey, propertyValue);

                    } else {

                        // The property exists and has not changed,
                        // no need to update
                        toRemoveDbProperties.remove(dbProperty);
                    }

                    found = true;
                }

            }//end for            

            if (!found) {

                DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyPageProperty.getPropertyValue().getValue());

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding survey page property of "+propertyValue+" for key: " + dbPropertyKey + " because the survey property didn't exist.");
                }
                session.saveOrUpdate(propertyValue);

                //TODO: A query for an existing property value row with the same value could decrease the table size
                newDbSurveyPage.getProperties().add(new DbSurveyPageProperty(newDbSurveyPage, dbPropertyKey, propertyValue));
            }
        }//end for

        for (DbSurveyPageProperty toRemoveDbProperty : toRemoveDbProperties) {

            newDbSurveyPage.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

        session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            newDbSurveyPage.getProperties().add(new DbSurveyPageProperty(newDbSurveyPage, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }
    
    /**
     * Converts survey question properties into new survey question properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param newDbSurveyElement the hibernate object to populate attributes for
     * @param properties The properties to copy
     * @param session The database session to work in
     */
    private static void copySurveyElementProperties(DbSurveyElement newDbSurveyElement, Set<DbSurveyElementProperty> propertiesToCopy, Session session){
        
        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyElementProperty> toRemoveDbProperties = new HashSet<>(newDbSurveyElement.getProperties());

        for (DbSurveyElementProperty existingDbSurveyElementProperty : propertiesToCopy) {

            //check if a property of the same name exists already
            DbPropertyKey dbPropertyKey = existingDbSurveyElementProperty.getPropertyKey();

            boolean found = false;

            //find the survey's value for this property
            for (DbSurveyElementProperty dbProperty : newDbSurveyElement.getProperties()) {

                if (dbProperty.getPropertyKey() == dbPropertyKey) {
                    //found the survey's property db row instance

                    if (!dbProperty.getPropertyValue().getValue().equals(existingDbSurveyElementProperty.getPropertyValue().getValue())) {
                        //the properties values are different

                        DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyElementProperty.getPropertyValue().getValue());

                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating survey element property "+dbProperty+" to "+propertyValue+" because the survey property changed.");
                        }
                        session.saveOrUpdate(propertyValue);

                        toAddMap.put(dbPropertyKey, propertyValue);

                    } else {

                        // The property exists and has not changed,
                        // no need to update
                        toRemoveDbProperties.remove(dbProperty);
                    }

                    found = true;
                }
            } //end for

            if (!found) {
                //the survey doesn't contain this property, therefore it needs to be added

                DbPropertyValue propertyValue = new DbPropertyValue(existingDbSurveyElementProperty.getPropertyValue().getValue());
               
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding survey element property of "+propertyValue+" because the survey property didn't exist.");
                }
                session.saveOrUpdate(propertyValue);

                //TODO: A query for an existing property value row with the same value could decrease the table size
                newDbSurveyElement.getProperties().add(new DbSurveyElementProperty(newDbSurveyElement, dbPropertyKey, propertyValue));
            }

        } //end for

        for (DbSurveyElementProperty toRemoveDbProperty : toRemoveDbProperties) {

            newDbSurveyElement.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

            session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            newDbSurveyElement.getProperties().add(new DbSurveyElementProperty(newDbSurveyElement, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }
    
    /**
     * Insert the survey represented by the common class representation in the survey database.  This method
     * will treat the survey and all primary key ids as new entries in the database, i.e. as an external survey
     * being inserted into the database.  Therefore new ids will be generated upon inserting in the database.  The mapper
     * will contain the relationship between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param survey contains the survey information to populate database tables with
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return Survey a new survey containing the new ids created by the database
     */
    public static DbSurvey insertExternalSurvey(Survey survey, ExternalSurveyMapper mapper, Session session) throws Exception{
        
        if(survey == null){
            throw new IllegalArgumentException("The survey can't be null.");
        }
        
        //check for existence of folder (which has a unique name)
        DbFolder folder = null;
        String folderName = survey.getFolder();
        if(folderName != null){
            
            DbFolder folderExample = new DbFolder(folderName);
            folder = dbMgr.selectRowByExample(folderExample, DbFolder.class, session);
            
            if(folder == null) {
                
                folderExample.getEditableToUserNames().addAll(survey.getEditableToUserNames());
                folderExample.getVisibleToUserNames().addAll(survey.getVisibleToUserNames());
                
                if(!insertOrUpdateRow(folderExample, session)){
                    //ERROR
                    logger.error("Unable to insert external folder named "+folderExample.getName()+" related to external survey with id "+survey.getId()+".");
                    return null;
                }
                
                folder = folderExample;
                 
            } else if((!folder.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)
                    && !folder.getEditableToUserNames().containsAll(survey.getEditableToUserNames()))) {
                // make sure this folder exists and is visible to the user
                
                folder.getEditableToUserNames().addAll(survey.getEditableToUserNames());
                folder.getVisibleToUserNames().addAll(survey.getVisibleToUserNames());
                                
                if(!insertOrUpdateRow(folder, session)){
                    //ERROR
                    logger.error("Unable to insert external folder named "+folder.getName()+" related to external survey with id "+survey.getId()+".");
                    return null;
                }
            }
        }
        
        //Create new Survey object to insert into db
        DbSurvey dbSurvey = new DbSurvey();
        dbSurvey.setName(survey.getName());
        dbSurvey.setFolder(folder);
        dbSurvey.getVisibleToUserNames().addAll(survey.getVisibleToUserNames());
        dbSurvey.getEditableToUserNames().addAll(survey.getEditableToUserNames());
        
        // Because we are committing the survey and it's children to the database from the top down,
        // we need to initialize the survey's page children manually
        dbSurvey.setSurveyPages(new HashSet<DbSurveyPage>());
        
        if(!insertOrUpdateRow(dbSurvey, session)){
            //ERROR 
            logger.error("Unable to insert external survey with id "+dbSurvey.getSurveyId()+".");
            return null;
        }
        
        //create placeholder (to represent an empty set and to prevent null pointers)
        dbSurvey.setProperties(new HashSet<DbSurveyProperty>());
        
        //now that the survey has been created with the new id and is in the db, create the survey pages
        for(int pageIndex = 0; pageIndex < survey.getPageCount(); pageIndex++){

            SurveyPage page = survey.getPages().get(pageIndex);
        
            //create new survey page to insert into db
            DbSurveyPage dbPage = new DbSurveyPage();
            dbPage.setName(page.getName());
            dbPage.setPageNumber(pageIndex+1);  //1-based index
            dbPage.setSurvey(dbSurvey);
            if(!insertOrUpdateRow(dbPage, session)){
                //ERROR
                logger.error("Unable to insert external survey page named "+dbPage.getName()+" in external survey with id "+dbSurvey.getSurveyId()+".");
                return null;
            }
            
            // Because we are committing the survey and it's children to the database from the top down,
            // we need to set the survey's page children manually
            dbSurvey.getSurveyPages().add(dbPage);
            
            // create placeholder (to represent an empty set and to prevent null pointers)
            dbPage.setSurveyElements(new HashSet<DbSurveyElement>());
            dbPage.setProperties(new HashSet<DbSurveyPageProperty>());
            
            //now the page has the new id and is in the db, create the pages elements
            for(int elementIndex = 0; elementIndex < page.getElements().size(); elementIndex++){
                
                AbstractSurveyElement element = page.getElements().get(elementIndex);
                  
                if (mapper == null || (mapper != null && !mapper.surveyQuestionExists(element.getId()))) {

                    DbSurveyElement dbElement = new DbSurveyElement();
                    dbElement.setElementNumber(elementIndex+1);  //1-based index
                    dbElement.setSurveyPage(dbPage);
                    
                    //create placeholder (to represent an empty set and to prevent null pointers)
                    dbElement.setProperties(new HashSet<DbSurveyElementProperty>());
                    
                    //check for existence of survey element type
                    SurveyElementTypeEnum elementTypeEnum = element.getSurveyElementType();
                    DbSurveyElementType dbSurveyElementTypeExample = new DbSurveyElementType(elementTypeEnum.getName(), elementTypeEnum.getDisplayName());
                    DbSurveyElementType dbSurveyElementType = dbMgr.selectRowByExample(dbSurveyElementTypeExample, DbSurveyElementType.class, session);
                    if(dbSurveyElementType == null){
                        //create new survey element type
                        
                        if(!insertOrUpdateRow(dbSurveyElementTypeExample, session)){
                            //ERROR
                            logger.error("Unable to insert external survey element named "+dbPage.getName()+" in external survey with id "+dbSurvey.getSurveyId()+".");
                            return null;
                        }
                        
                        dbSurveyElementType = dbSurveyElementTypeExample;
                    }
                    dbElement.setSurveyElementType(dbSurveyElementType);
                    
                    if(element instanceof AbstractSurveyQuestion) {
                        //handle survey questions different than other elements...
                        
                        @SuppressWarnings("unchecked")
                        AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;                 
                        
                            // insert survey question's question                    
                            AbstractQuestion question = surveyQuestion.getQuestion();
                            
                            question.setQuestionId(0);
                            question.getVisibleToUserNames().clear();
                            question.getEditableToUserNames().clear();
                            question.getVisibleToUserNames().addAll(survey.getVisibleToUserNames());
                            question.getEditableToUserNames().addAll(survey.getEditableToUserNames());
                            
                            try{
                                AbstractQuestion newQ = insertExternalQuestion(question, mapper, session);
                                dbElement.setQuestionId(newQ.getQuestionId());                              
                            }catch(Exception e){
                                throw new Exception("Unable to insert external question with id "+surveyQuestion.getId()+" in external survey with id "+dbSurvey.getSurveyId()+".", e);
                            }                     
                        
                    }else{
                        
                        // Set all ids to 0 so the system treats these as new survey elements
                        element.setId(0);
                    }
                    
    
                    if(!insertOrUpdateRow(dbElement, session)){
                        //ERROR
                        logger.error("Unable to update survey element with id "+dbElement.getSurveyElementId()+" in external survey with id "+dbSurvey.getSurveyId()+".");
                        return null;
                    }
                    
                    // Because we are committing the survey and it's children to the database from
                    // the top down, we need to set the page's children manually
                    dbPage.getSurveyElements().add(dbElement);
                    
                    //add survey question element id change to mapper
                    if(mapper != null && element instanceof AbstractSurveyQuestion){
                        mapper.addSurveyQuestion(element.getId(), dbElement.getSurveyElementId());
                    }
                    
                    //now the survey element has been created, create the survey element properties
                    if(element.getProperties() != null){                
                        giftToHibernate.convertSurveyElementProperties(dbElement, element.getProperties(), session);
                    }
                }                
            }
            
            //now the survey page has been created, create the survey page properties
            if(page.getProperties() != null){                
                giftToHibernate.convertSurveyPageProperties(dbPage, page.getProperties(), session);
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Finished converting page "+(pageIndex+1)+" of "+survey.getPageCount()+" from survey named "+survey.getName()+".\nConverted:\n"+page+"\nTo:\n"+dbPage);
            }
        }        
        
        //now the survey pages have been created, create the survey properties
        if(survey.getProperties() != null){
            giftToHibernate.convertSurveyProperties(dbSurvey, survey.getProperties(), session);
        }
        
        return dbSurvey;

    }

    /**
     * Insert the survey question represented by the common class representation in the survey database.  This method
     * will treat the survey question and all primary key ids as new entries in the database, i.e. as an external survey
     * being inserted into the database.  Therefore new ids will be generated upon inserting in the database.  The mapper
     * will contain the relationship between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param surveyQuestion contains the survey question information to populate database tables with
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return AbstractSurveyQuestion<? extends AbstractQuestion> a new survey question containing the new ids created by the database
     * @throws Exception if the insert failed
     */
    public static AbstractSurveyQuestion<? extends AbstractQuestion> insertExternalSurveyQuestion(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, 
            ExternalSurveyMapper mapper, Session session) throws Exception{
        
        if(surveyQuestion == null){
            throw new IllegalArgumentException("The survey question can't be null.");
        }
        
        int originalSurveyQuestionId = surveyQuestion.getId();
        surveyQuestion.setId(0);
        
        surveyQuestion.getQuestion().setQuestionId(0);                 
        AbstractQuestion newQ = insertExternalQuestion(surveyQuestion.getQuestion(), mapper, session);
        if(newQ == null){
            //ERROR
            logger.error("Unable to insert external question in external survey.");
            return null;
        }
        
        surveyQuestion.getQuestion().setQuestionId(newQ.getQuestionId());
        
        AbstractSurveyQuestion<? extends AbstractQuestion> newSurveyQuestion = insertSurveyQuestion(surveyQuestion, session);
        if(mapper != null && newSurveyQuestion != null){
            mapper.addSurveyQuestion(originalSurveyQuestionId, newSurveyQuestion.getId());
        }
        
        return newSurveyQuestion;
    }
    
    /**
     * Insert a survey question into the database.
     * 
     * @param surveyQuestion the survey question to insert into the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return AbstractSurveyQuestion<? extends AbstractQuestion> the new Survey Question 
     * @throws Exception if inserting the survey question failed
     */
    public static AbstractSurveyQuestion<? extends AbstractQuestion> insertSurveyQuestion(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, 
            Session session) throws Exception{
        
        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            DbSurveyElement dbSurveyElement = giftToHibernate.convertSurveyElement(surveyQuestion, session);

            dbMgr.insertRow(dbSurveyElement, session);

            if(commit){
                session.getTransaction().commit();
                session.close();
            }

            @SuppressWarnings("unchecked")
            AbstractSurveyQuestion<? extends AbstractQuestion> newSurveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) hibernateToGift.convertSurveyElement(dbSurveyElement);
            return newSurveyQuestion;

        } catch (Exception e) {
           
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e;
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Copy an existing db question.
     * 
     * @param questionId the db id of the question to copy
     * @param username the user to give read and write access to this new question db entry
     * @param session the transaction to do the work in.  Can't be null.
     * @return the new question id for the new created question
     * @throws Exception if there was a problem during the copy logic or insert statement
     */
    public static int copyQuestion(int questionId, String username, Session session) throws Exception{
        
        DbQuestion existingDbQuestion = dbMgr.selectRowById(questionId, DbQuestion.class);
        if(existingDbQuestion == null){
            throw new Exception("Failed to copy the question with id "+questionId+" because a question with that id doesn't exist in the survey database.");
        }
        
        DbQuestion newDbQuestion = new DbQuestion();
        newDbQuestion.setQuestionType(existingDbQuestion.getQuestionType());
        newDbQuestion.getEditableToUserNames().add(username);
        newDbQuestion.getVisibleToUserNames().add(username);
        newDbQuestion.setText(existingDbQuestion.getText());
        
        //directly copy, than manipulate below...
        newDbQuestion.setQuestionProperties(existingDbQuestion.getQuestionProperties());
        
        // Modify the question so references to non-shared option lists are copied
        for (DbQuestionProperty newDbQuestionProperty : newDbQuestion.getQuestionProperties()) {
            
            DbQuestionPropertyValue newDbQuestionPropertyValue = newDbQuestionProperty.getPropertyValue();
            if(newDbQuestionPropertyValue.getOptionListValue() != null){
                
                DbOptionList newDbOptionList = new DbOptionList();
                if(!newDbOptionList.getIsShared()){
                    // set option list id to 0 so that new ids 
                    // are generated to create a new option list.
                    
                    newDbOptionList.setOptionListId(0);
                    
                    newDbOptionList.getVisibleToUserNames().clear();
                    newDbOptionList.getVisibleToUserNames().add(username);
                    
                    newDbOptionList.getEditableToUserNames().clear();
                    newDbOptionList.getEditableToUserNames().add(username);
                }
            }
        }
        
        return insertQuestion(newDbQuestion, session);        
    }
    
    /**
     * Insert the question represented by the common class representation in the survey database.  This method
     * will treat the question and all primary key ids as new entries in the database, i.e. as an external survey
     * being inserted into the database.  Therefore new ids will be generated upon inserting in the database.  The mapper
     * will contain the relationship between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param question contains the question information to populate database tables with
     * @param mapper where to maintain information about the ids that have changed during the insert process.  Can't be null.
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return AbstractQuestion a new question containing the new ids created by the database
     * @throws Exception if the insert failed
     */
    public static AbstractQuestion insertExternalQuestion(AbstractQuestion question, ExternalSurveyMapper mapper, Session session) throws Exception{
        
        // Modify the question so references to non-shared option lists are copied
        for (SurveyPropertyKeyEnum propertyKey : question.getProperties().getKeys()) {

            Serializable value = question.getProperties().getPropertyValue(propertyKey);

            if (value instanceof OptionList) {

                OptionList optionList = (OptionList) value;
                
                if(!optionList.getIsShared()) {
                    // MH: Do not set option list id to 0 because the underlying logic needs to populate
                    // the mapper with the original option list id to the new option list id in order to
                    // reuse the new id for any subsequent instances of the option list
                    //optionList.setId(0);
                    
                    optionList.getVisibleToUserNames().addAll(question.getVisibleToUserNames());
                    optionList.getEditableToUserNames().addAll(question.getEditableToUserNames());

                }
            }
        }
        
        return insertQuestion(question, session, mapper);        
    }
    
    /**
     * Insert the option list represented by the common class representation in the survey database.  This method
     * will treat the option list and all primary key ids as new entries in the database, i.e. as an external survey
     * being inserted into the database.  Therefore new ids will be generated upon inserting in the database.  The mapper
     * will contain the relationship between original and new ids for certain survey items as deemed important for survey context
     * importing and domain content file updating.
     * 
     * @param optionList contains the option list information to populate database tables with
     * @param mapper where to maintain information about the ids that have changed during the insert process
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return DbOptionList a new option list containing the new ids created by the database, null if the insert failed.
     */
    public static DbOptionList insertExternalOptionList(OptionList optionList, ExternalSurveyMapper mapper, Session session){
        
        DbOptionList dbOptionList = new DbOptionList(optionList.getName());
        dbOptionList.setIsShared(optionList.getIsShared());       
        dbOptionList.setListOptions(new HashSet<DbListOption>());
        
        if(!insertOrUpdateRow(dbOptionList, session)){
            //ERROR
            logger.error("Unable to insert external option list in external question.");
            return null;
        }
        
        //now that the option list is in the database, create it's list options
        for(int optionIndex = 0; optionIndex < optionList.getListOptions().size(); optionIndex++){
            
            ListOption listOption = optionList.getListOptions().get(optionIndex);
            
            DbListOption dbListOption = new DbListOption(listOption.getText(), dbOptionList, optionIndex+1);
            if(!insertOrUpdateRow(dbListOption, session)){
                //ERROR
                logger.error("Unable to insert external list option in external option list with id "+dbOptionList.getOptionListId()+".");
                return null;
            }
            
            if(mapper != null){
                mapper.addSurveyQuestionReply(listOption.getId(), dbListOption.getListOptionId());
            }
        }
        
        return dbOptionList;
    }

    /**
     * Inserts a Question into the database
     *
     * @param question The question to insert into the database. </br>
     * Note: any option list should NOT have their ids set to zero when importing this question (e.g. from a course import) because
     * this method will populate the mapper (if not null) with the original option list ids to the new ids in order to reuse that new id
     * for subsequent instances of that same option list in the import.
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @param mapper where to maintain information about the ids that have changed during the insert process.  Can't be null if performing an import for
     * the question from an external source like a course import. 
     * @return AbstractQuestion The question in the database
     * @throws Exception if the insert failed
     */
    public static AbstractQuestion insertQuestion(AbstractQuestion question, Session session, ExternalSurveyMapper mapper) throws Exception {       

        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            DbQuestion dbQuestion = giftToHibernate.convertQuestion(question, session, mapper);

            dbMgr.insertRow(dbQuestion, session);

            if(commit){
                session.getTransaction().commit();
                session.close();
            }

            return hibernateToGift.convertQuestion(dbQuestion);

        } catch (Exception e) {
            
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e; 
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Inserts a Question into the database
     *
     * @param question The question to insert into the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return AbstractQuestion The question in the database
     * @throws Exception if the insert failed
     */
    private static int insertQuestion(DbQuestion dbQuestion, Session session) throws Exception {       

        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            dbMgr.insertRow(dbQuestion, session);

            if(commit){
                session.getTransaction().commit();
                session.close();
            }

            return dbQuestion.getQuestionId();

        } catch (Exception e) {
            
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e; 
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

    }
    
    /**
     * Insert or Update the provided data in the UMS database.
     * 
     * @param data a UMS hibernate class populated with information, this is the row to insert or update in the database
     * @param session The transaction to conduct the database operation in.  Can be null.  If null the transaction operation
     *                  will be created, committed and closed in this method.  If not null, the transaction must be already started and
     *                  the transaction will not be committed, closed (upon success) or rolled back (upon failure)
     * @return boolean whether the operation succeeded or not
     */
    public static boolean insertOrUpdateRow(Object data, Session session){
        
        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            if (dbMgr.saveOrUpdateRow(data, session)) {

                if(commit){
                    session.getTransaction().commit();
                    session.close();
                }
                
                return true;

            } else {

                logger.error("Could not perform a db insert for data of "+data+".");
                
                if(commit){
                    session.getTransaction().rollback();
                    session.close();
                }
            }

        } catch (Exception e) {

            logger.error("Caught an exception while inserting data of "+data, e);
            
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }
        
        return false;
    }
    
    /**
     * Insert the survey context survey into the db if the survey context is not found in the db or
     * update the existing survey context survey if the survey id has changed.
     * 
     * @param surveyContextSurvey contains the survey context id, survey and gift key values that are needed to insert
     * or update the survey context survey row in the table.  If null than null will be returned.  If the survey in this is null
     * than the null will be returned.</br>
     * Note:  The survey context and survey must already exist in the database.
     * @param session the session to perform the database operation.  If provided the session will remain open and the
     * transaction will not be committed.  If null the session will be managed w/in this method.
     * @return the Survey context survey resulting from the database operation. Null will be returned if there was a problem.
     */
    public static SurveyContextSurvey insertSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey, Session session){
        
        if(surveyContextSurvey == null){
            return null;
        }else if(surveyContextSurvey.getSurvey() == null){
            return null;
        }
        
        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {            
            DbSurveyContextSurvey dbSurveyContextSurvey = dbMgr.getSurveyContextSurvey(surveyContextSurvey.getSurveyContextId(), surveyContextSurvey.getKey());
            if(dbSurveyContextSurvey != null){
                
                if(dbSurveyContextSurvey.getSurvey().getSurveyId() != surveyContextSurvey.getSurvey().getId()){
                    //the survey has changed for the gift key
                    
                    DbSurvey dbSurvey = (DbSurvey) session.get(DbSurvey.class, surveyContextSurvey.getSurvey().getId());

                    if (dbSurvey != null) {
                        dbSurveyContextSurvey.setSurvey(dbSurvey);
                    }else{
                        //error
                        logger.error("Could not insert a survey context survey because the survey "+surveyContextSurvey.getSurvey()+" could not be found.\n" + dbSurveyContextSurvey);
                        
                        if(commit){
                            session.getTransaction().rollback();
                            session.close();
                        }

                        return null;
                    }
                }else{
                    //nothing to update
                    return surveyContextSurvey;
                }
            }else{

                dbSurveyContextSurvey = giftToHibernate.convertSurveyContextSurvey(surveyContextSurvey, session);
            }
    
            if (dbMgr.saveOrUpdateRow(dbSurveyContextSurvey, session)) {
                
                // update survey context last modified date
                String updateSurveyContextModifiedDateQuery = "UPDATE "+dbMgr.getSchemaFromConfig()+".SurveyContext as sc SET sc.lastModified = '"+new java.sql.Timestamp(System.currentTimeMillis())+"' where sc.SURVEYCONTEXTID_PK = "+dbSurveyContextSurvey.getSurveyContext().getSurveyContextId();
                dbMgr.executeUpdateSQLQuery(updateSurveyContextModifiedDateQuery, session);
                
                if(commit){
                    session.getTransaction().commit();
                    session.close();
                }

                return hibernateToGift.convertSurveyContextSurvey(dbSurveyContextSurvey);

            } else {

                logger.error("Could not insert a survey context survey: " + dbSurveyContextSurvey);
                
                if(commit){
                    session.getTransaction().rollback();
                    session.close();
                }

                return null;
            }

        } catch (Exception e) {

            logger.error("Caught an exception while inserting a survey context survey", e);
            
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

        return null;
    }
    
    /**
     * Inserts a survey into the database
     *
     * @param survey The survey to insert into the database
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return Survey The survey in the database if the insert was successful
     */
    public static Survey insertSurvey(Survey survey, Session session) {

        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {

            DbSurvey dbSurvey = giftToHibernate.convertSurvey(survey, session);

            if (dbMgr.saveOrUpdateRow(dbSurvey, session)) {

                if(commit){
                    session.getTransaction().commit();
                    session.close();
                }

                return hibernateToGift.convertSurvey(dbSurvey);

            } else {

                logger.error("Could not insert a survey, the survey could not be inserted: " + dbSurvey);
                
                if(commit){
                    session.getTransaction().rollback();
                    session.close();
                }

                return null;
            }

        } catch (Exception e) {

            logger.error("Caught an exception while inserting a survey", e);
            
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }

        return null;
    }
    
    /**
     * Inserts a survey property key into the database
     * 
     * @param key The survey property key to insert into the database
     * @throws Exception if there was a problem inserting
     */
    public static void insertSurveyPropertyKey(SurveyPropertyKeyEnum key) throws Exception{     
        dbMgr.insertRow(new DbPropertyKey(key.getName()));
    }
    
    /**
     * Gets a survey context common class representation for the survey context identified
     * Use the 'include generated surveys' flag to include concept knowledge surveys created during course
     * execution based on if the survey context has a knowledge question bank associated with it.  Setting this flag
     * to false is desired for GIFT exporting.
     *
     * @param surveyContextId The ID of the survey context to get
     * @param includeGeneratedSurveys whether or not to include any generated concept knowledge surveys from the 
     * survey context being returned.  The caller needs to be aware that removing these surveys could hide dependencies
     * in the returned survey context because the context doesn't contain all of the surveys actually associated with it.
     * However in other cases, like GIFT course exporting, these surveys shouldn't be associated with the original survey
     * context in the GIFT importing the exported survey context.
     * @return The survey context with the specified ID, null if the survey context doesn't exist.
     * @throws DetailedException if there was a problem retrieving the survey context
     */
    public static mil.arl.gift.common.survey.SurveyContext getSurveyContext(int surveyContextId, 
            boolean includeGeneratedSurveys) throws DetailedException {
        
        try {
            
            DbSurveyContext dbSurveyContext;
            
            if(includeGeneratedSurveys){
                dbSurveyContext = SurveyContextUtil.getSurveyContextEager(surveyContextId, dbMgr);
                
            } else {
                dbSurveyContext = SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr);
            }
            
            if(dbSurveyContext == null){
                return null;
            }
            
            return hibernateToGift.convertSurveyContext(dbSurveyContext, !includeGeneratedSurveys);
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the survey context with id "+surveyContextId+".", 
                    "There was an exception thrown when trying to retrieve the survey context.", e);
        }
    }
    
    /**
     * Returns whether the survey context exists in the database.
     * 
     * @param surveyContextId unique id of the survey context to check for
     * @return true iff the survey context was found in the database
     */
    public static boolean doesSurveyContextExist(int surveyContextId){
        
        try {
            long count = dbMgr.getRowCountByQuery("select count(*) from DbSurveyContext as sc where sc.surveyContextId = " + surveyContextId, null);
            return count > 0;
        } catch (Exception e) {
            logger.error("Caught an exception while checking if survey context ID of " + surveyContextId + " exists.", e);
            return false;
        }
    }
    
    /**
     * Return true if the survey context has a single survey response in the survey response table.
     * 
     * @param surveyContextId the survey context to look for survey responses for
     * @return true if a single survey response for this survey context is found
     */
    public static boolean doesSurveyContextHaveResponses(int surveyContextId){
        
        String queryString = "from DbSurveyResponse as response where response.surveyContext.surveyContextId = " + surveyContextId;
        List<DbSurveyResponse> dbSurveyResponses = dbMgr.selectRowsByQuery(DbSurveyResponse.class, queryString, UNUSED_INDEX, 1);
        
        return !dbSurveyResponses.isEmpty();
    }
    
    /**
     * Gets a survey context common class representation for the survey context identified
     * Note: the survey context return will contain concept knowledge surveys created during course
     * execution based on if the survey context has a knowledge question bank associated with it.
     *
     * @param surveyContextId The ID of the survey context to get
     * @return The survey context with the specified ID, null if the survey context doesn't exist.
     * @throws DetailedException if there was a problem retrieving the survey context
     */
    public static mil.arl.gift.common.survey.SurveyContext getSurveyContext(int surveyContextId) throws DetailedException {
        return getSurveyContext(surveyContextId, false);
    }
    
    /**
     * Gets a survey from the database
     *
     * @param id The ID of the survey to get
     * @return The survey with the specified ID, null if there was an
     * error
     */
    public static Survey getSurvey(int id) {
        try {
            DbSurvey dbSurvey = getDbSurvey(id, null);
            Survey survey = hibernateToGift.convertSurvey(dbSurvey);
            return survey;
        } catch (Exception e) {
            logger.error("Caught an exception while getting survey with ID " + id + " from database", e);
            return null;
        }
    }
    
    /**
     * Gets the db option list for the survey question
     * 
     * @param surveyQuestion The multiple choice or rating scale question
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return DbOptionList The reply options, null if the question type is incorrect or there has been an error
     */
    protected static List<DbOptionList> getDbOptionList(DbSurveyElement surveyQuestion, Session session){
        
        DbQuestion question = getDbQuestion(surveyQuestion.getQuestionId(), session);
        
        return getDbOptionList(question);
    }
    
    /**
     * Get the common option list for the survey question.
     * 
     * @param surveyQuestion the survey question to get the option list object
     * @return the survey question's option list.  Null if the survey question provided is null or the
     * survey question type doesn't have an option list.
     */
    public static OptionList getOptionList(AbstractSurveyQuestion<?> surveyQuestion){
        
        if(surveyQuestion == null){
            return null;
        }

        if(surveyQuestion instanceof MultipleChoiceSurveyQuestion){
            return ((MultipleChoiceSurveyQuestion)surveyQuestion).getChoices();
        }else if(surveyQuestion instanceof RatingScaleSurveyQuestion) {
            return ((RatingScaleSurveyQuestion)surveyQuestion).getChoices();
        }else{       
            return null;
        }
    }
    
    /**
     * Get the option lists for the survey question.
     * 
     * @param surveyQuestion the survey question to get the option list object
     * @return the survey question's option list. Null if the survey question provided is null or
     *         the survey question type doesn't have an option list.
     */
    public static List<OptionList> getOptionLists(AbstractQuestion surveyQuestion) {

        if (surveyQuestion == null) {
            return null;
        }

        QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(surveyQuestion);
        List<SurveyPropertyKeyEnum> surveyPropertyKeys = getSurveyPropertyKeysByQuestionType(questionType);

        List<OptionList> optionLists = new ArrayList<OptionList>();

        SurveyItemProperties properties = surveyQuestion.getProperties();

        for (SurveyPropertyKeyEnum key : surveyPropertyKeys) {
            if (properties.getPropertyValue(key) instanceof OptionList) {

                optionLists.add((OptionList) properties.getPropertyValue(key));

                // found all keys
                if (optionLists.size() == surveyPropertyKeys.size()) {
                    break;
                }
            }
        }

        return optionLists;
    }
    
    /**
     * Gets an option list from the database
     *
     * @param id The ID of the option list to get
     * @return GwtOptionList The option list with the specified ID, null if
     * there was an error
     */
    public static OptionList getOptionList(int id) {
        try {
            DbOptionList dbOptionList = getDbOptionList(id, null);
            return hibernateToGift.convertOptionList(dbOptionList);
        } catch (Exception e) {
            logger.error("Caught an exception while getting an option list from the database", e);
            return null;
        }
    }
     
    /**
     * Gets the list options for the survey
     * 
     * @param question the survey question
     * @return DbOptionList The list options. Returns null if the question is null. Can be empty.
     */
    private static List<DbOptionList> getDbOptionList(DbQuestion question) {
        
        if(question == null){
            return null;
        }

        QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(question.getQuestionType().getKey());
        List<SurveyPropertyKeyEnum> surveyPropertyKeys = getSurveyPropertyKeysByQuestionType(questionType);

        List<DbOptionList> optionLists = new ArrayList<DbOptionList>();

        for (DbQuestionProperty property : question.getQuestionProperties()) {

            SurveyPropertyKeyEnum propertyKey = SurveyPropertyKeyEnum.valueOf(property.getPropertyKey().getKey());

            if (surveyPropertyKeys.contains(propertyKey)) {
                optionLists.add(property.getPropertyValue().getOptionListValue());
                
                // found all keys
                if (optionLists.size() == surveyPropertyKeys.size()) {
                    break;
                }
            }
        }

        return optionLists;
    }

    /**
     * Builds the list of survey property keys for the given question type.
     * 
     * @param questionType the question type.
     * @return the list of survey property keys associated with the question type.
     */
    private static List<SurveyPropertyKeyEnum> getSurveyPropertyKeysByQuestionType(QuestionTypeEnum questionType) {
        List<SurveyPropertyKeyEnum> surveyPropertyKeys = new ArrayList<SurveyPropertyKeyEnum>();

        if (questionType == QuestionTypeEnum.MULTIPLE_CHOICE || questionType == QuestionTypeEnum.RATING_SCALE) {
            surveyPropertyKeys.add(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
        } else if (questionType == QuestionTypeEnum.MATRIX_OF_CHOICES) {
            surveyPropertyKeys.add(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
            surveyPropertyKeys.add(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
        }

        return surveyPropertyKeys;
    }
    
    /**
     * Gets an option list from the database
     *
     * @param optionListId The ID of the option list to get
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return DbOptionList The option list with the specified ID, null if
     * there was an error
     */
    protected static DbOptionList getDbOptionList(int optionListId, Session session){
        
        if (session == null) {
            return dbMgr.selectRowById(optionListId, DbOptionList.class);
        } else {
            return dbMgr.selectRowById(optionListId, DbOptionList.class, session);
        }
    }
    
    /**
     * Gets the list of survey property key names stored in the database
     * 
     * @return List<String> The list of survey property key names
     */
    public static List<String> getSurveyPropertyKeyNames() {
        
        try{
            List<DbPropertyKey> dbkeyList = dbMgr.selectAllRows(DbPropertyKey.class);
            List<String> keyList = new ArrayList<String>();
            
            for(DbPropertyKey dbKey : dbkeyList){       
                
                keyList.add(dbKey.getKey());
            }
            
            return keyList;
            
        } catch (Exception e) {
            logger.error("Caught an exception while getting survey property keys from the database", e);
        }

        return null;    
    }
    
    
    /**
     * Validates a concept survey containing questions from the specified survey context with the specified concepts.  If any validation
     * errors occur, a detailed exception is thrown containing reasons for the validation failure.
     * 
     * @param conceptSurvey - The existing EMAP question bank survey containing the entire set of possible questions.
     * @param surveyContextId - unique id of a survey context in the db
     * @throws DetailedException if there was a problem retrieving the survey based on the concept parameters specified
     */
    public static void validateConceptsSurvey(Survey conceptSurvey, int surveyContextId, final Map<String, ConceptParameters> concepts) throws DetailedException {
        getOrValidateConceptsSurvey(conceptSurvey, surveyContextId, concepts, false, true);
    }
    
    /**
     * Creates and gets a new survey containing questions from the specified survey context with the specified concepts.
     * 
     * @param conceptSurvey - The existing EMAP question bank survey containing the entire set of possible questions.
     * @param surveyContextId - unique id of a survey context in the db
     * @param concepts - the concepts with which the questions in the survey returned should be associated.  Can't be null.
     * @param createDatabaseEntry whether or not to store the survey being returned in the database for long term storage.
     * @return SurveyGiftData Metadata and a new survey containing questions from the specified survey context with the 
     * specified concepts.
     * @throws DetailedException if there was a problem retrieving the survey based on the concept parameters specified
     */
    public static SurveyGiftData getConceptsSurvey(Survey conceptSurvey, int surveyContextId, final Map<String, ConceptParameters> concepts, boolean createDatabaseEntry) throws DetailedException {
        return getOrValidateConceptsSurvey(conceptSurvey, surveyContextId, concepts, createDatabaseEntry, false);
    }
    
    
    /**
     * Creates or validates a new survey containing questions from the specified survey context with the specified concepts.
     * 
     * @param conceptSurvey - The existing EMAP question bank survey containing the entire set of possible questions.
     * @param surveyContextId - unique id of a survey context in the db
     * @param concepts - the concepts with which the questions in the survey returned should be associated.  Can't be null.
     * @param createDatabaseEntry whether or not to store the survey being returned in the database for long term storage.
     * @param validateOnly whether or not to only validate the concept survey (which is used to only make sure there are enough questions for the concepts).
     * @return SurveyGiftData Metadata and a new survey containing questions from the specified survey context with the 
     * specified concepts.
     * @throws DetailedException if there was a problem retrieving the survey based on the concept parameters specified
     */
    private static SurveyGiftData getOrValidateConceptsSurvey(Survey conceptSurvey, int surveyContextId, final Map<String, ConceptParameters> concepts, boolean createDatabaseEntry, boolean validateOnly) throws DetailedException{

        if(conceptSurvey != null){
            
            try{            
                //create a TreeSet to store questions while avoiding adding duplicates with the same questionId
                
                /* Note: Using a hash set here is not reliable since hash sets only check for duplicate object reference 
                 * values. When questions are converted from the database, they each get their own objects, which means that
                 * even two identical questions will have different reference values.*/
                TreeSet<AbstractQuestion> questionSet = new TreeSet<AbstractQuestion>(new Comparator<AbstractQuestion>(){
    
                    @Override
                    public int compare(AbstractQuestion q1, AbstractQuestion q2) {   
                        
                        return Integer.compare(q1.getQuestionId(), q2.getQuestionId());
                    }
                    
                });
                
                //get the list of elements in the EMAP question bank survey and randomize it
                List<AbstractSurveyElement> randomizedSurveyElements = new ArrayList<AbstractSurveyElement>(conceptSurvey.getPages().get(0).getElements());
                
                if (!validateOnly) {
                    Collections.shuffle(randomizedSurveyElements);
                }
                
                //populate the question set one concept at a time
                for(final String concept : concepts.keySet()){   
                     StringBuilder logBuilder = new StringBuilder("concept = '").append(concept).append("'\n\tPossible Concept Questions:\n");
                    
                    //maintain and populate a list of potential questions for the current concept to add to the question set                    
                    List<AbstractQuestion> potentialQuestionList = new ArrayList<AbstractQuestion>();                   
                    for(AbstractSurveyElement surveyElement:  randomizedSurveyElements){
                        
                        if(surveyElement instanceof AbstractSurveyQuestion){
                            
                            //for each question in the EMAP question bank survey...
                            
                            AbstractQuestion question = ((AbstractSurveyQuestion<?>) surveyElement).getQuestion();
                            
                            if(question != null && !questionSet.contains(question)){
                                                                
                                String associatedConcepts = (String) question.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS);

                                List<String> associatedConceptList = new CaseInsensitiveList();
                                associatedConceptList.addAll(SurveyItemProperties.decodeListString(associatedConcepts));
                                
                                //check the question to make sure it is associated with the current concept
                                //Note: ignore case
                                boolean containsThisConcept = associatedConceptList.contains(concept);
                                
                                /* check the question to make sure it is not associated with any concepts that are
                                 * not specified by the concept survey request */
                                associatedConceptList.removeAll(concepts.keySet());
                                boolean containsOnlySpecifiedConcepts = associatedConceptList.isEmpty();                    
                                
                                if(containsThisConcept && containsOnlySpecifiedConcepts){
        
                                    //does the question have a correct answer (positive weight reply)
                                    
                                    double highestPossibleScore = question.getHighestPossibleScore();
                                    if(highestPossibleScore > 0.0){
        
                                    //if the question meets all criteria, add it to the potential question list
                                    potentialQuestionList.add(question);
                                    logBuilder.append("\t\tAdding question '").append(question.getText()).append("' (Id ").append(question.getQuestionId())
                                        .append(")\n");
                                }else{
                                    logBuilder.append("\t\tSkipping question '").append(question.getText()).append("' (Id ").append(question.getQuestionId())
                                            .append(") because the highest possible score is ").append(highestPossibleScore).append(" which means there is no correct answer.  A correct answer is needed to determine the knowledge level of the learner.\n");
                                    }
                                }else{
                                    logBuilder.append("\t\tSkipping question '").append(question.getText()).append("' (Id ").append(question.getQuestionId())
                                        .append(") because it contains extraneous concepts\n");
                                }
                            }
                        }
                    }
                            
                    
                    if (!validateOnly) {
                        /* prioritize all potential questions by whether or not they are preferred or avoided and sort the list 
                         * containing them by their established priorities */
                        final boolean hasPreferredQuestions = 
                                concepts.get(concept).getPreferredQuestions() != null
                                && !concepts.get(concept).getPreferredQuestions().isEmpty();
                        
                        final boolean hasAvoidQuestions = 
                                concepts.get(concept).getAvoidQuestions() != null
                                && !concepts.get(concept).getAvoidQuestions().isEmpty();
                                                
                        if(hasPreferredQuestions || hasAvoidQuestions){
                        
                            Collections.sort(potentialQuestionList, new Comparator<AbstractQuestion>(){

                                @Override
                                public int compare(AbstractQuestion q1,
                                        AbstractQuestion q2) {
                                    
                                    /* Collections.sort() sorts a list's elements from least to greatest, so we want to return
                                     * -1 when q1 has a higher priority than q2 and 1 when q1 has lower priority than q2 to make
                                     * sure higher priority questions are considered first when iterating */
                                    
                                    //q1 and q2 have equivalent priority to start with
                                    int result = 0;
                                    
                                    if(hasPreferredQuestions){
                                        if(concepts.get(concept).getPreferredQuestions().contains(q1.getQuestionId()) 
                                                && !concepts.get(concept).getPreferredQuestions().contains(q2.getQuestionId())){
                                
                                            //q1 is preferred while q2 is not, so q1 has higher priority
                                            result = -1;
                                            
                                        } else if(concepts.get(concept).getPreferredQuestions().contains(q2.getQuestionId()) 
                                                && !concepts.get(concept).getPreferredQuestions().contains(q1.getQuestionId())){
                                            
                                            //q2 is preferred while q1 is not, so q1 has lower priority
                                            result = 1;
                                        }
                                    }
                                    
                                    if(result == 0 && hasAvoidQuestions){
                                        if(concepts.get(concept).getAvoidQuestions().contains(q1.getQuestionId()) 
                                                && !concepts.get(concept).getAvoidQuestions().contains(q2.getQuestionId())){
                                
                                            //q1 is avoided while q2 is not, so q1 has lower priority
                                            result = 1;
                                            
                                        } else if(concepts.get(concept).getAvoidQuestions().contains(q2.getQuestionId()) 
                                                && !concepts.get(concept).getAvoidQuestions().contains(q1.getQuestionId())){
                                            
                                            //q2 is avoided while q1 is not, so q1 has higher priority
                                            result = -1;
                                        }
                                    }
                                    
                                    return result;
                                }                           
                            });
                        }
                    }
                    
                    
                    logBuilder.append("\tFiltering by Question Property:\n");
                    
                    String potentialQuestionProperty;
                    boolean isQuestionWithMultipleDifficulties = false;
                    
                    /* add potential questions to the question set until the criteria established by the question parameters 
                     * associated with this concept are met or until there are no more potential questions left to add */                   
                    for(QuestionTypeParameter parameter : concepts.get(concept).getQuestionParams()){
                        
                        //get the requested number of questions with the specified question type parameter
                        final int maxNumberOfQuestions = parameter.getNumberOfQuestions();
                        int numberOfSelectedQuestions = 0;    
                        
                        logBuilder.append("\t\tproperty  = '").append(parameter.getQuestionProperty()).append("' ")
                                  .append("\tvalue  = '").append(parameter.getPropertyValue()).append("' ")
                                  .append("\trequested  = '").append(maxNumberOfQuestions).append("' ");
                        
                        /* use an iterator while traversing the list of potential questions to allow questions to be removed
                         * from the list whenever they are added to the question set */
                        Iterator<AbstractQuestion> potentialQuestionListIterator = potentialQuestionList.iterator();
                        while(potentialQuestionListIterator.hasNext()){
                            
                            AbstractQuestion potentialQuestion = potentialQuestionListIterator.next();
                            
                            if(potentialQuestion != null){                              
                            
                                potentialQuestionProperty = (String)potentialQuestion.getProperties().getPropertyValue(parameter.getQuestionProperty());
                                if(potentialQuestionProperty != null){
                                    //has the property
                                    
                                    if(potentialQuestionProperty.equals(parameter.getPropertyValue())){
                                        //an exact match to the property value versus having it in a delimited list for this property type
                                
                                        /* if a potential question has the specified property with the specified value, add it to the
                                         * question set and remove it from the list of potential questions */
                                        if(questionSet.add(potentialQuestion)){
                                            
                                            logBuilder.append("\n\t\tSelecting question '").append(potentialQuestion.getText()).append("' (Id ")
                                                .append(potentialQuestion.getQuestionId()).append(")\n");
                                            
                                            potentialQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.SELECTED_CONCEPT, concept);
                                            
                                            numberOfSelectedQuestions++;
                                            potentialQuestionListIterator.remove();
                                            
                                            if(numberOfSelectedQuestions >= maxNumberOfQuestions){
                                                break;
                                            }
                                        }
                                    }else if(potentialQuestionProperty.contains(parameter.getPropertyValue())){
                                        //has the property in the delimited list amongst other property values in that list
                                    
                                        isQuestionWithMultipleDifficulties = true;
                                        logBuilder.append("\n\t\t\tSkipping Question '").append(potentialQuestion.getText()).append("' (Id ").append(potentialQuestion.getQuestionId())
                                            .append(") because it has more than one value for the property '")
                                            .append(parameter.getQuestionProperty()).append("'. Questions with multiple values for this property will not be used.\n");
                                    }else{
                                        logBuilder.append("\n\t\t\tSkipping Question '").append(potentialQuestion.getText()).append("' (Id ").append(potentialQuestion.getQuestionId())
                                            .append(") because it has the property '")
                                            .append(parameter.getQuestionProperty()).append("' but not the value of '").append(parameter.getPropertyValue()).append("'\n");
                                    }
                                    
                                }else{
                                    //doesn't have the property
                                    
                                    logBuilder.append("\n\t\t\tSkipping Question '").append(potentialQuestion.getText()).append("' (Id ").append(potentialQuestion.getQuestionId())
                                        .append(") because it doesn't have values for the property '")
                                        .append(parameter.getQuestionProperty()).append("'\n");
                                }
                            }
                        }       
                        
                        logBuilder.append("\n\tTotal questions selected for this concept: ").append(numberOfSelectedQuestions);
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Summary for concept named '"+concept+"':\n"+logBuilder.toString());
                        }
                        
                        if(numberOfSelectedQuestions < maxNumberOfQuestions){
                            //ERROR - unable to find the requested number of questions 
                            
                            if(isQuestionWithMultipleDifficulties){
                                //at least 1 question for the concept was tagged with multiple difficulty values which causes that question to be excluded from selection
                                logger.error("Could not find enough questions in survey context "+surveyContextId+" with property of '" + parameter.getQuestionProperty() + "' and value of '" + parameter.getPropertyValue() +"' for concept named '" + concept + "' that also have correct answers." +
                                        " The request was for "+maxNumberOfQuestions+" but only "+numberOfSelectedQuestions+" were found.  This could be because the questions that are associated with this concept also are associated with other concepts not mentioned "+
                                        "in this request's list of concepts.  Therefore selecting one of those questions could lead to asking the learner about a concept that hasn't been taught yet. This could also be because some of the questions had multiple values for" +
                                        "the property, and were not selected for use in the survey, reducing the number of questions that could be selected.");
                                throw new DetailedException("Unable to satisfy the request to build a Knowledge Assessment survey.",
                                        "Could not find enough questions in survey context "+surveyContextId+" with property of '" + parameter.getQuestionProperty() + "' and value of '" + parameter.getPropertyValue() +"' for concept named '" + concept + "' that also have correct answers." +
                                        " The request was for "+maxNumberOfQuestions+" but only "+numberOfSelectedQuestions+" were found to have these properties.\n\n" +
                                        "Help:\n"+
                                        "i. Are there enough survey questions associated with the survey context which satisfy that rule? Some of the questions analyzed had multiple values for this property and were skipped, reducing the number of questions that could be selected.\n" + 
                                        "ii. Are the questions tagged with this concept also tagged with other concepts?  If so, are those questions tagged with extraneous concepts?"+
                                        " Selecting one of those questions could lead to asking the learner about a concept that hasn't been taught yet.\n"+
                                        "iii. Does every question have a correct answer? A correct answer means that the user can score a positive value if they answer the question while also adhering to any min/max selections allowed rules.\n\n'"+
                                        concept+"' questions search summary:\n"+logBuilder.toString(),
                                        null);
                            }else{
                                logger.error("Could not find enough questions in survey context "+surveyContextId+" with property of '" + parameter.getQuestionProperty() + "' and value of '" + parameter.getPropertyValue() +"' for concept named '" + concept + "' that also have correct answers." +
                                        " The request was for "+maxNumberOfQuestions+" but only "+numberOfSelectedQuestions+" were found.  This could be because the questions that are associated with this concept also are associated with other concepts not mentioned "+
                                        "in this request's list of concepts.  Therefore selecting one of those questions could lead to asking the learner about a concept that hasn't been taught yet.");
                                throw new DetailedException("Unable to satisfy the request to build a Knowledge Assessment survey.",
                                        "Could not find enough questions in survey context "+surveyContextId+" with property of '" + parameter.getQuestionProperty() + "' and value of '" + parameter.getPropertyValue() +"' for concept named '" + concept + "' that also have correct answers." +
                                        " The request was for "+maxNumberOfQuestions+" but only "+numberOfSelectedQuestions+" were found to have these properties.\n\n" +
                                        "Help:\n"+
                                        "i. Are there enough survey questions associated with the survey context which satisfy that rule?\n"+
                                        "ii. Are the questions tagged with this concept also tagged with other concepts?  If so, are those questions tagged with extraneous concepts?"+
                                        " Selecting one of those questions could lead to asking the learner about a concept that hasn't been taught yet.\n"+
                                        "iii. Does every question have a correct answer? A correct answer means that the user can score a positive value if they answer the question while also adhering to any min/max selections allowed rules.\n\n'"+
                                        concept+"' questions search summary:\n"+logBuilder.toString(),
                                        null);
                            }
                        }                   
                    }//end for on requested parameters      

                }//end for on concepts          
                
                //randomize the set of questions since the previous loop ordered the questions by concept
                List<AbstractQuestion> questionList = new ArrayList<AbstractQuestion>(questionSet);
                
                if (!validateOnly) {
                    Collections.shuffle(questionList); 
                }
                
                //create the concept questions survey
                SurveyGiftData surveyGiftData = null;
                Survey conceptsSurvey = new Survey();
                conceptsSurvey.setName(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME);
                
                SurveyProperties properties = new SurveyProperties(new HashMap<SurveyPropertyKeyEnum, Serializable>());
                properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.UNPRESENTABLE, true);
                properties.setSurveyType(SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK);
                conceptsSurvey.setProperties(properties);   
                
                if(createDatabaseEntry){
                    
                    //add the concept questions survey to the database
                    conceptsSurvey = insertSurvey(conceptsSurvey, null);
                }

                //populate the concept questions survey with questions
                List<SurveyPage> pages = new ArrayList<SurveyPage>();          
                    
                SurveyPage questionPage = new SurveyPage(0, "Concept Questions Page", conceptsSurvey.getId());
                
                List<AbstractSurveyElement> questionPageElements = new ArrayList<AbstractSurveyElement>();
                for(int i = 0; i < questionList.size(); i++) {
                    AbstractQuestion question = questionList.get(i);
                    
                    //The questionId should be zero if the survey is going to be pushed into the
                    //database indicating that Hibernate should provide the ids. If this survey 
                    //is not being pushed into the database, we need to set our own unique ids for
                    //each of the surveyQuestions.
                    int questionId = createDatabaseEntry ? 0 : i;
                    
                    AbstractSurveyQuestion<?> questionBankQuestion = AbstractSurveyQuestion.createSurveyQuestion(questionId, questionPage.getId(), question, question.getProperties());
                    questionPageElements.add(questionBankQuestion);
                }

                questionPage.setElements(questionPageElements);
                              
                pages.add(questionPage);
                              
                conceptsSurvey.setPages((ArrayList<SurveyPage>) pages);
                               
                if(createDatabaseEntry){
                    
                    //update the concept questions survey in the database
                    conceptsSurvey = insertSurvey(conceptsSurvey, null);
                    
                    //give the survey context survey a unique key identifying it in the survey context
                    String generatedKey = mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : " + conceptsSurvey.getId();
                    if(!generatedKey.matches(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX)){
                        throw new Exception("The generated GIFT Key of "+generatedKey+" doesn't match the regular expression of "+mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX+" used to filter these generated surveys from the SAS views.");
                    }
                
                    //create a survey context survey to associate the concepts survey with the survey context used to create it 
                    SurveyContextSurvey conceptsSurveyContextSurvey = new SurveyContextSurvey(surveyContextId, generatedKey, conceptsSurvey);                    

                    surveyGiftData = new SurveyGiftData(generatedKey, conceptsSurvey);
                                        
                    insertSurveyContextSurvey(conceptsSurveyContextSurvey, null);
                }
                
                //return the concept questions survey created
                return surveyGiftData == null ? new SurveyGiftData(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY, conceptsSurvey) 
                        : surveyGiftData;
            
            }catch(DetailedException e){
                throw e;
            }catch(Exception e){
                logger.error("Caught an exception while creating an associated concepts survey.", e);
                throw new DetailedException("Unable to satisfy the request to build a Knowledge Assessment survey.",
                            "An exception was thrown while building a knowledge assessment survey for survey context with id "+surveyContextId+". The error reads:\n\n"+e.getMessage(),
                            e);
            }
        }else{
            //didn't find a knowledge assessment question bank authored for the survey context
            throw new DetailedException("Unable to find the knowledge assessment question bank.",
                    "The survey context with id "+surveyContextId+" doesn't have any knowledge assessment questions associated with it.  Use the Survey Authoring System to add questions tagged with concept(s) to this survey context.",
                    null);
        }
    }
    
    /**
     * Creates and gets a new survey containing questions from the specified survey context with the specified concepts.
     * 
     * @param surveyContextId - unique id of a survey context in the db
     * @param concepts - the concepts with which the questions in the survey returned should be associated.  Can't be null.
     * @param createDatabaseEntry whether or not to store the survey being returned in the database for long term storage.
     * @return SurveyGiftData Metadata and a new survey containing questions from the specified survey context with the 
     * specified concepts.
     * @throws DetailedException if there was a problem retrieving the survey based on the concept parameters specified
     */
    public static SurveyGiftData getConceptsSurvey(int surveyContextId, final Map<String, ConceptParameters> concepts, boolean createDatabaseEntry) throws DetailedException{
        
        //get the course question bank survey associated with the specified survey context ID
        Survey conceptSurvey = getSurveyContextSurvey(surveyContextId, mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
        return getConceptsSurvey(conceptSurvey, surveyContextId, concepts, createDatabaseEntry);
    }
    
    
    /**
     * Gets all image files used by a survey.
     * 
     * @param survey the survey that is being checked for images
     * @param imageResources a pass-by-reference ArrayList that, in this method, is used to store image resources found in a survey. 
     * This ArrayList must be instantiated before being passed to this method.
     * @throws IllegalArgumentException if the imageResources parameter is null
     */
    public static void getSurveyImageReferences(Survey survey, ArrayList<File> imageResources) {
        
        if (imageResources == null) {
            throw new IllegalArgumentException("The parameter imageResources cannot be null. It is used as a pass-by-reference variable"
                    + " and needs to be instantiated before being passed into this method.");
        }
        
        // Search all survey pages for images
        for (SurveyPage page : survey.getPages()) {
            
            // Search all survey page elements for images
            for (AbstractSurveyElement element : page.getElements()) {
                
                if (element instanceof AbstractSurveyQuestion) {
                    
                    AbstractQuestion question = ((AbstractSurveyQuestion<?>)element).getQuestion();
                    
                    Set<String> imagePaths = question.getAllAssociatedImages();
                    
                    for(String path : imagePaths) {
                        
                        if(StringUtils.isBlank(path)) {
                            continue;
                        }
                        
                        /* Check if this is a legacy image file that does not have a media type. */
                        boolean isLegacyImage = !question.getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY)
                                || !path.equals(question.getQuestionMedia());
                        
                        if(isLegacyImage) {
                            
                            File imageFile = new File("data" + File.separator + path);
                            
                            //make sure the path is a descendant of the data folder
                            try{
                                if(!imageFile.getCanonicalPath().startsWith(DATA_FOLDER.getCanonicalPath())){
                                    continue;
                                }
                            }catch(IOException e){
                                logger.error("Unable to determine if the image file "+imageFile+" is a descendant of the data folder "+DATA_FOLDER+" when checking for survey image references in the survey "+survey.getName()+" ("+survey.getId()+").", e);
                                continue;
                            }
                            
                            // If a new survey image was found, add it to the list.
                            if (imageFile.exists()) {
                                if (!imageResources.contains(imageFile)) {
                                    imageResources.add(imageFile);
                                }
                            }else {
                                // The survey image reference could not be found, abort the export.
                                throw new IllegalArgumentException("Could not find the survey image " + imageFile.getPath() +
                                        " while exporting the survey '" + survey.getName() + "'. Please locate and place the image in " +
                                        imageFile.getParent() + " or remove the image reference from the survey.");
                            }
                            
                        } else {
                            
                            /* Non-legacy media files are placed in course folders, so they are automatically copied 
                             * with the rest of the course files and do not need to be gathered here */
                        }
                }
            }
        }
        }
    }
    
    /**
     * Used to collect information about survey objects that can be referenced multiple times
     * across surveys when a survey is being deleted.<br/>
     * For example, an option list can be used in multiple questions, across multiple
     * surveys, across multiple courses.
     * 
     * @author mhoffman
     *
     */
    public static class SurveyDeleteConstraintTracker{
        
        /**
         * mapping of option list id to the unique set of question ids for questions
         * that use that option list.  
         */
        public Map<Integer, Set<Integer>> optionListIdToOtherQuestionIds = new HashMap<>();
    }
    
    /**
     * This class is used to collect changes made to the primary key unique ids of survey items
     * when importing a survey context.
     * 
     * @author mhoffman
     *
     */
    public static class ExternalSurveyMapper{

        /** 
         * containers for primary key Id changes to specific types of survey items 
         * 
         * key: original id
         * value: new id
         */
        private Map<Integer, Integer> surveyContextMap = new HashMap<>();
        private Map<Integer, Integer> surveyMap = new HashMap<>();
        private Map<Integer, Integer> surveyQuestionMap = new HashMap<>();
        private Map<Integer, Integer> surveyQuestionReplyMap = new HashMap<>();
        private Map<Integer, DbSurvey> surveyContextSurveyMap = new HashMap<>();
        private Map<Integer, Integer> surveyQuestionOptionListMap = new HashMap<>();
        
        public void addSurveyQuestionOptionList(Integer originalId, Integer newId){
            surveyQuestionOptionListMap.put(originalId, newId);
        }
        
        public Integer getNewSurveyQuestionOptionListId(Integer originalId){
            return surveyQuestionOptionListMap.get(originalId);
        }
        
        public boolean surveyQuestionOptionListExists(Integer id){
            return surveyQuestionOptionListMap.containsKey(id);
        }
        
        public void addSurveyContext(Integer originalId, Integer newId){
            surveyContextMap.put(originalId, newId);
        }
        
        public Integer getNewSurveyContextId(Integer originalId){
            return surveyContextMap.get(originalId);
        }
        
        public void addSurvey(Integer originalId, Integer newId){
            surveyMap.put(originalId, newId);
        }
        
        public Integer getNewSurveyId(Integer originalId){
            return surveyMap.get(originalId);
        }
        
        public void addSurveyQuestion(Integer originalId, Integer newId){
            surveyQuestionMap.put(originalId, newId);
        }
        
        public Integer getNewSurveyQuestionId(Integer originalId){
            return surveyQuestionMap.get(originalId);
        }
        
        public void addSurveyQuestionReply(Integer originalId, Integer newId){
            surveyQuestionReplyMap.put(originalId, newId);
        }
        
        public Integer getNewSurveyQuestionReplyId(Integer originalId){
            return surveyQuestionReplyMap.get(originalId);
        }
              
        public boolean surveyContextExists(Integer id){
            return surveyContextMap.containsKey(id);
        }        
        
        public boolean surveyExists(Integer id){
            return surveyMap.containsKey(id);
        }
        
        public boolean surveyQuestionExists(Integer id){
            return surveyQuestionMap.containsKey(id);
        }
        
        public boolean surveyQuestionReplyExists(Integer id){
            return surveyQuestionReplyMap.containsKey(id);
        }
        
        public void addSurveyContextSurvey(Integer id, DbSurvey newSurvey) {
            surveyContextSurveyMap.put(id, newSurvey);
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ExternalSurveyMapper:\n");
            
            sb.append("Survey Context Id changes:");
            for(Integer originalId : surveyContextMap.keySet()){
                sb.append(" {").append(originalId).append(" -> ").append(surveyContextMap.get(originalId)).append("},");
            }
            sb.append("\n");
            
            sb.append("Survey Question Id changes:");
            for(Integer originalId : surveyQuestionMap.keySet()){
                sb.append(" {").append(originalId).append(" -> ").append(surveyQuestionMap.get(originalId)).append("},");
            }
            sb.append("\n");
            
            sb.append("Survey Question Option List Id changes:");
            for(Integer originalId : surveyQuestionOptionListMap.keySet()){
                sb.append(" {").append(originalId).append(" -> ").append(surveyQuestionOptionListMap.get(originalId)).append("},");
            }
            sb.append("\n");
            
            sb.append("Survey Question List Option Id changes:");
            for(Integer originalId : surveyQuestionReplyMap.keySet()){
                sb.append(" {").append(originalId).append(" -> ").append(surveyQuestionReplyMap.get(originalId)).append("},");
            }
            
            sb.append("]");
            return sb.toString();
        }
    }


    /**
     * Saves a survey from the GAT Survey Editor to the database. This method
     * can be used to insert a new survey or update an existing survey in the
     * database.
     * 
     * @param survey - The survey object to save to the database.
     * @param surveyContextId The survey context id that the survey belongs to.
     * @param session - An existing database session (optional). If null is
     * passed in, a new session is created.
     * @param username - used to determine write permissions
     * @return Survey - The survey object that was saved to the database.
     * @throws Exception if a database operation fails, or if the session parameter doesn't have an active transaction
     */
    public static Survey surveyEditorSaveSurvey(Survey survey, Integer surveyContextId, Session session, String username) throws Exception {

        // check if the survey we want to save is valid. A Question Bank can only have 1 page.
        if (SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK.equals(survey.getProperties().getSurveyType()) && survey.getPageCount() != 1) {
            throw new Exception(
                    "We are trying to save a question bank that contains more than one survey page. Question banks only support one survey page.");
        }
        
        // Get the previous version of the survey if one exists. Use this to
        // determine what elements are being removed and therefore what can be
        // deleted from the database
        Survey oldSurvey = survey.getId() != 0 ? getSurvey(survey.getId()) : null;
        
        //whether or not to commit the transaction in this method
        boolean commit = false;
        
        // Overwrite the editable and visible permissions for the survey. If the
        // survey is new, inherit the permissions from the survey context. If
        // the survey already exists, use whatever permissions were already
        // present on the survey.
        List<String> editableUserNames = null;
        List<String> visibleUserNames = null;
        if (survey.getId() == 0) {
            editableUserNames = getSurveyContextEditableToUserNames(surveyContextId);
            visibleUserNames = getSurveyContextVisibleToUserNames(surveyContextId);
        } else {
            editableUserNames = getSurveyEditableToUserNames(survey.getId());
            visibleUserNames = getSurveyVisibleToUserNames(survey.getId());
        }
        
        if(session == null){
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }else if(!session.getTransaction().isActive()){
            throw new IllegalArgumentException("The session must have an active transaction (i.e. a transaction that has began).");
        }
        
        try {
        
            // Insert / update the questions first.
            for (SurveyPage page : survey.getPages()) {
               
                for (AbstractSurveyElement element : page.getElements()) {
                    if(element instanceof AbstractSurveyQuestion) {
                        @SuppressWarnings("unchecked")
                        AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;
                        
                        AbstractQuestion absQuestion = surveyQuestion.getQuestion();
                        
                        if (editableUserNames != null || visibleUserNames != null) {
                            // Overwrite the editable and visible permissions
                            // for the question. If the question is new, inherit
                            // the permissions from the survey. If the question
                            // already exists, use whatever permissions were
                            // already present on the question.
                            int qId = absQuestion.getQuestionId();
                            absQuestion.getEditableToUserNames().clear();
                            absQuestion.getVisibleToUserNames().clear();
                            if (qId == 0) {
                                absQuestion.getEditableToUserNames().addAll(editableUserNames);                                
                                absQuestion.getVisibleToUserNames().addAll(visibleUserNames);
                            } else {
                                absQuestion.getEditableToUserNames().addAll(getSurveyQuestionEditableToUserNames(qId));
                                absQuestion.getVisibleToUserNames().addAll(getSurveyQuestionVisibleToUserNames(qId));
                            }
                            
                            List<OptionList> optionLists = getOptionLists(absQuestion);
                            if (optionLists != null) {
                                for (OptionList optList : optionLists) {
                                    // Overwrite the editable and visible
                                    // permissions for the option list. If the
                                    // option list is new, inherit the
                                    // permissions from the survey. If the
                                    // option list already exists, use whatever
                                    // permissions were already present on the
                                    // option list.
                                    int listId = optList.getId();
                                    optList.getEditableToUserNames().clear();
                                    optList.getVisibleToUserNames().clear();
                                    if(listId == 0) {
                                        optList.getEditableToUserNames().addAll(editableUserNames);
                                        optList.getVisibleToUserNames().addAll(visibleUserNames);
                                    } else {
                                        optList.getEditableToUserNames().addAll(getOptionListEditableToUserNames(listId));
                                        optList.getVisibleToUserNames().addAll(getOptionListVisibleToUserNames(listId));
                                    }
                                }
                            }
                        }
                        
                        if (absQuestion.getQuestionId() > 0) {
                            
                            boolean updated = Surveys.updateQuestion(absQuestion, username);
                            if(!updated){
                                boolean truncate = absQuestion.getText().length() > 50;
                                throw new Exception("Failed to save the survey question '"+absQuestion.getText().substring(0, truncate ? 50 : absQuestion.getText().length())+(truncate ? "..." : mil.arl.gift.common.io.Constants.EMPTY)+"'.  If available, check the server logs for more information.");
                            }
                            if(logger.isInfoEnabled()){
                                logger.info("Updated question with id: " + absQuestion.getQuestionId());
                            }
                        } else {
                            AbstractQuestion insertedQuestion = Surveys.insertQuestion(absQuestion,  session,  null);
                            
                            if (insertedQuestion != null) {
                                // Update the ids of the newly inserted abstract question.
                                if(logger.isInfoEnabled()){
                                    logger.info("Inserted new question with id: " + insertedQuestion.getQuestionId());
                                }
                                absQuestion.setQuestionId(insertedQuestion.getQuestionId());
                            } else {
                                logger.error("Inserted question is null.");
                                throw new Exception("inserted question was null for question: " + absQuestion.getText());
                            }                            
                        }                        
                        
                    }  
                }
            }//end for surveypages
            
            DbSurvey dbSurvey = giftToHibernate.convertSurvey(survey, session);
            
            // Determine which survey entities are missing and delete them
            if (oldSurvey != null) {
                try {
                    // TODO: (CHUCK #3629) UNDO COMMENT WHEN DELETE IS WORKING
                    // deleteRemovedElements(username, session, oldSurvey, survey);
                } catch (Throwable t) {
                    logger.error("There was a problem while deleting the elements that had been removed by a save from survey id = " + survey.getId(),
                            t);
                }
            }
            
            // Ensure that the survey has the appropriate visible/edit
            // permissions that are already defined in the database
            dbSurvey.getEditableToUserNames().clear();
            dbSurvey.getEditableToUserNames().addAll(editableUserNames);
            dbSurvey.getVisibleToUserNames().clear();
            dbSurvey.getVisibleToUserNames().addAll(visibleUserNames);
            
            if (dbMgr.saveOrUpdateRow(dbSurvey, session)) {
                
                // update survey context last modified date
                String updateSurveyContextModifiedDateQuery = "UPDATE "+dbMgr.getSchemaFromConfig()+".SurveyContext as sc SET sc.lastModified = '"+new java.sql.Timestamp(System.currentTimeMillis())+"' where sc.SURVEYCONTEXTID_PK = "+surveyContextId;
                dbMgr.executeUpdateSQLQuery(updateSurveyContextModifiedDateQuery, session);
                
                    if (commit) {
                        session.getTransaction().commit();
                        session.close();
                    }

                return hibernateToGift.convertSurvey(dbSurvey);
                
            } else {

                logger.error("Could not insert a survey, the survey could not be inserted: " + dbSurvey);
                
                if(commit){
                    session.getTransaction().rollback();
                    session.close();
                }

                return null;
            }

        } catch (Exception e) {
           
            if(commit){
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e;
        }finally{
            
            if(commit && session != null && session.isOpen()){
                session.close();
            }
        }
                
    }
    
    /**
     * Deletes the elements from the database that no longer exist in the new survey when compared
     * to the old survey.
     * 
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @param oldSurvey the old survey to be used as a baseline.
     * @param newSurvey the new survey that we can use to determine which elements were removed from
     *            the old survey.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @throws Throwable when there is a problem removing elements below the survey page level.
     */
    @SuppressWarnings("unused")
    private static void deleteRemovedElements(String username, Session session, Survey oldSurvey, Survey newSurvey, SurveyDeleteConstraintTracker tracker) throws Throwable {
        for (SurveyPage oldPage : oldSurvey.getPages()) {
            boolean pageFound = false;
            for (SurveyPage newPage : newSurvey.getPages()) {
                if (newPage.getId() != oldPage.getId()) {
                    continue;
                }

                pageFound = true;
                
                for(AbstractSurveyElement oldElement : oldPage.getElements()) {
                    boolean elementFound = false;
                    for(AbstractSurveyElement newElement : newPage.getElements()) {
                        if(oldElement.getId() != newElement.getId()) {
                            continue;
                        }
                        
                        elementFound = true;
                        
                        if(oldElement instanceof AbstractSurveyQuestion) {
                            AbstractSurveyQuestion<? extends AbstractQuestion> oldSurveyQuestion = (AbstractSurveyQuestion<?>) oldElement;
                            AbstractSurveyQuestion<? extends AbstractQuestion> newSurveyQuestion = (AbstractSurveyQuestion<?>) newElement;
                            
                            OptionList oldOptionList = (OptionList) oldSurveyQuestion
                                    .getQuestion()
                                    .getProperties()
                                    .getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                            OptionList newOptionList = (OptionList) newSurveyQuestion
                                    .getQuestion()
                                    .getProperties()
                                    .getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                            
                            if(oldOptionList == null || oldOptionList.getIsShared()) {
                                // There was no option list before the save or
                                // it's a shared list so it shouldn't be deleted
                                break;
                            } else if(newOptionList == null || oldOptionList.getId() != newOptionList.getId()) {
                                // The option list has either been
                                // removed or changed, therefore delete
                                // the old version
                                deleteOptionList(oldOptionList.getId(), username, session, tracker);
                            } else {
                                // Delete any ListOptions that are no longer
                                // included in the option list
                                for(ListOption oldListOption : oldOptionList.getListOptions()) {
                                    boolean listOptionFound = false;
                                    for(ListOption newListOption : newOptionList.getListOptions()) {
                                        if(oldListOption.getId() == newListOption.getId()) {
                                            listOptionFound = true;
                                            break;
                                        }                                        
                                    }
                                    
                                    if(!listOptionFound) {
                                        deleteListOption(oldListOption.getId(), username, session);
                                    }
                                }
                            }
                        }
                        
                        break;
                    }
                    
                    if(!elementFound) {
                        //Delete the question if necessary
                        if(oldElement instanceof AbstractSurveyQuestion) {
                            AbstractQuestion question = ((AbstractSurveyQuestion<?>) oldElement).getQuestion();
                            deleteQuestion(question.getQuestionId(), username, session, tracker);
                            
                            //If it's multiple choice, matrix, or rating scale, delete the OptionList(s)
                            if(question instanceof MatrixOfChoicesQuestion) {
                                MatrixOfChoicesQuestion matrixQuestion = (MatrixOfChoicesQuestion) question;
                                OptionList rows = matrixQuestion.getRowOptions();
                                OptionList cols = matrixQuestion.getColumnOptions();
                                
                                if(!cols.getIsShared()) {
                                    deleteOptionList(cols.getId(), username, session, tracker);
                                }
                                
                                if(!rows.getIsShared()) {
                                    deleteOptionList(rows.getId(), username, session, tracker);
                                }
                            } else if(question instanceof RatingScaleQuestion) {
                                RatingScaleQuestion ratingQuestion = (RatingScaleQuestion) question;
                                OptionList optionList = ratingQuestion.getReplyOptionSet();
                                if(!optionList.getIsShared()) {
                                    deleteOptionList(optionList.getId(), username, session, tracker);
                                }
                            } else if(question instanceof MultipleChoiceQuestion) {
                                MultipleChoiceQuestion mcQuestion = (MultipleChoiceQuestion) question;
                                OptionList optionList = mcQuestion.getReplyOptionSet();
                                if(!optionList.getIsShared()) {
                                    deleteOptionList(optionList.getId(), username, session, tracker);
                                }
                            }
                        }
                        
                        //Delete the element's properties
                    }
                }
            }

            if (!pageFound) {
                deleteSurveyPage(oldPage.getId(), username, session, tracker);
            }
        }
    }
    
    /**
     * Retrieves the list of users with editable permissions to the survey context.
     * 
     * @param surveyContextId the id of the survey context
     * @return list of users with editable survey context permissions. Can return empty.
     */
    private static List<String> getSurveyContextEditableToUserNames(Integer surveyContextId) {
        List<String> usernames = new ArrayList<String>();
        if (surveyContextId != null) {
            String query = "SELECT scEditable.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                    + ".SurveyContextEditableToUserNames as scEditable WHERE scEditable.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = " + surveyContextId;
            List<Object> results = dbMgr.executeSelectSQLQuery(query);
            for (Object name : results) {
                usernames.add((String) name);
            }
        }

        return usernames;
    }
    
    /**
     * Retrieves the list of users with editable permissions to the survey.
     * 
     * @param surveyId the id of the survey
     * @return list of users with editable survey permissions. Can return empty.
     */
    private static List<String> getSurveyEditableToUserNames(Integer surveyId) {
        List<String> usernames = new ArrayList<String>();
        
        if(surveyId == null) {
            return usernames;
        }
        
        String query = "SELECT sEditable.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".SurveyEditableToUserNames as sEditable WHERE sEditable.DBSURVEY_SURVEYID_PK = " + surveyId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for(Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }
    
    /**
     * Retrieves the list of users with editable permissions to the survey question.
     * 
     * @param questionId the id of the survey question
     * @return list of users with editable survey question permissions. Can return empty.
     */
    private static List<String> getSurveyQuestionEditableToUserNames(Integer questionId) {
        List<String> usernames = new ArrayList<String>();
        
        if(questionId == null) {
            return usernames;
        }
        
        String query = "SELECT qEditable.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".QuestionEditableToUsernames as qEditable WHERE qEditable.DBQUESTION_QUESTIONID_PK = " + questionId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for (Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }
    
    /**
     * Retrieves the list of users with editable permissions to the option list.
     * 
     * @param optionListId the id of the option list
     * @return list of users with editable option list permissions. Can return empty.
     */
    private static List<String> getOptionListEditableToUserNames(Integer optionListId) {
        List<String> usernames = new ArrayList<String>();
        
        if(optionListId == null) {
            return usernames;
        }
        
        String query = "SELECT listEditable.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".OptionListEditableToUsernames as listEditable WHERE listEditable.DBOPTIONLIST_OPTIONLISTID_PK = " + optionListId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for (Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }

    /**
     * Retrieves the list of users with visible permissions to the survey context.
     * 
     * @param surveyContextId the id of the survey context
     * @return list of users with visible survey context permissions. Can return empty.
     */
    private static List<String> getSurveyContextVisibleToUserNames(Integer surveyContextId) {
        List<String> usernames = new ArrayList<String>();
        if (surveyContextId != null) {
            String query = "SELECT scVisible.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                    + ".SurveyContextVisibleToUserNames as scVisible WHERE scVisible.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = " + surveyContextId;
            List<Object> results = dbMgr.executeSelectSQLQuery(query);
            for (Object name : results) {
                usernames.add((String) name);
            }
        }

        return usernames;
    }
    
    /**
     * Retrieves the list of users with visible permissions to the survey.
     * 
     * @param surveyId the id of the survey
     * @return list of users with visible survey permissions. Can return empty.
     */
    private static List<String> getSurveyVisibleToUserNames(Integer surveyId) {
        List<String> usernames = new ArrayList<String>();
        
        if(surveyId == null) {
            return usernames;
        }
        
        String query = "SELECT sVisible.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".SurveyVisibleToUserNames as sVisible WHERE sVisible.DBSURVEY_SURVEYID_PK = " + surveyId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for(Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }
    
    /**
     * Retrieves the list of users with visible permissions to the survey question.
     * 
     * @param questionId the id of the survey question
     * @return list of users with visible survey question permissions. Can return empty.
     */
    private static List<String> getSurveyQuestionVisibleToUserNames(Integer questionId) {
        List<String> usernames = new ArrayList<String>();
        
        if(questionId == null) {
            return usernames;
        }
        
        String query = "SELECT qVisible.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".QuestionVisibleToUsernames as qVisible WHERE qVisible.DBQUESTION_QUESTIONID_PK = " + questionId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for (Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }
    
    /**
     * Retrieves the list of users with visible permissions to the option list.
     * 
     * @param optionListId the id of the option list
     * @return list of users with visible option list permissions. Can return empty.
     */
    private static List<String> getOptionListVisibleToUserNames(Integer optionListId) {
        List<String> usernames = new ArrayList<String>();
        
        if(optionListId == null) {
            return usernames;
        }
        
        String query = "SELECT listVisible.USERNAME FROM " + dbMgr.getSchemaFromConfig()
                + ".OptionListVisibleToUsernames as listVisible WHERE listVisible.DBOPTIONLIST_OPTIONLISTID_PK = " + optionListId;
        
        List<Object> results = dbMgr.executeSelectSQLQuery(query);
        for (Object name : results) {
            usernames.add((String) name);
        }
        
        return usernames;
    }
    
    /**
     * Update the database representation of the question.
     * 
     * @param question - The question to be updated in the database.
     * @param username - used to check for write permissions on this question.  If the specified user doesn't have
     * write permissions then false is returned.
     * @return boolean - true if the question was updated, false otherwise.
     */
    public static boolean updateQuestion(AbstractQuestion question, String username) {
        
        if(isQuestionEditable(question, username)){
            
            Session session = dbMgr.createNewSession();

            session.beginTransaction();

            try {

                DbQuestion dbQuestion = giftToHibernate.convertQuestion(question, session, null);

                if (dbMgr.updateRow(dbQuestion, session)) {

                    session.getTransaction().commit();

                    session.close();

                    return true;

                } else {
                    
                    session.getTransaction().rollback();

                    logger.error("Could not update question, the question could not be update: " + dbQuestion);

                    session.close();

                    return false;
                }

            } catch (Exception e) {
                
                session.getTransaction().rollback();

                logger.error("Caught an exception while update a question", e);

                session.close();
            }
        }

        return false;
    }
    
    /**
     * Updates the survey permissions for the given user with the specified permission in the survey
     * database.
     * 
     * @param ownerUsername the gift username that owns the course that contains this survey
     *            context. This is used to make sure that if the owner doesn't have write access to
     *            something than neither should the user the owner is managing permissions for.
     * @param surveyContextId the id of survey context to update permission for. This will include
     *            surveys, questions and option list permission updates. Can't be null and must
     *            exist in the survey db.
     * @param username the gift username to change permissions for, this can mean adding or removing
     *            this user. Can't be null or empty.
     * @param permissionEnum the permission type to apply for this user. If null than both read and
     *            write will be removed for the user.
     * @return True if the survey context permissions was updated, false otherwise.
     * @throws DetailedException if there was a problem updating the survey context
     */
    public static boolean updateSurveyContextPermissions(String ownerUsername, Integer surveyContextId, String username, SharedCoursePermissionsEnum permissionEnum) throws DetailedException {
        
        if(surveyContextId == null){
            return false;
        }else if(username == null || username.isEmpty()){
            return false;
        }else if(ownerUsername == null || ownerUsername.isEmpty()){
            return false;
        }

        // check if the new permission is allowing the user to edit or view. If allowed to edit,
        // then they can also view.
        boolean isEditable = SharedCoursePermissionsEnum.EDIT_COURSE.equals(permissionEnum);
        boolean isViewable = isEditable || SharedCoursePermissionsEnum.VIEW_COURSE.equals(permissionEnum);

        Session session = dbMgr.createNewSession();
        try{
            
            session.beginTransaction();

            // 
            // Retrieve the survey context surveys (and not the entire DbSurveyContext for performance reasons) 
            // by performing a query that requests all of the surveys in this survey context that are NOT dynamically generated
            //
            String queryString = "from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId 
                    + " and scs.giftKey not like '" + Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : %'";
             
            final int UNUSED_INDEX = -1;
            
            Set<DbSurveyContextSurvey> dbSurveyContextSurveys = new HashSet<DbSurveyContextSurvey>(
                    dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
            if (dbSurveyContextSurveys != null) {
                for (DbSurveyContextSurvey surveyContextSurvey : dbSurveyContextSurveys) {
                    if (surveyContextSurvey.getSurvey() != null) {
                        DbSurvey survey = surveyContextSurvey.getSurvey();

                        //
                        // update view/edit permissions for survey
                        //
                        
                        //owner has write permission, therefore can manage write access to another user
                        if(isSurveyEditable(survey, ownerUsername)){
                            if (isEditable) {
                                survey.getEditableToUserNames().add(username);
                            } else {
                                survey.getEditableToUserNames().remove(username);
                            }
                        }
                        
                        //owner has view permissions, therefore can manage view access to another user
                        if(isSurveyVisible(survey, ownerUsername)){
                            if (isViewable) {
                                survey.getVisibleToUserNames().add(username);
                            } else {
                                survey.getVisibleToUserNames().remove(username);
                            }
                        }

                        if (!survey.getSurveyPages().isEmpty()) {
                            for (DbSurveyPage page : survey.getSurveyPages()) {
                                if (page.getSurveyElements() != null) {
                                    for (DbSurveyElement surveyElem : page.getSurveyElements()) {
                                        // retrieve the question
                                        DbQuestion question = getDbQuestion(surveyElem.getQuestionId(), session);
                                        if (question != null) {

                                            //
                                            // update view/edit permissions for the questions
                                            //
                                            
                                            //owner has write permission, therefore can manage write access to another user
                                            if(isQuestionEditable(question, ownerUsername)){
                                                if (isEditable) {
                                                    question.getEditableToUserNames().add(username);
                                                } else {
                                                    question.getEditableToUserNames().remove(username);
                                                }
                                            }
                                            
                                            //owner has write permission, therefore can manage write access to another user
                                            if(isQuestionVisible(question, ownerUsername)){
                                                if (isViewable) {
                                                    question.getVisibleToUserNames().add(username);
                                                } else {
                                                    question.getVisibleToUserNames().remove(username);
                                                }
                                            }

                                            // retrieve the option lists
                                            List<DbOptionList> optionLists = getDbOptionList(question);
                                            if (optionLists != null) {
                                                for (DbOptionList optList : optionLists) {

                                                    // update view/edit permissions for the option
                                                    // lists
                                                    
                                                    //owner has write permission, therefore can manage write access to another user
                                                    if(isOptionListEditable(optList, ownerUsername)){
                                                        if (isEditable) {
                                                            optList.getEditableToUserNames().add(username);
                                                        } else {
                                                            optList.getEditableToUserNames().remove(username);
                                                        }
                                                    }
                                                    
                                                    //owner has write permission, therefore can manage write access to another user
                                                    if(isOptionListViewable(optList, ownerUsername)){
                                                        if (isViewable) {
                                                            optList.getVisibleToUserNames().add(username);
                                                        } else {
                                                            optList.getVisibleToUserNames().remove(username);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 
            // update view/edit permissions for survey context
            //
            // Note: use SQL queries instead of building the DbSurveyContext for performance reasons
            //
            // Note: have the following set of logic that updates the survey context permission tables before the above update the survey permission tables
            //      would cause the following exception: org.hibernate.HibernateException: A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance: entities.Parent.childs
            //      
            String updateSurveyContextModifiedDateQuery = "UPDATE "+dbMgr.getSchemaFromConfig()+".SurveyContext as sc SET sc.lastModified = '"+new java.sql.Timestamp(System.currentTimeMillis())+"' where sc.SURVEYCONTEXTID_PK = "+surveyContextId;
            dbMgr.executeUpdateSQLQuery(updateSurveyContextModifiedDateQuery, session);
            
            String selectSurveyContextEditableQuery = "SELECT sceditable.USERNAME FROM "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTEDITABLETOUSERNAMES  as sceditable WHERE sceditable.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId;
            List<Object> scEditableRows = dbMgr.executeSelectSQLQuery(selectSurveyContextEditableQuery);
            
            String selectSurveyContextVisibleQuery = "SELECT scvisible.USERNAME FROM "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTVISIBLETOUSERNAMES as scvisible WHERE scvisible.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId;
            List<Object> scVisibleRows = dbMgr.executeSelectSQLQuery(selectSurveyContextVisibleQuery);

            //owner has write permission, therefore can manage write access to another user
            if(scEditableRows.contains(ownerUsername) || scEditableRows.contains(Constants.EDITABLE_WILDCARD)){
                if (isEditable && !scEditableRows.contains(username)) {
                    //execute query to add permission for user because it doesn't exist currently
                    String insertSurveyContextEditableQuery = "INSERT INTO "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTEDITABLETOUSERNAMES VALUES ("+surveyContextId+", '"+username+"')";
                    dbMgr.executeInsertSQLQuery(insertSurveyContextEditableQuery, session);
                } else if(!isEditable && scEditableRows.contains(username)){
                    //execute query to remove permission for user because it does exist currently
                    String deleteSurveyContextEditableQuery = "DELETE FROM "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTEDITABLETOUSERNAMES as sceditable WHERE sceditable.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId+" AND sceditable.USERNAME = '"+username+"'";
                    dbMgr.executeDeleteSQLQuery(deleteSurveyContextEditableQuery, session);
                }
            }
            
            //owner has view permission, therefore can manage view access to another user
            if(scVisibleRows.contains(ownerUsername) || scVisibleRows.contains(Constants.VISIBILITY_WILDCARD)){
                if (isViewable && !scVisibleRows.contains(username)) {
                    //execute query to add permission for user because it doesn't exist currently
                    String insertSurveyContextVisibleQuery = "INSERT INTO "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTVISIBLETOUSERNAMES VALUES ("+surveyContextId+", '"+username+"')";
                    dbMgr.executeInsertSQLQuery(insertSurveyContextVisibleQuery, session);
                } else if(!isViewable && scVisibleRows.contains(username)){
                    //execute query to remove permission for user because it does exist currently
                    String deleteSurveyContextVisibleQuery = "DELETE FROM "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTVISIBLETOUSERNAMES as scvisible WHERE scvisible.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId+" AND scvisible.USERNAME = '"+username+"'";
                    dbMgr.executeDeleteSQLQuery(deleteSurveyContextVisibleQuery, session);
                }
            }

            session.getTransaction().commit();
            
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }
        
        return true;
    }
    
    /**
     * Updates a survey context in the database
     *
     * @param surveyContext The survey context to update in the database
     * @return If the update was successful
     */
    public static boolean updateSurveyContext(SurveyContext surveyContext) {

        Session session = dbMgr.createNewSession();

        session.beginTransaction();

        try {

            DbSurveyContext dbSurveyContext = giftToHibernate.convertSurveyContext(surveyContext, session);
            dbSurveyContext.setLastModified(new Date());

            session.clear();
            
            if (dbMgr.updateRow(dbSurveyContext, session)) {

                session.getTransaction().commit();

                session.close();

                return true;

            } else {

                session.getTransaction().rollback();

                logger.error("Could not update a survey context, the survey context could not be updated: " + dbSurveyContext);

                session.close();

                return false;
            }

        } catch (Exception e) {

            session.getTransaction().rollback();

            logger.error("Caught an exception while updating a survey context", e);

            session.close();
        }

        return false;
    }
    
    /**
     * Updates a survey in the database
     *
     * @param gwtSurvey The survey to update in the database
     * @return boolean If the update was successful
     */
    public static Survey updateSurvey(Survey gwtSurvey) {

        Session session = dbMgr.createNewSession();

        session.beginTransaction();

        try {

            DbSurvey dbSurvey = giftToHibernate.convertSurvey(gwtSurvey, session);
             
            if (dbMgr.updateRow(dbSurvey, session)) {
 
                session.getTransaction().commit();

                session.close();

                return hibernateToGift.convertSurvey(dbSurvey);

            } else {
                
                session.getTransaction().rollback();

                logger.error("Could not update survey, the survey could not be updated: " + dbSurvey);

                session.close();

                return null;
            }

        } catch (Exception e) {
            
            session.getTransaction().rollback();

            logger.error("Caught an exception while updating a survey", e);
            
            session.close();
        }
        
        return null;
    }
    
    /**
     * This method attempts to safely delete a survey context if the user has permission. Survey context surveys are checked to make sure
     * shared surveys are not deleted. The surveys are checked for responses and deleted if specified, otherwise survey responses will be 
     * reported back in the result and the survey will not be deleted.
     *  
     * @param surveyContextId The id of the survey context
     * @param userName used to check if the user has permissions to delete
     * @param shouldDeleteResponses true if responses should be deleted. Otherwise, survey responses will be reported back.
     * @return a result containing information about the delete operation
     * @throws Exception if there was a problem retrieving the survey context
     */
    public static DeleteSurveyContextResponse deleteSurveyContextAndResponses(int surveyContextId, String userName, boolean shouldDeleteResponses) throws Exception {
    
        DeleteSurveyContextResponse result = new DeleteSurveyContextResponse();
            
        if(shouldDeleteResponses) {
                
            try{
                deleteSurveyResponses(surveyContextId, true, userName);
            }catch(Exception e){
                
                logger.error("Failed to delete the survey responses to survey context "+surveyContextId+" for "+userName, e);
                
                //Report that deletion failed on a survey
                result.setIsSuccess(false);
                result.setResponse("One or more learner responses could not be deleted. The most likely reason "
                            + "is that another user may be using these resources for an ongoing operation.");
                result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                return result;
            }           
        }else if(doesSurveyContextHaveResponses(surveyContextId)){
            
            //Report survey dependencies back to the user so that they can determine whether or not to delete the survey responses
            result.setIsSuccess(false);
            result.setHadSurveyResponses(true);
            result.setResponse("One or more of this course's surveys has recorded responses "
                    + "from learners who have taken this course. These responses need to be removed "
                    + "before any other survey resources can be deleted.");
            return result;
        }
                    
        try{
            deleteSurveyContextWithNoResponses(surveyContextId, userName);

            //All survey resources were successfully deleted
            result.setIsSuccess(true);
            return result;
            
        }catch(Exception e){
            
            logger.error("Failed to delete survey context", e);
            
            //Report that deletion failed on a survey context
            result.setIsSuccess(false);
            result.setResponse("The most likely reason is that another user may be "
                    + "using these resources for an ongoing operation.");
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            return result;
            
        } 
    }
    
    /**
     * Gets the print out of dependencies for a survey context, returning "none"
     * if there are no dependencies or null if there was error
     *
     * @param surveyContextId The ID of the survey context
     * @return String The print out of dependencies for a survey context
     */
    public static String getSurveyContextDependencies(int surveyContextId) {
        try {
            int surveyResponseCount = 0;
            
            String queryString = "from DbSurveyResponse as response where response.surveyContext.surveyContextId = " + surveyContextId;
            if(logger.isDebugEnabled()){
                logger.debug("queryString = " + queryString);
            }
            List<DbSurveyResponse> dbSurveyResponses = dbMgr.selectRowsByQuery(DbSurveyResponse.class, queryString, UNUSED_INDEX, UNUSED_INDEX);
            
            for (DbSurveyResponse dbSurveyResponse : dbSurveyResponses) {
                if (dbSurveyResponse.getSurveyContext().getSurveyContextId() == surveyContextId) {
                    surveyResponseCount += 1;
                }
            }
            if (surveyResponseCount > 0) {
                StringBuilder dependenciesString = new StringBuilder();
                if (surveyResponseCount > 0) {
                    dependenciesString.append(surveyResponseCount);
                    if (surveyResponseCount > 1) {
                        dependenciesString.append(" Survey Responses reference");
                    } else {
                        dependenciesString.append(" Survey Response references");
                    }
                    dependenciesString.append(" this survey context. Editing this will cause future results of the survey context to be different!<br/>");
                }
                return dependenciesString.toString();
            } else {
                return "none";
            }
        } catch (Exception e) {
            logger.error("Caught an exception while getting the dependencies for a survey context", e);
        }
        return null;
    }
    
    /**
     * Deletes all survey responses associated with the specified survey context from the database.
     * 
     * @param surveyContextId The survey context whose responses are to be deleted.
     * @param deleteGeneratedConceptSurveys whether or not to delete any generated concept surveys associated with this
     * survey context.  This is useful when cleaning/deleting survey responses to these generated surveys.
     * @param userName used to check if the user has permissions to delete
     * @throws Exception if there was a problem deleting
     */
    private static void deleteSurveyResponses(int surveyContextId, boolean deleteGeneratedConceptSurveys, String userName) throws Exception{
        
        Session session = dbMgr.createNewSession();
        
        try {   
            
            session.beginTransaction();
            
            if(!doesSurveyContextExist(surveyContextId)){
                return;
            }else if(isSurveyContextEditable(surveyContextId, userName)){
                
                //change to query using survey id AND survey context id on survey response table
                //perform a query that requests all of the survey responses in this survey context that are for the specified survey
                String queryString = "from DbSurveyResponse as sr where sr.surveyContext.surveyContextId = " + surveyContextId;
                
                Set<DbSurveyResponse> dbSurveyResponses = new HashSet<DbSurveyResponse>(
                        dbMgr.selectRowsByQuery(DbSurveyResponse.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
                
                try{
                    for(DbSurveyResponse response : dbSurveyResponses){
        
                        try{
                            dbMgr.deleteRow(response, session);
                        }catch(Exception e){
                            logger.error("Failed to delete survey responses in survey context "+surveyContextId+" for "+userName, e);
                            break;
                        }
        
                    }
                }catch(Exception e){
                    throw new Exception("Failed to delete the survey responses to the question bank survey.", e);
                }                
                
                //Note: commit this transaction before attempting to delete the generated surveys for question bank surveys
                //      otherwise a foreign key constraint exception will be thrown because you are trying to delete surveys
                //      which could have responses of which reference those surveys.
                session.getTransaction().commit();
                
                if(deleteGeneratedConceptSurveys){
                    deleteConceptSurveys(surveyContextId, userName);
                }
            }else{
                throw new Exception("Failed to delete the survey responses for a survey context because '"+userName+"' doesn't have write access to the survey context (id : "+surveyContextId+").");
            }
             
         } catch (Exception e) {             
             throw e;
         }finally{
             
             if(session != null && session.isOpen()){
                 session.close();
             }
         }
    }
    
    /**
     * Deletes all survey responses associated with the specified survey id from the database.
     * 
     * @param surveyContextId The id of the survey context whose responses are to be deleted.
     * @param surveyId The survey id for the responses that should be deleted.
     * @param userName used to check if the user has permissions to delete
     * @throws Exception if there was a problem deleting
     */
    public static void deleteSurveyResponses(int surveyContextId, int surveyId, String userName) throws Exception {
        
        Session session = dbMgr.createNewSession();
        
        try {   
            
            session.beginTransaction();
            
            if(!doesSurveyContextExist(surveyContextId)){
                return;
            }else if(isSurveyContextEditable(surveyContextId, userName)){
                
                //change to query using survey id AND survey context id on survey response table
                //perform a query that requests all of the survey responses in this survey context that are for the specified survey
                String queryString = "from DbSurveyResponse as sr where sr.surveyContext.surveyContextId = " + surveyContextId 
                        + " and sr.survey.surveyId = " + surveyId;
                
                Set<DbSurveyResponse> dbSurveyResponses = new HashSet<DbSurveyResponse>(
                        dbMgr.selectRowsByQuery(DbSurveyResponse.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
                           
                int deleteCount = dbSurveyResponses.size();
                for(DbSurveyResponse response : dbSurveyResponses){
                    dbMgr.deleteRow(response, session);
                }
                
                if(logger.isTraceEnabled()){
                    logger.trace("deleteSurveyResponsesForSurveyId - deleted: " + deleteCount + " rows for survey id: " + surveyId);
                }
                
                session.getTransaction().commit();
                
            }else{
                throw new Exception("Failed to delete the survey responses for survey with id "+surveyId+" because '"+userName+"' doesn't have write access to the survey context (id : "+surveyContextId+").");
            }
             
         } catch (Exception e) {               
             throw e;
         }finally{
             if(session != null && session.isOpen()){
                 session.close();
             }
         }
        
    }
    
    /**
     * Fully deletes the survey and questions used by the survey.<br/>  
     * This will go through and clean up both the survey AND the questions that are used by the survey.<br/>  
     * This does NOT check to see if the survey is used in other survey contexts, or if the questions are being
     * used in other surveys.  A separate rpc/check should be made if that is needed.<br/>  
     * This will also delete any survey responses and any references to this survey in existing survey contexts.
     * 
     * @param surveyId The id of the survey to be deleted.
     * @param username used to check if the user has permissions to delete
     * @param session the session to perform the delete operation.  If null a new session is created, with a new transaction, the transaction
     * is committed and the session is closed.  If the session is not null, it must have a transaction already began and the transaction+session
     * will not be committed or closed in this method.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @throws Exception if there was a problem deleting the survey
     */
    public static void deleteSurveyAndQuestions(int surveyId, String username, Session session, SurveyDeleteConstraintTracker tracker) throws Exception {  
        
        // Creates a session if one was not provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        }
        try{
            
            DbSurvey dbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class);
            
            if(dbSurvey != null && isSurveyEditable(dbSurvey, username)){
                
                deleteSurvey(surveyId, username, session, tracker);
                
                if(logger.isInfoEnabled()){
                    logger.info("Successfully deleted survey with id: " + surveyId );
                }
            }
        }catch(Exception e){
            throw new Exception("Unable to fully delete survey with id "+surveyId+" because an error occurred during deletion.", e);
            
        }finally{
                        
            if(commit && session != null && session.isOpen()){
                session.getTransaction().commit();
                session.close();
            }
        }
    }

    /**
     * Deletes a given survey element from the database
     * 
     * @param dbElement the element to delete
     * @param username the username who is deleting the element, used for
     * authentication
     * @param session the session to perform the delete operation.  If null a new session is created, with a new transaction, the transaction
     * is committed and the session is closed.  If the session is not null, it must have a transaction already began and the transaction+session
     * will not be committed or closed in this method.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the survey element was successfully deleted, false
     * otherwise.
     * @throws Throwable if there is an error while deleting the survey element
     */
    private static boolean deleteSurveyElement(DbSurveyElement dbElement, String username, Session session, SurveyDeleteConstraintTracker tracker) throws Throwable {
        // Creates a session if one was not provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.getCurrentSession();
            session.beginTransaction();
            commit = true;
        }

        try {            
            Iterator<DbSurveyElementProperty> propIter = dbElement.getProperties().iterator();
            while(propIter.hasNext()) {
                DbSurveyElementProperty dbElementProp = propIter.next();
                propIter.remove();
                deleteSurveyElementProperty(dbElementProp, username, session);
            }
            
            // Deletes the element in question
            dbMgr.deleteRow(dbElement, session);
            
            // Delete the question if it exists
            if(dbElement.getQuestionId() != 0) {
                deleteQuestion(dbElement.getQuestionId(), username, session, tracker);
            }
            
            return true;
        } catch (Throwable t) {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }

            throw t;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes a newDbQuestion from the database
     *
     * @param questionId The ID of the newDbQuestion to remove from the database
     * @param userName used to check if the user has permissions to delete
     * @param session the session to do the work in (Optional). If not provided
     * every delete row operation called by this method is done in a separate
     * transaction.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return true if the question was found and deleted.  False can be returned if the question doesn't exist in the db.
     * @throws Throwable if the delete operation failed
     */
    private static boolean deleteQuestion(int questionId, String userName, Session session, SurveyDeleteConstraintTracker tracker) throws Throwable {

        DbQuestion dbQuestion = dbMgr.selectRowById(questionId, DbQuestion.class, session);

        if (dbQuestion == null) {
            return false;
        }

        if (isQuestionEditable(dbQuestion, userName)) {
            
            // Dis-associate question categories with this question that is being deleted (but don't delete the category
            // because it is most likely associated with other questions in the survey db)
            // #3981 - when copying a survey (using select existing in the GAT) which has a question which is associated
            //         with a question category, that category was being assigned to the new question as well.  This is ok
            //         as the question is another instance of a question in that category.  The problem here is when deleting
            //         the newly created question (created from the question copy), the Hibernate logic attempts to delete
            //         the category as well.  This can cause a foreign key constraint exception if other questions are
            //         still using that category. The solution here is to clear out the references to categories (which are global)
            //         before deleting the question from the db.
            dbQuestion.getCategories().clear();
            String categoriesQuery = "FROM DbQuestionCategory as qc WHERE qc.question.questionId = "+dbQuestion.getQuestionId();
            List<DbQuestionCategory> categoryObjects = dbMgr.selectRowsByQuery(DbQuestionCategory.class, categoriesQuery, -1, -1, session);
            for (DbQuestionCategory categoryObject : categoryObjects) {
                dbMgr.deleteRow(categoryObject, session);
            }

            // Collects the categories and the properties for later deletion
            Collection<DbQuestionProperty> propsToDelete = new ArrayList<>(dbQuestion.getQuestionProperties());
            
            dbMgr.deleteRow(dbQuestion, session);
            
            // Delete all the question properties
            for (DbQuestionProperty property : propsToDelete) {
                deleteQuestionProperty(property, userName, session, tracker);
            }
            
            if(tracker != null){
                // now that the question has been removed, update the set of questions referencing
                // option lists by removing this question.  The idea being if there are questions
                // remaining after deleting all the survey items this thread is deleting than the
                // option list is safe to remove.  Hibernate apparently can't handle our complicated
                // tables, even in a single transaction.
                for(Set<Integer> questionIds : tracker.optionListIdToOtherQuestionIds.values()){
                    
                    if(questionIds != null){
                        questionIds.remove(dbQuestion.getQuestionId());
                    }
                }
            }
            
            return true;
            
        }
        
        return false;
    }
    
    /**
     * Deletes a survey property from the database.
     * 
     * @param dbSurveyProperty the question property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return True if the survey property was deleted, false otherwise.
     * @throws Throwable
     */
    private static boolean deleteSurveyProperty(DbSurveyProperty dbSurveyProperty, String username, Session session) throws Throwable {
        if (dbSurveyProperty == null) {
            return false;
        }

        // Get a session if none was provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.getCurrentSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            DbPropertyValue dbPropValue = dbSurveyProperty.getPropertyValue();
            dbMgr.deleteRow(dbSurveyProperty, session);
            deletePropertyValue(dbPropValue, username, session);
            return true;
        } catch (Throwable t) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw t;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Deletes a survey page property from the database.
     * 
     * @param dbSurveyPageProperty the question property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return True if the survey page property was deleted, false otherwise.
     * @throws Exception
     */
    private static boolean deleteSurveyPageProperty(DbSurveyPageProperty dbSurveyPageProperty, String username, Session session) throws Exception {
        if (dbSurveyPageProperty == null) {
            return false;
        }

        // Get a session if none was provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.getCurrentSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            DbPropertyValue dbPropValue = dbSurveyPageProperty.getPropertyValue();
            dbMgr.deleteRow(dbSurveyPageProperty, session);
            deletePropertyValue(dbPropValue, username, session);
            return true;
        } catch (Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Deletes a survey element property from the database.
     * 
     * @param dbSurveyElementProperty the question property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return True if the survey element property was deleted, false otherwise.
     * @throws Throwable
     */
    private static boolean deleteSurveyElementProperty(DbSurveyElementProperty dbSurveyElementProperty, String username, Session session) throws Throwable {
        
        if (dbSurveyElementProperty == null) {
            return false;
        }
        
        //Get a session if none was provided
        boolean commit = false;
        if(session == null) {
            session = dbMgr.getCurrentSession();
            session.beginTransaction();
            commit = true;
        } else if(!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }
        
        try {
            DbPropertyValue dbPropValue = dbSurveyElementProperty.getPropertyValue();
            dbMgr.deleteRow(dbSurveyElementProperty, session);
            deletePropertyValue(dbPropValue, username, session);
            return true;
        } catch (Throwable t) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw t;
        } finally {
            if(commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes a question property from the database.
     * 
     * @param dbQuestionProperty the question property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the question property was deleted, false otherwise.
     * @throws Throwable
     */
    private static boolean deleteQuestionProperty(DbQuestionProperty dbQuestionProperty, String username, Session session, SurveyDeleteConstraintTracker tracker) throws Throwable {

        if (dbQuestionProperty == null) {
            return false;
        }
        
        // Get a session if one was not provided
        boolean commit = false;
        if(session == null) {
            session = dbMgr.getCurrentSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }
        
        try {
            DbQuestionPropertyValue dbQuestionPropertyValue = dbQuestionProperty.getPropertyValue();
            dbMgr.deleteRow(dbQuestionProperty, session);
            deleteQuestionPropertyValue(dbQuestionPropertyValue, username, session, tracker);
            return false;
        } catch (Throwable t) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw t;
        } finally {
            if(commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes a question from the database
     *
     * @param question The newDbQuestion to remove from the database
     * @param username used to check write permissions
     * @return true if the user was allowed to delete the question, false otherwise
     * @throws Exception if there was a problem deleting
     */
    public static boolean deleteQuestion(AbstractQuestion question, String username) throws Exception {
        
        if(isQuestionEditable(question, username)){
    
            DbQuestion dbQuestion = dbMgr.selectRowById(question.getQuestionId(), DbQuestion.class);            
            dbMgr.deleteRow(dbQuestion);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Deletes a category from the database
     *
     * @param categoryName The category to delete from the database
     * @param username used to check write permissions
     * @throws Exception if there was a problem deleting
     */
    public static void deleteCategory(String categoryName, String username) throws Exception {
       
        Session session = null;
        try {

            session = dbMgr.createNewSession();
            session.beginTransaction();
            DbCategory dbCategory = dbMgr.selectRowByExample(new DbCategory(categoryName), DbCategory.class, session);

            if (dbCategory != null && isCategoryEditable(dbCategory, username)) {
                
                //
                // delete any references to this category
                //
                
                // QuestionCategory table
                String query = "from DbQuestionCategory as qc where qc.category.categoryId = " + dbCategory.getCategoryId();
                List<DbQuestionCategory> dbQuestionCategories = dbMgr.selectRowsByQuery(DbQuestionCategory.class, query, -1, -1);
                
                if(dbQuestionCategories != null && !dbQuestionCategories.isEmpty()){
                    for(DbQuestionCategory dbQuestionCategory : dbQuestionCategories){
                        
                        try{
                            dbMgr.deleteRow(dbQuestionCategory, session); 
                        
                        }catch(Exception e){
                            throw new RuntimeException("Failed to delete a DbQuestionCategory : "+dbQuestionCategory, e);
                        }
                    }
                }
                
                dbMgr.deleteRow(dbCategory, session);

                session.getTransaction().commit();
            }

        } catch (Exception e) {
            
            if(session != null){
                session.getTransaction().rollback();
            }
            
            throw e;
        }finally{
            
            if(session != null && session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Deletes the survey from any survey context it is being used in. </br> 
     * NOTE:  This will ALSO delete any survey responses that the survey has, so the user
     * should be presented with a warning dialog and proper permissions being checked before
     * using this function.</br>
     * NOTE: a logger statement will be written if a survey context survey couldn't be deleted
     * because the user didn't have the appropriate permissions
     * 
     * @param surveyId - The id of the survey to be removed from the survey contexts and responses tables.
     * @param userName used for write permission checks
     * @param session - The existing db session (optional).
     * @throws Exception if there was a problem deleting (doesn't include permissions issues to the survey context survey)
     */
    public static void removeSurveyFromSurveyContexts(int surveyId, String userName, Session session) throws Exception{
       
        if(logger.isInfoEnabled()){
            logger.info("removeSurveyFromSurveyContexts() called with survey id: " + surveyId+" for "+userName);
        }

        DbSurvey dbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class);
        
        if (dbSurvey != null) {
            String queryString = "from DbSurveyContextSurvey as scs where scs.survey.surveyId = " + surveyId;
            List<DbSurveyContextSurvey> contextList = dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, 
                    queryString, UNUSED_INDEX, UNUSED_INDEX);
            
            
            StringBuffer sb = new StringBuffer();
            for (DbSurveyContextSurvey context : contextList) {
                
                if (context.getSurvey().getSurveyId() == surveyId){
                    
                    if(isSurveyContextEditable(context.getSurveyContext().getSurveyContextId(), userName)) {
               
                        sb.append("found survey ").append(surveyId).append(" in survey context ").append(context.getSurveyContext().getSurveyContextId()).append(". Removing survey responses followed by the survey context reference that that survey.\n");
                        
                        // Delete the survey responses pertaining to the survey id.
                        Surveys.deleteSurveyResponses(context.getSurveyContext().getSurveyContextId(), surveyId, userName);
                        dbMgr.deleteRow(context);   
                    }else{
                        sb.append("Unable to remove survey ").append(surveyId).append(" from survey context ").append(context.getSurveyContext().getSurveyContextId()).append(" because the user ").append(userName).append(" doesn't have write permissions to that survey context.\n");
                    }
                }
            }
                        
            if(logger.isInfoEnabled() && sb.length() > 0){
                logger.info(sb.toString());
            }
        } else {
            logger.error("deleteSurveyResponsesForSurveyId() - could not find survey with id: " + surveyId);
        }
        
    }
    
    /**
     * Deletes a survey from the database.<br/>
     * This will also delete any survey responses and any references to this
     * survey in existing survey contexts.
     *
     * @param surveyId The id of the survey to delete from the database
     * @param username used for write permission checks. The username will need
     * editable permissions in order to delete.
     * @param session the hibernate session to do the work in. Can't be null.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the survey was successfully deleted, false otherwise
     * @throws DetailedException if there was a problem deleting, including the
     * user doesn't have permissions to delete the survey.
     */
    public static boolean deleteSurvey(int surveyId, String username, Session session, SurveyDeleteConstraintTracker tracker) throws DetailedException {

        DbSurvey dbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class, session);
        if (dbSurvey == null) {
            return false;
        } else if (isSurveyEditable(dbSurvey, username)) {
            try {
                //Delete each of the survey pages
                Iterator<DbSurveyPage> pageIter = dbSurvey.getSurveyPages().iterator();
                while(pageIter.hasNext()) {
                    DbSurveyPage dbPage = pageIter.next();
                    pageIter.remove();
                    deleteSurveyPage(dbPage, username, session, tracker);
                }
                
                //Delete each of the survey properties
                Iterator<DbSurveyProperty> propIter = dbSurvey.getProperties().iterator();
                while(propIter.hasNext()) {
                    DbSurveyProperty dbProp = propIter.next();
                    propIter.remove();
                    deleteSurveyProperty(dbProp, username, session);
                }
                
                //Delete the survey
                dbMgr.deleteRow(dbSurvey, session);
                return true;
            } catch (Throwable t) {
                throw new DetailedException("Failed to delete survey named '" + dbSurvey.getName() + "'.",
                        "The user name of '" + username + "' has permissions to edit the survey with id "
                                + dbSurvey.getSurveyId()
                                + " but an exception was thrown while deleting from the database.",
                        t);
            }
        } else {
            // ERROR
            throw new DetailedException(
                    "Failed to delete survey named '" + dbSurvey.getName() + "'.", "The user name of '" + username
                            + "' doesn't have permission to edit the survey with id " + dbSurvey.getSurveyId() + ".",
                    null);
        }
    }
    
    /**
     * Delete a given survey page from the database
     * 
     * @param surveyPageId the id of the survey page to delete
     * @param username the user who is deleting the survey page, used for
     * authentication
     * @param session an existing database session to use for the deletion,
     * can't be null
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return true if the page was deleted successfully, false otherwise
     */
    private static boolean deleteSurveyPage(int surveyPageId, String username, Session session, SurveyDeleteConstraintTracker tracker) {
        DbSurveyPage dbSurveyPage = dbMgr.selectRowById(surveyPageId, DbSurveyPage.class, session);
        return deleteSurveyPage(dbSurveyPage, username, session, tracker);
    }

    /**
     * Delete a given survey page from the database
     * 
     * @param dbSurveyPage the survey page to delete
     * @param username the user who is deleting the survey page, used for
     * authentication
     * @param session an existing database session to use for the deletion,
     * can't be null
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return true if the page was deleted successfully, false otherwise
     */
    private static boolean deleteSurveyPage(DbSurveyPage dbSurveyPage, String username, Session session, SurveyDeleteConstraintTracker tracker) {
        if (dbSurveyPage == null) {
            return false;
        } else if (isSurveyEditable(dbSurveyPage.getSurvey().getSurveyId(), username, session)) {
            try {
                Iterator<DbSurveyElement> elementIter = dbSurveyPage.getSurveyElements().iterator();
                while (elementIter.hasNext()) {
                    DbSurveyElement element = elementIter.next();
                    elementIter.remove();
                    deleteSurveyElement(element, username, session, tracker);
                }
                
                Iterator<DbSurveyPageProperty> propsIter = dbSurveyPage.getProperties().iterator();
                while (propsIter.hasNext()) {
                    DbSurveyPageProperty dbProperty = propsIter.next();
                    propsIter.remove();
                    deleteSurveyPageProperty(dbProperty, username, session);
                }

                // Delete the survey page and all the contained questions
                dbMgr.deleteRow(dbSurveyPage, session);

                return true;
            } catch (Throwable t) {
                throw new DetailedException("Failed to delete survey page with name '" + dbSurveyPage.getName() + "'.",
                        "The user name of '" + username + "' has permissions to edit the parent survey with id "
                                + dbSurveyPage.getSurvey().getSurveyId()
                                + " but an exception was thrown while deleting from the database.",
                        t);
            }
        } else {
            throw new DetailedException("Failed to delete survey page named '" + dbSurveyPage.getName() + "'.",
                    "The user name of " + username + " doesn't have permission to edit the parent survey with id "
                            + dbSurveyPage.getSurvey().getSurveyId() + ".",
                    null);
        }
    }
    
    /**
     * Deletes a survey context survey from the database. This is functionally the same as removing the survey context survey from its survey context,
     * but deleting the survey context survey directly avoids unnecessarily fetching the entire survey context from the database first, saving both time 
     * and memory when the survey context itself is otherwise unneeded.
     * 
     * @param giftKey The GIFT key identifying the survey context survey within its survey context
     * @param surveyContextId The id of the survey to delete from the database
     * @param username used for write permission checks
     * @return True if the survey context survey was deleted successfully, false otherwise.
     * @throws DetailedException if there was a problem deleting
     */
    public static boolean deleteSurveyContextSurvey(String giftKey, int surveyContextId, String username) throws DetailedException { 
        
        if(!doesSurveyContextExist(surveyContextId)){
            return true;
        }else{
            return SurveyContextUtil.deleteSurveyContextSurvey(giftKey, surveyContextId, username, dbMgr);
        }
    }
    
    /**
     * Return whether the survey in the survey context is editable to the user.
     * 
     * @param surveyContextId the id of the survey context containing the reference to the survey
     * @param survey the survey to check within the survey context
     * @param username used for write permissions checks
     * @return true if the survey context and survey is editable to the user
     */
    public static boolean isSurveyEditable(int surveyContextId, Survey survey, String username) {
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey "+survey.getId()+" in survey context "+surveyContextId+" for "+username);
            }
            return true;
        }  
        
        boolean isEditable = false;
        
        if (isSurveyContextEditable(surveyContextId, username) && isSurveyEditable(survey.getId(), username)) {
            isEditable = true;
        }
        
        return isEditable;
    }
    
    /**
     * Return whether the survey in the survey context is visible to the user.
     * 
     * @param surveyContextId the id of the survey context containing the reference to the survey
     * @param survey the survey to check within the survey context
     * @param username used for read permissions checks
     * @return true if the survey context and survey is visible to the user
     */
    public static boolean isSurveyVisible(int surveyContextId, Survey survey, String username){
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing viewing for survey "+survey.getId()+" in survey context "+surveyContextId+" for "+username);
            }
            return true;
        } 
        
        boolean visible = false;
        
        if(isSurveyVisible(survey, username)){
            visible = true;
        }
        
        return visible;
    }
    
    /**
     * Checks the user permissions for the survey context.  
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param surveyContextId - The id of the survey context to check the permissions for.
     * @param userName The user who for whom to test whether the context is editable.
     * @return True if the survey context are editable based on user permissions.  
     * False if the user does not have permissions for the survey context.
     * False if the survey context has no listed permissions.
     */
    public static boolean isSurveyContextEditable(int surveyContextId, String userName) {
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey context "+surveyContextId+" for "+userName);
            }
            return true;
        }         
        
        if(!doesSurveyContextExist(surveyContextId)){
            return false;
        }
        
        try{
            String selectSurveyContextEditableQuery = "SELECT sceditable.USERNAME FROM "+dbMgr.getSchemaFromConfig()+".SURVEYCONTEXTEDITABLETOUSERNAMES  as sceditable WHERE sceditable.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId;
            List<Object> editableToUserNames = dbMgr.executeSelectSQLQuery(selectSurveyContextEditableQuery);
            if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
                return true;
            }
        }catch(Exception e){
            logger.error("Failed to retrieve the survey context editable usernames for survey context "+surveyContextId, e);
        }
        
        return false;
    }
    
    /**
     * Return whether the question is visible to the user.
     * Note: this should remain private as it deals with DbQuestion.
     * 
     * @param dbQuestion contains the user permission information
     * @param userName the user to check view permissions
     * @return false if the question is not viewable to this user
     */
    private static boolean isQuestionVisible(DbQuestion dbQuestion, String userName){
        
        if(dbQuestion == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing viewing for question "+dbQuestion.getQuestionId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> viewableToUserNames = dbQuestion.getVisibleToUserNames();
        if(viewableToUserNames != null && (viewableToUserNames.contains(userName) || viewableToUserNames.contains(Constants.VISIBILITY_WILDCARD))) {
            return true;
        }
        
        return false;        
    }
    
    /**
     * Return whether the question is editable to the user.
     * Note: this should remain private as it deals with DbQuestion.
     * 
     * @param dbQuestion contains the user permission information
     * @param userName the user to check write permissions
     * @return false if the question is not editable to this user
     */
    private static boolean isQuestionEditable(DbQuestion dbQuestion, String userName){
        
        if(dbQuestion == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for question "+dbQuestion.getQuestionId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> editableToUserNames = dbQuestion.getEditableToUserNames();
        if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
            return true;
        }
        
        return false;        
    }
    
    /**
     * Return whether the question is editable to the user.
     * 
     * @param question contains the question id to look up user permission information
     * @param userName the user to check write permissions
     * @return false if the question is not editable to this user
     */
    public static boolean isQuestionEditable(AbstractQuestion question, String userName){
        
        if(question == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for question "+question.getQuestionId()+" for "+userName);
            }
            return true;
        } 
        
        DbQuestion dbQuestion = getDbQuestion(question.getQuestionId(), null);
        return isQuestionEditable(dbQuestion, userName);       
    }
    
    /**
     * Return whether the category is editable to the user.
     * Note: this should remain private as it deals with DbCategory.
     * 
     * @param dbCategory contains the user permission information
     * @param userName the user to check write permissions
     * @return false if the question is not editable to this user
     */
    private static boolean isCategoryEditable(DbCategory dbCategory, String userName){
        
        if(dbCategory == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for category "+dbCategory.getCategoryId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> editableToUserNames = dbCategory.getEditableToUserNames();
        if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
            return true;
        }
        
        return false;  
    }
    
    /**
     * Return whether the folder is editable to the user.
     * Note: this should remain private as it deals with DbFolder.
     * 
     * @param dbFolder contains the user permission information
     * @param userName the user to check write permissions
     * @return false if the question is not editable to this user
     */
    private static boolean isFolderEditable(DbFolder dbFolder, String userName){
        
        if(dbFolder == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for folder "+dbFolder.getFolderId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> editableToUserNames = dbFolder.getEditableToUserNames();
        if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
            return true;
        }
        
        return false;  
    }
    
    /**
     * Return whether the option list is viewable to the user.
     * Note: this should remain private as it deals with DbOptionList.
     * 
     * @param dbOptionList contains the user permission information
     * @param userName the user to check view permissions
     * @return false if the question is not viewable to this user
     */
    private static boolean isOptionListViewable(DbOptionList dbOptionList, String userName){
        
        if(dbOptionList == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing viewing for option list "+dbOptionList.getOptionListId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> viewableToUserNames = dbOptionList.getVisibleToUserNames();
        if(viewableToUserNames != null && (viewableToUserNames.contains(userName) || viewableToUserNames.contains(Constants.VISIBILITY_WILDCARD))) {
            return true;
        }
        
        return false;  
    }
    
    /**
     * Return whether the option list is editable to the user.
     * Note: this should remain private as it deals with DbOptionList.
     * 
     * @param dbOptionList contains the user permission information
     * @param userName the user to check write permissions
     * @return false if the question is not editable to this user
     */
    private static boolean isOptionListEditable(DbOptionList dbOptionList, String userName){
        
        if(dbOptionList == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for option list "+dbOptionList.getOptionListId()+" for "+userName);
            }
            return true;
        }
        
        Set<String> editableToUserNames = dbOptionList.getEditableToUserNames();
        if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
            return true;
        }
        
        return false;  
    }
    
    /**
     * Return whether the survey is editable to the user.
     * 
     * @param optionList contains the option list id to look up user permission information
     * @param userName the user to check write permissions
     * @return false if the survey is not editable to this user
     */
    public static boolean isOptionListEditable(OptionList optionList, String userName){
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for option list "+optionList.getId()+" for "+userName);
            }
            return true;
        } 
        
        DbOptionList dbOptionList = dbMgr.selectRowById(optionList.getId(), DbOptionList.class);        
        return isOptionListEditable(dbOptionList, userName);
    }
    
    /**
     * Return whether the survey is editable to the user.
     * 
     * @param surveyId the survey id to look up user permission information
     * @param userName the user to check write permissions
     * @return false if the survey is not editable to this user
     */
    public static boolean isSurveyEditable(int surveyId, String userName) {

        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey "+surveyId+" for "+userName);
            }
            return true;
        } 
        
        DbSurvey dbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class);        
        return isSurveyEditable(dbSurvey, userName);
    
    }
    
    /**
     * Return whether the survey is editable to the user.
     * 
     * @param surveyId the survey id to look up user permission information
     * @param userName the user to check write permissions
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return false if the survey is not editable to this user
     */
    private static boolean isSurveyEditable(int surveyId, String userName, Session session){
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey "+surveyId+" for "+userName);
            }
            return true;
        } 
        
        DbSurvey dbSurvey = dbMgr.selectRowById(surveyId, DbSurvey.class, session);        
        return isSurveyEditable(dbSurvey, userName);
    }
    
    /**
     * Return whether the survey is visible to the user.
     * 
     * @param survey contains the survey id to look up user permission information
     * @param userName the user to check read permissions
     * @return false if the survey is not visible to this user
     */
    public static boolean isSurveyVisible(Survey survey, String userName){
        
        if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing visibility for survey "+survey.getId()+" for "+userName);
            }
            return true;
        } 
        
        DbSurvey dbSurvey = dbMgr.selectRowById(survey.getId(), DbSurvey.class);        
        return isSurveyVisible(dbSurvey, userName);
    }
    
    /**
     * Checks the user permissions for the survey only.
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param dbSurvey The survey to check the permissions on.
     * @param userName the user to check permissions for       
     * @return True if the survey is editable based on user permissions.  If the user does not have permissions for the
     *         survey, then false is returned.
     */
    private static boolean isSurveyEditable(DbSurvey dbSurvey, String userName) {
        
        if(dbSurvey == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey "+dbSurvey.getSurveyId()+" for "+userName);
            }
            return true;
        }

        Set<String> editableToUserNames = dbSurvey.getEditableToUserNames();
        if(editableToUserNames != null && (editableToUserNames.contains(userName) || editableToUserNames.contains(Constants.EDITABLE_WILDCARD))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks the user permissions for the survey only.
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param dbSurvey The survey to check the permissions for.
     * @param userName used for write permission check
     *        
     * @return True if the survey is visible based on user permissions.  If the user does not have permissions for the
     *         survey, then false is returned.
     */
    private static boolean isSurveyVisible(DbSurvey dbSurvey, String userName) {
        
        if(dbSurvey == null){
            return false;
        }else if(CommonProperties.getInstance().getByPassSurveyPermissionCheck()){
            if (logger.isInfoEnabled()) {
                logger.info("Bypass check is true and deployment mode is Desktop, allowing visibility for survey "+dbSurvey.getSurveyId()+" for "+userName);
            }
            return true;
        }

        Set<String> visibleToUserNames = dbSurvey.getVisibleToUserNames();
        if(visibleToUserNames == null || visibleToUserNames.contains(userName) || visibleToUserNames.contains(Constants.VISIBILITY_WILDCARD)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Deletes a survey context from the database.<br/>
     * Note: this method will throw an exception if the survey context has any survey responses.  Use {@link #deleteSurveyContextAndResponses(int, String, boolean)}
     * to delete a survey context that has survey responses.
     *
     * @param surveyContextId The id of the survey context to delete from the database
     * @param userName used for write permission check
     * @throws Exception if there was a problem deleting the survey context and any surveys associated with the context.  This includes
     * if there are survey responses found.  Also if the survey context is not editable to the specified user.
     */
    private static void deleteSurveyContextWithNoResponses(int surveyContextId, String userName) throws Exception {

        if(!doesSurveyContextExist(surveyContextId)){
            return;
        }else if(isSurveyContextEditable(surveyContextId, userName)){
            
            //if the survey context has an associated question bank survey, delete it as well
            //Note: this must be done before attempting to delete a lazy loaded DbSurveyContext otherwise you will get an 
            //exception mentioning cascade will save the survey context
            try{
                deleteConceptBankSurvey(surveyContextId, userName);
            }catch(Exception e){
                throw new Exception("Failed to delete the question bank survey from the survey context "+surveyContextId+".  This can happen here if there are still survey responses or another user may be "
                                + "using these resources for an ongoing operation.", e);
            }
            
            //if the survey context has any generated concept surveys (i.e. knowledge assessment), delete them as well
            //Note: this must be done before attempting to delete a lazy loaded DbSurveyContext otherwise you will get an 
            //exception mentioning cascade will save the survey context
            try{
                deleteConceptSurveys(surveyContextId, userName);
            }catch(Exception e){
                throw new Exception("Failed to delete the question bank generated surveys from the survey context "+surveyContextId+".  This can happen here if there are still survey responses or another user may be "
                                + "using these resources for an ongoing operation.", e);
            }
            
            //
            // Now delete surveys that are NOT shared among other survey context
            // Note: currently this call will not return generated question bank surveys.
            //
            Set<DbSurveyContextSurvey> surveysInContext = getDbSurveyContextSurveys(surveyContextId, null);
            
            if(surveysInContext != null){
                Session session = dbMgr.createNewSession();
                session.beginTransaction();
                try{
                    // keep track of survey items that are referenced across questions/surveys in order
                    // to make sure those items can actually be deleted w/o causing a foreign key constraint
                    SurveyDeleteConstraintTracker tracker = new SurveyDeleteConstraintTracker();
                    
                    for(DbSurveyContextSurvey dbContextSurvey : surveysInContext){
                        
                        DbSurvey dbSurvey = dbContextSurvey.getSurvey();
                        
                        //check to see if this survey is used outside of this survey context
                        if(!Surveys.isSurveyInAnotherSurveyContext(surveyContextId, dbSurvey.getSurveyId())){
                            //delete the survey because its not being used in another survey context
                            try{
                                deleteSurveyAndQuestions(dbSurvey.getSurveyId(), userName, session, tracker);
                            }catch(Exception e){
                                throw new Exception("Failed to delete the survey "+dbSurvey.getSurveyId()+" from the survey database.  This can happen here if there are still survey responses or another user may be "
                                        + "using these resources for an ongoing operation.", e);
                            }
                        }else{
                            try{
                                //remove the survey reference from just the survey context since we can't delete the survey
                                //Note: show shared surveys this must be done prior to delete the survey context otherwise a cascade save exception will be thrown.
                                dbMgr.deleteRow(dbContextSurvey);
                            }catch(Exception e){
                                throw new Exception("Failed to delete the survey context survey row for the shared survey "+dbSurvey.getSurveyId()+" from the survey database.  This can happen here if there are still survey responses or another user may be "
                                        + "using these resources for an ongoing operation.", e);
                            }
                        }
        
                    }
                    
                    // now deal with the constraints ---
                    // now that the surveys have been removed, see if any of the collected items 
                    // (e.g. option lists) can be removed.  The idea being if there are no more references
                    // remaining after deleting all the survey items this thread is deleting than the
                    // collected item (e.g. option list) is safe to remove.  Hibernate apparently can't handle our complicated
                    // tables, even in a single transaction.
                    for(Integer dbOptionListId : tracker.optionListIdToOtherQuestionIds.keySet()){
                        
                        Set<Integer> questionIds = tracker.optionListIdToOtherQuestionIds.get(dbOptionListId);
                        if(questionIds == null || questionIds.isEmpty()){
                            //no more questions still reference this option list, safe to delete the option list
                            DbOptionList dbOptionList = getDbOptionList(dbOptionListId, session);
                            deleteOptionList(dbOptionList, userName, session, null);
                        }
                    }
                    session.getTransaction().commit();
                    
                }finally{
                    if(session != null && session.isOpen()){
                        session.close();
                    }
                }
            }
            
            //
            // Delete permissions tables entries for the survey context
            // NOTE: this must be done after deleting the generated question bank surveys as that logic requires
            //       that the user have write permissions on the survey context.
            //
            
            try{
                //delete survey context permissions table entries
                String query = "DELETE FROM "+dbMgr.getSchemaFromConfig()+".SurveyContextEditableToUserNames as editable WHERE editable.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId;
                dbMgr.deleteRowsBySQLQuery(query);
            }catch(Exception e){
                throw new Exception("Failed to delete the survey context references in the Editable table from the survey database", e);
            }
            
            try{
                //delete survey context permissions table entries
                String query = "DELETE FROM "+dbMgr.getSchemaFromConfig()+".SurveyContextVisibleToUserNames as visible WHERE visible.DBSURVEYCONTEXT_SURVEYCONTEXTID_PK = "+surveyContextId;
                dbMgr.deleteRowsBySQLQuery(query);
            }catch(Exception e){
                throw new Exception("Failed to delete the survey context references in the Visible table from the survey database", e);
            }
                        
            try{
                DbSurveyContext dbSurveyContext = dbMgr.selectRowById(surveyContextId, DbSurveyContext.class);
                if(dbSurveyContext != null){
                    dbMgr.deleteRow(dbSurveyContext);
                }
            }catch(Exception e){
                throw new Exception("Failed to delete the survey context "+surveyContextId+" from the survey database.  This can happen here if there are still survey responses for the survey context.", e);
            }            
   
        }else{
            throw new Exception("Unable to delete the survey context "+surveyContextId+" from the survey database because "+userName+" doesn't have write access to the survey context.");
        }


    }
    
    /**
     * This method is used to delete the survey created for a survey context that has all the concept mapped
     * questions.  That survey is used as a question bank for the survey context when a concept based knowledge
     * assessment survey is needed (i.e. dynamically created) during runtime.
     * 
     * @param surveyContextId the id of the survey context associated with the concept survey(s) to delete from the survey database.
     * @param userName used for write permission check
     * @throws Exception if there was a problem deleting
     */
    private static void deleteConceptBankSurvey(int surveyContextId, String userName){
        
        //perform a query that requests all of the surveys in this survey context that are NOT dynamically generated
        String queryString = "from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId 
                + " and scs.giftKey = '" + Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + "'";
        
        Session session = dbMgr.createNewSession();
        try{
            session.beginTransaction();
            
            Set<DbSurveyContextSurvey> contextSurveys = new HashSet<DbSurveyContextSurvey>(
                    dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
            
            SurveyDeleteConstraintTracker tracker = new SurveyDeleteConstraintTracker();
           
            //if the survey context has an associated question bank survey, delete it as well
            for(DbSurveyContextSurvey surveyContextSurvey : contextSurveys){
                    
                if(logger.isInfoEnabled()){
                    logger.info("Found the concept bank survey ("+surveyContextSurvey.getSurvey().getSurveyId()+") that was created for this survey context ("+surveyContextSurvey.getSurveyContext().getSurveyContextId()+").");
                }
                deleteSurvey(surveyContextSurvey.getSurvey().getSurveyId(), userName, session, tracker);
            }
            
            session.getTransaction().commit();

        }catch(Throwable t){
            
            throw new DetailedException("Failed to delete the course question bank",
                    "Caught exception while trying to delete the course question bank survey in the survey context "+ surveyContextId+" for "+userName, t);

        }finally{
            
            if(session != null && session.isOpen()){
                session.close();
            }
        }
        
    }
    
    /**
     * This method is used to delete surveys created for a survey context that contain concept associated questions.
     * These surveys were dynamically generated and given to a learner during course execution.
     * 
     * This is useful when cleaning/deleting survey responses to these generated surveys.
     * 
     * @param surveyContextId the id of the survey context associated with the concept survey(s) to delete from the survey database.
     * @param userName used for write permission check.  The user must have write permission to the survey context in order to delete
     * the generated question bank surveys.
     * @throws DetailedException if there was a problem deleting, including the user doesn't have permission to edit the survey context.
     */
    public static void deleteConceptSurveys(int surveyContextId, String userName) throws DetailedException{
        
        if(!doesSurveyContextExist(surveyContextId)){
            return;
        }
        
        if(!isSurveyContextEditable(surveyContextId, userName)){
            throw new DetailedException("Failed to delete the question bank surveys that were generated for learners",
                    "The user "+userName+" doesn't have permissions to edit the survey context.", null);
        }
        
        //perform a query that requests all of the surveys in this survey context that are NOT dynamically generated
        String queryString = "from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId 
                + " and scs.giftKey like '" + Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : %'";
        
        Session session = dbMgr.createNewSession();
        try{
            
            session.beginTransaction();

            Set<DbSurveyContextSurvey> contextSurveys = new HashSet<DbSurveyContextSurvey>(
                    dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
                   
            //if the survey context has an associated question bank survey, delete it as well
            for(DbSurveyContextSurvey surveyContextSurvey : contextSurveys){
                    
                if(logger.isInfoEnabled()){
                    logger.info("Found a concept survey ("+surveyContextSurvey.getSurvey().getSurveyId()+") that was generated for a learner for this survey context ("+surveyContextSurvey.getSurveyContext().getSurveyContextId()+").");
                }
                
                //
                // This requires custom delete logic (can't use deleteSurvey method) because generated surveys are given no
                // user permissions in the database.  Deleting these generated survey should inherit the survey context write
                // permissions
                //
                DbSurvey dbSurvey = surveyContextSurvey.getSurvey();
                try{
                    dbMgr.deleteRow(dbSurvey, session);
                }catch(Exception e){
                    throw new DetailedException("Failed to delete survey named '"+dbSurvey.getName()+"'.",
                            "The user name of '"+userName+"' has permissions to edit the survey context with id "+surveyContextId+" but an exception was thrown while deleting from the database.", e);
                }
                
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("Successfull delete the generated question bank surveys for survey context "+surveyContextId);
            }
            
            session.getTransaction().commit();

        }catch(Throwable t){
            
            throw new DetailedException("Failed to delete the question bank surveys that were generated for learners",
                    "Caught exception while trying to delete all generated question bank surveys in the survey context "+ surveyContextId+" for "+userName, t);

        }finally{
            
            if(session != null && session.isOpen()){
                session.close();
            }
        }
        
    }
    
    /**
     * Deletes a folder from the database
     *
     * @param folder The folder to delete from the database
     * @param userName used for write permission check
     * @return true if the folder was deleted
     * @throws Exception if there was a problem deleting
     */
    public static boolean deleteFolder(String folder, String userName) throws Exception {

        DbFolder dbFolder = dbMgr.selectRowByExample(new DbFolder(folder), DbFolder.class);

        if (dbFolder != null && isFolderEditable(dbFolder, userName)) {
            dbMgr.deleteRow(dbFolder);
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Deletes a property value from the database.
     * 
     * @param dbPropertyValue the property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return True if the property value was deleted, false otherwise.
     * @throws Exception
     */
    private static boolean deletePropertyValue(DbPropertyValue dbPropertyValue, String username, Session session) throws Exception {
        if(dbPropertyValue == null) {
            return false;
        }
        
        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }
        
        try {
            dbMgr.deleteRow(dbPropertyValue, session);
            return true;
        } finally {
            if(commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes a question property value from the database.
     * 
     * @param dbQuestionPropertyValue the question property value to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the question property value was deleted, false otherwise.
     * @throws Exception
     */
    private static boolean deleteQuestionPropertyValue(DbQuestionPropertyValue dbQuestionPropertyValue, String username, Session session, SurveyDeleteConstraintTracker tracker) throws Exception {
        if (dbQuestionPropertyValue == null) {
            return false;
        }

        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            DbOptionList dbOptionList = dbQuestionPropertyValue.getOptionListValue();
            dbMgr.deleteRow(dbQuestionPropertyValue, session);            
            deleteOptionList(dbOptionList, username, session, tracker);
            return true;
        } catch (Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }
        }
    }
    
    /**
     * Deletes a option list from the database
     *
     * @param optionListId The ID of the option list to delete from the database
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.   Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the option list was deleted, false otherwise.
     * @throws Exception if there was a problem deleting the option list
     */
    public static boolean deleteOptionList(int optionListId, String username, Session session, SurveyDeleteConstraintTracker tracker) throws Exception {
        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            DbOptionList dbOptionList = dbMgr.selectRowById(optionListId, DbOptionList.class);
            return deleteOptionList(dbOptionList, username, session, tracker);
        } catch (Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes an option list from the database
     *
     * @param dbOptionList The option list to delete from the database.
     * @param userName used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @param tracker used to keep track of survey objects that are being removed but are referenced by
     * more than one other survey item.  E.g. an option list that is used by two questions.  Can be null if
     * the foreign key constraint exception should be thrown by this method.
     * @return True if the option list was deleted, false otherwise.
     * @throws Exception if there was a problem deleting the option list
     */
    private static boolean deleteOptionList(DbOptionList dbOptionList, String userName, Session session, SurveyDeleteConstraintTracker tracker) throws Exception {

        // Ensure that a delete can be performed on the option list
        if (dbOptionList == null || dbOptionList.getIsShared() || !isOptionListEditable(dbOptionList, userName)) {
            return false;
        }

        if(tracker != null){
            // determine whether this option list is referenced by another question, making it a candidate
            // for waiting to be deleted until all references are removed to prevent foreign key constraint exception
            
            if(tracker.optionListIdToOtherQuestionIds.containsKey(dbOptionList.getOptionListId())){
                // the tracker already has this option list, meaning it was referenced by more than
                // one question at some point during this delete thread logic.
                return false;
            }else{
                List<Integer> questionIds = isOptionListInAnotherQuestion(dbOptionList.getOptionListId());
                if(questionIds.size() > 1){
                    // option list is in more than 1 question, can't delete the option list w/o causing a foreign key constraint exception
                    tracker.optionListIdToOtherQuestionIds.put(dbOptionList.getOptionListId(), new HashSet<Integer>(questionIds));
                    return false;
                }
                
            }
        }

        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            // Collect the OptionList's ListOptions for later deletion
            Collection<DbListOption> listOptionsToDel = new ArrayList<DbListOption>(dbOptionList.getListOptions());

            // Delete the DbOptionList and the DbListOptions
            dbMgr.deleteRow(dbOptionList, session);
            for (DbListOption dbListOption : listOptionsToDel) {
                deleteListOption(dbListOption, userName, session);
            }
            return true;
        } catch (Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes the list option with the given id from the database.
     * 
     * @param listOptionId the id of the list option to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return true if the delete was successful; false otherwise.
     * @throws Exception
     */
    private static boolean deleteListOption(int listOptionId, String username, Session session) throws Exception {
        
        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }
        
        try {
            DbListOption dbListOption = dbMgr.selectRowById(listOptionId, DbListOption.class);
            return deleteListOption(dbListOption, username, session);
        } catch(Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }
            
            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes the list option from the database.
     * 
     * @param dbListOption the list option to delete.
     * @param username used for write permission check.
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return true if the delete was successful; false otherwise.
     * @throws Exception
     */
    private static boolean deleteListOption(DbListOption dbListOption, String username, Session session) throws Exception {

        if (dbListOption == null || !isOptionListEditable(dbListOption.getOptionList(), username)) {
            return false;
        }

        // Get a session if one wasn't provided
        boolean commit = false;
        if (session == null) {
            session = dbMgr.createNewSession();
            session.beginTransaction();
            commit = true;
        } else if (!session.getTransaction().isActive()) {
            throw new IllegalArgumentException(
                    "The session must have an active transaction (i.e. a transaction that has began).");
        }

        try {
            dbMgr.deleteRow(dbListOption, session);
            return true;
        } catch (Exception e) {
            if (commit && session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw e;
        } finally {
            if (commit && session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Return the question property of the specified question and key
     *
     * @param question The question of the property
     * @param propertyKey The property key
     * @return QuestionProperty The question property from the database
     */
    @SuppressWarnings("unused")
    private static DbQuestionProperty getDbQuestionPropertyById(DbQuestion question, DbPropertyKey propertyKey) {

        return dbMgr.selectRowByTwoTupleCompositeId(QUESTION_ID_PROPERTY, question.getQuestionId(), PROPERTY_KEY_ID_PROPERTY, propertyKey.getId(), DbQuestionProperty.class);
    }

    /**
     * Return the question properties of the specified question
     *
     * @param questionId The question id of the properties
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return List<QuestionProperty> The question properties from the database
     */
    protected static List<DbQuestionProperty> getDbQuestionPropertiesById(int questionId, Session session) {

        if (session == null) {
            return dbMgr.selectRowsById(QUESTION_ID_PROPERTY, questionId, DbQuestionProperty.class);            
        } else {
            return dbMgr.selectRowsById(QUESTION_ID_PROPERTY, questionId, DbQuestionProperty.class, session);
        }
    }

    /**
     * Return the survey property of the specified survey and key
     *
     * @param survey The survey of the property
     * @param propertyKey The property key
     * @return SurveyProperty The survey property from the database
     */
    @SuppressWarnings("unused")
    private static DbSurveyProperty getDbSurveyPropertyById(DbSurvey survey, DbPropertyKey propertyKey) {

        return dbMgr.selectRowByTwoTupleCompositeId(SURVEY_ID_PROPERTY, survey.getSurveyId(), PROPERTY_KEY_ID_PROPERTY, propertyKey.getId(), DbSurveyProperty.class);
    }
    
    /**
     * Return the survey properties of the specified survey
     *
     * @param surveyId The survey id of the property
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return List<SurveyProperty> The survey properties from the database
     */
    protected static List<DbSurveyProperty> getDbSurveyPropertiesById(int surveyId, Session session) {

        if (session == null) {
            return dbMgr.selectRowsById(SURVEY_ID_PROPERTY, surveyId, DbSurveyProperty.class);
        } else {
            return dbMgr.selectRowsById(SURVEY_ID_PROPERTY, surveyId, DbSurveyProperty.class, session);
        }
    }

    /**
     * Return the survey page property of the specified survey page and key
     *
     * @param surveyPage The survey page of the property
     * @param propertyKey The property key
     * @return SurveyPageProperty The survey page property from the database
     */
    @SuppressWarnings("unused")
    private static DbSurveyPageProperty getDbSurveyPagePropertyById(DbSurveyPage surveyPage, DbPropertyKey propertyKey) {

        return dbMgr.selectRowByTwoTupleCompositeId(SURVEY_PAGE_ID_PROPERTY, surveyPage.getSurveyPageId(), PROPERTY_KEY_ID_PROPERTY, propertyKey.getId(), DbSurveyPageProperty.class);
    }
    /**
     * Return the survey page properties of the specified survey page
     *
     * @param surveyPage The survey page of the property
     * @param session - the hibernate session for the query (i.e. transaction)
     * @return List<SurveyPageProperty> The survey page properties from the
     * database
     */
    @SuppressWarnings("unused")
    private static List<DbSurveyPageProperty> getDbSurveyPagePropertiesById(DbSurveyPage surveyPage, Session session) {

        return dbMgr.selectRowsById(SURVEY_PAGE_ID_PROPERTY, surveyPage.getSurveyPageId(), DbSurveyPageProperty.class, session);
    }
    
    /**
     * Return the survey page properties of the specified survey page
     *
     * @param surveyPageId The survey page Id of the property
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return List<SurveyPageProperty> The survey page properties from the database
     */
    protected static List<DbSurveyPageProperty> getDbSurveyPagePropertiesById(int surveyPageId, Session session) {

        if (session == null) {
            return dbMgr.selectRowsById(SURVEY_PAGE_ID_PROPERTY, surveyPageId, DbSurveyPageProperty.class);
        } else {
            return dbMgr.selectRowsById(SURVEY_PAGE_ID_PROPERTY, surveyPageId, DbSurveyPageProperty.class, session);
        }
    }

    /**
     * Return the survey question property of the specified survey question and
     * key
     *
     * @param surveyElement The survey question of the property
     * @param propertyKey The property key
     * @param session - the hibernate session for the query (i.e. transaction)
     * @return SurveyQuestionProperty The survey question property from the
     * database
     */
    @SuppressWarnings("unused")
    private static DbSurveyElementProperty getDbSurveyElementPropertyById(DbSurveyElement surveyElement, DbPropertyKey propertyKey, Session session) {

        return dbMgr.selectRowByTwoTupleCompositeId(SURVEY_ELEMENT_ID_PROPERTY, surveyElement.getSurveyElementId(), PROPERTY_KEY_ID_PROPERTY, propertyKey.getId(), DbSurveyElementProperty.class, session);
    }
    
    /**
     * Return the survey question property of the specified survey question and
     * key
     *
     * @param surveyElement The survey question of the property
     * @param propertyKey The property key
     * @return SurveyQuestionProperty The survey question property from the
     * database
     */
    @SuppressWarnings("unused")
    private static DbSurveyElementProperty getDbSurveyElementPropertyById(DbSurveyElement surveyElement, DbPropertyKey propertyKey) {

        return dbMgr.selectRowByTwoTupleCompositeId(SURVEY_ELEMENT_ID_PROPERTY, surveyElement.getSurveyElementId(), PROPERTY_KEY_ID_PROPERTY, propertyKey.getId(), DbSurveyElementProperty.class);
    }

    /**
     * Return the survey question properties of the specified survey question
     *
     * @param surveyElement The survey question of the property
     * @param session - the hibernate session for the query (i.e. transaction)
     * @return List<DbSurveyElementProperty> The survey question properties from
     * the database
     */
    @SuppressWarnings("unused")
    private static List<DbSurveyElementProperty> getDbSurveyElementPropertiesForSurveyElement(DbSurveyElement surveyElement, Session session) {

        return dbMgr.selectRowsById(SURVEY_ELEMENT_ID_PROPERTY, surveyElement.getSurveyElementId(), DbSurveyElementProperty.class, session);
    }
    
    /**
     * Return the survey question properties of the specified survey question
     *
     * @param surveyElementId The survey question id of the property
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return List<DbSurveyElementProperty> The survey question properties from
     * the database
     */
    protected static List<DbSurveyElementProperty> getDbSurveyElementPropertiesByQuestionId(int surveyElementId, Session session) {
        
        if (session == null) {
            return dbMgr.selectRowsById(SURVEY_ELEMENT_ID_PROPERTY, surveyElementId, DbSurveyElementProperty.class);
        } else {
            return dbMgr.selectRowsById(SURVEY_ELEMENT_ID_PROPERTY, surveyElementId, DbSurveyElementProperty.class, session);
        }
    }

    /**
     * Return the survey question properties of the specified survey question
     *
     * @param surveyElement The survey question of the property
     * @return List<DbSurveyElementProperty> The survey question properties from
     * the database
     */
    @SuppressWarnings("unused")
    private static List<DbSurveyElementProperty> getDbSurveyElementPropertiesForSurveyElement(DbSurveyElement surveyElement) {

        return dbMgr.selectRowsById(SURVEY_ELEMENT_ID_PROPERTY, surveyElement.getSurveyElementId(), DbSurveyElementProperty.class);
    }

    /**
     * Return the list options of the specified option list
     *
     * @param optionListId The ID of the option list to get list options for
     * @param session a database session with a transaction (that has already began) to do all the insert operations in.  This is useful
     *               for doing rollback operations if any one of the inserts fails.
     *               Can be null if the caller doesn't care about partial fails and wants this class to manage the session.
     * @return List<ListOption> The list options from the database
     */
    protected static List<DbListOption> getDbListOptionsByOptionListId(int optionListId, Session session) {
        
        if (session == null) {
            return dbMgr.selectRowsById(LIST_OPTION_OPTION_LIST_ID_PROPERTY, optionListId, DbListOption.class);
        } else {
            return dbMgr.selectRowsById(LIST_OPTION_OPTION_LIST_ID_PROPERTY, optionListId, DbListOption.class, session);
        }
    }

    /**
     * Return the survey context surveys of the specified survey context
     *
     * @param surveyContext The survey context to get survey context surveys of
     * @return List<DbSurveyContextSurvey> The survey context surveys of the
     * survey context
     */
    @SuppressWarnings("unused")
    private static List<DbSurveyContextSurvey> getDbSurveyContextSurveyBySurveyContext(DbSurveyContext surveyContext) {

        return dbMgr.selectRowsById(SURVEY_CONTEXT_SURVEY_SURVEY_CONTEXT_ID_PROPERTY, surveyContext.getSurveyContextId(), DbSurveyContextSurvey.class);
    }
    
    /**
     * Gets the print out of dependencies for a question, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param questionId The ID of the question
     * @return String The print out of dependencies for a question
     */
    public static String getQuestionDependencies(int questionId) {
        try {
            List<DbSurveyElement> dbSurveyQuestions = dbMgr.selectRowsById(Surveys.SURVEY_ELEMENT_QUESTION_ID, questionId, DbSurveyElement.class);
            Set<DbSurvey> dbSurveys = new HashSet<DbSurvey>();
            Set<DbSurveyContext> dbSurveyContexts = new HashSet<DbSurveyContext>();
            for (DbSurveyElement dbSurveyQuestion : dbSurveyQuestions) {
                if (dbSurveyQuestion.getQuestionId() == questionId) {
                                        
                    SurveyProperties properties = hibernateToGift.convertSurveyProperties(dbSurveyQuestion.getSurveyPage().getSurvey().getProperties());
                    
                    if(!(properties.hasProperty(SurveyPropertyKeyEnum.UNPRESENTABLE)
                            && properties.getBooleanPropertyValue(SurveyPropertyKeyEnum.UNPRESENTABLE))){  
                        
                        //add surveys presentable through the SAS to the set of surveys
                        dbSurveys.add(dbSurveyQuestion.getSurveyPage().getSurvey());
                        
                    }else if(dbSurveyQuestion.getSurveyPage().getSurvey().getSurveyContextSurveys() != null
                            && !dbSurveyQuestion.getSurveyPage().getSurvey().getSurveyContextSurveys().isEmpty()){
                        
                        //add survey contexts containing unpresentable surveys to the set of survey contexts
                        for(DbSurveyContextSurvey surveyContextSurvey : dbSurveyQuestion.getSurveyPage().getSurvey().getSurveyContextSurveys()){                                                
                            dbSurveyContexts.add(surveyContextSurvey.getSurveyContext());                   
                        }
                    }
                }
            }          
            
            final String queryString = "from DbQuestionResponse as qr where qr." + QUESTION_RESPONSE_QUESTION_ID + " = " + questionId; 
    
            List<DbQuestionResponse> dbResponses = dbMgr.selectRowsByQuery(DbQuestionResponse.class, queryString, -1, -1);
            int responseCount = 0;
            for (DbQuestionResponse dbResponse : dbResponses) {
                if (dbResponse.getSurveyQuestion().getQuestionId() == questionId) {
                    responseCount += 1;
                }
            }
            if (!dbSurveys.isEmpty() || !dbSurveyContexts.isEmpty() || responseCount > 0) {
                StringBuilder dependenciesString = new StringBuilder();
                if (!dbSurveys.isEmpty()) {
                    dependenciesString.append(dbSurveys.size());
                    if (dbSurveys.size() > 1) {
                        dependenciesString.append(" Surveys reference");
                    } else {
                        dependenciesString.append(" Survey references");
                    }
                    dependenciesString.append(" this question:");
                    dependenciesString.append("<ul>");
                    for (DbSurvey dbSurvey : dbSurveys) {
                        dependenciesString.append("<li>").append(dbSurvey.getName()).append("</li>");
                    }
                    dependenciesString.append("</ul>");
                    dependenciesString.append("Editing this question will affect future presentations of ");
                    if (dbSurveys.size() > 1) {
                        dependenciesString.append("those surveys!");
                    } else {
                        dependenciesString.append("that survey!");
                    }
                    dependenciesString.append("<br/>");
                }
                if(!dbSurveyContexts.isEmpty()){
                    
                    dependenciesString.append(dbSurveyContexts.size());
                    if(dbSurveyContexts.size() > 1){
                        dependenciesString.append(" Survey Contexts reference");
                    } else {
                        dependenciesString.append(" Survey Context references");
                    }
                    dependenciesString.append(" this question:");
                    dependenciesString.append("<ul>");
                    for(DbSurveyContext surveyContext : dbSurveyContexts){
                        dependenciesString.append("<li>").append(surveyContext.getName()).append("</li>");
                    }
                    dependenciesString.append("</ul>");
                    dependenciesString.append("Editing this question will affect future presentations of ");
                    if (dbSurveyContexts.size() > 1) {
                        dependenciesString.append("those survey contexts!");
                    } else {
                        dependenciesString.append("that survey context!");
                    }
                    dependenciesString.append("<br/>");
                }
                if (responseCount > 0) {
                    dependenciesString.append(responseCount);
                    if (responseCount > 1) {
                        dependenciesString.append(" Responses reference");
                    } else {
                        dependenciesString.append(" Response references");
                    }
                    dependenciesString.append(" this question. Editing this question will cause future responses to the question to be different!<br/>");
                }
                return dependenciesString.toString();
            } else {
                return "none";
            }

        } catch (Exception e) {
            logger.error("Caught an exception while getting the dependencies for a question", e);
        }
        return null;
    }
    
    /**
     * Inserts an option list into the database
     *
     * @param userName The user creating the option list
     * @param gwtOptionList The option list to insert into the database
     * @return GwtOptionList The option list in the database
     * @throws Exception if there was a problem inserting
     */
    public static OptionList insertOptionList(String userName, OptionList gwtOptionList) throws Exception {

        Session session = dbMgr.createNewSession();

        session.beginTransaction();

        try {

            DbOptionList dbSurvey = giftToHibernate.convertOptionList(userName, gwtOptionList, session);

            dbMgr.insertRow(dbSurvey, session);

            session.getTransaction().commit();

            session.close();

            return hibernateToGift.convertOptionList(dbSurvey);

        } catch (Exception e) {

            session.getTransaction().rollback();

            session.close();
            
            throw e;
        }

    }

    /**
     * Updates an option list in the database
     *
     * @param userName the user updating the option list
     * @param gwtOptionList The option list to update in the database
     * @return boolean If the update was successful
     */
    public static boolean updateOptionList(String userName, OptionList gwtOptionList) {

        Session session = dbMgr.createNewSession();

        session.beginTransaction();

        try {

            DbOptionList dbOptionList = giftToHibernate.convertOptionList(userName, gwtOptionList, session);

            if (dbMgr.updateRow(dbOptionList, session)) {

                session.getTransaction().commit();

                session.close();

                return true;

            } else {

                session.getTransaction().rollback();

                logger.error("Could not update option list, the option list could not be updated: " + dbOptionList);

                session.close();

                return false;
            }

        } catch (Exception e) {

            session.getTransaction().rollback();

            logger.error("Caught an exception while updating an option list", e);

            session.close();
        }

        return false;
    }

    /**
     * Return the last modification date of the survey context provided.
     * 
     * @param surveyContextId the survey context to retrieve the last modification date for
     * @param username used for authentication, i.e. read permissions
     * @return the last modification date of the survey context.  Can be null if the date hasn't been set or
     * there was a problem retrieving the value from the survey context table.
     */
    public static Date getSurveyContextLastModification(int surveyContextId, String username){

        Date lastModified = null;
        try{
            DbSurveyContext dbSurveyContext = dbMgr.selectRowById(surveyContextId, DbSurveyContext.class);
            lastModified = dbSurveyContext.getLastModified();
        }catch(@SuppressWarnings("unused") Exception e){}
        
        return lastModified;
    }

}
