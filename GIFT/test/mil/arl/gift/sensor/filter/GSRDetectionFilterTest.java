/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import generated.sensor.GSRDetectionFilterInput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
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
 * This JUnit test is designed to test the GSR Detection filter using several
 * data input files.
 *
 * @author mhoffman
 */
public class GSRDetectionFilterTest {

    /** files containing GSR data to test the filter with (format: gsrValue) */
    private static final String[] gsrDataFiles = {"QSensorData.txt"};

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

        final int SAMPLING_RATE = 250;

        final float WINDOW_SIZE = 10.0f;

        TestSensor someSensor = new TestSensor("GSRDetectionFilterTestSensor", SensorTypeEnum.GSR);
        
        File curFile = new File(System.getProperty("user.dir"));

        //read each of the input files
        for (String dataFileName : gsrDataFiles) {
            
            File dataFile = new File(curFile, PackageUtil.getData() + File.separator + "tests" + File.separator + dataFileName);

            //create new filter instance for each new data file
            GSRDetectionFilterInput config = new GSRDetectionFilterInput();
            config.setSamplingRateHz(BigInteger.valueOf(SAMPLING_RATE));
            config.setWindowSize(WINDOW_SIZE);

            GSRDetectionFilter filter = new GSRDetectionFilter(config);
            EventProducerInformation information = new EventProducerInformation();
            
            EventProducerAttribute attribute = new EventProducerAttribute(SensorAttributeNameEnum.EDA, Tuple3dValue.class);
            information.addAttribute(attribute);
            
            filter.setEventProducerInformation(information);
            filter.setEventWriter(new TestWriter(filter.getEventProducerInformation()));

            System.out.println("\n##########################################\nTesting with " + dataFileName + "\n###############################");

            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            String line;
            long time = 0;
            while ((line = br.readLine()) != null) {

                time += 1;
                double value = Double.parseDouble(line);

                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>(1);
                sensorAttributeToValue.put(SensorAttributeNameEnum.GSR, new DoubleValue(SensorAttributeNameEnum.GSR, value));
                SensorData sd = new SensorData(sensorAttributeToValue, time * 1000);
                SensorDataEvent sde = new SensorDataEvent(someSensor, sd);
                filter.sensorDataEvent(sde);

            }//end while

            br.close();

        }//end for

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFilterSensorDataThrowsException() {

        final int SAMPLING_RATE = 1;

        final float WINDOW_SIZE = 10.0f;

        GSRDetectionFilterInput config = new GSRDetectionFilterInput();
        config.setSamplingRateHz(BigInteger.valueOf(SAMPLING_RATE));
        config.setWindowSize(WINDOW_SIZE);

        @SuppressWarnings("unused")
        GSRDetectionFilter filter = new GSRDetectionFilter(config);
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
        
        public TestWriter(EventProducerInformation eventProducerInformation){
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
