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

public class CheckLengthUnit extends ModelCheck {

	public CheckLengthUnit() {
		super("UNITS", "LENGTH");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			IfcUnitAssignment unitsInContext = ifcProject.getUnitsInContext();
	
			boolean lengthUnitFound = false;
	
			for (IfcUnit ifcUnit : unitsInContext.getUnits()) {
				if (ifcUnit instanceof IfcSIUnit) {
					IfcSIUnit ifcSIUnit = (IfcSIUnit) ifcUnit;
					if (ifcSIUnit.getUnitType() == IfcUnitEnum.LENGTHUNIT) {
						lengthUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.MILLI || ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						issueContainer.builder().type(lengthUnitFound ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Length unit definition").is(lengthUnitFound).shouldBe("Found").add();
						issueContainer.builder().type(metres ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Length unit").is(lengthUnitFound).shouldBe("Metres").add();
						issueContainer.builder().type(rightPrefix ? Type.SUCCESS : Type.ERROR).object(ifcSIUnit).message("Length unit prefix").is(lengthUnitFound).shouldBe("None or millis").add();
					}
				}
			}
			if (!lengthUnitFound) {
				issueContainer.builder().type(lengthUnitFound ? Type.SUCCESS : Type.ERROR).message("Length unit definition").is(lengthUnitFound).shouldBe("Found").add();
			}
		}
	}
}
