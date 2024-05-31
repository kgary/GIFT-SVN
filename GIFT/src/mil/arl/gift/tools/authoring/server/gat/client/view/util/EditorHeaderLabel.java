/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author nroberts
 *
 */
public class EditorHeaderLabel extends Composite implements HasText{

	private static EditorHeaderLabelUiBinder uiBinder = GWT
			.create(EditorHeaderLabelUiBinder.class);

	interface EditorHeaderLabelUiBinder extends
			UiBinder<Widget, EditorHeaderLabel> {
	}

	@UiField
	protected Image editorIcon;
	
	@UiField
	protected Label editorLabel;
	
	private PopupPanel tooltipPanel = new PopupPanel();
	
	private HTML tooltipHTML = new HTML();
	
	public EditorHeaderLabel() {
		initWidget(uiBinder.createAndBindUi(this));
		
		tooltipPanel.setWidget(tooltipHTML);
		
		tooltipPanel.setWidth("400px");
		
		tooltipPanel.getElement().getStyle().setProperty("color", "white");
		tooltipPanel.getElement().getStyle().setProperty("backgroundColor", "black");
		tooltipPanel.getElement().getStyle().setProperty("borderStyle", "none");
		tooltipPanel.getElement().getStyle().setProperty("borderRadius", "5px");
		tooltipPanel.getElement().getStyle().setProperty("padding", "5px");
		tooltipPanel.getElement().getStyle().setProperty("textShadow", "2px 2px rgba(0,0,0,0.5)");
		tooltipPanel.getElement().getStyle().setProperty("boxShadow", "3px 3px 5px rgba(0,0,0,0.5)");
		
		editorLabel.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				if(tooltipHTML.getHTML() != null && !tooltipHTML.getHTML().isEmpty()){
					tooltipPanel.showRelativeTo(editorLabel);
				}
			}
		});
		
		editorLabel.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				tooltipPanel.hide();
			}
		});
	}

	@Override
	public void setText(String text){
		editorLabel.setText(text);
	}
	
	public void setIconSource(String sourceURL){
		
		Image.prefetch(sourceURL);
		
		editorIcon.setUrl(sourceURL);
	}

	@Override
	public String getText() {		
		return editorLabel.getText();
	}
	
	public void setTooltip(String html){
		tooltipHTML.setHTML(html);
	}
}
