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

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog used to allow users to delete metadata files referencing a content file.
 * 
 * @author nroberts
 */
public class MetadataFileDeletionDialog extends ModalDialogBox {

	private static MetadataFileReferenceListDialogUiBinder uiBinder = GWT
			.create(MetadataFileReferenceListDialogUiBinder.class);

	interface MetadataFileReferenceListDialogUiBinder extends
			UiBinder<Widget, MetadataFileDeletionDialog> {
	}
	
	@UiField
	protected HasText contentFileName;
	
	@UiField
	protected FlowPanel filesList;
	
	/** A command used to refresh the MBP editor when a metadata file is deleted */
	private Command refreshCommand = null;

	/**
	 * Creates a new dialog for deleting metadata files
	 * 
	 * @param contentFile the content file referenced by the metadata files
	 * @param metadataFiles the files available for deletion
	 * @param refreshCommand a command used to update the MBP editor when a metadata file is deleted
	 */
	public MetadataFileDeletionDialog(String contentFile, Collection<String> metadataFiles, final Command refreshCommand) {
		setWidget(uiBinder.createAndBindUi(this));
		
		this.refreshCommand = refreshCommand;
		
		setText("Found Metadata References");	
		setGlassEnabled(true);
		
		contentFileName.setText(contentFile);
		
		filesList.clear();
		
		for(final String file : metadataFiles){
		    
		    MetadataWrapper metadataWrapper = new MetadataWrapper(file, file, false, false, null);
			
			filesList.add(new ContentFileWidget(metadataWrapper, null, new Command() {
				
				@Override
				public void execute() {
					
					deletePracticeApplicationMetadata(file);
				}
			}));
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
	
	/**
	 * Deletes the metadata file with the given path
	 * 
	 * @param metadataFilePath the path to the metadata file to delete
	 */
	public void deletePracticeApplicationMetadata(final String metadataFilePath){
		
		String objectName = "UNKNOWN";
		
		if(metadataFilePath != null){
			objectName = metadataFilePath;
		}
		
		OkayCancelDialog.show(
				"Delete Metadata?", 
				"Are you sure you want to delete " + (
						objectName != null 
								? "<b>" + objectName + "</b>" 
								: "this metadata file"
				) + "?", 
				"Yes, delete this metadata", 
				new OkayCancelCallback() {
					
					@Override
					public void okay() {
						
						//find the widget corresponding to this metadata file
						for(int i = 0; i < filesList.getWidgetCount(); i++){
							
							final Widget metadataWidget = filesList.getWidget(i);
							
							if(metadataFilePath != null && metadataWidget instanceof ContentFileWidget 
									&& metadataFilePath.equals(((ContentFileWidget) metadataWidget).getMetadataFilePath())){
								
								//determine if this metadata file is the last one in the list
								boolean isLastMetadata = i == filesList.getWidgetCount() - 1;
								
								DeleteMetadata action = new DeleteMetadata(
										GatClientUtility.getUserName(), 
										GatClientUtility.getBrowserSessionKey(),
										GatClientUtility.getBaseCourseFolderPath(), 
										metadataFilePath, 
										isLastMetadata //do a deep delete for the last metadata file to clean up any resources it references
								);
								
								BsLoadingDialogBox.display("Deleting Metadata", "Please wait while GIFT deletes this metadata.");
								
								//delete the metadata file
								SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GenericGatServiceResult<Void>>() {

									@Override
									public void onFailure(Throwable caught) {
										
										BsLoadingDialogBox.remove();
										
										DetailedException exception = new DetailedException(
												"GIFT was unable to delete this metadata. An unexpected error occurred "
												+ "during the deletion.", 
												caught.toString(), 
												caught
										);
										
										ErrorDetailsDialog dialog = new ErrorDetailsDialog(
												exception.getReason(), 
												exception.getDetails(), 
												exception.getErrorStackTrace()
										);
										
										dialog.center();
									}

									@Override
									public void onSuccess(GenericGatServiceResult<Void> result) {
										
										BsLoadingDialogBox.remove();
										
										if(result.getResponse().getWasSuccessful()){
											
											//refresh the MBP editor's panels to reflect the delete
											if(refreshCommand != null){
												refreshCommand.execute();
											}
											
											//remove the metadata file that was just deleted from the list
											filesList.remove(metadataWidget);
											
											//hide this dialog if the list is empty
											if(filesList.getWidgetCount() < 1){
												
												//all content files widgets have been removed, so there's no point in showing this dialog anymore
												hide();
											}
											
										} else {
											
											ErrorDetailsDialog dialog = new ErrorDetailsDialog(
													result.getResponse().getException().getReason(), 
													result.getResponse().getException().getDetails(), 
													result.getResponse().getException().getErrorStackTrace()
											);
											
											dialog.center();
										}
									}
								});
								
								break;
							}
						}
					    							    
					}
					
					@Override
					public void cancel() {
						//Nothing to do
					}
		});
	}

}
