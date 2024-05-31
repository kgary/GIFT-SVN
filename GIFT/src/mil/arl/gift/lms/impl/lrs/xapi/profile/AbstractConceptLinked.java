package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Generic representation for xAPI Profile components which utilize JSON Schema.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractConceptLinked extends AbstractProfileComponentDescribed {
    
    /**
     * the URI of a JSON-LD context for this Concept
     */
    private URI context;
    
    /**
     * the URI for accessing a JSON Schema for this Concept
     */
    private URI schema;
    
    /**
     * alternate way to include a JSON Schema, as a string, for this Concept
     */
    private String inLineSchema;
    
    /**
     * Parses context, schema and inLineSchema from SPARQL query result
     * 
     * @param id - identifier of the xAPI Profile component
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractConceptLinked(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        if(src.getContext() != null) {
            setContext(toURI(src.getContext()));
        }
        if(src.getSchema() != null) {
            setSchema(toURI(src.getSchema()));
        }
        if(src.getInlineSchema() != null) {
            setInLineSchema(src.getInlineSchema());
        }
    }
    
    /**
     * Getter for 'context'
     * 
     * @return context URI
     */
    public URI getContext() {
        return context;
    }
    
    /**
     * Setter for 'context'
     * 
     * @param jsonldContext - JSON-LD context URI 
     */
    private void setContext(URI jsonldContext) {
        if(jsonldContext == null) {
            throw new IllegalArgumentException("jsonldContext can not be null!");
        }
        this.context = jsonldContext;
    }
    
    /**
     * Getter for 'schema'
     * 
     * @return JSON Schema URI
     */
    public URI getSchema() {
        return schema;
    }
    
    /**
     * Setter for 'schema'
     * 
     * @param jsonSchema - URI pointing to JSON Schema
     */
    private void setSchema(URI jsonSchema) {
        if(jsonSchema == null) {
            throw new IllegalArgumentException("jsonSchema can not be null!");
        }
        this.schema = jsonSchema;
    }
    
    /**
     * Getter for 'inLineSchema'
     * 
     * @return JSON Schema string
     */
    public String getInLineSchema() {
        return inLineSchema;
    }
    
    /**
     * Setter for 'inLineSchema'
     * 
     * @param jsonSchema - JSON Schema string
     */
    private void setInLineSchema(String jsonSchema) {
        if(jsonSchema == null) {
            throw new IllegalArgumentException("jsonSchema can not be null!");
        }
        this.inLineSchema = jsonSchema;
    }
}
