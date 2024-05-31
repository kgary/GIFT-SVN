/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import mil.arl.gift.common.course.CourseFileAccessDetails.CourseFileUserPermissionsDetails;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * A table that displays the users that are currently accessing a given course
 * 
 * @author tflowers
 *
 */
public class CollaboratorsTableModal extends Composite {

    interface CollaboratorsTableModalUiBinder extends UiBinder<Widget, CollaboratorsTableModal> {
    }

    private static CollaboratorsTableModalUiBinder uiBinder = GWT.create(CollaboratorsTableModalUiBinder.class);

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(CollaboratorsTableModal.class.getName());

    @UiField
    protected Modal rootModal;

    @UiField
    protected CellTable<CourseFileUserPermissionsDetails> accessCellTable;

    /**
     * The data provider that contains the list of users who currently have the
     * course open in the GAT
     */
    private ListDataProvider<CourseFileUserPermissionsDetails> accessCellTableProvider = new ListDataProvider<>();

    /**
     * The message that is shown when there are no other users editing the
     * course
     */
    private static final String EMPTY_TABLE_MSG = "<p style='margin: 8px;'>There are no other users collaborating on this course at this time.</p>";

    /** The name of the user who has opened the course */
    private final String USERNAME = GatClientUtility.getUserName();

    /**
     * The unique identifier for the browser that the user is currently
     * accessing
     */
    private final String browserSessionKey = GatClientUtility.getBrowserSessionKey();

    /** The comparator used to sort the collaborators within the table */
    private final Comparator<CourseFileUserPermissionsDetails> userPermissionComparator = new Comparator<CourseFileUserPermissionsDetails>() {

        @Override
        public int compare(CourseFileUserPermissionsDetails o1, CourseFileUserPermissionsDetails o2) {

            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            if (o1.hasWritePermissions() != o2.hasWritePermissions()) {
                return o1.hasWritePermissions() ? -1 : 1;
            } else {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        }
    };

    /**
     * The constructor that initializes the widgets and saves the browser
     * session key when it becomes avialable
     */
    public CollaboratorsTableModal() {
        logger.fine(".ctor()");
        initWidget(uiBinder.createAndBindUi(this));
        initTable();
    }

    /**
     * Hides the modal
     */
    public void hide() {
        rootModal.hide();
    }

    /**
     * Sets the list of users who are currently accessing the course
     * 
     * @param newList the new list of users who are accessing the course
     */
    public void setAccessList(List<CourseFileUserPermissionsDetails> newList) {

        logger.fine("setAccessList({" + StringUtils.join(", ", newList) + "})");

        // Remove self from the access list before updating the list
        Iterator<CourseFileUserPermissionsDetails> iter = newList.iterator();
        while (iter.hasNext()) {
            CourseFileUserPermissionsDetails next = iter.next();
            if (USERNAME.equals(next.getUsername()) && browserSessionKey.equals(next.getBrowserSessionKey())) {
                iter.remove();
                break;
            }
        }

        Collections.sort(newList, userPermissionComparator);

        accessCellTableProvider.setList(newList);
        accessCellTableProvider.refresh();
    }

    /**
     * Displays the modal
     */
    public void show() {
        logger.fine("show()");
        rootModal.show();
    }

    /**
     * Initializes the widgets associated with the table
     */
    private void initTable() {
        logger.fine("initTable()");
        accessCellTableProvider.addDataDisplay(accessCellTable);

        // Sets the widget that is shown if there are no additional
        // collaborators
        HTMLPanel htmlPanel = new HTMLPanel(SafeHtmlUtils.fromTrustedString(EMPTY_TABLE_MSG));
        accessCellTable.setEmptyTableWidget(htmlPanel);

        accessCellTable.addColumn(new Column<CourseFileUserPermissionsDetails, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(CourseFileUserPermissionsDetails permissions) {
                String username = permissions.getUsername();
                String browser = permissions.getBrowserSessionKey();
                boolean sameUsername = username.equals(USERNAME);
                boolean sameBrowser = browser.equals(browserSessionKey);

                if (sameUsername && !sameBrowser) {
                    // Build the help icon to inform the user why their name is
                    // appearing in the table
                    Icon helpIcon = new Icon(IconType.QUESTION_CIRCLE);
                    helpIcon.setMarginLeft(4);
                    helpIcon.setTitle(
                            "You have this course opened in another browser, perhaps even on another computer. "
                                    + "If you have edit permissions and want to obtain the write lock, "
                                    + "be the first user to open the course.");
                    return new SafeHtmlBuilder().appendEscaped(username)
                            .append(SafeHtmlUtils.fromTrustedString(helpIcon.toString())).toSafeHtml();
                } else {
                    return SafeHtmlUtils.fromString(username);
                }
            }

        }, "Username");

        accessCellTable.addColumn(new Column<CourseFileUserPermissionsDetails, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(CourseFileUserPermissionsDetails details) {
                // Define constants for determing what units to measure the time
                // span in
                final long SECONDS = 1000;
                final long MINUTES = SECONDS * 60;
                final long HOURS = MINUTES * 60;
                final long DAYS = HOURS * 24;

                // Calculate how to display the time span. Use the coarsest
                // units possible so that the time span is expressed in a
                // reasonable choice of units
                long time = System.currentTimeMillis() - details.getLockGrantedTime();
                String timespan;
                if (time > DAYS) {
                    timespan = (time / DAYS) + " day(s)";
                } else if (time > HOURS) {
                    timespan = (time / HOURS) + " hour(s)";
                } else if (time > MINUTES) {
                    timespan = (time / MINUTES) + " minute(s)";
                } else if (time > SECONDS) {
                    timespan = (time / SECONDS) + " second(s)";
                } else {
                    timespan = time + " millisecond(s)";
                }

                // Build a span that displays the time span but displays the
                // time stamp within the tooltip
                SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
                htmlBuilder.append(SafeHtmlUtils.fromTrustedString("<span title='"));

                DateTimeFormat formatter = DateTimeFormat.getFormat("dd MMMM yyyy hh:mm aaa");
                Date dateTime = new Date(details.getLockGrantedTime());
                htmlBuilder.appendEscaped(formatter.format(dateTime));

                htmlBuilder.append(SafeHtmlUtils.fromTrustedString("'>"));
                htmlBuilder.appendEscaped(timespan);
                htmlBuilder.append(SafeHtmlUtils.fromTrustedString("</span>"));
                return htmlBuilder.toSafeHtml();
            }
        }, "Access Time");

        accessCellTable.addColumn(new TextColumn<CourseFileUserPermissionsDetails>() {

            @Override
            public String getValue(CourseFileUserPermissionsDetails permissions) {
                return permissions.getPermissions().getDisplayName();
            }

        }, "Permission");
    }
}