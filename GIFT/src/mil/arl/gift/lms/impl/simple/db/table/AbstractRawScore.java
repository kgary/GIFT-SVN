/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db.table;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="abstractrawscore")
public abstract class AbstractRawScore {

    private int scoreNodeId;
    
    /** units label */
    public String units;
    
    /**
     * Default constructor - required by hibernate
     */
    public AbstractRawScore(){
        
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param units units label
     */
    public AbstractRawScore(String units){
        this.units = units;
    }
    
    //primary key, auto-generated sequentially
    @Id
    @Column(name="scoreNodeId_PK")
    @TableGenerator(name="scorenodeid", table="scorenodepktb", pkColumnName="scorenodekey", pkColumnValue="scorenodevalue", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE, generator="scorenodeid")
    public int getScoreNodeId() {
        return scoreNodeId;
    }

    public void setScoreNodeId(int scoreNodeId) {
        this.scoreNodeId = scoreNodeId;
    }
    
    /**
     * Sets the units label. Null values are illegal.
     * 
     * @param units units label
     */
    public void setUnits(String units) {        
        this.units = units;
    }

    //can't be null
    @Column(nullable=false)
    public String getUnits() {
        
        return units;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractRawScore:");
        sb.append(" id = ").append(getScoreNodeId());
        sb.append(", units = ").append(getUnits());
        sb.append("]");
        
        return sb.toString();
    }
}
