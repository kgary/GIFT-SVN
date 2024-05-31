/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.LoadProgress;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.dis.DISToGIFTConverter.DISDialect;

/**
 * This is the base class for interop interface classes.  An interoperability interface handles communication between
 * a training application (e.g. VBS) or a messaging protocol (e.g. DIS, HLA) and GIFT.  It can support a set of GIFT messages
 * that are sent to the domain module (e.g. SIMAN load message) by communicating the necessary information to an external system.
 * Implementations can also support translating external communications (e.g. DIS entity state) into a GIFT message (e.g. common.sim.EntityState) for consumption
 * by GIFT modules.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractInteropInterface {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractInteropInterface.class);

    /** display name for the implemented interface instance */
    private String displayName;

    /** whether this interop interface is enable, i.e. should it handle incoming GIFT messages and incoming external messages */
    private boolean enabled = false;

    /** whether this interop interface is available for use by this GW module, i.e. an interface might be unavailable if the training application is not installed */
    private boolean available = true;

    /**
     * the usernames of the observer controllers that have connected to this interface in order to use a training application
     * as an external monitor using the Game Master interface
     */
    private Set<String> observerControllers;

    /**
     * whether the interface is required to be on the same machine as the user's tutor interface.
     * This can be due to the requirement of manipulating the Training Application's window(s).
     */
    private boolean requiresUsersDisplay;

    /**
     * the domain content server (currently jetty instance) address as specified by the
     * host and port domain module properties.  Note this address will not end with the forward slash needed
     * to reference a file path on that server.  This is important for some interop plugins in case they need
     * to download course content to use in the training app instance (e.g. powerpoint show)
     *
     * (e.g. http://10.1.21.123:8885)
     */
    private String domainContentServerAddress;

    /**
     * whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out.
     */
    private boolean isPlayback;

    /**
     * Can be used to indicate the type of interaction happening between GIFT
     * and the external application.  For example, when interacting with ARES
     * in learner mode, the learner shouldn't be able to have access to certain
     * scenario authoring features like moving objects placed in the scenario.
     *
     * @author mhoffman
     *
     */
    public enum InteractionMode{
        Learner,
        Author
    }

    /** the mode of interaction between GIFT and the external application this interop communicates with */
    protected InteractionMode mode = InteractionMode.Learner;

    /**
     * Class constructor
     *
     * @param displayName The display name of this implementation. Can't be
     *        null.
     * @param requiresUsersDisplay Whether the interface is required to be on
     *        the same machine as the user's tutor interface.
     */
    public AbstractInteropInterface(String displayName, boolean requiresUsersDisplay){

        if(displayName == null){
            throw new IllegalArgumentException("The display name can't be null.");
        }

        this.displayName = displayName;

        this.requiresUsersDisplay = requiresUsersDisplay;
    }

    /**
     * Set the mode of interaction between GIFT and the external application this interop communicates with
     *
     * @param mode the enumerated mode.  If null, the default of {@link InteractionMode#Learner} is used.
     */
    public void setInteractionMode(InteractionMode mode){

        if(mode == null){
            this.mode = InteractionMode.Learner;
        }else{
            this.mode = mode;
        }
    }

    /**
     * Return the name of this interop interface.
     *
     * @return String
     */
    public String getName(){
        return displayName;
    }

    /**
     * Set the domain content server address for this interop plugin implementation to use.
     * The domain content server (currently jetty instance) address as specified by the
     * host and port domain module properties.  Note this address will not end with the forward slash needed
     * to reference a file path on that server.  This is important for some interop plugins in case they need
     * to download course content to use in the training app instance (e.g. powerpoint show)
     *
     * @param domainContentServerAddr Can't be null or empty.
     */
    public void setDomainContentServerAddress(String domainContentServerAddr){

        if(domainContentServerAddr == null || domainContentServerAddr.isEmpty()){
            throw new IllegalArgumentException("The domain content server address can't be null or empty.");
        }

        this.domainContentServerAddress = domainContentServerAddr;
    }

    /**
     * Return the domain content server address for this interop plugin implementation to use.
     *
     * @return String Won't be null or empty.  (e.g. http://10.1.21.123:8885)
     */
    public String getDomainContentServerAddress(){
        return domainContentServerAddress;
    }

    /**
     * Return whether the interface is required to be on the same machine as the user's tutor interface.
     *
     * @return boolean
     */
    public boolean requiresUsersDisplay(){
        return requiresUsersDisplay;
    }

    /**
     * Return whether the interop interface is enabled or not.
     * i.e. should it handle incoming GIFT messages and incoming external messages
     *
     * @return boolean
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * Set whether the interop interface is enabled or not and whether playback is happening or not.
     *
     * @param value - enabled or not
     * @param isPlayback whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out.
     * @throws ConfigurationException if there was a problem
     */
    public void setEnabled(boolean value, boolean isPlayback) throws ConfigurationException{

        // only use playback parameter if the interop is being enabled
        // NOTE: call this first in case a interop plugin overrides setEnabled.
        setPlayback(value ? isPlayback : false);

        setEnabled(value);
    }

    /**
     * Set whether the interop interface is enabled or not.
     * i.e. should it handle incoming GIFT messages and incoming external messages
     *
     * @param value - enabled or not
     * @throws ConfigurationException if there was a problem
     */
    public void setEnabled(boolean value) throws ConfigurationException{
        enabled = value;

        if(!value){

            if(hasObserverControllers()) {
                observerControllers = null; //reset the connected observer controllers when this interface is disabled
            }

            setPlayback(false); // reset the playback mode when this interface is disabled
        }
    }

    /**
     * Return whether the interop interface is available or not.
     * i.e. an interface might be unavailable if the training application is not installed
     * or not running (e.g. VBS needs to be started with the GIFT shortcut and be at the main menu)
     *
     * @param testInterop whether or not to test the interop to determine if it is truly "available" to
     * be used in the execution of a course.  Some interops don't need to be tested at this point.
     * @return boolean
     */
    public boolean isAvailable(boolean testInterop){
        return available;
    }

    /**
     * Set whether the interop interface is available or not.
     * i.e. an interface might be unavailable if the training application is not installed
     *
     * @param value - available or not
     *          Note: if the interface was set to not available, you can't set it to available later on.
     */
    public void setAvailable(boolean value){

        if(!available && value){
            throw new IllegalArgumentException("The interface "+this+" has been previously set to not available, "+
                    "therefore you can't set it to available due to possible configuration issues during runtime.");
        }

        available = value;
    }

    /**
     * Return the course provided load arguments for a specific interop plugin implementation class.
     *
     * @param interopImpl the name of an interop interface class to use as the key to lookup any load arguments
     * @param loadArgs all of the load arguments for all necessary interops for the current course transition provided via a GIFT SIMAN message.
     * @return generated.course.InteropInputs the load arguments found based on the interop interface class name.  Can be null.
     */
    protected static generated.course.InteropInputs getLoadArgsByInteropImpl(String interopImpl, Map<String, Serializable> loadArgs){

        //need to search for this class implementation's inputs by class name
        for(String implClass : loadArgs.keySet()){

            if(interopImpl.contains(implClass)){
                Serializable toRet = loadArgs.get(implClass);
                if(toRet instanceof generated.course.InteropInputs) {
                	return (generated.course.InteropInputs) toRet;
                } else {
                	return null;
                }
            }
        }

        return null;
    }

    /**
     * Configure the interop interface using the configuration
     *
     * @param config - configuration parameters to use
     * @return boolean whether or not the config values were updated by the plugin interface during configuration.
     *              Note: this can be used to write the changes to disk for future use.
     * @throws ConfigurationException if there was a problem during configuring the interface
     */
    public abstract boolean configure(Serializable config) throws ConfigurationException;

    /**
     * Return the list of supported GIFT messages (e.g. SIMAN) that this interop plugin interface
     * can consume from GIFT modules (e.g. Domain module) and then send out over the network to the appropriate training application (e.g. VBS).
     *
     * @return can be empty but not null.  An empty list means that this interop plugin interface provides
     * no control mechanism to GIFT.  GIFT would like to be able to start, load, pause, stop (SIMAN related) at
     * a minimum so that the learner's experience is synchronized with the GIFT tutor.
     */
    public abstract List<MessageTypeEnum> getSupportedMessageTypes();

    /**
     * Return the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from
     * an external training applications (e.g. VBS).
     *
     * @return should not be empty or null because that would indicate this interop plugin interface produces
     * no useful information to GIFT about what the learner is doing in an external training application.  Without
     * this information it is much harder for GIFT to assess the learner's action and provide useful pedagogy.
     */
    public abstract List<MessageTypeEnum> getProducedMessageTypes();

    /**
     * Handle the message being sent by a GIFT module.  For some interop implementations this means
     * sending the message contents over the interop interface's network connection.  Depending on the implementation, the contents
     * of the GIFT message might have to be translated into another format/object before going out externally to GIFT.
     *
     * @param message the GIFT message to translate and communicate via this interop interface
     * @param errorMsg error message produced by the plugin handling this gift message
     * @return boolean whether the incoming message was responded to, via a new GIFT message to a GIFT module, by this interop implementation.
     *              This is useful for determining if the gateway module needs to send a generic reply on behalf of the interop implementation class.
     * @throws ConfigurationException if there was a configuration issue with the interop
     */
    public abstract boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException;

    /**
     * Return the collection of Training Application types needing to be configured to use
     * this interop interface implementation.
     * For example the vbsplugin.VBSPluginInterface.java requires the TrainingApplicationEnum.VBS be configured
     * prior to using that interop plugin.
     *
     * @return List<TrainingApplicationEnum> Can be empty but not null
     */
    public abstract List<TrainingApplicationEnum> getReqTrainingAppConfigurations();

    /**
     * Return information about the scenarios available in the external application that this
     * interop interface class is communicating with.
     *
     * This is useful for authoring tools that want to interact with the external application's scenario editor and
     * allow the GIFT course author to associate a scenario with a GIFT course object.
     *
     * @return contains metadata about the scenarios in the application (e.g. scenario names and descriptions)
     * A return value of null indicates that the interop plugin class doesn't support this method.
     * Note: each interop implementation class should identify the format of the returned object.
     * @throws DetailedException if there was a problem retrieving information about the available scenarios
     */
    public abstract Serializable getScenarios() throws DetailedException;

    /**
     * Return information about the currently loaded scenario in the external application.
     *
     * This is useful for determining whether the scenario is loaded as well as retrieve information
     * about the current external scenario of which could be shown to the user through a GIFT user interface.
     *
     * @return contains metadata about the currently loaded scenario.  A return value of null indicates that the
     * interop plugin class doesn't support this method or a scenario is not loaded.
     * Note: each interop implementation class should identify the format of the returned object.
     * @throws DetailedException if there was a problem retrieving the information about the current scenario.
     */
    public abstract Serializable getCurrentScenarioMetadata() throws DetailedException;

    /**
     * Return information about the selectable objects for the current scenario in the external application that this
     * interop interface class is communicating with.
     *
     * This is useful for authoring tools that want to interact with those objects and allow the GIFT course
     * author to associate GIFT assessments with those objects.
     *
     * @return contains metadata about the objects that are selectable in the application (e.g. object name, location, type)
     * A return value of null indicates that the interop plugin class doesn't support this method.
     * Note: each interop implementation class should identify the format of the returned object.
     * @throws DetailedException if there was a problem retrieving information about the selectable objects
     */
    public abstract Serializable getSelectableObjects() throws DetailedException;

    /**
     * Notify the external application to select the object in that application identified by the
     * provided identifier.  The external application may highlight that object in its user interface.
     *
     * This is useful for when an author or learner is selecting an object in GIFT that is associated with
     * an object in the external application.
     *
     * @param objectIdentifier a unique identifier that will help the external application know which object
     * is being selected.
     * @throws DetailedException if there was a problem communicating the selection event
     */
    public abstract void selectObject(Serializable objectIdentifier) throws DetailedException;

    /**
     * Uses the interop plugin implementation to load a scenario in the external application that it is
     * communicating with.
     *
     * @param scenarioIdentifier uniquely identifies the scenario in the external application.  This is normally
     * authored in the GIFT course.  A null value indicates that any loaded scenario should be cleared from view which
     * might mean that the external application should return to the main menu.
     * @throws DetailedException if there was a problem loading the scenario.
     */
    public abstract void loadScenario(String scenarioIdentifier) throws DetailedException;

    /**
     * Exports the current external training application scenario into a single file that can
     * be saved in the GIFT course folder.  This file can then be used by this interop plugin implementation
     * to load the scenario in the external training application during GIFT course execution.
     *
     * @param exportFolder the folder to export the scenario file(s) to.  The folder must exist.
     * @return the file that contains the scenario.  This file might be a zip that contains many files/folders.  A null
     * value could indicate that the external application has no file representation of the scenario that can be accessed
     * or that the interop plugin class doesn't support this method.
     * @throws DetailedException if there was a problem exporting the scenario from the external training application.
     */
    public abstract File exportScenario(File exportFolder) throws DetailedException;

    /**
     * The connection is being closed, time to clean up
     */
    public abstract void cleanup();

    /**
     * Gets whether this interface has any observer controllers that have connected to it
     * via the Game Master interface
     *
     * @return whether any observer controllers have connected to this interface
     */
    public boolean hasObserverControllers() {
        return observerControllers != null && !observerControllers.isEmpty();
    }

    /**
     * Gets whether the user with the given user name has permission to control this interface. If
     * this interface has no associated observer controllers and the given user is not an observer controller,
     * then this method will return true. Alternatively, if the given user is one of the observer controllers
     * associated with this interface, then this method will also return true.
     *
     * @param userName the user name of the observer controller requesting permission. Can be null, if the request
     * is being made by a non-observer-controller user.
     * @return whether the user has permission to control this interface.
     */
    public boolean hasObserverControllerPermission(String userName) {

        if(observerControllers == null || observerControllers.isEmpty()) {
            if(userName == null) {
                return true; //give non-observer-controllers permission to interfaces that are not being used by observer controllers
            }

        } else if(observerControllers.contains(userName)) {
            return true; //give observer controllers permission only to interfaces that they are using
        }

        return false;
    }

    /**
     * Adds the Game Master observer controller with the given username as a connector to this interface. If this interface
     * has any connected observer controllers, then it will not be disabled when new interops are initialized.
     *
     * @param observerName the username of the observer controller to add. If null, this method is a no-op.
     */
    public void addObserverController(String observerName) {

        if(observerName == null) {
            return;
        }

        if(observerControllers == null) {
            observerControllers = new HashSet<>();
        }

        observerControllers.add(observerName);
    }

    /**
     * Gets the DIS dialect that should be used to communicate over DIS when this interop is enabled. This can be used
     * to handle training-application-specific extensions to the DIS standard.
     *
     * @return the DIS dialect that should be used when communicating over DIS. Can be null if the default
     * dialect should be used.
     */
    public DISDialect getDisDialect() {
        return null;
    }

    /**
     * Return whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out.
     * @return the current playback flag value
     */
    public boolean isPlayback() {
        return isPlayback;
    }

    /**
     * Set whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out.
     * @param isPlayback true if currently performing playback
     */
    public void setPlayback(boolean isPlayback) {
        this.isPlayback = isPlayback;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("enabled = ").append(isEnabled());
        sb.append(", name = ").append(getName());
        sb.append(", domainContentServerAddress = ").append(getDomainContentServerAddress());
        sb.append(", observerControllers = ").append(observerControllers);

        return sb.toString();
    }

    /**
     * Manages sending content loading progress messages in a thread.
     *
     * @author mhoffman
     *
     */
    public static class LoadProgressHandler{

        /** the thread checking the queue and sending messages */
        private Thread thread;

        /** queue of load progress update values (percents 0 to 100)*/
        private Queue<Long> progressQueue = new ConcurrentLinkedDeque<>();

        private boolean alive = false;

        /** the interop instance producing the load progress updates */
        private AbstractInteropInterface interopInstance;

        public LoadProgressHandler(AbstractInteropInterface interopInstance){

            if(interopInstance == null){
                throw new IllegalArgumentException("The interop instance can't be null.");
            }

            this.interopInstance = interopInstance;
        }

        /**
         * Return whether the sending thread is still running
         *
         * @return true iff the thread has not be ordered to gracefully end and the thread is still alive
         */
        public boolean isRunning(){
            return alive && thread.isAlive();
        }

        /**
         * Start the thread that polls the queue for load progress values and send
         * the load progress GIFT message.  This will do nothing if the thread is already running.
         */
        public void start(){

            if(!alive && (thread == null || !thread.isAlive())){

                //reset previous executions of this class and its thread
                progressQueue.clear();

                thread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        while(alive){

                            Long value = progressQueue.poll();
                            if(value == null){

                                //wait before checking the queue again
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    logger.error("caught exception while sleeping", e);
                                }

                                continue;
                            }else{
                                //send the progress update

                                LoadProgress loadProgress = new LoadProgress();
                                loadProgress.setValueById(LoadProgress.TASK_KEY, "Loading PowerPoint show");
                                loadProgress.setValueById(LoadProgress.TASK_PROGRESS_KEY, value);

                                GatewayModule.getInstance().sendMessageToGIFT(loadProgress, MessageTypeEnum.LOAD_PROGRESS, interopInstance);
                            }

                            //automatically end the thread once 100 is reached
                            if(value >= 100){
                                alive = false;
                            }
                        }

                    }
                }, "load progress update");

                alive = true;
                thread.start();
            }
        }

        /**
         * Gracefully end the thread on the next loop
         */
        public void stop(){
            alive = false;
        }

        /**
         * Add a progress update value to the queue.
         *
         * @param progress a percent value (0 to 100) that indicates the load progress of training application content.
         */
        public void addLoadProgress(final long progress){
            progressQueue.add(progress);
        }
    }

    /**
     * Used to configure interop interfaces that require a special dialect for communicating over DIS
     *
     * @author nroberts
     */
    public interface HandlesDisDialect{

        /**
         * Sets the dialect to use when translating between GIFT game states and the DIS standard. DIS dialects
         * can be used to allow GIFT to handle DIS PDUs that are modified for specific training applications.
         *
         * @param dialect the dialect to use. If null, the default dialect will be used.
         */
        public void setDisDialect(DISDialect dialect);
    }
}
