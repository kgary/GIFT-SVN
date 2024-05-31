package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.Extensions;

/**
 * Helper methods attached to Extension Concepts.
 * 
 * @author Yet Analytics
 *
 */
public interface ExtensionConcept {
    
    /**
     * create an Extensions from this ExtensionConcept. Sets the Extensions key
     * to the id of this ExtensionConcept and the Extensions value to the passed
     * in 'value'
     * 
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ExtensionConcept
     * 
     * @return extension created from value
     */
    public Extensions asExtension(Object value);
    
    /**
     * creates and adds Extensions from this ExtensionConcept to the passed in Extensions.
     * Attempts to merge value with existing extension value(s) (if they exist) opposed to unapologetic overwrite
     * 
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ExtensionConcept
     * @param ext - extensions to add the current ExtensionConcept to
     * 
     * @return extension updated to contain value
     */
    public Extensions asExtension(Object value, Extensions ext);
    
    /**
     * creates this ExtensionConcept and used to update passed in extensions.
     * Update can be overwrite or attempt to merge when sensible
     * 
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ExtensionConcept
     * @param ext - extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return extension updated to contain value
     */
    public Extensions asExtension(Object value, Extensions ext, boolean forceOverwrite);
}
