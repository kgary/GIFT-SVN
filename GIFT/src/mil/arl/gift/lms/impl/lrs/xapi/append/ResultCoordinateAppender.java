package mil.arl.gift.lms.impl.lrs.xapi.append;

import com.rusticisoftware.tincan.Result;
import generated.dkf.Coordinate;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Coordinate Extension from Coordinate and adds to xAPI Statement
 * as Result Extension.
 * 
 * @author Yet Analytics
 *
 */
public class ResultCoordinateAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Result Coordinate Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Result Extension JSON from Coordinate";
    /** Coordinate to create Extension JSON from */
    private Coordinate coord;
    /** Result Extension from xAPI Profile */
    private ItsResultExtensionConcepts.CoordinateResult extension;
    /**
     * Parses Result Extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse Result Extension from xAPI Profile
     */
    private ResultCoordinateAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.extension = ItsResultExtensionConcepts.CoordinateResult.getInstance();
    }
    /**
     * Sets coordinate to create Result Extension JSON from
     * 
     * @param coordinate - Coordinate from Environment Adaptation
     * 
     * @throws LmsXapiProfileException when unable to parse Result Extension from xAPI Profile
     */
    public ResultCoordinateAppender(Coordinate coordinate) throws LmsXapiProfileException {
        this();
        if(coordinate == null) {
            throw new IllegalArgumentException("coordinate can not be null!");
        }
        this.coord = coordinate;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Result result = statement.getResult();
        extension.addToResult(result, coord);
        statement.setResult(result);
        return statement;
    }
}
