package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.Tristate;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class FacadeMaterial extends ModelCheck {

	public FacadeMaterial() {
		super("FACADE", "MATERIAL");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			Tristate isExternal = IfcUtils.getBooleanProperty(ifcProduct, "IsExternal");
			if (isExternal == Tristate.TRUE) {
				// Check properties
				String material = IfcUtils.getStringProperty(ifcProduct, "02 Materjal");
				if (material != null && material.toLowerCase().contains("raudbetoon")) {
					issueContainer.builder().object(ifcProduct).type(Type.SUCCESS).message("External facade object has the correct material").is(material).shouldBe("Raudbetoon").add();
				} else {
					// Check the materials
					String materialsString = IfcUtils.getMaterial(ifcProduct);
					if (materialsString.toLowerCase().contains("raudbetoon")) {
						issueContainer.builder().object(ifcProduct).type(Type.SUCCESS).message("External facade object has the correct material").is(materialsString).shouldBe("Raudbetoon").add();
					} else {
						issueContainer.builder().object(ifcProduct).type(Type.ERROR).message("External facade object should have the correct material").is(material == null ? "[Missing]" : material).shouldBe("Raudbetoon").add();
					}
				}
			}
		}
	}
}