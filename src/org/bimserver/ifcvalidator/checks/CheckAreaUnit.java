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
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class CheckAreaUnit extends ModelCheck {

	public CheckAreaUnit() {
		super("UNITS", "AREA");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
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
						issueInterface.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Area unit definition", areaUnitFound, "Found");
						issueInterface.add(metres ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Area unit", metres, "Metres squared");
						issueInterface.add(rightPrefix ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Area unit prefix", ifcSIUnit.getPrefix(), "None");
						valid = areaUnitFound && metres && rightPrefix;
					}
				}
			}
			if (!areaUnitFound) {
				issueInterface.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, "Area unit definition", areaUnitFound, "Found");
			}
		}
		return valid;
	}
}
