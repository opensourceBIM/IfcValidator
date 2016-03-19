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
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class CheckUnits extends ModelCheck {

	public CheckUnits() {
		super("UNITS", "UNITS");
	}
	
	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			IfcUnitAssignment unitsInContext = ifcProject.getUnitsInContext();
	
			boolean lengthUnitFound = false;
			boolean volumeUnitFound = false;
			boolean areaUnitFound = false;
	
			for (IfcUnit ifcUnit : unitsInContext.getUnits()) {
				if (ifcUnit instanceof IfcSIUnit) {
					IfcSIUnit ifcSIUnit = (IfcSIUnit) ifcUnit;
					if (ifcSIUnit.getUnitType() == IfcUnitEnum.LENGTHUNIT) {
						lengthUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.MILLI || ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						validationReport.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Length unit definition", lengthUnitFound, "Found");
						validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Length unit", metres, "Metres");
						validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Length unit prefix", ifcSIUnit.getPrefix(), "None or millis");
					} else if (ifcSIUnit.getUnitType() == IfcUnitEnum.AREAUNIT) {
						areaUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.SQUARE_METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						validationReport.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Area unit definition", areaUnitFound, "Found");
						validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Area unit", metres, "Metres squared");
						validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Area unit prefix", ifcSIUnit.getPrefix(), "None");
					} else if (ifcSIUnit.getUnitType() == IfcUnitEnum.VOLUMEUNIT) {
						volumeUnitFound = true;
						boolean metres = ifcSIUnit.getName() == IfcSIUnitName.CUBIC_METRE;
						boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
						validationReport.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Volume unit definition", volumeUnitFound, "Found");
						validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Volume unit", metres, "Cubic metres");
						validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Volume unit prefix", ifcSIUnit.getPrefix(), "None");
					}
				}
			}
			if (!lengthUnitFound) {
				validationReport.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Length unit definition", lengthUnitFound, "Found");
			}
			if (!areaUnitFound) {
				validationReport.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Area unit definition", areaUnitFound, "Found");
			}
			if (!volumeUnitFound) {
				validationReport.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Volume unit definition", volumeUnitFound, "Found");
			}
		}
	}
}