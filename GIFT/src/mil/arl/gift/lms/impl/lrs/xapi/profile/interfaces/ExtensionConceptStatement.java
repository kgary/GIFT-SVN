package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.Statement;

/**
 * Helper methods for Context/Result Extensions.
 * 
 * @author Yet Analytics
 *
 */

public interface ExtensionConceptStatement extends ExtensionConcept { 
    
    /**
     * Add Result or Context extension to statement when verb is one of the recommended verbs.
     * Attempts merge of value and any existing extension value for this Extension id.
     * 
     * @param statement - xAPI Statement to update
     * @param value - value to add to the extension within xAPI Statement
     */
    public void addToStatement(Statement statement, Object value);
    
    /**
     * Add Result or Context extension to statement when verb is one of the recommended verbs.
     * Merge versus overwrite of value within extension is controlled by forceOverwrite.
     * 
     * @param statement - xAPI Statement to update
     * @param value - value to add to the extension within xAPI Statement
     * @param forceOverwrite - should existing value within extension be overwritten
     */
    public void addToStatement(Statement statement, Object value, boolean forceOverwrite);
    
}
