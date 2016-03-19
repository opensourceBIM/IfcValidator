package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class AllObjectsInBuildingStorey extends ModelCheck {

	public AllObjectsInBuildingStorey() {
		super("BUILDINGSTOREY", "ALL_OBJECTS_IN_BUILDING_STOREY");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			if (ifcProduct instanceof IfcSite || ifcProduct instanceof IfcBuilding) {
				continue;
				// Skip
			}
			IfcBuildingStorey ifcBuildingStorey = IfcUtils.getIfcBuildingStorey(ifcProduct);
			if (ifcBuildingStorey == null) {
				validationReport.add(Type.ERROR, ifcProduct.getOid(), "Object " + getObjectIdentifier(ifcProduct) + " must be linked to a building storey", "None", "Building storey");
			}
		}
	}
}