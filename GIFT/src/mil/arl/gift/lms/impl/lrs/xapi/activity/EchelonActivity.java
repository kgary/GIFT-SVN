package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of an EchelonEnum
 * 
 * @author Yet Analytics
 *
 */
public class EchelonActivity extends AbstractGiftActivity {
    
    private static final String slug = "echelon";
    
    /**
     * Creates Activity from EchelonEnum
     * 
     * @param echelon - Echelon to convert to Activity
     * 
     * @throws LmsXapiActivityException when unable to create Activity id from echelon
     */
    public EchelonActivity(EchelonEnum echelon) throws LmsXapiActivityException {
        super(
                createId(slug,
                        echelon.getBranch().getDisplayName().toLowerCase(),
                        echelon.getDisplayName().toLowerCase()),
                echelon.getDisplayName(),
                echelon.getComponents());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromCollection(ContextActivitiesHelper.getGroupingActivities(statement.getContext()));
    }
}
