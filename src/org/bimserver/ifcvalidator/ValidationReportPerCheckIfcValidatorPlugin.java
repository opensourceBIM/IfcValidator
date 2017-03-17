package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.JsonValidationReport;

public class ValidationReportPerCheckIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public ValidationReportPerCheckIfcValidatorPlugin() {
		super("VALIDATION_JSON_1_0", true);
	}

	@Override
	protected IssueInterface createIssueInterface(Translator translator) {
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