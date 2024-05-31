/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.wrap;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.io.FileTreeModel;

/**
 * A representation of a training application object that can be authored using GIFT WRAP.
 * 
 * @author nroberts
 */
public class TrainingApplicationObject implements IsSerializable {

    /** The path to this object. */
    private FileTreeModel libraryPath;

    /** This library's training application information. */
    private TrainingAppCourseObjectWrapper trainingApplication;

    /**
     * Instantiates a new training application library.
     */
    @SuppressWarnings("unused")
    private TrainingApplicationObject() {

    }

    /**
     * Instantiates a new training application library with the given path and training application
     *
     * @param libraryPath the library path
     * @param trainingApplication the training application
     */
    public TrainingApplicationObject(FileTreeModel libraryPath, TrainingAppCourseObjectWrapper trainingApplication) {
        this.libraryPath = libraryPath;
        this.trainingApplication = trainingApplication;
    }

    /**
     * Gets the library path.
     *
     * @return the library path
     */
    public FileTreeModel getLibraryPath() {
        return libraryPath;
    }

    /**
     * Sets the library path.
     *
     * @param libraryPath the new library path
     */
    public void setLibraryPath(FileTreeModel libraryPath) {
        this.libraryPath = libraryPath;
    }

    /**
     * Gets the training application.
     *
     * @return the training application
     */
    public TrainingAppCourseObjectWrapper getTrainingApplication() {
        return trainingApplication;
    }

    /**
     * Sets the training application.
     *
     * @param trainingApplication the new training application
     */
    public void setTrainingApplication(TrainingAppCourseObjectWrapper trainingApplication) {
        this.trainingApplication = trainingApplication;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[TrainingApplicationObject: ");
        sb.append("libraryPath = ").append(getLibraryPath());
        
        if (getTrainingApplication() != null && getTrainingApplication().getTrainingApplicationObj() != null) {
            sb.append(", training application = ")
                    .append(getTrainingApplication().getTrainingApplicationObj().getTransitionName());
        }
        
        sb.append("]");

        return sb.toString();
    }
}
