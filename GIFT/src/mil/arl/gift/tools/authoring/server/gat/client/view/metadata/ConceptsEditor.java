/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import generated.metadata.ActivityType;
import generated.metadata.Attribute;
import generated.metadata.Attributes;
import generated.metadata.BooleanEnum;
import generated.metadata.Concept;
import generated.metadata.Metadata;
import generated.metadata.Metadata.Concepts;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.ContentReferenceEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;

/**
 * @author nroberts
 *
 */
public class ConceptsEditor extends Composite {

	private static ConceptsEditorUiBinder uiBinder = GWT
			.create(ConceptsEditorUiBinder.class);

	interface ConceptsEditorUiBinder extends UiBinder<Widget, ConceptsEditor> {
	}
	
	private static Logger logger = Logger.getLogger(ConceptsEditor.class.getName());

	@UiField(provided=true)
	protected DataGrid<CandidateConcept> conceptsTable = new DataGrid<CandidateConcept>();
	
	@UiField(provided=true)
	protected DataGrid<CandidateMetadataAttribute> attributesTable = new DataGrid<CandidateMetadataAttribute>();
	
	@UiField
	protected Widget attributesPanel;
	
	
	//'Concepts:' table columns ----------------------------------------------------------------------------------------------------------
	
	/** The checkbox column for the 'Concepts:' table */
    private Column<CandidateConcept, Boolean> conceptSelectionColumn = new Column<CandidateConcept, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateConcept candidate) {
			
			return candidate.isChosen();
		}
		
	   @Override
		public void render(Context context, CandidateConcept concept, SafeHtmlBuilder sb) {
			
			if(!candidateToCourseConcept.containsKey(concept)) {
				// if the concept is selected in conceptSelectionColumn, make the checkbox disabled
				sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>"));
			
			} else if(concept.isChosen()) {
				// if the concept is selected only in the practiceConceptSelectionColumn, just check the box
				sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>"));
			
			} else {
				// if the concept is being deselected, uncheck the box
				sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>"));
			}
		} 
    	
    };
	
	/** The name column for the 'Concepts:' table */
    private Column<CandidateConcept, SafeHtml> conceptNameColumn = new Column<CandidateConcept, SafeHtml>(new SafeHtmlCell()){

		@Override
		public SafeHtml getValue(CandidateConcept candidate) {

			if(candidate.getConceptName() != null ){
				
				if(candidateToCourseConcept.containsKey(candidate)){
					return SafeHtmlUtils.fromString(candidate.getConceptName());
					
				} else {
					
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("<span style='color: red;'>")
						.appendEscaped(candidate.getConceptName())
						.appendEscaped(" (This is not a concept in this course and will not be saved")
						.appendHtmlConstant("</span>");
					
					return sb.toSafeHtml();
				}
			}
			
			return null;
		}
	    	
	};
	    
	    
    //'Attributes:' table columns --------------------------------------------------------------------------------------------------------
	
    /** The checkbox column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, Boolean> attributeSelectionColumn = new Column<CandidateMetadataAttribute, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateMetadataAttribute candidate) {
			
			return candidate.isChosen();
		}
    	
    };
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, String> attributeNameColumn = new Column<CandidateMetadataAttribute, String>(new TextCell()){

		@Override
		public String getValue(CandidateMetadataAttribute candidate) {

			if(candidate.getAttribute() != null ){
				return candidate.getAttribute().getDisplayName();
			}
			
			return null;
		}
    	
    };
	    
	/** The concepts currently being edited*/
	private Concepts concepts;
	
	private ListDataProvider<CandidateConcept> conceptsDataProvider = new ListDataProvider<CandidateConcept>();
	
	private SingleSelectionModel<CandidateConcept> conceptsSelectionModel = new SingleSelectionModel<CandidateConcept>();
	
	private ListDataProvider<CandidateMetadataAttribute> attributesDataProvider = new ListDataProvider<CandidateMetadataAttribute>();
	
	private Metadata metadata;
	
	private Map<CandidateConcept, Concept> candidateToCourseConcept = new HashMap<CandidateConcept, Concept>();	
	private Map<CandidateConcept, Concept> candidateToLoadedConcept = new HashMap<CandidateConcept, Concept>();
	
	/** The reference editor used to modify the content associated with this editor's metadata */
    private ContentReferenceEditor referenceEditor;
	
	public ConceptsEditor() {
		initWidget(uiBinder.createAndBindUi(this));
		
		final List<String> courseConcepts = GatClientUtility.getBaseCourseConcepts();					
		
		// initialize 'Concepts:' table
        
        conceptsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        conceptsTable.setPageSize(Integer.MAX_VALUE);
        
        conceptsTable.addColumn(conceptSelectionColumn);
        conceptsTable.addColumn(conceptNameColumn);
        conceptsTable.setColumnWidth(conceptSelectionColumn, "50px");
        
        conceptSelectionColumn.setFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {
			
			@Override
			public void update(int index, CandidateConcept candidate, Boolean value) {
				
				candidate.setChosen(value);
				
				Concept concept = null;
				
				if(candidateToCourseConcept.containsKey(candidate)){
					concept = candidateToCourseConcept.get(candidate);
					
				} else if(candidateToLoadedConcept.containsKey(candidate)){
					concept = candidateToLoadedConcept.get(candidate);					
				}
				
				if(concept != null){
					
					//remove any outdated concepts with the same concept name
					if(concepts != null){
						
						Iterator<Concept> itr = concepts.getConcept().iterator();
						
						while(itr.hasNext()){
							
							Concept existingConcept = itr.next();
							
							if(existingConcept.getName() != null && existingConcept.getName().equalsIgnoreCase(concept.getName())){
								itr.remove();
							}
						}
					}
					
					//add the new concept in their place
					if(value){
						concepts.getConcept().add(concept);
					}				
					referenceEditor.conceptSelected(concept.getName(), Boolean.TRUE.equals(value));
				}
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
		});
        
        conceptsDataProvider.addDataDisplay(conceptsTable);
        
        for(String courseConcept : courseConcepts){
        	
        	CandidateConcept candidate = new CandidateConcept(courseConcept, false);
        	
        	Concept concept = new Concept();
        	concept.setName(courseConcept);
        	
        	candidateToCourseConcept.put(candidate, concept);
        }
        
        conceptsDataProvider.getList().addAll(candidateToCourseConcept.keySet());
        
        conceptsTable.setSelectionModel(conceptsSelectionModel);
        conceptsSelectionModel.addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				
				attributesDataProvider.getList().clear();
				
				CandidateConcept candidate = conceptsSelectionModel.getSelectedObject();
				
				Concept concept = null;
				
				if(candidateToCourseConcept.containsKey(candidate)){
					concept = candidateToCourseConcept.get(candidate);
					
				} else if(candidateToLoadedConcept.containsKey(candidate)){
					concept = candidateToLoadedConcept.get(candidate);					
				}
				
				if(concept != null){
					
                    //provide an empty set of attributes for this concept
                    generated.metadata.ActivityType.Passive passive = null;
                    
                    if(concept.getActivityType() == null){
                        concept.setActivityType(new ActivityType());
                    }
                    
                    if(concept.getActivityType().getType() != null){
                        // the type for this concept has already been defined
                        
                        if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                            // the predefined types is passive, passive needs associated metadata attributes
                            passive = (generated.metadata.ActivityType.Passive)concept.getActivityType().getType();
                        }else{
                            // this is neither constructive or active (not passive), which doesn't have metadata attributes.
                            // nothing else to do here
                            return;
                        }                        
                        
                    } else {
                        
                        passive = new generated.metadata.ActivityType.Passive();
                        passive.setAttributes(new generated.metadata.Attributes());
                        concept.getActivityType().setType(passive);
                    }
                    
					List<MetadataAttributeEnum> selectedAttributes = new ArrayList<MetadataAttributeEnum>();
					
					//Populate selectedAttributes with each
					//MetadataAttributeEnum contained within the selected
					//concept.
                    if(passive != null && passive.getAttributes() != null){
                        //Populate selectedAttributes with each
                        //MetadataAttributeEnum contained within the selected
                        //concept.
                        for(Attribute attribute : passive.getAttributes().getAttribute()){
                            
                            if(attribute.getValue() != null){
                                
                                try{
                                    selectedAttributes.add(MetadataAttributeEnum.valueOf(attribute.getValue()));
                                    
                                } catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
                                    //don't add an attribute that can't be shown
                                }
                            }
                        }   
                    }		
                    
					//Populate the attributes table with each MetadataAttribute
					for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
						
						if(metadata.getPresentAt() != null){
						    
						    boolean isValidAttributeForPhase = false;
							
							if(metadata.getPresentAt().getMerrillQuadrant() != null){
								
								MerrillQuadrantEnum quadrant = MerrillQuadrantEnum.valueOf(metadata.getPresentAt().getMerrillQuadrant());
								
        						isValidAttributeForPhase = (attribute.isContentAttribute() && quadrant == MerrillQuadrantEnum.RULE) ||
                                        (attribute.isContentAttribute() && quadrant == MerrillQuadrantEnum.EXAMPLE) ||
                                        (attribute.isContentAttribute() && quadrant == MerrillQuadrantEnum.RECALL) ||
                                        (attribute.isPracticeAttribute() && quadrant == MerrillQuadrantEnum.PRACTICE);	
        					
							} else if(BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly())) {
							    isValidAttributeForPhase = attribute.isContentAttribute();
							}
							
							//if this attribute is selected and is allowed for this phase, select its candidate
							if(isValidAttributeForPhase && selectedAttributes.contains(attribute)){
                                
                                CandidateMetadataAttribute candidateMetadata = new CandidateMetadataAttribute(concept, attribute, true);
                                
                                attributesDataProvider.getList().add(candidateMetadata);
                            
                            } else {
                                
                                CandidateMetadataAttribute candidateMetadata = new CandidateMetadataAttribute(concept, attribute, false);
                                
                                attributesDataProvider.getList().add(candidateMetadata);
                            }
						}						
					}
					
					attributesDataProvider.refresh();					
				}
			}
		});
        
        VerticalPanel emptyConceptsWidget = new VerticalPanel();
        emptyConceptsWidget.setSize("100%", "100%");
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        Label emptyConceptsLabel = new Label("No concepts were found. Please add a concept to begin editing it and its associated attributes.");
        emptyConceptsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyConceptsWidget.add(emptyConceptsLabel);
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        conceptsTable.setEmptyTableWidget(emptyConceptsWidget);
        
        
        // initialize 'Attributes:' table
        
        attributesTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        attributesTable.setPageSize(Integer.MAX_VALUE);
        
        attributesTable.addColumn(attributeSelectionColumn);
        attributesTable.addColumn(attributeNameColumn);
        attributesTable.setColumnWidth(attributeSelectionColumn, "50px");
        attributesTable.setSelectionModel(new SingleSelectionModel<CandidateMetadataAttribute>());
        
        attributeSelectionColumn.setFieldUpdater(new FieldUpdater<CandidateMetadataAttribute, Boolean>() {
            
            @Override
            public void update(int arg0, CandidateMetadataAttribute candidate, Boolean hasBeenSelected) {
                
                Concept parentConcept = candidate.getParentConcept();
                generated.metadata.ActivityType.Passive parentPassiveContent = null;
                if(parentConcept.getActivityType() != null && 
                        parentConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    parentPassiveContent = (generated.metadata.ActivityType.Passive)parentConcept.getActivityType().getType();
                }else{
                    generated.metadata.ActivityType activityType = new generated.metadata.ActivityType();
                    parentPassiveContent = new generated.metadata.ActivityType.Passive();
                    parentPassiveContent.setAttributes(new generated.metadata.Attributes());
                    activityType.setType(parentPassiveContent);
                }
                
                Attribute attribute = null;
                
                if(parentPassiveContent.getAttributes() != null){
                    for(Attribute existingAttribute : parentPassiveContent.getAttributes().getAttribute()){
                            
                        if(existingAttribute.getValue() != null && candidate.getAttribute().getName().equals(existingAttribute.getValue())){
                            attribute = existingAttribute;
                        }
                    } 
                }else{
                    parentPassiveContent.setAttributes(new generated.metadata.Attributes());
                }
                
                if(hasBeenSelected){
                    
                    if(attribute == null){
                        
                        attribute = new Attribute();
                        attribute.setValue(candidate.getAttribute().getName());
                        
                        candidate.setChosen(parentPassiveContent.getAttributes().getAttribute().add(attribute));
                        
                    } else {
                        candidate.setChosen(true);
                    }
                    
                } else {
                    
                    if(attribute != null){                      
                        candidate.setChosen(!parentPassiveContent.getAttributes().getAttribute().remove(attribute));
                    
                    } else {
                        candidate.setChosen(false);
                    }
                }
                
                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }
        });
        
        attributesDataProvider.addDataDisplay(attributesTable);
        
        VerticalPanel emptyAttributesWidget = new VerticalPanel();
        emptyAttributesWidget.setSize("100%", "100%");
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        Label emptyAttributesLabel = new Label("Please select a concept to view the attributes assigned to it.");
        emptyAttributesLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyAttributesWidget.add(emptyAttributesLabel);
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        attributesTable.setEmptyTableWidget(emptyAttributesWidget);
	}
	
	/**
	 * Loads the concepts from the given metadata into this editor so they can be modified
	 * 
	 * @param metadata the metadata to load the concepts from
	 */
    public void handleConcepts(Metadata metadata){
        //TODO What if the concepts contain attributes that aren't appropriate for the quadrant?
		
		if(metadata == null){
			logger.severe("ConceptsEditor: Failed to set concepts. No metadata has been provided.");
			return;
		}
        
        this.metadata = metadata;
        
        boolean needsAttributes = true;
        
        if(metadata.getPresentAt() != null                                  
                && BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly())){
            
            if(referenceEditor != null && referenceEditor.getQuestionExport() != null ) {
                
                //the metadata references a question export for remediation only presentation,
                //currently this means either constructive or active activity type (not passive)
                needsAttributes = false;
                
            }else if(referenceEditor != null && referenceEditor.getConversationTree() != null){
                
                // the metadata references a conversation tree for remediation only presentation,
                // currently this means Active activity type (not passive)
                needsAttributes = false;
                
            } else if(metadata.getContent() instanceof Metadata.TrainingApp) {
                
                //the metadata references a training app for remediation only presentation,
                //currently this means interactive activity type (not passive)
                needsAttributes = false;
            
            } else if(metadata.getContent() instanceof Metadata.LessonMaterial && isInteractiveOnlyContent()) {
                
                //the metadata references a training app for remediation only presentation,
                //currently this means interactive activity type (not passive)
                needsAttributes = false;
            }
            
        }
        
        attributesPanel.setVisible(needsAttributes);
        
        Concepts concepts = metadata.getConcepts();
        
        candidateToLoadedConcept.clear();
        
        if(concepts == null){
            this.concepts = new Concepts();
            metadata.setConcepts(this.concepts);
        
        } else {        
            this.concepts = concepts;
        }
        
        for(Concept concept : candidateToCourseConcept.values()){
            updateActivityType(concept);        
        }
        
        conceptsDataProvider.getList().clear();
        conceptsDataProvider.getList().addAll(candidateToCourseConcept.keySet());
        
        Iterator<Concept> itr = this.concepts.getConcept().iterator();
        
        Map<Concept, Concept> conceptToReplacement = new HashMap<Concept, Concept>();
        
        while(itr.hasNext()){
            
            Concept concept = itr.next();
            
            String conceptName = concept.getName();
            
            //check to see if another concept already handles this concept name. If one does, merge this one into it.
            CandidateConcept mergeCandidate = null;
            
            for(CandidateConcept courseConceptCandidate : candidateToCourseConcept.keySet()){
                
                Concept courseConcept = candidateToCourseConcept.get(courseConceptCandidate);
                
                if(courseConcept.getName() != null && courseConcept.getName().equalsIgnoreCase(concept.getName())){
                    mergeCandidate = courseConceptCandidate;
                    break;
                }               
            }
            
            for(CandidateConcept loadedConceptCandidate : candidateToLoadedConcept.keySet()){
                
                Concept loadedConcept = candidateToLoadedConcept.get(loadedConceptCandidate);
                
                if(loadedConcept.getName() != null && loadedConcept.getName().equalsIgnoreCase(concept.getName())){
                    mergeCandidate = loadedConceptCandidate;
                    break;
                }               
            }
            
            Concept mergeConcept = null;
            
            if(candidateToCourseConcept.containsKey(mergeCandidate)){
                mergeConcept = candidateToCourseConcept.get(mergeCandidate);
                
            } else if(candidateToLoadedConcept.containsKey(mergeCandidate)){
                mergeConcept = candidateToLoadedConcept.get(mergeCandidate);                    
            }
            
            if(mergeCandidate != null && mergeConcept != null){
                
                Attributes mergeAttributes = new Attributes();
                
                List<String> handledAttributeNames = new ArrayList<String>();
                
                if(mergeConcept.getActivityType() != null && mergeConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    
                    generated.metadata.ActivityType.Passive passiveContent = (generated.metadata.ActivityType.Passive)mergeConcept.getActivityType().getType();
                    if(passiveContent.getAttributes() != null){
                        for(Attribute attribute : passiveContent.getAttributes().getAttribute()){
                            mergeAttributes.getAttribute().add(attribute);
                            handledAttributeNames.add(attribute.getValue()); 
                        }
                    }
                }
                
                if(mergeConcept.getActivityType() != null && mergeConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    
                    generated.metadata.ActivityType.Passive passiveContent = (generated.metadata.ActivityType.Passive)concept.getActivityType().getType();
                    if(passiveContent.getAttributes() != null){
                        for(Attribute attribute : passiveContent.getAttributes().getAttribute()){
                            
                            if(attribute.getValue() != null && !handledAttributeNames.contains(attribute.getValue())){
                                mergeAttributes.getAttribute().add(attribute);
                            }
                        }
                    }
                }
                
                if(mergeConcept.getActivityType() == null){
                    mergeConcept.setActivityType(new generated.metadata.ActivityType());
                }
                
                if(mergeConcept.getActivityType().getType() == null){  
                    updateActivityType(mergeConcept);
                }

                if(mergeConcept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    ((generated.metadata.ActivityType.Passive)mergeConcept.getActivityType().getType()).setAttributes(mergeAttributes);
                }
                
                mergeCandidate.setChosen(true);
                
                //keep track of this merge so that the loaded concept can be replaced
                conceptToReplacement.put(concept, mergeConcept);
                
            } else {
                
                CandidateConcept candidate = new CandidateConcept(conceptName, true);
                
                candidateToLoadedConcept.put(candidate, concept);
                
                conceptsDataProvider.getList().add(candidate);
                
                //this concept is not defined by the course, we should attempt to remove it
                itr.remove();
            }
        } //end if
        
        //replace all the loaded concepts that needed to be merged with their merged counterparts
        for(Concept concept : conceptToReplacement.keySet()){
            this.concepts.getConcept().set(this.concepts.getConcept().indexOf(concept), conceptToReplacement.get(concept));
        }
    }
	
	/**
	 * A representation of a metadata attribute to be used in course content. This class is only intended to be used in 
	 * displaying data to the user, not modifying the underlying metadata.
	 * 
	 * @author nroberts
	 */
	public static class CandidateMetadataAttribute{
		
		/** The parent concept to which the attribute represented by this candidate belongs */
		private Concept parentConcept;
		
		/** The attribute represented by this candidate */
		private MetadataAttributeEnum attribute;
		
		/** Whether or not attribute represented by this candidate has been selected */
		private boolean isChosen = false;
		
		/**
		 * Creates a candidate for the specified attribute.
		 * 
		 * @param parentConcept the parent concept to which the attribute represented by this candidate belongs
		 * @param attribute the name of the attribute this candidate represents
		 * @param isChosen whether or not the attribute has been selected
		 */
		public CandidateMetadataAttribute(Concept parentConcept, MetadataAttributeEnum attribute, boolean isChosen){
			this.setChosen(isChosen);
			this.attribute = attribute;
			this.parentConcept = parentConcept;
		}

		/**
		 * Gets whether or not the attribute represented by this candidate has been chosen
		 * 
		 * @return  whether or not the attribute represented by this candidate has been chosen
		 */
		public boolean isChosen() {
			return isChosen;
		}

		/**
		 * Sets whether or not the attribute represented by this candidate has been chosen
		 * 
		 * @param isChosen whether or not the attribute represented by this candidate has been chosen
		 */
		public void setChosen(boolean isChosen) {
			this.isChosen = isChosen;
		}

		/**
		 * Gets the name of the attribute represented by this candidate
		 * 
		 * @return the name of the attribute represented by this candidate
		 */
		public MetadataAttributeEnum getAttribute() {
			return attribute;
		}
		
		/** 
		 * Gets the parent concept to which the attribute represented by this candidate belongs
		 * 
		 * @return the parent concept to which the attribute represented by this candidate belongs
		 */
		public Concept getParentConcept(){
			return parentConcept;
		}
		
	}

	/**
	 * Updates the concepts tables to account for changes in the given reference editor. This is
	 * mainly used to allow content types that require additional loading to initialize their
	 * concepts with the proper attributes (such as with question exports, which exclude 
	 * attributes altogether)
	 * 
	 * @param metadata the metadata containing the concepts
	 * @param editor the reference editor
	 */
	public void updateConceptsForType(Metadata metadata, ContentReferenceEditor editor){
		
        this.referenceEditor = editor;
		
        if(metadata != null){
            //reload the concepts so they can be changed to use the proper attributes
            handleConcepts(metadata);
        }
	}

	/**
     * Updates the activity type of the given concept based on the current visual state of the editor. 
     * If constructive, active, or interactive content is being authored, then this method will update 
     * the concept to use the Constructive, Active, or Interactive activity type, respectively.
     * 
     * @param concept the concept whose activity type should be updated. If null, this method will do nothing.
     */
	private void updateActivityType(Concept concept) {
        
        if(concept.getActivityType() == null){
            concept.setActivityType(new generated.metadata.ActivityType());
        }
            
        Serializable type = null;
        
        if(metadata.getPresentAt() != null                                   
                && generated.metadata.BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly())){
            
            if(referenceEditor != null && referenceEditor.getQuestionExport() != null) {
            
                AbstractQuestion question = referenceEditor.getQuestionExport();
                
                if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                        && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                    
                    //summarize content should use the Constructive activity type
                    type = new generated.metadata.ActivityType.Constructive();
                    
                } else {
                    
                    //highlight content should use the Active activity type
                    type = new generated.metadata.ActivityType.Active();
                }   
                
            } else if(referenceEditor != null && referenceEditor.getConversationTree() != null){
                
                // conversation tree should use the Active activity type
                type = new generated.metadata.ActivityType.Active();
                
            } else if(metadata.getContent() instanceof Metadata.TrainingApp){
                
                //training app content should use the Active activity type
                type = new generated.metadata.ActivityType.Interactive();
                
            } else if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Interactive){
                
                //if concept has an interactive activity, preserve it, since the author cannot change it
                type = new generated.metadata.ActivityType.Interactive();
            }
        }
        
        if(type != null){
            concept.getActivityType().setType(type);
        
        } else {
            concept.getActivityType().setType(new generated.metadata.ActivityType.Passive());
        }
    }
	
	/**
	 * Gets whether all of the concepts associated with this metadata's content are using the Interactive activity type
	 * 
	 * @return whether all concepts are interactive
	 */
	private boolean isInteractiveOnlyContent() {
	    
	    if(metadata.getConcepts() == null) {
	        return false;
	    }
	    
	    for(Concept concept : metadata.getConcepts().getConcept()) {
	        
	        if(concept.getActivityType() == null 
	                || !(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Interactive)) {
	            
	            return false;
	        }
	    }
	    
	    return true;
	}
}
