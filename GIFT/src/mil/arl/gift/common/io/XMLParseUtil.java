/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class contains helper functions for parsing xml documents.
 * 
 * @author mhoffman
 *
 */
public class XMLParseUtil {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLParseUtil.class);
    
    private static final String TIME_DELIM = ":";
    
    /** default constructor */
    private XMLParseUtil(){}
    
    /**
     * Create the Document for parsing
     * 
     * @param file the XML file to parse
     * @return Document the contents of the file as a document.
     * @throws DetailedException if there was a problem retrieving or parsing the document
     */
    public static Document parseXmlFile(FileProxy file) throws DetailedException{
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Document dom = db.parse(file.getInputStream());
           
            return dom;

        }catch(ParserConfigurationException pce) {
            logger.error("caught exception while trying to parse "+file.getFileId()+" file", pce);
            throw new DetailedException("Failed to parse the XML file '"+file.getFileId()+"'.", "There was a problem parsing the document : "+pce.getMessage(), pce);
        }catch(SAXException se) {
            logger.error("caught exception while trying to parse "+file.getFileId()+" file", se);
            //using escapeHtml to prevent parts of details being lost due to using html character
            throw new DetailedException("Found a problem with the XML file '" + file.getFileId() +  "' while trying to import. "
            		+ "You may edit the XML file outside of GIFT using a program such as Notepad++.", "There was a parse error with the document : "+ se.getMessage(), se);
        }catch(IOException ioe) {
            logger.error("caught exception while trying to parse "+file.getFileId()+" file", ioe);
            throw new DetailedException("Failed to parse the XML file '"+file.getFileId()+"'.", "There was a problem retrieving the document : "+ioe.getMessage(), ioe);
        }

    }
    
    
//    /**
//     * Return an enumeration class instance for the class name provided which has the name value.
//     * 
//     * @param className - the class name w/o the package path (e.g. ArousalLevelEnum)
//     * @param name - the name value for a class enum instance
//     * @return AbstractEnum - the enum instance which has the name value provided
//     */
//    public static AbstractEnum getEnum(String className, String name){
//        
//        try{
//            Class<? extends AbstractEnum> clazz = Class.forName("mil.arl.gift.common.enums."+className).asSubclass(AbstractEnum.class);
//            return AbstractEnum.valueOf(clazz, name);
//        }catch(Exception e){
//            logger.error("Caught exception while retrieving enum instance for class named "+className+" and name value of "+name, e);
//        }
//        
//        return null;
//    }
    
    /**
     * This method takes an xml element and the tag name, looks for the tag and gets
     * the text content 
     * 
     * @param ele - xml document element to get a child tag from
     * @param tagName - the child tag name to get its value of
     * @return String - the nodes value
     */
    public static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    
    /**
     * This method takes an xml element and the tag name, looks for the the tag and gets
     * the value which is returned as an int value
     * 
     * @param ele - xml document element to get a child tag from
     * @param tagName - the child tag name to get its value of
     * @return int - the node's value as an integer
     */
    public static int getIntValue(Element ele, String tagName) {
        return Integer.parseInt(getTextValue(ele,tagName));
    }
    
    /**
     * This method takes an xml element and the tag name, looks for the the tag and gets
     * the value which is returned as a double value
     * 
     * @param ele - xml document element to get a child tag from
     * @param tagName - the child tag name to get its value of
     * @return double - the node's value as a double
     */
    public static double getDoubleValue(Element ele, String tagName) {
        return Double.parseDouble(getTextValue(ele,tagName));
    }
    
    /**
     * This method takes an xml element and the tag name, looks for the tag and gets
     * the value which is returned as a boolean value
     * 
     * @param ele - xml document element to get a child tag from
     * @param tagName - the child tag name to get its value of
     * @return boolean - true iff the node's value reads "true" - case insensitive check.
     */
    public static boolean getBooleanValue(Element ele, String tagName){
        
        String value = getTextValue(ele, tagName);
        if("TRUE".equalsIgnoreCase(value)){
            return true;
        }
        
        return false;
    }
    
    /**
     * This method takes an xml element and the tag name, looks for the tag and gets
     * the value which is returned as a Time value.
     * Note: the xml element value must in the format of "hh:mm:ss"
     * 
     * @param ele - xml document element to get a child tag from
     * @param tagName - the child tag name to get its value of
     * @return Time - the time object for the value found in the xml element
     */
    public static Time getTime(Element ele, String tagName){
        
        String value = getTextValue(ele, tagName);
        String[] components = value.split(TIME_DELIM);
        
        Time time;
        if(components.length == 3){
            time = new Time((Integer.parseInt(components[0]) * 360 + Integer.parseInt(components[1]) * 60 + Integer.parseInt(components[2])) * 1000);
        }else{
            time = new Time(0);
        }
        
        return time;
    }
    
    
    /**
     * Return the child element with the given name
     * 
     * @param parent - the parent of the child element
     * @param name - the name of the child element to return
     * @return Element - the child element with the given name.  
     *              Note: returns null if child element is not found
     */
    public static Element getChild(Element parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            
            //node - getName will include an imported namespace (e.g. common), getLocalName will not
          if (child instanceof Element && name.equals(child.getLocalName())) {
            return (Element) child;
          }
        }
        return null;
    }
    
    /**
     * Return the children elements with the given name
     * 
     * @param parent - the parent of the children elements
     * @param name - the name of the children elements to return
     * @return List<Element> - the children elements with the given name.  
     */
    public static List<Element> getChildren(Element parent, String name) {
        List<Element> elements = new ArrayList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            
            //node - getName will include an imported namespace (e.g. common), getLocalName will not
          if (child instanceof Element && name.equals(child.getLocalName())) {
              elements.add((Element)child);
          }
        }
        return elements;
    }
}
