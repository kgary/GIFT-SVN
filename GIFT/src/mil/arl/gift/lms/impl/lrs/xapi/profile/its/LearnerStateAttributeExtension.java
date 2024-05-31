package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

/**
 * Helper methods specific to Learner State Attribute extensions.
 * 
 * @author Yet Analytics
 *
 */
public interface LearnerStateAttributeExtension extends ExtensionConceptStatement {
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to passed in Extensions
     * 
     * @param attribute - LearnerStateAttribute to parse
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(LearnerStateAttribute attribute, Extensions ext);
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to passed in Extensions
     * 
     * @param attribute - LearnerStateAttribute to parse
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(LearnerStateAttribute attribute, Extensions ext, boolean forceOverwrite);
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to Result Extensions
     * 
     * @param result - Result to update
     * @param attribute - LearnerStateAttribute to parse
     */
    public void addToResult(Result result, LearnerStateAttribute attribute);
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to Result Extensions
     * 
     * @param result - Result to update
     * @param attribute - LearnerStateAttribute to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, LearnerStateAttribute attribute, boolean forceOverwrite);
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param attribute - LearnerStateAttribute to parse
     */
    public void addToStatement(Statement statement, LearnerStateAttribute attribute);
    
    /**
     * Creates Extension value from LearnerStateAttribute and adds to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param attribute - LearnerStateAttribute to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, LearnerStateAttribute attribute, boolean forceOverwrite);
}
