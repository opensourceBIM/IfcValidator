package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;

public class CheckNlSFB extends ModelCheck  {

	public CheckNlSFB() {
		super("", "");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		return true;
	}
}