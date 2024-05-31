/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.client.WidgetFactory;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.data.CourseListResponse;
import mil.arl.gift.tutor.shared.data.GwtDomainOption;
import mil.arl.gift.tutor.shared.properties.SelectDomainWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * Widget for selecting a domain
 *
 * @author jleonard
 */
public class SelectDomainWidget extends Composite implements RequiresResize, ProvidesResize {
    
    private static Logger logger = Logger.getLogger(SelectDomainWidget.class.getName());
    
    private final StackLayoutPanel stackLayoutPanel = new StackLayoutPanel(Unit.PX);
    
    private static final String RECOMMENDED_LABEL = "Currently Recommended Courses";
    private static final String AVAILABLE_LABEL = "Other Available Courses";
    private static final String NOT_RECOMMENEDED_LABEL = "Refresher Courses";
    private static final String UNAVAILABLE_LABEL = "Unavailable";
    
    private static final String RECOMMENDED_ICON = "images/Recommended.png";
    private static final String AVAILABLE_ICON = "images/NoRecommendation.png";
    private static final String NOT_RECOMMENDED_ICON = "images/NotRecommended.png";
    private static final String UNAVAILABLE_ICON ="images/Unavailable.png";
    
    private static final String RECOMMENDED_CSS = "recommendedHeader";
    private static final String AVAILABLE_CSS = "otherCoursesHeader";
    private static final String NOT_RECOMMENDED_CSS = "notRecommendedHeader";
    private static final String UNAVAILABLE_CSS = "unavailableHeader";
    
    /**
     * Selection models for the course cell groupings
     */
    private final SingleSelectionModel<GwtDomainOption> recommendedSelectionModel = new SingleSelectionModel<GwtDomainOption>(keyProvider);
    private final SingleSelectionModel<GwtDomainOption> otherSelectionModel = new SingleSelectionModel<GwtDomainOption>(keyProvider);
    private final SingleSelectionModel<GwtDomainOption> notRecommendedSelectionModel = new SingleSelectionModel<GwtDomainOption>(keyProvider);
    private final SingleSelectionModel<GwtDomainOption> unavailableSelectionModel = new SingleSelectionModel<GwtDomainOption>(keyProvider);
    
    /**
     * The course data that can be selected under each course cell grouping
     */
    private final ListDataProvider<GwtDomainOption> recommendedCoursesData = new ListDataProvider<GwtDomainOption>();
    private final ListDataProvider<GwtDomainOption> otherCoursesData = new ListDataProvider<GwtDomainOption>();
    private final ListDataProvider<GwtDomainOption> notRecommendedCoursesData = new ListDataProvider<GwtDomainOption>();
    private final ListDataProvider<GwtDomainOption> unavailableCoursesData = new ListDataProvider<GwtDomainOption>();
    
    /**
     * The label to use if the cell grouping has no courses in it
     */
    private final HTML emptyRecommendedListLabel = new HTML();
    private final HTML emptyOtherListLabel = new HTML();
    private final HTML emptyNotRecommendedListLabel = new HTML();
    private final HTML emptyUnavailableListLabel = new HTML();

    /**
     * The navigation buttons for this widget
     */
    private final Button logoutButton = new Button("Logout");
    private final Button startSelectedCourseButton = new Button("Start Selected Course");
    
    /**
     * Progress indicator used by each stack panel
     */
    private final Image recommendedProgressIndicator = new Image("images/loading.gif");
    private final Image otherProgressIndicator = new Image("images/loading.gif");
    private final Image notRecommendedProgressIndicator = new Image("images/loading.gif");
    private final Image unavailableProgressIndicator = new Image("images/loading.gif");

    /**
     * Constructor
     *
     * @param instance The web page instance of the select domain page
     */
    public SelectDomainWidget(final WidgetInstance instance) {
        
        History.newItem(TutorUserWebInterface.SELECT_DOMAIN_TAG, false);
        final WidgetProperties properties = instance.getWidgetProperties();
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(15);
        this.initWidget(verticalPanel);
        verticalPanel.setWidth("350px");
        
        recommendedProgressIndicator.setStyleName("courseListProgressImage");
        otherProgressIndicator.setStyleName("courseListProgressImage");
        notRecommendedProgressIndicator.setStyleName("courseListProgressImage");
        unavailableProgressIndicator.setStyleName("courseListProgressImage");

        Label availableCoursesLabel = new Label("My Courses");
        availableCoursesLabel.setStyleName("availableCoursesLabel");
        verticalPanel.add(availableCoursesLabel);

        final AsyncCallback<RpcResponse> logoutRequestCallback = new AsyncCallback<RpcResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                logoutButton.setEnabled(true);
                startSelectedCourseButton.setEnabled(true);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                logoutButton.setEnabled(true);
                startSelectedCourseButton.setEnabled(true);
                Document.getInstance().setFooterWidget(null);
            }
        };
        
        logoutButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                logoutButton.setEnabled(false);
                startSelectedCourseButton.setEnabled(false);
                if (BrowserSession.getInstance() != null) {
                    BrowserSession.getInstance().logout(logoutRequestCallback);
                } else {
                    Document.getInstance().displayError("Logging out", "The browser session is invalid");
                    try {
                        //change the login widget type to the type that was used to get to the login page
                        Document.getInstance().setArticleWidget(WidgetFactory.createWidgetType(BrowserSession.getLoginType()));
                    } catch (@SuppressWarnings("unused") Exception ex) {
                        Document.getInstance().displayError("Logging out", "Cannot create the login widget");
                    }
                }
            }
        });
        
        BrowserSession.getInstance().getActiveDomainSessionName(new AsyncCallback<String>() {
        	@Override
        	public void onFailure(Throwable caught) {
        		//TODO: Display an error that the active domain session name can not be retrieved
        	}

        	@Override
        	public void onSuccess(String result) {
        		if (result != null) {
        			SelectDomainWidgetProperties.setActiveDomainSessionName(properties, result);
        			verticalPanel.add(new ResumeSessionWidget(instance));
        		} else {
        			displayDomainSelectionList(verticalPanel, null);
        		}
        	}
        });

    }
    
    /**
     * The cell used to render a GwtDomainOption
     * 
     * @author cnucci
     */
    private static class CourseCell extends AbstractCell<GwtDomainOption> {
        
        @Override
        public void render(Context context, GwtDomainOption value, SafeHtmlBuilder sb) {
            
            if(value != null) {
                sb.appendHtmlConstant("<div class=\"courseCell\">");
                sb.appendHtmlConstant("<div class=\"courseName\">");
                sb.appendEscaped(value.getDomainName());
                
                sb.appendHtmlConstant("<br>").appendEscaped(value.getDomainId());
                
                sb.appendHtmlConstant("</div><div class=\"courseDescription\">");
                if(value.getDescription() == null){
                    sb.appendEscaped("Not provided");
                }else{
                    
                    try{
                        sb.appendHtmlConstant(value.getDescription());
                    }catch(@SuppressWarnings("unused") IllegalArgumentException e){
                        //the description is not a valid HTML syntax, use the original logic to just append the text
                        sb.appendEscaped(value.getDescription());
                    }
                }
                
                sb.appendHtmlConstant("</div>");
                if(value.getRecommendationMessage() != null) {
                    sb.appendHtmlConstant("<div class=\"courseRecommendation\">");
                    sb.appendEscaped(value.getRecommendationMessage());
                    sb.appendHtmlConstant("</div>");
                }
                
                sb.appendHtmlConstant("</div>");
            }
        }
    }
    
    /**
     * Provides a key for list items, such that items that are to be treated as distinct have distinct keys.
     */
    private static final ProvidesKey<GwtDomainOption> keyProvider = new ProvidesKey<GwtDomainOption>() {
        
        @Override
        public Object getKey(GwtDomainOption item) {
            return item == null ? null : item.getDomainId();
        }
    };

    /**
     * Display the list of options for domain sessions
     *
     * @param root The widget to add further widgets to
     * @param domainOptions The list of domain options of the domain module
     */
    private void displayDomainSelectionList(CellPanel root, ArrayList<GwtDomainOption> domainOptions) {
        
    	stackLayoutPanel.setHeight("450px");
    	stackLayoutPanel.onResize();
    	stackLayoutPanel.setWidth("100%");
    	
        root.add(stackLayoutPanel);
        
        addToStack(stackLayoutPanel, RECOMMENDED_LABEL, RECOMMENDED_ICON, RECOMMENDED_CSS, recommendedCoursesData, recommendedSelectionModel, emptyRecommendedListLabel, recommendedProgressIndicator);
        addToStack(stackLayoutPanel, AVAILABLE_LABEL, AVAILABLE_ICON, AVAILABLE_CSS, otherCoursesData, otherSelectionModel, emptyOtherListLabel, otherProgressIndicator);
        addToStack(stackLayoutPanel, NOT_RECOMMENEDED_LABEL, NOT_RECOMMENDED_ICON, NOT_RECOMMENDED_CSS, notRecommendedCoursesData, notRecommendedSelectionModel, emptyNotRecommendedListLabel, notRecommendedProgressIndicator);
        addToStack(stackLayoutPanel, UNAVAILABLE_LABEL, UNAVAILABLE_ICON, UNAVAILABLE_CSS , unavailableCoursesData, unavailableSelectionModel, emptyUnavailableListLabel, unavailableProgressIndicator);

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        root.add(horizontalPanel);
        horizontalPanel.setWidth("100%");
        horizontalPanel.getElement().getStyle().setProperty("marginTop", "25px");
        root.setCellWidth(horizontalPanel, "100%");

        horizontalPanel.add(logoutButton);
        horizontalPanel.add(startSelectedCourseButton);
        horizontalPanel.setCellHorizontalAlignment(startSelectedCourseButton, HasHorizontalAlignment.ALIGN_RIGHT);
        logoutButton.setWidth("200px");
        logoutButton.setStyleName("btn-primary buttonPadding");
        startSelectedCourseButton.setWidth("200px");
        startSelectedCourseButton.setEnabled(false);
        startSelectedCourseButton.setStyleName("btn-primary buttonPadding");

        if (domainOptions != null) {
            logger.info("[SelectDomainWidget] Using domain options in properties");
            populateOptions(domainOptions);
            setEmptyListLabel("There are no courses in this section", false);
        } else {

            setEmptyListLabel("Getting course list", true);

            logger.info("[SelectDomainWidget] Domain options not in properties, asking server");
            BrowserSession.getInstance().getDomainOptions(new AsyncCallback<CourseListResponse>() {
                @Override
                public void onFailure(Throwable caught) {
                    startSelectedCourseButton.setEnabled(false);
                    
                    setEmptyListLabel("<font color=\"red\">A problem occurred while retrieving the course list</font>", false);
                }

                @Override
                public void onSuccess(CourseListResponse result) {
                    if (result != null) {
                        
                        if(result.getCourses() == null){
                            setEmptyListLabel("<font color=\"red\">"+result.getErrorReason()+"<br><font color=\"black\">Details:</font> "+result.getErrorDetails()+"</font>", false);

                        }else{
                            populateOptions(result.getCourses());
                            
                            setEmptyListLabel("There are no courses in this section", false);
    
                            startSelectedCourseButton.setEnabled(true);
                        }
                    } else {
                        
                        setEmptyListLabel("<font color=\"red\">A problem occurred while retrieving the course list</font>", false);
                    }
                }
            });
        }

        //This callback is used to handle the response of starting a course
        final AsyncCallback<RpcResponse> responseCallback = new AsyncCallback<RpcResponse>() {
            
            @Override
            public void onFailure(Throwable caught) {
                BrowserSession.getInstance().displaySelectDomainWidget();
                logoutButton.setEnabled(true);
                startSelectedCourseButton.setEnabled(true);
            }

            @Override
            public void onSuccess(RpcResponse result) {
             
                if(!result.isSuccess()){
                    //there was a problem (not related to RPC) handling the select domain request.
                    //A detailed error dialog should have already been presented to the user
                    
                    logoutButton.setEnabled(true);
                    startSelectedCourseButton.setEnabled(true);
                }
            }
        };
        
        //This is needed in case the user clicks on an 'unavailable' course which will cause the start
        //button to be disabled
        SelectionChangeEvent.Handler enableSelectionHandler = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                
                if(logoutButton.isEnabled()){
                    startSelectedCourseButton.setEnabled(true);
                }
            }
         };
        
        recommendedSelectionModel.addSelectionChangeHandler(enableSelectionHandler);
        otherSelectionModel.addSelectionChangeHandler(enableSelectionHandler);
        notRecommendedSelectionModel.addSelectionChangeHandler(enableSelectionHandler);
        
        //Disable the start course button when the user selects an 'unavailable' course
        unavailableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                startSelectedCourseButton.setEnabled(false);
            }
         });

        startSelectedCourseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent handling with multiple rapid clicks of the button
                if(startSelectedCourseButton.isEnabled()){
                    
                    final GwtDomainOption selectedDomain;
                    switch(stackLayoutPanel.getVisibleIndex()) {
                    case 0:
                        selectedDomain = recommendedSelectionModel.getSelectedObject();
                        break;
                    case 1:
                        selectedDomain = otherSelectionModel.getSelectedObject();
                        break;
                    case 2:
                        selectedDomain = notRecommendedSelectionModel.getSelectedObject();
                        break;
                    default:
                        selectedDomain = null;
                        break;
                    }
    
                    if(selectedDomain != null){
                        logoutButton.setEnabled(false);
                        startSelectedCourseButton.setEnabled(false);
                        
                        //handle the selected domain separately to allow the components to be disabled, visually, faster
                        //ticket 863
                        Timer timer = new Timer(){
                            @Override
                            public void run(){
                                BrowserSession.getInstance().userSelectDomain(selectedDomain.getDomainId(), selectedDomain.getDomainId(), responseCallback);
                            }
            
                        };
            
                        timer.schedule(500);
                    }
                }
            }
        });
    }
    
    /**
     * Populate the various course lists with the message provided.
     * 
     * @param message the text to shown in the course list
     * @param isTemporaryText whether the message is temporary until the server responds with information
     * to replace it
     */
    private void setEmptyListLabel(String message, boolean isTemporaryText) {
        emptyRecommendedListLabel.setHTML(message);
        emptyOtherListLabel.setHTML(message);
        emptyNotRecommendedListLabel.setHTML(message);
        emptyUnavailableListLabel.setHTML(message);
        
        //hide the progress indicator as the stack panels are no longer awaiting a server response
        recommendedProgressIndicator.setVisible(isTemporaryText);
        otherProgressIndicator.setVisible(isTemporaryText);
        notRecommendedProgressIndicator.setVisible(isTemporaryText);
        unavailableProgressIndicator.setVisible(isTemporaryText);
    }
    
    /**
     * Add a course list cell grouping to the stack layout panel.
     * 
     * @param stackLayoutPanel - the panel to add the new cell grouping too
     * @param headerText - the label to show on the cell header bar
     * @param headerImage - the image to show to the left of the header label
     * @param headerStyle - the name of the style to apply to the header
     * @param data - the list of items to select under the header
     * @param selectionModel - the selection model for the items under the header
     * @param emptyListLabel - the text to show under the header by default.
	 * @param progressIndicator - the progress indicator image to show when the stack is waiting for a response from the server
     */
    private static void addToStack(StackLayoutPanel stackLayoutPanel, 
            String headerText, String headerImage, String headerStyle, 
            ListDataProvider<GwtDomainOption> data, SingleSelectionModel<GwtDomainOption> selectionModel, 
            Label emptyListLabel, Image progressIndicator) {
        
        CellList<GwtDomainOption> cellList = new CellList<GwtDomainOption>(new CourseCell(), keyProvider);
        
        //setup the label and image on the same line
        HorizontalPanel panel = new HorizontalPanel();
        emptyListLabel.setStyleName("courseListPlaceholderText");
        panel.add(emptyListLabel);
        panel.add(progressIndicator);
        
        cellList.setEmptyListWidget(panel);
        cellList.setSelectionModel(selectionModel);
        data.addDataDisplay(cellList);
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(cellList);
        Widget header = createHeaderWidget(headerText, headerImage);
        if(headerStyle != null && !headerStyle.isEmpty()){
            header.addStyleName(headerStyle);
        }

        stackLayoutPanel.add(scrollPanel, header, 40);
    }
    
    /**
     * Create the header widget for one of the course cell groupings.
     * 
     * @param text the label to show on the cell header bar
     * @param image the image to show to the left of the header label
     * @return Widget the created widget with the given components
     */
    private static Widget createHeaderWidget(String text, String image) {
        
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setHeight("100%");
        hPanel.setSpacing(0);
        hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Image img = new Image(image);
        hPanel.add(img);
        hPanel.setCellVerticalAlignment(img, HorizontalPanel.ALIGN_MIDDLE);
        HTML headerText = new HTML("&nbsp;" + text);
        headerText.addStyleName("cw-StackPanelHeader");
        headerText.addStyleName("commonCourseHeaderText");
        hPanel.add(headerText);
        return new SimplePanel(hPanel);
    }

    /**
     * Populates the domain list with options to select
     *
     * @param options The domain options list
     */
    private void populateOptions(final ArrayList<GwtDomainOption> options) {
        
        recommendedCoursesData.getList().clear();
        otherCoursesData.getList().clear();
        notRecommendedCoursesData.getList().clear();
        unavailableCoursesData.getList().clear();

        for (GwtDomainOption i : options) {
            String recommendation = i.getRecommendationEnum();
            if(recommendation == null){
                otherCoursesData.getList().add(i);
            }else {
                if(recommendation.equals("Recommended")){
                    recommendedCoursesData.getList().add(i);
                }else if(recommendation.equals("NotRecommended")){
                    notRecommendedCoursesData.getList().add(i);
                }else{
                    unavailableCoursesData.getList().add(i);
                }
            }
        }

        if(!recommendedCoursesData.getList().isEmpty()) {
            stackLayoutPanel.showWidget(0);
            recommendedSelectionModel.setSelected(recommendedCoursesData.getList().get(0), true);
        } else if(!otherCoursesData.getList().isEmpty()) {
            stackLayoutPanel.showWidget(1);
            otherSelectionModel.setSelected(otherCoursesData.getList().get(0), true);
        } else if(!notRecommendedCoursesData.getList().isEmpty()) {
            stackLayoutPanel.showWidget(2);
            notRecommendedSelectionModel.setSelected(notRecommendedCoursesData.getList().get(0), true);
        }
        
        //Show error dialog if there are no options
        if(options.isEmpty()){
            Document.getInstance().displayDialog("Retrieving Courses", "GIFT was unable to find a single course to present to you.<br><br>" +
            		"If you believe this a mistake please start by checking the latest domain module log for issues in GIFT\\output\\logger\\module\\.");
        }

    }
    
    /** Adjusts the height of the Available Courses panel */
    @Override
    public void onResize() {

    	int height;
    	int min = 200;
        int max = 600;
    	
        height = Window.getClientHeight() - min;
            
        // keep the height between 200 and 600 pixels
        height = (height < min) ? min : (height > max) ? max : height;
            
        stackLayoutPanel.setHeight(height + "px");
    }
}
