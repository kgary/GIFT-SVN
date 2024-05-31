/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets;

import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;

/**
 * The header widget for GIFT Wrap.
 * 
 * @author sharrison
 */
public class WrapHeaderWidget extends AbstractBsWidget {

    /** The UiBinder that combines the ui.xml with this java class */
	private static WrapHeaderWidgetUiBinder uiBinder = GWT.create(WrapHeaderWidgetUiBinder.class);
	
	/** Defines the UiBinder that combines the ui.xml with a java class */
	interface WrapHeaderWidgetUiBinder extends UiBinder<Widget, WrapHeaderWidget> {
	}
	
	/** The header text to display to the user */
	@UiField
	protected Span headerText;
	
	/** where the header image will be added */
    @UiField
    NavbarBrand navBarHeader;
	
    /** will be the system image */
    Image headerImage = null;
	
	/** Constructor */
	public WrapHeaderWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	/**
	 * Sets the header text
	 * 
	 * @param text the header text
	 */
	public void setHeaderText(String text){
		headerText.setText(text);
	}
	
	/**
	 * Set the nav bar header image to the system icon
	 * @param systemIconUrl the URL accessible by the GAT that will be used as the system icon in the nav bar.
	 * Shouldn't be null or empty.
	 */
	public void setSystemIcon(String systemIconUrl){
	    
       if(headerImage == null){
            headerImage = new Image();
            headerImage.addStyleName("headerIconAdjustment");
            navBarHeader.add(headerImage);
       }
       
       headerImage.setUrl(systemIconUrl);

    }
}
