/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import javax.vecmath.Point3d;

/**
 * This training application state class contains information about a rifle shot event. 
 * 
 * @author mhoffman
 *
 */
public class RifleShotMessage implements TrainingAppState {

    private Point3d location;

    private float result;

    private int shotNumber;

    public RifleShotMessage() {

    }

    public RifleShotMessage(Point3d location, float result, int shotNumber) {

        this.location = location;
        this.result = result;
        this.shotNumber = shotNumber;
    }

    public Point3d getLocation() {
        return location;
    }

    public void setLocation(Point3d location) {
        this.location = location;
    }

    public float getResult() {
        return result;
    }

    public void setResult(float result) {
        this.result = result;
    }

    public int getShotNumber() {
        return shotNumber;
    }

    public void setShotNumber(int shotNumber) {
        this.shotNumber = shotNumber;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[RifleShotMessage: ");
        sb.append("Location = ").append(getLocation().toString());
        sb.append(", result = ").append(getResult());
        sb.append(", shotNumber = ").append(getShotNumber());
        sb.append("]");

        return sb.toString();
    }
}
