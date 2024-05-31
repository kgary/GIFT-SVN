/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PhaseEnum;
import mil.arl.gift.tools.services.file.FileServices;

/**
 * Used to provide additional, course runtime, information around a course object.
 * @author mhoffman
 *
 */
public class CourseObjectWrapper {
    
    /** the course object that was either authored or generated */
    private Serializable courseObject;
    
    /** a reference to the content (e.g. metadata file, dkf, powerpoint file, URL) that the course object is trying to present to the learner  */
    private CourseObjectReference courseObjectReference;
    
    /** whether the course object is providing remediation of some kind */
    private boolean remediationObject;
    
    /** enumerated types of adaptive courseflow phases where content is delivered */
    private PhaseEnum phaseEnum;

    /**
     * Set attributes
     * @param courseObject  the course object that was either authored or generated.  Can't be null. 
     * @param courseObjectReference a reference to the content (e.g. metadata file, dkf, powerpoint file, 
     * URL) that the course object is trying to present to the learner. Can't be null.
     */
    public CourseObjectWrapper(Serializable courseObject, CourseObjectReference courseObjectReference){
        
        if(courseObject == null){
            throw new IllegalArgumentException("The course object is null");
        }else if(courseObjectReference == null){
            throw new IllegalArgumentException("The course object reference is null");
        }
        
        this.courseObject = courseObject;
        this.courseObjectReference = courseObjectReference;
    }
    
    /**
     * Return the course object that was either authored or generated 
     * @return the course object.  Won't be null.
     */
    public Serializable getCourseObject() {
        return courseObject;
    }

    /**
     * Return a reference to the content (e.g. metadata file, dkf, powerpoint file, URL) that the course 
     * object is trying to present to the learner
     * @return the reference container, won't be null.
     */
    public CourseObjectReference getCourseObjectReference() {
        return courseObjectReference;
    }
    
    /**
     * Return whether the course object is providing remediation of some kind 
     * @return default is false
     */
    public boolean isRemediationObject() {
        return remediationObject;
    }

    /**
     * Set whether the course object is providing remediation of some kind 
     * @param remediationObject true if the course object contained in this wrapper is for remediation
     */
    public void setRemediationObject(boolean remediationObject) {
        this.remediationObject = remediationObject;
    }
    
    /**
     * Return the enumerated types of adaptive courseflow phases where content is delivered
     * @return can be null
     */
    public PhaseEnum getPhaseEnum() {
        return phaseEnum;
    }

    /**
     * Set the enumerated types of adaptive courseflow phases where content is delivered
     * @param phaseEnum can be null
     */
    public void setPhaseEnum(PhaseEnum phaseEnum) {
        this.phaseEnum = phaseEnum;
    }

    /**
     * Factory method used to generate an ordered list of course object wrappers for the list of
     * course objects provided.
     * 
     * @param courseObjects zero or more course objects to create new course object wrappers for.
     * @param authoredCourseFolder a pointer to the workspace course folder where the source of the course is located
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     * @return collection of new CourseObjectWrapper objects, one for each of the course objects provided in the list.
     */
    public static List<CourseObjectWrapper> generateCourseObjectWrappers(List<Serializable> courseObjects, AbstractFolderProxy authoredCourseFolder) throws IOException{
        
        List<CourseObjectWrapper> wrappers = new ArrayList<>(courseObjects.size());
        for(Serializable courseObject : courseObjects){
            wrappers.add(generateCourseObjectWrapper(courseObject, authoredCourseFolder));
        }
        
        return wrappers;
    }

    /**
     * Factory method used to generate a new wrapper around a course object.
     * 
     * @param courseObject the course object that was either authored or generated.  Can't be null. 
     * @param authoredCourseFolder a pointer to the workspace course folder where the source of the course is located. Can't be null.
     * @return a new CourseObjectWrapper around the course object
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    public static CourseObjectWrapper generateCourseObjectWrapper(Serializable courseObject, AbstractFolderProxy authoredCourseFolder) throws IOException{
        
        CourseObjectWrapper wrapper = null;
        if(courseObject == null){
            throw new IllegalArgumentException("the course object is null");
        }
               
        CourseObjectReference courseObjectReference = generateCourseObjectReference(courseObject, authoredCourseFolder, null);
        wrapper = new CourseObjectWrapper(courseObject, courseObjectReference);
        
        return wrapper;
    }
    
    /**
     * Factory method used to generated a new course object reference for a course object.
     * 
     * @param courseObject the course object that was either authored or generated.  Cant be null.
     * @param authoredCourseFolder a pointer to the workspace course folder where the source of the course is located. Can't be null.
     * @param alternativeMetadataFileReference the authored course folder relative path to the metadata file that was used to build the course
     * object.  Can be null.  Used as a backup reference in cases where a DKF was generated in the runtime course folder.
     * @return a new CourseObjectReference that contains a reference to the resource used by the course object
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    public static CourseObjectReference generateCourseObjectReference(Serializable courseObject, AbstractFolderProxy authoredCourseFolder, String alternativeMetadataFileReference) throws IOException{
        
        CourseObjectReference courseObjectReference = null;
        if(courseObject == null){
            throw new IllegalArgumentException("the course object is null");
        }else if(courseObject instanceof generated.course.Recall.PresentSurvey){
            courseObjectReference = new CourseObjectNameRef("Recall Survey");
        }else if(courseObject instanceof generated.course.TrainingApplication){
            // create reference to the dkf file
            
            generated.course.TrainingApplication tApp = (generated.course.TrainingApplication)courseObject;
            String dkfFileName = tApp.getDkfRef().getFile();
            
            String workspacesRelativeFilename = null;
            try{
                FileProxy dkfFileProxy = authoredCourseFolder.getRelativeFile(dkfFileName);
                workspacesRelativeFilename = FileServices.getInstance().getFileServices().trimWorkspacesPathFromFullFilePath(dkfFileProxy.getFileId());
            }catch(@SuppressWarnings("unused") Exception e){
                // ignore for now
                // E.g. if the simplest.dkf.xml was copied into the runtime folder for PowerPoint it won't be in
                //      the authored course folder. 
                if(StringUtils.isNotBlank(alternativeMetadataFileReference)){
                    workspacesRelativeFilename = alternativeMetadataFileReference;
                }
            }
           
            if(StringUtils.isNotBlank(workspacesRelativeFilename)){
                courseObjectReference = new WorkspaceRelativeFileCourseObjectRef(workspacesRelativeFilename);
            }else{
                //fall back
                courseObjectReference = new CourseObjectNameRef(tApp.getTransitionName());
            }
            
        }else if(courseObject instanceof generated.course.Guidance){
            courseObjectReference = new CourseObjectNameRef(((generated.course.Guidance)courseObject).getTransitionName());
        }else if(courseObject instanceof generated.course.LessonMaterial){
            
            generated.course.LessonMaterial lessonMaterial = (generated.course.LessonMaterial)courseObject;
            
            if(lessonMaterial.getLessonMaterialList() != null &&
                    lessonMaterial.getLessonMaterialList().getMedia().size() == 1){
                // when there is only one lesson material, use its URI as the resource to reference
                
                generated.course.Media media = lessonMaterial.getLessonMaterialList().getMedia().get(0);
                String workspacesRelativeRuntimeCourseFolder = FileServices.getInstance().getFileServices().trimWorkspacesPathFromFullFilePath(authoredCourseFolder.getFileId());
                courseObjectReference = new WorkspaceRelativeFileCourseObjectRef(workspacesRelativeRuntimeCourseFolder + File.separator + media.getUri());
            }else{
                courseObjectReference = new CourseObjectNameRef(((generated.course.LessonMaterial)courseObject).getTransitionName());
            }
        }else if(courseObject instanceof generated.course.PresentSurvey){
            courseObjectReference =new CourseObjectNameRef(((generated.course.PresentSurvey)courseObject).getTransitionName());
        } else {
            // fall through case
            courseObjectReference = new CourseObjectNameRef("UNKNOWN");
        }
        
        return courseObjectReference;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseObjectWrapper: courseObject = ");
        builder.append(courseObject);
        builder.append(", courseObjectReference = ");
        builder.append(courseObjectReference);
        builder.append(", remediationObject = ");
        builder.append(remediationObject);
        builder.append(", phaseEnum = ");
        builder.append(phaseEnum);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Contains a reference to a workspace file that was used to create a course object.
     * 
     * @author mhoffman
     *
     */
    public static class WorkspaceRelativeFileCourseObjectRef implements CourseObjectReference{
        
        /** 
         * workspace relative file<br\>
         * (e.g. Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml) 
         */
        private String workspaceRelativeFile;
        
        /**
         * Set attribute
         * @param workspaceRelativeFile workspace relative file.  Cant be empty or null.<br\>
         * (e.g. Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml) 
         */
        public WorkspaceRelativeFileCourseObjectRef(String workspaceRelativeFile){
            
            if(StringUtils.isBlank(workspaceRelativeFile)){
                throw new IllegalArgumentException("The workspace relative file can't be null or empty.");
            }
            this.workspaceRelativeFile = workspaceRelativeFile;
        }

        /**
         * Return workspace relative file<br\>
         * (e.g. Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml) 
         * @return wont be null or empty
         */
        public String getContentFile() {
            return workspaceRelativeFile;
        }

        @Override
        public String getContentReference() {
            return workspaceRelativeFile;
        }
        
        
    }
    
    /**
     * Contains a reference to a URL that was used to create a course object.
     * 
     * @author mhoffman
     *
     */
    public class URLRef implements CourseObjectReference{
        
        /** URL that was used to create a course object*/
        private URL url;
        
        /**
         * Set attribute 
         * @param url URL that was used to create a course object. Can't be null
         */
        public URLRef(URL url){
            
            if(url == null){
                throw new IllegalArgumentException("The URL is null");
            }else if(StringUtils.isBlank(url.toString())){
                throw new IllegalArgumentException("The URL value is blank");
            }
            this.url = url;
        }

        /**
         * Return URL that was used to create a course object
         * @return won't be null
         */
        public URL getUrl() {
            return url;
        }

        @Override
        public String getContentReference() {
            return url.toString();
        }
        
        
    }
    
    /**
     * Contains a reference to a course object name that was used to create a course object.
     * 
     * @author mhoffman
     *
     */
    public static class CourseObjectNameRef implements CourseObjectReference{
        
        /** course object name that was used to create a course object */
        private String courseObjectName;
        
        /**
         * Set attribute 
         * @param courseObjectName course object name that was used to create a course object.  Can't be null or empty.
         */
        public CourseObjectNameRef(String courseObjectName){
            
            if(StringUtils.isBlank(courseObjectName)){
                throw new IllegalArgumentException("The course object name can't be null or blank");
            }
            this.courseObjectName = courseObjectName;
        }
        
        @Override
        public String getContentReference(){
            return courseObjectName;
        }
    }
    
    /**
     * The common interface used for references that were used to create a course object.
     * 
     * @author mhoffman
     *
     */
    public interface CourseObjectReference{
        
        /**
         * Return the reference that was used to create a course object.
         * @return the reference (file name, URL, course object name).  Null is also possible.
         */
        public String getContentReference();
    }
}
