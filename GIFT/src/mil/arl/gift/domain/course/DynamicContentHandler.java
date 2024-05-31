/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.BooleanEnum;
import mil.arl.gift.common.course.BasicCourseHandler;
import mil.arl.gift.common.course.dkf.AbstractDKFHandler.AdditionalDKFValidationSettings;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ActiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ConstructiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.InteractiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.MetadataAttributeItem;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.PassiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.AbstractCDTCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.AbstractExpandedCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.ExampleCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.PracticeCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.RemediationCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.RuleCourseObject;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileFinder;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileSearchResult;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler.MetadataContentType;
import mil.arl.gift.domain.knowledge.metadata.MetadataSearchCriteria;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PhaseEnum;
import mil.arl.gift.tools.services.file.FileServices;

/**
 * Provides common logic used to build course objects that deliver content by first finding
 * metadata files that match search criteria and then using the activity referenced by that metadata. 
 * 
 * @author mhoffman
 *
 */
public class DynamicContentHandler {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DynamicContentHandler.class);    
    
    /** heading of the structured review shown on the after practice remediation page */
    public static final String AFTER_PRACTICE_REMEDIATION_HEADING = "Review concepts needing remediation after Practice";    
    
    /**
     * A message to display to the learner that indicates why the course was preemptively stopped due to
     * the number of successive remediation needed for the same course object.
     */
    public static final String BAILOUT_DETAILS_MSG = "Unfortunately you have reached the maximum number of attempts for this part of the course."+
            "\nFeel free to retake the course or notify your GIFT instructor/administrator for additional support.";
    
    /** this could contain the last executed quadrant type -or- the quadrant that would have been executed if the pedagogical request didn't change it */
    protected MerrillQuadrantEnum previousQuadrant = null;
    
    /** 
     * contains a translation between DKF practice concepts and their course equivalents.
     * This can be empty if this branch point course element doesn't contain a Practice quadrant.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, String> practiceConceptToCourseConcept = new CaseInsensitiveMap();
    
    /** 
     * flag used to indicate whether the rule and example tagged content for this course object should be excluded from content
     * selection when selecting content for the after recall remediation phase. 
     */
    private boolean excludeRuleExampleContent = false;
    
    /** 
     * contains the content delivered already  
     * This is useful for trying to present new content to the user during remediation.
     */
    protected Set<String> contentDelivered = new HashSet<>();
    
    /** the Domain descendant directory the course is executed from and where the course XML file is in */
    protected DesktopFolderProxy runtimeCourseDirectory;
    
    /** where the authored course resides, useful for updating persistant files like paradata */
    protected AbstractFolderProxy authoredCourseDirectory;
    
    /** the domain directory used by this domain module */
    protected static final File DOMAIN_DIRECTORY = new File(DomainModuleProperties.getInstance().getDomainDirectory());
    
    /** the DKF to use for generated training application transitions */
    private static final String DEFAULT_DKF_FILENAME = DomainModuleProperties.getInstance().getWorkspaceTemplatesDirectory() + File.separator + "simplest.dkf.xml";
    
    /** the dkf file used for generated training application transitions */
    protected static final File DEFAULT_DKF_FILE = new File(DEFAULT_DKF_FILENAME);
    
    /** contains information about the concepts being practice, will be null if not practicing in an adaptive courseflow course object */
    protected generated.course.Practice practiceQuadrant;
    
    /** the name of the course object needing dynamic content handling */
    private String courseObjectName;
    
    /** the current list of course concepts being used to determine what is being taught in either the rule/example or practice phases */
    protected Collection<String> conceptList;      
    
    /**
     * The number of successive remediation needed from the same practice quadrant in this
     * branch point.  This is used as an indicator that the learner is not comprehending
     * the content being taught and may need additional support or try re-taking the course.
     */
    public static int DEFAULT_PRACTICE_BAILOUT_CNT = 3;
    
    /** Indicates that there is no default set for attempts (either practice or recall).  */
    protected static int NO_DEFAULT_SET = -1;
    
    /**
     * the number of allowed practice attempts before prematurely but gracefully ending the course
     */
    protected int allowedPracticeAttempts = NO_DEFAULT_SET;
    
    /**
     * The current count of successive executions of this branch point's practice quadrant.
     * If the learner moves to the next transition past the recall or practice quadrant, the relative counter
     * is reset.  In that scenario the counter can be used again if the recall or practice quadrant is
     * reached later on as a result of remediation somewhere further on in the course execution.
     */
    protected int successivePracticeCount;
    
    /**
     * Set attributes
     * 
     * @param courseObjectName the name of the course object that requires this content handler.  can't be null or empty.
     * @param runtimeCourseDirectory the Domain descendant directory the course is executed from and where the course XML 
     * file is in as well as other content that can be delivered.
     * Can't be null and must exist.
     * @param authoredCourseDirectory the directory where the authored course resides.  Useful for updating persistant files
     * like paradata.
     * @param previousQuadrant the phase the learner just left (e.g. null if starting an adaptive courseflow course object, Practice if
     * just finishing a training app course object).  Can be null.
     */
    public DynamicContentHandler(String courseObjectName, DesktopFolderProxy runtimeCourseDirectory, 
            AbstractFolderProxy authoredCourseDirectory, MerrillQuadrantEnum previousQuadrant){
        
        if(StringUtils.isBlank(courseObjectName)){
            throw new IllegalArgumentException("The course object name can't be null or blank.");
        }else if(runtimeCourseDirectory == null){
            throw new IllegalArgumentException("The runtime course directory can't be null");
        }else if(authoredCourseDirectory == null){
            throw new IllegalArgumentException("The authored course directory can't be null");
        }
        
        this.runtimeCourseDirectory = runtimeCourseDirectory;
        this.authoredCourseDirectory = authoredCourseDirectory;
        this.previousQuadrant = previousQuadrant;
        this.courseObjectName = courseObjectName;
    }
    
    /**
     * Return the name of the course object needing dynamic content handling 
     * @return wont be null or empty.
     */
    public String getCourseObjectName(){
        return courseObjectName;
    }

    /**
     * Return the collection of concepts currently being taught.
     * 
     * @return the current list of course concepts being used to determine what is being taught in either the rule/example or practice phases
     */
    protected Collection<String> getConceptList(){
        
        if(practiceQuadrant != null && practiceQuadrant.getPracticeConcepts() != null){
            conceptList = practiceQuadrant.getPracticeConcepts().getCourseConcept();
        }else{
            conceptList = new ArrayList<>();
        }
        
        return conceptList;
    }
    
    /**
     * Set the number of allowed practice attempts before prematurely but gracefully ending the course
     * @param allowedPracticeAttempts must be greater than zero
     */
    public void setAllowedPracticeAttempts(int allowedPracticeAttempts){
        
        if(allowedPracticeAttempts < 1){
            throw new IllegalArgumentException("The allowed practice attempts of "+allowedPracticeAttempts+" must be greater than zero");
        }
        
        this.allowedPracticeAttempts = allowedPracticeAttempts;
    }
    
    /**
     * Set the practice quadrant information.
     * This will also populate the mapping of practice course concepts to course concepts for use during
     * metadata searching.
     * 
     * @param practiceQuadrant contains information about the concepts being practice, will be null if not 
     * practicing in an adaptive courseflow course object
     */
    public void setPracticeQuadrant(generated.course.Practice practiceQuadrant){
        this.practiceQuadrant = practiceQuadrant;
        
        for(String courseConcept : practiceQuadrant.getPracticeConcepts().getCourseConcept()){
            practiceConceptToCourseConcept.put(courseConcept, courseConcept);
        }
    }    
    
    /**
     * Return the practice quadrant information.
     * 
     * @return contains information about the concepts being practice, will be null if not 
     * practicing in an adaptive courseflow course object.  Will be null until {@link #setPracticeQuadrant(generated.course.Practice)}
     * is called. 
     */
    protected generated.course.Practice getPracticeQuadrant(){
        return practiceQuadrant;
    }
    
    /**
     * Set whether to exclude Rule and Example phase tagged content from the content selection of
     * the after recall remediation phase
     * 
     * @param excludeRuleExampleContent value to use
     */
    public void setExcludeRuleExampleContent(boolean excludeRuleExampleContent){
        this.excludeRuleExampleContent = excludeRuleExampleContent;
    }

    /**
     * Build a course object for the specified adaptive courseflow phase (Rule/Example/Remediation/Practice - the content delivery quadrants) based on the 
     * metadata search criteria and content referenced in the metadata file(s).
     * 
     * @param expandedCourseObject - contains information about the authored adaptive courseflow phase
     * used to help build the content delivery course object
     * @param transitions - the current list of course transition and the collection to add a new content deliver transition to
     * @param remediationContent whether the course objects being created are for remediation activity or not.
     * @throws Exception if there was a problem building the course element for the specified quadrant
     */
    public void buildContentDeliveryPhase(AbstractExpandedCourseObject expandedCourseObject, List<CourseObjectWrapper> transitions, boolean remediationContent) throws Exception{
        
        //
        // build dynamic training app transition
        //
        
        PhaseEnum phase = PhaseEnum.K;
        
        generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
        presentAt.setRemediationOnly(expandedCourseObject instanceof RemediationCourseObject ? generated.metadata.BooleanEnum.TRUE : generated.metadata.BooleanEnum.FALSE);
        if(expandedCourseObject instanceof AbstractCDTCourseObject){
            
            if(expandedCourseObject instanceof RuleCourseObject){
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.RULE.getName());
                phase = PhaseEnum.K;
            }else if(expandedCourseObject instanceof ExampleCourseObject){
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.EXAMPLE.getName());
                phase = PhaseEnum.K;
            }else if(expandedCourseObject instanceof PracticeCourseObject){
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.PRACTICE.getName());
                phase = PhaseEnum.P;
            }
        }

        //create the metadata search parameters
        MetadataSearchCriteria criteria = new MetadataSearchCriteria(presentAt);
        criteria.setExcludeRuleExampleContent(excludeRuleExampleContent);
        
        Collection<String> contentCourseConcepts;
        //MH part of ticket #1100 (look for other comments in this class)
        if(expandedCourseObject instanceof PracticeCourseObject){
            
            //hashset used to remove duplicate course concepts
            Set<String> contentCourseConceptsSet = new HashSet<>(practiceConceptToCourseConcept.values());
            contentCourseConcepts = new ArrayList<>(contentCourseConceptsSet);
        }else if(expandedCourseObject instanceof AbstractCDTCourseObject){
            contentCourseConcepts = ((AbstractCDTCourseObject)expandedCourseObject).getConcepts();
        }else if(expandedCourseObject instanceof RemediationCourseObject){
            contentCourseConcepts = ((RemediationCourseObject)expandedCourseObject).getRemediationInfo().getRemediationMap().keySet();
            phase = PhaseEnum.KR;
        }else{
            throw new Exception("Found an unhandled adaptive courseflow expanded course object of "+expandedCourseObject);
        }
        
        //when Practicing any DKF reference must contain the practice course concepts and those performance nodes
        //must have at least 1 scoring rule
        AdditionalDKFValidationSettings additionalValidation = null;
        if(expandedCourseObject instanceof PracticeCourseObject){
            additionalValidation = new AdditionalDKFValidationSettings("Matching practice course concepts to DKF assessed nodes.");
            additionalValidation.setNodeNamesToMatch(contentCourseConcepts);
            additionalValidation.setMatchedNamesNeedScoring(true);
            criteria.setAdditionalValidation(additionalValidation);
        }
        
        //for each concepts being taught in the phase, associate the (appropriate) metadata attributes.
        //This info will be used as a metadata search filter
        Map<String, List<AbstractRemediationConcept>> remediationMap;
        if(expandedCourseObject instanceof RemediationCourseObject){
            RemediationInfo remediationInfo = ((RemediationCourseObject)expandedCourseObject).getRemediationInfo();
            remediationMap = remediationInfo.getRemediationMap();
        }else{
            remediationMap = ((AbstractCDTCourseObject)expandedCourseObject).getRemediationMap();
        }
        
        //build the concept based criteria to use to search for metadata
        //Note: the course concepts (contentCourseConcepts) can be different than the adaptive courseflow 
        //      defined concepts in the following cases:
        // 1. {rule/example/recall/remediation} concept set can be a subset of Practice concepts 
        //        (i.e. see how the GAT allows authors to select more concepts for practice phase)
        // 2. After Practice remediation can contain concepts taught in previous adaptive course flows (because of 1)
        //
        for(String phaseConcept : contentCourseConcepts){
            
            // previousQuadrant = REMEDIATION_AFTER_RECALL after a passed recall and before practice is delivered
            // previousQuadrant = PRACTICE after a failed practice and before the after practice, generated structured review
            if(previousQuadrant == MerrillQuadrantEnum.PRACTICE){
                //filter on practice concepts after a failed practice (versus the set of knowledge course concepts defined at the root level of an adaptive courseflow)
                //Practice concept maybe defined differently (e.g. different alphabetic case) in the practice DKF but that is what the provided map is for
                
                if(!hasBranchPointConcept(phaseConcept, practiceConceptToCourseConcept, getPracticeQuadrant().getPracticeConcepts().getCourseConcept())){
                    continue;
                }
                phase = PhaseEnum.PR;
                
            }else if(previousQuadrant != MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL && !hasBranchPointConcept(phaseConcept, null, getConceptList())){
                // filter on course concepts but not when learner has just passed recall and is entering practice.
                // If entering practice after recall, the contentCourseConcepts will contain the practice concepts.  Therefore
                // we want every loop to happen in order to populate the criteria with those practice concepts.
                continue;
            }
            
            List<AbstractRemediationConcept> prioritizedRemediation = remediationMap.get(phaseConcept);  
            if(prioritizedRemediation != null){
                //not every course concept needs remediation in a multi-concept adaptive courseflow course object
                
                for(AbstractRemediationConcept remediationConcept : prioritizedRemediation){
                    
                    if((criteria.getQuadrant() != MerrillQuadrantEnum.PRACTICE && !hasBranchPointConcept(remediationConcept.getConcept(), null, getConceptList())) ||
                            (criteria.getQuadrant() == MerrillQuadrantEnum.PRACTICE && !hasBranchPointConcept(remediationConcept.getConcept(), practiceConceptToCourseConcept, contentCourseConcepts))){
                        //pedagogy request contains a course concept that isn't handled by this adaptive courseflow course object instance
                        // (Note: course concepts for practice can be a superset of the course concepts taught in this adaptive course flow)
                        //this can happen if the learner state contains course concepts that are NOT above expectation, say from a pre-test,
                        //and the learner fails to pass a recall phase for an adaptive courseflow for another course concept.  The reason is
                        //that the pedagogy doesn't currently have an idea about which course concepts are being taught.  It merely delivers
                        //a request to remediate on all non-expert course concepts being covered in the course.  Its up to the domain to apply
                        //domain context to the pedagogical request (i.e. does it make sense in the current state of the course to remediate on 
                        //which course concepts the pedagogy is tracking).
                        continue;
                    }
                    
                    generated.metadata.Concept concept = new generated.metadata.Concept();
                    concept.setName(phaseConcept);
                    
                    if(remediationConcept instanceof PassiveRemediationConcept){
                        //passive remediation has specific metadata attributes to look for
                        
                        List<MetadataAttributeItem> metadataAttributeItems = ((PassiveRemediationConcept)remediationConcept).getAttributes();
                        populateMetadataAttributes(concept, metadataAttributeItems);
                        
                        criteria.addConcept(concept);
                        
                    }else if(remediationConcept instanceof ActiveRemediationConcept){
                        
                        generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
                        generated.metadata.ActivityType.Active active = new generated.metadata.ActivityType.Active();
                        activityType.setType(active);
                        concept.setActivityType(activityType);
                        
                        criteria.addConcept(concept);
                        
                    }else if(remediationConcept instanceof ConstructiveRemediationConcept){
                        
                        generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
                        generated.metadata.ActivityType.Constructive constructive = new generated.metadata.ActivityType.Constructive();
                        activityType.setType(constructive);
                        concept.setActivityType(activityType);
                        
                        criteria.addConcept(concept);
                        
                    }else if(remediationConcept instanceof InteractiveRemediationConcept){
                        
                        generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
                        generated.metadata.ActivityType.Interactive interactive = new generated.metadata.ActivityType.Interactive();
                        activityType.setType(interactive);
                        concept.setActivityType(activityType);
                        
                        criteria.addConcept(concept);
                    }
                }//end for
            }

        }
        
        //
        // Add related course concepts specific to this MBP are not included in the required concepts
        // because content could be tagged with multiple concepts
        //        
        boolean found;
        for(String conceptName : getConceptList()){
            
            found = false;
            
            for(generated.metadata.Concept concept : criteria.getConcepts()){
                
                if(conceptName.equalsIgnoreCase(concept.getName())){
                    found = true;
                    break;
                }
            }
            
            if(!found){
                generated.metadata.Concept concept = new generated.metadata.Concept();
                concept.setName(conceptName);
                criteria.addRelatedConcept(concept);
            }
        }
        
        criteria.addDeliveredContent(contentDelivered);
        
        //required in order to include content that doesn't cover all concepts in a multi-concept branch
        //to allow for sequencing of content that will cover all of the concepts
        //ticket: #2791
        // Update: currently don't support sequencing for practice phase
        if(criteria.getQuadrant() != MerrillQuadrantEnum.PRACTICE){
            criteria.setAnySubsetOfRequired(true);
        }
        
        //
        // --> find metadata files for the current domain that best match the criteria
        //
        Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<>();
        quadrantSearchCriteria.put(criteria.getQuadrant(), criteria);
        Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToMetadata =  MetadataFileFinder.findFiles(runtimeCourseDirectory, quadrantSearchCriteria);
        Map<FileProxy, generated.metadata.Metadata> metadataFiles = quadrantToMetadata.get(criteria.getQuadrant()) != null ?
                quadrantToMetadata.get(criteria.getQuadrant()).getMetadataFilesMap() : new HashMap<>(0);
        
        if(metadataFiles.isEmpty()){
            
            if(expandedCourseObject instanceof RuleCourseObject){
                //relaxing the restriction on having to provide both rule and example content, now 
                //only need to provide example at a minimum.
                return;
            }else{
                throw new Exception("Unable to find any metadata files matching (at a minimum) this search criteria: "+criteria);
            }
        }
        
        //
        // Gather resource files (i.e. the thing that will be presented like PowerPoint show) for the metadata files that were found - 
        //    this will be used to help filter out content that has been presented before
        //
        
        for(FileProxy metadataFile : metadataFiles.keySet()){
                
            generated.metadata.Metadata metadata = metadataFiles.get(metadataFile);
            //
            // Determine type of content reference
            //
            String referenceFileName = null;
            if(metadata.getContent() instanceof generated.metadata.Metadata.Simple){
                referenceFileName = ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue();                               
            }else if(metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){
                referenceFileName = ((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue();
            }else if(metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial){
                referenceFileName = ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue();  
            }
                        
            Object resourceObj;
            if(referenceFileName != null){
                FileProxy referenceFileProxy = runtimeCourseDirectory.getRelativeFile(referenceFileName);
                resourceObj = referenceFileProxy;
                
                //
                // Keep track of content delivered - add the content reference that is going to be delivered
                //
                contentDelivered.add(referenceFileName);
                
            }else if(metadata.getContent() instanceof generated.metadata.Metadata.URL){
                String url = ((generated.metadata.Metadata.URL)metadata.getContent()).getValue();
                resourceObj = new URL(url);
                
                //
                // Keep track of content delivered - add the content reference that is going to be delivered
                //
                contentDelivered.add(url);
            }else{
                //ERROR
                throw new Exception("Didn't find a supported metadata content reference in the metadata file of "+metadataFile+".");
            }
        
            //for each resource file selected to be delivered, create a course object
            //and add it to the list of course objects to insert in the course flow
            AbstractExpandedCourseObject expandedCourseObjectCopy = expandedCourseObject.deepCopy();
            
            //
            // Create new course element that will handle presenting the content
            //
            
            String reference;
            if(resourceObj instanceof FileProxy){
                reference = runtimeCourseDirectory.getRelativeFileName((FileProxy) resourceObj);
            }else if(resourceObj instanceof URL){
                reference = ((URL)resourceObj).toString();
            }else{
                throw new Exception("Didn't find a supported metadata content reference in "+resourceObj+".");
            }
            
            // the course object that was created to present the content
            GeneratedCourseObjectWrapper generatedCourseObjectWrapper;
            try{
                generatedCourseObjectWrapper = buildContentCourseElement(reference);
            }catch(Exception e){
                throw new DetailedException("Failed to create a new course object for an Adaptive courseflow course object content.",
                        "An exception was thrown while trying to handle the metadata reference '"+reference+"' that reads:\n"+e.getMessage(),
                        e);                
            }
            
            expandedCourseObjectCopy.setCourseObject(generatedCourseObjectWrapper.getGeneratedCourseObject());  
            
            // attempt to get the authored course folder relative path to the metadata file as a backup
            // to a generated dkf path
            String metadataFilePathToUseForRef = null;
            if(generatedCourseObjectWrapper.isCreatedDKF()){
                
                try{
                    FileProxy metadataFileProxyToAuthoredCourseFolder = authoredCourseDirectory.getRelativeFile(metadataFile.getName());
                    metadataFilePathToUseForRef = FileServices.getInstance().getFileServices().trimWorkspacesPathFromFullFilePath(metadataFileProxyToAuthoredCourseFolder.getFileId());
                }catch(@SuppressWarnings("unused") Exception e){
                    // ignore , best effort
                }
            }
            
            // 
            // build wrapper around course object to store additional metadata needed later on
            //
            CourseObjectWrapper wrapper = new CourseObjectWrapper(expandedCourseObjectCopy, 
                    CourseObjectWrapper.generateCourseObjectReference(generatedCourseObjectWrapper.getGeneratedCourseObject(), 
                            authoredCourseDirectory,
                            metadataFilePathToUseForRef));
            wrapper.setRemediationObject(remediationContent);
            wrapper.setPhaseEnum(phase);
            transitions.add(wrapper);

        } // end for       

    }
    
    /**
     * Notification that a new course object is about to be presented to the learner.
     * This is used to keep track of the duration the content was presented to the learner.
     * 
     * @param previousCourseObject the previous course object that was presented to the learner
     * @param nextCourseObject the next course object that will be presented to the learner.
     */
    public void notifyNextCourseObject(CourseObjectWrapper previousCourseObject, CourseObjectWrapper nextCourseObject){
        // nothing to do right now
    }    
    
    /**
     * Notification that an assessment event happened.
     * This is used to keep track of the assessment results after presenting content.
     */
    public void notifyAssessmentEvent(boolean passed){
        // nothing to do right now
    }
    
    /**
     * Returns true if there is a practice attempt limit that has been configured for the course.
     * 
     * @return True if there is a recall attempt limit that has been configured for the course.
     */
    protected boolean hasPracticeAttemptLimit() {
        return allowedPracticeAttempts > 0;
    }
    
    /**
     * Return whether or not the remediation information mentions at least one of the concepts
     * covered by this branch point.
     * 
     * @param concept the remediation information from a pedagogical strategy to search
     * @param practiceToCourseConcept mapping of DKF concepts used in practice to course level concepts.  Can be null or empty. 
     * @param conceptList the course concepts to check (could be knowledge or skill concepts in an adaptive courseflow).  Can't be null.
     * If empty this method just returns false.
     * @return boolean true iff the remediation information references at least one of the concepts in
     * this branch point course element.
     */
    public boolean hasBranchPointConcept(String concept, Map<String, String> practiceToCourseConcept, Collection<String> conceptList){
        
        for(String name : conceptList){
            
            if(concept.equalsIgnoreCase(name)){
                return true;
            }else if(practiceToCourseConcept != null && name.equalsIgnoreCase(practiceToCourseConcept.get(concept))){
                //check if the remediation concept is a practice concept that matches when translated into a course concept
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Notification that a training application course object or practice phase is being started.  This notification
     * is needed to track the number of attempts and determine if the too many attempts have happened which
     * will result in a @link {@link CourseComprehensionException} to be thrown.
     */
    public void incrementAndCheckPracticeAttempt() throws CourseComprehensionException{
        
        successivePracticeCount++;
        
        if(hasPracticeAttemptLimit() && successivePracticeCount > allowedPracticeAttempts){
            //the learner is not increasing their comprehension of this branch points concepts at a pace the course author
            //wants, therefore bounce the learner out of the course (BAIL OUT!!!)
            
            if(logger.isInfoEnabled()){
                logger.info("The learner has failed to proceed past the practice activity after "+successivePracticeCount+
                        " attempts.  Therefore GIFT is gracefully stopping the exeuction of this course.");
            }
            throw new CourseComprehensionException("The learner has failed to proceed past the practice activity after "+successivePracticeCount+
                    " attempts.");
        }
    }
    
    /**
     * Return a new course transition that will present the content represented by the content reference provided.
     * 
     * @param contentRef - the content reference (i.e. file name, URL) containing content to display for this transition
     * @return the new transition (e.g. generated.course.TrainingApplication, generated.course.Guidance, generated.course.LessonMaterial)
     */
    private GeneratedCourseObjectWrapper buildContentCourseElement(String contentRef) throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Building training application transition from "+contentRef+".");
        }
        
        //
        // Check type of content file that is supported
        //
        Serializable courseElement = null;
        MetadataContentType type = getMetadataContentType(contentRef);
        if(type == null){
            throw new Exception("Unable to build a course element for the unsupported metadata content reference of "+contentRef+".");
        }
        
        generated.course.TrainingApplication tApp;
        boolean createdDKF = false;
        
        switch (type){
            
            case Powerpoint:
                
                tApp = new generated.course.TrainingApplication();
                tApp.setTransitionName("Generated PowerPoint course element");
                
                //create the default dkf in the runtime course folder
                String newDKFRelativeFilename = "temp" + File.separator + "simplest."+System.currentTimeMillis()+".dkf.xml";
                File newDefaultDKFFile = new File(runtimeCourseDirectory.getFileId() + File.separator + newDKFRelativeFilename);
                FileUtils.copyFile(DEFAULT_DKF_FILE, newDefaultDKFFile);
                runtimeCourseDirectory.createFile(newDefaultDKFFile, null);
                createdDKF = true;
                
                generated.course.DkfRef dkfRef = new generated.course.DkfRef();
                dkfRef.setFile(newDKFRelativeFilename);
                tApp.setDkfRef(dkfRef);
                
                tApp.setFinishedWhen(TrainingApplicationStateEnum.STOPPED.getName());
                
                generated.course.Interops interops = new generated.course.Interops();
                
                //only support ppt files right now...
                generated.course.Interop interop = buildPPTInterop(contentRef);
                interops.getInterop().add(interop);
                
                tApp.setInterops(interops);
                
                courseElement = tApp;
                
                break;
                
            case TrainingAppXML:
                
                FileProxy taFile = runtimeCourseDirectory.getRelativeFile(contentRef);
                UnmarshalledFile uTAFile = AbstractSchemaHandler.parseAndValidate(generated.course.TrainingApplicationWrapper.class, 
                        taFile.getInputStream(), 
                        AbstractSchemaHandler.COURSE_SCHEMA_FILE,
                        true);
                generated.course.TrainingApplicationWrapper taWrapper = 
                        (generated.course.TrainingApplicationWrapper)uTAFile.getUnmarshalled();
                
                courseElement = taWrapper.getTrainingApplication();
                
                break;
                
            case LessonMaterialXML:
                
                FileProxy lmFile = runtimeCourseDirectory.getRelativeFile(contentRef);
                UnmarshalledFile uLMFile = AbstractSchemaHandler.parseAndValidate(generated.course.LessonMaterialList.class, 
                                lmFile.getInputStream(), 
                                AbstractSchemaHandler.COURSE_SCHEMA_FILE, 
                                true);
                generated.course.LessonMaterialList lmList =
                        (generated.course.LessonMaterialList)uLMFile.getUnmarshalled();
                    
                //create course object
                generated.course.LessonMaterial lmCourseObject = new generated.course.LessonMaterial();
                lmCourseObject.setTransitionName(lmFile.getName());
                lmCourseObject.setLessonMaterialList(lmList);
                
                courseElement = lmCourseObject;
                
                break;
                
            case HTML:
                
                //for now, create a Guidance element to show
                generated.course.Guidance htmlGuidance = new generated.course.Guidance();
                htmlGuidance.setTransitionName("Generated HTML Guidance course element");
                htmlGuidance.setFullScreen(BooleanEnum.TRUE);
                
                generated.course.Guidance.File htmlGuidanceFile = new generated.course.Guidance.File();
                htmlGuidanceFile.setHTML(contentRef);
                
                htmlGuidance.setGuidanceChoice(htmlGuidanceFile);
                
                courseElement = htmlGuidance;
                
                break;
                
            case URL:
                
                //for now, create a Guidance element to show
                generated.course.Guidance urlGuidance = new generated.course.Guidance();
                urlGuidance.setTransitionName("Generated URL course element");
                urlGuidance.setFullScreen(BooleanEnum.TRUE);
                
                generated.course.Guidance.URL url = new generated.course.Guidance.URL();
                url.setAddress(contentRef);
                
                urlGuidance.setGuidanceChoice(url);
                
                courseElement = urlGuidance;
                
                break;
                
            case QuestionExport:
                
                //create a present survey course object
                generated.course.PresentSurvey presentSurvey = new generated.course.PresentSurvey();
                presentSurvey.setTransitionName("Generated Survey course element");
                presentSurvey.setShowInAAR(BooleanEnum.FALSE);
                presentSurvey.setFullScreen(BooleanEnum.TRUE);
                
                generated.course.SurveyExport surveyExport = new generated.course.SurveyExport();
                generated.course.SurveyExport.Question questionExport = new generated.course.SurveyExport.Question();
                questionExport.setFile(FileFinderUtil.getRelativePath(DOMAIN_DIRECTORY, runtimeCourseDirectory.getFolder()) + File.separator + contentRef);
                surveyExport.setType(questionExport);
                presentSurvey.setSurveyChoice(surveyExport);
                
                courseElement = presentSurvey;
                
                break;
                
            case ConversationTreeXML:
                
                // create a present survey course object with a conversation tree
                generated.course.PresentSurvey presentConvTree = new generated.course.PresentSurvey();
                presentConvTree.setTransitionName("Generated Conversation Tree course object");
                presentConvTree.setShowInAAR(BooleanEnum.FALSE);
                presentConvTree.setFullScreen(BooleanEnum.TRUE);
                
                generated.course.Conversation conversation = new generated.course.Conversation();
                generated.course.ConversationTreeFile treeFile = new generated.course.ConversationTreeFile();
                treeFile.setName(contentRef);
                conversation.setType(treeFile);
                presentConvTree.setSurveyChoice(conversation);
                
                courseElement = presentConvTree;
                
                break;
                
             default:
                 //ERROR - unsupported content file type
                 throw new ConfigurationException("When trying to build a training application course element the currently selected metadata's referenced content of "+contentRef+
                         " was found to be NOT one of the supported file types.  Therefore GIFT will not attempt to load this referenced piece of content.");
        }        

        
        //validate course element created instance against schema
        BasicCourseHandler handler = new BasicCourseHandler();
        if(courseElement instanceof generated.course.TrainingApplication){
            handler.validateTrainingApplication((generated.course.TrainingApplication)courseElement);
        }else if(courseElement instanceof generated.course.Guidance){
            handler.validateGuidance((generated.course.Guidance)courseElement);
        }else if(courseElement instanceof generated.course.LessonMaterial){
            handler.validateLessonMaterial((generated.course.LessonMaterial)courseElement);
        }
        
        GeneratedCourseObjectWrapper generatedCourseObjectWrapper = new GeneratedCourseObjectWrapper(courseElement);
        generatedCourseObjectWrapper.setCreatedDKF(createdDKF);
        
        return generatedCourseObjectWrapper;
    }
    
    /**
     * Determine the type of branch point content based on the content reference.
     *  ppsx, pps, ppsm = PowerPoint show
     *  trainingapp.xml = training application reference XML
     *  htm, html = local HTML file
     *  
     *  
     * @param contentReference the content reference to check
     * @return {@link MetadataContentType} enumerated type.  Null if a type is not found.
     */
    public static MetadataContentType getMetadataContentType(String contentReference){
        
        MetadataContentType type = null;
        if(contentReference != null){
            
            if(DomainCourseFileHandler.isSupportedPowerPointShow(contentReference)){
                //it's a powerpoint show
                type = MetadataContentType.Powerpoint;
                
            }else if(contentReference.endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)){
                //it's a training app element XML
                type = MetadataContentType.TrainingAppXML;
                
            }else if(contentReference.endsWith(AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION)){
                //it's a lesson material element XML
                type = MetadataContentType.LessonMaterialXML;
                
            }else if(DomainCourseFileHandler.isSupportedURL(contentReference)){
                //it's a URL
                type = MetadataContentType.URL;
            }else if(DomainCourseFileHandler.isSupportedHTML(contentReference)){
                /* it's a HTML file; make sure URL is checked before HTML
                 * because the URL can point to an HTML file and we don't want
                 * to get a false positive */
                type = MetadataContentType.HTML;
            }else if(contentReference.endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)){
                //it's a survey export
                type = MetadataContentType.QuestionExport;
            }else if(contentReference.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
                // it's a conversation tree
                type = MetadataContentType.ConversationTreeXML;
            }
        }
        
        return type;
    }
    
    /**
     * Return the interop training application course transition element used to describe the application
     * that will be used to display the specified content.
     * 
     * @param contentFileName - the file name containing content to display for this transition
     * @return generated.course.Interop
     */
    private generated.course.Interop buildPPTInterop(String contentFileName){
        
        generated.course.Interop interop = new generated.course.Interop();
        
        interop.setInteropImpl(DomainCourseFileHandler.GW_PPT_IMPL);
        
        generated.course.InteropInputs interopInputs = new generated.course.InteropInputs();
        generated.course.PowerPointInteropInputs ppInteropInputs = new generated.course.PowerPointInteropInputs();
        generated.course.PowerPointInteropInputs.LoadArgs loadArgs = new generated.course.PowerPointInteropInputs.LoadArgs();
        String pptDomainRelativeFileName = contentFileName;
        loadArgs.setShowFile(pptDomainRelativeFileName);
        ppInteropInputs.setLoadArgs(loadArgs);
        interopInputs.setInteropInput(ppInteropInputs);
        interop.setInteropInputs(interopInputs);
        
        return interop;
    }
    
    /**
     * Populate the concept's metadata element with the attributes provided.
     * 
     * @param concept the concept object to populate with metadata attributes
     * @param metadataAttributeItems the metadata attributes to place in the concept object appropriately
     */
    protected void populateMetadataAttributes(generated.metadata.Concept concept, List<MetadataAttributeItem> metadataAttributeItems){
        
        List<generated.metadata.Attribute> attributes = new ArrayList<>();
        for(MetadataAttributeItem item : metadataAttributeItems){
            
            //add only those metadata attributes that don't have a label (i.e. they aren't associated with a concept)
            // - OR - the label matches this concept name (i.e. this metadata item is specifically for this concept)
            //This will enforce concept filtering in cases where the pedagogical request contains concepts that
            //aren't being taught by this branch point.
            if(item.getLabel() == null || item.getLabel().equalsIgnoreCase(concept.getName())){
                generated.metadata.Attribute attribute = new generated.metadata.Attribute();
                
                attribute.setValue(item.getAttribute().getValue());
                attributes.add(attribute);
            }
        }
        
        //provide an empty set of attributes for this concept
        if(concept.getActivityType() == null){
            generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
            generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
            passive.setAttributes(new generated.metadata.Attributes());
            activityType.setType(passive);
            concept.setActivityType(activityType);
        }
        
        if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
            
            generated.metadata.ActivityType.Passive passive = (generated.metadata.ActivityType.Passive)concept.getActivityType().getType();
            passive.getAttributes().getAttribute().addAll(attributes);
        }
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DynamicContentHandler: ");
        
        sb.append("concepts = {");
        for(String concept : getConceptList()){
            sb.append(" ").append(concept).append(",");
        }
        sb.append("}");
        sb.append(", previousQuadrant = ").append(previousQuadrant);
        sb.append(", successivePracticeCount = ").append(successivePracticeCount);
        sb.append("]");
        
        return sb.toString();
    }
    
    
    /**
     * This exception is used to signal that the learner failed to comprehend this branch point.  
     * This could be caused by failing the Recall test or the Practice scenario enough times.
     * 
     * @author mhoffman
     *
     */
    public class CourseComprehensionException extends RuntimeException{
        
        private static final long serialVersionUID = 1L;
        
        public CourseComprehensionException(String message){
            super(message);
        }
    }
    
    /**
     * A wrapper around a generated course object created from a metadata file.
     * 
     * @author mhoffman
     *
     */
    public class GeneratedCourseObjectWrapper{
        
        /** the course object that was created */
        private Serializable generatedCourseObject;
        
        /** whether a DKF was created in the runtime course folder for the generated course object (e.g. simplest.dkf.xml) */
        private boolean createdDKF = false;
        
        /**
         * Set attribute 
         * @param generatedCourseObject the course object that was created, can't be null
         */
        public GeneratedCourseObjectWrapper(Serializable generatedCourseObject){
            setGeneratedCourseObject(generatedCourseObject);
        }

        /**
         * Return the course object that was created
         * @return won't be null
         */
        public Serializable getGeneratedCourseObject() {
            return generatedCourseObject;
        }

        /**
         * Set the course object that was created
         * @param generatedCourseObject can't be null
         */
        private void setGeneratedCourseObject(Serializable generatedCourseObject) {
            
            if(generatedCourseObject == null){
                throw new IllegalArgumentException("The generated course object can't be null");
            }
            this.generatedCourseObject = generatedCourseObject;
        }

        /**
         * Return whether a DKF was created in the runtime course folder for the generated course object (e.g. simplest.dkf.xml)
         * @return default is false
         */
        public boolean isCreatedDKF() {
            return createdDKF;
        }

        /**
         * Set whether a DKF was created in the runtime course folder for the generated course object (e.g. simplest.dkf.xml)
         * @param createdDKF true if a dkf file was created
         */
        public void setCreatedDKF(boolean createdDKF) {
            this.createdDKF = createdDKF;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[GeneratedCourseObjectWrapper: generatedCourseObject = ");
            builder.append(generatedCourseObject);
            builder.append(", createdDKF = ");
            builder.append(createdDKF);
            builder.append("]");
            return builder.toString();
        }
        
        
    }

}
