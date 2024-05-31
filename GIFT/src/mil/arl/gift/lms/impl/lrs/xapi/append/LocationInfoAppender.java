package mil.arl.gift.lms.impl.lrs.xapi.append;

import com.rusticisoftware.tincan.Result;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Location Info Extension from Environment Adaptation and adds
 * to xAPI Statement as Result Extension.
 * 
 * @author Yet Analytics
 *
 */
public class LocationInfoAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Location Info Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Location Info Result Extension JSON from Location Info";
    /** Location Info Result Extension from xAPI Profile */
    private ItsResultExtensionConcepts.LocationInfo ext;
    /** Location Info from Highlight Objects Environment Adaptation */
    private EnvironmentAdaptation.HighlightObjects.LocationInfo highlightLoc;
    /** Location Info from Create Breadcrumbs Environment Adaptation */
    private EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo breadcrumbLoc;
    /**
     * Sets Location Info result extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse extension from xAPI Profile
     */
    private LocationInfoAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.ext = ItsResultExtensionConcepts.LocationInfo.getInstance();
    }
    /**
     * Handles Location Info from Highlight Objects environment adaptation
     * 
     * @param locInfo - location info
     * 
     * @throws LmsXapiProfileException when unable to parse extension from xAPI Profile
     */
    public LocationInfoAppender(EnvironmentAdaptation.HighlightObjects.LocationInfo locInfo) throws LmsXapiProfileException {
        this();
        if(locInfo == null) {
            throw new IllegalArgumentException("Location Info can not be null!");
        }
        this.highlightLoc = locInfo;
    }
    /**
     * Handles Location Info from Create Breadcrumbs environment adaptation
     * 
     * @param locInfo - location info
     * 
     * @throws LmsXapiProfileException when unable to parse extension from xAPI Profile
     */
    public LocationInfoAppender(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo) throws LmsXapiProfileException {
        this();
        if(locInfo == null) {
            throw new IllegalArgumentException("Location Info can not be null!");
        }
        this.breadcrumbLoc = locInfo;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Result result = statement.getResult();
        if(highlightLoc != null) {
            ext.addToResult(result, highlightLoc);
        } else if(breadcrumbLoc != null) {
            ext.addToResult(result, breadcrumbLoc);
        } else {
            throw new LmsXapiAppenderException("Location Info can't be null!");
        }
        statement.setResult(result);
        return statement;
    }
}
