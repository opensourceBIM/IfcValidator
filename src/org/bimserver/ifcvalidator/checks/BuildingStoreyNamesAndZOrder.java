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

import java.util.Map;
import java.util.TreeMap;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class BuildingStoreyNamesAndZOrder extends ModelCheck {

	public BuildingStoreyNamesAndZOrder() {
		super("BUILDINGSTOREYS", "BUILDING_STOREY_NAMES_AND_Z_ORDER");
	}
	
	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
//		int nrBuildingStoreys = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuildingStorey());
//		issueContainer.add(nrBuildingStoreys > 0 ? Type.SUCCESS : Type.ERROR, null, null, -1, "Number of building storeys", nrBuildingStoreys + " IfcBuildingStorey objects", "> 0 IfcBuildingStorey objects");
		
		Map<Integer, IfcBuildingStorey> mapped = new TreeMap<>();
		for (IfcBuildingStorey ifcBuildingStorey : model.getAll(IfcBuildingStorey.class)) {
			String name = ifcBuildingStorey.getName();
			if (name.contains(" ")) {
				String[] split = name.split(" ");
				String number = split[0];
				try {
					if (!Character.isDigit(number.charAt(number.length() -1))) {
						// Must be a sub-storey, skip it for now
					} else {
						int storeyNumber = Integer.parseInt(number);
						if (mapped.containsKey(storeyNumber)) {
							issueContainer.builder().type(Type.ERROR).object(ifcBuildingStorey).message("Duplicate storey name").is(ifcBuildingStorey.getName()).shouldBe("").add();
						} else {
							mapped.put(storeyNumber, ifcBuildingStorey);
							issueContainer.builder().type(Type.SUCCESS).object(ifcBuildingStorey).message("Valid building name").is(ifcBuildingStorey.getName()).shouldBe("").add();
						}
					}
				} catch (NumberFormatException e) {
					issueContainer.builder().type(Type.ERROR).object(ifcBuildingStorey).message("Invalid building name, invalid number " + split[0]).is(ifcBuildingStorey.getName()).shouldBe("").add();
				}
			} else {
				issueContainer.builder().type(Type.ERROR).object(ifcBuildingStorey).message("Invalid building name, no spaces").is(ifcBuildingStorey.getName()).shouldBe("").add();
			}
		}
		if (mapped.size() > 1) {
			double lastZ = -1;
			boolean increasingWithHeight = true;
			for (int number : mapped.keySet()) {
				IfcBuildingStorey ifcBuildingStorey = mapped.get(number);
				double minZ = Double.MAX_VALUE;
				double maxZ = -Double.MAX_VALUE;
				for (IfcProduct ifcProduct : IfcUtils.getDecomposition(ifcBuildingStorey)) {
					GeometryInfo geometry = ifcProduct.getGeometry();
					if (geometry != null) {
						Vector3f min = geometry.getMinBounds();
						Vector3f max = geometry.getMaxBounds();
						if (min.getZ() < minZ) {
							minZ = min.getZ();
						}
						if (max.getZ() > maxZ) {
							maxZ = max.getZ();
						}
					}
				}
				double aabbCenterZ = minZ + (maxZ - minZ) / 2d;
				IfcBuildingStorey lastStorey = null;
				if (lastZ == -1 || aabbCenterZ > lastZ) {
					lastZ = aabbCenterZ;
					lastStorey = ifcBuildingStorey;
				} else {
					increasingWithHeight = false;
					issueContainer.builder().type(Type.ERROR).object(ifcBuildingStorey).message("Building storey " + getObjectIdentifier(ifcBuildingStorey) + " seems to be lower than " + getObjectIdentifier(lastStorey)).is(ifcBuildingStorey.getName()).shouldBe("").add();
				}
			}
			if (increasingWithHeight) {
				issueContainer.builder().type(Type.SUCCESS).message("Storeys seem to be increasing with z-value and naming").add();
			}		
		}
	}
}