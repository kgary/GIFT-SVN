package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.math.BigInteger;
import com.rusticisoftware.tincan.Context;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Highlight Objects Extension from the Environment Adaptation and adds
 * to xAPI Statement as Context Extension.
 * 
 * @author Yet Analytics
 *
 */
public class HighlightAdaptationAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Highlight Environemnt Adaptation Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Context Extension JSON from Highlight Objects Environment Adaptation";
    /** Offset from Highlight Objects Environment Adaptation */
    private EnvironmentAdaptation.HighlightObjects.Offset offset;
    /** Color from Highlight Objects Environment Adaptation */
    private EnvironmentAdaptation.HighlightObjects.Color color;
    /** Duration from Highlight Objects Environment Adaptation */
    private BigInteger duration;
    /** Context Extension from xAPI Profile */
    private ItsContextExtensionConcepts.Highlight ext;
    /**
     * Parses Context Extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    private HighlightAdaptationAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.ext = ItsContextExtensionConcepts.Highlight.getInstance();
    }
    /**
     * Parses Highlight Objects Environment Adaptation
     * 
     * @param adaptation - Highlight Objects Environment Adaptation to parse
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    public HighlightAdaptationAppender(EnvironmentAdaptation.HighlightObjects adaptation) throws LmsXapiProfileException {
        this();
        if(adaptation == null) {
            throw new IllegalArgumentException("Highlight Objects Environment Adaptation can not be null!");
        }
        this.offset = adaptation.getOffset();
        this.color = adaptation.getColor();
        this.duration = adaptation.getDuration();
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context context = statement.getContext();
        ext.addToContext(context, offset, color, duration);
        statement.setContext(context);
        return statement;
    }
}
