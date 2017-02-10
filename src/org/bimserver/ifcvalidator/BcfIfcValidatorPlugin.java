package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueInterface;

public class BcfIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public BcfIfcValidatorPlugin() {
		super("BCF_ZIP_2_0");
	}

	@Override
	protected IssueInterface createIssueInterface(Translator translator) {
		return new BcfInterface(translator);
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public String getFileName() {
		return "validationresults.bcfzip";
	}
}