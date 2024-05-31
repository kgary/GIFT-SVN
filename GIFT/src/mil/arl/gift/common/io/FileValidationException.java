/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * This exception is used for file validation errors.
 * 
 * @author mhoffman
 *
 */
public class FileValidationException extends DetailedException {

    private static final long serialVersionUID = 1L;
    
    /** the file name of the file being validated */
    private String filename;
    
    /**
     * No-arg constructor needed by GWT RPC. This constructor does not create a valid instance of this class and should not be used 
     * under most circumstances
     */
    public FileValidationException() {
        super();
    }
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param filename the file name of the file being validated that caused the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public FileValidationException(String reason, String details, String filename, Throwable cause){
        super(reason, details, cause);
        
        if(filename == null || filename.isEmpty()){
            throw new IllegalArgumentException("The file name can't be null.");
        }
        
        this.filename = filename;
    }    
    
    /**
     * Return the file name that caused the exception.
     * 
     * @return won't be null
     */
    public String getFileName(){
        return filename;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[FileValidationException: ");
        sb.append("\nfile = ").append(getFileName());
        sb.append("\ncause = ").append(super.toString());
        sb.append("\n]");
        return sb.toString();
    }

}
