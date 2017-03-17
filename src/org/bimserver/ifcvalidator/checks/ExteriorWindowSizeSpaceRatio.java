package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFeatureElementSubtraction;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelSpaceBoundary;
import org.bimserver.models.ifc2x3tc1.IfcRelVoidsElement;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.ifc2x3tc1.Tristate;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class ExteriorWindowSizeSpaceRatio extends ModelCheck {

	private WindowSpaceRatioConfiguration conf;

	public ExteriorWindowSizeSpaceRatio(WindowSpaceRatioConfiguration conf) {
		super("GEOMETRY", "RATIOS");
		this.conf = conf;
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			double totalWindowArea = 0;
			int nrWindowsUsed = 0;
			for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcSpace.getBoundedBy()) {
				IfcElement relatedBuildingElement = ifcRelSpaceBoundary.getRelatedBuildingElement();
				if (relatedBuildingElement != null) {
					boolean wallExternal = IfcUtils.getBooleanProperty(relatedBuildingElement, "IsExternal") == Tristate.TRUE;
					for (IfcRelVoidsElement ifcRelVoidsElement : relatedBuildingElement.getHasOpenings()) {
						IfcFeatureElementSubtraction relatedOpeningElement = ifcRelVoidsElement.getRelatedOpeningElement();
						if (relatedOpeningElement instanceof IfcOpeningElement) {
							IfcOpeningElement ifcOpeningElement = (IfcOpeningElement)relatedOpeningElement;
							for (IfcRelFillsElement ifcRelFillsElement : ifcOpeningElement.getHasFillings()) {
								IfcElement relatedBuildingElement2 = ifcRelFillsElement.getRelatedBuildingElement();
								if (relatedBuildingElement2 instanceof IfcWindow) {
									IfcWindow ifcWindow = (IfcWindow)relatedBuildingElement2;
									boolean windowExternal = IfcUtils.getBooleanProperty(ifcWindow, "IsExternal") == Tristate.TRUE;
									if (windowExternal || wallExternal) {
										double semanticArea = ifcWindow.getOverallWidth() * ifcWindow.getOverallHeight();
										GeometryInfo windowGeometry = relatedBuildingElement2.getGeometry();
										if (windowGeometry != null) {
											windowGeometry.getMaxBoundsUntranslated();
											double geometryArea = getBiggestSingleFaceOfUntranslatedBoundingBox(windowGeometry);
											if (Math.abs(geometryArea - semanticArea) > 0.001) {
												issueContainer.add(Type.ERROR, ifcWindow.eClass().getName(), ifcWindow.getGlobalId(), ifcWindow.getOid(), "Window area of geometry not consistent with semantic area (OverallWidth*OverallHeight)", String.format("%.2f", (semanticArea)), String.format("%.2f", (geometryArea)));
											} else {
												totalWindowArea += geometryArea;
												nrWindowsUsed++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (nrWindowsUsed == 0) {
				issueContainer.add(Type.CANNOT_CHECK, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Cannot check window/space ratio because no consistent (exterior) windows found in space \"" + ifcSpace.getName() + "\"", "", "");
			} else {
				if (ifcSpace.getGeometry() != null) {
					if (totalWindowArea * conf.getRatio() > ifcSpace.getGeometry().getArea()) {
						issueContainer.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Window/space area ratio for space \"" + ifcSpace.getName() + "\"", String.format("%.2f", (totalWindowArea * conf.getRatio())), " > " + String.format("%.2f", ifcSpace.getGeometry().getArea()));
					} else {
						issueContainer.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Window/space area ratio for space \"" + ifcSpace.getName() + "\"", String.format("%.2f", (totalWindowArea * conf.getRatio())), " > " + String.format("%.2f", ifcSpace.getGeometry().getArea()));
					}
				}
			}
		}
		return issueContainer.isValid();
	}
	
	private double getBiggestSingleFaceOfUntranslatedBoundingBox(GeometryInfo geometryInfo) {
		Vector3f max = geometryInfo.getMaxBoundsUntranslated();
		Vector3f min = geometryInfo.getMinBoundsUntranslated();
		
		double width = max.getX() - min.getX();
		double height = max.getY() - min.getY();
		double depth = max.getZ() - min.getZ();
		
		double biggestArea = width * height;
		if (height * depth > biggestArea) {
			biggestArea = height * depth;
		}
		if (depth * width > biggestArea) {
			biggestArea = depth * width;
		}
		return biggestArea;
	}
}