package org.bimserver.ifcvalidator.checks;

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

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;

public abstract class ModelCheck {

	private String identifier;
	private String groupIdentifier;

	public abstract void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException;

	public String getIdentifier() {
		return identifier;
	}
	
	public ModelCheck(String groupIdentifier, String identifier) {
		this.groupIdentifier = groupIdentifier;
		this.identifier = identifier;
	}
	
	public String getGroupIdentifier() {
		return groupIdentifier;
	}
	
	public String getObjectIdentifier(IfcProduct ifcProduct) {
		if (ifcProduct == null) {
			return "No object";
		}
		String name = ifcProduct.getName();
		if (name != null && !name.trim().equals("")) {
			return name;
		}
		String guid = ifcProduct.getGlobalId();
		if (guid != null && !guid.trim().equals("")) {
			return guid;
		}
		return ifcProduct.eClass().getName() + " " + ifcProduct.getOid();
	}
	
	public String getDescription(CheckerContext translator ) {
		return translator.translate(identifier + "_DESCRIPTION");
	}

	public boolean isEnabledByDefault() {
		return true;
	}

	public String getName(CheckerContext translator) {
		return translator.translate(identifier + "_NAME");
	}
}
