/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.FileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * A dialog that displays a list of media files and allows uploading & deleting files.
 * 
 * @author bzahid
 */
public class MediaManagementDialog extends Composite {
    
    private static final Logger logger = Logger.getLogger(MediaManagementDialog.class.getName());
	
	interface MediaManagementDialogUiBinder extends UiBinder<Widget, MediaManagementDialog> {
	}
	
	interface Style extends CssResource {
		String deleteEnabled();
		String deleteDisabled();
		String downloadEnabled();
		String downloadDisabled();
		String previewButton();
	}
	
	private static MediaManagementDialogUiBinder uiBinder = GWT.create(MediaManagementDialogUiBinder.class);
	
    private static final List<String> FILTERED_EXTENSIONS = Arrays.asList(
			AbstractSchemaHandler.COURSE_FILE_EXTENSION,
			AbstractSchemaHandler.DKF_FILE_EXTENSION,
			AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION,
			AbstractSchemaHandler.METADATA_FILE_EXTENSION,
			AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION,
			AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION,
			AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION,
			AbstractSchemaHandler.INTEROP_FILE_EXTENSION,
			AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION,
			AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION
		);
    
    private static final String FILE_ICON = "images/file.png";

    private static final String PDF_ICON = "images/pdf_icon.png";
    
    private static final String LOADING_ICON = "images/loading.gif";

    private static final String ERROR_ICON = "images/error.png";

    private static final String CHECK_COLUMN_WIDTH = "60px";

    private static final String SMALL_ICON_COLUMN_WIDTH = "75px";

    private static final String TYPE_COLUMN_WIDTH = "125px";

    private static final String PREVIEW_COLUMN_WIDTH = "75px";
	
    private boolean readOnly = false;
    
	@UiField
	protected Style style;
	
	@UiField
	protected Button uploadButton;
	
	@UiField
	protected Image downloadImage;
	
	@UiField
	protected FlowPanel downloadPanel;
	
	@UiField
	protected Image deleteImage;
	
	@UiField
	protected FlowPanel deletePanel;
	
	@UiField
	protected Modal courseMediaModal;
	
	@UiField
	protected Grid headerGrid;
	
	@UiField
	protected Label nameHeader;
	
	@UiField
	protected Label typeHeader;
	
	@UiField
	protected FlowPanel loadingOverlay;
	
	@UiField 
	protected CellTable<SelectableMediaWidget> mediaListTable;

    /** The html for the preview button column */
    private final SafeHtml previewHtml;
			
	private DefaultGatFileSelectionDialog uploadDialog = new DefaultGatFileSelectionDialog();
	
	/** Map of media file name to the url of its location on the file system */
	public Map<String, String> mediaNameToFileUrl = new HashMap<String, String>();
	
	private ListDataProvider<SelectableMediaWidget> mediaListDataProvider = new ListDataProvider<SelectableMediaWidget>();
	
	/** A list to keep track of which media files are selected */
	private List<SelectableMediaWidget> selectedFiles = new ArrayList<SelectableMediaWidget>();
	
	/** An instance of the {@link GatRpcServiceAsync} service */
	private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);
	
	private Column<SelectableMediaWidget, String> mediaListNameColumn = new Column<SelectableMediaWidget, String>(new TextCell() {

	        @Override
            public Set<String> getConsumedEvents() {
                HashSet<String> events = new HashSet<String>();
                events.add("click");
                return events;
            }

        }) {

		@Override
		public String getValue(SelectableMediaWidget mediaWidget) {
			return mediaWidget.getFileName();
		}
		
		@Override
		public void onBrowserEvent(Context context, Element elemn, SelectableMediaWidget object, NativeEvent event) {
		    super.onBrowserEvent(context, elemn, object, event);
		    if ("click".equals(event.getType())) {
                checkRow(context);
		    }
		}
	};
	
	private Column<SelectableMediaWidget, String> mediaListSmallIconColumn = new Column<SelectableMediaWidget, String>(new ButtonCell(){

            @Override
            public void render(final Context context, String value, SafeHtmlBuilder sb){
                SelectableMediaWidget widget = (SelectableMediaWidget) context.getKey();
                
                String imagePath = "";
                String href = "";

                String previewLink = mediaNameToFileUrl.get(widget.getFileName());

                if (previewLink == null) {
                    previewLink = LOADING_ICON;
                }

                href = "href=\"" + previewLink + "\"";

                if (widget != null && StringUtils.endsWith(widget.getType(), Constants.image_supported_types)) {
                    imagePath = previewLink;
                } else if (widget != null && widget.getType() == Constants.PDF){
                    imagePath = PDF_ICON;
                } else {
                    imagePath = FILE_ICON;
                }

		        SafeHtml html = SafeHtmlUtils.fromTrustedString("<a target=\"_blank\" " + href + " style=\"width: 25px;\">"
		                + "<img src=\"" + imagePath + "\" style=\"width: 25px;\"></a>");
		        sb.append(html);
            }
            
        }){
            
        @Override
        public String getValue(SelectableMediaWidget object) {
            return null;
        }
	    
	};
	
	private Column<SelectableMediaWidget, String> mediaListTypeColumn = new Column<SelectableMediaWidget, String>(new TextCell() { 

	        @Override
            public Set<String> getConsumedEvents() {
                HashSet<String> events = new HashSet<String>();
                events.add("click");
                return events;
            }

        }) {

		@Override
		public String getValue(SelectableMediaWidget mediaWidget) {
			String extension = mediaWidget.getFileName().substring( mediaWidget.getFileName().lastIndexOf(".") + 1);
			return extension.toUpperCase();
		}
		
		@Override
		public void onBrowserEvent(Context context, Element elemn, SelectableMediaWidget object, NativeEvent event) {
		    super.onBrowserEvent(context, elemn, object, event);
		    if ("click".equals(event.getType())) {
                checkRow(context);
		    }
		}
		
	};
	
	private Column<SelectableMediaWidget, Boolean> mediaListCheckColumn = new Column<SelectableMediaWidget, Boolean>(new CheckboxCell() {

            @Override
            public Set<String> getConsumedEvents() {
                HashSet<String> events = new HashSet<String>();
                events.add("click");
                return events;
            }

        }) {

		@Override
		public Boolean getValue(SelectableMediaWidget mediaWidget) {
			return Boolean.valueOf(mediaWidget.isSelected());
		}
		
		@Override
		public void onBrowserEvent(Context context, Element elemn, SelectableMediaWidget object, NativeEvent event) {
            super.onBrowserEvent(context, elemn, object, event);
		    if ("click".equals(event.getType())) {
                checkRow(context);
		    }
		}
		
	};
	
	private Column<SelectableMediaWidget, String> mediaListPreviewColumn = new Column<SelectableMediaWidget, String>(new ButtonCell(){
            
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb){
		        sb.append(previewHtml);
            }
            
        }){
            
        @Override
        public String getValue(SelectableMediaWidget object) {
            return null;
        }
	};
	
	private enum SortState{
	    NONE,
	    NAME_ASC,
	    NAME_DESC,
	    TYPE_ASC,
	    TYPE_DESC
	} SortState sortState = SortState.NONE;
	
	/**
	 * Creates a new Media Management Dialog
	 */
	public MediaManagementDialog() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		previewHtml = SafeHtmlUtils.fromTrustedString("<button title='Preview media' class='btn " + style.previewButton() + " btn-xs btn-default' "
	            + "type='button' style='margin: 0px;'><i class='fa fa-eye'></i> </button>");
		
		uploadDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
		uploadDialog.setIntroMessageHTML("Select a media file to use in this course. Other file types will be excluded.");
		uploadDialog.filterOutExtensions(FILTERED_EXTENSIONS);
		uploadDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				FileTreeModel model = new FileTreeModel(event.getValue());
				
                SelectableMediaWidget widget = new SelectableMediaWidget(model);
                SelectableMediaWidget identicalWidget = null;
                
                //see if there's already a media widget with an identical path
                for(SelectableMediaWidget existingWidget : mediaListDataProvider.getList()){
                	
                	if(existingWidget.getFilePath().equals(widget.getFilePath())){
                		identicalWidget = existingWidget;
                		break;
                	}
                }
                
                if(identicalWidget != null){
                	
                	//if a widget with an identical path is found, just reuse that widget
                	widget = identicalWidget;
                	
                } else {
                	
                	//otherwise, add the new widget
                	mediaListDataProvider.getList().add(widget);
                }

				String userName = GatClientUtility.getUserName();
				String relativeFileName = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + model.getRelativePathFromRoot(true);
				final String fileName = widget.getFileName();
			
				rpcService.getAssociatedCourseImage(userName, relativeFileName, new AsyncCallback<FetchContentAddressResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        mediaNameToFileUrl.put(fileName, ERROR_ICON);
                        mediaListDataProvider.refresh();
                        logger.severe("An error occured while getting preview link. " + caught.getClass().getName() + ": " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(FetchContentAddressResult result) {
                        if (result.isSuccess() && result.getContentURL() != null) {
                            mediaNameToFileUrl.put(fileName, result.getContentURL());
                        } else {
                            mediaNameToFileUrl.put(fileName, ERROR_ICON);
                            logger.severe("Failed to get preview link. " + result.getErrorMsg());
                        }
                        mediaListDataProvider.refresh();
                    }
				    
				});
				mediaListDataProvider.refresh();
			}
			
		});
		
		uploadButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				uploadDialog.center();
			}
			
		});
		
		mediaListCheckColumn.setFieldUpdater(new FieldUpdater<SelectableMediaWidget, Boolean>() {
			
			@Override
			public void update(int index, SelectableMediaWidget mediaWidget, Boolean isSelected) {
			    Context context = new Context(index, 0, mediaWidget);
			    checkRow(context);
			}
		});		
		
		downloadImage.setResource(GatClientBundle.INSTANCE.download());

        downloadPanel.addStyleName(style.downloadDisabled());

		downloadPanel.addDomHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				final List<String> selectedFiles = new ArrayList<String>();
				for(SelectableMediaWidget file : mediaListDataProvider.getList()) {
					if(file.isSelected()) {
						selectedFiles.add(file.getFileName());
					}
				}
				
				if(!selectedFiles.isEmpty()) {
				    for (String file : selectedFiles) {

                        String hostURL = com.google.gwt.core.client.GWT.getHostPageBaseURL();
                        String url = hostURL + "downloadService?fileName=" + URL.encodeQueryString(file) + "&fileInfo1=" + URL.encodeQueryString(mediaNameToFileUrl.get(file));
                        
                        Window.open(url, "_blank", "");
				    }
		    		
				}
			}
			
		}, ClickEvent.getType());
		
		deleteImage.setResource(GatClientBundle.INSTANCE.trashcan());
		
		deletePanel.addDomHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				if(readOnly) {
					return;
				}
				
				String selectedFilesHtmlList = "";
				final List<String> selectedFiles = new ArrayList<String>();				
				for(SelectableMediaWidget file : mediaListDataProvider.getList()) {
					if(file.isSelected()) {
						selectedFiles.add(file.getFilePath());
						selectedFilesHtmlList += "<li>" + file.getFileName() + "</li>";
					}
				}
				
				if(!selectedFiles.isEmpty()) {
										
					StringBuilder message = new StringBuilder();
		    		message.append("<div style='text-align: left;'>");
		    		message.append("The following files are not authorable by GIFT and may be referenced by metadata files in the same course folder:");				    		
		    		message.append("<br/>");
		    		message.append("<ul>");
		    		message.append(selectedFilesHtmlList);
		    		message.append("</ul>");
		    		message.append("If these files are deleted, their associated metadata files will remain in the course folder and will be ignored during course execution.");
		    		message.append("<br/>");
		    		message.append("<br/>");
		    		message.append("Do you still wish to permanently delete these files? This action cannot be undone.");
		    		message.append("<br/>");
		    		message.append("</div>");
		    		
					OkayCancelDialog.show("Possible References", message.toString(), "Yes, delete these files", new OkayCancelCallback() {

						@Override
						public void okay() {

							final FileOperationProgressModal fileProgressModal = new FileOperationProgressModal(ProgressType.DELETE);
							DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(GatClientUtility.getUserName(), GatClientUtility.getBrowserSessionKey(), selectedFiles, true);
							SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

								@Override
								public void onFailure(Throwable thrown) {

									fileProgressModal.stopPollForProgress(true);										
									ErrorDetailsDialog dialog = new ErrorDetailsDialog("An error occurred during the delete operation.", 
											thrown.getMessage(), DetailedException.getFullStackTrace(thrown));
									dialog.setDialogTitle("Failed to Delete Files");
									dialog.center();
								}

								@Override
								public void onSuccess(GatServiceResult result) {

									refreshMediaFilesList();
									fileProgressModal.stopPollForProgress(!result.isSuccess());

									if(!result.isSuccess()) {
										ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), 
												result.getErrorDetails(), result.getErrorStackTrace());
										dialog.setDialogTitle("Failed to Delete Files" );
										dialog.center();
									}
								}

							});
							
							fileProgressModal.startPollForProgress();
						}

						@Override
						public void cancel() {
							// Nothing to do
						}

					});
				} else {
					WarningDialog.warning("Unable to Delete", "No files have been selected to delete. Please select a file.");
				}
			}
			
		}, ClickEvent.getType());
		
		mediaListTable.setPageSize(Integer.MAX_VALUE);
		mediaListTable.addColumn(mediaListCheckColumn);
		mediaListTable.setColumnWidth(mediaListCheckColumn, CHECK_COLUMN_WIDTH);
		mediaListTable.addColumn(mediaListSmallIconColumn);
		mediaListTable.setColumnWidth(mediaListSmallIconColumn, SMALL_ICON_COLUMN_WIDTH);
		mediaListTable.addColumn(mediaListNameColumn);
		mediaListTable.addColumn(mediaListTypeColumn);
		mediaListTable.setColumnWidth(mediaListTypeColumn, TYPE_COLUMN_WIDTH);
		mediaListTable.addColumn(mediaListPreviewColumn);
		mediaListTable.setColumnWidth(mediaListPreviewColumn, PREVIEW_COLUMN_WIDTH);
		mediaListTable.setEmptyTableWidget(new HTML("No media files found."));
		mediaListDataProvider.addDataDisplay(mediaListTable);
		
		mediaListPreviewColumn.setFieldUpdater(new FieldUpdater<SelectableMediaWidget, String>() {

            @Override
            public void update(int index, SelectableMediaWidget object, String value) {
                previewMediaObject(object);
            }
		});
		
		headerGrid.getColumnFormatter().setWidth(0, "66px");
		headerGrid.getColumnFormatter().setWidth(2, "125px");
		headerGrid.getColumnFormatter().setWidth(3, "100px");
		
		nameHeader.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent arg0) {
                Comparator<SelectableMediaWidget> cmp;
                
                //Selects the appropriate comparator and appends the header based off of the current sorted state
                if(sortState != SortState.NAME_ASC) {
                    cmp = new Comparator<SelectableMediaWidget>(){
                        @Override
                        public int compare(SelectableMediaWidget o1, SelectableMediaWidget o2){
                            return o1.getFileName().compareTo(o2.getFileName());
                        }
                    };
                    nameHeader.setText("Name " + "\u25B2");
                    typeHeader.setText("Type");
                    sortState = SortState.NAME_ASC;
                }
                
                else {
                    cmp = new Comparator<SelectableMediaWidget>(){
                        @Override
                        public int compare(SelectableMediaWidget o1, SelectableMediaWidget o2){
                            return o2.getFileName().compareTo(o1.getFileName());
                        }
                    };
                    nameHeader.setText("Name " + "\u25BC");
                    typeHeader.setText("Type");
                    sortState = SortState.NAME_DESC;
                }
                
                //Performs the sort
                Collections.sort(mediaListDataProvider.getList(), cmp);
            }
		    
		});
		
		typeHeader.addClickHandler(new ClickHandler(){
		    
            @Override
            public void onClick(ClickEvent arg0) {
                Comparator<SelectableMediaWidget> cmp;
                
                //Selects the appropriate comparator and appends the header based off of the current sorted state
                if(sortState != SortState.TYPE_ASC) {
                    cmp = new Comparator<SelectableMediaWidget>(){
                        @Override
                        public int compare(SelectableMediaWidget o1, SelectableMediaWidget o2){
                            return o1.getType().compareTo(o2.getType());
                        }
                    };
                    nameHeader.setText("Name");
                    typeHeader.setText("Type " + "\u25B2");
                    sortState = SortState.TYPE_ASC;
                }
                
                else {
                    cmp = new Comparator<SelectableMediaWidget>(){
                        @Override
                        public int compare(SelectableMediaWidget o1, SelectableMediaWidget o2){
                            return o2.getType().compareTo(o1.getType());
                        }
                    };
                    nameHeader.setText("Name");
                    typeHeader.setText("Type " + "\u25BC");
                    sortState = SortState.TYPE_DESC;
                }
                
                //Performs the sort
                Collections.sort(mediaListDataProvider.getList(), cmp);
            }
		    
		});

        mediaListDataProvider.refresh();
        updateDownloadButton();
        
	}

	/**
	 * Checks the checkbox of the selected row
	 * 
	 * @param context the context of the media object selected
	 */
    private void checkRow(Context context) {
        
        SelectableMediaWidget media = (SelectableMediaWidget) context.getKey();
        boolean selected = !media.isSelected();
        media.setSelected(selected);

        if (selected) {
            selectedFiles.add(media);
        } else {
            selectedFiles.remove(media);
        }
        
        mediaListTable.redrawRow(context.getIndex());
        updateDownloadButton();
    }
	
	/**
	 * Updates the download button enabled/disabled css depending on if there are any files selected
	 * or if the course is in read only mode
	 */
	private void updateDownloadButton() {
	    boolean enable = !selectedFiles.isEmpty();

	    if (enable) {
			downloadPanel.removeStyleName(style.downloadDisabled());
			downloadPanel.addStyleName(style.downloadEnabled());
            downloadPanel.setTitle("");
        } else {
			downloadPanel.removeStyleName(style.downloadEnabled());
            downloadPanel.addStyleName(style.downloadDisabled());
            downloadPanel.setTitle("Select one or more media to download");
        }
	}
		
	/**
	 * Refreshes the media files table
	 */
	private void refreshMediaFilesList() {
	    
	    // Display the loading overlay to prevent users from
	    // modifying files until the list has been refreshed 
	    loadingOverlay.setVisible(true);
	    
		FetchMediaFiles action = new FetchMediaFiles();
		action.setUserName(GatClientUtility.getUserName());
		action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());

		SharedResources.getInstance().getDispatchService().execute(action, 
				new AsyncCallback<FetchMediaFilesResult>() {

			@Override
			public void onFailure(Throwable t) {
				WarningDialog.error("Error Retrieving Media", "A server error occurred while attempting to "
						+ "retrieve media files for this course: " + t.getMessage());
                loadingOverlay.setVisible(false);
			}

			@Override
			public void onSuccess(FetchMediaFilesResult result) {
				if(result.isSuccess()) {
				    selectedFiles.clear();
					mediaListDataProvider.getList().clear();
					List<FileTreeModel> fileList = new ArrayList<FileTreeModel>(result.getFileMap().keySet());
					Collections.sort(fileList, new Comparator<FileTreeModel>() {

                        @Override
                        public int compare(FileTreeModel o1, FileTreeModel o2) {
                            return o1.getFileOrDirectoryName().compareTo(o2.getFileOrDirectoryName());
                        }
					    
					});
					for(FileTreeModel file : fileList) {
						SelectableMediaWidget mediaFile = new SelectableMediaWidget(file);
						mediaListDataProvider.getList().add(mediaFile);
						mediaNameToFileUrl.put(mediaFile.getFileName(), result.getFileMap().get(file));
					}
				} else {
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
					dialog.setText("Error Retrieving Media");
					dialog.center();
				}
                loadingOverlay.setVisible(false);
			}

		});
	}
	
	/**
	 * Previews the media object by opening a new window with a preview of the media object
	 * 
	 * @param media The selected media object. No preview is displayed if media is null.
	 */
	private void previewMediaObject(SelectableMediaWidget media) {
		
        if(media != null) {
            
            String url = mediaNameToFileUrl.get(media.getFileName());
            
            Window.open(url, "_blank", "");

        } else {
            WarningDialog.error("Nothing to preview", "Failed to display preview since there was no media to preview.");
        }
	}
	
	/**
	 * Shows the media management dialog.
	 *  
	 * @param mediaFilesList A list containing the media files to display.
	 */
	public void show(List<SelectableMediaWidget> mediaFilesList) {
	    
	    refreshMediaFilesList();
		
	    selectedFiles.clear();
		mediaListDataProvider.getList().clear();
		mediaListDataProvider.getList().addAll(mediaFilesList);
		updateDownloadButton();
		courseMediaModal.show();
	}
	
	/**
	 * Add logic to execute when the modal dialog is hidden 
	 * 
	 * @param hideHandler The handler to execute when the modal dialog is hidden.
	 */
	public void addHideHandler(ModalHideHandler hideHandler) {
		courseMediaModal.addHideHandler(hideHandler);
	}
	
	/**
	 * Sets whether or not the current course is read-only. This prevents the user
	 * from making changes to media files in read-only mode.
	 * 
	 * @param readOnly True if the course is open in read-only mode, false otherwise. 
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		if(readOnly) {
			deleteImage.setResource(GatClientBundle.INSTANCE.trashcan_disabled());
			deletePanel.removeStyleName(style.deleteEnabled());
			deletePanel.addStyleName(style.deleteDisabled());
			downloadPanel.removeStyleName(style.downloadEnabled());
			downloadPanel.addStyleName(style.downloadDisabled());
		} else {
			deleteImage.setResource(GatClientBundle.INSTANCE.trashcan());
			deletePanel.removeStyleName(style.deleteDisabled());
			deletePanel.addStyleName(style.deleteEnabled());
			downloadPanel.removeStyleName(style.downloadDisabled());
			downloadPanel.addStyleName(style.downloadEnabled());
		}
		
		updateDownloadButton();
		
		uploadButton.setEnabled(!readOnly);
	}
}
