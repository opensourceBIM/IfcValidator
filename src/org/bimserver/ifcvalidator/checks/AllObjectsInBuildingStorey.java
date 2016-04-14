package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class AllObjectsInBuildingStorey extends ModelCheck {

	public AllObjectsInBuildingStorey() {
		super("BUILDINGSTOREYS", "ALL_OBJECTS_IN_BUILDING_STOREY");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		boolean ok = true;
		List<IfcProduct> products = model.getAllWithSubTypes(IfcProduct.class);
		for (IfcProduct ifcProduct : products) {
			if (ifcProduct instanceof IfcSite || ifcProduct instanceof IfcBuilding || ifcProduct instanceof IfcOpeningElement) {
				continue;
				// Skip
			}
			IfcBuildingStorey ifcBuildingStorey = IfcUtils.getIfcBuildingStorey(ifcProduct);
			if (ifcBuildingStorey == null) {
				issueInterface.add(Type.ERROR, ifcProduct.eClass().getName(), ifcProduct.getGlobalId(), ifcProduct.getOid(), "Object " + getObjectIdentifier(ifcProduct) + " must be linked to a building storey", "None", "Building storey");
				ok = false;
			}
		}
		if (ok) {
			issueInterface.add(Type.SUCCESS, translator.translate("ALL_OBJECTS_MUST_BE_LINKED_TO_A_BUILDING_STOREY"), translator.translate("ALL_OBJECTS_LINKED_TO_BUILDING_STOREY"), translator.translate("ALL_OBJECT_LINKED1") + " "+ products.size() + " " + translator.translate("ALL_OBJECTS_LINKED2"));
			return true;
		}
		return false;
	}
}