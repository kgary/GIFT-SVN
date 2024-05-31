/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.lm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import generated.course.LessonMaterial;
import generated.course.LtiProvider;
import generated.course.Media;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * An editor used to author lesson material
 */
public class LessonMaterialViewImpl extends Composite implements LessonMaterialView, CourseReadOnlyHandler {
    
    /** The ui binder. */
    private static LessonMaterialViewImplUiBinder uiBinder = GWT
            .create(LessonMaterialViewImplUiBinder.class);

    /**
     * The Interface LessonMaterialViewImplUiBinder.
     */
    interface LessonMaterialViewImplUiBinder extends
            UiBinder<Widget, LessonMaterialViewImpl> {
    }
    
    /** The Constant NULL_ENTRY_STRING. */
    private static final String NULL_ENTRY_STRING = "";
	
    /**
     * Collection of media objects and the GatServiceResult of WorkspaceFileExist
     * String Key - The URI of the media object (from FilePath.getFilePath())
     * GatServiceResult Value - The result of the WorkspaceFileExist action as a GatServiceResult.
     *                          isSuccessful method determines whether the file exists
     */
	public HashMap<String, GatServiceResult> mediaListValidMap = new HashMap<String, GatServiceResult>();
        
    @UiField
    protected FlowPanel mediaCollectionPanel;
    
    @UiField
    protected MediaPanel mediaTypePanel;
    
    /** The media list data grid. */
    @UiField
    protected DataGrid<Media> mediaListDataGrid;
    
    /** The disable checkbox. */
    @UiField
    protected CheckBox disabled;
    
    @UiField
    protected DisclosurePanel lmOptionsPanel;
    
    // Media name column
	private Column<Media, String> mediaNameColumn = new Column<Media, String>(new TextCell()){
		
		@Override
	    public void render(com.google.gwt.cell.client.Cell.Context context, 
	            final Media media, SafeHtmlBuilder sb) {
		    String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + media.getUri();
		    GatServiceResult result = mediaListValidMap.get(filePath);
		    
		    // if the mediaListValidMap is empty, meaning the files have not been validated yet, do not display the media objects as not found
		    boolean mediaFound = (mediaListValidMap.isEmpty()) ? true : (result != null && result.isSuccess());
			
		    final String renderHtml = "<div style=\"word-wrap: break-word;" + (mediaFound ? "\">" : " color: red;\" title=\"File not found.\">");
		    
			// make sure the name word-wraps to prevent extremely long rows
	        sb.appendHtmlConstant(renderHtml)
	        	.appendEscaped(getValue(media))
	        	.appendHtmlConstant("</div>")
	        ;
	    }
    	
    	@Override
    	public String getValue(Media media){

    		if(media != null && media.getName() != null){
    			return media.getName();
    			
    		} else {
    			return NULL_ENTRY_STRING;
    		}
    	}
    };
  
    
    private Column<Media, String> previewColumn = new Column<Media, String>(new ButtonCell(){
		
			@Override
		    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            String value, SafeHtmlBuilder sb) {
			    Media media = (Media) context.getKey();
			    boolean enabled = true;
			    if (media != null && !mediaListValidMap.isEmpty()) {
			        String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + media.getUri();
			        GatServiceResult result = mediaListValidMap.get(filePath);
			        if (result != null && !result.isSuccess()) {
			            enabled = false;
			        }
			    }
				
				//Nick: I really don't like using raw HTML here, but the Java object for a Bootstrap button doesn't seem to get the correct string
		        SafeHtml html = SafeHtmlUtils.fromTrustedString("<button title='Preview URL' class='btn GPBYFDEIXB btn-xs btn-default' "
		        		+ "type='button' style='margin: 0px;'" + (enabled ? "" : " disabled=true") + "><i class='fa fa-globe fa-lg'></i> </button>");
		        sb.append(html);
		    }
		}){
		
		@Override
		public String getValue(Media record) {
								
			return null;
		}
	};

    private Column<Media, String> editColumn = new Column<Media, String>(new ButtonCell(){
		
			@Override
		    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            String value, SafeHtmlBuilder sb) {
				
				Image image = new Image(value);
				image.setTitle("Edit this media");
				
				Media media = (Media) context.getKey();
				boolean enabled = true;
				if (media != null) {
				    String filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + media.getUri();
				    GatServiceResult result = mediaListValidMap.get(filePath);
				    if (result != null && !result.isSuccess()) {
				        enabled = false;
				    }
				}
				
		        SafeHtml html = SafeHtmlUtils.fromTrustedString("<img src=\"" + image.getUrl() +
		                "\" class=\"gwt-Image\" title=\"Edit this media\"" + (enabled ? "" : " disabled=true") + 
		        		"style=\"cursor: pointer;\"" + ">");
		        sb.append(html);
		    }
		}){
		
		@Override
		public String getValue(Media record) {
								
			return GatClientBundle.INSTANCE.edit_image().getSafeUri().asString();
		}
	};
	
	private Column<Media, String> removeColumn = new Column<Media, String>(new ButtonCell(){
	
			@Override
		    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            String value, SafeHtmlBuilder sb) {
				
		        Image image = new Image(value);
				image.setTitle("Remove this media");
				
		        SafeHtml html = SafeHtmlUtils.fromTrustedString("<img src=\"" + image.getUrl() + 
		        		"\" style=\"cursor: pointer;\"" + (GatClientUtility.isReadOnly() ? " disabled=true;" : "") + ">");
		        sb.append(html);
		    }
		}){
	
		@Override
		public String getValue(Media record) {
								
			return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
		}
	};
    
    /** The add button. */
    @UiField(provided=true)
	protected Image addButton = new Image(GatClientBundle.INSTANCE.add_image());

    /** The refresh button */
    @UiField(provided=true)
    protected Image validateButton = new Image(GatClientBundle.INSTANCE.check());
    
    
    /**
     * Instantiates a new lesson material view impl.
     */
    public LessonMaterialViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
        
        init();
    }
    
    /**
     * Initializes the view.
     */
    private void init(){
    	
        // Column names and widths
        
    	mediaListDataGrid.addColumn(mediaNameColumn, "Media"); 	
    	mediaListDataGrid.setColumnWidth(mediaNameColumn, "100%");
    	
    	mediaListDataGrid.addColumn(previewColumn);
    	mediaListDataGrid.setColumnWidth(previewColumn, "35px");
    	
        if (!GatClientUtility.isReadOnly()) {
            mediaListDataGrid.addColumn(editColumn);
            mediaListDataGrid.setColumnWidth(editColumn, "25px");
            
            mediaListDataGrid.addColumn(removeColumn);
            mediaListDataGrid.setColumnWidth(removeColumn, "45px");  //add 15px for vertical scrollbar spacing so it doesn't overlap remove button
        } else {
            mediaListDataGrid.addColumn(editColumn);
            mediaListDataGrid.setColumnWidth(editColumn, "40px"); //add 15px for vertical scrollbar spacing so it doesn't overlap remove button
            
            disabled.setEnabled(false);
        }

    	HTML emptyTableText = new HTML("No media has been added to this course object.");
    	emptyTableText.getElement().getStyle().setPadding(10, Unit.PX);
    	
    	mediaListDataGrid.setEmptyTableWidget(emptyTableText);
    	
    	setPanelVisibile(mediaCollectionPanel);
    	
    	addButton.setVisible(!GatClientUtility.isReadOnly());
    	
    }
    
    /**
     * Sets the visibility of the widget specified.
     * 
     * @param widget either {@link #mediaCollectionPanel} or {@link #mediaTypePanel}
     */
    private void setPanelVisibile(Widget widget){  
        
        if(widget == null){
            return;
        }
        
        mediaCollectionPanel.setVisible(widget == mediaCollectionPanel);
        mediaTypePanel.setVisible(widget == mediaTypePanel);        
    }
    
    @Override
    public void showMediaType(LessonMaterial lessonMaterial) {
    	if(lessonMaterial.getLessonMaterialList().getMedia() != null && !lessonMaterial.getLessonMaterialList().getMedia().isEmpty()) {
    		mediaTypePanel.editMedia(lessonMaterial, lessonMaterial.getLessonMaterialList().getMedia().get(0));
    		setPanelVisibile(mediaTypePanel);
    		
    	} else {
    	    setPanelVisibile(mediaCollectionPanel);
    	}
    }
    
    @Override
    public void showMediaCollection() {
        setPanelVisibile(mediaCollectionPanel);
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.lm.LessonMaterialView#getMediaListDisplay()
     */
    @Override
    public HasData<Media> getMediaListDisplay(){
    	return mediaListDataGrid;
    }
    
    @Override
    public HasData<CandidateConcept> getMediaConceptsTable() {
        return mediaTypePanel.getConceptsTable();
    }
    
    @Override
    public List<LtiProvider> getLtiProviderIdList() {
        return mediaTypePanel.getCourseLtiProviderList();
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.lm.LessonMaterialView#getAddMediaInput()
     */
    @Override
    public HasClickHandlers getAddMediaInput(){
    	return addButton;
    }
    
    @Override
    public HasClickHandlers getValidateMediaElementsButton(){
        return validateButton;
    }
    
    @Override
    public HasClickHandlers getReplaceSlideShowInput(){
    	return mediaTypePanel.getReplaceSlideShowInput();
    }
    
    @Override
    public DefaultGatFileSelectionDialog getAddSlideShowInput() {
    	return mediaTypePanel.getAddSlideShowInput();
    }
    
    @Override
    public Column<Media, String> getEditColumn(){
		return editColumn;
	}
	
    @Override
	public Column<Media, String> getPreviewColumn(){
		return previewColumn;
	}
	
    @Override
	public Column<Media, String> getRemoveColumn(){
		return removeColumn;
	}
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.lm.LessonMaterialView#redraw()
     */
    @Override
    public void redraw(){
    	mediaListDataGrid.redraw();
    }

    /**
     * Updates the mediaListValidMap by copying the values from map
     * @param map - the map to update the mediaListValidMap with
     */
    public void updateMediaListValidMap(Map<FilePath, GatServiceResult> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null");
        }
        
        for (Map.Entry<FilePath, GatServiceResult> entry : map.entrySet()) {
            mediaListValidMap.put(entry.getKey().getFilePath(), entry.getValue());
        }
        
    }

    @Override
    public HasValue<Boolean> getDisabledInput(){
        return disabled;
    }
    
    @Override
    public void setLmOptionsVisible(boolean visible){       
        lmOptionsPanel.setOpen(visible);
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		disabled.setEnabled(!isReadOnly);
	}
}
