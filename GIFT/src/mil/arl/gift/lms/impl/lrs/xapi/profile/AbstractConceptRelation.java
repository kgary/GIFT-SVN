package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.List;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation for xAPI Profile components which have relations to other
 * components within the same xAPI Profile or external xAPI Profile(s).
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractConceptRelation extends AbstractProfileComponentDescribed {

    /**
     * An array of IRIs of Concepts of the same type from this Profile version that have a broader meaning.
     */
    private List<URI> broader;
    
    /**
     * An array of IRIs of Concepts of the same type from a different Profile that have a broader meaning.
     */
    private List<URI> broadMatch;
    
    /**
     * An array of IRIs of Concepts of the same type from this Profile version that have a narrower meaning.
     */
    private List<URI> narrower;
    
    /**
     * An array of IRIs of Concepts of the same type from different Profiles that have narrower meanings.
     */
    private List<URI> narrowMatch;
    
    /**
     * An array of IRIs of Concepts of the same type from this Profile version that are close to this Concept's meaning.
     */
    private List<URI> related;
    
    /**
     * An array of IRIs of Concepts of the same type from a different Profile or a different version of the same Profile 
     * that has a related meaning that is not clearly narrower or broader. 
     * 
     * Useful to establish conceptual links between Profiles that can be used for discovery.
     */
    private List<URI> relatedMatch;
    
    /**
     * An array of IRIs of Concepts of the same type from a different Profile or a different version of the same Profile 
     * that have exactly the same meaning.
     */
    private List<URI> exactMatch;
    
    /**
     * Parses broader, broadMatch, narrower, narrowMatch, related, relatedMatch, exactMatch from SPARQL query result
     * 
     * @param id - identifier of the xAPI Profile component
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractConceptRelation(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        for(String broader : src.getBroader()) {
            addBroader(broader);
        }
        for(String broadMatch : src.getBroadMatch()) {
            addBroadMatch(broadMatch);
        }
        for(String narrower : src.getNarrower()) {
            addNarrower(narrower);
        }
        for(String narrowMatch : src.getNarrowMatch()) {
            addNarrowMatch(narrowMatch);
        }
        for(String related : src.getRelated()) {
            addRelated(related);
        }
        for(String relatedMatch : src.getRelatedMatch()) {
            addRelatedMatch(relatedMatch);
        }
        for(String exactMatch : src.getExactMatch()) {
            addExactMatch(exactMatch);
        }
    }
    
    /**
     * Existing 'broader' or new ArrayList to use as 'broader'
     * 
     * @return Possibly empty ArrayList of 'broader' URIs
     */
    private List<URI> existingBroader(){
        return existingOrNovel(broader);
    }
    
    /**
     * Existing 'broadMatch' or new ArrayList to use as 'broadMatch'
     * 
     * @return Possibly empty ArrayList of 'broadMatch' URIs
     */
    private List<URI> existingBroadMatch(){
        return existingOrNovel(broadMatch);
    }
    
    /**
     * Existing 'narrower' or new ArrayList to use as 'narrower'
     * 
     * @return Possibly empty ArrayList of 'narrower' URIs
     */
    private List<URI> existingNarrower(){
        return existingOrNovel(narrower);
    }
    
    /**
     * Existing 'narrowMatch' or new ArrayList to use as 'narrowMatch'
     * 
     * @return Possibly empty ArrayList of 'narrowMatch' URIs
     */
    private List<URI> existingNarrowMatch(){
        return existingOrNovel(narrowMatch);
    }
    
    /**
     * Existing 'related' or new ArrayList to use as 'related'
     * 
     * @return Possibly empty ArrayList of 'related' URIs
     */
    private List<URI> existingRelated(){
        return existingOrNovel(related);
    }
    
    /**
     * Existing 'relatedMatch' or new ArrayList to use as 'relatedMatch'
     * 
     * @return Possibly empty ArrayList of 'relatedMatch' URIs
     */
    private List<URI> existingRelatedMatch(){
        return existingOrNovel(relatedMatch);
    }
    
    /**
     * Existing 'exactMatch' or new ArrayList to use as 'exactMatch'
     * 
     * @return Possibly empty ArrayList of 'exactMatch' URIs
     */
    private List<URI> existingExactMatch(){
        return existingOrNovel(exactMatch);
    }
    
    /**
     * Getter for 'broader'
     * 
     * @return An array of IRIs of Concepts of the same type from this Profile version that have a broader meaning
     */
    public List<URI> getBroader() {
        return broader;
    }
    
    /**
     * Setter for 'broader'
     * 
     * @param broader - An array of IRIs of Concepts of the same type from this Profile version that have a broader meaning
     */
    private void setBroader(List<URI> broader) {
        if(broader == null) {
            throw new IllegalArgumentException("broader can not be null!");
        }
        this.broader = broader;
    }
    
    /**
     * Adds 'conceptId' to 'broader'
     * 
     * @param conceptId - identifier for concept to add to broader array
     */
    public void addBroader(String conceptId) throws LmsXapiProfileException {
        addBroader(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'broader'
     * 
     * @param conceptId - id of the broader concept
     */
    public void addBroader(URI conceptId) {
        List<URI> coll = existingBroader();
        coll.add(conceptId);
        setBroader(coll);
    }
    
    /**
     * Getter for 'broadMatch'
     * 
     * @return An array of IRIs of Concepts of the same type from a different Profile that have a broader meaning
     */
    public List<URI> getBroadMatch(){
        return broadMatch;
    }
    
    /**
     * Setter for 'broadMatch'
     * 
     * @param broadMatch - An array of IRIs of Concepts of the same type from a different Profile that have a broader meaning
     */
    private void setBroadMatch(List<URI> broadMatch) {
        if(broadMatch == null) {
            throw new IllegalArgumentException("broadMatch can not be null!");
        }
        this.broadMatch = broadMatch;
    }
    
    /**
     * Adds 'conceptId' to 'broadMatch'
     * 
     * @param conceptId - id of the broad match concept
     */
    public void addBroadMatch(String conceptId) throws LmsXapiProfileException {
        addBroadMatch(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'broadMatch'
     * 
     * @param conceptId - id of the broad match concept
     */
    public void addBroadMatch(URI conceptId) {
        List<URI> coll = existingBroadMatch();
        coll.add(conceptId);
        setBroadMatch(coll);
    }
    
    /**
     * Getter for 'narrower'
     * 
     * @return An array of IRIs of Concepts of the same type from this Profile version that have a narrower meaning
     */
    public List<URI> getNarrower(){
        return narrower;
    }
    
    /**
     * Setter for 'narrower'
     * 
     * @param narrower - An array of IRIs of Concepts of the same type from this Profile version that have a narrower meaning
     */
    private void setNarrower(List<URI> narrower) {
        if(narrower == null) {
            throw new IllegalArgumentException("narrower can not be null!");
        }
        this.narrower = narrower;
    }
    
    /**
     * Adds 'conceptId' to 'narrower'
     * 
     * @param conceptId - id of the narrower concept
     */
    public void addNarrower(String conceptId) throws LmsXapiProfileException {
        addNarrower(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'narrower'
     * 
     * @param conceptId - id of the narrower concept
     */
    public void addNarrower(URI conceptId) {
        List<URI> coll = existingNarrower();
        coll.add(conceptId);
        setNarrower(coll);
    }
    
    /**
     * Getter for 'narrowMatch'
     * 
     * @return An array of IRIs of Concepts of the same type from different Profiles that have narrower meanings
     */
    public List<URI> getNarrowMatch(){
        return narrowMatch;
    }
    
    /**
     * Setter for 'narrowMatch'
     * 
     * @param narrowMatch - An array of IRIs of Concepts of the same type from different Profiles that have narrower meanings
     */
    private void setNarrowMatch(List<URI> narrowMatch) {
        if(narrowMatch == null) {
            throw new IllegalArgumentException("narrowMatch can not be null!");
        }
        this.narrowMatch = narrowMatch;
    }
    
    /**
     * Adds 'conceptId' to 'narrowMatch'
     * 
     * @param conceptId - id of the narrow match concept
     */
    public void addNarrowMatch(String conceptId) throws LmsXapiProfileException {
        addNarrowMatch(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'narrowMatch'
     * 
     * @param conceptId - id of the narrow match concept
     */
    public void addNarrowMatch(URI conceptId) {
        List<URI> coll = existingNarrowMatch();
        coll.add(conceptId);
        setNarrowMatch(coll);
    }
    
    /**
     * Getter for 'related'
     * 
     * @return An array of IRIs of Concepts of the same type from this Profile version that are close to this Concept's meaning
     */
    public List<URI> getRelated(){
        return related;
    }
    
    /**
     * Setter for 'related'
     * 
     * @param related - An array of IRIs of Concepts of the same type from this Profile version that are close to this Concept's meaning
     */
    private void setRelated(List<URI> related) {
        if(related == null) {
            throw new IllegalArgumentException("related can not be null!");
        }
        this.related = related;
    }
    
    /**
     * Adds 'conceptId' to 'related'
     * 
     * @param conceptId - id of the related concept
     */
    public void addRelated(String conceptId) throws LmsXapiProfileException {
        addRelated(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'related'
     * 
     * @param conceptId - id of the related concept
     */
    public void addRelated(URI conceptId) {
        List<URI> coll = existingRelated();
        coll.add(conceptId);
        setRelated(coll);
    }
    
    /**
     * Getter for 'relatedMatch'
     * 
     * @return An array of IRIs of Concepts of the same type from a different Profile or a different version
     *         of the same Profile that has a related meaning that is not clearly narrower or broader
     */
    public List<URI> getRelatedMatch(){
        return relatedMatch;
    }
    
    /**
     * Setter for 'relatedMatch'
     * 
     * @param relatedMatch - An array of IRIs of Concepts of the same type from a different Profile or a different version
     *                       of the same Profile that has a related meaning that is not clearly narrower or broader
     */
    private void setRelatedMatch(List<URI> relatedMatch) {
        if(relatedMatch == null) {
            throw new IllegalArgumentException("relatedMatch can not be null!");
        }
        this.relatedMatch = relatedMatch;
    }
    
    /**
     * Adds 'conceptId' to 'relatedMatch'
     * 
     * @param conceptId - id for the related match concept
     */
    public void addRelatedMatch(String conceptId) throws LmsXapiProfileException {
        addRelatedMatch(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'relatedMatch'
     * 
     * @param conceptId - id for the related match concept
     */
    public void addRelatedMatch(URI conceptId) {
        List<URI> coll = existingRelatedMatch();
        coll.add(conceptId);
        setRelatedMatch(coll);
    }
    
    /**
     * Getter for 'exactMatch'
     * 
     * @return An array of IRIs of Concepts of the same type from a different Profile
     *         or a different version of the same Profile that have exactly the same meaning
     */
    public List<URI> getExactMatch(){
        return exactMatch;
    }
    
    /**
     * Setter for 'exactMatch'
     * 
     * @param exactMatch - An array of IRIs of Concepts of the same type from a different Profile
     *                     or a different version of the same Profile that have exactly the same meaning
     */
    private void setExactMatch(List<URI> exactMatch) {
        if(exactMatch == null) {
            throw new IllegalArgumentException("exactMatch can not be null!");
        }
        this.exactMatch = exactMatch;
    }
    
    /**
     * Adds 'conceptId' to 'exactMatch'
     * 
     * @param conceptId - id for the exact match concept
     */
    public void addExactMatch(String conceptId) throws LmsXapiProfileException {
        addExactMatch(toURI(conceptId));
    }
    
    /**
     * Adds 'conceptId' to 'exactMatch'
     * 
     * @param conceptId - id for the exact match concept
     */
    public void addExactMatch(URI conceptId) {
        List<URI> coll = existingExactMatch();
        coll.add(conceptId);
        setExactMatch(coll);
    }
}
