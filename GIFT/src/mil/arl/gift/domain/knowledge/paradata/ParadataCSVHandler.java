/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.paradata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PassedEnum;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PhaseEnum;

/**
 * Manages reading and writing a paradata CSV file.
 * 
 * @author mhoffman
 *
 */
public class ParadataCSVHandler {
    
    /** used for creating temp files when dealing with paradata files located in content management systems such as Nuxeo */
    protected static final File DOMAIN_DIRECTORY = new File(DomainModuleProperties.getInstance().getDomainDirectory());
    
    /** used to jump start creating a new paradata csv file, contains just the header row */
    private static final File TEMPLATE_PARADATA = new File(DomainModuleProperties.getInstance().getDomainDirectory() + File.separator + 
            "workspace" +  File.separator + "templates" + File.separator + "template.paradata");
    
    // TODO: use reflection on ParadataBean.java
    /** column header for csv file */
    private static final String[] header = new String[] { "date","phase","duration","passed","state"};
    
    /** the paradata CSV file to read/write */
    private FileProxy paradataFileProxy;
    
    /** parent folder to the paradata CSV file */
    private AbstractFolderProxy courseAuthoredFolder;
    
    /** current collection of paradata objects for the CSV file */
    private List<ParadataBean> paradataBeans;
    
    /**
     * Used for converting lines in the CSV file to objects in ParadataBean.
     */
    final CellProcessor[] readProcessors = new CellProcessor[] { 
            new ParseLong(), // date epoch
            new CellProcessor() {
                
                @Override
                public Object execute(Object value, CSVContext context) {
                    return PhaseEnum.valueOf((String) value);
                }
            }, // phase enum
            new ParseInt(), // duration seconds
            new CellProcessor(){
                
                @Override
                public Object execute(Object value, CSVContext context) {
                    return PassedEnum.valueOf((String) value);
                }
            }, // passed enum
            new NotNull() // learner state enum
    };
    
    /**
     * Used for converting objects in ParadataBean into lines in the CSV file
     */
    final CellProcessor[] writeProcessors = new CellProcessor[] { 
            new ParseLong(), // date epoch
            new CellProcessor() {
                
                @Override
                public Object execute(Object value, CSVContext context) {
                    return ((PhaseEnum)value).name();
                }
            }, // phase enum
            new ParseInt(), // duration seconds
            new CellProcessor(){
                
                @Override
                public Object execute(Object value, CSVContext context) {
                    return ((PassedEnum)value).name();
                }
            }, // passed enum
            new NotNull() // learner state
    };

    /**
     * Set attributes and read the paradata CSV file.
     * 
     * @param paradataFileProxy the paradata csv file that will be read.
     * @param courseFolder where the authored course resides, needed to update persistent paradata files
     * @throws Exception when there is a problem reading the paradata csv file.
     */
    public ParadataCSVHandler(FileProxy paradataFileProxy, AbstractFolderProxy courseAuthoredFolder) throws Exception{
        
        this.paradataFileProxy = paradataFileProxy;
        this.courseAuthoredFolder = courseAuthoredFolder;
        
        // TODO: probably not do this anymore, require caller to initiate read
        read();
    }
    
    /**
     * Create a new paradata csv file using the template file.
     * 
     * @param paradataFileName the name of the paradata file with a path relative to the course folder.
     * @param courseFolder the authored course folder, used to persistently store the paradata files
     * @return the proxy to the new paradata file
     * @throws IOException if there was a problem creating the file
     */
    public static FileProxy createParadataCSVFile(String paradataFileName, AbstractFolderProxy courseFolder) throws IOException{
        return courseFolder.createFile(TEMPLATE_PARADATA, paradataFileName);
    }
    
    /**
     * Add a new paradata object to the list read in from the paradata CSV file
     * 
     * @param paradataBean the new paradata object to add.  If null, method does nothing.
     */
    public void add(ParadataBean paradataBean){    
        
        if(paradataBean == null){
            return;
        }
        paradataBeans.add(paradataBean);
    }
    
    /**
     * Return the paradata objects currently being managed in memory for the paradata CSV file.
     * 
     * @return collection of paradata objects for the paradata CSV file
     */
    public List<ParadataBean> getData(){
        return paradataBeans;
    }
    
    /**
     * Return the next unique id to use for a new row in the paradata csv file.
     * @return the next unique id
     */
    public int getNextId(){
        return paradataBeans.isEmpty() ? 1 : paradataBeans.size()+1;
    }
    
    /**
     * Read in the CSV lines from the file. The file must exist.
     * Closes the reader.
     * @throws Exception when there is a problem reading the paradata csv file
     */
    private void read() throws Exception{

        CsvBeanReader beanReader = null;
        try {
            paradataFileProxy.clearStoredFileContents();
            InputStreamReader inputStreamReader = new InputStreamReader(paradataFileProxy.getInputStream());
            beanReader = new CsvBeanReader(inputStreamReader, CsvPreference.STANDARD_PREFERENCE);
            
            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getCSVHeader(true);
            
            paradataBeans = new ArrayList<>();
            ParadataBean paradata;
            while( (paradata = beanReader.read(ParadataBean.class, header, readProcessors)) != null ) {
                paradata.setId(getNextId());
                paradataBeans.add(paradata);
//                    System.out.println(String.format("lineNo=%s, rowNo=%s, paradata=%s", beanReader.getLineNumber(),
//                            beanReader.getLineNumber(), paradata));
            }

        } finally{
            
            if(beanReader != null){
                beanReader.close();                    
            }
        }
    }
    
    /**
     * Write the current paradata objects to the paradata CSV file, over-writing the existing contents.
     * Closes the writer.
     * @throws IOException 
     */
    public void write() throws IOException{
        
        ICsvBeanWriter beanWriter = null;
        try {
            File tempFile = null;
            Writer writer = null;
            
            if(!paradataFileProxy.isLocalFile()){
                tempFile = new File(DOMAIN_DIRECTORY.getAbsolutePath() + File.separator + paradataFileProxy.getName());   
                writer = new FileWriter(tempFile);
            }else{
                File file = new File(paradataFileProxy.getFileId());
                writer = new FileWriter(file);
            }
            
            beanWriter = new CsvBeanWriter(writer,
                    CsvPreference.STANDARD_PREFERENCE);
            
            // write the header
            beanWriter.writeHeader(header);
            
            // write the beans
            for( final ParadataBean paradataBean : paradataBeans ) {
                beanWriter.write(paradataBean, header, writeProcessors);
            }
            
            if(tempFile != null){
                // write the new file to the authored course folder (in Nuxeo)
                courseAuthoredFolder.updateFileContents(paradataFileProxy, tempFile, false, true);
            }
                
        } finally {
            if(beanWriter != null) {
                beanWriter.close();
            }
        }
    }

}
