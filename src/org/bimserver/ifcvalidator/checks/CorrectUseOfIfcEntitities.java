package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
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
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) {
		for (IfcSlab ifcSlab : model.getAll(IfcSlab.class)) {
			ifcSlab.getGeometry().getMaxBounds();
		}
		return true;
	}

	private void checkHeightLessThenWidthOrDepth(IfcProduct ifcProduct) {
//		GeometryInfo geometry = ifcProduct.getGeometry();
//		if (geometry != null) {
//			Vector3f max = geometry.getMaxBounds();
//			Vector3f min = geometry.getMinBounds();
//		}
	}
}
