package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of a Domain
 * 
 * @author Yet Analytics
 *
 */
public class DomainActivity extends AbstractGiftActivity {
    
    /**
     * Parses out course name from domain identifier
     * 
     * @param domainId - String expected to containing 'course.xml'
     * 
     * @return name of the course parsed from domain identifier
     */
    protected static String courseNameFromDomainId(String domainId) {
        String courseName = null;
        String[] items = domainId.split("\\\\");
        for(String item : items) {
            if(item.contains("course.xml")) {
                courseName = item;
                break;
            }
        }
        return courseName;
    }
    
    /**
     * Creation of Domain Activity from domain identifier
     * 
     * @param domainId - string containing course name used to create identifier and set display name
     * 
     * @throws LmsXapiActivityException when unable to create valid identifier
     */
    public DomainActivity(String domainId) throws LmsXapiActivityException {
        super(createId(courseNameFromDomainId(domainId)), courseNameFromDomainId(domainId));
    }
    
    /**
     * Wrapper for creation of Domain Activity from existing Activity
     * 
     * @param a - Activity to convert to Domain Activity
     * 
     * @throws URISyntaxException when passed in activity does not contain valid identifier
     */
    public DomainActivity(Activity a) throws URISyntaxException {
        super(a);
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Domain Activity expected to be found within Parent Context Activities
        return parseFromCollection(ContextActivitiesHelper.getParentActivities(statement.getContext()));
    }
}
