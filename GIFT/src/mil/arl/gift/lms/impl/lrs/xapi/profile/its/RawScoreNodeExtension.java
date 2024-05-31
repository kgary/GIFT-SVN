package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

/**
 * Helper methods specific to Raw Score Node extension.
 * 
 * @author Yet Analytics
 *
 */
public interface RawScoreNodeExtension extends ExtensionConceptStatement {
    
    /**
     * Create Extension value from RawScoreNode and add to passed in Extensions
     * 
     * @param node - RawScoreNode to parse
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(RawScoreNode node, Extensions ext);
    
    /**
     * Create Extension value from RawScoreNode and add to passed in Extensions
     * 
     * @param node - RawScoreNode to parse
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(RawScoreNode node, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension value from RawScoreNode and add to Result Extensions
     * 
     * @param result - Result to update
     * @param node - RawScoreNode to parse
     */
    public void addToResult(Result result, RawScoreNode node);
    
    /**
     * Create Extension value from RawScoreNode and add to Result Extensions
     * 
     * @param result - Result to update
     * @param node - RawScoreNode to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, RawScoreNode node, boolean forceOverwrite);
    
    /**
     * Create Extension value from RawScoreNode and add to Result within Statement
     * 
     * @param statement - Statement to update
     * @param node - RawScoreNode to parse
     */
    public void addToStatement(Statement statement, RawScoreNode node);
    
    /**
     * Create Extension value from RawScoreNode and add to Result within Statement
     * 
     * @param statement - Statement to update
     * @param node - RawScoreNode to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, RawScoreNode node, boolean forceOverwrite);
}
