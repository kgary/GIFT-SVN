/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The JOptionPaneUtil class provides utility functions for interfacing
 * with the JOptionPane object.  This class is meant to provide static utility methods
 * for interfacing with the JOptionPane class.
 * 
 * @author nblomberg
 *
 */
public class JOptionPaneUtil {
	
	/**
	 * Displays a JOptionPane confirm dialog that is centered and forced to the 'top' so it is shown in front
	 * of other windows.  This can be used in cases where there is no parent JFrame and a dialog
	 * is needed to be shown in front of other windows.
	 * 
	 * @param message - The message for the confirmation dialog.
	 * @param title - The title of the confirmation dialog.
	 * @param optionType - Must be one of an existing JOptionPane option type (such as JOptionPane.OK_CANCEL_OPTION)
	 * @param messageType - Must be one of the the JOptionPane messageType (such as JOptionPane.WARNING_MESSAGE)
	 * @return int - Integer defining the return 
	 * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true.
	 */
    public static int showConfirmDialog(Object message, String title, int optionType, int messageType) throws HeadlessException {
        int choice = 0;
        
        JFrame frmConfirm = new JFrame();
        
        // Hide the jframe.
        frmConfirm.setVisible(false);
        
        // Set the jframe to be on top.
        frmConfirm.setAlwaysOnTop(true);
        
        // This centers the jframe.
        frmConfirm.setLocationRelativeTo(null);
        
        // Bring up the confirmation dialog with the jframe as the parent (which forces the dialog to be in front/on top).
        choice = JOptionPane.showConfirmDialog(frmConfirm, 
                message, 
                title, 
                optionType, 
                messageType);

        // Clean up the jframe.
        frmConfirm.dispose();
        
        // Return the choice that was selected.
        return choice;
    }
    
    /**
     * Displays a JOptionPane option dialog that is centered and forced to the 'top' so it is shown in front
     * of other windows.  This can be used in cases where there is no parent JFrame and a dialog
     * is needed to be shown in front of other windows.
     * 
     * @param message - The message for the confirmation dialog.
     * @param title - The title of the confirmation dialog.     
     * @param messageType - Must be one of the the JOptionPane messageType (such as JOptionPane.WARNING_MESSAGE)
     * @param yesOkOptionText - the text to show for the Yes/OK button.  If null/empty, a Yes/OK choice will not be returned.
     * @param noOptionText - the text to show for the No button.  If null/empty, a No choice will not be returned.
     * @param cancelOptionText - the text to show for the Cancel button.  If null/empty, a Cancel choice will not be returned.
     * @return int - Integer defining the return 
     * cases:
     * if yesOk & no & cancel = {yes, no, cancel}
     * if yesOk & no = {yes, no}
     * if yesOk & cancel = {ok, cancel}
     * if yesOk = {yes}
     * if no = {no}
     * if cancel = {cancel}
     * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true.
     */
    public static int showOptionDialog(Object message, String title, int messageType, String yesOkOptionText, String noOptionText, String cancelOptionText) throws HeadlessException {
        int choice = 0;
        
        JFrame frmConfirm = new JFrame();
        
        // Hide the jframe.
        frmConfirm.setVisible(false);
        
        // Set the jframe to be on top.
        frmConfirm.setAlwaysOnTop(true);
        
        // This centers the jframe.
        frmConfirm.setLocationRelativeTo(null);
        
        Object[] options;
        List<String> optionslist = new ArrayList<>();
        boolean yesOption = false, noOption = false, cancelOption = false;
        if(yesOkOptionText != null && !yesOkOptionText.isEmpty()){
            optionslist.add(yesOkOptionText);
            yesOption = true;
        }
        
        if(noOptionText != null && !noOptionText.isEmpty()){
            optionslist.add(noOptionText);
            noOption = true;
        }
        
        if(cancelOptionText != null && !cancelOptionText.isEmpty()){
            optionslist.add(cancelOptionText);
            cancelOption = true;
        }
        
        options = optionslist.toArray();
        
        int optionType;
        if(yesOption && noOption && cancelOption){
            optionType = JOptionPane.YES_NO_CANCEL_OPTION;
        }else if(yesOption && noOption){
            optionType = JOptionPane.YES_NO_OPTION;
        }else if(yesOption && cancelOption){
            optionType = JOptionPane.OK_CANCEL_OPTION;
        }else if(yesOption){
            optionType = JOptionPane.YES_OPTION;
        }else if(cancelOption){
            optionType = JOptionPane.CANCEL_OPTION;
        }else if(noOption){
            optionType = JOptionPane.NO_OPTION;
        }else{
            throw new IllegalArgumentException("Unable to determine the type of options to show in the option dialog for yesOkOptionText = "+yesOkOptionText+", noOptionText = "+noOptionText+", cancelOptionText = "+cancelOptionText+".");
        }
        
        // Bring up the confirmation dialog with the jframe as the parent (which forces the dialog to be in front/on top).
        choice = JOptionPane.showOptionDialog(frmConfirm,
                message, 
                title, 
                optionType,
                messageType,
                null,
                options,
                null);

        // Clean up the jframe.
        frmConfirm.dispose();
        
        // Return the choice that was selected.
        return choice;
    }
}