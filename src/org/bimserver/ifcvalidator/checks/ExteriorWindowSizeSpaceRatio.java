package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFeatureElementSubtraction;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelSpaceBoundary;
import org.bimserver.models.ifc2x3tc1.IfcRelVoidsElement;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class ExteriorWindowSizeSpaceRatio extends ModelCheck {

	private WindowSpaceRatioConfiguration conf;

	public ExteriorWindowSizeSpaceRatio(WindowSpaceRatioConfiguration conf) {
		super("GEOMETRY", "RATIOS");
		this.conf = conf;
	}

	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			double totalWindowArea = 0;
			for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcSpace.getBoundedBy()) {
				IfcElement relatedBuildingElement = ifcRelSpaceBoundary.getRelatedBuildingElement();
				if (relatedBuildingElement != null) {
					for (IfcRelVoidsElement ifcRelVoidsElement : relatedBuildingElement.getHasOpenings()) {
						IfcFeatureElementSubtraction relatedOpeningElement = ifcRelVoidsElement.getRelatedOpeningElement();
						if (relatedOpeningElement instanceof IfcOpeningElement) {
							IfcOpeningElement ifcOpeningElement = (IfcOpeningElement)relatedOpeningElement;
							for (IfcRelFillsElement ifcRelFillsElement : ifcOpeningElement.getHasFillings()) {
								IfcElement relatedBuildingElement2 = ifcRelFillsElement.getRelatedBuildingElement();
								if (relatedBuildingElement2 instanceof IfcWindow) {
									GeometryInfo windowGeometry = relatedBuildingElement2.getGeometry();
									if (windowGeometry != null) {
										totalWindowArea += windowGeometry.getArea();
									}
								}
							}
						}
					}
				}
			}
			if (ifcSpace.getGeometry() != null) {
				if (totalWindowArea * conf.getRatio() > ifcSpace.getGeometry().getArea()) {
					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Window/space area ratio", totalWindowArea * conf.getRatio(), " > " + ifcSpace.getGeometry().getArea());
				} else {
					issueInterface.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Window/space area ratio", totalWindowArea * conf.getRatio(), " > " + ifcSpace.getGeometry().getArea());
				}
			}
		}
		return issueInterface.isValid();
	}
}