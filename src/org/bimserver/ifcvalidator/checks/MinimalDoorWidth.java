package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.utils.IfcUtils;
import org.bimserver.utils.LengthUnit;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class MinimalDoorWidth extends ModelCheck {

	// For now this is a fixed width, should become a setting
	private static final float MINIMAL_WIDTH_MM = 1100;
	
	public MinimalDoorWidth() {
		super("SPACING", "MINIMAL_DOOR_WIDTH");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		LengthUnit modelLengthUnit = IfcUtils.getLengthUnit(model);
		for (IfcDoor ifcDoor : model.getAll(IfcDoor.class)) {
			double overallWidthInModelUnits = ifcDoor.getOverallWidth();
			double overallWidthInMm = LengthUnit.MILLI_METER.convert(overallWidthInModelUnits, modelLengthUnit);
			if (overallWidthInMm < MINIMAL_WIDTH_MM) {
				issueContainer.builder().object(ifcDoor).type(Type.ERROR).message("OverallWidth of Door not sufficient").shouldBe(">= " + MINIMAL_WIDTH_MM).is(overallWidthInMm).add();
			} else {
				issueContainer.builder().object(ifcDoor).type(Type.SUCCESS).message("Door has the correct minimum width").shouldBe(">=" + MINIMAL_WIDTH_MM).is(overallWidthInMm).add();
			}
		}
	}
}
