package org.bimserver.ifcvalidator;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
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

import org.bimserver.validationreport.IssueContainerSerializer;

public class BcfIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public BcfIfcValidatorPlugin() {
		super("BCF_ZIP_2_0", false);
	}

	@Override
	protected IssueContainerSerializer createIssueInterface(CheckerContext translator) {
		return new BcfInterface(translator);
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public String getFileName() {
		return "validationresults.bcfzip";
	}
}