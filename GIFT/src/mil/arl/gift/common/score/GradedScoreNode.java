/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ScoreNodeTypeEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * ScoreNode that contains a graded value. Graded Values are derived by rolling up the grades/assessments of child nodes.
 * 
 * In a completed tree, GradedScoreNodes should not be leaf nodes.  GradedScoreNodes can be leaves only during tree construction.
 * 
 * @author cragusa
 *
 */
public class GradedScoreNode extends AbstractScoreNode implements Serializable {
        
    /**
     *  default
     */
    private static final long serialVersionUID = 1L;
       
    /**
     * Data structure to hold child nodes.
     */
    private List<AbstractScoreNode> children = new ArrayList<>();    
    
    /**
     * White space for indenting nested rows of hierarchy when printing
     * 
     */    
    private static final String INDENT = "   ";
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    @SuppressWarnings("unused")
    protected GradedScoreNode() { }

    /**
     * Constructor 
     * 
     * @param name - the name of the scorer node.
     * @param assessmentLevel - the assessment level given to the score node. can't be null.
     */
    public GradedScoreNode(String name, AssessmentLevelEnum assessmentLevel) {
        super(name, assessmentLevel);
    } 
    
    /**
     * Constructor used when the grade is not known yet.
     *  
     * @param name - the name of the scorer node.
     */
    public GradedScoreNode(String name){
        this(name, AssessmentLevelEnum.UNKNOWN);
    }
    
    /**
     * Creates a new instance copy of the @link {@link GradedScoreNode} provided.
     * @param original the {@link GradedScoreNode} to copy.  If null, null is returned.
     * @return the new deep copy {@link GradedScoreNode}
     */
    public static GradedScoreNode deepCopy(GradedScoreNode original){
        
        if(original == null){
            return null;
        }
        
        /*
         * NOTE: if you change this logic make sure to consider TaskScoreNode also
         */
        GradedScoreNode copy = new GradedScoreNode(original.getName());
        copy.setParent(original.getParent());
        if(original.getPerformanceNodeId() != null){
            copy.setPerformanceNodeId(original.getPerformanceNodeId());
        }
        
        copy.setAssessment(original.getAssessment());
        copy.setEvaluator(original.getEvaluator());
        copy.setObserverComment(original.getObserverComment());
        copy.setObserverMedia(original.getObserverMedia());
        
        deepCopyChildren(original, copy);
        
        return copy;
    }
    
    /**
     * Populates the children of the copy being built with new instances of the children from
     * the original provided.
     * @param original the {@link #GradedScoreNode()} to copy its children.  If null, this method does
     * nothing.
     * @param copyBeingBuilt where to add the new children. Can't be null.
     */
    protected static void deepCopyChildren(GradedScoreNode original, GradedScoreNode copyBeingBuilt) {
        
        if(original == null) {
            return;
        }else if(copyBeingBuilt == null) {
            throw new IllegalArgumentException("The copy being built is null");
        }
        
        for(AbstractScoreNode originalChild : original.getChildren()){
            
            AbstractScoreNode copyChild;
            if(originalChild instanceof TaskScoreNode){
                copyChild = TaskScoreNode.deepCopy((TaskScoreNode)originalChild);
            } else if(originalChild instanceof GradedScoreNode){
                copyChild = GradedScoreNode.deepCopy((GradedScoreNode)originalChild);
            }else if(originalChild instanceof RawScoreNode){
                copyChild = RawScoreNode.deepCopy((RawScoreNode)originalChild);
            }else{
                // found a score node type that is not handled at this time
                throw new RuntimeException("Found unhandled score node type as a child of '"+original.getName()+"' node.\n"+originalChild);
            }
            
            copyBeingBuilt.addChild(copyChild);
        }
    }
    
    /**
     * Removes any children nodes to this node that have other usernames and not the username provided.
     * This will recursively traverse the hierarchy.  If a child to this node is also a parent, then
     * that parent could be removed if it no longer has any children.  If a {@link RawScoreNode} has
     * no usernames then it is kept.
     * @param username the username to search for score nodes that are applicable.  If null or empty
     * this method does nothing.
     */
    public void removeUnrelatedScores(String username){
        
        if(StringUtils.isBlank(username)){
            return;
        }
        
        Iterator<AbstractScoreNode> itr = children.iterator();
        while(itr.hasNext()){
            AbstractScoreNode node = itr.next();
            
            if(node instanceof RawScoreNode){
                RawScoreNode rawNode = (RawScoreNode)node;
                if(rawNode.getUsernames() != null && !rawNode.getUsernames().contains(username)){
                    // there are other users defined at this leaf node but not this user, remove
                    // this node because its score doesn't apply to the user
                    itr.remove();
                }
            }else if(node instanceof GradedScoreNode){
                ((GradedScoreNode)node).removeUnrelatedScores(username);
                
                if(((GradedScoreNode)node).getChildren().isEmpty()){
                    // all of the children have been removed, therefore remove this node because
                    // it provides nothing useful now
                    itr.remove();
                }
            }
        }
    }
    
    /**
     * Convenience method to determine if the node has children;
     * 
     * @return boolean If this node is a leaf.
     */
    @Override
    public boolean isLeaf() {
        
        return children.isEmpty();
    }    
    
    /**
     * Getter method to get all the children.
     * 
     * @return all the children of this node.
     */
    public List<AbstractScoreNode> getChildren() {        
        return children;
    }
    
    /**
     * Adds a ScoreNode child to this node. 
     * 
     * @param node the child node to add.
     */
    public void addChild(AbstractScoreNode node) {
        
        if( node == null || node.getName() == null ) {
            
            throw new IllegalArgumentException("The node or node name can't be null.");
        }

            
        children.add(node);        
        node.setParent(this);        
    }
    
    /**
     * Replaces this node's ScoreNode child at the given index with the given node. This 
     * can be useful for overriding existing score nodes with injected scores, such as
     * those provided by an OC.
     * 
     * @param index the index of the child to replace.
     * @param node the child node to replace the original with.
     */
    public void setChild(int index, AbstractScoreNode node) {
        
        if( node == null || node.getName() == null ) {
            
            throw new IllegalArgumentException("The node or node name can't be null.");
        }

            
        children.set(index, node);
        node.setParent(this);
    }
    
    @Override
    protected String toDisplayString(String indent) {
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(super.toDisplayString(indent));
        
        buffer.append(": ").append(getAssessment().getDisplayName());
        
        if( !isLeaf() ) { //has children
            
            for(AbstractScoreNode node : children ) {  
                String indentStr = indent + INDENT;
                buffer.append("\n");
                buffer.append(node.toDisplayString(indentStr));
            }
        }
        
        return buffer.toString();
    }
    
    @Override
    public String toDisplayString() {    
        return toDisplayString("");
    }

    /**
     * Get the grade for this node as a string.
     * @return String The grade for this node as a string.
     */
    public String getGradeAsString() {        
        return getAssessment().getDisplayName();
    }
    
    /**
     * Return the grade for the score node with the given name that is a descendant
     * of this node.  The node could be another GradedScoreNode or another type like a RawScoreNode.
     * 
     * @param nodeName the name of a node to find under this node.  Can't be null or empty.
     * @return the grade of the first node under this node that has the given name.  Can be null.
     */
    public AssessmentLevelEnum getGradeByName(String nodeName){
        
        if(nodeName == null || nodeName.isEmpty()){
            throw new IllegalArgumentException("The node name can't be null or empty.");
        }
        
        if(getName().equals(nodeName)){
            return getAssessment();
        }else{
            //check descendants
            
            AssessmentLevelEnum assessment = null;
            for(AbstractScoreNode child : getChildren()){
                
                if(child instanceof GradedScoreNode){
                    assessment = ((GradedScoreNode)child).getGradeByName(nodeName);
                }else if(child instanceof RawScoreNode){
                    assessment = ((RawScoreNode)child).getAssessment();
                }
                
                if(assessment != null){
                    break;
                }
            }
            
            return assessment;
        }
    }    
    
    /**
     * Populated the provided collection with any descendant graded score nodes
     * that don't have a Passing score (e.g. PassFailEnum.PASS).
     * @param nonPassingDescendants the collection to populate.  If null this method does nothing.
     */
    public void getNonPassingNodeDescendants(Set<GradedScoreNode> nonPassingDescendants){
        
        if(nonPassingDescendants == null){
            return;
        }
        
        for(AbstractScoreNode child : getChildren()){
            
            if(child instanceof GradedScoreNode){
                GradedScoreNode node = (GradedScoreNode)child;
                
                if(!node.meetsStandard()){
                    nonPassingDescendants.add(this);
                }
                node.getNonPassingNodeDescendants(nonPassingDescendants);
            }

        }
    }
    
    /**
     * Return whether the assessment for this node meets or exceeds the standards.
     * @return see {@link AssessmentLevelEnum.hasReachedStandards()}
     */
    public boolean meetsStandard() {
        return getAssessment() != null && getAssessment().hasReachedStandards();
    }
  
    /**
     * Update the assessment for this node.
     * 
     * @param assessment the assessment for this node.  Can't be null.
     */
    public void updateAssessment(AssessmentLevelEnum assessment){
        setAssessment(assessment);
    }    
    
    @Override
    public ScoreNodeTypeEnum getType() {        
        return ScoreNodeTypeEnum.GRADED_SCORE_NODE;
    }
    
    
    @Override
    public String toString() {
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("GradedScoreNode: ");
        buffer.append(super.toString());
        
        buffer.append(", children=[");
        
        if( !this.isLeaf() ) {
   
            int childCount = 0;
            
            for( AbstractScoreNode child : children ) {
                
                if(childCount++ > 0) {
                    
                    buffer.append("; ");
                }
                
                buffer.append(child.toString());
            }
        }        
        buffer.append("]");
        
        buffer.append("}");
        
        return buffer.toString();
    }

	@Override
	public boolean isValid() {
		
		if( isLeaf() ) {
			return false;
		}else {
						
            for( AbstractScoreNode child : children ) {
            	
            	if(!child.isValid()) {            		
            		return false;
            	}
            }
		}
		
		return true;
	}
    
}




