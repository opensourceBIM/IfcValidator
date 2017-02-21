package org.bimserver.ifcvalidator.checks;

import java.util.Map;
import java.util.TreeMap;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class BuildingStoreyNamesAndZOrder extends ModelCheck {

	public BuildingStoreyNamesAndZOrder() {
		super("BUILDINGSTOREYS", "BUILDING_STOREY_NAMES_AND_Z_ORDER");
	}
	
	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
//		int nrBuildingStoreys = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuildingStorey());
//		issueInterface.add(nrBuildingStoreys > 0 ? Type.SUCCESS : Type.ERROR, null, null, -1, "Number of building storeys", nrBuildingStoreys + " IfcBuildingStorey objects", "> 0 IfcBuildingStorey objects");
		
		boolean valid = true;
		
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
							issueInterface.add(Type.ERROR, ifcBuildingStorey.eClass().getName(), ifcBuildingStorey.getGlobalId(), ifcBuildingStorey.getOid(), "Duplicate storey name", ifcBuildingStorey.getName(), "");
							valid = false;
						} else {
							mapped.put(storeyNumber, ifcBuildingStorey);
							issueInterface.add(Type.SUCCESS, ifcBuildingStorey.eClass().getName(), ifcBuildingStorey.getGlobalId(), ifcBuildingStorey.getOid(), "Valid building name", ifcBuildingStorey.getName(), "");
						}
					}
				} catch (NumberFormatException e) {
					issueInterface.add(Type.ERROR, ifcBuildingStorey.eClass().getName(), ifcBuildingStorey.getGlobalId(), ifcBuildingStorey.getOid(), "Invalid building name, invalid number " + split[0], ifcBuildingStorey.getName(), "");
					valid = false;
				}
			} else {
				issueInterface.add(Type.ERROR, ifcBuildingStorey.eClass().getName(), ifcBuildingStorey.getGlobalId(), ifcBuildingStorey.getOid(), "Invalid building name, no spaces", ifcBuildingStorey.getName(), "");
				valid = false;
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
					issueInterface.add(Type.ERROR, ifcBuildingStorey.eClass().getName(), ifcBuildingStorey.getGlobalId(), ifcBuildingStorey.getOid(), "Building storey " + getObjectIdentifier(ifcBuildingStorey) + " seems to be lower than " + getObjectIdentifier(lastStorey), ifcBuildingStorey.getName(), "");
					valid = false;
				}
			}
			if (increasingWithHeight) {
				issueInterface.add(Type.SUCCESS, "Storeys seem to be increasing with z-value and naming", "", "");
			}		
		}
		return valid;
	}
}