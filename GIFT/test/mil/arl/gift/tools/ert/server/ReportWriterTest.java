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
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.event.DefaultEvent;
import mil.arl.gift.common.ert.server.ReportWriter;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.net.api.message.Message;

/**
 * A jUnit test for the ReportWriter
 *
 * @author jleonard
 */
public class ReportWriterTest {

    /** path to a system message log file created for this test */
    private static final String LOG_FILE = PackageUtil.getTestData() + File.separator + "system.log";

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
    public void testReportWriter() throws Exception {

        System.out.println("Starting report creation process...");

        String inputFilename = LOG_FILE;
        MessageLogReader reader = MessageLogReader.createMessageLogReader(inputFilename);
        reader.parseLog(new FileProxy(new File(inputFilename)));

        System.out.println("Finished parsing log file");

        String outputFilename = "testResults/test.ReportWriter.out";
        
        // create a sample list of event source IDs to use when calling the ReportProperties constructor in line 101
        ArrayList<Integer> eventSourceIds = new ArrayList<Integer>();
        eventSourceIds.add(1);
        
        // create a sample list of eventTypeOptions to use when calling the ReportProperties constructor in line 101      
        ArrayList<EventType> eventTypeOptions = new ArrayList<EventType>();
        eventTypeOptions.add(new EventType("EventType1","Event Type 1", "Event Type 1"));
        List<EventReportColumn> eventColumns = new ArrayList<EventReportColumn>();
        	eventColumns.add(new EventReportColumn("EventType2:Col1_DN","EventType2:Col1"));
        	eventColumns.add(new EventReportColumn("EventType2:Col2_DN","EventType2:Col2"));
        	eventTypeOptions.add(new EventType("EventType 2 Name", "EventType 2 Name", "EventType 2 Description", eventColumns));
        eventTypeOptions.add(new EventType("EventType3","Event Type 3", "Event Type 3"));
        
        // create a sample list of report columns to use when calling the ReportProperties constructor in line 101
        ArrayList<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>();
        reportColumns.add(new EventReportColumn("Time", "Time"));
        reportColumns.add(new EventReportColumn("Event Type", "Event Type"));
        reportColumns.add(new EventReportColumn("User Id", "User_ID"));
        reportColumns.add(new EventReportColumn("Content", "Content"));
        reportColumns.add(new EventReportColumn("Domain Session Time", "DS_Time"));
        reportColumns.add(new EventReportColumn("EventType2:Col1_DN", "EventType2:Col1"));
        reportColumns.add(new EventReportColumn("EventType2:Col2_DN", "EventType2:Col2"));
        
        ReportWriter writer = new ReportWriter(new ReportProperties("anonymous", eventSourceIds, eventTypeOptions, reportColumns, ".", outputFilename), true);
        Set<MessageTypeEnum> types = reader.getTypesOfMessages();
        for (MessageTypeEnum type : types) {

            List<Message> messages = reader.getMessagesByType(type);
            for (Message message : messages) {
                DefaultEvent event;
                if (message.getPayload() == null) {
                    event = new DefaultEvent(message.getMessageType().toString(), message.getTimeStamp(), null);
                } else {
                    event = new DefaultEvent(message.getMessageType().toString(), message.getTimeStamp(), message.getPayload().toString());
                }

                writer.addRow(event.toRow());
            }
        }

        try {
            writer.write();
            System.out.println("Finished creating report file: " + outputFilename);
        } catch (Exception e) {
            System.out.println("Caught exception while writing report");
            e.printStackTrace();
        }
    }
}
