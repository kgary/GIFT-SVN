/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.xml.ws.WebServiceException;

import org.hibernate.Session;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyJSON;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.SurveysWrapperForTests;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyPage;

/**
 * A jUnit test for SurveyUtil.java
 * 
 * @author bzahid
 *
 */
public class SurveyUtilTest {

    /** Stores data returned by the functions in SurveyUtil.java*/
    int surveyContextId;
    List<SurveyContext> sc_list;
    List<SurveyContext> dsc_list;
    List<SurveyContextSurvey> scs_list;
    List<SurveyContextSurvey> dsc_set;
    
    private static Session session;

    @BeforeClass
    public static void connect() {
        
        try {
            UMSDatabaseManager.getInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Please connect to the survey database"
                    + " before continuing tests");
        }
    }
    
    @Before
    public void begin() {
        session = UMSDatabaseManager.getInstance().getCurrentSession();
        session.beginTransaction();
    }
    
    @After
    public void cleanup() {
        if (session != null && session.isOpen()) {
            session.getTransaction().rollback();
        }
    }
    
    @Test
    public void getHibernateSurveyContextsTest() {
        
        try {
            dsc_list = Surveys.getSurveyContexts(null);
        } catch (WebServiceException wse) {
            System.out.println("Unable to establish connection to AutoTutorWebService");
            wse.printStackTrace();
        }
        
        assertNotNull("getHibernateSurveyContexts() returned a null list", dsc_list);
        System.out.println("\nHibernate Survey Contexts:");
        
        /* Prints "-empty" if the list is empty */
        if(dsc_list.isEmpty()) {
            System.out.println("- empty");
        }
        
        /* Prints the list */
        for(SurveyContext s : dsc_list) {
            System.out.println(" "+ s);
        }
    }

    @Test
    public void getGiftSurveyContextsTest() {
        
        try {
            sc_list = Surveys.getSurveyContexts(null);
        } catch (WebServiceException wse) {
            System.out.println("Unable to establish connection to AutoTutorWebService");
            wse.printStackTrace();
        }
    
        assertNotNull("getGiftSurveyContexts() returned a null list", sc_list);
        System.out.println("\nGIFT Survey Contexts:");
        
        /* Prints "-empty" if the list is empty */
        if(sc_list.isEmpty()) {
            System.out.println("- empty");
        }
        
        for(SurveyContext s : sc_list) {
            System.out.println("- " + s);
        }
    }
    
    @Test
    public void getHibernateSurveyContextKeysTest() {
        
        surveyContextId = 12;
        
        try {       
            dsc_set = Surveys.getSurveyContextSurveys(surveyContextId);
        } catch (WebServiceException wse) {
            System.out.println("Unable to establish connection to AutoTutorWebService");
            wse.printStackTrace();
        }
        
        System.out.println("\nGIFT Survey Contexts (Key = " + surveyContextId + "):");
        
        /* Assertion fails if key does not exist */
        assertNotNull("Survey Context ID does not exist.", dsc_set);
    
        System.out.println("\nHibernate Survey Contexts by Key:");
        
        /* Prints "-empty" if the set is empty */
        if(dsc_set.isEmpty()) {
            System.out.println("- empty");
        }
        
        /* Prints the set */
        System.out.println(dsc_set.toString());
    }
    
    @Test
    public void getGiftSurveyContextKeysTest() {
        
        surveyContextId = 1;
        scs_list = Surveys.getSurveyContextSurveys(surveyContextId);
        
        System.out.println("\nGIFT Survey Contexts (Key = " + surveyContextId + "):");
        
        /* Assertion fails if surveyContextID key does not exist */
        assertNotNull("Survey Context ID does not exist.", scs_list);
        
        /* Prints "-empty" if the list is empty */
        if(scs_list.isEmpty()) {
            System.out.println("- empty");
        }
        
        /* Prints the list */
        for(SurveyContextSurvey s : scs_list) {
            System.out.println("- " + s);
        }
    }
    
    @Test
    public void insertAndDeleteSurvey() {

        Survey surveyFromFile = createSurveyFromFile();
        assertNotNull("Survey did not import correctly.", surveyFromFile);

        // This test is assuming that the user permission in the survey is the same throughout all the survey's children objects.
        assertFalse("Survey doesn't have editable permissions.", surveyFromFile.getEditableToUserNames().isEmpty());
        String username = surveyFromFile.getEditableToUserNames().iterator().next();
        
        boolean madeShared = false;
        // make first multiple choice option list "Yes No shared". This list should not be deleted when
        // deleting the survey later on.
        for (AbstractSurveyElement element : surveyFromFile.getPages().get(0).getElements()) {
            if (element instanceof AbstractSurveyQuestion) {
                AbstractQuestion question = ((AbstractSurveyQuestion<?>) element).getQuestion();
                if (question instanceof MultipleChoiceQuestion) {
                    OptionList optionList = (OptionList) question.getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                    optionList.setIsShared(true);
                    optionList.setName("Yes No");
                    optionList.getListOptions().clear();
                    optionList.getListOptions().add(new ListOption(0, "Yes"));
                    optionList.getListOptions().add(new ListOption(0, "No"));
                    question.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
                    madeShared = true;
                    break;
                }
            }
        }

        // ensure for proper testing that an option list got marked as 'shared'
        assertTrue("Did not mark any option lists as shared.", madeShared);

        // ******************************
        // **********  INSERT  **********
        // ******************************

        DbSurvey insertedSurvey = null;
        try {
            insertedSurvey = Surveys.insertExternalSurvey(surveyFromFile, null, session);
        } catch (Exception e) {
            fail("Error inserting the survey from file. Reason: "+e.getMessage());
        }
        
        // test that the inserted survey has the same number of pages and properties as the original
        assertNotNull("Survey did not insert into the database correctly.", insertedSurvey);
        assertEquals("Survey page count is not the same from file.", surveyFromFile.getPageCount(), insertedSurvey.getSurveyPages().size());
        assertEquals("Survey property count is not the same from file.", surveyFromFile.getProperties().getPropertyCount(), insertedSurvey.getProperties().size());

        Iterator<DbSurveyPage> insertedSurveyPageItr = insertedSurvey.getSurveyPages().iterator();
        
        // test that the inserted survey has the same number of elements per page as the original
        for (int i = 0; i < surveyFromFile.getPageCount(); i++) {
            DbSurveyPage page = insertedSurveyPageItr.next();
            assertEquals("Survey page element count is not the same from file.", surveyFromFile.getPages().get(i).getElements().size(), page.getSurveyElements().size());
        }
        
        // load the ids from the db survey into the survey from the file
        loadIds(surveyFromFile, insertedSurvey);
        
        // find shared option list id
        Integer sharedOptionListId = null;
        for (AbstractSurveyElement element : surveyFromFile.getPages().get(0).getElements()) {
            if (element instanceof AbstractSurveyQuestion) {
                AbstractQuestion question = ((AbstractSurveyQuestion<?>) element).getQuestion();
                if (question instanceof MultipleChoiceQuestion) {
                    OptionList optionList = (OptionList) question.getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                    if (optionList != null && optionList.getIsShared()) {
                        sharedOptionListId = optionList.getId();
                        break;
                    }
                }
            }
        }
        
        // ensure for proper testing that an option list got marked as 'shared'
        assertNotNull("Did not find the shared option list.", sharedOptionListId);

        // test that we can retrieve the inserted survey
        DbSurvey dbSurvey = SurveysWrapperForTests.getDbSurvey(surveyFromFile.getId(), session);
        assertNotNull("Could not retrieve survey from the database.", dbSurvey);
        
        Iterator<DbSurveyPage> dbSurveyPageItr = dbSurvey.getSurveyPages().iterator();
        
        // test that the retrieved survey contains the same number of pages and properties as the original
        assertEquals("Survey page count is not the same from the database.", surveyFromFile.getPageCount(), dbSurvey.getSurveyPages().size());
        assertEquals("Survey property count is not the same from the database.", surveyFromFile.getProperties().getPropertyCount(),
                dbSurvey.getProperties().size());

        for (int i = 0; i < surveyFromFile.getPageCount(); i++) {

            List<AbstractSurveyElement> fileElements = surveyFromFile.getPages().get(i).getElements();

            // test that the retrieved survey contains the same number of elements per page as the original
            assertEquals("Survey page element count is not the same from the database.", fileElements.size(), dbSurveyPageItr.next().getSurveyElements().size());
            for (AbstractSurveyElement element : fileElements) {

                if (element instanceof TextSurveyElement) {
                    TextSurveyElement textElem = (TextSurveyElement) element;
                    DbSurveyElement dbTextElem = SurveysWrapperForTests.getDbSurveyQuestion(textElem.getId(), session);

                    // test that the survey element can be retrieved and that it contains the same number of properties as the original
                    assertNotNull("Could not retrieve the text survey element from the database.", dbTextElem);
                    assertEquals("Text Element's property count is not the same.", textElem.getProperties().getPropertyCount(), dbTextElem.getProperties().size());
                } else if (element instanceof AbstractSurveyQuestion) {
                    AbstractSurveyQuestion<?> questionElem = (AbstractSurveyQuestion<?>) element;
                    DbSurveyElement dbQuestionElem = SurveysWrapperForTests.getDbSurveyQuestion(questionElem.getId(), session);

                    // test that the survey element can be retrieved and that it contains the same number of properties as the original
                    assertNotNull("Could not retrieve survey question from the database", dbQuestionElem);
                    assertEquals("SurveyQuestion's property count is not the same.", questionElem.getProperties().getPropertyCount(), dbQuestionElem.getProperties().size());

                    AbstractQuestion question = questionElem.getQuestion();
                    DbQuestion dbQuestion = SurveysWrapperForTests.getDbQuestion(question.getQuestionId(), session);

                    // test that the question can be retrieved and that it contains the same number of properties as the original
                    assertNotNull("Could not retrieve question from the database", dbQuestion);
                    assertEquals("Question's property count is not the same.", question.getProperties().getPropertyCount(), dbQuestion.getQuestionProperties().size());

                    // check if the question has an option list
                    if (question.getProperties().hasProperty(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)) {
                        OptionList optionList = (OptionList) question.getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                        DbOptionList dbOptionList = SurveysWrapperForTests.getDbOptionList(optionList.getId(), session);

                        // test that the option list can be retrieved
                        assertNotNull("Could not retrieve optionList from the database", dbOptionList);

                        // test if this option list is the one we made 'shared' in the beginning of the test
                        if (optionList.getId() == sharedOptionListId) {
                            assertTrue("Option List wasn't shared when it is supposed to be.", optionList.getIsShared());
                        }

                        // test that the retrieved option list maintains the original list's 'shared' value
                        assertEquals("Option list didn't maintain the 'shared' value.", optionList.getIsShared(), dbOptionList.getIsShared());

                        // test that the retrieved option list has the same number of options as the original
                        assertEquals("Option list size is different.", optionList.getListOptions().size(), dbOptionList.getListOptions().size());
                    }
                } else {
                    fail("Unknown page element type.");
                }
            }
        }
            
        // ******************************
        // **********  DELETE  **********
        // ******************************

        try {
            // perform delete
            Surveys.deleteSurvey(surveyFromFile.getId(), username, session, null);
        } catch (Exception e) {
            fail("Error deleting the survey. Reason: "+e.getMessage());
        }
        
        // test that the inserted survey was deleted
        assertNull("Survey was supposed to have been deleted.", SurveysWrapperForTests.getDbSurvey(surveyFromFile.getId(), session));
        
        // test that the survey properties were deleted
        assertTrue("Survey properties were supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyPropertiesById(surveyFromFile.getId(), session).isEmpty());

        for (int i = 0; i < surveyFromFile.getPageCount(); i++) {

            // test that the survey page was deleted
            assertNull("Survey page was supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyPage(surveyFromFile.getPages().get(i).getId(), session));

            // test that the survey pages properties were deleted
            assertTrue("Survey page properties were supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyPagePropertiesById(surveyFromFile.getId(), session).isEmpty());

            for (AbstractSurveyElement element : surveyFromFile.getPages().get(i).getElements()) {
                if (element instanceof TextSurveyElement) {
                    TextSurveyElement textElem = (TextSurveyElement) element;
                    
                    // test that the survey element was deleted.
                    assertNull("TextSurveyElement was supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyQuestion(textElem.getId(), session));
                    
                    // test that the survey element properties were deleted.
                    assertTrue("TextSurveyElement's properties were supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyElementPropertiesByQuestionId(textElem.getId(), session).isEmpty());
                } else if (element instanceof AbstractSurveyQuestion) {
                    AbstractSurveyQuestion<?> questionElem = (AbstractSurveyQuestion<?>) element;
                    
                    // test that the survey element was deleted.
                    assertNull("SurveyQuestion was supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyQuestion(questionElem.getId(), session));

                    // test that the survey element properties were deleted.
                    assertTrue("SurveyQuestion's properties were supposed to have been deleted.", SurveysWrapperForTests.getDbSurveyElementPropertiesByQuestionId(questionElem.getId(), session).isEmpty());

                    AbstractQuestion question = questionElem.getQuestion();
                    
                    // test that the question was deleted
                    assertNull("Question was supposed to have been deleted.", SurveysWrapperForTests.getDbQuestion(question.getQuestionId(), session));
                    
                    // test that the question properties were deleted.
                    assertTrue("Question's properties were supposed to have been deleted.", SurveysWrapperForTests.getDbQuestionPropertiesById(question.getQuestionId(), session).isEmpty());

                    // check if the question has an option list
                    for (SurveyPropertyKeyEnum propertyKey : question.getProperties().getKeys()) {
                        Serializable propValue = question.getProperties().getPropertyValue(propertyKey);
                        if (propValue instanceof OptionList) {
                            OptionList optionList = (OptionList) propValue;
                            
                            if (optionList.getIsShared()) {
                                // test that the option list was not deleted.
                                assertNotNull("OptionList is shared so it should not have been deleted.", SurveysWrapperForTests.getDbOptionList(optionList.getId(), session));

                                // test that the list options were not deleted.
                                assertFalse("ListOptions are part of a shared OptionList so it should not have been deleted.", SurveysWrapperForTests.getDbListOptionsByOptionListId(optionList.getId(), session).isEmpty());
                            } else {

                                // test that the option list was deleted.
                                assertNull("OptionList was supposed to have been deleted.", SurveysWrapperForTests.getDbOptionList(optionList.getId(), session));

                                // test that the list options were deleted.
                                assertTrue("ListOptions were supposed to have been deleted.", SurveysWrapperForTests.getDbListOptionsByOptionListId(optionList.getId(), session).isEmpty());
                            }
                        }
                    }
                } else {
                    fail("Unknown page element type.");
                }
            }
        }
    }
    
    /**
     * Creates a survey object from the data\tests\survey.json file.
     * 
     * @return the imported survey
     */
    private Survey createSurveyFromFile() {
        File testFile = new File("data" + File.separator + "tests" + File.separator + "survey.json");
        Survey survey = null;
        try (FileReader reader = new FileReader(testFile)) {
            try(Scanner scanner = new Scanner(reader)) {
                StringBuilder surveyJson = new StringBuilder();
                while(scanner.hasNext()) {
                    surveyJson.append(scanner.nextLine());
                }
                JSONObject obj = (JSONObject) new JSONParser().parse(surveyJson.toString());
                SurveyJSON codec = new SurveyJSON();
                survey = (Survey) codec.decode(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return survey;
    }
    
    /**
     * Loads the ids from the database survey elements to the survey.
     * 
     * @param survey the survey that needs to populate it's elements' ids
     * @param dbSurvey the survey from the database that contains ids for all the elements.
     */
    private void loadIds (Survey survey, DbSurvey dbSurvey) {
        survey.setId(dbSurvey.getSurveyId());
        
        // need to sort because hash sets don't maintain order
        List<DbSurveyPage> sortedPages = new ArrayList<DbSurveyPage>(dbSurvey.getSurveyPages());
        Collections.sort(sortedPages, new Comparator<DbSurveyPage>() {

            @Override
            public int compare(DbSurveyPage o1, DbSurveyPage o2) {
                return o1.getPageNumber() - o2.getPageNumber();
            }
        });
        
        assertEquals("Page lists are different sizes.", survey.getPageCount(), sortedPages.size());
        for (int pageIndex = 0; pageIndex < survey.getPages().size(); pageIndex++) {
            SurveyPage page = survey.getPages().get(pageIndex);
            DbSurveyPage dbPage = sortedPages.get(pageIndex);
            
            page.setId(dbPage.getSurveyPageId());
            
            // need to sort because hash sets don't maintain order
            List<DbSurveyElement> sortedSurveyElements = new ArrayList<DbSurveyElement>(dbPage.getSurveyElements());
            Collections.sort(sortedSurveyElements, new Comparator<DbSurveyElement>() {

                @Override
                public int compare(DbSurveyElement o1, DbSurveyElement o2) {
                    return o1.getElementNumber() - o2.getElementNumber();
                }
            });
                        
            assertEquals("Element lists are different sizes.", page.getElements().size(), sortedSurveyElements.size());
            for (int elementIndex = 0; elementIndex < page.getElements().size(); elementIndex++) {
                AbstractSurveyElement element = page.getElements().get(elementIndex);
                DbSurveyElement dbElement = sortedSurveyElements.get(elementIndex);
                
                element.setId(dbElement.getSurveyElementId());
                
                if (element instanceof AbstractSurveyQuestion) {
                    AbstractQuestion question = ((AbstractSurveyQuestion<?>)element).getQuestion();
                    question.setQuestionId(dbElement.getQuestionId());
                    
                    List<OptionList> optionLists = Surveys.getOptionLists(question);
                    List<DbOptionList> dbOptionLists = SurveysWrapperForTests.getDbOptionList(dbElement, session);
                    
                    assertEquals("Error setting Ids becuase the option lists are different sizes.", optionLists.size(), dbOptionLists.size());
                    
                    for (int i = 0; i < optionLists.size(); i++) {
                        optionLists.get(i).setId(dbOptionLists.get(i).getOptionListId());
                    }
                }
            }
        }
    }
}
