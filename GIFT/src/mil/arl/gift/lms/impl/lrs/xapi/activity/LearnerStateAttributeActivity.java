package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Activity representation of a Learner State Attribute
 * 
 * @author Yet Analytics
 *
 */
public class LearnerStateAttributeActivity extends AbstractGiftActivity {
    
    private static final String slug = "learner.state.attribute";
    
    /**
     * Creates Learner State Attribute Activity.
     * 
     * @param attrName - name of the learner state attribute
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public LearnerStateAttributeActivity(String attrName) throws LmsXapiActivityException {
        super(createId(slug, attrName), attrName);
    }
    
    /**
     * Create Learner State Attribute Activity from Learner State Attribute.
     * 
     * @param lsa - Learner State Attribute to create activity from
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public LearnerStateAttributeActivity(LearnerStateAttribute lsa) throws LmsXapiActivityException {
        this(lsa.getName().getDisplayName());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromStatementTarget(statement.getObject());
    }
}
