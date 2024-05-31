/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AvatarData;

/**
 * Contains utility methods for handling character related services.
 * 
 * @author mhoffman
 *
 */
public class CharacterServiceUtil {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(CharacterServiceUtil.class);

    /**
     * Generates character data from an character file hosted by the domain module
     *
     * Finds out the width and height of the character
     * @param runtimeCourseFolderPath the domain relative path to the course folder
     * @param characterUrl The course folder relative URL of the character to generate
     * @return AvatarData The converted character data with URL relative to the
     * domain module
     */
    public static AvatarData generateCharacter(String runtimeCourseFolderPath, String characterUrl) {

        String domainRelativeAvatarUrl = runtimeCourseFolderPath + Constants.FORWARD_SLASH + characterUrl;
        File characterHtmlFile = new File(CommonProperties.getInstance().getDomainDirectory() + runtimeCourseFolderPath + File.separator + characterUrl);

        String characterName = characterHtmlFile.getName().lastIndexOf(".html") != -1 ? characterHtmlFile.getName().substring(0, characterHtmlFile.getName().lastIndexOf('.')) : null;

        if (characterName != null) {

            File characterJsFile = new File(characterHtmlFile.getParent() + File.separator + characterName + "_Files" + File.separator + characterName + ".js");

            AvatarData parsedCharacterData = parseMsCharacterFile(characterJsFile, domainRelativeAvatarUrl);

            return parsedCharacterData;

        } else {

            throw new IllegalArgumentException("The character name could not be extracted from " + characterHtmlFile.getName());
        }
    }
    
    private static AvatarData parseMsCharacterFile(File characterFile, String url) {

        try {

            Scanner characterFileScanner = new Scanner(characterFile);

            String characterFileContents = characterFileScanner.useDelimiter("\\Z").next();

            characterFileScanner.close();

            //Pre 5.4.1 Character Builder syntax for width and height
            // width:#
            // height:#
            //
            // 5.4.1 Character Builder syntax
            // "width:"#
            // "height:"#
            boolean legacySyntax = false;

            int widthPropertyIndex = characterFileContents.indexOf("\"width\":");

            if (widthPropertyIndex == -1) {
                widthPropertyIndex = characterFileContents.indexOf("width:");

                if(widthPropertyIndex == -1){
                    throw new RuntimeException("The width property of the character could not be found.");
                }

                legacySyntax = true;
            }

            String characterWidthString = characterFileContents.substring(widthPropertyIndex + (legacySyntax ? 6 : 8), characterFileContents.indexOf(',', widthPropertyIndex));

            int characterWidth = Integer.parseInt(characterWidthString);

            int heightPropertyIndex = characterFileContents.indexOf("\"height\":");

            if (heightPropertyIndex == -1) {
                heightPropertyIndex = characterFileContents.indexOf("height:");

                if(heightPropertyIndex == -1){
                    throw new RuntimeException("The height property of the character could not be found.");
                }
            }

            // Firefox browsers do not automatically replace backslashes, resulting in invalid urls
            url = UriUtil.makeURICompliant(url);

            String characterHeightString = characterFileContents.substring(heightPropertyIndex + (legacySyntax ? 7 : 9), characterFileContents.indexOf(',', heightPropertyIndex));

            int characterHeight = Integer.parseInt(characterHeightString);

            return new AvatarData(url, characterHeight, characterWidth);

        } catch (@SuppressWarnings("unused") FileNotFoundException ex) {

            logger.warn("The character file could not be found: " + characterFile + ", defaulting to default character.");

            return null;
        }
    }
}
