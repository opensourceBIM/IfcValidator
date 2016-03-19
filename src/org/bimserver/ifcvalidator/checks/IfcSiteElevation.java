package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class IfcSiteElevation extends ModelCheck {

	public IfcSiteElevation() {
		super("SITE", "ELEVATION");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcSite ifcSite : model.getAll(IfcSite.class)) {
			if (ifcSite.isSetRefElevation()) {
				validationReport.add(Type.SUCCESS, ifcSite.getOid(), "RefElevation", "Not null", "Not null");
			} else {
				validationReport.add(Type.ERROR, ifcSite.getOid(), "RefElevation", null, "Not null");
			}
		}		
	}
}
