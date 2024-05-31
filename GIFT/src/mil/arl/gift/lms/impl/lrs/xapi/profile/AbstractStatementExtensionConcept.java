package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation for Extension xAPI Profile Components which have a recommended verb.
 *  
 * @author Yet Analytics
 *
 */
public abstract class AbstractStatementExtensionConcept extends AbstractExtensionConcept implements ExtensionConceptStatement {

    /**
     * An array of verb URIs that this extension is recommended for use with (extending to narrower of the same).
     */
    private List<URI> recommendedVerbs;
    
    /**
     * Parse fields from SPARQL query result
     * 
     * @param id - id of the extension concept
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractStatementExtensionConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        for(String recVerb : src.getRecommendedVerbs()) {
            addRecommendedVerb(toURI(recVerb));
        }
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
    protected static SparqlResult statementExtensionSparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createStatementExtensionQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute Statement Extension SPARQL query!", e);
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
    protected static SparqlResult statementExtensionSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchConcepts(id);
        } else {
            return statementExtensionSparqlQuery(id);
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
    protected static SparqlResult giftStatementExtensionSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchGiftConcepts(id);
        } else {
            return statementExtensionSparqlQuery(id);
        }
    }
    
    /**
     * @return existing recommended verbs if not null, empty ArrayList<URI> otherwise
     */
    protected List<URI> existingRecommendedVerbs(){
        return recommendedVerbs != null ? recommendedVerbs : new ArrayList<URI>();
    }
    
    /**
     * @return recommendedVerbs
     */
    public List<URI> getRecommendedVerbs(){
        return recommendedVerbs;
    }

    /**
     * Setter for recommendedVerbs
     * 
     * @param recommendedVerbs - collection of recommended verb URIs
     */
    private void setRecommendedVerbs(List<URI> recommendedVerbs) {
        if(recommendedVerbs == null) {
            throw new IllegalArgumentException("recommendedVerbs can not be null!");
        }
        this.recommendedVerbs = recommendedVerbs;
    }

    /**
     * Add verb as a recommended verb
     * 
     * @param verb - Verb to add to list of recommended verbs
     */
    public void addRecommendedVerb(Verb verb) {
        addRecommendedVerb(verb.getId());   
    }

    /**
     * Add verb to existing collection of recommended verbs
     * 
     * @param verbId - Verb URI id
     */
    public void addRecommendedVerb(URI verbId) {
        List<URI> coll = existingRecommendedVerbs();
        coll.add(verbId);
        setRecommendedVerbs(coll);
    }

    /**
     * Is the verb found within the recommended verbs
     * 
     * @param verb - verb to check
     * 
     * @return true if found within recommended verbs, false otherwise
     */
    public boolean isRecommendedVerb(Verb verb) {
        return isRecommended(verb.getId(), getRecommendedVerbs());
    }
}
