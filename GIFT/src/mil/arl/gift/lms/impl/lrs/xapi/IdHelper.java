package mil.arl.gift.lms.impl.lrs.xapi;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;

/**
 * This class handles creation of xAPI identifiers from data. 
 * 
 * @author Yet Analytics
 *
 */
public class IdHelper {
    
    /**
     * Encodes input using UTF-8
     * 
     * @param s - String to URL encode
     * 
     * @return encoded string
     * 
     * @throws LmsXapiActivityException when unable to encode s
     */
    public static String encodeSlug(String s) throws LmsXapiActivityException {
        if(s == null || StringUtils.isBlank(s)) {
            throw new LmsXapiActivityException("null or empty string will not be URL encoded!");
        }
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(s, CommonLrsEnum.ENCODING.getValue());
        } catch (UnsupportedEncodingException e) {
            throw new LmsXapiActivityException("Unable to URL encode arg: " + s, e);
        }
        return encoded;
    }
    
    /**
     * Creates the activity id string from name
     * 
     * @param name - string to encode and set as distinct part of activity id
     * 
     * @return the activity id string
     * 
     * @throws LmsXapiActivityException when unable to create the activity id
     */
    public static String createActivityId(String name) throws LmsXapiActivityException {
        return CommonLrsEnum.ACTIVITY_PREFIX.getValue() + encodeSlug(name);
    }
    
    /**
     * Creates the activity id from encoding all paths and joining with separator '/'
     * 
     * @param paths - n ordered strings to encode
     * 
     * @return - the activity id string
     * 
     * @throws LmsXapiActivityException when unable to encode one of the paths
     */
    public static String createActivityId(String... paths) throws LmsXapiActivityException {
        if(paths == null || paths.length == 0) {
            throw new LmsXapiActivityException("Activity Id paths must not be null or empty!");
        }
        List<String> formatted = new ArrayList<String>();
        for(String path : paths) {
            formatted.add(encodeSlug(path));
        }
        return CommonLrsEnum.ACTIVITY_PREFIX.getValue() + StringUtils.join(CommonLrsEnum.SEPERATOR_SLASH.getValue(), formatted);
    }
    
    /**
     * Helper method for conversion from String to URI
     * 
     * @param uri - String to create URI from
     * 
     * @return URI created from String
     * 
     * @throws URISyntaxException when unable to create URI from String
     */
    public static URI toURI(String uri) throws URISyntaxException {
        return new URI(uri);
    }
}
