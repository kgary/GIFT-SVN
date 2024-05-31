/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;

import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains the various types of mission data for a scenario.
 * 
 * @author sharrison
 */
public class Mission implements Serializable {

    /** Default */
    private static final long serialVersionUID = 1L;

    /** The mission source property */
    private String source;

    /** The mission MET property */
    private String met;

    /** The mission task property */
    private String task;

    /** The mission situation property */
    private String situation;

    /** The mission goals property */
    private String goals;

    /** The mission condition property */
    private String condition;

    /** The mission ROE property */
    private String roe;

    /** The mission threat warning property */
    private String threatWarning;

    /** The mission weapon status property */
    private String weaponStatus;

    /** The mission weapon posture property */
    private String weaponPosture;

    /**
     * Required for GWT serialization
     */
    private Mission() {
    }

    /**
     * Constructor.
     * 
     * @param source the mission source data.
     * @param met the mission MET data.
     * @param task the mission task data.
     * @param situation the mission situation data.
     * @param goals the mission goals data.
     * @param condition the mission condition data.
     * @param roe the mission ROE data.
     * @param threatWarning the mission threat warning data.
     * @param weaponStatus the mission weapon status data.
     * @param weaponPosture the mission weapon posture data.
     */
    public Mission(String source, String met, String task, String situation, String goals, String condition, String roe,
            String threatWarning, String weaponStatus, String weaponPosture) {
        this();
        this.source = source;
        this.met = met;
        this.task = task;
        this.situation = situation;
        this.goals = goals;
        this.condition = condition;
        this.roe = roe;
        this.threatWarning = threatWarning;
        this.weaponStatus = weaponStatus;
        this.weaponPosture = weaponPosture;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the value of the met property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMET() {
        return met;
    }

    /**
     * Gets the value of the task property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTask() {
        return task;
    }

    /**
     * Gets the value of the situation property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSituation() {
        return situation;
    }

    /**
     * Gets the value of the goals property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getGoals() {
        return goals;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the value of the roe property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getROE() {
        return roe;
    }

    /**
     * Gets the value of the threatWarning property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getThreatWarning() {
        return threatWarning;
    }

    /**
     * Gets the value of the weaponStatus property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getWeaponStatus() {
        return weaponStatus;
    }

    /**
     * Gets the value of the weaponPosture property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getWeaponPosture() {
        return weaponPosture;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((goals == null) ? 0 : goals.hashCode());
        result = prime * result + ((met == null) ? 0 : met.hashCode());
        result = prime * result + ((roe == null) ? 0 : roe.hashCode());
        result = prime * result + ((situation == null) ? 0 : situation.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((task == null) ? 0 : task.hashCode());
        result = prime * result + ((threatWarning == null) ? 0 : threatWarning.hashCode());
        result = prime * result + ((weaponPosture == null) ? 0 : weaponPosture.hashCode());
        result = prime * result + ((weaponStatus == null) ? 0 : weaponStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof Mission)) {
            return false;
        }

        Mission other = (Mission) obj;
        if (!StringUtils.equals(source, other.getSource())) {
            return false;
        }
        if (!StringUtils.equals(met, other.getMET())) {
            return false;
        }
        if (!StringUtils.equals(task, other.getTask())) {
            return false;
        }
        if (!StringUtils.equals(situation, other.getSituation())) {
            return false;
        }
        if (!StringUtils.equals(goals, other.getGoals())) {
            return false;
        }
        if (!StringUtils.equals(condition, other.getCondition())) {
            return false;
        }
        if (!StringUtils.equals(roe, other.getROE())) {
            return false;
        }
        if (!StringUtils.equals(threatWarning, other.getThreatWarning())) {
            return false;
        }
        if (!StringUtils.equals(weaponStatus, other.getWeaponStatus())) {
            return false;
        }
        if (!StringUtils.equals(weaponPosture, other.getWeaponPosture())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[Mission: ");
        sb.append("source = ").append(getSource());
        sb.append(", MET = ").append(getMET());
        sb.append(", task = ").append(getTask());
        sb.append(", situation = ").append(getSituation());
        sb.append(", goals = ").append(getGoals());
        sb.append(", condition = ").append(getCondition());
        sb.append(", ROE = ").append(getROE());
        sb.append(", threat warning = ").append(getThreatWarning());
        sb.append(", weapon status = ").append(getWeaponStatus());
        sb.append(", weapon posture = ").append(getWeaponPosture());
        sb.append("]");

        return sb.toString();
    }
}
