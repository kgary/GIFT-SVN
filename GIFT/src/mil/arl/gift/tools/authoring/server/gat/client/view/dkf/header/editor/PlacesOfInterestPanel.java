/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.AGL;
import generated.dkf.Area;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.Path;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlacesOfInterestReferencesUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestFilterPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.SharedGatSystemProperties;

/**
 * The widget that is responsible for editing a {@link PlacesOfInterest} object.
 * 
 * @author tflowers
 *
 */
public class PlacesOfInterestPanel extends ScenarioValidationComposite {
    
    /** The logger for the class */
    static final Logger logger = Logger.getLogger(PlacesOfInterestPanel.class.getName());

    /** The ui binder. */
    private static PlacesOfInterestPanelUiBinder uiBinder = GWT.create(PlacesOfInterestPanelUiBinder.class);

    /** The Interface PlaceofInterestPanelUiBinder */
    interface PlacesOfInterestPanelUiBinder extends UiBinder<Widget, PlacesOfInterestPanel> {
    }

    /**
     * The Interface PlaceOfInterestanelEventBinder.
     */
    interface PlacesOfInterestPanelEventBinder extends EventBinder<PlacesOfInterestPanel> {
    }

    /** The Constant eventBinder. */
    private static final PlacesOfInterestPanelEventBinder eventBinder = GWT.create(PlacesOfInterestPanelEventBinder.class);
    
    /** A list of places of interest that contain one or more search items*/
    private ArrayList<Serializable> placesOfInterestMatchingTerms = new ArrayList<Serializable>();
    
    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);
    
    /** A text box used to search for places of interest to select */
    @UiField
    protected TextBox poiSelect;
    
    /** A panel with buttons used to filter the types of places of interest shown */
    @UiField
    protected PlaceOfInterestFilterPanel filterPanel;
    
    /** The editor used to edit items in the list of places of interest*/
    private PlaceOfInterestItemEditor itemEditor = new PlaceOfInterestItemEditor();

    /** The places of interest data grid. */
    @UiField(provided=true)
    protected ItemListEditor<PlaceOfInterestWrapper> placesOfInterestList = new ItemListEditor<PlaceOfInterestWrapper>(itemEditor) {
        
        @Override
        public void remove(final PlaceOfInterestWrapper wrapper) {
            
            final Serializable element = wrapper.getPlaceOfInterest();
            
            final List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(element);
            
            if(references != null && !references.isEmpty()) {
                
                final PlaceOfInterestReferenceList referenceList = new PlaceOfInterestReferenceList();
                referenceList.getListEditor().replaceItems(references);
                referenceList.setOnJumpCommand(new Command() {
                    
                    @Override
                    public void execute() {
                        
                        //close the current prompt if the author jumps to a condition
                        OkayCancelDialog.cancel();
                    }
                });
                
                //defer the next prompt so that the previous one can close and so the reference list can be populated
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        int refCount = 0;
                        
                        for(PlaceOfInterestReference reference : references) {
                            refCount += reference.getReferenceCount();
                        }
                        
                        //this place of interest has references, so we need to ask the author if they want to remove these references first
                        OkayCancelDialog.show(
                                "Remove references?", 
                                refCount +" condition(s) are referencing this place of interest. These references must be removed in order to "
                                + "delete this place of interest.<br/><br/>Do you want to remove these references and continue with the delete? "
                                + "This will likely make the affected conditions invalid until they are assigned with different "
                                + "places of interest.<br/><br/>Conditions that reference this place of interest:", 
                                referenceList,
                                "Remove References and Continue Delete", 
                                new OkayCancelCallback() {
                                    
                                    @Override
                                    public void okay() {
                                        
                                        //this place of interest's references have been removed, so remove the place of interest itself now
                                        delete(wrapper);
                                        
                                        String name = ScenarioClientUtility.getPlaceOfInterestName(element);
                                        ScenarioClientUtility.updatePlaceOfInterestReferences(name, null);
                                    }
                                    
                                    @Override
                                    public void cancel() {
                                        
                                        if(logger.isLoggable(Level.FINE)){
                                            logger.fine("User cancelled removing references");
                                        }
                                    }
                                }
                        ); 
                    }
                });
                
                
                
            } else {
                
                //this place of interest has no references, so just remove it outright
                delete(wrapper);
            }
        }
        
        /**
         * Deletes the given place of interest regardless of any references it may have
         * 
         * @param element the place of interest to delete
         */
        private void delete(PlaceOfInterestWrapper wrapper) {
            
            Serializable element = wrapper.getPlaceOfInterest();
            
            super.remove(wrapper);
            
            //notify listeners that the place of interest was removed
            ScenarioEventUtility.fireDeleteScenarioObjectEvent(element, null);
        }
    };
    
    @UiField
    protected Button createFromFileButton;
    
    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;
    
    /** A dialog that is used to upload files so that places of interest can be created from them */
    private FileSelectionDialog createFromFileDialog = new FileSelectionDialog(
            SharedGatSystemProperties.COURSE_RESOURCE_UPLOAD_URL, 
            new CanHandleUploadedFile() {
                
                @Override
                public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {
                    
                    if(!TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(ScenarioClientUtility.getTrainingAppType())) {
                        
                        //if the user is authoring a non-mobile-device scenario, create places of 
                        //interest from the uploaded file normally
                        createPOIsFromFile(uploadFilePath, fileName, callback, false);
                        
                    } else {
                        
                        //if the user is authoring a mobile device scenario, ask whether they want to ignore potentially 
                        //inaccurate elevation values from the uploaded file
                        final ModalDialogBox ignoreElevationDialog = new ModalDialogBox();
                        ignoreElevationDialog.setCloseable(true);
                        ignoreElevationDialog.setGlassEnabled(true);
                        ignoreElevationDialog.setText("Mobile App Elevation Accuracy");
                        ignoreElevationDialog.setWidget(new HTML(
                                "Due to the inaccuracies of elevation values received from some mobile devices, <br/>"
                                + "the elevations received from the mobile app when taking a course are ignored. <br/><br/>"
                                + "In order for any location or distance checks to work correctly the elevation <br/>"
                                + "values for the points you are importing should also be ignored."
                        ));
                        
                        ignoreElevationDialog.getCloseButton().setText("Cancel");
                        ignoreElevationDialog.getCloseButton().setType(ButtonType.DANGER);
                        ignoreElevationDialog.getCloseButton().addClickHandler(new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                createPOIsFromFile(uploadFilePath, fileName, callback, null);
                            }
                        });
                        
                        Button useButton = new Button("Use elevations", new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                createPOIsFromFile(uploadFilePath, fileName, callback, false);
                                ignoreElevationDialog.hide();
                            }
                        });
                        useButton.setType(ButtonType.PRIMARY);
                        ignoreElevationDialog.setFooterWidget(useButton);
                        
                        Button ignoreButton = new Button("Ignore elevations", new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                createPOIsFromFile(uploadFilePath, fileName, callback, true);
                                ignoreElevationDialog.hide();
                            }
                        });
                        ignoreButton.setType(ButtonType.SUCCESS);
                        ignoreElevationDialog.setFooterWidget(ignoreButton);
                        
                        ignoreElevationDialog.center();
                    }
                }
            }, DefaultMessageDisplay.includeAllMessages);

    /**
     * Instantiates the panel which edits the specified {@link PlacesOfInterest}.
     */
    public PlacesOfInterestPanel() {

        initWidget(uiBinder.createAndBindUi(this));

        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        
        //initialize the filter panel
        resetTypeFilter();

        poiSelect.setPlaceholder("Search places of interest");
        poiSelect.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {
                
                if(scheduledFilter == null) {
                    
                    // Schedule a filter operation for the list. We don't want to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            // update the filter for the selection dropdown
                            loadAndFilterPlacesOfInterest();
                            
                            //allow the filter operation to be applied again, since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        poiSelect.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                /* select all of the search box's text when it gains focus so that it's easier for
                 * the author to clear out */
                poiSelect.selectAll();
            }
        });

        placesOfInterestList.setPlaceholder("No places of interest have been created.");
        placesOfInterestList.setRemoveItemDialogTitle("Delete Place of Interest?");
        placesOfInterestList.setRemoveItemStringifier(new Stringifier<PlaceOfInterestWrapper>() {

            @Override
            public String stringify(PlaceOfInterestWrapper wrapper) {
                
                Serializable obj = wrapper.getPlaceOfInterest();
                
                String name = ScenarioClientUtility.getPlaceOfInterestName(obj);

                StringBuilder builder = new StringBuilder().append("<b>")
                        .append((name != null) ? name : "this place of interest")
                        .append("</b>");

                return builder.toString();
            }
        });

        placesOfInterestList.setFields(buildListFields());

        placesOfInterestList.addCreateListAction("Click here to add a new place of interest", new CreateListAction<PlaceOfInterestWrapper>() {
            @Override
            public PlaceOfInterestWrapper createDefaultItem() {
                return new PlaceOfInterestWrapper();
            }
        });

        placesOfInterestList.addListChangedCallback(new ListChangedCallback<PlaceOfInterestWrapper>() {

            @Override
            public void listChanged(ListChangedEvent<PlaceOfInterestWrapper> event) {

                if (ListAction.ADD.equals(event.getActionPerformed())) {

                    List<Serializable> placesOfInterest = ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea();
                    PlaceOfInterestWrapper edited = event.getAffectedItems().get(0);

                    if (!placesOfInterest.contains(edited.getPlaceOfInterest())) {

                        /* if a new place of interest item is added, we need to update the schema objects and
                         * the place of interest picker accordingly */
                        placesOfInterest.add(edited.getPlaceOfInterest());
                    }

                    loadAndFilterPlacesOfInterest();
                    
                } else if(ListAction.EDIT.equals(event.getActionPerformed())) {
                    
                    for(PlaceOfInterestWrapper wrapper : event.getAffectedItems()) {
                        SharedResources.getInstance().getEventBus().fireEvent(new PlaceOfInterestEditedEvent(wrapper.getPlaceOfInterest()));
                        ScenarioClientUtility.updatePlaceOfInterestReferences(
                                ScenarioClientUtility.getPlaceOfInterestName(wrapper.getPlaceOfInterest()));
                    }
                }

                ScenarioEventUtility.fireDirtyEditorEvent(ScenarioClientUtility.getPlacesOfInterest());
            }
        });
        
        filterPanel.addValueChangeHandler(new ValueChangeHandler<List<Class<?>>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<Class<?>>> event) {
                
                if(filterPanel.isVisible()) {
                    
                    itemEditor.setAuthorablePlaceTypes(event.getValue());
                    
                    //if the type filter is visible, update the list whenever its values are changed
                    loadAndFilterPlacesOfInterest();
                }
            }
        });
        
        createFromFileDialog.setAllowedFileExtensions(new String[] {".csv"});
        createFromFileDialog.getWidget().getElement().addClassName("placeOfInterestImportDialogBody");
        createFromFileDialog.setIntroMessageHTML(
                "Please select a CSV file to create places of interest from."
                + "The file format must one of the formats established by the following tables.<br/><br/>"
                + "For GDC coordinates:<br/>"
                + "<div style='text-align: center;'><div class='placeOfInterestImportHelp'>"
                + "<table>"
                    + "<tr>"
                        + "<td>annotation</td>"
                        + "<td>lat</td>"
                        + "<td>lon</td>"
                        + "<td>elevation</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>Place 1</td>"
                        + "<td>1</td>"
                        + "<td>2</td>"
                        + "<td>3</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Place 2</td>"
                        + "<td>4</td>"
                        + "<td>5</td>"
                        + "<td>6</td>"
                    + "</tr>"
                + "</table></div></div><br/>"
                + "For GCC coordinates:<br/>"
                + "<div style='text-align: center;'><div class='placeOfInterestImportHelp'>"
                + "<table>"
                    + "<tr>"
                        + "<td>annotation</td>"
                        + "<td>x</td>"
                        + "<td>y</td>"
                        + "<td>z</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>Place 1</td>"
                        + "<td>1</td>"
                        + "<td>2</td>"
                        + "<td>3</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Place 2</td>"
                        + "<td>4</td>"
                        + "<td>5</td>"
                        + "<td>6</td>"
                    + "</tr>"
                + "</table></div></div><br/>"
                + "For AGL coordinates:<br/>"
                + "<div style='text-align: center;'><div class='placeOfInterestImportHelp'>"
                + "<table>"
                    + "<tr>"
                        + "<td>annotation</td>"
                        + "<td>aglx</td>"
                        + "<td>agly</td>"
                        + "<td>aglelevation</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>Place 1</td>"
                        + "<td>1</td>"
                        + "<td>2</td>"
                        + "<td>3</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Place 2</td>"
                        + "<td>4</td>"
                        + "<td>5</td>"
                        + "<td>6</td>"
                    + "</tr>"
                + "</table></div></div>"
                + "<b>Note:</b> The first line of the CSV file must contain the column names used to identify the GDC/GCC/AGL values "
                + "in each row. That set of column names must contain, at a minimum, all four column names shown above for that coordinate type. "
                + "The order of the column names does not matter. All other columns will be ignored.<br/><br/>"
        );
        
        createFromFileButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(!ScenarioClientUtility.isReadOnly()) {
                    
                    //present a file selection dialog so the author can pick the file to create places of interest from
                    createFromFileDialog.center();
                }else{
                    WarningDialog.info("Unable to Add", "This real-time assessment is in read-only mode.");
                }
            }
        });

        setReadonly(ScenarioClientUtility.isReadOnly());

        // needs to be called last
        initValidationComposite(validations);
    }

    /**
     * Requests that the server parse the uploaded file at the given path to create places of interest based on the values
     * inside of it.
     * 
     * @param uploadFilePath the path to the uploaded file to be parsed
     * @param fileName the name of the file provided by the user.  Won't be null or empty.  Can be useful for display
     * purposes.  Also useful in case the server changed the uploaded file name.
     * @param callback a callback to invoke once the uploaded file has been handled
     * @param ignoreElevation whether elevation values found in the file should be ignored. If null, no places of interest will
     * be created, and the uploaded file will simply be cleaned up
     */
    private void createPOIsFromFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback, Boolean ignoreElevation) {
        
        SharedResources.getInstance().getRpcService().getPlacesOfInterestFromFile(GatClientUtility.getUserName(), uploadFilePath, ignoreElevation, new AsyncCallback<GenericRpcResponse<List<Serializable>>>() {
            
            @Override
            public void onSuccess(GenericRpcResponse<List<Serializable>> result) {
                
                if(result.getWasSuccessful()) {
                    
                    if(result.getContent() != null && !result.getContent().isEmpty()) {
                        
                        SafeHtmlBuilder successMessage = new SafeHtmlBuilder()
                        .appendHtmlConstant("The following places of interest were successfully created from the given file:")
                        .appendHtmlConstant("<ul style='max-height: 300px; overflow: auto;'>");
                        
                        //ensure that the names of the places of interest being created do not conflict with existing POIs
                        for(Serializable createdPoi : result.getContent()) {
                            
                            String placeName = ScenarioClientUtility.getPlaceOfInterestName(createdPoi);
                            String originalPlaceName = placeName;
                            int i = 1;
                            
                            while(ScenarioClientUtility.getPlaceOfInterestWithName(placeName) != null) {
                                
                                //an existing place of interest already has this name, so modify the name to make it unique
                                placeName = originalPlaceName + " (" + ++i + ")";
                                        
                            }
                            
                            if(i > 1) {
                                
                                //a conflict was detected, so modify the created POI
                                if(createdPoi instanceof Point) {
                                    ((Point) createdPoi).setName(placeName);
                                    
                                } else if(createdPoi instanceof Path) {
                                    ((Path) createdPoi).setName(placeName);
                                    
                                } else if(createdPoi instanceof Area) {
                                    ((Area) createdPoi).setName(placeName);
                                }
                            }
                            
                            //add the created places of interest to the global places of interest list
                            ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea().add(createdPoi);
                            
                            successMessage.appendHtmlConstant("<li style='font-weight: bold;'>")
                            .appendEscaped(placeName)
                            .appendHtmlConstant("</li>");
                        }
                    
                        ScenarioClientUtility.gatherPlacesOfInterestReferences();
                        
                        //display a dialog listing the names of all the places of interest that were created
                        ModalDialogBox successDialog = new ModalDialogBox();
                        successDialog.setCloseable(true);
                        successDialog.setGlassEnabled(true);
                        successDialog.setText("Places of Interest Imported");
                        successDialog.setWidget(new HTML(successMessage.toSafeHtml()));
                        
                        successDialog.center();
                    }else{
                        //display a dialog stating there were no places of interest created
                        ModalDialogBox successDialog = new ModalDialogBox();
                        successDialog.setCloseable(true);
                        successDialog.setGlassEnabled(true);
                        successDialog.setText("No Places of Interest Imported");
                        successDialog.setWidget(new HTML("There were no places of interest found in the provided file '"+fileName+"'.<br/>Is this a .csv file with the proper format?"));
                        
                        successDialog.center();
                    }
                    
                    createFromFileDialog.hide();
                    
                } else {
                    
                    final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            result.getException().getReason(),
                            result.getException().getDetails(), 
                            result.getException().getErrorStackTrace()
                    );              
                    
                    dialog.setText("Failed to Create Places of Interest");
                    dialog.center();
                    
                    createFromFileDialog.hide();
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                
                if(caught instanceof DetailedException) {
                    
                    DetailedException exception = (DetailedException) caught;
                    
                    final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            "An error occurred while creating places of interest from the uploaded file.", 
                            exception.getReason() + "\n" + exception.getDetails(), 
                            exception.getErrorStackTrace()
                    );              
                    
                    dialog.setText("Failed to Create Places of Interest");
                    
                    createFromFileDialog.hide();
                    
                } else {
                    callback.onFailure(caught);
                }
            }
        });
    }

    /**
     * Populates the panel using the data within the given {@link PlacesOfInterest}.
     * 
     * @param placesOfInterest the data object that will be used to populate the panel.
     */
    public void edit(PlacesOfInterest placesOfInterest) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + placesOfInterest + ")");
        }

        if (placesOfInterest == null) {
            throw new IllegalArgumentException("The 'placesOfInterest' parameter can't be null");
        }

        // initialize places of interest list display
        loadAndFilterPlacesOfInterest();
        
        setReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Builds the fields for the list of places of interest
     * 
     * @return the list of fields
     */
    private List<ItemField<PlaceOfInterestWrapper>> buildListFields() {
        
        List<ItemField<PlaceOfInterestWrapper>> fields = new ArrayList<>();
        
        fields.add(new ItemField<PlaceOfInterestWrapper>("Name", null) {
            @Override
            public Widget getViewWidget(PlaceOfInterestWrapper wrapper) {
                
                FlowPanel panel = new FlowPanel();
                
                Serializable item = wrapper.getPlaceOfInterest();
                
                Widget iconWidget = getPlaceIcon(item.getClass());
                
                if(iconWidget != null) {
                    panel.add(iconWidget);
                }
                
                String name = ScenarioClientUtility.getPlaceOfInterestName(item);
                
                InlineLabel label = new InlineLabel(name);
                label.setTitle(name);
                label.setWidth("150px");
                label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
                label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                label.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                label.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
                label.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
                
                if (!placesOfInterestMatchingTerms.isEmpty() && placesOfInterestMatchingTerms.contains(item)) {
                    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    label.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
                }
                
                panel.add(label);
                panel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                
                return panel;
            }
        });
       
        fields.add(new ItemField<PlaceOfInterestWrapper>("Location", "100%") {
            @Override
            public Widget getViewWidget(PlaceOfInterestWrapper wrapper) {
                
                Serializable item = wrapper.getPlaceOfInterest();
                
                if(item instanceof Point){

                    Point point = (Point)item;
                    if (point.getCoordinate() == null) {
                        point.setCoordinate(new Coordinate());
                    }

                    Serializable coordinateType;
                    if (point.getCoordinate().getType() == null) {
                        GCC gcc = new GCC();
                        gcc.setX(BigDecimal.ZERO);
                        gcc.setY(BigDecimal.ZERO);
                        gcc.setZ(BigDecimal.ZERO);
                        coordinateType = gcc;
                    }else {
                        coordinateType = point.getCoordinate().getType();
                    }

                    FlowPanel flowPanel = new FlowPanel();
                    flowPanel.getElement().getStyle().setProperty("margin", "0px -15px");

                    CoordinateType typeEnum = CoordinateType.getCoordinateTypeFromCoordinate(coordinateType);
                    
                    BigDecimal xValue;
                    BigDecimal yValue;
                    BigDecimal zValue;
                    
                    switch (typeEnum) {
                    case GCC:
                        GCC gcc = (GCC) coordinateType;
                        xValue = gcc.getX();
                        yValue = gcc.getY();
                        zValue = gcc.getZ();
                        break;
                    case GDC:
                        GDC gdc = (GDC) coordinateType;
                        xValue = gdc.getLongitude();
                        yValue = gdc.getLatitude();
                        zValue = gdc.getElevation();
                        break;
                    case AGL:
                        AGL agl = (AGL) coordinateType;
                        xValue = agl.getX();
                        yValue = agl.getY();
                        zValue = agl.getElevation();
                        break;
                    default:
                        throw new UnsupportedOperationException("The type '" + typeEnum + "' was unexpected.");
                    }
                    
                    if (xValue == null) {
                        xValue = BigDecimal.ZERO;
                    }
                    
                    if (yValue == null) {
                        yValue = BigDecimal.ZERO;
                    }
                    
                    if (zValue == null) {
                        zValue = BigDecimal.ZERO;
                    }

                    Icon icon = new Icon(typeEnum.getIconType());
                    icon.setSize(IconSize.LARGE);
                    flowPanel.add(icon);
                    
                    InlineHTML htmlLabel = new InlineHTML(typeEnum.name());
                    htmlLabel.getElement().getStyle().setPaddingLeft(5, Unit.PX);
                    htmlLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
                    flowPanel.add(htmlLabel);
                    
                    flowPanel.add(new BubbleLabel(typeEnum.buildXLabel(Float.valueOf(xValue.floatValue()))));
                    flowPanel.add(new BubbleLabel(typeEnum.buildYLabel(Float.valueOf(yValue.floatValue()))));
                    flowPanel.add(new BubbleLabel(typeEnum.buildZLabel(Float.valueOf(zValue.floatValue()))));

                    return flowPanel;
                    
                }else if(item instanceof Path){
                    
                    int count = ((Path) item).getSegment().size() + 1; //add 1, since 2 segments specify 3 points   
                    
                    Label label = new Label(count + " points");
                    label.getElement().getStyle().setProperty("margin", "0px -15px");
                    
                    return label;
                    
                }else if(item instanceof Area){
                    
                    int count = ((Area) item).getCoordinate().size(); 
                    
                    Label label = new Label(count + " points");
                    label.getElement().getStyle().setProperty("margin", "0px -15px");
                    
                    return label;
                    
                }else{
                    return new FlowPanel();
                }
            }
        });
        
        fields.add(new ItemField<PlaceOfInterestWrapper>("References", null) {
            
            @Override
            public Widget getViewWidget(PlaceOfInterestWrapper wrapper) {
            
                Serializable item = wrapper.getPlaceOfInterest();
                
                int refCount = 0;
                
                List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(item);
                
                if(references != null) {
                    for(PlaceOfInterestReference ref : references) {
                        refCount += ref.getReferenceCount();
                    }
                }
                
                Label label = new Label("" + refCount);
                label.setHorizontalAlignment(Label.ALIGN_CENTER);
                
                return label;
            }
        });

        return fields;
    }
    
    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        placesOfInterestList.setReadonly(isReadonly);
        createFromFileButton.setEnabled(!isReadonly);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(placesOfInterestList);
    }
    
    
    /**
     * Loads the list of places of interest from the underlying schema objects and filters it based on the search text entered into the
     * current place of interest picker (if applicable). If no search text has been entered, then all of the places of interest will be shown.
     */
    public void loadAndFilterPlacesOfInterest() {
        
        //cancel any editing, otherwise buttons in the list will be stuck in a disabled state
        placesOfInterestList.cancelEditing();
        
        String filterText = null;
        
        if(StringUtils.isNotBlank(poiSelect.getText())) {
            filterText = poiSelect.getText().toLowerCase();
        }

        List<Serializable> filteredPoiList = new ArrayList<>();

        // determine search terms in the filter based on whitespace
        String[] searchTerms = null;

        if (filterText != null) {
            searchTerms = filterText.split("\\s+");
        }

        placesOfInterestMatchingTerms.clear();
        
        //determine which types of places of interest should be shown
        List<Class<?>> filteredTypes = new ArrayList<Class<?>>(filterPanel.getValue());
        
        List<Serializable> placesOfInterestMissingTerms = new ArrayList<>();

        for (Serializable placeOfInterest : ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea()) {
            
            if(filteredTypes.contains(placeOfInterest.getClass())) {

                if (searchTerms != null) {
                    
                    boolean missingTerm = false;
                    
                    String placeName = ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest);
                    
                    //check if each place of interest contains the necessary search terms
                    for(int i = 0; i < searchTerms.length; i++) {
                        
                        if(placeName == null || !placeName.toLowerCase().contains(searchTerms[i])) {
                            missingTerm = true;
                            break;
                        }
                    }
                    
                    if(!missingTerm) {
                        placesOfInterestMatchingTerms.add(placeOfInterest);
                        
                    } else {
                        placesOfInterestMissingTerms.add(placeOfInterest);
                    }
                    
                } else {
                    placesOfInterestMissingTerms.add(placeOfInterest);
                }
            }
        }
        
        //populate the list of places of interest, with places of interest that match search terms listed first
        filteredPoiList.addAll(placesOfInterestMatchingTerms);
        filteredPoiList.addAll(placesOfInterestMissingTerms);
        
        List<PlaceOfInterestWrapper> wrappers = new ArrayList<>();
        
        for(Serializable placeOfInterest : filteredPoiList) {
            wrappers.add(new PlaceOfInterestWrapper(placeOfInterest));
        }
        
        placesOfInterestList.setItems(wrappers);
    }
    
    /**
     * Updates the place of interest list whenever the scenario's place of interest references change
     * 
     * @param event an event indicating that place of interest references changed
     */
    @EventHandler
    protected void onPlacesOfInterestReferencesUpdate(PlacesOfInterestReferencesUpdatedEvent event) {
        loadAndFilterPlacesOfInterest();
    }

    /**
     * Rebuilds the learner action table
     */
    public void rebuildPlacesOfInterestTable() {
        loadAndFilterPlacesOfInterest();
    }

    /**
     * Refreshes the row containing the provided place of interest.
     * 
     * @param placeOfInterest the place of interest
     */
    public void refreshPlacesOfInterest(Serializable placeOfInterest) {
        
        if(placeOfInterest != null) {
            for(PlaceOfInterestWrapper wrapper : placesOfInterestList.getItems()) {
                
                if(placeOfInterest.equals(wrapper.getPlaceOfInterest())) {
                    placesOfInterestList.refresh(wrapper);
                    return;
                }
            }
        }
    }
    
    /**
     * Resets the filter panel so that all types of places of interest are included, by default
     */
    private void resetTypeFilter() {
        filterPanel.setValue(Arrays.asList(new Class<?>[] {Point.class, Path.class, Area.class}));
        itemEditor.setAuthorablePlaceTypes(filterPanel.getValue());
    }
    
    /**
     * Gets the icon that should be used for places of interest with the given type
     * 
     * @param placeType the type of place to get an icon for
     * @return the icon corresponding to the given place
     */
    public static Widget getPlaceIcon(Class<?> placeType) {
        
        Widget iconWidget = null;
        String tooltipText = null;
        
        if(Point.class.equals(placeType)){ 
            
            Icon icon = new Icon(IconType.MAP_MARKER);
            icon.setSize(IconSize.LARGE);
            iconWidget = icon;
            
            tooltipText = "Point of interest";
            
        } else if(Path.class.equals(placeType)) {
            
            Image image = new Image("images/timeline.png");
            iconWidget = image;
            
            tooltipText = "Path of interest";
            
        } else if(Area.class.equals(placeType)) {
            
            Image image = new Image("images/area.png");
            iconWidget = image;
            
            tooltipText = "Area of interest";
        }
        
        if(iconWidget != null) {
            
            final Tooltip tooltip = new Tooltip(iconWidget);
            tooltip.setTitle(tooltipText);
            tooltip.setContainer("body");
            iconWidget.addAttachHandler(new AttachEvent.Handler() {

                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if(!event.isAttached()) {
                        tooltip.hide();
                    }
                }
                
            });
            
            iconWidget.setWidth("24px");
            iconWidget.getElement().getStyle().setTextAlign(TextAlign.CENTER);
            iconWidget.getElement().getStyle().setMarginRight(7, Unit.PX);
            iconWidget.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        }
        
        return iconWidget;
    }
}