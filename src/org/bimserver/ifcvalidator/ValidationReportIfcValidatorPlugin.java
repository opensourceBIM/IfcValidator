package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueContainerSerializer;
import org.bimserver.validationreport.JsonValidationReport;

public class ValidationReportIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public ValidationReportIfcValidatorPlugin() {
		super("VALIDATION_JSON_1_0", false);
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