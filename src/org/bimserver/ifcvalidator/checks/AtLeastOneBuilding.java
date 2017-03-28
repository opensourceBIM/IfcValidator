package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class AtLeastOneBuilding extends ModelCheck {

	public AtLeastOneBuilding() {
		super("BUILDING", "AT_LEAST_ONE_BUILDING");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcBuilding> buildings = model.getAll(IfcBuilding.class);
		IfcBuilding building = buildings.size() == 1 ? buildings.get(0) : null;
		
		boolean valid = buildings.size() > 0;
		issueContainer.builder().type(valid ? Type.SUCCESS : Type.ERROR).object(building).message(checkerContext.translate("NUMBER_OF_BUILDINGS")).add();
//		issueContainer.add(, building == null ? null : "IfcBuilding", building == null ? null : building.getGlobalId(), building == null ? null : building.getOid(), translator.translate("NUMBER_OF_BUILDINGS"), buildings.size() + " " + translator.translate(buildings.size() == 1 ? "BUILDING_OBJECT" : "BUILDING_OBJECTS"), translator.translate("ATLEAST_ONE_BUILDING"));
	}
}