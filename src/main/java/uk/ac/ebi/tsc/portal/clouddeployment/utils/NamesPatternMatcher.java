package uk.ac.ebi.tsc.portal.clouddeployment.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author navis
 * @email <navis@ebi.ac.uk>
 *
 */

public class NamesPatternMatcher {

	private static final Logger logger = LoggerFactory.getLogger(NamesPatternMatcher.class);
	static String pattern = "^[a-zA-Z0-9]+([\\s\\.\\-\\_]?[a-zA-Z0-9]+)*";
	
	
	public static boolean nameMatchesPattern(String name){
		logger.info("Checking if input name matches pattern " + pattern);
		if(name.matches(pattern)){
			logger.info("Pattern matched");
			return true;
		}
		logger.info("Pattern not matched");
		return false;
		
	}
}
