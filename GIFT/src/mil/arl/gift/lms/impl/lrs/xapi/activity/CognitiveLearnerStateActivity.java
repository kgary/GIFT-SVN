package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;

/**
 * Activity representation of a Cognitive Learner State Attribute.
 * 
 * @author Yet Analytics
 *
 */
public class CognitiveLearnerStateActivity extends AbstractGiftActivity {
    
    //Singleton
    private static CognitiveLearnerStateActivity instance = null;
    // Constructor
    /**
     * Resolves Cognitive Learner State Activity from xAPI Profile.
     * 
     * @throws LmsXapiActivityException when unable to create Activity from Activity Concept
     * @throws LmsXapiProfileException when unable to parse Activity Concept from xAPI Profile
     * @throws URISyntaxException when Activity Concept contains invalid identifier
     */
    private CognitiveLearnerStateActivity() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
        super(ItsActivityConcepts.CognitiveStateActivity.getInstance().asActivity());
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
    public static CognitiveLearnerStateActivity getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
        if(instance == null) {
            instance = new CognitiveLearnerStateActivity();
        }
        return instance;
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Cognitive Learner State Activity expected to be found within Category Context Activities
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
}
