/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat.custnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This will present a dialog that allows the user to select a performance node from the Merrill's
 * branch point course transition's practice quadrant's referenced DKF.  The DKF is used to assess
 * the lesson given in the practice quadrant.  This dialog allows the user to associate DKF elements
 * with the practice quadrant.
 * 
 * @author mhoffman
 *
 */
public class MerrillsDKFPerformanceNodeSelectionDialog extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MerrillsDKFPerformanceNodeSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Merrill's Branch Point DKF Performance Node";
    
    private static final String LABEL = "Please select one of the Performance Assessment nodes found in this Practice Quadrant's DKF.\n" +
            "The selected node will be checked after completion of the practice session to determine what, if any, remediation is warranted." +
            "Note: The DKF reference needs to be authored prior to using this selection dialog.";
    
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String DKF_CONCEPT_NODE = "DKFConcept";
    
    public MerrillsDKFPerformanceNodeSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {
        
        Set<String> items = new HashSet<>();
        
        try{
            
            //
            // gather all concepts
            //              
            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                //Refactor - 
                //1) get list of branch point concepts
                //2) gather metadata files that are:  (? - what directory to search)
                //   i. valid
                //  ii. match those concepts 100%
                // iii. Practice quadrant 
                //3) get DKF and validate
                //4) gather DKF node names
                //5) display DKF name alongside node names in this custom dialog
                
                if(selectedNode.getLabelText().equals(DKF_CONCEPT_NODE)){
                    //found DKF_CONCEPT_NODE element
                    
                    //get directory of where the course being authored is saved
                    File courseFile = XMLAuthoringToolFormManager.getInstance().getCurrentFile();
                    if(courseFile == null){
                        // the course hasn't been saved yet, therefore not sure what directory to search for metadata
                        // present a dialog stating that the user must save the course first.
                        
                        JOptionPane.showMessageDialog(this,
                                "This course has not been saved yet, therefore the DKF Concept finder logic is unable to search for DKFs.",
                                "Error - Course has not been saved yet",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    
                    List<FileProxy> metadaFiles = new ArrayList<>();
                    FileFinderUtil.getFilesByExtension(new DesktopFolderProxy(courseFile.getParentFile()), metadaFiles, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
                    
                    
                    logger.error("Need to finish this logic!!!!");
                    
//                    //find the DKF file reference (DKF_CONCEPT_NODE -> ConceptPair -> array -> PracticeConcepts -> Practice child(0) is 
//                    //                              TrainingApplication child(1) is dkfRef child(0) is file)                    
//                    TreeNode practiceNode = selectedNode.getParent().getParent().getParent().getParent();
//                    String dkfFile;
//                    try{
//                        TreeNode dkfFileNode = practiceNode.getChildAt(0).getChildAt(1).getChildAt(0);
//                        dkfFile = (String) ((FToggleMutableNode)dkfFileNode).getValue();
//                    }catch(Exception e){
//                        JOptionPane.showMessageDialog(this,
//                                "Unable to find the DKF file node for the Training Application portion of this practice element.  Did you author a DKF reference yet in that section?.",
//                                "Error - No DKF reference to read from",
//                                JOptionPane.ERROR_MESSAGE);
//                        return null;
//                    }
//                    
//                    //Parse the DKF and gather the performance node names
//                    DomainDKFHandler dkfHandler = new DomainDKFHandler(CommonProperties.getInstance().getDomainDirectory() + File.separator + dkfFile);
//                    for(AbstractPerformanceAssessmentNode node : dkfHandler.getDomainAssessmentKnowledge().getScenario().getPerformanceNodes().values()){
//                            
//                        if(!items.add(node.getName())){
//                            //ERROR - this should be enforced in DKF validation but just in case show an error here
//                            JOptionPane.showMessageDialog(this,
//                                    "Found a duplicate named performance node of "+node.getName()+" in the DKF "+CommonProperties.getInstance().getDomainDirectory() + File.separator + dkfFile +".  This is not allowed.  Please correct the DKF before attempting to view this dialog's choices.",
//                                    "Error - DKF contains duplicate performance node name",
//                                    JOptionPane.ERROR_MESSAGE);
//                            return null;
//                        }
//                    }
//                    
//                    if(items.isEmpty()){
//                        //show error message dialog
//                        JOptionPane.showMessageDialog(this,
//                                "There are no Merrill's branch transition level concepts to choose from.  You need to populate the Merrill's branch transition concepts before attempting to select one of them.",
//                                "Error - No concepts to choose from",
//                                JOptionPane.ERROR_MESSAGE);
//                        return null;
//                    }
                }
                
            }
            
        }catch(Exception e){
            logger.error("Caught exception while gathering concept names", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather Merrill's branch concept names, check the CAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // not supported
    }

}
