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

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AtLeastOneBuilding extends ModelCheck {

	public AtLeastOneBuilding() {
		super("BUILDING", "AT_LEAST_ONE_BUILDING");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcBuilding> buildings = model.getAll(IfcBuilding.class);
		IfcBuilding building = buildings.size() == 1 ? buildings.get(0) : null;
		
		boolean valid = buildings.size() > 0;
		issueContainer.builder().originatingCheck(this.getClass().getSimpleName()).author(checkerContext.getAuthor()).type(valid ? Type.SUCCESS : Type.ERROR).object(building).message(checkerContext.translate("NUMBER_OF_BUILDINGS")).add();
//		issueContainer.add(, building == null ? null : "IfcBuilding", building == null ? null : building.getGlobalId(), building == null ? null : building.getOid(), translator.translate("NUMBER_OF_BUILDINGS"), buildings.size() + " " + translator.translate(buildings.size() == 1 ? "BUILDING_OBJECT" : "BUILDING_OBJECTS"), translator.translate("ATLEAST_ONE_BUILDING"));
	}
}