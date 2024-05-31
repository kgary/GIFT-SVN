/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A learner state outline represents the learner's current state in both 
 * tree form and table form.  This class displays data in the form of a tree.
 * 
 * @author mzellars
 *
 */
public class LearnerStateTreeModel implements TreeModel {
	
	 private LearnerStateNode<String> root;

     public LearnerStateTreeModel(LearnerStateNode<String> root) {
         this.root = root;
     }
     
     @Override
     public Object getRoot() {
         return root;
     }

     @Override
     public Object getChild(Object parent, int index) {
         @SuppressWarnings("unchecked")
			LearnerStateNode<String> n = (LearnerStateNode<String>)parent;
         
         return n.getChildren().get(index);
     }

     @Override
     public int getChildCount(Object parent) {
         @SuppressWarnings("unchecked")
			LearnerStateNode<String> n = (LearnerStateNode<String>)parent;
         
         return n.getChildren().size();
     }

     @Override
     public boolean isLeaf(Object node) {
     	@SuppressWarnings("unchecked")
			LearnerStateNode<String> n = (LearnerStateNode<String>)node;
     	
     	return n.getChildren().isEmpty();
     }

     @Override
     public void valueForPathChanged(TreePath path, Object newValue) {
         // do nothing
     }

     @Override
     public int getIndexOfChild(Object parent, Object child) {
     	@SuppressWarnings("unchecked")
			LearnerStateNode<String> p = (LearnerStateNode<String>)parent;
     	@SuppressWarnings("unchecked")
			LearnerStateNode<String> c = (LearnerStateNode<String>)child;
     	
     	for (int i=0; i<p.getChildren().size(); i++) {
     		if (p.getChildren().get(i) == c) {
     			return i;
     		}
     	}
     	
     	return -1;
     }

     @Override
     public void addTreeModelListener(TreeModelListener l) {
         // do nothing
     }

     @Override
     public void removeTreeModelListener(TreeModelListener l) {
         // do nothing
     }
}
