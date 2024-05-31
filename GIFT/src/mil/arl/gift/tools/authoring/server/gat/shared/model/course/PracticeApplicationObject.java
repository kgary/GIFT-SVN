/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.model.course;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import generated.course.LessonMaterialList;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;

/**
 * A representation of a training application object used for the Practice quadrant in a Merrill's Branch Point.
 *
 * @author nroberts
 */
public class PracticeApplicationObject implements IsSerializable{
	
	/** The training application information. */
	private TrainingAppCourseObjectWrapper trainingApplication;
	
    /** The lesson material information. */
	private LessonMaterialList lessonMaterial;
	
	/**  
	 * Mapping of the workspace-relative paths (e.g. 'Public/mhoffman/How to change a tire/lugNuts.metadata.xml') of this 
	 * practice application's associated metadata files to the metadata object for that file. 
	 */
	private Map<String, generated.metadata.Metadata> metadataFilesMap;
	
	/** 
	 * flag used to indicate if the practice application metadata contains only the required concepts and
	 * not other course concepts not in the required list
	 */
	private boolean containsOnlyRequiredConcepts = true;
	
	/**
	 * Instantiates a new training application object.
	 */
	public PracticeApplicationObject(){
	}
	
	/**
	 * Resets the practice objects back to a null.
	 */
	private void resetPracticeObjects() {
	    trainingApplication = null;
	    lessonMaterial = null;
	}

    /**
     * Gets the training application. Can be null if the practice application is a lesson material.
     *
     * @return the training application. Can be null.
     */
    public TrainingAppCourseObjectWrapper getTrainingApplication() {
        return trainingApplication;
    }

    /**
     * Sets the training application. Will nullify any other practice objects set.
     *
     * @param trainingApplication the new training application
     */
    public void setTrainingApplication(TrainingAppCourseObjectWrapper trainingApplication) {
        resetPracticeObjects();
        this.trainingApplication = trainingApplication;
    }

    /**
     * Gets the lesson material list. Can be null if the practice application is a training
     * application.
     *
     * @return the lesson material list. Can be null.
     */
    public LessonMaterialList getLessonMaterial() {
        return lessonMaterial;
    }

    /**
     * Sets the lesson material list. Will nullify any other practice objects set.
     *
     * @param lessonMaterial the new lesson material list for the practice application.
     */
    public void setLessonMaterial(LessonMaterialList lessonMaterial) {
        resetPracticeObjects();
        this.lessonMaterial = lessonMaterial;
    }

    /**
	 * Gets this practice application's associated metadata files map.
	 *
	 * @return Mapping of the workspace-relative paths (e.g. 'Public/mhoffman/How to change a tire/lugNuts.metadata.xml') of this 
     * practice application's associated metadata files to the metadata object for that file. 
	 */
	public Map<String, generated.metadata.Metadata> getMetadataFilesMap() {
		return metadataFilesMap;
	}

	/**
	 * Sets this practice application's associated metadata files.
	 *
	 * @param metadataFilesMap Mapping of the workspace-relative paths (e.g. 'Public/mhoffman/How to change a tire/lugNuts.metadata.xml') of this 
     * practice application's associated metadata files to the metadata object for that file. 
	 */
	public void setMetadataFilesMap(Map<String, generated.metadata.Metadata> metadataFilesMap) {
		this.metadataFilesMap = metadataFilesMap;
	}

	/**
	 * Return whether the practice application metadata contains on the required concepts and
     * not other course concepts not in the required list
     * 
	 * @return true by default
	 */
    public boolean doesOnlyContainRequiredConcepts() {
        return containsOnlyRequiredConcepts;
    }

    /**
     * Set the flag used to indicate if the practice application metadata contains on the required concepts and
     * not other course concepts not in the required list
     * 
     * @param containsOnlyRequiredConcepts
     */
    public void setContainsOnlyRequiredConcepts(boolean containsOnlyRequiredConcepts) {
        this.containsOnlyRequiredConcepts = containsOnlyRequiredConcepts;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[PracticeApplicationObject: trainingApplication=");
        builder.append(trainingApplication);
        builder.append(", lessonMaterial=");
        builder.append(lessonMaterial);
        builder.append(", metadataFiles=");
        builder.append(metadataFilesMap);
        builder.append(", containsOnlyRequiredConcepts=");
        builder.append(containsOnlyRequiredConcepts);
        builder.append("]");
        return builder.toString();
    }
	
	
}
