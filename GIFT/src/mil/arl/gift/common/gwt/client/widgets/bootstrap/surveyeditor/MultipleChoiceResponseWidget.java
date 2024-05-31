/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.DoubleBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A widget used to display and edit responses for multiple-choice questions
 * 
 * @author nroberts
 */
public class MultipleChoiceResponseWidget extends Composite {

    private static Logger logger = Logger.getLogger(MultipleChoiceResponseWidget.class.getName());

    private final static String PLACEHOLDER = "Click here to enter text for a new choice!";
    private final static String READ_ONLY_MODE_PLACEHOLDER = "Cannot edit choice's text in read-only mode";
    
	private static MultipleChoiceResponseWidgetUiBinder uiBinder = GWT
			.create(MultipleChoiceResponseWidgetUiBinder.class);

	interface MultipleChoiceResponseWidgetUiBinder extends
			UiBinder<Widget, MultipleChoiceResponseWidget> {
	}
	
	
	@UiField
	protected Widget optionsGroup;
	
	@UiField
	protected ValueEditableLabel label;
	
	@UiField(provided=true)
	protected RadioButton radio;
	
	@UiField
	protected CheckBox check;
	
	@UiField
	protected AnchorListItem moveUpItem;
	
	@UiField
	protected AnchorListItem moveDownItem;
	
	@UiField
	protected AnchorListItem removeChoiceItem;
	
	@UiField
	protected AnchorListItem setFeedbackItem;
	
	@UiField
	protected Tooltip tooltip;
	
	@UiField
	protected DoubleBox pointBox;
	
	@UiField
	protected Button dropDownButton;
	
	@UiField
	protected ButtonGroup pickerGroup;
	
	@UiField
	protected Tooltip pickerTooltip;
	
	@UiField
	protected Button pickerButton;
	
	@UiField
	protected Collapse feedbackPanel;
	
	@UiField
	protected Collapse feedbackPanelInner;
	
	@UiField
	protected Button feedbackButton;
	
	@UiField
	protected TextArea feedbackBox;
	
	@UiField
	protected FlowPanel selectGroup; 
	
	@UiField 
	protected Image warningImage;
	
	@UiField
	protected Tooltip warningMessage;
	
	protected DropDownMenu pickerMenu = new DropDownMenu();
	
	/** A popup panel used to help the picker menu show outside of scrollable containers */
	protected PopupPanel pickerPopup = new PopupPanel(true);
	
	/** Flag to indicate if the label is static (meaning that the user cannot edit it regardless of mode.) */
	private boolean staticLabel = false;
	
	/** Flag to indicate if the response is editable */
	private boolean isReadOnly = false;
	
	/** The weighted point value of the response. */
	Double pointValue = 0.0;
	
	/** Registration for the handler that handles click events when editing a response's label is disabled*/
	private HandlerRegistration labelClickRegistration;
	
	/** The current mode of the widget (writing or scoring mode). */  
	SurveyEditMode editMode = SurveyEditMode.WritingMode;
	
	/**
	 * Creates a new widget for entering responses to multiple choice questions
	 * 
	 * @param radioGroupName the group that this response's radio button should be a part of
	 */
	public MultipleChoiceResponseWidget(String radioGroupName) {
		
		radio = new RadioButton(radioGroupName);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		pointBox.setText(pointValue.toString());
        pointBox.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> e) {
                if(e.getValue() == null) {
                    pointBox.setValue(0.0);
                }
            }
        });
		
		pickerMenu.getElement().getStyle().setDisplay(Display.BLOCK);
		pickerMenu.setPull(Pull.LEFT);
		
		pickerPopup.add(pickerMenu);
		pickerPopup.getElement().getStyle().setProperty("border", "none");
		pickerPopup.getElement().getStyle().setProperty("padding", "0px");
		
		pickerButton.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				pickerMenu.getElement().getStyle().clearProperty("height");
				pickerMenu.getElement().getStyle().clearProperty("overflow-y");
				pickerMenu.getElement().getStyle().clearProperty("width");
				pickerMenu.getElement().getStyle().clearProperty("overflow-x");
				
				pickerPopup.showRelativeTo(pickerButton);				
				
				// reposition the popup so that choice text isn't cutoff at the end of the page
				int left = pickerPopup.getPopupLeft();
				int top = pickerPopup.getPopupTop();
				
				left -= pickerMenu.getOffsetWidth() - 25;	

				int bottom = top + pickerMenu.getOffsetHeight();
				
				// detect if the popup will exceed the browser's width or height and add scroll bars accordingly
				int excessiveHeight = bottom - Window.getClientHeight();
				int excessiveWidth = -left;
				
				if(excessiveHeight > 0){
					
					int scrollHeight = pickerMenu.getOffsetHeight() - excessiveHeight - 2 /* 2px for border*/;
					
					pickerMenu.setHeight(scrollHeight + "px");
					pickerMenu.getElement().getStyle().setProperty("overflow-y", "auto");
					
				}

				if(excessiveWidth > 0){
					
					left = 0;
					
					int scrollWidth = pickerMenu.getOffsetWidth() - excessiveWidth - 2 /* 2px for border*/;
					
					pickerMenu.setWidth(scrollWidth + "px");
					pickerMenu.getElement().getStyle().setProperty("overflow-x", "auto");
					
				}
				
				pickerPopup.setPopupPosition(left, top);
			}
		});
		
		feedbackButton.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(feedbackButton.getIcon().equals(IconType.CHEVRON_CIRCLE_RIGHT)){
					feedbackButton.setIcon(IconType.CHEVRON_CIRCLE_DOWN);
					
				} else {
					feedbackButton.setIcon(IconType.CHEVRON_CIRCLE_RIGHT);
				}
				
				feedbackPanelInner.toggle();
			}
		});
		
		setFeedbackItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(feedbackPanel.isShown()){
					
					feedbackBox.setValue(null);
					setFeedbackItem.setText("Add Feedback");
					
					feedbackPanelInner.hide();
					
				} else {
					setFeedbackItem.setText("Remove Feedback");
					
					feedbackPanelInner.show();
				}
				
				feedbackPanel.toggle();	
			}
		});
		
		radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue() && label.getValueObject() != null) {
					selectScenarioObject(label.getValueObject().getObjectId());
				}
			}
		});
		
		//prevent options from being dragged by mistake
		moveDownItem.getElement().setDraggable("false");
		moveUpItem.getElement().setDraggable("false");
		setFeedbackItem.getElement().setDraggable("false");
		removeChoiceItem.getElement().setDraggable("false");
		
		dropDownButton.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				
				//open the options drop down on mouse down
				optionsGroup.addStyleName("open");
			}
		});
		
		dropDownButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				//prevent the default click handler for the drop down button from executing, since we're using mouse down
				event.preventDefault();
				event.stopPropagation();
			}
		});
	}

	/**
	 * Gets this response's label
	 * 
	 * @return the label
	 */
	public ValueEditableLabel getLabel(){
		return label;
	}
	
	/**
	 * Displays a warning icon with a tooltip beside a response.
	 * 
	 * @param tooltip The tooltip to display
	 */
	public void showWarningIcon(String tooltip) {
		warningImage.setVisible(true);
		warningMessage.setTitle(tooltip);
	}
		
	/** 
	 * Hides the warning icon displayed by the response. 
	 */
	public void hideWarningIcon() {
		warningImage.setVisible(false);
	}
	
	/**
	 * Sets whether or not to allow the user to select multiple responses
	 * 
	 * @param multiselect whether or not to allow the user to select multiple responses
	 */
	public void setAllowMultiselect(boolean multiselect){
		
		if(multiselect){
			radio.setVisible(false);
			check.setVisible(true);
		
		} else {
			radio.setVisible(true);
			check.setVisible(false);
		}
	}
	
	/**
	 * Sets whether or not this choice is temporary and shouldn't be treated like a real choice
	 * 
	 * @param temporary whether or not this choice is temporary
	 */
	public void setTemporary(boolean temporary){
		optionsGroup.setVisible(!temporary);
	}

	/**
	 * Gets the menu item used to move this choice up
	 * 
	 * @return the menu item used to move this choice up
	 */
	public AnchorListItem getMoveUpItem() {
		return moveUpItem;
	}

	/**
	 * Gets the menu item used to move this choice down
	 * 
	 * @return the menu item used to move this choice down
	 */
	public AnchorListItem getMoveDownItem() {
		return moveDownItem;
	}

	/**
	 * Gets the menu item used to remove this choice
	 * 
	 * @return the menu item used to remove this choice
	 */
	public AnchorListItem getRemoveChoiceItem() {
		return removeChoiceItem;
	}
	
	/**
	 * Sets whether or not this response's text label should be editable
	 * 
	 * @param editable whether or not this response's text label should be editable
	 */
	public void setLabelEditable(boolean editable){
		
	    boolean addLabelClickRegistration = false;
	    if (!isLabelStatic()) {
	        label.setEditingEnabled(editable);

    		if(!editable && labelClickRegistration == null){
    			
    		    addLabelClickRegistration = true;
    			
    		} else if(editable && labelClickRegistration != null){
    						
    			//otherwise, handle clicking on the label normally
    			labelClickRegistration.removeHandler();
    			labelClickRegistration = null;
    		}
	    } else {
	        label.setEditingEnabled(false);
	        if(!editable && labelClickRegistration == null){
                
	            addLabelClickRegistration = true;
                
            }
	    }

	    if (editable) {
	        label.setPlaceholder(PLACEHOLDER);
	    } else {
	        label.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
	    }
	    
	    if (addLabelClickRegistration && labelClickRegistration == null) {
	        //if the is response isn't editable, make it so that clicking on the label clicks its accompanying radio button
            labelClickRegistration = label.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    radio.setValue(true);
                }
            });
	    }
	}

	/**
	 * Refreshes the widget based on the current mode (writing mode or scoring mode).
	 * 
	 */
    public void refresh() {
        if (editMode == SurveyEditMode.WritingMode) {
            pointBox.setVisible(false);
            selectGroup.setVisible(true);
            radio.setEnabled(true);
            check.setEnabled(true);
            dropDownButton.setEnabled(true);
            
        } else if (editMode == SurveyEditMode.ScoringMode) {
            pointBox.setVisible(true);
            pointBox.setEnabled(!isReadOnly && StringUtils.isNotBlank(label.getValue()));
            selectGroup.setVisible(false);
            radio.setEnabled(false);
            check.setEnabled(false);
            dropDownButton.setEnabled(false);
        } else {
            logger.severe("Unsupported mode: " + editMode);
        }
        
    }

    /**
     * Sets the mode of the response widget.
     * 
     * @param mode - The mode of the response widget (writing or scoring mode).
     */
    public void setMode(SurveyEditMode mode) {
        editMode = mode;
        
        if(editMode == SurveyEditMode.ScoringMode) {
        	setLabelEditable(false);
        	feedbackBox.setEnabled(false);
        	
        } else {
        	setLabelEditable(true);
        	feedbackBox.setEnabled(true);
        }
    }

    /**
     * Called when the survey editor mode has changed.
     * 
     * @param mode - The mode (writing or scoring) that the survey editor panel is in.
     */
    public void onEditorModeChanged(SurveyEditMode mode) {
        setMode(mode);
        
    }

    /**
     * Returns the weighted point value of the response.
     * 
     * @return Double - The weighted point value of the response.
     */
    public Double getPointValue() {
        return pointValue;
    }
    
    public void setPointValue(Double value){
    	pointValue = value;
    }
    
    public DoubleBox getPointBox(){
    	return pointBox;
    }


    /**
     * Sets if the label for this response is static (non editable).  If the label is static
     * the user is not able to edit it.  This can be used for common responses such
     * as true false questions where the answers to the multiple choice question do not change.
     * 
     * @param staticLabel - True to make the label static (non editable).  False to allow the label to be edited.  Default is false.
     */
    public void setStaticLabel(boolean staticLabel) {
        this.staticLabel = staticLabel;
        
        
        setLabelEditable(!staticLabel);
        
        
        
    }
    
    public boolean isLabelStatic() {
        return this.staticLabel;
    }

	/**
	 * Sets a list of predefined responses that can optionally be picked from
	 * 
	 * @param tooltip a tooltip for the button used to pick a response
	 * @param pickableResponses the list of pickable responses
	 */
	public void setPickableResponses(String tooltip,
			List<PickableObject> pickableResponses) {
		
		pickerMenu.clear();
		
		pickerTooltip.setTitle(tooltip);
		
		if(pickableResponses != null && !pickableResponses.isEmpty()){
			
			for(final PickableObject choice : pickableResponses){
				
				AnchorListItem item = new AnchorListItem(choice.getResponse());
				
				item.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						label.setValue(choice.getResponse(), choice, true);
						label.setEditingEnabled(false);
						setStaticLabel(true);
						label.setTooltipText("The choice text cannot be edited because it was selected from the list of scenario objecs.");
						pickerPopup.hide();
						hideWarningIcon();
					}
				});
				
				item.addDomHandler(new MouseOverHandler() {

					@Override
					public void onMouseOver(MouseOverEvent event) {
						if(choice.getObjectId() != null) {
							selectScenarioObject(choice.getObjectId());
						}
					}

				}, MouseOverEvent.getType());

				pickerMenu.add(item);
			}
			
			pickerGroup.setVisible(true);
			
		} else {
			pickerGroup.setVisible(false);
		}
	}
	
	private native void selectScenarioObject(String itemIdentifier) /*-{
		if($wnd.selectScenarioObject != null) {
			$wnd.selectScenarioObject(itemIdentifier);
		}
	}-*/;
		
	/**
	 * Sets the feedback text for this response
	 * 
	 * @param feedback the feedback text
	 */
	public void setFeedback(String feedback){		
		
		feedbackBox.setValue(feedback);
		
		if(feedback != null){
			setFeedbackItem.setText("Remove Feedback");
			feedbackPanel.show();
			
		} else {
			setFeedbackItem.setText("Add Feedback");
			feedbackPanel.hide();
		}
	}
	
	/**
	 * Gets the feedback text for this response
	 * 
	 * @return the feedback text
	 */
	public String getFeedback(){				
		return feedbackBox.getValue();
	}
	
	/**
	 * Gets the feedback box object for this response
	 * 
	 * @return the feedback text box
	 */
	public TextArea getFeedbackBox(){
		return feedbackBox;
	}
	
	/**
	 * Gets the button from the drop down menu that adds/removes feedback from the response
	 * 
	 * @return the Add/Remove Feedback button
	 */
	public AnchorListItem getAddFeedbackButton(){
		return setFeedbackItem;
	}
	
	/**
	 * Sets whether or not this widget should be read-only
	 * 
	 * @param readOnly whether or not this widget should be read-only
	 */
	public void setReadOnlyMode(boolean readOnly) {
	    this.isReadOnly = readOnly;
	    
		if(readOnly){
			
	        label.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
			feedbackPanelInner.show();
			feedbackButton.setIcon(IconType.CHEVRON_CIRCLE_DOWN);
			
		} else {
			
	        label.setPlaceholder(PLACEHOLDER);
			feedbackPanelInner.hide();
			feedbackButton.setIcon(IconType.CHEVRON_CIRCLE_RIGHT);
		}
		
		dropDownButton.setVisible(!readOnly);
		feedbackBox.setEnabled(!readOnly);
	}
}
