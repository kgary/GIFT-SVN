/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a row in a report
 * 
 * @author mhoffman
 *
 */
public class Row {

    /** the cells that make up this row */
    List<Cell> cells;
    
    /**
     * Default constructor - start with no cells in this row
     */
    public Row(){
        this.cells = new ArrayList<Cell>();
    }
    
    /**
     * Class constructor - populate with cells
     * 
     * @param cells the cells that make up this row
     */
    public Row(List<Cell> cells){
        this.cells = cells;
    }
    
    /**
     * Add a cell to this row
     * 
     * @param cell a new cell for this row
     */
    public void addCell(Cell cell){
        cells.add(cell);
    }
    
    /**
     * Add the collection of cells to this row
     * 
     * @param cells the cells that make up this row
     */
    public void addCells(Collection<Cell> cells){
        this.cells.addAll(cells);
    }
    
    /**
     * Return the list of cells for this row
     * 
     * @return the cells for this row
     */
    public List<Cell> getCells(){
        return cells;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Row: ");
        
        sb.append("cells = {");
        for(Cell cell : cells){
            sb.append(cell.toString()).append(", ");
        }
        sb.append("}");

        sb.append("]");
        
        return sb.toString();
    }
}
