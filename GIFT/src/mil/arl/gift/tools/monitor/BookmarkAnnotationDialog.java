/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import mil.arl.gift.common.io.ImageUtil;

/**
 * Extension of JDialog to allow editing of bookmark annotations.
 * 
 * @author cragusa
 *
 */
class BookmarkAnnotationDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    /** Constant indicating the OKAY button was pressed by the user */
    public static final int OKAY   = 1;
    
    /** Constant indicating the CANCEL button was pressed by user */
    public static final int CANCEL = 0;
    
    //TODO: (Optional) Put these dimensions in properties file
    
    /** Number of columns in the dialog's text area used for editing */
    private static final int TEXT_AREA_COLUMN_COUNT = 50;
    
    /** Number of rows in the dialog's text area used for editing */
    private static final int TEXT_AREA_ROW_COUNT = 5;
    
    /** variable to hold the return result of the dialog box */
    private int result;
    
    /** Text area used for typing by the user */
    private JTextArea textArea = new JTextArea();    
    
    /**
     * Patch the component so that the tab key moves the focus to the next component
     * 
     * @param c the component to patch
     */
    private static void applyTabKeyPatch(Component c) {

        Set<KeyStroke> strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
        
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
        
        strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
        
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);        
    }

    /** common cancel action */
    private void cancelAction() {
        result = CANCEL;
        BookmarkAnnotationDialog.this.setVisible(false);                
    }
    
    /** common okay action */
    private void okayAction() { 
        result = OKAY;
        BookmarkAnnotationDialog.this.setVisible(false);
    }
    
    /**
     * Constructs a new BookmarkAnnotationDialog
     * 
     * @param frame the frame that is the container (parent/grandparent) of the dialog.
     */
    BookmarkAnnotationDialog(JFrame frame) {
        
        super(frame, "Edit Bookmark Annotation");
        
        setLayout(new BorderLayout());
        
        frame.setIconImage(ImageUtil.getInstance().getSystemIcon());

        textArea.setColumns(TEXT_AREA_COLUMN_COUNT);
        textArea.setRows(TEXT_AREA_ROW_COUNT);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        
        applyTabKeyPatch(textArea);        
        
        JScrollPane scrollPane = new JScrollPane(textArea);    
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                
        final int BORDER_WIDTH = 7;
        Border border1 = BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH - 5, BORDER_WIDTH);
        Border border2 = BorderFactory.createLineBorder(Color.GRAY);        
        Border border3 = BorderFactory.createCompoundBorder(border1, border2);        
        scrollPane.setBorder(border3);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        JButton okayButton = new JButton("Okay");        
        ActionListener okayActionListener = new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent event) {
                okayAction();
            }
        };
        
        okayButton.addActionListener(okayActionListener);        
        
        JButton cancelButton = new JButton("Cancel");
        
        ActionListener cancelActionListener = new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelAction();
            }
        };
        
        cancelButton.addActionListener(cancelActionListener);        
        
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);        
        textArea.registerKeyboardAction(okayActionListener, enterKeyStroke, JComponent.WHEN_FOCUSED);
        
        KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);        
        textArea.registerKeyboardAction(cancelActionListener, escKeyStroke, JComponent.WHEN_FOCUSED);
        
        bottomPanel.add(okayButton);
        bottomPanel.add(cancelButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        setModal(true);        
        setResizable(false);
        pack();        
    }
  
    /**
     * Show the dialog using the provided text.
     * 
     * @param text the text used to populate the text area for editing.
     */
    void showDialog(String text) {
        
        textArea.setText(text);  
        
        Rectangle dialogRect = this.getBounds();
        Rectangle parentRect = this.getParent().getBounds();

        int x = parentRect.x + (parentRect.width - dialogRect.width)/2;
        int y = parentRect.y + (parentRect.height - dialogRect.height)/2;
        
        this.setLocation(new Point(x,y));
        
        this.setVisible(true);
    }
    
    /**
     * Get the text from the dialog.
     * @return the text from the dialog
     */
    String getText() {        
        return textArea.getText();
    }    
    
    /**
     * Get the result from the dialog indicating whether the user clicked OKAY or CANCEL.
     * 
     * @return the value OKAY if the user accepts the edits, or CANCEL if the user wants to cancel the edits.
     */
    int getResult() {        
        return result;
    }
}
