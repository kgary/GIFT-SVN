/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionPermission;
import mil.arl.gift.ums.db.table.DbExperimentSubject;

public class DataCollectionServicesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    //This test will create 2 experiments including 2 course folders.  Then add a subject to each experiment.
    //Finally deletes both created experiments which also deletes the course folders on disk.
    //It will also retrieve the experiments based on author.
    @Test
    public void manageExperiment() {

        DataCollectionServicesInterface experimentServices = ServicesManager.getInstance().getDataCollectionServices();
        DbDataCollection experimentA = null, experimentB = null;
        String authorA = "ExperimentServicesTest.junit.manageExperiment.A", authorB = "ExperimentServicesTest.junit.manageExperiment.B";
        try{

            String descriptionA = "This is the ExperimentServicesTest junit test.";
            String nameA = "ExperimentServicesTest.junit.manageExperiment.A";

            String descriptionB = "This is the ExperimentServicesTest junit test.";
            String nameB = "ExperimentServicesTest.junit.manageExperiment.B";

            //create Experiment "A"

            //not using ExperimentServicesInterface so don't have to worry about course options wrapper logic
            experimentA = UMSDatabaseManager.getInstance().createExperiment(nameA, descriptionA, authorA,
                    DataSetType.EXPERIMENT, null);

            //create fake course folder with a file for A
            //Note: the course folder needs to be set before attempting to delete the experiment
            String experimentFolderAPath = experimentA.getId();
            String courseFolderAPath= experimentFolderAPath + "/experimentservicestest.junit.coursefolderA";
            File experimentFolderA = new File("../Domain/Experiments/" + experimentFolderAPath);
            experimentFolderA.mkdir();
            File courseFolderA = new File("../Domain/Experiments/" + courseFolderAPath);
            courseFolderA.mkdir();
            File fileA = new File(courseFolderA + File.separator + "a.txt");
            fileA.createNewFile();
            experimentA.setCourseFolder(courseFolderAPath);
            experimentServices.updateDataCollectionItem(authorA, experimentA);

            //test if the experiment A can be retrieved by author A
            Set<DbDataCollection> experimentsA = experimentServices.getDataCollectionItems(authorA);
            Assert.assertTrue("Failed to retrieve experiment by author.", experimentsA.size() == 1);

            //make sure the course folder was updated in the database for experiment A
            DbDataCollection experimentAUpdated = experimentServices.getDataCollectionItem(experimentA.getId());
            Assert.assertTrue("Failed to update the experiment course folder.", courseFolderAPath.equals(experimentAUpdated.getCourseFolder()));

            //create Experiment "B"

            //not using ExperimentServicesInterface so don't have to worry about course options wrapper logic
            experimentB = UMSDatabaseManager.getInstance().createExperiment(nameB, descriptionB, authorB,
                    DataSetType.EXPERIMENT, null);

            //test if the experiment B can be retrieved by author B
            Set<DbDataCollection> experimentsB = experimentServices.getDataCollectionItems(authorB);
            Assert.assertTrue("Failed to retrieve experiment by author.", experimentsB.size() == 1);

            //create fake course folder with a file for B
            String experimentFolderBPath = experimentB.getId();
            String courseFolderBPath= experimentFolderBPath + "/experimentservicestest.junit.coursefolderB";
            File experimentFolderB = new File("../Domain/Experiments/" + experimentFolderBPath);
            experimentFolderB.mkdir();
            File courseFolderB = new File("../Domain/Experiments/" + courseFolderBPath);
            courseFolderB.mkdir();
            File fileB = new File(courseFolderB + File.separator + "b.txt");
            fileB.createNewFile();
            experimentB.setCourseFolder(courseFolderBPath);
            experimentServices.updateDataCollectionItem(authorB, experimentB);

            //make sure the course folder was updated in the database for experiment B
            DbDataCollection experimentBUpdated = experimentServices.getDataCollectionItem(experimentB.getId());
            Assert.assertTrue("Failed to update the experiment course folder.", courseFolderBPath.equals(experimentBUpdated.getCourseFolder()));

            //add subject to A
            DbExperimentSubject subject1A = new DbExperimentSubject();
            subject1A.setStartTime(new Date());
            subject1A.setMessageLogFilename("ds_1A.log");

            //not using ExperimentServicesInterface because that class doesn't manage adding subjects to experiments
            UMSDatabaseManager.getInstance().addSubjectToExperiment(subject1A, experimentA);

            //test if the subject A is now associated with experiment A
            experimentAUpdated = experimentServices.getDataCollectionItem(experimentA.getId());
            Assert.assertTrue("Failed to update the experiment's subjects.", experimentAUpdated.getSubjects().size() == 1);
            Assert.assertTrue("Failed to update the experiment's course folder.", experimentAUpdated.getCourseFolder().equals(courseFolderAPath));

            //add subject to B
            DbExperimentSubject subject1B = new DbExperimentSubject();
            subject1B.setStartTime(new Date());
            subject1B.setMessageLogFilename("ds_1B.log");

           //not using ExperimentServicesInterface because that class doesn't manage adding subjects to experiments
            UMSDatabaseManager.getInstance().addSubjectToExperiment(subject1B, experimentB);

            //test if the subject B is now associated with experiment B
            experimentBUpdated = experimentServices.getDataCollectionItem(experimentB.getId());
            Assert.assertTrue("Failed to update the experiment's subjects.", experimentBUpdated.getSubjects().size() == 1);
            Assert.assertTrue("Failed to update the experiment's course folder.", experimentBUpdated.getCourseFolder().equals(courseFolderBPath));

            //
            // Permissions check
            //

            // add permissions of different non-owner types
            DbDataCollectionPermission newManager = new DbDataCollectionPermission();
            newManager.setDataCollectionId(experimentAUpdated.getId());
            newManager.setDataCollectionUserRole(DataCollectionUserRole.MANAGER);
            newManager.setUsername(authorB);

            DbDataCollectionPermission newResearcher = new DbDataCollectionPermission();
            newResearcher.setDataCollectionId(experimentAUpdated.getId());
            newResearcher.setDataCollectionUserRole(DataCollectionUserRole.RESEARCHER);
            newResearcher.setUsername("another researcher");

            experimentAUpdated.getPermissions().add(newManager);
            experimentAUpdated.getPermissions().add(newResearcher);

            experimentServices.updateDataCollectionItem(authorA, experimentAUpdated);

            experimentAUpdated = experimentServices.getDataCollectionItem(experimentA.getId());

            Assert.assertTrue("Failed to update the experiment's permissions with a new manager and researcher.", experimentAUpdated.getPermissions().size() == 3);

            // have user with no permissions try to update the experiment properties
            try{
                experimentServices.updateDataCollectionItem("not a user username", experimentAUpdated);
                Assert.fail("A user with no permissions to an experiment was allowed to change the properties of that experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }

            // have user with incorrect permissions try to update the experiment properties
            try{
                experimentServices.updateDataCollectionItem("another researcher", experimentAUpdated);
                Assert.fail("A user with insufficient permissions to an experiment was allowed to change the properties of that experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }

            // have user with incorrect permissions try to delete the experiment
            try{
                experimentServices.deleteDataCollectionItem("another researcher", experimentAUpdated.getId(), null);
                Assert.fail("A user with insufficient permissions to an experiment was allowed to delete that experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }

            // have user with incorrect permissions try to export the raw data
            try{
                experimentServices.exportDataCollectionItemData("another researcher", "temp", experimentAUpdated.getId(), false, null);
                Assert.fail("A user with insufficient permissions to an experiment was allowed to export the raw data for that experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }

            // have user with incorrect permissions try to export the course
            try{
                experimentServices.exportDataCollectionItemCourse("another researcher", "temp", experimentAUpdated.getId(), null);
                Assert.fail("A user with insufficient permissions to an experiment was allowed to export the course for that experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }

            // remove permissions of different non-owner types
            Iterator<DbDataCollectionPermission> permissionsItr = experimentAUpdated.getPermissions().iterator();
            while(permissionsItr.hasNext()){

                DbDataCollectionPermission permission = permissionsItr.next();
                if(permission.getDataCollectionUserRole() != DataCollectionUserRole.OWNER){
                    permissionsItr.remove();
                }
            }

            experimentServices.updateDataCollectionItem(authorB, experimentAUpdated);
            Assert.assertTrue("Failed to update the experiment's permissions by removing all non owner users.", experimentAUpdated.getPermissions().size() == 1);


            // attempt to remove owner
            permissionsItr = experimentAUpdated.getPermissions().iterator();
            while(permissionsItr.hasNext()){

                DbDataCollectionPermission permission = permissionsItr.next();
                if(permission.getDataCollectionUserRole() == DataCollectionUserRole.OWNER){
                    permissionsItr.remove();
                }
            }

            try{
                experimentServices.updateDataCollectionItem(authorB, experimentAUpdated);
                Assert.fail("A user was able to remove all owners of an experiment");
            }catch(@SuppressWarnings("unused") Exception e){
                //this should happen
            }


        }catch(Exception e){
            System.out.println("Test failed because an exception was thrown.");
            e.printStackTrace();
            Assert.fail();
        }finally{

            Set<DbDataCollection> experimentsA = experimentServices.getDataCollectionItems(authorA);
            for(DbDataCollection experimentAToDelete : experimentsA){

                try{
                    experimentServices.deleteDataCollectionItem(authorA, experimentAToDelete.getId(), new ProgressIndicator());
                }catch(Exception e){
                    System.out.println("Failed to delete the created experiment of "+experimentAToDelete);
                    e.printStackTrace();
                }
            }

            Set<DbDataCollection> experimentsB = experimentServices.getDataCollectionItems(authorB);
            for(DbDataCollection experimentBToDelete : experimentsB){

                try{
                    experimentServices.deleteDataCollectionItem(authorB, experimentBToDelete.getId(), new ProgressIndicator());
                }catch(Exception e){
                    System.out.println("Failed to delete the created experiment of "+experimentBToDelete);
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void generateReport() throws Exception {

        DbDataCollection experiment = null;
        String author = "ExperimentServicesTest.junit";
        try{
            //a manually created domain session message log file to parse and test with
            String filename = "data" + File.separator + "tests" + File.separator + "Legacy_Domain_Session_Log.log";

            // TODO: this is how the ERT currently works, all files are found and assigned an id.  Based on the files the users selects, the ids of those files
            // are used to retrieve the file on the server.  This retrieval is done in ErtRpcServiceImpl.generateEventReport.  For the Experiment Services
            // this logic will need to change to something simple because basically we are automatically selecting all files and not allowing the user
            // to specify which to select from.  Maybe just make this optional, so if null then find all files for the experiment.
            ArrayList<Integer> eventSourceIds = new ArrayList<Integer>();
            eventSourceIds.add(1);

            // create a sample list of report columns to use when calling the ReportProperties constructor
            ArrayList<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>();
            reportColumns.add(new EventReportColumn("Time", "Time"));
            reportColumns.add(new EventReportColumn("Event Type", "Event Type"));
            reportColumns.add(new EventReportColumn("User Id", "User_ID"));
            reportColumns.add(new EventReportColumn("Content", "Content"));
            reportColumns.add(new EventReportColumn("Domain Session Time", "DS_Time"));

            // create an experiment instance with subjects needs to be created. [this uses the back door to creating an experiment so we don't copy a course folder as
            // done in ExperimentServices.createExperiment]
            String description = "This is the ExperimentServicesTest junit test.";
            String name = "ExperimentServicesTest.junit.experiment";

            experiment = UMSDatabaseManager.getInstance().createExperiment(name, description, author,
                    DataSetType.EXPERIMENT, null);

            //create fake course folder with a file
            //This is needed in order to cleanup the experiment w/o issue
            String experimentFolderPath = experiment.getId();
            String courseFolderPath= experimentFolderPath + "/experimentservicestest.generateReport.junit.coursefolder";
            File experimentFolder = new File("../Domain/Experiments/" + experimentFolderPath);
            experimentFolder.mkdir();
            File courseFolder = new File("../Domain/Experiments/" + courseFolderPath);
            courseFolder.mkdir();
            File file = new File(courseFolder + File.separator + "a.txt");
            file.createNewFile();
            experiment.setCourseFolder(courseFolderPath);
            ServicesManager.getInstance().getDataCollectionServices().updateDataCollectionItem(author, experiment);

            // create a subject for the experiment
            DbExperimentSubject subject = new DbExperimentSubject();
            subject.setMessageLogFilename(filename);
            subject.setStartTime(new Date());
            UMSDatabaseManager.getInstance().addSubjectToExperiment(subject, experiment);

            //
            // generate report
            //

            //TODO: try to generate report when the user doesn't have permissions

//          File dsLogFile = new File(filename);
//          MessageLogEventSourceParser parser = new MessageLogEventSourceParser(dsLogFile);
//
//          //select all events (i.e. message types) and all columns of each event for the test
//          List<EventType> eventTypes = parser.getTypesOfEvents();
//
//          //the name of the file to create
//          String outputFilename = "test.GenerateReport.csv";
//
//          ReportProperties reportProperties = new ReportProperties(eventSourceIds, eventTypes, reportColumns, ReportProperties.DEFAULT_EMPTY_CELL, outputFilename);
//
//            DownloadableFileRef fileRef = ServicesManager.getInstance().getExperimentServices().generateReport(experiment.getId(), progressIndicator, reportProperties);
//
//            //make sure a file was created
//            Assert.assertNotNull(fileRef);

        }catch(Exception e){
            System.out.println("Test failed because an exception was thrown.");
            e.printStackTrace();
            Assert.fail();
        }finally{
            //
            // cleanup
            //
            if(experiment != null){
                try{
                    ServicesManager.getInstance().getDataCollectionServices().deleteDataCollectionItem(author, experiment.getId(), new ProgressIndicator());
                }catch(Exception e){
                    System.out.println("Failed to delete the created experiment of "+experiment);
                    e.printStackTrace();
                }
            }
        }
    }

}
