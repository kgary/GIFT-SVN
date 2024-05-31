/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

import java.io.Serializable;
import java.util.ArrayList;
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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.db.AbstractHibernateDatabaseManager;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.ums.db.HibernateObjectConverter.SurveyItemPropertyValueJSONUtil;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionPermission;
import mil.arl.gift.ums.db.table.DbFolder;
import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionResponse;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyElementProperty;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyPageProperty;
import mil.arl.gift.ums.db.table.DbSurveyPageResponse;
import mil.arl.gift.ums.db.table.DbSurveyProperty;
import mil.arl.gift.ums.db.table.DbSurveyResponse;

/**
 * Converts Hibernate Survey Objects to Common Survey Objects
 *
 * @author jleonard
 */
public class HibernateObjectReverter {

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(HibernateObjectReverter.class);

    private final AbstractHibernateDatabaseManager dbMgr;
    
	/** Codec that will be used to encode/decode the property values */
    private final SurveyItemPropertyValueJSONUtil codec = new SurveyItemPropertyValueJSONUtil();
    
    /** Query to find the total number of subjects within the given experiment id */
    private final static String COUNT_SUBJECTS = "Select Count(*) From DbExperimentSubject as expSubj Where expSubj.experimentSubjectId.experiment.id=";
    
    /** Query to find the most recent experiment start time within the given experiment id */
    private final static String MAX_DATE_SUBJECTS = "Select Max(expSubj.startTime) From DbExperimentSubject as expSubj Where expSubj.experimentSubjectId.experiment.id=";
    
    /** Query to find the total number of LTI results within the given experiment id (data set id) */
    private final static String COUNT_LTI_RESULTS = "Select Count(*) From DbDataCollectionResultsLti as ltiCollection Where ltiCollection.dataSetId=";
    
    /** Query to find the most recent experiment start time within the given experiment id (data set id) */
    private final static String MAX_DATE_LTI_RESULTS = "Select Max(ltiCollection.startTime) From DbDataCollectionResultsLti as ltiCollection Where ltiCollection.dataSetId=";

    /**
     * Constructor
     *
     * @param dbMgr The Hibernate database manager to use to connect to the UMS
     * database
     */
    public HibernateObjectReverter(AbstractHibernateDatabaseManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    /**
     * Converts a Question class object into another class object
     *
     * @param question The Question object to convert
     * @return QuestionB The converted Question object.  Can be null if the question provided is null.
     */
    public AbstractQuestion convertQuestion(DbQuestion question) {
        
        if(question == null){
            return null;
        }

        QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(question.getQuestionType().getKey());

        List<Category> categories = convertCategories(question.getCategories());
        ArrayList<String> categoryNames = new ArrayList<String>();
        for(Category category : categories) {
        	categoryNames.add(category.getName());
        }
        
        AbstractQuestion marshalQuestion =
                AbstractQuestion.createQuestion(questionType,
                question.getQuestionId(),
                question.getText(),
                convertQuestionProperties(questionType, question.getQuestionProperties()),
                categoryNames,
                question.getVisibleToUserNames(),
                question.getEditableToUserNames());

        return marshalQuestion;
    }

    /**
     * Converts a collection of Question objects from one class to another
     *
     * @param questions The collection of Question objects to convert
     * @return Collection<QuestionB> The collection of converted Question
     * objects
     */
    public Collection<AbstractQuestion> convertQuestions(Collection<DbQuestion> questions) {
        Collection<AbstractQuestion> marshalQuestions = new ArrayList<>();
        for (DbQuestion question : questions) {
            marshalQuestions.add(convertQuestion(question));
        }
        return marshalQuestions;
    }

    /**
     * Converts a List Option class object into another class object
     *
     * @param dbListOption The List Option object to convert
     * @return ListOptionB The converted List Option object
     */
    public ListOption convertListOption(DbListOption dbListOption) {
        
        ListOption commonListOption =
                new ListOption(dbListOption.getListOptionId(),
                dbListOption.getText(),
                dbListOption.getOptionList().getOptionListId());
        commonListOption.setSortKey(dbListOption.getSortKey());
        return commonListOption;
    }

    /**
     * Converts a collection of List Option objects from one class to another
     *
     * @param dbListOptions The collection of List Option objects to convert
     * @return Collection<ListOptionB> The collection of converted List Option
     * objects
     */
    public List<ListOption> convertListOptions(List<DbListOption> dbListOptions) {

        List<DbListOption> sortedDbListOptions = new ArrayList<>(dbListOptions);

        Collections.sort(sortedDbListOptions, new Comparator<DbListOption>() {
            @Override
            public int compare(DbListOption o1, DbListOption o2) {

                return o1.getSortKey() - o2.getSortKey();
            }
        });

        List<ListOption> commonListOptions = new ArrayList<>();

        for (DbListOption dbListOption : sortedDbListOptions) {

            commonListOptions.add(convertListOption(dbListOption));
        }

        return commonListOptions;
    }

    /**
     * Converts a Option List class object into another class object
     *
     * @param dbOptionList The Option List object to convert
     * @return OptionListB The converted Option List object
     */
    public OptionList convertOptionList(DbOptionList dbOptionList) {

        OptionList commonOptionList = new OptionList(
                dbOptionList.getOptionListId(),
                dbOptionList.getName(),
                dbOptionList.getIsShared(),
                convertListOptions(new ArrayList<>(dbOptionList.getListOptions())),
                dbOptionList.getVisibleToUserNames(),
                dbOptionList.getEditableToUserNames());

        return commonOptionList;
    }

    /**
     * Converts a collection of Option List objects from one class to another
     *
     * @param dbOptionLists The collection of Option List objects to convert
     * @return Collection<OptionListB> The collection of converted Option List
     * objects
     */
    public Collection<OptionList> convertOptionLists(Collection<DbOptionList> dbOptionLists) {
        Collection<OptionList> marshalOptionLists = new ArrayList<>();
        for (DbOptionList dbOptionList : dbOptionLists) {
            marshalOptionLists.add(convertOptionList(dbOptionList));
        }
        return marshalOptionLists;
    }

    /**
     * Converts a Survey class object into another class object
     *
     * @param survey The Survey object to convert
     * @return SurveyB The converted Survey object
     */
    public Survey convertSurvey(DbSurvey survey) {

        String folder = null;

        if (survey.getFolder() != null) {

            folder = survey.getFolder().getName();
        }

        Survey marshalSurvey = new Survey(
                survey.getSurveyId(),
                survey.getName(),
                new ArrayList<>(convertSurveyPages(survey.getSurveyPages())),
                folder,
                convertSurveyProperties(survey.getProperties()),
                survey.getVisibleToUserNames(),
                survey.getEditableToUserNames());

        return marshalSurvey;
    }

    /**
     * Converts a collection of Survey objects from one class to another
     *
     * @param surveys The collection of Survey objects to convert
     * @return Collection<SurveyB> The collection of converted Survey objects
     */
    public Collection<Survey> convertSurveys(Collection<DbSurvey> surveys) {
        Collection<Survey> marshalSurveys = new ArrayList<>();
        for (DbSurvey survey : surveys) {
            marshalSurveys.add(convertSurvey(survey));
        }
        return marshalSurveys;
    }

    /**
     * Converts a Survey Context class object into another class object
     *
     * @param surveyContext The Survey Context object to convert.  Can't be null.
     * @param removeGeneratedSurveys whether or not to remove any generated concept knowledge surveys from the 
     * survey context being returned.  The caller needs to be aware that removing these surveys could hide dependencies
     * in the returned survey context because the context doesn't contain all of the surveys actually associated with it.
     * However in other cases, like GIFT course exporting, these surveys shouldn't be associated with the original survey
     * context in the GIFT importing the exported survey context.
     * @return SurveyContextB The converted Survey Context object
     */
    public SurveyContext convertSurveyContext(DbSurveyContext surveyContext, boolean removeGeneratedSurveys) {
        
        Set<DbSurveyContextSurvey> scs = surveyContext.getSurveyContextSurveys();
        Set<DbSurveyContextSurvey> scsToRemove = new HashSet<>(0);
        
        if(removeGeneratedSurveys){
            Iterator<DbSurveyContextSurvey> itr = scs.iterator();
            while(itr.hasNext()){
                DbSurveyContextSurvey contextSurvey = itr.next();
                if(contextSurvey.getGiftKey().matches(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX)){
                    scsToRemove.add(contextSurvey);
                }
            }
            
            scs.removeAll(scsToRemove);
        }
        
        SurveyContext marshalSurveyContext = new SurveyContext(
        		surveyContext.getSurveyContextId(),
        		surveyContext.getName(),
                new ArrayList<>(convertSurveyContextSurveys(scs)),
                surveyContext.getVisibleToUserNames(),
                surveyContext.getEditableToUserNames());
        return marshalSurveyContext;
    }

    /**
     * Converts a collection of Survey Context objects from one class to another
     *
     * @param surveyContexts The collection of Survey Context objects to convert
     * @return Collection<SurveyContextB> The collection of converted Survey
     * Context objects
     */
    public Collection<SurveyContext> convertSurveyContexts(Collection<DbSurveyContext> surveyContexts) {
        Collection<SurveyContext> marshalSurveyContexts = new ArrayList<>();
        for (DbSurveyContext surveyContext : surveyContexts) {
            marshalSurveyContexts.add(convertSurveyContext(surveyContext, false));
        }
        return marshalSurveyContexts;
    }

    /**
     * Converts a Survey Context Survey class object into another class object
     *
     * @param surveyContextSurvey The Survey Context Survey object to convert
     * @return SurveyContextSurveyB The converted Survey Context Survey object
     */
    public SurveyContextSurvey convertSurveyContextSurvey(DbSurveyContextSurvey surveyContextSurvey) {
        SurveyContextSurvey marshalSurveyContextSurvey =
                new SurveyContextSurvey(surveyContextSurvey.getSurveyContext().getSurveyContextId(),
                surveyContextSurvey.getGiftKey(), convertSurvey(surveyContextSurvey.getSurvey()));
        return marshalSurveyContextSurvey;
    }

    /**
     * Converts a collection of Survey Context Survey objects from one class to
     * another
     *
     * @param surveyContextSurveys The collection of Survey Context Survey
     * objects to convert
     * @return Collection<SurveyContextSurveyB> The collection of converted
     * Survey Context Survey objects
     */
    public Collection<SurveyContextSurvey> convertSurveyContextSurveys(Collection<DbSurveyContextSurvey> surveyContextSurveys) {
        Collection<SurveyContextSurvey> marshalSurveyContextSurveys = new ArrayList<>();
        for (DbSurveyContextSurvey surveyContextSurvey : surveyContextSurveys) {
            marshalSurveyContextSurveys.add(convertSurveyContextSurvey(surveyContextSurvey));
        }
        return marshalSurveyContextSurveys;
    }

    /**
     * Converts a Survey Page class object into another class object
     *
     * @param surveyPage The Survey Page object to convert
     * @param pageNumber The number of the page in the survey
     * @return SurveyPageB The converted Survey Page object
     */
    public SurveyPage convertSurveyPage(DbSurveyPage surveyPage, int pageNumber) {

        SurveyPage marshalSurveyPage =
                new SurveyPage(surveyPage.getSurveyPageId(),
                surveyPage.getName(),
                surveyPage.getSurvey().getSurveyId(),
                new ArrayList<>(convertSurveyElements(surveyPage.getSurveyElements())),
                convertSurveyPageProperties(surveyPage.getProperties()));

        return marshalSurveyPage;
    }

    /**
     * Converts a collection of Survey Page objects from one class to another
     *
     * @param surveyPages The collection of Survey Page objects to convert
     * @return Collection<SurveyPageB> The collection of converted Survey Page
     * objects
     */
    public Collection<SurveyPage> convertSurveyPages(Collection<DbSurveyPage> surveyPages) {

        List<DbSurveyPage> sortedSurveyPages = new ArrayList<>(surveyPages);

        Collections.sort(sortedSurveyPages, new Comparator<DbSurveyPage>() {
            @Override
            public int compare(DbSurveyPage o1, DbSurveyPage o2) {

                return o1.getPageNumber() - o2.getPageNumber();
            }
        });

        Collection<SurveyPage> marshalSurveyPages = new ArrayList<>();

        int pageNumber = 1;

        for (DbSurveyPage surveyPage : sortedSurveyPages) {

            marshalSurveyPages.add(convertSurveyPage(surveyPage, pageNumber));

            pageNumber += 1;
        }

        return marshalSurveyPages;
    }

    /**
     * Converts a database survey element object into a common survey element
     *
     * @param surveyElement The Survey Element object to convert
     * @return The converted Survey Element object.  Can be null if the survey element is a question type and the question was
     * not found by id in the question table of the survey db.
     */
    public AbstractSurveyElement convertSurveyElement(DbSurveyElement surveyElement) {
        
        SurveyElementTypeEnum surveyElementType = SurveyElementTypeEnum.valueOf(surveyElement.getSurveyElementType().getKey());
        
        AbstractSurveyElement commonSurveyElement = null;
        
        if (surveyElementType == SurveyElementTypeEnum.QUESTION_ELEMENT) {

            AbstractQuestion commonQuestion = convertQuestion(dbMgr.selectRowById(surveyElement.getQuestionId(), DbQuestion.class));

            if(commonQuestion != null){
                commonSurveyElement = AbstractSurveyQuestion.createSurveyQuestion(
                        surveyElement.getSurveyElementId(),
                        surveyElement.getSurveyPage().getSurveyPageId(),
                        commonQuestion,
                        convertSurveyQuestionProperties(surveyElement.getProperties()));
            }
        } else {

            commonSurveyElement = new TextSurveyElement(
                    surveyElement.getSurveyElementId(),
                    surveyElement.getSurveyPage().getSurveyPageId(),
                    convertSurveyQuestionProperties(surveyElement.getProperties()));
        }

        return commonSurveyElement;
    }

    /**
     * Converts a collection of database Survey Elements in to common Survey
     * Elements
     *
     * @param surveyElements The collection of Survey Element objects to convert
     * @return Collection<AbstractSurveyElement> The collection of converted
     * Survey Element objects
     */
    public Collection<AbstractSurveyElement> convertSurveyElements(Collection<DbSurveyElement> surveyElements) {

        List<DbSurveyElement> sortedSurveyElements = new ArrayList<>(surveyElements);

        Collections.sort(sortedSurveyElements, new Comparator<DbSurveyElement>() {
            @Override
            public int compare(DbSurveyElement o1, DbSurveyElement o2) {

                return o1.getElementNumber() - o2.getElementNumber();
            }
        });

        Collection<AbstractSurveyElement> commonSurveyElements = new ArrayList<>();

        for (DbSurveyElement surveyElement : sortedSurveyElements) {

            AbstractSurveyElement abstractSurveyElement = convertSurveyElement(surveyElement);
            if(abstractSurveyElement != null){
                commonSurveyElements.add(abstractSurveyElement);
            }
        }

        return commonSurveyElements;
    }

    /**
     * Converts a Question Response class object into another class object
     *
     * @param commonSurveyQuestion The converted survey question the response is
     * for
     * @param dbQuestionResponse The Question Response object to convert
     * @return QuestionResponseB The converted Response object
     */
    public AbstractQuestionResponse convertQuestionResponse(AbstractSurveyQuestion<? extends AbstractQuestion> commonSurveyQuestion, List<DbQuestionResponse> dbQuestionResponse) {

        List<QuestionResponseElement> responses = new ArrayList<>();

        for (DbQuestionResponse dbResponse : dbQuestionResponse) {

            QuestionResponseElement responseElement = new QuestionResponseElement(dbResponse.getText(),
                    dbResponse.getTextOptionList() != null ? convertOptionList(dbResponse.getTextOptionList()) : null,
                    dbResponse.getRowText(),
                    dbResponse.getRowTextOptionList() != null ? convertOptionList(dbResponse.getRowTextOptionList()) : null,
                    dbResponse.getAnswerTime());
            if(dbResponse.getQuestionResponseId() > 0){
                responseElement.setQuestionResponseId(dbResponse.getQuestionResponseId());
            }

            responses.add(responseElement);
        }

        return AbstractQuestionResponse.createResponse(commonSurveyQuestion, responses);
    }

    /**
     * Converts a map of converted survey questions to their unconverted
     * responses to a list of converted responses
     *
     * @param surveyQuestionToResponseMap Map of converted survey questions to
     * their unconverted responses to convert
     * @return Collection<QuestionResponseB> The collection of converted
     * Question Response objects
     */
    public Collection<AbstractQuestionResponse> convertQuestionResponses(Map<AbstractSurveyQuestion<? extends AbstractQuestion>, List<DbQuestionResponse>> surveyQuestionToResponseMap) {

        Collection<AbstractQuestionResponse> commonQuestionResponses = new ArrayList<>();

        for (AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion : surveyQuestionToResponseMap.keySet()) {

            commonQuestionResponses.add(convertQuestionResponse(surveyQuestion, surveyQuestionToResponseMap.get(surveyQuestion)));
        }

        return commonQuestionResponses;
    }

    /**
     * Converts a Survey Response class object into another class object
     *
     * @param dbSurveyResponse The Survey Response object to convert
     * @return SurveyResponseB The converted Survey Response object
     */
    public SurveyResponse convertSurveyResponse(DbSurveyResponse dbSurveyResponse) {

        Survey commonSurvey = convertSurvey(dbSurveyResponse.getSurvey());

        Map<SurveyPage, DbSurveyPageResponse> surveyPageToResponseMap = new HashMap<>();

        for (SurveyPage commonSurveyPage : commonSurvey.getPages()) {

            for (DbSurveyPageResponse dbSurveyPageResponse : dbSurveyResponse.getSurveyPageResponses()) {

                if (commonSurveyPage.getId() == dbSurveyPageResponse.getSurveyPage().getSurveyPageId()) {

                    surveyPageToResponseMap.put(commonSurveyPage, dbSurveyPageResponse);
                }
            }

        }

        SurveyResponse commonSurveyResponse =
                new SurveyResponse(dbSurveyResponse.getSurveyResponseId(),
                commonSurvey,
                dbSurveyResponse.getSurveyContext().getSurveyContextId(),
                dbSurveyResponse.getStartTime(),
                dbSurveyResponse.getEndTime(),
                new ArrayList<>(convertSurveyPageResponses(surveyPageToResponseMap)));
        
        return commonSurveyResponse;
    }

    /**
     * Converts a collection of Survey Response objects from one class to
     * another
     *
     * @param dbSurveyResponses The collection of Survey Response objects to
     * convert
     * @return Collection<SurveyResponseB> The collection of converted Survey
     * Response objects
     */
    public Collection<SurveyResponse> convertSurveyResponses(Collection<DbSurveyResponse> dbSurveyResponses) {
        Collection<SurveyResponse> commonSurveyResponses = new ArrayList<>();
        for (DbSurveyResponse dbSurveyResponse : dbSurveyResponses) {
            commonSurveyResponses.add(convertSurveyResponse(dbSurveyResponse));
        }
        return commonSurveyResponses;
    }

    /**
     * Converts a Survey Page Response class object into another class object
     *
     * @param commonSurveyPage The converted survey page the response is for
     * @param dbSurveyPageResponse The Survey Page Response object to convert
     * @return SurveyPageResponseB The converted Survey Page Response object
     */
    @SuppressWarnings("unchecked")
	public SurveyPageResponse convertSurveyPageResponse(SurveyPage commonSurveyPage, DbSurveyPageResponse dbSurveyPageResponse) {

        Map<AbstractSurveyQuestion<? extends AbstractQuestion>, List<DbQuestionResponse>> surveyQuestionToResponseMap = new HashMap<>();

        for (AbstractSurveyElement commonSurveyElement : commonSurveyPage.getElements()) {

            if (commonSurveyElement instanceof AbstractSurveyQuestion) {

                AbstractSurveyQuestion<? extends AbstractQuestion> commonSurveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) commonSurveyElement;

                List<DbQuestionResponse> responseList = new ArrayList<>();

                surveyQuestionToResponseMap.put(commonSurveyQuestion, responseList);

                for (DbQuestionResponse dbQuestionResponse : dbSurveyPageResponse.getQuestionResponses()) {

                    if (commonSurveyQuestion.getId() == dbQuestionResponse.getSurveyQuestion().getQuestionId()) {

                        responseList.add(dbQuestionResponse);
                    }
                }
            }
        }

        SurveyPageResponse commonSurveyPageResponse = new SurveyPageResponse(
                dbSurveyPageResponse.getSurveyPageResponseId(),
                dbSurveyPageResponse.getSurveyResponse().getSurveyResponseId(),
                commonSurveyPage,
                dbSurveyPageResponse.getStartTime(),
                dbSurveyPageResponse.getEndTime(),
                new ArrayList<>(convertQuestionResponses(surveyQuestionToResponseMap)));

        return commonSurveyPageResponse;
    }

    /**
     * Converts a map of converted survey pages to their unconverted responses
     * to a list of converted responses
     *
     * @param surveyPageToResponseMap Map of converted survey pages to their
     * unconverted responses to convert
     * @return Collection<SurveyPageResponseB> The collection of converted
     * Survey Page Response objects
     */
    public Collection<SurveyPageResponse> convertSurveyPageResponses(Map<SurveyPage, DbSurveyPageResponse> surveyPageToResponseMap) {

        Collection<SurveyPageResponse> commonSurveyPageResponses = new ArrayList<>();

        for (SurveyPage commonSurveyPage : surveyPageToResponseMap.keySet()) {

            commonSurveyPageResponses.add(convertSurveyPageResponse(commonSurveyPage, surveyPageToResponseMap.get(commonSurveyPage)));
        }

        return commonSurveyPageResponses;
    }

    /**
     * Converts hibernate survey properties into common survey properties
     *
     * @param properties The properties to convert
     * @return Map<SurveyPropertyKeyEnum, String> The common properties
     */
    public SurveyProperties convertSurveyProperties(Collection<DbSurveyProperty> properties) {

        Map<String, String> stringProperties = new HashMap<>();

        for (DbSurveyProperty property : properties) {

            stringProperties.put(property.getPropertyKey().getKey(), property.getPropertyValue().getValue());
        }

        return new SurveyProperties(convertProperties(stringProperties));
    }

    /**
     * Converts hibernate survey page properties into common survey page
     * properties
     *
     * @param properties The properties to convert
     * @return SurveyPageProperties The common properties
     */
    public SurveyItemProperties convertSurveyPageProperties(Collection<DbSurveyPageProperty> properties) {

        Map<String, String> stringProperties = new HashMap<>();

        for (DbSurveyPageProperty property : properties) {

            stringProperties.put(property.getPropertyKey().getKey(), property.getPropertyValue().getValue());
        }

        return new SurveyItemProperties(convertProperties(stringProperties));
    }

    /**
     * Converts hibernate survey question properties into common survey question
     * properties
     *
     * @param properties The properties to convert
     * @return SurveyQuestionProperties The common properties
     */
    public SurveyItemProperties convertSurveyQuestionProperties(Collection<DbSurveyElementProperty> properties) {

        Map<String, String> stringProperties = new HashMap<>();

        for (DbSurveyElementProperty property : properties) {

            stringProperties.put(property.getPropertyKey().getKey(), property.getPropertyValue().getValue());
        }

        return new SurveyItemProperties(convertProperties(stringProperties));
    }

    /**
     * Converts database question properties in to common question properties
     *
     * @param type The question type
     * @param properties The database question properties
     * @return AbstractQuestionProperties The common question properties
     */
    public SurveyItemProperties convertQuestionProperties(QuestionTypeEnum type, Collection<DbQuestionProperty> properties) {

        Map<SurveyPropertyKeyEnum, Serializable> questionProperties = new HashMap<>();

        for (DbQuestionProperty property : properties) {

            DbQuestionPropertyValue dbPropertyValue = property.getPropertyValue();

            Serializable value = null;

            if (dbPropertyValue != null) {

                String commonString = dbPropertyValue.getStringValue();
                
                if (commonString != null) {

                    try {

                        JSONObject jsonObj = (JSONObject) JSONValue.parse(commonString);

                        if (jsonObj != null) {

                            Serializable object = (Serializable) codec.decode(jsonObj);

                            value = object;
                        } else {

                            if(logger.isInfoEnabled()){
                                logger.info("Could not decode string '" + value + "', assuming old format.");
                            }

                            value = commonString;
                        }

                    } catch (@SuppressWarnings("unused") RuntimeException e) {
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Could not decode string '" + commonString + "', assuming old format.");
                        }
                        
                        value = commonString;
                    }

                } else {
                    
                    DbOptionList dbOptionList = dbPropertyValue.getOptionListValue();

                    if (dbOptionList != null) {

                        OptionList commonOptionList = convertOptionList(dbOptionList);
                        
                        value = commonOptionList;
                    }
                }
            }
            
            SurveyPropertyKeyEnum keyEnum = SurveyPropertyKeyEnum.valueOf(property.getPropertyKey().getKey());
            
            if(keyEnum == null){
                if(logger.isInfoEnabled()){
                    logger.info("Adding question property key " + property.getPropertyKey().getKey() + " to survey property key enum.");
                }
            	keyEnum = SurveyPropertyKeyEnum.createEnumeration(property.getPropertyKey().getKey());
            }
            
            questionProperties.put(keyEnum, value);
        }

        return new SurveyItemProperties(questionProperties);
    }

    /**
     * Converts a map of properties from one type to another
     *
     * @param properties The map of properties
     * @return Map<PropertyKeyB,String> The map of properties
     */
    public Map<SurveyPropertyKeyEnum, Serializable> convertProperties(Map<String, String> properties) {

        Map<SurveyPropertyKeyEnum, Serializable> commonProperties = new HashMap<>();

        for (String propertyKey : properties.keySet()) {

            SurveyPropertyKeyEnum key = SurveyPropertyKeyEnum.valueOf(propertyKey);
            
            if(key == null){
                if(logger.isInfoEnabled()){
                    logger.info("Adding question property key " + propertyKey + " to survey property key enum.");
                }
            	key = SurveyPropertyKeyEnum.createEnumeration(propertyKey);
            }
            
            String value = properties.get(propertyKey);

            try {

                JSONObject jsonObj = (JSONObject) JSONValue.parse(value);
                
                if(jsonObj != null) {

                    Serializable object = (Serializable) codec.decode(jsonObj);

                    commonProperties.put(key, object);
                    
                } else {
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Could not decode string '" + value + "', assuming old format.");
                    }
                    
                    commonProperties.put(key, value);
                }

            } catch (@SuppressWarnings("unused") RuntimeException e) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Could not decode string '" + value + "', assuming old format.");
                }

                commonProperties.put(key, value);
            }

   
        }

        return commonProperties;
    }

    /**
     * Converts a collection of hibernate category objects into common category
     * objects
     *
     * @param categories The collection of hibernate categories
     * @return List<Category> The list of common category objects
     */
    public List<Category> convertCategories(Collection<DbCategory> categories) {

        List<Category> commonCategories = new ArrayList<>();

        for (DbCategory dbCategory : categories) {
        	
        	Category category = new Category(
        			dbCategory.getCategoryId(),
        			dbCategory.getName(),
        			dbCategory.getVisibleToUserNames(),
        			dbCategory.getEditableToUserNames());
            commonCategories.add(category);
        }

        return commonCategories;
    }

    /**
     * Converts a collection of hibernate folder objects into common folder
     * objects
     *
     * @param folders The collection of hibernate folders
     * @return List<Folder> The list of common folder objects
     */
    public List<Folder> convertFolders(Collection<DbFolder> folders) {

        ArrayList<Folder> commonFolders = new ArrayList<Folder>();

        for (DbFolder dbFolder : folders) {

        	Folder folder = new Folder(
        			dbFolder.getFolderId(),
        			dbFolder.getName(),
        			dbFolder.getVisibleToUserNames(),
        			dbFolder.getEditableToUserNames());
        	
            commonFolders.add(folder);
        }

        return commonFolders;
    }

	/**
	 * Converts a hibernate experiment object into a common experiment object
	 * 
	 * @param dbExperiment The hibernate experiment.  Can't be null.
	 * @return The common experiment
	 */
	public DataCollectionItem convertExperiment(DbDataCollection dbExperiment) {
			
        // retrieve subject count
        long subjectSize = dbMgr.getRowCountByQuery(COUNT_SUBJECTS + "'" + dbExperiment.getId() + "'", null);

        // retrieve latest subject start time
        List<Date> dbSubjectLastAttempt = dbMgr.selectRowsByQuery(Date.class, MAX_DATE_SUBJECTS + "'" + dbExperiment.getId() + "'", -1, 1);

        Date latestSubjectAttemptDate = null;
        if (dbSubjectLastAttempt != null && !dbSubjectLastAttempt.isEmpty()) {
			// there should only be 1 result, the max date.
            latestSubjectAttemptDate = dbSubjectLastAttempt.get(0);
        }

        // retrieve the lti result count
        long ltiResultSize = dbMgr.getRowCountByQuery(COUNT_LTI_RESULTS + "'" + dbExperiment.getId() + "'", null);

        // retrieve latest lti result start time
        List<Date> dbLtiResultLastAttempt = dbMgr.selectRowsByQuery(Date.class, MAX_DATE_LTI_RESULTS + "'" + dbExperiment.getId() + "'", -1, 1);

        Date latestLtiAttemptDate = null;
        if (dbLtiResultLastAttempt != null && !dbLtiResultLastAttempt.isEmpty()) {
            // there should only be 1 result, the max date.
            latestLtiAttemptDate = dbLtiResultLastAttempt.get(0);
        }

        DataCollectionItem dataCollectionItem = new DataCollectionItem(dbExperiment.getId(), dbExperiment.getAuthorUsername(),
				dbExperiment.getName(), 
				dbExperiment.getDescription(), 
				ExperimentUrlManager.getExperimentUrl(dbExperiment.getId()), 
				dbExperiment.getCourseFolder(), 
				dbExperiment.getStatus(),
				subjectSize,
				latestSubjectAttemptDate,
				ltiResultSize,
				latestLtiAttemptDate,
				dbExperiment.getDataSetType(),
				dbExperiment.getSourceCourseId(),
				dbExperiment.getPublishedDate());
        
        Set<DataCollectionPermission> permissions = new HashSet<DataCollectionPermission>();
        if(dbExperiment.getPermissions() == null || dbExperiment.getPermissions().isEmpty()){
            //make sure the owner is set at a minimum
            DataCollectionPermission permission = 
                    new DataCollectionPermission(dbExperiment.getId(), dbExperiment.getAuthorUsername(), DataCollectionUserRole.OWNER);
            permissions.add(permission);
        }else{
            for(DbDataCollectionPermission dbPermission : dbExperiment.getPermissions()){
                
                DataCollectionPermission permission = 
                        new DataCollectionPermission(dbPermission.getDataCollectionId(), dbPermission.getUsername(), dbPermission.getDataCollectionUserRole());
                permissions.add(permission);
            }
        }
        
        dataCollectionItem.setPermissions(permissions);
        
        return dataCollectionItem;
	}
}
