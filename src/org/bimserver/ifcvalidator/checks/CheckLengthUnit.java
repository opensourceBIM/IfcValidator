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

public class CheckLengthUnit extends ModelCheck {

	public CheckLengthUnit() {
		super("UNITS", "LENGTH");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		boolean valid = false;
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
						issueInterface.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Length unit definition", lengthUnitFound, "Found");
						issueInterface.add(metres ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Length unit", metres, "Metres");
						issueInterface.add(rightPrefix ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Length unit prefix", ifcSIUnit.getPrefix(), "None or millis");
						valid = lengthUnitFound && metres && rightPrefix;
					}
				}
			}
			if (!lengthUnitFound) {
				issueInterface.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, "Length unit definition", lengthUnitFound, "Found");
			}
		}
		return valid;
	}
}
