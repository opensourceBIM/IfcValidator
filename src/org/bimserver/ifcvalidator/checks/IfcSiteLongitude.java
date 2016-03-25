package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class IfcSiteLongitude extends ModelCheck {

	public IfcSiteLongitude() {
		super("SITE", "LONGITUDE");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcSite ifcSite : model.getAll(IfcSite.class)) {
			// Only checking whether this data is available
			
			if (ifcSite.eIsSet(Ifc2x3tc1Package.eINSTANCE.getIfcSite_RefLongitude())) {
				// TODO check whether this is a valid WSG84
				validationReport.add(Type.SUCCESS, ifcSite.getOid(), "RefLongitude", ifcSite.getRefLongitude(), "Not null");
			} else {
				validationReport.add(Type.ERROR, ifcSite.getOid(), "RefLongitude", null, "Not null");
			}
		}		
	}
}
