package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.Bounds;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.validationreport.IssueBuilder;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class CompletelyInBoundingBox extends ModelCheck {

	private static final double[] BOUNDS_MM = new double[] {-2500, -2500, -2500, 2500, 2500, 2500};

	public CompletelyInBoundingBox() {
		super("LOCATION", "IN_BOUNDING_BOX");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		boolean inBounds = true;
		double[] modelBounds = new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo != null) {
				Bounds boundsMm = geometryInfo.getBoundsMm();
				if (boundsMm.getMin().getX() < BOUNDS_MM[0] || boundsMm.getMin().getY() < BOUNDS_MM[1] || boundsMm.getMin().getZ() < BOUNDS_MM[2]) {
					inBounds = false;
				}
				if (boundsMm.getMax().getX() > BOUNDS_MM[3] || boundsMm.getMax().getY() > BOUNDS_MM[4] || boundsMm.getMax().getZ() > BOUNDS_MM[5]) {
					inBounds = false;
				}
				if (boundsMm.getMin().getX() < modelBounds[0]) {
					modelBounds[0] = boundsMm.getMin().getX();
				}
				if (boundsMm.getMin().getY() < modelBounds[1]) {
					modelBounds[1] = boundsMm.getMin().getY();
				}
				if (boundsMm.getMin().getZ() < modelBounds[2]) {
					modelBounds[2] = boundsMm.getMin().getZ();
				}
				if (boundsMm.getMax().getX() > modelBounds[3]) {
					modelBounds[3] = boundsMm.getMax().getX();
				}
				if (boundsMm.getMax().getY() > modelBounds[4]) {
					modelBounds[4] = boundsMm.getMax().getY();
				}
				if (boundsMm.getMax().getZ() > modelBounds[5]) {
					modelBounds[5] = boundsMm.getMax().getZ();
				}
			}
		}
		IssueBuilder builder = issueContainer.builder().shouldBe("[" + BOUNDS_MM[0] + ", " + BOUNDS_MM[1] + ", " + BOUNDS_MM[2] + "] - [" + BOUNDS_MM[3] + ", " + BOUNDS_MM[4] + ", " + BOUNDS_MM[5] + "]");
		builder.is("[" + modelBounds[0] + ", " + modelBounds[1] + ", " + modelBounds[2] + "] - [" + modelBounds[3] + ", " + modelBounds[4] + ", " + modelBounds[5] + "]");
		if (inBounds) {
			builder.message("Model is completely within given bounds").type(Type.SUCCESS);
		} else {
			builder.message("Model is not completely within given bounds").type(Type.ERROR);
		}
		builder.add();
	}
}
