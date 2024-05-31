/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.FilterTypeEnum;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.ums.UMSModule;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.table.DbDomainSession;
import mil.arl.gift.ums.db.table.DbEventFile;
import mil.arl.gift.ums.db.table.DbSensorFile;
import mil.arl.gift.ums.db.table.DbUser;

import org.junit.Ignore;

/**
 * A jUnit test for the UMSDatabaseManager
 *
 * @author jleonard
 */
@Ignore
public class UMSDatabaseManagerTest {

    private static final String EXIT_CHOICE = "0";

    private static final String BACK_TO_MAIN = "0";

    private static final String INPUT_BREAK_STR = "#";

    private static void displayChoicePrompt() {
        System.out.print("Choice: ");
    }

    private static void displayMainMenu() {

        // Display menu graphics
        System.out.println("\n============================");
        System.out.println("|    MAIN MENU SELECTION   |");
        System.out.println("============================");
        System.out.println(" Options:");
        System.out.println("\t1. Insert");
        System.out.println("\t2. Delete");
        System.out.println("\t3. Update");
        //System.out.println("\t99. Erase All Table Rows");
        System.out.println("\t" + EXIT_CHOICE + ". Exit");
        System.out.println("\t99. Recreate DB");
        System.out.println("============================");
        displayChoicePrompt();
    }

    private static void displayInsertMenu() {

        System.out.println("\n============================");
        System.out.println("|   insert MENU SELECTION  |");
        System.out.println("============================");
        System.out.println(" Options:");
        System.out.println("\t1: User");
        System.out.println("\t2: Event File");
        System.out.println("\t3: Session (req: event file and user entry ids)");
        System.out.println("\t4: Sensor File (req: session entry id)");
        System.out.println("\t" + BACK_TO_MAIN + ": Main Menu");
        System.out.println("============================");
        displayChoicePrompt();
    }

    private static void displayDeleteMenu() {

        System.out.println("\n============================");
        System.out.println("|   delete MENU SELECTION  |");
        System.out.println("============================");
        System.out.println(" Options:");
        System.out.println("\t1: User");
        System.out.println("\t2: Event File");
        System.out.println("\t3: Session");
        System.out.println("\t4: Sensor File");
        System.out.println("\t" + BACK_TO_MAIN + ": Main Menu");
        System.out.println("============================");
        displayChoicePrompt();
    }

    private static void displayUpdateMenu() {

        System.out.println("\n============================");
        System.out.println("|   update MENU SELECTION  |");
        System.out.println("============================");
        System.out.println(" Options:");
        System.out.println("\t1: User");
        System.out.println("\t2: Event File");
        System.out.println("\t3: Session");
        System.out.println("\t4: Sensor File");
        System.out.println("\t" + BACK_TO_MAIN + ": Main Menu");
        System.out.println("============================");
        displayChoicePrompt();
    }

    /**
     * Handle row update by retrieving data from user via keyboard
     *
     * @param choice - the table chosen to update a row from
     * @param inputReader - the keyboard reader
     */
    private static void handleUpdate(String choice, BufferedReader inputReader) {

        try {

            if ("1".equals(choice)) {
                //delete user

                System.out.println("UPDATE USER");
                System.out.print("user id = ");
                String userIdStr = inputReader.readLine();

                DbUser user = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(userIdStr), DbUser.class);

                if (user == null) {
                    System.out.println("\nUnable to find User in table");
                    return;
                }

                System.out.println("Current User = " + user);

                //
                //get new values for fields
                //

                String genderTypes = " ";
                for (Object gType : GenderEnum.VALUES()) {
                    genderTypes += gType + " ";
                }

                GenderEnum genderType = null;
                do {
                    System.out.print("gender [" + genderTypes + "] = ");
                    String gender = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(gender)) {
                        return;
                    }

                    genderType = GenderEnum.valueOf(gender);

                } while (genderType == null);

                user.setGender(genderType.toString());

                if (UMSDatabaseManager.getInstance().updateRow(user)) {
                    System.out.println("\nUpdate was successful");
                } else {
                    System.out.println("\nUpdate failed");
                }

            } else if ("2".equals(choice)) {
                //delete event file

                System.out.println("UPDATE EVENT FILE");
                System.out.print("event file id = ");
                String eventFileIdStr = inputReader.readLine();

                DbEventFile eFile = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(eventFileIdStr), DbEventFile.class);

                if (eFile == null) {
                    System.out.println("\nUnable to find Event File in table");
                    return;
                }

                System.out.println("Current Event File = " + eFile);

                //
                //get new values for fields
                //

                System.out.print("filename = ");
                String filename = inputReader.readLine();

                eFile.setFileName(filename);

                if (UMSDatabaseManager.getInstance().updateRow(eFile)) {
                    System.out.println("\nUpdate was successful");
                } else {
                    System.out.println("\nUpdate failed");
                }

            } else if ("3".equals(choice)) {
                //delete user session

                System.out.println("UPDATE SESSION");
                System.out.print("session id = ");
                String sessionIdStr = inputReader.readLine();

                DbDomainSession session = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(sessionIdStr), DbDomainSession.class);

                if (session == null) {
                    System.out.println("\nUnable to find User Session in table");
                    return;
                }

                System.out.println("Current Session = " + session);

                //
                //get new values for fields
                //

                DbUser user = null;
                do {
                    System.out.print("user id = ");
                    String userid = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(userid)) {
                        return;
                    }

                    user = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(userid), DbUser.class);

                } while (user == null);

                DbEventFile eFile = null;
                do {
                    System.out.print("event file id = ");
                    String eventFileId = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(eventFileId)) {
                        return;
                    }

                    eFile = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(eventFileId), DbEventFile.class);

                } while (eFile == null);

                session.setUser(user);
                session.setEventFile(eFile);

                if (UMSDatabaseManager.getInstance().updateRow(session)) {
                    System.out.println("\nUpdate was successful");
                } else {
                    System.out.println("\nUpdate failed");
                }

            } else if ("4".equals(choice)) {
                //delete sensor file

                System.out.println("UPDATE SENSOR FILE");
                System.out.print("sensor file id = ");
                String sensorFileIdStr = inputReader.readLine();

                DbSensorFile sFile = UMSDatabaseManager.getInstance().selectRowById(Integer.parseInt(sensorFileIdStr), DbSensorFile.class);

                if (sFile == null) {
                    System.out.println("\nUnable to find Sensor File in table");
                    return;
                }

                System.out.println("Current Sensor File = " + sFile);

                //
                //get new values for fields
                //

                DbDomainSession uSession = null;
                do {
                    System.out.print("session id = ");
                    String sessionId = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(sessionId)) {
                        return;
                    }

                    uSession = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(sessionId), DbDomainSession.class);

                } while (uSession == null);

                System.out.print("filename = ");
                String filename = inputReader.readLine();

                if (INPUT_BREAK_STR.equals(filename)) {
                    return;
                }

                String sensorTypes = " ";
                for (Object sType : SensorTypeEnum.VALUES()) {
                    sensorTypes += sType + " ";
                }

                SensorTypeEnum sType = null;
                do {
                    System.out.print("sensor type [" + sensorTypes + "] = ");
                    String sensorType = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(sensorType)) {
                        return;
                    }

                    sType = SensorTypeEnum.valueOf(sensorType);

                } while (sType == null);


                String filterTypes = " ";
                for (Object fType : FilterTypeEnum.VALUES()) {
                    filterTypes += fType + " ";
                }

                FilterTypeEnum fType = null;
                do {
                    System.out.print("filter type [" + filterTypes + "] = ");
                    String filterType = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(filterType)) {
                        return;
                    }

                    fType = FilterTypeEnum.valueOf(filterType);

                } while (fType == null);

                sFile.setFileName(filename);
                sFile.setSensorType(sType.toString());
                sFile.setDomainSession(uSession);

                if (UMSDatabaseManager.getInstance().updateRow(sFile)) {
                    System.out.println("\nUpdate was successful");
                } else {
                    System.out.println("\nUpdate failed");
                }

            } else {
                System.out.println("Invalid selection of " + choice);
            }//end else-if

        } catch (Throwable e) {
            System.out.println("Returning to Main Menu because of caught exception during UPDATE:\n");
            e.printStackTrace();
        }
    }

    /**
     * Handle row delete by retrieving data from user via keyboard
     *
     * @param choice - the table chosen to delete a row from
     * @param inputReader - the keyboard reader
     */
    private static void handleDelete(String choice, BufferedReader inputReader) {

        try {

            if ("1".equals(choice)) {
                //delete user

                System.out.println("DELETE USER");
                System.out.print("user id = ");
                String userIdStr = inputReader.readLine();

                DbUser user = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(userIdStr), DbUser.class);

                if (user == null) {
                    System.out.println("\nUnable to find User in table");
                    return;
                }

                try{
                    UMSDatabaseManager.getInstance().deleteRow(user);
                    System.out.println("\nDelete was successful");
                } catch(Exception e) {
                    System.out.println("\nDelete failed");
                    e.printStackTrace();
                }

            } else if ("2".equals(choice)) {
                //delete event file

                System.out.println("DELETE EVENT FILE");
                System.out.print("event file id = ");
                String eventFileIdStr = inputReader.readLine();

                DbEventFile eFile = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(eventFileIdStr), DbEventFile.class);

                if (eFile == null) {
                    System.out.println("\nUnable to find Event File in table");
                    return;
                }

                try{
                    UMSDatabaseManager.getInstance().deleteRow(eFile);
                    System.out.println("\nDelete was successful");
                } catch(Exception e) {
                    System.out.println("\nDelete failed");
                    e.printStackTrace();
                }

            } else if ("3".equals(choice)) {
                //delete user session

                System.out.println("DELETE SESSION");
                System.out.print("session id = ");
                String sessionIdStr = inputReader.readLine();

                DbDomainSession session = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(sessionIdStr), DbDomainSession.class);

                if (session == null) {
                    System.out.println("\nUnable to find User Session in table");
                    return;
                }

                try{
                    UMSDatabaseManager.getInstance().deleteRow(session);
                    System.out.println("\nDelete was successful");
                } catch(Exception e) {
                    System.out.println("\nDelete failed");
                    e.printStackTrace();
                }

            } else if ("4".equals(choice)) {
                //delete sensor file

                System.out.println("DELETE SENSOR FILE");
                System.out.print("sensor file id = ");
                String sensorFileIdStr = inputReader.readLine();

                DbSensorFile sFile = UMSDatabaseManager.getInstance().selectRowById(Integer.parseInt(sensorFileIdStr), DbSensorFile.class);

                if (sFile == null) {
                    System.out.println("\nUnable to find Sensor File in table");
                    return;
                }

                try{
                    UMSDatabaseManager.getInstance().deleteRow(sFile);
                    System.out.println("\nDelete was successful");
                } catch(Exception e) {
                    System.out.println("\nDelete failed");
                    e.printStackTrace();
                }

            } else {
                System.out.println("Invalid selection of " + choice);
            }//end else-if

        } catch (Throwable e) {
            System.out.println("Returning to Main Menu because of caught exception during DELETE:\n");
            e.printStackTrace();
        }
    }

    /**
     * Handle row insert by retrieving data from user via keyboard
     *
     * @param choice - the table chosen to insert a row into
     * @param inputReader - the keyboard reader
     */
    private static void handleInsert(String choice, BufferedReader inputReader) {

        try {

            if ("1".equals(choice)) {
                //insert user

                System.out.println("INSERT USER");

                String genderTypes = " ";
                for (Object gType : GenderEnum.VALUES()) {
                    genderTypes += gType + " ";
                }

                GenderEnum genderType = null;
                do {
                    System.out.print("gender [" + genderTypes + "] = ");
                    String gender = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(gender)) {
                        return;
                    }

                    genderType = GenderEnum.valueOf(gender);

                } while (genderType == null);

                DbUser user = new DbUser(genderType.getName());

                try{
                    UMSDatabaseManager.getInstance().insertRow(user);
                    System.out.println("\nInsert was successful");
                }catch(Exception e){
                    System.out.println("\nInsert failed");
                    e.printStackTrace();
                }


            } else if ("2".equals(choice)) {
                //insert event file

                System.out.println("INSERT EVENT FILE");
                System.out.print("filename = ");
                String filename = inputReader.readLine();

                DbEventFile eFile = new DbEventFile(filename);

                try{
                    UMSDatabaseManager.getInstance().insertRow(eFile);
                    System.out.println("\nInsert was successful");
                } catch(Exception e) {
                    System.out.println("\nInsert failed");
                    e.printStackTrace();
                }

            } else if ("3".equals(choice)) {
                //insert session

                System.out.println("INSERT SESSION");

                DbUser user = null;
                do {
                    System.out.print("user id = ");
                    String userid = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(userid)) {
                        return;
                    }

                    user = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(userid), DbUser.class);

                } while (user == null);

                DbEventFile eFile = null;
                do {
                    System.out.print("event file id = ");
                    String eventFileId = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(eventFileId)) {
                        return;
                    }

                    eFile = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(eventFileId), DbEventFile.class);

                } while (eFile == null);


                DbDomainSession session = new DbDomainSession(user);
                session.setEventFile(eFile);
                session.setDomainSourceId("TEST");

                try{
                    UMSDatabaseManager.getInstance().insertRow(session);
                    System.out.println("\nInsert was successful");
                } catch(Exception e) {
                    System.out.println("\nInsert failed");
                    e.printStackTrace();
                }

            } else if ("4".equals(choice)) {
                //insert sensor file

                System.out.println("INSERT SENSOR FILE");

                DbDomainSession uSession = null;
                do {
                    System.out.print("session id = ");
                    String sessionId = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(sessionId)) {
                        return;
                    }

                    uSession = UMSDatabaseManager.getInstance().selectRowById(Integer.valueOf(sessionId), DbDomainSession.class);

                } while (uSession == null);

                System.out.print("filename = ");
                String filename = inputReader.readLine();

                if (INPUT_BREAK_STR.equals(filename)) {
                    return;
                }

                String sensorTypes = " ";
                for (Object sType : SensorTypeEnum.VALUES()) {
                    sensorTypes += sType + " ";
                }

                SensorTypeEnum sType = null;
                do {
                    System.out.print("sensor type [" + sensorTypes + "] = ");
                    String sensorType = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(sensorType)) {
                        return;
                    }

                    sType = SensorTypeEnum.valueOf(sensorType);

                } while (sType == null);


                String filterTypes = " ";
                for (Object fType : FilterTypeEnum.VALUES()) {
                    filterTypes += fType + " ";
                }

                FilterTypeEnum fType = null;
                do {
                    System.out.print("filter type [" + filterTypes + "] = ");
                    String filterType = inputReader.readLine();

                    if (INPUT_BREAK_STR.equals(filterType)) {
                        return;
                    }

                    fType = FilterTypeEnum.valueOf(filterType);

                } while (fType == null);

                DbSensorFile sFile = new DbSensorFile(uSession, sType.getName(), filename);

                try{
                    UMSDatabaseManager.getInstance().insertRow(sFile);
                    System.out.println("\nInsert was successful");
                } catch(Exception e) {
                    System.out.println("\nInsert failed");
                    e.printStackTrace();
                }

            } else {
                System.out.println("Invalid selection of " + choice);
            }//end else-if

        } catch (Throwable e) {
            System.out.println("Returning to Main Menu because of caught exception during INSERT:\n");
            e.printStackTrace();
        }
    }

    @Ignore
    public void testDatabaseManagement() throws ConfigurationException {

        UMSModule.getInstance();
        UMSDatabaseManager dbMgr = UMSDatabaseManager.getInstance();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String choice, subchoice;
        do {
            //reset
            choice = null;
            subchoice = null;

            try {
                displayMainMenu();
                choice = inputReader.readLine();

                if ("99".equals(choice)) {
                    //recreated the database - i.e. delete all entries

                    System.out.println("Are you sure you want to re-create all tables? [y/n]");
                    displayChoicePrompt();
                    subchoice = inputReader.readLine();

                    if ("y".equals(subchoice)) {
                        dbMgr.recreateDB();
                        System.out.println("database has been erased");
                    }

                } else if ("1".equals(choice)) {
                    //insert row into table

                    displayInsertMenu();
                    subchoice = inputReader.readLine();

                    if (subchoice != BACK_TO_MAIN) {
                        handleInsert(subchoice, inputReader);
                    }

                } else if ("2".equals(choice)) {
                    //delete row from table

                    displayDeleteMenu();
                    subchoice = inputReader.readLine();

                    if (subchoice != BACK_TO_MAIN) {
                        handleDelete(subchoice, inputReader);
                    }

                } else if ("3".equals(choice)) {
                    //update row in table

                    displayUpdateMenu();
                    subchoice = inputReader.readLine();

                    if (subchoice != BACK_TO_MAIN) {
                        handleUpdate(subchoice, inputReader);
                    }

                } else if (EXIT_CHOICE.equals(choice)) {
                    System.out.println("Exit selected");

                } else {
                    System.out.println("Invalid selection of " + choice);

                }//end else-if
            } catch (Exception e) {
                System.out.println("Test's main loop Caught exception:\n");
                e.printStackTrace();
            }

        } while (!EXIT_CHOICE.equals(choice));

        try {
            dbMgr.cleanup();
        } catch (Exception e) {
            System.out.println("Test's main loop caught exception while cleaning up:\n");
            e.printStackTrace();
        }

        System.out.println("Good-bye");
    }
    
    /**
     * Tests logic of retrieving domain session message log file names that are associated 
     * with GIFT published courses.  This was useful in testing the logic needed to delete
     * orphan domain session message log files in order to clean up the messages folder.  
     * A button was recently added to the GIFT control panel to perform this delete operation.
     */
    // change to @Test to be able to run this logic
    @Ignore
    public void testGetPublishedCourseLogFiles(){
        
        // get all the experiment log files names
        String selectStatement = "select MESSAGELOGFILENAME from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".experimentsubject";
        List<Object> results = UMSDatabaseManager.getInstance().executeSelectSQLQuery(selectStatement);
        System.out.println("Experiment logs:\n"+results);
        
        // get all the LTI log files names
        selectStatement = "select MESSAGELOGFILENAME from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".datacollectionresultslti";
        results = UMSDatabaseManager.getInstance().executeSelectSQLQuery(selectStatement);
        System.out.println("LTI logs:\n"+results);
    }
    
    /**
     * Tests logic of resorting a multiple choice question's choices.
     * Ticket #4454 identified an issue in the Hibernate converter that prevent
     * Multiple choice, matrix of choices and rating scale question choices from being reordered.
     */
    // change to @Test to be able to run this logic
    @Ignore
    public void testChangeMCQuestionChoiceOrder(){
        
        // use appropriate variables for the next 3 variables when running with your UMS survey db
        String username = "mhoffman";
        int surveyContextId = 60;
        Survey survey = Surveys.getSurvey(185);
        
        // change the question choice order like the survey composer would do
        OptionList optionList = ((MultipleChoiceSurveyQuestion)survey.getPages().get(0).getElements().get(0)).getChoices();
        optionList.getListOptions().get(0).setSortKey(1);
        optionList.getListOptions().get(1).setSortKey(0);
        
        try {
            Surveys.surveyEditorSaveSurvey(survey, surveyContextId, null, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
