/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * A JavaScript <a href='https://developer.mozilla.org/en-US/docs/Web/API/FileList'>FileList</a> object
 * containing a series of {@link File} objects
 * 
 * @author nroberts
 */
public class FileList extends JavaScriptObject {

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected FileList() {}
    
    /**
     * Gets the list of files as a {@link List}
     * 
     * @return a list containing all the files. Will not be null but can be empty.
     */
    final public List<File> getFiles() {
        int length = getLength();
        List<File> list = new ArrayList<File>(length);
        for(int i = 0; i < length; i++) {
            list.add(item(i));
        }
        return list;
    }
    
    /**
     * Gets the list of files as a {@link JsArray}
     * 
     * @return an array containing all the files. Will not be null but can be empty.
     */
    final public JsArray<File> getFilesArray() {
        int length = getLength();
        JsArray<File> array = JavaScriptObject.createArray(length).cast();
        for(int i = 0; i < length; i++) {
            array.push(item(i));
        }
        return array;
    }
    
    /**
     * Gets the number of files in this list
     * 
     * @return the number of files
     */
    final public native int getLength()/*-{
        return this.length;
    }-*/;
    
    /**
     * Gets the {@link File} at the given index within the list
     * 
     * @param index the index of the file to get
     * @return the {@link File} at the index. May throw a JavaScriptException if the index is out of bounds.
     */
    final public native File item(int index)/*-{
        return this.item(index);
    }-*/;

}
