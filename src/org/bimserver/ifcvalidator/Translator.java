package org.bimserver.ifcvalidator;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Translator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);
	private Properties properties;

	public Translator(Properties properties) {
		this.properties = properties;
	}
	
	public String translate(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			LOGGER.info("Missing translations for key " + key);
			return key;
		}
		return value;
	}
}
