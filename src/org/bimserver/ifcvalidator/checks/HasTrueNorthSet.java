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
	public void check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		for (IfcProject ifcProject : projects) {
			EList<IfcRepresentationContext> representationContexts = ifcProject.getRepresentationContexts();
			if (representationContexts.isEmpty()) {
				issueContainer.builder().type(Type.ERROR).object(ifcProject).message(translator.translate("IFC_PROJECT_NUMBER_OF_REPRESENTATION_CONTEXTS")).is("0").shouldBe("> 0").add();
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
				issueContainer.builder().type(trueNorth != null ? Type.SUCCESS : Type.ERROR).object(context).message(translator.translate("TRUE_NORTH_SET")).is(stringVersion).shouldBe(translator.translate("SET")).add();
			}
		}
	}
}
