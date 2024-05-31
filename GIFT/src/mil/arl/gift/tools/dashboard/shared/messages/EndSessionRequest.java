/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

/**
 * A game master message that is used to indicate that the session
 *
 * @author tflowers
 *
 */
public class EndSessionRequest implements Serializable {

    /** Class version id used for serialization and deserialization */
    private static final long serialVersionUID = 1L;

    /** The name of the user who requested to end the session */
    private String username;

    /**
     * Constructor required to make the class GWT Serializable.
     */
    private EndSessionRequest() {
    }

    /**
     * Creates a new {@link EndSessionRequest}.
     *
     * @param username The name of the user who is ending the session. Can't be
     *        null.
     */
    public EndSessionRequest(String username) {
        this();
        if (username == null) {
            throw new IllegalArgumentException("The parameter 'username' cannot be null.");
        }

        this.username = username;
    }

    /**
     * Getter for the name of the user who requested to end the session.
     *
     * @return The {@link String} value of {@link #username}. Can't be null.
     */
    public String getUsername() {
        return username;
    }
}
