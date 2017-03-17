package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AtLeastOneBuildingStorey extends ModelCheck {

	public AtLeastOneBuildingStorey() {
		super("BUILDING_STOREY", "AT_LEAST_ONE_BUILDING_STOREY");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcBuildingStorey> buildingStories = model.getAll(IfcBuildingStorey.class);
		int size = buildingStories.size();
		IfcBuildingStorey buildingStorey = size == 1 ? buildingStories.get(0) : null;
		
		boolean valid = size > 0;
		issueContainer.add(valid ? Type.SUCCESS : Type.ERROR, buildingStorey == null ? null : "IfcBuildingStorey", buildingStorey == null ? null : buildingStorey.getGlobalId(), buildingStorey == null ? null : buildingStorey.getOid(), translator.translate("NUMBER_OF_BUILDING_STOREYS"), size + " " + translator.translate(size == 1 ? "BUILDING_STOREY_OBJECT" : "BUILDING_STOREY_OBJECTS"), translator.translate("ATLEAST_ONE_BUILDING_STOREY"));
		return valid;
	}
}