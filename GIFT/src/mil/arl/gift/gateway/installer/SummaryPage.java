/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;



import java.util.List;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;


/**
 * Shows the user a summary of what will be done during the installation process.
 * 
 * @author mhoffman
 */
public class SummaryPage extends WizardPage {
	
	/** Generated serial */
	private static final long serialVersionUID = -8227208053911011410L;
	
	/** page label values */
	private static final String TITLE = "Summary";
	private static final String DESCRIPTION = "Summary";
	
	/**
	 * Creates a new SummaryPage that will summarize settings.
	 * 
	 * @param settings The settings to summarize.
	 */
	public SummaryPage(WizardSettings settings) {
		super(TITLE, DESCRIPTION);
		setupUi(settings);
	}
	
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(false);
		setFinishEnabled(true);
	}
	
    /**
     * Sets up all of the JavaFX components
     * Starts by initializing a new layout and label in swing, then adding an FX panel for rest of graphics
     * 
     * @param settings list of settings to summarize
     */
	private void setupUi(final WizardSettings settings) {
	    
		if(settings != null) {		 
			
		    BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		    setLayout(layout);		    
	        		    
		    add(Box.createVerticalStrut(10));
		    
		    JLabel summary = new JLabel();
		    summary.setText("<html><b><h2>&nbsp;&nbsp;&nbsp;&nbsp;The following is a summary of the installation that will be performed:</b></h2><br></html>");
            add(summary);
            add(Box.createVerticalStrut(5));
            final JFXPanel fxpanel = new JFXPanel();
            
            //It is necessary to run the creation of the Java FX panel nodes
            //on the JFX thread.  
            //In addition if the 'implicitExit' value is not set to false the JFX thread
            //will exit after the first use of it ends.  When that happens the runLater calls
            //do not execute (refer to their javadocs)
            Platform.setImplicitExit(false);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	
                	
                	StackPane root = new StackPane();
                    Scene scene = new Scene(root);               
                    Font defaultFont = Font.font("Arial", 15);
    		    	Font BoldFont = Font.font("Arial", FontWeight.BOLD, 15);


                	//
        		    // Training Applications -
        		    //
        		    TextFlow installSummary = new TextFlow();
        		    installSummary.setPadding(new Insets(10,10,0,15));
        		    
        		    //VBS
        		    if(settings.containsKey(TrainingApplicationInstallPage.VBS_HOME)){
        		    	//Split up in separate Text objects to bold only "Note: "
        		    	Text part1 = new Text("\u2022 VBS will be configured to work with GIFT.\n\n");
        		    	Text note = new Text("Note: ");
        		    	Text part2 = new Text("please read the GIFT install instructions for additional VBS scenario setup procedures, " +
        		        		"including how to install\nadditional VBS terrain and model files needed for VBS scenarios\n" +
        		        		"referenced by GIFT courses.\n\n");
        		    	part1.setFont(defaultFont);
        		    	part2.setFont(defaultFont);
        		    	note.setFont(BoldFont);
        		    	installSummary.getChildren().addAll(part1, note, part2);
        		    	installSummary.getChildren().get(1).setTranslateX(installSummary.getChildren().get(0).getLayoutX()+30);
        		    	installSummary.getChildren().get(2).setTranslateX(installSummary.getChildren().get(0).getLayoutX()+30);
        		    }
        		    
                    //DE Testbed
                    if(settings.containsKey(TrainingApplicationInstallPage.DE_TESTBED_HOME)){                    	
                    	Text text = new Text("\u2022 The DE Testbed will be configured to work with GIFT.\n\n");
                    	text.setFont(defaultFont);
                    	installSummary.getChildren().add(text);
                    }
                    
                    //PowerPoint
                    if(settings.containsKey(TrainingApplicationInstallPage.PPT_HOME)){
                    	Text text = new Text("\u2022 PowerPoint will be configured to work with GIFT.\n\n");
                    	text.setFont(defaultFont);
                    	installSummary.getChildren().add(text);
                    }                    
                    
                    Text finish = new Text("Click Finish to start the installation process...");
                    finish.setFont(BoldFont);
                    
                    if(!(installSummary.getChildren().isEmpty())){                    	
            		    root.getChildren().add(installSummary);
            		    finish.setTranslateY(finish.getBoundsInLocal().getMaxY() * 30);
            		    root.getChildren().add(finish);
                    }else{
                    	Text text = new Text("\u2022 No additional software components need to be installed based on the Training Applications that will be used in this course.");
                    	text.setFont(BoldFont);
                    	installSummary.getChildren().add(text);
                    	root.getChildren().add(installSummary);
                    	finish.setTranslateY(finish.getBoundsInLocal().getMaxY() * 20);
                    	root.getChildren().add(finish);
                    }
                    fxpanel.setScene(scene); 
                }
            });
            add(fxpanel);
		}
	}	
	
}
