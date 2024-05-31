/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.util.CompareUtil;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.util.Util;
import mil.arl.gift.tools.remote.HostInfo;
import mil.arl.gift.tools.remote.LaunchConstants;

import org.slf4j.LoggerFactory;

/**
 * A control panel that displays the status of all modules and has the
 * functionality to kill and start modules.
 *
 * @author jleonard
 */
public class ControlJPanel extends JPanel implements ModuleStatusListener, UserStatusListener, DomainSessionStatusListener, RemoteClientStatusListener {

    private static final String OFFLINE_BUTTON_TOOLTIP = "Module not active";

    private static final String ONLINE_BUTTON_TOOLTIP = "Press to kill selected module";

    /** prefix label for the open TUI button on the monitor */
    private static final String DEFAULT_TUI_WEBPAGE_BUTTON_LABEL = "Open Simple Sign-On Webpage";

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ControlJPanel.class);

    private static final long serialVersionUID = 1L;

    //private String scriptFile = null;

    private final ImageIcon ONLINE_ICON = new ImageIcon(getClass().getResource("images/on_small.png"));

    private final ImageIcon OFFLINE_ICON = new ImageIcon(getClass().getResource("images/off_small.png"));

    private final HostInfo localHostInfo = new HostInfo(HostInfo.HostType.IOS_EOS, "localhost", "127.0.0.1", 0);

    private URI tutorWebClientURI = null;

    private String ipAddress = Util.getLocalHostAddress().getHostAddress();

    /**
     * A class for making sure that something is always selected
     *
     * @param <E>
     */
    private class SelectionListDataListener<E> implements ListDataListener {

        private final JList<E> list;

        //TODO: remove this once the UMS/LMS become JLists on the monitor
        private final ListModel<E> model;

        private final JLabel icon;

        /**
         * Constructor
         *
         * @param list The list to manage selections
         * @param icon The icon to update when there is a selection
         */
        public SelectionListDataListener(JList<E> list, JLabel icon) {
            this.list = list;
            this.model = list.getModel();
            this.icon = icon;
        }

        //TODO: remove this once the UMS/LMS become JLists on the monitor
        public SelectionListDataListener(ListModel<E> model, JLabel icon) {
            this.list = null;
            this.model = model;
            this.icon = icon;
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            if (list != null && list.getSelectedIndex() < 0) {
                list.setSelectedIndex(0);
            }
            icon.setIcon(ONLINE_ICON);
            icon.setToolTipText(ONLINE_BUTTON_TOOLTIP);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            if (list != null){

                if(list.getSelectedIndex() < 0) {
                    list.setSelectedIndex(0);
                }

                //Note: getting the model in the constructor won't work for JList, for whatever reason
                //      the model can return size = 0 at this point, when there are actually items in the list
                if(list.getModel().getSize() == 0){
                    icon.setIcon(OFFLINE_ICON);
                    icon.setToolTipText(OFFLINE_BUTTON_TOOLTIP);
                }

            }else if (model.getSize() == 0) {
                //TODO: remove this 'else if' once the UMS/LMS become JLists on the monitor

                icon.setIcon(OFFLINE_ICON);
                icon.setToolTipText(OFFLINE_BUTTON_TOOLTIP);
            }
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            if (list != null && list.getSelectedIndex() < 0) {
                list.setSelectedIndex(0);
            }
        }
    }

    /**
     * Listener to inform when a host has been selected
     */
    private class HostListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (giftHostsList.getSelectedIndex() > -1) {

                enableLaunchButtons(true);

            } else {

                enableLaunchButtons(false);
            }
        }
    }

    /**
     * Listener to ensure a learner station is always selected
     */
    private class LearnerStationListListener implements ListDataListener {

        private final JList<HostInfo> list;

        /**
         * Constructor
         *
         * @param list The list of learner stations
         */
        public LearnerStationListListener(JList<HostInfo> list) {
            this.list = list;
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            if (list.getSelectedIndex() < 0) {
                list.setSelectedIndex(0);
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            if (list.getSelectedIndex() < 0) {
                list.setSelectedIndex(0);
            }
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            if (list.getSelectedIndex() < 0) {
                list.setSelectedIndex(0);
            }
        }
    }

    /**
     * An list entry for a domain session list
     */
    private class DomainSessionListEntry {

        public final DomainSession domainSession;

        /**
         * Constructor
         *
         * @param domainSession The domain session in the entry
         */
        public DomainSessionListEntry(DomainSession domainSession) {

            this.domainSession = domainSession;
        }
        
        /**
         * Whether this domain session contains the provided user session identifying details.
         * 
         * @param userSession the user session to check for in this domain session
         * @return true if the user session is in this domain session object
         */
        public boolean isUserSession(UserSession userSession){
            return ((UserSession)this.domainSession).equals(userSession);
        }

        @Override
        public boolean equals(Object o) {

            if (o != null && o instanceof DomainSessionListEntry) {
                DomainSessionListEntry oEntry = (DomainSessionListEntry) o;
                if (CompareUtil.equalsNullSafe(this.domainSession, oEntry.domainSession)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + this.domainSession.hashCode();

            return hash;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();

            sb.append("User ").append(domainSession.getUserId());
            sb.append(": Type (").append(domainSession.getSessionType()).append(")");
            sb.append(": Session ").append(domainSession.getDomainSessionId());

            if (domainSession.getExperimentId() != null) {
                sb.append(": Experiment ").append(domainSession.getExperimentId());
            }

            if (domainSession.getGlobalUserId() != null) {
                sb.append(": Global User Id ").append(domainSession.getGlobalUserId());
            }
            return sb.toString();
        }
    }

    private DefaultListModel<String> tutorListModel = new DefaultListModel<>(),
            sensorListModel = new DefaultListModel<>(),
            domainListModel = new DefaultListModel<>(),
            gatewayListModel = new DefaultListModel<>(),
            pedagogicalListModel = new DefaultListModel<>(),
            learnerListModel = new DefaultListModel<>(),
            umsListModel = new DefaultListModel<>(),
            lmsListModel = new DefaultListModel<>();

    private DefaultListModel<ActiveUserListEntry> userListModel = new DefaultListModel<>();

    private DefaultListModel<DomainSessionListEntry> domainSessionListModel = new DefaultListModel<>();

    private DefaultListModel<HostInfo> giftHostsListModel = new DefaultListModel<>();

    /**
     * Constructor
     */
    public ControlJPanel() {
        initComponents();

        tutorListModel.addListDataListener(new SelectionListDataListener<>(tutorList, tutorListPowerButton));
        sensorListModel.addListDataListener(new SelectionListDataListener<>(sensorList, sensorListPowerButton));
        domainListModel.addListDataListener(new SelectionListDataListener<>(domainList, domainListPowerButton));
        gatewayListModel.addListDataListener(new SelectionListDataListener<>(gatewayList, gatewayListPowerButton));
        pedagogicalListModel.addListDataListener(new SelectionListDataListener<>(pedList, pedListPowerButton));
        learnerListModel.addListDataListener(new SelectionListDataListener<>(learnerList, learnerListPowerButton));
        umsListModel.addListDataListener(new SelectionListDataListener<>(umsListModel, umsPowerButton));
        lmsListModel.addListDataListener(new SelectionListDataListener<>(lmsListModel, lmsPowerButton));

        tutorList.setModel(tutorListModel);
        tutorList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {

                String queueName = tutorList.getSelectedValue();
                if(queueName != null){

                    Integer tutorWebClientPort = MonitorModuleProperties.getInstance().getWebTutorPort();
                    String tutorWebClientPath = MonitorModuleProperties.getInstance().getWebTutorPath();
                    String queuePrefix = SubjectUtil.TUTOR_QUEUE_PREFIX + AbstractModule.ADDRESS_TOKEN_DELIM;
                    
                    String tutorWebClientHost = queueName.substring(queueName.lastIndexOf(queuePrefix) + queuePrefix.length(), queueName.lastIndexOf(AbstractModule.ADDRESS_TOKEN_DELIM));

                    String tutorWebClientURL = MonitorModuleProperties.getInstance().getTransferProtocol() + tutorWebClientHost + ":" + tutorWebClientPort + "/" + tutorWebClientPath;
                    try {

                        tutorWebClientURI = new URI(tutorWebClientURL);
                        tuiWebpageButton.setEnabled(isLaunchEnabled());
                    } catch (URISyntaxException ex) {
                        logger.error("Could not construct a valid TWC URL", ex);
                    }


                    //update the label on the 'Open Tutor Webpage' button
                    tuiWebpageButton.setText(tutorWebClientURI.toString());
                }
            }

        });
        sensorList.setModel(sensorListModel);
        domainList.setModel(domainListModel);
        gatewayList.setModel(gatewayListModel);
        pedList.setModel(pedagogicalListModel);
        learnerList.setModel(learnerListModel);

        userList.setModel(userListModel);
        userListModel.addListDataListener(new ListDataListener() {

            @Override
            public void intervalRemoved(ListDataEvent e) {
                activeUsersCntLabel.setText(String.valueOf(userListModel.size()));
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                activeUsersCntLabel.setText(String.valueOf(userListModel.size()));
            }

            @Override
            public void contentsChanged(ListDataEvent arg0) {
            }
        });

        domainSessionList.setModel(domainSessionListModel);
        domainSessionListModel.addListDataListener(new ListDataListener() {

            @Override
            public void intervalRemoved(ListDataEvent e) {
                activeDomainSessionsCntLabel.setText(String.valueOf(domainSessionListModel.size()));
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                activeDomainSessionsCntLabel.setText(String.valueOf(domainSessionListModel.size()));
            }

            @Override
            public void contentsChanged(ListDataEvent arg0) {
            }
        });
        commandHistoryList.setModel(new DefaultListModel<String>());

        giftHostsListModel.addListDataListener(new LearnerStationListListener(giftHostsList));

        giftHostsList.addListSelectionListener(new HostListSelectionListener());
        giftHostsList.setModel(giftHostsListModel);

        //scriptFile = System.getProperty("user.dir") + File.separator + "scripts" +File.separator+ "launchProcess.bat";

        for (ModuleTypeEnum i : ModuleTypeEnum.VALUES()) {
            setModuleOffline(i, "");
        }

        tuiWebpageButton.setEnabled(false);

        //onAdminServerOffline();

        //Add the host the monitor is on to the list of learner stations.
        giftHostsListModel.addElement(localHostInfo);

        URI messageBrokerURI = MonitorModule.getInstance().getMessageBrokerURI();

        if (messageBrokerURI != null && messageBrokerURI.getHost().equals("localhost")) {
            //get IP address (then URI) of localhost

            try {
                messageBrokerURI = new URI(messageBrokerURI.toString().replace("localhost", ipAddress));

            } catch (URISyntaxException e) {
                logger.error("Caught an URI syntax exception while getting localhost IP address", e);
            }
        }

        if (messageBrokerURI != null) {
            activeMqAddressLabel.setText(messageBrokerURI.toString());
        } else {
            throw new IllegalArgumentException("The message broker URI is null");
        }

        giftFolderLocationLabel.setText(new File("").getAbsolutePath());
        giftVersionLabel.setText(Version.getInstance().getName() + " ["+Version.getInstance().getReleaseDate()+"]");
    }

    /**
     * A module of the given type has come online, update the display components
     * for a module of that type.
     *
     * @param module The type of module online
     * @param queueName The module queue name
     */
    private void setModuleOnline(ModuleTypeEnum module, String queueName) {

        if (module == ModuleTypeEnum.TUTOR_MODULE) {
            addTutorModule(queueName);
        } else if (module == ModuleTypeEnum.DOMAIN_MODULE) {
            addDomainModule(queueName);
        } else if (module == ModuleTypeEnum.SENSOR_MODULE) {
            addSensorModule(queueName);
        } else if (module == ModuleTypeEnum.GATEWAY_MODULE) {
            addGatewayModule(queueName);
        } else if (module == ModuleTypeEnum.UMS_MODULE) {
            addUMSModule(queueName);
        } else if (module == ModuleTypeEnum.LMS_MODULE) {
            addLMSModule(queueName);
        } else if (module == ModuleTypeEnum.PEDAGOGICAL_MODULE) {
            addPedagogicalModule(queueName);
        } else if (module == ModuleTypeEnum.LEARNER_MODULE) {
            addLearnerModule(queueName);
        }

        if(!umsListModel.isEmpty()) {

            launchAllModulesButton.setEnabled(false);
            serverLaunchAllButton.setEnabled(false);
            umsButton.setEnabled(false);
        }

        if(!lmsListModel.isEmpty()) {

            launchAllModulesButton.setEnabled(false);
            serverLaunchAllButton.setEnabled(false);
            lmsButton.setEnabled(false);
        }

        killAllButton.setEnabled(true);
    }

    /**
     * A module of the given type has gone offline, update the display
     * components for a module of that type.
     *
     * @param module The type of module offline
     * @param queueName The module queue name
     */
    private void setModuleOffline(ModuleTypeEnum module, String queueName) {

        if (module == ModuleTypeEnum.TUTOR_MODULE) {
            removeTutorModule(queueName);
        } else if (module == ModuleTypeEnum.DOMAIN_MODULE) {
            removeDomainModule(queueName);
        } else if (module == ModuleTypeEnum.SENSOR_MODULE) {
            removeSensorModule(queueName);
        } else if (module == ModuleTypeEnum.GATEWAY_MODULE) {
            removeGatewayModule(queueName);
        } else if (module == ModuleTypeEnum.UMS_MODULE) {
            removeUMSModule(queueName);
        } else if (module == ModuleTypeEnum.LMS_MODULE) {
            removeLMSModule(queueName);
        } else if (module == ModuleTypeEnum.PEDAGOGICAL_MODULE) {
            removePedagogicalModule(queueName);
        } else if (module == ModuleTypeEnum.LEARNER_MODULE) {
            removeLearnerModule(queueName);
        }

        if (umsListModel.isEmpty() && lmsListModel.isEmpty()) {

            launchAllModulesButton.setEnabled(isLaunchEnabled() && !isMultipleGiftHostsSelected());
            serverLaunchAllButton.setEnabled(isLaunchEnabled() && !isMultipleGiftHostsSelected());
        }

        if (umsListModel.isEmpty()) {

            umsButton.setEnabled(isLaunchEnabled() && !isMultipleGiftHostsSelected());
        }

        if (lmsListModel.isEmpty()) {

            lmsButton.setEnabled(isLaunchEnabled() && !isMultipleGiftHostsSelected());
        }

        if(isAllModulesOffline()) {

            killAllButton.setEnabled(false);
        }
    }

    /**
     * Add a user to the user list
     *
     * @param userId The user ID
     */
    private void addUser(UserSession userSession) {

        ActiveUserListEntry item = new ActiveUserListEntry(userSession);

        if (!userListModel.contains(item)) {
            userListModel.addElement(item);
        }
    }

    /**
     * Removes a user from the user list
     *
     * @param userId The user ID
     */
    private void removeUser(UserSession userSession) {

        ActiveUserListEntry item = new ActiveUserListEntry(userSession);

        if (userListModel.contains(item)) {
            userListModel.removeElement(item);
        }
        
        // fail safe - if a user is removed the domain session that user was just in.  Should only be 1 domain session.
        // #5075 - lingering domain sessions issue
        for(int index = 0; index < domainSessionListModel.getSize(); index++){
            DomainSessionListEntry entry = domainSessionListModel.get(index);
            if(entry.isUserSession(userSession)){
                domainSessionListModel.removeElement(entry);
                break;
            }
        }
        
    }

    /**
     * Adds a domain session to the domain session list
     *
     * @param domainSession The domain session ID
     */
    private void addDomainSession(DomainSession domainSession) {
        DomainSessionListEntry entry = new DomainSessionListEntry(domainSession);
        if (!domainSessionListModel.contains(entry)) {
            domainSessionListModel.addElement(entry);
        }
    }

    /**
     * Removes a domain session from the domain session list
     *
     * @param domainSession The domain session ID
     */
    private void removeDomainSession(DomainSession domainSession) {
        DomainSessionListEntry entry = new DomainSessionListEntry(domainSession);
        if (domainSessionListModel.contains(entry)) {
            domainSessionListModel.removeElement(entry);
        }
    }

    /**
     * Adds a tutor module to the tutor module list
     *
     * @param queueName The tutor module queue name
     */
    private void addTutorModule(String queueName) {
        if (!tutorListModel.contains(queueName)) {
            tutorListModel.addElement(queueName);
        }
    }
    /**
     * Removes a tutor module from the tutor module list
     *
     * @param queueName The tutor module queue name.  Can't be null.
     */
    private void removeTutorModule(String queueName) {
        if (tutorListModel.contains(queueName)) {
            tutorListModel.removeElement(queueName);
        }
        if (tutorWebClientURI != null) {
            String queuePrefix = SubjectUtil.TUTOR_QUEUE_PREFIX + AbstractModule.ADDRESS_TOKEN_DELIM;
            String tutorWebClientHost = queueName.substring(queueName.lastIndexOf(queuePrefix) + queuePrefix.length(), queueName.lastIndexOf(AbstractModule.ADDRESS_TOKEN_DELIM));
            if (tutorWebClientHost.equals(tutorWebClientURI.getHost())) {

                tutorWebClientURI = null;
                tuiWebpageButton.setEnabled(false);
            }
        }
    }

    /**
     * Adds a sensor module to the sensor module list
     *
     * @param queueName The sensor module queue name
     */
    private void addSensorModule(String queueName) {
        if (!sensorListModel.contains(queueName)) {
            sensorListModel.addElement(queueName);
        }
    }

    /**
     * Removes a sensor module from the sensor module list
     *
     * @param queueName The sensor module queue name
     */
    private void removeSensorModule(String queueName) {
        if (sensorListModel.contains(queueName)) {
            sensorListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a domain module to the domain module list
     *
     * @param queueName The domain module queue name
     */
    private void addDomainModule(String queueName) {
        if (!domainListModel.contains(queueName)) {
            domainListModel.addElement(queueName);
        }
    }

    /**
     * Removes a domain module from the domain module list
     *
     * @param queueName The domain module queue name
     */
    private void removeDomainModule(String queueName) {
        if (domainListModel.contains(queueName)) {
            domainListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a gateway module to the gateway module list
     *
     * @param queueName The gateway module queue name
     */
    private void addGatewayModule(String queueName) {
        if (!gatewayListModel.contains(queueName)) {
            gatewayListModel.addElement(queueName);
        }
    }

    /**
     * Removes a gateway module from the gateway module list
     *
     * @param queueName The gateway module queue name
     */
    private void removeGatewayModule(String queueName) {
        if (gatewayListModel.contains(queueName)) {
            gatewayListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a learner module to the learner module list
     *
     * @param queueName The learner module queue name
     */
    private void addLearnerModule(String queueName) {
        if (!learnerListModel.contains(queueName)) {
            learnerListModel.addElement(queueName);
        }
    }

    /**
     * Removes a learner module from the learner module list
     *
     * @param queueName The learner module queue name
     */
    private void removeLearnerModule(String queueName) {
        if (learnerListModel.contains(queueName)) {
            learnerListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a ped module to the ped module list
     *
     * @param queueName The ped module queue name
     */
    private void addPedagogicalModule(String queueName) {
        if (!pedagogicalListModel.contains(queueName)) {
            pedagogicalListModel.addElement(queueName);
        }
    }

    /**
     * Removes a ped module from the ped module list
     *
     * @param queueName The ped module queue name
     */
    private void removePedagogicalModule(String queueName) {
        if (pedagogicalListModel.contains(queueName)) {
            pedagogicalListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a UMS module
     *
     * @param queueName The UMS module queue name
     */
    private void addUMSModule(String queueName) {
        if (!umsListModel.contains(queueName)) {
            umsListModel.addElement(queueName);
        }
    }

    /**
     * Removes a UMS module
     *
     * @param queueName The UMS module queue name
     */
    private void removeUMSModule(String queueName) {
        if (umsListModel.contains(queueName)) {
            umsListModel.removeElement(queueName);
        }
    }

    /**
     * Adds a LMS module
     *
     * @param queueName The LMS module queue name
     */
    private void addLMSModule(String queueName) {
        if (!lmsListModel.contains(queueName)) {
            lmsListModel.addElement(queueName);
        }
    }

    /**
     * Removes a LMS module
     *
     * @param queueName The LMS module queue name
     */
    private void removeLMSModule(String queueName) {
        if (lmsListModel.contains(queueName)) {
            lmsListModel.removeElement(queueName);
        }
    }

    /**
     * Returns if all modules are offline
     *
     * @return boolean True if all modules are offline
     */
    private boolean isAllModulesOffline() {

        return umsListModel.isEmpty()
                && lmsListModel.isEmpty()
                && pedagogicalListModel.isEmpty()
                && learnerListModel.isEmpty()
                && tutorListModel.isEmpty()
                && sensorListModel.isEmpty()
                && domainListModel.isEmpty()
                && gatewayListModel.isEmpty();
    }

    /**
     * Returns if modules can be launched
     *
     * @return boolean True if modules can be launched
     */
    private boolean isLaunchEnabled() {

        return giftHostsList.getSelectedIndex() > -1;
    }

    /**
     * Returns if multiple remote clients are selected
     *
     * @return True if multiple remote clients are selected
     */
    private boolean isMultipleGiftHostsSelected() {

        return giftHostsList.getSelectedValuesList().size() > 1;
    }

    /**
     * Sets if launch buttons should be enabled
     *
     * @param enable If the launch buttons should be enabled
     */
    private void enableLaunchButtons(boolean enable) {

        launchAllModulesButton.setEnabled(enable && umsListModel.isEmpty() && lmsListModel.isEmpty() && !isMultipleGiftHostsSelected());
        serverLaunchAllButton.setEnabled(enable && umsListModel.isEmpty() && lmsListModel.isEmpty() && !isMultipleGiftHostsSelected());
        learnerStationLaunchAllButton.setEnabled(enable);
        umsButton.setEnabled(enable && umsListModel.isEmpty() && !isMultipleGiftHostsSelected());
        lmsButton.setEnabled(enable && lmsListModel.isEmpty() && !isMultipleGiftHostsSelected());
        pedagogicalButton.setEnabled(enable);
        learnerButton.setEnabled(enable);
        tutorButton.setEnabled(enable);
        sensorButton.setEnabled(enable);
        domainButton.setEnabled(enable);
        gatewayButton.setEnabled(enable);
        tuiWebpageButton.setEnabled(enable && !tutorListModel.isEmpty());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel16 = new javax.swing.JPanel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        giftHostsList = new javax.swing.JList<>();
        filler34 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 10), new java.awt.Dimension(10, 10), new java.awt.Dimension(10, 10));
        jLabel3 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        commandHistoryList = new javax.swing.JList<>();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel23 = new javax.swing.JPanel();
        listPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        filler59 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20));
        umsPowerButton = new javax.swing.JLabel();
        filler56 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20));
        lmsPowerButton = new javax.swing.JLabel();
        filler57 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20));
        pedPanel = new javax.swing.JPanel();
        pedLabel = new javax.swing.JLabel();
        filler29 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel25 = new javax.swing.JPanel();
        pedListPowerButton = new javax.swing.JLabel();
        filler67 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane9 = new javax.swing.JScrollPane();
        pedList = new javax.swing.JList<>();
        filler33 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        learnerPanel = new javax.swing.JPanel();
        learnerLabel = new javax.swing.JLabel();
        filler35 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel26 = new javax.swing.JPanel();
        learnerListPowerButton = new javax.swing.JLabel();
        filler68 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane10 = new javax.swing.JScrollPane();
        learnerList = new javax.swing.JList<>();
        filler39 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel27 = new javax.swing.JPanel();
        killAllButton = new javax.swing.JButton();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 10), new java.awt.Dimension(10, 10), new java.awt.Dimension(10, 10));
        topModuleListPanel = new javax.swing.JPanel();
        sensorPanel = new javax.swing.JPanel();
        sensorLabel = new javax.swing.JLabel();
        filler26 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel12 = new javax.swing.JPanel();
        sensorListPowerButton = new javax.swing.JLabel();
        filler52 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane2 = new javax.swing.JScrollPane();
        sensorList = new javax.swing.JList<>();
        filler25 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        tutorPanel = new javax.swing.JPanel();
        tutorLabel = new javax.swing.JLabel();
        filler27 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel13 = new javax.swing.JPanel();
        tutorListPowerButton = new javax.swing.JLabel();
        filler53 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane1 = new javax.swing.JScrollPane();
        tutorList = new javax.swing.JList<>();
        filler28 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        domainPanel = new javax.swing.JPanel();
        domainLabel = new javax.swing.JLabel();
        filler22 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel14 = new javax.swing.JPanel();
        domainListPowerButton = new javax.swing.JLabel();
        filler54 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane4 = new javax.swing.JScrollPane();
        domainList = new javax.swing.JList<>();
        filler21 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        gatewayPanel = new javax.swing.JPanel();
        gatewayLabel = new javax.swing.JLabel();
        filler23 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel15 = new javax.swing.JPanel();
        gatewayListPowerButton = new javax.swing.JLabel();
        filler55 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jScrollPane3 = new javax.swing.JScrollPane();
        gatewayList = new javax.swing.JList<>();
        filler24 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel2 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel6 = new javax.swing.JPanel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jLabel4 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        launchAllModulesButton = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel5 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        serverLaunchAllButton = new javax.swing.JButton();
        filler66 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        umsButton = new javax.swing.JButton();
        filler30 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        lmsButton = new javax.swing.JButton();
        filler31 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        filler37 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        filler41 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        filler38 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel7 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        learnerStationLaunchAllButton = new javax.swing.JButton();
        filler42 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        sensorButton = new javax.swing.JButton();
        filler43 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        pedagogicalButton = new javax.swing.JButton();
        filler36 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        domainButton = new javax.swing.JButton();
        filler44 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        gatewayButton = new javax.swing.JButton();
        filler47 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        learnerButton = new javax.swing.JButton();
        filler48 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        tutorButton = new javax.swing.JButton();
        filler46 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        jPanel10 = new javax.swing.JPanel();
        tuiWebpageButton = new javax.swing.JButton();
        filler45 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5), new java.awt.Dimension(5, 5));
        topModuleListPanel1 = new javax.swing.JPanel();
        domainPanel4 = new javax.swing.JPanel();
        domainPanel5 = new javax.swing.JPanel();
        helpIcon = createImageIcon("images/help.png", "Help");
        helpButton1 = new JButton(helpIcon);
        helpButton2 = new JButton(helpIcon);
        activeUsersLabel = new javax.swing.JLabel();
        activeUsersCntLabel = new javax.swing.JLabel();
        filler19 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        filler70 = new javax.swing.Box.Filler(new java.awt.Dimension(495, 0), new java.awt.Dimension(495, 0), new java.awt.Dimension(495, 0));
        filler71 = new javax.swing.Box.Filler(new java.awt.Dimension(445, 0), new java.awt.Dimension(445, 0), new java.awt.Dimension(445, 0));
        jScrollPane5 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList<>();
        domainPanel2 = new javax.swing.JPanel();
        domainPanel3 = new javax.swing.JPanel();
        activeDomainSessionsLabel = new javax.swing.JLabel();
        activeDomainSessionsCntLabel = new javax.swing.JLabel();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(32767, 5));
        jScrollPane6 = new javax.swing.JScrollPane();
        domainSessionList = new javax.swing.JList<>();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        jPanel4 = new javax.swing.JPanel();
        monitorDomainSessionButton = new javax.swing.JButton();
        filler32 = new javax.swing.Box.Filler(new java.awt.Dimension(275, 0), new java.awt.Dimension(275, 0), new java.awt.Dimension(275, 0));
        endDomainSession = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel24 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        activeMqAddressLabel = new javax.swing.JLabel();
        filler65 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        jPanel21 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        giftVersionLabel = new javax.swing.JLabel();
        filler69 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        jLabel9 = new javax.swing.JLabel();
        giftFolderLocationLabel = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(670, 700));
        setMinimumSize(new java.awt.Dimension(670, 700));
        setPreferredSize(new java.awt.Dimension(670, 700));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jTabbedPane1.setAlignmentX(0.0F);
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(670, 700));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(670, 700));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(670, 700));

        jPanel16.setMaximumSize(new java.awt.Dimension(670, 683));
        jPanel16.setMinimumSize(new java.awt.Dimension(670, 683));
        jPanel16.setPreferredSize(new java.awt.Dimension(670, 683));
        jPanel16.setLayout(new javax.swing.BoxLayout(jPanel16, javax.swing.BoxLayout.LINE_AXIS));
        jPanel16.add(filler2);

        jPanel3.setAlignmentX(0.0F);
        jPanel3.setAlignmentY(0.0F);
        jPanel3.setAutoscrolls(true);
        jPanel3.setMaximumSize(new java.awt.Dimension(175, 65572));
        jPanel3.setMinimumSize(new java.awt.Dimension(175, 350));
        jPanel3.setPreferredSize(new java.awt.Dimension(175, 350));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));

        jLabel2.setText("GIFT Hosts");
        jLabel2.setAlignmentY(0.0F);
        jLabel2.setMaximumSize(new java.awt.Dimension(9999, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(0, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel3.add(jLabel2);

        jScrollPane7.setAlignmentX(0.0F);
        jScrollPane7.setMaximumSize(new java.awt.Dimension(200, 150));
        jScrollPane7.setMinimumSize(new java.awt.Dimension(200, 150));
        jScrollPane7.setPreferredSize(new java.awt.Dimension(200, 150));

        giftHostsList.setToolTipText("The list of hosts that can launch GIFT modules.");
        giftHostsList.setAlignmentX(0.0F);
        giftHostsList.setAlignmentY(0.0F);
        giftHostsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                giftHostsListValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(giftHostsList);

        jPanel3.add(jScrollPane7);

        filler34.setAlignmentX(0.0F);
        filler34.setAlignmentY(0.0F);
        jPanel3.add(filler34);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Launch Command History");
        jLabel3.setAlignmentY(0.0F);
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel3.setMaximumSize(new java.awt.Dimension(9999, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(0, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel3.add(jLabel3);

        jScrollPane8.setAlignmentX(0.0F);
        jScrollPane8.setMaximumSize(new java.awt.Dimension(200, 9999));
        jScrollPane8.setMinimumSize(new java.awt.Dimension(200, 125));
        jScrollPane8.setPreferredSize(new java.awt.Dimension(200, 125));

        commandHistoryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        commandHistoryList.setToolTipText("A history of commands (e.g. launch UMS module, kill Domain module) issued by this instance of the Monitor.");
        commandHistoryList.setAlignmentX(0.0F);
        commandHistoryList.setAlignmentY(0.0F);
        jScrollPane8.setViewportView(commandHistoryList);

        jPanel3.add(jScrollPane8);

        jPanel16.add(jPanel3);
        jPanel16.add(filler4);

        jPanel23.setAlignmentY(0.0F);
        jPanel23.setAutoscrolls(true);
        jPanel23.setMaximumSize(new java.awt.Dimension(475, 684));
        jPanel23.setMinimumSize(new java.awt.Dimension(475, 684));
        jPanel23.setPreferredSize(new java.awt.Dimension(475, 684));
        jPanel23.setLayout(new javax.swing.BoxLayout(jPanel23, javax.swing.BoxLayout.PAGE_AXIS));

        listPanel.setToolTipText("");
        listPanel.setAlignmentX(0.0F);
        listPanel.setAutoscrolls(true);
        listPanel.setMaximumSize(new java.awt.Dimension(475, 300));
        listPanel.setMinimumSize(new java.awt.Dimension(475, 300));
        listPanel.setPreferredSize(new java.awt.Dimension(475, 300));
        listPanel.setLayout(new javax.swing.BoxLayout(listPanel, javax.swing.BoxLayout.LINE_AXIS));

        jPanel11.setAlignmentX(0.0F);
        jPanel11.setAlignmentY(0.0F);
        jPanel11.setMaximumSize(new java.awt.Dimension(230, 300));
        jPanel11.setMinimumSize(new java.awt.Dimension(230, 300));
        jPanel11.setPreferredSize(new java.awt.Dimension(230, 300));
        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.PAGE_AXIS));

        filler59.setAlignmentX(0.0F);
        jPanel11.add(filler59);

        umsPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        umsPowerButton.setText("UMS Module");
        umsPowerButton.setToolTipText("Press when the icon is green to kill the UMS module.");
        umsPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                umsPowerButtonMouseClicked(evt);
            }
        });
        jPanel11.add(umsPowerButton);

        filler56.setAlignmentX(0.0F);
        jPanel11.add(filler56);

        lmsPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        lmsPowerButton.setText("LMS Module");
        lmsPowerButton.setToolTipText("Press when the icon is green to kill the LMS module.");
        lmsPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lmsPowerButtonMouseClicked(evt);
            }
        });
        jPanel11.add(lmsPowerButton);

        filler57.setAlignmentX(0.0F);
        jPanel11.add(filler57);

        pedPanel.setLayout(new javax.swing.BoxLayout(pedPanel, javax.swing.BoxLayout.PAGE_AXIS));

        pedLabel.setText("Pedagogical Modules");
        pedPanel.add(pedLabel);
        pedPanel.add(filler29);

        jPanel25.setAlignmentX(0.0F);
        jPanel25.setAlignmentY(0.0F);
        jPanel25.setLayout(new javax.swing.BoxLayout(jPanel25, javax.swing.BoxLayout.LINE_AXIS));

        pedListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        pedListPowerButton.setToolTipText("Press when a Pedagogical Module is selected to kill that module.");
        pedListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pedListPowerButtonMouseClicked(evt);
            }
        });
        jPanel25.add(pedListPowerButton);
        jPanel25.add(filler67);

        jScrollPane9.setAlignmentX(0.0F);
        jScrollPane9.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane9.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane9.setPreferredSize(new java.awt.Dimension(200, 50));

        pedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pedList.setToolTipText("List of Pedagogical modules that are online.");
        pedList.setVisibleRowCount(3);
        jScrollPane9.setViewportView(pedList);

        jPanel25.add(jScrollPane9);

        pedPanel.add(jPanel25);
        pedPanel.add(filler33);

        jPanel11.add(pedPanel);

        learnerPanel.setLayout(new javax.swing.BoxLayout(learnerPanel, javax.swing.BoxLayout.PAGE_AXIS));

        learnerLabel.setText("Learner Modules");
        learnerPanel.add(learnerLabel);
        learnerPanel.add(filler35);

        jPanel26.setAlignmentX(0.0F);
        jPanel26.setAlignmentY(0.0F);
        jPanel26.setLayout(new javax.swing.BoxLayout(jPanel26, javax.swing.BoxLayout.LINE_AXIS));

        learnerListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        learnerListPowerButton.setToolTipText("Press when a Learner Module is selected to kill that module.");
        learnerListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                learnerListPowerButtonMouseClicked(evt);
            }
        });
        jPanel26.add(learnerListPowerButton);
        jPanel26.add(filler68);

        jScrollPane10.setAlignmentX(0.0F);
        jScrollPane10.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane10.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane10.setPreferredSize(new java.awt.Dimension(200, 50));

        learnerList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        learnerList.setToolTipText("List of Learner modules that are online.");
        learnerList.setVisibleRowCount(3);
        jScrollPane10.setViewportView(learnerList);

        jPanel26.add(jScrollPane10);

        learnerPanel.add(jPanel26);
        learnerPanel.add(filler39);

        jPanel11.add(learnerPanel);

        filler6.setAlignmentX(0.0F);
        jPanel11.add(filler6);

        jPanel27.setAlignmentX(0.0F);
        jPanel27.setMaximumSize(new java.awt.Dimension(32767, 23));
        jPanel27.setMinimumSize(new java.awt.Dimension(111, 23));

        killAllButton.setText("Kill All Modules");
        killAllButton.setToolTipText("Notify all modules known to this monitor to close.");
        killAllButton.setAlignmentY(0.0F);
        killAllButton.setEnabled(false);
        killAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                killAllButtonActionPerformed(evt);
            }
        });
        jPanel27.add(killAllButton);

        jPanel11.add(jPanel27);

        listPanel.add(jPanel11);

        filler9.setAlignmentX(0.0F);
        filler9.setAlignmentY(0.0F);
        listPanel.add(filler9);

        topModuleListPanel.setAlignmentX(0.0F);
        topModuleListPanel.setAlignmentY(0.0F);
        topModuleListPanel.setLayout(new javax.swing.BoxLayout(topModuleListPanel, javax.swing.BoxLayout.PAGE_AXIS));

        sensorPanel.setLayout(new javax.swing.BoxLayout(sensorPanel, javax.swing.BoxLayout.PAGE_AXIS));

        sensorLabel.setText("Sensor Modules");
        sensorPanel.add(sensorLabel);
        sensorPanel.add(filler26);

        jPanel12.setAlignmentX(0.0F);
        jPanel12.setAlignmentY(0.0F);
        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        sensorListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        sensorListPowerButton.setToolTipText("Press when a Sensor Module is selected to kill that module.");
        sensorListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sensorListPowerButtonMouseClicked(evt);
            }
        });
        jPanel12.add(sensorListPowerButton);
        jPanel12.add(filler52);

        jScrollPane2.setAlignmentX(0.0F);
        jScrollPane2.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 50));

        sensorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sensorList.setToolTipText("List of Sensor modules that are online.");
        sensorList.setVisibleRowCount(3);
        jScrollPane2.setViewportView(sensorList);

        jPanel12.add(jScrollPane2);

        sensorPanel.add(jPanel12);
        sensorPanel.add(filler25);

        topModuleListPanel.add(sensorPanel);

        tutorPanel.setLayout(new javax.swing.BoxLayout(tutorPanel, javax.swing.BoxLayout.PAGE_AXIS));

        tutorLabel.setText("Tutor Modules");
        tutorPanel.add(tutorLabel);
        tutorPanel.add(filler27);

        jPanel13.setAlignmentX(0.0F);
        jPanel13.setAlignmentY(0.0F);
        jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.LINE_AXIS));

        tutorListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        tutorListPowerButton.setToolTipText("Press when a Tutor Module is selected to kill that module.");
        tutorListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tutorListPowerButtonMouseClicked(evt);
            }
        });
        jPanel13.add(tutorListPowerButton);
        jPanel13.add(filler53);

        jScrollPane1.setAlignmentX(0.0F);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 50));

        tutorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tutorList.setToolTipText("List of Tutor modules that are online.");
        tutorList.setPreferredSize(new java.awt.Dimension(100, 0));
        tutorList.setVisibleRowCount(3);
        jScrollPane1.setViewportView(tutorList);

        jPanel13.add(jScrollPane1);

        tutorPanel.add(jPanel13);
        tutorPanel.add(filler28);

        topModuleListPanel.add(tutorPanel);

        domainPanel.setLayout(new javax.swing.BoxLayout(domainPanel, javax.swing.BoxLayout.PAGE_AXIS));

        domainLabel.setText("Domain Modules");
        domainPanel.add(domainLabel);
        domainPanel.add(filler22);

        jPanel14.setAlignmentX(0.0F);
        jPanel14.setAlignmentY(0.0F);
        jPanel14.setLayout(new javax.swing.BoxLayout(jPanel14, javax.swing.BoxLayout.LINE_AXIS));

        domainListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        domainListPowerButton.setToolTipText("Press when a Domain Module is selected to kill that module.");
        domainListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                domainListPowerButtonMouseClicked(evt);
            }
        });
        jPanel14.add(domainListPowerButton);
        jPanel14.add(filler54);

        jScrollPane4.setAlignmentX(0.0F);
        jScrollPane4.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane4.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(200, 50));

        domainList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        domainList.setToolTipText("List of Domain modules that are online.");
        domainList.setVisibleRowCount(3);
        jScrollPane4.setViewportView(domainList);

        jPanel14.add(jScrollPane4);

        domainPanel.add(jPanel14);
        domainPanel.add(filler21);

        topModuleListPanel.add(domainPanel);

        gatewayPanel.setLayout(new javax.swing.BoxLayout(gatewayPanel, javax.swing.BoxLayout.PAGE_AXIS));

        gatewayLabel.setText("Gateway Modules");
        gatewayPanel.add(gatewayLabel);
        gatewayPanel.add(filler23);

        jPanel15.setAlignmentX(0.0F);
        jPanel15.setAlignmentY(0.0F);
        jPanel15.setLayout(new javax.swing.BoxLayout(jPanel15, javax.swing.BoxLayout.LINE_AXIS));

        gatewayListPowerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mil/arl/gift/tools/monitor/images/off_small.png"))); // NOI18N
        gatewayListPowerButton.setToolTipText("Press when a Gateway Module is selected to kill that module.");
        gatewayListPowerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gatewayListPowerButtonMouseClicked(evt);
            }
        });
        jPanel15.add(gatewayListPowerButton);
        jPanel15.add(filler55);

        jScrollPane3.setAlignmentX(0.0F);
        jScrollPane3.setMaximumSize(new java.awt.Dimension(200, 50));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(200, 50));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(200, 50));

        gatewayList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gatewayList.setToolTipText("List of Gateway modules that are online.");
        gatewayList.setAlignmentX(0.0F);
        gatewayList.setMaximumSize(new java.awt.Dimension(300, 0));
        gatewayList.setVisibleRowCount(3);
        jScrollPane3.setViewportView(gatewayList);

        jPanel15.add(jScrollPane3);

        gatewayPanel.add(jPanel15);
        gatewayPanel.add(filler24);

        topModuleListPanel.add(gatewayPanel);

        listPanel.add(topModuleListPanel);

        jPanel23.add(listPanel);

        jPanel2.setAlignmentX(0.0F);
        jPanel2.setAlignmentY(0.0F);
        jPanel2.setAutoscrolls(true);
        jPanel2.setMaximumSize(new java.awt.Dimension(625, 550));
        jPanel2.setMinimumSize(new java.awt.Dimension(625, 550));
        jPanel2.setPreferredSize(new java.awt.Dimension(625, 550));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add(filler1);

        jPanel6.setToolTipText("<html>Launch controls for the various GIFT modules.<br>Note: the module(s) will be started on the currently selected GIFT host machine.</html>");
        jPanel6.setAlignmentX(0.0F);
        jPanel6.setAlignmentY(0.0F);
        jPanel6.setMaximumSize(new java.awt.Dimension(425, 525));
        jPanel6.setMinimumSize(new java.awt.Dimension(425, 525));
        jPanel6.setPreferredSize(new java.awt.Dimension(425, 525));
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel6.add(filler3);

        jLabel4.setText("Use buttons below to launch GIFT Module(s) on selected GIFT Host(s)");
        jLabel4.setAlignmentX(0.5F);
        jPanel6.add(jLabel4);
        jPanel6.add(filler5);

        launchAllModulesButton.setText("Launch All Modules");
        launchAllModulesButton.setAlignmentX(0.5F);
        launchAllModulesButton.setEnabled(false);
        launchAllModulesButton.setMaximumSize(new java.awt.Dimension(200, 23));
        launchAllModulesButton.setMinimumSize(new java.awt.Dimension(200, 23));
        launchAllModulesButton.setPreferredSize(new java.awt.Dimension(200, 23));
        launchAllModulesButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchAllModulesButtonActionPerformed(evt);
            }
        });
        jPanel6.add(launchAllModulesButton);
        jPanel6.add(filler7);

        jPanel5.setMaximumSize(new java.awt.Dimension(405, 415));
        jPanel5.setMinimumSize(new java.awt.Dimension(405, 415));
        jPanel5.setPreferredSize(new java.awt.Dimension(405, 415));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        jPanel8.setToolTipText("");
        jPanel8.setAlignmentY(0.0F);
        jPanel8.setMaximumSize(new java.awt.Dimension(200, 450));
        jPanel8.setMinimumSize(new java.awt.Dimension(200, 450));
        jPanel8.setPreferredSize(new java.awt.Dimension(200, 450));
        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(153, 153, 153), null), "Server Modules"));
        jPanel18.setMaximumSize(new java.awt.Dimension(197, 118));
        jPanel18.setLayout(new javax.swing.BoxLayout(jPanel18, javax.swing.BoxLayout.PAGE_AXIS));

        serverLaunchAllButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        serverLaunchAllButton.setText("Launch All");
        serverLaunchAllButton.setToolTipText("Launch all server modules on the selected learner station(s).");
        serverLaunchAllButton.setEnabled(false);
        serverLaunchAllButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        serverLaunchAllButton.setMinimumSize(new java.awt.Dimension(100, 23));
        serverLaunchAllButton.setPreferredSize(new java.awt.Dimension(100, 23));
        serverLaunchAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverLaunchAllButtonActionPerformed(evt);
            }
        });
        jPanel18.add(serverLaunchAllButton);
        jPanel18.add(filler66);

        umsButton.setText("Launch UMS");
        umsButton.setEnabled(false);
        umsButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        umsButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        umsButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        umsButton.setPreferredSize(new java.awt.Dimension(0, 23));
        umsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        umsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                umsButtonActionPerformed(evt);
            }
        });
        jPanel18.add(umsButton);
        jPanel18.add(filler30);

        lmsButton.setText("Launch LMS");
        lmsButton.setEnabled(false);
        lmsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lmsButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        lmsButton.setPreferredSize(new java.awt.Dimension(0, 23));
        lmsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lmsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lmsButtonActionPerformed(evt);
            }
        });
        jPanel18.add(lmsButton);
        jPanel18.add(filler31);
        jPanel18.add(filler37);

        jPanel8.add(jPanel18);
        jPanel8.add(filler41);

        jPanel5.add(jPanel8);
        jPanel5.add(filler38);

        jPanel7.setAlignmentY(0.0F);
        jPanel7.setMaximumSize(new java.awt.Dimension(200, 400));
        jPanel7.setMinimumSize(new java.awt.Dimension(200, 400));
        jPanel7.setPreferredSize(new java.awt.Dimension(200, 400));
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(153, 153, 153), null), "Other Modules"));
        jPanel19.setToolTipText("");
        jPanel19.setMaximumSize(new java.awt.Dimension(198, 249));
        jPanel19.setMinimumSize(new java.awt.Dimension(198, 249));
        jPanel19.setPreferredSize(new java.awt.Dimension(198, 249));
        jPanel19.setLayout(new javax.swing.BoxLayout(jPanel19, javax.swing.BoxLayout.PAGE_AXIS));

        learnerStationLaunchAllButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        learnerStationLaunchAllButton.setText("Launch All");
        learnerStationLaunchAllButton.setToolTipText("Launch all client modules on the selected learner station(s).");
        learnerStationLaunchAllButton.setEnabled(false);
        learnerStationLaunchAllButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        learnerStationLaunchAllButton.setMinimumSize(new java.awt.Dimension(0, 23));
        learnerStationLaunchAllButton.setPreferredSize(new java.awt.Dimension(100, 23));
        learnerStationLaunchAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                learnerStationLaunchAllButtonActionPerformed(evt);
            }
        });
        jPanel19.add(learnerStationLaunchAllButton);
        jPanel19.add(filler42);

        sensorButton.setText("Launch Sensor");
        sensorButton.setEnabled(false);
        sensorButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        sensorButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        sensorButton.setPreferredSize(new java.awt.Dimension(0, 23));
        sensorButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sensorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sensorButtonActionPerformed(evt);
            }
        });
        jPanel19.add(sensorButton);
        jPanel19.add(filler43);

        pedagogicalButton.setText("Launch Pedagogical");
        pedagogicalButton.setEnabled(false);
        pedagogicalButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pedagogicalButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        pedagogicalButton.setPreferredSize(new java.awt.Dimension(0, 23));
        pedagogicalButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pedagogicalButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pedagogicalButtonActionPerformed(evt);
            }
        });
        jPanel19.add(pedagogicalButton);
        jPanel19.add(filler36);

        domainButton.setText("Launch Domain");
        domainButton.setEnabled(false);
        domainButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        domainButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        domainButton.setPreferredSize(new java.awt.Dimension(0, 23));
        domainButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        domainButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainButtonActionPerformed(evt);
            }
        });
        jPanel19.add(domainButton);
        jPanel19.add(filler44);

        gatewayButton.setText("Launch Gateway");
        gatewayButton.setEnabled(false);
        gatewayButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gatewayButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        gatewayButton.setPreferredSize(new java.awt.Dimension(0, 23));
        gatewayButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gatewayButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gatewayButtonActionPerformed(evt);
            }
        });
        jPanel19.add(gatewayButton);
        jPanel19.add(filler47);

        learnerButton.setText("Launch Learner");
        learnerButton.setEnabled(false);
        learnerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        learnerButton.setMaximumSize(new java.awt.Dimension(500, 23));
        learnerButton.setPreferredSize(new java.awt.Dimension(0, 23));
        learnerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        learnerButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                learnerButtonActionPerformed(evt);
            }
        });
        jPanel19.add(learnerButton);
        jPanel19.add(filler48);

        tutorButton.setText("Launch Tutor");
        tutorButton.setEnabled(false);
        tutorButton.setMaximumSize(new java.awt.Dimension(9999, 23));
        tutorButton.setPreferredSize(new java.awt.Dimension(0, 23));
        tutorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tutorButtonActionPerformed(evt);
            }
        });
        jPanel19.add(tutorButton);

        jPanel7.add(jPanel19);
        jPanel7.add(filler46);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(153, 153, 153), null), "Simple Sign-On Webpage"));
        jPanel10.setAlignmentX(0.0F);
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

        tuiWebpageButton.setText(DEFAULT_TUI_WEBPAGE_BUTTON_LABEL);
        tuiWebpageButton.setToolTipText("<html>Opens the Simple Sign-On webpage.<br>For other deployment modes please launch the GIFT Dashboard.</html>");
        tuiWebpageButton.setEnabled(false);
        tuiWebpageButton.setMaximumSize(new java.awt.Dimension(9999, 40));
        tuiWebpageButton.setPreferredSize(new java.awt.Dimension(0, 40));
        tuiWebpageButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tuiWebpageButtonActionPerformed(evt);
            }
        });
        jPanel10.add(tuiWebpageButton);

        jPanel7.add(jPanel10);
        jPanel7.add(filler45);

        jPanel5.add(jPanel7);

        jPanel6.add(jPanel5);

        jPanel2.add(jPanel6);

        jPanel23.add(jPanel2);

        jPanel16.add(jPanel23);

        jTabbedPane1.addTab("Module Control", jPanel16);

        topModuleListPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Sessions"));
        topModuleListPanel1.setMaximumSize(new java.awt.Dimension(9999, 150));
        topModuleListPanel1.setMinimumSize(new java.awt.Dimension(0, 150));

        helpButton1.setPreferredSize(new Dimension(helpIcon.getIconWidth(), helpIcon.getIconHeight()));
        helpButton1.setOpaque(false);
        helpButton1.setContentAreaFilled(false);
        helpButton1.setBorderPainted(false);
        helpButton1.setToolTipText("<html>A user will be listed in the list of active users after logging<br />into GIFT and will remain until the user is logged out.</html>");

        helpButton2.setPreferredSize(new Dimension(helpIcon.getIconWidth(), helpIcon.getIconHeight()));
        helpButton2.setOpaque(false);
        helpButton2.setContentAreaFilled(false);
        helpButton2.setBorderPainted(false);
        helpButton2.setToolTipText("<html>A domain session will be listed in the list of active domain sessions<br />while a user is in a course and will remain until the course has ended.<br /><br />You can monitor the sensors, learner state and course message traffic<br />for each domain session.</html>");

        domainPanel4.setAlignmentX(0.0F);
        domainPanel4.setLayout(new javax.swing.BoxLayout(domainPanel4, javax.swing.BoxLayout.PAGE_AXIS));

        domainPanel5.setAlignmentX(0.0F);
        domainPanel5.setLayout(new javax.swing.BoxLayout(domainPanel5, javax.swing.BoxLayout.X_AXIS));

        activeUsersLabel.setText("Active Users");
        EmptyBorder activeUsersLabelBorder = new EmptyBorder(0, 0, 0, 20);
        activeUsersLabel.setBorder(activeUsersLabelBorder);
        domainPanel5.add(activeUsersLabel);
        Font cntFont = new Font("Times", Font.BOLD,12);
        activeUsersCntLabel.setFont(cntFont);
        activeUsersCntLabel.setText("0");
        domainPanel5.add(activeUsersCntLabel);
        domainPanel5.add(filler70);
        domainPanel5.add(helpButton1);
        domainPanel4.add(domainPanel5);
        domainPanel4.add(filler19);

        jScrollPane5.setAlignmentX(0.0F);
        jScrollPane5.setMaximumSize(new java.awt.Dimension(600, 275));
        jScrollPane5.setMinimumSize(new java.awt.Dimension(220, 275));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(600, 200));

        userList.setToolTipText("List of active users known to this monitor.");
        userList.setVisibleRowCount(3);
        jScrollPane5.setViewportView(userList);

        domainPanel4.add(jScrollPane5);

        domainPanel2.setAlignmentY(0.0F);
        domainPanel2.setLayout(new javax.swing.BoxLayout(domainPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        domainPanel3.setAlignmentX(0.0F);
        domainPanel3.setLayout(new javax.swing.BoxLayout(domainPanel3, javax.swing.BoxLayout.X_AXIS));

        activeDomainSessionsLabel.setText("Active Domain Sessions");
        EmptyBorder activeDomainSessionsBorder = new EmptyBorder(0, 0, 0, 20);
        activeDomainSessionsLabel.setBorder(activeDomainSessionsBorder);
        domainPanel3.add(activeDomainSessionsLabel);
        activeDomainSessionsCntLabel.setText("0");
        activeDomainSessionsCntLabel.setFont(cntFont);
        domainPanel3.add(activeDomainSessionsCntLabel);
        domainPanel3.add(filler71);
        domainPanel3.add(helpButton2);
        domainPanel2.add(domainPanel3);
        domainPanel2.add(filler12);

        jScrollPane6.setAlignmentX(0.0F);
        jScrollPane6.setMaximumSize(new java.awt.Dimension(600, 300));
        jScrollPane6.setMinimumSize(new java.awt.Dimension(220, 250));
        jScrollPane6.setPreferredSize(new java.awt.Dimension(600, 250));

        domainSessionList.setToolTipText("List of active domain sessions known to this monitor.");
        domainSessionList.setVisibleRowCount(3);
        jScrollPane6.setViewportView(domainSessionList);

        domainPanel2.add(jScrollPane6);
        domainPanel2.add(filler20);

        jPanel4.setAlignmentX(0.0F);
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        monitorDomainSessionButton.setText("Monitor Domain Session");
        monitorDomainSessionButton.setToolTipText("Actively monitor the selected domain session.");
        monitorDomainSessionButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitorDomainSessionButtonActionPerformed(evt);
            }
        });
        jPanel4.add(monitorDomainSessionButton);
        jPanel4.add(filler32);

        endDomainSession.setText("End Session");
        endDomainSession.setToolTipText("Force the selected domain session to terminate prematurely.");
        endDomainSession.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endDomainSessionActionPerformed(evt);
            }
        });
        jPanel4.add(endDomainSession);

        domainPanel2.add(jPanel4);

        javax.swing.GroupLayout topModuleListPanel1Layout = new javax.swing.GroupLayout(topModuleListPanel1);
        topModuleListPanel1.setLayout(topModuleListPanel1Layout);
        topModuleListPanel1Layout.setHorizontalGroup(
            topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topModuleListPanel1Layout.createSequentialGroup()
                .addGroup(topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(topModuleListPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addComponent(domainPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(domainPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        topModuleListPanel1Layout.setVerticalGroup(
            topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topModuleListPanel1Layout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(domainPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(topModuleListPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					.addComponent(domainPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(342, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Active Sessions", topModuleListPanel1);

        jPanel24.setLayout(new javax.swing.BoxLayout(jPanel24, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(153, 153, 153), null), "System Status"));
        jPanel20.setToolTipText("Contains the addresses of the message bus and GAS.");
        jPanel20.setMaximumSize(new java.awt.Dimension(300, 100));
        jPanel20.setMinimumSize(new java.awt.Dimension(300, 100));
        jPanel20.setLayout(new javax.swing.BoxLayout(jPanel20, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.setToolTipText("The message bus address.");
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jLabel7.setText("ActiveMQ Address:");
        jLabel7.setMaximumSize(new java.awt.Dimension(200, 14));
        jLabel7.setMinimumSize(new java.awt.Dimension(0, 14));
        jLabel7.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel1.add(jLabel7);

        activeMqAddressLabel.setText("None");
        activeMqAddressLabel.setMaximumSize(new java.awt.Dimension(200, 14));
        activeMqAddressLabel.setMinimumSize(new java.awt.Dimension(0, 14));
        activeMqAddressLabel.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel1.add(activeMqAddressLabel);

        jPanel20.add(jPanel1);
        jPanel20.add(filler65);

        jPanel24.add(jPanel20);

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(153, 153, 153), null), "Misc"));
        jPanel21.setToolTipText("Contains the addresses of the message bus and GAS.");
        jPanel21.setMaximumSize(new java.awt.Dimension(300, 100));
        jPanel21.setMinimumSize(new java.awt.Dimension(300, 100));
        jPanel21.setLayout(new javax.swing.BoxLayout(jPanel21, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel9.setToolTipText("The message bus address.");
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.PAGE_AXIS));

        jLabel8.setText("GIFT Version:");
        jLabel8.setMaximumSize(new java.awt.Dimension(200, 14));
        jLabel8.setMinimumSize(new java.awt.Dimension(0, 14));
        jLabel8.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel9.add(jLabel8);

        giftVersionLabel.setText("None");
        giftVersionLabel.setMaximumSize(new java.awt.Dimension(200, 14));
        giftVersionLabel.setMinimumSize(new java.awt.Dimension(0, 14));
        giftVersionLabel.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel9.add(giftVersionLabel);

        jPanel21.add(jPanel9);
        jPanel21.add(filler69);

        jLabel9.setText("Location:");
        jLabel9.setMaximumSize(new java.awt.Dimension(200, 14));
        jLabel9.setMinimumSize(new java.awt.Dimension(0, 14));
        jLabel9.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel21.add(jLabel9);

        giftFolderLocationLabel.setText("unknown");
        giftFolderLocationLabel.setMaximumSize(new java.awt.Dimension(200, 14));
        giftFolderLocationLabel.setMinimumSize(new java.awt.Dimension(0, 14));
        giftFolderLocationLabel.setPreferredSize(new java.awt.Dimension(0, 14));
        jPanel21.add(giftFolderLocationLabel);

        jPanel24.add(jPanel21);

        jTabbedPane1.addTab("Admin", jPanel24);

        add(jTabbedPane1);

        getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path The absolute path of the image to create an icon from
     * @param description The description of the image
     * @return ImageIcon The image converted into an icon
     */
    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }

        System.err.println("Couldn't find file: " + path);
        return null;
    }

    private void umsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_umsButtonActionPerformed
        if (umsListModel.isEmpty()) {
            if (giftHostsList.getSelectedValuesList().size() == 1) {
                for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
                    HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
                    sendRemoteClientCommand(LaunchConstants.LAUNCH_UMS, learnerStationHostInfo);
                }
            }
        }
    }//GEN-LAST:event_umsButtonActionPerformed

    private void lmsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lmsButtonActionPerformed
        if (lmsListModel.isEmpty()) {
            if (giftHostsList.getSelectedValuesList().size() == 1) {
                for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
                    HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
                    sendRemoteClientCommand(LaunchConstants.LAUNCH_LMS, learnerStationHostInfo);
                }
            }
        }
    }//GEN-LAST:event_lmsButtonActionPerformed

    private void pedagogicalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pedagogicalButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_PED, learnerStationHostInfo);
        }
    }//GEN-LAST:event_pedagogicalButtonActionPerformed

    private void learnerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_learnerButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_LEARNER, learnerStationHostInfo);
        }
    }//GEN-LAST:event_learnerButtonActionPerformed

    private void sensorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sensorButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_SENSOR, learnerStationHostInfo);
        }
    }//GEN-LAST:event_sensorButtonActionPerformed

    private void domainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_DOMAIN, learnerStationHostInfo);
        }
    }//GEN-LAST:event_domainButtonActionPerformed

    private void gatewayButtonActionPerformed(java.awt.event.ActionEvent evt) {
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_GATEWAY, learnerStationHostInfo);
        }
    }

    private void killAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_killAllButtonActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to kill all modules?", "Kill Confirmation", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {

            logger.info("Killing all modules");

            for (Object i : Arrays.asList(umsListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.UMS_MODULE);
            }

            for (Object i : Arrays.asList(lmsListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.LMS_MODULE);
            }

            for (Object i : Arrays.asList(pedagogicalListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.PEDAGOGICAL_MODULE);
            }

            for (Object i : Arrays.asList(learnerListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.LEARNER_MODULE);
            }

            for (Object i : Arrays.asList(tutorListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.TUTOR_MODULE);
            }

            for (Object i : Arrays.asList(sensorListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.SENSOR_MODULE);
            }

            for (Object i : Arrays.asList(gatewayListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.GATEWAY_MODULE);
            }

            for (Object i : Arrays.asList(domainListModel.toArray())) {
                String iStr = (String) i;
                MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.DOMAIN_MODULE);
            }

        }
    }//GEN-LAST:event_killAllButtonActionPerformed

    private void endDomainSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endDomainSessionActionPerformed

        DomainSessionListEntry selected = domainSessionList.getSelectedValue();
        if (selected != null) {
            List<Object> domainModuleAddresses = Arrays.asList(domainListModel.toArray());
            for (Object domainModuleAddress : domainModuleAddresses) {
                MonitorModule.getInstance().closeDomainSession((String) domainModuleAddress, selected.domainSession);
            }

        }
    }//GEN-LAST:event_endDomainSessionActionPerformed

    private void tutorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tutorButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {

            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_TUTOR, learnerStationHostInfo);
        }
    }//GEN-LAST:event_tutorButtonActionPerformed

    private void tuiWebpageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tuiWebpageButtonActionPerformed
        if (tutorWebClientURI != null) {
            for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {

                HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
                sendRemoteClientCommand("launchWebpage " + tutorWebClientURI, learnerStationHostInfo);
            }
        }
    }//GEN-LAST:event_tuiWebpageButtonActionPerformed

    private void monitorDomainSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitorDomainSessionButtonActionPerformed
        DomainSessionListEntry selected = domainSessionList.getSelectedValue();
        if (selected != null) {
            logger.info("Now monitoring domain session "+selected.domainSession.getDomainSessionId());
            MonitorModule.getInstance().monitorDomainSession(selected.domainSession.getDomainSessionId());
        }
    }//GEN-LAST:event_monitorDomainSessionButtonActionPerformed

    private void sensorListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sensorListPowerButtonMouseClicked
        String selected = sensorList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.SENSOR_MODULE);
        }
    }//GEN-LAST:event_sensorListPowerButtonMouseClicked

    private void gatewayListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gatewayListPowerButtonMouseClicked
        String selected = gatewayList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.GATEWAY_MODULE);
        }
    }//GEN-LAST:event_gatewayListPowerButtonMouseClicked

    private void domainListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_domainListPowerButtonMouseClicked
        String selected = domainList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.DOMAIN_MODULE);
        }
    }//GEN-LAST:event_domainListPowerButtonMouseClicked

    private void tutorListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tutorListPowerButtonMouseClicked
        String selected = tutorList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.TUTOR_MODULE);
        }
    }//GEN-LAST:event_tutorListPowerButtonMouseClicked

    private void umsPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_umsPowerButtonMouseClicked
        for (Object i : Arrays.asList(umsListModel.toArray())) {
            String iStr = (String) i;
            MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.UMS_MODULE);
        }
    }//GEN-LAST:event_umsPowerButtonMouseClicked

    private void lmsPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lmsPowerButtonMouseClicked
        for (Object i : Arrays.asList(lmsListModel.toArray())) {
            String iStr = (String) i;
            MonitorModule.getInstance().killModule(iStr, ModuleTypeEnum.LMS_MODULE);
        }
    }//GEN-LAST:event_lmsPowerButtonMouseClicked

    private void serverLaunchAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverLaunchAllButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_UMS, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_LMS, learnerStationHostInfo);
        }
    }//GEN-LAST:event_serverLaunchAllButtonActionPerformed

    private void learnerStationLaunchAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_learnerStationLaunchAllButtonActionPerformed
        for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
            HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
            sendRemoteClientCommand(LaunchConstants.LAUNCH_SENSOR, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_PED, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_DOMAIN, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_GATEWAY, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_LEARNER, learnerStationHostInfo);
            sendRemoteClientCommand(LaunchConstants.LAUNCH_TUTOR, learnerStationHostInfo);
        }
    }//GEN-LAST:event_learnerStationLaunchAllButtonActionPerformed

    private void launchAllModulesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchAllModulesButtonActionPerformed

        if (giftHostsList.getSelectedValuesList().size() == 1) {
            for (Object learnerStationSelected : giftHostsList.getSelectedValuesList()) {
                HostInfo learnerStationHostInfo = (HostInfo) learnerStationSelected;
                sendRemoteClientCommand(LaunchConstants.LAUNCH_UMS, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_LMS, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_PED, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_LEARNER, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_TUTOR, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_SENSOR, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_DOMAIN, learnerStationHostInfo);
                sendRemoteClientCommand(LaunchConstants.LAUNCH_GATEWAY, learnerStationHostInfo);
            }
        }
    }//GEN-LAST:event_launchAllModulesButtonActionPerformed

    private void pedListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pedListPowerButtonMouseClicked
        String selected = pedList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.PEDAGOGICAL_MODULE);
        }
    }//GEN-LAST:event_pedListPowerButtonMouseClicked

    private void learnerListPowerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_learnerListPowerButtonMouseClicked
        String selected = learnerList.getSelectedValue();
        if (selected != null) {
            MonitorModule.getInstance().killModule(selected, ModuleTypeEnum.LEARNER_MODULE);
        }
    }//GEN-LAST:event_learnerListPowerButtonMouseClicked

    private void giftHostsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_giftHostsListValueChanged

    }//GEN-LAST:event_giftHostsListValueChanged



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeMqAddressLabel;
    private javax.swing.JLabel activeUsersLabel;
    private javax.swing.JLabel activeUsersCntLabel;
    private javax.swing.JList<String> commandHistoryList;
    private javax.swing.JButton domainButton;
    private javax.swing.JLabel domainLabel;
    private javax.swing.JList<String> domainList;
    private javax.swing.JLabel domainListPowerButton;
    private javax.swing.JPanel domainPanel;
    private javax.swing.JPanel domainPanel2;
    private javax.swing.JPanel domainPanel3;
    private javax.swing.JPanel domainPanel4;
    private javax.swing.JPanel domainPanel5;
	private javax.swing.ImageIcon helpIcon;
	private javax.swing.JButton helpButton1;
	private javax.swing.JButton helpButton2;
    private javax.swing.JList<DomainSessionListEntry> domainSessionList;
    private javax.swing.JButton endDomainSession;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler19;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler21;
    private javax.swing.Box.Filler filler22;
    private javax.swing.Box.Filler filler23;
    private javax.swing.Box.Filler filler24;
    private javax.swing.Box.Filler filler25;
    private javax.swing.Box.Filler filler26;
    private javax.swing.Box.Filler filler27;
    private javax.swing.Box.Filler filler28;
    private javax.swing.Box.Filler filler29;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler30;
    private javax.swing.Box.Filler filler31;
    private javax.swing.Box.Filler filler32;
    private javax.swing.Box.Filler filler33;
    private javax.swing.Box.Filler filler34;
    private javax.swing.Box.Filler filler35;
    private javax.swing.Box.Filler filler36;
    private javax.swing.Box.Filler filler37;
    private javax.swing.Box.Filler filler38;
    private javax.swing.Box.Filler filler39;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler41;
    private javax.swing.Box.Filler filler42;
    private javax.swing.Box.Filler filler43;
    private javax.swing.Box.Filler filler44;
    private javax.swing.Box.Filler filler45;
    private javax.swing.Box.Filler filler46;
    private javax.swing.Box.Filler filler47;
    private javax.swing.Box.Filler filler48;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler52;
    private javax.swing.Box.Filler filler53;
    private javax.swing.Box.Filler filler54;
    private javax.swing.Box.Filler filler55;
    private javax.swing.Box.Filler filler56;
    private javax.swing.Box.Filler filler57;
    private javax.swing.Box.Filler filler59;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler65;
    private javax.swing.Box.Filler filler66;
    private javax.swing.Box.Filler filler67;
    private javax.swing.Box.Filler filler68;
    private javax.swing.Box.Filler filler69;
    private javax.swing.Box.Filler filler70;
    private javax.swing.Box.Filler filler71;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JButton gatewayButton;
    private javax.swing.JLabel gatewayLabel;
    private javax.swing.JList<String> gatewayList;
    private javax.swing.JLabel gatewayListPowerButton;
    private javax.swing.JPanel gatewayPanel;
    private javax.swing.JLabel giftFolderLocationLabel;
    private javax.swing.JList<HostInfo> giftHostsList;
    private javax.swing.JLabel giftVersionLabel;
    private javax.swing.JLabel activeDomainSessionsLabel;
    private javax.swing.JLabel activeDomainSessionsCntLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton killAllButton;
    private javax.swing.JButton launchAllModulesButton;
    private javax.swing.JButton learnerButton;
    private javax.swing.JLabel learnerLabel;
    private javax.swing.JList<String> learnerList;
    private javax.swing.JLabel learnerListPowerButton;
    private javax.swing.JPanel learnerPanel;
    private javax.swing.JButton learnerStationLaunchAllButton;
    private javax.swing.JPanel listPanel;
    private javax.swing.JButton lmsButton;
    private javax.swing.JLabel lmsPowerButton;
    private javax.swing.JButton monitorDomainSessionButton;
    private javax.swing.JLabel pedLabel;
    private javax.swing.JList<String> pedList;
    private javax.swing.JLabel pedListPowerButton;
    private javax.swing.JPanel pedPanel;
    private javax.swing.JButton pedagogicalButton;
    private javax.swing.JButton sensorButton;
    private javax.swing.JLabel sensorLabel;
    private javax.swing.JList<String> sensorList;
    private javax.swing.JLabel sensorListPowerButton;
    private javax.swing.JPanel sensorPanel;
    private javax.swing.JButton serverLaunchAllButton;
    private javax.swing.JPanel topModuleListPanel;
    private javax.swing.JPanel topModuleListPanel1;
    private javax.swing.JButton tuiWebpageButton;
    private javax.swing.JButton tutorButton;
    private javax.swing.JLabel tutorLabel;
    private javax.swing.JList<String> tutorList;
    private javax.swing.JLabel tutorListPowerButton;
    private javax.swing.JPanel tutorPanel;
    private javax.swing.JButton umsButton;
    private javax.swing.JLabel umsPowerButton;
    private javax.swing.JList<ActiveUserListEntry> userList;
    // End of variables declaration//GEN-END:variables

    @Override
    public void moduleStatusAdded(long sentTime, ModuleStatus status) {
        ModuleTypeEnum type = status.getModuleType();
        String queueName = status.getQueueName();
        setModuleOnline(type, queueName);
    }

    @Override
    public void moduleStatusChanged(long sentTime, ModuleStatus status) {
        ModuleTypeEnum type = status.getModuleType();
        String queueName = status.getQueueName();
        setModuleOnline(type, queueName);
    }

    @Override
    public void moduleStatusRemoved(StatusReceivedInfo status) {
        ModuleStatus moduleStatus = status.getModuleStatus();
        ModuleTypeEnum type = moduleStatus.getModuleType();
        String queueName = moduleStatus.getQueueName();
        setModuleOffline(type, queueName);
    }

    @Override
    public void userStatusAdded(UserSession userSession) {
        this.addUser(userSession);
    }

    @Override
    public void userStatusRemoved(UserSession userSession) {
        this.removeUser(userSession);
    }

    @Override
    public void domainSessionActive(DomainSession domainSession) {
        this.addDomainSession(domainSession);
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {
        this.removeDomainSession(domainSession);
    }

    @Override
    public void onRemoteClientOnline(HostInfo hostInfo) {
        giftHostsListModel.addElement(hostInfo);
    }

    @Override
    public void onRemoteClientOffline(HostInfo hostInfo) {
        giftHostsListModel.removeElement(hostInfo);
    }

    private void addCommandHistory(String commandHistoryEntry) {
        DefaultListModel<String> commandListModel = (DefaultListModel<String>) (commandHistoryList.getModel());
        commandListModel.add(0, commandHistoryEntry);
    }

    /**
     * Sends a command to be executed by a remote client
     *
     * @param command The command to send
     * @param hostInfo The info about the remote client
     */
    private void sendRemoteClientCommand(String command, HostInfo hostInfo) {
        try {
            String commandResult;
            if (command.contains("localhost")) {
                commandResult = RemoteClientManager.getInstance().sendCommand(command.replace("localhost", ipAddress), hostInfo);
            } else {
                commandResult = RemoteClientManager.getInstance().sendCommand(command, hostInfo);
            }
            addCommandHistory(commandResult);
        } catch (IOException ex) {
            logger.error("Failed to execute command on remote client " + hostInfo.toString(), ex);
        }
    }

    /**
     * Gets the UMS list model
     *
     * @return DefaultListModel - The UMS list model
     */
    public DefaultListModel<String> getUMSListModel(){
    	return umsListModel;
    }
    
    @Override
    public String toString(){
        return "[ControlJPanel]";
    }

    /**
     * This inner class contains the user information shown in the Active User's component
     * of this panel.
     *
     * @author mhoffman
     *
     */
    private static class ActiveUserListEntry {

        private UserSession userSession;

        public ActiveUserListEntry(UserSession userSession){
            this.userSession = userSession;
        }

        public int getUserId(){
            return userSession.getUserId();
        }

        public UserSession getUserSession() {
            return userSession;
        }

        @Override
        public boolean equals(Object otherItem){

            if (otherItem instanceof ActiveUserListEntry) {

                ActiveUserListEntry o = (ActiveUserListEntry) otherItem;
                UserSession thisSession = getUserSession();
                UserSession otherSession = o.getUserSession();
                if (CompareUtil.equalsNullSafe(thisSession, otherSession)) {
                    return true;
                }

            }

            return false;
        }

        @Override
        public int hashCode(){

        	// Start with prime number
        	int hash = 3;
        	int mult = 31;

        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + getUserSession().hashCode();

        	return hash;
        }

        @Override
        public String toString(){

            StringBuilder sb = new StringBuilder();


            if (userSession.getUsername() != null) {
                sb.append(userSession.getUsername()).append(" (").append(getUserId()).append(")");
            } else {
                sb.append("User ").append(userSession.getUserId());
            }

            sb.append(": Type (").append(userSession.getSessionType()).append(")");

            if (userSession.getExperimentId() != null) {
                sb.append(": Experiment ").append(userSession.getExperimentId());
            }

            if (userSession.getGlobalUserId() != null) {
                sb.append(": Global User Id ").append(userSession.getGlobalUserId());
            }
            return sb.toString();

        }

    }
}
