/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FAbstractToggleNode;
import com.fg.ftreenodes.FTextLabelNode;
import com.fg.ftreenodes.FToggleNode;
import com.fg.ftreenodes.FToggleSwitchNode;
import com.fg.ftreenodes.ListParams;

import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.InlineDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterfaceCondition;
import mil.arl.gift.domain.knowledge.strategy.DefaultStrategyHandler;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.StrategyHandlerDialog;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.UserHistory;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the Condition implementation class element in the DKF schema file.
 * The dialog allows the user to specify which condition class to use in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class ConditionDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConditionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Condition Implementation";
    
    private static final String LABEL = "Please select an entry.\n" +
    		"To specify your own implementation of a Domain Module condition class,\n" +
    		" please provide the class path from the mil.arl.gift package, followed by the class name.\n" +
    		"(e.g. domain.knowledge.condition.AvoidLocationCondition)\n" +
    		"Providing a custom class parameter allows a GIFT developer to create their own implementation class while still using authored 'input' configuration parameters.\n"+
    		"Note: the user history entries are stored in GIFT/config/tools/<toolname>/UserHistory.txt";
    
    /** generated strategy and transition dkf element values */
    private static final String strategyHandlerImpl = StrategyHandlerDialog.formatClassName(DefaultStrategyHandler.class);
    private static final String transitionNodeName = "AutoTutor Session Node";
    private static final String STRATEGY_NAME_PREFIX = "AutoTutor strategy - (generated) ";
	
    /** last known list of user specified condition implementation class names */
    private List<String> conditionHistory;
    
    private static List<Class<?>> inputClasses;
    
    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();
    static{
        
        try{
            String packageName = "mil.arl.gift.domain.knowledge.condition";
            List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractCondition.class);
            for(Class<?> clazz : classes){
                
                String description = null;
                try{
                    AbstractCondition condition = (AbstractCondition) clazz.getDeclaredConstructor().newInstance();
                    ConditionDescription conditionDescription = condition.getDescription();
                    if(conditionDescription == null){
                        description = null;
                    }else if(conditionDescription instanceof InlineDescription){
                        description = ((InlineDescription)conditionDescription).getDescription();
                    }else{
                        FileDescription fileDescription = (FileDescription)conditionDescription;
                        
                        //read file into string
                        byte[] encoded = Files.readAllBytes(Paths.get(fileDescription.getFile().toURI()));
                        description = new String(encoded, StandardCharsets.UTF_8);
                    }
                  
                }catch(InstantiationException instExc){
                    
                    logger.error("Caught exception while trying to instantiate condition class of "+clazz.getName()+".", instExc);
                    
                    JOptionPane.showConfirmDialog(null, 
                            "<html>The condition class of "+clazz.getName()+" caused an exception,<br>" +
                            		"therefore it will not automatically be available in the condition selection dialog.<br>" +
                            		"Check the DAT log for more details.<br><br>" +
                            		"Note: Does the Condition provide a no-argument constructor? (which is required for all Conditions classes)</html>", 
                            "Problem with Condition class", 
                            JOptionPane.OK_OPTION, 
                            JOptionPane.ERROR_MESSAGE);
                    
                }catch(Exception e){
                    
                    logger.error("Caught exception while trying to instantiate condition class of "+clazz.getName()+".", e);
                    
                    JOptionPane.showConfirmDialog(null, 
                            "<html>The condition class of "+clazz.getName()+" caused an exception,<br>" +
                            		"therefore it will not automatically be available in the condition selection dialog.<br>" +
                            		"Check the DAT log for more details.</html>", 
                            "Problem with Condition class", 
                            JOptionPane.OK_OPTION, 
                            JOptionPane.ERROR_MESSAGE);
                }
                
                //remove package prefix of "mil.arl.gift." 
                SelectionItem item = new SelectionItem(clazz.getName().replaceFirst("mil.arl.gift.", ""), description);
                DEFAULT_ENTRIES.add(item);
            }
        
        }catch(Exception e){
            System.out.println("Unable to populate the default list of conditions because of the following exception.");
            e.printStackTrace();
            logger.error("Caught exception while trying to populate the default list of conditions.", e);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
        inputClasses = DomainKnowledgeUtil.getConditionInputClasses();
    }
    
    /**
     * Class constructor - create dialog
     */
    public ConditionDialog(){
        super(TITLE, LABEL, DEFAULT_ENTRIES);

        //wait for this dialog to close and then prompt for additional logic based on the selection made
        this.addWindowListener(new CustomWindowClosedHandler());

    }

    @Override
    public String[] getCustomValues() {
        
        String[] userHistory = null;
        
        if(conditionHistory == null){
            conditionHistory = new ArrayList<String>();
        }
        
        try{
            
            //get user history for the attribute
            userHistory = UserHistory.getInstance().getPropertyArray(UserHistory.CONDITION);
            
            if(userHistory != null){
                
                conditionHistory.clear();                
                conditionHistory.addAll(Arrays.asList(userHistory));
            }
            
        }catch(Exception e){
            System.out.println("There was an issue trying to read the user history, therefore the Condition dialog will not be correctly populated");
            e.printStackTrace();
        }
        
        return conditionHistory.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        
        try {
            //add new user provided entry to the collection of user's historic entries
            conditionHistory.add(value);            
            
            //write the new property value
            UserHistory.getInstance().setProperty(UserHistory.CONDITION, conditionHistory.toArray(new String[0]));
            
        } catch (Exception e) {
            System.out.println("Caught exception while trying to save the custom entry");
            e.printStackTrace();
        }
        
    }
    
    /**
     * This class will handle waiting for this dialog to close and then prompt for additional logic based on the selection made
     * 
     * @author mhoffman
     *
     */
    private class CustomWindowClosedHandler extends WindowAdapter{

    	@Override
    	public void windowClosed(WindowEvent e) {

    		Object selectedValue = getData();

    		FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
    		StringBuffer errorBuffer = new StringBuffer();

    		if(selectedValue != null && ((String)selectedValue).length() > 0) {

    			FAbstractToggleNode condition = (FAbstractToggleNode) selectedNode.getParent();
    			String conditionImpl = (String)selectedValue;

    			try{
    			    populateInput(condition, errorBuffer, conditionImpl);
    			}catch(DetailedException de){
    			    logger.error("Failed to select the condition input class automatically.", de);
    			}

    			/* Generates AutoTutor Action Elements */
    			if(AutoTutorWebServiceInterfaceCondition.class.getName().endsWith(((String)selectedValue))){
    				//the user chose the AutoTutor condition

    				int option = JOptionPane.showConfirmDialog(null,
    						"You selected an AutoTutor condition which requires you to provide the appropriate state \n" +
    								"transitions and an instructional strategy in order to allow the GIFT tutor loop to enable \n" +
    								"back and forth dialog interaction.\n\n" +
    								"Would you like to have those additional elements automatically generated for you?",
    								"Create AutoTutor Action elements", JOptionPane.YES_NO_OPTION,
    								JOptionPane.INFORMATION_MESSAGE);

    				if (option == JOptionPane.YES_OPTION){

    					//
    					// Auto Generate the necessary DKF actions elements for an AutoTutor condition
    					//

    					//TODO: re-factor set values to do all or nothing in case of an error

    					//unique strategy name to use
    					String strategyName =  STRATEGY_NAME_PREFIX + (new Date()).getTime();

    					try{

    						XMLAuthoringToolForm form = DAT.getInstance().getDATForm();

    						selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
    						FToggleNode conceptNode = (FToggleNode) selectedNode.getParent().getParent().getParent().getParent().getParent();
    						FAbstractToggleNode conceptNodeId = findChildNodeByName(conceptNode, "nodeId");
    						Object nodeIdObj = conceptNodeId.getValue();
    						if(nodeIdObj == null || ((String)nodeIdObj).length() == 0){
    							//clear condition choice, prompt user and return


    							logger.info("Removing condition dialog selected value of "+selectedNode.getValue()
    									+ " because the parent concept's node id has not been set yet.  The node "
    									+ "id must be set before auto generating action elements for an AutoTutor condition.");

    							selectedNode.setValue("");
    							form.setNodeValue(selectedNode, "");

    							JOptionPane.showMessageDialog(null, "The node id of the parent concept to this condition has not been set.  \n" +
    									"The Id is needed before auto generating AutoTutor condition action elements.\n\n" +
    									"Please populate the concept id and select the AutoTutor condition implementation again.", 
    									"Concept Node Id Needed", 
    									JOptionPane.INFORMATION_MESSAGE);

    							return;
    						}

    						//to hold any errors
    						errorBuffer = new StringBuffer();


    						FToggleNode root =  (FToggleNode)form.getTreeRootNode();
    						FAbstractToggleNode actions = findChildNodeByName(root, "actions");

    						//
    						// Create an instructional strategy
    						//
    						createInstructionalStrategy(nodeIdObj, strategyName, actions, errorBuffer);


    						//
    						// Create state transition
    						//
    						FAbstractToggleNode newTransition = createStateTransition(nodeIdObj, actions, errorBuffer);


    						//
    						// Link new strategy to to transition
    						//
    						FAbstractToggleNode strategyChoices = findChildNodeByName(newTransition, "strategyChoices");
    						form.populateNode((FToggleNode) strategyChoices);
    						FAbstractToggleNode listNode = (FAbstractToggleNode) strategyChoices.getChildAt(0);

    						FAbstractToggleNode strategyRef = (FAbstractToggleNode) listNode.getChildAt(0);
    						FAbstractToggleNode strategyNameRef = findChildNodeByName(strategyRef, "name");
    						strategyNameRef.setValue(strategyName);
    						form.setNodeValue((FToggleNode) strategyNameRef, strategyName);

    						//check for accumulating error(s)
    						if(errorBuffer.length() > 0){
    							throw new Exception(errorBuffer.toString());
    						}                        

    					}catch(Throwable exception){
    						logger.error("Caught exception while trying to generate some AutoTutor action elements.", exception);

    						//prompt user with error
    						JOptionPane.showMessageDialog(null, "There was a problem generating the actions elements for the AutoTutor condition.\n" +
    								"Please refer to the DAT log for more details.", 
    								"Problem creating AutoTutor Actions elements", 
    								JOptionPane.ERROR_MESSAGE);

    						return;
    					}

    					//show success prompt
    					JOptionPane.showMessageDialog(null, "Successfully created the Actions elements for the AutoTutor condition.", 
    							"Action elements created successfully", 
    							JOptionPane.INFORMATION_MESSAGE);

    				}//end if "yes" option
    			} 
    		}
    	}
        
        /**
         * Generate a new state transition element in the DKF.
         * 
         * @param nodeIdObj - the condition's parent concept node id containing the concept id to use as a reference in the state transition
         * @param actions - the actions node of the dkf, used to know where to add the new state transition to the xml tree
         * @param errorBuffer - used to add an error messages
         * @return FAbstractToggleNode - the new state transition element generated.
         * @throws DetailedException if there was a problem getting the DAT form
         */
        private FAbstractToggleNode createStateTransition(Object nodeIdObj, FAbstractToggleNode actions, StringBuffer errorBuffer) throws DetailedException{
            
            XMLAuthoringToolForm form = DAT.getInstance().getDATForm();
            
            //find the root state transitions node
            FAbstractToggleNode stateTransitions = findChildNodeByName(actions, "stateTransitions");
            if(stateTransitions == null){
                form.populateNode((FToggleNode) actions);
                stateTransitions = findChildNodeByName(actions, "stateTransitions");
            }
            
            if(stateTransitions.getChildCount() == 0){
                form.populateNode((FToggleNode) stateTransitions);
            }
            
            //find the state transitions list node where children can be added
            FAbstractToggleNode listNode = (FAbstractToggleNode) stateTransitions.getChildAt(0);
            listNode.setToggleSelected(true);
            FAbstractToggleNode newTransition = form.insertInstance((FToggleNode) listNode, listNode.getChildCount());
            form.populateNode((FToggleNode) newTransition);
            
            //find the logical expression node
            FAbstractToggleNode expression = findChildNodeByName(newTransition, "LogicalExpression");
            form.populateNode((FToggleNode) expression);
            listNode = (FAbstractToggleNode) expression.getChildAt(0);                   
            
            //select Performance Node as the learner state type choice  
            FToggleSwitchNode choice = (FToggleSwitchNode) findChildNodeByName(listNode, "Choice:");             
            int elementIndex = XMLAuthoringToolSelectionDialog.getChoiceIndex(choice, PerformanceNode.class.getSimpleName());
            
            if(elementIndex >= 0){
                
                //"PerformanceNode" selection done here
                form.switchBranch(choice, elementIndex);
                
                //create default child elements of PerformanceNode node
                FTextLabelNode newPerfNode = (FTextLabelNode) choice.getSubstituteNode();                                    
                form.populateNode(newPerfNode);
                
                FAbstractToggleNode nodeName = findChildNodeByName(newPerfNode, "name");
                nodeName.setValue(transitionNodeName);
                form.setNodeValue((FToggleNode) nodeName, nodeName.getValue());
                
                //set condition's parent concept node id
                FAbstractToggleNode nodeId = findChildNodeByName(newPerfNode, "nodeId");
                nodeId.setValue(nodeIdObj);
                form.setNodeValue((FToggleNode) nodeId, nodeId.getValue());
                
                FAbstractToggleNode previous = findChildNodeByName(newPerfNode, "previous");
                previous.setToggleSelected(true);
                ListParams choices = (ListParams) previous.getParameters();
                Object value = null;
                for(Object assessmentObj : choices){
                    
                    if(((String)assessmentObj).equals(AssessmentLevelEnum.UNKNOWN.getName())){
                        value = assessmentObj;
                        break;
                    }
                }
                
                if(value == null){
                    //ERROR
                    errorBuffer.append("Unable to find ").append(AssessmentLevelEnum.UNKNOWN.getName()).append(" as a previous assessment level choice. Therefore part of the performance node learner state logical expression will not be populated.");
                    
                }else{
                    previous.setValue(value);
                    form.setNodeValue((FToggleNode) previous, previous.getValue());
                }

                
            }else{
                //ERROR
                errorBuffer.append("Unable to find ").append(PerformanceNode.class.getSimpleName()).append(" as a learner state transition choice. Therefore part of the state transition will not be populated.");
            }
            
            return newTransition;
        }
        
        /**
         * Generate a new instructional strategy using the parameters provided, then add the new strategy to the current DKF as
         * the first strategy in the list of strategies.
         * 
         * @param nodeIdObj - the condition's parent concept node id containing the concept id to use as a reference in the strategy
         * @param strategyName - the unique name of the strategy being created
         * @param actions - the actions node of the dkf, used to know where to add the new strategy to the xml tree
         * @param errorBuffer - used to add an error messages
         * @throws DetailedException if there was a problem getting the DAT form
         */
        private void createInstructionalStrategy(Object nodeIdObj, String strategyName, FAbstractToggleNode actions, StringBuffer errorBuffer) throws DetailedException{
            
            XMLAuthoringToolForm form = DAT.getInstance().getDATForm();
            
            // find the instructionalStrategies toggle
            FAbstractToggleNode instructionalStrategies = findChildNodeByName(actions, "instructionalStrategies");
            
            // if it's null
            if(instructionalStrategies == null){
            	
            	// populate the upper toggle
                form.populateNode((FToggleNode) actions);
                
                // find instructionalStrategies toggle
                instructionalStrategies = findChildNodeByName(actions, "instructionalStrategies");
            }
            
            // if it has no child nodes, populate them
            if(instructionalStrategies.getChildCount() == 0){
                form.populateNode((FToggleNode) instructionalStrategies);
            }             
            
            FAbstractToggleNode listNode = (FAbstractToggleNode) instructionalStrategies.getChildAt(0);
            listNode.setToggleSelected(true);
            
            // creates a new strategy
            FAbstractToggleNode newStrategy = form.insertInstance((FToggleNode) listNode, listNode.getChildCount());
            
            // populates the new strategy's fields
            form.populateNode((FToggleNode) newStrategy);
            
            // get the strategy name node
            FAbstractToggleNode name = findChildNodeByName(newStrategy, "name");
            
            // set the node's value
            name.setValue(strategyName);
            
            // set the value on the form
            form.setNodeValue((FToggleNode) name, name.getValue());
            
            //select Performance Assessment as the strategy choice
            
            // find the "choice" node on the new strategy form
            FToggleSwitchNode choice = (FToggleSwitchNode) findChildNodeByName(newStrategy, "Choice:");
            
            // get the index of "PerformanceAssessment"
            int elementIndex = XMLAuthoringToolSelectionDialog.getChoiceIndex(choice, PerformanceAssessment.class.getSimpleName());
            
            if(elementIndex >= 0){
                
                //"performanceAssessment" selection done here
            	
            	// change the element selected on the choice node
                form.switchBranch(choice, elementIndex);
                
                // create default child elements of performance assessment strategy node
                FTextLabelNode newPerfAss = (FTextLabelNode) choice.getSubstituteNode();                                    
                form.populateNode(newPerfAss);
                
                //set condition's parent concept node id
                FAbstractToggleNode nodeId = findChildNodeByName(newPerfAss, "nodeId");
                nodeId.setValue(nodeIdObj);
                form.setNodeValue((FToggleNode) nodeId, nodeId.getValue());
                
                //set the strategy handler implementation class
                FAbstractToggleNode handler = findChildNodeByName(newPerfAss, "strategyHandler");
                form.populateNode((FToggleNode) handler);
                FAbstractToggleNode handlerImpl = findChildNodeByName(handler, "impl");
                handlerImpl.setValue(strategyHandlerImpl);
                form.setNodeValue((FToggleNode) handlerImpl, handlerImpl.getValue());
                
            }else{
                //ERROR
                errorBuffer.append("Unable to find ").append(PerformanceAssessment.class.getSimpleName()).append(" as a strategy type choice. Therefore part of the instructional strategy will not be populated.");
            }
        }
    }
    
    /** Automatically selects the condition input based on the condition implementation
     * specified.
     * 
     * @param condition - the condition node of the dkf, used to get the input node
     * @param errorBuffer - used to add error messages
     * @param conditionImpl - the string of the selected condition implementation
     * @throws DetailedException if there was a problem getting the DAT form
     */
    private void populateInput( FAbstractToggleNode condition, StringBuffer errorBuffer, String conditionImpl) throws DetailedException {

    	XMLAuthoringToolForm form = DAT.getInstance().getDATForm();
    	FAbstractToggleNode inputToggle;
    	FToggleSwitchNode choice;
    	Enumeration<?> enumeration;
    	boolean foundMatch = false;
    	int elementIndex = -1;
    	String inputName = "";
    	String choiceName;
    	    	    	    	    	
    	// find the input toggle under the "condition" toggle
    	inputToggle = findChildNodeByName(condition, "input");

    	if(inputToggle.getChildCount() == 0) {
    		// if the input toggle has no child nodes, populate them
    		
    		form.populateNode((FToggleNode) inputToggle);
    	}                    

    	// find the 'Choice' node under the input toggle
    	choice = (FToggleSwitchNode) findChildNodeByName(inputToggle, "Choice:"); 
    	enumeration = choice.children();
    	 	   	
    	for(Class<?> condInputClass : inputClasses) {
    		// Search for the input class that matches the selected condition
    		
    		foundMatch = DomainKnowledgeUtil.isValidConditionInputParam(DomainKnowledgeUtil.PACKAGE_PATH + conditionImpl, DomainKnowledgeUtil.GENERATED_PATH + condInputClass.getSimpleName());
    		    		
    		if(foundMatch) {
    			
    			inputName = condInputClass.getSimpleName();
    			break;
    		} 
    	}
    	    	
    	/* Find the matching input selection for the choice node */
    	while(enumeration.hasMoreElements()) {

    		// index counter for xml elements
    		elementIndex++;

    		// get the next element name
    		choiceName = ((FTextLabelNode)enumeration.nextElement()).getLabelText();

    		if(inputName.equalsIgnoreCase(choiceName))  {
    			
	    		// change the choice node's selection
	    		form.switchBranch(choice, elementIndex);
	
	    		// create default child elements of the input choice made
	    		FTextLabelNode newInput = (FTextLabelNode) choice.getSubstituteNode();                                    
	    		form.populateNode(newInput);
	    		
    			break;
    		}
    	}
    		
    	if(!foundMatch) {
    		// if no matching input class was found, report error
    		
    		errorBuffer.append("Unable to find ").append(DomainKnowledgeUtil.PACKAGE_PATH).append(conditionImpl)
    				.append(" as a condition input choice. Therefore part of the condition will not be populated.");
    		
    		JOptionPane.showMessageDialog(null, "No matching input found for " + conditionImpl +
    				"\nPlease select another condition. \n\nAlternatively, you may add a constructor" +
    				" to\n" + DomainKnowledgeUtil.PACKAGE_PATH + conditionImpl + ".java \nto receive an existing input class", 
    				"Invalid Condition Implementation Constructor", 
    				JOptionPane.OK_OPTION);
    	}
    }
    
}
