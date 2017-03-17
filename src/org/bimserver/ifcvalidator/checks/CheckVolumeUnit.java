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

public class CheckVolumeUnit extends ModelCheck {

	public CheckVolumeUnit() {
		super("UNITS", "VOLUME");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		boolean valid = false;
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
						issueContainer.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Volume unit definition", volumeUnitFound, "Found");
						issueContainer.add(metres ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Volume unit", metres, "Cubic metres");
						issueContainer.add(rightPrefix ? Type.SUCCESS : Type.ERROR, ifcSIUnit.eClass().getName(), null, ifcSIUnit.getOid(), "Volume unit prefix", ifcSIUnit.getPrefix(), "None");
						valid = volumeUnitFound && metres && rightPrefix;
					}
				}
			}
			if (!volumeUnitFound) {
				issueContainer.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, "Volume unit definition", volumeUnitFound, "Found");
			}
		}
		return valid;
	}
}
