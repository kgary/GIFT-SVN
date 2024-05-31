/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialManager;

/**
 * This class has the logic to retrieve the necessary information for a lesson material course transition.
 * 
 * @author mhoffman
 *
 */
public class LessonMaterialHandler implements TransitionHandler{
    
    /** Manager of the lesson material for this domain session */
    private LessonMaterialManager lessonMaterialManager;
    
    /** course transition information */
    private generated.course.LessonMaterial lessonMaterialTransition;

    /**
     * Class constructor - set attribute
     * 
     * @param lessonMaterialTransition the course transition information
     * @param courseDirectory the course folder that contains all course relevant files
     * @param runtimeCourseFolderRelativePath the relative path of the runtime course folder from the domain folder, set during initialization of this class
     */
    public LessonMaterialHandler(generated.course.LessonMaterial lessonMaterialTransition, AbstractFolderProxy courseDirectory, String runtimeCourseFolderRelativePath) {

        lessonMaterialManager = new LessonMaterialManager(courseDirectory, runtimeCourseFolderRelativePath);
        
        this.lessonMaterialTransition = lessonMaterialTransition;
    }
    
    /**
     * Return the lesson material list (expanded) for this course transition
     * 
     * @return generated.course.LessonMaterialList
     */
    public generated.course.LessonMaterialList getLessonMaterial(){
        
        lessonMaterialManager.clear();
        lessonMaterialManager.addLessonMaterialList(lessonMaterialTransition.getLessonMaterialList());
        lessonMaterialManager.addLessonMaterialFiles(lessonMaterialTransition.getLessonMaterialFiles());
        
        generated.course.LessonMaterialList lessonMaterial = new  generated.course.LessonMaterialList();
        lessonMaterial.setIsCollection(lessonMaterialManager.getLessonMaterial().getIsCollection());
        lessonMaterial.getMedia().addAll(lessonMaterialManager.getLessonMaterial().getMedia());
        
        //ensure that the assessment for this lesson material is carried over
        if(lessonMaterialTransition.getLessonMaterialList() != null){
            lessonMaterial.setAssessment(lessonMaterialTransition.getLessonMaterialList().getAssessment());
        }
        
        return lessonMaterial;
    }
}
