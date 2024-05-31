/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf;

import java.io.File;

import mil.arl.gift.common.util.StringUtils;

/**
 * Used for different condition description types
 * 
 * @author mhoffman
 *
 */
public abstract class ConditionDescription {
    
    /** a display name for the condition */
    private String displayName;
    
    /**
     * Set attribute(s)
     * 
     * @param displayName the display name of the condition.  Shouldn't include 'condition' in the name.  Can't
     * be null or empty.
     */
    public ConditionDescription(String displayName){
        setDisplayName(displayName);
    }
    
    /**
     * Set the display name for the condition
     * 
     * @param displayName can't be null or empty
     */
    private void setDisplayName(String displayName){
        
        if(StringUtils.isBlank(displayName)){
            throw new IllegalArgumentException("The display name can't be null or empty.");
        }
        this.displayName = displayName;
    }
    
    /**
     * Return the display name for this condition.
     * 
     * @return Shouldn't include 'condition' in the name.  Won't
     * be null or empty.
     */
    public String getDisplayName(){
        return displayName;
    }

    /**
     * The basic string whose value is a domain condition description.
     * 
     * @author mhoffman
     *
     */
    public static class InlineDescription extends ConditionDescription{
        
        /** the description for a condition */
        private String description;
        
        /**
         * Set the generic description for a domain condition
         * 
         * @param description the generic description for a domain condition.  Can't be null or empty. Supports HTML syntax. 
         * @param displayName the display name of the condition.  Recommended to not include 'condition' in the name.  Can't
         * be null or empty.
         */
        public InlineDescription(String description, String displayName){
            super(displayName);
            setDescription(description);
        }
        
        /**
         * Gets the generic description for a domain condition
         * 
         * @return the description for a domain condition.  Will be null if the url has been set instead. Supports HTML syntax. 
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the generic description for a domain condition
         * 
         * @param description the generic description for a domain condition.  Can't be null or empty. Supports HTML syntax. 
         */
        private void setDescription(String description) {
            
            if(StringUtils.isBlank(description)){
                throw new IllegalArgumentException("The description can't be null or empty.");
            }
            this.description = description;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[InlineDescription: description=");
            builder.append(description);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
    
    /**
     * A reference to a file which contains a domain condition description
     * 
     * @author mhoffman
     *
     */
    public static class FileDescription extends ConditionDescription{
        
        /** a file containing the description for a condition */
        private File file;
        
        /**
         * Set the file which contains the condition description
         * 
         * @param file contains the condition description.  Can't be null and must exist.
         * @param displayName the display name of the condition.  Recommended to not include 'condition' in the name.  Can't
         * be null or empty.
         */
        public FileDescription(File file, String displayName){
            super(displayName);
            setFile(file);
        }
        
        /**
         * Gets the file that contains the generic description for a domain condition
         * 
         * @return the file that contains the condition description.  Won't be null and will exist.
         */
        public File getFile() {
            return file;
        }

        /**
         * Sets the generic description for a domain condition
         * 
         * @param file contains the condition description.  Can't be null and must exist.
         */
        private void setFile(File file) {
            
            if(file == null){
                throw new IllegalArgumentException("The file can't be null.");
            }else if(!file.exists()){
                throw new IllegalArgumentException("The file '"+file.getAbsolutePath()+"' doesn't exist.");
            }
            this.file = file;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[FileDescription: file=");
            builder.append(file);
            builder.append("]");
            return builder.toString();
        }
    }
}
