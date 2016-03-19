package org.bimserver.ifcvalidator.checks;

import java.util.Map;
import java.util.TreeMap;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class BuildingStoreyNamesAndZOrder extends ModelCheck {

	public BuildingStoreyNamesAndZOrder() {
		super("BUILDINGSTOREYS", "BUILDING_STOREY_NAMES_AND_Z_ORDER");
	}
	
	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		int nrBuildingStoreys = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuildingStorey());
		validationReport.add(nrBuildingStoreys > 0 ? Type.SUCCESS : Type.ERROR, -1, "Number of building storeys", nrBuildingStoreys + " IfcBuildingStorey objects", "> 0 IfcBuildingStorey objects");
		
		// TODO check whether all objects are linked to storeys
		
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
							validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Duplicate storey name", ifcBuildingStorey.getName(), "");
						} else {
							mapped.put(storeyNumber, ifcBuildingStorey);
							validationReport.add(Type.SUCCESS, ifcBuildingStorey.getOid(), "Valid building name", ifcBuildingStorey.getName(), "");
						}
					}
				} catch (NumberFormatException e) {
					validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Invalid building name, invalid number " + split[0], ifcBuildingStorey.getName(), "");
				}
			} else {
				validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Invalid building name, no spaces", ifcBuildingStorey.getName(), "");
			}
		}
		double lastZ = -1;
		boolean increasingWithHeight = true;
		for (int number : mapped.keySet()) {
			IfcBuildingStorey ifcBuildingStorey = mapped.get(number);
			double minZ = Double.MAX_VALUE;
			double maxZ = -Double.MAX_VALUE;
			for (IfcProduct ifcProduct : IfcUtils.getChildren(ifcBuildingStorey)) {
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
			} else {
				increasingWithHeight = false;
				validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Building storey " + getObjectIdentifier(ifcBuildingStorey) + " seems to be lower than " + getObjectIdentifier(lastStorey), ifcBuildingStorey.getName(), "");
			}
		}
		if (increasingWithHeight) {
			validationReport.add(Type.SUCCESS, -1, "Storeys seem to be increasing with z-value and naming", "", "");
		}		
	}
}