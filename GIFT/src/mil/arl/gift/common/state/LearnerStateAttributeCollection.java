/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class provides information on a complex learner state (e.g. cognitive state such as knowledge maybe
 * broken down into sub-values, each with unique labels).  The complexity differs from a normal learner state
 * attribute in that this class supports multiple values for the attribute type (e.g. Knowledge).
 * 
 * For example:
 * 
 * type: Knowledge
 * short-term: unknown
 * long-term: unknown
 * predicted: unknown
 * 
 * map:
 *     'concept a'
 *          type: knowledge, short-term: novice, ...
 *     'concept b'
 *          type: knowledge, short-term: expert, ...
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class LearnerStateAttributeCollection extends LearnerStateAttribute {
    
    /** map of unique label for the attribute to the attribute's information */
    private Map<String, LearnerStateAttribute> labelToAttribute;
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    @SuppressWarnings("unused")
    private LearnerStateAttributeCollection() {}

    /**
     * Class constructor
     * 
     * @param attribute the attribute that describes this collection.  Can't be null.
     */
    public LearnerStateAttributeCollection(LearnerStateAttributeNameEnum attribute){
        super(attribute, attribute.getAttributeDefaultValue(), attribute.getAttributeDefaultValue(), attribute.getAttributeDefaultValue());
        
        labelToAttribute = new HashMap<>();
    }
    
    /**
     * Class constructor - use the map provided.
     * 
     * @param attribute the attribute that describes this collection
     * @param labelToAttribute map of unique label for the attribute to the attribute's information.  Can't be null.
     */
    public LearnerStateAttributeCollection(LearnerStateAttributeNameEnum attribute, Map<String, LearnerStateAttribute> labelToAttribute){
        super(attribute, attribute.getAttributeDefaultValue(), attribute.getAttributeDefaultValue(), attribute.getAttributeDefaultValue());
        
        if(labelToAttribute == null){
            throw new IllegalArgumentException("The label to attribute map can't be null.");
        }
        
        this.labelToAttribute = labelToAttribute;
    }
    
    /**
     * Add a new labeled attribute with the same learner state name to this collection.
     * 
     * @param label - must be a label not already in the collection
     * @param attribute - must be the same attribute name this collection represents
     */
    public void addAttribute(String label, LearnerStateAttribute attribute){
        
        if(labelToAttribute.containsKey(label)){
            throw new IllegalArgumentException("There is already an attribute of "+labelToAttribute.get(label)+" assigned to the label of "+label+".");
        }else if(attribute.getName() != this.getName()){
            throw new IllegalArgumentException("The attribute being added is of type "+attribute.getName()+" but this collection is for type "+this.getName()+".");
        }
        
        labelToAttribute.put(label, attribute);
    }
    
    /**
     * Update an existing labeled attribute with the same learner state name to this collection.
     * 
     * @param label - must be a label already in the collection
     * @param attribute - must be the same attribute name this collection represents
     */
    public void updateAttribute(String label, LearnerStateAttribute attribute){
        
        if(!labelToAttribute.containsKey(label)){
            throw new IllegalArgumentException("There is no attribute of "+labelToAttribute.get(label)+" assigned to the label of "+label+" to update. Use 'addAttribute' to add it first.");
        }else if(attribute.getName() != this.getName()){
            throw new IllegalArgumentException("The attribute being updated is of type "+attribute.getName()+" but this collection is for type "+this.getName()+".");
        }
        
        labelToAttribute.put(label, attribute);
    }
    
    /**
     * Return the map of unique label for the attribute to the attribute's information
     * 
     * @return Won't be null.
     */
    public Map<String, LearnerStateAttribute> getAttributes(){
        return labelToAttribute;
    }
    
    /**
     * Return the state attribute from this collection that contains the expertise level for the concept provided.
     * 
     * @param conceptName the name of the concept to get the expertise level for.  If null
     * or empty this method returns null.
     * @return the state attribute with the expertise level value for this concept.  If this collection contains another
     * collection that nested collection is also searched for this concept until all state
     * attribute labels are checked in this instance.  If the concept is found but the
     * value is not an expertise level, null is returned.  If the concept is not found, null is returned.
     */
    public LearnerStateAttribute getConceptExpertiseLevel(String conceptName){
        
        if(StringUtils.isNotBlank(conceptName)){
        
            for(String label : getAttributes().keySet()){
                
                LearnerStateAttribute childStateAttribute = getAttributes().get(label);               
                if(conceptName.equalsIgnoreCase(label)){
                    
                    if(childStateAttribute.getShortTerm() instanceof ExpertiseLevelEnum){
                        return childStateAttribute;
                    }
                }
    
                
                //are there more children to this state attribute that may contain other labeled things
                if(childStateAttribute instanceof LearnerStateAttributeCollection){
                    LearnerStateAttribute descendantStateAttribute = ((LearnerStateAttributeCollection)childStateAttribute).getConceptExpertiseLevel(conceptName);
                    if(descendantStateAttribute != null){
                        return descendantStateAttribute;
                    }
                }
            }
        }
        
        return null;
    }
    
    @Override
    public boolean equals(Object otherLearnerStateAttributeCollection){
        
        if(otherLearnerStateAttributeCollection instanceof LearnerStateAttributeCollection){
            
            LearnerStateAttributeCollection otherCollection = (LearnerStateAttributeCollection)otherLearnerStateAttributeCollection;
            
            //compare maps
            return this.getAttributes().equals(otherCollection.getAttributes());

        }else{
            return false;
        }
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerStateAttributeCollection: ");
        sb.append(super.toString());
        sb.append(", attributes = {");
        for(String label : labelToAttribute.keySet()){
            sb.append(" ").append(label).append(":").append(labelToAttribute.get(label)).append(",");
        }
        
        sb.append("}]");
        
        return sb.toString();
    }
}
