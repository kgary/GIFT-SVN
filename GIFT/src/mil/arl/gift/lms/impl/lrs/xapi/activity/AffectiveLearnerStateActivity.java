package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;

/**
 * Activity representation of an Affective Learner State Attribute.
 * 
 * @author Yet Analytics
 *
 */
public class AffectiveLearnerStateActivity extends AbstractGiftActivity {
    
    // Singleton
    private static AffectiveLearnerStateActivity instance = null;
    // Constructor
    /**
     * Resolves Affective Learner State Activity from xAPI Profile.
     * 
     * @throws LmsXapiActivityException when unable to create Activity from Activity Concept 
     * @throws LmsXapiProfileException when unable to parse Activity Concept from xAPI Profile
     * @throws URISyntaxException when Activity Concept contains invalid identifier
     */
    private AffectiveLearnerStateActivity() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
        super(ItsActivityConcepts.AffectiveStateActivity.getInstance().asActivity());
    }
    // Access
    /**
     * Calls private constructor and sets result as instance.
     * 
     * @return Activity representation of Activity Concept found within xAPI Profile
     * 
     * @throws LmsXapiActivityException when unable to create Activity from Activity Concept
     * @throws LmsXapiProfileException when unable to parse Activity Concept from xAPI Profile
     * @throws URISyntaxException when Activity Concept contains invalid identifier
     */
    public static AffectiveLearnerStateActivity getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
        if(instance == null) {
            instance = new AffectiveLearnerStateActivity();
        }
        return instance;
    }
    
    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Affective Learner State Activity expected to be found within Category Context Activities
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
}
