package org.bimserver.ifcvalidator;

import org.bimserver.validationreport.IssueContainerSerializer;

public class BcfIfcValidatorPlugin extends AbstractIfcValidatorPlugin {

	public BcfIfcValidatorPlugin() {
		super("BCF_ZIP_2_0", false);
	}

	@Override
	protected IssueContainerSerializer createIssueInterface(Translator translator) {
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