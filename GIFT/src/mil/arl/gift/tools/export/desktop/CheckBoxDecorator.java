/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * Decorates a component with a checkbox.
 * @author cdettmering
 */
public class CheckBoxDecorator extends JPanel implements TreeCellRenderer {
		
	/** Generated serial */
	private static final long serialVersionUID = -8850648932319152321L;

	/** The original renderer that will be decorated */
	private TreeCellRenderer original;
	
	/** Checkbox used for decorating */
	private JCheckBox checkbox;
	
	/**
	 * Creates a new CheckBoxDecorator that will decorate original.
	 * @param original The original renderer to be decorated.
	 */
	public CheckBoxDecorator(TreeCellRenderer original) {
		super();
		this.original = original;
		
		setLayout(new BorderLayout());
		checkbox = new JCheckBox();
		setOpaque(false);
		checkbox.setOpaque(false);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		// Grab the original component rendered to the tree
		Component originalRender = original.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		
		try {
			CheckBoxNode node = (CheckBoxNode)value;
			checkbox.setSelected(node.isSelected());
		
			// Decorate it with a checkbox
			removeAll();
			add(checkbox, BorderLayout.WEST);
			add(originalRender, BorderLayout.CENTER);
			revalidate();
		} catch(@SuppressWarnings("unused") ClassCastException e) {
			
			// Decorate it without a checkbox
			removeAll();
			add(originalRender, BorderLayout.CENTER);
			revalidate();
		}
		
		return this;
	}

}
