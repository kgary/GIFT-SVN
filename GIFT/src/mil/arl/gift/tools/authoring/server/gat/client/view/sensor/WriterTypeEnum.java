/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

/**
 * Each enum represents a "type" of Writer that can be used in a
 * SensorsConfiguration. This is particularly useful for end users because it
 * allows them to identify a Writer using the enum's displayName rather than
 * the WriterImpl that it maps to (see SensorsConfigurationMaps).
 * @author elafave
 *
 */
public enum WriterTypeEnum {

	GENERIC_WRITER("Generic Delimited Writer"),
	KINECT_SENSOR_WRITER("Kinect Writer"),
	SCIENTIFIC_NOTATION_WRITER("Scientific Notation Delimited Writer");
	
	private final String displayName;

    private WriterTypeEnum(String displayName){
        this.displayName = displayName;
    }
    
    /**
     * 
     * @return A human-friendly string that identifies the WriterTypeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * Identifies the WriterTypEnum with the given displayName.
     * @param displayName displayName of a WriterTypeEnum
     * @return WriterTypeEnum with the given displayName, null if none of the
     * WriterTypeEnums have the given displayName.
     */
    static public WriterTypeEnum fromDisplayName(String displayName) {
    	WriterTypeEnum [] writerTypeEnums = WriterTypeEnum.values();
        for(WriterTypeEnum writerTypeEnum : writerTypeEnums) {
        	if(writerTypeEnum.getDisplayName().equals(displayName)) {
        		return writerTypeEnum;
        	}
        }
        return null;
    }

}
