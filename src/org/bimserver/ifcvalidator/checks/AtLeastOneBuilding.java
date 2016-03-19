package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class AtLeastOneBuilding extends ModelCheck {

	public AtLeastOneBuilding() {
		super("BUILDING", "AT_LEAST_ONE_BUILDING");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		int nrBuildings = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuilding());
		validationReport.add(nrBuildings > 0 ? Type.SUCCESS : Type.ERROR, -1, "Number of buildings", nrBuildings + " IfcBuilding objects", "> 0 IfcBuilding objects");
	}
}