/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.survey;

import java.util.List;

import org.hibernate.Session;

import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyElementProperty;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyPageProperty;
import mil.arl.gift.ums.db.table.DbSurveyProperty;

/**
 * Wrapper for the Surveys.java class so that we can access the protected methods for our Junit
 * tests.
 * 
 * @author sharrison
 */
public class SurveysWrapperForTests extends Surveys {

    /**
     * Get the db survey with the unique id
     * 
     * @param surveyId the key to finding the survey in the database
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return the db survey object with the given id, null if not found
     */
    public static DbSurvey getDbSurvey(int surveyId, Session session) {
        return Surveys.getDbSurvey(surveyId, session);
    }

    /**
     * Get the db survey question (a question in a survey, not a direct reference to a survey) with
     * the unique id
     * 
     * @param surveyQuestionId - the key to finding the survey question in the database
     * @return DbSurveyElement - the survey question with the given id, null if not found
     */
    public static DbSurveyElement getDbSurveyQuestion(int surveyQuestionId, Session session) {
        return Surveys.getDbSurveyQuestion(surveyQuestionId, session);
    }

    /**
     * Get the db question with the unique id
     * 
     * @param questionId - the key to finding the question in the database
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return DbQuestion - the question with the given id, null if not found
     */
    public static DbQuestion getDbQuestion(int questionId, Session session) {
        return Surveys.getDbQuestion(questionId, session);
    }

    /**
     * Gets an option list from the database
     *
     * @param optionListId The ID of the option list to get
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return DbOptionList The option list with the specified ID, null if there was an error
     */
    public static DbOptionList getDbOptionList(int optionListId, Session session) {
        return Surveys.getDbOptionList(optionListId, session);
    }

    /**
     * Gets the db option list for the survey question
     * 
     * @param surveyQuestion The multiple choice or rating scale question
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return DbOptionList The reply options, null if the question type is incorrect or there has
     *         been an error
     */
    public static List<DbOptionList> getDbOptionList(DbSurveyElement surveyQuestion, Session session) {
        return Surveys.getDbOptionList(surveyQuestion, session);
    }

    /**
     * Return the survey properties of the specified survey
     *
     * @param surveyId The survey id of the property
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return List<SurveyProperty> The survey properties from the database
     */
    public static List<DbSurveyProperty> getDbSurveyPropertiesById(int surveyId, Session session) {
        return Surveys.getDbSurveyPropertiesById(surveyId, session);
    }

    /**
     * Get the db survey page with the unique id
     * 
     * @param surveyPageId the key to finding the survey page in the database
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return the db survey object with the given id, null if not found
     */
    public static DbSurveyPage getDbSurveyPage(int surveyPageId, Session session) {
        return Surveys.getDbSurveyPage(surveyPageId, session);
    }

    /**
     * Return the survey page properties of the specified survey page
     *
     * @param surveyPageId The survey page Id of the property
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return List<SurveyPageProperty> The survey page properties from the database
     */
    public static List<DbSurveyPageProperty> getDbSurveyPagePropertiesById(int surveyPageId, Session session) {
        return Surveys.getDbSurveyPagePropertiesById(surveyPageId, session);
    }

    /**
     * Return the survey question properties of the specified survey question
     *
     * @param surveyElementId The survey question id of the property
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return List<DbSurveyElementProperty> The survey question properties from the database
     */
    public static List<DbSurveyElementProperty> getDbSurveyElementPropertiesByQuestionId(int surveyElementId, Session session) {
        return Surveys.getDbSurveyElementPropertiesByQuestionId(surveyElementId, session);
    }

    /**
     * Return the question properties of the specified question
     *
     * @param questionId The question id of the properties
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return List<QuestionProperty> The question properties from the database
     */
    public static List<DbQuestionProperty> getDbQuestionPropertiesById(int questionId, Session session) {
        return Surveys.getDbQuestionPropertiesById(questionId, session);
    }

    /**
     * Return the list options of the specified option list
     *
     * @param optionListId The ID of the option list to get list options for
     * @param session a database session with a transaction (that has already began) to do all the
     *            insert operations in. This is useful for doing rollback operations if any one of
     *            the inserts fails. Can be null if the caller doesn't care about partial fails and
     *            wants this class to manage the session.
     * @return List<ListOption> The list options from the database
     */
    public static List<DbListOption> getDbListOptionsByOptionListId(int optionListId, Session session) {
        return Surveys.getDbListOptionsByOptionListId(optionListId, session);
    }
}
