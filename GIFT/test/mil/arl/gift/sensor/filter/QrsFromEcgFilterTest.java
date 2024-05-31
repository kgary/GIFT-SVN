/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerAttribute;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.writer.AbstractWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This JUnit test is designed to test the QrsFromEcgFilter using a series of input files.
 *
 * @author mhoffman, cragusa
 */
public class QrsFromEcgFilterTest {

    /** files containing ECG data to test the filter with (format: elapsedTime
     * ecgValue) */
    private String[] ecgDataFiles = {"ecg103b.txt", "ecg105a.txt", "ecg111a.txt", "ecg123a.txt", "ecg127a.txt", "ecg129a.txt"};

    /** the data delimiter for the input files content */
    private static final String DELIM = " ";

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
    public void testFilterSensorData() throws NumberFormatException, IOException {

        TestSensor someSensor = new TestSensor("QrsFromEcgFilterTestSensor", SensorTypeEnum.BIOHARNESS);

        File curFile = new File(System.getProperty("user.dir"));

        //read each of the input files
        for (String dataFileName : ecgDataFiles) {

            //create new filter instance for each new data file
        	QrsFromEcgFilter filter = new QrsFromEcgFilter();
            EventProducerInformation information = new EventProducerInformation();
            
            EventProducerAttribute attribute = new EventProducerAttribute(SensorAttributeNameEnum.HEART_RATE, DoubleValue.class);
            information.addAttribute(attribute);
            
            filter.setEventProducerInformation(information);
            filter.setEventWriter(new TestWriter(filter.getEventProducerInformation()));

            File dataFile = new File(curFile, PackageUtil.getData() + File.separator + "tests" + File.separator + dataFileName);

            System.out.println("\n##########################################\nTesting with " + 
            		dataFileName +  " (NOTE: This test requires several minutes to run)" +
            		"\n###############################");
            
            long startTime = System.currentTimeMillis();

            String line;
            try(BufferedReader br = new BufferedReader(new FileReader(dataFile))){

                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, DELIM);
    
                    if (st.countTokens() != 2) {
                        continue;
                    }
    
                    double time = Double.parseDouble(st.nextToken());
                    double value = Double.parseDouble(st.nextToken());
                    
                    long lineStartTime = System.currentTimeMillis();
                    
                    Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>(1);
                    sensorAttributeToValue.put(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE, new DoubleValue(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE, value));
                    SensorData sd = new SensorData(sensorAttributeToValue, (long) (time * 1000));
                    SensorDataEvent sde = new SensorDataEvent(someSensor, sd);                
                    filter.sensorDataEvent(sde);
                    
                    System.out.println(". ("+(System.currentTimeMillis() - lineStartTime) / 1000.0+" sec)");
                    
                }//end while

            }
            
            System.out.println("Completed after "+ (System.currentTimeMillis() - startTime) / 1000.0+" seconds");

        }//end for
    }

    @Ignore
    private static class TestSensor extends AbstractSensor {

        public TestSensor(String sensorName, SensorTypeEnum sensorType) {
            super(sensorName, sensorType);
        }

        @Override
        public boolean test() {
            return true;
        }
    }
    
    @Ignore
    private static class TestWriter extends AbstractWriter {

        public TestWriter(EventProducerInformation eventProducerInformation) {
            super(eventProducerInformation);
        }

        @Override
        public void sensorFilterEvent(long elapsedTime, SensorFilterEvent sensorFilterEvent) {
        }

        @Override
        public void sensorDataEvent(SensorDataEvent sensorDataEvent) {
        }

        @Override
        public void write(String line) {
        }

        @Override
        protected void write(SensorDataEvent sensorDataEvent) {
        }

        @Override
        protected void write(SensorFilterEvent sensorFilterEvent) {
        }

        @Override
        public boolean initialize(int userId, int domainSessionId, String experimentID) {

            return false;
        }

        @Override
        public void finished() {
        }
    }
}
