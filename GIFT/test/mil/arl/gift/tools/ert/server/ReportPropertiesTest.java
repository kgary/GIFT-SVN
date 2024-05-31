/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.server.ReportWriter;

/**
 * This test creates, saves and loads an ERT report properties object.
 * 
 * @author mhoffman
 *
 */
public class ReportPropertiesTest {

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

    @Test
    public void testReportProperties() throws Exception {
        
        System.out.println("Creating ReportProperties instance.");
        
        List<Integer> eventSourceIds = new ArrayList<Integer>();
        eventSourceIds.add(1);
        
        List<EventType> eventTypeOptions = new ArrayList<EventType>();
        eventTypeOptions.add(new EventType("EventType1", "Event Type 1", "Event Type 1"));
        List<EventReportColumn> eventColumns = new ArrayList<EventReportColumn>();
        EventReportColumn t2c1 = new EventReportColumn("EventType2:Col1_DN", "EventType2:Col1");
        eventColumns.add(t2c1);
        EventReportColumn t2c2 = new EventReportColumn("EventType2:Col2_DN", "EventType2:Col2");
        eventColumns.add(t2c2);
        eventTypeOptions.add(new EventType("EventType2", "Event Type 2", "Event Type 2", eventColumns));
        eventTypeOptions.add(new EventType("EventType3", "Event Type 3", "Event Type 3"));
        
        List<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>();
        reportColumns.add(new EventReportColumn("Time", "Time"));
        reportColumns.add(new EventReportColumn("Domain Session Time", "DS_Time"));
        reportColumns.add(new EventReportColumn("User Id", "UserId"));
        reportColumns.add(t2c1);
        reportColumns.add(t2c2);
        
        ReportProperties initialProperties = new ReportProperties("anonymous", eventSourceIds, eventTypeOptions, reportColumns, ReportProperties.DEFAULT_EMPTY_CELL, ReportProperties.DEFAULT_FILENAME);
        initialProperties.setDefaultSelectedEventTypes(eventTypeOptions);
        
        try{
            String filename = "ERT_ReportSettings.settings";
            System.out.println("Saving to "+filename);
            ReportWriter.saveReportProperties(new File(EventReportServer.DEFAULT_SETTINGS_ROOT_DIR + File.separator + filename), initialProperties);
            
            System.out.println("Loading from "+filename);
            
            ReportProperties finalProperties = EventReportServer.loadReportProperties(filename, initialProperties);
            
            System.out.println("Resulting = "+finalProperties);
        
        }catch(Exception e){
            e.printStackTrace();
            Assert.fail("Failed to save and then load the created ERT report properties.");
        }
    }
}
