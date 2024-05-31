/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gwt.http.client.URL;

import mil.arl.gift.common.util.StringUtils;

/**
 * The PlaceParamParser class is responsible for encoding Place urls using a generic
 * name/value mapping of params.
 *
 * The format of the url for the Place would look like the following:
 *
 * /#DkfPlace:filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop
 *  or
 *  {@literal <placetag>:<param1>=<value1>|<param2>=<value2>|<param3>=<value3> }
 *
 *  The tokens are separated by the "|" (pipe character) and the parameter name/value are separated by
 *  the "=" (equal) sign.
 *
 *  Note that empty/null values are not supported. If a null value is passed in, the
 *  parameter will not be included in the url token.
 *
 * @author nblomberg
 *
 */

public class PlaceParamParser {
    /** Instance of the logger */
    private static Logger logger = Logger.getLogger(PlaceParamParser.class.getName());

    /**
     * Separator character that is used to separate each name/value pair 
     * must match {@link #mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget.TOKEN_SEPARATOR} 
     **/
    private static final String TOKEN_SEPARATOR = "|";
    /**
     * Separator character that is used to separate a single name/value pair
     * must match {@link #mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget.PARAM_SEPARATOR} 
     **/
    private static final String PARAM_SEPARATOR = "=";

    /**
     * Method used to parse a url string and return a map of parameters based on that string.
     * The string token is expected to be in the format:
     *
     * /#DkfPlace:filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop
     *  or
     *  {@literal <placetag>:<param1>=<value1>|<param2>=<value2>|<param3>=<value3> }
     *
     *   *  The tokens are separated by the "|" (pipe character) and the parameter name/value are separated by
     *  the "=" (equal) sign.
     *
     *  Note that empty/null values are not supported. If a null value is passed in, the
     *  parameter will not be included in the url token.
     *
     *  Can also take URL encodings as well, e.g. CoursePlace:filePath=mhoffman/4459/4459.course.xml%7CdeployMode=Desktop
     *
     * @param token - The url token that will be parsed.  Should be in a format like:
     *                "filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop"
     * @param paramMap - The mapping of name/value pairs that will be returned based on the url token.  Can return an empty list.
     * @throws ParseException - If any parse error occurs, a {@link ParseException} will be thrown.
     */
    static public void parseTokenParameters(String token, HashMap<String, String> paramMap) throws ParseException  {

        String decodedToken = URL.decode(token);

        // Need to escape the pipe symbol here.
        String[] tokens = decodedToken.split("\\" + TOKEN_SEPARATOR);

        if (tokens.length > 0) {
            for (int x=0; x < tokens.length; x++) {

                String[] params = tokens[x].split(PARAM_SEPARATOR);

                if (params.length == 2) {
                    logger.info("setting map param of "+params[0]+" : "+params[1]);
                    paramMap.put(URL.decodeQueryString(params[0]),  URL.decodeQueryString(params[1]));
                } else {
                    throw new ParseException("Expected two parameters, but encountered " + params.length + ".  Exception occurred on token: " + tokens[x], 0);
                }

            }
        } else {
            throw new ParseException("Unexpected token length of : " + tokens.length, 0);
        }

    }

    /**
     * Takes a mapping of parameters and encodes a url token that will be used in the place url.
     * The url that will be encoded will look like the following:
     *
     * /#DkfPlace:filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop
     *  or
     *  {@literal <placetag>:<param1>=<value1>|<param2>=<value2>|<param3>=<value3> }
     *
     *   *  The tokens are separated by the "|" (pipe character) and the parameter name/value are separated by
     *  the "=" (equal) sign.
     *
     *  Note that empty/null values are not supported. If a null value is passed in, the
     *  parameter will not be included in the url token.
     *
     * @param paramMap - A mapping of parameters (name/value pairs) that will be encoded to a url string.
     *
     * @return  String - url token that will be returned from the parameter map. If there are no parameters, an empty string is returned.
     *                   The string that will be returned will be in a format like:  "filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop"
     */
    static public String encodeTokenParameters(HashMap<String, String> paramMap) {

        StringBuffer sb = new StringBuffer("");

        Iterator<Entry<String, String>> iter = paramMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> params = iter.next();

            /* Do not encode empty or null parameters. The following (for
             * example) is not supported: "filePath=|deployMode=Desktop" where
             * where filePath has no value. In this example, the url would be:
             * "deployMode=Desktop" and filePath would not be included on the
             * url. */
            final String paramKey = params.getKey();
            final String paramValue = params.getValue();
            if (StringUtils.isNotBlank(paramKey) && StringUtils.isNotBlank(paramValue)) {
                final String queryParamKey = URL.encodeQueryString(paramKey);
                final String queryParamValue = URL.encodeQueryString(paramValue);
                sb.append(queryParamKey).append(PARAM_SEPARATOR).append(queryParamValue);

                if (iter.hasNext()) {
                    sb.append(TOKEN_SEPARATOR);
                }
            }
        }

        return sb.toString();
    }


    /**
     * Utility method used to get the parameters based on a url token.
     * It is a wrapper function for {@link PlaceParamParser#parseTokenParameters(String, HashMap)}.
     * The difference is that this function wraps the exception handling and if there is any
     * exception encountered, an empty mapping is returned.
     *
     * The string token is expected to be in the format:
     *
     * /#DkfPlace:filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop
     *  or
     *  {@literal <placetag>:<param1>=<value1>|<param2>=<value2>|<param3>=<value3> }
     *
     *   *  The tokens are separated by the "|" (pipe character) and the parameter name/value are separated by
     *  the "=" (equal) sign.
     *
     *  Note that empty/null values are not supported. If a null value is passed in, the
     *  parameter will not be included in the url token.
     *
     *  Can also take URL encodings as well, e.g. CoursePlace:filePath=mhoffman/4459/4459.course.xml%7CdeployMode=Desktop
     *
     * @param token - The url token that will be parsed.  Should be in a format like:
     *                "filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop"
     * @return HashMap<String, String> - The mapping of name/value pairs that will be returned based on the url token.  Can return an empty list.
     */
    static public HashMap<String, String> getParams(String token) {

        String decodedToken = URL.decode(token);
        logger.info("Parsing for params\n"+decodedToken+"\n"+token);

        HashMap<String, String> params = new HashMap<>();
        try {
            PlaceParamParser.parseTokenParameters(decodedToken,  params);
        } catch (ParseException e) {
            logger.severe("Unable to parse place token of: " + decodedToken + ".  Error is: " +  e.getMessage());
        }

        logger.info("MAP is : "+params);

        return params;
    }
}
