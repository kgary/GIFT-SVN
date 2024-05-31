package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Activity representation of a Survey
 * 
 * @author Yet Analytics
 *
 */
public class SurveyActivity extends AbstractGiftActivity {

    /**
     * Creates Survey Activity.
     * 
     * @param surveyName - name of the survey
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public SurveyActivity(String surveyName) throws LmsXapiActivityException {
        super(createId(surveyName), surveyName);
    }
    
    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromStatementTarget(statement.getObject());
    }
}
