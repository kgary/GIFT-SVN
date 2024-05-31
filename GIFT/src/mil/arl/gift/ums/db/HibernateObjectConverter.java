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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.common.survey.SliderRange.ScaleType;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.json.StringPayloadJSON;
import mil.arl.gift.net.api.message.codec.json.survey.FreeResponseReplyWeightsJSON;
import mil.arl.gift.net.api.message.codec.json.survey.MatrixOfChoicesReplyWeightsJSON;
import mil.arl.gift.net.api.message.codec.json.survey.OptionListJSON;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionScorerJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyItemPropertyValueListJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyScorerJSON;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbDomainSession;
import mil.arl.gift.ums.db.table.DbExperimentSubject;
import mil.arl.gift.ums.db.table.DbFolder;
import mil.arl.gift.ums.db.table.DbGlobalUser;
import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbPropertyKey;
import mil.arl.gift.ums.db.table.DbPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionResponse;
import mil.arl.gift.ums.db.table.DbQuestionType;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyElementProperty;
import mil.arl.gift.ums.db.table.DbSurveyElementType;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyPageProperty;
import mil.arl.gift.ums.db.table.DbSurveyPageResponse;
import mil.arl.gift.ums.db.table.DbSurveyProperty;
import mil.arl.gift.ums.db.table.DbSurveyResponse;
import mil.arl.gift.ums.db.table.DbUser;

/**
 * Converts Common Survey Objects to Hibernate Survey Objects
 *
 * @author jleonard
 */
public class HibernateObjectConverter {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HibernateObjectConverter.class);

    /** used for UMS database operations */
    private final UMSDatabaseManager dbMgr;
    
	/** Codec that will be used to encode/decode the property values */
    private final SurveyItemPropertyValueJSONUtil codec = new SurveyItemPropertyValueJSONUtil();

    /**
     * Constructor
     *
     * @param dbMgr The Hibernate database manager to use to connect to the UMS database
     */
    public HibernateObjectConverter(UMSDatabaseManager dbMgr) {
        this.dbMgr = dbMgr;
    }

    /**
     * Converts a hibernate question object to a common question object
     *
     * @param dbQuestion The database question object to modify
     * @param question The Question object to convert. </br>
     * Note: any option list should NOT have their ids set to zero when importing this question (e.g. from a course import) because
     * this method will populate the mapper (if not null) with the original option list ids to the new ids in order to reuse that new id
     * for subsequent instances of that same option list in the import.
     * @param session The database session to work in
     * @param mapper where to maintain information about the ids that have changed during the insert process.
     * 		  Can't be null if performing an import for the question from an external source like a course import.
     */
    private void convertQuestion(DbQuestion dbQuestion, AbstractQuestion question, Session session, ExternalSurveyMapper mapper) {

        dbQuestion.setText(question.getText());

        dbQuestion.setVisibleToUserNames(question.getVisibleToUserNames());
        dbQuestion.setEditableToUserNames(question.getEditableToUserNames());

        QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.valueOf(question);

        DbQuestionType questionType = dbMgr.selectRowByExample(new DbQuestionType(questionTypeEnum.getName()), DbQuestionType.class, session);

        if (questionType != null) {

            dbQuestion.setQuestionType(questionType);

        } else {

            throw new RuntimeException("The question type of '" + questionTypeEnum.getName() + "' does not exist in the database.");
        }

        convertQuestionProperties(dbQuestion, question.getProperties(), session, mapper);

        convertCategories(dbQuestion.getCategories(), question.getCategories(), question.getVisibleToUserNames(), question.getEditableToUserNames(), session);
    }

    /**
     * Converts a common question object in to a hibernate question
     *
     * @param question The question object to convert. </br>
     * Note: any option list should NOT have their ids set to zero when importing this question (e.g. from a course import) because
     * this method will populate the mapper (if not null) with the original option list ids to the new ids in order to reuse that new id
     * for subsequent instances of that same option list in the import.
     * @param session The database session to work in
     * @param mapper where to maintain information about the ids that have changed during the insert process.
     * 		  Can't be null if performing an import for the question from an external source like a course import.
     * @return DbQuestion The hibernate question object
     * @throws Exception if there was a problem converting the question into a database class instance
     */
    public DbQuestion convertQuestion(AbstractQuestion question, Session session, ExternalSurveyMapper mapper) throws Exception {

        DbQuestion dbQuestion = (DbQuestion) session.get(DbQuestion.class, question.getQuestionId());

        if(logger.isDebugEnabled()){
            logger.debug("Checking question id: " + question.getQuestionId());
        }
        if (dbQuestion == null) {

            if(logger.isDebugEnabled()){
                logger.debug("dbQuestion is null, creating a new db question with text: "  + question.getText());
            }
            dbQuestion = new DbQuestion();
            dbQuestion.setQuestionProperties(new HashSet<DbQuestionProperty>());
            dbQuestion.setCategories(new HashSet<DbCategory>());
        } else {
            if(logger.isDebugEnabled()){
                logger.debug("db question is not null, question has: " + dbQuestion.getQuestionProperties().size() + " properties.");
            }
        }

        try{
            convertQuestion(dbQuestion, question, session, mapper);
        }catch(Exception e){
            throw new Exception("The question with text '"+question.getText()+"' (id "+question.getQuestionId()+") is invalid because it caused an exception.", e);
        }

        return dbQuestion;
    }

    /**
     * Converts a common List Option object into it's Hibernate representation
     *
     * @param dbListOption The database object to modify
     * @param listOption The List Option to convert
     * @param sortKey The option list number
     * @param session The database session to work in
     */
    private void convertListOption(DbOptionList dbOptionList, DbListOption dbListOption, ListOption listOption, int sortKey, Session session) {

        dbListOption.setOptionList(dbOptionList);
        dbListOption.setText(listOption.getText());
        dbListOption.setSortKey(sortKey);
    }

    /**
     * Converts a collection of List Option objects from one class to another
     *
     * @param dbOptionList the hibernate class to populate attributes for, this will match the listOptions at the end of this method
     * @param listOptions The collection of List Option objects to convert, i.e. this is the list that should be forced into the dbOptionList and
     * make sure the dbOptionList matches this list
     * @param session The database session to work in. Can't be null.
     * @return a map that relates ListOptions with newly inserted DbListOptions. Can be null if not used
     */
    private Map<ListOption, DbListOption> convertListOptions(DbOptionList dbOptionList, List<ListOption> listOptions, Session session) {

    	// This map relates original ListOptions with newly inserted DbListOptions.
    	// This way we can map old ids to new ids. The new ids are generated later when session.save() is called.
    	Map<ListOption, DbListOption> listOptionMap = null;

        Set<DbListOption> toRemoveDbListOptions = new HashSet<>(dbOptionList.getListOptions());

        int start = Math.max(listOptions.size(), dbOptionList.getListOptions().size()) + 1;
        int listOptionIndex = start;

        for (ListOption commonListOption : listOptions) {

            boolean found = false;

            if (commonListOption.getId() > 0) {

                for (DbListOption dbListOption : dbOptionList.getListOptions()) {

                    if (dbListOption.getListOptionId() == commonListOption.getId()) {

                        convertListOption(dbOptionList, dbListOption, commonListOption, listOptionIndex, session);

                        session.update(dbListOption);

                        toRemoveDbListOptions.remove(dbListOption);

                        found = true;

                        break;
                    }
                }
            }

            if (!found) {

            	// Inserting new list option into the database
                DbListOption dbListOption = new DbListOption(commonListOption.getText(), dbOptionList, listOptionIndex);

                if (listOptionMap == null) {
                	listOptionMap = new HashMap<>();
                }

                // Map the dbListOption and commonListOption together so that old ids can be mapped to new ids when they are generated
                listOptionMap.put(commonListOption, dbListOption);

                dbOptionList.getListOptions().add(dbListOption);
            }

            listOptionIndex += 1;
        }

        for (DbListOption toRemoveDbListOption : toRemoveDbListOptions) {

            dbOptionList.getListOptions().remove(toRemoveDbListOption);

            session.delete(toRemoveDbListOption);
        }

        session.flush();

        //this was after flush
        for (DbListOption dbListOption : dbOptionList.getListOptions()) {

            dbListOption.setSortKey(dbListOption.getSortKey() - start);
        }

        return listOptionMap;
    }

    /**
     * Converts a Option List class object into another class object
     *
     * @param dbOptionList the hibernate class to populate attributes for, those attributes come from a combination of the option list stored in the
     * database (retrieved by the commonOptionList id attribute) if updating an existing option list or creating a new one from an existing
     * option list, AND the attributes from commonOptionList. </br>
     * NOTE: the dbOptionList id should be 0 when this method should create a new option list and not 0 when it should merge
     * changes into an existing option list.
     * @param commonOptionList The Option List object to convert
     * @param session The database session to work in. Can't be null.
     * @return a map that relates ListOptions with newly inserted DbListOptions,
     * or null if an existing option list was updated.
     */
    private Map<ListOption, DbListOption> convertOptionList(DbOptionList dbOptionList, OptionList commonOptionList, Session session, ExternalSurveyMapper mapper) {

    	dbOptionList.setIsShared(commonOptionList.getIsShared());
    	dbOptionList.setName(commonOptionList.getName());

	    DbOptionList oldDbOptionList = (DbOptionList) session.get(DbOptionList.class, commonOptionList.getId());

	    /*
	     * Determine whether an existing option list should be updated (i.e. enter this if and return)
	     * or a new option list should be created.
	     * If the mapper is not null, a survey context is being imported and
	     * new ids are mapped to the survey context's old ids.
	     * If the in-database option list and the in-memory option list share
	     * the same persistent object and the values are not equal, an existing
	     * option list is being modified.
	     *
	     * #3981 - Before this if would check whether the existing option list in the db was different
	     * than the dbOptionList provided.  In cases where this method was called from convertOptionList(String userName, OptionList optionList, Session session)
	     * and the option list previously existed in the db, the if would be skipped.  For this ticket the if statement would
	     * be entered (when it shouldn't) because the source option list existed with an id but the newly created option list
	     * from a select existing survey (survey copy) had an id of 0 and therefore was not equal.  The cases are described below
	     * and the solution was to allow the caller of this method to use the dbOptionList id attribute as an indicator of whether
	     * this is a change to an existing option list (id != 0) or a new option list (id = 0);
	     */

	    // case 1: in db, just saving - want to enter here (oldDbOptionList != null, dbOptionList.getOptionListId() != 0)
	    // case 2: in db, copying - want to skip here (oldDbOptionList != null, dbOptionList.getOptionListId() == 0)
	    // case 3: in db, editing existing question choice - want to skip here (oldDbOptionList != null, dbOptionList.getOptionListId() != 0)
	    // case 4: not in db, creating - want to skip here (oldDbOptionList == null)
	    boolean existingQuestion = dbOptionList.getOptionListId() != 0;
	    if(mapper == null && oldDbOptionList != null && existingQuestion) {

	    	/*
	    	 * This represents the case where an option list is being modified.
	    	 * When an option list is being *copied* or *created*, all ids are
	    	 * initially equal to 0 and this logic is skipped.
	    	 * New ids are generated upon session.save()
	    	 * When an option list is being *modified*, the ids are not 0.
	    	 * All of these ids need to be copied over to prevent new entries
	    	 * in the database and to keep DKF references valid. Apply changes
	    	 * to existing database entries with session.merge()
	    	 */

	    	Iterator<DbListOption> listOptionIterator;

	    	/* Keep track of which list options were updated since oldDbOptionList may have entries that should be deleted */
	    	ArrayList<Integer> listOptionsToUpdate = new ArrayList<>();

	    	/* collect the choices that will be used to replaced the existing choices */
	    	Set<DbListOption> newDbListOptionSet = new HashSet<>();
            int commonIndex = 0;
	    	for(ListOption l : commonOptionList.getListOptions()) {
                l.setSortKey(commonIndex++);
	    	    DbListOption d = null;
	    	    if(l.getId() == 0){
	                /* a new choice */
	    	        d = new DbListOption(l.getText(), dbOptionList, l.getSortKey());

	    	    }else{
	    	        /* make sure the choice being passed in is already in the question's choices */
                    for (DbListOption dbListOption : oldDbOptionList.getListOptions()) {
                        if (StringUtils.equals(dbListOption.getText(), l.getText())
                                && dbListOption.getSortKey() == l.getSortKey()) {
	    	                d = dbListOption;
	    	                break;
	    	            }
	    	        }

	    	        if(d != null) {
	    	            /* found the choice in the question's choices, mark for updating */
	                    listOptionsToUpdate.add(l.getId());
	                }else{
	                    /* (fall through case) an unknown id was found on the choice provided and
	                     *  is not already in the question's choices */
	                    d = new DbListOption(l.getText(), dbOptionList, l.getSortKey());
	                }
	    	    }

	    		newDbListOptionSet.add(d);

	    	}

	    	/* now replace the existing options with the newly built set */
	    	dbOptionList.getListOptions().clear();
	    	dbOptionList.getListOptions().addAll(newDbListOptionSet);

	    	/* Using iterator to prevent ConcurrentModificationException */
	    	listOptionIterator = oldDbOptionList.getListOptions().iterator();

	    	while(listOptionIterator.hasNext()) {
	    		/* Delete list options from the database */

	    		DbListOption l = listOptionIterator.next();
	    		if(!listOptionsToUpdate.contains(l.getListOptionId())) {
		    		listOptionIterator.remove();
		    		session.delete(l);
                }
	    	}

	    	/* Save the old option list to remove any mappings to deleted list options. */
	    	session.save(oldDbOptionList);

	    	/* Flush the session to synchronize the database state. */
	    	session.flush();

	    	/* Copy the option list id. */
	    	dbOptionList.setOptionListId(commonOptionList.getId());

	    	return null;
	    }


	    Map<ListOption, DbListOption> listOptionMap = null;
	    if (commonOptionList.getListOptions() != null && !commonOptionList.getListOptions().isEmpty()) {
	        listOptionMap = convertListOptions(dbOptionList, commonOptionList.getListOptions(), session);
	    } else {
	        if(logger.isDebugEnabled()){
	            logger.debug("list options was null or empty, no items to convert.");
	        }
	    }


	    return listOptionMap;
    }

    /**
     * Converts a common option list object in to a Hibernate option list object
     *
     * @param userName The user to convert the option list for
     * @param optionList The option list object to convert
     * @param session The database session to work in
     * @return DbOptionList The hibernate option list object
     */
    public DbOptionList convertOptionList(String userName, OptionList optionList, Session session) {

        DbOptionList dbOptionList = (DbOptionList) session.get(DbOptionList.class, optionList.getId());

        if (dbOptionList == null) {

            dbOptionList = new DbOptionList();
            dbOptionList.setListOptions(new HashSet<DbListOption>());
            dbOptionList.getEditableToUserNames().add(userName);
            dbOptionList.getVisibleToUserNames().add(userName);
        }

        convertOptionList(dbOptionList, optionList, session, null);

        return dbOptionList;
    }

    /**
     * Converts a common survey object in to a Hibernate survey object
     *
     * @param survey The Survey object to convert
     * @param session The database session to work in
     * @return DbSurvey The converted Hibernate survey object
     */
    public DbSurvey convertSurvey(Survey survey, Session session) {

        DbSurvey dbSurvey = (DbSurvey) session.get(DbSurvey.class, survey.getId());

        if (dbSurvey == null) {

            dbSurvey = new DbSurvey();
            dbSurvey.setSurveyPages(new HashSet<DbSurveyPage>());
            dbSurvey.setProperties(new HashSet<DbSurveyProperty>());
        }

        convertSurvey(dbSurvey, survey, session);

        return dbSurvey;
    }

    /**
     * Converts a common option list object in to a Hibernate option list object
     *
     * @param dbSurvey The Hibernate survey object to modify
     * @param survey The common survey object to convert
     * @param session The database session to work in
     */
    private void convertSurvey(DbSurvey dbSurvey, Survey survey, Session session) {

        dbSurvey.setName(survey.getName());
        dbSurvey.setVisibleToUserNames(survey.getVisibleToUserNames());
        dbSurvey.setEditableToUserNames(survey.getEditableToUserNames());

        if (survey.getFolder() != null) {

            DbFolder dbFolder = dbMgr.selectRowByExample(new DbFolder(survey.getFolder()), DbFolder.class, session);

            if (dbFolder != null) {

                dbSurvey.setFolder(dbFolder);
            }
        }else{
        	dbSurvey.setFolder(null);
        }

        session.saveOrUpdate(dbSurvey);

        convertSurveyPages(dbSurvey, survey.getPages(), session);
        convertSurveyProperties(dbSurvey, survey.getProperties(), session);
    }

    /**
     * Populate the hibernate survey object's attributes with the common Survey Page class object
     *
     * @param surveyPage The Survey Page object to convert
     * @param pageNumber The number of the page in the survey
     * @param session The database session to work in
     */
    private void convertSurveyPage(DbSurvey dbSurvey, DbSurveyPage dbSurveyPage, SurveyPage surveyPage, int pageNumber, Session session) {

        dbSurveyPage.setSurvey(dbSurvey);

        dbSurveyPage.setName(surveyPage.getName());
        dbSurveyPage.setPageNumber(pageNumber);

        session.saveOrUpdate(dbSurveyPage);

        convertSurveyElements(dbSurveyPage, surveyPage.getElements(), session);
        convertSurveyPageProperties(dbSurveyPage, surveyPage.getProperties(), session);
    }

    /**
     * Converts a collection of Survey Page objects from one class to another
     *
     * @param dbSurvey the hibernate class to populate attributes for
     * @param surveyPages The collection of Survey Page objects to convert
     * @param session The database session to work in
     */
    private void convertSurveyPages(DbSurvey dbSurvey, Collection<SurveyPage> surveyPages, Session session) {

        Set<DbSurveyPage> toRemoveDbPages = new HashSet<>(dbSurvey.getSurveyPages());

        int start = Math.max(surveyPages.size(), dbSurvey.getSurveyPages().size()) + 1;
        int pageNumber = start;

        for (SurveyPage surveyPage : surveyPages) {

            boolean found = false;

            if (surveyPage.getId() > 0) {

                for (DbSurveyPage dbSurveyPage : dbSurvey.getSurveyPages()) {

                    if (dbSurveyPage.getSurveyPageId() == surveyPage.getId()) {

                        found = true;

                        toRemoveDbPages.remove(dbSurveyPage);

                        convertSurveyPage(dbSurvey, dbSurveyPage, surveyPage, pageNumber, session);
                    }
                }
            }

            if (!found) {

                DbSurveyPage dbSurveyPage = new DbSurveyPage();
                dbSurveyPage.setSurveyElements(new HashSet<DbSurveyElement>());
                dbSurveyPage.setProperties(new HashSet<DbSurveyPageProperty>());

                convertSurveyPage(dbSurvey, dbSurveyPage, surveyPage, pageNumber, session);

                dbSurvey.getSurveyPages().add(dbSurveyPage);
            }

            pageNumber += 1;
        }

        for (DbSurveyPage toRemoveDbPage : toRemoveDbPages) {

            dbSurvey.getSurveyPages().remove(toRemoveDbPage);

            session.delete(toRemoveDbPage);
        }

        session.flush();

        for (DbSurveyPage dbSurveyPage : dbSurvey.getSurveyPages()) {

            dbSurveyPage.setPageNumber(dbSurveyPage.getPageNumber() - start);
        }
    }

    /**
     * Converts a common Survey Element class object into a hibernate representation.
     *
     * @param surveyElement the common class to use to get a hibernate representation
     * @param session the database session to work in
     * @return DbSurveyElement The converted Hibernate survey element
     */
    public DbSurveyElement convertSurveyElement(AbstractSurveyElement surveyElement, Session session){

        DbSurveyElement dbSurveyElement = (DbSurveyElement) session.get(DbSurveyElement.class, surveyElement.getId());

        if (dbSurveyElement == null) {

            dbSurveyElement = new DbSurveyElement();
            dbSurveyElement.setProperties(new HashSet<DbSurveyElementProperty>());

            DbSurveyPage surveyPage = dbMgr.selectRowById(surveyElement.getSurveyPageId(), DbSurveyPage.class);
            dbSurveyElement.setSurveyPage(surveyPage);
        }

        convertSurveyElement(dbSurveyElement.getSurveyPage(), dbSurveyElement, surveyElement, dbSurveyElement.getElementNumber(), session);

        return dbSurveyElement;
    }

    /**
     * Converts Survey Element object to the database representation
     *
     * @param dbSurveyPage the page the element is on
     * @param dbSurveyElement the element being converted too
     * @param surveyElement the common object containing the attributes to convert
     * @param questionNumber the next index in the collection of survey elements for what will be the final version of the survey page
     * @param session The database session to work in
     */
    @SuppressWarnings("unchecked")
    public void convertSurveyElement(DbSurveyPage dbSurveyPage, DbSurveyElement dbSurveyElement, AbstractSurveyElement surveyElement, int questionNumber, Session session) {

        dbSurveyElement.setSurveyPage(dbSurveyPage);

        DbSurveyElementType surveyElementType = dbMgr.selectRowByExample(new DbSurveyElementType(surveyElement.getSurveyElementType().getName()), DbSurveyElementType.class, session);

        if (surveyElementType != null) {

            dbSurveyElement.setSurveyElementType(surveyElementType);

        } else {

            throw new RuntimeException("The survey element type of '" + surveyElement.getSurveyElementType() + "' does not exist in the database.");
        }

        dbSurveyElement.setSurveyElementId(surveyElement.getId());

        if (surveyElement instanceof AbstractSurveyQuestion) {

            dbSurveyElement.setQuestionId(((AbstractSurveyQuestion<? extends AbstractQuestion>) surveyElement).getQuestion().getQuestionId());
        }

        dbSurveyElement.setElementNumber(questionNumber);

        session.saveOrUpdate(dbSurveyElement);

        convertSurveyElementProperties(dbSurveyElement, surveyElement.getProperties(), session);
    }

    /**
     * Converts a collection of common survey elements to database survey elements
     *
     * @param dbSurveyPage the hibernate class to populate attributes for
     * @param surveyElements The collection of common survey elements to convert
     * @param session The database session to work in
     */
    private void convertSurveyElements(DbSurveyPage dbSurveyPage, Collection<AbstractSurveyElement> surveyElements, Session session) {

        Set<DbSurveyElement> toRemoveDbElements = new HashSet<>(dbSurveyPage.getSurveyElements());

        //get the next index in the collection of elements that will be in the final list of survey elements
        int start = Math.max(surveyElements.size(), dbSurveyPage.getSurveyElements().size()) + 1;
        int elementNumber = start;

        for (AbstractSurveyElement surveyElement : surveyElements) {

            boolean found = false;

            if (surveyElement.getId() > 0) {

                for (DbSurveyElement dbSurveyElement : dbSurveyPage.getSurveyElements()) {

                    if (dbSurveyElement.getSurveyElementId() == surveyElement.getId()) {

                        found = true;

                        toRemoveDbElements.remove(dbSurveyElement);

                        convertSurveyElement(dbSurveyPage, dbSurveyElement, surveyElement, elementNumber, session);
                    }
                }
            }

            if (!found) {

                DbSurveyElement dbSurveyElement = new DbSurveyElement();
                dbSurveyElement.setProperties(new HashSet<DbSurveyElementProperty>());

                convertSurveyElement(dbSurveyPage, dbSurveyElement, surveyElement, elementNumber, session);

                dbSurveyPage.getSurveyElements().add(dbSurveyElement);
            }

            elementNumber += 1;
        }

        for (DbSurveyElement toRemoveDbElement : toRemoveDbElements) {

            dbSurveyPage.getSurveyElements().remove(toRemoveDbElement);

            session.delete(toRemoveDbElement);
        }

        session.flush();

        for (DbSurveyElement dbSurveyElement : dbSurveyPage.getSurveyElements()) {

            dbSurveyElement.setElementNumber(dbSurveyElement.getElementNumber() - start);
        }
    }

    /**
     * Converts a Survey Context class object into another class object
     *
     * @param surveyContext The Survey Context object to convert
     * @param session The database session to work in
     * @return SurveyContextB The converted Survey Context object
     */
    public DbSurveyContext convertSurveyContext(SurveyContext surveyContext, Session session) {

        DbSurveyContext dbSurveyContext = SurveyContextUtil.getSurveyContextEager(surveyContext.getId(), dbMgr, session);

        if (dbSurveyContext == null) {

            dbSurveyContext = new DbSurveyContext();
            dbSurveyContext.setSurveyContextSurveys(new HashSet<DbSurveyContextSurvey>());
        }

        convertSurveyContext(dbSurveyContext, surveyContext, session);

        return dbSurveyContext;
    }

    public DbSurveyContextSurvey convertSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey, Session session){

        DbSurvey dbSurvey = (DbSurvey) session.get(DbSurvey.class, surveyContextSurvey.getSurvey().getId());

        if (dbSurvey != null) {

            DbSurveyContext dbSurveyContext = (DbSurveyContext) session.get(DbSurveyContext.class, surveyContextSurvey.getSurveyContextId());
            if(dbSurveyContext != null){

                DbSurveyContextSurvey dbSurveyContextSurvey = new DbSurveyContextSurvey(dbSurveyContext, dbSurvey, surveyContextSurvey.getKey());

                return dbSurveyContextSurvey;

            }else{
                throw new RuntimeException("The survey context of id '" + surveyContextSurvey.getSurveyContextId() + "' does not exist in the database.");
            }

        } else {

            throw new RuntimeException("The survey of id '" + surveyContextSurvey.getSurvey().getId() + "' does not exist in the database.");
        }
    }

    /**
     * Populate the Survey Context hibernate class attributes with the common class.
     *
     * @param dbSurveyContext the hibernate class to populate attributes for
     * @param surveyContext the common class to use to create the hibernate representation
     * @param session The database session to work in
     */
    private void convertSurveyContext(DbSurveyContext dbSurveyContext, SurveyContext surveyContext, Session session) {

        dbSurveyContext.setName(surveyContext.getName());
        dbSurveyContext.setVisibleToUserNames(surveyContext.getVisibleToUserNames());
        dbSurveyContext.setEditableToUserNames(surveyContext.getEditableToUserNames());

        session.saveOrUpdate(dbSurveyContext);

        convertSurveyContextSurveys(dbSurveyContext, surveyContext.getContextSurveys(), session);
    }

    /**
     * Converts a collection of Survey Context Survey objects from one class to
     * another
     *
     * @param dbSurveyContext the hibernate class to populate attributes for
     * @param surveyContextSurveys The collection of Survey Context Survey
     * objects to convert
     * @param session The database session to work in
     */
    private void convertSurveyContextSurveys(DbSurveyContext dbSurveyContext, Collection<SurveyContextSurvey> surveyContextSurveys, Session session) {

        Set<DbSurveyContextSurvey> toRemoveDbScss = new HashSet<>(dbSurveyContext.getSurveyContextSurveys());

        for (SurveyContextSurvey surveyContextSurvey : surveyContextSurveys) {

            DbSurvey dbSurvey = (DbSurvey) session.get(DbSurvey.class, surveyContextSurvey.getSurvey().getId());

            if (dbSurvey != null) {

                boolean found = false;

                for (DbSurveyContextSurvey dbSurveyContextSurvey : dbSurveyContext.getSurveyContextSurveys()) {

                    if (surveyContextSurvey.getKey().equals(dbSurveyContextSurvey.getGiftKey())) {

                        if (dbSurvey.getSurveyId() != dbSurveyContextSurvey.getSurvey().getSurveyId()) {

                            dbSurveyContextSurvey.setSurvey(dbSurvey);
                        }

                        toRemoveDbScss.remove(dbSurveyContextSurvey);

                        found = true;
                    }
                }

                if (!found) {

                    dbSurveyContext.getSurveyContextSurveys().add(new DbSurveyContextSurvey(dbSurveyContext, dbSurvey, surveyContextSurvey.getKey()));
                }

            } else {

                throw new RuntimeException("The survey of id '" + surveyContextSurvey.getSurvey().getId() + "' does not exist in the database.");
            }
        }

        for (DbSurveyContextSurvey toRemoveDbScs : toRemoveDbScss) {

            dbSurveyContext.getSurveyContextSurveys().remove(toRemoveDbScs);

            session.delete(toRemoveDbScs);
        }
    }

    /**
     * Converts a Survey Response class object into another class object
     *
     * @param dbSurveyResponse the hibernate class to populate attributes for
     * @param surveyResponse The Survey Response object to convert
     * @param userSession contains information about the user session
     * @param session The database session to work in
     * @param domainSessionId the domain session id
     */
    private void convertSurveyResponse(DbSurveyResponse dbSurveyResponse, SurveyResponse surveyResponse, UserSession userSession, Session session, int domainSessionId) {

        DbSurvey dbSurvey = (DbSurvey) session.get(DbSurvey.class, surveyResponse.getSurveyId());

        if (dbSurvey != null) {

            dbSurveyResponse.setSurveyResponseId(surveyResponse.getSurveyResponseId());
            dbSurveyResponse.setSurvey(dbSurvey);

            // Only one of 3 'ids' here is used to identify the user making the response.
            //   1) The global user id (if it exists)
            //   2) The gift user id (from the giftuser database table).
            //   3) The experiment id (subject/experiment id key) if it exists.
            // In the future, it may be better to have different types of users migrate to using the global id.
            if (userSession.getGlobalUserId() != null) {
                DbGlobalUser globalUser = dbMgr.selectRowById(userSession.getGlobalUserId(), DbGlobalUser.class);
                if (globalUser == null) {
                    throw new RuntimeException("The global user id of'" + userSession.getGlobalUserId() + "' does not exist in the database.");
                }

                dbSurveyResponse.setGlobalUser(globalUser);
            } else if(userSession.getExperimentId() == null){
                DbUser dbUser = (DbUser) session.get(DbUser.class, userSession.getUserId());

                if (dbUser == null) {

                    throw new RuntimeException("The user of id '" + userSession.getUserId() + "' does not exist in the database.");
                }

                dbSurveyResponse.setUser(dbUser);
            }else{

                DbExperimentSubject dbExperimentSubject = dbMgr.getExperimentSubject(userSession.getExperimentId(), userSession.getUserId());

                if (dbExperimentSubject == null) {

                    throw new RuntimeException("The subject of id '" + userSession.getUserId() + "' in experiment "+userSession.getExperimentId()+" does not exist in the database.");
                }

                dbSurveyResponse.setSubject(dbExperimentSubject);
            }


            DbSurveyContext dbSurveyContext = (DbSurveyContext) session.get(DbSurveyContext.class, surveyResponse.getSurveyContextId());

            if (dbSurveyContext == null) {

                throw new RuntimeException("The survey context of id '" + surveyResponse.getSurveyContextId() + "' does not exist in the database.");
            }

            dbSurveyResponse.setSurveyContext(dbSurveyContext);

            DbDomainSession dbDomainSession = (DbDomainSession) session.get(DbDomainSession.class, domainSessionId);

            if (dbDomainSession == null) {

                throw new RuntimeException("The domain session of id '" + domainSessionId + "' does not exist in the database.");
            }

            dbSurveyResponse.setDomainSession(dbDomainSession);

            dbSurveyResponse.setStartTime(surveyResponse.getSurveyStartTime());
            dbSurveyResponse.setEndTime(surveyResponse.getSurveyEndTime());

            convertSurveyPageResponses(dbSurveyResponse, surveyResponse.getSurveyPageResponses(), session);

        } else {

            throw new RuntimeException("The survey of id '" + surveyResponse.getSurveyId() + "' does not exist in the database.");
        }
    }

    /**
     * Converts a common survey response in to a Hibernate survey response
     * object
     *
     * WARNING: The ID of the survey response parameter is ignored, so new
     * survey response objects are created every time
     *
     * TODO: Update this method and subsequent methods to load and update
     * existing responses from the database
     *
     * @param surveyResponse The common survey response object to convert
     * @param userSession contains information about the user session
     * @param session The database session to work in
     * @param domainSessionId the domain session id
     * @return DbSurveyResponse The Hibernate survey response object
     */
    public DbSurveyResponse convertSurveyResponse(SurveyResponse surveyResponse, UserSession userSession, Session session, int domainSessionId) {

        DbSurveyResponse dbSurveyResponse = new DbSurveyResponse();
        dbSurveyResponse.setSurveyPageResponses(new HashSet<DbSurveyPageResponse>());

        convertSurveyResponse(dbSurveyResponse, surveyResponse, userSession, session, domainSessionId);

        return dbSurveyResponse;

    }

    /**
     * Converts a Survey Page Response class object into another class object
     *
     * @param dbSurveyResponse the hibernate class to populate attributes for
     * @param dbSurveyPage The converted survey page the response is for
     * @param dbSurveyPageResponse the hibernate class to populate attributes for
     * @param surveyPageResponse The Survey Page Response object to convert
     * @param session The database session to work in
     */
    private void convertSurveyPageResponse(DbSurveyResponse dbSurveyResponse, DbSurveyPage dbSurveyPage, DbSurveyPageResponse dbSurveyPageResponse, SurveyPageResponse surveyPageResponse, Session session) {

        dbSurveyPageResponse.setSurveyPageResponseId(surveyPageResponse.getSurveyPageResponseId());
        dbSurveyPageResponse.setSurveyResponse(dbSurveyResponse);
        dbSurveyPageResponse.setSurveyPage(dbSurveyPage);
        dbSurveyPageResponse.setStartTime(surveyPageResponse.getStartTime());
        dbSurveyPageResponse.setEndTime(surveyPageResponse.getEndTime());

        convertQuestionResponses(dbSurveyPageResponse, surveyPageResponse.getQuestionResponses(), session);
    }

    /**
     * Converts a map of converted survey pages to their unconverted responses
     * to a list of converted responses
     *
     * @param dbSurveyResponse the hibernate class to populate attributes for
     * @param session The database session to work in
     */
    private void convertSurveyPageResponses(DbSurveyResponse dbSurveyResponse, List<SurveyPageResponse> surveyPageResponses, Session session) {

        for (DbSurveyPage dbSurveyPage : dbSurveyResponse.getSurvey().getSurveyPages()) {

            for (mil.arl.gift.common.survey.SurveyPageResponse pageResponse : surveyPageResponses) {

                if (dbSurveyPage.getSurveyPageId() == pageResponse.getSurveyPage().getId()) {

                    DbSurveyPageResponse dbSurveyPageResponse = new DbSurveyPageResponse();
                    dbSurveyPageResponse.setQuestionResponses(new HashSet<DbQuestionResponse>());

                    convertSurveyPageResponse(dbSurveyResponse, dbSurveyPage, dbSurveyPageResponse, pageResponse, session);

                    dbSurveyResponse.getSurveyPageResponses().add(dbSurveyPageResponse);
                    break;
                }
            }
        }
    }

    /**
     * Converts a Question Response class object into another class object
     *
     * @param dbSurveyPageResponse The survey response for a survey page
     * @param questionResponse The Question Response object to convert
     * @param session The database session to work in
     */
    public void convertQuestionResponse(DbSurveyPageResponse dbSurveyPageResponse, AbstractQuestionResponse questionResponse, Session session) {

        for (QuestionResponseElement responseElement : questionResponse.getResponses()) {

            DbQuestionResponse dbQuestionResponse = new DbQuestionResponse();

            dbQuestionResponse.setQuestionResponseId(responseElement.getQuestionResponseId());
            // database does not allow a null text
            dbQuestionResponse.setText(responseElement.getText() == null ? mil.arl.gift.common.io.Constants.EMPTY : responseElement.getText());

            if (responseElement.getChoices() != null) {

                DbOptionList textOptionList = (DbOptionList) session.get(DbOptionList.class, responseElement.getChoices().getId());

                if (textOptionList == null) {

                    throw new RuntimeException("The option list of id '" + responseElement.getChoices().getId() + "' does not exist in the database.");
                }

                dbQuestionResponse.setTextOptionList(textOptionList);
            }

            if(responseElement.getRowChoice() != null){
                dbQuestionResponse.setRowText(responseElement.getRowChoice().getText());
            }

            if (responseElement.getRowChoices() != null) {

                DbOptionList rowOptionList = (DbOptionList) session.get(DbOptionList.class, responseElement.getRowChoices().getId());

                if (rowOptionList == null) {

                    throw new RuntimeException("The option list of id '" + responseElement.getRowChoices().getId() + "' does not exist in the database.");
                }

                dbQuestionResponse.setRowTextOptionList(rowOptionList);
            }

            DbSurveyElement surveyQuestion = (DbSurveyElement) session.get(DbSurveyElement.class, questionResponse.getSurveyQuestion().getId());

            if (surveyQuestion == null) {

                throw new RuntimeException("The survey element of id '" + questionResponse.getSurveyQuestion().getId() + "' does not exist in the database.");
            }

            dbQuestionResponse.setSurveyQuestion(surveyQuestion);
            dbQuestionResponse.setSurveyPageResponse(dbSurveyPageResponse);
            dbQuestionResponse.setAnswerTime(responseElement.getAnswerTime());

            dbSurveyPageResponse.getQuestionResponses().add(dbQuestionResponse);
        }
    }

    /**
     * Converts a map of converted survey questions to their unconverted
     * responses to a list of converted responses
     *
     * @param dbSurveyPageResponse the hibernate class to populate attributes for
     * @param questionResponses the common class to use to create a hibernate representation
     * @param session The database session to work in
     */
    private void convertQuestionResponses(DbSurveyPageResponse dbSurveyPageResponse, List<AbstractQuestionResponse> questionResponses, Session session) {

        for (DbSurveyElement surveyQuestion : dbSurveyPageResponse.getSurveyPage().getSurveyElements()) {

            for (AbstractQuestionResponse response : questionResponses) {

                if (response.getSurveyQuestion().getId() == surveyQuestion.getSurveyElementId()) {

                    convertQuestionResponse(dbSurveyPageResponse, response, session);
                    break;
                }
            }
        }
    }

    /**
     * Converts common survey properties into hibernate survey properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param dbSurvey The survey of the properties to convert
     * @param properties The properties to convert
     * @param session The database session to work in
     */
    public void convertSurveyProperties(DbSurvey dbSurvey, SurveyItemProperties properties, Session session) {

        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyProperty> toRemoveDbProperties = new HashSet<>(dbSurvey.getProperties());

        for (SurveyPropertyKeyEnum propertyKey : properties.getKeys()) {

            DbPropertyKey dbPropertyKey = dbMgr.selectRowByExample(new DbPropertyKey(propertyKey.getName()), DbPropertyKey.class, session);

            if (dbPropertyKey != null) {

                boolean found = false;

                for (DbSurveyProperty dbProperty : dbSurvey.getProperties()) {

                    if (dbProperty.getPropertyKey() == dbPropertyKey) {

                        JSONObject valueJsonObj = new JSONObject();

                        codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                        String jsonString = valueJsonObj.toJSONString();

                        if (!dbProperty.getPropertyValue().getValue().contentEquals(jsonString)) {

                            DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                            if(logger.isDebugEnabled()){
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
                }

                if (!found) {

                    JSONObject valueJsonObj = new JSONObject();

                    codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                    String jsonString = valueJsonObj.toJSONString();

                    DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                    if(logger.isDebugEnabled()){
                        logger.debug("Adding survey property of "+ propertyValue + " for key: " + propertyKey + " because the survey property didn't exist.");
                    }
                    session.saveOrUpdate(propertyValue);

                    //TODO: A query for an existing property value row with the same value could decrease the table size
                    dbSurvey.getProperties().add(new DbSurveyProperty(dbSurvey, dbPropertyKey, propertyValue));
                }

            } else {

                throw new RuntimeException("The property key '" + propertyKey + "' does not exist in the database.");
            }
        }

        for (DbSurveyProperty toRemoveDbProperty : toRemoveDbProperties) {

            dbSurvey.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

        session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            dbSurvey.getProperties().add(new DbSurveyProperty(dbSurvey, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }

    /**
     * Converts common survey page properties into hibernate survey page properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param dbSurveyPage the hibernate object to populate attributes for
     * @param properties The properties to convert
     * @param session The database session to work in
     */
    public void convertSurveyPageProperties(DbSurveyPage dbSurveyPage, SurveyItemProperties properties, Session session) {

        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyPageProperty> toRemoveDbProperties = new HashSet<>(dbSurveyPage.getProperties());

        for (SurveyPropertyKeyEnum propertyKey : properties.getKeys()) {

            DbPropertyKey dbPropertyKey = dbMgr.selectRowByExample(new DbPropertyKey(propertyKey.getName()), DbPropertyKey.class, session);

            if (dbPropertyKey != null) {

                boolean found = false;

                for (DbSurveyPageProperty dbProperty : dbSurveyPage.getProperties()) {

                    if (dbProperty.getPropertyKey() == dbPropertyKey) {

                        JSONObject valueJsonObj = new JSONObject();

                        codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                        String jsonString = valueJsonObj.toJSONString();

                        if (!dbProperty.getPropertyValue().getValue().contentEquals(jsonString)) {

                            DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                            if(logger.isDebugEnabled()){
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
                }

                if (!found) {

                    JSONObject valueJsonObj = new JSONObject();

                    codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                    String jsonString = valueJsonObj.toJSONString();

                    DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                    if(logger.isDebugEnabled()){
                        logger.debug("Adding survey page property of "+propertyValue+" for key: " + propertyKey + " because the survey property didn't exist.");
                    }
                    session.saveOrUpdate(propertyValue);

                    //TODO: A query for an existing property value row with the same value could decrease the table size
                    dbSurveyPage.getProperties().add(new DbSurveyPageProperty(dbSurveyPage, dbPropertyKey, propertyValue));
                }

            } else {

                throw new RuntimeException("The property key '" + propertyKey + "' does not exist in the database.");
            }
        }

        for (DbSurveyPageProperty toRemoveDbProperty : toRemoveDbProperties) {

            dbSurveyPage.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

        session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            dbSurveyPage.getProperties().add(new DbSurveyPageProperty(dbSurveyPage, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }

    /**
     * Converts common survey question properties into hibernate survey question properties
     * WARNING: this method will flush (i.e. commit) the given session, therefore anything in the session transaction must adhere to constraints.
     *
     * @param dbSurveyElement the hibernate object to populate attributes for
     * @param properties The properties to convert
     * @param session The database session to work in
     */
    public void convertSurveyElementProperties(DbSurveyElement dbSurveyElement, SurveyItemProperties properties, Session session) {

        Map<DbPropertyKey, DbPropertyValue> toAddMap = new HashMap<>();

        Set<DbSurveyElementProperty> toRemoveDbProperties = new HashSet<>(dbSurveyElement.getProperties());

        for (SurveyPropertyKeyEnum propertyKey : properties.getKeys()) {

            //check if a property of the same name exists already
            DbPropertyKey dbPropertyKey = dbMgr.selectRowByExample(new DbPropertyKey(propertyKey.getName()), DbPropertyKey.class, session);

            if (dbPropertyKey != null) {
                //it does exist

                boolean found = false;

                //find the survey's value for this property
                for (DbSurveyElementProperty dbProperty : dbSurveyElement.getProperties()) {

                    if (dbProperty.getPropertyKey() == dbPropertyKey) {
                        //found the survey's property db row instance

                        JSONObject valueJsonObj = new JSONObject();

                        codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                        String jsonString = valueJsonObj.toJSONString();

                        if (!dbProperty.getPropertyValue().getValue().contentEquals(jsonString)) {
                            //the properties values are different

                            DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                            if(logger.isDebugEnabled()){
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
                }

                if (!found) {
                    //the survey doesn't contain this property, therefore it needs to be added

                    JSONObject valueJsonObj = new JSONObject();

                    codec.encode(valueJsonObj, properties.getPropertyValue(propertyKey));

                    String jsonString = valueJsonObj.toJSONString();

                    DbPropertyValue propertyValue = new DbPropertyValue(jsonString);

                    if(logger.isDebugEnabled()){
                        logger.debug("Adding survey element property of "+propertyValue+" because the survey property didn't exist.");
                    }
                    session.saveOrUpdate(propertyValue);

                    //TODO: A query for an existing property value row with the same value could decrease the table size
                    dbSurveyElement.getProperties().add(new DbSurveyElementProperty(dbSurveyElement, dbPropertyKey, propertyValue));
                }

            } else {

                throw new RuntimeException("The property key '" + propertyKey + "' does not exist in the database.");
            }
        }

        for (DbSurveyElementProperty toRemoveDbProperty : toRemoveDbProperties) {

            dbSurveyElement.getProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }

            session.flush();

        for (DbPropertyKey dbPropertyKey : toAddMap.keySet()) {

            dbSurveyElement.getProperties().add(new DbSurveyElementProperty(dbSurveyElement, dbPropertyKey, toAddMap.get(dbPropertyKey)));
        }
    }

    /**
     * Converts common question properties to the database question properties
     *
     * @param dbQuestion The question these properties are for.
     * @param properties The common question properties. </br>
     * Note: any option list should NOT have their ids set to zero when importing this question (e.g. from a course import) because
     * this method will populate the mapper (if not null) with the original option list ids to the new ids in order to reuse that new id
     * for subsequent instances of that same option list in the import.
     * @param session The database session to work in
     * @param mapper where to maintain information about the ids that have changed during the insert process.
     * 		  Can't be null if performing an import for the question from an external source like a course import.
     */
    private void convertQuestionProperties(DbQuestion dbQuestion, SurveyItemProperties properties, Session session, ExternalSurveyMapper mapper) {

        Set<DbQuestionProperty> toRemoveDbProperties = new HashSet<>(dbQuestion.getQuestionProperties());

        //Need to keep track of newly added option lists so that we don't mistake a new option list for one that was just added
        Map<Integer, Integer> oldOptionListIdToExistingId = new HashMap<>();

        for (SurveyPropertyKeyEnum propertyKey : properties.getKeys()) {

            Serializable value = properties.getPropertyValue(propertyKey);

            DbPropertyKey dbPropertyKey = dbMgr.selectRowByExample(new DbPropertyKey(propertyKey.getName()), DbPropertyKey.class, session);

            if (dbPropertyKey != null) {

                DbOptionList dbOptionListValue = null;

                String stringValue = null;

                if (value instanceof OptionList) {

                    OptionList optionListValue = (OptionList) value;

                    if(logger.isDebugEnabled()){
                        logger.debug("found an instance of option list for question: " + dbQuestion.getText() + ".");
                    }

                    if(logger.isDebugEnabled()){

                        if (optionListValue.getListOptions() != null) {
                            logger.debug("listoptions size = " + optionListValue.getListOptions().size());
                        } else {
                            logger.debug("listoptions is null.");
                        }
                    }

                    dbOptionListValue = oldOptionListIdToExistingId.get(optionListValue.getId()) != null
                    		? (DbOptionList) session.get(DbOptionList.class, oldOptionListIdToExistingId.get(optionListValue.getId()))
                    		: null;

                    /**
                     * whether the option list being converted/created is a shared option list that is already in the db:
                     * 1. the option list is found in the db from the query above because
                     *    this is not the first time this option list was used by this question being converted/created
                     * 2. the option list was found by brute force as a match to an existing option list
                     *    in the db but that existing option list just had a different id in this db
                     */
                    boolean isExistingSharedOptionList = false;

                    /**
                     * whether the option list being converted/created is NOT shared and was just imported as part of
                     * the questions/survey/course being imported based on values in the provided mapper.  Therefore
                     * if the mapper is null this will always be false.
                     */
                    boolean isExistingImportedOptionList = false;

                    // check to see if the incoming shared option list is a duplicate of an existing shared option list
                    if (optionListValue.getIsShared()) {

                    	if(dbOptionListValue != null){
                    		isExistingSharedOptionList = true;

                    	} else {

	                    	DbOptionList exampleOptionList = new DbOptionList();
	                        exampleOptionList.setIsShared(true);

	                        String queryString = "from DbOptionList as optionList where optionList.isShared = "+true;

	                        //get the list of existing option lists so that they can be checked to find one identical to optionListValue
	                        List<DbOptionList> dbOptionLists = dbMgr.selectRowsByQuery(DbOptionList.class, queryString, Surveys.UNUSED_INDEX, Surveys.UNUSED_INDEX, session);

	                        //put the existing option list with same ID as optionListValue at the front to speed things up when the ID has not changed
	                        DbOptionList firstDbOptionListToCheck = (DbOptionList) session.get(DbOptionList.class, optionListValue.getId());
	                        if(firstDbOptionListToCheck != null){
	                        	dbOptionLists.remove(firstDbOptionListToCheck);
	                        	dbOptionLists.add(0, firstDbOptionListToCheck);
	                        }

	                        //check all existing shared option lists to see if one matches optionListValue
	                        for(DbOptionList existingDbOptionList : dbOptionLists){

	                        	if(existingDbOptionList.getIsShared()
	                        			&& existingDbOptionList.getName().equals(optionListValue.getName())
	                        			&& existingDbOptionList.getListOptions().size() == optionListValue.getListOptions().size()){

	                        		// makes sure the existing option list has the correct permissions
	                    			if(!optionListValue.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)
	                    					&& !optionListValue.getVisibleToUserNames().containsAll(dbQuestion.getVisibleToUserNames())) {

	                    				/* Get the existing option list from the database in order to check the visible/editable usernames.
	                    				 * This is necessary because the visible/editable usernames in the variable 'existingDbOptionList'
	                    				 * are always empty. */
	                    				DbOptionList existing = (DbOptionList) session.get(DbOptionList.class, existingDbOptionList.getOptionListId());

	        	                        if(existing != null && !existing.getEditableToUserNames().containsAll(dbQuestion.getEditableToUserNames())) {
	        	                        	// if the existing option list doesn't have the correct permissions, ignore it.
	        	                        	continue;
	        	                        }

	    	                        }

	                    			Map<Integer, Integer> oldListOptionIdToExistingListOptionId = new HashMap<>();

	                    			//convert the existing shared option list's set of list options into a list sorted by sort keys
	                    			List<DbListOption> sortedListOptions = new ArrayList<>(existingDbOptionList.getListOptions());
	                    			Collections.sort(sortedListOptions, new Comparator<DbListOption>(){

										@Override
										public int compare(DbListOption object1,
												DbListOption object2) {

											return Integer.compare(object1.getSortKey(), object2.getSortKey());
										}

	                    			});

	                    			/*
	                    			 * check to make sure that the original shared option list and the existing shared option list have
	                    			 * identical list options in the same order.
	                    			 */
	                    			for(int i = 0; i < optionListValue.getListOptions().size(); i++){

	                    				ListOption originalListOption = optionListValue.getListOptions().get(i);
	                    				DbListOption existingDbListOption = sortedListOptions.get(i);

	                    				if(existingDbListOption.getText().equals(originalListOption.getText())){

	                    					oldListOptionIdToExistingListOptionId.put(originalListOption.getId(), existingDbListOption.getListOptionId());
	                    				}

	                    			}

	                        		if(oldListOptionIdToExistingListOptionId.keySet().size() == optionListValue.getListOptions().size()){

	                        			//if an identical shared option list is found, use it for the question property
	                        			dbOptionListValue = existingDbOptionList;
	                                	oldOptionListIdToExistingId.put(optionListValue.getId(), dbOptionListValue.getOptionListId());

	                                	if(mapper != null && optionListValue.getId() != dbOptionListValue.getOptionListId()){

	                                		// Map the new option list ids to the old option list ids
	            	                    	for(ListOption listOption : optionListValue.getListOptions()){
	            	                            mapper.addSurveyQuestionReply(listOption.getId(), oldListOptionIdToExistingListOptionId.get(listOption.getId()));
	            	                    	}
	                                	}

	                                	isExistingSharedOptionList = true;
	                                	break;
	                        		}
	                    		}
	                        }
                    	}
                    } else if(mapper != null){
                        //this is part of a import from an external source (e.g. course import)

                        if(mapper.surveyQuestionOptionListExists(optionListValue.getId())){
                            //the option list was previously imported and was found in the mapper
                            //i.e. the option list was part of a previous question that was just imported earlier in this method (but not this call to this method)

                            isExistingImportedOptionList = true;

                            dbOptionListValue = (DbOptionList) session.get(DbOptionList.class, mapper.getNewSurveyQuestionOptionListId(optionListValue.getId()));

                        }
                    }

                    if(!isExistingSharedOptionList && !isExistingImportedOptionList) {
						// create a new option list

                    	Map<ListOption, DbListOption> listOptionMap = null;

	                    dbOptionListValue = new DbOptionList();
	                    dbOptionListValue.setListOptions(new HashSet<DbListOption>());

	                    // save the original id of the option list
                        //in order to not create new entries for the same option lists in this db but to reuse
                        //the option list id that was created for this first instance of an option list for subsequent
                        //instances of that option list in the import.
	                    int originalId = optionListValue.getId();

	                    optionListValue.getEditableToUserNames().clear();
	                    optionListValue.getVisibleToUserNames().clear();

	                    dbOptionListValue.getEditableToUserNames().addAll(dbQuestion.getEditableToUserNames());
	                    dbOptionListValue.getVisibleToUserNames().addAll(dbQuestion.getVisibleToUserNames());

	                    if(dbQuestion.getQuestionId() != 0){
	                        //this is an existing question therefore we want the option list to be updated
	                        //not recreated in the db
	                        dbOptionListValue.setOptionListId(optionListValue.getId());
	                    }

	                    // Convert the OptionList into a dbOptionList
	                    listOptionMap = convertOptionList(dbOptionListValue, optionListValue, session, mapper);

	                    if (dbOptionListValue.getListOptions() != null && !dbOptionListValue.getListOptions().isEmpty()) {
	                        if(listOptionMap != null) {
	                            // Save the session to generate new ids
	                            if(logger.isDebugEnabled()){
	                                logger.debug("Saving an option list to the db with new list options.");
	                            }
	                            session.save(dbOptionListValue);

	                            //keep track of option lists that have been created in the db as part of this method
                                //in order to not create new entries for the same option lists in this db but to reuse
                                //the option list id that was created for this first instance of an option list for subsequent
                                //instances of that option list in the import.
	                            if(mapper != null){
	                                mapper.addSurveyQuestionOptionList(originalId, dbOptionListValue.getOptionListId());
	                            }
	                        } else {
	                            // Merge the session to update existing values
	                            if(logger.isDebugEnabled()){
	                                logger.debug("Updating an existing option list to the db with existing list options.");
	                            }
	                            session.merge(dbOptionListValue);
	                        }
	                    } else {
	                        // Allow saving the database an optionlist without any listoptions.
	                        if (dbOptionListValue.getOptionListId() == 0) {
	                            if(logger.isDebugEnabled()){
	                                logger.debug("Saving a new option list to the db with no list options.");
	                            }
	                            // Save the session to generate new ids
                                session.save(dbOptionListValue);

                                //keep track of option lists that have been created in the db as part of this method
                                //in order to not create new entries for the same option lists in this db but to reuse
                                //the option list id that was created for this first instance of an option list for subsequent
                                //instances of that option list in the import.
                                if(mapper != null){
                                    mapper.addSurveyQuestionOptionList(optionListValue.getId(), dbOptionListValue.getOptionListId());
                                }
	                        } else {
	                            if(logger.isDebugEnabled()){
	                                logger.debug("Updating an existing option list to the db with no list options.");
	                            }
	                            session.merge(dbOptionListValue);
	                        }
	                    }


	                    oldOptionListIdToExistingId.put(dbOptionListValue.getOptionListId(), dbOptionListValue.getOptionListId());

	                    if (mapper != null && listOptionMap != null) {

	                    	// Map the new option list ids to the old option list ids
	                    	for(ListOption origListOption : listOptionMap.keySet()){
	                            mapper.addSurveyQuestionReply(origListOption.getId(), listOptionMap.get(origListOption).getListOptionId());
	                    	}
	                    }
                    }else if(isExistingImportedOptionList){

                        // Merge the session to update existing values
                        //in order to not create new entries for the same option lists in this db but to reuse
                        //the option list id that was created for this first instance of an option list for subsequent
                        //instances of that option list in the import.
                        if(logger.isDebugEnabled()){
                            logger.debug("Updating an existing option list to the db with existing list options.");
                        }
                        session.merge(dbOptionListValue);
                    }

                } else {


                    if (value != null) {
                        // Check to make sure the answer weight keys align with the number of options
                        if (dbPropertyKey.getKey().equals(SurveyPropertyKeyEnum.ANSWER_WEIGHTS.getName())) {
                            OptionList rowOptions = (OptionList) properties.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
                            OptionList columnOptions = (OptionList) properties.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
                            if (value instanceof MatrixOfChoicesReplyWeights) {
                                MatrixOfChoicesReplyWeights weights = (MatrixOfChoicesReplyWeights) value;
                                if (rowOptions != null) {
                                    // Remove extraneous rows
                                    if (weights.getReplyWeights().size() > rowOptions.getListOptions().size()) {
                                        int extraneousRows = weights.getReplyWeights().size() - rowOptions.getListOptions().size();
                                        for (int i=0; i<extraneousRows; i++) {
                                            weights.removeRow(rowOptions.getListOptions().size());
                                        }
                                    }
                                }
                                if (columnOptions != null) {
                                    int weightSize = weights.getReplyWeights().size();
                                    if (weightSize > 0 && weights.getReplyWeights().get(0).size() > columnOptions.getListOptions().size()) {
                                        // Truncate weights
                                        int extraneousColumns = weights.getReplyWeights().get(0).size() - columnOptions.getListOptions().size();
                                        for (int j=0; j<extraneousColumns; j++) {
                                            // Remove the column at index size() for each extraneous column
                                            weights.removeColumn(columnOptions.getListOptions().size());
                                        }
                                    }
                                }
                            }
                        }

                        JSONObject valueJsonObj = new JSONObject();

                        codec.encode(valueJsonObj, value);

                        stringValue = valueJsonObj.toJSONString();

                    } else {
                        logger.error("Unable to encode value for property with key: " + propertyKey);
                        throw new RuntimeException("The property with key: " + propertyKey + " has a null value.");
                    }

                }

                boolean found = false;

                for (DbQuestionProperty dbProperty : dbQuestion.getQuestionProperties()) {

                    if(logger.isDebugEnabled()){
                        logger.debug("checking db property key (" + dbProperty.getPropertyKey().getKey()  + ") with: " + dbPropertyKey.getKey() );
                        logger.debug("checking db property dbid (" + dbProperty.getPropertyKey().getId()  + ") with: " + dbPropertyKey.getId() );
                    }
                    if (dbProperty.getPropertyKey() == dbPropertyKey) {

                        DbOptionList oldDbOptionListValue = dbProperty.getPropertyValue().getOptionListValue();

                        if ((stringValue == null ? dbProperty.getPropertyValue().getStringValue() != null : !stringValue.equals(dbProperty.getPropertyValue().getStringValue()))
                                || (dbOptionListValue == null ? oldDbOptionListValue != null
                                : oldDbOptionListValue != null ? oldDbOptionListValue.getOptionListId() != dbOptionListValue.getOptionListId() : true)) {

                        	/* Update properties if:
                        	 * dbProperty's stringValue and current stringValue exist and are not equal
                             * OR if the current optionListId is not equal to the old optionListId
                             */

                            dbProperty.setPropertyValue(new DbQuestionPropertyValue(stringValue, dbOptionListValue));
                        }

                        toRemoveDbProperties.remove(dbProperty);

                        found = true;
                        if(logger.isDebugEnabled()){
                            logger.debug("found = true");
                        }
                    }
                }

                if (!found) {

                    if(logger.isDebugEnabled()){
                        logger.debug("property not found, adding property: key(" + dbPropertyKey + "), value(" + stringValue + ")");
                    }
                    dbQuestion.getQuestionProperties().add(new DbQuestionProperty(dbQuestion, dbPropertyKey, new DbQuestionPropertyValue(stringValue, dbOptionListValue)));
                }

            } else {

                throw new RuntimeException("The property key '" + propertyKey + "' does not exist in the database.");
            }
        }

        for (DbQuestionProperty toRemoveDbProperty : toRemoveDbProperties) {

            if(logger.isDebugEnabled()){
                logger.debug("deleting db property: key(" + toRemoveDbProperty.getPropertyValue() + "), value(" + toRemoveDbProperty.getPropertyValue() + ")");
            }
            dbQuestion.getQuestionProperties().remove(toRemoveDbProperty);

            session.delete(toRemoveDbProperty);
        }
    }

    /**
     * Converts category names into the associated database category object,
     * creating it if it doesn't exist
     *
     * @param dbCategories collection of categories to convert, i.e. make this collection match the entries in categories
     * @param categories The unique names of question categories that will be used to make sure the dbCategories contains the same values.
     * @param visableToUserNames the read permissions for the question being assigned the categories
     * @param editableToUserNames the write permissions for the question being assigned the categories
     * @param session The database session to work in.  Can't be null.
     */
    private void convertCategories(Set<DbCategory> dbCategories, Set<String> categories, Set<String> visableToUserNames, Set<String> editableToUserNames, Session session) {

        Set<DbCategory> toRemoveDbCategories = new HashSet<>(dbCategories);

        for (String categoryName : categories) {

            boolean found = false;

            for (DbCategory dbCategory : dbCategories) {

                if (dbCategory.getName().equals(categoryName)
                		&& (dbCategory.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)
                		|| dbCategory.getVisibleToUserNames().containsAll(visableToUserNames))) {
                	// make sure this category exists and is visible to the user

                    toRemoveDbCategories.remove(dbCategory);

                    found = true;
                }
            }

            if (!found) {

                DbCategory dbCategory = dbMgr.selectRowByExample(new DbCategory(categoryName), DbCategory.class, session);

                if (dbCategory == null) {
                	// if this category doesn't exist, create a new category

                    dbCategory = new DbCategory(categoryName);
                    dbCategory.setVisibleToUserNames(visableToUserNames);
                    dbCategory.setEditableToUserNames(editableToUserNames);

                } else if (!dbCategory.getVisibleToUserNames().containsAll(visableToUserNames)
                	&& !dbCategory.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)) {
                	// if this category exists and the user doesn't have permission to it, give the user permission

                	dbCategory.getVisibleToUserNames().addAll(visableToUserNames);
                    dbCategory.getEditableToUserNames().addAll(editableToUserNames);
                }

                dbCategories.add(dbCategory);
            }
        }

        for (DbCategory dbCategory : toRemoveDbCategories) {

            dbCategories.remove(dbCategory);
        }
    }
    
    /**
     * This is a copy of the {@link #SurveyItemPropertyValueJSON} codec that is used to encode/decode 
     * survey properties. By copying the codec logic, we can leverage the modified {@link #SliderRangeJSONUtil}
     * class created to handle encoding/decoding of the new step size.
     * 
     * @author cpolynice
     */
    protected static class SurveyItemPropertyValueJSONUtil implements JSONCodec {

        private static Logger jsonLogger = LoggerFactory.getLogger(SurveyItemPropertyValueJSONUtil.class);

        /** message attribute names */
        private static final String CLASS = "Property_Class";

        private static final String VALUE = "Property_Value";

        /** mapping of class names to the JSON codec class */
        private static final Map<String, Class<?>> implClassMap = new HashMap<>();

        static {
            implClassMap.put(OptionList.class.getName(), OptionListJSON.class);
            implClassMap.put(String.class.getName(), StringPayloadJSON.class);
            implClassMap.put(SurveyScorer.class.getName(), SurveyScorerJSON.class);
            implClassMap.put(QuestionScorer.class.getName(), QuestionScorerJSON.class);
            implClassMap.put(SliderRange.class.getName(), SliderRangeJSONUtil.class);
            implClassMap.put(MatrixOfChoicesReplyWeights.class.getName(), MatrixOfChoicesReplyWeightsJSON.class);
            implClassMap.put(FreeResponseReplyWeights.class.getName(), FreeResponseReplyWeightsJSON.class);
            implClassMap.put(ArrayList.class.getName(), SurveyItemPropertyValueListJSON.class);
        }

        @SuppressWarnings("deprecation")
        @Override
        public Object decode(JSONObject jsonObj) throws MessageDecodeException {

            try {

                String codecClassName = (String) jsonObj.get(CLASS);

                JSONCodec codec = (JSONCodec) implClassMap.get(codecClassName).newInstance();

                if (codec != null) {

                    JSONObject propertyValueJson = (JSONObject) jsonObj.get(VALUE);

                    if (propertyValueJson != null) {

                        return codec.decode(propertyValueJson);

                    } else {

                        throw new MessageDecodeException(this.getClass().getName(), "No property value");
                    }

                } else {

                    throw new MessageDecodeException(this.getClass().getName(), "No codec to decode class: " + codecClassName);
                }

            } catch (Exception e) {

                jsonLogger.error("Caught exception while creating a question property from " + jsonObj, e);
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public void encode(JSONObject jsonObj, Object payload) throws MessageEncodeException {

            try {
                String className = payload.getClass().getName();

                jsonObj.put(CLASS, className);

                JSONObject valueJson = new JSONObject();

                Class<?> clazz = implClassMap.get(className);

                if (clazz == null) {
                    
                    throw new MessageEncodeException(this.getClass().getName(), "Found unhandled property value class type of " + payload);
                }

                ((JSONCodec) clazz.getDeclaredConstructor().newInstance()).encode(valueJson, payload);

                jsonObj.put(VALUE, valueJson);

            } catch (Exception e) {

                logger.error("Caught exception while encoding " + this.getClass().getName(), e);
                throw new MessageEncodeException(this.getClass().getName(), "There was a problem encoding the property value");
            }
        }     
    }
    
    /**
     * This is a copy of the {@link #SliderRangeJSON} codec that is used to encode/decode 
     * a SliderRange class. By copying the codec logic, we are able to add support for handling
     * additional fields should the survey properties be extended.
     * 
     * @author cpolynice
     */
    protected static class SliderRangeJSONUtil implements JSONCodec {

        private static Logger jsonLogger = LoggerFactory.getLogger(SliderRangeJSONUtil.class);

        private static final String MIN_VALUE = "MIN_VALUE";

        private static final String MAX_VALUE = "MAX_VALUE";
        
        private static final String STEP_SIZE = "STEP_SIZE";
        
        private static final String SCALE_TYPE = "SCALE_TYPE";

        @Override
        public Object decode(JSONObject jsonObj) throws MessageDecodeException {

            double minValue = 0;
            double maxValue = 0;
            
            /* Default value of step size should be 1 since 0 is an illegal value */
            double stepSize = 1;
            
            /* Default scale type value is linear for scale growth */
            String scaleType = "LINEAR";

            try {    
                if (jsonObj.get(MIN_VALUE) != null) {
                    minValue = ((Double) jsonObj.get(MIN_VALUE)).doubleValue();
                }
                
                if (jsonObj.get(MAX_VALUE) != null) {
                    maxValue = ((Double) jsonObj.get(MAX_VALUE)).doubleValue();
                }

                if (jsonObj.get(STEP_SIZE) != null) {
                    stepSize = ((Double) jsonObj.get(STEP_SIZE)).doubleValue();                   
                }
                
                if (jsonObj.get(SCALE_TYPE) != null) {
                    scaleType = (String) jsonObj.get(SCALE_TYPE);                   
                }
                
                SliderRange sliderRange = new SliderRange(minValue, maxValue);
                sliderRange.setStepSize(stepSize);
                sliderRange.setScaleType(ScaleType.valueOf(scaleType));
                return sliderRange;
            } catch (Exception e) {
                jsonLogger.error("Caught exception while creating slider range from " + jsonObj, e);
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void encode(JSONObject jsonObj, Object payload) throws MessageEncodeException {

            SliderRange bounds = (SliderRange) payload;

            jsonObj.put(MIN_VALUE, bounds.getMinValue());
            jsonObj.put(MAX_VALUE, bounds.getMaxValue());
            jsonObj.put(STEP_SIZE, bounds.getStepSize());   
            
            if (bounds.getScaleType() != null) {
                jsonObj.put(SCALE_TYPE, bounds.getScaleType().name());
            }  
        }
    }
}
