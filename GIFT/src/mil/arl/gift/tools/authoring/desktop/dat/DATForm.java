/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat;

import java.io.File;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import generated.dkf.Scenario;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.IdGenerator;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.SurveyContextDialog;
import mil.arl.gift.ums.db.UMSDatabaseManager;

/**
 * This class extends the XML authoring tool form by providing DAT specific parameters.
 * In addition it can validate the dkf xml using the domain handler.
 * 
 * @author mhoffman
 *
 */
public class DATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DATForm.class);
    
    /** the default starting location for the dkf browser */
    private static final String DEFAULT_DOMAIN_DIRECTORY = ".."+File.separator+"Domain";
    private static final DesktopFolderProxy DOMAIN_DIR = new DesktopFolderProxy(new File(DEFAULT_DOMAIN_DIRECTORY));
    
    /** default title of form and prefix used when dkf has been loaded*/
    private static final String TITLE_PREFIX = "DKF Authoring Tool"; 
    
    /** the DKF documentation */
    private static URI DKF_DOC_FILE = HelpDocuments.getDKFDoc();
    
    /** the root generated class for the dkf content*/
    private static final Class<?> ROOT_CLASS = Scenario.class;
    
    private static final String TASKS = "tasks";
    private static final String TASK = "task";
    private static final String CONCEPT = "concept";
    private static final String NODE_ID = "nodeId";
    private static final String SURVEY_CONTEXT = "surveyContext";
    
    //the following block checks to make sure the class structure of Tasks->Task, Tasks and Concept from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{         
        generated.dkf.Tasks tasks = new generated.dkf.Tasks();
        tasks.getTask();
        generated.dkf.Task task = new generated.dkf.Task();
        task.getConcepts();
        task.getNodeId();
        generated.dkf.Concept concept = new generated.dkf.Concept();
        concept.getNodeId();
    }
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Domain Knowledge File (DKF) Authoring Tool (DAT).\n" +
    		"\tIt contains information on how to assess a lesson (e.g. training application instance/scenario) in a GIFT course.\n";
    
    /**
     * Default constructor - use default attributes
     * @throws DetailedException if there was a problem with the dkf schema file
     */
    public DATForm() throws DetailedException{
        this(AbstractSchemaHandler.DKF_SCHEMA_FILE);
    }
    
    /**
     * Class constructor - specify custom schema
     * 
     * @param schemaFile - the dkf schema to validate dkfs against
     * @throws DetailedException if there was a problem with the dkf schema file
     */
    public DATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, DEFAULT_DOMAIN_DIRECTORY, TITLE_PREFIX, FILE_COMMENT, DKF_DOC_FILE, AbstractSchemaHandler.DKF_FILE_EXTENSION);    
    }
    
    /**
     * Initialize and show the authoring tool form
     * @throws Throwable if there was a problem connecting to the UMS database
     */
    public void init() throws Throwable{
                
        if(CommonProperties.getInstance().shouldUseDBConnection()){
            //establish db connection up front 
          
            //show custom wait dialog
            //Note: not using JOptionPane here because it blocks the thread
            JDialog dialog = new JDialog();
            JLabel label = new JLabel("<html><br>Please wait while the UMS database connection is established.<br><br></html>");
            dialog.setLocationRelativeTo(this);
            dialog.setTitle("Please Wait...");
            dialog.add(label);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
            dialog.pack();
            dialog.setVisible(true);
        
            //disable the authoring tool window until the DB connection is finished or fails
            this.setEnabled(false);           
            
            try{
                UMSDatabaseManager.getInstance();
                dblView.showInfoMessage("Connection successfully established to Survey database");
            }catch(Throwable e){
                logger.error("Caught error while trying to establish connection to survey database", e);
                throw e;
            } finally{
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.dispose();
            }
                
            //re-enable the window for use
            this.setEnabled(true);
        }

        //Note: delaying showing the dialog until the DB connection has been established.
        super.setVisible(true);
    }
    
    @Override
    public void cleanup(){
        
        try {
            UMSDatabaseManager.getInstance().cleanup();
        } catch (Throwable e) {
            logger.error("Caught exception while trying to cleanup the UMS db manager.", e);
        }

        super.cleanup();
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
    	
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
        
        if (conversionUtil != null) {
    		UnmarshalledFile uFile = conversionUtil.convertScenario(fileToConvert, true, true);
    		generated.dkf.Scenario newScenario = (Scenario)uFile.getUnmarshalled();
   
    		// The original file has been converted to the latest version of GIFT
    		return newScenario;
    	}
        
        // The user cancelled the conversion
    	return null;
    }
    
    @Override
    protected boolean newXML(){
        
        if(super.newXML()){
            
            //reset id generator for new file
            IdGenerator.setGlobalId(0);
            
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void giftValidate(FileProxy contentFile) throws Throwable{        
        GIFTValidationResults validationResults = ValidationUtil.validateDKF(contentFile, DOMAIN_DIR, true);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw validationResults.getFirstError();
        }
    }
    
    @Override
    protected boolean loadXML(File file){
        
        //reset for new file contents being loaded
        SurveyContextDialog.resetCurrentSurveyContextId();
        
        if(super.loadXML(file)){
            
            //get survey context value
            Document dom = this.model.getDocument();
            
            NodeList nl = dom.getElementsByTagNameNS(CommonProperties.COMMON_NAMESPACE, SURVEY_CONTEXT);
            if(nl != null && nl.getLength() == 1){
                
                String contextIdStr = nl.item(0).getTextContent();
                if(contextIdStr != null && contextIdStr.length() > 0){
                    //successfully found survey context id element, save its value
                    SurveyContextDialog.setCurrentSurveyContextId(Integer.valueOf(contextIdStr));
                }
            }
            
            //
            //get highest performance node id
            //
            int highestNodeId = 0;
            Element rootNode = getRootNode();
            
            nl = rootNode.getElementsByTagName(TASKS);
            if(nl != null && nl.getLength() == 1){
                //found tasks element, the root of the Task/Concept hierarchy
                
                Element tasks = (Element)nl.item(0);
                
                nl = tasks.getElementsByTagName(TASK);
                if(nl != null){
                    
                    //get node info for each task and it's concepts
                    for(int taskIndex = 0; taskIndex < nl.getLength(); taskIndex++){
                        
                        Element task = (Element)nl.item(taskIndex);
                        String nodeIdStr = task.getAttribute(NODE_ID);
                        if(nodeIdStr == null || nodeIdStr.length() == 0){
                            continue;
                        }
                        
                        int nodeId = Integer.valueOf(nodeIdStr);
                        if(nodeId > highestNodeId){
                            highestNodeId = nodeId;
                        }
                        
                        NodeList conceptsNL = task.getElementsByTagName(CONCEPT);
                        if(conceptsNL != null){
                            
                            //get node info for each task's concept
                            for(int conceptIndex = 0; conceptIndex < conceptsNL.getLength(); conceptIndex++){
                                
                                Element concept = (Element)conceptsNL.item(conceptIndex);
                                nodeIdStr = concept.getAttribute(NODE_ID);
                                if(nodeIdStr == null || nodeIdStr.length() == 0){
                                    continue;
                                }
                                nodeId = Integer.valueOf(nodeIdStr);
                                
                                if(nodeId > highestNodeId){
                                    highestNodeId = nodeId;
                                }
                            }
                        }
                    }//end for
                }
            }
                
            IdGenerator.setGlobalId(highestNodeId);
            
            return true;
        }
        
        return false;
        
    }
}
