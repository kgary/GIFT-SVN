/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.util.HashMap;
import java.util.logging.Logger;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.MetadataPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;

/**
 * A widget used to show content files in the Merrill's Branch Point editor and edit their associated Metadata
 * 
 * @author nroberts
 */
public class ContentFileWidget extends Composite {
    
    /** instance of the logger */
    private static Logger logger = Logger.getLogger(ContentFileWidget.class.getName());

	private static ContentFileWidgetUiBinder uiBinder = GWT
			.create(ContentFileWidgetUiBinder.class);

	interface ContentFileWidgetUiBinder extends
			UiBinder<Widget, ContentFileWidget> {
	}
	
	@UiField
	protected InlineHTML fileNameLabel;
	
	@UiField(provided = true)
	protected Image editButton = new Image(GatClientBundle.INSTANCE.edit_image());
	
	@UiField(provided = true)
	protected Image removeButton = new Image(GatClientBundle.INSTANCE.cancel_image());
	
	/** contains the image/icon that represents the content type */
	@UiField
	protected HTML typeHtml;
	
	/** the tool tip for the content type image/icon */
	@UiField
	protected ManagedTooltip typeTooltip;
		   
    private String metadataFilePath = null;
	
    /**
     * Populate the widget with the metadata information provided.  This constructor
     * will disable the option to delete this content.
     * 
     * @param metadataWrapper contains information about the metadata content including the display name
     * @param metadataObjectEditor the editor to use to edit this metadata
     */
	public ContentFileWidget(final MetadataWrapper metadataWrapper, final CourseObjectModal metadataObjectEditor) {
		this(metadataWrapper, metadataObjectEditor, null);
	}

    /**
     * Populate the widget with the metadata information provided.  
     * 
     * @param metadataWrapper contains information about the metadata content including the display name
     * @param metadataObjectEditor the editor to use to edit this metadata
     * @param deleteCommand the command to execute when the author selects the delete button.  If null the
     * delete button will be hidden for this row
     */
	public ContentFileWidget(final MetadataWrapper metadataWrapper, 
	        final CourseObjectModal metadataObjectEditor, final Command deleteCommand) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.metadataFilePath = metadataWrapper.getMetadataFileName();
		
		if(metadataWrapper.hasExtraneousConcept()){
		    fileNameLabel.setHTML("<font color=\"gray\"><i>"+metadataWrapper.getDisplayName()+"</i></font>");
		    fileNameLabel.setTitle("*** References other course concepts ***\nMetadata file: " + metadataFilePath);
		}else{
		    fileNameLabel.setHTML("<b>"+metadataWrapper.getDisplayName()+"</b>");
		    fileNameLabel.setTitle("Metadata file: " + metadataFilePath);
		}
		
	    logger.info("building type html for "+metadataWrapper.getContentType());

	    ContentTypeEnum contentType = metadataWrapper.getContentType();
	    typeHtml.setHTML(CourseElementUtil.getContentTypeGraphic(contentType).getString());
	    typeTooltip.setTitle(CourseElementUtil.getContentTypeTitle(contentType));
		
		if(metadataObjectEditor != null){
			
			editButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					HashMap<String, String> paramMap = new HashMap<String, String>();
	            	paramMap.put(MetadataPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
	            	
					int index = metadataFilePath.lastIndexOf("/");
					String url = GatClientUtility.getModalDialogUrlWithParams(
							metadataFilePath.substring(0, index), metadataFilePath.substring(index + 1), paramMap);
					metadataObjectEditor.setSaveAndCloseButtonVisible(!GatClientUtility.isReadOnly());
					metadataObjectEditor.setCourseObjectUrl(CourseObjectName.METADATA.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
					metadataObjectEditor.setSaveButtonHandler(new ClickHandler() {
	
						@Override
						public void onClick(ClickEvent event) {
						    if (!GatClientUtility.isReadOnly()) {
						        GatClientUtility.saveEmbeddedCourseObject();
			                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this metadata being edited
						    }
						    metadataObjectEditor.stopEditor();
						}
						
					});
					metadataObjectEditor.show();
				}
			});
			
		} else {
		    hideEditButton();
		}
		
		if(deleteCommand != null){
		
		    if (GatClientUtility.isReadOnly()) {
		        hideRemoveButton();
		    } else {		          
	            removeButton.addClickHandler(new ClickHandler() {
	                
	                @Override
	                public void onClick(ClickEvent event) {
	                    
	                    deleteCommand.execute();
	                }
	            });
		    }

		
		} else {
		    hideRemoveButton();
		}
	}
		
	/**
	 * Hide the remove button from the table while still filling the cell with space so the following
	 * cell(s) in the row are still aligned in their respective column(s).
	 */
	private void hideRemoveButton(){	    
	    removeButton.getElement().getStyle().setProperty("visibility", "hidden");
	}
	
	/**
     * Hide the remove button from the table while still filling the cell with space so the following
     * cell(s) in the row are still aligned in their respective column(s).
     */
	private void hideEditButton(){	    
	    editButton.getElement().getStyle().setProperty("visibility", "hidden");
	}

	public String getMetadataFilePath() {
		return metadataFilePath;
	}

    
}
