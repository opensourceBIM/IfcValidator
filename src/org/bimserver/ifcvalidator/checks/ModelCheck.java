package org.bimserver.ifcvalidator.checks;

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
