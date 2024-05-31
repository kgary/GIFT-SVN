package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import java.util.List;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

/**
 * Helper methods specific to the Node Hierarchy extension.
 * 
 * @author Yet Analytics
 *
 */
public interface NodeHierarchyExtension extends ExtensionConceptStatement {
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to passed in Extensions
     * 
     * @param history - List of GradedScoreNode(s)
     * @param ext - Extensions to update
     * 
     * @return Updated Extensions
     * 
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public Extensions asExtension(List<GradedScoreNode> history, Extensions ext) throws LmsXapiExtensionException;
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to passed in Extensions
     * 
     * @param history - List of GradedScoreNode(s)
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return Updated Extensions
     * 
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public Extensions asExtension(List<GradedScoreNode> history, Extensions ext, boolean forceOverwrite) throws LmsXapiExtensionException;
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to Context
     * 
     * @param context - Context to add extension to
     * @param history - List of GradedScoreNodes represented within extension
     *  
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public void addToContext(Context context, List<GradedScoreNode> history) throws LmsXapiExtensionException;
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to Context
     * 
     * @param context - Context to add extension to
     * @param history - List of GradedScoreNodes represented within extension
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public void addToContext(Context context, List<GradedScoreNode> history, boolean forceOverwrite) throws LmsXapiExtensionException;
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to Statement
     * 
     * @param statement - Statement with Context to update
     * @param history - List of GradedScoreNodes represented within extension
     * 
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public void addToStatement(Statement statement, List<GradedScoreNode> history) throws LmsXapiExtensionException;
    /**
     * Create Extension value from Collection of GradedScoreNodes and add to Statement
     * 
     * @param statement - Statement with Context to update
     * @param history - List of GradedScoreNodes represented within extension
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @throws LmsXapiExtensionException when unable to create extension
     */
    public void addToStatement(Statement statement, List<GradedScoreNode> history, boolean forceOverwrite) throws LmsXapiExtensionException;
}
