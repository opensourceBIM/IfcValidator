package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;

public class AtLeastOneBuilding extends ModelCheck {

	public AtLeastOneBuilding() {
		super("BUILDING", "AT_LEAST_ONE_BUILDING");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcBuilding> buildings = model.getAll(IfcBuilding.class);
		IfcBuilding building = buildings.size() == 1 ? buildings.get(0) : null;
		
		boolean valid = buildings.size() > 0;
		issueContainer.add(valid ? Type.SUCCESS : Type.ERROR, building == null ? null : "IfcBuilding", building == null ? null : building.getGlobalId(), building == null ? null : building.getOid(), translator.translate("NUMBER_OF_BUILDINGS"), buildings.size() + " " + translator.translate(buildings.size() == 1 ? "BUILDING_OBJECT" : "BUILDING_OBJECTS"), translator.translate("ATLEAST_ONE_BUILDING"));
		return valid;
	}
}