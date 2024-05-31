/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.enums.SharedPublishedCoursePermissionsEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DefaultMessageDisplay;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;

/**
 * Bootstrap widget used to display permission settings to the user. These are a simple modal class
 * that can be dismissed by the user.
 *
 * @author sharrison
 */
public class BsDialogSharedPermissionsWidget extends AbstractBsWidget {
    
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(BsDialogSharedPermissionsWidget.class.getName());

    private static BsDialogSharedPermissionsWidgetUiBinder uiBinder = GWT.create(BsDialogSharedPermissionsWidgetUiBinder.class);
    
    /** how often to check for update course permissions progress updates, milliseconds */
    private static int CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION = 1000;

    /**
     * the dialog that contains the permissions table
     */
    @UiField
    Modal dialogModal;

    /**
     * the table of user permissions
     */
    @UiField
    CellTable<Serializable> sharedPermissionsCellTable;

    /**
     * used to add user permissions
     */
    @UiField
    Icon addButton;
    
    /**
     * used to add multiple users permissions
     */
    @UiField
    Icon addBulkButton;
    
    /** A dialog that is used to upload files so that permissions can be added from the users mentioned in it */
    private FileSelectionDialog addUsersFromFileDialog = new FileSelectionDialog(
            Dashboard.USER_PERMISSIONS_SERVLET_URL, 
            new CanHandleUploadedFile() {
                
                @Override
                public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {
                    
                    addUsersFromFileDialog.hide();  
                    
                    if(courseData != null){
                        //have server apply uploaded file
                        UiManager.getInstance().addCourseUserPermissionsFromFile(uploadFilePath, courseData, new AsyncCallback<GenericRpcResponse<DomainOption>>() {
    
                            @Override
                            public void onFailure(Throwable caught) {
    
                                UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                "There was a problem while updating the course permissions from the file '"+fileName+"'.", 
                                caught.toString(), null, courseData.getDomainName());
                            }
    
                            @Override
                            public void onSuccess(GenericRpcResponse<DomainOption> result) {
                                
                                logger.info("Received addCourseUserPermissionsFromFile async response of "+result);
                                
                                if(result != null){
                                    
                                    if(result.getWasSuccessful()){
                                        DomainOption updatedItem = result.getContent();
                                        if(updatedItem != null){
                                            // don't replace the object because BsCourseWidget has a reference to it
                                            // and replacing it would prevent BsCourseWidget from showing the latest permissions
                                            courseData.setDomainOptionPermissions(updatedItem.getDomainOptionPermissions());
                                            setData(courseData);
                                        }
                                    }else{
                                        UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                                result.getException().getReason(), result.getException().getDetails(), result.getException().getErrorStackTrace(), null);
                                    }
                                }
                                
                                dialogModal.show();                            
                            }
                        });
                         
                        //schedule poll for progress 1 second from now
                        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                            @Override
                            public boolean execute() {

                                checkUpdateCoursePermissionsProgress("Adding Permissions");

                                return false;
                            }

                        }, CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION);
                        
                    }else if(dataCollectionItem != null){
                        //have server apply uploaded file
                        UiManager.getInstance().addPublishedCourseUserPermissionsFromFile(uploadFilePath, dataCollectionItem, new AsyncCallback<GenericRpcResponse<DataCollectionItem>>() {
    
                            @Override
                            public void onFailure(Throwable caught) {
    
                                UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                "There was a problem while updating the published course permissions from the file '"+fileName+"'.", 
                                caught.toString(), null, courseData.getDomainName());
                            }
    
                            @Override
                            public void onSuccess(GenericRpcResponse<DataCollectionItem> result) {
                                
                                logger.info("Received addPublishedCourseUserPermissionsFromFile async response of "+result);
                                
                                if(result != null){
                                    
                                    if(result.getWasSuccessful()){
                                        DataCollectionItem updatedItem = result.getContent();
                                        if(updatedItem != null){
                                            setData(updatedItem);
                                        }
                                    }else{
                                        UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                                result.getException().getReason(), result.getException().getDetails(), result.getException().getErrorStackTrace(), null);
                                    }
                                }
                                
                                dialogModal.show();                            
                            }
                        });
                        
                        //schedule poll for progress 1 second from now
                        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                            @Override
                            public boolean execute() {

                                checkUpdateCoursePermissionsProgress("Adding Permissions");

                                return false;
                            }

                        }, CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION);
                        
                    }else{
                        // unhandled case                        
                        logger.severe("Found unhandled use of this widget.");
                        
                        // show this widget again
                        dialogModal.show();  
                    }
                    

                }
            }, DefaultMessageDisplay.includeAllMessages);
    
    /**
     * used to remove all user permissions
     */
    @UiField
    Icon removeAllButton;

    /**
     * used to close the modal
     */
    @UiField
    Button closeButton;
    
    /**
     * to show the name of the course whose permissions are being shown
     */
    @UiField
    Label courseIdLabel;
    
    /**
     * contains useful info about sharing courses
     */
    @UiField
    HTML shareCourseConsiderationHTML;
    
    /**
     * contains useful info about sharing published courses
     */
    @UiField
    HTML sharePublishedCourseConsiderationHTML;

    /** properties from the server */
    private ServerProperties serverProperties = null;

    /** The data provider for the shared course user permissions. */
    private ListDataProvider<Serializable> permissionsDataProvider = new ListDataProvider<Serializable>() {
        @SuppressWarnings("unchecked")
        @Override
        public void refresh() {

            if (permissionsDataProvider.getList() != null && !permissionsDataProvider.getList().isEmpty()) {
                
                List<Serializable> list = permissionsDataProvider.getList();
                
                if(list.get(0) instanceof DomainOptionPermissions){
                    Collections.sort((List<DomainOptionPermissions>)(List<?>)list);
                }else if(list.get(0) instanceof DataCollectionPermission){
                    Collections.sort((List<DataCollectionPermission>)(List<?>)list);
                }
            }

            super.refresh();
        }
    };

    interface BsDialogSharedPermissionsWidgetUiBinder extends UiBinder<Widget, BsDialogSharedPermissionsWidget> {
    }

    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
    
    /** 
     * contains the published course information (name, permissions, etc.) 
     * Will be null if this dialog is for a course (DomainOption), {@link courseData} will not be null. 
     */
    private DataCollectionItem dataCollectionItem;
    
    /**
     * List of published course roles that can be assigned.  This essentially removes the Owner role
     * from being assignable by a user.
     */
    private static final List<SharedPublishedCoursePermissionsEnum> SHARED_PUBLISHED_COURSE_ROLES;
    static{
        SHARED_PUBLISHED_COURSE_ROLES = new ArrayList<SharedPublishedCoursePermissionsEnum>(2);
        SHARED_PUBLISHED_COURSE_ROLES.add(SharedPublishedCoursePermissionsEnum.MANAGER);
        SHARED_PUBLISHED_COURSE_ROLES.add(SharedPublishedCoursePermissionsEnum.RESEARCHER);        
    }
    
    /** Widget for adding/editing a user's permission on a published course */
    private AddSharedPermissionsDialogWidget<SharedPublishedCoursePermissionsEnum> addPublishedCoursePermissionsDialogWidget = 
            new AddSharedPermissionsDialogWidget<SharedPublishedCoursePermissionsEnum>(SHARED_PUBLISHED_COURSE_ROLES);
    
    /** used to show progress on updating the course permissions on the server */
    private ProgressModalDialogBox coursePermissionsUpdateProgressDialog = null;

    /** 
     * contains the course information (name, permissions, etc.) 
     * Will be null if this dialog is for a published course (DataCollectionItem), {@link dataCollectionItem} will not be null. 
     */
    private DomainOption courseData;

    /** Widget for adding/editing a user's permission on a course */
    private AddSharedPermissionsDialogWidget<SharedCoursePermissionsEnum> addCoursePermissionsDialogWidget = 
            new AddSharedPermissionsDialogWidget<SharedCoursePermissionsEnum>(SharedCoursePermissionsEnum.VALUES());

    /** Widget for deleting a shared user from course access */
    private DeleteSharedPermissionsDialogWidget deletePermissionsDialogWidget = new DeleteSharedPermissionsDialogWidget();

    /** The user column for the permissions list */
    private Column<Serializable, String> userColumn = new Column<Serializable, String>(new TextCell()) {
        @Override
        public String getValue(Serializable permission) {
            StringBuilder builder = new StringBuilder();
            
            if(permission instanceof DomainOptionPermissions){
                
                DomainOptionPermissions optPermissions = (DomainOptionPermissions)permission;
                builder.append(optPermissions.getUser());
                
                if(optPermissions.isOwner()){
                    builder.append(" (owner)");
                }

            }else if(permission instanceof DataCollectionPermission){
                
                DataCollectionPermission dataCollectionPermission = (DataCollectionPermission)permission;
                builder.append(dataCollectionPermission.getUsername());

            }else{
                builder.append("UNKNOWN (ERROR)");
            }

            return builder.toString();
        }
    };

    /** The permission column for the permissions list */
    private Column<Serializable, String> permissionsColumn = new Column<Serializable, String>(new TextCell()) {
        @Override
        public String getValue(Serializable permission) {
            StringBuilder builder = new StringBuilder();
            
            if(permission instanceof DomainOptionPermissions){
                DomainOptionPermissions optPermissions = (DomainOptionPermissions)permission;
                if (optPermissions.getPermission() != null) {
                    builder.append(optPermissions.getPermission().getDisplayName());
                }
            }else if(permission instanceof DataCollectionPermission){
                
                DataCollectionPermission dataCollectionPermission = (DataCollectionPermission)permission;
                if(dataCollectionPermission.getDataCollectionUserRole() != null) {
                    builder.append(dataCollectionPermission.getDataCollectionUserRole().getDisplayName());
                }
            }

            return builder.toString();
        }
    };

    /** The column containing the edit button for the user's permissions */
    private Column<Serializable, String> editPermissionsColumn = new Column<Serializable, String>(new ButtonCell() {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {

            Icon icon = new Icon(IconType.PENCIL_SQUARE);
            icon.setSize(IconSize.LARGE);
            icon.setColor("BLUE");
            icon.setTitle("Edit this user's permissions");
            icon.addStyleName("sharedPermissionsCellTableButtons");

            // do not want the owner editing their own permissions
            icon.setVisible(value != null);

            SafeHtml html = SafeHtmlUtils.fromTrustedString(icon.toString());
            sb.append(html);
        }
    }) {

        @Override
        public String getValue(Serializable permission) {
            // Returning null will hide the edit button
            // Return null if:
            // 1. it is the owner, do not want the owner being changed
            // 2. this is the current user, do not want the user editing their own permissions
            
            boolean isOwner = false, isUser = false;
            if(permission instanceof DomainOptionPermissions){
                
                DomainOptionPermissions record = (DomainOptionPermissions)permission;
                isOwner = record.isOwner();
                isUser = StringUtils.equalsIgnoreCase(record.getUser(), UiManager.getInstance().getUserName());
                
            }else if(permission instanceof DataCollectionPermission){
                
                DataCollectionPermission dataCollectionPermission = (DataCollectionPermission)permission;
                isOwner = dataCollectionPermission.getDataCollectionUserRole() == DataCollectionUserRole.OWNER;
                isUser = StringUtils.equalsIgnoreCase(dataCollectionPermission.getUsername(), UiManager.getInstance().getUserName());
                
            } else{
                //unhandled type - be safe and block editing
                return null;
            }
            
            if(isOwner || isUser){
                return null;
            }else{
                return Constants.EMPTY;
            }
            
        }
    };

    /** The column containing the delete button for the user's permissions */
    private Column<Serializable, String> removeColumn = new Column<Serializable, String>(new ButtonCell() {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {

            Icon icon = new Icon(IconType.USER_TIMES);
            icon.setSize(IconSize.LARGE);
            icon.setColor("RED");
            icon.setTitle("Unshare the course with this user");
            icon.addStyleName("sharedPermissionsCellTableButtons");

            // do not want the owner editing their own permissions
            icon.setVisible(value != null);

            SafeHtml html = SafeHtmlUtils.fromTrustedString(icon.toString());
            sb.append(html);
        }
    }) {

        @Override
        public String getValue(Serializable permission) {
            
            // Returning null will hide the delete button
            // Return null if:
            // 1. it is the owner, do not want the owner being deleted
            // 2. this is the current user, do not want the user deleting their own permissions
            
            boolean isOwner = false, isUser = false;
            if(permission instanceof DomainOptionPermissions){
                
                DomainOptionPermissions record = (DomainOptionPermissions)permission;
                isOwner = record.isOwner();
                isUser = StringUtils.equalsIgnoreCase(record.getUser(), UiManager.getInstance().getUserName());
                
            }else if(permission instanceof DataCollectionPermission){
                
                DataCollectionPermission dataCollectionPermission = (DataCollectionPermission)permission;
                isOwner = dataCollectionPermission.getDataCollectionUserRole() == DataCollectionUserRole.OWNER;
                isUser = StringUtils.equalsIgnoreCase(dataCollectionPermission.getUsername(), UiManager.getInstance().getUserName());
                
            } else{
                //unhandled type - be safe and block editing
                return null;
            }
            
            if(isOwner || isUser){
                return null;
            }else{
                return Constants.EMPTY;
            }
        }
    };

    /**
     * Constructor.
     */
    public BsDialogSharedPermissionsWidget() {
        initWidget(uiBinder.createAndBindUi(this));

        // create permissions table
        sharedPermissionsCellTable.addColumn(userColumn, "User");
        sharedPermissionsCellTable.setColumnWidth(userColumn, "60%");
        sharedPermissionsCellTable.addColumn(permissionsColumn, "Permissions");
        sharedPermissionsCellTable.setColumnWidth(permissionsColumn, "30%");        
        sharedPermissionsCellTable.addColumn(editPermissionsColumn);
        sharedPermissionsCellTable.addColumn(removeColumn);

        sharedPermissionsCellTable.setPageSize(Integer.MAX_VALUE);
        sharedPermissionsCellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

        // add the cell table to the data provider
        permissionsDataProvider.addDataDisplay(sharedPermissionsCellTable);

        editPermissionsColumn.setFieldUpdater(new FieldUpdater<Serializable, String>() {

            @Override
            public void update(int index, Serializable permissionData, String value) {
                hide();                

                if(permissionData instanceof DomainOptionPermissions){
                    
                    DomainOptionPermissions permission = (DomainOptionPermissions)permissionData;
                    addCoursePermissionsDialogWidget.setValues(permission.getUser(), permission.getPermission());
                    addCoursePermissionsDialogWidget.show();
                    
                }else if(permissionData instanceof DataCollectionPermission){
                    
                    DataCollectionPermission permission = (DataCollectionPermission)permissionData;
                    SharedPublishedCoursePermissionsEnum sharedPermissionsEnum = null;
                    try{
                        sharedPermissionsEnum = ExperimentUtil.getSharedPublishedCoursePermissionsEnum(permission.getDataCollectionUserRole());
                    }catch(UnsupportedOperationException e){
                        logger.log(Level.SEVERE, "Unhandled user role type", e);
                    }
                    addPublishedCoursePermissionsDialogWidget.setValues(permission.getUsername(), sharedPermissionsEnum);
                    addPublishedCoursePermissionsDialogWidget.show();
                }
            }
        });

        removeColumn.setFieldUpdater(new FieldUpdater<Serializable, String>() {

            @Override
            public void update(int index, Serializable permissionsData, String value) {
                hide();
                deletePermissionsDialogWidget.setData(permissionsData);
                deletePermissionsDialogWidget.show();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                
                if(dataCollectionItem != null){
                    addPublishedCoursePermissionsDialogWidget.reset();
                    addPublishedCoursePermissionsDialogWidget.show();
                }else{
                    addCoursePermissionsDialogWidget.reset();
                    addCoursePermissionsDialogWidget.show();
                }

            }
        });
        
        addUsersFromFileDialog.setAllowedFileExtensions(new String[] {".csv"});     
        
        addBulkButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                addUsersFromFileDialog.center();                    
            }
        });
        
        removeAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {

                UiManager.getInstance().displayConfirmDialog("Remove all users?", 
                        "Are you sure you want to remove all of the users this course was shared with?", 
                        "Yes, remove all", "Cancel", new ConfirmationDialogCallback() {
                            
                            @Override
                            public void onDecline() {
                                // no-op
                            }
                            
                            @Override
                            public void onAccept() {
                                
                                boolean removedSomething = false;
                                if (CollectionUtils.isNotEmpty(permissionsDataProvider.getList())) {
                                    
                                    Set<DomainOptionPermissions> coursePermissions = null;
                                    Set<DataCollectionPermission> publishedCoursePermissions = null;
                                    for (int index = 0; index < permissionsDataProvider.getList().size(); index++) {
                                        
                                        Serializable permissionObj = permissionsDataProvider.getList().get(index);
                                
                                        if(permissionObj instanceof DomainOptionPermissions){
                                            //deleting permissions on a course
                                            
                                            final DomainOptionPermissions permission = (DomainOptionPermissions) permissionObj;
                                            if(permission.isOwner()){
                                                // don't remove owner permissions
                                                continue;
                                            }
                                            permission.setPermission(null);  //set to null to remove permissions server side
                                            
                                            removedSomething = true;                                            
                                            
                                            if(coursePermissions == null){
                                                coursePermissions = new HashSet<>();
                                            }
                                            
                                            coursePermissions.add(permission);

                                        }else{
                                            //deleting permissions on a published course
                                            
                                            final DataCollectionPermission permission = (DataCollectionPermission) permissionObj;
                                            if(permission.isOwner()){
                                                // don't remove owner permissions
                                                continue;
                                            }
                                            permission.updateDataCollectionUserRole(null);  //set to null to remove permissions server side
                                            
                                            removedSomething = true; 
                                            
                                            if(publishedCoursePermissions == null){
                                                publishedCoursePermissions = new HashSet<>();
                                            }
                                            
                                            publishedCoursePermissions.add(permission);
                                        }
                                    } // end for loop on all users  
                                    
                                    if(coursePermissions != null){                                        
                                        
                                        final Set<DomainOptionPermissions> coursePermissionsFinal = new HashSet<>(coursePermissions);

                                        updateCoursePermissions("Removing all permissions", coursePermissions, new AsyncCallback<RpcResponse>() {
    
                                            @Override
                                            public void onFailure(Throwable throwable) {  
                                                
                                                UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                        "There was an issue thrown on the server with removing one or more users."+throwable.toString(), 
                                                        null, null, courseData.getDomainName());
                                                
                                                logger.log(Level.SEVERE, "Failed to remove all permissions.", throwable);
                                                
                                                refrehsUI();
                                            }
    
                                            @Override
                                            public void onSuccess(RpcResponse response) {
                                                
                                                logger.info("Received updateCoursePermissions async response of "+response);
                                                                                                    
                                                if(response != null){
                                                    
                                                    if(response.isSuccess()){
                                                        
                                                        // manually remove each entry from client side data set, 
                                                        // can't rely on DomainOptionPermissions.equals since the permissions
                                                        // are now different
                                                        for(DomainOptionPermissions finalEntry : coursePermissionsFinal) {
                                                            
                                                            Iterator<Serializable> itr = permissionsDataProvider.getList().iterator();
                                                            Serializable remove = null;
                                                            while(itr.hasNext()) {
                                                                Serializable next = itr.next();
                                                                if(next instanceof DomainOptionPermissions) {
                                                                    DomainOptionPermissions candidate = (DomainOptionPermissions)next;
                                                                    if(candidate.getUser().equalsIgnoreCase(finalEntry.getUser())) {
                                                                        //found entry to remove from data model
                                                                        remove = next;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            
                                                            if(remove != null) {
                                                                permissionsDataProvider.getList().remove(remove);
                                                            }
                                                        }

                                                    }else{
                                                        UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                                "There was an issue (Not successful) with removing one or more users. "+response.getResponse(), 
                                                                response.getAdditionalInformation(), null, courseData.getDomainName());
                                                    }
                                                }else{
                                                    
                                                    UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                            "There was an issue (Null response) with removing one or more users. The user session could not be found.", 
                                                            null, null, courseData.getDomainName());
                                                }
                                                
                                                refrehsUI();                                                   
                                            }
                                            
                                            /**
                                             * handle refreshing the UI appropriately.
                                             */
                                            private void refrehsUI(){
                                                
                                                // update permission table
                                                permissionsDataProvider.refresh();
                                                
                                                dialogModal.show();
                                            }
    
                                        });
                                        
                                    }else if(publishedCoursePermissions != null){
                                        
                                        final Set<DataCollectionPermission> publishedCoursePermissionsFinal = new HashSet<>(publishedCoursePermissions);
                                        
                                        updatePublishedCoursePermissions("Removing all permissions", publishedCoursePermissions, new AsyncCallback<RpcResponse>() {
                                            
                                            @Override
                                            public void onFailure(Throwable throwable) { 
                                                
                                                UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                        "There was an issue thrown on the server with removing one or more users."+throwable.toString(), 
                                                        null, null, courseData.getDomainName());
                                                
                                                logger.log(Level.SEVERE, "Failed to remove all permissions.", throwable);
                                                
                                                refrehsUI(); 
                                            }
    
                                            @Override
                                            public void onSuccess(RpcResponse response) {
                                                
                                                logger.info("Received updatePublishedCoursePermissions async response of "+response);
                                                                                                    
                                                if(response != null){
                                                    
                                                    if(response.isSuccess()){
                                                        
                                                        // manually remove each entry from client side data set, 
                                                        // can't rely on DomainOptionPermissions.equals since the permissions
                                                        // are now different
                                                        for(DataCollectionPermission finalEntry : publishedCoursePermissionsFinal) {
                                                            
                                                            Iterator<Serializable> itr = permissionsDataProvider.getList().iterator();
                                                            Serializable remove = null;
                                                            while(itr.hasNext()) {
                                                                Serializable next = itr.next();
                                                                if(next instanceof DataCollectionPermission) {
                                                                    DataCollectionPermission candidate = (DataCollectionPermission)next;
                                                                    if(candidate.getUsername().equalsIgnoreCase(finalEntry.getUsername())) {
                                                                        //found entry to remove from data model
                                                                        remove = next;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            
                                                            if(remove != null) {
                                                                permissionsDataProvider.getList().remove(remove);
                                                            }
                                                        }                                                        

                                                    }else{
                                                        UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                                "There was an issue (Not successful) with removing one or more users. "+response.getResponse(), 
                                                                response.getAdditionalInformation(), null, courseData.getDomainName());
                                                    }
                                                }else{
                                                    
                                                    UiManager.getInstance().displayDetailedErrorDialog("Remove All Permissions Failed", 
                                                            "There was an issue (Null response) with removing one or more users. The user session could not be found.", 
                                                            null, null, courseData.getDomainName());
                                                }
                                                
                                                refrehsUI(); 
                                            }
                                            
                                            /**
                                             * handle refreshing the UI appropriately.
                                             */
                                            private void refrehsUI(){
                                                
                                                // update permission table
                                                permissionsDataProvider.refresh();
                                                
                                                dialogModal.show();
                                            }
    
                                        });

                                    }
                                    
                                }
                                
                                if(!removedSomething){
                                    // there are no permissions to remove                                    
                                    logger.info("Found no permissions to update, not contacting the server");

                                    dialogModal.show();
                                }                               

                            }
                        });
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                hide();
            }
        });

        addCoursePermissionsDialogWidget.setConfirmClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                
                createOrUpdatePermissionUser(addCoursePermissionsDialogWidget.getUser(), addCoursePermissionsDialogWidget.getPermission(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable throwable) {                        
                        addCoursePermissionsDialogWidget.setDialogMessage(throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {

                        if(response != null){
                            
                            if(response.isSuccess()){
                                
                                // update permission table
                                permissionsDataProvider.refresh();
                                
                            }else{
                                UiManager.getInstance().displayDetailedErrorDialog("Add Permissions Failed", 
                                        response.getResponse(), 
                                        response.getAdditionalInformation(), response.getErrorStackTrace(), courseData.getDomainName());
                            }
                        }else{
                            
                            UiManager.getInstance().displayDetailedErrorDialog("Add Permissions Failed", 
                                    "The user session could not be found", 
                                    null, null, courseData.getDomainName());
                        }
                        
                        addCoursePermissionsDialogWidget.hide();
                        dialogModal.show();
                    }

                });

            }
        });

        addCoursePermissionsDialogWidget.setCancelClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                addCoursePermissionsDialogWidget.hide();
                dialogModal.show();
            }
        });

        deletePermissionsDialogWidget.setConfirmClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (deletePermissionsDialogWidget.getPermissions() != null) {

                    Serializable permissionObj = deletePermissionsDialogWidget.getPermissions();
                    
                    if(permissionObj instanceof DomainOptionPermissions){
                        //deleting permissions on a course
                        
                        final DomainOptionPermissions permission = (DomainOptionPermissions) permissionObj;
                        permission.setPermission(null);  //set to null to remove permissions server side
                        
                        Set<DomainOptionPermissions> permissions = new HashSet<>();
                        permissions.add(permission);
                        
                        updateCoursePermissions("Removing permissions", permissions, new AsyncCallback<RpcResponse>() {

                            @Override
                            public void onFailure(Throwable throwable) {                                
                                UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                "There was a problem while updating the course permissions for user '"+permission.getUser()+"'.", 
                                throwable.toString(), null, courseData.getDomainName());
                            }

                            @Override
                            public void onSuccess(RpcResponse response) {
                                
                                if(response != null){
                                    
                                    if(response.isSuccess()){
                                        
                                        permissionsDataProvider.getList().remove(deletePermissionsDialogWidget.getPermissions());  //remove from client side table

                                        // update permission table
                                        permissionsDataProvider.refresh();
                                        
                                    }else{
                                        UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                                response.getResponse(), 
                                                response.getAdditionalInformation(), response.getErrorStackTrace(), courseData.getDomainName());
                                    }
                                }else{
                                    
                                    UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                            "The user session could not be found", 
                                            null, null, courseData.getDomainName());
                                }
                                
                                deletePermissionsDialogWidget.hide();
                                dialogModal.show();
                            }

                        });
                    }else{
                        //deleting permissions on a published course
                        
                        final DataCollectionPermission permission = (DataCollectionPermission) permissionObj;
                        permission.updateDataCollectionUserRole(null);  //set to null to remove permissions server side
                        
                        Set<DataCollectionPermission> permissions = new HashSet<>();
                        permissions.add(permission);
                        
                        updatePublishedCoursePermissions("Removing permission", permissions, new AsyncCallback<RpcResponse>() {

                            @Override
                            public void onFailure(Throwable throwable) {                                
                                UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                "There was a problem while updating the published course permissions for user '"+permission.getUsername()+"'.", 
                                throwable.toString(), null, dataCollectionItem.getName());
                            }

                            @Override
                            public void onSuccess(RpcResponse response) {
                                
                                if(response != null){
                                    
                                    if(response.isSuccess()){
                                        
                                        permissionsDataProvider.getList().remove(deletePermissionsDialogWidget.getPermissions());  //remove from client side table

                                        // update permission table
                                        permissionsDataProvider.refresh();
                                        
                                    }else{
                                        UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                                response.getResponse(), 
                                                response.getAdditionalInformation(), response.getErrorStackTrace(), dataCollectionItem.getName());
                                    }
                                }else{
                                    
                                    UiManager.getInstance().displayDetailedErrorDialog("Update Permissions Failed", 
                                            "The user session could not be found", 
                                            null, null, dataCollectionItem.getName());
                                }
                                
                                deletePermissionsDialogWidget.hide();
                                dialogModal.show();
                            }

                        });
                    }

                }
            }
        });

        deletePermissionsDialogWidget.setCancelClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                deletePermissionsDialogWidget.hide();
                dialogModal.show();
            }
        });
        
        addPublishedCoursePermissionsDialogWidget.setConfirmClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                createOrUpdatePermissionUser(addPublishedCoursePermissionsDialogWidget.getUser(), addPublishedCoursePermissionsDialogWidget.getPermission(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable throwable) {                   
                        addPublishedCoursePermissionsDialogWidget.setDialogMessage(throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {
                        
                        if(response != null){
                            
                            if(response.isSuccess()){
                                
                                // update permission table
                                permissionsDataProvider.refresh();
                                
                            }else{
                                UiManager.getInstance().displayDetailedErrorDialog("Add Permissions Failed", 
                                        response.getResponse(), 
                                        response.getAdditionalInformation(), response.getErrorStackTrace(), dataCollectionItem.getName());
                            }
                        }else{
                            
                            UiManager.getInstance().displayDetailedErrorDialog("Add Permissions Failed", 
                                    "The user session could not be found", 
                                    null, null, dataCollectionItem.getName());
                        }
                        
                        addPublishedCoursePermissionsDialogWidget.hide();
                        dialogModal.show();
                    }
                });

            }
        });

        addPublishedCoursePermissionsDialogWidget.setCancelClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                addPublishedCoursePermissionsDialogWidget.hide();
                dialogModal.show();
            }
        });

    }

    /**
     * Initializes this widget based on the properties that are sent from the server.
     * 
     * @param properties The properties retrieved from the server.
     */
    public void init(ServerProperties properties) {
        this.serverProperties = properties;
    }

    /**
     * Creates or Updates the given user with the specified permission for a course.
     * 
     * @param user the username.  Can't be null or empty.
     * @param permissionEnum the permission type.  Can't be null.
     * @param callback used to notify the caller of failure or success. Can't be null.
     */
    private void createOrUpdatePermissionUser(String user, SharedCoursePermissionsEnum permissionEnum, AsyncCallback<RpcResponse> callback) {
        // no user.
        if (StringUtils.isBlank(user)) {
            callback.onFailure(new Exception("Cannot have an empty user field."));
            return;
        }

        // no permission.
        if (permissionEnum == null) {
            callback.onFailure(new Exception("You must select a permission type for this user."));
            return;
        }

        // check against the prohibited names
        if (checkProhibitedUsername(user)) {
            callback.onFailure(new Exception("Cannot access a prohibited username."));
            return;
        }

        boolean create = true;

        // if user is already granted access, update permission
        if (permissionsDataProvider.getList() != null) {
            for (Serializable existingUserObj : permissionsDataProvider.getList()) {
                
                DomainOptionPermissions existingUser = (DomainOptionPermissions)existingUserObj;
                if (StringUtils.equalsIgnoreCase(existingUser.getUser(), user.trim())) {
                    if (existingUser.isOwner()) {
                        // user is not modifiable if the owner
                        callback.onFailure(new Exception("Cannot modify the owner's permissions"));
                        return;
                    }
                    create = false;
                    existingUser.setPermission(permissionEnum);
                    Set<DomainOptionPermissions> permissions = new HashSet<>();
                    permissions.add(existingUser);
                    updateCoursePermissions("Changing User Permission", permissions, callback);
                    break;
                }
            }
        }

        // new user
        if (create) {
            DomainOptionPermissions newPermissions = new DomainOptionPermissions(user, permissionEnum, false);
            permissionsDataProvider.getList().add(newPermissions);
            Set<DomainOptionPermissions> permissions = new HashSet<>();
            permissions.add(newPermissions);
            updateCoursePermissions("Changing User Permission", permissions, callback);
        }

    }
    
    /**
     * Creates or Updates the given user with the specified permission for a published course.
     * 
     * @param user the username.  Can't be null or empty.
     * @param permissionEnum the permission type.  Can't be null.
     * @param callback used to notify the caller of failure or success. Can't be null.
     */
    private void createOrUpdatePermissionUser(String user, SharedPublishedCoursePermissionsEnum permissionEnum, AsyncCallback<RpcResponse> callback) {
        // no user.
        if (StringUtils.isBlank(user)) {
            callback.onFailure(new Exception("Cannot have an empty user field."));
            return;
        }

        // no permission.
        if (permissionEnum == null) {
            callback.onFailure(new Exception("You must select a permission type for this user."));
            return;
        }

        // check against the prohibited names
        if (checkProhibitedUsername(user)) {
            callback.onFailure(new Exception("Cannot access a prohibited username."));
            return;
        }
        
        // convert AbstractEnum to DB enum
        ExperimentUtil.DataCollectionUserRole userRole = null;
        try{
            userRole = ExperimentUtil.getDataCollectionUserRole(permissionEnum);
        }catch(UnsupportedOperationException e){
            callback.onFailure(e);
        }

        boolean create = true;

        // if user is already granted access, update permission
        if (permissionsDataProvider.getList() != null) {
            for (Serializable existingUserObj : permissionsDataProvider.getList()) {
                
                DataCollectionPermission existingUser = (DataCollectionPermission)existingUserObj;
                if (StringUtils.equalsIgnoreCase(existingUser.getUsername(), user.trim())) {
                    if (existingUser.getDataCollectionUserRole() == DataCollectionUserRole.OWNER) {
                        // user is not modifiable if the owner
                        callback.onFailure(new Exception("Cannot modify the owner's permissions"));
                        return;
                    }
                    create = false;                    
                    
                    existingUser.updateDataCollectionUserRole(userRole);
                    Set<DataCollectionPermission> permissions = new HashSet<>();
                    permissions.add(existingUser);
                    updatePublishedCoursePermissions("Changing User Permission", permissions, callback);
                    break;
                }
            }
        }

        // new user
        if (create) {
            DataCollectionPermission newPermissions = new DataCollectionPermission(dataCollectionItem.getId(), user, userRole);
            permissionsDataProvider.getList().add(newPermissions);
            Set<DataCollectionPermission> permissions = new HashSet<>();
            permissions.add(newPermissions);
            updatePublishedCoursePermissions("Changing User Permission", permissions, callback);
        }

    }

    /**
     * Update the course permissions for one or more users.
     * 
     * @param progressTaskName the name of the task being performed to show in the progress dialog.  Shouldn't be null or empty.
     * @param permissions the permissions for one or more users.
     * @param callback used to notify the caller of failure or success
     */
    private void updateCoursePermissions(final String progressTaskName, Set<DomainOptionPermissions> permissions, AsyncCallback<RpcResponse> callback) {
        
        UiManager.getInstance().updatePermissions(permissions, courseData, callback);
        
        //schedule poll for progress 1 second from now
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {

                checkUpdateCoursePermissionsProgress(progressTaskName);

                return false;
            }

        }, CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION);
    }
    
    /**
     * Asks the server for the latest progress on updating the log index. The progress
     * is displayed to the user on the progress bar.  When the server indicates the request is
     * completed, the progress dialog is closed.  If the load operation is still
     * on going, this method will recursively call itself.
     * @param progressTaskName the name of the task being performed to show in the progress dialog.  Shouldn't be null or empty.
     */
    private void checkUpdateCoursePermissionsProgress(final String progressTaskName) {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Checking update course permissions progress for "+UiManager.getInstance().getUserName());
        }  
        
        if(coursePermissionsUpdateProgressDialog == null){
            coursePermissionsUpdateProgressDialog = new ProgressModalDialogBox("Updating Permissions...", new ProgressIndicator(), false);
        }else if(!coursePermissionsUpdateProgressDialog.isVisible()) {        
            // the dialog was hidden at the end of the last job, clear any previous job's last progress
            coursePermissionsUpdateProgressDialog.updateProgress(new ProgressIndicator());
        }
        
        coursePermissionsUpdateProgressDialog.show();

        UiManager.getInstance().getDashboardService().getUpdateCourseUserPermissionsProgress(UiManager.getInstance().getUserName(),
                new AsyncCallback<ProgressResponse>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("Error caught with getting update course permissions progress: " + t.getMessage());

                UiManager.getInstance().displayErrorDialog("Failed to update course permissions", "There was a server side error of\n"+t.getMessage(), null);

                coursePermissionsUpdateProgressDialog.hide();
            }

            @Override
            public void onSuccess(ProgressResponse progressResponse) {  

                coursePermissionsUpdateProgressDialog.updateProgress(progressResponse.getProgress());

                if(progressResponse.getErrorMessage() != null){

                    UiManager.getInstance().displayDetailedErrorDialog("Failed to update course permissions",
                            progressResponse.getErrorMessage(), progressResponse.getErrorDetails(),
                            null, null);
                    
                    coursePermissionsUpdateProgressDialog.hide();

                }else if(progressResponse.getProgress() != null && progressResponse.getProgress().isComplete()){
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Update course permissions request progress has completed");
                    }
                    
                    coursePermissionsUpdateProgressDialog.hide();

                } else {
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Update course permissions request progress continues.."+progressResponse);
                    }

                    //schedule another poll for progress 1 second from now
                    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                        @Override
                        public boolean execute() {

                            checkUpdateCoursePermissionsProgress(progressTaskName);

                            return false;
                        }

                    }, CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION);
                }
            }

        });

    }
    
    /**
     * Update the published course permissions for one or more users.
     * 
     * @param progressTaskName the name of the task being performed to show in the progress dialog.  Shouldn't be null or empty.
     * @param permissions the permissions for one or more users.
     * @param callback used to notify the caller of failure or success
     */
    private void updatePublishedCoursePermissions(final String progressTaskName, Set<DataCollectionPermission> permissions, AsyncCallback<RpcResponse> callback) {
        UiManager.getInstance().updatePermissions(permissions, dataCollectionItem, callback);
        
        //schedule poll for progress 1 second from now
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {

                checkUpdateCoursePermissionsProgress(progressTaskName);

                return false;
            }

        }, CHECK_UPDATE_COURSE_PERMISSIONS_PROGRESS_DURATION);
    }

    /**
     * Checks if the given username is prohibited.
     * 
     * @param user the username.
     * @return true if the username
     */
    private boolean checkProhibitedUsername(String user) {
        // null or empty isn't a prohibited value
        if (StringUtils.isBlank(user)) {
            return false;
        }

        // check prohibited names
        if (serverProperties != null) {
            for (String prohibitedName : serverProperties.getProhibitedNames()) {
                if (StringUtils.equalsIgnoreCase(prohibitedName, user.trim())) {
                    return true;
                }
            }
        }

        // not prohibited
        return false;
    }

    /**
     * Sets the data for the dialog (but does not explicitly show the dialog) so that permissions can be changed for a course.
     * 
     * @param courseData the information for a course
     */
    @SuppressWarnings("unchecked")
    public void setData(DomainOption courseData) {
        this.courseData = courseData;
        this.dataCollectionItem = null;
        
        courseIdLabel.setText(courseData.getDomainName());
        
        // for sharing courses
        addUsersFromFileDialog.setIntroMessageHTML(
                "Please select a CSV file that has a list of users to give permissions too."
                + "The file must be of the following format.<br/><br/>"
                + "<div style='text-align: center;'><div class='usersImportHelp'>"
                + "<table>"
                    + "<tr>"
                        + "<td>username 1,TakeCourse</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>username 2,ViewCourse</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>username 3,EditCourse</td>"
                    + "</tr>"
                + "</table></div></div><br/>"
        );
        
        shareCourseConsiderationHTML.setVisible(true);
        sharePublishedCourseConsiderationHTML.setVisible(false);

        // update permissions table with course data
        logger.info("set data for course: list size = "+ 
                (courseData.getDomainOptionPermissions() != null ? courseData.getDomainOptionPermissions().size() : "0"));
        permissionsDataProvider.setList((List<Serializable>)(List<?>)courseData.getDomainOptionPermissions());
        permissionsDataProvider.refresh();
    }
    
    /**
     * Sets the data for the dialog (but does not explicitly show the dialog) so that permissions can be changed for a published course.
     * 
     * @param dataCollectionItem the information for a published course
     */
    @SuppressWarnings("unchecked")
    public void setData(DataCollectionItem dataCollectionItem) {
        this.courseData = null;
        this.dataCollectionItem = dataCollectionItem;
        
        courseIdLabel.setText(dataCollectionItem.getName());
        
        // for sharing published courses
        addUsersFromFileDialog.setIntroMessageHTML(
                "Please select a CSV file that has a list of users to give permissions too."
                + "The file must be of the following format.<br/><br/>"
                + "<div style='text-align: center;'><div class='usersImportHelp'>"
                + "<table>"
                    + "<tr>"
                        + "<td>username 1,OWNER</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>username 2,MANAGER</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td>username 3,RESEARCHER</td>"
                        + "</tr>"
                + "</table></div></div><br/>"
        );
        
        shareCourseConsiderationHTML.setVisible(false);
        sharePublishedCourseConsiderationHTML.setVisible(true);

        // update permissions table with course data
        logger.info("set data for data collection item (published course): list size = "+
                (dataCollectionItem.getPermissions() != null ? dataCollectionItem.getPermissions().size() : "0"));
        permissionsDataProvider.setList((List<Serializable>)(List<?>)new ArrayList<DataCollectionPermission>(dataCollectionItem.getPermissions()));
        permissionsDataProvider.refresh();
    }

    /**
     * Accessor to show the modal dialog.
     */
    public void show() {
        dialogModal.show();
        isShown = true;
    }

    /**
     * Accessor to hide the modal dialog.
     */
    public void hide() {
        dialogModal.hide();
        isShown = false;
    }
    
    /**
     * Accessor to hide this widget and any children widgets.
     */
    public void hideAll() {
        hide();
        getAddSharedPublishedCoursePermissionsDialogWidget().hide();
        getAddSharedCoursePermissionsDialogWidget().hide();
        getDeleteSharedPermissionsDialogWidget().hide();
    }

    /**
     * Accessor to determine if the modal is being shown.
     * 
     * @return true if the modal is being shown, false otherwise.
     */
    public boolean isModalShown() {
        return isShown;
    }

    /**
     * Retrieves the add/edit permission dialog for a course
     * 
     * @return the dialog used to add or edit a user's permission for a course
     */
    public AddSharedPermissionsDialogWidget<SharedCoursePermissionsEnum> getAddSharedCoursePermissionsDialogWidget() {
        return addCoursePermissionsDialogWidget;
    }
    
    /**
     * Retrieves the add/edit permission dialog for a published course
     * 
     * @return the dialog used to add or edit a user's permission for a published course
     */
    public AddSharedPermissionsDialogWidget<SharedPublishedCoursePermissionsEnum> getAddSharedPublishedCoursePermissionsDialogWidget() {
        return addPublishedCoursePermissionsDialogWidget;
    }

    /**
     * Retrieves the dialog used to remove a person's permission to the course
     * 
     * @return the dialog used to remove a shared user.
     */
    public DeleteSharedPermissionsDialogWidget getDeleteSharedPermissionsDialogWidget() {
        return deletePermissionsDialogWidget;
    }
}
