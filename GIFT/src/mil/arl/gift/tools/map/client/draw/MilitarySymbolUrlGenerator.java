/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;

import java.util.ArrayList;

import mil.arl.gift.tools.map.shared.SIDC;

/**
 * A generator that is used to generate the URLs needed to render {@link MilitarySymbol}s to a map using a hosted
 * rendering service.
 * 
 * @author nroberts
 */
public class MilitarySymbolUrlGenerator {

    /** The URL parameter used to define the color of the background behind any text displayed by a symbol */
    private static final String TEXT_BACKGROUND_COLOR_PARAM = "TEXTBACKGROUNDCOLOR=";
    
    /** The URL parameter used to define the color of any text displayed by a symbol */
    private static final String TEXT_COLOR_PARAM = "TEXTCOLOR=";
    
    /** The URL parameter used to define the color of the exterior line of a symbol */
    private static final String LINE_COLOR_PARAM = "LINECOLOR=";
    
    /** The URL parameter used to define the color used to fill a symbol */
    private static final String FILL_COLOR_PARAM = "FILLCOLOR=";
    
    /** The URL parameter used to define the unique designation to be displayed by a symbol */
    private static final String UNIQUE_DESIGNATION_PARAM = "T=";
    
    /** The URL parameter used to define the size of the unmodified icon used by the symbol */
    private static final String SIZE_PARAM = "SIZE=";
    
    /** The URL of used to reach the military symbol rendering service */
    private String milSymServiceUrl;
    
    /**
     * Creates a new generator that uses the military symbol rendering service hosted at the given location 
     * to generate the URLs needed to render {@link MilitarySymbol}s to a map.
     * 
     * @param milSymServletUrl the URL of the military symbol rendering service. Cannot be null.
     */
    public MilitarySymbolUrlGenerator(String milSymServiceUrl) {
        
        if(milSymServiceUrl == null) {
            throw new IllegalArgumentException("The URL to the military symbol rendering service cannot be null");
        }
        
        this.milSymServiceUrl = milSymServiceUrl;
    }
    
    /**
     * Return the URL of the service providing military symbols (e.g. 2525C)
     * @return the URL, won't be null.
     */
    public String getMilitarySymbolServiceURL(){
        return milSymServiceUrl;
    }

    
    /**
     * Generates the URL needed to render the given {@link MilitarySymbol} using the military symbol rendering service
     * specified by this generator.
     * 
     * @param symbol the symbol that a rendering URL is needed for
     * @param sidc the symbol identification code specifying which icon to retrieve.
     * @return a URL that can be used to render the provided symbol using this generator's specified rendering service
     */
    public String getSymbolUrl(MilitarySymbol symbol, SIDC sidc) {
        
        if(symbol == null) {
            throw new IllegalArgumentException("The military symbol to obtain a URL for cannot be null");
        }
        
        /*
         * Generate the URL needed to render this symbol using the provided military symbol rendering service.
         * 
         * NOTE: The below code assumes that the military symbol rendering service is a hosted instance of 
         * the 'mil-sym-java' libraries (see https://github.com/missioncommand/mil-sym-java) and uses the 
         * appropriate URL structure expected by said libraries. If the military symbol rendering service
         * is changed to use a different implementation in the future, then this code will need to
         * be changed accordingly.
         */
        sidc.setStandardIdentity(symbol.getAffiliation().getCode());
        sidc.setStatus(symbol.getStatus().getCode());
        
        StringBuilder sb = new StringBuilder()
                .append(milSymServiceUrl)
                .append("/renderer/image/")
                .append(sidc);
        
        ArrayList<String> urlParams = new ArrayList<>();
        
        if(symbol.getUniqueDesignation() != null) {
            urlParams.add(UNIQUE_DESIGNATION_PARAM + symbol.getUniqueDesignation());
        }
        
        if(symbol.getFillColor() != null) {
            urlParams.add(FILL_COLOR_PARAM + symbol.getFillColor());
        }
        
        if(symbol.getLineColor() != null) {
            urlParams.add(LINE_COLOR_PARAM + symbol.getLineColor());
        }
        
        if(symbol.getTextColor() != null) {
            urlParams.add(TEXT_COLOR_PARAM + symbol.getTextColor());
        }
        
        if(symbol.getTextBackgroundColor() != null) {
            urlParams.add(TEXT_BACKGROUND_COLOR_PARAM + symbol.getTextBackgroundColor());
        }
        
        if(symbol.getSize() != null) {
            urlParams.add(SIZE_PARAM + symbol.getSize());
        }
        
        if(!urlParams.isEmpty()) {
            
            sb.append("?");
            
            boolean isFirstParam = true;
            for(String param : urlParams) {
                
                if(isFirstParam) {
                    isFirstParam = false;
                    
                } else {
                    sb.append("&");
                }
                
                sb.append(param);
            }
        }
        
        return sb.toString();
    }
}
