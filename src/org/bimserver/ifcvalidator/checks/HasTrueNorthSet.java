package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcGeometricRepresentationContext;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.ifc2x3tc1.IfcRepresentationContext;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;
import org.eclipse.emf.common.util.EList;

import com.google.common.base.Joiner;

public class HasTrueNorthSet extends ModelCheck {

	public HasTrueNorthSet() {
		super("REPRESENTATION", "HAS_TRUE_NORTH_SET");
	}
	
	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			EList<IfcRepresentationContext> representationContexts = ifcProject.getRepresentationContexts();
			if (representationContexts.isEmpty()) {
				validationReport.add(Type.ERROR, ifcProject.getOid(), translator.translate("IFC_PROJECT_NUMBER_OF_REPRESENTATION_CONTEXTS"), "0", "> 0");
			} else {
				IfcDirection trueNorth = null;
				for (IfcRepresentationContext ifcRepresentationContext : representationContexts) {
					if (ifcRepresentationContext instanceof IfcGeometricRepresentationContext) {
						IfcGeometricRepresentationContext ifcGeometricRepresentationContext = (IfcGeometricRepresentationContext)ifcRepresentationContext;
						if (ifcGeometricRepresentationContext.getTrueNorth() != null) {
							trueNorth = ifcGeometricRepresentationContext.getTrueNorth();
						}
					}
				}
				String stringVersion = "null";
				if (trueNorth != null) {
					Joiner joiner = Joiner.on(", ").skipNulls();
					stringVersion = joiner.join(trueNorth.getDirectionRatios());
				}
				validationReport.add(trueNorth != null ? Type.SUCCESS : Type.ERROR, -1, "TrueNorth", stringVersion, "Set");
			}
		}
	}
}
