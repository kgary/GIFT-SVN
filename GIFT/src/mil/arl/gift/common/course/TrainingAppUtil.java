/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import generated.course.DETestbedInteropInputs;
import generated.course.DISInteropInputs;
import generated.course.EmbeddedApps;
import generated.course.GenericLoadInteropInputs;
import generated.course.HAVENInteropInputs;
import generated.course.Interop;
import generated.course.MobileApp;
import generated.course.PowerPointInteropInputs;
import generated.course.RIDEInteropInputs;
import generated.course.SimpleExampleTAInteropInputs;
import generated.course.TC3InteropInputs;
import generated.course.UnityInteropInputs;
import generated.course.VBSInteropInputs;
import generated.course.VREngageInteropInputs;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.TrainingApplicationEnum;

/**
 * Utility used to help with logic associated with training application course objects (i.e. generated.course.TrainingApplication).
 * 
 * @author mhoffman
 *
 */
public class TrainingAppUtil {

    /**
     * Training app plugin interface classes
     */
    public static final String VBS_PLUGIN_INTERFACE = "gateway.interop.vbsplugin.VBSPluginInterface";  
    public static final String SIMPLE_EXAMPLE_TA_PLUGIN_INTERFACE = "gateway.interop.simple.SimpleExampleTAPluginInterface";
    public static final String TC3_PLUGIN_INTERFACE = "gateway.interop.tc3plugin.TC3PluginInterface";  
    public static final String PPT_INTERFACE = "gateway.interop.ppt.PPTInterface"; 
    public static final String DIS_INTERFACE = "gateway.interop.dis.DISInterface"; 
    public static final String DE_TESTBED_INTERFACE_PLUGIN_XML_RPC_INTERFACE = "gateway.interop.detestbedplugin.DETestbedPluginXMLRPCInterface";   
    public static final String DE_TESTBED_PLUGIN_DIS_INTERFACE  = "gateway.interop.detestbedplugin.DETestbedPluginDISInterface";   
    public static final String SUDOKU_TA_PLUGIN_INTERFACE = "gateway.interop.sudoku.SudokuTAPluginInterface";  
    public static final String ARES_TA_PLUGIN_INTERFACE = "gateway.interop.ares.ARESInterface";
    public static final String VR_ENGAGE_PLUGIN_INTERFACE = "gateway.interop.vrengage.VREngageInterface";
    public static final String UNITY_PLUGIN_INTERFACE = "gateway.interop.unity.UnityInterface";
    public static final String HAVEN_PLUGIN_INTERFACE = "gateway.interop.sesandboxplugin.SESandboxPluginInterface";
    public static final String RIDE_PLUGIN_INTERFACE = "gateway.interop.ride.RIDEPluginInterface";
//  public static final String TEST_TA_PLUGIN_INTERFACE = "gateway.interop.scatt.SCATTInterface";    
    
    /**
     * mapping of unique training application type to the list of gateway interop class names used
     * to communicate with that application.  
     * The value is relative to the root of the source package "mil.arl.gift", "e.g. gateway.interop.dis.DISInterface"
     */
    public static final Map<TrainingApplicationEnum, List<String>> trainingAppToInteropClassNames;
       
    /**
     * mapping of unique gateway interop class name to the interop configuration generated class that is responsible
     * for configuring that interop class.
     */
    public static final Map<String, Class<?>> interopClassNameToInputClass;
    
    static{
        
        trainingAppToInteropClassNames = new HashMap<TrainingApplicationEnum, List<String>>();
        interopClassNameToInputClass = new HashMap<String, Class<?>>();
        
        //add mappings for Simple TA's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA).add(SIMPLE_EXAMPLE_TA_PLUGIN_INTERFACE);
        
        //add mappings for Testbed's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.DE_TESTBED, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.DE_TESTBED).add(DE_TESTBED_INTERFACE_PLUGIN_XML_RPC_INTERFACE);
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.DE_TESTBED).add(DE_TESTBED_PLUGIN_DIS_INTERFACE);
        
        //add mappings for VBS's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.VBS, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.VBS).add(VBS_PLUGIN_INTERFACE);
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.VBS).add(DIS_INTERFACE);
        
        //add mappings for HAVEN's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.HAVEN, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.HAVEN).add(HAVEN_PLUGIN_INTERFACE);
        
        //add mappings for RIDE's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.RIDE, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.RIDE).add(RIDE_PLUGIN_INTERFACE);
        
        //add mappings for TC3's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.TC3, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.TC3).add(TC3_PLUGIN_INTERFACE);
        
        //add mappings for Sudoku's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.SUDOKU, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.SUDOKU).add(SUDOKU_TA_PLUGIN_INTERFACE);
        
        //add mappings for PowerPoint's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.POWERPOINT, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.POWERPOINT).add(PPT_INTERFACE);
        
        //add mappings for ARES's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.ARES, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.ARES).add(ARES_TA_PLUGIN_INTERFACE);
        
        //add mappings for VR-Engage's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.VR_ENGAGE, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.VR_ENGAGE).add(VR_ENGAGE_PLUGIN_INTERFACE);
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.VR_ENGAGE).add(DIS_INTERFACE);
        
      //add mappings for Unity's interop class names
        trainingAppToInteropClassNames.put(TrainingApplicationEnum.UNITY_DESKTOP, new ArrayList<String>());
        trainingAppToInteropClassNames.get(TrainingApplicationEnum.UNITY_DESKTOP).add(UNITY_PLUGIN_INTERFACE);
        
        //add mappings from each iterop class name to its respective interop inputs
        interopClassNameToInputClass.put(DIS_INTERFACE, DISInteropInputs.class);
        interopClassNameToInputClass.put(DE_TESTBED_PLUGIN_DIS_INTERFACE, DISInteropInputs.class);
        interopClassNameToInputClass.put(DE_TESTBED_INTERFACE_PLUGIN_XML_RPC_INTERFACE, DETestbedInteropInputs.class);      
        interopClassNameToInputClass.put(SIMPLE_EXAMPLE_TA_PLUGIN_INTERFACE, SimpleExampleTAInteropInputs.class);
        interopClassNameToInputClass.put(VBS_PLUGIN_INTERFACE, VBSInteropInputs.class);
        interopClassNameToInputClass.put(TC3_PLUGIN_INTERFACE, TC3InteropInputs.class);
        interopClassNameToInputClass.put(SUDOKU_TA_PLUGIN_INTERFACE, SimpleExampleTAInteropInputs.class);
        interopClassNameToInputClass.put(PPT_INTERFACE, PowerPointInteropInputs.class);
        interopClassNameToInputClass.put(ARES_TA_PLUGIN_INTERFACE, GenericLoadInteropInputs.class);
        interopClassNameToInputClass.put(VR_ENGAGE_PLUGIN_INTERFACE, VREngageInteropInputs.class);
        interopClassNameToInputClass.put(UNITY_PLUGIN_INTERFACE, UnityInteropInputs.class);
        interopClassNameToInputClass.put(HAVEN_PLUGIN_INTERFACE, HAVENInteropInputs.class);
        interopClassNameToInputClass.put(RIDE_PLUGIN_INTERFACE, RIDEInteropInputs.class);
        
        //Example of another widget that is available for use:
        //interopClassNameToInputClass.put(TEST_TA_PLUGIN_INTERFACE, generated.course.CustomInteropInputs.class);
    }
    
    /**
     * Return the training application type the provided interop class can be used to communicate with.
     * 
     * @param interopClassName the name of a gateway interop plugin class (e.g. "gateway.interop.dis.DISInterface")
     * @return the first training application found to use the interop plugin
     */
    public static TrainingApplicationEnum getTrainingAppTypes(String interopClassName){
        
        for(TrainingApplicationEnum trainingAppEnum : trainingAppToInteropClassNames.keySet()){
            
            List<String> classes = trainingAppToInteropClassNames.get(trainingAppEnum);
            for(String className : classes){
                
                if(interopClassName.endsWith(className)){
                    //found
                    return trainingAppEnum;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Return the training application type based on the interop plugin implementations or embedded
     * applications defined in the training application object provided.
     * 
     * @param trainingApplication the object to find interop plugin implementation class references
     *        or embedded applications to use as the criteria for determining the training
     *        application type the object is communication with
     * @return the training application type the object will be communicating with. Will be null if
     *         the training application type couldn't be determined.
     */
    public static TrainingApplicationEnum getTrainingAppType(generated.course.TrainingApplication trainingApplication){
        
        if(trainingApplication == null) {
            return null;
        }
        
        TrainingApplicationEnum appType = null;
        if(trainingApplication.getTrainingAppTypeEnum() != null){
            try{
                appType = TrainingApplicationEnum.valueOf(trainingApplication.getTrainingAppTypeEnum());
                return appType;
            }catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
                // value wasn't specified correctly in the training app course object,
                // check the old way by looking at the interops
            }
        }
        
        generated.course.Interops appInterops = trainingApplication.getInterops();
        if (appInterops == null || appInterops.getInterop() == null || appInterops.getInterop().isEmpty()) {
            // check for embedded training apps
            if (trainingApplication.getEmbeddedApps() != null) {
                EmbeddedApps apps = trainingApplication.getEmbeddedApps();
                if (apps.getEmbeddedApp() != null && apps.getEmbeddedApp().getEmbeddedAppImpl() instanceof MobileApp) {
                    appType = TrainingApplicationEnum.MOBILE_DEVICE_EVENTS;
                } else {
                    appType = TrainingApplicationEnum.UNITY_EMBEDDED;
                }
            }
        } else {
            List<String> candidateInteropImpls = new ArrayList<String>();
            List<Interop> interops = appInterops.getInterop();
            for (Interop interop : interops) {

                if (interop.getInteropImpl() != null) {
                    candidateInteropImpls.add(interop.getInteropImpl());
                }
            }

            appType = getTrainingAppTypeByInterops(candidateInteropImpls);
            if(appType == null){
                //check by interop inputs next
                
                for(Interop interop : interops){
                    
                    if(interop.getInteropInputs() != null && interop.getInteropInputs() != null){
                        
                        if(interop.getInteropInputs().getInteropInput() instanceof SimpleExampleTAInteropInputs){
                            
                            boolean isSudoku = interop.getInteropImpl() != null && interop.getInteropImpl().equals(SUDOKU_TA_PLUGIN_INTERFACE);
                            
                            if(isSudoku){                           
                                appType = TrainingApplicationEnum.SUDOKU;
                                break;
                                
                            } else {
                                appType = TrainingApplicationEnum.SIMPLE_EXAMPLE_TA;
                                break;
                            }                                               
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof DETestbedInteropInputs){
                            
                            appType = TrainingApplicationEnum.DE_TESTBED;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof VBSInteropInputs){
                            
                            appType = TrainingApplicationEnum.VBS;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof generated.course.HAVENInteropInputs) {
                            
                            appType = TrainingApplicationEnum.HAVEN;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof generated.course.RIDEInteropInputs) {
                            
                            appType = TrainingApplicationEnum.RIDE;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof TC3InteropInputs){
                            
                            appType = TrainingApplicationEnum.TC3;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof PowerPointInteropInputs){
                            
                            appType = TrainingApplicationEnum.POWERPOINT;
                            break;
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof VREngageInteropInputs){
                            
                            appType = TrainingApplicationEnum.VR_ENGAGE;
                            break;
                        
                        } else if(interop.getInteropInputs().getInteropInput() instanceof UnityInteropInputs){
                            
                            appType = TrainingApplicationEnum.UNITY_DESKTOP;
                            break;
                        }
                    }
                }//end for
            }            
        }
        
        if(appType != null){
            // make sure the provided training app object has the optional value set
            trainingApplication.setTrainingAppTypeEnum(appType.getName());
        }
        return appType;
    }
    
    /** 
     * Return the training application type based on the interop plugin implementation classes provided.
     * 
     * @param candidateInteropImpls the interop plugin implementation class references to use as the criteria
     * for determining the training application type the object is communication with
     * @return the training application type the collection of interop plugin classes will be communicating with when used
     * together.  Will be null if the training application type couldn't be determined.
     */
    public static TrainingApplicationEnum getTrainingAppTypeByInterops(List<String> candidateInteropImpls){
        
        TrainingApplicationEnum appType = null;
        for(TrainingApplicationEnum trainingAppEnum : trainingAppToInteropClassNames.keySet()){
            
            List<String> classNames = trainingAppToInteropClassNames.get(trainingAppEnum);
            
            if(classNames == null){
                continue;
            }
            
            //check for 100% match
            boolean match = true;
            for(String interopClassName : candidateInteropImpls){
                
                if(!classNames.contains(interopClassName)){
                    match = false;
                    break;
                }
            }
            
            if(match){
                appType = trainingAppEnum;
                break;
            }
        }
        
        return appType;
    }
}
