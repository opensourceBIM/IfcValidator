package org.bimserver.ifcvalidator;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

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
	private String author;

	public CheckerContext(String filename, Properties properties, Path rootPath, String author) {
		this.filename = filename;
		this.properties = properties;
		this.rootPath = rootPath;
		this.author = author;
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

	public String getAuthor() {
		return author;
	}
}