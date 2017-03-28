package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
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

public class CheckVolumeUnit extends ModelCheck {

	public CheckVolumeUnit() {
		super("UNITS", "VOLUME");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			IfcUnitAssignment unitsInContext = ifcProject.getUnitsInContext();
	
			boolean volumeUnitFound = false;
	
			for (IfcUnit ifcUnit : unitsInContext.getUnits()) {
				if (ifcUnit instanceof IfcSIUnit) {
					IfcSIUnit ifcSIUnit = (IfcSIUnit) ifcUnit;
					if (ifcSIUnit.getUnitType() == IfcUnitEnum.VOLUMEUNIT) {
						volumeUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.CUBIC_METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						issueContainer.builder().type(volumeUnitFound ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Volume unit definition").is(volumeUnitFound).shouldBe("Found").add();
						issueContainer.builder().type(metres ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Volume unit").is(metres).shouldBe("Cubic metres").add();
						issueContainer.builder().type(rightPrefix ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Volume unit prefix").is(ifcSIUnit.getPrefix()).shouldBe("None").add();
					}
				}
			}
			if (!volumeUnitFound) {
				issueContainer.builder().type(volumeUnitFound ? Type.SUCCESS : Type.ERROR).message("Volume unit definition").is(volumeUnitFound).shouldBe("Found").add();
			}
		}
	}
}
