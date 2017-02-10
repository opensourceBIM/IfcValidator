package org.bimserver.ifcvalidator;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.checks.ModelCheck;
import org.bimserver.ifcvalidator.checks.ModelCheckerRegistry;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.JsonValidationReport;

public class Tester {
	private ModelCheckerRegistry modelCheckerRegistry;
	private JsonValidationReport jsonValidationReport;

	public Tester() {
		jsonValidationReport = new JsonValidationReport();
		modelCheckerRegistry = new ModelCheckerRegistry();
	}
	
	public boolean test(IfcModelInterface model, String groupIdentifier, String identifier) {
		ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
		try {
			return modelCheck.check(model, jsonValidationReport, null);
		} catch (IssueException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public JsonValidationReport getJsonValidationReport() {
		return jsonValidationReport;
	}
}
