/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import java.awt.Dimension;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is used to present miscellaneous uninstall information.
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
    
    private BoxLayout mainLayout;
    
    private boolean uninstallPython = true;
    private boolean deleteGIFT = false;
    
    /** flag used to determine if the UI components for this page have already been created */
    boolean uiCreated = false;
        
    /**
     * Default constructor - create GUI components
     */
    public MiscPage(){    	
        super(TITLE, DESCRIPTION);        
        setupUi();
    }
    
    /**
     * Creates the user interface components.
     * Note: has no affect if this method has been called and returned without error before or if the default
     * constructor was used.
     */
    public void createUI(){        
        
        if(uiCreated){
            return;
        }
        
        setupUi();        
        
    }
    
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        
        settings.put(UninstallSettings.PYTHON_UNINSTALL, uninstallPython);
        settings.put(UninstallSettings.DELETE_GIFT, deleteGIFT);

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
        
        mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(mainLayout);
        // prevents oversizing the panels when running GIFT uninstaller
        this.setPreferredSize(new Dimension(350, 350));
        
        JLabel descLabel = new JLabel("<html><h3>Other items to consider:</h3></html>");
        
        Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
        descLabel.setBorder(paddingBorder);
        add(descLabel);        
        
        final JFXPanel fxpanel = new JFXPanel();    
        add(fxpanel);       
        
        //It is necessary to run the creation of the Java FX panel nodes
        //on the JFX thread.  
        //In addition if the 'implicitExit' value is not set to false the JFX thread
        //will exit after the first use of it ends.  When that happens the runLater calls
        //do not execute (refer to their javadocs)
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                
                try{
                    initFX(fxpanel);

                }catch(Exception e){
                    logger.error("Caught exception while trying to build Training Applications uninstall page.", e);
                }
            }
        });

        uiCreated = true;
    }
    
    /**
     * Build the panel.
     * 
     * @param fxpanel the panel to add panes to
     */
    private void initFX(JFXPanel fxpanel){
        
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        
        setupDeleteGIFT(vbox);
        setupWinPython(vbox);
        
        root.setCenter(vbox);
        
        Scene scene = new Scene(root, Color.LIGHTGRAY);
        fxpanel.setScene(scene);
    }
    
    /**
     * Sets up the WinPython components
     */
    private void setupWinPython(VBox vbox) {
    	
        logger.info("Building Python uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("Python");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallPython = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- remove environment variable");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(70);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);

    }
    
    private void setupDeleteGIFT(VBox vbox){
        
        logger.info("Building Delete GIFT node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("Delete GIFT files");
        checkBox.setSelected(false);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                deleteGIFT = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- permanetly deletes GIFT files on this computer");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(47);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }


    
}
