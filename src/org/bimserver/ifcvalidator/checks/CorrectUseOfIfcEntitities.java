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
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.validationreport.IssueContainer;

// TODO
public class CorrectUseOfIfcEntitities extends ModelCheck {

	private static final int MAX_SLAB_THICKNESS_MM = 200;
	
	public CorrectUseOfIfcEntitities() {
		super("IFC_ENTITIES", "CORRECT_USE_OF_IFC_ENTITIES");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) {
		for (IfcSlab ifcSlab : model.getAll(IfcSlab.class)) {
			ifcSlab.getGeometry().getMaxBounds();
		}
	}

	private void checkHeightLessThenWidthOrDepth(IfcProduct ifcProduct) {
//		GeometryInfo geometry = ifcProduct.getGeometry();
//		if (geometry != null) {
//			Vector3f max = geometry.getMaxBounds();
//			Vector3f min = geometry.getMinBounds();
//		}
	}
}
