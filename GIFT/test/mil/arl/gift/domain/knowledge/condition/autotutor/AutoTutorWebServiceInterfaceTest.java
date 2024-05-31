/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.autotutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A jUnit test for the AutoTutorWebServiceInterfaceTest
 *
 * @author jleonard
 */
public class AutoTutorWebServiceInterfaceTest {
    
    String COIN_URL = "https://dsspp.gifttutoring.org/retrieve?json={\"guid\":\"f0890bb9-c809-45f5-9db7-8428e267674\",\"return\":\"scriptContent\"}";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Ignore("This test involves human intervention as it tests dialog interaction with AutoTutor.")  
    @Test
    public void testAutoTutorService() {

        System.out.println("Testing AutoTutor webservice at " + new Date() + "...\n");

        //
        // Establish the connection
        //
        AutoTutorWebServiceInterface atws = null;
        do {
            try {
                atws = new AutoTutorWebServiceInterface();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Caught exception while trying to establish connection to AT WS.  Trying again...");
            }

        } while (atws == null);

        System.out.println("Established connection at " + new Date());


        //
        // Initialize the script
        //
        String userName = "gift";
        AutoTutorWebServiceInterface.InitScriptResponse initResponse = null;
        do {
            try {
                atws.initScriptByURL(COIN_URL, userName);
                initResponse = atws.getInitScriptResponse();
                System.out.println("Init Script for user " + userName + " for script " + COIN_URL + " returned session id of = " + initResponse.getSessionId());
            } catch (Exception e) {
                System.out.println("Caught exception while trying to init script on AT WS.  Trying again...");
                e.printStackTrace();
            }

        } while (initResponse == null);

        try {

            //
            // Get the initial (pre-user text) actions
            //
            String input;
            AutoTutorWebServiceInterface.ActionsResponse actionResponse = null;
            do {
                try {
                    actionResponse = atws.getInitialActions();
                } catch (Exception e) {
                    System.out.println("Caught exception while trying to get actions for initial question with AT WS.  Trying again...");
                    e.printStackTrace();
                }

            } while (actionResponse == null);

            do {
                System.out.println("Speach =\n" + actionResponse.getSpeakAsString());
                System.out.println("Text =\n" + actionResponse.getDisplayTextAsString());

                //when session has ended, bail out of loop
                if (actionResponse.hasEnded()) {
                    System.out.println("Session has reached the end.");
                    break;
                }

                //
                // User provides text
                //
                do {
                    System.out.println("Answer: ");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    input = br.readLine();
                } while (input == null || input.length() == 0);

                //
                // Assess user's text
                //
                AutoTutorWebServiceInterface.AssessmentResponse assResponse = null;
                do {
                    try {
                        System.out.println("Getting assessments...");
                        assResponse = atws.getAssessments(input);
                        System.out.println("Received = " + assResponse);
                        System.out.println("GIFT Expectation Assessment = " + atws.calculateAssessment(assResponse, AutoTutorWebServiceInterface.EXPECTATION));
                        System.out.println("GIFT Hint Assessment = " + atws.calculateAssessment(assResponse, AutoTutorWebServiceInterface.HINT));
                        System.out.println("GIFT Prompt Assessment = " + atws.calculateAssessment(assResponse, AutoTutorWebServiceInterface.PROMPT));
//                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        assResponse = null;
                        System.out.println("Caught exception while trying to get assessments for user text with AT WS.  Trying again...");
                    }
                } while (assResponse == null);

                //
                // Retrieve next set of actions based on user's text
                //
                do {
                    try {
                        System.out.println("Getting Actions...");
                        actionResponse = atws.getActions(input);

                    } catch (Exception e) {
                        actionResponse = null;
                        System.out.println("Caught exception while trying to get actions for user text with AT WS.  Trying again...");
                        e.printStackTrace();
                    }
                } while (actionResponse == null);

            } while (true);  // don't end until AT WS says the script has been completed.


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nLog: \n" + atws.getLog());

        System.out.println("\nGood-bye");
    }
}
