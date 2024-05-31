package mil.arl.gift.lms.impl.lrs.xapi.append;

import com.rusticisoftware.tincan.Context;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Weather Extension from Environment Adaptation and adds to
 * xAPI Statement as Context Extension.
 * 
 * @author Yet Analytics
 *
 */
public class WeatherAdaptationAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Weather Environment Adaptation Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Extension JSON from Environment Adaptation and adds to xAPI Statement as Context Extension";
    /** Overcast Environment Adaptation */
    private EnvironmentAdaptation.Overcast overcast;
    /** Fog Environment Adaptation */
    private EnvironmentAdaptation.Fog fog;
    /** Rain Environment Adaptation */
    private EnvironmentAdaptation.Rain rain;
    /** Weather Environment Adaptation Context Extension */
    private ItsContextExtensionConcepts.WeatherEnvironmentAdaptation extension;
    /**
     * Parses Context Extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    private WeatherAdaptationAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.extension = ItsContextExtensionConcepts.WeatherEnvironmentAdaptation.getInstance();
    }
    /**
     * Sets Overcast Environment Adaptation to create Extension JSON from
     * 
     * @param adaptation - Overcast Environment Adaptation
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    public WeatherAdaptationAppender(EnvironmentAdaptation.Overcast adaptation) throws LmsXapiProfileException {
        this();
        if(adaptation == null) {
            throw new IllegalArgumentException("Adaptation can not be null!");
        }
        this.overcast = adaptation;
    }
    /**
     * Sets Fog Environment Adaptation to create Extension JSON from
     * 
     * @param adaptation - Fog Environment Adaptation
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    public WeatherAdaptationAppender(EnvironmentAdaptation.Fog adaptation) throws LmsXapiProfileException {
        this();
        if(adaptation == null) {
            throw new IllegalArgumentException("Adaptation can not be null!");
        }
        this.fog = adaptation;
    }
    /**
     * Sets Rain Environment Adaptation to create Extension JSON from
     * 
     * @param adaptation - Rain Environment Adaptation
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    public WeatherAdaptationAppender(EnvironmentAdaptation.Rain adaptation) throws LmsXapiProfileException {
        this();
        if(adaptation == null) {
            throw new IllegalArgumentException("Adaptation can not be null!");
        }
        this.rain = adaptation;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context ctx = statement.getContext();
        if(overcast != null) {
            extension.addToContext(ctx, overcast);
        } else if(fog != null) {
            extension.addToContext(ctx, fog);
        } else if(rain != null) {
            extension.addToContext(ctx, rain);
        } else {
            throw new LmsXapiAppenderException("Unable to append weather adaptation context extension!");
        }
        statement.setContext(ctx);
        return statement;
    }
}
