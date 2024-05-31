/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import generated.metadata.ActivityType;
import generated.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.util.CaseInsensitiveList;

/**
 * This class has the logic to find metadata files that match a collection of given attributes.
 * 
 * @author mhoffman
 *
 */
public class MetadataFileFinder {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetadataFileFinder.class);
    
    /** used to randomly choose an integer index in a collection of the next content to show the learner */
    private static final Random RANDOM_REMEDIATION = new Random();
    
    /**
     * Returns the metadata files found that reference the content file provided.  The search is recursive
     * from the starting directory.
     * 
     * @param startingDirectory the directory to start the recursive search
     * @param content the content file whose file name will be looked for in the metadata files found
     * @param quadrant used to filter out metadata files that reference the content.  Optional, can be null.
     * @return the collection of metadata files that reference the content file and are a descendant of the starting directory.
     * Can be empty but not null.
     * @throws DetailedException if there was a problem searching for metadata files
     */
    public static List<FileProxy> findFilesForContent(AbstractFolderProxy startingDirectory, FileProxy content, MerrillQuadrantEnum quadrant) throws DetailedException{
        
        if(startingDirectory == null){
            throw new IllegalArgumentException("The starting directory can't be null.");
        }else if(!startingDirectory.isDirectory()){
            throw new IllegalArgumentException("The starting directory '"+startingDirectory.getFileId()+"' can't be a file, it must be a directory.");
        }else if(content == null){
            throw new IllegalArgumentException("The content can't be null.");
        }
        
        List<FileProxy> potentialFiles = new ArrayList<>();
        try {
            FileFinderUtil.getFilesByExtension(startingDirectory, potentialFiles, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
        } catch (IOException e) {
            throw new DetailedException("Failed to retrieve the metadata files under '"+startingDirectory.getName()+"'.",
                    "There was a problem when trying to find the metadata files under '"+startingDirectory.getFileId()+"'.\n\n"+e.getMessage(),
                    e);
        }        
        
        //the content path for server files uses forward slashes while metadata references use backward slashes
        //therefore need to make the content path for server files have backward slashes
        String contentId = content.getFileId().replace(Constants.FORWARD_SLASH, File.separator);
        
        //need an iterator to safely remove files that do not meet the concept criteria from the potential file list
        Iterator<FileProxy> potentialFilesIterator = potentialFiles.iterator();
                
        //find the metadata file(s) that reference the content
        Metadata metadata;
        while(potentialFilesIterator.hasNext()){
            
            metadata = null;
            FileProxy metadataFile  = potentialFilesIterator.next();
        
            try{
                //parse and validate the xml file
                metadata = MetadataSchemaHandler.getMetadata(metadataFile, true);
                String reference = MetadataSchemaHandler.getReference(metadata);
                
                if(!contentId.endsWith(reference)){
                    //this metadata file doesn't reference the content
                    potentialFilesIterator.remove();
                }else if(quadrant != null){
                    //need to check the metadata file's quadrant
                    
                    String metadataQuadrant = metadata.getPresentAt().getMerrillQuadrant();
                    if(metadataQuadrant == null || !quadrant.getName().equals(metadataQuadrant)){
                        //this metadata file references a different quadrant
                        potentialFilesIterator.remove();
                    }
                }
                
            }catch(Throwable e){
                logger.error("Ignoring metadata files of "+metadataFile.getFileId()+" because an exception happened while parsing.", e);
                potentialFilesIterator.remove();
                continue;
            }
        }//end while

        return potentialFiles;
    }
    
    /**
     * Find all the metadata files in the directory provided.  This doesn't validate the files only
     * that they exist.
     * 
     * @param directory the directory to search for metadata files.
     * @return List<FileProxy> - metadata files. Won't be null but can be empty.
     * @throws IOException if there was a problem retrieving files from the directory
     */
    public static List<FileProxy> findFiles(AbstractFolderProxy directory) throws IOException{
        
        //gather metadata files under the directory
        List<FileProxy> potentialFiles = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(directory, potentialFiles, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
        return potentialFiles;
    }
    
    /**
     * Find metadata files starting in the directory that match the given search criteria.
     * 
     * Note: the current implementation will return all metadata files that match the greatest number of 
     * the search criteria's concept paired attribute parameters.
     * 
     * @param courseFolder - the course directory to search for metadata files related to the course (e.g. a subdirectory of Domain)
     * @param quadrantSearchCriteria - mapping of adaptive courseflow phase (e.g. Rule) to the metadata search criteria to 
     * match against in metadata files in the course folder.  Can't be null.  If empty this method returns no metadata results.
     * @return mapping of adaptive courseflow phase (e.g. Rule) to metadata search results which include the metadata files
     * that match the search criteria for that phase.  Won't be null but can be empty.
     * @throws IOException if there was a problem retrieving files from the directory
     */
    public static Map<MerrillQuadrantEnum, MetadataFileSearchResult> findFiles(AbstractFolderProxy courseFolder, 
            Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria) throws IOException{
        
        //gather metadata files under the directory
        List<FileProxy> potentialFiles = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(courseFolder, potentialFiles, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
        
        if(logger.isInfoEnabled()){
            logger.info("There are "+potentialFiles.size()+" metadata file(s) to check for the course in the directory of "+courseFolder+".");
        }
        
        //
        // save the metadata files that have the best match to the search criteria
        // Note: concept matching must be 100%, while attribute matching is best match.
        //
        Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToQualifiedMetadatas = new HashMap<>();
        int numAttributesMatched, highestConceptsMatched = 0, numConceptsMatched;
        
        //need an iterator to safely remove files that do not meet the concept criteria from the potential file list
        Iterator<FileProxy> potentialFilesIterator = potentialFiles.iterator();
        
        InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();

        Map<MerrillQuadrantEnum, SearchResults> quadrantToSearchResults = new HashMap<MerrillQuadrantEnum, SearchResults>(3);
        Map<FileProxy, generated.metadata.Metadata> parsedMetadatas = parseAndRemove(potentialFilesIterator);
        List<FileProxy> passedGeneralValidation = new ArrayList<>();
        
        List<String> reqConceptsMatchedList;
        
        for(MerrillQuadrantEnum quadrant : quadrantSearchCriteria.keySet()){
            
            MetadataSearchCriteria criteria = quadrantSearchCriteria.get(quadrant);
            SearchResults quadrantSearchResults = new SearchResults();
            
            //
            // Gather concepts from criteria
            // Note: searching for concepts is optional, e.g. DomainCourseFileHandler.checkCourse doesn't specify
            //       any concepts so that it can gather all metadata at once and filter later.
            //
            List<String> criteriaReqConceptNames = null, criteriaOptConceptNames = null;
            if(criteria.getConcepts() != null 
                    && !criteria.getConcepts().isEmpty()){
                                    
                criteriaReqConceptNames = getConcepts(criteria.getConcepts());
            }
            
            if(criteria.getRelatedConcepts() != null
                    && !criteria.getRelatedConcepts().isEmpty()){
                
                criteriaOptConceptNames = getConcepts(criteria.getRelatedConcepts());
            }
            
            //
            // Check Metadata against criteria
            //
            for(FileProxy metadataFile : parsedMetadatas.keySet()){
                
                reqConceptsMatchedList = new ArrayList<>();
                numAttributesMatched = 0;
                numConceptsMatched = 0;
                
                generated.metadata.Metadata metadata = parsedMetadatas.get(metadataFile);
                
                //check if a request was made to ignore this file
                if(criteria.getFilesToIgnore() != null && criteria.getFilesToIgnore().contains(metadataFile)){
                    continue;
                }
                
                //Now check the parsed metadata against the criteria
                try{
                    //First: 
                    // if searching for remediation only
                    //   i. remove Practice metadata
                    //   ii. searching for specific quadrant, remove metadata tagged with other quadrant
                    // otherwise
                    //   i. metadata quadrant is specified, remove metadata if searching for a different quadrant
                    generated.metadata.PresentAt metadataPresentAt = metadata.getPresentAt();
                    String metadataPhaseStr = metadataPresentAt.getMerrillQuadrant();
                    MerrillQuadrantEnum metadataQuadrant = metadataPhaseStr != null ? MerrillQuadrantEnum.valueOf(metadataPhaseStr) : null;
                    if(criteria.isRemediationOnly()){
                        
                        if(metadataQuadrant == MerrillQuadrantEnum.PRACTICE){
                            //no practice during remediation
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("Removing metadata file from selection because it is tagged with Practice.  Needed remediation only content which can't be practice.\n"+metadataFile);
                            }
                            
                            continue;
                            
                        }else if(criteria.getQuadrant() != null && criteria.getQuadrant() != MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL && criteria.getQuadrant() != MerrillQuadrantEnum.REMEDIATION_AFTER_PRACTICE){
                            //the request is for remediation only content {Rule, Example, Null+Remediation Only}
                            //Don't want to enter here if the search is looking for remediation phase {null, REMEDIATION_AFTER_RECALL, REMEDIATION_AFTER_PRACTICE}
                            //because everything but Practice should be allowed in this search case.
                            //
                            //When another quadrant is specified (i.e. not remediation), remove metadata if metadata phase doesn't match the search quadrant
                            
                            if(metadataQuadrant == null){
                                
                                if(logger.isDebugEnabled()){
                                    logger.debug("Removing metadata file from selection because it is not tagged with a phase.  Needed content tagged with "+criteria.getQuadrant()+".\n"+metadataFile);
                                }
                                
                                continue;
                                
                            }else if(metadataQuadrant != criteria.getQuadrant()){
                                
                                if(logger.isDebugEnabled()){
                                    logger.debug("Removing metadata file from selection because it is tagged with the wrong phase of "+metadataQuadrant+".  Needed content tagged with "+criteria.getQuadrant()+".\n"+metadataFile);
                                }
                                
                                continue;
                            }
                            
                        }else if(criteria.isExcludeRuleExampleContent() && metadataQuadrant != null){
                            //author overriding default behavior - exclude content that is tagged with quadrant (i.e. rule/example content)
                            continue;
                        }

                    }else{
                        
                        if(metadataQuadrant != null){  
                            //this applies when the metadata file is tagged with Rule/Example/Practice
                            
                            if(criteria.getQuadrant() == null){
                                //looking for no quadrant and found a quadrant
                            
                                if(logger.isDebugEnabled()){
                                    logger.debug("Removing metadata file from selection because it is tagged with "+metadataQuadrant+".  Needed to not be tagged with a phase.\n"+metadataFile);
                                }
                                
                                continue;
                                
                            }else if(criteria.getQuadrant() != null && metadataQuadrant != criteria.getQuadrant()){
                                //looking for a specific quadrant and found a different quadrant
                                
                                if(logger.isDebugEnabled()){
                                    logger.debug("Removing file from selection because it is for the wrong quadrant: "+metadataFile+".  Needed to be "+criteria.getQuadrant()+".\n"+metadataFile);
                                }
                                
                                continue;
                            }
                        }else if(metadataQuadrant == null && 
                                criteria.getQuadrant() != null && criteria.getQuadrant() != MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL && criteria.getQuadrant() != MerrillQuadrantEnum.REMEDIATION_AFTER_PRACTICE){
                            //metadataQuadrant is null when remediation authored content
                            //when criteria is looking for remediation the value will be null, REMEDIATION_AFTER_RECALL or REMEDIATION_AFTER_PRACTICE
                            //When this entering here the criteria is looking for a non-remediation quadrant but the metadata has a remediation quadrant value, so skip it
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("Removing file from selection because it is for the wrong quadrant: "+metadataFile+".  Needed to be "+criteria.getQuadrant()+".\n"+metadataFile);
                            }
                            
                            continue;
                        }
                    }
                    
                    //Second:
                    // Check the metadata file to make sure it meets the specified concept criteria
                    if(criteriaReqConceptNames != null 
                            && !criteriaReqConceptNames.isEmpty()
                            && metadata.getConcepts() != null
                            && metadata.getConcepts().getConcept() != null){
                                            
                        List<generated.metadata.Concept> metadataConcepts = metadata.getConcepts().getConcept();
                        List<String> metadataConceptNames = getConcepts(metadataConcepts);
                        
                        //
                        // Check the following:
                        // 1. at least 1 of the criteria's required concepts are in this metadata
                        // 2. remaining metadata concepts are in the criteria's optional concepts
                        boolean conceptMatch = false, extraneousConcept = false;                
                            
                        //iterate over the concepts in the current metadata file
                        for(String metadataConcept : metadataConceptNames){
                            
                            conceptMatch = false;
                            
                            //iterate over the concepts that are required
                            for(String criteriaConcept : criteriaReqConceptNames){                                
                                
                                if(metadataConcept.equalsIgnoreCase(criteriaConcept)){
                                    //found the criteria's concept in this metadata file
                                    conceptMatch = true;
                                    reqConceptsMatchedList.add(criteriaConcept);
                                    break;
                                }
                            }
                            
                            if(!conceptMatch){
                                //found a concept in the metadata that isn't in the criteria's required concepts,
                                //check the criteria's optional concepts
                                
                                if(criteriaOptConceptNames == null || !criteriaOptConceptNames.contains(metadataConcept)){
                                    //this metadata has too many concepts
                                    extraneousConcept = true;
                                    break;
                                }else{
                                    //the current metadata concept is an optional criteria concept
                                    continue;
                                }
                                
                            }else{
                                numConceptsMatched++;
                            }
                        }//end for metadata concepts

                        if(extraneousConcept){
                            
                            /* if the metadata file has more concepts than specified by the criteria, remove 
                             * it from the list of potential files*/
                            if(logger.isDebugEnabled()){
                                logger.debug("Removing "+metadataFile+" from the list of potential metadata files because it contains a concept not mentioned in the search criteria of "+criteria+".");
                            }
                            
                            continue;
                            
                        }else if(numConceptsMatched != criteria.getConcepts().size() && !criteria.isAnySubsetOfRequired()){
                            
                            /* if the metadata file is missing a required concept specified by the criteria, remove 
                             * it from the list of potential files
                             * UNLESS - a subset of the required concepts is acceptable
                             */
                            if(logger.isDebugEnabled()){
                                logger.debug("Removing "+metadataFile+" from the list of potential metadata files because it doesn't reference all the required concepts specified in the search criteria of "+criteria+".");
                            }
                            
                            continue;
                        }
                    }
                    
                    //Third: 
                    // Search for each attribute of each concept in the search criteria
                    for(generated.metadata.Concept criteriaConcept : criteria.getConcepts()){
                        
                        if(metadata.getConcepts() != null){
                            //check the concept attributes of the file
                           
                            for(generated.metadata.Concept metadataConcept : metadata.getConcepts().getConcept()){
                                
                                if(criteriaConcept.getName().equalsIgnoreCase(metadataConcept.getName())){
                                    //found a matching concept
                                    
                                    if(criteriaConcept.getActivityType() != null && criteriaConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                                        
                                        if(metadataConcept.getActivityType() != null && metadataConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                                            //the activity type for this metadata concept is also passive
                                        
                                            generated.metadata.ActivityType.Passive criteriaConceptPassive = ((generated.metadata.ActivityType.Passive)criteriaConcept.getActivityType().getType());
                                            generated.metadata.Attributes criteriaConceptAttributes = criteriaConceptPassive.getAttributes();
        
                                            if(criteriaConceptAttributes != null){                                        
                                                
                                                generated.metadata.ActivityType.Passive metadataConceptPassive = ((generated.metadata.ActivityType.Passive)metadataConcept.getActivityType().getType());
                                                generated.metadata.Attributes metadataConceptAttributes = metadataConceptPassive.getAttributes();
                                                
                                                for(generated.metadata.Attribute criteriaAttribute : criteriaConceptAttributes.getAttribute()){
                                                    
                                                    for(generated.metadata.Attribute metadataAttribute : metadataConceptAttributes.getAttribute()){                                    
                                                        
                                                        if(metadataAttribute.getValue().equals(criteriaAttribute.getValue())){
                                                            //found a attribute value match, so far this file matches
                                                            
                                                            numAttributesMatched++;
                                                            break;
                                                        }
                                                    }
                                                    
                                                }
                                            }
                                        }
                                    }
                                }
                                

                            }
                        }
                        
                    }//end for
                    
                    
                    //
                    // Now that the metadata has satisfied the search criteria, perform GIFT validation (if requested).
                    // This is done here to avoid performing the more expensive GIFT validation of metadata files 
                    // that don't satisfy the search criteria.
                    //   1. when request to validate AND
                    //   1.1. custom additional validation specified OR 
                    //   1.2  has not passed general gift validation in a previous loop
                    // 
                    if(criteria.shouldValidate() && 
                            (criteria.getAdditionalValidation() != null ||  !passedGeneralValidation.contains(metadataFile))){
                        
                        try{
                            GIFTValidationResults validationResults = MetadataSchemaHandler.checkMetadata(metadata, courseFolder, criteria.getAdditionalValidation(), null, connectionStatus, true);
                            if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
                                throw validationResults.getFirstError();
                            }
                            
                            passedGeneralValidation.add(metadataFile);
                        }catch(Throwable e){
                            
                            if(criteria.getAdditionalValidation() == null){
                                logger.error("Ignoring metadata file '"+metadataFile.getFileId()+"' because an exception happened while checking it.  This file will be ignored for this search.", e);
                            }else{
                                logger.error("Ignoring metadata file '"+metadataFile.getFileId()+"' because an exception happened while checking it.  This file can be checked again because this check had specific validation that other checks in this search might not require.", e);
                            }
                            continue;
                        }
                    }
                    
                    
                    //Save this result for analysis later
                    SearchResult searchResult = new SearchResult(metadataFile, metadata, reqConceptsMatchedList);
                    searchResult.setMetadataAttributeMatches(numAttributesMatched);
                    searchResult.setIsDeliveredContent(criteria.getDeliveredContent().contains(MetadataSchemaHandler.getReference(metadata)));
                    searchResult.setCoversAllConcepts(criteriaReqConceptNames != null && numConceptsMatched == criteriaReqConceptNames.size());
                    quadrantSearchResults.addResult(searchResult);
                    
                    //keep track of the highest, individual metadata required concept coverage amount
                    //to determine if there exists a single file that can satisfy the current concept coverage
                    //request
                    if(numConceptsMatched > highestConceptsMatched){
                        highestConceptsMatched = numConceptsMatched;
                    }
                
                }catch(Throwable e){
                    logger.error("Ignoring metadata file '"+metadataFile.getFileId()+"' because an exception happened while checking it.  This file should be ignored in future searches.", e);
                    criteria.addFileToIgnore(metadataFile);
                    continue;
                }
                
                
            } //end for loop on parsed metadata(s)
            
            quadrantToSearchResults.put(quadrant, quadrantSearchResults);
                
        }//end for loop on quadrant search criteria
                
        //
        // Process search results
        //
        // At this point the remaining metadata are guaranteed to:
        // 1 - reference at least one of the required concepts in the search criteria
        // 2 - possibly reference one or more of the optional concepts in the search criteria
        // 3 - reference the same phase (e.g. Rule) as the search criteria (can be null)
        // 4 - match the same 'remediation only' flag value in the search criteria (can be null, i.e. false)
        // 5 - not contain any extraneous concepts, i.e. concepts not found in the required + optional course concepts in the search criteria
        //
        // Priorities:
        // 1 - apply iCAP activity prioritization (pedagogical request)
        // 2 - Don't Duplicate Content (if possible)
        // 3 - Maximize needed coverage of content (i.e. choose fewest content files for the concepts needed)     
        // 4 - Maximize appropriateness via metadata attributes and EMAP learner state tree
        // 5 - [Remediation Phase Only] 
        //   5.1 - not-delivered Remediation only content chosen at random (if available)
        //   5.2 - when no new content is available at this point:
        //   5.2.1 - if no remediation only tagged content was ever authored, goto 5 below {e.g. courses authored prior to remediation phase}
        //   5.2.2 - otherwise randomly choose from delivered content that is available to this point
        // 
        // 6 - Paradata resolution (if more than 1 content passes the above tests for the same set of concepts)
        //    - for each metadata, compare paradata on metadata that covers same concepts
        // 7 - random choice (if more than 1 content matches the above for the same set of concepts) 

        for(MerrillQuadrantEnum quadrant : quadrantToSearchResults.keySet()){
            
            //quadrant specific set of metadata that satisfies the required parts of the search criteria (see for loop comment above)
            SearchResults results = quadrantToSearchResults.get(quadrant);
            
            MetadataSearchCriteria criteria = quadrantSearchCriteria.get(quadrant);
            Map<FileProxy, generated.metadata.Metadata> filesThatQualify = new HashMap<>();
            
            // mapping of concept name that needs metadata search results to the prioritized activity list of search results.
            // The list value in the map for each concept name key contains a descending priority list of sets.  Each set
            // in that list contains zero or more metadata search results that have the concept and are
            // the same activity type (e.g. Active).  The purpose is to determine the ideal set of activity
            // for the concepts requested based on priority.
            Map<String, List<Set<SearchResult>>> conceptToPrioritizedSearchResults = new HashMap<>();

            if(!results.getResults().isEmpty()){
                
                if(highestConceptsMatched == 0 || (criteria.isAnySubsetOfRequired() && !criteria.shouldFilterMetadataByGIFTLogic())){
                    //don't apply filtering logic, return the files found - this is useful for displaying lists of files in authoring
                    // - if the highest number of matching concepts is none
                    // - if any subset of the required concepts is allowed in content AND not using GIFT filtering metadata logic
                    
                    populateAllFilesFromSearchResults(results.getResults(), filesThatQualify);
                    
                }else{
                                    
                    //
                    // Step 1 - Activity Prioritization (i.e. iCAP - Interactive/Constructive/Active/Passive)
                    //
                    handleActivityPrioritization(results, criteria, conceptToPrioritizedSearchResults);
                    
                    // whether there is more than one metadata that needs to be used to deliver
                    // the prioritized activities for the concepts requested, i.e. need to perform
                    // additional logic to try and limit the amount of activities requested of the learner
                    boolean moreThanOneMetadata = false;
                    
                    boolean conceptsHaveSearchResult = conceptToPrioritizedSearchResults.size() == criteria.getConceptSet().size();
                    if(conceptsHaveSearchResult){
                        // every concept have at least one search result, meaning there is a metadata out there that
                        // has the requested concept and highest priority activity for that concept.
                    
                        // how many unique files are there across all concepts
                        HashSet<SearchResult> searchResultCompleteSet = new HashSet<>();
                        for(List<Set<SearchResult>> conceptSearchResults : conceptToPrioritizedSearchResults.values()){
                            
                            for(Set<SearchResult> conceptSearchResultGroup : conceptSearchResults){
                                searchResultCompleteSet.addAll(conceptSearchResultGroup);
                            }
                        }
                        
                        moreThanOneMetadata = searchResultCompleteSet.size() != 1;
                        
                        if(!moreThanOneMetadata){
                            // add the one (or zero) search results that satisfy the request, no need to continue down
                            // selecting metadata search results
                            for(SearchResult searchResult : searchResultCompleteSet){
                                filesThatQualify.put(searchResult.getMetadataFile(), searchResult.getMetadata());
                            }
                        }
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Did every concept have at least one search result = "+conceptsHaveSearchResult+".  Is there more than 1 metadata file for all concept(s) = "+moreThanOneMetadata);
                        }
                        
                    }else{
                        // there is at least one concept that doesn't have a prioritized activity metadata file
                        // Rely on other means to select metadata files, i.e. not using prioritized activity
                        // request but just trying to select metadata files from the set that has the allowed concepts.
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("One or more concepts have no metadata based on activity prioritization.  Switching to not using activity prioritization.");
                        }
                        
                        moreThanOneMetadata = results.getResults().size() > 1;
                        
                        if(!moreThanOneMetadata){
                            // add the one (or zero) search results that satisfy the request, no need to continue down
                            // selecting metadata search results
                            for(SearchResult searchResult : results.getResults()){
                                filesThatQualify.put(searchResult.getMetadataFile(), searchResult.getMetadata());
                            }
                        }
                    }
                    
                    if(moreThanOneMetadata){
                        //need to down select OR rely on passive metadata attributes as a back up
                        //to activity prioritization
                        
                        //
                        // Step 2, 3, 4
                        //                        
                        List<SearchResults> candidateSearchResultsList = generateSearchResultCombinations(new ArrayList<List<Set<SearchResult>>>(conceptToPrioritizedSearchResults.values()));

                        // try each combination to find the first, best group of metadata to use based on request concept-activity priority
                        for(SearchResults candidateSearchResults : candidateSearchResultsList){

                            populateFiles(candidateSearchResults, true, criteria.isAnySubsetOfRequired(), filesThatQualify, criteria.getConcepts());
                            
                            if(!filesThatQualify.isEmpty()){
                                // found a set that satisfies the concepts, don't keep looking for a set that might have lower priority activities
                                results = candidateSearchResults;
                                break;
                            }
                        }
                        
                    
                        if(logger.isInfoEnabled()){
                            logger.info("After applying GIFT prioritization logic to "+results.getResults().size()+" potential content metadata files, there are now "+filesThatQualify.size()+" left to further analyze.");
                        }
                        
                        if(filesThatQualify.isEmpty()){
                            //
                            // Step 3, 4 (because Step 2 probably filtered to much content out)
                            //
                            populateFiles(results, false, criteria.isAnySubsetOfRequired(), filesThatQualify, criteria.getConcepts());
                        }
                        
                        if(criteria.shouldFilterMetadataByGIFTLogic() && filesThatQualify.size() > 1){
                            //there is more than 1 content file to present, therefore we might need
                            //to pair the set down using paradata and randomize logic                        
                            
                            if(logger.isInfoEnabled()){
                                logger.info("Attempting to down-select the "+filesThatQualify.size()+" metadata file(s) using paradata and randomization");
                            }
                            
                            //make the list sorted by number of concepts covered
                            results.sortDescendingByConceptsCovered();
                            
                            //
                            // grab metadata files that cover the same number of required concepts
                            //
                            Map<SearchResult, FileProxy> groupedFiles = new HashMap<>();
                            for(int i = 0; i < results.getResults().size(); i++){
                                
                                groupedFiles.clear();
                                
                                SearchResult first = results.getResults().get(i);
                                if(!filesThatQualify.containsKey(first.getMetadataFile())){
                                    //this result was removed in a previous search
                                    continue;
                                }
                                
                                groupedFiles.put(first, first.getMetadataFile());
                                
                                for(int j = i+1; j < results.getResults().size(); j++){
                                    
                                    SearchResult second = results.getResults().get(j);
                                    if(!filesThatQualify.containsKey(second.getMetadataFile())){
                                        //this result was removed in a previous search
                                        continue;
                                    }
                                    
                                    //group metadata that covers same concepts in order to determine the best in that group
                                    if(first.coversSameConcepts(second)){
                                        groupedFiles.put(second, second.getMetadataFile());
                                    }
                                }
                                
                                if(groupedFiles.size() > 1){
                                    
                                    FileProxy bestFile = null;
                                    
                                    //
                                    // Step 5 - [Remediation Phase Only]
                                    //
                                    if(criteria.getQuadrant() != MerrillQuadrantEnum.RULE || criteria.getQuadrant() != MerrillQuadrantEnum.EXAMPLE ||
                                            criteria.getQuadrant() != MerrillQuadrantEnum.PRACTICE){
                                        
                                        // Step 5.1 - find not-delivered remediation only content
                                        //
                                        // If null is returned Step 5.2.* is handled inherently because content at this point is
                                        // already been delivered, ideal concept coverage, minimal content set, references max possible set
                                        // of metadata attributes and/or is active/passive content. 
                                        bestFile = selectRemediationContentRandomly(groupedFiles, true, true);
                                    }
                                    
                                    if(bestFile == null){
                                        //
                                        // Step 6 and 7 - need to pair down the choices using paradata and randomization logic
                                        //                            
                                        bestFile = ParadataUtil.selectBest(groupedFiles.values(), courseFolder);
                                    }                               
                                    
                                    //remove the other files not selected so they don't get presented as well
                                    for(FileProxy file : groupedFiles.values()){
                                        
                                        if(!file.equals(bestFile)){
                                            
                                            if(logger.isDebugEnabled()){
                                                logger.debug("Removing file from selection because it is not the best among paradata for its grouping: "+file+".");
                                            }
                                            
                                            filesThatQualify.remove(file);
                                            
                                            //restart analysis - if you don't restart the outer for loop chances are not all files will be compared
                                            //                   e.g. 3 files, 2nd file is removed above, second outer loop iteration will start with second file
                                            //                   and never compare anything against the third file in the inner loop.
                                            i = -1;
                                        }
                                    }
                                }
                            }//end for on grouped files
                        }
                    }
                }
                
            }//end for loop on quadrant search results

            MetadataFileSearchResult metadataSearchResult = new MetadataFileSearchResult(filesThatQualify);
            quadrantToQualifiedMetadatas.put(quadrant, metadataSearchResult);
        }
        
        return quadrantToQualifiedMetadatas;
    }
    
    /**
     * Return all combinations of the collection provided.  This is useful for determining which combination
     * of metadata satisfies the highest activity priority for the concepts requested.
     * 
     * @param conceptSearchResultSets contains a descending priority list of sets for each concept.  Each set
     * in that list contains zero or more metadata search results that have the concept and are
     * the same activity type (e.g. Active).  The purpose is to determine the ideal set of activity
     * for the concepts requested based on priority.
     * @return collection of search results where each search result contains zero or more metadata where
     * each metadata covers a required concept and has different activity priority for a concept.  This
     * should be used to determine the best set of metadata based on concept-activity priority.
     */
    private static List<SearchResults> generateSearchResultCombinations(List<List<Set<SearchResult>>> conceptSearchResultSets){
        
        if (conceptSearchResultSets == null || conceptSearchResultSets.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<List<Set<SearchResult>>> result = new ArrayList<>();
            permutationsImpl(conceptSearchResultSets, result, 0, new ArrayList<Set<SearchResult>>());
            
            // combine the list of sets at each index of the collection into a single search results object
            List<SearchResults> listToReturn = new ArrayList<>();
            for(List<Set<SearchResult>> group : result){
                SearchResults searchResults = new SearchResults();
                for(Set<SearchResult> groupSearchResults : group){
                    searchResults.getResults().addAll(groupSearchResults);
                }
                
                listToReturn.add(searchResults);
            }
            return listToReturn;
        }
    }


    /** 
     * Recursively builds the result collection based on the original collection provided.  The result
     * collection contains all permutations of the collection provided.  This is useful for determining which permutation
     * of metadata satisfies the highest activity priority for the concepts requested.
     * 
     * @param ori contains a descending priority list of sets for each concept.  Each set
     * in that list contains zero or more metadata search results that have the concept and are
     * the same activity type (e.g. Active).  The purpose is to determine the ideal set of activity
     * for the concepts requested based on priority.
     * @param result where the permutations are placed.
     * @param d the depth of the current recursion, used to track which index in the collection the logic is at
     * @param current the current permutation being built at this level of the recursion
     */
    private static void permutationsImpl(List<List<Set<SearchResult>>> ori, List<List<Set<SearchResult>>> result, int d, List<Set<SearchResult>> current) {
        // if depth equals number of original collections, final reached, add and return
        if (d == ori.size()) {
            result.add(current);
          return;
        }

        // iterate from current collection and copy 'current' element N times, one for each element
        List<Set<SearchResult>> currentCollection = ori.get(d);
        for (Set<SearchResult> element : currentCollection) {
            List<Set<SearchResult>> copy = new ArrayList<>(current);
            copy.add(element);
            permutationsImpl(ori, result, d + 1, copy);
        }
    }
    
    /**
     * Parse the metadata files found in the provided iterator and return a mapping of file to the in-memory generated object
     * representation of the file contents.  If a file fails to pass schema validation during unmarshalling the file
     * will not be included in the return collection and a log message will created mentioning that file.
     * 
     * @param potentialFilesIterator iterator for the collection of metadata files to parse into memory.  Can't be null.
     * @return mapping of metadata file to the in-memory generated object representation of the file contents.
     */
    private static Map<FileProxy, generated.metadata.Metadata> parseAndRemove(Iterator<FileProxy> potentialFilesIterator){
     
        Map<FileProxy, generated.metadata.Metadata> parsedMetadata = new HashMap<>();
        while(potentialFilesIterator.hasNext()){
            
            FileProxy metadataFile = potentialFilesIterator.next();

            try{
                //parse and validate the xml file
                parsedMetadata.put(metadataFile, MetadataSchemaHandler.getMetadata(metadataFile, true));
                
            }catch(Throwable e){
                logger.error("Ignoring metadata file '"+metadataFile.getFileId()+"' because an exception happened while checking it.  This file should be ignored in future searches.", e);
                continue;
            }
          
        }
      
      return parsedMetadata;
    }
    
    /**
     * Attempts to populate the provided files map with metadata search results that have interactive, constructive or active activity types
     * for a single required concept.
     *
     * @param results contains the metadata to process, i.e. information to apply to the priorities logic
     * @param criteria contains the metadata search criteria including concepts and the activity types to search for
     * @param conceptToPrioritizedSearchResults mapping of concept name that needs metadata search results to the prioritized 
     * activity list of search results. The list value in the map for each concept name key contains a descending priority list of sets.  
     * Each set in that list contains zero or more metadata search results that have the concept and are the same activity 
     * type (e.g. Active).  The purpose is to determine the ideal set of activity for the concepts requested based on priority.
     * <br>
     * This method will not add any entries in the map for a concept if:<br>
     * 1 - the search results contains 0 metadata files for that concept<br>
     * 2 - there are no metadata with the requested activity type in the search results
     */
    private static void handleActivityPrioritization(SearchResults results, MetadataSearchCriteria criteria, 
            Map<String, List<Set<SearchResult>>> conceptToPrioritizedSearchResults){
        
        if(results.getResults().size() > 1){
            // there must be more than one metadata file to try to prioritize 
            
            for(generated.metadata.Concept conceptCriteria : criteria.getConcepts()){
                
                String conceptName = conceptCriteria.getName();
                
                // manage when a new set is needed based on whether the concept is being found again 
                // Concepts can be in the criteria more than once if there is an activity priority defined
                // (e.g. Active then Passive for 'concept A')
                if(conceptToPrioritizedSearchResults.containsKey(conceptName)){
                    List<Set<SearchResult>> conceptPrioritizeMetadata = conceptToPrioritizedSearchResults.get(conceptName);
                    Set<SearchResult> newMetadataGroup = new HashSet<SearchResult>(1);
                    conceptPrioritizeMetadata.add(newMetadataGroup);
                }
                
                // add metadata files that have this concept, grouping by activity type, ordered by activity type prioritization
                // check metadata files for the concept-activity type
                for(SearchResult searchResult : results.getResults()){
                    
                    generated.metadata.Metadata searchResultMetadata = searchResult.getMetadata();
                    for(generated.metadata.Concept searchResultConcept : searchResultMetadata.getConcepts().getConcept()){
                        
                        if(searchResultConcept.getName().equalsIgnoreCase(conceptCriteria.getName())){
                            //found the concept whose map value is being created, now organize by activity type
                            
                            if(searchResultConcept.getActivityType().getType().getClass() == conceptCriteria.getActivityType().getType().getClass()){
                                
                                // Performing the logic of creating map entries here so its easier to tell
                                // outside of this method whether each concept has at least one search result in the map
                                Set<SearchResult> currMetadataGroup;
                                List<Set<SearchResult>> conceptPrioritizeMetadata = conceptToPrioritizedSearchResults.get(conceptName);
                                if(conceptPrioritizeMetadata == null){
                                    conceptPrioritizeMetadata = new ArrayList<Set<SearchResult>>(1);
                                    currMetadataGroup = new HashSet<SearchResult>(1);
                                    conceptPrioritizeMetadata.add(currMetadataGroup);
                                    conceptToPrioritizedSearchResults.put(conceptName, conceptPrioritizeMetadata);
                                }else{
                                    // this is the not the first time we have encountered this concept, therefore 
                                    // add this search result to the last set.  The sets are managed in an outer for loop
                                    
                                    currMetadataGroup = conceptPrioritizeMetadata.get(conceptPrioritizeMetadata.size()-1);
                                }
                                
                                currMetadataGroup.add(searchResult);
                            }
                            
                            break;  // the search result should only have 1 instance of each concept and the concept we were 
                                    // looking for was found in the current search result
                        }
                        
                    }
                    
                }
            }
            
            if(logger.isDebugEnabled()){
                StringBuilder sb = new StringBuilder("After applying activity prioritization per concept:");
                for(String concept : conceptToPrioritizedSearchResults.keySet()){
                    
                    List<Set<SearchResult>> conceptList = conceptToPrioritizedSearchResults.get(concept);
                    sb.append("\n").append(concept).append(" ").append(conceptList.size()).append(" choice sets");
                    if(!conceptList.isEmpty()){
                        sb.append("starting with:\n").append(conceptList.get(0));
                    }
                }
                logger.debug(sb.toString());
            }
            
        }//end if
    }
    
    /**
     * Returns a metadata file (proxy) randomly selected from the collection provided.
     * 
     * @param groupedFiles collection of files to choose randomly from
     * @param newContentOnly whether the collection of files should be filtered before randomly choosing based
     * on whether the content referenced by the metadata has been presented to the learner already.
     * @param remediationOnlyContent whether the collection of files should be filtered before randomly choosing based
     * on whether the metadata contains a 'remediation only' element value matching this parameter's value.
     * @return the randomly selected metadata file reference.  Will be null if the collection provided was empty or
     * if there were no files that adhered to the filtering flags provided.
     */
    private static FileProxy selectRemediationContentRandomly(Map<SearchResult, FileProxy> groupedFiles, 
            boolean newContentOnly, boolean remediationOnlyContent){
        
        FileProxy bestFile = null;
        
        if(groupedFiles != null && !groupedFiles.isEmpty()){
            
            //find new content, if any
            List<SearchResult> candidates = new ArrayList<>();
            for(SearchResult result : groupedFiles.keySet()){
                
                if(newContentOnly && result.isDeliveredContent()){
                    //wanted new content and this content was delivered
                    continue;
                }else if(remediationOnlyContent && !result.isRemediationOnlyContent()){
                    //wanted remediation only content and this content is not tagged with remediation only
                    continue;
                }
                
                candidates.add(result);
            }
            
            if(!candidates.isEmpty()){
                int index = RANDOM_REMEDIATION.nextInt(candidates.size());
                SearchResult selected = candidates.get(index);
                bestFile = selected.getMetadataFile();
            }
        }
        
        return bestFile;
    }
    
    /**
     * This method will populate the files map by following the GIFT metadata prioritization logic mentioned below on
     * the Search Results provided. Those Search Results contain the metadata information to check.
     * <p>
     * Priorities:<p>
     * 1 - Don't Duplicate Content (if possible)<br>
     * 2 - Maximize needed coverage of content (i.e. choose fewest content files for the concepts needed)<br>
     * 3 - Maximize appropriateness via metadata attributes and EMAP learner state tree<br>
     * 4 - Paradata resolution (if more than 1 content passes the above tests for the same set of concepts)
     *     - for each metadata, compare paradata on metadata that covers same concepts<br>
     * 5 - random choice (if more than 1 content matches the above for the same set of concepts) <br>
     * 
     * @param results contains the metadata to process, i.e. information to apply to the priorities logic
     * @param filterDeliveredContent whether or not to filter search results whose metadata references content that has already
     * been delivered to the learner in this course execution.  This is useful for presenting new content to the user if available.
     * @param anySubsetOfRequired flag used to indicate whether any non-empty subset of the required concepts can be used
     * to select a metadata file.  This doesn't mean the metadata file can have extraneous concepts
     * that are not in the required or related concepts list.
     * @param files the map to populate with search result information based on the priority logic.  This map can then be used
     * to present content to the learner.
     * @param requiredConcepts the collection of required concepts being taught at this point in time
     */
    private static void populateFiles(SearchResults results, 
            boolean filterDeliveredContent, boolean anySubsetOfRequired, 
            Map <FileProxy, generated.metadata.Metadata> files, List<generated.metadata.Concept> requiredConcepts){
        
        if(files == null){
            throw new IllegalArgumentException("The files map can't be null.");
        }
        
        //
        // Gather the search result candidates
        //        
        List<SearchResult> resultCandidates = new ArrayList<>(3);
        if(logger.isInfoEnabled()){
            logger.info("There are "+results.getResults().size()+" metadata(s) to consider.  Attempting to filter by: delivered content = "+filterDeliveredContent+" and best concept coverage.\n"+results);
        }
        boolean hasFullCoverageResult = populateSearchResultCandidates(results, filterDeliveredContent, anySubsetOfRequired, resultCandidates);
        
        // At this point the list has:
        //   CASE A 
        //      i. one or more full coverage, non duplicated metadata(s)
        //   CASE B
        //      i. zero full coverage, non duplicated metadata(s)
        //      ii. one or more non-full coverage, non duplicated metadata
        //   CASE C
        //      i. no elements - probably because everything was duplicated
        
        // CASE A -> need to choose by paradata followed by random
        
        if(hasFullCoverageResult){
            //add all, let caller sort by paradata/random
            if(logger.isInfoEnabled()){
                logger.info("There are "+resultCandidates.size()+" metadata(s) that covers all "+requiredConcepts.size()+" concept(s).  Attempting to filter by best metadata attribute matches.\n"+resultCandidates);
            }
            filterResultsByMetadata(resultCandidates);
            if(logger.isInfoEnabled()){
                logger.info("There are now "+resultCandidates.size()+" metadata(s) that covers all "+requiredConcepts.size()+" concept(s) after filtering by best metadata attribute matches.\n"+resultCandidates);
            }
            populateAllFilesFromSearchResults(resultCandidates, files);
        }
        
        // CASE B -> need to sub-sort by metadata matches with the already sorted by concept coverage list
        //     grab set 2,3,...size list at a time
        //      if same concept coverage as earlier choice, choose next in list for set (this is to remove selecting ABC and ABC content since first ABC is better metadata match wise)
        //      for selected set determine if all concepts are covered, if so return file set
        //      otherwise keep grabbing different permutations
        
        else if(!resultCandidates.isEmpty()){
            if(logger.isInfoEnabled()){
                logger.info("There are "+resultCandidates.size()+" metadata(s) that each cover some subset of the "+requiredConcepts.size()+" concept(s).  Attempting to select a group of content to present by max concept coverage and best metadata attribute matches.\n"+resultCandidates);
            }
            handleSearchResultSubset(resultCandidates, files, requiredConcepts);
            if(logger.isInfoEnabled()){
                logger.info("There are now "+resultCandidates.size()+" metadata(s) that each cover some subset of the "+requiredConcepts.size()+" concept(s) after down selecting by max concept coverage and best metadata attribute matching.\n"+resultCandidates);
            }
        }
         
        // CASE C -> choose max coverage, ignoring duplication
        //     start at INIT again ignoring 'if duplicate' check
        //     Note: if reached CASE C again than something is wrong.  Return no files to cause an error.
        //     
        
        else if(filterDeliveredContent && resultCandidates.isEmpty()){
            if(logger.isInfoEnabled()){
                logger.info("There are "+resultCandidates.size()+" metadata(s) that each cover some set of the "+requiredConcepts.size()+" concept(s) and have already been presented to the learner.  Attempting to select from this duplicated set.\n"+resultCandidates);
            }
            populateFiles(results, false, anySubsetOfRequired, files, requiredConcepts);
        }
        
    }
    
    /**
     * Filter the provided list so that it only contains the result candidates with the highest
     * number of metadata matches.  There can be multiple results still remaining after this
     * logic is complete.  Obviously Passive metadata can have 0 to N metadata matches while
     * all other activity types will have 0.  The modified resultCandidates will still contain
     * all other activity types (even though they have 0 metadata matches) to allow logic later 
     * on after this method is called to down select the ICAP metadata by random/paradata logic.
     * 
     * @param resultCandidates the search results to filter based on keeping the ones with the 
     * highest number of metadata matches (or 0 for Interactive-Constructive-Active type activities).
     * No new search result objects will be created.
     */    
    private static void filterResultsByMetadata(List<SearchResult> resultCandidates){
        
        List<SearchResult> toKeep = new ArrayList<>(0);
        int highestMetadataMatch = 0;
        for(SearchResult result : resultCandidates){
            
            if(result.getMetadataAttributeMatches() == highestMetadataMatch){
                //found another result with the same number of matches as the highest thus far
                toKeep.add(result);
            }else if(result.getMetadataAttributeMatches() > highestMetadataMatch){
                //found a new highest metadata match, clear the lower result matches from the list to keep
                toKeep.clear();
                toKeep.add(result);
                highestMetadataMatch = result.getMetadataAttributeMatches();
            }else if(!result.isPassiveContent()){
                //only passive content has metadata attributes, therefore don't exclude interactive/constructive/active content
                //For example if the resultCandidates contains two metadata: A - passive, already delivered, B - active, already delivered
                //this logic should return both even know the active metadata file has no learner state attributes.  Allow
                //the randomize logic to down select (outside of this method)
                toKeep.add(result);
            }
        }
        
        resultCandidates.retainAll(toKeep);
    }
    
    /**
     * This method will attempt to select the ideal set of metadata from the search result candidates provided.
     * The set maybe be a subset of those candidates based on:
     * - a set must cover all the required concepts in some manner
     * - chosen sets have the least amount of duplicate concept coverage (i.e. limit the number of times concept A is presented)
     * - then chose sets that have the highest metadata attribute matches
     * 
     * The method caller is responsible for filtering the file maps further based on paradata and randomness because
     * the map can contain metadata that references the same concept coverage based on the aforementioned logic.
     * 
     * @param resultCandidates collection of Search result candidates to consider when populated the files map
     * @param files contains information from the search results that passed aforementioned logic.
     * @param requiredConcepts the collection of required concepts being taught at this point in time
     */
    private static void handleSearchResultSubset(List<SearchResult> resultCandidates, 
            Map<FileProxy, generated.metadata.Metadata> files, List<generated.metadata.Concept> requiredConcepts){
        
        if(resultCandidates.isEmpty()){
            //there are no result candidates, therefore nothing to do
            return;
        }else if(resultCandidates.size() == 1){
            //there is only 1 result candidate, therefore add it and be done here
            
            SearchResult result = resultCandidates.get(0);
            FileProxy metadataFile = result.getMetadataFile();
            
            if(!files.containsKey(metadataFile)){
                files.put(metadataFile, result.getMetadata());
            }
            
            return;
        }
        
        
        // [A,B,C,D] in descending order of concept coverage
        // 1 -> pre-determined that there isn't 1 item that covers all concepts, therefore skip choosing 1 item as a set
        // 2 -> [{A, B}, {A, C}, {A, D}, {B, C}, {B, D}, {C, D}]
        // 3 -> [{A, B, C}, {A, B, D}, {A, C, D}, {B, C, D}]
        // 4 -> [{A, B, C, D}]
        OrderedPowerSet<SearchResult> pSet = new OrderedPowerSet<SearchResult>(resultCandidates);
        
        //0 - loop until satisfied (start with smallest sets and work up)
        for(int chooseN = 2; chooseN <= resultCandidates.size(); chooseN++){
            
            List<LinkedHashSet<SearchResult>> setsList = pSet.getPermutationsList(chooseN);
        
            //
            // 1 - remove sets where any of the items in that set have the same concept coverage 
            //    (we don't want to present material that covers the same concepts more than 1x)
            Iterator<LinkedHashSet<SearchResult>> itr = setsList.iterator();
            boolean removed;
            while(itr.hasNext()){
                
                removed = false;
                LinkedHashSet<SearchResult> hashset = itr.next();
                SearchResult[] setarray = hashset.toArray(new SearchResult[hashset.size()]);
                for(int i = 0; i < setarray.length; i++){
                    
                    SearchResult first = setarray[i];
                    
                    for(int j = i+1; j < setarray.length; j++){
                        
                        SearchResult second = setarray[j];
                        
                        if(first.coversSameConcepts(second)){
                            //remove this set because two search results cover the same required concepts
                            //therefore this set presents to much duplication of concepts and we don't want that
                            itr.remove();
                            removed = true;
                            break;
                        }
                    }
                    
                    if(removed){
                        //since we removed this set, don't need to keep checking it
                        break;
                    }
                }
                    
                
            }//end while
            
            //
            // 2 - remove sets that don't cover all required concepts
            //
            itr = setsList.iterator();
            Map<LinkedHashSet<SearchResult>, Integer> setToNumConceptsCovered = new HashMap<>();
            
            int totalConceptsCovered = 0;
            List<String> coveredConcepts = new ArrayList<>(requiredConcepts.size());
            List<String> criteriaReqConceptNames = getConcepts(requiredConcepts);
            while(itr.hasNext()){                

                LinkedHashSet<SearchResult> hashset = itr.next();
                totalConceptsCovered = 0;
                coveredConcepts.clear();
                
                SearchResult[] setarray = hashset.toArray(new SearchResult[hashset.size()]);
                for(SearchResult result : setarray){
                    
                    //calculating the number of required concepts covered by the current set
                    totalConceptsCovered += result.getReqConceptsCovered().size();
                    
                    for(String reqConceptName : criteriaReqConceptNames){
                        
                        //loop instead of calling '.contains' in order to support case insensitive search
                        //besides this loop will be equal/smaller too the outer loop based on logic before this method
                        //that removes metadata with extraneous concepts
                        for(String resultConceptName : result.getReqConceptsCovered()){
                            
                            if(reqConceptName.equalsIgnoreCase(resultConceptName)){
                                
                                if(!coveredConcepts.contains(reqConceptName)){
                                    //keep track of the required concept names found thus far
                                    coveredConcepts.add(reqConceptName);
                                }
                                
                                //stop searching for this required concept
                                break;
                            }
                        }
                        
                    }
                }
                
                //store the number of total concepts covered for this set to be used later
                setToNumConceptsCovered.put(hashset, totalConceptsCovered);
                
                if(coveredConcepts.size() != criteriaReqConceptNames.size()){
                    //all the required concepts were NOT found in this set, therefore
                    //it doesn't satisfy the search requirements for this method
                    itr.remove();
                }
                
            }//end while
            
            //all sets were removed because none of them (as a group of N metadata(s)) covered all the concepts required
            if(setsList.isEmpty()){
                continue;
            }
            
            //
            // 3 - create sorted list by # concepts covered by set (ascending order)
            //    this is to limit the amount of concepts covered multiple times (e.g. don't want {ABC, ABD, ABE} - to many A and B)
            sortByConceptCoverage(setsList, setToNumConceptsCovered);
            
            //
            // 4 - i. grab sets that have same # from (3) from the beginning of the sorted list
            //    ii. calculate # of metadata attribute matches
            //    iii. choose set(s) that have the same highest attribute matches 
            List<LinkedHashSet<SearchResult>> groupedSets = new ArrayList<>();
            int totalValue = 0, currentHighMetadataMatchCnt = -1;
            for(LinkedHashSet<SearchResult> set : setsList){
                
                Integer numConceptsCovered = setToNumConceptsCovered.get(set);
                if(totalValue == 0){
                    //this maybe the first iteration of this loop -or-
                    //the last iterations set this to 0 meaning those sets had no concept coverage.
                    totalValue = numConceptsCovered;
                }
                
                if(numConceptsCovered == totalValue){
                    
                    // calculate the total number of metadata attribute matches among all the search results
                    // in this set.
                    // Note: this is a simple best count approach rather than a best average or some other more
                    //       complicated way of picking the best set based on metadata attribute match count alone
                    int candidateHighMetadataMatchCnt = 0;
                    Iterator<SearchResult> resultItr = set.iterator();
                    while(resultItr.hasNext()){
                        candidateHighMetadataMatchCnt += resultItr.next().getMetadataAttributeMatches();
                    }
                    
                    if(currentHighMetadataMatchCnt > candidateHighMetadataMatchCnt){
                        // this set doesn't have more metadata attributes than the last set    
                        continue;
                    }else if(currentHighMetadataMatchCnt == candidateHighMetadataMatchCnt){
                        // this set has the same amount of metadata attributes as the last set
                        groupedSets.add(set);
                        
                    }else{
                        // this set has more metadata attributes than all the last sets

                        // remove the previous since they aren't the best matches anymore
                        groupedSets.clear();
                        
                        // set the new number of metadata matches to meet
                        currentHighMetadataMatchCnt = candidateHighMetadataMatchCnt;

                        groupedSets.add(set);
                    }
                    
                    
                }else{
                    break;
                }
            }
            
            //
            // 5 - add all files from all grouped sets to the files map (removing duplicates)
            //    let caller sort by paradata/random among files that cover the exact same concepts
            //    if there aren't any files, continue looking in the next permutation of the for loop
            if(!groupedSets.isEmpty()){
                
                //get files for sets
                for(LinkedHashSet<SearchResult> set : groupedSets){
                    
                    Iterator<SearchResult> itrResult = set.iterator();
                    while(itrResult.hasNext()){
                        
                        SearchResult result = itrResult.next();
                        FileProxy metadataFile = result.getMetadataFile();
                        
                        if(!files.containsKey(metadataFile)){
                            files.put(metadataFile, result.getMetadata());
                        }
                    }
                }
                
                //done searching
                break;
            }
            
        }//end for
    }
    
    /**
     * Sort the list of sets provided based on the number of required concepts (ascending order) covered by that set's metadata(s).
     * The number can be greater than or equal to the number of required concepts.
     * This is useful because the GIFT prioritization logic doesn't want concepts to be covered too many times in a row (i.e. a single
     * sequence of content such as for the Rule quadrant).
     * 
     * For example:
     * Result Sets: A, B, C have 3, 4, 3 number of required concepts covered from the list of 2 required concepts.  This means that there
     * are multiple metadata filess in each set with some overlapping concept coverage (how else can you get 3 or 4 from 2)
     * Resulting Sorted Set: A, C, B - this places A and C at higher priority than B since B duplicates concept coverage more than A and  B.
     * 
     * @param setsList the list of search results to sort based on the total number of required concepts covered by each set
     * @param setToNumConceptsCovered contains information on the total number for each set in the collection
     */
    private static void sortByConceptCoverage(List<LinkedHashSet<SearchResult>> setsList, final Map<LinkedHashSet<SearchResult>, Integer> setToNumConceptsCovered){
        
        if(setToNumConceptsCovered == null){
            throw new IllegalArgumentException("The set to number concepts covered map can't be null.");
        }
        
        Collections.sort(setsList, new Comparator<LinkedHashSet<SearchResult>>() {

            @Override
            public int compare(LinkedHashSet<SearchResult> o1,
                    LinkedHashSet<SearchResult> o2) {

                if(!setToNumConceptsCovered.containsKey(o1)){
                    //TODO: really should be throwing an error here!
                    return 1;
                }else if(!setToNumConceptsCovered.containsKey(o2)){
                    //TODO: really should be throwing an error here!
                    return -1;
                }else{                    
                    //ascending order
                    return setToNumConceptsCovered.get(o1).compareTo(setToNumConceptsCovered.get(o2));
                }
            }
            
        });
    }
    
    /**
     * Populate the Search Result candidates collection by searching the Search Results instance for possible
     * matches of the following logic:</br>
     * </br>
     *  If 'filtering delivered content'</br>
     *      then ignore search results with metadata that references content that has already been delivered</br>
     *  Else If a search result covers all the required concepts (being taught at this time)</br>
     *      then add it to the candidates collection</br>
     *  Else If reached a search result that doesn't cover all the required concepts and a previous search result did</br>
     *      then the method is over because having at least one full concept coverage is the ideal scenario</br>
     *  Else add the search result to the candidate collection</br>
     * </br>
     * @param results contains information about different metadata files of which needs to be searched to determine
     * which entries are candidates at this time.  
     * @param filterDeliveredContent whether or not to filter search results whose metadata references content that has already
     * been delivered to the learner in this course execution.  This is useful for presenting new content to the user if available.
     * @param anySubsetOfRequired flag used to indicate whether any non-empty subset of the required concepts can be used
     * to select a metadata file.  This doesn't mean the metadata file can have extraneous concepts
     * that are not in the required or related concepts list.
     * @param resultCandidates the collection to populate with search result candidates that pass the prioritization logic.
     * @return boolean whether or not the search result candidates contains one or more full required concept coverage metadata.
     */
    private static boolean populateSearchResultCandidates(SearchResults results, boolean filterDeliveredContent, boolean anySubsetOfRequired, List<SearchResult> resultCandidates){
        
        if(results == null){
            throw new IllegalArgumentException("The search results can't be null.");
        }
        
        //need to sort in order for the for loop logic to work correctly since it assumes
        //that entries earlier in the list have more concept coverage
        results.sortDescendingByConceptsCovered();
        
        boolean hasFullCoverageResult = false;
        for(SearchResult result : results.getResults()){
            
            if(filterDeliveredContent && result.isDeliveredContent()){
                //Priority 1: try not to present content that has already been delivered
                continue;
            }else if(result.doesCoverAllConcepts()){
                //Priority 1+2: a not presented yet content that covers all concepts
                resultCandidates.add(result);
                hasFullCoverageResult = true;
            }else if(hasFullCoverageResult /*&& !anySubsetOfRequired*/){
                //MH 11/8/16 ticket #2887: anySubsetOfRequired is true because of it being set in MerrillsBranchPointHandler 
                //for ticket #2791.  This causes all content to be added in the following else which causes all content to be presented instead
                //of just the not presented yet content that covers all concepts.
                //there is already one or more full coverage content that hasn't been delivered yet, 
                //therefore don't need this content which only covers a subset.
                //Furthermore, because the search results are sorted by concept coverage in descending order we know
                //that there are no other full coverage content after this search result.
                break;  
            }else{
                //Priority 1+2: add a not presented yet content that covers a subset of the concepts (minimize number of these files later)
                resultCandidates.add(result);
            }
        }
        
        return hasFullCoverageResult;
    }
    
    /**
     * This method simply populates the files map with the appropriate information supplied by the search results provided.
     * 
     * @param resultCandidates the search results to retrieve the necessary information from for the files map
     * @param files the map to populate using the search results
     */
    private static void populateAllFilesFromSearchResults(List<SearchResult> resultCandidates, Map<FileProxy, generated.metadata.Metadata> files){
        
        if(files == null){
            throw new IllegalArgumentException("The files map can't be null.");
        }
        
        for(SearchResult result : resultCandidates){
            files.put(result.getMetadataFile(), result.getMetadata());
        }
    }
    
    /**
     * Gather and return the concept names in the list of generated metadata concept objects.
     * 
     * @param concepts the collection of metadata concepts to gather concept names from
     * @return List<String> concept names, will not contain duplicate names even if the content of the 
     * concepts list provided does have duplicate concept names.
     */
    private static List<String> getConcepts(List<generated.metadata.Concept> concepts){
        
        if(concepts == null){
            throw new IllegalArgumentException("The concepts list can't be null.");
        }
        
        List<String> conceptNames = new CaseInsensitiveList();
        for(generated.metadata.Concept concept : concepts){
           
            if(!conceptNames.contains(concept.getName())){
                conceptNames.add(concept.getName());
            }
        }
        return conceptNames;
    }
    
    /**
     * Returns whether the collection of metadata covers all of the concepts specified in some 
     * permutation (i.e. does some set from the metadata mention all of the concepts at least once).  
     * 
     * @param concepts the concepts to check for in the metadatas provided
     * @param metadatas the metadata to check if it's referenced concepts cover the concepts specified.
     * @return boolean true iff some permutation of the metadata provided covers all the concepts specified.
     */
    @SuppressWarnings("unused")
    private boolean coversAllConcepts(List<generated.metadata.Concept> concepts, Collection<generated.metadata.Metadata> metadatas){
        
        boolean found;
        for(generated.metadata.Concept concept : concepts){
            
            found = false; //reset
            
            for(generated.metadata.Metadata metadata : metadatas){
                
                for(generated.metadata.Concept metadataConcept : metadata.getConcepts().getConcept()){
                    
                    if(concept.getName().equalsIgnoreCase(metadataConcept.getName())){
                        found = true;
                        break;
                    }
                }
                
                if(found){
                    break;
                }
            }
            
            if(!found){
                return false;
            }
        }
        
        return true;
        
    }
    
    /**
     * This inner class is used to wrap a collection of results and provide helper methods
     * to organize and process those results.
     * 
     * @author mhoffman
     *
     */
    private static class SearchResults{
        
        List<SearchResult> results = new ArrayList<>();
        
        public SearchResults(){
            
        }
        
        /**
         * Add a search result to the collection of results.
         * 
         * @param result the result to add
         */
        public void addResult(SearchResult result){
            
            if(result == null){
                throw new IllegalArgumentException("The result can't be null.");
            }
            
            results.add(result);
        }
        
        /**
         * Return the collection of search results known to this class.
         * 
         * @return collection of search results.  Won't be null.
         */
        public List<SearchResult> getResults(){
            return results;
        }
        
        /**
         * Sort the collection of search results known to this class by
         * the number of required concepts covered in descending order.
         * This is useful for trying to select the Search Result (i.e. metadata)
         * that has the most required concepts coverage.
         */
        public void sortDescendingByConceptsCovered(){
            
            Collections.sort(results, new Comparator<SearchResult>() {

                @Override
                public int compare(SearchResult o1, SearchResult o2) {
                    //note: descending the parameters are switched below
                    return o2.getReqConceptsCovered().size() - o1.getReqConceptsCovered().size();
                }
                
                
            });
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[SearchResults: ");
            sb.append("results = {");
            for(SearchResult result : results){
                sb.append("\n").append(result).append(",");
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }    

    /**
     * This inner class contains the specifics about a single metadata file that is being considered
     * for the current part of the course that needs content to be delivered.
     * 
     * @author mhoffman
     *
     */
    private static class SearchResult{
        
        private FileProxy metadataFile;
        
        /** the jaxb contents of the metadata file */
        private generated.metadata.Metadata metadata;
        
        /** 
         * whether the content referenced by the metadata has been 
         * delivered to the learner in the current course execution 
         */
        private boolean deliveredContent = false;
        
        /**
         * the current required concepts that are covered by this metadata
         */
        private List<String> reqConceptsCovered;
        
        /**
         * whether this metadata covers all the current required concepts
         */
        private boolean coversAllConcepts = false;
        
        /**
         * the number of ideal metadata attributes that were matched in this metadata
         */
        private int metadataAttributeMatches = 0;
        
        /**
         * Set the provided attributes.
         * 
         * @param metadataFile the file containing the metadata
         * @param metadata the jaxb contents of the metadata file
         * @param reqConceptsMatchedList the current required concepts that are covered by this metadata
         */
        public SearchResult(FileProxy metadataFile, generated.metadata.Metadata metadata, List<String> reqConceptsMatchedList){
            
            if(metadataFile == null){
                throw new IllegalArgumentException("The metadata file of "+metadataFile+"can't be null and must exist.");
            }
            
            if(metadata == null){
                throw new IllegalArgumentException("The metadata can't be null.");
            }
            
            if(reqConceptsMatchedList == null){
                throw new IllegalArgumentException("The required concepts matched list can't be null.");
            }
            
            this.metadataFile = metadataFile;
            this.metadata = metadata;
            this.reqConceptsCovered = reqConceptsMatchedList;
        }
        
        public FileProxy getMetadataFile(){
            return metadataFile;
        }
        
        public generated.metadata.Metadata getMetadata(){
            return metadata;
        }
        
        public void setIsDeliveredContent(boolean value){
            this.deliveredContent = value;
        }
        
        public boolean isDeliveredContent(){
            return deliveredContent;
        }
        
        public void setCoversAllConcepts(boolean value){
            this.coversAllConcepts = value;
        }
        
        public boolean doesCoverAllConcepts(){
            return coversAllConcepts;
        }
        
        public void setMetadataAttributeMatches(int value){
            this.metadataAttributeMatches = value;
        }
        
        public int getMetadataAttributeMatches(){
            return metadataAttributeMatches;
        }
        
        public List<String> getReqConceptsCovered(){
            return reqConceptsCovered;
        }
        
        /**
         * Return true if the ALL the concept activity types are passive (i.e. not interactive, constructive or active).
         * 
         * @return true if all passive activity types for each concept defined in the metadata object
         */
        public boolean isPassiveContent(){
            
            if(metadata.getConcepts() != null && 
                    !metadata.getConcepts().getConcept().isEmpty()){
                
                for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){
                    
                    if(!(concept.getActivityType().getType() instanceof ActivityType.Passive)){
                        return false;
                    }
                }
                
                return true;
            }
            
            return false;
        }
        
        /**
         * Return whether the provided search result covers the same required concepts as this
         * search result.  The order can be different but the elements must match, therefore the size
         * will be the same as well.
         * 
         * @param otherSearchResult the other search result to compare to this
         * @return boolean true iff the two search results cover the exact same required concepts
         */
        public boolean coversSameConcepts(SearchResult otherSearchResult){
         
            if(this.getReqConceptsCovered().size() == otherSearchResult.getReqConceptsCovered().size()){
                
                boolean match;
                for(String thisConcept : this.getReqConceptsCovered()){
                 
                    match = false;
                    for(String otherConcept : otherSearchResult.getReqConceptsCovered()){
                        
                        if(thisConcept.equalsIgnoreCase(otherConcept)){
                            match = true;
                            break;
                        }
                    }
                    
                    if(!match){
                        return false;
                    }
                    
                }
                
                return true;
            }
            
            return false;
        }
        
        /**
         * Return whether the metadata for this search result is tagged with remediation
         * only as true.
         * 
         * @return true if the metadata is tagged with remediation only equal to true
         */
        public boolean isRemediationOnlyContent(){            
            return metadata.getPresentAt().getRemediationOnly() == generated.metadata.BooleanEnum.TRUE ;
        }
        
        @Override
        public boolean equals(Object otherSearchResult){
         
            if(otherSearchResult instanceof SearchResult){
                
                SearchResult other = (SearchResult)otherSearchResult;
                return this.getMetadataFile().equals(other.getMetadataFile()) &&
                    ((this.doesCoverAllConcepts() && other.doesCoverAllConcepts()) || !(this.doesCoverAllConcepts() || other.doesCoverAllConcepts())) &&
                    ((this.doesCoverAllConcepts() && other.doesCoverAllConcepts()) || !(this.doesCoverAllConcepts() || other.doesCoverAllConcepts())) &&
                    this.getMetadataAttributeMatches() == other.getMetadataAttributeMatches() &&
                    this.coversSameConcepts(other);
            }
            
            return false;
        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 19;
        	int mult = 61;
        	
        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + this.getMetadataFile().hashCode();
        	hash = mult * hash + this.getMetadataAttributeMatches();
        	hash = mult * hash + (this.doesCoverAllConcepts() ?  0 : mult);
        	
        	return hash;
        }
                
        
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[SearchResult: ");
            sb.append("metadataFile = ").append(getMetadataFile());
            sb.append(", coversAllConcepts = ").append(doesCoverAllConcepts());
            sb.append(", deliveredContentAlready = ").append(isDeliveredContent());
            sb.append(", numMetadataMatches = ").append(getMetadataAttributeMatches());
            sb.append(", requireConceptsCovered = {");
            for(String concept : getReqConceptsCovered()){
                sb.append(" ").append(concept).append(",");
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This inner class can create the power sets for the list of items provided which is useful
     * for trying different selections of metadata in order to facilitate GIFT metadata prioritization rules.
     * 
     * @author mhoffman
     * {@link "http://stackoverflow.com/questions/20935315/java-generate-all-possible-combinations-of-a-given-list"}
     * @param <E> the type of the items being permutated.
     */
    public static class OrderedPowerSet<E> {
        
        private List<E> inputList;
        public int N;
        private Map<Integer, List<LinkedHashSet<E>>> map = 
                new HashMap<Integer, List<LinkedHashSet<E>>>();

        public OrderedPowerSet(List<E> list) {
            inputList = list;
            N = list.size();
        }

        /**
         * Return the collection of sets choosing 'elementCount' of 'list.size'.
         * 
         * For example:
         * input = {A,B,C}
         * elementCount = 2
         * return = [{A,B}, {A,C}, {B,C}]
         * 
         * @param elementCount the number of elements to select for each set in the list.
         * @return List<LinkedHashSet<E>> a collection of sets, where the size of each set
         * is equal to 'elementCount'.  The order of the sets will reflect the order of the items
         * in the input list provided in the constructor.
         */
        public List<LinkedHashSet<E>> getPermutationsList(int elementCount) {
            if (elementCount < 1 || elementCount > N) {
                throw new IndexOutOfBoundsException(
                        "Can only generate permutations for a count between 1 to " + N);
            }
            if (map.containsKey(elementCount)) {
                return map.get(elementCount);
            }

            ArrayList<LinkedHashSet<E>> list = new ArrayList<LinkedHashSet<E>>();

            if (elementCount == N) {
                list.add(new LinkedHashSet<E>(inputList));
            } else if (elementCount == 1) {
                for (int i = 0 ; i < N ; i++) {
                    LinkedHashSet<E> set = new LinkedHashSet<E>();
                    set.add(inputList.get(i));
                    list.add(set);
                }
            } else {
                list = new ArrayList<LinkedHashSet<E>>();
                for (int i = 0 ; i <= N - elementCount ; i++) {
                    @SuppressWarnings("unchecked")
                    ArrayList<E> subList = (ArrayList<E>)((ArrayList<E>)inputList).clone();
                    for (int j = i ; j >= 0 ; j--) {
                        subList.remove(j);
                    }
                    OrderedPowerSet<E> subPowerSet =  new OrderedPowerSet<E>(subList);

                    List<LinkedHashSet<E>> pList =  subPowerSet.getPermutationsList(elementCount-1);
                    for (LinkedHashSet<E> s : pList) {
                        LinkedHashSet<E> set = new LinkedHashSet<E>();
                        set.add(inputList.get(i));
                        set.addAll(s);
                        list.add(set);
                    }               
                }
            }

            map.put(elementCount, list);

            return map.get(elementCount);
        }
    }
}
