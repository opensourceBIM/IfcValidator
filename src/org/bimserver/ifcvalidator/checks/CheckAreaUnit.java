package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.ifc2x3tc1.IfcSIPrefix;
import org.bimserver.models.ifc2x3tc1.IfcSIUnit;
import org.bimserver.models.ifc2x3tc1.IfcSIUnitName;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcUnitEnum;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class CheckAreaUnit extends ModelCheck {

	public CheckAreaUnit() {
		super("UNITS", "AREA");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		boolean valid = false;
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			IfcUnitAssignment unitsInContext = ifcProject.getUnitsInContext();
	
			boolean areaUnitFound = false;
	
			for (IfcUnit ifcUnit : unitsInContext.getUnits()) {
				if (ifcUnit instanceof IfcSIUnit) {
					IfcSIUnit ifcSIUnit = (IfcSIUnit) ifcUnit;
					if (ifcSIUnit.getUnitType() == IfcUnitEnum.AREAUNIT) {
						areaUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.SQUARE_METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						issueContainer.builder().type(areaUnitFound ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Area unit definition").is(areaUnitFound).shouldBe("Found").add();
						issueContainer.builder().type(metres ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Area unit").is(metres).shouldBe("Metres squared").add();
						issueContainer.builder().type(rightPrefix ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Area unit prefix").is(ifcSIUnit.getPrefix()).shouldBe("None").add();
						valid = areaUnitFound && metres && rightPrefix;
					}
				}
			}
			if (!areaUnitFound) {
				issueContainer.builder().type(areaUnitFound ? Type.SUCCESS : Type.ERROR).message("Area unit definition").is(areaUnitFound).shouldBe("Found").add();
			}
		}
	}
}