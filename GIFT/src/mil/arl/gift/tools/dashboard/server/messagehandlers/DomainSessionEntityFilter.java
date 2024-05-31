/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.messagehandlers;

import static mil.arl.gift.common.util.StringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nps.moves.disutil.EulerConversions;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityMarking;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.server.EntityTypeToSidcConverter;
import mil.arl.gift.tools.dashboard.server.WebMonitorModule;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.server.gamemaster.MessageFrequencyThrottle;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.LearnerEntityInfo;
import mil.arl.gift.tools.dashboard.shared.messages.SessionEntityIdentifier;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * An entity filter that is scoped to a domain session within the scope of a
 * game master browser session.
 *
 * @author tflowers
 *
 */
public class DomainSessionEntityFilter {

    /**
     * A metadata object containing cached information about a particular
     * entity.
     *
     * @author tflowers
     *
     */
    private class EntityMetadata {

        /**
         * The number of seconds at which an entity should 'expire' after not
         * receiving additional {@link EntityState} payloads
         */
        private static final int ENTITY_TIMEOUT = 30;

        /** The identifier of the entity that is being described */
        private final EntityIdentifier entityId;

        /** The last state that was received for the entity */
        private EntityState lastState;

        /**
         * The current scheduled future that executes timeout logic if necessary
         */
        private ScheduledFuture<?> timeoutFuture;

        /**
         * Constructs an initialized {@link EntityMetadata} describing an entity
         * of a provided id.
         *
         * @param entityId The id of the entity being described.
         */
        public EntityMetadata(EntityIdentifier entityId) {
            if (logger.isInfoEnabled()) {
                logger.info("Creating an " + EntityMetadata.class.getSimpleName() + " for " + entityId);
            }

            this.entityId = entityId;
        }

        /**
         * Getter for the lastState.
         *
         * @return The value of {@link #lastState}.
         */
        public EntityState getLastState() {
            return lastState;
        }

        /**
         * Setter for the last {@link EntityState} that was received for the
         * described entity.
         *
         * @param lastState The new value of {@link #lastState}. Can't be null.
         */
        public void setLastState(EntityState lastState) {
            this.lastState = lastState;
            if (lastState.getAppearance().isActive()) {
                scheduleTimeout();
            } else {
                tryCancelTimeout();
                drop();
            }
        }

        /**
         * Drops this {@link EntityMetadata} from the
         * {@link DomainSessionEntityFilter} and alerts the downstream client
         * that the entity has been deactivated.
         */
        public void drop() {
            if (logger.isTraceEnabled()) {
                logger.trace("drop()");
            }

            synchronized (DomainSessionEntityFilter.this) {
                /* Send a message to the client to hide the entity. */
                final EntityStateUpdate payload = buildClientPayload(lastState, false);
                sendMessage(lastReceivedTimeStamp, payload, true);

                entityIdToMetadata.remove(entityId);

                Iterator<Map.Entry<EntityIdentifier, EntityIdentifier>> iter = teamIdToPrincipleEntityId.entrySet()
                        .iterator();

                /* Removes the principle entity mappings for all the teams who
                 * were using the removed entity as its principle member. */
                while (iter.hasNext()) {
                    Map.Entry<EntityIdentifier, EntityIdentifier> entry = iter.next();
                    if (entry.getValue().equals(entityId)) {
                        iter.remove();
                    }
                }
            }
        }

        /**
         * Schedules (or reschedules) a timeout for this entity.
         */
        public synchronized void scheduleTimeout() {
            /* Cancel an existing timeout if it exists */
            tryCancelTimeout();

            /* Schedule a new timeout that will remove this entity state from
             * the outer class' map */
            timeoutFuture = threadPool.schedule(this::onTimeout, ENTITY_TIMEOUT, TimeUnit.SECONDS);
        }

        /**
         * Attempt to cancel the timeout behavior if possible.
         *
         * @return True if there was a timeout to cancel, false otherwise.
         */
        public synchronized boolean tryCancelTimeout() {
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
                timeoutFuture = null;
                return true;
            }

            return false;
        }

        /**
         * Invoked when the entity this {@link EntityMetadata} describes has
         * time out.
         */
        private void onTimeout() {
            if (logger.isInfoEnabled()) {
                logger.info("Timeout occurred for " + entityId + ". Last received state was " + lastState);
            }

            drop();
        }
    }

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(DomainSessionEntityFilter.class);

    /**
     * The converter that is used for mapping {@link EntityType} objects to
     * their {@link SIDC} equivalent.
     */
    private final EntityTypeToSidcConverter entityTypeToSidcConverter = EntityTypeToSidcConverter.getInstance();

    /**
     * The thread pool on which scheduled actions for
     * {@link DomainSessionEntityFilter} objects are enqueued.
     */
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new ThreadFactory() {

        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            final String name = DomainSessionEntityFilter.class.getSimpleName() + " Heart Beat " + counter.getAndIncrement();
            final Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        }
    });

    /**
     * to use as team velocity and orientation in a team entity state message
     */
    private static final Vector3d ZERO_VEC = new Vector3d();

    /** to use as team articulated parameters in a team entity state message */
    private static final List<ArticulationParameter> EMPTY_ARTICULATED_PARAMS = new ArrayList<>(0);

    /** to use as team appearance in a team entity state message */
    private static final EntityAppearance DEFAULT_ENTITY_APPEARANCE = new EntityAppearance(DamageEnum.HEALTHY,
            PostureEnum.UNUSED);

    /** A mapping of an entity's id to its metadata. */
    private final Map<EntityIdentifier, EntityMetadata> entityIdToMetadata = new ConcurrentHashMap<>();

    /**
     * A mapping of the {@link EntityIdentifier} of a {@link Team} to the
     * {@link EntityIdentifier} of its <i>principle member</i>. A principle
     * member is the first member of the team for whom an {@link EntityState}
     * was received.
     */
    private final Map<EntityIdentifier, EntityIdentifier> teamIdToPrincipleEntityId = new ConcurrentHashMap<>();

    /** Maps GIFT role name to whether or not it should be shown. */
    private final Map<String, Boolean> roleNameToShouldShow = new ConcurrentHashMap<>();

    /** Maps GIFT role names to the last reported location of the entity */
    private final Map<String, Point3d> roleNameToLocation = new ConcurrentHashMap<>();

    /** The unique key identifying the domain session that is being filtered. */
    private final DomainSessionKey domainSessionKey;

    /** The websocket to which all filtered updates are sent. */
    private final AbstractServerWebSocket webSocket;

    /** The id to use as the application id for GIFT */
    private Integer applicationId = null;

    /** Tracks what the next id value for a team is */
    private static final AtomicInteger nextTeamId = new AtomicInteger(0);

    /** The simulation time of the last received message */
    private long lastReceivedTimeStamp = Long.MIN_VALUE;

    /**
     * The future that is responsible for pinging the client with the latest
     * entity states.
     */
    private ScheduledFuture<?> heartbeatFuture = null;

    /**
     * Constructs a {@link DomainSessionEntityFilter} for a provided
     * {@link AbstractKnowledgeSession} that outputs to a provided
     * {@link AbstractServerWebSocket}.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which {@link EntityState} messages are being filtered.
     * @param webSocket The {@link AbstractServerWebSocket} to which the
     *        filtered messages should be sent.
     */
    DomainSessionEntityFilter(DomainSessionKey domainSessionKey, AbstractServerWebSocket webSocket) {
        if (webSocket == null) {
            throw new IllegalArgumentException("The parameter 'webSocket' cannot be null.");
        }

        this.domainSessionKey = domainSessionKey;
        this.webSocket = webSocket;
    }

    /**
     * Handles an incoming {@link EntityState} message payload for a given
     * {@link DashboardBrowserWebSession} listening to a given
     * {@link AbstractKnowledgeSession}. This method will check the
     * {@link EntityState} against the current filter configuration and will
     * send messages to the client to update the locations of both the specified
     * entity and any teams to which the entity belongs.
     *
     * @param time The epoch time in milliseconds at which this message was
     *        sent.
     * @param state The {@link EntityState} to handle. Can't be null.
     */
    void handleEntityUpdate(long time, EntityState state) {
        handleEntityUpdate(time, state, false);
    }

    /**
     * Handles an incoming {@link EntityState} message payload for a given
     * {@link DashboardBrowserWebSession} listening to a given
     * {@link AbstractKnowledgeSession}. This method will check the
     * {@link EntityState} against the current filter configuration and will
     * send messages to the client to update the locations of both the specified
     * entity and any teams to which the entity belongs.
     *
     * @param time The epoch time in milliseconds at which this message was
     *        sent.
     * @param state The {@link EntityState} to handle. Can't be null.
     * @param sendStateForAllEntities A flag indicating if entities that don't
     *        match the filter should have deactivate states sent when they
     *        don't match the filter.
     */
    private synchronized void handleEntityUpdate(long time, EntityState state, boolean sendStateForAllEntities) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(time, state, sendStateForAllEntities);
            logger.trace("handleEntityUpdate(" + join(", ", params) + ")");
        }

        /* Unpack the entity state */
        final EntityIdentifier entityId = state.getEntityID();
        final EntityMarking entityMarking = state.getEntityMarking();
        final String entityMarkingValue = entityMarking != null ? entityMarking.getEntityMarking() : null;

        /* Update the position of the specified entity */
        indexState(time, state);

        /* Determine the team or team member for this entity update if it
         * exists. */
        AbstractKnowledgeSession knowledgeSession = getKnowledgeSession();
        if(knowledgeSession == null) {
            logger.warn("Unable to handle entity update because domain session " + domainSessionKey
                    + "'s entity filter does not have an associated knowledge session.");
            return;
        }

        final Team team = knowledgeSession.getTeamStructure();
        AbstractTeamUnit referencedUnit = null;
        if (team != null) {
            referencedUnit = team.getTeamMemberByEntityMarking(entityMarkingValue);

            /* If the referenced unit has still not been determined, try finding
             * it by entity id */
            if (referencedUnit == null) {
                referencedUnit = team.getTeamElementByEntityId(entityId);
            }
        }

        /* If the entity isn't a team member, only show the entity if there
         * isn't a filter applied and return early. */
        if (referencedUnit == null) {
            if (!isFilterApplied()) {
                EntityStateUpdate payload = buildClientPayload(state);
                sendMessage(time, payload, false);
            } else if (sendStateForAllEntities) {
                EntityStateUpdate payload = buildClientPayload(state, false);
                sendMessage(time, payload, true);
            }

            /* There is no team structure to walk up for this entity so return
             * early. */
            return;
        }

        /* Since the entity belongs to the team structure, send the message to
         * the client if the team or team member passes the current filter. */
        if (referencedUnit instanceof TeamMember<?>) {
            TeamMember<?> referencedMember = (TeamMember<?>) referencedUnit;
            referencedMember.setEntityIdentifier(entityId);
            if (passesFilter(referencedMember)) {
                EntityStateUpdate payload = buildClientPayload(state);
                sendMessage(time, payload, false);
                return;
            } else if (sendStateForAllEntities) {
                EntityStateUpdate payload = buildClientPayload(state, false);
                sendMessage(time, payload, true);
            }
        } else if (referencedUnit instanceof Team) {
            Team referencedTeam = (Team) referencedUnit;
            if (isFilterApplied() && passesFilter(referencedTeam)) {
                EntityStateUpdate payload = buildClientPayload(state);
                sendMessage(time, payload, false);
                return;
            } else if (sendStateForAllEntities) {
                EntityStateUpdate payload = buildClientPayload(state, false);
                sendMessage(time, payload, true);
            }
        }

        /* Since the entity didn't pass the filter, walk up the team hierarchy
         * and see if any of the parent teams pass the current filter */
        for (Team currTeam = referencedUnit.getParentTeam(); currTeam != null; currTeam = currTeam.getParentTeam()) {

            /* Calculate/get the identifier of the team */
            EntityIdentifier teamEntityId = currTeam.getEntityIdentifier();
            if (teamEntityId == null) {
                final int siteId = entityId.getSimulationAddress().getSiteID();
                teamEntityId = createTeamIdentifier(siteId);
                currTeam.setEntityIdentifier(teamEntityId);
            }

            /* If an entity state needs to be sent for the team, create it and
             * send it */
            if (passesFilter(currTeam)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} passed the team filter", currTeam.getName());
                }

                EntityState teamEntityState = createTeamEntityState(state, teamEntityId, currTeam);

                entityIdToMetadata.computeIfAbsent(teamEntityId, EntityMetadata::new).setLastState(teamEntityState);

                EntityStateUpdate payload = buildClientPayload(teamEntityState);
                sendMessage(time, payload, false);

                /* If we don't have to send a state for all entities we can
                 * break here. */
                if (!sendStateForAllEntities) {
                    break;
                }
            } else if (sendStateForAllEntities) {
                EntityState teamEntityState = createTeamEntityState(state, teamEntityId, currTeam);
                EntityStateUpdate payload = buildClientPayload(teamEntityState, false);
                sendMessage(time, payload, true);
            }
        }
    }

    /**
     * Creates an {@link EntityIdentifier} for a {@link Team} using a given site
     * ID.
     *
     * @param siteId The value of the site ID to use within the generated
     *        {@link EntityIdentifier}.
     * @return The {@link EntityIdentifier} that was generated. Can't be null.
     */
    private EntityIdentifier createTeamIdentifier(int siteId) {
        return new EntityIdentifier(new SimulationAddress(siteId, applicationId), nextTeamId.getAndIncrement());
    }

    /**
     * Create an entity state that represents a team with the data from the
     * provided entity state and the given location.
     *
     * @param individualEntityState the entity state to use as a basis for the
     *        new team entity state being returned.<br/>
     *        Only force ID and entity type are currently used.
     * @param teamEntityId the team entity id that the new entity state will
     *        represent.
     * @param team The {@link Team} for which the {@link EntityState} is being
     *        created. Can't be null.
     * @return the newly created team entity state.
     */
    private synchronized EntityState createTeamEntityState(EntityState individualEntityState, EntityIdentifier teamEntityId, Team team) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(individualEntityState, teamEntityId, team);
            logger.trace("createTeamEntityState(" + join(", ", params) + ")");
        }

        /* If the team doesn't have a principle member yet. Assign it this
         * one */
        if (!teamIdToPrincipleEntityId.containsKey(teamEntityId)) {
            teamIdToPrincipleEntityId.put(teamEntityId, individualEntityState.getEntityID());
        }

        final Point3d location = getTeamLocation(team);
        final Vector3d orientation = getTeamOrientation(team);
        final EchelonEnum echelon = team.getEchelon();
        final String name = team.getName();

        if (entityIdToMetadata.containsKey(teamEntityId)) {
            /* Return a copy of the last received state with a modified
             * location. */
            final EntityState lastState = entityIdToMetadata.get(teamEntityId).getLastState();
            return lastState.replaceLocation(location).replaceOrientation(orientation);
        } else {

            //ensure team entities are visually represented using similar SIDC codes with the appropriate echelon indicators
            final EntityType teamEntityType = new EntityType(EntityType.ENTITY_TYPE_LIFEFORM, 0, 0, 0, 0, 0, 0).replaceEchelon(echelon);

            final Integer forceID = individualEntityState.getForceID();
            final EntityMarking entityMarking = new EntityMarking(EntityMarking.ASCII_CHARACTER_SET, name);
            return new EntityState(teamEntityId, forceID, teamEntityType, ZERO_VEC,
                    location, orientation, EMPTY_ARTICULATED_PARAMS, DEFAULT_ENTITY_APPEARANCE, entityMarking);
        }
    }

    /**
     * Constructs a client message for a provided {@link EntityState} payload.
     *
     * @param state The {@link EntityState} for which the client payload should
     *        be constructed. Can't be null.
     * @return The constructed client payload. Can't be null.
     */
    private EntityStateUpdate buildClientPayload(EntityState state) {
        return buildClientPayload(state, state.getAppearance().isActive());
    }

    /**
     * Sends an {@link EntityStateUpdate} to the client via the
     * {@link #webSocket} if it passes the frequency filter.
     *
     * @param time The epoch time (in milliseconds) at which the entity updated
     *        occurred.
     * @param update The {@link EntityStateUpdate} to send to the client.
     * @param ignoreFrequencyFilter A flag indicating whether or not the
     *        frequency filter should be consulted when attempting to send the
     *        message to the client.
     */
    private void sendMessage(long time, EntityStateUpdate update, boolean ignoreFrequencyFilter) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(time, update);
            logger.trace("sendMessage(" + join(", ", params) + ")");
        }

        final MessageFrequencyThrottle freqThrottle = MessageFrequencyThrottle.getInstance();
        final int domainSessionId = update.getSessionEntityId().getHostDomainSessionId();
        if (ignoreFrequencyFilter || freqThrottle.allow(domainSessionId, update)) {

            AbstractKnowledgeSession knowledgeSession = getKnowledgeSession();
            if(knowledgeSession == null) {
                logger.warn("Unable to send an entity state update because domain session " + domainSessionId
                        + "'s entity filter does not have an associated knowledge session.");
                return;
            }

            final DashboardMessage msg = new DashboardMessage(update, knowledgeSession, time);
            webSocket.send(msg);
        }
    }

    /**
     * Constructs a client message for a provided {@link EntityState} payload.
     *
     * @param state The {@link EntityState} for which the client payload should
     *        be constructed. Can't be null.
     * @param isActive A flag indicating whether the generated
     *        {@link EntityStateUpdate} should indicate whether or not the
     *        entity is active.
     * @return The constructed client payload. Can't be null.
     */
    private EntityStateUpdate buildClientPayload(EntityState state, boolean isActive) {

        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(state, isActive);
            logger.trace("buildClientMessage(" + join(", ", params) + ")");
        }

        AbstractKnowledgeSession knowledgeSession = getKnowledgeSession();
        if(knowledgeSession == null) {
            logger.warn("Unable to build a client payload because domain session " + domainSessionKey
                    + "'s entity filter does not have an associated knowledge session.");
            return null;
        }

        /* Modify the state to override the active flag if it does not match the
         * value that has been requested. */
        if (state.getAppearance().isActive() != isActive) {
            EntityAppearance newAppearance = state.getAppearance().replaceActive(isActive);
            state = state.replaceAppearance(newAppearance);
        }

        /* Unpack the entity state */
        final EntityIdentifier entityId = state.getEntityID();
        final Integer entityIdValue = entityId.getEntityID();
        final int applicationId = entityId.getSimulationAddress().getApplicationID();
        final EntityType entityType = state.getEntityType();
        final Integer entityForceId = state.getForceID();
        final Point3d entityLocation = state.getLocation();
        final EntityAppearance entityAppearance = state.getAppearance();
        final EntityMarking entityMarking = state.getEntityMarking();
        final String entityMarkingValue = entityMarking != null ? entityMarking.getEntityMarking() : null;
        final Vector3d entityOrientation = state.getOrientation();
        final Vector3d entityVelocity = state.getLinearVelocity();

        /* If an entity location is not provided, no update should be sent to
         * the client. */
        if (entityLocation == null) {
            return null;
        }

        final SessionMember hostSessionMember = knowledgeSession.getHostSessionMember();
        int hostDomainSessionId = hostSessionMember.getDomainSessionId();
        String hostUsername = hostSessionMember.getSessionMembership().getUsername();

        /* Create the identifier that is used by the client to uniquely identify
         * this entity in the context of GIFT. */
        SessionEntityIdentifier sessionEntityIdentifier = new SessionEntityIdentifier(applicationId,
                hostDomainSessionId,
                entityIdValue);

        /* Pass the client a version of this location that can be rendered to a
         * map */
        GCC gccLocation = new GCC(entityLocation.getX(), entityLocation.getY(), entityLocation.getZ());
        GDC gdcLocation = CoordinateUtil.getInstance().convertToGDC(gccLocation);
        final mil.arl.gift.tools.map.shared.GDC sharedGdc = new mil.arl.gift.tools.map.shared.GDC(
                gdcLocation.getLatitude(), gdcLocation.getLongitude(), gdcLocation.getElevation());

        EntityStateUpdate locationUpdate = null;

        /* Determine if this entity state represents a member of a team or a
         * team within the knowledge session */
        TeamMember<?> teamMember = null;
        Team team = null;

        if (entityMarking != null) {

            Team teamStructure = knowledgeSession.getTeamStructure();
            /* Get the team member this entity state represents using its unique
             * entity marking */
            teamMember = teamStructure.getTeamMemberByEntityMarking(entityMarkingValue);

            if (teamMember == null) {
                AbstractTeamUnit teamUnit = teamStructure.getTeamElement(entityMarkingValue);
                if (teamUnit instanceof Team) {
                    team = (Team) teamUnit;
                }
            }
        }

        if (teamMember != null) {

            /* this entity is a team member, so attach additional information
             * about its state within the team */
            if (teamMember.isPlayable()) {

                /* check if this playable entity is being controlled by a
                 * learner */
                Integer memberDomainSessionId = null;
                String userName = null;

                if (teamMember.equals(knowledgeSession.getHostSessionMember().getSessionMembership().getTeamMember())) {

                    /* this entity is being played by the hosting learner, so
                     * store the host's session info */
                    memberDomainSessionId = hostDomainSessionId;
                    userName = hostUsername;

                } else if (knowledgeSession instanceof TeamKnowledgeSession) {
                    TeamKnowledgeSession teamSession = (TeamKnowledgeSession) knowledgeSession;

                    /* Check to see if the entity is being played by one of the
                     * learner's who have joined the host */
                    for (SessionMember member : teamSession.getJoinedMembers().values()) {
                        SessionMembership sessionMembership = member.getSessionMembership();
                        if (sessionMembership.getTeamMember().equals(teamMember)) {
                            memberDomainSessionId = member.getDomainSessionId();
                            userName = sessionMembership.getUsername();
                            break;
                        }
                    }
                }

                /* If a learner's entity state has changed, send a learner
                 * location update */
                if (memberDomainSessionId != null) {
                    locationUpdate = new EntityStateUpdate(sessionEntityIdentifier, sharedGdc,
                            entityTypeToSidcConverter.getSidc(entityType),
                            entityForceId, new LearnerEntityInfo(memberDomainSessionId, userName),
                            entityAppearance.getDamage(), isActive);
                }

            }
        } else if (team != null) {
            locationUpdate = new EntityStateUpdate(sessionEntityIdentifier, sharedGdc,
                    entityTypeToSidcConverter.getSidc(entityType), entityForceId,
                    new LearnerEntityInfo(hostDomainSessionId, entityMarkingValue), entityAppearance.getDamage(),
                    isActive);
        }

        /* If a non-learner entity state has changed, just send a regular entity
         * update */
        if (locationUpdate == null) {
            locationUpdate = new EntityStateUpdate(sessionEntityIdentifier, sharedGdc,
                    entityTypeToSidcConverter.getSidc(entityType), entityForceId,
                    entityAppearance.getDamage(), isActive);
        }

        /* Attach any available role information to updates for team and team
         * member entities */
        if (teamMember != null) {
            locationUpdate.setRoleName(teamMember.getName());
            locationUpdate.setPlayable(teamMember.isPlayable());
        } else if (team != null) {
            locationUpdate.setRoleName(team.getName());
            locationUpdate.setPlayable(true);
        }

        /* Obtain the entity's heading in degrees clockwise from north by
         * converting from its DIS Euler angles */
        final double latRadians = Math.toRadians(gdcLocation.getLatitude());
        final double longRadians = Math.toRadians(gdcLocation.getLongitude());
        final double heading = EulerConversions.getOrientationFromEuler(latRadians, longRadians,
                entityOrientation.getX(), entityOrientation.getY());

        locationUpdate.setOrientation(heading);
        locationUpdate.setEntityMarking(entityMarkingValue);
        locationUpdate.setVelocity(entityVelocity.length());

        /* Calculate the GIFT marking */
        String giftMarking;
        if (team != null) {
            giftMarking = team.getName();
        } else if (teamMember != null) {
            giftMarking = teamMember.getName();
        } else {
            giftMarking = null;
        }

        EntityState aresState;
        if (giftMarking != null) {
            final EntityMarking aresEntityMarking = state.getEntityMarking().replaceGiftDisplayName(giftMarking);
            aresState = state.replaceEntityMarking(aresEntityMarking);
        } else {
            aresState = state;
        }
        
        if (entityAppearance.getPosture() != null) {
            locationUpdate.setPosture(entityAppearance.getPosture());
        }

        /* forward filtered entity states to any connected training
         * applications */
        WebMonitorModule.getInstance().sendGameStateMessageToGateway(aresState, MessageTypeEnum.ENTITY_STATE);

        return locationUpdate;
    }

    /**
     * Should be called when this {@link DomainSessionEntityFilter} is no longer
     * being used in order to clean up resources properly.
     */
    public synchronized void destroy() {
        if (logger.isInfoEnabled()) {
            logger.info("Destroying the "+DomainSessionEntityFilter.class.getSimpleName()+" for domain session "+domainSessionKey);
        }

        /* Cancel the heart beat */
        final ScheduledFuture<?> hbFuture = heartbeatFuture;
        if (hbFuture != null) {
            hbFuture.cancel(false);
        }

        /* Cancel the timeouts */
        Iterator<Entry<EntityIdentifier, EntityMetadata>> iter = entityIdToMetadata.entrySet().iterator();
        while (iter.hasNext()) {
            final EntityMetadata meta = iter.next().getValue();
            meta.tryCancelTimeout();
            meta.drop();
            iter.remove();
        }
    }

    /**
     * Updates which entities and teams should be allowed through this
     * {@link DomainSessionEntityFilter}.
     *
     * @param filterValue A mapping of GIFT roles to a boolean flag indicating
     *        whether or not they should be shown. Can't be null.
     */
    public void updateDomainSessionFilter(Map<String, Boolean> filterValue) {
        if (logger.isTraceEnabled()) {
            logger.trace("updateDomainSessionFilter(" + filterValue + ")");
        }

        if (filterValue == null) {
            throw new IllegalArgumentException("The parameter 'filterValue' cannot be null.");
        }

        roleNameToShouldShow.clear();
        roleNameToShouldShow.putAll(filterValue);

        /* Process each entities last message again now that the filter has
         * changed */
        rehandleLatestStates(true);
    }

    /**
     * Starts a thread to periodically sends the last received
     * {@link EntityState} messages to the client in order to prevent an entity
     * timeout on the downstream clients.
     */
    public synchronized void startHeartbeatService() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting the heartbeat service for domain session "+ domainSessionKey);
        }

        heartbeatFuture = threadPool.scheduleWithFixedDelay(this::rehandleLatestStates, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops the thread that periodically sends the last received
     * {@link EntityState} messages to the client.
     */
    public synchronized void stopHeartbeatService() {
        if (logger.isTraceEnabled()) {
            logger.trace("stopHeartbeatService()");
        }

        /* Stop the current heart beat future if possible */
        ScheduledFuture<?> future = heartbeatFuture;
        if (future != null) {
            future.cancel(false);
            heartbeatFuture = null;
        }
    }

    /**
     * Process each entity's latest {@link EntityState} through the pipeline
     * again.
     */
    private void rehandleLatestStates() {
        rehandleLatestStates(false);
    }

    /**
     * Handle all the currently cached entity states again.
     *
     * @param sendStateForAllEntities Whether or not all entities should have an
     *        {@link EntityState} sent. If true, an entity that has been
     *        filtered out will have an {@link EntityState} sent to hide the
     *        entity. If false, {@link EntityState} messages will only be sent
     *        for entities that are permitted to be shown by the filter.
     */
    private synchronized void rehandleLatestStates(boolean sendStateForAllEntities) {
        if (logger.isTraceEnabled()) {
            logger.trace("rehandleLatestStates(" + sendStateForAllEntities + ")");
        }

        entityIdToMetadata.values().stream().map(EntityMetadata::getLastState)
                .forEach(state -> handleEntityUpdate(lastReceivedTimeStamp, state, sendStateForAllEntities));
    }

    /**
     * Extracts relevant information from an incoming state.
     *
     * @param time the time at which the payload was sent/received.
     * @param state The {@link EntityState} to extract information from.
     */
    private synchronized void indexState(long time, EntityState state) {
        /* Index the location from the message */
        lastReceivedTimeStamp = time;
        final EntityMarking entityMarking = state.getEntityMarking();
        final String entityMarkingValue = entityMarking != null ? entityMarking.getEntityMarking() : null;
        if (entityMarkingValue != null) {
            roleNameToLocation.put(entityMarkingValue, state.getLocation());
        }

        /* If the application id has not yet been determined, calculate it. */
        if (applicationId == null) {
            applicationId = state.getEntityID().getSimulationAddress().getApplicationID() + 1;
        }

        final EntityIdentifier entityID = state.getEntityID();

        EntityMetadata metadata = entityIdToMetadata.computeIfAbsent(entityID, EntityMetadata::new);
        metadata.setLastState(state);
    }

    /**
     * Determines whether any filtering is being applied to the entities for
     * this domain session.
     *
     * @return True if filtering is being applied, false otherwise.
     */
    public boolean isFilterApplied() {
        final boolean filterSpecified = !roleNameToShouldShow.isEmpty();
        final boolean allEntitiesAreSame = roleNameToShouldShow.values().stream().distinct().count() == 1;
        return filterSpecified && !allEntitiesAreSame;
    }

    /**
     * Determines whether a provided {@link AbstractTeamUnit} passes the current
     * filter.
     *
     * @param teamUnit The {@link AbstractTeamUnit} to test against the current
     *        filter. Can't be null.
     * @return True if the {@link AbstractTeamUnit} passes the filter (should be
     *         sent to the client), false otherwise.
     */
    private boolean passesFilter(AbstractTeamUnit teamUnit) {
        boolean toRet;
        if (teamUnit instanceof TeamMember<?>) {
            if (!isFilterApplied()) {
                toRet = true;
            } else {
                TeamMember<?> member = (TeamMember<?>) teamUnit;
                toRet = roleNameToShouldShow.get(member.getName()) == Boolean.TRUE;
            }
        } else if (teamUnit instanceof Team) {
            if (!isFilterApplied()) {
                toRet = false;
            } else {
                Team team = (Team) teamUnit;

                /* Determine if the team is explicitly permitted */
                final boolean teamIsPermitted = roleNameToShouldShow.get(team.getName()) == Boolean.TRUE;

                /* Determine if any of the direct descendants are checked */
                final boolean anySubordinateIsShown = team.getUnits().stream().map(AbstractTeamUnit::getName)
                        .map(roleNameToShouldShow::get).anyMatch(shouldShow -> shouldShow == Boolean.TRUE);

                toRet = teamIsPermitted && !anySubordinateIsShown;
            }
        } else {
            final String msg = String.format(
                    "Unable to check the %s of type %s against the filter.",
                    AbstractTeamUnit.class.getSimpleName(),
                    teamUnit.getClass().getName());
            throw new IllegalArgumentException(msg);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(teamUnit.getName() + (toRet ? " passed" : " failed") + " the filter");
        }

        return toRet;
    }
    
    /**
     * Return the latest location of the entity with the id provided.
     * @param entityId used to look up the entity metadata of a specific entity.
     * @return the last location received by this filter for the entity specified.</br>
     * Can return null if the entityId is null, the entity metadata can't be found,
     * the last state or last location is null.
     */
    public Point3d getLatestEntityLocation(EntityIdentifier entityId){
        
        if(entityId == null){
            return null;
        }
        
        EntityMetadata metadata = entityIdToMetadata.get(entityId);
        if(metadata == null){
            return null;
        }else if(metadata.getLastState() == null){
            return null;
        }
        
        return metadata.getLastState().getLocation();
    }
    
    /**
     * Return the latest force ID of the entity with the ID provided.
     * 
     * @param entityId used to look up the entity metadata of a specific entity. Can be null.
     * @return the last force ID received by this filter for the entity specified.</br>
     * Can return null if the entityId is null, the entity metadata can't be found,
     * the last state or last force ID is null.
     */
    public Integer getForceId(EntityIdentifier entityId){
        
        if(entityId == null){
            return null;
        }
        
        EntityMetadata metadata = entityIdToMetadata.get(entityId);
        if(metadata == null){
            return null;
        }else if(metadata.getLastState() == null){
            return null;
        }
        
        return metadata.getLastState().getForceID();
    }

    /**
     * Calculate the center location of a provided {@link Team}.
     *
     * @param team The {@link Team} for which to calculate the center location.
     *        Can't be null.
     * @return The {@link Point3d} representing the center location of the team.
     *         Can't be null.
     */
    private Point3d getTeamLocation(Team team) {
        /* Get an iterator of the locations of each individual in the
         * team. */
        Iterator<Point3d> pointIter = team.getTeamMemberIdentifiers().stream()
                .map(roleNameToLocation::get)
                .filter(Objects::nonNull)
                .iterator();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MAX_VALUE * -1;
        double maxY = Double.MAX_VALUE * -1;
        double maxZ = Double.MAX_VALUE * -1;

        if (!pointIter.hasNext()) {
            return null;
        }

        while (pointIter.hasNext()) {
            Point3d point = pointIter.next();
            final double x = point.getX();
            final double y = point.getY();
            final double z = point.getZ();

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        /* Calculate the center of the minimal area */
        final double midX = (maxX - minX) / 2 + minX;
        final double midY = (maxY - minY) / 2 + minY;
        final double midZ = (maxZ - minZ) / 2 + minZ;
        return new Point3d(midX, midY, midZ);
    }

    /**
     * Determines the orientation direction of a team based on the team's
     * <i>principle member's</i> orientation.
     *
     * @see #teamIdToPrincipleEntityId
     *
     * @param team The {@link Team} for which to determine the orientation.
     *        Can't be null.
     * @return The {@link Vector3d} representing the orientation of the provided
     *         {@link Team}. Can't be null but can be {@link #ZERO_VEC} if there
     *         was a problem determining the orientation of the {@link Team}.
     */
    private Vector3d getTeamOrientation(Team team) {
        /* Ensure there is a team identifier */
        final EntityIdentifier teamId = team.getEntityIdentifier();
        final String teamName = team.getName();
        if (teamId == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unable to determine the orientation of the team '{}' beacause no team identifier has been established.",
                        teamName);
            }

            return ZERO_VEC;
        }

        final EntityIdentifier principleId = teamIdToPrincipleEntityId.get(teamId);
        if (principleId == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unable to determine the orientation of the team '{}' because there is no mapping to its principle team member",
                        teamName);
            }

            return ZERO_VEC;
        }

        final EntityMetadata principleMetadata = entityIdToMetadata.get(principleId);
        if (principleMetadata == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unable to determine the orientation of the team '{}' because there is no available EntityMetadata for its principle team member",
                        teamName);
            }

            return ZERO_VEC;
        }

        return principleMetadata.getLastState().getOrientation();
    }

    /**
     * Gets the knowledge session of the domain session that is being filtered
     *
     * @return the knowledge session. Can be null if no knowledge session data has been cached for this
     * filter's associated domain session.
     */
    private AbstractKnowledgeSession getKnowledgeSession() {
        try {
            return WebMonitorModule.getInstance().getDomainInformationCache()
                    .getCachedKnowledgeSession(domainSessionKey).get();
        } catch (Exception e) {
            logger.error("Unable to get cached knowledge session for domain session " + domainSessionKey
                    + "'s entity filter because an error occurred while waiting for it.", e);
        }

        return null;
    }

    /**
     * Determines the SIDC for the military symbol that should be used to represent the latest entity state information
     * corresponding to the role with the given name. If the role with the given name is a team, then its SIDC will be
     * determined using the available state information from its constituent team members. This ensures that a SIDC
     * can still be obtained for a team even if that team's data is being filtered out by the current team filter.
     *
     * @param roleName the name of the team role that a SIDC is being requested for
     * @return  the SIDC corresponding to the given team role. Can be null if no entity data has been processed
     * for an entity with the given team role or one of its child roles.
     */
    public SIDC getLatestRoleSidc(String roleName) {
        EntityState latestState = getLatestRoleState(roleName);
        if(latestState == null || latestState.getEntityType() == null) {
            return null;
        }

        return entityTypeToSidcConverter.getSidc(latestState.getEntityType());
    }

    /**
     * Determines  the latest entity state information corresponding to the role with the given name. If the role with
     * the given name is a team, then its state will be determined using the available state information from its
     * constituent team members. This ensures that a state can still be obtained for a team even if that team's data
     * is being filtered out by the current team filter.
     *
     * @param roleName the name of the team role that a state is being requested for
     * @return  the state corresponding to the given team role. Can be null if no entity data has been processed
     * for an entity with the given team role or one of its child roles.
     */
    private EntityState getLatestRoleState(String roleName) {

        AbstractKnowledgeSession knowledgeSession = getKnowledgeSession();
        if(knowledgeSession == null) {
            logger.warn("Unable to handle entity update because domain session " + domainSessionKey
                    + "'s entity filter does not have an associated knowledge session.");
            return null;
        }

        final Team team = knowledgeSession.getTeamStructure();
        if(team == null) {
            return null;
        }

        //determine which team unit has the target role name
        AbstractTeamUnit referencedUnit = getTeamUnitFromRole(roleName, team);

        if(referencedUnit instanceof TeamMember) {

            if(referencedUnit.getEntityIdentifier() == null) {
                return null; //no entity data has been processed for this team member, so no state is available
            }

            //get the latest entity data that has been processed for this ream member
            EntityMetadata metadata = entityIdToMetadata.get(referencedUnit.getEntityIdentifier());
            if(metadata != null) {
                return metadata.getLastState();
            }

        } else if(referencedUnit instanceof Team){

            Team refTeam = (Team) referencedUnit;

            EntityIdentifier teamEntityId = refTeam.getEntityIdentifier();
            EntityMetadata firstMemberMetadata = null;
            if (teamEntityId == null) {

                //if this team does not yet have an associated ID, attempt to create one
                TeamMember<?> firstMember = null;
                for(TeamMember<?> currMember : refTeam.getTeamMembers()) {

                    if(currMember.getEntityIdentifier() == null) {
                        continue;
                    }

                    //only create the team ID if at least one of its members has associated entity data
                    firstMemberMetadata = entityIdToMetadata.get(currMember.getEntityIdentifier());
                    if(firstMemberMetadata != null){
                        firstMember = currMember;
                        break;
                    }
                }

                if(firstMember == null) {
                    return null; //no entity data has been processed for any of this team's members, so no state is available
                }

                //finish creating the team's entity ID using the entity data that has been processed
                final int siteId = firstMember.getEntityIdentifier().getSimulationAddress().getSiteID();
                teamEntityId = createTeamIdentifier(siteId);
                refTeam.setEntityIdentifier(teamEntityId);
            }

            //determine if this team has an entity state explicitly associated with it's ID yet
            EntityMetadata metadata = entityIdToMetadata.get(teamEntityId);

            if(metadata != null) {

                //return the entity state associated win the team's entity ID
                return metadata.getLastState();

            } else  {

                //no entity state is associated with this team's entity ID, so create a state from its members' states
                if(firstMemberMetadata != null) {

                    //already had to look for the first available entity state for one of this team's members, so reuse that
                    metadata = firstMemberMetadata;

                } else {

                    //look for any team member that has a processed entity state associated with it
                    for(TeamMember<?> currMember : refTeam.getTeamMembers()) {

                        if(currMember.getEntityIdentifier() == null) {
                            continue;
                        }

                        metadata = entityIdToMetadata.get(currMember.getEntityIdentifier());
                        if(metadata != null) {
                            break;
                        }
                    }
                }

                if(metadata != null) {

                    //return an entity state for this team based on its constituent members' states
                    return createTeamEntityState(metadata.getLastState(), refTeam.getEntityIdentifier(), refTeam);
                }
            }
        }

        return null;
    }

    /**
     * Gets the team unit in the given team hierarchy that has the given role name
     *
     * @param roleName the name of the role to look for. If null, null will be returned.
     * @param unit the team hierarchy to look for the role in. If null, null will be returned.
     * @return the team unit with the given role name. Can be null if no unit with that role name
     * was found in the given hierarchy.
     */
    private AbstractTeamUnit getTeamUnitFromRole(String roleName, AbstractTeamUnit unit) {

        if(roleName == null || unit == null) {
            return null;
        }

        if(StringUtils.equals(roleName, unit.getName())) {
            return unit;

        } else if(unit instanceof Team) {

            Team team = (Team) unit;
            for(AbstractTeamUnit subUnit : team.getUnits()) {

                AbstractTeamUnit foundUnit = getTeamUnitFromRole(roleName, subUnit);
                if(foundUnit != null) {
                    return foundUnit;
                }
            }
        }

        return null;
    }
}