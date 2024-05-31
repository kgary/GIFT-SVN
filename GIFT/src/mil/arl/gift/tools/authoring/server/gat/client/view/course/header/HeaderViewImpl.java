/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

import generated.course.BooleanEnum;
import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import generated.course.LtiProvider;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.consumer.LtiConsumerPropertiesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.provider.LtiProviderPropertiesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;


/**
 * The Class CourseHeaderEditor.
 */
public class HeaderViewImpl extends ScenarioValidationComposite implements HeaderView{
	
    /** The logger. */
    private static Logger logger = Logger.getLogger(HeaderViewImpl.class.getName());
            
	/**
	 * The Interface CourseHeaderEditorUiBinder.
	 */
	interface HeaderViewImplUiBinder extends
	UiBinder<Widget, HeaderViewImpl> {
	}	

	/** The ui binder. */
	private static HeaderViewImplUiBinder uiBinder = GWT
			.create(HeaderViewImplUiBinder.class);	
	 
	public ConceptNode node;
	
	/** The name. */
	protected @UiField TextBox name;
	
	/** The description. */
	protected @UiField TextArea description;
   
	
    @UiField protected CourseObjectModal configurationDialog;
    
    @UiField protected HTMLPanel configurationProperties;
    
    @UiField protected HTMLPanel courseProperties;
        
    @UiField protected ConceptHierarchyPanel conceptsPanel;
    
    @UiField protected FlowPanel propertiesConceptList;
    
    
    /**
     * Course description
     */
    
    @UiField protected HTML descriptionLabel;
    
    @UiField protected HTML emptyDescriptionLabel;
    
    @UiField protected DeckPanel descriptionDeckPanel;
    
    @UiField protected Collapse descriptionCollapse;
    
    @UiField protected FocusPanel descriptionPanel;
    
    @UiField protected Tooltip descriptionTooltip;
    
    @UiField protected HTMLPanel descriptionTextPanel;
    
    @UiField protected Button editDescriptionButton;
    
    /**
     * course concepts
     */
    
    @UiField protected FocusPanel conceptsPropertiesPanel;
    
    @UiField protected Collapse conceptsCollapse;    

    @UiField protected DeckPanel propertiesConceptDeckPanel;
    
    @UiField protected Tooltip conceptsTooltip;
    
    @UiField protected HTMLPanel conceptsTextPanel;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button editConceptsButton;

    
    /**
     * LTI Consumer/Provider
     */
    
    @UiField protected HTMLPanel ltiProvidersPanel;
        
    @UiField(provided=true)
    protected Image addLtiProviderListButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    @UiField protected DeckPanel ltiProviderListDeck;
    
    @UiField protected Widget emptyLtiProviderListPanel;
    
    @UiField public CellTable<LtiProvider> ltiProviderListDataGrid;
    
    @UiField protected LtiConsumerPropertiesPanel ltiConsumerProperties;
    
    @UiField protected LtiProviderPropertiesPanel ltiProviderProperties;
    
    /**
     * Course Image
     */    
    
    @UiField protected Image courseTileImage;
    
    @UiField protected Tooltip courseImageTooltip;
    
    @UiField protected FocusPanel coverImagePanel;
    
    @UiField protected Collapse coverImageCollapse;
    
    @UiField protected Tooltip coverImageTooltip;
    
    @UiField protected HTMLPanel coverImageTextPanel;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button editCoverImageButton;
    
    @UiField protected Widget editCoverImageContainer;
    
    /**
     * course history
     */
    
    @UiField protected FocusPanel historyPropertiesPanel;
    
    @UiField protected Collapse historyCollapse;
    
    @UiField protected DeckPanel propertiesHistoryDeckPanel;
    
    @UiField protected Label lastSuccessfulValidationLabel;
    
    @UiField protected Label lastModifiedLabel;
    
    @UiField protected Label surveyContextLastModifiedLabel;
    
    @UiField protected Widget refreshCourseHistoryContainer;
    
    @UiField protected Tooltip historyPropertiesTooltip;
    
    @UiField protected HTMLPanel historyPropertiesTextPanel;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button refreshCourseHistoryButton;
    
    /**
     * course configuration files
     */    
    
    @UiField protected Tooltip learnerTooltip;
    
    @UiField protected Tooltip replaceLearnerTooltip;
    
    @UiField protected Tooltip replacePedTooltip;
    
    @UiField protected Tooltip pedTooltip;
    
    @UiField protected FocusPanel sensorPanel;
    
    @UiField protected FocusPanel learnerPanel;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button editLearnerButton;
    
    @UiField protected Widget editLearnerContainer;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button editPedButton;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button replacePedButton;
    
    @UiField protected org.gwtbootstrap3.client.ui.Button replaceLearnerButton;
    
    @UiField protected Widget editPedContainer;
    
    @UiField protected Widget replacePedContainer;
    
    private final static String READONLY_TOOLTIP = "Cannot edit due to read-only mode.";
    
    /** String value shown to the user when the LTI data needs to be hidden */
    private final static String PROTECTED_LTI_DATA = "**protected**";
    

    
    
    @SuppressWarnings("unused")
    private boolean readOnly = false;
    
    /** The name column for the concept list */
    private Column<Concepts.List.Concept, String> conceptListNameColumn;
	
    /** The ID column for the LTI providers list */
    private Column<LtiProvider, String> ltiProviderListIdColumn = new Column<LtiProvider, String>(new TextCell()) {
        @Override
        public String getValue(LtiProvider provider) {
            return provider.getIdentifier();
        }
    };

    /** The key column for the LTI providers list */
    private Column<LtiProvider, String> ltiProviderListKeyColumn = new Column<LtiProvider, String>(new TextCell()) {
        @Override
        public String getValue(LtiProvider provider) {
            if (GatClientUtility.isReadOnly() && BooleanEnum.TRUE.equals(provider.getProtectClientData())) {
                return PROTECTED_LTI_DATA;
            } else {
                return provider.getKey();
            }
        }
    };

    /** The shared secret column for the LTI providers list */
    private Column<LtiProvider, String> ltiProviderListSecretColumn = new Column<LtiProvider, String>(new TextCell()) {
        @Override
        public String getValue(LtiProvider provider) {
            if (GatClientUtility.isReadOnly() && BooleanEnum.TRUE.equals(provider.getProtectClientData())) {
                return PROTECTED_LTI_DATA;
            } else {
                return provider.getSharedSecret();
            }
        }
    };
    
    /** The column containing the edit button for the LTI provider */
    private Column<LtiProvider, String> ltiProviderListEditColumn = new Column<LtiProvider, String>(new ButtonCell(){
        
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, 
                    String value, SafeHtmlBuilder sb) {
                
                Image image = new Image(value);
                image.setTitle("Edit this LTI provider");
                
                SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(html);
            }
        }){
        
        @Override
        public String getValue(LtiProvider record) {
                                
            return GatClientBundle.INSTANCE.edit_image().getSafeUri().asString();
        }
    };
    
    /** The column containing the delete button for the LTI provider */
    private Column<LtiProvider, String> ltiProviderListRemoveColumn = new Column<LtiProvider, String>(new ButtonCell(){
        
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, 
                String value, SafeHtmlBuilder sb) {
            
            Image image = new Image(value);
            image.setTitle("Remove this LTI provider");
            
            SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
            sb.append(html);
        }
    }){
    
    @Override
    public String getValue(LtiProvider record) {
                            
        return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
    }};
    
    /** The protect data column for the LTI providers list */
    private Column<LtiProvider, Boolean> ltiProviderListProtectClientDataColumn = new Column<LtiProvider, Boolean>(new CheckboxCell(true, true)) {
        @Override
        public Boolean getValue(LtiProvider record) {
            return BooleanEnum.TRUE.equals(record.getProtectClientData());
        }
    };
    
    private TextCell conceptListNameCell = new TextCell();
    	
	@UiField
	protected CheckBox excludeCheckBox;
        
	/** The model used by the concept hierarchy tree */
	protected ConceptNodeTreeModel simpleConceptHierarchyModel = new ConceptNodeTreeModel(false);
	
	@UiField(provided = true)
	protected CellTree propertiesConceptTree = new CellTree(simpleConceptHierarchyModel, null, 
			(CellTree.BasicResources) GWT.create(CellTree.BasicResources.class), new CellTree.CellTreeMessages(){

		@Override
		public String emptyTree() {
			return "Add course concepts to track a learner's assessment across courses.";
		}

		@Override
		public String showMore() {
			return "Show More";
		}
		
	});
	
	@UiField
	protected CellTable<Concepts.List.Concept> propertiesConceptListDataGrid;
	
	protected DefaultGatFileSelectionDialog sensorFileDialog = new DefaultGatFileSelectionDialog();
	
	@UiField
	protected Button courseImageFileBrowseButton;
	
	protected DefaultGatFileSelectionDialog learnerFileDialog = new DefaultGatFileSelectionDialog();
	
	protected DefaultGatFileSelectionDialog courseImageFileDialog = new DefaultGatFileSelectionDialog(); 
	
	@UiField
	protected Label courseImageFileNameLabel;
	
	@UiField(provided=true)
	protected Image clearImageButton = new Image(GatClientBundle.INSTANCE.cancel_image());
	
	@UiField(provided=true)
	protected Icon previewTileIcon = new Icon(IconType.EYE);
	
	protected DefaultGatFileSelectionDialog pedagogicalFileDialog = new DefaultGatFileSelectionDialog();
	
	@UiField
	protected HTMLPanel propertiesEmptyConceptPanel;
	
	
	/**
	 * Instantiates a new course header editor.
	 */
	public HeaderViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));

		 
        //Add the name column to the data grid.
		conceptListNameColumn = new Column<Concepts.List.Concept, String>(conceptListNameCell) {
			@Override
			public String getValue(Concepts.List.Concept arg0) {
				return arg0.getName();
			}
        };
       
        conceptListNameColumn.setSortable(true);
      
        ltiProviderListDataGrid.addColumn(ltiProviderListIdColumn, "Identifier");
        ltiProviderListDataGrid.setColumnWidth(ltiProviderListIdColumn, "20%");
        ltiProviderListDataGrid.addColumn(ltiProviderListKeyColumn, "Client Key");
        ltiProviderListDataGrid.setColumnWidth(ltiProviderListKeyColumn, "25%");
        ltiProviderListDataGrid.addColumn(ltiProviderListSecretColumn, "Client Shared Secret");
        ltiProviderListDataGrid.setColumnWidth(ltiProviderListSecretColumn, "25%");
        ltiProviderListDataGrid.addColumn(ltiProviderListProtectClientDataColumn, "Protect Client Data");
        ltiProviderListDataGrid.setColumnWidth(ltiProviderListProtectClientDataColumn, "20%");
        ltiProviderListDataGrid.addColumn(ltiProviderListEditColumn);
        ltiProviderListDataGrid.addColumn(ltiProviderListRemoveColumn);
        
        ltiProviderListDataGrid.setPageSize(Integer.MAX_VALUE);
        
        ltiProviderListDataGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        ltiProviderListDataGrid.setEmptyTableWidget(new HTML("<span style='font-size: 12pt;'>No LTI providers have been added.</span>"));
		
        propertiesConceptListDataGrid.addColumn(conceptListNameColumn);
        propertiesConceptListDataGrid.setColumnWidth(conceptListNameColumn, "100%");
        propertiesConceptListDataGrid.setPageSize(Integer.MAX_VALUE);
        propertiesConceptListDataGrid.setEmptyTableWidget(new HTML("Add course concepts to track a learner's assessment across courses."));
      
		propertiesConceptDeckPanel.getWidget(0);
		propertiesHistoryDeckPanel.showWidget(0);
		
		courseImageFileDialog.setIntroMessageHTML("<html><font size=\"3\"><b>This image will be used as the course tile image on the GIFT Dashboard. <br><br>Some recommendations include: <ul>"
				+ "<li>Supported file types: .jpg, .jpeg, .gif and .png.</li>"
				+ "<li>Image will be scaled to a 140x310 pixel tile, so a similar aspect ratio is advised.</li>"
				+ "<li>File size should not exceed 1 MB.</li>"
				+ "<li>Use smaller file sizes for faster loading of the image.</li></ul></font></b></html>");
		
		courseImageFileBrowseButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				courseImageFileDialog.center();
			}
		});
		
		sensorFileDialog.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION});
		learnerFileDialog.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION});
		courseImageFileDialog.getFileSelector().setAllowedFileExtensions(new String[]{".gif", ".jpg", ".jpeg", ".png", ".GIF", ".JPG", ".JPEG", ".PNG"});
		pedagogicalFileDialog.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION});
		
		ltiProviderListDeck.showWidget(ltiProviderListDeck.getWidgetIndex(emptyLtiProviderListPanel));
				
		courseTileImage.setResource(GatClientBundle.INSTANCE.course_default());
		courseTileImage.setWidth("310px");
		courseTileImage.setHeight("140px");
	
		descriptionDeckPanel.showWidget(0);
		getDescriptionInput().addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				int index;
				String text = event.getValue() ;
				descriptionLabel.setHTML(text);
								
				if (text == null || text.trim().isEmpty()) {
					index = descriptionDeckPanel.getWidgetIndex(emptyDescriptionLabel);
				} else {
					index = descriptionDeckPanel.getWidgetIndex(descriptionLabel);
				}
    
				descriptionDeckPanel.showWidget(index);
			}
			
		});		
		
		descriptionPanel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				//show/hide the course description
				descriptionCollapse.toggle();
			}
		});
		
		descriptionTextPanel.addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                descriptionTooltip.show();
            }
        }, MouseOverEvent.getType());
		
		descriptionTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                descriptionTooltip.hide();
            }
		    
		}, MouseOutEvent.getType());
		
		editDescriptionButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
				event.stopPropagation();
			}
		});
		
		editDescriptionButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
		});
		
		editDescriptionButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        });
         
      
		conceptsPropertiesPanel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				//show/hide the course contents
				conceptsCollapse.toggle();
			}
		});		
		
		conceptsTextPanel.addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                conceptsTooltip.show();
            }
        }, MouseOverEvent.getType());
        
        conceptsTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                conceptsTooltip.hide();
            }
            
        }, MouseOutEvent.getType());
        
        editConceptsButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        });
        
        editConceptsButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        });
        
        editConceptsButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        });
		
		coverImagePanel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				//show/hide the course cover image
				coverImageCollapse.toggle();
			}
		});	
		
		coverImageTextPanel.addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                coverImageTooltip.show();
            }
        }, MouseOverEvent.getType());
        
        coverImageTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                coverImageTooltip.hide();
            }
            
        }, MouseOutEvent.getType());
		
        historyPropertiesPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //show/hide the course history
                historyCollapse.toggle();
            }
        });
        
        historyPropertiesTextPanel.addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                historyPropertiesTooltip.show();
            }
        }, MouseOverEvent.getType());
        
        historyPropertiesTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                historyPropertiesTooltip.hide();
            }
            
        }, MouseOutEvent.getType());
		
		editCoverImageContainer.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
				event.stopPropagation();
			}
		}, ClickEvent.getType());
		
		editCoverImageContainer.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
		}, MouseOutEvent.getType());
		
		editCoverImageContainer.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        }, MouseOverEvent.getType());
		
        refreshCourseHistoryContainer.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        }, ClickEvent.getType());
        
        refreshCourseHistoryContainer.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        }, MouseOutEvent.getType());
		
        refreshCourseHistoryContainer.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
            }
        }, MouseOverEvent.getType());
		
		editLearnerContainer.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				//prevent mouse events from registering up the hierarchy
				event.stopPropagation();
			}
		}, ClickEvent.getType());
		
		editPedContainer.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
				event.stopPropagation();
			}
		}, ClickEvent.getType());
		
		replacePedContainer.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                
                //prevent mouse events from registering up the hierarchy
				event.stopPropagation();
			}
		}, ClickEvent.getType());
	}
    
    @Override
    public HasData<LtiProvider> getLtiProviderList() {
        return ltiProviderListDataGrid;
    }
    
	@Override
	public HasData<Concepts.List.Concept> getSimpleConceptList() {
    	return propertiesConceptListDataGrid;
    }
    
	@Override
	public HasValue<String> getNameInput() {
		return this.name;
	}

	@Override
	public HasValue<String> getDescriptionInput() {
		return description;
	}
	
	@Override
	public void setAddDescriptionHandler(ClickHandler descriptionHandler) {
		editDescriptionButton.addClickHandler(descriptionHandler);
	}

	@Override
    public HasClickHandlers getAddLtiProviderListButton() {
        return addLtiProviderListButton;
    }

	@Override
	public void setSortHandlerForConceptList(ListHandler<Concept> sortHandler) {
		Comparator<Concepts.List.Concept> nameComparator = new Comparator<Concepts.List.Concept>() {
			@Override
			public int compare(Concept arg0, Concept arg1) {
				if(arg0 == arg1) {
					return 0;
				}
				
				if(arg0 != null && arg0.getName() != null) {
					return (arg1 != null && arg1.getName() != null) ? arg0.getName().compareToIgnoreCase(arg1.getName()) : 1;
				}
				return -1;
			}			
		};
		sortHandler.setComparator(conceptListNameColumn, nameComparator);
	}

	@Override
	public HashMap<ConceptNode, ListDataProvider<ConceptNode>> getPropertiesHierarchyDataProviders() {
		return simpleConceptHierarchyModel.getDataProviders();
	}
	
	@Override
	public TreeNode getSimpleRootConceptTreeNode(){
		return propertiesConceptTree.getRootTreeNode();
	}
	
	@Override
	public void setNameLimit(int limit) {
		name.setMaxLength(limit);
	}
	
	@Override 
	public void setPropertiesConceptsClickHandler(ClickHandler clickHandler) {
		editConceptsButton.addDomHandler(clickHandler, ClickEvent.getType());  
	}
	
	@Override 
    public void setLtiConsumerClickHandler(ClickHandler clickHandler) {
        ltiConsumerProperties.setEditProvidersButtonClickHandler(clickHandler);
    }
	
	@Override 
	public HasValue<String> getSensorFileDialog(){
		return sensorFileDialog;
	}
	
	@Override
	public org.gwtbootstrap3.client.ui.Button getLearnerFileButton() {
		return replaceLearnerButton;
	}
	
	@Override
	public org.gwtbootstrap3.client.ui.Button getLearnerEditButton() {
		return editLearnerButton;
	}
	
	@Override
	public org.gwtbootstrap3.client.ui.Button getPedFileButton() {
		return replacePedButton;
	}
	
	@Override
	public org.gwtbootstrap3.client.ui.Button getPedEditButton() {
		return editPedButton;
	}
	
	@Override
	public void showLearnerSelectDialog() {
		learnerFileDialog.center();
	}
	
	@Override
	public void showPedSelectDialog() {
		pedagogicalFileDialog.center();
	}
	
	@Override
	public void showSensorSelectDialog() {
		sensorFileDialog.center();
	}
	
	@Override
	public Widget getConfigurationsWidget() {
		configurationProperties.setVisible(true);
		return configurationProperties;
	}
	
	@Override
	public Widget getCoursePropertiesWidget() {
		courseProperties.setVisible(true);
		return courseProperties;
	}
	
	@Override
    public LtiConsumerPropertiesPanel getLTIConsumerPropertiesPanel() {
        return ltiConsumerProperties;
    }
	
	@Override
    public LtiProviderPropertiesPanel getLTIProviderPropertiesPanel() {
        return ltiProviderProperties;
    }
		
	@Override
	public void showLearnerConfigEditor(final String coursePath, String url) {
		
		if(url == null) {
			url = GatClientUtility.createModalDialogUrl(coursePath, "Learner", AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
		} else {
			url =  GatClientUtility.getModalDialogUrl(coursePath, url);
		}
		
		final String modalUrl = url;
		configurationDialog.setCourseObjectUrl(CourseObjectName.LEARNER_CONFIG.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), modalUrl);
		configurationDialog.setAdditionalButton("Add Interpreter", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				GatClientUtility.additionalButtonAction();
			}
			
		});
		
		configurationDialog.setSaveAndCloseButtonVisible(!GatClientUtility.isReadOnly());
		configurationDialog.setSaveButtonHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
			    if (!GatClientUtility.isReadOnly()) {
			        GatClientUtility.saveEmbeddedCourseObject();
	                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, modalUrl, AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
	                learnerFileDialog.setValue(filename, true);
	                configurationDialog.stopEditor();
	                
	                if(!GatClientUtility.isReadOnly()){
                        GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this learner config being edited
                    }
			    }
				
			}
			
		});
		configurationDialog.show();
		
	}
	
	@Override
	public void showSensorConfigEditor(final String coursePath, String url) {
		
		if(url == null) {
			url = GatClientUtility.createModalDialogUrl(coursePath, "Sensor", AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
		} else {
			url = GatClientUtility.getModalDialogUrl(coursePath, url);
		}
		
		final String modalUrl = url;
		configurationDialog.setCourseObjectUrl(CourseObjectName.SENSOR_CONFIG.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), modalUrl);
		configurationDialog.setAdditionalButton("Add Sensor", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				GatClientUtility.additionalButtonAction();
			}
			
		});
		
		configurationDialog.setSaveAndCloseButtonVisible(!GatClientUtility.isReadOnly());
		configurationDialog.setSaveButtonHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
			    if (!GatClientUtility.isReadOnly()) {
			        GatClientUtility.saveEmbeddedCourseObject();
	                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, modalUrl, AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
	                sensorFileDialog.setValue(filename, true);
	                
	                GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this sensor config being edited
			    }
				
			}
			
		});
		configurationDialog.setCancelCallback(null);
		configurationDialog.show();
		
	}
	
	@Override
	public void showPedConfigEditor(final String coursePath, String url) {
		
	    if(url == null) {
            url = GatClientUtility.createModalDialogUrl(coursePath, "Ped", AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION);            
        } else {
            url =  GatClientUtility.getModalDialogUrl(coursePath, url);
        }
		
		final String modalUrl = url;
		configurationDialog.setCourseObjectUrl(CourseObjectName.PED_CONFIG.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), modalUrl);
		configurationDialog.setAdditionalButton("Add Pedagogical Data", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				GatClientUtility.additionalButtonAction();
			}
			
		});
		
		configurationDialog.setSaveAndCloseButtonVisible(!GatClientUtility.isReadOnly());
		configurationDialog.setSaveButtonHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
			    if (!GatClientUtility.isReadOnly()) {
			        GatClientUtility.saveEmbeddedCourseObject();
	                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, modalUrl, AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION);
	                pedagogicalFileDialog.setValue(filename, true);
	                configurationDialog.stopEditor();
	                
	                GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this ped config being edited
			    }
				
			}
			
		});
		
		configurationDialog.show();
		
	}
	
	@Override 
	public HasValue<String> getLearnerFileDialog(){
		return learnerFileDialog;
	}
	
	@Override 
	public HasValue<String> getPedagogicalFileDialog(){
		return pedagogicalFileDialog;
	}

	@Override
	public HasValue<Boolean> getExcludeCheckBox(){
		return excludeCheckBox;
	}

	@Override
    public Column<LtiProvider, Boolean> getLtiProviderListProtectClientDataColumn(){
        return ltiProviderListProtectClientDataColumn;   
    }
	
	@Override
    public Column<LtiProvider, String> getLtiProviderListEditColumn(){
        return ltiProviderListEditColumn;   
    }
    
    @Override
    public Column<LtiProvider, String> getLtiProviderListRemoveColumn(){
        return ltiProviderListRemoveColumn; 
    }
	
	@Override
	public void showConceptHierarchyEmptyPanel(boolean show){
		
		if(show){
			propertiesConceptDeckPanel.showWidget(propertiesConceptDeckPanel.getWidgetIndex(propertiesEmptyConceptPanel)); 
			
			
		} else {
			propertiesConceptDeckPanel.showWidget(propertiesConceptDeckPanel.getWidgetIndex(propertiesConceptTree));
			 
		}
		
	}

	@Override
    public HandlerRegistration addEmptyLtiProvidersListPanelClickHandler(ClickHandler handler){
        return emptyLtiProviderListPanel.addDomHandler(handler, ClickEvent.getType());
    }
	
	@Override
	public ConceptHierarchyPanel getConceptsPanel() {
		return conceptsPanel;
	}
	
	@Override
    public Widget getLtiProvidersPanel() {
        return ltiProvidersPanel;
    }
	
	@Override
    public void showLtiProvidersListEmptyPanel(boolean show){
        
        if(show){
            ltiProviderListDeck.showWidget(ltiProviderListDeck.getWidgetIndex(emptyLtiProviderListPanel));
        } else {
            ltiProviderListDeck.showWidget(ltiProviderListDeck.getWidgetIndex(ltiProviderListDataGrid));
        }
    }

	@Override
	public HasValue<String> getCourseImageFileDialog() {
		return courseImageFileDialog;
	}
	
	@Override
	public void showCourseImageFileDialog() {
		courseImageFileDialog.center();
	}

	@Override
	public HasText getCourseImageNameLabel() {
		return courseImageFileNameLabel;
	}
	
    @Override
    public HasText getLastSuccessfulValidationLabel() {
        return lastSuccessfulValidationLabel;
    }
    
    @Override
    public HasText getLastModifiedLabel() {
        return lastModifiedLabel;
    }
    
    @Override
    public HasText getSurveyContextLastModifiedLabel() {
        return surveyContextLastModifiedLabel;
    }
	
	@Override
	public Image getClearImageButton() {
		return clearImageButton;
	}
	
	@Override
	public void setClearImageButtonVisible(boolean visible){
		clearImageButton.setVisible(visible);
	}
	
	@Override
	public void setPreviewTileIconVisible(boolean visible){
		previewTileIcon.setVisible(visible);
	}
	
	@Override
	public Icon getPreviewTileIcon(){
		return previewTileIcon;
	}
	
	@Override
	public Image getCourseTileImage() {
		return courseTileImage;
	}
	
	@Override
	public HasClickHandlers getCourseTileImageButton() {
		return editCoverImageButton;
	}
	
    @Override
    public HasClickHandlers getRefreshCourseHistoryButton() {
        return refreshCourseHistoryButton;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        logger.info("HeaderViewImpl::setReadOnly() readOnly = " + readOnly);
        this.readOnly = readOnly;
    }

    @Override
    public void setPedTooltipVisibility(boolean visible) {
        logger.info("HeaderViewImpl::setPedTooltipVisibility visible=" + visible );
        
        if (visible) {
            pedTooltip.setTitle("Preview Pedagogical Configuration");
        } else {
            pedTooltip.setTitle("Edit Pedagogical Configuration");
        }
        
        replacePedButton.setEnabled(!visible);
        replacePedButton.setVisible(!visible);
    }

    @Override
    public void setCourseImageTooltipVisibility(boolean visible) {
        logger.info("HeaderViewImpl::setCourseImageTooltipVisibility visible=" + visible );
        
        editCoverImageButton.setEnabled(!visible);
        
        if (visible) {
            courseImageTooltip.setTitle(READONLY_TOOLTIP);
        } else {
            courseImageTooltip.setTitle("Change Image");
        }
    }

    @Override
    public void setLearnerTooltipVisibility(boolean visible) {
        logger.info("HeaderViewImpl::setLearnerTooltipVisibility visible=" + visible );
        
        if (visible) {
            learnerTooltip.setTitle("Preview Learner Configuration");
        } else {
            learnerTooltip.setTitle("Edit Learner Configuration");
        }
        
        replaceLearnerButton.setEnabled(!visible);
        replaceLearnerButton.setVisible(!visible);
        
    }
    @Override
    public void setConceptsList(List<Concepts.List.Concept> list){
        
        //Copy the list of concepts into the editor. This allows us to leave the concept list set to null until a concept is added
       List<Concepts.List.Concept> renderedConcepts = new ArrayList<Concepts.List.Concept>();
        
        if(list != null) {
            renderedConcepts.addAll(list);
        } 
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(conceptsPanel);
    }
    
    @Override
    public void populatePropertiesHierarchy(ConceptNode root) {
        simpleConceptHierarchyModel.setRoot(root);
        refreshNode(propertiesConceptTree.getRootTreeNode());
    }
    
    /**
     * Refreshes the given node to match its underlying data
     * 
     * @param node the node to refresh. Cannot be null.
     */
    private void refreshNode(TreeNode node) {
        int childCount = node.getChildCount();
        for(int i = 0; i < childCount; i++) {
            if(node.isChildOpen(i)) {
                
                //nodes are only visually updated when they are opened, so reopen this node
                node.setChildOpen(i, false);               
                refreshNode(node.setChildOpen(i, true));
            }
        }
    }

}
