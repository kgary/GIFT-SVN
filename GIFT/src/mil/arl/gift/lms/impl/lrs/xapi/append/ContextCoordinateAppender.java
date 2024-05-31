package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.util.List;
import com.rusticisoftware.tincan.Context;
import generated.dkf.Coordinate;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Coordinate Extension from Coordinate(s) and adds to xAPI Statement
 * as Context Extension.
 * 
 * @author Yet Analytics
 *
 */
public class ContextCoordinateAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Coordinate Context Extension Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Coordinate Context Extension to xAPI Statement";
    /** Single Coordinate used to create Extension JSON */
    private Coordinate coord;
    /** Collection of Coordinates used to create Extension JSON*/
    private List<Coordinate> coords;
    /** Context Extension defined within xAPI Profile */
    private ItsContextExtensionConcepts.CoordinateContext extension;
    /**
     * Parses Coordinate Context Extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse from xAPI Profile
     */
    private ContextCoordinateAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.extension = ItsContextExtensionConcepts.CoordinateContext.getInstance();
    }
    /**
     * Constructor called when there is only a single Coordinate used to create Extension JSON
     * 
     * @param coordinate - Coordinate to create Extension JSON from
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension Concept from xAPI Profile
     */
    public ContextCoordinateAppender(Coordinate coordinate) throws LmsXapiProfileException {
        this();
        if(coordinate == null) {
            throw new IllegalArgumentException("Coordinate can not be null!");
        }
        this.coord = coordinate;
    }
    /**
     * Constructor called when a collection of Coordinates are used to create Extension JSON
     * 
     * @param coordinates - Collection of Coordinates to create Extension JSON from
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension Concept from xAPI Profile
     */
    public ContextCoordinateAppender(List<Coordinate> coordinates) throws LmsXapiProfileException {
        this();
        if(coordinates == null) {
            throw new IllegalArgumentException("Coordinates can not be null!");
        }
        this.coords = coordinates;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context context = statement.getContext();
        if(coord != null) {
            extension.addToContext(context, coord);
        } else if(coords != null) {
            extension.addToContext(context, coords);
        }
        statement.setContext(context);
        return statement;
    }
}
