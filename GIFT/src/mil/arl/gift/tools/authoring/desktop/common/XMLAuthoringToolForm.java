/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.TreeNode;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.fg.ftree.FCancelException;
import com.fg.ftree.FTreeEditorEvent;
import com.fg.ftree.FTreeEditorListener;
import com.fg.ftree.FTreeExpansionListener;
import com.fg.ftree.FTreeNodeEvent;
import com.fg.ftree.FTreeSelectionListener;
import com.fg.ftreenodes.FAbstractToggleNode;
import com.fg.ftreenodes.FTextField;
import com.fg.ftreenodes.FTextFieldNode;
import com.fg.ftreenodes.FToggleNode;
import com.fg.ftreenodes.FToggleSwitchNode;
import com.fg.xmleditor.FXDocumentModel;
import com.fg.xmleditor.FXDocumentModelImpl;
import com.fg.xmleditor.FXDoubleView;
import com.fg.xmleditor.FXModelException;
import com.fg.xmleditor.FXModelStatusListener;
import com.fg.xmleditor.FXStatusEvent;
import com.fg.xmleditor.FXView;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.tools.authoring.common.conversion.LatestVersionException;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;

/**
 * This is the main dialog of the XML Authoring tool.  It allows the user to load, edit and validate an xml file
 * with an associated xsd.
 * 
 * @author mhoffman
 */
public abstract class XMLAuthoringToolForm extends javax.swing.JFrame implements FXModelStatusListener, FTreeSelectionListener, FTreeExpansionListener, FTreeEditorListener {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLAuthoringToolForm.class);    
    
    /** documentation on using the tool user interface */
    private static URI toolDocumentation = HelpDocuments.getXMLAuthoringToolDoc();
    
    /** version attributes and values */
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String ENCODING = "UTF-8";
    
    /** create generic schema validation error dialog */
    private static JFrame VALIDATION_ERROR_DIALOG = new JFrame();
    static{
        
        VALIDATION_ERROR_DIALOG.setTitle("Schema Validation Error");
        VALIDATION_ERROR_DIALOG.setPreferredSize(new Dimension(365, 210));
        VALIDATION_ERROR_DIALOG.setResizable(false);
        
        JLabel introLabel = new JLabel("<html><br>There has been a schema validation error.</html>");
        
        String imagePath = "src"+File.separator+"mil"+File.separator+"arl"+File.separator+"gift"+File.separator+"common"+File.separator+"images"+File.separator+"findInvalidNode.png";
        ImageIcon icon = new ImageIcon(imagePath);

        JLabel useFindNodeLabel = new JLabel("<html>Try using the 'Find Invalid Node' button on the<br>tool bar of the authoring tool dialog<br>to find the invalid node.</html>");
        useFindNodeLabel.setIcon(icon);
        
        JLabel checkLogLabel = new JLabel("Another option is to check the log for more details.\n");

        VALIDATION_ERROR_DIALOG.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(0, 10, 0, 0);
        VALIDATION_ERROR_DIALOG.getContentPane().add(introLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.ipadx = 30;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(5, 20, 0, 10);
        VALIDATION_ERROR_DIALOG.getContentPane().add(useFindNodeLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.ipadx = 25;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(18, 20, 0, 0);
        VALIDATION_ERROR_DIALOG.getContentPane().add(checkLogLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.ipadx = 63;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(10, 200, 0, 0);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                VALIDATION_ERROR_DIALOG.setVisible(false);
                
            }
        });
        VALIDATION_ERROR_DIALOG.getContentPane().add(okButton, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.ipadx = 300;
        c.ipady = 9;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(0, 10, 0, 0);
        VALIDATION_ERROR_DIALOG.getContentPane().add(new JSeparator(), c);          

        VALIDATION_ERROR_DIALOG.pack();
        
        //DEBUG
        //VALIDATION_ERROR_DIALOG.setVisible(true);
    }
    
    /** the xml schema file */
    private File schemaFile;
    
    /** flag used to determine if the user doesn't want to update the schema file during the current tool execution */
    private boolean ignoreSchemaUpdate = false;
    
    /** the default starting location for the file browser */
    private String defaultBrowsePath = ".";
    
    private String contentFileSuffix;
    
    /** default title of form and prefix used when xml file has been loaded*/
    private String titlePrefix; 
    
    /** a comment to add to the top of any saved file */
    private String toolFileComment = null;
    
    /** the documentation for the tool instance (e.g. DAT) */
    private URI schemaDocumentation;
    
    /** xml editor components used to render the xml contents */
    protected FXDoubleView dblView;
    protected FXDocumentModel model;
    protected FToggleNode selectedNode;
    
    /** the current xml file */
    private File contentFile = null;
    
    /** for loading xmls */
    private JFileChooser fileDlg;
    
    private Class<?> rootElementClass;
    
    /** 
     * used to keep the current value of the selected textfield being authored in order to set it as the value if the
     * user selects a component on the form that would normally cause the textfield value to be cleared. 
     */
    private String textFieldBuffer = "";

    /** 
     * Creates new form XMLAuthoringToolForm
     * 
     *  @param schemaFile - the schema file used to validate the xml 
     *  @param rootElementClass - the root generated class for the xml content
     *  @param defaultBrowsePath - the path to default to when showing file browsers
     *  @param titlePrefix - the title (prefix) to show on the form
     *  @param fileComment - a comment to place at the top of every file saved
     *  @param helpDocumentation - the file containing more documentation and will be linked to the help file menu option. Can be null.
     *  @param contentFileSuffix - the suffix or extension used by xml files being authored
     * @throws DetailedException if the schema caused an exception
     */
    public XMLAuthoringToolForm(File schemaFile, Class<?> rootElementClass, String defaultBrowsePath, String titlePrefix, String fileComment,
            URI helpDocumentation, String contentFileSuffix) throws DetailedException {
        
        if(contentFileSuffix == null){
            throw new IllegalArgumentException("The content suffix can't be null");
        }

        setSchemaFile(schemaFile);
        this.defaultBrowsePath = defaultBrowsePath;
        this.titlePrefix = titlePrefix;
        this.contentFileSuffix = contentFileSuffix;
        this.rootElementClass = rootElementClass;
        this.toolFileComment = fileComment;
        
        initComponents();
        
        this.setIconImage(ImageUtil.getInstance().getSystemIcon());

        if(helpDocumentation != null){
            this.schemaDocumentation = helpDocumentation;
        }else{
            logger.warn("Disabling schema documentation help menu item because the documentation at "+helpDocumentation+" was not found.");
            this.schemaDocumentationMenuItem.setEnabled(false);
        }        
        
        customInit();        
        
        //enable conversion wizard
        configureConversionWizard();
    }
    
    /**
     * Set the schema file to use for validating and building the XML file
     * 
     * @param schemaFile
     */
    private void setSchemaFile(File schemaFile){
        
        if(schemaFile == null || !schemaFile.exists()){
            throw new IllegalArgumentException("The schema file of "+schemaFile+" needs to exist");
        }
        
        this.schemaFile = schemaFile;
    }
    
    /**
     * Return the schema file for the xml content being authored.
     * 
     * @return File
     */
    public File getSchemaFile(){
        return schemaFile;
    } 
    
    /**
     * Display the schema validation error dialog that provides generic help for schema related issues.
     */
    protected void showSchemaValidationErrorDialog(){
        VALIDATION_ERROR_DIALOG.setLocationRelativeTo(this);
        VALIDATION_ERROR_DIALOG.setVisible(true);
    }
    
    /**
     * Gather the contents of the exception provided including message
     * and cause.
     * 
     * @param exception the exception to analyze
     * @param sb the buffer to place the information 
     */
    private void buildExceptionStack(Throwable exception, StringBuilder sb){
    	
        sb.append(exception.getMessage());
        sb.append("\n");
        for (StackTraceElement ste : exception.getStackTrace()) {
            sb.append("    ").append(ste.toString()).append("\n");
        }
        
        if(exception.getCause() != null){
        	buildExceptionStack(exception.getCause(), sb);
        }
    }
    
    /**
     * Shows a dialog to the user with a stack trace of the exception provided.
     * 
     * @param title the title to give to the dialog
     * @param exception the exception information to show to the user in a dialog
     */
    @SuppressWarnings("unused")
    private void showExceptionDialog(String title, FileValidationException exception){
    	
    	if(exception == null){
    		return;
    	}
        
        StringBuilder sb = new StringBuilder("Error: ");
        buildExceptionStack(exception, sb);

        JTextArea jta = new JTextArea(sb.toString());
        jta.setEditable(false);
        JScrollPane jsp = new JScrollPane(jta){

			private static final long serialVersionUID = 1L;

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 320);
            }
        };
        JOptionPane.showMessageDialog(
            this, jsp, title, JOptionPane.ERROR_MESSAGE);

    }
    
//    /**
//     * Return the currently selected node in the tree.
//     * Note: can be null if there is no selected node.
//     * 
//     * @return FToggleNode
//     */
//    public FToggleNode getSelectedNode(){
//        return selectedNode;
//     }
    
    /**
     * Return the root node of the XML document.  This is useful for manipulating the XML nodes.
     * 
     * @return Element the DOM document root node element
     */
    public Element getRootNode(){
        return model.getDocument().getDocumentElement();
    }
    
    /**
     * Return the root node of the swing-based tree component.  This is useful for manipulating the GUI components of the
     * authoring tool.
     * 
     * @return Object
     */
    public Object getTreeRootNode(){
        return model.getRoot();
    }
    
    /**
     * Populates a empty Element node with all child nodes. 
     * Performs operation and returns true when operation is allowed and does nothing and returns false otherwise.
     * 
     * @param parentNode the node to create child default elements for
     * @return boolean Whether the operation succeeded.
     */
    public boolean populateNode(FToggleNode parentNode){
        return model.populateNode(parentNode);
    }
    
    /**
     * Assigns a new value to specified FToggleNode node.
     * 
     * @param node the node to assign a new value too
     * @param newValue the value to assign to the node
     */
    public void setNodeValue(FToggleNode node, Object newValue){
        model.setNodeValue(node, newValue);
    }
    
    /**
     * Insert a new FToggleNode instance of Element, Substitution Group or Model Group into an Array folder at the specified position. 
     * If an instance can be inserted, the method returns reference to the new instance, otherwise it returns null.
     * 
     * @param arrayNode FToggleNode object, which represents Array folder node.
     * @param index the location in the array to insert a new node
     * @return FToggleNode a reference to the inserted instance or null when operation is not allowed.
     */
    public FToggleNode insertInstance(FToggleNode arrayNode, int index){
        return model.insertInstance(arrayNode, index);
    }
    
    /**
     * Used to select a choice element.
     * 
     * @param choice the choice element that has one or more choices.
     * @param elementIndex the location of the item to choose in the choice element.
     */
    public void switchBranch(FToggleSwitchNode choice, int elementIndex){
        FXView leftView = dblView.getLeftView();
        choice.switchBranch(leftView.getTree(), elementIndex);
    }

    /**
     * Called when the form is closing and is useful for cleaning up any logic that was started
     * by using this form.
     */
    public void cleanup(){
        
    }
    
    /**
     * Close this frame gracefully.
     */
    public void close(){
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * Custom initialization of the form.  Instantiate the xml editor component with the schema.
     * @throws DetailedException if the schema caused an exception
     */
    private void customInit() throws DetailedException{
        
        setTitle();
        
        this.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e){
                cleanup();
            }
        });
        
        fileDlg = new JFileChooser(new File(defaultBrowsePath).getAbsolutePath());
        fileDlg.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith(contentFileSuffix)) {
                    return true;
                } else if (file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "*"+contentFileSuffix;
            }
        });
        fileDlg.setAcceptAllFileFilterUsed(false);

        //setup the xmleditor
        model = new FXDocumentModelImpl();
        model.addModelStatusListener(this);

        dblView = new FXDoubleView(model);
        dblView.setSize(this.xmleditorPanel.getSize());        
        
        //and add it to the panel
        this.xmleditorPanel.setLayout(new BorderLayout());
        this.xmleditorPanel.add(dblView);
        
        logger.info("Loaded schema file of "+schemaFile.getAbsolutePath());
        System.out.println("Schema file = "+schemaFile.getAbsolutePath());        
        loadSchema(schemaFile);
        
        //create empty xml by default
        newXML();

        //setup listening for node selection events
        FXView leftView = dblView.getLeftView();
        FXView rightView = dblView.getRightView();
        leftView.getTree().addFTreeEditorListener(this);
        leftView.getTree().addFTreeSelectionListener(this);
        leftView.getTree().addFTreeExpansionListener(this);
        rightView.getTree().addFTreeSelectionListener(this);
        rightView.getTree().addFTreeExpansionListener(this);
        rightView.getTree().addFTreeEditorListener(this);
        
        //disable automatic vertical scrolling whenever the text in any of the message areas is updated.
        for(JTextArea messageArea : getMessageAreaList()){
        	((DefaultCaret) messageArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    
    /**
     * Set the title of the DAT form to include the name of the dkf xml file being used
     */
    private void setTitle()
    {
        if(contentFile != null){
            saveXMLMenuItem.setEnabled(true);
            setTitle(titlePrefix + " : " + contentFile.getName());
        }else{
            saveXMLMenuItem.setEnabled(false);
            setTitle(titlePrefix);
        }
    }
    
    /**
     * Execute the conversion wizard logic for this tool.
     * 
     * @param fileToConvert - the XML file containing elements validated against the previous schema version for the particular file type.
     * @return Object - the generated class object as a result of the conversion process
     * @throws Exception if there was a severe error during the conversion process
     */
    protected abstract Object executeConversion(FileProxy fileToConvert) throws Exception;
    
    /**
     * Return the course folder for the XML file being authored in this class.  
     * 
     * @param forceSave if true and the file hasn't been saved
     * this method will present the file save as dialog to the user.  Once saved, a recursive search up the
     * file tree is performed searching for a course.xml file.  Once a course file is found it's parent folder
     * is deemed the course folder of this file.  That course folder is returned.  If false and the file hasn't been saved, null is returned.
     * @return the course folder for this file being authored.  Null if the course folder hasn't been determined yet, most likely
     * because the file hasn't been saved.
     */
    public File updateCourseFolder(boolean forceSave){
        
        if(contentFile == null){
            
            if(forceSave){
                //force user to save file first
                
                JOptionPane.showConfirmDialog(this, 
                        "<html>You must save this XML file inside a course folder before continuing.<br>"
                        + "A course folder is a workspace folder (e.g. Public or 'username' folder) that contains a course.xml file.<br><br>"
                        + "For example: 'mycourse' is a course folder if Domain\\workspace\\jsmith\\mycourse\\A.course.xml exists.<br><br>"
                        + "<b>If you don't save the file correctly it will most likely not validate.</b></html>", 
                        "Save before continuing", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE); 
                
                 if(!saveAsXML()){
                     XMLAuthoringToolFormManager.getInstance().setCourseFolder(null);
                     return null;
                 }
            }else{
                return null;
            }
            
        }else if(XMLAuthoringToolFormManager.getInstance().getCourseFolder() != null){
            //the content file is saved and the course folder was previously saved
            return XMLAuthoringToolFormManager.getInstance().getCourseFolder();
        }
        
        //find course folder by recursively searching up the file tree until a course.xml file is found
        try{
            XMLAuthoringToolFormManager.getInstance().setCourseFolder(FileFinderUtil.findAncestorCourseFolder(contentFile.getParentFile()));
        }catch(IOException e){
            XMLAuthoringToolFormManager.getInstance().setCourseFolder(null);
            logger.error("Caught exception while trying to find the course folder to '"+contentFile+"'.", e);
        }
        
        return XMLAuthoringToolFormManager.getInstance().getCourseFolder();
    }
    
    /**
     * Return the course folder for the XML file being authored in this class.  If the file hasn't been saved
     * this method will present the file save as dialog to the user.  Once saved, a recursive search up the
     * file tree is performed searching for a course.xml file.  Once a course file is found it's parent folder
     * is deemed the course folder of this file.  That course folder is returned.
     * 
     * @return the course folder for this file being authored
     */
    public File updateCourseFolder(){
        return updateCourseFolder(true);
    }
    
    /**
     * Configure the conversion wizard logic, thereby enabling the feature in the implementing authoring tool.
     */
    protected void configureConversionWizard(){
        conversionWizardMenuItem.setEnabled(true);
    }
        
    /**
     * Validate the xml file by parsing it using GIFT source
     * 
     * @param contentFile the file containing the contents to validate
     * @throws Exception if there was any type of problem validating the XML file
     */
    protected void giftValidate(FileProxy contentFile)throws Throwable{        
        //should be implemented by classes that extend this class (e.g. DATForm.java)
    }
    
    /**
     * The schema was successfully loaded so enable the appropriate form components for the user
     */
    private void setSchemaEnabled(){
        this.loadXMLMenuItem.setEnabled(true);
    }
    
    /**
     * Based on whether the xml was successfully loaded or not, set the appropriate access to form components for the user
     * 
     * @param enabled - whether or not the xml was successfully loaded
     */
    private void setXMLEnabled(boolean enabled){
        this.saveAsXMLMenuItem.setEnabled(enabled);
        this.saveXMLMenuItem.setEnabled(enabled);
        this.validateMenuItem.setEnabled(enabled);
    }
    
    /**
     * Load the schema file to use for xml validation
     * 
     * @param file - the xml xsd
     * @return boolean - whether the schema was successfully loaded
     */
    private void loadSchema(File file) throws DetailedException {
        
        try {
            
            if(!ignoreSchemaUpdate){
                //check if schema version number is up-to-date
                
                String schemaVersion = getSchemaVersion();
                String giftVersion = Version.getInstance().getCurrentSchemaVersion();
                if(!schemaVersion.equals(giftVersion)){
                    
                    //ask user if they want to auto-update the schema version attribute value
                    Object[] options = {"Yes", "Maybe later", "Not anytime soon"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "The schema version of "+schemaVersion+" in "+file+"\n\rdiffers from the GIFT version of "+giftVersion+".\n\r" +
                                    "Would you like to update the schema version value in that file automatically?",
                            "Update Schema Version",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    switch(choice){
                        case 0:
                            
                            if(updateSchemaVersion()){
                                JOptionPane.showMessageDialog(this,
                                    "Successfully updated schema file.",
                                    "Schema Updated",
                                    JOptionPane.PLAIN_MESSAGE);
                            }else{
                                JOptionPane.showMessageDialog(this,
                                        "Failed to update schema file, check log for more details.",
                                        "Update failed",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            break;
                        case 2:
                            logger.info("User choose to not update the schema therefore not asking again during the current program execution.");
                            ignoreSchemaUpdate = true;
                            break;
                       default:
                    	   break;
                    }
                }
            }
            
            model.newDocument(file.toURI().toURL());
            setSchemaEnabled();
            dblView.showInfoMessage("XML Schema: " + file.getAbsolutePath());
            
        } catch (Exception ex) {
            logger.error("Caught exception while trying to load schema of "+file.getAbsolutePath(), ex);
            dblView.showErrorMessage("Error: " + ex.toString());
            JOptionPane.showMessageDialog(this, "Can't load the XML Schema",
              "Error", JOptionPane.ERROR_MESSAGE);
            throw new DetailedException("Failed to load the schema.",
                    "The schema file "+file.getAbsolutePath()+" caused an error.",
                    ex);
        }
    }
    
    /**
     * Update the schema version attribute value and write the update schema contents to disk.
     */
    private boolean updateSchemaVersion(){
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document doc = db.parse(schemaFile);
            
            //get the root element
            Element docEle = doc.getDocumentElement();

            String schemaVersion = docEle.getAttribute(VERSION_ATTRIBUTE);
            if(schemaVersion != null){
                logger.info("Updating schema version attribute value from "+schemaVersion+" to "+Version.getInstance().getCurrentSchemaVersion());
            }else{
                logger.info("Setting schema version attribute value to "+Version.getInstance().getCurrentSchemaVersion());
            }
            
            docEle.setAttribute(VERSION_ATTRIBUTE, Version.getInstance().getCurrentSchemaVersion());
            
            // write the content into xml file
            FileOutputStream out = new FileOutputStream(schemaFile);
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput format = impl.createLSOutput();
            
            writer.getDomConfig().setParameter("format-pretty-print", true);
            format.setEncoding(ENCODING);
            format.setByteStream(out);
            writer.write(doc, format);
                        
            logger.info("Schema file of "+schemaFile.getAbsolutePath()+" updated");

        }catch(Exception e) {
            logger.error("Caught exception while trying to update the schema version", e);
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    /**
     * Create the required nodes of the schema
     * 
     * @return whether a new XML document was created
     */
    protected boolean newXML(){
                
        if (!XMLCloseConfirmation()){
            return false;
        }
        
        try {
            
            if (model.getSchemaURL() == null){
                throw new FXModelException("No XML Schema loaded");
            }

            //Note: using newDocument with just the schema URL param failed to produce a new document 
            model.newDocument(schemaFile.toURI().toURL(), rootElementClass.getSimpleName());
            setXMLEnabled(true);
            setContentFile(null);
            
          } catch (Exception ex) {
              logger.error("Caught exception while creating new xml", ex);
              setXMLEnabled(false);
              dblView.showErrorMessage("Error: " + ex.getMessage());
              JOptionPane.showMessageDialog(this, "Can't open new XML Document", "Error", JOptionPane.ERROR_MESSAGE);
              return false;
          }
        
          setTitle();
          return true;
    }
    
    private void setContentFile(File contentFile){
        this.contentFile = contentFile;
        
        //only update the course folder when the content file is being set, otherwise
        //when the tool is loading a New file this method is called which would then cause
        //the save as dialog to appear which is not what we want to happen right away
        if(contentFile != null){
            updateCourseFolder();
        }else{
            //reset
            XMLAuthoringToolFormManager.getInstance().setCourseFolder(null);
        }
        
        XMLAuthoringToolFormManager.getInstance().setCurrentFile(contentFile);
    }
    
    /**
     * Find a child node in the authoring tool tree under the given parent with the given name.
     * 
     * @param parent The parent to check it's children
     * @param name - the label of an xml node to find as a child to the parent node.
     * @return FAbstractToggleNode - the first child node found to have the name specified.  Can be null if no node was found.
     */
    public static FAbstractToggleNode findChildNodeByName(FAbstractToggleNode parent, String name){
        
        if(parent == null){
            return null;
        }else if(name == null){
            return null;
        }
        
        int childCnt = parent.getChildCount();
        for(int i = 0; i < childCnt; i++){
            
            TreeNode node = parent.getChildAt(i);
            if(node instanceof FAbstractToggleNode && ((FAbstractToggleNode)node).getLabelText().equals(name)){
                return (FAbstractToggleNode) node;
            }
        }
        
        return null;
    }

    /**
     * Load the XML contents into the xmleditor component.
     * 
     * @param file - the XML file 
     * @return boolean - whether or not the XML content was successfully loaded
     */
    protected boolean loadXML(File file) {
        
        try {
            
            boolean success = loadXML(file.toURI().toURL(), file.getAbsolutePath());
            setContentFile(file);
            setTitle();
            return success;
            
        }catch (Exception ex) {
            
            logger.error("Caught exception while trying to load XML", ex);
            setXMLEnabled(false);
            dblView.showErrorMessage("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Can't load the XML Document", "Error", JOptionPane.ERROR_MESSAGE);
            setTitle();
            return false;
        }
    }
    
    /**
     * Load the XML contents into the xmleditor component.
     * 
     * @param source - the source of XML content {URL, Document} 
     * @return boolean - whether or not the XML content was successfully loaded
     */
    private boolean loadXML(Object source, String sourceName) {
        
        try {
            
            List<?> lostElements;
            if(source instanceof URL){
                lostElements = model.openDocument(model.getSchemaURL(), (URL)source);
            }else if(source instanceof Document){
                lostElements = model.openDocument(model.getSchemaURL(), (Document)source);
            }else{
                throw new IllegalArgumentException("Received unhandled xml source of "+source+" to load.");
            }
            
            if(lostElements != null){
                
                StringBuffer sb = new StringBuffer("Error: The source XML document is invalid.\n" +
                  "The following elements have not been loaded:");
                
                //build string showing elements not loaded
                for (int i = 0; i < lostElements.size(); i++) {
                    sb.append("\n");
                    int k = sb.length();
                    Node element = (Node)lostElements.get(i);
                    sb.append(element.getNodeName());
                    Node node = element.getParentNode();
                    while (node != null && !(node instanceof Document)){
                      sb.insert(k, node.getNodeName() + "/");
                      node = node.getParentNode();
                    }
                }
                
                dblView.showErrorMessage(sb.toString());
                
            }else{
                dblView.showErrorMessage("");
            }
            
            //enable save as menu item
            setXMLEnabled(true);
            setTitle();
            logger.info("Loaded xml from "+sourceName);
            dblView.showInfoMessage("XML: " + sourceName);

            return true;
            
        } catch (Exception ex) {
            logger.error("Caught exception while trying to load XML", ex);
            setXMLEnabled(false);
            dblView.showErrorMessage("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Can't load the XML Document", "Error", JOptionPane.ERROR_MESSAGE);
            setTitle();
            return false;
        }
    }
  
    
    /**
     * Present a file save dialog and save the xml contents to the file selected.
     * 
     * @return boolean - whether the xml contents were successfully written to the file or not
     * @throws SAXException 
     */
    private boolean saveAsXML(){
        
        File file = browseForFileToSave();
        if (file != null){
            
            //add suffix if not already in file name
            if (!file.getName().endsWith(contentFileSuffix)){
                file = new File(file.getAbsolutePath() + contentFileSuffix);
            }
            
            return saveXML(file);
        }
        
        return false;
    }
    
    /**
     * Save the xml contents to the file specified.
     * 
     * @param file - the file to save too
     * @return boolean - whether the xml contents were successfully written to the file or not
     */
    protected boolean saveXML(File file){
        return saveXML(file, model.getDocument(), model.isDocumentValid(), true);
    }
    
    /**
     * Save the xml contents to the file specified.
     * 
     * @param file - the file to save too
     * @param doc - the entire XML document
     * @param isValid - whether or not the document is valid (i.e. adheres to the schema)
     * @param updateTool - whether or not to update the tool dialog or not.  You would want to update the tool
     *                     if the saved document contents are currently loaded into the tool.
     * @return boolean - whether the xml contents were successfully written to the file or not
     */
    protected boolean saveXML(File file, Document doc, boolean isValid, boolean updateTool)
    {
        if (doc == null){
            logger.error("Unable to save the xml contents because the xml editor document is null");
            dblView.showErrorMessage("Unable to save xml, check log for more details");
            return false;
        }

        if(file != null){
            
            FileOutputStream out = null;
            Writer writer = null;
            try {
            	LSSerializer serializer = null;
                file.createNewFile();
                out = new FileOutputStream(file);
                
                //prepare the output
                DOMImplementation impl = doc.getImplementation();
                DOMImplementationLS implLS = (DOMImplementationLS)impl.getFeature("LS", "3.0");
                LSOutput format = implLS.createLSOutput();
                format.setEncoding(ENCODING);
                format.setByteStream(out); 
                serializer = implLS.createLSSerializer();
                serializer.getDomConfig().setParameter("format-pretty-print", true);
                
                //increment the file version for this save
                incrementVersion(doc, true);
                
                //write comment about authoring tool to top of the XML file
                if(toolFileComment != null){
                    final Comment comment = doc.createComment(toolFileComment);
                    Node node = doc.getDocumentElement();
                    doc.insertBefore(comment, node);
                }
                
                // write to file
                serializer.write(doc, format);
                
                if(updateTool){
                    //only update the authoring tool dialog and it's objects if the document is the content currently
                    //loaded in the tool
                    
                    model.setDocumentChanged(false);
                
                    setContentFile(file);
                    setTitle();
                }
                
                logger.info("Saved XML contents to file named "+file.getAbsoluteFile());
                dblView.showInfoMessage("Saved XML contents to file named "+file.getAbsolutePath());
                dblView.showErrorMessage("");
                
                if(!isValid){
                    //show warning message
                    JOptionPane.showMessageDialog(this, 
                            "The authored content is not valid against the schema.  Therefore this file\n" +
                            "should not be used with GIFT until the contents are correct.  Using\n" +
                            "this incomplete file can result in GIFT not executing correctly. \n\n" +
                            "Please use the Validate menu option to be presented with the issue(s) found.", 
                            "File saved but contents are not valid", 
                            JOptionPane.WARNING_MESSAGE);
                }
                
                return true;
                
            } catch (Exception e) {
                logger.error("Caught exception while trying to save xml to "+file.getAbsolutePath(), e);
                dblView.showErrorMessage("Caught exception while trying to save xml to "+file.getAbsolutePath()+":"+ e.getMessage());
                return false;
                
            } finally {
                try { 
                    if(out != null){
                        out.close(); 
                    }
                } catch (@SuppressWarnings("unused") Exception ignore) {}
                
                try { 
                    if(writer != null){
                        writer.close(); 
                    }
                } catch (@SuppressWarnings("unused") Exception ignore) {}
            }
            
        }
      
        dblView.showErrorMessage("Unable to save xml because the file is null");
        return false;
    }
    
    /**
     * Increment the XML file's version value.
     * 
     * version format is "a.b.c" where
     *     a = xsd major version
     *     b = xsd minor version
     *     c = XML file version  
     * 
     * @param doc - the Dom document to update the version node for
     * @param updateToolNode - whether or not to update the version's node value shown in the XML authoring tool.
     *          You would not want to do this if the current XML content is different than the document provided here.
     */
    private void incrementVersion(Document doc, boolean updateToolNode){
        
        try{
            NamedNodeMap map = doc.getDocumentElement().getAttributes();
            if(map != null){
                Attr versionAttr = (Attr)map.getNamedItem(VERSION_ATTRIBUTE);
                if(versionAttr == null) {
                    versionAttr = doc.createAttribute(VERSION_ATTRIBUTE);
                        }
                        
                String schemaVersion = getSchemaVersion();  //default prefix of schema version
                String currentVersion = versionAttr.getValue();
                String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
                
                //This if condition should always be true. I considered
                //deleting it but I may not be familar enough with the code to
                //knw this for a fact.
                if(newVersion != null && newVersion.length() > 0){
                    
                    logger.info("File version changed from "+versionAttr.getValue()+" to "+newVersion+".");
                    versionAttr.setValue(newVersion);
                    map.setNamedItemNS(versionAttr);
                    
                    if(updateToolNode){
                    
                        //cause version attribute to be refreshed on tool's document
                        FToggleNode root =  (FToggleNode)model.getRoot();
                        for(int i = 0; i < root.getChildCount(); i++){
                            
                            FToggleNode child = (FToggleNode)root.getChildAt(i);
                            if(child.getLabelText().equals(VERSION_ATTRIBUTE)){
                                
                                child.setToggleSelected(true);
                                child.setValue(newVersion);
                                model.setNodeValue(child, child.getValue());
                                
                                logger.info("File version changed from "+child.getValue()+" to "+newVersion+" in the XML authoring tool.");
                                
                                break;
                            }
                            
                        }
                    }
                   
                }
            }
        }catch(Exception e){
            logger.error("Caught exception while trying to increment file version value", e);
        }
    }
    
    /**
     * Parse the schema file and get the version attribute value
     * 
     * @return String - schema version attribute value, a default value will be returned if the schema contains no version attribute.
     */
    private String getSchemaVersion(){
        String version = CommonUtil.getSchemaVersion(schemaFile);
        return version;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPaneButtonGroup = new javax.swing.ButtonGroup();
        xmleditorPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenuItem = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        loadXMLMenuItem = new javax.swing.JMenuItem();
        saveXMLMenuItem = new javax.swing.JMenuItem();
        saveAsXMLMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        validateMenuItem = new javax.swing.JMenuItem();
        validateAllMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        reloadSchemaMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        conversionWizardMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        viewMenuItem = new javax.swing.JMenu();
        horizontalSplitCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        verticalSplitCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        syncCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        xmlContentMenuitem = new javax.swing.JMenuItem();
        helpMenuItem = new javax.swing.JMenu();
        schemaDocumentationMenuItem = new javax.swing.JMenuItem();
        toolDocumentationMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("datFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout xmleditorPanelLayout = new javax.swing.GroupLayout(xmleditorPanel);
        xmleditorPanel.setLayout(xmleditorPanelLayout);
        xmleditorPanelLayout.setHorizontalGroup(
            xmleditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 828, Short.MAX_VALUE)
        );
        xmleditorPanelLayout.setVerticalGroup(
            xmleditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 619, Short.MAX_VALUE)
        );

        fileMenuItem.setText("File");
        fileMenuItem.setMargin(new java.awt.Insets(0, 10, 0, 10));

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        newMenuItem.setText("New");
        newMenuItem.setToolTipText("Create a new XML file using the schema to create the initial structure.");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(newMenuItem);
        fileMenuItem.add(jSeparator3);

        loadXMLMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        loadXMLMenuItem.setText("Load");
        loadXMLMenuItem.setEnabled(false);
        loadXMLMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadXMLMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(loadXMLMenuItem);

        saveXMLMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveXMLMenuItem.setText("Save");
        saveXMLMenuItem.setEnabled(false);
        saveXMLMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveXMLMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(saveXMLMenuItem);

        saveAsXMLMenuItem.setText("Save As...");
        saveAsXMLMenuItem.setEnabled(true);
        saveAsXMLMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsXMLMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(saveAsXMLMenuItem);
        fileMenuItem.add(jSeparator2);

        validateMenuItem.setText("Validate");
        validateMenuItem.setToolTipText("Validate this content using GIFT source code to check for issues.");
        validateMenuItem.setEnabled(false);
        validateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(validateMenuItem);

        validateAllMenuItem.setText("Validate All Files");
        validateAllMenuItem.setToolTipText("Find all XML content files of this type and validate using GIFT source code to check for issues.");
        validateAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                try{
                    validateAllMenuItemActionPerformed(evt);
                }catch(Exception e){
                    logger.error("Caught exception while trying to validate all XML files for this authoring tool type.", e);
                    dblView.showErrorMessage("Exception occurred while trying to validate all XML files for this authoring tool type.  Check this tool's log file.");
                }
            }
        });
        fileMenuItem.add(validateAllMenuItem);
        fileMenuItem.add(jSeparator5);

        reloadSchemaMenuItem.setText("Re-Load Schema");
        reloadSchemaMenuItem.setToolTipText("Reload the XML Schema for this tool");
        reloadSchemaMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadSchemaMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(reloadSchemaMenuItem);
        fileMenuItem.add(jSeparator1);

        conversionWizardMenuItem.setText("Conversion Wizard");
        conversionWizardMenuItem.setEnabled(false);
        conversionWizardMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conversionWizardMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(conversionWizardMenuItem);
        fileMenuItem.add(jSeparator6);

        closeMenuItem.setText("Exit");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(closeMenuItem);

        menuBar.add(fileMenuItem);

        viewMenuItem.setText("View");
        viewMenuItem.setMargin(new java.awt.Insets(0, 0, 0, 10));

        splitPaneButtonGroup.add(horizontalSplitCheckBoxMenuItem);
        horizontalSplitCheckBoxMenuItem.setSelected(true);
        horizontalSplitCheckBoxMenuItem.setText("Horizontal Split");
        horizontalSplitCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalSplitCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewMenuItem.add(horizontalSplitCheckBoxMenuItem);

        splitPaneButtonGroup.add(verticalSplitCheckBoxMenuItem);
        verticalSplitCheckBoxMenuItem.setText("Vertical Split");
        verticalSplitCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalSplitCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewMenuItem.add(verticalSplitCheckBoxMenuItem);
        viewMenuItem.add(jSeparator4);

        syncCheckBoxMenuItem.setText("Synchronize");
        syncCheckBoxMenuItem.setToolTipText("Synchronizes the selection of nodes in the two tree views ");
        syncCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewMenuItem.add(syncCheckBoxMenuItem);
        viewMenuItem.add(jSeparator8);

        xmlContentMenuitem.setText("Content as XML");
        xmlContentMenuitem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmlContentMenuitemActionPerformed(evt);
            }
        });
        viewMenuItem.add(xmlContentMenuitem);

        menuBar.add(viewMenuItem);

        helpMenuItem.setText("Help");

        schemaDocumentationMenuItem.setText("Schema Documentation");
        schemaDocumentationMenuItem.setToolTipText("Opens the Schema documentation in your default program for .doc files.");
        schemaDocumentationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemaDocumentationMenuItemActionPerformed(evt);
            }
        });
        helpMenuItem.add(schemaDocumentationMenuItem);

        toolDocumentationMenuItem.setText("Tool Help");
        toolDocumentationMenuItem.setToolTipText("Opens this tool's documentation in your default program for .doc files.");
        toolDocumentationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolDocumentationMenuItemActionPerformed(evt);
            }
        });
        toolDocumentationMenuItem.setEnabled(toolDocumentation != null);
        helpMenuItem.add(toolDocumentationMenuItem);
        helpMenuItem.add(jSeparator7);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenuItem.add(aboutMenuItem);

        menuBar.add(helpMenuItem);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(xmleditorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(xmleditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The validate menu item was selected.  Validate the dkf contents against GIFT source logic used by modules such as the Domain module.
     * 
     * @param evt
     */
    private void validateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateMenuItemActionPerformed
        
        //clear error message panel in case an error is created with the following logic
        dblView.showErrorMessage("");
        
        //used for dialog created in this method
        final Component parentComponent = this;
        
        final JDialog pleaseWaitDialog = new JDialog();
        
        //This task will handle the validation call
        //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
        final SwingWorker<Void, Void> validationWorker = new SwingWorker<Void, Void>() {
            
            @Override
            protected Void doInBackground() throws Exception {  
                
                dblView.showInfoMessage("Validating XML...");
                
                try{
                    giftValidate(new FileProxy(contentFile));
                    dblView.showInfoMessage("Validation Complete");
                    
//                }catch(FileValidationException e){
//                    logger.error("Caught error while trying to GIFT-validate the XML contents authored", e);
//                    dblView.showErrorMessage("The contents were saved, however an error was detected.  Check the appropriate authoring tool log for more details.");
//                    
//                    if(e.getException() instanceof UnmarshalException){
//                        showSchemaValidationErrorDialog();
//                    }else if(e.getException() instanceof IllegalArgumentException){
//                    	showExceptionDialog("Error Validating "+contentFile.getName(), e);
//                    }
                    
                }catch(FileValidationException e){
                    logger.error("Caught error while trying to GIFT-validate the XML contents authored", e);
                    dblView.showErrorMessage("The contents were saved, however an error was detected.\n\nCheck the appropriate authoring tool log for more details.");
                    
                    if(e.getCause() != null && e.getCause() instanceof UnmarshalException){
                        showSchemaValidationErrorDialog();
                    }else{
                        JOptionPaneUtil.showConfirmDialog("The contents were saved, however an error was detected.\n\n"+e+"\n\nCheck the appropriate authoring tool log for more details.", "Failed to validate", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }

                }catch(Throwable t){
                    logger.error("Caught error while trying to GIFT-validate, check appropriate authoring tool log for more details.", t);
                    dblView.showErrorMessage("ERROR: "+t.getMessage()+", check appropriate authoring tool log for more details.");
                }
                
                pleaseWaitDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                pleaseWaitDialog.dispose();
                
                return null;
            }
        };
        
        //This task will handle the save call
        //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
        final SwingWorker<Void, Void> saveWorker = new SwingWorker<Void, Void>() {
            
            private boolean shouldValidate = false;
            
            @Override
            protected Void doInBackground() throws Exception { 
                
                try{
                    if(performAppropriateSave()){
                        shouldValidate = true;
                    }else {
                    	pleaseWaitDialog.dispose();
                    	dblView.showInfoMessage("Validation aborted");
                    }
                    
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                return null;
            } 
            
            @Override
            protected void done() {
                
                if(shouldValidate){
                    //start the worker task (which will also dispose of the 'please wait' dialog)
                    validationWorker.execute(); 
                }
            }
        };
        
        //Handle whether or not to save first and then validate or validate right away 
        //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
        
                if(model.isDocumentChanged()){
                    //need to save before validating
                    
                    int option = JOptionPane.showConfirmDialog(parentComponent,
                            "The XML Document must be saved before GIFT validation can begin. \nNote: Please de-select the last edited node to completely capture all edits.",
                            "information", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    if (option == JOptionPane.CANCEL_OPTION){
                        dblView.showInfoMessage("Validation aborted");
            
                    }else if (option == JOptionPane.OK_OPTION){               
                        
                        // show custom 'please wait' dialog
                        JLabel label = new JLabel("<html><br>Please wait for the save and validate logic to finish.<br><br></html>");
                        pleaseWaitDialog.setLocationRelativeTo(parentComponent);
                        pleaseWaitDialog.setTitle("Please Wait...");
                        pleaseWaitDialog.add(label);
                        pleaseWaitDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        pleaseWaitDialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                        pleaseWaitDialog.pack();
                        pleaseWaitDialog.setVisible(true);
                        
                        //start the worker task (which will also dispose of the 'please wait' dialog)
                        saveWorker.execute();                                                

                    }
                    
                }else if(saveXMLMenuItem.isEnabled()){
                    //contents match whats in the saved file
                    
                    // show custom 'please wait' dialog
                    JLabel label = new JLabel("<html><br>Please wait for the validation logic to finish.<br><br></html>");
                    pleaseWaitDialog.setLocationRelativeTo(parentComponent);
                    pleaseWaitDialog.setTitle("Please Wait...");
                    pleaseWaitDialog.add(label);
                    pleaseWaitDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    pleaseWaitDialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                    pleaseWaitDialog.pack();
                    pleaseWaitDialog.setVisible(true);
                    
                    //start the worker task (which will also dispose of the 'please wait' dialog)
                    validationWorker.execute(); 
                    
                }else{
                    dblView.showInfoMessage("There is no content to validate.");
                }
            }
                    
        });

    }//GEN-LAST:event_validateMenuItemActionPerformed

    /**
     * The close menu item was selected.  Close the form.
     * 
     * @param evt
     */
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        
        if(applicationClosing()){  
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            close();
            System.exit(0);
        }
          
    }//GEN-LAST:event_closeMenuItemActionPerformed
    
    /**
     * Handle prompts for when the form is closing.  
     * 
     * @return boolean - whether the form should close or not based on the success/failure or choices made by the user in the prompts.
     */
    private boolean applicationClosing(){
        
        if(model.isDocumentChanged()){
            
            int option = JOptionPane.showConfirmDialog(this,
                    "Would you like to save before exiting?",
                    "Save", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION){                
                return performAppropriateSave();
                
            }else if(option == JOptionPane.CANCEL_OPTION){
                return false;
            }else if(option == JOptionPane.CLOSED_OPTION){
                return false;
            }
        }
        
        return true;
    }

    /**
     * The Save As menu item was selected.  Save the XML using a file save as dialog.
     * 
     * @param evt
     */
    private void saveAsXMLMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsXMLMenuItemActionPerformed
        
        //used for dialog created in this method
        final Component parentComponent = this;
        
        //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {              
                
                // show custom 'please wait' dialog
                final JDialog dialog = new JDialog();
                JLabel label = new JLabel("<html><br>Please wait for the file to be saved.<br><br></html>");
                dialog.setLocationRelativeTo(parentComponent);
                dialog.setTitle("Please Wait...");
                dialog.add(label);
                dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                dialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                dialog.pack();
                dialog.setVisible(true); 
                
                //Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
                final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        
                        try{
                            saveAsXML();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        //dispose of please wait dialog
                        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.dispose();
                        
                        return null;
                    }
                };
                
                //start the worker task (which will also dispose of the 'please wait' dialog)
                worker.execute();                
        
            }
            
        });
        
    }//GEN-LAST:event_saveAsXMLMenuItemActionPerformed

    /**
     * The Save menu item was selected.  Save the XML using the current file as the file to write too.
     * 
     * @param evt
     */
    private void saveXMLMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveXMLMenuItemActionPerformed
        
        //used for dialog created in this method
        final Component parentComponent = this;
        
        //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {              
                
                // show custom 'please wait' dialog
                final JDialog dialog = new JDialog();
                JLabel label = new JLabel("<html><br>Please wait for the file to be saved.<br><br></html>");
                dialog.setLocationRelativeTo(parentComponent);
                dialog.setTitle("Please Wait...");
                dialog.add(label);
                dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                dialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                dialog.pack();
                dialog.setVisible(true); 
                
                //Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
                final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        
                        try{
                            saveXML(contentFile); 
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        //dispose of please wait dialog
                        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.dispose();
                        
                        return null;
                    }
                };
                
                //start the worker task (which will also dispose of the 'please wait' dialog)
                worker.execute();                
        
            }
            
        });
    }//GEN-LAST:event_saveXMLMenuItemActionPerformed

    /**
     * The Load menu item was selected.  Load a XML using a file select dialog.
     * 
     * @param evt
     */
    private void loadXMLMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadXMLMenuItemActionPerformed
        
        //check whether the xml should be closed or not
        if (!XMLCloseConfirmation()){
            return;
        }

        final File file = browseForFileToOpen();
        if(file != null){ 
            
            //used for dialog created in this method
            final Component parentComponent = this;
            
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {              
                    
                    // show custom 'please wait' dialog
                    final JDialog dialog = new JDialog();
                    JLabel label = new JLabel("<html><br>Please wait for the file to be loaded.<br><br></html>");
                    dialog.setLocationRelativeTo(parentComponent);
                    dialog.setTitle("Please Wait...");
                    dialog.add(label);
                    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    dialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                    dialog.pack();
                    dialog.setVisible(true); 
                    
                    //Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
                    final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        
                        @Override
                        protected Void doInBackground() throws Exception {
                            
                            try{
                                loadXML(file); 
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            
                            //dispose of please wait dialog
                            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                            dialog.dispose();
                            
                            return null;
                        }
                    };
                    
                    //start the worker task (which will also dispose of the 'please wait' dialog)
                    worker.execute();                
            
                }
                
            });
            
        }
        
    }//GEN-LAST:event_loadXMLMenuItemActionPerformed
    
    /**
     * Show an open file browse dialog to allow the user to select a file with the appropriate extension.
     * 
     * @return File - the file selected.  Can be null if no file was selected.
     */
    protected File browseForFileToOpen(){
        
        if (fileDlg.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            //get the xml to open
            File file = fileDlg.getSelectedFile();
            return file;
            }
            
        return null;
        }

    /**
     * Show an save file browse dialog to allow the user to select a file with the appropriate extension.
     * 
     * @return File - the file selected.  Can be null if no file was selected.
     */
    protected File browseForFileToSave(){
        
        if (fileDlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            //get the xml to save
            File file = fileDlg.getSelectedFile();
            
            if(file != null && file.exists() && !file.equals(contentFile)){
                int option = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to over-write the selected file of \n'"+file+"'?",
                        "Over write existing file?", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                
                if (option == JOptionPane.CANCEL_OPTION){
                    dblView.showInfoMessage("Save aborted.");
                    return null;
        
                }else if (option == JOptionPane.NO_OPTION){  
                    //allow user to browse again...
                    return browseForFileToSave();
                }
            }
            
            return file;
        }
        
        return null;
    }

    /**
     * The documentation menu item was selected.  Open the schema documentation document.
     * 
     * @param evt
     */
    private void schemaDocumentationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemaDocumentationMenuItemActionPerformed
        
        Desktop desktop = Desktop.getDesktop(); 
        try {
            desktop.browse(schemaDocumentation);  // opens application associated with file
        }catch(Exception ex) {
            logger.error("Caught exception while trying to open the documentation at "+schemaDocumentation, ex);
            JOptionPane.showMessageDialog(this,"Unable to open documentation\nPlease refer to the log for more details","Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_schemaDocumentationMenuItemActionPerformed

    /**
     * The new menu item was selected.  Create a new XML document instance of the loaded schema.
     * 
     * @param evt
     */
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed        
        newXML();
    }//GEN-LAST:event_newMenuItemActionPerformed

    /**
     * The window is closing because the "X" was selected on the window
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
            
        if(!applicationClosing()){
            //abort closing because file save failed                
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            return;
        }

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }//GEN-LAST:event_formWindowClosing

    /**
     * The Tool help menu item was selected.  Open the tool help documentation document.
     * 
     * @param evt
     */
    private void toolDocumentationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolDocumentationMenuItemActionPerformed
        
        Desktop desktop = Desktop.getDesktop(); 
        try {
            desktop.browse(toolDocumentation);  // opens application associated with file
        }catch(Exception ex) {
            logger.error("Caught exception while trying to open the documentation at "+toolDocumentation, ex);
            JOptionPane.showMessageDialog(this,"Unable to open documentation\nPlease refer to the log for more details","Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_toolDocumentationMenuItemActionPerformed

    /**
     * The horizontal split menu item was selected.  Change the view orientation to horizontal split.
     * 
     * @param evt
     */
    private void horizontalSplitCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalSplitCheckBoxMenuItemActionPerformed
        dblView.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    }//GEN-LAST:event_horizontalSplitCheckBoxMenuItemActionPerformed

    /**
     * The synchronization menu item was selected.  Change the synch mode from its current value.
     * 
     * @param evt
     */
    private void syncCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncCheckBoxMenuItemActionPerformed
        dblView.setSyncSelectNodes(this.syncCheckBoxMenuItem.getModel().isSelected());
    }//GEN-LAST:event_syncCheckBoxMenuItemActionPerformed

    /**
     * The vertical split menu item was selected.  Change the view orientation to vertical split.
     * 
     * @param evt
     */
    private void verticalSplitCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalSplitCheckBoxMenuItemActionPerformed
        dblView.setOrientation(JSplitPane.VERTICAL_SPLIT);
    }//GEN-LAST:event_verticalSplitCheckBoxMenuItemActionPerformed

    /**
     * The reload schema menu item was selected.  Reload the schema file for this tool.
     * 
     * @param evt
     */
    private void reloadSchemaMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadSchemaMenuItemActionPerformed
        
        try{
            
            if(model.isDocumentChanged()){
                int option = JOptionPane.showConfirmDialog(this,
                        "The current document will be closed to reload the schema.\nWould you like to save it first?",
                        "Save", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                
                if (option == JOptionPane.YES_OPTION){

                    if(!performAppropriateSave()){
                        //abort closing because file saving operation failed   
                        return;
                    }

                }
            }else{
                
                int option = JOptionPane.showConfirmDialog(this,
                        "The current document will be closed to reload the schema.\nDo you wish to continue?",
                        "Continue?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                
                if (option == JOptionPane.NO_OPTION){
                    //abort operation
                    return;
                }
            }
            
            loadSchema(schemaFile);
            setSchemaFile(schemaFile);
            
            //create empty xml by default
            newXML();
            
            dblView.showInfoMessage("Successfully reloaded schema file");
            dblView.showErrorMessage("");
        }catch(Throwable e){
            dblView.showErrorMessage("There was an exception thrown while trying to reload the schema file: "+schemaFile.getAbsolutePath()+", "+e.getMessage());
            logger.error("Caught exception while trying to reload schema file", e);
        }
        
    }//GEN-LAST:event_reloadSchemaMenuItemActionPerformed

    /**
     * The validate all menu item was selected.  Search for all XML files with the appropriate file extension for this tool.
     * Then execute schema and GIFT validation on each file and show the list of failed files (if any) to the user.
     *  
     * @param evt
     * @throws IOException 
     */
    private void validateAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) throws IOException {//GEN-FIRST:event_validateAllMenuItemActionPerformed
        
        dblView.showInfoMessage("Validating...");
        
        //gather files of appropriate type
        List<FileProxy> files = new ArrayList<>();
        DesktopFolderProxy browseLocation = new DesktopFolderProxy(new File(defaultBrowsePath));
        FileFinderUtil.getFilesByExtension(browseLocation, files, contentFileSuffix);
        
        dblView.showErrorMessage("");
        
        if(files.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Unable to find any files with the extension of '"+contentFileSuffix+"' starting from the directory of "+browseLocation.getName()+".",
                    "No Files to Validate",
                    JOptionPane.INFORMATION_MESSAGE);
            
            dblView.showInfoMessage("");
        }else{
        
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to validate "+files.size()+" files found with extension of '"+contentFileSuffix+"'?",
                    "Confirmation", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.CANCEL_OPTION){
                dblView.showInfoMessage("Validation aborted.");
    
            }else if (option == JOptionPane.OK_OPTION){
                
                //need 'final' version of list
                final List<FileProxy> filesToValidate = files;
                
                //used for dialog created in this method
                final Component parentComponent = this;
                
                final JDialog pleaseWaitDialog = new JDialog();
                final JLabel subtaskLabel = new JLabel("*");
                
                //This task will handle the validation call
                //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
                final SwingWorker<Void, Void> validationWorker = new SwingWorker<Void, Void>() {
                    
                    @Override
                    protected Void doInBackground() throws Exception {  
                                 
                        StringBuffer sb = new StringBuffer();
                        int cnt = 0;
                        for(FileProxy file : filesToValidate){
                            
                            try{
                                String pathToDisplay = file.getFileId();
                                int width = subtaskLabel.getFontMetrics(subtaskLabel.getFont()).stringWidth(pathToDisplay);
                                if(width > 600){
                                    //if the file path is too long, replace the inner third with ellipses
                                    int strLength = pathToDisplay.length();
                                    pathToDisplay = pathToDisplay.substring(0, strLength/3) + "..." + pathToDisplay.substring(2 * (strLength/3), strLength);
                                }
                                
                                subtaskLabel.setText(pathToDisplay);
                                pleaseWaitDialog.repaint();
                                
                                giftValidate(file);
                                
                            }catch(Throwable t){
                                logger.error("Caught error while trying to GIFT-validate the XML contents authored in "+file.getFileId(), t);
                                sb.append("\n").append(file.getFileId());
                                cnt++;
                            }
                        }//end for                        
                        
                        pleaseWaitDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        pleaseWaitDialog.dispose();
                        
                        dblView.showInfoMessage("Validation Complete.");
        
                        if(cnt > 0){
                            JOptionPane.showMessageDialog(parentComponent,
                                    "Failed to validate the following "+cnt+" file(s):\n"+sb.toString()+"\n\nPlease refer to the appropriate tool's log file for errors reported per file.",
                                    "Validation Error(s)",
                                    JOptionPane.ERROR_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(parentComponent,
                                    "All files validated successfully.",
                                    "Validation Successfull",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                        
                        return null;
                    }
                };
                
                //Handle presenting 'please wait' dialog then validating 
                //Note: Release the calling event dispatch thread to allow the following dialogs to be shown correctly  
                SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {                            
                                
                        // show custom 'please wait' dialog
                        JPanel pan = new JPanel();
                        pan.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        GroupLayout layout = new GroupLayout(pan);
                        pan.setLayout(layout);
                        pleaseWaitDialog.setMinimumSize(new Dimension(700, 100));
                        JLabel label = new JLabel("<html>Please wait for the validate logic to finish...<br></html>");
                        pleaseWaitDialog.setLocationRelativeTo(parentComponent);
                        pleaseWaitDialog.setTitle("Please Wait...");
                        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(label).addComponent(subtaskLabel));
                        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(label).addComponent(subtaskLabel));
                        pleaseWaitDialog.add(pan);
                        pleaseWaitDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        pleaseWaitDialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                        pleaseWaitDialog.pack();
                        pleaseWaitDialog.setVisible(true);
                                
                        //start the worker task (which will also dispose of the 'please wait' dialog)
                        validationWorker.execute(); 
                    }
                });
                
            }//end ok-option
        }//end else
        
    }//GEN-LAST:event_validateAllMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        
        JLabel label = new JLabel();
        JEditorPane ep = new JEditorPane("text/html", "<html>This tool is used to author '"+contentFileSuffix+"' files for GIFT version "+Version.getInstance().getName()+"." +
        		"<br><br> Visit the <a href=\"https://gifttutoring.org/\">GIFT Portal</a> for News, Help and other information.</html>");

        // handle link events
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    try {
                        Desktop.getDesktop().browse(new URI("https://gifttutoring.org"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());

        // show
        ImageIcon giftIcon = new ImageIcon(ImageUtil.getInstance().getSystemIcon());
        JOptionPane.showMessageDialog(this, ep, "About "+titlePrefix, JOptionPane.INFORMATION_MESSAGE, giftIcon);
        
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * The conversion wizard menu item was selected.  Handle converting an XML file from a previous version of GIFT to the current
     * version of GIFT.
     * 
     * @param evt
     */
    private void conversionWizardMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conversionWizardMenuItemActionPerformed
        
        try {
        	String message = "<html><body>"+
        						"<p style=\"width:420px; text-align:center;\">"+
        							"Welcome to the " + Version.getInstance().getName() + " Conversion Wizard.<br>To begin you will need to select the file that needs to be migrated to this version of GIFT."+
        						"</p>"+
        						"<br><hr style=\"width:100%;\" /><br>"+
        						"<p style=\"width:420px;\">Note that only the file selected for conversion will be converted appropriately. Any referenced GIFT XML files (e.g. if a course references a DKF) will not be automatically converted."+ 
        							"<br>Look for this feature in an upcoming release of GIFT."+
        						"</p>"+
        					 "</body></html>";        	
            JOptionPane.showMessageDialog(this, message, "Conversion Wizard", JOptionPane.INFORMATION_MESSAGE);
            
            File file = browseForFileToOpen();
            if(file != null){
                
                //convert the previous schema version file into the current schema generated class instance
            	Object convertedObject = null;
            	try {
            		convertedObject = executeConversion(new FileProxy(file));
            		
                	if(convertedObject != null){
                        
                        Document document = AbstractSchemaHandler.getDocument(convertedObject, rootElementClass, schemaFile, true);
                        if(document == null){
                            throw new Exception("The conversion process returned a null document.");
                        }
                        
                        //update version
                        incrementVersion(document, false);
                        
                        Object[] options = {"Load into this tool", "Save to a file"};
                        int option = JOptionPane.showOptionDialog(this,
                                "What would you like to do with the converted XML contents now?\n",
                                "Successfully Converted File", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                        
                        switch(option){
                        
                        case 0:
                            //load object
                            
                            //if XML is loaded already, ask are they sure they want to start the conversion wizard which will load another XML file
                            if (!XMLCloseConfirmation()){
                                return;
                            }

                            // newXML() must be called because it's possible that a conversion has already taken place. If it has, the user won't be prompted
                            // to save this current conversion as a new file. Instead, the previously converted file will be overwritten because 
                            // model.isDocumentChanged() is currently false when it should be true. 
                            newXML();
                            
                            loadXML(document, "Converted File");
                            
                            //since the model was empty before
                            model.setDocumentChanged(true);
                            
                            break;
                            
                        case 1:
                            //save object
                            
                            File newFile = browseForFileToSave();
                            if(newFile != null){
                                //1) assume the contents are valid 
                                //2) since the contents weren't loaded into the tool, don't update the tool as if it were
                                saveXML(newFile, document, true, false);

                            }else{
                                dblView.showInfoMessage("Conversion aborted because no file name was provided to save the converted contents too."); 
                            }
                            
                            break;
                            
                        default:
                            dblView.showInfoMessage("Conversion aborted.");
                        }

                    }else{
                        //dblView.showErrorMessage("The conversion process returned a null object.  Check the tool's log for more information.");
                    	dblView.showInfoMessage("Conversion aborted.");
                    }       
                	
            	}catch (LatestVersionException e) {
            		
            		// User attempted to convert a file that is already at the latest version of GIFT.
            		Object[] options = {"Load into this tool", "Cancel"};
                    int option = JOptionPane.showOptionDialog(this,
                            e.getMessage(),
                            "Conversion unnecessary", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                    
                    switch(option){
                    
                    case 0:
                        //load file
                        
                        //if XML is loaded already, ask are they sure they want to start the conversion wizard which will load another XML file
                        if (!XMLCloseConfirmation()){
                            return;
                        }
                        
                        loadXML(file);
                        
                        //since the model was empty before
                        model.setDocumentChanged(true);
                        
                        break;           
                        
                    default:
                        dblView.showInfoMessage("Cancelled.");
                    }
                    
            	}catch (UnsupportedVersionException e) {
            		
            		// User attempted to convert a file whose version is not supported by the conversion wizard.
                	JOptionPane.showMessageDialog(null, e.getMessage(),
                				"Version unsupported",
                				JOptionPane.OK_OPTION);
            	}
                
            }else{
                dblView.showInfoMessage("Conversion aborted.");
            }
            
        }catch (Exception e) {
        
            e.printStackTrace();
            dblView.showErrorMessage("There was an exception thrown while trying to execute the conversion wizard: "+e.getMessage());
            logger.error("Caught exception while trying to execute the conversion wizard", e);
        }        
        
    }//GEN-LAST:event_conversionWizardMenuItemActionPerformed
    
    /**
     * Based upon the state of the authoring tool, perform a 'save' or 'save-as' operation.
     * 
     * @return boolean if either save was completed successfully.
     */ 
    private boolean performAppropriateSave(){
        
        if(saveXMLMenuItem.isEnabled()){
            //use save option if the file was previously saved
            return saveXML(contentFile);
        }else{
            //file was not previously saved, need to use save as
            return saveAsXML();
        }
    }

    /**
     * The XML Content menu item was selected.  Present the current content shown in the authoring tool in an XML viewer.
     * 
     * @param evt
     */
    private void xmlContentMenuitemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xmlContentMenuitemActionPerformed
        
        //clear message panels
        dblView.showErrorMessage("");
        dblView.showInfoMessage("");
        
        //the document has changed or has not been saved, therefore it needs to be saved first
        //If document is loaded with no changes, no save is needed
        boolean needSave = model.isDocumentChanged() || !saveXMLMenuItem.isEnabled();
        
        boolean showXML = true;

        if(needSave){
            
            int option = JOptionPane.showConfirmDialog(this,
                    "In order to display the latest content in XML you will need to save first.",
                    "Save", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION){
                
                if(!performAppropriateSave()){
                    showXML = false;                  
                    dblView.showErrorMessage("The file was not saved, therefore not showing the XML content.");
                    JOptionPane.showMessageDialog(this, "The file was not saved, therefore not showing the XML content.", "Unable to show XML Content", JOptionPane.ERROR_MESSAGE);
                }                
            }else{
                showXML = false;
            }
            
        }
        
        if(showXML){
            
            //
            //open file in IE
            //
            
            // NOTE: This DEFINITELY only works on Microsoft Windows
            try{
                String[] args = {"C:/Program Files/Internet Explorer/iexplore.exe ", contentFile.toURI().toString()};
                Runtime.getRuntime().exec(args);
            }catch(Exception e){
                logger.error("Caught exception while trying to open "+contentFile+" in IE.", e);
                dblView.showErrorMessage("There was an exception thrown while trying to open the XML file of "+contentFile+".  Refer to the tool log for more details.");
            }
        }

            
//        }else{
//            dblView.showErrorMessage("There is no XML content to show because nothing has been saved.");
//            JOptionPane.showMessageDialog(this, "There is no XML content to show because nothing has been saved.", "Unable to show XML Content", JOptionPane.ERROR_MESSAGE);
//        }

    
    }//GEN-LAST:event_xmlContentMenuitemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem conversionWizardMenuItem;
    private javax.swing.JMenu fileMenuItem;
    private javax.swing.JMenu helpMenuItem;
    private javax.swing.JCheckBoxMenuItem horizontalSplitCheckBoxMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JMenuItem loadXMLMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem reloadSchemaMenuItem;
    protected javax.swing.JMenuItem saveAsXMLMenuItem;
    protected javax.swing.JMenuItem saveXMLMenuItem;
    private javax.swing.JMenuItem schemaDocumentationMenuItem;
    private javax.swing.ButtonGroup splitPaneButtonGroup;
    private javax.swing.JCheckBoxMenuItem syncCheckBoxMenuItem;
    private javax.swing.JMenuItem toolDocumentationMenuItem;
    private javax.swing.JMenuItem validateAllMenuItem;
    private javax.swing.JMenuItem validateMenuItem;
    private javax.swing.JCheckBoxMenuItem verticalSplitCheckBoxMenuItem;
    private javax.swing.JMenu viewMenuItem;
    private javax.swing.JMenuItem xmlContentMenuitem;
    private javax.swing.JPanel xmleditorPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void newDocumentLoaded(FXStatusEvent fxse) {
    }

    @Override
    public void docValidityStatusChanged(FXStatusEvent fxse) {
    }
    
    /**
     * Return whether or not a xml can be closed to allow another to be opened.
     * 
     * @return boolean - whether or not the current xml should be closed
     */
    private boolean XMLCloseConfirmation()
    {

        if(model.isDocumentChanged()){
            
            int option = JOptionPane.showConfirmDialog(this,
                    "The current XML Document has been changed. Would you like to save it?",
                    "Save XML Content", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.CANCEL_OPTION){
                return false;
            }else if (option == JOptionPane.YES_OPTION){
                return saveAsXML();
            }else{
                return true;
            }
            
        }else{
          return true;
        }
    }

    @Override
    public void nodeSelected(FTreeNodeEvent event) {
        
        selectedNode = (FToggleNode) event.getTreeNode();
        XMLAuthoringToolFormManager.getInstance().setSelectedNode(selectedNode);
        
        //reset buffer for next selected node
//        textFieldBuffer = "";
        textFieldBuffer = null;
        
//        System.out.println("selected "+selectedNode);
    }

    @Override
    public void nodeUnselected(FTreeNodeEvent event) {
        
        Object treeNodeObj = event.getTreeNode();
        if(treeNodeObj == selectedNode){
//            System.out.println("un-selected "+selectedNode);
            
            //if the user was typing in a text field and then selected an expansion bar on the XML authoring tool w/o
            //hitting the enter keyboard button, the default behavior is to clear out the textfield value.
            //This logic will set the textfield value to the current buffer
            if(treeNodeObj instanceof FTextFieldNode && textFieldBuffer != null){
//                System.out.println("set "+selectedNode+" to "+textFieldBuffer);
                ((FTextFieldNode)treeNodeObj).setValue(textFieldBuffer);
                model.setNodeValue(((FTextFieldNode)treeNodeObj), ((FTextFieldNode)treeNodeObj).getValue());
            }
            
            selectedNode = null;
            XMLAuthoringToolFormManager.getInstance().setSelectedNode(selectedNode);
        }
    }

    @Override
    public void nodeCollapsed(FTreeNodeEvent event) {
      selectedNode = (FToggleNode) event.getTreeNode();
      XMLAuthoringToolFormManager.getInstance().setSelectedNode(selectedNode);
//      System.out.println("selected-nodeCollapsed "+selectedNode);        
    }

    @Override
    public void nodeExpanded(FTreeNodeEvent event) {
      selectedNode = (FToggleNode) event.getTreeNode();
      XMLAuthoringToolFormManager.getInstance().setSelectedNode(selectedNode);
//      System.out.println("selected-nodeExpanded "+selectedNode);        
    }

    @Override
    public void nodeWillCollapse(FTreeNodeEvent arg0) throws FCancelException {        
    }

    @Override
    public void nodeWillExpand(FTreeNodeEvent arg0) throws FCancelException {        
    }
    
    @Override
    public void cellEditorValueChanged(FTreeEditorEvent arg0) {
//        System.out.print("value changed");
        
        //check if the item being edited is a textfield
        if(arg0.getEditor() instanceof FTextField){
            
            //save the current value to use in case the value is lost due to selecting other components in the tool
            //without "saving" the value typed by pushing the enter keyboard button.
            Object data = ((FTextField)arg0.getEditor()).getData();
//            System.out.println(" to "+data);
            textFieldBuffer = (String) data;
        }

    }
    
    @Override
    public void cellEditingWillStop(FTreeEditorEvent arg0) {
    }
    
    @Override
    public void cellEditingStopped(FTreeEditorEvent arg0) {
//        System.out.println("Cell editing stopped");
    }
    
    @Override
    public void cellEditingStarted(FTreeEditorEvent arg0) {
//        System.out.println("Cell editing started");
    }
    
    /**
     * Returns the list of text areas used for displaying messages.
     * 
     * @return ArrayList - the list of text areas used for displaying messages.
     */
    private ArrayList<JTextArea> getMessageAreaList(){
    	
        ArrayList<JTextArea> messageAreaList = new ArrayList<JTextArea>();
        
    	try{
    		
    		FXView[] views = {dblView.getLeftView(), dblView.getRightView()};
    		
    		for(FXView view : views){
    		
		    	JSplitPane parentPane = (JSplitPane)((JPanel)((JSplitPane) view.getComponent(0)).getComponent(2)).getComponent(0);
		   
		    	for(Component c : ((Container) parentPane).getComponents()){    
		 			if(c instanceof JScrollPane){
		        		JTextArea messageArea = (JTextArea) ((JViewport) ((JScrollPane) c).getComponent(0)).getComponent(0);
		        		messageAreaList.add(messageArea);
		 			}
		    	}
    		}
	    	
	    	return messageAreaList;
	    	
    	} catch (Exception e){
    		logger.error("Caught exception while getting the list of text areas.", e);
    		return messageAreaList;
    	}
    }
}
