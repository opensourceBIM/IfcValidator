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
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AtLeastOneBuildingStorey extends ModelCheck {

	public AtLeastOneBuildingStorey() {
		super("BUILDING_STOREY", "AT_LEAST_ONE_BUILDING_STOREY");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcBuildingStorey> buildingStories = model.getAll(IfcBuildingStorey.class);
		int size = buildingStories.size();
		IfcBuildingStorey buildingStorey = size == 1 ? buildingStories.get(0) : null;
		
		boolean valid = size > 0;
		issueContainer.builder().
			type(valid ? Type.SUCCESS : Type.ERROR).
			object(buildingStorey).
			message(checkerContext.translate(size == 1 ? "BUILDING_STOREY_OBJECT" : "BUILDING_STOREY_OBJECTS")).
			is(size + " " + checkerContext.translate(size == 1 ? "BUILDING_STOREY_OBJECT" : "BUILDING_STOREY_OBJECTS")).
			shouldBe(checkerContext.translate("ATLEAST_ONE_BUILDING_STOREY")).
			add();
	}
}