/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import mil.arl.gift.common.ert.EventReportColumn;

/**
 * This class represents a cell in a row in a report
 * 
 * @author mhoffman
 *
 */
public class Cell {

    /** name of the column this cell should be a part of */
    private EventReportColumn column;
    
    /** contents of the cell */
    private String value;
    
    /**
     * Class constructor - set attributes
     * 
     * @param value - the value to display in the cell
     * @param column - the column to put the value under
     */
    public Cell(String value, EventReportColumn column){
        setValue(value);
        this.column = column;
    }
    
    /**
     * Return the value (i.e. contents) in this cell
     * 
     * @return String
     */
    public String getValue(){
        return value;
    }
    
    /**
     * Set the value of this cell
     * @param value the value for this cell
     */
    public void setValue(String value){
        this.value = value;
    }
    
    /**
     * Return the name of the column this cell should be in.
     * 
     * @return the column this cell should be in.
     */
    public EventReportColumn getColumn(){
        return column;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Cell: ");
        sb.append("column = ").append(getColumn());
        sb.append(", value = ").append(getValue());
        sb.append("]");
        
        return sb.toString();
    }
}
