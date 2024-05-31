/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fg.ftreenodes.FTextLabelNode;
import com.fg.ftreenodes.FToggleNode;
import com.fg.ftreenodes.FToggleSwitchNode;
import com.fg.xmleditor.FXView;

import generated.course.Course;
import generated.course.Guidance;
import generated.course.Interop;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.TrainingApplication;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.LessonMaterialFileFilter;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.XMLParseUtil;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.IdGenerator;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.SurveyContextDialog;
import mil.arl.gift.ums.db.UMSDatabaseManager;

/**
 * This class extends the XML authoring tool form by providing CAT specific parameters.
 * In addition it can validate the course xml using the domain handler.
 * 
 * @author mhoffman
 *
 */
public class CATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CATForm.class);
    
    /** default title of form and prefix used when course has been loaded*/
    private static final String TITLE_PREFIX = "Course Authoring Tool"; 
    
    /** the course documentation */
    private static URI COURSE_DOC_FILE = HelpDocuments.getCourseDoc();
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Course Authoring Tool (CAT).\n" +
            "\tIt contains information about the flow of a course in GIFT.\n";
    
    /** the root generated class for the course content*/
    private static final Class<?> ROOT_CLASS = generated.course.Course.class;
    
    /** course XML tags */
    private static final String SURVEY_CONTEXT_TAG = "surveyContext";
    private static final String TRANSITIONS_TAG = "transitions";
    private static final String TRANSITIONS_NAME_TAG = "transitionName";
    private static final String GUIDANCE_CHOICE_TAG = "Choice:";
    
    /** auto generated guidance transition strings */
    private static final String TRANSITION_NAME = "Auto Generated Guidance";
    private static final String DEFAULT_MESSAGE = "<html>The pre-requisites to run this course are:<ol>" +
            "<li>pre-requisite A</li>" +
            "<li>pre-requisite B</li>" +
            "<li>pre-requisite C</li>" +
            "</ol></html>";
    
    /**
     * Default constructor - use default attributes
     * @throws DetailedException if there was a problem with the course schema file
     */
    public CATForm() throws DetailedException{
        this(AbstractSchemaHandler.DKF_SCHEMA_FILE);
    }
    
    /**
     * Class constructor - specify custom schema
     * 
     * @param schemaFile - the course schema to validate courses against
     * @throws DetailedException if there was a problem with the course schema file
     */
    public CATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, CATProperties.getInstance().getWorkspaceDirectory(), TITLE_PREFIX, FILE_COMMENT, COURSE_DOC_FILE, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
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

        }
        
        //NOTE: calling setVisible has to be done here because at this time the CAT (just like all the GIFT Authoring tools) exposes the first level
        //of nodes in the schema and the course.xsd has the survey context node near the top.  This causes the survey context dialog class
        //to load once the tool window is visible, thereby attempting to connect to the UMS db.  Placing the setVisible call as the first
        //line of code in this method would cause 2 simultaneous UMS connection attempts. 
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
            UnmarshalledFile uFile = conversionUtil.convertCourse(fileToConvert, true, true);
            generated.course.Course newCourse = (Course)uFile.getUnmarshalled();
        
            return newCourse;
        }
        
        // User cancelled
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
        
        try{
            File courseFolder = new File(contentFile.getFileId()).getParentFile();
            ValidationUtil.validateCourse(contentFile, new DesktopFolderProxy(courseFolder), true, true, null);       
        } catch (DKFValidationException e) {
            throw new CourseFileValidationException("There was a problem with a DKF file referenced by the course file.\n"+e.getReason(),
                    e.getDetails(),
                    contentFile.getFileId(),
                    e);
        }
    }
    
    @Override
    protected boolean loadXML(File file){
        
        //reset for new file contents being loaded
        SurveyContextDialog.resetCurrentSurveyContextId();
        
        if(super.loadXML(file)){
            
            //get survey context value
            Document dom = this.model.getDocument();
            
            Element surveyContext = XMLParseUtil.getChild(dom.getDocumentElement(), SURVEY_CONTEXT_TAG);
            if(surveyContext != null){
                
                //successfully found survey context id element, save its value
                SurveyContextDialog.setCurrentSurveyContextId(Integer.valueOf(surveyContext.getTextContent()));
            }
            
            //reset generic id generator for new file
            IdGenerator.setGlobalId(0);
            
            return true;
        }
        
        return false;
        
    }
    
    @Override
    protected boolean saveXML(File file){
        
        if(super.saveXML(file)){
            
            DomainCourseFileHandler courseHandler = null;
            try{
                courseHandler = new DomainCourseFileHandler(new FileProxy(file), new DesktopFolderProxy(file.getParentFile()), true);
                CourseValidationResults validationResults = courseHandler.checkCourse(true, null);
                if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
                    return true;
                }
            }catch(Throwable e){              
                //if unable to parse the course successfully, don't continue with customized save logic as it might fail or be incomplete
                logger.warn("Caught exception while trying to validate course file named: "+file.getAbsolutePath()+", therefore the lesson material content will not be extracted for resuse", e);
                return true;
            }
            
            Course course = courseHandler.getCourse();
            List<Serializable> transitions = course.getTransitions().getTransitionType();
            
            //
            // If there is a training application instance in the course, ask the author if they would like to add a guidance course
            // transition stating any pre-reqs for that training application prior to running a course.
            //
            if(!transitions.isEmpty()){
                StringBuffer sb = null;
                
                for(Serializable transition : transitions){
                    
                    if(transition instanceof TrainingApplication){
                        
                        if(sb == null){
                            
                            sb = new StringBuffer();
                            sb.append("Would you like to add a guidance transition to the beginning of this course with pre-requisite information\n")
                                    .append("about the following training application transitions:\n\n");
                            
                            //setup table
                            sb.append("<html><table><tr><td>Transition Name</td><td>Implementation</td></tr>");
                        }
                        
                        TrainingApplication application = (TrainingApplication)transition;
                        
                        //optional
                        String transitionName = application.getTransitionName();                        

                        sb.append("<tr><td>");
                        if(transitionName != null && transitionName.length() > 0){
                            sb.append(transitionName);
                        }else{
                            sb.append("not-provided");
                        }
                        sb.append("</td>");
                                  
                        boolean firstRow = true;
                        for(Interop interop : application.getInterops().getInterop()){
                            
                            String interopImpl = interop.getInteropImpl();
                            
                            if(!firstRow){
                                sb.append("</tr><tr><td></td>");
                            }
                            
                            sb.append("<td>");
                            if(interopImpl != null && interopImpl.length() > 0){
                                sb.append(interopImpl);
                            }else{
                                sb.append("not-selected");
                            }
                            sb.append("</td>");
                            
                            firstRow = false;
                        }
                        
                        sb.append("</tr>");
                    }
                }
                
                if(sb != null){
                    //show choice dialog
                    
                    sb.append("</table></html>");
                    
                    int choice = JOptionPane.showConfirmDialog(this,
                            sb.toString(),
                            "Add Guidance Transition?",
                            JOptionPane.YES_NO_OPTION);
                    
                    if(choice == 0){
                        //auto-add guidance transition
                        
                        logger.info("Auto generating a new Guidance transition to put in the beggining of the course.");

                        FToggleNode root =  (FToggleNode)model.getRoot();
                        for(int i = 0; i < root.getChildCount(); i++){
                            
                            FToggleNode child = (FToggleNode)root.getChildAt(i);
                            if(child.getLabelText().equals(TRANSITIONS_TAG)){
                                //found transitions element
                                
                                //create new child transition
                                FToggleNode arrayNode = (FToggleNode)child.getChildAt(0);                                
                                FToggleSwitchNode newTransition = (FToggleSwitchNode) model.insertInstance(arrayNode, 0);
                                
                                //select guidance as the transition type
                                boolean found = false;
                                int elementIndex = -1;
                                //Note: can't call getChildCount here because it returns zero
                                Enumeration<?> enumeration = newTransition.children();
                                while(enumeration.hasMoreElements()){
                                    
                                    elementIndex++;
                                    if(((FTextLabelNode)enumeration.nextElement()).getLabelText().equals(Guidance.class.getSimpleName())){
                                        found = true;
                                        break;
                                    }
                                }
                                
                                if(found){
                                    
                                    //"guidance" selection done here
                                    FXView leftView = dblView.getLeftView();
                                    newTransition.switchBranch(leftView.getTree(), elementIndex);
                                    
                                    //create default child elements of guidance node
                                    FTextLabelNode newGuidance = (FTextLabelNode) newTransition.getSubstituteNode();                                    
                                    model.populateNode(newGuidance);
                                    
                                    //populate guidance descendant elements
                                    for(int j = 0; j < newGuidance.getChildCount(); j++){
                                        
                                        FToggleNode guidanceElement = (FToggleNode)newGuidance.getChildAt(j);
                                        if(guidanceElement.getLabelText().equals(TRANSITIONS_NAME_TAG)){
                                            
                                            guidanceElement.setToggleSelected(true);
                                            guidanceElement.setValue(TRANSITION_NAME);
                                            model.setNodeValue(guidanceElement, TRANSITION_NAME);
                                            
                                        }else if(guidanceElement.getLabelText().equals(GUIDANCE_CHOICE_TAG)){                                            
                          
                                            //find Message choice                                             
                                            found = false;
                                            elementIndex = -1;
                                            //Note: can't call getChildCount here because it returns zero
                                            enumeration = guidanceElement.children();
                                            while(enumeration.hasMoreElements()){
                                                
                                                elementIndex++;
                                                if(((FTextLabelNode)enumeration.nextElement()).getLabelText().equalsIgnoreCase(Guidance.Message.class.getSimpleName())){
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            
                                            if(found){
                                                
                                                //"message" selection done here
                                                ((FToggleSwitchNode)guidanceElement).switchBranch(leftView.getTree(), elementIndex);
                                                
                                                //create default child elements of message node
                                                FTextLabelNode newMessage = (FTextLabelNode) guidanceElement.getSubstituteNode();                                    
                                                model.populateNode(newMessage);
                                                
                                                //populate guidance descendant elements with some generic helper message 
                                                FToggleNode contentNode = (FToggleNode) newMessage.getChildAt(0);
                                                contentNode.setValue(DEFAULT_MESSAGE);
                                                model.setNodeValue(contentNode, DEFAULT_MESSAGE);
                                            }

                                        }
                                    }//end for
                                    

                                }else{
                                    logger.error("Unable to find the transition choice of "+Guidance.class.getSimpleName()+", therefore a new Guidance transition will not be automatically created.");
                                    
                                    JOptionPane.showMessageDialog(this,
                                            "Failed to find the transition choice of "+Guidance.class.getSimpleName()+", therefore a new Guidance transition will not be automatically created.",
                                            "Failed to Auto Generated Guidance Transition",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                                
                                //save again, since changes were made
                                super.saveXML(file);
                                
                                JOptionPane.showMessageDialog(this,
                                        "Successfully added a guidance transition to the beginning of the course.  Please edit the guidance message according to the specific pre-requisites of this course.",
                                        "Guidance Transition Added",
                                        JOptionPane.INFORMATION_MESSAGE);
                                
                                break;
                                
                            }//end if
                            
                        }//end for
                    }//end if
                }//end if sb
                
            }//end if transitions
            
            
            //
            // now that the course file is saved, pull out the lesson material sections and create new xml files (lessonMaterial.xml) for reuse
            // in other course.xml files.
            //
            
            if(CATProperties.getInstance().getGenerateLessonMaterialFile()){
            
                try{
                    
                    String courseFilePath = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator));
                    
                    Marshaller marshaller;
                    try {
                        JAXBContext jaxbContext = JAXBContext.newInstance(LessonMaterialList.class);
                        marshaller = jaxbContext.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    } catch (JAXBException e) {
                        logger.error("Caught JAXB exception while creating marshaller", e);
                        throw new FileValidationException("Unable to create lesson material reference files.",
                                e.getMessage(),
                                file.getAbsolutePath(),
                                e);
                    }                
                    
                    int lessonMaterialIndex = 1;
                    for(Object transition : transitions){
                        
                        if(transition instanceof LessonMaterial){
                            
                            LessonMaterial lMaterial = (LessonMaterial)transition;                        
                            
                            //get lesson material content and create XML file from it
                            LessonMaterialList lessonMaterialList = lMaterial.getLessonMaterialList();
                            if(lessonMaterialList != null){                   
                                
                                try {  
                                    FileOutputStream fos = new FileOutputStream(courseFilePath + File.separator + "auto-generated_"+course.getName() + "." +lessonMaterialIndex+ "." + TimeUtil.formatCurrentTime() + "." + LessonMaterialFileFilter.FILE_EXTENSION);
                                    marshaller.marshal(lessonMaterialList, fos);
                                    lessonMaterialIndex++;
                                    fos.close();
                                    
                                } catch (Exception e) {
                                    logger.error("Failed to create lesson material reference file because exception was caught", e);
                                }
                            }
                        }
                    }
                }catch(FileValidationException e){
                    logger.error("Caught error while trying to e", e);
                    dblView.showErrorMessage("The contents where saved, however an error was reported, check CAT log for more details.");
                }
            
            }
            
            return true;
        }
        
        return false;
    }

}
