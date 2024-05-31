/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.EntityDisplaySettingsPanel.RenderableAttribute;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider;
import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.SessionEntityIdentifier;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Affiliation;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;
import mil.arl.gift.tools.map.client.draw.PointShape;

/**
 * An object containing data surrounding a session entity that is needed to render a representation
 * of that entity on a map.
 *
 * @author nroberts
 */
public class EntityDataPoint extends AbstractMapDataPoint{

    /** The Unicode character corresponding to the degree symbol */
    private static final String UNICODE_DEGREE_SIGN = "\u00B0";

    /** A number format used to render doubles as strings rounded down to two decimal points of precision */
    private static final NumberFormat TWO_DECIMAL_PRECISION_FORMAT = NumberFormat.getDecimalFormat().overrideFractionDigits(0, 2);

    /** The amount of time to wait to clean up this entity after it was last updated */
    private static final int ENTITY_CLEANUP_TIMEOUT = 30000;

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EntityDataPoint.class.getName());

    /** The military symbol that will represent this entity when it is rendered on the map */
    private MilitarySymbol symbol = new MilitarySymbol(Affiliation.NEUTRAL, Status.PRESENT);

    /** The layer used to render this entity on the map as part of a knowledge session */
    private final SessionMapLayer sessionLayer;

    /**
     * A flag indicating whether or not the {@link #erase()} method has been
     * called.
     */
    boolean hasBeenErased = false;

    /** The optional callback to execute if the role changes */
    private ChangeCallback<String> roleChangeHandler = null;

    /**
     * A timer used to clean up this entity from the map when it has not been updated for an extended
     * period of time. If an update is detected before this timer executes, then the timer should be
     * cancelled and reset.
     */
    private Timer timeoutTimer = new Timer() {

        @Override
        public void run() {

            //clean up this entity from the map when it has not been updated in a while
            EntityDataPoint.this.sessionLayer.removeEntity(EntityDataPoint.this.id);
        }
    };

    /** The ID used to uniquely identify this entity within its session */
    private final SessionEntityIdentifier id;

    /** The latest state of this entity that was received from the server */
    private EntityStateUpdate state;

    /** The assessment of the shape */
    private AssessmentLevelEnum assessment;

    /** The shape used to render this entity to a map */
    private MapShape shape;

    /**
     * Creates a representation of an entity that is rendered to the given maps at the given location as part of the given session layer
     *
     * @param initialState the initial state of the entity that was received from the server. Cannot be null.
     * @param sessionLayer the knowledge session layer that the entity should be rendered for. Cannot be null.
     */
    public EntityDataPoint(EntityStateUpdate initialState, SessionMapLayer sessionLayer){

        if(initialState == null) {
            throw new IllegalArgumentException("An entity data point's initial state cannot be null.");
        } else if (sessionLayer == null) {
            throw new IllegalArgumentException("An entity data point's session layer cannot be null.");
        }

        this.id = initialState.getSessionEntityId();
        this.sessionLayer = sessionLayer;
        
        // allow entity map icons to toggle their selection based on clicking on the icon
        // this will make it so the icon's label appears and then disappears by clicking on the icon
        setToggleSelected(true);

        //update this data point to match its initial state
        setState(initialState);

        //initialize the shape used to render this data point
        MapShape shape = sessionLayer.createMapShape(this);

        if(shape == null) {
            throw new IllegalArgumentException("A map entity's shape cannot be null.");
        }

        this.shape = shape;
        shape.getMapPoint().setClickCommand(new Command() {

            @Override
            public void execute() {
                EntityDataPoint.this.sessionLayer.onDataPointClicked(EntityDataPoint.this);
            }
        });
    }

    /**
     * Return the session entity identifier for this shape.
     *
     * @return the session entity identifier that is unique to this shape.
     */
    public SessionEntityIdentifier getId() {
        return id;
    }

    @Override
    public PointShape<?> getMapPoint() {
        return shape.getMapPoint();
    }

    /**
     * Set the assessment for this shape. Will not be applied to the map until
     * {@link #draw()} is called.
     *
     * @param assessment the assessment to set. Can't be null.
     */
    public void setAssessment(AssessmentLevelEnum assessment) {
        if (assessment == null) {
            throw new IllegalArgumentException("The parameter 'assessment' cannot be null.");
        }

        this.assessment = assessment;
    }

    @Override
    public void draw() {

        //cancel this entity's timeout, since it is being updated
        timeoutTimer.cancel();

        final Status oldStatus = symbol.getStatus();

        /* Show the appropriate assessment level indicator for all playable
         * entities */
        if (state.getRoleName() == null) {
            symbol.setStatus(Status.PRESENT);
        } else {
            symbol.setStatus(convertAssessmentToStatus(assessment));
        }
        
        /* Size the symbol based on whatever size has been selected by the user */
        symbol.setSize((int) (MilitarySymbol.DEFAULT_SIZE * Dashboard.getInstance().getSettings().getMilSymbolScale()));

        // draw the point based on its current state
        if (!hasBeenErased) {
            shape.draw(getMappableData());

            if (oldStatus != symbol.getStatus()) {

                /* Notify this entity's listeners when its status
                 * changes */
                EntityStatusProvider.getInstance().entityStatusChange(sessionLayer.getDomainSessionId(),
                        state.getRoleName(), symbol.getStatus(), oldStatus);

                /* Play an animation to draw the user's attention to
                 * this entity when its status changes */
                playPulseAnimation();
            }

            //schedule a timeout for this entity that will clean it up if it isn't updated for a while
            timeoutTimer.schedule(ENTITY_CLEANUP_TIMEOUT);
        }
    }

    @Override
    public String getAttributeData(RenderableAttribute attribute) {

        switch(attribute) {

            case LOCATION:
                return state.getLocation().toString();

            case HOST_DOMAIN_SESSION:
                return Integer.toString(id.getHostDomainSessionId());

            case DOMAIN_SESSION:
                return state.getLearnerInfo() != null
                        ? Integer.toString(state.getLearnerInfo().getDomainSessionId())
                        : null;

            case ROLE:
                return state.getRoleName();

            case VELOCITY:
                return state.getVelocity() != null
                        ? TWO_DECIMAL_PRECISION_FORMAT.format(state.getVelocity()) + " m/s"
                        : null;

            case ORIENTATION:
                return state.getOrientation() != null
                        ? TWO_DECIMAL_PRECISION_FORMAT.format(state.getOrientation()) + UNICODE_DEGREE_SIGN
                        : null;

            case ENTITY_MARKER:
                return state.getEntityMarking();

            case USER_NAME:
                return state.getLearnerInfo() != null
                        ? state.getLearnerInfo().getUserName()
                        : null;
                        
            case HEALTH:
                return state.getDamage().getDisplayName();
                
            case SIDC:
                return state.getSIDC().toString();
                
            case POSTURE:
                return state.getPosture().getDisplayName();
                
            case ENTITY_ID:
                return String.valueOf(state.getSessionEntityId().getEntityId());
                
            case PLAYABLE:
                return String.valueOf(state.isPlayable());

            default:
                return null;
        }
    }

    /**
     * Gets the image URL that should be used as the icon for this entity's on the map based on its current state.
     *
     * @return the icon URL. Will not be null.
     */
    private String getIconUrl() {
        
        if(symbol == null || state == null) {
            return null;
        }

        return SessionsMapPanel.getMilitarySymbolGenerator().getSymbolUrl(symbol, state.getSIDC());
    }
    
    /**
     * Gets the military symbol used to represent this entity's state visually
     * 
     * @return the military symbol. Will not be null.
     */
    public MilitarySymbol getSymbol() {
        return symbol;
    }

    /**
     * Sets the change handler for the entity's role.
     *
     * @param roleChangeHandler the change handler to set. Can be null to remove
     *        the existing handler.
     */
    public void setRoleChangedHandler(ChangeCallback<String> roleChangeHandler) {
        this.roleChangeHandler = roleChangeHandler;
    }

    /**
     * Sets this shape's entity state based on the given update. The shape's appearance will change to match
     * this state the next time it is drawn.
     *
     * @param newState the entity state update. Cannot be null and cannot have a null location, force ID, or entity type.
     */
    public void setState(EntityStateUpdate newState) {

        if(newState == null) {
            throw new IllegalArgumentException("The new entity state to update to cannot be null");
        }

        if(newState.getLocation() == null) {
            throw new IllegalArgumentException("The location of the entity state to update to cannot be null");
        }

        if(newState.getForceId() == null) {
            throw new IllegalArgumentException("The force ID of the entity state to update to cannot be null");
        }

        if(newState.getSIDC() == null) {
            throw new IllegalArgumentException("The type of the entity state to update to cannot be null");
        }

        final String oldRoleName = state != null ? state.getRoleName() : null;

        this.state = newState;

        //assign the military symbol's affiliation based on the entity's force ID
        symbol.setAffiliation(MilitarySymbol.Affiliation.getAffiliationFromForceId(state.getForceId()));

        if (roleChangeHandler != null && !StringUtils.equalsIgnoreCase(oldRoleName, state.getRoleName())) {

            //notify listeners that this entity's role has changed
            roleChangeHandler.onChange(state.getRoleName(), oldRoleName);
        }
    }

    /**
     * Gets the latest entity state that this shape is currently representing
     *
     * @return the entity state. Will not be null.
     */
    public EntityStateUpdate getState() {
        return this.state;
    }

    /**
     * Converts the provided assessment into a {@link Status}.
     *
     * @param assessment the assessment to convert.
     * @return the {@link Status} that is associated with the provided
     *         assessment.
     */
    public static Status convertAssessmentToStatus(AssessmentLevelEnum assessment) {

        if (AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment)) {

            /* Show a red bar if this entity's session's performance assessment
             * is below expectation */
            return Status.PRESENT_DESTROYED;
        } else if(AssessmentLevelEnum.UNKNOWN.equals(assessment) || assessment == null){
            /* show no color if this entity's session's performance assessment
             * is unknown or not set */
            return Status.PRESENT;
        }else{

            /* Show a green bar if this entity's session's performance
             * assessment is At or Above */
            return Status.PRESENT_FULLY_CAPABLE;
        }
    }

    /**
     * Erases this entity from both the map and the minimap
     */
    public void erase() {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Removing "+id);
        }
        timeoutTimer.cancel();
        shape.erase();
        hasBeenErased = true;
    }

    /**
     * Checks if this shape is in the provided layer.
     *
     * @param layer the layer to check.
     * @return true if this shape's parent layer (which was declared on
     *         construction) is the same as the provided layer; false otherwise.
     */
    public boolean isShapeInLayer(SessionMapLayer layer) {
        return this.sessionLayer == layer;
    }

    /**
     * Plays a pulsing animation on all of the elements that are currently drawn onto a map for this entity.
     * Will do nothing if no elements are currently drawn onto a map for this entity.
     */
    public void playPulseAnimation() {
        shape.playPulseAnimation();
    }

    @Override
    public MappableData getMappableData() {

        int priority = 0;
        String dataLabel = null;
        String name = null;

        if(isSelected()) {

            //ensure that selected entities are always rendered on top
            priority = SELECTED_ENTITY_Z_INDEX;
            dataLabel = sessionLayer.getEntitySettings().buildEntityLabel(EntityDataPoint.this);            

        } else if (state.getLearnerInfo() != null) {
            priority = LEARNER_ENTITY_Z_INDEX;

        } else {
            priority = PLAYABLE_ENTITY_Z_INDEX;
        }

        if (state.getLearnerInfo() != null) {

            name = state.getLearnerInfo().getUserName();

            if(name == null) {

                //show the entity's domain session ID if no user name is available
               name = Integer.toString(state.getLearnerInfo().getDomainSessionId());
            }
        }

        return new MappableData(state.getLocation(),
                Dashboard.getInstance().getSettings().isShowTeamOrgName() ? name : null, 
                getIconUrl(), priority, dataLabel, isSelected());
    }

    /**
     * Gets the name of the role associated with this entity in the session's team organization
     *
     * @return the entity's role name. Can be null.
     */
    public String getRoleName() {

        if(state == null) {
            return null;
        }

        return state.getRoleName();
    }

    /**
     * Gets the force ID assigned to this entity
     *
     * @return the force ID. Can be null.
     */
    public Integer getForceID() {

        if(state == null) {
            return null;
        }

        return state.getForceId();
    }

    /**
     * Gets this entity's current assessment in the session
     *
     * @return the entity's assessment. Can be null.
     */
    public AssessmentLevelEnum getAssessment() {
        return assessment;
    }

    @Override
    public String toString() {
        return new StringBuilder("[EntityDataPoint: ")
                .append("id = ").append(id)
                .append(", roleChangeHandler = ").append(roleChangeHandler)
                .append(", sessionLayer = ").append(sessionLayer)
                .append(", shape = ").append(shape)
                .append(", state = ").append(state)
                .append(", symbol = ").append(symbol)
                .append(", timeoutTimer = ").append(timeoutTimer)
                .append("]").toString();
    }
}
