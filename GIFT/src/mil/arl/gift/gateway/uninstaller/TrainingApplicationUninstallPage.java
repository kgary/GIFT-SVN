/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.uninstaller;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
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

import mil.arl.gift.common.enums.TrainingApplicationEnum;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the logic to build a user interface for removing any configurations created during
 * the training applications (TA) portion of the GIFT installer.  It allows the UI to be configured 
 * based on the types of TAs that were configured for a GIFT instance or execution of a single course.
 * 
 * @author mhoffman
 *
 */
public class TrainingApplicationUninstallPage extends WizardPage {

    /**
     * default value
     */
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.getLogger(TrainingApplicationUninstallPage.class);
    
    /** wizard settings used in the UninstallThread class */
    public static final String VBS_UNINSTALL            = "VBS_UNINSTALL";
    public static final String DE_TESTBED_UNINSTALL     = "DE_TESTBED_UNINSTALL"; 
    public static final String PPT_UNINSTALL            = "POWERPOINT_UNINSTALL";
    public static final String TC3_UNINSTALL            = "TC3_UNINSTALL";
    public static final String VR_ENGAGE_UNINSTALL      = "VR_ENGAGE_UNINSTALL";
    public static final String VBS_FILES                = "VBS_FILES";
    public static final String VR_ENGAGE_FILES          = "VR_ENGAGE_FILES";
    
    /** dialog labels */
    private static final String TITLE = "Training Applications";
    private static final String DESCRIPTION = "Training Applications";
 
    private static List<String> VBS_FILES_MAP = new ArrayList<>();
    private static List<String> VR_ENGAGE_FILES_MAP = new ArrayList<>();
    static{
        
        VBS_FILES_MAP.add("GIFTVBSGame.bat");
        VBS_FILES_MAP.add("plugins" + File.separator + "gift" + File.separator);
        VBS_FILES_MAP.add("plugins" + File.separator + "GIFTVBSPlugin.dll");
        VBS_FILES_MAP.add("plugins64" + File.separator + "gift" + File.separator);
        VBS_FILES_MAP.add("plugins64" + File.separator + "GIFTVBSPlugin64.dll");
        VBS_FILES_MAP.add("mycontent" + File.separator + "scripts" + File.separator + "init.sqf");
        VBS_FILES_MAP.add("mycontent" + File.separator + "scripts" + File.separator + "useTutor.sqf");
        VBS_FILES_MAP.add("mycontent" + File.separator + "scripts" + File.separator + "useTutorFunction.sqf");
        VR_ENGAGE_FILES_MAP.add("plugins64" + File.separator + "vrForces" + File.separator + "release" 
                + File.separator + "VrfGiftPluginDIS.dll");
        VR_ENGAGE_FILES_MAP.add("plugins64" + File.separator + "vrForces" + File.separator + "release" 
                + File.separator + "VrfGiftPlugin.properties");
        VR_ENGAGE_FILES_MAP.add("data" + File.separator + "simulationModelSets" + File.separator + "VR-Engage" + File.separator 
                + "scripts" + File.separator + "gift_script_executor.lua");
        VR_ENGAGE_FILES_MAP.add("data" + File.separator + "simulationModelSets" + File.separator + "VR-Engage" + File.separator 
                + "scripts" + File.separator + "gift_script_executor.xml");
    }
    
    private BoxLayout mainLayout;
    
    /** flag used to determine if the UI components for this page have already been created */
    boolean uiCreated = false;
    
    /**
     * collection of training applications to provided UI elements for. 
     *      If null or empty, all enumerated training application types will be considered.
     *      Duplicates will be filtered out automatically for the caller.
     */
    private List<TrainingApplicationEnum> trainingApps;
    
    /**
     * used for notification of page events like whether the page has any UI components for the user to 
     * interact with. Can be null.
     */
    private PageCreationEventListener pageEventListener;
    
    private boolean uninstallPPT = true;
    private boolean uninstallVBS = true;
    private boolean uninstallDETestbed = true;
    private boolean uninstallTC3 = true;
    private boolean uninstallVrEngage = true;
        
    /**
     * Simply stores the provided attributes for use during the creation of the user interface components 
     * needed to configured the specified training applications for use with GIFT .
     * Note: this constructor requires that you call createUI method to actually build the user interface
     * components.
     * 
     * @param trainingApps collection of training applications to provided UI elements for. 
     *      If null or empty, all enumerated training application types will be considered.
     *      Duplicates will be filtered out automatically for the caller.
     * @param pageEventListener used for notification of page creation events.  Can be null.
     */
    public TrainingApplicationUninstallPage(List<TrainingApplicationEnum> trainingApps, PageCreationEventListener pageEventListener){
        super(TITLE, DESCRIPTION);

        if(trainingApps == null || trainingApps.isEmpty()){
            //setup all Training Apps
            this.trainingApps = TrainingApplicationEnum.VALUES();
        }else{
            this.trainingApps = trainingApps;
        }        

        setPageEventListener(pageEventListener);
    }
    
    /**
     * Create the user interface components needed to configured ALL
     * training applications for use with GIFT.
     */
    public TrainingApplicationUninstallPage(){
        super(TITLE, DESCRIPTION);

        //setup all Training Apps
        this.trainingApps = TrainingApplicationEnum.VALUES();
        setupUi();
    }
    
    /**
     * Set the listener used for notification of page events like whether the page has any UI components for the user to 
     * interact with.
     * 
     * @param pageEventListener can be null
     */
    private void setPageEventListener(PageCreationEventListener pageEventListener){
        this.pageEventListener = pageEventListener;
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
    
    //Note: this is only called as part of the next page Wizard logic.
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        
        logger.info("Checking if any settings need to be changed.");
        
        settings.put(PPT_UNINSTALL, uninstallPPT);
        settings.put(DE_TESTBED_UNINSTALL, uninstallDETestbed);
        settings.put(VBS_UNINSTALL, uninstallVBS);
        settings.put(TC3_UNINSTALL, uninstallTC3);
        settings.put(VR_ENGAGE_UNINSTALL, uninstallVrEngage);
        
        settings.put(VBS_FILES, VBS_FILES_MAP);
        settings.put(VR_ENGAGE_FILES, VR_ENGAGE_FILES_MAP);
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {      
        setNextEnabled(true);
        setFinishEnabled(false);
    }
    
    /**
     * Sets up all of the user interface components for this page
     */
    private synchronized void setupUi() {
        
        //filter duplicates
        final List<TrainingApplicationEnum> filteredTrainingApps = new ArrayList<>();
        for(TrainingApplicationEnum trainingApp : trainingApps){
            
            if(!filteredTrainingApps.contains(trainingApp)){
                filteredTrainingApps.add(trainingApp);
            }
        }

        mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(mainLayout);
		// prevents oversizing the panels when running GIFT uninstaller
        this.setPreferredSize(new Dimension(350, 350));
        
        JLabel descLabel = new JLabel("<html><h3>If found, the following configurations created by GIFT will be removed:</h3><br>(the applications won't be uninstalled)</html>");
        
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
                    boolean panesCreated = initFX(fxpanel, filteredTrainingApps);
                    
                    //notify the optional listener whether this page contains anything
                    //useful to present to the user
                    if(pageEventListener != null){
                        pageEventListener.skipPage(!panesCreated);
                    }
                }catch(Exception e){
                    logger.error("Caught exception while trying to build Training Applications uninstall page.", e);
                    if(pageEventListener != null){
                        pageEventListener.pageError("failed to build the training applications uninstall page");
                    }
                }
            }
        });

        uiCreated = true;
    }
        
    /**
     * Build the panel by building each pane for each training application user interface.
     * 
     * @param fxpanel the panel to add panes to for each training application user interface
     * @param trainingApps training applications to provided UI elements for. 
     * @return boolean whether the JFX panel had any panes added to it.
     */
    private boolean initFX(JFXPanel fxpanel, List<TrainingApplicationEnum> trainingApps){
        
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        
        for(TrainingApplicationEnum trainingApp : trainingApps){
            
            if(trainingApp == TrainingApplicationEnum.VBS){                
                buildVBSNode(vbox);
            }else if(trainingApp == TrainingApplicationEnum.DE_TESTBED){
                buildDETestbedNode(vbox);
            }else if(trainingApp == TrainingApplicationEnum.TC3){
                buildTC3Node(vbox);
            }else if(trainingApp == TrainingApplicationEnum.POWERPOINT){
                buildPPTNode(vbox);
            }else if(trainingApp == TrainingApplicationEnum.SIMPLE_EXAMPLE_TA ||
                    trainingApp == TrainingApplicationEnum.SUDOKU){
                //nothing to configure
                continue;
            }else if(trainingApp == TrainingApplicationEnum.VR_ENGAGE){
                buildVrEngageNode(vbox);
            }else{
                if(logger.isInfoEnabled()) {
                    logger.info("Found unhandled training application type of '"+trainingApp+"'.  No uninstaller UI component will be created for it.");
                }
                continue;
            }
            
        }
        
        if(vbox.getChildren().isEmpty()){
            return false;
        }else{        
        
            root.setCenter(vbox);
            
            Scene scene = new Scene(root, Color.LIGHTGRAY);
            fxpanel.setScene(scene);
            return true;
        }
    }
    
    /**
     * Create GUI components for PowerPoint installation information.
     * 
     * @param titledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildPPTNode(VBox vbox){
        
        logger.info("Building PowerPoint uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("PowerPoint");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallPPT = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- remove environment variable");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(28);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }
    
    /**
     * Create GUI components for VBS installation information.
     * 
     * @param vbsTitledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildVBSNode(VBox vbox){
        
        logger.info("Building VBS uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("VBS");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallVBS = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- remove environment variable and delete GIFT files copied\nto your VBS installation directory");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(67);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }

    /**
     * Create GUI components for VR-Engage installation information.
     * 
     * @param vbox the box to add user interface components to. Cannot be null.
     */
    private void buildVrEngageNode(VBox vbox){
        
        logger.info("Building VR-Engage uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("Vr-Engage");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallVrEngage = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- remove environment variable and delete GIFT files copied\nto your VR-Engage installation directory");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(67);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }
    
    /**
     * Create GUI components for TC3 installation information.
     * 
     * @param titledPane the pane to add user interface components too
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildTC3Node(VBox vbox){
        
        logger.info("Building TC3 uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("TC3");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallTC3 = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- nothing to do");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(68);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }

    
    /**
     * Create GUI components for DE Testbed installation information.
     * 
     * @param deTestbedTitledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildDETestbedNode(VBox vbox){
        
        logger.info("Building DE Testbed uninstall node.");
        
        CheckBox checkBox = new CheckBox();
        checkBox.setText("DE Testbed");
        checkBox.setSelected(true);
        
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val){
                uninstallDETestbed = new_val;
            }
        });
        
        Label infoLabel = new Label();
        infoLabel.setText("- remove environment variable");
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(28);
        hbox.getChildren().add(checkBox);
        hbox.getChildren().add(infoLabel);
        
        vbox.getChildren().add(hbox);
    }  

    
    /**
     * Used for notification of page events caused during the creation of the page.
     * For example the page contents could be configurable and in some instances the page
     * might not contain anything that the user needs to see.
     * 
     * @author mhoffman
     *
     */
    public interface PageCreationEventListener{
        
        /**
         * Notification whether or not this page can be skipped in the set of pages
         * for an install user interface.  Pages are usually skipped if the page has nothing
         * userful to present to the user.
         * 
         * @param value true if the page can be skipped.
         */
        public void skipPage(boolean value);
        
        /**
         * Notification that there was an error on this installation page.
         * 
         * @param reason information about the error
         */
        public void pageError(String reason);
    }

}

