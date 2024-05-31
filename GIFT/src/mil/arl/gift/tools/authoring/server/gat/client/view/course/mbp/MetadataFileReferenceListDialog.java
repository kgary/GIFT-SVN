/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.util.Collection;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog used to show the list of metadata files referencing a content file.
 * 
 * @author nroberts
 */
public class MetadataFileReferenceListDialog extends ModalDialogBox {

	private static MetadataFileReferenceListDialogUiBinder uiBinder = GWT
			.create(MetadataFileReferenceListDialogUiBinder.class);

	interface MetadataFileReferenceListDialogUiBinder extends
			UiBinder<Widget, MetadataFileReferenceListDialog> {
	}
	
	@UiField
	protected HasText contentFileName;
	
	@UiField
	protected FlowPanel filesList;
	
	@UiField
	protected CourseObjectModal metadataObjectDialog;

	public MetadataFileReferenceListDialog(String contentFile, Collection<String> metadataFiles) {
		setWidget(uiBinder.createAndBindUi(this));
		
		setText("Found Metadata References");	
		setGlassEnabled(true);
		
		contentFileName.setText(contentFile);
		
		filesList.clear();
		
		for(String file : metadataFiles){
			
		    MetadataWrapper metadataWrapper = new MetadataWrapper(file, file, false, false, null);
			filesList.add(new ContentFileWidget(metadataWrapper, metadataObjectDialog));
		}
		
		Button closeButton = new Button("Close");
		closeButton.setType(ButtonType.PRIMARY);
		
		closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		setFooterWidget(closeButton);
	}

}
