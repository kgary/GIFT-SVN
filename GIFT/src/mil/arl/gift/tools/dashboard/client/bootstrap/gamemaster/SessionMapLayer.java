/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import static mil.arl.gift.common.util.StringUtils.join;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.state.EvaluationResult;
import mil.arl.gift.common.state.LearnerStateUtil;
import mil.arl.gift.common.util.BiDirectionalHashMap;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.SessionEntityIdentifier;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * An object used to track all of the entities rendered for a particular
 * knowledge session and maintain data surrounding how they are rendered to the
 * map
 *
 * @author nroberts
 */
public class SessionMapLayer {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionMapLayer.class.getName());

    /**
     * The shape entities that have been created for this map layer's associated
     * session
     */
    private Map<SessionEntityIdentifier, EntityDataPoint> entityToShapeMap = new HashMap<>();

    /**
     * The user roles mapped to their respective map shapes. This is a 1-to-1
     * relationship.
     */
    private BiDirectionalHashMap<String, EntityDataPoint> roleToShapeMap = new BiDirectionalHashMap<>();

    /** The collection of entity roles that warrant an alert notification */
    private Set<String> alertEntityRoles = new HashSet<>();

    /** The map panel containing this map layer */
    private final SessionsMapPanel parentMapPanel;

    /**
     * The id of the domain session that this map layer is representing
     */
    private final int domainSessionId;

    /** The team structure for the session */
    private final Team teamStructure;

    /**
     * The latest knowledge session state results. Used to update new shapes.
     */
    private Map<String, AssessmentLevelEnum> roleAssessmentState = new HashMap<>();

    /**
     * Constructor.
     *
     * @param parentMapPanel the parent map panel containing this map layer.
     *        Can't be null.
     * @param domainSessionId the id of the domain session that this map layer
     *        is representing.
     */
    public SessionMapLayer(SessionsMapPanel parentMapPanel, int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(parentMapPanel, domainSessionId);
            logger.fine("SessionMapLayer(" + join(", ", params) + ")");
        }

        if (parentMapPanel == null) {
            throw new IllegalArgumentException("The parameter 'parentMapPanel' cannot be null.");
        }

        this.parentMapPanel = parentMapPanel;
        this.domainSessionId = domainSessionId;

        AbstractKnowledgeSession session = ActiveSessionProvider.getInstance()
                .getActiveSessionFromDomainSessionId(domainSessionId);
        if (session == null || session.getTeamStructure() == null) {
            throw new IllegalArgumentException(
                    "The parameter 'domainSessionId' must represent an active knowledge session that contains a team structure.");
        }

        teamStructure = session.getTeamStructure();
    }
    
    /**
     * Return the entity roles that need attention.
     *
     * @return the collection of roles that need attention.
     */
    public Set<String> getAlertRoles() {
        return new HashSet<>(alertEntityRoles);
    }

    /**
     * Add an entity role that needs attention.
     *
     * @param role the role of the entity that needs attention. Null will not be
     *        added. Must represent a shape that exists in this map layer.
     * @return true if the user role was added to the alert collection; false if
     *         it wasn't due to an invalid role or if it was already in the
     *         collection.
     */
    public boolean addAlertEntity(String role) {
        if (StringUtils.isBlank(role) || !roleToShapeMap.containsKey(role)) {
            return false;
        }

        boolean added = alertEntityRoles.add(role);
        if (added) {
            parentMapPanel.addAlerts(1);
        }

        return added;
    }

    /**
     * Remove an entity role from the collection of entities that need
     * attention.
     *
     * @param role the role of the entity to remove.
     * @return true if the user role was removed from the alert collection;
     *         false if it wasn't due to an invalid role or if it wasn't in the
     *         collection.
     */
    public boolean removeAlertEntity(String role) {
        boolean removed = alertEntityRoles.remove(role);
        if (removed) {
            parentMapPanel.addAlerts(-1);
        }

        return removed;
    }

    /**
     * Redraws the entity and team data points known to this map layer.
     */
    public void redrawDataPoints(){

        Iterator<EntityDataPoint> entitiesItr = entityToShapeMap.values().iterator();
        while(entitiesItr.hasNext()){
            entitiesItr.next().draw();
        }
    }

    /**
     * Adds the provided shape to this map layer.
     *
     * @param shape the shape to be added.
     * @throws IllegalArgumentException if the shape is null or if the shape
     *         doesn't belong to this map layer.
     */
    public void addShapeToLayer(final EntityDataPoint shape) {
        if (shape == null) {
            throw new IllegalArgumentException("The parameter 'shape' cannot be null.");
        } else if (!shape.isShapeInLayer(this)) {
            throw new IllegalArgumentException(
                    "The parameter 'shape' does not belong to this map layer and therefore cannot be added.");
        }

        final String roleName = shape.getState().getRoleName();
        if (StringUtils.isNotBlank(roleName)) {
            roleToShapeMap.put(roleName, shape);
            applyLatestStateResultsToShape(shape);
        }

        shape.setRoleChangedHandler(new ChangeCallback<String>() {
            @Override
            public void onChange(String newValue, String oldValue) {
                roleToShapeMap.removeKey(oldValue);
                if (newValue != null) {
                    roleToShapeMap.put(newValue, shape);
                    applyLatestStateResultsToShape(shape);
                }
            }
        });

        entityToShapeMap.put(shape.getId(), shape);
    }

    /**
     * Clears the entire layer including its map shapes and alerts.
     */
    public void clear() {
        Set<SessionEntityIdentifier> ids = new HashSet<>(entityToShapeMap.keySet());
        for (SessionEntityIdentifier idToRemove : ids) {
            removeEntity(idToRemove);
        }
    }

    /**
     * Removes the given entity from this map layer's collections, erases its
     * shape on the map, and, if necessary decrements the number of entity
     * alerts from the global count.
     *
     * @param id the ID that uniquely identifies the entity to remove within
     *        this map layer's associated session
     */
    public void removeEntity(SessionEntityIdentifier id) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeEntity(" + id + ")");
        }

        /* Remove entity id from the map */
        EntityDataPoint removedShape = entityToShapeMap.remove(id);
        if (removedShape != null) {
            String role = roleToShapeMap.removeValue(removedShape);

            /* Remove entity id from alerts (if possible) */
            removeAlertEntity(role);

            /* Remove the shape from the map */
            eraseShape(removedShape);
        } else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("A request to remove the entity with id '" + id
                        + "' was unsuccessful because no shape for the id exists.");
            }
        }
    }

    /**
     * Erases the provided shape from this map layer.
     *
     * @param shape the shape to erase. Nothing will happen if null.
     */
    private void eraseShape(EntityDataPoint shape) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("eraseShape(" + shape + ")");
        }

        if (shape == null) {
            return;
        }

        if (shape.equals(parentMapPanel.getSelectedMapData())) {
            /* Deselect the erased shape */
            parentMapPanel.setSelectedMapData(null);
        }

        /* Erase the rendered shape from the map */
        shape.erase();
    }

    /**
     * Get the map points associated with the entities in this map layer.
     *
     * @return the collection of map points. Can be empty. Will never be null.
     */
    public Collection<PointShape<?>> getMapPoints() {
        Set<PointShape<?>> mapPoints = new HashSet<>();
        for (EntityDataPoint shape : entityToShapeMap.values()) {
            mapPoints.add(shape.getMapPoint());
        }
        return mapPoints;
    }

    /**
     * Get the shape associated with the provided session entity identifier.
     *
     * @param entityId the id of the entity's shape.
     * @return the entity's shape.
     */
    public EntityDataPoint getShape(SessionEntityIdentifier entityId) {
        return entityToShapeMap.get(entityId);
    }

    /**
     * Get the shape associated with the provided team role.
     *
     * @param role the role of the entity.
     * @return the entity's shape.
     */
    public EntityDataPoint getShape(String role) {
        return roleToShapeMap.getFromKey(role);
    }

    /**
     * Get the number of entities in this map layer.
     *
     * @return the current number of map layer entities.
     */
    public int getEntitySize() {
        return entityToShapeMap.size();
    }

    /**
     * Get the domain session id associated with this map layer.
     *
     * @return the domain session id for the map layer.
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }

    /**
     * Apply the latest team role assessment results to the
     * {@link EntityDataPoint} using the shape's role name.
     *
     * @param shape the {@link EntityDataPoint} to update.
     */
    private void applyLatestStateResultsToShape(EntityDataPoint shape) {
        if (shape == null || shape.getState().getRoleName() == null) {
            return;
        }

        AssessmentLevelEnum roleAssessment = roleAssessmentState.get(shape.getState().getRoleName());
        if (roleAssessment != null) {
            shape.setAssessment(roleAssessment);
        }
    }

    /**
     * Update the assessments for each of the map layer's team roles using the
     * provided knowledge session state.
     *
     * @param state the knowledge session state used to update the team role
     *        assessments. Can't be null.
     */
    public void updateTeamRoleAssessments(KnowledgeSessionState state) {
        if (state == null) {
            throw new IllegalArgumentException("The parameter 'state' cannot be null.");
        }

        /* Perform the assessment evaluation */
        Map<String, EvaluationResult> results = LearnerStateUtil.performEvaluation(state.getLearnerState(), teamStructure);

        for (Entry<String, EvaluationResult> entry : results.entrySet()) {
            final String role = entry.getKey();
            final AssessmentLevelEnum newAssessment = entry.getValue().getAssessmentLevel();

            /* Null assessment means that entity received a "visual only" state.
             * Do not process further. */
            if (newAssessment == null) {
                continue;
            }

            final AssessmentLevelEnum previousAssessment = roleAssessmentState.get(role);
            if (previousAssessment != null && previousAssessment.equals(newAssessment)) {
                /* Same assessment */
                continue;
            } else {
                /* Update the assessment state */
                roleAssessmentState.put(role, newAssessment);
            }

            EntityDataPoint shape = roleToShapeMap.getFromKey(role);
            if (shape == null) {
                /* Let listeners know the status changed. Typically entity shape
                 * would do this when we set its assessment, but the shape
                 * doesn't exist. */
                EntityStatusProvider.getInstance().entityStatusChange(domainSessionId, role,
                        EntityDataPoint.convertAssessmentToStatus(newAssessment), null);
            } else {
                shape.setAssessment(newAssessment);
                shape.draw();
            }
        }
    }

    /**
     * Gets the settings for entities that are selected on the map
     *
     * @return the entity settings. Will not be null.
     */
    public EntitySettingsPanel getEntitySettings() {
        return parentMapPanel.getEntitySettings();
    }

    /**
     * Creates a shape on the map to render the data from the given data point
     *
     * @param mapData the map data point to create a shape for. Cannot be null.
     * @return the shape created for the given data. Will not be null.
     */
    public MapShape createMapShape(AbstractMapDataPoint mapData) {
        return parentMapPanel.createMapShape(mapData);
    }

    /**
     * Gets the location of the given locations' centroid (i.e. the center of mass) on the map.
     *
     * @param locations the locations to get the centroid for. Cannot be null or empty.
     * @return the calculated centroid location. Will not be null.
     */
    public AbstractMapCoordinate getCentroid(AbstractMapCoordinate... locations) {
        return parentMapPanel.getCentroid(locations);
    }

    /**
     * Notifies the map that the shape used to render the given map data has been clicked on
     *
     * @param mapData the map data point whose shape was clicked on. If null, this method will do nothing.
     */
    public void onDataPointClicked(AbstractMapDataPoint mapData) {
        parentMapPanel.onDataPointClicked(this, mapData);
    }
}
