/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains File operation utility methods.
 * 
 * @author mhoffman
 *
 */
public class FileUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);    

    public static final String SURVEY_REF_EXPORT_SUFFIX = ".surveys.export";
    public static final String QUESTION_EXPORT_SUFFIX = ".question.export";    
    
    public static final String XTSP_JSON_EXPORT_SUFFIX = ".json";
    
    public static final String BACKUP_FILE_EXTENSION = ".bak";
    
    public static final String PARADATA_FILE_EXTENSION = ".paradata";
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-YYYY_hh-mm-ss");
    
    /** collection of files to delete when the JVM shuts down */
    private static List<File> filesToDeleteOnShutdown = new ArrayList<>();
    
    /** 
     * The name of the temporary directory for this GIFT version to place all temp files created (and hopefully
     * automatically deleted) for this GIFT instance.
     */
    private static final String GIFT_TEMP_DIR_NAME = "GIFT."+Version.getInstance().getName()+".";
    private static File GIFT_TEMP_DIR;
    static{
        try {
            Path path = Files.createTempDirectory(GIFT_TEMP_DIR_NAME);
            GIFT_TEMP_DIR = new File(path.toUri());            
            registerFileToDeleteOnShutdown(GIFT_TEMP_DIR);
            
            Runtime.getRuntime().addShutdownHook(new Thread("Deleting Files on JVM Shutdown") {

                @Override
                public void run() {
                    
                    for(File file : filesToDeleteOnShutdown){
                        FileUtils.deleteQuietly(file);  //will never throw an exception (according to javadoc)
                    }
                }
           });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a file to be deleted when the JVM shuts down.
     * 
     * @param file an existing file that should be deleted when closing the JVM
     */
    public static void registerFileToDeleteOnShutdown(File file){
        
        if(file != null){
            filesToDeleteOnShutdown.add(file);
        }
    }
    
    /**
     * Return the temporary directory created for this GIFT instance to write temporary
     * files too.
     * 
     * @return File the GIFT temp directory.  Note this directory will automatically be deleted
     * when the JVM exits.  Can be null if the creation of the temp directory failed.
     */
    public static File getGIFTTempDirectory(){
        return GIFT_TEMP_DIR;
    }

    /**
     * Recursively counts the number of files in a directory.
     * 
     * @param dir The directory to count the files in.
     * @return Total number of files in dir, or -1 if error.
     */
    public static int countFilesInDirectory(File dir) {
        
        if(!(dir.exists() && dir.isDirectory())) {
            throw new IllegalArgumentException("The directory of "+dir+" doesn't exist.");
        }
        
        File[] files = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                
                if(pathname.isDirectory()){
                    return !pathname.getName().endsWith(Constants.SVN);
                }
                
                return true;
            }
        });
        int count = 0;
        for(File f : files) {
            if(f.isDirectory()) {
                count += countFilesInDirectory(f);
            } else if(f.isFile()) {
                count++;
            }
        }
        
        return count;
    }    
    
    /**
     * Converts bytes to MB
     * 
     * @param bytes The bytes to convert
     * @return The MB equivalent of bytes.
     */
    public static float byteToMb(float bytes) {
        return bytes / 1024f / 1024f;
    }
    
    /**
	 * Converts bytes to KB
	 * 
	 * @param bytes The bytes to convert
	 * @return The KB equivalent of bytes.
	 */
	public static long byteToKb(long bytes) {
		return bytes / 1024L;
	}	
    
    /**
     * Delete the specified file from disk.  If the file is a directory it will
     * recursively delete all ancestor files and then it will delete the root directory.
     *  
     * @param file the file to delete.  Can be a directory.
     * @throws IOException if there was a problem deleting the directory
     */
    public static void delete(File file) throws IOException{
     
        if(file.isDirectory()){
            
            //directory is empty, then delete it
            if(file.list().length==0){
 
               file.delete();
 
            }else{
 
               //list all the directory contents
               String files[] = file.list();
 
               for (String temp : files) {
                  //construct the file structure
                  File fileDelete = new File(file, temp);
 
                  //recursive delete
                 delete(fileDelete);
               }
 
               //check the directory again, if empty then delete it
               if(file.list().length==0){
                 file.delete();
               }
            }
 
        }else{
            //if file, then delete it
            file.delete();
        }
    }
    
    /** 
     * Deletes a directory from disk.  This is a wrapper function for the 
     * Apache Commons IO library deleteDirectory method.  
     * 
     * @param directory - Directory object to be deleted (cannot be null,  must be a directory, must exist for this method to do anything).
     * @throws IOException - Exception if there was an error trying to delete the directory.
     */
    public static void deleteDirectory(File directory) throws IOException {
        
        if (directory != null && directory.isDirectory() && directory.exists()) {
            FileUtils.deleteDirectory(directory); 
        }
    }
    
    /**
     * Recursively deletes directory files in order to display progress to the user.
     * 
     * @param dir The directory to delete
     * @param deleteProgressData contains information used to track progress as well as be updated as progress continues
     * @param progressIndicator used to show export progress to the user.  This method will also update the subtask progress.
     */
    public static void deleteDirectory(File dir, DeleteProgressData deleteProgressData, 
            ProgressIndicator progressIndicator) {
        
        if(!(dir.exists() && dir.isDirectory())) {
            logger.error("Could not delete " + dir.getAbsolutePath() + " directory doesn't exist or path is not a directory");
            return;
        }
        
        File[] files = dir.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                deleteDirectory(f, deleteProgressData, progressIndicator);
            } else if(f.isFile()) {
                deleteProgressData.currentFileCount++;
                progressIndicator.setSubtaskProgress((int)(((float)deleteProgressData.currentFileCount / (float)deleteProgressData.totalFileCount) * 100));
                progressIndicator.setPercentComplete((progressIndicator.getSubtaskProgress() / (100 / deleteProgressData.percentOfTask)) + deleteProgressData.originalProgress);
                
                if(logger.isInfoEnabled()){
                    logger.info("Deleting " + f.getAbsolutePath());
                }
                
                if(!FileUtils.deleteQuietly(f)) {
                    logger.error("Could not delete " + f.getAbsolutePath());
                }
            }
        }
        
        //now that all files have been deleted, delete the directory
        dir.delete();
    }
    
    /**
     * Copy the specified file to a new file with a timestamped suffix added to the new file name.
     *  
     * @param file the file to make a copy of
     * @return File the new file with the timestamp suffix attached to the end of the original file name
     * @throws IOException if there was a problem copying the file
     */
    public static File backupFile(File file) throws IOException{
        
        File newFile = new File(file.getAbsolutePath() + "." + sdf.format(new Date()) + BACKUP_FILE_EXTENSION);
        FileUtils.copyFile(file, newFile);
        return newFile;
    }
    
    /**
     * Updates all URI fields containing a particular URI within a course file or lesson material file.
     * 
     * @param oldUri - the old URI to be replaced
     * @param newUri - the new URI that the old URI should be replaced with
     * @param filename - the name of the file on which to update references
     * @return boolean whether or not the file contents changed, i.e. were any URI values changed
     * @throws Exception - If an error occurs while accessing the file or updating its contents.
     */
    public static boolean updateUriFields(String oldUri, String newUri, String filename) throws Exception{
        
        if(filename == null){
            throw new IllegalArgumentException("The file name can't be null");
        }
        
        File f = new File(filename);
        if(!f.exists()){
            throw new IllegalArgumentException("Unable to find file named "+f.getAbsolutePath());
        }
        
        java.io.FileInputStream input = new java.io.FileInputStream(f); 
        String fileContent = org.apache.commons.io.IOUtils.toString(input, "UTF-8");        
        input.close();
        
        // in both the old URI to be replaced and the replacement URI, replace all "&"s not followed by "amp;" with "&amp;"s
        // "&amp;"s become "&"s internally when the course XML files are parsed, so they need to be reverted here
        oldUri = oldUri.replaceAll("\\Q&\\E(amp;){0}", "&amp;");
        newUri = newUri.replaceAll("\\Q&\\E(amp;){0}", "&amp;");
        
        // Replace any URI fields containing the old URI with new URI fields containing the new URI
        // "\\Q" and "\\E" are used to ignore special regular expression symbols between them.
        // "\\s*" is used to ignore whitespace characters.
        String newFileContent = fileContent.replaceAll("\\Q<uri>\\E\\s*\\Q" + oldUri + "\\E\\s*\\Q</uri>\\E", "<uri>" + newUri + "</uri>");
        
        java.io.FileOutputStream output = new java.io.FileOutputStream(f);
        org.apache.commons.io.IOUtils.write(newFileContent, output, "UTF-8");       
        output.close();

        return !newFileContent.equals(fileContent);
    }
    
    /**
     * Returns the total size of the specified file including all descendant file sizes.
     * Ignores .svn folders.
     * 
     * @param file the file to get the size of
     * @return the total size in bytes of the file.
     */
    public static long getSize(File file){
        
        long size = 0;
        
        if(!file.isDirectory()){
            return FileUtils.sizeOf(file);
        }
        
        //get files in this directory, ignoring .svn folder
        File[] files = file.listFiles(FileFinderUtil.getSVNFolderAndExtensionsFileFilter());
        for(File f : files) {
            if(f.isDirectory()) {
                size += getSize(f);
            } else if(f.isFile()) {
                size += FileUtils.sizeOf(f);
            }
        }
        
        return size;
    }
    
    /**
     * Return the latest modified file (ignoring directories) in the directory provided. 
     * 
     * @param directory the directory to search
     * @return milliseconds since epoch of the latest modified file in the directory.  If there
     * are no files in the directory the time is for the folder itself.
     */
    public static long getLastModifiedDate(File directory){
        
        if(directory == null || !directory.exists()){
            throw new IllegalArgumentException("The directory "+directory+" doesn't exist or is null.");
        }else if(directory.isFile()){
            return directory.lastModified();
        }
        
        long latestModified = 0;
        File[] files = directory.listFiles();
        if(files == null || files.length == 0){
            return directory.lastModified();
        }
        
        for(File file : files) {
            //don't look at directories which have a tendency to update times independent
            //of descendant files.  We see this when deploying a GIFT Cloud instance, all folders
            //have new timestamps even though their descendant files haven't changed modified dates.
            if (!file.isDirectory() && latestModified < file.lastModified()) {
                latestModified = file.lastModified();
            }
        }
        
        return latestModified;
    }
    
    /**
     * This inner class is used to wrap various data members used for maintaining and calculating
     * progress of a delete folder (and all descendant files) operation.
     * 
     * @author mhoffman
     *
     */
    public static class DeleteProgressData{
        
        /** running total of the number of files deleted for the delete operation */
        public int currentFileCount;
        
        /** total number of files that will be deleted */
        public final int totalFileCount;
        
        /** 
         * the percent this delete operation is of the total task being performed
         * e.g. deleting the export directory after creating the export zip is only 25% of the 100% of the export task
         */
        public final int percentOfTask;
        
        /** 
         * the percent complete the overall task being performed is at before starting this delete operation
         * e.g. deleting the export directory after creating the export zip starts at the point when progress is 75% complete 
         */
        public final int originalProgress;
        
        /**
         * Set attributes
         * 
         * @param totalFileCount total number of files that will be deleted
         * @param percentOfTask the percent this delete operation is of the total task being performed
         * e.g. deleting the export directory after creating the export zip is only 25% of the 100% of the export task
         * @param currentProgress the percent complete the overall task being performed is at before starting this delete operation
         * e.g. deleting the export directory after creating the export zip starts at the point when progress is 75% complete 
         */
        public DeleteProgressData(int totalFileCount, int percentOfTask, int currentProgress){
            
            if(totalFileCount <= 0){
                throw new IllegalArgumentException("The total file count of "+totalFileCount+" must be greater than zero.");
            }
            
            this.totalFileCount = totalFileCount;
            
            if(percentOfTask <= 0){
                throw new IllegalArgumentException("The percent of task of "+percentOfTask+" must be greater than zero.");
            }
            
            this.percentOfTask = percentOfTask;
            
            if(currentProgress <= 0){
                throw new IllegalArgumentException("The current progress of "+currentProgress+" must be greater than zero.");
            }
            
            this.originalProgress = currentProgress;
        }
    }
}
