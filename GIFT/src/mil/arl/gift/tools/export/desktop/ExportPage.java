/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content panel that holds the initial GUI for exporting GIFT into  a single
 * executable
 * 
 * @author cdettmering
 */
public class ExportPage extends WizardPage {
	
	private static final long serialVersionUID = 0;

	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(ExportPage.class);
	
	/** dialog labels */
	private static final String TITLE = "Export";
	private static final String DESCRIPTION = "Choose Output File";
	private static final String BROWSE = "Save As ...";
	
	private static final String ZIP_FILE_EXT = "zip";
	
	/** Allows the user to choose the directory to export GIFT into */
	private JFileChooser fileChooser;
	
	/** Shows the directory currently selected, and allows for manual typing */
	private JTextField directoryField;
	
	/** Browse button, opens file chooser */
	private JButton browseButton;
	
	/** 
	 * Creates a new ExportPanel 
	 */
	public ExportPage() {
		super(TITLE, DESCRIPTION);
		setupUi();
	}
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		if(directoryField.getText().length() > 0) {
			setNextEnabled(true);
		} else {
			setNextEnabled(false);
		}
	}
	
	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		settings.put(ExportSettings.getOutputFile(), directoryField.getText());
	}
	
	/**
	 * Sets up all of the swing components
	 */
	private void setupUi() {
		
		// Setup File Chooser
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Zip Archives (*.zip)", "zip"));
		
		// Setup directory field
		directoryField = new JTextField();
		directoryField.setEditable(false);
		
		// Setup browse button
		browseButton = new JButton();
		browseButton.setText(BROWSE);
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				File file = openFileDialog();
				
				if(file != null){
    	            String path = file.getAbsolutePath();
    	            path = verifyFileExtension(path, ZIP_FILE_EXT);
    	            logger.info("Chose file of '" + path + "' to save exported contents too.");
    	            directoryField.setText(path);
    	            setNextEnabled(true);
				}
			}
			
		});
		
		// Setup layout
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addComponent(directoryField)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(browseButton))
		);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(directoryField)
							.addComponent(browseButton))
		);
	}
	
	/**
	 * Handles opening the file dialog, that allows the user to choose a
	 * directory.
	 * 
	 * @return File - the file to save too, can be null if no file was provided
	 */
	private File openFileDialog() {
	    
		logger.info("Opening choose output file dialog.");
		int returnVal = fileChooser.showSaveDialog(this);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) {
		    
			File file = fileChooser.getSelectedFile();
			
			//if the file name was typed instead of selected, it may not have the file extension in the name
			if(!file.getName().endsWith("."+ZIP_FILE_EXT)){
			    //add the extension to complete the file name
			    file = new File(file.getAbsolutePath() + "." + ZIP_FILE_EXT);
			}
			
			if(file.exists()){
			    int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to overwrite "+file.getAbsolutePath()+"?", "Overwrite file", JOptionPane.YES_NO_OPTION);
			    
			    if(choice == JOptionPane.YES_OPTION){
			        logger.info("User chose to overwrite existing file at "+file.getAbsolutePath());
			    }else{
			        logger.info("User chose to not overwrite existing file at "+file.getAbsolutePath());
			        return openFileDialog();
			    }
			} else {
				//Test right now to make sure we can write this file. 
				//There's also a chance that the user won't be able to write over the existing file, but we'll catch that at the end. 
				try {				
					if( file.createNewFile() ) {
						file.delete();
					}
				} catch(IOException ex) {
					logger.warn("Unable to create file: " + file.getAbsolutePath(), ex);
					JOptionPane.showMessageDialog(null, "Unable to create file: " + 
							file.getAbsolutePath() + "\n This may be a permissions issue." +
							"\n Try putting the file in a different folder.", 
							"Warning", JOptionPane.WARNING_MESSAGE);	
					file = null;
				}
			}
						
            return file;
			
		} else {
			logger.info("Choose output file dialog cancelled.");
			return null;
		}
	}
	
	/**
	 * Verifies that path ends with extension and adds it if necessary
	 * 
	 * @param path The path to the file in question
	 * @param extension The file extension to check for
	 * @return Path with file extension
	 */
	private String verifyFileExtension(String path, String extension) {
	    
		if(path.endsWith("." + extension)) {
		    //path ends with appropriate extension already, just use the provided path as it is
			return path;
		}else{
		    
		    if(path.endsWith(".")){
		        //path ends with '.' and needs extension added.
		        return path.concat(extension);
		    }else{
		        //path ends without extension, so add the extension to the end of the path
		        return path.concat("." + extension);
		    }
		}

	}
	
	
}
