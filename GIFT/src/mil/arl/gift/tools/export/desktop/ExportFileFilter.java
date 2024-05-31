/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;

/**
 * This class is a file filter for files being exported when using the export tutor tool.
 * 
 * @author mhoffman
 *
 */
public class ExportFileFilter implements FileFilter {
    
    private static Logger logger = LoggerFactory.getLogger(ExportFileFilter.class);
    
    /** default files to ignore */
    private static final File OUTPUT_DIR = new File(PackageUtil.getOutput());
    private static final File SENSOR_OUT_DIR = new File(PackageUtil.getSensorOutput());
    private static final File BUILD_DIR = new File("build");
    private static final File GENERATED_DIR = new File("generated");
    private static final String LOG_EXTENSION = ".log";
    private static final String CSV_EXTENSION = ".csv";
    
    private static final String INSTALL_GIFT_FILENAME = "installGIFT.bat";   
    private static final String LAUNCH_GIFT_FILENAME = "launchGIFT.bat";	
    private static final String INSTALL_GIFT_README_FILENAME = "GiftReadme.txt";
    
    /** the filter rules, ordered by precedence */
    private List<ExtendedFilterRule> rules = new ArrayList<>();
    
    /** the configured domain directory */
    private static final File DOMAIN_DIR = new File(ExportProperties.getInstance().getWorkspaceDirectory());
    private static final File RUNTIME_DIR = new File(ExportProperties.getInstance().getDomainRuntimeDirectory());
    private static final File EXPORT_DIR = new File(ExportProperties.getInstance().getExportDirectory());
    private static final File IMPORT_DIR = new File(ExportProperties.getInstance().getImportDirectory());
    private static final File EXPERIMENTS_DIR = new File(ExportProperties.getInstance().getExperimentsDirectory());
    private static final File TEMP_DIR = new File(ExportProperties.getInstance().getDomainDirectory()+ File.separator + "temp");
    private static final File TEMPLATES_DIR = new File(ExportProperties.getInstance().getWorkspaceDirectory()+ File.separator + "templates");
    private static final File VBS_COMMON_DIR = new File(ExportProperties.getInstance().getDomainExternalDirectory() + File.separator + "VBS.common");
    
    /**
     * Class constructor - build current filter rules
     * 
     * @param settings - contains current export wizard tool settings
     * @throws IOException 
     */
    public ExportFileFilter(WizardSettings settings) throws IOException{
        init(settings);
    }
    
    /**
     * Initialize the filter by building the current filter rules.
     * 
     * @param settings - contains current export wizard tool settings
     * @throws IOException 
     */
	private void init(WizardSettings settings) throws IOException{
	    
        boolean domainContentOnly = (Boolean)settings.get(ExportSettings.getExportDomainContentOnly());
        
        addIgnoreFileFilter(SENSOR_OUT_DIR, new String[]{CSV_EXTENSION});
        addIgnoreFileFilter(OUTPUT_DIR, new String[]{LOG_EXTENSION});
        
        //the build and generated directories are not needed for an export
        //nor to run GIFT and only add to the file size
        addIgnoreFileFilter(BUILD_DIR);
        addIgnoreFileFilter(GENERATED_DIR);
        
        if(domainContentOnly){
            //don't export these folders when doing a content only export,
            //when doing a full export these folders are needed but any content w/in is not (TODO)
            addIgnoreFileFilter(RUNTIME_DIR);
            addIgnoreFileFilter(TEMP_DIR);
            addIgnoreFileFilter(EXPORT_DIR);
            addIgnoreFileFilter(IMPORT_DIR);
            addIgnoreFileFilter(TEMPLATES_DIR);
            addIgnoreFileFilter(VBS_COMMON_DIR);
            addIgnoreFileFilter(EXPERIMENTS_DIR);
        }
    
        DomainContent content = (DomainContent)settings.get(ExportSettings.getDomainContent());        
       
        //
        //add filters for course folders not selected
        //
        List<File> courseFolders = getAllCourseFolders();
        for(File courseFolder : courseFolders){
            
            if(!content.contains(courseFolder.getCanonicalPath())) {                
                addIgnoreFileFilter(courseFolder);
            }
        }
        
        // Filter out the installGIFT Readme and launchGIFT batch files.
        addIgnoreFileFilter(new File("../"+LAUNCH_GIFT_FILENAME));
        addIgnoreFileFilter(new File("../"+INSTALL_GIFT_README_FILENAME));
        
        if(domainContentOnly){
            //include only the necessary files to run the Importer tool
            
            addIgnoreFileFilter(new File("../Training.Apps")); 
            
            if (settings.containsKey(ExportSettings.getExternalDomainResources())) {
	            @SuppressWarnings("unchecked")
	            ArrayList<File> domainResources = (ArrayList<File>)settings.get(ExportSettings.getExternalDomainResources());
	            for (File resource : domainResources) {
					
					addIncludeFileFilter(resource);
				}
            }
			
			// This is a domain content only export so remove the installGIFT script.
            addIgnoreFileFilter(new File("../"+INSTALL_GIFT_FILENAME));
            
			//Note: add this rule last as a catch those files that fall through other rules
            addIgnoreFileFilter(new File("../GIFT"));        
        }
        
        boolean exportUserData = (Boolean)settings.get(ExportSettings.getExportUserData());
        if(!exportUserData){
            //remove LMS history by ignoring the LMS database during export which will cause the default, empty
            //LMS db zip to be used upon first launching the exported GIFT.
            
            addIgnoreFileFilter(new File("../GIFT/data/derbyDb/GiftLms"));          
        }
    }
	
	/**
	 * Return the list of course folders for the desktop workspace.
	 * 
	 * @return collection of course folders found on this version of GIFT.  Can be empty but not null.
	 * @throws IOException if there was a problem searching for course folders
	 */
	public static List<File> getAllCourseFolders() throws IOException{
	    
	    List<File> courseFolders = new ArrayList<>();
        List<FileProxy> courseFiles = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(new DesktopFolderProxy(DOMAIN_DIR), courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
        for(FileProxy courseFile : courseFiles) {
            File file = new File(courseFile.getFileId());
            
            if(!courseFolders.contains(file.getParentFile())){
                courseFolders.add(file.getParentFile());
            }
        }
        
        return courseFolders;
	}
    
    /**
     * Add a file filter rule to the collection of rules for this file filter to include the specified file.
     * 
     * @param file the file to include
     * @return IncludeFileInfo the rule created
     * @throws IllegalArgumentException if the file doesn't exist
     */
    public IncludeFileInfo addIncludeFileFilter(File file) throws IllegalArgumentException{        

        IncludeFileInfo rule = new IncludeFileInfo(file);
        if(!rules.contains(rule)){
            rules.add(rule);
            return rule;
        }else{
            return (IncludeFileInfo) rules.get(rules.indexOf(rule));
        }
    }
    
    /**
     * Add a file filter rule to the collection of rules for this file filter to exclude the files with the given extension
     * in the specified directory.
     * 
     * @param ancestorDirectory - some ancestor directory to use in filtering files with the extension
     * @param extensions - the file extension to filter on for files that are a descendant of the ancestor directory.
     * @return IgnoreFileInfo - the rule created.  Will be null if the file doesn't exist.
     */
    public IgnoreFileInfo addIgnoreFileFilter(File ancestorDirectory, String[] extensions){
        
        try{
            IgnoreFileInfo rule = new IgnoreFileInfo(ancestorDirectory, extensions);
            if(!rules.contains(rule)){
                rules.add(rule);
                return rule;
            }else{
                return (IgnoreFileInfo) rules.get(rules.indexOf(rule));
            }
        }catch(@SuppressWarnings("unused") IllegalArgumentException e){
            //don't worry about it, the file doesn't exists so it will definitely be ignored!
        }
        
        return null;
    }
    
    /**
     * Add a rule to the collection of rules for this file filter to exclude the specified file or directory.
     * 
     * @param fileToIgnore - the file to not include, i.e. filter it out. This can also be a directory.
     * @return IgnoreFileInfo - the rule created
     */ 
    public IgnoreFileInfo addIgnoreFileFilter(File fileToIgnore){
          
        try{
            IgnoreFileInfo rule = new IgnoreFileInfo(fileToIgnore);
            if(!rules.contains(rule)){
                rules.add(rule);
                return rule;
            }else{
                return (IgnoreFileInfo) rules.get(rules.indexOf(rule));
            }
        }catch(@SuppressWarnings("unused") IllegalArgumentException e){
            //don't worry about it, the file doesn't exists so it will definitely be ignored!
        }
        
        return null;
    }
    
    /**
     * Return the total file size (i.e. FileUtils.sizeof(File)) value for all files
     * filtered out by this filter's rules.
     * 
     * @param root - a directory to search it's descendent files
     * @return long - the total files size
     */
    public long getSizeOfFilteredFiles(File root){
        
        long size = 0;
        
        File[] files = root.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                size += getSizeOfFilteredFiles(f);
            } else if(f.isFile()) {
                
                if(!accept(f)){
                    size += FileUtils.sizeOf(f);
                }
            }
        }
        
        return size;
    }

    @Override
    public boolean accept(File f) {
        
        for(ExtendedFilterRule rule : rules){
            
            try {
                
                boolean passed = rule.check(f);
                if(rule instanceof IgnoreFileInfo){
                    
                    if(passed){
                        //the file passed the ignore rule, meaning it should be ignored
                        logger.info("Ignoring "+f+" because of "+rule);
                        return false;
                    }
                    
                }else if(rule instanceof IncludeFileInfo){
                    
                    if(passed){
                        //the file passed the include rule, meaning it should be included
                        logger.info("Including "+f);
                        return true;
                    }
                }
                    
            } catch (IOException e) {
                logger.error("Caught exception while checking file "+f+" against "+rule, e);
                continue;
            }
        }
        
        return true;
    }
    
    /**
     * Return whether or not the ancestor/descendant relationship is true.
     * 
     * @param ancestor a possible ancestor to the descendant file
     * @param descendant a possible descendant to the ancestor file
     * @return boolean if the descendant file is a descendant of the ancestor file
     * @throws IOException if there was a problem accessing the files
     */
    private static boolean isDescendantFile(File ancestor, File descendant) throws IOException{        
        
        if(descendant == null){
            return true;
        }
        
        return ancestor.isDirectory() && descendant.getCanonicalPath().startsWith(ancestor.getCanonicalPath());
    }
    
    /**
     * This inner class is used to wrap a file filter rule.  It contains logic to evaluate the rule
     * as well and can support both filtering by file extension or a directory.
     * 
     * @author mhoffman
     *
     */
    private static interface ExtendedFilterRule{
        
        /**
         * Return whether or not the file passes this rule.
         * 
         * @param file the file to check against this rule.
         * @return boolean if the file passes the rules and is accepted.
         * @throws IOException thrown if an I/O exception occurs
         */
        public boolean check(File file) throws IOException;
    }
    
    /**
     * This inner class is used to wrap a file filter rule, more specifically a rule to include a file.
     * 
     * @author mhoffman
     *
     */
    private static class IncludeFileInfo implements ExtendedFilterRule{
        
        /** the file to include */
        private File file;
        
        /** whether the file is a file or a directory */
        private boolean isFile;
        
        /**
         * Set the file. 
         * 
         * @param file the file to include
         * @throws IllegalArgumentException if the file doesn't exist
         */
        public IncludeFileInfo(File file) throws IllegalArgumentException{
            
            if(file == null || !file.exists()){
                throw new IllegalArgumentException("The file of "+file+" doesn't exist.");
            }
            
            this.file = file;
            
            isFile = file.isFile();
        }

        @Override
        public boolean check(File file) throws IOException {
            
            if(file.isFile()){
                
                if(isFile){
                    //both are files, therefore they must be that same to pass this rule
                    return this.file.getCanonicalPath().equals(file.getCanonicalPath());
                    
                }else if(isDescendantFile(this.file, file)){
                    //the file is a descendant of this directory
                    return true;
                }
                
            }else if(isDescendantFile(file, this.file)){
                //either this file is a descendent of the directory or they are the same directory
                return true;
                
            }else if(isDescendantFile(this.file, file)){
                //the directory is a descendent of this directory
                return true;
            }
            
            return false;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[IncludeFileInfo: ");
            sb.append("file = ").append(file);
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This inner class is used to wrap a file filter rule, more specifically a rule to ignore a file.
     * 
     * @author mhoffman
     *
     */
    private static class IgnoreFileInfo implements ExtendedFilterRule{
        
    	/** a file to filter out or a file to filter descendant files from */
        private File fileToIgnore;

        /** file extensions to filter from files that are a descendant of the 'fileToIgnore' */
        private String[] fileExtensionsToIgnore = new String[0];
        
        /**
         * Class constructor - set attribute(s) 
         * 
         * @param fileToIgnore - a file to filter out or a directory to filter descendant files from
         * @throws IllegalArgumentException if the file doesn't exist
         */
        public IgnoreFileInfo(File fileToIgnore) throws IllegalArgumentException{
            
            if(fileToIgnore == null || !fileToIgnore.exists()){
                throw new IllegalArgumentException("The file "+fileToIgnore+" doesn't exist.");
            }
            
            this.fileToIgnore = fileToIgnore;
        }
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param directory - a directory to filter out or a directory to filter descendant files from
         * @param fileExtensionsToIgnore - file extensions to filter from files that are a descendant of the 'directory'
         * @throws IllegalArgumentException if the file doesn't exist
         */
        public IgnoreFileInfo(File directory, String[] fileExtensionsToIgnore) throws IllegalArgumentException{
            this(directory);
            
            if(fileExtensionsToIgnore == null || fileExtensionsToIgnore.length == 0){
                throw new IllegalArgumentException("There are no file extension to ignore.  If you wish to use a directory ignore rule use the other constructor.");
            }
            
            this.fileExtensionsToIgnore = fileExtensionsToIgnore;         
        }       
        
        /**
         * Return the file to filter out.  This could also be a directory to filter descendant files from
         * 
         * @return File
         */
        public File getFileToIgnore() {
            return fileToIgnore;
        }

        /**
         * Return the file extensions to filter from files that are a descendant of the 'directory'
         * 
         * @return String[]
         */
        public String[] getFileExtensionsToIgnore() {
            return fileExtensionsToIgnore;
        }

        @Override 
        public boolean equals(Object other){
            
            if(other instanceof IgnoreFileInfo){
                
                IgnoreFileInfo otherInfo = (IgnoreFileInfo)other;
                try {
                    
                    if(otherInfo.getFileToIgnore().getCanonicalPath().equals(this.getFileToIgnore().getCanonicalPath())){
                     
                        if(otherInfo.getFileExtensionsToIgnore().length == 0 && this.getFileExtensionsToIgnore().length == 0){
                            return true;                        
                        }else{
                            //check for matching extensions
                            
                            if(otherInfo.getFileExtensionsToIgnore().length == this.fileExtensionsToIgnore.length){
                                
                                boolean found = false;
                                for(String extension : this.fileExtensionsToIgnore){
                                    
                                    found = false;
                                    for(String otherExtension : otherInfo.getFileExtensionsToIgnore()){
                                        
                                        if(extension.equals(otherExtension)){
                                            found = true;
                                            break;
                                        }
                                    }
                                    
                                    if(!found){
                                        break;
                                    }
                                }
                                
                                if(found){
                                    return true;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Caught exception while trying to compare the directory paths", e);
                }

            }
            
            return false;
        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 29;
        	int mult = 71;
        	
        	try {
	        	// Take another prime as multiplier, add members used in equals
	        	hash = mult * hash + this.getFileToIgnore().getCanonicalPath().hashCode();
	        	hash = mult * hash + this.getFileExtensionsToIgnore().length;
	        	
	        	for(String extension : this.getFileExtensionsToIgnore()) {
	        		hash = mult * hash + extension.hashCode();
	        	}
        	} catch (IOException e) {
        		logger.error("Caught exception while computing hash code.", e);
        	}
        	return hash;
        }
        
        @Override
        public boolean check(File file) throws IOException{
            return shouldIgnore(file);
        }
        
        /**
         * Return whether or not to ignore the specified file based on the rules in this class.
         * 
         * @param file - the file to check, i.e. possible filter out
         * @return boolean - true if the file should be filtered
         * @throws IOException - thrown if an I/O exception occurs
         */
        private boolean shouldIgnore(File file) throws IOException{
            
            if(isDescendantFile(fileToIgnore, file)){
                //if parent is not null it means the file is some descendant of the directory
                
                if(fileExtensionsToIgnore.length == 0){
                    //no file extensions were specified, therefore ignoring this directory
                    return true;

                }else{
                    //check the file extension
                    
                    String name = file.getAbsolutePath();
                    for(String extension : fileExtensionsToIgnore){
                        
                        if(name.endsWith(extension)){
                            return true;
                        }
                    }
                }
            }else if (fileToIgnore.equals(file)){
            
            	return true;
            }

            
            return false;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[IgnoreFileInfo: ");
            sb.append("fileToIgnore = ").append(getFileToIgnore());
            
            sb.append(", extensions = {");
            for(String extension : getFileExtensionsToIgnore()){
                sb.append(extension).append(", ");
            }
            sb.append("}");
            
            sb.append("]");
            return sb.toString();
        }

    }

}
