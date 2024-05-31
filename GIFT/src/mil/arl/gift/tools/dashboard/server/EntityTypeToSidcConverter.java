/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * A singleton class that is responsible for translating a DIS type tuple into a
 * corresponding SIDC identifier.
 *
 * @author tflowers
 *
 */
public class EntityTypeToSidcConverter {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(EntityTypeToSidcConverter.class);

    /** The singleton instance of this class */
    private static WeakReference<EntityTypeToSidcConverter> instance = null;

    /** The map used to lookup a SIDC for a given DIS type tuple */
    private final Map<String, String> disToSidcLookup = new HashMap<>();

    /**
     * The private constructor that initializes the
     */
    private EntityTypeToSidcConverter() {
        String mappingFilePath = DashboardProperties.getInstance().getDisToSidcMappingFilePath();
        File mappingFile = new File(mappingFilePath);
        try {
            /* Load the file content */
            Object fileContent = new Yaml().load(new FileInputStream(mappingFile));

            if (fileContent instanceof Map<?, ?>) {
                Map<?, ?> fileContentMap = (Map<?, ?>) fileContent;
                extractMappings(fileContentMap);
            } else {
                final String fileContentClass = fileContent != null ? fileContent.getClass().getName() : "null";
                final String msg = String.format("Expected the YAML document to be a Map but instead it is: %s", fileContentClass);
                logger.error(msg);
            }
        } catch (FileNotFoundException e) {
            final String msg = String.format("The file %s was not found", mappingFile.getAbsolutePath());
            logger.error(msg, e);
        }
    }

    /**
     * Performs all the necessary type checks on a Map that was extracted from
     * the configuration file.
     *
     * @param fileContentMap The raw map that was pulled from the configuration
     *        file on which all type checks and extractions should be performed.
     */
    private void extractMappings(Map<?, ?> fileContentMap) {
        if (fileContentMap == null) {
            throw new IllegalArgumentException("The parameter 'fileContentMap' cannot be null.");
        }

        for (Object rawKey : fileContentMap.keySet()) {

            /* Ensure that the key for the map is a DIS String */
            if (rawKey instanceof String) {
                String disString = (String) rawKey;
                Object rawValue = fileContentMap.get(disString);

                /* Ensure that the DIS String is mapped to the expected type, a
                 * map of parameters to values. */
                if (rawValue instanceof Map<?, ?>) {
                    final Map<?, ?> valueMap = (Map<?, ?>) rawValue;
                    final Object rawSidcValue = valueMap.get("sidc");

                    /* Ensure the SIDC value is the expected type, String */
                    if (rawSidcValue instanceof String) {
                        String sidc = (String) rawSidcValue;
                        disToSidcLookup.put(disString, sidc);
                    } else {
                        String sidcValueClass = rawSidcValue != null ? rawSidcValue.getClass().getName() : "null";
                        final String msg = String.format(
                                "The expected type of the 'sidc' property for '%s' should be String but instead it is: %s",
                                disString, sidcValueClass);
                        logger.error(msg);
                    }
                } else {
                    final String rawValueClass = rawValue != null ? rawValue.getClass().getName() : "null";
                    final String msg = String.format(
                            "Expected a map of properties for '%s' but instead found: %s", disString,
                            rawValueClass);
                    logger.error(msg);
                }
            } else {
                final String rawKeyClass = rawKey != null ? rawKey.getClass().getName() : "null";
                final String msg = String.format(
                        "Expected a DIS String as the root entry for the YAML file but instead it is: %s",
                        rawKeyClass);
                logger.error(msg);
            }
        }
    }

    /**
     * Getter for the singleton instance of the
     * {@link EntityTypeToSidcConverter} class. <b>NOTE:</b> the result of this
     * method should be saved for as long as it is needed by the caller since
     * the singleton object is automatically garbage collected when it is not
     * being used by any outside classes. This means that every call to
     * {@link #getInstance()} potentially has to instantiate the singleton which
     * is likely an expensive operation.
     *
     * @return The singleton instance of this class.
     */
    public synchronized static EntityTypeToSidcConverter getInstance() {
        EntityTypeToSidcConverter toRet;
        if (instance == null) {
            toRet = new EntityTypeToSidcConverter();
            instance = new WeakReference<>(toRet);
        } else {
            toRet = instance.get();
            if (toRet == null) {
                toRet = new EntityTypeToSidcConverter();
                instance = new WeakReference<>(toRet);
            }
        }

        return toRet;
    }

    /**
     * Returns a SIDC string for a given {@link EntityType}. If a SIDC is not
     * explicitly defined for an {@link EntityType} then the next closest entity
     * type is tried (examples below).
     *
     * <h1>Tuple Notation</h1>
     *
     * The {@link EntityType} internally is translated into a tuple notation
     * that facilitates the fall-back lookup behavior. Below is a template for
     * the notation.
     *
     * <pre>
     * {KIND}.{DOMAIN}.{COUNTRY}.{CATEGORY}.{SUBCATEGORY}.{SPECIFIC}.{EXTRA}
     * </pre>
     *
     * <h1>Example Fallback</h1>
     *
     * If the EntityType '1.2.3.4.5.6.7' is requested, then the key and
     * fall-backs would be queried in the following order.
     * <ol>
     * <li>1.2.3.4.5.6.7</li>
     * <li>1.2.3.4.5.6.0</li>
     * <li>1.2.3.4.5.0.0</li>
     * <li>1.2.3.4.0.0.0</li>
     * <li>1.2.0.0.0.0.0</li>
     * <li>1.0.0.0.0.0.0</li>
     * <li>0.0.0.0.0.0.0</li>
     * </ol>
     *
     * @param type The {@link EntityType} for which to find a SIDC. Can't be
     *        null.
     * @return The SIDC {@link String}. Can be null if no corresponding string
     *         was found.
     */
    public SIDC getSidc(EntityType type) {

        /* Constructs a list from each element to make it easier to generate
         * fallback keys to lookup in the map */
        int entityKind = type.getEntityKind();
        int domain = type.getDomain();
        int country = type.getCountry();
        int category = type.getCategory();
        int subcategory = type.getSubcategory();
        int specific = type.getSpecific();
        int extra = type.getExtra();

        List<Integer> composite = Arrays.asList(entityKind, domain, country, category, subcategory, specific, extra);

        /* Try the current key and if it doesn't exist, try the next key in the
         * fall-back chain. This will continue until a default 'all-0' key. This
         * loop can execute size + 1 times (convert each step to 0 + test all
         * 0s). */
        for (int i = composite.size(); i >= 0; i--) {
            String compositeStr = StringUtils.join(".", composite);

            boolean hasKey = disToSidcLookup.containsKey(compositeStr);
            if(!hasKey && country != 0) {

                //if no key matches the current key's country, try setting country to 0, like ARES does
                List<Integer> newComposite = new ArrayList<>(composite);
                newComposite.set(2, 0);
                compositeStr = StringUtils.join(".", newComposite);
                hasKey = disToSidcLookup.containsKey(compositeStr);
            }

            if (hasKey) {

                SIDC sidc = new SIDC(disToSidcLookup.get(compositeStr));
                if(type.getEchelon() != null) {

                    //modify the SIDC to show an indicator of the entity type's echelon
                    sidc.setEchelon(type.getEchelon());
                }

                return sidc;

            } else if (i > 0) {
                /* Set the next higher tier to 0 */
                composite.set(i - 1, 0);
            }
        }

        throw new UnsupportedOperationException(
                "A default SIDC should have been found for " + StringUtils.join(".", composite) + ", but it wasn't.");
    }
}
