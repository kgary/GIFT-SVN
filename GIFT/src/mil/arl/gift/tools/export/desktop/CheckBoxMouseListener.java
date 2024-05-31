/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.DesktopFolderProxy;

/**
 * Listens to mouse clicks on the checkboxes.
 * @author cdettmering
 *
 */
public class CheckBoxMouseListener extends MouseAdapter {
    
    /** Logger instance */
    private static Logger logger = LoggerFactory.getLogger(CheckBoxMouseListener.class);

	/** The tree that contains the CheckBoxNode objects */
	private JTree tree;
	
	/** Listens to selections and de-selections */
	private DomainSelectionListener listener;
	
	/**
	 * Creates a new CheckBoxMouseListener that listens to events on tree.
	 * 
	 * @param tree The file tree
	 * @param listener Listens to selections and de-selections 
	 */
	public CheckBoxMouseListener(JTree tree, DomainSelectionListener listener) {
		this.tree = tree;
		this.listener = listener;
	}
	
	/**
	 * Handles mouse pressed events. This will either check or uncheck the checkbox.
	 * 
	 * @param e The MouseEvent given by Java.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		
		if(path == null) {
			return;
		}
		
		CheckBoxNode node = (CheckBoxNode) path.getLastPathComponent();
		
		if(node == null) {
			return;
		}
		
		// Update tree
		node.setSelected(!node.isSelected());
		try{
    		if(node.isSelected()) {
    			listener.exportFileSelected(new DesktopFolderProxy((File)node.getUserObject()));
    		} else {
    			listener.exportFileUnselected(new DesktopFolderProxy((File)node.getUserObject()));
    		}
		}catch(Exception exception){
		    logger.error("Caught exception while trying to update the tree for "+node.getUserObject(), exception);
		    JOptionPane.showConfirmDialog(null, 
		            "There was a problem updating the checkbox for "+node.getUserObject()+".", 
		            "Failed to update checkbox", 
		            JOptionPane.OK_OPTION, 
		            JOptionPane.ERROR_MESSAGE);
		}
		tree.treeDidChange();
	}
	
}
