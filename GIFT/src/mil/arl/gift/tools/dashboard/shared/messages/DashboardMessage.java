/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;

/**
 * A message used to communicate via websocket that does not expect a response back from the receiver
 *
 * @author nblomberg
 *
 */
public class DashboardMessage extends AbstractWebSocketMessage {

    /** Payload data within the RuntimeToolMessage wrapper */
    private Serializable payload;

    /** The knowledge session that the payload is to be applied to */
    private AbstractKnowledgeSession knowledgeSession;

    /** The domain session id */
    private int domainSessionId;

    /** The browser session sending the message. */
    private String browserSessionKey;

    /** The timestamp of the message */
    private long timestamp;

    /**
     * Constructor - default (Needed for gwt serialization).
     */
    private DashboardMessage() {
    }

    /**
     * Constructor. Use this constructor if you don't have a knowledge session.
     * 
     * @param payload The real message. Can't be null.
     * @param domainSessionId The domain session id for the payload.
     * @param timestamp the timestamp of this message.
     */
    public DashboardMessage(Serializable payload, int domainSessionId, long timestamp) {
        this();
        if (payload == null) {
            throw new IllegalArgumentException("The parameter 'payload' cannot be null.");
        }

        this.payload = payload;
        this.domainSessionId = domainSessionId;
        this.timestamp = timestamp;
    }

    /**
     * Constructor. Uses a current timestamp.
     * 
     * @param payload The real message. Can't be null.
     * @param knowledgeSession The knowledge session that the payload is being
     *        applied to. Can't be null.
     */
    public DashboardMessage(Serializable payload, AbstractKnowledgeSession knowledgeSession) {
        this();
        if (payload == null) {
            throw new IllegalArgumentException("The parameter 'payload' cannot be null.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        } else if (knowledgeSession.inPastSessionMode()) {
            throw new IllegalArgumentException(
                    "A 'Past' knowledge session requires a timestamp; use the other constructor.");
        }

        this.payload = payload;
        this.knowledgeSession = knowledgeSession;
        this.domainSessionId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor
     * 
     * @param payload The real message. Can't be null.
     * @param knowledgeSession The knowledge session that the payload is being
     *        applied to. Can't be null.
     * @param timestamp the timestamp of this message. If null, the current time will be used.
     */
    public DashboardMessage(Serializable payload, AbstractKnowledgeSession knowledgeSession, Long timestamp) {
        this();
        if (payload == null) {
            throw new IllegalArgumentException("The parameter 'payload' cannot be null.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        this.payload = payload;
        this.knowledgeSession = knowledgeSession;
        this.domainSessionId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        this.timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
    }

    /**
     * @return the payload
     */
    public Serializable getPayload() {
        return payload;
    }

    /**
     * Return the knowledge session that the {@link #payload} is being applied
     * to.
     * 
     * @return the knowledge session. Can be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    /**
     * Retrieve the domain session id. This was either explicitly set or
     * extracted from the {@link #knowledgeSession} if it exists.
     * 
     * @return the domain session id.
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }

    /**
     * @return the browserSessionKey
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }

    /**
     * @param browserSessionKey the browserSessionKey to set
     */
    public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

    /**
     * Get the timestamp of the message
     * 
     * @return the timestamp (millis)
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[DashboardMessage: ");
        sb.append("browserSessionKey = ").append(getBrowserSessionKey());
        sb.append(", payloadClass = ").append(payload.getClass()); 
        sb.append(", payload = ").append(payload);
        sb.append(", knowledge session = ").append(knowledgeSession);
        sb.append(", timestamp = ").append(timestamp);
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
