package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import com.rusticisoftware.tincan.Attachment;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of an Attachment Usage Type Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class AttachmentUsageTypeConcept extends AbstractConceptRelation {
    
    /**
     * Set Attachment Usage Type Concept fields from SPARQL query result
     * 
     * @param id - identifier for Attachment Usage Type
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public AttachmentUsageTypeConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }

    /**
     * Convert from AttachmentUsageType concept to Attachment Usage Type
     * 
     * @return the URI for the Attachment Usage Type
     */
    public URI asAttachmentUsageType() {
        return getId();
    }

    /**
     * Sets UsageType within the Attachment to be the id of this AttachmentUsageType Concept
     * 
     * @param attachment - the attachment to update
     */
    public void addToAttachment(Attachment attachment) {
        if(attachment == null) {
            throw new IllegalArgumentException("attachment can not be null!");
        }
        attachment.setUsageType(asAttachmentUsageType());
    }
}
