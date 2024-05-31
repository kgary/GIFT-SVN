package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation for Document Resource xAPI Profile components.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractDocumentResourceConcept extends AbstractConceptLinked {

    /**
     * The media type for the resource, as described in RFC 2046 (e.g. application/json).
     */
    private String contentType;
    
    /**
     * Parses content type from SPARQL query result
     * 
     * @param id - identifier of the xAPI Profile component
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractDocumentResourceConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        if(src.getContentType() != null) {
            setContentType(src.getContentType());
        }
    }
    
    /**
     * Getter for 'contentType'
     * 
     * @return The media type for the resource
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Setter for 'contentType'
     * 
     * @param contentType - The media type for the resource
     */
    private void setContentType(String contentType) {
        if(contentType == null) {
            throw new IllegalArgumentException("contentType can not be null!");
        }
        this.contentType = contentType;
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result
     * 
     * @param id - identifier for xAPI Profile component
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult documentResourceSparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createDocumentResourceQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute document resource SPARQL query!", e);
            }
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchConcepts(id);
        } else {
            throw new LmsXapiProfileException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When the local client is
     * used, all concepts within xAPI Profiles found within LMS configuration folder are searched.
     * 
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult documentResourceSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchConcepts(id);
        } else {
            return documentResourceSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When the local client is
     * used, concepts within GIFT xAPI Profiles are searched.
     * 
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult giftDocumentResourceSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchGiftConcepts(id);
        } else {
            return documentResourceSparqlQuery(id);
        }
    }
}
