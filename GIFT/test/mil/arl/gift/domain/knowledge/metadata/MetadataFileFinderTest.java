/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;

/**
 * Used to test the domain module's metadata file finder logic.
 * 
 * @author mhoffman
 *
 */
public class MetadataFileFinderTest {

    @Test
    @Ignore("Requires a course folder with metadata files")
    public void fileFinderTests() {

        String courseFolderPath = ".." + File.separator + "Domain" + File.separator + "workspace" + File.separator + "Public" + File.separator + "2991 rule example restrictions test";
        
        //
        // example phase, single concept, passive activity filtering
        //
        generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
        presentAt.setMerrillQuadrant(MerrillQuadrantEnum.EXAMPLE.getName());
        MetadataSearchCriteria criteria = new MetadataSearchCriteria(presentAt);
        
        //don't want metadata to be filtered by GIFT logic, we want all metadata for the quadrant and the concepts specified
        criteria.setFilterMetadataByGIFTLogic(false);
        
        //allow various subsets of the requested concepts to be used to find metadata (i.e. a returned metadata doesn't
        //have to have every concept but at least 1 and no concepts other than the ones specified)
        criteria.setAnySubsetOfRequired(true);
        
        generated.metadata.Concept concept = new generated.metadata.Concept();
        concept.setName("concept a");
        generated.metadata.Attribute attribute = new generated.metadata.Attribute();
        attribute.setValue(MetadataAttributeEnum.MEDIUM_DIFFICULTY.getName());
        generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
        passive.setAttributes(new generated.metadata.Attributes());
        passive.getAttributes().getAttribute().add(attribute);
        generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
        activityType.setType(passive);
        concept.setActivityType(activityType);
        
        List<generated.metadata.Concept> concepts = new ArrayList<>();
        concepts.add(concept);
        criteria.addConcepts(concepts);        
        
        try{
            Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<>();
            quadrantSearchCriteria.put(criteria.getQuadrant(), criteria);
            Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToMetadata =  MetadataFileFinder.findFiles(new DesktopFolderProxy(new File(courseFolderPath)), quadrantSearchCriteria);
            Map<FileProxy, generated.metadata.Metadata> files = quadrantToMetadata.get(criteria.getQuadrant()) != null ?
                    quadrantToMetadata.get(criteria.getQuadrant()).getMetadataFilesMap() : new HashMap<>(0);

            System.out.println("Found "+files.size()+" files matching criteria.");
            for(FileProxy file : files.keySet()){
                System.out.println(file.getFileId());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        //
        // remediation phase, no quadrant, single concept, passive filtering
        //
        presentAt = new generated.metadata.PresentAt();
        presentAt.setMerrillQuadrant(null);
        presentAt.setRemediationOnly(generated.metadata.BooleanEnum.TRUE);
        criteria = new MetadataSearchCriteria(presentAt);
        
        //don't want metadata to be filtered by GIFT logic, we want all metadata for the quadrant and the concepts specified
        criteria.setFilterMetadataByGIFTLogic(false);
        
        //allow various subsets of the requested concepts to be used to find metadata (i.e. a returned metadata doesn't
        //have to have every concept but at least 1 and no concepts other than the ones specified)
        criteria.setAnySubsetOfRequired(true);
        
        concept = new generated.metadata.Concept();
        concept.setName("concept a");
        attribute = new generated.metadata.Attribute();
        attribute.setValue(MetadataAttributeEnum.MEDIUM_DIFFICULTY.getName());
        passive = new generated.metadata.ActivityType.Passive();
        passive.setAttributes(new generated.metadata.Attributes());
        passive.getAttributes().getAttribute().add(attribute);
        activityType = new generated.metadata.ActivityType();
        activityType.setType(passive);
        concept.setActivityType(activityType);
        
        concepts = new ArrayList<>();
        concepts.add(concept);
        criteria.addConcepts(concepts);        
        
        try{
            Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<>();
            quadrantSearchCriteria.put(criteria.getQuadrant(), criteria);
            Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToMetadata =  MetadataFileFinder.findFiles(new DesktopFolderProxy(new File(courseFolderPath)), quadrantSearchCriteria);
            Map<FileProxy, generated.metadata.Metadata> files = quadrantToMetadata.get(criteria.getQuadrant()) != null ?
                    quadrantToMetadata.get(criteria.getQuadrant()).getMetadataFilesMap() : new HashMap<>(0);
                    
            System.out.println("Found "+files.size()+" files matching criteria.");
            for(FileProxy file : files.keySet()){
                System.out.println(file.getFileId());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        //
        // remediation phase, no quadrant, single concept, constructive filtering
        //
        presentAt = new generated.metadata.PresentAt();
        presentAt.setMerrillQuadrant(null);
        presentAt.setRemediationOnly(generated.metadata.BooleanEnum.TRUE);
        criteria = new MetadataSearchCriteria(presentAt);
        
        //don't want metadata to be filtered by GIFT logic, we want all metadata for the quadrant and the concepts specified
        criteria.setFilterMetadataByGIFTLogic(false);
        
        //allow various subsets of the requested concepts to be used to find metadata (i.e. a returned metadata doesn't
        //have to have every concept but at least 1 and no concepts other than the ones specified)
        criteria.setAnySubsetOfRequired(true);
        
        concept = new generated.metadata.Concept();
        concept.setName("concept a");
        generated.metadata.ActivityType.Constructive constructive = new generated.metadata.ActivityType.Constructive();
        activityType = new generated.metadata.ActivityType();
        activityType.setType(constructive);
        concept.setActivityType(activityType);
        
        concepts = new ArrayList<>();
        concepts.add(concept);
        criteria.addConcepts(concepts);        
        
        try{
            Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<>();
            quadrantSearchCriteria.put(criteria.getQuadrant(), criteria);
            Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToMetadata =  MetadataFileFinder.findFiles(new DesktopFolderProxy(new File(courseFolderPath)), quadrantSearchCriteria);
            Map<FileProxy, generated.metadata.Metadata> files = quadrantToMetadata.get(criteria.getQuadrant()) != null ?
                    quadrantToMetadata.get(criteria.getQuadrant()).getMetadataFilesMap() : new HashMap<>(0);
                    
            System.out.println("Found "+files.size()+" files matching criteria.");
            for(FileProxy file : files.keySet()){
                System.out.println(file.getFileId());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
