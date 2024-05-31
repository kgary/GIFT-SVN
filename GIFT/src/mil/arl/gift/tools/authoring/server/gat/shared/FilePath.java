/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.io.Constants;

/**
 * A class representing a file path, differentiating between the file name and the prefix of the address 
 * 
 * @author cpadilla
 *
 */
public class FilePath implements IsSerializable {

    /**
     * Any prefix for the file, ie. file directory or uri
     */
    public String prefix;

    /**
     * The name of the file
     */
    public String file;
    
    /**
     * Constructor
     */
    public FilePath() {
        
    }
    
    /**
     * Constructor
     * 
     * @param prefix - any prefix for the file, ie. file directory or uri
     * @param file - the name of the file
     * @throws IllegalArgumentException - if prefix or file are null
     */
    public FilePath(String prefix, String file) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }

        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        this.prefix = prefix;
        this.file = file;
    }
    
    /**
     * Gets the full filepath of the file with the prefix + file
     * @return filePath - the file path of the file (the combined prefix + file name)
     */
    public String getFilePath() {
        return prefix + Constants.FORWARD_SLASH + file;
    }

    /**
     * Gets the file name of the FilePath
     * @return file - the name of the file
     */
    public String getFileName() {
        return file;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FilePath: prefix=");
        builder.append(prefix);
        builder.append(", file=");
        builder.append(file);
        builder.append("]");
        return builder.toString();
    }
}

