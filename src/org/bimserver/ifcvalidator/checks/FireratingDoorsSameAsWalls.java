package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFeatureElementSubtraction;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelVoidsElement;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.utils.IfcUtils;
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
			EList<IfcRelVoidsElement> openings = ifcWall.getHasOpenings();
			for (IfcRelVoidsElement ifcRelVoidsElement : openings) {
				IfcFeatureElementSubtraction relatedOpeningElement = ifcRelVoidsElement.getRelatedOpeningElement();
				if (relatedOpeningElement instanceof IfcOpeningElement) {
					IfcOpeningElement ifcOpeningElement = (IfcOpeningElement)relatedOpeningElement;
					EList<IfcRelFillsElement> hasFillings = ifcOpeningElement.getHasFillings();
					for (IfcRelFillsElement ifcRelFillsElement : hasFillings) {
						IfcElement relatedBuildingElement = ifcRelFillsElement.getRelatedBuildingElement();
						check(issueContainer, ifcWall, relatedBuildingElement, wallFireRating, checkerContext);
					}
				}
			}
		}
	}

	private void check(IssueContainer issueContainer, IfcWall ifcWall, IfcElement ifcElement, String wallFireRating, CheckerContext checkerContext) {
		String elementFireRating = IfcUtils.getStringProperty(ifcElement, "FireRating");
		if (elementFireRating == null) {
			if (wallFireRating == null) {
				// No need to report anything
			} else {
				issueContainer.builder().originatingCheck(this.getClass().getSimpleName()).author(checkerContext.getAuthor()).object(ifcElement).message(ifcElement.eClass().getName() + " has no fire rating").type(Type.CANNOT_CHECK).shouldBe(wallFireRating).add();
			}
		} else {
			if (wallFireRating == null) {
				// So the wall has no firerating, but the door/window has one, what to do?
			} else {
				if (wallFireRating.equalsIgnoreCase(elementFireRating)) {
					// OK
				} else {
					issueContainer.builder().originatingCheck(this.getClass().getSimpleName()).author(checkerContext.getAuthor()).object(ifcElement).message("Fire rating not the same as containing wall").type(Type.ERROR).is(elementFireRating).shouldBe(wallFireRating).add();
				}
			}
		}
	}
}
