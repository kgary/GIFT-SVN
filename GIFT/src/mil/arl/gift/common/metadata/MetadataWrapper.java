/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metadata;

import java.io.Serializable;

/**
 * Contains information about a metadata file.
 * 
 * @author mhoffman
 *
 */
public class MetadataWrapper implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** name to display to the author for this metadata file */
    private String displayName;
    
    /** metadata file name including the path */
    private String metadataFileName;
    
    /** whether the metadata is tagged with remediation only */
    private boolean remediationOnly;
    
    /** whether the metadata has extraneous concepts when compared to some request for metadata */
    private boolean extraneousConcept;
    
    /** the unmarshalled metadata file content */
    private generated.metadata.Metadata metadata;
    
    /** 
     * the type of content the metadata is describing
     * Can be null if the type can't be determined or when it isn't needed (e.g. deleting metadata)
     */
    private ContentTypeEnum contentType;
    
    /**
     * Enumeration for the different types of content that GIFT supports creating metadata for in
     * the authoring tool and GIFT xml schemas (xsd).
     * @author mhoffman
     *
     */
    public enum ContentTypeEnum{
        
        SLIDE_SHOW,
        POWERPOINT,
        PDF,
        LOCAL_WEBPAGE,
        LOCAL_IMAGE,
        WEB_ADDRESS,
        YOUTUBE_VIDEO,
        LOCAL_VIDEO,
        LTI_PROVIDER,
        HIGHLIGHT_PASSAGE,
        CONVERSATION_TREE,
        SUMMARIZE_PASSAGE,
        VIRTUAL_BATTLESPACE,
        TC3,
        DE_TESTBED,
        ARES,
        VR_ENGAGE,
        UNITY,
        HAVEN,
        RIDE,
        DEMO_APPLICATION
        
    }
    
    /**
     * Default constructor needed for GWT.  Should not use.
     */
    @SuppressWarnings("unused")
    private MetadataWrapper(){}
    
    /**
     * Set attributes.
     * 
     * @param metadataFileName metadata file name including the path
     * @param displayName name to display to the author for this metadata file
     * @param remediationOnly whether the metadata is tagged with remediation only
     * @param extraneousConcept whether the metadata has extraneous concepts when compared to some request for metadata
     * @param contentType the type of content the metadata is describing
     * Can be null if the type can't be determined or when it isn't needed (e.g. deleting metadata)
     */
    public MetadataWrapper(String metadataFileName, String displayName, boolean remediationOnly, boolean extraneousConcept, ContentTypeEnum contentType){
        
        setMetadataFileName(metadataFileName);
        setDisplayName(displayName);
        setIsRemediationOnly(remediationOnly);
        setHasExtraneousConcept(extraneousConcept);
        setContentType(contentType);
    }    

    /**
     * Return the type of content the metadata is describing
     * @return Can be null if the type can't be determined or when it isn't needed (e.g. deleting metadata)
     */
    public ContentTypeEnum getContentType() {
        return contentType;
    }

    /**
     * Set the type of content the metadata is describing
     * @param contentType Can be null if the type can't be determined or when it isn't needed (e.g. deleting metadata)
     */
    private void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }

    /**
     * Return the unmarshalled metadata file content
     * @return can be null.
     */
    public generated.metadata.Metadata getMetadata() {
        return metadata;
    }

    /**
     * Set the unmarshalled metadata file content
     * 
     * @param metadata can be null
     */
    public void setMetadata(generated.metadata.Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Return the name to display to the author for this metadata file
     *  
     * @return display name of the metadata file
     */
    public String getDisplayName() {
        return displayName;
    }

    private void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the metadata file name including the path
     * 
     * @return name of the metadata file
     */
    public String getMetadataFileName() {
        return metadataFileName;
    }

    private void setMetadataFileName(String metadataFileName) {
        this.metadataFileName = metadataFileName;
    }

    /**
     * Return whether the metadata is tagged with remediation only
     * 
     * @return true iff the metadata contains the remediation only element and the value is true
     */
    public boolean isRemediationOnly() {
        return remediationOnly;
    }

    private void setIsRemediationOnly(boolean remediationOnly) {
        this.remediationOnly = remediationOnly;
    }

    /**
     * Return whether the metadata has extraneous concepts when compared to some request for metadata
     * 
     * @return whether the metadata object contains extraneous concepts.  Default is false.
     */
    public boolean hasExtraneousConcept() {
        return extraneousConcept;
    }

    /**
     * Set whether the metadata has extraneous concepts when compared to some request for metadata
     * 
     * @param extraneousConcept true if the metadata associated with this object is a search result
     * based on using extraneous course concepts
     */
    public void setHasExtraneousConcept(boolean extraneousConcept) {
        this.extraneousConcept = extraneousConcept;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[MetadataWrapper: displayName = ");
        builder.append(displayName);
        builder.append(", contentType = ");
        builder.append(contentType);
        builder.append(", metadataFileName = ");
        builder.append(metadataFileName);
        builder.append(", remediationOnly = ");
        builder.append(remediationOnly);
        builder.append(", extraneousConcept = ");
        builder.append(extraneousConcept);
        builder.append(", metadata = ");
        builder.append(metadata);
        builder.append("]");
        return builder.toString();
    }

    
}
