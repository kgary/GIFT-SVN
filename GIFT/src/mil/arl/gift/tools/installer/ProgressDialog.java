/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.ImageUtil;

/**
 * Simple progress dialog
 * 
 * @author cdettmering
 *
 */
public class ProgressDialog extends JDialog implements PropertyChangeListener {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(ProgressDialog.class);
	
	private static final Image ICON = ImageUtil.getInstance().getSystemIcon();
	
	/** Generated serial */
	private static final long serialVersionUID = 1081688438526312762L;

	private static final String TITLE = "Installing";
	private static final Dimension SIZE = new Dimension(260, 140);
	private JProgressBar bar;
	private JProgressBar sub;
	private JLabel subLabel;
	private JButton cancelButton;
	private InstallThread task;
	
	/**
	 * Creates a new ProgressDialog that tracks the progress of task.
	 * 
	 * @param task The task to visually update.
	 */
	public ProgressDialog(InstallThread task) {
		this.task = task;
		task.addPropertyChangeListener(this);
		setupUi();
		if(ICON == null) {
			logger.error("Could not load GIFT icon.");
		}
	}
	
	/**
	 * Gets called when task has been updated
	 * 
	 * @param arg0 Not used.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		int progress = task.getProgress();
		int subProgress = task.getSubProgress();
				
		if(progress == -1) {
			bar.setIndeterminate(true);
			sub.setStringPainted(false);
		} else if(progress != -1) {
			bar.setIndeterminate(false);
			bar.setValue(task.getProgress());
			sub.setStringPainted(true);
		}
		
		if(subProgress == -1) {
			sub.setIndeterminate(true);
			sub.setStringPainted(false);
		} else if(subProgress != -1) {
			sub.setIndeterminate(false);
			sub.setValue(task.getSubProgress());
			sub.setStringPainted(true);
		}
		
		subLabel.setText(task.getSubtaskString());
		
		if(progress == 100) {
			JOptionPane.showMessageDialog(this, task.getSubtaskString(), "Install Complete", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(ICON));			
            
            //Open GIFT Readme after user closes above dialog
            if(InstallThread.NEW_GIFT_README_FILE.exists()){
                
                try {
                    String output = InstallThread.executeAndWaitForCommand(new String[]{"cmd.exe", "/c", "start", "notepad", InstallThread.NEW_GIFT_README_FILE.getAbsolutePath()});
                    logger.debug("Open GIFT readme output = "+output+".");
                } catch (Exception e) {
                    logger.error("Caught exception while trying to open the GIFT Readme file. Moving on...", e);
                }
            }else{
                logger.error("Unable to find the GIFT Readme at "+InstallThread.NEW_GIFT_README_FILE+".");
            }
            
			System.exit(0);
		}
	}
	
	/**
	 * Sets up the progress dialog UI
	 */
	private void setupUi() {
		setTitle(TITLE);
		setSize(SIZE);
		setMinimumSize(SIZE);
		setIconImage(ICON);
		setLocationRelativeTo(null);
		
		// Make the red X cancel
		WindowListener exitListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				task.cancel();
			}
		};
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(exitListener);
		
		/** Progress */
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setAlignmentX(Component.CENTER_ALIGNMENT);
		sub = new JProgressBar();
		subLabel = new JLabel();
		subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
		sep1.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(subLabel);
		panel.add(sub);
		panel.add(sep1);
		JLabel overall = new JLabel("Overall Install");
		overall.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(overall);
		panel.add(bar);
		/** End Progress */
		
		/** Cancel button */
		JPanel cancelPanel = new JPanel();
		cancelPanel.add(Box.createHorizontalGlue());
		cancelPanel.setLayout(new BoxLayout(cancelPanel, BoxLayout.X_AXIS));
		cancelButton = new JButton("Cancel");
		cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				task.cancel();
			}
			
		});
		cancelPanel.add(cancelButton);
		/** End cancel button */
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(panel);
		getContentPane().add(new JSeparator(SwingConstants.VERTICAL));
		getContentPane().add(cancelPanel);
	}

}
