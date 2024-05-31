/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import generated.conversation.Conversation;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeAction;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeActions;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.ConversationUtil;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This will test a conversation tree.  It will start the conversation, check the tutor conversation text
 * that would normally be presented to the learner, make a choice in the conversation when a question node
 * is reached and finally check the assessment of a choice made in the conversation.
 * 
 * @author mhoffman
 *
 */
public class ConversationTreeTest {
    
    /** the conversation file to use for testing purposes */
    private static File CONVERSATION_FILE = new File("data"+File.separator+"tests"+File.separator+"evenNumbers.conversationTree.xml");
    
    /**
     * conversation elements from the conversation file to check for in this test
     */
    private static String FIRST_TUTOR_MSG = "Welcome to the conversation!";
    private static String SECOND_TUTOR_MSG = "Please choose a path";
    private static String SECOND_MSG_CHOICE_ONE = "Alpha";
    private static String SECOND_MSG_CHOICE_TWO = "Bravo";
    private static String THIRD_TUTOR_MSG = "Which is an even number?";
    private static String THIRD_MSG_CHOICE_ONE = "4";
    private static String THIRD_MSG_CHOICE_TWO = "7";
    private static String ASSESSMENT_CONCEPT = "even numbers";
    private static AssessmentLevelEnum CONCEPT_ASSESSMENT_VALUE = AssessmentLevelEnum.BELOW_EXPECTATION;
    private static float CONCEPT_ASSESSMENT_CONFIDENCE_VALUE = 0.9f;
    
    private generated.conversation.Conversation conversation;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {        

        UnmarshalledFile uFile = AbstractSchemaHandler.parseAndValidate(new FileInputStream(CONVERSATION_FILE), FileType.CONVERSATION, true);
        conversation = 
                (Conversation)uFile.getUnmarshalled();

    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * This will test converting a conversation object to JSON and then back to a conversation object.
     * This is what happens during load and save operations as the GAT UI for the conversation tree understands
     * JSON and not the generated conversation tree classes.
     */
    @Test
    public void conversionTest() {
        
        //convert to JSON object
        JSONObject jsonObj = null;
        try{
            jsonObj = ConversationUtil.toJSON(conversation);
        }catch(Exception e){
            e.printStackTrace();
            Assert.fail("Failed to convert conversation object to JSON object.");
        }
        
        //convert back to generated object
        @SuppressWarnings("unused")
        generated.conversation.Conversation conversation2;
        try{
            conversation2 = ConversationUtil.fromJSON(jsonObj);
        }catch(Exception e){
            e.printStackTrace();
            Assert.fail("Failed to convert conversation JSON object to conversation object.");
        }
        
        //validate new generated object
        try{
            FileProxy conversationFileProxy = new FileProxy(CONVERSATION_FILE);
            ConversationTreeFileHandler handler = new ConversationTreeFileHandler(conversationFileProxy, true);
            GIFTValidationResults validationResults = handler.checkConversation();
            if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
                throw validationResults.getFirstError();
            }
        }catch(Throwable e){
            e.printStackTrace();
            Assert.fail("Failed to validate the converted conversation object.");
        }
    }
    
    /**
     * This test will attempt to walk through a conversation including making choices
     * at question nodes and checking the concept assessments for those choices.
     */
    @Test
    public void previewTest(){
        
        ConversationManager conversationMgr = new ConversationManager();
        
        //just generate a new chat id to use here
        int chatId = DisplayChatWindowRequest.getNextChatId();
        
        //handles conversation assessments from choices made in the conversation
        ConversationAssessmentHandlerInterface conversationAssessmentHandler = new ConversationAssessmentHandlerInterface() {
            
            @Override
            public void assessPerformanceFromConversation(
                    List<ConversationAssessment> assessments) {

                Assert.assertTrue("Found incorrect number of assessments in the current part of the conversation.  Expected 1 but found "+assessments.size()+".", assessments.size() == 1);
                
                ConversationAssessment assessment = assessments.get(0);
                Assert.assertTrue("Found incorrect concept in assessment.  Expected '"+ASSESSMENT_CONCEPT+"' but found '"+assessment.getConcept()+"'.", assessment.getConcept().equals(ASSESSMENT_CONCEPT));
                Assert.assertTrue("Found incorrect concept assessment value in assessment.  Expected '"+CONCEPT_ASSESSMENT_VALUE+"' but found '"+assessment.getAssessmentLevel()+"'.", (assessment.getAssessmentLevel() == CONCEPT_ASSESSMENT_VALUE));
                Assert.assertTrue("Found incorrect concept assessment confidence value in assessment.  Expected "+CONCEPT_ASSESSMENT_CONFIDENCE_VALUE+" but found "+assessment.getConfidence()+".", (assessment.getConfidence() == CONCEPT_ASSESSMENT_CONFIDENCE_VALUE));
            }
        };
        
        //for this junit this is needed to check the tutor conversation text that would normally be presented
        //to the learner 
        DomainKnowledgeActionInterface domainKnowledgeActionHandler = new DomainKnowledgeActionInterface() {
            
            boolean checkedFirstInfo = false, checkedSecondInfo = false;
            
            @Override
            public void scenarioStarted() {
                //nothing to do
            }
            
            @Override
            public void scenarioCompleted(LessonCompleted lessonCompleted) {
                //nothing to do                
            }
            
            @Override
            public void performanceAssessmentCreated(
                    PerformanceAssessment performanceAssessment) {
                //nothing to do
            }
            
            @Override
            public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
                //nothing to do                
            }
            
            @Override
            public void fatalError(String reason, String details) {
                //nothing to do                
            }
            
            @Override
            public void handleDomainActionWithLearner(DomainAssessmentContent information) {
                
                if(information instanceof ConversationTreeActions){
                    
                    ConversationTreeActions actions = (ConversationTreeActions)information;
                    
                    if(!checkedFirstInfo){
                        checkFirstConversationElement(actions);
                        checkedFirstInfo = true;
                    }else if(!checkedSecondInfo){
                        checkSecondConversationElement(actions);
                        checkedSecondInfo = true;
                    }
                    
                }else{
                    Assert.fail("Received unhandled domain assessment content to display to the user in "+information+".");
                }
            }
            
            private void checkSecondConversationElement(ConversationTreeActions actions){
                
                //check message
                ConversationTreeAction action = actions.getActions().get(0);
                Assert.assertTrue("Received incorrect third conversation element.  Expected '"+THIRD_TUTOR_MSG+"', found '"+action.getText()+"'.", action.getText().equals(THIRD_TUTOR_MSG));
                
                //check second tutor message choices
                List<String> choices = action.getChoices();
                Assert.assertTrue("Found no choices for question '"+THIRD_TUTOR_MSG+"'.", choices != null);
                Assert.assertTrue("Expected 2 choices for question '"+THIRD_TUTOR_MSG+"' but found "+choices.size(), choices.size() == 2);
                    
                String choice1 = choices.get(0);
                Assert.assertTrue("Expected the first choice for the question '"+action.getText()+"' to be '"+THIRD_MSG_CHOICE_ONE+"' but found '"+choice1+"'", choice1.equals(THIRD_MSG_CHOICE_ONE));
                
                String choice2 = choices.get(1);
                Assert.assertTrue("Expected the second choice for the question '"+action.getText()+"' to be '"+THIRD_MSG_CHOICE_TWO+"' but found '"+choice2+"'", choice2.equals(THIRD_MSG_CHOICE_TWO));
            }
            
            private void checkFirstConversationElement(ConversationTreeActions actions){
                
                //check first tutor message
                ConversationTreeAction action1 = actions.getActions().get(0);
                Assert.assertTrue("Received incorrect first conversation element.  Expected '"+FIRST_TUTOR_MSG+"', found '"+action1.getText()+"'.", action1.getText().equals(FIRST_TUTOR_MSG));
                
                //check second tutor message
                ConversationTreeAction action2 = actions.getActions().get(1);
                Assert.assertTrue("Received incorrect second conversation element.  Expected '"+SECOND_TUTOR_MSG+"', found '"+action2.getText()+"'.", action2.getText().equals(SECOND_TUTOR_MSG));
                
                //check second tutor message choices
                List<String> choices = action2.getChoices();
                Assert.assertTrue("Found no choices for question '"+SECOND_TUTOR_MSG+"'.", choices != null);
                Assert.assertTrue("Expected 2 choices for question '"+SECOND_TUTOR_MSG+"' but found "+choices.size(), choices.size() == 2);
                    
                String choice1 = choices.get(0);
                Assert.assertTrue("Expected the first choice for the question '"+action2.getText()+"' to be '"+SECOND_MSG_CHOICE_ONE+"' but found '"+choice1+"'", choice1.equals(SECOND_MSG_CHOICE_ONE));
                
                String choice2 = choices.get(1);
                Assert.assertTrue("Expected the second choice for the question '"+action2.getText()+"' to be '"+SECOND_MSG_CHOICE_TWO+"' but found '"+choice2+"'", choice2.equals(SECOND_MSG_CHOICE_TWO));
            }
            
            @Override
            public void displayDuringLessonSurvey(
                    AbstractSurveyLessonAssessment surveyAssessment,
                    SurveyResultListener surveyResultListener) {
                //nothing to do                
            }
        };
        
        //will hold onto this calling thread until the choice conversation node elements are reached 
        conversationMgr.startConversationTree(chatId, conversation, conversationAssessmentHandler, domainKnowledgeActionHandler);
        
        //send a learner choice, this will not create a performance assessment to happen
        List<String> userEntries = new ArrayList<>();
        userEntries.add(SECOND_MSG_CHOICE_ONE);
        ChatLog chatLogResponse1 = new ChatLog(chatId, new ArrayList<String>(0), userEntries);
        
        //will hold onto this calling thread until the choice conversation node elements are reached
        conversationMgr.addUserResponse(chatLogResponse1);
        
        //send a learner choice, this should cause a performance assessment to happen
        userEntries = new ArrayList<>();
        userEntries.add(THIRD_MSG_CHOICE_TWO);
        ChatLog chatLogResponse2 = new ChatLog(chatId, new ArrayList<String>(0), userEntries);
        
        //will hold onto this calling thread until the choice conversation node elements are reached
        conversationMgr.addUserResponse(chatLogResponse2);
        
        //end conversation
        conversationMgr.stopConversation(chatId);
    }

}
