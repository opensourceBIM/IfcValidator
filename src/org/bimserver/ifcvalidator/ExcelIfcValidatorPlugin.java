package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueContainerSerializer;

public class ExcelIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public ExcelIfcValidatorPlugin() {
		super("VALIDATION_XLSX_1_0", false);
	}

	@Override
	protected IssueContainerSerializer createIssueInterface(CheckerContext translator) {
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