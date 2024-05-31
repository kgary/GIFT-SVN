/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.StringValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerAttribute;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.writer.GenericSensorDelimitedWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This junit test is used to test the ability to write complex sensor data to a csv file while maintaining
 * the idea that each sensor data attribute value is in its own cell. For example, a tuple 3d value of {1.0,2.0,3.0}
 * should span 3 columns using the generic sensor writer, while a string value of "1.0,2.0,3.0" should span 1 column 
 * and have the commas replaced with another character (since the comma is the delimiter character).
 * 
 * @author mhoffman
 *
 */
public class ComplexSensorDelimitedWriterTest {
    
    /** the user id to use for the test */
    private static final int userId = 1;
    
    /** the domain session id to use for the test */
    private static final int dsId = 1;
    
    /** the domain session output folder created for this test */
    private File dsOutputFolder = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        
        // create domain session output folder 
        dsOutputFolder = new File("output"+File.separator+"domainSessions"+File.separator+"domainSession"+dsId+"_uId"+userId);
        dsOutputFolder.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        
        // clean up the folder that was created for this test
        if(dsOutputFolder != null){
            FileUtil.deleteDirectory(dsOutputFolder);
        }
    }

    @Test
    public void testGenericSensorDelimitedWriterDelimReplacement() throws NumberFormatException, IOException {
        
        try{
            SelfAssessmentSensor sensor = new SelfAssessmentSensor("ComplexSensorDelimitedWriterTestSensor");
            
            //
            //create value to write
            //
            StringValue value = new StringValue(SensorAttributeNameEnum.HEAD, "1.0,2.0,3.0");
            
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
            sensorAttributeToValue.put(SensorAttributeNameEnum.HEAD, value);
            
            SensorData data = new SensorData(sensorAttributeToValue, 0);
            SensorDataEvent sensorDataEvent = new SensorDataEvent(sensor, data);
            
            //
            //create writer configuration
            //
            generated.sensor.GenericSensorDelimitedWriter configuration = new generated.sensor.GenericSensorDelimitedWriter();
            configuration.setDatumDelimReplacementChar("|");
            configuration.setFilePrefix("GenericSensorDelimitedWriterDelimReplacement-Test");
            
            //create writer instance
            EventProducerInformation information = new EventProducerInformation();
            
            EventProducerAttribute attribute = new EventProducerAttribute(SensorAttributeNameEnum.HEAD, StringValue.class);
            information.addAttribute(attribute);
            
            GenericSensorDelimitedWriter writer = new GenericSensorDelimitedWriter(configuration, information);
            boolean success = writer.initialize(1, 1, null);
            Assert.assertTrue("Failed to initialize the sensor writer", success);
            
            //write event
            writer.sensorDataEvent(sensorDataEvent);
            
            //close writer (and the file)
            writer.finished();
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail();
        }
    }
    
    @Test
    public void testGenericSensorDelimitedWriterComplexData() throws IOException {
        
        try{
            SelfAssessmentSensor sensor = new SelfAssessmentSensor("ComplexSensorDelimitedWriterTestSensor");
            
            //
            //create value to write
            //
            Tuple3dValue value = new Tuple3dValue(SensorAttributeNameEnum.HEAD, new Point3d(1.0,2.0,3.0));
            
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
            sensorAttributeToValue.put(SensorAttributeNameEnum.HEAD, value);
            
            SensorData data = new SensorData(sensorAttributeToValue, 0);
            SensorDataEvent sensorDataEvent = new SensorDataEvent(sensor, data);
            
            //
            //create writer configuration
            //
            generated.sensor.GenericSensorDelimitedWriter configuration = new generated.sensor.GenericSensorDelimitedWriter();
            configuration.setDatumDelimReplacementChar("|");
            configuration.setFilePrefix("GenericSensorDelimitedWriterComplexData-Test");
            
            //create writer instance
            EventProducerInformation information = new EventProducerInformation();
            
            EventProducerAttribute attribute = new EventProducerAttribute(SensorAttributeNameEnum.HEAD, Tuple3dValue.class);
            information.addAttribute(attribute);
            
            GenericSensorDelimitedWriter writer = new GenericSensorDelimitedWriter(configuration, information);
            boolean success = writer.initialize(1, 1, null);
            Assert.assertTrue("Failed to initialize the sensor writer", success);
            
            //write event
            writer.sensorDataEvent(sensorDataEvent);
            
            //close writer (and the file)
            writer.finished();
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail();
        }
    }
}
