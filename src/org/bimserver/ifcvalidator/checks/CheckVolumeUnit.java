package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

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
