/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dignitas.vrengage.protobuf.VrEngageCommon.Vector3d;
import com.dignitas.vrengage.protobuf.VrEngageCommon.VrEngageMessage;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.ActorSide;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.CloudState;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.CreateActor;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Fog;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Overcast;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Rain;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.RemoveActors;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.RunScript;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Teleport;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.TimeOfDay;
import com.dignitas.vrengage.protobuf.VrEngageLOS.LosRequest;
import com.dignitas.vrengage.protobuf.VrEngageLOS.LosResponse;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;

import generated.gateway.VREngage;
import junit.framework.Assert;
import mil.arl.gift.gateway.interop.vrengage.VREngageInterface;

/**
 * <p>
 * A JUnit test that tests {@link VREngageInterface}'s ability to communicate
 * with the VR-Engage training application.
 * </p>
 *
 * <p>
 * This test is not included by default in the automated test suite because it
 * requires the user to already be running a VR-Engage scenario. If you wish to
 * include this test in the automated suite, perform the following steps.
 * </p>
 *
 * <ol>
 * <li>Remove it from the list of excluded files in
 * <b>test/mil/arl/gift/build.xml</b></li>
 * <li>Make any necessary updates to the classpath in
 * <b>test/mil/arl/gift/build.xml</b></li>
 * <li>Remove the @Ignore annotation from this test class</li>
 * </ol>
 *
 * @author nroberts
 */
@Ignore
public class VREngageInterfaceTest {

    private static final int DEFAULT_ADAPTATION_DURATION = 5;

    /** The instance of {@link VREngageInterface} to use for all test cases*/
    private VREngageInterface vreInterface;

    @Before
    public void setUp() throws Exception {

        vreInterface = new VREngageInterface("VrEngage-Test");

        VREngage config = new VREngage();
        config.setNetworkAddress("127.0.0.1");
        config.setNetworkPort(1234);
        vreInterface.configure(config); //configure TCP connection

        assertTrue(vreInterface.isAvailable(true)); //establish initial TCP handshake

        vreInterface.setEnabled(true); //connect to TCP socket
        assertTrue(vreInterface.isEnabled());
    }

    @After
    public void tearDown() throws Exception {

        vreInterface.setEnabled(false); //disconnect from TCP socket
        assertTrue(!vreInterface.isEnabled());
    }

    /**
     * Sends a line-of-sight request message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testLosMessage() {

        int entityId = 5;
        Point3d point = new Point3d(-5506979.471978, -2240680.071091, 2301623.894955);

        Map<Point3d, Double> locationsToVisiblity = new HashMap<>();

        Vector3d.Builder locationBuilder = Vector3d.newBuilder();
        locationBuilder.setX(point.getX());
        locationBuilder.setY(point.getY());
        locationBuilder.setZ(point.getZ());

        LosRequest.Builder requestBuilder = LosRequest.newBuilder();
        requestBuilder.setEntityId(entityId);
        requestBuilder.setLocation(locationBuilder.build());

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(requestBuilder.build()));
        messageBuilder.build();

        VrEngageMessage command = messageBuilder.build();

        VrEngageMessage result = vreInterface.sendCommand(command, new StringBuilder(), true);

        if(result != null) {

            double visibilityResult = 0.0;

            try {
                Any payload = result.getPayload();

                if (payload.is(LosResponse.class)) {

                    LosResponse response = payload.unpack(LosResponse.class);
                    visibilityResult = response.getVisibility();

                    System.out.println("LOS visibility result: " + visibilityResult);

                } else {
                    Assert.fail("result was not a line-of-sight response. reporting 0.0 as visibility result");
                }

            } catch (InvalidProtocolBufferException e) {
                Assert.fail(
                        "line-of-sight response was not formatted properly. reporting 0.0 as visibility result. Error: "
                                + e);
            }

            locationsToVisiblity.put(point, visibilityResult);
        }
        else {
            Assert.fail("Received null result from command: " + command);
        }
    }

    /**
     * Sends an overcast command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testOvercastMessage() {

        int duration = DEFAULT_ADAPTATION_DURATION;

        Overcast.Builder overcastBuilder = Overcast.newBuilder();
        overcastBuilder.setState(CloudState.THUNDERSTORM);
        overcastBuilder.setDuration(Duration.newBuilder().setSeconds(duration));

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(overcastBuilder.build()));

        System.out.println("sending command to VR-Engage plugin:\n"+overcastBuilder.toString());

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a fog command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testFogMessage() {

        double density = 0.6d;
        int duration = DEFAULT_ADAPTATION_DURATION;

        double rColor = 0d;
        double gColor = 0d;
        double bColor = 1d;

        Vector3d.Builder colorBuilder = Vector3d.newBuilder();
        colorBuilder.setX(rColor);
        colorBuilder.setY(gColor);
        colorBuilder.setZ(bColor);

        Fog.Builder fogBuilder = Fog.newBuilder();
        fogBuilder.setDensity(density);
        fogBuilder.setDuration(Duration.newBuilder().setSeconds(duration));
        fogBuilder.setColorRgb(colorBuilder);

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(fogBuilder.build()));

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a time-of-day command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testTimeOfDayMessage() {

        long secondsOfDay = TimeUnit.HOURS.toSeconds(15); //3:00pm

        TimeOfDay.Builder todBuilder = TimeOfDay.newBuilder();
        todBuilder.setTimePastMidnight(Duration.newBuilder().setSeconds(secondsOfDay));

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(todBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a remove actors command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testRemoveActorsMessage() {

        RemoveActors.Builder raBuilder = RemoveActors.newBuilder();
        raBuilder.addEntityMarking("BldgEnemy1");
        raBuilder.addEntityMarking("BldgEnemy2");

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(raBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a teleport command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testTeleportMessage() {

        Point3d point = new Point3d(-5506979.471978, -2240680.071091, 2301623.894955);

        Vector3d.Builder locationBuilder = Vector3d.newBuilder();
        locationBuilder.setX(point.getX());
        locationBuilder.setY(point.getY());
        locationBuilder.setZ(point.getZ());

        Teleport.Builder teleportBuilder = Teleport.newBuilder();
        teleportBuilder.setLocation(locationBuilder);
        teleportBuilder.setEntityMarking("FT1_Leader"); //move fire team leader
        teleportBuilder.setHeading(180); //facing south

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(teleportBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a create actor command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testCreateActorMessage(){

        CreateActor.Builder createBuilder = CreateActor.newBuilder();

        Point3d point = new Point3d(-5506979.471978, -2240680.071091, 2301623.894955);

        Vector3d.Builder locationBuilder = Vector3d.newBuilder();
        locationBuilder.setX(point.getX());
        locationBuilder.setY(point.getY());
        locationBuilder.setZ(point.getZ());

        createBuilder.setLocation(locationBuilder);
        createBuilder.setSide(ActorSide.ENEMY);
        createBuilder.setType("3:1:159:102:5:112:3"); //African Insurgent

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(createBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a rain command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testRainMessage() {

        double value = 0.7;

        int duration = DEFAULT_ADAPTATION_DURATION;

        Rain.Builder rainBuilder = Rain.newBuilder();
        rainBuilder.setIntensity(value);
        rainBuilder.setDuration(Duration.newBuilder().setSeconds(duration));

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(rainBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }

    /**
     * Sends a run script command message to VR-Engage and consumes the result to verify that it
     * was responded to appropriately
     */
    @Test
    public void testScriptMessage() {

        RunScript.Builder scriptBuilder = RunScript.newBuilder();
        scriptBuilder.setScriptText("vrf:setVisibilityDistance(vrf:getGlobalWeather(), 50)");

        VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
        messageBuilder.setPayload(Any.pack(scriptBuilder.build()));
        messageBuilder.build();

        vreInterface.sendCommand(messageBuilder.build(), new StringBuilder(), true);
    }
}
