/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.gateway.installer.InstallWizardListener.InstallFinishedCallback;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage.PageCreationEventListener;

import org.ciscavate.cjwizard.WizardContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the Install Gateway Training Applications GUI.
 * 
 * @author mhoffman
 */
public class InstallMain {
    
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallMain.class);
	
	private static final String TITLE = "GIFT Gateway Module Setup";
	
	/** size of the dialog */
	private static final Dimension SIZE = new Dimension(850, 435);
	
	/**
	 * collection of training applications that need to be configured for this instance of the installer
	 */
	private List<TrainingApplicationEnum> trainingApps;
	
	/**
	 * the page containing the training application configuration user interface elements
	 */
	private TrainingApplicationInstallPage trainingAppInstallPage;
	
	/**
	 * used to prevent the calling thread that shows the installation dialog from
	 * continuing until after the installation logic has completed   
	 */
	private Object blockingObject = new Object();
	
	/**
	 * flag used to indicate whether the installation logic completed or was canceled
	 */
	private boolean installCompleted = false;
	
	/**
	 * the installer dialog instance
	 */
	private JFrame dialog;

	/**
	 * Simply save the training applications to configure in this installation dialog instance.
	 * 
	 * @param trainingApps If null or empty, all of the GIFT training applications configuration user 
	 * interface components will be shown in the installation dialog.
	 */
	public InstallMain(List<TrainingApplicationEnum> trainingApps) {
	    this.trainingApps = trainingApps;
	}
		
	/**
	 * Show the installation dialog to the user.  The dialog will be comprised of the training application
	 * configuration user interface components specified in the constructor.  If there is nothing to show
	 * to the user (i.e. nothing for the user to configure), the dialog will not be shown.
	 * 
	 * Note that this method will block the calling thread until the installation logic is completed or
	 * there is nothing to show to the user.
	 * 
	 * @return boolean whether the installation logic completed.
	 */
	public boolean show(){   

	    //used to be notified of whether the installation dialog should be created or
	    //just release the calling thread.
        PageCreationEventListener pageEventListener = new PageCreationEventListener() {
            
            @Override
            public void skipPage(boolean value) {
                          
                if(value){
                	
                	installCompleted = true;
                	
                    
                    logger.info("The GW installer dialog is not needed and will not be shown.  Notifying the waiting thread to wakeup.");
                    
                    synchronized (blockingObject) {
                        blockingObject.notifyAll();
                    } 
                }else{
                    createAndShowDialog();                  
                }
            }

            @Override
            public void pageError(String reason) {
                
                logger.info("There was an error caused on the Training Applications install page of '"+reason+"'.  Notifying the waiting thread to wakeup.");
                
                synchronized (blockingObject) {
                    blockingObject.notifyAll();
                } 
            }
        };
      
      // Create Training Application page
      trainingAppInstallPage = new TrainingApplicationInstallPage(trainingApps, pageEventListener);
      trainingAppInstallPage.setRequireAll(true);
      trainingAppInstallPage.createUI();

      
      //block until the installation logic is complete
      synchronized (blockingObject) {
          try {
              logger.info("Placing the calling thread in a wait until the installer is complete or fails.");
              blockingObject.wait();
          } catch (InterruptedException e1) {
              logger.error("Caught exception while waiting for installation dialog to complete.", e1);
          }
      }  
      
      return installCompleted;

	}
	
	/**
	 * Close the installer dialog prematurely from outside of the installer's UI logic.
	 * For example, the Gateway is being order to close because the domain session is ending.
	 * 
	 * @param reason a message describing why the installer needs to close
	 */
	public void close(String reason){
	    
	    if(dialog != null && isRunning()){
	    
    	    logger.warn("Prematurely closing the installer because '"+reason+"'.");
    	    
    	    JOptionPane.showConfirmDialog(null, "Prematurely closing the installer because '"+reason+"'.", 
    	            "Installer Closing", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
    	    
    	    dialog.dispose();
	    }
	}
	
	/**
	 * Return whether or not the installer is running.  True doesn't indicate that the
	 * dialog is currently visible.
	 * 
	 * @return boolean
	 */
	public boolean isRunning(){
	    return dialog != null && dialog.isActive();
	}
	
	/**
	 * Create the installation dialog with the necessary pages.
	 */
	private void createAndShowDialog(){
	    
	    logger.info("Creating GW installer dialog.");
	    
        InstallPageFactory factory = new InstallPageFactory();
        factory.addPage(trainingAppInstallPage);
        
        WizardContainer container = new WizardContainer(factory);
        dialog = new JFrame();
        
        Image icon = ImageUtil.getInstance().getSystemIcon();
        if(icon != null) {
            dialog.setIconImage(icon);
        } else {
            logger.error("Could not load GIFT icon.");
        }

        dialog.setTitle(TITLE);
        dialog.setSize(SIZE);
        
        //used to release the calling thread once the install logic has completed
        final WindowListener windowListener = new WindowListener() {
            
            @Override
            public void windowOpened(WindowEvent e) {
                
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                
            }
            
            @Override
            public void windowDeiconified(WindowEvent e) {
                
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                
                logger.info("The GW installer dialog has closed.  Notifying the waiting thread to wakeup.");
                
                //the installation logic is complete, release the calling thread
                synchronized (blockingObject) {
                    blockingObject.notifyAll();
                }
            }
            
            @Override
            public void windowActivated(WindowEvent e) {
                
            }
        };
        
        dialog.addWindowListener(windowListener);
	    
        dialog.getContentPane().add(container);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null); // According to the Javadoc this centers a JDialog
        
        //used to handle whether the installation completed or was canceled
        InstallFinishedCallback installFinishedCallback = new InstallFinishedCallback() {
            
            @Override
            public void completed() {
                installCompleted = true;               
            }
            
            @Override
            public void canceled() {
                //close the dialog which will cause the calling thread to be released
                dialog.dispose();
            }
        };
        
        InstallWizardListener installWizardListener = new InstallWizardListener(dialog, installFinishedCallback);
        container.addWizardListener(installWizardListener);         
        
        dialog.setVisible(true);
        
        logger.info("GW installer dialog is now visible.");
	}

}
