/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows the user to select which domain content they want to export.
 * 
 * @author cdettmering
 */
public class DomainSelectionPage extends WizardPage {

	/** Generated serial */
	private static final long serialVersionUID = 8020454107500480151L;
	
	private static Logger logger = LoggerFactory.getLogger(DomainSelectionPage.class);
	
	private static final String TITLE = "Domain Selection";
	private static final String DESCRIPTION = "Select Domain Content";
	
	/** Root directory of the Domain folder */
	private File WORKSPACE_DIR = new File(ExportProperties.getInstance().getWorkspaceDirectory());
	
	/** The file tree that shows valid directories to export */
	private JTree fileTree;
	
	/** Content that will be exported */
	private DomainContent content;
	
	/**
	 * Creates a new DomainSelectionPanel, starting the file tree at domainRoot, 
	 * and using switcher to switch panels.
	 * 
	 * @param domainRoot The root of the Domain folder.
	 */
	public DomainSelectionPage() {
		super(TITLE, DESCRIPTION);
		content = new DomainContent();
	}
	
	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		settings.put(ExportSettings.getDomainContent(), content);
	}
	
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        
        try{
            setupUi();
        }catch(IOException e){
            logger.error("Caught exception while trying to build the domain selection page.", e);
        }
    }
	
	/**
	 * Sets up the user interface.
	 * @throws IOException 
	 */
	private void setupUi() throws IOException {
	    
	    this.removeAll();
		
		// Setup tree structure
		DefaultMutableTreeNode nodeRoot = new DefaultMutableTreeNode(WORKSPACE_DIR.getAbsolutePath());
		
		List<File> courseFolders = ExportFileFilter.getAllCourseFolders();
        List<FileCheckBoxNode> nodes = new ArrayList<>(); //keep track to avoid cases where more than 1 course files is in the same folder
		for(File courseFolder : courseFolders){
		    
            FileCheckBoxNode checkboxNode = new FileCheckBoxNode(courseFolder, WORKSPACE_DIR);
            
            if(!nodes.contains(checkboxNode)){
                nodeRoot.add(checkboxNode);
                nodes.add(checkboxNode);
            }
		}
		
//		List<FileProxy> courseFiles = new ArrayList<>();
//		FileFinderUtil.getFilesByExtension(new DesktopFolderProxy(root), courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
//		
//		if(courseFiles != null) {
//		    
//		    List<FileCheckBoxNode> nodes = new ArrayList<>(); //keep track to avoid cases where more than 1 course files is in the same folder
//		    
//			for(FileProxy courseFile : courseFiles) {
//			    File file = new File(courseFile.getFileId());
//			    FileCheckBoxNode checkboxNode = new FileCheckBoxNode(file.getParentFile(), root);
//			    
//			    if(!nodes.contains(checkboxNode)){
//			        nodeRoot.add(checkboxNode);
//			        nodes.add(checkboxNode);
//			    }
//			}
//		}
		
		fileTree = new JTree(nodeRoot);
		fileTree.addMouseListener(new CheckBoxMouseListener(fileTree, content));
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileTree.setCellRenderer(new CheckBoxDecorator(fileTree.getCellRenderer()));
		fileTree.setBorder(BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ));
		JScrollPane treeView = new JScrollPane(fileTree);
		add(treeView);
	}
}
