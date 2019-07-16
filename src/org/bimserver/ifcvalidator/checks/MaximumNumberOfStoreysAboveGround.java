package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class MaximumNumberOfStoreysAboveGround extends ModelCheck {

	private static final int MAX_ABOVE_GROUND = 6;
	
	public MaximumNumberOfStoreysAboveGround() {
		super("STOREYS", "MAX_ABOVE_GROUND");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		int storeysAboveGroundLevel = 0;
		for (IfcBuildingStorey ifcBuildingStorey : model.getAll(IfcBuildingStorey.class)) {
			if (ifcBuildingStorey.getElevation() > 0) {
				storeysAboveGroundLevel++;
			}
		}
		if (storeysAboveGroundLevel > MAX_ABOVE_GROUND) {
			issueContainer.builder().is(storeysAboveGroundLevel).shouldBe("< " + MAX_ABOVE_GROUND).message("Too many building storeys above ground level").type(Type.ERROR).add();
		} else {
			issueContainer.builder().is(storeysAboveGroundLevel).shouldBe("< " + MAX_ABOVE_GROUND).message("Correct amount of building storeys above ground level").type(Type.SUCCESS).add();
		}
	}
}