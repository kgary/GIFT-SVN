/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/              
package mil.arl.gift.gateway.interop.ppt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the loading of the Jacob dll into a class that can be used by the same class loader
 * multiple times without causing an UnsatisfiedLinkError jacob-1.17-M2-x64.dll already loaded in another classloader (#5266).
 * This class is then placed into its own jar using Gateway's build.xml compileJacobLoader target.
 * 
 * @author mhoffman
 *
 */
public class JacobLibraryStaticLoader {
    
    /** 
     * A system property used to tell across class loaders whether Jacob has been loaded. 
     * 
     * This is needed because attempting to load the DLL into the same JVM more than once causes an error, 
     * so relying on static class allocation alone is not sufficient because this class could be loaded 
     * by multiple class loaders.
     */ 
    private static final String JACOB_DLL_LOADED_PROPERTY = "jacobDllLoaded";
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(JacobLibraryStaticLoader.class);

    /**
     * load JACOB DLL
     * This was originally in PPTInterface.java but moved here to prevent UnsatisfiedLinkError (#5266)
     */
    public static void loadJacobNativeLibrary(){
        
        if(System.getProperty(JACOB_DLL_LOADED_PROPERTY) != null) {
            
            /* Jacob has already been loaded, so don't load it again. */
            return;
        }
        
//        String userDir = System.getProperty("user.dir");
//        System.load(userDir+"\\external\\jacob\\jacob-1.17-M2-x86.dll");
        String jacob_x86_Name = "jacob-1.17-M2-x86";
        String jacob_x64_Name = "jacob-1.17-M2-x64";
        
        String javaArch = null;
        boolean doesContain_x64 = false, doesContain_x86 = false;
        try{
            //
            // is jacob already loaded?
            //
            
            javaArch = System.getProperty("os.arch");
//            System.out.println("javaArch = "+javaArch);
            if(javaArch != null && javaArch.contains("64")){
                System.loadLibrary(jacob_x64_Name);
            }else{
                System.loadLibrary(jacob_x86_Name);
            }

            /* Set a system property so that all class loaders know that Jacob is loaded */
            System.setProperty(JACOB_DLL_LOADED_PROPERTY, "true");

        }catch(Throwable t){
            logger.error("Caught throwable while trying to load jacob library.  The ability to control and monitor PowerPoint may not work in this instance.  The operating system architecture is '"+javaArch+"'.  Found x64 Jacob: "+doesContain_x64+".  Found x86 Jacob: "+doesContain_x86, t);
        }

    }
}
