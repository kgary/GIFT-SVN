/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.List;

import mil.arl.gift.common.MobileAppProperties;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;

/**
 * Utility class for helping to extract attributes from common objects used across messages into
 * cells that will be output by the ERT.
 * 
 * @author mhoffman
 *
 */
public class CommonEventObjectsUtil {

    /**
     * Parse a mobile app properties object into cells.
     * 
     * @param mobileAppProperties contains information about mobile app being used by learner.  If null this method
     * does nothing.
     * @param cells where to place the attributes extracted, can't be null.
     */
    public static void createCellsForMobileAppProperties(MobileAppProperties mobileAppProperties, List<Cell> cells){
        
        if(cells == null){
            throw new IllegalArgumentException("The cells parameter is null");
        }else if(mobileAppProperties == null){
            return;
        }
        
        cells.add(new Cell(String.valueOf(mobileAppProperties.getPlatform()), EventReportColumn.PLATFORM_COLUMN));
        cells.add(new Cell(String.valueOf(mobileAppProperties.getVersion()), EventReportColumn.VERSION_COLUMN));
        cells.add(new Cell(String.valueOf(mobileAppProperties.getScreenWidth()), EventReportColumn.SCREEN_WIDTH_COLUMN));
        cells.add(new Cell(String.valueOf(mobileAppProperties.getScreenHeight()), EventReportColumn.SCREEN_HEIGHT_COLUMN));
    }
}
