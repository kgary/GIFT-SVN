/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import generated.proto.gateway.ACKProto;
import generated.proto.gateway.ActiveDomainSessionsRequestProto.ActiveDomainSessionsRequest;
import generated.proto.gateway.ApplyStrategiesProto.ApplyStrategies;
import generated.proto.gateway.CloseDomainSessionRequestProto.CloseDomainSessionRequest;
import generated.proto.gateway.DomainOptionsRequestProto.DomainOptionsRequest;
import generated.proto.gateway.DomainSelectionRequestProto.DomainSelectionRequest;
import generated.proto.gateway.EvaluatorUpdateRequestProto.EvaluatorUpdateRequest;
import generated.proto.gateway.NACKProto.NACK;
import generated.proto.gateway.ServiceBusMessageProto.ServiceBusMessage;
import generated.proto.gateway.StrategyProto;
import generated.proto.gateway.StrategyToApplyProto.StrategyToApply;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.net.api.ProtobufMessageClient;
import mil.arl.gift.net.api.ProtobufQueueMessageClient;
import mil.arl.gift.net.api.ProtobufTopicMessageClient;

/**
 * A JUnit test that tests {@link AdaptiveLearningServiceInterface}'s ability to
 * communicate with the domain module over the Adaptive Learning service bus.
 * Requirements:
 * 1. Adaptive Learning Service bus must be running and listening on {@value #CONNECTION_URL}
 * 2. GIFT must be running as this test needs the domain module to receive and reply to request as well as the other 
 *    modules to be ready to run a course that is started in this test.
 * 
 *
 * @author sharrison
 */
@Ignore // this test requires that other systems be running and configured correctly.  Therefore it will not be included as part of the automated test suite
public class AdaptiveLearningServiceInterfaceTest {

    /** The connection url for the clients */
    private static final String CONNECTION_URL = "tcp://localhost:61618";

    /** Test user to run the course */
    private static final String TEST_USER = "testUser";

    /** The hard-coded test course */
    private static final String DOMAIN_SOURCE_ID = "Public/Urban Operation - Resource Delivery - STE/Urban Operation - Resource Delivery - STE.course.xml";

    /**
     * The error message that is returned if there was an exception that was not
     * handled while processing a request.
     */
    private static final ServiceBusMessage DEFAULT_ERROR_MSG = ServiceBusMessage.newBuilder().setNack(
            NACK.newBuilder().setReason("Uncaught Exception").setMessage("There was a problem handling the request."))
            .build();
    
    /**
     * The ACK message that should be sent back to the external application to
     * confirm that the message was received and handle successfully.
     */
    private static final ServiceBusMessage DEFAULT_ACK_MSG = ServiceBusMessage.newBuilder()
            .setAck(ACKProto.ACK.getDefaultInstance()).build();

    /** The outgoing message client */
    private static final ProtobufMessageClient<ServiceBusMessage> apiClient = new ProtobufQueueMessageClient<>(
            CONNECTION_URL, "Api", false, ServiceBusMessage.getDefaultInstance(), DEFAULT_ERROR_MSG, DEFAULT_ACK_MSG);

    /** The incoming message client */
    private static final ProtobufMessageClient<ServiceBusMessage> broadcastClient = new ProtobufTopicMessageClient<>(
            CONNECTION_URL, "Broadcast", false, ServiceBusMessage.getDefaultInstance(), DEFAULT_ERROR_MSG, DEFAULT_ACK_MSG);


    /**
     * The {@link CompletableFuture} that allows tests to wait for a specific
     * message type before ending
     */
    private static CompletableFuture<Void> waitingForMessageFuture = null;

    /** The instance of the helper class to check for a specific message type */
    private static Function<ServiceBusMessage, Boolean> checkMessageType;
    
    /** the original lesson level found in common properties before this junit started, 
     * used to restore the value when this junit is finished */
    private static LessonLevelEnum originalLessonLevel = null;

    /**
     * Actions to perform before running tests in this class
     *
     * @throws JMSException if there was a problem establishing a connection to
     *         the service bus.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws JMSException {
        
        // make sure the lesson level is RTA for this test
        originalLessonLevel = CommonProperties.getInstance().getLessonLevel();
        CommonProperties.getInstance().setCommandLineArgs(new String[]{CommonProperties.LESSON_LEVEL, LessonLevelEnum.RTA.getName()});
        
        apiClient.connect();

        broadcastClient.setMessageHandler(AdaptiveLearningServiceInterfaceTest::onBroadcastMessage);
        broadcastClient.connect();
    }

    /** Cleanup actions to perform after all the tests are finished */
    @AfterClass
    public static void tearDownAfterClass() {
        apiClient.disconnect(false);
        broadcastClient.disconnect(false);
        
        // return the property back to the value it was before this junit started
        CommonProperties.getInstance().setCommandLineArgs(new String[]{CommonProperties.LESSON_LEVEL, originalLessonLevel.getName()});
    }

    /** Actions to perform before running each test */
    @Before
    public void setUp() {
        /* Nothing to do before each test */
    }

    /** Actions to perform after running each test */
    @After
    public void tearDown() {
        /* End the 'waiting for message type' future if it is still active */
        if (waitingForMessageFuture != null && !waitingForMessageFuture.isDone()) {
            waitingForMessageFuture.cancel(true);
        }

        /* Reset these static values to null in case the next test doesn't need
         * them */
        waitingForMessageFuture = null;
        checkMessageType = null;
    }

    /**
     * Sends a request for the domain options
     *
     * @throws Exception if there was an unexpected problem while running the
     *         test.
     */
    @Test
    public void testDomainOptionsRequest() throws Exception {
        
        System.out.println("starting testDomainOptionsRequest");

        final DomainOptionsRequest optionsRequest = DomainOptionsRequest.newBuilder()
                .setLMSUserName(TEST_USER)
                .build();

        final ServiceBusMessage request = ServiceBusMessage.newBuilder()
                .setDomainOptionsRequest(optionsRequest)
                .build();

        ServiceBusMessage response = apiClient.sendMessageAsync(request).get(60, TimeUnit.SECONDS);
        System.out.println(response);
        assertNotNull(response);
        assertTrue(response.hasDomainOptionsReply());
        assertTrue(response.getDomainOptionsReply().getDomainOptionsCount() > 0);
    }
    
    /**
     * Sends an Evaluator Update Request protobuf message over the protobuf message client.
     * This request will set the performance assessment value of the 'Mission Brief' task
     * in the Urban Operation - Resource Delivery - STE course's dkf.
     * @throws Exception if there was an unexpected problem while running the test.
     */
    @Test
    public void testEvaluatorUpdateRequest() throws Exception{
        
        System.out.println("starting testEvaluatorUpdateRequest");
        
        final EvaluatorUpdateRequest evaluatorUpdateRequest = EvaluatorUpdateRequest.newBuilder()
                .setEvaluator("GIFT junit")
                .setTaskConceptName("Mission Brief")
                .setPerformanceMetric("BelowExpectation")
                .setTimestamp(System.currentTimeMillis())
                .build();
        
        final ServiceBusMessage request = ServiceBusMessage.newBuilder()
                .setEvaluatorUpdateRequest(evaluatorUpdateRequest)
                .build();
        
        ServiceBusMessage response = apiClient.sendMessageAsync(request).get(60, TimeUnit.SECONDS);
        System.out.println(response);
        assertNotNull(response);
    }

    /**
     * Sends a request for a selected domain
     *
     * @throws Exception if there was an unexpected problem while running the
     *         test.
     */
    @Test
    public void testDomainSelectionRequest() throws Exception {
        
        System.out.println("starting testDomainSelectionRequest");

        /* Initialize the static variables so that we can wait for the
         * LESSON_STARTED message before the test ends */
        waitingForMessageFuture = new CompletableFuture<>();
        checkMessageType = ServiceBusMessage::hasLessonStarted;

        /* Start the course */
        int dsId = sendSelectionRequest();

        /* Wait until the LESSON_STARTED is received. */
        waitingForMessageFuture.get(1, TimeUnit.MINUTES);
        
        // close the session
        sendCloseRequest(dsId);
    }

    /**
     * Tests the functionality of the {@link ActiveDomainSessionsRequest} and
     * {@link ActiveDomainSessionsReply}.
     *
     * @throws Exception if there was an unexpected error while executing the
     *         test.
     */
    @Test
    public void testActiveSessionsList() throws Exception {
        
        System.out.println("starting testActiveSessionsList");
        
        final int domainSessionId = sendSelectionRequest();

        final ActiveDomainSessionsRequest activeSessionsRequest = ActiveDomainSessionsRequest.getDefaultInstance();
        final ServiceBusMessage listActiveSessions = ServiceBusMessage.newBuilder()
                .setActiveDomainSessionsRequest(activeSessionsRequest).build();

        final ServiceBusMessage response = apiClient.sendMessageAsync(listActiveSessions).get(60, TimeUnit.SECONDS);
        System.out.println(response);
        assertNotNull(response);
        assertTrue(response.hasActiveDomainSessionsReply());

        sendCloseRequest(domainSessionId);
    }

    /**
     * Tests that the {@link ApplyStrategies} is sent to a running domain
     * session.
     *
     * @throws Exception if there was an unexpected error in the test.
     */
    @Test
    public void testApplyStrategies() throws Exception {
        
        System.out.println("starting testApplyStrategies");
        
        final int domainSessionId = sendSelectionRequest();

        /* Build the apply strategies message */
        StrategyProto.Strategy strat = StrategyProto.Strategy.newBuilder().setName("Strategy A").build();
        final StrategyToApply stratToApply = StrategyToApply.newBuilder().setEvaluator("External Application")
                .setStrategy(strat).setTrigger("Trigger Strategy A").build();
        final ApplyStrategies applyStrategies = ApplyStrategies.newBuilder().setDomainSessionId(domainSessionId)
                .setEvaluator("External Application").addStrategies(stratToApply).build();
        final ServiceBusMessage applyMsg = ServiceBusMessage.newBuilder().setApplyStrategies(applyStrategies).build();

        final ServiceBusMessage replyResponse = apiClient.sendMessageAsync(applyMsg).get(60, TimeUnit.SECONDS);
        System.out.println(replyResponse);
        assertNotNull(replyResponse);
        assertTrue(replyResponse.hasAck());
    }

    /**
     * Tests that the {@link CloseDomainSessionRequest} closes the session
     * properly.
     *
     * @throws Exception if there was an unexpected error while running the
     *         test.
     */
    @Test
    public void testCloseDomainSessionMessage() throws Exception {
        
        System.out.println("starting testCloseDomainSessionMessage");
        
        final int domainSessionId = sendSelectionRequest();
        sendCloseRequest(domainSessionId);
    }

    /**
     * Performs the selection of the course to run.
     *
     * @return The domain session id of the started session.
     * @throws Exception if there was a problem sending the selection request.
     */
    private int sendSelectionRequest() throws Exception {
        DomainSelectionRequest selectionRequest = DomainSelectionRequest.newBuilder()
                .setDomainSourceId(DOMAIN_SOURCE_ID)
                .setLmsUsername(TEST_USER).build();

        final ServiceBusMessage message = ServiceBusMessage.newBuilder().setDomainSelectionRequest(
                selectionRequest)
                .build();

        final ServiceBusMessage startReply = apiClient.sendMessageAsync(message).get(60, TimeUnit.SECONDS);
        System.out.println(startReply);
        assertNotNull(startReply);
        assertTrue(startReply.hasDomainSelectionReply());
        return startReply.getDomainSelectionReply().getDomainSession().getDomainSessionId();
    }

    /**
     * Sends a {@link CloseDomainSessionRequest} for the specified domain
     * session.
     *
     * @param domainSessionId The id of the domain session to close.
     * @throws Exception if there was a problem closing the domain session.
     */
    private void sendCloseRequest(int domainSessionId) throws Exception {
        final CloseDomainSessionRequest closeRequest = CloseDomainSessionRequest.newBuilder()
                .setDomainSessionId(domainSessionId).build();

        final ServiceBusMessage message = ServiceBusMessage.newBuilder()
                .setCloseDomainSessionRequest(closeRequest)
                .build();

        final ServiceBusMessage closeResponse = apiClient.sendMessageAsync(message).get(60, TimeUnit.SECONDS);
        System.out.println(closeResponse);
        assertNotNull(closeResponse);
        assertTrue(closeResponse.hasAck());
    }

    /**
     * Method to receive the incoming messages from the service bus.
     *
     * @param msg the received message.
     * @return The {@link CompletableFuture} that will complete with a response
     *         to the message. Will always be null.
     */
    private static CompletableFuture<ServiceBusMessage> onBroadcastMessage(ServiceBusMessage msg) {
        System.out.println(msg);

        /* Check if the message type is required for a test to complete */
        if (waitingForMessageFuture != null && checkMessageType != null && checkMessageType.apply(msg)) {
            waitingForMessageFuture.complete(null);
        }

        return null;
    }
}
