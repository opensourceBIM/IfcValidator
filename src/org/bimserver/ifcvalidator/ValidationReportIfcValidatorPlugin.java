package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.JsonValidationReport;

public class ValidationReportIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public ValidationReportIfcValidatorPlugin() {
		super("IFC Validator", "http://extend.bimserver.org/validationreport");
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