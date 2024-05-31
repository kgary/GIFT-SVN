/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;
import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.ConceptHierarchyPanel.ImportHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;

/**
 * A browser that loads and displays authoritative resources from the server
 * 
 * @author nroberts
 */
public class AuthoritativeResourceBrowser extends Composite {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(AuthoritativeResourceBrowser.class.getName());

    private static AuthoritativeResourceBrowserUiBinder uiBinder = GWT
            .create(AuthoritativeResourceBrowserUiBinder.class);
    
    /** A handler used to listen for when the user tries to import an authoritative resource */
    private ImportHandler importer;

    interface AuthoritativeResourceBrowserUiBinder extends UiBinder<Widget, AuthoritativeResourceBrowser> {
    }

    /**
     * Creates a new browser that uses the given handler to import authoritative resources
     * 
     * @param importer the handler used to import authoritative resources. Cannot be null.
     */
    public AuthoritativeResourceBrowser(ImportHandler importer) {
        
        if(importer == null) {
            throw new IllegalArgumentException("The handler used to import authoritative resources cannot be null");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        this.importer = importer;
    }
    
    /** The panel used to display the resources being browsed */
    @UiField
    protected FlowPanel resourceList;
    
    /** Breadcrumbs used to return to previously visited resources*/
    @UiField
    protected Breadcrumbs breadcrumbs;
    
    /** A button used to select all displayed resources for importing */
    @UiField
    protected Button selectAllButton;
    
    /** A button used to de-select all displayed resources for importing */
    @UiField
    protected Button selectNoneButton;
    
    /** A button used to de-select all displayed resources for importing */
    @UiField
    protected BlockerPanel waitForResourceBlocker;
    
    /** The resource that the resources currently being displayed were obtained from*/
    private AuthoritativeResourceRecord parentResource;

    /**
     * Refreshes the authoritative resources currently being shown by the browser with fresh data from the server
     */
    public void refresh() {
        
        resourceList.clear();
        
        selectAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                for(Widget widget : resourceList) {
                    if(widget instanceof ResourceDataWidget) {
                        
                        ResourceDataWidget resWidget = (ResourceDataWidget) widget;
                        resWidget.setSelected(true);
                    }
                }
            }
        });
        
        selectNoneButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                for(Widget widget : resourceList) {
                    if(widget instanceof ResourceDataWidget) {
                        
                        ResourceDataWidget resWidget = (ResourceDataWidget) widget;
                        resWidget.setSelected(false);
                    }
                }
            }
        });
        
        parentResource = null;
        updateBreadcrumbs();
        
        waitForResourceBlocker.block();
        
        SharedResources.getInstance().getRpcService().queryAuthoritativeResources(null, null, null, null, 
              	 new AsyncCallback<GenericRpcResponse<List<AuthoritativeResourceRecord>>>() {

      				@Override
      				public void onFailure(Throwable caught) {
      					 ErrorDetailsDialog dialog = new ErrorDetailsDialog("An unexpected error occurred while displaying authoritative resources from the configured system. Please contact your administrator.", 
                	             caught.toString(), null);
                	      dialog.center();
                	      
      					logger.severe("Caught error while browsing authoritative resources: " + caught.toString());
      					waitForResourceBlocker.unblock();
      				}
      				
      				@Override
      				public void onSuccess(GenericRpcResponse<List<AuthoritativeResourceRecord>> result) {
      					if(!result.getWasSuccessful()) {
      						DetailedExceptionSerializedWrapper e = result.getException();

      						ErrorDetailsDialog dialog;                    
      						if(e != null){
      						    dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(), e.getErrorStackTrace());

      						} else {
      						    dialog = new ErrorDetailsDialog("An unexpected error occurred while loading authoritative resources from the configured system. Please contact your administrator.", 
      						        "An unhandled error occurred while querying authoritative resources.", null);
      						}
      						    dialog.center();
      				   }
      					
      				    else {
      					    for(final AuthoritativeResourceRecord resource : result.getContent()) {
                            resource.setParent(parentResource);
                            ResourceDataWidget resourceWidget = new ResourceDataWidget(resource, new Command() {
                                
                                  @Override
                                  public void execute() {
                                    importer.onImport(getSelectedResources());
                                  }
                              }, new Command() {

								@Override
								public void execute() {
								
								  if(resource.getChildrenIDs() != null) {
	                                 parentResource = resource;
	                                 updateBreadcrumbs();
	                                 displayResources(resource.getChildrenIDs());
	                              }
							  }
						});
                            
                            resourceList.add(resourceWidget);
      					
      				       }
      					}	
      					
      					   waitForResourceBlocker.unblock();
      				}		
      		  }); 
          }
    
    /**
     * Loads the authoritative resources with the given IDs and displays them in the browser
     * 
     * @param list the IDs of the resources to display. If null, the browser will be cleared, but nothing will be loaded or displayed.
     */
    
	public void displayResources(List<String> list) {
        
        resourceList.clear();
        
        if(list == null) {
            return;
        }
        
        waitForResourceBlocker.block();
        SharedResources.getInstance().getRpcService().getAuthoritativeResources(
                   list,
                  new AsyncCallback<GenericRpcResponse<List<AuthoritativeResourceRecord>>>() {
                          
                         @Override
                          public void onSuccess(GenericRpcResponse<List<AuthoritativeResourceRecord>> result) {
                        	 if(!result.getWasSuccessful()) {
                                 DetailedExceptionSerializedWrapper e = result.getException();

                                 ErrorDetailsDialog dialog;                    
                                 if(e != null){
                                     dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(), e.getErrorStackTrace());

                                 } else {
                                     dialog = new ErrorDetailsDialog("An unexpected error occurred while displaying authoritative resources from the configured system. Please contact your administrator.", 
                                         "An unhandled error occurred while displaying authoritative resources.", null);
                                 }
                                 dialog.center();
                                 
                                 return;
                              }
                        	 
                               List<AuthoritativeResourceRecord> resources = result.getContent();
                               if(resources != null){
                               for(final AuthoritativeResourceRecord resource : resources){
                               resource.setParent(parentResource);
                               ResourceDataWidget resourceWidget = new ResourceDataWidget(resource, new Command() {
                                  
                                   @Override
                                   public void execute() {
                                       importer.onImport(getSelectedResources());
                                   }
                               }, new Command() {

                                      @Override
                                      public void execute() {

                                          if(resource.getChildrenIDs() != null) {
                                           parentResource = resource;
                                           updateBreadcrumbs();
                                           displayResources(resource.getChildrenIDs());
                                       }
                                   }
                              
                               });
                      
                               resourceList.add(resourceWidget);
                           }
                         }

                            waitForResourceBlocker.unblock();
                      }
                          
                           @Override
                           public void onFailure(Throwable caught) {
                        	   ErrorDetailsDialog dialog = new ErrorDetailsDialog("An unexpected error occurred while displaying authoritative resources from the configured system. Please contact your administrator.", 
                        	             caught.toString(), null);
                        	   dialog.center();
                        	   
                               logger.severe("Caught error while browsing authoritative resources: " + caught.toString());
                               
                               waitForResourceBlocker.unblock();
                     }
                }
            ); 
        }

	
    /**
     * Updates the displayed breadcrumbs to match the resources that the user has visited so far, allowing them to return
     * to resources after they drill down into their children.
     */
    private void updateBreadcrumbs() {
        
        breadcrumbs.clear();
        
        if(parentResource == null) {
            breadcrumbs.setVisible(false);
            return;
            
        } else {
            breadcrumbs.setVisible(true);
        }
        
        AuthoritativeResourceRecord currParent = parentResource;
        do{
            AnchorListItem breadcrumb = new AnchorListItem(currParent.getName());
            final AuthoritativeResourceRecord resource = currParent;
            breadcrumb.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    if(resource.getParent() != null) {
                        parentResource = resource.getParent();
                        displayResources(resource.getParent().getChildrenIDs());
                        
                    } else {
                        refresh();
                    }
                    
                    updateBreadcrumbs();
                }
            });
            
            breadcrumbs.insert(breadcrumb, 0);
            
            currParent = currParent.getParent();
            
        } while(currParent != null);
    }
    
    /**
     * Gets all of the displayed resources that have been selected. This will also
     * de-select said resources as they are gathered.
     * 
     * @return the selected resources. Will not be null, but can be empty.
     */
    public List<AuthoritativeResourceRecord> getSelectedResources(){
        
        List<AuthoritativeResourceRecord> selectedResources = new ArrayList<>();
        
        for(Widget widget : resourceList) {
            if(widget instanceof ResourceDataWidget) {
                
                ResourceDataWidget resWidget = (ResourceDataWidget) widget;
                if(resWidget.isSelected()) {
                    selectedResources.add(resWidget.getResource());
                    resWidget.setSelected(false);
                }
            }
        }
        
        return selectedResources;
    }
}
