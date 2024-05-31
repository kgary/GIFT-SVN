/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.Course;
import generated.dkf.Assessments;
import generated.dkf.Concept;
import generated.dkf.Question;
import generated.dkf.Questions;
import generated.dkf.Reply;
import generated.dkf.Scenario;
import generated.dkf.Task;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;

/**
 * This class provides logic to import survey contexts that were exported using the GIFT export tutor tool.
 * The result of importing the survey contexts as new survey elements may cause courses and/or DKFs to be updated
 * as well (i.e. the files are updated).
 * 
 * @author mhoffman
 *
 */
public class CourseDBImporter {
    
    private static Logger logger = LoggerFactory.getLogger(CourseDBImporter.class);
    
    public static final String COURSE_SURVEY_REF_EXPORT_SUFFIX = ".course" + FileUtil.SURVEY_REF_EXPORT_SUFFIX;
    private static final String DKF_SURVEY_REF_EXPORT_SUFFIX = ".dkf" + FileUtil.SURVEY_REF_EXPORT_SUFFIX;
    
    private static final String ORIGINAL_DOMAIN_FILE_SUFFIX = ".IMPORTED.BACKUP";

    /**
     * Search the specified domain directory for survey export files.  For each file found, import the
     * survey context and all referenced survey elements.  If a particular type of survey element's unique id
     * is changed when inserted into the database, write the update to the DKF or course file.
     * 
     * Notes:  
     *    1) for each export file found, the survey context db inserts will happen in the same transaction.  
     *          If there is an exception, the inserts will be rolled back and an exception will be thrown.
     *    2) for each export file found, after survey context db insert, all related DKF and course files that need to be
     *        updated will have the original files copied to a new file name.  If writing the new update content to the original
     *        file name fails an exception will be thrown.
     *    3) If an exception is thrown, the exported domain content should not be imported (at all).
     * 
     * @param username the user importing the course
     * @param tempDomainDirectory the root directory containing all course folders
     * @param movedSurveyImages an optional mapping identifying survey images that have changed locations and need their references updated. If null or empty, no
     * image references will be changed.  Key: original survey image location value as used in a survey item image reference, Value: the new location
     * normally in a new generated folder.
     * @param usersWorkspaceFolder the user's workspace folder to import the course(s) to.  Can't be null.
     * @return String a summary of the changes made for the survey imports.
     * @throws Exception if there was a severe error importing surveys or updating DKF or course files.
     */
    public static String importSurveyExports(String username, DesktopFolderProxy tempDomainDirectory, Map<String, String> movedSurveyImages, AbstractFolderProxy usersWorkspaceFolder) throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Searching domain directory of "+tempDomainDirectory+" for survey export files.");
        }
        
        //check for survey export for this export file
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, FileUtil.SURVEY_REF_EXPORT_SUFFIX);
        
        if(logger.isInfoEnabled()){
            logger.info("Found "+files.size()+" survey export files.");
        }
        
        Map<FileProxy, AbstractSchemaHandler> filesToUpdate = new HashMap<>();
        
        //Do all db inserts for this file in one session, if exception then roll back.  Then throw exception to halt import.
        //If no exception, commit after finishing course/dkf updates
        Session session = UMSDatabaseManager.getInstance().createNewSession(); 
        session.beginTransaction();
        
        //record of the changes made to accommodate the survey imports
        StringBuffer summary = new StringBuffer();
        summary.append("Survey Import Summary:\n");
        
        InputStream is;
        BufferedReader br;
        String line;
        
        // The mapper helps prevent multiple database entries i.e. if a course uses the same survey multiple times
        ExternalSurveyMapper mapper = new ExternalSurveyMapper();
        
        try{
        	
        	boolean handledCourseReferences = false; //whether or not at least one survey export has had its course references handled
        	
        	Iterator<FileProxy> filesItr = files.iterator();
        
            while(filesItr.hasNext()){
            	
            	FileProxy fileProxy = filesItr.next();
                
                DesktopFolderProxy courseFolder = (DesktopFolderProxy) tempDomainDirectory.getParentFolder(fileProxy);
                
                if(logger.isInfoEnabled()){
                    logger.info("Handling survey export file of "+fileProxy.getFileId()+".");
                }
                
                //read in JSON entry(ies).
                is = fileProxy.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                
                while((line = br.readLine()) != null){
                    JSONObject obj = (JSONObject) JSONValue.parse(line);
                    SurveyContextJSON json = new SurveyContextJSON();
                    SurveyContext surveyContext = (SurveyContext) json.decode(obj);
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Replacing permissions for survey context " + surveyContext.getName() + " with " +"<" + username + ">."); 
                    }
	                    
	                surveyContext.getEditableToUserNames().clear();
	                surveyContext.getVisibleToUserNames().clear();
	                    
	                surveyContext.getEditableToUserNames().add(username);
	                surveyContext.getVisibleToUserNames().add(username);
	                
	                if(surveyContext.getContextSurveys() != null){
                    	
	                	//if any images used by this survey context were moved, then we need to update all of the questions using those images
	                	//so that they point to the new locations.
                    	for(SurveyContextSurvey contextSurvey : surveyContext.getContextSurveys()){
                    		
                    		if(contextSurvey.getSurvey() != null){
                    		    
                    		    if(contextSurvey.getSurvey().getProperties() != null 
                                        && contextSurvey.getSurvey().getProperties().hasProperty(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE)){
                                    
                    		        /* Make sure that attempting to copy media items from this survey properly pulls them
                    		         * from the course folder's destination location */
                                    contextSurvey.getSurvey().getProperties().setPropertyValue(
                                            SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE, 
                                            usersWorkspaceFolder.getName() + Constants.FORWARD_SLASH + courseFolder.getName());
                                }
                    		    
                    		    if(movedSurveyImages == null 
                    		            || movedSurveyImages.isEmpty()
                    		            || contextSurvey.getSurvey().getPages() == null){
                    		        
                    		        /* Skip the remaining logic, since we do not need to update image references */
                    		        continue;
                    		    }
                    			
                    			for(SurveyPage page: contextSurvey.getSurvey().getPages()){
                    				
                    				if(page.getElements() != null){
                    					
                    					for(AbstractSurveyElement element : page.getElements()){
                    						
                    						if(element instanceof AbstractSurveyQuestion<?>){
                    							
                    							Object question = ((AbstractSurveyQuestion<?>) element).getQuestion();
                    							
                    							if(question instanceof AbstractQuestion){
                    								
                    								AbstractQuestion absQuestion = (AbstractQuestion) question;
                    								
                    								Serializable questionImage = absQuestion.getProperties().getPropertyValue(
                    										SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY
                    								);
                        							
                        							if(questionImage != null && movedSurveyImages.containsKey(questionImage)){
                        							
                        								absQuestion.getProperties().setPropertyValue(
                            									SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, 
                            									movedSurveyImages.get(questionImage)
                            							);
                        							}
                    							}
                    						}
                    					}
                    				}
                    			}
                    		}
                    	}
                    }
                    
                    if (mapper != null && !mapper.surveyContextExists(surveyContext.getId())) {
                    	
	                    //insert survey context into db 
	                    //Note: its various unique ids that are table primary keys will be changed to 0 before being inserted.  The insert
	                    //      process will create new unique ids.  The original and new ids will be contained in the mapper object for later reference.
	                    SurveyContext newSurveyContext = Surveys.insertExternalSurveyContext(surveyContext, mapper, null, session);
	                    if(newSurveyContext == null){
	                        //ERROR
	                        br.close();
	                        throw new Exception("Failed to import the survey context with original id of "+surveyContext.getId()+" from "+fileProxy.getFileId()+".");
	                    }
	                    
                    }
                }//end while
                
                br.close();
                if(logger.isInfoEnabled()){
                    logger.info("Finished inserting the database entries in the same database transaction (yet to be committed).");
                }
                
                //for each export file, determine if for a course or a dkf
                String filename = fileProxy.getFileId();
                if(filename.endsWith(COURSE_SURVEY_REF_EXPORT_SUFFIX)){
                    //found course
                    
                    //get course file with similar name
                    String courseFileName = filename.replace(FileUtil.SURVEY_REF_EXPORT_SUFFIX, Constants.XML);
                    File courseFile = new File(courseFileName);
                    if(courseFile.exists()){
                    	
                    	FileProxy courseFileProxy = new FileProxy(courseFile);
                        handleCourseSurveyReferences(courseFileProxy, mapper, filesToUpdate, courseFolder);
                        
                        handledCourseReferences = true;
                        
                    }else{
                    	
                    	if(filesItr.hasNext()){                    		
                    	    
                    	    if(logger.isInfoEnabled()){
                                logger.info("Unable to find a course file named " + courseFile + " for the survey export file named " + fileProxy.getFileId() 
	                    		+ ". This survey export file was likely left over from an import made in an older version of GIFT by accident, "
	                    		+ "so updating course references will be skipped for this survey export.");
                    	    }
                    	
                    	} else if(!handledCourseReferences){
                    		
                    		//ERROR
                    		throw new Exception("Unable to find a course file named " + courseFile + " while trying to import the survey "
                    				+ "export file named " + fileProxy.getFileId() +". No matching course files were found for any of "
                    				+ "this course's survey exports.");
                    	}
                    }
                    
                }else if(filename.endsWith(DKF_SURVEY_REF_EXPORT_SUFFIX)){
                    //found dkf
                    
                    //get DKF with similar name
                    String dkfName = filename.replace(FileUtil.SURVEY_REF_EXPORT_SUFFIX, Constants.XML);
                    File dkf = new File(dkfName);
                    if(dkf.exists()){
                        handleDKFSurveyReferences(new FileProxy(dkf), courseFolder, mapper, filesToUpdate);
                    }else{
                        //ERROR
                        throw new Exception("While trying to import the survey export file of "+fileProxy.getFileId()+", unable to find the DKF named "+dkf+".");
                    }
                    
                }else{
                    //ERROR
                    throw new Exception("Unable to handle survey export file of "+fileProxy.getFileId()+" because it has an unknown file extension.");
                }           
                
            }//end files for
            
            //write id changes to summary
            writeSurveyIdChangesSummary(mapper, summary);
            
            if(logger.isInfoEnabled()){
                logger.info("Starting to update "+filesToUpdate.size()+" files.");
            }
            
            summary.append("\n\nOriginal Imported Files Backedup:\n");
            
            //
            // Update Files
            //
            for(FileProxy fileToUpdate : filesToUpdate.keySet()){
                
                File actualFileToUpdate = new File(fileToUpdate.getFileId());
                AbstractSchemaHandler handler = filesToUpdate.get(fileToUpdate);
                
                if(handler instanceof DomainCourseFileHandler){
                    
                    //backup the original file
                    File backup = new File(fileToUpdate.getFileId() + ORIGINAL_DOMAIN_FILE_SUFFIX);                    
                    Files.copy(Paths.get(actualFileToUpdate.toURI()), Paths.get(backup.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Copied original Course file of "+fileToUpdate+" to "+backup+".");
                    }
                    
                    //write to summary
                    summary.append("\nCopied original Course file of ").append(fileToUpdate).append(" to ").append(backup).append(".");
                    
                    try{
                        //write the new contents to the original file name
                        //Note: continue to write even if invalid which allows disabled (incomplete) course objects to be written [#3570]
                        DomainCourseFileHandler.writeToFile(((DomainCourseFileHandler) handler).getCourse(), actualFileToUpdate, true);
                    }catch(Exception e){
                        throw new Exception("Failed to write an updated version of "+actualFileToUpdate.getName(), e);
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Successfully updated survey references for the course file of "+fileToUpdate);
                    }
                    
                    //write to summary
                    summary.append("\nSuccessfully updated survey references for the course file of ").append(fileToUpdate);
                    
                }else if(handler instanceof DomainDKFHandler){
                    
                    //backup the original file
                    File backup = new File(fileToUpdate.getFileId() + ORIGINAL_DOMAIN_FILE_SUFFIX);                    
                    Files.copy(Paths.get(actualFileToUpdate.toURI()), Paths.get(backup.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Copied original DKF of "+fileToUpdate+" to "+backup+".");
                    }
                    
                    //write to summary
                    summary.append("\nCopied original DKF of ").append(fileToUpdate).append(" to ").append(backup).append(".");
                    
                    try{
                        //write the new contents to the original file name
                        //Note: continue to write even if invalid which allows disabled (incomplete) course objects to be written [#4471]
                        DomainDKFHandler.writeToFile(((DomainDKFHandler) handler).getScenario(), actualFileToUpdate, true);
                    }catch(Exception e){
                        throw new Exception("Failed to write an updated version of "+actualFileToUpdate.getName(), e);
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Successfully updated survey references for the DKF of "+fileToUpdate);
                    }
                    
                    //write to summary
                    summary.append("\nSuccessfully updated survey references for the DKF of ").append(fileToUpdate);
                    
                }else{
                    //ERROR
                    throw new Exception("Found unhandled schema handler of "+handler+" for file to update of "+fileToUpdate+".");
                }
            }
            
            if(logger.isInfoEnabled()){
                logger.info("Committing database transaction...");
            }
            
            session.getTransaction().commit();
            session.close();
            
            if(logger.isInfoEnabled()){
                logger.info("Database commit successful.");
            }
            
        }catch(Throwable e){
            logger.error("Caught exception while trying to import survey export(s).  Rolling back database transaction.", e);
            
            session.getTransaction().rollback();
            
            throw new DetailedException("Failed to import all "+files.size()+" survey exports found in "+tempDomainDirectory+".",
                    "There was an exception thrown that reads:\n"+e.getMessage(),
                    e);
        }
        
        return summary.toString();
    } 
    
    /**
     * Write the survey mapper contents to the summary in order to document the changes being made to 
     * survey items during the survey context import(s).
     * 
     * @param mapper contains the changes made to ids for specific survey items during the import process
     * @param summary where to write a summary of the changes made
     */
    private static void writeSurveyIdChangesSummary(ExternalSurveyMapper mapper, StringBuffer summary){        
        summary.append(mapper.toString());
    }
    
    /**
     * Check for survey item id updates in the specified course file using the mapping of id changes.
     * 
     * @param courseFileProxy the course file to check for and update survey item ids based on the mappings provided
     * @param mapper contains the mapping of original to new survey item ids
     * @param filesToUpdate a collection of files needing to be updated, i.e. written to disk
     * @param courseFolder the course folder used by the DKF for course relative paths
     * @throws IOException if there was a problem retrieving the course file
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
     */
    public static void handleCourseSurveyReferences(FileProxy courseFileProxy, ExternalSurveyMapper mapper, Map<FileProxy, AbstractSchemaHandler> filesToUpdate, 
            AbstractFolderProxy courseFolder) throws IOException, CourseFileValidationException, FileValidationException, DKFValidationException{
        
    	copyCourseSurveyReferences(courseFileProxy, mapper, filesToUpdate, courseFolder);
    }
    
    /**
     * Check for survey item id updates in the specified course file using the mapping of id changes.
     * 
     * @param courseFileProxy the course file to check for and update survey item ids based on the mappings provided
     * @param mapper contains the mapping of original to new survey item ids
     * @param filesToUpdate a collection of files needing to be updated, i.e. written to disk
     * @param courseFolder the course folder used by the DKF for course relative paths
     * @throws IOException if there was a problem retrieving the course file
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
     */
    public static void copyCourseSurveyReferences(FileProxy courseFileProxy, ExternalSurveyMapper mapper, Map<FileProxy, AbstractSchemaHandler> filesToUpdate, 
            AbstractFolderProxy courseFolder) throws IOException, CourseFileValidationException, FileValidationException, DKFValidationException{
        
        if(logger.isInfoEnabled()){
            logger.info("Handling course survey references for course file named "+courseFileProxy);
        }
        
        //flag used to indicate if the course file was updated and should be updated on disk (i.e. write the updated contents to the file)        
        boolean updatedCourseFile = false;
        
        //use default schema and DON'T perform GIFT validation as part of survey importing/reference-updates
        
        //Note: the course folder XML files might not be schema valid especially when being up-converted automatically for the user
        //      therefore we need to continue parsing even if there are validation issues
        DomainCourseFileHandler courseHandler = new DomainCourseFileHandler(courseFileProxy, courseFolder, false);
        Course course = courseHandler.getCourse();
        
        //check if the course survey context changed
        if(course.getSurveyContext() != null){
            int courseSurveyContextId = course.getSurveyContext().intValue();
            
            Integer newCourseSurveyContextId = mapper.getNewSurveyContextId(courseSurveyContextId);
            if(newCourseSurveyContextId != null && newCourseSurveyContextId != courseSurveyContextId){
                //the id has changed, update the value
                
                course.setSurveyContext(BigInteger.valueOf(newCourseSurveyContextId));
                updatedCourseFile = true;
            }
        }
        
        //check for other survey references that may need to be updated in DKFs
        List<FileProxy> dkfs = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(courseFolder, dkfs, AbstractSchemaHandler.DKF_FILE_EXTENSION);
        
        for(FileProxy dkf : dkfs){
            handleDKFSurveyReferences(dkf, courseFolder, mapper, filesToUpdate);
        }
        
        //check if the course file was changed
        if(updatedCourseFile){
          //add the file to be updated later
            
            filesToUpdate.put(courseFileProxy, courseHandler);

            if(logger.isInfoEnabled()){
                logger.info("Added course file of "+courseFileProxy+" to collection of files to update.");
            }
        }
    }
    
    /**
     * Check for survey item id updates in the specified DKF using the mapping of id changes.
     * 
     * @param dkfFile the DKF to check for and update survey item ids based on the mappings provided
     * @param courseFolder the course folder used by the DKF for course folder relative paths
     * @param mapper contains the mapping of original to new survey item ids
     * @param filesToUpdate a collection of files needing to be updated, i.e. written to disk
     * @throws IOException if there was a problem retrieving the file
     * @throws FileValidationException if there was a validation issue against the schema
     * @throws DKFValidationException if there was a problem building the DKF objects
     * @throws DetailedException if there was a problem updating the dkf survey context because that survey
     * context wasn't imported
     */
    public static void handleDKFSurveyReferences(FileProxy dkfFile, AbstractFolderProxy courseFolder,
            ExternalSurveyMapper mapper, Map<FileProxy, AbstractSchemaHandler> filesToUpdate) 
                    throws FileValidationException, IOException, DKFValidationException, DetailedException{
        
        if(logger.isInfoEnabled()){
            logger.info("Handling DKF survey references for DKF named "+dkfFile);
        }
        
        boolean updatedDKF = false;

        /* get the dkf parsed and validated against schema Note: the course folder XML files might
         * not be schema valid especially when being up-converted automatically for the user
         * therefore we need to continue parsing even if there are validation issues */
        DomainDKFHandler dkfh = new DomainDKFHandler(dkfFile, courseFolder, null, false);
        Scenario scenario = dkfh.getScenario();
        
        //
        // Check for updated survey context id
        //
        BigInteger domainSurveyContext = scenario.getResources().getSurveyContext();
        if(domainSurveyContext == null){
            //there is no survey context for this DKF, therefore there are no survey references in the file
            return;
        }
        
        int dkfSurveyContextId = domainSurveyContext.intValue();
        Integer newDKFSurveyContextId = mapper.getNewSurveyContextId(dkfSurveyContextId);
        if(newDKFSurveyContextId != null){
            //the id has changed, update the DKF value
            
            scenario.getResources().setSurveyContext(BigInteger.valueOf(newDKFSurveyContextId));
            updatedDKF = true;
        }else{
            throw new DetailedException("The DKF survey context could not be found amongst the imported survey contexts", 
                    "The DKF '"+dkfFile.getFileId()+"' references the survey context "+domainSurveyContext+".  That survey context information "+
                            "wasn't found in the survey contexts being imported therefore that DKF survey context id can't be updated to the new "+
                            "value in the database being used by this GIFT instance.\n\nDoes the course import zip contain a survey context export file that contains the survey context with id "+domainSurveyContext+".  If not, the course export zip is incomplete.", null);
        }
        
        //
        // Check for updated survey question related ids
        //
        for(Task task : scenario.getAssessment().getTasks().getTask()){
            
            Assessments assessments = task.getAssessments();
            updatedDKF |= handleAssessmentsSurveyRefs(assessments, mapper);
            
            for(Concept concept : task.getConcepts().getConcept()){
                updatedDKF |= handleAssessmentsSurveyRefs(concept, mapper);
            }
            
        }//end Task for loop
        
        //check if the DKF was changed
        if(updatedDKF){
            //add the file to be updated later
            
            filesToUpdate.put(dkfFile, dkfh);

            if(logger.isInfoEnabled()){
                logger.info("Added DKF of "+dkfFile+" to collection of files to update.");
            }
        }
    }
    
    /**
     * Handle checking and updating this concept's survey references
     * 
     * @param concept the concept to handle
     * @param mapper contains the mapping of original to new survey item ids
     * @return boolean true if at least one of the concept (or subconcept) had its survey references updated
     */
    private static boolean handleAssessmentsSurveyRefs(Concept concept, ExternalSurveyMapper mapper){
        
        boolean updatedDKF = false;
        
        //deal with this concept
        updatedDKF |= handleAssessmentsSurveyRefs(concept.getAssessments(), mapper);
        
        Serializable child = concept.getConditionsOrConcepts();
        if(child instanceof generated.dkf.Concepts){
            //the concept has sub-concepts, now deal with those
            
            generated.dkf.Concepts concepts = (generated.dkf.Concepts)child;
            for(Concept childConcept : concepts.getConcept()){
                updatedDKF |= handleAssessmentsSurveyRefs(childConcept, mapper);
            }
            
        }
        
        return updatedDKF;
    }
    
    /**
     * Handle checking and updating the DKF's assessments survey references
     * 
     * @param assessments performance node assessment object from a DKF.  Can be null.
     * @param mapper contains the mapping of original to new survey item ids
     * @return boolean true if at least one of the assessments had its survey references updated
     */
    private static boolean handleAssessmentsSurveyRefs(Assessments assessments, ExternalSurveyMapper mapper){
        
        boolean updatedDKF = false;
        
        if(assessments != null){
            
            if(assessments.getAssessmentTypes() != null){
                
                for(Object assessmentTypeObj : assessments.getAssessmentTypes()){
                    
                    if(assessmentTypeObj instanceof Assessments.Survey){
                        //found a survey assessment
                        
                        Assessments.Survey surveyAssessment = (Assessments.Survey) assessmentTypeObj;
                        final Questions questions = surveyAssessment.getQuestions();
                        if (questions != null && !questions.getQuestion().isEmpty()) {

                            for (Question question : questions.getQuestion()){
                                //found a survey question assessment element
                                
                                //check if the survey question id changed
                                int questionId = question.getKey().intValue();
                                Integer newQuestionId = mapper.getNewSurveyQuestionId(questionId);
                                if(newQuestionId != null){
                                    //the id has changed, update the DKF value
                                    
                                    question.setKey(BigInteger.valueOf(newQuestionId));
                                    updatedDKF = true;                                        
                                }
                                
                                for(Reply reply : question.getReply()){
                                    
                                    //check if the survey question reply id changed
                                    int replyId = reply.getKey().intValue();
                                    Integer newReplyId = mapper.getNewSurveyQuestionReplyId(replyId);
                                    if(newReplyId != null){
                                        //the id has changed, update the DKF value
                                        
                                        reply.setKey(BigInteger.valueOf(newReplyId));
                                        updatedDKF = true;
                                    }
                                }
                                
                            }//end Question for loop
                        }
                    }
                }//end Assessment Obj for loop
            }
        }
        
        return updatedDKF;
    }
}
