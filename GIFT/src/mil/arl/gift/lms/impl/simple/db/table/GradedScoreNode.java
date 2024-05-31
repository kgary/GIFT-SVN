/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db.table;

import java.util.Set;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@Entity
@Table(name="gradedscorenode")
public class GradedScoreNode extends AbstractScoreNode  {
    
    //TODO: changed this to DomainSession once LMS and UMS are merged
    private int domainSessionId;
    
    private String grade;
    
    private Set<AbstractScoreNode> children;
    
    /**
     * Default constructor - required by hibernate
     */
    public GradedScoreNode(){
        
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param name the name of the node
     * @param parent the parent node to this node.  Can be null.
     * @param grade the grade for this node
     * @param domainSessionId the domain session id that created this score node
     */
    public GradedScoreNode(String name, AbstractScoreNode parent, String grade, int domainSessionId){
        super(name, parent);
        this.grade = grade;
        this.domainSessionId = domainSessionId;
    }

    public int getDomainSessionId() {
        return domainSessionId;
    }

    public void setDomainSessionId(int domainSessionId) {
        this.domainSessionId = domainSessionId;
    }

    //can't be null
    @Column(nullable=false)
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    //created bi-directional 
    //now when GradedScoreNode is deleted, the children are deleted
    @OneToMany(targetEntity=AbstractScoreNode.class, mappedBy="parent", cascade=CascadeType.ALL/*, fetch=FetchType.LAZY*/)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<AbstractScoreNode> getChildren() {
        return children;
    }
    public void setChildren(Set<AbstractScoreNode> children) {
        this.children = children;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GradedScoreNode:");
        sb.append(super.toString());
        sb.append(", value = ").append(getGrade());
        sb.append(", domainSession = ").append(getDomainSessionId());
        sb.append("]");
        
        return sb.toString();
    }
}
