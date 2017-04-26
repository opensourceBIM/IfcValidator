package org.bimserver.ifcvalidator.checks;

import java.util.List;

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
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFeatureElementSubtraction;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelSpaceBoundary;
import org.bimserver.models.ifc2x3tc1.IfcRelVoidsElement;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.ifc2x3tc1.Tristate;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class ExteriorWindowSizeSpaceRatio extends ModelCheck {

	private WindowSpaceRatioConfiguration conf;

	public ExteriorWindowSizeSpaceRatio(WindowSpaceRatioConfiguration conf) {
		super("GEOMETRY", "RATIOS");
		this.conf = conf;
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcSpace> spaces = model.getAll(IfcSpace.class);
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			if (ifcSpace.getObjectType() != null && ifcSpace.getObjectType().equalsIgnoreCase("parking")) {
				continue;
			}
			IfcBuildingStorey ifcBuildingStorey = IfcUtils.getIfcBuildingStorey(ifcSpace);
			double totalWindowArea = 0;
			int nrWindowsUsed = 0;
			
			float lengthUnitPrefix = IfcUtils.getLengthUnitPrefix(model);
			
			for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcSpace.getBoundedBy()) {
				IfcElement relatedBuildingElement = ifcRelSpaceBoundary.getRelatedBuildingElement();
				if (relatedBuildingElement != null) {
					boolean wallExternal = IfcUtils.getBooleanProperty(relatedBuildingElement, "IsExternal") == Tristate.TRUE;
					for (IfcRelVoidsElement ifcRelVoidsElement : relatedBuildingElement.getHasOpenings()) {
						IfcFeatureElementSubtraction relatedOpeningElement = ifcRelVoidsElement.getRelatedOpeningElement();
						if (relatedOpeningElement instanceof IfcOpeningElement) {
							IfcOpeningElement ifcOpeningElement = (IfcOpeningElement)relatedOpeningElement;
							for (IfcRelFillsElement ifcRelFillsElement : ifcOpeningElement.getHasFillings()) {
								IfcElement relatedBuildingElement2 = ifcRelFillsElement.getRelatedBuildingElement();
								if (relatedBuildingElement2 instanceof IfcWindow) {
									IfcWindow ifcWindow = (IfcWindow)relatedBuildingElement2;
									boolean windowExternal = IfcUtils.getBooleanProperty(ifcWindow, "IsExternal") == Tristate.TRUE;
									if (windowExternal || wallExternal) {
										double semanticArea = ifcWindow.getOverallWidth() * ifcWindow.getOverallHeight() * Math.pow(lengthUnitPrefix, 2);
										GeometryInfo windowGeometry = relatedBuildingElement2.getGeometry();
										if (windowGeometry != null) {
											double geometricArea = getBiggestSingleFaceOfUntranslatedBoundingBox(windowGeometry);
											if (semanticArea - geometricArea > 0.001) {
												issueContainer.builder().type(Type.ERROR).object(ifcWindow).message("Semantic window area (OverallWidth*OverallHeight) larger than geometric area").is(String.format("%.2f", (semanticArea))).shouldBe(String.format("%.2f", (geometricArea))).buildingStorey(ifcBuildingStorey).add();
											} else {
												totalWindowArea += semanticArea;
												nrWindowsUsed++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (nrWindowsUsed == 0) {
				issueContainer.builder().type(Type.CANNOT_CHECK).object(ifcSpace).message("Cannot check window/space ratio because no consistent (exterior) windows found in space \"" + ifcSpace.getName() + "\"").buildingStorey(ifcBuildingStorey).add();
			} else {
				if (ifcSpace.getGeometry() != null) {
					if (totalWindowArea * conf.getRatio() > ifcSpace.getGeometry().getArea()) {
						issueContainer.builder().type(Type.SUCCESS).object(ifcSpace).message("Window/space area ratio for space \"" + ifcSpace.getName() + "\"").is(String.format("%.2f", (totalWindowArea * conf.getRatio()))).shouldBe(" > " + String.format("%.2f", ifcSpace.getGeometry().getArea())).buildingStorey(ifcBuildingStorey).add();
					} else {
						issueContainer.builder().type(Type.ERROR).object(ifcSpace).message("Window/space area ratio for space \"" + ifcSpace.getName() + "\"").is(String.format("%.2f", (totalWindowArea * conf.getRatio()))).shouldBe(" > " + String.format("%.2f", ifcSpace.getGeometry().getArea())).buildingStorey(ifcBuildingStorey).add();
					}
				}
			}
		}
		if (spaces.isEmpty()) {
			issueContainer.builder().type(Type.CANNOT_CHECK).message("No IfcSpace objects found in model").add();
		}
	}
	
	private double getBiggestSingleFaceOfUntranslatedBoundingBox(GeometryInfo geometryInfo) {
		Vector3f max = geometryInfo.getMaxBoundsUntranslated();
		Vector3f min = geometryInfo.getMinBoundsUntranslated();
		
		double width = max.getX() - min.getX();
		double height = max.getY() - min.getY();
		double depth = max.getZ() - min.getZ();
		
		double biggestArea = width * height;
		if (height * depth > biggestArea) {
			biggestArea = height * depth;
		}
		if (depth * width > biggestArea) {
			biggestArea = depth * width;
		}
		return biggestArea;
	}
}