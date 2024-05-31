/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper methods to find classes based on different search criteria.
 * 
 * @author mhoffman
 *
 */
public class ClassFinderUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ClassFinderUtil.class);
    
    /** helper strings */
    private static final String CLASS_EXT = ".class";
    private static final String PACKAGE_SEPERATOR = ".";
    private static final String JAR_EXT = "jar";
    
    /**
     * Return the Java classes found in the specified package
     * 
     * @param srcPackage the GIFT package to get classes in (e.g. "mil.arl.gift.common")
     * @return An enumeration of URL objects for the resource. 
     * If no resources could be found, the enumeration will be empty. 
     * Resources that the class loader doesn't have access to will not be in the enumeration. 
     * @throws IOException if there was a problem retrieving the resources for the given path
     */
    public static Enumeration<URL> getClassesInPackage(String srcPackage) throws IOException{
        
        if(srcPackage == null){
            throw new IllegalArgumentException("The source package can't be null.");
        }
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = srcPackage.replace('.', '/');
        logger.info("Looking for resources in "+path+".");
        Enumeration<URL> resources = classLoader.getResources(path);       
        
        return resources;
    }
    
    /**
     * Return a list of Java classes that contain the annotation specified.
     * 
     * @param srcPackage the GIFT package to get classes in (e.g. "mil.arl.gift.common")
     * @param annotation the class annotation (e.g. javax.persistence.Entity.class)  to look for in the classes found in the specified package
     * @return collection of classes found in the package provided with the class annotation specified.  Can be empty but not null.
     * @throws IOException if an IOException occurs while trying to connect to the JAR file for this connection or obtain resources for the package.
     * @throws ClassNotFoundException if the class in the source package cannot be located
     */
    public static List<Class<?>> getClassesWithAnnotation(String srcPackage, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException{
        
        if(annotation == null){
            throw new IllegalArgumentException("The annotation class can't be null.");
        }
        
        Enumeration<URL> resources = getClassesInPackage(srcPackage);

        List<Class<?>> annotatedClasses = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            
            logger.info("looking at package resource of "+resource+".");
            
            if(resource.getProtocol().equals(JAR_EXT)){
                //the resource is a jar containing classes and not a build directory containing classes
                
                logger.info("Found a jar containing classes");
                
                String path = srcPackage.replace('.', '/');
                
                JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
                JarFile jar = jarConnection.getJarFile();
                
                //parse the file
                JarEntry jarEntry;
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    jarEntry= entries.nextElement();
                    
                    if(jarEntry.getName().endsWith (CLASS_EXT)){
                        //found a class
                        
                        String rawName = jarEntry.getName();
                        logger.info("Found a class named "+rawName+".");
                        
                        if(rawName.startsWith(path)){
                            //found class within specified package
                        
                            //get jar class name into java formatted class name (a.b.myClass)
                            String className = rawName.replace("/", PACKAGE_SEPERATOR);
                            className = className.replace(CLASS_EXT, "");
                            Class<?> clazz = Class.forName(className);
                            
                            if(clazz.getAnnotation(annotation) != null){
                                
                                if(!annotatedClasses.contains(clazz)){
                                    annotatedClasses.add(clazz);
                                }
                            }
                        }
                    }
                }//end while
                
                jar.close();
                
            }else{
            
                //the resource is assumed to be a build directory containing classes
                logger.info("Found a build directory containing classes");
    
                //this logic is used to remove/replace special characters from file names such as %20 for spaces
                File f;
                try {
                    f = new File(resource.toURI());
                } catch(@SuppressWarnings("unused") URISyntaxException e) {
                    f = new File(resource.getPath());
                }
                
                List<Class<?>> classes = findAllClasses(f, srcPackage, false);
                for(Class<?> clazz : classes){
                    
                    if(clazz.getAnnotation(annotation) != null){
                        
                        if(!annotatedClasses.contains(clazz)){
                            annotatedClasses.add(clazz);
                        }
                    }
                }
                
            }//end else
        }
        
        return annotatedClasses;
    }
    
    /**
     * Return a list of subclasses of the superclass specified starting at the source package name and recursively searching
     * through sub-folders.
     * 
     * @param srcPackage the source package to start at (e.g. mil.arl.gift)
     * @param superclass the superclass to check potential classes against, i.e. is the potential class a subclass of the superclass
     * @return collection of classes found matching the criteria
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<Class<?>> getSubClassesOf(String srcPackage, Class<?> superclass) throws IOException, ClassNotFoundException{ 
        return getSubClassesOf(srcPackage, superclass, false, null);
    }

    /**
     * Return a list of subclasses of the superclass specified starting at the source package name and recursively searching
     * through sub-folders.
     * 
     * @param srcPackage the source package to start at (e.g. mil.arl.gift)
     * @param superclass the superclass to check potential classes against, i.e. is the potential class a subclass of the superclass
     * @param excludePackages the optional field to specify packages that should be excluded (e.g. mil.arl.gift.domain.knowledge.condition.simile).
     * @return collection of classes found matching the criteria
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<Class<?>> getSubClassesOf(String srcPackage, Class<?> superclass, Set<String> excludePackages) throws IOException, ClassNotFoundException{ 
        return getSubClassesOf(srcPackage, superclass, false, excludePackages);
    }

    /**
     * Return a list of subclasses of the superclass specified starting at the source package name and recursively searching
     * through sub-folders.
     * 
     * Note: This method is synchronized because there is an IllegalStateException thrown when two threads run this method 
     * at the same time on the same jar file (see tickets 2083 & 1570).
     * 
     * @param srcPackage the source package to start at (e.g. mil.arl.gift)
     * @param superclass the superclass to check potential classes against, i.e. is the potential class a subclass of the superclass
     * @param returnAbstractClasses whether or not to include abstract classes in the returned collection
     * @param excludePackages the optional field to specify packages that should be excluded (e.g. mil.arl.gift.domain.knowledge.condition.simile).
     * @return collection of classes found matching the criteria
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static synchronized List<Class<?>> getSubClassesOf(String srcPackage, Class<?> superclass, boolean returnAbstractClasses, Set<String> excludePackages) throws IOException, ClassNotFoundException{   
        
        if(srcPackage == null){
            throw new IllegalArgumentException("The source package can't be null.");
        }else if(superclass == null){
            throw new IllegalArgumentException("The super class can't be null.");
        }

        String path = srcPackage.replace('.', '/');
        Enumeration<URL> resources = getClassesInPackage(srcPackage);

        if (excludePackages == null) {
            excludePackages = new HashSet<String>();
        }

        List<Class<?>> subclasses = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if(resource.getProtocol().equals(JAR_EXT)){
                //the resource is a jar containing classes and not a build directory containing classes
                
                JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
                jarConnection.setDefaultUseCaches(false); //MH: trying to fix illegalStateException thrown on the hasMoreElements #1570
                JarFile jar = jarConnection.getJarFile();
                
                //parse the file
                JarEntry jarEntry;
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    jarEntry= entries.nextElement();
                    
                    if(jarEntry.getName().endsWith (CLASS_EXT)){
                        //found a class
                        
                        String rawName = jarEntry.getName();
                        if(rawName.startsWith(path)){
                            //found class within specified package
                        
                            //get jar class name into java formatted class name (a.b.myClass)
                            String className = rawName.replace("/", PACKAGE_SEPERATOR);
                            className = className.replace(CLASS_EXT, "");

                            boolean exclude = false;
                            for (String excludedPackage : excludePackages) {
                                if (className.startsWith(excludedPackage)) {
                                    exclude = true;
                                    break;
                                }
                            }
                            if (exclude) {
                                continue;
                            }

                            Class<?> clazz = Class.forName(className);
                            
                            if(isSubclass(clazz, superclass, returnAbstractClasses)){
                            	if(!subclasses.contains(clazz)){
                            		subclasses.add(clazz);      
                            	}
                            }
                        }

                    }
        
                }//end while
                
                jar.close();
                
            }else{
                //the resource is assumed to be a build directory containing classes
                boolean exclude = false;
                for (String excludedPackage : excludePackages) {
                    if (resource.getPath().startsWith(excludedPackage)) {
                        exclude = true;
                        break;
                    }
                }
                if (exclude) {
                    continue;
                }

                //this logic is used to remove/replace special characters from file names such as %20 for spaces
                File f;
                try {
                    f = new File(resource.toURI());
                } catch(@SuppressWarnings("unused") URISyntaxException e) {
                    f = new File(resource.getPath());
                }
                
                List<Class<?>> classes = findAllClasses(f, srcPackage, true);
                for(Class<?> clazz : classes){
                    
                    if(isSubclass(clazz, superclass, returnAbstractClasses)){
                    	if(!subclasses.contains(clazz)){
                            subclasses.add(clazz);      
                    	}
                    }
                }
            }
        }
        
        return subclasses;
    }
    
    /**
     * Determine if the clazz is a subclass to superclass.
     * 
     * @param clazz the class to check if it is a subclass of the superclass
     * @param superclass the superclass to check potential classes against, i.e. is the potential class a subclass of the superclass
     * @param returnAbstractClasses whether or not to include abstract classes in the returned collection
     * @return boolean whether or not the clazz is a subclass of superclass
     */
    private static boolean isSubclass(Class<?> clazz, Class<?> superclass, boolean returnAbstractClasses){
        
        if(superclass.isAssignableFrom(clazz) && !superclass.equals(clazz)){    
            
            if(returnAbstractClasses || !Modifier.isAbstract(clazz.getModifiers())){
                return true;
            }
        }
         
        return false;
    }
        
    /**
     * Return a list of all classes starting at the source package name and recursively searching
     * through sub-folders.
     *  
     * @param directory the location to start searching from
     * @param packageName name of the package to start searching from (e.g. mil.arl.gift)
     * @param recursiveSearch whether or not to recursively search through sub directories
     * @return collection of classes found 
     * @throws ClassNotFoundException if a class could not be found using the class loader
     */
    public static List<Class<?>> findAllClasses(File directory, String packageName, boolean recursiveSearch) throws ClassNotFoundException {
        
        if(directory == null){
            throw new IllegalArgumentException("The directory can't be null.");
        }else if(packageName == null){
            throw new IllegalArgumentException("The package name can't be null.");
        }
        
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory() && recursiveSearch) {
                assert !file.getName().contains(PACKAGE_SEPERATOR);
                classes.addAll(findAllClasses(file, packageName + PACKAGE_SEPERATOR + file.getName(), recursiveSearch));
            } else if (file.getName().endsWith(".class")) {
                
                try{
                    classes.add(Class.forName(packageName + PACKAGE_SEPERATOR + file.getName().substring(0, file.getName().length() - 6)));
                }catch(UnsatisfiedLinkError linkError){
                    logger.warn("Not adding class for "+file.getName()+" to the list of potential classes found in "+directory+" because the necessary library was not found.  " +
                            "This most likely means that you have some source code that uses an external library that you don't have.", linkError);
                }
            }
        }
        return classes;
    }
    
    
    /**
     * Read the annotation for the generated class field and return the list of classes
     * that can be assigned to the specified field.  
     * For example, this can return the list of classes that can be added to a list field.
     * 
     * @param field - the field to get annotation information that lists the possible classes that can be used to instantiate this field. 
     * @return will not be null but can be empty.
     */
    public static List<Class<?>> getChoiceTypes(Field field){
        
        List<Class<?>> types = new ArrayList<>();
        
        Annotation[] annotations = field.getDeclaredAnnotations();                        
        for(Annotation annotation : annotations){
            
            if(annotation.annotationType().isAssignableFrom(XmlElements.class)){
                //found list of elements
                //create one of each applicable type in the list
                
                XmlElement[] elements = ((XmlElements)annotation).value();
                for(XmlElement element : elements){
                    
                    types.add(element.type());
                }
                
                break;
            }
        }
        
        return types;
    }
}
