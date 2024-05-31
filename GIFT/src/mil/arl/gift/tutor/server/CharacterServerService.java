/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

/**
 * A singleton for getting and setting the state of the Character Server
 *
 * @author jleonard
 */
public class CharacterServerService {

    private static CharacterServerService instance = null;
    
    private boolean isOnline = false;

    /**
     * Gets the singleton instance of the character server service
     *
     * @return The instance of the character server service
     */
    public static CharacterServerService getInstance() {

        if (instance == null) {

            instance = new CharacterServerService();
        }

        return instance;
    }

    /**
     * Default Constructor Exists for the state of setting it private
     */
    private CharacterServerService() {
    }

    /**
     * Gets if the Character Server is online
     *
     * @return boolean True if the Character Server is online
     */
    public boolean isOnline() {

        return isOnline;
    }

    /**
     * Sets if the Character Server is online
     *
     * @param isOnline True if the Character Server is online
     */
    public void setIsOnline(boolean isOnline) {

        this.isOnline = isOnline;
    }
}
