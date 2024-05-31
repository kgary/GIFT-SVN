/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.logging.Logger;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.resources.client.CssResource;

/**
 * A widget for displaying scoring assessments to the user.
 * 
 * @author bzahid
 */
public class CourseEventScoreWidget extends AbstractBsWidget {
    
    private static Logger logger = Logger.getLogger(CourseEventScoreWidget.class.getName());

	private static CourseEventScoreWidgetUiBinder uiBinder = GWT.create(CourseEventScoreWidgetUiBinder.class);
	
	interface CourseEventScoreWidgetUiBinder extends UiBinder<Widget, CourseEventScoreWidget>{
	}
	
	/** Interface that allows access to the UI style names */
	interface ScoreStyles extends CssResource {
	    String childPanel();
		String buttonLabel();
		String passButton();
		String failButton();
		String passLabel();
		String failLabel();
		String growPanel();
	  }
	
	@UiField
	protected static ScoreStyles style;
	
	@UiField
	protected FlowPanel hierarchyPanel;
	
	private AbstractScoreNode scoreNode;
	
    /**
     * Initializes the scoring widget.
     * 
     * @param scoreNode The root score node.
     */
	public CourseEventScoreWidget(AbstractScoreNode scoreNode) {
		initWidget(uiBinder.createAndBindUi(this));	
		this.scoreNode = scoreNode;
		hierarchyPanel.add(createHierarchy(scoreNode));
	}
	
	
	/**
	 * Creates a hierarchy view of the score nodes.
	 * 
	 * @param scoreNode The score node root.
	 * @return A widget containing the hierarchy of score nodes.
	 */
	private FlowPanel createHierarchy(AbstractScoreNode scoreNode) {
		
		FlowPanel container = new FlowPanel();
		HorizontalPanel scoreNamePanel = new HorizontalPanel();
		HTML name = new HTML();
		name.addStyleName(style.buttonLabel());
		name.setText(scoreNode.getName());
		scoreNamePanel.getElement().getStyle().setProperty("marginBottom", "13px");
		
		if(scoreNode instanceof GradedScoreNode) {
		    
		    logger.info("creating hierarchy view for graded score node of:\n"+scoreNode);
			
			GradedScoreNode gNode = (GradedScoreNode) scoreNode;
			
			if(gNode.getChildren().isEmpty()) {
				name.setText(scoreNode.getName() + ": " + gNode.getGradeAsString());
				scoreNamePanel.add(name);
				
			} else {
				final FlowPanel growPanel = new FlowPanel();
				final FlowPanel childrenPanel = new FlowPanel();
				final Button expandButton = new Button();
				
				if(gNode.getAssessment().hasReachedStandards()) {
					expandButton.setIcon(IconType.CHECK);
					expandButton.setType(ButtonType.SUCCESS);
					expandButton.addStyleName(style.passButton());
					growPanel.setHeight("0px");
					growPanel.addStyleName(style.passLabel());
					name.addStyleName(style.passLabel());
				} else {
					expandButton.setIcon(IconType.TIMES);
					expandButton.setType(ButtonType.DANGER);
					expandButton.addStyleName(style.failButton());
					growPanel.setHeight("100%");
					growPanel.addStyleName(style.failLabel());
					name.addStyleName(style.failLabel());
				}
				
				for(AbstractScoreNode node : gNode.getChildren()) {
					childrenPanel.add(createHierarchy(node));
				}
				
				growPanel.add(childrenPanel);
				growPanel.addStyleName(style.childPanel());
				growPanel.addStyleName(style.growPanel());
				
				expandButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						String height = growPanel.getElement().getStyle().getHeight();
						if(height.equals("0px")) {
							growPanel.setHeight("100%");
						} else {
							growPanel.setHeight("0px");
						}
					}
					
				});
				
				FocusPanel focusPanel = new FocusPanel();
				focusPanel.add(name);
				scoreNamePanel.add(expandButton);
				scoreNamePanel.add(focusPanel);
				container.add(scoreNamePanel);
				container.add(growPanel);
				
				focusPanel.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent arg0) {
						expandButton.click();
					}
					
				});
			}
			
		} else if(scoreNode instanceof RawScoreNode) {
		    
            logger.info("creating hierarchy view for raw score node of:\n"+scoreNode);
			            
			StringBuilder sb = new StringBuilder();
			RawScoreNode rNode = (RawScoreNode) scoreNode;
			
			sb.append("- ");
			sb.append(name.getHTML());
			sb.append(": ");			
			sb.append("<span style=\"font-weight: normal; font-size: 14px;");
			sb.append("\">");
			sb.append(rNode.getAssessment().getDisplayName());
			sb.append(" - ");
			sb.append(rNode.getRawScore().toDisplayString());
			sb.append("</span>");
			
			name.setHTML(sb.toString());
			scoreNamePanel.add(name);
			container.add(scoreNamePanel);
			
			if(CollectionUtils.isNotEmpty(rNode.getUsernames())){               
                 
                HorizontalPanel whoPanel = new HorizontalPanel();
                whoPanel.getElement().getStyle().setMarginLeft(25, Unit.PX);
                whoPanel.getElement().getStyle().setProperty("marginBottom", "13px");
                
                HTML whoHtml = new HTML();
                whoHtml.addStyleName(style.buttonLabel());
                whoHtml.setText("Who: ");
                
                StringBuilder whoPanelSB = new StringBuilder();
                whoPanelSB.append(whoHtml.getHTML());
                whoPanelSB.append(StringUtils.join(", ", rNode.getUsernames()));
                
                whoHtml.setHTML(whoPanelSB.toString());                
                whoPanel.add(whoHtml);
                container.add(whoPanel);
			}


			name.getElement().getStyle().setProperty("cursor", "default");
			
		}
		
		return container;
	}
	
	/**
	 * Creates a print-friendly view of the hierarchy and returns the inner html
	 * 
	 * @return the inner html of the print-friendly score node hierarchy
	 */
	public String getPrintableScoreDetails() {
		return createPrintFriendlyHierarchy(scoreNode).getElement().getInnerHTML();
	}
	
	/**
	 * Creates a print-friendly view of the score node hierarchy.
	 * 
	 * @param scoreNode The score node root.
	 * @return A widget containing the hierarchy of score nodes.
	 */
	private FlowPanel createPrintFriendlyHierarchy(AbstractScoreNode scoreNode) {
		
		FlowPanel container = new FlowPanel();
		HorizontalPanel scoreNamePanel = new HorizontalPanel();
		HTML name = new HTML();
		name.setText(scoreNode.getName());
		scoreNamePanel.getElement().getStyle().setProperty("marginBottom", "13px");
		
		if(scoreNode instanceof GradedScoreNode) {
			
			GradedScoreNode gNode = (GradedScoreNode) scoreNode;
			
			if(gNode.getChildren().isEmpty()) {
				name.setText(scoreNode.getName() + ": " + gNode.getGradeAsString());
				scoreNamePanel.add(name);
				
			} else {
				final FlowPanel growPanel = new FlowPanel();
				final FlowPanel childrenPanel = new FlowPanel();
				StringBuilder sb = new StringBuilder();
				
				sb.append("<span style=\"color: ");
				if(gNode.getAssessment().hasReachedStandards()) {
					sb.append("green;\">");
				} else {
					sb.append("red\">");
				}
				sb.append(gNode.getGradeAsString());
				sb.append(": ");
				sb.append("</span>");
				sb.append(scoreNode.getName());
				name.setHTML(sb.toString());
				
				for(AbstractScoreNode node : gNode.getChildren()) {
					childrenPanel.add(createPrintFriendlyHierarchy(node));
				}
				
				growPanel.getElement().getStyle().setProperty("margin-left", "15px");
				growPanel.add(childrenPanel);
				scoreNamePanel.add(name);
				container.add(scoreNamePanel);
				container.add(growPanel);
				
			}
			
		} else if(scoreNode instanceof RawScoreNode) {
			
			StringBuilder sb = new StringBuilder();
			RawScoreNode rNode = (RawScoreNode) scoreNode;
			
			sb.append(name.getHTML());
			sb.append(": ");
			sb.append(rNode.getAssessment().getDisplayName());
			sb.append(" - ");
			sb.append(rNode.getRawScore().toDisplayString());
			
			name.setHTML(sb.toString());
			scoreNamePanel.add(name);
			container.add(scoreNamePanel);
			
			scoreNamePanel.getElement().getStyle().setProperty("margin-left", "10px");
			if(rNode.getAssessment().compareTo(AssessmentLevelEnum.BELOW_EXPECTATION) == 0) {
				name.getElement().getStyle().setProperty("color", "red");
			} else {
				name.getElement().getStyle().setProperty("color", "green");
			}
		}
		
		return container;
	}
	
}

