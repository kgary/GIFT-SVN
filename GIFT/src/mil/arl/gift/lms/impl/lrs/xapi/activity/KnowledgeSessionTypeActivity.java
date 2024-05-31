package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;

/**
 * Activity representation of the Knowledge Session Type.
 * 
 * Used within xAPI Statements to indicate if the data was generated from previously
 * recorded interactions or live interactions
 * 
 * @author Yet Analytics
 *
 */
public class KnowledgeSessionTypeActivity extends AbstractGiftActivity {
    
    /**
     * Converts from static / canonical Activity found in xAPI Profile to Activity
     * 
     * @param a - static / canonical Activity parsed from xAPI Profile
     * 
     * @throws LmsXapiActivityException when unable to convert to Activity
     * @throws URISyntaxException when activity id invalid
     */
    protected KnowledgeSessionTypeActivity(ItsActivityConcepts a) throws LmsXapiActivityException, URISyntaxException {
        super(a.asActivity());
    }
    
    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
    
    public static class Active extends KnowledgeSessionTypeActivity {
        // Singleton
        private static Active instance = null;
        // Constructor
        private Active() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.ActiveKnowledgeSessionActivity.getInstance());
        }
        // Access
        public static Active getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Active();
            }
            return instance;
        }
    }
    
    public static class ActivePlayback extends KnowledgeSessionTypeActivity {
        // Singleton
        private static ActivePlayback instance = null;
        // Constructor
        private ActivePlayback() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.ActivePlaybackKnowledgeSessionActivity.getInstance());
        }
        // Access
        public static ActivePlayback getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new ActivePlayback();
            }
            return instance;
        }
    }
}
