/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;


import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.team.TeamMember;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Strong;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to handle selecting/joining/hosting team sessions prior to starting a training application such as VBS3.
 * 
 * @author nblomberg
 *
 */
public class UserSessionWidget extends Composite  {

    /** Ui binder interface. */
    interface UserSessionWidgetUiBinder extends UiBinder<Widget, UserSessionWidget> {
    }
    
    /**
     * A callback used to kick a team member when an appropriate button is clicked
     * 
     * @author nroberts
     */
    public static interface KickMemberCallback {
        
        /**
         * Kicks the given member from their team session
         * 
         * @param member the member to kick
         */
        public void kickMember(SessionMember member);
    }
        
    /** Instance of the ui binder. */
    private static UserSessionWidgetUiBinder uiBinder = GWT.create(UserSessionWidgetUiBinder.class);
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(UserSessionWidget.class.getName());
    
    /** The session member data associated with this widget. */
    SessionMember memberData = null;
    
    /** Controls the style of the div for the non local user. */
    private static final String DEFAULT_STYLE = "teamSessionUser";
    
    /** Controls the style of the div for a local user. */
    private static final String LOCAL_USER_STYLE = "teamSessionUserLocal";
    
    /** Label for an unassigned user. */
    private static final String UNASSIGNED_LABEL = "---";
    
    /** Tooltip for the host. */
    private static final String HOST_TOOLTIP = "HOST";

    /** Type of user this widget represents. */
    public enum UserType {
        HOST,
        USER
    }
    
        
    /** Is the local user. */
    boolean isLocalUser = false;
    
    @UiField
    Icon userTypeIcon;
    
    @UiField
    Strong userName;
    
    @UiField
    Strong teamRole;
    
    @UiField
    Row userRow;
    
    @UiField
    Tooltip iconTooltip;
    
    @UiField
    Icon readyIcon;
    
    /**
     * Constructor 
     * 
     * @param memberData  The session member data associated with this widget. Cannot be null.
     * @param type The type of user that the widget represents such as (HOST or normal USER).
     * @param isLocalUser Indicates if the user is the local user.
     * @param isHost whether this user session widget should represent the host
     */
    public UserSessionWidget(final SessionMember memberData, UserType type, boolean isLocalUser, final boolean isHost) {
        this(memberData, type, isHost, isHost, null);
    }

    /**
     * Constructor 
     * 
     * @param memberData  The session member data associated with this widget. Cannot be null.
     * @param type The type of user that the widget represents such as (HOST or normal USER).
     * @param isLocalUser Indicates if the user is the local user.
     * @param isHost whether this user session widget should represent the host
     * @param onKickCommand the callback to be invoked to kick this user session from its team session
     */
    public UserSessionWidget(final SessionMember memberData, UserType type, boolean isLocalUser, final boolean isHost, final KickMemberCallback onKickCallback) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("UserSessionWidget()");
        }
        initWidget(uiBinder.createAndBindUi(this));

        this.isLocalUser = isLocalUser;
        
        addAttachHandler(new AttachEvent.Handler() {
            
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                
                if(!event.isAttached()) {
                    iconTooltip.hide(); //prevent tooltip from getting stuck if this widget is detached
                }
            }
        });

        // Only local users can assign their role.
        if (!isLocalUser) {
            userRow.addStyleName(DEFAULT_STYLE);
        } else {
            userRow.addStyleName(LOCAL_USER_STYLE);
        }
        
        // Update the widget with data that can change.
        updateWidget(memberData, type, isHost);
        
        userTypeIcon.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(onKickCallback != null && isHost) {
                    onKickCallback.kickMember(memberData); //kick the user session this widget represents
                }
            }
        });
    }
    
    /** 
     * Returns if the widget represents the local user.
     * 
     * @return True if the widget represents the local user, false otherwise.
     */
    public boolean isLocalUser() {
        return isLocalUser;
    }
    
    /**
     * Updates the user session widget based on the member data and the type of user.
     * 
     * @param memberData The  member data to update the widget from
     * @param type The type of user that the widget represents
     * @param isHost whether the member that the widget represents is the host
     */
    public void updateWidget(SessionMember memberData, UserType type, boolean isHost) {
        
        this.memberData = memberData;
        
        if (type == UserType.HOST) {
            
            userTypeIcon.setType(IconType.LAPTOP);
            iconTooltip.setTitle(HOST_TOOLTIP);
            
        } else {
            
            if(isHost) {
                
                //show buttons to allow the host to kick joiners
                userTypeIcon.setType(IconType.TIMES_CIRCLE);
                userTypeIcon.setColor("red");
                userTypeIcon.getElement().getStyle().setCursor(Cursor.POINTER);
                iconTooltip.setTitle("JOINER - Click to kick");
                
            } else {
                
                //hide kick buttons from non-host joiners
                userTypeIcon.setType(IconType.USER);
                userTypeIcon.setVisible(false);
                userTypeIcon.getElement().getStyle().clearCursor();
                iconTooltip.setTitle("");
            }
        }
        
        SessionMembership sessionMembership = memberData.getSessionMembership();        
            
        userName.setText(sessionMembership.getUsername());

        if (sessionMembership.getTeamMember() != null) {
            
            TeamMember<?> teamMember = sessionMembership.getTeamMember();
            teamRole.setText(teamMember.getName());
            getElement().getStyle().setBackgroundColor("rgb(210,250,210)");
            readyIcon.setVisible(true);
            
            //change CSS order so members with assigned roles appear last
            getElement().getStyle().setProperty("order", "1");
            
        } else {
            
            teamRole.setText(UNASSIGNED_LABEL);
            getElement().getStyle().setBackgroundColor("white");
            readyIcon.setVisible(false);
            
            //clear CSS order so members without assigned roles appear first (default value of 'order' is '0')
            getElement().getStyle().clearProperty("order");
        }

    }
    
    /**
     * Gets the session member data associated with this widget
     * 
     * @return The session member data associated with the widget
     */
    public SessionMember getSessionMemberData() {
        return memberData;
    }
}
