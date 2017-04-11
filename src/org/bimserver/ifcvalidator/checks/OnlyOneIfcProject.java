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

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class OnlyOneIfcProject extends ModelCheck {
	public OnlyOneIfcProject() {
		super("PROJECT", "ONLY_ONE_IFC_PROJECT");
	}
	
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		
		IfcProject ifcProject = projects.size() == 1 ? projects.get(0) : null;
		issueContainer.builder().type(projects.size() == 1 ? Type.SUCCESS : Type.ERROR)
		.object(ifcProject).message(checkerContext.translate("NUMBER_OF_PROJECTS")).is(projects.size() + " " + checkerContext.translate(projects.size() == 1 ? "PROJECT" : "PROJECTS")).shouldBe(checkerContext.translate("EXACTLY_ONE_PROJECT")).add();
	}
}