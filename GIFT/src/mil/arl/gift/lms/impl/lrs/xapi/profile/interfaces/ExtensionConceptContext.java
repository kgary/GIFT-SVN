package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.Context;

/**
 * Helper methods for Concept Extensions.
 * 
 * @author Yet Analytics
 *
 */
public interface ExtensionConceptContext extends ExtensionConceptStatement {

    /**
     * Add Extension Concept with value to Context Extensions.
     * 
     * @param context - Context to update
     * @param value - value to be found within Extension
     */
    public void addToContext(Context context, Object value);
    
    /**
     * Add Extension Concept with value to Context Extensions.
     * Merge versus overwrite of value within extension is controlled by forceOverwrite.
     * 
     * @param context - Context to update
     * @param value - value to be found within Extension
     * @param forceOverwrite - should existing value within extension be overwritten
     */
    public void addToContext(Context context, Object value, Boolean forceOverwrite);
}
