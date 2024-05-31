/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db.table;

import java.util.Date;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@Entity
@Table(name="scoremetadata")
public class ScoreMetadata {

    private int scoreMetadataId;    
    private Date time;
    
    private int domainSessionId;
    
    private int userId;
    
    private GradedScoreNode rootNode;
    
    private String domainName;
    
    /**
     * Class constructor - needed by hibernate
     */
    public ScoreMetadata(){
        
    }
    
    public ScoreMetadata(int userId, int domainSessionId, Date time, GradedScoreNode rootNode, String domainName){
        this.userId = userId;
        this.domainSessionId = domainSessionId;
        this.time = time;
        this.rootNode = rootNode;
        this.domainName = domainName;
    }
    
    //primary key, auto-generated sequentially
    @Id
    @Column(name="scoreMetadataId_PK")
    @TableGenerator(name="scoremetadataid", table="scoremetadatapktb", pkColumnName="scoremetadatakey", pkColumnValue="scoremetadatavalue", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE, generator="scoremetadataid")
    public int getScoreMetadataId() {
        return scoreMetadataId;
    }
    public void setScoreMetadataId(int scoreMetadataId) {
        this.scoreMetadataId = scoreMetadataId;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    
    public int getDomainSessionId() {
        return domainSessionId;
    }

    public void setDomainSessionId(int domainSessionId) {
        this.domainSessionId = domainSessionId;
    }
    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @OneToOne
    @JoinColumn(name="rootNode_gradedScoreNodeId_FK")
    public GradedScoreNode getRoot() {
        
        return rootNode;
    }
    
    public void setRoot(GradedScoreNode rootNode) {
        
        this.rootNode = rootNode;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ScoreMetadata:");
        sb.append(", id = ").append(getScoreMetadataId());
        sb.append(", time = ").append(getTime());
        sb.append(", user = ").append(getUserId());
        sb.append(", domainSession = ").append(getDomainSessionId());
        sb.append(", root node = ").append(getRoot());
        sb.append(", domainName = ").append(getDomainName());
        sb.append("]");
        
        return sb.toString();
    }
}
