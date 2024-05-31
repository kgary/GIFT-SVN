/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for loading GIFT common images to use in code.
 * 
 * @author cdettmering
 */
public class ImageUtil {

	private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);
	
	private static ImageUtil instance;
	
	private Map<String, Image> cache;
	
	private ImageUtil() {
		cache = new HashMap<String, Image>();
	}
	
	public static ImageUtil getInstance() {
		if(instance == null) {
			instance = new ImageUtil();
		}
		return instance;
	}
	
	public Image getSystemIcon() {
		return getImage(Constants.FORWARD_SLASH + ImageProperties.getInstance().getPropertyValue(ImageProperties.SYSTEM_ICON_SMALL));
	}
	
	private Image getImage(String image) {
		if(cache.get(image) == null) {
			loadImage(image);
		}
		return cache.get(image);
	}
	
	private void loadImage(String image) {
		URL imageUrl = getClass().getResource(image);
		if(imageUrl == null) {
			logger.error("Could not find " + image);
		}
		
		try {
			Image giftImage = ImageIO.read(imageUrl);
			cache.put(image, giftImage);
		} catch(IOException e) {
			logger.error("Could not read " + imageUrl, e);
		}
	}
}
