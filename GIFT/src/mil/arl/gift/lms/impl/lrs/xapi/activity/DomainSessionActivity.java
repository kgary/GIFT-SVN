package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of a Domain Session
 * 
 * @author Yet Analytics
 *
 */
public class DomainSessionActivity extends AbstractGiftActivity {
    
    private static final String slug = "domain.session";
    
    /**
     * Create Domain Session Activity with Domain Session identifier set as display name and used within Activity identifier creation
     * 
     * @param domainSessionId - Domain Session identifier
     * 
     * @throws LmsXapiActivityException when unable to create valid identifier
     */
    public DomainSessionActivity(Integer domainSessionId) throws LmsXapiActivityException {
        super(createId(slug, domainSessionId.toString()), domainSessionId.toString());
    }
    
    /**
     * Wrapper for creation of Domain Session Activity from existing Activity
     * 
     * @param a - Activity to convert to Domain Activity
     * 
     * @throws URISyntaxException
     */
    public DomainSessionActivity(Activity a) throws URISyntaxException {
        super(a);
    }
    
    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        Activity match = parseFromStatementTarget(statement.getObject());
        if(match != null) {
            return match;
        } else {
            return parseFromCollection(ContextActivitiesHelper.getGroupingActivities(statement.getContext()));
        }
    }
}
