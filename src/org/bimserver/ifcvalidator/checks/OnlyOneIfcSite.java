package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class OnlyOneIfcSite extends ModelCheck {

	public OnlyOneIfcSite() {
		super("SITE", "ONLY_ONE_SITE");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		List<IfcSite> sites = model.getAll(IfcSite.class);
		IfcSite ifcSite = sites.size() == 1 ? sites.get(0) : null;
		
		issueInterface.add(sites.size() == 1 ? Type.SUCCESS : Type.ERROR, "IfcSite", ifcSite == null ? null : ifcSite.getGlobalId(), ifcSite == null ? null : ifcSite.getOid(), translator.translate("NUMBER_OF_SITES"), sites.size() + " " + translator.translate(sites.size() == 1 ? "SITE" : "SITES"), translator.translate("EXACTLY_ONE_SITE"));
		
		return sites.size() == 1;
	}
}
