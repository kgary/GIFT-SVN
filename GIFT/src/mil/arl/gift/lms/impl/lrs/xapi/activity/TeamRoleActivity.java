package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of a Team Role
 * 
 * @author Yet Analytics
 *
 */
public class TeamRoleActivity extends AbstractGiftActivity {
    
    private static final String slug = "team.role";
    
    /**
     * Create Team Role Activity.
     * 
     * @param roleIdentifier - identifier for the team role
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public TeamRoleActivity(String roleIdentifier) throws LmsXapiActivityException {
        super(createId(slug, roleIdentifier), roleIdentifier);
    }
    
    /**
     * Create Team Role Activity.
     * 
     * @param teamRole - TeamMember to convert to xAPI Activity
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public TeamRoleActivity(TeamMember<?> teamRole) throws LmsXapiActivityException {
        // NOTE: name used for alignment to assessed team org entities within PerformanceStateAttribute
        this(teamRole.getName());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
}
