package mil.arl.gift.lms.impl.lrs.xapi.profile;

import java.net.URI;
import java.util.List;
import com.rusticisoftware.tincan.Extensions;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConcept;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Generic representation for Extension xAPI Profile components.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractExtensionConcept extends AbstractConceptLinked implements ExtensionConcept {
    
    /**
     * Parse fields from SPARQL query result
     * 
     * @param id - id of the extension concept
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set xAPI Profile component id or type
     */
    public AbstractExtensionConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
    
    /**
     * extension setter without guard rails
     * 
     * @param value - value to set at key for this ExtensionConcept
     * @param ext - extensions to add the value to at key
     */
    private void addToExtensions(Object value, Extensions ext) {
        if(ext == null) {
            throw new IllegalArgumentException("ext can not be null!");
        }
        ext.put(getId(), value);
    }
    
    /**
     * extension setter with overwrite guard rails
     * 
     * @param value - value to set or add at key for this ExtensionConcept
     * @param ext - extensions to possibly update 
     */
    private void updateExtensions(Object value, Extensions ext) {
        if(ext == null) {
            throw new IllegalArgumentException("ext can not be null!");
        }
        // existing this (ExtensionConcept) found within ext?
        JsonNode existingThis = parseFromExtensions(ext);
        // Prevent unintended overwrite
        if(existingThis != null) {
            // existing instance of This within existing extensions
            if(!existingThis.equals(value)) {
                // no-op when ==, ext already contains value at key
                if(existingThis instanceof ArrayNode) {
                    // have source collection to update
                    ArrayNode coll = (ArrayNode) existingThis;
                    // branch off of value type
                    if(value instanceof ArrayNode) {
                        // add items to existing coll
                        for(JsonNode item : (ArrayNode) value) {
                            coll.add(item);
                        }
                        addToExtensions(coll, ext);
                    } else {
                        // add thing to existing coll
                        coll.add((JsonNode) value);
                        addToExtensions(coll, ext);
                    }
                } else if(value instanceof ArrayNode) {
                    // value is a collection to update, existing + value = coll case handled above
                    ArrayNode coll = (ArrayNode) value;
                    coll.add(existingThis);
                    addToExtensions(coll, ext);   
                } else {
                    // neither existingThis nor value are a collection, overwrite with value!
                    addToExtensions(value, ext);
                }
            }
        } else {
            // no conflict at key so set as value
            addToExtensions(value, ext);
        }
    }
    
    /**
     * extension setter with ability to force overwrite
     * 
     * @param value - value to set for key
     * @param ext - extension to possibly update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    private void updateExtensions(Object value, Extensions ext, boolean forceOverwrite) {
        if(forceOverwrite) {
            addToExtensions(value, ext);
        } else {
            updateExtensions(value, ext);
        }
    }
    
    @Override
    public Extensions asExtension(Object value) {
        return asExtension(value, new Extensions(), true);
    }
    
    
    @Override
    public Extensions asExtension(Object value, Extensions ext) {
        return asExtension(value, ext, false);
    }
    
    @Override
    public Extensions asExtension(Object value, Extensions ext, boolean forceOverwrite) {
        updateExtensions(value, ext, forceOverwrite);
        return ext;
    }
    
    /**
     * Parses out the ExtensionConcept from the passed in Extensions
     * 
     * @param ext - extensions to parse this ExtensionConcept out of
     * 
     * @return JsonNode found at extension id within extension
     */
    public JsonNode parseFromExtensions(Extensions ext) {
        if(ext == null) {
            throw new IllegalArgumentException("ext can not be null!");
        }
        return ext.get(getId());
    }
    
    /**
     * Compares toCheck to the URIs found within recommended.
     * 
     * @param toCheck - target URI
     * @param recommended - collection of recommended URIs
     * 
     * @return does recommended contain the target URI
     */
    public boolean isRecommended(URI toCheck, List<URI> recommended) {
        if(toCheck == null) {
            return false;
        }
        // Absence of recommended means any and all are recommended
        if(recommended == null || CollectionUtils.isEmpty(recommended)) {
            return true;
        }
        for(URI rec : recommended) {
            if(toCheck.toString().equals(rec.toString())) {
                return true;
            }
        }
        return false;
    }
}
