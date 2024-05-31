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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to find files in GIFT
 * 
 * @author mhoffman
 *
 */
public class FileFinderUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FileFinderUtil.class);
    
    /**
     * File filter used to ignore GIFT authorable files (e.g. dkf.xml) and .svn folders.  This is useful
     * for finding other files (e.g. PowerPoint) associated with a course that have been uploaded to the course folder.
     */
    public static final FileFilter IGNORE_SVN_AND_GIFT_FILES_FILE_FILTER = new FileFilter() {
        
        @Override
        public boolean accept(File f) {                    
            
            //always filter .svn folders
            if (f.isDirectory()){                
                return !f.getName().equals(Constants.SVN);                
            } 
            
            try{
                AbstractSchemaHandler.getFileType(f.getName());
                return false;  //a GIFT file, ignore it
            }catch(@SuppressWarnings("unused") IllegalArgumentException e){
                return true; //NOT a GIFT file, accept it
            }

        }
    };
    
    /**
     * Recursively search up the file tree starting in the directory provided until a course.xml
     * file is found.  The parent folder to the course file is a course folder that is returned
     * by this method.  This method will not search into folders that are found as the intent
     * is to find an ancestor folder that contains a course file.
     * 
     * The search will stop once the workspace folder has been reached.
     * 
     * @param directory the directory to start searching in, followed by it's parent folder.
     * @return the first ancestor course folder found starting with the provided directory.  Null can be returned if
     * no course folder was found.
     * @throws IOException if there was a problem searching
     */
    public static File findAncestorCourseFolder(File directory) throws IOException{
        
        if(directory == null){
            return null;
        }else if(!directory.isDirectory()){
            throw new IllegalArgumentException("The directory value must be a directory.");
        }        
        
        //look for a course.xml file in the current directory
        File[] files = directory.listFiles();
        for(File file : files){
            
            if(file.getName().endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)){
                return file.getParentFile();
            }
        }
        
        //didn't find a course.xml file in this directory, check the parent directory
        File parent = directory.getParentFile();
        if(parent == null || parent.getCanonicalPath().equals(new File(CommonProperties.getInstance().getWorkspaceDirectory()).getCanonicalPath())){
            //reached the end of searching
            return null;
        }else{
            return findAncestorCourseFolder(parent);
        }
    }
    
    /**
     * This method will attempt to find the file specified in the resources known to this class.
     * This is useful for finding resources (i.e. files) that could be in a jar on the classpath.
     * 
     * @param filename the name of the file to find
     * @return one of the following could be returned:
     *          null: the file was not found 
     *          InputStream: the file was found by getResourceAsStream method
     */
    public static InputStream getFileByClassLoader(String filename){
        
        String formattedFilename = filename.replace("\\", "/");
        if(!formattedFilename.startsWith("/")){
            formattedFilename = "/" + formattedFilename;
        }
        
        if(FileFinderUtil.class.getResourceAsStream(formattedFilename) != null){

            InputStream iStream = FileFinderUtil.class.getResourceAsStream(formattedFilename);
            return iStream;
        }
        
        return null;
    }

    /**
     * Find files of a certain name (e.g. "filename.txt") from the starting directory down.
     * 
     * @param startingDirectory - where to start the search for files
     * @param files - list of files found
     * @param filename - the filename to search for
     * @throws IOException if there was a problem retrieving the files
     */
    public static void getFilesByName(AbstractFolderProxy startingDirectory, List<FileProxy> files,
            final String filename) throws IOException {
        files.addAll(startingDirectory.listFilesByName(null, filename));
        Collections.sort(files);
    }

    /**
     * Find files of a certain extension (e.g. "mp3") from the starting directory down.
     * 
     * @param startingDirectory - where to start the search for files
     * @param files - list of files found
     * @param extensions - a list of file extensions to filter on (can be null if no filter is needed)
     * @throws IOException if there was a problem retrieving the files
     */
    public static void getFilesByExtension(AbstractFolderProxy startingDirectory, List<FileProxy> files, final String... extensions) throws IOException {
        getFilesByExtension(startingDirectory, files, null, extensions);
    }
    
    /**
     * Find files of a certain extension (e.g. "mp3") from the starting directory down.
     * 
     * @param startingDirectory - where to start the search for files
     * @param files - list of files found
     * @param pathsToExclude - a sequence of paths that should not be searches when fetching the files. If
     * null or empty, then no paths will be excluded.
     * @param extensions - a list of file extensions to filter on (can be null if no filter is needed)
     * @throws IOException if there was a problem retrieving the files
     */
    public static void getFilesByExtension(AbstractFolderProxy startingDirectory, List<FileProxy> files, Iterable<String> pathsToExclude, final String... extensions) throws IOException{
        
        files.addAll(startingDirectory.listFiles(pathsToExclude, extensions));        
        Collections.sort(files);
    }
    
    /**
    * break a path down into individual elements and add to a list.
    * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
    * 
    * @param f input file
    * @return a List collection with the individual elements of the path in reverse order.</br>
    * Null will be returned if an error happened.
    */
    private static List<String> getPathList(File f) {
        
        List<String> l = new ArrayList<String>();
        File r;
        try {
            r = f.getCanonicalFile();
            //MH: changed from using the while loop to this if block because for some unknown reason the file length for a folder (w/in Domain/)
            //created by Stephanie was of length zero which caused the while loop to finish prematurely.
            if(r != null){
                String[] tokens = r.getAbsolutePath().split(Pattern.quote(File.separator));
                for(int i = tokens.length-1; i > 0; i--){
                    l.add(tokens[i]);
                }
            }
//            while(r != null && r.length() > 0) {
//                l.add(r.getName());
//                r = r.getParentFile();
//            }
        }catch (IOException e) {
            logger.error("An exception happened when trying to get the canonical file for "+f, e);
            l = null;
        }
        
        return l;
    }
    
    /**
    * figure out a string representing the relative path of
    * 'f' with respect to 'r'
    * 
    * @param r home path
    * @param f path of file
    */
    private static String matchPathLists(List<String> r,List<String> f) {
        
        int i;
        int j;
        String s;
        
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size()-1;
        j = f.size()-1;
        
        // first eliminate common root
        while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }
        
        // for each remaining level in the home path, add a ..
        for(;i>=0;i--) {
            s += ".." + File.separator;
        }
        
        // for each level in the file path, add the path
        for(;j>=1;j--) {
            s += f.get(j) + File.separator;
        }
        
        //if 'f' is a file, then get the file name
        if(j >= 0){
            s += f.get(j);
        }
        
        return s;
    }
    
    /**
    * get relative path of File 'f' with respect to 'home' directory</br>
    * example : </br>
    * home = /a/b/c</br>
    * f = /a/d/e/x.txt</br>
    * s = getRelativePath(home,f) = ../../d/e/x.txt
    * 
    * @param home base path, should be a directory, not a file, or it doesn't make sense
    * @param f file to generate path for
    * @return path from home to f as a string
    */
    public static String getRelativePath(File home,File f){
        
        List<String> homelist;
        List<String> filelist;
        String s;
        
        homelist = getPathList(home);
        filelist = getPathList(f);
        
        s = matchPathLists(homelist,filelist);
        return s;
    }
    
    /**
     * Gets the relative path to a file in a course folder. For example:<br>
     * coursePath = username/courseFolder/course.xml<br>
     * filePath = default-domain/workspaces/username/courseFolder/dkfFolder/dkf.xml<br>
     * result = dkfFolder/dkf.xml<br>
     * 
     * @param coursePath The path to the course.xml file
     * @param filePath The path to a file in the course folder
     * @return A course relative path to the file. Returns null if the filePath is not contained 
     * in the courseFolder, or if it is already relative to the course folder.
     */
    public static String getCourseFolderRelativePath(String coursePath, String filePath) {
    	
    	// filePaths sometimes have slashes that don't match the coursePath, so convert them 
    	// before doing any string manipulation 
    	String fileName = filePath.replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);
        
    	// Remove any leading slashes from the courseFolder path.
    	String courseFolder = coursePath.startsWith(Constants.FORWARD_SLASH) ? coursePath.substring(1) : coursePath;
    	
    	// Trim the path so that it only contains username/courseFolder/
        courseFolder = courseFolder.substring(0, courseFolder.lastIndexOf(Constants.FORWARD_SLASH) + 1);
        
        if(fileName.contains(courseFolder)) {
        	// Remove "default-domain/workspaces/username/courseFolder/" from the filePath
        	return fileName.substring(fileName.indexOf(courseFolder) + courseFolder.length());
        }
        
    	return null;
    }
    
    /**
     * Return a new file filter that can be used to filter out the .svn folder and include any
     * file extension(s) (suffix) provided.
     * 
     * @param extensions optional file extensions to filter on (i.e. include files with those extension(s)).  Can be null or empty - means don't
     * filter on file extensions.
     * @return a new file filter
     */
    public static FileFilter getSVNFolderAndExtensionsFileFilter(String... extensions){
        
        return new FileFilter() {
            
            @Override
            public boolean accept(File f) {                    
                
                //always filter .svn folders
                if (f.isDirectory()){
                    
                    return !f.getName().equals(Constants.SVN);
                    
                } else if(extensions != null && extensions.length > 0) {
                    //check extensions provided
                    
                    for(String extension : extensions){

                        if(f.getName().endsWith(extension)) {
                            return true;
                        }
                    }                        
                    
                } 
                
                //only return true here if there are no extensions
                return extensions == null || extensions.length == 0;
            }
        };
    }

}
