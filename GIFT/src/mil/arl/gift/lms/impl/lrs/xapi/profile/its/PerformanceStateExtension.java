package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

/**
 * Helper methods specific to the Performance State extension.
 * 
 * @author Yet Analytics
 *
 */
public interface PerformanceStateExtension extends ExtensionConceptStatement {
    
    /**
     * Creates Extension value from AbstractPerformanceState and adds to passed in Extensions
     * 
     * @param performanceState - AbstractPerformanceState to parse into Extension JSON
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(AbstractPerformanceState performanceState, Extensions ext);
    
    /**
     * Creates Extension value from AbstractPerformanceState and adds to passed in Extensions
     * 
     * @param performanceState - AbstractPerformanceState to parse into Extension JSON
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(AbstractPerformanceState performanceState, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension value from AbstractPerformanceState and add to Result Extensions
     * 
     * @param result - Result to update
     * @param performanceState - AbstractPerformanceState to parse
     */
    public void addToResult(Result result, AbstractPerformanceState performanceState);
    
    /**
     * Create Extension value from AbstractPerformanceState and add to Result Extensions
     * 
     * @param result - Result to update
     * @param performanceState - AbstractPerformanceState to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, AbstractPerformanceState performanceState, boolean forceOverwrite);
    
    /**
     * Create Extension value from AbstractPerformanceState and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param performanceState - AbstractPerformanceState to parse
     */
    public void addToStatement(Statement statement, AbstractPerformanceState performanceState);
    
    /**
     * Create Extension value from AbstractPerformanceState and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param performanceState - AbstractPerformanceState to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, AbstractPerformanceState performanceState, boolean forceOverwrite);
}
