/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import generated.course.StrategyHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.metadata.SimpleMetadataFileHandler;

/**
 * This class contains information about a Branch Adaptation instructional strategy.  The strategy type
 * could be to progress forward in the course, provide remediation, or skip (i.e. advance) past certain
 * element of the course.
 * 
 * @author mhoffman
 *
 */
public class BranchAdaptationStrategy extends AbstractCourseStrategy {   
    
    private static StrategyHandler DEFAULT_STRATEGY_HANDLER;
    static{
        DEFAULT_STRATEGY_HANDLER = new StrategyHandler();
        DEFAULT_STRATEGY_HANDLER.setImpl("domain.knowledge.strategy.DefaultStrategyHandler");
    }
    
    public static final String DEFAULT_STRATEGY_NAME = "BranchAdaptationStrategy";   
    
    /**
     * contains information about the strategy type
     * {@link AdvancementInfo}, {@link ProgressionInfo}, {@link RemediationInfo}
     */
    private BranchAdpatationStrategyTypeInterface strategyType;
    
    /**
     * Set the strategy type for this branch adaptation 
     * 
     * @param strategyType contains information about the strategy type
     * {@link AdvancementInfo}, {@link ProgressionInfo}, {@link RemediationInfo}
     */
    public BranchAdaptationStrategy(BranchAdpatationStrategyTypeInterface strategyType){
        super(DEFAULT_STRATEGY_NAME, DEFAULT_STRATEGY_HANDLER);
        
        setStrategyType(strategyType);
    }
    
    private void setStrategyType(BranchAdpatationStrategyTypeInterface strategyType){
        
        if(strategyType == null){
            throw new IllegalArgumentException("The strategy type can't be null.");
        }
        
        this.strategyType = strategyType;
    }
    
    /**
     * Return the strategy type for this branch adaptation strategy.  
     * The type contains information about the strategy type
     * {@link AdvancementInfo}, {@link ProgressionInfo}, {@link RemediationInfo}
     * 
     * @return BranchAdpatationStrategyTypeInterface
     */
    public BranchAdpatationStrategyTypeInterface getStrategyType(){
        return strategyType;
    } 
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BranchAdaptationStrategy: ");
        sb.append(super.toString());
        sb.append(", type = ").append(getStrategyType());

        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * A common interface for all strategy types defined in this class.
     * 
     * @author mhoffman
     *
     */
    public interface BranchAdpatationStrategyTypeInterface{
        
    }
    
    /**
     * A common class that contains quadrant content information such as the ideal metadata attributes
     * to search for.
     * 
     * @author mhoffman
     *
     */
    public static abstract class QuadrantInfo{
        
        /** the next quadrant to branch too */
        private MerrillQuadrantEnum quadrant;
        
        /** attributes used to help determine what content to present */
        private List<MetadataAttributeItem> attributes;
        
        /**
         * Default constructor - used when the next quadrant should be the 'no quadrant' quadrant, 
         * i.e. exit the branch point course element.
         */
        public QuadrantInfo(){
            
        }
        
        /**
         * Set the attributes provided.
         * 
         * @param quadrant the next quadrant to branch too
         * @param attributes the collection of ideal attributes to search for when selecting content to present.
         *          Can be empty but not null
         */
        public QuadrantInfo(MerrillQuadrantEnum quadrant, List<MetadataAttributeItem> attributes){
            
            if(quadrant == null){
                throw new IllegalArgumentException("The quadrant can't be null");
            }
            
            if(attributes == null){
                throw new IllegalArgumentException("The attributes can't be null");
            }
            
            this.quadrant = quadrant;
            this.attributes = attributes;
        }        
        
        /**
         * Return the next quadrant to branch too
         * 
         * @return MerrillQuadrantlEnum - can be null as an indication to exit the branch point course transition.
         */
        public MerrillQuadrantEnum getQuadrant(){
            return quadrant;
        }
        
        /**
         * Return the attributes used to help determine what content to present
         * 
         * @return List<MetadataAttributeItem> can be null or empty
         */
        public List<MetadataAttributeItem> getAttributes(){
            return attributes;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("quadrant = ").append(getQuadrant());
            
            sb.append(", attributes = {");
            if(getAttributes() != null){
                for(MetadataAttributeItem attribute : getAttributes()){
                    sb.append(attribute).append(", ");
                }
            }
            sb.append("}");
            
            return sb.toString();
        }
        
    }
    
    /**
     * This strategy type is used to indicate that the branch point course element should progress
     * to the next scheduled element (e.g. Rule content, Example content, Recall test, Practice scenario, end)
     * 
     * @author mhoffman
     *
     */
    public static class ProgressionInfo 
        extends QuadrantInfo    
        implements BranchAdpatationStrategyTypeInterface{        
        
        /**
         * Used to end the branch point course element
         */
        public ProgressionInfo(){
            
        }
        
        /**
         * Progress to the next branch point course element.
         * 
         * @param quadrant the next quadrant to transition too
         * @param attributes collection of ideal attributes used to search for content to present
         *           Can be empty but not null.
         */
        public ProgressionInfo(MerrillQuadrantEnum quadrant, List<MetadataAttributeItem> attributes){
            super(quadrant, attributes);

        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ProgressionInfo: ").append(super.toString());
            sb.append("]");
            
            return sb.toString();
        }
    }
    
    /**
     * This strategy type is used to indicate that one or more concepts can be skipped (i.e. advanced).
     * It contains advancement information for one or more concepts.
     * For example, Concept "A" doesn't need to be taught in the course because the learner is
     * an expert.
     * 
     * @author mhoffman
     */
    public static class AdvancementInfo implements BranchAdpatationStrategyTypeInterface{

        /**  required collection of course concepts that the learner is an expert on (either for knowledge or skill learner state attribute) */
        private List<AdvancementConcept> concepts;
        
        /** true if the course concepts the learner is an expert on is for Skill and not knowledge */
        private boolean isSkill = false;
        
        /**
         * Set attributes for knowledge expertise on one or more concepts.
         * 
         * @param concepts the collection of concepts needing advancement in the course.  Can't be null or empty.
         */
        public AdvancementInfo(List<AdvancementConcept> concepts) {
            this(concepts, false);
        }
        
        /**
         * Class constructor - set attribute(s).
         * 
         * @param concepts the collection of concepts needing advancement in the course, i.e. the learner is an expert on these concepts.
         * Can't be null or empty.
         * @param isSkill true if the course concepts the learner is an expert on is for skill and not knowledge
         */
        public AdvancementInfo(List<AdvancementConcept> concepts, boolean isSkill){
            
            if(concepts == null || concepts.isEmpty()){
                throw new IllegalArgumentException("The advancement concept list must contain at least one entry.");
            }
            
            this.concepts = concepts;
            this.isSkill = isSkill;
        }

        /**
         * Return the collection of course concepts needing advancement in the course, i.e. the learner is an expert on these concepts.
         * 
         * @return List<AdvancementConcept> - Will not be null, nor empty
         */
        public List<AdvancementConcept> getConcepts(){
            return concepts;
        }
        
        /**
         * Return whether these concepts are for knowledge or skill expertise.
         * 
         * @return true if the course concepts the learner is an expert on is for skill and not knowledge.  Default is false.
         */
        public boolean isSkill() {
            return isSkill;
        }
        
        @Override
        public boolean equals(Object otherObject){
            
            if(otherObject instanceof AdvancementInfo){
                
                AdvancementInfo otherAdvancementInfo = (AdvancementInfo)otherObject;
                if(this.getConcepts().size() != otherAdvancementInfo.getConcepts().size()){
                    return false;
                }else{
                    //compare items to each other
                    
                    for(AdvancementConcept concept : this.getConcepts()){
                        
                        if(!otherAdvancementInfo.getConcepts().contains(concept)){
                            return false;
                        }
                    }
                    
                    if(otherAdvancementInfo.isSkill() != isSkill()){
                        return false;
                    }
                    
                    return true;
                }
            }else{
                return false;
            }

        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 2;
        	int mult = 113;
        	
        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + this.getConcepts().size();
        	
        	for(AdvancementConcept concept : this.getConcepts()) {
        		hash = mult * hash + concept.hashCode();
        	}
        	
        	hash += isSkill() ? 1 : 0;
        	
        	return hash;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[AdvancementInfo: ");
            
            sb.append("concepts = {");
            for(AdvancementConcept concept : this.getConcepts()){
                sb.append(" ").append(concept).append(",");
            }
            sb.append("}, isSkill = ").append(isSkill());
            
            sb.append("]");
            return sb.toString();
        }
        
        /**
         * This class contains information about a specific concept that can be skipped because the user
         * is deemed an expert in terms of cognitive knowledge (or other reason deemed apprporiate by the pedagogical model).
         * 
         * @author mhoffman
         *
         */
        public static class AdvancementConcept{            

            /** the unique name of a course concept the learner doesn't need to be re-taught */
            private String concept;
            
            /** (optional) the reason for this advancement (e.g. the learner tested out of this concept, the learner is an expert on this concept) */
            private String reason;
            
            /**
             * Class constructor - set attribute(s).
             * 
             * @param concept the unique name of a course concept the learner doesn't need to be re-taught
             */
            public AdvancementConcept(String concept){
                
                if(concept == null){
                    throw new IllegalArgumentException("The concept can't be null.");
                }
                
                this.concept = concept;
            }
            
            
            /**
             * Return the reason for this advancement 
             * (e.g. the learner tested out of this concept, the learner is an expert on this concept)
             * 
             * @return reason can be null
             */
            public String getReason() {
                return reason;
            }

            /**
             * Set the reason for this advancement 
             * (e.g. the learner tested out of this concept, the learner is an expert on this concept)
             * 
             * @param reason the reason to set.  Can be null.
             */
            public void setReason(String reason) {
                this.reason = reason;
            }

            /**
             * Return the unique name of a course concept the learner doesn't need to be re-taught
             * 
             * @return concept
             */
            public String getConcept() {
                return concept;
            }
            
            @Override
            public boolean equals(Object otherAdvancementConcept){            
                return otherAdvancementConcept instanceof AdvancementConcept && 
                        this.getConcept().equalsIgnoreCase(((AdvancementConcept)otherAdvancementConcept).getConcept());
            }
            
            @Override
            public int hashCode(){
            	
            	// Start with prime number
            	int hash = 23;
            	int mult = 87;

            	// Take another prime as multiplier, add members used in equals
            	hash = mult * hash + this.getConcept().toLowerCase().hashCode();
            	
            	return hash;
            }
            
            @Override
            public String toString(){
                
                StringBuilder sb = new StringBuilder();
                sb.append("[AdvancementConcept: ");
                sb.append("concept = ").append(getConcept());
                sb.append(", reason = ").append(getReason());
                sb.append("]");
                return sb.toString();
            }
        }
        
    }
    
    /**
     * This strategy type is used to indicate that remediation is needed on one or more concepts.
     * 
     * @author mhoffman
     */
    public static class RemediationInfo
        implements BranchAdpatationStrategyTypeInterface{
        
        /** 
         * map of assessed concept name to a descending prioritized list of remediation types.
         * The list won't be null or empty. 
         */
        private Map<String, List<AbstractRemediationConcept>> conceptRemediationPriorityMap = new HashMap<>();
        
        /** whether this remediation request is for after a practice phase versus after recall phase */
        private boolean afterPractice = false;
        
        /**
         * Set/Replace the remediation map.
         * 
         * @param remediationMap map of assessed concept name to a descending prioritized list of remediation types.
         * The map can't have null key strings, empty key strings, null lists, empty lists.
         */
        public void setConceptRemediationMap(Map<String, List<AbstractRemediationConcept>> remediationMap){
            
            if(remediationMap == null){
                throw new IllegalArgumentException("The remediation map can't be null.");
            }
            
            //check the map
            //i. no null key strings
            //ii. no empty key strings
            //iii. no null lists
            //iv. no empty lists
            for(String concept : remediationMap.keySet()){
                
                List<AbstractRemediationConcept> prioritizedList = remediationMap.get(concept);
                
                if(concept == null || concept.isEmpty()){
                    throw new IllegalArgumentException("Found a null or empty concept in the remediation map.");
                }else if(prioritizedList == null || prioritizedList.isEmpty()){
                    throw new IllegalArgumentException("The prioritized list for concept '"+concept+"' can't be null or empty.");
                }
            }
            
            //forcing concept case insensitivity until a third party library is integrated to do this for us (e.g. Apache collections4)
            Map<String, List<AbstractRemediationConcept>> caseInsensitiveRemediationMap = new HashMap<>();
            Iterator<String> conceptNameItr = remediationMap.keySet().iterator();
            while(conceptNameItr.hasNext()){
                
                String conceptName = conceptNameItr.next();
                caseInsensitiveRemediationMap.put(conceptName.toLowerCase(), remediationMap.get(conceptName));                
            }
            
            this.conceptRemediationPriorityMap = caseInsensitiveRemediationMap;
        }

        /**
         * Set the descending prioritized list of remediation for the concept.
         * 
         * @param concept an assessed concept name. Can't be null or empty.
         * @param prioritizedList descending prioritized list of remediation for the concept.  Can't be null or empty.
         */
        public void setConceptRemediation(String concept, List<AbstractRemediationConcept> prioritizedList){
            
            if(concept == null || concept.isEmpty()){
                throw new IllegalArgumentException("The concept can't be null or empty.");
            }else if(prioritizedList == null || prioritizedList.isEmpty()){
                throw new IllegalArgumentException("The prioritized list can't be null or empty.");
            }
            
            //force lower case so we are case insensitive
            conceptRemediationPriorityMap.put(concept.toLowerCase(), prioritizedList);
        }
        
        /**
         * Return the map of assessed concept name to a descending prioritized list of remediation types.
         * 
         * @return can be empty. The map won't have null key strings, empty key strings, null lists, empty lists.
         */
        public Map<String, List<AbstractRemediationConcept>> getRemediationMap(){
            return conceptRemediationPriorityMap;
        }

        /**
         * Return whether this remediation request is for after a practice phase versus after recall phase
         *  
         * @return default is false
         */
        public boolean isAfterPractice() {
            return afterPractice;
        }

        /**
         * Set whether this remediation request is for after a practice phase versus after recall phase
         * 
         * @param afterPractice true if this remediation request is for after practice phase
         */
        public void setAfterPractice(boolean afterPractice) {
            this.afterPractice = afterPractice;
        }

        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[RemediationInfo: ");
            sb.append("afterPractice = ").append(isAfterPractice());
            
            sb.append(", concepts = {\n");
            for(String concept : conceptRemediationPriorityMap.keySet()){
                sb.append(" ").append(concept).append(" :\n");
                
                List<AbstractRemediationConcept> prioritizedList = conceptRemediationPriorityMap.get(concept);
                for(AbstractRemediationConcept remediation : prioritizedList){
                    sb.append("  ").append(remediation).append(",\n");
                }
            }
            sb.append("}");
            
            
            sb.append("]");
            return sb.toString();
        }

    }
    
    /**
     * This inner class contains active remediation information for a specific concept.
     * 
     * @author mhoffman
     *
     */
    public static class ActiveRemediationConcept extends AbstractRemediationConcept{
        
        /**
         * Set attribute
         * 
         * @param concept the name of the concept that needs remediation.  Can't be null or empty.
         */
        public ActiveRemediationConcept(String concept){
            super(concept);
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ActiveRemediationConcept: ");
            sb.append(super.toString());            
            sb.append("]");
            
            return sb.toString();
        }
    }
    
    /**
     * This inner class contains interactive remediation information for a specific concept.
     * 
     * @author mhoffman
     *
     */
    public static class InteractiveRemediationConcept extends AbstractRemediationConcept{
        
        /**
         * Set attribute
         * 
         * @param concept the name of the concept that needs remediation.  Can't be null or empty.
         */
        public InteractiveRemediationConcept(String concept){
            super(concept);
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[InteractiveRemediationConcept: ");
            sb.append(super.toString());            
            sb.append("]");
            
            return sb.toString();
        }
    }
      
    
    /**
     * This inner class contains constructive remediation information for a specific concept.
     * 
     * @author mhoffman
     *
     */
    public static class ConstructiveRemediationConcept extends AbstractRemediationConcept{
        
        /**
         * Set attribute
         * 
         * @param concept the name of the concept that needs remediation.  Can't be null or empty.
         */
        public ConstructiveRemediationConcept(String concept){
            super(concept);
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ConstructiveRemediationConcept: ");
            sb.append(super.toString());            
            sb.append("]");
            
            return sb.toString();
        }
    }
    
    /**
     * This inner class contains passive remediation information for a specific concept in a specific phase (e.g. Example).
     * For example, Concept "A" needs remediation in the form of the Merrill's Rule quadrant.
     * 
     * @author mhoffman
     *
     */
    public static class PassiveRemediationConcept extends AbstractRemediationConcept{
        
        /** the quadrant to execute for the correct level of remediation for the concept */
        private MerrillQuadrantEnum quadrant;
        
        /** attributes used to help determine what content to present */
        private List<MetadataAttributeItem> attributes;
        
        /**
         * Class constructor - set attributes
         * 
         * @param concept the name of the concept that needs remediation.  Can't be null or empty.
         * @param attributes the collection of ideal attributes to search for when selecting content to present.
         *          Can be empty but not null
         * @param quadrant the quadrant to execute for the correct level of remediation for the concept, can't be null.
         */
        public PassiveRemediationConcept(String concept, List<MetadataAttributeItem> attributes, MerrillQuadrantEnum quadrant){
            super(concept);
            
            if(quadrant == null){
                throw new IllegalArgumentException("The quadrant can't be null.");
            }
            
            this.quadrant = quadrant;
            
            if(attributes == null){
                throw new IllegalArgumentException("The attributes can't be null");
            }
            
            this.attributes = attributes;
        }
        
        /**
         * Return the quadrant to execute for the correct level of remediation for the concept
         * 
         * @return won't be null.
         */
        public MerrillQuadrantEnum getQuadrant(){
            return quadrant;
        }
        
        /**
         * Return the attributes used to help determine what content to present
         * 
         * @return won't be null but can be empty
         */
        public List<MetadataAttributeItem> getAttributes(){
            return attributes;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[PassiveRemediationConcept: ");
            sb.append(super.toString());            
            sb.append(", quadrant = ").append(getQuadrant());
            
            sb.append(", attributes = {");
            if(getAttributes() != null){
                for(MetadataAttributeItem attribute : getAttributes()){
                    sb.append(attribute).append(", ");
                }
            }
            sb.append("}");
            sb.append("]");
            
            return sb.toString();
        }
    }
    
    /**
     * This inner class contains remediation information for a specific concept.
     * For example, Concept "A" needs remediation.
     * 
     * @author mhoffman
     *
     */
    public static abstract class AbstractRemediationConcept{
        
        /** the name of the concept that needs remediation */
        private String concept;
        
        /**
         * Class constructor - set attributes
         * 
         * @param concept the name of the concept that needs remediation.  Can't be null or empty.
         */
        public AbstractRemediationConcept(String concept){
            
            if(concept == null || concept.isEmpty()){
                throw new IllegalArgumentException("The concept name can't be empty.");
            }

            this.concept = concept;
        }
        
        /**
         * Return the quadrant to execute for the correct level of remediation for the concept
         * 
         * @return won't be null or empty
         */
        public String getConcept(){
            return concept;
        }

        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("concept = ").append(getConcept());                        
            return sb.toString();
        }
    }
    
    /**
     * This class contains information about a metadata attribute item for a branch adaptation strategy.
     * 
     * @author mhoffman
     *
     */
    public static class MetadataAttributeItem implements Comparable<MetadataAttributeItem>{
        
        /** the default priority value to use if a priority is not specified */
        private static final int DEFAULT_PRIORITY = 1;        
        
        /** the metadata attribute object */
        private generated.metadata.Attribute attribute;
        
        /** used by this class to convert a Attribute XML string to it's generated class equivalent (generated.metadata.Attribute) */
        private static SimpleMetadataFileHandler handler = new SimpleMetadataFileHandler();
        
        /** a non-negative number, the higher the number the higher the priority of this attribute */
        private int priority = DEFAULT_PRIORITY;
        
        /** (optional) a unique label used to distinguish this set of metadata attributes derived from a particular piece
         * of learner state from another piece that may be of the same learner state attribute name/type
         * 
         * For example:  the label could contain a concept name ("map reading") of which the learner state attribute type ("Knowledge")
         * has a certain value ("expert") which is different than a different concept name ("compass") with the same attribute type and
         * different value ("novice").  Two instances of this class would be needed because it is possible two different sets of
         * metadata attributes are needed, one for each concept. 
         */
        private String label;
        
        /**
         * Class constructor - set attribute
         * 
         * @param rawAttribute - the metadata attribute of interest encoded as a string
         */
        public MetadataAttributeItem(String rawAttribute){
            this(rawAttribute, DEFAULT_PRIORITY);
        }

        /**
         * Class constructor - set attribute and it's priority
         * 
         * @param rawAttribute - the metadata attribute of interest encoded as a string
         * @param priority - a non-negative number, the higher the number the higher the priority of this attribute
         */
        public MetadataAttributeItem(String rawAttribute, int priority){
            
            setPriority(priority);
            
            try{
                generated.metadata.Attribute attribute = handler.getAttribute(rawAttribute);
                setAttribute(attribute);
            }catch(Exception e){
                throw new IllegalArgumentException("There was a problem decoding the raw metadata attribute string of '"+rawAttribute+"'.", e);
            }         
        }
        
        /**
         * Class constructor - set attribute and it's priority
         * 
         * @param attribute - the metadata attribute of interest
         */
        public MetadataAttributeItem(generated.metadata.Attribute attribute){
            this(attribute, DEFAULT_PRIORITY);         
        }
        
        /**
         * Class constructor - set attribute and it's priority
         * 
         * @param attribute - the metadata attribute of interest
         * @param priority - a non-negative number, the higher the number the higher the priority of this attribute
         */
        public MetadataAttributeItem(generated.metadata.Attribute attribute, int priority){
            
            setAttribute(attribute);
            setPriority(priority);           
        }
        
        /**
         * Set the a unique label used to distinguish this set of metadata attributes derived from a particular piece
         * of learner state from another piece that may be of the same learner state attribute name/type
         * 
         * @param label - the label could contain a concept name ("map reading") of which the learner state attribute type ("Knowledge")
         * has a certain value ("expert") which is different than a different concept name ("compass") with the same attribute type and
         * different value ("novice").  Two instances of this class would be needed because it is possible two different sets of
         * metadata attributes are needed, one for each concept. 
         */
        public void setLabel(String label){
            this.label = label;
        }
        
        /**
         * Return the  unique label used to distinguish this set of metadata attributes derived from a particular piece
         * of learner state from another piece that may be of the same learner state attribute name/type 
         * 
         * @return String can be null or empty
         */
        public String getLabel(){
            return label;
        }
        
        private void setAttribute(generated.metadata.Attribute attribute){
            
            if(attribute == null){
                throw new IllegalArgumentException("The attribute can't be null.");
            }
            
            if(attribute.getValue() == null){
                throw new IllegalArgumentException("The attribute's value member can't be null.");
            }
            
            this.attribute = attribute;

        }
        
        /**
         * Get the metadata attribute information.
         * 
         * @return generated.metadata.Attribute
         */
        public generated.metadata.Attribute getAttribute(){
            return attribute;
        }
        
        private void setPriority(int priority){
            
            if(priority < 0){
                throw new IllegalArgumentException("The priority can't be negative");
            }
            
            this.priority = priority;
        }
        
        /**
         * Return the priority of this attribute.
         * 
         * @return int - a non-negative number
         */
        public int getPriority(){
            return priority;
        }
        
        public void increasePriority(){
            priority++;
        }
        
        public void decreasePriority(){
            
            if(priority > 0){
                priority--;
            }
        }        

        @Override
        public boolean equals(Object other) {
            
            if(other instanceof MetadataAttributeItem){

                MetadataAttributeItem otherItem = ((MetadataAttributeItem)other);
                
                //check attribute value
                return this.getAttribute().getValue().equals(otherItem.getAttribute().getValue()) &&
                        ((this.getLabel() == null && otherItem.getLabel() == null) ||  
                                (this.getLabel() != null && otherItem.getLabel() != null && this.getLabel().equals(otherItem.getLabel())));                
            }
            
            return false;
        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 17;
        	int mult = 227;
        	
        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + this.getAttribute().getValue().hashCode();
        	
        	if(this.getLabel() != null) {
        		hash = mult * hash + this.getLabel().hashCode();
        	}
        	
        	return hash;
        }
        
        @Override
        public int compareTo(MetadataAttributeItem otherItem) {
            
            if(this.getPriority() < otherItem.getPriority()){
                return -1;
            }else if(this.getPriority() > otherItem.getPriority()){
                return 1;
            }else{
                return 0;
            }
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[MetadataAttributeItem: ");
            sb.append("attribute = ").append(getAttribute().getValue());            
            sb.append(", priority = ").append(getPriority());
            sb.append(", label = ").append(getLabel());
            sb.append("]");
            
            return sb.toString();
        }

    }
}
