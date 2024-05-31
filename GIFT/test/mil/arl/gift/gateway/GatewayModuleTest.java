/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.dis.DISInterface;
import mil.arl.gift.net.api.message.Message;
import org.jdis.pdu.DetonationPDU;
import org.jdis.pdu.EntityStatePDU;
import org.jdis.pdu.FirePDU;
import org.jdis.pdu.PDU;
import org.jdis.pdu.record.BurstDescriptor;
import org.jdis.pdu.record.EntityIdentifier;
import org.jdis.pdu.record.EntityType;
import org.jdis.pdu.record.EulerAngles;
import org.jdis.pdu.record.EventIdentifier;
import org.jdis.pdu.record.LinearVelocityVector;
import org.jdis.pdu.record.SimulationAddress;
import org.jdis.pdu.record.WorldCoordinates;
import org.junit.Ignore;

/**
 * A jUnit test for the GatewayModule
 *
 * @author jleonard
 */
@Ignore
public class GatewayModuleTest {

    @Ignore
    private static class TestGateway extends GatewayModule {

        /** menu choices */
        public static final int EXIT_CHOICE = 0;

        public static final int STARTRESUME_CHOICE = 1;

        public static final int STOPFREEZE_CHOICE = 2;

        public static final int ENTITY_STATE_CHOICE = 3;

        public static final int FIRE_CHOICE = 4;

        public static final int DETONATION_CHOICE = 5;

        private static final int APP_ID = 43068;

        private static final int SITE_ID = 13553;

        /**
         * Class constructor - customizes the loading of interop interfaces for
         * this test class by specifying a different interop config file from
         * what would normally be loaded using just a gateway module.
         */
        public TestGateway() {
            super(PackageUtil.getData() + File.separator + "tests" + File.separator + "test.interopConfig.xml");
            instance = this;
        }

        private static void displayChoicePrompt() {
            System.out.print("Choice: ");
        }

        public static void displayMainMenu() {

            // Display menu graphics
            System.out.println("\n============================");
            System.out.println("|    MAIN MENU SELECTION   |");
            System.out.println("============================");
            System.out.println(" Options:");
            System.out.println("\t" + STARTRESUME_CHOICE + ". Send Start/Resume Message");
            System.out.println("\t" + STOPFREEZE_CHOICE + ". Send Stop/Freeze Message");
            System.out.println("\t" + ENTITY_STATE_CHOICE + ". Send Entity State PDU");
            System.out.println("\t" + FIRE_CHOICE + ". Send Fire PDU");
            System.out.println("\t" + DETONATION_CHOICE + ". Send Detonation PDU");
            System.out.println("\t" + EXIT_CHOICE + ". Exit");
            System.out.println("============================");
            displayChoicePrompt();
        }

        private static Message createStartResume() {

            Calendar cal = Calendar.getInstance();
            long realWorldTime = cal.getTimeInMillis();
            long simulationTime = 0;
            long requestID = 999;
            StartResume startResume = new StartResume(realWorldTime, simulationTime, requestID);

            return new Message(MessageTypeEnum.START_RESUME, 0, null, null, null, null, startResume, false);
        }

        private static Message createStopFreeze() {

            long realWorldTime = Calendar.getInstance().getTimeInMillis();

            //Reasons:
            // 0: OTHER
            // 1: RECESS
            // 2: TERMINATION
            // 3: SYSTEM_FAILURE
            // 4: SECURITY_VIOLATION
            // 5: ENTITY_RECONSTRUCTION
            // 6: STOP_FOR_RESET
            // 7: STOP_FOR_RESTART
            // 8: ABORT_TRAINING_RETURN_TO_TACTICAL_OPERATIONS
            Integer reason = 2;

            //Behaviors:
            // 0: run internal simulation clock
            // 1: transmit PDUs
            // 2: update simulation models of other entities via record PDUs
            // 3: continue transmit PDU
            // 4: cease update simulation models of other entities via received PDUs
            // 5: continue updates simulation models of other entities via received PDUs
            Integer frozenBehavior = 0;

            long requestID = 999;
            StopFreeze stopFreeze = new StopFreeze(realWorldTime, reason, frozenBehavior, requestID);

            return new Message(MessageTypeEnum.STOP_FREEZE, 0, null, null, null, null, stopFreeze, false);
        }

        public static EntityStatePDU createEntityState() {

            EntityStatePDU pdu = new EntityStatePDU();

            EntityIdentifier eId = new EntityIdentifier();
            eId.setSimulationAddress(APP_ID, SITE_ID);
            eId.setEntityIdentifier(101);
            pdu.setEntityID(eId);

            LinearVelocityVector vel = new LinearVelocityVector(1.0f, 2.0f, 3.0f);
            pdu.setEntityLinearVelocity(vel);

            WorldCoordinates location = new WorldCoordinates(10.0f, 20.0f, 30.0f);
            pdu.setEntityLocation(location);

            EulerAngles orientation = new EulerAngles(0.7f, -0.2f, 0.8f);
            pdu.setEntityOrientation(orientation);

            //Force ID
            // 0: other
            // 1: friendly
            // 2: opposing
            // 3: neutral
            pdu.setForceID(1);

            //Platform.<domain>.United States.<category>.<subcategory>.<specific>.<extra>
            EntityType type = new EntityType(1, 1, 225, 1, 1, 1, 0);
            pdu.setEntityType(type);

            return pdu;
        }

        public static FirePDU createFire() {

            FirePDU pdu = new FirePDU();

            SimulationAddress simAddr = new SimulationAddress(APP_ID, SITE_ID);

            EntityIdentifier firingEntityID = new EntityIdentifier();
            firingEntityID.setSimulationAddress(simAddr);
            firingEntityID.setEntityIdentifier(1);
            pdu.setFiringEntityID(firingEntityID);

            EntityIdentifier targetEntityID = new EntityIdentifier();
            targetEntityID.setSimulationAddress(simAddr);
            targetEntityID.setEntityIdentifier(1);
            pdu.setTargetEntityID(targetEntityID);

            EntityIdentifier munitionID = new EntityIdentifier();
            munitionID.setSimulationAddress(simAddr);
            munitionID.setEntityIdentifier(101);
            pdu.setMunitionID(munitionID);

            WorldCoordinates location = new WorldCoordinates(10.0f, 20.0f, 30.0f);
            pdu.setLocationInWorldCoordinates(location);

            LinearVelocityVector vel = new LinearVelocityVector(1.0f, 2.0f, 3.0f);
            pdu.setVelocity(vel);

            BurstDescriptor bDesc = new BurstDescriptor();
            bDesc.setFuse(0); //other
            bDesc.setQuantity(1);
            bDesc.setRate(60); //rounds per minute
            bDesc.setWarhead(4000); //practice
            //munition.<domain>.United States.<category>.<subcategory>.<specific>.<extra>
            EntityType munitionType = new EntityType(2, 1, 255, 1, 1, 1, 0);
            bDesc.setMunitionType(munitionType);
            pdu.setBurstDescriptor(bDesc);

            EventIdentifier eventID = new EventIdentifier();
            eventID.setSimulationAddress(simAddr);
            eventID.setEventIdentifier(999);
            pdu.setEventID(eventID);

            return pdu;
        }

        public static DetonationPDU createDetonation() {

            DetonationPDU pdu = new DetonationPDU();

            SimulationAddress simAddr = new SimulationAddress(APP_ID, SITE_ID);

            EntityIdentifier firingEntityID = new EntityIdentifier();
            firingEntityID.setSimulationAddress(simAddr);
            firingEntityID.setEntityIdentifier(1);
            pdu.setFiringEntityID(firingEntityID);

            EntityIdentifier targetEntityID = new EntityIdentifier();
            targetEntityID.setSimulationAddress(simAddr);
            targetEntityID.setEntityIdentifier(1);
            pdu.setTargetEntityID(targetEntityID);

            EntityIdentifier munitionID = new EntityIdentifier();
            munitionID.setSimulationAddress(simAddr);
            munitionID.setEntityIdentifier(101);
            pdu.setMunitionID(munitionID);

            WorldCoordinates location = new WorldCoordinates(10.0f, 20.0f, 30.0f);
            pdu.setLocationInWorldCoordinates(location);

            LinearVelocityVector vel = new LinearVelocityVector(1.0f, 2.0f, 3.0f);
            pdu.setVelocity(vel);

            BurstDescriptor bDesc = new BurstDescriptor();
            bDesc.setFuse(0); //other
            bDesc.setQuantity(1);
            bDesc.setRate(60); //rounds per minute
            bDesc.setWarhead(4000); //practice
            //munition.<domain>.United States.<category>.<subcategory>.<specific>.<extra>
            EntityType munitionType = new EntityType(2, 1, 255, 1, 1, 1, 0);
            bDesc.setMunitionType(munitionType);
            pdu.setBurstDescriptor(bDesc);

            EventIdentifier eventID = new EventIdentifier();
            eventID.setSimulationAddress(simAddr);
            eventID.setEventIdentifier(999);
            pdu.setEventID(eventID);

            pdu.setDetonationResult(12); //dirt blast, medium

            pdu.setLocationInEntityCoordinates(20.0f, 25.0f, 30.0f);

            return pdu;
        }

        public static void sendPDU(TestGateway gw, PDU pdu) {

            //find the first dis interop interface to send it
            DISInterface disInterface = null;
            for (AbstractInteropInterface interop : gw.getInterops().values()) {

                if (interop instanceof DISInterface) {
                    disInterface = (DISInterface) interop;
                    break;
                }
            }

            if (disInterface == null) {
                System.out.println("unable to find a DIS interop interface, therefore the pdu will not be sent");
            } else {
                disInterface.sendPDU(pdu);
                System.out.println("pdu sent: " + pdu);
            }
        }
    }

    //TODO: Turn this in to an automatic test
    @Ignore
    public void testGatewayModule() {

        TestGateway gw = new TestGateway();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        int choice;

        try {

            do {
                //reset
                choice = -1;

                try {
                    TestGateway.displayMainMenu();
                    choice = Integer.valueOf(inputReader.readLine());

                    switch (choice) {
                        case TestGateway.STARTRESUME_CHOICE:
                            Message srmessage = TestGateway.createStartResume();
                            gw.handleMessageFromGIFT(srmessage);
                            System.out.println("message sent: " + srmessage);
                            break;
                        case TestGateway.STOPFREEZE_CHOICE:
                            Message sfmessage = TestGateway.createStopFreeze();
                            gw.handleMessageFromGIFT(sfmessage);
                            System.out.println("message sent: " + sfmessage);
                            break;
                        case TestGateway.ENTITY_STATE_CHOICE:
                            TestGateway.sendPDU(gw, TestGateway.createEntityState());
                            break;
                        case TestGateway.FIRE_CHOICE:
                            TestGateway.sendPDU(gw, TestGateway.createFire());
                            break;
                        case TestGateway.DETONATION_CHOICE:
                            TestGateway.sendPDU(gw, TestGateway.createDetonation());
                            break;
                        default:
                            System.out.println("Unhandled choice of " + choice);
                    }

                } catch (Exception e) {
                    System.out.println("Test's main loop Caught exception:\n" + e);
                }

            } while (TestGateway.EXIT_CHOICE != choice);
        } catch (Exception e) {
            System.out.println("Caught exception while trying to create pdu writer");
            e.printStackTrace();
        }

        System.out.println("Good-bye");

        //kill any threads
        System.exit(0);
    }
}
