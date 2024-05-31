/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains information about Gateway module interop plugin interface classes and the Domain module condition
 * classes that could analyze training application messages that the interop classes could produce.
 * 
 * @author mhoffman
 *
 */
public class InteropsInfo implements Serializable {
    
    /**
     * default serial version number
     */
    private static final long serialVersionUID = 1L;
    
    /** 
     * mapping of interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface) to
     * information about that interop plugin 
     */
    private Map<String, InteropInfo> interopInfoMap = new HashMap<String, InteropInfo>();
    
    /**
     * Add information about an interop plugin class.
     * 
     * @param interopInfo contains information about an interop plugin class.  If null this method doesn't nothing.  
     * If another interop info exist with the same interop class name it will be replaced with the provided object.
     */
    public void addInteropInfo(InteropInfo interopInfo){
        
        if(interopInfo == null){
            return;
        }
        
        interopInfoMap.put(interopInfo.getInteropClassName(), interopInfo);
    }
    
    /**
     * Return the mapping of interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface) to
     * information about that interop plugin 
     * 
     * @return can be empty but not null.
     */
    public Map<String, InteropInfo> getInteropsInfoMap(){
        return interopInfoMap;
    }
    
    /**
     * Contains information about a Gateway module interop plugin interface class including
     * the Domain module condition classes that can be used because of the messages this
     * interop plugin class produces.
     * 
     * @author mhoffman
     *
     */
    public static class InteropInfo implements Serializable{
        
        /**
         * default serial version number
         */
        private static final long serialVersionUID = 1L;

        /** interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface) */
        private String interopClassName;
        
        /** the GIFT message types this interop plugin class can send to other GIFT modules */
        private List<MessageTypeEnum> producedMessageTypes;
        
        /** information about the domain module condition classes found to consume one or more of the produced message types */
        private Set<ConditionInfo> relevantConditions;
        
        /**
         * Required for GWT serialization.  Don't use.
         */
        @SuppressWarnings("unused")
        private InteropInfo(){ }
        
        /**
         * Set attributes.
         * 
         * @param interopClassName interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface).
         * Can't be null or empty.
         * @param producedMessageTypes the GIFT message types this gateway module interop plugin class can send to other GIFT modules.
         * Can't be null and shouldn't be empty.
         * @param relevantConditions information about the domain module condition classes found to consume one or more of the produced message types.
         * Can't be null and shouldn't be empty. 
         */
        public InteropInfo(String interopClassName, List<MessageTypeEnum> producedMessageTypes, Set<ConditionInfo> relevantConditions){
            
            setInteropClassName(interopClassName);
            setProducedMessageTypes(producedMessageTypes);
            setRelevantConditions(relevantConditions);
        }        

        /**
         * Return the GIFT message types this interop plugin class can send to other GIFT modules
         *  
         * @return won't be null and shouldn't be empty without great consideration of the implications.
         */
        public List<MessageTypeEnum> getProducedMessageTypes() {
            return producedMessageTypes;
        }

        /**
         * Set the gift message types this interop plugin class can send to other GIFT modules
         * 
         * @param producedMessageTypes can't be null.
         */
        private void setProducedMessageTypes(List<MessageTypeEnum> producedMessageTypes) {
            
            if(producedMessageTypes == null){
                throw new IllegalArgumentException("The produced message types is null");
            }
            this.producedMessageTypes = producedMessageTypes;
        }

        /**
         * Return the interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface)
         * 
         * @return won't be null or empty.
         */
        public String getInteropClassName() {
            return interopClassName;
        }

        /**
         * Set the interop class name without the 'mil.arl.gift' prefix (e.g. gateway.interop.dis.DISInterface)
         * 
         * @param interopClassName can't be null or empty
         */
        private void setInteropClassName(String interopClassName) {
            
            if(StringUtils.isBlank(interopClassName)){
                throw new IllegalArgumentException("The interop class name is null or empty");
            }
            this.interopClassName = interopClassName;
        }

        /**
         * Return information about the domain module condition classes found to consume one or more of the produced message types.
         * 
         * @return Can't be null and shouldn't be empty without great consideration of the implications. 
         */
        public Set<ConditionInfo> getRelevantConditions() {
            return relevantConditions;
        }

        /**
         * Set the information about the domain module condition classes found to consume one or more of the produced message types.
         * 
         * @param relevantConditions can't be null
         */
        private void setRelevantConditions(Set<ConditionInfo> relevantConditions) {
            
            if(relevantConditions == null){
                throw new IllegalArgumentException("The relevant conditions list is null");
            }
            this.relevantConditions = relevantConditions;
        }


        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[InteropInfo: interopClassName = ");
            builder.append(interopClassName);
            builder.append(", producedMessageTypes = ");
            builder.append(producedMessageTypes);
            builder.append(", relevantConditions = ");
            builder.append(relevantConditions);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
    
    /**
     * Contains information about a Domain module condition class.
     * 
     * @author mhoffman
     *
     */
    public static class ConditionInfo implements Serializable{
        
        /** The package prefix that should be removed from class names */
        private static final String PACKAGE_PREFIX = "mil.arl.gift.";

        /**
         * default serial version number
         */
        private static final long serialVersionUID = 1L;
        
        /** the name of the condition class */
        private String conditionClassName;
        
        /** a human readable name for the condition */
        private String displayName;
        
        /** a useful description of the condition including its assessment rules and life cycle */
        private String conditionDesc;
          
        /** the gift messages it can consume and assess on */
        private List<MessageTypeEnum> consumedMessageTypes;
        
        /** 
         * unique set of learner actions needed to be shown to the learner for 
         * this condition to assess the learner. E.g. Radio, Spot Report, Start Pace Count buttons on the TUI.
         */
        private Set<generated.dkf.LearnerActionEnumType> neededLearnerActionTypes;
        
        /**
         * Required for GWT serialization.  Don't use.
         */
        @SuppressWarnings("unused")
        private ConditionInfo(){ }
        
        /**
         * Set attributes
         * 
         * @param conditionClass the condition class reference. Can't be null.
         * @param displayName a human readable name for the condition. Can't be null or empty.
         * @param conditionDesc a useful description of the condition including its assessment rules and life cycle.
         * Can't be null or empty. Can contain HTML.
         * @param consumedMessageTypes the gift messages it can consume and assess on.  Can't be null or empty.
         * @param neededLearnerActionTypes unique set of learner actions needed to be shown to the learner for 
         * this condition to assess the learner. E.g. Radio, Spot Report, Start Pace Count buttons on the TUI.
         * Can be null or empty.
         */
        public ConditionInfo(Class<?> conditionClass, String displayName, String conditionDesc,
                List<MessageTypeEnum> consumedMessageTypes, Set<generated.dkf.LearnerActionEnumType> neededLearnerActionTypes){
            
            setConditionClass(conditionClass.getName());
            setDisplayName(displayName);
            setConditionDesc(conditionDesc);
            setConsumedMessageTypes(consumedMessageTypes);
            setNeededLearnerActionTypes(neededLearnerActionTypes);
        }
        
        @Override
        public boolean equals(Object otherConditionInfo){
            
            return otherConditionInfo != null && otherConditionInfo instanceof ConditionInfo &&
                    ((ConditionInfo)otherConditionInfo).getConditionClass().equals(this.getConditionClass()) &&
                    ((ConditionInfo)otherConditionInfo).getConditionDesc().equals(this.getConditionDesc()) &&
                    ((ConditionInfo)otherConditionInfo).getConsumedMessageTypes().equals(this.getConsumedMessageTypes()) &&
                    ((ConditionInfo)otherConditionInfo).getDisplayName().equals(this.getDisplayName());
        }
        
        @Override
        public int hashCode(){
            
            int prime = 31;
            int result = 1;
            
            result = prime * result + conditionClassName.hashCode();
            
            return result;
        }

        /**
         * Return the condition class reference
         * 
         * @return won't be null or empty.
         */
        public String getConditionClass() {
            return conditionClassName;
        }

        /**
         * Sets the name of the condition class that the {@link ConditionInfo}
         * object represents
         * 
         * @param conditionClassName The fully qualified name of the class that
         *        is being represented. Can't be null or empty.
         */
        private void setConditionClass(String conditionClassName) {
            if (StringUtils.isBlank(conditionClassName)) {
                throw new IllegalArgumentException("The condition class name is null or empty");
            }

            if (conditionClassName.startsWith(PACKAGE_PREFIX)) {
                conditionClassName = conditionClassName.substring(PACKAGE_PREFIX.length());
            }

            this.conditionClassName = conditionClassName;
        }

        /**
         * Return a human readable name for the condition
         * 
         * @return won't be null or empty.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Set a human readable name for the condition
         * 
         * @param displayName can't be null or empty.
         */
        private void setDisplayName(String displayName) {
            
            if(StringUtils.isBlank(displayName)){
                throw new IllegalArgumentException("The display name is null or empty");
            }
            this.displayName = displayName;
        }

        /**
         * Return a useful description of the condition including its assessment rules and life cycle.
         * 
         * @return won't be null or empty.  Can contain HTML.
         */
        public String getConditionDesc() {
            return conditionDesc;
        }

        /**
         * Set a useful description of the condition including its assessment rules and life cycle.
         * 
         * @param conditionDesc can't be null or empty.  Can contain HTML.
         */
        private void setConditionDesc(String conditionDesc) {
            
            if(StringUtils.isBlank(conditionDesc)){
                throw new IllegalArgumentException("The condition description is null or empty");
            }
            this.conditionDesc = conditionDesc;
        }

        /**
         * Return the gift messages it can consume and assess on.  
         * 
         * @return won't be null.
         */
        public List<MessageTypeEnum> getConsumedMessageTypes() {
            return consumedMessageTypes;
        }

        /**
         * Set the gift messages it can consume and assess on.
         * 
         * @param consumedMessageTypes can't be null.
         */
        private void setConsumedMessageTypes(List<MessageTypeEnum> consumedMessageTypes) {
            
            if(consumedMessageTypes == null){
                throw new IllegalArgumentException("The consumed message types is null");
            }
            this.consumedMessageTypes = consumedMessageTypes;
        }
        
        /**
         * Return the unique set of learner actions needed to be shown to the learner for 
         * this condition to assess the learner. E.g. Radio, Spot Report, Start Pace Count buttons on the TUI.
         * @return can be null or empty.
         */
        public Set<generated.dkf.LearnerActionEnumType> getNeededLearnerActionTypes(){
            return neededLearnerActionTypes;
        }
        
        /**
         * Set unique set of learner actions needed to be shown to the learner for 
         * this condition to assess the learner. E.g. Radio, Spot Report, Start Pace Count buttons on the TUI.
         * @param neededLearnerActionTypes can be null or empty.
         */
        private void setNeededLearnerActionTypes(Set<generated.dkf.LearnerActionEnumType> neededLearnerActionTypes){
            this.neededLearnerActionTypes = neededLearnerActionTypes;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConditionInfo: conditionClass = ");
            builder.append(conditionClassName);
            builder.append(", displayName = ");
            builder.append(displayName);
            builder.append(", conditionDesc = ");
            builder.append(conditionDesc);
            builder.append(", consumedMessageTypes = ");
            builder.append(consumedMessageTypes);
            builder.append(", neededLearnerActionTypes = ");
            builder.append(neededLearnerActionTypes);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
}
