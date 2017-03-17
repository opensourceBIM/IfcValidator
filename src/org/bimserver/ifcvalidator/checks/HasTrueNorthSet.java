package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcGeometricRepresentationContext;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.ifc2x3tc1.IfcRepresentationContext;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;
import org.eclipse.emf.common.util.EList;

import com.google.common.base.Joiner;

public class HasTrueNorthSet extends ModelCheck {

	public HasTrueNorthSet() {
		super("REPRESENTATION", "HAS_TRUE_NORTH_SET");
	}
	
	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		boolean valid = projects.size() > 0;
		for (IfcProject ifcProject : projects) {
			EList<IfcRepresentationContext> representationContexts = ifcProject.getRepresentationContexts();
			if (representationContexts.isEmpty()) {
				issueContainer.add(Type.ERROR, ifcProject.eClass().getName(), ifcProject.getGlobalId(), ifcProject.getOid(), translator.translate("IFC_PROJECT_NUMBER_OF_REPRESENTATION_CONTEXTS"), "0", "> 0");
				valid = false;
			} else {
				IfcDirection trueNorth = null;
				IfcGeometricRepresentationContext context = null;
				for (IfcRepresentationContext ifcRepresentationContext : representationContexts) {
					if (ifcRepresentationContext instanceof IfcGeometricRepresentationContext) {
						IfcGeometricRepresentationContext ifcGeometricRepresentationContext = (IfcGeometricRepresentationContext)ifcRepresentationContext;
						if (ifcGeometricRepresentationContext.getTrueNorth() != null) {
							trueNorth = ifcGeometricRepresentationContext.getTrueNorth();
							context = ifcGeometricRepresentationContext;
						}
					}
				}
				String stringVersion = "null";
				if (trueNorth != null) {
					Joiner joiner = Joiner.on(", ").skipNulls();
					stringVersion = joiner.join(trueNorth.getDirectionRatios());
				}
				issueContainer.add(trueNorth != null ? Type.SUCCESS : Type.ERROR, "IfcGeometricRepresentationContext", null, context == null ? null : context.getOid(), translator.translate("TRUE_NORTH_SET"), stringVersion, translator.translate("SET"));
				if (trueNorth == null) {
					valid = false;
				}
			}
		}
		return valid;
	}
}
