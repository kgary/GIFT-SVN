/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.ImageProperties;

/**
 * This class displays a loading dialog and progress bar. 
 * 
 * @author bzahid
 */
public class ProgressDialog {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProgressDialog.class);
    
	/** the desired dimensions of the splash image */
	private static final int IMG_WIDTH  = 400;
	private static final int IMG_HEIGHT = 264;
	
	/** the amount of time to sleep in between incrementing the progress bar to create a smooth animated effect */
	private static final int LONG_SLEEP = 80;
	private static final int SHORT_SLEEP = 40;
		
	/** the maximum amount of progress to increment using a short sleep time */
	private static final int THRESHOLD = 20;
	
	/** shared ui components */
	JWindow frame = null;
	JLabel statusLabel = null;
	JProgressBar progressBar = null;
	
	/**
	 * Constructor
	 */
	public ProgressDialog() {
		initComponents();
	}
	
	/**
	 * Initializes the dialog components
	 */
	private void initComponents() {
		
		frame = new JWindow();
		statusLabel = new JLabel();
		progressBar = new JProgressBar(0, 100);
		
		JPanel imageLayer = new JPanel();
		JPanel statusLayer = new JPanel();
		JPanel progressLayer = new JPanel();
		JLayeredPane layeredPane = new JLayeredPane();
		URL url = ProgressDialog.class.getResource("/" + ImageProperties.getInstance().getPropertyValue(ImageProperties.APP_LOADING));
		Image bgImage = new ImageIcon(url).getImage();				
		JLabel imgLabel = new JLabel(new ImageIcon(bgImage.getScaledInstance(IMG_WIDTH, IMG_HEIGHT, Image.SCALE_SMOOTH)));
		
		// Style the progress bar
		progressBar.setBorderPainted(false);
		progressBar.setForeground(new Color(160,167,162));
		progressBar.setBackground(new Color(80,87,82));
		progressBar.setPreferredSize(new Dimension(230, 8));
		
		// Style the status label
		statusLabel.setPreferredSize(new Dimension(IMG_WIDTH - 60, 50));
//		statusLabel.setForeground(new Color(150,157,152));
		statusLabel.setForeground(new Color(69,70,69));
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
        // Make all windows transparent
        imageLayer.setOpaque(false);
        statusLayer.setOpaque(false);
        progressLayer.setOpaque(false);
        frame.setBackground(new Color(0,0,0,0));
        
        // Setting bounds is required by JLayeredPane
        statusLayer.setBounds(18, -10, IMG_WIDTH - 60, 50);
        progressLayer.setBounds(75, 243, 200, 100);
        frame.setBounds(0, 0, IMG_WIDTH, IMG_HEIGHT);
        imageLayer.setBounds(0, 0, IMG_WIDTH, IMG_HEIGHT);
                
        // Add content
        imageLayer.add(imgLabel);
        statusLayer.add(statusLabel);
        progressLayer.add(progressBar);
        
        layeredPane.add(statusLayer, JLayeredPane.MODAL_LAYER);
        layeredPane.add(imageLayer, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(progressLayer, JLayeredPane.PALETTE_LAYER);
        
        frame.setContentPane(layeredPane);
	}
	
	/**
	 * Displays a status message in the dialog and increments the progress bar to the specified amount.
	 * If the amount is 100 or greater, the dialog will close.
	 * 
	 * @param status - the message to display
	 * @param progress - the progress value to display. Must be a positive integer.
	 * 
	 * @throws IllegalArgumentException if the progress amount is negative.
	 */
	public void update(String status, int progress) throws IllegalArgumentException {
		
		if(progress < 0) {
			throw new IllegalArgumentException("Cannot update progress bar with a value less than 0");
		}
		
		if(status != null) {
			statusLabel.setText(status);
		}

		if(progress < 100) {

			int sleep = SHORT_SLEEP;
			if(progress > getCurrentProgress()) {
				progress = progress - getCurrentProgress();
				if(progress > THRESHOLD) {
					// sleep for longer if there is a lot of progress to be made
					sleep = LONG_SLEEP;
				}
			}

			for(int i = 0; i < progress; i++) {
				progressBar.setValue(getCurrentProgress() + 1);
				try {
					// allow the progress bar to increment steadily
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					logger.error("Caught exception while updating progress", e);
				}
			}
		} else {
			frame.dispose();
		}

	}
	
	/** 
	 * Gets the current progress amount 
	 *
	 * @return the current progress amount
	 */
	public int getCurrentProgress() {
		return (int) (Math.round(progressBar.getPercentComplete() * 100));
	}
	
	/**
	 * Displays the progress dialog
	 */
	public void show() {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	/**
	 * Closes the progress dialog
	 */
	public void hide() {
		frame.dispose();
	}
	
}
