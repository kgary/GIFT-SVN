/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.sensor.impl.AbstractSensor;

import org.junit.Ignore;

/**
 * A jUnit test for the SensorModule
 *
 * @author jleonard
 */
@Ignore
public class SensorModuleTest {

    private static final String EXIT_CHOICE = "0";

    private static void displayChoicePrompt() {
        System.out.print("Choice: ");
    }

    private static void displayMainMenu() {

        // Display menu graphics
        System.out.println("\n============================");
        System.out.println("|    MAIN MENU SELECTION   |");
        System.out.println("============================");
        System.out.println(" Options:");
        System.out.println("\t1. Display Sensor(s) State");
        System.out.println("\t2. Start Sensor(s)");
        System.out.println("\t3. Stop Sensor(s)");
        System.out.println("\t" + EXIT_CHOICE + ". Exit");
        System.out.println("============================");
        displayChoicePrompt();
    }

    //TODO: Turn this in to an automatic test
    @Ignore
    public void testSensorModule() {

        try {
            SensorsConfigFileHandler configHandler = new SensorsConfigFileHandler(new FileProxy(new File("config" + File.separator + "sensor" + File.separator + "configurations" + File.separator + "SelfAssessment.sensorconfig.xml")));

            System.out.println("Reading " + configHandler.getConfigurationFileName());

            SensorModule sModule = new SensorModule();
            
            Collection<AbstractEventProducer> producers = configHandler.getEventProducers();
            sModule.addEventProducers(producers);

            //set arbitrary domain session info
            sModule.setTestIds(-1, -1);

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String choice;
            do {
                choice = null;

                try {
                    displayMainMenu();
                    choice = inputReader.readLine();

                    if ("1".equals(choice)) {
                        //show sensor states

                        for (AbstractEventProducer producer : sModule.getProducers()) {

                            if (producer instanceof AbstractSensor) {
                                AbstractSensor sensor = (AbstractSensor) producer;
                                System.out.println(sensor);
                            }
                        }


                    } else if ("2".equals(choice)) {
                        //start sensors

                        for (AbstractEventProducer producer : sModule.getProducers()) {

                            if (producer instanceof AbstractSensor) {
                                AbstractSensor sensor = (AbstractSensor) producer;
                                sensor.start(System.currentTimeMillis());
                                System.out.println(sensor.getSensorName() + " " + sensor.getSensorState());
                            }
                        }

                    } else if ("3".equals(choice)) {
                        //stop sensors

                        for (AbstractEventProducer producer : sModule.getProducers()) {

                            if (producer instanceof AbstractSensor) {
                                AbstractSensor sensor = (AbstractSensor) producer;
                                sensor.stop();
                                System.out.println(sensor.getSensorName() + " " + sensor.getSensorState());
                            }
                        }

                    } else if (EXIT_CHOICE.equals(choice)) {
                        System.out.println("Exit selected");

                    } else {
                        System.out.println("Invalid selection of " + choice);

                    }//end else-if


                } catch (Exception e) {
                    System.out.println("Test's main loop Caught exception:\n" + e);
                    e.printStackTrace();
                }

            } while (!EXIT_CHOICE.equals(choice));

        } catch (Throwable e) {
            System.err.println("Caught exception while reading configuration file");
            e.printStackTrace();
        }

        System.out.println("Good-bye");

        //kill any threads
        System.exit(0);
    }
}
