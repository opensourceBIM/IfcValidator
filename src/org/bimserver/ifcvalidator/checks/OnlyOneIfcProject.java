package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class OnlyOneIfcProject extends ModelCheck {
	public OnlyOneIfcProject() {
		super("PROJECT", "ONLY_ONE_IFC_PROJECT");
	}
	
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		int nrIfcProjects = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcProject());
		validationReport.add(nrIfcProjects == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of projects", nrIfcProjects + " projects", "Exactly 1 IfcProject object");
	}
}