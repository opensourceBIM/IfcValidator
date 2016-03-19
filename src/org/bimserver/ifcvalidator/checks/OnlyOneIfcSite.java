package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class OnlyOneIfcSite extends ModelCheck {

	public OnlyOneIfcSite() {
		super("SITE", "ONLY_ONE_SITE");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		int nrIfcSites = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcSite());
		validationReport.add(nrIfcSites == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of sites", nrIfcSites + " sites", "Exactly 1 IfcSite object");
	}
}
