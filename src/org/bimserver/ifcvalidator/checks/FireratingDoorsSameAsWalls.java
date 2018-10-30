package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFeatureElementSubtraction;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelVoidsElement;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.Issue;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;
import org.eclipse.emf.common.util.EList;

public class FireratingDoorsSameAsWalls extends ModelCheck {

	public FireratingDoorsSameAsWalls() {
		super("FIRERATING", "FIRERATING_DOORS_SAME_AS_WALLS");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcWall> walls = model.getAllWithSubTypes(IfcWall.class);
		for (IfcWall ifcWall : walls) {
			String wallFireRating = IfcUtils.getStringProperty(ifcWall, "FireRating");
			if (wallFireRating == null) {
				issueContainer.builder().object(ifcWall).message("Wall has no fire rating").type(Type.CANNOT_CHECK).add();
			} else {
				EList<IfcRelVoidsElement> openings = ifcWall.getHasOpenings();
				for (IfcRelVoidsElement ifcRelVoidsElement : openings) {
					IfcFeatureElementSubtraction relatedOpeningElement = ifcRelVoidsElement.getRelatedOpeningElement();
					if (relatedOpeningElement instanceof IfcOpeningElement) {
						IfcOpeningElement ifcOpeningElement = (IfcOpeningElement)relatedOpeningElement;
						EList<IfcRelFillsElement> hasFillings = ifcOpeningElement.getHasFillings();
						for (IfcRelFillsElement ifcRelFillsElement : hasFillings) {
							IfcElement relatedBuildingElement = ifcRelFillsElement.getRelatedBuildingElement();
							if (relatedBuildingElement instanceof IfcDoor) {
								check(issueContainer, ifcWall, (IfcDoor)relatedBuildingElement, wallFireRating);
							}
						}
					}
				}
			}
		}
	}

	private void check(IssueContainer issueContainer, IfcWall ifcWall, IfcDoor ifcDoor, String wallFireRating) {
		String doorFireRating = IfcUtils.getStringProperty(ifcDoor, "FireRating");
		if (doorFireRating == null) {
			issueContainer.builder().object(ifcDoor).message("Door has no fire rating").type(Type.CANNOT_CHECK).shouldBe(wallFireRating).add();
		} else {
			issueContainer.builder().object(ifcDoor).message("Fire rating not the same as containing wall").type(Type.ERROR).is(doorFireRating).shouldBe(wallFireRating).add();
		}
	}
}
