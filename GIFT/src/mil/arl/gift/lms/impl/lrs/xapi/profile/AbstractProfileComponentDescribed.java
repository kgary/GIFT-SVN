package mil.arl.gift.lms.impl.lrs.xapi.profile;

import com.rusticisoftware.tincan.LanguageMap;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.LanguageTagEnum;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation of xAPI Profile components which have a prefLabel and definition.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractProfileComponentDescribed extends AbstractProfileComponent {

    /** Language Map containing label (in possibly multiple languages) for this Concept */
    private LanguageMap prefLabel;
    /** Language Map containing descriptive text (in possibly multiple languages) for this Concept */
    private LanguageMap definition;
    
    /**
     * Parses prefLabel and definition from SPARQL query result
     * 
     * @param id - identifier of the xAPI Profile component
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractProfileComponentDescribed(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        if(src.getPrefLabel() != null && src.getPrefLabelLang() != null) {
            setPrefLabel(LanguageTagEnum.valueOf(toScreamingSnakeCase(src.getPrefLabelLang())), src.getPrefLabel());
        }
        if(src.getDefinition() != null && src.getDefinitionLang() != null) {
            setDefinition(LanguageTagEnum.valueOf(toScreamingSnakeCase(src.getDefinitionLang())), src.getDefinition());
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
    protected static SparqlResult conceptRelationSparqlQuery(String id) throws LmsXapiProfileException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            try {
                data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createConceptRelationQuery(id));
            } catch (LmsXapiSparqlException e) {
                throw new LmsXapiProfileException("Unable to execute SPARQL concept relation query!", e);
            }
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchConcepts(id);
        } else {
            throw new LmsXapiProfileException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, all concepts are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult conceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, concepts within the ADL xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult adlConceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchAdlConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, concepts within the TLA xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult tlaConceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchMomConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, concepts within the GIFT xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult giftConceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchGiftConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, concepts within the TinCan xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult tincanConceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchTincanConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Construct SPARQL query string for id, execute query and parse result. When executing query against
     * local profile server client, concepts within the SCORM xAPI Profile are searched.
     *  
     * @param id - identifier for xAPI Profile component
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to execute SPARQL query
     */
    protected static SparqlResult scormConceptRelationSparqlQuery(String id, boolean forceLocal) throws LmsXapiProfileException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchScormConcepts(id);
        } else {
            return conceptRelationSparqlQuery(id);
        }
    }
    
    /**
     * Determines appropriate LanguageMap to return for prefLabel
     * 
     * @return existing LanguageMap or new instance of one
     */
    private LanguageMap existingPrefLabel() {
        return prefLabel != null ? prefLabel : new LanguageMap();
    }
    
    /**
     * Determines appropriate LanguageMap to return for definition
     * 
     * @return existing LanguageMap or new instance of one
     */
    private LanguageMap existingDefinition() {
        return definition != null ? definition : new LanguageMap();
    }
    
    /**
     * Getter for prefLabel
     * 
     * @return LanguageMap set as prefLabel
     */
    public LanguageMap getPrefLabel() {
        return prefLabel;
    }
    
    /**
     * Getter for United States English prefLabel
     * 
     * @return String found at 'en-US' within prefLabel LanguageMap 
     */
    public String getEnglishPrefLabel() {
        return getPrefLabel().get(LanguageTagEnum.EN_US.getValue());
    }
    
    /**
     * Setter for prefLabel
     * 
     * @param languageTag - Language tag within Language Map
     * @param label - label within Language Map
     */
    private void setPrefLabel(LanguageTagEnum languageTag, String label) {
        if(languageTag == null || label == null) {
            throw new IllegalArgumentException("languageTag and label can not be null!");
        }
        LanguageMap lmap = existingPrefLabel();
        lmap.put(languageTag.getValue(), label);
        this.prefLabel = lmap;
    }
    
    /**
     * Getter for definition
     * 
     * @return LanguageMap set as definition
     */
    public LanguageMap getDefinition() {
        return definition;
    }
    
    /**
     * Getter for United States English definition
     * 
     * @return String found at 'en-US' within definition LanguageMap
     */
    public String getEnglishDefinition() {
        return getDefinition().get(LanguageTagEnum.EN_US.getValue());
    }
    
    /**
     * Setter for definition
     * 
     * @param languageTag - Language tag within Language Map
     * @param label - label within Language Map
     */
    private void setDefinition(LanguageTagEnum languageTag, String definition) {
        if(languageTag == null || definition == null) {
            throw new IllegalArgumentException("languageTag and definition can not be null!");
        }
        LanguageMap lmap = existingDefinition();
        lmap.put(languageTag.getValue(), definition);
        this.definition = lmap;
    }
}
