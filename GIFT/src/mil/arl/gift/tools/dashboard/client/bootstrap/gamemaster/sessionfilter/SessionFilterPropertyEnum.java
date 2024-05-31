/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.util.StringUtils;

/**
 * The filter property types to be used to filter knowledge sessions.
 * 
 * @author sharrison
 */
public enum SessionFilterPropertyEnum {
    /** The course name filter property */
    COURSE_NAME("Course Name"),

    /** The experiment name filter property */
    EXPERIMENT_NAME("Experiment Name"),

    /** The domain session id filter property */
    SESSION_ID("Session ID");

    /** The display name for the enum */
    private String name;

    /**
     * Constructor.
     * 
     * @param name the display name for the enum
     */
    private SessionFilterPropertyEnum(String name) {
        this.name = name;
    }

    /**
     * Get the display name for the enum.
     * 
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Finds the {@link SessionFilterPropertyEnum} that matches the provided
     * name.
     * 
     * @param name the name to find.
     * @return the {@link SessionFilterPropertyEnum} if one matches. Otherwise,
     *         returns null.
     */
    public static SessionFilterPropertyEnum findFilterPropertyEnum(String name) {
        for (SessionFilterPropertyEnum propertyEnum : SessionFilterPropertyEnum.values()) {
            if (StringUtils.equalsIgnoreCase(propertyEnum.getName(), name)) {
                return propertyEnum;
            }
        }

        return null;
    }

    /**
     * Get the value of the property from the provided session.
     * 
     * @param propertyEnum the property to use to pull the value from the
     *        session.
     * @param knowledgeSession the session containing the data.
     * @return the value representing the session's property.
     */
    public static String getFilterPropertyValue(SessionFilterPropertyEnum propertyEnum,
            AbstractKnowledgeSession knowledgeSession) {
        String sessionValue = null;
        switch (propertyEnum) {
        case COURSE_NAME:
            sessionValue = knowledgeSession.getCourseName();
            break;
        case EXPERIMENT_NAME:
            sessionValue = knowledgeSession.getExperimentName();
            break;
        case SESSION_ID:
            sessionValue = Integer.toString(knowledgeSession.getHostSessionMember().getDomainSessionId());
            break;
        default:
            throw new UnsupportedOperationException(
                    "Found unsupported filter property '" + propertyEnum.getName() + "'.");
        }

        return sessionValue;
    }

    @Override
    public String toString() {
        return getName();
    }
}
