/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.gateway.interop.ppt.FindOfficeInstallation;

/**
 * Contains the logic to build a user interface for configuring training applications to be used
 * with GIFT.  It allows the UI to be configured based on the types of TAs that need to be configured
 * for a GIFT instance or execution of a single course.
 * 
 * @author mhoffman
 *
 */
public class TrainingApplicationInstallPage extends WizardPage {

    /**
     * default value
     */
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.getLogger(TrainingApplicationInstallPage.class);
    
    /** windows user environment variables */
    public static final String VBS_HOME         = UserEnvironmentUtil.VBS_HOME;
    public static final String DE_TESTBED_HOME  = UserEnvironmentUtil.DE_TESTBED_HOME; 
    public static final String PPT_HOME         = UserEnvironmentUtil.PPT_HOME;
    public static final String VR_ENGAGE_HOME  = UserEnvironmentUtil.VR_ENGAGE_HOME;
    
    public static final String DE_TESTBED_HOME_BATCH = "run_de_testbed.bat";
    
    /** vbs settings properties */
    public static final String VBS_FILES = "VBS_FILES";
    public static final String DELETE_VBS2_PLUGIN = "DELETE_VBS2_PLUGIN";

    /** Unity GIFT Wrap properties */
    public static final String UNITY_LAND_NAV_DOWNLOAD = "UNITY_LAND_NAV_DOWNLOAD";
    
    /** VR-Engage settings properties */
    public static final String VR_ENGAGE_FILES = "VR_ENGAGE_FILES";

    /** dialog labels */
    private static final String TITLE = "Training Applications";
    private static final String DESCRIPTION = "Training Applications";
    private static final String BROWSE = "Browse ...";
    
    /** link to install document for helpful information regarding training applications */
    private static final URI INSTALL_DOCUMENTATION = HelpDocuments.getInstallDoc();
    private static final String INSTALL_DOCUMENT_LINK = "For more information refer to the GIFT Install documentation.";
        
    /** various images used on this install page */
    private static final Image VBS3_IMAGE = new Image(TrainingApplicationInstallPage.class.getResourceAsStream("/mil/arl/gift/common/images/VBS3.png"));
    private static final Image ATTENTION  = new Image(TrainingApplicationInstallPage.class.getResourceAsStream("/mil/arl/gift/common/images/Unavailable.png"));
    private static final Image COMPLETED  = new Image(TrainingApplicationInstallPage.class.getResourceAsStream("/mil/arl/gift/common/images/check.png"));
    private static final Image NOT_COMPLETED = new Image(TrainingApplicationInstallPage.class.getResourceAsStream("/mil/arl/gift/common/images/errorIcon.png"));
    
    /** the number of vbs plugin files */
    private static final int VBS_FILE_COUNT = 7;
    
    /** the number of VR-Engage plugin files */
    private static final int VR_ENGAGE_FILE_COUNT = 4;
    
    /** the label of the button on the file browse dialog used to approve a file selection */
    private static final String APPROVE_BUTTON_TEXT = "Select";
 
    /** unicode bullet character to be used with javafx labels */
    private static final String BULLET = "\u25CF  ";   
    
    private BoxLayout mainLayout;
    
    /** A map of vbs source files to destination files */
    private HashMap<File, File> vbsFileMap = new HashMap<File, File>();
    
    /** A list of missing files from the vbs home directory */
    private StringBuilder missingVbsFilesList = new StringBuilder(); 
    
    /**
     * Mapping of the various inputs and whether the input has been satisfied (i.e. provided)
     * This is useful for determining if all inputs have been provided in order to determine
     * when to set the next button to be enabled/disabled.
     */
    private Map<String, Boolean> inputProvidedMap = new HashMap<>();
    
    /**
     * flag used to indicate if all inputs shown to the user are required to be populated
     */
    private boolean requireAll = false;
    
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
    
    /**
     *  VBS
     */

    /** Allows the user to choose the VBS installation directory */
    private JFileChooser vbsFileChooser;
    
    /** Shows the directory currently selected, and allows for manual typing */
    private TextField vbsDirectoryField;
    
    /** Browse button, opens file chooser */
    private Button vbsBrowseButton;
    
    /** Indicates whether or not to copy over VBS files */
    private boolean configureVBS = false;
    private boolean askForVBSInstallLocation = true;
    private boolean deleteVbs2Plugin = false;
    private boolean copyAllVbsFiles = true;
    
    /** Used to store the environment variable value for VBS */
    private Object vbsHomeValue;
    
    /**
     *  VR-Engage
     */
    
    /** A map of VR-Engage source files to destination files */
    private HashMap<File, File> vrEngageFileMap = new HashMap<File, File>();
    
    /** A list of missing files from the VR-Engage home directory */
    private StringBuilder missingVrEngageFilesList = new StringBuilder(); 
    
    /** Allows the user to choose the VBS installation directory */
    private JFileChooser vrEngageFileChooser;
    
    /** Shows the directory currently selected, and allows for manual typing */
    private TextField vrEngageDirectoryField;
    
    /** Browse button, opens file chooser */
    private Button vrEngageBrowseButton;
    
    /** Indicates whether or not to copy over VR-Engage files */
    private boolean configureVrEngage = false;
    private boolean askForVrEngageInstallLocation = true;
    private boolean copyAllVrEngageFiles = true;
    
    /** Used to store the environment variable value for VR-Engage */
    private Object vrEngageHomeValue;
    
    /**
     * DE Testbed
     */
    
    /** Allows the user to choose the DE Testbed installation directory */
    private JFileChooser deTestbedFileChooser;
    
    /** Shows the directory currently selected, and allows for manual typing */
    private TextField deTestbedDirectoryField;
    
    /** Browse button, opens file chooser */
    private Button deTestbedBrowseButton;
    
    /**
     * PowerPoint
     */
    
    /** 
     * used to store the environment variable value for PowerPoint.  
     * Will be null if PowerPoint is not being configured OR is already configured correctly.
     */
    private String pptHomeValue = null;
    
    /**
     * Unity GIFT Wrap
     */

    /**
     * used to store the flag to indicate if install should download the Unity project from
     * gifttutoring.org.
     */
    private boolean downloadUnity = false;

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
    public TrainingApplicationInstallPage(List<TrainingApplicationEnum> trainingApps, PageCreationEventListener pageEventListener){
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
    public TrainingApplicationInstallPage(){
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

    /**
     * Set whether all inputs shown to the user are required to be populated.
     * Default is false.
     * 
     * @param value if all inputs are required
     */
    public void setRequireAll(boolean value){
        requireAll = value;
    }
    
    //Note: this is only called as part of the next page Wizard logic.
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        
        if (logger.isInfoEnabled()) {
            logger.info("Checking if any settings need to be changed.");
        }
        
        //VBS
        if(vbsDirectoryField != null && vbsDirectoryField.getText() != null && vbsDirectoryField.getText().length() > 0){
          if(configureVBS) {
              if(logger.isInfoEnabled()) {
                  logger.info("Will be updating '"+VBS_HOME+"' value to '"+vbsDirectoryField.getText()+"'.");
              }
              settings.put(VBS_HOME, vbsDirectoryField.getText());
              
              vbsFilesMissing(vbsDirectoryField.getText(), copyAllVbsFiles);
              if(!vbsFileMap.isEmpty()) {
            	  settings.put(VBS_FILES, vbsFileMap);
            	  settings.put(DELETE_VBS2_PLUGIN, deleteVbs2Plugin);
              }
          }
          
          if(logger.isInfoEnabled()) {
              logger.info("The VBS directory didn't change.");
          }
        }
        
        //DE Testbed
        if(deTestbedDirectoryField != null && deTestbedDirectoryField.getText() != null && deTestbedDirectoryField.getText().length() > 0){
            if(logger.isInfoEnabled()) {
                logger.info("Will be updating '"+DE_TESTBED_HOME+"' value to '"+deTestbedDirectoryField.getText()+"'.");
            }
            settings.put(DE_TESTBED_HOME, deTestbedDirectoryField.getText());
        }
        
        //PowerPoint
        if(pptHomeValue != null){
            if(logger.isInfoEnabled()) {
                logger.info("Will be updating '"+PPT_HOME+"' value to '"+pptHomeValue+"'.");
            }
            settings.put(PPT_HOME, pptHomeValue);
        }

        // Unity GIFT Wrap
        if (logger.isInfoEnabled()) {
            logger.info("Updating '" + UNITY_LAND_NAV_DOWNLOAD + "' value to '" + downloadUnity + "'.");
        }
        settings.put(UNITY_LAND_NAV_DOWNLOAD, downloadUnity);
        
        //VR-Engage
        if(vrEngageDirectoryField != null && vrEngageDirectoryField.getText() != null && vrEngageDirectoryField.getText().length() > 0){
          if(configureVrEngage) {
              if(logger.isInfoEnabled()) {
                  logger.info("Will be updating '"+VR_ENGAGE_HOME+"' value to '"+vrEngageDirectoryField.getText()+"'.");
              }
              settings.put(VR_ENGAGE_HOME, vrEngageDirectoryField.getText());
              
              vrEngageFilesMissing(vrEngageDirectoryField.getText(), copyAllVrEngageFiles);
              if(!vrEngageFileMap.isEmpty()) {
                  settings.put(VR_ENGAGE_FILES, vrEngageFileMap);
              }
          }
          
          if(logger.isInfoEnabled()) {
              logger.info("The VR-Engage directory didn't change.");
          }
        }
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {      
        setNextEnabled(shouldSetNextEnabled());
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
		// prevents oversizing the panels when running GIFT installer
        this.setPreferredSize(new Dimension(350, 350));
        
        JLabel descLabel = new JLabel("<html><h4>(optional) Choose which desktop Training Application(s) this GIFT instance might use:</h4></html>");
        
        if (CommonProperties.getInstance().isRemoteMode()) {
        	descLabel.setText("<html><h3>This course requires the following Training Applications in order to run.  Training Applications that require attention<br>are indicated below:</h3></html>");
        }
        
        Border paddingBorder = BorderFactory.createEmptyBorder(5,10,5,10);
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
                    logger.error("Caught exception while trying to build Training Applications install page.", e);
                    if(pageEventListener != null){
                        pageEventListener.pageError("failed to build the training applications install page");
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
        Accordion accordion = new Accordion();
        
        for(TrainingApplicationEnum trainingApp : trainingApps){
            
            final TitledPane titledPane = new TitledPane();

            UpdatePaneStatusCallback statusCallback = new UpdatePaneStatusCallback() {
                
                @Override
                public void statusUpdated(boolean completed) {
                    Image statusImage = completed? COMPLETED : NOT_COMPLETED;
                    ImageView statusView = new ImageView(statusImage);
                    
                    statusView.setFitHeight(24);
                    statusView.setFitWidth(24);
                    titledPane.setGraphic(statusView);                
                }
            };
            
            try{
                
                if(trainingApp == TrainingApplicationEnum.VBS){                
                    buildVBSNode(titledPane, statusCallback);
                }else if(trainingApp == TrainingApplicationEnum.DE_TESTBED){
                    buildDETestbedNode(titledPane, statusCallback);
                }else if(trainingApp == TrainingApplicationEnum.TC3){
                    buildTC3Node(titledPane, statusCallback);
                }else if(trainingApp == TrainingApplicationEnum.POWERPOINT){
                    buildPPTNode(titledPane, statusCallback);
                }else if (trainingApp == TrainingApplicationEnum.UNITY_EMBEDDED) {
                    buildUnityGIFTWrapNode(titledPane, statusCallback);
                }else if(trainingApp == TrainingApplicationEnum.SIMPLE_EXAMPLE_TA ||
                        trainingApp == TrainingApplicationEnum.SUDOKU){
                    //nothing to configure
                    continue;
                } else if(trainingApp == TrainingApplicationEnum.VR_ENGAGE){                
                    buildVrEngageNode(titledPane, statusCallback);
                }else{
                    if(logger.isInfoEnabled()) {
                        logger.info("Found unhandled training application type of '"+trainingApp+"'.  No installer UI component will be created for it.");
                    }
                    continue;
                }
                
            }catch(Exception e){
                logger.error("Caught exception while trying to build the Training Applications install page for the training application type of '"+trainingApp+"'.", e);
                String errorMsg = "There was a problem building the installer panel for the training application type '"+trainingApp+"'.\n The error reads: "+e.getMessage();
                buildErrorNode(trainingApp, errorMsg, titledPane);
            }
            
            accordion.getPanes().add(titledPane);
            
            if(titledPane.expandedProperty().getValue() && accordion.getExpandedPane() == null){
                //expand the first title pane being added that needs to be expanded
                accordion.setExpandedPane(titledPane);
            }

        }
        
        if(accordion.getPanes().isEmpty()){
            return false;
        }else{        
        
            root.setCenter(accordion);
            
            Scene scene = new Scene(root, Color.LIGHTGRAY);
            fxpanel.setScene(scene);
            return true;
        }
    }
    
    /**
     * Build a node that describes an error with building a training app installer component.
     * 
     * @param trainingAppType the type of training application that was trying to have an installer UI component
     * built for it
     * @param errorMsg the reason it failed
     * @param titledPane where to place the node created that contains the error message
     */
    private void buildErrorNode(TrainingApplicationEnum trainingAppType, String errorMsg, TitledPane titledPane){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building error panel for '"+trainingAppType+"' because of the error message '"+errorMsg+"'.");
        }
        
        HBox wrapper = new HBox();
        VBox container = new VBox();
        ScrollPane scrollpane = new ScrollPane();
        
        ImageView statusImage = new ImageView(NOT_COMPLETED);
        statusImage.setFitWidth(24);
        statusImage.setFitHeight(24);
        statusImage.setImage(NOT_COMPLETED);
        
        Label infoLabel = new Label(errorMsg);

        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
        infoLabel.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(infoLabel);
        
        wrapper.setPadding(new Insets(10,10,10,10)); 
        wrapper.getChildren().add(container);
        
        HBox.setHgrow(container, Priority.ALWAYS);
        
        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollpane.setContent(wrapper);
        
        titledPane.setText(trainingAppType.getDisplayName());
        titledPane.setGraphic(statusImage);
        titledPane.setContent(scrollpane);
        titledPane.setExpanded(true);
    }
    
    /**
     * Create GUI components for PowerPoint installation information.
     * 
     * @param titledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildPPTNode(TitledPane titledPane, UpdatePaneStatusCallback updatePaneStatusCallback){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building PowerPoint install instructions.");
        }
        
        HBox wrapper = new HBox();
        VBox container = new VBox();
        ScrollPane scrollpane = new ScrollPane();
    	Label infoLabel = new Label();
    	final ImageView statusImage = new ImageView(COMPLETED);
        boolean pptFound = true;
        
    	String pptHomePath = UserEnvironmentUtil.getEnvironmentVariable(TrainingApplicationInstallPage.PPT_HOME);
    	Map<String, String> pptInstalls = null;
    	String supportedPptPath = null;
    	try{
    		pptInstalls = FindOfficeInstallation.findSupportedPowerPointVersions();
    		if(pptInstalls != null && !pptInstalls.isEmpty()) {
    			supportedPptPath = pptInstalls.values().iterator().next();
    		}
            
        }catch(Throwable e){
            logger.error("Caught exception while trying to find PowerPoint on your computer", e);
        }
            
    	//
    	// Create powerpoint file browser ui
    	//
                
    	HBox browseBox = new HBox();
    	final Button browseButton = new Button(BROWSE);
    	final TextField textfield = new TextField();
    	textfield.setMinSize(60, 20);
    	browseButton.setMinWidth(100);
    	browseBox.setPadding(new Insets(0,0,10,0));
    	HBox.setHgrow(textfield, Priority.ALWAYS); 
    	browseBox.getChildren().addAll(textfield, browseButton);

    	// Setup File Chooser
    	final JFileChooser fileChooser = new JFileChooser("C:/");
    	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	fileChooser.setFileFilter(new FileNameExtensionFilter("Executable files", "exe")); 
    	
    	//Disable the "select" button if a powerpoint executable is not selected
    	fileChooser.addPropertyChangeListener(new PropertyChangeListener() {

    		@Override
    		public void propertyChange(PropertyChangeEvent evt) {

    			setSelectButtonState(fileChooser, false);

    			if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {

    				File file = (File) evt.getNewValue();
    				if(file != null && FindOfficeInstallation.isPowerPointExecutable(file.getName())) { 
    					setSelectButtonState(fileChooser, true);
    				}
    			}

    			fileChooser.repaint();
    		}
    	});

    	browseButton.setOnAction(new EventHandler<ActionEvent>() {

    		@Override public void handle(ActionEvent e) {
    			File file = openChooseFolderDialog(fileChooser);

    			if(file != null){
    				textfield.setText(file.getPath());
    				File[] olbFile = file.getParentFile().listFiles(new FilenameFilter() {

    					@Override
    					public boolean accept(File dir, String name) {
    						return FindOfficeInstallation.isPowerPointOlb(name);
    					}

    				});

    				if(olbFile.length > 0) {
    					pptHomeValue = olbFile[0].getPath();
    					inputProvidedMap.put(PPT_HOME, true);
    					setNextEnabled(shouldSetNextEnabled());
    					if(statusImage.getImage().equals(NOT_COMPLETED)) {
    						statusImage.setImage(COMPLETED);
    					}
    				}
    			}
    		}            
    	});

    	//
    	// Create choices for selecting a powerpoint version to use
    	//
    	final String warningText = "Warning: Using an unsupported PowerPoint version with GIFT may cause errors during "
    			+ "PowerPoint shows in courses. To use an unsupported version, navigate to your PowerPoint executable file.";
    	final String fileNotFoundText = "Error: The path to the PowerPoint executable file is invalid.";
    	final Label warning = new Label(warningText);
		warning.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
		warning.setWrapText(true);
		warning.setVisible(false);

		final ToggleGroup radioGroup = new ToggleGroup();
		if(pptInstalls != null) {
			final Map<String, String> pptVersions = pptInstalls;
			for(final String pptName : pptVersions.keySet()) {

				RadioButton button = new RadioButton("Use " + pptName);
				button.setStyle("-fx-font-weight: bold;");
				button.setToggleGroup(radioGroup);
				container.getChildren().add(button);
				button.selectedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
						if(newValue) {
							pptHomeValue = pptVersions.get(pptName);
							if(logger.isInfoEnabled()) {
    							logger.info("set powerpoint home install page variable to '"+pptHomeValue+"' because it was selected from list of found installs.");
							}
							inputProvidedMap.put(PPT_HOME, true);
							statusImage.setImage(COMPLETED);
							warning.setVisible(false);
							browseButton.setDisable(true);
							textfield.setEditable(false);
							setNextEnabled(true);
						}
					}
				});
			}
		}
		
		final RadioButton button = new RadioButton("Use unsupported PowerPoint version");
		button.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					if(warning.getText().equals(fileNotFoundText)) {
						setNextEnabled(false);
						statusImage.setImage(NOT_COMPLETED);
					} else {
						pptHomeValue = FindOfficeInstallation.getPowerPointOLBPath(textfield.getText());
                        if(logger.isInfoEnabled()) {
                            logger.info("set powerpoint home install page variable to '"+pptHomeValue+"' because it was selected from unsupported powerpoint versions.");
                        }
						statusImage.setImage(ATTENTION);
					}
					warning.setVisible(true);
					browseButton.setDisable(false);
					textfield.setEditable(true);
				}
			}
		});
		
		textfield.textProperty().addListener(new ChangeListener<String>() {
			
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
				// If the user chose to use an unsupported PowerPoint version, verify the file path
				// before enabling the 'Next' button.

				File newFile = new File(newValue);					
				boolean enabled = (newFile.exists() && FindOfficeInstallation.isPowerPointExecutable(newFile.getName()));
				warning.setText(enabled ? warningText : fileNotFoundText);
				setNextEnabled(enabled || !button.isSelected());
				if(button.isSelected()) {
					statusImage.setImage(enabled ? ATTENTION : NOT_COMPLETED);
					if(enabled) {
						pptHomeValue = FindOfficeInstallation.getPowerPointOLBPath(newValue);
                        if(logger.isInfoEnabled()) {
                            logger.info("set powerpoint home install page variable to '"+pptHomeValue+"' because it was selected from user defined install.");
                        }
					}
				}
			}
			
		});

		button.setToggleGroup(radioGroup);
		button.setStyle("-fx-font-weight: bold;");
		browseBox.setPadding(new Insets(0,0,5,20));
		warning.setPadding(new Insets(0,0,10,20));
		
		// Make sure the environment variable is still valid
    	if(pptHomePath != null && new File(pptHomePath).exists()){
    		statusImage.setImage(ATTENTION);
    		
    		if(logger.isInfoEnabled()) {
        		logger.info("The PowerPoint environment variable was found: " + pptHomePath);
    		}
    		
    		if(pptInstalls == null || pptInstalls.isEmpty()) {
    			// No supported powerpoint versions were found. Warn the user that their version of powerpoint is unsupported.
    			if(logger.isInfoEnabled()) {
        			logger.info("It appears a PowerPoint environment variable has been set, however, the version of PowerPoint is not supported.");
    			}
    			
    			infoLabel.setText("A version of PowerPoint was found that has not been tested for use with GIFT.  The GIFT installer can continue, and GIFT "
    					+ "will be configured to use this version of PowerPoint.  However, learners might encounter unforseen errors when interacting with GIFT courses that contain PowerPoint content."
    					+ "\n\nIn order to use GIFT with a supported version of PowerPoint, first install a supported version of PowerPoint, and then re-run this installer. "
    					+ "The installer will update the GIFT configuration to use the supported version of PowerPoint.  This installer can be re-run at any time.\n\n"
    					+ "Supported versions:\n"
    					+  "    \u2022 2016 C2R Home and Business (32 bit)\n"
    					+  "    \u2022 2013 C2R Home and Business (32 bit)\n"
    					+  "    \u2022 2010 Home and Business (32 bit)\n"
    					+  "    \u2022 2007 Home and Business (32 bit)");
    			pptHomeValue = pptHomePath;
                if(logger.isInfoEnabled()) {
                    logger.info("set powerpoint home install page variable to '"+pptHomeValue+"' because it was found but is unsupported.");
                }

    			
    		} else { 
    			if(pptInstalls != null && pptInstalls.values().contains(pptHomePath)) {
    				// The environment variable points to a supported powerpoint version. Nothing to do.
    				if(logger.isInfoEnabled()) {
        				logger.info("The PowerPoint environment variable has been set and is a supported version.");
    				}
    				
    				infoLabel.setText("PowerPoint was found on your computer and GIFT is already configured to use it.");
    				radioGroup.selectToggle(radioGroup.getToggles().get(0));
    				statusImage.setImage(COMPLETED);
    			} else {
    				// The environment variable isn't a supported powerpoint version. Warn the user 
    				if(logger.isInfoEnabled()) {
        				logger.info("It appears a PowerPoint environment variable has been set, however, the version of "
        				        + "PowerPoint is not supported. Other PowerPoint installations were found on this computer.");
    				}
    				
                    infoLabel.setText("A supported version of PowerPoint was found on your computer however GIFT was previously configured with an unsupported version of PowerPoint. A supported "
                            + "version has been selected for you to prevent unforseen errors when learners are taking courses that present PowerPoint shows.");
                    
                    //Note: must set the textfield value before calling button.setSelected(true) as the button event handler (above)
                    //      uses the textfield value
                    textfield.setText(FindOfficeInstallation.getPowerPointExePath(pptHomePath));  
                    button.setSelected(true);
    				radioGroup.selectToggle(radioGroup.getToggles().get(0));
    			}
    			
    			container.getChildren().addAll(button, browseBox, warning);
    		}
        }else{
           
            if(supportedPptPath == null || !new File(supportedPptPath).exists()){
            	// If no other supported versions were found, check for unsupported versions
            	if(logger.isInfoEnabled()) {
                	logger.info("No PowerPoint environment variable has been set and no supported PowerPoint installations were found.");
            	}
            	
            	pptHomePath = FindOfficeInstallation.findUnsupportedPowerPointVersion();            	
            	if(pptHomePath != null) {
            		// If an unsupported version was found, use it to configure GIFT.
            		if(logger.isInfoEnabled()) {
                		logger.info("Found unsupported PowerPoint installation at: " + pptHomePath);
            		}
            		
            		infoLabel.setText("An unsupported version of PowerPoint was found and will be configured for use with GIFT. This "
            				+ "may result in unexpected behavior when running courses that present PowerPoint shows.\n\n"
            				+ "To prevent errors, GIFT should be configured with a supported PowerPoint version. After "
            				+ "installing a supported version of PowerPoint, you will need to restart this installer.\n\n"
	                        + "Supported versions:\n"
	                        +  "    \u2022 2016 C2R Home and Business (32 bit)\n"
	                        +  "    \u2022 2013 C2R Home and Business (32 bit)\n"
	                        +  "    \u2022 2010 Home and Business (32 bit)\n"
	                        +  "    \u2022 2007 Home and Business (32 bit)");
            		pptHomeValue = pptHomePath;
                    if(logger.isInfoEnabled()) {
                        logger.info("set powerpoint home install page variable to '"+pptHomeValue+"' because it was found from looking for unsupported installs.");
                    }

            		statusImage.setImage(ATTENTION);
            	} else {
            		// Warn the user that no powerpoint versions were found.
            		if(logger.isInfoEnabled()) {
                		logger.info("No PowerPoint installations were found on this computer.");
            		}
            		
            		//can't use HTML ul and li tags in javafx 8
	                infoLabel.setText("PowerPoint was NOT found on your computer."
                        + "  Please install Microsoft PowerPoint in order to run courses that present PowerPoint shows.  After installing PowerPoint you will need to restart this installer.\n\n"
                        + "Supported versions:\n"
                        +  "    \u2022 2016 C2R Home and Business (32 bit)\n"
                        +  "    \u2022 2013 C2R Home and Business (32 bit)\n"
                        +  "    \u2022 2010 Home and Business (32 bit)\n"
                        +  "    \u2022 2007 Home and Business (32 bit)");
	                
	                // Let the user browse for their powerpoint executable
	                Label label = new Label("If you have already installed a supported version of Microsoft PowerPoint, please browse to your PowerPoint executable file.");
	               
	                label.setWrapText(true);
	                label.setStyle("-fx-font-weight: bold;  -fx-text-fill: red;");
	                browseBox.setPadding(new Insets(0,0,10,0));
	                container.getChildren().addAll(label, browseBox);
	                statusImage.setImage(NOT_COMPLETED);
	                pptFound = false;
            	}
            }else{
            	// A supported version of powerpoint was found.
            	if(logger.isInfoEnabled()) {
                	logger.info("set powerpoint home install page variable to supported PowerPoint installation at: " + supportedPptPath);
            	}
            	
                infoLabel = new Label("PowerPoint was found on your computer and GIFT will be configured to use it.");
                pptHomeValue = supportedPptPath;
                radioGroup.selectToggle(radioGroup.getToggles().get(0));
                container.getChildren().addAll(button, browseBox, warning);
                statusImage.setImage(COMPLETED);
            }
        }
        
        if(pptFound){
            infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
            inputProvidedMap.put(PPT_HOME, true);
        }else{
            //show that PowerPoint was not found
            infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red");
            titledPane.setExpanded(true);
            inputProvidedMap.put(PPT_HOME, (pptHomeValue != null));
        }
        
        statusImage.setFitWidth(24);
        statusImage.setFitHeight(24);
        infoLabel.setWrapText(true);
        infoLabel.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(0, infoLabel);
        
        wrapper.setPadding(new Insets(0,10,0,10)); 
        wrapper.getChildren().add(container);
        
        HBox.setHgrow(container, Priority.ALWAYS);
        
        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollpane.setContent(wrapper);
        
        titledPane.setText(TrainingApplicationEnum.POWERPOINT.getDisplayName());
        titledPane.setGraphic(statusImage);
        titledPane.setContent(scrollpane);
    }
    
    /**
     * Create GUI components for VBS installation information.
     * 
     * @param vbsTitledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildVBSNode(TitledPane vbsTitledPane, UpdatePaneStatusCallback updatePaneStatusCallback){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building VBS install instructions.");
        }
        
        HBox wrapper = new HBox();
    	HBox browseBox = new HBox();
    	HBox versionBox = new HBox();
    	final VBox container  = new VBox();
    	final VBox configBox = new VBox();
    	ScrollPane scrollpane = new ScrollPane();
    	ImageView alertImage = new ImageView(ATTENTION);
    	ImageView statusImage = new ImageView(COMPLETED);               
        final Label infoLabel = new Label("Before running a GIFT course that uses VBS, VBS needs to be running and at the main menu.");
        
        alertImage.setFitWidth(19);
        alertImage.setFitHeight(20);
        infoLabel.setGraphic(alertImage);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
        infoLabel.setPadding(new Insets(10, 0, 10, 0));
        
        vbsHomeValue = UserEnvironmentUtil.getEnvironmentVariable(VBS_HOME);
        
        container.getChildren().add(infoLabel);        
        
        if(vbsHomeValue != null){
            //check the value
            
            if(logger.isInfoEnabled()) {
                logger.info("Checking "+VBS_HOME+" value of "+vbsHomeValue+".");
            }
            
            try{
                File vbsHomeDir = new File((String) vbsHomeValue);
                if(vbsHomeDir.exists()){
                    //directory exists, show user information about VBS has already been configured for use with GIFT
                    //but still need to make sure all necessary VBS scenario related files are in the appropriate VBS folders (refer to installation documentation)
                    
                	Label label = new Label();
                	
                	if(!vbsFilesMissing((String) vbsHomeValue, false)) {
                		label.setText("It appears that VBS has already been setup for interaction with GIFT based on the "
                            +VBS_HOME+" Windows environment variable being set.  Please check that all necessary VBS "
                            + "scenario related files are in the appropriate VBS folders.");
                		container.getChildren().add(label);
                		
                	} else {
                		// present the user with a list of files missing from the vbs home directory
                		
                		configureVBS = true;
                		Label missingLabel = new Label(missingVbsFilesList.toString());
                		configBox.getChildren().addAll(label, missingLabel);
                		container.getChildren().add(configBox);
                		
                		if(vbsFileMap.size() < VBS_FILE_COUNT) {
                			// if only some of the files are missing, give the user the option to 
                			// copy and replace all files or to copy and replace missing files only
                			
	                		HBox radioBox = new HBox();
	                		final ToggleGroup radioGroup = new ToggleGroup();
	                		RadioButton allFilesButton = new RadioButton("Install all files");
	                		RadioButton missingFilesButton = new RadioButton("Install missing files");
	                		Label configureLabel = new Label("These files are needed for GIFT to interact with VBS. ");
	                		infoLabel.setText("VBS plugin files will be reinstalled.\n"
	                			+ "Before running a GIFT course that uses VBS, VBS needs to be running and at the main menu.");
	                		label.setText("The following files were not found in the VBS directory: ");
	                		
	                		allFilesButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
	
								@Override
								public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
									copyAllVbsFiles = newValue;
								}
	                			
	                		});
	                			                		
	                		allFilesButton.setSelected(true);
	                		allFilesButton.setToggleGroup(radioGroup);
	                		missingFilesButton.setToggleGroup(radioGroup);
	                		
	                		radioBox.setSpacing(25);
	                		configureLabel.setWrapText(true);
	                		infoLabel.getGraphic().setTranslateY(-9);
	                		missingLabel.setPadding(new Insets(5, 0, 10, 0));
	                		configBox.setPadding(new Insets(0, 0, 10, 23));
	                		radioBox.getChildren().addAll(allFilesButton, missingFilesButton);
	                		configBox.getChildren().addAll(configureLabel, radioBox);
	                		
                		} else {
                			// if all of the files are missing, don't show any options.
                			
                			label.setText("The following files were not found in the VBS directory and will be installed: ");
                			missingLabel.setPadding(new Insets(5, 0, 0, 0));
                			configBox.setPadding(new Insets(0, 0, 0, 23));
                		}
                		   
                	}
                	
                    Hyperlink link = new Hyperlink();
                    link.setText(INSTALL_DOCUMENT_LINK);
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            try {
                                Desktop.getDesktop().browse(INSTALL_DOCUMENTATION);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }                        
                        }
                    });

                    label.setWrapText(true);
                    link.setPadding(new Insets(13, -6, 13, 0));                    
                    container.getChildren().add(link);
                    
                    askForVBSInstallLocation = false;
                    
                }else{
                    logger.warn("The current '"+VBS_HOME+"' Windows Environment variable value of "+vbsHomeValue+" is not a valid directory on this computer.");
                                        
                    //show user information about how the current environment variable value is not valid
                    TextFlow flow = new TextFlow();
                    Text t1 = new Text("The "+VBS_HOME+" Windows environment variable value of '" 
                    		+ vbsHomeValue + "' is not valid. Check the installer log file in ");
                    Text t2 = new Text("GIFT/output/logger/tools/");
                    Text t3 = new Text(" for more information.\n");  
                    
                    t2.setStyle("-fx-font-style: italic");
                    flow.getChildren().addAll(t1, t2, t3);
                    container.getChildren().add(flow);
                }
                
            }catch(Exception e){
                logger.error("There was an exception thrown when trying to verify the '"+VBS_HOME+"' Windows Environment variable value of "+vbsHomeValue+".", e);
                                
                //show user information about how the current environment variable value is not valid
                TextFlow flow = new TextFlow();
                Text t1 = new Text("The "+VBS_HOME+" Windows environment variable value of '" 
                		+ vbsHomeValue + "' is not valid. Check the installer log file in ");
                Text t2 = new Text("GIFT/output/logger/tools/");
                Text t3 = new Text(" for more information.\n");  
                
                t2.setStyle("-fx-font-style: italic");
                flow.getChildren().addAll(t1, t2, t3);
                container.getChildren().add(flow);
            }
        } 
        
        //show supported versions of vbs
        Label versionLabel = new Label("Supported Version(s):");
        ImageView vbs3View = new ImageView(VBS3_IMAGE);
        
        vbs3View.setFitWidth(70);
        vbs3View.setFitHeight(70);
        versionBox.setPadding(new Insets(0,0,17,0));
        container.getChildren().addAll(versionLabel, versionBox);
        
        //show file browse component(s)
        
        Label label = new Label();
        label.setWrapText(true);
        
        if(askForVBSInstallLocation) {
            //if VBS is not already installed, show prompt
            
            inputProvidedMap.put(VBS_HOME, false);
            
            label.setText(""
                    + ""
                    + "Please select your VBS installation directory"
                    + " (e.g. C:\\Bohemia Interactive\\VBS3) in order"
                    + " to automatically configure VBS for use with GIFT.");
            
            statusImage.setFitWidth(24);
            statusImage.setFitHeight(24);
            statusImage.setImage(NOT_COMPLETED);
            vbsTitledPane.setExpanded(true);
        } else {
            
            label.setText("VBS Installation Directory:");
            inputProvidedMap.put(VBS_HOME, true);
            statusImage.setFitWidth(24);
            statusImage.setFitHeight(24);
            statusImage.setImage(ATTENTION);
        }
        
        // Setup File Chooser
        vbsFileChooser = new JFileChooser("C:/");
        vbsFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        //disable the "select" button if a VBS directory folder is not selected
        vbsFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    
                    File file = (File) evt.getNewValue();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible VBS installation folder                                
                        for(String child : file.list()){
                            
                            if(child.matches("VBS[0-9](_64)?.exe")){
                                setSelectButtonState(vbsFileChooser, true);
                            }
                        }                            
                    }
                    else{
                        setSelectButtonState(vbsFileChooser, false);
                    }
                }else{
                    setSelectButtonState(vbsFileChooser, false);
                }
                
                vbsFileChooser.repaint();
            }
        });

        // Setup directory field
        vbsDirectoryField = new TextField();
        vbsDirectoryField.setEditable(false);
        vbsDirectoryField.setMinSize(60, 20);
        
        if(!askForVBSInstallLocation) {
            // Fill the directory field if VBS is already installed
            
            vbsDirectoryField.setText((String) vbsHomeValue);
        }
        
        // Setup browse button
        vbsBrowseButton = new Button();
        vbsBrowseButton.setText(BROWSE);
        vbsBrowseButton.setMinWidth(100);
        
        vbsBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override public void handle(ActionEvent e) {
                File file = openChooseFolderDialog(vbsFileChooser);
                String path = "";
                
                if(file != null){
                    path = file.getAbsolutePath();
                    if(logger.isInfoEnabled()) {
                        logger.info("Chose directory of '" + path + "' as the VBS installation directory.");
                    }
                    vbsDirectoryField.setText(path);
                        
                    if(!path.equals(vbsHomeValue) || askForVBSInstallLocation) {
                        // If the directory field changed, configure VBS
                        inputProvidedMap.put(VBS_HOME, true);
                        configureVBS = copyAllVbsFiles = true;
                        updatePaneStatusCallback.statusUpdated(true);
                    
                        if(container.getChildren().contains(configBox) && configBox.getChildren().size() > 1) {
                        	// hide the missing files list if it was displayed.                        	
                        	configBox.getChildren().clear();
                        	configBox.getChildren().add(new Label("The VBS installation directory you "
                        			+ "selected will automatically be configured for use with GIFT."));
                        	infoLabel.setText("Before running a GIFT course that uses VBS, VBS needs to be running and at the main menu.");
                        	infoLabel.getGraphic().setTranslateY(0);
                        }
                    
                    }
                    
                    setNextEnabled(shouldSetNextEnabled());
                }
            }            
        });
        
        wrapper.setPadding(new Insets(10,10,10,10)); 
        browseBox.setPadding(new Insets(0,0,10,0));
        
        HBox.setHgrow(container, Priority.ALWAYS);
        HBox.setHgrow(vbsDirectoryField, Priority.ALWAYS);        
        browseBox.getChildren().addAll(vbsDirectoryField, vbsBrowseButton);
        container.getChildren().addAll(label, browseBox);
        wrapper.getChildren().addAll(container);

        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollpane.setContent(wrapper);
        
        vbsTitledPane.setText(TrainingApplicationEnum.VBS.getDisplayName());
        vbsTitledPane.setGraphic(statusImage);
        vbsTitledPane.setContent(scrollpane);
    }
    
    /**
     * Determines whether or not GIFT plugin files are missing from the vbs installation directory.
     * If plugin files are missing, the vbs file map is populated with source and destination paths.
     * 
     * @param vbsPath the path to the VBS directory
     * @param addAllFiles whether or not all files should be installed. If false, only the missing 
     * files will be installed.
     * @return true if there are files missing, false otherwise.
     */
    private boolean vbsFilesMissing(String vbsPath, boolean addAllFiles) {
    
    	File domainDir = new File(CommonProperties.getInstance().getDomainDirectory());
    	String vbsCommon = CommonProperties.getInstance().getDomainExternalDirectory() + File.separator + "VBS.common";
    	String trainingApps = domainDir.getParent() + File.separator + "Training.Apps/VBS";
    	StringBuilder loggerMsg = new StringBuilder();
    	vbsFileMap.clear();
    	
    	if(addAllFiles) {
    		loggerMsg.append("The following files will be installed to the VBS installation directory: ");
    	} else {
    		loggerMsg.append("The following files were not found in the VBS installation directory and will be installed: ");
    	}
    	
    	// check for missing files
    	File vbsGame = new File (vbsPath + "/GIFTVBSGame.bat");
    	if(!vbsGame.exists() || addAllFiles) {
    		vbsFileMap.put(new File(trainingApps + "/GIFTVBSGame.bat"), vbsGame);
    		missingVbsFilesList.append(BULLET).append("GIFTVBSGame.bat \n");
    		loggerMsg.append("GIFTVBSGame.bat, ");
    	}
    	
    	File plugin = new File(vbsPath + "/plugins/GIFTVBSPlugin.dll");
    	if(!plugin.exists() || addAllFiles) {
    		vbsFileMap.put(new File(trainingApps + "/VBSGIFTPlugin/GIFTVBSPlugin.dll"), plugin);
    		missingVbsFilesList.append(BULLET).append("plugins/GIFTVBSPlugin.dll \n");
    		loggerMsg.append("GIFTVBSPlugin.dll, ");
    	}
    	
    	/**
    	 *  Second dll file in case VBS is launched in 64 bit mode
    	 */
    	if(new File(vbsPath + "/plugins64").exists()){
	    	File plugin64 = new File(vbsPath + "/plugins64/GIFTVBSPlugin64.dll");
	    	if(!plugin64.exists() || addAllFiles) {
	    		vbsFileMap.put(new File(trainingApps + "/VBSGIFTPlugin/GIFTVBSPlugin64.dll"), plugin64);
	    		missingVbsFilesList.append(BULLET).append("plugins64/GIFTVBSPlugin64.dll \n");
	    		loggerMsg.append("GIFTVBSPlugin64.dll, ");
	    	}
    	}
    	
    	File pluginConfig = new File(vbsPath + "/plugins/gift/config");
    	if(!pluginConfig.exists() || addAllFiles) {
    		vbsFileMap.put(new File(trainingApps + "/PluginData/config"), pluginConfig);
    		missingVbsFilesList.append(BULLET).append("plugins/gift/config \n");
    		loggerMsg.append("gift/config, ");
    	}
    	
    	File pluginData = new File(vbsPath + "/plugins/gift/data");
    	if(!pluginData.exists() || addAllFiles) {
    		vbsFileMap.put(new File(trainingApps + "/PluginData/data"), pluginData);
    		missingVbsFilesList.append(BULLET).append("plugins/gift/data \n");
    		loggerMsg.append("gift/data, ");
    	}
    	
    	File pluginLogs = new File(vbsPath + "/plugins/gift/logs");
    	if(!pluginLogs.exists() || addAllFiles) {
    		vbsFileMap.put(new File(trainingApps + "/PluginData/logs"), pluginLogs);
    		missingVbsFilesList.append(BULLET).append("plugins/gift/logs \n");
    		loggerMsg.append("gift/logs, ");
    	}
    	
    	/**
    	 * Copy the gift folder from the 32bit plugin folder to the plugins64 folder
    	 */
    	if(new File(vbsPath + "/plugins64").exists()){
	    	File giftFolder64 = new File(vbsPath + "/plugins64/gift");
	    	if(!giftFolder64.exists() || addAllFiles) {
	    		vbsFileMap.put(new File(trainingApps + "/PluginData"), giftFolder64);
	    		missingVbsFilesList.append(BULLET).append("plugins64/gift \n");
	    		loggerMsg.append("gift, ");
	    	}
    	}
    	
    	File scripts = new File(vbsPath + "/mycontent/scripts");
    	if(!scripts.exists() || addAllFiles) {
    		vbsFileMap.put(new File(vbsCommon + "/scripts"), scripts);
    		missingVbsFilesList.append(BULLET).append("mycontent/scripts \n");
    		loggerMsg.append("mycontent/scripts, ");
    	}
    	
    	deleteVbs2Plugin = new File(vbsPath + "/plugins/GIFTVBS2Plugin.dll").exists() || new File(vbsPath + "/GIFTVBS2Game.bat").exists();    
    	
    	if(vbsFileMap.isEmpty()) {
    		if(logger.isInfoEnabled()) {
        		logger.info("All GIFT VBS plugin files were found in the VBS installation directory.");
    		}
    	} else {
    		if(logger.isInfoEnabled()) {
        		logger.info(loggerMsg.toString());
    		}

    		if(deleteVbs2Plugin) {
    			if(logger.isInfoEnabled()) {
        			logger.info("A previous version of the GIFT VBS plugin was found and will be deleted.");
    			}
    		}
    	}
    	
    	return !vbsFileMap.isEmpty();
    }
    
    /**
     * Create GUI components for TC3 installation information.
     * 
     * @param titledPane the pane to add user interface components too
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildTC3Node(TitledPane titledPane, UpdatePaneStatusCallback updatePaneStatusCallback){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building TC3 install instructions.");
        }
        
        HBox wrapper = new HBox();
        VBox container = new VBox();
        ScrollPane scrollpane = new ScrollPane();
        
        ImageView statusImage = new ImageView(COMPLETED);
        statusImage.setFitWidth(24);
        statusImage.setFitHeight(24);
        statusImage.setImage(ATTENTION);
        
        Label infoLabel = new Label("Please be aware that before running a GIFT course that uses TC3, TC3 needs to be running and at the main menu.\n\n"
                + "Note: GIFT doesn't actively detect whether TC3 is at the main menu or not, hence the warning that it is your responsibility to do so.");
        
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
        infoLabel.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(infoLabel);
        
        wrapper.setPadding(new Insets(10,10,10,10)); 
        wrapper.getChildren().add(container);
        
        HBox.setHgrow(container, Priority.ALWAYS);
        
        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollpane.setContent(wrapper);
        
        titledPane.setText(TrainingApplicationEnum.TC3.getDisplayName());
        titledPane.setGraphic(statusImage);
        titledPane.setContent(scrollpane);
        titledPane.setExpanded(true);
    }

    
    /**
     * Create GUI components for DE Testbed installation information.
     * 
     * @param deTestbedTitledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildDETestbedNode(TitledPane deTestbedTitledPane, UpdatePaneStatusCallback updatePaneStatusCallback){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building DE Testbed install instructions.");
        }
        
    	HBox wrapper = new HBox();
    	HBox browseBox = new HBox();
    	VBox container = new VBox();
    	ScrollPane scrollpane = new ScrollPane();
        ImageView statusImage = new ImageView(COMPLETED);
        boolean askForDETestbedInstallLocation = true;
        
        Object deTestbedHomeValue = UserEnvironmentUtil.getEnvironmentVariable(DE_TESTBED_HOME);
        
        
        if(deTestbedHomeValue != null){
            // check the value
            
            if(logger.isInfoEnabled()) {
                logger.info("Checking "+DE_TESTBED_HOME+" value of "+deTestbedHomeValue+".");
            }
            
            try{
                /* 
                 * changed this logic to use a specific file in the DE Testbed install which only exists if the testbed
                 * has been installed and will be removed when uninstalled.  This fixes the issue of the installer not
                 * invalidating an uninstalled DE Testbed home environment variable thereby causing issues in a DE Testbed course. 
                 */
                File deTestbedGIFTConfig = new File((String) deTestbedHomeValue + File.separator + "config_gift.xml");
                
                if(deTestbedGIFTConfig.exists()){
                    // directory exists, show user information about DE Testbed has already been configured for use with GIFT
                    
                	Label label = new Label("It appears that DE Testbed has already been setup "
                    		+ "for interaction with GIFT based on the " + DE_TESTBED_HOME 
                    		+ " Windows environment variable being set.");
                    
                    Hyperlink link = new Hyperlink();
                    link.setText(INSTALL_DOCUMENT_LINK);
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            try {
                                Desktop.getDesktop().browse(INSTALL_DOCUMENTATION);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }                        
                        }
                    });
                    
                    askForDETestbedInstallLocation = false;
                    
                    label.setWrapText(true);
                    link.setPadding(new Insets(13, -6, 15, 0));
                    container.getChildren().addAll(label, link);
                    
                }else{
                    logger.warn("The current '"+DE_TESTBED_HOME+"' Windows Environment "
                    		+ "variable value of "+deTestbedHomeValue+" is not a valid "
                    		+ "directory (or a valid DE Testbed install) on this computer.");
                                        
                    //show user information about how the current environment variable value is not valid
                    TextFlow flow = new TextFlow();
                    Text t1 = new Text("The "+DE_TESTBED_HOME+" Windows environment variable value of '" 
                    		+ deTestbedHomeValue + "' is not valid. Check the installer log file in ");
                    Text t2 = new Text("GIFT/output/logger/tools/");
                    Text t3 = new Text(" for more information.\n");  
                    
                    t2.setStyle("-fx-font-style: italic");
                    flow.getChildren().addAll(t1, t2, t3);
                    container.getChildren().add(flow);
                }
                
            }catch(Exception e){
                logger.error("There was an exception thrown when trying to verify the '"+DE_TESTBED_HOME+"' "
                		+ "Windows Environment variable value of "+deTestbedHomeValue+".", e);
                                
                //show user information about how the current environment variable value is not valid
                TextFlow flow = new TextFlow();
                Text t1 = new Text("The "+DE_TESTBED_HOME+" Windows environment variable value of '"
                        + deTestbedHomeValue + "' is not valid. Check the installer log file in ");
                Text t2 = new Text("GIFT/output/logger/tools/");
                Text t3 = new Text(" for more information.\n");  
                
                t2.setStyle("-fx-font-style: italic");
                flow.getChildren().addAll(t1, t2, t3);
                container.getChildren().add(flow);
            }
        } 
        
        // show file browse component(s)
        
        Label label = new Label();
        label.setWrapText(true);
        
        if(askForDETestbedInstallLocation) {
            // If DE Testbed is not already installed, show prompt
            
            label.setText(""
                    + "Please select your DE Testbed installation directory "
                    + "(e.g. C:\\de_testbed\\de_testbed-gift_v1_0-vc110-x64) "
                    + "in order to automatically configure DE Testbed for use with GIFT.\n\n"
                    + "Note: if you have set the path to your DE Testbed before and can confirm that the '"+DE_TESTBED_HOME+"' "
                    + "Windows user environment variable exists and is set correctly, the problem is most likely that Windows isn't providing "
                    + "the updated environment variables to this Java process.  If your seeing this installer as the result of running the GIFT "
                    + "Gateway module jnlp when starting a GIFT course, then you will need to restart your browser.  If running GIFT Local, you will need to restart that as well.");
            
            statusImage.setFitWidth(24);
            statusImage.setFitHeight(24);
            statusImage.setImage(NOT_COMPLETED);
            inputProvidedMap.put(DE_TESTBED_HOME, false);
            deTestbedTitledPane.setExpanded(true);

        } else {
            label.setText("DE Testbed Installation Directiory");

            inputProvidedMap.put(DE_TESTBED_HOME, true);

        }
        
        // Setup File Chooser
        deTestbedFileChooser = new JFileChooser("C:/Users/");
        deTestbedFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        //disable the "select" button if a DE Testbed directory folder is not selected
        deTestbedFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    
                    File file = (File) evt.getNewValue();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible DE Testbed installation folder                                
                        for(String child : file.list()){
                            
                            if(child.equals(DE_TESTBED_HOME_BATCH)){
                                setSelectButtonState(deTestbedFileChooser, true);
                            }
                        }
                        
                    }
                    else{
                        setSelectButtonState(deTestbedFileChooser, false);
                    }
                }else{
                    setSelectButtonState(deTestbedFileChooser, false);
                }

                deTestbedFileChooser.repaint();
            }
        });

        // Setup directory field
        deTestbedDirectoryField = new TextField();
        deTestbedDirectoryField.setEditable(false);
        deTestbedDirectoryField.setMinSize(60, 20);
        browseBox.setPadding(new Insets(0,0,5,0));
        
        if(!askForDETestbedInstallLocation) {
            // Fill the directory field if DE Testbed is already installed
            
            deTestbedDirectoryField.setText((String) deTestbedHomeValue);
        }
        
        // Setup browse button
        deTestbedBrowseButton = new Button();
        deTestbedBrowseButton.setText(BROWSE);
        deTestbedBrowseButton.setMinWidth(100);
        
        deTestbedBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override public void handle(ActionEvent e) {
                File file = openChooseFolderDialog(deTestbedFileChooser);
                
                if(file != null){
                    String path = file.getAbsolutePath();
                    if(logger.isInfoEnabled()) {
                        logger.info("Chose directory of '" + path + "' as the DE Testbed installation directory.");
                    }
                    deTestbedDirectoryField.setText(path);
                    inputProvidedMap.put(DE_TESTBED_HOME, true);
                    setNextEnabled(shouldSetNextEnabled());
                    updatePaneStatusCallback.statusUpdated(true);
                }                
            }            
        });
        
        HBox.setHgrow(container, Priority.ALWAYS);
        HBox.setHgrow(deTestbedDirectoryField, Priority.ALWAYS);        
        browseBox.getChildren().addAll(deTestbedDirectoryField, deTestbedBrowseButton);
        container.getChildren().addAll(label, browseBox);
        wrapper.setPadding(new Insets(10,10,10,10)); 
        wrapper.getChildren().add(container);
        
        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollpane.setContent(wrapper);
        
        deTestbedTitledPane.setText(TrainingApplicationEnum.DE_TESTBED.getDisplayName());
        deTestbedTitledPane.setGraphic(statusImage);
        deTestbedTitledPane.setContent(scrollpane);
    }

    /**
     * Create GUI components for Unity GIFT Wrap installation information.
     * 
     * @param titledPane the pane to add user interface components too
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could
     *        affect the pane or the parent of the pane (e.g. the Training App information has been
     *        updated and therefore this pane is completed).
     */
    private void buildUnityGIFTWrapNode(TitledPane titledPane, UpdatePaneStatusCallback updatePaneStatusCallback) {

        if (logger.isInfoEnabled()) {
            logger.info("Building Unity GIFT Wrap install instructions.");
        }

        HBox wrapper = new HBox();
        VBox container = new VBox();
        ScrollPane scrollpane = new ScrollPane();

        ImageView statusImage = new ImageView(COMPLETED);
        statusImage.setFitWidth(24);
        statusImage.setFitHeight(24);
        statusImage.setImage(ATTENTION);

        /* check if the unity scenario project has already been downloaded */
        boolean alreadyDownloaded = false;

        File landNavScenarioFolder = new File(CommonProperties.getInstance().getTrainingAppsDirectory()
                + File.separator + PackageUtil.getLandNavScenario());

        DesktopFolderProxy searchFolder = new DesktopFolderProxy(landNavScenarioFolder);

        try {
            List<FileProxy> files = new ArrayList<>();
            FileFinderUtil.getFilesByName(searchFolder, files, "index.html");
            alreadyDownloaded = !files.isEmpty();
        } catch (@SuppressWarnings("unused") IOException e) {
            // assume already downloaded is false
        }

        titledPane.setExpanded(!alreadyDownloaded);
        if (alreadyDownloaded) {
            CheckBox downloadUnityCheckbox = new CheckBox(
                    "The Unity Land Nav scenario has already been downloaded for this GIFT instance.\n"+
                    "Would you like to over-write your instance with a new download?");
            downloadUnityCheckbox.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
            downloadUnityCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    downloadUnity = new_val;
                }
            });
            Label hintLabel = new Label(
                    "\nNote: This download may take a few minutes and an internet connection is required.");
            container.getChildren().addAll(downloadUnityCheckbox, hintLabel);

        } else {
            CheckBox downloadUnityCheckbox = new CheckBox(
                    "Download the Unity Land Nav scenario for GIFT Wrap authoring and course execution.");
            downloadUnityCheckbox.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
            downloadUnityCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    downloadUnity = new_val;
                }
            });
            Label hintLabel = new Label(
                    "\nNote: This download may take a few minutes and an internet connection is required.");
            container.getChildren().addAll(downloadUnityCheckbox, hintLabel);
        }

        wrapper.setPadding(new Insets(10, 10, 10, 10));
        wrapper.getChildren().add(container);

        HBox.setHgrow(container, Priority.ALWAYS);

        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollpane.setContent(wrapper);

        titledPane.setText("Unity (GIFT Wrap)");
        titledPane.setGraphic(statusImage);
        titledPane.setContent(scrollpane);
    }

    /**
     * Return whether or not this page's next button should be enabled.
     * The button should be enabled if all the inputs are not required, or if they
     * are required, then all of them are provided.
     * 
     * @return boolean
     */
    private boolean shouldSetNextEnabled(){
        
        //don't require all inputs to be populated
        if(!requireAll){
            return true;
        }
        
        for(Boolean reqMet : inputProvidedMap.values()){
            
            if(reqMet == null || !reqMet){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handles opening the file dialog, that allows the user to choose a directory.
     * 
     * @param fileChooser - the file chooser to open
     * @return File - the directory chosen, can be null if no directory was selected
     */
    private File openChooseFolderDialog(JFileChooser fileChooser) {
        if (logger.isInfoEnabled()) {
            logger.info("Opening choose folder dialog.");
        }
        int returnVal = fileChooser.showDialog(this, APPROVE_BUTTON_TEXT);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            
            File file = fileChooser.getSelectedFile();
            return file;
            
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Choose folder dialog cancelled.");
            }
            return null;
        }
    }
        
    /**
     * Set the enabled flag for the "select" button in the provided container.
     * 
     * @param c a container with components 
     * @param flag whether or not the "select" button should be enabled in the provided container (if found)
     */
    public static void setSelectButtonState(Container c, boolean flag) {
        
        int len = c.getComponentCount();
        for (int i = 0; i < len; i++) {
            
            Component comp = c.getComponent(i);

            if (comp instanceof JButton) {
                JButton b = (JButton) comp;

                if ( b.getText() != null && b.getText().equals(APPROVE_BUTTON_TEXT) ) {
                    b.setEnabled(flag);
                }

            } else if (comp instanceof Container) {
                  setSelectButtonState((Container) comp, flag);
            }
        }     
    }
    
    /**
     * Used for notification of pane events such as whether the user has provided
     * the necessary information for a training application to be configured with GIFT.
     * 
     * @author mhoffman
     *
     */
    private interface UpdatePaneStatusCallback{
        
        /**
         * Notification that a pane has either completed or not been completed.  To be completed
         * means the necessary information was missing and has now been provided during the execution
         * of this user interface.
         * 
         * @param completed
         */
        public void statusUpdated(boolean completed);

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

    /**
     * Create GUI components for VR-Engage installation information.
     * 
     * @param vrEngageTitledPane the pane to add user interface components too 
     * @param updatePaneStatusCallback used to notify the owner of the pane of events that could affect
     * the pane or the parent of the pane (e.g. the Training App information has been updated and therefore
     * this pane is completed).
     */
    private void buildVrEngageNode(TitledPane vrEngageTitledPane, UpdatePaneStatusCallback updatePaneStatusCallback){
        
        if(logger.isInfoEnabled()) {
            logger.info("Building VR-Engage install instructions.");
        }
        
        HBox wrapper = new HBox();
        HBox browseBox = new HBox();
        final VBox container  = new VBox();
        final VBox configBox = new VBox();
        ScrollPane scrollpane = new ScrollPane();
        ImageView alertImage = new ImageView(ATTENTION);
        ImageView statusImage = new ImageView(COMPLETED);               
        final Label infoLabel = new Label("Before running a GIFT course that uses VR-Engage, VR-Engage needs to be running.");
        
        alertImage.setFitWidth(19);
        alertImage.setFitHeight(20);
        infoLabel.setGraphic(alertImage);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black");
        infoLabel.setPadding(new Insets(10, 0, 10, 0));
        
        vrEngageHomeValue = UserEnvironmentUtil.getEnvironmentVariable(VR_ENGAGE_HOME);
        
        container.getChildren().add(infoLabel);        
        
        if(vrEngageHomeValue != null){
            //check the value
            
            if(logger.isInfoEnabled()) {
                logger.info("Checking "+VR_ENGAGE_HOME+" value of "+vrEngageHomeValue+".");
            }
            
            try{
                File vrEngageHomeDir = new File((String) vrEngageHomeValue);
                if(vrEngageHomeDir.exists()){
                    //directory exists, show user information about VR-Engage has already been configured for use with GIFT
                    //but still need to make sure all necessary VR-Engage scenario related files are in the appropriate VR-Engage folders (refer to installation documentation)
                    
                    Label label = new Label();
                    
                    if(!vrEngageFilesMissing((String) vrEngageHomeValue, false)) {
                        label.setText("It appears that VR-Engage has already been setup for interaction with GIFT based on the "
                            +VR_ENGAGE_HOME+" Windows environment variable being set.  Please check that all necessary VR-Engage "
                            + "scenario related files are in the appropriate VR-Engage folders.");
                        container.getChildren().add(label);
                        
                    } else {
                        // present the user with a list of files missing from the VR-Engage home directory
                        
                        configureVrEngage = true;
                        Label missingLabel = new Label(missingVrEngageFilesList.toString());
                        configBox.getChildren().addAll(label, missingLabel);
                        container.getChildren().add(configBox);
                        
                        if(vrEngageFileMap.size() < VR_ENGAGE_FILE_COUNT) {
                            // if only some of the files are missing, give the user the option to 
                            // copy and replace all files or to copy and replace missing files only
                            
                            HBox radioBox = new HBox();
                            final ToggleGroup radioGroup = new ToggleGroup();
                            RadioButton allFilesButton = new RadioButton("Install all files");
                            RadioButton missingFilesButton = new RadioButton("Install missing files");
                            Label configureLabel = new Label("These files are needed for GIFT to interact with VR_Engage. ");
                            infoLabel.setText("VR-Engage plugin files will be reinstalled.\n"
                                + "Before running a GIFT course that uses VR-Enage, VR-Engage needs to be running and at the main menu.");
                            label.setText("The following files were not found in the VR-Engage directory: ");
                            
                            allFilesButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
    
                                @Override
                                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                                    copyAllVrEngageFiles = newValue;
                                }
                                
                            });
                                                        
                            allFilesButton.setSelected(true);
                            allFilesButton.setToggleGroup(radioGroup);
                            missingFilesButton.setToggleGroup(radioGroup);
                            
                            radioBox.setSpacing(25);
                            configureLabel.setWrapText(true);
                            infoLabel.getGraphic().setTranslateY(-9);
                            missingLabel.setPadding(new Insets(5, 0, 10, 0));
                            configBox.setPadding(new Insets(0, 0, 10, 23));
                            radioBox.getChildren().addAll(allFilesButton, missingFilesButton);
                            configBox.getChildren().addAll(configureLabel, radioBox);
                            
                        } else {
                            // if all of the files are missing, don't show any options.
                            
                            label.setText("The following files were not found in the VR-Engage directory and will be installed: ");
                            missingLabel.setPadding(new Insets(5, 0, 0, 0));
                            configBox.setPadding(new Insets(0, 0, 0, 23));
                        }
                           
                    }
                    
                    Hyperlink link = new Hyperlink();
                    link.setText(INSTALL_DOCUMENT_LINK);
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            try {
                                Desktop.getDesktop().browse(INSTALL_DOCUMENTATION);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }                        
                        }
                    });

                    label.setWrapText(true);
                    link.setPadding(new Insets(13, -6, 13, 0));                    
                    container.getChildren().add(link);
                    
                    askForVrEngageInstallLocation = false;
                    
                }else{
                    logger.warn("The current '"+VR_ENGAGE_HOME+"' Windows Environment variable value of "+vrEngageHomeValue+" is not a valid directory on this computer.");
                                        
                    //show user information about how the current environment variable value is not valid
                    TextFlow flow = new TextFlow();
                    Text t1 = new Text("The "+VR_ENGAGE_HOME+" Windows environment variable value of '" 
                            + vrEngageHomeValue + "' is not valid. Check the installer log file in ");
                    Text t2 = new Text("GIFT/output/logger/tools/");
                    Text t3 = new Text(" for more information.\n");  
                    
                    t2.setStyle("-fx-font-style: italic");
                    flow.getChildren().addAll(t1, t2, t3);
                    container.getChildren().add(flow);
                }
                
            }catch(Exception e){
                logger.error("There was an exception thrown when trying to verify the '"+VR_ENGAGE_HOME+"' Windows Environment variable value of "+vrEngageHomeValue+".", e);
                                
                //show user information about how the current environment variable value is not valid
                TextFlow flow = new TextFlow();
                Text t1 = new Text("The "+VR_ENGAGE_HOME+" Windows environment variable value of '" 
                        + vrEngageHomeValue + "' is not valid. Check the installer log file in ");
                Text t2 = new Text("GIFT/output/logger/tools/");
                Text t3 = new Text(" for more information.\n");  
                
                t2.setStyle("-fx-font-style: italic");
                flow.getChildren().addAll(t1, t2, t3);
                container.getChildren().add(flow);
            }
        } 
        
        //show file browse component(s)
        
        Label label = new Label();
        label.setWrapText(true);
        
        if(askForVrEngageInstallLocation) {
            //if VR-Engage is not already installed, show prompt
            
            inputProvidedMap.put(VR_ENGAGE_HOME, false);
            
            label.setText(""
                    + ""
                    + "Please select your VR-Engage installation directory"
                    + " (e.g. C:\\MAK\\vrengage) in order"
                    + " to automatically configure VR-Engage for use with GIFT.");
            
            statusImage.setFitWidth(24);
            statusImage.setFitHeight(24);
            statusImage.setImage(NOT_COMPLETED);
            vrEngageTitledPane.setExpanded(true);
        } else {
            
            label.setText("VR-Engage Installation Directory:");
            inputProvidedMap.put(VR_ENGAGE_HOME, true);
            statusImage.setFitWidth(24);
            statusImage.setFitHeight(24);
            statusImage.setImage(ATTENTION);
        }
        
        // Setup File Chooser
        vrEngageFileChooser = new JFileChooser("C:/");
        vrEngageFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        //disable the "select" button if a VR-Engage directory folder is not selected
        vrEngageFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    
                    File file = (File) evt.getNewValue();
                    if (file != null && file.isDirectory()) { 
                        
                        //check for specific file(s) in the possible VR-Engage installation folder   
                        boolean exeFound = false;
                        
                        for(File subFolder : file.listFiles()) {
                            if(subFolder.isDirectory() && subFolder.getName().startsWith("bin")) {
                                
                                for(String child : subFolder.list()){
                                    
                                    if(child.matches("vrEngage.exe")){
                                        setSelectButtonState(vrEngageFileChooser, true);
                                        exeFound = true;
                                        break;
                                    }
                                }       
                            }
                        }
                        
                        if(!exeFound) {
                            setSelectButtonState(vrEngageFileChooser, false);
                        }
                        
                    } else {
                        setSelectButtonState(vrEngageFileChooser, false);
                    }
                } else {
                    setSelectButtonState(vrEngageFileChooser, false);
                }
                
                vrEngageFileChooser.repaint();
            }
        });

        // Setup directory field
        vrEngageDirectoryField = new TextField();
        vrEngageDirectoryField.setEditable(false);
        vrEngageDirectoryField.setMinSize(60, 20);
        
        if(!askForVrEngageInstallLocation) {
            // Fill the directory field if VR-Engage is already installed
            
            vrEngageDirectoryField.setText((String) vrEngageHomeValue);
        }
        
        // Setup browse button
        vrEngageBrowseButton = new Button();
        vrEngageBrowseButton.setText(BROWSE);
        vrEngageBrowseButton.setMinWidth(100);
        
        vrEngageBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override public void handle(ActionEvent e) {
                File file = openChooseFolderDialog(vrEngageFileChooser);
                String path = "";
                
                if(file != null){
                    path = file.getAbsolutePath();
                    if(logger.isInfoEnabled()) {
                        logger.info("Chose directory of '" + path + "' as the VR-Engage installation directory.");
                    }
                    vrEngageDirectoryField.setText(path);
                        
                    if(!path.equals(vrEngageHomeValue) || askForVrEngageInstallLocation) {
                        // If the directory field changed, configure VR-Engage
                        inputProvidedMap.put(VR_ENGAGE_HOME, true);
                        configureVrEngage = copyAllVrEngageFiles = true;
                        updatePaneStatusCallback.statusUpdated(true);
                    
                        if(container.getChildren().contains(configBox) && configBox.getChildren().size() > 1) {
                            // hide the missing files list if it was displayed.                         
                            configBox.getChildren().clear();
                            configBox.getChildren().add(new Label("The VR-Engage installation directory you "
                                    + "selected will automatically be configured for use with GIFT."));
                            infoLabel.setText("Before running a GIFT course that uses VR-Engage, VR-Engage needs to be running and at the main menu.");
                            infoLabel.getGraphic().setTranslateY(0);
                        }
                    
                    }
                    
                    setNextEnabled(shouldSetNextEnabled());
                }
            }            
        });
        
        wrapper.setPadding(new Insets(10,10,10,10)); 
        browseBox.setPadding(new Insets(0,0,10,0));
        
        Label readmeLabel = new Label();
        readmeLabel.setText("Note: Your VR-Engage player station must be set up to load GIFT's VR-Forces backend plugin in order "
                + "for GIFT to communicate with VR-Engage. To do this, please refer to the \"Steps to add GIFT plugin to VR-Engage\" install "
                + "instructions in\nTraining.Apps/VR-Engage/VTMAKPlugin/README.txt");
        readmeLabel.setWrapText(true);
        
        HBox.setHgrow(container, Priority.ALWAYS);
        HBox.setHgrow(vrEngageDirectoryField, Priority.ALWAYS);        
        browseBox.getChildren().addAll(vrEngageDirectoryField, vrEngageBrowseButton);
        container.getChildren().addAll(label, browseBox, readmeLabel);
        wrapper.getChildren().addAll(container);

        scrollpane.setFitToWidth(true);
        scrollpane.setPrefViewportHeight(185);
        scrollpane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollpane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollpane.setContent(wrapper);
        
        vrEngageTitledPane.setText(TrainingApplicationEnum.VR_ENGAGE.getDisplayName());
        vrEngageTitledPane.setGraphic(statusImage);
        vrEngageTitledPane.setContent(scrollpane);
    }
    
    /**
     * Determines whether or not GIFT plugin files are missing from the VR-Engage installation directory.
     * If plugin files are missing, the VR-Engage file map is populated with source and destination paths.
     * 
     * @param vrEngagePath the path to the VR-Engage directory
     * @param addAllFiles whether or not all files should be installed. If false, only the missing 
     * files will be installed.
     * @return true if there are files missing, false otherwise.
     */
    private boolean vrEngageFilesMissing(String vrEngagePath, boolean addAllFiles) {
    
        File domainDir = new File(CommonProperties.getInstance().getDomainDirectory());
        String trainingApps = domainDir.getParent() + File.separator + "Training.Apps/VR-Engage";
        StringBuilder loggerMsg = new StringBuilder();
        vrEngageFileMap.clear();
        
        if(addAllFiles) {
            loggerMsg.append("The following files will be installed to the VR-Engage installation directory: ");
        } else {
            loggerMsg.append("The following files were not found in the VBS installation directory and will be installed: ");
        }
        
        // check for missing files
        File vrEngagePlugin = new File (vrEngagePath + "/plugins64/vrForces/release/VrfGiftPluginDIS.dll");
        if(!vrEngagePlugin.exists() || addAllFiles) {
            vrEngageFileMap.put(new File(trainingApps + "/VTMAKPlugin/VrfGiftPluginDIS.dll"), vrEngagePlugin);
            missingVrEngageFilesList.append(BULLET).append("VrfGiftPluginDIS.dll \n");
            loggerMsg.append("VrfGiftPluginDIS.dll, ");
        }
        
        File vrEngageProps = new File (vrEngagePath + "/plugins64/vrForces/release/VrfGiftPlugin.properties");
        if(!vrEngageProps.exists() || addAllFiles) {
            vrEngageFileMap.put(new File(trainingApps + "/VTMAKPlugin/VrfGiftPlugin.properties"), vrEngageProps);
            missingVrEngageFilesList.append(BULLET).append("VrfGiftPlugin.properties \n");
            loggerMsg.append("VrfGiftPlugin.properties, ");
        }
        
        File vrEngageScriptExr = new File (vrEngagePath + "/data/simulationModelSets/VR-Engage/scripts/gift_script_executor.lua");
        if(!vrEngageScriptExr.exists() || addAllFiles) {
            vrEngageFileMap.put(new File(trainingApps + "/VTMAKPlugin/gift_script_executor.lua"), vrEngageScriptExr);
            missingVrEngageFilesList.append(BULLET).append("gift_script_executor.lua \n");
            loggerMsg.append("gift_script_executor.lua, ");
        }
        
        File vrEngageScriptXml = new File (vrEngagePath + "/data/simulationModelSets/VR-Engage/scripts/gift_script_executor.xml");
        if(!vrEngageScriptXml.exists() || addAllFiles) {
            vrEngageFileMap.put(new File(trainingApps + "/VTMAKPlugin/gift_script_executor.xml"), vrEngageScriptXml);
            missingVrEngageFilesList.append(BULLET).append("gift_script_executor.xml \n");
            loggerMsg.append("gift_script_executor.xml, ");
        }
        
        return !vrEngageFileMap.isEmpty();
    }
}

