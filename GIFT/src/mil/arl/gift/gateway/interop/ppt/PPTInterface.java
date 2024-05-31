/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.ppt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.DispatchEvents;
import com.jacob.com.Variant;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleProperties;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;

/**
 * This is the powerpoint interop interface responsible for communicating with windows microsoft powerpoint application.
 * 
 * @author mhoffman
 *
 */
public class PPTInterface extends AbstractInteropInterface {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PPTInterface.class);
    
    /** Application.WindowState property constants (http://msdn.microsoft.com/en-us/library/ff744049.aspx)*/
    private static final String WINDOW_STATE = "WindowState";
    private static final int PP_WINDOW_MAXIMIZED = 1;
    private static final int PP_WINDOW_MINIMIZED = 2;
    private static final int PP_WINDOW_MAXIMIZED_2013 = 3; //Maximized value was changed in PowerPoint 2013
    
    /** Constants used to check the version of PowerPoint being used */
    private static final String VERSION = "Version";
    private static final String PP_VERSION_2013_PREFIX = "15."; //PowerPoint 2013 is denoted with a version number starting with 15
    
    /** microsoft powerpoint object model references */
    private static final String POWERPOINT_APP = "PowerPoint.Application";
    private static final String PRESENTATIONS = "Presentations";
    private static final String OPEN = "Open";
    private static final String QUIT = "Quit";
    private static final String ACTIVE_PRESENTATION = "ActivePresentation";
    private static final String SAVED = "Saved";
    private static final String SLIDESHOW_WINDOW = "SlideShowWindow";
    private static final String SLIDES = "Slides";
    private static final String VIEW = "View";
    private static final String COUNT = "Count";
    private static final String CURRENT_SHOW_POS = "CurrentShowPosition";
    private static final String VISIBLE = "Visible";
    private static final String SLIDESHOW_SETTINGS = "SlideShowSettings";
    private static final String RUN = "Run";
    
    private static final String TEMP_PPT_PREFIX = "GIFT-PPT-";
    
    /** determines how often a load content update message is sent in terms of percent complete delta since last update was sent */
    private static final double UPDATE_THRESHOLD = 0.05;
    
    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static{
        supportedMsgTypes = new ArrayList<MessageTypeEnum>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
    }
    
    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<TrainingApplicationEnum>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.POWERPOINT); 
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<MessageTypeEnum>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.START_RESUME);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.POWERPOINT_STATE);
    }
    
    /**
     * Load the Jacob dll
     */
    static {
        JacobLibraryStaticLoader.loadJacobNativeLibrary();
    }
    
    /** 
     * The powerpoint type library used to launch the powerpoint show
     * example: C:\\Program Files (x86)\\Microsoft Office\\Office14\\MSPPT.OLB
     */
    private String typeLibrary;    
            
    /** the powerpoint application */
    private ActiveXComponent pptApp;
    
    /** the ppt file being shown to learner */
    private File pptFile;
    
    /** thread responsible for launching and closing powerpoint */
    private PPTLaunchThread launchThread;
    
    /** thread responsible for waiting for the launch thread to complete */
    private PPTMainThread mainThread;
    
    /** manages sending powerpoint show loading progress to the user on the TUI */
    private LoadProgressHandler loadProgressHandler = null;
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param name - display name of this interface
     */
    public PPTInterface(String name){
        super(name, true); 
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {  

        try{
            typeLibrary = FindOfficeInstallation.selectPowerPoint(); 
        }catch(DetailedException e){
            throw e;
        }catch(Exception e){
            throw new ConfigurationException("There was a severe problem while trying to find or select PowerPoint on this computer.",
                    e.getMessage(),
                    e);
        }
        
        if(typeLibrary == null){
            
            if (!GatewayModuleProperties.getInstance().isRemoteMode()) {
                //just in case the logic in selectPowerPoint isn't sound this method will cause an exception
                //the reason server mode is fine with returning null is because the java web start gateway module will
                //display a dialog with a message that no powerpoint was found.
                throw new ConfigurationException("Unable to find a supported version of PowerPoint on this computer.",
                        "Would you like to continue by making the '"+getName()+"' interface unavailable to use?\n"+
                        "(this automatically updates '"+GatewayModuleProperties.getInstance().getInteropConfig()+"' and requires you to use the GIFT installer to enable it in the future)\n\n" +
                        "Otherwise install a supported version of the PowerPoint program and configure GIFT by using the GIFT installer.",
                        null);
            }else{
                logger.info("PowerPoint was not found on this computer.  A message should be displayed about this to the user in the JWS GW installer.");
            }
        }else{
            logger.info("Plugin has been configured");
        }      
        
        return false;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) {
        
        MessageTypeEnum mType = message.getMessageType();
        
        if(mType == MessageTypeEnum.SIMAN){
            
            Siman siman = (Siman)message.getPayload();
            
            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {  
			
				logger.info("Received load request");
                
                //check if main thread is still active, if so send ERROR
                if(mainThread != null && mainThread.isAlive()){
                    
                    GatewayModule.getInstance().sendReplyMessageToGIFT(
                            message,
                            new NACK(ErrorEnum.OPERATION_FAILED, "There is another powerpoint thread active, therefore can't start another powerpoint show"),
                            MessageTypeEnum.PROCESSED_NACK,
                            this);

                    return true;
                }
                
                //
                // get appropriate configuration info for loading
                //
                generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());                
                generated.course.PowerPointInteropInputs inputs = (generated.course.PowerPointInteropInputs) interopInputs.getInteropInput();
                
                //get the ppt show file name
                String filename = inputs.getLoadArgs().getShowFile();                
                if(filename == null){
                    
                    //ERROR: send NACK
                    GatewayModule.getInstance().sendReplyMessageToGIFT(
                            message,
                            new NACK(ErrorEnum.OPERATION_FAILED, "There was no powerpoint show file name provided in "+siman+"."),
                            MessageTypeEnum.PROCESSED_NACK,
                            this);

                    return true;
                }
				
				logger.info("Received load request with file name of "+filename);
				
				//Download PPT to the temp location
				//Note: the temp file will be deleted when finished with it
				try{
    	            File tempFile = File.createTempFile(TEMP_PPT_PREFIX, Long.toString(System.nanoTime()), FileUtil.getGIFTTempDirectory());
    	            
    	            //TODO: find a library that does this URL encoding for us
    	            String urlStr;
    	            if(siman.getRuntimeCourseFolderPath() != null){
    	                urlStr = getDomainContentServerAddress() + Constants.FORWARD_SLASH + UriUtil.makeURICompliant(siman.getRuntimeCourseFolderPath()) +
    	                        Constants.FORWARD_SLASH + UriUtil.makeURICompliant(filename);
    	            }else{
    	                urlStr = getDomainContentServerAddress() + Constants.FORWARD_SLASH + 
                                UriUtil.makeURICompliant(filename);
    	            }
    	            
    	            
    	            if(siman.getFileSize() == 0){
                        logger.debug("Using URL of '"+urlStr+"' to get the PowerPoint show. The file size was not provided, therefore copying with no progress indication.");

                        try (InputStream in = new URL(urlStr).openStream()) {
                            Files.copy(in, Paths.get(tempFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        }
    	            }else{
                        logger.debug("Using URL of '"+urlStr+"' to get the PowerPoint show. The file size was provided, therefore copying with progress indication.");                        

                        //
                        // create the file byte by byte
                        //
        	            try(FileOutputStream out = new FileOutputStream(tempFile)){
                            try (InputStream in = new URL(urlStr).openStream()) {
                                
                                byte[] buffer = new byte[1024];
                                int read = 0;
                                long counter = 0;
                                long fileSize = siman.getFileSize();
                                double currentPercent;
                                double lastUpdatePercent = 0;
                                
                                //to empty the progress bar shown to the user in the TUI (if shown)
                                sendLoadProgressUpdate(0);

                                while((read = in.read(buffer)) != -1){
                                    
                                    counter += read;
                                    
                                    currentPercent = (1.0 * counter / fileSize);
                                    if(currentPercent > (lastUpdatePercent + UPDATE_THRESHOLD)){
                                        //send update
                                        sendLoadProgressUpdate((long)(currentPercent*100));
                                        lastUpdatePercent = currentPercent;
                                        //logger.debug("counter = "+counter+", fileSize = "+fileSize+", currentPercent = "+currentPercent+".");
                                    }
                                    
                                    out.write(buffer, 0, read);
                                }
                                
                                //to fill the progress bar shown to the user in the TUI (if shown)
                                sendLoadProgressUpdate(100);
            	            }
        	            }
    	            }

                    pptFile = tempFile;
                    pptFile.deleteOnExit();  //in case this gateway module JVM goes down before any cleanup code can be called
                                             //In the future this may not be the best solution when the Gateway module instance
                                             //runs for an extended amount of time and downloads can accumulate.
                    
                    if(!pptFile.exists()){ 
                        
                        //ERROR: send NACK
                        GatewayModule.getInstance().sendReplyMessageToGIFT(
                                message,
                                new NACK(ErrorEnum.OPERATION_FAILED, "Unable to find the powerpoint type file named "+pptFile+" from show file argument of "+filename+"."),
                                MessageTypeEnum.PROCESSED_NACK,
                                this);
    
                        return true;
                    }
				}catch(Exception e){
				    logger.error("Caught exception while retrieving PPT file of "+filename, e);
				    
				    //ERROR: send NACK
                    GatewayModule.getInstance().sendReplyMessageToGIFT(
                            message,
                            new NACK(ErrorEnum.OPERATION_FAILED, "Unable to download the powerpoint file named "+filename+"."),
                            MessageTypeEnum.PROCESSED_NACK,
                            this);

                    return true;
				}
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.STOP){
                
				logger.info("Received stop request");
                cleanup();
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.START){
                
				logger.info("Received start request");
                loadScenario(pptFile.getAbsolutePath());
                                
            } else{
                logger.warn("This interop interface is receiving a Siman message type ("+siman+") that it can't handle");
            }
            
        }else{
            logger.warn("This interop interface is receiving a GIFT message, type = "+mType+" that it can't handle, something is wrong.");
        }
        
        return false;
    }
    
    /**
     * Send a load progress message to the domain module.  This happens on a thread in order to quickly
     * release the calling thread.
     * 
     * @param progress the loading task percent complete value (0 to 100)
     */
    private void sendLoadProgressUpdate(final long progress){
        
        if(loadProgressHandler == null){
            loadProgressHandler = new LoadProgressHandler(this);
        }
        
        if(!loadProgressHandler.isRunning()){
            loadProgressHandler.start();
        }
        
        loadProgressHandler.addLoadProgress(progress);
    }

    @Override
    public void cleanup() {

        //make sure the threads are finished
        if(mainThread != null && mainThread.isAlive()){
            logger.info("Received cleanup call, shutting down powerpoint thread");
            try{
                mainThread.shutdown();
            }catch(Exception e){
                logger.error("Caught exception while trying to shutdown the main thread.", e);
            }
            mainThread = null;
        }
        
        //Must wait for the launch thread to finish before returning from cleanup(), otherwise
        //a System.exit(0) will be called before the thread can finish and close powerpoint
        try {
        	if(launchThread != null && launchThread.isAlive()){
        		launchThread.join();
        	}
		} catch (InterruptedException e) {
			logger.error("Encountered an error while waiting for the launchThread to finish.", e);
			e.printStackTrace();
		}
    }
    
    /**
     * Handle the error message by sending an error message state from the Gateway module.
     * Note: this should not be used in direct response to an incoming GIFT message.  For those errors use Gateway 
     * module method for replying to GIFT.
     * 
     * @param message - the specific error message to send
     */
    private void handleError(String message){
        GatewayModule.getInstance().sendMessageToGIFT(new PowerPointState(message), MessageTypeEnum.POWERPOINT_STATE, this);
    }
    
    /**
     * Handling sending a message to GIFT module(s) from the Gateway module.
     * 
     * @param payload - the contents of the message to send
     * @param mType - the message type of the message to send
     */
    private void sendMessageToGIFT(TrainingAppState payload, MessageTypeEnum mType){
        GatewayModule.getInstance().sendMessageToGIFT(payload, mType, this);
    }
    
    /**
     * Send the power point state to GIFT
     */
    private void sendPowerPointState(){
        
        PowerPointState state = getCurrentPowerPointState();
        
        if(state != null){
            sendMessageToGIFT(state, MessageTypeEnum.POWERPOINT_STATE);
        }
    }
    
    /**
     * Return a new power point state object based on the current state of the power point show.
     * 
     * @return PowerPointState - new instance, can be null if a state could not be created
     */ 
    private PowerPointState getCurrentPowerPointState(){
        
        try{
            int slideNumber = getSlideNumber();
            int slideCount = getNumberOfSlides();
            
            return new PowerPointState(slideNumber, slideCount);
        }catch(Exception e){
            logger.error("Unable to send power point state because caught exception", e);
        }
        
        return null;
    }
    
    /**
     * Get the current slide number in the current show from PowerPoint using JACOB.
     * 
     * @return int - the current slide number
     */
    private int getSlideNumber(){
        
        Dispatch activePres = pptApp.getProperty(ACTIVE_PRESENTATION).toDispatch();
        Dispatch ssWindow = Dispatch.get(activePres, SLIDESHOW_WINDOW).toDispatch();
        Dispatch view = Dispatch.get(ssWindow, VIEW).toDispatch();
        Variant showPos = Dispatch.get(view, CURRENT_SHOW_POS);
        return showPos.getInt();
    }
    
    /**
     * Get the number of slides in the current show from PowerPoint using JACOB.
     * 
     * @return int - the number of slides
     */
    private int getNumberOfSlides(){
        
        Dispatch activePres = pptApp.getProperty(ACTIVE_PRESENTATION).toDispatch();
        Dispatch slides = Dispatch.get(activePres, SLIDES).toDispatch();
        Variant count = Dispatch.get(slides, COUNT);
        return count.getInt();
    }
    
    /**
     * Return whether or not the presentation has a write password or not.
     * Note: This is only available in Office 2010 and after.
     * 
     * Ref: http://msdn.microsoft.com/en-us/library/ff744704(v=office.14).aspx
     * 
     * @return boolean
     */
    @SuppressWarnings("unused")
    private boolean hasWritePassword(){
        
        try{
            Dispatch activePres = pptApp.getProperty(ACTIVE_PRESENTATION).toDispatch();
            Variant writePassword = Dispatch.get(activePres, "WritePassword");
            String password = writePassword != null ? writePassword.getString() : "";
//            System.out.println("Write password = "+password);
            
            return writePassword != null;
            
        }catch(Exception e){
            //nothing to do - exception will probably be thrown for earlier version of office
//            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Return the number of seconds that the current slide has been displayed.
     * 
     * @return long - number of seconds
     */
    @SuppressWarnings("unused")
    private long getSlideElapsedTime(){
        
        Dispatch activePres = pptApp.getProperty(ACTIVE_PRESENTATION).toDispatch();
        Dispatch ssWindow = Dispatch.get(activePres, SLIDESHOW_WINDOW).toDispatch();
        Dispatch view = Dispatch.get(ssWindow, VIEW).toDispatch();
        Variant elapasedTime = Dispatch.get(view, "SlideElapsedTime");
        return elapasedTime.getLong(); 
    }
    
    /**
     * Returns the number of seconds that have elapsed since the beginning of the slide show..
     * 
     * @return long - number of seconds
     */
    @SuppressWarnings("unused")
    private long getShowElapsedTime(){
        
        Dispatch activePres = pptApp.getProperty(ACTIVE_PRESENTATION).toDispatch();
        Dispatch ssWindow = Dispatch.get(activePres, SLIDESHOW_WINDOW).toDispatch();
        Dispatch view = Dispatch.get(ssWindow, VIEW).toDispatch();
        Variant elapasedTime = Dispatch.get(view, "PresentationElapsedTime");
        return elapasedTime.getLong(); 
    }
    
    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations(){
        return REQ_TRAINING_APPS;
    }
    
    @Override
    public List<MessageTypeEnum> getProducedMessageTypes(){
        return PRODUCED_MSG_TYPES;
    }
    

    @Override
    public Serializable getScenarios() {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() {
        return null;
    }
    
    @Override
    public void loadScenario(String scenarioIdentifier)
            throws DetailedException {
        
        File file = new File(scenarioIdentifier);
        if(file.exists()){
            
            pptFile = file;
            
            mainThread = new PPTMainThread();
            mainThread.start();
            
        }else{
            //ERROR
            throw new DetailedException("Failed to load the PowerPoint show '"+scenarioIdentifier+"'.", 
                    "The show isn't a file that exists on this computer.", null);
        }
        

    }
    
    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }
    
    @Override
    public void selectObject(Serializable objectIdentifier)
            throws DetailedException {
        
    }
    
    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PPTInterface: ");
        sb.append(super.toString());
        
        sb.append(", messageTypes = {");
        for(MessageTypeEnum mType : supportedMsgTypes){
            sb.append(mType).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * This thread handles waiting for the launch thread to complete and then notifying GIFT that 
     * the powerpoint show is finished.
     * 
     * @author mhoffman
     *
     */
    private class PPTMainThread extends Thread{
        
        /**
         * Class constructor - set the thread name
         */
        public PPTMainThread(){
            super("PPTMainThread");
        }
        
        /**
         * Responsible for starting the launch thread, waiting for it to finish, then 
         * sending a message to GIFT modules when powerpoint is closed.
         */
        @Override
        public void run(){
            
            logger.info("Starting PPT main thread");
            
            ComThread.InitMTA();
            launchThread = new PPTLaunchThread();
            launchThread.start();

            try {
                //TODO: move this to the "SlideShowBegin" powerpoint event
                StartResume sr= new StartResume(new Date().getTime(), 0, 0);
                sendMessageToGIFT(sr, MessageTypeEnum.START_RESUME);
                
                logger.info("Entering wait until Powerpoint show has finished");
                
                launchThread.join();
                
                logger.info("The launch thread has been released, time to notify GIFT by sending the Stop message");
                
                //send successful completion of powerpoint type message
                StopFreeze sf = new StopFreeze(new Date().getTime(), 0, 0, 1);
                sendMessageToGIFT(sf, MessageTypeEnum.STOP_FREEZE);
                
            } catch (InterruptedException e) {

                logger.error("An exception was caught while waiting for the PPT launch thread to complete", e);
                e.printStackTrace();
                  
                //send error state 
                handleError("There was an issue while waiting for PowerPoint to finish");
            }finally{
                ComThread.Release();
            }
            
            logger.info("PPT main thread is finished");
        }
        
        /**
         * Force a shutdown of the power point main thread
         */
        public void shutdown(){
            
            if(launchThread != null){
                launchThread.shutdown();
            }
        }
    }
    
    /**
     * This thread handles launching and closing the powerpoint application.
     * 
     * @author mhoffman
     *
     */
    private class PPTLaunchThread extends Thread{
        
        /** powerpoint event listener */
        private PPTEvents events;
        
        /** 
         * whether the SlideShowEnd method should be called in PPTEvents (as opposed to powerpoint calling it)
         * in order to prematurely end the show (e.g. end the show if the course is being terminated prematurely) 
         */
        private boolean needsTermination = false;
        
        /** 
         * whether the powerpoint.exe task should be task killed in order to close the application
         * forcefully.  This is only needed if gift started powerpoint and the show is still running
         * when the gift course is exiting
          */
        private boolean needsTaskKill = false;
        
        /**
         * Class constructor - set the thread name
         */
        public PPTLaunchThread(){
            super("PPTLaunchThread");
        }
        
        /**
         * Responsible for launching powerpoint, waiting for it to finish, then closing any remaining
         * powerpoint windows.
         */
        @Override
        @SuppressWarnings("unused")
        public void run(){
            
            try{
                ComThread.InitMTA();
                
                //check if powerpoint is already opened, so we don't close the user's personal powerpoint file later
                boolean isopened = false;
                ActiveXComponent existingPPT = ActiveXComponent.connectToActiveInstance(POWERPOINT_APP);
                if(existingPPT != null){
                    logger.warn("PowerPoint is currently opened, therefore the Gateway module will not attempt to close it upon completion of the PowerPoint presentation.");
                    isopened = true;
                }
                
                pptApp = new ActiveXComponent(POWERPOINT_APP);
                
                boolean isPowerPoint2013 = false; 
                
                try{
                	isPowerPoint2013 = pptApp.getProperty(VERSION).getString().startsWith(PP_VERSION_2013_PREFIX);
                	
                } catch(Exception e){
                	logger.warn("Caught an exception while checking whether or not PowerPoint 2013 is being used.");
                }
                
                //minimize, then maximize the PowerPoint application window to force the slideshow to be in the foreground
                //when it is started.  The issue is when PowerPoint is launched in this class on a laptop, the show tends to be
                //in the background of other GIFT windows.
	            pptApp.setProperty(WINDOW_STATE, new Variant(PP_WINDOW_MINIMIZED));
	            pptApp.setProperty(WINDOW_STATE, new Variant(!isPowerPoint2013 ? PP_WINDOW_MAXIMIZED : PP_WINDOW_MAXIMIZED_2013));
	                                
                //NOTE: this is required for PPT 2007 (not 2010), w/o it the OPEN method call will throw an exception
                pptApp.setProperty(VISIBLE, new Variant(true));
                
                Dispatch presentations = pptApp.getProperty(PRESENTATIONS).toDispatch();
    
                //register for application event notification
                events = new PPTEvents();
                DispatchEvents de = new DispatchEvents(pptApp, events, POWERPOINT_APP, typeLibrary);
              
                //open/launch powerpoint file in read only mode (meaning any changes made won't cause a prompt asking to save upon closing)
                Variant readonly = new Variant(true);
                Dispatch presentation = Dispatch.call(presentations, OPEN, pptFile.getAbsolutePath(), readonly).
                                     toDispatch();
                                	
                //in PowerPoint 2013/2010/2007, the slide show needs to be started explicitly
                Dispatch slideShowSettings = Dispatch.get(presentation, SLIDESHOW_SETTINGS).getDispatch();
                Dispatch.call(slideShowSettings, RUN);
                
                pptApp.setProperty(WINDOW_STATE, new Variant(PP_WINDOW_MINIMIZED));
                
                //before waiting until the slideshow ends gracefully, check the terminate flag
                events.needsTermination = this.needsTermination;
    
                //wait for powerpoint show to finish before continuing
                try {
                    synchronized(this){
                        logger.info("Waiting for PowerPoint show to end.");
                        wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
    
                //must sleep to allow for cleanup somewhere, not really sure.  Without this sleep, closing powerpoint will cause "Invalid Request" exception
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for powerpoint to cleanup", e);
                    e.printStackTrace();
                }
                
                if(isopened){
                    //Not closing powerpoint application because powerpoint was opened before this plugin started, therefore
                    //we don't want to close the user's personal powerpoint file.
                    pptApp.setProperty(WINDOW_STATE, new Variant(PP_WINDOW_MINIMIZED));
                }else{
                    
                    //Close any lingering powerpoint application
                    //Note: the close logic was added because for some reason the powerpoint window is opened -and remains open- when using JACOB
                    //      to launch a powerpoint show file.  This doesn't happen when using windows to launch a powerpoint show, therefore we have to 
                    //      close the remaining powerpoint window after the show has completed.
                    try{
                        closePowerpoint(pptApp);

                    }catch(ComFailException cfe){
                        logger.error("Caught exception while trying to close powerpoint, this may result in the powerpoint application remaining open",cfe);
                    }finally{
                        
                        if(needsTaskKill){
                            // when the slide show didn't end on its own (e.g. the last slide was reached) the above
                            // closePowerpoint method call won't close POWERPOINT.EXE, only closes the slide show throw JACOB,
                            // This taskkill will make sure the EXE closes as well. This should only be done if powerpoint
                            // was NOT running prior to GIFT starting the powerpoint show AND GIFT is forcefully closing
                            // powerpoint because the course is ending prematurely.
                            try{
                                logger.info("Executing taskkill command on POWERPNT.EXE");
                                Runtime.getRuntime().exec("taskkill /F /IM POWERPNT.EXE");
                            }catch(Exception e){
                                logger.error("Caught exception while trying to call taskkill command on POWERPNT.EXE", e);
                            }
                        }
                    }

                }
                
            }catch(Throwable t){
                //There maybe more to do than just log the exception, maybe PowerPoint is open and should be closed or
                //a launchThread.notifyAll(), ...
                logger.error("Caught exception while running PowerPoint launch thread on Powerpoint file of "+pptFile.getAbsolutePath(), t);
                System.out.println("The PowerPoint launch thread threw an exception when handling "+pptFile.getAbsolutePath()+", not sure what the state of the Gateway module will be (or GIFT for that matter)");
                t.printStackTrace();
            }finally{
                ComThread.Release();
                ComThread.quitMainSTA();
            }
            
            logger.info("PowerPoint launch thread has ended.");
        }
        
        /**
         * Close the PowerPoint application
         * 
         * @param ppt -  Microsoft COM API instance for PowerPoint
         * @throws ComFailException - this can be thrown when invoking the "Quit" command
         */
        public void closePowerpoint(ActiveXComponent ppt) throws ComFailException{
            
            logger.info("Commanding Powerpoint application to close");
            
            //to quit ppt        
            ppt.invoke(QUIT, new Variant[] {});
        }
        
        /**
         * Force a shutdown of the power point launch thread
         */
        public void shutdown(){
            
            // need to force kill the application since this is not a graceful end
            // to the slide show (graceful is when the slide show ends because the end was reached)
            needsTaskKill = true;
            
            if(events != null){
                //artificially send the end show event
                logger.info("Artificially causing a slide show end event to cause PowerPoint show to close");
                events.SlideShowEnd(null);
            }else{
                logger.info("Unable to manually end the PowerPoint show because one is not currently running.  However if one is attempting to start, it will be closed shortly.");
                needsTermination = true;
            }
        }
    }
    
    /**
     * Class that is notified of powerpoint application events.</br>
     * Note: All event methods currently must have the same signature: one argument which is a Java array of Variants, 
     * and a void return type.
        
     * 
     * @author mhoffman
     *
     */
    public class PPTEvents{
        
        /** 
         * whether the SlideShowEnd method should be called by code in this class (as opposed to powerpoint calling it)
         * in order to prematurely end the show (e.g. end the show if the course is being terminated prematurely) 
         */
        boolean needsTermination = false;
        
        /**
         * Occurs after a slide show ends, immediately after the last SlideShowNextSlide event occurs
         * Note: The SlideShowEnd event always occurs before a slide show ends if the SlideShowBegin event has occurred
         * Ref: http://msdn.microsoft.com/en-us/library/ff746536.aspx
         * 
         * @param args
         *            the COM Variant objects that this event passes in.
         */
        public void SlideShowEnd(Variant[] args) {
          
            logger.info("Received notification of Slide Show end event");
            
            //prevent saving any changes made to the presentation (i.e. show) during the lesson 
            //(e.g. the Hemorrhage Control PPT shows questions with radio buttons for the user to select.  Selecting a 
            //      response changes the underlying file.  This change causes a "do you want to save" prompt when closing the 
            //      PowerPoint application, something we don't want to happen when using GIFT to present the show)
            try{
                ActiveXComponent activePres = pptApp.getPropertyAsComponent(ACTIVE_PRESENTATION);
                activePres.setProperty(SAVED, new Variant(true));
            }catch(Exception e){
                needsTermination = true;
                logger.warn("There was a problem trying to prevent PowerPoint from asking if you want to save changes to the presentation.  "+
                        "Therefore, if there were changes detected by PowerPoint application, the application might not close automatically when GIFT attempts to do so", e);
            }
            
            logger.info("Notifying launch thread to continue its post-start slide show logic.");
            synchronized(launchThread){
                launchThread.notifyAll();
            }
        }
        
        /**
         * Occurs when you start a slide show
         * Ref: http://msdn.microsoft.com/en-us/library/ff746741.aspx
         * 
         * @param args
         *            the COM Variant objects that this event passes in.
         */
        public void SlideShowBegin(Variant[] args) {
            logger.info("Received notification of Slide Show begin event");
            
            if(needsTermination){
                logger.info("Manually ending the slide show because the launch thread has indicated it needs to be closed.");
                this.SlideShowEnd(null);
            }
        }
        
        /**
         * Occurs immediately before the transition to the next slide. For the first slide, occurs immediately after the SlideShowBegin event.
         * Ref: http://msdn.microsoft.com/en-us/library/ff745863.aspx
         * 
         * @param args
         *            the COM Variant objects that this event passes in.
         */
        public void SlideShowNextSlide (Variant[] args) {
            
            if(needsTermination){
                logger.info("Manually ending the slide show because the launch thread has indicated it needs to be closed.");
                this.SlideShowEnd(null);
            }
            
            sendPowerPointState();          
        }
        
        /**
         * Occurs when the user clicks Next to move within the current slide.
         * The SlideShowOnNext event does not fire when users click Next to move to the next slide, 
         * but rather only when they click Next to move within a given slide, for example to run the next animation on the slide.
         * Ref: http://msdn.microsoft.com/en-us/library/ff746469.aspx
         * 
         * @param args
         *            the COM Variant objects that this event passes in.
         */
        public void SlideShowOnNext(Variant[] args) {          
          
        }
        
        /**
         * Occurs when the user clicks Previous to move within the current slide.
         * The SlideShowOnPrevious event does not fire when users click Previous to move from one slide to the previous one, 
         * but rather only when they click Previous to move within a given slide, for example to rerun the previous animation on the slide.
         * Ref: http://msdn.microsoft.com/en-us/library/ff744749.aspx
         * 
         * @param args
         *            the COM Variant objects that this event passes in.
         */
        public void SlideShowOnPrevious(Variant[] args) {          
          
        }
        
    }

}
