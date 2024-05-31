package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of Training Application type
 * 
 * @author Yet Analytics
 *
 */
public class TrainingApplicationActivity extends AbstractGiftActivity {
    
    private static final String slug = "training.application";

    /**
     * Create Training Application Activity.
     * 
     * @param trainingApp - Training Application representation
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public TrainingApplicationActivity(TrainingApplicationEnum trainingApp) throws LmsXapiActivityException {
        super(createId(slug, trainingApp.getName()),
                trainingApp.getName(),
                trainingApp.getDisplayName());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromCollection(ContextActivitiesHelper.getCategoryActivities(statement.getContext()));
    }
}
