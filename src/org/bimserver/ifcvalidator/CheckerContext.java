package org.bimserver.ifcvalidator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckerContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckerContext.class);
	private Properties properties;
	private String filename;
	private Path rootPath;

	public CheckerContext(String filename, Properties properties, Path rootPath) {
		this.filename = filename;
		this.properties = properties;
		this.rootPath = rootPath;
	}
	
	public String translate(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			LOGGER.info("Missing translations for key " + key + " in " + filename);
			return key;
		}
		return value;
	}
	
	public InputStream getResource(String name) throws IOException {
		Path resolve = rootPath.resolve("input").resolve(name);
		return Files.newInputStream(resolve);
	}
}