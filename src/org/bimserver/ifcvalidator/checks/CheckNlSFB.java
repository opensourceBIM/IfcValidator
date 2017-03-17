package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;

public class CheckNlSFB extends ModelCheck  {

	public CheckNlSFB() {
		super("", "");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		return true;
	}
}