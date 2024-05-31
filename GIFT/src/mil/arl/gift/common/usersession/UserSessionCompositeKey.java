/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.usersession;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.util.CompareUtil;

/**
 * The UserSessionCompositeKey class can be used in places where a mapping of
 * unique user sessions needs to be maintained based on the data within each
 * user session. Currently the gift user id, experiment id, global user id, and
 * type are all used to form a unique key to identify uniqueness to a user
 * session.
 * 
 * @author nblomberg
 *
 */
public class UserSessionCompositeKey {

    private Integer giftUserId;
    private String experimentId;
    private Integer globalUserId;
    private UserSessionType sessionType;

    /**
     * Constructor
     */
    public UserSessionCompositeKey() {

    }

    /**
     * Constructor
     * 
     * @param giftUserId
     *            The gift user id (can be null if at least one of the other
     *            parameters are not null)
     * @param experimentId
     *            The experiment id (can be null if at least one of the other
     *            parameters are not null)
     * @param globalUserId
     *            The global user id (can be null if at least one of the other
     *            parameters are not null)
     * @param sessionType
     *            The user session type (cannot be null)
     */
    public UserSessionCompositeKey(Integer giftUserId, String experimentId, Integer globalUserId,
            UserSessionType sessionType) {
        setGiftUserId(giftUserId);
        setExperimentId(experimentId);
        setGlobalUserId(globalUserId);
        setSessionType(sessionType);
    }

    /**
     * Constructor
     * 
     * @param userSession
     *            The user session to create a composite key for.
     */
    public UserSessionCompositeKey(UserSession userSession) {
        if (userSession == null) {
            throw new IllegalArgumentException("userSession cannot be null");
        }

        setGiftUserId(userSession.getUserId());
        setExperimentId(userSession.getExperimentId());
        setGlobalUserId(userSession.getGlobalUserId());
        setSessionType(userSession.getSessionType());
    }

    /**
     * @return the giftUserId
     */
    public Integer getGiftUserId() {
        return giftUserId;
    }

    /**
     * @param giftUserId
     *            the giftUserId to set
     */
    public void setGiftUserId(Integer giftUserId) {
        this.giftUserId = giftUserId;
    }

    /**
     * @return the experimentId
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * @param experimentId
     *            the experimentId to set
     */
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    /**
     * @return the globalUserId
     */
    public Integer getGlobalUserId() {
        return globalUserId;
    }

    /**
     * @param globalUserId
     *            the globalUserId to set
     */
    public void setGlobalUserId(Integer globalUserId) {
        this.globalUserId = globalUserId;
    }

    /**
     * @return the sessionType
     */
    public UserSessionType getSessionType() {
        return sessionType;
    }

    /**
     * @param sessionType
     *            the sessionType to set
     */
    public void setSessionType(UserSessionType sessionType) {
        this.sessionType = sessionType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }

        UserSessionCompositeKey other = (UserSessionCompositeKey) obj;

        if (CompareUtil.equalsNullSafe(this.getGiftUserId(), other.getGiftUserId())
                && CompareUtil.equalsNullSafe(this.getExperimentId(), other.getExperimentId())
                && CompareUtil.equalsNullSafe(this.getGlobalUserId(), other.getGlobalUserId())
                && CompareUtil.equalsNullSafe(this.getSessionType(), other.getSessionType())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash;

        if (getGiftUserId() != null) {
            hash += getGiftUserId().hashCode();
        }

        if (getExperimentId() != null) {
            hash += getExperimentId().hashCode();
        }

        if (getGlobalUserId() != null) {
            hash += getGlobalUserId().hashCode();
        }

        if (getSessionType() != null) {
            hash += getSessionType().hashCode();
        }

        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("[UserCompositeKey: ");
        sb.append("GiftUserId: ").append(getGiftUserId());
        sb.append(", ExperimentId: ").append(getExperimentId());
        sb.append(", GlobalUserId: ").append(getGlobalUserId());
        sb.append(", SessionType: ").append(getSessionType());
        sb.append("]");

        return sb.toString();

    }

}
