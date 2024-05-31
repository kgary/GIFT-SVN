/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.CourseValidationResults.CourseObjectValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.util.StringUtils;

/**
 * An extension of {@link ModalDialogBox} that formats and presents 
 * exception details in a user-friendly manner.
 * 
 * @author bzahid
 */
public abstract class DetailsDialogBox extends ModalDialogBox { 
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(DetailsDialogBox.class.getName());
      
    /** The download button */
	protected Button downloadButton;
	
	/** The reason for the exception */
	protected String reason;
	
	/** Map that links the course object name to the course object validation results. */
	protected Map<String, CourseObjectValidationResults> courseObjectToErrorDetails;
	
	/** The course error details for the exception */
	protected List<ErrorDetails> courseErrorDetails = new ArrayList<ErrorDetails>(1);
	
	/** (optional) name of the course the error is associated with.  This is printed to the downloadable file. */
	private String courseName;
	
	/** The date */
	private Date date = new Date();

	/** The registration for adding a click handler */
	private HandlerRegistration handlerRegistration;
	
	/** The content */
	private static VerticalPanel contentHeader = new VerticalPanel();
	
	/** Help string */
	private static final HTML whatsThisBody = new HTML("<html><font size=\"3\" face=\"Open Sans\" color=\"#666666\">A stack trace is used by software developers to find the logic which caused this" 
            + " message dialog to appear. Normally stack traces are presented only when errors happen and not, for example, " 
            + "as part of validating forms where the author is providing information. The difference is that GIFT is an " 
            + "open source framework used by various users with different roles from a course designer to a software developer.<br><br>" 
            + "By exposing this level of information, a non-software developer can report details of an issue to a software developer " 
            + "(e.g. possibly using the forums on <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\" style=\"color: #0022EE\">gifttutoring.org</a>)" 
            + " that are normally located in one of GIFT's log files on the server, while at the same time a software developer can quickly reference the source mentioned in the stack trace to understand " 
            + "the possible problem.</font></html>");
	
	/** "What's This" style dialog that will be shown if the user clicks the question mark icon next to the desired element */
	private static final HelpLink whatsThis = new HelpLink();
	
	/** Icon for the help link */
	private static final Icon whatsThisIcon = new Icon(IconType.QUESTION_CIRCLE);
	
	static{
		whatsThis.setHelpCaption("Stack Trace");
		whatsThisIcon.setColor("#6464c8");
		whatsThis.setHelpLink(whatsThisIcon);       

        whatsThis.setHelpBody(whatsThisBody);
        whatsThis.getElement().getStyle().setProperty("cursor", "default");
	}
	
	/**
	 * Initializes an empty error dialog box.
	 */
	public DetailsDialogBox() {
		super();
		initComponents();
	}

    /**
     * Creates an error dialog box with expandable details and an optional stack trace.<br\>
     * Note: used to display a single error
     * 
     * @param reason The user-friendly message about the error.
     * @param errorDetails contains a single error with details and optional stack trace. Can be null.
     * @param courseName the name of the course causing the error. Optional if the error is not
     *            related to a course. Can be null but not empty.
     */
    public DetailsDialogBox(String reason, ErrorDetails errorDetails, String courseName) {
        super();        
        
        this.reason = reason;
        
        if(errorDetails != null){
            this.courseErrorDetails.add(errorDetails);
        }
        
        setCourseName(courseName);
        
        initComponents();
        setWidget(createErrorContent(reason, errorDetails)); 
    }
	
	/**
     * Creates an error dialog box with expandable details and a stack trace.
     * 
     * @param reason The user-friendly message about the group of course validation errors. 
     * @param courseValidationResults the course validation results that include one or more issues to display.
     * @param courseName the name of the course causing the error.  Optional if the error is not related to a course.  Can be null but not empty.
     * @param isSingleObject whether the validation issue is for a course object (true) or the entire course/file (false)
     * @param helpMessage custom opening help text for the dialog. If null, a default message will be used.
     */
    public DetailsDialogBox(String reason, CourseValidationResults courseValidationResults, String courseName, boolean isSingleObject, String helpMessage) {
        
        super();
        
        this.reason = reason;
        
        Map<String, CourseObjectValidationResults> courseObjectMap = new HashMap<String, CourseValidationResults.CourseObjectValidationResults>();
        if (courseValidationResults.getCourseObjectResults() != null) {
            for (CourseObjectValidationResults courseObject : courseValidationResults.getCourseObjectResults()) {
                courseObjectMap.put(courseObject.getCourseObjectName(), courseObject);
            }
        }
        this.courseObjectToErrorDetails = courseObjectMap;
        this.courseErrorDetails = buildCourseErrorDetails(courseValidationResults);
        
        setCourseName(courseName);
        
        initComponents();
        
        if (isSingleObject) {
            List<CourseObjectValidationResults> validationResults = new ArrayList<CourseObjectValidationResults>();
            validationResults.addAll(courseObjectToErrorDetails.values());
            setWidget(createCourseObjectErrorContent(validationResults));
        } else {
            // Need to pass in a GIFT validation result, not a child of it, so do a shallow copy of the issues.
            GIFTValidationResults giftResults = new GIFTValidationResults();
            giftResults.setCriticalIssue(courseValidationResults.getCriticalIssue());
            giftResults.addWarningIssues(courseValidationResults.getWarningIssues());
            giftResults.addImportantIssues(courseValidationResults.getImportantIssues());
            
            setWidget(createCourseErrorContent(giftResults, courseObjectToErrorDetails, helpMessage));
        }
    }
	
	/**
	 * Initializes the ui components
	 */
	private void initComponents() {
		
		setCloseable(true);
		setGlassEnabled(true);

		downloadButton = new Button("Save to File");	
		downloadButton.setTitle("Save the contents of this dialog to a zip file.  Useful for sharing the details with others.");
		downloadButton.setType(ButtonType.PRIMARY);
		downloadButton.setIcon(IconType.DOWNLOAD);
		handlerRegistration = downloadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				startDownload();
			}
		});
		
		setFooterWidget(downloadButton);
	}

    /**
     * Removes the download button from this dialog. Once this method is called,
     * the button cannot be restored and a new dialog instance will need to be
     * created. Since {@link #startDownload()} is an abstract method, it will
     * still need to be implemented, but the content will not matter anymore
     * because it will never get executed.
     */
    public void removeDownloadButton() {
        downloadButton.removeFromParent();
    }

    /**
     * Builds the course object details message from the given course validation results.
     * 
     * @param courseValidationResults the course validation results that contains the course and
     *            course object issue messages.  Can't be null.
     * @return the collection of course and course object validation issues sorted by most critical issues first.  Each issue
     * has additional severity labeling in the details attributes.
     */
	protected List<ErrorDetails> buildCourseErrorDetails(CourseValidationResults courseValidationResults) {
	
	    List<ErrorDetails> errorDetailsList = new ArrayList<ErrorDetails>();
	    
	    logger.info("Building course error details...");
	    
	    if (courseValidationResults == null) {
	        return errorDetailsList;
	    }
	    	    
	    
        // append GIFT validation course issues
        if (courseValidationResults.hasCriticalIssueIgnoreCourseObjects()) {
            DetailedExceptionSerializedWrapper exceptionWrapper = courseValidationResults.getCriticalIssue();
            if(exceptionWrapper != null){
                errorDetailsList.add(
                        new ErrorDetails("Course Issue [critical]: " + exceptionWrapper.getReason()+" "+exceptionWrapper.getDetails(), 
                                exceptionWrapper.getSerializedStackTrace()));
            }
        }
	    
        if (courseValidationResults.hasImportantIssuesIgnoreCourseObjects()) {
            for (DetailedExceptionSerializedWrapper importantDetail : courseValidationResults.getImportantIssues()) {
                errorDetailsList.add(
                        new ErrorDetails("Course Issue [important]: " + importantDetail.getReason()+" "+importantDetail.getDetails(),
                                importantDetail != null ? importantDetail.getSerializedStackTrace() : null));
            }
        }

	    if (courseValidationResults.hasWarningIssuesIgnoreCourseObjects()) {
            for (DetailedExceptionSerializedWrapper warningDetail : courseValidationResults.getWarningIssues()) {
                errorDetailsList.add(
                        new ErrorDetails("Course Issue [warning]: " + warningDetail.getReason()+" "+warningDetail.getDetails(),
                                warningDetail != null ? warningDetail.getSerializedStackTrace() : null));
            }
        }
	    
	    // append course object issues
        if (courseValidationResults.getCourseObjectResults() != null) {
            
            logger.info("appending course object issues...");
            
            for (CourseObjectValidationResults courseObject : courseValidationResults.getCourseObjectResults()) {
                GIFTValidationResults results = courseObject.getValidationResults();
                if (results != null && results.hasIssues()) {
                    if (results.hasCriticalIssue()) {
                        errorDetailsList.add(
                                new ErrorDetails(courseObject.getCourseObjectName() + " [critical]: " + results.getCriticalIssue().getReason()+" "+results.getCriticalIssue().getDetails(),
                                        results.getCriticalIssue() != null ? results.getCriticalIssue().getSerializedStackTrace() : null));
                    }

                    if (results.hasImportantIssues()) {
                        for (DetailedExceptionSerializedWrapper importantDetail : results.getImportantIssues()) {
                            errorDetailsList.add(
                                    new ErrorDetails(courseObject.getCourseObjectName() + " [important]: " + importantDetail.getReason()+" "+importantDetail.getDetails(),
                                            importantDetail != null ? importantDetail.getSerializedStackTrace() : null));
                        }
                    }

                    if (results.hasWarningIssues()) {
                        for (DetailedExceptionSerializedWrapper warningDetail : results.getWarningIssues()) {
                            errorDetailsList.add(
                                    new ErrorDetails(courseObject.getCourseObjectName() + " [warning]: " + warningDetail.getReason()+" "+warningDetail.getDetails(),
                                            warningDetail != null ? warningDetail.getSerializedStackTrace() : null));
                        }
                    }
                }
            }
        }
        
        logger.info("Finished building course error details.");
        
        return errorDetailsList;
	}
	
	/**
	 * Logic to execute when the download button is clicked
	 */
	public abstract void startDownload();
	
	/**
	 * Displays a success dialog informing the user that their error report was created.
	 * 
	 * @param downloadUrl The download url of the error report
	 * @param closeHandler The CloseHandler to add to the dialog
	 */
	public void showSuccessDialog(String downloadUrl, CloseHandler<PopupPanel> closeHandler) {
		ModalDialogBox successDialog = new ModalDialogBox();
		
		successDialog.setText("Error Report Generated!");
		successDialog.setWidget(new HTML("An error report file has been successfully created. "
				+ "<br/><br/>If your download doesn't start in a few seconds, please <a href="
				+ "'" + downloadUrl + "' target='_self'>click here</a> to start the download."));
		
		successDialog.setCloseable(true);
		successDialog.setGlassEnabled(true);   //Note: this will glass everything under this dialog, for z-indexing values
		                                       //when using this with the GAT checkout tools.authoring.gat.war.GiftAuthoringTool.css (search for "z-index")
		successDialog.center();
		
		// attempt to download the file automatically
		Window.open(downloadUrl, "_self", "");
		
        if(closeHandler != null) {
            successDialog.addCloseHandler(closeHandler);
        }
    }
		
	/**
	 * Displays a failure dialog informing the user that their error report could not be created.
	 * 
	 * @param errorMessage A user friendly message explaining why the report generation failed.
	 */
	public void showFailureDialog(String errorMessage) {
		
		ModalDialogBox failDialog = new ModalDialogBox();
		failDialog.setText("Failed to Create Log File");
		failDialog.setWidget(new HTML("Failed to create the log file. " + errorMessage));
		failDialog.setCloseable(true);
		failDialog.setGlassEnabled(true);     //Note: this will glass everything under this dialog, for z-indexing values
                                              //when using this with the GAT checkout tools.authoring.gat.war.GiftAuthoringTool.css (search for "z-index")
		failDialog.center();
	}
	
    /**
     * Creates an expandable panel
     * 
     * @param content The content to add to the expandable panel
     * @param headerWidget widget that can be placed in the header, usually a "what's this?" icon
     *        that produces an appropriate modal when clicked.
     * @param headerIcon the icon to display above the expandable panel
     * @param headerText The text to display above the expandable panel
     * @param open Whether or not the panel should be expanded initially
     * @return the panel that was created
     */
	private static DisclosurePanel createExpandableWidget(Widget content, final Widget headerWidget, String headerIcon, String headerText, boolean open) {
		
		DisclosurePanel collapsiblePanel = new DisclosurePanel();
		collapsiblePanel.getElement().getStyle().setProperty("marginTop", "5px");
		collapsiblePanel.getElement().getStyle().setProperty("marginLeft", "15px");
        FlowPanel headerPanel = new FlowPanel();

        final Icon arrow = new Icon(open ? IconType.CHEVRON_DOWN : IconType.CHEVRON_RIGHT);
        headerPanel.add(arrow);

        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        if (headerIcon != null) {
            htmlBuilder.appendHtmlConstant(" ").appendHtmlConstant(headerIcon);
        }

        htmlBuilder.appendHtmlConstant(" ").appendHtmlConstant(headerText);
        headerPanel.add(new InlineHTML(htmlBuilder.toSafeHtml()));

        collapsiblePanel.add(content);
		collapsiblePanel.setOpen(open);
		
		// If there is a headerWidget (such as a "what's this?" question mark icon), add DomHandler to prevent
		//collapsible panels from interacting with clicks on the headerWidget
		if(headerWidget != null){
			headerWidget.addDomHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					headerWidget.getElement().getStyle().setProperty("cursor", "default");
					event.stopPropagation();
				}
			}, ClickEvent.getType());
			
			headerPanel.add(headerWidget);
		}
		
		// Open and close handlers to update the arrow icon next to the panel headers
        collapsiblePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> arg0) {
                arrow.setType(IconType.CHEVRON_DOWN);
            }
        });

        collapsiblePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
            @Override
            public void onClose(CloseEvent<DisclosurePanel> arg0) {
                arrow.setType(IconType.CHEVRON_RIGHT);
            }
        });
		
		collapsiblePanel.setHeader(headerPanel);
		return collapsiblePanel;
	}
	
	/** 
	 * Sets the header text of the dialog.
	 * 
	 * @param header - the header text to display
	 */
	public void setDialogTitle(String header) {
		super.setText(header);
	}
	
	/**
	 * Set the widget that is placed in the content header of the dialog.
	 * This will clear out any existing widgets in the content header panel.
	 * 
	 * @param widget The widget to display in the header
	 */
	public void setContentHeader(Widget widget){
	    contentHeader.clear();
	    contentHeader.add(widget);
	}
	
    /**
     * Creates and formats the contents of the dialog.
     * 
     * @param courseErrors
     *            the course errors that aren't associated with any specific
     *            course object.
     * @param courseObjectDetails
     *            the course object validation details.
     * @param helpMessage a helpful message to show at the top of the dialog. If null,
     *            a default message will be shown
     * @return the contents of the dialog.
     */
    public static ScrollPanel createCourseErrorContent(GIFTValidationResults courseErrors, Map<String, CourseObjectValidationResults> courseObjectDetails, String helpMessage) {

        // set width and height to 75% of the screen
        String width;
        if (Window.getClientWidth() * .75 < 825) {
            width = "825px";
        } else {
            width = Double.toString(Window.getClientWidth() * .75) + "px";
        }

        String height;
        if (Window.getClientHeight() * .75 < 300) {
            height = "300px";
        } else {
            height = Double.toString(Window.getClientHeight() * .75) + "px";
        }
        
        String contentWidth = "770px";

        ScrollPanel scrollPanel = new ScrollPanel();
        VerticalPanel content = new VerticalPanel();
        VerticalPanel expandables = new VerticalPanel();
                
        // style everything
        content.clear();
        content.setWidth(width);

        content.setWidth("90%");
        
        if(courseErrors != null && courseErrors.hasIssues()) {
            expandables.add(createPanelForGIFTValidationResults(courseErrors, "<b>Course Errors</b>", getReportIcon(), true));
        }
        
        if (courseObjectDetails != null && !courseObjectDetails.isEmpty()) {
            VerticalPanel courseObjectGroupWrapper = new VerticalPanel();

            // add courses to courseObjectGroupWrapper
            for (Entry<String, CourseObjectValidationResults> eachCourse : courseObjectDetails.entrySet()) {
                LinkedList<String> authoredBranchAncestortNames = eachCourse.getValue().getAuthoredBranchAncestortNames();
                GIFTValidationResults errorDetails = eachCourse.getValue().getValidationResults();
                String icon = eachCourse.getValue().getCourseObjectIcon();
                
                // build the collapse header text - place any nested course objects (e.g. authored branch course objects) names
                // ahead of the course object that has validation issues. (e.g. X > Y > Z where Z is the course object with an issue)
                String headerText = eachCourse.getKey();
                if(authoredBranchAncestortNames != null && !authoredBranchAncestortNames.isEmpty()){
                    headerText = StringUtils.join(" <b>&gt;</b> ", authoredBranchAncestortNames)  + " <b>&gt;</b> <b>" + headerText + "</b";
                }else{
                    headerText = eachCourse.getKey();
                }
                if (errorDetails != null && errorDetails.hasIssues()) {
                    courseObjectGroupWrapper.add(createPanelForGIFTValidationResults(errorDetails, headerText, getIconFromName(icon), false));
                }
            }
            
            if (courseObjectGroupWrapper.getWidgetCount() == 0) {
                HTML htmlLabel = new HTML("No Course Object Validation Issues.");
                htmlLabel.getElement().getStyle().setProperty("wordWrap", "break-word");
                htmlLabel.getElement().getStyle().setProperty("margin", "5px 0px 5px 7px");
                htmlLabel.setWidth(contentWidth);
                courseObjectGroupWrapper.add(htmlLabel);
            }
            
            expandables.add(createExpandableWidget(courseObjectGroupWrapper, null, getTreeIcon(), "<b>Course Object Errors</b>", true));
        }
        
        scrollPanel.setSize(width, height);
        scrollPanel.getElement().getStyle().setProperty("margin", "-15px");
        content.getElement().getStyle().setProperty("margin", "15px");

        // should never happen since this panel is only displayed if there is a validation error, but just in case...
        if (expandables.getWidgetCount() == 0) {
            HTML htmlLabel = new HTML("No Course Validation Issues.");
            htmlLabel.getElement().getStyle().setProperty("wordWrap", "break-word");
            htmlLabel.getElement().getStyle().setProperty("margin", "5px 0px 5px 7px");
            htmlLabel.setWidth(contentWidth);
            expandables.add(htmlLabel);
        }
        
        contentHeader.clear();
        content.add(contentHeader);
        
        if(helpMessage != null) {
            
            HTML htmlLabel1 = new HTML(helpMessage);
            content.add(htmlLabel1);
        
        } else {
            
            HTML htmlLabel1 = new HTML("Below is a list of one or more validation issues that were found in the course.");
            HTML htmlLabel2 = new HTML("If you need additional help, please create a new thread in the forums on gifttutoring.org.");
            content.add(htmlLabel1);
            content.add(htmlLabel2);
        }
        
        content.add(expandables);
        
        scrollPanel.setWidget(content);
        
        return scrollPanel;
    }
    
    /**
     * Creates the panel for each GIFTValidationResults entry
     * 
     * @param validationResults
     *            the validation results to display
     * @param headerText
     *            header text for the disclosure panel
     * @param headerIcon
     *            the header icon for the disclosure panel
     * @param startExpanded
     *            true to start with the disclosure panel open; false to have it
     *            start closed.
     * @return the Disclosure Panel
     */
    private static DisclosurePanel createPanelForGIFTValidationResults(GIFTValidationResults validationResults, String headerText, String headerIcon, boolean startExpanded) {
        VerticalPanel issueWrapper = new VerticalPanel();
        VerticalPanel criticalIssuePanel = new VerticalPanel();
        VerticalPanel importantIssuePanel = new VerticalPanel();
        VerticalPanel warningIssuePanel = new VerticalPanel();
        boolean hasCritical = validationResults.hasCriticalIssue();
        boolean hasImportant = validationResults.hasImportantIssues();
        if (hasCritical) {
            criticalIssuePanel.add(createExpandableWidget(createDetailsPanel(validationResults.getCriticalIssue()), null, null, validationResults.getCriticalIssue().getReason(), false));
            criticalIssuePanel.getElement().getStyle().setProperty("marginBottom", "15px");
            issueWrapper.add(createExpandableWidget(criticalIssuePanel, null, getCriticalIcon(), "Critical Issues", true));
        }
        if (hasImportant) {
            for (DetailedExceptionSerializedWrapper importantDetail : validationResults.getImportantIssues()) {
                importantIssuePanel.add(createExpandableWidget(createDetailsPanel(importantDetail), null, null, importantDetail.getReason(), false));
            }
            importantIssuePanel.getElement().getStyle().setProperty("marginBottom", "15px");
            issueWrapper.add(createExpandableWidget(importantIssuePanel, null, getImportantIcon(), "Important Issues", !hasCritical));
        }
        if (validationResults.hasWarningIssues()) {
            for (DetailedExceptionSerializedWrapper warningDetail : validationResults.getWarningIssues()) {
                warningIssuePanel.add(createExpandableWidget(createDetailsPanel(warningDetail), null, null, warningDetail.getReason(), false));
            }
            issueWrapper.add(createExpandableWidget(warningIssuePanel, null, getWarningIcon(), "Warning Issues", !(hasCritical || hasImportant)));
        }
        
        return createExpandableWidget(issueWrapper, null, headerIcon, headerText, startExpanded);
    }
    
	/**
     * Creates and formats the contents of the dialog.<br/>
     * Note: this currently only supports a single error.  See {@link #createCourseErrorContent(GIFTValidationResults, Map)}
     * and {@link #createCourseObjectErrorContent(List)} for how to present multiple errors in this dialog.
     * 
     * @param reason The user-friendly message about the error.
     * @param errorDetails The developer-friendly message about the error.
     * 
     * @return the contents of the dialog.
     */
    public static ScrollPanel createErrorContent(final String reason, final ErrorDetails errorDetails) {

        String width = "825px";
        String contentWidth = "770px";
        String stackTraceWidth = "1500px";

        boolean color = true;
        HTML detailsLabel = new HTML();
        ScrollPanel scrollPanel = new ScrollPanel();
        VerticalPanel content = new VerticalPanel();
        VerticalPanel expandables = new VerticalPanel();
        VerticalPanel detailsWrapper = new VerticalPanel();
        VerticalPanel stackTracePanel = new VerticalPanel();
        VerticalPanel stackTraceWrapper = new VerticalPanel();
        HTML reasonLabel  = new HTML(DocumentUtil.convertToHtmlString(reason));
        
        // style everything
        content.clear();
        content.setWidth(width);
        detailsLabel.getElement().getStyle().setProperty("wordWrap", "break-word"); 
        detailsLabel.getElement().getStyle().setProperty("margin", "5px 0px 15px 7px");     
        stackTraceWrapper.getElement().getStyle().setProperty("marginTop", "8px");
        stackTracePanel.setWidth(stackTraceWidth);
        reasonLabel.setWidth(contentWidth);
        reasonLabel.getElement().getStyle().setProperty("fontSize", "15px");

        content.setWidth("90%");
        detailsLabel.setWidth(contentWidth);
        scrollPanel.setSize(width, "300px");
        scrollPanel.getElement().getStyle().setProperty("margin", "-15px");
        content.getElement().getStyle().setProperty("margin", "15px");
        reasonLabel.getElement().getStyle().setProperty("margin", "6px 0px 15px 5px");  

        detailsLabel.setHTML(DocumentUtil.convertToHtmlString(errorDetails.getDetails()));
        
        // format the stack trace, include a "what's this" helpLink in header of panel        
        
        if(errorDetails.getStacktrace() != null && !errorDetails.getStacktrace().isEmpty()) {
            
            for (String e : errorDetails.getStacktrace()) {

                HTML label = new HTML();

                if (e.startsWith("at ")) {

                    // indent the stack trace
                    label.getElement().getStyle().setProperty("padding", " 3px 0px 3px 30px");

                    // alternate row colors for readability
                    label.getElement().getStyle().setBackgroundColor(color ? "#FAFAFA" : "#F6FEFE");

                    stackTracePanel.add(label);

                } else {
                    label.getElement().getStyle().setProperty("margin", "0px 0px 3px 7px");
                    label.setWidth(contentWidth);
                    stackTracePanel.add(label);
                }

                label.setText(e);
                color = !color;
            }

        } else {
            
            HTML label = new HTML();
            
            label.getElement().getStyle().setProperty("margin", "0px 0px 3px 7px");
            label.setWidth(contentWidth);
            stackTraceWrapper.add(label);
            
            label.setText("Stack trace unavailable.");
        }
        
        stackTraceWrapper.add(stackTracePanel);
        
        // place the stack trace within the details panel
        detailsWrapper.add(detailsLabel);
        detailsWrapper.add(createExpandableWidget(stackTraceWrapper, whatsThis, null, "Stack Trace ", false));
        expandables.add(createExpandableWidget(detailsWrapper, null, null, "Details", true));
        
        contentHeader.clear();
        content.add(contentHeader);
        content.add(reasonLabel);
        content.add(expandables);
        
        scrollPanel.setWidget(content);
        
        return scrollPanel;
    }
    
    /**
     * Creates and formats the contents of the dialog.
     * 
     * @param courseObjectDetails the course object validation details.
     * @return the scroll panel that was created
     */
	public static ScrollPanel createCourseObjectErrorContent(List<CourseObjectValidationResults> courseObjectDetails) {

        // set width and height to 75% of the screen
        String width;
        if (Window.getClientWidth() * .75 < 825) {
            width = "825px";
        } else {
            width = Double.toString(Window.getClientWidth() * .75) + "px";
        }

        String height;
        if (Window.getClientHeight() * .75 < 300) {
            height = "300px";
        } else {
            height = Double.toString(Window.getClientHeight() * .75) + "px";
        }
        
		String contentWidth = "770px";

		ScrollPanel scrollPanel = new ScrollPanel();
		VerticalPanel content = new VerticalPanel();
		VerticalPanel expandables = new VerticalPanel();
				
		// style everything
		content.clear();
		content.setWidth(width);

		content.setWidth("90%");
		
		// add courses to courseObjectWrapper; should only have 1 entry in the map
        VerticalPanel courseObjectWrapper = new VerticalPanel();
        for (CourseObjectValidationResults eachCourse : courseObjectDetails) {
            GIFTValidationResults errorDetails = eachCourse.getValidationResults();
            if (errorDetails != null && errorDetails.hasIssues()) {
                VerticalPanel criticalIssuePanel = new VerticalPanel();
                VerticalPanel importantIssuePanel = new VerticalPanel();
                VerticalPanel warningIssuePanel = new VerticalPanel();
                boolean hasCritical = errorDetails.hasCriticalIssue();
                boolean hasImportant = errorDetails.hasImportantIssues();
                if (hasCritical) {
                    criticalIssuePanel.add(createExpandableWidget(createDetailsPanel(errorDetails.getCriticalIssue()), null, null, errorDetails.getCriticalIssue().getReason(), false));
                    criticalIssuePanel.getElement().getStyle().setProperty("marginBottom", "15px");
                    courseObjectWrapper.add(createExpandableWidget(criticalIssuePanel, null, getCriticalIcon(), "Critical Issues", true));
                }
                if (hasImportant) {
                    for (DetailedExceptionSerializedWrapper importantDetail : errorDetails.getImportantIssues()) {                        
                        importantIssuePanel.add(createExpandableWidget(createDetailsPanel(importantDetail), null, null, importantDetail.getReason(), false));
                    }
                    importantIssuePanel.getElement().getStyle().setProperty("marginBottom", "15px");
                    courseObjectWrapper.add(createExpandableWidget(importantIssuePanel, null, getImportantIcon(), "Important Issues", !hasCritical));
                }
                if (errorDetails.hasWarningIssues()) {
                    for (DetailedExceptionSerializedWrapper warningDetail : errorDetails.getWarningIssues()) {
                        warningIssuePanel.add(createExpandableWidget(createDetailsPanel(warningDetail), null, null, warningDetail.getReason(), false));
                    }
                    courseObjectWrapper.add(createExpandableWidget(warningIssuePanel, null, getWarningIcon(), "Warning Issues", !(hasCritical || hasImportant)));
                }
            }
        }
        
        scrollPanel.setSize(width, height);
        scrollPanel.getElement().getStyle().setProperty("margin", "-15px");
        content.getElement().getStyle().setProperty("margin", "15px");

        if (courseObjectWrapper.getWidgetCount() == 0) {
            HTML htmlLabel = new HTML("Course Object Details unavailable.");
            htmlLabel.getElement().getStyle().setProperty("wordWrap", "break-word");
            htmlLabel.getElement().getStyle().setProperty("margin", "5px 0px 5px 7px");
            htmlLabel.setWidth(contentWidth);
            courseObjectWrapper.add(htmlLabel);
        }

        expandables.add(courseObjectWrapper);
		
		contentHeader.clear();
		content.add(contentHeader);
		content.add(expandables);
		
		scrollPanel.setWidget(content);
		
		return scrollPanel;
	}
    
    /**
     * Creates the details panel which can include the stack trace.
     * 
     * @param exceptionWrapper the wrapper that contains the exception and the pretty stack trace to
     *            log
     * @return the closable panel
     */
    private static VerticalPanel createDetailsPanel(DetailedExceptionSerializedWrapper exceptionWrapper) {
        VerticalPanel vertPanel = new VerticalPanel();
        vertPanel.getElement().getStyle().setProperty("marginTop", "5px");

        StringBuilder labelBuilder = new StringBuilder();
        
        labelBuilder.append(exceptionWrapper.getDetails()).append("\n\n");

        List<String> stackTrace = exceptionWrapper.getSerializedStackTrace();
        if (stackTrace != null && !stackTrace.isEmpty()) {
            for (String e : stackTrace) {
                if (e.startsWith("at ")) {
                    // indent the stack trace
                    labelBuilder.append("\t");
                }

                labelBuilder.append(e).append("\n");
            }
        } else {
            labelBuilder.append("Stack trace unavailable.");
        }

        vertPanel.add(new HTML(DocumentUtil.convertToHtmlString(labelBuilder.toString())));
        return vertPanel;
    }
	
	/**
	 * Replaces the download button click handler with a new one.
	 * 
	 * @param downloadHandler The new click handler to execute when the download button is clicked.
	 */
	public void setNewDownloadHandler(ClickHandler downloadHandler) {
		if(handlerRegistration != null) {
			handlerRegistration.removeHandler();
		}
		
		handlerRegistration = downloadButton.addClickHandler(downloadHandler);
	}
	
	/**
     * Helper method to determine if a dialog is showing the same data as another set of
     * title, reason, details.
     * 
     * @param title The title to compare the dialog with.
     * @param errorReason The error reason to compare the dialog with.
     * @param errorDetails The error details to compare the dialog with.
     * @return true if the dialog is already showing the same data, false otherwise.
     */
    public boolean isSameDialog(String title, String errorReason, String errorDetails) {
        boolean isSameDialog = false;
        
        if (Objects.equals(getTitle(), title) &&
                Objects.equals(this.reason, errorReason)) {
        	
        	if(this.courseErrorDetails.size() == 1 && Objects.equals(this.courseErrorDetails.get(0).getDetails(), errorDetails)) {
        		// there is only 1 detail in this dialog and it matches the provided details value
        		isSameDialog = true;
        	}else if(this.courseErrorDetails.isEmpty() && StringUtils.isBlank(errorDetails)) {
        		// there is no detail in this dialog and the provided details was also null/empty
        		isSameDialog = true;
        	}

        }
        
        return isSameDialog;
    }
    
    /**
     * Retrieves the icon from the name
     * 
     * @param iconName
     *            the name
     * @return the icon
     */
    private static String getIconFromName(String iconName) {
        if (iconName == null) {
            return null;
        }
        
        return "<img src=\"" + iconName + "\" height=\"32\" width=\"32\" />";
    }
    
    /**
     * Retrieves the tree icon
     * 
     * @return the tree icon
     */
    private static String getTreeIcon() {
        return "<img src=\"images/tree.png\" height=\"32\" width=\"32\" />";
    }
    
    /**
     * Retrieves the report icon
     * 
     * @return the report icon
     */
    private static String getReportIcon() {
        return "<img src=\"images/report.png\" height=\"32\" width=\"32\" />";
    }
    
    /**
     * Retrieves the critical icon
     * 
     * @return the critical icon
     */
    private static String getCriticalIcon() {
        return "<img src=\"images/stop.png\" height=\"16\" width=\"16\" />";
    }
    
    /**
     * Retrieves the important icon
     * 
     * @return the important icon
     */
    private static String getImportantIcon() {
        return "<img src=\"images/Alert.png\" height=\"16\" width=\"16\" />";
    }
    
    /**
     * Retrieves the warning icon
     * 
     * @return the warning icon
     */
    private static String getWarningIcon() {
        return "<img src=\"images/warning.png\" height=\"16\" width=\"16\" />";
    }
    
    /**
     * Get the name of the course the error happened with.
     * 
     * @return  can be null (but not empty) if the error is not associated with a course.
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Set the name of the course the error happened in.
     * 
     * @param courseName can be null (but not empty) if the error is not associated with a course.
     */
    public void setCourseName(String courseName) {
        
        if(courseName != null && courseName.isEmpty()){
            throw new IllegalArgumentException("The course name can't be empty.");
        }
        
        this.courseName = courseName;
    }
    
    /**
     * Get the date when this error object was created.
     * 
     * @return the date when the error was created.
     */
    public Date getDate(){
        return date;
    }
}
