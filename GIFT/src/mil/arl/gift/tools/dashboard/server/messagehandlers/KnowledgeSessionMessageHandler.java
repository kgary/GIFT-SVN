/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.messagehandlers;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.dkf.Actions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices;
import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import mil.arl.gift.common.EndKnowledgeSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.course.strategy.StrategyStateUpdate;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.common.ta.state.RemoveEntity;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StrategyUtil;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.tools.dashboard.server.DashboardBrowserWebSession;
import mil.arl.gift.tools.dashboard.server.KnowledgeSessionMessageManager;
import mil.arl.gift.tools.dashboard.server.UserSessionManager;
import mil.arl.gift.tools.dashboard.server.UserWebSession;
import mil.arl.gift.tools.dashboard.server.WebMonitorModule;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.shared.messages.AddKnowledgeSession;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.DetonationUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.GatewayConnection;
import mil.arl.gift.tools.dashboard.shared.messages.GeolocationUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.InitializationMessage;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.RemoveEntityMessage;
import mil.arl.gift.tools.dashboard.shared.messages.SessionEntityIdentifier;

/**
 * Interface used to represent message handlers for ActiveMQ messages.
 *
 * @author nblomberg
 *
 */
public class KnowledgeSessionMessageHandler implements MessageHandlerInterface {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(KnowledgeSessionMessageHandler.class);

    /** The entity filter that is scoped to this browser session */
    private final SessionEntityFilter sessionEntityFilter;

    /**
     * Constructor
     *
     * @param browserSessionKey The unique identifier of the browser session
     *        this {@link KnowledgeSessionMessageHandler} is scoped to.
     */
    public KnowledgeSessionMessageHandler(String browserSessionKey) {
        sessionEntityFilter = SessionEntityFilter.getInstance(browserSessionKey);
    }


    @Override
    public void handleMessage(final DashboardBrowserWebSession browserSession, final DomainSessionMessageInterface domainMsg) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleMessage()");
        }

        if (browserSession == null) {
            return;
        }

        final Object payload = domainMsg.getPayload();

        /* Handle message for a newly created knowledge session */
        if (payload instanceof KnowledgeSessionCreated) {
            KnowledgeSessionCreated ksCreated = (KnowledgeSessionCreated) payload;
            handleMessage(browserSession, domainMsg, ksCreated.getKnowledgeSession());
            return;
        }

        /* Attempt to retrieve the knowledge session for the ending session; if
         * not found, just process the message anyway */
        final boolean isEnding = domainMsg.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST
                || domainMsg.getMessageType() == MessageTypeEnum.LESSON_COMPLETED;

        /* Retrieve knowledge session for the domain */
        DomainSessionKey key = new DomainSessionKey(domainMsg);

        /* Gets the knowledge session and handles the message */
        WebMonitorModule.getInstance().getDomainInformationCache().getCachedKnowledgeSession(key).thenAccept(knowledgeSession -> {
            if (knowledgeSession != null || isEnding) {
                handleMessage(browserSession, domainMsg, knowledgeSession);
            }
        }).exceptionally(caught -> {
            logger.error("There was an error while retrieving the knowledge session for domain session '" + key + "'.",
                    caught);
            return null;
        });
    }

    /**
     * Allow feature/implementation specific handling of ActiveMQ messages.
     *
     * @param browserSession the browser session that is requesting the message
     *        to be processed.
     * @param domainMsg the domain session message.
     * @param knowledgeSession the knowledge session that the message is being
     *        applied to.
     */
    private void handleMessage(DashboardBrowserWebSession browserSession, DomainSessionMessageInterface domainMsg, AbstractKnowledgeSession knowledgeSession) {
        String bsk = browserSession.getBrowserSessionKey();
        int userId = domainMsg.getUserId();
        int domainSessionId = domainMsg.getDomainSessionId();
        MessageTypeEnum msgType = domainMsg.getMessageType();
        Object payload = domainMsg.getPayload();
        long msgTimestamp = domainMsg.getTimeStamp();

        Serializable clientPayload = null;

        DomainSessionKey key = new DomainSessionKey(domainMsg);

        if (payload instanceof KnowledgeSessionCreated) {
            KnowledgeSessionCreated ksCreated = (KnowledgeSessionCreated) payload;
            WebMonitorModule.getInstance().sendGameStateMessageToGateway(ksCreated, MessageTypeEnum.KNOWLEDGE_SESSION_CREATED);
            clientPayload = new AddKnowledgeSession(ksCreated.getKnowledgeSession());
        } else if (payload instanceof InitializePedagogicalModelRequest) {

            /* If an InitializePedagogicalModelRequest has arrived after a game
             * master has attached, send the InitializationMessage again so that
             * they receive the list of available strategies. */
            InitializePedagogicalModelRequest initPedRequest = (InitializePedagogicalModelRequest) payload;
            LinkedHashMap<Strategy, Boolean> strategyAssociationMap = convertPedagogicalRequestToStrategyLookup(
                    initPedRequest);
            LearnerState lastLearnerState = WebMonitorModule.getInstance().getDomainInformationCache().getLastLearnerState(key);

            KnowledgeSessionState state = new KnowledgeSessionState(lastLearnerState);
            state.setCachedTaskStates(WebMonitorModule.getInstance().getDomainInformationCache().getCachedTaskStates(key));
            state.setCachedProcessedStrategy(WebMonitorModule.getInstance().getDomainInformationCache().getCachedProcessedStrategies(key));

            clientPayload = new InitializationMessage(strategyAssociationMap, state);
        } else if (payload instanceof LearnerState) {
            final LearnerState learnerState = (LearnerState) payload;
            WebMonitorModule.getInstance().sendGameStateMessageToGateway(learnerState, MessageTypeEnum.LEARNER_STATE);

            KnowledgeSessionState state = new KnowledgeSessionState(learnerState);
            state.setCachedTaskStates(WebMonitorModule.getInstance().getDomainInformationCache().getCachedTaskStates(key));

            /* if a learner state update is received, push it to the client do
             * that the game master view is updated */
            clientPayload = state;
        } else if (payload instanceof ApplyStrategies){
            // apply strategies message is sent from the game master for all strategies to apply
            // This includes auto apply strategies, manually approved strategies and manually applied strategies (e.g. strategy preset)

            ApplyStrategies applyStrategies = (ApplyStrategies)payload;

            StrategyStateUpdate updateMsg = new StrategyStateUpdate(applyStrategies.getEvaluator());
            
            for (StrategyToApply entry : applyStrategies.getStrategies()) {
                final String reason = entry.getTrigger();

                List<Strategy> applied = updateMsg.getAppliedStrategies().get(reason);
                if(applied == null){
                    applied = new ArrayList<>();
                    updateMsg.getAppliedStrategies().put(reason, applied);
                }

                Strategy strategy = entry.getStrategy();
                final List<Serializable> strategyActivities = strategy.getStrategyActivities();
                if (CollectionUtils.isEmpty(strategyActivities)) {
                    continue;
                }

                applied.add(strategy);
            }

                clientPayload = updateMsg;
            

        } else if (payload instanceof AuthorizeStrategiesRequest) {
            // authorize strategies request message is only sent if the domain module
            // is requesting authorization to apply strategies 
            // It is not sent if the game master user sends a strategy that was not requested, i.e. strategy preset
            AuthorizeStrategiesRequest request = (AuthorizeStrategiesRequest) payload;

            StrategyStateUpdate updateMsg = new StrategyStateUpdate(request.getEvaluator());

            for (Entry<String, List<StrategyToApply>> entry : request.getRequests().entrySet()) {
                final String reason = entry.getKey();

                List<Strategy> applied = new ArrayList<>();
                List<Strategy> pending = new ArrayList<>();

                for (StrategyToApply strategyToApply : entry.getValue()) {
                    Strategy strategy = strategyToApply.getStrategy();
                    final List<Serializable> strategyActivities = strategy.getStrategyActivities();
                    if (CollectionUtils.isEmpty(strategyActivities)) {
                        continue;
                    }

                    /* If it is mandatory or only contains activities directed
                     * at the controller, add it to the process list and move on
                     * to the next strategy */
                    if (StrategyUtil.isMandatory(strategyActivities.get(0))
                            || StrategyUtil.isToControllerOnly(strategy)) {
                        applied.add(strategy);
                    } else {
                        pending.add(strategy);
                    }
                }

                if (!applied.isEmpty()) {
                    updateMsg.getAppliedStrategies().put(reason, applied);
                }

                if (!pending.isEmpty()) {
                    updateMsg.getPendingStrategies().put(reason, pending);
                }
            }

            clientPayload = updateMsg;
        } else if (payload instanceof ExecuteOCStrategy) {
            clientPayload = (ExecuteOCStrategy) payload;
        } else if (payload instanceof Geolocation) {

            Geolocation geolocation = (Geolocation) payload;
            if (logger.isDebugEnabled()) {
                logger.debug("Sending geolocation to client: " + geolocation);
            }

            if(geolocation.getCoordinates() != null) {

                //pass the client a version of this location that can be rendered to a map
                GeolocationUpdate geolocationMessage = new GeolocationUpdate(
                        knowledgeSession.getHostSessionMember().getDomainSessionId(),
                        new mil.arl.gift.tools.map.shared.GDC(
                                geolocation.getCoordinates().getLatitude(),
                                geolocation.getCoordinates().getLongitude(),
                                geolocation.getCoordinates().getElevation()
                        ),
                        geolocation.getAccuracy(),
                        geolocation.getAltitudeAccuracy(),
                        geolocation.getHeading(),
                        geolocation.getSpeed());
                geolocationMessage.getLearnerInfo().setUsername(knowledgeSession.getHostSessionMember().getSessionMembership().getUsername());

                clientPayload = geolocationMessage;
            }

        } else if(payload instanceof RemoveEntity){

            RemoveEntity removeEntity = (RemoveEntity)payload;
            if (logger.isDebugEnabled()) {
                logger.debug("Sending remove entity to client: " + removeEntity);
            }

            RemoveEntityMessage removeEntityMessage = new RemoveEntityMessage(removeEntity,
                    new SessionEntityIdentifier(
                            removeEntity.getReceivingId().getSimulationAddress().getApplicationID(),
                            knowledgeSession.getHostSessionMember().getDomainSessionId(),
                            removeEntity.getReceivingId().getEntityID()
            ));

            clientPayload = removeEntityMessage;
        } else if (payload instanceof EntityState) {


                final EntityState entityState = (EntityState) payload;
                final DomainSessionEntityFilter dsef = sessionEntityFilter.getDomainSessionFilter(key);
                if (dsef != null) {
                    dsef.handleEntityUpdate(msgTimestamp, entityState);
                } else if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Unable to handle an EntityState for domain session {} because a filter doesn't exist for it",
                            domainSessionId);
                }


        } else if(payload instanceof Detonation) {

            //only handle game states from Gateway to avoid duplication when the Domain is playing back entity states

            Detonation detonation = (Detonation) payload;

            final DomainSessionEntityFilter dsef = sessionEntityFilter.getDomainSessionFilter(key);
            mil.arl.gift.tools.map.shared.GDC firingEntityMap = null;
            Integer firingEntityForceId = null;
            if(dsef != null){
                Point3d firingEntityLoc = dsef.getLatestEntityLocation(detonation.getFiringEntityID());
                
                if(firingEntityLoc != null){
                    GCC firingEntityGCC = new GCC(firingEntityLoc.getX(), firingEntityLoc.getY(), firingEntityLoc.getZ());
                    GDC firingEntityGDC = CoordinateUtil.getInstance().convertToGDC(firingEntityGCC);
                    firingEntityMap = 
                            new mil.arl.gift.tools.map.shared.GDC(firingEntityGDC.getLatitude(), firingEntityGDC.getLongitude(), firingEntityGDC.getElevation());
                }
                
                /* Determine the force/affiliation of the firing entity. */
                firingEntityForceId = dsef.getForceId(detonation.getFiringEntityID());
            }

            GCC gccLocation = new GCC(detonation.getLocation().getX(),
                    detonation.getLocation().getY(),
                    detonation.getLocation().getZ());

            GDC gdcLocation = CoordinateUtil.getInstance().convertToGDC(gccLocation);

            //pass the client a version of this location that can be rendered to a map
            clientPayload = new DetonationUpdate(
					key.getDomainSessionId(),
                    new mil.arl.gift.tools.map.shared.GDC(
                            gdcLocation.getLatitude(), gdcLocation.getLongitude(), gdcLocation.getElevation()),
                            detonation.getDetonationResult(),
                            firingEntityMap,
                            firingEntityForceId
            );

            /* forward game state to any connected training applications used as
             * external monitors */
            WebMonitorModule.getInstance().sendGameStateMessageToGateway(detonation, MessageTypeEnum.DETONATION);

        } else if(payload instanceof WeaponFire) {

            /* forward game state to any connected training applications used as
             * external monitors */
            WebMonitorModule.getInstance().sendGameStateMessageToGateway(payload, MessageTypeEnum.WEAPON_FIRE);

        } else if (payload instanceof Geolocation) {

            Geolocation geolocation = (Geolocation) payload;

            if(geolocation.getCoordinates() != null) {

                //pass the client a version of this location that can be rendered to a map
                GeolocationUpdate geolocationMessage = new GeolocationUpdate(
                        userId,
                        new mil.arl.gift.tools.map.shared.GDC(
                                geolocation.getCoordinates().getLatitude(),
                                geolocation.getCoordinates().getLongitude(),
                                geolocation.getCoordinates().getElevation()
                        ),
                        geolocation.getAccuracy(),
                        geolocation.getAltitudeAccuracy(),
                        geolocation.getHeading(),
                        geolocation.getSpeed());

                clientPayload = geolocationMessage;
            }

        } else if (msgType == MessageTypeEnum.LESSON_COMPLETED
                || msgType == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {


            /* if a learner state update is received, push it to the client so that the game master
             * view is updated */
            clientPayload = new EndKnowledgeSessionRequest();

            /* Deregister if it's not a 'Past' session */
            if (knowledgeSession != null && !knowledgeSession.inPastSessionMode()) {
                KnowledgeSessionMessageManager.getInstance().deregisterBrowser(bsk, knowledgeSession);
            }

            //determine if any training applications are being used as external monitors
            GatewayConnection gatewayConnection = WebMonitorModule.getInstance().getGatewayConnection();
            if(gatewayConnection != null) {

                //get the user whose browser session is handling this message
                UserWebSession userSession = UserSessionManager.getInstance().getUserSession(browserSession.getUserSessionKey());
                if(userSession != null) {

                    String userName = userSession.getUserSessionInfo().getUserName();
                    try {

                        //clean up the current external monitor connections as they are closed in the Gateway
                        WebMonitorModule.getInstance().setGatewayConnections(null, userName);

                        //attempt to reconnect to the external monitors once the previous connection has been cleaned up
                        WebMonitorModule.getInstance().setGatewayConnections(gatewayConnection, userName);

                    } catch (Exception e) {
                        logger.error("Unable to reconnect to training application for user '"
                                + userName + "' due to an exception.", e);
                    }
                }
            }
        }

        if (clientPayload != null) {
            DashboardMessage clientMessage;
            if (knowledgeSession != null) {
                clientMessage = new DashboardMessage(clientPayload, knowledgeSession, msgTimestamp);
            } else {
                clientMessage = new DashboardMessage(clientPayload, domainSessionId, msgTimestamp);
            }

            clientMessage.setBrowserSessionKey(bsk);
            browserSession.sendWebSocketMessage(clientMessage);
        }
    }

    /**
     * Converts an {@link InitializePedagogicalModelRequest} containing the
     * scenario {@link Strategy strategies} into a mapping of {@link Strategy
     * strategies} to whether or not they have an associated
     * {@link StateTransition}.
     *
     * @param initPedRequest The {@link InitializePedagogicalModelRequest} to
     *        convert. Can't be null.
     * @return The mapping of {@link Strategy} to whether or not it has an
     *         association with a {@link StateTransition}. The map will never be
     *         null. If The {@link InitializePedagogicalModelRequest} contains
     *         course actions instead of scenario actions, an empty map will be
     *         returned.
     */
    private LinkedHashMap<Strategy, Boolean> convertPedagogicalRequestToStrategyLookup(InitializePedagogicalModelRequest initPedRequest) {
        if (initPedRequest == null) {
            throw new IllegalArgumentException("The parameter 'initPedRequest' cannot be null.");
        }

        LinkedHashMap<Strategy, Boolean> toRet = new LinkedHashMap<>();

        /* We only care about DKF actions */
        if (initPedRequest.isCourseActions()) {
            return toRet;
        }

        try {
            /* Deserializes the XML string */
            byte[] actionsBytes = initPedRequest.getActions().getBytes();
            UnmarshalledFile actionsFile = AbstractSchemaHandler.parseAndValidate(Actions.class,
                    new ByteArrayInputStream(actionsBytes), (java.io.File) null, true);

            final Actions actions = (Actions) actionsFile.getUnmarshalled();
            List<Strategy> strategies = actions.getInstructionalStrategies().getStrategy();

            LinkedHashMap<String, Strategy> nameToStrategyMap = new LinkedHashMap<>();
            for (Strategy strategy : strategies) {
                nameToStrategyMap.put(strategy.getName(), strategy);
                toRet.put(strategy, false);
            }

            /* Find strategy associations with the state transitions */
            List<StateTransition> stateTransitions = actions.getStateTransitions() == null ? null
                    : actions.getStateTransitions().getStateTransition();
            if (stateTransitions != null) {
                for (StateTransition stateTransition : stateTransitions) {
                    StrategyChoices choices = stateTransition.getStrategyChoices();
                    if (choices == null) {
                        continue;
                    }

                    for (StrategyRef strategyRef : choices.getStrategies()) {
                        Strategy transitionStrategy = nameToStrategyMap.get(strategyRef.getName());
                        if (transitionStrategy != null) {
                            toRet.put(transitionStrategy, true);
                        }
                    }
                }
            }
        } catch (FileNotFoundException | JAXBException | SAXException e) {
            logger.error("There was an error while handling the " + initPedRequest + " message payload.", e);
        }

        return toRet;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[KnowledgeSessionMessageHandler: ");
        sb.append("]");
        return sb.toString();
    }

}
