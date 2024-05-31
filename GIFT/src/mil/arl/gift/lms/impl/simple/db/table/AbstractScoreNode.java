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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="abstractscorenode")
public abstract class AbstractScoreNode {

    /** the unique id of this node */
    private int scoreNodeId;
    
    /** the display name of the score node */
    private String name;
    
    /** (optional) the parent score node to this node */
    private AbstractScoreNode parent;
    
    /** (optional) the performance node id associated with this score node. */
    private Integer performanceNodeId;
    
    /**
     * Default constructor - required by hibernate
     */
    public AbstractScoreNode(){
        
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the display name of the score node
     * @param parent - the parent of this node.  Can be null if there is no parent.
     */
    public AbstractScoreNode(String name, AbstractScoreNode parent){
        this.name = name;
        this.parent = parent;
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
     * Gets the name of this node.
     * @return String The name of this node.
     */
    //can't be null
    @Column(nullable=false)
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this node.
     * @param name - the display name of this node
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the performance node id that is associated with this score node.
     * 
     * @return Integer the performance node id that is associated with this score node.
     */
    //can be null
    @Column(nullable=true)
    public Integer getPerformanceNodeId() {
        return performanceNodeId;
    }

    /**
     * Sets the performance node id that is associated with this score node.
     * 
     * @param performanceNodeId the performance node id that is associated with this score node.
     */
    public void setPerformanceNodeId(Integer performanceNodeId) {
        this.performanceNodeId = performanceNodeId;
    }
    
    /**
     * Sets a link to the parent node. Used by getFullName to construct the full name.
     * @param parent - the parent node
     */
    public void setParent(AbstractScoreNode parent) {
        this.parent = parent;
    }
    
    /**
     * Gets the parent node.
     * @return a reference to the parent node. Value will be null if node is root. 
     * (NOTE: The node is still a root node if it hasn't been added to a parent.)
     */
    //if a score node is deleted, don't want parent score node to be deleted
    @ManyToOne
    @JoinColumn(name="parent_scoreNodeId_FK")
    public AbstractScoreNode getParent() {
        return parent;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractScoreNode:");
        sb.append(" id = ").append(getScoreNodeId());
        sb.append(", name = ").append(getName());
        sb.append(", parent = ").append(getParent());
        sb.append(", ancestorPerformanceNodeId = ").append(getPerformanceNodeId());
        sb.append("]");
        
        return sb.toString();
    }
}
