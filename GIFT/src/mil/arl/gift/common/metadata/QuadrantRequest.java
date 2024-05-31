/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metadata;

import java.io.Serializable;
import java.util.List;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;

/**
 * Contains the metadata search criteria for an adaptive courseflow phase (e.g. Rule).
 * 
 * @author mhoffman
 *
 */
public class QuadrantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private MerrillQuadrantEnum adaptivePhase;
    
    private boolean includeRemediationContent;
    
    private boolean excludeRuleExampleContent;
    
    private List<String> requiredConcepts;
    
    private List<String> otherCourseConcepts;
    
    /**
     * Required for GWT serialization.  
     * DONT USE.
     */
    public QuadrantRequest() {}
    
    /**
     * Creates a new action to get the list of files corresponding to a Merrill quadrant
     * 
     * @param username the username of the user making the request.  Used for read permission checks.
     * @param courseFilePath the location of the course folder.  Can't be null or empty.  Must exist.
     * @param adaptivePhase the phase of the Merrill's quadrant {@link mil.arl.gift.common.enums.MerrillQuadrantEnum} to use
     * to filter metadata files that are found in the course folder.  
     * Can be null if {@link #includeRemediationContent} is true.
     * @param includeRemediationContent whether to include metadata files in the search results that are tagged with
     * remediation only information.  This essentially ignores the {@link #adaptivePhaseName} parameter.
     * @param requiredConcepts the required concepts to look for in metadata files in the course folder. Can't be null.
     * @param otherCourseConcepts other concepts that can be in the metadata files found (as long as the required concepts are in the file as well).  Can be empty but not null.
     */
    public QuadrantRequest(MerrillQuadrantEnum adaptivePhase, boolean includeRemediationContent,
            List<String> requiredConcepts, List<String> otherCourseConcepts) {
        
        this.includeRemediationContent = includeRemediationContent;
        
        setQuadrant(adaptivePhase);
        setRequiredConcepts(requiredConcepts);
        setOtherConcepts(otherCourseConcepts);   
    }
    
    private void setQuadrant(MerrillQuadrantEnum quadrant){
        
        if(!includeRemediationContent && quadrant == null){
            throw new IllegalArgumentException("The quadrant  can't be null");
        }
        
        this.adaptivePhase = quadrant;
    }
    
    /**
     * the phase of the Merrill's quadrant {@link mil.arl.gift.common.enums.MerrillQuadrantEnum} to use
     * to filter metadata files that are found in the course folder.  
     * 
     * @return Can be null if {@link #includeRemediationContent} is true.
     */
    public MerrillQuadrantEnum getAdpativePhase() {
        return adaptivePhase;
    }
    
    /**
     * Whether to include metadata files in the search results that are tagged with
     * remediation only information.  This essentially ignores the {@link #adaptivePhaseName} parameter.
     * 
     * @return should the search results include remediation only tagged metadata files
     */
    public boolean shouldIncludeRemediationContent(){
        return includeRemediationContent;
    }

    private void setRequiredConcepts(List<String> requiredConcepts){
        
        if(requiredConcepts == null){
            throw new IllegalArgumentException("The required concepts collection can't be null.");
        }
        
        this.requiredConcepts = requiredConcepts;
    }

    /**
     * The required concepts to use when finding content files
     * 
     * @return collection of required concepts to use when searching for content files
     * for a adaptive courseflow phase.  Can't be empty or null.
     */
    public List<String> getRequiredConcepts() {
        return requiredConcepts;
    }
    
    private void setOtherConcepts(List<String> otherCourseConcepts){
        
        if(otherCourseConcepts == null){
            throw new IllegalArgumentException("The other course concepts collection can't be null.");
        }
        
        this.otherCourseConcepts = otherCourseConcepts;
    }
    
    /**
     * The other course concepts not including in the required concepts.
     * 
     * @return collection of the other course concepts. Can be empty but not null.
     */
    public List<String> getOtherCourseConcepts() {
        return otherCourseConcepts;
    }

    /**
     * Return whether to exclude Rule and Example phase tagged content from the content selection 
     * of the after recall remediation phase
     * 
     * @return default is false
     */
    public boolean shouldExcludeRuleExampleContent() {
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
    public String toString() {        
        
        StringBuilder builder = new StringBuilder();
        builder.append("[QuadrantRequest: ");
        builder.append("quadrant=");
        builder.append(adaptivePhase);
        builder.append(", includeRemediationContent=");
        builder.append(includeRemediationContent);
        builder.append(", excludeRuleExampleContent=");
        builder.append(excludeRuleExampleContent);
        builder.append(", requiredConcepts=");
        builder.append(requiredConcepts);
        builder.append(", otherCourseConcepts=");
        builder.append(otherCourseConcepts);
        builder.append("]");
        return builder.toString();
    }

}
