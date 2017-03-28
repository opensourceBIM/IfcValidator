package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AllObjectsInBuildingStorey extends ModelCheck {

	public AllObjectsInBuildingStorey() {
		super("BUILDINGSTOREYS", "ALL_OBJECTS_IN_BUILDING_STOREY");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		boolean ok = true;
		List<IfcProduct> products = model.getAllWithSubTypes(IfcProduct.class);
		for (IfcProduct ifcProduct : products) {
			if (ifcProduct instanceof IfcSite || ifcProduct instanceof IfcBuilding || ifcProduct instanceof IfcOpeningElement) {
				continue;
				// Skip
			}
			IfcBuildingStorey ifcBuildingStorey = IfcUtils.getIfcBuildingStorey(ifcProduct);
			if (ifcBuildingStorey == null) {
				issueContainer.builder().type(Type.ERROR).object(ifcProduct).message("Object " + getObjectIdentifier(ifcProduct) + " must be linked to a building storey").add();
				ok = false;
			}
		}
		if (ok) {
			issueContainer.builder().type(Type.SUCCESS).message(checkerContext.translate("ALL_OBJECTS_MUST_BE_LINKED_TO_A_BUILDING_STOREY")).is(checkerContext.translate("ALL_OBJECTS_LINKED_TO_BUILDING_STOREY")).shouldBe(checkerContext.translate("ALL_OBJECT_LINKED1") + " "+ products.size() + " " + checkerContext.translate("ALL_OBJECTS_LINKED2")).add();
		}
	}
}