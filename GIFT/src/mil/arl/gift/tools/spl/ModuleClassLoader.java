/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the URLClassLoader that makes it easy to add jarfiles
 * and whole directories.
 * 
 * @author cdettmering
 *
 */
public class ModuleClassLoader extends URLClassLoader {
	
	private static Logger logger = LoggerFactory.getLogger(ModuleClassLoader.class);
	
	/** Save the real parent so that we can implement parent-last classloading */ 
	private ClassLoader realParent;
	
	private static final String WILDCARD = "*";
	private static final String DIR_WILDCARD = "**";
	private static final String JAR_SUFFIX = ".jar";
	
	public ModuleClassLoader(URL[] urls, ClassLoader parent) {
		//send in null for the parent so it only checks the URLs in this class.
		super(urls, null);
		realParent = parent;
	}
	
	/**
	 * Processes the list of dependencies provided by adding .jar files and directories
	 * to the class loader.  
	 * supported syntax (all relative to GIFT folder):
	 * i. specific jar, ending with something'.jar' - "external/guava-18.0.jar"
	 * ii. specific directory, ending with '*' - "external/hibernate/*"
	 * iii. any intermediate directory, containing one (and only one) '**' - "data/conversionWizard/**\/*"
	 * 
	 * @param dependencies list of directories and jars to add to the class loader
	 */
	public void handleDependencies(List<String> dependencies){
	    
	    if(dependencies == null){
	        return;
	    }
	    
        for(int index = 0; index < dependencies.size(); index++) {
            
            String dependency = dependencies.get(index);
            
            if(dependency.contains(DIR_WILDCARD)){
                //recursively get all files that end with .jar
                
                dependency = dependency.substring(0, dependency.indexOf(DIR_WILDCARD));
                
                File directory = new File(dependency);
                if(directory.isDirectory()) {
                    
                        File[] files = directory.listFiles();
                        for(File file : files){
                            
                            if(file.isDirectory()){                                
                                dependencies.add(file.getAbsolutePath() + File.separator + DIR_WILDCARD);

                            }else if(file.getName().endsWith(JAR_SUFFIX)){
                                addJar(file.getAbsolutePath());
                            }
                        }
                }
                
            }else if(dependency.endsWith(WILDCARD)) {
                //add a directory so that all jars in that directory are used
                dependency = dependency.replace(WILDCARD, "");
                addDirectory(dependency);
                
            } else if(dependency.endsWith(JAR_SUFFIX)) {
                //add the specific jar
                addJar(dependency);

                
            }
        }
	}
	
	/**
	 * Adds a .jar file to the class loader
	 * 
	 * @param jarFile Path to the jarfile
	 */
	public void addJar(String jarFile) {
	    
		if(new File(jarFile).exists()) {
		    
			try {
			    if(logger.isInfoEnabled()) {
			        logger.info("Adding jar file " + jarFile);
			    }
    			// Java likes forward slashes
    			jarFile = jarFile.replace("\\", "/");
    			
    			//MH: old logic which wasn't allowing the ClassFinderLogic to find the UMS hibernate table classes
    			//   because this class couldn't find the package resource when adding the ums db jar in the following manner.
    			//addURL(new URL("jar:file:" + jarFile + "!/"));
    			addURL(new File(jarFile).toURI().toURL());


    			//System.out.println("Adding " + jarFile);
			} catch(MalformedURLException e) {
				//this should only happen if jarFile itself was a URI/URL
				//that is malformed and not a file.
				logger.error("Exception caught Malformed URL Exception while trying to add jar file " + jarFile + "." , e );

            }
			
		} else {
			logger.error("The file " + jarFile + " does not exist, cannot add to classpath");
		}
	}
	
	/**
	 * Adds a file directory to the class loader
	 * 
	 * @param fileDir Path to the file directory
	 */
	public void addFileDirectory(String fileDir) {
		if(new File(fileDir).exists()) {
			try {
			    if(logger.isInfoEnabled()) {
			        logger.info("Adding directory " + fileDir);
			    }
			    // Java likes forward slashes
			    fileDir = fileDir.replace("\\", "/");
			    addURL(new URL("file:" + fileDir));
			    //System.out.println("Adding " + fileDir);
			} catch(MalformedURLException e) {
				//this should only happen if fileDir itself was a URI/URL
				//that is malformed and not a file directory.
				logger.error("Exception caught while trying to add directory " + fileDir + ".", e);
			}
		}
	}
	
	/**
	 * Adds all the jar files in dir
	 * @param dir The directory to add files from.
	 */
	public void addDirectory(String dir) {
		File directory = new File(dir);
		if(directory.isDirectory()) {
			if(logger.isInfoEnabled()) {
			    logger.info("Adding all jar files in directory " + dir);
			}
			File[] jarFiles = directory.listFiles();
			for(File file : jarFiles) {
				//System.out.println("Looking at " + file.getName());
				if(file.getName().endsWith(".jar") || file.getName().endsWith(".JAR")) {
					//System.out.println("Adding " + file.getAbsolutePath());
					addJar(file.getPath());
				}
			}
		}
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch(@SuppressWarnings("unused") ClassNotFoundException e) {
			return realParent.loadClass(name);
		}
	}
}
