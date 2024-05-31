/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.adaptivelearningbus;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.gateway.AdaptiveLearningServiceBus;
import generated.proto.gateway.ACKProto;
import generated.proto.gateway.ActiveDomainSessionsReplyProto.ActiveDomainSessionsReply;
import generated.proto.gateway.ApplyStrategiesProto;
import generated.proto.gateway.AuthorizeStrategiesRequestProto;
import generated.proto.gateway.CloseDomainSessionRequestProto;
import generated.proto.gateway.DomainOptionsReplyProto;
import generated.proto.gateway.DomainOptionsRequestProto;
import generated.proto.gateway.DomainSelectionReplyProto.DomainSelectionReply;
import generated.proto.gateway.DomainSelectionRequestProto;
import generated.proto.gateway.EvaluatorUpdateRequestProto;
import generated.proto.gateway.LearnerStateProto;
import generated.proto.gateway.LessonCompletedProto;
import generated.proto.gateway.LessonStartedProto;
import generated.proto.gateway.NACKProto;
import generated.proto.gateway.PedagogicalRequestProto;
import generated.proto.gateway.ServiceBusMessageProto.ServiceBusMessage;
import generated.proto.gateway.TeamMemberRoleAssignmentReplyProto.TeamMemberRoleAssignmentReply;
import generated.proto.gateway.TeamMemberRoleAssignmentRequestProto;
import generated.proto.gateway.TeamMemberRoleAssignmentRequestProto.TeamMemberRoleAssignmentRequest;
import generated.proto.gateway.TutorUserInterfaceFeedbackPayloadProto;
import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.ProtobufQueueMessageClient;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;

/**
 * The {@link AbstractInteropInterface} that is used to communicate with the
 * Adaptive Learning Service Bus.
 *
 * @author tflowers
 *
 */
public class AdaptiveLearningServiceInterface extends AbstractInteropInterface {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AdaptiveLearningServiceInterface.class);

    /** The client used to communicate with the service bus. */
    private ProtobufQueueMessageClient<ServiceBusMessage> apiClient;

    /**
     * The client that is used to broadcast messages to externals applications.
     */
    private ProtobufQueueMessageClient<ServiceBusMessage> broadcastClient;

    /** The message types that this type can consume. */
    private static final List<MessageTypeEnum> supportedMsgTypes = Arrays.asList(
            MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
            MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST,
            MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST,
            MessageTypeEnum.DISPLAY_TEAM_SESSIONS,
            MessageTypeEnum.KNOWLEDGE_SESSION_CREATED,
            MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST,
            MessageTypeEnum.LEARNER_STATE,
            MessageTypeEnum.LESSON_COMPLETED,
            MessageTypeEnum.LESSON_STARTED,
            MessageTypeEnum.PEDAGOGICAL_REQUEST,
            MessageTypeEnum.SENSOR_FILTER_DATA,
            MessageTypeEnum.SIMAN,
            MessageTypeEnum.COURSE_STATE);

    /**
     * The message types that this interop plugin can produce.
     * In this case the interop plugin doesn't connect to an external training application but
     * an external application that is suppose to manage GIFT sessions.  Therefore while it does
     * produce GIFT messages to manage the sessions, the messages are not an indicator of learner
     * state like the other interop plugins report.  That is why the list is empty.
     */
    private static final List<MessageTypeEnum> producedMsgTypes = Arrays.asList();

    /**
     * The training applications that are required to run for this interop
     * Just like {@link #producedMsgTypes}, this list is empty because this interop plugin
     * doesn't connect to external training applications.
     */
    private static final List<TrainingApplicationEnum> reqTrainingAppConfigurations = Arrays.asList();

    /**
     * The error message that is sent by default when there is an uncaught
     * exception while handling incoming requests.
     */
    private static final ServiceBusMessage DEFAULT_ERROR_MSG = ServiceBusMessage.newBuilder()
            .setNack(NACKProto.NACK.newBuilder()
                    .setReason("Unhandled Error")
                    .setMessage("There was an unhandled error while processing the request. Check the gateway log for details."))
            .build();

    /**
     * The ACK message that should be sent back to the external application to
     * confirm that the message was received.
     */
    private static final ServiceBusMessage DEFAULT_ACK_MSG = ServiceBusMessage.newBuilder()
            .setAck(ACKProto.ACK.getDefaultInstance()).build();

    /**
     * The ACK message that should be sent back to the external application to
     * confirm that the message was handled successfully.
     */
    private static final ServiceBusMessage DEFAULT_PROCESSED_ACK_MSG = ServiceBusMessage.newBuilder()
            .setAck(ACKProto.ACK.newBuilder().setHasProcessed(true)).build();

    /**
     * The URL that should be used to establish a connection with the message
     * bus.
     */
    private String connectionUrl = null;

    /**
     * Creates an {@link AdaptiveLearningServiceInterface} with the provided
     * display name.
     *
     * @param displayName The display to use for this instance. Can't be null.
     */
    public AdaptiveLearningServiceInterface(String displayName) {
        super(displayName, false);
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        if (config instanceof AdaptiveLearningServiceBus) {
            AdaptiveLearningServiceBus alsbConfig = (AdaptiveLearningServiceBus) config;
            connectionUrl = "tcp://" + alsbConfig.getNetworkAddress() + ":" + alsbConfig.getNetworkPort();
            try {
                createClients(connectionUrl);
            } catch (JMSException jmsEx) {
                disconnectClients();
                throw new ConfigurationException("Unable to connect to the Adaptive Learning Service Message Bus",
                        "There was a problem connecting GIFT to the Adaptive Learning Service message bus.\n"
                                + "Try the following:\n"
                                + "1) Confirm that the message bus is running. Normally this means running AdaptiveLearningServiceBus\\launchServiceBus.bat\n"
                                + "2) Make sure message bus is reachable at '"
                                + connectionUrl + "'.\nIf not, you can change the address and port in\nGIFT\\config\\gateway\\configurations\\default.interopConfig.xml.\nSearch for 'AdaptiveLearningServiceBus'.\n\n"
                                + "If you disable this Gateway Interop Plugin you will be allowed to continue but this instance of GIFT\n"
                                + "will not be accessible by an external system using the Adaptive Learning Service Message bus.",
                        jmsEx);
            }
        }

        return false;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes() {
        return producedMsgTypes;
    }

    /**
     * Handle the message received on the service bus from the external system that is suppose
     * to be managing GIFT sessions.
     *
     * @param message the service bus message to translate and communicate via
     *        this interop interface.  Can't be null.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request. Can be null if no response is generated.
     */
    public CompletableFuture<ServiceBusMessage> handleServiceBusMessage(ServiceBusMessage message) {

        if (!LessonLevelEnum.RTA.equals(CommonProperties.getInstance().getLessonLevel())) {
            final ServiceBusMessage nackMsg = ServiceBusMessage.newBuilder()
                    .setNack(NACKProto.NACK.newBuilder()
                            .setReason("Configuration Error")
                            .setMessage("The adaptive learning service bus can only be used when GIFT's LessonLevel property (in common.properties) is set to RTA."))
                    .build();
            return CompletableFuture.completedFuture(nackMsg);
        }

        if (message.hasDomainOptionsRequest()) {
            return handleDomainOptionsRequest(message);
        } else if (message.hasDomainSelectionRequest()) {
            return handleDomainSelectionRequest(message);
        } else if (message.hasActiveDomainSessionsRequest()) {
            return handleActiveDomainSessionsRequest(message);
        } else if (message.hasCloseDomainSessionRequest()) {
            return handleCloseDomainSessionRequest(message);
        } else if (message.hasApplyStrategies()) {
            return handleApplyStrategies(message);
        } else if(message.hasEvaluatorUpdateRequest()){
            return handleEvaluatorUpdateRequest(message);
        } else {
            logger.warn("Received an unexpected message " + message);
        }

        return null;
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException {
        final MessageTypeEnum msgType = message.getMessageType();
        final Object payload = message.getPayload();

        if (MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST.equals(msgType)) {
            handleCloseDomainSessionRequest((DomainSessionMessage) message, errorMsg);

        } else if (MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST.equals(msgType)) {
            handleDisplayFeedbackTutorRequest(message, errorMsg);

        } else if (MessageTypeEnum.DISPLAY_TEAM_SESSIONS.equals(msgType)) {
            handleDisplayTeamSessions(errorMsg);

        } else if (MessageTypeEnum.KNOWLEDGE_SESSION_CREATED.equals(msgType)) {
            // No action required.

        } else if (MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST.equals(msgType)) {
            // No action required.

        } else if (MessageTypeEnum.LEARNER_STATE.equals(msgType)) {
            handleLearnerState(message, errorMsg);

        } else if (MessageTypeEnum.LESSON_COMPLETED.equals(msgType)) {
            handleLessonCompleted(message, errorMsg);

        } else if (MessageTypeEnum.LESSON_STARTED.equals(msgType)) {
            handleLessonStarted(message, errorMsg);

        } else if (MessageTypeEnum.PEDAGOGICAL_REQUEST.equals(msgType)) {
            handlePedagogicalRequest(message, errorMsg);

        } else if (MessageTypeEnum.SENSOR_FILTER_DATA.equals(msgType)) {
            handleSensorFilterData((FilteredSensorData) payload, errorMsg);

        } else if (MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST.equals(msgType)) {
            handleAuthorizeStrategiesRequest(message, errorMsg);

        } else if (MessageTypeEnum.SIMAN.equals(msgType)) {
            // No action required, yet.  In the future a Pause might be important to share.
            // Loading, Starting and most likely stopping SIMAN operations is handled by the external
            // system manager and NOT GIFT's responsibility in this configuration.

        } else if(message.getMessageType() == MessageTypeEnum.COURSE_STATE) {

            /* No action required, yet. The Gateway needs to handle these messages when
             * GIFT is run in RTA mode since they are normally handled by the Tutor to
             * clean up the TUI when a course object ends, even though the Gateway really
             * doesn't have anything to clean up */

        } else {
            throw new IllegalArgumentException("The incoming message of type " + msgType + " could not be handled.");
        }

        return false;
    }

    /**
     * Handles an incoming message asking to close the domain session.
     *
     * @param closeDomainSessionMsg The {@link DomainSessionMessage} that is
     *        requesting a specific domain session to be closed.
     * @param errorMsg The {@link StringBuilder} to which error messages should
     *        be appended.
     */
    private void handleCloseDomainSessionRequest(DomainSessionMessage closeDomainSessionMsg, StringBuilder errorMsg) {
        if (logger.isTraceEnabled()) {
            logger.trace("handleCloseDomainSessionRequest(" + closeDomainSessionMsg + ", \"" + errorMsg + "\")");
        }

        final CloseDomainSessionRequestProto.CloseDomainSessionRequest protoRequest;
        try {
            protoRequest = ProtoToGIFTConverter.convertToProto(
                    (CloseDomainSessionRequest) closeDomainSessionMsg.getPayload(),
                    closeDomainSessionMsg.getDomainSessionId());
        } catch (Exception e) {
            logger.error("Unable to convert the CloseDomainSessionRequest.", e);
            errorMsg.append(
                    "There was a problem converting the CloseDomainSessionRequest due to: '" + e.getMessage() + "'.");
            return;
        }

        final ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder().setCloseDomainSessionRequest(protoRequest)
                .setTimestamp(ProtoToGIFTConverter.convertToProto(closeDomainSessionMsg.getTimeStamp())).build();

        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (JMSException e) {
            logger.error("There was a problem broadcasting an CloseDomainSessionRequest.", e);
            errorMsg.append("There was a problem broadcasting the CloseDomainSessionRequest due to: '").append(e.getMessage()).append(
                    "'.");
        }
    }

    /**
     * Sends a feedback string to the service bus.
     *
     * @param msg The {@link Message} to send to the service bus. Can't be null.
     * @param errorMsg The {@link StringBuilder} to append error messages to.
     *        Can't be null.
     */
    private void handleDisplayFeedbackTutorRequest(Message msg, StringBuilder errorMsg) {
        if (!(msg instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The pedagogical request message must be of type 'DomainSessionMessageInterface'.");
        }

        DomainSessionMessageInterface dsMessage = (DomainSessionMessageInterface) msg;

        TutorUserInterfaceFeedback feedback = (TutorUserInterfaceFeedback) msg.getPayload();
        TutorUserInterfaceFeedbackPayloadProto.TutorUserInterfaceFeedbackPayload protoFeedback = ProtoToGIFTConverter
                .convertToProto(feedback, dsMessage.getDomainSessionId());

        final ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder()
                .setTimestamp(ProtoToGIFTConverter.convertToProto(msg.getTimeStamp()))
                .setTutorUserInterfaceFeedbackPayload(protoFeedback)
                .build();
        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (JMSException jmsEx) {
            errorMsg.append("There was a problem sending the tutor feedback message: ").append(jmsEx.getMessage());
        }
    }

    /**
     * Handles an incoming {@link MessageTypeEnum#DISPLAY_TEAM_SESSIONS}
     * request.
     *
     * @param sb The {@link StringBuilder} to which to append error messages.
     */
    private void handleDisplayTeamSessions(StringBuilder sb) {
        final GatewayModule gatewayModule = GatewayModule.getInstance();
        gatewayModule.sendCreateTeamKnowledgeSession().thenCompose(domainMsg -> {
            final int domainSessionId = domainMsg.getDomainSessionId();
            final KnowledgeSessionsReply replyPayload = (KnowledgeSessionsReply) domainMsg.getPayload();
            final Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = replyPayload.getKnowledgeSessionMap();
            final TeamKnowledgeSession teamSession = (TeamKnowledgeSession) knowledgeSessionMap
                    .get(domainSessionId);

            final Team team = teamSession.getTeamStructure();

            final TeamMemberRoleAssignmentRequestProto.Team protoTeam = ProtoToGIFTConverter
                    .convertToProto(team);
            final TeamMemberRoleAssignmentRequest request = TeamMemberRoleAssignmentRequest
                    .newBuilder()
                    .setRootTeam(protoTeam)
                    .setDomainSessionId(domainSessionId)
                    .build();
            final ServiceBusMessage protoMsg = ServiceBusMessage.newBuilder()
                    .setTeamMemberRoleAssignmentRequest(request)
                    .build();
            return broadcastClient.sendMessageAsync(protoMsg).thenCompose(reply -> {
                if (!reply.hasTeamMemberRoleAssignmentReply()) {
                    throw new RuntimeException("The reply was unexpected: " + reply);
                }

                final TeamMemberRoleAssignmentReply assignmentReply = reply.getTeamMemberRoleAssignmentReply();
                final Map<String, String> roleToLearnerMap = assignmentReply.getRoleToLearnerMapMap();

                /* Build the group membership */
                final GroupMembership groupMembership = (GroupMembership) teamSession.getHostSessionMember()
                        .getSessionMembership();
                for (String roleName : roleToLearnerMap.keySet()) {
                    final String learnerName = roleToLearnerMap.get(roleName);

                    /* Build a membership for this assignment */
                    IndividualMembership individualMembership = new IndividualMembership(learnerName);
                    individualMembership.setTeamMember((TeamMember<?>) team.getTeamElement(roleName));

                    /* Aggregate this individual into the group */
                    groupMembership.getMembers().add(individualMembership);
                }

                /* Send the update back to the domain module */
                final Map<Integer, AbstractKnowledgeSession> sessionMap = new HashMap<>();
                sessionMap.put(domainSessionId, teamSession);
                KnowledgeSessionsReply newReply = new KnowledgeSessionsReply(sessionMap);
                return gatewayModule.sendKnowledgeSessionUpdateRequest(newReply);
            });
        });
    }

    /**
     * Sends a snapshot of the learner's current state to the service bus.
     *
     * @param message The {@link Message} to handle. Must be of type
     *        {@link DomainSessionMessageInterface} and must contain a
     *        {@link MessageTypeEnum#LEARNER_STATE}.
     * @param errorMsg The {@link StringBuilder} to append any error messages
     *        to. Can't be null.
     */
    private void handleLearnerState(Message message, StringBuilder errorMsg) {
        if (!(message instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The learner state message must be of type 'DomainSessionMessageInterface'.");
        } else if (!(message.getPayload() instanceof LearnerState)) {
            throw new IllegalArgumentException("The learner state message must contain a learner state payload.");

        }

        int domainSessionId = ((DomainSessionMessageInterface) message).getDomainSessionId();
        LearnerStateProto.LearnerState protoLearnerState = ProtoToGIFTConverter
                .convertToProto((LearnerState) message.getPayload(), domainSessionId);
        ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder().setLearnerState(protoLearnerState)
                .setTimestamp(ProtoToGIFTConverter.convertToProto(message.getTimeStamp())).build();
        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (JMSException e) {
            logger.error("There was a problem broadcasting a learner state.", e);
            errorMsg.append("There was a problem broadcasting the learner state due to: '").append(e.getMessage()).append("'.");
        }
    }

    /**
     * Handles an incoming {@link Message} of type
     * {@link MessageTypeEnum#LESSON_COMPLETED}.
     *
     * @param message The {@link Message} to handle. Must be of type
     *        {@link DomainSessionMessageInterface}.
     * @param errorMsg The {@link StringBuilder} to populate with any error
     *        messages. Can't be null.
     */
    private void handleLessonCompleted(Message message, StringBuilder errorMsg) {
        if (!(message instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The lesson completed message must be of type 'DomainSessionMessageInterface'.");
        }

        int domainSessionId = ((DomainSessionMessageInterface) message).getDomainSessionId();
        LessonCompletedProto.LessonCompleted protoLessonCompleted = LessonCompletedProto.LessonCompleted.newBuilder()
                .setDomainSessionId(domainSessionId).build();

        ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder().setLessonCompleted(protoLessonCompleted)
                .setTimestamp(ProtoToGIFTConverter.convertToProto(message.getTimeStamp())).build();
        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (Exception ex) {
            logger.error("There was a problem broadcasting the a lesson completed message.", ex);
        }
    }

    /**
     * Handles an incoming {@link Message} of type
     * {@link MessageTypeEnum#LESSON_STARTED}.
     *
     * @param message The {@link Message} to handle. Must be of type
     *        {@link DomainSessionMessageInterface}.
     * @param errorMsg The {@link StringBuilder} to populate with any error
     *        messages. Can't be null.
     */
    private void handleLessonStarted(Message message, StringBuilder errorMsg) {
        if (!(message instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The lesson started message must be of type 'DomainSessionMessageInterface'.");
        }

        int domainSessionId = ((DomainSessionMessageInterface) message).getDomainSessionId();
        LessonStartedProto.LessonStarted protoLessonStarted = LessonStartedProto.LessonStarted.newBuilder()
                .setDomainSessionId(domainSessionId).build();
        ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder().setLessonStarted(protoLessonStarted)
                .setTimestamp(ProtoToGIFTConverter.convertToProto(message.getTimeStamp())).build();
        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (Exception ex) {
            logger.error("There was a problem broadcasting the a lesson completed message.", ex);
        }
    }

    /**
     * Sends a request from the pedagogical module to the service bus.
     *
     * @param msg The {@link Message} which contains the
     *        {@link PedagogicalRequest} to send to the service bus. Can't be
     *        null.
     * @param errorMsg The {@link StringBuilder} to which error messages should
     *        be appended. Can't be null.
     */
    private void handlePedagogicalRequest(Message msg, StringBuilder errorMsg) {
        if (!(msg instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The pedagogical request message must be of type 'DomainSessionMessageInterface'.");
        }

        DomainSessionMessageInterface dsMessage = (DomainSessionMessageInterface) msg;
        PedagogicalRequest pedRequest = (PedagogicalRequest) msg.getPayload();
        PedagogicalRequestProto.PedagogicalRequest protoPedRequest;
        protoPedRequest = ProtoToGIFTConverter.convertToProto(pedRequest, dsMessage.getDomainSessionId());

        ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder()
                .setTimestamp(ProtoToGIFTConverter.convertToProto(msg.getTimeStamp()))
                .setPedagogicalRequest(protoPedRequest)
                .build();

        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (JMSException e) {
            errorMsg.append("There was a problem sending a pedagogical request: ").append(e.getMessage());
        }
    }

    /**
     * Sends filter data to the service bus.
     *
     * @param sensorData The {@link FilteredSensorData} to send to the service
     *        bus. Can't be null.
     * @param errorMsg The {@link StringBuilder} to which error messages should
     *        be appended. Can't be null.
     */
    private void handleSensorFilterData(FilteredSensorData sensorData, StringBuilder errorMsg) {
        // TODO: will be sent to the message broker in a future version
    }

    /**
     * Sends an authorize strategy request to the service bus.
     *
     * @param message The {@link Message} to handle. Must be of type
     *        {@link DomainSessionMessageInterface} and must contain a
     *        {@link MessageTypeEnum#AUTHORIZE_STRATEGIES_REQUEST}.
     * @param errorMsg The {@link StringBuilder} to which error messages should
     *        be appended. Can't be null.
     */
    private void handleAuthorizeStrategiesRequest(Message message, StringBuilder errorMsg) {
        if (!(message instanceof DomainSessionMessageInterface)) {
            throw new IllegalArgumentException(
                    "The authorize strategies request message must be of type 'DomainSessionMessageInterface'.");
        } else if (!(message.getPayload() instanceof AuthorizeStrategiesRequest)) {
            throw new IllegalArgumentException(
                    "The authorize strategies request message must contain an AuthorizeStrategiesRequest payload.");

        }

        final AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest protoRequest;
        try {
            int domainSessionId = ((DomainSessionMessageInterface) message).getDomainSessionId();
            protoRequest = ProtoToGIFTConverter.convertToProto((AuthorizeStrategiesRequest) message.getPayload(),
                    domainSessionId);
        } catch (Exception e) {
            logger.error("Unable to convert the AuthorizeStrategiesRequest.", e);
            errorMsg.append(
                    "There was a problem converting the AuthorizeStrategiesRequest due to: '" + e.getMessage() + "'.");
            return;
        }

        final ServiceBusMessage protoMessage = ServiceBusMessage.newBuilder()
                .setAuthorizeStrategiesRequest(protoRequest)
                .setTimestamp(ProtoToGIFTConverter.convertToProto(message.getTimeStamp())).build();

        try {
            broadcastClient.sendMessage(protoMessage);
        } catch (JMSException e) {
            logger.error("There was a problem broadcasting an AuthorizeStrategiesRequest.", e);
            errorMsg.append("There was a problem broadcasting the AuthorizeStrategiesRequest due to: '").append(e.getMessage()).append(
                    "'.");
        }
    }

    /**
     * Handle the {@link DomainOptionsRequest} message from the service bus. It
     * will be sent to the domain module to be processed. The response,
     * {@link DomainOptionsReply}, will be sent back along the service bus to
     * the external system.
     *
     * @param message the message to process. Can't be null and must contain a
     *        {@link DomainOptionsRequest}.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request.
     */
    private CompletableFuture<ServiceBusMessage> handleDomainOptionsRequest(final ServiceBusMessage message) {

        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        } else if (!message.hasDomainOptionsRequest()) {
            throw new IllegalArgumentException("The parameter 'message' must contain a DomainOptionsRequest.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("AdaptiveLearningService plugin received DomainOptionsRequest.");
        }

        final DomainOptionsRequestProto.DomainOptionsRequest protoDomainOptionsRequest = message
                .getDomainOptionsRequest();
        final DomainOptionsRequest domainOptionsRequest = ProtoToGIFTConverter
                .convertFromProto(protoDomainOptionsRequest);
        final String username = protoDomainOptionsRequest.getLMSUserName();

        return GatewayModule.getInstance().sendDomainOptionsRequest(username, domainOptionsRequest)
                .thenApply(domainOptionsList -> {
                    DomainOptionsReplyProto.DomainOptionsReply reply = ProtoToGIFTConverter
                            .convertToProto(domainOptionsList);
                    return ServiceBusMessage.newBuilder().setDomainOptionsReply(reply).build();
                }).exceptionally(err -> {
                    logger.error("There was a problem handling the DomainOptionsRequest protobuf message.", err);
                    NACKProto.NACK nack = NACKProto.NACK.newBuilder().setReason("Domain Options Request Error")
                            .setMessage(err.getMessage()).build();

                    return ServiceBusMessage.newBuilder().setNack(nack).build();
                });
    }

    /**
     * Handle the {@link DomainSelectionRequest} message from the service bus.
     * It will be sent to the domain module to be processed. The response,
     * {@link DomainSelectionReply}, will be sent back along the service bus to
     * the external system.
     *
     * @param message the message to process. Can't be null and must contain a
     *        {@link DomainSelectionRequest}.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request. Can be null if no response is generated.
     */
    private CompletableFuture<ServiceBusMessage> handleDomainSelectionRequest(final ServiceBusMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        } else if (!message.hasDomainSelectionRequest()) {
            throw new IllegalArgumentException("The parameter 'message' must contain a DomainSelectionRequest.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("AdaptiveLearningService plugin received DomainSelectionRequest.");
        }

        final DomainSelectionRequestProto.DomainSelectionRequest protoDomainSelectionRequest = message
                .getDomainSelectionRequest();
        final DomainSelectionRequest domainSelectionRequest = ProtoToGIFTConverter
                .convertFromProto(protoDomainSelectionRequest);
        final String userName = protoDomainSelectionRequest.getLmsUsername();

        return GatewayModule.getInstance().sendDomainSelectionRequest(userName, domainSelectionRequest)
                .thenApply(domainSession -> {

                    /* Build the protobuf version of the GIFT message. */
                    DomainSelectionReply.Builder builder = DomainSelectionReply.newBuilder();
                    builder.setDomainSession(ProtoToGIFTConverter.convertToProto(domainSession));
                    return ServiceBusMessage.newBuilder().setDomainSelectionReply(builder).build();
                }).exceptionally(err -> {
                    logger.error("There was a problem sending a domain selection.", err);
                    NACKProto.NACK nack = NACKProto.NACK.newBuilder().setReason("Domain Selection Error")
                            .setMessage(err.getMessage()).build();
                    return ServiceBusMessage.newBuilder().setNack(nack).build();
                });
    }

    /**
     * Handle the {@link ActiveDomainSessionsRequest} message from the service
     * bus. It will be sent to the domain module to be processed. The response,
     * {@link ActiveDomainSessionsReply}, will be sent back along the service
     * bus to the external system.
     *
     * @param message the message to process. Can't be null and must contain an
     *        {@link ActiveDomainSessionsRequest}.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request. Can be null if no response is generated.
     */
    private CompletableFuture<ServiceBusMessage> handleActiveDomainSessionsRequest(final ServiceBusMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        } else if (!message.hasActiveDomainSessionsRequest()) {
            throw new IllegalArgumentException("The parameter 'message' must contain an ActiveDomainSessionsRequest.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("AdaptiveLearningService plugin received ActiveDomainSessionsRequest.");
        }

        return GatewayModule.getInstance().sendActiveDomainSessionsRequest().thenApply(domainSessionList -> {

            /* Build the protobuf version of the GIFT message. */
            ActiveDomainSessionsReply.Builder builder = ActiveDomainSessionsReply.newBuilder();

            for (DomainSession dSession : domainSessionList.getDomainSessions()) {
                builder.addDomainSessions(ProtoToGIFTConverter.convertToProto(dSession));
            }

            return ServiceBusMessage.newBuilder().setActiveDomainSessionsReply(builder).build();
        }).exceptionally(err -> {
            NACKProto.NACK nack = NACKProto.NACK.newBuilder().setReason("Active Domain Sessions Request Error")
                    .setMessage(err.getMessage()).build();
            return ServiceBusMessage.newBuilder().setNack(nack).build();
        });
    }

    /**
     * Handle the {@link CloseDomainSessionRequest} message from the service
     * bus. It will be sent to the domain module to be processed.
     *
     * @param message the message to process. Can't be null and must contain an
     *        {@link CloseDomainSessionRequest}.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request. Can be null if no response is generated.
     */
    private CompletableFuture<ServiceBusMessage> handleCloseDomainSessionRequest(final ServiceBusMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        } else if (!message.hasCloseDomainSessionRequest()) {
            throw new IllegalArgumentException("The parameter 'message' must contain a CloseDomainSessionRequest.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("AdaptiveLearningService plugin received CloseDomainSessionRequest.");
        }

        final CloseDomainSessionRequestProto.CloseDomainSessionRequest protoCloseDomainSessionRequest = message
                .getCloseDomainSessionRequest();

        final CloseDomainSessionRequest closeDomainSessionRequest = ProtoToGIFTConverter
                .convertFromProto(protoCloseDomainSessionRequest);

        GatewayModule.getInstance().sendCloseDomainSessionRequest(closeDomainSessionRequest);
        return CompletableFuture.completedFuture(DEFAULT_PROCESSED_ACK_MSG);
    }
    
    /**
     * Handle the {@link generated.proto.gateway.EvaluatorUpdateRequestProto.EvaluatorUpdateRequest} message from the service bus.
     * It will be sent to the domain module to be processed.
     * 
     * @param evaluatorUpdateRequestMsg the protobuf message to process.  Can't be null and must contain an 
     * {@link generated.proto.gateway.EvaluatorUpdateRequestProto.EvaluatorUpdateRequest}.
     * @return The {@link CompletableFuture} that can be used to respond to the
     *         request. Can be null if no response is generated.
     */
    private CompletableFuture<ServiceBusMessage> handleEvaluatorUpdateRequest(ServiceBusMessage evaluatorUpdateRequestMsg){
        if (evaluatorUpdateRequestMsg == null) {
            throw new IllegalArgumentException("The parameter 'evaluatorUpdateRequestMsg' cannot be null.");
        } else if (!evaluatorUpdateRequestMsg.hasEvaluatorUpdateRequest()) {
            throw new IllegalArgumentException("The parameter 'evaluatorUpdateRequestMsg' must contain a EvaluatorUpdateRequest.");
        }
        
        if (logger.isInfoEnabled()) {
            logger.info("AdaptiveLearningService plugin received EvaluatorUpdateRequest.");
        }
        
        final EvaluatorUpdateRequestProto.EvaluatorUpdateRequest evaluatorUpdateRequestProto = evaluatorUpdateRequestMsg.getEvaluatorUpdateRequest();
        
        final EvaluatorUpdateRequest evaluatorUpdateRequest = ProtoToGIFTConverter.convertFromProto(evaluatorUpdateRequestProto);
        GatewayModule.getInstance().sendEvaluatorUpdateRequest(evaluatorUpdateRequest);
        return CompletableFuture.completedFuture(DEFAULT_PROCESSED_ACK_MSG);
        
    }

    /**
     * Handles an incoming
     * {@link generated.proto.gateway.ApplyStrategiesProto.ApplyStrategies} from
     * the service bus by sending an equivalent {@link ApplyStrategies} payload
     * to GIFT's message bus.
     *
     * @param msg The {@link ServiceBusMessage} to handle. Can't be null.
     * @return A null {@link CompletableFuture} to indicate that no response is
     *         expected.
     */
    private CompletableFuture<ServiceBusMessage> handleApplyStrategies(ServiceBusMessage msg) {
        final ApplyStrategiesProto.ApplyStrategies protoApplyStrategies = msg.getApplyStrategies();
        final ApplyStrategies applyStrategies = ProtoToGIFTConverter.convertFromProto(protoApplyStrategies);

        GatewayModule.getInstance().sendApplyStrategiesRequest(applyStrategies);
        return CompletableFuture.completedFuture(DEFAULT_PROCESSED_ACK_MSG);
    }

    @Override
    public boolean isEnabled() {
        return apiClient.isOnline() && broadcastClient.isOnline();
    }

    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations() {
        return reqTrainingAppConfigurations;
    }

    @Override
    public Serializable getScenarios() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() throws DetailedException {
        return null;
    }

    @Override
    public void selectObject(Serializable objectIdentifier) throws DetailedException {
        // not supported
    }

    @Override
    public void loadScenario(String scenarioIdentifier) throws DetailedException {
        // not supported
    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }

    @Override
    public void cleanup() {
        disconnectClients();
    }

    /**
     * Creates the various {@link ProtobufMessageClient} to connect to the external message broker.
     *
     * @param connectionUrl The URL of the service bus to which to connect. E.g. tcp://127.0.0.1:61618
     * @throws JMSException if there was an error connecting one of the clients.
     */
    private void createClients(String connectionUrl) throws JMSException {
        if (logger.isTraceEnabled()) {
            logger.trace("createClients(" + connectionUrl + ")");
        }

        final boolean isRTA = LessonLevelEnum.RTA.equals(CommonProperties.getInstance().getLessonLevel());

        if (apiClient == null) {
            final ServiceBusMessage msgTemplate = ServiceBusMessage.getDefaultInstance();
            apiClient = new ProtobufQueueMessageClient<>(connectionUrl, "Api", false, msgTemplate, DEFAULT_ERROR_MSG, DEFAULT_ACK_MSG);
        }

        if (!apiClient.isOnline()) {
            apiClient.setMessageHandler(this::handleServiceBusMessage);
            try {
                apiClient.connect(false); // don't attempt to reconnect, let the exception happen so it can be handled if it needs to be
            } catch (JMSException jmsEx) {
                if (isRTA) {
                    throw jmsEx;
                }
            }
        }

        if (broadcastClient == null) {
            final ServiceBusMessage msgTemplate = ServiceBusMessage.getDefaultInstance();
            broadcastClient = new ProtobufQueueMessageClient<>(connectionUrl, "Broadcast", false, msgTemplate,
                    DEFAULT_ERROR_MSG, DEFAULT_ACK_MSG);
        }

        if (!broadcastClient.isOnline()) {
            try {
                broadcastClient.connect(false); // don't attempt to reconnect, let the exception happen so it can be handled if it needs to be
            } catch (JMSException jmsEx) {
                if (isRTA) {
                    throw jmsEx;
                }
            }
        }
    }

    /**
     * Disconnects the {@link #apiClient} and/or the {@link #broadcastClient} if
     * they are connected.
     */
    private void disconnectClients() {
        if (apiClient != null && apiClient.isOnline()) {
            apiClient.disconnect(true);
        }

        if (broadcastClient != null && broadcastClient.isOnline()) {
            broadcastClient.disconnect(true);
        }
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[AdaptiveLearningServiceInterface: ");
        sb.append(super.toString());
        sb.append(", connection = ").append(connectionUrl);

        sb.append(", messageTypes = {");
        for(MessageTypeEnum mType : supportedMsgTypes){
            sb.append(mType ).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
