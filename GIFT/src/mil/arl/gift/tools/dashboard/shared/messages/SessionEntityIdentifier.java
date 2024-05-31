/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.util.Objects;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An object that uniquely identifies a training application entity in a domain knowledge session
 *
 * @author nroberts
 */
public class SessionEntityIdentifier implements IsSerializable{

    /** The ID of the application within which this entity exists */
    private Integer applicationId;

    /** The ID of the domain session that is hosting the domain knowledge session this entity is a part of */
    private int hostDomainSessionId;

    /** The entity's ID in the training application being used by the domain knowledge session */
    private int entityId;

    /**
     * Default, no-argument constructor required for GWT RPC serialization
     */
    private SessionEntityIdentifier() {}

    /**
     * Creates a new identifier for a training application entity with the given
     * ID in a domain knowledge session that hosted by the domain session with
     * the given ID
     *
     * @param hostDomainSessionId the ID of the domain session that is hosting
     *        the domain knowledge session this entity is a part of
     * @param entityId the entity's ID in the training application being used by
     *        the domain knowledge session
     */
    public SessionEntityIdentifier(int hostDomainSessionId, int entityId) {
        this(null, hostDomainSessionId, entityId);
    }

    /**
     * Creates a new identifier for a training application entity with the given
     * ID in a domain knowledge session that hosted by the domain session with
     * the given ID
     *
     * @param applicationId The ID of the application within which this entity
     *        exists.
     * @param hostDomainSessionId the ID of the domain session that is hosting
     *        the domain knowledge session this entity is a part of
     * @param entityId the entity's ID in the training application being used by
     *        the domain knowledge session
     */
    public SessionEntityIdentifier(Integer applicationId, int hostDomainSessionId, int entityId) {
        this();

        this.applicationId = applicationId;
        this.hostDomainSessionId = hostDomainSessionId;
        this.entityId = entityId;
    }

    /**
     * Gets the ID of the domain session that is hosting the domain knowledge session this entity is a part of
     *
     * @return the host domain session ID
     */
    public int getHostDomainSessionId() {
        return hostDomainSessionId;
    }

    /**
     * Gets the entity's ID in the training application being used by the domain knowledge session
     *
     * @return the training application entity ID
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Getter for the identifier of the application that is representing this
     * entity.
     *
     * @return The value of {@link #applicationId}. Can be null if none was
     *         specified.
     */
    public Integer getApplicationId() {
        return applicationId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entityId;
        result = prime * result + hostDomainSessionId;
        result = prime * result + (applicationId != null ? applicationId : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionEntityIdentifier other = (SessionEntityIdentifier) obj;
        if (entityId != other.entityId)
            return false;
        if (hostDomainSessionId != other.hostDomainSessionId)
            return false;
        if (!Objects.equals(applicationId, other.applicationId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("[SessionEntityIdentifier: ")
            .append("hostDomainSessionId = ").append(hostDomainSessionId)
            .append(", entityId = ").append(entityId)
            .append("]").toString();
    }
}
