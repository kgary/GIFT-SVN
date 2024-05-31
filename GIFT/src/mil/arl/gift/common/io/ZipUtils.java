/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Some easy to use functions for dealing with archives using the apache library.
 * 
 * @author cdettmering
 */
public class ZipUtils {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(ZipUtils.class);
	
	/**
	 * Write the contents of the input stream to the zip file specified.  This is useful for when
	 * the input stream is either completely ready to be read or the data is coming in over time (e.g. HTTP request).
	 * 
	 * @param inputStream the input stream of data to write to the file.  Can't be null.
	 * @param destinationZip the zip file to write.  Can't be null.
	 * @throws IOException if there was a problem with the stream or writing to the file.
	 */
	public static void zipFromInputStream(InputStream inputStream, File destinationZip) throws IOException{                      
	    FileUtils.copyInputStreamToFile(inputStream, destinationZip);
	}

	/**
	 * Create a zip file containing the folder provided.
	 * 
	 * @param folderToZip the folder to zip.
	 *         Note: this folder will not be included in the zip, rather the zip will start with its children.
	 * @param zipFile the zip file to populate
     * @throws IllegalArgumentException if there was a problem with the file, e.g. it doesn't exist
     * @throws IOException if there was a problem writing the file to the stream
	 */
	public static void zipFolder(File folderToZip, File zipFile) throws IllegalArgumentException, IOException{
	    
	    if(folderToZip == null || !folderToZip.exists() || folderToZip.isFile()){
	        throw new IllegalArgumentException("The folder to zip of "+folderToZip+" doesn't exist or is not a directory.");
	    }
	    
	    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));	    	    
	    addFile(zos, folderToZip, folderToZip.getCanonicalPath());
	    zos.close();
	}
	
	/**
	 * Create a zip file containing the files provided.
	 * Note:  the resulting zip will contain a flat list of files
	 * 
	 * @param filesToZip the files to include in the zip file
	 * @param zipFile the zip file to populate
     * @throws IllegalArgumentException if there was a problem with a file, e.g. it doesn't exist
     * @throws IOException if there was a problem writing the file to the stream
	 */
	public static void zipFiles(List<File> filesToZip, File zipFile) throws IllegalArgumentException, IOException{
	    
	    if(filesToZip == null || filesToZip.isEmpty()){
	        throw new IllegalArgumentException("There must be at least 1 file to zip.");
	    }
	    
        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))){ 
            for(File file : filesToZip){
                addFile(zos, file, file.getParentFile().getCanonicalPath());
            }
        }
	}
	
	/**
     * Add a file to an existing zip stream.
     * 
     * @param zip the string path to the existing zip. Used so a new zip is not
     *        created
     * @param file the file to add to the zip
     * 
     * @throws IllegalArgumentException if there was a problem with the file,
     *         e.g. it doesn't exist
     * @throws IOException if there was a problem writing the file to the stream
     */
    public static void copyZip(String zipSrc, String zipDest)
            throws IllegalArgumentException, IOException, net.lingala.zip4j.exception.ZipException {
        net.lingala.zip4j.core.ZipFile srcZip = new net.lingala.zip4j.core.ZipFile(zipSrc);
        net.lingala.zip4j.core.ZipFile destZip = new net.lingala.zip4j.core.ZipFile(zipDest);
        
        for (Object fh : srcZip.getFileHeaders()) {
            ZipParameters parameters = new ZipParameters();

            parameters.setFileNameInZip(((FileHeader) fh).getFileName());
            parameters.setSourceExternalStream(true);

            /* Needed to maintain the size of the original .zip. */
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
            
            BufferedInputStream is = new BufferedInputStream(srcZip.getInputStream((FileHeader) fh));

            if (is != null) {
                destZip.addStream(is, parameters);
            }
        }
    }

    /**
     * Add a folder to an existing zip stream.
     * 
     * @param zip the string path to the existing zip. Used so a new zip is not
     *        created
     * @param folder the folder to add to the zip
     * @param prefix the directory to add the zip to. Cannot be null.
     * 
     * @throws IllegalArgumentException if there was a problem with the file,
     *         e.g. it doesn't exist
     * @throws IOException if there was a problem writing the file to the stream
     * @throws ZipException if there was a problem creating a zip file with the
     *         given path
     */
    public static void addFolder(String zip, File folder)
            throws IllegalArgumentException, IOException, net.lingala.zip4j.exception.ZipException {
        net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(zip);
        zipFile.addFolder(folder, new ZipParameters());
    }

    /**
     * Add the file to the zip stream.
     * 
     * @param zos the stream where the file contents are being written too
     * @param file the file to write to the zip
     * @param baseFolderName the name of the folder containing the file to zip
     *        (the same value as file, if the file is the root folder). Note:
     *        this folder will not be included in the zip, rather the zip will
     *        start with its children.
     * @throws IllegalArgumentException if there was a problem with the file,
     *         e.g. it doesn't exist
     * @throws IOException if there was a problem writing the file to the stream
     */
	private static void addFile(ZipOutputStream zos, File file, String baseFolderName)throws IllegalArgumentException, IOException{
	    
        if(file.exists()){
            
            if(file.isDirectory()){
                
                File subfolders[] = file.listFiles();
                for(int i=0;i<subfolders.length;i++){
                    addFile(zos, subfolders[i],baseFolderName);    
                }
                
            }else{
                //add file
                
                //extract the relative name for entry purpose
                String fileName = file.getCanonicalPath();  //get the name w/o any '..' in the path
                String entryName = fileName.substring(baseFolderName.length()+1,fileName.length()); //remove the base folder name from the name of the zip entry
                
                if(logger.isInfoEnabled()){
                    logger.info("Adding entry to zip " + entryName + "...");
                }
                
                ZipEntry ze= new ZipEntry(entryName);
                zos.putNextEntry(ze);
                
                try(FileInputStream in = new FileInputStream(fileName)){
                    int len;
                    byte buffer[] = new byte[1024];
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                
                }
                zos.closeEntry();
 
            }
            
        }else{
            throw new IllegalArgumentException("The file to zip of "+file+" doesn't exist.");
        }
 
    }
	
    /**
     * Unzips archive into outpitDir
     * 
     * @param archive The zip file to unzip
     * @param outputDir The output directory to unzip into.
     * @param progressIndicator used to notify the caller of unzip progress on a file by file basis (i.e. percent complete)
     * Can be null.
     * @throws IOException if there was a problem un-zipping the archive
     */
    public static void unzipArchive(File archive, File outputDir, ProgressIndicator progressIndicator) throws IOException  {
        
        if(logger.isInfoEnabled()){
            logger.info("Unzipping archive " + archive.getAbsolutePath());
        }
        
        ZipFile zipfile = new ZipFile(archive);
        double totalFileCnt = 0;
        //apparently there is not size method so have to calculate manually
        for (Enumeration<ZipArchiveEntry> e = zipfile.getEntries(); e.hasMoreElements(); ) {
            e.nextElement();
            totalFileCnt++;
        }
        
        int currentFileCnt = 1;
        for (Enumeration<ZipArchiveEntry> e = zipfile.getEntries(); e.hasMoreElements(); ) {
            ZipArchiveEntry entry = e.nextElement();
            unzipEntry(zipfile, entry, outputDir);
            if(progressIndicator != null){
            	progressIndicator.setPercentComplete((int)((currentFileCnt++/totalFileCnt) * 100));
            }
        }
        
        ZipFile.closeQuietly(zipfile);
    }

    /**
     * Unzips the single entry from zipFile into outputDir
     * 
     * @param zipfile The zip file that entry lives in
     * @param entry The zip entry to unzip
     * @param outputDir The output directory to unzip to.  Can already exist.
     * @throws IOException if there was a problem creating files related to unzipping this entry
     * @throws ZipException if there was a problem getting the contents of the zip entry
     */
    public static void unzipEntry(ZipFile zipfile, ZipArchiveEntry entry, File outputDir) throws ZipException, IOException {
    	
    	if(logger.isInfoEnabled()){
    	    logger.info("Unpacking entry " + entry.getName());
    	}
        if (entry.isDirectory()) {
            
            File dir = new File(outputDir, entry.getName());
            if(!dir.exists() && !createDir(dir)){
                throw new IOException("Unable to create directory named "+dir+".");
            }
            
            return;
        }
        
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            if(!createDir(outputFile.getParentFile())){
                throw new IOException("Unable to create directory named "+outputFile.getParentFile()+".");
            }
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } catch(IOException e) {
        	logger.error("Caught exception while unzipping entry", e);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }
    
    /**
     * Unzip a specific folder from the archive to the output directory.
     * 
     * Example:  export.zip contains
     *                a/one/
     *                b/two/
     *           
     *            if folderPath = "a", the output directory will contain the "a" directory
     *            if folderPath = "b/two", the output directory will contain the "b/two" directory
     *               
     * @param archive the zip file to unzip
     * @param outputDir the directory to unzip too
     * @param folderPath the path of a folder to unzip
     * @throws Exception - thrown if there is a severe exception during un-zipping
     */
    public static void unzipFolder(File archive, File outputDir, String folderPath) throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Unzipping folder with path "+folderPath+" in archive " + archive.getAbsolutePath()+" to "+outputDir);
        }
        
        //zip entry folders end with forward slash, therefore the folder to search for should too
        if(!folderPath.endsWith("/")){
            folderPath += "/";
        }
        
        ZipFile zipfile = new ZipFile(archive);
        for (Enumeration<ZipArchiveEntry> e = zipfile.getEntries(); e.hasMoreElements(); ) {
            ZipArchiveEntry entry = e.nextElement();
            
            if(entry.getName().equals(folderPath) || entry.getName().startsWith(folderPath)){
                //found folder
                unzipEntry(zipfile, entry, outputDir);
            }
        }
        
        ZipFile.closeQuietly(zipfile);
    }

    /**
     * Creates directory dir if possible
     * 
     * @param dir The directory to create, cannot be null.
     * @return boolean whether the directory was created or not
     */
    public static boolean createDir(File dir) {
        
    	if(dir != null) {
	    	if(logger.isInfoEnabled()){
	    	    logger.info("Creating directory " + dir.getAbsolutePath());
	    	}
	    	
	    	return dir.mkdirs();
    	}
    	
    	return false;
    }
    
    /**
     * Deletes the directory dir is possible
     * 
     * @param dir The directory to delete, cannot be null and must exist.
     */
    public static void deleteDir(File dir) {
        
    	if(dir != null && dir.exists() && dir.isDirectory()) {
	    	if(logger.isInfoEnabled()){
	    	    logger.info("Deleting directory " + dir.getAbsolutePath());
	    	}
	    	try {
	    		FileUtils.deleteDirectory(dir);
	    	} catch(IOException e) {
	    		logger.error("Caught exception while deleteing directory", e);
	    	}
    	}
    }
}