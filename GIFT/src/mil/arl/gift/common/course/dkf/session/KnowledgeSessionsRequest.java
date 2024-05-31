/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.util.StringUtils;

/**
 * Contains filter options for retrieving knowledge session list from the domain module.
 * 
 * @author mhoffman
 *
 */
public class KnowledgeSessionsRequest implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** whether running knowledge sessions should be returned in the reply */
    private boolean runningSessions = true;
    
    /** whether individual knowledge sessions should be returned in the reply */
    private boolean individualSessions = true;
    
    /** whether team sessions that have no empty positions should be returned in the reply */
    private boolean fullTeamSessions = true;
        
    /** the list of course ids that knowledge sessions must have in order to include those sessions the reply */
    private List<String> courseIds = null; 
    
    /**
     * Default constructor.
     */
    public KnowledgeSessionsRequest(){}

    /**
     * Return whether running knowledge sessions should be returned in the reply
     * @return the default is to return knowledge sessions (true by default)
     */
    public boolean isRunningSessions() {
        return runningSessions;
    }
    
    /**
     * Set whether running knowledge sessions should be returned in the reply
     * @param runningSessions false if knowledge sessions should be filtered out in the reply.
     */
    public void setRunningSessions(boolean runningSessions) {
        this.runningSessions = runningSessions;
    }

    /**
     * Return whether individual knowledge sessions should be returned in the reply
     * @return the default is to return individual knowledge sessions (true by default)
     */
    public boolean isIndividualSessions() {
        return individualSessions;
    }

    /**
     * Set whether individual knowledge sessions should be returned in the reply
     * @param individualSessions false if individual knowledge sessions should be filtered out
     * in the reply (i.e. return only team knowledge sessions)
     */
    public void setIndividualSessions(boolean individualSessions) {
        this.individualSessions = individualSessions;
    }

    /**
     * Return whether team sessions that have no empty positions should be returned in the reply
     * @return the default is to return full team sessions (true by default)
     */
    public boolean isFullTeamSessions() {
        return fullTeamSessions;
    }

    /**
     * Set whether team sessions that have no empty positions should be returned in the reply
     * @param fullTeamSessions false if full team knowledge sessions should be filtered out
     * in the reply
     */
    public void setFullTeamSessions(boolean fullTeamSessions) {
        this.fullTeamSessions = fullTeamSessions;
    }

    /**
     * Return the list of course ids that knowledge sessions must have in order to include those sessions the reply
     * @return can be null or empty if this filter is not used.  Null be default.
     */
    public List<String> getCourseIds() {
        return courseIds;
    }

    /**
     * Set  the list of course ids that knowledge sessions must have in order to include those sessions the reply.
     * @param courseIds can be null or empty if this filter is not used.
     */
    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }
    
    /**
     * Add a course id to the list of course ids to filter on.
     * 
     * @param courseId can't be null or empty.
     */
    public void addCourseId(String courseId){
        
        if(StringUtils.isBlank(courseId)){
            return;
        }
        
        if(courseIds == null){
            courseIds = new ArrayList<>();
        }
        
        courseIds.add(courseId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[KnowledgeSessionsRequest: runningSessions = ");
        builder.append(runningSessions);
        builder.append(", individualSessions = ");
        builder.append(individualSessions);
        builder.append(", fullTeamSessions = ");
        builder.append(fullTeamSessions);
        builder.append(", courseIds = ");
        builder.append(courseIds);
        builder.append("]");
        return builder.toString();
    }
    

}
