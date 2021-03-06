package org.bimserver.ifcvalidator;

import org.bimserver.ifcvalidator.checks.NewChecksRegistry;
import org.bimserver.plugins.SchemaName;

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
import org.bimserver.validationreport.JsonValidationReport;

public class NewChecksValidatorPlugin extends AbstractIfcValidatorPlugin {

	public NewChecksValidatorPlugin() {
		super(SchemaName.VALIDATION_JSON_2_0, false, new NewChecksRegistry());
	}

	@Override
	protected IssueContainerSerializer createIssueInterface(CheckerContext translator) {
		return new JsonValidationReport();
	}

	@Override
	public String getContentType() {
		return "application/json; charset=utf-8";
	}

	@Override
	public String getFileName() {
		return "validationresults.json";
	}
}