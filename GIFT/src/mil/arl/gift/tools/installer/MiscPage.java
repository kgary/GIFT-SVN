/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import mil.arl.gift.common.io.UserEnvironmentUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is used to present miscellaneous install information.
 * 
 * @author bzahid
 * 
 */
public class MiscPage extends WizardPage {

	private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(MiscPage.class);
    
    /** dialog labels */
    private static final String TITLE  = "Miscellaneous";
    private static final String DESCRIPTION = "Miscellaneous";
    
    /** Default paths */
    private static final String IE_PATH = "C:/Program Files/Internet Explorer/iexplore.exe";
    private static final String GIFT_PYTHON_URL = InstallerProperties.getInstance().getWinPythonURL();
    private static final String GIFT_PYTHON = "external/" + InstallerProperties.getInstance().getWinPythonExe();
       
    /** Image paths */
    private static final String WINPY_IN_IMG = "bin/images/pythonInstalled.png";
    private static final String WINPY_DL_IMG = "bin/images/installPython.png";
    private static final String LESS_IMG = "bin/images/less.png";
    private static final String MORE_IMG = "bin/images/more.png";
    
    private static final String SELECT_PYTHON_INSTALL_DIR_BUTTON_LABEL = "Select Python Installation Directory";
    private static final String SELECT_WINPYTHON_INSTALL_DIR_BUTTON_LABEL = "Select WinPython Installation Directory";
    
    /** Used to store the environment variable value for Python */
    private static Object pyValue;
    
    /** Python GUI components */
    private static JPanel panel;
    private static Box pyDirBrowseBox;

    /** WinPython GUI components*/
    private static Box winPyDirBrowseBox;
    private static JLabel winPyInfoLabel;   
    private static JLabel errorLabel;
    private static JLabel directoryLabel;
    
    private static JLabel pyVarInfoLabel;
    private static JButton skipButton;
    private static JButton installButton;
    private static JButton downloadButton; 
    private static JTextField pyDirectoryField;
    private static JTextField winPyDirectoryField;
    private static GridBagConstraints panel_gbc;
    private static GridBagConstraints button_gbc;
        
    /** used to cache the python install directory if already set so the loading of the next installer page isn't slowed down */
    private File pyDir = null;
    
    /**
     * Default constructor - create GUI components
     */
    public MiscPage(){    	
        super(TITLE, DESCRIPTION);        
        setupUi();
    }
    
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        
        if(!pyDirectoryField.getText().isEmpty()) {
        	settings.put(InstallSettings.PYTHON_HOME, pyDirectoryField.getText());
        }else if(!winPyDirectoryField.getText().isEmpty()){
            
            if(pyDir == null || !pyDir.exists()){
                //find python directory - expensive, slows down the summary page from loading after this page in the installer
                pyDir = getPythonDirectory(new File(winPyDirectoryField.getText()));
            }

            if(pyDir != null){
                //is a winpython folder with a python directory with a python.exe                
                settings.put(InstallSettings.PYTHON_HOME, pyDir.getAbsolutePath());
            }
        }else{
            //this is to remove the variable if not a valid path on this computer or the user cleared the path
        	settings.put(InstallSettings.PYTHON_HOME, null);
        }
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        setNextEnabled(true);
        setFinishEnabled(false);
    }
    
    /**
     * Sets up all of the swing components
     */
    private void setupUi() {
    	setupWinPython();
    }
    
    /**
     * Sets up the WinPython components
     */
    private void setupWinPython() {
    	
    	final File winPyExe = new File(GIFT_PYTHON);	
    	
    	Image pyImage = new ImageIcon(WINPY_IN_IMG).getImage();
    	Image lessImage  = new ImageIcon(LESS_IMG).getImage();
    	Image moreImage = new ImageIcon(MORE_IMG).getImage();
    	Image pyImage2 = new ImageIcon(WINPY_DL_IMG).getImage();
    	ImageIcon pyIcon  = new ImageIcon(pyImage.getScaledInstance(40, 40,Image.SCALE_SMOOTH));
    	ImageIcon pyIcon2 = new ImageIcon(pyImage2.getScaledInstance(40, 40,Image.SCALE_SMOOTH));
    	final ImageIcon plusIcon  = new ImageIcon(lessImage.getScaledInstance(8, 8,Image.SCALE_SMOOTH));
    	final ImageIcon minusIcon = new ImageIcon(moreImage.getScaledInstance(8, 8,Image.SCALE_SMOOTH));
    	
    	JLabel titleLabel = new JLabel("<html><h3>Miscellaneous</h3></html>");
    	JLabel pyTitleLabel = new JLabel("<html><b>Python <i>(optional)</i></b> &nbsp;&nbsp;[tested against WinPython v2.7.5.1]</html>");
    	JLabel winPyTitleLabel = new JLabel("<html><b>WinPython <i>(optional)</i></b></html>");
    	JLabel pyInstalledLabel = new JLabel("<html>Already have Python installed?  Show GIFT where or have "+
    	        "GIFT help you with WinPython below.<br><br><b>Note:</b> this has a higher precedence than WinPython setting.</html>");
    	JLabel pyInfoLabel  = new JLabel("WinPython is a free, open-source distribution of the Python programming language.");
    	final JLabel blurbLabel = new JLabel("<html>In GIFT, Python can be leveraged for its strengths including fast, "
    			+ "low resource<br>utilization scientific computations. There have been Sensor filters as well as<br>Learner"
    			+ " state models written in Python and integrated into GIFT.<br><br>If you plan on leveraging Python or are "
    			+ "not sure, we suggest you select to<br>install Python by following the instructions.</html>");
    	    	
    	JButton pyDirBrowseButton = new JButton("Browse ..."); 
    	JButton winPyDirBrowseButton = new JButton("Browse ..."); 
    	JButton pyDirClearButton = new JButton("Remove");
    	JButton winPyDirClearButton = new JButton("Remove");
    	final JButton moreButton  = new JButton("<html><h5><b>Click here to learn more</b></h5></html>", plusIcon);
    	final JButton reinstallButton = new JButton(" Reinstall WinPython", pyIcon2);
    	final JFileChooser winPyDirFileChooser = new JFileChooser("C:/");
    	final JFileChooser pyDirFileChooser = new JFileChooser("C:/");
    	   
    	panel = new JPanel();
    	pyVarInfoLabel = new JLabel();
    	winPyInfoLabel = new JLabel();  
        pyDirectoryField = new JTextField();
        winPyDirectoryField = new JTextField();
    	panel_gbc  = new GridBagConstraints();
        button_gbc = new GridBagConstraints();
        pyDirBrowseBox = Box.createHorizontalBox();
        winPyDirBrowseBox = Box.createHorizontalBox();
    	
    	errorLabel = new JLabel("<html><center><span style=\"color:#C7050C\">Warning:"
    			+ " The WinPython installer failed to complete successfully.<br>Click"
        		+ " the 'Install' button to try again.</span></center></html>");
        directoryLabel = new JLabel("<html>Please download and run the WinPython installer."
        		+ " When the installer completes,<br>please select the WinPython folder you"
        		+ " created during the installation.</html>");
    	
        installButton = new JButton(" Install WinPython for use with GIFT", pyIcon2);
    	skipButton = new JButton("Click here if WinPython is already installed     ", pyIcon);
    	downloadButton = new JButton(" Click here to download and install WinPython", pyIcon2); 
    	    	
    	pyDirectoryField.setEditable(false);
    	pyDirClearButton.setVisible(false);
    	winPyDirectoryField.setEditable(false);
    	winPyDirClearButton.setVisible(false);
    	pyDirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	winPyDirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    
    	panel.setLayout(new GridBagLayout());
	    setLayout(new FlowLayout(FlowLayout.LEFT));
	    
	    moreButton.setOpaque(false);
	    moreButton.setFocusPainted(false);
	    moreButton.setBorderPainted(false);
	    moreButton.setContentAreaFilled(false);
	    moreButton.setBorder(new EmptyBorder(0,0,0,0));	    
	    moreButton.setHorizontalTextPosition(JButton.LEFT);
	    
	    winPyDirBrowseBox.add(winPyDirectoryField);
	    winPyDirBrowseBox.add(winPyDirBrowseButton);
	    winPyDirBrowseBox.add(winPyDirClearButton);
	    
        pyDirBrowseBox.add(pyDirectoryField);
        pyDirBrowseBox.add(pyDirBrowseButton);
        pyDirBrowseBox.add(pyDirClearButton);
    	
        panel_gbc.gridy = 1;
        panel_gbc.anchor = GridBagConstraints.WEST;
        panel_gbc.insets = new Insets(5, 21, 5, 61);   
        panel.add(titleLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 40, 2, 61);
        panel.add(pyTitleLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 51, 0, 0); 
        panel.add(moreButton, panel_gbc);  
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 51, 15, 0); 
        panel.add(blurbLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 50, 15, 61);
        panel.add(pyInstalledLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 50, 15, 61);
        panel.add(pyVarInfoLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 50, 15, 0);
        panel_gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pyDirBrowseBox, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 50, 2, 61);
        panel.add(winPyTitleLabel, panel_gbc);
        
        panel_gbc.gridy += 1;
        panel_gbc.insets = new Insets(0, 51, 15, 0);
        panel.add(pyInfoLabel, panel_gbc);      
                
	    button_gbc.weighty = 1.0;
	    button_gbc.gridy += panel_gbc.gridy + 3;
	    button_gbc.fill  = GridBagConstraints.NONE;        
	    button_gbc.anchor = GridBagConstraints.CENTER;	    
	    
	    pyValue = UserEnvironmentUtil.getEnvironmentVariable(InstallSettings.PYTHON_HOME);
    	    	    	    	
    	if(pyValue != null) {
    		// The environment variable has been set.
    		
    		 File envPyDir = new File((String) pyValue);
    		 if(envPyDir.exists()){
    			 // The environment variable is valid
    		     
    		     //is this python or winpython?
                 if(checkWinPythonFolder(envPyDir.getParentFile())){
                     //winpython
                     winPyInfoLabel.setText("<html><i>It appears WinPython has already been configured for use with GIFT.</i></html>");
                     pyDir = envPyDir.getParentFile();
                     winPyDirectoryField.setText(pyDir.getAbsolutePath());
                 }else{
                     pyDirectoryField.setText(envPyDir.getAbsolutePath());
                     pyDirClearButton.setVisible(true);
                     downloadOrSkipWinPython();  //show for WinPython in case they don't want to use the already installed Python
                 }                            		     
    			 
    			 panel_gbc.insets = new Insets(0, 91, 15, 0);
    			 
             } else {
            	 // The environment variable is invalid; warn user
                 
                 logger.warn("Found a GIFT reference to python with the key of "+InstallSettings.PYTHON_HOME+" and the path value of '"+pyValue+"'.  This location does NOT exist.");
            	 
            	 panel_gbc.insets = new Insets(5, 61, 2, 0);
            	 pyVarInfoLabel.setText("<html><div style=\"width: 350px;\"><font color=\"red\">Warning:</font><br>GIFT was previously configured with the Python install at \"" 
            			+  pyValue + "\" which is no longer accessible. Please use the GIFT uninstaller to remove this reference or provide another Python install on this page.</html>");
            	 
            	 downloadOrSkipWinPython();
             }
    		 
    		 panel_gbc.gridy += 1;
    		 panel.add(winPyInfoLabel, panel_gbc);
    		 button_gbc.insets = new Insets(0, 41, 17, 0);
    		 panel.add(reinstallButton, button_gbc);
    		 
    	} else {
    		
    		if(winPyExe.isFile()) {
    			// The winpython.exe is in GIFT/external. Show install prompt.
    			button_gbc.insets = new Insets(0, 41, 17, 0);
    			panel.add(installButton, button_gbc);
    		} else {
    			// winpython.exe not found. Show download prompt.
    			downloadOrSkipWinPython();
    		}
    	}
    	
    	panel_gbc.gridy = 12;
    	panel_gbc.insets = new Insets(0, 101, 2, 21);
    	panel.add(errorLabel, panel_gbc);
    	
    	panel_gbc.gridy += 1;
    	panel_gbc.insets = new Insets(0, 52, 2, 21);
    	panel_gbc.fill = GridBagConstraints.HORIZONTAL;
    	panel.add(directoryLabel, panel_gbc);
    	
    	panel_gbc.gridy += 1;
    	panel_gbc.insets = new Insets(0, 51, 2, 0);
    	panel_gbc.anchor = GridBagConstraints.CENTER;
    	panel.add(winPyDirBrowseBox, panel_gbc);
    	
    	blurbLabel.setVisible(false);
    	errorLabel.setVisible(false);
    	winPyDirBrowseBox.setVisible(false);
    	directoryLabel.setVisible(false);
    	
    	add(panel);
    	
    	// Set the directory and modify UI if the correct file is selected in the file chooser
    	pyDirBrowseButton.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) { 
    		        
                int returnVal = pyDirFileChooser.showDialog(pyDirFileChooser, SELECT_PYTHON_INSTALL_DIR_BUTTON_LABEL);
                
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    
                    File file = pyDirFileChooser.getSelectedFile();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible VBS installation folder                                
                        for(String child : file.list()){
                            
                            if(child.equals("python.exe")){
                                pyDirectoryField.setText(file.getAbsolutePath());
                                pyDirClearButton.setVisible(true);
                                break;
                            }
                        }                            
                    }

                }
    		}
    	});
    	
        // Set the directory and modify UI if the correct file is selected in the file chooser
        winPyDirBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { 
                    
                int returnVal = winPyDirFileChooser.showDialog(winPyDirFileChooser, SELECT_WINPYTHON_INSTALL_DIR_BUTTON_LABEL);
                
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    
                    File file = winPyDirFileChooser.getSelectedFile();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible VBS installation folder                                
                        for(String child : file.list()){
                            
                            if(child.equals("WinPython Control Panel.exe")){
                                winPyDirectoryField.setText(file.getAbsolutePath());
                                winPyDirClearButton.setVisible(true);
                                break;
                            }
                        }                            
                    }
                }   
            }
        });
        
        pyDirClearButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                pyDirectoryField.setText("");
                pyDirClearButton.setVisible(false);
            }
        });
        
        winPyDirClearButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                winPyDirectoryField.setText("");
                winPyDirClearButton.setVisible(false);
            }
        });
    	
    	// Open the 'Files' page on the GIFT Portal in IE
    	downloadButton.addActionListener(new ActionListener() {
    		
            @Override
            public void actionPerformed(ActionEvent e) {

                skipButton.setVisible(false);
                String[] args = {IE_PATH, GIFT_PYTHON_URL};
                
                try {           
                    Runtime.getRuntime().exec(args);
                } catch (Exception x) {
                    logger.error("Caught exception while attempting to download file: " + x);
                }      
                
                downloadAndInstallWinPython();
            }
        });
    	
    	// Disable the select button if a WinPython folder is not selected
    	winPyDirFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
            	boolean select = false;
            	
            	if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    
                    File file = (File) evt.getNewValue();
                    if (file != null && file.isDirectory()) {                         
                        //check for specific file(s) in the possible WinPython installation folder                               
                                
                        if(checkWinPythonFolder(file)){
                            //is a winpython folder
                            
                            if(getPythonDirectory(file) != null){
                                //is a winpython folder with a python directory with a python.exe
                                
                                select = true;
                            }                               
                        } 
                    }                    
                }
            	setSelectButtonState(winPyDirFileChooser, select);
                winPyDirFileChooser.repaint();
            }
    	});
    	
        // Disable the select button if a Python folder is not selected
        pyDirFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
                boolean select = false;
                
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    
                    File file = (File) evt.getNewValue();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible python installation folder                                
                        for(String child : file.list()){
                            
                            if(child.equals("python.exe")){
                                select = true;
                            }
                        } 
                    }                    
                }
                setSelectButtonState(pyDirFileChooser, select);
                pyDirFileChooser.repaint();
            }
        });
    	
    	installButton.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			directoryLabel.setText("Please select the WinPython folder created by the installer:");
    			installWinPython();
    		}
    	});
    	
    	moreButton.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			if(!blurbLabel.isVisible()) {
    				blurbLabel.setVisible(true);
    				moreButton.setIcon(minusIcon);
    				moreButton.setText("<html><h5><b>Click here to hide</b></h5></html>");
    			} else {
    				blurbLabel.setVisible(false);
    				moreButton.setIcon(plusIcon);
    				moreButton.setText("<html><h5><b>Click here to learn more</b></h5></html>");
    			}
    		}
    	});
    	
    	skipButton.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			
    			directoryLabel.setText("Please select your WinPython folder:");
    			
    			downloadButton.setVisible(false);
    			skipButton.setVisible(false);
    			directoryLabel.setVisible(true);
    			winPyDirBrowseBox.setVisible(true);
    		}
    	});
    	
    	reinstallButton.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			
    			winPyInfoLabel.setVisible(false);
    			
    			if(winPyExe.exists()) {
    				directoryLabel.setText("Please select the WinPython folder created by the installer:");
    				installWinPython();
    			} else {
    				// If WinPython.exe isn't in GIFT/external, prompt the user to download
    				reinstallButton.setVisible(false);
    				downloadAndInstallWinPython();
    			}
    		}
    	});
    }
    
    /**
     * Return true if the directory provided is a WinPython installation directory
     * 
     * @param directory the directory to check 
     * @return true iff the directory contains a specific file that indicates it is a winpython folder
     */
    private boolean checkWinPythonFolder(File directory){
        
        Collection<File> files = FileUtils.listFiles(directory, new NameFileFilter("WinPython Command Prompt.exe"), null);
        
        return files != null && files.size() == 1;
    }
    
    /**
     * Return the directory containing python.exe.  This is a recursive search. The directory
     * returned can be the directory provided.
     * 
     * @param directory the directory to search recursively
     * @return the folder containing python.exe at or below the directory provided.  Can be null.
     */
    private File getPythonDirectory(File directory){
        
        Collection<File> files = FileUtils.listFiles(directory, new NameFileFilter("python.exe"), DirectoryFileFilter.INSTANCE);
        
        if(files != null && files.size() == 1){
            return files.iterator().next().getParentFile();
        }
        
        return null;
    }
    
    /** 
     * Displays the download prompt and field for the downloaded winpython exe from GIFT portal. 
     */
    private void downloadOrSkipWinPython() {
    		
    	button_gbc.insets = new Insets(0, 41, 2, 0);
		panel.add(downloadButton, button_gbc);
		button_gbc.gridy += 1;
		button_gbc.insets = new Insets(5, 41, 2, 0);
		panel.add(skipButton, button_gbc);
				
		downloadButton.setVisible(true);
		skipButton.setVisible(true);
    }
        
    /** 
     * Displays the download prompt and field for the WinPython folder. 
     */
    private void downloadAndInstallWinPython() {
    
    	downloadButton.setText("Click here to download WinPython");
    	
    	button_gbc.gridy += 1;
    	button_gbc.insets = new Insets(0, 41, 2, 0);
    	panel.add(downloadButton, button_gbc);
    	
		panel_gbc.gridy += 2;
		panel_gbc.insets = new Insets(15, 52, 2, 61);
		panel.add(directoryLabel, panel_gbc);
			
		panel_gbc.gridy += 1;
		panel_gbc.anchor = GridBagConstraints.CENTER;
		panel_gbc.insets = new Insets(0, 61, 2, 21);
		panel.add(winPyDirBrowseBox, panel_gbc);
		
		downloadButton.setVisible(true);
		directoryLabel.setVisible(true);
		winPyDirBrowseBox.setVisible(true);
    }
    
    /** 
     * Disables UI and launches WinPython installer 
     */
    private void installWinPython() {
    	
    	// Hide the error label
		errorLabel.setVisible(false);  			
		
		// Disable UI
		setNextEnabled(false);
		setPrevEnabled(false);
		installButton.setEnabled(false);
		downloadButton.setEnabled(false);
		
		// Change cursor to waiting
		panel.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		JOptionPane.showMessageDialog(null, "<html><div style=\"width: 250px;\">After"
				+ " you press okay, the WinPython installer will start.<br>When the "
				+ "WinPython installer finishes, you will need to provide GIFT with "
				+ "the Destination Folder where you installed WinPython.</div></html>", "GIFT Installer Message", JOptionPane.INFORMATION_MESSAGE);
		
		Thread t = new Thread(new Runnable() { 			
		
			@Override 
			public void run() {
				
				Process process = null;
    			ProcessBuilder builder = new ProcessBuilder(GIFT_PYTHON);
    			builder.redirectErrorStream(true);
    			    			
    	        try {    	        	
    	        	process = builder.start();
					process.waitFor();
    	        } catch (IOException | InterruptedException x) {
					logger.error("Caught exception while attempting to start WinPython installer at " + GIFT_PYTHON + ":\n" + x);
    	        }    
    	        
    	        // Re-enable UI
    	        setNextEnabled(true);
    	        setPrevEnabled(true);
    			installButton.setEnabled(true);
    			downloadButton.setEnabled(true);
    			
    			if(process != null && process.exitValue() == 0) {
    				
    				// Hide everything, show the prompt for the environment variable value
    				directoryLabel.setVisible(true);
    				winPyDirBrowseBox.setVisible(true);

    			} else {
    				errorLabel.setVisible(true);
				}
    			
    			// Change cursor back to normal
    			panel.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		t.start();
    }
    
    private static void setSelectButtonState(Container c, boolean flag) {
        
        int len = c.getComponentCount();
        for (int i = 0; i < len; i++) {
            
          Component comp = c.getComponent(i);

          if (comp instanceof JButton) {
            JButton b = (JButton) comp;

            if ( b.getText() != null ) {
            	if(b.getText().equals(SELECT_PYTHON_INSTALL_DIR_BUTTON_LABEL) || b.getText().equals(SELECT_WINPYTHON_INSTALL_DIR_BUTTON_LABEL)) {
            		b.setEnabled(flag);
            	} else if(b.getText().equals("Open")) {
            		b.setEnabled(true);
            	}
            }
          } else if (comp instanceof Container) {
              setSelectButtonState((Container) comp, flag);
          }
        }     
    }    
}
