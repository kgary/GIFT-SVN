/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.wizard;

import generated.ped.MetadataAttribute;

import java.util.ArrayList;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.MetadataAttributesEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Glorified wrapper around the MetadataAttributesEditor.
 * @author elafave
 *
 */
public class SelectMetadataAttributesPane extends Composite {
	
	/** The ui binder. */
    interface SelectMetadataAttributesPaneUiBinder extends UiBinder<Widget, SelectMetadataAttributesPane> {} 
	private static SelectMetadataAttributesPaneUiBinder uiBinder = GWT.create(SelectMetadataAttributesPaneUiBinder.class);
	
	@UiField
	protected MetadataAttributesEditor metadataAttributesEditor;
	
	private ArrayList<MetadataAttribute> attributes = new ArrayList<MetadataAttribute>();
	
    public SelectMetadataAttributesPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        reset(MerrillQuadrantEnum.RULE);
    }
    
    public void reset(MerrillQuadrantEnum quadrant) {
    	attributes = new ArrayList<MetadataAttribute>();
    	metadataAttributesEditor.setAttributes(quadrant, attributes);
    }
    
    public ArrayList<MetadataAttribute> getMetadataAttributes() {
    	return attributes;
    }
    
    @Override
    public void setVisible(boolean visible){
    	super.setVisible(visible);
    	
    	if(visible){
    		metadataAttributesEditor.redraw();
    	}
    }
}
