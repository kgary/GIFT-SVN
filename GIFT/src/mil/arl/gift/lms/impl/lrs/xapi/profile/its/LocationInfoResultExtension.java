package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

public interface LocationInfoResultExtension extends ExtensionConceptStatement {

    /**
     * Create Extension value from LocationInfo and add to passed in Extensions
     * 
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, Extensions ext);
    
    /**
     * Create Extension value from LocationInfo and add to passed in Extensions
     * 
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension value from LocationInfo and add to passed in Extensions
     * 
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, Extensions ext);
    
    /**
     * Create Extension value from LocationInfo and add to passed in Extensions
     * 
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions
     * 
     * @param result - Result to update
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     */
    public void addToResult(Result result, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions
     * 
     * @param result - Result to update
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, boolean forceOverwrite);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions
     * 
     * @param result - Result to update
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     */
    public void addToResult(Result result, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions
     * 
     * @param result - Result to update
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, boolean forceOverwrite);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     */
    public void addToStatement(Statement statement, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, boolean forceOverwrite);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     */
    public void addToStatement(Statement statement, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo);
    
    /**
     * Create Extension JSON from LocationInfo and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, boolean forceOverwrite);
}
