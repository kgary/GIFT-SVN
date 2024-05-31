/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.lm;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import generated.course.ImageProperties;
import generated.course.Media;
import generated.course.PDFProperties;
import generated.course.Size;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * A dialog used to add and edit lesson material media
 * 
 * @author nroberts
 */
public class AddMediaDialog extends Composite implements HasValue<Media>{

	private static AddFeedbackDialogUiBinder uiBinder = GWT
			.create(AddFeedbackDialogUiBinder.class);

	interface AddFeedbackDialogUiBinder extends
			UiBinder<Widget, AddMediaDialog> {
	}
	
	@UiField
	protected Heading dialogTitle;
	
	@UiField
	protected Modal addMediaModal;
		
	@UiField
	protected Button confirmButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected DeckPanel mainDeck;
	
	@UiField
	protected Button pdfButton;
	
	@UiField
	protected Button localWebpageButton;
	
	@UiField
	protected Button webAddressButton;
	
	@UiField
    protected Button videoButton;
	
	@UiField
	protected Button imageButton;
	
	@UiField
	protected Button youTubeButton;
	
	@UiField
	protected MediaPanel mediaPanel;
	
	@UiField
	protected Widget choicePanel;
	
	@UiField
	protected Button changeTypeButton;
	
	private Media value = null;

	/**
	 * Creates a new dialog for adding and editing media
	 */
	public AddMediaDialog() {
		initWidget(uiBinder.createAndBindUi(this));
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addMediaModal.hide();
			}
		});
		
		// PDF
        pdfButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                Media media = new Media();               
                media.setMediaTypeProperties(new PDFProperties());
                
                if(value != null){
                	media.setName(value.getName());
                }
                
                value = media;
                
                mediaPanel.editMedia(null, media);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
        
        // Local webpage
        localWebpageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

            	Media media = new Media();
            	media.setMediaTypeProperties(new WebpageProperties());
            	
            	if(value != null){
                	media.setName(value.getName());
                }
            	
            	value = media;
                    
                mediaPanel.editLocalWebpage(value, false);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
        
        // Video
        videoButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                     
                Media media = new Media();
                media.setMediaTypeProperties(new VideoProperties());
                
                if(value != null){
                    media.setName(value.getName());
                }
                
                value = media;
                
                mediaPanel.editMetadataMedia(media, null, false);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
        
		// Image
        imageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                     
                Media media = new Media();
                media.setMediaTypeProperties(new ImageProperties());
                
                if(value != null){
                	media.setName(value.getName());
                }
                
                value = media;
                
                mediaPanel.editMetadataMedia(media, null, false);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
        
		// Website
        webAddressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
            	
            	Media media = new Media();
            	media.setMediaTypeProperties(new WebpageProperties());
            	
            	if(value != null){
                	media.setName(value.getName());
                }
            	
            	value = media;
                    
                mediaPanel.editWebAddress(value, false);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
		
		// Youtube
        youTubeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                Media media = new Media();
                media.setMediaTypeProperties(new YoutubeVideoProperties());
                
                if(value != null){
                	media.setName(value.getName());
                }
                
                value = media;
                
                mediaPanel.editMetadataMedia(media, null, false);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
                changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
            }
        });
        
        changeTypeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
					
				Media media = new Media();
				
				if(value != null){
                	media.setName(value.getName());
                }
				
				value = media;
				
				mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
				
				changeTypeButton.setVisible(false);
			}
		});
		
		confirmButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(GatClientUtility.isReadOnly()) {
					// Make sure logic doesn't execute if a user modifies the DOM
					return;
				}
				
				if(value != null){
					
					if(value.getName() == null || value.getName().trim().isEmpty()){
						WarningDialog.alert("Information Missing", "Please enter some text to use for this media's title");
						return;
					}
					
					if(value.getMediaTypeProperties() != null){
						
						if(value.getMediaTypeProperties() instanceof WebpageProperties){
							
							if(value.getUri() == null || value.getUri().trim().isEmpty()){
								
								WarningDialog.alert("Invalid value", "Please specify a webpage to use.");
								return;
							}
							
						} else if(value.getMediaTypeProperties() instanceof PDFProperties){
							
							if(value.getUri() == null || value.getUri().trim().isEmpty()){
								
								WarningDialog.alert("Invalid value", "Please select a PDF file to use.");
								return;
							}
							
						} else if(value.getMediaTypeProperties() instanceof ImageProperties){
							
							if(value.getUri() == null || value.getUri().trim().isEmpty()){
								
								WarningDialog.alert("Invalid value", "Please select an image file to use.");
								return;
							}
							
						} else if(value.getMediaTypeProperties() instanceof VideoProperties){
                            
                            if(value.getUri() == null || value.getUri().trim().isEmpty()){
                                
                                WarningDialog.alert("Invalid value", "Please select a video file to use.");
                                return;
                            }
							
						} else if(value.getMediaTypeProperties() instanceof YoutubeVideoProperties){
							
							if(value.getUri() == null || value.getUri().trim().isEmpty()){
								
								WarningDialog.alert("Invalid value", "Please specify a YouTube video to use.");
								return;
							}
							
							YoutubeVideoProperties properties = (YoutubeVideoProperties) value.getMediaTypeProperties();
							
							if(properties.getSize() != null){
								
								if(properties.getSize().getWidth() == null){
									
									WarningDialog.alert("Invalid value", "Please specify a width for the video to display.");
									return;
									
								}
								
								if(properties.getSize().getHeight() == null){
									
									WarningDialog.alert("Invalid value", "Please specify a height for the video to display.");
									return;
									
								}
							}
							
						} else {
							
							WarningDialog.alert("Invalid value", "Please select a media type.");
							return;
						}
						
					} else {
						
						WarningDialog.alert("Invalid value", "Please select a media type.");
						return;
					}
					
				} else {
					
					WarningDialog.alert("Invalid value", "Please select a media type.");
					return;
				}
				
				ValueChangeEvent.fire(AddMediaDialog.this, value);
			}
		});
				
        if(GatClientUtility.isReadOnly()){
        	confirmButton.setVisible(false);
        	changeTypeButton.setVisible(false);
        }
	}
	
	@Override
	public void setValue(Media value){
		
		Media mediaItem = new Media();
		
		//Checks if editing existing media
        if(value == null || value.getUri() == null || value.getUri().equals("")) {
            dialogTitle.setText("Add Media");
            confirmButton.setText("Add Media");
        } else {
            dialogTitle.setText("Edit Media");
            confirmButton.setText("Save Changes");
        }
		
		if(value != null){		
		        
		    //create a copy of the given media item so that changes can be cancelled
			mediaItem = new Media();
			mediaItem.setName(value.getName());
			mediaItem.setMessage(value.getMessage());
			mediaItem.setUri(value.getUri());
			
			if(value.getMediaTypeProperties() != null){
				
				if(value.getMediaTypeProperties() instanceof WebpageProperties){						
					mediaItem.setMediaTypeProperties(new WebpageProperties());
					
				} else if(value.getMediaTypeProperties() instanceof ImageProperties){				
					mediaItem.setMediaTypeProperties(new ImageProperties());
					
				} else if(value.getMediaTypeProperties() instanceof VideoProperties){      
				    VideoProperties properties = (VideoProperties) value.getMediaTypeProperties();
				    VideoProperties copyProperties = new VideoProperties();
				    
				    copyProperties.setAllowAutoPlay(properties.getAllowAutoPlay());
				    copyProperties.setAllowFullScreen(properties.getAllowFullScreen());
				    
				    if(properties.getSize() != null) {
				        Size copySize = new Size();
				        
				        copySize.setHeight(properties.getSize().getHeight());
				        copySize.setWidth(properties.getSize().getWidth());
				        
				        copySize.setHeightUnits(properties.getSize().getHeightUnits());
				        copySize.setWidthUnits(properties.getSize().getWidthUnits());
				        
				        copySize.setConstrainToScreen(properties.getSize().getConstrainToScreen());
				        
				        copyProperties.setSize(copySize);
				    }
				    
                    mediaItem.setMediaTypeProperties(copyProperties);
					
				} else if(value.getMediaTypeProperties() instanceof PDFProperties){	
					mediaItem.setMediaTypeProperties(new PDFProperties());
					
				} else if(value.getMediaTypeProperties() instanceof YoutubeVideoProperties){	

					YoutubeVideoProperties properties = (YoutubeVideoProperties) value.getMediaTypeProperties();
					YoutubeVideoProperties copyProperties = new YoutubeVideoProperties();
					
					copyProperties.setAllowAutoPlay(properties.getAllowAutoPlay());
					copyProperties.setAllowFullScreen(properties.getAllowFullScreen());
					
					if(properties.getSize() != null){
						
						Size copySize = new Size();
						
						copySize.setHeight(properties.getSize().getHeight());
						copySize.setWidth(properties.getSize().getWidth());
						
						copySize.setHeightUnits(properties.getSize().getHeightUnits());
						copySize.setWidthUnits(properties.getSize().getWidthUnits());
						
						copySize.setConstrainToScreen(properties.getSize().getConstrainToScreen());
						
						copyProperties.setSize(copySize);
					}
					
					mediaItem.setMediaTypeProperties(copyProperties);
					
				}
			}
		}
		
		this.value = mediaItem;
		
		if(mediaItem.getMediaTypeProperties() == null){		
			
			mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
			
			changeTypeButton.setVisible(false);
			
		} else if(mediaItem.getMediaTypeProperties() instanceof WebpageProperties){	
						
			if(CourseElementUtil.isWebAddress(mediaItem)) {
				mediaPanel.editWebAddress(mediaItem, false);
				
			} else {
				mediaPanel.editLocalWebpage(mediaItem, false);
			}		
			
            mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
            
            changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
			
		} else if(mediaItem.getMediaTypeProperties() instanceof ImageProperties){				
				
			 mediaPanel.editMetadataMedia(mediaItem, null, false);
             mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
             
             changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
             
		} else if(mediaItem.getMediaTypeProperties() instanceof VideoProperties){              
            
		    mediaPanel.editMetadataMedia(mediaItem, null, false);
            mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
            
            changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
			
		} else if(mediaItem.getMediaTypeProperties() instanceof PDFProperties){	
								
			mediaPanel.editMedia(null, mediaItem);
            mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
            
            changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
			
		} else if(mediaItem.getMediaTypeProperties() instanceof YoutubeVideoProperties){	

			mediaPanel.editMetadataMedia(mediaItem, null, false);
            mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
            
            changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
			
		} else {
			
			mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
			
			changeTypeButton.setVisible(!GatClientUtility.isReadOnly());
		}
		
	}
	
	/**
	 * Shows the add media dialog
	 */
	public void center() {
		addMediaModal.show();
	}
	
	/**
	 * Hides the add media dialog
	 */
	public void hide() {
		addMediaModal.hide();
		value = null;
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Media> handler) {
		
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Media getValue() {
		return value;
	}

	@Override
	public void setValue(Media value, boolean arg1) {
		setValue(value);
		
		ValueChangeEvent.fire(this, value);
	}
}
