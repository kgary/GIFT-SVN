package mil.arl.gift.lms.impl.lrs.xapi;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiUUIDException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;

/**
 * Utility class for creating a v2 UUID from an ordered collection of Strings.
 * 
 * @author Yet Analytics
 *
 */
public class UUIDHelper {
    
    /**
     * Creates UUID from ordered collection of strings
     * 
     * @param components - strings to be joined to form UUID basis
     * 
     * @return V3 UUID
     * 
     * @throws LmsXapiUUIDException when unable to create V3 UUID
     */
    public static UUID createUUIDFromData(String... components) throws LmsXapiUUIDException {
        if(components == null || components.length == 0) {
            throw new LmsXapiUUIDException("Unable to create seed from null components!");
        }
        List<String> comp = Arrays.asList(components);
        String seed = StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), comp);
        UUID id;
        try {
            id = UUID.nameUUIDFromBytes(seed.getBytes(CommonLrsEnum.ENCODING.getValue()));
        } catch (UnsupportedEncodingException e) {
            throw new LmsXapiUUIDException("Unable to encode seed into UUID!", e);
        }
        return id;
    }
}
