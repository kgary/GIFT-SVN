package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.Result;

/**
 * Helper methods for Result Extensions.
 * 
 * @author Yet Analytics
 *
 */
public interface ExtensionConceptResult extends ExtensionConceptStatement {

    /**
     * Add Extension Concept with value to Result Extensions.
     * 
     * @param result - Result to update
     * @param value - value to be found within Extension
     */
    public void addToResult(Result result, Object value);
    
    /**
     * Add Extension Concept with value to Result Extensions.
     * Merge versus overwrite of value within extension is controlled by forceOverwrite.
     * 
     * @param result- Result to update
     * @param value - value to be found within Extension
     * @param forceOverwrite - should existing value within extension be overwritten
     */
    public void addToResult(Result result, Object value, Boolean forceOverwrite);
}
