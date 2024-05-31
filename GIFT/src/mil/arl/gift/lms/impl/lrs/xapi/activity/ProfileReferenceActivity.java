package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URI;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of xAPI Profile reference
 * 
 * @author Yet Analytics
 *
 */
public class ProfileReferenceActivity extends AbstractGiftActivity {

    /**
     * Creates Profile Reference Activity.
     * 
     * @param uri - xAPI Profile inScheme
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public ProfileReferenceActivity(URI uri) throws LmsXapiActivityException {
        super(uri.toString());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
}
