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
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcGeometricRepresentationContext;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.ifc2x3tc1.IfcRepresentationContext;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;
import org.eclipse.emf.common.util.EList;

import com.google.common.base.Joiner;

public class HasTrueNorthSet extends ModelCheck {

	public HasTrueNorthSet() {
		super("REPRESENTATION", "HAS_TRUE_NORTH_SET");
	}
	
	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		for (IfcProject ifcProject : projects) {
			EList<IfcRepresentationContext> representationContexts = ifcProject.getRepresentationContexts();
			if (representationContexts.isEmpty()) {
				issueContainer.builder().originatingCheck(this.getClass().getSimpleName()).author(checkerContext.getAuthor()).type(Type.ERROR).object(ifcProject).message(checkerContext.translate("IFC_PROJECT_NUMBER_OF_REPRESENTATION_CONTEXTS")).is("0").shouldBe("> 0").add();
			} else {
				IfcDirection trueNorth = null;
				IfcGeometricRepresentationContext context = null;
				for (IfcRepresentationContext ifcRepresentationContext : representationContexts) {
					if (ifcRepresentationContext instanceof IfcGeometricRepresentationContext) {
						IfcGeometricRepresentationContext ifcGeometricRepresentationContext = (IfcGeometricRepresentationContext)ifcRepresentationContext;
						if (ifcGeometricRepresentationContext.getTrueNorth() != null) {
							trueNorth = ifcGeometricRepresentationContext.getTrueNorth();
							context = ifcGeometricRepresentationContext;
						}
					}
				}
				String stringVersion = "null";
				if (trueNorth != null) {
					Joiner joiner = Joiner.on(", ").skipNulls();
					stringVersion = joiner.join(trueNorth.getDirectionRatios());
				}
				issueContainer.builder().originatingCheck(this.getClass().getSimpleName()).author(checkerContext.getAuthor()).type(trueNorth != null ? Type.SUCCESS : Type.ERROR).object(context).message(checkerContext.translate("TRUE_NORTH_SET")).is(stringVersion).shouldBe(checkerContext.translate("SET")).add();
			}
		}
	}
}
