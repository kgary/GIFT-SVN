/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generated.metadata.BooleanEnum;
import mil.arl.gift.common.course.dkf.AbstractDKFHandler.AdditionalDKFValidationSettings;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.io.FileProxy;

/**
 * This class specifies search parameters that can be used to find matching domain metadata files.
 * 
 * @author mhoffman
 *
 */
public class MetadataSearchCriteria {
    
    /** 
     * the concepts the content MUST cover as well as the metadata attributes for each concept
     * i.e. these are the concepts that need to be covered in the content referenced by the metadata found
     */
    private generated.metadata.Metadata.Concepts requiredConcepts = new generated.metadata.Metadata.Concepts();
    
    /** 
     * the related concepts the content could contain as well as the metadata attributes for each concept 
     * i.e. these are the concepts that related to the required concepts but aren't necessary to be in the
     * content referenced by the metadata found.
     */
    private generated.metadata.Metadata.Concepts relatedOptionalConcepts = new generated.metadata.Metadata.Concepts();
    
    /** 
     * the Global Merrill's quadrant the content should adhere too (i.e. Rule - content presents rules for the concepts) 
     */
    private MerrillQuadrantEnum quadrant;
    
    /** 
     * whether remedial tagged content only is being searched for 
     */
    private boolean remediationOnly = false;
    
    /**
     * whether to exclude Rule and Example phase tagged content from the content selection of the after recall remediation phase
     */
    private boolean excludeRuleExampleContent = false;
    
    /** optional settings for validating any DKFs referenced in any metadata files found */
    private AdditionalDKFValidationSettings additionalValidation;
    
    /** 
     * optional list of metadata files to ignore when searching 
     * This could be due to validation errors.
     */
    private List<FileProxy> filesToIgnore = new ArrayList<>(0);
    
    /** 
     * optional list of content that have already been delivered to the learner 
     * in the current course execution.
     */
    private List<String> deliveredContent = new ArrayList<>(0);
    
    /**
     * flag used to indicate whether the metadata files found with the concepts and attributes
     * should be further filtered based on GIFT logic
     * <p>
     * Priorities:<p>
     * 1 - Don't Duplicate Content (if possible)<br>
     * 2 - Maximize needed coverage of content (i.e. choose fewest content files for the concepts needed)<br>
     * 3 - Maximize appropriateness via metadata attributes and EMAP learner state tree<br>
     * 4 - Paradata resolution (if more than 1 content passes the above tests for the same set of concepts)
     *     - for each metadata, compare paradata on metadata that covers same concepts<br>
     * 5 - random choice (if more than 1 content matches the above for the same set of concepts) <br>
     */
    private boolean filterMetadataByGIFTLogic = true;
    
    /**
     * flag used to indicate whether any non-empty subset of the required concepts can be used
     * to select a metadata file.  This doesn't mean the metadata file can have extraneous concepts
     * that are not in the required or related concepts list.
     */
    private boolean anySubsetOfRequired = false;
    
    /**
     * Whether the metadata files found should be validated against GIFT logic.  This can
     * add additional time and resources to read in and validate other GIFT files (e.g. dkf, training app ref)
     */
    private boolean validate = true;
    
    /**
     * Default constructor - set attributes
     * 
     * @param presentAt the global presentation information to use to filter on.  If the Merrill's quadrant is null the remediation only
     * must be true.  Can't be null. 
     */
    public MetadataSearchCriteria(generated.metadata.PresentAt presentAt){

        if(presentAt == null){
            throw new IllegalArgumentException("The presentation filter can't be null.");
        }
        
        String quadrantStr = presentAt.getMerrillQuadrant();
        if(quadrantStr != null){
            this.quadrant = MerrillQuadrantEnum.valueOf(quadrantStr);
        }

        generated.metadata.BooleanEnum remediationOnlyEnum = presentAt.getRemediationOnly();
        this.remediationOnly = remediationOnlyEnum != null && remediationOnlyEnum.equals(BooleanEnum.TRUE);
        
        if(this.quadrant == null && !this.remediationOnly){
            throw new IllegalArgumentException("Currently don't support searching for all metadata, therefore either the adaptive courseflow phase or remediation only value must be set.");
        }
    }
    
    /**
     * Return whether the metadata files found should be validated against GIFT logic.  This can
     * add additional time and resources to read in and validate other GIFT files (e.g. dkf, training app ref)
     * 
     * @return the value set for this search criteria
     */
    public boolean shouldValidate() {
        return validate;
    }

    /**
     * Set whether the metadata files found should be validated against GIFT logic.  This can
     * add additional time and resources to read in and validate other GIFT files (e.g. dkf, training app ref)
     * 
     * @param validate the value to use for this flag
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public void setAdditionalValidation(AdditionalDKFValidationSettings additionalValidation){
        this.additionalValidation = additionalValidation;
    }
    
    /**
     * Return optional settings for validating any DKFs referenced in any metadata files found 
     * 
     * @return AdditionalDKFValidationSettings can be null
     */
    public AdditionalDKFValidationSettings getAdditionalValidation(){
        return additionalValidation;
    }
    
    /**
     * Return the Merrill's quadrant the content should adhere too (i.e. Rule - content presents rules for the concepts) 
     * 
     * @return can be null
     */
    public MerrillQuadrantEnum getQuadrant(){
        return quadrant;
    }
    
    /**
     * Whether the search is looking for content tagged for remediation purposes only
     * 
     * @return whether remedial tagged content only is being searched for.  Default is false
     */
    public boolean isRemediationOnly(){
        return remediationOnly;
    }
    
    /**
     * Add metadata file to ignore when searching 
     * 
     * @param metadataFile file to ignore
     */
    public void addFileToIgnore(FileProxy metadataFile){
        
        if(metadataFile != null){
            filesToIgnore.add(metadataFile);
        }
    }
    
    /**
     * Return the list of metadata files to ignore  when searching 
     * 
     * @return List<File> can be empty but not null
     */
    public List<FileProxy> getFilesToIgnore(){
        return filesToIgnore;
    }
    
    /**
     * Add a list of content that have already been delivered to the learner 
     * in the current course execution.  These content can be referenced by one or more metadata
     * files. 
     * 
     * @param content the content reference to add (e.g. file name on disk, website URL)
     */
    public void addDeliveredContent(Collection<String> content){
        
        if(content != null && !content.isEmpty()){
            deliveredContent.addAll(content);
        }
    }
    
    /**
     * Return the list of content that have already been delivered to the learner
     * in the current course execution.  These content can be referenced by one or more metadata
     * files. 
     * 
     * @return List<String> can be empty but not null
     */
    public List<String> getDeliveredContent(){
        return deliveredContent;
    }
    
    /**
     * Add all the concepts in the list to the required concepts to search for.
     * 
     * @param concepts concepts to add.  If null or empty, nothing happens.  Each concept in the list
     * provided can't be null, can't have a null or empty concept name and the concept can't already
     * exist in the list.
     */
    public void addConcepts(List<generated.metadata.Concept> concepts){
        
        if(concepts == null){
            return;
        }
        
        for(generated.metadata.Concept concept : concepts){
            addConcept(concept);
        }
    }    
    
    /**
     * Add a concept to the required concepts to search for.
     * 
     * @param concept can't be null, can't have a null or empty concept name
     */
    public void addConcept(generated.metadata.Concept concept){
        
        if(concept == null){
            throw new IllegalArgumentException("The concept can't be null.");
        }else if(concept.getName() == null || concept.getName().isEmpty()){
            throw new IllegalArgumentException("The concept name can't be null or empty.");
        }
        
        requiredConcepts.getConcept().add(concept);
    }
    
    /**
     * Return the concepts the content MUST cover as well as the metadata attributes for each concept
     * i.e. these are the concepts that need to be covered in the content referenced by the metadata found
     * 
     * @return can be empty.  Can contain multiple of the same concept name if the concept is being request
     * with different activity prioritization.
     */
    public List<generated.metadata.Concept> getConcepts(){
        return requiredConcepts.getConcept();
    }
    
    /**
     * Return a new collection of the unique concept names that are required concepts
     * for this search criteria.
     * @return a new set of the unique concept names.  Wont be null but can be empty if {@link #requiredConcepts} is empty.
     * Can contain less entries than {@link #requiredConcepts} if {@link #requiredConcepts} contains more
     * than one entry for a concept.
     */
    public Set<String> getConceptSet(){
        
        Set<String> conceptSet = new HashSet<>();
        for(generated.metadata.Concept concept : requiredConcepts.getConcept()){
            conceptSet.add(concept.getName());
        }
        
        return conceptSet;
    }
    
    public void addRelatedConcepts(List<generated.metadata.Concept> concepts){
        this.relatedOptionalConcepts.getConcept().addAll(concepts);
    }    
    
    /**
     * Add concepts that are allowed to be in the metadata file as well but are not required to be there.
     * This allows a metadata file to be returned if it has more than the required concepts as long as those additional
     * concepts are in this collection of related concepts.
     * 
     * @param concept can't be null
     */
    public void addRelatedConcept(generated.metadata.Concept concept){
        
        if(concept == null){
            throw new IllegalArgumentException("The concept can't be null.");
        }
        
        relatedOptionalConcepts.getConcept().add(concept);
    }
    
    /**
     * Return the concepts that are allowed to be in the metadata file as well but are not required to be there.
     * This allows a metadata file to be returned if it has more than the required concepts as long as those additional
     * concepts are in this collection of related concepts.
     * 
     * @return List<generated.metadata.Concept> can be empty
     */
    public List<generated.metadata.Concept> getRelatedConcepts(){
        return relatedOptionalConcepts.getConcept();
    }
    
    /**
     * Return whether the metadata files found with the quadrant, concepts and attributes specified should
     * be further filtered based on other GIFT logic.  Currently this logic is paradata analysis and randomization.
     * 
     * @return true if additional filtering should be performed
     */
    public boolean shouldFilterMetadataByGIFTLogic() {
        return filterMetadataByGIFTLogic;
    }

    /**
     * Set whether the metadata files found with the quadrant, concepts and attributes specified should
     * be further filtered based on other GIFT logic. Currently this logic is paradata analysis and randomization.
     * 
     * @param filterMetadataByGIFTLogic if further filtering is needed
     */
    public void setFilterMetadataByGIFTLogic(boolean filterMetadataByGIFTLogic) {
        this.filterMetadataByGIFTLogic = filterMetadataByGIFTLogic;
    }

    /**
     * Return whether any non-empty subset of the required concepts can be used
     * to select a metadata file.  This doesn't mean the metadata file can have extraneous concepts
     * that are not in the required or related concepts list. A value of true can cause the collection of content returned
     * to include multiple instances of the same concept (e.g. 3 content associated with concept 'B')
     * 
     * @return if any subsets of concepts can be used
     */
    public boolean isAnySubsetOfRequired() {
        return anySubsetOfRequired;
    }

    /**
     * Set whether any non-empty subset of the required concepts can be used
     * to select a metadata file.  This doesn't mean the metadata file can have extraneous concepts
     * that are not in the required or related concepts list.  A value of true can cause the collection of content returned
     * to include multiple instances of the same concept (e.g. 3 content associated with concept 'B')
     * 
     * @return if any subsets of concepts can be used
     */
    public void setAnySubsetOfRequired(boolean anySubsetOfRequired) {
        this.anySubsetOfRequired = anySubsetOfRequired;
    }

    /**
     * Return whether to exclude Rule and Example phase tagged content from the content selection 
     * of the after recall remediation phase.
     * 
     * @return default is false
     */
    public boolean isExcludeRuleExampleContent() {
        return excludeRuleExampleContent;
    }

    /**
     * Set whether to exclude Rule and Example phase tagged content from the content selection of
     * the after recall remediation phase
     * 
     * @param excludeRuleExampleContent value to use
     */
    public void setExcludeRuleExampleContent(boolean excludeRuleExampleContent) {
        this.excludeRuleExampleContent = excludeRuleExampleContent;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SearchCriteria: ");
        
        sb.append("\nrequired-concepts = {");
        for(generated.metadata.Concept concept : requiredConcepts.getConcept()){
            
            sb.append(" { name = ").append(concept.getName()).append(", attributes = {");
            
            if(concept.getActivityType() != null && concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                generated.metadata.ActivityType.Passive passive = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType());
                generated.metadata.Attributes attributes = passive.getAttributes();
                if(attributes != null){
                    for(generated.metadata.Attribute attribute : passive.getAttributes().getAttribute()){
                        sb.append(" ").append(attribute.getValue()).append(",");
                    }
                }
            }
            sb.append("}}");
            
            sb.append(",");
        }        
        sb.append("}");
        
        sb.append(",\noptional-concepts = {");
        for(generated.metadata.Concept concept : relatedOptionalConcepts.getConcept()){
            
            sb.append(" name = ").append(concept.getName()).append(", attributes = {");
            sb.append("attributes = {");
            
            if(concept.getActivityType() != null && concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                generated.metadata.ActivityType.Passive passive = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType());
                generated.metadata.Attributes attributes = passive.getAttributes();
                if(attributes != null){  //can be null because MerrillsBranchPointHandler creates an instance with just a concept name and no attributes
                    for(generated.metadata.Attribute attribute : attributes.getAttribute()){
                        sb.append(" ").append(attribute.getValue()).append(",");
                    }
                }
            }
            sb.append("} }");
            
            sb.append(",");
        }        
        sb.append("}");
        
        sb.append(",\n quadrant = ").append(getQuadrant());
        sb.append(", remediationOnly = ").append(isRemediationOnly());
        sb.append(", excludeRuleExampleContent = ").append(isExcludeRuleExampleContent());
        sb.append(", shouldValidate = ").append(shouldValidate());
        sb.append(", additionalValidation = ").append(getAdditionalValidation());
        sb.append(", filterMetadataByGIFTLogic = ").append(shouldFilterMetadataByGIFTLogic());
        sb.append(", anySubsetOfRequired = ").append(isAnySubsetOfRequired());
        sb.append(", filesToIgnore = {\n").append(getFilesToIgnore()).append("}\n");
        sb.append(", filesWithDeliveredContent = {\n").append(getDeliveredContent()).append("}");
        
        sb.append("]");
        return sb.toString();
    }

}
