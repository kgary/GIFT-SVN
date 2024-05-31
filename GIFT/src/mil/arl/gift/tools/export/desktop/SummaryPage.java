/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.ContentProxyInterface;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.ums.db.survey.Surveys;

import org.apache.commons.io.FileUtils;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the user a summary of what settings were selected.
 * 
 * @author cdettmering
 */
public class SummaryPage extends WizardPage {
    
    private static Logger logger = LoggerFactory.getLogger(SummaryPage.class);
	
	/** Generated serial */
	private static final long serialVersionUID = -8227208053911011410L;
	
	private static final String TITLE = "Summary";
	private static final String DESCRIPTION = "Summary";
	
	private static final File ROOT = new File(".");
	
	private static final String ENOUGH_SPACE_FONT_COLOR = "green";
	private static final String INSUFFICIENT_SPACE_FONT_COLOR = "red";
	private static final String EQUAL_SPACE_FONT_COLOR = "blue";
	
	private JLabel diskSpaceLabel;	
	private JLabel domainContentLabel;
	private boolean enableFinish;
	
	/**
	 * Creates a new SummaryPage that will summarize settings.
	 * 
	 * @param settings The settings to summarize.
	 */
	public SummaryPage(WizardSettings settings) {
		super(TITLE, DESCRIPTION);
		enableFinish = true;
	}	
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(false);
		setupUi(settings);
	}
	
	private void setupUi(final WizardSettings settings) {
		if(settings != null) {

			boolean domainContentOnly = (Boolean)settings.get(ExportSettings.getExportDomainContentOnly());
			
			domainContentLabel = new JLabel();
			updateDomainContentLabel(settings, null);
			
			diskSpaceLabel = new JLabel();
			diskSpaceLabel.setText("<html><br><br><p style=\"margin-left:5px;\"><b><i>Computing required disk space. . .</i></b></p></html>");
			
			String configSummary = "<html><br><p style=\"margin-left:5px;\"><b>Note:</b></p><br>";
												
			if (!domainContentOnly) {
				
				configSummary += "<p style=\"margin-left:5px;\">Your full copy of GIFT will be exported with the current configuration settings.</p><br>"+
								 "<p style=\"margin-left:5px;\">Below is a list of configuration settings that will be copied:" +
						   		 "<ul><li>Sensor Configurations (config/sensor)</li>" +
						   		 "<li>Module Properties (config/&lt;module_name&gt;)</li>" +
						   		 "<li>Log4j Settings (config/&lt;module_name&gt;)</li></ul>" +
						   		 "</p><p style=\"margin-left:5px;\">Databases will automatically be configured.</p><br>" +
						   		 "<p style=\"margin-left:5px;\">Make sure the configuration is compatible with the target machine <br>where the exported tutor will be installed.</p><br>";
			}
			
			configSummary += "<p style=\"margin-left:5px;\">Courses intended for export should use locally referenced files that are in the<br>" +
							 "parent folder of the particular course, otherwise the file(s) will not be copied.</p><br>" +
							 "<p style=\"margin-left:5px;\">The export process will gather Survey Contexts needed by the courses being<br>" +
							 "exported as well as any other resources referenced by those surveys<br>(e.g. survey question images).</p><br>";

        	configSummary += "<br><p style=\"margin-left:5px;\">Click <b>Finish</b> to start the export process...<br></p><br></html>";
		
			JLabel configLabel = new JLabel();
			configLabel.setText(configSummary);
			
			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
			add(domainContentLabel);
			add(diskSpaceLabel);
			add(configLabel);
			
			// Use a scheduler to show the user that the required disk space is being computed
			final ShowComputingDiskSpace scheduler = new ShowComputingDiskSpace();
			scheduler.showComputingDiskSpaceText();

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
	                    
			    /** disk space info for temporarily copying files to be used in the export zip */
                private String tempDriveLetter;
                private float requiredTemp = -1;
                private float availableTemp = -1;
                
                /** disk space info for the export zip on the destination drive */
                private String destDriveLetter;
                private float availableDest = -1;
                
                @Override
                protected Void doInBackground() throws Exception {
        		    try{                    
						// Find external resources that are used by the Domain content being exported (e.g. survey images)
						boolean domainContentOnly = (Boolean)settings.get(ExportSettings.getExportDomainContentOnly());
						if (domainContentOnly) {
							DomainContent content = (DomainContent)settings.get(ExportSettings.getDomainContent());
							List<File> files = getExternalDomainResources(content);
							settings.put(ExportSettings.getExternalDomainResources(), files);
		                        
							//display these resources in the file summary
							updateDomainContentLabel(settings, files);
						}
					
				        //amount of disk space required to make the export - specifically the temp location used
				        requiredTemp = getRequiredDiskSpace(settings);
				        
				        String tempDir = System.getProperty("java.io.tmpdir"); 
				        availableTemp = getAvailableDiskSpace(tempDir);
				        
				        //determine drives for temp files and export zip file destination
				        File outputFile = new File((String) settings.get(ExportSettings.getOutputFile()));
				        tempDriveLetter = tempDir.substring(0, 2);
				        destDriveLetter = outputFile.getAbsolutePath().substring(0, 2);
				        
				        if(tempDriveLetter.equals(destDriveLetter)){
				            //GIFT is on the same partition as the GIFT export destination, therefore
				            //the required disk space is less than 2x the required temp disk space
				            requiredTemp *= 2;
				            
				        }else{
				            //amount of space available on the export destination drive
				        
				            String parentDir = outputFile.getParent();
				            availableDest = getAvailableDiskSpace(parentDir);	
				        }			   
				        
				    }catch(Throwable e){
				        logger.error("Unable to determine required and available disk space because an exception was thrown.", e);
				    }
				    
				    return null;
                }
	                    
                @Override
                protected void done() {
                    // Shut down the "Computing Disk Space" scheduler safely before displaying the required disk space
                    while (!scheduler.shutdown()) {
                        logger.error("There was a problem shutting down the ShowComputingDiskSpaceText scheduler. Retrying...");
                    }
                    
                    try{
                        setDiskspaceLabel(tempDriveLetter, requiredTemp, availableTemp, destDriveLetter, availableDest);
                        setFinishEnabled(enableFinish);
                        
                    }catch(Exception e){
                    	// An exception was thrown while computing disk space.  Do not allow the export process to continue.
                    	String errorMsg = "Unable to set required and available disk space labels because an exception was thrown.";
                        logger.error(errorMsg, e);	
                        setDiskspaceErrorMsg(errorMsg);
                        setFinishEnabled(false);
                    }
                    
                }
                
            };
            worker.execute();
		}
	}
	
	/**
	 * Update the domain content label on the summary page with the list of domain relative files
	 * that will be exported.  If a course references a file not in the domain folder it will also
	 * be shown in the label.  That list of externally referenced files is provided as an optional
	 * argument to this method.
	 * 
	 * @param settings contains the export settings to summarize
	 * @param externalDomainContent contains any files external to the domain folder that are referenced
	 * by the courses being exported.  Can be null or empty.
	 */
	private void updateDomainContentLabel(final WizardSettings settings, List<File> externalDomainContent){
	    
        DomainContent content = (DomainContent)settings.get(ExportSettings.getDomainContent());
        String output = (String)settings.get(ExportSettings.getOutputFile());
	    
	    StringBuffer summary = new StringBuffer();
        summary.append("<html><br><p style=\"margin-left:5px;\"><b>Domain Content to include:</b></p><ul>");
        for(int i = 0; i < content.getExportFiles().size(); i++) {
            
            ContentProxyInterface file = content.getExportFiles().get(i);

            String dirSuffix = file.isDirectory() ? File.separator : "";
            summary.append("<li>").append(content.getExportFiles().get(i).getName()).append(dirSuffix).append("</li>");
        }
        
        boolean domainContentOnly = (Boolean)settings.get(ExportSettings.getExportDomainContentOnly());

        //add any external domain folder content referenced by the content only export
        if(domainContentOnly && externalDomainContent != null && !externalDomainContent.isEmpty()){
            
            for(File file : externalDomainContent){
                
                //in case the file was deleted since the domain content page
                if(file.exists()){
                    String giftFile = FileFinderUtil.getRelativePath(ROOT, file);
                    summary.append("<li>GIFT").append(File.separator).append(giftFile).append("</li>");
                }
            }
            
        }else if(content.getExportFiles().isEmpty()){
            //show something in the list if no domain files are being exported
            summary.append("<li><i>nothing</i></li>");
        }

        summary.append("</ul>");
        
        summary.append("<br><p style=\"margin-left:5px;\"><b>Export Tutor to:</b>   ").append(output).append("</p></html>");
        domainContentLabel.setText(summary.toString());
	}

	/**
	 * Parses the Domain content being exported to get resources from the GIFT folder that the
	 * Domain content uses.  An example of a resource is survey images that a course might use. 
	 * 
	 * @param content the content the user has chosen to export
	 * @return an ArrayList of the resources to export.
	 * @throws IOException if there was a problem retrieving files from the system
	 * @throws DKFValidationException if there was a problem building DKF objects
	 * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
	 * @throws CourseFileValidationException if there was a problem building course objects
	 */
	private List<File> getExternalDomainResources(DomainContent content) throws IOException, FileValidationException, DKFValidationException, CourseFileValidationException {
		
		ArrayList<File> domainResources = new ArrayList<File>();
		
		for(ContentProxyInterface file : content.getExportFiles()){
			
			//can be a course file or a directory
            if(file.isDirectory()){
                //looking for courses...
                
                //
                // find course(s)
                //
                List<FileProxy> courseFiles = new ArrayList<>();
                FileFinderUtil.getFilesByExtension((AbstractFolderProxy)file, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION); 
                
                if (courseFiles.isEmpty()) {
                	continue;
                }
                
                for(FileProxy courseFile : courseFiles){
                	parseCourseForResources(courseFile, domainResources);
                }
                
            }else if(file.getName().endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)){
                //is a course file
            	parseCourseForResources((FileProxy) file, domainResources);
            }
			
		}
		
		return domainResources;
	}
	
	/**
	 * Parses a course file for resources that are used by the course
	 * 
	 * @param courseFile the course file being parsed
	 * @param domainResources a pass-by-reference holder for the resources that are found.
	 *        This ArrayList must be instantiated before being passed to this method.
	 * @throws IOException if there was a problem retrieving files from the system
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
	 */
	private void parseCourseForResources(FileProxy courseFile, ArrayList<File> domainResources) 
	        throws IOException, FileValidationException, DKFValidationException, CourseFileValidationException {
		
		if (domainResources == null) {
    		throw new IllegalArgumentException("The parameter domainResources cannot be null. It is used as a pass-by-reference variable"
    				+ " and needs to be instantiated before being passed into this method.");
    	}
		
		File courseFolder = new File(courseFile.getFileId()).getParentFile();
		DesktopFolderProxy courseFolderProxy = new DesktopFolderProxy(courseFolder);
		DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, courseFolderProxy, false);
		CourseValidationResults validationResults = dcfh.checkCourse(false, null);
		if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
		    throw new CourseFileValidationException("Course validation failed.", 
		            "There was one or more issues during validation", courseFile.getName(), validationResults.getFirstError());
		}
    	
    	//piggy back on the survey validation request logic to gather survey references used in this course
        List<SurveyCheckRequest> surveyChecks = dcfh.buildSurveyValidationRequest();
        
        //keep track of ids already exported to avoid exporting the same survey context more than once for a course
        List<Integer> exportedSurveyContexts = new ArrayList<>();
        
        for(SurveyCheckRequest check : surveyChecks){
            
            int surveyContextId = check.getSurveyContextId();
            
            if(exportedSurveyContexts.contains(surveyContextId)){
                //already exported this survey context's resources
                continue;
            }
            
            // Get image references for this survey context
            try{
                SurveyContext sContext = Surveys.getSurveyContext(dcfh.getSurveyContextId());
                for(SurveyContextSurvey scs : sContext.getContextSurveys()){
                    Survey survey = scs.getSurvey();
                    Surveys.getSurveyImageReferences(survey, domainResources);
                }
                
                exportedSurveyContexts.add(dcfh.getSurveyContextId());
            }catch(Exception e){
                throw new CourseFileValidationException("Failed to export the surveys for the course named '"+courseFile.getName()+"'.", 
                        "There was an exception thrown when retrieving the surveys from the database.", courseFile.getFileId(), e);
            }
        }      
        
        // Any other resources used by the course should be searched for here
        
	}
	
	/**
	 * Gets the required disk space to export GIFT.
	 * 
	 * @return Required disk space in MB
	 * @throws IOException if there was a problem calculating disk space
	 */
	private float getRequiredDiskSpace(WizardSettings settings) throws IOException {
	    
		// One directory up is GIFT root.		    
	    float fullSize = FileUtils.sizeOfDirectory(new File(".."));
	    
	    //now calculate the size of what is going to be filtered out
	    ExportFileFilter filter = new ExportFileFilter(settings); 
	    float filteredSize = 0;
	    try{
	        filteredSize = filter.getSizeOfFilteredFiles(new File(".."));
	    }catch(Exception e){
	        logger.error("Caught exception while trying to calculate the filtered files size.", e);
	    }
	    
	    return byteToMb((fullSize - filteredSize));
	}
	
	/**
	 * Gets the available disk space on the output drive.
	 * 
	 * @param output The output path
	 * @return Available disk space in MB.
	 */
	private float getAvailableDiskSpace(String output) {
		return byteToMb(new File(output).getFreeSpace());
	}
	
	/**
	 * Converts bytes to MB
	 * 
	 * @param bytes The bytes to convert
	 * @return The MB equivalent of bytes.
	 */
	private float byteToMb(float bytes) {
		return bytes / 1024f / 1024f;
	}
	
	/**
	 * Create the disk space summary based on the information provided.
	 * 
	 * @param tempDriveLetter the drive letter (e.g. "C:") where temporary files will be created as part of the export process.
	 * @param requiredTemp the amount of MB needed for the files being created in the temp folder
	 * @param availableTemp the amount of MB available on the drive where the temp folder is
	 * @param destDriveLetter the drive letter (e.g. "X:") where the export zip will be created.  This can be the same as the tempDriveLetter.
	 * @param availableDest the amount of MB available on the drive where the export zip will be created
	 */
	private void setDiskspaceLabel(String tempDriveLetter, float requiredTemp, float availableTemp, 
	        String destDriveLetter, float availableDest) {
	    
	    boolean sameDrive = tempDriveLetter.equals(destDriveLetter);
	    
		String diskSummary = "<html><p style=\"margin-left:5px;\"><table>";
		
		//format the required temp disk space value to a display string
		String requiredTempStr = "ERROR";
		if(requiredTemp > 0 && requiredTemp < 0.01){
		    DecimalFormat subDf = new DecimalFormat("#,###.000");
		    requiredTempStr = subDf.format(requiredTemp);
		}else if(requiredTemp != -1){
		    DecimalFormat df = new DecimalFormat("#,###.00");
		    requiredTempStr = df.format(requiredTemp);
		}
		
		//
		// show required disk space
		//
		
		if(sameDrive){
		    //the temp directory is on the same drive as the destination
		    diskSummary += "<tr><td><b>Disk Space Required To Export ("+tempDriveLetter+")</b></td><td>" + requiredTempStr + " MB</td></tr>";
		    
		}else{
		    diskSummary += "<tr><td colspan=\"2\"><b>Disk Space Required To Export:</b></td></tr>";
		    
		    //temp drive
		    diskSummary += "<tr><td align = \"center\"><b>"+tempDriveLetter+"</b></td><td>" + requiredTempStr + " MB</td></tr>";
		    
		    //destination drive
            diskSummary += "<tr><td align = \"center\"><b>"+destDriveLetter+"</b></td><td>" + requiredTempStr + " MB</td></tr>";
		}
		    
		//
		// show available disk space
		//
        DecimalFormat df = new DecimalFormat("#,###");
		
		if(sameDrive){
		    //the temp directory is on the same drive as the destination		    

            String availableStr = availableTemp == -1 ? "ERROR" : df.format(availableTemp);
            if(requiredTemp > availableTemp) {
                diskSummary += "<tr><td><b>Available Disk Space ("+tempDriveLetter+")</b></td><td><font color=\""+INSUFFICIENT_SPACE_FONT_COLOR+"\">" + availableStr + "</font> MB</td></tr></table>";
                diskSummary += "<p style=\"margin-left:5px;\"><font face=\"verdana\" color=\"red\">Not enough disk space available</font></p>";
                enableFinish = false;
            }else if(requiredTemp == availableTemp){
                diskSummary += "<tr><td><b>Available Disk Space ("+tempDriveLetter+")</b></td><td><font color=\""+EQUAL_SPACE_FONT_COLOR+"\">" + availableStr + "</font> MB</td></tr></table>";
            }else{
                diskSummary += "<tr><td><b>Available Disk Space ("+tempDriveLetter+")</b></td><td><font color=\""+ENOUGH_SPACE_FONT_COLOR+"\">" + availableStr + "</font> MB</td></tr></table>";
            }
            
		}else{
		    //handle the temp and destination drives on their own lines
		    
		    diskSummary += "<tr><td colspan=\"2\"><b>Available Disk Space:</b></td></tr>";
		    diskSummary += "<tr><td align = \"center\"><b>"+tempDriveLetter+"</b></td>";
		    		    
            String availableTempStr = availableTemp == -1 ? "ERROR" : df.format(availableTemp);
            if(requiredTemp > availableTemp) {
                diskSummary += "<td><font color=\""+INSUFFICIENT_SPACE_FONT_COLOR+"\">" + availableTempStr + "</font> MB</td><td><font face=\"verdana\" color=\""+INSUFFICIENT_SPACE_FONT_COLOR+"\">Not enough disk space available</font></td></tr>";
                enableFinish = false;
            }else if(requiredTemp == availableTemp){
                diskSummary += "<td><font color=\""+EQUAL_SPACE_FONT_COLOR+"\">" + availableTempStr + "</font> MB</td></tr>";
            }else{
                diskSummary += "<td><font color=\""+ENOUGH_SPACE_FONT_COLOR+"\">" + availableTempStr + "</font> MB</td></tr>";
            }
            
            diskSummary += "<tr><td align = \"center\"><b>"+destDriveLetter+"</b></td>";
            
            String availableDestStr = availableDest == -1 ? "ERROR" : df.format(availableDest);
            if(requiredTemp > availableDest) {
                diskSummary += "<td><font color=\""+INSUFFICIENT_SPACE_FONT_COLOR+"\">" + availableDestStr + "</font> MB</td><td><font face=\"verdana\" color=\""+INSUFFICIENT_SPACE_FONT_COLOR+"\">Not enough disk space available</font></td></tr></table>";
                enableFinish = false;
            }else if(requiredTemp == availableDest){
                diskSummary += "<td><font color=\""+EQUAL_SPACE_FONT_COLOR+"\">" + availableDestStr + "</font> MB</td></tr></table>";
            }else{
                diskSummary += "<td><font color=\""+ENOUGH_SPACE_FONT_COLOR+"\">" + availableDestStr + "</font> MB</td></tr></table>";
            }
		}

        
		diskSummary += "</html>";
		diskSpaceLabel.setText(diskSummary);
	}
	
	/**
	 * If an error occurred while calculating disk space, display a message to the user.
	 * This way they see an error and can still continue with the export process if they want.
	 * 
	 * @param message
	 */
	private void setDiskspaceErrorMsg(String errorMsg) {
		
		String message = "<html><p style=\"margin-left:5px;\">" +
						 "<br><span style=\"color:red;\">"+errorMsg+
						 "<br>Are any of the courses you are exporting invalid?"+
						 "</html>";
		
		diskSpaceLabel.setText(message);
	}
	
	/**
	 * Used to show the user a "Computing required disk space..." indication 
	 * while they are waiting for the program to determine the required disk space for the export.
	 */
	class ShowComputingDiskSpace {
		private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
		String startText = "<html><br><br><p style=\"margin-left:5px;\"><b><i>Computing required disk space";
		String endText = "</i></b></p></html>";				
		String[] dots = {"", ".", ". .", ". . ."};				
		int i=1;
		
		public void showComputingDiskSpaceText() {
		    
		    Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					if (++i == 4) i=0;					
					diskSpaceLabel.setText(startText + dots[i] + endText);	
				}
			}, "Computing Disk Space Label Update");
			
			scheduler.scheduleAtFixedRate(new Thread(t, "ShowComputingDiskSpace Thread"), 0, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
		} 
		
		/**
		 * Shuts down the scheduler safely. Should be called once the required
		 * disk space has been computed and is ready to be displayed.
		 * 
		 * @return true once the scheduler is shutdown or false if the shutdown request timed out
		 */
		public boolean shutdown() {
			scheduler.shutdown();
			try {
				return scheduler.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("There was a problem shutting down the ShowComputingDiskSpaceText scheduler: " + e);
			}
			
			return false;
		}		
	}
	
}
