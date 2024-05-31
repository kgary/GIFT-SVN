/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.CourseEventScoreWidget;
import mil.arl.gift.common.gwt.shared.HtmlGenerator;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.tools.dashboard.client.UiManager;

/**
 * The BsCourseHistoryWidget represents a single item in a list group in the MyStats screen
 * for the Course History List.  It displays the name, date, grade of the lms record of a specified user.
 * The widget is part of a LinkedGroup and is put into a LinkedGroupItem widget as it's parent.
 * 
 * @author nblomberg
 *
 */
public class BsCourseHistoryWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsCourseHistoryWidget.class.getName());
    
    private static BsCourseHistoryWidgetUiBinder uiBinder = GWT.create(BsCourseHistoryWidgetUiBinder.class);    
    
    interface BsCourseHistoryWidgetUiBinder extends UiBinder<Widget, BsCourseHistoryWidget> {
    }
    
    private static final String ABOVE_EXPECTATION_GRADE_HTML = HtmlGenerator.SPAN_DARK_GREEN + "<b>" + AssessmentLevelEnum.ABOVE_EXPECTATION.getDisplayName() + "</b>  " + HtmlGenerator.SPAN_END;
    private static final String AT_EXPECTATION_GRADE_HTML = HtmlGenerator.SPAN_GREEN + "<b>" + AssessmentLevelEnum.AT_EXPECTATION.getDisplayName() + "</b>  " + HtmlGenerator.SPAN_END;
    private static final String FAIL_GRADE_HTML = HtmlGenerator.SPAN_RED + "<b>" + AssessmentLevelEnum.BELOW_EXPECTATION.getDisplayName() + "</b>  " + HtmlGenerator.SPAN_END;
    private static final String INCOMPLETE_GRADE_HTML = HtmlGenerator.SPAN_ORANGE + "<b>" + AssessmentLevelEnum.UNKNOWN.getDisplayName() + "</b>  " + HtmlGenerator.SPAN_END;
    
    /** The HTML used to render the "Details" button in this widget's table */
    private static final SafeHtml DETAILS_BUTTON_HTML = SafeHtmlUtils.fromSafeConstant("<button class='btn btn-primary courseHistoryRowButton'><i class='fa fa-info-circle' aria-hidden='true'></i>Details</button>");
    
    @UiField
    HTML dateField;
    
    @UiField 
    Modal historyDetails;
    
    @UiField
    Button printButton;
    
    @UiField
    Heading dlgCourseName;
    
    @UiField
    Heading dlgCourseDate;
    
    @UiField
    Span dlgCourseGrade;
    
    
    @UiField
    FlowPanel dlgCourseGradeDetails;
    
    @UiField
    Label dlgCourseLms;
    
    @UiField(provided=true)
    protected CellTable<LMSCourseRecord> cellTable = new CellTable<LMSCourseRecord>();
    
    /** contains the records for a course execution */
    private ListDataProvider<LMSCourseRecord> tableDataProvider = new ListDataProvider<LMSCourseRecord>();

    /**
     * Render the name of this single record among the collection of records for a single course execution
     */
    private Column<LMSCourseRecord, SafeHtml> recordEntryNameColumn = new Column<LMSCourseRecord, SafeHtml>(new SafeHtmlCell()){

        @Override
        public SafeHtml getValue(LMSCourseRecord data){
        
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            
            //
            // First: get the root node name
            //
            GradedScoreNode candidateNode = data.getRoot();
            String name = data.getRoot().getName();
            if(name.equalsIgnoreCase(recordName)){
                // the name matches the course name, 
                // see if we can find a better name by looking at the next level in the score hierarchy
                
                List<AbstractScoreNode> children = candidateNode.getChildren();
                if(children.size() == 1){
                    //there is only 1 child under the root, get it's name
                    AbstractScoreNode candidateNodeObj = children.iterator().next();
                    name = candidateNodeObj.getName();
                }
            }
            
            sb.appendHtmlConstant("<div/>");
            sb.appendHtmlConstant("" + name + "");
            sb.appendHtmlConstant("</div>");
            
            return sb.toSafeHtml();
        }
    };

    /**
     * Render the grade column for this single record among the collection of records for a single course execution
     */
    private Column<LMSCourseRecord, SafeHtml> gradeColumn = new Column<LMSCourseRecord, SafeHtml>(new SafeHtmlCell()){

        @Override
        public SafeHtml getValue(LMSCourseRecord record){
            
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            
            //build HTML to be rendered here
            sb.appendHtmlConstant("<div/>");
            
            iconCourseGrade.setVisible(true);
            
            String gradeHtml = "";
            if (record.getRoot().getAssessment().equals(AssessmentLevelEnum.AT_EXPECTATION)) {
                iconCourseGrade.setType(IconType.SMILE_O);
                iconCourseGrade.setColor(HtmlGenerator.PASS_GREEN);
                gradeHtml =  AT_EXPECTATION_GRADE_HTML;
                
            }else if(record.getRoot().getAssessment().equals(AssessmentLevelEnum.ABOVE_EXPECTATION)) {
                iconCourseGrade.setType(IconType.SMILE_O);
                iconCourseGrade.setColor(HtmlGenerator.PASS_DARK_GREEN);
                gradeHtml =  ABOVE_EXPECTATION_GRADE_HTML;
                
            } else if (record.getRoot().getAssessment().isPoorPerforming()) {
                iconCourseGrade.setType(IconType.FROWN_O);
                iconCourseGrade.setColor(HtmlGenerator.FAIL_RED);                
                gradeHtml = FAIL_GRADE_HTML;
                
            } else {
                iconCourseGrade.setVisible(false);  //currently don't have an icon
                gradeHtml = INCOMPLETE_GRADE_HTML;
            }
                        
              sb.appendHtmlConstant(gradeHtml);
              sb.appendHtmlConstant(iconCourseGrade.toString());
              sb.appendHtmlConstant("</div>");
              
              return sb.toSafeHtml();
        }
    };
    
    /**
     * Render the details column for this single record among the collection of records for a single course execution
     */
    private Column<LMSCourseRecord, SafeHtml> detailsColumn = new Column<LMSCourseRecord, SafeHtml>(new SafeHtmlCell()){

        @Override
        public SafeHtml getValue(LMSCourseRecord record){              
        	return DETAILS_BUTTON_HTML;
        }
    };

    // the name of the course for this record
    private String recordName;
    
    // A dynamically created icon that displays the course grade.
    private Icon iconCourseGrade = null;
    
    // used to format the timestamp of the record
    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm a z";    
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(DATE_FORMAT);

    private static final String SCORECARD_LABEL = "Scorecard: ";
    private static final String OVERALL_GRADE_LABEL = "Overall Grade: ";
    private static final String LMS_LABEL = "Source : ";
    
    /**
     * Constructor
     */
    public BsCourseHistoryWidget(List<LMSCourseRecord> courseRecords) {

        initWidget(uiBinder.createAndBindUi(this));
        
        historyDetails.hide();
        
        tableDataProvider.getList().addAll(courseRecords);
        
        //just grab the first record's info
        recordName = courseRecords.get(0).getRoot().getName();
        Date date = courseRecords.get(0).getDate();        
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Creating the BsCourseHistoryWidget for record named "+recordName+", date "+date+" (browser session id: " + UiManager.getInstance().getSessionId() + ")");
        }
        
        iconCourseGrade = new Icon();

        cellTable.addColumn(gradeColumn);
        cellTable.setColumnWidth(gradeColumn, "25%");
        
        cellTable.addColumn(recordEntryNameColumn);
        cellTable.setColumnWidth(recordEntryNameColumn, "100%");
        
        cellTable.addColumn(detailsColumn);
        
        cellTable.setRowStyles(new RowStyles<LMSCourseRecord>() {
			
			@Override
			public String getStyleNames(LMSCourseRecord row, int rowIndex) {
				return "courseHistoryRowStyle";
			}
		});
        
        tableDataProvider.addDataDisplay(cellTable);

        cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LMSCourseRecord>(){

            @Override
            public void onCellPreview(CellPreviewEvent<LMSCourseRecord> event){
                
                if(BrowserEvents.CLICK.equals(event.getNativeEvent().getType())){
                    //perform row click logic here using the cell context information in event.getContext()
                    
                    LMSCourseRecord record = event.getValue();
                    showCourseHistoryDetailsPopup(record);
                }
            }

        });
    
        dateField.setText(dateFormat.format(date));   
    }   
    
    /**
     * Return the name for this collection of records.  This is normally
     * the GIFT course name.
     * 
     * @return the name of the record used to label this collection or records
     */
    public String getRecordName(){
        return recordName;
    }
    
    /**
     * Display the course history details popup based on a course history widget that was selected.
     * 
     * @param widget - The course history widget that was selected (this widget contains the lms course record details).
     */
    private void showCourseHistoryDetailsPopup(LMSCourseRecord record) {
        // Populate the modal dialog with the selected item details.

        dlgCourseName.setText(SCORECARD_LABEL + record.getRoot().getName());
        
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(DATE_FORMAT);
        dlgCourseDate.setText(dateFormat.format(record.getDate()));

        dlgCourseGrade.clear();
        String gradeHtml = "";
        if (record.getRoot().getAssessment().hasReachedStandards()) {
            iconCourseGrade.setType(IconType.SMILE_O);
            iconCourseGrade.setColor(HtmlGenerator.PASS_GREEN);
            gradeHtml =  HtmlGenerator.SPAN_GREEN + record.getRoot().getAssessment().getDisplayName() + "  " + HtmlGenerator.SPAN_END;
            
        } else if (record.getRoot().getAssessment().isPoorPerforming()) {
            iconCourseGrade.setType(IconType.FROWN_O);
            iconCourseGrade.setColor(HtmlGenerator.FAIL_RED);
            
            gradeHtml = HtmlGenerator.SPAN_RED + record.getRoot().getAssessment().getDisplayName() + "  " + HtmlGenerator.SPAN_END;
            
        } else {
            iconCourseGrade.setVisible(false);
        }
        
        dlgCourseGrade.setHTML(OVERALL_GRADE_LABEL + gradeHtml);
        // Once we set the HTML, we need to re-add the icon widget to the span.
        dlgCourseGrade.add(iconCourseGrade);
        
        dlgCourseLms.setHTML(LMS_LABEL + "<i>" + record.getLMSConnectionInfo().getName() + "</i>");

        dlgCourseGradeDetails.clear();
        final CourseEventScoreWidget scoreWidget = new CourseEventScoreWidget(record.getRoot()); 
        dlgCourseGradeDetails.add(scoreWidget);
        historyDetails.show();
        
        //click handler for the button to open new temporary window to print scorecard
        printButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                //grab the HTML of all the inner score card components as a string to pass into print function
                String innerhtml = dlgCourseName.getElement().getInnerHTML() + "<br>" + dlgCourseGrade.getElement().getInnerHTML() + 
                        "<br>" + dlgCourseDate.getElement().getInnerHTML() + "<hr>" + scoreWidget.getPrintableScoreDetails() + 
                        "<hr>" + dlgCourseLms.getElement().getInnerHTML(); 
               JsniUtility.printhtml(innerhtml);
            }
        });
        
    }
}
