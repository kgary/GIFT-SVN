/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.assessments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.TextArea;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.TreeNode;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.cell.ExtendedSelectionCell;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.TextAreaDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.TreeNodeEnum;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * A popup panel that allows the user to edit TreeNode assessments.
 * 
 * @author bzahid
 */
public class AssessmentEditor extends PopupPanel {
	
	private static AssessmentEditorUiBinder uiBinder = GWT.create(AssessmentEditorUiBinder.class);
	
	interface AssessmentEditorUiBinder extends UiBinder<Widget, AssessmentEditor> {
	}
	
	/** Callback to execute when a field has been edited. */
	public static interface UpdateNodeCallback {
		void update();
	}
	
	/** Instance of the logger */
    private static Logger logger = Logger.getLogger(AssessmentEditor.class.getName());
	
	private static final String CHOICE_HELP_TEXT = "Provide an assessment of the learner on one or more concepts if this choice is selected for the question."
	        + "<br/><br/>The resulting performance assessment will be used to update the learner's state during course execution."
	        + "<br/><br/>The <b>confidence</b> value is a part of the learner's performance assessment but is currently not being used by GIFT.";
	
   private static final String END_HELP_TEXT = "Provide an assessment of the learner on one or more concepts if this is where the conversation ends based on the learner's decisions."
            + "<br/><br/>The resulting performance assessment will be used to update the learner's state during course execution."
            + "<br/><br/>The <b>confidence</b> value is a part of the learner's performance assessment but is currently not being used by GIFT.";
	
	@UiField
	protected InlineHTML heading;
	
	@UiField
	protected Widget textPanel;
	
	@UiField
	protected TextArea msgTextArea;
	
	@UiField
	protected FlowPanel extraneousPanel;
	
	/** 
     * The help button message
     */
    @UiField
    protected HTML assessmentTooltip;
	
	@UiField (provided=true)
	protected CellTable<AssessmentRecord> assessmentsCellTable = new CellTable<AssessmentRecord>();
	
    @UiField (provided=true)
    protected CellTable<AssessmentRecord> extraneousAssessmentsCellTable = new CellTable<AssessmentRecord>();
	
	protected ListDataProvider<AssessmentRecord> dataProvider = new ListDataProvider<AssessmentRecord>();
	
	protected ListDataProvider<AssessmentRecord> extraneousDataProvider = new ListDataProvider<AssessmentRecord>();
	
	@UiField (provided=true)
	protected Image addAssessmentImage = new Image(GatClientBundle.INSTANCE.add_image());
	
	@UiField (provided=true)
	protected Image editTextImage = new Image(GatClientBundle.INSTANCE.edit_image());
	
	private TreeNode assessmentNode;
	
	private List<String> courseConcepts = GatClientUtility.getBaseCourseConcepts();
	
	/** A callback used to update a node when its assessment data changes */
	final UpdateNodeCallback callback;
		
	
	/** 
	 * Creates a popup panel for editing TreeNode assessments.
	 * 
	 * @param callback The callback to execute when an assessment field has been changed.
	 */
	public AssessmentEditor(final UpdateNodeCallback callback) {
		
		this.callback = callback;
		
		setWidget(uiBinder.createAndBindUi(this));	
		
		setAutoHideEnabled(true);
		getElement().getStyle().setProperty("borderRadius", "5px");
		getElement().getStyle().setProperty("border", "solid 1px lightgray");
		getElement().getStyle().setProperty("boxShadow", "0px 5px 25px rgba(0,0,0,0.2)");
		msgTextArea.setEnabled(false);
		
		final TextAreaDialog editTextDialog  = new TextAreaDialog("Edit Choice", "Enter the choice text:", "Ok");
		editTextDialog.setValidationMessage("Text must be at least 3 characters.");
		
		editTextDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(event.getValue() != null && event.getValue().length() < 3) {
					editTextDialog.showValidationMessage(true);
					return;
				}
				
				assessmentNode.setText(event.getValue());
				msgTextArea.setText(event.getValue());
				callback.update();
				editTextDialog.hide();
			}
			
		});
		
		editTextImage.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				editTextDialog.center();
				editTextDialog.setValue(assessmentNode.getText());
			}
			
		});
		
		setupTable();
		setupExtraneousTable();
	}
	
	private void setupTable() {
		
		//
		// Setup the Assessment column
		//
		ExtendedSelectionCell assessmentSelectionCell = new ExtendedSelectionCell();
		
		List<String> assessmentChoices = new ArrayList<String>();
		for(AssessmentLevelEnum assessmentLevelEnum : AssessmentLevelEnum.VALUES()){			
			assessmentChoices.add(assessmentLevelEnum.getDisplayName());
		}
		assessmentSelectionCell.setDefaultOptions(assessmentChoices);
		
		Column<AssessmentRecord, String> assessmentSelectionColumn = new Column<AssessmentRecord, String>(assessmentSelectionCell){

			@Override
			public String getValue(AssessmentRecord record) {
				return record.getAssessmentLevel();
			}
		};
		assessmentSelectionColumn.setSortable(true);
		assessmentSelectionColumn.setFieldUpdater(new FieldUpdater<AssessmentRecord, String>() {
			
			@Override
			public void update(int index, AssessmentRecord key, String value) {
				key.setAssessmentLevel(value);
				assessmentNode.setAssessmentLevel(index, value);
			}
		});
		
		//
		// Setup the Concept column
		//
		List<String> conceptOptions;
				
		if(courseConcepts != null){
			conceptOptions = courseConcepts;
			
		} else {
			conceptOptions = new ArrayList<String>();
		}
		
		final ExtendedSelectionCell conceptNameCell = new ExtendedSelectionCell(conceptOptions);
		Column<AssessmentRecord, String> conceptNameColumn = new Column<AssessmentRecord, String>(conceptNameCell) {
			
			@Override
			public String getValue(AssessmentRecord record) {
			    // Note: string must match the list of choices put into this column (see conceptNameCell above)
				return record.getConcept() != null ? record.getConcept().toLowerCase() : record.getConcept();
			}
		};
		conceptNameColumn.setSortable(true);
		conceptNameColumn.setFieldUpdater(new FieldUpdater<AssessmentRecord, String>() {
			
			@Override
			public void update(int index, AssessmentRecord key, String value) {
			    
				if(conceptNameCausesConflict(assessmentNode, value)) {
				    
					WarningDialog.warning("Duplicate concept", "There is already an assessment for the concept named '"+value+"' at this part of the conversation.");
					conceptNameCell.clearViewData(key);
					dataProvider.refresh();
				} else {
					key.setConcept(value);
					assessmentNode.setConcept(index, value);
				}
				
				callback.update();
			}
		});
		
		//
		// Setup the Confidence column
		//		
		final TextInputCell confidenceCell = new TextInputCell();
		Column<AssessmentRecord, String> confidenceColumn = new Column<AssessmentRecord, String>(confidenceCell) {
			
			@Override
			public String getValue(AssessmentRecord record) {
				return record.getConfidence();
			}
		};
		confidenceColumn.setSortable(true);
		confidenceColumn.setFieldUpdater(new FieldUpdater<AssessmentRecord, String>() {
			
			@Override
			public void update(int index, AssessmentRecord key, String value) {
				try {
					Double confidence = Double.parseDouble(value);
					if(confidence < 0 || confidence > 1) {
						throw new IllegalArgumentException("Confidence value must be between 0 and 1 (inclusive).");
					}
					key.setConfidence(value);
					assessmentNode.setConfidence(index, value);
				} catch(@SuppressWarnings("unused") Exception e){
					WarningDialog.warning("Invalid value", "Confidence values must be a number between 0 and 1 (inclusive).");
					confidenceCell.clearViewData(key);
					dataProvider.refresh();
				}
				
				callback.update();
			}
		});
		
		//
		// Setup the delete column
		//
		Column<AssessmentRecord, String> deleteColumn = new Column<AssessmentRecord, String>(new ButtonCell(){
			
			@Override
		    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            String value, SafeHtmlBuilder sb) {
				
		        SafeHtml html = SafeHtmlUtils.fromTrustedString(new Image(value).toString());
		        sb.append(html);
		    	}
		}){
		
			@Override
			public String getValue(AssessmentRecord record){
				return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
			}
		};
		
		deleteColumn.setFieldUpdater(new FieldUpdater<AssessmentRecord, String>() {
			
			@Override
			public void update(final int index, final AssessmentRecord key, String value) {
				OkayCancelDialog.show("Delete Assessment", 
						"Are you sure you want to delete the '" + key.getConcept() + "' assessment?", 
						"Delete", 
						new OkayCancelCallback() {

							@Override
							public void okay() {
								dataProvider.getList().remove(key);
								dataProvider.refresh();
								assessmentNode.deleteAssessment(getAssessmentIndex(key));
								
								updateAddButton();
								
								callback.update();
							}

							@Override
							public void cancel() {
								callback.update();
							}
					
				});
			}
		});
		
		// Add the columns to the table
		assessmentsCellTable.addColumn(conceptNameColumn, "Concept");
		assessmentsCellTable.addColumn(assessmentSelectionColumn, "Assessment");
		assessmentsCellTable.addColumn(confidenceColumn, "Confidence");
		assessmentsCellTable.addColumn(deleteColumn);
		assessmentsCellTable.setColumnWidth(deleteColumn, "0%");
		
		//
		// setup column sorting comparators
		//
		ColumnSortEvent.ListHandler<AssessmentRecord> conceptNameColumnSortHandler = new ColumnSortEvent.ListHandler<>(
                dataProvider.getList());
		conceptNameColumnSortHandler.setComparator(conceptNameColumn,
                new Comparator<AssessmentRecord>() {
            
            @Override
            public int compare(AssessmentRecord thisObject, AssessmentRecord thatObject) {
                if (thisObject == thatObject) {
                    return 0;
                }

                // Compare the concent name
                if (thisObject != null) {
                    return (thatObject != null) ?
                            thisObject.getConcept().compareTo(thatObject.getConcept()) : 1;
                }
                return -1;
            }
        });
		
        ColumnSortEvent.ListHandler<AssessmentRecord> assessmentColumnSortHandler = new ColumnSortEvent.ListHandler<>(
                dataProvider.getList());
        assessmentColumnSortHandler.setComparator(assessmentSelectionColumn,
                new Comparator<AssessmentRecord>() {
            
            @Override
            public int compare(AssessmentRecord thisObject, AssessmentRecord thatObject) {
                if (thisObject == thatObject) {
                    return 0;
                }

                // Compare the assessment name
                if (thisObject != null) {
                    return (thatObject != null) ?
                            thisObject.getAssessmentLevel().compareTo(thatObject.getAssessmentLevel()) : 1;
                }
                return -1;
            }
        });
        
        ColumnSortEvent.ListHandler<AssessmentRecord> confidenceColumnSortHandler = new ColumnSortEvent.ListHandler<>(
                dataProvider.getList());
        confidenceColumnSortHandler.setComparator(confidenceColumn,
                new Comparator<AssessmentRecord>() {
            
            @Override
            public int compare(AssessmentRecord thisObject, AssessmentRecord thatObject) {
                if (thisObject == thatObject) {
                    return 0;
                }

                // Compare the confidence name
                if (thisObject != null) {
                    return (thatObject != null) ?
                            thisObject.getConfidence().compareTo(thatObject.getConfidence()) : 1;
                }
                return -1;
            }
        });
        
		assessmentsCellTable.addColumnSortHandler(conceptNameColumnSortHandler);
        assessmentsCellTable.addColumnSortHandler(assessmentColumnSortHandler);
        assessmentsCellTable.addColumnSortHandler(confidenceColumnSortHandler);
		
		// Setup the add button
		addAssessmentImage.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    
			    //find next course concept that doesn't have an assessment
			    String nextConcept = null;
			    boolean hasAssessment;
			    for(String courseConcept : courseConcepts){
			        
			        hasAssessment = false;
                    for(AssessmentRecord record : dataProvider.getList()){
                        
                        if(record.getConcept().equalsIgnoreCase(courseConcept)){
                            //already have an assessment for this concept
                            hasAssessment = true;
                            break;
                        }
                    }
                    
                    if(!hasAssessment){
                        nextConcept = courseConcept;
                        break;
                    }
			    }

			    if(nextConcept == null){
			        //there are no other concepts, the add button should be disabled
			        //show warning to author as well
			        
			        return;
			    }
				
			    AssessmentRecord record = new AssessmentRecord();
				record.setConcept(nextConcept);
				record.setAssessmentLevel(AssessmentLevelEnum.AT_EXPECTATION.getDisplayName());
				record.setConfidence("0.9");
				
				dataProvider.getList().add(record);
				updateAddButton();
				
				addAssessment(record);
				
				callback.update();
			}
		});
		
		// Setup the data provider
		dataProvider.addDataDisplay(assessmentsCellTable);
	}
	
	/**
	 * Build the extraneous concept cell table which shows the assessed concepts in this conversation node
	 * that are not course concepts.  This way the author can remove them.
	 */
	private void setupExtraneousTable(){
	    
	    //
	    // Setup the concept name column
	    //
        TextColumn<AssessmentRecord> conceptNameColumn = new TextColumn<AssessmentRecord>() {
            
            @Override
            public String getValue(AssessmentRecord record) {
                return record.getConcept();
            }
            
            @Override 
            public void render(Context context, AssessmentRecord record, SafeHtmlBuilder sb){ 
                    sb.appendHtmlConstant("<div style='color: red; font-style: italic;'>"); 
                    sb.appendEscaped(record.getConcept()); 
                    sb.appendHtmlConstant("</div>"); 

            } 
        };
        
        //
        // Setup the Assessment column
        //        
        TextColumn<AssessmentRecord> assessmentSelectionColumn = new TextColumn<AssessmentRecord>(){

            @Override
            public String getValue(AssessmentRecord record) {
                return record.getAssessmentLevel();
            }
        };
        
        //
        // Setup the Confidence column
        //      
        TextColumn<AssessmentRecord> confidenceColumn = new TextColumn<AssessmentRecord>() {
            
            @Override
            public String getValue(AssessmentRecord record) {
                return record.getConfidence();
            }
        };
        
        //
        // Setup the delete column
        //
        Column<AssessmentRecord, String> deleteColumn = new Column<AssessmentRecord, String>(new ButtonCell(){
            
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, 
                    String value, SafeHtmlBuilder sb) {
                
                SafeHtml html = SafeHtmlUtils.fromTrustedString(new Image(value).toString());
                sb.append(html);
                }
        }){
        
            @Override
            public String getValue(AssessmentRecord record){
                return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
            }
        };
        
        deleteColumn.setFieldUpdater(new FieldUpdater<AssessmentRecord, String>() {
            
            @Override
            public void update(final int index, final AssessmentRecord key, String value) {
                OkayCancelDialog.show("Delete Assessment", 
                        "Are you sure you want to delete the '" + key.getConcept() + "' assessment?", 
                        "Delete", 
                        new OkayCancelCallback() {

                            @Override
                            public void okay() {
                                extraneousDataProvider.getList().remove(key);
                                extraneousDataProvider.refresh();
                                
                                
                                assessmentNode.deleteAssessment(getAssessmentIndex(key));
                                
                                extraneousPanel.setVisible(!extraneousDataProvider.getList().isEmpty());
                            }

                            @Override
                            public void cancel() {
                                // nothing to do
                            }
                    
                });
            }
        });
        
        // Add the columns to the table
        extraneousAssessmentsCellTable.addColumn(conceptNameColumn, "Concept");
        extraneousAssessmentsCellTable.addColumn(assessmentSelectionColumn, "Assessment");
        extraneousAssessmentsCellTable.addColumn(confidenceColumn, "Confidence");
        extraneousAssessmentsCellTable.addColumn(deleteColumn);
        extraneousAssessmentsCellTable.setColumnWidth(deleteColumn, "0%");
        
        extraneousDataProvider.addDataDisplay(extraneousAssessmentsCellTable);
	}
	
	/**
	 * Return the index of the concept assessment in the conversation tree node.
	 * This is useful for removing that assessment element from the corresponding XML data model.
	 * 
	 * @param record the assessment record to look for in the assessment element of the tree node
	 * @return the index of the assessment in the element, -1 if it couldnt be found
	 */
	private int getAssessmentIndex(AssessmentRecord record){
	    
	       for(int index = 0; index < getAssessmentsLength(assessmentNode); index++) {
	           
	           if(record.getConcept().equalsIgnoreCase(assessmentNode.getConcept(index))){
	               return index;
	           }
	       }
	       
	       return -1;
	}

	/**
	 * Sets the TreeNode containing an assessment to edit.
	 * 
	 * @param assessmentNode The TreeNode with an assessment to edit.
	 */
	public void setAssessmentNode(TreeNode assessmentNode) {
		this.assessmentNode = assessmentNode;		
		msgTextArea.setValue(assessmentNode.getText());
		
		dataProvider.getList().clear();
		extraneousDataProvider.getList().clear();
		
		for(int index = 0; index < getAssessmentsLength(assessmentNode); index++) {
		    
		    boolean isExtraneous = true;
		    if(courseConcepts != null){
		        
		        // perform case insensitive comparison
		        for(String courseConcept : courseConcepts){
		            
		            if(courseConcept.equalsIgnoreCase(assessmentNode.getConcept(index))){
		                isExtraneous = false;
		                break;
		            }
		        }
    		    
		    }

		    if(isExtraneous){
		        extraneousDataProvider.getList().add(new AssessmentRecord(
	                        assessmentNode.getConcept(index),
	                        assessmentNode.getAssessmentLevel(index),
	                        assessmentNode.getConfidence(index)));
		    }else{
		        dataProvider.getList().add(new AssessmentRecord(
                        assessmentNode.getConcept(index),
                        assessmentNode.getAssessmentLevel(index),
                        assessmentNode.getConfidence(index)));
		    }
		}
		
		//check if all course concepts are in the assessment table
		updateAddButton();
		
		if(TreeNodeEnum.END_NODE.equals(TreeNodeEnum.fromName(assessmentNode.getType()))){
			
			//neeed to hide the text editor for end nodes, since theit text isn't editable
			heading.setText("End Assessment");
			assessmentTooltip.setHTML(END_HELP_TEXT);
			textPanel.setVisible(false);
			
		} else {
			
			heading.setText("Choice Assessment");
			assessmentTooltip.setHTML(CHOICE_HELP_TEXT);
			textPanel.setVisible(true);
		}
		
		dataProvider.refresh();
		extraneousDataProvider.refresh();
		
		extraneousPanel.setVisible(!extraneousDataProvider.getList().isEmpty());
	}
	
	/**
	 * Set whether the add assessment button is visible or not depending on
	 * whether all course concepts are represented in the assessment table.  This
	 * will prevent the author from having duplicate concept assessments.
	 */
	private void updateAddButton(){
	    
        boolean found = false;
        if(courseConcepts != null){
            for(String concept : courseConcepts){
                
                found = false;
                for(AssessmentRecord record : dataProvider.getList()){
                    
                    if(record.getConcept().equalsIgnoreCase(concept)){
                        found = true;
                        break;
                    }
                }
                
                if(!found){
                    //didn't find this concept so stop searching
                    
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Didn't find '"+concept+"' course concept in assessment table.");
                    }
                    break;
                }
            }
        }
        
        if(found){
            //found all concepts
            
            if(logger.isLoggable(Level.INFO)){
                logger.info("Hiding add assessment button because all course concepts are in assessment table.");
            }
            addAssessmentImage.setVisible(false);
        }else{
            
            if(logger.isLoggable(Level.INFO)){
                logger.info("Showing add assessment button because all course concepts are in assessment table.");
            }
            addAssessmentImage.setVisible(true);
        }
	}
	
	/**
	 * Displays the popup panel at the upper right of the view.
	 */
	public void showEditor() {
	    
        if(courseConcepts == null || courseConcepts.isEmpty()){
            
            WarningDialog.error("Missing Course Concepts",
                    "Before opening the conversation node's assessment editor please take a moment to author one or more course concepts."+
                    "  The assessment editor allows you to create a performance assessment "+
                    "during course execution if the learner happens to reach this part of the course." + 
                    "<br/><br/>" +
                    "<b>How to add course concepts:</b><br/>"+
                    "<ol><li>Exit the conversation tree editor selecting one of the buttons on the bottom right.</li>"+
                    "<li>Click 'Course Properties' on the left hand side of the course creator.</li>"+
                    "<li>Author course concepts under the 'CONCEPTS' section on the course properties panel.</li></ol><br/>");
        }else{
            //only show the assessment editor if there are course concepts, otherwise the author
            //could create some invalid state (i.e. adding assessments with no concepts), plus we would
            //have to deal with displaying concepts in the conversation that aren't associated with the course.
            show();
            getElement().getStyle().setProperty("right", "17px");
            getElement().getStyle().setProperty("left", "auto");
            getElement().getStyle().setProperty("top", "77px");
        }		

	}
	
	/**
	 * Adds an assessment record to the assessment TreeNode
	 * 
	 * @param record The assessment record to add.
	 */
	private void addAssessment(AssessmentRecord record) {
		
		if(courseConcepts == null || courseConcepts.isEmpty()){
			
			WarningDialog.error("Missing Course Concepts",
                    "Before opening the conversation node's assessment editor please take a moment to author one or more course concepts."+
                    "  The assessment editor allows you to create a performance assessment "+
                    "during course execution if the learner happens to reach this part of the course." + 
                    "<br/><br/>" +
                    "<b>How to add course concepts:</b><br/>"+
                    "<ol><li>Exit the conversation tree editor selecting one of the buttons on the bottom right.</li>"+
                    "<li>Click 'Course Properties' on the left hand side of the course creator.</li>"+
                    "<li>Author course concepts under the 'CONCEPTS' section on the course properties panel.</li></ol><br/>");
			
			return;
		}
		
		assessmentNode.addAssessment(record.getConcept(), record.getAssessmentLevel(), record.getConfidence());
	}
	
	/**
	 * Checks if the assessment concept name conflicts with any other concept names
	 * 
	 * @param treeNode The TreeNode containing assessments
	 * @param conceptName The name of the concept
	 * @return true if the concept name causes a conflict, false otherwise.
	 */
	private native boolean conceptNameCausesConflict(JavaScriptObject treeNode, String conceptName) /*-{
		var assessmentArray = treeNode.assessments;
		var conflict = false;
		assessmentArray.forEach(function(d, i) {
			if(d.concept == conceptName) {
				conflict = true;
			}
		});
		
		return conflict;
		
	}-*/;
		
	/**
	 * Gets the number of available assessments.
	 * 
	 * @param treeNode The TreeNode containing assessments.
	 * @return the number of available assessments.
	 */
	private native int getAssessmentsLength(JavaScriptObject treeNode) /*-{
		return treeNode.assessments == null ? 0 : treeNode.assessments.length;
	}-*/;
	
	/**
	 * A container for the assessment properties in the table.
	 */
	private static class AssessmentRecord {
		
		private String concept;
		private String assessmentLevel;
		private String confidence;
			
		/**
		 * Creates an empty assessment record.
		 */
		public AssessmentRecord() {
		}
		
		/**
		 * Initializes a new assessment record.
		 * 
		 * @param concept The concept for this assessment.
		 * @param assessmentLevel The assessment expectation level.
		 * @param confidence The confidence value of this assessment.
		 */
		public AssessmentRecord(String concept, String assessmentLevel, String confidence) {
			this.concept = concept;
			this.assessmentLevel = assessmentLevel;
			this.confidence = confidence;
		}
		
		/**
		 * Gets the assessment expectation level.
		 * 
		 * @return the assessment expectation level 
		 */
		public String getAssessmentLevel() {
			return assessmentLevel;
		}
		
		/**
		 * Sets the assessment expectation level. 
		 * 
		 * @param assessmentLevel The assessment expectation level to set
		 */
		public void setAssessmentLevel(String assessmentLevel) {
			this.assessmentLevel = assessmentLevel;
		}
		
		/**
		 * Gets the concept for this assessment record.
		 * 
		 * @return the concept.
		 */
		public String getConcept() {
			return concept;
		}
		
		/**
		 * Sets the concept for this assessment record.
		 * 
		 * @param concept The concept to set
		 */
		public void setConcept(String concept) {
			this.concept = concept;
		}
		
		/**
		 * Gets the confidence value for this assessment record.
		 * 
		 * @return the confidence
		 */
		public String getConfidence() {
			return confidence;
		}
		/**
		 * Sets the confidence value for this assessment record.
		 * 
		 * @param value the confidence to set
		 */
		public void setConfidence(String value) {
			this.confidence = value;
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[AssessmentRecord: concept=");
            builder.append(concept);
            builder.append(", assessmentLevel=");
            builder.append(assessmentLevel);
            builder.append(", confidence=");
            builder.append(confidence);
            builder.append("]");
            return builder.toString();
        }
		
	}
}
