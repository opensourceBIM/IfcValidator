package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.Bounds;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

/*
 * Validates the height of the building according to a maximum above ground level.
 * Ground-level is assumed to be at the local z-coordinate 0
 *
 */
public class MaxBuildingHeightAboveGroundLevel extends ModelCheck {

	// TODO make this a setting
	private static final double MAX_HEIGHT_ABOVE_GROUND_LEVEL_MM = 10000d;
	
	public MaxBuildingHeightAboveGroundLevel() {
		super("BUILDING", "MAX_HEIGHT_ABOVE_GROUND_LEVEL");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		double maxModelHeight = -Double.MAX_VALUE;
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			if (ifcProduct.getGeometry() != null) {
				GeometryInfo geometryInfo = ifcProduct.getGeometry();
				Bounds objectBoundsMm = geometryInfo.getBoundsMm();
				if (objectBoundsMm.getMax().getZ() > maxModelHeight) {
					maxModelHeight = objectBoundsMm.getMax().getZ();
				}
			}
		}
		if (maxModelHeight == -Double.MAX_VALUE) {
			issueContainer.builder().is(maxModelHeight).shouldBe("<= " + MAX_HEIGHT_ABOVE_GROUND_LEVEL_MM).type(Type.CANNOT_CHECK).message("Building height could not be determined").add();
		} else {
			if (maxModelHeight > MAX_HEIGHT_ABOVE_GROUND_LEVEL_MM) {
				issueContainer.builder().is((int)maxModelHeight + "mm").shouldBe("<= " + MAX_HEIGHT_ABOVE_GROUND_LEVEL_MM).type(Type.ERROR).message("Model height is higher than maximum height").add();
			} else {
				issueContainer.builder().is((int)maxModelHeight + "mm").shouldBe("<= " + MAX_HEIGHT_ABOVE_GROUND_LEVEL_MM).type(Type.SUCCESS).message("Model height is less than maximum height").add();
			}
		}
	}
}