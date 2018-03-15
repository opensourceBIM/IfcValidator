package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
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

public class CheckAreaUnit extends ModelCheck {

	public CheckAreaUnit() {
		super("UNITS", "AREA");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
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
					}
				}
			}
			if (!areaUnitFound) {
				issueContainer.builder().type(areaUnitFound ? Type.SUCCESS : Type.ERROR).message("Area unit definition").is(areaUnitFound).shouldBe("Found").add();
			}
		}
	}
}