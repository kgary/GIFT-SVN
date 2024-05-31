package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import mil.arl.gift.lms.impl.lrs.xapi.IdHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.Client;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQuery;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation of properties common to xAPI Profile components.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractProfileComponent {
    
    /** URI identifier for the Concept */
    private URI id;
    
    /** ProfileComponentTypeEnum that represents the Concept's type */
    private ProfileComponentTypeEnum type;
    
    /** The URI of the specific Profile version this Concept is defined within */
    private URI inScheme;
    
    /** Is this Concept current? */
    private boolean deprecated;
    
    /** Instance of the Profile Server Client */
    protected static Client profileServer = Client.getClient();
    
    /**
     * Helper method for String -> URI
     * 
     * @param uri - string to convert to URI
     * 
     * @return string as URI
     * 
     * @throws LmsXapiProfileException when unable to form URI from string
     */
    protected static URI toURI(String uri) throws LmsXapiProfileException {
        if(uri == null) {
            throw new IllegalArgumentException("uri can not be null!");
        }
        try {
            return IdHelper.toURI(uri);
        } catch (URISyntaxException e) {
            throw new LmsXapiProfileException("Unable to convert string to URI!", e);
        }
    }
    
    /**
     * null safe return of a collection of arbitrary type
     * 
     * @param <T> - type of the collection
     * @param existing - possibly null collection
     * 
     * @return passed in argument when non-null, new array list of T otherwise
     */
    protected static <T> List<T> existingOrNovel(List<T> existing){
        return existing != null ? existing : new ArrayList<T>();
    }
    
    /**
     * Constructor that checks to see if Lrs's connect method has been called.
     * 
     * If it has not, only the localClient will be non-null, otherwise both the
     * local and live clients are non-null.
     */
    private AbstractProfileComponent() {
        if(profileServer.getLiveClient() == null) {
            // Attempt to resolve Client with live profile server
            // -> live client non-null after Lrs.connect called
            profileServer = Client.getClient();
        }
    }
    
    /**
     * convert string to screaming snake case
     * 
     * @param s - string to convert
     * 
     * @return converted string
     */
    protected static String toScreamingSnakeCase(String s) {
        if(s == null) {
            throw new IllegalArgumentException("string can not be null!");
        }
        StringBuilder builder = new StringBuilder();
        char[] nameChars = s.toCharArray();
        for (int i = 0; i < nameChars.length; i++) {
            char ch = nameChars[i];
            if (i != 0 && Character.isUpperCase(ch)) {
                builder.append('_').append(ch);
            } else {
                builder.append(Character.toUpperCase(ch));
            }
        }
        return builder.toString();
    }
    
    /**
     * Parses the type field from SparqlResult and determines corresponding profile component type
     * 
     * @param src - SPARQL query result
     * 
     * @return profile component type enumeration corresponding to the type
     * 
     * @throws LmsXapiProfileException when the SPARQL query result doesn't contain the type field
     */
    public static ProfileComponentTypeEnum parseSparqlType(SparqlResult src) throws LmsXapiProfileException {
        if(src == null) {
            throw new IllegalArgumentException("SparqlResult was null!");
        }
        if(src.getType() == null) {
            throw new LmsXapiProfileException("?Type within SparqlResult was null!");
        }
        String type = src.getType();
        if(type.contains("#")) {
            String[] split = type.split("#");
            String matchable = split[split.length-1];
            return ProfileComponentTypeEnum.valueOf(toScreamingSnakeCase(matchable));
        } else {
            return ProfileComponentTypeEnum.valueOf(toScreamingSnakeCase(type));
        }
    }
    
    /**
     * Parse out fields from SPARQL query result common to all xAPI Profile components
     * 
     * @param id - identifier for the xAPI Profile component
     * @param src - SPARQL query result for the xAPI Profile component
     * 
     * @throws LmsXapiProfileException when unable to set id or type
     */
    public AbstractProfileComponent(String id, SparqlResult src) throws LmsXapiProfileException {
        this();
        setId(toURI(id));
        setComponentType(parseSparqlType(src));
        if(src.getInScheme() != null) {
            setInScheme(toURI(src.getInScheme()));
        }
        if(src.getDeprecated() == null || src.getDeprecated() == "false") {
            setDeprecated(false);
        } else if(src.getDeprecated() == "true") {
            setDeprecated(true);
        }
    }
    
    /**
     * Execute SPARQL query string and parse result
     * 
     * @param q - local or live SPARQL query client
     * @param sparql - SPARQL query string
     * 
     * @return parsed SPARQL query result
     * 
     * @throws LmsXapiSparqlException when unable to execute SPARQL query
     */
    protected static SparqlResult runQuery(SparqlQuery q, String sparql) throws LmsXapiSparqlException {
        if(q == null || sparql == null) {
            throw new IllegalArgumentException("SparqlQuery and Query String can not be null!");
        }
        return SparqlQuery.parseQuery(q.executeQuery(sparql));
    }
    
    /**
     * Create SPARQL query string for pattern with id, execute query and parse results
     *  
     * @param id - identifier for the pattern
     * @param forceLocal - should the local client be used opposed to querying ADL xAPI Profile server
     * 
     * @return result of the SPARQL query
     * 
     * @throws LmsXapiSparqlException when unable to execute SPARQL query
     */
    protected SparqlResult patternSparqlQuery(String id, boolean forceLocal) throws LmsXapiSparqlException {
        if(forceLocal && profileServer.getLocalClient() != null) {
            return profileServer.getLocalClient().searchPatterns(id);
        } else {
            return patternSparqlQuery(id);
        }
    }
    
    /**
     * Create SPARQL query string for pattern with id, execute query and parse results
     * 
     * @param id - identifier for the pattern
     * 
     * @return result of the SPARQL query
     * 
     * @throws LmsXapiSparqlException when unable to execute SPARQL query
     */
    protected SparqlResult patternSparqlQuery(String id) throws LmsXapiSparqlException {
        SparqlResult data;
        if(profileServer.getLiveClient() != null) {
            data = runQuery(profileServer.getLiveClient(), SparqlQueryString.createPatternQuery(id));
        } else if(profileServer.getLocalClient() != null) {
            data = profileServer.getLocalClient().searchPatterns(id);
        } else {
            throw new LmsXapiSparqlException("Both local and live profile server instances are null!");
        }
        return data;
    }
    
    /**
     * Setter for 'id'
     * 
     * @param id - identifier for the xAPI Profile component
     */
    private void setId(URI id) {
        if(id == null) {
            throw new IllegalArgumentException("id can not be null!");
        }
        this.id = id;
    }
    
    /**
     * Getter for 'id'
     * 
     * @return identifier for the xAPI Profile component
     */
    public URI getId() {
        return id;
    }
    
    /**
     * Setter for 'type'
     * 
     * @param conceptType - Enumeration corresponding to one of the xAPI Profile component type
     */
    private void setComponentType(ProfileComponentTypeEnum conceptType) {
        if(conceptType == null) {
            throw new IllegalArgumentException("conceptType can not be null!");
        }
        this.type = conceptType;
    }
    
    /**
     * Getter for 'type'
     */
    public ProfileComponentTypeEnum getComponentType() {
        return type;
    }
    
    /**
     * Setter for 'inScheme'
     * 
     * @param inScheme - xAPI Profile version identifier in which the xAPI Profile component is defined
     */
    private void setInScheme(URI inScheme) {
        if(inScheme == null) {
            throw new IllegalArgumentException("inScheme can not be null!");
        }
        this.inScheme = inScheme;
    }
    
    /**
     * Getter for 'inScheme'
     * 
     * @return xAPI Profile version identifier in which the xAPI Profile component is defined
     */
    public URI getInScheme() {
        return inScheme;
    }
    
    /**
     * Setter for 'deprecated'
     * 
     * @param deprecated - is this xAPI Profile component deprecated?
     */
    private void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
    
    /**
     * Getter for 'deprecated'
     * 
     * @return is this xAPI Profile component deprecated?
     */
    public boolean getDeprecated() {
        return deprecated;
    }
}
