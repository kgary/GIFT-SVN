/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;

/**
 * This class is responsible for creating the log file name.
 * 
 * @author mhoffman
 *
 */
public class ModuleLogFileAppender extends FileAppender {

        /**
         * The date format to substitute into the filename.
         */
        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        
        private static FieldPosition fp = new FieldPosition(DateFormat.YEAR_FIELD);

        /**
         * Default constructor
         */
        public ModuleLogFileAppender() {
            super();
        }

        @Override
        public void setFile(String file) {

            String val = file.trim();
            val = substituteDateTime(val);
            fileName = val;
        }

        @Override
        public void activateOptions() {
        	
            if (fileName != null) {

                try {
                    super.setFile(fileName, fileAppend, bufferedIO, bufferSize);
                    
                }catch (java.io.IOException e) {

                    errorHandler.error("setFile(" + fileName + "," + fileAppend +
                                       ") call failed.", e,
                                       ErrorCode.FILE_OPEN_FAILURE);
                }
                
            }else {
            	
                LogLog.warn("File option not set for appender [" + name + "].");
                LogLog.warn(
                      "Are you using RepositoryFileAppender instead of ConsoleAppender?");
            }
        }

        /**
         * Replace the wild card character in the filename with a date-timestamp formatted string
         * 
         * @param filename - the filename to alter
         * @return String - the filename with the wild card character replaced
         */
        private String substituteDateTime(String filename) {
        	
            int pos = filename.indexOf("*");
            if (pos == -1){
            	return filename;
            }

            StringBuffer sb = new StringBuffer();
            int newPos = pos + 1;

            sb  .append(filename.substring(0, pos));
            sdf .format(new Date(), sb, fp);
            sb  .append(filename.substring(newPos));

            return sb.toString();
        }
 
}
