package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Interface implemented by classes which extend AbstractGiftActivity and allows for each class to specify
 * where the Activity can be found within an xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public interface XapiActivity {

    /**
     * Parses xAPI Activity from xAPI Statement. Location of Activity determined by implementing class.
     * 
     * @param statement - xAPI Statement to parse Activity from
     * 
     * @return Activity if found within xAPI Statement, null otherwise
     * 
     * @throws LmsXapiActivityException when parsing xAPI statement results in an error
     */
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException;
}
