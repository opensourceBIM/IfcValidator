package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueInterface;

public class ExcelIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public ExcelIfcValidatorPlugin() {
		super("validationexcel");
	}

	@Override
	protected IssueInterface createIssueInterface(Translator translator) {
		return new ExcelIssueInterface(translator);
	}

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	@Override
	public String getFileName() {
		return "validationresults.xlsx";
	}
}
