package org.bimserver.ifcvalidator.checks;

import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.utils.IfcTools2D;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueBuilder;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class MaximumGroundArea extends ModelCheck {

	private static final double MAX_GROUND_AREA_M2 = 1000;
	
	public MaximumGroundArea() {
		super("GROUND", "MAX_GROUND_AREA");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		Area area = new Area();
		
		Set<IfcBuildingStorey> groundLevelStoreys = new HashSet<>();
		
		for (IfcBuildingStorey ifcBuildingStorey : model.getAll(IfcBuildingStorey.class)) {
			if (ifcBuildingStorey.getElevation() >= 0 && ifcBuildingStorey.getElevation() < 0.5) {
				groundLevelStoreys.add(ifcBuildingStorey);
			}
		}
		
		if (groundLevelStoreys.isEmpty()) {
			issueContainer.builder().message("No groundlevel(s) found in model").type(Type.ERROR).add();
			return;
		}

		TreeMap<Double, IfcProduct> areas = new TreeMap<>();
		float lengthUnitPrefix = IfcUtils.getLengthUnitPrefix(model) * 1000;
		IfcTools2D ifcTools2D = new IfcTools2D();

		for (IfcBuildingStorey ifcBuildingStorey : groundLevelStoreys) {
			for (IfcProduct ifcProduct : IfcUtils.getDecompositionAndContainmentRecursive(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcSite || ifcProduct instanceof IfcSpace) {
					continue;
				}
				GeometryInfo geometryInfo = ifcProduct.getGeometry();
				if (geometryInfo == null) {
					continue;
				}
				
				Area newArea = ifcTools2D.get2D(ifcProduct, lengthUnitPrefix);
				
				// Old code, using the aabb
//				Bounds boundsMm = geometryInfo.getBoundsMm();
//				Rectangle rectangle = new Rectangle((int)boundsMm.getMin().getX(), (int)boundsMm.getMin().getY(), (int)(boundsMm.getMax().getX() - boundsMm.getMin().getX()), (int)(boundsMm.getMax().getY() - boundsMm.getMin().getY()));
//				Area newArea = new Area(rectangle);

				float areaM2 = IfcTools2D.getArea(newArea) / 1000000;
				
				if (Float.isNaN(areaM2) || Float.isInfinite(areaM2)) {
					// Skip
				} else {
					areas.put((double) areaM2, ifcProduct);
					area.add(newArea);
				}
			}
		}

//		Entry<Double, IfcProduct> last = areas.lastEntry();
//		System.out.println("Biggest 10");
//		for (int i=0; i<10; i++) {
//			System.out.println(last.getKey() + " " + last.getValue().eClass().getName() + " - " + last.getValue().getName());
//			last = areas.lowerEntry(last.getKey());
//		}
//		System.out.println();
//		System.out.println("Smallest 10");
//		last = areas.firstEntry();
//		for (int i=0; i<10; i++) {
//			System.out.println(last.getKey() + " " + last.getValue().eClass().getName() + " - " + last.getValue().getName());
//			last = areas.higherEntry(last.getKey());
//		}
//
		float areaM2 = IfcTools2D.getArea(area) / 1000000;
//		System.out.println("Area: " + areaM2 + "m2");
//		new Display("Area", 1024, 768, area);
		
		IssueBuilder builder = issueContainer.builder().is(areaM2 + "m2").shouldBe("<= " + MAX_GROUND_AREA_M2 + "m2");
		if (areaM2 > MAX_GROUND_AREA_M2) {
			builder.type(Type.ERROR).message("Ground area is too large");
		} else {
			builder.type(Type.SUCCESS).message("Ground area is within the limits");
		}
		builder.add();
	}
}