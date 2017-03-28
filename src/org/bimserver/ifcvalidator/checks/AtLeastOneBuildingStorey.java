package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AtLeastOneBuildingStorey extends ModelCheck {

	public AtLeastOneBuildingStorey() {
		super("BUILDING_STOREY", "AT_LEAST_ONE_BUILDING_STOREY");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcBuildingStorey> buildingStories = model.getAll(IfcBuildingStorey.class);
		int size = buildingStories.size();
		IfcBuildingStorey buildingStorey = size == 1 ? buildingStories.get(0) : null;
		
		boolean valid = size > 0;
		issueContainer.builder().
			type(valid ? Type.SUCCESS : Type.ERROR).
			object(buildingStorey).
			message(checkerContext.translate(size == 1 ? "BUILDING_STOREY_OBJECT" : "BUILDING_STOREY_OBJECTS")).
			is(size + " " + checkerContext.translate(size == 1 ? "BUILDING_STOREY_OBJECT" : "BUILDING_STOREY_OBJECTS")).
			shouldBe(checkerContext.translate("ATLEAST_ONE_BUILDING_STOREY")).
			add();
	}
}