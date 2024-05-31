package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import java.util.List;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import generated.dkf.Coordinate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces.ExtensionConceptStatement;

public interface CoordinateResultExtension extends ExtensionConceptStatement {

    /**
     * Create Extension value from Coordinate and adds to passed in Extensions
     * 
     * @param coordinate - Coordinate to parse
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(Coordinate coordinate, Extensions ext);
    
    /**
     * Create Extension value from Coordinate and adds to passed in Extensions
     * 
     * @param coordinate - Coordinate to parse
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(Coordinate coordinate, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension value from Coordinates and adds to passed in Extensions
     * 
     * @param coordinates - Coordinate(s) to parse
     * @param ext - Extensions to update
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(List<Coordinate> coordinates, Extensions ext);
    
    /**
     * Create Extension value from Coordinates and adds to passed in Extensions
     * 
     * @param coordinates - Coordinate(s) to parse
     * @param ext - Extensions to update
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     * 
     * @return updated Extensions
     */
    public Extensions asExtension(List<Coordinate> coordinates, Extensions ext, boolean forceOverwrite);
    
    /**
     * Create Extension value from Coordinate and add to Result Extensions
     * 
     * @param result - Result to update
     * @param coordinate - Coordinate to parse
     */
    public void addToResult(Result result, Coordinate coordinate);
    
    /**
     * Create Extension value from Coordinate and add to Result Extensions
     * 
     * @param result - Result to update
     * @param coordinate - Coordinate to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, Coordinate coordinate, boolean forceOverwrite);
    
    /**
     * Create Extension value from collection of Coordinate and add to Result Extensions
     * 
     * @param result - Result to update
     * @param coordinates - Coordinate(s) to parse
     */
    public void addToResult(Result result, List<Coordinate> coordinates);
    
    /**
     * Create Extension value from collection of Coordinate and add to Result Extensions
     * 
     * @param result - Result to update
     * @param coordinates - Coordinate(s) to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToResult(Result result, List<Coordinate> coordinates, boolean forceOverwrite);
    
    /**
     * Create Extension value from Coordinate and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param coordinate - Coordinate to parse
     */
    public void addToStatement(Statement statement, Coordinate coordinate);
    
    /**
     * Create Extension value from Coordinate and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param coordinate - Coordinate to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, Coordinate coordinate, boolean forceOverwrite);
    
    /**
     * Create Extension value from collection of Coordinate and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param coordinates - Coordinate(s) to parse
     */
    public void addToStatement(Statement statement, List<Coordinate> coordinates);
    
    /**
     * Create Extension value from collection of Coordinate and add to Result Extensions within Statement
     * 
     * @param statement - Statement to update
     * @param coordinates - Coordinate(s) to parse
     * @param forceOverwrite - if true, always overwrite at key even if previous value exists
     */
    public void addToStatement(Statement statement, List<Coordinate> coordinates, boolean forceOverwrite);
}
