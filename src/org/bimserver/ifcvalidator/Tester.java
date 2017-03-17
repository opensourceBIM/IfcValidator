package org.bimserver.ifcvalidator;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.checks.ModelCheck;
import org.bimserver.ifcvalidator.checks.ModelCheckerRegistry;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;

public class Tester {
	private ModelCheckerRegistry modelCheckerRegistry;
	private IssueContainer issueContainer;

	public Tester() {
		issueContainer = new IssueContainer();
		modelCheckerRegistry = new ModelCheckerRegistry();
	}
	
	public boolean test(IfcModelInterface model, String groupIdentifier, String identifier) {
		ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
		try {
			return modelCheck.check(model, issueContainer, null);
		} catch (IssueException e) {
			e.printStackTrace();
		}
		return false;
	}

	public IssueContainer getIssueContainer() {
		return issueContainer;
	}
}
